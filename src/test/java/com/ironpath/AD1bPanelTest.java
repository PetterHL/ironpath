package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.*;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.ironpath.advisor.EP1Fixtures.*;
import static com.ironpath.advisor.R15Fixtures.*;
import static com.ironpath.advisor.P4aFixtures.TIME;

/** Real Swing controls over authored account evidence; fallback sprites, no client acceptance. */
public class AD1bPanelTest
{
	private KnowledgeCatalog catalog; private Advisor advisor; private IronpathPanel panel;
	private AccountSnapshot account; private Preferences preferences; private PaceScenario manual;
	private String appliedActivity,appliedMethod; private boolean current=true;
	private List<Component> children(Container p)
	{ List<Component> all=new ArrayList<>();for(Component c:p.getComponents()){all.add(c);if(c instanceof Container)all.addAll(children((Container)c));}return all; }
	private JButton button(String name)
	{ return children(panel).stream().filter(c->c instanceof JButton && (name.equals(((JButton)c).getText())||name.equals(((JButton)c).getToolTipText()))).map(c->(JButton)c).findFirst().orElseThrow(); }
	private JButton named(String name)
	{ return children(panel).stream().filter(c->c instanceof JButton && name.equals(c.getName())).map(c->(JButton)c).findFirst().orElseThrow(); }
	private JTextField input()
	{ return children(panel).stream().filter(c->c instanceof JTextField && "pace-input".equals(c.getName())).map(c->(JTextField)c).findFirst().orElseThrow(); }
	private String text()
	{ return children(panel).stream().filter(c->c instanceof JTextArea).map(c->((JTextArea)c).getText()).collect(Collectors.joining("\n")); }
	private void render()
	{
		List<Advisor.UpgradeView> all=advisor.upgrades(account,preferences,new PoweredMagic(catalog).propose(account),manual);
		PursuitOverview overview=new PursuitOverview(all,List.of(),List.of(),List.of());
		panel.render("Synthetic AD1b component evidence",account,List.of(),catalog,null,preferences,new IronpathPanel.Actions(){
			public void goal(String id){}public void clear(){}public void confirm(String key,Boolean value){}public void skip(String id,boolean value){}
			public boolean current(){return current;}
			public void pace(String activity,String mode,String method,String value)
			{
				appliedActivity=activity;appliedMethod=method;
				manual=value.isBlank()?null:new PaceScenario(activity,mode,Double.parseDouble(value),Double.parseDouble(value),"Full eligible repeat hour",TIME,method);
				render();
			}
		},"Session-only authored test inputs",List.of(),overview,new PoweredMagic(catalog).propose(account));
	}
	private void layout(Container p){p.doLayout();for(Component c:p.getComponents())if(c instanceof Container)layout((Container)c);}
	private void capture(String name) throws Exception
	{
		panel.setSize(225,20000);for(int i=0;i<4;i++)layout(panel);panel.setSize(225,panel.getPreferredSize().height);for(int i=0;i<4;i++)layout(panel);
		for(Component component:children(panel)) {
			boolean visible=true;
			for(Component ancestor=component;ancestor!=null;ancestor=ancestor.getParent()) { visible &= ancestor.isVisible(); }
			if(visible && (component instanceof JTextArea || component instanceof JButton || component instanceof JLabel || component instanceof JTextField)) {
				Point at=SwingUtilities.convertPoint(component,0,0,panel);
				assertTrue(component.getName()+" extends past native width: "+at+" "+component.getSize(),at.x>=0 && at.x+component.getWidth()<=225);
				if(component instanceof JTextArea) {
					assertTrue(component.getHeight()>=component.getPreferredSize().height);
					if("acquisition-evidence".equals(component.getParent().getName())) { assertEquals("Estimate basis must use the native column",0,component.getX()); }
				}
			}
		}
		BufferedImage image=new BufferedImage(225,900,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();g.setColor(panel.getBackground());g.fillRect(0,0,225,900);panel.printAll(g);g.dispose();
		Path dir=Path.of("build/validation/ad1b/images");Files.createDirectories(dir);ImageIO.write(image,"png",dir.resolve(name+".png").toFile());
	}
	@Test public void automaticNumericPathAndManualClearCarrySelectedActivityAndRejectStaleControls() throws Exception
	{
		catalog=KnowledgeCatalog.load(new Gson());advisor=new Advisor(catalog);preferences=absent(catalog);account=ready();
		SwingUtilities.invokeAndWait(()->{
			try{
				panel=new IronpathPanel();render();button("Browse").doClick();named("open-upgrade-scorching-bow").doClick();button("Acquisition").doClick();
				assertTrue(text().contains("Model-based estimate"));assertTrue(text().contains("Mean 9.1 h"));button("Estimate basis and route evidence").doClick();assertEquals("",input().getText());capture("synapse-automatic");
				input().setText("40");button("Apply pace").doClick();assertEquals("tormented-demons",appliedActivity);assertTrue(appliedMethod.startsWith("demon-switch-repeat/"));
				assertTrue(text().contains("Manual pace scenario"));assertTrue(text().contains("Mean 12.5 h"));
				button("Clear pace").doClick();assertNull(manual);assertTrue(text().contains("Mean 9.1 h"));
				JButton stale=button("Apply pace");input().setText("1");current=false;stale.doClick();assertNull(manual);current=true;
				button("← Upgrades").doClick();named("open-upgrade-toxic-blowpipe").doClick();button("Acquisition").doClick();
				assertTrue(text().contains("Mean 25.6 h"));capture("fang-automatic");
				button("Estimate basis and route evidence").doClick();input().setText("10");button("Apply pace").doClick();assertEquals("zulrah",manual.activity);assertTrue(text().contains("Mean 51.2 h"));
				panel.setFont(panel.getFont().deriveFont(20f));render();button("Estimate basis and route evidence").doClick();capture("large-font");
			}catch(Exception e){throw new RuntimeException(e);}
		});
	}
}
