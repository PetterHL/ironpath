package com.ironpath.nex;

import java.util.*;

/** Explicit finite supply uses. Inventory changes never create uses, healing policies or attack evidence. */
public final class NexSupplies
{
	public static final String VERSION="nex-supplies-1";
	public enum Kind
	{
		SHARK("Shark",3,385), KARAMBWAN("Cooked karambwan",2,3144),
		BREW("Saradomin brew dose",0,6691,6689,6687,6685),
		RESTORE("Super restore dose",0,3030,3028,3026,3024),
		SUPER_COMBAT("Super combat dose",0,12701,12699,12697,12695),
		RANGING("Ranging potion dose",0,173,171,169,2444);
		public final String label;
		public final int attackDelay;
		/** One through four doses; food has one unit per item. */
		public final List<Integer> items;
		Kind(String label,int delay,Integer... items)
		{ this.label=label; attackDelay=delay; this.items=List.of(items); }
		public boolean potion() { return attackDelay==0; }
		/** Cost key denotes one food or one dose, never the price of a full bottle. */
		public int costKey() { return items.get(0); }
	}
	public static final class Use
	{
		public final int tick;
		public final Kind kind;
		public Use(int tick,Kind kind)
		{
			if(tick<0 || tick>=SpecTimeline.MAX_TICKS) { throw new IllegalArgumentException("Supply tick outside window"); }
			this.tick=tick; this.kind=Objects.requireNonNull(kind);
		}
	}
	private NexSupplies() { }
	/** Normal food, potion, karambwan; this is a declared same-tick order, not observed clicks. */
	public static List<Use> ordered(Map<Kind,List<Integer>> offsets,int end)
	{
		List<Use> uses=new ArrayList<>();
		offsets.forEach((kind,ticks)->ticks.forEach(tick->uses.add(new Use(tick,kind))));
		uses.sort(Comparator.comparingInt((Use u)->u.tick).thenComparingInt(u->u.kind==Kind.SHARK?0:u.kind==Kind.KARAMBWAN?2:1));
		validate(uses,end); return List.copyOf(uses);
	}
	public static void validate(List<Use> uses,int end)
	{
		if(uses.size()>128) { throw new IllegalArgumentException("Use at most 128 supply actions"); }
		int last=-1,foodReady=0,comboReady=0,potionReady=0;
		for(Use u:uses)
		{
			if(u.tick<last || u.tick>=end) { throw new IllegalArgumentException("Supply actions must be ordered within the window"); }
			if(u.kind.potion())
			{ if(u.tick<potionReady) { throw new IllegalArgumentException("Leave at least 3 ticks between potions"); } potionReady=u.tick+3; }
			else if(u.kind==Kind.SHARK)
			{ if(u.tick<foodReady) { throw new IllegalArgumentException("Leave at least 3 ticks between normal foods"); } foodReady=u.tick+3; }
			else
			{
				if(u.tick<comboReady)
				{ throw new IllegalArgumentException("Karambwan requires its eating cooldown or a same-tick normal-food combo"); }
				comboReady=foodReady=u.tick+3;
			}
			last=u.tick;
		}
	}
	/** Frozen usable inventory only. Doses are pooled within an exact reviewed potion family; no pickups/decanting. */
	public static Map<Kind,Integer> quantities(Map<Integer,Long> inventory)
	{
		if(inventory==null) { throw new IllegalArgumentException("Supply inventory unread"); }
		Map<Kind,Integer> quantities=new EnumMap<>(Kind.class);
		for(Kind kind:Kind.values())
		{
			long total=0;
			for(int i=0;i<kind.items.size();i++)
			{
				long count=inventory.getOrDefault(kind.items.get(i),0L);
				if(count<0 || count>28) { throw new IllegalArgumentException("Supply stock outside inventory bounds"); }
				total+=count*(kind.potion()?i+1:1);
			}
			if(total>112) { throw new IllegalArgumentException("Supply doses exceed inventory bounds"); }
			quantities.put(kind,(int)total);
		}
		return Map.copyOf(quantities);
	}
	public static Map<Kind,Integer> consumed(List<Use> uses)
	{
		Map<Kind,Integer> out=new EnumMap<>(Kind.class);
		for(Use use:uses) { out.merge(use.kind,1,Integer::sum); }
		return Map.copyOf(out);
	}
	public static void stock(List<Use> uses,Map<Integer,Long> inventory)
	{
		if(uses.isEmpty()) { return; }
		Map<Kind,Integer> available=quantities(inventory);
		consumed(uses).forEach((kind,n)->{
			if(n>available.get(kind)) { throw new IllegalArgumentException("Insufficient usable "+kind.label+" stock"); }
		});
	}
	/** Conditional level arithmetic only. HP remains an independent per-launch declaration, with no survival trajectory. */
	public static Map<String,Integer> levels(Map<String,Integer> base,Map<String,Integer> initial,List<Use> uses,int tick)
	{
		Map<String,Integer> levels=new TreeMap<>(initial);
		for(Use use:uses)
		{
			if(use.tick>tick) { break; }
			switch(use.kind)
			{
				case BREW:
					for(String skill:List.of("ATTACK","STRENGTH","RANGED","MAGIC"))
					{ if(levels.containsKey(skill)) { int current=need(levels,skill); levels.put(skill,Math.max(0,current-current/10-2)); } }
					boost(base,levels,"DEFENCE",20,2); break;
				case RESTORE:
					for(String skill:List.of("ATTACK","STRENGTH","DEFENCE","RANGED","MAGIC"))
					{
						if(!levels.containsKey(skill) || !base.containsKey(skill)) { levels.remove(skill); continue; }
						int current=need(levels,skill),max=need(base,skill); if(current<max) { levels.put(skill,Math.min(max,current+max/4+8)); }
					}
					break;
				case SUPER_COMBAT:
					for(String skill:List.of("ATTACK","STRENGTH","DEFENCE")) { boost(base,levels,skill,15,5); } break;
				case RANGING: boost(base,levels,"RANGED",10,4); break;
				default: break;
			}
		}
		return Map.copyOf(levels);
	}
	private static int need(Map<String,Integer> levels,String skill)
	{
		Integer n=levels.get(skill);
		if(n==null || n<0 || n>255) { throw new IllegalArgumentException("Supply-dependent "+skill+" level unread"); }
		return n;
	}
	private static void boost(Map<String,Integer> base,Map<String,Integer> levels,String skill,int percent,int flat)
	{
		if(!levels.containsKey(skill) || !base.containsKey(skill)) { levels.remove(skill); return; }
		int current=need(levels,skill),max=need(base,skill),amount=max*percent/100+flat;
		levels.put(skill,Math.max(current,Math.min(max+amount,current+amount)));
	}
}
