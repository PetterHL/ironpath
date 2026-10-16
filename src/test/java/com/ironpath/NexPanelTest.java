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
	{ return children(panel).stream().filter(c->c instanceof JTextArea).map(c->((JTextArea)c).getText()).reduce("",(a,b)->a+"\n"+b); }
	private static void layout(Container c) { c.doLayout(); for(Component child:c.getComponents()) { if(child instanceof Container) { layout((Container)child); } } }
	private static void capture(NexPanel panel,String name) throws Exception
	{
		panel.setSize(225,10000); layout(panel); int height=panel.getPreferredSize().height; panel.setSize(225,height); layout(panel);
		BufferedImage image=new BufferedImage(225,height,BufferedImage.TYPE_INT_RGB); Graphics2D graphics=image.createGraphics(); panel.printAll(graphics); graphics.dispose();
		Path directory=Path.of("build/validation/n1"); Files.createDirectories(directory); ImageIO.write(image,"png",directory.resolve(name+".png").toFile());
		for(Component c:children(panel))
		{
			if(c instanceof JTextArea && c.getWidth()>0)
			{ assertTrue("Wrapped row width",c.getWidth()<=225); assertTrue("Wrapped row height",c.getHeight()>=c.getPreferredSize().height); }
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
	private static NexPanel.Actions actions()
	{
		return new NexPanel.Actions() {
			public boolean current() { return true; } public boolean canEdit() { return true; }
			public void method(NexMethod method) {} public void confirm(String key,Boolean value) {}
			public void reset() {} public void enable(boolean enabled) {}
		};
	}
}
