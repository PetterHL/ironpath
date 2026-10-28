package com.ironpath.nex;

import com.ironpath.encounter.CombatState;
import com.ironpath.encounter.EncounterSummary.Outcome;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** In-memory frozen evidence. No actors, names, hit objects or mutable client state. */
public final class NexSummary
{
	public static final String TIMER = "Observed Nex smoke announcement to Nex death/dying; game ticks × 0.6. Not calibrated to the game timer.";
	public static final String DAMAGE_SCOPE = "Observed mine / (mine + other) damage hitsplats on Nex + Fumus/Umbra/Cruor/Glacies; healing and unclassified types excluded. Whole-kill coverage UNVERIFIED; this ratio is not whole-kill damage contribution or loot credit; thralls/DoT/overkill may differ.";
	public static final class Sample
	{
		public final int tick, animation;
		public final String phase, window, target, role;
		public final CombatState state;
		public final NexTargetState targetState;
		public Sample(int tick, String phase, String window, String target, String role, int animation, CombatState state)
		{ this(tick,phase,window,target,role,animation,state,NexTargetState.UNKNOWN); }
		public Sample(int tick,String phase,String window,String target,String role,int animation,CombatState state,NexTargetState targetState)
		{ this.tick=tick; this.phase=phase; this.window=window; this.target=target; this.role=role; this.animation=animation; this.state=state; this.targetState=java.util.Objects.requireNonNull(targetState); }
	}
	public static final class Damage
	{
		public final long local, total;
		public Damage(long local, long total) { this.local=local; this.total=total; }
	}
	public enum StartSource { UNKNOWN, SMOKE_ANNOUNCEMENT, ARENA_BARRIER_TRANSITION, ACTIVE_ARENA_POLL }
	/** Scalar capture origin, distinct from the later smoke-anchored measurement start. */
	public static final class Origin
	{
		public static final Origin UNKNOWN=new Origin(null,StartSource.UNKNOWN,NexTargetState.Form.UNKNOWN,null);
		public final Integer tick,barrier;
		public final StartSource source;
		public final NexTargetState.Form form;
		public Origin(Integer tick,StartSource source,NexTargetState.Form form,Integer barrier)
		{
			this.tick=tick; this.source=source==null?StartSource.UNKNOWN:source;
			this.form=form==null?NexTargetState.Form.UNKNOWN:form;
			this.barrier=barrier!=null && barrier>=0 && barrier<=3?barrier:null;
		}
	}
	/** Exact observed signals, for explaining capture eligibility without reconstructing events. */
	public static final class Signals
	{
		public final Integer boundaryTick, smokeTick, deathTick, barrierEndTick;
		public final boolean continuous;
		public final Set<String> phases;
		public final Integer observedSmokeTick;
		public final List<String> trace;
		public final Origin origin;
		public Signals(Integer boundaryTick,Integer smokeTick,Integer deathTick,Integer barrierEndTick,boolean continuous,Set<String> phases)
		{ this(boundaryTick,smokeTick,deathTick,barrierEndTick,continuous,phases,smokeTick,List.of()); }
		public Signals(Integer boundaryTick,Integer smokeTick,Integer deathTick,Integer barrierEndTick,boolean continuous,Set<String> phases,
			Integer observedSmokeTick,List<String> trace)
		{ this(boundaryTick,smokeTick,deathTick,barrierEndTick,continuous,phases,observedSmokeTick,trace,Origin.UNKNOWN); }
		public Signals(Integer boundaryTick,Integer smokeTick,Integer deathTick,Integer barrierEndTick,boolean continuous,Set<String> phases,
			Integer observedSmokeTick,List<String> trace,Origin origin)
		{
			this.boundaryTick=boundaryTick; this.smokeTick=smokeTick; this.deathTick=deathTick; this.barrierEndTick=barrierEndTick;
			this.continuous=continuous; this.phases=Set.copyOf(phases);
			this.observedSmokeTick=observedSmokeTick; this.trace=List.copyOf(trace);
			this.origin=java.util.Objects.requireNonNull(origin);
		}
	}
	public final long key;
	public final int startTick, endTick, sampledTicks, expectedTicks, unclassifiedHits;
	public final Outcome outcome;
	public final String reason, cohort, method;
	public final String timerScope;
	public final List<Sample> timeline;
	public final List<Sample> beforeStart;
	public final Map<String,Damage> damage;
	public final Set<String> gaps;
	public final Signals signals;
	public final NexContext context;
	public final NexComparability comparability;
	public final NexObservations observations;
	public final NexActions actions;
	public NexSummary(long key, int start, int end, int sampled, int expected, int unclassified,
		Outcome outcome, String reason, String cohort, String method, List<Sample> timeline,
		Map<String,Damage> damage, Set<String> gaps)
	{ this(key,start,end,sampled,expected,unclassified,outcome,reason,cohort,method,timeline,damage,gaps,null); }
	public NexSummary(long key, int start, int end, int sampled, int expected, int unclassified,
		Outcome outcome, String reason, String cohort, String method, List<Sample> timeline,
		Map<String,Damage> damage, Set<String> gaps, Signals signals)
	{ this(key,start,end,sampled,expected,unclassified,outcome,reason,cohort,method,timeline,damage,gaps,signals,List.of()); }
	public NexSummary(long key, int start, int end, int sampled, int expected, int unclassified,
		Outcome outcome, String reason, String cohort, String method, List<Sample> timeline,
		Map<String,Damage> damage, Set<String> gaps, Signals signals, List<Sample> beforeStart)
	{ this(key,start,end,sampled,expected,unclassified,outcome,reason,cohort,method,timeline,damage,gaps,signals,beforeStart,NexContext.UNKNOWN,null); }
	public NexSummary(long key,int start,int end,int sampled,int expected,int unclassified,Outcome outcome,String reason,
		String cohort,String method,List<Sample> timeline,Map<String,Damage> damage,Set<String> gaps,Signals signals,List<Sample> beforeStart,
		NexContext context,NexComparability comparability)
	{ this(key,start,end,sampled,expected,unclassified,outcome,reason,cohort,method,timeline,damage,gaps,signals,beforeStart,context,comparability,NexObservations.EMPTY); }
	public NexSummary(long key,int start,int end,int sampled,int expected,int unclassified,Outcome outcome,String reason,
		String cohort,String method,List<Sample> timeline,Map<String,Damage> damage,Set<String> gaps,Signals signals,List<Sample> beforeStart,
		NexContext context,NexComparability comparability,NexObservations observations)
	{ this(key,start,end,sampled,expected,unclassified,outcome,reason,cohort,method,timeline,damage,gaps,signals,beforeStart,context,comparability,observations,NexActions.EMPTY); }
	public NexSummary(long key,int start,int end,int sampled,int expected,int unclassified,Outcome outcome,String reason,
		String cohort,String method,List<Sample> timeline,Map<String,Damage> damage,Set<String> gaps,Signals signals,List<Sample> beforeStart,
		NexContext context,NexComparability comparability,NexObservations observations,NexActions actions)
	{
		this.actions=java.util.Objects.requireNonNull(actions);
		this.observations=java.util.Objects.requireNonNull(observations);
		this.key=key; startTick=start; endTick=end; sampledTicks=sampled; expectedTicks=expected;
		unclassifiedHits=unclassified; this.outcome=outcome; this.reason=reason; this.cohort=cohort; this.method=method;
		this.timeline=List.copyOf(timeline); this.damage=Map.copyOf(damage); this.gaps=Set.copyOf(gaps);
		this.beforeStart=List.copyOf(beforeStart);
		this.signals=signals;
		this.context=java.util.Objects.requireNonNull(context); this.comparability=comparability;
		timerScope=outcome==Outcome.COMPLETE?TIMER:"Incomplete capture; interval excluded from complete averages";
	}
	public double seconds() { return Math.max(0,endTick-startTick)*0.6; }
	public long localDamage() { return damage.values().stream().mapToLong(d->d.local).sum(); }
	public long totalDamage() { return damage.values().stream().mapToLong(d->d.total).sum(); }
	/** Ratio of recorded classified payloads only; no whole-kill coverage or contribution support. */
	public Double share() { return totalDamage()==0 ? null : 100.0*localDamage()/totalDamage(); }
}
