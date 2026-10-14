package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.*;
import com.ironpath.encounter.*;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.imageio.ImageIO;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.ironpath.advisor.RecentBossComparisonTest.*;

public class RecentBossPanelTest
{
	KnowledgeCatalog catalog; Advisor advisor; IronpathPanel panel;
	AccountSnapshot account; Preferences preferences; KillImpactResult result;
	@Before public void setup() throws Exception
	{
		catalog=KnowledgeCatalog.load(new Gson()); advisor=new Advisor(catalog); account=owned(); preferences=R15Fixtures.absent(catalog);
		result=new RecentBossComparison(catalog).evaluate(summary(state(9185,9144,103),Set.of()),account,preferences);
		SwingUtilities.invokeAndWait(()->{ panel=new IronpathPanel(); panel.recentBoss(result,true,List.of(result.summary)); render(); });
	}
	void render()
	{ panel.render("Synthetic recent boss",account,advisor.evaluate(account,preferences),catalog,null,preferences,null,"Synthetic storage",List.of(),advisor.upgrades(account,preferences),new PoweredMagic(catalog).propose(account)); }
	List<Component> children(Container parent)
	{ List<Component> result=new ArrayList<>(); for(Component child:parent.getComponents()) { result.add(child); if(child instanceof Container) { result.addAll(children((Container)child)); } } return result; }
	JButton button(String name)
	{ return children(panel).stream().filter(c->c instanceof JButton && (name.equals(c.getName()) || name.equals(((JButton)c).getText()))).map(c->(JButton)c).findFirst().orElseThrow(); }
	String text() { return children(panel).stream().filter(c->c instanceof JTextArea).map(c->((JTextArea)c).getText()).collect(Collectors.joining("\n")); }
    void invalidateTree(Container root) { root.invalidate(); for(Component child:root.getComponents()) { if(child instanceof Container) { invalidateTree((Container)child); } } }

	void layout(Container root) { root.doLayout(); for(Component child:root.getComponents()) { if(child instanceof Container) { layout((Container)child); } } }
    boolean visible(Component child)
    { for(Component c=child;c!=null;c=c.getParent()) { if(!c.isVisible()) { return false; } if(c==panel) { return true; } } return false; }

	void capture(String name) throws Exception
	{
        panel.setSize(225,20000);
        for(int pass=0;pass<6;pass++) { invalidateTree(panel); layout(panel); panel.setSize(225,Math.max(600,panel.getPreferredSize().height)); }
        invalidateTree(panel); layout(panel);
		for(Component child:children(panel))
		{ if(visible(child) && child instanceof JTextArea) {
			assertTrue(child.getBounds().toString(),child.getWidth()<=225);
			assertTrue(name+" "+((JTextArea)child).getText()+" height "+child.getHeight()+" < "+child.getPreferredSize().height,
				child.getHeight()>=child.getPreferredSize().height);
		} }
		BufferedImage image=new BufferedImage(225,600,BufferedImage.TYPE_INT_RGB); Graphics2D graphics=image.createGraphics();
		graphics.setColor(net.runelite.client.ui.ColorScheme.DARK_GRAY_COLOR); graphics.fillRect(0,0,225,600); panel.printAll(graphics); graphics.dispose();
		Path directory=Path.of("build/reports/ki1/images"); Files.createDirectories(directory); ImageIO.write(image,"png",directory.resolve(name+".png").toFile());
	}
	@Test public void recentContextFreezesWhileNewKillArrivesAndValuePathBackWork() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			try
			{
				button("open-recent-boss").doClick(); assertTrue(text().contains("Observed engagement 54.0 s")); capture("recent-context");
				button("recent-value-19547").doClick(); assertTrue(text().contains("Actually worn:")); assertTrue(text().contains("Rune crossbow")); capture("recent-value"); button("Inputs and sources").doClick(); capture("recent-inputs");
				KillImpactResult newer=new RecentBossComparison(catalog).evaluate(summary(state(20997,11212,90),Set.of()),account,preferences);
				panel.recentBoss(newer,true,List.of(newer.summary)); render(); assertTrue(text().contains("Rune crossbow"));
				button("Back to recent boss").doClick(); button("recent-path-19547").doClick(); assertTrue(text().contains("Path from the captured account context")); capture("recent-path");
				button("Back to recent boss").doClick(); button("← Upgrades").doClick(); assertFalse(text().contains("Actually worn:"));
			}
			catch(Exception e) { throw new RuntimeException(e); }
		});
	}
	@Test public void unknownAmmoNoGainAndEnlargedFontKeepHonestState() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			try
			{
				KillImpactResult unknown=new RecentBossComparison(catalog).evaluate(summary(state(12926,0,99),Set.of()),account,preferences);
				panel.recentBoss(unknown,true,List.of(unknown.summary)); render(); button("open-recent-boss").doClick();
				assertTrue(text().contains("Internal darts are unread")); capture("unknown-ammo");
				panel.setFont(panel.getFont().deriveFont(20f)); render(); capture("large-font");
				panel.recentBoss(null,false,List.of()); render(); assertFalse(text().contains("Internal darts are unread"));
			}
			catch(Exception e) { throw new RuntimeException(e); }
		});
	}
	@Test public void accountTransitionAndOldControlsCannotReopenHistoricalResult() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			JButton old=button("open-recent-boss");
			panel.render("Account transition",null,List.of(),catalog,null,Preferences.EMPTY,null,"",List.of()); old.doClick();
			assertFalse(text().contains("Observed engagement"));
		});
	}
}
