package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.encounter.*;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.ironpath.advisor.P4aFixtures.*;

public class ZilyanaWornStateComparisonTest
{
	private KnowledgeCatalog catalog;
	@Before public void load() throws Exception { catalog=KnowledgeCatalog.load(new Gson()); }
	public static CombatState state(int weapon,int ammo,int visible)
	{
		Map<String,Integer> slots=new LinkedHashMap<>(); PoweredMagic.SLOTS.forEach(s->slots.put(s,0)); slots.put("weapon",weapon); slots.put("ammo",ammo);
		return new CombatState(slots,Map.of("RANGED",90),Map.of("RANGED",visible),Set.of("EAGLE_EYE"),1,1000,false);
	}
	public static EncounterSummary summary(CombatState state,Set<String> gaps)
	{ return new EncounterSummary(1,7,new EncounterContext(null,null,"zilyana","ordinary","normal",1,"solo",Map.of()),
		EncounterSummary.Outcome.COMPLETE,"synthetic-pilot", "Synthetic credited death",10,100,255,
		List.of(new EncounterSummary.Segment(10,100,state)),gaps); }
	public static AccountSnapshot owned()
	{ return account(Set.of(25867,20997,11212,21326,892,9144),99,99); }
	@Test public void otherTargetsPhasesAndModesCannotReuseTheZilyanaComparison()
	{
		EncounterSummary ordinary=summary(state(9185,9144,90),Set.of());
		for (EncounterContext context : List.of(
			new EncounterContext(null,null,"nex","ordinary","normal",3,"trio",Map.of()),
			new EncounterContext(null,null,"zilyana","shadow","normal",1,"solo",Map.of()),
			new EncounterContext(null,null,"zilyana","ordinary","unreviewed",1,"solo",Map.of())))
		{
			EncounterSummary unsupported=new EncounterSummary(ordinary.key,ordinary.generation,context,
				ordinary.outcome,ordinary.adapterVersion,ordinary.reason,ordinary.startTick,ordinary.endTick,
				ordinary.localDamage,ordinary.segments,ordinary.gaps);
			KillImpactResult result=new ZilyanaWornStateComparison(catalog).evaluate(unsupported,owned(),Preferences.EMPTY);
			assertTrue(result.options.isEmpty());
			assertTrue(result.gaps.stream().anyMatch(g -> g.contains("encounter-specific evaluation required")));
		}
	}
	@Test public void ownedTbowDoesNotRewriteActualWornCrossbowAndExactDrainedLevel()
	{
		KillImpactResult r=new ZilyanaWornStateComparison(catalog).evaluate(summary(state(9185,9144,73),Set.of()),owned(),R15Fixtures.absent(catalog));
		assertTrue(r.gaps.toString(),r.gaps.isEmpty());
		KillImpactResult.Option owned=r.options.stream().filter(o->o.kind.equals("Try owned setup")).findFirst().orElseThrow();
		assertEquals(Integer.valueOf(9185),owned.comparison.beforeSetup.slots.get("weapon"));
		assertEquals(Integer.valueOf(20997),owned.comparison.afterSetup.slots.get("weapon"));
		assertEquals(73,owned.comparison.before.visibleMagic); assertEquals(73,owned.comparison.after.visibleMagic);
		assertTrue(owned.comparison.dpsGain()>0);
		KillImpactResult.Option anguish=r.options.stream().filter(o->o.itemId==19547).findFirst().orElseThrow();
		assertEquals(Integer.valueOf(9185),anguish.comparison.afterSetup.slots.get("weapon"));
		assertFalse(anguish.comparison.available(ComparisonResult.Metric.EXPECTED_KILL_TIME));
	}
	@Test public void actualArrowAmmoHasSeparateRecurringUpgradeAndNoGainOwnedControl()
	{
		KillImpactResult r=new ZilyanaWornStateComparison(catalog).evaluate(summary(state(20997,892,90),Set.of()),owned(),R15Fixtures.absent(catalog));
		assertTrue(r.options.stream().anyMatch(o->o.kind.equals("Recurring ammunition") && o.itemId==11212 && o.comparison.dpsGain()>0));
		KillImpactResult same=new ZilyanaWornStateComparison(catalog).evaluate(summary(state(20997,11212,90),Set.of()),account(Set.of(20997,11212),99,99),R15Fixtures.absent(catalog));
		assertEquals(0,same.options.stream().filter(o->o.kind.equals("Try owned setup")).findFirst().orElseThrow().comparison.dpsGain(),1e-12);
	}
	@Test public void internalAmmoUnknownItemSpecialAndQuiverRemainExplicitGaps()
	{
		for(CombatState state:List.of(state(12926,0,99),state(999999,0,99)))
		{
			KillImpactResult r=new ZilyanaWornStateComparison(catalog).evaluate(summary(state,Set.of()),owned(),R15Fixtures.absent(catalog));
			assertFalse(r.gaps.isEmpty()); assertTrue(r.options.stream().allMatch(o->o.comparison==null));
		}
		KillImpactResult special=new ZilyanaWornStateComparison(catalog).evaluate(summary(state(20997,11212,99),Set.of("Special energy used; reduction/proc effects unverified")),owned(),Preferences.EMPTY);
		assertTrue(special.gaps.stream().anyMatch(g->g.contains("reduced target")));
		CombatState base=state(20997,11212,99); Map<String,Integer> slots=new HashMap<>(base.slots); slots.put("cape",28955);
		CombatState quiver=new CombatState(slots,base.realLevels,base.visibleLevels,base.prayers,1,1000,false);
		KillImpactResult r=new ZilyanaWornStateComparison(catalog).evaluate(summary(quiver,Set.of()),owned(),Preferences.EMPTY);
		assertTrue(r.gaps.stream().anyMatch(g->g.contains("quiver")));
	}
	@Test public void visibleLevelEntryPreservesUnboostedAndPotionRegressionSemantics()
	{
		var items=catalog.ranged.items.stream().collect(java.util.stream.Collectors.toMap(i->i.id,i->i));
		CombatSetup setup=CombatSetup.ranged(state(20997,11212,90).slots,"zilyana",Map.of());
		TargetProfile target=catalog.comparison.targets.stream().filter(t->t.id.equals("zilyana")).findFirst().orElseThrow();
		for(int level:new int[]{1,50,90,99})
		{
			assertEquals(RangedModel.calculate(level,setup,target,items).dps,RangedModel.calculateVisible(level,setup,target,items).dps,1e-12);
			CombatSetup potion=setup.edit("boost","true");
			assertEquals(RangedModel.calculate(level,potion,target,items).dps,RangedModel.calculateVisible(level+4+level/10,potion,target,items).dps,1e-12);
		}
	}
}
