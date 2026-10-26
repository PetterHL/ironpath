package com.ironpath.advisor;

import org.junit.Test;
import static org.junit.Assert.*;

public class EncounterImpactTest
{
	@Test public void theoreticalExamplesAndRoleWeighting()
	{
		var gain=EncounterImpact.weighted(1.0,0.10,true,true);
		var estimate=EncounterImpact.estimate(0.30,gain,240.0,0.0);
		assertEquals(0.3203883495,estimate.contribution,1e-10);
		assertEquals(6.99029126,estimate.secondsSaved,1e-8);
		var weighted=EncounterImpact.weighted(0.60,0.10,true,true);
		assertEquals(0.06,weighted.fraction,1e-12);
		assertEquals(0.3123772102,EncounterImpact.estimate(0.30,weighted,240.0,0.0).contribution,1e-10);
		var mock=EncounterImpact.estimate(0.30,EncounterImpact.weighted(1.0,0.04,true,true),240.0,0.0);
		assertEquals(0.3083003953,mock.contribution,1e-10); assertEquals(2.84584980,mock.secondsSaved,1e-8);
	}
	@Test public void zeroNegativeSoloAndFixedWaits()
	{
		var zero=EncounterImpact.estimate(0.30,EncounterImpact.weighted(1.0,0.0,true,true),240.0,0.0);
		assertEquals(0.30,zero.contribution,0); assertEquals(0,zero.secondsSaved,0);
		var loss=EncounterImpact.estimate(0.30,EncounterImpact.weighted(1.0,-0.10,true,true),240.0,0.0);
		assertTrue(loss.contribution<0.30); assertTrue(loss.secondsSaved<0); assertTrue(loss.newDuration>240);
		var solo=EncounterImpact.estimate(1.0,EncounterImpact.weighted(1.0,0.10,true,true),240.0,0.0);
		assertEquals(1.0,solo.contribution,0); assertEquals(240/1.1,solo.newDuration,1e-12);
		var fixed=EncounterImpact.estimate(0.30,EncounterImpact.weighted(1.0,0.10,true,true),240.0,120.0);
		assertEquals(6.99029126/2,fixed.secondsSaved,1e-8);
	}
	@Test public void missingOrInvalidInputsBlockOnlyDependentResults()
	{
		var gain=EncounterImpact.weighted(1.0,0.10,true,true);
		assertNull(EncounterImpact.weighted(0.60,0.10,false,true));
		assertNull(EncounterImpact.weighted(0.60,0.10,true,false));
		assertNull(EncounterImpact.weightedRates(0.60,10,12.5,false));
		// Equal damage per attack with 5 -> 4 ticks is +25% role throughput, weighted by 60%.
		assertEquals(0.15,EncounterImpact.weightedRates(0.60,50/5.0,50/4.0,true).fraction,1e-12);
		for(Double invalid:new Double[]{null,Double.NaN,Double.POSITIVE_INFINITY,-0.01,1.01})
		{
			assertNull(EncounterImpact.weighted(invalid,0.10,true,true));
			assertNull(EncounterImpact.estimate(invalid,gain,240.0,0.0).contribution);
		}
		assertNull(EncounterImpact.estimate(0.30,null,240.0,0.0).secondsSaved);
		for(Double invalid:new Double[]{null,Double.NaN,Double.POSITIVE_INFINITY,-1.0,0.0})
		{
			var estimate=EncounterImpact.estimate(0.30,gain,invalid,0.0);
			assertNotNull(estimate.contribution); assertNull(estimate.secondsSaved);
		}
		assertNull(EncounterImpact.estimate(1.0,EncounterImpact.weighted(1.0,-1.0,true,true),240.0,0.0).contribution);
		assertNull(EncounterImpact.estimate(0.30,gain,240.0,241.0).secondsSaved);
		assertNull(EncounterImpact.weighted(1.0,-1.01,true,true));
	}
}
