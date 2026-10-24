package com.ironpath.nex;

import java.util.*;

/** Immutable bounded callback evidence. Correlation is not a legal-opportunity or consumption model. */
public final class NexObservations
{
	public static final String VERSION="nex-observations-6";
	public static final int MAX=2048;
	public static final int MAX_HUD_COUNT=65535;
	public enum HudCheck { VARIABLE_UPDATE, SCRIPT_CALLBACK, NO_FRESH_VARIABLE, DISABLED, WIDGET_MISSING, WIDGET_HIDDEN,
		NO_INTERACTION, UNTRACKED_TARGET, DEFINITION_MISMATCH, INVALID_VALUES, SERVER_DISAGREEMENT, AMBIGUOUS_TARGET, ACCEPTED_CALLBACK }
	public enum Kind { ENERGY, DEATH_CHARGE_ACTIVE, DEATH_CHARGE_COOLDOWN, DEATH_CHARGE_UPGRADE,
		DC_CAST_EFFECT, DC_END_EFFECT, TARGET_DEATH, TICK_RESOURCES }
	public enum Source { LOCAL, OTHER, UNKNOWN }
	public enum Weapon { FANG, TWISTED_BOW, VOIDWAKER, ZARYTE_CROSSBOW }
	public enum Attack { ORDINARY_CANDIDATE, SPECIAL_CANDIDATE, ORDINARY_OR_SPECIAL_CANDIDATE, UNCLASSIFIED }
	public static final class Health
	{
		public final int tick,cycle;
		public final long order;
		public final String target;
		public final NexTargetState targetState;
		public Health(int tick,int cycle,long order,String target,NexTargetState targetState)
		{ this.tick=tick; this.cycle=cycle; this.order=order; this.target=target; this.targetState=Objects.requireNonNull(targetState); }
	}
	public static final class Launch
	{
		public final long order;
		public final int cycle;
		public final NexSummary.Sample sample;
		public final Weapon weapon;
		public final Attack attack;
		public Launch(long order,int cycle,NexSummary.Sample sample,Weapon weapon,Attack attack)
		{ this.order=order; this.cycle=cycle; this.sample=Objects.requireNonNull(sample); this.weapon=weapon; this.attack=attack; }
	}
	public static final class Signal
	{
		public final Kind kind;
		public final int tick,cycle,value,serverValue;
		public final long order;
		public final String target;
		/** Only settled tick readings carry state; callback payloads retain their own original value/time. */
		public final com.ironpath.encounter.CombatState state;
		public Signal(Kind kind,int tick,int cycle,long order,int value,int serverValue,String target)
		{ this(kind,tick,cycle,order,value,serverValue,target,null); }
		public Signal(Kind kind,int tick,int cycle,long order,int value,int serverValue,String target,com.ironpath.encounter.CombatState state)
		{ this.kind=Objects.requireNonNull(kind); this.tick=tick; this.cycle=cycle; this.order=order; this.value=value; this.serverValue=serverValue; this.target=target; this.state=state; }
	}
	public static final class Flight
	{
		public final int tick,cycle,id,startCycle,endCycle;
		public final long order;
		public final String target;
		public final Source source;
		public final NexTargetState targetState;
		public Flight(int tick,int cycle,long order,int id,int startCycle,int endCycle,String target,Source source)
		{ this(tick,cycle,order,id,startCycle,endCycle,target,source,NexTargetState.UNKNOWN); }
		public Flight(int tick,int cycle,long order,int id,int startCycle,int endCycle,String target,Source source,NexTargetState targetState)
		{ this.tick=tick; this.cycle=cycle; this.order=order; this.id=id; this.startCycle=startCycle; this.endCycle=endCycle; this.target=target; this.source=Objects.requireNonNull(source); this.targetState=Objects.requireNonNull(targetState); }
	}
	public static final class Impact
	{
		public final int tick,cycle,amount;
		public final long order;
		public final String target;
		public final NexTargetState targetState;
		public Impact(int tick,int cycle,long order,String target,int amount,NexTargetState targetState)
		{ this.tick=tick; this.cycle=cycle; this.order=order; this.target=target; this.amount=amount; this.targetState=Objects.requireNonNull(targetState); }
	}
	public static final NexObservations EMPTY=new NexObservations(List.of(),List.of(),List.of(),List.of(),false);
	public final List<Launch> launches;
	public final List<Signal> signals;
	public final List<Flight> flights;
	public final List<Impact> impacts;
	public final List<Health> health;
	public final Map<HudCheck,Integer> hudChecks;
	public final NexCallbackDiagnostics callbacks;
	public final boolean truncated;
	public NexObservations(List<Launch> launches,List<Signal> signals,List<Flight> flights,List<Impact> impacts,boolean truncated)
	{ this(launches,signals,flights,impacts,List.of(),truncated); }
	public NexObservations(List<Launch> launches,List<Signal> signals,List<Flight> flights,List<Impact> impacts,List<Health> health,boolean truncated)
	{ this(launches,signals,flights,impacts,health,truncated,Map.of()); }
	public NexObservations(List<Launch> launches,List<Signal> signals,List<Flight> flights,List<Impact> impacts,List<Health> health,boolean truncated,Map<HudCheck,Integer> hudChecks)
	{ this(launches,signals,flights,impacts,health,truncated,hudChecks,NexCallbackDiagnostics.EMPTY); }
	public NexObservations(List<Launch> launches,List<Signal> signals,List<Flight> flights,List<Impact> impacts,List<Health> health,boolean truncated,Map<HudCheck,Integer> hudChecks,NexCallbackDiagnostics callbacks)
	{
		this.callbacks=Objects.requireNonNull(callbacks);
		if(launches.size()+signals.size()+flights.size()+impacts.size()+health.size()>MAX) { throw new IllegalArgumentException("Bounded observations required"); }
		this.launches=List.copyOf(launches); this.signals=List.copyOf(signals); this.flights=List.copyOf(flights); this.impacts=List.copyOf(impacts); this.truncated=truncated;
		this.health=List.copyOf(health);
		for(var count:hudChecks.values()) { if(count==null || count<0 || count>MAX_HUD_COUNT) { throw new IllegalArgumentException("Bounded HUD counts required"); } }
		this.hudChecks=Map.copyOf(hudChecks);
	}
}
