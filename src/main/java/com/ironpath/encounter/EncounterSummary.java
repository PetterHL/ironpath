package com.ironpath.encounter;

import java.util.List;
import java.util.Set;

/** Frozen session evidence; engagement timing is not a game-reported full kill timer. */
public final class EncounterSummary
{
	public enum Outcome { COMPLETE, FAILED, CENSORED, EXCLUDED }
	public static final class Segment
	{
		public final int startTick, endTick;
		public final CombatState state;
		public Segment(int start, int end, CombatState state) { startTick=start; endTick=end; this.state=state; }
		public int ticks() { return Math.max(0,endTick-startTick); }
	}
	public final long key, generation;
	public final EncounterContext context;
	public final Outcome outcome;
	public final String adapterVersion, reason;
	public final int startTick, endTick, localDamage;
	public final List<Segment> segments;
	public final Set<String> gaps;
	public EncounterSummary(long key, long generation, EncounterContext context, Outcome outcome,
		String adapterVersion, String reason, int start, int end, int damage, List<Segment> segments, Set<String> gaps)
	{
		this.key=key; this.generation=generation; this.context=context; this.outcome=outcome;
		this.adapterVersion=adapterVersion; this.reason=reason; startTick=start; endTick=end; localDamage=damage;
		this.segments=List.copyOf(segments); this.gaps=Set.copyOf(gaps);
	}
	public String durationLabel()
	{ return String.format(java.util.Locale.ROOT,"Observed engagement %.1f s",Math.max(0,endTick-startTick)*0.6); }
}
