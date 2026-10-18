package com.ironpath;

import com.ironpath.advisor.ComparisonResult;
import com.ironpath.advisor.NexOwnedResult;
import com.ironpath.advisor.PoweredMagic;
import java.util.*;

/** Small deterministic projection of existing comparisons; performs no combat calculations. */
final class NexPresentation
{
	static final class Card
	{
		final int choiceIndex, pairIndex;
		final NexOwnedResult.Choice choice;
		final ComparisonResult pair;
		Card(int choiceIndex,int pairIndex,NexOwnedResult.Choice choice,ComparisonResult pair)
		{ this.choiceIndex=choiceIndex; this.pairIndex=pairIndex; this.choice=choice; this.pair=pair; }
	}
	/** Carried before bank preparation, then original capture order. First positive supported
	 * package per state; identical before/after packages in a role appear once,
	 * preferring the carried scenario when bank search finds that same change.
	 * Percentages from unrelated states are never compared or combined. */
	static List<Card> cards(NexOwnedResult result)
	{
		List<Card> cards=new ArrayList<>(); Set<List<Object>> seen=new HashSet<>();
		for(boolean bank:List.of(false,true))
		{
			for(int i=0;i<result.choices.size();i++)
			{
				var c=result.choices.get(i); if(c.bankPreparation!=bank || !c.supported()) { continue; }
				List<ComparisonResult> pairs=new ArrayList<>(); pairs.add(c.comparison); pairs.addAll(c.alternatives);
				for(int j=0;j<pairs.size();j++)
				{
					var p=pairs.get(j);
					if(p.before==null || p.after==null || !(p.dpsGain()>0)) { continue; }
					List<Object> key=List.of(c.actual.role,p.beforeSetup.slots,p.beforeSetup.loadedAmmo,p.afterSetup.slots,p.afterSetup.loadedAmmo);
					if(seen.add(key)) { cards.add(new Card(i,j,c,p)); }
					break;
				}
			}
		}
		return List.copyOf(cards);
	}
	/** A quiver projectile is separate from the physical worn ammo slot. */
	static Map<String,Integer> changedItems(ComparisonResult pair)
	{
		Map<String,Integer> changed=new LinkedHashMap<>();
		for(String slot:PoweredMagic.SLOTS)
		{ if(!Objects.equals(pair.beforeSetup.slots.get(slot),pair.afterSetup.slots.get(slot))) { changed.put(slot,pair.afterSetup.slots.getOrDefault(slot,-1)); } }
		// Moving the projectile from quiver to the worn ammo slot is one ammunition
		// change, not a second empty-item upgrade.
		if(pair.beforeSetup.loadedAmmo!=pair.afterSetup.loadedAmmo && !changed.containsKey("ammo"))
		{ changed.put("quiver projectile",pair.afterSetup.loadedAmmo>0?pair.afterSetup.loadedAmmo:pair.afterSetup.slots.getOrDefault("ammo",-1)); }
		return Collections.unmodifiableMap(changed);
	}
	static String scope(NexOwnedResult.Choice choice)
	{
		String phase=choice.actual.phase.toLowerCase(Locale.ROOT).replace('_',' ');
		return Character.toUpperCase(choice.actual.role.charAt(0))+choice.actual.role.substring(1)+" · "+phase;
	}
	static String reason(ComparisonResult pair)
	{
		boolean hit=pair.after.maxHit>pair.before.maxHit, accuracy=pair.available(ComparisonResult.Metric.HIT_CHANCE) && pair.after.hitChance>pair.before.hitChance;
		if(hit && accuracy) { return "Higher max hit and accuracy."; }
		if(hit) { return "Higher max hit."; }
		if(accuracy) { return "Higher accuracy."; }
		return "More damage in this setup.";
	}
	static String context(com.ironpath.nex.NexContext context)
	{
		String strategy=context.strategy==com.ironpath.nex.NexContext.Strategy.UNKNOWN?"Unknown strategy":context.strategy==com.ironpath.nex.NexContext.Strategy.OTHER?"Other strategy":context.strategy.name();
		String group=context.group==com.ironpath.nex.NexContext.Group.ORGANIZED?"":context.group==com.ironpath.nex.NexContext.Group.MASS?" · mass":" · unknown group";
		String count=context.participants!=null?context.participants+" players":context.participation==com.ironpath.nex.NexContext.Participation.VARIABLE?"variable group":"unknown count";
		return strategy+group+" · "+count+" · "+(context.confirmed()?"confirmed":"unconfirmed");
	}
}
