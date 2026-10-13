package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.preferences.Preferences;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.ComparisonResult.Metric.THEORETICAL_DPS;
import static com.ironpath.advisor.P4aFixtures.*;
import static org.junit.Assert.*;

/** Reproduces reported gear classes with synthetic observations, never a captured account. */
public class ClientEquipmentRegressionTest
{
	// Default, the seven other elven clans, and the cosmetic Deadman reward in permanent worlds.
	private static final int[][] CRYSTAL = {{23971,23975,23979},{27705,27697,27701},{27717,27709,27713},
		{27729,27721,27725},{27741,27733,27737},{27753,27745,27749},{27765,27757,27761},
		{27777,27769,27773},{33031,33023,33027}};
	private KnowledgeCatalog catalog;
	private Advisor advisor;
	private RangedComparisonService service;
	@Before public void load() throws Exception
	{ catalog=KnowledgeCatalog.load(new Gson()); advisor=new Advisor(catalog); service=new RangedComparisonService(catalog); }
	private static AccountSnapshot equipped(Map<String,Integer> worn, int... stored)
	{
		Map<String,Integer> slots=RangedModelTest.slots(0); slots.putAll(worn);
		Set<Integer> equipment=slots.values().stream().filter(id->id>0).collect(Collectors.toSet());
		Set<Integer> bank=new HashSet<>(); for (int id : stored) { bank.add(id); }
		AccountSnapshot a=account(bank,99,99);
		Map<String,Integer> levels=new HashMap<>(a.levels.value()); levels.put("RANGED",99);
		return new AccountSnapshot(fact(levels),a.inventory,fact(equipment),a.bank,a.rigour,a.augury,a.facts,
			fact(slots),Observation.unknown(),Observation.unknown(),Observation.unknown());
	}
	private List<Advisor.UpgradeView> views(AccountSnapshot a)
	{ return advisor.upgrades(a,Preferences.EMPTY); }
	private Advisor.UpgradeView rosewood(AccountSnapshot a)
	{ return views(a).stream().filter(v->v.upgrade.id.equals("rosewood-blowpipe")).findFirst().orElseThrow(); }
	public static AccountSnapshot reportedGearFixture()
	{ return equipped(Map.of("weapon",28688,"head",27729,"body",27721,"legs",27725,"feet",31097),25867,11230); }

	@Test public void everyCrystalColourSuppressesRepeatAcquisitionAndKeepsFullBowfaBonuses()
	{
		for (int[] colour : CRYSTAL)
		{
			AccountSnapshot a=equipped(Map.of("weapon",25867,"head",colour[0],"body",colour[1],"legs",colour[2]));
			Preferences stale=Preferences.EMPTY.confirm("owns-crystalHelmOwned",false,TIME)
				.confirm("owns-crystalBodyOwned",false,TIME).confirm("owns-crystalLegsOwned",false,TIME);
			assertTrue(advisor.upgrades(a,stale).stream().noneMatch(v->v.upgrade.id.startsWith("crystal-")));
			CombatSetup setup=service.propose(a);
			assertEquals(Integer.valueOf(colour[0]),setup.slots.get("head"));
			assertEquals(Integer.valueOf(colour[1]),setup.slots.get("body"));
			assertEquals(Integer.valueOf(colour[2]),setup.slots.get("legs"));
			ComparisonResult comparison=service.compare(a,Preferences.EMPTY,setup,19547);
			assertTrue(comparison.reason,comparison.available(THEORETICAL_DPS));
			// Existing independent hand case: 28 ordinary max hit, full crystal floor(28*46/40)=32.
			assertEquals(32,comparison.before.maxHit); assertEquals(4,comparison.before.attackTicks);
			assertEquals(107L*(128+9+31+18+64)*26/20,comparison.before.attackRoll);
		}
	}
	@Test public void inactiveColoursEstablishOwnershipButNeverActiveSetDamage()
	{
		for (int[] colour : CRYSTAL)
		{
			AccountSnapshot a=equipped(Map.of("weapon",25867,"head",colour[0]+2,"body",colour[1]+2,"legs",colour[2]+2));
			assertTrue(views(a).stream().noneMatch(v->v.upgrade.id.startsWith("crystal-")));
			ComparisonResult c=service.compare(a,Preferences.EMPTY,service.propose(a),19547);
			assertTrue(c.reason,c.available(THEORETICAL_DPS)); assertEquals(28,c.before.maxHit);
			assertEquals(107L*(128+64),c.before.attackRoll);
		}
	}
	@Test public void blazingHasTheSameOrdinaryStatsAsToxicAndIsAnOwnedAlternative()
	{
		AccountSnapshot blazing=equipped(Map.of("weapon",28688),11230), toxic=equipped(Map.of("weapon",12926),11230);
		ComparisonResult b=service.compare(blazing,Preferences.EMPTY,service.propose(blazing),19547);
		ComparisonResult t=service.compare(toxic,Preferences.EMPTY,service.propose(toxic),19547);
		assertTrue(b.reason,b.available(THEORETICAL_DPS)); assertEquals(20,b.before.maxHit);
		assertEquals(2,b.before.attackTicks); assertEquals(107L*(30+64),b.before.attackRoll);
		assertEquals(t.before.dps,b.before.dps,1e-12); assertEquals(t.before.hitChance,b.before.hitChance,1e-12);
		assertTrue(b.assumptions.stream().anyMatch(s->s.contains("Blazing blowpipe")));
		assertTrue(views(blazing).stream().noneMatch(v->v.upgrade.id.equals("toxic-blowpipe")));
		assertEquals(Integer.valueOf(28688),service.referenceChoices(blazing).get("weapon").keySet().stream()
			.map(Integer::valueOf).filter(id->id==28688).findFirst().orElseThrow());
	}
	@Test public void emptyBlowpipesAndManualOwnershipCannotCreateAUsableWeaponOrDartSupply()
	{
		for (int empty : new int[]{12924,28687})
		{
			AccountSnapshot a=equipped(Map.of(),empty,11230);
			assertTrue(views(a).stream().noneMatch(v->v.upgrade.id.equals("toxic-blowpipe")));
			assertFalse(service.compare(a,Preferences.EMPTY,service.propose(a),12926).available(THEORETICAL_DPS));
			assertFalse(service.choices("weapon",a).containsKey(Integer.toString(empty)));
			assertFalse(rosewood(equipped(Map.of("weapon",25867),empty)).pursuit.reasonCodes.contains("OWNED_STRONGER_BLOWPIPE"));
		}
		AccountSnapshot unreadDarts=equipped(Map.of("weapon",28688));
		assertFalse(service.compare(unreadDarts,Preferences.EMPTY,service.propose(unreadDarts),19547).available(THEORETICAL_DPS));
		AccountSnapshot bow=equipped(Map.of("weapon",25867));
		Preferences manual=Preferences.EMPTY.confirm("owns-toxicBlowpipe",true,TIME);
		assertFalse(advisor.upgrades(bow,manual).stream().filter(v->v.upgrade.id.equals("rosewood-blowpipe"))
			.findFirst().orElseThrow().pursuit.reasonCodes.contains("OWNED_STRONGER_BLOWPIPE"));
	}
	@Test public void allEightTreadsFormsSupportRangedWithoutLosingTheirDifferentStrength()
	{
		for (int id : new int[]{31088,31091,31092,31093,31094,31095,31096,31097})
		{
			AccountSnapshot a=equipped(Map.of("weapon",28688,"feet",id),11230);
			CombatSetup setup=service.propose(a); assertEquals(Integer.valueOf(id),setup.slots.get("feet"));
			ComparisonResult c=service.compare(a,Preferences.EMPTY,setup,26235);
			assertTrue(c.reason,c.available(THEORETICAL_DPS)); assertEquals(45,c.before.attackBonus);
			assertEquals(id==31092||id==31094||id==31096||id==31097 ? 58 : 57,c.before.damagePercent,0);
			assertTrue(views(a).stream().noneMatch(v->v.upgrade.id.equals("avernic-treads")));
		}
	}
	@Test public void reportedBlackCrystalBlazingAndMaxTreadsHaveNumbersAndNoRepeatGrinds()
	{
		AccountSnapshot a=reportedGearFixture();
		List<Advisor.UpgradeView> all=views(a);
		assertTrue(all.stream().noneMatch(v->v.upgrade.id.startsWith("crystal-")||v.upgrade.id.equals("toxic-blowpipe")||v.upgrade.id.equals("avernic-treads")));
		assertTrue(all.stream().filter(v->v.upgrade.id.equals("zaryte-vambraces")).findFirst().orElseThrow().comparison.available(THEORETICAL_DPS));
		Advisor.UpgradeView optional=rosewood(a);
		assertEquals(PursuitAssessment.Category.DEFER,optional.pursuit.category);
		assertTrue(optional.card.reason.contains("scale-free")); assertTrue(optional.valueEvidence.specialist);
		PursuitOverview overview=new PursuitOverview(all,List.of(),List.of(),List.of());
		assertTrue(overview.all.stream().anyMatch(v->v.upgrade.id.equals(optional.upgrade.id)));
		assertFalse(overview.preview.stream().anyMatch(v->v.upgrade.id.equals(optional.upgrade.id)));
		assertTrue(overview.preview.stream().noneMatch(v->v.pursuit.category==PursuitAssessment.Category.DEFER));
	}
	@Test public void observedStrongerBlowpipeExplainsRosewoodEvenWhenAnotherSlotIsUnknown()
	{
		AccountSnapshot a=equipped(Map.of("weapon",28688,"feet",99999),11230);
		Advisor.UpgradeView optional=rosewood(a);
		assertFalse(optional.comparison.available(THEORETICAL_DPS)); assertTrue(optional.comparison.reason.contains("99999"));
		assertEquals(PursuitAssessment.Category.DEFER,optional.pursuit.category);
		assertTrue(optional.card.reason.contains("Owned Toxic/Blazing"));
		// Never remap an unrelated ID to the reported boots or erase an unreviewed contribution.
		AccountSnapshot arrowtips=equipped(Map.of("weapon",28688,"feet",31047),11230);
		assertTrue(service.validate(arrowtips,Preferences.EMPTY,service.propose(arrowtips)).contains("31047"));
	}
}
