package com.ironpath.preferences;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ironpath.advisor.KnowledgeCatalog;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import net.runelite.client.util.Filepath;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class LocalPreferencesTest
{
	@Rule public TemporaryFolder temporary = new TemporaryFolder();
	private Filepath directory;
	private KnowledgeCatalog catalog;
	private LocalPreferences store;
	private final String a = LocalPreferences.key("synthetic-config-10:rs-a:character-1");
	private static final Instant TIME = Instant.parse("2026-10-12T12:00:00Z");
	@Before public void setup() throws Exception
	{
		// Unchecked access is test-only, to give the supported store an isolated temporary root.
		directory = Filepath.Unchecked.getRooted(temporary.getRoot().toPath());
		catalog = KnowledgeCatalog.load(new Gson());
		store = new LocalPreferences(() -> directory, new Gson(), catalog);
	}

	@Test public void restartRetainsOnlyUserChoicesAndDoesNotLeakAcrossCharacterOrProfile() throws Exception
	{
		Preferences preferences = Preferences.EMPTY.goal("train-rigour").skip("unlock-rigour", true).confirm("item-12932", false, TIME);
		store.write(a, preferences);
		LocalPreferences restarted = new LocalPreferences(() -> directory, new Gson(), catalog);
		Preferences read = restarted.read(a);
		assertEquals(preferences.goal, read.goal); assertEquals(preferences.skipped, read.skipped);
		assertFalse(read.confirmations.get("item-12932").value); assertEquals(TIME, read.confirmations.get("item-12932").time);
		assertTrue(restarted.read(LocalPreferences.key("synthetic-config-10:rs-b:character-2")).confirmations.isEmpty());
		assertNull(restarted.read(LocalPreferences.key("synthetic-config-11:rs-a:character-1")).goal);
		String text = Files.readString(temporary.getRoot().toPath().resolve(a + ".json"));
		JsonObject json = new JsonParser().parse(text).getAsJsonObject();
		assertEquals(Set.of("schemaVersion", "goal", "skipped", "confirmations", "method", "upgradedDeathCharge", "locks"), json.keySet());
		assertEquals(Set.of("value", "time"), json.getAsJsonObject("confirmations").getAsJsonObject("item-12932").keySet());
		assertFalse(text.contains("synthetic-config")); assertFalse(text.contains("bank")); assertFalse(text.contains("levels"));
		store.write(a, Preferences.EMPTY);
		assertTrue(restarted.read(a).confirmations.isEmpty()); assertTrue(restarted.read(a).skipped.isEmpty()); assertNull(restarted.read(a).goal);
		assertEquals(1, temporary.getRoot().list().length); // No abandoned temporary file.
	}

	@Test public void firstSaveCreatesMissingPluginDirectoryAndReloads() throws Exception
	{
		Filepath pluginDirectory = directory.joinSegment("plugin-data").joinSegment("ironpath-advisor");
		LocalPreferences firstRun = new LocalPreferences(() -> pluginDirectory, new Gson(), catalog);
		assertFalse(pluginDirectory.exists());
		assertNull(firstRun.read(a).goal);
		Preferences preferences = Preferences.EMPTY.goal("plan-team-cox").confirm("rigour", true, TIME);
		firstRun.write(a, preferences);
		assertTrue(pluginDirectory.isDirectory());
		LocalPreferences restarted = new LocalPreferences(() -> pluginDirectory, new Gson(), catalog);
		Preferences read = restarted.read(a);
		assertEquals(preferences.goal, read.goal);
		assertTrue(read.confirmations.get("rigour").value);
		assertEquals(TIME, read.confirmations.get("rigour").time);
	}

	@Test public void schemaThreeRetainsOnlyExplicitMethodAndLocks() throws Exception
	{
		NexMethod method=new NexMethod(false,Map.of("melee/shield",22322,"ranged/shield",0));
		store.write(a,Preferences.EMPTY.nex(method)); Preferences read=store.read(a);
		assertFalse(read.nex.upgradedDeathCharge); assertEquals(method.locks,read.nex.locks);
		assertTrue(store.read(LocalPreferences.key("other-profile")).nex.locks.isEmpty());
		assertThrows(IllegalArgumentException.class,()->new NexMethod(true,Map.of("inventory/weapon",1)));
		assertThrows(IllegalArgumentException.class,()->method.lock("melee/weapon",-1));
		String text=Files.readString(temporary.getRoot().toPath().resolve(a+".json"));
		assertFalse(text.contains("timeline")); assertFalse(text.contains("damage")); assertFalse(text.contains("roster"));
	}

	private void raw(String text) throws Exception { try (Writer writer = directory.joinSegment(a + ".json").openWriter()) { writer.write(text); } }
	@Test public void corruptUnsupportedAndOversizedFilesFailWithoutBeingOverwritten() throws Exception
	{
		for (String text : new String[]{"{broken", "{\"schemaVersion\":99}",
			"{\"schemaVersion\":1,\"skipped\":[],\"confirmations\":{\"rigour\":{\"value\":true,\"time\":\"invalid\"}}}", " ".repeat(65537)})
		{
			raw(text); assertThrows(IOException.class, () -> store.read(a));
			assertEquals(text, Files.readString(temporary.getRoot().toPath().resolve(a + ".json")));
		}
		assertThrows(IllegalArgumentException.class, () -> store.read("../outside"));
	}

	@Test public void retiredIdsAreDroppedAndUnsupportedChoicesCannotBeSaved() throws Exception
	{
		raw("{\"schemaVersion\":1,\"goal\":\"retired\",\"skipped\":[\"retired\",\"unlock-rigour\"],\"confirmations\":{\"retired\":{\"value\":true}}}");
		Preferences read = store.read(a);
		assertNull(read.goal); assertEquals(Set.of("unlock-rigour"), read.skipped); assertTrue(read.confirmations.isEmpty());
		assertThrows(IllegalArgumentException.class, () -> store.write(a, Preferences.EMPTY.goal("arbitrary-goal")));
	}

	@Test public void serializedWritesAndLoadsPreserveOrderEvenAfterFailure() throws Exception
	{
		Queue<Runnable> tasks = new ArrayDeque<>();
		PreferenceIO io = new PreferenceIO(store, tasks::add);
		var failed = io.save("invalid-key", Preferences.EMPTY);
		Preferences latest = Preferences.EMPTY.goal("train-augury");
		var first = io.save(a, Preferences.EMPTY.goal("train-rigour"));
		var second = io.save(a, latest);
		var read = io.load(a);
		assertFalse(read.isDone()); assertEquals(1, tasks.size());
		while (!tasks.isEmpty()) { tasks.remove().run(); }
		assertTrue(failed.isCompletedExceptionally()); assertFalse(first.isCompletedExceptionally());
		assertFalse(second.isCompletedExceptionally()); assertEquals(latest.goal, read.join().goal);
	}
	@Test public void t3SchemaMigratesStableIdsAndNewActivityChoicesSurviveRestartInIsolation() throws Exception
	{
		raw("{\"schemaVersion\":1,\"goal\":\"assemble-swamp-trident\",\"skipped\":[\"unlock-rigour\",\"retired\"],\"confirmations\":{\"item-27277\":{\"value\":true,\"time\":\"2026-10-12T12:00:00Z\"}}}");
		Preferences migrated = store.read(a);
		assertEquals("assemble-swamp-trident", migrated.goal);
		assertEquals(Set.of("unlock-rigour"), migrated.skipped);
		assertTrue(migrated.confirmations.get("item-27277").value);
		assertEquals(TIME, migrated.confirmations.get("item-27277").time);
		store.write(a, migrated.goal("ranged-options").skip("cg", true).confirm("owns-bowfaOrBetter", false, TIME));
		LocalPreferences restarted = new LocalPreferences(() -> directory, new Gson(), catalog);
		Preferences reloaded = restarted.read(a);
		assertEquals(Set.of("unlock-rigour", "cg"), reloaded.skipped);
		assertEquals("ranged-options", reloaded.goal);
		assertFalse(reloaded.confirmations.get("owns-bowfaOrBetter").value);
		JsonObject doc = new JsonParser().parse(Files.readString(temporary.getRoot().toPath().resolve(a + ".json"))).getAsJsonObject();
		assertEquals(3, doc.get("schemaVersion").getAsInt());
		assertEquals(Set.of("schemaVersion", "goal", "skipped", "confirmations", "method", "upgradedDeathCharge", "locks"), doc.keySet());
		assertTrue(restarted.read(LocalPreferences.key("synthetic-config-11:rs-a:character-1")).skipped.isEmpty());
		assertTrue(restarted.read(LocalPreferences.key("synthetic-config-10:rs-b:character-2")).confirmations.isEmpty());
	}

}
