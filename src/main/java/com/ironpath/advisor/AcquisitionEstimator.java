package com.ironpath.advisor;

/** Independently authored analytic distributions; no simulation, IO or historical KC inference. */
public final class AcquisitionEstimator
{
	public EffortEstimate estimate(AcquisitionModel model, boolean stageRemaining, String unavailableReason, PaceScenario pace)
	{ return estimate(model,stageRemaining,unavailableReason,pace,model.requiredSuccesses,false); }
	public EffortEstimate estimate(AcquisitionModel model, boolean stageRemaining, String unavailableReason,
		PaceScenario pace, int successes, boolean conditionalWork)
	{ return estimate(model,stageRemaining,unavailableReason,pace,successes,conditionalWork,pace==null?null:pace.methodKey); }
	public EffortEstimate estimate(AcquisitionModel model, boolean stageRemaining, String unavailableReason,
		PaceScenario pace, int successes, boolean conditionalWork, String methodKey)
	{
		if (!stageRemaining || successes==0)
		{ return new EffortEstimate(model,unavailableReason,null,null,null,null,null,successes,conditionalWork,methodKey); }
		if (successes<0 || successes>10) { throw new IllegalArgumentException("Unsupported remaining reward count"); }
		if (model.support!=AcquisitionModel.Support.REVIEWED || model.mechanic==AcquisitionModel.Mechanic.UNSUPPORTED)
		{ return new EffortEstimate(model,"Probability unavailable: " + model.reason,null,null,null,null,null,successes,conditionalWork,methodKey); }
		double p=successProbability(model);
		if (!Double.isFinite(p) || p<=0 || p>1)
		{ return new EffortEstimate(model,"No finite supported completion estimate for this probability.",null,null,null,null,null,successes,conditionalWork,methodKey); }
		PaceScenario matched=pace!=null && pace.activity.equals(model.activity) && pace.mode.equals(model.mode)?pace:null;
		return new EffortEstimate(model,"Eligible future trials including success; component acquisition only, not a full-item ETA."
			+ (conditionalWork?" Conditional on the stated remaining components; other storage/allocation may reduce work.":""),
			successes/p,quantile(p,successes,.1),quantile(p,successes,.5),quantile(p,successes,.9),matched,successes,conditionalWork,methodKey);
	}
	public static double successProbability(AcquisitionModel model)
	{
		double p=(double)model.numerator/model.denominator;
		return model.mechanic==AcquisitionModel.Mechanic.INDEPENDENT_ROLLS
			? p==1?1:-Math.expm1(model.rolls*Math.log1p(-p)):p;
	}
	public static long quantile(double p,double q) { return quantile(p,1,q); }
	public static long quantile(double p,int successes,double q)
	{
		if (!Double.isFinite(p)||p<=0||p>1||!Double.isFinite(q)||q<=0||q>=1||successes<1||successes>10)
		{ throw new IllegalArgumentException("Finite probability/quantile and supported reward count required"); }
		if (p==1) { return successes; }
		if (successes==1)
		{
			double n=Math.ceil(Math.log1p(-q)/Math.log1p(-p));
			if(n>=Long.MAX_VALUE) { throw new IllegalArgumentException("Quantile exceeds supported range"); }
			return Math.max(1,(long)n);
		}
		long low=successes-1, high=successes;
		while (probabilityBy(p,high,successes)<q)
		{
			if (high>Long.MAX_VALUE/2) { throw new IllegalArgumentException("Quantile exceeds supported range"); }
			high*=2;
		}
		while(high-low>1)
		{
			long mid=low+(high-low)/2;
			if(probabilityBy(p,mid,successes)>=q) { high=mid; } else { low=mid; }
		}
		return high;
	}
	public static double probabilityBy(double p,long trials) { return probabilityBy(p,trials,1); }
	/** Negative-binomial waiting time CDF: P(at least r successes in n Bernoulli trials). */
	public static double probabilityBy(double p,long trials,int successes)
	{
		if(!Double.isFinite(p)||p<0||p>1||trials<0||successes<1||successes>10)
		{ throw new IllegalArgumentException("Invalid trial probability/count"); }
		if(trials<successes||p==0) { return 0; }
		if(p==1) { return 1; }
		if(successes==1) { return -Math.expm1(trials*Math.log1p(-p)); }
		// For small finite populations direct terms preserve exact dyadic boundaries (e.g. 2 successes by 3 fair trials).
		if(trials<=1024 && p>=1e-6)
		{
			double term=Math.pow(1-p,trials), lower=term;
			for(int i=1;i<successes;i++) { term*=((double)(trials-i+1)/i)*p/(1-p);lower+=term; }
			return Math.max(0,Math.min(1,1-lower));
		}
		double logTerm=trials*Math.log1p(-p), logSum=logTerm;
		for(int i=1;i<successes;i++)
		{
			logTerm+=Math.log((double)(trials-i+1)/i)+Math.log(p)-Math.log1p(-p);
			double max=Math.max(logSum,logTerm);
			logSum=max+Math.log(Math.exp(logSum-max)+Math.exp(logTerm-max));
		}
		return Math.max(0,Math.min(1,-Math.expm1(logSum)));
	}
}
