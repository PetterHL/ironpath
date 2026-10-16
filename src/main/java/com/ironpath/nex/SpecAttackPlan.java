package com.ironpath.nex;

import java.util.*;
import static com.ironpath.nex.SpecTimeline.*;

/** Finite conditional launch plan. Ammo-saving/charge outcomes are supplied branches, never inferred readings. */
public final class SpecAttackPlan
{
	public static final String VERSION="nex-spec-plan-1";
	public enum Quiver { NONE, UNCHARGED, CHARGED, BLESSED }
	/** Active retrieval, after any body/obstruction restrictions, not merely ownership of an Ava device. */
	public enum Retrieval
	{
		NONE(0), ACCUMULATOR(0.72), ASSEMBLER(0.80);
		public final double saveChance;
		Retrieval(double saveChance) { this.saveChance=saveChance; }
	}
	public static final class Launch
	{
		public final Attack attack;
		public final int ammoId;
		public final Retrieval retrieval;
		public final Quiver quiver;
		public final boolean savedAmmo, spentSplinter;
		public final SpecAttackModel.Damage unchargedDamage, chargedDamage;
		public final String damageGap;
		/**
		 * Both damage branches must be computed from their own complete packages. A charged quiver is not a
		 * fixed damage multiplier. ammoId=0 denotes a non-projectile attack; only storable arrows/bolts are supported.
		 * Null damage requires a named gap and blocks only damage, preserving the independently supplied resource plan.
		 */
		public Launch(Attack attack,int ammoId,Retrieval retrieval,Quiver quiver,boolean savedAmmo,boolean spentSplinter,
			SpecAttackModel.Damage unchargedDamage,SpecAttackModel.Damage chargedDamage,String damageGap)
		{
			this.attack=Objects.requireNonNull(attack); this.retrieval=Objects.requireNonNull(retrieval);
			this.quiver=Objects.requireNonNull(quiver);
			if(ammoId<0 || attack==Attack.VOIDWAKER && ammoId!=0 || attack==Attack.ZARYTE_CROSSBOW && ammoId==0
				|| ammoId==0 && (quiver!=Quiver.NONE || retrieval!=Retrieval.NONE || savedAmmo || spentSplinter)
				|| savedAmmo && retrieval==Retrieval.NONE || spentSplinter && quiver!=Quiver.CHARGED)
			{ throw new IllegalArgumentException("Incompatible projectile, retrieval or quiver branch"); }
			if((unchargedDamage==null || (quiver==Quiver.CHARGED || quiver==Quiver.BLESSED) && chargedDamage==null)
				&& (damageGap==null || damageGap.isBlank()))
			{ throw new IllegalArgumentException("Unsupported damage needs a scoped explanation"); }
			if(unchargedDamage!=null && unchargedDamage.attack!=attack || chargedDamage!=null && chargedDamage.attack!=attack)
			{ throw new IllegalArgumentException("Damage model does not match the launch type"); }
			if(unchargedDamage!=null && unchargedDamage.usesProjectile!=(ammoId>0)
				|| chargedDamage!=null && chargedDamage.usesProjectile!=(ammoId>0))
			{ throw new IllegalArgumentException("Projectile damage requires an exact finite ammo input"); }
			this.ammoId=ammoId; this.savedAmmo=savedAmmo; this.spentSplinter=spentSplinter;
			this.unchargedDamage=unchargedDamage; this.chargedDamage=chargedDamage; this.damageGap=damageGap;
		}
	}
	public static final class Plan
	{
		public final String context;
		public final SpecTimeline.Plan energy;
		public final Map<Integer,Launch> launches;
		public final Map<Integer,Integer> ammunition;
		public final int splinters;
		/** Known usable stock, not bank presence. Every attack event has one indexed launch, including equal-tick fallbacks. */
		public Plan(String context,SpecTimeline.Plan energy,Map<Integer,Launch> launches,Map<Integer,Integer> ammunition,int splinters)
		{
			if(context==null || context.isBlank() || splinters<0 || splinters>20000)
			{ throw new IllegalArgumentException("Explicit scenario context and known splinter stock required"); }
			this.energy=Objects.requireNonNull(energy);
			for(Map.Entry<Integer,Integer> stock:ammunition.entrySet())
			{ if(stock.getKey()<1 || stock.getValue()<0) { throw new IllegalArgumentException("Exact projectile and nonnegative known stock required"); } }
			int count=0;
			for(int i=0;i<energy.events.size();i++)
			{
				Event event=energy.events.get(i); if(!event.isAttack()) { continue; }
				Launch launch=launches.get(i); ++count;
				if(launch==null || launch.attack!=event.attack || launch.ammoId>0 && !ammunition.containsKey(launch.ammoId))
				{ throw new IllegalArgumentException("Every attack needs matching package inputs and known exact ammo stock"); }
			}
			if(count!=launches.size()) { throw new IllegalArgumentException("Launch index does not identify an attack event"); }
			this.context=context; this.launches=Map.copyOf(launches); this.ammunition=Map.copyOf(ammunition); this.splinters=splinters;
		}
	}
	public static final class Hit
	{
		public final int eventIndex;
		public final boolean charged;
		public final SpecAttackModel.Damage damage;
		public final String gap;
		private Hit(int eventIndex,boolean charged,SpecAttackModel.Damage damage,String gap)
		{ this.eventIndex=eventIndex; this.charged=charged; this.damage=damage; this.gap=gap; }
	}
	public static final class Result
	{
		public final String context;
		public final SpecTimeline.Result resources;
		public final Map<Integer,Integer> remainingAmmo, spentAmmo;
		public final int remainingSplinters, spentSplinters;
		/** Probability of these resource outcomes for accepted shots; not a confidence interval or encounter likelihood. */
		public final double resourceBranchProbability, logResourceBranchProbability;
		/** Sum of conditional launch expectations. No simulated HP trajectory, personal contribution or team time. */
		public final Double expectedDamage, expectedSelfDamage;
		public final List<Hit> hits;
		public final List<String> gaps;
		private Result(Plan p,Ledger ledger,SpecTimeline.Result resources)
		{
			context=p.context; this.resources=resources;
			remainingAmmo=Map.copyOf(ledger.ammo); Map<Integer,Integer> spent=new TreeMap<>();
			p.ammunition.forEach((id,quantity)->spent.put(id,quantity-ledger.ammo.get(id)));
			spentAmmo=Map.copyOf(spent); remainingSplinters=ledger.splinters; spentSplinters=p.splinters-ledger.splinters;
			logResourceBranchProbability=ledger.logProbability;
			resourceBranchProbability=Math.exp(logResourceBranchProbability); hits=List.copyOf(ledger.hits);
			List<String> gaps=new ArrayList<>(); double damage=0,self=0;
			for(Hit hit:hits)
			{
				if(hit.damage==null) { gaps.add("Event "+hit.eventIndex+": "+hit.gap); }
				else { damage+=hit.damage.expectedDamage; self+=hit.damage.expectedSelfDamage; }
			}
			this.gaps=List.copyOf(gaps); expectedDamage=gaps.isEmpty()?damage:null; expectedSelfDamage=gaps.isEmpty()?self:null;
		}
	}
	private static final class Ledger implements SpecTimeline.AttackResources
	{
		final Plan plan;
		final Map<Integer,Integer> ammo;
		final List<Hit> hits=new ArrayList<>();
		int splinters;
		double logProbability;
		Ledger(Plan plan) { this.plan=plan; ammo=new HashMap<>(plan.ammunition); splinters=plan.splinters; }
		@Override public boolean available(int index)
		{ Launch l=plan.launches.get(index); return l.ammoId==0 || ammo.get(l.ammoId)>0; }
		@Override public void accepted(int index)
		{
			Launch l=plan.launches.get(index);
			boolean charged=l.quiver==Quiver.BLESSED || l.quiver==Quiver.CHARGED && splinters>0;
			if(l.ammoId>0)
			{
				logProbability+=Math.log(l.savedAmmo?l.retrieval.saveChance:1-l.retrieval.saveChance);
				if(!l.savedAmmo) { ammo.compute(l.ammoId,(id,quantity)->quantity-1); }
				if(l.quiver==Quiver.CHARGED && charged)
				{
					logProbability+=Math.log(l.spentSplinter?1.0/3:2.0/3);
					if(l.spentSplinter) { --splinters; }
				}
			}
			SpecAttackModel.Damage damage=charged?l.chargedDamage:l.unchargedDamage;
			hits.add(new Hit(index,charged,damage,damage==null?l.damageGap:null));
		}
	}
	public Result run(Plan plan)
	{
		Ledger ledger=new Ledger(plan);
		return new Result(plan,ledger,new SpecTimeline().run(plan.energy,ledger));
	}
}
