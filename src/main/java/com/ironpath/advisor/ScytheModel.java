package com.ironpath.advisor;

/** Ordinary offence against one reviewed-size target; adjacent-target geometry is excluded. */
public final class ScytheModel
{
	private ScytheModel() { }
	private static int hits(Integer size)
	{ if(size==null || size<1) { throw new IllegalArgumentException("Scythe requires reviewed target size"); } return Math.min(3,size); }
	public static int maxHit(int firstMax,Integer size)
	{ int total=0;for(int hit=0;hit<hits(size);hit++) { total+=firstMax/(1<<hit); } return total; }
	public static double meanDamage(int firstMax,Integer size)
	{ double total=0;for(int hit=0;hit<hits(size);hit++) { total+=MeleeModel.ordinaryMean(firstMax/(1<<hit)); } return total; }
}
