package com.ironpath.advisor;

import java.util.Map;

/** Explicit, source-reviewed PvM interactions; each multiplier floors independently. */
public final class MeleeEffects
{
	public enum Gear { NONE, SLAYER, SALVE, SALVE_ENCHANTED, INQUISITOR_HEAD, INQUISITOR_BODY, INQUISITOR_LEGS }
	private MeleeEffects() { }
	private static boolean wearing(CombatSetup setup, Map<Integer,MeleeItem> items, Gear effect)
	{ return setup.slots.values().stream().map(items::get).anyMatch(i->i!=null && i.effect==effect); }
	private static long condition(long value, CombatSetup setup, TargetProfile target, Map<Integer,MeleeItem> items)
	{
		if (target.undead && wearing(setup,items,Gear.SALVE_ENCHANTED)) { return value*6/5; }
		if (target.undead && wearing(setup,items,Gear.SALVE) || setup.onTask && target.slayer && wearing(setup,items,Gear.SLAYER)) { return value*7/6; }
		return value;
	}
	private static long bane(long value, TargetProfile target, MeleeItem weapon)
	{
		if (weapon.family==MeleeModel.Weapon.LANCE && target.dragon) { return value*6/5; }
		if (weapon.family==MeleeModel.Weapon.DEMONBANE && target.demonbanePercent!=null)
		{ return value+value*70*target.demonbanePercent/10000; }
		return value;
	}
	private static long inquisitor(long value, CombatSetup setup, Map<Integer,MeleeItem> items)
	{
		if (setup.meleeStyle.type!=MeleeModel.Type.CRUSH) { return value; }
		int units=0;
		for (int id:setup.slots.values())
		{
			MeleeItem item=items.get(id); if (item==null) { continue; }
			if (item.effect==Gear.INQUISITOR_HEAD) { units+=1; }
			if (item.effect==Gear.INQUISITOR_BODY || item.effect==Gear.INQUISITOR_LEGS) { units+=2; }
		}
		return value*(200+units)/200;
	}
	static long attackRoll(long value, CombatSetup setup, TargetProfile target, MeleeItem weapon, Map<Integer,MeleeItem> items)
	{ return inquisitor(bane(condition(value,setup,target,items),target,weapon),setup,items); }
	static int maxHit(int value, CombatSetup setup, TargetProfile target, MeleeItem weapon, Map<Integer,MeleeItem> items)
	{ return (int)inquisitor(bane(condition(value,setup,target,items),target,weapon),setup,items); }
	/** Preserve mechanic-bearing packages before ordinary stat dominance pruning. */
	static String signature(Map<String,Integer> slots, Map<Integer,MeleeItem> items)
	{ return slots.values().stream().map(items::get).filter(i->i!=null && i.effect!=Gear.NONE).map(i->i.effect.name()).sorted().collect(java.util.stream.Collectors.joining("/")); }
}
