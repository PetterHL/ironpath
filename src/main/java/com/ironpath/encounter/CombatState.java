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
	public CombatState(Map<String,Integer> slots, Map<String,Integer> real, Map<String,Integer> visible,
		Set<String> prayers, Integer style, Integer energy, boolean selected)
	{
		this.slots=Map.copyOf(slots); realLevels=Map.copyOf(real); visibleLevels=Map.copyOf(visible);
		this.prayers=Set.copyOf(prayers); this.style=style; specialEnergy=energy; specialSelected=selected;
	}
	@Override public boolean equals(Object value)
	{
		if (!(value instanceof CombatState)) { return false; }
		CombatState s=(CombatState)value;
		return slots.equals(s.slots) && realLevels.equals(s.realLevels) && visibleLevels.equals(s.visibleLevels)
			&& prayers.equals(s.prayers) && Objects.equals(style,s.style)
			&& Objects.equals(specialEnergy,s.specialEnergy) && specialSelected==s.specialSelected;
	}
	@Override public int hashCode() { return Objects.hash(slots,realLevels,visibleLevels,prayers,style,specialEnergy,specialSelected); }
}
