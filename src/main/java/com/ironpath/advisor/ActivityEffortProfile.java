package com.ironpath.advisor;

import java.util.List;

/** Reviewed method assumptions shared by rewards; never a prediction of execution skill. */
public final class ActivityEffortProfile
{
	public String id, activity, mode, version, reviewed, scope, gap;
	public List<String> sources;
	public List<Method> methods;
	public List<Detour> detours = List.of();
	public static final class Method
	{
		public String id, label, limitation;
		public KnowledgeCatalog.Condition when;
		public RemainingEffort.Suitability suitability;
		public RemainingEffort.Band minimumBand;
	}
	public static final class Detour
	{
		public String upgrade, reason;
	}
}
