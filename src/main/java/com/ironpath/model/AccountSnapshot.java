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
	public final Observation<Map<String, Integer>> equipmentSlots;
	public final Observation<Map<Integer, Long>> inventoryQuantities, equipmentQuantities, bankQuantities;

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
		this(levels, inventory, equipment, bank, rigour, augury, facts, Observation.unknown(),
			Observation.unknown(), Observation.unknown(), Observation.unknown());
	}

	public AccountSnapshot(Observation<Map<String, Integer>> levels,
		Observation<Set<Integer>> inventory, Observation<Set<Integer>> equipment,
		Observation<Set<Integer>> bank, Observation<Boolean> rigour, Observation<Boolean> augury,
		Map<String, Observation<Boolean>> facts, Observation<Map<String, Integer>> equipmentSlots,
		Observation<Map<Integer, Long>> inventoryQuantities, Observation<Map<Integer, Long>> equipmentQuantities,
		Observation<Map<Integer, Long>> bankQuantities)
	{
		this.equipmentSlots = copyMap(equipmentSlots);
		this.inventoryQuantities = copyMap(inventoryQuantities);
		this.equipmentQuantities = copyMap(equipmentQuantities);
		this.bankQuantities = copyMap(bankQuantities);
		this.facts = Map.copyOf(facts);
		this.levels = copyLevels(levels);
		this.inventory = copyItems(inventory);
		this.equipment = copyItems(equipment);
		this.bank = copyItems(bank);
		this.rigour = rigour;
		this.augury = augury;
	}

	private static <K, V> Observation<Map<K, V>> copyMap(Observation<Map<K, V>> fact)
	{ return fact.known() ? Observation.observed(Map.copyOf(fact.value()), fact.time(), fact.source()) : fact; }

	/** Conservative count from readable containers. Never establishes global absence or current supplies. */
	public Observation<Long> observedQuantity(int id)
	{
		long total = 0; java.time.Instant time = null;
		java.util.List<String> evidence = new java.util.ArrayList<>();
		for (Observation<Map<Integer, Long>> container : java.util.List.of(inventoryQuantities, equipmentQuantities, bankQuantities))
		{
			if (!container.known()) { continue; }
			// Observations made at different times can describe the same moved stack.
			// The largest observed container count is a conservative planning lower bound;
			// never add a stale bank count to a newer carried count.
			total = Math.max(total, container.value().getOrDefault(id, 0L));
			if (time == null || container.time().isBefore(time)) { time = container.time(); }
			evidence.add(container.evidence());
		}
		return time == null ? Observation.unknown() : Observation.observed(total, time,
			"largest observed container count; other amounts unknown; bank may be stale; " + String.join("; ", evidence));
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
