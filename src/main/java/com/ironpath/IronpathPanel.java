package com.ironpath;

import com.ironpath.advisor.Advisor;
import com.ironpath.advisor.Advisor.Recommendation;
import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.advisor.PoweredMagic;
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
		if (!SwingUtilities.isEventDispatchThread()) { throw new IllegalStateException("Swing thread required"); }
		long currentView = ++view;
		refresh = () -> render(status, snapshot, advice, catalog, goal, preferences, actions, storageStatus, reasons, upgrades, reference);
		if (snapshot == null || upgrades.stream().noneMatch(u -> u.upgrade.id.equals(detail))) { detail = null; }
		removeAll(); setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); setBackground(ColorScheme.DARK_GRAY_COLOR);
		text(this, "Ironpath — Your path to endgame PvM");
		text(this, status);
		if (catalog != null)
		{
			text(this, "Knowledge " + catalog.version + " · reviewed " + catalog.reviewed);
			if (ChronoUnit.DAYS.between(catalog.reviewed, LocalDate.now()) > 90)
			{ text(this, "Catalogue review is over 90 days old. Check sources before committing to a plan."); }
		}
		if (snapshot != null && catalog != null)
		{
			text(this, snapshot.bank.known() ? "Bank observed at " + java.time.format.DateTimeFormatter.ofPattern("HH:mm 'UTC'")
				.withZone(java.time.ZoneOffset.UTC).format(snapshot.bank.time()) + " this session."
				: "Bank unknown. Positive equipment/inventory can still support provisional advice.");
			PoweredMagic model = new PoweredMagic(catalog);
			Advisor.UpgradeView selected = upgrades.stream().filter(u -> u.upgrade.id.equals(detail)).findFirst().orElse(null);
			if (selected != null)
			{
				button(this, "Back to upgrades", () -> navigate(null, currentView, actions));
				upgradeDetails(selected, snapshot, preferences, catalog, actions, reference, model);
			}
			else
			{
				if (upgrades.isEmpty()) { text(this, "No supported substantial upgrade in this slice. Observe a supported magic weapon or boot/style baseline. Bowfa alone does not establish specialist need; unknown ownership is not a shopping list. Existing broad advice is below."); }
				for (Advisor.UpgradeView upgrade : upgrades.subList(0, Math.min(3, upgrades.size())))
				{ upgradeCard(this, upgrade, snapshot, preferences, reference, model, currentView, actions); }
				if (upgrades.size() > 3)
				{
					JPanel more = section(this, "More supported upgrades");
					for (Advisor.UpgradeView upgrade : upgrades.subList(3, upgrades.size()))
					{ upgradeCard(more, upgrade, snapshot, preferences, reference, model, currentView, actions); }
				}
				List<Recommendation> quick = new Advisor(catalog).quickActions(snapshot, preferences);
				if (!quick.isEmpty())
				{
					JPanel quickPanel = section(this, "Quick actions");
					for (Recommendation recommendation : quick) { card(quickPanel, recommendation, catalog, actions); }
				}
				JPanel broad = section(this, "Existing broad PvM advice");
				text(broad, "Reviewed activity/preparation advice across CG, Zulrah, ToA, Slayer, Moons, Titans and ordinary team CoX. These cases have no calculated upgrade benefit.");
				for (Recommendation recommendation : advice) { card(broad, recommendation, catalog, actions); }
				JPanel moreActivities = section(broad, "More available activities");
				for (Recommendation recommendation : new Advisor(catalog).availableActivities(snapshot, preferences))
				{
					if (advice.stream().noneMatch(r -> r.decision.id.equals(recommendation.decision.id)))
					{ card(moreActivities, recommendation, catalog, actions); }
				}
				for (String reason : reasons) { text(broad, reason); }
				JPanel account = section(this, "Account and preferences");
				observations(account, snapshot, catalog);
				text(account, storageStatus);
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
		detail = id; refresh.run();
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

	private void upgradeCard(JPanel parent, Advisor.UpgradeView upgrade, AccountSnapshot a, Preferences p,
		PoweredMagic.Setup setup, PoweredMagic model, long expectedView, Actions actions)
	{
		JPanel card = new JPanel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0), BorderFactory.createEtchedBorder()));
		JLabel icon = new JLabel("◇"); icon.setToolTipText(upgrade.upgrade.title); icon.setForeground(ColorScheme.TEXT_COLOR);
		if (itemManager != null) { itemManager.getImage(upgrade.upgrade.itemId).addTo(icon); }
		card.add(icon); text(card, upgrade.upgrade.title);
		text(card, (upgrade.questions.isEmpty() ? "Planning gap confirmed" : "Ownership unconfirmed")
			+ (upgrade.routes.stream().noneMatch(r -> r.available) ? " · Plan ahead" : "")
			+ (upgrade.equip.stream().anyMatch(r -> r.truth != Truth.TRUE) ? " · Needs equip levels" : "")
			+ (upgrade.band == KnowledgeCatalog.Benefit.MINOR ? " · Contextual / defer" : ""));
		PoweredMagic.Result result = model.compare(a, p, setup, upgrade.upgrade.comparison);
		text(card, result.before == null ? upgrade.upgrade.headline : "Reference max hit " + result.before.maxHit + " → " + result.after.maxHit
			+ "; theoretical DPS " + String.format(java.util.Locale.ROOT, "%.2f → %.2f", result.before.dps, result.after.dps)
			+ " at ordinary Dharok. " + (setup.confirmed ? "Confirmed reference." : "Proposed reference; correct in details."));
		if (!upgrade.applications.isEmpty()) { text(card, "Contexts: " + upgrade.upgrade.applications.stream()
			.filter(application -> upgrade.applications.contains(application.text)).map(application -> application.label).collect(java.util.stream.Collectors.joining(" · "))); }
		text(card, "Remaining: " + upgrade.upgrade.effortShort);
		JButton open = button(card, "Open details", () -> navigate(upgrade.upgrade.id, expectedView, actions));
		open.setName("open-upgrade-" + upgrade.upgrade.id); parent.add(card);
	}

	private void upgradeDetails(Advisor.UpgradeView view, AccountSnapshot a, Preferences p, KnowledgeCatalog catalog,
		Actions actions, PoweredMagic.Setup setup, PoweredMagic model)
	{
		KnowledgeCatalog.Upgrade upgrade = view.upgrade;
		text(this, upgrade.title + "\n" + view.label); text(this, view.value);
		for (String evidence : view.evidence) { text(this, "Baseline evidence: " + evidence); }
		PoweredMagic.Result comparison = model.compare(a, p, setup, upgrade.comparison);
		text(this, comparison.summary());
		if (!upgrade.comparison.equals("NONE"))
		{
			JPanel assumptions = section(this, "Reference setup and assumptions");
			for (String assumption : comparison.assumptions) { text(assumptions, assumption); }
			if (comparison.before == null && setup != null) { text(assumptions, "Selected slots: " + setup.slots + ". Zero denotes an explicitly empty reference slot. Prayer " + setup.prayer + ", boost " + setup.boost + ", style " + setup.style + "."); }
			if (actions != null && actions.canEdit() && setup != null) { referenceControls(assumptions, a, setup, actions, model, comparison.before != null); }
		}
		text(this, "Alternative: " + upgrade.alternatives); text(this, "Limits: " + upgrade.limitation);
		text(this, "Remaining work: " + upgrade.remainingWork + " Review: " + upgrade.reviewed + ".");
		for (String application : view.applications) { text(this, application); }
		java.util.Set<String> confirmationKeys = new java.util.LinkedHashSet<>();
		if (upgrade.ownedWhen.test == KnowledgeCatalog.Test.POSSESSION) { confirmationKeys.add("owns-" + upgrade.ownedWhen.key); }
		for (String set : upgrade.unknownSets) { confirmationKeys.add("owns-" + set); }
		confirmationKeys.addAll(view.questions);
		for (String key : confirmationKeys)
		{
			text(this, "Targeted ownership check: " + catalog.factChoices().get(key) + "? "
				+ (p.confirmations.containsKey(key) ? p.confirmations.get(key).evidence() : "Unconfirmed outside observed storage.")
				+ " Positive observed/manual possession takes precedence over an absence.");
			if (actions != null)
			{
				button(this, "Already owned", () -> actions.confirm(key, true));
				button(this, "Confirm not owned", () -> actions.confirm(key, false));
				button(this, "Clear confirmation", () -> actions.confirm(key, null));
			}
		}
		text(this, "Equip requirements (separate from acquisition)"); requirements(this, view.equip);
		if (!view.parts.isEmpty()) { text(this, "Owned parts and additions — optional steps are labelled"); requirements(this, view.parts); }
		text(this, "Acquisition paths — each outcome is grouped once");
		for (Advisor.RouteView route : view.routes)
		{
			text(this, route.decision.title + "\n" + route.status + "\nNext: " + route.next);
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

	private void referenceControls(JPanel parent, AccountSnapshot a, PoweredMagic.Setup setup, Actions actions, PoweredMagic model, boolean supported)
	{
		text(parent, "Reference only. Empty slots are deliberate exclusions, not zero-valued unknown account gear. Prayer/boost are assumptions; quantities and remaining charges are not measured.");
		button(parent, "Use minimal template", () -> actions.reference("template", "true"));
		for (String slot : List.of("weapon", "neck", "feet"))
		{
			JComboBox<Choice> choice = choices(model.choices(slot, a), false); select(choice, Integer.toString(setup.slots.get(slot)));
			choice.setName("reference-" + slot);
			parent.add(choice);
			JButton apply = button(parent, "Apply " + slot, () -> { if (choice.getSelectedItem() != null) { actions.reference(slot, ((Choice) choice.getSelectedItem()).id); } });
			apply.setEnabled(choice.getItemCount() > 0);
		}
		for (String key : List.of("prayer", "boost", "style"))
		{
			Map<String, String> options = new java.util.LinkedHashMap<>();
			if (key.equals("prayer")) { for (PoweredMagic.Prayer value : PoweredMagic.Prayer.values()) { options.put(value.name(), value.name()); } }
			if (key.equals("boost")) { for (PoweredMagic.Boost value : PoweredMagic.Boost.values()) { options.put(value.name(), value.name()); } }
			if (key.equals("style")) { for (PoweredMagic.Style value : PoweredMagic.Style.values()) { options.put(value.name(), value.name()); } }
			JComboBox<Choice> choice = choices(options, false); select(choice, key.equals("prayer") ? setup.prayer.name() : key.equals("boost") ? setup.boost.name() : setup.style.name());
			choice.setName("reference-" + key);
			parent.add(choice); button(parent, "Apply " + key, () -> actions.reference(key, ((Choice) choice.getSelectedItem()).id));
		}
		if (supported) { button(parent, "Confirm reference", () -> actions.reference("confirm", "true")); }
	}

	private void requirements(JPanel parent, List<Advisor.Requirement> requirements)
	{
		for (Advisor.Requirement requirement : requirements)
		{ text(parent, requirement.role + " · " + (requirement.truth == Truth.TRUE ? "Confirmed for planning" : requirement.truth == Truth.FALSE ? "Confirmed gap" : "Unknown")
			+ ": " + requirement.label + ". " + requirement.evidence); }
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
				+ (requirement.skipped ? " / skipped" : "") + ": " + requirement.label + ". " + requirement.evidence);
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
		button.addActionListener(event -> action.run()); panel.add(button); return button;
	}
	private static String level(AccountSnapshot account, String skill) { return account.level(skill) == null ? "unknown" : account.level(skill).toString(); }
	private static String unlock(Observation<Boolean> fact) { return !fact.known() ? "unknown" : fact.value() ? "unlocked" : "not unlocked"; }
	private static void text(JPanel panel, String message)
	{
		JTextArea text = new JTextArea(message); text.setLineWrap(true); text.setWrapStyleWord(true);
		text.setColumns(20);
		text.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH - 12, Integer.MAX_VALUE));
		text.setEditable(false); text.setOpaque(false);
		text.setForeground(ColorScheme.TEXT_COLOR);
		text.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); text.setAlignmentX(LEFT_ALIGNMENT);
		panel.add(text);
	}
}
