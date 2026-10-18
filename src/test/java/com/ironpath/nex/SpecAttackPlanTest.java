package com.ironpath.nex;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.ironpath.nex.SpecTimeline.Attack;
import static com.ironpath.nex.SpecTimeline.Event;
import static com.ironpath.nex.SpecTimeline.Ring;
import static com.ironpath.nex.SpecTimeline.RestoreOrder;
import static com.ironpath.nex.SpecTimeline.Status;
import static com.ironpath.nex.SpecAttackPlan.*;

public class SpecAttackPlanTest
{
	private final SpecAttackPlan model=new SpecAttackPlan();
	private final SpecAttackModel.Limit limit=new SpecAttackModel.Limit(3400,1000);
	private SpecAttackModel.Damage fixed(int value)
	{ return SpecAttackModel.ordinary(value,value,1,false,limit,"Synthetic complete package / legal single launch"); }
	private Launch melee(int value)
	{ return new Launch(Attack.ORDINARY,0,Retrieval.NONE,Quiver.NONE,false,false,fixed(value),null,null); }
	private Launch arrow(Retrieval retrieval,Quiver quiver,boolean saved,boolean spent)
	{ return new Launch(Attack.ORDINARY,21326,retrieval,quiver,saved,spent,SpecAttackModel.ordinary(10,10,1,true,limit,"Uncharged synthetic arrow package"),
		SpecAttackModel.ordinary(11,11,1,true,limit,"Charged synthetic arrow package"),null); }
	private SpecTimeline.Plan energy(int initial,int end,Event... events)
	{ return new SpecTimeline.Plan(initial,end,Ring.LIGHTBEARER,25,RestoreOrder.BEFORE_EVENTS,List.of(events)); }
	private Result twoArrows(int stock,int splinters,Launch first,Launch second)
	{ return model.run(new Plan("Same two legal opportunities",energy(1000,10,Event.ordinary(0,5),Event.ordinary(5,5)),
		Map.of(0,first,1,second),Map.of(21326,stock),splinters)); }
	@Test public void finiteSpecsDisplaceOrdinaryAttacksAndCompeteForEnergyAndAmmo()
	{
		Launch vw=new Launch(Attack.VOIDWAKER,0,Retrieval.NONE,Quiver.NONE,false,false,SpecAttackModel.voidwaker(40,limit,"VW package"),null,null);
		Launch zcb=new Launch(Attack.ZARYTE_CROSSBOW,21944,Retrieval.NONE,Quiver.NONE,false,false,
			SpecAttackModel.crossbow(40,100,199,SpecAttackModel.Bolt.RUBY,true,true,false,99,true,limit,"ZCB/ruby package"),null,null);
		SpecTimeline.Plan timeline=energy(1000,20,Event.voidwaker(0),Event.ordinary(1,5),Event.zaryteCrossbow(4,true),
			Event.ordinary(4,5),Event.voidwaker(9),Event.ordinary(13,5),Event.ordinary(13,5));
		Result r=model.run(new Plan("Finite mixed roles",timeline,Map.of(0,vw,1,melee(50),2,zcb,3,arrow(Retrieval.NONE,Quiver.NONE,false,false),
			4,vw,5,arrow(Retrieval.NONE,Quiver.NONE,false,false),6,melee(7)),Map.of(21326,1,21944,1),0));
		assertEquals(2,r.resources.voidwakerSpecs); assertEquals(0,r.resources.zaryteSpecs); assertEquals(2,r.resources.ordinaryAttacks);
		assertEquals(1000,r.resources.spent); assertEquals(0,r.resources.finalEnergy); assertEquals(97,r.expectedDamage,1e-12);
		assertEquals(Integer.valueOf(1),r.spentAmmo.get(21326)); assertEquals(Integer.valueOf(0),r.spentAmmo.get(21944));
		assertEquals(Status.ATTACK_DELAY,r.resources.entries.get(1).status);
		assertEquals(Status.INSUFFICIENT_ENERGY,r.resources.entries.get(2).status);
		assertEquals(Status.NO_AMMUNITION,r.resources.entries.get(5).status);
		assertEquals(6,r.hits.get(3).eventIndex); // Explicit same-tick fallback works, with no phantom delay.
	}
	@Test public void emptyBoltsRejectASpecBeforeSpendingAndAllowAnExplicitFallback()
	{
		Launch zcb=new Launch(Attack.ZARYTE_CROSSBOW,21944,Retrieval.NONE,Quiver.NONE,false,false,null,null,"HP trajectory unavailable");
		Result r=model.run(new Plan("Empty bolts",energy(1000,10,Event.zaryteCrossbow(0,true),Event.ordinary(0,5)),
			Map.of(0,zcb,1,melee(7)),Map.of(21944,0),0));
		assertEquals(1000,r.resources.finalEnergy); assertEquals(0,r.resources.spent); assertEquals(7,r.expectedDamage,0);
		assertEquals(Status.NO_AMMUNITION,r.resources.entries.get(0).status); assertTrue(r.gaps.isEmpty());
	}
	@Test public void lastSplinterBoostsThatShotAndExhaustionChangesTheNextCompletePackage()
	{
		Result r=twoArrows(2,1,arrow(Retrieval.NONE,Quiver.CHARGED,false,true),arrow(Retrieval.NONE,Quiver.CHARGED,false,true));
		assertEquals(21,r.expectedDamage,0); assertTrue(r.hits.get(0).charged); assertFalse(r.hits.get(1).charged);
		assertEquals(1,r.spentSplinters); assertEquals(0,r.remainingSplinters); assertEquals(1.0/3,r.resourceBranchProbability,1e-12);
		assertEquals(20,twoArrows(2,0,arrow(Retrieval.NONE,Quiver.CHARGED,false,true),arrow(Retrieval.NONE,Quiver.CHARGED,false,true)).expectedDamage,0);
	}
	@Test public void chargedAndBlessedHaveEqualOffenceWhileOnlyChargedConsumesSplinters()
	{
		Result charged=twoArrows(2,2,arrow(Retrieval.NONE,Quiver.CHARGED,false,true),arrow(Retrieval.NONE,Quiver.CHARGED,false,true));
		Result blessed=twoArrows(2,0,arrow(Retrieval.NONE,Quiver.BLESSED,false,false),arrow(Retrieval.NONE,Quiver.BLESSED,false,false));
		assertEquals(22,charged.expectedDamage,0); assertEquals(charged.expectedDamage,blessed.expectedDamage);
		assertEquals(2,charged.spentSplinters); assertEquals(0,blessed.spentSplinters);
		assertEquals(1.0/9,charged.resourceBranchProbability,1e-12); assertEquals(1,blessed.resourceBranchProbability,0);
	}
	@Test public void allFiniteAmmoBranchesNormalizeAndDoNotSpendExpectedFractionalStock()
	{
		Launch saved=arrow(Retrieval.ASSEMBLER,Quiver.NONE,true,false),lost=arrow(Retrieval.ASSEMBLER,Quiver.NONE,false,false);
		// The three terminal paths with one arrow: saved/saved, saved/lost, lost/rejected.
		List<Result> branches=List.of(twoArrows(1,0,saved,saved),twoArrows(1,0,saved,lost),twoArrows(1,0,lost,lost));
		assertEquals(1,branches.stream().mapToDouble(r->r.resourceBranchProbability).sum(),1e-12);
		assertEquals(18,branches.stream().mapToDouble(r->r.resourceBranchProbability*r.expectedDamage).sum(),1e-12);
		assertEquals(0.36,branches.stream().mapToDouble(r->r.resourceBranchProbability*r.spentAmmo.get(21326)).sum(),1e-12);
		for(Result r:branches) { assertTrue(r.remainingAmmo.get(21326)>=0); }
	}
	@Test public void finiteSplinterBranchesNormalizeWithoutAssumingPermanentCharge()
	{
		Launch spent=arrow(Retrieval.NONE,Quiver.CHARGED,false,true),kept=arrow(Retrieval.NONE,Quiver.CHARGED,false,false);
		List<Result> branches=List.of(twoArrows(2,1,spent,kept),twoArrows(2,1,kept,spent),twoArrows(2,1,kept,kept));
		assertEquals(1,branches.stream().mapToDouble(r->r.resourceBranchProbability).sum(),1e-12);
		assertEquals(65.0/3,branches.stream().mapToDouble(r->r.resourceBranchProbability*r.expectedDamage).sum(),1e-12);
		assertEquals(5.0/9,branches.stream().mapToDouble(r->r.resourceBranchProbability*r.spentSplinters).sum(),1e-12);
	}
	@Test public void longRareResourceBranchesKeepAFiniteLogProbability()
	{
		List<Event> events=new ArrayList<>(); Map<Integer,Launch> launches=new HashMap<>();
		for(int i=0;i<500;i++)
		{ events.add(Event.ordinary(i*5,5)); launches.put(i,arrow(Retrieval.ASSEMBLER,Quiver.NONE,false,false)); }
		SpecTimeline.Plan energy=new SpecTimeline.Plan(1000,2500,Ring.LIGHTBEARER,25,RestoreOrder.BEFORE_EVENTS,events);
		Result r=model.run(new Plan("Long conditional resource branch",energy,launches,Map.of(21326,500),0));
		assertEquals(500,r.resources.ordinaryAttacks); assertEquals(Integer.valueOf(500),r.spentAmmo.get(21326));
		assertEquals(0,r.resourceBranchProbability,0); // Double probability underflows; the branch is still possible.
		assertEquals(500*Math.log(0.2),r.logResourceBranchProbability,1e-8);
	}
	@Test public void damageGapsPreserveEnergyAmmoAndUnrelatedSupportedLaunches()
	{
		Launch unsupported=new Launch(Attack.VOIDWAKER,0,Retrieval.NONE,Quiver.NONE,false,false,null,null,"Zaros soft-cap/deflect window unsupported");
		Result r=model.run(new Plan("Named unsupported damage",energy(1000,10,Event.voidwaker(0),Event.ordinary(4,5)),
			Map.of(0,unsupported,1,arrow(Retrieval.NONE,Quiver.NONE,false,false)),Map.of(21326,1),0));
		assertNull(r.expectedDamage); assertNull(r.expectedSelfDamage); assertEquals(500,r.resources.spent);
		assertEquals(1,r.gaps.size()); assertTrue(r.gaps.get(0).contains("Zaros"));
		assertEquals(10,r.hits.get(1).damage.expectedDamage,0); assertEquals(Integer.valueOf(1),r.spentAmmo.get(21326));
	}
	@Test public void deathChargeAndNaturalRestorationStillShareEnergyWithResourceGatedSpecs()
	{
		Launch vw=new Launch(Attack.VOIDWAKER,0,Retrieval.NONE,Quiver.NONE,false,false,SpecAttackModel.voidwaker(40,limit,"VW package"),null,null);
		Result r=model.run(new Plan("Improved DC and fixed Lightbearer",energy(400,26,Event.cast(0,true),Event.eligibleDeath(1,"a"),
			Event.voidwaker(1),Event.eligibleDeath(2,"b"),Event.voidwaker(25)),Map.of(2,vw,4,vw),Map.of(),0));
		assertEquals(1,r.resources.voidwakerSpecs); assertEquals(2,r.resources.deathChargeProcs);
		assertEquals(300,r.resources.finalEnergy); assertEquals(40,r.expectedDamage,1e-12);
		assertEquals(2,r.resources.entries.get(2).eventIndex); assertEquals(-1,r.resources.entries.get(4).eventIndex);
	}
	@Test public void identicalAndWorseWholePlansKeepZeroAndNegativeDeltas()
	{
		Result base=twoArrows(2,0,arrow(Retrieval.NONE,Quiver.BLESSED,false,false),arrow(Retrieval.NONE,Quiver.BLESSED,false,false));
		Result same=twoArrows(2,0,arrow(Retrieval.NONE,Quiver.BLESSED,false,false),arrow(Retrieval.NONE,Quiver.BLESSED,false,false));
		Result lower=twoArrows(2,0,arrow(Retrieval.NONE,Quiver.UNCHARGED,false,false),arrow(Retrieval.NONE,Quiver.UNCHARGED,false,false));
		assertEquals(0,same.expectedDamage-base.expectedDamage,0); assertEquals(-2,lower.expectedDamage-base.expectedDamage,0);
	}
	@Test public void explicitVoidwakerAndZarytePlansIncludeDifferentDisplacedOrdinaryAttacks()
	{
		String context="Synthetic ten-tick horizon, fixed HP and explicitly legal opportunities";
		Result baseline=model.run(new Plan(context,energy(1000,10,Event.ordinary(0,5),Event.ordinary(5,5)),
			Map.of(0,melee(20),1,melee(20)),Map.of(),0));
		Launch vw=new Launch(Attack.VOIDWAKER,0,Retrieval.NONE,Quiver.NONE,false,false,SpecAttackModel.voidwaker(40,limit,"VW package"),null,null);
		Result v=model.run(new Plan(context,energy(1000,10,Event.voidwaker(0),Event.ordinary(4,5),Event.ordinary(5,5),Event.ordinary(9,5)),
			Map.of(0,vw,1,melee(20),2,melee(20),3,melee(20)),Map.of(),0));
		Launch zcb=new Launch(Attack.ZARYTE_CROSSBOW,21944,Retrieval.NONE,Quiver.NONE,false,false,
			SpecAttackModel.crossbow(40,100,199,SpecAttackModel.Bolt.RUBY,true,true,false,99,true,limit,"ZCB package"),null,null);
		Result z=model.run(new Plan(context,energy(1000,10,Event.zaryteCrossbow(0,true),Event.ordinary(4,5),Event.ordinary(5,5)),
			Map.of(0,zcb,1,melee(20),2,melee(20)),Map.of(21944,1),0));
		assertEquals(40,baseline.expectedDamage,1e-12);
		assertEquals(40,v.expectedDamage-baseline.expectedDamage,1e-12); // One VW plus two ordinary, not baseline + VW.
		assertEquals(38.3,z.expectedDamage-baseline.expectedDamage,1e-12); // One ZCB plus one ordinary.
		assertEquals(500,v.resources.spent); assertEquals(750,z.resources.spent);
		assertEquals(Integer.valueOf(1),z.spentAmmo.get(21944));
	}
	@Test public void plansFreezeInputsAndRejectUnknownStocksMismatchedDamageAndImpossibleBranches()
	{
		Map<Integer,Integer> stocks=new HashMap<>(); stocks.put(21326,1);
		Map<Integer,Launch> launches=new HashMap<>(); launches.put(0,arrow(Retrieval.NONE,Quiver.NONE,false,false));
		Plan p=new Plan("Frozen",energy(0,1,Event.ordinary(0,5)),launches,stocks,0); stocks.clear(); launches.clear();
		Result r=model.run(p); assertEquals(10,r.expectedDamage,0);
		assertThrows(UnsupportedOperationException.class,()->r.spentAmmo.clear());
		assertThrows(UnsupportedOperationException.class,()->r.hits.clear());
		assertThrows(IllegalArgumentException.class,()->new Plan("Unknown ammo",p.energy,p.launches,Map.of(),0));
		assertThrows(IllegalArgumentException.class,()->new Plan("Unknown splinters",p.energy,p.launches,p.ammunition,-1));
		assertThrows(IllegalArgumentException.class,()->new Plan("Missing launch",p.energy,Map.of(),p.ammunition,0));
		assertThrows(IllegalArgumentException.class,()->new Launch(Attack.VOIDWAKER,0,Retrieval.NONE,Quiver.NONE,false,false,fixed(10),null,null));
		assertThrows(IllegalArgumentException.class,()->arrow(Retrieval.NONE,Quiver.NONE,true,false));
		assertThrows(IllegalArgumentException.class,()->arrow(Retrieval.NONE,Quiver.BLESSED,false,true));
		assertThrows(IllegalArgumentException.class,()->new Launch(Attack.ORDINARY,21326,Retrieval.NONE,Quiver.NONE,false,false,fixed(10),null,null));
		assertThrows(IllegalArgumentException.class,()->new Launch(Attack.ORDINARY,0,Retrieval.NONE,Quiver.NONE,false,false,null,null,null));
	}
}
