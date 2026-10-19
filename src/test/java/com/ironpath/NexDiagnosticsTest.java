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
	{ return summary(key,arrows,"EMBRACE_DARKNESS"); }
	private NexSummary summary(long key,boolean arrows,String window)
	{
		CombatState base=NexOwnedFixtures.state(NexOwnedFixtures.ranged(),arrows?Map.of(11212,200L,999999,12345L):Map.of(999999,12345L));
		Map<String,Integer> levels=new HashMap<>(base.visibleLevels); levels.put(PRIVATE,999);
		var state=new CombatState(base.slots,base.realLevels,levels,base.prayers,base.style,base.specialEnergy,base.specialSelected,
			base.inventory,base.resources,Map.of(20997,PRIVATE));
		var sample=new NexSummary.Sample(10,"SHADOW",window,"NEX@"+PRIVATE,"ranged",-1,state);
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
		assertTrue(text.contains("schema 2")); assertTrue(text.contains("SYNTHETIC_OFFLINE"));
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
	@Test public void compactCardsExpandWithoutDroppingAlternativesAndKeepSelectionWhenReturningFromDiagnostics() throws Exception
	{
		List<NexSummary.Sample> samples=new ArrayList<>();
		for(String missing:List.of("none","head","neck"))
		{
			var slots=new LinkedHashMap<>(NexOwnedFixtures.ranged()); if(!missing.equals("none")) { slots.put(missing,0); }
			samples.add(NexOwnedFixtures.sample("ranged",NexOwnedFixtures.state(slots,Map.of(11212,200L))));
		}
		var summary=NexOwnedFixtures.summary(samples); var result=evaluate(summary); var cards=NexPresentation.cards(result);
		assertTrue("Three distinct complete-package changes",cards.size()>=3);
		SwingUtilities.invokeAndWait(()->{
			var session=new NexSession(); session.select("test",301); session.add(summary);
			var panel=new NexPanel(null); var account=NexOwnedFixtures.account(null);
			panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"",result);
			assertTrue(selectedTab(panel,"nex-nav-encounter")); assertFalse(selectedTab(panel,"nex-nav-diagnostics"));
			assertEquals(2,visibleCards(panel));
			PanelPreview.click(panel,"More comparisons ("+(cards.size()-2)+")"); assertEquals(cards.size(),visibleCards(panel));
			panel.render("Account refresh",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"",result);
			assertEquals("Expansion survives refresh",cards.size(),visibleCards(panel));
			var third=cards.get(2);
			((JButton)PanelPreview.children(panel).stream().filter(c->("nex-card-"+third.choiceIndex).equals(c.getName())).findFirst().orElseThrow()).doClick();
			assertEquals(third.pairIndex,PanelPreview.combo(panel,"nex-owned-pair").getSelectedIndex());
			PanelPreview.click(panel,"Diagnostics"); assertTrue(selectedTab(panel,"nex-nav-diagnostics"));
			PanelPreview.click(panel,"Back to comparison");
			assertEquals(third.pairIndex,PanelPreview.combo(panel,"nex-owned-pair").getSelectedIndex());
			PanelPreview.click(panel,"Back to encounter"); assertEquals(cards.size(),visibleCards(panel));
			PanelPreview.click(panel,"Fewer comparisons"); assertEquals(2,visibleCards(panel));
			PanelPreview.click(panel,"Diagnostics"); PanelPreview.click(panel,"Calculation details");
			assertTrue(PanelPreview.text(panel).contains("build "+BuildInfo.VERSION));
			assertTrue(PanelPreview.text(panel).contains("Independent state comparisons"));
		});
	}
	private static long visibleCards(NexPanel panel)
	{ return PanelPreview.children(panel).stream().filter(c->c.getName()!=null && c.getName().startsWith("nex-equipment-card-") && PanelPreview.visible(c)).count(); }
	private static boolean selectedTab(NexPanel panel,String name)
	{ return ((JButton)PanelPreview.children(panel).stream().filter(c->name.equals(c.getName())).findFirst().orElseThrow()).isSelected(); }
	@Test public void diagnosticCalculationDetailsSurviveAccountRefreshesUntilTheSelectedKillChanges() throws Exception
	{
		var first=summary(1,true); var original=evaluate(first); var second=summary(2,false); var next=evaluate(second);
		SwingUtilities.invokeAndWait(()->{
			var session=new NexSession(); session.select("test",301); session.add(first);
			var panel=new NexPanel(null);
			panel.render("Observed",NexOwnedFixtures.account(null),Preferences.EMPTY,catalog,session.stats(),true,actions(),"",original);
			PanelPreview.click(panel,"Diagnostics"); PanelPreview.click(panel,"Calculation details");
			for(String status:List.of("Inventory refreshed","Equipment refreshed","Bank refreshed"))
			{
				panel.render(status,NexOwnedFixtures.account(Set.of(11212)),Preferences.EMPTY,catalog,session.stats(),true,actions(),"",original);
				assertTrue("Keep expanded calculation details after "+status,PanelPreview.text(panel).contains("State coverage"));
			}
			PanelPreview.click(panel,"Calculation details");
			panel.render("Another refresh",NexOwnedFixtures.account(null),Preferences.EMPTY,catalog,session.stats(),true,actions(),"",original);
			assertFalse("An explicitly collapsed section stays collapsed",PanelPreview.text(panel).contains("State coverage"));
			PanelPreview.click(panel,"Calculation details"); session.add(second);
			panel.render("New result",NexOwnedFixtures.account(null),Preferences.EMPTY,catalog,session.stats(),true,actions(),"",next);
			assertTrue("A newer kill does not replace frozen Diagnostics or its disclosure",PanelPreview.text(panel).contains("State coverage"));
			PanelPreview.click(panel,"Encounter"); PanelPreview.click(panel,"Diagnostics");
			assertFalse("A different selected kill starts collapsed",PanelPreview.text(panel).contains("State coverage"));
			PanelPreview.click(panel,"Calculation details");
			panel.render("Logged out",null,Preferences.EMPTY,catalog,session.stats(),true,null,"");
			assertFalse("Identity clearing cannot retain expanded history",PanelPreview.text(panel).contains("State coverage"));
		});
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
	@Test public void reportPreservesRecognizedWindowsWithoutEnablingUnsupportedComparisons()
	{
		for(String window:List.of("UNKNOWN","NONE","FEAR_THE_SHADOW","EMBRACE_DARKNESS",
			"BLOOD_SIPHON","BLOOD_SACRIFICE","ICE_CONTAIN","ICE_PRISON",
			"MINION_FUMUS","MINION_UMBRA","MINION_CRUOR","MINION_GLACIES",PRIVATE))
		{
			var summary=summary(1,true,window); var result=evaluate(summary);
			String text=report(summary,result,0,0,NexReport.State.READY).format();
			assertTrue(window,text.contains("; window: "+(window.equals(PRIVATE)?"UNKNOWN":window)+"\n"));
			assertFalse(text,text.contains(PRIVATE));
			if(!Set.of("UNKNOWN","NONE","FEAR_THE_SHADOW","EMBRACE_DARKNESS").contains(window))
			{
				assertFalse(window,result.choices.get(0).supported());
				assertTrue(text.contains("Selected comparison unavailable:"));
				assertFalse(text.contains("Before metrics:"));
			}
		}
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
			assertFalse(PanelPreview.text(panel).contains("No matched average")); assertFalse(PanelPreview.text(panel).contains("Session mean"));
			PanelPreview.click(panel,"Compare package"); PanelPreview.click(panel,"Diagnostics");
			PanelPreview.click(panel,"Copy report now");
			assertTrue(PanelPreview.text(panel).contains("Clipboard unavailable or busy"));
			var area=area(panel); assertTrue(area.isFocusable()); assertFalse(area.isEditable());
			assertEquals(copied.get(),area.getText()); assertTrue(PanelPreview.visible(area));
			String frozen=area.getText(); JButton stale=copyButton(panel);
			var scroll=(JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class,area);
			// Keep the focused subtree attached; removing it would lose native focus even
			// if a replacement later restored the same selection in a headless test.
			var detachments=new java.util.concurrent.atomic.AtomicInteger();
			scroll.addHierarchyListener(e->{ if((e.getChangeFlags()&java.awt.event.HierarchyEvent.PARENT_CHANGED)!=0) { detachments.incrementAndGet(); } });
			scroll.setSize(201,180); scroll.doLayout(); scroll.getViewport().doLayout();
			area.setCaretPosition(150); area.moveCaretPosition(50);
			scroll.getViewport().setViewPosition(new java.awt.Point(0,120));
			var position=scroll.getViewport().getViewPosition(); assertEquals(120,position.y);
			panel.render("Inventory refreshed",NexOwnedFixtures.account(null),Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",original);
			assertReportInteraction(panel,area,frozen,position,detachments.get());
			assertTrue(PanelPreview.text(panel).contains("Clipboard unavailable or busy"));
			session.add(second);
			panel.render("New kill",account,Preferences.EMPTY.nex(NexMethod.PILOT.lock("ranged/ammo",0)),catalog,session.stats(),true,actions(),"local",next);
			assertReportInteraction(panel,area,frozen,position,detachments.get());
			copied.set("unchanged"); stale.doClick(); assertEquals("unchanged",copied.get());
			PanelPreview.click(panel,"Copy report now"); assertEquals(frozen,copied.get());
			PanelPreview.click(panel,"Encounter");
			assertEquals("Removed text widget cannot retain an old report", "",area.getText());
			PanelPreview.click(panel,"Owned setup comparisons");
			PanelPreview.combo(panel,"nex-owned-support-filter").setSelectedItem("Unavailable comparisons");
			PanelPreview.click(panel,"Diagnostics"); PanelPreview.click(panel,"Copy report now");
			assertTrue(copied.get().contains("No selected comparison")); assertFalse(copied.get().contains("Actual slots"));
			panel.render("Identity changed",null,Preferences.EMPTY,catalog,session.stats(),true,null,"");
			copied.set("unchanged"); stale.doClick(); assertEquals("unchanged",copied.get());
			PanelPreview.click(panel,"Diagnostics"); PanelPreview.click(panel,"Copy report now");
			assertFalse(copied.get().contains("Actual slots")); assertFalse(copied.get().contains("25.0"));
		});
	}
	private static void assertReportInteraction(NexPanel panel,JTextArea original,String frozen,java.awt.Point position,int detachments)
	{
		// Inspect before another Copy action: reopening would conceal the refresh bug.
		assertTrue("Fallback stays expanded",PanelPreview.visible(area(panel)));
		assertSame("Keep the focused report component",original,area(panel));
		assertEquals(frozen,original.getText());
		assertEquals(frozen.substring(50,150),original.getSelectedText());
		assertEquals(50,original.getCaret().getDot()); assertEquals(150,original.getCaret().getMark());
		var scroll=(JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class,original);
		scroll.doLayout(); scroll.getViewport().doLayout();
		assertEquals(position,scroll.getViewport().getViewPosition());
		assertEquals("Do not detach the focused subtree",0,detachments);
	}
	@Test public void inaccessibleReportIsClearedAndStaleCopyCannotRestoreIt() throws Exception
	{
		var summary=summary(1,true); var result=evaluate(summary);
		SwingUtilities.invokeAndWait(()->{
			for(String transition:List.of("Nex session paused. Revalidate at a bank.","Logged out","Account/profile changed","Preferences unavailable","Reset"))
			{
				var session=new NexSession(); session.select("test",301); session.add(summary);
				var account=NexOwnedFixtures.account(null); var copied=new AtomicReference<String>();
				var panel=new NexPanel(null,copied::set,NexReport.Provenance.SYNTHETIC_OFFLINE);
				panel.render("Ready",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"",result);
				PanelPreview.click(panel,"Diagnostics"); PanelPreview.click(panel,"Copy report now");
				var previous=area(panel); var stale=copyButton(panel);
				previous.select(10,100); assertTrue(PanelPreview.visible(previous));
				if(transition.equals("Reset")) { session.clear("Reset session selected."); session.select("test",301); }
				boolean noAccount=transition.equals("Logged out") || transition.equals("Account/profile changed");
				panel.render(transition,noAccount?null:account,Preferences.EMPTY,catalog,session.stats(),true,
					noAccount || transition.equals("Preferences unavailable")?null:actions(),"",result);
				assertEquals(transition,"",previous.getText());
				assertFalse(transition,PanelPreview.visible(area(panel)));
				copied.set("unchanged"); stale.doClick(); assertEquals(transition,"unchanged",copied.get());
				PanelPreview.click(panel,"Copy report now");
				assertFalse(transition,copied.get().contains("Report-local kill:"));
				assertFalse(transition,copied.get().contains("Actual slots"));
			}
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
			var pending=area(panel); pending.select(10,100);
			panel.render("Ready",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"",evaluate(summary));
			assertEquals("A different report clears the old widget","",pending.getText());
			assertFalse("A different report starts collapsed",PanelPreview.visible(area(panel)));
			PanelPreview.click(panel,"Copy report now"); assertTrue(copied.get().contains("READY"));
			panel.render("Nex session paused. Open a bank to revalidate.",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"",evaluate(summary));
			PanelPreview.click(panel,"Copy report now"); assertTrue(copied.get().contains("PAUSED")); assertFalse(copied.get().contains("Actual slots"));
			session.clear("Reset session selected."); session.select("test",301);
			panel.render("Cleared",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"",null);
			PanelPreview.click(panel,"Copy report now"); assertTrue(copied.get().contains("RESET")); assertFalse(copied.get().contains("Report-local kill:"));
		});
	}
	@Test public void diagnosticsOpenedBeforeCalculationChoosesTheVisibleImprovementWhenItArrives() throws Exception
	{
		var original=summary(1,true); var visible=original.timeline.get(0);
		var noTarget=new NexSummary.Sample(0,visible.phase,visible.window,null,visible.role,-1,visible.state);
		var summary=new NexSummary(1,0,100,100,100,0,Outcome.COMPLETE,"synthetic","synthetic","synthetic",List.of(noTarget,visible),
			original.damage,Set.of(),original.signals);
		var result=evaluate(summary); assertFalse(result.choices.get(0).supported()); assertFalse(NexPresentation.cards(result).isEmpty());
		var card=NexPresentation.cards(result).get(0); assertTrue(card.choiceIndex>0);
		SwingUtilities.invokeAndWait(()->{
			for(boolean bankReturn:List.of(false,true))
			{
				var session=new NexSession(); session.select("test",301); session.add(summary);
				var account=NexOwnedFixtures.account(null); var copied=new AtomicReference<String>();
				var panel=new NexPanel(null,copied::set,NexReport.Provenance.SYNTHETIC_OFFLINE);
				if(bankReturn)
				{
					assertTrue(session.suspend("test",301,100,true));
					panel.render("Nex session paused. Open a bank to revalidate.",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"",null);
					PanelPreview.click(panel,"Diagnostics"); PanelPreview.click(panel,"Copy report now");
					assertTrue(copied.get().contains("PAUSED")); assertFalse(copied.get().contains("Observed slots"));
					assertTrue(session.revalidate("test",301,101,true,false));
				}
				panel.render("Pending",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"",null);
				if(!bankReturn) { PanelPreview.click(panel,"Diagnostics"); }
				PanelPreview.click(panel,"Copy report now"); assertTrue(copied.get().contains("PENDING"));
				panel.render("Ready",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"",result);
				PanelPreview.click(panel,"Copy report now");
				assertTrue(copied.get(),copied.get().contains("Selected scenario: "+(card.choiceIndex+1)+"; pair: "+card.pairIndex));
				assertTrue(copied.get().contains("Before metrics:"));
			}
		});
	}

	@Test public void unavailableRowsRetainAllowlistedObservedEquipmentAndTargetWithoutRawIdentifiers()
	{
		var original=summary(1,true); var before=original.timeline.get(0); var captured=before.state;
		var slots=new HashMap<>(captured.slots); slots.put(PRIVATE,12345);
		var state=new CombatState(slots,captured.realLevels,captured.visibleLevels,captured.prayers,captured.style,
			captured.specialEnergy,captured.specialSelected,captured.inventory,captured.resources,captured.itemNames);
		var sample=new NexSummary.Sample(10,before.phase,before.window,null,before.role,-1,state);
		var summary=new NexSummary(1,0,100,100,100,0,Outcome.COMPLETE,PRIVATE,PRIVATE,PRIVATE,List.of(sample),Map.of(),Set.of(),original.signals);
		var result=evaluate(summary); String text=report(summary,result,0,0,NexReport.State.READY).format();
		assertTrue(text.contains("Observed slots (exact item IDs")); assertTrue(text.contains("weapon=20997"));
		assertTrue(text.contains("Observed target class: UNKNOWN")); assertTrue(text.contains("Selected comparison unavailable: TARGET_UNSUPPORTED"));
		assertFalse(text.contains("Before metrics:")); assertFalse(text.contains(PRIVATE)); assertFalse(text.contains("12345")); assertFalse(text.contains("999999"));
		for(var hidden:List.of(NexReport.State.PAUSED,NexReport.State.NO_ACCOUNT,NexReport.State.RESET,NexReport.State.NOT_READY))
		{ assertFalse(report(summary,result,0,0,hidden).format().contains("Observed slots")); }
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
