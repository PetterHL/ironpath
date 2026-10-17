package com.ironpath;

import com.ironpath.dev.ReviewReport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Development entry point only; scenario registry has no encounter-specific result type. */
public final class OfflineReview
{
	@FunctionalInterface
	interface Scenario { void run(Path output, ReviewReport report) throws Exception; }
	private OfflineReview() { }
	static ReviewReport run(String id, Path output) throws Exception
	{
		Map<String, Scenario> scenarios = Map.of("nex-complete", (directory, report) -> new OfflineNexScenario().run(directory, report));
		Scenario scenario = scenarios.get(id);
		if (scenario == null) { throw new IllegalArgumentException("Unknown scenario '" + id + "'. Available: " + scenarios.keySet()); }
		Files.createDirectories(output);
		// A failed replay must not leave last run's apparently successful images alongside its report.
		try (var files = Files.list(output))
		{ for (Path file : (Iterable<Path>) files.filter(p -> p.getFileName().toString().endsWith(".png"))::iterator) { Files.delete(file); } }
		ReviewReport report = new ReviewReport();
		Map<Object, Object> fonts = new HashMap<>();
		LookAndFeel previous = UIManager.getLookAndFeel();
		report.section("Pipeline", "Ordered mocked public API callbacks → production NexObserver/CombatStateReader → NexCapture → NexSession "
			+ "→ production NexOwnedEvaluator on a worker → actual NexPanel on Swing EDT. "
			+ "No pre-completed summary injection. Synthetic account facts/preferences are supplied directly; "
			+ "plugin event-bus wiring, account reader, preference IO and asynchronous plugin publication guards are not exercised by this scenario.");
		try
		{
			SwingUtilities.invokeAndWait(() -> {
				for (Object key : java.util.List.copyOf(UIManager.getDefaults().keySet()))
				{ Object value = UIManager.get(key); if (value instanceof Font) { fonts.put(key, value); } }
				try { UIManager.setLookAndFeel(new net.runelite.client.ui.laf.RuneLiteLAF()); }
				catch (Exception ex) { throw new IllegalStateException(ex); }
			});
			scenario.run(output, report);
		}
		catch (Exception | AssertionError failure)
		{
			report.check("Scenario completed without exception", "no exception", failure.toString());
			failure.printStackTrace(System.err);
		}
		finally
		{
			SwingUtilities.invokeAndWait(() -> {
				for (Object key : java.util.List.copyOf(UIManager.getDefaults().keySet()))
				{ if (UIManager.get(key) instanceof Font && !fonts.containsKey(key)) { UIManager.put(key, null); } }
				try { UIManager.setLookAndFeel(previous); }
				catch (Exception ex) { throw new IllegalStateException(ex); }
				fonts.forEach(UIManager::put);
			});
			report.section("Verification limits", "Synthetic replay and headless component painting do not establish live RuneLite event mappings, "
				+ "real ItemManager sprites, client DPI/focus/scroll/GPU behavior or player usefulness. "
				+ "N1–N4 live acceptance stays open. Bank/reset/pending-result/frozen-history scenarios are after the DEV1 review checkpoint.");
			report.write(output);
		}
		return report;
	}
	public static void main(String[] args) throws Exception
	{
		if (args.length != 1) { throw new IllegalArgumentException("Usage: offlineReview --args='nex-complete'"); }
		Path output = Path.of(System.getProperty("ironpath.reviewOutput", "build/review")).resolve(args[0]);
		ReviewReport report = run(args[0], output);
		System.out.println((report.passed() ? "PASS" : "FAIL") + " · report and native previews: " + output.toAbsolutePath());
		if (!report.passed()) { throw new AssertionError(report.failures() + " offline review checks failed; see report.md"); }
	}
}
