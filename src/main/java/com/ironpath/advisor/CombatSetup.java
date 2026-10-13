package com.ironpath.advisor;

import java.util.Map;

/** Complete memory-only reference. -1 is unknown, 0 explicitly excludes a slot.
 * Prayer, boost, supplies and attack style are assumptions, never live combat state. */
public class CombatSetup
{
	public final Map<String, Integer> slots;
	public final Map<String, String> provenance;
	public final PoweredMagic.Prayer prayer;
	public final PoweredMagic.Boost boost;
	public final PoweredMagic.Style style;
	public final String target;
	public final boolean confirmed, minimalTemplate;
	public CombatSetup(Map<String, Integer> slots, PoweredMagic.Prayer prayer, PoweredMagic.Boost boost,
		PoweredMagic.Style style, boolean confirmed, boolean template, String target, Map<String, String> provenance)
	{
		this.slots = Map.copyOf(slots); this.prayer = prayer; this.boost = boost; this.style = style;
		this.confirmed = confirmed; minimalTemplate = template; this.target = target;
		this.provenance = Map.copyOf(provenance);
	}
}
