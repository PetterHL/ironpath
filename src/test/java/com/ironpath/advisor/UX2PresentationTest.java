package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.model.Truth;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.ironpath.advisor.EP1Fixtures.*;
import static com.ironpath.advisor.R15Fixtures.*;

public class UX2PresentationTest
{
	@Test public void presentationPreservesEverySupportedMetricZeroAndLossAcrossAccountFixtures() throws Exception
	{
		KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson());Advisor advisor=new Advisor(catalog);
		boolean zero=false,negative=false;
		for(var account:profiles(catalog).values()) {
			for(var view:advisor.upgrades(account,absent(catalog))) {
				for(var metric:view.presentation.comparisonMetrics) {
					if(metric.delta==null) { assertNull(metric.before);assertNull(metric.after);assertNotEquals(UpgradePresentation.Availability.TRUE_ZERO,metric.availability);continue; }
					assertEquals(metric.after-metric.before,metric.delta,0);
					if(metric.kind==ComparisonResult.Metric.MAX_HIT) { assertEquals(view.comparison.before.maxHit,metric.before,0);assertEquals(view.comparison.after.maxHit,metric.after,0); }
					if(metric.kind==ComparisonResult.Metric.THEORETICAL_DPS) { assertEquals(view.comparison.dpsGain(),metric.delta,0); }
					if(metric.delta==0) { zero=true;assertEquals(UpgradePresentation.Availability.TRUE_ZERO,metric.availability); }
					if(metric.delta<0) { negative=true;assertEquals(UpgradePresentation.Availability.AVAILABLE,metric.availability); }
				}
				if(!view.card.context.isEmpty() && view.valueEvidence.specialist && view.comparison.available(ComparisonResult.Metric.THEORETICAL_DPS) && view.comparison.dpsGain()<=1e-10) {
					var contextual=view.contextComparisons.stream().filter(c->c.available(ComparisonResult.Metric.THEORETICAL_DPS)&&c.dpsGain()>1e-10).findFirst().orElse(null);
					if(contextual!=null && contextual.beforeSetup!=null) { assertEquals(catalog.itemNames().get(contextual.beforeSetup.slots.get("weapon")),view.card.baseline); }
				}
				assertFalse(view.presentation.status.contains(" · "));
				assertFalse(view.presentation.nextStep.startsWith("METHOD:"));
				for(var change:view.presentation.changes) { assertFalse(change.beforeName.startsWith("Item "));assertFalse(change.afterName.startsWith("Item ")); }
			}
		}
		assertTrue("Supported zero remains distinct",zero);assertTrue("Trade-off is preserved",negative);
	}
	@Test public void ownedIntermediateKeepsOnlyRemainingWorkInNormalAcquisition() throws Exception
	{
		KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson());Advisor advisor=new Advisor(catalog);
		var account=items(ready(),19535,19529,12922);
		for(var view:advisor.upgrades(account,absent(catalog))) {
			if(!view.upgrade.id.equals("necklace-of-anguish")&&!view.upgrade.id.equals("toxic-blowpipe")) { continue; }
			assertTrue(view.pursuit.remainingEffort.acquisitionBypassed);
			assertTrue(view.remainingPath.stream().noneMatch(r->r.label.equals("Zenyte shard")||r.label.equals("Tanzanite fang")));
			assertFalse(view.presentation.nextStep.contains("Acquire Zenyte shard"));
		}
	}
}
