package com.ironpath.nex;

import com.ironpath.encounter.CombatState;
import com.ironpath.encounter.EncounterSummary.Outcome;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NexResourceScenariosTest
{
	private NexSummary.Sample sample(int tick,Integer energy,int ring)
	{ return new NexSummary.Sample(tick,"SMOKE","NONE","NEX","melee",-1,new CombatState(Map.of("ring",ring),Map.of(),Map.of(),Set.of(),0,energy,false)); }
	private NexResourceScenarios scenario(int end,boolean continuous,List<NexSummary.Sample> samples)
	{ return NexResourceReview.from(new NexSummary(1,0,end,end,end,0,Outcome.COMPLETE,"synthetic","private","private",samples,Map.of(),Set.of(),
		new NexSummary.Signals(0,0,end,end+1,continuous,Set.of("SMOKE")))).scenarios; }
	@Test public void phaseSensitivityReusesFiniteTimelineWithoutAttributingObservedChanges()
	{
		var s=scenario(125,true,List.of(sample(0,1000,25975),sample(40,0,25975)));
		assertTrue(s.gaps.isEmpty()); assertEquals(3,s.examples.size());
		var vw=s.examples.get(0); assertEquals(2,vw.minUses); assertEquals(2,vw.maxUses); assertEquals(52,vw.cases);
		var zcb=s.examples.get(1); assertEquals(1,zcb.minUses); assertEquals(1,zcb.maxUses);
		// Later sampled zero does not become spending or an input event in the hypothetical example.
		var unchanged=scenario(125,true,List.of(sample(0,1000,25975)));
		assertEquals(unchanged.examples.get(0).maxUses,vw.maxUses);
		assertThrows(UnsupportedOperationException.class,()->s.examples.clear());
	}
	@Test public void unknownConflictingLateOrUnreadStartCannotBecomeZeroOrFullEnergy()
	{
		for(var samples:List.of(List.of(sample(0,null,25975)),List.of(sample(1,1000,25975)),List.of(sample(0,1000,25975),sample(0,500,25975))))
		{ var s=scenario(100,true,samples); assertTrue(s.examples.isEmpty()); assertNull(s.initialEnergy); assertTrue(s.gaps.contains(NexResourceScenarios.Gap.START_ENERGY)); }
	}
	@Test public void fixedRingContinuousAndDurationBoundsAreRequired()
	{
		assertTrue(scenario(3001,true,List.of(sample(0,1000,25975))).gaps.contains(NexResourceScenarios.Gap.DURATION));
		assertTrue(scenario(100,false,List.of(sample(0,1000,25975))).gaps.contains(NexResourceScenarios.Gap.CAPTURE));
		var s=scenario(100,true,List.of(sample(0,1000,25975),sample(10,1000,0)));
		assertTrue(s.examples.isEmpty()); assertTrue(s.gaps.contains(NexResourceScenarios.Gap.RING));
	}
}
