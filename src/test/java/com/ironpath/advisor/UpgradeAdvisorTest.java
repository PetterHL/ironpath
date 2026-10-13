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
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.P4aFixtures.*;
import static org.junit.Assert.*;

public class UpgradeAdvisorTest
{
	private KnowledgeCatalog catalog;
	private Advisor advisor;
	@Before public void setup() throws Exception { catalog=KnowledgeCatalog.load(new Gson());advisor=new Advisor(catalog); }
	private List<Advisor.UpgradeView> views(AccountSnapshot a,Preferences p){return advisor.upgrades(a,p);}
	private List<String> ids(AccountSnapshot a,Preferences p){return views(a,p).stream().map(v->v.upgrade.id).collect(Collectors.toList());}
	private Advisor.UpgradeView view(AccountSnapshot a,Preferences p,String id){return views(a,p).stream().filter(v->v.upgrade.id.equals(id)).findFirst().orElseThrow();}

	@Test public void noGoalHasAccountSpecificMidgameAndPostBowfaOutcomes()
	{
		AccountSnapshot mid=account(Set.of(11907,6585),75,80);
		assertTrue(ids(mid,Preferences.EMPTY).containsAll(List.of("swamp-trident","occult-necklace","tumekens-shadow")));
		assertTrue(view(mid,Preferences.EMPTY,"occult-necklace").label.contains("Plan ahead"));
		assertTrue(view(mid,Preferences.EMPTY,"occult-necklace").routes.get(0).next.contains("SLAYER >= 93"));
		assertTrue(view(mid,Preferences.EMPTY,"swamp-trident").equip.stream().anyMatch(r->r.truth==com.ironpath.model.Truth.FALSE));
		AccountSnapshot post=account(Set.of(11907,6585,25867,23975,23979,23971,11840),90,90);
		assertTrue(ids(post,Preferences.EMPTY).containsAll(List.of("swamp-trident","occult-necklace","avernic-treads")));
		assertFalse(ids(post,Preferences.EMPTY).contains("zaryte-crossbow"));
	}

	@Test public void positiveInventoryWithUnreadOrEmptyBankSupportsProvisionalButNeverMissingOwnership()
	{
		AccountSnapshot base=account(Set.of(),90,90);
		AccountSnapshot unread=new AccountSnapshot(base.levels,fact(Set.of(11907)),base.equipment,Observation.unknown(),base.rigour,base.augury,base.facts);
		for(AccountSnapshot a:List.of(unread,new AccountSnapshot(base.levels,unread.inventory,base.equipment,base.bank,base.rigour,base.augury,base.facts)))
		{
			assertTrue(ids(a,Preferences.EMPTY).containsAll(List.of("swamp-trident","occult-necklace","tumekens-shadow")));
			for(Advisor.UpgradeView v:views(a,Preferences.EMPTY)) { assertTrue(v.label.contains("Ownership unconfirmed"));assertFalse(v.questions.isEmpty());assertFalse(v.label.contains("missing")); }
		}
		assertTrue(ids(base,Preferences.EMPTY).isEmpty()); // No evidenced baseline: no shopping list.
	}

	@Test public void confirmClearAndObservedManualPositivesKeepTheirPrecedence()
	{
		AccountSnapshot a=account(Set.of(11907),90,90);
		Preferences p=Preferences.EMPTY.confirm("owns-occult",false,TIME);
		assertTrue(view(a,p,"occult-necklace").label.contains("Planning gap confirmed"));
		assertTrue(view(a,p.confirm("owns-occult",null,TIME),"occult-necklace").label.contains("Ownership unconfirmed"));
		assertFalse(ids(a,p.confirm("item-19720",true,TIME)).contains("occult-necklace"));
		assertFalse(ids(account(Set.of(11907,12002),90,90),p).contains("occult-necklace"));
		Preferences advanced=Preferences.EMPTY.confirm("item-27277",true,TIME);
		assertFalse(ids(a,advanced).contains("swamp-trident"));
		assertFalse(ids(a,Preferences.EMPTY.confirm("owns-competingMagic",true,TIME)).contains("swamp-trident"));
		AccountSnapshot parts=account(Set.of(11907,12932,1755),90,90);
		assertFalse(advisor.quickActions(parts,Preferences.EMPTY.confirm("owns-competingMagic",true,TIME))
			.stream().anyMatch(r -> r.decision.id.equals("assemble-swamp-trident")));
		assertEquals(1,view(a,Preferences.EMPTY,"swamp-trident").questions.size());
		assertEquals(PoweredMagic.Status.UNSUPPORTED,new PoweredMagic(catalog).compare(account(Set.of(),90,90),advanced,new PoweredMagic(catalog).propose(account(Set.of(),90,90)),"OCCULT").status);
	}

	@Test public void ownedEquivalentFormsSuppressOnlyTheirAcquisition()
	{
		for(int id:new int[]{12899,12900,22292,22294,22323,27277,31115})
		{ assertFalse(ids(account(Set.of(11907,id),90,90),Preferences.EMPTY).contains("swamp-trident")); }
		for(int id:new int[]{31088,31091,31092,31093,31094,31095,31096,31097})
		{ assertFalse(ids(account(Set.of(11907,11840,id),90,90),Preferences.EMPTY).contains("avernic-treads")); }
		assertFalse(ids(account(Set.of(11785,26374),90,90),Preferences.EMPTY).contains("zaryte-crossbow"));
	}

	@Test public void advancedRangedOwnershipAloneDoesNotProveOrRemoveSpecialistValue()
	{
		assertFalse(ids(account(Set.of(20997),99,99),Preferences.EMPTY).contains("zaryte-crossbow"));
		assertFalse(ids(account(Set.of(25867,23975,23979,23971),99,99),Preferences.EMPTY).contains("zaryte-crossbow"));
		AccountSnapshot a=account(Set.of(20997,11785,13576),99,99);
		Advisor.UpgradeView specialist=view(a,Preferences.EMPTY,"zaryte-crossbow");
		assertTrue(specialist.upgrade.limitation.contains("successful hit"));assertTrue(specialist.upgrade.limitation.contains("110 is not a guaranteed hit"));
		assertTrue(specialist.routes.get(0).next.contains("The Frozen Door"));
		assertEquals(KnowledgeCatalog.Benefit.MINOR,specialist.band);
		assertEquals(KnowledgeCatalog.Benefit.MODERATE,view(a,Preferences.EMPTY.confirm("zaryte-context",true,TIME),"zaryte-crossbow").band);
		assertFalse(ids(a,Preferences.EMPTY.skip("nex",true)).contains("zaryte-crossbow"));
	}

	@Test public void marginalMagicBootCaseIsExplicitlyDeferredAndMeleeOnlyPrimordialDoesNotRecommend()
	{
		Advisor.UpgradeView v=view(account(Set.of(11907,13235),90,90),Preferences.EMPTY,"avernic-treads");
		assertEquals(KnowledgeCatalog.Benefit.MINOR,v.band);assertTrue(v.label.contains("consider deferring"));
		assertTrue(v.value.contains("same 1% magic damage"));
		assertFalse(ids(account(Set.of(4151,13239),90,90),Preferences.EMPTY).contains("avernic-treads"));
		assertTrue(view(account(Set.of(11907,9185,13235),90,90),Preferences.EMPTY,"avernic-treads").applications.size()>1);
	}

	@Test public void blockedTreadsShowQuestEquipAndSeparateSharedRewardEligibilityWithoutAGoal()
	{
		AccountSnapshot a=account(Set.of(11907,9185,11840,19544),90,90);
		Advisor.UpgradeView v=view(a,Preferences.EMPTY,"avernic-treads");
		assertTrue(v.routes.get(0).next.contains("The Final Dawn"));
		assertTrue(v.shared.get(0).contains("delve 2"));assertTrue(v.shared.get(1).contains("delve 3"));
		assertTrue(v.upgrade.limitation.contains("deeper-delve"));
		assertTrue(v.shared.get(0).contains("10,000 demon tears"));
		assertTrue(v.parts.stream().allMatch(r -> r.role.equals("OPTIONAL ADDITION")));
		assertTrue(v.parts.stream().anyMatch(r -> r.label.contains("4,000") && r.truth == com.ironpath.model.Truth.UNKNOWN));
		assertTrue(view(levels(a,"RUNECRAFT",59),Preferences.EMPTY,"avernic-treads").parts.stream()
			.anyMatch(r -> r.label.contains("Runecraft") && r.truth == com.ironpath.model.Truth.FALSE));
		AccountSnapshot advanced=account(Set.of(11907,9185,11840,19544,27275,31106),90,90);
		assertTrue(view(advanced,Preferences.EMPTY,"avernic-treads").shared.isEmpty());
		assertEquals(1,views(a,Preferences.EMPTY).stream().filter(x->x.upgrade.id.equals("avernic-treads")).count());
	}

	@Test public void oneSwampOutcomeGroupsPartsRoutesAndActivitySkipsWithoutHidingOwnedUtility()
	{
		AccountSnapshot a=account(Set.of(11907,12932,1755),90,90);
		Advisor.UpgradeView v=view(a,Preferences.EMPTY,"swamp-trident");
		assertEquals(1,v.routes.size()); // Owned fang suppresses another Zulrah grind.
		assertTrue(v.routes.get(0).available);assertEquals("assemble-swamp-trident",v.routes.get(0).decision.id);
		assertTrue(v.parts.stream().allMatch(r->r.truth==com.ironpath.model.Truth.TRUE));
		assertTrue(ids(a,Preferences.EMPTY.skip("zulrah",true)).contains("swamp-trident"));
		assertTrue(advisor.quickActions(a,Preferences.EMPTY.skip("zulrah",true)).stream().anyMatch(r->r.decision.id.equals("assemble-swamp-trident")));
		AccountSnapshot seas=account(Set.of(11907,9185),90,90);
		Advisor.UpgradeView skipped=view(seas,Preferences.EMPTY.skip("zulrah",true),"swamp-trident");
		assertTrue(skipped.routes.stream().noneMatch(r->r.decision.activity.equals("zulrah")));
		assertTrue(view(seas,Preferences.EMPTY,"swamp-trident").routes.stream().anyMatch(r->r.decision.activity.equals("zulrah")));
	}

	@Test public void accessPreparationAndComponentsStaySeparateAndLiveFactsBeatManualFallback()
	{
		AccountSnapshot a=account(Set.of(11785,26372,26231),90,90);
		Preferences p=Preferences.EMPTY.confirm("frozen-door",true,TIME).confirm("nex-entry",true,TIME).confirm("nex-team",true,TIME);
		assertFalse(view(a,p,"zaryte-crossbow").routes.get(0).available); // Observed quest false beats manual true.
		AccountSnapshot unlocked=facts(a,"frozen-door",fact(true));
		assertTrue(view(unlocked,p,"zaryte-crossbow").routes.get(0).available);
		assertTrue(view(unlocked,p,"zaryte-crossbow").parts.stream().anyMatch(r->r.label.contains("250")&&r.truth==com.ironpath.model.Truth.UNKNOWN));
		AccountSnapshot prepGap=levels(unlocked,"RANGED",79);
		assertTrue(view(prepGap,p,"zaryte-crossbow").equip.stream().anyMatch(r->r.truth==com.ironpath.model.Truth.FALSE));
		Advisor.UpgradeView noTeam=view(unlocked,p.confirm("nex-team",false,TIME),"zaryte-crossbow");
		assertTrue(noTeam.routes.get(0).status.contains("Preparation"));
	}

	@Test public void goalAliasesNeverIncreaseValueAndOptionalGoalsOnlyRefineRelevantBands() throws Exception
	{
		AccountSnapshot a=account(Set.of(11907,9185,11840,11785),90,90);
		List<String> original=ids(a,Preferences.EMPTY);
		JsonObject doc=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/catalog.json"))).getAsJsonObject();
		for(int i=0;i<4;i++)
		{
			JsonObject alias=doc.getAsJsonArray("goals").get(0).getAsJsonObject().deepCopy();alias.addProperty("id","goal-alias-"+(char)('a'+i));doc.getAsJsonArray("goals").add(alias);
			doc.getAsJsonArray("upgrades").get(3).getAsJsonObject().getAsJsonArray("goals").add(alias.get("id"));
		}
		Advisor aliases=new Advisor(KnowledgeCatalog.read(new Gson(),new StringReader(doc.toString())));
		assertEquals(original,aliases.upgrades(a,Preferences.EMPTY).stream().map(v->v.upgrade.id).collect(Collectors.toList()));
		assertEquals(advisor.upgrades(a,Preferences.EMPTY).stream().filter(v->v.upgrade.id.equals("tumekens-shadow")).findFirst().orElseThrow().pursuit.category,advisor.upgrades(a,Preferences.EMPTY.goal("ranged-options")).stream().filter(v->v.upgrade.id.equals("tumekens-shadow")).findFirst().orElseThrow().pursuit.category);
		assertEquals(advisor.upgrades(a,Preferences.EMPTY).stream().filter(v->v.upgrade.id.equals("tumekens-shadow")).findFirst().orElseThrow().pursuit.category,advisor.upgrades(a,Preferences.EMPTY.goal("magic-upgrade")).stream().filter(v->v.upgrade.id.equals("tumekens-shadow")).findFirst().orElseThrow().pursuit.category);
		assertFalse(ids(account(Set.of(20997),99,99),Preferences.EMPTY.goal("toa-entry")).contains("zaryte-crossbow"));
	}

	@Test public void everyOutcomeHasAnInspectablePathAndUnsupportedNumbersPreserveQualitativeAdvice()
	{
		AccountSnapshot a=account(Set.of(11907,9185,11840,11785),90,90);
		assertTrue(views(a,Preferences.EMPTY).size() > 4);
		for(Advisor.UpgradeView v:views(a,Preferences.EMPTY)) { assertFalse(v.routes.isEmpty());assertFalse(v.upgrade.sources.isEmpty());assertFalse(v.upgrade.alternatives.isBlank()); }
		AccountSnapshot unsupported=new AccountSnapshot(a.levels,a.inventory,fact(Set.of(9999)),a.bank,a.rigour,a.augury,a.facts);
		assertTrue(ids(unsupported,Preferences.EMPTY).contains("swamp-trident"));
		assertEquals(PoweredMagic.Status.UNSUPPORTED,new PoweredMagic(catalog).compare(unsupported,Preferences.EMPTY,new PoweredMagic(catalog).propose(unsupported),"SWAMP").status);
	}
}
