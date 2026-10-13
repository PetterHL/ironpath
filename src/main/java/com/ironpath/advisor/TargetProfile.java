package com.ironpath.advisor;

import java.util.List;

/** Reviewed NPC phase. ToA scaling is explicitly unsupported in R1. */
public final class TargetProfile
{
	public String id, name, variant;
	public int npcId, magicLevel, magicDefence;
	public boolean scalingSupported;
	public List<String> sources;
	public long defenceRoll() { return (long) (magicLevel + 9) * (magicDefence + 64); }
}
