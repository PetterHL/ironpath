package com.ironpath.nex;

import com.ironpath.encounter.CombatState;
import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.preferences.NexMethod;
import java.util.*;
import java.util.function.Consumer;

/** Bounded deterministic Nex evidence machine. The adapter supplies reviewed signals and spawn keys. */
public final class NexCapture
{
	public static final String VERSION="nex-public-12";
	public static final int MAX_TICKS=3000, MAX_SAMPLES=1024, MAX_EVENTS=20000, MAX_SIGNALS=32,
		END_GRACE=5, END_WAIT=20;
	private final Consumer<NexSummary> sink;
	private final List<NexSummary.Sample> timeline=new ArrayList<>();
	private final List<NexSummary.Sample> beforeStart=new ArrayList<>();
	private final Map<String,NexSummary.Damage> damage=new LinkedHashMap<>();
	private final Set<Long> hitKeys=new HashSet<>();
	private final List<String> signals=new ArrayList<>();
	private final List<NexObservations.Launch> launches=new ArrayList<>();
	private final List<NexObservations.Signal> resourceSignals=new ArrayList<>();
	private final List<NexObservations.Flight> flights=new ArrayList<>();
	private final List<NexObservations.Impact> impacts=new ArrayList<>();
	private final List<NexObservations.Health> health=new ArrayList<>();
	private final Map<NexObservations.HudCheck,Integer> hudChecks=new EnumMap<>(NexObservations.HudCheck.class);
	private boolean observationsTruncated;
	private final List<NexActions.Signal> actions=new ArrayList<>();
	private boolean actionsTruncated;
	private final NexCallbackDiagnostics.Builder callbacks=new NexCallbackDiagnostics.Builder();
	public void callback(NexCallbackDiagnostics.Target target,NexCallbackDiagnostics.Stage stage,int amount)
	{ if(active()) { callbacks.add(target,stage,amount); } }
	private final Set<String> gaps=new LinkedHashSet<>(), phases=new LinkedHashSet<>(), gear=new TreeSet<>();
	private long serial, key;
	private volatile NexMethod method;
	private NexContext context;
	private int origin, start, last, sampled, lastSample, boundaryTick=-1, death=-1, barrierEnd=-1,
		smokeTick=-1, observedSmoke=-1, events, unclassified;
	private String phase="UNKNOWN", window="UNKNOWN", roster;
	private boolean fullStart, continuous, rosterStable;

	public NexCapture(Consumer<NexSummary> sink) { this.sink=sink; }
	public boolean active() { return method!=null; }
	public long key() { return key; }
	/** Client-thread scalar snapshot for the recording indicator, without exposing the live trace. */
	public Progress progress()
	{ return active()?new Progress(key,Math.max(0,(death>=0?death:last)-start),phase,fullStart):null; }
	public static final class Progress
	{
		public final long key;
		public final int elapsedTicks;
		public final String phase;
		public final boolean smokeObserved;
		private Progress(long key,int elapsedTicks,String phase,boolean smokeObserved)
		{ this.key=key; this.elapsedTicks=elapsedTicks; this.phase=phase; this.smokeObserved=smokeObserved; }
	}
	/** A true start flag denotes an actually observed smoke announcement, never arena entry. */
	public void start(int tick, boolean smokeObserved, NexMethod method, String roster)
	{ start(tick,smokeObserved,method,roster,NexContext.PILOT); }
	public void start(int tick,boolean smokeObserved,NexMethod method,String roster,NexContext defaults)
	{
		if (active()) { abort(tick,Outcome.CENSORED,"New encounter before completion"); }
		this.method=Objects.requireNonNull(method); key=++serial; origin=start=last=tick; lastSample=tick-1;
		this.context=Objects.requireNonNull(defaults).inherited();
		this.roster=roster; fullStart=smokeObserved; continuous=true; rosterStable=roster!=null;
		smokeTick=observedSmoke=smokeObserved?tick:-1;
		phase=fullStart?"SMOKE":"UNKNOWN"; window="UNKNOWN";
		if(fullStart) { phases.add(phase); }
		else { gaps.add("Smoke start not observed; capture may have begun before smoke or mid-kill"); }
		baselineGaps();
	}
	private void baselineGaps()
	{
		if (roster==null) { gaps.add("Arena roster unknown; matching team composition is not established"); }
		gaps.add("Impact weapon/launch identity unknown, including same-tick switches and delayed hits");
		gaps.add("Phase on a boundary tick follows observed client event order");
		gaps.add("Inventory deltas are not confirmed food/potion consumption; internal item charges may be unread");
	}
	/** Anchor only an unknown pre-combat interval; retain earlier states separately without backfilling. */
	public boolean anchorSmoke(int tick, String roster)
	{
		observeSmoke(tick);
		if(!active() || fullStart || death>=0 || barrierEnd>=0 || !phases.isEmpty() || !window.equals("UNKNOWN")
			|| unclassified>0 || damage.values().stream().anyMatch(d->d.total>0) || !event(tick)) { return false; }
		beforeStart.addAll(timeline); timeline.clear(); damage.clear(); hitKeys.clear(); gaps.clear(); gear.clear();
		clearObservations();
		actions.removeIf(a->a.tick<tick-NexActions.PRE_TICKS);
		start=tick; lastSample=tick-1; sampled=0; fullStart=true; smokeTick=tick;
		phase="SMOKE"; phases.add(phase); continuous=true; this.roster=roster; rosterStable=roster!=null;
		baselineGaps();
		return true;
	}
	public void barrierStart(int tick) { if(active() && boundaryTick<0) { boundaryTick=tick; } }
	public void observeSmoke(int tick) { if(active() && observedSmoke<0) { observedSmoke=tick; } }
	/** Bounded in-memory diagnostics retain the beginning and most recent signals. */
	public void signal(String entry)
	{
		if(!active()) { return; }
		if(signals.size()>=MAX_SIGNALS) { signals.remove(MAX_SIGNALS/2); }
		signals.add(entry);
	}
	private boolean event(int tick)
	{
		if (!active()) { return false; }
		if (tick<last) { continuous=false; gaps.add("Out-of-order event"); return false; }
		last=tick;
		if (++events>MAX_EVENTS || tick-origin>MAX_TICKS)
		{ abort(tick,Outcome.CENSORED,"Capture event/time limit reached"); return false; }
		return true;
	}
	public void phase(int tick, String next, String nextWindow)
	{
		if (!event(tick) || death>=0) { return; }
		if (next!=null)
		{
			List<String> order=List.of("SMOKE","SHADOW","BLOOD","ICE","ZAROS");
			if (order.indexOf(next)<order.indexOf(phase)) { gaps.add("Late phase signal ignored"); return; }
			if (!phase.equals(next)) { phase=next; phases.add(next); window="UNKNOWN"; }
		}
		if (nextWindow!=null) { window=nextWindow; }
	}
	public void sample(int tick, CombatState state, String target, String role, int animation, String roster)
	{ sample(tick,state,target,role,animation,roster,NexTargetState.UNKNOWN); }
	public void sample(int tick,CombatState state,String target,String role,int animation,String roster,NexTargetState targetState)
	{
		if (!event(tick)) { return; }
		if (death<0)
		{
			if (tick>lastSample)
			{
				if (tick>lastSample+1) { continuous=false; gaps.add("Missing game-tick samples"); }
				++sampled; lastSample=tick;
			}
			if (state==null || state.slots.values().stream().anyMatch(id->id<0))
			{ continuous=false; gaps.add("Worn state unread"); }
			if (!Objects.equals(this.roster,roster)) { rosterStable=false; gaps.add("Observed arena roster changed; team cohort unmatched"); }
			if (state!=null)
			{
				int ring=state.slots.getOrDefault("ring",-1);
				if (ring>=0 && ring!=25975) { gaps.add("Observed ring differs from the selected fixed-Lightbearer method"); }
				gear.add(role+":"+new TreeMap<>(state.slots)+":"+new TreeMap<>(state.realLevels)
					+":"+new TreeMap<>(state.visibleLevels)+":"+new TreeSet<>(state.prayers));
				NexSummary.Sample previous=timeline.isEmpty()?null:timeline.get(timeline.size()-1);
				if (previous==null || animation>=0 || !previous.state.equals(state) || !previous.phase.equals(phase)
					|| !previous.window.equals(window) || !Objects.equals(previous.target,target) || !previous.role.equals(role) || previous.targetState.form!=targetState.form)
				{
					if (timeline.size()+beforeStart.size()>=MAX_SAMPLES) { abort(tick,Outcome.CENSORED,"State buffer limit reached"); return; }
					timeline.add(new NexSummary.Sample(tick,phase,window,target,role,animation,state,targetState));
				}
			}
		}
		finishIfReady(tick,false);
	}
	/** The caller supplies a distinct event key, never a mutable hitsplat object identity. */
	public boolean hit(int tick, long eventKey, String target, int amount, boolean mine, boolean other, boolean heal)
	{
		if(!active()) { return false; }
		var scopeTarget=NexCallbackDiagnostics.target(target);
		NexCallbackDiagnostics.Stage stage;
		if(tick<last) { stage=NexCallbackDiagnostics.Stage.HIT_OUT_OF_ORDER; }
		else if(amount<0) { stage=NexCallbackDiagnostics.Stage.HIT_INVALID_AMOUNT; }
		else if(death>=0 && tick>death) { stage=NexCallbackDiagnostics.Stage.HIT_AFTER_DEATH; }
		else if(hitKeys.contains(eventKey)) { stage=NexCallbackDiagnostics.Stage.HIT_DUPLICATE_KEY; }
		else if(events>=MAX_EVENTS || tick-origin>MAX_TICKS) { stage=NexCallbackDiagnostics.Stage.HIT_CAPTURE_LIMIT; }
		else if(heal) { stage=NexCallbackDiagnostics.Stage.HIT_HEAL; }
		else if(!mine && !other) { stage=NexCallbackDiagnostics.Stage.HIT_UNCLASSIFIED; }
		else { stage=mine?NexCallbackDiagnostics.Stage.HIT_OWN:NexCallbackDiagnostics.Stage.HIT_OTHER; }
		callback(scopeTarget,stage,amount);
		if(stage==NexCallbackDiagnostics.Stage.HIT_INVALID_AMOUNT || stage==NexCallbackDiagnostics.Stage.HIT_AFTER_DEATH
			|| stage==NexCallbackDiagnostics.Stage.HIT_DUPLICATE_KEY) { return false; }
		if(!event(tick)) { return false; }
		hitKeys.add(eventKey);
		if (heal) { gaps.add("Healing observed and excluded from damage ratio"); return false; }
		if (!mine && !other) { ++unclassified; gaps.add("Unclassified hitsplats excluded from damage ratio"); return false; }
		String scope=phase+" / "+window+" / "+target;
		NexSummary.Damage before=damage.getOrDefault(scope,new NexSummary.Damage(0,0));
		damage.put(scope,new NexSummary.Damage(before.local+(mine?amount:0),before.total+amount));
		return true;
	}
	public void death(int tick) { if (event(tick) && death<0) { death=tick; } }
	public void barrierEnd(int tick) { if (event(tick) && barrierEnd<0) { barrierEnd=tick; } }
	public void gap(String gap) { if (active() && gaps.size()<64) { gaps.add(gap); } }
	private boolean observation(int tick)
	{ return observation(tick,false); }
	private boolean observation(int tick,boolean resourceTail)
	{ return observation(tick,resourceTail,null,null,null,0); }
	private boolean observation(int tick,boolean resourceTail,String target,NexCallbackDiagnostics.Stage retained,NexCallbackDiagnostics.Stage omitted,int amount)
	{
		if(!active()) { return false; }
		boolean tail=!resourceTail && death>=0 && tick>death;
		boolean full=launches.size()+resourceSignals.size()+flights.size()+impacts.size()+health.size()>=NexObservations.MAX;
		// Freeze the boundary count before event() can finish/clear a capture at its main limit.
		if(retained!=null) { callback(NexCallbackDiagnostics.target(target),tail || full || tick<last || events>=MAX_EVENTS || tick-origin>MAX_TICKS?omitted:retained,amount); }
		if(tail) { return false; }
		if(full) { observationsTruncated=true; return false; }
		return event(tick);
	}
	/** Action rows have a separate bound; diagnostic overflow cannot censor the main capture. */
	public void actionTruncated() { if(active()) { actionsTruncated=true; } }
	public void action(NexActions.Signal signal)
	{
		if(!active() || signal.tick<start-NexActions.PRE_TICKS || death>=0 && signal.tick>death) { return; }
		if(actions.size()>=NexActions.MAX) { actionsTruncated=true; return; }
		actions.add(signal);
	}
	public NexSummary.Sample actionSample(int tick,CombatState state,String target,int animation,NexTargetState targetState)
	{ return new NexSummary.Sample(tick,active()?phase:"UNKNOWN",active()?window:"UNKNOWN",target,"unclassified",animation,state,targetState); }
	public void launch(int tick,int cycle,long order,int animation,CombatState state,String target,NexTargetState targetState,
		NexObservations.Weapon weapon,NexObservations.Attack attack)
	{
		if(observation(tick)) { launches.add(new NexObservations.Launch(order,cycle,new NexSummary.Sample(tick,phase,window,target,
			weapon==NexObservations.Weapon.TWISTED_BOW || weapon==NexObservations.Weapon.ZARYTE_CROSSBOW?"ranged":"melee",animation,state,targetState),weapon,attack)); }
	}
	/** Keep late resource callbacks during the existing bounded end grace; never extend the fight timeline. */
	public void resource(NexObservations.Signal signal) { if(observation(signal.tick,true)) { resourceSignals.add(signal); } }
	public void flight(NexObservations.Flight flight)
	{ if(observation(flight.tick,false,flight.target,NexCallbackDiagnostics.Stage.PROJECTILE_RETAINED,NexCallbackDiagnostics.Stage.PROJECTILE_OMITTED,0)) { flights.add(flight); } }
	public void impact(NexObservations.Impact impact)
	{ if(observation(impact.tick,false,impact.target,NexCallbackDiagnostics.Stage.HIT_IMPACT_RETAINED,NexCallbackDiagnostics.Stage.HIT_IMPACT_OMITTED,impact.amount)) { impacts.add(impact); } }
	/** HP-only changes belong to the diagnostic bound, never the worn-state buffer. */
	public void health(NexObservations.Health reading)
	{
		var previous=health.isEmpty()?null:health.get(health.size()-1);
		if(previous!=null && Objects.equals(previous.target,reading.target) && previous.targetState.equals(reading.targetState)) { return; }
		if(observation(reading.tick)) { health.add(reading); }
	}
	/** Fixed counters never spend main capture events or observation rows. Saturation is explicit in reports. */
	public void hudCheck(NexObservations.HudCheck check)
	{ if(active()) { hudChecks.compute(check,(k,n)->n==null?1:Math.min(NexObservations.MAX_HUD_COUNT,n+1)); } }
	private void clearObservations() { launches.clear(); resourceSignals.clear(); flights.clear(); impacts.clear(); health.clear(); hudChecks.clear(); callbacks.clear(); observationsTruncated=false; }
	public void flush(int tick) { finishIfReady(tick,true); }
	private void finishIfReady(int tick, boolean leaving)
	{
		if (!active() || death<0 && barrierEnd<0) { return; }
		int signal=death<0?barrierEnd:barrierEnd<0?death:Math.max(death,barrierEnd);
		// Dying precedes arena completion; reverse callback order gets only the shorter tolerance.
		int wait=death>=0 && barrierEnd>=0?END_GRACE:END_WAIT;
		if (!leaving && tick<signal+wait) { return; }
		long endLag=(long)barrierEnd-death;
		boolean correlated=death>=0 && barrierEnd>=0 && endLag>=-END_GRACE && endLag<=END_WAIT;
		List<String> missing=new ArrayList<>();
		if(!fullStart) { missing.add("smoke start not observed before combat"); }
		List<String> missingPhases=new ArrayList<>(List.of("SMOKE","SHADOW","BLOOD","ICE","ZAROS")); missingPhases.removeAll(phases);
		if(!missingPhases.isEmpty()) { missing.add("missing phases "+String.join(", ",missingPhases)); gaps.add("Missing phase boundaries: "+String.join(", ",missingPhases)); }
		if(!continuous) { missing.add("tick or worn-state coverage interrupted"); }
		if(death<0) { missing.add("Nex death/dying not observed"); }
		if(barrierEnd<0) { missing.add("arena end boundary not observed"); }
		if(death>=0 && barrierEnd>=0 && !correlated) { missing.add("end signals outside matching window"); }
		if(death>=0 && (death<=start || lastSample<death-1)) { missing.add("final pre-death state sample missing"); }
		boolean complete=missing.isEmpty();
		finish(complete?Outcome.COMPLETE:Outcome.CENSORED,
			complete?"Correlated Nex death/dying and arena barrier completion":"Incomplete capture: "+String.join("; ",missing),
			death>=0?death:signal);
	}
	public void abort(int tick, Outcome outcome, String reason)
	{ if (active()) { finish(outcome,reason,Math.max(start,tick)); } }
	public void clear() { method=null; timeline.clear(); beforeStart.clear(); damage.clear(); hitKeys.clear(); signals.clear(); gaps.clear(); phases.clear(); gear.clear(); clearObservations(); actions.clear(); actionsTruncated=false; boundaryTick=death=barrierEnd=smokeTick=observedSmoke=-1; sampled=events=unclassified=0; }
	private void finish(Outcome outcome, String reason, int end)
	{
		String cohort=VERSION+":"+method.cohort()+":"+gear+":"+(rosterStable?roster:"unmatched-"+key);
		NexSummary.Signals signals=new NexSummary.Signals(boundaryTick<0?null:boundaryTick,smokeTick<0?null:smokeTick,
			death<0?null:death,barrierEnd<0?null:barrierEnd,continuous,phases,observedSmoke<0?null:observedSmoke,this.signals);
		Set<NexComparability.Setup> setups=new HashSet<>();
		timeline.forEach(s->setups.add(new NexComparability.Setup(s.role,s.state)));
		NexComparability comparability=new NexComparability(VERSION,setups,roster,rosterStable);
		NexObservations observations=new NexObservations(launches,resourceSignals,flights,impacts,health,observationsTruncated,hudChecks,callbacks.snapshot());
		sink.accept(new NexSummary(key,start,end,sampled,Math.max(1,end-start),unclassified,outcome,reason,cohort,method.cohort(),timeline,damage,gaps,signals,beforeStart,context,comparability,observations,new NexActions(actions,actionsTruncated)));
		clear();
	}
}
