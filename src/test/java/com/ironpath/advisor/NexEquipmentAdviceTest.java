package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.encounter.CombatState;
import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.nex.*;
import com.ironpath.preferences.*;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.NexOwnedFixtures.*;
import static org.junit.Assert.*;

public class NexEquipmentAdviceTest
{
	private KnowledgeCatalog catalog;
	@Before public void load() throws Exception { catalog=KnowledgeCatalog.load(new Gson()); }
	private NexSummary summary(Outcome outcome,List<NexSummary.Sample> samples,Map<String,NexSummary.Damage> damage,NexContext context)
	{ return new NexSummary(42,0,500,500,500,0,outcome,"synthetic",NexMethod.ID,NexMethod.ID,samples,damage,Set.of(),null,List.of(),context,null); }
	private NexOwnedResult evaluate(NexSummary summary)
	{ return new NexOwnedEvaluator(catalog).evaluate(summary,account(Set.of(22322)),Preferences.EMPTY); }
	@Test public void missingAttackMappingsAndPartialCaptureStillProduceEquipmentResults()
	{
		var gear=state(melee(),Map.of(24780,1L));
		var sample=new NexSummary.Sample(5,"UNKNOWN","UNKNOWN",null,"unclassified",-1,gear);
		var complete=evaluate(summary(Outcome.COMPLETE,List.of(sample),Map.of(),NexContext.PILOT));
		var partial=evaluate(summary(Outcome.CENSORED,List.of(sample),Map.of(),NexContext.PILOT));
		assertTrue(complete.advice.rows.isEmpty()); assertFalse(complete.equipment.rows.isEmpty());
		assertEquals(complete.equipment.rows.size(),partial.equipment.rows.size());
		for(int i=0;i<complete.equipment.rows.size();i++)
		{
			var row=complete.equipment.rows.get(i);
			assertEquals(row.damageGain,partial.equipment.rows.get(i).damageGain);
			assertNotNull(row.impact.secondsSaved); assertNull(partial.equipment.rows.get(i).impact.secondsSaved);
			assertTrue(row.actionIndices.isEmpty()); assertSame(gear,row.sample.state);
		}
	}
	@Test public void observedShareAndKillTimeUseWeightedGainRatherThanWholeMeleeGain()
	{
		var s=summary(Outcome.COMPLETE,List.of(sample("melee",state(melee(),Map.of()))),Map.of("NEX",new NexSummary.Damage(1200,4000)),NexContext.PILOT);
		var result=evaluate(s);
		var row=result.equipment.rows.stream().filter(r->r.damageGain!=null && r.damageGain>0.01).findFirst().orElseThrow();
		double gain=row.damageGain*0.7;
		assertEquals(0.7,row.affectedShare,0); assertEquals(0.3*(1+gain)/(1+0.3*gain),row.impact.contribution,1e-12);
		assertEquals(300-300/(1+0.3*gain),row.impact.secondsSaved,1e-12);
		assertTrue(row.impact.secondsSaved<300-300/(1+0.3*row.damageGain));
		assertEquals(30,s.share(),0);
	}
	@Test public void rangedBoltsAndSpecialsNeedNeitherHpNorProjectileOwnershipNorDeathCharge()
	{
		var ranged=new HashMap<>(ranged()); ranged.put("weapon",26374); ranged.put("ammo",21944); ranged.put("shield",0);
		var vw=new HashMap<>(melee()); vw.put("weapon",27690);
		var result=evaluate(summary(Outcome.COMPLETE,List.of(sample("unclassified",withoutDetailResources(state(ranged,Map.of()))),sample("unclassified",withoutDetailResources(state(vw,Map.of())))),Map.of(),NexContext.PILOT));
		assertTrue(result.equipment.gaps.toString(),result.equipment.rows.stream().anyMatch(r->r.role.equals("ranged")));
		assertTrue(result.equipment.gaps.toString(),result.equipment.rows.stream().anyMatch(r->r.role.equals("spec") && r.sample.state.slots.get("weapon")==26374));
		var voidwaker=result.equipment.rows.stream().filter(r->r.role.equals("spec") && r.sample.state.slots.get("weapon")==27690).findFirst().orElseThrow();
		assertEquals(0.1,voidwaker.affectedShare,0); assertNotNull(voidwaker.impact.contribution);
		assertTrue(voidwaker.pair.before.minHit>0);
	}
	private CombatState withoutDetailResources(CombatState state)
	{
		var levels=new HashMap<>(state.visibleLevels); levels.remove("HITPOINTS"); levels.remove("PRAYER");
		return new CombatState(state.slots,state.realLevels,levels,state.prayers,state.style,null,false,state.inventory,Map.of());
	}
	@Test public void resourcePollingDoesNotDuplicateTheoreticalScenarios()
	{
		var first=state(melee(),Map.of(24780,1L)); var visible=new HashMap<>(first.visibleLevels); visible.put("HITPOINTS",50); visible.put("PRAYER",30);
		var second=new CombatState(first.slots,first.realLevels,visible,first.prayers,first.style,100,false,first.inventory,first.resources);
		var one=evaluate(summary(Outcome.COMPLETE,List.of(sample("melee",first)),Map.of(),NexContext.PILOT));
		var two=evaluate(summary(Outcome.COMPLETE,List.of(sample("melee",first),sample("melee",second)),Map.of(),NexContext.PILOT));
		assertEquals(one.equipment.rows.size(),two.equipment.rows.size());
	}
	@Test public void settingsRecalculateFrozenKillAndRejectTheOldWorkerResult()
	{
		var summary=summary(Outcome.COMPLETE,List.of(sample("melee",state(melee(),Map.of(24780,1L)))),Map.of(),NexContext.PILOT);
		var session=new NexSession(); session.select("synthetic",301); session.add(summary);
		var initial=session.freezeInputs(summary.key,new NexEvaluation(account(Set.of(22322)),Preferences.EMPTY,catalog));
		var old=initial.inputs.evaluate(initial,()->true); assertTrue(session.publish(initial,old));
		var preferences=Preferences.EMPTY.nex(NexMethod.PILOT.lock("melee/neck",24780));
		var changed=session.planning(summary.key,preferences); assertSame(summary,changed.summary); assertNull(changed.calculation);
		assertFalse(session.publish(initial,old));
		var next=changed.inputs.evaluate(changed,()->true); assertTrue(session.publish(changed,next));
		assertFalse(next.equipment.rows.isEmpty());
		for(var row:next.equipment.rows) { assertEquals(Integer.valueOf(24780),row.pair.afterSetup.slots.get("neck")); }
		var context=new NexContext(NexContext.Strategy.RP2,NexContext.Group.ORGANIZED,NexContext.Participation.EXACT,5,
			NexContext.Source.ANNOTATED,NexContext.Source.ANNOTATED,NexContext.Source.ANNOTATED);
		assertTrue(session.annotate(summary.key,changed.revision,context)); var annotated=session.record(summary.key);
		var recalculated=annotated.inputs.evaluate(annotated,()->true); assertTrue(session.publish(annotated,recalculated));
		assertEquals(0.15,recalculated.equipment.rows.get(0).affectedShare,0);
		var row=recalculated.equipment.rows.get(0); double gain=0.15*row.damageGain;
		assertEquals(0.2*(1+gain)/(1+0.2*gain),row.impact.contribution,1e-12);
		session.select("other-account",301); assertFalse(session.publish(annotated,recalculated)); assertNull(session.record(summary.key));
	}
	@Test public void unknownGearDoesNotBecomeOwnedOrNumericallySupported()
	{
		var slots=new HashMap<>(melee()); slots.put("body",99999);
		assertTrue(evaluate(summary(Outcome.COMPLETE,List.of(sample("melee",state(slots,Map.of()))),Map.of(),NexContext.PILOT)).equipment.rows.isEmpty());
	}
}
