package com.ironpath.nex;

import com.ironpath.encounter.CombatState;
import java.util.*;

/** Frozen observation evidence for descriptive matched averages, never a causal team-damage model. */
public final class NexComparability
{
	public static final class Setup
	{
		public final String role;
		public final Map<String,Integer> slots, realLevels, visibleLevels;
		public final Integer style;
		public final Map<String,Integer> projectileState;
		public final Set<String> prayers;
		public Setup(String role,CombatState state)
		{
			this.role=role; slots=state.slots; realLevels=state.realLevels; visibleLevels=state.visibleLevels; prayers=state.prayers; style=state.style;
			Map<String,Integer> projectile=new HashMap<>();
			for(String key:List.of("quiver-ammo-id-raw","quiver-charged-raw")) { if(state.resources.containsKey(key)) { projectile.put(key,state.resources.get(key)); } }
			projectileState=Map.copyOf(projectile);
		}
		@Override public boolean equals(Object o)
		{
			if(!(o instanceof Setup)) { return false; } Setup s=(Setup)o;
			return Objects.equals(role,s.role) && slots.equals(s.slots) && realLevels.equals(s.realLevels)
				&& visibleLevels.equals(s.visibleLevels) && prayers.equals(s.prayers) && Objects.equals(style,s.style) && projectileState.equals(s.projectileState);
		}
		@Override public int hashCode() { return Objects.hash(role,slots,realLevels,visibleLevels,prayers,style,projectileState); }
	}
	public final String adapter;
	public final Set<Setup> setups;
	private final String roster;
	public final boolean rosterStable;
	public NexComparability(String adapter,Set<Setup> setups,String roster,boolean rosterStable)
	{ this.adapter=Objects.requireNonNull(adapter); this.setups=Set.copyOf(setups); this.roster=roster; this.rosterStable=rosterStable && roster!=null; }
	public String exclusion(NexContext context)
	{
		if(!context.confirmed()) { return "Kill context unconfirmed, unknown or variable"; }
		if(context.group!=NexContext.Group.ORGANIZED || context.strategy==NexContext.Strategy.OTHER)
		{ return "Uncontrolled group or unspecified strategy"; }
		if(!rosterStable) { return "Observed arena roster unknown or changed"; }
		if(setups.isEmpty() || setups.stream().anyMatch(s->!s.slots.keySet().containsAll(com.ironpath.advisor.PoweredMagic.SLOTS)
			|| s.slots.values().stream().anyMatch(id->id<0) || s.style==null
			|| s.projectileState.values().stream().anyMatch(value->value<0)
			|| !s.realLevels.keySet().containsAll(List.of("ATTACK","STRENGTH","DEFENCE","HITPOINTS","PRAYER","RANGED","MAGIC"))
			|| !s.visibleLevels.keySet().containsAll(s.realLevels.keySet()))) { return "Observed setup or levels incomplete"; }
		return null;
	}
	public boolean matches(NexComparability other,NexContext a,NexContext b)
	{
		return other!=null && exclusion(a)==null && other.exclusion(b)==null
			&& a.strategy==b.strategy && a.group==b.group && Objects.equals(a.participants,b.participants)
			&& adapter.equals(other.adapter) && setups.equals(other.setups) && Objects.equals(roster,other.roster);
	}
}
