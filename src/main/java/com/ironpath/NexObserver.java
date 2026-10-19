package com.ironpath;

import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.nex.NexCapture;
import com.ironpath.nex.NexSession;
import com.ironpath.nex.NexTargetState;
import com.ironpath.preferences.NexMethod;
import java.util.*;
import net.runelite.api.*;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarbitID;

/** Client-thread public-event adapter; mappings and limits are reviewed in docs/research/nex-events.md. */
final class NexObserver
{
	final NexCapture capture;
	private final Client client;
	private final CombatStateReader reader;
	private final Set<Integer> melee=new HashSet<>(), ranged=new HashSet<>();
	private final Map<NPC,String> targets=new IdentityHashMap<>();
	private final Set<Hitsplat> hits=Collections.newSetFromMap(new IdentityHashMap<>());
	private final List<String> signals=new ArrayList<>();
	private NPC boss;
	private int barrier=-1, boundary=-100, smoke=-100, bossGone=-1, bossForm=-1, view=Integer.MIN_VALUE;
	private boolean attempted, arenaObserved;
	private long spawnSerial, hitSerial;
	private NexMethod method=NexMethod.PILOT;
	private com.ironpath.nex.NexContext defaults=com.ironpath.nex.NexContext.PILOT;
	private boolean enabled=true;
	private int rosterTick=Integer.MIN_VALUE;
	private String cachedRoster;
	private static final Set<Integer> RESOURCE_VARBITS=new HashSet<>();
	static
	{
		for(Prayer prayer:Prayer.values()) { RESOURCE_VARBITS.add(prayer.getVarbit()); }
		RESOURCE_VARBITS.addAll(Set.of(VarbitID.ARCEUUS_DEATH_CHARGE_ACTIVE,VarbitID.ARCEUUS_DEATH_CHARGE_COOLDOWN,
			VarbitID.WORN_ITEMS_DIZANAS_QUIVER_CHARGED,VarbitID.CHARGES_DIZANAS_QUIVER_QUANTITY,
			VarbitID.RUNE_POUCH_TYPE_1,VarbitID.RUNE_POUCH_TYPE_2,VarbitID.RUNE_POUCH_TYPE_3,VarbitID.RUNE_POUCH_TYPE_4,VarbitID.RUNE_POUCH_TYPE_5,VarbitID.RUNE_POUCH_TYPE_6,
			VarbitID.RUNE_POUCH_QUANTITY_1,VarbitID.RUNE_POUCH_QUANTITY_2,VarbitID.RUNE_POUCH_QUANTITY_3,VarbitID.RUNE_POUCH_QUANTITY_4,VarbitID.RUNE_POUCH_QUANTITY_5,VarbitID.RUNE_POUCH_QUANTITY_6));
	}
	static boolean resourceChanged(int varbit, int varp)
	{
		return RESOURCE_VARBITS.contains(varbit) || Set.of(
			net.runelite.api.gameval.VarPlayerID.SA_ENERGY,net.runelite.api.gameval.VarPlayerID.SA_ATTACK,
			net.runelite.api.gameval.VarPlayerID.COM_MODE,net.runelite.api.gameval.VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO,
			net.runelite.api.gameval.VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO_AMOUNT).contains(varp);
	}

	NexObserver(Client client, KnowledgeCatalog catalog, NexSession session)
	{
		this.client=client; reader=new CombatStateReader(client); capture=new NexCapture(session::add);
		catalog.melee.items.stream().filter(i->i.slot.equals("weapon")).forEach(i->melee.add(i.id));
		catalog.ranged.items.stream().filter(i->i.slot.equals("weapon")).forEach(i->ranged.add(i.id));
	}
	synchronized boolean enabled() { return enabled; }
	synchronized void enable(boolean value) { clearActive(); enabled=value; }
	synchronized void method(NexMethod method)
	{
		if (!this.method.cohort().equals(method.cohort()))
		{ capture.abort(client.getTickCount(),Outcome.CENSORED,"Method or planning locks changed during capture"); }
		this.method=method;
	}
	/** Future defaults only. Never mutate or censor an active attempt. */
	synchronized void defaults(com.ironpath.nex.NexContext context) { defaults=context.inherited(); }
	static boolean bossId(int id)
	{ return id==NpcID.NEX || id==NpcID.NEX_SPAWNING || id==NpcID.NEX_SOULSPLIT || id==NpcID.NEX_DEFLECT || id==NpcID.NEX_DYING; }
	static String targetName(int id)
	{
		if (bossId(id)) { return "NEX"; }
		switch(id)
		{
			case NpcID.NEX_SMOKEMAGE: return "FUMUS";
			case NpcID.NEX_SHADOWMAGE: return "UMBRA";
			case NpcID.NEX_BLOODMAGE: return "CRUOR";
			case NpcID.NEX_ICEMAGE: return "GLACIES";
			default: return null;
		}
	}
	synchronized boolean inArena() { return boss!=null && (barrier==2 || barrier==3); }
	synchronized boolean arenaExitObserved() { return barrier==0 || barrier==1; }
	/** A barrier exit may precede LOADING. Preserve this source only until that scene transition. */
	synchronized boolean bankTravelSource() { return arenaObserved || inArena(); }
	/** Same public arena evidence used to revalidate a bank return, independent of preference IO readiness. */
	synchronized void confirmArenaSource() { arenaObserved=true; }
	synchronized void clearActive()
	{ capture.clear(); targets.clear(); hits.clear(); signals.clear(); boss=null; attempted=arenaObserved=false; bossGone=bossForm=-1; barrier=-1; boundary=smoke=-100; view=Integer.MIN_VALUE; rosterTick=Integer.MIN_VALUE; cachedRoster=null; }
	private void signal(String value)
	{
		String entry="tick "+client.getTickCount()+" · "+value;
		if(capture.active()) { capture.signal(entry); return; }
		if(signals.size()>=NexCapture.MAX_SIGNALS) { signals.remove(NexCapture.MAX_SIGNALS/2); }
		signals.add(entry);
	}
	synchronized void transition()
	{
		signal("scene transition; barrier "+barrier);
		capture.flush(client.getTickCount());
		capture.abort(client.getTickCount(),Outcome.CENSORED,"Left arena or scene loading during active capture");
		clearActive();
	}
	private boolean sameView(Actor actor)
	{ return actor!=null && actor.getWorldView()!=null && actor.getWorldView().getId()==view; }
	synchronized void spawn(NPC actor)
	{
		if (!enabled || actor==null || targetName(actor.getId())==null || actor.getWorldView()==null) { return; }
		Player local=client.getLocalPlayer();
		if (local==null || local.getWorldView()==null || actor.getWorldView().getId()!=local.getWorldView().getId()) { return; }
		if (bossId(actor.getId()))
		{
			if (boss!=null && boss!=actor)
			{ capture.abort(client.getTickCount(),Outcome.CENSORED,"Nex spawn identity replaced"); targets.clear(); boundary=smoke=-100; }
			if(boss!=actor || bossForm!=actor.getId()) { signal("Nex form "+actor.getId()+(boss!=actor?" (new actor)":" (same actor)")); }
			if(boss!=actor) { attempted=false; }
			bossForm=actor.getId();
			boss=actor; bossGone=-1; view=actor.getWorldView().getId();
			if(inArena()) { arenaObserved=true; }
		}
		if (!targets.containsKey(actor))
		{
			if (targets.size()>=32) { capture.abort(client.getTickCount(),Outcome.CENSORED,"Target identity limit"); return; }
			targets.put(actor,targetName(actor.getId())+"@"+actor.getWorldView().getId()+":"+actor.getIndex()+":"+(++spawnSerial));
		}
		if (actor==boss && actor.getId()==NpcID.NEX_DYING) { capture.death(client.getTickCount()); }
	}
	synchronized void changed(NPC actor)
	{
		if (actor==boss && !bossId(actor.getId()))
		{ capture.abort(client.getTickCount(),Outcome.CENSORED,"Unreviewed Nex transformation"); clearActive(); }
		else if (targetName(actor.getId())!=null) { spawn(actor); }
	}
	synchronized void despawn(NPC actor)
	{
		// Keep boss identity until the end grace window; despawn alone never completes a kill.
		if (actor!=boss) { targets.remove(actor); } else { signal("Nex despawn"); bossGone=client.getTickCount(); }
	}
	synchronized void barrier()
	{ barrier(client.getVarbitValue(VarbitID.NEX_BARRIER),"poll"); }
	synchronized void barrier(int next)
	{ barrier(next,"event"); }
	private void barrier(int next,String source)
	{
		if (!enabled) { return; }
		int tick=client.getTickCount();
		boolean changed=next!=barrier;
		if(changed) { signal("barrier "+(barrier<0?"initial":barrier)+"→"+next+" ("+source+")"); }
		if (barrier==2 && next==3)
		{ boundary=tick; if(!capture.active()) { attempted=false; } capture.barrierStart(tick); }
		if (barrier==3 && next==2) { capture.barrierEnd(tick); }
		if (barrier==3 && (next==0 || next==1))
		{
			capture.flush(tick); capture.abort(tick,Outcome.CENSORED,"Arena left before correlated completion");
		}
		barrier=next;
		if(inArena()) { arenaObserved=true; }
		startIfReady(false);
		if(changed) { sample(-1); }
	}
	synchronized void chat(ChatMessageType type, String message)
	{
		if (type!=ChatMessageType.NPC_SAY && type!=ChatMessageType.GAMEMESSAGE || message==null) { return; }
		String text=message.replaceAll("<[^>]+>","").trim();
		if (text.startsWith("Nex|") || text.startsWith("Nex:")) { overhead(boss,text.substring(4).trim()); }
	}
	synchronized void overhead(Actor actor, String raw)
	{
		if (!enabled || actor!=boss || !sameView(actor) || raw==null) { return; }
		String text=raw.replaceAll("<[^>]+>","").trim().toLowerCase(Locale.ROOT);
		int tick=client.getTickCount(); String phase=null, window=null;
		switch(text)
		{
			case "fill my soul with smoke!": smoke=tick; phase="SMOKE"; break;
			case "darken my shadow!": phase="SHADOW"; break;
			case "flood my lungs with blood!": phase="BLOOD"; break;
			case "infuse me with the power of ice!": phase="ICE"; break;
			case "now, the power of zaros!": phase="ZAROS"; break;
			case "embrace darkness!": window="EMBRACE_DARKNESS"; break;
			case "fear the shadow!": window="FEAR_THE_SHADOW"; break;
			case "a siphon will solve this!": window="BLOOD_SIPHON"; break;
			case "i demand a blood sacrifice!": window="BLOOD_SACRIFICE"; break;
			case "contain this!": window="ICE_CONTAIN"; break;
			case "die now, in a prison of ice!": window="ICE_PRISON"; break;
			case "fumus, don't fail me!": window="MINION_FUMUS"; break;
			case "umbra, don't fail me!": window="MINION_UMBRA"; break;
			case "cruor, don't fail me!": window="MINION_CRUOR"; break;
			case "glacies, don't fail me!": window="MINION_GLACIES"; break;
			default: return;
		}
		if("SMOKE".equals(phase)) { signal("smoke line observed"); }
		startIfReady(false);
		if (capture.active())
		{
			capture.phase(tick,phase,window); sample(-1);
		}
	}
	synchronized void tick()
	{
		if (!enabled) { return; }
		var world=client.getTopLevelWorldView();
		if (world!=null && world.npcs()!=null) { world.npcs().stream().filter(n->targetName(n.getId())!=null).forEach(this::spawn); }
		barrier();
		Player local=client.getLocalPlayer(); int tick=client.getTickCount();
		if (capture.active() && (local==null || local.isDead()))
		{ capture.abort(tick,local==null?Outcome.CENSORED:Outcome.FAILED,"Local player died/unavailable"); return; }
		if(capture.active() && bossGone>=0 && tick-bossGone>NexCapture.END_WAIT)
		{ capture.flush(tick); capture.abort(tick,Outcome.CENSORED,"Nex despawned without correlated completion"); }
		if (capture.active() && (!sameView(local) || !sameView(boss)))
		{ capture.abort(tick,Outcome.CENSORED,"World-view identity changed"); clearActive(); return; }
		startIfReady(true);
		sample(-1);
		if(!capture.active()) { hits.clear(); }
	}
	private void startIfReady(boolean allowPartial)
	{
		int tick=client.getTickCount();
		boolean smokeStart=smoke==tick;
		if(capture.active())
		{
			if(smokeStart) { capture.anchorSmoke(tick,roster()); }
			return;
		}
		if(attempted || boss==null || (barrier!=3 && !(smokeStart && barrier==2))
			|| boss.getId()==NpcID.NEX_DYING || !sameView(client.getLocalPlayer())) { return; }
		if(!smokeStart && !allowPartial && boundary!=tick) { return; }
		hits.clear(); attempted=true;
		capture.start(tick,smokeStart,method,roster(),defaults);
		if(boundary>=0 && boundary<=tick) { capture.barrierStart(boundary); }
		signals.forEach(capture::signal);
		signals.clear();
		// Sample the actual start callback, without reconstructing earlier equipment or phase.
		sample(-1);
	}
	private String roster()
	{
		int tick=client.getTickCount();
		if(rosterTick!=tick) { rosterTick=tick; cachedRoster=readRoster(); }
		return cachedRoster;
	}
	private String readRoster()
	{
		var world=client.getTopLevelWorldView();
		if (world==null || world.players()==null || view==Integer.MIN_VALUE) { return null; }
		Set<String> roster=new TreeSet<>();
		for (Player player:world.players())
		{
			if (player.getWorldView()==null || player.getWorldView().getId()!=view || player.getName()==null) { return null; }
			// Session-only hashes support observed-roster cohorts, not server party membership.
			roster.add(com.ironpath.preferences.LocalPreferences.key(player.getName().toLowerCase(Locale.ROOT)));
		}
		return roster.isEmpty()?null:String.join(",",roster);
	}
	synchronized void sample(int animation)
	{
		if (!enabled || !capture.active()) { return; }
		Player local=client.getLocalPlayer();
		NPC npc=local!=null && local.getInteracting() instanceof NPC?(NPC)local.getInteracting():null;
		String target=npc!=null && sameView(npc)?targets.get(npc):null;
		NexTargetState targetState=target==null?NexTargetState.UNKNOWN:targetState(npc);
		var state=reader.read(); int weapon=state.slots.getOrDefault("weapon",-1);
		String role=state.specialSelected?"spec candidate":ranged.contains(weapon)?"ranged":melee.contains(weapon)?"melee":"unclassified";
		capture.sample(client.getTickCount(),state,target,role,animation,roster(),targetState);
	}
	private static NexTargetState targetState(NPC npc)
	{
		NexTargetState.Form form;
		switch(npc.getId())
		{
			case NpcID.NEX: form=NexTargetState.Form.NEX; break;
			case NpcID.NEX_SPAWNING: form=NexTargetState.Form.SPAWNING; break;
			case NpcID.NEX_SOULSPLIT: form=NexTargetState.Form.SOUL_SPLIT; break;
			case NpcID.NEX_DEFLECT: form=NexTargetState.Form.DEFLECT_MELEE; break;
			case NpcID.NEX_DYING: form=NexTargetState.Form.DYING; break;
			default: form=NexTargetState.Form.MINION;
		}
		return new NexTargetState(form,npc.getHealthRatio(),npc.getHealthScale());
	}
	synchronized void hit(HitsplatApplied event)
	{
		if (!enabled || !capture.active() || !(event.getActor() instanceof NPC)) { return; }
		NPC target=(NPC)event.getActor(); Hitsplat hit=event.getHitsplat();
		if (targetName(target.getId())==null || !sameView(target) || !targets.containsKey(target) || !hits.add(hit)) { return; }
		if (hits.size()>NexCapture.MAX_EVENTS) { capture.abort(client.getTickCount(),Outcome.CENSORED,"Hit identity limit"); hits.clear(); return; }
		capture.hit(client.getTickCount(),++hitSerial,targets.get(target),hit.getAmount(),hit.isMine(),hit.isOthers(),hit.getHitsplatType()==HitsplatID.HEAL);
	}
	synchronized void death(Actor actor)
	{
		if (actor==client.getLocalPlayer()) { capture.abort(client.getTickCount(),Outcome.FAILED,"Player death"); }
		else if (actor==boss && sameView(actor)) { signal("Nex ActorDeath"); capture.death(client.getTickCount()); }
	}
}
