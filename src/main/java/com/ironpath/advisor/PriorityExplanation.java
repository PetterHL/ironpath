package com.ironpath.advisor;

/** Product explanation of a progression-2 result. Never renders the ranker's diagnostic prose. */
public final class PriorityExplanation
{
	public enum Constraint { NONE, OWNERSHIP, METHOD, PREPARATION, ACCOUNT, COMPARISON, EFFORT }
	public final ValueEvidence.Importance value;
	public final RemainingEffort.Band effort;
	public final Constraint constraint;
	public final String cardReason, detailReason;

	PriorityExplanation(Advisor.UpgradeView view, UpgradePresentation.Availability availability, String utility)
	{
		value=view.valueEvidence.importance; effort=view.pursuit.remainingEffort.band;
		RemainingEffort remaining=view.pursuit.remainingEffort;
		constraint=!view.questions.isEmpty()?Constraint.OWNERSHIP
			:remaining.suitability==RemainingEffort.Suitability.PREPARE?Constraint.PREPARATION
			:remaining.suitability==RemainingEffort.Suitability.UNKNOWN?Constraint.METHOD
			:value==ValueEvidence.Importance.UNCERTAIN && (availability==UpgradePresentation.Availability.INPUT_REQUIRED || availability==UpgradePresentation.Availability.MODEL_UNSUPPORTED)?Constraint.COMPARISON
			:effort==RemainingEffort.Band.UNKNOWN?Constraint.EFFORT
			:view.pursuit.category==PursuitAssessment.Category.CHECK || view.pursuit.category==PursuitAssessment.Category.PLAN?Constraint.ACCOUNT
			:Constraint.NONE;
		String gain=view.pursuit.reasonCodes.contains("DOMINATED")?"A supported alternative offers at least as much for no more work"
			:view.valueEvidence.independentUtility?utility
			:value==ValueEvidence.Importance.KEY?(view.valueEvidence.reasonCodes.contains("MAJOR_SUPPORTED_IMPROVEMENT")?"Major supported improvement":"Core role upgrade")
			:value==ValueEvidence.Importance.STRONG?(view.valueEvidence.reasonCodes.contains("MATERIAL_CORE_GAIN")?"Useful supported improvement":"Useful role upgrade")
			:value==ValueEvidence.Importance.CONTEXTUAL?"Useful in the reviewed encounters"
			:value==ValueEvidence.Importance.MARGINAL?"Limited gain over owned alternatives":"Personal gain not established";
		cardReason=gain+"; "+(remaining.acquisitionBypassed?"owned parts shorten the work"
			:effort==RemainingEffort.Band.UNKNOWN?"effort needs checking":effort.label.toLowerCase(java.util.Locale.ROOT)+" remaining effort")
			+(constraint==Constraint.OWNERSHIP?", if still needed":constraint==Constraint.PREPARATION?", after preparation"
			:constraint==Constraint.METHOD?", method unconfirmed":constraint==Constraint.ACCOUNT?", account checks remain":"")+".";
		String check=constraint==Constraint.OWNERSHIP?"Confirm the remaining ownership checks in Account before starting a new grind."
			:constraint==Constraint.PREPARATION?"Complete the remaining preparation before pursuing this route."
			:constraint==Constraint.METHOD?"Check compatible gear and levels in Acquisition before choosing this method."
			:constraint==Constraint.ACCOUNT?"Check the remaining access, resources or requirements in Acquisition."
			:constraint==Constraint.COMPARISON?"A supported comparison is needed before claiming personal offensive gain."
			:constraint==Constraint.EFFORT?"Remaining work needs an estimate before treating this as a next step."
			:view.pursuit.priority==ProgressionPriority.Priority.OPTIONAL?"Owned alternatives or limited supported gain make this optional; review its specific uses before grinding."
			:view.pursuit.priority==ProgressionPriority.Priority.LATER?"Nearer worthwhile choices can take priority over this remaining work."
			:remaining.suitability==RemainingEffort.Suitability.WORKABLE?"The reviewed method is workable with limitations."
			:remaining.acquisitionBypassed?"Only remaining resources, skills and assembly are assessed."
			:view.pursuit.priority==ProgressionPriority.Priority.LONG_HORIZON?"The remaining grind keeps this behind nearer worthwhile options when available."
			:"Value and remaining work place this among the account's next opportunities.";
		detailReason=cardReason+" "+check;
	}
}
