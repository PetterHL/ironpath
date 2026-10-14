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
	private String query = "";
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
	private boolean filtersExpanded, includeOwned, accountView, orderUpdated;
	private Anchor accountAnchor;
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
            pathDetail=false; homeAnchor=browseAnchor=detailAnchor=accountAnchor=null; browse=false; category="All"; query="";
            searchDot=searchMark=0; returnFocus=null; facets.clear(); filtersExpanded=includeOwned=accountView=orderUpdated=false;
        }
		if (snapshot == null || upgrades.stream().noneMatch(u -> u.upgrade.id.equals(detail))
			&& overview.activities.stream().noneMatch(a -> ("activity:" + a.id).equals(detail))
			&& overview.owned.stream().noneMatch(u->("owned:"+u.upgrade.id).equals(detail))) { detail = null; }
		removeAll(); setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); setBackground(ColorScheme.DARK_GRAY_COLOR);
		JPanel heading=new ActionLinks(); heading.setFont(getFont());
		JLabel brand=new JLabel("IronPath"); brand.setForeground(ColorScheme.TEXT_COLOR); brand.setFont(getFont().deriveFont(java.awt.Font.BOLD)); heading.add(brand);
		if (snapshot!=null && !browse && detail==null && !accountView && !recentView) { button(heading,"Browse",()->showBrowse(currentView,actions)).setName("home-browse"); }
		add(heading);
		if (snapshot==null) { text(this,status); }
		else {
			JButton evidence=button(this,"Based on observed gear ›",()->showAccount(true,currentView,actions)); evidence.setName("account-evidence");
			evidence.getAccessibleContext().setAccessibleName("Account evidence and preferences: observations, timestamps and unknowns");
			evidence.setOpaque(false); evidence.setBorder(BorderFactory.createEmptyBorder(2,0,2,0));
			evidence.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		}
		if (status.startsWith("Pace needs")) { text(this, status); }
		if (snapshot != null && catalog != null)
		{
			com.ironpath.advisor.OwnedUpgrade selectedOwned=overview.owned.stream().filter(u->("owned:"+u.upgrade.id).equals(detail)).findFirst().orElse(null);
			Advisor.UpgradeView selected = upgrades.stream().filter(u -> u.upgrade.id.equals(detail)).findFirst().orElse(null);
			if (accountView) {
				button(this,recentView ? "Back to recent boss" : "Back to upgrades",()->showAccount(false,currentView,actions)).setName("account-back");
				account(this,status,snapshot,catalog,preferences,actions,storageStatus,goal);
			}
			else if(recentView && displayedRecent!=null)
			{
				button(this,recentOption==null ? "Back to upgrades" : "Back to recent boss",()->{
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
				button(this,"Back to upgrades",()->navigate(null,currentView,actions)); ownedDetails(selectedOwned,catalog,actions);
			}
			else if (selected != null)
			{
				button(this, "Back to upgrades", () -> navigate(null, currentView, actions));
				upgradeDetails(selected, snapshot, preferences, catalog, actions, reference);
			}
			else if (detail != null && detail.startsWith("activity:"))
			{
				button(this, "Back to upgrades", () -> navigate(null, currentView, actions));
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

					if (upgrades.isEmpty()) { text(this, "No remaining supported upgrade from these observations. Open the bank or inspect coverage; unknown is not absence. No priorities are added to fill the list."); }
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
				JPanel account = section(this, "Account and preferences");
				account(account,status,snapshot,catalog,preferences,actions,storageStatus,goal);
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
		accountView=open; refresh.run();
		restoreNavigation(viewport,open ? null : accountAnchor,open ? "account-back" : "account-evidence",view,actions);
	}
	private void account(JPanel parent,String status,AccountSnapshot snapshot,KnowledgeCatalog catalog,Preferences preferences,Actions actions,String storageStatus,Advisor.GoalView goal)
	{
		text(parent,"Reference setups stay in memory; explicit planning choices are saved locally.");
		recentControls(parent,actions);
		text(parent,status); text(parent,"Knowledge "+catalog.version+" · reviewed "+catalog.reviewed);
		if (ChronoUnit.DAYS.between(catalog.reviewed,LocalDate.now())>90) { text(parent,"Review is over 90 days old. Check sources before pursuing."); }
		observations(parent,snapshot,catalog); text(parent,storageStatus);
		if (actions!=null && actions.canEdit()) { controls(parent,catalog,preferences,snapshot,actions); }
		if (goal!=null) { goal(parent,goal,catalog,actions); }
	}

    private void recentControls(JPanel parent,Actions actions)
    {
        long currentView=view;
				if(actions!=null)
				{
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
		button(this, "Overview", () -> switchList(false,expectedView,actions)).setName("browse-overview");
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
		JPanel card=new JPanel(); card.setFont(getFont()); card.setName("upgrade-card-"+upgrade.upgrade.id);
		card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS)); card.setAlignmentX(LEFT_ALIGNMENT);
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR); card.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),BorderFactory.createEmptyBorder(6,6,6,6)));
		itemHeader(card,upgrade.upgrade,current);
		text(card,copy.reason); card.getComponent(card.getComponentCount()-1).setFocusable(false); card.getComponent(card.getComponentCount()-1).setName("card-reason");
		text(card,copy.metric); card.getComponent(card.getComponentCount()-1).setFocusable(false); card.getComponent(card.getComponentCount()-1).setName("comparison-headline");
		text(card,copy.status); card.getComponent(card.getComponentCount()-1).setFocusable(false); card.getComponent(card.getComponentCount()-1).setName("card-status");
		JPanel actionsRow=new ActionLinks(); actionsRow.setFont(getFont()); actionsRow.setOpaque(false); actionsRow.setAlignmentX(LEFT_ALIGNMENT);
		JButton open=button(actionsRow,"Value",()->{ if (current.getAsBoolean()) { navigate(upgrade.upgrade.id,false,expectedView,actions); } });
		open.getActionMap().put("open-card",open.getActionMap().get("activate"));
		open.setName("open-upgrade-"+upgrade.upgrade.id); open.setToolTipText(upgrade.upgrade.title);
		open.getAccessibleContext().setAccessibleName("Value for "+upgrade.upgrade.title);
		JButton path=button(actionsRow,"Path",()->{ if (current.getAsBoolean()) { navigate(upgrade.upgrade.id,true,expectedView,actions); } });
		path.setName("path-upgrade-"+upgrade.upgrade.id); path.getAccessibleContext().setAccessibleName("Path for "+upgrade.upgrade.title);
		card.add(actionsRow);
		java.awt.event.MouseAdapter activate=new java.awt.event.MouseAdapter() { public void mouseClicked(java.awt.event.MouseEvent event) { if (valid(expectedView,actions)) { open.doClick(); } } };
		clickable(card,activate,open); parent.add(card);
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
		JPanel card=new JPanel(); card.setFont(getFont()); card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS)); card.setAlignmentX(LEFT_ALIGNMENT);
		card.setName("upgrade-card-"+id); card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),BorderFactory.createEmptyBorder(6,6,6,6)));
		itemHeader(card,owned.upgrade,current);
		text(card,"Acquisition covered by owned gear or a confirmed planning fact.");
		text(card,"Usability and personal benefit need their own checks.");
		JButton open=button(card,"Value",()->{if(current.getAsBoolean()){navigate(id,expectedView,actions);}}); open.setName("open-upgrade-"+id);
		open.getAccessibleContext().setAccessibleName("Owned use of "+owned.upgrade.title); clickable(card,new java.awt.event.MouseAdapter(){public void mouseClicked(java.awt.event.MouseEvent e){open.doClick();}},open);
		parent.add(card);
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
		JPanel evidence=section(this,"Ownership evidence");text(evidence,owned.evidence);
		sources(this,owned.sources,catalog);
	}

	private void clickable(java.awt.Container parent, java.awt.event.MouseAdapter listener, JButton open)
	{
		parent.addMouseListener(listener);
		for (java.awt.Component child : parent.getComponents())
		{ if (child instanceof javax.swing.AbstractButton) { continue; } if (child instanceof java.awt.Container) { clickable((java.awt.Container)child,listener,open); } else { child.addMouseListener(listener); } }
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
		text(this,view.pursuit.category.label);

		long expectedView = this.view;
		JPanel tabs = new JPanel(new java.awt.GridLayout(1, 2, 4, 0)); tabs.setFont(getFont()); tabs.setOpaque(false); tabs.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH - 12, 30));
		tabs.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 12, 30));
		tabs.setMinimumSize(new Dimension(PluginPanel.PANEL_WIDTH - 12, 30)); tabs.setAlignmentX(LEFT_ALIGNMENT);
		JButton valueTab=button(tabs, "Value", () -> { if (valid(expectedView, actions)) { pathDetail = false; refresh.run(); } });
		JButton pathTab=button(tabs, "Path", () -> { if (valid(expectedView, actions)) { pathDetail = true; refresh.run(); } });
		JButton selectedTab=pathDetail ? pathTab : valueTab; selectedTab.setContentAreaFilled(true); selectedTab.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
		selectedTab.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new java.awt.Color(244,213,141)),BorderFactory.createEmptyBorder(2,3,2,3)));
		selectedTab.getAccessibleContext().setAccessibleDescription("Selected view");
		int tabHeight=Math.max(valueTab.getPreferredSize().height,pathTab.getPreferredSize().height);
		tabs.setMaximumSize(new Dimension(213,tabHeight)); tabs.setPreferredSize(new Dimension(213,tabHeight)); tabs.setMinimumSize(new Dimension(0,tabHeight)); add(tabs);
		if (pathDetail) { acquisition(view, catalog, actions); return; }
		text(this,view.valueEvidence.importance.label + ": " + view.valueEvidence.reason);
		text(this,"Baseline: " + view.valueEvidence.baseline);
		if (!view.pursuit.blockers.isEmpty()) { text(this,"Next check: " + view.pursuit.blockers.get(0)); }
		for (com.ironpath.advisor.ValueEvidence.Role role : view.valueEvidence.roles)
		{
			text(this,role.reason + " Applicable: " + role.applicable + ".");
			for (String alternative : role.alternatives) { text(this,"Adequate owned alternative: " + alternative); }
			text(this,"Defer / limits: " + role.limitation);
		}
		for (String trail : view.indirectReasons) { text(this,trail); }
		JPanel whyOrder = section(this, "Why this order");
		for (String reason : view.pursuit.reasons) { text(whyOrder, reason); }
		text(whyOrder, "Ranking " + view.pursuit.version + ": " + String.join(", ", view.pursuit.reasonCodes));
		for (String evidence : view.evidence) { text(whyOrder, evidence); }
		ComparisonResult comparison = view.comparison;
		setup = comparison.beforeSetup == null ? setup : comparison.beforeSetup;
		if (comparison.beforeSetup != null && comparison.afterSetup != null)
		{
			String slot = upgrade.comparison.equals("MELEE") ? catalog.melee.items.stream().filter(i -> i.id == upgrade.itemId).map(i -> i.slot).findFirst().orElse("weapon") : upgrade.comparison.equals("RANGED") ? catalog.ranged.items.stream().filter(i -> i.id == upgrade.itemId).map(i -> i.slot).findFirst().orElse("weapon") : catalog.comparison.items.stream().filter(i -> i.id == upgrade.itemId).map(i -> i.slot).findFirst().orElse("weapon");
			itemSummary(this, "Before", comparison.beforeSetup.slots.get(slot), catalog);
			itemSummary(this, "After", comparison.afterSetup.slots.get(slot), catalog);
			if (!comparison.beforeSetup.slots.get("shield").equals(comparison.afterSetup.slots.get("shield")))
			{ text(this, "Off-hand removed for a two-handed replacement."); }
		}
		if (comparison.before == null) { text(this, (comparison.primaryMetric == ComparisonResult.Metric.THEORETICAL_DPS
			? "Theoretical DPS unavailable · model gap: " : "Comparison unavailable: ") + comparison.reason); }
		if (comparison.before != null)
		{
			String max = "Max hit  " + comparison.before.maxHit + " → " + comparison.after.maxHit + "  ("
				+ String.format(java.util.Locale.ROOT, "%+d", comparison.after.maxHit - comparison.before.maxHit) + ")";
			String dps = comparison.available(ComparisonResult.Metric.THEORETICAL_DPS) ? String.format(java.util.Locale.ROOT,
				"Theoretical DPS  %.2f → %.2f  (%+.2f)", comparison.before.dps, comparison.after.dps, comparison.dpsGain()) : "Theoretical DPS unavailable · model gap";
			boolean dpsFirst = comparison.primaryMetric == ComparisonResult.Metric.THEORETICAL_DPS;
			text(this, dpsFirst ? dps : max); text(this, dpsFirst ? max : dps);
			String gap = comparison.gaps.get(ComparisonResult.Metric.THEORETICAL_DPS);
			if (gap != null) { text(this, (comparison.available(ComparisonResult.Metric.HIT_CHANCE) ? "DPS gap: " : "DPS and hit chance gap: ") + gap); }
			text(this, comparison.available(ComparisonResult.Metric.HIT_CHANCE) ? String.format(java.util.Locale.ROOT,
				"Hit chance  %.1f%% → %.1f%%", comparison.before.hitChance * 100, comparison.after.hitChance * 100) : "Hit chance unavailable · model gap");
			text(this, String.format(java.util.Locale.ROOT, "Attack interval  %d → %d ticks (%.1f → %.1f s)",
				comparison.before.attackTicks, comparison.after.attackTicks, comparison.before.attackTicks * 0.6, comparison.after.attackTicks * 0.6));
			text(this, comparison.target.name + " · " + setup.assumptionLabel() + ". Reference only.");
		}
		if (!upgrade.comparison.equals("NONE"))
		{
			JPanel assumptions = section(this, "Reference setup and assumptions");
			for (String assumption : comparison.assumptions) { text(assumptions, assumption); }
			if (comparison.before == null && setup != null) { text(assumptions, "Selected slots: " + setup.slots + ". Zero denotes an explicitly empty reference slot. " + setup.assumptionLabel() + "."); }
			if (actions != null && actions.canEdit() && setup != null) { referenceControls(assumptions, a, setup, actions, view.referenceChoices, comparison.before != null); }
		}
		text(this, "Alternative: " + upgrade.alternatives); text(this, "Limits: " + upgrade.limitation);
		if (!view.contextComparisons.isEmpty())
		{
			JPanel contexts = section(this, "Reviewed encounter trade-offs");
			text(contexts, "Each encounter uses its strongest supported owned package. These examples are separate from the selected reference; gains are not added.");
			for (ComparisonResult contextual : view.contextComparisons)
			{
				text(contexts, contextual.summary());
				JPanel inputs = section(contexts, contextual.target.name + " inputs and assumptions");
				for (String assumption : contextual.assumptions) { text(inputs, assumption); }
			}
		}
		JPanel remaining = section(this, "Remaining work and review"); text(remaining, upgrade.remainingWork + " Review: " + upgrade.reviewed + ".");
		for (String application : view.applications) { text(this, application); }
		java.util.Set<String> confirmationKeys = new java.util.LinkedHashSet<>();
		if (upgrade.ownedWhen.test == KnowledgeCatalog.Test.POSSESSION) { confirmationKeys.add("owns-" + upgrade.ownedWhen.key); }
		for (String set : upgrade.unknownSets) { confirmationKeys.add("owns-" + set); }
		confirmationKeys.addAll(view.questions);
		JPanel ownership = section(this, "Already own this? / Correct ownership");
		for (String key : confirmationKeys)
		{
			text(ownership, "Targeted ownership check: " + catalog.factChoices().get(key) + "? "
				+ (p.confirmations.containsKey(key) ? p.confirmations.get(key).evidence() : "Unconfirmed outside observed storage.")
				+ " Positive observed/manual possession takes precedence over an absence.");
			if (actions != null)
			{
				button(ownership, "Already owned", () -> actions.confirm(key, true));
				button(ownership, "Confirm not owned", () -> actions.confirm(key, false));
				button(ownership, "Clear confirmation", () -> actions.confirm(key, null));
			}
		}
		sources(this, view.evidenceSources, catalog);
	}

	private void itemSummary(JPanel parent, String label, int id, KnowledgeCatalog catalog)
	{
		JLabel icon = new JLabel(label); icon.setForeground(ColorScheme.TEXT_COLOR);
		if (id > 0 && itemManager != null) { itemManager.getImage(id).addTo(icon); } parent.add(icon);
		text(parent, id == 0 ? "Deliberately empty slot" : catalog.comparison.items.stream().filter(i -> i.id == id).map(i -> i.label).findFirst()
			.orElseGet(() -> catalog.ranged.items.stream().filter(i -> i.id == id).map(i -> i.label).findFirst()
				.orElseGet(() -> catalog.melee.items.stream().filter(i -> i.id == id).map(i -> i.label).findFirst().orElse("Unknown reference"))));
	}

	private void acquisition(Advisor.UpgradeView view, KnowledgeCatalog catalog, Actions actions)
	{
		KnowledgeCatalog.Upgrade upgrade = view.upgrade;
		text(this, "Next: " + (view.pursuit.blockers.isEmpty() ? view.routes.stream().filter(r -> r.decision.id.equals(view.pursuit.route)).findFirst().orElse(view.routes.get(0)).next : view.pursuit.blockers.get(0)));
		for (com.ironpath.advisor.ValueEvidence.Role role : view.valueEvidence.roles)
		{ for (String alternative : role.alternatives) { text(this,"Owned method alternative: " + alternative); } }
		for (com.ironpath.advisor.ProgressionKnowledge.Context context : catalog.progression.contexts)
		{
			if (!context.activity.equals(view.pursuit.activity)) { continue; }
			for (com.ironpath.advisor.ProgressionKnowledge.Preparation option : context.preparation)
			{ text(this,option.constraint + ": " + option.label); }
		}
		effort(view.pursuit, actions);
		if (!view.path.isEmpty()) { JPanel recipe = section(this, "Recipe and ALL/ANY alternatives"); text(recipe, "ALL/ANY recipe: completed intermediates bypass upstream work. This is the full reviewed recipe, not an instruction to repeat it."); requirements(recipe, view.path); }
		text(this, "Equip requirements (separate from acquisition)"); requirements(this, view.equip);
		if (!view.parts.isEmpty()) { text(this, "Owned parts and additions — optional steps are labelled"); requirements(this, view.parts); }
		text(this, "Acquisition paths — each outcome is grouped once");
		for (Advisor.RouteView route : view.routes)
		{
			text(this, route.decision.title + "\n" + (route.decision.id.equals(view.pursuit.route) ? view.pursuit.category.label + " · remaining route" : route.status) + "\nNext: " + route.next);
			JPanel steps = section(this, "Route requirements"); requirements(steps, route.requirements);
			text(steps, route.decision.access); text(steps, "Suggested preparation: " + route.decision.preparation);
			text(steps, "Trade-off: " + route.decision.tradeoff); text(steps, "Remaining effort: " + route.decision.effort);
			if (actions != null && actions.canEdit()) { button(steps, "Skip this activity", () -> actions.skip(route.decision.activity, true)); }
		}
		if (!view.shared.isEmpty())
		{
			text(this, "Same Doom acquisition activity, separate conditional rewards. Treads require completed delve 4. Rewards are rolled separately; their benefits are not added and nothing certifies delve capability.");
			for (String reward : view.shared) { text(this, reward); }
		}
		sources(this, view.evidenceSources, catalog);
	}

	private void effort(PursuitAssessment pursuit, Actions actions)
	{
		String process=pursuit.acquisitionMode == PursuitAssessment.Mode.READY ? "Assembly/claim remains" : pursuit.acquisitionMode == PursuitAssessment.Mode.BOUNDED ? "Finite remaining task" : pursuit.acquisitionMode == PursuitAssessment.Mode.REPEATABLE ? "Repeatable acquisition" : pursuit.acquisitionMode == PursuitAssessment.Mode.LONG_TAIL ? "Long-tail acquisition" : "Effort not estimated";
		text(this, "Remaining effort: " + process + " · " + pursuit.burden.toString().toLowerCase(java.util.Locale.ROOT) + " work" + (pursuit.tail == PursuitAssessment.Tail.LONG ? " · long RNG tail" : ""));
		EffortEstimate estimate = pursuit.estimate;
		if (estimate == null) { text(this, "Time not estimated. " + pursuit.reasons.get(1)); return; }
		text(this, estimate.remainingOutcome + " · next reward only");
		if (pursuit.category == PursuitAssessment.Category.CHECK || pursuit.category == PursuitAssessment.Category.PLAN)
		{ text(this, "Conditional planning: confirm the route/component checks above before pursuing a new shard."); }
		if (estimate.meanTrials == null) { text(this, estimate.reason); return; }
		text(this, String.format(java.util.Locale.ROOT,"Mean %.0f kills · median %d\n90%% by %d eligible kills; 10%% take longer.",estimate.meanTrials,estimate.p50,estimate.p90));
		if (estimate.pace == null) { text(this, "Hours unavailable until an explicit eligible-kills/hour scenario is supplied."); }
		else
		{
			text(this,String.format(java.util.Locale.ROOT,"Conditional on %.1f eligible kills/hour:\nMean %.1f h · median %.1f h\n90%% by %.1f h; 10%% later.",estimate.pace.minimumPerHour,estimate.meanHours(),estimate.medianHours(),estimate.p90Hours()));
			if (estimate.pace.maximumPerHour > estimate.pace.minimumPerHour)
			{ text(this,String.format(java.util.Locale.ROOT,"Pace sensitivity %.1f–%.1f kills/h: mean %.1f–%.1f h. Scenario range, not a confidence interval.",estimate.pace.minimumPerHour,estimate.pace.maximumPerHour,estimate.meanTrials/estimate.pace.maximumPerHour,estimate.meanHours())); }
			text(this, estimate.pace.provenance + ". " + estimate.pace.scope + ". Entered " + estimate.pace.entered);
		}
		for (String exclusion : estimate.exclusions) { text(this, exclusion); }
		text(this, "Fixed-rate dry kills do not reduce remaining expectation. RNG has no completion bar.");
		long expectedView=this.view;
		if (actions != null && actions.canEdit())
		{
			JPanel input=section(this,"Set pace (optional)"); text(input,"Eligible kills per active hour, including your cycle overhead. A number or range; blank clears. Session only.");
			javax.swing.JTextField pace=new javax.swing.JTextField(estimate.pace == null ? "" : Double.toString(estimate.pace.minimumPerHour)
				+ (estimate.pace.maximumPerHour > estimate.pace.minimumPerHour ? "-" + estimate.pace.maximumPerHour : ""));
			pace.setName("pace-input"); pace.getAccessibleContext().setAccessibleName("Eligible demonic gorilla kills per active hour, or range");
			pace.setMaximumSize(new Dimension(213,28)); input.add(pace);
			button(input,"Apply pace",() -> { if (valid(expectedView,actions)) { actions.pace(pace.getText()); } });
			button(input,"Clear pace",() -> { if (valid(expectedView,actions)) { actions.pace(""); } });
		}
		JPanel assumptions=section(this,"Acquisition model and inputs"); text(assumptions,estimate.modelVersion + " · reviewed " + estimate.reviewed + " · " + estimate.reason);
		text(assumptions, estimate.numerator + "/" + estimate.denominator + " per " + estimate.trialUnit + ". Recipient: " + estimate.recipient);
		for (String assumption : estimate.assumptions) { text(assumptions,assumption); }
	}

	private void referenceControls(JPanel parent, AccountSnapshot a, com.ironpath.advisor.CombatSetup setup, Actions actions, Map<String, Map<String, String>> referenceChoices, boolean supported)
	{
		boolean ranged = setup.family == com.ironpath.advisor.CombatSetup.Family.RANGED;
		boolean melee = setup.family == com.ironpath.advisor.CombatSetup.Family.MELEE;
		java.util.function.BiConsumer<String,String> edit = (key,value) -> actions.reference((melee ? "melee/" : ranged ? "ranged/" : "") + key,value);
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
			JPanel evidence = section(parent, "Evidence"); text(evidence, requirement.evidence); }
	}

	private void controls(JPanel parent, KnowledgeCatalog catalog, Preferences preferences, AccountSnapshot account, Actions actions)
	{
		text(parent, "Optional goal (one at a time)");
		JComboBox<Choice> goals = choices(Advisor.goalChoices(catalog), true);
		select(goals, preferences.goal);
		parent.add(goals);
		button(parent, "Set goal", () -> actions.goal(((Choice) goals.getSelectedItem()).id));
		JPanel choices = section(parent, "Skips and manual confirmations");
		text(choices, "Skip an activity to exclude its acquisition route. Owned rewards stay useful; skips never complete targets.");
		JComboBox<Choice> activitySkip = choices(catalog.activities, false);
		choices.add(activitySkip);
		button(choices, "Skip selected activity", () -> actions.skip(((Choice) activitySkip.getSelectedItem()).id, true));
		catalog.activities.forEach((id, label) -> {
			if (preferences.skipped.contains(id)) {
				text(choices, "Skipped activity: " + label);
				button(choices, "Restore activity", () -> actions.skip(id, false));
			}
		});
		text(choices, "Individual reminders can also be skipped without excluding their whole activity.");
		Map<String, String> activities = new java.util.LinkedHashMap<>();
		catalog.decisions.stream().filter(d -> "reviewed".equals(d.status)).forEach(d -> activities.put(d.id, d.title));
		JComboBox<Choice> activity = choices(activities, false);
		choices.add(activity);
		button(choices, "Skip selected action", () -> actions.skip(((Choice) activity.getSelectedItem()).id, true));
		for (KnowledgeCatalog.Decision decision : catalog.decisions)
		{
			if (!preferences.skipped.contains(decision.id)) { continue; }
			text(choices, "Skipped: " + decision.title);
			button(choices, "Restore action", () -> actions.skip(decision.id, false));
		}
		text(choices, "Manual ownership/unlocks are reversible planning assumptions. They do not confirm charged, worn or usable gear. Readable client quest/unlock facts take precedence; positive observed possession overrides manual absence.");
		JComboBox<Choice> fact = choices(catalog.factChoices(), false);
		choices.add(fact);
		button(choices, "Owned / unlocked", () -> actions.confirm(((Choice) fact.getSelectedItem()).id, true));
		button(choices, "Not owned / locked", () -> actions.confirm(((Choice) fact.getSelectedItem()).id, false));
		button(choices, "Clear selected fact", () -> actions.confirm(((Choice) fact.getSelectedItem()).id, null));
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
			text(choices, label + ": " + entry.getValue().evidence() + "." + precedence);
			button(choices, "Clear confirmation", () -> actions.confirm(entry.getKey(), null));
		});
		button(choices, "Reset all planning choices", actions::clear);
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
		}; text.setLineWrap(true); text.setWrapStyleWord(true);
		text.setMinimumSize(new Dimension(0,0));
		text.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		text.setFont(panel.getFont());
		text.setEditable(false); text.setOpaque(false);
		text.setForeground(ColorScheme.TEXT_COLOR);
		text.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0)); text.setAlignmentX(LEFT_ALIGNMENT);
		panel.add(text);
	}
}
