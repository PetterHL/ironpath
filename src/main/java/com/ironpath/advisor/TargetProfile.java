package com.ironpath.advisor;

import java.util.List;

/** Reviewed NPC phase or explicitly authored analytical reference. */
public final class TargetProfile
{
	public String id, name, variant;
	public int npcId, magicLevel, magicDefence;
	public Integer defenceLevel;
	public Integer meleeStab, meleeSlash, meleeCrush;
	public Long meleeDefenceRoll(MeleeModel.Type type)
	{ Integer bonus=type==MeleeModel.Type.STAB ? meleeStab : type==MeleeModel.Type.SLASH ? meleeSlash : meleeCrush;
		return defenceLevel==null || bonus==null ? null : (long)(defenceLevel+9)*(bonus+64); }
	public Integer magicAttack, rangedLight, rangedStandard, rangedHeavy;
	public boolean xerician, dragon, slayer;
	public boolean analytical;
	public boolean scalingSupported;
	public List<String> sources;
	public long defenceRoll() { return (long) (magicLevel + 9) * (magicDefence + 64); }
	public Long rangedDefenceRoll(RangedItem.DefenceType type)
	{
		Integer bonus = type == RangedItem.DefenceType.LIGHT ? rangedLight : type == RangedItem.DefenceType.HEAVY ? rangedHeavy : rangedStandard;
		return defenceLevel == null || bonus == null ? null : (long) (defenceLevel + 9) * (bonus + 64);
	}
}
