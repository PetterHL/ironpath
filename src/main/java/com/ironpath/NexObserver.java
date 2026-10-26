package com.ironpath;

import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.nex.NexCapture;
import com.ironpath.nex.NexSession;
import com.ironpath.nex.NexTargetState;
import com.ironpath.nex.NexHealth;
import com.ironpath.nex.NexObservations;
import com.ironpath.nex.NexActions;
import com.ironpath.nex.NexCallbackDiagnostics;
import static com.ironpath.nex.NexCallbackDiagnostics.Stage.*;
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
	private final Map<Integer,Integer> targetMaxima=new HashMap<>();
	private final Map<NPC,String> targets=new IdentityHashMap<>();
	// Diagnostic object reuse only. Object identity is not a public event key.
	private final Set<Hitsplat> hits=Collections.newSetFromMap(new IdentityHashMap<>());
	private final Set<Projectile> projectiles=Collections.newSetFromMap(new IdentityHashMap<>());
	private final Map<ActorSpotAnim,Integer> effects=new IdentityHashMap<>();
	private long callbackOrder;
	private final java.util.Deque<NexActions.Signal> actionContext=new ArrayDeque<>();
	private boolean actionContextTruncated;
	private NPC hudTarget;
	private int hudTick=-1,hudCycle=-1,hudDefinition=-1,hudHp=-1,hudMaximum=-1,hudUpdateTick=-1;
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
		RESOURCE_VARBITS.addAll(Set.of(VarbitID.ARCEUUS_DEATH_CHARGE_ACTIVE,VarbitID.ARCEUUS_DEATH_CHARGE_COOLDOWN,VarbitID.DEATH_CHARGE_SCROLL_USED,
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
		catalog.comparison.targets.stream().filter(t->t.id.startsWith("nex-") && t.hitpoints!=null)
			.forEach(t->targetMaxima.put(t.npcId,t.hitpoints));
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
	{ capture.clear(); actionContext.clear(); actionContextTruncated=false; targets.clear(); hits.clear(); projectiles.clear(); effects.clear(); resetHud(); signals.clear(); boss=null; attempted=arenaObserved=false; bossGone=bossForm=-1; barrier=-1; boundary=smoke=-100; view=Integer.MIN_VALUE; rosterTick=Integer.MIN_VALUE; cachedRoster=null; }
	private void invalidateHud() { hudTarget=null; hudTick=hudCycle=hudDefinition=hudHp=hudMaximum=-1; }
	private void resetHud() { invalidateHud(); hudUpdateTick=-1; }
	synchronized void interactingChanged(Actor actor) { if(actor==client.getLocalPlayer()) { resetHud(); action(NexActions.Kind.INTERACTION,NexActions.Source.LOCAL_INTERACTION); } }
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
			{ capture.abort(client.getTickCount(),Outcome.CENSORED,"Nex spawn identity replaced"); targets.clear(); actionContext.clear(); actionContextTruncated=false; projectiles.clear(); effects.clear(); resetHud(); boundary=smoke=-100; }
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
		if(actor==hudTarget || client.getLocalPlayer()!=null && client.getLocalPlayer().getInteracting()==actor) { resetHud(); }
		if (actor==boss && !bossId(actor.getId()))
		{ capture.abort(client.getTickCount(),Outcome.CENSORED,"Unreviewed Nex transformation"); clearActive(); }
		else if (targetName(actor.getId())!=null) { spawn(actor); }
	}
	synchronized void despawn(NPC actor)
	{
		if(actor==hudTarget) { resetHud(); }
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
		if(!enabled || message==null || message.length()>512) { return; }
		if(type!=ChatMessageType.NPC_SAY && type!=ChatMessageType.GAMEMESSAGE && type!=ChatMessageType.SPAM && type!=ChatMessageType.ENGINE) { return; }
		String text=message.replaceAll("<[^>]+>","").trim();
		if(type==ChatMessageType.ENGINE)
		{
			// Pinned public ENGINE contract is a game-engine message. Admit only exact DC success text.
			var kind=NexActions.message(text);
			if(kind==NexActions.Kind.DC_NORMAL || kind==NexActions.Kind.DC_UPGRADED) { action(kind,NexActions.Source.ENGINE_MESSAGE); }
			return;
		}
		if(type==ChatMessageType.GAMEMESSAGE || type==ChatMessageType.SPAM)
		{
			var kind=NexActions.message(text);
			if(kind!=null) { action(kind,type==ChatMessageType.SPAM?NexActions.Source.SPAM_MESSAGE:NexActions.Source.GAME_MESSAGE); }
		}
		if (type!=ChatMessageType.SPAM && (text.startsWith("Nex|") || text.startsWith("Nex:"))) { overhead(boss,text.substring(4).trim()); }
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
		action(NexActions.Kind.SETTLED,NexActions.Source.GAME_TICK);
		var settled=sample(-1);
		if(capture.active())
		{
			NPC target=local!=null && local.getInteracting() instanceof NPC?(NPC)local.getInteracting():null;
			capture.resource(new NexObservations.Signal(NexObservations.Kind.TICK_RESOURCES,tick,client.getGameCycle(),++callbackOrder,
				client.getVarpValue(net.runelite.api.gameval.VarPlayerID.SA_ENERGY),client.getServerVarpValue(net.runelite.api.gameval.VarPlayerID.SA_ENERGY),targets.get(target),settled));
		}
		if(!capture.active()) { hits.clear(); projectiles.clear(); effects.clear(); resetHud(); }
	}
	private void startIfReady(boolean allowPartial)
	{
		int tick=client.getTickCount();
		boolean smokeStart=smoke==tick;
		if(capture.active())
		{
			if(smokeStart && capture.anchorSmoke(tick,roster())) { hits.clear(); projectiles.clear(); effects.clear(); resetHud(); }
			return;
		}
		if(attempted || boss==null || (barrier!=3 && !(smokeStart && barrier==2))
			|| boss.getId()==NpcID.NEX_DYING || !sameView(client.getLocalPlayer())) { return; }
		if(!smokeStart && !allowPartial && boundary!=tick) { return; }
		hits.clear(); projectiles.clear(); effects.clear(); resetHud(); attempted=true;
		capture.start(tick,smokeStart,method,roster(),defaults);
		actionContext.stream().filter(a->a.tick>=tick-NexActions.PRE_TICKS).forEach(capture::action);
		if(actionContextTruncated) { capture.actionTruncated(); }
		actionContext.clear(); actionContextTruncated=false;
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
	/** Freeze public scalars on the client thread, including bounded arena pre-start context. */
	private void action(NexActions.Kind kind,NexActions.Source source)
	{
		if(!enabled || !inArena()) { return; }
		Player local=client.getLocalPlayer(); if(!sameView(local)) { return; }
		NPC npc=local.getInteracting() instanceof NPC?(NPC)local.getInteracting():null;
		String target=sameView(npc)?targets.get(npc):null;
		int tick=client.getTickCount(), animation=local.getAnimation();
		var sample=capture.actionSample(tick,reader.read(),target,animation,target==null?NexTargetState.UNKNOWN:targetState(npc));
		var signal=new NexActions.Signal(kind,source,tick,client.getGameCycle(),++callbackOrder,animation,local.getAnimationFrame(),movingPose(local),sample);
		if(capture.active()) { capture.action(signal); }
		else if(!attempted)
		{
			while(!actionContext.isEmpty() && actionContext.peekFirst().tick<tick-NexActions.PRE_TICKS) { actionContext.removeFirst(); }
			if(actionContext.size()>=NexActions.PRE_MAX) { actionContext.removeFirst(); actionContextTruncated=true; }
			actionContext.addLast(signal);
		}
	}
	private static boolean movingPose(Player local)
	{
		int pose=local.getPoseAnimation();
		return pose>=0 && pose!=local.getIdlePoseAnimation() && (pose==local.getWalkAnimation() || pose==local.getRunAnimation()
			|| pose==local.getWalkRotateLeft() || pose==local.getWalkRotateRight() || pose==local.getWalkRotate180());
	}
	synchronized void animation() { action(NexActions.Kind.ANIMATION,NexActions.Source.LOCAL_ANIMATION); sampleAnimation(); }
	private void sampleAnimation() { Player local=client.getLocalPlayer(); if(local!=null) { sample(local.getAnimation()); } }
	synchronized com.ironpath.encounter.CombatState sample(int animation)
	{
		if (!enabled || !capture.active()) { return null; }
		Player local=client.getLocalPlayer();
		NPC npc=local!=null && local.getInteracting() instanceof NPC?(NPC)local.getInteracting():null;
		String target=npc!=null && sameView(npc)?targets.get(npc):null;
		if(target==null) { resetHud(); }
		NexTargetState targetState=target==null?NexTargetState.UNKNOWN:targetState(npc);
		var state=reader.read(); int weapon=state.slots.getOrDefault("weapon",-1);
		String role=state.specialSelected?"spec candidate":ranged.contains(weapon)?"ranged":melee.contains(weapon)?"melee":"unclassified";
		capture.sample(client.getTickCount(),state,target,role,animation,roster(),targetState);
		capture.health(new NexObservations.Health(client.getTickCount(),client.getGameCycle(),++callbackOrder,target,targetState));
		if(animation>=0)
		{
			NexObservations.Weapon pilot=weapon==26219 || weapon==27246?NexObservations.Weapon.FANG:weapon==20997?NexObservations.Weapon.TWISTED_BOW
				:weapon==27690?NexObservations.Weapon.VOIDWAKER:weapon==26374?NexObservations.Weapon.ZARYTE_CROSSBOW:null;
			if(pilot!=null)
			{
				// Generated animation names are mapping candidates, not live acceptance of these weapon combinations.
				boolean ordinary=pilot==NexObservations.Weapon.FANG && animation==9471 || pilot==NexObservations.Weapon.TWISTED_BOW && animation==426
					|| pilot==NexObservations.Weapon.VOIDWAKER && (animation==386 || animation==390);
				boolean ambiguousCrossbow=pilot==NexObservations.Weapon.ZARYTE_CROSSBOW && (animation==7552 || animation==9168);
				boolean special=pilot==NexObservations.Weapon.VOIDWAKER && (animation==11240 || animation==11275);
				capture.launch(client.getTickCount(),client.getGameCycle(),++callbackOrder,animation,state,target,targetState,pilot,
					special?NexObservations.Attack.SPECIAL_CANDIDATE:ordinary?NexObservations.Attack.ORDINARY_CANDIDATE
						:ambiguousCrossbow?NexObservations.Attack.ORDINARY_OR_SPECIAL_CANDIDATE:NexObservations.Attack.UNCLASSIFIED);
			}
		}
		return state;
	}
	private NexTargetState targetState(NPC npc)
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
		String target=targets.get(npc);
		// Normal permanent-world maxima, independently reviewed; no party scaling or inferred maximum.
		NexHealth hp=NexHealth.bar(npc.getHealthRatio(),npc.getHealthScale(),targetMaxima.getOrDefault(bossId(npc.getId())?NpcID.NEX:npc.getId(),0),
			client.getTickCount(),client.getGameCycle(),target,npc.getId(),view);
		if(hudTarget!=npc || hudDefinition!=npc.getId()) { invalidateHud(); }
		if(hudTarget==npc && hudTick==client.getTickCount() && client.getGameCycle()>=hudCycle && hudVisible()
			&& hudHp==client.getServerVarbitValue(VarbitID.HPBAR_HUD_HP) && hudMaximum==client.getServerVarbitValue(VarbitID.HPBAR_HUD_BASEHP)
			&& hudDefinition==client.getServerVarpValue(net.runelite.api.gameval.VarPlayerID.HPBAR_HUD_NPC)
			&& hudHp==client.getVarbitValue(VarbitID.HPBAR_HUD_HP) && hudMaximum==client.getVarbitValue(VarbitID.HPBAR_HUD_BASEHP)
			&& hudDefinition==client.getVarpValue(net.runelite.api.gameval.VarPlayerID.HPBAR_HUD_NPC))
		{ hp=NexHealth.hud(hudHp,hudMaximum,hudTick,hudCycle,target,hudDefinition,view); }
		return new NexTargetState(form,npc.getHealthRatio(),npc.getHealthScale(),hp);
	}
	/** A definition ID never supplies a spawn identity. Bind only at the actual HUD script callback. */
	private boolean hudVisible()
	{
		var widget=client.getWidget(net.runelite.api.gameval.InterfaceID.HpbarHud.HP_BAR_TEXT);
		return client.getVarbitValue(VarbitID.HPBAR_HUD_BOSS_DISABLED)==0 && widget!=null && !widget.isHidden();
	}
	synchronized void hud()
	{
		invalidateHud();
		if(!enabled || !capture.active()) { return; }
		capture.hudCheck(NexObservations.HudCheck.SCRIPT_CALLBACK);
		if(hudUpdateTick!=client.getTickCount()) { capture.hudCheck(NexObservations.HudCheck.NO_FRESH_VARIABLE); return; }
		if(client.getVarbitValue(VarbitID.HPBAR_HUD_BOSS_DISABLED)!=0) { capture.hudCheck(NexObservations.HudCheck.DISABLED); return; }
		var widget=client.getWidget(net.runelite.api.gameval.InterfaceID.HpbarHud.HP_BAR_TEXT);
		if(widget==null) { capture.hudCheck(NexObservations.HudCheck.WIDGET_MISSING); return; }
		if(widget.isHidden()) { capture.hudCheck(NexObservations.HudCheck.WIDGET_HIDDEN); return; }
		Player local=client.getLocalPlayer();
		if(local==null || !(local.getInteracting() instanceof NPC)) { capture.hudCheck(NexObservations.HudCheck.NO_INTERACTION); return; }
		NPC npc=(NPC)local.getInteracting();
		int definition=client.getServerVarpValue(net.runelite.api.gameval.VarPlayerID.HPBAR_HUD_NPC);
		int hp=client.getServerVarbitValue(VarbitID.HPBAR_HUD_HP),maximum=client.getServerVarbitValue(VarbitID.HPBAR_HUD_BASEHP);
		if(!sameView(npc) || !targets.containsKey(npc)) { capture.hudCheck(NexObservations.HudCheck.UNTRACKED_TARGET); return; }
		if(npc.getId()!=definition) { capture.hudCheck(NexObservations.HudCheck.DEFINITION_MISMATCH); return; }
		if(hp<0 || maximum<1 || maximum>100000 || hp>maximum) { capture.hudCheck(NexObservations.HudCheck.INVALID_VALUES); return; }
		if(definition!=client.getVarpValue(net.runelite.api.gameval.VarPlayerID.HPBAR_HUD_NPC)
			|| hp!=client.getVarbitValue(VarbitID.HPBAR_HUD_HP) || maximum!=client.getVarbitValue(VarbitID.HPBAR_HUD_BASEHP))
		{ capture.hudCheck(NexObservations.HudCheck.SERVER_DISAGREEMENT); return; }
		if(targets.keySet().stream().filter(n->sameView(n) && n.getId()==definition).count()!=1) { capture.hudCheck(NexObservations.HudCheck.AMBIGUOUS_TARGET); return; }
		hudTarget=npc; hudDefinition=definition; hudHp=hp; hudMaximum=maximum; hudTick=client.getTickCount(); hudCycle=client.getGameCycle();
		capture.hudCheck(NexObservations.HudCheck.ACCEPTED_CALLBACK);
		capture.health(new NexObservations.Health(hudTick,hudCycle,++callbackOrder,targets.get(npc),targetState(npc)));
	}
	private void resource(NexObservations.Kind kind,int value,int server,String target)
	{ capture.resource(new NexObservations.Signal(kind,client.getTickCount(),client.getGameCycle(),++callbackOrder,value,server,target)); }
	synchronized void resourceEvent(int varbit,int varp,int value)
	{
		if(!enabled || !capture.active()) { return; }
		if(varbit==VarbitID.HPBAR_HUD_HP || varbit==VarbitID.HPBAR_HUD_BASEHP
			|| varp==net.runelite.api.gameval.VarPlayerID.HPBAR_HUD_INFO || varp==net.runelite.api.gameval.VarPlayerID.HPBAR_HUD_NPC)
		{ hudUpdateTick=client.getTickCount(); capture.hudCheck(NexObservations.HudCheck.VARIABLE_UPDATE); }
		if(varp==net.runelite.api.gameval.VarPlayerID.SA_ENERGY)
		{ resource(NexObservations.Kind.ENERGY,value,client.getServerVarpValue(varp),null); }
		else if(varbit==VarbitID.ARCEUUS_DEATH_CHARGE_ACTIVE) { resource(NexObservations.Kind.DEATH_CHARGE_ACTIVE,value,client.getServerVarbitValue(varbit),null); }
		else if(varbit==VarbitID.ARCEUUS_DEATH_CHARGE_COOLDOWN) { resource(NexObservations.Kind.DEATH_CHARGE_COOLDOWN,value,client.getServerVarbitValue(varbit),null); }
		else if(varbit==VarbitID.DEATH_CHARGE_SCROLL_USED) { resource(NexObservations.Kind.DEATH_CHARGE_UPGRADE,value,client.getServerVarbitValue(varbit),null); }
	}
	synchronized void spot(Actor actor)
	{
		if(!enabled || !capture.active() || actor!=client.getLocalPlayer()) { return; }
		Set<ActorSpotAnim> present=Collections.newSetFromMap(new IdentityHashMap<>());
		if(actor.getSpotAnims()!=null) for(var animation:actor.getSpotAnims())
		{
			if(present.size()>=32) { break; }
			present.add(animation); int id=animation.getId();
			if(Objects.equals(effects.put(animation,id),id)) { continue; }
			if(id==1854 || id==3288) { resource(NexObservations.Kind.DC_CAST_EFFECT,id,-1,null); }
			if(id==1855 || id==3289) { resource(NexObservations.Kind.DC_END_EFFECT,id,-1,null); }
		}
		effects.keySet().retainAll(present);
	}
	private void check(NexCallbackDiagnostics.Target target,NexCallbackDiagnostics.Stage stage,int amount)
	{ capture.callback(target,stage,amount); }
	synchronized void projectile(Projectile projectile)
	{
		if(!enabled || !capture.active()) { return; }
		Actor source=projectile==null?null:projectile.getSourceActor(),target=projectile==null?null:projectile.getTargetActor();
		String identity=target instanceof NPC && sameView(target)?targets.get(target):null;
		var scope=target instanceof NPC?NexCallbackDiagnostics.target(targetName(((NPC)target).getId())):NexCallbackDiagnostics.Target.UNKNOWN;
		check(scope,PROJECTILE_ENTRY,0);
		if(projectile==null) { check(scope,PROJECTILE_NULL,0); return; }
		if(projectiles.contains(projectile))
		{
			check(scope,PROJECTILE_REPEAT,0);
			// Later getter availability is diagnostic only; never revise the retained first flight.
			check(scope,source==null?PROJECTILE_REPEAT_UNKNOWN_SOURCE
				:source==client.getLocalPlayer()?PROJECTILE_REPEAT_LOCAL_SOURCE:PROJECTILE_REPEAT_OTHER_SOURCE,0);
			return;
		}
		// Public actors are nullable, with no promised later resolution time. Preserve first-callback scope.
		if(source!=null && source!=client.getLocalPlayer()) { check(scope,PROJECTILE_OTHER_SOURCE,0); return; }
		if(target!=null && identity==null) { check(scope,PROJECTILE_OUTSIDE_TARGET,0); return; }
		if(projectiles.size()>=NexObservations.MAX) { check(scope,PROJECTILE_OBJECT_LIMIT,0); return; }
		projectiles.add(projectile);
		var evidence=source==null?NexObservations.Source.UNKNOWN:NexObservations.Source.LOCAL;
		check(scope,source==null?PROJECTILE_UNKNOWN:PROJECTILE_LOCAL,0);
		capture.flight(new NexObservations.Flight(client.getTickCount(),client.getGameCycle(),++callbackOrder,projectile.getId(),projectile.getStartCycle(),projectile.getEndCycle(),identity,evidence,
			identity==null?NexTargetState.UNKNOWN:targetState((NPC)target)));
	}
	synchronized void hit(HitsplatApplied event)
	{
		if (!enabled || !capture.active()) { return; }
		Actor actor=event==null?null:event.getActor(); Hitsplat hit=event==null?null:event.getHitsplat();
		NPC target=actor instanceof NPC?(NPC)actor:null;
		String name=target==null?null:targetName(target.getId());
		var scope=target==null?NexCallbackDiagnostics.Target.UNKNOWN:name==null?NexCallbackDiagnostics.Target.OUTSIDE:NexCallbackDiagnostics.target(name);
		// Freeze scalars on entry; never retain a mutable payload in the worker evidence.
		int amount=hit==null?0:hit.getAmount(),type=hit==null?-1:hit.getHitsplatType();
		boolean mine=hit!=null && hit.isMine(),other=hit!=null && hit.isOthers();
		check(scope,HIT_ENTRY,amount);
		if(target==null) { check(scope,HIT_NOT_NPC,amount); return; }
		if(hit==null) { check(scope,HIT_MISSING_PAYLOAD,0); return; }
		if(name==null) { check(scope,HIT_UNKNOWN_TARGET,amount); return; }
		if(!sameView(target)) { check(scope,HIT_WRONG_VIEW,amount); return; }
		if(!targets.containsKey(target)) { check(scope,HIT_UNTRACKED,amount); return; }
		if(hits.contains(hit)) { check(scope,HIT_REPEATED_OBJECT,amount); }
		else if(hits.size()<NexObservations.MAX) { hits.add(hit); }
		else { check(scope,HIT_OBJECT_TRACKING_LIMIT,amount); }
		// HitsplatApplied describes an application, including non-rendered hits. No identity lifetime is promised.
		// Count each delivery. Amount/tick and object identity cannot distinguish duplicate dispatch from valid hits.
		boolean accepted=capture.hit(client.getTickCount(),++hitSerial,targets.get(target),amount,mine,other,type==HitsplatID.HEAL);
		if(accepted && mine)
		{
			capture.impact(new NexObservations.Impact(client.getTickCount(),client.getGameCycle(),++callbackOrder,targets.get(target),amount,targetState(target)));
		}
	}
	synchronized void death(Actor actor)
	{
		if(actor instanceof NPC && targets.containsKey(actor)) { resource(NexObservations.Kind.TARGET_DEATH,0,-1,targets.get(actor)); }
		if (actor==client.getLocalPlayer()) { capture.abort(client.getTickCount(),Outcome.FAILED,"Player death"); }
		else if (actor==boss && sameView(actor)) { signal("Nex ActorDeath"); capture.death(client.getTickCount()); }
	}
}
