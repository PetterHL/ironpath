package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.preferences.Preferences;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.ComparisonResult.Metric.*;
import static com.ironpath.advisor.P4aFixtures.*;
import static com.ironpath.advisor.R15Fixtures.*;
import static org.junit.Assert.*;

public class R2AdviceTest
{
	private KnowledgeCatalog catalog; private Advisor advisor; private RangedComparisonService service; private Preferences missing;
	@Before public void load() throws Exception { catalog=KnowledgeCatalog.load(new Gson());advisor=new Advisor(catalog);service=new RangedComparisonService(catalog);missing=absent(catalog); }
	public static AccountSnapshot postBowfa()
	{
		AccountSnapshot a=items(developed(false),21326,892,811,21330,2503,2497,10499);
		return withFact(withFact(a,"blood-moon-rises",fact(true)),"dragon-slayer-ii",fact(true));
	}
	private Advisor.UpgradeView view(AccountSnapshot a,String id)
	{ return advisor.upgrades(a,missing).stream().filter(v->v.upgrade.id.equals(id)).findFirst().orElseThrow(); }
	@Test public void postBowfaKeepsCrystalAndSpecialistTbowWithoutPrioritisingItsGrind()
	{
		AccountSnapshot a=postBowfa();CombatSetup baseline=service.propose(a);
		assertNull(service.validate(a,missing,baseline));assertEquals(Integer.valueOf(25867),baseline.slots.get("weapon"));
		assertEquals(Integer.valueOf(23975),baseline.slots.get("body"));
		Advisor.UpgradeView masori=view(a,"masori-body");assertTrue(masori.comparison.dpsGain()<0);assertEquals(PursuitAssessment.Category.DEFER,masori.pursuit.category);
		Advisor.UpgradeView tbow=view(a,"twisted-bow");assertTrue(tbow.comparison.dpsGain()<0);
		assertTrue(tbow.contextComparisons.stream().anyMatch(r->r.target.id.equals("zilyana")&&r.dpsGain()>0));
		assertEquals(PursuitAssessment.Category.LONG_TERM,tbow.pursuit.category);
		assertFalse(new PursuitOverview(advisor.upgrades(a,missing),List.of(),List.of(),List.of()).pursuits.stream().anyMatch(v->v.upgrade.id.equals("twisted-bow")));
	}
	@Test public void tbowOwnershipLeavesBowfaAndFastLightAttackNichesVisible()
	{
		AccountSnapshot a=items(postBowfa(),20997,11212,27226,27229,27232);
		assertFalse(advisor.upgrades(a,missing).stream().anyMatch(v->v.upgrade.id.equals("twisted-bow")));
		assertEquals(Integer.valueOf(20997),service.propose(a,"zilyana").slots.get("weapon"));
		assertEquals(Integer.valueOf(25867),service.propose(a).slots.get("weapon"));
		Advisor.UpgradeView pipe=view(a,"toxic-blowpipe");assertTrue(pipe.comparison.dpsGain()>0);assertFalse(pipe.value.isBlank());
		AccountSnapshot noBowfa=withFact(account(Set.of(20997,11212,27226,27229,27232,19547),99,99),"song-of-elves",fact(true));
		assertTrue(advisor.upgrades(noBowfa,missing).stream().anyMatch(v->v.upgrade.id.equals("bowfa")));
	}
	@Test public void ownedJewelleryAndFortifiedArmourAreStrongAlternativeFacts()
	{
		AccountSnapshot a=items(postBowfa(),33639,27238);
		assertFalse(advisor.upgrades(a,missing).stream().anyMatch(v->v.upgrade.id.equals("necklace-of-anguish")||v.upgrade.id.equals("necklace-of-rupture")||v.upgrade.id.equals("masori-body")));
		assertEquals(Integer.valueOf(33639),service.propose(a).slots.get("neck"));
		ComparisonResult base=service.compare(postBowfa(),missing,service.propose(postBowfa()),27229);
		ComparisonResult fortified=service.compare(postBowfa(),missing,service.propose(postBowfa()),27238);
		assertEquals(base.after.maxHit,fortified.after.maxHit);assertEquals(base.after.dps,fortified.after.dps,1e-12);
	}
	@Test public void packagesUseOwnedAmmoAndCandidateOnlySuppliesStayExplicit()
	{
		AccountSnapshot a=items(postBowfa(),12926,11230,31583);CombatSetup setup=service.propose(a);
		assertEquals(Integer.valueOf(12926),setup.slots.get("weapon"));assertEquals(11230,setup.loadedAmmo);
		assertTrue(setup.provenance.get("loaded-ammo").contains("proposed loading"));
		assertNull(service.validate(a,missing,setup));
		CombatSetup bad=setup.edit("weapon","31583").edit("loaded-ammo","21330");
		assertTrue(service.validate(a,missing,bad).contains("invalid"));assertFalse(service.compare(a,missing,bad,19547).available(THEORETICAL_DPS));
		AccountSnapshot empty=account(Set.of(12924),99,99);assertFalse(service.compare(empty,missing,service.propose(empty),19547).available(THEORETICAL_DPS));
		AccountSnapshot noDarts=postBowfa();ComparisonResult candidate=service.compare(noDarts,missing,service.propose(noDarts),12926);
		assertTrue(candidate.available(THEORETICAL_DPS));assertTrue(candidate.assumptions.stream().anyMatch(s->s.contains("loading")));
	}
	@Test public void offHandCompatibilityAndTargetDefenceTypeAreVisible()
	{
		AccountSnapshot a=items(postBowfa(),9185,9144,21000);CombatSetup s=service.propose(a,"zilyana").edit("weapon","9185").edit("ammo","9144").edit("shield","21000");
		assertNull(service.validate(a,missing,s));ComparisonResult r=service.compare(a,missing,s,20997);
		assertEquals(Integer.valueOf(0),r.afterSetup.slots.get("shield"));assertNotEquals(r.before.defenceRoll,r.after.defenceRoll);
		assertEquals((300+9L)*(75+64),r.before.defenceRoll);assertEquals((300+9L)*(100+64),r.after.defenceRoll);
		assertFalse(service.compare(a,missing,service.propose(a),21000).available(THEORETICAL_DPS));
	}
	@Test public void ownedIntermediateAndQuantityRemoveOnlyTheirRemainingAcquisitionStage()
	{
		AccountSnapshot a=items(postBowfa(),19535);
		Advisor.UpgradeView necklace=view(a,"necklace-of-anguish");assertEquals(PursuitAssessment.Mode.BOUNDED,necklace.pursuit.acquisitionMode);
		assertFalse(necklace.pursuit.blockers.stream().anyMatch(s->s.contains("Zenyte shard")||s.contains("Monkey Madness")));
		assertFalse(advisor.activityOpportunities(a,missing,advisor.upgrades(a,missing)).stream().filter(o->o.activity.equals("demonic-gorillas")).anyMatch(o->o.usefulFamilies.contains("necklace-of-anguish")));
		AccountSnapshot seed=withQuantity(items(postBowfa(),25859),23962,150);
		// An account with an inactive bow still owns its outcome, but an owned seed does not establish usable gear.
		AccountSnapshot raw=withFact(account(Set.of(9185,9144,25859),99,99),"song-of-elves",fact(true));
		raw=new AccountSnapshot(raw.levels,raw.inventory,raw.equipment,raw.bank,raw.rigour,raw.augury,raw.facts,raw.equipmentSlots,raw.inventoryQuantities,raw.equipmentQuantities,fact(Map.of(23962,150L)));
		assertEquals(PursuitAssessment.Mode.BOUNDED,view(raw,"bowfa").pursuit.acquisitionMode);
		assertEquals(Integer.valueOf(9185),service.propose(raw).slots.get("weapon"));
		assertFalse(advisor.upgrades(seed,missing).stream().anyMatch(v->v.upgrade.id.equals("bowfa")));
	}
	@Test public void unknownGearAndManualOwnershipCannotBecomeASuppliedBaseline()
	{
		AccountSnapshot empty=account(Set.of(),99,99);Preferences manual=missing.confirm("owns-toxicBlowpipe",true,TIME);
		assertFalse(service.compare(empty,manual,service.propose(empty),19547).available(THEORETICAL_DPS));
		AccountSnapshot a=postBowfa();Map<String,Integer> worn=RangedModelTest.slots(25867);worn.put("body",999999);
		AccountSnapshot unknown=new AccountSnapshot(a.levels,a.inventory,fact(Set.of(25867,999999)),a.bank,a.rigour,a.augury,a.facts,fact(worn),a.inventoryQuantities,a.equipmentQuantities,a.bankQuantities);
		// Observed crystal body is an explicitly supported replacement, rather than unknown being zero.
		assertEquals(Integer.valueOf(23975),service.propose(unknown).slots.get("body"));
		CombatSetup selected=service.propose(unknown).edit("body","999999");assertTrue(service.validate(unknown,missing,selected).contains("unknown"));
	}
	@Test public void currentBlowpipeCraftingAndOwnedComponentsDoNotRepeatTheGrind()
	{
		AccountSnapshot a=items(postBowfa(),12922);Advisor.UpgradeView pipe=view(a,"toxic-blowpipe");
		assertEquals(PursuitAssessment.Mode.BOUNDED,pipe.pursuit.acquisitionMode);
		assertTrue(pipe.path.stream().anyMatch(r->r.label.contains("78 Fletching")));
		assertFalse(pipe.pursuit.blockers.stream().anyMatch(s->s.contains("Obtain tanzanite")));
		assertFalse(pipe.comparison.beforeSetup.slots.containsValue(12926));
		Advisor.UpgradeView cape=view(items(postBowfa(),21907),"avas-assembler");
		assertEquals(PursuitAssessment.Mode.BOUNDED,cape.pursuit.acquisitionMode);
		assertFalse(cape.pursuit.blockers.stream().anyMatch(s->s.contains("Obtain Vorkath")));
		assertNull(cape.pursuit.estimate);
	}
	@Test public void candidateWeaponsCompareACompleteOwnedVoidAlternative()
	{
		AccountSnapshot a=items(postBowfa(),11664,13072,13073,8842,27226,27229,27232,9144,21905);
		ComparisonResult result=service.compare(a,missing,service.propose(a),11785);
		assertTrue(result.available(THEORETICAL_DPS));
		assertEquals(Integer.valueOf(11664),result.afterSetup.slots.get("head"));
		assertEquals(Integer.valueOf(8842),result.afterSetup.slots.get("hands"));
		assertTrue(result.assumptions.stream().anyMatch(s->s.contains("complete Void")));
	}
	@Test public void sharedRaidFamiliesShrinkAndOwnedOutcomesDoNotPadResults()
	{
		AccountSnapshot a=items(postBowfa(),27275,12002,19544,21018,21024);
		// Explicit high-defence magic context makes the remaining robe useful; ranged retains its automatic neutral reference.
		List<Advisor.UpgradeView> views=advisor.upgrades(a,missing,new PoweredMagic(catalog).propose(a,"graardor"));
		ActivityOpportunity cox=advisor.activityOpportunities(a,missing,views).stream().filter(o->o.activity.equals("chambers-of-xeric")).findFirst().orElseThrow();
		assertTrue(cox.usefulFamilies.contains("twisted-bow"));assertEquals(cox.usefulFamilies.toString(),1,cox.usefulFamilies.stream().filter(f->f.equals("ancestral")).count());
		AccountSnapshot all=items(a,catalog.upgrades.stream().mapToInt(u->u.itemId).toArray());assertTrue(advisor.upgrades(all,missing).isEmpty());
		assertFalse(advisor.upgrades(a,missing.skip("chambers-of-xeric",true)).stream().anyMatch(v->v.upgrade.id.equals("twisted-bow")));
	}
	@Test public void referenceConfirmationInvalidatesOnAmmoPrayerAndAccountChanges()
	{
		AccountSnapshot a=postBowfa();RangedReference state=new RangedReference();state.update(a,missing,catalog);
		assertTrue(state.edit("confirm","true",a,missing,catalog));assertTrue(state.update(a,missing,catalog).confirmed);
		AccountSnapshot changed=items(a,33589);assertFalse(state.update(changed,missing,catalog).confirmed);
		assertTrue(state.edit("prayer","RIGOUR",changed,missing,catalog));assertFalse(state.edit("confirm","true",changed,missing,catalog));
		state.clear();AccountSnapshot other=account(Set.of(9185,9144),90,99);assertEquals(Integer.valueOf(9185),state.update(other,missing,catalog).slots.get("weapon"));
	}
	@Test public void representativeProfilesGenerateReviewableCurrentEvidence() throws Exception
	{
		Map<String,AccountSnapshot> profiles=new java.util.LinkedHashMap<>();profiles.put("Post-Bowfa",postBowfa());profiles.put("Tbow owned",items(postBowfa(),20997,11212,27226,27229,27232));profiles.put("Tbow and blowpipe",items(profiles.get("Tbow owned"),12926,11230));profiles.put("Shadow owned",items(postBowfa(),27275));profiles.put("Owned zenyte necklace",items(postBowfa(),19535));profiles.put("All active outcomes owned",items(postBowfa(),catalog.upgrades.stream().mapToInt(u->u.itemId).toArray()));
		StringBuilder report=new StringBuilder("# R2 synthetic account evidence\n\nOriginal synthetic inputs, no goal. Model ranged-1 / powered-magic-2; knowledge r2-1; pursuit-1. Not a live-client or player-usefulness pass. Exact source revisions remain in the bundled catalogue.\n\n");
		for(Map.Entry<String,AccountSnapshot> entry:profiles.entrySet())
		{
			AccountSnapshot a=entry.getValue();List<Advisor.UpgradeView> views=advisor.upgrades(a,missing);
			assertEquals(views.stream().map(v->v.upgrade.id+v.pursuit.category).collect(Collectors.toList()),advisor.upgrades(a,missing).stream().map(v->v.upgrade.id+v.pursuit.category).collect(Collectors.toList()));
			report.append("## ").append(entry.getKey()).append("\n\nObserved synthetic bank: ").append(new java.util.TreeSet<>(a.bank.value())).append(". Levels: ").append(new java.util.TreeMap<>(a.levels.value())).append(". Equipment explicitly empty; supplies/loadout are reference assumptions.\n\n");
			if(views.isEmpty()){report.append("No remaining active outcome; no padding. Comprehensive coverage remains open.\n\n");}
			for(Advisor.UpgradeView v:views)
			{
				report.append("- **").append(v.upgrade.title).append("** — ").append(v.pursuit.category.label).append(". ").append(v.comparison.summary()).append("\n  Priority: ").append(v.pursuit.reasons).append("\n  Next step: ").append(v.routes.get(0).next).append("\n");
				for(ComparisonResult r:v.contextComparisons){report.append("  Separate encounter: ").append(r.summary()).append("\n");}
			}
			report.append("\n");
		}
		java.nio.file.Path path=java.nio.file.Path.of("build/reports/r2/scenarios.md");java.nio.file.Files.createDirectories(path.getParent());java.nio.file.Files.writeString(path,report);
	}
}
