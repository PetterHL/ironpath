package com.ironpath.advisor;

import com.google.gson.*;
import com.ironpath.model.*;
import com.ironpath.preferences.Preferences;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.P4aFixtures.*;
import static com.ironpath.advisor.R15Fixtures.*;
import static com.ironpath.advisor.PursuitAssessment.*;
import static com.ironpath.advisor.ValueEvidence.Importance.*;
import static org.junit.Assert.*;

/** Original no-goal counterfactuals. Only supported portions of PI profiles are asserted. */
public class R3ProgressionTest
{
	private KnowledgeCatalog catalog; private Advisor advisor; private Preferences missing;
	@Before public void load() throws Exception { catalog=KnowledgeCatalog.load(new Gson()); advisor=new Advisor(catalog); missing=absent(catalog); }
	private Advisor.UpgradeView view(AccountSnapshot a,String id)
	{ return advisor.upgrades(a,missing).stream().filter(v->v.upgrade.id.equals(id)).findFirst().orElseThrow(); }
	private AccountSnapshot muspah()
	{ return withFact(withFact(R2AdviceTest.postBowfa(),"secrets-of-north",fact(true)),"muspah-preparation",fact(true)); }
	private ActivityOpportunity activity(AccountSnapshot a,String id)
	{ return advisor.activityOpportunities(a,missing,advisor.upgrades(a,missing)).stream().filter(v->v.activity.equals(id)).findFirst().orElseThrow(); }
	@Test public void coreNeckImportanceSurvivesSmallNeutralBreakpointAndOwnedRuptureEndsDuplicateWork()
	{
		AccountSnapshot a=items(R2AdviceTest.postBowfa(),19535,1597);
		Advisor.UpgradeView neck=view(a,"necklace-of-anguish");
		assertEquals(KEY,neck.valueEvidence.importance);assertTrue(neck.pursuit.reasonCodes.contains("CORE_ROLE_GAP"));
		assertEquals(Mode.BOUNDED,neck.pursuit.acquisitionMode);assertFalse(neck.pursuit.blockers.stream().anyMatch(s->s.contains("Monkey Madness")));
		assertTrue(neck.card.reason.contains(neck.valueEvidence.baseline));assertNotNull(neck.comparison.before);
		assertTrue(advisor.upgrades(items(a,33639),missing).stream().noneMatch(v->v.upgrade.id.equals("necklace-of-anguish")));
	}
	@Test public void burstAndConvenienceSurviveMegararesAndTheirIndependentMetricGaps()
	{
		AccountSnapshot a=items(muspah(),20997,27275,22325,11212,27226,27229,27232);
		Advisor.UpgradeView zaryte=view(a,"zaryte-crossbow"),venator=view(a,"venator-bow");
		assertEquals(STRONG,zaryte.valueEvidence.importance);assertTrue(zaryte.valueEvidence.independentUtility);
		assertTrue(zaryte.card.metric.contains("Burst not quantified"));assertTrue(zaryte.valueEvidence.roles.get(0).limitation.contains("successful hit"));
		assertEquals(CONTEXTUAL,venator.valueEvidence.importance);assertTrue(venator.valueEvidence.reasonCodes.contains("REVIEWED_ROLE_VALUE"));
		assertEquals(Category.PURSUE,venator.pursuit.category);assertTrue(venator.card.metric.contains("geometry gap"));
		assertFalse(advisor.upgrades(a,missing).stream().anyMatch(v->v.upgrade.id.equals("swamp-trident")||v.upgrade.id.equals("twisted-bow")||v.upgrade.id.equals("bowfa")));
	}
	@Test public void contextLossNarrowsBurstWithoutPretendingAnOwnedBowCoversIt()
	{
		AccountSnapshot a=items(muspah(),20997,11785);
		assertEquals(STRONG,view(a,"zaryte-crossbow").valueEvidence.importance);
		Advisor.UpgradeView narrowed=view(withFact(a,"zaryte-context",fact(false)),"zaryte-crossbow");
		assertEquals(MARGINAL,narrowed.valueEvidence.importance);assertEquals(Category.DEFER,narrowed.pursuit.category);
		Advisor.UpgradeView unknown=view(withFact(a,"zaryte-context",Observation.unknown()),"zaryte-crossbow");
		assertEquals(UNCERTAIN,unknown.valueEvidence.importance);assertEquals(Category.CHECK,unknown.pursuit.category);
		assertFalse(narrowed.valueEvidence.independentUtility);
	}
	@Test public void exactFiveShardQuantityBypassesMuspahButPresenceOrFourShardsDoesNot()
	{
		AccountSnapshot a=withFact(withQuantity(items(muspah(),27614),27614,4),"secrets-of-north",fact(false));
		Advisor.UpgradeView four=view(a,"venator-bow");assertTrue(four.path.stream().anyMatch(n->n.label.contains("4 / 5")));
		assertTrue(four.pursuit.blockers.stream().anyMatch(s->s.contains("Secrets")));
		Advisor.UpgradeView five=view(withQuantity(a,27614,5),"venator-bow");
		assertEquals(Mode.BOUNDED,five.pursuit.acquisitionMode);assertEquals(Category.PURSUE,five.pursuit.category);
		assertFalse(five.pursuit.blockers.stream().anyMatch(s->s.contains("Secrets")||s.contains("Muspah")));
		assertFalse(advisor.upgrades(items(a,27612),missing).stream().anyMatch(v->v.upgrade.id.equals("venator-bow")));
		Preferences manual=missing.confirm("owns-venatorShards",true,TIME);
		assertNotEquals(Mode.BOUNDED,advisor.upgrades(muspah(),manual).stream().filter(v->v.upgrade.id.equals("venator-bow")).findFirst().orElseThrow().pursuit.acquisitionMode);
	}
	@Test public void hornAndAcbRemoveDropsButUnknownShardsRemainAnExplicitCheck()
	{
		Advisor.UpgradeView z=view(items(muspah(),26372,11785),"zaryte-crossbow");
		assertEquals(Mode.BOUNDED,z.pursuit.acquisitionMode);assertEquals(Category.CHECK,z.pursuit.category);
		assertTrue(z.pursuit.blockers.stream().anyMatch(s->s.toLowerCase().contains("shard")));
		assertFalse(z.pursuit.blockers.stream().anyMatch(s->s.contains("Obtain nihil horn")));
	}
	@Test public void knownClothEligibilitySurvivesUnknownTreadsAndEyeEligibility()
	{
		AccountSnapshot a=items(withFact(withFact(withFact(muspah(),"final-dawn",fact(true)),"doom-preparation",fact(true)),"doom-delve-two",fact(true)),19544,11840);
		ActivityOpportunity doom=activity(a,"doom");assertTrue(doom.available);assertEquals(Truth.TRUE,doom.access);
		assertTrue(doom.usefulRewards.toString(),doom.usefulRewards.contains("Mokhaiotl cloth"));assertTrue(doom.uncertainRewards.toString(),doom.uncertainRewards.stream().anyMatch(s->s.contains("treads")));
		assertFalse(doom.usefulRewards.contains("Eye of Ayak"));
		AccountSnapshot tier4=withFact(a,"doom-delve-four",fact(true));assertTrue(activity(tier4,"doom").usefulRewards.contains("Base avernic treads"));
		assertEquals(Truth.TRUE,activity(tier4,"doom").access);
	}
	@Test public void heartOwnershipChangesSharedMaterialsWithoutPromisingBothAllocations()
	{
		AccountSnapshot a=withQuantity(items(muspah(),27614),27614,4);
		assertFalse(activity(a,"muspah").usefulFamilies.contains("heart-saturation"));
		ActivityOpportunity heart=activity(items(a,20724),"muspah");assertTrue(heart.usefulFamilies.contains("heart-saturation"));
		assertTrue(heart.exclusions.stream().anyMatch(s->s.contains("cannot also charge")));
		assertFalse(activity(items(a,20724,27641),"muspah").usefulFamilies.contains("heart-saturation"));
		AccountSnapshot sufficient=withQuantity(items(a,20724,27616),27616,150000);
		assertFalse(activity(sufficient,"muspah").usefulFamilies.contains("heart-saturation"));
		assertTrue(activity(sufficient,"muspah").exclusions.stream().anyMatch(s->s.contains("bypass collection")));
		AccountSnapshot constrained=withQuantity(sufficient,27616,15000);
		assertTrue(activity(constrained,"muspah").uncertainRewards.stream().anyMatch(s->s.contains("quantity/allocation")));
	}
	@Test public void optionalVenatorNeverBlocksAnExistingReviewedColosseumMethod()
	{
		AccountSnapshot a=withFact(items(muspah(),12926,11230),"colosseum-preparation",fact(true));
		ActivityOpportunity arena=activity(a,"colosseum");assertTrue(arena.available);
		assertTrue(arena.preparation.stream().anyMatch(s->s.startsWith("OPTIONAL_IMPROVEMENT")));
		Advisor.UpgradeView venator=view(a,"venator-bow");
		assertTrue(venator.valueEvidence.roles.stream().flatMap(r->r.alternatives.stream()).anyMatch(s->s.contains("blowpipe")));
		assertTrue(venator.indirectReasons.stream().anyMatch(s->s.contains("quiver")));
	}
	@Test public void previewKeepsActionableFirstThenImportantLongTermAndSpecialistBeforeRoleRepeats()
	{
		List<Advisor.UpgradeView> views=advisor.upgrades(muspah(),missing);
		PursuitOverview overview=new PursuitOverview(views,List.of(),List.of(),List.of());
		assertEquals(overview.pursuits.get(0).upgrade.id,overview.preview.get(0).upgrade.id);
		assertEquals(Tail.LONG,overview.preview.get(1).pursuit.tail);
		assertNotEquals(overview.preview.get(0).upgrade.role,overview.preview.get(1).upgrade.role);
		assertTrue(overview.preview.get(2).valueEvidence.specialist);
		assertNotEquals(overview.preview.get(0).upgrade.role,overview.preview.get(2).upgrade.role);
		long justified=views.stream().filter(v->v.pursuit.category!=Category.DEFER).count();
		assertEquals(views.size(),overview.all.size());assertEquals(justified,overview.preview.size());assertEquals(justified,overview.preview.stream().map(v->v.upgrade.id).distinct().count());
		assertEquals(overview.previewReasons, new PursuitOverview(views,List.of(),List.of(),List.of()).previewReasons);
	}
	@Test public void noFillerWhenAllDeliveredOutcomesAreOwnedAndSkipsOnlyRemoveTheirRoute()
	{
		AccountSnapshot all=items(muspah(),catalog.upgrades.stream().mapToInt(u->u.itemId).toArray());
		assertTrue(advisor.upgrades(all,missing).isEmpty());
		List<Advisor.UpgradeView> skipped=advisor.upgrades(muspah(),missing.skip("muspah",true));
		assertFalse(skipped.stream().anyMatch(v->v.upgrade.id.equals("venator-bow")));
		assertTrue(skipped.stream().anyMatch(v->v.upgrade.id.equals("tumekens-shadow")));
	}
	private JsonObject document() throws Exception
	{ try(Reader in=new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/catalog.json"))){ return new JsonParser().parse(in).getAsJsonObject(); } }
	private List<String> fingerprint(Advisor evaluator,AccountSnapshot a)
	{ return evaluator.upgrades(a,missing).stream().map(v->v.upgrade.id+v.pursuit.category+v.valueEvidence.importance).collect(Collectors.toList()); }
	@Test public void duplicateUtilityAliasesAndFeedbackDoNotAmplifyRankOrRepeatTrails() throws Exception
	{
		AccountSnapshot a=withFact(muspah(),"colosseum-preparation",fact(true));List<String> before=fingerprint(advisor,a);
		JsonObject d=document();JsonObject relation=d.getAsJsonArray("roleSupport").get(5).getAsJsonObject().deepCopy();
		relation.addProperty("id","venator-alias");relation.addProperty("context","muspah-bowfa");d.getAsJsonArray("roleSupport").add(relation);
		for(JsonElement source:d.getAsJsonArray("sources")){if(relation.getAsJsonArray("sources").contains(source.getAsJsonObject().get("id")))source.getAsJsonObject().getAsJsonArray("relations").add("venator-alias");}
		Advisor changed=new Advisor(KnowledgeCatalog.read(new Gson(),new StringReader(d.toString())));
		assertEquals(before,fingerprint(changed,a));
		Advisor.UpgradeView v=changed.upgrades(a,missing).stream().filter(x->x.upgrade.id.equals("venator-bow")).findFirst().orElseThrow();
		assertEquals(v.indirectReasons.size(),v.indirectReasons.stream().distinct().count());
		assertFalse(v.indirectReasons.stream().anyMatch(t->t.contains("which still offers Venator bow")));
	}
	@Test public void withdrawnClaimOnlyRemovesDependentUtilityAndHardCyclesRemainRejected() throws Exception
	{
		JsonObject d=document();d.getAsJsonArray("roleSupport").get(4).getAsJsonObject().addProperty("status","withdrawn");
		Advisor changed=new Advisor(KnowledgeCatalog.read(new Gson(),new StringReader(d.toString())));
		Advisor.UpgradeView v=changed.upgrades(muspah(),missing).stream().filter(x->x.upgrade.id.equals("venator-bow")).findFirst().orElseThrow();
		assertFalse(v.valueEvidence.independentUtility);assertEquals(UNCERTAIN,v.valueEvidence.importance);
		assertNotNull(changed.upgrades(muspah(),missing).stream().filter(x->x.upgrade.id.equals("necklace-of-anguish")).findFirst().orElseThrow().comparison.before);
		JsonObject upgrade=d.getAsJsonArray("upgrades").get(d.getAsJsonArray("upgrades").size()-1).getAsJsonObject();
		JsonObject deps=new JsonObject();deps.addProperty("mode","NODE");deps.addProperty("node","assemble");upgrade.getAsJsonArray("path").get(0).getAsJsonObject().add("dependencies",deps);
		assertThrows(IllegalArgumentException.class,()->KnowledgeCatalog.read(new Gson(),new StringReader(d.toString())));
	}
	@Test public void unknownOwnershipKeepsConditionalUtilityWithoutFabricatedNumbers()
	{
		List<Advisor.UpgradeView> views=advisor.upgrades(muspah(),Preferences.EMPTY);
		Advisor.UpgradeView v=views.stream().filter(x->x.upgrade.id.equals("venator-bow")).findFirst().orElseThrow();
		assertEquals(CONTEXTUAL,v.valueEvidence.importance);assertEquals(Category.CHECK,v.pursuit.category);
		assertTrue(v.card.status.contains("ownership unconfirmed"));
		Advisor.UpgradeView core=views.stream().filter(x->x.upgrade.id.equals("necklace-of-anguish")).findFirst().orElseThrow();
		assertEquals(KEY,core.valueEvidence.importance);assertEquals(Category.CHECK,core.pursuit.category);
		assertTrue(core.valueEvidence.reasonCodes.contains("OWNERSHIP_CONDITIONAL_ROLE"));
		assertTrue(core.card.status.contains("ownership unconfirmed"));
		assertFalse(new PursuitOverview(views,List.of(),List.of(),List.of()).longTerm.isEmpty());assertFalse(v.comparison.available(ComparisonResult.Metric.THEORETICAL_DPS));
	}
	@Test public void explicitSingleCombatScenarioRemovesOnlyItsBounceBenefit()
	{
		AccountSnapshot single=withFact(withFact(muspah(),"multi-combat-method",fact(false)),"colosseum-preparation",fact(false));
		Advisor.UpgradeView v=view(single,"venator-bow");assertEquals(MARGINAL,v.valueEvidence.importance);assertEquals(Category.DEFER,v.pursuit.category);
		assertTrue(v.valueEvidence.reasonCodes.contains("METHOD_NO_BENEFIT"));
		assertEquals(CONTEXTUAL,view(withFact(single,"colosseum-preparation",fact(true)),"venator-bow").valueEvidence.importance);
	}
	@Test public void magicEncounterValueSurvivesNeutralLossWithoutChangingSelectedNumbers()
	{
		Advisor.UpgradeView shadow=view(muspah(),"tumekens-shadow");
		assertTrue(shadow.comparison.dpsGain()<0);assertEquals("neutral-zero-defence",shadow.comparison.target.id);
		assertTrue(shadow.contextComparisons.stream().anyMatch(r->r.available(ComparisonResult.Metric.THEORETICAL_DPS)&&r.dpsGain()>0));
		assertTrue(shadow.valueEvidence.specialist);assertEquals(Category.LONG_TERM,shadow.pursuit.category);
	}
	@Test public void generatePinnedSupportedScenarioEvidence() throws Exception
	{
		Map<String,AccountSnapshot> profiles=new LinkedHashMap<>();
		profiles.put("PI01 team preparation only; lance/debuff models pending",withFact(muspah(),"cox-preparation",fact(true)));
		profiles.put("PI02 mature CoX; current modelled rewards owned",items(muspah(),21018,21021,21024,21034,21079));
		profiles.put("PI03 quiver with owned switches",withFact(items(muspah(),12926,11230),"colosseum-preparation",fact(true)));
		profiles.put("PI04 multi-combat convenience without preference inference",muspah());
		profiles.put("PI05 Doom; selected method reviewed, delve ability unknown",withFact(withFact(muspah(),"final-dawn",fact(true)),"doom-preparation",fact(true)));
		profiles.put("PI08 four observed shards",withQuantity(items(muspah(),27614),27614,4));
		profiles.put("PI09 three megarares; utility missing",items(muspah(),20997,27275,22325,11212,27226,27229,27232,12002,19544));
		profiles.put("PI10 all delivered outcomes owned",items(muspah(),catalog.upgrades.stream().mapToInt(u->u.itemId).toArray()));
		profiles.put("PI11 owned heart; essence competition",withQuantity(items(muspah(),20724,27614),27614,4));
		profiles.put("PI12 unknown ownership",muspah());
		profiles.put("PI14 owned treads; individual addition model pending",items(muspah(),31088,13239));
		profiles.put("PI15 scroll state unknown; penny/pouch model pending",muspah());
		profiles.put("PI16 reviewed method before optional Venator detour",profiles.get("PI03 quiver with owned switches"));
		StringBuilder report=new StringBuilder("# R3a synthetic account evidence\n\nNo goal. Knowledge r3a-1 / schema 10 / progression-1. Unchanged powered-magic-2 and ranged-1 models. Original snapshots, explicit planning facts and absent confirmations, not player evidence. Source revisions are pinned in catalog.json. Unsupported profile portions remain pending in R3a.md.\n\n");
		for(Map.Entry<String,AccountSnapshot> entry:profiles.entrySet())
		{
			AccountSnapshot a=entry.getValue();Preferences p=entry.getKey().startsWith("PI12")?Preferences.EMPTY:missing;
			List<Advisor.UpgradeView> views=advisor.upgrades(a,p);PursuitOverview overview=new PursuitOverview(views,List.of(),List.of(),List.of());
			report.append("## ").append(entry.getKey()).append("\n\nInputs: observed bank ").append(new TreeSet<>(a.bank.value())).append("; real levels ").append(new TreeMap<>(a.levels.value())).append("; facts ").append(a.facts.entrySet().stream().filter(e->e.getValue().known()).sorted(Map.Entry.comparingByKey()).map(e->e.getKey()+"="+e.getValue().value()).collect(Collectors.toList())).append("; observed counts ").append(a.bankQuantities.value()).append(". References are proposed complete compatible assumptions, not equipped setups.\n\n");
			if(views.isEmpty())report.append("No remaining supported outcome; no filler. Comprehensive role coverage remains open.\n\n");
			for(Advisor.UpgradeView v:overview.preview)
			{
				report.append("- **").append(v.upgrade.title).append("**: ").append(v.valueEvidence.importance.label).append(" / ").append(v.pursuit.category.label).append(". ").append(v.card.reason).append("\n  Card: ").append(v.card.metric).append("; ").append(v.card.status).append("\n  Selected comparison: ").append(v.comparison.summary()).append("\n  Reasons: ").append(v.pursuit.reasonCodes).append(". Preview: ").append(overview.previewReasons.get(v.upgrade.id)).append("\n  Next: ").append(v.routes.get(0).next).append("\n");
			}
			report.append("\n");
		}
		Path path=Path.of("build/reports/r3a/scenarios.md");Files.createDirectories(path.getParent());Files.writeString(path,report);
	}
}
