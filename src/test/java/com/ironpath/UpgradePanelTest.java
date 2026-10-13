package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.Advisor;
import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.advisor.PoweredMagic;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.PluginPanel;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.P4aFixtures.*;
import static org.junit.Assert.*;

/** Real Swing controls at the native panel width; no authenticated client or usefulness claim. */
public class UpgradePanelTest
{
	private IronpathPanel panel;
	private KnowledgeCatalog catalog;
	private Advisor advisor;
	private AccountSnapshot account;
	private Preferences preferences=Preferences.EMPTY;
	private PoweredMagic.Setup setup;
	@Before public void setup() throws Exception
	{
		catalog=KnowledgeCatalog.load(new Gson());advisor=new Advisor(catalog);
		account=account(Set.of(11907,6585,9185,11840,11785),90,90);setup=new PoweredMagic(catalog).propose(account);
		SwingUtilities.invokeAndWait(()->{panel=new IronpathPanel();render();});
	}
	private void render()
	{
		panel.render("Synthetic no-goal preview",account,advisor.evaluate(account,preferences),catalog,advisor.goal(account,preferences),preferences,new IronpathPanel.Actions()
		{
			public void goal(String id){preferences=preferences.goal(id);render();}
			public void skip(String id,boolean value){preferences=preferences.skip(id,value);render();}
			public void confirm(String key,Boolean value){preferences=preferences.confirm(key,value,TIME);render();}
			public void clear(){preferences=Preferences.EMPTY;render();}
			public void reference(String key,String value){setup=setup.edit(key,value);render();}
		},"Synthetic storage",List.of(),advisor.upgrades(account,preferences,setup),setup);
	}
	private List<Component> children(Container container)
	{
		List<Component> result=new ArrayList<>();
		for(Component child:container.getComponents()){result.add(child);if(child instanceof Container)result.addAll(children((Container)child));}
		return result;
	}
	private JButton button(String label){return children(panel).stream().filter(c->c instanceof JButton).map(c->(JButton)c).filter(b->b.getText().equals(label)||label.equals(b.getToolTipText())).findFirst().orElseThrow();}
	private JButton open(String id){if(children(panel).stream().noneMatch(c->("open-upgrade-"+id).equals(c.getName()))){button("Browse").doClick();}return children(panel).stream().filter(c->c instanceof JButton).map(c->(JButton)c).filter(b->("open-upgrade-"+id).equals(b.getName())).findFirst().orElseThrow();}
	private boolean visible(Component c){while(c!=panel){if(!c.isVisible())return false;c=c.getParent();if(c==null)return false;}return true;}
	private String text(){return children(panel).stream().filter(c->c instanceof JTextArea).map(c->((JTextArea)c).getText()).collect(java.util.stream.Collectors.joining("\n"));}

	@Test public void noGoalPrimaryCardsAndAdministrationStaySeparateAtNarrowWidth() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			assertEquals(0,children(panel).stream().filter(c->c instanceof JButton&&"Open details".equals(((JButton)c).getText())).count());
			assertTrue(visible(button("Browse")));
			assertTrue(children(panel).stream().anyMatch(c->c.getName()!=null&&c.getName().startsWith("open-upgrade-")&&visible(c)));
			assertFalse(visible(button("Set goal")));assertTrue(visible(button("Account and preferences")));
			for(Component c:children(panel))
			{
				if(c instanceof JTextArea){assertTrue(((JTextArea)c).getLineWrap());assertTrue(c.getMaximumSize().width<=PluginPanel.PANEL_WIDTH);}
				if(c instanceof JButton||c instanceof JComboBox){assertTrue(c.getMaximumSize().width<=PluginPanel.PANEL_WIDTH);}
			}
		});
	}

	@Test public void actualDetailsBackAndTargetedConfirmClearUpdateTheOutcome() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			open("swamp-trident").doClick();assertTrue(visible(button("Back to upgrades")));assertTrue(text().contains("25 → 28"));
			button("Confirm not owned").doClick();assertFalse(preferences.confirmations.get("owns-swampOrBetter").value);
			button("Clear confirmation").doClick();assertFalse(preferences.confirmations.containsKey("owns-swampOrBetter"));
			button("Already owned").doClick();assertTrue(preferences.confirmations.get("owns-swampOrBetter").value);
			assertFalse(advisor.upgrades(account,preferences).stream().anyMatch(v->v.upgrade.id.equals("swamp-trident")));
			open("occult-necklace").doClick();button("Back to upgrades").doClick();assertTrue(visible(button("Account and preferences")));
		});
	}

	@Test public void referenceChoiceChangesNumbersAndConfirmationRemainsAReference() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			open("occult-necklace").doClick();button("Reference setup and assumptions").doClick();
			JComboBox<?> neck=(JComboBox<?>)children(panel).stream().filter(c->"reference-neck".equals(c.getName())).findFirst().orElseThrow();
			neck.setSelectedIndex(0);button("Apply neck").doClick();assertEquals(Integer.valueOf(0),setup.slots.get("neck"));
			assertTrue(text().contains("deliberately empty"));button("Confirm reference").doClick();assertTrue(setup.confirmed);
			assertTrue(text().contains("Confirmed reference estimate"));assertFalse(text().contains("expected kill time"));
		});
	}

	@Test public void producesHeadlessPrimaryAndDetailPreviews() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			try{capture("p4a-primary.png");open("swamp-trident").doClick();capture("p4a-detail.png");}
			catch(Exception failure){throw new RuntimeException(failure);}
		});
	}
	@Test public void r1ValuePathUnsupportedAndOverviewRenderAtNativeWidth() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			try
			{
				account=com.ironpath.advisor.R1AdviceTest.postBowfa(false);setup=new PoweredMagic(catalog).propose(account);render();capture("r1-overview.png");
				open("tumekens-shadow").doClick();assertTrue(text().contains("Off-hand removed"));capture("r1-value.png");
				button("Path").doClick();assertTrue(text().contains("Beneath Cursed Sands"));assertFalse(text().contains("Theoretical DPS  "));capture("r1-path.png");
				button("Value").doClick();setup=setup.edit("target","toa");render();assertTrue(text().contains("no default invocation"));capture("r1-unsupported.png");
				button("Back to upgrades").doClick();assertTrue(visible(button("Account and preferences")));
				setup=setup.edit("target","graardor");render();open("magus-ring").doClick();button("Path").doClick();assertTrue(text().contains("ALL/ANY"));capture("r1-assembly-path.png");
				AccountSnapshot base=account;java.util.Set<Integer> items=new java.util.HashSet<>(base.bank.value());items.addAll(Set.of(11785,26372));
				account=new AccountSnapshot(base.levels,base.inventory,base.equipment,fact(items),base.rigour,base.augury,base.facts,
					com.ironpath.model.Observation.unknown(),com.ironpath.model.Observation.unknown(),com.ironpath.model.Observation.unknown(),fact(java.util.Map.of(26231,120L)));
				setup=new PoweredMagic(catalog).propose(account);render();button("Back to upgrades").doClick();open("zaryte-crossbow").doClick();
				assertTrue(text().contains("specialist role"));button("Path").doClick();button("Evidence").doClick();capture("r1-zaryte-path.png");
			}
			catch(Exception failure){throw new RuntimeException(failure);}
		});
	}
	@Test public void backRestoresOverviewScrollAndStaleTabCannotReopenOtherAccount() throws Exception
	{
		javax.swing.JScrollPane[] holder=new javax.swing.JScrollPane[1];JButton[] oldPath=new JButton[1];
		SwingUtilities.invokeAndWait(()->{
			holder[0]=new javax.swing.JScrollPane(panel);holder[0].setSize(PluginPanel.PANEL_WIDTH,600);
			panel.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH,4000));holder[0].doLayout();holder[0].getViewport().setViewPosition(new java.awt.Point(0,350));
			open("swamp-trident").doClick();oldPath[0]=button("Path");button("Back to upgrades").doClick();
		});
		SwingUtilities.invokeAndWait(()->{
			assertEquals(350,holder[0].getViewport().getViewPosition().y);
			panel.render("Account changed",null,List.of(),catalog,null,Preferences.EMPTY,null,"",List.of());oldPath[0].doClick();
			assertFalse(text().contains("Trident of the swamp"));
		});
	}

	private void capture(String filename) throws Exception
	{
		for(Component c:children(panel))if(c instanceof JTextArea){c.setSize(PluginPanel.PANEL_WIDTH-12,Short.MAX_VALUE);c.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH-12,c.getPreferredSize().height));}
		panel.setSize(PluginPanel.PANEL_WIDTH,10000);layout(panel);
		int height=Math.min(5000,panel.getPreferredSize().height);panel.setSize(PluginPanel.PANEL_WIDTH,height);layout(panel);
		BufferedImage image=new BufferedImage(PluginPanel.PANEL_WIDTH,height,BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics=image.createGraphics();panel.paint(graphics);graphics.dispose();
		Path directory=Path.of("build/reports/p4a");Files.createDirectories(directory);ImageIO.write(image,"png",directory.resolve(filename).toFile());
	}
	private void layout(Container c){c.doLayout();for(Component child:c.getComponents())if(child instanceof Container)layout((Container)child);}
}
