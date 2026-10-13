package com.ironpath;

import com.ironpath.advisor.Advisor;
import com.ironpath.advisor.Advisor.Recommendation;
import com.ironpath.advisor.KnowledgeCatalog;
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
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.LinkBrowser;

final class IronpathPanel extends PluginPanel
{
	interface Actions
	{
		void goal(String id);
		void skip(String id, boolean skipped);
		void confirm(String key, Boolean value);
		void clear();
	}

	void render(String status, AccountSnapshot snapshot, List<Recommendation> advice, KnowledgeCatalog catalog,
		Advisor.GoalView goal, Preferences preferences, Actions actions, String storageStatus, List<String> reasons)
	{
		if (!SwingUtilities.isEventDispatchThread()) { throw new IllegalStateException("Swing thread required"); }
		removeAll();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		text(this, "Ironpath — Your path to endgame PvM");
		text(this, "PvM progression · CG, Zulrah, ToA, Slayer, Moons, Royal Titans and ordinary team CoX. Standard Ironman; limited variants. Live validation is incomplete.");
		text(this, status);
		if (catalog != null)
		{
			text(this, "Knowledge " + catalog.version + " · reviewed " + catalog.reviewed);
			if (ChronoUnit.DAYS.between(catalog.reviewed, LocalDate.now()) > 90)
			{ text(this, "Catalogue review is over 90 days old. Check the sources before committing to a plan."); }
		}
		if (snapshot != null)
		{
			text(this, snapshot.bank.known() ? "Bank last observed this session: " + snapshot.bank.time()
				: "Bank unknown. Open your bank to observe it, then return here.");
			JPanel observations = section(this, "Account observations");
			text(observations, "Real levels: Prayer " + level(snapshot, "PRAYER") + ", Defence " + level(snapshot, "DEFENCE")
				+ ", Ranged " + level(snapshot, "RANGED") + ", Magic " + level(snapshot, "MAGIC") + ". " + snapshot.levels.evidence());
			text(observations, "Rigour: " + unlock(snapshot.rigour) + "; Augury: " + unlock(snapshot.augury) + ". Client varbits.");
			snapshot.facts.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e ->
				text(observations, catalog.factChoices().get(e.getKey()) + ": " + unlock(e.getValue()) + ". " + e.getValue().evidence()));
			text(observations, "Inventory: " + snapshot.inventory.evidence() + "\nEquipment: " + snapshot.equipment.evidence());
			text(observations, "Only observed storage is covered. Not observed does not mean never acquired. Check charges, ammunition, unnoting, access and supplies before use.");
			text(this, storageStatus);
			if (actions != null) { controls(catalog, preferences, snapshot, actions); }
			if (goal != null) { goal(goal, catalog, actions); }
			if (advice.isEmpty())
			{
				text(this, "No supported available suggestion. Unknown facts do not establish a missing upgrade.");
				if (!reasons.isEmpty())
				{
					JPanel explanation = section(this, "Why no suggestion");
					for (String reason : reasons) { text(explanation, reason); }
				}
			}
			for (Recommendation recommendation : advice) { card(recommendation, catalog, actions); }
		}
		text(this, "Local evaluation. Observations clear on account transitions. Only your goal, skips and manual confirmations are saved locally for this character/profile. Source links carry no account data.");
		revalidate(); repaint();
	}

	private void controls(KnowledgeCatalog catalog, Preferences preferences, AccountSnapshot account, Actions actions)
	{
		text(this, "Optional goal (one at a time)");
		JComboBox<Choice> goals = choices(Advisor.goalChoices(catalog), true);
		select(goals, preferences.goal);
		add(goals);
		button(this, "Set goal", () -> actions.goal(((Choice) goals.getSelectedItem()).id));
		JPanel choices = section(this, "Skips and manual confirmations");
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

	private void goal(Advisor.GoalView goal, KnowledgeCatalog catalog, Actions actions)
	{
		text(this, "Goal: " + goal.title + "\n" + goal.status);
		if (goal.nearestMissing != null) { text(this, "Nearest confirmed missing prerequisite: " + goal.nearestMissing); }
		text(this, "Next: " + goal.nextAction);
		JPanel requirements = section(this, "Goal prerequisites and sources");
		for (Advisor.Requirement requirement : goal.requirements)
		{
			text(requirements, requirement.role + " · " + (requirement.truth == Truth.TRUE ? "Confirmed" : requirement.truth == Truth.FALSE ? "Missing" : "Unknown")
				+ (requirement.skipped ? " / skipped" : "") + ": " + requirement.label + ". " + requirement.evidence);
		}
		text(requirements, "Reference route access / requirements: " + goal.decision.access);
		text(requirements, "Alternatives have their own access. Suggested preparation is an authored planning baseline, not a game entry requirement.");
		text(requirements, "Alternative: " + goal.decision.alternative);
		sources(requirements, goal.decision, catalog);
		if (actions != null) { button(this, "Clear goal", () -> actions.goal(null)); }
	}

	private void card(Recommendation recommendation, KnowledgeCatalog catalog, Actions actions)
	{
		KnowledgeCatalog.Decision decision = recommendation.decision;
		JPanel card = new JPanel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
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
		add(card);
	}

	private void sources(JPanel panel, KnowledgeCatalog.Decision decision, KnowledgeCatalog catalog)
	{
		JPanel references = section(panel, "Reviewed sources (" + decision.sources.size() + ")");
		for (String id : decision.sources)
		{
			KnowledgeCatalog.Source source = catalog.sources.get(id);
			button(references, "Source: " + id, () -> LinkBrowser.browse(source.url));
		}
	}

	private JPanel section(JPanel parent, String label)
	{
		JPanel details = new JPanel(); details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS)); details.setVisible(false);
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

	private static void button(JPanel panel, String label, Runnable action)
	{
		JButton button = new JButton(label);
		button.setToolTipText(label);
		button.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH - 12, 28));
		button.setAlignmentX(LEFT_ALIGNMENT);
		button.addActionListener(event -> action.run()); panel.add(button);
	}
	private static String level(AccountSnapshot account, String skill) { return account.level(skill) == null ? "unknown" : account.level(skill).toString(); }
	private static String unlock(Observation<Boolean> fact) { return !fact.known() ? "unknown" : fact.value() ? "unlocked" : "not unlocked"; }
	private static void text(JPanel panel, String message)
	{
		JTextArea text = new JTextArea(message); text.setLineWrap(true); text.setWrapStyleWord(true);
		text.setEditable(false); text.setOpaque(false);
		text.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); text.setAlignmentX(LEFT_ALIGNMENT);
		panel.add(text);
	}
}
