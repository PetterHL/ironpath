package com.ironpath.advisor;

import com.google.gson.*;
import com.ironpath.model.*;
import com.ironpath.preferences.Preferences;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.*;
import static org.junit.Assert.*;
import static com.ironpath.advisor.P4aFixtures.*;
import static com.ironpath.advisor.R15Fixtures.*;
import static com.ironpath.advisor.EP1Fixtures.*;
import static com.ironpath.advisor.RemainingEffort.*;

public class EP1ProgressionTest
{
	private KnowledgeCatalog catalog; private Advisor advisor; private Preferences absent;
	@Before public void load() throws Exception { catalog=KnowledgeCatalog.load(new Gson());advisor=new Advisor(catalog);absent=absent(catalog); }
	private Advisor.UpgradeView view(AccountSnapshot a,String id)
	{ return advisor.upgrades(a,absent).stream().filter(v->v.upgrade.id.equals(id)).findFirst().orElseThrow(); }
	private JsonObject document() throws Exception
	{ try(Reader in=new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/catalog.json"))){return new JsonParser().parse(in).getAsJsonObject();} }
	@Test public void nearerSubstantialChoicesPrecedeVeryLongAndPreviewIsAnOrderedSubsequence()
	{
		List<Advisor.UpgradeView> all=advisor.upgrades(ready(),absent); List<String> ids=all.stream().map(v->v.upgrade.id).collect(Collectors.toList());
		assertTrue(ids.toString(),ids.indexOf("toxic-blowpipe")<ids.indexOf("tumekens-shadow"));
		assertTrue(ids.toString(),ids.indexOf("scorching-bow")<ids.indexOf("tumekens-shadow"));
		PursuitOverview overview=new PursuitOverview(all,List.of(),List.of(),List.of());int previous=-1;Set<String> families=new HashSet<>();
		for(Advisor.UpgradeView v:overview.preview){int next=ids.indexOf(v.upgrade.id);assertTrue(next>previous);assertTrue(families.add(v.pursuit.family));previous=next;}
		assertFalse(overview.longTerm.isEmpty());assertEquals(all,overview.all);
		assertFalse(all.stream().anyMatch(v->v.pursuit.reasonCodes.contains("IMPORTANCE_BEFORE_BURDEN")));
	}
	@Test public void doomPackageChangesSuitabilityWithoutCertifyingSkillOrCreatingPremiumAccess()
	{
		Advisor.UpgradeView strong=view(items(ready(),29591,6746),"avernic-treads");
		Advisor.UpgradeView weaker=view(items(level(ready(),"RANGED",80),29591,6746),"avernic-treads");
		assertEquals(Suitability.SUITABLE,strong.pursuit.remainingEffort.suitability);assertEquals(Suitability.WORKABLE,weaker.pursuit.remainingEffort.suitability);
		assertEquals(Band.LONG,strong.pursuit.remainingEffort.band);assertEquals(Band.VERY_LONG,weaker.pursuit.remainingEffort.band);
		assertNull(strong.presentation.maximumMeanHours);assertTrue(strong.pursuit.remainingEffort.reason.contains("no delve success"));
		Advisor.UpgradeView early=view(level(items(ready(),29591,6746),"MAGIC",60),"avernic-treads");
		assertEquals(Suitability.PREPARE,early.pursuit.remainingEffort.suitability);assertEquals(Band.UNKNOWN,early.pursuit.remainingEffort.band);
		assertNull(early.presentation.minimumMeanHours);assertFalse(early.pursuit.blockers.stream().anyMatch(s->s.contains("Scythe")||s.contains("Zaryte")));
	}
	@Test public void directAndPreparationRoutesKeepTheirOwnWorkAndNeverInventSavings()
	{
		Advisor.UpgradeView treads=view(items(ready(),20997,6746),"avernic-treads");
		assertEquals(2,treads.presentation.routes.size());UpgradePresentation.RouteChoice detour=treads.presentation.routes.get(1);
		assertEquals("scorching-bow",detour.preparationUpgrade);assertFalse(detour.preparationOwned);assertNull(detour.totalHours);
		assertTrue(detour.reason.contains("Include its own synapse"));
		assertEquals(Band.MEDIUM,detour.preparationBand);
		Advisor.UpgradeView prepared=view(items(ready(),20997,6746,29591),"avernic-treads");assertTrue(prepared.presentation.routes.get(1).preparationOwned);
		assertEquals(Band.SHORT,prepared.presentation.routes.get(1).preparationBand);
		assertEquals(ValueEvidence.Importance.MARGINAL,view(items(ready(),20997,6746),"scorching-bow").valueEvidence.importance);
	}
	@Test public void componentsRemoveOnlyTheirStagesAndRetainExclusiveAllocations()
	{
		AccountSnapshot a=items(ready(),29580,70,12922,19535,1597);
		Advisor.UpgradeView synapse=view(a,"scorching-bow"),pipe=view(a,"toxic-blowpipe"),neck=view(a,"necklace-of-anguish");
		for(Advisor.UpgradeView v:List.of(synapse,pipe,neck)){assertEquals(Band.SHORT,v.pursuit.remainingEffort.band);assertTrue(v.pursuit.remainingEffort.acquisitionBypassed);}
		assertTrue(synapse.pursuit.allocations.contains("tormented-synapse"));assertTrue(neck.pursuit.allocations.contains("zenyte"));
		assertFalse(synapse.pursuit.blockers.stream().anyMatch(s->s.contains("While Guthix")));
		Advisor.UpgradeView horn=view(items(ready(),26372),"zaryte-crossbow");assertEquals(Band.UNKNOWN,horn.pursuit.remainingEffort.band);
		assertFalse(horn.pursuit.remainingEffort.acquisitionBypassed);
		Advisor.UpgradeView supplied=view(withQuantity(items(ready(),26372,11785),26231,250),"zaryte-crossbow");assertEquals(Band.SHORT,supplied.pursuit.remainingEffort.band);
	}
	@Test public void allContractsAndSecondaryActivitiesUseProgressionTwoAndPromotionsFailWithoutEvidence() throws Exception
	{
		assertTrue(catalog.decisions.stream().filter(d->d.status.equals("reviewed")).allMatch(d->d.pursuit!=null && catalog.effortProfiles.containsKey(d.pursuit.profile)));
		List<Advisor.UpgradeView> views=advisor.upgrades(ready(),absent);
		assertTrue(views.stream().allMatch(v->v.routeAssessments.size()==v.routes.size() && v.routeAssessments.stream().allMatch(a->a.version.equals("progression-2"))));
		assertTrue(advisor.availableActivities(ready(),absent).stream().allMatch(r->r.remainingEffort!=null && r.priority!=null));
		JsonObject d=document();d.getAsJsonArray("decisions").get(0).getAsJsonObject().remove("pursuit");
		assertThrows(IllegalArgumentException.class,()->KnowledgeCatalog.read(new Gson(),new StringReader(d.toString())));
		JsonObject missing=document();missing.getAsJsonArray("activityRewards").get(0).getAsJsonObject().remove("profile");
		assertThrows(IllegalArgumentException.class,()->KnowledgeCatalog.read(new Gson(),new StringReader(missing.toString())));
	}
	@Test public void longGrindMayLeadNearlyCompleteAccountsAndUnknownGearNeverSelectsWeakestProfile()
	{
		AccountSnapshot a=profiles(catalog).get("nearly-complete");List<Advisor.UpgradeView> all=advisor.upgrades(a,absent);
		assertEquals(1,all.size());assertEquals("tumekens-shadow",all.get(0).upgrade.id);assertEquals(Band.VERY_LONG,all.get(0).pursuit.remainingEffort.band);
		assertEquals("tumekens-shadow",new PursuitOverview(all,List.of(),List.of(),List.of()).preview.get(0).upgrade.id);
		for(Advisor.UpgradeView v:advisor.upgrades(unread(ready()),absent))
		{if(!v.pursuit.remainingEffort.acquisitionBypassed){assertEquals(Suitability.UNKNOWN,v.pursuit.remainingEffort.suitability);assertNull(v.presentation.maximumMeanHours);}}
	}
	@Test public void optionalManualPaceHasHonestScopeAndClearReturnsUnavailableWithoutChangingBands()
	{
		AccountSnapshot a=ready();PaceScenario pace=new PaceScenario("demonic-gorillas","ordinary",40,60,"Eligible kills including cycle overhead",TIME);
		Advisor.UpgradeView manual=advisor.upgrades(a,absent,new PoweredMagic(catalog).propose(a),pace).stream().filter(v->v.upgrade.id.equals("tormented-bracelet")).findFirst().orElseThrow();
		assertEquals(UpgradePresentation.RangeKind.SCENARIO_MEAN_SENSITIVITY,manual.presentation.rangeKind);assertEquals(5,manual.presentation.minimumMeanHours,1e-10);
		assertEquals(7.5,manual.presentation.maximumMeanHours,1e-10);assertTrue(manual.presentation.estimateScope.contains("Next zenyte shard"));
		Advisor.UpgradeView cleared=view(a,"tormented-bracelet");assertNull(cleared.presentation.maximumMeanHours);assertEquals(manual.pursuit.remainingEffort.band,cleared.pursuit.remainingEffort.band);
	}
	@Test public void unavailableProbabilityWithdrawsOnlyItsDependentEstimate() throws Exception
	{
		Advisor.UpgradeView before=view(ready(),"tormented-bracelet");JsonObject document=document();
		document.getAsJsonArray("acquisitions").get(0).getAsJsonObject().addProperty("support","INVALID");
		Advisor changed=new Advisor(KnowledgeCatalog.read(new Gson(),new StringReader(document.toString())));
		Advisor.UpgradeView after=changed.upgrades(ready(),absent).stream().filter(v->v.upgrade.id.equals("tormented-bracelet")).findFirst().orElseThrow();
		assertEquals(before.pursuit.remainingEffort.band,after.pursuit.remainingEffort.band);assertEquals(before.pursuit.priority,after.pursuit.priority);
		assertNull(after.pursuit.estimate.meanTrials);assertNull(after.presentation.maximumMeanHours);assertTrue(after.comparison.dpsGain()>0);
	}

	@Test public void unmodelledOwnedFamilyCannotManufactureStrongestOwnedDpsAdvice()
	{
		Advisor.UpgradeView pipe=view(items(ready(),29591),"toxic-blowpipe");
		assertFalse(pipe.comparison.available(ComparisonResult.Metric.THEORETICAL_DPS));assertTrue(pipe.comparison.available(ComparisonResult.Metric.MAX_HIT));
		assertTrue(pipe.comparison.gaps.get(ComparisonResult.Metric.THEORETICAL_DPS).contains("Scorching bow"));
		assertEquals(UpgradePresentation.Availability.MODEL_UNSUPPORTED,pipe.presentation.primaryMetric.availability);
		assertNotEquals(ValueEvidence.Importance.KEY,pipe.valueEvidence.importance);
	}

	@Test public void queuesAndCompleteCoverageEvidence() throws Exception
	{
		if(!Boolean.getBoolean("ironpath.ep1Evidence")){return;}
		Path root=Path.of("build/validation/ep1");Files.createDirectories(root);List<String> rows=new ArrayList<>();
		for(Map.Entry<String,AccountSnapshot> entry:profiles(catalog).entrySet())
		{
			rows.add("## "+entry.getKey());
			for(Advisor.UpgradeView v:advisor.upgrades(entry.getValue(),absent))
			{rows.add(v.upgrade.id+" | "+v.valueEvidence.importance+" | "+v.pursuit.priority+" | "+v.pursuit.category+" | "+v.pursuit.remainingEffort.band+" | "+v.pursuit.remainingEffort.suitability+" | "+v.presentation.estimateScope);}
		}
		Files.write(root.resolve("queues.txt"),rows);
	}
}
