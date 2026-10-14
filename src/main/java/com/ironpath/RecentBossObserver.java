package com.ironpath;

import com.ironpath.advisor.PoweredMagic;
import com.ironpath.encounter.CombatState;
import com.ironpath.encounter.EncounterCapture;
import com.ironpath.encounter.EncounterContext;
import com.ironpath.encounter.EncounterSummary;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.events.ServerNpcLoot;

/** Client-thread Zilyana pilot. No actor crosses into workers, Swing, or a frozen summary. */
final class RecentBossObserver
{
	static final String VERSION="zilyana-public-1";
	static final int NPC_ID=2205;
	final EncounterCapture capture=new EncounterCapture();
	private final Client client;
	private NPC npc;
	private boolean engaged;
	private long serial, generation;
	private int worldView;
	private boolean enabled;
	RecentBossObserver(Client client) { this.client=client; }
	synchronized boolean enabled() { return enabled; }
	synchronized void enable(boolean value, long token) { enabled=value; reset(token); }
	synchronized void reset(long token) { generation=token; npc=null; engaged=false; capture.reset(token); }
	synchronized void spawn(NPC actor)
	{
		if (!enabled || actor.getId()!=NPC_ID) { return; }
		if (capture.active()) { capture.abort(client.getTickCount(),EncounterSummary.Outcome.CENSORED,"Boss spawn/index replaced before completion"); }
		npc=actor; engaged=false; ++serial;
		worldView=actor.getWorldView()==null ? Integer.MIN_VALUE : actor.getWorldView().getId();
	}
	synchronized void changed(NPC actor)
	{
		if (actor==npc && actor.getId()!=NPC_ID)
		{ capture.abort(client.getTickCount(),EncounterSummary.Outcome.EXCLUDED,"Unreviewed boss transformation"); npc=null; }
	}
	synchronized void despawn(NPC actor)
	{
		// A death can despawn before loot arrives; keep only the scalar trace through finalization.
		if (actor==npc) { npc=null; }
	}
	synchronized void tick()
	{
		if (!enabled) { return; }
		int tick=client.getTickCount();
		Player player=client.getLocalPlayer();
		if (npc!=null && (!sameView(npc) || player==null || player.getWorldView()==null || player.getWorldView().getId()!=worldView))
		{ capture.abort(tick,EncounterSummary.Outcome.CENSORED,"World-view identity changed"); npc=null; }
		if (!engaged && npc!=null && player!=null && (player.getInteracting()==npc || npc.getInteracting()==player))
		{
			engaged=true;
			// Visible players do not establish party membership or their contribution.
			capture.start(serial,generation,new EncounterContext(null,null,"zilyana","ordinary","normal",null,
				null,Map.of()),VERSION,tick,state());
			if (npc.getHealthScale()>0 && npc.getHealthRatio()>=0 && npc.getHealthRatio()<npc.getHealthScale())
			{ capture.gap("Engagement began with an already-damaged target; observed duration is partial"); }
		}
		if (capture.active())
		{
			if (player==null || player.isDead()) { capture.abort(tick,EncounterSummary.Outcome.FAILED,"Player death or unavailable player"); }
			else
			{
				if (npc!=null && npc.getWorldView()!=null && npc.getWorldView().players()!=null && npc.getWorldView().players().stream().count()>1)
				{ capture.gap("Other players observed; team contribution and shared reductions unknown"); }
				capture.sample(tick,state());
				// Missing despawn/death is censored, never converted to completion.
				if (npc==null && capture.active()) { capture.gap("Target despawned; awaiting corroborated completion"); }
			}
		}
	}
	private boolean sameView(NPC actor)
	{ return actor.getWorldView()!=null && actor.getWorldView().getId()==worldView && actor.getId()==NPC_ID; }
	synchronized void sample() { if (enabled && capture.active()) { capture.sample(client.getTickCount(),state()); } }
	synchronized void inventoryChanged() { if(capture.active()) { capture.gap("Inventory changed; consumption/drop and attack delay are not inferred"); } }
	synchronized void death(Actor actor)
	{
		if (actor==client.getLocalPlayer()) { capture.abort(client.getTickCount(),EncounterSummary.Outcome.FAILED,"Player died"); }
		else if (npc!=null && actor==npc && sameView(npc)) { capture.death(client.getTickCount()); }
	}
	synchronized void hit(HitsplatApplied event)
	{
		if (npc!=null && event.getActor()==npc && sameView(npc) && capture.active() && event.getHitsplat().isMine())
		{ capture.localDamage(client.getTickCount(),event.getHitsplat().getAmount()); }
	}
	synchronized void loot(ServerNpcLoot event)
	{
		if (enabled && capture.active() && event.getComposition()!=null && event.getComposition().getId()==NPC_ID)
		{ capture.loot(client.getTickCount()); }
	}
	private CombatState state()
	{
		if(!client.isClientThread()) { throw new IllegalStateException("Client thread required"); }
		Map<String,Integer> slots=new HashMap<>();
		var worn=AccountSnapshotReader.slots(client.getItemContainer(InventoryID.WORN),Instant.now());
		for(String slot : PoweredMagic.SLOTS) { slots.put(slot,worn.known() ? worn.value().getOrDefault(slot,-1) : -1); }
		Map<String,Integer> real=new HashMap<>(), visible=new HashMap<>();
		for (Skill skill : List.of(Skill.ATTACK,Skill.STRENGTH,Skill.DEFENCE,Skill.RANGED,Skill.MAGIC,Skill.PRAYER,Skill.HITPOINTS))
		{ int r=client.getRealSkillLevel(skill), v=client.getBoostedSkillLevel(skill); if(r>0) { real.put(skill.name(),r); visible.put(skill.name(),v); } }
		Set<String> prayers=new HashSet<>();
		for (Prayer prayer : Prayer.values()) { if(client.isPrayerActive(prayer)) { prayers.add(prayer.name()); } }
		return new CombatState(slots,real,visible,prayers,client.getVarpValue(VarPlayerID.COM_MODE),
			client.getVarpValue(VarPlayerID.SA_ENERGY),client.getVarpValue(VarPlayerID.SA_ATTACK)==1);
	}
}
