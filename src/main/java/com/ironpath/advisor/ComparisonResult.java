package com.ironpath.advisor;

import java.util.List;
import java.util.Map;

/** Support is per metric; max hit can remain valid when accuracy history is unknown. */
public final class ComparisonResult
{
	public enum Metric { MAX_HIT, HIT_CHANCE, THEORETICAL_DPS, EXPECTED_KILL_TIME, TRIP_PERFORMANCE }
	public enum OffenceScope { STEADY_REFERENCE, NEXT_FIVE_SWINGS }
	public final OffenceScope offenceScope;
	public final CombatSetup beforeSetup, afterSetup;
	public final PoweredMagic.Metrics before, after;
	public final TargetProfile target;
	public final Map<Metric, PoweredMagic.Status> support;
	public final Map<Metric, String> gaps;
	public final String reason, modelVersion, knowledgeVersion;
	public final List<String> assumptions;
	public final Metric primaryMetric;
	/** Domain classification retained independently of the displayed metric. */
	public final boolean weaponComparison;
	ComparisonResult(CombatSetup beforeSetup, CombatSetup afterSetup, PoweredMagic.Metrics before,
		PoweredMagic.Metrics after, TargetProfile target, Map<Metric, PoweredMagic.Status> support,
		String reason, String model, String knowledge, List<String> assumptions, boolean weaponComparison, Map<Metric, String> gaps)
	{
		this(beforeSetup,afterSetup,before,after,target,support,reason,model,knowledge,assumptions,dpsFirst,gaps,OffenceScope.STEADY_REFERENCE);
	}
	ComparisonResult(CombatSetup beforeSetup, CombatSetup afterSetup, PoweredMagic.Metrics before,
		PoweredMagic.Metrics after, TargetProfile target, Map<Metric, PoweredMagic.Status> support,
		String reason, String model, String knowledge, List<String> assumptions, boolean dpsFirst, Map<Metric, String> gaps,OffenceScope scope)
	{
		offenceScope=scope;
		this.beforeSetup = beforeSetup; this.afterSetup = afterSetup; this.before = before; this.after = after;
		this.target = target; this.support = Map.copyOf(support); this.reason = reason;
		this.gaps = Map.copyOf(gaps);
		modelVersion = model; knowledgeVersion = knowledge; this.assumptions = List.copyOf(assumptions);
		this.weaponComparison = weaponComparison;
		primaryMetric = Metric.THEORETICAL_DPS;
	}
	public boolean available(Metric metric) { return support.get(metric) != PoweredMagic.Status.UNSUPPORTED; }
	public double dpsGain() { return available(Metric.THEORETICAL_DPS) ? after.dps - before.dps : Double.NaN; }
	public boolean changesSetup()
	{
		if(beforeSetup==null || afterSetup==null) { return false; }
		long changes=PoweredMagic.SLOTS.stream().filter(s->!java.util.Objects.equals(beforeSetup.slots.get(s),afterSetup.slots.get(s))).count();
		return changes+(beforeSetup.loadedAmmo==afterSetup.loadedAmmo?0:1)>1;
	}
	public String headline()
	{
		if (before == null) { return primaryMetric == Metric.THEORETICAL_DPS ? "DPS unavailable · model gap" : "Combat value · model gap"; }
		if (primaryMetric == Metric.THEORETICAL_DPS)
		{
			return (available(Metric.THEORETICAL_DPS) ? String.format(java.util.Locale.ROOT,
				(offenceScope==OffenceScope.NEXT_FIVE_SWINGS ? "Five-swing mean DPS" : "DPS")+" %.2f → %.2f (%+.2f)", before.dps, after.dps, dpsGain()) : "DPS unavailable · model gap")
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
			(offenceScope==OffenceScope.NEXT_FIVE_SWINGS ? "five-swing mean DPS" : "theoretical DPS")+" %.2f → %.2f (%+.2f)", before.dps, after.dps, dpsGain()) : "theoretical DPS unavailable · model gap";
		String max = "Max hit " + before.maxHit + " → " + after.maxHit;
		return (beforeSetup.confirmed ? "Confirmed" : "Proposed") + " reference · "
			+ (primaryMetric == Metric.THEORETICAL_DPS ? dps + "; " + max : max + "; " + dps)
			+ "; " + hit + ". " + target.name + ". " + reason;
	}
}
