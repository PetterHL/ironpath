package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.*;
import com.ironpath.encounter.KillImpactResult;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.awt.Component;
import java.awt.Container;
import java.util.*;
import java.util.List;
import javax.swing.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.ironpath.advisor.ZilyanaWornStateComparisonTest.*;

/** Integration regressions at the UX / melee / recent-context boundaries. */
public class CombinedFeaturePanelTest
{
    KnowledgeCatalog catalog; Advisor advisor; IronpathPanel panel; AccountSnapshot account; Preferences preferences;
    IronpathPanel.Actions actions;
    @Before public void setup() throws Exception
    {
        catalog=KnowledgeCatalog.load(new Gson()); advisor=new Advisor(catalog); preferences=R15Fixtures.absent(catalog);
        account=MeleeAdviceTest.ordinary();
        SwingUtilities.invokeAndWait(()->{ panel=new IronpathPanel(); render(); });
    }
    void render()
    {
        var upgrades=advisor.upgrades(account,preferences);
        var overview=new PursuitOverview(upgrades,List.of(),List.of(),List.of(),advisor.ownedUpgrades(account,preferences));
        panel.render("Combined synthetic integration",account,List.of(),catalog,null,preferences,actions,"",List.of(),overview,new PoweredMagic(catalog).propose(account));
    }
    List<Component> children(Container parent)
    { List<Component> values=new ArrayList<>(); for(Component c:parent.getComponents()) { values.add(c); if(c instanceof Container) { values.addAll(children((Container)c)); } } return values; }
    Component named(String name) { return children(panel).stream().filter(c->name.equals(c.getName())).findFirst().orElseThrow(); }
    void click(String name) { ((JButton)named(name)).doClick(); }
    void choose(String name,String label)
    {
        JComboBox<?> combo=(JComboBox<?>)named(name);
        for(int i=0;i<combo.getItemCount();i++) { if(label.equals(combo.getItemAt(i).toString())) { combo.setSelectedIndex(i); return; } }
        fail("Missing facet "+label);
    }
    @Test public void meleeWeaponFacetRetainsAcquisitionThenOwnedUseAfterObservationChange() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            click("home-browse"); click("browse-filters"); choose("browse-style","Melee"); choose("browse-slot","weapon");
            assertNotNull(named("open-upgrade-abyssal-tentacle"));
            account=R15Fixtures.items(account,12006); render(); choose("browse-category","Owned / covered");
            assertNotNull(named("open-upgrade-owned:abyssal-tentacle"));
            assertTrue(children(panel).stream().noneMatch(c->"open-upgrade-abyssal-tentacle".equals(c.getName())));
        });
    }
    @Test public void recentContextAccountEvidenceReturnsAndDisableClearsHistoricalSurface() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            account=owned();
            KillImpactResult result=new ZilyanaWornStateComparison(catalog).evaluate(summary(state(9185,9144,103),Set.of()),account,preferences);
            actions=new IronpathPanel.Actions()
            {
                public void goal(String id) {} public void skip(String id,boolean value) {} public void confirm(String key,Boolean value) {} public void clear() {}
                public void recentBoss(boolean enabled) { panel.recentBoss(enabled ? result : null,enabled,List.of()); render(); }
            };
            panel.recentBoss(result,true,List.of(result.summary)); render(); click("open-recent-boss");
            click("account-evidence"); assertNotNull(named("toggle-recent-boss")); click("account-back");
            assertNotNull(named("recent-boss-context"));
            click("account-evidence"); click("toggle-recent-boss"); click("account-back");
            assertTrue(children(panel).stream().noneMatch(c->"recent-boss-context".equals(c.getName())));
            assertTrue(children(panel).stream().noneMatch(c->"open-recent-boss".equals(c.getName())));
        });
    }
}
