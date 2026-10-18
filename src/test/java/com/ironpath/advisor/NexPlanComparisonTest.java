package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.encounter.CombatState;
import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.nex.*;
import com.ironpath.preferences.NexMethod;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

/** Whole runtime join, with hand-calculated launch expectations and finite stock. */
public class NexPlanComparisonTest
{
	private final KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson());
	public NexPlanComparisonTest() throws Exception { }
	public static Map<String,Integer> slots(int weapon,int ammo)
	{
		Map<String,Integer> slots=new TreeMap<>(); PoweredMagic.SLOTS.forEach(slot->slots.put(slot,0));
		slots.put("ring",25975); slots.put("weapon",weapon); slots.put("ammo",ammo); return slots;
	}
	public static NexOwnedResult fixture(KnowledgeCatalog catalog,int weapon,int ammo,Map<Integer,Long> inventory,Map<String,Integer> counters,boolean ownedNeck)
	{
		Map<String,Integer> slots=slots(weapon,ammo), levels=new TreeMap<>();
		for(String key:List.of("ATTACK","STRENGTH","DEFENCE","RANGED","MAGIC","HITPOINTS","PRAYER","AGILITY")) { levels.put(key,99); }
		Map<String,Integer> visible=new TreeMap<>(levels); visible.put("ATTACK",118); visible.put("STRENGTH",118); visible.put("RANGED",112);
		CombatState state=new CombatState(slots,levels,visible,Set.of(weapon==27690?"PIETY":"RIGOUR"),1,1000,false,inventory,counters,Map.of(weapon,"PRIVATE_SENTINEL"));
		var sample=new NexSummary.Sample(0,"SMOKE","NONE","NEX@PRIVATE_SENTINEL","spec candidate",-1,state);
		var summary=new NexSummary(1,0,125,125,125,0,Outcome.COMPLETE,"PRIVATE_SENTINEL","PRIVATE_SENTINEL","PRIVATE_SENTINEL",List.of(sample),Map.of(),Set.of(),
			new NexSummary.Signals(0,0,125,126,true,Set.of("SMOKE")));
		ComparisonResult pair=null; CombatSetup after=null;
		if(ownedNeck)
		{
			var edited=new TreeMap<>(slots); edited.put("neck",29801);
			var factory=new NexPackageFactory(catalog); var before=factory.build(true,slots,Map.of(),1,state.prayers,0); after=factory.build(true,edited,Map.of(),1,state.prayers,0);
			var items=catalog.melee.items.stream().collect(Collectors.toMap(i->i.id,i->i)); var target=catalog.comparison.targets.stream().filter(t->t.id.equals("nex-base")).findFirst().orElseThrow();
			pair=new ComparisonResult(before,after,MeleeModel.calculateVisible(118,118,before,target,items),MeleeModel.calculateVisible(118,118,after,target,items),
				target,Map.of(),"Synthetic N2 pair",MeleeModel.VERSION,catalog.version,List.of(),false,Map.of());
		}
		var choice=new NexOwnedResult.Choice(sample,after,pair,pair==null?"Special role":null,true,List.of());
		return new NexOwnedResult(summary,List.of(choice),NexMethod.PILOT,Map.of());
	}
	public static NexPlanComparison.Declaration declaration(Integer hp,Map<String,Integer> upgrades,Map<Integer,Integer> quantities)
	{ return new NexPlanComparison.Declaration(1,1000,25,SpecTimeline.RestoreOrder.BEFORE_EVENTS,hp,99,200,false,true,true,
		NexPlanComparison.Policy.FIRST_SPECIAL,List.of(0),List.of(),List.of(),upgrades,quantities); }
	private NexPlanComparison.Result evaluate(NexOwnedResult source,NexPlanComparison.Declaration declaration)
	{ return new NexPlanComparison(catalog).evaluate(source,0,0,declaration); }
	@Test public void allThreeModelsShareFrozenCompletePackagesAndKeepMarginalGainsSeparate()
	{
		var source=fixture(catalog,27690,0,Map.of(29801,1L),Map.of("inventory-slots-used",1),true);
		var result=evaluate(source,declaration(500,Map.of("cape",21295),Map.of(21295,1)));
		var a=result.comparison.actualToOwned.before; var o=result.comparison.actualToOwned.after; var u=result.comparison.ownedToUpgrade.after;
		assertNotNull(a.resourceGaps.toString(),a.plan); assertNotNull(o.resourceGaps.toString(),o.plan); assertNotNull(u.resourceGaps.toString(),u.plan);
		// Effective strength 156; base max 35 -> 38 -> 40; Voidwaker integer expectations 34.5 -> 38 -> 40.
		assertEquals(34.5,a.plan.expectedDamage,1e-12); assertEquals(38,o.plan.expectedDamage,1e-12); assertEquals(40,u.plan.expectedDamage,1e-12);
		assertEquals(3.5,result.comparison.actualToOwned.expectedDamage,1e-12); assertEquals(2,result.comparison.ownedToUpgrade.expectedDamage,1e-12);
		assertEquals(1,a.plan.resources.voidwakerSpecs); assertEquals(0,a.plan.resources.ordinaryAttacks); assertEquals(500,a.plan.resources.spent);
		assertSame(a.conditions,o.conditions); assertSame(o.conditions,u.conditions);
		assertEquals(Integer.valueOf(0),source.choices.get(0).actual.state.slots.get("cape"));
		String report=result.report(); assertFalse(report.contains("PRIVATE_SENTINEL")); assertFalse(report.contains("NEX@"));
		assertTrue(report.contains(SpecAttackPlan.VERSION)); assertTrue(report.contains(SpecTimeline.VERSION)); assertTrue(report.contains(NexPackageComparison.VERSION));
		assertTrue(report.contains("Captured-to-owned: expected damage change=3.500000")); assertTrue(report.contains("Owned-to-upgrade: expected damage change=2.000000"));
		assertThrows(UnsupportedOperationException.class,()->result.declaration.upgrades.put("ring",0));
	}
	@Test public void missingHpWithholdsDamageAndPreservesIndependentResourceResults()
	{
		var source=fixture(catalog,27690,0,Map.of(),Map.of("inventory-slots-used",0),false);
		var r=evaluate(source,declaration(null,Map.of(),Map.of()));
		assertNotNull(r.comparison.actualToOwned.before.plan); assertNull(r.comparison.actualToOwned.before.plan.expectedDamage);
		assertEquals(500,r.comparison.actualToOwned.before.plan.resources.spent); assertNull(r.comparison.actualToOwned.expectedDamage);
		assertFalse(r.comparison.actualToOwned.before.plan.gaps.isEmpty());
	}
	@Test public void unknownStartAndRestoreAreNotSilentlyFilledFromResourceDeltas()
	{
		var source=fixture(catalog,27690,0,Map.of(),Map.of("inventory-slots-used",0),false);
		var d=new NexPlanComparison.Declaration(5,null,null,null,500,99,200,false,true,false,NexPlanComparison.Policy.FIRST_SPECIAL,
			List.of(0),List.of(),List.of(),Map.of(),Map.of());
		var r=evaluate(source,d); assertNull(r.comparison.actualToOwned.before.plan); assertNull(r.comparison.actualToOwned.expectedDamage);
		assertTrue(r.comparison.actualToOwned.before.resourceGaps.toString().contains("Unknown"));
	}
	@Test public void finiteAmmoIsSharedAndRejectedSpecialsDoNotSpendEnergy()
	{
		var source=fixture(catalog,26374,21944,Map.of(),Map.of("worn-ammo-quantity",1,"inventory-slots-used",0),false);
		var d=new NexPlanComparison.Declaration(101,1000,25,SpecTimeline.RestoreOrder.BEFORE_EVENTS,500,99,200,false,true,true,
			NexPlanComparison.Policy.FUNDED_SPECIAL_WITH_ORDINARY_FALLBACK,List.of(0,100),List.of(),List.of(),Map.of(),Map.of());
		var r=evaluate(source,d); var plan=r.comparison.actualToOwned.before.plan;
		assertNotNull(r.comparison.actualToOwned.before.resourceGaps.toString(),plan);
		assertEquals(1,plan.resources.zaryteSpecs); assertEquals(750,plan.resources.spent); assertEquals(Integer.valueOf(1),plan.spentAmmo.get(21944));
		assertEquals(Integer.valueOf(0),plan.remainingAmmo.get(21944)); assertEquals(0,plan.resources.ordinaryAttacks);
		double hit=49416.0/(2*57567),proc=hit+(1-hit)*.06;
		assertEquals(proc*110,plan.expectedDamage,1e-12); assertEquals(proc*9,plan.expectedSelfDamage,1e-12);
	}
	@Test public void explicitImprovedDeathChargeEventsAndFallbackUseOneFiniteTimeline()
	{
		var source=fixture(catalog,27690,0,Map.of(),Map.of("inventory-slots-used",0),false);
		var d=new NexPlanComparison.Declaration(6,0,25,SpecTimeline.RestoreOrder.BEFORE_EVENTS,500,99,200,false,true,true,
			NexPlanComparison.Policy.FUNDED_SPECIAL_WITH_ORDINARY_FALLBACK,List.of(0,4),List.of(0),List.of(0,1),Map.of(),Map.of());
		var r=evaluate(source,d); var p=r.comparison.actualToOwned.before.plan;
		assertNotNull(r.comparison.actualToOwned.before.resourceGaps.toString(),p); assertEquals(2,p.resources.deathChargeProcs);
		assertEquals(300,p.resources.restored); assertEquals(0,p.resources.voidwakerSpecs); assertEquals(2,p.resources.ordinaryAttacks); assertEquals(0,p.resources.spent);
	}
	@Test public void unknownInventoryNeverBecomesUsableStockAndHypotheticalStockDoesNotLeakIntoOwned()
	{
		var source=fixture(catalog,27690,0,null,Map.of("inventory-slots-used",0),false);
		var r=evaluate(source,declaration(500,Map.of("cape",21295),Map.of(21295,1)));
		assertNull(r.comparison.actualToOwned.before.plan); assertNull(r.comparison.ownedToUpgrade.after.plan);
		assertFalse(r.comparison.actualToOwned.after.scenario.stock.equipment.known());
		var known=fixture(catalog,27690,0,Map.of(),Map.of("inventory-slots-used",0),false);
		r=evaluate(known,declaration(500,Map.of("cape",21295),Map.of(21295,1)));
		assertFalse(r.comparison.actualToOwned.after.scenario.stock.equipment.value().containsKey(21295));
		assertEquals(Integer.valueOf(1),r.comparison.ownedToUpgrade.after.scenario.stock.equipment.value().get(21295));
	}
	@Test public void exactVariantsHandsAndPlanningLocksBlockOnlyDependentHypotheticalPlan()
	{
		var source=fixture(catalog,27690,0,Map.of(),Map.of("inventory-slots-used",0),false);
		for(var edits:List.of(Map.of("shield",20463),Map.of("ring",0)))
		{
			Map<Integer,Integer> q=new HashMap<>(); edits.values().stream().filter(id->id>0).forEach(id->q.put(id,1));
			var r=evaluate(source,declaration(500,edits,q)); assertNotNull(r.comparison.actualToOwned.before.plan); assertNull(r.comparison.ownedToUpgrade.after.plan);
			assertNull(r.comparison.ownedToUpgrade.expectedDamage);
		}
		var locked=new NexOwnedResult(source.summary,source.choices,NexMethod.PILOT.lock("spec/cape",0),Map.of());
		var r=evaluate(locked,declaration(500,Map.of("cape",21295),Map.of(21295,1)));
		assertNotNull(r.comparison.actualToOwned.after.plan); assertNull(r.comparison.ownedToUpgrade.after.plan);
	}
	@Test public void unknownOpportunityListIsNotAnEmptyZeroDamagePlan()
	{
		assertThrows(IllegalArgumentException.class,()->new NexPlanComparison.Declaration(10,1000,25,SpecTimeline.RestoreOrder.BEFORE_EVENTS,
			500,99,200,false,true,true,NexPlanComparison.Policy.FIRST_SPECIAL,List.of(),List.of(),List.of(),Map.of(),Map.of()));
		assertThrows(IllegalArgumentException.class,()->new NexPlanComparison.Declaration(10,1000,25,SpecTimeline.RestoreOrder.BEFORE_EVENTS,
			500,99,200,false,true,true,NexPlanComparison.Policy.FIRST_SPECIAL,List.of(0,0),List.of(),List.of(),Map.of(),Map.of()));
	}
	@Test public void zeroAndNegativeMarginalGainsSurviveAndBankPackingIsNotInvented()
	{
		var source=fixture(catalog,27690,0,Map.of(29801,1L),Map.of("inventory-slots-used",1),true);
		var zero=evaluate(source,declaration(500,Map.of(),Map.of())); assertEquals(0,zero.comparison.ownedToUpgrade.expectedDamage,1e-12);
		var negative=evaluate(source,declaration(500,Map.of("neck",0),Map.of())); assertEquals(-3.5,negative.comparison.ownedToUpgrade.expectedDamage,1e-12);
		var original=source.choices.get(0);
		var bankChoice=new NexOwnedResult.Choice(original.actual,original.proposed,original.comparison,null,true,List.of(),true,List.of());
		var bank=new NexOwnedResult(source.summary,List.of(bankChoice),source.planning,source.scenarioUnlocks);
		var result=evaluate(bank,declaration(500,Map.of(),Map.of()));
		assertNotNull(result.comparison.actualToOwned.before.plan); assertNull(result.comparison.actualToOwned.after.plan);
		assertNull(result.comparison.actualToOwned.expectedDamage);
		assertTrue(result.comparison.actualToOwned.after.resourceGaps.toString().contains("packing"));
	}

	@Test public void reportAllowlistRejectsUnexpectedSlotNamesAndUnrelatedCapturedStock()
	{
		var original=fixture(catalog,27690,0,Map.of(999999,12345L),Map.of("inventory-slots-used",1),false);
		var c=original.choices.get(0); var before=c.actual.state; var slots=new HashMap<>(before.slots); slots.put("PRIVATE_SENTINEL",12345);
		var malformed=new CombatState(slots,before.realLevels,before.visibleLevels,before.prayers,before.style,before.specialEnergy,false,before.inventory,before.resources);
		var sample=new NexSummary.Sample(0,"SMOKE","NONE","NEX@PRIVATE_SENTINEL","spec candidate",-1,malformed);
		var choice=new NexOwnedResult.Choice(sample,null,null,"synthetic malformed slots",true,List.of());
		var source=new NexOwnedResult(original.summary,List.of(choice),original.planning,Map.of());
		var result=evaluate(source,declaration(500,Map.of(),Map.of()));
		assertNull(result.comparison.actualToOwned.before.plan); String report=result.report();
		assertFalse(report.contains("PRIVATE_SENTINEL")); assertFalse(report.contains("999999")); assertFalse(report.contains("12345"));
		assertTrue(report.length()<=24000);
	}

}
