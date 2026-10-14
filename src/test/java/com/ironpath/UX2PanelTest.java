package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.*;
import com.ironpath.model.*;
import com.ironpath.preferences.Preferences;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.ironpath.advisor.EP1Fixtures.*;
import static com.ironpath.advisor.R15Fixtures.*;

/** Original synthetic accounts at real Swing width; placeholders are not client sprites. */
public class UX2PanelTest
{
	private KnowledgeCatalog catalog; private Advisor advisor; private IronpathPanel panel;
	private AccountSnapshot account; private Preferences preferences; private PursuitOverview overview;
	private int edits; private PaceScenario pace; private PoweredMagic.Setup reference;
	private final IronpathPanel.Actions actions=new IronpathPanel.Actions() {
		public void goal(String id) { edits++; preferences=preferences.goal(id); render(); }
		public void skip(String id,boolean value) { edits++; preferences=preferences.skip(id,value); render(); }
		public void confirm(String key,Boolean value) { edits++; preferences=preferences.confirm(key,value,com.ironpath.advisor.P4aFixtures.TIME); render(); }
		public void clear() { edits++; preferences=Preferences.EMPTY; render(); }
		public void reference(String key,String value) { edits++; }
		public void pace(String value) { edits++; pace=value.isBlank()?null:new PaceScenario("demonic-gorillas","ordinary",30,60,"Synthetic full-cycle manual scenario",com.ironpath.advisor.P4aFixtures.TIME); render(); }
	};
	@Before public void setup() throws Exception {
		catalog=KnowledgeCatalog.load(new Gson()); advisor=new Advisor(catalog); account=ready(); preferences=absent(catalog);
		SwingUtilities.invokeAndWait(()->{panel=new IronpathPanel();render();});
	}
	private void render() {
		List<Advisor.UpgradeView> all=advisor.upgrades(account,preferences,reference==null?new PoweredMagic(catalog).propose(account):reference,pace);
		overview=new PursuitOverview(all,advisor.activityOpportunities(account,preferences,all),List.of(),List.of(),advisor.ownedUpgrades(account,preferences));
		panel.render("Synthetic UX2 validation",account,List.of(),catalog,null,preferences,actions,"Synthetic local storage",List.of(),overview,new PoweredMagic(catalog).propose(account));
	}
	private List<Component> children(Container parent) { List<Component> out=new ArrayList<>();for(Component c:parent.getComponents()){out.add(c);if(c instanceof Container)out.addAll(children((Container)c));}return out; }
	private Component named(String name) { return children(panel).stream().filter(c->name.equals(c.getName())).findFirst().orElseThrow(); }
	private JButton button(String name) { return children(panel).stream().filter(c->c instanceof JButton).map(c->(JButton)c).filter(b->name.equals(b.getText())||name.equals(b.getToolTipText())||name.equals(b.getName())).findFirst().orElseThrow(); }
	private boolean visible(Component c) { for(Component p=c;p!=null;p=p.getParent()){if(!p.isVisible())return false;if(p==panel)return true;}return false; }
	private String shown() { return children(panel).stream().filter(c->c instanceof JTextArea && visible(c)).map(c->((JTextArea)c).getText()).collect(Collectors.joining("\n")); }
	private void layout(Container c) { c.doLayout();for(Component child:c.getComponents())if(child instanceof Container)layout((Container)child); }
	private void size() { panel.setSize(225,20000);for(int i=0;i<6;i++)layout(panel);panel.setSize(225,panel.getPreferredSize().height);for(int i=0;i<6;i++)layout(panel); }
	private void capture(String name) throws Exception {
		size();Path dir=Path.of("build/validation/ad1c/images");Files.createDirectories(dir);
		BufferedImage image=new BufferedImage(225,600,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();g.setColor(panel.getBackground());g.fillRect(0,0,225,600);panel.printAll(g);g.setColor(panel.getBackground());g.fillRect(0,0,225,600);panel.printAll(g);g.dispose();ImageIO.write(image,"png",dir.resolve(name+".png").toFile());
		for(Component c:children(panel))if(visible(c)&&(c instanceof JTextArea||c instanceof JButton||c instanceof JLabel)) {
			Point at=SwingUtilities.convertPoint(c,0,0,panel);
			assertTrue(c.getName()+" extends past sidebar: "+at+" "+c.getSize(),at.x>=0 && at.x+c.getWidth()<=225);
		}
		StringBuilder bounds=new StringBuilder("225 px component, 213 px inner width. Synthetic account, fallback sprites.\n");
		for(Component c:children(panel))if(c instanceof JTextArea && visible(c)) {
			JTextArea label=(JTextArea)c;assertTrue(label.getWidth()>0);assertTrue(label.getWidth()<=213);
			assertTrue(label.getText(),label.getHeight()>=label.getPreferredSize().height);
			bounds.append(label.getName()).append(" ").append(c.getBounds()).append(" ").append(label.getText()).append("\n");
		}
		Files.writeString(dir.resolve(name+"-bounds.txt"),bounds);
	}
	@Test public void everyAccountUsesDomainOrderOneCardActionAndNoReferenceBadges() throws Exception {
		SwingUtilities.invokeAndWait(()->{try {
			for(var fixture:profiles(catalog).entrySet()) {
				account=fixture.getValue();panel=new IronpathPanel();render();capture(fixture.getKey()+"-home");
				List<String> actual=children(panel).stream().map(Component::getName).filter(n->n!=null&&n.startsWith("upgrade-card-")).map(n->n.substring(13)).collect(Collectors.toList());
				assertEquals(overview.preview.stream().map(u->u.upgrade.id).collect(Collectors.toList()),actual);
				assertFalse(shown().contains("0 Defence"));assertFalse(shown().contains("long tail"));assertFalse(shown().contains("model gap"));
				for(Component c:children(panel))if(c.getName()!=null&&c.getName().startsWith("upgrade-card-")&&visible(c)) {
					assertEquals(1,children((Container)c).stream().filter(x->x instanceof JButton).count());
					assertEquals(1,children((Container)c).stream().filter(x->"card-priority-reason".equals(x.getName())).count());
					assertTrue(children((Container)c).stream().filter(x->"card-status".equals(x.getName())).count()<=1);
				}
			}
		} catch(Exception e){throw new RuntimeException(e);} });
	}
	@Test public void compactComparisonAcquisitionAndSecondaryBasisRemainDistinct() throws Exception {
		SwingUtilities.invokeAndWait(()->{try {
			button("Browse").doClick();button("open-upgrade-toxic-blowpipe").doClick();capture("supported-blowpipe-value");assertTrue(shown().contains("Theoretical DPS"));button("← Upgrades").doClick();button("open-upgrade-zaryte-crossbow").doClick();capture("zaryte-value");
			assertTrue(shown().contains("Why this position"));assertTrue(shown().contains("Special attack burst"));
			assertFalse(shown().contains("0 Defence"));assertFalse(visible(named("value-evidence")));
			button("Evidence and calculation basis").doClick();assertTrue(shown().contains("Enemy with 0 Defence"));
			button("Acquisition").doClick();capture("zaryte-path");assertTrue(shown().contains("From Nex"));assertTrue(shown().contains("Next step"));
			assertFalse(shown().contains("Model "));assertFalse(visible(named("acquisition-evidence")));
			button("← Upgrades").doClick();button("open-upgrade-masori-body").doClick();button("Acquisition").doClick();capture("masori-path");
			assertTrue(shown().contains("Tombs of Amascut"));assertTrue(shown().contains("Hours not estimated"));
		} catch(Exception e){throw new RuntimeException(e);} });
	}
	@Test public void unavailableInputAndUnsupportedModelOfferDifferentActions() throws Exception {
		SwingUtilities.invokeAndWait(()->{try {
			account=ready();reference=new PoweredMagic(catalog).propose(account).edit("head","-1");panel=new IronpathPanel();render();button("Browse").doClick();button("open-upgrade-tumekens-shadow").doClick();capture("shadow-input-check");
			assertTrue(shown().contains("Theoretical DPS not calculated"));assertTrue(children(panel).stream().anyMatch(c->"correct-comparison-input".equals(c.getName())));
			button("correct-comparison-input").doClick();assertTrue(visible(named("reference-corrections")));
			JButton stale=button("Apply weapon");int previous=edits;panel.render("Account changed",null,List.of(),catalog,null,Preferences.EMPTY,null,"",List.of());stale.doClick();assertEquals(previous,edits);
			account=items(ready(),31106);reference=new PoweredMagic(catalog).propose(account).edit("hands","31106");panel=new IronpathPanel();render();button("Browse").doClick();button("open-upgrade-tumekens-shadow").doClick();capture("shadow-model-gap");
			assertTrue(shown().contains("Theoretical DPS not calculated"));assertTrue(children(panel).stream().noneMatch(c->"correct-comparison-input".equals(c.getName())));
		} catch(Exception e){throw new RuntimeException(e);} });
	}
	@Test public void accountCorrectionsDiagnosticsAndStaleActionsPreserveIdentity() throws Exception {
		SwingUtilities.invokeAndWait(()->{try {
			button("Account").doClick();capture("account");assertFalse(shown().contains("varbits"));assertFalse(shown().contains("Knowledge "));assertFalse(shown().contains("Synthetic local storage"));
			button("Correct ownership and unlocks").doClick();JButton old=button("Owned / unlocked");capture("account-corrections");
			button("Diagnostics").doClick();capture("diagnostics");assertTrue(shown().contains("Knowledge "));assertTrue(shown().contains("Client varbits"));assertTrue(shown().contains("Bank:"));
			button("Back to account").doClick();int previous=edits;old.doClick();assertEquals(previous,edits);
			button("← Upgrades").doClick();assertNotNull(named("homepage-deck"));
		} catch(Exception e){throw new RuntimeException(e);} });
	}
	@Test public void manualPaceIsScopedAndClearReturnsToUnavailableHours() throws Exception {
		SwingUtilities.invokeAndWait(()->{try {
			button("Browse").doClick();button("open-upgrade-tormented-bracelet").doClick();button("Acquisition").doClick();
			assertFalse(shown().contains("Estimated mean"));button("Estimate basis and route evidence").doClick();((JTextField)named("pace-input")).setText("30-60");button("Apply pace").doClick();capture("manual-next-shard");
			assertTrue(shown().contains("Estimated mean"));assertTrue(shown().contains("Next zenyte shard"));assertTrue(shown().contains("manual scenario"));
			button("Estimate basis and route evidence").doClick();button("Clear pace").doClick();assertFalse(shown().contains("Estimated mean"));
		} catch(Exception e){throw new RuntimeException(e);} });
	}
	@Test public void largeFontsWrapAndKeyboardOpeningKeepsSearchAndFilters() throws Exception {
		SwingUtilities.invokeAndWait(()->{try {
			panel.setFont(panel.getFont().deriveFont(20f));render();capture("large-font-home");button("Browse").doClick();((JTextField)named("browse-search")).setText("zaryte");
			JButton card=button("open-upgrade-zaryte-crossbow");assertEquals("open-card",card.getInputMap().get(KeyStroke.getKeyStroke("ENTER")));assertEquals("open-card",card.getInputMap().get(KeyStroke.getKeyStroke("SPACE")));
			card.getActionMap().get("open-card").actionPerformed(new java.awt.event.ActionEvent(card,0,"Enter"));capture("large-font-value");
			button("Acquisition").doClick();capture("large-font-path");button("← Upgrades").doClick();assertEquals("zaryte",((JTextField)named("browse-search")).getText());
			button("Account").doClick();capture("large-font-account");button("Diagnostics").doClick();capture("large-font-diagnostics");
		} catch(Exception e){throw new RuntimeException(e);} });
	}
	@Test public void ad1aDpsFirstAndCompletePackageChangesRemainReadableAtNativeWidth() throws Exception {
		SwingUtilities.invokeAndWait(()->{try {
			for(float font:List.of(12f,20f)) {
				account=AD1aComparisonTest.brimstoneFixture();reference=null;panel=new IronpathPanel();panel.setFont(panel.getFont().deriveFont(font));render();
				button("Browse").doClick();button("open-upgrade-magus-ring").doClick();
				assertTrue(shown().contains("% DPS"));assertTrue(shown().contains("Brimstone ring"));
				assertFalse(shown().contains("Theoretical DPS not calculated"));
				assertTrue(shown().indexOf("Max hit")<0 || shown().indexOf("Theoretical DPS")<shown().indexOf("Max hit"));capture("ad1a-magus-"+(int)font);
				account=AD1aComparisonTest.bucklerFixture();panel=new IronpathPanel();panel.setFont(panel.getFont().deriveFont(font));render();
				button("Browse").doClick();button("open-upgrade-twisted-buckler").doClick();
				assertTrue(shown().contains("Setup change"));assertFalse(shown().contains("Theoretical DPS not calculated"));
				assertTrue(shown().contains("Blazing blowpipe"));assertTrue(shown().contains("Rune crossbow"));
				assertTrue(shown().contains("Twisted buckler"));assertTrue(shown().contains("Dragon dart"));
				assertTrue(shown().contains("No loaded darts"));assertTrue(shown().contains("Runite bolts"));
				capture("ad1a-buckler-"+(int)font);
			}
		} catch(Exception e){throw new RuntimeException(e);} });
	}
	@Test public void groupedOwnershipCorrectionsReturnToTheSameDetailAndRejectStaleEdits() throws Exception {
		SwingUtilities.invokeAndWait(()->{try {
			account=ClientEquipmentRegressionTest.reportedLightbearerFixture();preferences=Preferences.EMPTY;panel=new IronpathPanel();render();
			assertTrue(shown().contains("Recommended order"));assertFalse(shown().contains("Check ownership"));
			button("Browse").doClick();((JTextField)named("browse-search")).setText("zaryte");button("open-upgrade-zaryte-vambraces").doClick();
			button("Acquisition").doClick();button("Account").doClick();button("Checks affecting advice").doClick();
			AccountChecks.Check check=overview.checks.ownership.stream().filter(c->c.outcomes.size()>1).findFirst().orElseThrow();
			button("account-check-"+check.key).doClick();
			assertTrue(visible(named("account-corrections")));
			assertEquals(catalog.factChoices().get(check.key),((JComboBox<?>)named("account-fact")).getSelectedItem().toString());
			capture("shared-correction");JButton stale=button("Not owned / locked");
			button("← Upgrades").doClick();assertTrue(shown().contains("Next step"));
			assertEquals("Selected view",button("Acquisition").getAccessibleContext().getAccessibleDescription());
			int previous=edits;stale.doClick();assertEquals(previous,edits);
			button("← Upgrades").doClick();assertEquals("zaryte",((JTextField)named("browse-search")).getText());
			button("Account").doClick();button("Not owned / locked").doClick();
			assertTrue(overview.checks.ownership.stream().noneMatch(c->c.key.equals(check.key)));
		} catch(Exception e){throw new RuntimeException(e);} });
	}

}
