package com.ironpath.dev;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Assertions accumulate so a failed review still leaves useful expected/actual evidence. */
public final class ReviewReport
{
	private final StringBuilder body = new StringBuilder();
	private final List<String> checks = new ArrayList<>();
	private int failures;

	public void section(String title, String text)
	{ body.append("\n## ").append(title).append("\n\n").append(text).append('\n'); }

	public boolean check(String label, Object expected, Object actual)
	{ return record(label, display(expected), display(actual), Objects.equals(expected, actual)); }

	private static String display(Object value)
	{
		if (value instanceof java.util.Map)
		{
			return ((java.util.Map<?, ?>) value).entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
				.sorted().collect(java.util.stream.Collectors.joining(", ", "{", "}"));
		}
		if (value instanceof java.util.Set)
		{
			return ((java.util.Set<?>) value).stream().map(String::valueOf).sorted()
				.collect(java.util.stream.Collectors.joining(", ", "[", "]"));
		}
		return String.valueOf(value);
	}

	public boolean near(String label, double expected, double actual, double tolerance)
	{
		return record(label, expected + " ± " + tolerance, String.valueOf(actual),
			Double.isFinite(actual) && Math.abs(expected - actual) <= tolerance);
	}

	private boolean record(String label, String expected, String actual, boolean passed)
	{
		if (!passed) { failures++; }
		checks.add("| " + (passed ? "PASS" : "FAIL") + " | " + cell(label) + " | " + cell(expected) + " | " + cell(actual) + " |");
		return passed;
	}

	private static String cell(String value) { return value.replace("|", "\\|").replace("\n", " "); }
	public boolean passed() { return failures == 0; }
	public int failures() { return failures; }
	public void write(Path output) throws IOException
	{
		Files.createDirectories(output);
		String header = "# Offline review\n\nSYNTHETIC evidence; no OSRS login or gameplay.\n\n"
			+ (passed() ? "PASS" : "FAIL") + " · " + checks.size() + " checks · " + failures + " failures\n";
		String table = "\n## Expected versus actual\n\n| Result | Check | Expected | Actual |\n|---|---|---|---|\n"
			+ String.join("\n", checks) + "\n";
		Files.writeString(output.resolve("report.md"), header + body + table, StandardCharsets.UTF_8);
	}
}
