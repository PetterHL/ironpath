package com.ironpath.nex;

import com.ironpath.encounter.CombatState;
import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.preferences.NexMethod;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.ironpath.nex.NexActions.Kind.*;
import static com.ironpath.nex.NexActionTimeline.Reason.*;

/** Independent tick scripts, never live mapping acceptance. */
public class NexActionTimelineTest
{
	List<NexSummary> out=new ArrayList<>(); NexCapture capture=new NexCapture(out::add); long order;
	CombatState fang=state(26219,0,false);
	static CombatState state(int weapon,Integer style,boolean special)
	{ return new CombatState(Map.of("weapon",weapon),Map.of("ATTACK",99),Map.of("ATTACK",99),Set.of(),style,1000,special); }
	void start(int tick) { capture.start(tick,true,NexMethod.PILOT,"synthetic"); }
	void signal(int tick,NexActions.Kind kind,int animation,int frame,CombatState state,String target,boolean moving)
	{
		var source=kind==SETTLED?NexActions.Source.GAME_TICK:kind==ANIMATION?NexActions.Source.LOCAL_ANIMATION:NexActions.Source.GAME_MESSAGE;
		capture.action(new NexActions.Signal(kind,source,tick,tick*30,++order,animation,frame,moving,
			capture.actionSample(tick,state,target,animation,NexTargetState.UNKNOWN)));
	}
	void signal(int tick,NexActions.Kind kind) { signal(tick,kind,-1,0,fang,"NEX@test",false); }
	void attack(int tick) { signal(tick,ANIMATION,9471,0,fang,"NEX@test",false); signal(tick,SETTLED,9471,0,fang,"NEX@test",false); }
	NexSummary finish(int tick) { capture.abort(tick,Outcome.CENSORED,"synthetic"); return out.get(out.size()-1); }
	List<NexActionTimeline.Entry> attacks(NexActionTimeline t)
	{ return t.entries.stream().filter(NexActionTimeline.Entry::attack).collect(java.util.stream.Collectors.toList()); }
	@Test public void unknownInitialClockRepeatsBlocksAndAvailableTicksNeverManufactureAttacks()
	{
		start(0); attack(0);
		signal(1,ANIMATION,9471,1,fang,"NEX@test",false); signal(1,SETTLED); // premature callback
		signal(2,ANIMATION,424,0,fang,"NEX@test",false); // block
		for(int t=2;t<=8;t++) { signal(t,SETTLED,9471,4,fang,"NEX@test",false); } // held animation: no restart
		attack(9); var timeline=new NexActionTimeline(finish(10));
		assertEquals(2,attacks(timeline).size()); assertNull(attacks(timeline).get(0).readyBefore);
		assertEquals(Integer.valueOf(5),attacks(timeline).get(0).readyAfter);
		assertTrue(timeline.entries.stream().anyMatch(e->e.reason==COOLDOWN));
		assertEquals(NexActionTimeline.Readiness.AVAILABLE,attacks(timeline).get(1).readiness);
		assertTrue(timeline.entries.stream().anyMatch(e->e.reason==CONTINUING_ANIMATION));
	}
	@Test public void repeatedAnimationCanSupplyAProvisionalFrameRestartButNeverATimerOnlyAttack()
	{
		start(0); attack(0);
		for(int t=1;t<5;t++) { signal(t,SETTLED,9471,t,fang,"NEX@test",false); }
		signal(5,SETTLED,9471,0,fang,"NEX@test",false);
		var timeline=new NexActionTimeline(finish(6)); assertEquals(2,attacks(timeline).size());
		assertEquals(FRAME_RESTART,attacks(timeline).get(1).reason);
	}
	@Test public void sharkAndKarambwanDelaysAddAndEqualMessagesRemainSeparate()
	{
		start(0); attack(0); signal(1,SETTLED); signal(1,FOOD_SHARK); signal(1,FOOD_KARAMBWAN); signal(1,FOOD_KARAMBWAN);
		for(int t=2;t<=11;t++) { signal(t,SETTLED); }
		attack(12); var t=new NexActionTimeline(finish(13));
		assertEquals(2,attacks(t).size()); assertEquals(Integer.valueOf(12),attacks(t).get(1).readyBefore);
		assertEquals(2,t.entries.stream().filter(e->e.signal.kind==FOOD_KARAMBWAN).count());
	}
	@Test public void eatAndAttackSameTickAreAmbiguousInEitherCallbackOrder()
	{
		for(boolean eatFirst:List.of(true,false))
		{
			start(0); if(eatFirst) { signal(0,FOOD_SHARK); } attack(0); if(!eatFirst) { signal(0,FOOD_SHARK); }
			var t=new NexActionTimeline(finish(1)); assertTrue(attacks(t).isEmpty());
			assertTrue(t.entries.stream().anyMatch(e->e.reason==SAME_TICK_FOOD)); assertNull(t.entries.get(t.entries.size()-1).readyAfter);
		}
	}
	@Test public void settledSwapMissingTargetMovingUnknownStyleAndSpecialStayExcluded()
	{
		for(var changed:List.of(state(20997,0,false),state(26219,2,false),state(26219,null,false),state(26219,0,true)))
		{
			start(0); signal(0,ANIMATION,9471,0,changed,"NEX@test",false); signal(0,SETTLED,9471,0,changed,"NEX@test",false);
			assertTrue(attacks(new NexActionTimeline(finish(1))).isEmpty());
		}
		start(0); signal(0,ANIMATION,9471,0,fang,"NEX@test",false); signal(0,SETTLED,9471,0,state(20997,0,false),"NEX@test",false);
		assertEquals(SETTLED_INPUTS_CHANGED,new NexActionTimeline(finish(1)).entries.get(0).reason);
		start(0); signal(0,ANIMATION,9471,0,fang,null,false); signal(0,SETTLED);
		assertEquals(NO_TARGET,new NexActionTimeline(finish(1)).entries.get(0).reason);
		start(0); signal(0,ANIMATION,9471,0,fang,"NEX@test",true); signal(0,SETTLED);
		assertEquals(MOVING,new NexActionTimeline(finish(1)).entries.get(0).reason);
	}
	@Test public void unknownFoodOrAWeaponSwapInvalidatesClockAndMissingTicksExcludeRestart()
	{
		start(0); attack(0); signal(1,FOOD_UNKNOWN); signal(1,SETTLED); signal(2,SETTLED,424,0,state(20997,0,false),"NEX@test",false);
		signal(3,SETTLED); attack(4); attack(10);
		var t=new NexActionTimeline(finish(11)); assertNull(attacks(t).get(1).readyBefore);
		assertTrue(t.entries.stream().anyMatch(e->e.reason==MISSING_TICKS));
	}
	@Test public void smokeReanchorPreservesPrestartClockAndResetClearsIt()
	{
		capture.start(0,false,NexMethod.PILOT,"synthetic"); attack(0); signal(1,SETTLED); signal(2,FOOD_SHARK);
		assertTrue(capture.anchorSmoke(2,"synthetic")); signal(2,SETTLED); signal(3,SETTLED);
		var s=finish(4); var t=new NexActionTimeline(s);
		assertEquals(0,t.entries.get(0).signal.tick); assertEquals(Integer.valueOf(8),t.entries.get(t.entries.size()-1).readyAfter);
		assertEquals(2,s.startTick); capture.clear(); start(10); attack(10);
		assertNull(attacks(new NexActionTimeline(finish(11))).get(0).readyBefore);
		assertEquals(6,s.actions.signals.size()); assertThrows(UnsupportedOperationException.class,()->s.actions.signals.clear());
	}
	@Test public void zeroImpactsAreKeptAndMultipleOrUnmatchedImpactsStayAmbiguous()
	{
		start(0); attack(0);
		capture.impact(new NexObservations.Impact(0,0,++order,"NEX@test",0,NexTargetState.UNKNOWN));
		capture.impact(new NexObservations.Impact(0,0,++order,"NEX@test",20,NexTargetState.UNKNOWN));
		capture.impact(new NexObservations.Impact(0,0,++order,"UMBRA@other",0,NexTargetState.UNKNOWN));
		var t=new NexActionTimeline(finish(1)); assertEquals(3,t.impacts.size());
		assertTrue(t.impacts.stream().allMatch(l->l.ambiguous)); assertEquals(0,t.impacts.get(0).impact.amount);
		assertEquals(1,t.impacts.get(0).candidateOrders.size()); assertTrue(t.impacts.get(2).candidateOrders.isEmpty());
	}
	@Test public void interactionAndRejectedIntentNeverCreateAnAttackAndOutOfOrderEvidenceLosesClock()
	{
		start(0); signal(0,INTERACTION); signal(0,SETTLED); signal(1,INTERACTION); signal(1,SETTLED);
		attack(2); signal(1,ANIMATION,9471,0,fang,"NEX@test",false);
		var t=new NexActionTimeline(finish(3)); assertEquals(1,attacks(t).size());
		assertEquals(OUT_OF_ORDER,t.entries.get(t.entries.size()-1).reason); assertNull(t.entries.get(t.entries.size()-1).readyAfter);
	}
	@Test public void lateUnreconciledAnimationCannotBePromotedByAnImpactOrCastMessage()
	{
		start(0); signal(0,DC_NORMAL); signal(0,ANIMATION,9471,0,fang,"NEX@test",false);
		capture.impact(new NexObservations.Impact(0,0,++order,"NEX@test",0,NexTargetState.UNKNOWN));
		var t=new NexActionTimeline(finish(1)); assertTrue(attacks(t).isEmpty());
		assertEquals(NO_SETTLED_STATE,t.entries.get(1).reason); assertTrue(t.impacts.get(0).ambiguous);
	}

	@Test public void unsupportedWindowsRemainSampleExclusionsWithoutPretendingSmokeResetsClock()
	{
		start(0); signal(0,SETTLED);
		capture.phase(1,"BLOOD","BLOOD_SIPHON");
		var target=new NexTargetState(NexTargetState.Form.NEX,30,30);
		for(var kind:List.of(ANIMATION,SETTLED))
		{
			capture.action(new NexActions.Signal(kind,NexActions.Source.LOCAL_ANIMATION,1,30,++order,9471,0,false,
				capture.actionSample(1,fang,"NEX@test",9471,target)));
		}
		var t=new NexActionTimeline(finish(2)); assertEquals(1,attacks(t).size());
		assertEquals(NexActionTimeline.SampleGap.UNSUPPORTED_WINDOW,attacks(t).get(0).sampleGap);
	}

	@Test public void overflowIsExplicitAndNeverCensorsMainCaptureOrLeaksIntoNextCapture()
	{
		start(0); for(int i=0;i<NexActions.MAX+1;i++) { signal(0,DC_NORMAL); }
		assertTrue(capture.active()); var s=finish(1); assertTrue(s.actions.truncated); assertEquals(NexActions.MAX,s.actions.signals.size());
		start(2); signal(2,DC_UPGRADED); var next=finish(3); assertFalse(next.actions.truncated); assertEquals(1,next.actions.signals.size());
	}
}
