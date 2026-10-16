package com.ironpath.nex;

import com.ironpath.encounter.CombatState;
import com.ironpath.encounter.EncounterSummary.Outcome;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NexResourceReviewTest
{
	private NexSummary.Sample sample(int tick,Integer energy,Integer charge,Integer cooldown,Integer ring)
	{
		Map<String,Integer> resources=new HashMap<>(),slots=new HashMap<>();
		if(charge!=null) { resources.put("death-charge-active",charge); }
		if(cooldown!=null) { resources.put("death-charge-cooldown",cooldown); }
		if(ring!=null) { slots.put("ring",ring); }
		return new NexSummary.Sample(tick,"SMOKE","UNKNOWN",null,"unclassified",-1,
			new CombatState(slots,Map.of(),Map.of(),Set.of(),null,energy,false,null,resources));
	}
	private NexSummary summary(List<NexSummary.Sample> timeline,List<NexSummary.Sample> before)
	{
		return new NexSummary(1,10,20,10,10,0,Outcome.COMPLETE,"synthetic","cohort","method",timeline,Map.of(),Set.of(),
			new NexSummary.Signals(null,10,20,20,true,Set.of("SMOKE")),before);
	}
	@Test public void retainsSameTickOrderAndCountsOnlySampledNetChangesWithoutAttribution()
	{
		var r=NexResourceReview.from(summary(List.of(sample(10,1000,2,1,25975),sample(11,500,1,1,25975),
			sample(11,650,1,1,25975),sample(12,650,1,1,25975),sample(19,750,0,1,25975)),List.of(sample(0,0,0,0,25975))));
		assertEquals(4,r.points.size()); assertEquals(11,r.points.get(1).tick); assertEquals(11,r.points.get(2).tick);
		assertEquals(250,r.sampledIncreases); assertEquals(500,r.sampledDecreases);
		assertEquals(Integer.valueOf(1000),r.firstEnergy); assertEquals(Integer.valueOf(750),r.lastEnergy);
		assertTrue(r.improvedActiveObserved); assertTrue(r.fixedLightbearerObserved); assertTrue(r.gaps.isEmpty());
		assertThrows(UnsupportedOperationException.class,()->r.points.clear());
		assertThrows(UnsupportedOperationException.class,()->r.gaps.add("mutated"));
	}
	@Test public void unreadAndInvalidValuesBreakDeltaChainsRatherThanBecomingZero()
	{
		var r=NexResourceReview.from(summary(List.of(sample(10,null,null,null,null),sample(11,1000,1,0,25975),
			sample(12,-1,9,4,-1),sample(13,500,0,0,25975),sample(14,1001,0,0,25975)),List.of()));
		assertNull(r.firstEnergy); assertNull(r.lastEnergy); assertEquals(0,r.sampledIncreases); assertEquals(0,r.sampledDecreases);
		assertFalse(r.improvedActiveObserved); assertFalse(r.fixedLightbearerObserved); assertEquals(3,r.gaps.size());
		assertNull(r.points.get(2).energy); assertNull(r.points.get(2).activeCharges);
	}
	@Test public void excludesPreSmokeAndPostEndSamplesAndDoesNotInventStartOrRingContinuity()
	{
		var r=NexResourceReview.from(summary(List.of(sample(9,0,2,1,25975),sample(11,400,1,0,25975),
			sample(19,500,0,0,0),sample(21,1000,2,0,25975)),List.of(sample(0,1000,2,1,25975))));
		assertEquals(2,r.points.size()); assertEquals(Integer.valueOf(400),r.firstEnergy); assertEquals(Integer.valueOf(500),r.lastEnergy);
		assertFalse(r.improvedActiveObserved); assertFalse(r.fixedLightbearerObserved);
		assertEquals(1,r.gaps.size()); assertTrue(r.gaps.iterator().next().contains("later than"));
		var empty=NexResourceReview.from(summary(List.of(),List.of())); assertNull(empty.firstEnergy); assertFalse(empty.fixedLightbearerObserved);
	}
}
