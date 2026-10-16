package com.ironpath.advisor;

import com.ironpath.model.Truth;
import java.util.List;
import java.util.function.Function;

/** Account-selected A0 method and remaining route. Numeric support is independent. */
public final class RemainingEffort
{
	public enum Band
	{
		SHORT("Short"), MEDIUM("Medium"), LONG("Long"), VERY_LONG("Very long"), UNKNOWN("Unavailable");
		public final String label;
		Band(String label) { this.label=label; }
	}
	public enum Suitability
	{
		SUITABLE("Suitable package"), WORKABLE("Workable with limitations"), PREPARE("Prepare first"), UNKNOWN("Method check needed");
		public final String label;
		Suitability(String label) { this.label=label; }
	}
	public final Band band;
	public final Suitability suitability;
	public final String profile, version, method, reason, scope, gap;
	public final List<String> sources;
	public final boolean acquisitionBypassed;

	RemainingEffort(Band band, Suitability suitability, String profile, String version, String method,
		String reason, String scope, String gap, List<String> sources, boolean bypassed)
	{
		this.band=band; this.suitability=suitability; this.profile=profile; this.version=version; this.method=method;
		this.reason=reason; this.scope=scope; this.gap=gap; this.sources=List.copyOf(sources); acquisitionBypassed=bypassed;
	}
	static RemainingEffort evaluate(ActivityEffortProfile profile, Band routeBand, String routeReason,
		Function<KnowledgeCatalog.Condition,Truth> truth, boolean bypassed)
	{
		if (bypassed)
		{ return new RemainingEffort(Band.SHORT,Suitability.SUITABLE,profile.id,profile.version,"Owned components / assembly",
			"Confirmed components remove acquisition; remaining skills, quantities and assembly still need checking.",
			"Remaining assembly and resources only",profile.gap,profile.sources,true); }
		ActivityEffortProfile.Method selected=profile.methods.stream().filter(m->truth.apply(m.when)==Truth.TRUE)
			.sorted(java.util.Comparator.comparing((ActivityEffortProfile.Method m)->m.suitability).thenComparing(m->m.id)).findFirst().orElse(null);
		if (selected==null)
		{
			boolean unknown=profile.methods.stream().anyMatch(m->truth.apply(m.when)==Truth.UNKNOWN);
			return new RemainingEffort(Band.UNKNOWN,unknown?Suitability.UNKNOWN:Suitability.PREPARE,profile.id,profile.version,
				"Check a reviewed compatible method",unknown?"Essential method inputs are unconfirmed; unread gear is not absent."
					+ " " + nextCheck(profile.methods.get(0).when,truth)
					:"The reviewed method has foundational preparation gaps. " + nextCheck(profile.methods.get(0).when,truth),
				profile.scope,profile.gap,profile.sources,false);
		}
		Band band=routeBand==Band.UNKNOWN ? Band.UNKNOWN : selected.minimumBand.ordinal()>routeBand.ordinal()?selected.minimumBand:routeBand;
		return new RemainingEffort(band,selected.suitability,profile.id,profile.version,selected.label,
			routeReason + " " + selected.limitation,profile.scope,profile.gap,profile.sources,false);
	}
	private static String nextCheck(KnowledgeCatalog.Condition condition,Function<KnowledgeCatalog.Condition,Truth> truth)
	{
		if (condition.test==KnowledgeCatalog.Test.ALL)
		{
			KnowledgeCatalog.Condition missing=condition.conditions.stream().filter(c->truth.apply(c)==Truth.FALSE).findFirst()
				.orElseGet(()->condition.conditions.stream().filter(c->truth.apply(c)!=Truth.TRUE).findFirst().orElse(condition.conditions.get(0)));
			return nextCheck(missing,truth);
		}
		if (condition.test==KnowledgeCatalog.Test.ANY) { return "Check one adequate alternative: " + condition.conditions.stream()
			.map(c->nextCheck(c,truth)).collect(java.util.stream.Collectors.joining(" / ")); }
		if (condition.test==KnowledgeCatalog.Test.LEVEL) { return "Check " + condition.key.toLowerCase(java.util.Locale.ROOT) + " " + condition.minimum + "."; }
		return "Check " + (condition.key==null?"the method inputs":condition.key.replace('-',' ')) + "; observe usable gear and recheck supplies.";
	}
}
