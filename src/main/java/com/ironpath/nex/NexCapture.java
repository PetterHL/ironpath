package com.ironpath.nex;

import com.ironpath.encounter.CombatState;
import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.preferences.NexMethod;
import java.util.*;
import java.util.function.Consumer;

/** Bounded deterministic Nex evidence machine. The adapter supplies reviewed signals and spawn keys. */
public final class NexCapture
{
	public static final String VERSION="nex-public-4";
	public static final int MAX_TICKS=3000, MAX_SAMPLES=1024, MAX_EVENTS=20000, MAX_SIGNALS=32,
		END_GRACE=5, END_WAIT=20;
	private final Consumer<NexSummary> sink;
	private final List<NexSummary.Sample> timeline=new ArrayList<>();
	private final List<NexSummary.Sample> beforeStart=new ArrayList<>();
	private final Map<String,NexSummary.Damage> damage=new LinkedHashMap<>();
	private final Set<Long> hitKeys=new HashSet<>();
	private final List<String> signals=new ArrayList<>();
	private final Set<String> gaps=new LinkedHashSet<>(), phases=new LinkedHashSet<>(), gear=new TreeSet<>();
	private long serial, key;
	private volatile NexMethod method;
	private int origin, start, last, sampled, lastSample, boundaryTick=-1, death=-1, barrierEnd=-1,
		smokeTick=-1, observedSmoke=-1, events, unclassified;
	private String phase="UNKNOWN", window="UNKNOWN", roster;
	private boolean fullStart, continuous, rosterStable;

	public NexCapture(Consumer<NexSummary> sink) { this.sink=sink; }
	public boolean active() { return method!=null; }
	public long key() { return key; }
	/** A true start flag denotes an actually observed smoke announcement, never arena entry. */
	public void start(int tick, boolean smokeObserved, NexMethod method, String roster)
	{
		if (active()) { abort(tick,Outcome.CENSORED,"New encounter before completion"); }
		this.method=Objects.requireNonNull(method); key=++serial; origin=start=last=tick; lastSample=tick-1;
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
					|| !previous.window.equals(window) || !Objects.equals(previous.target,target) || !previous.role.equals(role))
				{
					if (timeline.size()+beforeStart.size()>=MAX_SAMPLES) { abort(tick,Outcome.CENSORED,"State buffer limit reached"); return; }
					timeline.add(new NexSummary.Sample(tick,phase,window,target,role,animation,state));
				}
			}
		}
		finishIfReady(tick,false);
	}
	public void hit(int tick, long eventKey, String target, int amount, boolean mine, boolean other, boolean heal)
	{
		if (!event(tick) || !hitKeys.add(eventKey) || amount<0 || (death>=0 && tick>death)) { return; }
		if (heal) { gaps.add("Healing observed and excluded from damage ratio"); return; }
		if (!mine && !other) { ++unclassified; gaps.add("Unclassified hitsplats excluded from damage ratio"); return; }
		String scope=phase+" / "+window+" / "+target;
		NexSummary.Damage before=damage.getOrDefault(scope,new NexSummary.Damage(0,0));
		damage.put(scope,new NexSummary.Damage(before.local+(mine?amount:0),before.total+amount));
	}
	public void death(int tick) { if (event(tick) && death<0) { death=tick; } }
	public void barrierEnd(int tick) { if (event(tick) && barrierEnd<0) { barrierEnd=tick; } }
	public void gap(String gap) { if (active() && gaps.size()<64) { gaps.add(gap); } }
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
	public void clear() { method=null; timeline.clear(); beforeStart.clear(); damage.clear(); hitKeys.clear(); signals.clear(); gaps.clear(); phases.clear(); gear.clear(); boundaryTick=death=barrierEnd=smokeTick=observedSmoke=-1; sampled=events=unclassified=0; }
	private void finish(Outcome outcome, String reason, int end)
	{
		String cohort=VERSION+":"+method.cohort()+":"+gear+":"+(rosterStable?roster:"unmatched-"+key);
		NexSummary.Signals signals=new NexSummary.Signals(boundaryTick<0?null:boundaryTick,smokeTick<0?null:smokeTick,
			death<0?null:death,barrierEnd<0?null:barrierEnd,continuous,phases,observedSmoke<0?null:observedSmoke,this.signals);
		sink.accept(new NexSummary(key,start,end,sampled,Math.max(1,end-start),unclassified,outcome,reason,cohort,method.cohort(),timeline,damage,gaps,signals,beforeStart));
		clear();
	}
}
