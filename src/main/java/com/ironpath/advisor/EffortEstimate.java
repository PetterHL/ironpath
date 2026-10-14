package com.ironpath.advisor;

import java.util.List;

/** A partial next-reward estimate, never an implicit total-route ETA. Null means unavailable. */
public final class EffortEstimate
{
	public enum Level { A0, A1, A2 }
	public final Level level;
	public final String modelId, modelVersion, route, node, remainingOutcome, trialUnit, reason;
	public final String recipient, activity, mode, methodKey;
	public final java.time.LocalDate reviewed;
	public final long numerator, denominator;
	public final int remainingSuccesses, rolls;
	public final boolean conditionalWork;
	public final Double meanTrials;
	public final Long p10, p50, p90;
	public final PaceScenario pace;
	public final List<String> exclusions, assumptions, sources;

	EffortEstimate(AcquisitionModel model, String reason, Double mean, Long p10, Long p50, Long p90, PaceScenario pace)
	{ this(model,reason,mean,p10,p50,p90,pace,1,false); }
	EffortEstimate(AcquisitionModel model, String reason, Double mean, Long p10, Long p50, Long p90,
		PaceScenario pace, int successes, boolean conditionalWork)
	{ this(model,reason,mean,p10,p50,p90,pace,successes,conditionalWork,pace==null?null:pace.methodKey); }
	EffortEstimate(AcquisitionModel model, String reason, Double mean, Long p10, Long p50, Long p90,
		PaceScenario pace, int successes, boolean conditionalWork, String methodKey)
	{
		remainingSuccesses=successes; rolls=model.rolls; this.conditionalWork=conditionalWork;
		activity=model.activity; mode=model.mode; modelId=model.id; modelVersion=model.version; route=model.route; node=model.node;
		remainingOutcome=successes==1 ? (conditionalWork?"If needed: next ":"Next ") + model.rewardFamily.replace('-', ' ')
			: (conditionalWork?"Up to ":"Remaining ") + successes + " " + model.rewardFamily.replace('-', ' ') + " rewards"; trialUnit=model.trialUnit;
		recipient=model.recipient; reviewed=model.reviewed; numerator=model.numerator; denominator=model.denominator;
		this.methodKey=methodKey;
		this.reason=reason; meanTrials=mean; this.p10=p10; this.p50=p50; this.p90=p90; this.pace=pace;
		level=mean == null ? Level.A0 : pace == null ? Level.A1 : Level.A2;
		exclusions=model.exclusions; assumptions=model.assumptions; sources=model.sources;
	}
	public Double minimumMeanHours() { return meanTrials == null || pace == null ? null : meanTrials/pace.maximumPerHour; }
	public Double meanHours() { return meanTrials == null || pace == null ? null : meanTrials/pace.minimumPerHour; }
	public Double medianHours() { return p50 == null || pace == null ? null : p50/pace.minimumPerHour; }
	public Double p90Hours() { return p90 == null || pace == null ? null : p90/pace.minimumPerHour; }
}
