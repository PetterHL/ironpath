package com.ironpath.advisor;

import java.time.Instant;

/** Explicit session-only scenario. This is never inferred from equipment or a personal best. */
public final class PaceScenario
{
	public enum SourceKind { USER_SCENARIO }
	public final SourceKind sourceKind=SourceKind.USER_SCENARIO;
	public final double minimumPerHour, maximumPerHour;
	public final String activity, mode, scope, provenance;
	public final Instant entered;

	public PaceScenario(String activity, String mode, double minimumPerHour, double maximumPerHour,
		String scope, Instant entered)
	{
		if (activity == null || mode == null || scope == null || scope.isBlank() || entered == null
			|| !Double.isFinite(minimumPerHour) || !Double.isFinite(maximumPerHour)
			|| minimumPerHour <= 0 || maximumPerHour < minimumPerHour)
		{ throw new IllegalArgumentException("Use positive eligible trials per active hour, or clear the scenario."); }
		this.activity=activity; this.mode=mode; this.minimumPerHour=minimumPerHour; this.maximumPerHour=maximumPerHour;
		this.scope=scope; this.entered=entered; provenance="User-entered scenario; not measured performance";
	}
}
