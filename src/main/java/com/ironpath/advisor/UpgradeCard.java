package com.ironpath.advisor;

import java.util.Locale;

/** Evaluated card copy; Swing only measures and renders these fields. */
public final class UpgradeCard
{
	public final String reason, metric, status, effort, priorityReason;
	public final UpgradePresentation presentation;
	public UpgradeCard(Advisor.UpgradeView view)
	{
		reason=view.valueEvidence.reason;
		presentation=view.presentation; effort=presentation.effort.band.label + " remaining effort"; priorityReason=presentation.priorityReason;
		ComparisonResult comparison=view.comparison;
		if (view.valueEvidence.independentUtility)
		{
			metric=view.valueEvidence.roles.stream().filter(r->r.applicable==com.ironpath.model.Truth.TRUE && !r.covered
				&& r.dimension!=ProgressionKnowledge.Dimension.ORDINARY_OFFENCE)
				.map(r->r.dimension==ProgressionKnowledge.Dimension.BURST ? "Burst not quantified · HP/energy check"
					: r.dimension==ProgressionKnowledge.Dimension.INVENTORY ? "Slot savings not quantified · method check"
					: r.dimension==ProgressionKnowledge.Dimension.CONTROL ? "Demonbane/control · DPS model gap"
					: r.dimension==ProgressionKnowledge.Dimension.MULTI_TARGET || r.dimension==ProgressionKnowledge.Dimension.CONVENIENCE ? "Bounce DPS unavailable · geometry gap"
					: "Reviewed resource value · numeric model gap").findFirst().orElse("Role value · numeric model gap");
		}
		else if (view.valueEvidence.specialist && comparison.available(ComparisonResult.Metric.THEORETICAL_DPS) && comparison.dpsGain()<=1e-10)
		{
			metric=view.contextComparisons.stream().filter(c->c.available(ComparisonResult.Metric.THEORETICAL_DPS)&&c.dpsGain()>1e-10)
				.map(UpgradeCard::metric).findFirst().orElse(metric(comparison));
		}
		else { metric=metric(comparison); }
		String check=view.pursuit.blockers.isEmpty() ? "" : view.pursuit.blockers.get(0);
		if (check.contains(": ")) { check=check.substring(check.indexOf(": ")+2); }
		status=view.pursuit.category.label + (view.pursuit.tail==PursuitAssessment.Tail.LONG ? " · long tail" : "") + (!view.questions.isEmpty() ? " · ownership unconfirmed"
			: view.pursuit.category==PursuitAssessment.Category.CHECK || view.pursuit.category==PursuitAssessment.Category.PLAN ? " · " + check
			: "");
	}
	private static String metric(ComparisonResult result)
	{
		String target=result.target==null ? "reference" : result.target.name;
		if (result.primaryMetric==ComparisonResult.Metric.THEORETICAL_DPS)
		{ return result.available(ComparisonResult.Metric.THEORETICAL_DPS) ? String.format(Locale.ROOT,"DPS %+.2f · %s",result.dpsGain(),target) : "DPS unavailable · model gap"; }
		if (result.available(ComparisonResult.Metric.MAX_HIT))
		{ return String.format(Locale.ROOT,"Max hit %+d · %s",result.after.maxHit-result.before.maxHit,target); }
		return "DPS unavailable · model gap";
	}
}
