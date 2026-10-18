package com.ironpath;

import com.ironpath.dev.ReviewReport;
import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.advisor.NexOwnedEvaluator;
import com.ironpath.nex.NexCapture;
import net.runelite.api.Client;
import java.nio.file.Files;
import java.nio.file.Path;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Development entry point only; scenario registry has no encounter-specific result type. */
public final class OfflineReview
{
	@FunctionalInterface
	interface Scenario { void run(Path output, ReviewReport report) throws Exception; }
	private OfflineReview() { }
	static Map<String, Scenario> scenarios()
	{
		Map<String, Scenario> scenarios = new LinkedHashMap<>();
		scenarios.put("nex-complete", (directory, report) -> new OfflineNexScenario().run(directory, report));
		for (String id : OfflineLifecycleScenarios.IDS)
		{ scenarios.put(id, (directory, report) -> OfflineLifecycleScenarios.run(id, directory, report)); }
		for(String id:OfflineContextScenarios.IDS)
		{ scenarios.put(id,(directory,report)->OfflineContextScenarios.run(id,directory,report)); }
		return scenarios;
	}
	static ReviewReport run(String id, Path output) throws Exception
	{
		Map<String, Scenario> scenarios = scenarios();
		Scenario scenario = scenarios.get(id);
		if (scenario == null) { throw new IllegalArgumentException("Unknown scenario '" + id + "'. Available: " + scenarios.keySet()); }
		Files.createDirectories(output);
		// A failed replay must not leave last run's apparently successful images alongside its report.
		try (var files = Files.list(output))
		{ for (Path file : (Iterable<Path>) files.filter(p -> p.getFileName().toString().endsWith(".png"))::iterator) { Files.delete(file); } }
		ReviewReport report = new ReviewReport();
		Map<Object, Object> fonts = new HashMap<>();
		LookAndFeel previous = UIManager.getLookAndFeel();
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
			Throwable cause = failure;
			while ((cause instanceof java.lang.reflect.InvocationTargetException || cause instanceof java.util.concurrent.ExecutionException)
				&& cause.getCause() != null) { cause = cause.getCause(); }
			report.check("Scenario completed without exception", "no exception", cause.toString());
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
				+ "N1–N4 live acceptance stays open. RuneLite dependencies are mocked at the public API boundary; callbacks are invoked directly without real event-bus delivery or OS-window interaction.");
			report.write(output);
		}
		return report;
	}
	static void versions(ReviewReport report, KnowledgeCatalog catalog) throws Exception
	{
		report.section("Versions", "Plugin " + System.getProperty("ironpath.version", "test runtime") + "; JDK " + System.getProperty("java.version")
			+ "; RuneLite API JAR " + Path.of(Client.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getFileName()
			+ "; RuneLite client JAR " + Path.of(net.runelite.client.ui.PluginPanel.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getFileName()
			+ "; adapter " + NexCapture.VERSION + "; evaluator " + NexOwnedEvaluator.VERSION + "; knowledge " + catalog.version
			+ "; Gradle " + System.getProperty("ironpath.gradleVersion", "test runtime")
			+ "; catalogue schema 14; preferences schema 4. Model versions are recorded with comparison inputs. Font: DejaVu Sans 12/20 px, RuneLiteLAF, headless Java2D.");
	}
	public static void main(String[] args) throws Exception
	{
		if (args.length != 1) { throw new IllegalArgumentException("Usage: offlineReview --args='all|list|<scenario>'"); }
		if (args[0].equals("list")) { System.out.println(String.join("\n", scenarios().keySet())); return; }
		Path root = Path.of(System.getProperty("ironpath.reviewOutput", "build/review"));
		List<String> ids = args[0].equals("all") ? List.copyOf(scenarios().keySet()) : List.of(args[0]);
		int failures = 0;
		StringBuilder index = new StringBuilder("# Synthetic offline review\n\n| Scenario | Result | Evidence |\n|---|---|---|\n");
		for (String id : ids)
		{
			Path output = root.resolve(id);
			ReviewReport report = run(id, output);
			String status = report.passed() ? "PASS" : "FAIL";
			failures += report.failures();
			index.append("| ").append(id).append(" | ").append(status).append(" | [Report and PNGs](")
				.append(id).append("/report.md) |\n");
			System.out.println(status + " · report and native previews: " + output.toAbsolutePath());
		}
		if (args[0].equals("all")) { Files.writeString(root.resolve("index.md"), index.toString()); }
		if (failures > 0) { throw new AssertionError(failures + " offline review checks failed; see scenario reports"); }
	}
}
