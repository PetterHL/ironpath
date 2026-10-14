package com.ironpath.advisor;

import com.google.gson.*;
import com.ironpath.model.*;
import com.ironpath.preferences.Preferences;
import java.io.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;
import static com.ironpath.advisor.P4aFixtures.*;
import static com.ironpath.advisor.R15Fixtures.*;
import static com.ironpath.advisor.EP1Fixtures.*;

/** Original synthetic accounts and independent finite-event arithmetic, never player evidence. */
public class AD1bAcquisitionTest
{
	private KnowledgeCatalog catalog; private Advisor advisor; private Preferences preferences;
	@Before public void load() throws Exception
	{ catalog=KnowledgeCatalog.load(new Gson());advisor=new Advisor(catalog);preferences=absent(catalog); }
	private Advisor.UpgradeView view(Advisor a,AccountSnapshot account,String id,PaceScenario pace)
	{ return a.upgrades(account,preferences,new PoweredMagic(catalog).propose(account),pace).stream().filter(v->v.upgrade.id.equals(id)).findFirst().orElseThrow(); }
	private Advisor.UpgradeView view(AccountSnapshot a,String id) { return view(advisor,a,id,null); }
	private AccountSnapshot without(AccountSnapshot a,int... ids)
	{
		Set<Integer> bank=new HashSet<>(a.bank.value());for(int id:ids) { bank.remove(id); }
		return new AccountSnapshot(a.levels,a.inventory,a.equipment,fact(bank),a.rigour,a.augury,a.facts,
			a.equipmentSlots,a.inventoryQuantities,a.equipmentQuantities,a.bankQuantities);
	}
	private JsonObject document() throws Exception
	{ try(Reader in=new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/catalog.json"))){return new JsonParser().parse(in).getAsJsonObject();} }
	private Advisor changed(JsonObject doc) throws Exception { return new Advisor(KnowledgeCatalog.read(new Gson(),new StringReader(doc.toString()))); }
	@Test public void automaticHoursWithoutPersonalHistoryAndMatchingManualClearRestoresDefault()
	{
		AccountSnapshot account=ready();Advisor.UpgradeView auto=view(account,"tormented-bracelet");
		assertEquals(300.0/70,auto.presentation.maximumMeanHours,1e-12);
		assertEquals(PaceScenario.SourceKind.MODEL_BASED,auto.presentation.estimateSourceKind);
		assertEquals(EffortEstimate.Level.A2,auto.pursuit.estimate.level);assertNull(auto.pursuit.estimate.pace.entered);
		PaceScenario manual=new PaceScenario("demonic-gorillas","ordinary",30,50,"Full eligible repeat-hour scenario",TIME);
		Advisor.UpgradeView overridden=view(advisor,account,"tormented-bracelet",manual);
		assertEquals(6,overridden.presentation.minimumMeanHours,0);assertEquals(10,overridden.presentation.maximumMeanHours,0);
		assertEquals(PaceScenario.SourceKind.USER_SCENARIO,overridden.presentation.estimateSourceKind);
		assertTrue(new UpgradeCard(overridden).effort.startsWith("Manual pace scenario"));
		assertEquals(auto.presentation.maximumMeanHours,view(advisor,account,"tormented-bracelet",null).presentation.maximumMeanHours);
		PaceScenario other=new PaceScenario("zulrah","ordinary",100,100,"Different activity",TIME);
		assertEquals(auto.presentation.maximumMeanHours,view(advisor,account,"tormented-bracelet",other).presentation.maximumMeanHours);
		assertEquals(auto.pursuit.priority,overridden.pursuit.priority);
	}
	@Test public void independentZulrahRollsUseCompletionProbabilityNotExpectedQuantity()
	{
		AcquisitionModel m=catalog.acquisitionModels.get("next-tanzanite-fang");
		// All 1024 x 1024 pairs except both failures; includes the pair with two fangs once.
		double p=2047.0/1048576;assertEquals(p,AcquisitionEstimator.successProbability(m),1e-15);
		EffortEstimate estimate=new AcquisitionEstimator().estimate(m,true,"",null);
		assertEquals(1048576.0/2047,estimate.meanTrials,1e-10);assertNotEquals(512,estimate.meanTrials,1e-5);
		Advisor.UpgradeView automatic=view(ready(),"toxic-blowpipe");
		assertNotNull(automatic.presentation.maximumMeanHours);
		assertEquals(estimate.meanTrials/20,automatic.presentation.maximumMeanHours,1e-10);
		// Matching manual pace refines the same conditional component distribution.
		PaceScenario manual=new PaceScenario("zulrah","ordinary",20,20,"Eligible repeat-hour scenario",TIME);
		Advisor.UpgradeView blowpipe=view(advisor,ready(),"toxic-blowpipe",manual);
		assertEquals(estimate.meanTrials/20,blowpipe.presentation.maximumMeanHours,1e-10);
		assertNull(view(withFact(ready(),"regicide",fact(false)),"toxic-blowpipe").presentation.maximumMeanHours);
		assertNull(view(items(ready(),12922),"toxic-blowpipe").pursuit.estimate.meanTrials);
	}
	@Test public void repeatedComponentsHaveRealJointQuantilesAndNoSummedMedians()
	{
		assertEquals(.5,AcquisitionEstimator.probabilityBy(.5,3,2),1e-15);
		assertEquals(3,AcquisitionEstimator.quantile(.5,2,.5));assertEquals(7,AcquisitionEstimator.quantile(.5,2,.9));
		assertEquals(57.0/64,AcquisitionEstimator.probabilityBy(.5,6,2),1e-15);
		// Independent enumeration of equiprobable bit sequences checks the distribution without its recurrence.
		for(int n=2;n<=10;n++)
		{
			int eligible=0;for(int bits=0;bits<(1<<n);bits++) { if(Integer.bitCount(bits)>=2) { eligible++; } }
			assertEquals((double)eligible/(1<<n),AcquisitionEstimator.probabilityBy(.5,n,2),1e-14);
		}
		assertNotEquals(2*AcquisitionEstimator.quantile(.5,.5),AcquisitionEstimator.quantile(.5,2,.5));
		for(int r:new int[]{2,3,5})for(double p:new double[]{.01,.02,.5,1})for(double q:new double[]{.1,.5,.9})
		{
			long n=AcquisitionEstimator.quantile(p,r,q);
			assertTrue(AcquisitionEstimator.probabilityBy(p,n,r)>=q);
			assertTrue(AcquisitionEstimator.probabilityBy(p,n-1,r)<q);
		}
	}
	@Test public void remainingCrystalSeedsUseConservativeQuantitiesAndSharedAllocationIsNotAdded()
	{
		AccountSnapshot account=without(ready(),23975);Advisor.UpgradeView fresh=view(account,"crystal-body");
		assertEquals(150,fresh.pursuit.estimate.meanTrials,0);assertEquals(25,fresh.presentation.maximumMeanHours,0);
		assertTrue(fresh.pursuit.estimate.conditionalWork);assertTrue(new UpgradeCard(fresh).effort.contains("if components needed"));assertTrue(fresh.pursuit.estimate.remainingOutcome.startsWith("Up to 3"));
		Advisor.UpgradeView partial=view(withQuantity(account,23956,2),"crystal-body");
		assertEquals(1,partial.pursuit.estimate.remainingSuccesses);assertEquals(50,partial.pursuit.estimate.meanTrials,0);
		assertEquals(50.0/6,partial.presentation.maximumMeanHours,1e-10);
		assertNull(view(withQuantity(account,23956,3),"crystal-body").pursuit.estimate.meanHours());
		AccountSnapshot shared=withQuantity(without(ready(),23975,23979),23956,1);
		assertEquals(2,view(shared,"crystal-body").pursuit.estimate.remainingSuccesses);
		assertEquals(1,view(shared,"crystal-legs").pursuit.estimate.remainingSuccesses);
		assertTrue(view(shared,"crystal-body").pursuit.estimate.exclusions.stream().anyMatch(e->e.contains("cannot fund multiple")));
		AccountSnapshot generated=items(without(ready(),25867),9185);
		assertEquals(400.0/6,view(generated,"bowfa").presentation.maximumMeanHours,1e-10);
	}
	@Test public void muspahShardRemainderAndMethodAreSeparateFromOwnedBowfaAdequacy()
	{
		assertNull(view(ready(),"venator-bow").presentation.maximumMeanHours);
		AccountSnapshot tbow=items(ready(),20997);Advisor.UpgradeView fresh=view(tbow,"venator-bow");
		assertEquals(500,fresh.pursuit.estimate.meanTrials,0);assertEquals(20,fresh.presentation.maximumMeanHours,0);
		Advisor.UpgradeView partial=view(withQuantity(tbow,27614,3),"venator-bow");
		assertEquals(200,partial.pursuit.estimate.meanTrials,0);assertEquals(8,partial.presentation.maximumMeanHours,0);
		assertNull(view(withQuantity(tbow,27614,5),"venator-bow").pursuit.estimate.meanTrials);
		assertNull(view(level(tbow,"RANGED",80),"venator-bow").presentation.maximumMeanHours);
	}
	@Test public void unknownInputsAccessAndOwnedIntermediatesNeverBecomeZeroOrFreshFullRouteWork()
	{
		for(Advisor.UpgradeView v:advisor.upgrades(unread(ready()),Preferences.EMPTY))
		{ assertNull(v.presentation.maximumMeanHours); }
		assertNull(view(items(ready(),19496),"tormented-bracelet").pursuit.estimate.meanHours());
		assertNull(view(withFact(ready(),"monkey-madness-ii",Observation.unknown()),"tormented-bracelet").presentation.maximumMeanHours);
		assertNull(view(level(ready(),"PRAYER",43),"tormented-bracelet").presentation.maximumMeanHours);
		assertNotNull(view(ready(),"scorching-bow").presentation.maximumMeanHours);
		assertEquals(500.0/55,view(ready(),"scorching-bow").presentation.maximumMeanHours,1e-12);
		assertTrue(view(ready(),"scorching-bow").presentation.estimateScope.contains("component acquisition only"));
	}
	@Test public void capturedManualMethodDoesNotTransferWhenCompatibleMethodChanges()
	{
		AccountSnapshot tbow=items(ready(),20997);EffortEstimate e=view(tbow,"venator-bow").pursuit.estimate;
		PaceScenario tbowPace=new PaceScenario("muspah","ordinary",40,40,"Full eligible repeat hour",TIME,e.methodKey);
		assertEquals(12.5,view(advisor,tbow,"venator-bow",tbowPace).presentation.maximumMeanHours,0);
		assertNull(view(advisor,ready(),"venator-bow",tbowPace).presentation.maximumMeanHours);
		assertNull(view(advisor,level(tbow,"RANGED",80),"venator-bow",tbowPace).presentation.maximumMeanHours);
	}
	@Test public void sourceWithdrawalRemovesOnlyDependentHoursAndMalformedModelsCannotBePromoted() throws Exception
	{
		JsonObject doc=document();doc.getAsJsonArray("automaticPaces").get(2).getAsJsonObject().addProperty("support","INVALID");
		Advisor withdrawn=changed(doc);Advisor.UpgradeView still=view(withdrawn,ready(),"tormented-bracelet",null);
		assertEquals(300,still.pursuit.estimate.meanTrials,0);assertNull(still.presentation.maximumMeanHours);
		assertEquals(view(ready(),"tormented-bracelet").pursuit.remainingEffort.band,still.pursuit.remainingEffort.band);
		assertNotNull(view(withdrawn,items(without(ready(),25867),9185),"bowfa",null).presentation.maximumMeanHours);
		for(String field:List.of("sources","when"))
		{
			JsonObject malformed=document();malformed.getAsJsonArray("automaticPaces").get(0).getAsJsonObject().remove(field);
			assertThrows(IllegalArgumentException.class,()->changed(malformed));
		}
		JsonObject malformed=document();malformed.getAsJsonArray("acquisitions").get(3).getAsJsonObject().addProperty("rolls",2);
		assertThrows(IllegalArgumentException.class,()->changed(malformed));
	}
}
