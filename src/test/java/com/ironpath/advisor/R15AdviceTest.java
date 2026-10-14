package com.ironpath.advisor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.preferences.Preferences;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.P4aFixtures.*;
import static com.ironpath.advisor.R15Fixtures.*;
import static com.ironpath.advisor.PursuitAssessment.*;
import static org.junit.Assert.*;

public class R15AdviceTest
{
	private KnowledgeCatalog catalog;
	private Advisor advisor;
	private Preferences missing;
	// Preserve the originally selected high-defence context for these R1/R1.5 regressions.
	private List<Advisor.UpgradeView> reviewedUpgrades(Advisor evaluator, AccountSnapshot a, Preferences p)
	{ return evaluator.upgrades(a, p, new PoweredMagic(catalog).propose(a, "graardor")).stream()
		.filter(v -> !v.upgrade.comparison.equals("RANGED") && !v.upgrade.id.equals("venator-bow")).collect(java.util.stream.Collectors.toList()); }
	@Before public void setup() throws Exception { catalog=KnowledgeCatalog.load(new Gson());advisor=new Advisor(catalog);missing=absent(catalog); }
	private Advisor.UpgradeView view(AccountSnapshot a, Preferences p, String id)
	{ return reviewedUpgrades(advisor, a,p).stream().filter(v->v.upgrade.id.equals(id)).findFirst().orElseThrow(); }
	private ActivityOpportunity cox(AccountSnapshot a, Preferences p)
	{ return advisor.activityOpportunities(a,p,reviewedUpgrades(advisor, a,p)).stream().filter(v->v.activity.equals("chambers-of-xeric")).findFirst().orElseThrow(); }
	@Test public void postBowfaHasNearerPursuitsWithoutPromotingShadowFromLongTerm()
	{
		AccountSnapshot a=developed(false);List<Advisor.UpgradeView> views=reviewedUpgrades(advisor, a,missing);
		assertEquals(Category.PURSUE,view(a,missing,"tormented-bracelet").pursuit.category);
		assertEquals(Category.LONG_TERM,view(a,missing,"tumekens-shadow").pursuit.category);
		assertTrue(views.indexOf(viewById(views,"tormented-bracelet"))<views.indexOf(viewById(views,"tumekens-shadow")));
		assertEquals(Category.LONG_TERM,view(a,missing.goal("magic-upgrade"),"tumekens-shadow").pursuit.category);
		PursuitOverview overview=new PursuitOverview(views,advisor.activityOpportunities(a,missing,views),List.of(),List.of());
		assertTrue(overview.pursuits.size()<=2);assertTrue(overview.longTerm.stream().anyMatch(v->v.upgrade.id.equals("tumekens-shadow")));
	}
	private Advisor.UpgradeView viewById(List<Advisor.UpgradeView> views,String id) { return views.stream().filter(v->v.upgrade.id.equals(id)).findFirst().orElseThrow(); }
	@Test public void sixRequiredProfilesAndCounterfactualsRemainAccountSpecific() throws Exception
	{
		AccountSnapshot base=developed(false);
		AccountSnapshot tbow=items(base,20997);
		AccountSnapshot shadow=developed(true);
		AccountSnapshot many=items(developed(true),12002,19544);
		AccountSnapshot onlyRare=items(base,21018,21021,21024,12002,19544,28313,26374,31097,21034,21079);
		AccountSnapshot obtainable=withFact(withQuantity(items(base,11785,26372),26231,250),"nex-entry",fact(false));
		List<AccountSnapshot> profiles=List.of(base,tbow,shadow,many,onlyRare,obtainable);
		StringBuilder report=new StringBuilder("# R1.5a synthetic account comparisons\n\nOriginal synthetic facts; no goals or player-usefulness evidence. Knowledge r1.5a-1, ranking pursuit-1, combat powered-magic-2, next-shard zenyte-fixed-1. Explicit absent planning confirmations are fixture inputs, not a required runtime questionnaire.\n\n");
		int number=0;
		for(AccountSnapshot a:profiles)
		{
			List<Advisor.UpgradeView> views=reviewedUpgrades(advisor, a,missing);
			assertEquals(views.stream().map(v->v.upgrade.id+v.pursuit.category+v.pursuit.reasonCodes).collect(java.util.stream.Collectors.toList()),reviewedUpgrades(advisor, a,missing).stream().map(v->v.upgrade.id+v.pursuit.category+v.pursuit.reasonCodes).collect(java.util.stream.Collectors.toList()));
			if(a==shadow) { assertFalse(views.stream().anyMatch(v->v.upgrade.id.equals("swamp-trident")||v.upgrade.id.equals("tumekens-shadow"))); }
			report.append("## ").append(++number).append(". ").append(List.of("Post-Bowfa","Tbow, no Shadow","Shadow owner","Several missing CoX families","Only rare R1 magic reward remains","Several nearer opportunities / owned horn").get(number-1)).append("\n\n");
			report.append("Observed synthetic facts: ").append(a.facts.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).map(e->e.getKey()+"="+(e.getValue().known()?e.getValue().value():"unknown")).collect(java.util.stream.Collectors.joining(", "))).append(". Rigour/Augury false; unread fields remain unknown. Confirmed-absent ownership keys: ").append(new java.util.TreeSet<>(missing.confirmations.keySet())).append(". Quantities: ").append(a.bankQuantities.known()?new java.util.TreeMap<>(a.bankQuantities.value()):"unknown").append(".\n\n");
			report.append("Synthetic observed bank: ").append(new java.util.TreeSet<>(a.bank.value())).append(". Real levels ").append(new java.util.TreeMap<>(a.levels.value())).append(".\n\n");
			for(Advisor.UpgradeView v:views)
			{
				if(v.comparison.beforeSetup!=null) { report.append("Reference slots: ").append(new java.util.TreeMap<>(v.comparison.beforeSetup.slots)).append("; target ").append(v.comparison.target.id).append("; prayer ").append(v.comparison.beforeSetup.prayer).append("; boost ").append(v.comparison.beforeSetup.boost).append("; style ").append(v.comparison.beforeSetup.style).append(". All zero slots are labelled template exclusions, never inferred account absence.\n\n"); }
				report.append("- **").append(v.upgrade.title).append("** — ").append(v.pursuit.category.label).append("; ").append(v.pursuit.acquisitionMode).append(" / ").append(v.pursuit.burden).append(". ").append(v.pursuit.reasons.get(0)).append("\n  ").append(v.comparison.summary()).append("\n  Next check: ").append(v.pursuit.blockers).append("\n  Codes: ").append(v.pursuit.reasonCodes).append("\n  Alternative: ").append(v.pursuit.alternative).append("; route ").append(v.pursuit.route).append(". Exact source IDs/revisions: see this outcome and its route in the [bundled register](../../src/main/resources/com/ironpath/catalog.json).\n");
			}
			report.append("\nShared activities:\n\n");
			for(ActivityOpportunity activity:advisor.activityOpportunities(a,missing,views)) { report.append("- ").append(activity.title).append(": ").append(activity.category).append("; families ").append(activity.usefulFamilies).append(". ").append(activity.reason).append("\n"); }
			report.append("\n");
		}
		assertTrue(cox(many,missing).category.equals("Worth doing"));
		assertEquals(Category.PURSUE,view(obtainable,missing,"zaryte-crossbow").pursuit.category);
		assertEquals(Mode.BOUNDED,view(obtainable,missing,"zaryte-crossbow").pursuit.acquisitionMode);
		assertEquals(Category.LONG_TERM,view(onlyRare,missing,"tumekens-shadow").pursuit.category);
		assertTrue(new PursuitOverview(reviewedUpgrades(advisor, onlyRare,missing),List.of(),List.of(),List.of()).pursuits.isEmpty());
		java.nio.file.Path file=java.nio.file.Path.of("build/reports/r15/scenarios.md");java.nio.file.Files.createDirectories(file.getParent());java.nio.file.Files.writeString(file,report);
	}
	@Test public void sharedCoxFamiliesShrinkWithOwnershipAndNeverCountUnknownUnlocks()
	{
		AccountSnapshot a=developed(false);ActivityOpportunity shared=cox(a,missing);
		assertEquals(Set.of("ancestral","augury","rigour"),Set.copyOf(shared.usefulFamilies));
		assertEquals("Worth doing",shared.category);
		AccountSnapshot scrolls=items(a,21034,21079);shared=cox(scrolls,missing);
		assertEquals(List.of("ancestral"),shared.usefulFamilies);assertEquals("Long-term activity",shared.category);
		AccountSnapshot unknown=new AccountSnapshot(a.levels,a.inventory,a.equipment,a.bank,Observation.unknown(),Observation.unknown(),a.facts,a.equipmentSlots,a.inventoryQuantities,a.equipmentQuantities,a.bankQuantities);
		assertEquals(List.of("ancestral"),cox(unknown,missing).usefulFamilies);
		assertEquals(2,cox(unknown,missing).uncertainRewards.size());
		assertFalse(cox(unknown,missing).category.equals("Worth doing"));
		assertFalse(advisor.activityOpportunities(a,missing.skip("chambers-of-xeric",true),reviewedUpgrades(advisor, a,missing.skip("chambers-of-xeric",true))).stream().anyMatch(o->o.activity.equals("chambers-of-xeric")));
	}
	@Test public void oneOrdinaryRewardDoesNotBecomeLongTailJustBecauseItIsNotShared()
	{
		AccountSnapshot a=developed(false);
		ActivityOpportunity gorillas=advisor.activityOpportunities(a,missing,reviewedUpgrades(advisor, a,missing)).stream().filter(o->o.activity.equals("demonic-gorillas")).findFirst().orElseThrow();
		assertEquals("Worth doing",gorillas.category);assertEquals(List.of("tormented-bracelet"),gorillas.usefulFamilies);
	}
	@Test public void ownedShardOrIntermediateRemovesOnlyItsDropStageAndStillChecksUnknownWork()
	{
		AccountSnapshot a=items(developed(false),19529);
		assertNull(view(a,missing,"tormented-bracelet").pursuit.estimate.meanTrials);
		assertEquals(Mode.BOUNDED,view(a,missing,"tormented-bracelet").pursuit.acquisitionMode);
		AccountSnapshot uncut=withFact(items(developed(false),19496),"monkey-madness-ii",fact(false));
		assertNull(view(uncut,missing,"tormented-bracelet").pursuit.estimate.meanTrials);
		assertEquals(Category.PURSUE,view(uncut,missing,"tormented-bracelet").pursuit.category);
		AccountSnapshot noCounts=new AccountSnapshot(a.levels,a.inventory,a.equipment,a.bank,a.rigour,a.augury,a.facts);
		assertEquals(Category.CHECK,view(noCounts,missing,"tormented-bracelet").pursuit.category);
		assertNull(view(noCounts,missing,"tormented-bracelet").pursuit.estimate.meanHours());
	}
	@Test public void acquisitionOutputsAreRemainingWorkAndOwnedPartsExposeARealReadyAssembly()
	{
		AccountSnapshot original=developed(false);Set<Integer> owned=new java.util.HashSet<>(original.bank.value());owned.remove(12899);owned.addAll(Set.of(11907,1755));
		AccountSnapshot a=new AccountSnapshot(original.levels,original.inventory,original.equipment,fact(owned),original.rigour,original.augury,original.facts,original.equipmentSlots,original.inventoryQuantities,original.equipmentQuantities,original.bankQuantities);
		assertEquals(Category.PURSUE,view(a,missing,"swamp-trident").pursuit.category);
		assertEquals(Mode.REPEATABLE,view(a,missing,"swamp-trident").pursuit.acquisitionMode);
		var assembly=view(items(a,12932),missing,"swamp-trident");assertEquals(Category.PURSUE,assembly.pursuit.category);assertEquals(Mode.READY,assembly.pursuit.acquisitionMode);
	}
	@Test public void ownedHornNeverInstructsAnotherHornAndDoesNotAssumeShardQuantity()
	{
		AccountSnapshot a=withFact(items(developed(false),26372,11785),"frozen-door",fact(false));
		var v=view(a,missing,"zaryte-crossbow");assertEquals(Mode.BOUNDED,v.pursuit.acquisitionMode);assertEquals(Category.CHECK,v.pursuit.category);
		assertFalse(v.pursuit.blockers.stream().anyMatch(b->b.contains("Nex")||b.contains("Frozen Door")));
		var hornOnly=view(withQuantity(items(developed(false),26372),26231,250),missing,"zaryte-crossbow");
		assertEquals(Category.CHECK,hornOnly.pursuit.category);assertEquals(Mode.UNKNOWN,hornOnly.pursuit.acquisitionMode);
		a=withQuantity(a,26231,250);v=view(a,missing,"zaryte-crossbow");assertEquals(Category.PURSUE,v.pursuit.category);
	}
	@Test public void ownedComponentsCannotVoteForFreshActivityRewards()
	{
		AccountSnapshot a=items(developed(false),19529,26372,11785,28291);
		List<ActivityOpportunity> activities=advisor.activityOpportunities(a,missing,reviewedUpgrades(advisor, a,missing));
		assertTrue(activities.stream().noneMatch(o->o.activity.equals("demonic-gorillas")||o.activity.equals("nex")||o.activity.equals("duke-sucellus")));
		AccountSnapshot vestige=items(developed(false),28281);
		var ring=view(vestige,missing,"magus-ring");assertEquals(Mode.BOUNDED,ring.pursuit.acquisitionMode);assertEquals(Category.CHECK,ring.pursuit.category);
		Set<Integer> noRing=new java.util.HashSet<>(vestige.bank.value());noRing.remove(6731);
		AccountSnapshot onlyVestige=new AccountSnapshot(vestige.levels,vestige.inventory,vestige.equipment,fact(noRing),vestige.rigour,vestige.augury,vestige.facts,vestige.equipmentSlots,vestige.inventoryQuantities,vestige.equipmentQuantities,vestige.bankQuantities);
		assertEquals(Mode.UNKNOWN,view(onlyVestige,missing,"magus-ring").pursuit.acquisitionMode);
	}
	@Test public void unknownOwnershipBlockedAccessAndInvalidProbabilityPreserveValueWithoutUrgency() throws Exception
	{
		AccountSnapshot a=developed(false);
		assertEquals(Category.CHECK,view(a,Preferences.EMPTY,"tumekens-shadow").pursuit.category);
		assertEquals(Category.PLAN,view(withFact(a,"beneath-cursed-sands",fact(false)),missing,"tumekens-shadow").pursuit.category);
		JsonObject doc=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/catalog.json"))).getAsJsonObject();doc.getAsJsonArray("acquisitions").get(0).getAsJsonObject().addProperty("support","INVALID");
		Advisor invalid=new Advisor(KnowledgeCatalog.read(new Gson(),new StringReader(doc.toString())));
		var v=reviewedUpgrades(invalid, a,missing).stream().filter(u->u.upgrade.id.equals("tormented-bracelet")).findFirst().orElseThrow();
		assertEquals(Category.CHECK,v.pursuit.category);assertTrue(v.comparison.dpsGain()>0);assertNull(v.pursuit.estimate.meanTrials);
	}
	@Test public void raidPreparationRemainsConsequentialEvenWhenItIsNotAHardRecipeEdge()
	{
		assertEquals(Category.CHECK,view(withFact(developed(false),"toa-preparation",Observation.unknown()),missing,"tumekens-shadow").pursuit.category);
		assertEquals(Category.CHECK,view(withFact(developed(false),"toa-preparation",fact(false)),missing,"tumekens-shadow").pursuit.category);
		assertEquals(Category.CHECK,view(withFact(developed(false),"cox-preparation",Observation.unknown()),missing,"ancestral-robe-top").pursuit.category);
	}
	@Test public void sourceRecordOrderAndExtraEncounterProseCannotChangeCategoriesOrPriority() throws Exception
	{
		AccountSnapshot a=developed(false);List<String> original=reviewedUpgrades(advisor, a,missing).stream().map(v->v.upgrade.id+v.pursuit.category+v.pursuit.reasonCodes).collect(java.util.stream.Collectors.toList());
		JsonObject doc=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/catalog.json"))).getAsJsonObject();
		com.google.gson.JsonArray reversed=new com.google.gson.JsonArray();for(int i=doc.getAsJsonArray("sources").size()-1;i>=0;i--)reversed.add(doc.getAsJsonArray("sources").get(i));doc.add("sources",reversed);
		JsonObject upgrade=doc.getAsJsonArray("upgrades").get(8).getAsJsonObject();
		for(int i=0;i<12;i++){JsonObject app=upgrade.getAsJsonArray("applications").get(0).getAsJsonObject().deepCopy();app.addProperty("id","prose-"+i);app.addProperty("text","Extra description without evidence of wider context");upgrade.getAsJsonArray("applications").add(app);}
		Advisor changed=new Advisor(KnowledgeCatalog.read(new Gson(),new StringReader(doc.toString())));
		JsonObject unreviewed=doc.deepCopy();unreviewed.getAsJsonArray("upgrades").get(8).getAsJsonObject().getAsJsonArray("contexts").add("more-prose-is-not-reviewed-coverage");
		assertThrows(IllegalArgumentException.class,()->KnowledgeCatalog.read(new Gson(),new StringReader(unreviewed.toString())));
		assertEquals(original,reviewedUpgrades(changed, a,missing).stream().map(v->v.upgrade.id+v.pursuit.category+v.pursuit.reasonCodes).collect(java.util.stream.Collectors.toList()));
	}
	@Test public void nearlyCompleteAndSkippedAccountsDoNotFillSlots()
	{
		AccountSnapshot a=items(developed(true),21018,21021,21024,12002,19544,28313,26374,31097);
		List<Advisor.UpgradeView> views=reviewedUpgrades(advisor, a,missing);assertTrue(views.isEmpty());
		PursuitOverview overview=new PursuitOverview(views,List.of(),List.of(),List.of());assertTrue(overview.pursuits.isEmpty());assertTrue(overview.longTerm.isEmpty());
		assertTrue(reviewedUpgrades(advisor, developed(false),missing.skip("demonic-gorillas",true)).stream().noneMatch(v->v.upgrade.id.equals("tormented-bracelet")));
	}
}
