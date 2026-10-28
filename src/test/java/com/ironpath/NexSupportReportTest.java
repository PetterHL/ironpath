package com.ironpath;

import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.model.Truth;
import com.ironpath.nex.*;
import java.util.*;
import net.runelite.api.GameState;
import org.junit.Test;
import static org.junit.Assert.*;

public class NexSupportReportTest
{
	private static final String PRIVATE="PRIVATE_NAME_ACCOUNT_CHAT_TRACE";
	private NexSummary summary(long key,Outcome outcome,String reason)
	{
		var origin=new NexSummary.Origin((int)key*100,NexSummary.StartSource.ACTIVE_ARENA_POLL,NexTargetState.Form.NEX,3);
		var signals=new NexSummary.Signals(null,outcome==Outcome.COMPLETE?(int)key*100:null,null,null,true,Set.of("SMOKE",PRIVATE),null,List.of(PRIVATE),origin);
		return new NexSummary(key,(int)key*100,(int)key*100+20,20,20,0,outcome,reason,PRIVATE,PRIVATE,List.of(),Map.of(),Set.of(PRIVATE),signals);
	}
	private NexSupportReport.Runtime runtime(NexSession.Stats stats,Truth bank)
	{ return new NexSupportReport.Runtime(stats.scope,GameState.LOGGED_IN,350,bank,0,Truth.FALSE,Truth.TRUE,false,false); }
	private String report(NexSession session,NexSummary selected,String frozen,NexReport.State state)
	{
		var stats=session.stats();
		return NexSupportReport.snapshot(new NexSupportReport.Frame(stats,runtime(stats,Truth.TRUE)),stats.scope,selected,0,frozen,
			NexReport.Provenance.SYNTHETIC_OFFLINE,state,NexPanel.TrackingStatus.OUTSIDE);
	}
	@Test public void oneCopyIncludesEveryExclusionEvenWhenACompleteKillIsSelected()
	{
		var session=new NexSession(); session.select(PRIVATE,301);
		var complete=summary(1,Outcome.COMPLETE,PRIVATE); session.add(complete);
		session.add(summary(2,Outcome.CENSORED,"State buffer limit reached"));
		session.add(summary(3,Outcome.CENSORED,"Left arena or scene loading during active capture"));
		String frozen="Frozen selected calculation stays unchanged.\n";
		String text=report(session,complete,frozen,NexReport.State.READY);
		assertTrue(text.contains("1 complete; 0 failed; 2 excluded"));
		assertTrue(text.contains("Selected record: A1; latest recorded interval: A3"));
		assertTrue(text.contains("Attempt A2: outcome=CENSORED; exclusion=State buffer limit reached"));
		assertTrue(text.contains("Attempt A3: outcome=CENSORED; exclusion=Left arena or scene loading during active capture"));
		assertTrue(text.contains("origin=ACTIVE_ARENA_POLL; origin form=NEX; origin barrier=3; origin offset=100"));
		assertTrue(text.contains("Overview records: 3/3; complete=true"));
		assertEquals(frozen,NexSupportReport.selected(text)); assertFalse(text.contains(PRIVATE));
	}
	@Test public void pausedReportExplainsRuntimeReadingsWithoutRevealingHiddenRecords()
	{
		var session=new NexSession(); session.select(PRIVATE,301); var complete=summary(1,Outcome.COMPLETE,PRIVATE); session.add(complete);
		String text=report(session,complete,PRIVATE,NexReport.State.PAUSED);
		assertTrue(text.contains("bank visible=TRUE")); assertTrue(text.contains("identity readable=TRUE"));
		assertTrue(text.contains("Session overview: HIDDEN"));
		assertFalse(text.contains("Attempt A")); assertFalse(text.contains("1 complete")); assertFalse(text.contains(PRIVATE));
	}
	@Test public void aCopiedOverviewRemainsSelfContainedAfterLogoutWhileNewExportsClearEvidence()
	{
		var session=new NexSession(); session.select(PRIVATE,301); var complete=summary(1,Outcome.COMPLETE,PRIVATE); session.add(complete);
		session.add(summary(2,Outcome.CENSORED,"Arena left before correlated completion"));
		String copied=report(session,complete,"Selected complete report",NexReport.State.READY);
		session.clear("Account transition: observations cleared. Reopen your bank after login.");
		String cleared=report(session,complete,PRIVATE,NexReport.State.NO_ACCOUNT);
		assertTrue(copied.contains("Attempt A2: outcome=CENSORED; exclusion=Arena left before correlated completion"));
		assertTrue(cleared.contains("Last clear category: ACCOUNT_TRANSITION"));
		assertFalse(cleared.contains("Attempt A")); assertFalse(cleared.contains(PRIVATE));
	}
	@Test public void currentOverviewAndFrozenSelectedEvidenceHaveIndependentLifetimes()
	{
		var session=new NexSession(); session.select(PRIVATE,301); var selected=summary(1,Outcome.COMPLETE,PRIVATE); session.add(selected);
		var stats=session.stats(); var frame=new NexSupportReport.Frame(stats,runtime(stats,Truth.FALSE));
		String first=NexSupportReport.snapshot(frame,stats.scope,selected,0,"Frozen bytes",NexReport.Provenance.SYNTHETIC_OFFLINE,NexReport.State.READY,NexPanel.TrackingStatus.READY);
		session.add(summary(2,Outcome.CENSORED,"State buffer limit reached"));
		String next=report(session,selected,"Frozen bytes",NexReport.State.READY);
		assertFalse(first.contains("Attempt A2")); assertTrue(next.contains("Attempt A2"));
		assertEquals(NexSupportReport.selected(first),NexSupportReport.selected(next));
		assertEquals(first,NexSupportReport.snapshot(frame,stats.scope,selected,0,"Frozen bytes",NexReport.Provenance.SYNTHETIC_OFFLINE,NexReport.State.READY,NexPanel.TrackingStatus.READY));
	}
	@Test public void boundsUnknownsAndScopeChangesCannotLeakRawInputs()
	{
		var session=new NexSession(); session.select(PRIVATE,301);
		var old=summary(1,Outcome.COMPLETE,PRIVATE); session.add(old); long oldScope=session.stats().scope;
		for(int i=2;i<=51;i++) { session.add(summary(i,Outcome.CENSORED,PRIVATE)); }
		String text=report(session,old,PRIVATE,NexReport.State.READY);
		assertTrue(text.contains("Overview records: 50/50; complete=true"));
		assertTrue(text.length()<NexSupportReport.MAX_OVERVIEW_CHARS); assertFalse(text.contains(PRIVATE));
		session.select("different synthetic account",302); session.add(summary(1,Outcome.CENSORED,"Player death"));
		var stats=session.stats();
		text=NexSupportReport.snapshot(new NexSupportReport.Frame(stats,runtime(stats,Truth.TRUE)),oldScope,old,0,PRIVATE,
			NexReport.Provenance.SYNTHETIC_OFFLINE,NexReport.State.READY,NexPanel.TrackingStatus.READY);
		assertTrue(text.contains("SESSION_CHANGED")); assertFalse(text.contains("Attempt A")); assertFalse(text.contains(PRIVATE));
		assertTrue(BuildInfo.REVISION.matches("[0-9a-f]{40}|unknown"));
		assertTrue(Set.of("CLEAN","DIRTY","UNKNOWN").contains(BuildInfo.WORKTREE));
	}
	@Test public void freshCopyWithChangedAnnotationsWithholdsTheStaleSelectedCalculation()
	{
		var session=new NexSession(); session.select(PRIVATE,301);
		var selected=summary(1,Outcome.COMPLETE,PRIVATE); session.add(selected);
		assertTrue(session.annotate(selected.key,0,NexContext.PILOT));
		String text=report(session,selected,PRIVATE,NexReport.State.READY);
		assertTrue(text.contains("Attempt A1: outcome=COMPLETE"));
		assertTrue(text.contains("Selected record unavailable or revised")); assertFalse(text.contains(PRIVATE));
	}
}
