package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.model.Truth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.AdvisorTest.fact;
import static org.junit.Assert.*;

/** Boundary regressions, separate from representative product fixtures. */
public class CoxDecisionTest
{
	private KnowledgeCatalog catalog;
	private Advisor advisor;
	@Before public void setup() throws Exception { catalog = KnowledgeCatalog.load(new Gson()); advisor = new Advisor(catalog); }

	private AccountSnapshot account(Set<Integer> bank, Observation<Set<Integer>> equipment, Integer crafting)
	{
		Map<String, Integer> levels = new HashMap<>();
		for (String skill : List.of("ATTACK", "STRENGTH", "DEFENCE", "RANGED", "MAGIC", "HITPOINTS", "PRAYER", "AGILITY")) { levels.put(skill, 90); }
		if (crafting != null) { levels.put("CRAFTING", crafting); }
		return new AccountSnapshot(fact(levels), fact(Set.of()), equipment, fact(bank), fact(true), fact(true));
	}

	private boolean suggests(AccountSnapshot account, String id) { return advisor.evaluate(account).stream().anyMatch(r -> r.decision.id.equals(id)); }

	@Test public void assemblyNeedsKnownLevelsAndToolAndStopsForOwnedVariants()
	{
		Set<Integer> parts = Set.of(12932, 1755, 22290);
		assertTrue(suggests(account(parts, fact(Set.of()), 59), "assemble-swamp-trident"));
		assertFalse(suggests(account(parts, fact(Set.of()), 58), "assemble-swamp-trident"));
		assertFalse(suggests(account(parts, fact(Set.of()), null), "assemble-swamp-trident"));
		assertFalse(suggests(account(Set.of(12932, 22290), fact(Set.of()), 80), "assemble-swamp-trident"));
		for (int competing : List.of(12900, 22294, 22481, 25733, 27277, 31115, 30070))
		{
			assertFalse("Owned alternative " + competing, suggests(account(Set.of(12932, 1755, 22290, competing), fact(Set.of()), 80), "assemble-swamp-trident"));
		}
	}

	@Test public void currentEquipmentAndUnknownEquipmentDoNotInventSwitchGaps()
	{
		Set<Integer> kit = Set.of(22978, 4151, 12002, 11907);
		assertFalse(suggests(account(kit, Observation.unknown(), 80), "review-olm-lance"));
		assertFalse(suggests(account(kit, Observation.unknown(), 80), "review-occult-switch"));
		assertFalse(suggests(account(kit, fact(Set.of(22978, 12002)), 80), "review-olm-lance"));
		assertFalse(suggests(account(kit, fact(Set.of(22978, 12002)), 80), "review-occult-switch"));
		assertTrue(suggests(account(kit, fact(Set.of(6585)), 80), "review-occult-switch"));
		assertFalse(suggests(account(Set.of(22978, 4151, 22325), fact(Set.of()), 80), "review-olm-lance"));
	}

	@Test public void cosmeticAndEmptyFormsHaveDistinctUses()
	{
		AccountSnapshot account = account(Set.of(27246, 25896, 23975, 23979, 23971, 25731, 27100, 19720), fact(Set.of(12436)), 80);
		List<Advisor.Recommendation> advice = advisor.evaluate(account);
		assertTrue(suggests(account, "review-occult-switch"));
		assertTrue(suggests(account, "coordinate-defence-reduction"));
		assertTrue(advice.stream().flatMap(r -> r.evidence.stream()).anyMatch(e -> e.contains("elder maul (or) observed")));
		assertTrue(advice.stream().flatMap(r -> r.evidence.stream()).anyMatch(e -> e.contains("holy Sanguinesti staff observed")));
		// The uncharged form suppresses redundant assembly but cannot prove a usable powered spell.
		assertFalse(suggests(account(Set.of(4151, 9185, 25733), fact(Set.of()), 80), "plan-team-cox"));
		assertFalse(suggests(account(Set.of(4151, 9185, 30070), fact(Set.of()), 80), "plan-team-cox"));
	}

	@Test public void actualAllAnyConditionsKeepThreeValuedSemantics()
	{
		KnowledgeCatalog.Condition when = catalog.decisions.stream().filter(d -> d.id.equals("coordinate-defence-reduction")).findFirst().orElseThrow().when;
		AccountSnapshot known = account(Set.of(13576), fact(Set.of()), 80);
		// A known usable hammer makes ANY true even though the other storage is unknown.
		AccountSnapshot partial = new AccountSnapshot(known.levels, fact(Set.of(13576)), known.equipment,
			Observation.unknown(), known.rigour, known.augury);
		assertEquals(Truth.TRUE, advisor.truth(when, partial));
		partial = new AccountSnapshot(known.levels, fact(Set.of()), known.equipment, Observation.unknown(), known.rigour, known.augury);
		assertEquals(Truth.UNKNOWN, advisor.truth(when, partial));
		Map<String, Integer> low = new HashMap<>(known.levels.value()); low.put("ATTACK", 1); low.put("STRENGTH", 1);
		partial = new AccountSnapshot(fact(low), partial.inventory, partial.equipment, partial.bank, partial.rigour, partial.augury);
		assertEquals(Truth.FALSE, advisor.truth(when, partial));
	}
}
