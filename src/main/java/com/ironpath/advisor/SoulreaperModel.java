package com.ironpath.advisor;

/** Pre-attack stacks; no HP drain under the implemented July rules. No encounter timing or specials. */
public final class SoulreaperModel
{
	private SoulreaperModel() { }
	public static int effectiveStrength(int visible,int prayerPercent,int stacks,int stance)
	{
		if(stacks<0 || stacks>5) { throw new IllegalArgumentException("Soul stacks must be 0–5"); }
		return visible*prayerPercent/100+visible*stacks*6/100+stance+8;
	}
}
