package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.advisor.P4aFixtures;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.nex.NexSession;
import com.ironpath.nex.NexCaptureTest;
import com.ironpath.preferences.NexMethod;
import com.ironpath.preferences.Preferences;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Synthetic native Swing captures and navigation, not real RuneLite/player acceptance. */
public class NexPanelTest
{
	private static List<Component> children(Container parent)
	{
		List<Component> result=new ArrayList<>();
		for(Component child:parent.getComponents()) { result.add(child); if(child instanceof Container) { result.addAll(children((Container)child)); } }
		return result;
	}
	private static JButton button(NexPanel panel,String text)
	{ return (JButton)children(panel).stream().filter(c->c instanceof JButton && text.equals(((JButton)c).getToolTipText())).findFirst().orElseThrow(); }
	private static String shown(NexPanel panel)
	{ return children(panel).stream().filter(c->c instanceof JTextArea && visible(c)).map(c->((JTextArea)c).getText()).reduce("",(a,b)->a+"\n"+b); }
	private static boolean visible(Component component)
	{ for(Component c=component;c!=null;c=c.getParent()) { if(!c.isVisible()) { return false; } } return true; }
	private static void assertNoManualSetupControls(NexPanel panel)
	{
		Set<String> removed=Set.of("Setup","Account","Gear & method","Gear & unlocks","Gear locks","Assume upgrade",
			"Save method","Save gear lock","Remove selected gear lock","Remove correction","Upgraded Death Charge","Save lock / correction","Clear selected lock","Owned / unlocked","Not owned / locked","Clear selected fact");
		for(Component child:children(panel))
		{
			if(child instanceof AbstractButton)
			{
				var control=(AbstractButton)child;
				assertFalse("Removed control: "+control.getText(),removed.stream().anyMatch(s->s.equals(control.getText()) || s.equals(control.getToolTipText())));
			}
			assertFalse("Removed lock editor", "nex-lock-slot".equals(child.getName()) || "nex-lock-item".equals(child.getName()) || "nex-section-gear".equals(child.getName()) || "nex-section-unlocks".equals(child.getName()) || "nex-account-choice".equals(child.getName()));
		}
		assertFalse(shown(panel).contains("Account corrections"));
		assertFalse(shown(panel).contains("Planning locks / corrections"));
	}
	private static void layout(Container c) { c.doLayout(); for(Component child:c.getComponents()) { if(child instanceof Container) { layout((Container)child); } } }
	private static void capture(NexPanel panel,String name) throws Exception
	{
		panel.setSize(225,10000);
		// Settle nested width-dependent wrapping, including a just-expanded basis.
		for(int pass=0;pass<3;pass++) { panel.invalidate(); layout(panel); panel.setSize(225,panel.getPreferredSize().height); }
		layout(panel); int height=panel.getHeight();
		BufferedImage image=new BufferedImage(225,height,BufferedImage.TYPE_INT_RGB); Graphics2D graphics=image.createGraphics(); panel.printAll(graphics); graphics.dispose();
		Path directory=Path.of(name.startsWith("resources")?"build/validation/n3":name.startsWith("owned")?"build/validation/n2":"build/validation/n1"); Files.createDirectories(directory); ImageIO.write(image,"png",directory.resolve(name+".png").toFile());
		for(Component c:children(panel))
		{
			if(c instanceof JTextArea && visible(c) && c.getWidth()>0)
			{ assertTrue("Wrapped row width",c.getWidth()<=225); assertTrue("Wrapped row height: "+((JTextArea)c).getText().substring(0,Math.min(40,((JTextArea)c).getText().length())),c.getHeight()>=c.getPreferredSize().height); }
		}
	}
	@Test public void encounterNavigationFreezesInspectedKillAndClearsAllDetailsOnIdentityBoundary() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{try {
			KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson()); NexSession session=new NexSession(); session.select("a",301);
			session.add(NexCaptureTest.replay(0,20,60)); NexPanel panel=new NexPanel(null);
			AccountSnapshot account=P4aFixtures.account(Set.of(11907),99,90);
			NexPanel.Actions actions=actions(); panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions,"local");
			assertTrue(shown(panel).contains("Last kill")); assertFalse(shown(panel).contains("Recommended order"));
			button(panel,"Kill and calculation details").doClick(); assertTrue(shown(panel).contains("Kill #"));
			assertFalse(shown(panel).contains("Capture adapter"));
			com.ironpath.dev.PanelPreview.openSection(panel,"Diagnostics"); button(panel,"Calculation details").doClick();
			session.add(NexCaptureTest.replay(100,40,0)); panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions,"local");
			assertTrue(shown(panel).contains("20 local") || shown(panel).contains("27 local") || shown(panel).contains("20 observed"));
			panel.render("Profile changed",null,Preferences.EMPTY,catalog,session.stats(),true,null,"local");
			assertFalse(shown(panel).contains("local /")); assertFalse(shown(panel).contains("Kill #"));
		} catch(Exception ex) { throw new RuntimeException(ex); } });
	}
	@Test public void anEmptyResetSessionExplainsItsLossWithoutDisplayingOldHistory() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{try {
			var catalog=KnowledgeCatalog.load(new Gson()); var session=new NexSession(); session.select("synthetic-a",301);
			session.add(NexCaptureTest.replay(0,20,60));
			assertFalse(session.suspend("synthetic-a",301,40,false));
			var account=P4aFixtures.account(Set.of(11907),99,90); var panel=new NexPanel(null);
			session.select("synthetic-a",301); session.revalidate("synthetic-a",301,2000,true,false);
			for(float font:List.of(12f,20f))
			{
				panel.setFont(panel.getFont().deriveFont(font));
				panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local");
				assertTrue(shown(panel).contains("Session cleared"));
				com.ironpath.dev.PanelPreview.openSection(panel,"Diagnostics");
				assertTrue(shown(panel).contains("Previous session cleared: Travel did not start"));
				com.ironpath.dev.PanelPreview.encounter(panel);
				assertFalse(shown(panel).contains("synthetic-a")); assertFalse(shown(panel).contains("Last kill"));
				capture(panel,"bank-reset-"+(int)font);
			}
			session.add(NexCaptureTest.replay(100,20,60));
			panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local");
			assertTrue(shown(panel).contains("Last kill")); assertFalse(shown(panel).contains("Previous session cleared"));
		} catch(Exception ex) { throw new RuntimeException(ex); } });
	}
	@Test public void resumedCaptureReplacesThePausedBannerAndShowsConcreteEligibilitySignals() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{try {
			var catalog=KnowledgeCatalog.load(new Gson()); var session=new NexSession(); session.select("a",301);
			var account=P4aFixtures.account(Set.of(11907),99,90); var panel=new NexPanel(null);
			panel.render("Nex session paused. Open a bank to revalidate.",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",null);
			assertTrue(shown(panel).contains("Open bank to verify"));
			panel.render("Nex session updated locally.",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",null,true);
			assertTrue(shown(panel).contains("Tracking kill…")); assertFalse(shown(panel).contains("Open bank to verify"));
			assertFalse(shown(panel).contains("Ready to observe the next"));
			session.add(NexCaptureTest.replay(0,20,60));
			panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local");
			button(panel,"Kill and calculation details").doClick();
			assertFalse(shown(panel).contains("Measured start"));
			com.ironpath.dev.PanelPreview.openSection(panel,"Diagnostics"); button(panel,"Calculation details").doClick();
			assertTrue(shown(panel).contains("Measured start (smoke): tick 0"));
			assertTrue(shown(panel).contains("Nex death/dying: tick 30"));
			assertTrue(shown(panel).contains("Arena end boundary: tick 31"));
			assertTrue(shown(panel).contains("Smoke line observed: tick 0"));
			assertTrue(shown(panel).contains("Capture adapter "+com.ironpath.nex.NexCapture.VERSION));
			assertTrue(shown(panel).contains("Barrier readings:"));
			button(panel,"Capture signal trace").doClick();
		} catch(Exception ex) { throw new RuntimeException(ex); } });
	}
	@Test public void pausedSessionOffersResetAndRejectsAStaleResetButton() throws Exception
	{
		var catalog=KnowledgeCatalog.load(new Gson());
		SwingUtilities.invokeAndWait(()->{
			var session=new NexSession(); session.select("a",301);
			var complete=NexCaptureTest.replay(0,20,60); session.add(complete); session.suspend("a",301,40,true);
			var account=P4aFixtures.account(Set.of(11907),99,90); var panel=new NexPanel(null);
			var resets=new java.util.concurrent.atomic.AtomicInteger();
			NexPanel.Actions actions=new NexPanel.Actions()
			{
				public boolean current() { return true; } public boolean canEdit() { return true; }
				public void method(NexMethod method) {} public void confirm(String key,Boolean value) {}
				public void reset() { resets.incrementAndGet(); session.clear(); } public void enable(boolean enabled) {}
			};
			panel.render("Nex session paused. Open a bank to revalidate.",account,Preferences.EMPTY,catalog,session.stats(),true,actions,"local");
			assertFalse(shown(panel).contains("Last kill"));
			JButton reset=button(panel,"Clear session"); reset.doClick();
			assertEquals(1,resets.get()); assertFalse(session.suspended()); assertTrue(session.recent().isEmpty());
			panel.render("Account changed",null,Preferences.EMPTY,catalog,session.stats(),true,null,"local");
			reset.doClick(); assertEquals("An old view cannot reset a new session",1,resets.get());
		});
	}
	@Test public void sidebarOmitsManualGearAndUnlockControlsAcrossSessionStates() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{try {
			var catalog=KnowledgeCatalog.load(new Gson()); var session=new NexSession(); session.select("a",301);
			var account=P4aFixtures.account(Set.of(11907),99,90);
			var prefs=Preferences.EMPTY.nex(NexMethod.PILOT.lock("melee/shield",22322)).confirm("rigour",true,java.time.Instant.EPOCH);
			String paused="Nex session paused. Open a bank to revalidate this account.";
			for(float font:List.of(12f,20f))
			{
				var panel=new NexPanel(null); panel.setFont(panel.getFont().deriveFont(font));
				panel.render("Waiting for login",null,Preferences.EMPTY,catalog,new NexSession().stats(),true,null,"local");
				assertNoManualSetupControls(panel);
				panel.render("Ready",account,prefs,catalog,new NexSession().stats(),true,actions(),"local");
				assertTrue(shown(panel).contains("Waiting for Nex")); assertNoManualSetupControls(panel);
				panel.render("Observed",account,prefs,catalog,new NexSession().stats(),true,actions(),"local",null,true);
				assertTrue(shown(panel).contains("Tracking kill…")); assertNoManualSetupControls(panel);
				if(session.stats().last==null) { session.add(NexCaptureTest.replay(0,20,60)); }
				panel.render(paused,account,prefs,catalog,session.stats(),true,actions(),"local");
				assertTrue(shown(panel).contains("Open bank to verify")); assertFalse(shown(panel).contains("Last kill"));
				assertNoManualSetupControls(panel); com.ironpath.dev.PanelPreview.openSection(panel,"Diagnostics"); assertNoManualSetupControls(panel);
				assertTrue(shown(panel).contains("Open bank to verify")); com.ironpath.dev.PanelPreview.encounter(panel);
				panel.render("Observed",account,prefs,catalog,session.stats(),true,actions(),"local");
				assertTrue(shown(panel).contains("Last kill")); assertTrue(button(panel,"Compare setups").isSelected());
				assertFalse(shown(panel).contains("Nex session paused")); assertNoManualSetupControls(panel);
				button(panel,"Kill and calculation details").doClick(); assertNoManualSetupControls(panel);
				com.ironpath.dev.PanelPreview.openSection(panel,"Diagnostics"); button(panel,"Calculation details").doClick();
				assertTrue(shown(panel).contains("Detected one active charge"));
				com.ironpath.dev.PanelPreview.openSection(panel,"Diagnostics"); assertNoManualSetupControls(panel);
			}
		} catch(Exception ex) { throw new RuntimeException(ex); } });
	}
	@Test public void narrowAndEnlargedFontMainAndDetailsWrap() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{try {
			KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson()); NexSession session=new NexSession(); session.select("a",301); session.add(NexCaptureTest.replay(0,20,60));
			for(float font:List.of(12f,20f))
			{
				NexPanel panel=new NexPanel(null); panel.setFont(panel.getFont().deriveFont(font));
				panel.render("Observed",P4aFixtures.account(Set.of(11907),99,90),Preferences.EMPTY,catalog,session.stats(),true,actions(),"local");
				capture(panel,"main-"+(int)font); assertNoManualSetupControls(panel);
				button(panel,"Kill and calculation details").doClick(); capture(panel,"details-"+(int)font);
				assertFalse(shown(panel).contains("Detected one active charge"));
				com.ironpath.dev.PanelPreview.openSection(panel,"Diagnostics"); button(panel,"Calculation details").doClick();
				assertTrue(shown(panel).contains("Detected one active charge"));
			}
		} catch(Exception ex) { throw new RuntimeException(ex); } });
	}
	@Test public void preSmokeHistoryRemainsInspectableWhileTheDefaultSelectionStartsAtSmoke() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{try {
			var catalog=KnowledgeCatalog.load(new Gson()); var session=new NexSession(); session.select("a",301);
			var base=NexCaptureTest.replay(0,20,60);
			var before=new com.ironpath.nex.NexSummary.Sample(-10,"UNKNOWN","UNKNOWN",null,"unclassified",-1,base.timeline.get(0).state);
			var summary=new com.ironpath.nex.NexSummary(base.key,base.startTick,base.endTick,base.sampledTicks,base.expectedTicks,
				base.unclassifiedHits,base.outcome,base.reason,base.cohort,base.method,base.timeline,base.damage,base.gaps,base.signals,List.of(before));
			session.add(summary);
			for(float font:List.of(12f,20f))
			{
				var panel=new NexPanel(null); panel.setFont(panel.getFont().deriveFont(font));
				panel.render("Observed",P4aFixtures.account(Set.of(11907),99,90),Preferences.EMPTY,catalog,session.stats(),true,actions(),"local");
				button(panel,"Kill and calculation details").doClick();
				JComboBox<?> samples=(JComboBox<?>)children(panel).stream().filter(c->"nex-history-state".equals(c.getName())).findFirst().orElseThrow();
				assertEquals(1,samples.getSelectedIndex()); assertTrue(samples.getSelectedItem().toString().contains("SMOKE"));
				assertFalse(shown(panel).contains("excluded from fight statistics and equipment comparisons"));
				samples.setSelectedIndex(0);
				assertFalse(shown(panel).contains("UNKNOWN / UNKNOWN"));
				com.ironpath.dev.PanelPreview.openSection(panel,"Diagnostics"); button(panel,"Calculation details").doClick();
				assertTrue(shown(panel).contains("excluded from fight statistics and equipment comparisons"));
				((JComboBox<?>)children(NexScenarioPanelTest.named(panel,"nex-section-diagnostics-body",JPanel.class)).stream().filter(c->"nex-history-state".equals(c.getName())).findFirst().orElseThrow()).setSelectedIndex(0);
				assertTrue(shown(panel).contains("UNKNOWN / UNKNOWN"));
				capture(panel,"pre-smoke-"+(int)font);
			}
		} catch(Exception ex) { throw new RuntimeException(ex); } });
	}
	private static NexPanel.Actions actions()
	{
		return new NexPanel.Actions() {
			public boolean current() { return true; } public boolean canEdit() { return true; }
			public void method(NexMethod method) {} public void confirm(String key,Boolean value) {}
			public void reset() {} public void enable(boolean enabled) {}
		};
	}
	@Test public void savedFactsRetainExactVariantsAndSettingsFailuresRemainVisible() throws Exception
	{
		var catalog=KnowledgeCatalog.load(new Gson());
		String description=catalog.factChoices().get("owns-r3bFang");
		assertTrue(description.contains("Osmumten's fang (26219)")); assertTrue(description.contains("Osmumten's fang (or) (27246)"));
		var prefs=Preferences.EMPTY.confirm("owns-r3bFang",true,java.time.Instant.EPOCH);
		SwingUtilities.invokeAndWait(()->{
			var panel=new NexPanel(null); var session=new NexSession(); session.select("a",301);
			var account=P4aFixtures.account(Set.of(11907),99,90);
			panel.render("Observed",account,prefs,catalog,session.stats(),true,actions(),"Preferences saved locally.");
			assertNoManualSetupControls(panel);
			com.ironpath.dev.PanelPreview.openSection(panel,"Diagnostics");
			assertTrue(shown(panel).contains(description)); assertTrue(prefs.confirmations.get("owns-r3bFang").value);
			com.ironpath.dev.PanelPreview.openSection(panel,"Team & method");
			panel.render("Observed",account,prefs,catalog,session.stats(),true,actions(),"Local save failed.");
			assertTrue(shown(panel).contains("Settings not saved"));
			panel.render("Observed",account,prefs,catalog,session.stats(),true,actions(),"Preferences saved locally.");
			assertFalse(shown(panel).contains("Settings not saved"));
		});
	}
	@Test public void resourceReviewIsFrozenReadableAndClearedWithTheAccount() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{try {
			var catalog=KnowledgeCatalog.load(new Gson()); var summary=NexCaptureTest.replay(0,20,60);
			var result=new com.ironpath.advisor.NexOwnedResult(summary,List.of());
			var session=new NexSession(); session.select("a",301); session.add(summary);
			var account=P4aFixtures.account(Set.of(11907),99,90);
			for(float font:List.of(12f,20f))
			{
				session.clear(); session.select("a",301); session.add(summary);
				var panel=new NexPanel(null); panel.setFont(panel.getFont().deriveFont(font));
				panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",result);
				button(panel,"Base-stat comparisons").doClick();
				assertFalse(shown(panel).contains("Sampled energy changes"));
				button(panel,"Spec energy & Death Charge").doClick();
				assertTrue(shown(panel).contains("Spec energy & Death Charge · kill #1"));
				assertFalse(shown(panel).contains("not total restoration or spending"));
				assertTrue(shown(panel).contains("Upgraded Death Charge use: Observed"));
				capture(panel,"resources-"+(int)font);
				com.ironpath.dev.PanelPreview.openSection(panel,"Diagnostics"); button(panel,"Calculation details").doClick();
				assertTrue(shown(panel).contains("not total restoration or spending"));
				button(panel,"Resource readings and rules").doClick(); capture(panel,"resources-trace-"+(int)font);
				var readings=(JComboBox<?>)children(panel).stream().filter(c->"nex-resource-reading".equals(c.getName())).findFirst().orElseThrow();
				assertEquals(result.resources.points.size(),readings.getItemCount()); readings.setSelectedIndex(1);
				assertTrue(shown(panel).contains("energy 100.0%"));
				assertTrue(shown(panel).contains("Voidwaker 50.0%; Zaryte crossbow 75.0%"));
				assertFalse(shown(panel).contains("seconds saved"));
				var base=NexCaptureTest.replay(100,40,0);
				var next=new com.ironpath.nex.NexSummary(2,base.startTick,base.endTick,base.sampledTicks,base.expectedTicks,
					base.unclassifiedHits,base.outcome,base.reason,base.cohort,base.method,base.timeline,base.damage,base.gaps,base.signals,base.beforeStart);
				session.add(next);
				panel.render("New result",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",new com.ironpath.advisor.NexOwnedResult(next,List.of()));
				assertTrue(shown(panel).contains("Report record #1"));
				assertTrue(shown(panel).contains("Spec energy & Death Charge · kill #1"));
				panel.render("Profile changed",null,Preferences.EMPTY,catalog,session.stats(),true,null,"local",null);
				assertFalse(shown(panel).contains("energy 100.0%")); assertFalse(shown(panel).contains("Report record #"));
			}
		} catch(Exception ex) { throw new RuntimeException(ex); } });
	}
	@Test public void ownedPackagesWrapKeepFrozenHistoryAndExposeBloodFuryLossWithoutHealingClaims() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{try {
			KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson());
			var r=com.ironpath.advisor.NexOwnedFixtures.sample("ranged",com.ironpath.advisor.NexOwnedFixtures.state(com.ironpath.advisor.NexOwnedFixtures.ranged(),Map.of(11212,200L)));
			var m=com.ironpath.advisor.NexOwnedFixtures.sample("melee",com.ironpath.advisor.NexOwnedFixtures.state(com.ironpath.advisor.NexOwnedFixtures.melee(),Map.of(24780,1L)));
			var summary=com.ironpath.advisor.NexOwnedFixtures.summary(List.of(r,m));
			var account=com.ironpath.advisor.NexOwnedFixtures.account(null);
			var result=new com.ironpath.advisor.NexOwnedEvaluator(catalog).evaluate(summary,account,Preferences.EMPTY);
			NexSession session=new NexSession(); session.select("a",301); session.add(summary);
			for(float font:List.of(12f,20f))
			{
				session.clear(); session.select("a",301); session.add(summary);
				NexPanel panel=new NexPanel(null); panel.setFont(panel.getFont().deriveFont(font));
				panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",result);
				assertTrue(shown(panel).contains(" DPS")); capture(panel,"owned-main-"+(int)font);
				button(panel,"Base-stat comparisons").doClick(); capture(panel,"owned-details-"+(int)font);
				assertTrue(shown(panel).contains("Selected quiver ammo: Amethyst arrow"));
				assertTrue(shown(panel).contains("ammo slot: Ancient blessing"));
				button(panel,"Full gear setups").doClick(); capture(panel,"owned-basis-"+(int)font);
				JComboBox<?> state=(JComboBox<?>)children(panel).stream().filter(c->"nex-owned-state".equals(c.getName())).findFirst().orElseThrow(); state.setSelectedIndex(1);
				JComboBox<?> pair=(JComboBox<?>)children(panel).stream().filter(c->"nex-owned-pair".equals(c.getName())).findFirst().orElseThrow(); pair.setSelectedIndex(1);
				assertTrue(shown(panel).contains("Amulet of blood fury")); capture(panel,"owned-loss-"+(int)font);
				assertFalse(shown(panel).contains("Same theoretical DPS · zero change"));
				var next=new com.ironpath.nex.NexSummary(2,100,200,100,100,0,com.ironpath.encounter.EncounterSummary.Outcome.COMPLETE,
					"synthetic complete",summary.cohort,summary.method,List.of(r,m),Map.of(),Set.of());
				session.add(next); var fresh=new com.ironpath.advisor.NexOwnedEvaluator(catalog).evaluate(next,account,Preferences.EMPTY);
				panel.render("New kill",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",fresh);
				assertTrue(shown(panel).contains("Owned gear · kill #1"));
				assertTrue(shown(panel).contains("Amulet of blood fury"));
				button(panel,"Back to encounter").doClick(); button(panel,"Base-stat comparisons").doClick();
				assertTrue(shown(panel).contains("Owned gear · kill #2"));
				panel.render("Profile changed",null,Preferences.EMPTY,catalog,session.stats(),true,null,"",null);
				assertFalse(shown(panel).contains("Ancient blessing")); assertFalse(shown(panel).contains("Blood fury"));
			}
		} catch(Exception ex) { throw new RuntimeException(ex); } });
	}
	@Test public void lockedMeleeFiltersAndResourceReviewKeepTheSameKillAcrossTravelAndNewResults() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{try {
			var catalog=KnowledgeCatalog.load(new Gson());
			var slots=new LinkedHashMap<>(com.ironpath.advisor.NexOwnedFixtures.melee());
			slots.put("cape",24224); slots.put("shield",24143);
			var state=com.ironpath.advisor.NexOwnedFixtures.state(slots,Map.of());
			var first=com.ironpath.advisor.NexOwnedFixtures.sample("melee",state);
			var spent=new com.ironpath.encounter.CombatState(state.slots,state.realLevels,state.visibleLevels,state.prayers,
				state.style,500,true,state.inventory,state.resources);
			var second=new com.ironpath.nex.NexSummary.Sample(21,first.phase,first.window,first.target,"melee",-1,spent);
			var missing=new com.ironpath.nex.NexSummary.Sample(22,first.phase,first.window,null,"melee",-1,spent);
			var summary=com.ironpath.advisor.NexOwnedFixtures.summary(List.of(first,second,missing));
			var account=com.ironpath.advisor.NexOwnedFixtures.account(null);
			var session=new NexSession(); session.select("a",301); session.add(summary);
				var inputs=new com.ironpath.advisor.NexEvaluation(account,Preferences.EMPTY,catalog);
				var record=session.freezeInputs(summary.key,inputs); var result=inputs.evaluate(record,()->true);
				assertTrue(session.publish(record,result));
				assertEquals(2,result.choices.stream().filter(c->c.supported()).count());
			var panel=new NexPanel(null);
			panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",result);
			button(panel,"Base-stat comparisons").doClick();
			button(panel,"Full gear setups").doClick();
			assertTrue(shown(panel).contains("Infernal cape (l)")); assertTrue(shown(panel).contains("Dragon defender (l)"));
			button(panel,"Spec energy & Death Charge").doClick();
			var support=(JComboBox<?>)children(panel).stream().filter(c->"nex-owned-support-filter".equals(c.getName())).findFirst().orElseThrow();
			support.setSelectedItem("Unavailable comparisons");
			assertTrue(shown(panel).contains("No comparison available"));
			assertTrue(shown(panel).contains("First recorded energy 100.0% · last recorded 50.0%"));
			com.ironpath.dev.PanelPreview.openSection(panel,"Diagnostics"); button(panel,"Calculation details").doClick(); button(panel,"Resource readings and rules").doClick();
			var readings=(JComboBox<?>)children(panel).stream().filter(c->"nex-resource-reading".equals(c.getName())).findFirst().orElseThrow();
			readings.setSelectedIndex(1); assertTrue(shown(panel).contains("Tick 21 · energy 50.0%"));
			session.suspend("a",301,40,true);
			panel.render("Nex session paused. Open a bank to revalidate this account.",account,Preferences.EMPTY,catalog,
				session.stats(),true,actions(),"local",null);
			assertFalse(shown(panel).contains("No complete kill yet")); assertFalse(shown(panel).contains("First recorded energy"));
			readings.setSelectedIndex(0); assertFalse(shown(panel).contains("energy 100.0%"));
			var base=NexCaptureTest.replay(100,40,0);
			var next=new com.ironpath.nex.NexSummary(2,base.startTick,base.endTick,base.sampledTicks,base.expectedTicks,
				base.unclassifiedHits,base.outcome,base.reason,base.cohort,base.method,base.timeline,base.damage,base.gaps,base.signals,base.beforeStart);
			session.revalidate("a",301,50,true,false); session.add(next);
			panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",
				new com.ironpath.advisor.NexOwnedResult(next,List.of()));
			assertTrue(shown(panel).contains("Report record #1"));
				button(panel,"Calculation details").doClick();
				assertTrue(shown(panel),shown(panel).contains("Target unknown or minion"));
			assertTrue(shown(panel).contains("First recorded energy 100.0% · last recorded 50.0%"));
		} catch(Exception ex) { throw new RuntimeException(ex); } });
	}
	@Test public void ownedFiltersPreserveBankScopeExposeGapsAndClearOnIdentityChange() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{try {
			var catalog=KnowledgeCatalog.load(new Gson());
			var r=com.ironpath.advisor.NexOwnedFixtures.sample("ranged",com.ironpath.advisor.NexOwnedFixtures.state(com.ironpath.advisor.NexOwnedFixtures.ranged(),Map.of()));
			var m=com.ironpath.advisor.NexOwnedFixtures.sample("melee",com.ironpath.advisor.NexOwnedFixtures.state(com.ironpath.advisor.NexOwnedFixtures.melee(),Map.of()));
			var missing=new com.ironpath.nex.NexSummary.Sample(30,m.phase,m.window,null,"melee",-1,m.state);
			var summary=com.ironpath.advisor.NexOwnedFixtures.summary(List.of(r,m,missing));
			var account=com.ironpath.advisor.NexOwnedFixtures.account(Set.of(11212));
			var result=new com.ironpath.advisor.NexOwnedEvaluator(catalog).evaluate(summary,account,Preferences.EMPTY);
			assertEquals(4,result.choices.size());
			var session=new NexSession(); session.select("a",301); session.add(summary);
			for(float font:List.of(12f,20f))
			{
				var panel=new NexPanel(null); panel.setFont(panel.getFont().deriveFont(font));
				panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",result);
				com.ironpath.dev.PanelPreview.openSection(panel,"Diagnostics"); button(panel,"Calculation details").doClick();
				assertTrue(shown(panel).contains("Supported scenarios: carried 2, bank preparation 1. Unavailable: 1"));
				com.ironpath.dev.PanelPreview.encounter(panel);
				button(panel,"Base-stat comparisons").doClick();
				JComboBox<?> role=(JComboBox<?>)children(panel).stream().filter(c->"nex-owned-role-filter".equals(c.getName())).findFirst().orElseThrow();
				JComboBox<?> support=(JComboBox<?>)children(panel).stream().filter(c->"nex-owned-support-filter".equals(c.getName())).findFirst().orElseThrow();
				JComboBox<?> states=(JComboBox<?>)children(panel).stream().filter(c->"nex-owned-state".equals(c.getName())).findFirst().orElseThrow();
				assertEquals(3,states.getItemCount());
				role.setSelectedItem("Melee"); assertEquals(1,states.getItemCount());
				support.setSelectedItem("All states"); assertEquals(2,states.getItemCount());
				support.setSelectedItem("Unavailable comparisons"); assertEquals(1,states.getItemCount());
				assertTrue(shown(panel).contains("No comparison available"));
				assertFalse(shown(panel).contains("Target unknown or minion"));
				capture(panel,"owned-filter-gaps-"+(int)font);
				role.setSelectedItem("Ranged"); assertEquals(0,states.getItemCount()); assertFalse(states.isEnabled());
				assertTrue(shown(panel).contains("No matching comparisons"));
				support.setSelectedItem("All states"); assertEquals(2,states.getItemCount()); assertTrue(states.isEnabled());
				states.setSelectedIndex(1); assertTrue(shown(panel).contains("Bank gear"));
				capture(panel,"owned-filter-bank-"+(int)font);
				panel.render("Identity changed",null,Preferences.EMPTY,catalog,session.stats(),true,null,"",null);
				role.setSelectedItem("Melee"); support.setSelectedItem("All states");
				assertFalse(shown(panel).contains("Owned equipment")); assertFalse(shown(panel).contains("Target unknown or minion"));
			}
		} catch(Exception ex) { throw new RuntimeException(ex); } });
	}
}
