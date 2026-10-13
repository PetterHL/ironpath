package com.ironpath.advisor;

import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Original synthetic accounts; no player data or asserted game-client accuracy. */
public final class P4aFixtures
{
	public static final Instant TIME = Instant.parse("2026-10-12T20:00:00Z");
	public static <T> Observation<T> fact(T value) { return Observation.observed(value, TIME, "synthetic fixture"); }
	public static AccountSnapshot account(Set<Integer> items, int magic, int slayer)
	{
		Map<String, Integer> levels = new HashMap<>();
		for (String key : List.of("ATTACK", "STRENGTH", "DEFENCE", "RANGED", "PRAYER", "HITPOINTS", "AGILITY", "CRAFTING", "HERBLORE", "RUNECRAFT")) { levels.put(key, 90); }
		levels.put("MAGIC", magic); levels.put("SLAYER", slayer);
		return new AccountSnapshot(fact(levels), fact(Set.of()), fact(Set.of()), fact(items), fact(true), fact(true),
			Map.of("regicide", fact(true), "song-of-elves", fact(true), "final-dawn", fact(false), "frozen-door", fact(false)));
	}
	public static AccountSnapshot levels(AccountSnapshot a, String key, int level)
	{
		Map<String, Integer> levels = new HashMap<>(a.levels.value()); levels.put(key, level);
		return new AccountSnapshot(fact(levels), a.inventory, a.equipment, a.bank, a.rigour, a.augury, a.facts);
	}
	public static AccountSnapshot facts(AccountSnapshot a, String key, Observation<Boolean> value)
	{
		Map<String, Observation<Boolean>> facts = new HashMap<>(a.facts); facts.put(key, value);
		return new AccountSnapshot(a.levels, a.inventory, a.equipment, a.bank, a.rigour, a.augury, facts);
	}
}
