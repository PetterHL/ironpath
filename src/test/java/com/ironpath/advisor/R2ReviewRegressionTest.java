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
	private Advisor advisor;
	private RangedComparisonService service;
	private Preferences missing;
	@Before public void load() throws Exception
	{ catalog=KnowledgeCatalog.load(new Gson()); advisor=new Advisor(catalog); service=new RangedComparisonService(catalog); missing=absent(catalog); }
	private Advisor.UpgradeView view(AccountSnapshot a, String id)
	{ return advisor.upgrades(a,missing).stream().filter(v->v.upgrade.id.equals(id)).findFirst().orElseThrow(); }
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
		Advisor.UpgradeView body=view(a,"crystal-body");
		assertTrue(body.contextComparisons.stream().anyMatch(r->r.target.id.equals("graardor")&&r.dpsGain()>0));
		assertNotEquals(PursuitAssessment.Category.DEFER,body.pursuit.category);
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
	@Test public void ownedUncutAndCutZenyteRemoveGorillaWorkButKeepDownstreamRequirements()
	{
		for (int intermediate : new int[]{19496,19493})
		{
			AccountSnapshot a=withFact(levels(levels(items(zenyteAccount(),intermediate),"CRAFTING",70),"MAGIC",90),"monkey-madness-ii",fact(false));
			Advisor.UpgradeView anguish=view(a,"necklace-of-anguish");
			assertEquals(PursuitAssessment.Mode.BOUNDED,anguish.pursuit.acquisitionMode);
			assertEquals(PursuitAssessment.Burden.LOW,anguish.pursuit.burden);
			assertTrue(anguish.pursuit.blockers.stream().anyMatch(s->s.contains("CRAFTING >= 92")));
			assertTrue(anguish.pursuit.blockers.stream().anyMatch(s->s.contains("MAGIC >= 93")));
			assertTrue(anguish.pursuit.blockers.stream().anyMatch(s->s.contains("gold bar")));
			assertEquals(intermediate==19496,anguish.pursuit.blockers.stream().anyMatch(s->s.contains("CRAFTING >= 89")));
			assertFalse(anguish.pursuit.blockers.toString(),anguish.pursuit.blockers.stream().anyMatch(s->s.contains("Monkey Madness")||s.contains("Zenyte shard")));
			assertFalse(advisor.activityOpportunities(a,missing,advisor.upgrades(a,missing)).stream().filter(o->o.activity.equals("demonic-gorillas")).anyMatch(o->o.usefulFamilies.contains("necklace-of-anguish")));
		}
	}
	@Test public void absentZenyteKeepsTheActualGorillaAcquisition()
	{
		AccountSnapshot a=zenyteAccount(); Advisor.UpgradeView anguish=view(a,"necklace-of-anguish");
		assertEquals(PursuitAssessment.Mode.REPEATABLE,anguish.pursuit.acquisitionMode);
		assertTrue(advisor.activityOpportunities(a,missing,advisor.upgrades(a,missing)).stream().filter(o->o.activity.equals("demonic-gorillas")).anyMatch(o->o.usefulFamilies.contains("necklace-of-anguish")));
	}
	@Test public void ruptureNeedsCraftingForBothRawAndEtchedFang()
	{
		for (int fang : new int[]{33634,33636})
		{
			AccountSnapshot a=withFact(levels(items(zenyteAccount(),fang,19547),"CRAFTING",70),"blood-moon-rises",fact(true));
			Advisor.UpgradeView rupture=view(a,"necklace-of-rupture");
			assertTrue(rupture.pursuit.blockers.toString(),rupture.pursuit.blockers.stream().anyMatch(s->s.contains("CRAFTING >= 84")));
			assertEquals(PursuitAssessment.Category.PLAN,rupture.pursuit.category);
			assertFalse(view(levels(a,"CRAFTING",84),"necklace-of-rupture").pursuit.blockers.stream().anyMatch(s->s.contains("CRAFTING >= 84")));
		}
	}
	@Test public void unknownCraftingRemainsARuptureCheck()
	{
		AccountSnapshot a=items(zenyteAccount(),33636,19547);
		Map<String,Integer> known=new HashMap<>(a.levels.value()); known.remove("CRAFTING");
		a=new AccountSnapshot(fact(known),a.inventory,a.equipment,a.bank,a.rigour,a.augury,a.facts,a.equipmentSlots,a.inventoryQuantities,a.equipmentQuantities,a.bankQuantities);
		Advisor.UpgradeView rupture=view(a,"necklace-of-rupture");
		assertTrue(rupture.pursuit.blockers.toString(),rupture.pursuit.blockers.stream().anyMatch(s->s.contains("CRAFTING >= 84")));
		assertEquals(PursuitAssessment.Category.CHECK,rupture.pursuit.category);
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
