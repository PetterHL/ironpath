package com.ironpath.nex;

import com.ironpath.encounter.CombatState;
import java.util.*;

/** Finite normal-rune branch for explicitly declared Death Charge casts. No cast attribution or substitutions. */
public final class NexSpellResources
{
	public static final String VERSION="nex-spell-stock-1";
	public static final Set<Integer> POUCHES=Set.of(12791,24416,27281,27509);
	public static final Map<Integer,String> RUNES=Map.of(560,"Death rune",565,"Blood rune",566,"Soul rune");
	private NexSpellResources() { }
	public static Map<Integer,Integer> stock(CombatState state)
	{
		if(state.inventory==null) { throw new IllegalArgumentException("Spell rune inventory unread"); }
		Map<Integer,Integer> stock=new TreeMap<>();
		for(int id:RUNES.keySet())
		{
			long n=state.inventory.getOrDefault(id,0L);
			if(n<0 || n>Integer.MAX_VALUE) { throw new IllegalArgumentException("Spell rune quantity outside bounds"); }
			stock.put(id,(int)n);
		}
		if(POUCHES.stream().noneMatch(id->state.inventory.getOrDefault(id,0L)>0)) { return Map.copyOf(stock); }
		for(int i=1;i<=6;i++)
		{
			Integer id=state.resources.get("rune-pouch-item-"+i),n=state.resources.get("rune-pouch-quantity-"+i);
			if(id==null || n==null || id<0 || n<0 || n>16000 || id==0 && n>0)
			{ throw new IllegalArgumentException("Carried rune pouch contents unread"); }
			if(RUNES.containsKey(id)) { stock.merge(id,n,Math::addExact); }
		}
		return Map.copyOf(stock);
	}
	/** Rejected cooldown casts consume no runes. Other casting access and spell/switch delays remain declarations. */
	public static Map<Integer,Integer> spent(NexPackageComparison.Conditions conditions,NexPackageComparison.Scenario scenario)
	{
		if(conditions.steps.stream().noneMatch(s->s.kind==NexPackageComparison.StepKind.CAST)) { return Map.of(); }
		List<CombatState> states=new ArrayList<>();
		for(var loadout:scenario.loadouts.values())
		{ if(!loadout.state.known()) { throw new IllegalArgumentException("Spell package state unknown"); } states.add(loadout.state.value()); }
		List<NexSupplies.Use> supplies=new ArrayList<>();
		for(var step:conditions.steps) { if(step.kind==NexPackageComparison.StepKind.SUPPLY) { supplies.add(step.supply); } }
		int ready=0,casts=0;
		for(var step:conditions.steps)
		{
			if(step.kind!=NexPackageComparison.StepKind.CAST || step.tick<ready) { continue; }
			for(CombatState state:states)
			{
				Integer magic=NexSupplies.levels(state.realLevels,state.visibleLevels,supplies,step.tick).get("MAGIC");
				if(magic==null || magic<80) { throw new IllegalArgumentException("Death Charge requires known Magic 80 at the declared cast"); }
			}
			ready=step.tick+SpecTimeline.DEATH_CHARGE_TICKS; casts++;
		}
		Map<Integer,Integer> spent=new TreeMap<>();
		for(CombatState state:states)
		{
			// Known loose runes suffice independently of unread pouch contents.
			boolean loose=state.inventory!=null;
			for(int id:RUNES.keySet()) { if(state.inventory==null || state.inventory.getOrDefault(id,0L)<casts) { loose=false; } }
			Map<Integer,Integer> available=loose?Map.of():stock(state);
			if(!loose) for(int id:RUNES.keySet())
			{ if(available.get(id)<casts) { throw new IllegalArgumentException("Insufficient usable "+RUNES.get(id)+" stock for declared casts; rune substitution is not modelled"); } }
		}
		for(int id:RUNES.keySet())
		{
			spent.put(id,casts);
		}
		return Map.copyOf(spent);
	}
}
