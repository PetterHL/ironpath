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
		{ this(comparison, comparison.primaryMetric, baseline, qualitative); }
		Metric(ComparisonResult comparison, ComparisonResult.Metric metric, String baseline, boolean qualitative)
		{ this(comparison,metric,baseline,qualitative,null); }
		Metric(ComparisonResult comparison, ComparisonResult.Metric metric,String baseline,boolean qualitative,com.ironpath.model.AccountSnapshot account)
		{
			kind=metric; this.baseline=baseline; context=comparison.target==null?"Reviewed role":comparison.target.name;
			unit=kind==ComparisonResult.Metric.THEORETICAL_DPS?"DPS":kind==ComparisonResult.Metric.HIT_CHANCE?"%":"max hit";
			boolean available=!qualitative && comparison.before!=null && comparison.available(kind);
			before=available?(kind==ComparisonResult.Metric.THEORETICAL_DPS?comparison.before.dps:kind==ComparisonResult.Metric.HIT_CHANCE?comparison.before.hitChance*100:(double)comparison.before.maxHit):null;
			after=available?(kind==ComparisonResult.Metric.THEORETICAL_DPS?comparison.after.dps:kind==ComparisonResult.Metric.HIT_CHANCE?comparison.after.hitChance*100:(double)comparison.after.maxHit):null;
			delta=available?after-before:null;
			availability=qualitative?Availability.QUALITATIVE:available?delta==0?Availability.TRUE_ZERO:Availability.AVAILABLE
				:comparison.beforeSetup!=null && (comparison.beforeSetup.slots.values().stream().anyMatch(i->i<0)||comparison.beforeSetup.loadedAmmo<0 || comparison.beforeSetup.slots.getOrDefault("weapon",0)==0 || missingLevels(account,comparison.beforeSetup))
					?Availability.INPUT_REQUIRED:Availability.MODEL_UNSUPPORTED;
			reason=available?"Supported selected reference":comparison.gaps.getOrDefault(kind,comparison.reason);
			action=availability==Availability.INPUT_REQUIRED?(missingLevels(account,comparison.beforeSetup)?"Observe real combat levels":correction(comparison.beforeSetup)):availability==Availability.MODEL_UNSUPPORTED?"Inspect the model gap":"Inspect supported role / context";
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
	public static final class SlotChange
	{
		public final String slot, beforeName, afterName;
		public final int beforeId, afterId;
		SlotChange(String slot,int before,int after,MapLookup names)
		{ this.slot=slot; beforeId=before; afterId=after; beforeName=names.name(slot,before); afterName=names.name(slot,after); }
	}
	private interface MapLookup { String name(String slot,int id); }
	public final List<Metric> comparisonMetrics;
	public final List<SlotChange> changes;
	public final String contextLabel, utility, baseline, limitation;
	public final Integer beforeTicks,afterTicks;
	public final Metric primaryMetric;
	public final RemainingEffort effort;
	public final ProgressionPriority.Priority priority;
	public final String status, priorityReason, nextStep, estimateOrigin, estimateScope;
	public final RangeKind rangeKind;
	public final Double minimumMeanHours, maximumMeanHours;
	public final List<RouteChoice> routes;
	UpgradePresentation(Advisor.UpgradeView view, List<RouteChoice> routes,KnowledgeCatalog catalog,com.ironpath.model.AccountSnapshot account)
	{
		primaryMetric=new Metric(view.comparison,view.comparison.primaryMetric,view.valueEvidence.baseline,view.valueEvidence.independentUtility,account);
		comparisonMetrics=List.of(new Metric(view.comparison,view.comparison.primaryMetric,view.valueEvidence.baseline,false,account),
			new Metric(view.comparison,view.comparison.primaryMetric==ComparisonResult.Metric.MAX_HIT?ComparisonResult.Metric.THEORETICAL_DPS:ComparisonResult.Metric.MAX_HIT,view.valueEvidence.baseline,false,account),
			new Metric(view.comparison,ComparisonResult.Metric.HIT_CHANCE,view.valueEvidence.baseline,false,account));
		contextLabel=view.comparison.target==null || view.comparison.target.analytical ? "" : view.comparison.target.name;
		utility=view.valueEvidence.roles.stream().filter(r->r.applicable==com.ironpath.model.Truth.TRUE && !r.covered && r.dimension!=ProgressionKnowledge.Dimension.ORDINARY_OFFENCE)
			.map(r->utilityLabel(r.dimension)).findFirst().orElse("Reviewed specialist value");
		limitation=view.upgrade.limitation;
		beforeTicks=view.comparison.before==null?null:view.comparison.before.attackTicks; afterTicks=view.comparison.after==null?null:view.comparison.after.attackTicks;
		java.util.Map<Integer,String> names=catalog.itemNames();
		java.util.ArrayList<SlotChange> changed=new java.util.ArrayList<>();
		if(view.comparison.beforeSetup!=null && view.comparison.afterSetup!=null) {
			for(String slot:PoweredMagic.SLOTS) {
				int before=view.comparison.beforeSetup.slots.getOrDefault(slot,-1),after=view.comparison.afterSetup.slots.getOrDefault(slot,-1);
				if(before!=after) { changed.add(new SlotChange(slot,before,after,(key,id)->id==0?"Empty "+key:id<0?"Unobserved "+key:names.getOrDefault(id,"Unreviewed "+key+" item"))); }
			}
		}
		changed.sort(java.util.Comparator.comparingInt((SlotChange c)->c.slot.equals("weapon")?0:c.afterId==view.upgrade.itemId?1:2));
		changes=List.copyOf(changed);
		baseline=view.upgrade.comparison.equals("NONE")?"Reviewed owned alternatives":changes.stream().filter(c->c.slot.equals("weapon")).findFirst().or(()->changes.stream().findFirst()).map(c->c.beforeName).orElse(view.valueEvidence.baseline);
		effort=view.pursuit.remainingEffort; priority=view.pursuit.priority;
		status=primaryMetric.availability==Availability.INPUT_REQUIRED?primaryMetric.action
			: !view.questions.isEmpty()?"Check ownership"
			: effort.suitability==RemainingEffort.Suitability.PREPARE?"Preparation needed"
			: effort.suitability==RemainingEffort.Suitability.UNKNOWN?"Check acquisition method"
			: view.pursuit.category==PursuitAssessment.Category.DEFER?"Optional"
			: view.pursuit.category==PursuitAssessment.Category.LONG_TERM?"Long-term"
			: effort.band==RemainingEffort.Band.UNKNOWN?"Effort not estimated":effort.band.label+" remaining effort";
		priorityReason=(primaryMetric.availability==Availability.MODEL_UNSUPPORTED && view.valueEvidence.importance==ValueEvidence.Importance.UNCERTAIN?"Personal gain is not established by the supported model.":view.valueEvidence.reason) + " " + (effort.acquisitionBypassed?"Owned components shorten the remaining work."
			:effort.suitability==RemainingEffort.Suitability.PREPARE?"Preparation comes before this acquisition."
			:effort.suitability==RemainingEffort.Suitability.UNKNOWN?"Acquisition suitability still needs checking."
			:effort.band==RemainingEffort.Band.UNKNOWN?"Remaining effort is not established."
			:effort.band.label+" remaining effort with the reviewed "+(effort.suitability==RemainingEffort.Suitability.WORKABLE?"workable":"compatible")+" method.");
		Advisor.RouteView selected=view.routes.stream().filter(r->r.decision.id.equals(view.pursuit.route)).findFirst().orElseThrow();
		Advisor.Requirement next=effort.acquisitionBypassed?selected.nextStep:selected.requirements.stream().filter(r->!r.role.equals("SUGGESTED PREPARATION") && r.truth!=com.ironpath.model.Truth.TRUE && !r.skipped).findFirst().orElse(selected.nextStep);
		nextStep=effort.suitability==RemainingEffort.Suitability.PREPARE || effort.suitability==RemainingEffort.Suitability.UNKNOWN?effort.reason
			:next==null?"Pursue "+selected.decision.title:(next.truth==com.ironpath.model.Truth.UNKNOWN?"Check ":next.role.equals("ACTIVITY")?"":next.role.equals("COMPONENT")?"Acquire ":"Complete ")+next.label;
		EffortEstimate estimate=view.pursuit.estimate;
		boolean hours=estimate!=null && estimate.meanHours()!=null;
		rangeKind=hours?RangeKind.SCENARIO_MEAN_SENSITIVITY:RangeKind.UNAVAILABLE;
		minimumMeanHours=hours?estimate.meanTrials/estimate.pace.maximumPerHour:null;
		maximumMeanHours=hours?estimate.meanHours():null;
		estimateOrigin=hours?estimate.pace.provenance:"Reviewed qualitative activity profile";
		estimateScope=estimate==null?effort.scope:estimate.remainingOutcome + "; " + (hours?estimate.pace.scope:"no hours available");
		this.routes=List.copyOf(routes);
	}
	private static boolean missingLevels(com.ironpath.model.AccountSnapshot account,CombatSetup setup)
	{
		if(account==null || setup==null) { return false; }
		return setup.family==CombatSetup.Family.MELEE?account.level("ATTACK")==null || account.level("STRENGTH")==null
			:account.level(setup.family==CombatSetup.Family.RANGED?"RANGED":"MAGIC")==null;
	}

	private static String correction(CombatSetup setup)
	{
		if(setup==null) { return "Observe equipment"; }
		if(setup.loadedAmmo<0) { return "Check loaded ammunition"; }
		return PoweredMagic.SLOTS.stream().filter(s->setup.slots.getOrDefault(s,-1)<0).findFirst()
			.map(s->"Check "+s+" slot").orElse("Observe a usable weapon");
	}
	private static String utilityLabel(ProgressionKnowledge.Dimension dimension)
	{
		switch(dimension) {
			case BURST: return "Special attack burst";
			case INVENTORY: return "Inventory convenience";
			case CONTROL: return "Demonbane / control";
			case MULTI_TARGET: return "Multi-target attacks";
			case CONVENIENCE: return "Multi-target convenience";
			default: return "Reviewed resource value";
		}
	}

}
