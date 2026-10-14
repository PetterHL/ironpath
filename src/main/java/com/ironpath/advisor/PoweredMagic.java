package com.ironpath.advisor;

import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Compatibility facade for P4a callers; all arithmetic now lives in the shared family service. */
public final class PoweredMagic
{
	public enum Prayer
	{
		NONE(100, 0), MYSTIC_MIGHT(115, 2), AUGURY(125, 4);
		final int accuracyPercent, damagePercent;
		Prayer(int accuracy, int damage) { accuracyPercent = accuracy; damagePercent = damage; }
	}
	public enum Boost { NONE, MAGIC_POTION, SATURATED_HEART }
	public enum Style { ACCURATE, LONGRANGE }
	public enum Status { SUPPORTED, CONDITIONAL, UNSUPPORTED }
	public static final List<String> SLOTS = List.of("head", "cape", "neck", "ammo", "weapon", "body", "shield", "legs", "hands", "feet", "ring");
	private final CombatComparisonService service;
	public PoweredMagic(KnowledgeCatalog catalog) { service = new CombatComparisonService(catalog); }
	public static final class Setup extends CombatSetup
	{
		public Setup(Map<String, Integer> slots, Prayer prayer, Boost boost, Style style, boolean confirmed, boolean template)
		{ this(slots, prayer, boost, style, confirmed, template, "neutral-zero-defence", Map.of()); }
		public Setup(Map<String, Integer> slots, Prayer prayer, Boost boost, Style style, boolean confirmed, boolean template,
			String target, Map<String, String> provenance)
		{ super(slots, prayer, boost, style, confirmed, template, target, provenance); }
		public Setup edit(String key, String value)
		{
			Map<String, Integer> next = new LinkedHashMap<>(slots);
			Map<String, String> origins = new LinkedHashMap<>(provenance);
			if (SLOTS.contains(key)) { next.put(key, Integer.valueOf(value)); origins.put(key, "explicit reference correction; ownership/levels revalidated"); }
			if (key.equals("template"))
			{
				SLOTS.stream().filter(slot -> !slot.equals("weapon")).forEach(slot -> {
					next.put(slot, 0); origins.put(slot, "explicit minimal template exclusion; unknown account gear remains unknown");
				});
			}
			return new Setup(next, key.equals("prayer") ? Prayer.valueOf(value) : prayer,
				key.equals("boost") ? Boost.valueOf(value) : boost, key.equals("style") ? Style.valueOf(value) : style,
				key.equals("confirm"), key.equals("template") || minimalTemplate,
				key.equals("target") ? value : target, origins);
		}
		public Setup unconfirmed() { return new Setup(slots, prayer, boost, style, false, minimalTemplate, target, provenance); }
	}
	public Setup propose(AccountSnapshot account) { return service.propose(account); }
	public Setup propose(AccountSnapshot account, String target) { return service.propose(account, target); }
	public Map<String, String> choices(String slot, AccountSnapshot a) { return service.choices(slot, a); }
	public Map<String, String> targetChoices() { return service.targetChoices(); }
	public String validate(AccountSnapshot a, Preferences p, Setup s) { return service.validate(a, p, s); }
	public Result compare(AccountSnapshot a, Preferences p, Setup s, String kind)
	{
		int id = kind.equals("OCCULT") ? 12002 : kind.equals("SWAMP") ? 12899 : 0;
		ComparisonResult result = service.compare(a, p, s, id);
		Status status = result.support.get(ComparisonResult.Metric.THEORETICAL_DPS);
		return new Result(status, result.reason, result.before, result.after, result.assumptions, result.summary());
	}
	public static double hitChance(long attack, long defence)
	{
		if (attack < 0 || defence < 0) { throw new IllegalArgumentException("Negative roll is unsupported"); }
		return attack > defence ? 1.0 - (defence + 2.0) / (2.0 * (attack + 1.0)) : attack / (2.0 * (defence + 1.0));
	}
	public static double successfulMean(int maxHit)
	{
		if (maxHit < 1) { throw new IllegalArgumentException("Unsupported max hit"); }
		return maxHit / 2.0 + 1.0 / (maxHit + 1.0);
	}
	public static final class Metrics
	{
		public final int maxHit, visibleMagic, effectiveMagic, attackBonus, attackTicks;
		public final double damagePercent, hitChance, dps;
		public final long attackRoll, defenceRoll;
		Metrics(int maxHit, double chance, double dps, int visible, int effective, int attack, double damage, long roll, long defence, int ticks)
		{ this.maxHit = maxHit; hitChance = chance; this.dps = dps; visibleMagic = visible; effectiveMagic = effective;
			attackBonus = attack; damagePercent = damage; attackRoll = roll; defenceRoll = defence; attackTicks = ticks; }
	}
	public static final class Result
	{
		public final Status status;
		public final String reason;
		public final Metrics before, after;
		public final List<String> assumptions;
		private final String summary;
		Result(Status status, String reason, Metrics before, Metrics after, List<String> assumptions, String summary)
		{ this.status = status; this.reason = reason; this.before = before; this.after = after; this.assumptions = List.copyOf(assumptions); this.summary = summary; }
		public String summary() { return summary; }
	}
}
