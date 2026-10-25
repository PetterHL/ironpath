package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.encounter.CombatState;
import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.nex.*;
import com.ironpath.preferences.*;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.ironpath.advisor.NexOwnedFixtures.*;

/** Independent attack scripts and HP enumeration; no claim of live mapping acceptance. */
public class NexFangAdviceTest
{
	private KnowledgeCatalog catalog;
	private final List<NexActions.Signal> signals=new ArrayList<>();
	private CombatState gear=state(melee(),Map.of(24780,1L,20997,1L));
	private long order;
	@Before public void load() throws Exception { catalog=KnowledgeCatalog.load(new Gson()); }
	private void signal(int tick,NexActions.Kind kind,String phase,String window,String target,NexTargetState.Form form,CombatState state)
	{
		var sample=new NexSummary.Sample(tick,phase,window,target,"unclassified",9471,state,new NexTargetState(form,1,30));
		signals.add(new NexActions.Signal(kind,kind==NexActions.Kind.SETTLED?NexActions.Source.GAME_TICK:NexActions.Source.LOCAL_ANIMATION,
			tick,tick*30,++order,9471,tick,false,sample));
	}
	private void attack(int tick,String phase,String window,String target,NexTargetState.Form form,CombatState state)
	{
		signal(tick,NexActions.Kind.ANIMATION,phase,window,target,form,state);
		signal(tick,NexActions.Kind.SETTLED,phase,window,target,form,state);
	}
	private void attack(int tick) { attack(tick,"SHADOW","FEAR_THE_SHADOW","NEX@test",NexTargetState.Form.NEX,gear); }
	private void twoAttacks()
	{
		attack(5);
		for(int tick=6;tick<10;tick++) { signal(tick,NexActions.Kind.SETTLED,"SHADOW","FEAR_THE_SHADOW","NEX@test",NexTargetState.Form.NEX,gear); }
		attack(10);
	}
	private NexSummary summary(Outcome outcome,boolean truncated,List<NexObservations.Impact> impacts)
	{
		var samples=new ArrayList<NexSummary.Sample>(); signals.forEach(s->samples.add(s.sample));
		return new NexSummary(1,0,100,100,100,0,outcome,"synthetic",NexMethod.ID,NexMethod.ID,samples,Map.of(),Set.of(),null,
			List.of(),NexContext.UNKNOWN,null,new NexObservations(List.of(),List.of(),List.of(),impacts,false),new NexActions(signals,truncated));
	}
	private NexOwnedResult evaluate(NexSummary s,Preferences prefs)
	{ return new NexOwnedEvaluator(catalog).evaluate(s,account(Set.of(22322)),prefs); }
	@Test public void automaticCompletePackagesUseSameSampleAndKeepMissesNegativeAndHypotheticalPairs()
	{
		twoAttacks(); var noImpacts=evaluate(summary(Outcome.COMPLETE,false,List.of()),Preferences.EMPTY);
		var zero=new NexObservations.Impact(5,150,1000,"NEX@test",0,NexTargetState.UNKNOWN);
		var hit=new NexObservations.Impact(10,300,1001,"NEX@test",90,NexTargetState.UNKNOWN);
		var withImpacts=evaluate(summary(Outcome.COMPLETE,false,List.of(zero,hit)),Preferences.EMPTY);
		assertEquals(2,noImpacts.advice.included.size()); assertEquals(noImpacts.advice.included,withImpacts.advice.included);
		assertEquals(noImpacts.advice.rows.size(),withImpacts.advice.rows.size()); assertEquals(1,withImpacts.advice.zeroImpacts);
		assertTrue(noImpacts.advice.rows.stream().anyMatch(r->r.kind==NexFangAdvice.Kind.CARRIED && r.gain.upper<0));
		assertTrue(noImpacts.advice.rows.stream().anyMatch(r->r.kind==NexFangAdvice.Kind.BANK_PREPARATION));
		assertTrue(noImpacts.advice.rows.stream().anyMatch(r->r.kind==NexFangAdvice.Kind.UPGRADE && r.gain.upper>0));
		for(int i=0;i<noImpacts.advice.rows.size();i++)
		{
			var row=noImpacts.advice.rows.get(i); var other=withImpacts.advice.rows.get(i);
			assertEquals(List.of(0,6),row.actionIndices); assertEquals(row.gain.lower,other.gain.lower,0); assertEquals(row.gain.upper,other.gain.upper,0);
			assertEquals(Integer.valueOf(26219),row.pair.beforeSetup.slots.get("weapon"));
			assertEquals(row.pair.beforeSetup.slots.get("weapon"),row.pair.afterSetup.slots.get("weapon"));
			assertEquals(row.pair.beforeSetup.meleeStyle,row.pair.afterSetup.meleeStyle); assertEquals(5,row.pair.after.attackTicks);
		}
		assertFalse(noImpacts.advice.json().get("validated").getAsBoolean());
		assertEquals(NexFangAdvice.STATUS,noImpacts.advice.json().get("status").getAsString());
		var json=new Gson().fromJson(noImpacts.observations.export,com.google.gson.JsonObject.class);
		assertEquals("nex-evidence-5",json.get("schema").getAsString());
		Map<Integer,com.google.gson.JsonObject> actions=new HashMap<>();
		for(var event:json.getAsJsonArray("events"))
		{ var e=event.getAsJsonObject(); if("ACTION_SIGNAL".equals(e.get("type").getAsString())) { actions.put(e.get("actionIndex").getAsInt(),e); } }
		for(var pair:json.getAsJsonObject("fangAdvice").getAsJsonArray("comparisons"))
		for(var index:pair.getAsJsonObject().getAsJsonArray("actionIndices"))
		{ assertEquals("PROVISIONAL_INFERENCE",actions.get(index.getAsInt()).get("confidence").getAsString()); }
	}
	@Test public void uncertaintyFindsInteriorPairedExtremaAndNeverSubtractsIndependentRanges()
	{
		var before=metrics(0,8,1); var after=metrics(0,20,0.6);
		var actual=NexFangAdvice.pairedBounds(before,after);
		double lo=Double.POSITIVE_INFINITY,hi=Double.NEGATIVE_INFINITY; int minHp=0;
		for(int hp=1;hp<=100;hp++)
		{
			double delta=oracle(0,20,0.6,hp)-oracle(0,8,1,hp);
			if(delta<lo) { lo=delta; minHp=hp; } hi=Math.max(hi,delta);
		}
		assertTrue("Minimum must be inside HP range, not at either endpoint",minHp>1 && minHp<100);
		assertEquals(lo,actual.lower,1e-12); assertEquals(hi,actual.upper,1e-12);
		var same=NexFangAdvice.pairedBounds(before,before); assertEquals(0,same.lower,0); assertEquals(0,same.upper,0);
		assertEquals(0,NexFangAdvice.pairedBounds(metrics(0,0,0),metrics(0,0,0)).upper,0);
	}
	private static PoweredMagic.Metrics metrics(int min,int max,double chance)
	{ return new PoweredMagic.Metrics(min,max,chance,0,0,0,0,0,0,0,5); }
	private static double oracle(int min,int max,double chance,int hp)
	{
		double sum=0;
		for(int roll=min;roll<=max;roll++) { sum+=Math.min(hp,max==0?0:Math.max(1,roll))*chance/(max-min+1); }
		return sum;
	}
	@Test public void unknownWindowsTargetsAndZarosCannotBorrowSupportFromAnOrdinaryGroup()
	{
		for(String phase:List.of("UNKNOWN","ZAROS"))
		{ signals.clear(); attack(5,phase,"FEAR_THE_SHADOW","NEX@test",NexTargetState.Form.NEX,gear); assertTrue(evaluate(summary(Outcome.COMPLETE,false,List.of()),Preferences.EMPTY).advice.rows.isEmpty()); }
		for(String window:List.of("UNKNOWN","BLOOD_SIPHON","MINION_FUMUS","EMBRACE_DARKNESS","ICE_PRISON"))
		{ signals.clear(); attack(5,"SHADOW",window,"NEX@test",NexTargetState.Form.NEX,gear); assertTrue(evaluate(summary(Outcome.COMPLETE,false,List.of()),Preferences.EMPTY).advice.rows.isEmpty()); }
		for(var form:List.of(NexTargetState.Form.UNKNOWN,NexTargetState.Form.MINION,NexTargetState.Form.SOUL_SPLIT,NexTargetState.Form.DEFLECT_MELEE))
		{ signals.clear(); attack(5,"SHADOW","FEAR_THE_SHADOW","NEX@test",form,gear); assertTrue(evaluate(summary(Outcome.COMPLETE,false,List.of()),Preferences.EMPTY).advice.rows.isEmpty()); }
	}
	@Test public void truncationIncompleteAndCancellationWithholdAllNumericalAdvice()
	{
		twoAttacks();
		for(var result:List.of(evaluate(summary(Outcome.CENSORED,false,List.of()),Preferences.EMPTY),evaluate(summary(Outcome.COMPLETE,true,List.of()),Preferences.EMPTY)))
		{ assertTrue(result.advice.rows.isEmpty()); assertFalse(result.advice.blockers.isEmpty()); }
		var canceled=new NexOwnedEvaluator(catalog).evaluate(summary(Outcome.COMPLETE,false,List.of()),account(null),Preferences.EMPTY,()->false);
		assertTrue(canceled.advice.rows.isEmpty());
	}
	@Test public void ambiguityAndUnreconciledAttacksAreExcludedWithoutLookingForPositiveHits()
	{
		attack(5); signal(5,NexActions.Kind.FOOD_SHARK,"SHADOW","FEAR_THE_SHADOW","NEX@test",NexTargetState.Form.NEX,gear);
		var result=evaluate(summary(Outcome.COMPLETE,false,List.of()),Preferences.EMPTY);
		assertTrue(result.advice.rows.isEmpty()); assertFalse(result.advice.excluded.isEmpty());
		signals.clear(); signal(5,NexActions.Kind.ANIMATION,"SHADOW","FEAR_THE_SHADOW","NEX@test",NexTargetState.Form.NEX,gear);
		assertTrue(evaluate(summary(Outcome.COMPLETE,false,List.of()),Preferences.EMPTY).advice.rows.isEmpty());
	}
	@Test public void missingEquipmentAndConflictingWeaponLockCannotCreateAUsablePackage()
	{
		attack(5);
		var locked=evaluate(summary(Outcome.COMPLETE,false,List.of()),Preferences.EMPTY.nex(NexMethod.PILOT.lock("melee/weapon",27690)));
		assertTrue(locked.advice.rows.isEmpty());
		signals.clear(); var slots=new HashMap<>(melee()); slots.put("hands",-1); gear=state(slots,Map.of()); attack(5);
		assertTrue(evaluate(summary(Outcome.COMPLETE,false,List.of()),Preferences.EMPTY).advice.rows.isEmpty());
	}
	@Test public void unknownEnergyAndConsumptionDoNotBlockIndependentOrdinaryDamage()
	{
		var s=gear; gear=new CombatState(s.slots,s.realLevels,s.visibleLevels,s.prayers,s.style,null,false,s.inventory,Map.of()); attack(5);
		var result=evaluate(summary(Outcome.COMPLETE,false,List.of()),Preferences.EMPTY);
		assertFalse(result.advice.rows.isEmpty()); assertEquals(8,NexPlanSupport.automaticGaps().size());
		assertThrows(UnsupportedOperationException.class,()->result.advice.rows.clear());
		assertThrows(UnsupportedOperationException.class,()->result.advice.included.clear());
	}
	@Test public void incidentalPlayerResourceChangesKeepTheSameCallbackPackageInTheAdviceSample()
	{
		signal(5,NexActions.Kind.ANIMATION,"SHADOW","FEAR_THE_SHADOW","NEX@test",NexTargetState.Form.NEX,gear);
		var visible=new HashMap<>(gear.visibleLevels); visible.put("HITPOINTS",81); visible.put("PRAYER",98);
		var settled=new CombatState(gear.slots,gear.realLevels,visible,gear.prayers,gear.style,gear.specialEnergy,false,gear.inventory,gear.resources);
		signal(5,NexActions.Kind.SETTLED,"SHADOW","FEAR_THE_SHADOW","NEX@test",NexTargetState.Form.NEX,settled);
		var result=evaluate(summary(Outcome.COMPLETE,false,List.of()),Preferences.EMPTY);
		assertEquals(1,result.advice.included.size()); assertFalse(result.advice.rows.isEmpty());
		for(var row:result.advice.rows) { assertSame(gear,row.sample.state); assertEquals(List.of(0),row.actionIndices); }
		assertEquals(Integer.valueOf(99),result.advice.rows.get(0).sample.state.visibleLevels.get("HITPOINTS"));
	}
	@Test public void resourcePollingDoesNotSplitIdenticalOffenceAndAvailableGearAcrossAttacks()
	{
		var first=gear; attack(5);
		for(int tick=6;tick<10;tick++) { signal(tick,NexActions.Kind.SETTLED,"SHADOW","FEAR_THE_SHADOW","NEX@test",NexTargetState.Form.NEX,gear); }
		var visible=new HashMap<>(gear.visibleLevels); visible.put("HITPOINTS",60); visible.put("PRAYER",90);
		var resources=new HashMap<>(gear.resources); resources.put("death-charge-active",1); resources.put("quiver-splinters-raw",0);
		var inventory=new HashMap<>(gear.inventory); inventory.put(385,5L); // Supply units cannot change equip availability.
		gear=new CombatState(gear.slots,gear.realLevels,visible,gear.prayers,gear.style,500,false,inventory,resources);
		attack(10); var result=evaluate(summary(Outcome.COMPLETE,false,List.of()),Preferences.EMPTY);
		assertEquals(2,result.advice.included.size());
		for(var row:result.advice.rows) { assertSame(first,row.sample.state); assertEquals(List.of(0,6),row.actionIndices); }
	}
	@Test public void newlyAvailableEquipmentCannotLeakBackIntoAnEarlierAttackGroup()
	{
		gear=state(melee(),Map.of()); attack(5);
		for(int tick=6;tick<10;tick++) { signal(tick,NexActions.Kind.SETTLED,"SHADOW","FEAR_THE_SHADOW","NEX@test",NexTargetState.Form.NEX,gear); }
		gear=state(melee(),Map.of(24780,1L)); attack(10);
		var result=evaluate(summary(Outcome.COMPLETE,false,List.of()),Preferences.EMPTY);
		assertEquals(2,result.advice.included.size());
		for(var row:result.advice.rows)
		{
			assertEquals(1,row.actionIndices.size());
			if(row.kind==NexFangAdvice.Kind.CARRIED && row.actionIndices.contains(0)) { assertNotEquals(Integer.valueOf(24780),row.pair.afterSetup.slots.get("neck")); }
		}
		assertTrue(result.advice.rows.stream().anyMatch(r->r.kind==NexFangAdvice.Kind.CARRIED && r.actionIndices.contains(6) && r.pair.afterSetup.slots.get("neck")==24780));
	}
}
