package com.ironpath.advisor;

import java.util.List;
import java.util.Map;

/** Reviewed ordinary melee facts. Special attacks, sets and creature effects need family modules. */
public final class MeleeItem
{
	public int id;
	public Integer stab, slash, crush, strength, ticks;
	public String label, slot, equipFact;
	public boolean twoHanded;
	public List<MeleeModel.Style> styles = List.of();
	public Map<String,Integer> requirements;
	public List<String> sources;
	public int attack(MeleeModel.Type type)
	{ return type == MeleeModel.Type.STAB ? stab : type == MeleeModel.Type.SLASH ? slash : crush; }
}
