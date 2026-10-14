package com.ironpath.advisor;

/** Evaluated compact copy, derived from typed values; no ranking or report parsing in Swing. */
public final class UpgradeCard
{
	public final String reason, metric, status, effort, priorityReason, baseline, context;
	public final Double delta;
	public final UpgradePresentation presentation;
	public UpgradeCard(Advisor.UpgradeView view,KnowledgeCatalog catalog)
	{
		presentation=view.presentation; reason=view.valueEvidence.reason;
		effort=presentation.minimumMeanHours==null?presentation.effort.band.label+" remaining effort"
			:String.format(java.util.Locale.ROOT,"%s: ~%.1f h mean · %s",presentation.estimateSourceKind==PaceScenario.SourceKind.MODEL_BASED?"Model-based estimate":"Manual pace scenario",presentation.maximumMeanHours,presentation.conditionalComponentWork?"if components needed":"component only"); priorityReason=presentation.priorityReason;
		status=presentation.status;
		UpgradePresentation.Metric value=presentation.primaryMetric;
		String compared=presentation.baseline;
		String scope=presentation.contextLabel;
		if(view.valueEvidence.independentUtility) { metric=presentation.utility; delta=null;
			if(view.upgrade.comparison.equals("NONE")) { compared=view.valueEvidence.roles.stream().flatMap(r->r.alternatives.stream()).findFirst().orElse("No confirmed role alternative"); } }
		else {
			ComparisonResult result=view.comparison;
			if(view.valueEvidence.specialist && result.available(ComparisonResult.Metric.THEORETICAL_DPS) && result.dpsGain()<=1e-10) {
				ComparisonResult contextual=view.contextComparisons.stream().filter(c->c.available(ComparisonResult.Metric.THEORETICAL_DPS)&&c.dpsGain()>1e-10).findFirst().orElse(null);
				if(contextual!=null) { value=new UpgradePresentation.Metric(contextual,compared,false); scope=contextual.target.name;
					if(contextual.beforeSetup!=null) { compared=catalog.itemNames().getOrDefault(contextual.beforeSetup.slots.get("weapon"),"Unobserved weapon")+(contextual.changesSetup()?" setup":""); }
				}
			}
			delta=value.delta;
			metric=value.headline();
		}
		context=scope; baseline=compared;
	}
}
