package com.ironpath.advisor;

/** Shared ordinary inclusive rolls and successful-zero distribution; no family modifiers. */
public final class AttackMath
{
	private AttackMath() {}
	public static double hitChance(long attack, long defence)
	{
		if (attack < 0 || defence < 0) { throw new IllegalArgumentException("Negative roll is unsupported"); }
		return attack > defence ? 1.0 - (defence + 2.0) / (2.0 * (attack + 1.0)) : attack / (2.0 * (defence + 1.0));
	}
	public static double successfulMean(int maxHit)
	{
		if (maxHit < 1) { throw new IllegalArgumentException("Unsupported max hit"); }
		return maxHit / 2.0 + 1.0 / (maxHit + 1.0);
	}
}
