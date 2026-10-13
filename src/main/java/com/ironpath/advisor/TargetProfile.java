package com.ironpath.advisor;

import java.util.List;

/** Reviewed NPC phase or explicitly authored analytical reference. */
public final class TargetProfile
{
	public String id, name, variant;
	public int npcId, magicLevel, magicDefence;
	public Integer defenceLevel;
	public boolean analytical;
	public boolean scalingSupported;
	public List<String> sources;
	public long defenceRoll() { return (long) (magicLevel + 9) * (magicDefence + 64); }
}
