package com.ironpath.advisor;

import java.util.List;
import java.util.Map;

/** Support is per metric; max hit can remain valid when accuracy history is unknown. */
public final class ComparisonResult
{
	public enum Metric { MAX_HIT, HIT_CHANCE, THEORETICAL_DPS, EXPECTED_KILL_TIME, TRIP_PERFORMANCE }
	public final CombatSetup beforeSetup, afterSetup;
	public final PoweredMagic.Metrics before, after;
	public final TargetProfile target;
	public final Map<Metric, PoweredMagic.Status> support;
	public final String reason, modelVersion, knowledgeVersion;
	public final List<String> assumptions;
	public final Metric primaryMetric;
	ComparisonResult(CombatSetup beforeSetup, CombatSetup afterSetup, PoweredMagic.Metrics before,
		PoweredMagic.Metrics after, TargetProfile target, Map<Metric, PoweredMagic.Status> support,
		String reason, String model, String knowledge, List<String> assumptions, boolean dpsFirst)
	{
		this.beforeSetup = beforeSetup; this.afterSetup = afterSetup; this.before = before; this.after = after;
		this.target = target; this.support = Map.copyOf(support); this.reason = reason;
		modelVersion = model; knowledgeVersion = knowledge; this.assumptions = List.copyOf(assumptions);
		primaryMetric = dpsFirst ? Metric.THEORETICAL_DPS : Metric.MAX_HIT;
	}
	public boolean available(Metric metric) { return support.get(metric) != PoweredMagic.Status.UNSUPPORTED; }
	public double dpsGain() { return available(Metric.THEORETICAL_DPS) ? after.dps - before.dps : Double.NaN; }
	public String headline()
	{
		if (before == null) { return primaryMetric == Metric.THEORETICAL_DPS ? "DPS unavailable · model gap" : "Combat value · model gap"; }
		if (primaryMetric == Metric.THEORETICAL_DPS)
		{
			return (available(Metric.THEORETICAL_DPS) ? String.format(java.util.Locale.ROOT,
				"DPS %.2f → %.2f (%+.2f)", before.dps, after.dps, dpsGain()) : "DPS unavailable · model gap")
				+ " · " + target.name;
		}
		return String.format(java.util.Locale.ROOT, "%+d max hit · %s", after.maxHit-before.maxHit, target.name);
	}
	public String summary()
	{
		if (before == null) { return "Comparison unavailable: " + reason; }
		String hit = available(Metric.HIT_CHANCE) ? String.format(java.util.Locale.ROOT,
			"hit chance %.1f%% → %.1f%% (%+.1f percentage points)", before.hitChance * 100, after.hitChance * 100,
			(after.hitChance - before.hitChance) * 100) : "hit chance unavailable";
		String dps = available(Metric.THEORETICAL_DPS) ? String.format(java.util.Locale.ROOT,
			"theoretical DPS %.2f → %.2f (%+.2f)", before.dps, after.dps, dpsGain()) : "theoretical DPS unavailable · model gap";
		String max = "Max hit " + before.maxHit + " → " + after.maxHit;
		return (beforeSetup.confirmed ? "Confirmed" : "Proposed") + " reference · "
			+ (primaryMetric == Metric.THEORETICAL_DPS ? dps + "; " + max : max + "; " + dps)
			+ "; " + hit + ". " + target.name + ". " + reason;
	}
}
