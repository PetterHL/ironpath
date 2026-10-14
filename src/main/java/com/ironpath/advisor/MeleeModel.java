package com.ironpath.advisor;

import java.util.Map;

/** Independently authored ordinary PvM melee arithmetic; integer rounding is deliberate. */
public final class MeleeModel
{
	public static final String VERSION = "melee-2";
	public enum Weapon { ORDINARY, FANG, SCYTHE, SOULREAPER, LANCE, DEMONBANE }
	public enum Souls
	{
		START(0), ONE(1), TWO(2), THREE(3), FOUR(4), RETAINED(5), RAMP(-1);
		final int stacks;
		Souls(int stacks) { this.stacks=stacks; }
		public String label() { return this==START ? "Start / lost stacks (0)" : this==RAMP ? "Next five swings: 0 to 4 stacks" : "Retained "+stacks+" soul stacks"; }
	}
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
		CRUSH_ACCURATE(Type.CRUSH,3,0), CRUSH_AGGRESSIVE(Type.CRUSH,0,3), CRUSH_CONTROLLED(Type.CRUSH,1,1), CRUSH_DEFENSIVE(Type.CRUSH,0,0);
		public final Type type;
		final int attack, strength;
		Style(Type type,int attack,int strength) { this.type=type; this.attack=attack; this.strength=strength; }
		public String label() { String[] parts=name().toLowerCase(java.util.Locale.ROOT).split("_"); return parts[0]+" · "+parts[1]; }
	}
	private MeleeModel() { }
	public static PoweredMagic.Metrics calculate(int attackLevel, int strengthLevel, CombatSetup setup, TargetProfile target, Map<Integer,MeleeItem> items)
	{
		if (setup.family != CombatSetup.Family.MELEE || attackLevel<1 || attackLevel>99 || strengthLevel<1 || strengthLevel>99
			|| setup.meleeStyle == null || setup.meleePrayer == null || setup.meleeBoost == null || setup.souls == null)
		{ throw new IllegalArgumentException("Unsupported ordinary melee inputs"); }
		MeleeItem weapon=items.get(setup.slots.get("weapon"));
		if (weapon == null || weapon.ticks<1 || !weapon.styles.contains(setup.meleeStyle)
			|| weapon.twoHanded && setup.slots.get("shield") != 0 || !(target.scalingSupported || target.meleeScenario) || target.meleeDefenceRoll(setup.meleeStyle.type)==null
			|| setup.defenceOverride < -1 || setup.defenceOverride > target.defenceLevel)
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
		long roll=MeleeEffects.attackRoll((long)effectiveAttack*Math.max(0,attack+64),setup,target,weapon,items);
		long defence=target.meleeDefenceRoll(setup.meleeStyle.type);
		if (setup.defenceOverride>=0) { defence=defence/(target.defenceLevel+9)*(setup.defenceOverride+9); }
		double chance=weapon.family==Weapon.FANG && setup.meleeStyle.type==Type.STAB
			? FangModel.hitChance(roll,defence,target.variant.equals("TOA")) : PoweredMagic.hitChance(roll,defence);
		int swings=weapon.family==Weapon.SOULREAPER && setup.souls==Souls.RAMP ? 5 : 1;
		int effectiveStrength=0,max=0; double mean=0;
		for (int swing=0;swing<swings;swing++)
		{
			int stacks=weapon.family!=Weapon.SOULREAPER ? 0 : swings==5 ? swing : setup.souls.stacks;
			effectiveStrength=SoulreaperModel.effectiveStrength(visibleStrength,setup.meleePrayer.strength,stacks,setup.meleeStyle.strength);
			int base=(int)(((long)effectiveStrength*Math.max(0,strength+64)+320)/640);
			base=MeleeEffects.maxHit(base,setup,target,weapon,items);
			int upper=weapon.family==Weapon.FANG ? base-base*3/20 : base;
			if (weapon.family==Weapon.SCYTHE)
			{
				max=Math.max(max,ScytheModel.maxHit(base,target.size));
				mean+=ScytheModel.meanDamage(base,target.size)/swings;
			}
			else { max=Math.max(max,upper); mean+=(weapon.family==Weapon.FANG ? FangModel.mean(base) : ordinaryMean(base))/swings; }
		}
		return new PoweredMagic.Metrics(max,chance,chance*mean/(weapon.ticks*0.6),visibleStrength,effectiveStrength,attack,strength,roll,defence,weapon.ticks);
	}
	static double ordinaryMean(int max) { return max==0 ? 0 : max/2.0+1.0/(max+1.0); }
}
