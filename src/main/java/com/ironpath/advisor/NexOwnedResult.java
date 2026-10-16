package com.ironpath.advisor;

import com.ironpath.nex.NexSummary;
import java.util.List;

/** Frozen post-kill choices. Each row is an independent alternative, never an additive trip plan. */
public final class NexOwnedResult
{
	public static final class Choice
	{
		public final NexSummary.Sample actual;
		public final CombatSetup proposed;
		public final ComparisonResult comparison;
		public final String gap;
		public final boolean exhaustive;
		public final boolean bankPreparation;
		public final List<ComparisonResult> alternatives;
		public final List<String> coverage;
		Choice(NexSummary.Sample actual, CombatSetup proposed, ComparisonResult comparison, String gap,
			boolean exhaustive, List<String> coverage)
		{ this(actual,proposed,comparison,gap,exhaustive,coverage,false,List.of()); }
		Choice(NexSummary.Sample actual, CombatSetup proposed, ComparisonResult comparison, String gap,
			boolean exhaustive, List<String> coverage, boolean bankPreparation, List<ComparisonResult> alternatives)
		{
			this.actual=actual; this.proposed=proposed; this.comparison=comparison; this.gap=gap;
			this.exhaustive=exhaustive; this.coverage=List.copyOf(coverage);
			this.bankPreparation=bankPreparation; this.alternatives=List.copyOf(alternatives);
		}
		public String headline()
		{
			if(comparison==null || comparison.before==null) { return "Offence unavailable · "+gap; }
			return NexOwnedResult.headline(comparison);
		}
	}
	public static String headline(ComparisonResult comparison)
	{
		double delta=comparison.dpsGain(), before=comparison.before.dps;
		if(delta==0) { return "Same theoretical DPS · zero change"; }
		if(before==0) { return Math.abs(delta)<0.005?(delta<0?"−":"+")+"<0.01 theoretical DPS":String.format(java.util.Locale.ROOT,"%+.2f theoretical DPS",delta); }
		double percent=100*delta/before;
		return Math.abs(percent)<0.05?(percent<0?"−":"+")+"<0.1% theoretical DPS":String.format(java.util.Locale.ROOT,"%+.1f%% theoretical DPS",percent);
	}
	public final NexSummary summary;
	public final List<Choice> choices;
	public final com.ironpath.nex.NexResourceReview resources;
	public NexOwnedResult(NexSummary summary, List<Choice> choices)
	{
		this.summary=summary; this.choices=List.copyOf(choices);
		// Constructed with the frozen post-kill result on the existing guarded worker.
		resources=summary==null?null:com.ironpath.nex.NexResourceReview.from(summary);
	}
}
