package com.ironpath.nex;

import com.google.gson.Gson;
import com.ironpath.advisor.*;
import com.ironpath.preferences.*;
import java.util.*;
import org.junit.Test;
import static com.ironpath.nex.NexContext.*;
import static org.junit.Assert.*;

public class NexContextTest
{
	public static NexContext confirmed(Strategy strategy,Group group,Integer count)
	{ return new NexContext(strategy,group,count==null?Participation.UNKNOWN:Participation.EXACT,count,Source.ANNOTATED,Source.ANNOTATED,Source.ANNOTATED); }
	private static NexSession session()
	{ var session=new NexSession(); session.select("synthetic",301); return session; }
	private static void edit(NexSession session,NexSummary summary,NexContext context)
	{ assertTrue(session.annotate(summary.key,session.record(summary.key).revision,context)); }

	@Test public void dimensionsAndProvenanceCannotCreateAFalseExactCount()
	{
		for(Integer count:Arrays.asList(null,0,-1))
		{ assertThrows(IllegalArgumentException.class,()->new NexContext(Strategy.MP2,Group.MASS,Participation.EXACT,count,Source.INHERITED,Source.INHERITED,Source.INHERITED)); }
		assertThrows(IllegalArgumentException.class,()->new NexContext(Strategy.MP2,Group.MASS,Participation.VARIABLE,3,Source.INHERITED,Source.INHERITED,Source.INHERITED));
		var mass=new NexContext(Strategy.RP2,Group.MASS,Participation.VARIABLE,null,Source.ANNOTATED,Source.ANNOTATED,Source.ANNOTATED);
		assertEquals(Strategy.RP2,mass.strategy); assertNull(mass.participants); assertFalse(mass.confirmed()); assertFalse(mass.pilot());
		assertEquals(Source.UNKNOWN,NexContext.UNKNOWN.countSource); assertFalse(NexContext.PILOT.confirmed());
		var explicit=confirmed(Strategy.MP2,Group.ORGANIZED,3);
		assertTrue(explicit.pilot()); assertFalse(explicit.inherited().pilot());
		assertEquals(Source.INHERITED,Preferences.EMPTY.context(explicit).nexContext.countSource);
	}
	@Test public void defaultsFreezeAtAttemptStartAndSmokeReanchoringDoesNotConfirmThem()
	{
		List<NexSummary> completed=new ArrayList<>(); var capture=new NexCapture(completed::add);
		capture.start(0,false,NexMethod.PILOT,"roster",confirmed(Strategy.RP2,Group.MASS,7));
		capture.sample(0,NexCaptureTest.state(26219,0,1000,0),null,"melee",-1,"roster");
		assertTrue(capture.anchorSmoke(1,"roster")); capture.abort(2,com.ironpath.encounter.EncounterSummary.Outcome.CENSORED,"fixture");
		assertEquals(Strategy.RP2,completed.get(0).context.strategy); assertEquals(Group.MASS,completed.get(0).context.group);
		assertEquals(Integer.valueOf(7),completed.get(0).context.participants); assertEquals(Source.INHERITED,completed.get(0).context.strategySource);
	}
	@Test public void correctingAnOlderKillReconcilesMeansWithoutChangingAnyObservations()
	{
		var session=session(); var first=NexCaptureTest.replay(0,20,60); var last=NexCaptureTest.replay(100,40,0);
		session.add(first); session.add(last); assertEquals(0,session.stats().n); assertTrue(Double.isNaN(session.stats().meanSeconds));
		edit(session,last,confirmed(Strategy.MP2,Group.ORGANIZED,3)); assertEquals(1,session.stats().n);
		edit(session,first,confirmed(Strategy.MP2,Group.ORGANIZED,3)); assertEquals(2,session.stats().n);
		var frozen=session.stats(); assertEquals(18,frozen.meanSeconds,1e-10);
		edit(session,first,confirmed(Strategy.RP2,Group.ORGANIZED,3)); assertEquals(1,session.stats().n); assertEquals(2,frozen.n);
		assertSame(first,session.record(first.key).summary); assertSame(first.timeline,session.record(first.key).summary.timeline);
		assertEquals(27,first.localDamage()); assertEquals(87,first.totalDamage()); assertEquals(18,first.seconds(),1e-10);
		assertEquals(Source.INHERITED,first.context.strategySource); assertEquals(2,session.record(first.key).revision);
		assertFalse(session.annotate(first.key,0,NexContext.UNKNOWN));
	}
	@Test public void matchingRequiresConfirmedContextAndObservedCompositionBeyondEqualCounts()
	{
		var summary=NexCaptureTest.replay(0,20,60); var pilot=confirmed(Strategy.MP2,Group.ORGANIZED,3);
		var evidence=summary.comparability; assertTrue(evidence.matches(evidence,pilot,pilot));
		for(NexContext context:List.of(NexContext.UNKNOWN,NexContext.PILOT,confirmed(Strategy.MP2,Group.MASS,3),
			confirmed(Strategy.OTHER,Group.ORGANIZED,3),confirmed(Strategy.MP2,Group.ORGANIZED,null)))
		{ assertFalse(evidence.matches(evidence,pilot,context)); }
		assertFalse(evidence.matches(evidence,pilot,confirmed(Strategy.MP2,Group.ORGANIZED,4)));
		assertFalse(evidence.matches(evidence,pilot,confirmed(Strategy.RP2,Group.ORGANIZED,3)));
		assertFalse(evidence.matches(new NexComparability(evidence.adapter,evidence.setups,"another-roster",true),pilot,pilot));
		assertFalse(evidence.matches(new NexComparability(evidence.adapter,evidence.setups,null,false),pilot,pilot));
		assertFalse(evidence.matches(new NexComparability("different-adapter",evidence.setups,"anonymous-roster-a",true),pilot,pilot));
		assertFalse(evidence.matches(new NexComparability(evidence.adapter,Set.of(),"anonymous-roster-a",true),pilot,pilot));
	}
	@Test public void planningLocksDoNotBecomeObservedPerformanceEvidence()
	{
		var session=session(); var a=NexCaptureTest.replay(0,20,60);
		var b=new NexSummary(2,100,130,a.sampledTicks,a.expectedTicks,a.unclassifiedHits,a.outcome,a.reason,
			"different-planning-locks",NexMethod.PILOT.lock("melee/shield",12954).cohort(),a.timeline,a.damage,a.gaps,a.signals,a.beforeStart,a.context,a.comparability);
		session.add(a); session.add(b); edit(session,a,confirmed(Strategy.MP2,Group.ORGANIZED,3)); edit(session,b,confirmed(Strategy.MP2,Group.ORGANIZED,3));
		assertEquals(2,session.stats().n);
	}
	@Test public void differentObservedGearStyleOrQuiverProjectileCannotMatchByCountAlone()
	{
		var summary=NexCaptureTest.replay(0,20,60); var c=confirmed(Strategy.MP2,Group.ORGANIZED,3);
		for(String changed:List.of("gear","style","projectile"))
		{
			var state=summary.timeline.get(0).state; var slots=new HashMap<>(state.slots); var resources=new HashMap<>(state.resources);
			if(changed.equals("gear")) { slots.put("weapon",20997); }
			if(changed.equals("projectile")) { resources.put("quiver-ammo-id-raw",21326); }
			var other=new com.ironpath.encounter.CombatState(slots,state.realLevels,state.visibleLevels,state.prayers,changed.equals("style")?1:state.style,
				state.specialEnergy,state.specialSelected,state.inventory,resources);
			var setups=new HashSet<>(summary.comparability.setups); setups.add(new NexComparability.Setup("melee",other));
			assertFalse(changed,summary.comparability.matches(new NexComparability(summary.comparability.adapter,setups,"anonymous-roster-a",true),c,c));
		}
	}
	@Test public void correctedCalculationsReuseFrozenInputsAndRejectPendingRevisionsOrOtherModels() throws Exception
	{
		var session=session(); var summary=NexOwnedFixtures.summary(List.of(NexOwnedFixtures.sample("ranged",NexOwnedFixtures.state(NexOwnedFixtures.ranged(),Map.of(11212,200L)))));
		session.add(summary); var catalog=KnowledgeCatalog.load(new Gson());
		var inputs=new NexEvaluation(NexOwnedFixtures.account(null),Preferences.EMPTY.nex(NexMethod.PILOT.lock("ranged/weapon",20997)),catalog);
		var initial=session.freezeInputs(summary.key,inputs); var stale=inputs.evaluate(initial,()->true);
		edit(session,summary,confirmed(Strategy.RP2,Group.MASS,8)); assertFalse(session.publish(initial,stale));
		var corrected=session.record(summary.key); assertSame(inputs,corrected.inputs);
		var result=inputs.evaluate(corrected,()->true); assertTrue(session.publish(corrected,result));
		assertEquals(1,result.contextRevision); assertEquals(inputs.revision,result.inputRevision); assertEquals(Strategy.RP2,result.context.strategy);
		assertEquals(stale.choices.get(0).comparison.after.dps,result.choices.get(0).comparison.after.dps,0);
		assertEquals(stale.planning.locks,result.planning.locks); assertFalse(result.context.pilot());
		edit(session,summary,NexContext.UNKNOWN); var current=session.record(summary.key);
		var wrongModel=new NexOwnedResult(summary,result.choices,result.planning,current.context,current.revision,inputs.revision,"old-model");
		assertFalse(session.publish(current,wrongModel)); assertNull(session.record(summary.key).calculation);
	}
	@Test public void resetLogoutSuspensionAndEvictionProtectAnnotationsAndPendingCalculations() throws Exception
	{
		var session=session(); var first=NexCaptureTest.replay(0,20,60); session.add(first);
		var inputs=new NexEvaluation(NexOwnedFixtures.account(null),Preferences.EMPTY,KnowledgeCatalog.load(new Gson()));
		var record=session.freezeInputs(first.key,inputs); var pending=inputs.evaluate(record,()->true);
		session.suspend("synthetic",301,50,true); assertNull(session.record(first.key)); assertTrue(session.stats().records.isEmpty());
		assertFalse(session.annotate(first.key,0,NexContext.UNKNOWN)); assertFalse(session.publish(record,pending));
		assertTrue(session.revalidate("synthetic",301,60,true,false)); assertSame(record,session.record(first.key));
		for(int i=1;i<=NexSession.MAX_RECENT;i++) { session.add(NexCaptureTest.replay(i*100,20,60)); }
		assertNull(session.record(first.key)); assertFalse(session.annotate(first.key,0,NexContext.UNKNOWN)); assertFalse(session.publish(record,pending));
		assertEquals(NexSession.MAX_RECENT,session.stats().records.size()); session.clear(); assertTrue(session.stats().records.isEmpty());
		session.select("new-account",301); session.add(first); assertFalse(session.publish(record,pending));
		assertEquals(0,session.record(first.key).revision); assertNull(session.record(first.key).inputs);
	}
}
