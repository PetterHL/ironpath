package com.ironpath.advisor;

import com.ironpath.model.*;
import com.ironpath.preferences.Preferences;
import java.util.*;

/** Qualitative remaining-work policy, independent of GE prices or invented acquisition hours. */
public final class IronmanOrder
{
	public static final String VERSION="ironman-order-2";
	public static final class Work
	{
		public final int tier; // 0 available, 1 assembly, 2..5 reviewed bands, 6 unknown, 7 preparation
		public final String reason;
		Work(int tier,String reason) { this.tier=tier; this.reason=reason; }
	}
	public static Work work(ComparisonResult pair,KnowledgeCatalog c,AccountSnapshot account,Preferences prefs)
	{
		int tier=0; List<String> reasons=new ArrayList<>();
		for(var entry:pair.afterSetup.slots.entrySet())
		{
			int id=entry.getValue(); if(id<=0 || Objects.equals(pair.beforeSetup.slots.get(entry.getKey()),id)) { continue; }
			if(account.observedItem(id)==Truth.TRUE) { reasons.add("Already observed: "+id); continue; }
			var upgrade=c.upgrades.stream().filter(u->u.itemId==id).findFirst().orElse(null);
			if(upgrade==null) { tier=Math.max(tier,6); reasons.add("Acquisition unreviewed: "+id); continue; }
			var route=c.decisions.stream().filter(d->upgrade.routes.contains(d.id) && d.pursuit!=null)
				.min(Comparator.comparingInt(d->d.pursuit.band==null?6:d.pursuit.band.ordinal()+2)).orElse(null);
			int itemTier=route==null || route.pursuit.band==null?6:route.pursuit.band.ordinal()+2;
			if(route!=null)
			{
				Truth ready=Truth.all(condition(route.accessWhen,c,account,prefs),condition(route.prepWhen,c,account,prefs));
				if(ready!=Truth.TRUE) { itemTier=ready==Truth.FALSE?7:6; }
				if(route.pursuit.profile!=null)
				{
					var profile=c.effortProfiles.get(route.pursuit.profile);
					if(profile!=null)
					{
						var remaining=RemainingEffort.evaluate(profile,route.pursuit.band,route.pursuit.reason,
							cond->condition(cond,c,account,prefs),false);
						itemTier=Math.max(itemTier,remaining.suitability==RemainingEffort.Suitability.PREPARE?7:
							remaining.band==RemainingEffort.Band.UNKNOWN?6:remaining.band.ordinal()+2);
					}
				}
			}
			itemTier=remainingPath(upgrade,itemTier,c,account,prefs);
			tier=Math.max(tier,itemTier); reasons.add(upgrade.remainingWork+(itemTier==1
				?" Confirmed components bypass acquisition; assembly supplies and allocations require checking."
				:" Remaining requirements and allocations require checking."));
		}
		return new Work(tier,String.join(" ",reasons));
	}
	private static int remainingPath(KnowledgeCatalog.Upgrade upgrade,int acquisitionTier,KnowledgeCatalog c,AccountSnapshot a,Preferences p)
	{
		if(upgrade.path.isEmpty()) { return acquisitionTier; }
		Map<String,KnowledgeCatalog.PathNode> nodes=new HashMap<>(); Set<String> referenced=new HashSet<>();
		for(var node:upgrade.path) { nodes.put(node.id,node); references(node.dependencies,referenced); }
		Map<String,Integer> memo=new HashMap<>(); int tier=1;
		// Start at the outputs, not every historical ingredient or unused alternative.
		for(var node:upgrade.path)
		{
			if(!node.alternativeOnly && !referenced.contains(node.id))
			{ tier=Math.max(tier,nodeTier(node,acquisitionTier,nodes,memo,c,a,p)); }
		}
		return tier;
	}
	private static void references(KnowledgeCatalog.Dependencies dependencies,Set<String> ids)
	{
		if(dependencies==null) { return; }
		if(dependencies.mode==KnowledgeCatalog.DependencyMode.NODE) { ids.add(dependencies.node); }
		else { dependencies.children.forEach(d->references(d,ids)); }
	}
	private static int nodeTier(KnowledgeCatalog.PathNode node,int acquisitionTier,Map<String,KnowledgeCatalog.PathNode> nodes,
		Map<String,Integer> memo,KnowledgeCatalog c,AccountSnapshot a,Preferences p)
	{
		if(memo.containsKey(node.id)) { return memo.get(node.id); }
		Truth complete=condition(node.completeWhen,c,a,p);
		if(node.quantity!=null && node.quantity>1)
		{
			var quantity=a.observedQuantity(node.itemId);
			complete=Truth.all(complete,!quantity.known()?Truth.UNKNOWN:quantity.value()>=node.quantity?Truth.TRUE:Truth.FALSE);
		}
		// A completed downstream stage replaces all of its upstream work.
		if(complete==Truth.TRUE) { memo.put(node.id,0); return 0; }
		int tier;
		if(node.acquisition || "ACTIVITY".equals(node.role)) { tier=acquisitionTier; }
		else if(node.dependencies!=null) { tier=1; } // An assembly operation, not an unmet skill.
		else if("ASSEMBLY".equals(node.role) || "ACCESS".equals(node.role) || "PREPARATION".equals(node.role)
			|| node.alternativeOnly) { tier=complete==Truth.FALSE?7:6; }
		else
		{
			// Routine supply placeholders remain assembly checks. Missing owned-component
			// alternatives cannot manufacture an assembly-only path.
			tier=node.completeWhen.test==KnowledgeCatalog.Test.CONSTANT?1:acquisitionTier;
		}
		tier=Math.max(tier,dependencyTier(node.dependencies,acquisitionTier,nodes,memo,c,a,p));
		memo.put(node.id,tier); return tier;
	}
	private static int dependencyTier(KnowledgeCatalog.Dependencies d,int acquisitionTier,Map<String,KnowledgeCatalog.PathNode> nodes,
		Map<String,Integer> memo,KnowledgeCatalog c,AccountSnapshot a,Preferences p)
	{
		if(d==null) { return 0; }
		if(d.mode==KnowledgeCatalog.DependencyMode.NODE) { return nodeTier(nodes.get(d.node),acquisitionTier,nodes,memo,c,a,p); }
		var tiers=d.children.stream().mapToInt(child->dependencyTier(child,acquisitionTier,nodes,memo,c,a,p));
		return d.mode==KnowledgeCatalog.DependencyMode.ANY?tiers.min().orElse(6):tiers.max().orElse(0);
	}
	static Truth condition(KnowledgeCatalog.Condition x,KnowledgeCatalog c,AccountSnapshot a,Preferences p)
	{
		if(x==null) { return Truth.UNKNOWN; }
		Truth truth;
		switch(x.test)
		{
			case CONSTANT: return Boolean.TRUE.equals(x.value)?Truth.TRUE:Truth.FALSE;
			case ALL: return Truth.all(x.conditions.stream().map(v->condition(v,c,a,p)).toArray(Truth[]::new));
			case ANY: return Truth.any(x.conditions.stream().map(v->condition(v,c,a,p)).toArray(Truth[]::new));
			case LEVEL: return a.atLeast(x.key,x.minimum);
			case FACT: truth=AccountFacts.planning(a,p,x.key); break;
			case RIGOUR: truth=Truth.of(a.rigour); break;
			case AUGURY: truth=Truth.of(a.augury); break;
			case POSSESSION: case ITEMS:
				var ids=c.itemSets.get(x.key); if(ids==null) { return Truth.UNKNOWN; }
				truth=Truth.any(ids.stream().map(a::observedItem).toArray(Truth[]::new)); break;
			case USABLE:
				var usableIds=c.itemSets.get(x.key); if(usableIds==null) { return Truth.UNKNOWN; }
				truth=Truth.any(usableIds.stream().map(id->{
					var gear=c.gear.values().stream().flatMap(List::stream).filter(g->g.id==id).findFirst().orElse(null);
					return gear==null?Truth.UNKNOWN:Truth.all(a.observedItem(id),a.atLeast(gear.skill,gear.level),
						gear.useWhen==null?Truth.TRUE:condition(gear.useWhen,c,a,p));
				}).toArray(Truth[]::new)); break;
			default: return Truth.UNKNOWN;
		}
		return !Boolean.FALSE.equals(x.value)?truth:truth==Truth.UNKNOWN?truth:truth==Truth.TRUE?Truth.FALSE:Truth.TRUE;
	}
	/** Positive alternatives first; diminishing benefit preference with a fixed remaining-work penalty.
	 * Monotone in benefit and effort, so beneficial/easier dominance is preserved. Unknown is penalized. */
	public static double priority(double gain,int tier)
	{ return Math.log1p(Math.max(0,gain)*100)/Math.log(2)-0.5*tier; }
	private IronmanOrder() { }
}
