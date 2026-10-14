package com.ironpath.advisor;

import java.util.List;

/** Immutable ranker output, including the evidence that produced the label and order. */
public final class PursuitAssessment
{
	public enum Category
	{
		PURSUE("Worth pursuing"), LONG_TERM("Long-term"), CHECK("Check first"), PLAN("Plan ahead"), DEFER("Small gain / Defer");
		public final String label;
		Category(String label) { this.label=label; }
	}
	public enum Mode { READY, BOUNDED, REPEATABLE, LONG_TAIL, UNKNOWN }
	public enum Burden { LOW, MODERATE, HIGH, UNKNOWN }
	public enum Tail { ORDINARY, LONG, UNKNOWN }
	public final String id, route, activity, mode, family, version, alternative;
	public final Category category;
	public final Mode acquisitionMode;
	public final Burden burden;
	public final Tail tail;
	public final List<String> reasonCodes, reasons, blockers, allocations;
	public final EffortEstimate estimate;
	public final ValueEvidence valueEvidence;
	public final RemainingEffort remainingEffort;
	public final ProgressionPriority.Priority priority;

	PursuitAssessment(RecommendationRanker.Candidate c, Category category, List<String> codes, List<String> reasons, String alternative)
	{
		id=c.id; route=c.route; activity=c.activity; mode=c.activityMode; family=c.family; version=RecommendationRanker.VERSION;
		valueEvidence=c.evidence; this.category=category; acquisitionMode=c.mode; burden=c.burden; tail=c.tail; this.alternative=alternative;
		remainingEffort=c.remainingEffort; priority=ProgressionPriority.of(c.importance(),remainingEffort,c.unknown,c.blocked,category==Category.DEFER);
		reasonCodes=List.copyOf(codes); this.reasons=List.copyOf(reasons); blockers=c.blockers; allocations=c.allocations; estimate=c.estimate;
	}
}
