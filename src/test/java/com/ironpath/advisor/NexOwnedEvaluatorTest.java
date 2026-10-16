package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.encounter.CombatState;
import com.ironpath.model.*;
import com.ironpath.nex.NexSummary;
import com.ironpath.preferences.*;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.NexOwnedFixtures.*;
import static com.ironpath.advisor.ComparisonResult.Metric.*;
import static org.junit.Assert.*;

public class NexOwnedEvaluatorTest
{
	private KnowledgeCatalog catalog; private NexOwnedEvaluator evaluator;
	@Before public void load() throws Exception { catalog=KnowledgeCatalog.load(new Gson()); evaluator=new NexOwnedEvaluator(catalog); }
	@Test public void newlyReviewedEnchantedQuiverBoltsCannotBecomeOrdinaryOnlyN2Damage()
	{
		var slots=new HashMap<>(ranged()); slots.put("weapon",26374);
		var original=state(slots,Map.of()); var resources=new HashMap<>(original.resources);
		resources.put("quiver-ammo-id-raw",21944);
		var captured=new CombatState(original.slots,original.realLevels,original.visibleLevels,original.prayers,
			original.style,1000,false,original.inventory,resources);
		var result=evaluator.evaluate(summary(List.of(sample("ranged",captured))),account(null),Preferences.EMPTY);
		assertNull(result.choices.get(0).comparison);
		assertTrue(result.choices.get(0).gap.contains("Enchanted bolt"));
	}
	@Test public void exactLockedMeleeFormsSupportAnOrdinaryComparisonWithoutChangingCapturedIds()
	{
		Map<String,Integer> slots=new LinkedHashMap<>(melee()); slots.put("cape",24224); slots.put("shield",24143);
		var actual=sample("melee",state(slots,Map.of()));
		var result=evaluator.evaluate(summary(List.of(actual)),account(null),Preferences.EMPTY);
		var choice=result.choices.get(0); assertNotNull(choice.gap,choice.comparison); assertNotNull(choice.comparison.before);
		assertEquals(0,choice.comparison.dpsGain(),1e-12);
		assertEquals(Integer.valueOf(24224),choice.comparison.beforeSetup.slots.get("cape"));
		assertEquals(Integer.valueOf(24143),choice.proposed.slots.get("shield"));
		assertEquals(actual.state.slots,choice.proposed.slots);
	}
	@Test public void lockedMeleeFormsRetainTheReviewedBonusesAndDoNotAuthorizeDamagedForms()
	{
		var items=catalog.melee.items.stream().collect(java.util.stream.Collectors.toMap(i->i.id,i->i));
		assertEquals(4,items.get(24224).stab.intValue()); assertEquals(4,items.get(24224).slash.intValue());
		assertEquals(4,items.get(24224).crush.intValue()); assertEquals(8,items.get(24224).strength.intValue());
		assertEquals(25,items.get(24143).stab.intValue()); assertEquals(24,items.get(24143).slash.intValue());
		assertEquals(23,items.get(24143).crush.intValue()); assertEquals(6,items.get(24143).strength.intValue());
		for(int broken:new int[]{21287,33500,33502,20463})
		{
			Map<String,Integer> slots=new LinkedHashMap<>(melee()); slots.put("cape",21295);
			slots.put(broken==20463?"shield":"cape",broken);
			var result=evaluator.evaluate(summary(List.of(sample("melee",state(slots,Map.of())))),account(null),Preferences.EMPTY);
			assertFalse(result.choices.get(0).supported()); assertTrue(result.choices.get(0).gap.contains("Unreviewed melee contribution"));
		}
	}
	@Test public void normalMeleeItemsDoNotCreateOwnershipOfTheirLockedForms()
	{
		Map<String,Integer> slots=new LinkedHashMap<>(melee()); slots.put("cape",21295);
		var actual=sample("melee",state(slots,Map.of()));
		Map<String,Integer> locked=new LinkedHashMap<>(slots); locked.put("cape",24224); locked.put("shield",24143);
		var pair=evaluator.compare(actual,evaluator.setup(actual,locked),account(null),Preferences.EMPTY);
		assertNull(pair.before); assertTrue(pair.reason.contains("not carried"));
	}
	@Test public void repeatedUnsupportedStatesHaveGroupedReasonsWithoutLosingTheStates()
	{
		var actual=sample("ranged",state(ranged(),Map.of()));
		List<NexSummary.Sample> samples=new ArrayList<>(); samples.add(actual);
		int tick=actual.tick+1;
		for(int energy:new int[]{0,500,1000})
		{
			var s=actual.state;
			var spec=new CombatState(s.slots,s.realLevels,s.visibleLevels,s.prayers,s.style,energy,true,s.inventory,s.resources);
			samples.add(new NexSummary.Sample(tick++,actual.phase,actual.window,actual.target,"spec candidate",-1,spec));
		}
		var result=evaluator.evaluate(summary(samples),account(null),Preferences.EMPTY);
		assertEquals(4,result.choices.size()); assertEquals(1,result.choices.stream().filter(c->c.supported()).count());
		assertEquals(1,result.gaps.size()); assertEquals(Integer.valueOf(3),result.gaps.values().iterator().next());
		assertThrows(UnsupportedOperationException.class,()->result.gaps.clear());
	}
	@Test public void preSmokeHistoryIsRetainedButExcludedFromOwnedFightComparisons()
	{
		var actual=sample("ranged",state(ranged(),Map.of())); var base=summary(List.of(actual));
		var before=new NexSummary.Sample(-10,"UNKNOWN","UNKNOWN",null,"unclassified",-1,actual.state);
		var captured=new NexSummary(base.key,base.startTick,base.endTick,base.sampledTicks,base.expectedTicks,base.unclassifiedHits,
			base.outcome,base.reason,base.cohort,base.method,base.timeline,base.damage,base.gaps,base.signals,List.of(before));
		var result=evaluator.evaluate(captured,account(null),Preferences.EMPTY);
		assertEquals(1,result.choices.size()); assertSame(actual,result.choices.get(0).actual);
		assertEquals(1,result.summary.beforeStart.size());
	}
	@Test public void reportedCompleteSwitchesKeepBlessingProjectileDefenderAndZeroIdentity()
	{
		var r=sample("ranged",state(ranged(),Map.of(26219,1L,12954,1L)));
		var m=sample("melee",state(melee(),Map.of(20997,1L)));
		var result=evaluator.evaluate(summary(List.of(r,m)),account(null),Preferences.EMPTY);
		assertEquals(2,result.choices.size());
		for(var c:result.choices)
		{
			assertNotNull(c.gap,c.comparison); assertEquals(0,c.comparison.dpsGain(),1e-12);
			assertEquals(c.actual.state.slots,c.comparison.beforeSetup.slots);
			assertEquals(Integer.valueOf(20235),c.proposed.slots.get("ammo"));
			assertFalse(c.comparison.available(EXPECTED_KILL_TIME)); assertFalse(c.comparison.available(TRIP_PERFORMANCE));
		}
		assertEquals(21326,result.choices.get(0).comparison.beforeSetup.loadedAmmo);
		assertTrue(result.choices.get(0).comparison.assumptions.toString().contains("r2-amethyst-arrow@"));
		assertEquals(Integer.valueOf(0),result.choices.get(0).actual.state.slots.get("shield"));
		assertEquals(Integer.valueOf(12954),result.choices.get(1).actual.state.slots.get("shield"));
		// Independent integer hand cases: ranged attack 200 + quiver 10, strength 35 + arrows 55 + quiver 1.
		var ranged=result.choices.get(0).comparison.before;
		assertEquals(210,ranged.attackBonus); assertEquals(91,ranged.damagePercent,0);
		assertEquals(72,ranged.maxHit); assertEquals(53303,ranged.attackRoll);
		assertEquals(53303.0/(2*68327),ranged.hitChance,1e-12);
		assertEquals((36+1.0/73)*ranged.hitChance/3.0,ranged.dps,1e-12);
		assertEquals(5,ranged.attackTicks); assertEquals(269L*254,ranged.defenceRoll);
		assertEquals(5,result.choices.get(1).comparison.before.attackTicks);
	}
	@Test public void dragonArrowsAndBloodFuryExposePositiveAndNegativeWholePackagePairs()
	{
		var r=sample("ranged",state(ranged(),Map.of(11212,100L)));
		var m=sample("melee",state(melee(),Map.of(24780,1L)));
		var result=evaluator.evaluate(summary(List.of(r,m)),account(null),Preferences.EMPTY);
		var arrow=result.choices.get(0).comparison; assertTrue(arrow.reason,arrow.dpsGain()>0);
		assertEquals(Integer.valueOf(11212),arrow.afterSetup.slots.get("ammo"));
		assertEquals(Integer.valueOf(20235),arrow.beforeSetup.slots.get("ammo"));
		var blood=result.choices.get(1).alternatives.stream().filter(p->p.afterSetup.slots.get("neck")==24780).findFirst().orElseThrow();
		assertTrue(blood.dpsGain()<0); assertFalse(blood.available(TRIP_PERFORMANCE));
		assertEquals(Integer.valueOf(29801),result.choices.get(1).proposed.slots.get("neck"));
	}
	@Test public void unrelatedScorchingDoesNotSuppressSupportedPairAndSearchAdmitsItsCoverageGap()
	{
		var s=sample("ranged",state(ranged(),Map.of(29591,1L,11212,20L)));
		var slots=new HashMap<>(ranged()); slots.put("ammo",11212);
		var pair=evaluator.compare(s,evaluator.setup(s,slots),account(null),Preferences.EMPTY);
		assertTrue(pair.reason,pair.available(THEORETICAL_DPS)); assertTrue(pair.dpsGain()>0);
		var c=evaluator.evaluate(summary(List.of(s)),account(null),Preferences.EMPTY).choices.get(0);
		assertTrue(c.coverage.toString().contains("not modelled")); assertNotNull(c.comparison);
	}
	@Test public void locksAreConstraintsAndNeverRewriteActualOrCreateOwnership()
	{
		var s=sample("melee",state(melee(),Map.of(24780,1L)));
		Preferences locked=Preferences.EMPTY.nex(NexMethod.PILOT.lock("melee/neck",24780));
		var c=evaluator.evaluate(summary(List.of(s)),account(null),locked).choices.get(0);
		assertTrue(c.comparison.dpsGain()<0); assertEquals(Integer.valueOf(29801),c.actual.state.slots.get("neck"));
		Preferences absent=Preferences.EMPTY.nex(NexMethod.PILOT.lock("melee/shield",22322));
		assertNull(evaluator.evaluate(summary(List.of(s)),account(null),absent).choices.get(0).comparison);
		assertThrows(IllegalArgumentException.class,()->NexMethod.PILOT.lock("melee/ring",6737));
	}
	@Test public void bankGearHasSeparatePreparationScopeAndCannotBecomeCarriedAvailability()
	{
		var s=sample("ranged",state(ranged(),Map.of()));
		var result=evaluator.evaluate(summary(List.of(s)),account(Set.of(11212)),Preferences.EMPTY);
		assertEquals(2,result.choices.size()); assertEquals(0,result.choices.get(0).comparison.dpsGain(),1e-12);
		assertTrue(result.choices.get(1).bankPreparation); assertTrue(result.choices.get(1).comparison.dpsGain()>0);
		assertTrue(result.choices.get(1).coverage.toString().contains("packing"));
		var slots=new HashMap<>(ranged()); slots.put("ammo",11212);
		assertFalse(evaluator.compare(s,evaluator.setup(s,slots),account(Set.of(11212)),Preferences.EMPTY).available(THEORETICAL_DPS));
	}
	@Test public void unknownAmmoChargesAndSlotsWithholdOnlyAffectedState()
	{
		var known=state(ranged(),Map.of()); var resources=new HashMap<>(known.resources); resources.remove("quiver-ammo-id-raw");
		var unread=new CombatState(known.slots,known.realLevels,known.visibleLevels,known.prayers,known.style,1000,false,Map.of(),resources);
		var m=sample("melee",state(melee(),Map.of()));
		var result=evaluator.evaluate(summary(List.of(sample("ranged",unread),m)),account(null),Preferences.EMPTY);
		assertNull(result.choices.get(0).comparison); assertNotNull(result.choices.get(1).comparison);
		resources=new HashMap<>(known.resources); resources.put("quiver-splinters-raw",0);
		unread=new CombatState(known.slots,known.realLevels,known.visibleLevels,known.prayers,known.style,1000,false,Map.of(),resources);
		assertNull(evaluator.evaluate(summary(List.of(sample("ranged",unread))),account(null),Preferences.EMPTY).choices.get(0).comparison);
		var slots=new HashMap<>(melee()); slots.put("head",-1);
		assertNull(evaluator.evaluate(summary(List.of(sample("melee",state(slots,Map.of())))),account(null),Preferences.EMPTY).choices.get(0).comparison);
	}
	@Test public void twoHandedPackagesAndSetsAreEvaluatedTogetherNotBySummedItemStats()
	{
		var s=sample("melee",state(melee(),Map.of(22325,1L)));
		var result=evaluator.evaluate(summary(List.of(s)),account(null),Preferences.EMPTY).choices.get(0);
		var scythe=result.alternatives.stream().filter(p->p.afterSetup.slots.get("weapon")==22325).findFirst().orElseThrow();
		assertEquals(Integer.valueOf(0),scythe.afterSetup.slots.get("shield"));
		var bad=new HashMap<>(scythe.afterSetup.slots); bad.put("shield",12954);
		assertFalse(evaluator.compare(s,evaluator.setup(s,bad),account(null),Preferences.EMPTY).available(THEORETICAL_DPS));
		var r=sample("ranged",state(ranged(),Map.of(25867,1L,23971,1L,23979,1L)));
		var bowfa=evaluator.evaluate(summary(List.of(r)),account(null),Preferences.EMPTY).choices.get(0);
		assertTrue(bowfa.comparison.reason,bowfa.comparison.available(THEORETICAL_DPS));
		assertEquals(r.state.slots,bowfa.comparison.beforeSetup.slots);
	}
	@Test public void allStatesAndRolesRemainSeparateAndSpecialWindowsAreNotOrdinaryDps()
	{
		var r=sample("ranged",state(ranged(),Map.of())); var m=sample("melee",state(melee(),Map.of()));
		var spec=new NexSummary.Sample(30,"ICE","UNKNOWN","NEX@1:1:1","spec candidate",-1,m.state);
		var minion=new NexSummary.Sample(40,"ICE","MINION_GLACIES","GLACIES@1:2:2","ranged",-1,r.state);
		var zaros=new NexSummary.Sample(50,"ZAROS","UNKNOWN","NEX@1:1:1","ranged",-1,r.state);
		var result=evaluator.evaluate(summary(List.of(r,m,spec,minion,zaros)),account(null),Preferences.EMPTY);
		assertEquals(5,result.choices.size()); assertNull(result.choices.get(2).comparison); assertNull(result.choices.get(3).comparison); assertNull(result.choices.get(4).comparison);
		assertEquals("spec candidate",result.choices.get(2).actual.role);
	}
	@Test public void suppliedChargedAndBlessedQuiversHaveEqualOrdinaryOffenceWithoutAnEnergyUplift()
	{
		var s=sample("ranged",state(ranged(),Map.of(28955,1L)));
		var slots=new HashMap<>(ranged()); slots.put("cape",28955);
		var c=evaluator.compare(s,evaluator.setup(s,slots),account(null),Preferences.EMPTY);
		assertTrue(c.reason,c.available(THEORETICAL_DPS)); assertEquals(0,c.dpsGain(),0);
		assertEquals(21326,c.afterSetup.loadedAmmo); assertEquals(25975,(int)c.afterSetup.slots.get("ring"));
	}
	@Test public void meleeVisibleLevelsUseExactDecayRatherThanInferringAFullPotion()
	{
		var s=sample("melee",state(melee(),Map.of()));
		var visible=new HashMap<>(s.state.visibleLevels); visible.put("ATTACK",113); visible.put("STRENGTH",110);
		var decayed=new CombatState(s.state.slots,s.state.realLevels,visible,s.state.prayers,1,1000,false,Map.of(),s.state.resources);
		var sample=sample("melee",decayed); var c=evaluator.compare(sample,evaluator.setup(sample,melee()),account(null),Preferences.EMPTY);
		assertEquals(110,c.before.visibleMagic); assertEquals(0,c.dpsGain(),0);
		assertTrue(evaluator.compare(s,evaluator.setup(s,melee()),account(null),Preferences.EMPTY).before.dps>c.before.dps);
	}
	@Test public void fullInventoryCannotSilentlyDiscardSuppliesForATwoHandedSwitch()
	{
		var s=state(melee(),Map.of(22325,1L)); var resources=new HashMap<>(s.resources); resources.put("inventory-slots-used",28);
		var full=sample("melee",new CombatState(s.slots,s.realLevels,s.visibleLevels,s.prayers,1,1000,false,s.inventory,resources));
		var slots=new HashMap<>(melee()); slots.put("weapon",22325); slots.put("shield",0);
		var c=evaluator.compare(full,evaluator.setup(full,slots),account(null),Preferences.EMPTY);
		assertFalse(c.available(THEORETICAL_DPS)); assertTrue(c.reason.contains("inventory space"));
		assertEquals(Integer.valueOf(12954),full.state.slots.get("shield"));
	}
	@Test public void searchWorkLimitPreservesTheActualBaselineAndDoesNotClaimAnOptimum()
	{
		Set<Integer> bank=new HashSet<>(); catalog.ranged.items.forEach(i->bank.add(i.id));
		var s=sample("ranged",state(ranged(),Map.of()));
		var choices=evaluator.evaluate(summary(List.of(s)),account(bank),Preferences.EMPTY).choices;
		var bankChoice=choices.get(1);
		assertFalse(bankChoice.exhaustive); assertNotNull(bankChoice.comparison);
		assertTrue(bankChoice.comparison.dpsGain()>=0); assertTrue(bankChoice.coverage.toString().contains("work limit"));
	}
}
