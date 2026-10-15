package com.ironpath.nex;

import com.ironpath.encounter.CombatState;
import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.preferences.NexMethod;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Original synthetic event replays. These are not real-client kill recordings. */
public class NexCaptureTest
{
	public static CombatState state(int weapon, int shield, int energy, int charge)
	{
		return new CombatState(Map.of("weapon",weapon,"shield",shield,"ring",25975,"ammo",11212),
			Map.of("ATTACK",99,"RANGED",99),Map.of("ATTACK",118,"RANGED",112),Set.of("PIETY"),0,energy,false,
			Map.of(385,5L),Map.of("death-charge-active",charge,"worn-ammo-quantity",100));
	}
	public static NexSummary replay(int offset, int mine, int other)
	{
		List<NexSummary> summaries=new ArrayList<>(); NexCapture capture=new NexCapture(summaries::add);
		replay(capture,offset,true,false,mine,other);
		return summaries.get(0);
	}
	private static void replay(NexCapture c, int offset, boolean full, boolean missing, int mine, int other)
	{
		c.start(offset,full,NexMethod.PILOT,"anonymous-roster-a");
		for (int t=0;t<=36;t++)
		{
			int tick=offset+t;
			if (t==5) { c.phase(tick,"SHADOW",null); }
			if (t==8) { c.phase(tick,null,"EMBRACE_DARKNESS"); }
			if (t==12) { c.phase(tick,null,"FEAR_THE_SHADOW"); }
			if (t==15) { c.phase(tick,"BLOOD",null); }
			if (t==20) { c.phase(tick,"ICE",null); }
			if (t==25) { c.phase(tick,"ZAROS",null); }
			if (t==30) { c.death(tick); c.death(tick); }
			if (t==31) { c.barrierEnd(tick); c.barrierEnd(tick); }
			if (t==10)
			{
				c.hit(tick,1,"NEX@view:index:spawn",mine,true,false,false);
				c.hit(tick,1,"NEX@view:index:spawn",mine,true,false,false); // repeated callback
				c.hit(tick,2,"NEX@view:index:spawn",other,false,true,false);
				c.hit(tick,3,"UMBRA@view:index:spawn",7,true,false,false);
				c.hit(tick,4,"NEX@view:index:spawn",10,false,false,true); // heal
				c.hit(tick,5,"NEX@view:index:spawn",3,false,false,false); // unknown type
			}
			if (missing && t==18) { continue; }
			CombatState state=t>=8 && t<12?state(20997,0,1000,2):t==14?state(27690,22322,500,1):state(26219,22322,500,1);
			c.sample(tick,state,"NEX@view:index:spawn",t>=8 && t<12?"ranged":t==14?"spec candidate":"melee",t==14?1378:-1,"anonymous-roster-a");
		}
	}
	@Test public void multiswitchReplayKeepsDefenderAndImpactPhaseWithoutWeaponAttribution()
	{
		NexSummary s=replay(100,20,60);
		assertEquals(Outcome.COMPLETE,s.outcome); assertEquals(18,s.seconds(),1e-12);
		assertEquals(27,s.localDamage()); assertEquals(87,s.totalDamage()); assertEquals(100.0*27/87,s.share(),1e-12);
		assertEquals(1,s.unclassifiedHits);
		assertTrue(s.damage.keySet().stream().anyMatch(k->k.contains("SHADOW / EMBRACE_DARKNESS / UMBRA")));
		assertTrue(s.timeline.stream().anyMatch(x->x.role.equals("ranged") && x.state.slots.get("shield")==0));
		assertTrue(s.timeline.stream().anyMatch(x->x.tick>112 && x.state.slots.get("shield")==22322));
		assertEquals(Integer.valueOf(22322),s.timeline.get(0).state.slots.get("shield"));
		assertTrue(s.timeline.stream().anyMatch(x->x.state.resources.get("death-charge-active")==2));
		assertTrue(s.timeline.stream().anyMatch(x->x.state.specialEnergy==500));
		assertTrue(s.gaps.stream().anyMatch(x->x.contains("Impact weapon/launch identity unknown")));
		assertThrows(UnsupportedOperationException.class,()->s.timeline.clear());
	}
	@Test public void midKillMissingAndOutOfOrderEvidenceNeverBecomeFullCompletions()
	{
		for (boolean missing:List.of(false,true))
		{
			List<NexSummary> summaries=new ArrayList<>(); NexCapture c=new NexCapture(summaries::add);
			replay(c,0,!missing,missing,20,60);
			if(missing) { assertEquals(Outcome.CENSORED,summaries.get(0).outcome); }
		}
		List<NexSummary> summaries=new ArrayList<>(); NexCapture c=new NexCapture(summaries::add);
		replay(c,0,false,false,20,60); assertEquals(Outcome.CENSORED,summaries.get(0).outcome);
		assertEquals("UNKNOWN",summaries.get(0).timeline.get(0).phase);
		c.start(100,true,NexMethod.PILOT,"a"); c.sample(102,state(26219,22322,1000,0),null,"melee",-1,"a");
		c.phase(101,"SHADOW",null); c.death(103); c.barrierEnd(103); c.flush(103);
		assertEquals(Outcome.CENSORED,summaries.get(1).outcome);
	}
	@Test public void missingOrLateEndAndUnrelatedLootCannotCompleteAndBoundsAreFinite()
	{
		for (boolean death:List.of(true,false))
		{
			List<NexSummary> summaries=new ArrayList<>(); NexCapture c=new NexCapture(summaries::add);
			c.start(0,true,NexMethod.PILOT,"a");
			if(death) { c.death(10); } else { c.barrierEnd(10); }
			c.flush(16); assertEquals(Outcome.CENSORED,summaries.get(0).outcome);
		}
		List<NexSummary> summaries=new ArrayList<>(); NexCapture c=new NexCapture(summaries::add);
		c.start(0,true,NexMethod.PILOT,"a"); c.sample(NexCapture.MAX_TICKS+1,state(1,0,0,0),null,"unknown",-1,"a");
		assertEquals(Outcome.CENSORED,summaries.get(0).outcome); assertFalse(c.active());
		c.start(4000,true,NexMethod.PILOT,"a");
		for(int i=0;i<=NexCapture.MAX_SAMPLES;i++) { c.sample(4000+i,state(i,0,0,0),null,"unknown",1,"a"); }
		assertFalse(c.active()); assertTrue(summaries.get(1).reason.contains("buffer"));
	}
	@Test public void sameTickSwitchesAndLateDuplicatePhasePreserveRangedWindow()
	{
		List<NexSummary> summaries=new ArrayList<>(); NexCapture c=new NexCapture(summaries::add);
		c.start(0,true,NexMethod.PILOT,"a"); c.phase(0,"SHADOW","EMBRACE_DARKNESS");
		c.sample(0,state(26219,22322,1000,2),null,"melee",-1,"a");
		c.phase(0,"SHADOW",null); c.phase(0,"SMOKE",null);
		c.sample(0,state(20997,0,500,1),null,"ranged",-1,"a"); c.abort(1,Outcome.CENSORED,"test end");
		NexSummary s=summaries.get(0); assertEquals(2,s.timeline.size());
		assertEquals("EMBRACE_DARKNESS",s.timeline.get(1).window);
	}
}
