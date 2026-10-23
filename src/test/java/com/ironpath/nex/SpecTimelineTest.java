package com.ironpath.nex;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.ironpath.nex.SpecTimeline.*;

/** Hand-calculated resource cases, independent of damage formulas and live capture eligibility. */
public class SpecTimelineTest
{
	private final SpecTimeline model=new SpecTimeline();
	private Result run(int energy,int end,Ring ring,int first,Event... events)
	{ return model.run(new Plan(energy,end,ring,first,RestoreOrder.BEFORE_EVENTS,List.of(events))); }
	@Test public void specsCompeteForTheSameEnergyAndOrdinaryAttackOpportunities()
	{
		Result r=run(1000,20,Ring.LIGHTBEARER,25,Event.voidwaker(0),Event.ordinary(1,5),
			Event.zaryteCrossbow(4,true),Event.ordinary(4,5),Event.voidwaker(9),Event.ordinary(13,5));
		assertEquals(2,r.voidwakerSpecs); assertEquals(0,r.zaryteSpecs); assertEquals(2,r.ordinaryAttacks);
		assertEquals(0,r.finalEnergy); assertEquals(1000,r.spent);
		assertEquals(Status.ATTACK_DELAY,r.entries.get(1).status);
		assertEquals(Status.INSUFFICIENT_ENERGY,r.entries.get(2).status);
		Result baseline=run(1000,20,Ring.LIGHTBEARER,25,Event.ordinary(0,5),Event.ordinary(5,5),Event.ordinary(10,5),Event.ordinary(15,5));
		assertEquals(4,baseline.ordinaryAttacks); assertEquals(1000,baseline.finalEnergy);
		// Accepted specs replace opportunities rather than being added to all four ordinary attacks.
		assertEquals(baseline.ordinaryAttacks,r.ordinaryAttacks+r.voidwakerSpecs);
	}
	@Test public void lightbearerProducesFiniteRestoresInsteadOfADpsMultiplier()
	{
		Result normal=run(0,101,Ring.STANDARD,50);
		Result light=run(0,101,Ring.LIGHTBEARER,25);
		assertEquals(200,normal.finalEnergy); assertEquals(400,light.finalEnergy);
		assertEquals(0,light.voidwakerSpecs); assertEquals(0,light.zaryteSpecs);
		assertEquals(0,run(0,25,Ring.LIGHTBEARER,25).finalEnergy); // exclusive end
		assertEquals(100,run(0,26,Ring.LIGHTBEARER,25).finalEnergy);
	}
	@Test public void fullEnergyStopsNaturalRestorationUntilAnIntervalAfterSpending()
	{
		Result r=run(1000,80,Ring.LIGHTBEARER,25,Event.voidwaker(26),Event.voidwaker(30));
		assertEquals(0,r.wasted); assertEquals(200,r.restored); assertEquals(200,r.finalEnergy);
		assertEquals(List.of(51,76),r.entries.stream().filter(e->e.eventIndex==-1).map(e->e.tick).collect(java.util.stream.Collectors.toList()));
		assertEquals(1000+r.restored-r.spent,r.finalEnergy);
	}
	@Test public void recordedFullCapAndBelowCapOffsetsHaveDifferentClockBehavior()
	{
		// Existing report: full at 129, spending at 137, next +100 at 162 (offsets 0,8,33).
		for(RestoreOrder order:RestoreOrder.values()) for(int phase=0;phase<=25;phase++)
		{
			var r=model.run(new Plan(1000,34,Ring.LIGHTBEARER,phase,order,List.of(Event.voidwaker(8))));
			assertEquals(600,r.finalEnergy); assertEquals(100,r.restored); assertEquals(0,r.wasted);
			assertEquals(33,r.entries.stream().filter(e->e.eventIndex==-1).findFirst().get().tick);
		}
		// Below full, a spend at 224 does not move the 212 -> 237 cycle (offsets 0,12,25).
		var below=run(600,26,Ring.LIGHTBEARER,25,Event.voidwaker(12));
		assertEquals(200,below.finalEnergy);
		assertEquals(25,below.entries.stream().filter(e->e.eventIndex==-1).findFirst().get().tick);
		var standard=run(1000,59,Ring.STANDARD,0,Event.voidwaker(8));
		assertEquals(58,standard.entries.stream().filter(e->e.eventIndex==-1).findFirst().get().tick);
	}
	@Test public void reachingFullByNaturalOrDeclaredDeathChargeRestartsTheClockAfterLaterSpending()
	{
		var natural=run(900,55,Ring.LIGHTBEARER,5,Event.voidwaker(29));
		assertEquals(List.of(5,54),natural.entries.stream().filter(e->e.eventIndex==-1).map(e->e.tick).collect(java.util.stream.Collectors.toList()));
		assertEquals(600,natural.finalEnergy); assertEquals(0,natural.wasted);
		var proc=run(950,29,Ring.LIGHTBEARER,10,Event.cast(0,true),Event.eligibleDeath(1,"declared"),Event.voidwaker(3));
		assertEquals(600,proc.finalEnergy); assertEquals(100,proc.wasted);
		assertEquals(28,proc.entries.stream().filter(e->e.eventIndex==-1).findFirst().get().tick);
		// Within-tick caller order remains explicit, including a cap-reaching proc then a spend.
		var sameTick=run(950,27,Ring.LIGHTBEARER,1,Event.cast(0,true),Event.eligibleDeath(1,"declared"),Event.voidwaker(1));
		assertEquals(26,sameTick.entries.stream().filter(e->e.eventIndex==-1 && e.tick>1).findFirst().get().tick);
	}
	@Test public void improvedDeathChargeNeedsACastAndTwoDistinctEligibleDeaths()
	{
		Result r=run(0,24,Ring.LIGHTBEARER,25,Event.eligibleDeath(0,"before-cast"),Event.cast(1,true),
			Event.eligibleDeath(2,"a"),Event.eligibleDeath(3,"a"),Event.eligibleDeath(4,"b"),Event.eligibleDeath(5,"c"));
		assertEquals(300,r.finalEnergy); assertEquals(2,r.deathChargeProcs);
		assertEquals(Status.NO_ACTIVE_CHARGE,r.entries.get(0).status);
		assertEquals(Status.DUPLICATE_DEATH,r.entries.get(3).status);
		assertEquals(Status.NO_ACTIVE_CHARGE,r.entries.get(5).status);
		assertEquals(150,run(0,24,Ring.LIGHTBEARER,25,Event.cast(0,false),Event.eligibleDeath(1,"a"),Event.eligibleDeath(2,"b")).finalEnergy);
		assertEquals(0,run(0,24,Ring.LIGHTBEARER,25,Event.cast(0,true)).finalEnergy);
	}
	@Test public void procsConsumeChargesAtFullEnergyAndShareTheCapWithRegeneration()
	{
		Result r=run(950,24,Ring.LIGHTBEARER,25,Event.cast(0,true),Event.eligibleDeath(1,"a"),Event.eligibleDeath(2,"b"),Event.voidwaker(3));
		assertEquals(2,r.deathChargeProcs); assertEquals(50,r.restored); assertEquals(250,r.wasted); assertEquals(500,r.finalEnergy);
		assertEquals(950+r.restored-r.spent,r.finalEnergy);
	}
	@Test public void cooldownStartsAtCastAndDoesNotResetWhenChargesAreConsumed()
	{
		Result r=run(0,105,Ring.STANDARD,50,Event.cast(0,true),Event.eligibleDeath(1,"a"),
			Event.eligibleDeath(2,"b"),Event.cast(3,true),Event.cast(99,false),Event.cast(100,false),Event.eligibleDeath(101,"c"));
		assertEquals(3,r.deathChargeProcs); assertEquals(650,r.finalEnergy);
		assertEquals(2,r.entries.stream().filter(e->e.status==Status.CAST_COOLDOWN).count());
		assertEquals(0,run(0,101,Ring.STANDARD,50,Event.cast(0,true),Event.eligibleDeath(100,"expired")).deathChargeProcs);
		assertEquals(1,run(0,100,Ring.STANDARD,50,Event.cast(0,true),Event.eligibleDeath(99,"active")).deathChargeProcs);
	}
	@Test public void sameTickOrderAndUnknownRestorationPhaseRemainExplicitSensitivityCases()
	{
		List<Event> events=List.of(Event.voidwaker(25));
		Result before=model.run(new Plan(400,26,Ring.LIGHTBEARER,25,RestoreOrder.BEFORE_EVENTS,events));
		Result after=model.run(new Plan(400,26,Ring.LIGHTBEARER,25,RestoreOrder.AFTER_EVENTS,events));
		assertEquals(1,before.voidwakerSpecs); assertEquals(0,before.finalEnergy);
		assertEquals(0,after.voidwakerSpecs); assertEquals(500,after.finalEnergy);
		List<Result> cases=model.unknownRestorationPhase(400,2,Ring.LIGHTBEARER,List.of(Event.voidwaker(1)));
		assertEquals(52,cases.size()); assertTrue(cases.stream().anyMatch(r->r.voidwakerSpecs==0)); assertTrue(cases.stream().anyMatch(r->r.voidwakerSpecs==1));
		List<Result> atStart=model.unknownRestorationPhase(400,1,Ring.LIGHTBEARER,List.of(Event.voidwaker(0)));
		assertTrue(atStart.stream().anyMatch(r->r.voidwakerSpecs==1)); assertTrue(atStart.stream().anyMatch(r->r.voidwakerSpecs==0));
		Result castFirst=run(0,2,Ring.STANDARD,50,Event.cast(0,true),Event.eligibleDeath(0,"a"));
		Result deathFirst=run(0,2,Ring.STANDARD,50,Event.eligibleDeath(0,"a"),Event.cast(0,true));
		assertEquals(150,castFirst.finalEnergy); assertEquals(0,deathFirst.finalEnergy);
	}
	@Test public void zaryteCostsAndItsAttackDelayAreNotVoidwakerDefaults()
	{
		Result rapid=run(1000,11,Ring.STANDARD,50,Event.zaryteCrossbow(0,true),Event.ordinary(4,5),Event.ordinary(5,5));
		assertEquals(750,rapid.spent); assertEquals(1,rapid.zaryteSpecs); assertEquals(1,rapid.ordinaryAttacks);
		assertEquals(Status.ATTACK_DELAY,rapid.entries.get(1).status);
		Result other=run(1000,11,Ring.STANDARD,50,Event.zaryteCrossbow(0,false),Event.ordinary(5,5),Event.ordinary(6,5));
		assertEquals(Status.ATTACK_DELAY,other.entries.get(1).status); assertEquals(1,other.ordinaryAttacks);
		assertEquals(0,run(749,1,Ring.STANDARD,50,Event.zaryteCrossbow(0,true)).zaryteSpecs);
	}
	@Test public void rejectsUnknownInvalidUnorderedAndUnboundedPlansAndFreezesResults()
	{
		assertThrows(IllegalArgumentException.class,()->run(-1,1,Ring.STANDARD,50));
		assertThrows(IllegalArgumentException.class,()->run(1001,1,Ring.STANDARD,50));
		assertThrows(IllegalArgumentException.class,()->run(0,MAX_TICKS+1,Ring.STANDARD,50));
		assertThrows(IllegalArgumentException.class,()->run(0,10,Ring.LIGHTBEARER,26));
		assertThrows(IllegalArgumentException.class,()->run(0,10,Ring.STANDARD,50,Event.cast(5,true),Event.cast(4,true)));
		assertThrows(IllegalArgumentException.class,()->run(0,10,Ring.STANDARD,50,Event.cast(10,true)));
		assertThrows(IllegalArgumentException.class,()->Event.ordinary(0,0));
		assertThrows(IllegalArgumentException.class,()->Event.eligibleDeath(0,""));
		assertThrows(IllegalArgumentException.class,()->model.run(new Plan(0,1,Ring.STANDARD,50,RestoreOrder.BEFORE_EVENTS,Collections.nCopies(MAX_EVENTS+1,Event.cast(0,true)))));
		List<Event> events=new ArrayList<>(); events.add(Event.voidwaker(0));
		Plan plan=new Plan(1000,1,Ring.STANDARD,50,RestoreOrder.BEFORE_EVENTS,events); events.clear();
		Result r=model.run(plan); assertEquals(1,r.voidwakerSpecs);
		assertThrows(UnsupportedOperationException.class,()->r.entries.clear());
	}
}
