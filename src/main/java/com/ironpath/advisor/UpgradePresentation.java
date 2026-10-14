package com.ironpath.advisor;

import java.util.List;

/** Typed normal-flow data shared by the current UI and UX2; diagnostic evidence stays separate. */
public final class UpgradePresentation
{
	public enum Availability { AVAILABLE, TRUE_ZERO, INPUT_REQUIRED, MODEL_UNSUPPORTED, QUALITATIVE }
	public enum RangeKind { SCENARIO_MEAN_SENSITIVITY, UNAVAILABLE }
	public static final class Metric
	{
		public final ComparisonResult.Metric kind;
		public final Availability availability;
		public final String unit, baseline, context, reason, action;
		public final Double before, after, delta;
		Metric(ComparisonResult comparison, String baseline, boolean qualitative)
		{
			kind=comparison.primaryMetric; this.baseline=baseline; context=comparison.target==null?"Reviewed role":comparison.target.name;
			unit=kind==ComparisonResult.Metric.THEORETICAL_DPS?"damage / second":"damage";
			boolean available=!qualitative && comparison.before!=null && comparison.available(kind);
			before=available?(kind==ComparisonResult.Metric.THEORETICAL_DPS?comparison.before.dps:(double)comparison.before.maxHit):null;
			after=available?(kind==ComparisonResult.Metric.THEORETICAL_DPS?comparison.after.dps:(double)comparison.after.maxHit):null;
			delta=available?after-before:null;
			availability=qualitative?Availability.QUALITATIVE:available?delta==0?Availability.TRUE_ZERO:Availability.AVAILABLE
				:comparison.beforeSetup!=null && (comparison.beforeSetup.slots.values().stream().anyMatch(i->i<0)||comparison.beforeSetup.loadedAmmo<0)
					?Availability.INPUT_REQUIRED:Availability.MODEL_UNSUPPORTED;
			reason=available?"Supported selected reference":comparison.gaps.getOrDefault(kind,comparison.reason);
			action=availability==Availability.INPUT_REQUIRED?"Correct reference inputs":availability==Availability.MODEL_UNSUPPORTED?"Inspect the model gap":"Inspect supported role / context";
		}
	}
	public static final class RouteChoice
	{
		public final String method, preparationUpgrade, reason, scope;
		public final RemainingEffort.Band preparationBand, acquisitionBand;
		public final boolean preparationOwned;
		public final Double totalHours=null;
		RouteChoice(String method, String preparation, boolean owned, RemainingEffort.Band preparationBand,
			RemainingEffort.Band acquisitionBand,String reason)
		{
			this.method=method; preparationUpgrade=preparation; preparationOwned=owned; this.preparationBand=preparationBand;
			this.acquisitionBand=acquisitionBand; this.reason=reason; scope="Separate preparation and reward work; no combined time saving established";
		}
	}
	public final Metric primaryMetric;
	public final RemainingEffort effort;
	public final ProgressionPriority.Priority priority;
	public final String status, priorityReason, nextStep, estimateOrigin, estimateScope;
	public final RangeKind rangeKind;
	public final Double minimumMeanHours, maximumMeanHours;
	public final List<RouteChoice> routes;
	UpgradePresentation(Advisor.UpgradeView view, List<RouteChoice> routes)
	{
		primaryMetric=new Metric(view.comparison,view.valueEvidence.baseline,view.valueEvidence.independentUtility);
		effort=view.pursuit.remainingEffort; priority=view.pursuit.priority;
		status=view.pursuit.category.label;
		priorityReason=view.valueEvidence.importance.label + " · " + effort.band.label.toLowerCase(java.util.Locale.ROOT) + " remaining effort";
		nextStep=view.pursuit.blockers.isEmpty()?view.routes.stream().filter(r->r.decision.id.equals(view.pursuit.route)).findFirst().orElseThrow().next:view.pursuit.blockers.get(0);
		EffortEstimate estimate=view.pursuit.estimate;
		boolean hours=estimate!=null && estimate.meanHours()!=null;
		rangeKind=hours?RangeKind.SCENARIO_MEAN_SENSITIVITY:RangeKind.UNAVAILABLE;
		minimumMeanHours=hours?estimate.meanTrials/estimate.pace.maximumPerHour:null;
		maximumMeanHours=hours?estimate.meanHours():null;
		estimateOrigin=hours?estimate.pace.provenance:"Reviewed qualitative activity profile";
		estimateScope=estimate==null?effort.scope:estimate.remainingOutcome + "; " + (hours?estimate.pace.scope:"no hours available");
		this.routes=List.copyOf(routes);
	}
}
