package com.ironpath.advisor;

import java.util.List;

/** Bundled, source-dependent repeat-hour scenario. Applicability never proves execution speed. */
public final class AutomaticPaceProfile
{
	public String id, activity, mode, version, reviewed, scope, methodLabel;
	public AcquisitionModel.Support support;
	public double minimumPerHour, maximumPerHour;
	public KnowledgeCatalog.Condition when;
	public List<String> sources, assumptions, exclusions;
	public PaceScenario scenario()
	{
		return new PaceScenario(this);
	}
}
