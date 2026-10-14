package com.ironpath.advisor;

import java.util.Map;

/** Complete memory-only reference. -1 is unknown, 0 explicitly excludes a slot.
 * Prayer, boost, supplies and attack style are assumptions, never live combat state. */
public class CombatSetup
{
	public enum Family { POWERED_MAGIC, RANGED, MELEE }
	public final Family family;
	public final MeleeModel.Prayer meleePrayer;
	public final MeleeModel.Boost meleeBoost;
	public final MeleeModel.Style meleeStyle;
	public final MeleeModel.Souls souls;
	public final int defenceOverride;
	public final RangedModel.Prayer rangedPrayer;
	public final RangedModel.Style rangedStyle;
	public final boolean rangingPotion, onTask;
	public final int loadedAmmo;
	public final Map<String, Integer> slots;
	public final Map<String, String> provenance;
	public final PoweredMagic.Prayer prayer;
	public final PoweredMagic.Boost boost;
	public final PoweredMagic.Style style;
	public final String target;
	public final boolean confirmed, minimalTemplate;
	public CombatSetup(Map<String, Integer> slots, PoweredMagic.Prayer prayer, PoweredMagic.Boost boost,
		PoweredMagic.Style style, boolean confirmed, boolean template, String target, Map<String, String> provenance)
	{
		this(slots, prayer, boost, style, confirmed, template, target, provenance, Family.POWERED_MAGIC,
			RangedModel.Prayer.NONE, RangedModel.Style.RAPID, false, false, 0);
	}
	public CombatSetup(Map<String, Integer> slots, PoweredMagic.Prayer prayer, PoweredMagic.Boost boost,
		PoweredMagic.Style style, boolean confirmed, boolean template, String target, Map<String, String> provenance,
		Family family, RangedModel.Prayer rangedPrayer, RangedModel.Style rangedStyle,
		boolean rangingPotion, boolean onTask, int loadedAmmo)
	{
		this(slots,prayer,boost,style,confirmed,template,target,provenance,family,rangedPrayer,rangedStyle,rangingPotion,onTask,loadedAmmo,
			MeleeModel.Prayer.NONE,MeleeModel.Boost.NONE,MeleeModel.Style.SLASH_CONTROLLED);
	}
	private CombatSetup(Map<String,Integer> slots, PoweredMagic.Prayer prayer, PoweredMagic.Boost boost,
		PoweredMagic.Style style, boolean confirmed, boolean template, String target, Map<String,String> provenance,
		Family family, RangedModel.Prayer rangedPrayer, RangedModel.Style rangedStyle, boolean rangingPotion, boolean onTask, int loadedAmmo,
		MeleeModel.Prayer meleePrayer, MeleeModel.Boost meleeBoost, MeleeModel.Style meleeStyle)
	{
		this(slots,prayer,boost,style,confirmed,template,target,provenance,family,rangedPrayer,rangedStyle,rangingPotion,onTask,loadedAmmo,
			meleePrayer,meleeBoost,meleeStyle,MeleeModel.Souls.START,-1);
	}
	private CombatSetup(Map<String,Integer> slots, PoweredMagic.Prayer prayer, PoweredMagic.Boost boost,
		PoweredMagic.Style style, boolean confirmed, boolean template, String target, Map<String,String> provenance,
		Family family, RangedModel.Prayer rangedPrayer, RangedModel.Style rangedStyle, boolean rangingPotion, boolean onTask, int loadedAmmo,
		MeleeModel.Prayer meleePrayer, MeleeModel.Boost meleeBoost, MeleeModel.Style meleeStyle, MeleeModel.Souls souls, int defenceOverride)
	{
		this.souls=souls; this.defenceOverride=defenceOverride;
		this.meleePrayer=meleePrayer; this.meleeBoost=meleeBoost; this.meleeStyle=meleeStyle;
		this.slots = Map.copyOf(slots); this.prayer = prayer; this.boost = boost; this.style = style;
		this.confirmed = confirmed; minimalTemplate = template; this.target = target;
		this.provenance = Map.copyOf(provenance);
		this.family = family; this.rangedPrayer = rangedPrayer; this.rangedStyle = rangedStyle;
		this.rangingPotion = rangingPotion; this.onTask = onTask; this.loadedAmmo = loadedAmmo;
	}
	public static CombatSetup melee(Map<String,Integer> slots, String target, Map<String,String> provenance)
	{ return new CombatSetup(slots,PoweredMagic.Prayer.NONE,PoweredMagic.Boost.NONE,PoweredMagic.Style.ACCURATE,
		false,false,target,provenance,Family.MELEE,RangedModel.Prayer.NONE,RangedModel.Style.RAPID,false,false,0); }
	public CombatSetup meleeCopy(Map<String,Integer> next, Map<String,String> origins, MeleeModel.Style attackStyle)
	{ return new CombatSetup(next,prayer,boost,style,false,minimalTemplate,target,origins,family,rangedPrayer,rangedStyle,rangingPotion,onTask,loadedAmmo,
		meleePrayer,meleeBoost,attackStyle,souls,defenceOverride); }
	public static CombatSetup ranged(Map<String, Integer> slots, String target, Map<String, String> provenance)
	{ return new CombatSetup(slots, PoweredMagic.Prayer.NONE, PoweredMagic.Boost.NONE, PoweredMagic.Style.ACCURATE,
		false, false, target, provenance, Family.RANGED, RangedModel.Prayer.NONE, RangedModel.Style.RAPID, false, false, 0); }
	public CombatSetup rangedCopy(Map<String, Integer> next, Map<String, String> origins, String targetId, int darts)
	{ return new CombatSetup(next, prayer, boost, style, false, minimalTemplate, targetId, origins,
		family, rangedPrayer, rangedStyle, rangingPotion, onTask, darts); }
	public CombatSetup edit(String key, String value)
	{
		Map<String, Integer> next = new java.util.LinkedHashMap<>(slots);
		Map<String, String> origins = new java.util.LinkedHashMap<>(provenance);
		if (PoweredMagic.SLOTS.contains(key)) { next.put(key, Integer.valueOf(value)); origins.put(key, "Explicit reference correction; ownership/requirements revalidated."); }
		if (family==Family.MELEE && key.equals("style")) { origins.put("melee-style","Explicit melee style correction."); }
		if (key.equals("template")) { PoweredMagic.SLOTS.stream().filter(s -> !s.equals("weapon") && !s.equals("ammo")).forEach(s -> {
			next.put(s, 0); origins.put(s, "Explicit template exclusion; other account gear remains unknown."); }); }
		if (family==Family.MELEE)
		{
			return new CombatSetup(next,prayer,boost,style,key.equals("confirm"),key.equals("template") || minimalTemplate,
				key.equals("target") ? value : target,origins,family,rangedPrayer,rangedStyle,rangingPotion,
				key.equals("task") ? parseBoolean(value) : onTask,loadedAmmo,
				key.equals("prayer") ? MeleeModel.Prayer.valueOf(value) : meleePrayer,
				key.equals("boost") ? MeleeModel.Boost.valueOf(value) : meleeBoost,
				key.equals("style") ? MeleeModel.Style.valueOf(value) : meleeStyle,
				key.equals("souls") ? MeleeModel.Souls.valueOf(value) : souls,
				key.equals("target") ? -1 : key.equals("defence") ? Integer.parseInt(value) : defenceOverride);
		}
		return new CombatSetup(next, prayer, boost, style, key.equals("confirm"), key.equals("template") || minimalTemplate,
			key.equals("target") ? value : target, origins, family,
			key.equals("prayer") ? RangedModel.Prayer.valueOf(value) : rangedPrayer,
			key.equals("style") ? RangedModel.Style.valueOf(value) : rangedStyle,
			key.equals("boost") ? parseBoolean(value) : rangingPotion,
			key.equals("task") ? parseBoolean(value) : onTask,
			key.equals("loaded-ammo") ? Integer.parseInt(value) : loadedAmmo);
	}
	private static boolean parseBoolean(String value)
	{ if (!value.equals("true") && !value.equals("false")) { throw new IllegalArgumentException("Invalid boolean assumption"); } return Boolean.parseBoolean(value); }
	public CombatSetup unconfirmed()
	{ return new CombatSetup(slots, prayer, boost, style, false, minimalTemplate, target, provenance,
		family, rangedPrayer, rangedStyle, rangingPotion, onTask, loadedAmmo, meleePrayer, meleeBoost, meleeStyle,souls,defenceOverride); }
	public String assumptionLabel()
	{ return family == Family.MELEE ? meleePrayer.label() + " · " + meleeBoost.label() + " · " + meleeStyle.label()
		+ " · " + (onTask ? "Eligible task assumed" : "Off-task") + " · " + souls.label()
		+ (defenceOverride<0 ? " · target reference Defence" : " · assumed target Defence "+defenceOverride)
		: family == Family.RANGED ? rangedPrayer + " · " + (rangingPotion ? "Ranging potion" : "No boost") + " · " + rangedStyle
		: prayer + " · " + boost + " · " + style; }
}
