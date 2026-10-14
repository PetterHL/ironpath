package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.*;
import com.ironpath.model.*;
import com.ironpath.preferences.Preferences;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.P4aFixtures.*;
import static com.ironpath.advisor.R15Fixtures.*;
import static org.junit.Assert.*;

/** Native component evidence. Fallback/loading sprites and synthetic accounts, never a client pass. */
public class R3PanelTest
{
	private KnowledgeCatalog catalog; private Advisor advisor; private IronpathPanel panel;
	private AccountSnapshot account; private Preferences preferences; private PursuitOverview overview; private JScrollPane scroll;
	@Before public void load() throws Exception
	{
		catalog=KnowledgeCatalog.load(new Gson()); advisor=new Advisor(catalog); preferences=absent(catalog);
		account=R2AdviceTest.postBowfa();
		SwingUtilities.invokeAndWait(()->{ panel=new IronpathPanel(); scroll=new JScrollPane(panel); scroll.setBorder(null); scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); render(); });
	}
	private void render()
	{
		List<Advisor.UpgradeView> all=advisor.upgrades(account,preferences);
		overview=new PursuitOverview(all,advisor.activityOpportunities(account,preferences,all),List.of(),List.of(),advisor.ownedUpgrades(account,preferences));
		panel.render("Synthetic R3a evidence",account,List.of(),catalog,null,preferences,new IronpathPanel.Actions()
		{
			public void goal(String id) {} public void clear() {} public void confirm(String key,Boolean v) {}
			public void skip(String id,boolean v) { preferences=preferences.skip(id,v);render(); }
		},"Synthetic local storage",List.of(),overview,new PoweredMagic(catalog).propose(account));
	}
	@Test public void reportedLightbearerHasNumbersAndSpecialistWithoutRosewood() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			try
			{
				account=ClientEquipmentRegressionTest.reportedLightbearerFixture(); preferences=Preferences.EMPTY; render();
				assertTrue(children(panel).stream().noneMatch(c->"upgrade-card-rosewood-blowpipe".equals(c.getName())));
				assertTrue(children(panel).stream().noneMatch(c->"upgrade-card-crystal-body".equals(c.getName())||"upgrade-card-toxic-blowpipe".equals(c.getName())));
				capture("lightbearer-home"); button("Specialist").doClick();
				assertNotNull(named("upgrade-card-venator-bow"));
				assertTrue(children(panel).stream().noneMatch(c->"upgrade-card-rosewood-blowpipe".equals(c.getName())));
				capture("lightbearer-specialist");
				((JComboBox<?>)named("browse-category")).setSelectedItem("All");
				((JButton)named("open-upgrade-zaryte-vambraces")).doClick();
				assertFalse(text().contains("Unreviewed or incompatible ranged item"));
				assertTrue(text().contains("Theoretical DPS")); capture("lightbearer-value");
			}
			catch(Exception e){throw new RuntimeException(e);}
		});
	}
	@Test public void emptySpecialistExplainsScopeAndKeepsOtherBrowseChoices() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			account=ClientEquipmentRegressionTest.unreadDartsFixture();
			preferences=Preferences.EMPTY.skip("muspah",true); render(); button("Specialist").doClick();
			assertTrue(text().contains("0 upgrades")); assertTrue(text().contains("Owned outcomes, skipped routes or unconfirmed method inputs"));
			((JComboBox<?>)named("browse-category")).setSelectedItem("All");
			assertNotNull(named("upgrade-card-zaryte-vambraces"));
		});
	}
	private List<Component> children(Container parent)
	{ List<Component> result=new ArrayList<>();for(Component c:parent.getComponents()){result.add(c);if(c instanceof Container)result.addAll(children((Container)c));}return result; }
	private Component named(String id) { return children(panel).stream().filter(c->id.equals(c.getName())).findFirst().orElseThrow(); }
	private JButton button(String title) { return children(panel).stream().filter(c->c instanceof JButton).map(c->(JButton)c).filter(c->title.equals(c.getText())||title.equals(c.getToolTipText())).findFirst().orElseThrow(); }
	private List<String> cardIds() { return children(panel).stream().map(Component::getName).filter(n->n!=null&&n.startsWith("upgrade-card-")).collect(java.util.stream.Collectors.toList()); }
	private void settle() throws Exception { SwingUtilities.invokeAndWait(()->{}); SwingUtilities.invokeAndWait(()->{}); }
	private String text() { return children(panel).stream().filter(c->c instanceof JTextArea).map(c->((JTextArea)c).getText()).collect(java.util.stream.Collectors.joining("\n")); }
	private boolean visible(Component c)
	{ for(Component p=c;p!=null;p=p.getParent()){if(!p.isVisible())return false;if(p==panel)return true;}return false; }
	private void layout(Container parent) { parent.doLayout(); for(Component c:parent.getComponents())if(c instanceof Container)layout((Container)c); }
	private void size()
	{
		panel.setSize(225,20000);for(int i=0;i<4;i++)layout(panel);
		panel.setSize(225,panel.getPreferredSize().height);for(int i=0;i<4;i++)layout(panel);
	}
	private void capture(String name) throws Exception
	{ capture(name,0); }
	private void capture(String name,int offsetY) throws Exception
	{
		size();
		StringBuilder bounds=new StringBuilder();
		for(Component c:children(panel))if(c instanceof JTextArea&&visible(c)){
			bounds.append(c.getName()).append(" ").append(((JTextArea)c).getText()).append(" ").append(c.getBounds()).append(" parent ").append(c.getParent().getBounds()).append("\n");
		}
		Path boundsPath=Path.of("build/reports/r3a/images",name+"-bounds.txt");Files.createDirectories(boundsPath.getParent());Files.writeString(boundsPath,bounds);
		BufferedImage image=new BufferedImage(225,600,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();
		g.setColor(net.runelite.client.ui.ColorScheme.DARK_GRAY_COLOR);g.fillRect(0,0,225,600);
		g.translate(0,-offsetY);panel.printAll(g);g.dispose();
		Path dir=Path.of("build/reports/r3a/images");Files.createDirectories(dir);ImageIO.write(image,"png",dir.resolve(name+".png").toFile());
	}
	@Test public void compactHomepageAndBrowseShareFullCardsAndMeasuredDensity() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			try
			{
				capture("post-bowfa-home");
				HomepageDeck deck=(HomepageDeck)named("homepage-deck");assertEquals(1,deck.columns(213));
				List<Component> cards=children(deck).stream().filter(c->c.getName()!=null&&c.getName().startsWith("upgrade-card-")&&c.isVisible()).collect(java.util.stream.Collectors.toList());
				int complete=0;StringBuilder heights=new StringBuilder("225x600 synthetic native component, 213px inner width; placeholder imagery\n");
				for(Component card:cards){int top=SwingUtilities.convertPoint(card,0,0,panel).y;if(top+card.getHeight()<=600)complete++;heights.append(card.getName()).append(": top ").append(top).append(", height ").append(card.getHeight()).append("\n");}
				assertTrue(heights.toString(),complete>=2);assertTrue(cards.size()>complete);
				Path report=Path.of("build/reports/r3a/heights.txt");Files.writeString(report,heights.toString());
				assertTrue(visible(button("Long-term")));assertTrue(visible(button("Specialist")));
				for(Component card:cards)
				{
					String id=card.getName().substring("upgrade-card-".length());assertTrue(visible(named("open-upgrade-"+id)));assertTrue(visible(named("path-upgrade-"+id)));
					assertTrue(children((Container)card).stream().anyMatch(c->"card-reason".equals(c.getName())));
				}
				button("Browse").doClick();capture("post-bowfa-browse");
				assertEquals(overview.all.size(),children(panel).stream().filter(c->c.getName()!=null&&c.getName().startsWith("upgrade-card-")).count());
			}
			catch(Exception e){throw new RuntimeException(e);}
		});
	}
	@Test public void directPathChildAndBodyHaveSeparateKeyboardActionsAndBackRestoresFilter() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			button("Specialist").doClick();JComboBox<?> filter=(JComboBox<?>)named("browse-category");assertEquals("Specialist",filter.getSelectedItem());
			JButton path=(JButton)named("path-upgrade-zaryte-crossbow");assertTrue(path.getAccessibleContext().getAccessibleName().contains("Zaryte"));
			path.getActionMap().get("activate").actionPerformed(new java.awt.event.ActionEvent(path,0,"Enter"));
			assertTrue(text().contains("Next:"));assertTrue(text().contains("Acquisition paths"));assertFalse(text().contains("Why this order"));
			button("Back to upgrades").doClick();assertEquals("Specialist",((JComboBox<?>)named("browse-category")).getSelectedItem());
			JButton value=(JButton)named("open-upgrade-zaryte-crossbow");value.doClick();assertNotNull(button("Why this order"));
			button("Back to upgrades").doClick();
			JPanel card=(JPanel)named("upgrade-card-zaryte-crossbow");
			for(java.awt.event.MouseListener listener:card.getMouseListeners())listener.mouseClicked(new java.awt.event.MouseEvent(card,java.awt.event.MouseEvent.MOUSE_CLICKED,0,0,2,2,1,false));
			assertNotNull(button("Why this order"));path.doClick();assertFalse(text().contains("Acquisition paths"));
		});
	}
	@Test public void neutralLossRemainsInValueWhileCardUsesReviewedSpecialistGain() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			try
			{
				button("Browse").doClick();Advisor.UpgradeView tbow=overview.all.stream().filter(v->v.upgrade.id.equals("twisted-bow")).findFirst().orElseThrow();
				assertTrue(tbow.comparison.dpsGain()<0);assertTrue(tbow.card.metric.contains("Zilyana"));
				((JButton)named("open-upgrade-twisted-bow")).doClick();assertTrue(text().contains("Enemy with 0 Defence"));
				assertTrue(text().contains(String.format(java.util.Locale.ROOT,"(%+.2f)",tbow.comparison.dpsGain())));capture("tbow-neutral-value");
				button("Path").doClick();capture("tbow-path");
			}
			catch(Exception e){throw new RuntimeException(e);}
		});
	}
	@Test public void actualWidthAndFontsDriveOneTwoColumnLayoutWithoutClipping() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			BrowseGrid grid=new BrowseGrid();JPanel card=new JPanel();card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));
			JTextArea label=new JTextArea("A reviewed option");label.setLineWrap(true);label.setWrapStyleWord(true);card.add(label);grid.add(card);
			assertEquals(1,grid.columns(213));assertEquals(1,grid.columns(345));assertEquals(2,grid.columns(346));
			label.setText("Averylongunbreakableequipmentname");assertEquals(1,grid.columns(346));
			label.setText("A substantially longer reviewed equipment name");label.setFont(label.getFont().deriveFont(24f));assertEquals(1,grid.columns(346));
			assertEquals(24f,label.getFont().getSize2D(),0);
		});
	}
	@Test public void enlargedFontAndPartialEmptyLongTermAndMegararesHaveHonestCaptures() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			try
			{
				panel.setFont(panel.getFont().deriveFont(20f));render();capture("post-bowfa-large-font");
				for(Component c:children(panel))if(c instanceof JTextArea&&visible(c)){assertEquals(20f,c.getFont().getSize2D(),0);assertTrue(((JTextArea)c).getText(),c.getHeight()>=c.getPreferredSize().height);}
				panel.setFont(panel.getFont().deriveFont(12f));
				account=items(account,20997,27275,22325,11212,27226,27229,27232,12002,19544);render();capture("three-megarares-home");
				button("Specialist").doClick();capture("specialist");((JButton)named("path-upgrade-venator-bow")).doClick();capture("venator-path");
				button("Back to upgrades").doClick();button("Overview").doClick();
				preferences=Preferences.EMPTY;render();capture("partial-home");assertTrue(text().contains("ownership unconfirmed"));
				preferences=absent(catalog);account=items(account,catalog.upgrades.stream().mapToInt(u->u.itemId).toArray());render();capture("all-owned");assertTrue(overview.all.isEmpty());assertFalse(text().contains("Huge upgrade"));
				// All current outcomes except the two long-tail weapons, with known preparation/access.
				Set<Integer> bank=new java.util.HashSet<>(account.bank.value());bank.remove(27275);bank.remove(20997);
				account=new AccountSnapshot(account.levels,account.inventory,account.equipment,fact(bank),account.rigour,account.augury,account.facts,account.equipmentSlots,account.inventoryQuantities,account.equipmentQuantities,account.bankQuantities);
				render();capture("long-term-only");assertTrue(overview.pursuits.isEmpty());assertFalse(overview.longTerm.isEmpty());
				panel.render("Log in to observe this account",null,List.of(),catalog,null,Preferences.EMPTY,null,"",List.of());capture("empty");
			}
			catch(Exception e){throw new RuntimeException(e);}
		});
	}
	@Test public void itemManagerLoadingRequestsAndLateCallbacksCannotCrossAccountGeneration() throws Exception
	{
		List<JLabel> delayed=new ArrayList<>();net.runelite.client.game.ItemManager manager=org.mockito.Mockito.mock(net.runelite.client.game.ItemManager.class);
		net.runelite.client.util.AsyncBufferedImage image=org.mockito.Mockito.mock(net.runelite.client.util.AsyncBufferedImage.class);
		org.mockito.Mockito.when(manager.getImage(org.mockito.ArgumentMatchers.anyInt())).thenReturn(image);
		org.mockito.Mockito.doAnswer(call->{delayed.add(call.getArgument(0));return null;}).when(image).addTo(org.mockito.ArgumentMatchers.any(JLabel.class));
		SwingUtilities.invokeAndWait(()->{
			try
			{
				panel=new IronpathPanel(manager);render();capture("loading-images");assertFalse(delayed.isEmpty());
				JButton oldPath=(JButton)named("path-upgrade-venator-bow");
				panel.render("Account changed",null,List.of(),catalog,null,Preferences.EMPTY,null,"",List.of());
				for(JLabel label:delayed){label.setIcon(new ImageIcon(new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB)));assertNull(label.getIcon());}
				oldPath.doClick();assertFalse(text().contains("Venator bow"));capture("cleared-after-delayed-image");
			}
			catch(Exception e){throw new RuntimeException(e);}
		});
	}
	@Test public void backAndLiveUpdatesKeepStableScrollAnchorsAndProfileClearDropsThem() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			button("Browse").doClick();size();scroll.setSize(242,600);layout(scroll);size();
			Component anchor=named("upgrade-card-necklace-of-anguish");scroll.getViewport().setViewPosition(new Point(0,SwingUtilities.convertPoint(anchor,0,0,panel).y+5));
			((JButton)named("path-upgrade-necklace-of-anguish")).doClick();button("Back to upgrades").doClick();size();
		});
		SwingUtilities.invokeAndWait(()->{});
		SwingUtilities.invokeAndWait(()->{
			Component anchor=named("upgrade-card-necklace-of-anguish");assertEquals(5,scroll.getViewport().getViewPosition().y-SwingUtilities.convertPoint(anchor,0,0,panel).y);
			account=items(account,19544);render();size();
		});
		SwingUtilities.invokeAndWait(()->{});
		SwingUtilities.invokeAndWait(()->{
			Component anchor=named("upgrade-card-necklace-of-anguish");assertEquals(5,scroll.getViewport().getViewPosition().y-SwingUtilities.convertPoint(anchor,0,0,panel).y);
			panel.render("Profile changed",null,List.of(),catalog,null,Preferences.EMPTY,null,"",List.of());assertFalse(text().contains("Necklace of anguish"));
		});
	}
	@Test public void nameSearchCombinesWithCategoryAndClearRestoresOriginalOrder() throws Exception
	{
		List<String> original=new ArrayList<>();
		SwingUtilities.invokeAndWait(()->{
			button("Browse").doClick(); original.addAll(cardIds());
			JTextField search=(JTextField)named("browse-search");
			assertEquals("Search upgrades by item name",search.getAccessibleContext().getAccessibleName());
			JButton oldPath=(JButton)named("path-upgrade-necklace-of-anguish");
			search.setText("  CROSSBOW   zaryte  "); search.setCaretPosition(7);
			assertSame(search,named("browse-search")); // Typing retains the live text editor and focus/caret.
			oldPath.doClick(); assertNotNull(named("browse-search"));
		});
		settle();
		SwingUtilities.invokeAndWait(()->{
			assertEquals(List.of("upgrade-card-zaryte-crossbow"),cardIds());
			assertEquals(7,((JTextField)named("browse-search")).getCaretPosition());
			((JComboBox<?>)named("browse-category")).setSelectedItem("Specialist");
			assertEquals(List.of("upgrade-card-zaryte-crossbow"),cardIds());
			((JComboBox<?>)named("browse-category")).setSelectedItem("Pursue");
			assertTrue(cardIds().isEmpty()); assertTrue(text().contains("No matching upgrades in this category"));
			button("Clear filters").doClick();
			assertEquals("All",((JComboBox<?>)named("browse-category")).getSelectedItem());
			assertEquals("",((JTextField)named("browse-search")).getText()); assertEquals(original,cardIds());
		});
	}
	@Test public void searchAndCategorySurvivePathBackAndLiveAccountUpdates() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{ button("Specialist").doClick(); ((JTextField)named("browse-search")).setText("venator"); });
		settle();
		SwingUtilities.invokeAndWait(()->{
			assertEquals(List.of("upgrade-card-venator-bow"),cardIds());
			((JButton)named("path-upgrade-venator-bow")).doClick(); assertTrue(text().contains("Next:"));
			button("Back to upgrades").doClick();
			assertEquals("venator",((JTextField)named("browse-search")).getText());
			assertEquals("Specialist",((JComboBox<?>)named("browse-category")).getSelectedItem());
			account=items(account,19544); render();
			assertEquals("venator",((JTextField)named("browse-search")).getText());
			assertEquals(List.of("upgrade-card-venator-bow"),cardIds());
		});
	}
	@Test public void pendingSearchAndOldControlsCannotRestoreClearedAccount() throws Exception
	{
		JTextField[] old=new JTextField[1]; JButton[] clear=new JButton[1];
		SwingUtilities.invokeAndWait(()->{
			button("Specialist").doClick(); clear[0]=button("Clear filters");
			old[0]=(JTextField)named("browse-search"); old[0].setText("venator"); old[0].setCaretPosition(6); old[0].moveCaretPosition(2);
			panel.render("Account changed",null,List.of(),catalog,null,Preferences.EMPTY,null,"",List.of());
			clear[0].doClick(); old[0].setText("twisted");
		});
		settle();
		SwingUtilities.invokeAndWait(()->{
			assertTrue(cardIds().isEmpty()); assertFalse(text().contains("Venator bow")); render();
			assertNotNull(named("homepage-deck")); button("Browse").doClick();
			assertEquals("All",((JComboBox<?>)named("browse-category")).getSelectedItem());
			assertEquals("",((JTextField)named("browse-search")).getText());
			assertEquals(0,((JTextField)named("browse-search")).getCaret().getDot());
			assertEquals(0,((JTextField)named("browse-search")).getCaret().getMark());
		});
	}
	@Test public void overviewAndBrowseKeepIndependentAnchorsAndFiltersStartAtTop() throws Exception
	{
		int[] homeY=new int[1],browseY=new int[1];
		SwingUtilities.invokeAndWait(()->{
			size(); scroll.setSize(242,600); layout(scroll); size();
			scroll.getViewport().setViewPosition(new Point(0,210)); homeY[0]=scroll.getViewport().getViewPosition().y;
			button("Browse").doClick(); size();
		});
		settle();
		SwingUtilities.invokeAndWait(()->{
			assertEquals(0,scroll.getViewport().getViewPosition().y);
			Component card=named("upgrade-card-necklace-of-anguish"); browseY[0]=SwingUtilities.convertPoint(card,0,0,panel).y+5;
			scroll.getViewport().setViewPosition(new Point(0,browseY[0])); button("Overview").doClick(); size();
		});
		settle();
		SwingUtilities.invokeAndWait(()->{ assertEquals(homeY[0],scroll.getViewport().getViewPosition().y); button("Browse").doClick(); size(); });
		settle();
		SwingUtilities.invokeAndWait(()->{
			assertEquals(browseY[0],scroll.getViewport().getViewPosition().y);
			((JComboBox<?>)named("browse-category")).setSelectedItem("Specialist"); size();
		});
		settle();
		SwingUtilities.invokeAndWait(()->{ assertEquals(0,scroll.getViewport().getViewPosition().y); });
	}
	@Test public void browseSearchAndEmptyResultsWrapAtNativeWidthAndLargeFont() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			try { button("Browse").doClick(); capture("ux-browse"); ((JTextField)named("browse-search")).setText("zaryte"); }
			catch(Exception e) { throw new RuntimeException(e); }
		});
		settle();
		SwingUtilities.invokeAndWait(()->{
			try { capture("ux-search"); ((JTextField)named("browse-search")).setText("no such upgrade"); }
			catch(Exception e) { throw new RuntimeException(e); }
		});
		settle();
		SwingUtilities.invokeAndWait(()->{
			try {
				assertTrue(cardIds().isEmpty()); assertTrue(text().contains("0 upgrades")); capture("ux-no-results");
				panel.setFont(panel.getFont().deriveFont(20f)); render(); capture("ux-large-font");
				JTextField search=(JTextField)named("browse-search"); assertEquals(20f,search.getFont().getSize2D(),0);
				for(Component c:children(panel)) if(c instanceof JTextArea&&visible(c)) { assertTrue(((JTextArea)c).getText(),c.getHeight()>=c.getPreferredSize().height); }
			}
			catch(Exception e) { throw new RuntimeException(e); }
		});
	}
	@Test public void backAnchorsToSameCardWhenEarlierUpgradeDisappearsDuringDetail() throws Exception
	{
		int[] before=new int[1];
		SwingUtilities.invokeAndWait(()->{
			button("Browse").doClick(); size(); scroll.setSize(242,600); layout(scroll); size();
			Component card=named("upgrade-card-necklace-of-anguish"); before[0]=SwingUtilities.convertPoint(card,0,0,panel).y;
			scroll.getViewport().setViewPosition(new Point(0,before[0]+5));
			((JButton)named("path-upgrade-necklace-of-anguish")).doClick();
			account=items(account,19544); render(); button("Back to upgrades").doClick(); size();
		});
		settle();
		SwingUtilities.invokeAndWait(()->{
			int after=SwingUtilities.convertPoint(named("upgrade-card-necklace-of-anguish"),0,0,panel).y;
			assertTrue("Owning tormented removes an earlier card",after<before[0]);
			assertEquals(after+5,scroll.getViewport().getViewPosition().y);
		});
	}
	@Test public void accountRefreshPreservesSearchCaretSelectionAndReplacementPosition() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			button("Browse").doClick();
			JTextField search=(JTextField)named("browse-search"); search.setText("zaryte crossbow");
			// A backwards selection must preserve its active end, not just the sorted bounds.
			search.setCaretPosition(6); search.moveCaretPosition(2);
			account=items(account,19544); render();
			JTextField refreshed=(JTextField)named("browse-search");
			assertEquals(2,refreshed.getCaret().getDot()); assertEquals(6,refreshed.getCaret().getMark());
			assertEquals("ryte",refreshed.getSelectedText());
			refreshed.replaceSelection("X"); assertEquals("zaX crossbow",refreshed.getText());
			refreshed.setCaretPosition(2); account=items(account,12002); render();
			JTextField next=(JTextField)named("browse-search"); assertEquals(2,next.getCaretPosition());
			next.replaceSelection("Y"); assertEquals("zaYX crossbow",next.getText());
		});
	}
	@Test public void deferredAccountAnchorCannotOverrideNewSearchScroll() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{ button("Browse").doClick(); size(); scroll.setSize(242,600); layout(scroll); size(); });
		settle();
		SwingUtilities.invokeAndWait(()->{
			scroll.getViewport().setViewPosition(new Point(0,81));
			// Queue restoration from the old result set, then type before that callback runs.
			account=items(account,19544); render(); size();
			((JTextField)named("browse-search")).setText("zaryte"); size();
			assertEquals(0,scroll.getViewport().getViewPosition().y);
		});
		settle();
		SwingUtilities.invokeAndWait(()->{
			assertEquals("zaryte",((JTextField)named("browse-search")).getText());
			assertEquals(2,cardIds().size()); assertEquals(0,scroll.getViewport().getViewPosition().y);
		});
	}
	private void selectFacet(String key,String label)
	{
		JComboBox<?> combo=(JComboBox<?>)named("browse-"+key);
		for(int i=0;i<combo.getItemCount();i++) { if(label.equals(combo.getItemAt(i).toString())) {combo.setSelectedIndex(i); return;} }
		throw new AssertionError("Missing "+key+": "+label);
	}
	@Test public void discoveryCountsMatchDestinationsAndHeaderOpensAccountEvidence() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			JButton longer=(JButton)named("home-long-term"); assertEquals("Long-term ("+overview.longTerm.size()+")",longer.getText());
			((JButton)named("account-evidence")).doClick(); assertTrue(text().contains("Bank:")); assertNotNull(named("account-back"));
			assertTrue(children(panel).stream().noneMatch(c->"homepage-deck".equals(c.getName())));
			button("Back to upgrades").doClick(); assertNotNull(named("homepage-deck"));
			button("Long-term").doClick(); assertEquals(overview.longTerm.size(),cardIds().size());
			button("Overview").doClick(); button("Specialist").doClick();
			assertEquals(overview.all.stream().filter(u->u.valueEvidence.specialist).count(),cardIds().size());
			((JButton)named("path-upgrade-zaryte-crossbow")).doClick(); ((JButton)named("account-evidence")).doClick();
			button("Back to upgrades").doClick(); assertTrue(text().contains("Acquisition paths"));
			button("Back to upgrades").doClick(); assertEquals("Specialist",((JComboBox<?>)named("browse-category")).getSelectedItem());
			((JButton)named("path-upgrade-zaryte-crossbow")).doClick(); ((JButton)named("account-evidence")).doClick();
			account=items(account,26374); render(); assertNotNull(named("account-back"));
			button("Back to upgrades").doClick(); assertNotNull(named("browse-category"));
		});
	}
	@Test public void combinedFacetsKeepDomainOrderAndBackRestoresExpandedFilters() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			button("Browse").doClick(); button("More filters").doClick();
			selectFacet("style","Ranged"); selectFacet("slot","neck");
			assertEquals(overview.all.stream().filter(u->new BrowseMetadata(catalog,u.upgrade).matches("Ranged","neck","","")).map(u->"upgrade-card-"+u.upgrade.id).collect(java.util.stream.Collectors.toList()),cardIds());
			assertFalse(cardIds().isEmpty()); String id=cardIds().get(0).substring("upgrade-card-".length());
			((JButton)named("path-upgrade-"+id)).doClick(); button("Back to upgrades").doClick();
			assertTrue(visible(named("browse-style"))); assertEquals("Ranged",((JComboBox<?>)named("browse-style")).getSelectedItem().toString());
			((JTextField)named("browse-search")).setText("not an item"); assertTrue(cardIds().isEmpty());
			button("Clear filters").doClick(); assertEquals(overview.all.size(),cardIds().size());
			selectFacet("role","ranged neck"); assertTrue(cardIds().contains("upgrade-card-necklace-of-anguish"));
			selectFacet("activity","Demonic Gorillas"); assertFalse(cardIds().isEmpty());
		});
	}
	@Test public void ownedSearchExplainsCoverageWithoutAcquisitionOrInventedMetrics() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			account=items(account,19544); preferences=preferences.skip("demonic-gorillas",true); render();
			button("Browse").doClick(); button("More filters").doClick(); ((JCheckBox)named("browse-owned")).doClick();
			((JTextField)named("browse-search")).setText("tormented");
			assertEquals(List.of("upgrade-card-owned:tormented-bracelet"),cardIds());
			((JButton)named("open-upgrade-owned:tormented-bracelet")).doClick();
			assertTrue(text().contains("acquisition is not recommended")); assertTrue(text().contains("not quantified"));
			assertTrue(children(panel).stream().noneMatch(c->c instanceof JButton&&"Path".equals(((JButton)c).getText())));
			button("Back to upgrades").doClick(); assertEquals("tormented",((JTextField)named("browse-search")).getText());
			((JComboBox<?>)named("browse-category")).setSelectedItem("Owned / covered"); assertEquals(1,cardIds().size());
		});
	}
	@Test public void browseUpdateNoticeTracksEvidenceChangesNotRepaintOrNavigation() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			button("Browse").doClick(); render(); assertTrue(children(panel).stream().noneMatch(c->"browse-update".equals(c.getName())));
			account=items(account,19544); render(); assertNotNull(named("browse-update"));
			button("Overview").doClick(); button("Browse").doClick(); assertTrue(children(panel).stream().noneMatch(c->"browse-update".equals(c.getName())));
			panel.render("Profile changed",null,List.of(),catalog,null,Preferences.EMPTY,null,"",List.of()); render(); button("Browse").doClick();
			assertTrue(children(panel).stream().noneMatch(c->"browse-update".equals(c.getName())));
		});
	}
	@Test public void facetsOwnedAndAccountActionsClearAcrossProfileChanges() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			button("Browse").doClick(); button("More filters").doClick(); selectFacet("style","Ranged");
			((JCheckBox)named("browse-owned")).doClick(); JButton oldAccount=(JButton)named("account-evidence");
			JComboBox<?> oldStyle=(JComboBox<?>)named("browse-style");
			panel.render("Profile changed",null,List.of(),catalog,null,Preferences.EMPTY,null,"",List.of()); oldAccount.doClick(); oldStyle.setSelectedIndex(0);
			render(); assertNotNull(named("homepage-deck")); button("Browse").doClick();
			assertFalse(((JCheckBox)named("browse-owned")).isSelected()); assertEquals("Any style",((JComboBox<?>)named("browse-style")).getSelectedItem().toString());
			assertFalse(visible(named("browse-style")));
		});
	}
	@Test public void fullDiscoveryCapturesRetainWrappingWithEnlargedFont() throws Exception
	{
		SwingUtilities.invokeAndWait(()->{
			try {
				capture("ux-completion-home"); ((JButton)named("open-upgrade-tormented-bracelet")).doClick(); capture("ux-completion-value");
				button("Path").doClick(); capture("ux-completion-path"); button("Back to upgrades").doClick();
				((JButton)named("account-evidence")).doClick(); capture("ux-completion-account"); button("Back to upgrades").doClick();
				button("Browse").doClick(); capture("ux-completion-browse");
				button("More filters").doClick(); selectFacet("style","Ranged"); selectFacet("slot","neck"); capture("ux-completion-filters");
				panel.setFont(panel.getFont().deriveFont(20f)); render(); capture("ux-completion-large-font");
				for(Component c:children(panel)) if(c instanceof JTextArea&&visible(c)) {assertTrue(((JTextArea)c).getText(),c.getHeight()>=c.getPreferredSize().height);}
				panel.setFont(panel.getFont().deriveFont(12f)); render(); button("Clear filters").doClick(); button("More filters").doClick();
				((JComboBox<?>)named("browse-category")).setSelectedItem("Owned / covered"); capture("ux-completion-owned");
				String ownedId=cardIds().get(0).substring("upgrade-card-".length()); ((JButton)named("open-upgrade-"+ownedId)).doClick(); capture("ux-completion-owned-value");
			}
			catch(Exception e){throw new RuntimeException(e);}
		});
	}

}
