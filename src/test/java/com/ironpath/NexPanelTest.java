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
	private static void layout(Container c) { c.doLayout(); for(Component child:c.getComponents()) { if(child instanceof Container) { layout((Container)child); } } }
	private static void capture(NexPanel panel,String name) throws Exception
	{
		panel.setSize(225,10000);
		// Settle nested width-dependent wrapping, including a just-expanded basis.
		for(int pass=0;pass<3;pass++) { panel.invalidate(); layout(panel); panel.setSize(225,panel.getPreferredSize().height); }
		layout(panel); int height=panel.getHeight();
		BufferedImage image=new BufferedImage(225,height,BufferedImage.TYPE_INT_RGB); Graphics2D graphics=image.createGraphics(); panel.printAll(graphics); graphics.dispose();
		Path directory=Path.of(name.startsWith("owned")?"build/validation/n2":"build/validation/n1"); Files.createDirectories(directory); ImageIO.write(image,"png",directory.resolve(name+".png").toFile());
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
			assertTrue(shown(panel).contains("Last complete kill")); assertFalse(shown(panel).contains("Recommended order"));
			button(panel,"Kill and calculation details").doClick(); assertTrue(shown(panel).contains("Tick" ) || shown(panel).contains("NEX@"));
			session.add(NexCaptureTest.replay(100,40,0)); panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions,"local");
			assertTrue(shown(panel).contains("20 local") || shown(panel).contains("27 local") || shown(panel).contains("20 observed"));
			panel.render("Profile changed",null,Preferences.EMPTY,catalog,session.stats(),true,null,"local");
			assertFalse(shown(panel).contains("local /")); assertFalse(shown(panel).contains("Kill #"));
		} catch(Exception ex) { throw new RuntimeException(ex); } });
	}
	@Test public void resumedCaptureReplacesThePausedBannerAndShowsConcreteEligibilitySignals() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{try {
			var catalog=KnowledgeCatalog.load(new Gson()); var session=new NexSession(); session.select("a",301);
			var account=P4aFixtures.account(Set.of(11907),99,90); var panel=new NexPanel(null);
			panel.render("Nex session paused. Open a bank to revalidate.",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",null);
			assertTrue(shown(panel).contains("Nex session paused"));
			panel.render("Nex session updated locally.",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",null,true);
			assertTrue(shown(panel).contains("Observing the current Nex attempt")); assertFalse(shown(panel).contains("Nex session paused"));
			assertFalse(shown(panel).contains("Ready to observe the next"));
			session.add(NexCaptureTest.replay(0,20,60));
			panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local");
			button(panel,"Kill and calculation details").doClick();
			assertTrue(shown(panel).contains("Measured start (smoke): tick 0"));
			assertTrue(shown(panel).contains("Nex death/dying: tick 30"));
			assertTrue(shown(panel).contains("Arena end boundary: tick 31"));
			assertTrue(shown(panel).contains("Smoke line observed: tick 0"));
			assertTrue(shown(panel).contains("Capture adapter nex-public-4"));
			assertTrue(shown(panel).contains("Barrier readings:"));
			button(panel,"Capture signal trace").doClick();
		} catch(Exception ex) { throw new RuntimeException(ex); } });
	}
	@Test public void setupDoesNotClaimAnEmptySessionWhileBankRevalidationHidesHistory() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{try {
			var catalog=KnowledgeCatalog.load(new Gson()); var session=new NexSession(); session.select("a",301);
			var account=P4aFixtures.account(Set.of(11907),99,90); var panel=new NexPanel(null);
			String paused="Nex session paused. Open a bank to revalidate this account.";
			panel.render(paused,account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local");
			button(panel,"Setup").doClick(); assertTrue(shown(panel).contains(paused));
			assertFalse(shown(panel).contains("No complete kill yet"));
			session.add(NexCaptureTest.replay(0,20,60));
			panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local");
			assertTrue(shown(panel).contains("Automatic observations are the baseline"));
			assertFalse(shown(panel).contains("Nex session paused")); assertFalse(shown(panel).contains("No complete kill yet"));
		} catch(Exception ex) { throw new RuntimeException(ex); } });
	}
	@Test public void narrowAndEnlargedFontMainSetupAndDetailsWrap() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{try {
			KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson()); NexSession session=new NexSession(); session.select("a",301); session.add(NexCaptureTest.replay(0,20,60));
			for(float font:List.of(12f,20f))
			{
				NexPanel panel=new NexPanel(null); panel.setFont(panel.getFont().deriveFont(font));
				panel.render("Observed",P4aFixtures.account(Set.of(11907),99,90),Preferences.EMPTY,catalog,session.stats(),true,actions(),"local");
				capture(panel,"main-"+(int)font); button(panel,"Setup").doClick(); capture(panel,"setup-"+(int)font);
				assertTrue(shown(panel).contains("assumed for planning")); button(panel,"Encounter").doClick();
				button(panel,"Kill and calculation details").doClick(); capture(panel,"details-"+(int)font);
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
				assertTrue(shown(panel).contains("excluded from fight statistics and equipment comparisons"));
				samples.setSelectedIndex(0); assertTrue(shown(panel).contains("UNKNOWN / UNKNOWN"));
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
				NexPanel panel=new NexPanel(null); panel.setFont(panel.getFont().deriveFont(font));
				panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",result);
				assertTrue(shown(panel).contains("theoretical DPS")); capture(panel,"owned-main-"+(int)font);
				button(panel,"Owned setup comparisons").doClick(); capture(panel,"owned-details-"+(int)font);
				assertTrue(shown(panel).contains("Selected quiver projectile: Amethyst arrow"));
				assertTrue(shown(panel).contains("ammo slot: Ancient blessing"));
				button(panel,"Full setups and calculation basis").doClick(); capture(panel,"owned-basis-"+(int)font);
				JComboBox<?> state=(JComboBox<?>)children(panel).stream().filter(c->"nex-owned-state".equals(c.getName())).findFirst().orElseThrow(); state.setSelectedIndex(1);
				JComboBox<?> pair=(JComboBox<?>)children(panel).stream().filter(c->"nex-owned-pair".equals(c.getName())).findFirst().orElseThrow(); pair.setSelectedIndex(1);
				assertTrue(shown(panel).contains("Blood fury healing and survival are not estimated")); capture(panel,"owned-loss-"+(int)font);
				assertFalse(shown(panel).contains("Same theoretical DPS · zero change"));
				var next=new com.ironpath.nex.NexSummary(2,100,200,100,100,0,com.ironpath.encounter.EncounterSummary.Outcome.COMPLETE,
					"synthetic complete",summary.cohort,summary.method,List.of(r,m),Map.of(),Set.of());
				session.add(next); var fresh=new com.ironpath.advisor.NexOwnedEvaluator(catalog).evaluate(next,account,Preferences.EMPTY);
				panel.render("New kill",account,Preferences.EMPTY,catalog,session.stats(),true,actions(),"local",fresh);
				assertTrue(shown(panel).contains("Owned equipment · kill #1"));
				assertTrue(shown(panel).contains("Blood fury healing and survival are not estimated"));
				button(panel,"Back to encounter").doClick(); button(panel,"Owned setup comparisons").doClick();
				assertTrue(shown(panel).contains("Owned equipment · kill #2"));
				panel.render("Profile changed",null,Preferences.EMPTY,catalog,session.stats(),true,null,"",null);
				assertFalse(shown(panel).contains("Ancient blessing")); assertFalse(shown(panel).contains("Blood fury"));
			}
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
				assertTrue(shown(panel).contains("Supported scenarios: carried 2, bank preparation 1. Unavailable: 1"));
				button(panel,"Owned setup comparisons").doClick();
				JComboBox<?> role=(JComboBox<?>)children(panel).stream().filter(c->"nex-owned-role-filter".equals(c.getName())).findFirst().orElseThrow();
				JComboBox<?> support=(JComboBox<?>)children(panel).stream().filter(c->"nex-owned-support-filter".equals(c.getName())).findFirst().orElseThrow();
				JComboBox<?> states=(JComboBox<?>)children(panel).stream().filter(c->"nex-owned-state".equals(c.getName())).findFirst().orElseThrow();
				assertEquals(3,states.getItemCount());
				role.setSelectedItem("melee"); assertEquals(1,states.getItemCount());
				support.setSelectedItem("All states"); assertEquals(2,states.getItemCount());
				support.setSelectedItem("Unavailable comparisons"); assertEquals(1,states.getItemCount());
				assertTrue(shown(panel).contains("Target unknown or minion"));
				button(panel,"Why comparisons are unavailable").doClick();
				assertTrue(shown(panel).contains("1 scenario · Target unknown or minion"));
				capture(panel,"owned-filter-gaps-"+(int)font);
				role.setSelectedItem("ranged"); assertEquals(0,states.getItemCount()); assertFalse(states.isEnabled());
				assertTrue(shown(panel).contains("No scenarios match these filters"));
				support.setSelectedItem("All states"); assertEquals(2,states.getItemCount()); assertTrue(states.isEnabled());
				states.setSelectedIndex(1); assertTrue(shown(panel).contains("Bank preparation scenario"));
				capture(panel,"owned-filter-bank-"+(int)font);
				panel.render("Identity changed",null,Preferences.EMPTY,catalog,session.stats(),true,null,"",null);
				role.setSelectedItem("melee"); support.setSelectedItem("All states");
				assertFalse(shown(panel).contains("Owned equipment")); assertFalse(shown(panel).contains("Target unknown or minion"));
			}
		} catch(Exception ex) { throw new RuntimeException(ex); } });
	}
}
