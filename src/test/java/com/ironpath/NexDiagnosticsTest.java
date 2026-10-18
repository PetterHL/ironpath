package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.*;
import com.ironpath.dev.PanelPreview;
import com.ironpath.encounter.CombatState;
import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.nex.*;
import com.ironpath.preferences.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Export privacy and coherence are product contracts, independent of clipboard availability. */
public class NexDiagnosticsTest
{
	private static final String PRIVATE="PRIVATE_SENTINEL_ACCOUNT_ROSTER";
	private final KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson());
	public NexDiagnosticsTest() throws Exception { }
	private NexSummary summary(long key,boolean arrows)
	{
		CombatState base=NexOwnedFixtures.state(NexOwnedFixtures.ranged(),arrows?Map.of(11212,200L,999999,12345L):Map.of(999999,12345L));
		Map<String,Integer> levels=new HashMap<>(base.visibleLevels); levels.put(PRIVATE,999);
		var state=new CombatState(base.slots,base.realLevels,levels,base.prayers,base.style,base.specialEnergy,base.specialSelected,
			base.inventory,base.resources,Map.of(20997,PRIVATE));
		var sample=new NexSummary.Sample(10,"SHADOW","EMBRACE_DARKNESS","NEX@"+PRIVATE,"ranged",-1,state);
		return new NexSummary(key,0,100,100,100,0,Outcome.COMPLETE,PRIVATE,PRIVATE,PRIVATE,List.of(sample),
			Map.of(PRIVATE,new NexSummary.Damage(20,80)),Set.of(PRIVATE),
			new NexSummary.Signals(1,0,100,101,true,Set.of("SHADOW"),0,List.of(PRIVATE)));
	}
	private NexOwnedResult evaluate(NexSummary summary)
	{ return new NexOwnedEvaluator(catalog).evaluate(summary,NexOwnedFixtures.account(null),Preferences.EMPTY); }
	private NexReport report(NexSummary summary,NexOwnedResult result,int index,int pair,NexReport.State state)
	{ return NexReport.snapshot(summary,result,index,pair,catalog,NexReport.Provenance.SYNTHETIC_OFFLINE,state); }
	@Test public void changedArrowIsIdentifiedAndDuplicatePackagesKeepCaptureOrder()
	{
		var summary=summary(1,true); var result=evaluate(summary);
		var cards=NexPresentation.cards(result); assertEquals(1,cards.size());
		assertEquals(Map.of("ammo",11212),NexPresentation.changedItems(cards.get(0).pair));
		var duplicated=new NexOwnedResult(summary,List.of(result.choices.get(0),result.choices.get(0)));
		assertEquals(1,NexPresentation.cards(duplicated).size());
		assertEquals(0,NexPresentation.cards(duplicated).get(0).choiceIndex);
		assertTrue(NexPresentation.cards(evaluate(summary(2,false))).isEmpty());
	}
	@Test public void reportUsesAnAllowlistAndExactFrozenComparisonInputs()
	{
		var summary=summary(1,true); var result=evaluate(summary); var snapshot=report(summary,result,0,0,NexReport.State.READY);
		String text=snapshot.format();
		assertFalse(text,text.contains(PRIVATE)); assertFalse(text.contains("999999")); assertFalse(text.contains("12345"));
		assertTrue(text.contains("schema 1")); assertTrue(text.contains("SYNTHETIC_OFFLINE"));
		assertTrue(text.contains("Report-local kill: K1")); assertFalse(text.contains("kill #1"));
		assertTrue(text.contains("immutable calculation revision: "+result.revision));
		assertTrue(text.contains("20 / 80; share percent: 25.0"));
		assertTrue(text.contains("RANGED=112")); assertTrue(text.contains("ammo=11212"));
		assertTrue(text.contains("Relevant carried candidate quantity · item 11212: 200"));
		assertTrue(text.contains("selected quiver projectile: 21326"));
		assertTrue(text.contains("No kill timestamp recorded")); assertTrue(text.contains("Before metrics:"));
		assertTrue(text.contains("Frozen planning assumption")); assertTrue(text.contains("unverified"));
		// Mutable catalogue target must not change an already constructed report model.
		result.choices.get(0).comparison.target.defenceLevel=1;
		assertEquals(text,snapshot.format());
	}
	@Test public void hiddenOrMismatchedResultsCannotBeExported()
	{
		var summary=summary(1,true); var result=evaluate(summary);
		for(var state:List.of(NexReport.State.NO_ACCOUNT,NexReport.State.PAUSED,NexReport.State.RESET,NexReport.State.NOT_READY))
		{
			String text=report(summary,result,0,0,state).format();
			assertFalse(text.contains("Report-local kill:")); assertFalse(text.contains("Actual slots")); assertFalse(text.contains("25.0"));
		}
		String text=report(summary,evaluate(summary(2,true)),0,0,NexReport.State.PENDING).format();
		assertTrue(text.contains("No other kill's result exported")); assertFalse(text.contains("Candidate slots"));
	}
	@Test public void outputIsBoundedAndDisclosesOmittedFields()
	{
		var summary=summary(1,true); var result=evaluate(summary);
		catalog.sources.values().forEach(source->source.revision="x".repeat(NexReport.MAX_CHARS));
		String text=report(summary,result,0,0,NexReport.State.READY).format();
		assertTrue(text.length()<=NexReport.MAX_CHARS); assertTrue(text.contains("fields omitted by size limit"));
		assertTrue(text.contains("Omissions:"));
	}
	@Test public void clipboardFailureIsSelectableAndHistoricalExportStaysFrozenAfterNewKill() throws Exception
	{
		var first=summary(1,true); var original=evaluate(first); var second=summary(2,false); var next=evaluate(second);
		SwingUtilities.invokeAndWait(()->{
			var session=new NexSession(); session.select("test",301); session.add(first);
			var account=NexOwnedFixtures.account(null); var copied=new AtomicReference<String>();
			var panel=new NexPanel(null,text->{ copied.set(text); throw new IllegalStateException("busy"); },NexReport.Provenance.SYNTHETIC_OFFLINE);
			panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",original);
			assertTrue(PanelPreview.text(panel).contains("Dragon arrow")); assertFalse(PanelPreview.text(panel).contains("Twisted bow"));
			assertTrue(PanelPreview.text(panel).contains("1 matching kill")); assertFalse(PanelPreview.text(panel).contains("Session mean"));
			PanelPreview.click(panel,"Compare package"); PanelPreview.click(panel,"Diagnostics");
			PanelPreview.click(panel,"Copy report now");
			assertTrue(PanelPreview.text(panel).contains("Clipboard unavailable or busy"));
			var area=area(panel); assertTrue(area.isFocusable()); assertFalse(area.isEditable());
			assertEquals(copied.get(),area.getText()); assertTrue(PanelPreview.visible(area));
			String frozen=area.getText(); JButton stale=copyButton(panel);
			session.add(second);
			panel.render("New kill",account,Preferences.EMPTY.nex(NexMethod.PILOT.lock("ranged/ammo",0)),catalog,session.stats(),true,actions(),"local",next);
			assertEquals("Removed text widget cannot retain an old report", "",area.getText());
			PanelPreview.click(panel,"Copy report now"); assertEquals(frozen,copied.get());
			PanelPreview.click(panel,"Encounter"); PanelPreview.click(panel,"Owned setup comparisons");
			PanelPreview.combo(panel,"nex-owned-support-filter").setSelectedItem("Unavailable comparisons");
			PanelPreview.click(panel,"Diagnostics"); PanelPreview.click(panel,"Copy report now");
			assertTrue(copied.get().contains("No selected comparison")); assertFalse(copied.get().contains("Actual slots"));
			panel.render("Identity changed",null,Preferences.EMPTY,catalog,session.stats(),true,null,"");
			copied.set("unchanged"); stale.doClick(); assertEquals("unchanged",copied.get());
			PanelPreview.click(panel,"Diagnostics"); PanelPreview.click(panel,"Copy report now");
			assertFalse(copied.get().contains("Actual slots")); assertFalse(copied.get().contains("25.0"));
		});
	}
	@Test public void diagnosticsIsReachableForEmptyPendingResetAndPausedStates() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			var session=new NexSession(); session.select("test",301);
			var account=NexOwnedFixtures.account(null); var copied=new AtomicReference<String>();
			var panel=new NexPanel(null,copied::set,NexReport.Provenance.SYNTHETIC_OFFLINE);
			panel.render("Waiting for login",null,Preferences.EMPTY,catalog,session.stats(),true,null,"");
			PanelPreview.click(panel,"Diagnostics"); PanelPreview.click(panel,"Copy report now");
			assertTrue(copied.get().contains("NO_ACCOUNT"));
			var summary=summary(1,true); session.add(summary);
			panel.render("Pending",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"",null);
			PanelPreview.click(panel,"Copy report now"); assertTrue(copied.get().contains("PENDING"));
			panel.render("Ready",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"",evaluate(summary));
			PanelPreview.click(panel,"Copy report now"); assertTrue(copied.get().contains("READY"));
			panel.render("Nex session paused. Open a bank to revalidate.",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"",evaluate(summary));
			PanelPreview.click(panel,"Copy report now"); assertTrue(copied.get().contains("PAUSED")); assertFalse(copied.get().contains("Actual slots"));
			session.clear("Reset session selected."); session.select("test",301);
			panel.render("Cleared",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"",null);
			PanelPreview.click(panel,"Copy report now"); assertTrue(copied.get().contains("RESET")); assertFalse(copied.get().contains("Report-local kill:"));
		});
	}
	private static JTextArea area(NexPanel panel)
	{ return (JTextArea)PanelPreview.children(panel).stream().filter(c->"nex-report-text".equals(c.getName())).findFirst().orElseThrow(); }
	private static JButton copyButton(NexPanel panel)
	{ return (JButton)PanelPreview.children(panel).stream().filter(c->"nex-copy-report".equals(c.getName())).findFirst().orElseThrow(); }
	private static NexPanel.Actions actions()
	{
		return new NexPanel.Actions() {
			public boolean current() { return true; } public boolean canEdit() { return true; }
			public void method(NexMethod method) {} public void confirm(String key,Boolean value) {}
			public void reset() {} public void enable(boolean value) {}
		};
	}
}
