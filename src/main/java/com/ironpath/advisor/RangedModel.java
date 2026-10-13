package com.ironpath.advisor;

import java.util.Map;

/** Independently authored integer ranged family arithmetic. Sources are in the bundled contract. */
public final class RangedModel
{
	public static final String VERSION = "ranged-1";
	public enum Prayer
	{
		NONE(100,100), EAGLE_EYE(115,115), DEADEYE(118,118), RIGOUR(120,123);
		final int accuracy, strength;
		Prayer(int accuracy, int strength) { this.accuracy = accuracy; this.strength = strength; }
	}
	public enum Style { ACCURATE, RAPID, LONGRANGE }
	private RangedModel() { }
	public static RangedItem.Ammo ammoKind(RangedItem weapon)
	{
		switch (weapon.weapon)
		{
			case TWISTED: case SHORTBOW: return RangedItem.Ammo.ARROW;
			case CROSSBOW: case DRAGON_HUNTER: return RangedItem.Ammo.BOLT;
			case TOXIC: case ROSEWOOD: return RangedItem.Ammo.DART;
			default: return RangedItem.Ammo.NONE;
		}
	}
	public static boolean compatible(RangedItem weapon, RangedItem ammo)
	{ return ammo != null && ammo.ammo == ammoKind(weapon) && ammo.ammo != RangedItem.Ammo.NONE && ammo.ammoTier <= weapon.maxAmmoTier; }
	/** In-game integer divisions truncate toward zero, including the small negative term at Magic 0. */
	public static int twistedPercent(int magic, boolean accuracy)
	{
		if (magic < 0 || magic > 350) { throw new IllegalArgumentException("Twisted scaling input outside reviewed cap"); }
		int centre = accuracy ? 100 : 140, ceiling = accuracy ? 140 : 250;
		int displacement = 3 * magic / 10 - centre;
		return Math.max(0, Math.min(ceiling, ceiling + (3 * magic - (accuracy ? 10 : 14)) / 100 - displacement * displacement / 100));
	}
	public static double successfulMean(int maxHit, int floor)
	{
		if (maxHit < 0 || floor < 1 || floor > maxHit && maxHit > 0) { throw new IllegalArgumentException("Unsupported damage floor"); }
		if (maxHit == 0) { return 0; }
		// Replace every successful value below the floor, retaining misses separately.
		return maxHit / 2.0 + floor * (floor + 1.0) / (2.0 * (maxHit + 1.0));
	}
	public static PoweredMagic.Metrics calculate(int level, CombatSetup setup, TargetProfile target, Map<Integer,RangedItem> items)
	{
		RangedItem weapon = items.get(setup.slots.get("weapon"));
		if (weapon == null || weapon.weapon == null) { throw new IllegalArgumentException("Missing ranged weapon"); }
		Long defence = target.rangedDefenceRoll(weapon.defenceType);
		if (defence == null || target.magicAttack == null || !target.scalingSupported) { throw new IllegalArgumentException("Target inputs unsupported"); }
		int attack = 0, strength = 0, crystal = 0;
		for (String slot : PoweredMagic.SLOTS)
		{
			int id = setup.slots.get(slot); if (id == 0) { continue; }
			RangedItem item = items.get(id); if (item == null) { throw new IllegalArgumentException("Unknown item stats"); }
			if (slot.equals("ammo") && ammoKind(weapon) != RangedItem.Ammo.ARROW && ammoKind(weapon) != RangedItem.Ammo.BOLT) { continue; }
			attack += item.attack; strength += item.strength; crystal += item.crystalWeight;
		}
		RangedItem ammo = items.get(ammoKind(weapon) == RangedItem.Ammo.DART ? setup.loadedAmmo : setup.slots.get("ammo"));
		if (ammoKind(weapon) != RangedItem.Ammo.NONE && !compatible(weapon, ammo)) { throw new IllegalArgumentException("Invalid or unknown ammunition"); }
		if (ammoKind(weapon) == RangedItem.Ammo.DART) { strength += ammo.strength; } // Dart accuracy never contributes to a blowpipe.
		RangedItem cape = items.get(setup.slots.get("cape"));
		if (cape != null && cape.chargedQuiver && (ammoKind(weapon) == RangedItem.Ammo.ARROW || ammoKind(weapon) == RangedItem.Ammo.BOLT))
		{ attack += 10; strength += 1; }
		int visible = level + (setup.rangingPotion ? 4 + level / 10 : 0);
		int style = setup.rangedStyle == Style.ACCURATE ? 3 : 0;
		int effectiveAttack = visible * setup.rangedPrayer.accuracy / 100 + style + 8;
		int effectiveStrength = visible * setup.rangedPrayer.strength / 100 + style + 8;
		boolean voidSet = part(setup,items,"head",RangedItem.SetPart.VOID_HEAD)
			&& part(setup,items,"hands",RangedItem.SetPart.VOID_HANDS)
			&& (part(setup,items,"body",RangedItem.SetPart.VOID_BODY) || part(setup,items,"body",RangedItem.SetPart.ELITE_BODY))
			&& (part(setup,items,"legs",RangedItem.SetPart.VOID_LEGS) || part(setup,items,"legs",RangedItem.SetPart.ELITE_LEGS));
		if (voidSet)
		{
			effectiveAttack = effectiveAttack * 11 / 10;
			boolean elite = part(setup,items,"body",RangedItem.SetPart.ELITE_BODY) && part(setup,items,"legs",RangedItem.SetPart.ELITE_LEGS);
			effectiveStrength = elite ? effectiveStrength * 9 / 8 : effectiveStrength * 11 / 10;
		}
		int max = (int) ((long) effectiveStrength * Math.max(0, strength + 64) + 320) / 640;
		long roll = (long) effectiveAttack * Math.max(0, attack + 64);
		if (weapon.weapon == RangedItem.Weapon.BOWFA || weapon.weapon == RangedItem.Weapon.CRYSTAL)
		{ max = max * (40 + crystal) / 40; roll = roll * (20 + crystal) / 20; }
		RangedItem head = items.get(setup.slots.get("head"));
		boolean task = setup.onTask && target.slayer && head != null && head.slayerHelmet;
		if (task) { max = max * (weapon.weapon == RangedItem.Weapon.DRAGON_HUNTER && target.dragon ? 28 : 23) / 20; roll = roll * 23 / 20; }
		if (weapon.weapon == RangedItem.Weapon.DRAGON_HUNTER && target.dragon)
		{ if (!task) { max = max * 5 / 4; } roll = roll * 13 / 10; }
		if (weapon.weapon == RangedItem.Weapon.TWISTED)
		{
			int magic = Math.min(target.xerician ? 350 : 250, Math.max(target.magicLevel,target.magicAttack));
			max = max * twistedPercent(magic,false) / 100; roll = roll * twistedPercent(magic,true) / 100;
		}
		int ticks = weapon.ticks - (setup.rangedStyle == Style.RAPID ? 1 : 0);
		double chance = PoweredMagic.hitChance(roll,defence);
		int floor = ammo != null && ammo.seeking && ammoKind(weapon) == RangedItem.Ammo.ARROW ? Math.min(3,max) : Math.min(1,max);
		double mean = max == 0 ? 0 : successfulMean(max,floor);
		return new PoweredMagic.Metrics(max,chance,chance * mean / (ticks * 0.6),visible,effectiveStrength,attack,strength,roll,defence,ticks);
	}
	private static boolean part(CombatSetup s, Map<Integer,RangedItem> items, String slot, RangedItem.SetPart part)
	{ RangedItem item = items.get(s.slots.get(slot)); return item != null && item.setPart == part; }
}
