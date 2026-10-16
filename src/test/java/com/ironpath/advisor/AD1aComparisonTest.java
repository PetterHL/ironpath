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

}
