package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.preferences.Preferences;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.P4aFixtures.*;
import static org.junit.Assert.*;

public class PoweredMagicTest
{
	private KnowledgeCatalog catalog;
	private PoweredMagic model;
	@Before public void setup() throws Exception { catalog = KnowledgeCatalog.load(new Gson()); model = new PoweredMagic(catalog); }
	private PoweredMagic.Result compare(AccountSnapshot a, String kind) { return model.compare(a, Preferences.EMPTY, model.propose(a), kind); }

	@Test public void handDerivedNinetyMagicIncludesAccuracySuccessfulZeroAndFourTickInterval()
	{
		AccountSnapshot a = account(Set.of(11907), 90, 90);
		PoweredMagic.Result swamp = compare(a, "SWAMP"), occult = compare(a, "OCCULT");
		assertEquals(25, swamp.before.maxHit); assertEquals(28, swamp.after.maxHit); assertEquals(26, occult.after.maxHit);
		// Inclusive rolls: (90+3+8)*(15+64)=7979; Dharok (1+9)*(64-11)=530.
		assertEquals(7979, swamp.before.attackRoll); assertEquals(530, swamp.before.defenceRoll);
		assertEquals(29.0 / 30, swamp.before.hitChance, 1e-12);
		// Successful hits 0..25 have their zero raised to 1: sum 326 over 26 outcomes.
		assertEquals((29.0 / 30) * (326.0 / 26) / 2.4, swamp.before.dps, 1e-12);
		assertEquals((1 - 266.0 / 8990) * (407.0 / 29) / 2.4, swamp.after.dps, 1e-12);
		assertEquals(PoweredMagic.Status.CONDITIONAL, swamp.status);
		assertTrue(swamp.summary().contains("percentage points"));
		assertTrue(swamp.assumptions.stream().anyMatch(s -> s.contains("no wind weakness")));
	}

	@Test public void independentMagicBreakpointsUseVisibleLevelsAndIntegerDamageRounding()
	{
		int[][] expected = {{75,20,21},{78,21,22},{89,24,25},{90,25,26},{92,25,26},{93,26,27},{99,28,29}};
		for (int[] row : expected)
		{
			PoweredMagic.Result result = compare(account(Set.of(11907), row[0], 90), "OCCULT");
			assertEquals(row[1], result.before.maxHit); assertEquals(row[2], result.after.maxHit);
		}
		assertEquals(24, compare(account(Set.of(11907), 78, 90), "SWAMP").after.maxHit);
		assertEquals(27, compare(account(Set.of(11907), 89, 90), "SWAMP").after.maxHit);
		assertEquals(31, compare(account(Set.of(11907), 99, 90), "SWAMP").after.maxHit);
	}

	@Test public void stylesAffectAccuracyAndNeverPoweredStaffBaseMaxHit()
	{
		AccountSnapshot a = account(Set.of(11907), 90, 90); PoweredMagic.Setup setup = model.propose(a);
		PoweredMagic.Result result = model.compare(a, Preferences.EMPTY, setup.edit("style", "LONGRANGE"), "OCCULT");
		assertEquals(25, result.before.maxHit); assertEquals(99, result.before.effectiveMagic); assertEquals(7821, result.before.attackRoll);
		assertTrue(result.before.hitChance < compare(a, "OCCULT").before.hitChance);
	}

	@Test public void supportedPrayerBoostAndNegativeBootBonusHaveIndependentHandChecks()
	{
		AccountSnapshot a = account(Set.of(11907,13237), 90, 90); PoweredMagic.Setup s = model.propose(a);
		PoweredMagic.Result might = model.compare(a, Preferences.EMPTY, s.edit("feet", "0").edit("prayer", "MYSTIC_MIGHT").edit("boost", "MAGIC_POTION"), "OCCULT");
		// Visible 94, floor(94*1.15)+3+8=119; floor((31-5)*1.02)=26, occult total 7%=27.
		assertEquals(94, might.before.visibleMagic); assertEquals(119, might.before.effectiveMagic);
		assertEquals(9401, might.before.attackRoll); assertEquals(26, might.before.maxHit); assertEquals(27, might.after.maxHit);
		PoweredMagic.Result augury = model.compare(a, Preferences.EMPTY, s.edit("feet", "0").edit("prayer", "AUGURY"), "OCCULT");
		assertEquals(123, augury.before.effectiveMagic); assertEquals(26, augury.before.maxHit); assertEquals(27, augury.after.maxHit);
		assertEquals(6767, model.compare(a, Preferences.EMPTY, s, "OCCULT").before.attackRoll); // 101*(64+15-12)
	}

	@Test public void successfulZerosAreNotMissesAndBothAccuracyBranchesAreChecked()
	{
		assertEquals(0.25, PoweredMagic.hitChance(5,9), 1e-12);
		assertEquals(0.625, PoweredMagic.hitChance(11,7), 1e-12);
		assertEquals(0.45, PoweredMagic.hitChance(9,9), 1e-12);
		assertEquals(0, PoweredMagic.hitChance(0,9), 0);
		assertEquals(1, PoweredMagic.successfulMean(1), 0);
		assertEquals(4.0/3, PoweredMagic.successfulMean(2), 1e-12);
		assertThrows(IllegalArgumentException.class, () -> PoweredMagic.hitChance(-1,9));
		assertThrows(IllegalArgumentException.class, () -> PoweredMagic.successfulMean(0));
	}

	@Test public void anOwnedEquivalentHasZeroDeltaAndUnknownSlotsNeverBecomeZero()
	{
		AccountSnapshot a = account(Set.of(11907,12002), 90, 90);
		PoweredMagic.Result result = compare(a,"OCCULT"); assertEquals(result.before.dps, result.after.dps, 0);
		Map<String,Integer> slots = new HashMap<>(model.propose(a).slots); slots.remove("head");
		assertEquals(PoweredMagic.Status.UNSUPPORTED, model.compare(a, Preferences.EMPTY,
			new PoweredMagic.Setup(slots, PoweredMagic.Prayer.NONE, PoweredMagic.Boost.NONE, PoweredMagic.Style.ACCURATE,false,false),"OCCULT").status);
		assertEquals(PoweredMagic.Status.UNSUPPORTED, model.compare(a, Preferences.EMPTY,model.propose(a).edit("neck","11907"),"OCCULT").status);
	}

	@Test public void unsupportedEquipmentRequiresAnExplicitMinimalReferenceChoice()
	{
		AccountSnapshot base=account(Set.of(11907),90,90);
		AccountSnapshot a = new AccountSnapshot(base.levels,base.inventory,fact(Set.of(9999)),base.bank,base.rigour,base.augury,base.facts);
		PoweredMagic.Setup s=model.propose(a);
		assertEquals(PoweredMagic.Status.UNSUPPORTED,model.compare(a,Preferences.EMPTY,s,"SWAMP").status);
		assertEquals(PoweredMagic.Status.CONDITIONAL,model.compare(a,Preferences.EMPTY,s.edit("template","true"),"SWAMP").status);
		AccountSnapshot unread=new AccountSnapshot(a.levels,a.inventory,Observation.unknown(),a.bank,a.rigour,a.augury,a.facts);
		assertTrue(compare(unread,"OCCULT").reason.contains("Equipment is unread"));
	}

	@Test public void manualAndEmptyWeaponsDoNotBecomeChargedWhileReviewedEnhancedFormsWork()
	{
		AccountSnapshot empty=account(Set.of(11908),90,90); PoweredMagic.Setup s=model.propose(empty);
		assertEquals(PoweredMagic.Status.UNSUPPORTED,model.compare(empty,Preferences.EMPTY.confirm("item-11907",true,TIME),s,"SWAMP").status);
		for(int id:new int[]{11905,11907,22288}) { assertEquals(25,compare(account(Set.of(id),90,90),"SWAMP").before.maxHit); }
		assertEquals(PoweredMagic.Status.CONDITIONAL,compare(account(Set.of(27275),90,90),"OCCULT").status);
		assertEquals(PoweredMagic.Status.UNSUPPORTED,compare(account(Set.of(11907),75,90),"SWAMP").status);
	}

	@Test public void unavailablePrayersAndEquipRequirementsWithholdNumbers()
	{
		AccountSnapshot a=account(Set.of(11907,13239),90,90);
		assertEquals(PoweredMagic.Status.UNSUPPORTED,compare(levels(a,"STRENGTH",74),"OCCULT").status);
		assertEquals(PoweredMagic.Status.UNSUPPORTED,model.compare(levels(a,"PRAYER",44),Preferences.EMPTY,model.propose(a).edit("prayer","MYSTIC_MIGHT"),"OCCULT").status);
		AccountSnapshot locked=new AccountSnapshot(a.levels,a.inventory,a.equipment,a.bank,a.rigour,fact(false),a.facts);
		assertEquals(PoweredMagic.Status.UNSUPPORTED,model.compare(locked,Preferences.EMPTY.confirm("augury",true,TIME),model.propose(a).edit("prayer","AUGURY"),"OCCULT").status);
	}

	@Test public void originalSyntheticOracleMatrixMatchesThePinnedLocalReference() throws Exception
	{
		// Input corpus was authored for this task. Outputs are from a separate GPL reference run,
		// not imported source/stat data; tolerance covers only floating-point arithmetic.
		int count=0;
		try(BufferedReader reader=new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/p4a-oracle.csv"),StandardCharsets.UTF_8)))
		{
			String line;
			while((line=reader.readLine())!=null)
			{
				if(line.startsWith("#")||line.startsWith("magic"))continue;
				String[] v=line.split(","); int magic=Integer.parseInt(v[0]),weapon=Integer.parseInt(v[1]),neck=Integer.parseInt(v[2]);
				AccountSnapshot a=account(neck==0?Set.of(weapon):Set.of(weapon,neck),magic,90);
				PoweredMagic.Setup s=model.propose(a).edit("style",v[3].toUpperCase(java.util.Locale.ROOT));
				if(v[4].equals("might-potion"))s=s.edit("prayer","MYSTIC_MIGHT").edit("boost","MAGIC_POTION");
				if(v[4].equals("augury"))s=s.edit("prayer","AUGURY");
				PoweredMagic.Metrics r=model.compare(a,Preferences.EMPTY,s,"OCCULT").before;
				assertNotNull(line,r); assertEquals(line,Integer.parseInt(v[5]),r.maxHit); assertEquals(line,Long.parseLong(v[6]),r.attackRoll);
				assertEquals(line,530,r.defenceRoll); assertEquals(line,Double.parseDouble(v[7]),r.hitChance,1e-12); assertEquals(line,Double.parseDouble(v[8]),r.dps,1e-10);count++;
			}
		}
		assertEquals(234,count);
	}
}
