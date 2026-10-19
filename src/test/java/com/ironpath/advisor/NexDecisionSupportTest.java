package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.nex.*;
import java.util.*;
import org.junit.Test;
import static com.ironpath.nex.NexSupplies.Kind.*;
import static org.junit.Assert.*;

/** Hand cases for finite supply effects, scoped costs and target transforms; not gameplay acceptance. */
public class NexDecisionSupportTest
{
	private final KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson());
	public NexDecisionSupportTest() throws Exception { }
	private NexOwnedResult source(Map<Integer,Long> inventory)
	{ return NexPlanComparisonTest.fixture(catalog,27690,0,inventory,Map.of("inventory-slots-used",inventory.size()),false); }
	private NexPlanComparison.Declaration declaration(List<NexSupplies.Use> supplies,Map<Integer,Long> costs)
	{
		return new NexPlanComparison.Declaration(15,1000,25,SpecTimeline.RestoreOrder.BEFORE_EVENTS,500,99,200,false,true,true,
			NexPlanComparison.Policy.ORDINARY,List.of(0,4,8,12),List.of(),List.of(),Map.of(),Map.of(),supplies,costs);
	}
	private NexPlanComparison.Result evaluate(NexOwnedResult source,NexPlanComparison.Declaration declaration)
	{ return new NexPlanComparison(catalog).evaluate(source,0,0,declaration); }
	private NexOwnedResult context(NexOwnedResult before,String target,String phase,String window)
	{
		var old=before.choices.get(0).actual;
		var sample=new NexSummary.Sample(0,phase,window,target+"@PRIVATE_TARGET",old.role,-1,old.state);
		var original=before.summary;
		var summary=new NexSummary(original.key,0,15,16,15,0,original.outcome,"","","",List.of(sample),Map.of(),Set.of(),original.signals);
		return new NexOwnedResult(summary,List.of(new NexOwnedResult.Choice(sample,null,null,"Declared spec state",true,List.of())),before.planning,before.scenarioUnlocks);
	}
	@Test public void comboFoodShiftsOutstandingCooldownAndConsumesOnlyFiniteDeclaredUnits()
	{
		var source=source(Map.of(385,1L,3144,1L));
		var supply=NexSupplies.ordered(Map.of(SHARK,List.of(1),KARAMBWAN,List.of(1)),15);
		var before=evaluate(source,declaration(List.of(),Map.of())).comparison.actualToOwned.before.plan;
		var result=evaluate(source,declaration(supply,Map.of(385,100L,3144,50L)));
		var after=result.comparison.actualToOwned.before.plan;
		assertNotNull(after); assertEquals(4,before.resources.ordinaryAttacks);
		// Attack 0 sets ready=4; shark +3 and karambwan +2 shift ready to 9. Offsets 4/8 are rejected.
		assertEquals(2,after.resources.ordinaryAttacks); assertEquals(2,after.hits.size());
		assertEquals(0,after.resources.spent); assertTrue(after.spentAmmo.isEmpty());
		var cost=NexPlanSupport.cost(result.comparison.actualToOwned.before,result.declaration.unitCosts);
		assertEquals(Long.valueOf(150),cost.gp); assertEquals(Map.of(385,1,3144,1),cost.units);
		assertEquals(Map.of(385,1L,3144,1L),source.choices.get(0).actual.state.inventory);
	}
	@Test public void idleFoodDoesNotInventLostAttacksAndPotionDoesNotDelayAnAttack()
	{
		var source=source(Map.of(385,1L,6685,1L));
		var uses=NexSupplies.ordered(Map.of(SHARK,List.of(0),BREW,List.of(0)),15);
		var result=evaluate(source,declaration(uses,Map.of()));
		assertNotNull(result.comparison.actualToOwned.before.plan);
		assertEquals(4,result.comparison.actualToOwned.before.plan.resources.ordinaryAttacks);
		assertEquals(99,result.declaration.playerHp.intValue());
		assertNull(NexPlanSupport.cost(result.comparison.actualToOwned.before,Map.of()).gp);
		assertEquals(Set.of(385,6691),NexPlanSupport.cost(result.comparison.actualToOwned.before,Map.of()).missingPrices);
	}
	@Test public void brewRestoreAndReboostUseCurrentLevelsRatherThanRestoringPreviousBoosts()
	{
		Map<String,Integer> base=Map.of("ATTACK",99,"STRENGTH",99,"DEFENCE",99,"RANGED",99,"MAGIC",99);
		Map<String,Integer> initial=Map.of("ATTACK",118,"STRENGTH",118,"DEFENCE",118,"RANGED",112,"MAGIC",99);
		var uses=NexSupplies.ordered(Map.of(BREW,List.of(0,3,6),RESTORE,List.of(9),SUPER_COMBAT,List.of(12)),15);
		assertEquals(Integer.valueOf(105),NexSupplies.levels(base,initial,uses,0).get("STRENGTH"));
		assertEquals(Integer.valueOf(82),NexSupplies.levels(base,initial,uses,6).get("STRENGTH"));
		assertEquals(Integer.valueOf(99),NexSupplies.levels(base,initial,uses,9).get("STRENGTH"));
		assertEquals(Integer.valueOf(118),NexSupplies.levels(base,initial,uses,12).get("STRENGTH"));
		assertEquals(Integer.valueOf(99),NexSupplies.levels(base,initial,uses,12).get("RANGED"));
		assertEquals(Integer.valueOf(118),initial.get("STRENGTH"));
		var source=source(Map.of(6685,1L,3024,1L,12695,1L));
		var result=evaluate(source,declaration(uses,Map.of()));
		var e=result.comparison.actualToOwned.before;
		assertNotNull(e.resourceGaps.toString(),e.plan);
		Map<Integer,Integer> maxAt=new TreeMap<>();
		e.launches.forEach((index,launch)->maxAt.put(e.conditions.steps.get(index).tick,launch.chargedMetrics.maxHit));
		assertTrue(maxAt.get(0)>maxAt.get(8));
		assertEquals(Integer.valueOf(35),maxAt.get(12));
	}
	@Test public void supplyCooldownsUnknownInventoryAndExhaustedDosesCannotBecomeAValidPlan()
	{
		assertThrows(IllegalArgumentException.class,()->NexSupplies.ordered(Map.of(BREW,List.of(0),RESTORE,List.of(1)),15));
		assertThrows(IllegalArgumentException.class,()->NexSupplies.ordered(Map.of(SHARK,List.of(0,1)),15));
		assertThrows(IllegalArgumentException.class,()->NexSupplies.ordered(Map.of(KARAMBWAN,List.of(0,1)),15));
		var uses=NexSupplies.ordered(Map.of(BREW,List.of(0,3)),15);
		var insufficient=evaluate(source(Map.of(6691,1L)),declaration(uses,Map.of())).comparison.actualToOwned.before;
		assertNull(insufficient.plan); assertTrue(insufficient.resourceGaps.toString().contains("Insufficient usable"));
		assertEquals(Integer.valueOf(7),NexSupplies.quantities(Map.of(6685,1L,6687,1L)).get(BREW));
		assertThrows(IllegalArgumentException.class,()->NexSupplies.stock(uses,null));
	}
	@Test public void minionsUseTheirOwnDefencesAndAccurateNonMagicFlatArmourBeforeHpAndCaps()
	{
		for(var entry:Map.of("FUMUS","SMOKE","UMBRA","SHADOW","CRUOR","BLOOD","GLACIES","ICE").entrySet())
		{
			var source=context(source(Map.of()),entry.getKey(),entry.getValue(),"MINION_"+entry.getKey());
			var result=evaluate(source,NexPlanComparisonTest.declaration(500,Map.of(),Map.of()));
			var evaluation=result.comparison.actualToOwned.before;
			assertNotNull(evaluation.resourceGaps.toString(),evaluation.plan); assertNotNull(evaluation.plan.expectedDamage);
			assertEquals((200L+9)*(entry.getKey().equals("CRUOR")?25+64:100+64),evaluation.launches.get(0).chargedMetrics.defenceRoll);
			assertEquals(34.5,evaluation.plan.expectedDamage,1e-12); // VW's Magic-type spec bypasses non-Magic armour.
			assertTrue(evaluation.versions.containsKey("n3-minion-reference"));
			assertFalse(result.report().contains("PRIVATE_TARGET"));
		}
		var limit=new SpecAttackModel.Limit(500,200,-2);
		// Accurate zero-max branch gets +2, a miss stays 0.
		assertEquals(1,SpecAttackModel.ordinary(0,0,.5,true,limit,"test").expectedDamage,1e-12);
		assertEquals(0,SpecAttackModel.ordinary(0,50,0,true,limit,"test").expectedDamage,1e-12);
		assertEquals(1,SpecAttackModel.ordinary(0,50,1,true,new SpecAttackModel.Limit(1,200,-2),"test").maximum);
		assertEquals(3,SpecAttackModel.ordinary(0,50,1,true,new SpecAttackModel.Limit(500,3,-2),"test").maximum);
	}
	@Test public void unknownWindowsWrongMinionPhasesAndZarosKeepDamageUnavailableWithoutLosingEnergy()
	{
		for(var source:List.of(context(source(Map.of()),"NEX","SMOKE","UNKNOWN"),
			context(source(Map.of()),"FUMUS","ICE","MINION_FUMUS"),context(source(Map.of()),"NEX","ZAROS","NONE")))
		{
			var result=evaluate(source,NexPlanComparisonTest.declaration(500,Map.of(),Map.of()));
			var plan=result.comparison.actualToOwned.before.plan;
			assertNotNull(plan); assertNull(plan.expectedDamage); assertEquals(500,plan.resources.spent);
			assertNull(result.comparison.actualToOwned.expectedDamage);
		}
	}
	@Test public void acquisitionUsesExactCatalogueRoutesWithoutInferringCompletionAndCostsRemainScoped()
	{
		var routes=NexPlanSupport.routes(catalog,Map.of("weapon",26374,"cape",28949,"ammo",21944));
		assertEquals(3,routes.size());
		assertTrue(routes.stream().filter(r->r.item==26374).findFirst().orElseThrow().stages.contains("250 nihil shards"));
		assertNull(routes.stream().filter(r->r.item==21944).findFirst().orElseThrow().work);
		var source=NexPlanComparisonTest.fixture(catalog,26374,21944,Map.of(),Map.of("inventory-slots-used",0,"worn-ammo-quantity",1),false);
		var result=evaluate(source,NexPlanComparisonTest.declaration(500,Map.of(),Map.of()));
		var evaluation=result.comparison.actualToOwned.before;
		assertNull(NexPlanSupport.cost(evaluation,Map.of()).gp);
		assertEquals(Long.valueOf(500),NexPlanSupport.cost(evaluation,Map.of(21944,500L)).gp);
		assertEquals(Long.valueOf(0),NexPlanSupport.cost(evaluation,Map.of(21944,0L)).gp);
		assertThrows(IllegalArgumentException.class,()->NexPlanSupport.cost(evaluation,Map.of(21944,-1L)));
		assertTrue(result.report().contains("automatic capture-to-recommendation gaps="));
		assertTrue(result.report().contains("Other gear upkeep, Death Charge runes"));
	}
}
