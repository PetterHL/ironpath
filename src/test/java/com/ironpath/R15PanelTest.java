package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.*;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.P4aFixtures.*;
import static com.ironpath.advisor.R15Fixtures.*;
import static org.junit.Assert.*;

/** Native Swing at 225 logical pixels with explicit sprite fallbacks; no real-client evidence. */
public class R15PanelTest
{
	private KnowledgeCatalog catalog;private Advisor advisor;private IronpathPanel panel;
	private AccountSnapshot account;private Preferences preferences;private PoweredMagic.Setup reference;private PaceScenario pace;
	@Before public void setup() throws Exception
	{
		catalog=KnowledgeCatalog.load(new Gson());advisor=new Advisor(catalog);account=developed(false);preferences=absent(catalog);reference=new PoweredMagic(catalog).propose(account, "graardor");
		SwingUtilities.invokeAndWait(()->{panel=new IronpathPanel();render();});
	}
	private void render()
	{
		List<Advisor.UpgradeView> views=advisor.upgrades(account,preferences,reference,pace);
		panel.render("Synthetic R1.5a evidence",account,advisor.evaluate(account,preferences),catalog,null,preferences,new IronpathPanel.Actions()
		{
			public void goal(String id){preferences=preferences.goal(id);render();}
			public void skip(String id,boolean value){preferences=preferences.skip(id,value);render();}
			public void confirm(String key,Boolean value){preferences=preferences.confirm(key,value,TIME);render();}
			public void clear(){preferences=Preferences.EMPTY;render();}
			public void pace(String input){pace=input.isBlank()?null:new PaceScenario("demonic-gorillas","ordinary",Double.parseDouble(input),Double.parseDouble(input),"Full cycle scenario",TIME);render();}
		},"Synthetic storage",List.of(),new PursuitOverview(views,advisor.activityOpportunities(account,preferences,views),advisor.quickActions(account,preferences),advisor.availableActivities(account,preferences)),reference);
	}
	private List<Component> children(Container parent)
	{ List<Component> list=new ArrayList<>();for(Component child:parent.getComponents()){list.add(child);if(child instanceof Container)list.addAll(children((Container)child));}return list; }
	private JButton button(String title) { return children(panel).stream().filter(c->c instanceof JButton).map(c->(JButton)c).filter(b->title.equals(b.getText())||title.equals(b.getToolTipText())).findFirst().orElseThrow(); }
	private JButton open(String id)
	{
		if(children(panel).stream().noneMatch(c->("open-upgrade-"+id).equals(c.getName())))button("Browse").doClick();
		return children(panel).stream().filter(c->("open-upgrade-"+id).equals(c.getName())).map(c->(JButton)c).findFirst().orElseThrow();
	}
	private String text() { return children(panel).stream().filter(c->c instanceof JTextArea).map(c->((JTextArea)c).getText()).collect(java.util.stream.Collectors.joining("\n")); }
	@Test public void neutralWeaponAndArmourSummariesKeepTheirMetricOrderAndVisibleGaps() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			try
			{
				account=items(developed(false),12002,19544,31106);
				reference=new PoweredMagic(catalog).propose(account).edit("hands","19544");render();
				assertTrue(text().contains("Enemy with 0 Defence"));
				capture("neutral-overview");button("Browse").doClick();capture("neutral-browse");
				assertTrue(text().contains("DPS "));assertFalse(text().contains("Graardor"));
				open("tumekens-shadow").doClick();
				assertTrue(text().indexOf("Theoretical DPS")<text().indexOf("Max hit"));
				assertTrue(text().contains("4 → 5 ticks (2.4 → 3.0 s)"));capture("neutral-weapon-value");
				button("Back to upgrades").doClick();open("ancestral-hat").doClick();
				assertTrue(text().indexOf("Max hit")<text().indexOf("Theoretical DPS"));capture("neutral-armour-value");
				button("Back to upgrades").doClick();reference=reference.edit("hands","31106");render();
				assertTrue(text().contains("DPS unavailable · model gap"));open("tumekens-shadow").doClick();
				assertTrue(text().indexOf("Theoretical DPS unavailable")<text().indexOf("Max hit"));capture("neutral-history-gap");
			}
			catch(Exception failure){throw new RuntimeException(failure);}
		});
	}
	@Test public void overviewBrowseValuePathActivityUnknownEmptyAndUnsupportedCaptures() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			try
			{
				assertTrue(text().contains("Long-term upgrades"));capture("overview");button("Browse").doClick();capture("browse");
				assertEquals(advisor.upgrades(account,preferences).size(),children(panel).stream().filter(c->c.getName()!=null&&c.getName().startsWith("open-upgrade-")).count());
				open("tormented-bracelet").doClick();capture("value");button("Path").doClick();assertTrue(text().contains("Mean 300 kills"));assertTrue(text().contains("Hours unavailable"));capture("path-kills");
				button("Set pace (optional)").doClick();JTextField input=children(panel).stream().filter(c->"pace-input".equals(c.getName())).map(c->(JTextField)c).findFirst().orElseThrow();input.setText("40");button("Apply pace").doClick();
				assertTrue(text().contains("Mean 7.5 h"));assertTrue(text().contains("17.3 h"));assertTrue(text().contains("not a completed tormented bracelet"));capture("path-hours");
				button("Clear pace").doClick();assertNull(pace);assertTrue(text().contains("Hours unavailable"));
				button("Back to upgrades").doClick();button("Shared activities").doClick();children(panel).stream().filter(c->"open-activity:chambers-of-xeric/ordinary".equals(c.getName())).map(c->(JButton)c).findFirst().orElseThrow().doClick();
				assertTrue(text().contains("Useful missing reward: Rigour"));assertTrue(text().contains("one dependent armour family"));capture("activity");button("Back to upgrades").doClick();
				open("tumekens-shadow").doClick();reference=reference.edit("target","toa");render();assertTrue(text().contains("Theoretical DPS unavailable"));capture("unsupported");
				button("Back to upgrades").doClick();button("Overview").doClick();preferences=Preferences.EMPTY;render();capture("unknown");
				preferences=absent(catalog);account=items(developed(true),12002,19544,28313,21018,21021,21024,26374,31097);reference=new PoweredMagic(catalog).propose(account, "graardor");render();capture("empty");
				assertTrue(text().contains("No remaining supported upgrade"));
			}
			catch(Exception failure) { throw new RuntimeException(failure); }
		});
	}
	@Test public void fontMetricsAndLongNamesTriggerSingleColumnWithoutClippingOrShrinking() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			button("Browse").doClick();layoutPanel();BrowseGrid grid=(BrowseGrid)children(panel).stream().filter(c->c instanceof BrowseGrid).findFirst().orElseThrow();
			assertEquals(2,grid.columns(213));assertEquals(1,grid.columns(190));
			JTextArea title=children((Container)grid.getComponent(0)).stream().filter(c->c instanceof JTextArea).map(c->(JTextArea)c).findFirst().orElseThrow();String original=title.getText();
			title.setText("An extraordinarily long reviewed equipment name for a supported specialist");assertEquals(1,grid.columns(213));title.setText(original);
			float font=grid.getFont().getSize2D();for(Component child:children(grid))child.setFont(child.getFont().deriveFont(20f));
			assertEquals(1,grid.columns(213));assertEquals(font,grid.getFont().getSize2D(),0);
			layoutPanel();for(Component tile:grid.getComponents())assertTrue(tile.getX()+tile.getWidth()<=grid.getWidth());
			try { capture("browse-large-font"); } catch(Exception failure) { throw new RuntimeException(failure); }
		});
	}
	@Test public void pursuitRowsAllocateEnoughHeightForFullWrappingNamesAndContext() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			layoutPanel();
			for(Component child:children(panel))
			{
				if(!(child instanceof JTextArea))continue;
				Component ancestor=child;boolean visible=true;while(ancestor!=panel){visible &= ancestor.isVisible();ancestor=ancestor.getParent();if(ancestor==null){visible=false;break;}}
				if(visible){assertTrue(((JTextArea)child).getText()+": allocated height "+child.getHeight()+" < wrapping height "+child.getPreferredSize().height,child.getHeight()>=child.getPreferredSize().height);}
			}
		});
	}
	@Test public void categorySelectionKeyboardActivationAndStaleControlsAreGuarded() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			button("Browse").doClick();JComboBox<?> filter=children(panel).stream().filter(c->"browse-category".equals(c.getName())).map(c->(JComboBox<?>)c).findFirst().orElseThrow();filter.setSelectedItem("Long-term");
			JButton shadow=open("tumekens-shadow");assertNotNull(shadow.getAccessibleContext().getAccessibleName());assertNotNull(shadow.getInputMap().get(KeyStroke.getKeyStroke("ENTER")));
			shadow.getActionMap().get("open-card").actionPerformed(new java.awt.event.ActionEvent(shadow,0,""));JButton oldPath=button("Path");button("Back to upgrades").doClick();
			assertTrue(text().contains("· Long-term"));oldPath.doClick();assertFalse(text().contains("Acquisition paths"));
			panel.render("Account changed",null,List.of(),catalog,null,Preferences.EMPTY,null,"",List.of());shadow.doClick();oldPath.doClick();assertFalse(text().contains("Tumeken"));
		});
	}
	@Test public void delayedSpriteOnlyUpdatesDetachedComponentsAfterAccountClear() throws Exception
	{
		List<JLabel> pending=new ArrayList<>();
		net.runelite.client.game.ItemManager manager=org.mockito.Mockito.mock(net.runelite.client.game.ItemManager.class);
		net.runelite.client.util.AsyncBufferedImage sprite=org.mockito.Mockito.mock(net.runelite.client.util.AsyncBufferedImage.class);
		org.mockito.Mockito.when(manager.getImage(org.mockito.ArgumentMatchers.anyInt())).thenReturn(sprite);
		org.mockito.Mockito.doAnswer(call->{pending.add(call.getArgument(0));return null;}).when(sprite).addTo(org.mockito.ArgumentMatchers.any(JLabel.class));
		SwingUtilities.invokeAndWait(()->{
			panel=new IronpathPanel(manager);render();assertFalse(pending.isEmpty());
			panel.render("Account changed",null,List.of(),catalog,null,Preferences.EMPTY,null,"",List.of());
			for(JLabel label:pending){label.setIcon(new ImageIcon(new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB)));assertFalse(SwingUtilities.isDescendingFrom(label,panel));}
			assertFalse(text().contains("Tormented"));assertFalse(text().contains("Tumeken"));
		});
	}
	private void layout(Container parent) { parent.doLayout();for(Component child:parent.getComponents())if(child instanceof Container)layout((Container)child); }
	@Test public void detailTextFitsItsActualWrappedDocumentAtWiderPanelWidths() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			open("tumekens-shadow").doClick();
			for(int width:new int[]{225,245,300})
			for(float font:new float[]{12,14,16,20})
			{
				for(Component child:children(panel))if(child instanceof JTextArea)child.setFont(child.getFont().deriveFont(font));
				for(Component child:children(panel))if(child instanceof Container)((Container)child).invalidate();panel.invalidate();
				panel.setSize(width,20000);for(int i=0;i<4;i++)layout(panel);
				panel.setSize(width,panel.getPreferredSize().height);for(int i=0;i<4;i++)layout(panel);
				for(Component child:children(panel))
				{
					if(!(child instanceof JTextArea)||!child.isShowing()&&!child.isVisible())continue;
					JTextArea area=(JTextArea)child;
					if(area.getHeight()==0)continue;
					try
					{
						area.getUI().getRootView(area).setSize(area.getWidth()-area.getInsets().left-area.getInsets().right,area.getHeight()-area.getInsets().top-area.getInsets().bottom);
						java.awt.geom.Rectangle2D end=area.modelToView2D(area.getDocument().getLength());
						assertNotNull(end);
						assertTrue(width+" font "+font+" size "+area.getSize()+" preferred "+area.getPreferredSize()+" end "+end+": "+area.getText(),end.getMaxY()+area.getInsets().bottom<=area.getHeight());
					}
					catch(javax.swing.text.BadLocationException failure){throw new AssertionError(failure);}
				}
			}
		});
	}
	private void layoutPanel()
	{
		panel.setSize(225,20000);for(int i=0;i<3;i++)layout(panel);panel.setSize(225,panel.getPreferredSize().height);for(int i=0;i<3;i++)layout(panel);
	}
	private void capture(String name) throws Exception
	{
		layoutPanel();int height=Math.min(8000,panel.getPreferredSize().height);panel.setSize(225,height);layout(panel);
		BufferedImage image=new BufferedImage(225,height,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();panel.paint(g);g.dispose();
		Path path=Path.of("build/reports/r15/images");Files.createDirectories(path);ImageIO.write(image,"png",path.resolve(name+".png").toFile());
	}
}
