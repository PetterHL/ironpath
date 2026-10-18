package com.ironpath;

import com.ironpath.dev.PanelPreview;
import com.ironpath.dev.ReviewReport;
import com.ironpath.encounter.EncounterSummary.Outcome;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

/** Compact end-to-end scenarios reuse the real lifecycle, evaluator and panel regression paths. */
final class OfflineLifecycleScenarios
{
	static final List<String> IDS = List.of("nex-unknown", "nex-incomplete", "nex-bank-trip",
		"nex-identity-pending", "nex-reset-pending", "nex-frozen-history", "nex-bank-long-trip", "nex-arena-reload");

	private OfflineLifecycleScenarios() { }
	static void run(String id, Path output, ReviewReport report) throws Exception
	{
		report.section("Pipeline", "Mocked public RuneLite state/events → actual IronpathPlugin handlers, AccountSnapshotReader, "
			+ "NexObserver/capture/session and in-memory PreferenceIO → production owned evaluator on a real worker "
			+ "→ controlled client callback queue → plugin-owned NexPanel on the Swing EDT. No completed-summary injection. "
			+ "Handlers are invoked directly; RuneLite event-bus dispatch is not exercised.");
		report.section("Scenario and provenance", "`" + id + "`. Reported gear reuses NexOwnedFixtures. All levels (99 real; "
			+ "118 attack/strength, 112 ranged visible), Rigour/Piety, unlocks, three visible players, stock and callbacks "
			+ "are synthetic. Preferences are read from memory; writes fail. Bank containers and quest scripts are unread. "
			+ "Default resources: 100% energy, two active Death Charge charges, 500 quiver amethyst arrows, 200 splinters, "
			+ "200 carried dragon arrows. Unknown scenario explicitly substitutes unread counters/containers. "
			+ "Only frozen in-memory summaries cross the bank trip; this is not saved combat history.");
		for (int font : List.of(12, 20))
		{
			report.section("Replay at " + font + "px", "Each font starts with a fresh plugin, queues, identity, account and session.");
			try (var replay = new OfflinePluginReplay(font))
			{
				if (font == 12) { OfflineReview.versions(report, replay.catalog); }
				try
				{
					switch (id)
					{
						case "nex-unknown": unknown(replay, output, font, report); break;
						case "nex-incomplete": incomplete(replay, output, font, report); break;
						case "nex-bank-trip": banking(replay, output, font, report); break;
						case "nex-identity-pending": pending(replay, output, font, report, true); break;
						case "nex-reset-pending": pending(replay, output, font, report, false); break;
						case "nex-frozen-history": frozen(replay, output, font, report); break;
						case "nex-bank-long-trip": longBanking(replay, output, font, report); break;
						case "nex-arena-reload": arenaReload(replay, output, font, report); break;
						default: throw new IllegalArgumentException("Unknown lifecycle scenario: " + id);
					}
					diagnostics(replay,output,font,report);
				}
				finally
				{
					evidence(replay, font, report);
					report.section("Ordered callbacks · " + font + "px", "```text\n" + String.join("\n", replay.trace) + "\n```");
				}
			}
		}
	}

	private static void unknown(OfflinePluginReplay r, Path output, int font, ReviewReport report) throws Exception
	{
		r.inventoryReadable = false;
		r.energy = r.charge = r.cooldown = r.ammoId = r.ammoQuantity = r.splinters = r.quiverCharged = -1;
		var summary = r.fight(20, 20, 60, true); r.settle();
		report.check("Unknown inputs still permit correlated completion", Outcome.COMPLETE, summary.outcome);
		report.check("Unread bank stays unknown", false, r.plugin.session.snapshot().bank.known());
		report.check("Unread inventory remains null in captured states", true, summary.timeline.stream().allMatch(s -> s.state.inventory == null));
		var result = r.plugin.ownedResult;
		report.check("No fabricated bank preparation", false, result.choices.stream().anyMatch(c -> c.bankPreparation));
		report.check("Ranged states with unread ammo have no numerical comparison", true,
			result.choices.stream().anyMatch(c -> c.actual.role.equals("ranged"))
			&& result.choices.stream().filter(c -> c.actual.role.equals("ranged")).noneMatch(c -> c.supported()));
		report.check("Independent supported melee remains available", true,
			result.choices.stream().anyMatch(c -> c.actual.role.equals("melee") && c.supported() && c.comparison.dpsGain() == 0));
		report.check("Unread first energy is not zero", null, result.resources.firstEnergy);
		report.check("Unread last energy is not zero", null, result.resources.lastEnergy);
		report.check("Unread charge does not establish improved active use", false, result.resources.improvedActiveObserved);
		report.check("Unknown energy changes carry an explicit gap", true, result.resources.gaps.stream().anyMatch(g -> g.contains("energy readings are unavailable")));
		capture(r, output, "main", font, report);
		onEdt(() -> {
			PanelPreview.click(r.panel, "Owned setup comparisons");
			PanelPreview.combo(r.panel, "nex-owned-support-filter").setSelectedItem("Unavailable comparisons");
			PanelPreview.combo(r.panel, "nex-owned-role-filter").setSelectedItem("ranged");
			report.check("Unknown ammo has visible scoped explanation", true, PanelPreview.text(r.panel).contains("quiver") || PanelPreview.text(r.panel).contains("ammunition"));
		});
		capture(r, output, "unknown-ammo", font, report);
		onEdt(() -> {
			PanelPreview.click(r.panel, "Special-attack resources"); PanelPreview.click(r.panel, "Resource readings and rules");
			report.check("Unknown energy is displayed as unread", true, PanelPreview.text(r.panel).contains("First recorded energy unread · last recorded unread"));
			report.check("Unknown Death Charge is displayed as unread", true, PanelPreview.text(r.panel).contains("Death Charge active unread"));
		});
		capture(r, output, "unknown-resources", font, report);
	}

	private static void incomplete(OfflinePluginReplay r, Path output, int font, ReviewReport report) throws Exception
	{
		var summary = r.fight(12, 20, 60, false); r.settle();
		report.check("Exit without corroboration remains censored", Outcome.CENSORED, summary.outcome);
		report.check("Excluded attempt reason", "Arena left before correlated completion", summary.reason);
		report.check("Incomplete kill does not enter complete averages", 0, r.plugin.nexSession.stats().n);
		report.check("One excluded attempt retained", 1, r.plugin.nexSession.stats().censored);
		report.check("No last complete summary", null, r.plugin.nexSession.stats().last);
		report.check("No complete timing mean", true, Double.isNaN(r.plugin.nexSession.stats().meanSeconds));
		report.check("No post-kill comparison manufactured", null, r.plugin.ownedResult);
		onEdt(() -> report.check("Incomplete result is visible", true, PanelPreview.text(r.panel).contains("Last attempt · CENSORED")));
		capture(r, output, "excluded", font, report);
		onEdt(() -> PanelPreview.click(r.panel, "Last attempt details"));
		capture(r, output, "excluded-details", font, report);
	}

	private static void banking(OfflinePluginReplay r, Path output, int font, ReviewReport report) throws Exception
	{
		var first = r.fight(20, 20, 60, true); r.settle();
		report.check("First bank-trip kill is complete", Outcome.COMPLETE, first.outcome);
		r.loading(); r.edt();
		report.check("Scene transition suspends history", true, r.plugin.nexSession.suspended());
		report.check("Suspended history is inaccessible", 0, r.plugin.nexSession.recent().size());
		report.check("Scene transition clears active capture", false, r.plugin.nex.capture.active());
		capture(r, output, "travel-paused", font, report);
		r.arrive(true);
		report.check("Recognized bank revalidates the same summary (kill #" + first.key + ")", true, first == r.plugin.nexSession.stats().last);
		report.check("Bank widget without a readable container does not invent stock", false, r.plugin.session.snapshot().bank.known());
		capture(r, output, "at-bank", font, report);
		r.loading(); r.arrive(false);
		report.check("Recognized arena return retains history (kill #" + first.key + ")", true, first == r.plugin.nexSession.stats().last);
		var second = r.fight(30, 60, 60, true); r.settle();
		report.check("Second bank-trip kill is complete", Outcome.COMPLETE, second.outcome);
		report.check("Two independent completed encounters", 2, r.plugin.nexSession.stats().complete);
		report.check("Next kill uses a new capture key", true, second.key != first.key);
		report.check("Matched cohort after bank return", first.cohort, second.cohort);
		report.check("Inherited contexts stay out of matched averages", 0, r.plugin.nexSession.stats().n);
		report.check("Unconfirmed timing mean stays unavailable", true, Double.isNaN(r.plugin.nexSession.stats().meanSeconds));
		report.check("Unconfirmed mean share stays unavailable", true, Double.isNaN(r.plugin.nexSession.stats().meanShare));
		report.check("Unconfirmed aggregate share stays unavailable", true, Double.isNaN(r.plugin.nexSession.stats().aggregateShare));
		report.check("Fresh comparison is for the next kill (kill #" + second.key + ")", true, second == r.plugin.ownedResult.summary);
		report.section("Banking scope · " + font + "px", "Recognized short same-account bank trip. "
			+ "The separate nex-bank-long-trip and nex-arena-reload scenarios exercise PR #38's extended retention and reload paths.");
		capture(r, output, "next-kill", font, report);
	}

	private static void longBanking(OfflinePluginReplay r, Path output, int font, ReviewReport report) throws Exception
	{
		var first = r.fight(20, 20, 60, true); r.settle();
		report.check("First long-trip kill is complete", Outcome.COMPLETE, first.outcome);
		r.barrier(1); r.barrier = 2; // The observed exit must win over this lagging getter at LOADING.
		r.loading(); r.loading(); r.arrive(false);
		r.waitTicks(150); // Visible nearby Nex is not proof of returning from a bank.
		report.check("Outbound leg stays paused beyond the old 100-tick deadline", true, r.plugin.nexSession.suspended());
		report.check("Nearby Nex cannot expose unvalidated history", 0, r.plugin.nexSession.recent().size());
		report.check("Outbound scene cannot restart capture", false, r.plugin.nex.capture.active());
		report.check("Outbound old comparison remains hidden", null, r.plugin.ownedResult);
		onEdt(() -> {
			report.check("Paused view explains bank revalidation", true, PanelPreview.text(r.panel).contains("Nex session paused"));
			report.check("Reset is available after live account revalidation", true, PanelPreview.children(r.panel).stream()
				.anyMatch(c -> c instanceof javax.swing.JButton && "nex-reset".equals(c.getName()) && PanelPreview.visible(c)));
		});
		capture(r, output, "late-outbound", font, report);
		r.arrive(true);
		report.check("Late verified bank restores the exact first summary", true, first == r.plugin.nexSession.stats().last);
		report.check("Late bank restores the completed result", 1, r.plugin.nexSession.stats().complete);
		capture(r, output, "late-bank", font, report);
		r.loading(); r.waitTicks(150); r.arrive(false);
		report.check("Long bank-origin return retains the exact first summary", true, first == r.plugin.nexSession.stats().last);
		var second = r.fight(30, 60, 60, true); r.settle();
		report.check("Next post-bank kill completes", Outcome.COMPLETE, second.outcome);
		report.check("Post-bank comparison uses a new capture key", true, second.key != first.key && r.plugin.ownedResult.summary == second);
		report.check("Long-trip history contains both complete kills", 2, r.plugin.nexSession.stats().complete);
		report.check("Unconfirmed long-trip timing stays unmatched", true, Double.isNaN(r.plugin.nexSession.stats().meanSeconds));
		report.check("Unconfirmed long-trip aggregate stays unmatched", true, Double.isNaN(r.plugin.nexSession.stats().aggregateShare));
		capture(r, output, "next-kill", font, report);
		r.changeIdentity(); r.callbacks.drain(); r.edt();
		report.check("Removing the deadline does not weaken identity clearing", 0, r.plugin.nexSession.stats().attempts);
		report.check("Changed identity clears the comparison", null, r.plugin.ownedResult);
		capture(r, output, "identity-cleared", font, report);
	}

	private static void arenaReload(OfflinePluginReplay r, Path output, int font, ReviewReport report) throws Exception
	{
		var first = r.fight(20, 20, 60, true); r.settle();
		r.loading(); r.bossPresent = false; r.loading(); r.arrive(false, false);
		report.check("Missing NPCs during rebuild keep history suspended", true, r.plugin.nexSession.suspended());
		report.check("Rebuilding scene keeps old results inaccessible", 0, r.plugin.nexSession.recent().size());
		capture(r, output, "rebuilding", font, report);
		r.bossPresent = true; r.tick(); r.settle();
		report.check("Same-arena evidence resumes without a bank visit", false, r.plugin.nexSession.suspended());
		report.check("Arena reload retains the exact complete summary", true, first == r.plugin.nexSession.stats().last);
		var second = r.fight(30, 60, 60, true); r.settle();
		report.check("Next real smoke callback reaches capture after reload", true, second.key != first.key);
		report.check("Next post-reload kill completes", Outcome.COMPLETE, second.outcome);
		report.check("Arena reload retains two complete kills", 2, r.plugin.nexSession.stats().complete);
		report.check("Current comparison is for the post-reload kill", true, r.plugin.ownedResult.summary == second);
		capture(r, output, "next-kill", font, report);
	}

	private static void pending(OfflinePluginReplay r, Path output, int font, ReviewReport report, boolean identity) throws Exception
	{
		var first = r.fight(20, 20, 60, true); r.settle();
		report.check("Positive control publishes through the real worker (kill #" + first.key + ")", true, first == r.plugin.ownedResult.summary);
		var second = r.fight(30, 40, 40, true);
		report.check("Replacement worker is actually queued", 1, r.workers.size());
		report.check("Replacement remains unpublished while held", null, r.plugin.ownedResult);
		r.edt();
		onEdt(()->{ PanelPreview.click(r.panel,"Diagnostics"); report.check("Pending Diagnostics has no substituted calculation",true,PanelPreview.text(r.panel).contains("Calculation pending")); });
		capture(r,output,"pending-diagnostics",font,report);
		onEdt(()->PanelPreview.click(r.panel,"Encounter"));
		Runnable stale = r.workers.take();
		Runnable publication;
		if (identity)
		{
			r.runWorker(stale);
			report.check("Finished worker has queued a client publication", 1, r.callbacks.size());
			publication = r.callbacks.take();
			r.trace.add("tick " + r.tick + " · Evaluation finished for kill #" + second.key + "; publication held");
			r.changeIdentity();
		}
		else
		{
			onEdt(() -> PanelPreview.click(r.panel, "Reset session"));
			r.callbacks.drain();
			r.trace.add("tick " + r.tick + " · Actual Reset action invalidates held worker");
			r.runWorker(stale);
			report.check("Stale worker still reaches the publication queue after reset", 1, r.callbacks.size());
			publication = r.callbacks.take();
		}
		r.callbacks.drain(); r.edt();
		report.check("Old result cannot republish after " + (identity ? "identity change" : "reset"), null, r.plugin.ownedResult);
		report.check("Cleared encounter history remains empty", 0, r.plugin.nexSession.stats().attempts);
		onEdt(() -> report.check("Cleared panel contains no old comparison", false, PanelPreview.text(r.panel).contains("Owned equipment · kill")));
		capture(r, output, "stale-rejected", font, report);
		if (identity) { r.ready(); }
		var fresh = r.fight(20, 20, 60, true); r.settle();
		report.check("Fresh work after invalidation can publish (kill #" + fresh.key + ")", true, fresh == r.plugin.ownedResult.summary);
		report.check("Fresh history starts from one complete kill", 1, r.plugin.nexSession.stats().complete);
		var current = r.plugin.ownedResult;
		r.trace.add("tick " + r.tick + " · Release OLD publication after fresh result is already visible");
		publication.run(); r.edt();
		report.check("Delayed old callback cannot overwrite fresh result", true, current == r.plugin.ownedResult);
		report.check("Rejected old callback schedules no evaluation with stale account", 0, r.workers.size());
		capture(r, output, "fresh-published", font, report);
	}

	private static void frozen(OfflinePluginReplay r, Path output, int font, ReviewReport report) throws Exception
	{
		var first = r.fight(20, 20, 60, true); r.settle();
		onEdt(() -> {
			PanelPreview.click(r.panel, "Owned setup comparisons");
			PanelPreview.combo(r.panel, "nex-owned-role-filter").setSelectedItem("melee");
			JComboBox<?> pairs = PanelPreview.combo(r.panel, "nex-owned-pair");
			int bloodFury = -1;
			for (int i = 0; i < pairs.getItemCount(); i++) { if (pairs.getItemAt(i).toString().toLowerCase(java.util.Locale.ROOT).contains("blood fury")) { bloodFury = i; break; } }
			if (bloodFury < 0) { throw new IllegalStateException("Expected existing blood-fury regression alternative"); }
			pairs.setSelectedIndex(bloodFury);
		});
		capture(r, output, "inspecting-first", font, report);
		final String[] frozenReport={null};
		onEdt(()->{ PanelPreview.click(r.panel,"Diagnostics"); frozenReport[0]=exportText(r); });
		var second = r.fight(30, 40, 40, true); r.settle();
		report.check("Fresh result publishes while earlier comparison is open (kill #" + second.key + ")", true, second == r.plugin.ownedResult.summary);
		onEdt(() -> {
			report.check("Export also stays bound to earlier selected negative pair",frozenReport[0],exportText(r));
			PanelPreview.click(r.panel,"Back to comparison");
			report.check("Earlier comparison stays frozen", true, PanelPreview.text(r.panel).contains("Owned equipment · kill #" + first.key));
			report.check("Selected negative alternative stays selected", true, PanelPreview.text(r.panel).contains("Blood fury healing and survival are not estimated"));
			PanelPreview.click(r.panel, "Special-attack resources");
			report.check("Resource review also belongs to the earlier kill", true, PanelPreview.text(r.panel).contains("Special-attack resources · kill #" + first.key));
		});
		capture(r, output, "frozen-comparison", font, report);
		onEdt(() -> { PanelPreview.click(r.panel, "Back to encounter"); PanelPreview.click(r.panel, "Kill and calculation details"); });
		var third = r.fight(40, 60, 20, true); r.settle();
		report.check("Third result publishes while details are inspected (kill #" + third.key + ")", true, third == r.plugin.ownedResult.summary);
		onEdt(() -> report.check("Inspected second-kill details stay frozen", true, PanelPreview.text(r.panel).contains("Kill #" + second.key + " · COMPLETE")));
		capture(r, output, "frozen-details", font, report);
		onEdt(() -> {
			PanelPreview.click(r.panel, "Back to encounter");
			report.check("Returning shows the latest complete timing", true, PanelPreview.text(r.panel).contains("Last complete kill · 24.0 s"));
			PanelPreview.click(r.panel, "Owned setup comparisons");
			report.check("Reopening comparisons selects the latest kill", true, PanelPreview.text(r.panel).contains("Owned equipment · kill #" + third.key));
		});
		capture(r, output, "latest-comparison", font, report);
	}

	private static String exportText(OfflinePluginReplay r)
	{ return ((javax.swing.JTextArea)PanelPreview.children(r.panel).stream().filter(c->"nex-report-text".equals(c.getName())).findFirst().orElseThrow()).getText(); }
	private static void diagnostics(OfflinePluginReplay r,Path output,int font,ReviewReport report) throws Exception
	{
		onEdt(()->PanelPreview.click(r.panel,"Diagnostics"));
		capture(r,output,"diagnostics",font,report);
		onEdt(()->{
			try { PanelPreview.viewport(r.panel,output.resolve("diagnostics-viewport-"+font+".png"),225,600,null,List.of("nex-copy-report"),report); }
			catch(Exception ex) { throw new RuntimeException(ex); }
			PanelPreview.click(r.panel,"Copy report now");
			report.check("Clipboard failure keeps selectable report "+font,true,PanelPreview.text(r.panel).contains("Clipboard unavailable or busy"));
			String text=exportText(r);
			report.check("Synthetic provenance in production report "+font,true,text.contains("SYNTHETIC_OFFLINE"));
			report.check("Report has no profile/roster identity "+font,false,text.contains("offline-synthetic-a") || text.contains("offline-synthetic-b") || text.contains("NEX@"));
			try { java.nio.file.Files.writeString(output.resolve("diagnostic-report-"+font+".txt"),text); }
			catch(Exception ex) { throw new RuntimeException(ex); }
		});
		capture(r,output,"selectable-report",font,report);
	}

	private static void evidence(OfflinePluginReplay r, int font, ReviewReport report)
	{
		StringBuilder table = new StringBuilder("| Kill | Outcome / reason | Smoke → death/end ticks | Seconds | Mine / total | Share | Sampled / eligible |\n|---|---|---|---|---|---|---|\n");
		for (var summary : r.encounters)
		{
			table.append("| ").append(summary.key).append(" | ").append(summary.outcome).append(" · ").append(summary.reason)
				.append(" | ").append(summary.startTick).append(" → ").append(summary.endTick).append(" | ").append(summary.seconds())
				.append(" | ").append(summary.localDamage()).append(" / ").append(summary.totalDamage()).append(" | ").append(summary.share())
				.append("% | ").append(summary.sampledTicks).append(" / ").append(summary.expectedTicks).append(" |\n");
			report.section("Capture scope/gaps · " + font + "px · kill #" + summary.key,
				summary.timerScope + "\n\n" + com.ironpath.nex.NexSummary.DAMAGE_SCOPE + "\n\n" + new java.util.TreeSet<>(summary.gaps));
		}
		report.section("Captured encounters · " + font + "px", table.toString());
		for (var result : r.results.values())
		{
			StringBuilder rows = new StringBuilder();
			for (var choice : result.choices)
			{
				rows.append("- Tick ").append(choice.actual.tick).append(" · ").append(choice.actual.role).append(" · ").append(choice.actual.phase)
					.append(" / ").append(choice.actual.window).append(" · ").append(choice.headline()).append("\n")
					.append("  - Actual slots: `").append(new java.util.TreeMap<>(choice.actual.state.slots)).append("`; visible levels: `")
					.append(new java.util.TreeMap<>(choice.actual.state.visibleLevels)).append("`; prayers: ").append(new java.util.TreeSet<>(choice.actual.state.prayers))
					.append("; style: ").append(choice.actual.state.style).append("; inventory: ").append(choice.actual.state.inventory == null ? "unread" : new java.util.TreeMap<>(choice.actual.state.inventory)).append(".\n");
				if (choice.supported())
				{
					rows.append("  - Proposed slots: `").append(new java.util.TreeMap<>(choice.proposed.slots)).append("`. ")
						.append(choice.comparison.summary()).append("\n  - Sources/assumptions: ").append(String.join("; ", choice.comparison.assumptions)).append("\n");
				}
				rows.append("  - Coverage: ").append(choice.coverage).append("\n");
			}
			report.section("Published comparisons · " + font + "px · kill #" + result.summary.key,
				rows + "\nGrouped gaps: " + result.gaps + ". No attack attribution, spec damage, survival, personal contribution or team-time claim.");
		}
	}

	private static void onEdt(Runnable action) throws Exception { SwingUtilities.invokeAndWait(action); }
	private static void capture(OfflinePluginReplay r, Path output, String state, int font, ReviewReport report) throws Exception
	{
		onEdt(() -> {
			try { PanelPreview.capture(r.panel, output.resolve(state + "-" + font + ".png"), 225, report); }
			catch (Exception failure) { throw new IllegalStateException(failure); }
		});
	}
}
