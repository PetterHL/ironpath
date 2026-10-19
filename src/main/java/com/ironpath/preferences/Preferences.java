package com.ironpath.preferences;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Only explicit user choices belong here. No observed containers, levels or identity. */
public final class Preferences
{
	public static final Preferences EMPTY = new Preferences(null, Set.of(), Map.of());
	public final String goal;
	public final Set<String> skipped;
	public final Map<String, Confirmation> confirmations;
	public final NexMethod nex;
	public final com.ironpath.nex.NexContext nexContext;

	public Preferences(String goal, Set<String> skipped, Map<String, Confirmation> confirmations)
	{ this(goal, skipped, confirmations, NexMethod.PILOT); }

	public Preferences(String goal, Set<String> skipped, Map<String, Confirmation> confirmations, NexMethod nex)
	{ this(goal,skipped,confirmations,nex,com.ironpath.nex.NexContext.PILOT); }
	public Preferences(String goal,Set<String> skipped,Map<String,Confirmation> confirmations,NexMethod nex,com.ironpath.nex.NexContext context)
	{
		this.goal = goal;
		this.skipped = Set.copyOf(skipped);
		this.confirmations = Map.copyOf(confirmations);
		this.nex = java.util.Objects.requireNonNull(nex);
		this.nexContext=java.util.Objects.requireNonNull(context).inherited();
	}

	public Preferences context(com.ironpath.nex.NexContext context) { return new Preferences(goal,skipped,confirmations,nex,context); }
	public Preferences nex(NexMethod method) { return new Preferences(goal, skipped, confirmations, method,nexContext); }
	public Preferences goal(String id) { return new Preferences(id, skipped, confirmations, nex,nexContext); }
	public Preferences skip(String id, boolean skip)
	{
		Set<String> next = new HashSet<>(skipped);
		if (skip) { next.add(id); } else { next.remove(id); }
		return new Preferences(goal, next, confirmations, nex,nexContext);
	}
	public Preferences confirm(String key, Boolean value, Instant time)
	{
		Map<String, Confirmation> next = new HashMap<>(confirmations);
		if (value == null) { next.remove(key); } else { next.put(key, new Confirmation(value, time)); }
		return new Preferences(goal, skipped, next, nex,nexContext);
	}

	public static final class Confirmation
	{
		public final boolean value;
		public final Instant time;
		public Confirmation(boolean value, Instant time) { this.value = value; this.time = java.util.Objects.requireNonNull(time); }
		public String evidence() { return "manual planning confirmation (" + value + ") at " + time; }
	}
}
