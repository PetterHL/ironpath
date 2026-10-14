package com.ironpath.encounter;

import java.util.Map;

/** Scope, not a universal boss schema. Null optional values mean unknown/not observed. */
public final class EncounterContext
{
	public final String activity, run, encounter, phase, mode, role;
	public final Integer partySize;
	public final Map<String, Integer> scaling;
	public EncounterContext(String activity, String run, String encounter, String phase,
		String mode, Integer partySize, String role, Map<String, Integer> scaling)
	{
		this.activity=activity; this.run=run; this.encounter=encounter; this.phase=phase;
		this.mode=mode; this.partySize=partySize; this.role=role; this.scaling=Map.copyOf(scaling);
	}
}
