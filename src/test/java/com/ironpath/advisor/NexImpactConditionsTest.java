package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.nex.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.ironpath.nex.NexPlanComparison.*;

/** Runtime joins use independently supplied impact conditions, never damage-fed HP or carry-forward. */
public class NexImpactConditionsTest
{
	private final KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson());
	public NexImpactConditionsTest() throws Exception { }
	private Declaration declaration(List<Integer> ticks,Map<Integer,Impact> impacts)
	{
		// Deliberately conflicting fixed inputs must have no effect in per-opportunity mode.
		return new Declaration(10,1000,25,SpecTimeline.RestoreOrder.BEFORE_EVENTS,1,1,1,false,false,true,
			Policy.FUNDED_SPECIAL_WITH_ORDINARY_FALLBACK,ticks,List.of(),List.of(),Map.of(),Map.of(),List.of(),Map.of(),
			new Zaros(NexTargetState.Overhead.DEFLECT_MELEE,1000,1000,true),ConditionMode.PER_OPPORTUNITY,impacts);
	}
	private Impact impact(Integer hp,int defence,NexTargetState.Overhead overhead)
	{ return new Impact(hp,99,200,true,new Zaros(overhead,defence,null,true)); }
	private NexPlanComparison.Result evaluate(NexOwnedResult source,Declaration d)
	{ return new NexPlanComparison(catalog).evaluate(source,0,0,d); }
	@Test public void independentHpAndStatsReachEveryWholePackageAndSameTickFallback()
	{
		var rows=new HashMap<Integer,Impact>(); rows.put(0,impact(500,250,NexTargetState.Overhead.SOUL_SPLIT)); rows.put(4,impact(10,300,NexTargetState.Overhead.NONE));
		var d=declaration(List.of(0,4),rows); rows.clear();
		var r=evaluate(NexPlanComparisonTest.zarosFixture(catalog),d);
		for(var e:List.of(r.comparison.actualToOwned.before,r.comparison.actualToOwned.after,r.comparison.ownedToUpgrade.after))
		{
			assertNotNull(e.resourceGaps.toString(),e.plan);
			// Two VW rolls: uniform 17..52 has mean 34.5, then independently supplied 10 HP caps every outcome.
			assertEquals(44.5,e.plan.expectedDamage,1e-12); assertEquals(1000,e.plan.resources.spent);
			assertEquals(2,e.plan.resources.voidwakerSpecs); assertEquals(0,e.plan.resources.ordinaryAttacks);
			// This fixture's style button selects slash: Nex slash defence 140 plus 64.
			assertEquals(259L*204,e.launches.get(0).chargedMetrics.defenceRoll); assertEquals(309L*204,e.launches.get(2).chargedMetrics.defenceRoll);
			assertEquals(10,e.conditions.steps.get(2).targetHp.value().intValue());
			assertEquals(e.conditions.steps.get(2).targetHp.value(),e.conditions.steps.get(3).targetHp.value());
			assertSame(e.conditions.steps.get(2).context.value(),e.conditions.steps.get(3).context.value());
		}
		assertEquals(0,r.comparison.actualToOwned.expectedDamage,0); assertEquals(0,r.comparison.ownedToUpgrade.expectedDamage,0);
		assertEquals(Integer.valueOf(260),catalog.comparison.targets.stream().filter(t->t.id.equals("nex-base")).findFirst().orElseThrow().defenceLevel);
		assertThrows(UnsupportedOperationException.class,()->d.impacts.clear());
		String report=r.report(); assertTrue(report.contains("schema 5")); assertTrue(report.contains("Condition mode=PER_OPPORTUNITY"));
		assertTrue(report.contains("Declared attack tick=4; target HP=10; player HP=99; hard cap=200; ruby allowed=true; impact confirmed=true; overhead=NONE; target Defence=300; target Magic=null"));
		assertFalse(report.contains("Declared Zaros fixed window=")); assertFalse(report.contains("PRIVATE_SENTINEL"));
	}
	@Test public void absentOrPartialRowsCannotBorrowFixedOrEarlierValuesButRejectedOpportunitiesDoNotCreateDamageGaps()
	{
		var source=NexPlanComparisonTest.zarosFixture(catalog);
		var first=impact(500,250,NexTargetState.Overhead.SOUL_SPLIT);
		for(Map<Integer,Impact> rows:List.of(Map.of(0,first),Map.of(0,first,4,impact(null,300,NexTargetState.Overhead.NONE))))
		{
			var e=evaluate(source,declaration(List.of(0,4),rows)).comparison.actualToOwned.before;
			assertNotNull(e.plan); assertNull(e.plan.expectedDamage); assertEquals(1000,e.plan.resources.spent); assertFalse(e.plan.gaps.isEmpty());
		}
		var e=evaluate(source,declaration(List.of(0,1,4),Map.of(0,first,4,impact(10,300,NexTargetState.Overhead.NONE)))).comparison.actualToOwned.before;
		assertEquals(44.5,e.plan.expectedDamage,1e-12); assertEquals(2,e.plan.hits.size());
		assertFalse(e.conditions.steps.get(2).targetHp.known()); // tick 1 is rejected by the weapon delay.
	}
	@Test public void oneDeflectionOrUnconfirmedImpactWithholdsOnlyDependentDamage()
	{
		for(var z:List.of(new Zaros(NexTargetState.Overhead.DEFLECT_MELEE,250,null,true),new Zaros(NexTargetState.Overhead.NONE,250,null,false)))
		{
			var e=evaluate(NexPlanComparisonTest.zarosFixture(catalog),declaration(List.of(0,4),Map.of(0,impact(500,250,NexTargetState.Overhead.SOUL_SPLIT),4,new Impact(500,99,200,true,z)))).comparison.actualToOwned.before;
			assertNotNull(e.plan); assertNull(e.plan.expectedDamage); assertEquals(1000,e.plan.resources.spent); assertEquals(2,e.plan.hits.size());
			assertNotNull(e.plan.hits.get(0).damage); assertNull(e.plan.hits.get(1).damage);
		}
	}
	@Test public void rubyEligibilityAndPlayerHpVaryWithoutChangingSharedStock()
	{
		var source=NexPlanComparisonTest.fixture(catalog,26374,21944,Map.of(),Map.of("inventory-slots-used",0,"worn-ammo-quantity",2),false);
		var rows=Map.of(0,new Impact(500,99,200,true,Zaros.UNKNOWN),5,new Impact(100,9,200,true,Zaros.UNKNOWN));
		var e=evaluate(source,declaration(List.of(0,5),rows)).comparison.actualToOwned.before;
		assertNotNull(e.plan); assertNotNull(e.plan.expectedDamage);
		assertTrue(e.plan.hits.get(0).damage.procChance>0); assertEquals(0,e.plan.hits.get(1).damage.procChance,0);
		assertTrue(e.plan.hits.get(0).damage.expectedSelfDamage>0); assertEquals(0,e.plan.hits.get(1).damage.expectedSelfDamage,0);
		assertEquals(Integer.valueOf(2),e.plan.spentAmmo.get(21944)); assertEquals(750,e.plan.resources.spent); assertEquals(1,e.plan.resources.ordinaryAttacks);
	}
	@Test public void keysAndNumbersAreBoundedAndAFullReportDisclosesTruncation()
	{
		assertThrows(IllegalArgumentException.class,()->declaration(List.of(0),Map.of(1,Impact.UNKNOWN)));
		assertThrows(IllegalArgumentException.class,()->new Impact(0,99,200,true,Zaros.UNKNOWN));
		assertThrows(IllegalArgumentException.class,()->new Impact(500,256,200,true,Zaros.UNKNOWN));
		List<Integer> ticks=new ArrayList<>(); Map<Integer,Impact> rows=new TreeMap<>();
		for(int t=0;t<128;t++) { ticks.add(t); rows.put(t,impact(500,250,NexTargetState.Overhead.SOUL_SPLIT)); }
		var d=new Declaration(128,1000,25,SpecTimeline.RestoreOrder.BEFORE_EVENTS,null,null,null,false,null,true,Policy.ORDINARY,
			ticks,List.of(),List.of(),Map.of(),Map.of(),List.of(),Map.of(),Zaros.UNKNOWN,ConditionMode.PER_OPPORTUNITY,rows);
		String report=evaluate(NexPlanComparisonTest.zarosFixture(catalog),d).report();
		assertTrue(report.length()<=24000); assertTrue(report.endsWith("[Fields omitted by size limit]"));
	}
}
