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

/** Headless native Swing evidence only; actual RuneLite sprites/DPI/user usefulness remain pending. */
public class R2PanelTest
{
	private KnowledgeCatalog catalog; private Advisor advisor; private IronpathPanel panel;
	private AccountSnapshot account; private Preferences preferences; private PoweredMagic.Setup magic; private CombatSetup ranged;
	private void render()
	{
		List<Advisor.UpgradeView> views=advisor.upgrades(account,preferences,magic,ranged,null);
		panel.render("Synthetic R2 evidence",account,advisor.evaluate(account,preferences),catalog,null,preferences,new IronpathPanel.Actions()
		{
			public void goal(String id) { }
			public void skip(String id,boolean v) { }
			public void confirm(String key,Boolean v) { }
			public void clear() { }
			public void reference(String key,String value)
			{ if(key.startsWith("ranged/")){ranged=ranged.edit(key.substring(7),value);}else{magic=magic.edit(key,value);}render(); }
		},"Synthetic storage",List.of(),new PursuitOverview(views,advisor.activityOpportunities(account,preferences,views),advisor.quickActions(account,preferences),advisor.availableActivities(account,preferences)),magic);
	}
	private List<Component> children(Container root)
	{ List<Component> out=new ArrayList<>();for(Component c:root.getComponents()){out.add(c);if(c instanceof Container){out.addAll(children((Container)c));}}return out; }
	private JButton button(String title)
	{ return children(panel).stream().filter(c->c instanceof JButton).map(c->(JButton)c).filter(b->title.equals(b.getText())||title.equals(b.getToolTipText())).findFirst().orElseThrow(); }
	private void open(String id)
	{ if(children(panel).stream().noneMatch(c->("open-upgrade-"+id).equals(c.getName()))){button("Browse").doClick();}
		children(panel).stream().filter(c->("open-upgrade-"+id).equals(c.getName())).map(c->(JButton)c).findFirst().orElseThrow().doClick(); }
	private String text()
	{ return children(panel).stream().filter(c->c instanceof JTextArea).map(c->((JTextArea)c).getText()).collect(java.util.stream.Collectors.joining("\n")); }
	private void capture(String name) throws Exception
	{
		panel.setSize(225,1);layout(panel);int h=panel.getPreferredSize().height;panel.setSize(225,h);layout(panel);
		BufferedImage image=new BufferedImage(225,h,BufferedImage.TYPE_INT_RGB);java.awt.Graphics2D g=image.createGraphics();panel.printAll(g);g.dispose();
		java.nio.file.Path dir=java.nio.file.Path.of("build/reports/r2/images");java.nio.file.Files.createDirectories(dir);javax.imageio.ImageIO.write(image,"png",dir.resolve(name+".png").toFile());
		assertEquals(225,panel.getWidth());
	}
	private void layout(Container c) { c.doLayout();for(Component child:c.getComponents()){if(child instanceof Container){layout((Container)child);}} }
	@Test public void rangedValuePathControlsAndHonestGapsRenderAtNativeWidth() throws Exception
	{
		catalog=KnowledgeCatalog.load(new Gson());advisor=new Advisor(catalog);account=R2AdviceTest.postBowfa();preferences=R15Fixtures.absent(catalog);
		magic=new PoweredMagic(catalog).propose(account);ranged=new RangedComparisonService(catalog).propose(account);
		SwingUtilities.invokeAndWait(()->{
			try
			{
				panel=new IronpathPanel();render();capture("overview");button("Browse").doClick();capture("browse");
				assertFalse(text().contains("Enemy with 0 Defence"));
				assertTrue(children(panel).stream().filter(c->"card-context".equals(c.getName())).map(c->((JTextArea)c).getText()).anyMatch(s->s.contains("Zilyana")));
				open("twisted-bow");assertTrue(text().indexOf("Theoretical DPS  ")<text().indexOf("Max hit  "));
				assertFalse(text().contains("Unknown reference"));
				button("Evidence and calculation basis").doClick();assertTrue("Target",text().contains("Commander Zilyana"));
				assertTrue("Supply provenance",text().contains("Candidate-only supplied ammunition")||text().contains("Observed ammunition"));capture("tbow-value");
				@SuppressWarnings("unchecked") JComboBox<Object> target=(JComboBox<Object>)children(panel).stream().filter(c->"reference-target".equals(c.getName())).findFirst().orElseThrow();
				for(int i=0;i<target.getItemCount();i++){if(target.getItemAt(i).toString().contains("Zilyana")){target.setSelectedIndex(i);}}
				button("Apply target").doClick();assertEquals("zilyana",ranged.target);assertEquals("neutral-zero-defence",magic.target);
				button("Acquisition").doClick();assertTrue(text().contains("Hours not estimated")||text().contains("Remaining effort"));capture("tbow-path");
				button("← Upgrades").doClick();open("necklace-of-anguish");button("Acquisition").doClick();assertTrue(text().contains("92 Crafting"));assertFalse(text().contains("Bracelet mould"));capture("anguish-path");
				button("← Upgrades").doClick();account=R15Fixtures.items(account,12926);ranged=ranged.edit("weapon","12926").edit("loaded-ammo","-1");render();open("twisted-bow");
				assertTrue(text().contains("DPS not calculated")||text().contains("DPS and hit chance gap"));assertTrue(text().contains("unobserved")||text().contains("not observed"));capture("ammo-gap");
			}
			catch(Exception e){throw new RuntimeException(e);}
		});
	}
	@Test public void staleRangedEditsCannotCrossSnapshotCharacterOrProfileGeneration() throws Exception
	{
		KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson());AccountSnapshot a=R2AdviceTest.postBowfa();ObservationSession session=new ObservationSession();
		session.select("character/profile-A");long generation=session.generation();session.publish(generation,session.identity(),a);session.loadPreferences(generation,session.identity(),Preferences.EMPTY,true);
		session.rangedReference(catalog);long revision=session.revision();
		assertTrue(session.editReference(generation,revision,session.identity(),a,"ranged/confirm","true",catalog));assertTrue(session.rangedReference(catalog).confirmed);
		assertFalse(session.editReference(generation,revision,session.identity(),a,"ranged/target","zilyana",catalog));
		assertFalse(session.reference(catalog).confirmed);
		session.select("character/profile-B");assertNull(session.rangedReference(catalog));
		assertFalse(session.editReference(generation,session.revision(),"character/profile-A",a,"ranged/confirm","true",catalog));
		session.publish(session.generation(),session.identity(),a);session.loadPreferences(session.generation(),session.identity(),Preferences.EMPTY,true);
		assertFalse(session.rangedReference(catalog).confirmed);assertEquals("neutral-zero-defence",session.rangedReference(catalog).target);
		session.clear();assertNull(session.rangedReference(catalog));assertTrue(session.preferences().confirmations.isEmpty());
	}
}
