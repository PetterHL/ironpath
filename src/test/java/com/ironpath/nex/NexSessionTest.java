package com.ironpath.nex;

import com.ironpath.encounter.EncounterSummary.Outcome;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class NexSessionTest
{
	@Test public void arenaSceneReloadKeepsCompletedHistoryAndAcceptsTheNextKillWithoutBanking()
	{
		NexSession session=new NexSession(); session.select("a",301);
		var first=NexCaptureTest.replay(0,20,60); session.add(first);
		assertTrue(session.suspend("a",301,40,true,true)); assertTrue(session.recent().isEmpty());
		// Repeated LOADING must preserve the original reason even after the adapter was cleared.
		assertTrue(session.suspend("a",301,41,true,false));
		assertFalse("Missing arena NPCs during a scene rebuild are inconclusive",session.revalidate("a",301,42,false,false));
		assertTrue(session.revalidate("a",301,2000,false,true)); assertFalse(session.suspended());
		assertSame(first,session.stats().last);
		session.add(NexCaptureTest.replay(2100,20,60)); assertEquals(2,session.stats().complete);
		// A reload is not a bank visit and cannot authorize an unrelated departure.
		assertFalse(session.suspend("a",301,2200,false)); assertTrue(session.recent().isEmpty());
	}
	@Test public void leavingTheArenaRevokesReloadEligibilityUntilBankVerification()
	{
		NexSession session=new NexSession(); session.select("a",301);
		var first=NexCaptureTest.replay(0,20,60); session.add(first);
		assertTrue(session.suspend("a",301,40,true,true));
		assertFalse(session.revalidate("a",301,41,false,false,true));
		// Neither another scene load nor a later nearby Nex can promote an outbound leg.
		assertTrue(session.suspend("a",301,42,true,true));
		assertFalse(session.revalidate("a",301,2000,false,true)); assertTrue(session.suspended());
		assertTrue(session.recent().isEmpty());
		assertTrue(session.revalidate("a",301,4000,true,true)); assertSame(first,session.stats().last);
	}
	@Test public void arenaReloadCannotBypassIdentityWorldClockOrResetBoundaries()
	{
		for(String boundary:List.of("account","unknown","world","clock","profile","reset"))
		{
			NexSession session=new NexSession(); session.select("a",301); session.add(NexCaptureTest.replay(0,20,60));
			assertTrue(session.suspend("a",301,40,true,true));
			if(boundary.equals("profile")) { session.select("b",301); }
			else if(boundary.equals("reset")) { session.clear(); }
			assertFalse(boundary,session.revalidate(boundary.equals("unknown")?null:boundary.equals("account")?"b":"a",
				boundary.equals("world")?302:301,boundary.equals("clock")?39:2000,false,true));
			assertFalse(boundary,session.suspended()); assertTrue(boundary,session.recent().isEmpty());
			session.select("a",301); session.revalidate("a",301,4000,true,false);
			assertTrue("A later bank cannot restore cleared history: "+boundary,session.recent().isEmpty());
		}
	}
	@Test public void resetReasonSurvivesFreshAccountReadsUntilANewAttemptWithoutExposingIdentity()
	{
		NexSession session=new NexSession(); session.select("private-fixture-a",301); session.add(NexCaptureTest.replay(0,20,60));
		var frozen=session.stats(); session.suspend("private-fixture-a",301,40,true);
		assertFalse(session.revalidate(null,301,2000,true,false));
		var cleared=session.stats(); assertNotNull(cleared.resetReason); assertNull(cleared.last); assertEquals(0,cleared.attempts);
		assertFalse(cleared.resetReason.contains("private-fixture")); assertNotNull(frozen.last); assertNull(frozen.resetReason);
		session.select("private-fixture-b",301); session.revalidate("private-fixture-b",301,2001,true,false);
		assertEquals(cleared.resetReason,session.stats().resetReason); assertNull(session.stats().last);
		session.add(NexCaptureTest.replay(2100,20,60)); assertNull(session.stats().resetReason); assertEquals(1,session.stats().n);
		assertEquals(0,cleared.attempts); assertNotNull(cleared.resetReason);
	}
	@Test public void delayedBankAndReturnKeepHiddenHistoryWithoutAOneMinuteDeadline()
	{
		NexSession session=new NexSession(); session.select("a",301);
		NexSummary first=NexCaptureTest.replay(0,20,60); session.add(first);
		assertTrue(session.suspend("a",301,40,true));
		for(int tick:List.of(141,1000,2000))
		{
			assertFalse(session.revalidate("a",301,tick,false,false));
			assertTrue("Waiting for a bank must not discard completed history",session.suspended());
			assertTrue(session.recent().isEmpty());
		}
		assertTrue(session.revalidate("a",301,2001,true,false));
		assertSame(first,session.stats().last); assertEquals(1,session.stats().n);
		assertTrue(session.suspend("a",301,2010,false));
		assertFalse(session.revalidate("a",301,4000,false,false)); assertTrue(session.suspended());
		assertTrue(session.revalidate("a",301,4001,false,true)); assertSame(first,session.stats().last);
	}
	@Test public void nearbyNexBeforeOpeningTheBankDoesNotEraseTheOutboundHistory()
	{
		NexSession session=new NexSession(); session.select("a",301); var first=NexCaptureTest.replay(0,20,60); session.add(first);
		assertTrue(session.suspend("a",301,40,true));
		assertFalse(session.revalidate("a",301,41,false,true));
		assertTrue("Nearby arena evidence must keep the outbound leg paused until bank verification",session.suspended());
		assertTrue(session.recent().isEmpty());
		assertTrue(session.revalidate("a",301,2000,true,true)); assertSame(first,session.stats().last);
	}
	@Test public void severalOneKillTripsKeepOnlySameAccountRevalidatedSummaries()
	{
		NexSession session=new NexSession(); session.select("synthetic-a",301);
		for(int i=0;i<4;i++)
		{
			session.add(NexCaptureTest.replay(i*100,20,60));
			assertTrue(session.suspend("synthetic-a",301,i*100+40,true)); assertTrue(session.recent().isEmpty());
			assertFalse(session.revalidate("synthetic-a",301,i*100+42,false,false));
			assertTrue(session.revalidate("synthetic-a",301,i*100+45,true,false));
			assertEquals(i+1,session.stats().complete);
			assertTrue(session.suspend("synthetic-a",301,i*100+50,false));
			assertTrue(session.revalidate("synthetic-a",301,i*100+55,false,true));
			assertEquals(i+1,session.stats().complete);
		}
		assertEquals(4,session.stats().n);
		session.suspend("synthetic-a",301,440,true);
		assertFalse(session.revalidate("synthetic-b",301,441,true,false)); assertTrue(session.recent().isEmpty());
	}
	@Test public void meanPerKillAndAggregateSharesDifferAndFailedAttemptsStaySeparate()
	{
		NexSession session=new NexSession(); session.select("a",301);
		session.add(NexCaptureTest.replay(0,20,60)); session.add(NexCaptureTest.replay(100,40,0));
		NexSummary first=session.recent().get(0);
		session.add(new NexSummary(3,0,10,10,10,0,Outcome.FAILED,"death",first.cohort,first.method,List.of(),java.util.Map.of(),java.util.Set.of()));
		var stats=session.stats(); assertEquals(2,stats.n); assertEquals(1,stats.failed); assertEquals(3,stats.attempts);
		assertEquals((100.0*27/87+100.0)/2,stats.meanShare,1e-12);
		assertEquals(100.0*(27+47)/(87+47),stats.aggregateShare,1e-12); assertNotEquals(stats.meanShare,stats.aggregateShare,0.01);
	}
	@Test public void ambiguousLoadingWorldProfileAndClockChangesClearAndBoundedSessionsEvictOldest()
	{
		for(String boundary:List.of("not-arena","world","profile","unknown-identity","clock-rewind"))
		{
			NexSession session=new NexSession(); session.select("a",301); session.add(NexCaptureTest.replay(0,20,60));
			if(boundary.equals("not-arena")) { assertFalse(session.suspend("a",301,40,false)); }
			else
			{
				session.suspend("a",301,40,true);
				if(boundary.equals("profile")) { session.select("b",301); }
				else { session.revalidate(boundary.equals("unknown-identity")?null:"a",boundary.equals("world")?302:301,boundary.equals("clock-rewind")?39:2000,true,false); }
			}
			assertTrue(boundary,session.recent().isEmpty());
		}
		NexSession session=new NexSession(); session.select("a",301);
		for(int i=0;i<NexSession.MAX_RECENT+5;i++) { session.add(NexCaptureTest.replay(i*100,20,60)); }
		assertEquals(NexSession.MAX_RECENT,session.recent().size()); assertEquals(500,session.recent().get(0).startTick);
	}
	@Test public void changedSetupOrObservedRosterSeparatesCohorts()
	{
		NexSession session=new NexSession(); session.select("a",301); NexSummary s=NexCaptureTest.replay(0,20,60);
		session.add(s); session.add(new NexSummary(2,100,140,40,40,0,s.outcome,s.reason,"different-setup-or-roster",s.method,s.timeline,s.damage,s.gaps));
		assertEquals(2,session.stats().complete); assertEquals(1,session.stats().n);
	}
	@Test public void firstVerifiedBankVisitRecognizesReturnTravelWithoutPriorArenaHistory()
	{
		NexSession session=new NexSession(); session.select("a",301);
		assertTrue(session.revalidate("a",301,10,true,false));
		assertTrue(session.suspend("a",301,15,false)); assertTrue(session.suspended());
		assertTrue(session.revalidate("a",301,20,false,true)); assertFalse(session.suspended());
		// Once back in the arena a new unbanked scene is not a second return leg.
		assertFalse(session.suspend("a",301,25,false));
	}
	@Test public void visibleBankRevalidatesEvenIfNexRemainsLoadedNearby()
	{
		NexSession session=new NexSession(); session.select("a",301); session.add(NexCaptureTest.replay(0,20,60));
		session.suspend("a",301,40,true);
		assertTrue(session.revalidate("a",301,45,true,true)); assertEquals(1,session.stats().complete);
		assertTrue(session.suspend("a",301,50,false));
		assertTrue(session.revalidate("a",301,55,false,true)); assertEquals(1,session.stats().complete);
	}
}
