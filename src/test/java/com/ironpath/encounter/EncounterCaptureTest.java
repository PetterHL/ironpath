package com.ironpath.encounter;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class EncounterCaptureTest
{
	static final EncounterContext SOLO=new EncounterContext(null,null,"zilyana","ordinary","normal",1,"solo",Map.of());
	static CombatState state(int level)
	{ return new CombatState(Map.of("weapon",25867),Map.of("RANGED",90),Map.of("RANGED",level),Set.of(),1,1000,false); }
	static EncounterCapture capture()
	{ EncounterCapture c=new EncounterCapture(); c.reset(7); c.start(1,7,SOLO,"pilot-1",10,state(103)); return c; }
	@Test public void deathAndLocalCreditFinalizeOnceWithFrozenState()
	{
		EncounterCapture c=capture(); c.localDamage(11,20); c.sample(11,state(103)); c.sample(12,state(102)); for(int i=13;i<30;i++) { c.sample(i,state(102)); } c.death(30); c.loot(31); c.loot(32);
		assertTrue(c.recent().isEmpty()); c.sample(35,state(90));
		EncounterSummary s=c.recent().get(0); assertEquals(EncounterSummary.Outcome.COMPLETE,s.outcome);
		assertEquals(30,s.endTick); assertEquals(20,s.localDamage); assertEquals(2,s.segments.size());
		assertEquals(Integer.valueOf(102),s.segments.get(1).state.visibleLevels.get("RANGED"));
		assertEquals(20,s.segments.stream().mapToInt(EncounterSummary.Segment::ticks).sum());
		c.loot(36); c.death(36); assertEquals(1,c.recent().size());
	}
	@Test public void missingDeathMissingCreditMidKillAndFailedAreDistinct()
	{
		EncounterCapture c=capture(); c.localDamage(11,20); c.loot(12); c.abort(30,EncounterSummary.Outcome.CENSORED,"Despawn without death");
		assertEquals(EncounterSummary.Outcome.CENSORED,c.recent().get(0).outcome);
		c.start(2,7,SOLO,"pilot",40,state(100)); c.localDamage(41,30); c.death(50); c.sample(55,state(100));
		assertEquals(EncounterSummary.Outcome.CENSORED,c.recent().get(1).outcome);
		c.start(3,7,SOLO,"pilot",60,state(100)); c.gap("Engagement began with an already-damaged target"); c.localDamage(61,10); c.death(70); c.loot(70); c.sample(75,state(100));
		assertEquals(EncounterSummary.Outcome.CENSORED,c.recent().get(2).outcome);
		c.start(4,7,SOLO,"pilot",80,state(100)); c.abort(90,EncounterSummary.Outcome.FAILED,"Player died");
		assertEquals(EncounterSummary.Outcome.FAILED,c.recent().get(3).outcome);
	}
	@Test public void lateLootCannotReopenDeathOrCompleteNextEncounter()
	{
		EncounterCapture c=capture(); c.localDamage(11,10); c.death(20); c.sample(25,state(100)); c.loot(26);
		assertEquals(EncounterSummary.Outcome.CENSORED,c.recent().get(0).outcome);
		c.start(2,7,SOLO,"pilot",200,state(100)); c.loot(201); c.localDamage(202,10); c.death(220); c.sample(225,state(100));
		assertEquals(EncounterSummary.Outcome.CENSORED,c.recent().get(1).outcome);
	}
	@Test public void sameTickSwitchesRetainAmbiguityWithoutInventingElapsedTicks()
	{
		EncounterCapture c=capture(); c.sample(10,state(102)); c.sample(10,state(101)); c.localDamage(11,10); for(int i=11;i<20;i++) { c.sample(i,state(101)); } c.death(20); c.loot(20); c.sample(25,state(100));
		EncounterSummary s=c.recent().get(0); assertTrue(s.gaps.stream().anyMatch(g->g.contains("Same-tick")));
		assertEquals(10,s.segments.stream().mapToInt(EncounterSummary.Segment::ticks).sum());
	}
	@Test public void resetRejectsOldGenerationAndClearsAllPerformanceEvidence()
	{
		EncounterCapture c=capture(); c.reset(8); c.localDamage(11,50); c.death(20); c.loot(20);
		c.start(2,7,SOLO,"pilot",30,state(99)); assertFalse(c.active()); assertTrue(c.recent().isEmpty());
	}
	@Test public void stateAndEventOverflowAreCensoredNotFastKills()
	{
		EncounterCapture c=capture();
		for(int i=0;i<EncounterCapture.MAX_SEGMENTS+1;i++) { c.sample(11+i,state(100+i%2)); }
		assertFalse(c.active()); assertEquals(EncounterSummary.Outcome.CENSORED,c.recent().get(0).outcome);
		c=capture(); for(int i=0;i<EncounterCapture.MAX_EVENTS+1;i++) { c.localDamage(11,0); }
		assertEquals(EncounterSummary.Outcome.CENSORED,c.recent().get(0).outcome);
	}
	@Test public void sharedContractKeepsRaidRoomPhasePartyAndScalingUnknown()
	{
		EncounterCapture c=new EncounterCapture(); c.reset(1);
		EncounterContext room=new EncounterContext("toa","synthetic-run","akkha","melee","normal",null,null,Map.of());
		c.start(9,1,room,"fixture-only",1,state(90)); c.abort(10,EncounterSummary.Outcome.EXCLUDED,"No live adapter");
		EncounterSummary s=c.recent().get(0); assertEquals("toa",s.context.activity); assertEquals("akkha",s.context.encounter);
		assertNull(s.context.partySize); assertTrue(s.context.scaling.isEmpty());
		EncounterContext phase=new EncounterContext(null,null,"muspah","ranged","normal",1,"solo",Map.of());
		c.start(10,1,phase,"fixture-only",20,state(90)); c.abort(30,EncounterSummary.Outcome.EXCLUDED,"No live adapter");
		assertEquals("ranged",c.recent().get(1).context.phase);
	}
	@Test public void recentHistoryAndTimeoutStayBoundedAndMissingSamplesAreCensored()
	{
		EncounterCapture c=capture(); c.localDamage(11,20); c.death(30); c.loot(30); c.sample(35,state(100));
		assertEquals(EncounterSummary.Outcome.CENSORED,c.recent().get(0).outcome);
		assertTrue(c.recent().get(0).gaps.contains("Missing tick/state samples"));
		c=capture(); c.sample(EncounterCapture.MAX_TICKS+11,state(100));
		assertEquals(EncounterSummary.Outcome.CENSORED,c.recent().get(0).outcome);
		long begin=System.nanoTime();
		for(int run=0;run<100;run++)
		{
			int start=run*10000+10000;
			c.start(run+10,7,SOLO,"fixture",start,state(100));
			c.localDamage(start,20);
			for(int tick=start+1;tick<start+100;tick++) { c.sample(tick,state(100)); }
			c.death(start+100); c.loot(start+100); c.sample(start+105,state(100));
		}
		assertEquals(EncounterCapture.MAX_RECENT,c.recent().size());
		assertTrue(c.recent().stream().allMatch(s->s.outcome==EncounterSummary.Outcome.COMPLETE));
		try {
			java.nio.file.Path path=java.nio.file.Path.of("build/reports/ki1/resource-budget.txt");
			java.nio.file.Files.createDirectories(path.getParent());
			java.nio.file.Files.writeString(path,"Pure replay: 100 encounters / 9900 unchanged-state samples in "+
				((System.nanoTime()-begin)/1_000_000.0)+" ms; retained "+c.recent().size()+" summaries.\n"+
				"Limits: 512 segments, 20000 events, 3000 encounter ticks; overflow/timeout censored. Not client-thread latency evidence.\n");
		} catch(java.io.IOException e) { throw new AssertionError(e); }
	}

}
