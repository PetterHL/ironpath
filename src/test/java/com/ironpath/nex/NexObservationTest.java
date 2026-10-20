package com.ironpath.nex;

import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.preferences.NexMethod;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Arithmetic and lifecycle tests of synthetic evidence, not live RuneLite validation. */
public class NexObservationTest
{
	@Test public void selectedZarosOverheadSeparatesCountsAndImportedLaunchTicksWithoutUsingHpAsAKey()
	{
		List<NexSummary> out=new ArrayList<>(); var c=new NexCapture(out::add); c.start(0,true,NexMethod.PILOT,"roster");
		c.phase(0,"ZAROS","UNKNOWN"); var state=NexCaptureTest.state(26219,12954,1000,2);
		for(int t=0;t<3;t++)
		{
			var form=t==1?NexTargetState.Form.DEFLECT_MELEE:NexTargetState.Form.SOUL_SPLIT;
			var target=new NexTargetState(form,30-t,30);
			c.sample(t,state,"NEX@spawn","melee",-1,"roster",target);
			c.launch(t,30*t,t+1,9471,state,"NEX@spawn",target,NexObservations.Weapon.FANG,NexObservations.Attack.ORDINARY_CANDIDATE);
		}
		c.abort(3,Outcome.CENSORED,"test"); var summary=out.get(0);
		assertEquals(List.of(0,2),NexObservationReview.planLaunches(summary,summary.timeline.get(0)).stream().map(l->l.sample.tick).collect(java.util.stream.Collectors.toList()));
		assertEquals(2,NexObservationReview.launches(summary,summary.timeline.get(0)).size());
		assertEquals(1,NexObservationReview.launches(summary,summary.timeline.get(1)).size());
		var otherSpawn=new NexSummary.Sample(0,"ZAROS","UNKNOWN","NEX@different","melee",-1,state,summary.timeline.get(0).targetState);
		assertTrue(NexObservationReview.launches(summary,otherSpawn).isEmpty());
	}
	@Test public void barIntervalsContainEveryReachableIntegerWithoutInventingExactHp()
	{
		for(int max:new int[]{1,10,99,500,1000,3400}) for(int scale:new int[]{1,2,30,255}) for(int hp=0;hp<=max;hp++)
		{
			int ratio=hp==0?0:scale==1?1:1+(int)((long)hp*(scale-1)/max);
			var h=NexHealth.bar(ratio,scale,max,4,120,"NEX@private",11278,-1);
			assertNotNull(h.lower); assertTrue(h.lower<=hp); assertTrue(h.upper>=hp);
			assertEquals(NexHealth.Source.ACTOR_BAR,h.source); assertEquals(NexHealth.Validity.BOUNDED,h.validity);
		}
		var half=NexHealth.bar(15,30,1000,1,30,"NEX@private",11278,-1);
		assertEquals(Integer.valueOf(483),half.lower); assertEquals(Integer.valueOf(517),half.upper);
		assertSame(NexHealth.UNKNOWN,NexHealth.bar(-1,30,3400,1,30,"NEX",11278,-1));
		assertSame(NexHealth.UNKNOWN,NexHealth.bar(10,0,3400,1,30,"NEX",11278,-1));
		assertSame(NexHealth.UNKNOWN,NexHealth.hud(3401,3400,1,30,"NEX",11278,-1));
	}
	@Test public void smokeReanchorAndBufferBoundCannotLeakPriorOrLaterObservations()
	{
		List<NexSummary> out=new ArrayList<>(); var c=new NexCapture(out::add);
		c.start(0,false,NexMethod.PILOT,"roster");
		c.hudCheck(NexObservations.HudCheck.DISABLED);
		c.health(new NexObservations.Health(0,0,0,"NEX@old",new NexTargetState(NexTargetState.Form.NEX,30,30)));
		c.resource(new NexObservations.Signal(NexObservations.Kind.ENERGY,0,0,1,500,500,null));
		assertTrue(c.anchorSmoke(1,"roster"));
		for(int i=0;i<NexObservations.MAX_HUD_COUNT+10;i++) { c.hudCheck(NexObservations.HudCheck.SCRIPT_CALLBACK); }
		assertTrue(c.active());
		for(int i=0;i<NexObservations.MAX+10;i++)
		{ c.resource(new NexObservations.Signal(NexObservations.Kind.ENERGY,1,30,i+2,1000,1000,null)); }
		c.abort(2,Outcome.CENSORED,"test");
		assertEquals(NexObservations.MAX,out.get(0).observations.signals.size()); assertTrue(out.get(0).observations.truncated);
		assertTrue(out.get(0).observations.signals.stream().allMatch(s->s.value==1000));
		assertTrue(out.get(0).observations.health.isEmpty());
		assertEquals(Map.of(NexObservations.HudCheck.SCRIPT_CALLBACK,NexObservations.MAX_HUD_COUNT),out.get(0).observations.hudChecks);
		assertThrows(UnsupportedOperationException.class,()->out.get(0).observations.hudChecks.clear());
		assertThrows(UnsupportedOperationException.class,()->out.get(0).observations.signals.clear());
		c.start(3,true,NexMethod.PILOT,"roster"); c.abort(4,Outcome.CENSORED,"next");
		assertTrue(out.get(1).observations.signals.isEmpty()); assertFalse(out.get(1).observations.truncated);
		assertTrue(out.get(1).observations.hudChecks.isEmpty());
	}
	@Test public void overlappingAndZeroHitsKeepAllCandidatesAndNeverUseImpactEquipment()
	{
		List<NexSummary> out=new ArrayList<>(); var c=new NexCapture(out::add);
		c.start(0,true,NexMethod.PILOT,"roster");
		var bow=NexCaptureTest.state(20997,0,1000,2);
		c.sample(0,bow,"NEX@spawn","ranged",-1,"roster");
		c.launch(1,30,1,426,bow,"NEX@spawn",NexTargetState.UNKNOWN,NexObservations.Weapon.TWISTED_BOW,NexObservations.Attack.ORDINARY_CANDIDATE);
		c.flight(new NexObservations.Flight(1,30,2,1120,30,90,"NEX@spawn",true));
		c.launch(2,60,3,426,bow,"NEX@spawn",NexTargetState.UNKNOWN,NexObservations.Weapon.TWISTED_BOW,NexObservations.Attack.ORDINARY_CANDIDATE);
		c.sample(2,NexCaptureTest.state(26219,12954,1000,2),"NEX@spawn","melee",-1,"roster");
		c.impact(new NexObservations.Impact(3,90,4,"NEX@spawn",0,NexTargetState.UNKNOWN));
		c.impact(new NexObservations.Impact(3,90,5,"UMBRA@spawn",10,NexTargetState.UNKNOWN));
		c.abort(4,Outcome.CENSORED,"test");
		var review=new NexObservationReview(out.get(0));
		assertEquals(2,review.matches.get(0).candidates.size()); assertTrue(review.matches.get(1).candidates.isEmpty());
		assertEquals(0,review.matches.get(0).impact.amount);
		assertTrue(review.matches.get(0).candidates.stream().allMatch(l->l.sample.state.slots.get("weapon")==20997));
	}
	@Test public void settledSwitchDoesNotRewriteLaunchOrTurnStartingStockIntoConsumption()
	{
		List<NexSummary> out=new ArrayList<>(); var c=new NexCapture(out::add);
		c.start(0,true,NexMethod.PILOT,"roster"); var old=NexCaptureTest.state(20997,0,1000,2);
		var state=new com.ironpath.encounter.CombatState(old.slots,old.realLevels,old.visibleLevels,old.prayers,old.style,old.specialEnergy,false,
			old.inventory,Map.of("spec-energy-server",1000));
		c.sample(0,state,"NEX@spawn","ranged",-1,"roster");
		c.launch(0,0,1,426,state,"NEX@spawn",NexTargetState.UNKNOWN,NexObservations.Weapon.TWISTED_BOW,NexObservations.Attack.ORDINARY_CANDIDATE);
		c.resource(new NexObservations.Signal(NexObservations.Kind.TICK_RESOURCES,0,1,2,1000,1000,"NEX@spawn",NexCaptureTest.state(26219,12954,1000,2)));
		var dropped=new com.ironpath.encounter.CombatState(state.slots,state.realLevels,state.visibleLevels,state.prayers,state.style,1000,false,Map.of(),state.resources);
		c.sample(1,dropped,"NEX@spawn","ranged",-1,"roster"); c.abort(2,Outcome.CENSORED,"test");
		var review=new NexObservationReview(out.get(0)); var launch=out.get(0).observations.launches.get(0);
		assertEquals(NexObservationReview.Reconciliation.INPUTS_CHANGED,review.reconciled.get(launch));
		assertEquals(Integer.valueOf(20997),launch.sample.state.slots.get("weapon"));
		assertEquals(Integer.valueOf(1000),review.startEnergy); assertEquals(Integer.valueOf(5),review.startSupplies.get(NexSupplies.Kind.SHARK));
		assertEquals(Long.valueOf(5),review.startResources.state.inventory.get(385));
	}
	@Test public void lateDeathChargeEvidenceSurvivesOnlyTheExistingEndGrace()
	{
		List<NexSummary> out=new ArrayList<>(); var c=new NexCapture(out::add); c.start(0,true,NexMethod.PILOT,"roster");
		c.sample(0,NexCaptureTest.state(26219,12954,500,1),"NEX@spawn","melee",-1,"roster"); c.death(1); c.barrierEnd(2);
		c.resource(new NexObservations.Signal(NexObservations.Kind.ENERGY,2,60,1,650,650,null));
		c.resource(new NexObservations.Signal(NexObservations.Kind.DC_END_EFFECT,2,60,2,3289,-1,null));
		c.flush(7); assertEquals(1,out.get(0).endTick); assertEquals(2,out.get(0).observations.signals.size());
		c.resource(new NexObservations.Signal(NexObservations.Kind.ENERGY,8,240,3,750,750,null));
		assertEquals(2,out.get(0).observations.signals.size()); assertEquals(0,out.get(0).totalDamage());
	}
}
