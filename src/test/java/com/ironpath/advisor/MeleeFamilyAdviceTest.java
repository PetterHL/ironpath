package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.P4aFixtures.*;
import static org.junit.Assert.*;

public class MeleeFamilyAdviceTest
{
	private KnowledgeCatalog catalog; private MeleeComparisonService service;
	@Before public void load() throws Exception { catalog=KnowledgeCatalog.load(new Gson());service=new MeleeComparisonService(catalog); }
	private Preferences preferences() { return R15Fixtures.absent(catalog).confirm("barbarian-training",true,TIME).confirm("barbarian-hasta",true,TIME).confirm("knight-waves",true,TIME).confirm("vardorvis-defeated",true,TIME).confirm("whisperer-defeated",true,TIME); }
	private AccountSnapshot owned(int... more)
	{
		AccountSnapshot a=R15Fixtures.items(MeleeAdviceTest.ordinary(),more);Map<Integer,Long> counts=new HashMap<>();a.bank.value().forEach(id->counts.put(id,1L));
		return new AccountSnapshot(a.levels,a.inventory,a.equipment,a.bank,a.rigour,a.augury,a.facts,a.equipmentSlots,fact(Map.of()),fact(Map.of()),fact(counts));
	}
	private Advisor.UpgradeView view(AccountSnapshot a,Preferences p,String id)
	{ return new Advisor(catalog).upgrades(a,p).stream().filter(v->v.upgrade.id.equals(id)).findFirst().orElseThrow(); }
	@Test public void supportedFamiliesReplaceTheFormerBlanketCompetitorGap()
	{
		for(int id:List.of(26219,22325,28338,22978,24417,29589))
		{
			AccountSnapshot a=owned(id);ComparisonResult c=service.compare(a,preferences(),service.propose(a,preferences()),12006);
			assertTrue(c.reason,c.available(ComparisonResult.Metric.THEORETICAL_DPS));assertFalse(c.reason.contains("competing melee family not modelled"));
			assertFalse(c.available(ComparisonResult.Metric.TRIP_PERFORMANCE));assertFalse(c.available(ComparisonResult.Metric.EXPECTED_KILL_TIME));
		}
	}
	@Test public void targetAndTaskChangesReconsiderOwnedPackagesWithoutErasingFocusedCorrections()
	{
		AccountSnapshot a=owned(26219,22978,12018,11865);Preferences p=preferences();
		CombatSetup neutral=service.propose(a,p),vorkath=neutral.edit("target","vorkath-melee");
		ComparisonResult changed=service.compare(a,p,vorkath,12006);
		assertEquals(Integer.valueOf(12018),changed.beforeSetup.slots.get("neck"));
		CombatSetup strongest=service.propose(a,p,"vorkath-melee");
		assertEquals(strongest.slots,changed.beforeSetup.slots);
		CombatSetup focused=vorkath.edit("neck","6585");
		assertEquals(Integer.valueOf(6585),service.compare(a,p,focused,12006).beforeSetup.slots.get("neck"));
		CombatSetup slash=vorkath.edit("weapon","26219").edit("style","SLASH_AGGRESSIVE");
		assertEquals(MeleeModel.Style.SLASH_AGGRESSIVE,service.compare(a,p,slash,12006).beforeSetup.meleeStyle);
	}
	@Test public void anOffhandCandidateCannotDisappearFromATwoHandedOnlyPackage()
	{
		AccountSnapshot a=account(Set.of(22325,26382,26384,26386,22981,29801,31091),99,99);
		ComparisonResult c=service.compare(a,preferences(),service.propose(a,preferences()),22322);
		assertFalse(c.available(ComparisonResult.Metric.THEORETICAL_DPS));assertTrue(c.reason.contains("compatible candidate"));
	}
	@Test public void storedReferenceControlsFollowTheReconsideredOwnedPackage()
	{
		AccountSnapshot a=owned(22325);MeleeReference reference=new MeleeReference();reference.update(a,preferences(),catalog);
		assertTrue(reference.edit("target","duke-melee",a,preferences(),catalog));CombatSetup changed=reference.update(a,preferences(),catalog);
		assertEquals(Integer.valueOf(22325),changed.slots.get("weapon"));assertEquals(Integer.valueOf(0),changed.slots.get("shield"));
		assertTrue(reference.edit("style","SLASH_AGGRESSIVE",a,preferences(),catalog));assertNull(service.validate(a,preferences(),reference.update(a,preferences(),catalog)));
	}
	@Test public void blackMaskNeedsItsCombatRequirementAndUnknownContributingSkillsStayUnknown()
	{
		AccountSnapshot a=account(Set.of(1333,8921),1,1);
		for(var entry:Map.of("ATTACK",40,"STRENGTH",20,"DEFENCE",10,"HITPOINTS",10,"PRAYER",1,"RANGED",1).entrySet()) { a=levels(a,entry.getKey(),entry.getValue()); }
		assertFalse(service.referenceChoices(a).get("head").containsKey("8921"));
		ComparisonResult c=service.compare(a,Preferences.EMPTY,service.propose(a).edit("head","8921"),1333);
		assertFalse(c.available(ComparisonResult.Metric.THEORETICAL_DPS));assertTrue(c.reason.contains("combat level 40"));
		Map<String,Integer> skills=new HashMap<>(a.levels.value());skills.remove("HITPOINTS");
		AccountSnapshot unread=new AccountSnapshot(fact(skills),a.inventory,a.equipment,a.bank,a.rigour,a.augury,a.facts);
		assertTrue(service.compare(unread,Preferences.EMPTY,service.propose(unread).edit("head","8921"),1333).reason.contains("unread"));
	}
	@Test public void effectAwarePackageSearchKeepsSalveAndCurrentInquisitorAlternatives()
	{
		AccountSnapshot a=owned(24417,24419,24420,24421,26382,26384,26386);Preferences p=preferences();
		CombatSetup selected=service.propose(a,p,"dharok");Map<Integer,MeleeItem> items=catalog.melee.items.stream().collect(java.util.stream.Collectors.toMap(i->i.id,i->i));
		TargetProfile t=catalog.comparison.targets.stream().filter(x->x.id.equals("dharok")).findFirst().orElseThrow();double best=0;
		for(int head:List.of(10828,26382,24419))for(int body:List.of(10551,26384,24420))for(int legs:List.of(11834,26386,24421))
			for(MeleeModel.Style style:items.get(24417).styles)
			{ CombatSetup s=selected.edit("weapon","24417").edit("head",Integer.toString(head)).edit("body",Integer.toString(body)).edit("legs",Integer.toString(legs)).edit("style",style.name());best=Math.max(best,MeleeModel.calculate(90,90,s,t,items).dps); }
		assertTrue(MeleeModel.calculate(90,90,selected,t,items).dps>=best-1e-12);
		AccountSnapshot dragon=owned(22978,12018);assertEquals(Integer.valueOf(12018),service.propose(dragon,p,"vorkath-melee").slots.get("neck"));
	}
	@Test public void ownedClawHastaAndLeatherBypassFreshHydraWorkButNotAssemblyUnlocks()
	{
		int claw=catalog.itemSets.get("hydraClaw").get(0),leather=catalog.itemSets.get("hydraLeather").get(0);
		AccountSnapshot a=owned(claw,11889,leather);a=R15Fixtures.withFact(a,"dragon-slayer-ii",fact(true));
		// Preserve the independently observed component counts when changing a level.
		Map<String,Integer> levels=new HashMap<>(a.levels.value());levels.put("SLAYER",1);
		a=new AccountSnapshot(fact(levels),a.inventory,a.equipment,a.bank,a.rigour,a.augury,a.facts,a.equipmentSlots,a.inventoryQuantities,a.equipmentQuantities,a.bankQuantities);
		Advisor.UpgradeView lance=view(a,preferences(),"dragon-hunter-lance"),gloves=view(a,preferences(),"ferocious-gloves");
		assertTrue(lance.pursuit.blockers.toString(),lance.pursuit.blockers.stream().noneMatch(s->s.contains("95") || s.contains("Hydra assignment")));
		assertTrue(gloves.pursuit.blockers.toString(),gloves.pursuit.blockers.stream().noneMatch(s->s.contains("95") || s.contains("Hydra assignment")));
		Advisor.UpgradeView locked=view(a,preferences().confirm("barbarian-training",false,TIME),"dragon-hunter-lance");
		assertTrue(locked.pursuit.blockers.stream().anyMatch(s->s.contains("Barbarian")));
	}
	@Test public void freshLanceStillShowsHydraRequirementsAndSkippingDoesNotCreateOwnership()
	{
		Advisor.UpgradeView fresh=view(owned(),preferences(),"dragon-hunter-lance");
		assertTrue(fresh.pursuit.blockers.toString(),fresh.pursuit.blockers.stream().anyMatch(s->s.contains("95") || s.contains("Hydra")));
		assertTrue(new Advisor(catalog).upgrades(owned(),preferences().skip("hydra",true)).stream().noneMatch(v->v.upgrade.id.equals("dragon-hunter-lance")));
	}
	@Test public void ownedTreadsOnlyOfferTheMissingPrimordialAdditionAndRetainEquipGaps()
	{
		AccountSnapshot a=owned(31088,13239);Advisor.UpgradeView addition=view(a,preferences(),"avernic-treads-primordial");
		assertTrue(addition.comparison.available(ComparisonResult.Metric.THEORETICAL_DPS));assertTrue(addition.upgrade.remainingWork.contains("4,000"));
		assertEquals(PursuitAssessment.Mode.BOUNDED,addition.pursuit.acquisitionMode);
		assertTrue(addition.pursuit.blockers.stream().noneMatch(s->s.contains("Doom")));
		for(int form:List.of(31091,31094,31095,31097))
		{ assertTrue(new Advisor(catalog).upgrades(owned(form),preferences()).stream().noneMatch(v->v.upgrade.id.equals("avernic-treads-primordial"))); }
		AccountSnapshot low=levels(owned(31088),"DEFENCE",70);
		assertFalse(view(low,preferences(),"avernic-treads-primordial").comparison.available(ComparisonResult.Metric.THEORETICAL_DPS));
		assertTrue(new Advisor(catalog).upgrades(low,preferences()).stream().noneMatch(v->v.upgrade.id.equals("avernic-treads")));
	}
	@Test public void finishedCosmeticsAndNearlyCompleteAccountsDoNotGetDuplicateGrinds()
	{
		for(String id:List.of("torva-head","torva-body","torva-legs","amulet-of-rancour","scythe-of-vitur","soulreaper-axe","osmumtens-fang"))
		{
			KnowledgeCatalog.Upgrade u=catalog.upgrades.stream().filter(x->x.id.equals(id)).findFirst().orElseThrow();
			for(int form:catalog.itemSets.get(u.ownedWhen.key))
			{ assertTrue(id+"/"+form,new Advisor(catalog).upgrades(owned(form),preferences()).stream().noneMatch(v->v.upgrade.id.equals(id))); }
		}
		Set<Integer> all=catalog.upgrades.stream().map(u->u.itemId).collect(java.util.stream.Collectors.toSet());all.add(4151);
		assertTrue(new Advisor(catalog).upgrades(account(all,99,99),preferences()).isEmpty());
	}
	@Test public void soulScenariosAndSuppliesNeverBecomePersonalTimeOrTripClaims()
	{
		AccountSnapshot a=owned();CombatSetup reference=service.propose(a,preferences()).edit("souls","RAMP");
		ComparisonResult ramp=service.compare(a,preferences(),reference,28338),retained=service.compare(a,preferences(),reference.edit("souls","RETAINED"),28338);
		assertTrue(ramp.reason.contains("five-swing"));assertTrue(retained.after.dps>ramp.after.dps);
		assertFalse(ramp.available(ComparisonResult.Metric.EXPECTED_KILL_TIME));assertFalse(retained.available(ComparisonResult.Metric.TRIP_PERFORMANCE));
		assertTrue(ramp.assumptions.stream().anyMatch(s->s.contains("no HP drain")));
	}
	@Test public void bloodFuryRetainsIndependentSustainValueDespiteALowerOffensiveMetric()
	{
		AccountSnapshot a=owned(19553);Advisor.UpgradeView sustain=view(a,preferences(),"amulet-of-blood-fury");
		assertTrue(sustain.comparison.available(ComparisonResult.Metric.THEORETICAL_DPS));assertTrue(sustain.comparison.dpsGain()<0);
		assertTrue(sustain.valueEvidence.independentUtility);assertTrue(sustain.card.metric.contains("Resource utility"));
		assertFalse(sustain.comparison.available(ComparisonResult.Metric.TRIP_PERFORMANCE));
		assertTrue(new Advisor(catalog).upgrades(owned(24780),preferences()).stream().noneMatch(v->v.upgrade.id.equals("amulet-of-blood-fury")));
	}
	@Test public void partialOathplateStockChangesOnlyRemainingProductionAmounts()
	{
		int plate=catalog.itemSets.get("infernalPlate").get(0),shards=catalog.itemSets.get("oathplateShards").get(0),shale=catalog.itemSets.get("infernalShale").get(0);
		AccountSnapshot a=owned(plate,shards,shale);Map<Integer,Long> counts=new HashMap<>(a.bankQuantities.value());counts.put(plate,8L);counts.put(shards,50L);counts.put(shale,280L);
		Map<String,Integer> skills=new HashMap<>(a.levels.value());skills.put("SMITHING",83);
		a=new AccountSnapshot(fact(skills),a.inventory,a.equipment,a.bank,a.rigour,a.augury,a.facts,a.equipmentSlots,a.inventoryQuantities,a.equipmentQuantities,fact(counts));
		Advisor.UpgradeView v=view(a,preferences(),"oathplate-head");
		assertTrue(v.path.stream().anyMatch(r->r.label.contains("50 / 50 observed") && r.truth==com.ironpath.model.Truth.TRUE));
		assertTrue(v.path.stream().anyMatch(r->r.label.contains("280 / 280 observed") && r.truth==com.ironpath.model.Truth.TRUE));
		assertEquals(PursuitAssessment.Mode.BOUNDED,v.pursuit.acquisitionMode);
		AccountSnapshot unread=new AccountSnapshot(a.levels,a.inventory,a.equipment,a.bank,a.rigour,a.augury,a.facts);
		assertTrue(view(unread,preferences(),"oathplate-head").path.stream().anyMatch(r->r.label.contains("shards for missing") && r.truth==com.ironpath.model.Truth.UNKNOWN && r.evidence.contains("unread")));
	}
	@Test public void developedAccountContextMatrixChangesOwnedChoiceWithoutUniversalFamilyOrdering() throws Exception
	{
		AccountSnapshot a=owned(26219,22325,28338,22978,24417,29796,22324,26382,26384,26386,24419,24420,24421,22981,31091,28307,28316,29801,22322,12018);
		Preferences p=preferences();List<String> rows=new java.util.ArrayList<>();Set<Integer> choices=new java.util.HashSet<>();
		rows.add("# Original R3b developed-account scenarios\n\nSynthetic complete owned kit; no player measurements, goal, actual raid scaling, specs or time/trip claims.\n\n| Context | Soul state | Owned weapon | DPS | Tentacle delta |\n|---|---|---|---|---|");
		for(String target:List.of("neutral-zero-defence","dharok","graardor","vorkath-melee","duke-melee","olm-melee-scenario","akkha-melee-scenario","baba-melee-scenario"))
			for(String souls:List.of("START","RAMP","RETAINED"))
			{
				CombatSetup seed=service.propose(a,p,target).edit("souls",souls);
				ComparisonResult c=service.compare(a,p,seed,12006);assertTrue(c.reason,c.available(ComparisonResult.Metric.THEORETICAL_DPS));
				choices.add(c.beforeSetup.slots.get("weapon"));
				rows.add(String.format(java.util.Locale.ROOT,"| %s | %s | %d | %.4f | %+.4f |",target,souls,c.beforeSetup.slots.get("weapon"),c.before.dps,c.dpsGain()));
			}
		assertTrue("Different contexts must retain different adequate strongest-owned families",choices.size()>1);
		AccountSnapshot team=R15Fixtures.withFact(owned(26219,12899,25865,23971,23975,23979),"cox-preparation",fact(true));
		Advisor.UpgradeView lance=view(team,p,"dragon-hunter-lance");assertTrue(lance.contextComparisons.stream().anyMatch(c->c.target.id.equals("olm-melee-scenario") && c.available(ComparisonResult.Metric.THEORETICAL_DPS)));
		rows.add("\nPI01: owned Fang retained; lance has supported fixed-input dragon value and its separate substantial Hydra/hasta route. Team debuff effectiveness/readiness is not inferred.\nPI06: existing ranged zenyte/anguish assertions remain independent of melee support.\nPI09/PI10: supported finished forms suppress their own acquisition; specials, multi-target and sustain coverage remain independent gaps.\nPI13: matrix above varies size, type, dragon/demon status and soul state; the Olm Defence override assertion is separate.\nPI14: owned treads bypass new Doom acquisition; only missing primordial addition and its equip/resources remain in the dedicated test.");
		java.nio.file.Path path=java.nio.file.Path.of("build/reports/r3b/family-accounts.md");java.nio.file.Files.createDirectories(path.getParent());java.nio.file.Files.write(path,rows,java.nio.charset.StandardCharsets.UTF_8);
	}
}
