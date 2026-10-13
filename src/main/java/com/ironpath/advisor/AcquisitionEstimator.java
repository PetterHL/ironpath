package com.ironpath.advisor;

/** Independently authored analytic distribution; no simulation, client, clock, IO or historical KC. */
public final class AcquisitionEstimator
{
	public EffortEstimate estimate(AcquisitionModel model, boolean stageRemaining, String unavailableReason, PaceScenario pace)
	{
		if (!stageRemaining) { return new EffortEstimate(model, unavailableReason, null, null, null, null, null); }
		if (model.support != AcquisitionModel.Support.REVIEWED || model.mechanic != AcquisitionModel.Mechanic.FIXED_BERNOULLI)
		{ return new EffortEstimate(model, "Probability unavailable: " + model.reason, null, null, null, null, null); }
		double p=(double)model.numerator/model.denominator;
		if (!Double.isFinite(p) || p <= 0 || p > 1)
		{ return new EffortEstimate(model, "No finite supported completion estimate for this probability.", null, null, null, null, null); }
		PaceScenario matched=pace != null && pace.activity.equals(model.activity) && pace.mode.equals(model.mode) ? pace : null;
		return new EffortEstimate(model, "Future eligible trials, including the successful trial; not a full-item ETA.",
			1/p, quantile(p, .1), quantile(p, .5), quantile(p, .9), matched);
	}
	public static long quantile(double p, double q)
	{
		if (!Double.isFinite(p) || p <= 0 || p > 1 || !Double.isFinite(q) || q <= 0 || q >= 1)
		{ throw new IllegalArgumentException("Finite probability and quantile in (0,1) required."); }
		if (p == 1) { return 1; }
		double n=Math.ceil(Math.log1p(-q)/Math.log1p(-p));
		if (n > Long.MAX_VALUE) { throw new IllegalArgumentException("Quantile exceeds supported trial range."); }
		return Math.max(1, (long)n);
	}
	public static double probabilityBy(double p, long futureTrials)
	{
		if (!Double.isFinite(p) || p < 0 || p > 1 || futureTrials < 0) { throw new IllegalArgumentException("Invalid trial probability"); }
		return futureTrials == 0 ? 0 : p == 1 ? 1 : -Math.expm1(futureTrials*Math.log1p(-p));
	}
}
