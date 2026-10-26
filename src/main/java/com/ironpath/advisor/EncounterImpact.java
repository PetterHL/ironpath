package com.ironpath.advisor;

/** Constant-throughput estimates. Inputs must describe the same encounter damage pool. */
public final class EncounterImpact
{
	public static final String VERSION="encounter-impact-1";
	public static final class Gain
	{
		public final double fraction;
		private Gain(double fraction) { this.fraction=fraction; }
	}
	public static final class Estimate
	{
		public final Double contribution,secondsSaved,newDuration;
		public final String gap;
		private Estimate(Double contribution,Double secondsSaved,Double newDuration,String gap)
		{ this.contribution=contribution; this.secondsSaved=secondsSaved; this.newDuration=newDuration; this.gap=gap; }
	}
	/** A role sample is insufficient without supported whole-encounter weighting and equal tempo. */
	public static Gain weighted(Double affectedShare,Double roleGain,boolean coverageSupported,boolean equalTempo)
	{
		if(!coverageSupported || !equalTempo || !fraction(affectedShare) || roleGain==null
			|| !Double.isFinite(roleGain) || roleGain < -1) { return null; }
		return new Gain(affectedShare*roleGain);
	}
	/** Speed-changing comparisons need rates over equivalent exposure, not an equal attack count. */
	public static Gain weightedRates(Double affectedShare,double beforeRate,double afterRate,boolean equivalentExposure)
	{
		if(!equivalentExposure || !Double.isFinite(beforeRate) || beforeRate<=0
			|| !Double.isFinite(afterRate) || afterRate<0) { return null; }
		return weighted(affectedShare,afterRate/beforeRate-1,true,true);
	}
	public static Estimate estimate(Double share,Gain gain,Double duration,Double fixedSeconds)
	{
		if(gain==null) { return new Estimate(null,null,null,"Whole-encounter damage weighting unavailable"); }
		if(!fraction(share)) { return new Estimate(null,null,null,"Contribution unavailable for the same damage pool"); }
		double multiplier=1+share*gain.fraction;
		if(multiplier<=0 || !Double.isFinite(multiplier)) { return new Estimate(null,null,null,"No finite team throughput"); }
		double contribution=share*(1+gain.fraction)/multiplier;
		if(duration==null || !Double.isFinite(duration) || duration<=0 || fixedSeconds==null
			|| !Double.isFinite(fixedSeconds) || fixedSeconds<0 || fixedSeconds>duration)
		{ return new Estimate(contribution,null,null,"Duration or fixed-time input unavailable"); }
		double next=fixedSeconds+(duration-fixedSeconds)/multiplier;
		return new Estimate(contribution,duration-next,next,null);
	}
	private static boolean fraction(Double x) { return x!=null && Double.isFinite(x) && x>=0 && x<=1; }
	private EncounterImpact() { }
}
