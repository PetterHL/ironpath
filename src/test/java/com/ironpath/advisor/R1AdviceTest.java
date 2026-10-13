package com.ironpath.advisor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.model.Truth;
import com.ironpath.preferences.Preferences;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.P4aFixtures.*;
import static org.junit.Assert.*;

public class R1AdviceTest
{
	private KnowledgeCatalog catalog;
	private Advisor advisor;
	// Preserve the originally selected high-defence context for these R1/R1.5 regressions.
	private List<Advisor.UpgradeView> reviewedUpgrades(Advisor evaluator, AccountSnapshot a, Preferences p)
	{ return evaluator.upgrades(a, p, new PoweredMagic(catalog).propose(a, "graardor")); }
	@Before public void setup() throws Exception { catalog=KnowledgeCatalog.load(new Gson());advisor=new Advisor(catalog); }
	public static AccountSnapshot postBowfa(boolean shadow)
	{
		Set<Integer> items=new HashSet<>(Set.of(12899,4708,4712,4714,21795,6585,7462,13235,6731,25985,25867,23971,23975,23979));
		if(shadow)items.add(27275);return account(items,99,99);
	}
	@Test public void noGoalPostBowfaHasNearerOptionsAndRealShadowAncestralPlanAheadValue()
	{
		List<Advisor.UpgradeView> views=reviewedUpgrades(advisor, postBowfa(false),Preferences.EMPTY);
		assertTrue(views.stream().anyMatch(v->v.upgrade.id.equals("tumekens-shadow")&&v.comparison.dpsGain()>0&&v.label.contains("Plan ahead")));
		assertTrue(views.subList(0,3).stream().anyMatch(v->v.upgrade.id.equals("tumekens-shadow")));
		assertTrue(views.stream().anyMatch(v->v.upgrade.id.equals("tormented-bracelet")&&v.comparison.dpsGain()>0));
		assertTrue(views.stream().anyMatch(v->v.upgrade.id.startsWith("ancestral-")&&v.comparison.dpsGain()>0));
		assertTrue(views.stream().allMatch(v->v.evidence.stream().anyMatch(s->s.contains("Ranking:"))));
	}
	@Test public void gainingShadowChangesAdviceAndNeverSuggestsASwampGrind()
	{
		List<Advisor.UpgradeView> views=reviewedUpgrades(advisor, postBowfa(true),Preferences.EMPTY);
		assertFalse(views.stream().anyMatch(v->v.upgrade.id.equals("swamp-trident")||v.upgrade.id.equals("tumekens-shadow")));
		assertTrue(views.stream().anyMatch(v->v.upgrade.id.startsWith("ancestral-")&&v.comparison.beforeSetup.slots.get("weapon")==27275&&v.comparison.dpsGain()>0));
		assertEquals(Integer.valueOf(0),views.get(0).comparison.beforeSetup.slots.get("shield"));
	}
	@Test public void tinyOrZeroAccessoryGainCannotOutrankCapabilityByAddingEncounterText()
	{
		AccountSnapshot a=postBowfa(false);List<Advisor.UpgradeView> original=reviewedUpgrades(advisor, a,Preferences.EMPTY);
		Advisor.UpgradeView shadow=original.stream().filter(v->v.upgrade.id.equals("tumekens-shadow")).findFirst().orElseThrow();
		Advisor.UpgradeView ring=original.stream().filter(v->v.upgrade.id.equals("magus-ring")).findFirst().orElseThrow();
		assertTrue(original.indexOf(shadow)<original.indexOf(ring));
		for(int i=0;i<30;i++){KnowledgeCatalog.Application app=new KnowledgeCatalog.Application();app.id="extra-"+i;app.text="Another authored description";app.label="Extra";app.when=ring.upgrade.when;ring.upgrade.applications=new java.util.ArrayList<>(ring.upgrade.applications);ring.upgrade.applications.add(app);}
		assertEquals(original.get(0).upgrade.id,reviewedUpgrades(advisor, a,Preferences.EMPTY).get(0).upgrade.id);
	}
	@Test public void quantityReadingsStayUnknownWhenUnreadAndNeverAddDuplicatedMovedStacks()
	{
		AccountSnapshot base=account(Set.of(11785,26372,26231),99,99);
		assertFalse(base.observedQuantity(26231).known());
		AccountSnapshot a=new AccountSnapshot(base.levels,base.inventory,base.equipment,base.bank,base.rigour,base.augury,base.facts,
			Observation.unknown(),fact(Map.of(26231,120L)),fact(Map.of()),fact(Map.of(26231,120L)));
		Advisor.UpgradeView zaryte=reviewedUpgrades(advisor, a,Preferences.EMPTY).stream().filter(v->v.upgrade.id.equals("zaryte-crossbow")).findFirst().orElseThrow();
		assertEquals(120L,(long)a.observedQuantity(26231).value());
		assertTrue(zaryte.parts.stream().anyMatch(r->r.label.contains("250")&&r.truth==Truth.UNKNOWN&&r.evidence.contains("120 / 250")));
		AccountSnapshot enough=new AccountSnapshot(base.levels,base.inventory,base.equipment,base.bank,base.rigour,base.augury,base.facts,
			Observation.unknown(),Observation.unknown(),Observation.unknown(),fact(Map.of(26231,250L)));
		assertTrue(reviewedUpgrades(advisor, enough,Preferences.EMPTY).stream().filter(v->v.upgrade.id.equals("zaryte-crossbow")).findFirst().orElseThrow().parts.stream().anyMatch(r->r.label.contains("250")&&r.truth==Truth.TRUE));
	}
	@Test public void ownedIntermediateTakesAnyBranchWithoutRequiringTheUpstreamGrind()
	{
		AccountSnapshot a=account(Set.of(12899,28291),99,99);
		Advisor.UpgradeView magus=reviewedUpgrades(advisor, a,Preferences.EMPTY).stream().filter(v->v.upgrade.id.equals("magus-ring")).findFirst().orElseThrow();
		assertTrue(magus.routes.get(0).next.contains("Three chromium ingots"));
		assertFalse(magus.routes.get(0).next.contains("vestige"));
		assertTrue(magus.path.stream().anyMatch(r->r.evidence.contains("ANY")));
	}
	@Test public void unknownEquippedDamageSlotRemainsVisibleUntilExplicitCorrection()
	{
		AccountSnapshot base=postBowfa(false);Map<String,Integer> slots=new java.util.HashMap<>();PoweredMagic.SLOTS.forEach(s->slots.put(s,0));slots.put("ammo",999999);
		AccountSnapshot a=new AccountSnapshot(base.levels,base.inventory,fact(Set.of(999999)),base.bank,base.rigour,base.augury,base.facts,
			fact(slots),Observation.unknown(),Observation.unknown(),Observation.unknown());
		CombatComparisonService service=new CombatComparisonService(catalog);PoweredMagic.Setup proposed=service.propose(a);
		assertEquals(Integer.valueOf(999999),proposed.slots.get("ammo"));
		assertFalse(service.compare(a,Preferences.EMPTY,proposed,27275).available(ComparisonResult.Metric.MAX_HIT));
		assertTrue(service.compare(a,Preferences.EMPTY,proposed.edit("ammo","0"),27275).available(ComparisonResult.Metric.MAX_HIT));
	}
	@Test public void expandedCataloguesHaveNoContentQuotaAndPathCyclesAndBadSourceLinksFailClosed() throws Exception
	{
		JsonObject document=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/catalog.json"))).getAsJsonObject();
		for(int i=0;i<80;i++){JsonObject route=document.getAsJsonArray("decisions").get(0).getAsJsonObject().deepCopy();route.addProperty("id","expanded-route-"+i);document.getAsJsonArray("decisions").add(route);for(var source:document.getAsJsonArray("sources"))if(java.util.stream.StreamSupport.stream(route.getAsJsonArray("sources").spliterator(),false).anyMatch(x->x.equals(source.getAsJsonObject().get("id"))))source.getAsJsonObject().getAsJsonArray("decisions").add(route.get("id"));}
		assertTrue(KnowledgeCatalog.read(new Gson(),new StringReader(document.toString())).decisions.size()>64);
		JsonObject path=document.getAsJsonArray("upgrades").get(4).getAsJsonObject().getAsJsonArray("path").get(0).getAsJsonObject();JsonObject self=new JsonObject();self.addProperty("mode","NODE");self.addProperty("node",path.get("id").getAsString());path.add("dependencies",self);
		assertThrows(IllegalArgumentException.class,()->KnowledgeCatalog.read(new Gson(),new StringReader(document.toString())));
	}
	@Test public void representativeSyntheticProfilesProduceReproducibleValueReports() throws Exception
	{
		Map<String,AccountSnapshot> profiles=new java.util.LinkedHashMap<>();profiles.put("Midgame trident",account(Set.of(11907,6585),80,80));
		profiles.put("Post-Bowfa",postBowfa(false));profiles.put("Shadow owned",postBowfa(true));
		Set<Integer> tbow=new HashSet<>(postBowfa(false).bank.value());tbow.add(20997);profiles.put("Tbow, no Shadow",account(tbow,99,99));
		Set<Integer> mega=new HashSet<>(postBowfa(true).bank.value());mega.addAll(Set.of(20997,22325));profiles.put("Three megarares, gear gaps",account(mega,99,99));
		Set<Integer> complete=new HashSet<>(mega);complete.addAll(Set.of(21018,21021,21024,12002,31106,28313,31097,26374));profiles.put("Nearly complete R1 magic",facts(account(complete,99,99),"desert-treasure-ii",fact(true)));
		StringBuilder report=new StringBuilder("# R1 synthetic account demonstrations\n\nOriginal anonymous fixtures, not player-usefulness evidence. No goal. Model powered-magic-2; knowledge r1-1.\n\nSources and exact revisions: [bundled register](../../src/main/resources/com/ironpath/catalog.json). All omitted slots are explicit reference exclusions; raw accounts and references are never saved.\n\n");
		for(Map.Entry<String,AccountSnapshot> profile:profiles.entrySet())
		{
			List<Advisor.UpgradeView> views=reviewedUpgrades(advisor, profile.getValue(),Preferences.EMPTY);
			if(profile.getKey().contains("Shadow")&&!profile.getKey().contains("no Shadow"))assertFalse(views.stream().anyMatch(v->v.upgrade.id.equals("swamp-trident")));
			if(profile.getKey().startsWith("Nearly complete"))assertTrue(views.isEmpty());
			report.append("## ").append(profile.getKey()).append("\n\nReal levels: ").append(profile.getValue().levels.value()).append(". Observed synthetic bank IDs: ").append(new java.util.TreeSet<>(profile.getValue().bank.value())).append(". Inventory/equipment explicitly empty.\n\n");
			if(views.isEmpty())report.append("No remaining supported R1 magic outcome; no padding. Other combat-family coverage remains open.\n\n");
			for(Advisor.UpgradeView v:views.subList(0,Math.min(3,views.size())))
			{
				report.append("- **").append(v.upgrade.title).append("** — ").append(v.label).append(". ").append(v.comparison.summary()).append("\n");
				if(v.comparison.beforeSetup!=null)report.append("  Baseline slots: ").append(v.comparison.beforeSetup.slots).append("; prayer ").append(v.comparison.beforeSetup.prayer).append("; boost ").append(v.comparison.beforeSetup.boost).append("; style ").append(v.comparison.beforeSetup.style).append(".\n");
				if(v.comparison.afterSetup!=null)report.append("  Candidate slots: ").append(v.comparison.afterSetup.slots).append(".\n");
				report.append("  ").append(v.evidence.get(v.evidence.size()-1)).append("\n  Path next: ").append(v.routes.get(0).next).append("\n\n");
			}
		}
		java.nio.file.Path path=java.nio.file.Path.of("build/reports/r1/scenarios.md");java.nio.file.Files.createDirectories(path.getParent());java.nio.file.Files.writeString(path,report.toString());
	}
}
