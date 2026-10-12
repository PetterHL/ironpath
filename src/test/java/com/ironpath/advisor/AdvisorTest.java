package com.ironpath.advisor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.model.Truth;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Synthetic account observations, never shipped as advice or captured from a player. */
public class AdvisorTest
{
	private static final Instant TIME = Instant.parse("2026-10-12T10:00:00Z");
	private KnowledgeCatalog catalog;
	private Advisor advisor;

	@Before public void setup() throws Exception
	{
		catalog = KnowledgeCatalog.load(new Gson());
		advisor = new Advisor(catalog);
	}

	public static <T> Observation<T> fact(T value)
	{
		return Observation.observed(value, TIME, "synthetic test observation");
	}

	public static AccountSnapshot account(int prayer, Observation<Set<Integer>> bank,
		Observation<Boolean> rigour, Observation<Boolean> augury)
	{
		Map<String, Integer> levels = new HashMap<>();
		for (String skill : List.of("ATTACK", "STRENGTH", "RANGED", "MAGIC", "HITPOINTS", "DEFENCE", "AGILITY")) { levels.put(skill, 90); }
		levels.put("PRAYER", prayer);
		return new AccountSnapshot(fact(levels), fact(Set.of()), fact(Set.of()), bank, rigour, augury);
	}

	private List<String> ids(AccountSnapshot account)
	{
		return advisor.evaluate(account).stream().map(r -> r.decision.id).collect(Collectors.toList());
	}

	@Test public void unopenedBankAndUnlocksRemainUnknown()
	{
		AccountSnapshot account = account(70, Observation.unknown(), Observation.unknown(), Observation.unknown());
		assertEquals(Truth.UNKNOWN, account.observedItem(21034));
		assertTrue(ids(account).isEmpty());
		// Positive scroll evidence still cannot resolve an unobserved completion condition.
		assertTrue(ids(account(80, fact(Set.of(21034)), Observation.unknown(), fact(false))).isEmpty());
	}

	@Test public void midgameDecliningCgCanPlanWithObservedCrossbow()
	{
		AccountSnapshot account = account(70, fact(Set.of(4151, 9185, 11907)), fact(false), fact(false));
		assertEquals(List.of("plan-team-cox"), ids(account));
		Advisor.Recommendation suggestion = advisor.evaluate(account).get(0);
		assertTrue(suggestion.evidence.stream().anyMatch(e -> e.contains("rune crossbow")));
		assertTrue(suggestion.decision.alternative.contains("decline CG"));
		assertTrue(suggestion.decision.tradeoff.contains("does not establish raid access"));
		assertTrue(catalog.decisions.stream().noneMatch(d -> d.id.contains("gauntlet")));
	}

	@Test public void postBowfaRaiderGetsRemainingAuguryLevelGap()
	{
		AccountSnapshot account = account(74, fact(Set.of(25867, 23975, 23979, 23971, 4151, 11907)), fact(true), fact(true));
		assertEquals(List.of("train-augury", "plan-team-cox"), ids(account));
		assertTrue(advisor.evaluate(account).get(0).evidence.stream().anyMatch(e -> e.contains("Prayer 74 < 77")));
		assertTrue(advisor.evaluate(account).get(1).evidence.stream().anyMatch(e -> e.contains("Bow of faerdhinen")));
		assertTrue(advisor.evaluate(account).get(0).decision.alternative.contains("do not need another arcane scroll"));
	}

	@Test public void advancedGearDoesNotEraseConfirmedPrayerGapOrDuplicateTraining()
	{
		AccountSnapshot account = account(72, fact(Set.of(26219, 20997, 27275)), fact(true), fact(true));
		assertEquals(List.of("train-rigour", "plan-team-cox"), ids(account));
		List<String> evidence = advisor.evaluate(account).get(1).evidence;
		assertTrue(evidence.stream().anyMatch(e -> e.contains("Tumeken's shadow")));
		assertTrue(evidence.stream().anyMatch(e -> e.contains("twisted bow")));
		assertEquals(ids(account), ids(account));
	}

	@Test public void consumedUnlockWinsOverScrollAbsenceOrSpareScroll()
	{
		assertTrue(ids(account(80, fact(Set.of()), fact(true), fact(true))).isEmpty());
		assertTrue(ids(account(80, fact(Set.of(21034, 21079)), fact(true), fact(true))).isEmpty());
		AccountSnapshot account = account(80, fact(Set.of(21034, 21079)), fact(false), fact(false));
		assertEquals(List.of("unlock-augury", "unlock-rigour"), ids(account));
		assertTrue(advisor.evaluate(account).get(0).evidence.get(0).contains("synthetic test observation at"));
	}

	@Test public void readingScrollDoesNotInventSkillRequirement()
	{
		AccountSnapshot account = account(30, fact(Set.of(21034)), fact(false), Observation.unknown());
		assertEquals(List.of("unlock-rigour"), ids(account));
		assertTrue(advisor.evaluate(account).get(0).evidence.stream().anyMatch(e -> e.contains("no skill requirement")));
	}

	@Test public void inactiveGearAndComponentsAreNotUsableVariants()
	{
		assertTrue(ids(account(80, fact(Set.of(4151, 9185, 11908)), fact(true), fact(true))).isEmpty());
		assertTrue(ids(account(80, fact(Set.of(4151, 25862, 23975, 23979, 23971, 11907)), fact(true), fact(true))).isEmpty());
		assertTrue(ids(account(80, fact(Set.of(4151, 25867, 23975, 23979, 11907)), fact(true), fact(true))).isEmpty());
		assertTrue(ids(account(80, fact(Set.of(12004, 9185, 11907)), fact(true), fact(true))).isEmpty());
	}

	@Test public void strongerWeaponRequiresItsOwnObservedWieldLevel()
	{
		AccountSnapshot original = account(80, fact(Set.of(4151, 20997, 27275)), fact(true), fact(true));
		Map<String, Integer> levels = new HashMap<>(original.levels.value());
		levels.put("MAGIC", 80);
		AccountSnapshot account = new AccountSnapshot(fact(levels), original.inventory, original.equipment, original.bank, original.rigour, original.augury);
		assertTrue(ids(account).isEmpty());
	}

	@Test public void noAdviceExplainsUnchargedMagicWithoutInventingMissingOwnership()
	{
		AccountSnapshot account = account(90, fact(Set.of(20997, 22978, 11908)), fact(true), fact(true));
		assertTrue(advisor.evaluate(account).isEmpty());
		List<String> reasons = advisor.explainNoAdvice(account);
		assertTrue(reasons.stream().anyMatch(r -> r.contains("Rigour: already unlocked")));
		assertTrue(reasons.stream().anyMatch(r -> r.contains("Augury: already unlocked")));
		assertTrue(reasons.stream().anyMatch(r -> r.contains("magic weapon") && r.contains("charged powered-staff")));
		assertTrue(reasons.stream().anyMatch(r -> r.contains("Other storage remains unknown")));
		assertTrue(reasons.stream().noneMatch(r -> r.contains("melee weapon") || r.contains("ranged weapon")));
		assertTrue(reasons.stream().noneMatch(r -> r.toLowerCase(java.util.Locale.ROOT).contains("obtain")));
		// Observing a charged supported form enables the existing sample, not a new grind.
		AccountSnapshot charged = account(90, fact(Set.of(20997, 22978, 11907)), fact(true), fact(true));
		assertEquals(List.of("plan-team-cox"), ids(charged));
		assertTrue(advisor.explainNoAdvice(charged).isEmpty());
	}

	@Test public void noAdvicePreservesUnknownBankAndSkillReasons()
	{
		AccountSnapshot original = account(90, Observation.unknown(), fact(true), fact(true));
		Map<String, Integer> levels = new HashMap<>(original.levels.value());
		levels.remove("ATTACK");
		AccountSnapshot account = new AccountSnapshot(fact(levels), original.inventory, original.equipment, original.bank, original.rigour, original.augury);
		List<String> reasons = advisor.explainNoAdvice(account);
		assertTrue(reasons.stream().anyMatch(r -> r.contains("ATTACK unknown")));
		assertTrue(reasons.stream().anyMatch(r -> r.contains("Unread containers remain unknown")));
		assertTrue(reasons.stream().noneMatch(r -> r.contains("ATTACK 0")));
	}

	@Test public void noAdviceExplainsBowfaPackageCoverageAndOwnWieldLevel()
	{
		AccountSnapshot original = account(90, fact(Set.of(4151, 25867, 11907)), fact(true), fact(true));
		List<String> bowfaReasons = advisor.explainNoAdvice(original);
		assertTrue(bowfaReasons.stream().anyMatch(r -> r.contains("Bowfa observed") && r.contains("default-colour armour only")));
		assertTrue(bowfaReasons.stream().noneMatch(r -> r.contains("no covered ranged weapon")));
		Map<String, Integer> levels = new HashMap<>(original.levels.value());
		levels.put("RANGED", 80);
		AccountSnapshot lowLevel = new AccountSnapshot(fact(levels), fact(Set.of()), fact(Set.of()), fact(Set.of(4151, 20997, 11907)), fact(true), fact(true));
		assertTrue(advisor.explainNoAdvice(lowLevel).stream().anyMatch(r -> r.contains("twisted bow observed") && r.contains("85 wield level")));
	}

	@Test public void immutableSnapshotKeepsOriginalEvidence()
	{
		Set<Integer> items = new HashSet<>(Set.of(21034));
		Map<String, Integer> levels = new HashMap<>(Map.of("PRAYER", 74));
		AccountSnapshot account = new AccountSnapshot(fact(levels), fact(items), fact(Set.of()), Observation.unknown(), fact(false), fact(false));
		items.clear(); levels.put("PRAYER", 1);
		assertEquals(Truth.TRUE, account.observedItem(21034));
		assertEquals(Integer.valueOf(74), account.level("PRAYER"));
		assertEquals(TIME, account.inventory.time());
	}

	@Test public void threeValuedCompositionDoesNotFlattenUnknown()
	{
		assertEquals(Truth.FALSE, Truth.all(Truth.UNKNOWN, Truth.FALSE));
		assertEquals(Truth.UNKNOWN, Truth.all(Truth.TRUE, Truth.UNKNOWN));
		assertEquals(Truth.TRUE, Truth.any(Truth.UNKNOWN, Truth.TRUE));
		assertEquals(Truth.UNKNOWN, Truth.any(Truth.FALSE, Truth.UNKNOWN));
	}

	private JsonObject document()
	{
		return new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/catalog.json"), StandardCharsets.UTF_8)).getAsJsonObject();
	}

	@Test public void unreviewedCandidateCannotBecomeAdvice()
	{
		JsonObject doc = document();
		doc.getAsJsonArray("decisions").get(0).getAsJsonObject().addProperty("status", "candidate");
		Advisor candidateAdvisor = new Advisor(KnowledgeCatalog.read(new Gson(), new StringReader(doc.toString())));
		assertTrue(candidateAdvisor.evaluate(account(80, fact(Set.of(21034)), fact(false), fact(true))).isEmpty());
	}

	@Test public void invalidCatalogueFailsClosed()
	{
		JsonObject doc = document();
		doc.getAsJsonArray("decisions").get(0).getAsJsonObject().addProperty("kind", "DOWNLOAD_RULES");
		String invalidKind = doc.toString();
		assertThrows(IllegalArgumentException.class, () -> KnowledgeCatalog.read(new Gson(), new StringReader(invalidKind)));
		doc = document();
		doc.getAsJsonArray("sources").get(0).getAsJsonObject().addProperty("url", "https://example.com/account");
		String invalidSource = doc.toString();
		assertThrows(IllegalArgumentException.class, () -> KnowledgeCatalog.read(new Gson(), new StringReader(invalidSource)));
	}
}
