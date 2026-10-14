package com.ironpath.advisor;

/** Original closed-form expectation for independent uniform attack rolls. No special attack. */
public final class FangModel
{
	private FangModel() { }
	public static double hitChance(long attack, long defence, boolean toa)
	{
		if (attack<0 || defence<0) { throw new IllegalArgumentException("Negative roll"); }
		if (toa) { double miss=1-PoweredMagic.hitChance(attack,defence); return 1-miss*miss; }
		double a=attack,d=defence;
		// Outside ToA both attack draws face the same defence draw; their failures are correlated.
		return a>=d ? 1-(d+2)*(2*d+3)/(6*(a+1)*(a+1)) : a*(4*a+5)/(6*(a+1)*(d+1));
	}
	static double mean(int baseMax)
	{
		int lower=baseMax*3/20,upper=baseMax-lower;
		return baseMax==0 ? 0 : (lower+upper)/2.0+(lower==0 ? 1.0/(upper+1) : 0);
	}
}
