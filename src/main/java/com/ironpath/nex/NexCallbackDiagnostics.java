package com.ironpath.nex;

import java.util.*;

/** Fixed vocabularies and saturating scalar counters; no actors, identifiers or event capacity. */
public final class NexCallbackDiagnostics
{
	public static final int MAX_COUNT=65535;
	public static final long MAX_AMOUNT=Integer.MAX_VALUE;
	public enum Target { NEX, FUMUS, UMBRA, CRUOR, GLACIES, OUTSIDE, UNKNOWN }
	public enum Stage
	{
		HIT_ENTRY, HIT_NOT_NPC, HIT_MISSING_PAYLOAD, HIT_UNKNOWN_TARGET, HIT_WRONG_VIEW, HIT_UNTRACKED,
		HIT_REPEATED_OBJECT, HIT_OBJECT_TRACKING_LIMIT, HIT_IDENTITY_REJECTED,
		HIT_DUPLICATE_KEY, HIT_INVALID_AMOUNT, HIT_AFTER_DEATH, HIT_OUT_OF_ORDER, HIT_CAPTURE_LIMIT,
		HIT_OWN, HIT_OTHER, HIT_HEAL, HIT_UNCLASSIFIED, HIT_IMPACT_RETAINED, HIT_IMPACT_OMITTED,
		PROJECTILE_ENTRY, PROJECTILE_NULL, PROJECTILE_REPEAT, PROJECTILE_OTHER_SOURCE,
		PROJECTILE_OUTSIDE_TARGET, PROJECTILE_OBJECT_LIMIT, PROJECTILE_LOCAL, PROJECTILE_UNKNOWN,
		PROJECTILE_RETAINED, PROJECTILE_OMITTED
	}
	public static final class Tally
	{
		public final int count;
		public final long amount;
		public Tally(int count,long amount) { this.count=count; this.amount=amount; }
		public boolean atLimit() { return count==MAX_COUNT || amount==MAX_AMOUNT; }
	}
	public static final NexCallbackDiagnostics EMPTY=new Builder().snapshot();
	public final Map<Target,Map<Stage,Tally>> counts;
	private NexCallbackDiagnostics(Map<Target,Map<Stage,Tally>> counts) { this.counts=Map.copyOf(counts); }
	public Tally get(Target target,Stage stage) { return counts.getOrDefault(target,Map.of()).getOrDefault(stage,new Tally(0,0)); }
	public static Target target(String value)
	{
		if(value!=null) for(var target:Target.values()) { if(target.name().equals(value.split("@",2)[0])) { return target; } }
		return Target.UNKNOWN;
	}
	public static final class Builder
	{
		private final Map<Target,Map<Stage,Tally>> counts=new EnumMap<>(Target.class);
		public void add(Target target,Stage stage,int amount)
		{
			var stages=counts.computeIfAbsent(target,k->new EnumMap<>(Stage.class));
			var old=stages.getOrDefault(stage,new Tally(0,0));
			stages.put(stage,new Tally(Math.min(MAX_COUNT,old.count+1),Math.min(MAX_AMOUNT,old.amount+Math.max(0,amount))));
		}
		public NexCallbackDiagnostics snapshot()
		{ Map<Target,Map<Stage,Tally>> copy=new EnumMap<>(Target.class); counts.forEach((k,v)->copy.put(k,Map.copyOf(v))); return new NexCallbackDiagnostics(copy); }
		public void clear() { counts.clear(); }
	}
}
