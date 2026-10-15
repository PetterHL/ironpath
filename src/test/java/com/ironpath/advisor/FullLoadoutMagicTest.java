package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.preferences.Preferences;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.P4aFixtures.*;
import static com.ironpath.advisor.ComparisonResult.Metric.*;
import static org.junit.Assert.*;

public class FullLoadoutMagicTest
{
	private KnowledgeCatalog catalog;
	private CombatComparisonService service;
	@Before public void setup() throws Exception { catalog = KnowledgeCatalog.load(new Gson()); service = new CombatComparisonService(catalog); }
	private Map<String, Integer> slots(int weapon, int armour)
	{
		int[][] robes = {{4708,4712,4714},{26241,26243,26245},{21018,21021,21024}};
		Map<String, Integer> slots = new HashMap<>(); PoweredMagic.SLOTS.forEach(slot -> slots.put(slot, 0));
		slots.putAll(Map.of("weapon",weapon,"head",robes[armour][0],"body",robes[armour][1],"legs",robes[armour][2],
			"cape",21795,"neck",12002,"hands",19544,"feet",13235,"ring",11770));
		if (weapon != 27275) { slots.put("shield",25985); } return slots;
	}
	@Test public void observedAncientBlessingRemainsInSupportedReferenceWithoutChangingMagicMetrics()
	{
		Map<String,Integer> equipped=slots(12899,2);equipped.put("ammo",20235);
		Set<Integer> owned=new HashSet<>(equipped.values());owned.remove(0);
		AccountSnapshot base=account(owned,99,99);
		AccountSnapshot a=new AccountSnapshot(base.levels,base.inventory,fact(owned),base.bank,base.rigour,base.augury,base.facts,
			fact(equipped),Observation.unknown(),Observation.unknown(),Observation.unknown());
		CombatSetup proposed=service.propose(a);
		assertEquals(Integer.valueOf(20235),proposed.slots.get("ammo"));
		ComparisonResult blessing=service.compare(a,Preferences.EMPTY,proposed,27275);
		Map<String,Integer> empty=new HashMap<>(proposed.slots);empty.put("ammo",0);
		CombatSetup without=new CombatSetup(empty,proposed.prayer,proposed.boost,proposed.style,proposed.confirmed,proposed.minimalTemplate,proposed.target,proposed.provenance);
		ComparisonResult control=service.compare(a,Preferences.EMPTY,without,27275);
		assertTrue(blessing.reason,blessing.available(THEORETICAL_DPS));
		assertEquals(control.before.maxHit,blessing.before.maxHit);
		assertEquals(control.before.attackRoll,blessing.before.attackRoll);
		assertEquals(control.before.dps,blessing.before.dps,0);
		assertTrue(blessing.assumptions.toString().contains("ancient-blessing"));
	}
	@Test public void fullLoadoutsMatch540PinnedOriginalOracleCases() throws Exception
	{
		int count = 0;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/r1-oracle.csv"), StandardCharsets.UTF_8)))
		{
			String line; while ((line = reader.readLine()) != null)
			{
				if (line.startsWith("#") || line.startsWith("magic")) { continue; } String[] row = line.split(",");
				Map<String,Integer> slots = slots(Integer.parseInt(row[1]),Integer.parseInt(row[2]));
				Set<Integer> owned = new HashSet<>(slots.values()); owned.remove(0); AccountSnapshot a = account(owned,Integer.parseInt(row[0]),99);
				PoweredMagic.Prayer prayer = row[5].equals("augury-heart") ? PoweredMagic.Prayer.AUGURY : row[5].equals("might-potion") ? PoweredMagic.Prayer.MYSTIC_MIGHT : PoweredMagic.Prayer.NONE;
				PoweredMagic.Boost boost = row[5].equals("augury-heart") ? PoweredMagic.Boost.SATURATED_HEART : row[5].equals("might-potion") ? PoweredMagic.Boost.MAGIC_POTION : PoweredMagic.Boost.NONE;
				CombatSetup setup = new CombatSetup(slots,prayer,boost,PoweredMagic.Style.valueOf(row[4].toUpperCase(java.util.Locale.ROOT)),true,false,row[3].equals("1673") ? "dharok" : "graardor",Map.of());
				ComparisonResult result = service.compare(a,Preferences.EMPTY,setup,12002); assertTrue(line + result.reason,result.available(THEORETICAL_DPS));
				assertEquals(line,Integer.parseInt(row[6]),result.before.maxHit); assertEquals(line,Long.parseLong(row[7]),result.before.attackRoll);
				assertEquals(line,Long.parseLong(row[8]),result.before.defenceRoll); assertEquals(line,Double.parseDouble(row[9]),result.before.hitChance,1e-12);
				assertEquals(line,Double.parseDouble(row[10]),result.before.dps,1e-10); count++;
			}
		} assertEquals(540,count);
	}
	@Test public void handDerivedShadowFractionalBonusesPrayerOrderAndBothTargets()
	{
		Map<String,Integer> slots=slots(27275,2); AccountSnapshot a=account(new HashSet<>(slots.values()),99,99);
		CombatSetup s=new CombatSetup(slots,PoweredMagic.Prayer.AUGURY,PoweredMagic.Boost.SATURATED_HEART,PoweredMagic.Style.ACCURATE,true,false,"graardor",Map.of());
		ComparisonResult r=service.compare(a,Preferences.EMPTY,s,12002);
		// Gear 22.5% × 3 + 4% prayer = 71.5%; floor((112/3 + 1) × 1.715) = 65.
		assertEquals(65,r.before.maxHit); assertEquals(483,r.before.attackBonus); assertEquals(71.5,r.before.damagePercent,0);
		assertEquals(82597,r.before.attackRoll); assertEquals(32218,r.before.defenceRoll);
		assertEquals((1-32220.0/(2*82598))* (65.0/2+1.0/66) / 3,r.before.dps,1e-12);
		assertEquals(0,r.dpsGain(),0); assertFalse(r.available(EXPECTED_KILL_TIME)); assertFalse(r.available(TRIP_PERFORMANCE));
	}
	@Test public void toaCapArithmeticIsCheckedButUnconfiguredRaidNeverProducesNumbers()
	{
		Map<String,Integer> slots=slots(27275,2);slots.put("hands",31106);slots.put("ring",28313);
		AccountSnapshot a=facts(account(new HashSet<>(slots.values()),99,99),"desert-treasure-ii",fact(true));
		CombatSetup setup=new CombatSetup(slots,PoweredMagic.Prayer.AUGURY,PoweredMagic.Boost.NONE,PoweredMagic.Style.ACCURATE,true,false,"toa",Map.of());
		TargetProfile toa=catalog.comparison.targets.stream().filter(t->t.id.equals("toa")).findFirst().orElseThrow();
		Map<Integer,KnowledgeCatalog.CombatItem> items=catalog.comparison.items.stream().collect(java.util.stream.Collectors.toMap(i->i.id,i->i));
		// Hypothetical ToA gear cap: 26% × 4 capped to 100%; prayer added after cap.
		assertEquals(69,PoweredStaffModel.calculate(99,setup,toa,items).maxHit);
		ComparisonResult result=service.compare(a,Preferences.EMPTY,setup,21018);
		assertNull(result.before);assertFalse(result.available(MAX_HIT));assertTrue(result.reason.contains("no default invocation"));
	}
	@Test public void replacingOneHandedStaffRemovesWardAndNeverManufacturesAnEmptyUnknown()
	{
		Map<String,Integer> slots=slots(12899,0);AccountSnapshot a=account(new HashSet<>(slots.values()),99,99);
		CombatSetup s=new CombatSetup(slots,PoweredMagic.Prayer.NONE,PoweredMagic.Boost.NONE,PoweredMagic.Style.ACCURATE,false,false,"graardor",Map.of());
		ComparisonResult r=service.compare(a,Preferences.EMPTY,s,27275);assertTrue(r.available(THEORETICAL_DPS));assertEquals(Integer.valueOf(0),r.afterSetup.slots.get("shield"));
		slots.put("head",-1);assertNull(service.compare(a,Preferences.EMPTY,new CombatSetup(slots,s.prayer,s.boost,s.style,false,false,s.target,Map.of()),27275).before);
		slots.put("head",4708);slots.put("weapon",27275);AccountSnapshot owned=account(new HashSet<>(slots.values()),99,99);
		assertTrue(service.validate(owned,Preferences.EMPTY,new CombatSetup(slots,s.prayer,s.boost,s.style,false,false,s.target,Map.of())).contains("Two-handed"));
	}
	@Test public void conflictionHistoryWithholdsOnlyUnsupportedMetricsAndStrongerOwnedHandsSuppressGrind()
	{
		Map<String,Integer> slots=slots(31113,1);slots.put("hands",31106);AccountSnapshot a=account(new HashSet<>(slots.values()),99,99);
		CombatSetup s=new CombatSetup(slots,PoweredMagic.Prayer.NONE,PoweredMagic.Boost.NONE,PoweredMagic.Style.ACCURATE,false,false,"graardor",Map.of());
		ComparisonResult r=service.compare(a,Preferences.EMPTY,s,21018);assertTrue(r.available(MAX_HIT));assertFalse(r.available(HIT_CHANCE));assertFalse(r.available(THEORETICAL_DPS));
		slots.put("weapon",27275);slots.put("shield",0);AccountSnapshot shadow=account(new HashSet<>(slots.values()),99,99);
		assertTrue(service.compare(shadow,Preferences.EMPTY,new CombatSetup(slots,s.prayer,s.boost,s.style,false,false,s.target,Map.of()),21018).available(THEORETICAL_DPS));
	}
	@Test public void ownedEyeAndShadowReplaceWeakSeasBaselinesButUnchargedOwnershipCannotSupplyNumbers()
	{
		AccountSnapshot a=account(Set.of(11907,31113),99,99);assertEquals(Integer.valueOf(31113),service.propose(a).slots.get("weapon"));
		AccountSnapshot b=account(Set.of(11907,27275,19544,12002),99,99);assertEquals(Integer.valueOf(27275),service.propose(b).slots.get("weapon"));
		AccountSnapshot c=account(Set.of(27277),99,99);assertNull(service.compare(c,Preferences.EMPTY,service.propose(c),21018).before);
	}
	@Test public void accuracyGapsNameOnlyPresentItemsAndTheirAffectedSide()
	{
		for (int mode=0; mode<3; mode++)
		{
			Map<String,Integer> slots=slots(12899,1);
			if (mode!=1) { slots.put("ring",22975); }
			if (mode!=0) { slots.put("hands",31106); }
			AccountSnapshot a=account(new HashSet<>(slots.values()),99,99);
			CombatSetup s=new CombatSetup(slots,PoweredMagic.Prayer.NONE,PoweredMagic.Boost.NONE,PoweredMagic.Style.ACCURATE,false,false,"neutral-zero-defence",Map.of());
			ComparisonResult r=service.compare(a,Preferences.EMPTY,s,27275);
			assertTrue(r.available(MAX_HIT));
			if (mode==0) { assertTrue(r.available(HIT_CHANCE));assertTrue(r.available(THEORETICAL_DPS));assertTrue(r.gaps.isEmpty());continue; }
			assertFalse(r.available(HIT_CHANCE));assertFalse(r.available(THEORETICAL_DPS));
			assertFalse(r.gaps.containsKey(MAX_HIT)); assertTrue(Double.isNaN(r.dpsGain()));
			String gap=r.gaps.get(THEORETICAL_DPS);
			assertFalse(gap.contains("Brimstone ring: accuracy proc"));
			assertEquals(mode!=0,gap.contains("Before: confliction gauntlets: one-handed miss-history reroll"));
			assertFalse(gap.contains("After: confliction"));
			assertEquals(gap,r.gaps.get(HIT_CHANCE));
		}
		Map<String,Integer> slots=slots(12899,1);AccountSnapshot a=account(new HashSet<>(slots.values()),99,99);
		CombatSetup s=new CombatSetup(slots,PoweredMagic.Prayer.NONE,PoweredMagic.Boost.NONE,PoweredMagic.Style.ACCURATE,false,false,"neutral-zero-defence",Map.of());
		ComparisonResult candidate=service.compare(a,Preferences.EMPTY,s,31106);
		assertTrue(candidate.gaps.get(THEORETICAL_DPS).startsWith("After: confliction"));
		assertTrue(service.compare(a,Preferences.EMPTY,s,27275).gaps.isEmpty());
	}
}
