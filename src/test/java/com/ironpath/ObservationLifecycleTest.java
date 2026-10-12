package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.model.AccountSnapshot;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.Queue;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.api.WorldType;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ConfigProfile;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.ui.ClientToolbar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Mocked API regression tests, separate from product fixtures and in-game validation. */
public class ObservationLifecycleTest
{
	private IronpathPlugin plugin;
	private Client client;
	private ConfigManager config;
	private ItemContainer bank;
	private Widget bankWidget;
	private final Queue<Runnable> delayed = new ArrayDeque<>();

	@Before public void setup() throws Exception
	{
		client = mock(Client.class);
		config = mock(ConfigManager.class);
		plugin = new IronpathPlugin();
		plugin.client = client;
		plugin.configManager = config;
		plugin.clientThread = mock(ClientThread.class);
		plugin.toolbar = mock(ClientToolbar.class);
		plugin.reader = new AccountSnapshotReader(client);
		plugin.gson = new Gson();
		doAnswer(call -> { delayed.add(call.getArgument(0)); return null; }).when(plugin.clientThread).invokeLater(any(Runnable.class));
		when(client.isClientThread()).thenReturn(true);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.getAccountHash()).thenReturn(1L);
		when(config.getRSProfileKey()).thenReturn("synthetic-character-a");
		ConfigProfile profile = mock(ConfigProfile.class);
		when(profile.getId()).thenReturn(10L); when(config.getProfile()).thenReturn(profile);
		plugin.executor = mock(java.util.concurrent.ScheduledExecutorService.class);
		doAnswer(call -> { ((Runnable) call.getArgument(0)).run(); return null; }).when(plugin.executor).execute(any(Runnable.class));
		plugin.preferenceStorage = new com.ironpath.preferences.PreferenceStorage()
		{
			public com.ironpath.preferences.Preferences read(String key) { return com.ironpath.preferences.Preferences.EMPTY; }
			public void write(String key, com.ironpath.preferences.Preferences preferences) {}
		};
		when(client.getWorldType()).thenReturn(EnumSet.of(WorldType.MEMBERS));
		when(client.getVarbitValue(VarbitID.IRONMAN)).thenReturn(1);
		when(client.getRealSkillLevel(any(Skill.class))).thenReturn(90);
		when(client.getVarbitValue(VarbitID.PRAYER_RIGOUR_UNLOCKED)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.PRAYER_AUGURY_UNLOCKED)).thenReturn(1);
		ItemContainer inventory = items();
		ItemContainer equipment = items();
		when(client.getItemContainer(InventoryID.INV)).thenReturn(inventory);
		when(client.getItemContainer(InventoryID.WORN)).thenReturn(equipment);
		bank = items(new Item(ItemID.RAIDS_PRAYERSCROLL, 1));
		when(client.getItemContainer(InventoryID.BANK)).thenReturn(bank);
		bankWidget = mock(Widget.class);
		plugin.startUp();
		SwingUtilities.invokeAndWait(() -> {});
	}

	@After public void cleanup() throws Exception
	{
		plugin.shutDown();
		SwingUtilities.invokeAndWait(() -> {});
	}

	private static ItemContainer items(Item... items)
	{
		ItemContainer container = mock(ItemContainer.class);
		when(container.getItems()).thenReturn(items);
		return container;
	}

	private void tick()
	{
		while (!delayed.isEmpty()) { delayed.remove().run(); }
		plugin.onGameTick(new GameTick());
	}

	private void observeBank()
	{
		when(client.getWidget(InterfaceID.Bankmain.ITEMS)).thenReturn(bankWidget);
		WidgetLoaded loaded = new WidgetLoaded();
		loaded.setGroupId(InterfaceID.BANKMAIN);
		plugin.onWidgetLoaded(loaded);
		plugin.onItemContainerChanged(new ItemContainerChanged(InventoryID.BANK, bank));
		tick(); tick(); tick(); // Settle the queued preference load before comparing idle revisions.
		assertTrue(plugin.session.snapshot().bank.known());
	}

	@Test public void staleClientBankContainerCannotBecomeObservationUntilOpened()
	{
		tick();
		assertNull(plugin.session.snapshot());
		tick();
		assertFalse(plugin.session.snapshot().bank.known());
		observeBank();
		AccountSnapshot observed = plugin.session.snapshot();
		Instant time = observed.bank.time();
		when(client.getWidget(InterfaceID.Bankmain.ITEMS)).thenReturn(null);
		plugin.onItemContainerChanged(new ItemContainerChanged(InventoryID.INV, items()));
		tick();
		assertEquals(time, plugin.session.snapshot().bank.time());
	}

	@Test public void placeholdersAndZeroQuantityNeverEstablishPossession()
	{
		ItemContainer container = items(new Item(ItemID.RAIDS_PRAYERSCROLL, 0), new Item(-1, 1), new Item(ItemID.ABYSSAL_WHIP, 1));
		assertEquals(java.util.Set.of(ItemID.ABYSSAL_WHIP), AccountSnapshotReader.container(container, Instant.EPOCH, "synthetic bank").value());
		assertFalse(AccountSnapshotReader.container(null, Instant.EPOCH, "synthetic bank").known());
	}

	@Test public void everyGameStateTransitionClearsFactsAndRejectsOldResults()
	{
		for (GameState state : GameState.values())
		{
			observeBank();
			AccountSnapshot previous = plugin.session.snapshot();
			long token = plugin.session.generation();
			long revision = plugin.session.revision();
			GameStateChanged changed = new GameStateChanged();
			changed.setGameState(state);
			plugin.onGameStateChanged(changed);
			assertNull("Clearing " + state, plugin.session.snapshot());
			assertFalse(plugin.session.currentView(token, revision));
			assertFalse(plugin.session.publish(token, "synthetic-character-a:1", previous));
			tick(); tick();
			assertFalse(plugin.session.snapshot().bank.known());
		}
	}

	@Test public void profileAndCharacterSwitchCannotReuseBankOrUnlocks()
	{
		observeBank();
		plugin.onProfileChanged(new ProfileChanged());
		assertNull(plugin.session.snapshot());
		tick(); tick();
		assertFalse(plugin.session.snapshot().bank.known());
		observeBank();
		long token = plugin.session.generation();
		AccountSnapshot previous = plugin.session.snapshot();
		when(config.getRSProfileKey()).thenReturn("synthetic-character-b");
		when(client.getAccountHash()).thenReturn(2L);
		when(client.getVarbitValue(VarbitID.PRAYER_RIGOUR_UNLOCKED)).thenReturn(0);
		plugin.onRuneScapeProfileChanged(new RuneScapeProfileChanged("synthetic-character-a", "synthetic-character-b"));
		assertNull(plugin.session.snapshot());
		tick(); tick();
		assertFalse(plugin.session.snapshot().bank.known());
		assertFalse(plugin.session.snapshot().rigour.value());
		assertFalse(plugin.session.publish(token, "synthetic-character-b:2", previous));
	}

	@Test public void identityChangeEvenWithoutEventDropsBank()
	{
		observeBank();
		when(client.getAccountHash()).thenReturn(2L);
		tick();
		assertFalse(plugin.session.snapshot().bank.known());
	}

	@Test public void unsupportedModesAndWorldsClearAndAbstain()
	{
		for (int mode : new int[]{0, 2, 3, 4, 5, 6})
		{
			when(client.getVarbitValue(VarbitID.IRONMAN)).thenReturn(1);
			observeBank();
			when(client.getVarbitValue(VarbitID.IRONMAN)).thenReturn(mode);
			tick();
			assertNull(plugin.session.snapshot());
		}
		when(client.getVarbitValue(VarbitID.IRONMAN)).thenReturn(1);
		for (WorldType world : EnumSet.complementOf(EnumSet.of(WorldType.MEMBERS, WorldType.SKILL_TOTAL)))
		{
			when(client.getWorldType()).thenReturn(EnumSet.of(WorldType.MEMBERS));
			observeBank();
			when(client.getWorldType()).thenReturn(EnumSet.of(WorldType.MEMBERS, world));
			tick();
			assertNull("World " + world, plugin.session.snapshot());
		}
	}

	@Test public void missingProfileAndDisableDropObservations()
	{
		observeBank();
		when(config.getRSProfileKey()).thenReturn(null);
		tick();
		assertNull(plugin.session.snapshot());
		when(config.getRSProfileKey()).thenReturn("synthetic-character-a");
		observeBank();
		plugin.shutDown();
		assertNull(plugin.session.snapshot());
		tick();
		assertNull(plugin.session.snapshot());
	}

	@Test public void irrelevantVarbitsDoNotReevaluateOrChangeEvidenceTime()
	{
		observeBank();
		long revision = plugin.session.revision();
		plugin.onVarbitChanged(new VarbitChanged());
		tick();
		assertEquals(revision, plugin.session.revision());
		when(client.getVarbitValue(VarbitID.PRAYER_AUGURY_UNLOCKED)).thenReturn(0);
		plugin.onVarbitChanged(new VarbitChanged());
		tick();
		assertFalse(plugin.session.snapshot().augury.value());
	}
}
