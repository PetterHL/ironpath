package com.ironpath.advisor;

import java.util.Map;

/** Original integer arithmetic for ordinary powered attacks. No imported calculator code. */
final class PoweredStaffModel
{
	static PoweredMagic.Metrics calculate(int magic, CombatSetup setup, TargetProfile target,
		Map<Integer, KnowledgeCatalog.CombatItem> items)
	{
		int visible = magic + (setup.boost == PoweredMagic.Boost.MAGIC_POTION ? 4
			: setup.boost == PoweredMagic.Boost.SATURATED_HEART ? 4 + magic / 10 : 0);
		int effective = visible * setup.prayer.accuracyPercent / 100
			+ (setup.style == PoweredMagic.Style.ACCURATE ? 3 : 1) + 8;
		int attack = 0, damage = 0;
		for (int id : setup.slots.values())
		{
			if (id == 0) { continue; }
			KnowledgeCatalog.CombatItem item = items.get(id);
			attack += item.magicAttack; damage += item.damagePermille == null ? item.damagePercent * 10 : item.damagePermille;
		}
		KnowledgeCatalog.CombatItem weapon = items.get(setup.slots.get("weapon"));
		int base, ticks;
		switch (weapon.weapon)
		{
			case "SEAS": base = visible / 3 - 5; ticks = 4; break;
			case "SWAMP": base = visible / 3 - 2; ticks = 4; break;
			case "SANG": base = visible / 3; ticks = 4; break;
			case "EYE": base = visible / 3 - 6; ticks = 3; break;
			case "SHADOW": base = visible / 3 + 1; ticks = 5;
				int multiplier = target.variant.equals("TOA") ? 4 : 3;
				attack *= multiplier; damage = Math.min(1000, damage * multiplier); break;
			default: throw new IllegalArgumentException("Unsupported powered family");
		}
		// Gear is multiplied/capped first. Prayer damage is added after Shadow's cap.
		damage += setup.prayer.damagePercent * 10;
		int max = Math.max(1, base) * (1000 + damage) / 1000;
		long roll = (long) effective * Math.max(0, 64 + attack), defence = target.defenceRoll();
		double chance = PoweredMagic.hitChance(roll, defence);
		// Sang's July 2026 successful-hit proc precedes the final successful-zero floor.
		// Non-proc (80%) zero becomes 1; proc (20%) adds 8, including to zero.
		double mean = weapon.weapon.equals("SANG") ? max / 2.0 + 1.6 + 0.8 / (max + 1.0) : PoweredMagic.successfulMean(max);
		int reportedMax = weapon.weapon.equals("SANG") ? max + 8 : max;
		return new PoweredMagic.Metrics(reportedMax, chance, chance * mean / (ticks * 0.6),
			visible, effective, attack, damage / 10.0, roll, defence, ticks);
	}
}
