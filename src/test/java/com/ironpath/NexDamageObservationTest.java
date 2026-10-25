package com.ironpath;

import com.google.gson.JsonParser;
import com.ironpath.nex.*;
import com.ironpath.preferences.NexMethod;
import java.util.*;
import net.runelite.api.*;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.gameval.NpcID;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static com.ironpath.nex.NexCallbackDiagnostics.Stage.*;
import static com.ironpath.nex.NexCallbackDiagnostics.Target.*;

/** Synthetic object reuse is a failure-mode probe, not evidence of live client object reuse. */
public class NexDamageObservationTest
{
	private NexObserverTest f;
	@Before public void setup() throws Exception { f=new NexObserverTest(); f.setup(); f.start(); }
	private static final class MutableHit implements Hitsplat
	{
		int type=HitsplatID.DAMAGE_ME,amount=20;
		public int getHitsplatType() { return type; }
		public int getAmount() { return amount; }
		public int getDisappearsOnGameCycle() { return 100; }
	}
	private void deliver(Actor target,Hitsplat hit)
	{ var event=new HitsplatApplied(); event.setActor(target); event.setHitsplat(hit); f.observer.hit(event); }
	private NexSummary finish() { f.observer.transition(); return f.session.recent().get(f.session.recent().size()-1); }
	private static void tally(NexSummary s,NexCallbackDiagnostics.Target target,NexCallbackDiagnostics.Stage stage,int count,long amount)
	{ var t=s.observations.callbacks.get(target,stage); assertEquals(stage.name(),count,t.count); assertEquals(stage.name(),amount,t.amount); }
	@Test public void equalSameTickAndReusedObjectCallbacksCountSeparatelyWithFrozenAmountsAndForms()
	{
		var reused=new MutableHit();
		deliver(f.boss,reused); deliver(f.boss,new MutableHit()); deliver(f.boss,reused);
		f.tick.set(2); reused.amount=30; when(f.boss.getId()).thenReturn(NpcID.NEX_DEFLECT); f.observer.changed(f.boss); deliver(f.boss,reused);
		f.tick.set(3); reused.type=HitsplatID.BLOCK_ME; reused.amount=0; deliver(f.minion,reused);
		reused.type=HitsplatID.DAMAGE_OTHER; reused.amount=40; deliver(f.minion,reused);
		reused.type=HitsplatID.HEAL; reused.amount=50; deliver(f.boss,reused);
		reused.type=-123; reused.amount=60; deliver(f.boss,reused);
		var summary=finish(); reused.amount=999;
		assertEquals(90,summary.localDamage()); assertEquals(130,summary.totalDamage()); assertEquals(1,summary.unclassifiedHits);
		assertEquals(List.of(20,20,20,30,0),summary.observations.impacts.stream().map(h->h.amount).collect(java.util.stream.Collectors.toList()));
		assertEquals(NexTargetState.Form.DEFLECT_MELEE,summary.observations.impacts.get(3).targetState.form);
		tally(summary,NEX,HIT_ENTRY,6,200); tally(summary,NEX,HIT_OWN,4,90);
		tally(summary,NEX,HIT_REPEATED_OBJECT,4,160); tally(summary,NEX,HIT_IDENTITY_REJECTED,0,0);
		tally(summary,NEX,HIT_HEAL,1,50); tally(summary,NEX,HIT_UNCLASSIFIED,1,60);
		tally(summary,UMBRA,HIT_OWN,1,0); tally(summary,UMBRA,HIT_OTHER,1,40);
		assertThrows(UnsupportedOperationException.class,()->summary.observations.callbacks.counts.clear());
	}
	@Test public void replayingOnePublicEventIsCountedAsTwoDeliveriesBecauseNoDuplicateKeyIsSupplied()
	{
		var event=new HitsplatApplied(); event.setActor(f.boss); event.setHitsplat(new MutableHit());
		f.observer.hit(event); f.observer.hit(event);
		var summary=finish(); assertEquals(40,summary.localDamage()); tally(summary,NEX,HIT_REPEATED_OBJECT,1,20);
	}
	@Test public void rejectionBoundariesAndPostDeathTrafficHaveCountsAndSumsWithoutEnteringFightDamage()
	{
		var hit=new MutableHit(); deliver(f.player,hit); deliver(f.boss,null);
		deliver(f.npc(999999,3),hit);
		var foreign=f.npc(NpcID.NEX,4); when(foreign.getWorldView()).thenReturn(mock(WorldView.class)); deliver(foreign,hit);
		deliver(f.npc(NpcID.NEX,5),hit);
		hit.amount=-1; deliver(f.boss,hit); hit.amount=20;
		f.observer.death(f.boss); f.tick.set(2); deliver(f.boss,hit);
		var summary=finish(); assertEquals(0,summary.totalDamage()); assertTrue(summary.observations.impacts.isEmpty());
		tally(summary,UNKNOWN,HIT_NOT_NPC,1,20); tally(summary,NEX,HIT_MISSING_PAYLOAD,1,0);
		tally(summary,OUTSIDE,HIT_UNKNOWN_TARGET,1,20); tally(summary,NEX,HIT_WRONG_VIEW,1,20);
		tally(summary,NEX,HIT_UNTRACKED,1,20); tally(summary,NEX,HIT_INVALID_AMOUNT,1,0); tally(summary,NEX,HIT_AFTER_DEATH,1,20);
	}
	@Test public void nullableProjectileSourceStaysUnknownAndNeverEnablesRangedCorrelation()
	{
		var equipment=f.client.getItemContainer(net.runelite.api.gameval.InventoryID.WORN);
		when(equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx())).thenReturn(new Item(20997,1));
		f.tick.set(2); when(f.client.getGameCycle()).thenReturn(60); f.observer.sample(426);
		Projectile p=mock(Projectile.class); when(p.getTargetActor()).thenReturn(f.boss);
		when(p.getId()).thenReturn(1120); when(p.getStartCycle()).thenReturn(60); when(p.getEndCycle()).thenReturn(120);
		f.observer.projectile(p);
		// Nullable API promises no later-resolution contract; the first reading remains immutable.
		when(p.getSourceActor()).thenReturn(f.player); f.observer.projectile(p);
		Projectile other=mock(Projectile.class); when(other.getTargetActor()).thenReturn(f.boss); when(other.getSourceActor()).thenReturn(mock(Player.class)); f.observer.projectile(other);
		Projectile outside=mock(Projectile.class); when(outside.getTargetActor()).thenReturn(f.player); f.observer.projectile(outside);
		f.tick.set(4); when(f.client.getGameCycle()).thenReturn(120); deliver(f.boss,new MutableHit());
		var summary=finish(); var review=new NexObservationReview(summary);
		assertEquals(1,summary.observations.flights.size()); assertEquals(NexObservations.Source.UNKNOWN,summary.observations.flights.get(0).source);
		assertTrue(review.matches.get(0).candidates.isEmpty());
		String ui=String.join("\n",review.window.page(0)); assertTrue(ui.contains("source=UNKNOWN"));
		var json=new JsonParser().parse(review.export).getAsJsonObject();
		assertEquals("nex-evidence-5",json.get("schema").getAsString()); assertFalse(review.export.contains("localSource"));
		assertTrue(review.export.contains("\"source\":\"UNKNOWN\""));
		assertEquals("UNVERIFIED",json.get("damageCoverage").getAsString()); assertTrue(json.get("wholeKillContributionPercent").isJsonNull());
		tally(summary,NEX,PROJECTILE_REPEAT_LOCAL_SOURCE,1,0);
		tally(summary,NEX,PROJECTILE_ENTRY,3,0); tally(summary,NEX,PROJECTILE_REPEAT,1,0); tally(summary,NEX,PROJECTILE_OTHER_SOURCE,1,0);
		tally(summary,UNKNOWN,PROJECTILE_OUTSIDE_TARGET,1,0);
		assertEquals(1,json.getAsJsonObject("callbackChecks").getAsJsonObject("NEX").getAsJsonObject("PROJECTILE_UNKNOWN").get("count").getAsInt());
	}
	@Test public void repeatedProjectileGetterReadingsStayDiagnosticAcrossSourceAndTargetChanges()
	{
		Projectile p=mock(Projectile.class); when(p.getTargetActor()).thenReturn(f.boss);
		f.observer.projectile(p); f.observer.projectile(p);
		when(p.getSourceActor()).thenReturn(f.player); f.observer.projectile(p);
		when(p.getSourceActor()).thenReturn(mock(Player.class)); f.observer.projectile(p);
		when(p.getTargetActor()).thenReturn(f.player); f.observer.projectile(p);
		var summary=finish();
		assertEquals(1,summary.observations.flights.size());
		assertEquals(NexObservations.Source.UNKNOWN,summary.observations.flights.get(0).source);
		tally(summary,NEX,PROJECTILE_REPEAT_UNKNOWN_SOURCE,1,0);
		tally(summary,NEX,PROJECTILE_REPEAT_LOCAL_SOURCE,1,0);
		tally(summary,NEX,PROJECTILE_REPEAT_OTHER_SOURCE,1,0);
		tally(summary,UNKNOWN,PROJECTILE_REPEAT_OTHER_SOURCE,1,0);
		// Scope is the target getter at this callback, not a new shot or a retained target mutation.
		assertEquals(NexTargetState.Form.NEX,summary.observations.flights.get(0).targetState.form);
		var json=new JsonParser().parse(new NexObservationReview(summary).export).getAsJsonObject();
		assertEquals(1,json.getAsJsonObject("callbackChecks").getAsJsonObject("NEX")
			.getAsJsonObject("PROJECTILE_REPEAT_LOCAL_SOURCE").get("count").getAsInt());
		f.observer.clearActive(); f.tick.set(0); f.start();
		assertTrue(finish().observations.callbacks.counts.isEmpty());
	}
	@Test public void diagnosticSaturationDoesNotSpendCaptureSlotsAndClearsWithSmokeAndLifecycle()
	{
		List<NexSummary> out=new ArrayList<>(); var c=new NexCapture(out::add); c.start(0,false,NexMethod.PILOT,"private");
		c.callback(NEX,HIT_ENTRY,20); assertTrue(c.anchorSmoke(1,"private"));
		for(int i=0;i<NexCallbackDiagnostics.MAX_COUNT+10;i++) { c.callback(UMBRA,HIT_ENTRY,Integer.MAX_VALUE); }
		assertTrue(c.active()); c.abort(2,com.ironpath.encounter.EncounterSummary.Outcome.CENSORED,"test");
		var s=out.get(0); tally(s,NEX,HIT_ENTRY,0,0); tally(s,UMBRA,HIT_ENTRY,NexCallbackDiagnostics.MAX_COUNT,NexCallbackDiagnostics.MAX_AMOUNT);
		assertTrue(s.observations.callbacks.get(UMBRA,HIT_ENTRY).atLimit()); assertFalse(s.observations.truncated); assertTrue(s.timeline.isEmpty());
		c.start(3,true,NexMethod.PILOT,"private"); c.abort(4,com.ironpath.encounter.EncounterSummary.Outcome.CENSORED,"test");
		assertTrue(out.get(1).observations.callbacks.counts.isEmpty());
	}
	@Test public void objectAndObservationBoundsNeverRejectValidDamageOrCensorTheCapture()
	{
		for(int i=0;i<NexObservations.MAX+10;i++) { deliver(f.boss,new MutableHit()); }
		assertTrue(f.observer.capture.active());
		var summary=finish(); assertEquals(20L*(NexObservations.MAX+10),summary.localDamage());
		tally(summary,NEX,HIT_OBJECT_TRACKING_LIMIT,10,200); tally(summary,NEX,HIT_IDENTITY_REJECTED,0,0);
		assertTrue(summary.observations.truncated);
		assertEquals(NexObservations.MAX+10,summary.observations.callbacks.get(NEX,HIT_IMPACT_RETAINED).count+summary.observations.callbacks.get(NEX,HIT_IMPACT_OMITTED).count);
		f.observer.clearActive(); f.tick.set(0); f.start();
		assertTrue(f.observer.capture.active()); var next=finish(); assertTrue(next.observations.callbacks.counts.isEmpty());
	}
	@Test public void captureLimitFreezesTheLastOmissionBeforeClearingAndExplicitKeysStillRejectReplay()
	{
		List<NexSummary> out=new ArrayList<>(); var c=new NexCapture(out::add); c.start(0,true,NexMethod.PILOT,"private");
		assertTrue(c.hit(0,1,"NEX@private",20,true,false,false));
		assertFalse(c.hit(0,1,"NEX@private",20,true,false,false));
		for(int i=1;i<NexCapture.MAX_EVENTS;i++) { c.phase(0,"SMOKE","NONE"); }
		c.flight(new NexObservations.Flight(0,0,1,1120,0,60,"NEX@private",NexObservations.Source.UNKNOWN));
		assertFalse(c.active()); var s=out.get(0); assertEquals(20,s.localDamage());
		tally(s,NEX,HIT_DUPLICATE_KEY,1,20); tally(s,NEX,PROJECTILE_OMITTED,1,0); tally(s,NEX,PROJECTILE_RETAINED,0,0);
	}
}
