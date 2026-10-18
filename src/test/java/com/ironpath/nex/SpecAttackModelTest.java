package com.ironpath.nex;

import org.junit.Test;
import static org.junit.Assert.*;
import static com.ironpath.nex.SpecAttackModel.*;

/** Hand-enumerated cases, not expected values generated from production formulas. */
public class SpecAttackModelTest
{
	private final Limit high=new Limit(3400,1000);
	private Damage bolt(int max,Bolt bolt,boolean zcb,boolean spec,boolean diary,int hp,int playerHp,boolean eligible,int cap)
	{ return crossbow(max,100,199,bolt,zcb,spec,diary,playerHp,eligible,new Limit(hp,cap),"Synthetic complete package / fixed legal target window"); }
	private void normalized(Damage d)
	{
		assertEquals(1,d.outcomes.stream().mapToDouble(o->o.probability).sum(),1e-12);
		for(Outcome o:d.outcomes) { assertTrue(o.probability>0); assertTrue(o.damage>=0); }
	}
	@Test public void oddVoidwakerMaxUsesAnInclusiveRoundedIntervalAndGuaranteedDamage()
	{
		Damage d=voidwaker(51,high,"Voidwaker package, 51 ordinary melee max");
		assertEquals(25,d.minimum); assertEquals(76,d.maximum); assertEquals(52,d.outcomes.size());
		assertEquals(50.5,d.expectedDamage,1e-12); assertEquals(0,d.procChance,0); normalized(d);
		assertEquals(1545.0/52,voidwaker(51,new Limit(100,30),"Hard cap 30").expectedDamage,1e-12);
		assertEquals(20,voidwaker(51,new Limit(20,100),"20 HP left").expectedDamage,1e-12);
		assertEquals(1,voidwaker(1,high,"Successful zero boundary").expectedDamage,0);
	}
	@Test public void ordinaryMissAndSuccessfulZeroAreSeparateAndFangBoundsCanBeSupplied()
	{
		Damage d=ordinary(0,4,0.25,false,high,"Ordinary single hit");
		// On a hit: 1, 1, 2, 3, 4. Three quarters miss.
		assertEquals(0.55,d.expectedDamage,1e-12); assertEquals(0.75,d.outcomes.get(0).probability,0); normalized(d);
		assertEquals(12,ordinary(6,34,0.6,false,high,"Reviewed Fang bounds at base max 40").expectedDamage,1e-12);
		assertEquals(0,ordinary(0,0,1,false,high,"Zero max matches retained family model").expectedDamage,0);
	}
	@Test public void ordinaryRubyProcBypassesAccuracyAndReplacesRatherThanAddsDamage()
	{
		Damage d=bolt(40,Bolt.RUBY,false,false,false,1000,59,true,1000);
		// Ordinary accuracy 1/4, non-proc hit mean (1+1+2+...+40)/41.
		assertEquals(0.94*0.25*(20+1.0/41)+6,d.expectedDamage,1e-12);
		assertEquals(0.06,d.procChance,1e-12); assertEquals(0.30,d.expectedSelfDamage,1e-12); normalized(d);
		Damage noAccuracy=crossbow(40,0,199,Bolt.RUBY,false,false,false,59,true,high,"Zero accuracy still permits a natural proc");
		assertEquals(6,noAccuracy.expectedDamage,1e-12);
	}
	@Test public void zaryteSpecDoublesTheRollAndRetainsNaturalProcsAfterAnAccuracyMiss()
	{
		// Attack 100 -> 200 versus defence 199: spec accuracy exactly 1/2.
		Damage d=bolt(40,Bolt.RUBY,true,true,false,1000,59,true,1000);
		assertEquals(0.53,d.procChance,1e-12); assertEquals(58.3,d.expectedDamage,1e-12);
		assertEquals(2.65,d.expectedSelfDamage,1e-12); assertEquals(0,d.minimum); assertEquals(110,d.maximum); normalized(d);
		assertEquals(58.63,bolt(40,Bolt.RUBY,true,true,true,1000,59,true,1000).expectedDamage,1e-12);
		assertEquals(6.6,crossbow(40,0,199,Bolt.RUBY,true,true,false,59,true,high,"Natural proc after failed Evoke").expectedDamage,1e-12);
	}
	@Test public void rubyUsesCurrentHpRoundedCapsAndExplicitEligibility()
	{
		assertEquals(109*0.53,bolt(40,Bolt.RUBY,true,true,false,499,59,true,1000).expectedDamage,1e-12);
		assertEquals(110*0.53,bolt(40,Bolt.RUBY,true,true,false,500,59,true,1000).expectedDamage,1e-12);
		assertEquals(22*0.53,bolt(40,Bolt.RUBY,true,true,false,100,59,true,1000).expectedDamage,1e-12);
		assertEquals(50*0.53,bolt(40,Bolt.RUBY,true,true,false,1000,59,true,50).expectedDamage,1e-12);
		assertEquals(0,bolt(40,Bolt.RUBY,true,true,false,4,59,true,1000).expectedDamage,0);
		Damage low=bolt(40,Bolt.RUBY,true,true,false,1000,9,true,1000);
		assertEquals(0,low.procChance,0); assertEquals(0.5*(20+1.0/41),low.expectedDamage,1e-12);
		assertEquals(0.53,bolt(40,Bolt.RUBY,true,true,false,1000,10,true,1000).expectedSelfDamage,1e-12);
		assertEquals(low.expectedDamage,bolt(40,Bolt.RUBY,true,true,false,1000,99,false,1000).expectedDamage,1e-12);
	}
	@Test public void diamondHasItsOwnProcRateAndRoundedDamageRange()
	{
		// ZCB max 41 -> floor(51.66)=51; 1/2 spec accuracy plus 1/2 * 10% natural.
		Damage spec=bolt(41,Bolt.DIAMOND,true,true,false,1000,99,true,1000);
		assertEquals(51,spec.maximum); assertEquals(0.55,spec.procChance,1e-12);
		assertEquals(0.55*(25.5+1.0/52),spec.expectedDamage,1e-12); normalized(spec);
		Damage ordinary=bolt(41,Bolt.DIAMOND,false,false,false,1000,99,true,1000);
		assertEquals(47,ordinary.maximum); assertEquals(0.1,ordinary.procChance,1e-12);
		assertEquals(0.9*0.25*(20.5+1.0/42)+0.1*(23.5+1.0/48),ordinary.expectedDamage,1e-12);
		assertEquals(0,ordinary.expectedSelfDamage,0);
		assertEquals(0.11,bolt(41,Bolt.DIAMOND,true,false,true,1000,99,true,1000).procChance,1e-12);
	}
	@Test public void damageDistributionsNormalizeAcrossLowHpAndCapsAndRemainImmutable()
	{
		for(Bolt bolt:Bolt.values()) { for(boolean spec:new boolean[]{true,false}) { for(int hp:new int[]{1,4,499,500,3400})
		{
			Damage d=bolt(41,bolt,true,spec,true,hp,59,true,30); normalized(d); assertTrue(d.maximum<=Math.min(hp,30));
		} } }
		assertThrows(UnsupportedOperationException.class,()->voidwaker(50,high,"basis").outcomes.clear());
	}
	@Test public void unknownInvalidAndUnsupportedInputsDoNotBecomeZero()
	{
		assertThrows(IllegalArgumentException.class,()->ordinary(0,10,Double.NaN,false,high,"basis"));
		assertThrows(IllegalArgumentException.class,()->ordinary(11,10,1,false,high,"basis"));
		assertThrows(IllegalArgumentException.class,()->voidwaker(-1,high,"basis"));
		assertThrows(IllegalArgumentException.class,()->voidwaker(1001,high,"basis"));
		assertThrows(IllegalArgumentException.class,()->voidwaker(50,high,""));
		assertThrows(IllegalArgumentException.class,()->new Limit(-1,100));
		assertThrows(IllegalArgumentException.class,()->new Limit(100,0));
		assertThrows(IllegalArgumentException.class,()->crossbow(40,-1,10,Bolt.RUBY,true,true,false,99,true,high,"basis"));
		assertThrows(IllegalArgumentException.class,()->crossbow(40,100,10,Bolt.RUBY,false,true,false,99,true,high,"basis"));
		assertThrows(IllegalArgumentException.class,()->crossbow(40,100,10,Bolt.RUBY,true,true,false,0,true,high,"basis"));
	}
}
