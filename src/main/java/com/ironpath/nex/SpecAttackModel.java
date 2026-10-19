package com.ironpath.nex;

import com.ironpath.advisor.PoweredMagic;
import java.util.*;

/** Original single-launch arithmetic. Exact package stats and legal target conditions are caller inputs. */
public final class SpecAttackModel
{
	public static final String VERSION="nex-spec-damage-3";
	public enum Bolt { RUBY, DIAMOND }
	public static final class Outcome
	{
		public final int damage, selfDamage;
		public final boolean boltProc;
		public final double probability;
		private Outcome(int damage,int selfDamage,boolean boltProc,double probability)
		{ this.damage=damage; this.selfDamage=selfDamage; this.boltProc=boltProc; this.probability=probability; }
	}
	public static final class Damage
	{
		public final SpecTimeline.Attack attack;
		public final boolean usesProjectile;
		public final List<Outcome> outcomes;
		public final double expectedDamage, expectedSelfDamage, procChance;
		public final int minimum, maximum;
		public final String basis;
		private Damage(SpecTimeline.Attack attack,boolean usesProjectile,List<Outcome> outcomes,String basis)
		{
			if(basis==null || basis.isBlank()) { throw new IllegalArgumentException("Package/target provenance required"); }
			this.attack=attack; this.usesProjectile=usesProjectile; this.outcomes=List.copyOf(outcomes); this.basis=basis;
			double damage=0,self=0,proc=0; int lo=Integer.MAX_VALUE,hi=0;
			for(Outcome o:outcomes)
			{
				damage+=o.probability*o.damage; self+=o.probability*o.selfDamage;
				if(o.boltProc) { proc+=o.probability; }
				lo=Math.min(lo,o.damage); hi=Math.max(hi,o.damage);
			}
			expectedDamage=damage; expectedSelfDamage=self; procChance=proc; minimum=lo; maximum=hi;
		}
	}
	/** Current HP and an explicitly reviewed hard hit cap, with the reviewed minion non-Magic flat-armour adjustment. No general phase model. */
	public static final class Limit
	{
		public final int hp, hitCap, flatArmour;
		public Limit(int hp,int hitCap) { this(hp,hitCap,0); }
		public Limit(int hp,int hitCap,int flatArmour)
		{
			if(flatArmour!=0 && flatArmour!=-2) { throw new IllegalArgumentException("Unreviewed flat armour"); }
			this.flatArmour=flatArmour;
			if(hp<1 || hp>100000 || hitCap<1 || hitCap>100000) { throw new IllegalArgumentException("Known positive HP and hard hit cap required"); }
			this.hp=hp; this.hitCap=hitCap;
		}
		private int apply(int value,boolean accurate) { return Math.min(value-(accurate?flatArmour:0),Math.min(hp,hitCap)); }
	}
	private SpecAttackModel() { }
	private static void max(int value)
	{ if(value<0 || value>1000) { throw new IllegalArgumentException("Max hit outside reviewed bounds"); } }
	private static void add(List<Outcome> out,int damage,int self,boolean proc,double probability,Limit limit,boolean accurate)
	{ if(probability>0) { out.add(new Outcome(limit.apply(damage,accurate),self,proc,probability)); } }
	private static void uniform(List<Outcome> out,int lo,int hi,double weight,boolean proc,Limit limit)
	{
		for(int value=lo;value<=hi;value++)
		{ add(out,hi==0?0:Math.max(1,value),0,proc,weight/(hi-lo+1),limit,true); }
	}
	/** Includes the successful-zero-to-one rule; misses remain zero. Fang may supply its reviewed bounds/chance. */
	public static Damage ordinary(int minHit,int maxHit,double accuracy,boolean usesProjectile,Limit limit,String basis)
	{
		max(maxHit); Objects.requireNonNull(limit);
		if(minHit<0 || minHit>maxHit || !Double.isFinite(accuracy) || accuracy<0 || accuracy>1)
		{ throw new IllegalArgumentException("Known damage interval and hit chance required"); }
		List<Outcome> out=new ArrayList<>(); add(out,0,0,false,1-accuracy,limit,false);
		uniform(out,minHit,maxHit,accuracy,false,limit); return new Damage(SpecTimeline.Attack.ORDINARY,usesProjectile,out,basis);
	}
	/** Uses the Voidwaker package's ordinary melee max, not the main-hand Fang max or Magic bonuses. */
	public static Damage voidwaker(int meleeMax,Limit limit,String basis)
	{
		max(meleeMax); Objects.requireNonNull(limit);
		List<Outcome> out=new ArrayList<>(); int lower=meleeMax/2,upper=meleeMax+lower;
		// Voidwaker deals Magic-type damage; minion non-Magic flat armour does not apply.
		uniform(out,lower,upper,1,false,new Limit(limit.hp,limit.hitCap));
		return new Damage(SpecTimeline.Attack.VOIDWAKER,false,out,basis);
	}
	/** PvM ruby/diamond only. Heavy ranged defence and complete package stats. Player HP is required only for ruby. */
	public static Damage crossbow(int maxHit,long attackRoll,long defenceRoll,Bolt bolt,boolean zaryte,boolean special,
		boolean hardKandarin,Integer playerHp,boolean rubyAllowed,Limit limit,String basis)
	{
		max(maxHit); Objects.requireNonNull(bolt); Objects.requireNonNull(limit);
		if(attackRoll<0 || attackRoll>1000000000L || defenceRoll<0 || defenceRoll>1000000000L
			|| playerHp!=null && (playerHp<1 || playerHp>255) || bolt==Bolt.RUBY && playerHp==null || special && !zaryte)
		{ throw new IllegalArgumentException("Known PvM rolls/HP and supported crossbow special required"); }
		double chance=PoweredMagic.hitChance(attackRoll*(special?2:1),defenceRoll);
		double natural=(bolt==Bolt.RUBY?0.06:0.10)*(hardKandarin?1.1:1);
		// Natural ruby/diamond effects bypass accuracy, including a missed Evoke roll.
		boolean eligible=bolt!=Bolt.RUBY || rubyAllowed && playerHp>=10;
		double proc=eligible?(special?chance+(1-chance)*natural:natural):0;
		double ordinaryHit=eligible && special?0:chance*(1-proc);
		double miss=eligible && special?1-proc:(1-chance)*(1-proc);
		List<Outcome> out=new ArrayList<>(); add(out,0,0,false,miss,limit,false);
		uniform(out,0,maxHit,ordinaryHit,false,limit);
		if(bolt==Bolt.RUBY)
		{ add(out,Math.min(zaryte?110:100,limit.hp*(zaryte?22:20)/100),playerHp/10,true,proc,limit,true); }
		else { uniform(out,0,maxHit*(zaryte?126:115)/100,proc,true,limit); }
		return new Damage(special?SpecTimeline.Attack.ZARYTE_CROSSBOW:SpecTimeline.Attack.ORDINARY,true,out,basis);
	}
}
