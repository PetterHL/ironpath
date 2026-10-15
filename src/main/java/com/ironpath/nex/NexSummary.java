package com.ironpath.nex;

import com.ironpath.encounter.CombatState;
import com.ironpath.encounter.EncounterSummary.Outcome;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** In-memory frozen evidence. No actors, names, hit objects or mutable client state. */
public final class NexSummary
{
	public static final String TIMER = "Observed arena barrier 2→3 to Nex death/dying; game ticks × 0.6";
	public static final String DAMAGE_SCOPE = "Observed mine / (mine + other) damage hitsplats on Nex + Fumus/Umbra/Cruor/Glacies; healing and unclassified types excluded. Not loot credit; thralls/DoT/overkill may differ.";
	public static final class Sample
	{
		public final int tick, animation;
		public final String phase, window, target, role;
		public final CombatState state;
		public Sample(int tick, String phase, String window, String target, String role, int animation, CombatState state)
		{ this.tick=tick; this.phase=phase; this.window=window; this.target=target; this.role=role; this.animation=animation; this.state=state; }
	}
	public static final class Damage
	{
		public final long local, total;
		public Damage(long local, long total) { this.local=local; this.total=total; }
	}
	public final long key;
	public final int startTick, endTick, sampledTicks, expectedTicks, unclassifiedHits;
	public final Outcome outcome;
	public final String reason, cohort, method;
	public final String timerScope;
	public final List<Sample> timeline;
	public final Map<String,Damage> damage;
	public final Set<String> gaps;
	public NexSummary(long key, int start, int end, int sampled, int expected, int unclassified,
		Outcome outcome, String reason, String cohort, String method, List<Sample> timeline,
		Map<String,Damage> damage, Set<String> gaps)
	{
		this.key=key; startTick=start; endTick=end; sampledTicks=sampled; expectedTicks=expected;
		unclassifiedHits=unclassified; this.outcome=outcome; this.reason=reason; this.cohort=cohort; this.method=method;
		this.timeline=List.copyOf(timeline); this.damage=Map.copyOf(damage); this.gaps=Set.copyOf(gaps);
		timerScope=outcome==Outcome.COMPLETE?TIMER:"Partial attempt: first captured tick to end/censor signal; excluded from complete averages";
	}
	public double seconds() { return Math.max(0,endTick-startTick)*0.6; }
	public long localDamage() { return damage.values().stream().mapToLong(d->d.local).sum(); }
	public long totalDamage() { return damage.values().stream().mapToLong(d->d.total).sum(); }
	public Double share() { return totalDamage()==0 ? null : 100.0*localDamage()/totalDamage(); }
}
