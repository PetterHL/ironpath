package com.ironpath.advisor;

import java.util.Map;

/** Independently authored ordinary PvM melee arithmetic; integer rounding is deliberate. */
public final class MeleeModel
{
	public static final String VERSION = "melee-1";
	public enum Type { STAB, SLASH, CRUSH }
	public enum Prayer
	{
		NONE(100,100), INCREDIBLE_ULTIMATE(115,115), CHIVALRY(115,118), PIETY(120,123);
		final int attack, strength;
		Prayer(int attack, int strength) { this.attack=attack; this.strength=strength; }
		public String label() { return this==NONE ? "No offensive prayer" : this==INCREDIBLE_ULTIMATE ? "Incredible Reflexes + Ultimate Strength" : this==PIETY ? "Piety" : "Chivalry"; }
	}
	public enum Boost
	{
		NONE, SUPER_ATTACK_STRENGTH;
		public String label() { return this==NONE ? "No boost" : "Super attack + strength (full boost)"; }
	}
	public enum Style
	{
		STAB_ACCURATE(Type.STAB,3,0), STAB_AGGRESSIVE(Type.STAB,0,3), STAB_CONTROLLED(Type.STAB,1,1), STAB_DEFENSIVE(Type.STAB,0,0),
		SLASH_ACCURATE(Type.SLASH,3,0), SLASH_AGGRESSIVE(Type.SLASH,0,3), SLASH_CONTROLLED(Type.SLASH,1,1), SLASH_DEFENSIVE(Type.SLASH,0,0),
		CRUSH_ACCURATE(Type.CRUSH,3,0), CRUSH_AGGRESSIVE(Type.CRUSH,0,3), CRUSH_DEFENSIVE(Type.CRUSH,0,0);
		public final Type type;
		final int attack, strength;
		Style(Type type,int attack,int strength) { this.type=type; this.attack=attack; this.strength=strength; }
		public String label() { String[] parts=name().toLowerCase(java.util.Locale.ROOT).split("_"); return parts[0]+" · "+parts[1]; }
	}
	private MeleeModel() { }
	public static PoweredMagic.Metrics calculate(int attackLevel, int strengthLevel, CombatSetup setup, TargetProfile target, Map<Integer,MeleeItem> items)
	{
		if (setup.family != CombatSetup.Family.MELEE || attackLevel<1 || attackLevel>99 || strengthLevel<1 || strengthLevel>99
			|| setup.meleeStyle == null || setup.meleePrayer == null || setup.meleeBoost == null || setup.onTask)
		{ throw new IllegalArgumentException("Unsupported ordinary melee inputs"); }
		MeleeItem weapon=items.get(setup.slots.get("weapon"));
		if (weapon == null || weapon.ticks<1 || !weapon.styles.contains(setup.meleeStyle)
			|| weapon.twoHanded && setup.slots.get("shield") != 0 || !target.scalingSupported || target.meleeDefenceRoll(setup.meleeStyle.type)==null)
		{ throw new IllegalArgumentException("Unsupported melee weapon/style, hands or target"); }
		int attack=0,strength=0;
		for (String slot : PoweredMagic.SLOTS)
		{
			Integer id=setup.slots.get(slot);
			if (id == null || id<0) { throw new IllegalArgumentException("Unknown melee slot"); }
			if (id==0) { continue; }
			MeleeItem item=items.get(id);
			if (item==null || !slot.equals(item.slot)) { throw new IllegalArgumentException("Unknown melee contribution"); }
			attack+=item.attack(setup.meleeStyle.type); strength+=item.strength;
		}
		boolean boosted=setup.meleeBoost==Boost.SUPER_ATTACK_STRENGTH;
		int visibleAttack=attackLevel+(boosted ? 5+attackLevel*15/100 : 0);
		int visibleStrength=strengthLevel+(boosted ? 5+strengthLevel*15/100 : 0);
		int effectiveAttack=visibleAttack*setup.meleePrayer.attack/100+setup.meleeStyle.attack+8;
		int effectiveStrength=visibleStrength*setup.meleePrayer.strength/100+setup.meleeStyle.strength+8;
		int max=(int)(((long)effectiveStrength*Math.max(0,strength+64)+320)/640);
		long roll=(long)effectiveAttack*Math.max(0,attack+64), defence=target.meleeDefenceRoll(setup.meleeStyle.type);
		double chance=PoweredMagic.hitChance(roll,defence);
		double mean=max==0 ? 0 : max/2.0+1.0/(max+1.0);
		return new PoweredMagic.Metrics(max,chance,chance*mean/(weapon.ticks*0.6),visibleStrength,effectiveStrength,attack,strength,roll,defence,weapon.ticks);
	}
}
