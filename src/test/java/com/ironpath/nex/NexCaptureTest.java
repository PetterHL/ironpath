package com.ironpath.nex;

import com.ironpath.encounter.CombatState;
import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.preferences.NexMethod;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Original synthetic event replays. These are not real-client kill recordings. */
public class NexCaptureTest
{
	public static CombatState state(int weapon, int shield, int energy, int charge)
	{
		Map<String,Integer> slots=new HashMap<>(), real=new HashMap<>(), visible=new HashMap<>();
		com.ironpath.advisor.PoweredMagic.SLOTS.forEach(slot->slots.put(slot,0)); slots.putAll(Map.of("weapon",weapon,"shield",shield,"ring",25975,"ammo",11212));
		for(String skill:List.of("ATTACK","STRENGTH","DEFENCE","HITPOINTS","PRAYER","RANGED","MAGIC")) { real.put(skill,99); visible.put(skill,99); }
		visible.putAll(Map.of("ATTACK",118,"STRENGTH",118,"RANGED",112));
		return new CombatState(slots,real,visible,Set.of("PIETY"),0,energy,false,
			Map.of(385,5L),Map.of("death-charge-active",charge,"worn-ammo-quantity",100));
	}
	public static NexSummary replay(int offset, int mine, int other)
	{
		List<NexSummary> summaries=new ArrayList<>(); NexCapture capture=new NexCapture(summaries::add);
		replay(capture,offset,true,false,mine,other);
		NexSummary s=summaries.get(0);
		// Separate fixture captures share one synthetic session; give them distinct kill keys.
		return new NexSummary(offset+1,s.startTick,s.endTick,s.sampledTicks,s.expectedTicks,s.unclassifiedHits,s.outcome,s.reason,
			s.cohort,s.method,s.timeline,s.damage,s.gaps,s.signals,s.beforeStart,s.context,s.comparability);
	}
	private static void replay(NexCapture c, int offset, boolean full, boolean missing, int mine, int other)
	{
		c.start(offset,full,NexMethod.PILOT,"anonymous-roster-a");
		for (int t=0;t<=36;t++)
		{
			int tick=offset+t;
			if (t==5) { c.phase(tick,"SHADOW",null); }
			if (t==8) { c.phase(tick,null,"EMBRACE_DARKNESS"); }
			if (t==12) { c.phase(tick,null,"FEAR_THE_SHADOW"); }
			if (t==15) { c.phase(tick,"BLOOD",null); }
			if (t==20) { c.phase(tick,"ICE",null); }
			if (t==25) { c.phase(tick,"ZAROS",null); }
			if (t==30) { c.death(tick); c.death(tick); }
			if (t==31) { c.barrierEnd(tick); c.barrierEnd(tick); }
			if (t==10)
			{
				c.hit(tick,1,"NEX@view:index:spawn",mine,true,false,false);
				c.hit(tick,1,"NEX@view:index:spawn",mine,true,false,false); // repeated callback
				c.hit(tick,2,"NEX@view:index:spawn",other,false,true,false);
				c.hit(tick,3,"UMBRA@view:index:spawn",7,true,false,false);
				c.hit(tick,4,"NEX@view:index:spawn",10,false,false,true); // heal
				c.hit(tick,5,"NEX@view:index:spawn",3,false,false,false); // unknown type
			}
			if (missing && t==18) { continue; }
			CombatState state=t>=8 && t<12?state(20997,0,1000,2):t==14?state(27690,22322,500,1):state(26219,22322,500,1);
			c.sample(tick,state,"NEX@view:index:spawn",t>=8 && t<12?"ranged":t==14?"spec candidate":"melee",t==14?1378:-1,"anonymous-roster-a");
		}
	}
	@Test public void multiswitchReplayKeepsDefenderAndImpactPhaseWithoutWeaponAttribution()
	{
		NexSummary s=replay(100,20,60);
		assertEquals(Outcome.COMPLETE,s.outcome); assertEquals(18,s.seconds(),1e-12);
		assertEquals(27,s.localDamage()); assertEquals(87,s.totalDamage()); assertEquals(100.0*27/87,s.share(),1e-12);
		assertEquals(1,s.unclassifiedHits);
		assertTrue(s.damage.keySet().stream().anyMatch(k->k.contains("SHADOW / EMBRACE_DARKNESS / UMBRA")));
		assertTrue(s.timeline.stream().anyMatch(x->x.role.equals("ranged") && x.state.slots.get("shield")==0));
		assertTrue(s.timeline.stream().anyMatch(x->x.tick>112 && x.state.slots.get("shield")==22322));
		assertEquals(Integer.valueOf(22322),s.timeline.get(0).state.slots.get("shield"));
		assertTrue(s.timeline.stream().anyMatch(x->x.state.resources.get("death-charge-active")==2));
		assertTrue(s.timeline.stream().anyMatch(x->x.state.specialEnergy==500));
		assertTrue(s.gaps.stream().anyMatch(x->x.contains("Impact weapon/launch identity unknown")));
		assertThrows(UnsupportedOperationException.class,()->s.timeline.clear());
	}
	@Test public void midKillMissingAndOutOfOrderEvidenceNeverBecomeFullCompletions()
	{
		for (boolean missing:List.of(false,true))
		{
			List<NexSummary> summaries=new ArrayList<>(); NexCapture c=new NexCapture(summaries::add);
			replay(c,0,!missing,missing,20,60);
			if(missing) { assertEquals(Outcome.CENSORED,summaries.get(0).outcome); }
		}
		List<NexSummary> summaries=new ArrayList<>(); NexCapture c=new NexCapture(summaries::add);
		replay(c,0,false,false,20,60); assertEquals(Outcome.CENSORED,summaries.get(0).outcome);
		assertEquals("UNKNOWN",summaries.get(0).timeline.get(0).phase);
		c.start(100,true,NexMethod.PILOT,"a"); c.sample(102,state(26219,22322,1000,0),null,"melee",-1,"a");
		c.phase(101,"SHADOW",null); c.death(103); c.barrierEnd(103); c.flush(103);
		assertEquals(Outcome.CENSORED,summaries.get(1).outcome);
	}
	@Test public void missingOrLateEndAndUnrelatedLootCannotCompleteAndBoundsAreFinite()
	{
		for (boolean death:List.of(true,false))
		{
			List<NexSummary> summaries=new ArrayList<>(); NexCapture c=new NexCapture(summaries::add);
			c.start(0,true,NexMethod.PILOT,"a");
			if(death) { c.death(10); } else { c.barrierEnd(10); }
			c.flush(16); assertEquals(Outcome.CENSORED,summaries.get(0).outcome);
		}
		List<NexSummary> summaries=new ArrayList<>(); NexCapture c=new NexCapture(summaries::add);
		c.start(0,true,NexMethod.PILOT,"a"); c.sample(NexCapture.MAX_TICKS+1,state(1,0,0,0),null,"unknown",-1,"a");
		assertEquals(Outcome.CENSORED,summaries.get(0).outcome); assertFalse(c.active());
		c.start(4000,true,NexMethod.PILOT,"a");
		for(int i=0;i<=NexCapture.MAX_SAMPLES;i++) { c.sample(4000+i,state(i,0,0,0),null,"unknown",1,"a"); }
		assertFalse(c.active()); assertTrue(summaries.get(1).reason.contains("buffer"));
	}
	@Test public void sameTickSwitchesAndLateDuplicatePhasePreserveRangedWindow()
	{
		List<NexSummary> summaries=new ArrayList<>(); NexCapture c=new NexCapture(summaries::add);
		c.start(0,true,NexMethod.PILOT,"a"); c.phase(0,"SHADOW","EMBRACE_DARKNESS");
		c.sample(0,state(26219,22322,1000,2),null,"melee",-1,"a");
		c.phase(0,"SHADOW",null); c.phase(0,"SMOKE",null);
		c.sample(0,state(20997,0,500,1),null,"ranged",-1,"a"); c.abort(1,Outcome.CENSORED,"test end");
		NexSummary s=summaries.get(0); assertEquals(2,s.timeline.size());
		assertEquals("EMBRACE_DARKNESS",s.timeline.get(1).window);
	}
	@Test public void unmatchedStartSmokeAndEndSignalsHaveSpecificReasonsAndFrozenEvidence()
	{
		List<NexSummary> summaries=new ArrayList<>(); NexCapture c=new NexCapture(summaries::add);
		c.start(0,false,NexMethod.PILOT,"a"); c.sample(0,state(26219,22322,1000,0),null,"melee",-1,"a");
		for(int t=1;t<=36;t++)
		{
			if(t==11) { c.observeSmoke(t); c.phase(t,"SMOKE",null); }
			if(t==15) { c.phase(t,"SHADOW",null); }
			if(t==20) { c.phase(t,"BLOOD",null); }
			if(t==23) { c.phase(t,"ICE",null); }
			if(t==25) { c.phase(t,"ZAROS",null); }
			if(t==30) { c.death(t); }
			if(t==31) { c.barrierEnd(t); }
			c.sample(t,state(26219,22322,1000,0),null,"melee",-1,"a");
		}
		var s=summaries.get(0); assertEquals(Outcome.CENSORED,s.outcome);
		assertTrue(s.reason.contains("smoke start not observed before combat")); assertNull(s.signals.smokeTick);
		assertEquals(Integer.valueOf(11),s.signals.observedSmokeTick);
		assertNull(s.signals.boundaryTick); assertEquals(Integer.valueOf(30),s.signals.deathTick);
		assertEquals(Integer.valueOf(31),s.signals.barrierEndTick); assertTrue(s.signals.continuous);
		assertEquals(5,s.signals.phases.size()); assertThrows(UnsupportedOperationException.class,()->s.signals.phases.clear());
		c.start(100,true,NexMethod.PILOT,"a"); c.death(110); c.flush(116);
		assertTrue(summaries.get(1).reason.contains("arena end boundary not observed"));
		assertEquals(5,s.signals.phases.size());
	}
	@Test public void dyingBeforeArenaEndCanCompleteWithoutIncludingTheDeathAnimationInTime()
	{
		List<NexSummary> summaries=new ArrayList<>(); NexCapture c=new NexCapture(summaries::add);
		c.start(0,true,NexMethod.PILOT,"a");
		for(int t=0;t<=43;t++)
		{
			if(t==5) { c.phase(t,"SHADOW",null); }
			if(t==10) { c.phase(t,"BLOOD",null); }
			if(t==15) { c.phase(t,"ICE",null); }
			if(t==20) { c.phase(t,"ZAROS",null); }
			if(t==30) { c.death(t); }
			if(t==36) { assertTrue(c.active()); assertTrue(summaries.isEmpty()); }
			if(t==38) { c.barrierEnd(t); }
			c.sample(t,state(26219,22322,1000,0),null,"melee",-1,"a");
		}
		var summary=summaries.get(0);
		assertEquals(Outcome.COMPLETE,summary.outcome);
		assertEquals(Integer.valueOf(38),summary.signals.barrierEndTick);
		assertFalse(summary.reason.contains("end signals outside matching window"));
		assertFalse(summary.reason.contains("arena end boundary not observed"));
		assertEquals(30,summary.endTick); assertEquals(30,summary.sampledTicks);
	}
	@Test public void diagnosticSignalsAreBoundedImmutableAndClearedBetweenAttempts()
	{
		List<NexSummary> summaries=new ArrayList<>(); NexCapture c=new NexCapture(summaries::add);
		c.start(0,false,NexMethod.PILOT,"a");
		for(int i=0;i<200;i++) { c.signal("signal "+i); }
		c.abort(1,Outcome.CENSORED,"test end");
		var trace=summaries.get(0).signals.trace;
		assertEquals(NexCapture.MAX_SIGNALS,trace.size());
		assertEquals("signal 0",trace.get(0)); assertEquals("signal 199",trace.get(trace.size()-1));
		assertThrows(UnsupportedOperationException.class,()->trace.clear());
		c.start(2,false,NexMethod.PILOT,"a"); c.abort(3,Outcome.CENSORED,"next end");
		assertTrue(summaries.get(1).signals.trace.isEmpty()); assertEquals(NexCapture.MAX_SIGNALS,trace.size());
	}
	@Test public void missingEndEvidenceHasABoundedDiagnosticWait()
	{
		List<NexSummary> summaries=new ArrayList<>(); NexCapture c=new NexCapture(summaries::add);
		c.start(0,true,NexMethod.PILOT,"a"); c.death(10);
		c.sample(10+NexCapture.END_WAIT-1,state(26219,22322,1000,0),null,"melee",-1,"a");
		assertTrue(c.active());
		c.sample(10+NexCapture.END_WAIT,state(26219,22322,1000,0),null,"melee",-1,"a");
		assertFalse(c.active()); assertEquals(1,summaries.size());
		assertTrue(summaries.get(0).reason.contains("arena end boundary not observed"));
	}
	@Test public void endCorrelationHasDirectionalBoundsAndCannotRecoverAfterTimeout()
	{
		for(int lag:new int[]{-6,-5,0,20,21})
		{
			List<NexSummary> summaries=new ArrayList<>(); NexCapture c=new NexCapture(summaries::add);
			c.start(0,true,NexMethod.PILOT,"a");
			for(int t=0;t<=56;t++)
			{
				if(t==5) { c.phase(t,"SHADOW",null); }
				if(t==10) { c.phase(t,"BLOOD",null); }
				if(t==15) { c.phase(t,"ICE",null); }
				if(t==20) { c.phase(t,"ZAROS",null); }
				if(t==30) { c.death(t); }
				if(t==30+lag) { c.barrierEnd(t); }
				c.sample(t,state(26219,22322,1000,0),null,"melee",-1,"a");
			}
			assertEquals(1,summaries.size());
			assertEquals("lag "+lag,lag>=-5 && lag<=20?Outcome.COMPLETE:Outcome.CENSORED,summaries.get(0).outcome);
			assertEquals(30,summaries.get(0).endTick);
		}
	}
	@Test public void smokeAnchorSeparatesEarlierStatesAndDoesNotResetTotalCaptureLimits()
	{
		List<NexSummary> summaries=new ArrayList<>(); NexCapture c=new NexCapture(summaries::add);
		c.start(0,false,NexMethod.PILOT,"waiting-roster");
		c.sample(0,state(1,0,1000,0),null,"unclassified",-1,"waiting-roster");
		assertTrue(c.anchorSmoke(21,"fight-roster"));
		c.sample(21,state(26219,22322,1000,0),null,"melee",-1,"fight-roster");
		assertFalse(c.anchorSmoke(22,"fight-roster"));
		c.sample(NexCapture.MAX_TICKS+1,state(26219,22322,1000,0),null,"melee",-1,"fight-roster");
		var summary=summaries.get(0); assertEquals(21,summary.startTick);
		assertTrue(summary.reason.contains("time limit"));
		assertEquals("UNKNOWN",summary.beforeStart.get(0).phase); assertEquals(0,summary.beforeStart.get(0).tick);
		assertEquals("SMOKE",summary.timeline.get(0).phase); assertEquals(21,summary.timeline.get(0).tick);
		assertThrows(UnsupportedOperationException.class,()->summary.beforeStart.clear());
		assertTrue(summary.gaps.stream().noneMatch(g->g.startsWith("Smoke start not observed")));
	}
	@Test public void preSmokeAndFightStatesShareTheSameBufferLimitAndClearIndependentlyOfFrozenHistory()
	{
		List<NexSummary> summaries=new ArrayList<>(); NexCapture c=new NexCapture(summaries::add);
		c.start(0,false,NexMethod.PILOT,"a");
		for(int t=0;t<512;t++) { c.sample(t,state(26219,22322,1000,0),null,"melee",1,"a"); }
		assertTrue(c.anchorSmoke(512,"a"));
		for(int t=512;t<=NexCapture.MAX_SAMPLES;t++) { c.sample(t,state(26219,22322,1000,0),null,"melee",1,"a"); }
		var summary=summaries.get(0); assertTrue(summary.reason.contains("buffer limit"));
		assertEquals(NexCapture.MAX_SAMPLES,summary.beforeStart.size()+summary.timeline.size());
		assertEquals(512,summary.beforeStart.size());
		c.start(2000,false,NexMethod.PILOT,"a"); c.abort(2001,Outcome.CENSORED,"next attempt");
		assertTrue(summaries.get(1).beforeStart.isEmpty()); assertEquals(512,summary.beforeStart.size());
	}
}
