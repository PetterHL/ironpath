package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.*;
import com.ironpath.dev.PanelPreview;
import com.ironpath.nex.*;
import com.ironpath.preferences.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.accessibility.AccessibleState;
import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NexAccordionTest
{
	private static <T extends Component> T named(Container panel,String name,Class<T> type)
	{ return NexScenarioPanelTest.named(panel,name,type); }
	private static JButton header(NexPanel panel,String name) { return named(panel,"nex-section-"+name,JButton.class); }
	private static NexPanel.Actions actions()
	{
		return new NexPanel.Actions() {
			public boolean current() { return true; } public boolean canEdit() { return true; }
			public void method(NexMethod value) {} public void confirm(String key,Boolean value) {}
			public void enable(boolean value) {} public void reset() {}
		};
	}
	@Test public void independentKeyboardSectionsKeepDraftSelectionScrollAndReportsAcrossRefreshAndTravel() throws Exception
	{
		var catalog=KnowledgeCatalog.load(new Gson());
		var source=NexPlanComparisonTest.fixture(catalog,27690,0,Map.of(),Map.of("inventory-slots-used",0),true);
		var account=NexOwnedFixtures.account(null); var session=new NexSession(); session.select("synthetic",301); session.add(source.summary);
		var panel=new NexPanel(null); var viewport=new JViewport();
		AtomicReference<JTextArea> report=new AtomicReference<>(); AtomicReference<JTextField> draft=new AtomicReference<>();
		AtomicReference<NexScenarioPanel> planner=new AtomicReference<>(); AtomicReference<Point> position=new AtomicReference<>();
		SwingUtilities.invokeAndWait(()->{
			panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",source);
			assertTrue(header(panel,"compare").isSelected());
			for(String name:List.of("team","diagnostics"))
			{ assertFalse(header(panel,name).isSelected()); assertTrue(header(panel,name).getAccessibleContext().getAccessibleStateSet().contains(AccessibleState.COLLAPSED)); }
			JButton team=header(panel,"team"); team.getActionMap().get("activate").actionPerformed(new java.awt.event.ActionEvent(team,0,"ENTER"));
			assertTrue(team.getAccessibleContext().getAccessibleStateSet().contains(AccessibleState.EXPANDED));
			JTextField count=named(panel,"nex-context-count",JTextField.class); count.setText("7"); draft.set(count);
			PanelPreview.click(panel,"Base-stat comparisons"); PanelPreview.click(panel,"Plan specs & gear");
			var plan=(NexScenarioPanel)PanelPreview.children(panel).stream().filter(c->c instanceof NexScenarioPanel).findFirst().orElseThrow(); planner.set(plan);
			named(plan,"n3-horizon",JTextField.class).setText("37");
			PanelPreview.openSection(panel,"Diagnostics"); PanelPreview.click(panel,"Copy report now");
			JTextArea area=named(panel,"nex-report-text",JTextArea.class); report.set(area); area.select(2,12);
			// Leave room for the report, which intentionally clears during suspension.
			viewport.setView(panel); viewport.setSize(225,600); settleLayout(panel); viewport.setViewPosition(new Point(0,1600)); position.set(viewport.getViewPosition());
			panel.render("Inventory refresh",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",source);
			settleLayout(panel);
			assertSame(count,named(panel,"nex-context-count",JTextField.class)); assertEquals("7",count.getText());
			assertSame(plan,PanelPreview.children(panel).stream().filter(c->c instanceof NexScenarioPanel).findFirst().orElseThrow());
			assertEquals("37",named(plan,"n3-horizon",JTextField.class).getText());
			assertSame(area,named(panel,"nex-report-text",JTextArea.class)); assertEquals(2,area.getSelectionStart()); assertEquals(12,area.getSelectionEnd());
			assertTrue(header(panel,"team").isSelected());
		});
		SwingUtilities.invokeAndWait(()->assertEquals(position.get(),viewport.getViewPosition()));
		SwingUtilities.invokeAndWait(()->{
			PanelPreview.closeSection(panel,"Team & method"); PanelPreview.openSection(panel,"Team & method");
			assertSame(draft.get(),named(panel,"nex-context-count",JTextField.class)); assertSame(report.get(),named(panel,"nex-report-text",JTextArea.class));
			session.suspend("synthetic",301,130,true,true);
			panel.render("Nex session paused",null,Preferences.EMPTY,catalog,session.stats(),true,null,"",null,false,NexPanel.TrackingStatus.LOADING);
			assertFalse(PanelPreview.text(panel).contains("Owned gear · kill")); assertEquals("",report.get().getText());
			settleLayout(panel);
		});
		// Let the shortened suspension actually lay out and run the deferred scroll clamp.
		SwingUtilities.invokeAndWait(()->{
			assertTrue("Suspension clamps the visible scroll",viewport.getViewPosition().y<position.get().y);
			panel.render("Nex session paused",null,Preferences.EMPTY,catalog,session.stats(),true,null,"",null,false,NexPanel.TrackingStatus.BANK);
			settleLayout(panel);
		});
		SwingUtilities.invokeAndWait(()->{
			session.revalidate("synthetic",301,140,true,false);
			panel.render("Verified return",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",source,false,NexPanel.TrackingStatus.READY);
			// A second queued refresh must not consume the saved offset before it is applied.
			panel.render("Return inventory refresh",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",source,false,NexPanel.TrackingStatus.READY);
			settleLayout(panel);
			assertSame(draft.get(),named(panel,"nex-context-count",JTextField.class)); assertEquals("7",draft.get().getText());
			assertSame(planner.get(),PanelPreview.children(panel).stream().filter(c->c instanceof NexScenarioPanel).findFirst().orElseThrow());
			assertEquals("37",named(planner.get(),"n3-horizon",JTextField.class).getText());
		});
		SwingUtilities.invokeAndWait(()->assertEquals("Verified return restores the pre-suspension scroll",position.get(),viewport.getViewPosition()));
		SwingUtilities.invokeAndWait(()->{
			session.suspend("synthetic",301,150,true,true);
			panel.render("Nex session paused",null,Preferences.EMPTY,catalog,session.stats(),true,null,"",null,false,NexPanel.TrackingStatus.LOADING);
			settleLayout(panel);
		});
		SwingUtilities.invokeAndWait(()->{
			session.clear("Reset"); session.select("different",301);
			panel.render("Account changed",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local");
			settleLayout(panel);
			assertNotSame(draft.get(),named(panel,"nex-context-count",JTextField.class));
			assertFalse(PanelPreview.text(panel).contains("Owned gear · kill"));
			assertNull(named(panel,"n3-horizon",JTextField.class));
		});
		SwingUtilities.invokeAndWait(()->assertEquals("Hard scope clears the suspended offset",new Point(),viewport.getViewPosition()));
	}
	private static void settleLayout(Container root)
	{
		root.setSize(225,10000);
		for(int i=0;i<4;i++) { layout(root); root.setSize(225,Math.max(600,root.getPreferredSize().height)); }
		layout(root);
	}
	private static void layout(Container root)
	{ root.doLayout(); for(Component c:root.getComponents()) { if(c instanceof Container) { layout((Container)c); } } }
	@Test public void firstOpeningTeamTargetsTheLatestKillRatherThanAnUntouchedHiddenEditor() throws Exception
	{
		var catalog=KnowledgeCatalog.load(new Gson()); var session=new NexSession(); session.select("synthetic",301);
		var first=NexCaptureTest.replay(0,20,60); var base=NexCaptureTest.replay(100,40,40);
		var next=new NexSummary(2,base.startTick,base.endTick,base.sampledTicks,base.expectedTicks,base.unclassifiedHits,
			base.outcome,base.reason,base.cohort,base.method,base.timeline,base.damage,base.gaps,base.signals,base.beforeStart);
		SwingUtilities.invokeAndWait(()->{
			var panel=new NexPanel(null); var account=NexOwnedFixtures.account(null); session.add(first);
			panel.render("First result",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"");
			session.add(next); panel.render("Second result",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"");
			PanelPreview.openSection(panel,"Team & method");
			var editor=(NexContextEditor)PanelPreview.children(panel).stream().filter(c->c instanceof NexContextEditor).findFirst().orElseThrow();
			assertEquals(2,editor.key);
			// A default draft deliberately opened before any kill keeps its own target.
			var empty=new NexSession(); empty.select("synthetic",301); var defaults=new NexPanel(null);
			defaults.render("No kill",account,Preferences.EMPTY,catalog,empty.stats(),true,actions(),"");
			PanelPreview.openSection(defaults,"Team & method");
			var draft=named(defaults,"nex-context-count",JTextField.class); draft.setText("7");
			empty.add(first); defaults.render("First result",account,Preferences.EMPTY,catalog,empty.stats(),true,actions(),"");
			assertSame(draft,named(defaults,"nex-context-count",JTextField.class)); assertEquals("7",draft.getText());
			var defaultEditor=(NexContextEditor)PanelPreview.children(defaults).stream().filter(c->c instanceof NexContextEditor).findFirst().orElseThrow();
			assertEquals(-1,defaultEditor.key);
		});
	}
	@Test public void savedCorrectionsKeepProvenanceAndObservedFactPrecedenceInDiagnostics() throws Exception
	{
		var catalog=KnowledgeCatalog.load(new Gson()); var session=new NexSession(); session.select("synthetic",301);
		var account=NexOwnedFixtures.account(null); var when=java.time.Instant.parse("2026-10-19T10:00:00Z");
		var prefs=Preferences.EMPTY.confirm("owns-r3bFang",true,when).confirm("rigour",false,when);
		SwingUtilities.invokeAndWait(()->{
			var panel=new NexPanel(null); panel.render("Observed",account,prefs,catalog,session.stats(),true,actions(),"local");
			assertNull(header(panel,"gear")); assertNull(header(panel,"unlocks"));
			PanelPreview.openSection(panel,"Diagnostics"); assertTrue(PanelPreview.text(panel).contains("manual planning confirmation (false) at "+when));
			assertEquals(Boolean.FALSE,prefs.confirmations.get("rigour").value); assertTrue(account.rigour.value());
			assertEquals(com.ironpath.model.Truth.TRUE,AccountFacts.planning(account,prefs,"rigour"));
		});
	}
}
