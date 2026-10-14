package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.*;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Synthetic native components, not authenticated RuneLite/sprite/DPI validation. */
public class R3bPanelTest
{
	private KnowledgeCatalog catalog; private Advisor advisor; private IronpathPanel panel;
	private AccountSnapshot account; private Preferences preferences; private CombatSetup magic,ranged,melee;
	private void render()
	{
		List<Advisor.UpgradeView> views=advisor.upgrades(account,preferences,magic,ranged,melee,null);
		panel.render("Synthetic R3b evidence",account,advisor.evaluate(account,preferences),catalog,null,preferences,new IronpathPanel.Actions()
		{
			public void goal(String id) { }
			public void skip(String id,boolean v) { }
			public void confirm(String key,Boolean v) { }
			public void clear() { }
			public void reference(String key,String value) { assertTrue(key.startsWith("melee/"));melee=melee.edit(key.substring(6),value);render(); }
		},"Synthetic storage",List.of(),new PursuitOverview(views,advisor.activityOpportunities(account,preferences,views),advisor.quickActions(account,preferences),advisor.availableActivities(account,preferences)),new PoweredMagic(catalog).propose(account));
	}
	private List<Component> children(Container root)
	{ List<Component> out=new ArrayList<>();for(Component c:root.getComponents()){out.add(c);if(c instanceof Container){out.addAll(children((Container)c));}}return out; }
	private JButton button(String title)
	{ return children(panel).stream().filter(c->c instanceof JButton).map(c->(JButton)c).filter(b->title.equals(b.getText())||title.equals(b.getToolTipText())).findFirst().orElseThrow(); }
	private void open()
	{
		if(children(panel).stream().noneMatch(c->"open-upgrade-abyssal-tentacle".equals(c.getName()))){button("Browse").doClick();}
		children(panel).stream().filter(c->"open-upgrade-abyssal-tentacle".equals(c.getName())).map(c->(JButton)c).findFirst().orElseThrow().doClick();
	}
	private String text()
	{ return children(panel).stream().filter(c->c instanceof JTextArea).map(c->((JTextArea)c).getText()).collect(java.util.stream.Collectors.joining("\n")); }
	private void capture(String name) throws Exception
	{
		panel.setSize(225,1);layout(panel);int h=panel.getPreferredSize().height;panel.setSize(225,h);layout(panel);
		BufferedImage image=new BufferedImage(225,h,BufferedImage.TYPE_INT_RGB);java.awt.Graphics2D g=image.createGraphics();panel.printAll(g);g.dispose();
		java.nio.file.Path dir=java.nio.file.Path.of("build/reports/r3b/images");java.nio.file.Files.createDirectories(dir);javax.imageio.ImageIO.write(image,"png",dir.resolve(name+".png").toFile());
		if(name.startsWith("soul-") || name.startsWith("treads-"))
		{
			BufferedImage viewport=new BufferedImage(225,Math.min(600,h),BufferedImage.TYPE_INT_RGB);
			java.awt.Graphics2D clip=viewport.createGraphics();panel.printAll(clip);clip.dispose();
			javax.imageio.ImageIO.write(viewport,"png",dir.resolve(name+"-viewport.png").toFile());
		}
	}
	private void layout(Container c) { c.doLayout();for(Component child:c.getComponents()){if(child instanceof Container){layout((Container)child);}} }
	@Test public void meleeValuePathAndFocusedControlsWorkAtNativeWidth() throws Exception
	{
		catalog=KnowledgeCatalog.load(new Gson());advisor=new Advisor(catalog);account=MeleeAdviceTest.ordinary();preferences=R15Fixtures.absent(catalog);
		magic=new CombatComparisonService(catalog).propose(account);ranged=new RangedComparisonService(catalog).propose(account);melee=new MeleeComparisonService(catalog).propose(account);
		SwingUtilities.invokeAndWait(()->{try {
			panel=new IronpathPanel();render();capture("overview");open();
			assertTrue(text().contains("Abyssal whip"));assertTrue(text().contains("Abyssal tentacle"));assertFalse(text().contains("Unknown reference"));
			assertTrue(text().indexOf("Theoretical DPS  ")<text().indexOf("Max hit  "));capture("value");
			button("Reference setup and assumptions").doClick();
			JComboBox<?> style=(JComboBox<?>)children(panel).stream().filter(c->"reference-style".equals(c.getName())).findFirst().orElseThrow();
			assertEquals(3,style.getItemCount());assertFalse(style.getItemAt(0).toString().contains("AGGRESSIVE"));
			JComboBox<?> target=(JComboBox<?>)children(panel).stream().filter(c->"reference-target".equals(c.getName())).findFirst().orElseThrow();
			for(int i=0;i<target.getItemCount();i++){if(target.getItemAt(i).toString().contains("Graardor")){target.setSelectedIndex(i);}}
			button("Apply target").doClick();assertEquals("graardor",melee.target);assertEquals("neutral-zero-defence",magic.target);assertEquals("neutral-zero-defence",ranged.target);
			button("Acquisition").doClick();assertTrue(text().contains("10,000"));assertTrue(text().contains("87 Slayer"));capture("path");button("Estimate basis and route evidence").doClick();capture("path-expanded");
			button("← Upgrades").doClick();account=R15Fixtures.items(account,28997);render();open();assertTrue(text().contains("competing melee"));assertTrue(text().contains("THEORETICAL_DPS:"));assertFalse(text().contains("DPS and hit chance gap:"));assertTrue(text().contains("Max hit"));capture("family-gap");
				button("← Upgrades").doClick();account=R15Fixtures.items(MeleeAdviceTest.ordinary(),22325);
				melee=new MeleeComparisonService(catalog).propose(account).edit("weapon","22325").edit("shield","0").edit("style","SLASH_ACCURATE");
				render();open();assertTrue(text().contains("off-hand restored"));capture("restored-off-hand");
		} catch(Exception e){throw new RuntimeException(e);} });
	}
	@Test public void staleMeleeEditsConfirmationAndLifecycleStateStayIsolated() throws Exception
	{
		KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson());AccountSnapshot a=MeleeAdviceTest.ordinary();ObservationSession s=new ObservationSession();
		s.select("character/profile-A");long generation=s.generation();s.publish(generation,s.identity(),a);s.loadPreferences(generation,s.identity(),Preferences.EMPTY,true);
		s.meleeReference(catalog);s.rangedReference(catalog);s.reference(catalog);long revision=s.revision();
		assertTrue(s.editReference(generation,revision,s.identity(),a,"melee/confirm","true",catalog));assertTrue(s.meleeReference(catalog).confirmed);
		assertFalse(s.editReference(generation,revision,s.identity(),a,"melee/target","graardor",catalog));assertFalse(s.reference(catalog).confirmed);assertFalse(s.rangedReference(catalog).confirmed);
		AccountSnapshot changed=com.ironpath.advisor.P4aFixtures.levels(a,"STRENGTH",95);s.publish(generation,s.identity(),changed);assertFalse(s.meleeReference(catalog).confirmed);
		assertTrue(s.editReference(generation,s.revision(),s.identity(),changed,"melee/boost","SUPER_ATTACK_STRENGTH",catalog));
		assertTrue(s.editReference(generation,s.revision(),s.identity(),changed,"melee/confirm","true",catalog));
		assertTrue(s.editPreferences(generation,s.identity(),Preferences.EMPTY.confirm("knight-waves",true,com.ironpath.advisor.P4aFixtures.TIME)));assertFalse(s.meleeReference(catalog).confirmed);
		s.select("character/profile-B");assertNull(s.meleeReference(catalog));assertFalse(s.editReference(generation,s.revision(),"character/profile-A",changed,"melee/confirm","true",catalog));
		s.publish(s.generation(),s.identity(),a);s.loadPreferences(s.generation(),s.identity(),Preferences.EMPTY,true);
		assertEquals(MeleeModel.Boost.NONE,s.meleeReference(catalog).meleeBoost);assertFalse(s.meleeReference(catalog).confirmed);assertEquals("neutral-zero-defence",s.meleeReference(catalog).target);
		s.clear();assertNull(s.meleeReference(catalog));assertTrue(s.preferences().confirmations.isEmpty());
	}
	@Test public void soulTaskAndDefenceScenarioControlsHaveIndependentNativeValueAndPath() throws Exception
	{
		catalog=KnowledgeCatalog.load(new Gson());advisor=new Advisor(catalog);
		account=R15Fixtures.items(MeleeAdviceTest.ordinary(),31088,13239);
		preferences=R15Fixtures.absent(catalog);
		magic=new CombatComparisonService(catalog).propose(account);ranged=new RangedComparisonService(catalog).propose(account);
		melee=new MeleeComparisonService(catalog).propose(account);
		SwingUtilities.invokeAndWait(()->{try {
			panel=new IronpathPanel();render();button("Browse").doClick();
			children(panel).stream().filter(c->"open-upgrade-soulreaper-axe".equals(c.getName())).map(c->(JButton)c).findFirst().orElseThrow().doClick();
			assertTrue(text().contains("no HP drain"));capture("soul-start");
			button("Reference setup and assumptions").doClick();
			JComboBox<?> souls=(JComboBox<?>)children(panel).stream().filter(c->"reference-souls".equals(c.getName())).findFirst().orElseThrow();
			assertEquals(7,souls.getItemCount());souls.setSelectedIndex(6);button("Apply soul state").doClick();
			assertEquals(MeleeModel.Souls.RAMP,melee.souls);assertTrue(text().contains("five-swing"));capture("soul-ramp");
			button("Reference setup and assumptions").doClick();
			JTextField defence=(JTextField)children(panel).stream().filter(c->"reference-defence".equals(c.getName())).findFirst().orElseThrow();
			defence.setText("0");button("Apply target Defence").doClick();assertEquals(0,melee.defenceOverride);
			assertEquals("neutral-zero-defence",ranged.target);assertEquals("neutral-zero-defence",magic.target);
			button("Path").doClick();assertTrue(text().contains("2,000"));assertTrue(text().contains("75 Magic"));capture("soul-path");
			button("Back to upgrades").doClick();
			children(panel).stream().filter(c->"open-upgrade-avernic-treads-primordial".equals(c.getName())).map(c->(JButton)c).findFirst().orElseThrow().doClick();
			button("Path").doClick();assertTrue(text().contains("4,000"));capture("treads-primordial-path");
		} catch(Exception e){throw new RuntimeException(e);} });
	}
}
