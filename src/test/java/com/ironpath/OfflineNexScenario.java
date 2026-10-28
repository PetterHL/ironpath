package com.ironpath;

import com.ironpath.advisor.ComparisonResult;
import com.ironpath.advisor.NexOwnedEvaluator;
import com.ironpath.advisor.NexOwnedFixtures;
import com.ironpath.advisor.NexOwnedResult;
import com.ironpath.dev.OrderedReplay;
import com.ironpath.dev.PanelPreview;
import com.ironpath.dev.ReviewReport;
import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.nex.NexSession;
import com.ironpath.nex.NexSummary;
import com.ironpath.preferences.NexMethod;
import com.ironpath.preferences.Preferences;
import java.awt.Font;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import net.runelite.api.*;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.gameval.NpcID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** The first DEV1 scenario. All runtime boundary values are explicitly synthetic. */
final class OfflineNexScenario extends OfflineNexClient
{
	private final NexSession session = new NexSession();
	private final NexObserver observer;
	private final Preferences prefs = Preferences.EMPTY.nex(NexMethod.PILOT.lock("melee/shield", 12954));
	private final List<String> trace = new ArrayList<>();

	OfflineNexScenario() throws Exception
	{
		session.select("offline-synthetic-profile", 301);
		observer = new NexObserver(client, catalog, session);
		observer.method(prefs.nex);
	}

	private void hit(NPC actor, boolean mine, int amount)
	{
		Hitsplat hit = mock(Hitsplat.class);
		when(hit.isMine()).thenReturn(mine); when(hit.isOthers()).thenReturn(!mine);
		when(hit.getHitsplatType()).thenReturn(mine ? HitsplatID.DAMAGE_ME : HitsplatID.DAMAGE_OTHER);
		when(hit.getAmount()).thenReturn(amount);
		HitsplatApplied event = new HitsplatApplied(); event.setActor(actor); event.setHitsplat(hit);
		observer.hit(event);
	}

	void run(Path output, ReviewReport report) throws Exception
	{
		report.section("Pipeline", "Ordered mocked public API callbacks → production NexObserver/CombatStateReader → NexCapture → NexSession "
			+ "→ production NexOwnedEvaluator on a worker → actual NexPanel on Swing EDT. No pre-completed summary injection. "
			+ "Synthetic account facts/preferences are supplied directly; plugin event-bus wiring, account reader, preference IO "
			+ "and asynchronous plugin publication guards are outside this direct-adapter scenario. Use the lifecycle scenarios for those guards.");
		report.section("Scenario and provenance", "`nex-complete` · Nex / Trio MP2 / one kill per trip.\n\n"
			+ "Reported ranged/melee equipment comes from NexOwnedFixtures; no private account dump. "
			+ "All levels (real 99; visible attack/strength 118, ranged 112), prayers (Rigour/Piety), style button 1, "
			+ "three-person visible roster, event order, HP-independent hits, stock and timing are SYNTHETIC. "
			+ "Voidwaker is a synthetic spec-candidate switch, not an attributed attack. Lightbearer throughout. "
			+ "Synthetic stock: 500 quiver amethyst arrows, 200 splinters, 200 carried dragon arrows, one of each switch item. "
			+ "Account/unlock facts reuse NexOwnedFixtures.account(null); bank knowledge remains unknown. "
			+ "Explicit preferences: upgraded Death Charge assumed; melee/shield locked to Dragon defender 12954. "
			+ "Detected active charges 2 → 1 and energy 1000 → 500 are readings, not inferred casts/procs/specs.");
		OrderedReplay replay = new OrderedReplay();
		replay.at(0, "Initial arena poll (barrier 2, ranged equipment)", observer::tick);
		replay.at(0, "Smoke overhead anchors capture before next GameTick", () -> observer.overhead(boss, "Fill my soul with smoke!"));
		for (int t = 1; t <= 36; t++)
		{
			if (t == 1)
			{
				replay.at(t, "Barrier event value 3; polled value still 2", () -> observer.barrier(3));
				replay.at(t, "Polled barrier catches up before GameTick", () -> barrier = 3);
			}
			if (t == 5) { replay.at(t, "Shadow game message", () -> observer.chat(ChatMessageType.GAMEMESSAGE, "Nex: Darken my shadow!")); }
			if (t == 8) { replay.at(t, "Ranged MP2 window", () -> observer.overhead(boss, "Embrace darkness!")); }
			if (t == 9) { replay.at(t, "20 mine on Nex, 60 other on Umbra", () -> { hit(boss, true, 20); hit(minion, false, 60); }); }
			if (t == 10)
			{
				replay.at(t, "Equip melee with defender and Piety before window callback", () -> worn = NexOwnedFixtures.melee());
				replay.at(t, "Melee MP2 window", () -> observer.overhead(boss, "Fear the shadow!"));
			}
			if (t == 12)
			{
				replay.at(t, "Voidwaker selected special; resource callback", () -> {
					worn = new LinkedHashMap<>(NexOwnedFixtures.melee()); worn.put("weapon", 27690); special = true; observer.sample(-1);
				});
			}
			if (t == 13)
			{
				replay.at(t, "Return to melee, sampled energy/charge decrease", () -> {
					worn = NexOwnedFixtures.melee(); special = false; energy = 500; charge = 1; observer.sample(-1);
				});
			}
			if (t == 15) { replay.at(t, "Blood phase", () -> observer.overhead(boss, "Flood my lungs with blood!")); }
			if (t == 20) { replay.at(t, "Ice phase", () -> observer.overhead(boss, "Infuse me with the power of ice!")); }
			if (t == 21) { replay.at(t, "Two-handed ranged switch with empty offhand", () -> { worn = NexOwnedFixtures.ranged(); observer.sample(-1); }); }
			if (t == 25) { replay.at(t, "Zaros phase", () -> observer.overhead(boss, "NOW, THE POWER OF ZAROS!")); }
			if (t == 30) { replay.at(t, "Nex dying public NPC transformation", () -> { when(boss.getId()).thenReturn(NpcID.NEX_DYING); observer.changed(boss); }); }
			if (t == 31)
			{
				replay.at(t, "Arena-end event 2 while polled barrier remains 3", () -> observer.barrier(2));
				replay.at(t, "Polled barrier catches up before GameTick", () -> barrier = 2);
			}
			replay.at(t, "GameTick", observer::tick);
		}
		replay.run(value -> tick = value, trace::add);
		report.section("Ordered callback script", "```text\n" + String.join("\n", trace) + "\n```");
		report.check("One captured attempt", 1, session.recent().size());
		if (session.recent().isEmpty()) { throw new IllegalStateException("No summary produced by public events"); }
		NexSummary summary = session.recent().get(0);
		report.check("Encounter outcome", Outcome.COMPLETE, summary.outcome);
		report.check("Smoke start tick", 0, summary.startTick); report.check("Death tick", 30, summary.endTick);
		report.check("Arena-end event payload", 31, summary.signals.barrierEndTick);
		report.check("All five phases", Set.of("SMOKE", "SHADOW", "BLOOD", "ICE", "ZAROS"), summary.signals.phases);
		report.near("Duration: (30 - 0) × 0.6", 18, summary.seconds(), 1e-10);
		report.check("Sampled eligible ticks", 30, summary.sampledTicks);
		report.check("Expected eligible ticks", 30, summary.expectedTicks);
		report.check("Local damage, duplicates excluded", 20L, summary.localDamage());
		report.check("Total Nex + named minion damage", 80L, summary.totalDamage());
		report.near("Share: 100 × 20 / (20 + 60)", 25, summary.share() == null ? Double.NaN : summary.share(), 1e-10);
		report.check("Complete statistics sample count remains unconfirmed", 0, session.stats().n);
		report.check("Excluded attempts", 0, session.stats().failed + session.stats().censored);
		report.check("Captured role switches", true, summary.timeline.stream().map(s -> s.role).collect(java.util.stream.Collectors.toSet()).containsAll(Set.of("ranged", "melee", "spec candidate")));
		report.check("Spec candidate remains evidence, not confirmed attack", true, summary.timeline.stream().anyMatch(s -> s.role.equals("spec candidate") && s.state.slots.get("weapon") == 27690));
		report.check("Melee defender survives ranged empty offhand", true, summary.timeline.stream().anyMatch(s -> s.role.equals("melee") && s.state.slots.get("shield") == 12954)
			&& summary.timeline.stream().anyMatch(s -> s.role.equals("ranged") && s.state.slots.get("shield") == 0));
		report.section("Capture result", summary.outcome + " · " + summary.reason + "\n\n" + summary.timerScope + "\n\n"
			+ NexSummary.DAMAGE_SCOPE + "\n\nGaps: " + new java.util.TreeSet<>(summary.gaps) + ".\n\n"
			+ "Eligibility is synthetic observed-attempt coverage, not coverage of live kills. Damage buckets: "
			+ summary.damage.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue().local + "/" + e.getValue().total).sorted().collect(java.util.stream.Collectors.joining("; ")));
		var account = NexOwnedFixtures.account(null);
		var worker = Executors.newSingleThreadExecutor(r -> new Thread(r, "offline-evaluator"));
		NexOwnedResult result;
		try { result = worker.submit(() -> {
			NexOwnedResult evaluated = new NexOwnedEvaluator(catalog).evaluate(summary, account, prefs);
			checkComparisons(evaluated, report);
			return evaluated;
		}).get(30, TimeUnit.SECONDS); }
		finally { worker.shutdownNow(); }
		OfflineReview.versions(report, catalog);
		SwingUtilities.invokeAndWait(() -> {
			try { previews(output, result, report); } catch (Exception ex) { throw new RuntimeException(ex); }
		});
	}

	private void checkComparisons(NexOwnedResult result, ReviewReport report)
	{
		var ranged = result.choices.stream().filter(c -> c.actual.tick == 8 && c.actual.role.equals("ranged")).findFirst().orElseThrow();
		report.check("Ranged MP2 ordinary comparison supported", true, ranged.supported());
		if (ranged.supported())
		{
			var metrics = ranged.comparison.before;
			// Independent hand case retained in NexOwnedEvaluatorTest, not computed by the model under review.
			report.check("Ranged attack bonus: 200 + 10 quiver", 210, metrics.attackBonus);
			report.near("Ranged strength: 35 + 55 arrows + 1 quiver", 91, metrics.damagePercent, 0);
			report.check("Tbow max hit, independent integer hand case", 72, metrics.maxHit);
			report.check("Tbow attack roll, independent hand case", 53303L, metrics.attackRoll);
			report.check("Nex defence roll: 269 × 254", 68326L, metrics.defenceRoll);
			double chance = 53303.0 / (2 * 68327);
			report.near("Hit chance: 53303 / (2 × 68327)", chance, metrics.hitChance, 1e-12);
			report.near("DPS: (36 + 1/73) × hit chance / 3", (36 + 1.0 / 73) * chance / 3, metrics.dps, 1e-12);
			report.check("Rapid attack delay", 5, metrics.attackTicks);
			report.check("Selected quiver projectile", 21326, ranged.comparison.beforeSetup.loadedAmmo);
			report.check("Physical ammo slot remains blessing", 20235, ranged.comparison.beforeSetup.slots.get("ammo"));
			report.check("Carried owned proposal can improve ordinary offence", true, ranged.comparison.dpsGain() > 0);
			Map<String, Integer> expectedSlots = new TreeMap<>(NexOwnedFixtures.ranged()); expectedSlots.put("ammo", 11212);
			report.check("Whole proposed package changes only the carried arrow", expectedSlots, ranged.proposed.slots);
			// Visible strength 112 with Rigour gives 145; base max 35 -> 36, Nex Tbow factor 206%.
			report.check("Dragon-arrow max hit: floor(36 × 206 / 100)", 74, ranged.comparison.after.maxHit);
			report.check("Arrow change leaves attack roll unchanged", 53303L, ranged.comparison.after.attackRoll);
			report.near("Owned DPS: (37 + 1/75) × hit chance / 3", (37 + 1.0 / 75) * chance / 3, ranged.comparison.after.dps, 1e-12);
			report.near("Whole-package delta: hand-computed expected damage difference / 3",
				((37 + 1.0 / 75) - (36 + 1.0 / 73)) * chance / 3, ranged.comparison.dpsGain(), 1e-12);
		}
		var firstMelee = result.choices.stream().filter(c -> c.actual.role.equals("melee") && c.supported()).findFirst().orElseThrow();
		var identity = new NexOwnedEvaluator(catalog).compare(firstMelee.actual,
			new NexOwnedEvaluator(catalog).setup(firstMelee.actual, firstMelee.actual.state.slots), NexOwnedFixtures.account(null), prefs);
		report.near("Identical complete melee package gives zero", 0, identity.dpsGain(), 1e-12);
		report.check("Supported blood-fury alternative retains negative offence", true, firstMelee.alternatives.stream()
			.anyMatch(p -> p.afterSetup.slots.get("neck") == 24780 && p.dpsGain() < 0));
		report.check("Spec candidate has scoped gap", true, result.choices.stream().anyMatch(c -> c.actual.role.equals("spec candidate") && !c.supported()));
		report.check("Zaros has scoped gap", true, result.choices.stream().anyMatch(c -> c.actual.phase.equals("ZAROS") && !c.supported()));
		report.check("No bank scenarios fabricated from unknown bank", false, result.choices.stream().anyMatch(c -> c.bankPreparation));
		StringBuilder rows = new StringBuilder("| Tick/role/phase/window | Result |\n|---|---|\n");
		for (var choice : result.choices)
		{
			rows.append("| ").append(choice.actual.tick).append(" / ").append(choice.actual.role).append(" / ").append(choice.actual.phase)
				.append(" / ").append(choice.actual.window).append(" | ").append(choice.headline()).append(" |\n");
			if (!choice.supported()) { continue; }
			var pair = choice.comparison;
			report.check("Tick " + choice.actual.tick + " does not estimate team time", false, pair.available(ComparisonResult.Metric.EXPECTED_KILL_TIME));
			if (choice.actual.tick != 8 && choice.actual.tick != 10) { continue; }
			report.section("Comparison inputs/results · tick " + choice.actual.tick,
				"Actual slots: `" + new TreeMap<>(choice.actual.state.slots) + "`\n\nVisible levels: `" + new TreeMap<>(choice.actual.state.visibleLevels)
				+ "`; prayers: " + choice.actual.state.prayers + "; style: " + choice.actual.state.style + "; target: " + choice.actual.target
				+ ".\n\nCarried quantities: `" + new TreeMap<>(choice.actual.state.inventory) + "`.\n\nProposed slots: `" + new TreeMap<>(choice.proposed.slots)
				+ "`\n\n" + pair.summary() + "\n\nModel " + pair.modelVersion + "; knowledge " + pair.knowledgeVersion
				+ ".\n\nAssumptions/sources: " + String.join("; ", pair.assumptions) + "\n\nCoverage: " + choice.coverage + ".");
		}
		report.section("Comparison overview", rows + "\nGrouped gaps: " + result.gaps + ".\n\n"
			+ "Whole-package ordinary comparisons are conditional, independent alternatives; counts are not fight coverage. "
			+ "No spec damage, attack attribution, survival, personal contribution or team-time output is introduced.");
	}

	private void previews(Path output, NexOwnedResult result, ReviewReport report) throws Exception
	{
		for (int font : List.of(12, 20))
		{
			PanelPreview.fonts(font);
			NexPanel panel = new NexPanel(OfflineSprites.manager(),text->{ throw new java.awt.HeadlessException(); },NexReport.Provenance.SYNTHETIC_OFFLINE); panel.setFont(new Font("DejaVu Sans", Font.PLAIN, font));
			NexPanel.Actions actions = new NexPanel.Actions() {
				public boolean current() { return true; } public boolean canEdit() { return false; }
				public void method(NexMethod method) { throw new UnsupportedOperationException("Read-only preview"); }
				public void confirm(String key, Boolean value) { throw new UnsupportedOperationException("Read-only preview"); }
				public void reset() { throw new UnsupportedOperationException("Read-only preview"); }
				public void enable(boolean value) { throw new UnsupportedOperationException("Read-only preview"); }
			};
			panel.render("Synthetic offline scenario", NexOwnedFixtures.account(null), prefs, catalog, session.stats(), true, actions, "Synthetic; no preference IO", null);
			report.check("Pending main withholds old comparisons "+font,true,PanelPreview.text(panel).contains("Recalculating…") && !PanelPreview.text(panel).contains(" DPS"));
			capture(panel,output,"pending-main",font,report);
			var nonPositive=new NexOwnedResult(result.summary,result.choices.stream().filter(c->NexPresentation.cards(new NexOwnedResult(result.summary,List.of(c))).isEmpty()).collect(java.util.stream.Collectors.toList()),result.planning,result.scenarioUnlocks,result.context,result.contextRevision,result.inputRevision,result.modelRevision,result.advice,result.equipment);
			panel.render("Synthetic offline scenario", NexOwnedFixtures.account(null), prefs, catalog, session.stats(), true, actions, "Synthetic; no preference IO", nonPositive);
			report.check("No-positive fixture retains supported alternatives "+font,true,nonPositive.choices.stream().anyMatch(c->c.supported()));
			report.check("Captured loadouts provide theoretical comparisons without attributed attacks "+font,true,PanelPreview.text(panel).contains(" DPS"));
			capture(panel,output,"no-improvement-main",font,report);
			panel.render("Synthetic offline scenario", NexOwnedFixtures.account(null), prefs, catalog, session.stats(), true, actions, "Synthetic; no preference IO", result);
			report.check("Main view " + font + "px shows measured kill", true, PanelPreview.text(panel).contains("Last kill") && PanelPreview.text(panel).contains("18.0 s"));
			capture(panel, output, "main", font, report);
			PanelPreview.viewport(panel,output.resolve("encounter-viewport-"+font+".png"),225,600,null,List.of("nex-duration","nex-edit-context","nex-combat-role"),report);
			PanelPreview.openSection(panel,"Diagnostics"); capture(panel,output,"diagnostics",font,report);
			PanelPreview.viewport(panel,output.resolve("diagnostics-viewport-"+font+".png"),225,600,"nex-section-diagnostics",List.of("nex-copy-report"),report);
			PanelPreview.click(panel,"Copy report now");
			report.check("Headless copy has selectable fallback "+font,true,PanelPreview.text(panel).contains("Clipboard unavailable or busy"));
			String exported=((javax.swing.JTextArea)PanelPreview.children(panel).stream().filter(c->"nex-report-text".equals(c.getName())).findFirst().orElseThrow()).getText();
			report.check("Production report labels synthetic provenance "+font,true,exported.contains("SYNTHETIC_OFFLINE"));
			report.check("Production report retains scoped exact inputs "+font,true,exported.contains("ammo=11212") && exported.contains("RANGED=112"));
			java.nio.file.Files.writeString(output.resolve("diagnostic-report-"+font+".txt"),exported);
			capture(panel,output,"selectable-report",font,report);
			PanelPreview.encounter(panel);
			PanelPreview.click(panel, "Base-stat comparisons");
			PanelPreview.combo(panel, "nex-owned-role-filter").setSelectedItem("Ranged");
			report.check("Comparison view " + font + "px preserves projectile", true, PanelPreview.text(panel).contains("Selected quiver ammo: Amethyst arrow"));
			capture(panel, output, "comparison", font, report);
			PanelPreview.viewport(panel,output.resolve("comparison-viewport-"+font+".png"),225,600,null,List.of("nex-section-compare"),report);
			PanelPreview.click(panel, "Full gear setups"); capture(panel, output, "basis", font, report);
			PanelPreview.combo(panel, "nex-owned-support-filter").setSelectedItem("Unavailable comparisons");
			PanelPreview.combo(panel, "nex-owned-role-filter").setSelectedItem("Special attacks");
			report.check("Gap view " + font + "px preserves unsupported spec", true, PanelPreview.text(panel).contains("No comparison available"));
			capture(panel, output, "gap", font, report);
			PanelPreview.click(panel, "Spec energy & Death Charge");
			capture(panel, output, "resources", font, report);
			PanelPreview.click(panel, "Back to encounter"); PanelPreview.click(panel, "Kill and calculation details");
			capture(panel, output, "kill-details", font, report);
		}
	}
	private static void capture(NexPanel panel, Path output, String state, int font, ReviewReport report) throws Exception
	{ PanelPreview.capture(panel, output.resolve(state + "-" + font + ".png"), 225, report); }
}
