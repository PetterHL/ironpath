package com.ironpath.nex;

import java.util.*;

/** Independent finite resource scenario. Supplied opportunities are assumptions, never inferred attacks. */
public final class SpecTimeline
{
	public static final String VERSION="nex-spec-resources-1";
	public static final int CAP=1000, RESTORE=100, DEATH_CHARGE_RESTORE=150,
		DEATH_CHARGE_TICKS=100, MAX_TICKS=3000, MAX_EVENTS=2048;
	public enum Ring
	{
		STANDARD(50), LIGHTBEARER(25);
		public final int interval;
		Ring(int interval) { this.interval=interval; }
	}
	public enum Attack
	{
		ORDINARY(0), VOIDWAKER(500), ZARYTE_CROSSBOW(750);
		public final int cost;
		Attack(int cost) { this.cost=cost; }
	}
	/** Sensitivity policies; neither establishes actual server ordering on an unresolved tick. */
	public enum RestoreOrder { BEFORE_EVENTS, AFTER_EVENTS }
	public enum Status { APPLIED, INSUFFICIENT_ENERGY, ATTACK_DELAY, NO_AMMUNITION, CAST_COOLDOWN, NO_ACTIVE_CHARGE, DUPLICATE_DEATH }
	private enum Kind { CAST, DEATH, ATTACK }
	public static final class Event
	{
		public final int tick, delay;
		public final Attack attack;
		public final String deathKey;
		public final boolean upgraded;
		private final Kind kind;
		private Event(int tick,Kind kind,Attack attack,int delay,String deathKey,boolean upgraded)
		{
			if(tick<0 || tick>=MAX_TICKS) { throw new IllegalArgumentException("Event tick outside scenario bounds"); }
			this.tick=tick; this.kind=kind; this.attack=attack; this.delay=delay; this.deathKey=deathKey; this.upgraded=upgraded;
		}
		public static Event cast(int tick,boolean upgraded) { return new Event(tick,Kind.CAST,null,0,null,upgraded); }
		/** The caller must establish death eligibility; an NPC dying is insufficient. Keys identify distinct deaths. */
		public static Event eligibleDeath(int tick,String key)
		{
			if(key==null || key.isBlank()) { throw new IllegalArgumentException("Distinct eligible death key required"); }
			return new Event(tick,Kind.DEATH,null,0,key,false);
		}
		public static Event ordinary(int tick,int delay)
		{
			if(delay<1 || delay>10) { throw new IllegalArgumentException("Explicit ordinary attack delay required (1–10 ticks)"); }
			return new Event(tick,Kind.ATTACK,Attack.ORDINARY,delay,null,false);
		}
		public static Event voidwaker(int tick) { return new Event(tick,Kind.ATTACK,Attack.VOIDWAKER,4,null,false); }
		public static Event zaryteCrossbow(int tick,boolean rapid)
		{ return new Event(tick,Kind.ATTACK,Attack.ZARYTE_CROSSBOW,rapid?5:6,null,false); }
		public String label()
		{ return kind==Kind.ATTACK?attack.toString():kind==Kind.CAST?(upgraded?"Improved Death Charge cast":"Death Charge cast"):"Eligible death"; }
		public boolean isAttack() { return kind==Kind.ATTACK; }
	}
	/** Starts attack-ready, with no active Death Charge and an available cast. These are explicit assumptions. */
	public static final class Plan
	{
		public final int initialEnergy, endTick, firstRestoreTick;
		public final Ring ring;
		public final RestoreOrder order;
		public final List<Event> events;
		public Plan(int initialEnergy,int endTick,Ring ring,int firstRestoreTick,RestoreOrder order,List<Event> events)
		{
			if(initialEnergy<0 || initialEnergy>CAP) { throw new IllegalArgumentException("Starting energy must be known (0–1000)"); }
			if(endTick<1 || endTick>MAX_TICKS) { throw new IllegalArgumentException("Scenario duration outside bounds"); }
			this.ring=Objects.requireNonNull(ring); this.order=Objects.requireNonNull(order);
			if(firstRestoreTick<0 || firstRestoreTick>ring.interval) { throw new IllegalArgumentException("Restoration phase outside fixed-ring cycle"); }
			if(events.size()>MAX_EVENTS) { throw new IllegalArgumentException("Scenario event limit"); }
			int last=-1;
			for(Event event:events)
			{
				if(event.tick<last || event.tick>=endTick) { throw new IllegalArgumentException("Events must be ordered within the exclusive end tick"); }
				last=event.tick;
			}
			this.initialEnergy=initialEnergy; this.endTick=endTick; this.firstRestoreTick=firstRestoreTick;
			this.events=List.copyOf(events);
		}
	}
	public static final class Entry
	{
		/** Index in Plan.events; -1 for a natural restore. Equal-tick events retain distinct identities. */
		public final int eventIndex, tick, before, after, wasted;
		public final String event;
		public final Status status;
		private Entry(int eventIndex,int tick,String event,Status status,int before,int after,int wasted)
		{ this.eventIndex=eventIndex; this.tick=tick; this.event=event; this.status=status; this.before=before; this.after=after; this.wasted=wasted; }
	}
	public static final class Result
	{
		public final int finalEnergy, restored, spent, wasted, ordinaryAttacks, voidwakerSpecs, zaryteSpecs, deathChargeProcs;
		public final List<Entry> entries;
		private Result(State s)
		{
			finalEnergy=s.energy; restored=s.restored; spent=s.spent; wasted=s.wasted;
			ordinaryAttacks=s.ordinary; voidwakerSpecs=s.voidwaker; zaryteSpecs=s.zaryte; deathChargeProcs=s.procs;
			entries=List.copyOf(s.entries);
		}
	}
	private static final class State
	{
		int energy, restored, spent, wasted, ready, charges, expires, castReady, ordinary, voidwaker, zaryte, procs;
		final Set<String> deaths=new HashSet<>();
		final List<Entry> entries=new ArrayList<>();
		void gain(int eventIndex,int tick,String label,int amount)
		{
			int before=energy, accepted=Math.min(amount,CAP-energy); energy+=accepted;
			restored+=accepted; wasted+=amount-accepted;
			entries.add(new Entry(eventIndex,tick,label,Status.APPLIED,before,energy,amount-accepted));
		}
		void apply(Event e,int index,AttackResources resources)
		{
			int before=energy;
			Status status=Status.APPLIED;
			switch(e.kind)
			{
				case CAST:
					if(e.tick<castReady) { status=Status.CAST_COOLDOWN; }
					else { charges=e.upgraded?2:1; expires=castReady=e.tick+DEATH_CHARGE_TICKS; }
					break;
				case DEATH:
					if(!deaths.add(e.deathKey)) { status=Status.DUPLICATE_DEATH; }
					else if(charges==0 || e.tick>=expires) { status=Status.NO_ACTIVE_CHARGE; }
					else
					{
						--charges; ++procs; gain(index,e.tick,"Death Charge proc",DEATH_CHARGE_RESTORE); return;
					}
					break;
				case ATTACK:
					if(e.tick<ready) { status=Status.ATTACK_DELAY; }
					else if(energy<e.attack.cost) { status=Status.INSUFFICIENT_ENERGY; }
					else if(resources!=null && !resources.available(index)) { status=Status.NO_AMMUNITION; }
					else
					{
						energy-=e.attack.cost; spent+=e.attack.cost; ready=e.tick+e.delay;
						if(e.attack==Attack.ORDINARY) { ++ordinary; }
						else if(e.attack==Attack.VOIDWAKER) { ++voidwaker; }
						else { ++zaryte; }
						if(resources!=null) { resources.accepted(index); }
					}
					break;
				default: throw new IllegalStateException();
			}
			entries.add(new Entry(index,e.tick,e.label(),status,before,energy,0));
		}
	}
	/** End tick is exclusive. Equal-tick explicit events retain caller order; rejected events spend nothing. */
	public Result run(Plan plan)
	{ return run(plan,null); }
	/** Package-local resource join: admission precedes spending; only accepted launches consume stock. */
	interface AttackResources
	{
		boolean available(int eventIndex);
		void accepted(int eventIndex);
	}
	Result run(Plan plan,AttackResources resources)
	{
		State s=new State(); s.energy=plan.initialEnergy;
		int index=0;
		for(int tick=0;tick<plan.endTick;tick++)
		{
			boolean restore=tick>=plan.firstRestoreTick && (tick-plan.firstRestoreTick)%plan.ring.interval==0;
			if(restore && plan.order==RestoreOrder.BEFORE_EVENTS) { s.gain(-1,tick,"Natural restoration",RESTORE); }
			while(index<plan.events.size() && plan.events.get(index).tick==tick)
			{ s.apply(plan.events.get(index),index,resources); ++index; }
			if(restore && plan.order==RestoreOrder.AFTER_EVENTS) { s.gain(-1,tick,"Natural restoration",RESTORE); }
		}
		return new Result(s);
	}
	/** All first-restore offsets, including due-at-start, and both orders. Sensitivity cases, not probabilities. */
	public List<Result> unknownRestorationPhase(int initialEnergy,int endTick,Ring ring,List<Event> events)
	{
		List<Result> results=new ArrayList<>();
		for(int phase=0;phase<=ring.interval;phase++)
		{ for(RestoreOrder order:RestoreOrder.values()) { results.add(run(new Plan(initialEnergy,endTick,ring,phase,order,events))); } }
		return List.copyOf(results);
	}
}
