package com.ironpath.advisor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.StringReader;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.P4aFixtures.TIME;
import static org.junit.Assert.*;

public class AcquisitionEstimatorTest
{
	private AcquisitionModel model;
	private final AcquisitionEstimator estimator=new AcquisitionEstimator();
	@Before public void setup() throws Exception { model=KnowledgeCatalog.load(new Gson()).acquisitionModels.get("next-zenyte-shard"); }
	private AcquisitionModel model(long numerator,long denominator,String support) throws Exception
	{
		JsonObject doc=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/catalog.json"))).getAsJsonObject();
		JsonObject spec=doc.getAsJsonArray("acquisitions").get(0).getAsJsonObject();
		spec.addProperty("numerator",numerator);spec.addProperty("denominator",denominator);spec.addProperty("support",support);
		return KnowledgeCatalog.read(new Gson(),new StringReader(doc.toString())).acquisitionModels.get("next-zenyte-shard");
	}
	@Test public void reviewedNextShardMatchesHandCasesAndNeedsAnExplicitPace()
	{
		EffortEstimate estimate=estimator.estimate(model,true,"",null);
		assertEquals(300,estimate.meanTrials,1e-12);assertEquals(208L,(long)estimate.p50);assertEquals(690L,(long)estimate.p90);assertEquals(32L,(long)estimate.p10);
		assertNull(estimate.meanHours());assertEquals(EffortEstimate.Level.A1,estimate.level);
		assertTrue(AcquisitionEstimator.probabilityBy(1.0/300,207)<.5);assertTrue(AcquisitionEstimator.probabilityBy(1.0/300,208)>=.5);
		assertTrue(AcquisitionEstimator.probabilityBy(1.0/300,689)<.9);assertTrue(AcquisitionEstimator.probabilityBy(1.0/300,690)>=.9);
		PaceScenario pace=new PaceScenario(model.activity,model.mode,40,40,"Full eligible cycle, including banking/failures",TIME);
		estimate=estimator.estimate(model,true,"",pace);assertEquals(7.5,estimate.meanHours(),1e-12);assertEquals(5.2,estimate.medianHours(),1e-12);assertEquals(17.25,estimate.p90Hours(),1e-12);
		assertEquals(EffortEstimate.Level.A2,estimate.level);assertTrue(estimate.exclusions.stream().anyMatch(s->s.contains("onyx")));
	}
	@Test public void invalidMechanicsAndCertainOrImpossibleEventsNeverInventZeroHours() throws Exception
	{
		assertNull(estimator.estimate(model(0,300,"REVIEWED"),true,"",null).meanTrials);
		assertEquals(1,estimator.estimate(model(1,1,"REVIEWED"),true,"",null).meanTrials,0);
		assertEquals(1L,(long)estimator.estimate(model(1,1,"REVIEWED"),true,"",null).p90);
		assertNull(estimator.estimate(model(1,300,"INVALID"),true,"",new PaceScenario(model.activity,model.mode,40,40,"scenario",TIME)).meanHours());
		assertThrows(IllegalArgumentException.class,()->model(301,300,"REVIEWED"));assertThrows(IllegalArgumentException.class,()->model(1,0,"REVIEWED"));
		for(double p:new double[]{0,-1,Double.NaN,Double.POSITIVE_INFINITY}) { assertThrows(IllegalArgumentException.class,()->new PaceScenario(model.activity,model.mode,p,p,"scenario",TIME)); }
	}
	@Test public void ownedStageAndMismatchedPaceCannotBecomeAnEstimate()
	{
		assertNull(estimator.estimate(model,false,"Owned intermediate bypasses drop",null).meanTrials);
		assertNull(estimator.estimate(model,true,"",new PaceScenario("toa","ordinary",40,40,"scenario",TIME)).meanHours());
		PaceScenario range=new PaceScenario(model.activity,model.mode,30,50,"cycle scenario",TIME);
		EffortEstimate e=estimator.estimate(model,true,"",range);assertEquals(10,e.meanHours(),0);assertEquals(6,e.meanTrials/range.maximumPerHour,0);
	}
	@Test public void fixedRateDryHistoryDoesNotChangeFutureExpectation()
	{
		double p=1.0/300;
		for(long dry:new long[]{0,20,300,1000})
		{
			double conditional=(AcquisitionEstimator.probabilityBy(p,dry+208)-AcquisitionEstimator.probabilityBy(p,dry))/(1-AcquisitionEstimator.probabilityBy(p,dry));
			assertEquals(AcquisitionEstimator.probabilityBy(p,208),conditional,1e-12);
			assertEquals(300,estimator.estimate(model,true,"",null).meanTrials,0);
		}
	}
}
