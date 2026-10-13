package com.ironpath.advisor;

import java.util.Objects;
import java.util.Set;

/**
 * Original ordinary ranged family arithmetic, not yet connected to personalised advice.
 * Inputs are explicit supplied/usable attack scenarios, never inferred ownership or readiness.
 * Gear bonuses exclude weapon and ammunition; inactive crystal pieces give no set modifier.
 */
public final class RangedAttackModel
{
	public static final String VERSION = "ranged-family-1";
	public enum Family
	{
		CRYSTAL_BOW(100,78,5), BOWFA(128,106,5), TWISTED_BOW(70,20,6);
		final int attack, strength, ticks;
		Family(int attack, int strength, int ticks) { this.attack=attack; this.strength=strength; this.ticks=ticks; }
	}
	public enum Style { ACCURATE, RAPID, LONGRANGE }
	public enum Prayer
	{
		NONE(100,100), EAGLE_EYE(115,115), DEADEYE(118,118), RIGOUR(120,123);
		final int accuracy, strength;
		Prayer(int accuracy, int strength) { this.accuracy=accuracy; this.strength=strength; }
	}
	public enum Ammo { NONE, ORDINARY_ARROW, SEEKER_ARROW }
	public enum Unmodelled { VOID, SLAYER, SALVE, QUIVER_CHARGE_EFFECT, TARGET_EFFECT, RAID_SCALING, SPECIAL_ATTACK }
	public static final class Setup
	{
		public final Family family;
		public final Style style;
		public final Prayer prayer;
		public final Ammo ammo;
		public final Integer realRanged, visibleRanged, gearAttack, gearStrength, ammoStrength;
		public final boolean usableWeapon, activeHelm, activeBody, activeLegs;
		public final Set<Unmodelled> unmodelled;
		public Setup(Family family, Style style, Prayer prayer, Ammo ammo, Integer realRanged, Integer visibleRanged,
			Integer gearAttack, Integer gearStrength, Integer ammoStrength, boolean usableWeapon,
			boolean activeHelm, boolean activeBody, boolean activeLegs, Set<Unmodelled> unmodelled)
		{
			this.family=Objects.requireNonNull(family); this.style=Objects.requireNonNull(style);
			this.prayer=Objects.requireNonNull(prayer); this.ammo=Objects.requireNonNull(ammo);
			this.realRanged=realRanged; this.visibleRanged=visibleRanged; this.gearAttack=gearAttack;
			this.gearStrength=gearStrength; this.ammoStrength=ammoStrength; this.usableWeapon=usableWeapon;
			this.activeHelm=activeHelm; this.activeBody=activeBody; this.activeLegs=activeLegs;
			this.unmodelled=Set.copyOf(unmodelled);
		}
	}
	public static final class Target
	{
		public final Integer defenceLevel, standardRangedDefence, magicLevel, magicAttackBonus;
		/** CoX only selects the tbow curve cap; target stats must already be explicitly scaled. */
		public final boolean chambers, statsKnown, ordinaryDistribution;
		public Target(Integer defenceLevel, Integer standardRangedDefence, Integer magicLevel, Integer magicAttackBonus,
			boolean chambers, boolean statsKnown, boolean ordinaryDistribution)
		{
			this.defenceLevel=defenceLevel; this.standardRangedDefence=standardRangedDefence;
			this.magicLevel=magicLevel; this.magicAttackBonus=magicAttackBonus;
			this.chambers=chambers; this.statsKnown=statsKnown; this.ordinaryDistribution=ordinaryDistribution;
		}
		public static Target neutral() { return new Target(0,0,0,0,false,true,true); }
	}
	public static final class Result
	{
		public final Integer maxHit, attackTicks, effectiveAccuracy, effectiveStrength;
		public final Long attackRoll, defenceRoll;
		public final Double hitChance, dps;
		public final String reason;
		private Result(Integer max, Integer ticks, Integer accuracy, Integer strength, Long attack, Long defence,
			Double chance, Double dps, String reason)
		{ maxHit=max; attackTicks=ticks; effectiveAccuracy=accuracy; effectiveStrength=strength;
			attackRoll=attack; defenceRoll=defence; hitChance=chance; this.dps=dps; this.reason=reason; }
		public boolean available() { return maxHit != null; }
	}
	public Result calculate(Setup setup, Target target)
	{
		Objects.requireNonNull(setup); Objects.requireNonNull(target);
		if (!setup.unmodelled.isEmpty()) { return gap("Unsupported mechanics: " + setup.unmodelled.stream()
			.map(Enum::name).sorted().collect(java.util.stream.Collectors.joining(", "))); }
		if (!setup.usableWeapon) { return gap("An active/charged and supplied weapon scenario is required; ownership alone is insufficient."); }
		if (!target.statsKnown || !target.ordinaryDistribution) { return gap("Explicit final target stats and ordinary damage distribution are required; scaling/target effects are unsupported."); }
		if (setup.realRanged == null || setup.visibleRanged == null || setup.gearAttack == null || setup.gearStrength == null)
		{ return gap("Ranged levels or complete offensive gear bonuses are unknown; unread gear is not zero."); }
		if (setup.realRanged < 1 || setup.realRanged > 99 || setup.visibleRanged < 1 || setup.visibleRanged > 150
			|| setup.gearAttack < -1000 || setup.gearAttack > 10000 || setup.gearStrength < -1000 || setup.gearStrength > 10000)
		{ return gap("Ranged input is outside the supported arithmetic domain."); }
		if (target.defenceLevel == null || target.standardRangedDefence == null || target.defenceLevel < 0
			|| target.defenceLevel > 10000 || target.standardRangedDefence < -64 || target.standardRangedDefence > 10000)
		{ return gap("Known standard ranged defence inputs are required."); }
		if (setup.ammo == Ammo.SEEKER_ARROW) { return gap("Seeker arrow successful-hit floor and eligibility are not modelled."); }
		boolean tbow=setup.family == Family.TWISTED_BOW;
		if (tbow && (setup.ammo != Ammo.ORDINARY_ARROW || setup.ammoStrength == null || setup.ammoStrength <= 0 || setup.ammoStrength > 1000)
			|| !tbow && (setup.ammo != Ammo.NONE || setup.ammoStrength == null || setup.ammoStrength != 0))
		{ return gap("Incompatible or unknown ammunition; crystal bows use no arrows and tbow requires supported ordinary arrows."); }
		if (tbow && (target.magicLevel == null || target.magicAttackBonus == null || target.magicLevel < 0 || target.magicLevel > 10000
			|| target.magicAttackBonus < -1000 || target.magicAttackBonus > 10000))
		{ return gap("Tbow requires target Magic level and offensive Magic bonus; magic defence bonus is a separate stat."); }
		int style=setup.style == Style.ACCURATE ? 3 : 0;
		int effectiveAccuracy=setup.visibleRanged * setup.prayer.accuracy / 100 + style + 8;
		int effectiveStrength=setup.visibleRanged * setup.prayer.strength / 100 + style + 8;
		long attack=(long)effectiveAccuracy * Math.max(0,64+setup.family.attack+setup.gearAttack);
		int strength=setup.family.strength+setup.gearStrength+setup.ammoStrength;
		int max=(int)(((long)effectiveStrength * Math.max(0,64+strength) + 320)/640);
		if (!tbow)
		{
			int crystal=(setup.activeHelm ? 1 : 0)+(setup.activeBody ? 3 : 0)+(setup.activeLegs ? 2 : 0);
			attack=attack*(20+crystal)/20; max=max*(40+crystal)/40;
		}
		else
		{
			int magic=Math.min(target.chambers ? 350 : 250,Math.max(target.magicLevel,target.magicAttackBonus));
			attack=attack*curvePercent(magic,true)/100;
			max=max*curvePercent(magic,false)/100;
		}
		if (max < 1) { return gap("Zero maximum-damage distributions are outside this first ranged increment."); }
		long defence=(long)(target.defenceLevel+9)*(target.standardRangedDefence+64);
		double chance=AttackMath.hitChance(attack,defence);
		int ticks=setup.family.ticks-(setup.style == Style.RAPID ? 1 : 0);
		return new Result(max,ticks,effectiveAccuracy,effectiveStrength,attack,defence,chance,
			chance*AttackMath.successfulMean(max)/(ticks*0.6),"Ordinary uninterrupted supplied-attack scenario; not measured performance or encounter time.");
	}
	/** Integer stages of the reviewed tbow curve; truncate each fractional term before multiplication. */
	private int curvePercent(int magic, boolean accuracy)
	{
		int offset=accuracy ? 10 : 14, ceiling=accuracy ? 140 : 250;
		int scaledMagic=3*magic/10;
		int distance=scaledMagic-10*offset;
		int percent=ceiling+(3*magic-offset)/100-distance*distance/100;
		return Math.max(0,Math.min(ceiling,percent));
	}
	private Result gap(String reason) { return new Result(null,null,null,null,null,null,null,null,reason); }
}
