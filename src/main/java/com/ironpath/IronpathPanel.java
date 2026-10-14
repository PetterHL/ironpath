package com.ironpath;

import com.ironpath.advisor.Advisor;
import com.ironpath.advisor.Advisor.Recommendation;
import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.advisor.PoweredMagic;
import com.ironpath.advisor.ComparisonResult;
import com.ironpath.advisor.PursuitOverview;
import com.ironpath.advisor.PursuitAssessment;
import com.ironpath.advisor.ActivityOpportunity;
import com.ironpath.advisor.EffortEstimate;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.model.Truth;
import com.ironpath.preferences.Preferences;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import net.runelite.client.game.ItemManager;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.LinkBrowser;

final class IronpathPanel extends PluginPanel
{
	private final ItemManager itemManager;
	private String detail;
	private boolean pathDetail, browse;
	private String category = "All", returnFocus;
	private String query = "", correctionKey;
	private JTextField searchField;
	private int searchDot, searchMark;
	private Anchor homeAnchor, browseAnchor, detailAnchor;
	private long view, browseResultsView;
	private Runnable refresh;
	private PursuitOverview renderedOverview;
	private com.ironpath.encounter.KillImpactResult latestRecent, displayedRecent;
	private com.ironpath.encounter.KillImpactResult.Option recentOption;
	private List<com.ironpath.encounter.EncounterSummary> recentSummaries=List.of();
	private boolean recentEnabled, recentView, recentPath;
	private Anchor recentHomeAnchor, recentListAnchor;
	private String recentReturnFocus;
	void recentBoss(com.ironpath.encounter.KillImpactResult latest, boolean enabled,
		List<com.ironpath.encounter.EncounterSummary> summaries)
	{
		latestRecent=latest; recentEnabled=enabled; recentSummaries=List.copyOf(summaries);
		if(!enabled || latest==null) { recentView=false; displayedRecent=null; recentOption=null; }
	}
	private final Map<String,String> facets=new java.util.LinkedHashMap<>();
	private boolean filtersExpanded, includeOwned, accountView, diagnosticsView, orderUpdated;
	private Anchor accountAnchor, diagnosticsAnchor;
	IronpathPanel() { this(null); }
	IronpathPanel(ItemManager itemManager) { this.itemManager = itemManager; }

	interface Actions
	{
		void goal(String id);
		void skip(String id, boolean skipped);
		void confirm(String key, Boolean value);
		void clear();
		default void reference(String key, String value) {}
		default void pace(String value) {}
		default void pace(String activity, String mode, String value) { pace(value); }
		default void pace(String activity, String mode, String methodKey, String value) { pace(activity,mode,value); }
		default void recentBoss(boolean enabled) {}
		default void clearRecentBoss() {}
		default boolean current() { return true; }
		default boolean canEdit() { return true; }
	}

	void render(String status, AccountSnapshot snapshot, List<Recommendation> advice, KnowledgeCatalog catalog,
		Advisor.GoalView goal, Preferences preferences, Actions actions, String storageStatus, List<String> reasons)
	{ render(status, snapshot, advice, catalog, goal, preferences, actions, storageStatus, reasons, List.of(), null); }

	void render(String status, AccountSnapshot snapshot, List<Recommendation> advice, KnowledgeCatalog catalog,
		Advisor.GoalView goal, Preferences preferences, Actions actions, String storageStatus, List<String> reasons,
		List<Advisor.UpgradeView> upgrades, com.ironpath.advisor.CombatSetup reference)
	{
		render(status, snapshot, advice, catalog, goal, preferences, actions, storageStatus, reasons,
			new PursuitOverview(upgrades, List.of(), List.of(), advice), reference);
	}

	void render(String status, AccountSnapshot snapshot, List<Recommendation> advice, KnowledgeCatalog catalog,
		Advisor.GoalView goal, Preferences preferences, Actions actions, String storageStatus, List<String> reasons,
		PursuitOverview overview, com.ironpath.advisor.CombatSetup reference)
	{
		List<Advisor.UpgradeView> upgrades = overview.all;
		if (!SwingUtilities.isEventDispatchThread()) { throw new IllegalStateException("Swing thread required"); }
		if (snapshot!=null && searchField!=null && query.equals(searchField.getText())) {
			searchDot=searchField.getCaret().getDot(); searchMark=searchField.getCaret().getMark();
		}
		searchField=null;
		javax.swing.JViewport liveViewport=(javax.swing.JViewport)SwingUtilities.getAncestorOfClass(javax.swing.JViewport.class,this);
		Anchor updateAnchor=snapshot!=null && renderedOverview!=null && overview!=renderedOverview && detail==null && !accountView ? captureAnchor(liveViewport) : null;
		boolean lostDetail=snapshot!=null && detail!=null && upgrades.stream().noneMatch(u -> u.upgrade.id.equals(detail))
			&& overview.activities.stream().noneMatch(a -> ("activity:" + a.id).equals(detail))
			&& overview.owned.stream().noneMatch(u->("owned:"+u.upgrade.id).equals(detail));
		if (lostDetail) { if(accountView) { accountAnchor=detailAnchor; } else { updateAnchor=detailAnchor; } }
		if (snapshot!=null && renderedOverview!=null && overview!=renderedOverview && browse
			&& !renderedOverview.all.stream().map(u->u.upgrade.id).collect(java.util.stream.Collectors.toList())
			.equals(overview.all.stream().map(u->u.upgrade.id).collect(java.util.stream.Collectors.toList()))) { orderUpdated=true; }
		renderedOverview=snapshot==null ? null : overview;
		long currentView = ++view;
		refresh = () -> render(status, snapshot, advice, catalog, goal, preferences, actions, storageStatus, reasons, overview, reference);
        if(snapshot==null) {
            recentHomeAnchor=recentListAnchor=null; recentReturnFocus=null; recentView=false; displayedRecent=null; recentOption=null; latestRecent=null;
            pathDetail=false; homeAnchor=browseAnchor=detailAnchor=accountAnchor=diagnosticsAnchor=null; browse=false; category="All"; query="";
            searchDot=searchMark=0; returnFocus=null; correctionKey=null; facets.clear(); filtersExpanded=includeOwned=accountView=diagnosticsView=orderUpdated=false;
        }
		if (snapshot == null || upgrades.stream().noneMatch(u -> u.upgrade.id.equals(detail))
			&& overview.activities.stream().noneMatch(a -> ("activity:" + a.id).equals(detail))
			&& overview.owned.stream().noneMatch(u->("owned:"+u.upgrade.id).equals(detail))) { detail = null; }
		removeAll(); setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); setBackground(ColorScheme.DARK_GRAY_COLOR);
		JPanel heading=new ActionLinks(); heading.setFont(getFont());
		JLabel brand=new JLabel("IronPath"); brand.setForeground(ColorScheme.TEXT_COLOR); brand.setFont(getFont().deriveFont(java.awt.Font.BOLD)); heading.add(brand);
		add(heading);
		if(snapshot!=null) {
			JPanel navigation=new ActionLinks(); navigation.setFont(getFont()); navigation.setName("main-navigation");
			JButton upgradesTab=button(navigation,"Upgrades",()->{
				if(accountView) { showAccount(false,currentView,actions); }
				else if(recentView) {
					if(!valid(currentView,actions)) { return; }
					recentView=false; recentOption=null; refresh.run();
				} else if(detail!=null) { navigate(null,currentView,actions); }
				else if(browse) { switchList(false,currentView,actions); }
			}); upgradesTab.setName("main-upgrades");
			JButton accountTab=button(navigation,"Account",()->showAccount(true,currentView,actions)); accountTab.setName("account-evidence");
			selectedTab(accountView?accountTab:upgradesTab); add(navigation);
			if(!accountView && detail==null && !recentView) {
				JPanel variants=new ActionLinks(); variants.setFont(getFont()); variants.setName("upgrade-variants");
				JButton recommended=button(variants,"Recommended",()->switchList(false,currentView,actions)); recommended.setName("recommended-view");
				JButton browseTab=button(variants,"Browse",()->showBrowse(currentView,actions)); browseTab.setName("home-browse");
				selectedTab(browse?browseTab:recommended); add(variants);
				if(!browse) {
					label(this,"Recommended order");
					text(this,"Value and remaining work for your account. Choices with unresolved checks stay conditional.");
				}
				if(!overview.checks.ownership.isEmpty()) {
					button(this,"Account checks ("+overview.checks.ownership.size()+")",()->showAccount(true,currentView,actions)).setName("shared-account-checks");
				}
			}
		} else { text(this,status); }

		if (status.startsWith("Pace needs")) { text(this, status); }
		if (snapshot != null && catalog != null)
		{
			com.ironpath.advisor.OwnedUpgrade selectedOwned=overview.owned.stream().filter(u->("owned:"+u.upgrade.id).equals(detail)).findFirst().orElse(null);
			Advisor.UpgradeView selected = upgrades.stream().filter(u -> u.upgrade.id.equals(detail)).findFirst().orElse(null);
			if (accountView) {
				if(diagnosticsView) {
					button(this,"Back to account",()->showDiagnostics(false,currentView,actions)).setName("diagnostics-back");
					label(this,"Diagnostics"); text(this,status); text(this,storageStatus);
					text(this,"Knowledge "+catalog.version+" · reviewed "+catalog.reviewed);
					observations(this,snapshot,catalog);
				} else {
					button(this,recentView ? "Back to recent boss" : "← Upgrades",()->showAccount(false,currentView,actions)).setName("account-back");
					account(this,status,snapshot,catalog,preferences,actions,storageStatus,goal);
				}
			}
			else if(recentView && displayedRecent!=null)
			{
				button(this,recentOption==null ? "← Upgrades" : "Back to recent boss",()->{
					if(!valid(currentView,actions)) { return; }
					javax.swing.JViewport viewport=(javax.swing.JViewport)SwingUtilities.getAncestorOfClass(javax.swing.JViewport.class,this);
					boolean toList=recentOption!=null;
					if(toList) { recentOption=null; recentPath=false; }
					else { recentView=false; }
					refresh.run();
					restoreNavigation(viewport,toList ? recentListAnchor : recentHomeAnchor,toList ? recentReturnFocus : "open-recent-boss",view,actions);
				});
				if(recentOption!=null && recentPath && recentOption.upgrade!=null)
				{ text(this,"Path from the captured account context"); acquisition(recentOption.upgrade,catalog,actions); }
				else
				{
					add(new RecentBossPanel(displayedRecent,recentOption,itemManager,catalog,getFont(),()->valid(currentView,actions),(option,path)->{
						if(!valid(currentView,actions)) { return; }
						javax.swing.JViewport viewport=(javax.swing.JViewport)SwingUtilities.getAncestorOfClass(javax.swing.JViewport.class,this);
						recentListAnchor=captureAnchor(viewport); recentReturnFocus=(path ? "recent-path-" : "recent-value-")+option.itemId;
						recentOption=option; recentPath=path; refresh.run();
						restoreNavigation(viewport,null,null,view,actions);
					}));
					if(recentOption!=null) { for(String gap:displayedRecent.gaps) { text(this,gap); } }
				}
			}
			else if (selectedOwned!=null) {
				button(this,"← Upgrades",()->navigate(null,currentView,actions)); ownedDetails(selectedOwned,catalog,actions);
			}
			else if (selected != null)
			{
				button(this, "← Upgrades", () -> navigate(null, currentView, actions));
				upgradeDetails(selected, snapshot, preferences, catalog, actions, reference);
			}
			else if (detail != null && detail.startsWith("activity:"))
			{
				button(this, "← Upgrades", () -> navigate(null, currentView, actions));
				overview.activities.stream().filter(a -> ("activity:" + a.id).equals(detail)).findFirst().ifPresent(a -> activityDetails(a, catalog, actions, currentView));
			}
			else
			{
				if (browse) { browse(overview, catalog, reference, currentView, actions); }
				else
				{
					if(latestRecent!=null)
					{
						button(this,"Recent boss · Commander Zilyana",()->{
							if(!valid(currentView,actions)) { return; }
							javax.swing.JViewport viewport=(javax.swing.JViewport)SwingUtilities.getAncestorOfClass(javax.swing.JViewport.class,this);
							recentHomeAnchor=captureAnchor(viewport);
							displayedRecent=latestRecent; recentView=true; recentOption=null; refresh.run();
							restoreNavigation(viewport,null,null,view,actions);
						}).setName("open-recent-boss");
					}
					JPanel discovery=new ActionLinks(); discovery.setFont(getFont());
					JButton longer=button(discovery,"Long-term ("+overview.longTerm.size()+")",()->showCategory("Long-term",currentView,actions)); longer.setName("home-long-term"); longer.setToolTipText("Long-term");
					JButton specialist=button(discovery,"Specialist ("+upgrades.stream().filter(u->u.valueEvidence.specialist).count()+")",()->showCategory("Specialist",currentView,actions)); specialist.setName("home-specialist"); specialist.setToolTipText("Specialist"); add(discovery);
					HomepageDeck deck=new HomepageDeck(); deck.setName("homepage-deck");
					for (Advisor.UpgradeView upgrade : overview.preview)
					{ upgradeCard(deck,upgrade,reference,currentView,actions,false); }
					add(deck);

					if (upgrades.isEmpty()) { text(this, "No remaining supported upgrade is established. Open the bank to check owned alternatives, or browse coverage."); }
					else if (overview.preview.isEmpty()) { text(this, "No worthwhile new pursuit is established in this reference. Browse retains small-gain and optional specialist alternatives."); }
					button(this, "Browse all (" + upgrades.size() + ")", () -> showBrowse(currentView, actions)).setName("home-browse-all");
					overview.activities.stream().filter(a -> a.category.equals("Worth doing")).findFirst().ifPresent(a -> activityCard(a,currentView,actions));
					if (!overview.quickActions.isEmpty())
					{
						JPanel quickPanel = section(this, "Quick actions");
						for (Recommendation recommendation : overview.quickActions) { card(quickPanel, recommendation, catalog, actions); }
					}
				}
				JPanel broad = section(this, "Existing broad PvM advice");
				text(broad, "Reviewed activity/preparation advice. These cases have no calculated upgrade benefit.");
				for (Recommendation recommendation : overview.broadAdvice) { card(broad, recommendation, catalog, actions); }
				for (String reason : reasons) { text(broad, reason); }

			}
		}
		if (snapshot==null) { text(this,"Open the bank after login to improve account evidence."); }
		revalidate(); repaint();
		if (updateAnchor!=null && liveViewport!=null)
		{ restoreNavigation(liveViewport,updateAnchor,null,currentView,actions); }
	}

	private void navigate(String id, long expectedView, Actions actions)
	{ navigate(id,false,expectedView,actions); }
	private void navigate(String id, boolean path, long expectedView, Actions actions)
	{
		if (view != expectedView || (actions != null && !actions.current())) { return; }
		javax.swing.JViewport viewport = (javax.swing.JViewport) SwingUtilities.getAncestorOfClass(javax.swing.JViewport.class, this);
		if (detail == null && id != null) { detailAnchor = captureAnchor(viewport); }
		if (id != null) { returnFocus = id.startsWith("activity:") ? "open-" + id : (path ? "path-upgrade-" : "open-upgrade-") + id; }
		detail = id; pathDetail = path; refresh.run(); long returnedView = view;
		Anchor destination = id == null ? detailAnchor : null;
		restoreNavigation(viewport, destination, id == null ? returnFocus : null, returnedView, actions);
	}
	private void showAccount(boolean open,long expectedView,Actions actions)
	{
		if (!valid(expectedView,actions)||accountView==open) { return; }
		javax.swing.JViewport viewport=(javax.swing.JViewport)SwingUtilities.getAncestorOfClass(javax.swing.JViewport.class,this);
		if (open) { accountAnchor=captureAnchor(viewport); }
		accountView=open; diagnosticsView=false; refresh.run();
		restoreNavigation(viewport,open ? null : accountAnchor,open ? "account-back" : null,view,actions);
	}
	private void showDiagnostics(boolean open,long expectedView,Actions actions)
	{
		if(!valid(expectedView,actions)) { return; }
		javax.swing.JViewport viewport=(javax.swing.JViewport)SwingUtilities.getAncestorOfClass(javax.swing.JViewport.class,this);
		if(open) { diagnosticsAnchor=captureAnchor(viewport); }
		diagnosticsView=open; refresh.run();
		restoreNavigation(viewport,open?null:diagnosticsAnchor,open?"diagnostics-back":"open-diagnostics",view,actions);
	}

	private void account(JPanel parent,String status,AccountSnapshot snapshot,KnowledgeCatalog catalog,Preferences preferences,Actions actions,String storageStatus,Advisor.GoalView goal)
	{
		long expectedView=view;
		label(parent,"Your account");
		text(parent,"Attack "+level(snapshot,"ATTACK")+" · Strength "+level(snapshot,"STRENGTH")+" · Defence "+level(snapshot,"DEFENCE"));
		text(parent,"Ranged "+level(snapshot,"RANGED")+" · Magic "+level(snapshot,"MAGIC")+" · Prayer "+level(snapshot,"PRAYER"));
		text(parent,"Rigour: "+unlock(snapshot.rigour)+" · Augury: "+unlock(snapshot.augury));
		text(parent,snapshot.bank.known()?"Bank recorded this session; reopen it after changes.":"Bank not read this session. Open it to check owned alternatives.");
		JPanel gear=section(parent,"Recorded gear");
		if(snapshot.equipmentSlots.known()) {
			for(String slot:PoweredMagic.SLOTS) {
				Integer id=snapshot.equipmentSlots.value().get(slot); if(id==null || id<=0) { continue; }
				text(gear,slot+": "+gearName(catalog,id));
			}
		} else { text(gear,"Worn slots not recorded; observed possessions do not establish the current loadout."); }
		catalog.itemNames().entrySet().stream().sorted(Map.Entry.comparingByValue()).filter(g->snapshot.observedItem(g.getKey())==Truth.TRUE)
			.forEach(g->text(gear,"Observed: "+g.getValue()));
		text(gear,"Charges, loaded ammunition and manual ownership remain separate checks.");
		JPanel unlocks=section(parent,"Recorded unlocks");
		snapshot.facts.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e->{
			String name=catalog.factChoices().get(e.getKey()); if(name!=null) { text(unlocks,name+": "+unlock(e.getValue())); }
		});
		if(renderedOverview!=null && !renderedOverview.checks.ownership.isEmpty()) {
			JPanel checks=section(parent,"Checks affecting advice"); checks.setName("account-checks");
			text(checks,"Open the bank to check owned alternatives. Each planning correction below applies to all affected upgrades.");
			for(com.ironpath.advisor.AccountChecks.Check check:renderedOverview.checks.ownership) {
				String name=catalog.factChoices().get(check.key); if(name==null) { continue; }
				button(checks,name+" · "+check.outcomes.size()+" upgrade"+(check.outcomes.size()==1?"":"s"),()->{
					if(!valid(expectedView,actions)) { return; }
					correctionKey=check.key; refresh.run(); long correctionView=view;
					SwingUtilities.invokeLater(()->{
						if(!accountView || !check.key.equals(correctionKey) || !valid(correctionView,actions)) { return; }
						focusAccountFact(this);
					});
				}).setName("account-check-"+check.key);
			}
		}
		if(actions!=null && actions.canEdit()) { controls(parent,catalog,preferences,snapshot,actions); }
		if(goal!=null) { JPanel chosen=section(parent,"Selected goal"); goal(chosen,goal,catalog,actions); }
		JPanel experimental=section(parent,"Recent boss (experimental)"); recentControls(experimental,actions);
		text(parent,"Planning choices are saved locally; observed gear and reference setups stay in session memory.");
		if(ChronoUnit.DAYS.between(catalog.reviewed,LocalDate.now())>90) { text(parent,"Knowledge review is over 90 days old; check sources before pursuing."); }
		button(parent,"Diagnostics",()->showDiagnostics(true,expectedView,actions)).setName("open-diagnostics");
	}
	private String gearName(KnowledgeCatalog catalog,int id)
	{
		return catalog.itemNames().getOrDefault(id,"Unreviewed item");
	}

	private void recentControls(JPanel parent,Actions actions)
	{
		long currentView=view;
		if(actions==null) { return; }
		button(parent,recentEnabled ? "Disable recent boss (experimental)" : "Enable recent boss (experimental)",()->{
			if(valid(currentView,actions)) { actions.recentBoss(!recentEnabled); }
		}).setName("toggle-recent-boss");
		if(recentEnabled)
		{
			text(parent,"Zilyana pilot · session memory only. Wait for the next observed spawn; live-client validation pending.");
			String counts=java.util.Arrays.stream(com.ironpath.encounter.EncounterSummary.Outcome.values()).map(o->
				o.name().toLowerCase(java.util.Locale.ROOT)+" "+recentSummaries.stream().filter(k->k.outcome==o).count())
				.collect(java.util.stream.Collectors.joining(" · "));
			text(parent,counts);
			button(parent,"Clear recent kills",()->{ if(valid(currentView,actions)) { actions.clearRecentBoss(); } });
		}
	}

	private void restoreNavigation(javax.swing.JViewport viewport, Anchor anchor, String focus, long expectedView, Actions actions)
	{
		long expectedResults=browseResultsView;
		SwingUtilities.invokeLater(() -> {
			if (!valid(expectedView, actions) || browse && detail==null && browseResultsView!=expectedResults) { return; }
			if (viewport != null) {
				if (anchor == null) { viewport.setViewPosition(new java.awt.Point()); }
				else { restoreAnchor(viewport, anchor); }
			}
			if (focus != null) { returnFocus = focus; restoreFocus(this); }
		});
	}

	private static final class Anchor
	{
		final String id, focus; final int offset, position;
		Anchor(String id,int offset,int position,String focus) { this.id=id; this.offset=offset; this.position=position; this.focus=focus; }
	}
	private Anchor captureAnchor(javax.swing.JViewport viewport)
	{
		if (viewport==null) { return null; }
		int position=viewport.getViewPosition().y; java.awt.Component selected=null;
		for (java.awt.Component card : namedCards(this))
		{
			if (!card.isVisible()) { continue; }
			int y=SwingUtilities.convertPoint(card,0,0,this).y;
			if (selected==null || y<=position) { selected=card; } else { break; }
		}
		java.awt.Component focused=java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		String focus=focused!=null && SwingUtilities.isDescendingFrom(focused,this) ? focused.getName() : null;
		return new Anchor(selected==null ? null : selected.getName(),selected==null ? 0 : position-SwingUtilities.convertPoint(selected,0,0,this).y,position,focus);
	}
	private java.util.List<java.awt.Component> namedCards(java.awt.Container parent)
	{
		java.util.List<java.awt.Component> cards=new java.util.ArrayList<>();
		for (java.awt.Component child : parent.getComponents())
		{
			if (child.getName()!=null && (child.getName().startsWith("upgrade-card-") || child.getName().startsWith("recent-option-"))) { cards.add(child); }
			else if (child instanceof java.awt.Container) { cards.addAll(namedCards((java.awt.Container)child)); }
		}
		return cards;
	}
	private void restoreAnchor(javax.swing.JViewport viewport,Anchor anchor)
	{
		viewport.doLayout(); doLayout();
		java.awt.Component card=namedCards(this).stream().filter(c->c.isVisible() && c.getName().equals(anchor.id)).findFirst().orElse(null);
		int y=card==null ? anchor.position : SwingUtilities.convertPoint(card,0,0,this).y+anchor.offset;
		viewport.setViewPosition(new java.awt.Point(0,Math.max(0,Math.min(y,getHeight()-viewport.getExtentSize().height))));
		if (anchor.focus!=null) { returnFocus=anchor.focus; restoreFocus(this); }
	}

	private void observations(JPanel parent, AccountSnapshot snapshot, KnowledgeCatalog catalog)
	{
		text(parent, "Real levels: Prayer " + level(snapshot, "PRAYER") + ", Defence " + level(snapshot, "DEFENCE")
			+ ", Ranged " + level(snapshot, "RANGED") + ", Magic " + level(snapshot, "MAGIC") + ". " + snapshot.levels.evidence());
		text(parent, "Rigour: " + unlock(snapshot.rigour) + "; Augury: " + unlock(snapshot.augury) + ". Client varbits.");
		snapshot.facts.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e ->
			text(parent, catalog.factChoices().get(e.getKey()) + ": " + unlock(e.getValue()) + ". " + e.getValue().evidence()));
		text(parent, "Inventory: " + snapshot.inventory.evidence() + "\nEquipment: " + snapshot.equipment.evidence());
		text(parent, "Bank: " + snapshot.bank.evidence());
		text(parent, "Not observed is not global absence. Manual current-task/access assumptions must be rechecked before play; their timestamps are planning provenance.");
	}

	private void showBrowse(long expectedView, Actions actions)
	{
		if (!valid(expectedView, actions)) { return; }
		switchList(true, expectedView, actions);
	}
	private void showCategory(String selection,long expectedView,Actions actions)
	{
		if (!valid(expectedView,actions)) { return; }
		category=selection; query=""; facets.clear(); includeOwned=false; browseAnchor=null; switchList(true,expectedView,actions);
	}
	private void switchList(boolean toBrowse, long expectedView, Actions actions)
	{
		if (!valid(expectedView, actions)) { return; }
		javax.swing.JViewport viewport=(javax.swing.JViewport)SwingUtilities.getAncestorOfClass(javax.swing.JViewport.class,this);
		if (browse) { browseAnchor=captureAnchor(viewport); } else { homeAnchor=captureAnchor(viewport); }
		browse=toBrowse; if(!toBrowse) { orderUpdated=false; } refresh.run();
		Anchor destination=toBrowse ? browseAnchor : homeAnchor;
		restoreNavigation(viewport,destination,destination==null ? (toBrowse ? "browse-search" : "home-browse") : destination.focus,view,actions);
	}
	private void refreshBrowse(String focus, Actions actions)
	{
		browseAnchor=null; refresh.run();
		javax.swing.JViewport viewport=(javax.swing.JViewport)SwingUtilities.getAncestorOfClass(javax.swing.JViewport.class,this);
		restoreNavigation(viewport,null,focus,view,actions);
	}
	private boolean valid(long expectedView, Actions actions) { return view == expectedView && (actions == null || actions.current()); }
	private void restoreFocus(java.awt.Container parent)
	{
		for (java.awt.Component component : parent.getComponents())
		{
			if (returnFocus != null && returnFocus.equals(component.getName())) { component.requestFocusInWindow(); return; }
			if (component instanceof java.awt.Container) { restoreFocus((java.awt.Container) component); }
		}
	}
	private void browse(PursuitOverview overview, KnowledgeCatalog catalog, com.ironpath.advisor.CombatSetup reference, long expectedView, Actions actions)
	{

		if (orderUpdated) { text(this,"Upgrade list updated · reading position kept"); getComponent(getComponentCount()-1).setName("browse-update"); }
		text(this,"Search upgrades");
		JTextField search=new JTextField(query); search.setName("browse-search"); search.setFont(getFont());
		search.setCaretPosition(Math.min(searchMark,query.length())); search.moveCaretPosition(Math.min(searchDot,query.length())); searchField=search;
		search.setForeground(ColorScheme.TEXT_COLOR); search.setBackground(ColorScheme.DARKER_GRAY_COLOR); search.setCaretColor(ColorScheme.TEXT_COLOR);
		search.setAlignmentX(LEFT_ALIGNMENT); search.setMaximumSize(new Dimension(Integer.MAX_VALUE,search.getPreferredSize().height));
		search.getAccessibleContext().setAccessibleName("Search upgrades by item name"); add(search);
		JPanel results=new JPanel(); results.setLayout(new BoxLayout(results,BoxLayout.Y_AXIS)); results.setOpaque(false); results.setFont(getFont()); results.setAlignmentX(LEFT_ALIGNMENT);
		search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			private void changed() {
				if (!valid(expectedView,actions)) { return; }
				query=search.getText(); browseAnchor=null;
				browseResults(results,overview,catalog,reference,expectedView,actions); revalidate(); repaint();
				javax.swing.JViewport viewport=(javax.swing.JViewport)SwingUtilities.getAncestorOfClass(javax.swing.JViewport.class,IronpathPanel.this);
				if (viewport!=null) { viewport.setViewPosition(new java.awt.Point()); }
			}
			public void insertUpdate(javax.swing.event.DocumentEvent e) { changed(); }
			public void removeUpdate(javax.swing.event.DocumentEvent e) { changed(); }
			public void changedUpdate(javax.swing.event.DocumentEvent e) { changed(); }
		});
		JComboBox<String> filter = new JComboBox<>(new String[]{"All", "Pursue", "Long-term", "Specialist", "Check", "Owned / covered"});
		filter.setName("browse-category"); filter.setSelectedItem(category); filter.setAlignmentX(LEFT_ALIGNMENT);
		filter.getAccessibleContext().setAccessibleName("Browse recommendation category");
		filter.setFont(getFont()); filter.setMaximumSize(new Dimension(Integer.MAX_VALUE,filter.getPreferredSize().height));
		filter.addActionListener(event -> { if (valid(expectedView, actions)) { category = (String) filter.getSelectedItem(); refreshBrowse("browse-category",actions); } }); add(filter);
		browseFacets(overview,catalog,expectedView,actions);
		browseResults(results,overview,catalog,reference,expectedView,actions); add(results);
		if (!overview.activities.isEmpty())
		{
			JPanel activities=section(this, "Shared activities");
			for (ActivityOpportunity activity : overview.activities)
			{
				JButton open=button(activities, activity.title + " · " + activity.category, () -> navigate("activity:" + activity.id, expectedView, actions));
				open.setName("open-activity:" + activity.id);
			}
		}
	}
	private void browseResults(JPanel parent,PursuitOverview overview,KnowledgeCatalog catalog,com.ironpath.advisor.CombatSetup reference,long expectedView,Actions actions)
	{
		parent.removeAll(); ++browseResultsView;
		if (!category.equals("All") || !query.isEmpty() || includeOwned || facets.values().stream().anyMatch(v->!v.isEmpty())) {
			button(parent,"Clear filters",()->{ if (valid(expectedView,actions)) { category="All"; query=""; facets.clear(); includeOwned=false; refreshBrowse("browse-search",actions); } }).setName("browse-clear");
		}
		List<Advisor.UpgradeView> visible = overview.all.stream().filter(u -> category.equals("All")
			|| category.equals("Pursue") && u.pursuit.category == PursuitAssessment.Category.PURSUE
			|| category.equals("Long-term") && (u.pursuit.category == PursuitAssessment.Category.LONG_TERM || u.pursuit.tail == PursuitAssessment.Tail.LONG)
			|| category.equals("Specialist") && u.valueEvidence.specialist
			|| category.equals("Check") && (u.pursuit.category == PursuitAssessment.Category.CHECK || u.pursuit.category == PursuitAssessment.Category.PLAN))
			.filter(u -> matchesSearch(u.upgrade.title,query) && matchesFacets(catalog,u.upgrade)
				&& (facet("importance").isEmpty()||u.valueEvidence.importance.label.equals(facet("importance")))
				&& (facet("status").isEmpty()||u.pursuit.category.label.equals(facet("status"))))
			.collect(java.util.stream.Collectors.toList());
		List<com.ironpath.advisor.OwnedUpgrade> owned=includeOwned && category.equals("All") || category.equals("Owned / covered")
			? overview.owned.stream().filter(u->matchesSearch(u.upgrade.title,query)&&matchesFacets(catalog,u.upgrade)
				&& facet("importance").isEmpty() && (facet("status").isEmpty()||facet("status").equals("Owned / covered"))).collect(java.util.stream.Collectors.toList()) : List.of();
		text(parent, (visible.size()+owned.size()) + " upgrades · " + category + (!owned.isEmpty() ? " · "+owned.size()+" owned / covered" : ""));
		parent.getComponent(parent.getComponentCount()-1).setName("browse-count");
		BrowseGrid grid = new BrowseGrid(); grid.setName("browse-grid");
		for (Advisor.UpgradeView upgrade : visible) { upgradeCard(grid, upgrade, reference, expectedView, actions, true); } parent.add(grid);
		for (com.ironpath.advisor.OwnedUpgrade upgrade : owned) { ownedCard(grid,upgrade,expectedView,actions); }
		if (visible.isEmpty() && owned.isEmpty()) { text(parent, (!query.trim().isEmpty() || facets.values().stream().anyMatch(v->!v.isEmpty())) && (!overview.all.isEmpty()||!overview.owned.isEmpty())
			? "No matching upgrades in this category. Clear filters to see all supported upgrades."
			: category.equals("Specialist")
			? "No remaining reviewed specialist upgrade is established from observed gear. Owned outcomes, skipped routes or unconfirmed method inputs can leave this filter empty. All shows other supported upgrades."
			: overview.all.isEmpty() ? "No remaining supported upgrade from these observations. Unknown is not absence; no priorities are added to fill the list." : "No supported outcome in this category. All retains conditional, specialist and small-gain choices."); }
	}
	private String facet(String key) { return facets.getOrDefault(key,""); }
	private boolean matchesFacets(KnowledgeCatalog catalog,KnowledgeCatalog.Upgrade upgrade)
	{ return new com.ironpath.advisor.BrowseMetadata(catalog,upgrade).matches(facet("style"),facet("slot"),facet("activity"),facet("role")); }
	private void browseFacets(PursuitOverview overview,KnowledgeCatalog catalog,long expectedView,Actions actions)
	{
		JPanel options=section(this,"More filters"); options.setVisible(filtersExpanded);
		JButton toggle=(JButton)getComponent(getComponentCount()-2); toggle.setName("browse-filters");
		for (java.awt.event.ActionListener listener:toggle.getActionListeners()) { toggle.removeActionListener(listener); }
		toggle.addActionListener(e->{ if(valid(expectedView,actions)) { filtersExpanded=!filtersExpanded; options.setVisible(filtersExpanded); revalidate(); } });
		Map<String,java.util.Set<String>> values=new java.util.LinkedHashMap<>();
		for (String key:List.of("style","slot","activity","role","importance","status")) { values.put(key,new java.util.TreeSet<>()); }
		java.util.List<KnowledgeCatalog.Upgrade> outcomes=new java.util.ArrayList<>(); overview.all.forEach(u->outcomes.add(u.upgrade)); overview.owned.forEach(u->outcomes.add(u.upgrade));
		for (KnowledgeCatalog.Upgrade upgrade:outcomes) {
			com.ironpath.advisor.BrowseMetadata meta=new com.ironpath.advisor.BrowseMetadata(catalog,upgrade);
			values.get("style").addAll(meta.styles); values.get("slot").addAll(meta.slots); values.get("activity").addAll(meta.activities); values.get("role").addAll(meta.roles);
		}
		overview.all.forEach(u->{values.get("importance").add(u.valueEvidence.importance.label); values.get("status").add(u.pursuit.category.label);});
		if(!overview.owned.isEmpty()) { values.get("status").add("Owned / covered"); }
		values.forEach((key,choices)->{
			Map<String,String> labels=new java.util.LinkedHashMap<>(); labels.put("","Any "+key);
			choices.forEach(v->labels.put(v,key.equals("activity") ? catalog.activities.getOrDefault(v,v) : v.replace('-',' ')));
			if(!facet(key).isEmpty()&&!labels.containsKey(facet(key))) { labels.put(facet(key),facet(key).replace('-',' ')); }
			JComboBox<Choice> choice=choices(labels,false); select(choice,facet(key)); choice.setName("browse-"+key); choice.setFont(getFont());
			choice.setMaximumSize(new Dimension(Integer.MAX_VALUE,choice.getPreferredSize().height)); choice.getAccessibleContext().setAccessibleName("Filter upgrades by "+key);
			text(options,Character.toUpperCase(key.charAt(0))+key.substring(1)); options.add(choice);
			String selectedLabel=labels.get(((Choice)choice.getSelectedItem()).id);
			if (choice.getFontMetrics(getFont()).stringWidth(selectedLabel)>180) { text(options,selectedLabel); }
			choice.addActionListener(e->{if(valid(expectedView,actions)){facets.put(key,((Choice)choice.getSelectedItem()).id);refreshBrowse("browse-"+key,actions);}});
		});
		javax.swing.JCheckBox owned=new javax.swing.JCheckBox("Include owned in All",includeOwned); owned.setFont(getFont()); owned.setOpaque(false); owned.setForeground(ColorScheme.TEXT_COLOR); owned.setName("browse-owned"); options.add(owned);
		owned.getAccessibleContext().setAccessibleName("Include owned or covered outcomes in All results");
		if (owned.getFontMetrics(getFont()).stringWidth("Include owned in All")>185) { owned.setText("<html><div style='width:150px'>Include owned in All</div></html>"); }
		owned.addActionListener(e->{if(valid(expectedView,actions)){includeOwned=owned.isSelected();refreshBrowse("browse-owned",actions);}});
		text(options,"Filters describe reviewed uses, not readiness.");
	}
	private static boolean matchesSearch(String title,String query)
	{
		String name=title.toLowerCase(java.util.Locale.ROOT);
		for (String word:query.trim().toLowerCase(java.util.Locale.ROOT).split("\\s+")) {
			if (!name.contains(word)) { return false; }
		}
		return true;
	}
	private void upgradeCard(JPanel parent, Advisor.UpgradeView upgrade, com.ironpath.advisor.CombatSetup setup,
		long expectedView, Actions actions, boolean tile)
	{
		long expectedResults=browseResultsView;
		java.util.function.BooleanSupplier current=()->valid(expectedView,actions) && (!tile || browseResultsView==expectedResults);
		com.ironpath.advisor.UpgradeCard copy=upgrade.card;
		UpgradeTile card=new UpgradeTile(upgrade.upgrade.id,upgrade.upgrade.title,getFont(),()->{
			if(current.getAsBoolean()) { navigate(upgrade.upgrade.id,false,expectedView,actions); }
		});
		itemHeader(card.body,upgrade.upgrade,current);
		text(card.body,copy.metric); JTextArea result=(JTextArea)card.body.getComponent(card.body.getComponentCount()-1);
		result.setName("comparison-headline"); result.setFont(getFont().deriveFont(java.awt.Font.BOLD,getFont().getSize2D()+3));
		result.setForeground(copy.delta==null || copy.delta==0 ? ColorScheme.TEXT_COLOR : copy.delta>0 ? new java.awt.Color(137,218,156) : new java.awt.Color(255,160,160));
		if(!copy.context.isEmpty()) { text(card.body,copy.context); card.body.getComponent(card.body.getComponentCount()-1).setName("card-context"); }
		text(card.body,"Compared with "+copy.baseline); card.body.getComponent(card.body.getComponentCount()-1).setName("card-baseline");
		text(card.body,copy.presentation.priorityExplanation.cardReason); card.body.getComponent(card.body.getComponentCount()-1).setName("card-priority-reason");
		if(copy.presentation.primaryMetric.availability==com.ironpath.advisor.UpgradePresentation.Availability.INPUT_REQUIRED) { text(card.body,copy.status); card.body.getComponent(card.body.getComponentCount()-1).setName("card-status"); }
		card.finish(); parent.add(card);
	}

	private void itemHeader(JPanel parent,KnowledgeCatalog.Upgrade upgrade,java.util.function.BooleanSupplier current)
	{
		JPanel header=new JPanel(new CardHeaderLayout()); header.setFont(getFont()); header.setOpaque(false); header.setAlignmentX(LEFT_ALIGNMENT);
		JLabel icon=new JLabel("◇")
		{
			@Override public void setIcon(javax.swing.Icon image) { if (current.getAsBoolean()) { super.setIcon(image); setText(image==null ? "◇" : ""); if(image!=null) { getAccessibleContext().setAccessibleName(upgrade.title+" item image"); } } }
		};
		icon.setPreferredSize(new Dimension(40,40)); icon.setForeground(ColorScheme.TEXT_COLOR); icon.setName("sprite-"+upgrade.id);
		icon.getAccessibleContext().setAccessibleName(upgrade.title+" item image; placeholder while loading");
		if (itemManager!=null) { itemManager.getImage(upgrade.itemId).addTo(icon); }
		header.add(icon); text(header,upgrade.title);
		JTextArea title=(JTextArea)header.getComponent(1); title.setFocusable(false); title.setName("card-title"); title.setForeground(new java.awt.Color(244,213,141)); title.setFont(getFont().deriveFont(java.awt.Font.BOLD));
		parent.add(header);
	}
	private void ownedCard(JPanel parent,com.ironpath.advisor.OwnedUpgrade owned,long expectedView,Actions actions)
	{
		long expectedResults=browseResultsView;
		java.util.function.BooleanSupplier current=()->valid(expectedView,actions)&&browseResultsView==expectedResults;
		String id="owned:"+owned.upgrade.id;
		UpgradeTile card=new UpgradeTile(id,owned.upgrade.title,getFont(),()->{if(current.getAsBoolean()){navigate(id,expectedView,actions);}});
		itemHeader(card.body,owned.upgrade,current);
		text(card.body,"Owned / covered");
		text(card.body,"Inspect uses and usability");
		card.finish(); parent.add(card);
	}

	private void ownedDetails(com.ironpath.advisor.OwnedUpgrade owned,KnowledgeCatalog catalog,Actions actions)
	{
		long expectedView=view; itemHeader(this,owned.upgrade,()->valid(expectedView,actions));
		text(this,"Owned / covered · acquisition is not recommended");
		text(this,"Possession or an adequate alternative covers this outcome. It does not establish charges, equipment compatibility or current usability.");
		text(this,"Personal benefit is not quantified in this owned-use view. Acquisition importance does not apply.");
		for(String role:owned.roles){text(this,role);}
		for(String application:owned.applications){text(this,application);}
		text(this,"Limits: "+owned.upgrade.limitation);
		JPanel evidence=section(this,"Ownership evidence and sources");text(evidence,owned.evidence);
		sources(evidence,owned.sources,catalog);
	}


	private void activityCard(ActivityOpportunity activity, long expectedView, Actions actions)
	{
		text(this, activity.title); text(this, "Useful families: " + String.join(" · ", activity.usefulFamilies));
		activitySprites(this, activity, expectedView, actions);
		JButton open=button(this, activity.usefulFamilies.size()>1 ? "Inspect shared rewards ›" : "Inspect useful reward ›", () -> navigate("activity:" + activity.id, expectedView, actions)); open.setName("open-activity:" + activity.id);
		open.getAccessibleContext().setAccessibleName("Open " + activity.title + " shared rewards");
	}
	private void activityDetails(ActivityOpportunity activity, KnowledgeCatalog catalog, Actions actions, long expectedView)
	{
		text(this, activity.title + " · " + activity.category); text(this, activity.reason);
		text(this,"Activity access: " + activity.access + " · method review: " + activity.method + ". Neither certifies success.");
		for (String preparation : activity.preparation) { text(this,preparation); }
		activitySprites(this, activity, expectedView, actions);
		text(this, "Mode: " + activity.mode + ". Time not estimated: personal reward mechanics and cycle pace are unsupported.");
		for (String reward : activity.usefulRewards) { text(this, "Useful missing reward: " + reward); }
		for (String reward : activity.uncertainRewards) { text(this, "Check: " + reward); }
		for (String exclusion : activity.exclusions) { text(this, exclusion); }
		if (!activity.available) { text(this, "Check activity preparation/access in the individual Path before pursuing. This view does not certify readiness."); }
		if (!activity.allocations.isEmpty()) { text(this, "Exclusive resource choices: " + String.join(", ", activity.allocations)); }
		if (actions != null && actions.canEdit()) { button(this, "Skip this activity", () -> { if (valid(expectedView, actions)) { actions.skip(activity.activity,true); } }); }
		if (!activity.sources.isEmpty()) { sources(this,activity.sources,catalog); }
	}
	private void activitySprites(JPanel parent, ActivityOpportunity activity, long expectedView, Actions actions)
	{
		JPanel strip=new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,4,0)); strip.setOpaque(false);
		strip.setAlignmentX(LEFT_ALIGNMENT); strip.setMaximumSize(new Dimension(213,40)); strip.setPreferredSize(new Dimension(213,40));
		for (int id : activity.itemIds.stream().limit(4).collect(java.util.stream.Collectors.toList()))
		{
			JLabel icon=new JLabel("◇") { @Override public void setIcon(javax.swing.Icon image) { if(valid(expectedView,actions)) { super.setIcon(image); } } }; icon.setForeground(ColorScheme.TEXT_COLOR); icon.setPreferredSize(new Dimension(40,40));
			icon.getAccessibleContext().setAccessibleName("Representative useful reward; item " + id);
			if (itemManager != null) { itemManager.getImage(id).addTo(icon); } strip.add(icon);
		}
		parent.add(strip);
	}

	private void upgradeDetails(Advisor.UpgradeView view, AccountSnapshot a, Preferences p, KnowledgeCatalog catalog,
		Actions actions, com.ironpath.advisor.CombatSetup setup)
	{
		KnowledgeCatalog.Upgrade upgrade = view.upgrade;
		long detailView=this.view;
		itemHeader(this,upgrade,()->valid(detailView,actions));


		long expectedView = this.view;
		JPanel tabs=new ActionLinks(); tabs.setFont(getFont()); tabs.setName("detail-tabs");
		JButton valueTab=button(tabs,"Impact",()->changeDetailTab(false,expectedView,actions));
		JButton pathTab=button(tabs,"Acquisition",()->changeDetailTab(true,expectedView,actions));
		valueTab.setName("detail-value-tab"); pathTab.setName("detail-path-tab");
		selectedTab(pathDetail?pathTab:valueTab); add(tabs);
		if (pathDetail) { acquisition(view, catalog, actions); return; }
		com.ironpath.advisor.UpgradePresentation presentation=view.presentation;
		comparison(this,presentation,()->valid(expectedView,actions));
		label(this,"Why this position"); text(this,presentation.priorityReason);

		JPanel evidence=section(this,"Evidence and calculation basis"); evidence.setName("value-evidence");
		ComparisonResult comparison=view.comparison;
		setup=comparison.beforeSetup==null?setup:comparison.beforeSetup;
		calculationBasis(evidence,comparison);
		for(com.ironpath.advisor.ValueEvidence.Role role:view.valueEvidence.roles) {
			text(evidence,role.reason);
			for(String alternative:role.alternatives) { text(evidence,"Adequate owned alternative: "+alternative); }
			text(evidence,role.limitation);
		}
		for(String trail:view.indirectReasons) { text(evidence,trail); }
		text(evidence,"Alternative: "+upgrade.alternatives); text(evidence,"Limits: "+upgrade.limitation);
		if(!upgrade.comparison.equals("NONE") && setup!=null) {
			JPanel assumptions=section(evidence,"Reference setup and assumptions"); assumptions.setName("reference-corrections");
			for(String assumption:comparison.assumptions) { text(assumptions,assumption); }
			if(actions!=null && actions.canEdit()) { referenceControls(assumptions,a,setup,actions,view.referenceChoices,comparison.before!=null); }
			if(presentation.primaryMetric.availability==com.ironpath.advisor.UpgradePresentation.Availability.INPUT_REQUIRED) {
				button(this,presentation.primaryMetric.action,()->{if(valid(expectedView,actions)){ evidence.setVisible(true); assumptions.setVisible(true); revalidate(); }}).setName("correct-comparison-input");
			}
		}
		for(ComparisonResult contextual:view.contextComparisons) {
			text(evidence,contextual.target.name+": "+contextual.summary());
			for(String assumption:contextual.assumptions) { text(evidence,assumption); }
		}
		for(String application:view.applications) { text(evidence,application); }
		sources(evidence,view.evidenceSources,catalog);
		button(this,"Account corrections",()->showAccount(true,expectedView,actions)).setName("detail-account-corrections");
	}

	private void changeDetailTab(boolean path,long expectedView,Actions actions)
	{
		if(!valid(expectedView,actions)) { return; }
		javax.swing.JViewport viewport=(javax.swing.JViewport)SwingUtilities.getAncestorOfClass(javax.swing.JViewport.class,this);
		pathDetail=path; refresh.run(); restoreNavigation(viewport,null,path?"detail-path-tab":"detail-value-tab",view,actions);
	}

	private void focusAccountFact(java.awt.Container parent)
	{
		for(java.awt.Component component:parent.getComponents()) {
			if("account-fact".equals(component.getName())) {
				component.requestFocusInWindow();
				if(component instanceof javax.swing.JComponent) { ((javax.swing.JComponent)component).scrollRectToVisible(new java.awt.Rectangle(component.getSize())); }
				return;
			}
			if(component instanceof java.awt.Container) { focusAccountFact((java.awt.Container)component); }
		}
	}

	private void selectedTab(JButton button)
	{
		button.setContentAreaFilled(true); button.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
		button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new java.awt.Color(244,213,141)),BorderFactory.createEmptyBorder(2,3,2,3)));
		button.getAccessibleContext().setAccessibleDescription("Selected view");
	}

	private void label(JPanel parent,String message)
	{
		text(parent,message); JTextArea label=(JTextArea)parent.getComponent(parent.getComponentCount()-1);
		label.setFont(parent.getFont().deriveFont(java.awt.Font.BOLD)); label.setForeground(new java.awt.Color(244,213,141));
	}
	private void comparison(JPanel parent,com.ironpath.advisor.UpgradePresentation presentation,java.util.function.BooleanSupplier current)
	{
		if(presentation.primaryMetric.availability==com.ironpath.advisor.UpgradePresentation.Availability.QUALITATIVE) {
			label(parent,presentation.utility); text(parent,"Ordinary DPS does not quantify this role.");
			if(presentation.comparisonMetrics.stream().noneMatch(m->m.delta!=null)) { return; }
		}
		for(com.ironpath.advisor.UpgradePresentation.SlotChange change:presentation.changes.stream().limit(1).collect(java.util.stream.Collectors.toList())) {
			label(parent,presentation.comparisonLabel);
			if(presentation.setupChange) { text(parent,"Complete compatible setups; all changed equipment is listed below."); }
			itemSummary(parent,"Before",change.beforeId,change.beforeName,current);
			itemSummary(parent,"After",change.afterId,change.afterName,current);
		}
		if(presentation.changes.isEmpty()) { text(parent,"Compared with "+presentation.baseline); }
		if(!presentation.contextLabel.isEmpty()) { text(parent,presentation.contextLabel); }
		int index=0;
		if(presentation.primaryMetric.delta!=null && presentation.primaryMetric.availability!=com.ironpath.advisor.UpgradePresentation.Availability.QUALITATIVE) {
			label(parent,presentation.primaryMetric.headline());
			JTextArea headline=(JTextArea)parent.getComponent(parent.getComponentCount()-1);headline.setName("detail-primary-result");
			headline.setFont(parent.getFont().deriveFont(java.awt.Font.BOLD,parent.getFont().getSize2D()+4));
			double gain=presentation.primaryMetric.delta;
			headline.setForeground(gain>0?new java.awt.Color(137,218,156):gain<0?new java.awt.Color(255,160,160):parent.getForeground());
		}
		for(com.ironpath.advisor.UpgradePresentation.Metric metric:presentation.comparisonMetrics) {
			boolean primary=index++==0;
			if(!primary && metric.delta!=null && metric.delta==0) { continue; }
			String title=metric.kind==ComparisonResult.Metric.THEORETICAL_DPS?"Theoretical DPS":metric.kind==ComparisonResult.Metric.MAX_HIT?"Max hit":"Hit chance";
			String value=metric.delta==null?title+" not calculated":metric.kind==ComparisonResult.Metric.MAX_HIT
				?String.format(java.util.Locale.ROOT,"%s  %.0f → %.0f  (%+.0f)",title,metric.before,metric.after,metric.delta)
				:metric.kind==ComparisonResult.Metric.THEORETICAL_DPS?String.format(java.util.Locale.ROOT,"%s  %.2f → %.2f  (%s)",title,metric.before,metric.after,metric.absoluteDpsDelta())
				:String.format(java.util.Locale.ROOT,"%s  %.2f → %.2f  (%+.2f%s)",title,metric.before,metric.after,metric.delta,metric.kind==ComparisonResult.Metric.HIT_CHANCE?" pp":"");
			text(parent,value); JTextArea row=(JTextArea)parent.getComponent(parent.getComponentCount()-1); row.setName("detail-metric-"+metric.kind);
			if(primary && metric.delta!=null && presentation.primaryMetric.availability!=com.ironpath.advisor.UpgradePresentation.Availability.QUALITATIVE) { row.setFont(parent.getFont().deriveFont(java.awt.Font.BOLD,parent.getFont().getSize2D()+2)); }
			if(metric.delta!=null && metric.delta!=0) { row.setForeground(metric.delta>0?new java.awt.Color(137,218,156):new java.awt.Color(255,160,160)); }
		}
		for(com.ironpath.advisor.UpgradePresentation.SlotChange change:presentation.changes.stream().skip(1).collect(java.util.stream.Collectors.toList())) {
			text(parent,change.label+": "+change.beforeName+" → "+change.afterName);
		}
		if(presentation.beforeTicks!=null && !presentation.beforeTicks.equals(presentation.afterTicks)) {
			text(parent,String.format(java.util.Locale.ROOT,"Attack interval %d → %d ticks (%.1f → %.1f s)",presentation.beforeTicks,presentation.afterTicks,presentation.beforeTicks*0.6,presentation.afterTicks*0.6));
		}
		com.ironpath.advisor.UpgradePresentation.Metric primary=presentation.comparisonMetrics.get(0);
		if(primary.delta==null) { text(parent,primary.reason); }
	}
	private void calculationBasis(JPanel parent,ComparisonResult comparison)
	{
		if(comparison.target!=null && comparison.target.analytical) {
			text(parent,"Shared analytical comparison: Enemy with 0 Defence. Ordinary uninterrupted attacks; this is not boss throughput, kill time or trip performance.");
		} else if(comparison.target!=null) { text(parent,"Selected context: "+comparison.target.name+". Ordinary offence only."); }
		if(comparison.beforeSetup!=null) { text(parent,comparison.beforeSetup.assumptionLabel()); }
		comparison.gaps.forEach((metric,gap)->text(parent,metric+": "+gap));
		if(comparison.before!=null) { text(parent,comparison.summary()); }
		text(parent,"Model "+comparison.modelVersion+" · knowledge "+comparison.knowledgeVersion);
	}
	private void itemSummary(JPanel parent,String label,int id,String name,java.util.function.BooleanSupplier current)
	{
		JLabel image=new JLabel("◇") { @Override public void setIcon(javax.swing.Icon icon) { if(current.getAsBoolean()) { super.setIcon(icon); setText(icon==null?"◇":""); } } };
		image.setPreferredSize(new Dimension(32,32)); image.setForeground(ColorScheme.TEXT_COLOR);
		image.getAccessibleContext().setAccessibleName(label+" "+name);
		if(id>0 && itemManager!=null) { itemManager.getImage(id).addTo(image); }
		JPanel row=new JPanel(new CardHeaderLayout()); row.setFont(parent.getFont()); row.setOpaque(false); row.setAlignmentX(LEFT_ALIGNMENT);
		row.add(image); text(row,label+": "+name); row.getComponent(1).setName("card-title"); parent.add(row);
	}

	private void acquisition(Advisor.UpgradeView view, KnowledgeCatalog catalog, Actions actions)
	{
		long expectedView=this.view;
		com.ironpath.advisor.UpgradePresentation presentation=view.presentation;
		Advisor.RouteView selected=view.routes.stream().filter(r->r.decision.id.equals(view.pursuit.route)).findFirst().orElseThrow();
		label(this,"From "+catalog.activities.getOrDefault(selected.decision.activity,selected.decision.title));
		List<Advisor.Requirement> path=view.path.isEmpty()?selected.requirements:view.remainingPath;
		remaining(this,"Remaining work",path.stream().filter(r->r.role.equals("COMPONENT")||r.role.equals("ASSEMBLY")||r.role.equals("ACTIVITY")).collect(java.util.stream.Collectors.toList()));
		label(this,"Next step"); text(this,presentation.nextStep);
		text(this,"Remaining effort: "+presentation.effort.band.label);
		if(presentation.effort.acquisitionBypassed) { text(this,"Owned components remove the fresh reward grind."); }
		else { text(this,presentation.effort.suitability.label+" · "+presentation.effort.method); }
		EffortEstimate estimate=view.pursuit.estimate;
		if(presentation.minimumMeanHours!=null) {
			text(this,String.format(java.util.Locale.ROOT,"Estimated mean %.1f–%.1f h · manual scenario",presentation.minimumMeanHours,presentation.maximumMeanHours));
			text(this,presentation.estimateScope+". Scenario sensitivity, not an RNG interval or deadline.");
		} else if(estimate!=null && estimate.meanTrials!=null) {
			text(this,String.format(java.util.Locale.ROOT,"%s · mean %.0f eligible kills",estimate.remainingOutcome,estimate.meanTrials));
			text(this,"Next reward only; hours not estimated.");
		} else { text(this,"Hours not estimated; "+presentation.effort.scope+"."); }
		JPanel requirements=section(this,"Access, equipment and preparation"); requirements.setName("acquisition-requirements");
		remaining(requirements,"Access",path.stream().filter(r->r.role.equals("ACCESS")||r.role.equals("PREREQUISITE")).collect(java.util.stream.Collectors.toList()));
		remaining(requirements,"Method checks",selected.requirements.stream().filter(r->r.role.equals("INPUTS / CONTEXT")).collect(java.util.stream.Collectors.toList()));
		remaining(requirements,"Suggested preparation (not access)",path.stream().filter(r->r.role.equals("SUGGESTED PREPARATION")||r.role.equals("PREPARATION")).collect(java.util.stream.Collectors.toList()));
		remaining(requirements,"Equip",view.equip);
		if(!presentation.routes.isEmpty() || !view.valueEvidence.roles.isEmpty()) {
			for(com.ironpath.advisor.ValueEvidence.Role role:view.valueEvidence.roles) {
				for(String alternative:role.alternatives) { text(this,"Adequate owned alternative: "+alternative); }
			}
			for(com.ironpath.advisor.UpgradePresentation.RouteChoice choice:presentation.routes) {
				if(choice.preparationUpgrade==null) { continue; }
				KnowledgeCatalog.Upgrade preparation=catalog.upgrades.stream().filter(u->u.id.equals(choice.preparationUpgrade)).findFirst().orElseThrow();
				label(this,"Optional preparation: "+preparation.title);
				text(this,"Preparation: "+(choice.preparationOwned?"already owned":choice.preparationBand.label)+" · acquisition: "+choice.acquisitionBand.label);
				text(this,choice.reason); text(this,"Combined hours unavailable; compare with starting the direct method.");
			}
		}
		JPanel basis=section(this,"Estimate basis and route evidence"); basis.setName("acquisition-evidence");
		text(basis,presentation.effort.reason); text(basis,presentation.effort.gap);
		text(basis,"Method/profile: "+presentation.effort.profile+" · "+presentation.effort.version);
		if(estimate!=null) { estimateBasis(basis,estimate,actions,expectedView); }
		if(!view.path.isEmpty()) { label(basis,"Full recipe and ALL/ANY alternatives"); requirements(basis,view.path); }
		label(basis,"Complete route evidence"); requirements(basis,selected.requirements);
		label(basis,"Equipment requirements"); requirements(basis,view.equip);
		for(Advisor.RouteView route:view.routes) {
			if(route!=selected) { label(basis,"Alternative: "+route.decision.title); text(basis,route.next); requirements(basis,route.requirements); }
			text(basis,route.decision.tradeoff);
			if(actions!=null && actions.canEdit()) { button(basis,"Skip this activity",()->{if(valid(expectedView,actions)){actions.skip(route.decision.activity,true);}}); }
		}
		for(com.ironpath.advisor.ProgressionKnowledge.Context context:catalog.progression.contexts) {
			if(context.activity.equals(view.pursuit.activity)) {
				for(com.ironpath.advisor.ProgressionKnowledge.Preparation option:context.preparation) { text(basis,option.constraint+": "+option.label); }
			}
		}
		for(String reward:view.shared) { text(basis,reward); }
		sources(basis,view.evidenceSources,catalog);
	}
	private void remaining(JPanel parent,String heading,List<Advisor.Requirement> requirements)
	{
		List<Advisor.Requirement> missing=requirements.stream().filter(r->r.truth!=Truth.TRUE).collect(java.util.stream.Collectors.toList());
		if(!missing.isEmpty()) {
			label(parent,heading);
			for(Advisor.Requirement requirement:missing) { text(parent,(requirement.truth==Truth.UNKNOWN?"Check: ":"Remaining: ")+requirement.label); }
		}

	}
	private void estimateBasis(JPanel parent,EffortEstimate estimate,Actions actions,long expectedView)
	{
		text(parent,"Scope: "+estimate.remainingOutcome+". Only this reward, not complete assembly or a full-item ETA.");
		if(estimate.meanTrials!=null) {
			text(parent,String.format(java.util.Locale.ROOT,"Mean %.0f kills · median %d · 90%% by %d; 10%% take longer.",estimate.meanTrials,estimate.p50,estimate.p90));
			if(estimate.pace!=null) {
				text(parent,String.format(java.util.Locale.ROOT,"Manual %.1f–%.1f eligible kills/hour · median %.1f h · P90 %.1f h",estimate.pace.minimumPerHour,estimate.pace.maximumPerHour,estimate.medianHours(),estimate.p90Hours()));
				text(parent,estimate.pace.provenance+". "+estimate.pace.scope);
			}
			if(actions!=null && actions.canEdit()) {
				label(parent,"Set pace (optional)"); text(parent,"Eligible kills per active hour, including cycle overhead. Number or range; session only.");
				JTextField pace=new JTextField(estimate.pace==null?"":estimate.pace.minimumPerHour+(estimate.pace.maximumPerHour>estimate.pace.minimumPerHour?"-"+estimate.pace.maximumPerHour:""));
				pace.setName("pace-input"); pace.getAccessibleContext().setAccessibleName("Eligible "+estimate.trialUnit+" per active hour, or range"); pace.setMaximumSize(new Dimension(213,28)); parent.add(pace);
				button(parent,"Apply pace",()->{if(valid(expectedView,actions)){actions.pace(pace.getText());}});
				button(parent,"Clear pace",()->{if(valid(expectedView,actions)){actions.pace("");}});
			}
		}
		text(parent,estimate.modelVersion+" · reviewed "+estimate.reviewed+" · "+estimate.reason);
		text(parent,estimate.numerator+"/"+estimate.denominator+" per "+estimate.trialUnit+". Recipient: "+estimate.recipient);
		for(String assumption:estimate.assumptions) { text(parent,assumption); }
		for(String exclusion:estimate.exclusions) { text(parent,exclusion); }
		text(parent,"Fixed-rate dry kills do not reduce remaining expectation.");
	}

	private void referenceControls(JPanel parent, AccountSnapshot a, com.ironpath.advisor.CombatSetup setup, Actions actions, Map<String, Map<String, String>> referenceChoices, boolean supported)
	{
		boolean ranged = setup.family == com.ironpath.advisor.CombatSetup.Family.RANGED;
		boolean melee = setup.family == com.ironpath.advisor.CombatSetup.Family.MELEE;
		long expectedView=view;
		java.util.function.BiConsumer<String,String> edit = (key,value) -> { if(valid(expectedView,actions)) { actions.reference((melee ? "melee/" : ranged ? "ranged/" : "") + key,value); } };
		text(parent, "Reference only. Empty slots are deliberate exclusions, not zero-valued unknown account gear. Prayer/boost are assumptions; quantities and remaining charges are not measured.");
		button(parent, "Use minimal template", () -> edit.accept("template", "true"));
		for (String slot : PoweredMagic.SLOTS)
		{
			JPanel destination = List.of("weapon", "neck", "feet").contains(slot) ? parent : section(parent, "Change " + slot);
			JComboBox<Choice> choice = choices(referenceChoices.getOrDefault(slot, Map.of()), false); select(choice, Integer.toString(setup.slots.get(slot)));
			choice.setName("reference-" + slot);
			destination.add(choice);
			JButton apply = button(destination, "Apply " + slot, () -> { if (choice.getSelectedItem() != null) { edit.accept(slot, ((Choice) choice.getSelectedItem()).id); } });
			apply.setEnabled(choice.getItemCount() > 0);
		}
		for (String key : List.of("prayer", "boost", "style", "target"))
		{
			Map<String, String> options = new java.util.LinkedHashMap<>();
			if (key.equals("target")) { options.putAll(referenceChoices.getOrDefault("target", Map.of())); }
			if (key.equals("prayer")) { for (PoweredMagic.Prayer value : PoweredMagic.Prayer.values()) { options.put(value.name(), value.name()); } }
			if (key.equals("boost")) { for (PoweredMagic.Boost value : PoweredMagic.Boost.values()) { options.put(value.name(), value.name()); } }
			if (key.equals("style")) { for (PoweredMagic.Style value : PoweredMagic.Style.values()) { options.put(value.name(), value.name()); } }
			if (ranged && !key.equals("target"))
			{
				options.clear();
				if (key.equals("prayer")) { for (com.ironpath.advisor.RangedModel.Prayer value : com.ironpath.advisor.RangedModel.Prayer.values()) { options.put(value.name(), value.name()); } }
				if (key.equals("style")) { for (com.ironpath.advisor.RangedModel.Style value : com.ironpath.advisor.RangedModel.Style.values()) { options.put(value.name(), value.name()); } }
				if (key.equals("boost")) { options.put("false","No boost"); options.put("true","Ranging potion (full boost assumed)"); }
			}
			if (melee && !key.equals("target"))
			{
				options.clear();
				if (key.equals("prayer")) { for (com.ironpath.advisor.MeleeModel.Prayer value : com.ironpath.advisor.MeleeModel.Prayer.values()) { options.put(value.name(),value.label()); } }
				if (key.equals("boost")) { for (com.ironpath.advisor.MeleeModel.Boost value : com.ironpath.advisor.MeleeModel.Boost.values()) { options.put(value.name(),value.label()); } }
				if (key.equals("style")) { options.putAll(referenceChoices.getOrDefault("styles-"+setup.slots.get("weapon"),Map.of())); }
			}
			JComboBox<Choice> choice = choices(options, false); select(choice, key.equals("target") ? setup.target : melee
				? key.equals("prayer") ? setup.meleePrayer.name() : key.equals("boost") ? setup.meleeBoost.name() : setup.meleeStyle.name() : ranged
				? key.equals("prayer") ? setup.rangedPrayer.name() : key.equals("boost") ? Boolean.toString(setup.rangingPotion) : setup.rangedStyle.name()
				: key.equals("prayer") ? setup.prayer.name() : key.equals("boost") ? setup.boost.name() : setup.style.name());
			choice.setName("reference-" + key);
			parent.add(choice); button(parent, "Apply " + key, () -> { if (choice.getSelectedItem() != null) { edit.accept(key, ((Choice) choice.getSelectedItem()).id); } });
		}
		if (ranged)
		{
			JComboBox<Choice> darts = choices(referenceChoices.getOrDefault("loaded-ammo",Map.of()),false);
			select(darts,Integer.toString(setup.loadedAmmo)); darts.setName("reference-loaded-ammo"); parent.add(darts);
			button(parent,"Assume loaded darts",() -> { if (darts.getSelectedItem() != null) { edit.accept("loaded-ammo",((Choice) darts.getSelectedItem()).id); } });
			Map<String,String> tasks = new java.util.LinkedHashMap<>(); tasks.put("false","Off-task"); tasks.put("true","On eligible Slayer task (assumed)");
			JComboBox<Choice> task = choices(tasks,false); select(task,Boolean.toString(setup.onTask)); task.setName("reference-task"); parent.add(task);
			button(parent,"Apply task assumption",() -> edit.accept("task",((Choice) task.getSelectedItem()).id));
		}
		if (supported) { button(parent, "Confirm reference", () -> edit.accept("confirm", "true")); }
	}

	private void requirements(JPanel parent, List<Advisor.Requirement> requirements)
	{
		for (Advisor.Requirement requirement : requirements)
		{ text(parent, requirement.role + " · " + (requirement.truth == Truth.TRUE ? "Confirmed for planning" : requirement.truth == Truth.FALSE ? "Confirmed gap" : "Unknown")
			+ ": " + requirement.label);
			text(parent,requirement.evidence); }
	}

	private void controls(JPanel parent, KnowledgeCatalog catalog, Preferences preferences, AccountSnapshot account, Actions actions)
	{
		long expectedView=view;
		JPanel optional=section(parent,"Optional goal");
		text(optional, "No goal is needed for account advice.");
		JComboBox<Choice> goals = choices(Advisor.goalChoices(catalog), true);
		select(goals, preferences.goal);
		optional.add(goals);
		button(optional, "Set goal", () -> { if(valid(expectedView,actions)){ actions.goal(((Choice) goals.getSelectedItem()).id); } });
		JPanel choices = section(parent, "Skipped routes and reminders");
		text(choices, "Skip an activity to exclude its acquisition route. Owned rewards stay useful; skips never complete targets.");
		JComboBox<Choice> activitySkip = choices(catalog.activities, false);
		choices.add(activitySkip);
		button(choices, "Skip selected activity", () -> { if(valid(expectedView,actions)){ actions.skip(((Choice) activitySkip.getSelectedItem()).id, true); } });
		catalog.activities.forEach((id, label) -> {
			if (preferences.skipped.contains(id)) {
				text(choices, "Skipped activity: " + label);
				button(choices, "Restore activity", () -> { if(valid(expectedView,actions)){ actions.skip(id, false); } });
			}
		});
		text(choices, "Individual reminders can also be skipped without excluding their whole activity.");
		Map<String, String> activities = new java.util.LinkedHashMap<>();
		catalog.decisions.stream().filter(d -> "reviewed".equals(d.status)).forEach(d -> activities.put(d.id, d.title));
		JComboBox<Choice> activity = choices(activities, false);
		choices.add(activity);
		button(choices, "Skip selected action", () -> { if(valid(expectedView,actions)){ actions.skip(((Choice) activity.getSelectedItem()).id, true); } });
		for (KnowledgeCatalog.Decision decision : catalog.decisions)
		{
			if (!preferences.skipped.contains(decision.id)) { continue; }
			text(choices, "Skipped: " + decision.title);
			button(choices, "Restore action", () -> { if(valid(expectedView,actions)){ actions.skip(decision.id, false); } });
		}
		JPanel corrections=section(parent,"Correct ownership and unlocks"); corrections.setName("account-corrections");
		text(corrections, "Manual ownership/unlocks are reversible planning assumptions. They do not confirm charged, worn or usable gear. Readable client quest/unlock facts take precedence; positive observed possession overrides manual absence.");
		JComboBox<Choice> fact = choices(catalog.factChoices(), false);
		corrections.add(fact); fact.setName("account-fact");
		if(correctionKey!=null) { select(fact,correctionKey); corrections.setVisible(true); }
		button(corrections, "Owned / unlocked", () -> { if(valid(expectedView,actions)){ actions.confirm(((Choice) fact.getSelectedItem()).id, true); } });
		button(corrections, "Not owned / locked", () -> { if(valid(expectedView,actions)){ actions.confirm(((Choice) fact.getSelectedItem()).id, false); } });
		button(corrections, "Clear selected fact", () -> { if(valid(expectedView,actions)){ actions.confirm(((Choice) fact.getSelectedItem()).id, null); } });
		preferences.confirmations.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry ->
		{
			String label = catalog.factChoices().get(entry.getKey());
			if (label == null) { return; }
			String precedence = "";
			if (entry.getKey().startsWith("item-") && !entry.getValue().value
				&& account.observedItem(Integer.parseInt(entry.getKey().substring(5))) == Truth.TRUE)
			{ precedence = " Positive observed possession takes precedence."; }
			if (account.fact(entry.getKey()).known())
			{ precedence = " Readable client unlock takes precedence."; }
			text(corrections,label+": manually "+(entry.getValue().value?"owned / unlocked":"not owned / locked")+"."+precedence);
			button(corrections, "Clear confirmation", () -> { if(valid(expectedView,actions)){ actions.confirm(entry.getKey(), null); } });
		});
		button(choices, "Reset all planning choices", ()->{if(valid(expectedView,actions)){actions.clear();}});
	}

	private void goal(JPanel parent, Advisor.GoalView goal, KnowledgeCatalog catalog, Actions actions)
	{
		text(parent, "Goal: " + goal.title + "\n" + goal.status);
		if (goal.nearestMissing != null) { text(parent, "Nearest confirmed missing prerequisite: " + goal.nearestMissing); }
		text(parent, "Next: " + goal.nextAction);
		JPanel requirements = section(parent, "Goal prerequisites and sources");
		for (Advisor.Requirement requirement : goal.requirements)
		{
			text(requirements, requirement.role + " · " + (requirement.truth == Truth.TRUE ? "Confirmed" : requirement.truth == Truth.FALSE ? "Missing" : "Unknown")
				+ (requirement.skipped ? " / skipped" : "") + ": " + requirement.label);
			JPanel evidence = section(parent, "Evidence: " + requirement.label); text(evidence, requirement.evidence);
		}
		text(requirements, "Reference route access / requirements: " + goal.decision.access);
		text(requirements, "Alternatives have their own access. Suggested preparation is an authored planning baseline, not a game entry requirement.");
		text(requirements, "Alternative: " + goal.decision.alternative);
		sources(requirements, goal.decision, catalog);
		if (actions != null && actions.canEdit()) { button(parent, "Clear goal", () -> actions.goal(null)); }
	}

	private void card(JPanel parent, Recommendation recommendation, KnowledgeCatalog catalog, Actions actions)
	{
		KnowledgeCatalog.Decision decision = recommendation.decision;
		JPanel card = new JPanel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0), BorderFactory.createEtchedBorder()));
		text(card, decision.title); text(card, decision.why);
		JPanel details = section(card, "Why and alternatives");
		for (String evidence : recommendation.evidence) { text(details, "Evidence: " + evidence); }
		text(details, "Access / requirements: " + decision.access);
		text(details, "Suggested preparation (not access): " + decision.preparation);
		text(details, "Alternative: " + decision.alternative);
		text(details, "Trade-off: " + decision.tradeoff);
		text(details, "Effort: " + decision.effort);
		text(details, "At most three materially different activities. Manual possession cannot establish usable weapons. Decision review: " + decision.reviewed);
		sources(details, decision, catalog);
		if (actions != null)
		{
			boolean route = decision.kind == KnowledgeCatalog.Kind.ACQUIRE || decision.activity.equals("slayer");
			button(card, route ? "Skip this activity" : "Skip this action", () -> actions.skip(route ? decision.activity : decision.id, true));
		}
		parent.add(card);
	}

	private void sources(JPanel panel, KnowledgeCatalog.Decision decision, KnowledgeCatalog catalog)
	{ sources(panel, decision.sources, catalog); }

	private void sources(JPanel panel, List<String> sourceIds, KnowledgeCatalog catalog)
	{
		JPanel references = section(panel, "Reviewed sources (" + sourceIds.size() + ")");
		for (String id : sourceIds)
		{
			KnowledgeCatalog.Source source = catalog.sources.get(id);
			button(references, "Source: " + id, () -> LinkBrowser.browse(source.url));
		}
	}

	private JPanel section(JPanel parent, String label)
	{
		JPanel details = new JPanel(); details.setFont(parent.getFont()); details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS)); details.setVisible(false);
		details.setBackground(ColorScheme.DARK_GRAY_COLOR);
		button(parent, label, () -> { details.setVisible(!details.isVisible()); revalidate(); });
		parent.add(details); return details;
	}

	private static JComboBox<Choice> choices(Map<String, String> options, boolean noGoal)
	{
		JComboBox<Choice> combo = new JComboBox<>();
		if (noGoal) { combo.addItem(new Choice(null, "No goal")); }
		options.forEach((id, label) -> combo.addItem(new Choice(id, label)));
		combo.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH - 12, 26));
		combo.setAlignmentX(LEFT_ALIGNMENT); return combo;
	}

	private static void select(JComboBox<Choice> combo, String id)
	{
		for (int i = 0; i < combo.getItemCount(); i++)
		{
			if (java.util.Objects.equals(combo.getItemAt(i).id, id)) { combo.setSelectedIndex(i); return; }
		}
	}
	private static final class Choice
	{
		final String id, label;
		Choice(String id, String label) { this.id = id; this.label = label; }
		@Override public String toString() { return label; }
	}

	static JButton button(JPanel panel, String label, Runnable action)
	{
		JButton button = new JButton(label);
		button.setFont(panel.getFont()); button.setMargin(new java.awt.Insets(2,3,2,3));
		button.setForeground(ColorScheme.TEXT_COLOR); button.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
		button.setContentAreaFilled(false); button.setOpaque(true);
		if (label.length() > 24 || button.getFontMetrics(button.getFont()).stringWidth(label)>185)
		{ button.setText("<html><div style='width:150px'>" + label.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</div></html>"); }
		button.setToolTipText(label);
		int height = Math.max(28, button.getPreferredSize().height);
		button.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH - 12, height));
		button.setMinimumSize(new Dimension(0, height));
		button.setAlignmentX(LEFT_ALIGNMENT);
		button.addActionListener(event -> action.run());
		button.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("ENTER"), "activate");
		button.getActionMap().put("activate", new javax.swing.AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent event) { button.doClick(); } });
		panel.add(button); return button;
	}
	private static String level(AccountSnapshot account, String skill) { return account.level(skill) == null ? "unknown" : account.level(skill).toString(); }
	private static String unlock(Observation<Boolean> fact) { return !fact.known() ? "unknown" : fact.value() ? "unlocked" : "not unlocked"; }
	static void text(JPanel panel, String message)
	{
		JTextArea text = new JTextArea(message)
		{
			@Override public void scrollRectToVisible(java.awt.Rectangle bounds)
			{
				// A newly rendered label's deferred caret repaint must not scroll the result list.
				if (isFocusOwner()) { super.scrollRectToVisible(bounds); }
			}
			@Override public Dimension getPreferredSize()
			{
				int width = getParent() == null || getParent().getWidth() <= 0 ? 201 : Math.max(1, getParent().getWidth() - getParent().getInsets().left - getParent().getInsets().right);
				// BoxLayout caps the allocated width even when the native viewport is wider.
				// Measure wrapping at that same width so detail paragraphs cannot lose lines.
				width = Math.min(width, getMaximumSize().width);
				if ("card-title".equals(getName())) { width=Math.max(1,width-46); }
				java.awt.Insets insets = getInsets();
				javax.swing.text.View view = getUI().getRootView(this);
				// Swing reserves one pixel for the document caret when wrapping.
				view.setSize(Math.max(1, width - insets.left - insets.right - 1), Integer.MAX_VALUE);
				return new Dimension(width, (int) Math.ceil(view.getPreferredSpan(javax.swing.text.View.Y_AXIS))
					+ insets.top + insets.bottom);
			}
		}; text.setFocusable(false); text.setLineWrap(true); text.setWrapStyleWord(true);
		text.setMinimumSize(new Dimension(0,0));
		text.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		text.setFont(panel.getFont());
		text.setEditable(false); text.setOpaque(false);
		text.setForeground(ColorScheme.TEXT_COLOR);
		text.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0)); text.setAlignmentX(LEFT_ALIGNMENT);
		panel.add(text);
	}
}
