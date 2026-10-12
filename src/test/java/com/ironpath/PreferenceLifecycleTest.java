package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.preferences.LocalPreferences;
import com.ironpath.preferences.PreferenceStorage;
import com.ironpath.preferences.Preferences;
import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.WorldType;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ConfigProfile;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Real sidebar actions with mocked client events and deliberately delayed worker/client queues. */
public class PreferenceLifecycleTest
{
	private IronpathPlugin plugin;
	private Client client;
	private ConfigManager config;
	private ConfigProfile profile;
	private IronpathPanel panel;
	private final Queue<Runnable> workers = new ArrayDeque<>(), callbacks = new ArrayDeque<>();
	private final Map<String, Preferences> files = new HashMap<>();
	private boolean readFails, writeFails;
	private static final String A = "10:synthetic-rs-a:1", B = "10:synthetic-rs-b:2";

	@Before public void setup() throws Exception
	{
		plugin = new IronpathPlugin(); client = mock(Client.class); config = mock(ConfigManager.class); profile = mock(ConfigProfile.class);
		plugin.client = client; plugin.configManager = config; plugin.clientThread = mock(ClientThread.class);
		plugin.toolbar = mock(ClientToolbar.class); plugin.gson = new Gson(); plugin.reader = new AccountSnapshotReader(client);
		plugin.executor = mock(ScheduledExecutorService.class);
		doAnswer(c -> { workers.add(c.getArgument(0)); return null; }).when(plugin.executor).execute(any(Runnable.class));
		doAnswer(c -> { callbacks.add(c.getArgument(0)); return null; }).when(plugin.clientThread).invokeLater(any(Runnable.class));
		plugin.preferenceStorage = new PreferenceStorage()
		{
			public Preferences read(String key) throws IOException
			{ if (readFails) { throw new IOException("synthetic unreadable preferences"); } return files.getOrDefault(key, Preferences.EMPTY); }
			public void write(String key, Preferences preferences) throws IOException
			{ if (writeFails) { throw new IOException("synthetic unwritable preferences"); } files.put(key, preferences); }
		};
		when(client.isClientThread()).thenReturn(true); when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.getWorldType()).thenReturn(EnumSet.of(WorldType.MEMBERS)); when(client.getVarbitValue(VarbitID.IRONMAN)).thenReturn(1);
		when(client.getRealSkillLevel(any(Skill.class))).thenReturn(90);
		when(config.getProfile()).thenReturn(profile); when(profile.getId()).thenReturn(10L);
		characterA();
		plugin.startUp(); SwingUtilities.invokeAndWait(() -> {});
		ArgumentCaptor<NavigationButton> nav = ArgumentCaptor.forClass(NavigationButton.class);
		verify(plugin.toolbar).addNavigation(nav.capture()); panel = (IronpathPanel) nav.getValue().getPanel();
	}
	@After public void cleanup() throws Exception { plugin.shutDown(); SwingUtilities.invokeAndWait(() -> {}); }
	private void characterA() { when(config.getRSProfileKey()).thenReturn("synthetic-rs-a"); when(client.getAccountHash()).thenReturn(1L); }
	private void characterB() { when(config.getRSProfileKey()).thenReturn("synthetic-rs-b"); when(client.getAccountHash()).thenReturn(2L); }
	private void callbacks() throws Exception { while (!callbacks.isEmpty()) { callbacks.remove().run(); } SwingUtilities.invokeAndWait(() -> {}); }
	private void tick() throws Exception { callbacks(); plugin.onGameTick(new GameTick()); SwingUtilities.invokeAndWait(() -> {}); }
	private void load() throws Exception { while (!workers.isEmpty()) { workers.remove().run(); } callbacks(); }
	private void login() throws Exception { tick(); tick(); load(); }

	private static <T extends Component> T find(Container parent, Class<T> type, java.util.function.Predicate<T> test)
	{
		for (Component child : parent.getComponents())
		{
			if (type.isInstance(child) && test.test(type.cast(child))) { return type.cast(child); }
			if (child instanceof Container)
			{ T result = find((Container) child, type, test); if (result != null) { return result; } }
		}
		return null;
	}
	private JButton button(String label) { return find(panel, JButton.class, b -> b.getText().equals(label)); }
	private void setGoal() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			JComboBox<?> goals = find(panel, JComboBox.class, c -> c.getItemAt(0).toString().equals("No goal"));
			assertNotNull(goals); goals.setSelectedIndex(2); // Rigour
			button("Set goal").doClick();
		});
	}

	@Test public void delayedLoadAndOldUiActionCannotApplyToAnotherCharacter() throws Exception
	{
		files.put(LocalPreferences.key(A), Preferences.EMPTY.goal("train-rigour").confirm("rigour", true, Instant.EPOCH));
		tick(); tick(); assertFalse(plugin.session.preferencesLoaded());
		characterB(); tick(); load();
		assertTrue(plugin.session.preferencesLoaded()); assertNull(plugin.session.preferences().goal);
		assertTrue(plugin.session.preferences().confirmations.isEmpty()); assertFalse(plugin.session.snapshot().bank.known());
		characterA(); tick(); load(); assertEquals("train-rigour", plugin.session.preferences().goal);
		setGoal(); // UI change queued while A is current.
		characterB(); plugin.onProfileChanged(new ProfileChanged()); tick(); tick(); load();
		assertNull(plugin.session.preferences().goal); assertFalse(files.containsKey(LocalPreferences.key(B)));
	}

	@Test public void acceptedSaveUsesOriginalCharacterAndRestartLoadsOnlyThatProfilesChoices() throws Exception
	{
		login(); setGoal(); callbacks(); // Save accepted for A, worker write still delayed.
		characterB(); tick(); load();
		assertEquals("train-rigour", files.get(LocalPreferences.key(A)).goal);
		assertNull(plugin.session.preferences().goal); assertFalse(files.containsKey(LocalPreferences.key(B)));
		characterA(); tick(); load(); assertEquals("train-rigour", plugin.session.preferences().goal);
		when(profile.getId()).thenReturn(11L); tick(); load(); assertNull(plugin.session.preferences().goal);
		when(profile.getId()).thenReturn(10L); tick(); load(); assertEquals("train-rigour", plugin.session.preferences().goal);
		plugin.shutDown(); assertTrue(plugin.session.preferences().confirmations.isEmpty()); assertNull(plugin.session.preferences().goal);
		plugin.startUp(); tick(); tick(); load(); assertEquals("train-rigour", plugin.session.preferences().goal);
	}

	@Test public void corruptReadDisablesEditingAndDisableRejectsPendingLoads() throws Exception
	{
		readFails = true; login(); assertTrue(plugin.session.preferencesLoaded()); assertFalse(plugin.session.writable());
		assertTrue(plugin.session.storageStatus().contains("Editing disabled")); assertNull(button("Set goal"));
		readFails = false; characterB(); tick(); assertFalse(plugin.session.preferencesLoaded());
		plugin.shutDown(); load(); assertFalse(plugin.session.preferencesLoaded()); assertNull(plugin.session.snapshot());
		assertTrue(files.isEmpty());
	}

	@Test public void failedSaveIsVisibleAndUnsupportedWorldClearsPlanningState() throws Exception
	{
		login(); writeFails = true; setGoal(); callbacks(); load();
		assertEquals("train-rigour", plugin.session.preferences().goal);
		assertTrue(plugin.session.storageStatus().contains("save failed")); assertTrue(files.isEmpty());
		when(client.getWorldType()).thenReturn(EnumSet.of(WorldType.MEMBERS, WorldType.PVP_ARENA));
		tick(); assertNull(plugin.session.snapshot()); assertNull(plugin.session.preferences().goal);
		assertFalse(plugin.session.preferencesLoaded());
		when(client.getWorldType()).thenReturn(EnumSet.of(WorldType.MEMBERS)); tick(); load();
		assertNull(plugin.session.preferences().goal); assertTrue(plugin.session.preferencesLoaded());
	}

	@Test public void manualConfirmClearSkipRestoreAndResetControlsUpdateOnlyExplicitChoices() throws Exception
	{
		login();
		SwingUtilities.invokeAndWait(() -> button("Owned / unlocked").doClick()); callbacks(); load();
		assertTrue(plugin.session.preferences().confirmations.get("rigour").value);
		SwingUtilities.invokeAndWait(() -> button("Clear selected fact").doClick()); callbacks(); load();
		assertTrue(plugin.session.preferences().confirmations.isEmpty());
		SwingUtilities.invokeAndWait(() -> button("Skip selected action").doClick()); callbacks(); load();
		assertTrue(plugin.session.preferences().skipped.contains("unlock-rigour"));
		SwingUtilities.invokeAndWait(() -> button("Restore action").doClick()); callbacks(); load();
		assertTrue(plugin.session.preferences().skipped.isEmpty());
		setGoal(); callbacks(); load();
		SwingUtilities.invokeAndWait(() -> button("Reset all planning choices").doClick()); callbacks(); load();
		assertNull(plugin.session.preferences().goal); assertTrue(plugin.session.preferences().skipped.isEmpty());
		assertTrue(plugin.session.preferences().confirmations.isEmpty());
	}
}
