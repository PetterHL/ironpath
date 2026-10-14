package com.ironpath.advisor;

/** Versioned product calibration. Cells are priority classes, not additive utility or time scores. */
public final class ProgressionPriority
{
	public enum Priority
	{
		NEXT("Strong next step"), WORTHWHILE("Worthwhile next step"), LATER("Later opportunity"),
		LONG_HORIZON("Long horizon"), CONDITIONAL("Conditional"), OPTIONAL("Optional / covered");
		public final String label;
		Priority(String label) { this.label=label; }
	}
	// SHORT, MEDIUM, LONG, VERY_LONG, UNKNOWN; KEY, STRONG, CONTEXTUAL, MARGINAL, UNCERTAIN.
	private static final Priority[][] TABLE={
		{Priority.NEXT,Priority.NEXT,Priority.WORTHWHILE,Priority.LONG_HORIZON,Priority.CONDITIONAL},
		{Priority.NEXT,Priority.WORTHWHILE,Priority.LATER,Priority.LONG_HORIZON,Priority.CONDITIONAL},
		{Priority.WORTHWHILE,Priority.LATER,Priority.LONG_HORIZON,Priority.LONG_HORIZON,Priority.CONDITIONAL},
		{Priority.OPTIONAL,Priority.OPTIONAL,Priority.OPTIONAL,Priority.OPTIONAL,Priority.OPTIONAL},
		{Priority.CONDITIONAL,Priority.CONDITIONAL,Priority.CONDITIONAL,Priority.CONDITIONAL,Priority.CONDITIONAL}
	};
	public static Priority of(ValueEvidence.Importance value, RemainingEffort effort, boolean unknown, boolean blocked, boolean dominated)
	{
		if (dominated || value==ValueEvidence.Importance.MARGINAL) { return Priority.OPTIONAL; }
		if (unknown || blocked || effort.suitability==RemainingEffort.Suitability.UNKNOWN || effort.suitability==RemainingEffort.Suitability.PREPARE)
		{ return Priority.CONDITIONAL; }
		return TABLE[value.ordinal()][effort.band.ordinal()];
	}
}
