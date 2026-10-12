package com.ironpath.preferences;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.ironpath.advisor.Advisor;
import com.ironpath.advisor.KnowledgeCatalog;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.runelite.client.util.Filepath;

/** Plugin-owned local JSON, never ConfigManager. Keys contain no username or raw account ID. */
public final class LocalPreferences implements PreferenceStorage
{
	@FunctionalInterface public interface Directory { Filepath get() throws IOException; }
	private final Directory directory;
	private final Gson gson;
	private final Set<String> decisions, goals, facts;
	public LocalPreferences(Directory directory, Gson gson, KnowledgeCatalog catalog)
	{
		this.directory = directory;
		this.gson = gson;
		decisions = catalog.decisions.stream().map(d -> d.id).collect(Collectors.toSet());
		goals = Advisor.goalChoices(catalog).keySet();
		facts = catalog.factChoices().keySet();
	}

	/** Hash the RuneLite config-profile + RS-profile + character identity in memory. */
	public static String key(String identity)
	{
		try
		{
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(identity.getBytes(StandardCharsets.UTF_8));
			StringBuilder key = new StringBuilder();
			for (byte b : digest) { key.append(String.format(java.util.Locale.ROOT, "%02x", b & 255)); }
			return key.toString();
		}
		catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
	}

	private Filepath file(String key) throws IOException
	{
		if (!key.matches("[a-f0-9]{64}")) { throw new IllegalArgumentException("Invalid local preference key"); }
		return directory.get().joinSegment(key + ".json");
	}

	@Override public Preferences read(String key) throws IOException
	{
		Filepath file = file(key);
		if (!file.exists()) { return Preferences.EMPTY; }
		if (file.size() > 65536) { throw new IOException("Preference file too large"); }
		try (Reader reader = file.openReader())
		{
			Document doc = gson.fromJson(reader, Document.class);
			if (doc == null || doc.schemaVersion != 1 || doc.skipped == null || doc.confirmations == null
				|| doc.skipped.size() > 12 || doc.confirmations.size() > 64)
			{ throw new IllegalArgumentException("Unsupported preferences"); }
			// Retired catalogue IDs are dropped, never executed or converted into observations.
			String goal = goals.contains(doc.goal) ? doc.goal : null;
			Set<String> skipped = new HashSet<>(doc.skipped); skipped.retainAll(decisions);
			Map<String, Preferences.Confirmation> confirmations = new HashMap<>();
			for (Map.Entry<String, Fact> entry : doc.confirmations.entrySet())
			{
				if (!facts.contains(entry.getKey())) { continue; }
				Fact fact = entry.getValue();
				if (fact == null || fact.value == null || fact.time == null) { throw new IllegalArgumentException("Invalid confirmation"); }
				confirmations.put(entry.getKey(), new Preferences.Confirmation(fact.value, Instant.parse(fact.time)));
			}
			return new Preferences(goal, skipped, confirmations);
		}
		catch (JsonParseException | IllegalArgumentException | java.time.DateTimeException ex) { throw new IOException("Invalid preference file", ex); }
	}

	@Override public void write(String key, Preferences preferences) throws IOException
	{
		if ((preferences.goal != null && !goals.contains(preferences.goal)) || !decisions.containsAll(preferences.skipped)
			|| !facts.containsAll(preferences.confirmations.keySet())) { throw new IllegalArgumentException("Unsupported preferences"); }
		Document doc = new Document();
		doc.schemaVersion = 1; doc.goal = preferences.goal; doc.skipped = preferences.skipped;
		doc.confirmations = new HashMap<>();
		preferences.confirmations.forEach((id, confirmation) ->
		{
			Fact fact = new Fact(); fact.value = confirmation.value; fact.time = confirmation.time.toString();
			doc.confirmations.put(id, fact);
		});
		Filepath destination = file(key);
		destination.getParent().createDirectories();
		Filepath temporary = destination.getParent().createTempFile("preferences-", ".tmp");
		try
		{
			try (Writer writer = temporary.openWriter()) { gson.toJson(doc, writer); }
			try { temporary.moveTo(destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
			catch (AtomicMoveNotSupportedException ex) { temporary.moveTo(destination, StandardCopyOption.REPLACE_EXISTING); }
		}
		finally { temporary.deleteIfExists(); }
	}

	private static final class Document
	{
		int schemaVersion;
		String goal;
		Set<String> skipped;
		Map<String, Fact> confirmations;
	}
	private static final class Fact { Boolean value; String time; }
}
