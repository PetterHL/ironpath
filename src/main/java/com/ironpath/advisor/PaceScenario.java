package com.ironpath.advisor;

import java.time.Instant;

/** Explicit session-only scenario. This is never inferred from equipment or a personal best. */
public final class PaceScenario
{
	public enum SourceKind { USER_SCENARIO, MODEL_BASED }
	public final SourceKind sourceKind;
	public final String profile, version, methodKey;
	public final java.time.LocalDate reviewed;
	public final java.util.List<String> sources, assumptions, exclusions;
	public final double minimumPerHour, maximumPerHour;
	public final String activity, mode, scope, provenance;
	public final Instant entered;

	public PaceScenario(String activity, String mode, double minimumPerHour, double maximumPerHour,
		String scope, Instant entered)
	{ this(activity,mode,minimumPerHour,maximumPerHour,scope,entered,null); }
	public PaceScenario(String activity, String mode, double minimumPerHour, double maximumPerHour,
		String scope, Instant entered, String methodKey)
	{
		if (activity == null || mode == null || scope == null || scope.isBlank() || entered == null
			|| !Double.isFinite(minimumPerHour) || !Double.isFinite(maximumPerHour)
			|| minimumPerHour <= 0 || maximumPerHour < minimumPerHour)
		{ throw new IllegalArgumentException("Use positive eligible trials per active hour, or clear the scenario."); }
		this.methodKey=methodKey; sourceKind=SourceKind.USER_SCENARIO; profile=null; version=null; reviewed=null;
		sources=java.util.List.of(); assumptions=java.util.List.of(); exclusions=java.util.List.of();
		this.activity=activity; this.mode=mode; this.minimumPerHour=minimumPerHour; this.maximumPerHour=maximumPerHour;
		this.scope=scope; this.entered=entered; provenance="User-entered scenario; not measured performance";
	}
	PaceScenario(AutomaticPaceProfile p)
	{
		methodKey=p.id + "/" + p.version; sourceKind=SourceKind.MODEL_BASED; profile=p.id; version=p.version;
		reviewed=java.time.LocalDate.parse(p.reviewed); sources=java.util.List.copyOf(p.sources);
		assumptions=java.util.List.copyOf(p.assumptions); exclusions=java.util.List.copyOf(p.exclusions);
		activity=p.activity; mode=p.mode; scope=p.scope;
		minimumPerHour=p.minimumPerHour; maximumPerHour=p.maximumPerHour;
		entered=null; provenance="Model-based estimate; documented repeat-hour scenario, not measured performance";
	}
}
