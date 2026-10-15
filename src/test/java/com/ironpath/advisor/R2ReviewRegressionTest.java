package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.ComparisonResult.Metric.*;
import static com.ironpath.advisor.P4aFixtures.*;
import static com.ironpath.advisor.R15Fixtures.*;
import static org.junit.Assert.*;

/** Original synthetic regressions for the four R2 review findings. */
public class R2ReviewRegressionTest
{
	private KnowledgeCatalog catalog;

	private RangedComparisonService service;
	private Preferences missing;
	@Before public void load() throws Exception
	{ catalog=KnowledgeCatalog.load(new Gson()); service=new RangedComparisonService(catalog); missing=absent(catalog); }

	private AccountSnapshot rangedPackages()
	{ return account(Set.of(12926,11230,25867,27226,27229,27232,19547,28955,7462),99,99); }
	private AccountSnapshot zenyteAccount()
	{ return withFact(levels(account(Set.of(12788,892),99,99),"CRAFTING",99),"monkey-madness-ii",fact(true)); }
	@Test public void crystalBodyReconsidersOwnedBowfaAfterBlowpipeReference()
	{
		AccountSnapshot a=rangedPackages(); CombatSetup before=service.propose(a,"graardor");
		assertEquals(Integer.valueOf(12926),before.slots.get("weapon"));
		ComparisonResult result=service.compare(a,missing,before,23975);
		assertTrue(result.summary(),result.dpsGain()>0);
		assertEquals(Integer.valueOf(25867),result.afterSetup.slots.get("weapon"));
		assertEquals(Integer.valueOf(23975),result.afterSetup.slots.get("body"));
		assertEquals(Integer.valueOf(0),result.afterSetup.slots.get("ammo"));
		assertEquals(Integer.valueOf(0),result.afterSetup.slots.get("shield"));
		assertEquals(THEORETICAL_DPS,result.primaryMetric);
	}
	@Test public void armourCannotBorrowAnUnobservedWeaponOrItsAmmo()
	{
		AccountSnapshot a=account(Set.of(12926,11230,27229,25862),99,99);
		// An inactive ownership family does not establish a supplied Bowfa package.
		Preferences manual=missing.confirm("owns-bowfa",true,TIME);
		ComparisonResult result=service.compare(a,manual,service.propose(a,"graardor"),23975);
		assertEquals(Integer.valueOf(12926),result.afterSetup.slots.get("weapon"));
		assertTrue(result.dpsGain()<0);
		AccountSnapshot noArrows=account(Set.of(12926,11230,20997,27229),99,99);
		ComparisonResult unavailableAmmo=service.compare(noArrows,missing,service.propose(noArrows,"zilyana"),23975);
		assertEquals(Integer.valueOf(12926),unavailableAmmo.afterSetup.slots.get("weapon"));
	}
	@Test public void offhandUpgradeFindsOwnedOneHandedPackage()
	{
		AccountSnapshot a=items(rangedPackages(),9185,9144);
		ComparisonResult result=service.compare(a,missing,service.propose(a,"graardor"),21000);
		assertTrue(result.reason,result.available(THEORETICAL_DPS));
		assertEquals(Integer.valueOf(9185),result.afterSetup.slots.get("weapon"));
		assertEquals(Integer.valueOf(21000),result.afterSetup.slots.get("shield"));
		assertEquals(Integer.valueOf(9144),result.afterSetup.slots.get("ammo"));
	}
	@Test public void packageSearchPreservesSelectedCombatAssumptionsAndCandidate()
	{
		AccountSnapshot a=items(rangedPackages(),11664,13072,13073,8842);
		CombatSetup before=service.propose(a,"graardor").edit("prayer","EAGLE_EYE").edit("boost","true").edit("style","ACCURATE").edit("task","true");
		ComparisonResult result=service.compare(a,missing,before,23975);
		assertTrue(result.reason,result.available(THEORETICAL_DPS));
		assertEquals(Integer.valueOf(23975),result.afterSetup.slots.get("body"));
		assertEquals(before.rangedPrayer,result.afterSetup.rangedPrayer);
		assertEquals(before.rangingPotion,result.afterSetup.rangingPotion);
		assertEquals(before.rangedStyle,result.afterSetup.rangedStyle);
		assertEquals(before.onTask,result.afterSetup.onTask);
		assertEquals(before.target,result.afterSetup.target);
	}

	@Test public void blessedQuiverWinsForArrowsAndBoltsWithoutAddingCrystalOrDartBonus()
	{
		AccountSnapshot a=account(Set.of(20997,11212,28947,28955),99,99);
		CombatSetup best=service.propose(a,"zilyana");
		assertEquals(Integer.valueOf(28955),best.slots.get("cape"));
		ComparisonResult r=service.compare(a,missing,best.edit("cape","28947"),28955);
		assertTrue(r.dpsGain()>0); assertEquals(10,r.after.attackBonus-r.before.attackBonus);
		assertEquals(1,r.after.damagePercent-r.before.damagePercent,0);
		AccountSnapshot bolts=account(Set.of(9185,9144,28947,28955),99,99);
		assertEquals(Integer.valueOf(28955),service.propose(bolts,"zilyana").slots.get("cape"));
		for (Set<Integer> owned : Set.of(Set.of(25867,28947,28955),Set.of(12926,11230,28947,28955)))
		{
			AccountSnapshot other=account(owned,99,99); CombatSetup s=service.propose(other,"zilyana");
			ComparisonResult unchanged=service.compare(other,missing,s.edit("cape","28947"),28955);
			assertEquals(unchanged.before.attackBonus,unchanged.after.attackBonus);
			assertEquals(unchanged.before.damagePercent,unchanged.after.damagePercent,0);
		}
	}
}
