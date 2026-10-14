package com.ironpath.advisor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.preferences.Preferences;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.P4aFixtures.*;
import static org.junit.Assert.*;

public class MeleeAdviceTest
{
	private KnowledgeCatalog catalog; private MeleeComparisonService service;
	@Before public void load() throws Exception { catalog=KnowledgeCatalog.load(new Gson());service=new MeleeComparisonService(catalog); }
	public static AccountSnapshot ordinary()
	{ return facts(account(Set.of(4151,10828,10551,11834,7462,6570,6585,11840,11773,12954),90,90),"fremennik-isles",fact(true)); }
	private Advisor.UpgradeView advice(AccountSnapshot a, Preferences p)
	{ return new Advisor(catalog).upgrades(a,p).stream().filter(v->v.upgrade.id.equals("abyssal-tentacle")).findFirst().orElseThrow(); }
	@Test public void completeWhipUpgradeHasValueAndASeparateRemainingRoute()
	{
		AccountSnapshot a=ordinary();CombatSetup before=service.propose(a);ComparisonResult c=service.compare(a,Preferences.EMPTY,before,12006);
		assertEquals(Integer.valueOf(4151),before.slots.get("weapon"));assertTrue(c.available(ComparisonResult.Metric.THEORETICAL_DPS));assertTrue(c.dpsGain()>0);
		assertEquals(Integer.valueOf(12954),c.afterSetup.slots.get("shield"));assertEquals(ComparisonResult.Metric.THEORETICAL_DPS,c.primaryMetric);
		assertEquals(4,c.after.attackTicks);assertFalse(c.available(ComparisonResult.Metric.EXPECTED_KILL_TIME));assertFalse(c.available(ComparisonResult.Metric.TRIP_PERFORMANCE));
		Advisor.UpgradeView view=advice(a,R15Fixtures.absent(catalog));assertNotNull(view.card);assertTrue(view.path.stream().anyMatch(r->r.label.contains("Combine")));
		assertTrue(view.upgrade.limitation.contains("Poison"));assertTrue(c.assumptions.stream().anyMatch(s->s.contains("10,000")));
	}
	@Test public void bestOwnedPackageUsesDpsAndNeverKeepsAnIncompatibleDefender()
	{
		AccountSnapshot a=R15Fixtures.items(ordinary(),7158,21009,23528);
		CombatSetup proposal=service.propose(a);assertEquals(Integer.valueOf(4151),proposal.slots.get("weapon"));
		ComparisonResult slow=service.compare(a,Preferences.EMPTY,proposal,7158);
		assertEquals(Integer.valueOf(0),slow.afterSetup.slots.get("shield"));assertEquals(7,slow.after.attackTicks);assertTrue(slow.before.maxHit<slow.after.maxHit);assertTrue(slow.dpsGain()<0);
		AccountSnapshot onlySlow=account(Set.of(7158,12954),90,90);CombatSetup before=service.propose(onlySlow);
		assertEquals(Integer.valueOf(0),before.slots.get("shield"));ComparisonResult whip=service.compare(onlySlow,Preferences.EMPTY,before,12006);
		assertEquals(Integer.valueOf(12954),whip.afterSetup.slots.get("shield"));assertTrue(whip.dpsGain()>0);
		AccountSnapshot unsupported=new AccountSnapshot(onlySlow.levels,onlySlow.inventory,fact(Set.of(4151,999999)),fact(Set.of(7158)),
			onlySlow.rigour,onlySlow.augury,onlySlow.facts,fact(Map.of("weapon",4151,"shield",999999)),Observation.unknown(),Observation.unknown(),Observation.unknown());
		CombatSetup twoHanded=service.propose(unsupported);assertEquals(Integer.valueOf(7158),twoHanded.slots.get("weapon"));
		assertFalse(service.compare(unsupported,Preferences.EMPTY,twoHanded,12006).available(ComparisonResult.Metric.THEORETICAL_DPS));
	}
	@Test public void paretoPackagesKeepAccuracyTradeoffsInsteadOfGreedyStrength()
	{
		// Both ring alternatives must be searched; an attack-heavy case can win without the largest strength.
		AccountSnapshot a=account(Set.of(1333,11773,11772),90,90);
		CombatSetup proposal=service.propose(a,"graardor");
		Map<Integer,MeleeItem> items=catalog.melee.items.stream().collect(java.util.stream.Collectors.toMap(i->i.id,i->i));
		TargetProfile t=catalog.comparison.targets.stream().filter(x->x.id.equals("graardor")).findFirst().orElseThrow();
		double best=-1;
		for(int ring:List.of(11773,11772))for(MeleeModel.Style style:items.get(1333).styles)
		{ CombatSetup s=proposal.edit("ring",Integer.toString(ring)).edit("style",style.name());best=Math.max(best,MeleeModel.calculate(90,90,s,t,items).dps); }
		assertEquals(best,MeleeModel.calculate(90,90,proposal,t,items).dps,1e-12);
	}
	@Test public void explicitExclusionsArePreservedAcrossCandidatePackages()
	{
		AccountSnapshot a=ordinary();CombatSetup before=service.propose(a).edit("ring","0").edit("neck","0");
		ComparisonResult c=service.compare(a,Preferences.EMPTY,before,12006);
		assertEquals(Integer.valueOf(0),c.afterSetup.slots.get("ring"));assertEquals(Integer.valueOf(0),c.afterSetup.slots.get("neck"));
	}
	@Test public void unknownGearManualOwnershipAndInvalidStylesCannotCreateNumbers()
	{
		AccountSnapshot a=ordinary();CombatSetup before=service.propose(a);
		assertFalse(service.compare(a,Preferences.EMPTY,before.edit("head","-1"),12006).available(ComparisonResult.Metric.MAX_HIT));
		assertTrue(service.compare(a,Preferences.EMPTY,before.edit("neck","4081"),12006).reason.contains("Unreviewed")); // Salve is not zero.
		assertTrue(service.compare(a,Preferences.EMPTY,before.edit("style","SLASH_AGGRESSIVE"),12006).reason.contains("stance"));
		assertTrue(service.compare(a,Preferences.EMPTY,before.edit("target","toa"),12006).reason.contains("scaling"));
		AccountSnapshot unread=new AccountSnapshot(a.levels,Observation.unknown(),Observation.unknown(),Observation.unknown(),a.rigour,a.augury,a.facts);
		Preferences manual=Preferences.EMPTY.confirm("owns-abyssalTentacle",true,TIME);
		assertFalse(service.compare(unread,manual,service.propose(unread),12006).available(ComparisonResult.Metric.THEORETICAL_DPS));
		assertTrue(service.compare(a,manual,before.edit("weapon","12006"),4151).reason.contains("not observed"));
	}
	@Test public void unmodelledStrongOwnedFamiliesWithholdSuperiorityButRetainUnaffectedMetrics()
	{
		for(int id:List.of(26219,22325,28338,22978))
		{
			AccountSnapshot a=R15Fixtures.items(ordinary(),id);ComparisonResult c=service.compare(a,Preferences.EMPTY,service.propose(a),12006);
			assertFalse(c.available(ComparisonResult.Metric.THEORETICAL_DPS));assertTrue(c.available(ComparisonResult.Metric.MAX_HIT));assertTrue(c.available(ComparisonResult.Metric.HIT_CHANCE));
			assertTrue(c.reason.contains("competing melee"));assertNotEquals(ValueEvidence.Importance.KEY,advice(a,R15Fixtures.absent(catalog)).valueEvidence.importance);
		}
	}
	@Test public void ownedComponentsBypassTheirDropRequirementsAndFinishedFormsSuppressAcquisition()
	{
		AccountSnapshot a=levels(R15Fixtures.items(ordinary(),12004),"SLAYER",1);Preferences p=R15Fixtures.absent(catalog);
		Advisor.UpgradeView view=advice(a,p);assertEquals(PursuitAssessment.Mode.READY,view.pursuit.acquisitionMode);
		assertTrue(view.pursuit.blockers.stream().noneMatch(s->s.contains("87")||s.contains("task")));
		assertTrue(view.path.stream().filter(r->r.label.contains("87 Slayer")||r.label.contains("task")).allMatch(r->r.truth==com.ironpath.model.Truth.TRUE));
		for(int form:List.of(12006,26484))
		{ AccountSnapshot owned=R15Fixtures.items(a,form);assertTrue(new Advisor(catalog).upgrades(owned,p).stream().noneMatch(v->v.upgrade.id.equals("abyssal-tentacle"))); }
		assertTrue(new Advisor(catalog).upgrades(a,p.skip("slayer",true)).stream().noneMatch(v->v.upgrade.id.equals("abyssal-tentacle")));
	}
	@Test public void prayerUnlockAndEquipRequirementsAreRevalidated()
	{
		AccountSnapshot a=ordinary();CombatSetup s=service.propose(a).edit("prayer","PIETY");
		assertTrue(service.compare(a,Preferences.EMPTY,s,12006).reason.contains("unlock"));
		Preferences unlocked=Preferences.EMPTY.confirm("knight-waves",true,TIME);assertTrue(service.compare(a,unlocked,s,12006).available(ComparisonResult.Metric.THEORETICAL_DPS));
		assertFalse(service.compare(levels(a,"DEFENCE",60),unlocked,s,12006).available(ComparisonResult.Metric.THEORETICAL_DPS));
		assertFalse(service.compare(levels(a,"ATTACK",70),Preferences.EMPTY,service.propose(levels(a,"ATTACK",70)),12006).available(ComparisonResult.Metric.THEORETICAL_DPS));
	}
	@Test public void ownedQuestHeadgearNeedsItsEquipUnlock()
	{
		AccountSnapshot a=facts(ordinary(),"fremennik-isles",Observation.unknown());
		assertTrue(service.compare(a,Preferences.EMPTY,service.propose(a),12006).reason.contains("equip unlock"));
		Preferences p=Preferences.EMPTY.confirm("fremennik-isles",true,TIME);
		assertTrue(service.compare(a,p,service.propose(a,p),12006).available(ComparisonResult.Metric.THEORETICAL_DPS));
		assertFalse(service.compare(facts(a,"fremennik-isles",fact(false)),p,service.propose(a,p),12006).available(ComparisonResult.Metric.THEORETICAL_DPS));
		AccountSnapshot lockedUpgrade=R15Fixtures.items(ordinary(),24271);
		assertEquals(Integer.valueOf(10828),service.propose(lockedUpgrade).slots.get("head")); // Unconfirmed stronger item cannot prune the usable alternative.
	}
	@Test public void bloodFuryHasEqualOrdinaryStatsWithoutInventedHealingOrTripGain()
	{
		AccountSnapshot a=R15Fixtures.items(ordinary(),24780);CombatSetup s=service.propose(a).edit("neck","6585");
		ComparisonResult c=service.compare(a,Preferences.EMPTY,s,24780);assertEquals(0,c.dpsGain(),1e-12);
		assertFalse(c.available(ComparisonResult.Metric.TRIP_PERFORMANCE));assertTrue(c.assumptions.stream().anyMatch(x->x.contains("lower offence never establishes lower sustain")));
	}
	@Test public void everyActiveOutcomeOwnedHasNoPaddedAcquisitionAdvice()
	{
		java.util.Set<Integer> ids=new java.util.HashSet<>(ordinary().bank.value());catalog.upgrades.forEach(u->ids.add(u.itemId));
		AccountSnapshot a=account(ids,99,99);assertTrue(new Advisor(catalog).upgrades(a,R15Fixtures.absent(catalog)).isEmpty());
	}
	@Test public void malformedMeleeContractsFailClosed() throws Exception
	{
		for(String mutation:List.of("missing-stat","bad-style","partial-target","unknown-source","hands"))
		{
			JsonObject doc=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/catalog.json"))).getAsJsonObject();
			JsonObject item=doc.getAsJsonObject("melee").getAsJsonArray("items").get(0).getAsJsonObject();
			if(mutation.equals("missing-stat")){item.remove("strength");}
			if(mutation.equals("bad-style")){item.getAsJsonArray("styles").set(0,new com.google.gson.JsonPrimitive("INVENTED"));}
			if(mutation.equals("partial-target")){doc.getAsJsonObject("comparison").getAsJsonArray("targets").get(0).getAsJsonObject().remove("meleeSlash");}
			if(mutation.equals("unknown-source")){item.getAsJsonArray("sources").add("unreviewed");}
			if(mutation.equals("hands")){item.addProperty("slot","neck");item.addProperty("twoHanded",true);}
			try{KnowledgeCatalog.read(new Gson(),new StringReader(doc.toString()));fail(mutation);}catch(IllegalArgumentException expected){}
		}
	}
}
