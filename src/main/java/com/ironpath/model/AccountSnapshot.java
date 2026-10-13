package com.ironpath.model;

import java.util.Map;
import java.util.Set;

/** Immutable, deliberately excludes character identifiers. FALSE possession means only
 * not observed in these containers; it never establishes global nonownership. */
public final class AccountSnapshot
{
	public final Observation<Map<String, Integer>> levels;
	public final Observation<Set<Integer>> inventory;
	public final Observation<Set<Integer>> equipment;
	public final Observation<Set<Integer>> bank;
	public final Observation<Boolean> rigour;
	public final Observation<Boolean> augury;
	public final Map<String, Observation<Boolean>> facts;

	public AccountSnapshot(Observation<Map<String, Integer>> levels,
		Observation<Set<Integer>> inventory, Observation<Set<Integer>> equipment,
		Observation<Set<Integer>> bank, Observation<Boolean> rigour, Observation<Boolean> augury)
	{
		this(levels, inventory, equipment, bank, rigour, augury, Map.of());
	}

	public AccountSnapshot(Observation<Map<String, Integer>> levels,
		Observation<Set<Integer>> inventory, Observation<Set<Integer>> equipment,
		Observation<Set<Integer>> bank, Observation<Boolean> rigour, Observation<Boolean> augury,
		Map<String, Observation<Boolean>> facts)
	{
		this.facts = Map.copyOf(facts);
		this.levels = copyLevels(levels);
		this.inventory = copyItems(inventory);
		this.equipment = copyItems(equipment);
		this.bank = copyItems(bank);
		this.rigour = rigour;
		this.augury = augury;
	}

	private static Observation<Map<String, Integer>> copyLevels(Observation<Map<String, Integer>> fact)
	{
		return fact.known() ? Observation.observed(Map.copyOf(fact.value()), fact.time(), fact.source()) : fact;
	}

	private static Observation<Set<Integer>> copyItems(Observation<Set<Integer>> fact)
	{
		return fact.known() ? Observation.observed(Set.copyOf(fact.value()), fact.time(), fact.source()) : fact;
	}

	public Observation<Boolean> fact(String key)
	{
		if (key.equals("rigour")) { return rigour; }
		if (key.equals("augury")) { return augury; }
		return facts.getOrDefault(key, Observation.unknown());
	}

	public Integer level(String skill)
	{
		return levels.known() ? levels.value().get(skill) : null;
	}

	public Truth atLeast(String skill, int minimum)
	{
		Integer level = level(skill);
		return level == null ? Truth.UNKNOWN : level >= minimum ? Truth.TRUE : Truth.FALSE;
	}

	public Truth observedItem(int id)
	{
		return Truth.any(inContainer(inventory, id), inContainer(equipment, id), inContainer(bank, id));
	}

	private static Truth inContainer(Observation<Set<Integer>> fact, int id)
	{
		return !fact.known() ? Truth.UNKNOWN : fact.value().contains(id) ? Truth.TRUE : Truth.FALSE;
	}

	public String itemEvidence(int id)
	{
		for (Observation<Set<Integer>> fact : java.util.List.of(inventory, equipment, bank))
		{
			if (fact.known() && fact.value().contains(id)) { return fact.evidence(); }
		}
		return "not observed; other storage is unknown";
	}
}
