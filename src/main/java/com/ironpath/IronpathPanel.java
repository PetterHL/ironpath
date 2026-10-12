package com.ironpath;

import com.ironpath.advisor.Advisor.Recommendation;
import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.LinkBrowser;

final class IronpathPanel extends PluginPanel
{
	void render(String status, AccountSnapshot snapshot, List<Recommendation> advice, KnowledgeCatalog catalog)
	{
		if (!SwingUtilities.isEventDispatchThread()) { throw new IllegalStateException("Swing thread required"); }
		removeAll();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		text(this, "Ironpath — Your path to endgame PvM");
		text(this, "T1 experiment: five CoX preparation samples. Standard Ironman, permanent members worlds. Ordinary teams; limited gear variants. Other account goals are outside coverage.");
		text(this, status);
		if (catalog != null)
		{
			text(this, "Knowledge " + catalog.version + " · reviewed " + catalog.reviewed);
			if (ChronoUnit.DAYS.between(catalog.reviewed, LocalDate.now()) > 90)
			{
				text(this, "Catalogue review is over 90 days old. Check the linked sources before committing to a plan.");
			}
		}
		if (snapshot != null)
		{
			text(this, "Real levels: Prayer " + level(snapshot, "PRAYER") + ", Defence " + level(snapshot, "DEFENCE")
				+ ", Ranged " + level(snapshot, "RANGED") + ", Magic " + level(snapshot, "MAGIC") + ". " + snapshot.levels.evidence());
			text(this, "Rigour: " + unlock(snapshot.rigour) + "; Augury: " + unlock(snapshot.augury) + ". Unlocks read from client varbits.");
			text(this, "Inventory: " + snapshot.inventory.evidence() + "\nEquipment: " + snapshot.equipment.evidence());
			text(this, snapshot.bank.known() ? "Bank last observed this session: " + snapshot.bank.time()
				: "Bank unknown. Open your bank to observe it, then return here.");
			text(this, "Only observed storage is covered. Not observed does not mean never acquired. Stored gear may need charges, ammunition or unnoting; check before use.");
			if (advice.isEmpty())
			{
				text(this, "No supported suggestion from these five samples. Observe your bank if needed. Unlocks, unrecognised variants and other storage remain unknown; no missing gear is inferred.");
			}
			for (Recommendation recommendation : advice) { card(recommendation, catalog); }
		}
		text(this, "Local evaluation. Observations are memory-only and cleared on account transitions. Source buttons open your browser with a fixed source URL and no account data.");
		revalidate();
		repaint();
	}

	private void card(Recommendation recommendation, KnowledgeCatalog catalog)
	{
		KnowledgeCatalog.Decision decision = recommendation.decision;
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0), BorderFactory.createEtchedBorder()));
		text(card, decision.title);
		text(card, decision.why);
		JButton expand = new JButton("Why and alternatives");
		card.add(expand);
		JPanel details = new JPanel();
		details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
		for (String evidence : recommendation.evidence) { text(details, "Observed: " + evidence); }
		text(details, "Access / requirements: " + decision.access);
		text(details, "Alternative: " + decision.alternative);
		text(details, "Trade-off: " + decision.tradeoff);
		text(details, "Effort: " + decision.effort);
		text(details, "Selection: owned scroll actions first, then confirmed training gaps, then team planning; at most one training action and one choice per family. When both unlocked prayers have level gaps, Rigour's lower threshold is prioritised before Augury. This is authored ordering, not an efficiency score.");
		text(details, "Sample review: " + decision.reviewed);
		for (String id : decision.sources)
		{
			KnowledgeCatalog.Source source = catalog.sources.get(id);
			JButton link = new JButton("Source: " + id);
			link.addActionListener(event -> LinkBrowser.browse(source.url));
			details.add(link);
		}
		details.setVisible(false);
		expand.addActionListener(event -> { details.setVisible(!details.isVisible()); revalidate(); });
		card.add(details);
		add(card);
	}

	private static String level(AccountSnapshot account, String skill)
	{
		return account.level(skill) == null ? "unknown" : account.level(skill).toString();
	}

	private static String unlock(Observation<Boolean> fact)
	{
		return !fact.known() ? "unknown" : fact.value() ? "unlocked" : "not unlocked";
	}

	private static void text(JPanel panel, String message)
	{
		JTextArea text = new JTextArea(message);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setEditable(false);
		text.setOpaque(false);
		text.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		text.setAlignmentX(LEFT_ALIGNMENT);
		panel.add(text);
	}
}
