package com.ironpath.encounter;

import java.util.Map;
import java.util.Set;
import java.util.Objects;

/** Immutable public-state sample. Empty slots are observed; unread slots are -1, never zero. */
public final class CombatState
{
	public final Map<String,Integer> slots, realLevels, visibleLevels;
	public final Set<String> prayers;
	public final Integer style, specialEnergy;
	public final boolean specialSelected;
	/** Null containers are unread, not empty. Raw resource counters do not establish usable stock. */
	public final Map<Integer, Long> inventory;
	public final Map<String, Integer> resources;
	public final Map<Integer, String> itemNames;
	public CombatState(Map<String,Integer> slots, Map<String,Integer> real, Map<String,Integer> visible,
		Set<String> prayers, Integer style, Integer energy, boolean selected)
	{ this(slots, real, visible, prayers, style, energy, selected, null, Map.of()); }

	public CombatState(Map<String,Integer> slots, Map<String,Integer> real, Map<String,Integer> visible,
		Set<String> prayers, Integer style, Integer energy, boolean selected,
		Map<Integer,Long> inventory, Map<String,Integer> resources)
	{ this(slots,real,visible,prayers,style,energy,selected,inventory,resources,Map.of()); }

	public CombatState(Map<String,Integer> slots, Map<String,Integer> real, Map<String,Integer> visible,
		Set<String> prayers, Integer style, Integer energy, boolean selected,
		Map<Integer,Long> inventory, Map<String,Integer> resources, Map<Integer,String> names)
	{
		this.slots=Map.copyOf(slots); realLevels=Map.copyOf(real); visibleLevels=Map.copyOf(visible);
		this.prayers=Set.copyOf(prayers); this.style=style; specialEnergy=energy; specialSelected=selected;
		this.inventory=inventory==null ? null : Map.copyOf(inventory); this.resources=Map.copyOf(resources);
		itemNames=Map.copyOf(names);
	}
	@Override public boolean equals(Object value)
	{
		if (!(value instanceof CombatState)) { return false; }
		CombatState s=(CombatState)value;
		return slots.equals(s.slots) && realLevels.equals(s.realLevels) && visibleLevels.equals(s.visibleLevels)
			&& prayers.equals(s.prayers) && Objects.equals(style,s.style)
			&& Objects.equals(specialEnergy,s.specialEnergy) && specialSelected==s.specialSelected
			&& Objects.equals(inventory,s.inventory) && resources.equals(s.resources) && itemNames.equals(s.itemNames);
	}
	@Override public int hashCode() { return Objects.hash(slots,realLevels,visibleLevels,prayers,style,specialEnergy,specialSelected,inventory,resources,itemNames); }
}
