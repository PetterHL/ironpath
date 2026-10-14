package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/** Joint delivery behavior over real Advisor results; authored fixtures, no client acceptance. */
public class IntegratedDeliveryTest
{
	@Test public void meleeAccessoriesUseRelativeDpsWhileSustainRetainsItsNegativeOffence() throws Exception
	{
		KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson());
		AccountSnapshot account=MeleeAdviceTest.ordinary();
		Preferences preferences=R15Fixtures.absent(catalog).confirm("vardorvis-defeated",true,P4aFixtures.TIME);
		List<Advisor.UpgradeView> views=new Advisor(catalog).upgrades(account,preferences);
		for(String id:List.of("amulet-of-torture","ferocious-gloves","primordial-boots","ultor-ring"))
		{
			Advisor.UpgradeView view=views.stream().filter(v->v.upgrade.id.equals(id)).findFirst().orElseThrow();
			assertTrue(id,view.comparison.available(ComparisonResult.Metric.THEORETICAL_DPS));
			assertEquals(ComparisonResult.Metric.THEORETICAL_DPS,view.presentation.primaryMetric.kind);
			assertNotNull(id,view.presentation.primaryMetric.relativePercent);
			assertTrue(id,view.card.metric.contains("% DPS"));
			assertFalse(id,view.valueEvidence.reason.contains("ranged neck"));
		}
		account=R15Fixtures.items(account,19553);
		Advisor.UpgradeView blood=new Advisor(catalog).upgrades(account,preferences).stream()
			.filter(v->v.upgrade.id.equals("amulet-of-blood-fury")).findFirst().orElseThrow();
		assertTrue(blood.valueEvidence.independentUtility);
		assertTrue(blood.comparison.dpsGain()<0);
		assertEquals(UpgradePresentation.Availability.QUALITATIVE,blood.presentation.primaryMetric.availability);
		assertTrue(blood.presentation.comparisonMetrics.get(0).delta<0);
	}
	@Test public void soulRampScopeSurvivesDpsFirstCardAndDetailContracts() throws Exception
	{
		KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson());
		AccountSnapshot account=MeleeAdviceTest.ordinary();
		Preferences preferences=R15Fixtures.absent(catalog).confirm("vardorvis-defeated",true,P4aFixtures.TIME);
		MeleeComparisonService melee=new MeleeComparisonService(catalog);
		CombatSetup reference=melee.propose(account,preferences).edit("souls","RAMP");
		List<Advisor.UpgradeView> views=new Advisor(catalog).upgrades(account,preferences,
			new CombatComparisonService(catalog).propose(account),new RangedComparisonService(catalog).propose(account),reference,null);
		Advisor.UpgradeView soul=views.stream().filter(v->v.upgrade.id.equals("soulreaper-axe")).findFirst().orElseThrow();
		assertEquals(ComparisonResult.OffenceScope.NEXT_FIVE_SWINGS,soul.comparison.offenceScope);
		assertEquals(ComparisonResult.OffenceScope.NEXT_FIVE_SWINGS,soul.presentation.primaryMetric.offenceScope);
		assertTrue(soul.card.metric,soul.card.metric.contains("five-swing mean"));
		assertTrue(soul.presentation.primaryMetric.headline().contains("DPS"));
		assertFalse(soul.comparison.available(ComparisonResult.Metric.EXPECTED_KILL_TIME));
	}
}
