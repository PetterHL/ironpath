package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.AdvisorTest.fact;
import static org.junit.Assert.*;

/** Five illustrative accounts, not live-account captures or player usefulness evidence. */
public class CoxAccountFixturesTest
{
	private Advisor advisor;
	@Before public void setup() throws Exception { advisor = new Advisor(KnowledgeCatalog.load(new Gson())); }

	private AccountSnapshot account(int combat, int prayer, int crafting, Observation<Set<Integer>> bank, Set<Integer> equipment)
	{
		Map<String, Integer> levels = new HashMap<>();
		for (String skill : List.of("ATTACK", "STRENGTH", "DEFENCE", "RANGED", "MAGIC", "HITPOINTS")) { levels.put(skill, combat); }
		levels.put("PRAYER", prayer); levels.put("CRAFTING", crafting); levels.put("AGILITY", 75);
		return new AccountSnapshot(fact(levels), fact(Set.of()), fact(equipment), bank, fact(true), fact(true));
	}

	private Advisor.Recommendation find(List<Advisor.Recommendation> advice, String id)
	{
		return advice.stream().filter(r -> r.decision.id.equals(id)).findFirst().orElseThrow();
	}

	@Test public void unopenedBankCorrectlyAbstains()
	{
		AccountSnapshot account = account(85, 77, 75, Observation.unknown(), Set.of());
		assertTrue(advisor.evaluate(account).isEmpty());
		// Even positive parts do not prove the account lacks a competing magic weapon.
		account = new AccountSnapshot(account.levels, fact(Set.of(12932, 1755, 11908)), account.equipment,
			Observation.unknown(), account.rigour, account.augury);
		assertTrue(advisor.evaluate(account).isEmpty());
	}

	@Test public void midgameDecliningCgHasAnOrdinaryTeamPlanningOption()
	{
		AccountSnapshot account = account(85, 70, 75, fact(Set.of(4151, 9185, 11907)), Set.of());
		account = new AccountSnapshot(account.levels, account.inventory, account.equipment, account.bank, fact(false), fact(false));
		List<Advisor.Recommendation> advice = advisor.evaluate(account);
		assertEquals(1, advice.size());
		Advisor.Recommendation plan = find(advice, "plan-team-cox");
		assertTrue(plan.evidence.stream().anyMatch(e -> e.contains("rune crossbow")));
		assertTrue(plan.decision.alternative.contains("decline CG"));
		assertTrue(plan.decision.access.contains("Great Kourend, which is unobserved"));
		assertTrue(plan.decision.applicability.contains("not game access"));
	}

	@Test public void postBowfaAccountCanFinishAnOwnedMagicUpgrade()
	{
		AccountSnapshot account = account(90, 80, 80,
			fact(Set.of(25884, 23975, 23979, 23971, 4151, 11907, 12932, 1755)), Set.of());
		List<Advisor.Recommendation> advice = advisor.evaluate(account);
		assertEquals(2, advice.size());
		Advisor.Recommendation assembly = find(advice, "assemble-swamp-trident");
		assertTrue(assembly.evidence.stream().anyMatch(e -> e.contains("magic fang observed")));
		assertTrue(assembly.evidence.stream().anyMatch(e -> e.contains("Crafting 80 >= 59")));
		assertTrue(assembly.decision.alternative.contains("scale upkeep"));
		assertTrue(assembly.decision.tradeoff.contains("Uncharge"));
		assertTrue(find(advice, "plan-team-cox").evidence.stream().anyMatch(e -> e.contains("Bow of faerdhinen")));
		assertTrue(advice.stream().noneMatch(r -> r.decision.id.contains("unlock") || r.decision.id.contains("train")));
	}

	@Test public void establishedRaiderCanReviewSwitchesAndTeamCoverage()
	{
		AccountSnapshot account = account(93, 80, 85,
			fact(Set.of(25867, 23975, 23979, 23971, 4151, 12899, 13576, 12002)), Set.of(6585));
		List<Advisor.Recommendation> advice = advisor.evaluate(account);
		assertEquals(2, advice.size());
		Advisor.Recommendation neck = find(advice, "review-occult-switch");
		assertTrue(neck.evidence.stream().anyMatch(e -> e.contains("fury currently equipped")));
		assertTrue(neck.evidence.stream().anyMatch(e -> e.contains("trident of the swamp observed")));
		assertTrue(neck.decision.alternative.contains("simpler hybrid switch"));
		assertTrue(neck.decision.tradeoff.contains("not proof"));
		Advisor.Recommendation team = find(advice, "coordinate-defence-reduction");
		assertTrue(team.evidence.stream().anyMatch(e -> e.contains("dragon warhammer observed")));
		assertTrue(team.decision.alternative.contains("Bandos godsword and elder maul"));
		assertTrue(team.decision.tradeoff.contains("coverage are unknown"));
		assertTrue(advice.stream().noneMatch(r -> r.decision.id.equals("plan-team-cox") || r.decision.id.equals("assemble-swamp-trident")));
	}

	@Test public void establishedAccountWithAyakAndLanceGetsContextualComparisons()
	{
		AccountSnapshot account = account(95, 90, 85,
			fact(Set.of(25867, 23975, 23979, 23971, 26219, 22978, 31113, 11804, 12932, 1755, 11908)), Set.of(19720));
		List<Advisor.Recommendation> advice = advisor.evaluate(account);
		assertEquals(2, advice.size());
		Advisor.Recommendation lance = find(advice, "review-olm-lance");
		assertTrue(lance.evidence.stream().anyMatch(e -> e.contains("Osmumten's fang observed")));
		assertTrue(lance.decision.why.contains("Olm"));
		assertTrue(lance.decision.alternative.contains("scythe or Soulreaper axe"));
		assertTrue(lance.decision.access.contains("miniquest sections are unknown"));
		Advisor.Recommendation team = find(advice, "coordinate-defence-reduction");
		assertTrue(team.evidence.stream().anyMatch(e -> e.contains("Eye of Ayak (charged) observed")));
		assertTrue(team.evidence.stream().anyMatch(e -> e.contains("Bandos godsword observed")));
		assertTrue(advice.stream().noneMatch(r -> r.decision.id.equals("review-occult-switch") || r.decision.id.equals("assemble-swamp-trident")));
	}
}
