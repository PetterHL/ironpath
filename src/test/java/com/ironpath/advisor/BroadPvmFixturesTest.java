package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.model.Truth;
import com.ironpath.preferences.Preferences;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.AdvisorTest.fact;
import static org.junit.Assert.*;

/** Synthetic observations and explicit choices. These are not player usefulness or in-game results. */
public class BroadPvmFixturesTest
{
	private Advisor advisor;
	private KnowledgeCatalog catalog;
	private static final Instant TIME = Instant.parse("2026-10-12T17:00:00Z");
	private static final Set<Integer> BASE = Set.of(4151, 9185, 11907, 25979);
	@Before public void setup() throws Exception { catalog = KnowledgeCatalog.load(new Gson()); advisor = new Advisor(catalog); }
	private AccountSnapshot account(Set<Integer> bank, int slayer, boolean rigour, boolean augury)
	{
		Map<String, Integer> levels = new HashMap<>();
		for (String skill : List.of("ATTACK", "STRENGTH", "DEFENCE", "HITPOINTS", "RANGED", "MAGIC", "PRAYER", "AGILITY", "CRAFTING", "HERBLORE")) { levels.put(skill, 90); }
		levels.put("SLAYER", slayer);
		Map<String, Observation<Boolean>> facts = new HashMap<>();
		for (String key : List.of("song-of-elves", "regicide", "beneath-cursed-sands", "perilous-moons")) { facts.put(key, fact(true)); }
		facts.put("deadeye", fact(false)); facts.put("mystic-vigour", fact(false));
		return new AccountSnapshot(fact(levels), fact(Set.of()), fact(Set.of()), fact(bank), fact(rigour), fact(augury), facts);
	}
	private Preferences gaps()
	{
		Preferences p = Preferences.EMPTY;
		for (String set : List.of("bowfaOrBetter", "atlatl", "swampOrBetter", "seasOrBetter", "fang", "magicFang"))
		{ p = p.confirm("owns-" + set, false, TIME); }
		return p;
	}
	private List<String> ids(AccountSnapshot a, Preferences p)
	{ return advisor.evaluate(a, p).stream().map(r -> r.decision.id).collect(Collectors.toList()); }
	private AccountSnapshot changes(AccountSnapshot a, Map<String, Integer> levels, Map<String, Observation<Boolean>> facts)
	{ return new AccountSnapshot(fact(levels), a.inventory, a.equipment, a.bank, a.rigour, a.augury, facts); }

	@Test public void unopenedBankAndEvenAnEmptyOpenedBankDoNotInventAcquisitionGaps()
	{
		AccountSnapshot known = account(Set.of(), 90, true, true);
		AccountSnapshot unread = new AccountSnapshot(known.levels, known.inventory, known.equipment, Observation.unknown(), known.rigour, known.augury, known.facts);
		assertTrue(ids(unread, Preferences.EMPTY).isEmpty());
		assertTrue(ids(known, Preferences.EMPTY).isEmpty());
		Advisor.GoalView goal = advisor.goal(unread, Preferences.EMPTY.goal("ranged-options"));
		assertFalse(goal.status.startsWith("Satisfied"));
		assertTrue(goal.requirements.stream().anyMatch(r -> r.evidence.contains("Confirm only this target")));
		assertFalse(ids(unread, Preferences.EMPTY.goal("ranged-options")).contains("pursue-bowfa"));
	}

	@Test public void goalsRefineSupportedTiesWithoutPromotingUnknownOrLongerWork()
	{
		AccountSnapshot a = account(BASE, 90, false, false);
		Preferences p = gaps();
		assertEquals("seek-zulrah-magic-fang", ids(a, p.goal("ranged-options")).get(0)); // The reviewed medium route precedes the longer CG route.
		assertEquals("seek-zulrah-magic-fang", ids(a, p.goal("magic-upgrade")).get(0));
		assertEquals("seek-zulrah-magic-fang", ids(a, p.goal("toa-entry")).get(0)); // A goal does not rewrite method/effort evidence.
		assertTrue(catalog.decisions.stream().filter(d -> d.id.equals("seek-zulrah-magic-fang")).findFirst().orElseThrow()
			.goals.containsAll(List.of("toa-entry", "plan-team-cox")));
		for (String goal : List.of("ranged-options", "magic-upgrade", "toa-entry"))
		{
			List<Advisor.Recommendation> result = advisor.evaluate(a, p.goal(goal));
			assertTrue(result.size() <= 3);
			assertEquals(result.size(), result.stream().map(r -> r.decision.activity).distinct().count());
			assertEquals(ids(a, p.goal(goal)), ids(a, p.goal(goal)));
		}
	}

	@Test public void declinedCgYieldsMoonsAndNeverReappearsThroughAnotherCard()
	{
		AccountSnapshot a = account(BASE, 90, false, false);
		Preferences p = gaps().goal("ranged-options").skip("cg", true);
		assertTrue(advisor.availableActivities(a, p).stream().anyMatch(r -> r.decision.id.equals("seek-moons-atlatl")));
		assertTrue(advisor.evaluate(a, p).stream().noneMatch(r -> r.decision.activity.equals("cg")));
		Advisor.Recommendation moons = advisor.availableActivities(a, p).stream().filter(r -> r.decision.id.equals("seek-moons-atlatl")).findFirst().orElseThrow();
		assertTrue(moons.decision.alternative.contains("not") || moons.decision.tradeoff.contains("not"));
		assertTrue(moons.evidence.stream().anyMatch(e -> e.contains("manual planning confirmation (false)")));
		assertEquals("Available preparation action", advisor.goal(a, p).status);
		assertTrue(advisor.goal(a, p).nextAction.contains("Moons"));
		assertTrue(advisor.availableActivities(a,p.skip("cg",false)).stream().anyMatch(r->r.decision.id.equals("pursue-bowfa")));
	}

	@Test public void postBowfaGetsMultipleNonCoxAcquisitionChoicesAndCrystalStillWorksAfterSkip()
	{
		AccountSnapshot a = account(Set.of(4151, 25867, 23975, 23979, 23971, 11907, 25979), 90, true, false);
		Preferences p = gaps().skip("cg", true);
		List<String> general = ids(a, p);
		assertTrue(general.contains("seek-zulrah-magic-fang"));
		assertTrue(general.contains("seek-toa-fang"));
		assertTrue(general.contains("seek-titans-prayers"));
		assertFalse(general.contains("pursue-bowfa")); assertFalse(general.contains("seek-moons-atlatl"));
		assertTrue(advisor.availableActivities(a, p.goal("plan-team-cox")).stream().anyMatch(r -> r.decision.id.equals("plan-team-cox")));
		assertTrue(advisor.availableActivities(a, p.goal("plan-team-cox")).stream().filter(r -> r.decision.id.equals("plan-team-cox")).findFirst().orElseThrow().evidence.stream().anyMatch(e -> e.contains("Bow of faerdhinen")));
		assertTrue(advisor.goal(a, p.goal("ranged-options")).status.startsWith("Satisfied"));
	}

	@Test public void establishedRaiderWithAdvancedMagicGetsTheRemainingFangRoute()
	{
		AccountSnapshot a = account(Set.of(4151, 25867, 23975, 23979, 23971, 27275, 25979), 95, true, true);
		List<String> result = ids(a, gaps().goal("toa-entry"));
		assertEquals("seek-toa-fang", result.get(0));
		assertFalse(result.contains("seek-zulrah-magic-fang"));
		assertFalse(result.contains("seek-seas-trident"));
		assertTrue(result.contains("plan-team-cox"));
	}

	@Test public void manualAdvancedOwnershipSuppressesGrindsButCannotCreateUsableGear()
	{
		AccountSnapshot a = account(Set.of(4151, 9185, 11908, 25979), 90, true, true);
		Preferences p = gaps().confirm("item-27277", true, TIME);
		List<String> result = ids(a, p);
		assertFalse(result.contains("seek-seas-trident")); assertFalse(result.contains("seek-zulrah-magic-fang"));
		assertFalse(result.contains("seek-toa-fang")); assertFalse(result.contains("plan-team-cox"));
		assertEquals(Truth.FALSE, a.observedItem(27277));
		assertTrue(advisor.goal(a, p.goal("magic-upgrade")).status.startsWith("Satisfied"));
		assertFalse(advisor.goal(a, p.goal("toa-entry")).status.startsWith("Available"));
	}

	@Test public void observedAndManualPositivesOverrideASetAbsence()
	{
		AccountSnapshot a = account(Set.of(25862), 90, true, true);
		assertFalse(ids(a, gaps()).contains("pursue-bowfa"));
		assertTrue(advisor.goal(a, gaps().goal("ranged-options")).status.startsWith("Satisfied"));
		AccountSnapshot empty = account(Set.of(), 90, true, true);
		Preferences p = gaps().confirm("item-20997", true, TIME);
		assertFalse(ids(empty, p).contains("pursue-bowfa"));
		assertFalse(ids(empty, p).contains("seek-moons-atlatl"));
		assertFalse(ids(empty, p).contains("plan-team-cox"));
	}

	@Test public void slayerTrainingAndTaskAcquisitionAreSeparateAndBothStopForOwnedAlternatives()
	{
		AccountSnapshot low = account(Set.of(4151, 9185, 25979), 86, true, true);
		Preferences p = gaps().goal("magic-upgrade");
		assertTrue(advisor.availableActivities(low,p).stream().anyMatch(r->r.decision.id.equals("train-slayer-trident"))); // Unreviewed training effort remains conditional, rather than forced first.
		assertFalse(ids(low, p).contains("seek-seas-trident"));
		AccountSnapshot high = account(low.bank.value(), 87, true, true);
		assertTrue(advisor.availableActivities(high,p).stream().anyMatch(r->r.decision.id.equals("seek-seas-trident")));
		assertFalse(ids(high, p).contains("train-slayer-trident"));
		assertTrue(advisor.availableActivities(high,p).stream().filter(r->r.decision.id.equals("seek-seas-trident")).findFirst().orElseThrow().decision.access.contains("current task is unobserved"));
		assertFalse(ids(high, p.confirm("item-31115", true, TIME)).contains("seek-seas-trident"));
		assertFalse(ids(low, p.confirm("item-31115", true, TIME)).contains("train-slayer-trident"));
	}

	@Test public void activitySkipExcludesBothSlayerStepsWithoutCompletingTheOutcome()
	{
		AccountSnapshot a = account(Set.of(), 86, true, true);
		Preferences p = gaps().goal("magic-upgrade").skip("slayer", true);
		assertTrue(advisor.evaluate(a, p).stream().noneMatch(r -> r.decision.activity.equals("slayer")));
		assertFalse(advisor.goal(a, p).status.startsWith("Satisfied"));
	}

	@Test public void missingAccessAndPreparationStaySeparateAndAnyAlternativeCanSatisfyTheGoal()
	{
		AccountSnapshot a = account(BASE, 90, true, true);
		Map<String, Observation<Boolean>> facts = new HashMap<>(a.facts); facts.put("song-of-elves", fact(false));
		AccountSnapshot missingCg = changes(a, a.levels.value(), facts);
		Preferences p = gaps().goal("ranged-options");
		assertTrue(advisor.availableActivities(missingCg, p).stream().anyMatch(r -> r.decision.id.equals("seek-moons-atlatl")));
		assertEquals("Available preparation action", advisor.goal(missingCg, p).status);
		Map<String, Integer> levels = new HashMap<>(a.levels.value()); levels.put("HERBLORE", 49);
		AccountSnapshot prepGap = changes(missingCg, levels, facts);
		assertFalse(ids(prepGap, p).contains("seek-moons-atlatl"));
		Advisor.GoalView goal = advisor.goal(prepGap, p);
		assertTrue(goal.status.startsWith("Blocked:"));
		assertTrue(goal.nearestMissing.contains("ACCESS") && goal.nearestMissing.contains("Song of the Elves"));
		Advisor.GoalView declined = advisor.goal(prepGap, p.skip("cg", true));
		assertTrue(declined.nearestMissing.contains("PREPARATION") && declined.nearestMissing.contains("HERBLORE >= 50"));
		assertTrue(goal.requirements.stream().anyMatch(r -> r.evidence.contains("HERBLORE >= 50") && r.evidence.contains("Perilous Moons")));
		KnowledgeCatalog.Decision moons = catalog.decisions.stream().filter(d -> d.id.equals("seek-moons-atlatl")).findFirst().orElseThrow();
		assertEquals(Truth.TRUE, advisor.truth(moons.accessWhen, prepGap));
		assertEquals(Truth.FALSE, advisor.truth(moons.prepWhen, prepGap));
		assertTrue(moons.preparation.contains("not dungeon access"));
	}

	@Test public void higherPrayersSatisfyAnyBranchesAndUnknownsDoNotTurnIntoUnlockGrinds()
	{
		AccountSnapshot a = account(BASE, 90, true, true);
		assertFalse(ids(a, gaps()).contains("seek-titans-prayers"));
		Map<String, Observation<Boolean>> unknown = new HashMap<>(a.facts);
		unknown.put("deadeye", Observation.unknown()); unknown.put("mystic-vigour", Observation.unknown());
		AccountSnapshot higher = changes(a, a.levels.value(), unknown);
		assertFalse(ids(higher, gaps()).contains("seek-titans-prayers"));
		AccountSnapshot unconfirmed = new AccountSnapshot(a.levels, a.inventory, a.equipment, a.bank,
			Observation.unknown(), Observation.unknown(), unknown);
		assertFalse(ids(unconfirmed, gaps()).contains("seek-titans-prayers"));
		Preferences manual = gaps().confirm("rigour", false, TIME).confirm("augury", false, TIME)
			.confirm("deadeye", false, TIME).confirm("mystic-vigour", false, TIME);
		assertTrue(ids(unconfirmed, manual).contains("seek-titans-prayers"));
	}
	@Test public void observedUnconsumedIntermediateScrollStopsItsAcquisitionRoute()
	{
		AccountSnapshot a = account(Set.of(4151, 9185, 11907, 30627), 90, true, false);
		assertFalse(ids(a, gaps()).contains("seek-titans-prayers"));
		AccountSnapshot unread = new AccountSnapshot(a.levels, fact(Set.of(30627)), a.equipment, Observation.unknown(), a.rigour, a.augury, a.facts);
		assertFalse(ids(unread, gaps()).contains("seek-titans-prayers"));
		assertFalse(a.fact("mystic-vigour").value()); // The scroll is not the permanent unlock.
	}

}
