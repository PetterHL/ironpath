package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.ironpath.advisor.ComparisonResult.Metric.*;
import static com.ironpath.advisor.P4aFixtures.*;
import static com.ironpath.advisor.R15Fixtures.*;

/** Original synthetic inputs; the player's complete magic/ranged setup has not been supplied. */
public class AD1aComparisonTest
{
	private KnowledgeCatalog catalog;
	@Before public void load() throws Exception { catalog=KnowledgeCatalog.load(new Gson()); }
	private Map<String,Integer> slots(int weapon)
	{ Map<String,Integer> out=new LinkedHashMap<>();PoweredMagic.SLOTS.forEach(s->out.put(s,0));out.put("weapon",weapon);return out; }
	public static AccountSnapshot brimstoneFixture()
	{ return withFact(account(Set.of(12899,22975,19544,12002,4708,4712,4714,20235),99,99),"duke-defeated",fact(true)); }
	public static AccountSnapshot bucklerFixture()
	{ return items(ClientEquipmentRegressionTest.reportedLightbearerFixture(),9185,9144); }
	/** Count successful integer roll pairs, independent of the production piecewise formula. */
	private double countedChance(long attack,long defence)
	{
		long successes=0;
		for(long rolledAttack=0;rolledAttack<=attack;rolledAttack++) { successes+=Math.min(rolledAttack,defence+1); }
		return (double)successes/((attack+1)*(defence+1));
	}
	@Test public void brimstoneMixtureMatchesIndependentRollCountingAcrossStaffsAndDefences()
	{
		Map<Integer,KnowledgeCatalog.CombatItem> items=catalog.comparison.items.stream().collect(Collectors.toMap(i->i.id,i->i));
		for(int weapon:List.of(11907,12899,22323,31113,27275)) for(int defence:List.of(0,9,575,1999,9001,32218)) {
			Map<String,Integer> worn=slots(weapon);worn.put("ring",22975);
			CombatSetup setup=new PoweredMagic.Setup(worn,PoweredMagic.Prayer.NONE,PoweredMagic.Boost.NONE,PoweredMagic.Style.ACCURATE,false,false);
			TargetProfile target=new TargetProfile();target.variant="STANDARD";
			// The minimum game Magic level is zero; -64 bonus also exercises a zero defence roll.
			target.magicLevel=defence==0?0:defence-9;target.magicDefence=defence==0?-64:-63;
			PoweredMagic.Metrics actual=PoweredStaffModel.calculate(99,setup,target,items);
			double expected=(3*countedChance(actual.attackRoll,defence)+countedChance(actual.attackRoll,defence*9/10))/4;
			assertEquals("weapon "+weapon+", defence "+defence,expected,actual.hitChance,1e-12);
			int base=weapon==11907?28:weapon==12899?31:weapon==22323?33:weapon==31113?27:34;
			double mean=weapon==22323?base/2.0+1.6+0.8/(base+1):base/2.0+1.0/(base+1);
			assertEquals(expected*mean/(actual.attackTicks*0.6),actual.dps,1e-12);
			assertEquals(defence,actual.defenceRoll);
		}
	}
	@Test public void magusAgainstBrimstoneFlowsThroughAdvisorWithoutInventingPlayerInputs()
	{
		AccountSnapshot a=brimstoneFixture();Advisor advisor=new Advisor(catalog);
		var view=advisor.upgrades(a,absent(catalog)).stream().filter(v->v.upgrade.id.equals("magus-ring")).findFirst().orElseThrow();
		assertEquals(Integer.valueOf(22975),view.comparison.beforeSetup.slots.get("ring"));
		assertEquals(THEORETICAL_DPS,view.presentation.primaryMetric.kind);
		assertTrue(view.comparison.reason,view.comparison.available(THEORETICAL_DPS));
		assertTrue(view.card.metric.contains("% DPS"));assertEquals("Brimstone ring",view.card.baseline);
		assertEquals(view.comparison.dpsGain()/view.comparison.before.dps*100,view.presentation.primaryMetric.relativePercent,1e-12);
		assertTrue(view.comparison.assumptions.stream().anyMatch(s->s.startsWith("Brimstone:")));
		// One-handed Confliction still withholds only dependent metrics, even with Brimstone.
		a=items(a,31106);var service=new CombatComparisonService(catalog);
		var comparison=service.compare(a,Preferences.EMPTY,service.propose(a).edit("hands","31106"),28313);
		assertTrue(comparison.available(MAX_HIT));assertFalse(comparison.available(THEORETICAL_DPS));
		assertTrue(comparison.gaps.get(THEORETICAL_DPS).contains("confliction"));
		assertFalse(comparison.gaps.get(THEORETICAL_DPS).contains("Brimstone"));
	}
	@Test public void ringProposalUsesProcRatherThanFlatStatsOrInputOrder()
	{
		AccountSnapshot a=account(Set.of(12899,22975,6731),99,99);var service=new CombatComparisonService(catalog);
		// At this breakpoint the 0.2% flat ring bonus adds no max hit; Brimstone improves accuracy.
		assertEquals(Integer.valueOf(22975),service.propose(a,"graardor").slots.get("ring"));
		var proposed=service.propose(a,"graardor");
		var brim=service.compare(a,Preferences.EMPTY,proposed,22975);
		var seers=service.compare(a,Preferences.EMPTY,proposed,6731);
		assertEquals(brim.before.maxHit,seers.after.maxHit);assertTrue(brim.before.dps>seers.after.dps);
	}
	@Test public void bucklerAgainstBlazingNamesWeaponShieldAndBothAmmoLocations()
	{
		var a=bucklerFixture();var view=new Advisor(catalog).upgrades(a,absent(catalog)).stream()
			.filter(v->v.upgrade.id.equals("twisted-buckler")).findFirst().orElseThrow();
		assertTrue(view.comparison.reason,view.comparison.available(THEORETICAL_DPS));
		assertEquals(Integer.valueOf(28688),view.comparison.beforeSetup.slots.get("weapon"));
		assertEquals(Integer.valueOf(9185),view.comparison.afterSetup.slots.get("weapon"));
		assertEquals(Integer.valueOf(21000),view.comparison.afterSetup.slots.get("shield"));
		assertTrue(view.presentation.setupChange);assertEquals("Setup change",view.presentation.comparisonLabel);
		assertEquals("Blazing blowpipe setup",view.presentation.baseline);
		assertTrue(view.presentation.changes.stream().map(c->c.slot).collect(Collectors.toSet()).containsAll(Set.of("weapon","shield","ammo","loaded-ammo")));
		assertEquals(11230,view.comparison.beforeSetup.loadedAmmo);assertEquals(0,view.comparison.afterSetup.loadedAmmo);
		assertEquals(Integer.valueOf(9144),view.comparison.afterSetup.slots.get("ammo"));
		assertTrue("A package loss must remain a loss",view.presentation.primaryMetric.delta<0);
		if(view.card.context.isEmpty()) { assertTrue(view.card.metric,view.card.metric.startsWith("-")); }
		else { assertTrue(view.contextComparisons.stream().anyMatch(c->c.target.name.equals(view.card.context) && c.dpsGain()==view.card.delta)); }
	}
	@Test public void zeroBaselineUsesAbsoluteGainWhileZeroAndLossKeepTheirSign()
	{
		Map<ComparisonResult.Metric,PoweredMagic.Status> support=new EnumMap<>(ComparisonResult.Metric.class);
		for(var metric:ComparisonResult.Metric.values()) { support.put(metric,PoweredMagic.Status.SUPPORTED); }
		for(double[] pair:List.of(new double[]{0,0},new double[]{0,2},new double[]{0,0.00001},new double[]{2,2},new double[]{2,1},new double[]{2,2.00001},new double[]{2,1.99999})) {
			var before=new PoweredMagic.Metrics(10,1,pair[0],99,110,20,0,100,0,4);
			var after=new PoweredMagic.Metrics(10,1,pair[1],99,110,20,0,100,0,4);
			var c=new ComparisonResult(null,null,before,after,null,support,"fixture","fixture","fixture",List.of(),false,Map.of());
			var m=new UpgradePresentation.Metric(c,"Explicit synthetic baseline",false);
			assertFalse(m.headline().contains("NaN"));assertFalse(m.headline().contains("Infinity"));
			if(m.delta!=0 && Math.abs(m.delta)<0.005) { assertTrue(m.absoluteDpsDelta().contains("<0.01"));assertFalse(m.headline().contains("0.0%")); }
			if(pair[0]==0) { assertNull(m.relativePercent);assertFalse(m.headline().contains("%")); }
			else { assertEquals((pair[1]-pair[0])/pair[0]*100,m.relativePercent,0); }
			assertEquals(pair[0]==pair[1]?UpgradePresentation.Availability.TRUE_ZERO:UpgradePresentation.Availability.AVAILABLE,m.availability);
		}
	}
	@Test public void everyActiveOutcomeHasAuditableDisplayedMetricAndNamedInputOrModelGap() throws Exception
	{
		Advisor advisor=new Advisor(catalog);Map<String,AccountSnapshot> profiles=new LinkedHashMap<>(EP1Fixtures.profiles(catalog));
		profiles.put("brimstone-synthetic",brimstoneFixture());profiles.put("reported-ranged-classes-synthetic",bucklerFixture());
		profiles.put("bowfa-without-armour",withFact(account(Set.of(25867),99,99),"song-of-elves",fact(true)));
		profiles.put("seas-baseline",account(Set.of(11907),99,99));
		Map<String,List<String>> rows=new TreeMap<>();
		for(var u:catalog.upgrades) { if(u.status.equals("reviewed")) { rows.put(u.id,new ArrayList<>()); } }
		StringBuilder evidence=new StringBuilder("# AD1a evaluated metric coverage\n\nOriginal synthetic fixtures, including reported ranged gear classes with explicitly chosen missing inputs. No complete player setup was supplied. Conditional reference numbers are not measured performance. `absent(catalog)` is an explicit synthetic planning input, never inferred account absence.\n\n");
		evidence.append("| Outcome | Fixture | Classification | Displayed result | Baseline | Displayed context | Dependent gap |\n|---|---|---|---|---|---|---|\n");
		for(var profile:profiles.entrySet()) for(var v:advisor.upgrades(profile.getValue(),absent(catalog))) {
			var primary=v.presentation.primaryMetric;
			assertEquals(THEORETICAL_DPS,primary.kind);
			String classification=v.valueEvidence.independentUtility?"intentional utility-led":primary.availability==UpgradePresentation.Availability.INPUT_REQUIRED?"missing account input"
				:primary.availability==UpgradePresentation.Availability.MODEL_UNSUPPORTED?"unsupported mechanic/context":"supported and displayed DPS";
			if(!v.valueEvidence.independentUtility && primary.delta!=null) { assertTrue(v.card.metric,v.card.metric.contains("DPS")); }
			rows.get(v.upgrade.id).add(profile.getKey());
			String gap=v.comparison.available(THEORETICAL_DPS)?"—":v.comparison.gaps.getOrDefault(THEORETICAL_DPS,v.comparison.reason);
			String context=!v.card.context.isEmpty()?v.card.context:v.comparison.target==null?"Reviewed role":v.comparison.target.name;
			evidence.append("| ").append(v.upgrade.id).append(" | ").append(profile.getKey()).append(" | ").append(classification).append(" | ").append(v.card.metric)
				.append(" | ").append(v.card.baseline).append(" | ").append(context).append(" | ").append(gap.replace("|","/")).append(" |\n");
		}
		Path dir=Path.of("build/validation/ad1a");Files.createDirectories(dir);Files.writeString(dir.resolve("metric-coverage.md"),evidence);
		assertEquals("Active outcomes lacking evaluated fixtures",List.of(),rows.entrySet().stream().filter(r->r.getValue().isEmpty()).map(Map.Entry::getKey).collect(Collectors.toList()));
	}
}
