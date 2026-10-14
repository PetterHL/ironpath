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

/** Pure synthetic planning fixtures: user choices must not become game observations. */
public class PreferencesAdvisorTest
{
	private Advisor advisor;
	private KnowledgeCatalog catalog;
	private static final Instant TIME = Instant.parse("2026-10-12T12:00:00Z");
	@Before public void setup() throws Exception { catalog = KnowledgeCatalog.load(new Gson()); advisor = new Advisor(catalog); }
	private List<String> ids(AccountSnapshot account, Preferences preferences)
	{ return advisor.evaluate(account, preferences).stream().map(r -> r.decision.id).collect(Collectors.toList()); }
	private AccountSnapshot account(int prayer, Set<Integer> items, Observation<Boolean> rigour, Observation<Boolean> augury)
	{
		AccountSnapshot base = AdvisorTest.account(prayer, fact(items), rigour, augury);
		Map<String, Integer> levels = new HashMap<>(base.levels.value()); levels.put("CRAFTING", 90);
		return new AccountSnapshot(fact(levels), base.inventory, base.equipment, base.bank, rigour, augury);
	}

	@Test public void remainingEffortJointlyControlsQueueWhileGoalKeepsItsOwnNextAction()
	{
		AccountSnapshot account = account(70, Set.of(4151, 22978, 9185, 11907, 12932, 1755, 13576), fact(true), fact(true));
		List<String> general = ids(account, Preferences.EMPTY);
		assertEquals(3, general.size()); assertEquals("assemble-swamp-trident", general.get(0));
		Preferences augury = Preferences.EMPTY.goal("train-augury");
		assertEquals("assemble-swamp-trident", ids(account, augury).get(0)); // Ready owned-component work precedes unreviewed training effort.
		assertTrue(advisor.goal(account, augury).nextAction.contains("Prayer to 77"));
		assertTrue(advisor.availableActivities(account, augury).stream().anyMatch(r -> r.decision.id.equals("train-augury")));
		Preferences cox = Preferences.EMPTY.goal("plan-team-cox");
		assertEquals("assemble-swamp-trident", ids(account, cox).get(0));
		assertTrue(advisor.goal(account, cox).nextAction.contains("defence-reduction"));
		assertTrue(advisor.availableActivities(account, cox).stream().anyMatch(r -> r.decision.id.equals("plan-team-cox")));
		assertEquals(ids(account, cox), ids(account, cox));
		assertEquals(3, advisor.evaluate(account, cox).stream().map(r -> r.decision.family).distinct().count());
		assertTrue(advisor.availableActivities(account, augury).stream().filter(r -> r.decision.id.equals("train-augury")).findFirst().orElseThrow().evidence.stream().anyMatch(e -> e.contains("directly supports selected goal")));
	}

	@Test public void teamGoalIsAvailableWhenSpecificCoordinationReplacesBroadPlanning()
	{
		AccountSnapshot account = account(90, Set.of(4151, 9185, 11907, 13576), fact(true), fact(true));
		Preferences preferences = Preferences.EMPTY.goal("plan-team-cox");
		assertEquals(List.of("coordinate-defence-reduction"), ids(account, preferences));
		Advisor.GoalView goal = advisor.goal(account, preferences);
		assertTrue(goal.requirements.stream().allMatch(r -> r.truth == Truth.TRUE));
		assertNull(goal.nearestMissing);
		assertEquals("Available preparation action", goal.status);
		assertTrue(goal.nextAction.startsWith("Agree team defence-reduction coverage"));
		assertTrue(goal.nextAction.contains("not certification of raid access"));
	}

	@Test public void teamGoalAlternativeDoesNotOverrideMissingUnknownOrSkippedPrerequisites()
	{
		AccountSnapshot account = account(90, Set.of(4151, 9185, 11907, 13576), fact(true), fact(true));
		Preferences preferences = Preferences.EMPTY.goal("plan-team-cox");
		Advisor.GoalView skipped = advisor.goal(account, preferences.skip("plan-team-cox", true));
		assertTrue(skipped.status.startsWith("Blocked by a skipped action"));
		assertTrue(skipped.nextAction.startsWith("Restore the skipped action"));
		Advisor.GoalView missing = advisor.goal(account(69, account.bank.value(), fact(true), fact(true)), preferences);
		assertTrue(missing.status.startsWith("Blocked:"));
		assertEquals("PRAYER >= 70", missing.nearestMissing);
		assertFalse(missing.nextAction.startsWith("Agree team defence-reduction coverage"));
		AccountSnapshot unread = new AccountSnapshot(account.levels, account.inventory, account.equipment,
			Observation.unknown(), account.rigour, account.augury);
		Advisor.GoalView unknown = advisor.goal(unread, preferences);
		assertTrue(unknown.status.startsWith("Needs observation or confirmation"));
		assertNull(unknown.nearestMissing);
		assertTrue(unknown.requirements.stream().anyMatch(r -> r.truth == Truth.UNKNOWN));
		assertFalse(unknown.nextAction.startsWith("Agree team defence-reduction coverage"));
	}

	@Test public void skippedRequiredUnlockBlocksGoalAndRestoringItDoesNotCompleteIt()
	{
		AccountSnapshot account = account(70, Set.of(21034), fact(false), fact(true));
		Preferences goal = Preferences.EMPTY.goal("train-rigour").skip("unlock-rigour", true);
		assertEquals(List.of("train-augury"), ids(account, goal));
		Advisor.GoalView blocked = advisor.goal(account, goal);
		assertTrue(blocked.status.contains("Blocked by a skipped"));
		assertEquals("Rigour permanently unlocked", blocked.nearestMissing);
		assertEquals(Truth.FALSE, blocked.requirements.get(0).truth);
		assertTrue(blocked.requirements.get(0).skipped);
		Preferences restored = goal.skip("unlock-rigour", false);
		assertEquals(List.of("unlock-rigour", "train-augury"), ids(account, restored));
		assertTrue(advisor.goal(account, restored).nextAction.contains("dexterous prayer scroll"));
		assertTrue(advisor.goal(account, restored).status.startsWith("Blocked:"));
		// Actual completion can satisfy an old skip. The skip alone never did.
		AccountSnapshot unlocked = account(70, Set.of(), fact(true), fact(true));
		assertEquals("train-rigour", ids(unlocked, goal).get(0));
	}

	@Test public void meetingPrayerLevelAloneDoesNotCompleteAUseGoal()
	{
		AccountSnapshot account = account(99, Set.of(21034), fact(false), fact(true));
		Advisor.GoalView goal = advisor.goal(account, Preferences.EMPTY.goal("train-rigour"));
		assertTrue(goal.status.startsWith("Blocked:"));
		assertEquals("Rigour permanently unlocked", goal.nearestMissing);
		assertTrue(goal.nextAction.contains("dexterous prayer scroll"));
	}

	@Test public void unknownOwnershipDoesNotProduceMissingPartsOrAnInventedGrind()
	{
		AccountSnapshot account = account(90, Set.of(), fact(true), fact(true));
		Preferences preferences = Preferences.EMPTY.goal("assemble-swamp-trident");
		Advisor.GoalView goal = advisor.goal(account, preferences);
		assertNull(goal.nearestMissing);
		assertTrue(goal.requirements.stream().filter(r -> r.label.startsWith("Possession")).allMatch(r -> r.truth == Truth.UNKNOWN));
		assertTrue(ids(account, preferences).isEmpty());
		preferences = preferences.confirm("item-12932", false, TIME);
		goal = advisor.goal(account, preferences);
		assertTrue(goal.nearestMissing.contains("magic fang"));
		assertTrue(goal.nextAction.contains("No acquisition route"));
		assertTrue(ids(account, preferences).isEmpty());
	}

	@Test public void manualOwnedAlternativeSuppressesRedundantAssemblyAndCanBeCleared()
	{
		AccountSnapshot account = account(90, Set.of(12932, 1755, 11908), fact(true), fact(true));
		assertEquals(List.of("assemble-swamp-trident"), ids(account, Preferences.EMPTY));
		Preferences manual = Preferences.EMPTY.confirm("item-27277", true, TIME);
		assertTrue(ids(account, manual).isEmpty());
		assertEquals(Truth.FALSE, account.observedItem(27277));
		assertEquals(List.of("assemble-swamp-trident"), ids(account, manual.confirm("item-27277", null, TIME)));
	}

	@Test public void manualItemsCannotCreateUsableGearOrAssemblySupplies()
	{
		AccountSnapshot account = account(90, Set.of(), fact(true), fact(true));
		Preferences manual = Preferences.EMPTY;
		for (int id : List.of(4151, 9185, 11907, 12932, 1755)) { manual = manual.confirm("item-" + id, true, TIME); }
		assertTrue(ids(account, manual).isEmpty());
		assertEquals(Truth.FALSE, account.observedItem(11907));
		Advisor.GoalView goal = advisor.goal(account, manual.goal("plan-team-cox"));
		assertTrue(goal.requirements.stream().filter(r -> r.label.startsWith("Recognised")).allMatch(r -> r.truth == Truth.UNKNOWN));
	}

	@Test public void livePositivesOverrideManualAbsenceAndUnlockFallbackIsLabelled()
	{
		AccountSnapshot account = account(70, Set.of(12932, 1755, 11908), Observation.unknown(), fact(true));
		Preferences manual = Preferences.EMPTY.confirm("item-12932", false, TIME).confirm("rigour", true, TIME);
		Advisor.GoalView goal = advisor.goal(account, manual.goal("assemble-swamp-trident"));
		assertEquals(Truth.TRUE, goal.requirements.get(0).truth);
		assertEquals("assemble-swamp-trident", ids(account, manual).get(0));
		assertTrue(advisor.availableActivities(account, manual).stream().filter(r->r.decision.id.equals("train-rigour")).findFirst().orElseThrow().evidence.stream().anyMatch(e -> e.contains("manual planning confirmation")));
		AccountSnapshot readable = new AccountSnapshot(account.levels, account.inventory, account.equipment, account.bank, fact(false), fact(true));
		assertFalse(ids(readable, manual).contains("train-rigour"));
		assertFalse(readable.rigour.value());
	}

	@Test public void observedWeaponPathShowsItsConfirmedLevelBlockerWithoutMissingOwnership() throws Exception
	{
		AccountSnapshot base = account(90, Set.of(4151, 25867, 23975, 23979, 23971, 11907), fact(true), fact(true));
		Map<String, Integer> levels = new HashMap<>(base.levels.value()); levels.put("AGILITY", 65);
		AccountSnapshot low = new AccountSnapshot(fact(levels), base.inventory, base.equipment, base.bank, base.rigour, base.augury);
		Advisor.GoalView goal = advisor.goal(low, Preferences.EMPTY.goal("plan-team-cox"));
		assertEquals("AGILITY >= 70", goal.nearestMissing);
		assertTrue(goal.status.startsWith("Blocked:")); assertTrue(advisor.evaluate(low).isEmpty());
		assertTrue(goal.requirements.stream().filter(r -> r.label.startsWith("Recognised observed ranged")).allMatch(r -> r.truth == Truth.UNKNOWN));
	}

	@Test public void sourceUrlsAreIdenticalForEveryAccountAndPreference()
	{
		AccountSnapshot account = account(70, Set.of(4151, 9185, 11907, 21034), fact(false), fact(true));
		for (Preferences preferences : List.of(Preferences.EMPTY, Preferences.EMPTY.goal("train-rigour")))
		{
			for (Advisor.Recommendation recommendation : advisor.evaluate(account, preferences))
			{
				for (String id : recommendation.decision.sources)
				{
					String url = catalog.sources.get(id).url;
					assertTrue(url.matches("https://oldschool\\.runescape\\.wiki/w/[A-Za-z0-9_/%()]+\\?oldid=[0-9]+")
						|| url.matches("https://github\\.com/runelite/runelite/blob/[a-f0-9]{40}/runelite-api/src/main/java/net/runelite/api/gameval/ItemID\\.java"));
				}
			}
		}
	}
}
