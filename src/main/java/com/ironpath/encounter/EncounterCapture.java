package com.ironpath.encounter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Pure bounded state machine. The adapter supplies public evidence, never mutable actors. */
public final class EncounterCapture
{
	public static final int FINALIZE_TICKS=5, MAX_TICKS=3000, MAX_SEGMENTS=512, MAX_EVENTS=20000, MAX_RECENT=50;
	private final Deque<EncounterSummary> recent=new ArrayDeque<>();
	private final List<EncounterSummary.Segment> segments=new ArrayList<>();
	private final Set<String> gaps=new LinkedHashSet<>();
	private long generation, key;
	private EncounterContext context;
	private String adapter;
	private int start, lastTick, lastSample, stateStart, death=-1, loot=-1, damage, events;
	private CombatState state;
	public synchronized void reset(long nextGeneration) { generation=nextGeneration; recent.clear(); discard(); }
	private void discard() { context=null; state=null; segments.clear(); gaps.clear(); death=loot=-1; damage=events=0; }
	public synchronized boolean active() { return context!=null; }
	public synchronized long key() { return key; }
	public synchronized List<EncounterSummary> recent() { return List.copyOf(recent); }
	public synchronized void start(long key, long token, EncounterContext context, String adapter, int tick, CombatState state)
	{
		if (token!=generation) { return; }
		if (active()) { finish(EncounterSummary.Outcome.CENSORED,"New spawn before completion",tick); }
		this.key=key; this.context=context; this.adapter=adapter; start=lastTick=lastSample=stateStart=tick; this.state=state;
		if (state==null) { gaps.add("Initial combat state unread"); }
	}
	private boolean event(int tick)
	{
		if (!active() || tick<lastTick) { return false; }
		lastTick=tick;
		if (++events>MAX_EVENTS || tick-start>MAX_TICKS)
		{ finish(EncounterSummary.Outcome.CENSORED,"Capture limit reached; incomplete evidence",tick); return false; }
		return true;
	}
	public synchronized void sample(int tick, CombatState next)
	{
		if (!event(tick)) { return; }
		if(death<0 && tick-lastSample>1) { gaps.add("Missing tick/state samples"); }
		lastSample=tick;
		if (death<0 && !java.util.Objects.equals(state,next))
		{
			if (tick==stateStart) { gaps.add("Same-tick state changes; damage is not attributed to the currently worn item"); }
			if (state!=null) { segments.add(new EncounterSummary.Segment(stateStart,tick,state)); }
			if (segments.size()>=MAX_SEGMENTS) { finish(EncounterSummary.Outcome.CENSORED,"State buffer overflow",tick); return; }
			if (next==null) { gaps.add("Combat state unread"); }
			if (state!=null && next!=null && state.specialEnergy!=null && next.specialEnergy!=null
				&& next.specialEnergy<state.specialEnergy) { gaps.add("Special energy used; reduction/proc effects unverified"); }
			state=next; stateStart=tick;
		}
		if (death>=0 && tick-death>=FINALIZE_TICKS)
		{
			boolean complete=damage>0 && loot>=death-1 && loot<=death+FINALIZE_TICKS
				&& gaps.stream().noneMatch(g->g.startsWith("Engagement began") || g.contains("state unread") || g.startsWith("Missing tick"));
			finish(complete ? EncounterSummary.Outcome.COMPLETE : EncounterSummary.Outcome.CENSORED,
				complete ? "Observed death and local server loot credit" : "Incomplete capture or missing local loot credit",death);
		}
	}
	public synchronized void localDamage(int tick, int amount) { if(event(tick) && amount>=0) { damage+=amount; } }
	public synchronized void gap(String reason) { if(active()) { gaps.add(reason); } }
	public synchronized void death(int tick) { if(event(tick) && death<0) {
		if(tick-lastSample>1) { gaps.add("Missing tick/state samples"); }
		death=tick;
	} }
	public synchronized void loot(int tick) { if(event(tick) && (death<0 || tick<=death+FINALIZE_TICKS)) { loot=tick; } }
	public synchronized void abort(int tick, EncounterSummary.Outcome outcome, String reason)
	{ if(active()) { finish(outcome,reason,tick); } }
	private void finish(EncounterSummary.Outcome outcome, String reason, int end)
	{
		if (state!=null) { segments.add(new EncounterSummary.Segment(stateStart,Math.max(stateStart,end),state)); }
		recent.addLast(new EncounterSummary(key,generation,context,outcome,adapter,reason,start,end,damage,segments,gaps));
		while (recent.size()>MAX_RECENT) { recent.removeFirst(); }
		discard();
	}
}
