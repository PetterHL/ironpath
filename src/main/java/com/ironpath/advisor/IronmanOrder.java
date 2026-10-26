package com.ironpath.advisor;

import com.ironpath.model.*;
import com.ironpath.preferences.Preferences;
import java.util.*;

/** Qualitative remaining-work policy, independent of GE prices or invented acquisition hours. */
public final class IronmanOrder
{
	public static final String VERSION="ironman-order-1";
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
			// Complete components remove acquisition only when every acquisition node is confirmed.
			var nodes=upgrade.path.stream().filter(n->n.acquisition).collect(java.util.stream.Collectors.toList());
			boolean assembled=!nodes.isEmpty() && nodes.stream().allMatch(n->condition(n.completeWhen,c,account,prefs)==Truth.TRUE
				&& (n.quantity==null || n.quantity<=1 || n.itemId!=null && account.observedQuantity(n.itemId).known()
					&& account.observedQuantity(n.itemId).value()>=n.quantity));
			if(assembled) { itemTier=1; }
			if(route!=null)
			{
				Truth ready=Truth.all(condition(route.accessWhen,c,account,prefs),condition(route.prepWhen,c,account,prefs));
				if(!assembled && ready!=Truth.TRUE) { itemTier=ready==Truth.FALSE?7:6; }
				if(route.pursuit.profile!=null && !assembled)
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
			for(var node:upgrade.path)
			{
				if(!"SKILL".equals(node.role)) { continue; }
				Truth skill=condition(node.completeWhen,c,account,prefs);
				if(skill!=Truth.TRUE) { itemTier=Math.max(itemTier,skill==Truth.FALSE?7:6); }
			}
			tier=Math.max(tier,itemTier); reasons.add(upgrade.remainingWork+(assembled?" Confirmed components bypass acquisition.":" Remaining requirements and allocations require checking."));
		}
		return new Work(tier,String.join(" ",reasons));
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
