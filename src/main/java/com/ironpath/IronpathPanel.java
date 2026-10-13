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
	private java.awt.Point overviewPosition;
	private long view;
	private Runnable refresh;
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
		default boolean current() { return true; }
		default boolean canEdit() { return true; }
	}

	void render(String status, AccountSnapshot snapshot, List<Recommendation> advice, KnowledgeCatalog catalog,
		Advisor.GoalView goal, Preferences preferences, Actions actions, String storageStatus, List<String> reasons)
	{ render(status, snapshot, advice, catalog, goal, preferences, actions, storageStatus, reasons, List.of(), null); }

	void render(String status, AccountSnapshot snapshot, List<Recommendation> advice, KnowledgeCatalog catalog,
		Advisor.GoalView goal, Preferences preferences, Actions actions, String storageStatus, List<String> reasons,
		List<Advisor.UpgradeView> upgrades, PoweredMagic.Setup reference)
	{
		render(status, snapshot, advice, catalog, goal, preferences, actions, storageStatus, reasons,
			new PursuitOverview(upgrades, List.of(), List.of(), advice), reference);
	}

	void render(String status, AccountSnapshot snapshot, List<Recommendation> advice, KnowledgeCatalog catalog,
		Advisor.GoalView goal, Preferences preferences, Actions actions, String storageStatus, List<String> reasons,
		PursuitOverview overview, PoweredMagic.Setup reference)
	{
		List<Advisor.UpgradeView> upgrades = overview.all;
		if (!SwingUtilities.isEventDispatchThread()) { throw new IllegalStateException("Swing thread required"); }
		long currentView = ++view;
		refresh = () -> render(status, snapshot, advice, catalog, goal, preferences, actions, storageStatus, reasons, overview, reference);
		if (snapshot == null) { pathDetail = false; overviewPosition = null; browse = false; category = "All"; returnFocus = null; }
		if (snapshot == null || upgrades.stream().noneMatch(u -> u.upgrade.id.equals(detail))
			&& overview.activities.stream().noneMatch(a -> ("activity:" + a.id).equals(detail))) { detail = null; }
		removeAll(); setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); setBackground(ColorScheme.DARK_GRAY_COLOR);
		text(this, "IronPath");
		text(this, snapshot == null ? status : "Based on observed gear · local planning");
		if (status.startsWith("Pace needs")) { text(this, status); }
		if (snapshot != null && catalog != null)
		{
			Advisor.UpgradeView selected = upgrades.stream().filter(u -> u.upgrade.id.equals(detail)).findFirst().orElse(null);
			if (selected != null)
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
				if (browse) { browse(overview, reference, currentView, actions); }
				else
				{
					button(this, "Browse", () -> showBrowse(currentView, actions));
					text(this, "Worth pursuing");
					if (overview.pursuits.isEmpty()) { text(this, "No confirmed immediate pursuit. Check inputs or explore longer-term value below."); }
					for (Advisor.UpgradeView upgrade : overview.pursuits) { upgradeCard(this, upgrade, reference, currentView, actions, false); }
					overview.activities.stream().filter(a -> a.category.equals("Worth doing")).findFirst().ifPresent(a -> {
						text(this, "Worth doing"); activityCard(a, currentView, actions);
					});
					if (!overview.longTerm.isEmpty())
					{
						text(this, "Long-term upgrades"); upgradeCard(this, overview.longTerm.get(0), reference, currentView, actions, false);
					}
					if (!overview.conditional.isEmpty())
					{
						text(this, "Check first / Plan ahead"); upgradeCard(this, overview.conditional.get(0), reference, currentView, actions, false);
					}
					if (upgrades.isEmpty()) { text(this, "No remaining supported upgrade from these observations. Open the bank or inspect coverage; unknown is not absence. No priorities are added to fill the list."); }
					button(this, "Browse all (" + upgrades.size() + ")", () -> showBrowse(currentView, actions));
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
				text(account, status); text(account, "Knowledge " + catalog.version + " · reviewed " + catalog.reviewed);
				if (ChronoUnit.DAYS.between(catalog.reviewed, LocalDate.now()) > 90) { text(account, "Review is over 90 days old. Check sources before pursuing."); }
				observations(account, snapshot, catalog); text(account, storageStatus);
				if (actions != null && actions.canEdit()) { controls(account, catalog, preferences, snapshot, actions); }
				if (goal != null) { goal(account, goal, catalog, actions); }
			}
		}
		text(this, "Local planning · live checks pending. Reference setups stay in memory; planning choices are saved locally.");
		revalidate(); repaint();
	}

	private void navigate(String id, long expectedView, Actions actions)
	{
		if (view != expectedView || (actions != null && !actions.current())) { return; }
		javax.swing.JViewport viewport = (javax.swing.JViewport) SwingUtilities.getAncestorOfClass(javax.swing.JViewport.class, this);
		if (detail == null && id != null && viewport != null) { overviewPosition = viewport.getViewPosition(); }
		if (id != null) { returnFocus = id.startsWith("activity:") ? "open-" + id : "open-upgrade-" + id; }
		detail = id; pathDetail = false; refresh.run(); long returnedView = view;
		if (viewport != null) { java.awt.Point position = id == null && overviewPosition != null ? overviewPosition : new java.awt.Point();
			SwingUtilities.invokeLater(() -> { if (view == returnedView && (actions == null || actions.current())) { viewport.setViewPosition(position); if (id == null) { restoreFocus(this); } } }); }
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
		browse = true; refresh.run();
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
	private void browse(PursuitOverview overview, PoweredMagic.Setup reference, long expectedView, Actions actions)
	{
		button(this, "Overview", () -> { if (valid(expectedView, actions)) { browse = false; refresh.run(); } });
		JComboBox<String> filter = new JComboBox<>(new String[]{"All", "Pursue", "Long-term", "Check"});
		filter.setName("browse-category"); filter.setSelectedItem(category); filter.setMaximumSize(new Dimension(213,28)); filter.setAlignmentX(LEFT_ALIGNMENT);
		filter.getAccessibleContext().setAccessibleName("Browse recommendation category");
		filter.addActionListener(event -> { if (valid(expectedView, actions)) { category = (String) filter.getSelectedItem(); refresh.run(); } }); add(filter);
		List<Advisor.UpgradeView> visible = overview.all.stream().filter(u -> category.equals("All")
			|| category.equals("Pursue") && u.pursuit.category == PursuitAssessment.Category.PURSUE
			|| category.equals("Long-term") && u.pursuit.category == PursuitAssessment.Category.LONG_TERM
			|| category.equals("Check") && (u.pursuit.category == PursuitAssessment.Category.CHECK || u.pursuit.category == PursuitAssessment.Category.PLAN)).collect(java.util.stream.Collectors.toList());
		text(this, visible.size() + " upgrades · " + category);
		BrowseGrid grid = new BrowseGrid(); grid.setName("browse-grid");
		for (Advisor.UpgradeView upgrade : visible) { upgradeCard(grid, upgrade, reference, expectedView, actions, true); } add(grid);
		if (visible.isEmpty()) { text(this, overview.all.isEmpty() ? "No remaining supported upgrade from these observations. Unknown is not absence; no priorities are added to fill the list." : "No supported outcome in this category. All retains conditional, specialist and small-gain choices."); }
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
	private void upgradeCard(JPanel parent, Advisor.UpgradeView upgrade, PoweredMagic.Setup setup,
		long expectedView, Actions actions, boolean tile)
	{
		JPanel card = new JPanel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setAlignmentX(LEFT_ALIGNMENT);
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR); card.setMaximumSize(new Dimension(213,Integer.MAX_VALUE));
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,1,1,1,ColorScheme.MEDIUM_GRAY_COLOR),BorderFactory.createEmptyBorder(6,6,6,6)));
		JPanel header=new JPanel(new java.awt.BorderLayout()); header.setOpaque(false); header.setMaximumSize(new Dimension(213,40)); header.setAlignmentX(LEFT_ALIGNMENT);
		JLabel icon = new JLabel("◇"); icon.setPreferredSize(new Dimension(40,40)); icon.setForeground(ColorScheme.TEXT_COLOR);
		icon.setToolTipText(upgrade.upgrade.title); icon.getAccessibleContext().setAccessibleName(upgrade.upgrade.title + " sprite");
		if (itemManager != null) { itemManager.getImage(upgrade.upgrade.itemId).addTo(icon); }
		header.add(icon, java.awt.BorderLayout.WEST);
		JButton open = new JButton("›"); open.setMargin(new java.awt.Insets(0,4,0,4)); open.setPreferredSize(new Dimension(24,28));
		open.setMaximumSize(new Dimension(24,28)); open.setToolTipText(upgrade.upgrade.title); open.setName("open-upgrade-" + upgrade.upgrade.id);
		open.getAccessibleContext().setAccessibleName("Open " + upgrade.upgrade.title + ", " + upgrade.pursuit.category.label);
		open.addActionListener(event -> navigate(upgrade.upgrade.id, expectedView, actions));
		open.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("ENTER"), "open-card");
		open.getActionMap().put("open-card", new javax.swing.AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent event) { open.doClick(); } });
		header.add(open,java.awt.BorderLayout.EAST);
		JPanel content=card;
		if (tile) { card.add(header); }
		else
		{
			card.setLayout(new CompactRowLayout()); card.add(icon,java.awt.BorderLayout.WEST);
			JPanel actionWell=new JPanel(new java.awt.GridBagLayout()); actionWell.setOpaque(false); actionWell.add(open); card.add(actionWell,java.awt.BorderLayout.EAST);
			content=new JPanel(); content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS)); content.setOpaque(false); card.add(content,java.awt.BorderLayout.CENTER);
		}
		text(content, upgrade.upgrade.title); content.getComponent(content.getComponentCount()-1).setFont(getFont().deriveFont(java.awt.Font.BOLD));
		ComparisonResult result = upgrade.comparison;
		String headline=result.before != null && result.available(ComparisonResult.Metric.MAX_HIT)
			? String.format(java.util.Locale.ROOT, "%+d max hit · %s reference", result.after.maxHit-result.before.maxHit, result.target.id.equals("graardor") ? "Graardor" : result.target.name)
			: upgrade.upgrade.role.equals("powered-magic") ? "Magic support · model gap" : upgrade.upgrade.role.equals("ranged-special") ? "Ruby special role" : "Hybrid boot slot";
		text(content, headline);
		text(content, upgrade.pursuit.category.label + (tile ? "" : " · " + upgrade.pursuit.burden.toString().toLowerCase(java.util.Locale.ROOT) + " work")
			+ (upgrade.pursuit.tail == PursuitAssessment.Tail.LONG && upgrade.pursuit.category != PursuitAssessment.Category.LONG_TERM ? " · Long tail" : ""));
		if (!tile && !upgrade.pursuit.blockers.isEmpty()) { text(content, "Next check: " + upgrade.pursuit.blockers.get(0)); }
		java.awt.event.MouseAdapter activate=new java.awt.event.MouseAdapter() { public void mouseClicked(java.awt.event.MouseEvent event) { if (valid(expectedView, actions)) { open.doClick(); } } };
		clickable(card,activate,open); parent.add(card);
	}
	private void clickable(java.awt.Container parent, java.awt.event.MouseAdapter listener, JButton open)
	{
		parent.addMouseListener(listener);
		for (java.awt.Component child : parent.getComponents())
		{ if (child == open) { continue; } if (child instanceof java.awt.Container) { clickable((java.awt.Container)child,listener,open); } else { child.addMouseListener(listener); } }
	}
	private void activityCard(ActivityOpportunity activity, long expectedView, Actions actions)
	{
		text(this, activity.title); text(this, "Useful families: " + String.join(" · ", activity.usefulFamilies));
		activitySprites(this, activity);
		JButton open=button(this, "Several useful rewards ›", () -> navigate("activity:" + activity.id, expectedView, actions)); open.setName("open-activity:" + activity.id);
		open.getAccessibleContext().setAccessibleName("Open " + activity.title + " shared rewards");
	}
	private void activityDetails(ActivityOpportunity activity, KnowledgeCatalog catalog, Actions actions, long expectedView)
	{
		text(this, activity.title + " · " + activity.category); text(this, activity.reason);
		activitySprites(this, activity);
		text(this, "Mode: " + activity.mode + ". Time not estimated: personal reward mechanics and cycle pace are unsupported.");
		for (String reward : activity.usefulRewards) { text(this, "Useful missing reward: " + reward); }
		for (String reward : activity.uncertainRewards) { text(this, "Check: " + reward); }
		for (String exclusion : activity.exclusions) { text(this, exclusion); }
		if (!activity.available) { text(this, "Check activity preparation/access in the individual Path before pursuing. This view does not certify readiness."); }
		if (!activity.allocations.isEmpty()) { text(this, "Exclusive resource choices: " + String.join(", ", activity.allocations)); }
		if (actions != null && actions.canEdit()) { button(this, "Skip this activity", () -> { if (valid(expectedView, actions)) { actions.skip(activity.activity,true); } }); }
		if (!activity.sources.isEmpty()) { sources(this,activity.sources,catalog); }
	}
	private void activitySprites(JPanel parent, ActivityOpportunity activity)
	{
		JPanel strip=new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,4,0)); strip.setOpaque(false);
		strip.setAlignmentX(LEFT_ALIGNMENT); strip.setMaximumSize(new Dimension(213,40)); strip.setPreferredSize(new Dimension(213,40));
		for (int id : activity.itemIds.stream().limit(4).collect(java.util.stream.Collectors.toList()))
		{
			JLabel icon=new JLabel("◇"); icon.setForeground(ColorScheme.TEXT_COLOR); icon.setPreferredSize(new Dimension(40,40));
			icon.getAccessibleContext().setAccessibleName("Representative useful reward; item " + id);
			if (itemManager != null) { itemManager.getImage(id).addTo(icon); } strip.add(icon);
		}
		parent.add(strip);
	}

	private void upgradeDetails(Advisor.UpgradeView view, AccountSnapshot a, Preferences p, KnowledgeCatalog catalog,
		Actions actions, PoweredMagic.Setup setup)
	{
		KnowledgeCatalog.Upgrade upgrade = view.upgrade;
		text(this, upgrade.title + "\n" + view.pursuit.category.label);
		text(this, view.pursuit.reasons.get(0));
		if (view.pursuit.alternative != null)
		{
			String alternative=catalog.upgrades.stream().filter(u -> u.id.equals(view.pursuit.alternative)).map(u -> u.title).findFirst().orElse(view.pursuit.alternative);
			text(this, "Consider first: " + alternative + ". Its remaining route qualifies independently; gains and resource allocations are not combined.");
		}
		if (!view.pursuit.blockers.isEmpty()) { text(this, "Next check: " + view.pursuit.blockers.get(0)); }
		long expectedView = this.view;
		JPanel tabs = new JPanel(new java.awt.GridLayout(1, 2, 4, 0)); tabs.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH - 12, 30));
		tabs.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 12, 30));
		tabs.setMinimumSize(new Dimension(PluginPanel.PANEL_WIDTH - 12, 30)); tabs.setAlignmentX(LEFT_ALIGNMENT);
		button(tabs, "Value", () -> { if (valid(expectedView, actions)) { pathDetail = false; refresh.run(); } });
		button(tabs, "Path", () -> { if (valid(expectedView, actions)) { pathDetail = true; refresh.run(); } }); add(tabs);
		if (pathDetail) { acquisition(view, catalog, actions); return; }
		text(this, "Why for your account: " + view.value);
		JPanel whyOrder = section(this, "Why this order");
		for (String reason : view.pursuit.reasons) { text(whyOrder, reason); }
		text(whyOrder, "Ranking " + view.pursuit.version + ": " + String.join(", ", view.pursuit.reasonCodes));
		for (String evidence : view.evidence) { text(whyOrder, evidence); }
		ComparisonResult comparison = view.comparison;
		if (comparison.beforeSetup != null && comparison.afterSetup != null)
		{
			String slot = catalog.comparison.items.stream().filter(i -> i.id == upgrade.itemId).map(i -> i.slot).findFirst().orElse("weapon");
			itemSummary(this, "Before", comparison.beforeSetup.slots.get(slot), catalog);
			itemSummary(this, "After", comparison.afterSetup.slots.get(slot), catalog);
			if (!comparison.beforeSetup.slots.get("shield").equals(comparison.afterSetup.slots.get("shield")))
			{ text(this, "Off-hand removed for a two-handed replacement."); }
		}
		if (comparison.before == null) { text(this, "Comparison unavailable: " + comparison.reason); }
		if (comparison.before != null)
		{
			text(this, "Max hit  " + comparison.before.maxHit + " → " + comparison.after.maxHit + "  ("
				+ String.format(java.util.Locale.ROOT, "%+d", comparison.after.maxHit - comparison.before.maxHit) + ")");
			text(this, comparison.available(ComparisonResult.Metric.HIT_CHANCE) ? String.format(java.util.Locale.ROOT,
				"Hit chance  %.1f%% → %.1f%%", comparison.before.hitChance * 100, comparison.after.hitChance * 100) : "Hit chance  Unsupported for this history mechanic");
			text(this, comparison.available(ComparisonResult.Metric.THEORETICAL_DPS) ? String.format(java.util.Locale.ROOT,
				"Theoretical DPS  %.2f → %.2f  (%+.2f)", comparison.before.dps, comparison.after.dps, comparison.dpsGain()) : "Theoretical DPS  Unsupported for this history mechanic");
			text(this, comparison.target.name + " · " + setup.prayer + " · " + setup.boost + ". Reference only.");
		}
		if (!upgrade.comparison.equals("NONE"))
		{
			JPanel assumptions = section(this, "Reference setup and assumptions");
			for (String assumption : comparison.assumptions) { text(assumptions, assumption); }
			if (comparison.before == null && setup != null) { text(assumptions, "Selected slots: " + setup.slots + ". Zero denotes an explicitly empty reference slot. Prayer " + setup.prayer + ", boost " + setup.boost + ", style " + setup.style + "."); }
			if (actions != null && actions.canEdit() && setup != null) { referenceControls(assumptions, a, setup, actions, view.referenceChoices, comparison.before != null); }
		}
		text(this, "Alternative: " + upgrade.alternatives); text(this, "Limits: " + upgrade.limitation);
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
		sources(this, upgrade.sources, catalog);
	}

	private void itemSummary(JPanel parent, String label, int id, KnowledgeCatalog catalog)
	{
		JLabel icon = new JLabel(label); icon.setForeground(ColorScheme.TEXT_COLOR);
		if (id > 0 && itemManager != null) { itemManager.getImage(id).addTo(icon); } parent.add(icon);
		text(parent, id == 0 ? "Deliberately empty slot" : catalog.comparison.items.stream().filter(i -> i.id == id).map(i -> i.label).findFirst().orElse("Unknown reference"));
	}

	private void acquisition(Advisor.UpgradeView view, KnowledgeCatalog catalog, Actions actions)
	{
		KnowledgeCatalog.Upgrade upgrade = view.upgrade;
		text(this, "Next: " + (view.pursuit.blockers.isEmpty() ? view.routes.stream().filter(r -> r.decision.id.equals(view.pursuit.route)).findFirst().orElse(view.routes.get(0)).next : view.pursuit.blockers.get(0)));
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
		sources(this, upgrade.sources, catalog);
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

	private void referenceControls(JPanel parent, AccountSnapshot a, PoweredMagic.Setup setup, Actions actions, Map<String, Map<String, String>> referenceChoices, boolean supported)
	{
		text(parent, "Reference only. Empty slots are deliberate exclusions, not zero-valued unknown account gear. Prayer/boost are assumptions; quantities and remaining charges are not measured.");
		button(parent, "Use minimal template", () -> actions.reference("template", "true"));
		for (String slot : PoweredMagic.SLOTS)
		{
			JPanel destination = List.of("weapon", "neck", "feet").contains(slot) ? parent : section(parent, "Change " + slot);
			JComboBox<Choice> choice = choices(referenceChoices.getOrDefault(slot, Map.of()), false); select(choice, Integer.toString(setup.slots.get(slot)));
			choice.setName("reference-" + slot);
			destination.add(choice);
			JButton apply = button(destination, "Apply " + slot, () -> { if (choice.getSelectedItem() != null) { actions.reference(slot, ((Choice) choice.getSelectedItem()).id); } });
			apply.setEnabled(choice.getItemCount() > 0);
		}
		for (String key : List.of("prayer", "boost", "style", "target"))
		{
			Map<String, String> options = new java.util.LinkedHashMap<>();
			if (key.equals("target")) { options.putAll(referenceChoices.getOrDefault("target", Map.of())); }
			if (key.equals("prayer")) { for (PoweredMagic.Prayer value : PoweredMagic.Prayer.values()) { options.put(value.name(), value.name()); } }
			if (key.equals("boost")) { for (PoweredMagic.Boost value : PoweredMagic.Boost.values()) { options.put(value.name(), value.name()); } }
			if (key.equals("style")) { for (PoweredMagic.Style value : PoweredMagic.Style.values()) { options.put(value.name(), value.name()); } }
			JComboBox<Choice> choice = choices(options, false); select(choice, key.equals("prayer") ? setup.prayer.name() : key.equals("boost") ? setup.boost.name() : key.equals("target") ? setup.target : setup.style.name());
			choice.setName("reference-" + key);
			parent.add(choice); button(parent, "Apply " + key, () -> actions.reference(key, ((Choice) choice.getSelectedItem()).id));
		}
		if (supported) { button(parent, "Confirm reference", () -> actions.reference("confirm", "true")); }
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
		JPanel details = new JPanel(); details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS)); details.setVisible(false);
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

	private static JButton button(JPanel panel, String label, Runnable action)
	{
		JButton button = new JButton(label);
		if (label.length() > 24)
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
	private static void text(JPanel panel, String message)
	{
		JTextArea text = new JTextArea(message)
		{
			@Override public Dimension getPreferredSize()
			{
				int width = getParent() == null || getParent().getWidth() <= 0 ? 201 : Math.max(1, getParent().getWidth() - getParent().getInsets().left - getParent().getInsets().right);
				// BoxLayout caps the allocated width even when the native viewport is wider.
				// Measure wrapping at that same width so detail paragraphs cannot lose lines.
				width = Math.min(width, getMaximumSize().width);
				java.awt.Insets insets = getInsets();
				javax.swing.text.View view = getUI().getRootView(this);
				// Swing reserves one pixel for the document caret when wrapping.
				view.setSize(Math.max(1, width - insets.left - insets.right - 1), Integer.MAX_VALUE);
				return new Dimension(width, (int) Math.ceil(view.getPreferredSpan(javax.swing.text.View.Y_AXIS))
					+ insets.top + insets.bottom);
			}
		}; text.setLineWrap(true); text.setWrapStyleWord(true);
		text.setMinimumSize(new Dimension(0,0));
		text.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH - 12, Integer.MAX_VALUE));
		text.setEditable(false); text.setOpaque(false);
		text.setForeground(ColorScheme.TEXT_COLOR);
		text.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); text.setAlignmentX(LEFT_ALIGNMENT);
		panel.add(text);
	}
}
