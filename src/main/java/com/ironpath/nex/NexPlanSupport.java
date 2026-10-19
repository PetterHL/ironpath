package com.ironpath.nex;

import com.ironpath.advisor.KnowledgeCatalog;
import java.util.*;

/** Bounded decision context for an immutable declared plan. No capture attribution, global ranking or price IO. */
public final class NexPlanSupport
{
	public static final String VERSION="nex-plan-support-2";
	public static final int SPLINTER=28924;
	public static final class Cost
	{
		public final Map<Integer,Integer> units;
		public final Set<Integer> missingPrices;
		public final Long gp;
		private Cost(Map<Integer,Integer> units,Map<Integer,Long> prices)
		{
			if(prices.size()>32 || prices.entrySet().stream().anyMatch(e->e.getKey()<1 || e.getKey()>1000000 || e.getValue()<0 || e.getValue()>1000000000L))
			{ throw new IllegalArgumentException("Bounded nonnegative unit costs required"); }
			this.units=Map.copyOf(units); Set<Integer> missing=new TreeSet<>(); long sum=0;
			for(var entry:units.entrySet())
			{
				if(entry.getValue()==0) { continue; }
				Long price=prices.get(entry.getKey());
				if(price==null) { missing.add(entry.getKey()); }
				else { sum=Math.addExact(sum,Math.multiplyExact(price,entry.getValue().longValue())); }
			}
			missingPrices=Set.copyOf(missing); gp=missing.isEmpty()?sum:null;
		}
	}
	public static final class Route
	{
		public final int item;
		public final String label,work;
		public final List<String> stages,sources;
		private Route(int item,String label,String work,List<String> stages,List<String> sources)
		{ this.item=item; this.label=label; this.work=work; this.stages=List.copyOf(stages); this.sources=List.copyOf(sources); }
	}
	private NexPlanSupport() { }
	/** Counts only admitted projectiles, their specified splinter branch and explicitly consumed supply units. */
	public static Cost cost(NexPackageComparison.Evaluation evaluation,Map<Integer,Long> prices)
	{
		if(evaluation.plan==null) { return null; }
		Map<Integer,Integer> units=new TreeMap<>(evaluation.plan.spentAmmo);
		evaluation.spentRunes.forEach((id,n)->units.merge(id,n,Integer::sum));
		if(evaluation.plan.spentSplinters>0) { units.merge(SPLINTER,evaluation.plan.spentSplinters,Integer::sum); }
		for(var step:evaluation.conditions.steps)
		{ if(step.kind==NexPackageComparison.StepKind.SUPPLY) { units.merge(step.supply.kind.costKey(),1,Integer::sum); } }
		return new Cost(units,prices);
	}
	/** These are reviewed routes, not assessed remaining account work or complete-item acquisition times. */
	public static List<Route> routes(KnowledgeCatalog catalog,Map<String,Integer> edits)
	{
		List<Route> routes=new ArrayList<>();
		for(int id:new TreeSet<>(edits.values()))
		{
			if(id==0) { continue; }
			var upgrade=catalog.upgrades.stream().filter(u->u.itemId==id && "reviewed".equals(u.status)).findFirst();
			if(upgrade.isEmpty())
			{ routes.add(new Route(id,catalog.itemNames().getOrDefault(id,"Item "+id),null,List.of(),List.of())); continue; }
			var u=upgrade.get();
			routes.add(new Route(id,u.title,u.remainingWork,
				u.path.stream().filter(n->!n.alternativeOnly).map(n->n.label).collect(java.util.stream.Collectors.toList()),u.sources));
		}
		return List.copyOf(routes);
	}
	public static String decision(NexPackageComparison.Delta delta)
	{
		if(delta.expectedDamage==null) { return "Damage comparison unavailable"; }
		if(Math.abs(delta.expectedDamage)<1e-9) { return "Same expected damage in this plan"; }
		return delta.expectedDamage>0?"Higher expected damage in this plan":"Lower expected damage in this plan";
	}
	/** Fixed vocabulary: none of these absent signals can be inferred from equipment, impacts or net deltas. */
	public static List<String> automaticGaps()
	{
		return List.of("LEGAL_LAUNCH_ATTRIBUTION", "PER_LAUNCH_TARGET_HP", "INITIAL_ATTACK_AND_SPELL_READINESS",
			"NATURAL_RESTORE_PHASE_AND_ORDER", "CAST_AND_ELIGIBLE_DEATH_ATTRIBUTION", "USABLE_STOCK_AND_PACKING",
			"TARGET_PHASE_AND_OVERHEAD_EFFECTS", "CONFIRMED_SUPPLY_CONSUMPTION");
	}
}
