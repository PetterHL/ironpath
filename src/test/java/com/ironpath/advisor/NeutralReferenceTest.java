package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.P4aFixtures.*;
import static com.ironpath.advisor.ComparisonResult.Metric.*;
import static org.junit.Assert.*;

public class NeutralReferenceTest
{
	private KnowledgeCatalog catalog;
	private CombatComparisonService service;
	@Before public void setup() throws Exception { catalog=KnowledgeCatalog.load(new Gson()); service=new CombatComparisonService(catalog); }
	@Test public void defaultNeedsNoGoalAndHasOrdinaryRollsAndSuccessfulZeros()
	{
		AccountSnapshot a=account(Set.of(11907),90,90);
		PoweredMagic.Setup proposal=service.propose(a);
		ComparisonResult r=service.compare(a,Preferences.EMPTY,proposal,12899);
		assertEquals("neutral-zero-defence",proposal.target);
		assertEquals("Enemy with 0 Defence",r.target.name);
		assertEquals(576,r.before.defenceRoll);
		assertEquals(7979,r.before.attackRoll);
		assertEquals(1-578.0/15960,r.before.hitChance,1e-12);
		assertEquals((1-578.0/15960)*(326.0/26)/2.4,r.before.dps,1e-12);
		assertTrue(r.before.hitChance<1);
		assertTrue(r.assumptions.toString().contains("Authored analytical reference; Defence 0; Magic 0"));
		assertFalse(r.assumptions.toString().contains("NPC 0"));
		assertEquals("graardor",service.compare(a,Preferences.EMPTY,proposal.edit("target","graardor"),12899).target.id);
		assertEquals(530,service.compare(a,Preferences.EMPTY,proposal.edit("target","dharok"),12899).before.defenceRoll);
		assertFalse(service.compare(a,Preferences.EMPTY,proposal.edit("target","toa"),12899).available(THEORETICAL_DPS));
	}
	@Test public void largerMaxHitCanLoseToFasterStaffAndOwnedPackageUsesDps()
	{
		AccountSnapshot a=account(Set.of(12899),99,99);
		ComparisonResult r=service.compare(a,Preferences.EMPTY,service.propose(a),27275);
		assertTrue(r.after.maxHit>r.before.maxHit);
		assertEquals(4,r.before.attackTicks); assertEquals(5,r.after.attackTicks);
		assertTrue(r.dpsGain()<0);
		assertEquals(THEORETICAL_DPS,r.primaryMetric);
		assertTrue(r.headline().startsWith("DPS "));
		assertTrue(r.summary().indexOf("theoretical DPS")<r.summary().indexOf("Max hit"));
		AccountSnapshot owned=account(Set.of(12899,27275),99,99);
		assertEquals(Integer.valueOf(12899),service.propose(owned).slots.get("weapon"));
		// Neutral marginal loss does not remove the specialist/long-term item from discovery.
		Advisor.UpgradeView view=new Advisor(catalog).upgrades(a,R15Fixtures.absent(catalog)).stream()
			.filter(v->v.upgrade.id.equals("tumekens-shadow")).findFirst().orElseThrow();
		assertEquals(PursuitAssessment.Category.CHECK,view.pursuit.category);
		assertTrue(view.comparison.dpsGain()<0);
		assertTrue(view.contextComparisons.stream().anyMatch(c->c.available(THEORETICAL_DPS)&&c.dpsGain()>0));
	}
	@Test public void equalMaxHitsStillHaveDifferentDpsAtDifferentIntervals()
	{
		AccountSnapshot a=account(Set.of(11907,31113,12002),90,90);
		PoweredMagic.Setup s=service.propose(a).edit("weapon","11907").edit("neck","0");
		ComparisonResult seas=service.compare(a,Preferences.EMPTY,s,11907);
		ComparisonResult eye=service.compare(a,Preferences.EMPTY,s.edit("weapon","31113").edit("neck","12002"),31113);
		assertEquals(seas.before.maxHit,eye.before.maxHit);
		assertEquals(4,seas.before.attackTicks); assertEquals(3,eye.before.attackTicks);
		assertTrue(eye.before.dps>seas.before.dps);
	}
	@Test public void completeReplacementAccessoryAndHistoryGapKeepMetricBoundaries()
	{
		AccountSnapshot a=account(Set.of(12899,25985,12002,31106),99,99);
		PoweredMagic.Setup s=service.propose(a).edit("hands","0");
		ComparisonResult shadow=service.compare(a,Preferences.EMPTY,s,27275);
		assertEquals(Integer.valueOf(0),shadow.afterSetup.slots.get("shield"));
		assertEquals(shadow.beforeSetup.target,shadow.afterSetup.target);
		ComparisonResult accessory=service.compare(a,Preferences.EMPTY,s,19544);
		assertEquals(MAX_HIT,accessory.primaryMetric);
		assertTrue(accessory.headline().contains("max hit"));
		assertEquals(accessory.before.attackTicks,accessory.after.attackTicks);
		ComparisonResult gap=service.compare(a,Preferences.EMPTY,s.edit("hands","31106"),27275);
		assertTrue(gap.available(MAX_HIT)); assertFalse(gap.available(THEORETICAL_DPS));
		assertTrue(gap.headline().startsWith("DPS unavailable · model gap"));
		assertFalse(gap.headline().contains("max hit"));
		ComparisonResult unknown=service.compare(a,Preferences.EMPTY,s.edit("head","-1"),27275);
		assertTrue(unknown.headline().startsWith("DPS unavailable"));assertNull(unknown.before);
	}
	@Test public void sixNeutralProfilesKeepDeterministicAdviceAndHonestContextualValue() throws Exception
	{
		Advisor advisor=new Advisor(catalog);
		AccountSnapshot base=R15Fixtures.developed(false);
		Map<String,AccountSnapshot> profiles=new java.util.LinkedHashMap<>();
		profiles.put("Post-Bowfa, weak magic damage kit",base);
		profiles.put("Tbow owned, same magic kit",R15Fixtures.items(base,20997));
		profiles.put("Shadow owned, same magic kit",R15Fixtures.developed(true));
		profiles.put("Multiple megarares and supported damage gear",R15Fixtures.items(R15Fixtures.developed(true),20997,22325,12002,19544));
		profiles.put("Only long-tail supported gap",R15Fixtures.items(base,21018,21021,21024,12002,19544,28313,26374,31097,21034,21079));
		profiles.put("Nearly complete supported magic",R15Fixtures.items(R15Fixtures.developed(true),21018,21021,21024,12002,19544,28313,26374,31097));
		StringBuilder report=new StringBuilder("# Neutral reference synthetic account results\n\n13 October 2026. Original synthetic inputs, no goal or player-usefulness evidence. Knowledge reference-1, combat powered-magic-2, ranking pursuit-1. Exact fixture inputs are in NeutralReferenceTest and R15Fixtures.\n\n");
		for(Map.Entry<String,AccountSnapshot> profile:profiles.entrySet())
		{
			AccountSnapshot a=profile.getValue(); Preferences p=R15Fixtures.absent(catalog);
			var first=advisor.upgrades(a,p); var repeat=advisor.upgrades(a,p);
			assertEquals(first.stream().map(v->v.upgrade.id+v.pursuit.category).collect(java.util.stream.Collectors.toList()),
				repeat.stream().map(v->v.upgrade.id+v.pursuit.category).collect(java.util.stream.Collectors.toList()));
			assertTrue(first.stream().filter(v->v.comparison.before!=null).allMatch(v->v.comparison.target.analytical));
			if(a.observedItem(27275)==com.ironpath.model.Truth.TRUE)assertFalse(first.stream().anyMatch(v->v.upgrade.id.equals("swamp-trident")));
			if(profile.getKey().startsWith("Nearly complete"))assertTrue(first.stream().noneMatch(v -> v.upgrade.comparison.equals("POWERED_MAGIC")));
			report.append("## ").append(profile.getKey()).append("\n\n");
			if(first.isEmpty())report.append("No remaining supported outcome; wider combat-family coverage remains pending.\n\n");
			for(var view:first)report.append("- **").append(view.upgrade.title).append("** — ").append(view.pursuit.category.label)
				.append(". ").append(view.comparison.summary()).append("\n  ").append(view.pursuit.reasons.get(0)).append("\n");
			report.append("\n");
		}
		java.nio.file.Path path=java.nio.file.Path.of("build/reports/neutral/scenarios.md");
		java.nio.file.Files.createDirectories(path.getParent());java.nio.file.Files.writeString(path,report);
	}
	@Test public void neutralFullLoadoutsMatchPinnedIndependentReference() throws Exception
	{
		int count=0;
		try (BufferedReader reader=new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/neutral-oracle.csv"),StandardCharsets.UTF_8)))
		{
			String line;
			while((line=reader.readLine())!=null)
			{
				if(line.startsWith("#")||line.startsWith("magic"))continue;
				String[] row=line.split(","); int weapon=Integer.parseInt(row[1]),armour=Integer.parseInt(row[2]);
				int[][] robes={{4708,4712,4714},{26241,26243,26245},{21018,21021,21024}};
				Map<String,Integer> slots=new HashMap<>(); PoweredMagic.SLOTS.forEach(slot->slots.put(slot,0));
				slots.putAll(Map.of("weapon",weapon,"head",robes[armour][0],"body",robes[armour][1],"legs",robes[armour][2],
					"cape",21795,"neck",12002,"hands",19544,"feet",13235,"ring",11770));
				if(weapon!=27275)slots.put("shield",25985);
				AccountSnapshot a=account(new HashSet<>(slots.values()),Integer.parseInt(row[0]),99);
				boolean boosted=row[4].equals("augury-heart");
				CombatSetup s=new CombatSetup(slots,boosted?PoweredMagic.Prayer.AUGURY:PoweredMagic.Prayer.NONE,
					boosted?PoweredMagic.Boost.SATURATED_HEART:PoweredMagic.Boost.NONE,
					PoweredMagic.Style.valueOf(row[3].toUpperCase(java.util.Locale.ROOT)),false,false,"neutral-zero-defence",Map.of());
				ComparisonResult r=service.compare(a,Preferences.EMPTY,s,12002);
				assertTrue(line+": "+r.reason,r.available(THEORETICAL_DPS));
				assertEquals(line,Integer.parseInt(row[5]),r.before.maxHit);
				assertEquals(line,Long.parseLong(row[6]),r.before.attackRoll);
				assertEquals(line,Long.parseLong(row[7]),r.before.defenceRoll);
				assertEquals(line,Double.parseDouble(row[8]),r.before.hitChance,1e-12);
				assertEquals(line,Double.parseDouble(row[9]),r.before.dps,1e-10); count++;
			}
		}
		assertEquals(180,count);
	}
}
