package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.*;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.ironpath.advisor.EP1Fixtures.*;
import static com.ironpath.advisor.R15Fixtures.*;

/** Synthetic Swing component captures with fallback sprites; actual RuneLite remains a client check. */
public class EP1PanelTest
{
	private IronpathPanel panel;private KnowledgeCatalog catalog;private Advisor advisor;
	private AccountSnapshot account;private Preferences preferences;private PursuitOverview overview;
	private void render()
	{
		List<Advisor.UpgradeView> all=advisor.upgrades(account,preferences);
		overview=new PursuitOverview(all,advisor.activityOpportunities(account,preferences,all),List.of(),List.of());
		panel.render("Synthetic EP1 component evidence",account,List.of(),catalog,null,preferences,new IronpathPanel.Actions(){public void goal(String id){}public void clear(){}public void confirm(String key,Boolean value){}public void skip(String id,boolean value){}},"Session-only synthetic context",List.of(),overview,new PoweredMagic(catalog).propose(account));
	}
	private List<Component> children(Container parent){List<Component> out=new ArrayList<>();for(Component c:parent.getComponents()){out.add(c);if(c instanceof Container)out.addAll(children((Container)c));}return out;}
	private JButton named(String id){return children(panel).stream().filter(c->c instanceof JButton&&id.equals(c.getName())).map(c->(JButton)c).findFirst().orElseThrow();}
	private JButton button(String label){return children(panel).stream().filter(c->c instanceof JButton&&label.equals(((JButton)c).getText())).map(c->(JButton)c).findFirst().orElseThrow();}
	private String text(){return children(panel).stream().filter(c->c instanceof JTextArea).map(c->((JTextArea)c).getText()).collect(Collectors.joining("\n"));}
	private void layout(Container container){container.doLayout();for(Component c:container.getComponents())if(c instanceof Container)layout((Container)c);}
	private boolean visible(Component c){for(Component p=c;p!=null;p=p.getParent()){if(!p.isVisible())return false;if(p==panel)return true;}return false;}
	private void capture(String name) throws Exception
	{
		panel.setSize(225,20000);for(int i=0;i<5;i++)layout(panel);panel.setSize(225,panel.getPreferredSize().height);for(int i=0;i<5;i++)layout(panel);
		BufferedImage image=new BufferedImage(225,600,BufferedImage.TYPE_INT_RGB);Graphics2D graphics=image.createGraphics();graphics.setColor(panel.getBackground());graphics.fillRect(0,0,225,600);panel.printAll(graphics);graphics.dispose();
		Path dir=Path.of("build/validation/ep1/images");Files.createDirectories(dir);ImageIO.write(image,"png",dir.resolve(name+".png").toFile());
		for(Component component:children(panel)){if(component instanceof JTextArea&&"card-effort".equals(component.getName())&&visible(component)){assertTrue(component.getWidth()>100);assertTrue(component.getHeight()>=component.getFontMetrics(component.getFont()).getHeight());}}
	}
	@Test public void integratedQueueAndEffortRemainReadableAndBackPreservesNavigation() throws Exception
	{
		catalog=KnowledgeCatalog.load(new Gson());advisor=new Advisor(catalog);preferences=absent(catalog);account=ready();
		SwingUtilities.invokeAndWait(()->{
			try{
				panel=new IronpathPanel();render();capture("post-bowfa-home");
				List<String> displayed=children(panel).stream().map(Component::getName).filter(n->n!=null&&n.startsWith("upgrade-card-")).map(n->n.substring("upgrade-card-".length())).collect(Collectors.toList());
				int previous=-1;for(String id:displayed){int index=overview.all.stream().map(v->v.upgrade.id).collect(Collectors.toList()).indexOf(id);assertTrue(index>previous);previous=index;}
				assertTrue(text().contains("remaining effort"));button("Browse").doClick();named("open-upgrade-toxic-blowpipe").doClick();assertTrue(text().contains("remaining effort"));capture("blowpipe-value");button("Acquisition").doClick();assertTrue(text().contains("Remaining effort: Medium"));capture("blowpipe-path");
				button("← Upgrades").doClick();named("open-upgrade-scorching-bow").doClick();button("Acquisition").doClick();assertTrue(text().contains("synapse"));assertTrue(text().contains("Mean 500 trials"));capture("synapse-path");
				button("← Upgrades").doClick();account=items(ready(),20997,6746);render();named("open-upgrade-avernic-treads").doClick();button("Acquisition").doClick();assertTrue(text().contains("Combined hours unavailable"));capture("doom-detour");
				account=profiles(catalog).get("nearly-complete");panel=new IronpathPanel();render();assertEquals("tumekens-shadow",overview.preview.get(0).upgrade.id);capture("nearly-complete-home");
				panel.setFont(panel.getFont().deriveFont(20f));render();capture("large-font");
			}catch(Exception e){throw new RuntimeException(e);}
		});
	}
}
