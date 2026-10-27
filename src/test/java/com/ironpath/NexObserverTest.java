package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.nex.NexSession;
import com.ironpath.nex.NexTargetState;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.runelite.api.*;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarbitID;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** Mocked public API signals, never represented as recorded live-client evidence. */
public class NexObserverTest
{
	Client client; Player player; NPC boss, minion; WorldView world; NexObserver observer; NexSession session;
	AtomicInteger tick=new AtomicInteger(), barrier=new AtomicInteger(2);
	List<NPC> npcs=new ArrayList<>();
	@Before public void setup() throws Exception
	{
		client=mock(Client.class); player=mock(Player.class); world=mock(WorldView.class);
		when(client.isClientThread()).thenReturn(true); when(client.getTickCount()).thenAnswer(i->tick.get());
		when(client.getVarbitValue(VarbitID.NEX_BARRIER)).thenAnswer(i->barrier.get());
		when(client.getLocalPlayer()).thenReturn(player); when(client.getTopLevelWorldView()).thenReturn(world);
		when(player.getWorldView()).thenReturn(world); when(player.getName()).thenReturn("synthetic roster member");
		when(world.getId()).thenReturn(-1);
		IndexedObjectSet players=mock(IndexedObjectSet.class), actors=mock(IndexedObjectSet.class);
		when(world.players()).thenReturn(players); when(players.iterator()).thenAnswer(i->List.of(player).iterator());
		when(world.npcs()).thenReturn(actors); when(actors.stream()).thenAnswer(i->npcs.stream());
		when(client.getRealSkillLevel(any(Skill.class))).thenReturn(99); when(client.getBoostedSkillLevel(any(Skill.class))).thenReturn(112);
		ItemContainer equipment=mock(ItemContainer.class); when(client.getItemContainer(InventoryID.WORN)).thenReturn(equipment);
		when(equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx())).thenReturn(new Item(26219,1));
		boss=npc(NpcID.NEX,1); minion=npc(NpcID.NEX_SHADOWMAGE,2); npcs.add(boss); npcs.add(minion);
		when(player.getInteracting()).thenReturn(boss);
		session=new NexSession(); session.select("synthetic",301);
		observer=new NexObserver(client,KnowledgeCatalog.load(new Gson()),session);
	}
	NPC npc(int id,int index)
	{ NPC actor=mock(NPC.class); when(actor.getId()).thenReturn(id); when(actor.getIndex()).thenReturn(index); when(actor.getWorldView()).thenReturn(world); return actor; }
	@Test public void bankingSourceSurvivesExitBarrierOnlyUntilTheSceneTransition()
	{
		barrier.set(1); observer.tick(); assertFalse(observer.bankTravelSource());
		barrier.set(2); observer.barrier(); assertTrue(observer.bankTravelSource());
		barrier.set(1); observer.barrier(); assertFalse(observer.inArena()); assertTrue(observer.bankTravelSource());
		observer.transition(); assertFalse(observer.bankTravelSource());
		observer.tick(); assertFalse("A nearby Nex outside the arena is not a new source",observer.bankTravelSource());
		barrier.set(2); observer.barrier(); assertTrue(observer.bankTravelSource());
		observer.clearActive(); assertFalse(observer.bankTravelSource());
	}
	void start()
	{
		observer.tick(); observer.overhead(boss,"Fill my soul with smoke!");
		tick.set(1); barrier.set(3); observer.tick(); assertTrue(observer.capture.active());
	}
	@Test public void publicTargetFormsAndHealthBarsFreezeWithoutInventingExactHpOrCarryingAFormerTarget() throws Exception
	{
		when(boss.getHealthRatio()).thenReturn(9); when(boss.getHealthScale()).thenReturn(30);
		start();
		tick.set(2); observer.overhead(boss,"NOW, THE POWER OF ZAROS!");
		when(boss.getId()).thenReturn(NpcID.NEX_SOULSPLIT); observer.changed(boss); observer.sample(-1);
		tick.set(3); when(boss.getId()).thenReturn(NpcID.NEX_DEFLECT); observer.changed(boss); observer.sample(-1);
		tick.set(4); when(boss.getHealthRatio()).thenReturn(-1); observer.sample(-1);
		tick.set(5); when(player.getInteracting()).thenReturn(null); observer.sample(-1);
		observer.transition(); var summary=session.recent().get(0);
		var soul=summary.timeline.stream().filter(s->s.targetState.form==NexTargetState.Form.SOUL_SPLIT).findFirst().orElseThrow();
		var deflect=summary.timeline.stream().filter(s->s.targetState.form==NexTargetState.Form.DEFLECT_MELEE && s.tick==3).findFirst().orElseThrow();
		assertEquals(NexTargetState.Overhead.SOUL_SPLIT,soul.targetState.overhead());
		assertEquals(NexTargetState.Overhead.DEFLECT_MELEE,deflect.targetState.overhead());
		assertEquals(Integer.valueOf(9),soul.targetState.healthRatio); assertEquals(Integer.valueOf(30),soul.targetState.healthScale);
		assertTrue(soul.targetState.reading().contains("exact HP and target levels unknown"));
		assertNull(summary.observations.health.stream().filter(s->s.tick==4).findFirst().orElseThrow().targetState.healthRatio);
		assertSame(NexTargetState.UNKNOWN,summary.timeline.stream().filter(s->s.tick==5).findFirst().orElseThrow().targetState);
		when(boss.getHealthRatio()).thenReturn(1); observer.clearActive();
		assertEquals(Integer.valueOf(9),soul.targetState.healthRatio); assertEquals(NexTargetState.Overhead.SOUL_SPLIT,soul.targetState.overhead());
	}
	void hit(NPC actor,boolean mine,boolean other,int type,int amount)
	{
		Hitsplat hit=mock(Hitsplat.class); when(hit.isMine()).thenReturn(mine); when(hit.isOthers()).thenReturn(other);
		when(hit.getHitsplatType()).thenReturn(type); when(hit.getAmount()).thenReturn(amount);
		HitsplatApplied event=new HitsplatApplied(); event.setActor(actor); event.setHitsplat(hit);
		observer.hit(event);
	}
	@Test public void actionMessagesAreTypedBoundedAndDoNotReplacePhaseParsingOrRetainChat()
	{
		start();
		String normal="Upon the death of your next foe, some of your special attack energy will be restored.";
		String upgraded="Upon the death of your next two foes, some of your special attack energy will be restored.";
		observer.chat(ChatMessageType.SPAM,"<col=ff0000>You eat the shark.</col>");
		observer.chat(ChatMessageType.SPAM,"You eat the karambwan."); observer.chat(ChatMessageType.SPAM,"You eat the karambwan.");
		observer.chat(ChatMessageType.GAMEMESSAGE,"<col=00ff00>"+normal+"</col>"); observer.chat(ChatMessageType.GAMEMESSAGE,upgraded);
		observer.chat(ChatMessageType.PUBLICCHAT,normal); observer.chat(ChatMessageType.NPC_SAY,normal);
		observer.chat(ChatMessageType.CONSOLE,upgraded); observer.chat(ChatMessageType.GAMEMESSAGE,"Player's unrelated private content");
		observer.chat(ChatMessageType.GAMEMESSAGE,"You eat the monkfish.");
		observer.chat(ChatMessageType.GAMEMESSAGE,"Nex: Darken my shadow!");
		observer.transition(); var s=session.recent().get(0);
		assertEquals(2,s.actions.signals.stream().filter(a->a.kind==com.ironpath.nex.NexActions.Kind.FOOD_KARAMBWAN).count());
		assertEquals(1,s.actions.signals.stream().filter(a->a.kind==com.ironpath.nex.NexActions.Kind.DC_NORMAL).count());
		assertEquals(1,s.actions.signals.stream().filter(a->a.kind==com.ironpath.nex.NexActions.Kind.DC_UPGRADED).count());
		assertEquals(1,s.actions.signals.stream().filter(a->a.kind==com.ironpath.nex.NexActions.Kind.FOOD_UNKNOWN).count());
		assertTrue(s.timeline.stream().anyMatch(a->a.phase.equals("SHADOW")));
		String export=new com.ironpath.nex.NexObservationReview(s).export;
		assertTrue(export.contains("FOOD_SHARK")); assertTrue(export.contains("SPAM_MESSAGE")); assertTrue(export.contains(com.ironpath.nex.NexActionTimeline.VERSION));
		assertFalse(export.contains("Upon the death")); assertFalse(export.contains("unrelated private")); assertFalse(export.contains("synthetic roster"));
		assertFalse(export.contains("NEX@")); assertFalse(export.contains("monkfish"));
	}
	@Test public void engineMessagesAdmitOnlyExactDeathChargeAndMantaRayKeepsItsOwnTypedSignal()
	{
		observer.tick(); observer.overhead(boss,"Fill my soul with smoke!");
		String normal="Upon the death of your next foe, some of your special attack energy will be restored.";
		String upgraded="Upon the death of your next two foes, some of your special attack energy will be restored.";
		observer.chat(ChatMessageType.ENGINE,"<col=00ff00>"+normal+"</col>");
		observer.chat(ChatMessageType.ENGINE,upgraded);
		observer.chat(ChatMessageType.ENGINE,"You eat the manta ray.");
		observer.chat(ChatMessageType.ENGINE,"Nex: Darken my shadow!");
		for(var type:List.of(ChatMessageType.PUBLICCHAT,ChatMessageType.PRIVATECHAT,ChatMessageType.CONSOLE,
			ChatMessageType.NPC_SAY,ChatMessageType.DIALOG,ChatMessageType.MESBOX,ChatMessageType.PLAYERRELATED))
		{ observer.chat(type,upgraded); observer.chat(type,"You eat the manta ray."); }
		observer.chat(ChatMessageType.SPAM,"You eat the manta ray.");
		observer.chat(ChatMessageType.GAMEMESSAGE,"You eat the manta ray.");
		observer.transition(); var summary=session.recent().get(0);
		assertEquals(2,summary.actions.signals.stream().filter(a->a.source==com.ironpath.nex.NexActions.Source.ENGINE_MESSAGE).count());
		assertEquals(2,summary.actions.signals.stream().filter(a->a.kind==com.ironpath.nex.NexActions.Kind.FOOD_MANTA_RAY).count());
		assertEquals(1,summary.actions.signals.stream().filter(a->a.kind==com.ironpath.nex.NexActions.Kind.DC_NORMAL).count());
		assertEquals(1,summary.actions.signals.stream().filter(a->a.kind==com.ironpath.nex.NexActions.Kind.DC_UPGRADED).count());
		assertFalse(summary.timeline.stream().anyMatch(s->s.phase.equals("SHADOW")));
		String report=new com.ironpath.nex.NexObservationReview(summary).export;
		assertTrue(report.contains("ENGINE_MESSAGE")); assertTrue(report.contains("FOOD_MANTA_RAY"));
		assertFalse(report.contains("You eat")); assertFalse(report.contains("Upon the death"));
	}
	@Test public void arenaPrestartActionsAndSmokeDoNotResetTheFangClockButLifecycleDoes()
	{
		observer.tick(); when(player.getAnimation()).thenReturn(9471); observer.animation(); observer.tick();
		tick.set(1); observer.tick(); tick.set(2); observer.overhead(boss,"Fill my soul with smoke!"); observer.tick();
		observer.transition(); var s=session.recent().get(0);
		var timeline=new com.ironpath.nex.NexActionTimeline(s);
		var first=timeline.entries.stream().filter(com.ironpath.nex.NexActionTimeline.Entry::attack).findFirst().orElseThrow();
		assertEquals(0,first.signal.tick); assertNull(first.readyBefore); assertEquals(2,s.startTick);
		assertEquals(Integer.valueOf(5),timeline.entries.get(timeline.entries.size()-1).readyAfter);
		tick.set(30); observer.tick(); observer.overhead(boss,"Fill my soul with smoke!"); observer.tick(); observer.transition();
		var next=session.recent().get(1); assertTrue(next.actions.signals.stream().allMatch(a->a.tick>=30));
		assertTrue(new com.ironpath.nex.NexActionTimeline(next).entries.stream().allMatch(a->a.readyBefore==null));
	}

	@Test public void prestartContextOverflowIsExplicitAndResetClearsTheBound()
	{
		observer.tick();
		for(int i=0;i<com.ironpath.nex.NexActions.PRE_MAX+1;i++) { observer.chat(ChatMessageType.SPAM,"You eat the shark."); }
		observer.overhead(boss,"Fill my soul with smoke!"); observer.transition();
		assertTrue(session.recent().get(0).actions.truncated);
		tick.set(20); observer.tick(); observer.overhead(boss,"Fill my soul with smoke!"); observer.transition();
		assertFalse(session.recent().get(1).actions.truncated);
	}

	void phasesAndEnd()
	{
		for(int t=2;t<=36;t++)
		{
			tick.set(t);
			if(t==5) { observer.chat(ChatMessageType.GAMEMESSAGE,"Nex: Darken my shadow!"); }
			if(t==8) { observer.overhead(boss,"Embrace darkness!"); }
			if(t==9) { hit(boss,true,false,HitsplatID.DAMAGE_ME,20); hit(minion,false,true,HitsplatID.DAMAGE_OTHER,60); }
			if(t==10) { observer.overhead(boss,"Fear the shadow!"); }
			if(t==15) { observer.overhead(boss,"Flood my lungs with blood!"); }
			if(t==20) { observer.overhead(boss,"Infuse me with the power of ice!"); }
			if(t==25) { observer.overhead(boss,"NOW, THE POWER OF ZAROS!"); when(boss.getId()).thenReturn(NpcID.NEX_SOULSPLIT); observer.changed(boss); }
			if(t==30) { observer.death(boss); }
			if(t==31) { barrier.set(2); }
			observer.tick();
		}
	}
	@Test public void initiallyActiveArenaWithLaterSmokeAndDyingBeforeBarrierCompletes()
	{
		barrier.set(3);
		for(int t=0;t<=103;t++)
		{
			tick.set(t);
			if(t==21) { observer.overhead(boss,"Fill my soul with smoke!"); }
			if(t==30) { observer.overhead(boss,"Darken my shadow!"); }
			if(t==45) { observer.overhead(boss,"Flood my lungs with blood!"); }
			if(t==60) { observer.overhead(boss,"Infuse me with the power of ice!"); }
			if(t==75) { observer.overhead(boss,"NOW, THE POWER OF ZAROS!"); }
			if(t==90) { when(boss.getId()).thenReturn(NpcID.NEX_DYING); observer.changed(boss); }
			if(t==98) { barrier.set(2); observer.barrier(2); }
			observer.tick();
		}
		assertEquals(1,session.recent().size()); var summary=session.recent().get(0);
		assertEquals(Outcome.COMPLETE,summary.outcome);
		assertEquals(21,summary.startTick); assertEquals(90,summary.endTick);
		assertEquals(69,summary.sampledTicks); assertEquals(69,summary.expectedTicks);
		assertEquals(21,summary.timeline.get(0).tick); assertEquals("SMOKE",summary.timeline.get(0).phase);
		assertEquals(0,summary.beforeStart.get(0).tick); assertEquals("UNKNOWN",summary.beforeStart.get(0).phase);
		assertThrows(UnsupportedOperationException.class,()->summary.beforeStart.clear());
		assertNull(summary.signals.boundaryTick);
		assertEquals(Integer.valueOf(21),summary.signals.smokeTick);
		assertEquals(Integer.valueOf(98),summary.signals.barrierEndTick);
	}
	@Test public void smokeCallbackCapturesInitialStateBeforeFollowingGameTick()
	{
		observer.tick(); observer.overhead(boss,"Fill my soul with smoke!");
		tick.set(1); barrier.set(3); observer.barrier(); assertTrue(observer.capture.active());
		// The next game tick has already advanced. The origin remains the callback tick.
		phasesAndEnd(); assertEquals(Outcome.COMPLETE,session.recent().get(0).outcome);
		assertEquals(0,session.recent().get(0).startTick);
		assertEquals(0,session.recent().get(0).timeline.get(0).tick);
		assertEquals(Integer.valueOf(1),session.recent().get(0).signals.boundaryTick);
	}
	@Test public void observedBoundaryBeforeSmokeDoesNotBecomeAMidKillEnable()
	{
		observer.tick();
		tick.set(1); barrier.set(3); observer.tick();
		assertTrue(observer.capture.active());
		tick.set(2); observer.overhead(boss,"Fill my soul with smoke!");
		phasesAndEnd();
		var summary=session.recent().get(0);
		assertEquals(Outcome.COMPLETE,summary.outcome);
		assertEquals(2,summary.startTick);
		assertEquals("UNKNOWN",summary.beforeStart.get(0).phase);
		assertEquals("SMOKE",summary.timeline.get(0).phase);
		assertTrue(summary.timeline.stream().anyMatch(s->s.tick==2 && s.phase.equals("SMOKE")));
		assertEquals(Integer.valueOf(1),summary.signals.boundaryTick);
		assertEquals(Integer.valueOf(2),summary.signals.smokeTick);
	}
	@Test public void boundaryCallbackSamplesBeforeSmokeInTheSameTick()
	{
		observer.tick(); tick.set(1); barrier.set(3); observer.barrier();
		assertTrue(observer.capture.active()); observer.overhead(boss,"Fill my soul with smoke!");
		phasesAndEnd(); var summary=session.recent().get(0);
		assertEquals(Outcome.COMPLETE,summary.outcome); assertEquals(1,summary.startTick);
		assertEquals("UNKNOWN",summary.beforeStart.get(0).phase);
		assertEquals("SMOKE",summary.timeline.get(0).phase);
		assertEquals(Integer.valueOf(1),summary.signals.smokeTick);
	}
	@Test public void smokeAnchorsAnInitiallyActiveArenaWithoutInventingAnArenaTransition()
	{
		barrier.set(3); observer.tick(); tick.set(1); observer.overhead(boss,"Fill my soul with smoke!");
		phasesAndEnd(); var summary=session.recent().get(0);
		assertEquals(Outcome.COMPLETE,summary.outcome);
		assertNull(summary.signals.boundaryTick); assertEquals(Integer.valueOf(1),summary.signals.smokeTick);
		assertEquals(Integer.valueOf(1),summary.signals.observedSmokeTick);
		assertTrue(summary.signals.trace.stream().anyMatch(s->s.contains("barrier initial→3 (poll)")));
		assertTrue(summary.signals.trace.stream().anyMatch(s->s.contains("smoke line observed")));
		assertEquals(1,summary.startTick); assertEquals("UNKNOWN",summary.beforeStart.get(0).phase);
		assertTrue(summary.signals.phases.containsAll(List.of("SMOKE","SHADOW","BLOOD","ICE","ZAROS")));
	}
	@Test public void eventPayloadEndIsRetainedEvenWhenNoPollSeesTheEndValue()
	{
		start(); tick.set(5); observer.death(boss);
		tick.set(6); observer.barrier(2); // The independent client getter still reads 3.
		observer.capture.flush(6);
		var summary=session.recent().get(0);
		assertEquals(Integer.valueOf(6),summary.signals.barrierEndTick);
		assertTrue(summary.signals.trace.stream().anyMatch(s->s.contains("barrier 3→2 (event)")));
		assertTrue(summary.signals.trace.stream().anyMatch(s->s.contains("Nex ActorDeath")));
		assertEquals(Outcome.CENSORED,summary.outcome); // Missing phases are still required.
		observer.clearActive(); tick.set(100); observer.tick(); observer.capture.flush(100);
		observer.transition();
		var next=session.recent().get(1);
		assertTrue(next.signals.trace.stream().noneMatch(s->s.contains("tick 6 ") || s.contains("tick 5 ")));
		assertTrue(summary.signals.trace.stream().anyMatch(s->s.contains("tick 6 ")));
	}

	@Test public void reviewedTargetsTransformsAndDuplicateCallbacksProduceOneCompleteSummary()
	{
		start(); hit(npc(NpcID.NEX_PRISON_BLOOD_REAVER_BOSS,4),true,false,HitsplatID.DAMAGE_ME,500);
		phasesAndEnd(); assertEquals(1,session.recent().size()); var s=session.recent().get(0);
		assertEquals(Outcome.COMPLETE,s.outcome); assertEquals(18,s.seconds(),1e-10);
		assertEquals(20,s.localDamage()); assertEquals(80,s.totalDamage());
		assertTrue(s.damage.keySet().stream().anyMatch(k->k.contains("SHADOW / EMBRACE_DARKNESS / UMBRA@")));
		assertTrue(s.timeline.stream().anyMatch(x->x.window.equals("FEAR_THE_SHADOW")));
		observer.tick(); assertEquals(1,session.recent().size());
	}
	@Test public void spawningAroundOneCompletedKillDoesNotCreateExtraAttempts()
	{
		// An initial active-arena reading can be present while Nex is still spawning.
		barrier.set(3); when(boss.getId()).thenReturn(NpcID.NEX_SPAWNING);
		observer.tick(); tick.set(1); observer.transition();
		// The one actual encounter has all the independently required completion signals.
		tick.set(2); when(boss.getId()).thenReturn(NpcID.NEX); observer.tick();
		observer.overhead(boss,"Fill my soul with smoke!"); phasesAndEnd();
		// A fresh spawning actor and a new 2 -> 3 barrier reading are not another fight.
		NPC respawn=npc(NpcID.NEX_SPAWNING,3); npcs.clear(); npcs.add(respawn);
		tick.set(37); barrier.set(3); observer.tick();
		tick.set(38); observer.transition();
		assertEquals("One real kill, without excluded waiting/respawn captures",1,session.recent().size());
		assertEquals(Outcome.COMPLETE,session.recent().get(0).outcome);
		assertEquals(1,session.stats().complete); assertEquals(0,session.stats().censored);
		assertSame(session.stats().last,session.stats().lastAttempt);
	}
	@Test public void smokeOnSpawningFormStartsImmediatelyAndKeepsPrestartActionContext()
	{
		barrier.set(3); when(boss.getId()).thenReturn(NpcID.NEX_SPAWNING);
		observer.tick(); observer.chat(ChatMessageType.SPAM,"You eat the shark.");
		assertFalse("Spawning plus an arena reading is not combat start",observer.capture.active());
		tick.set(1); observer.overhead(boss,"Fill my soul with smoke!");
		assertTrue("Smoke is authoritative even before the NPC getter transforms",observer.capture.active());
		when(boss.getId()).thenReturn(NpcID.NEX); phasesAndEnd();
		assertEquals(1,session.recent().size()); var summary=session.recent().get(0);
		assertEquals(Outcome.COMPLETE,summary.outcome); assertEquals(1,summary.startTick);
		assertEquals(1,summary.actions.signals.stream().filter(a->a.kind==com.ironpath.nex.NexActions.Kind.FOOD_SHARK && a.tick==0).count());
	}
	@Test public void midKillEnableAndDeathDoNotCreateFalseCompletionsOrRepeatedAttempts()
	{
		barrier.set(3); observer.tick(); assertTrue(observer.capture.active()); phasesAndEnd();
		assertEquals(Outcome.CENSORED,session.recent().get(0).outcome);
		observer.clearActive(); barrier.set(3); observer.tick(); observer.death(player);
		for(int t=37;t<42;t++) { tick.set(t); observer.tick(); }
		assertEquals(2,session.recent().size()); assertEquals(Outcome.FAILED,session.recent().get(1).outcome);
	}
	@Test public void wrongActorTextSpawnReplacementAndTeleportAreCensored()
	{
		start(); observer.overhead(minion,"NOW, THE POWER OF ZAROS!");
		NPC replacement=npc(NpcID.NEX,1); tick.set(2); observer.spawn(replacement);
		assertEquals(Outcome.CENSORED,session.recent().get(0).outcome);
		observer.tick(); observer.transition(); assertFalse(observer.capture.active());
		assertTrue(session.recent().stream().allMatch(s->s.outcome!=Outcome.COMPLETE));
	}
	@Test public void missingDeathBarrierOrTickAndStaleDeathAfterResetCannotComplete()
	{
		start(); tick.set(5); observer.death(boss); observer.tick();
		tick.set(26); observer.tick(); assertEquals(Outcome.CENSORED,session.recent().get(0).outcome);
		observer.clearActive(); observer.death(boss); observer.barrier(); assertFalse(observer.capture.active());
		assertEquals(1,session.recent().size());
	}
	@Test public void aLateSmokeLineAfterObservedDamageCannotEraseAMidKillArrival()
	{
		barrier.set(3); observer.tick(); hit(boss,true,false,HitsplatID.DAMAGE_ME,7);
		tick.set(1); observer.overhead(boss,"Fill my soul with smoke!");
		phasesAndEnd(); var summary=session.recent().get(0);
		assertEquals(Outcome.CENSORED,summary.outcome); assertNull(summary.signals.smokeTick);
		assertEquals(Integer.valueOf(1),summary.signals.observedSmokeTick);
		assertEquals(0,summary.startTick); assertTrue(summary.beforeStart.isEmpty());
		assertEquals(27,summary.localDamage());
	}
	@Test public void aLateSmokeLineAfterALaterPhaseCannotRebaseTheCapture()
	{
		barrier.set(3); observer.tick(); observer.overhead(boss,"Darken my shadow!");
		tick.set(1); observer.overhead(boss,"Fill my soul with smoke!");
		phasesAndEnd(); var summary=session.recent().get(0);
		assertEquals(Outcome.CENSORED,summary.outcome); assertNull(summary.signals.smokeTick);
		assertTrue(summary.beforeStart.isEmpty()); assertEquals(0,summary.startTick);
	}
	@Test public void wrongActorSmokeAndDuplicateSmokeDoNotManufactureOrMoveTheStart()
	{
		barrier.set(3); observer.tick(); observer.overhead(minion,"Fill my soul with smoke!");
		tick.set(1); observer.overhead(boss,"Fill my soul with smoke!");
		tick.set(2); observer.overhead(boss,"Fill my soul with smoke!");
		phasesAndEnd(); var summary=session.recent().get(0);
		assertEquals(Outcome.COMPLETE,summary.outcome); assertEquals(1,summary.startTick);
		assertEquals(Integer.valueOf(1),summary.signals.smokeTick);
	}
	@Test public void smokeBeforeBarrierThenLocalDeathDoesNotRestartTheSameAttempt()
	{
		start(); tick.set(2); observer.death(player);
		for(int t=2;t<10;t++) { tick.set(t); observer.tick(); }
		assertFalse(observer.capture.active()); assertEquals(1,session.recent().size());
		assertEquals(Outcome.FAILED,session.recent().get(0).outcome);
	}
}
