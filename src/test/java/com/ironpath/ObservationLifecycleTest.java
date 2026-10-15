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
import net.runelite.api.gameval.VarPlayerID;
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
	@Test public void selectedQuestScriptsAndPrayerFactsRefreshWithoutLeakingAcrossCharacters()
	{
		when(client.getIntStack()).thenReturn(new int[]{2}); // Quest.getState's supported script result: finished.
		observeBank();
		assertTrue(plugin.session.snapshot().fact("song-of-elves").value());
		assertTrue(plugin.session.snapshot().fact("frozen-door").value());
		assertTrue(plugin.session.snapshot().fact("final-dawn").value());
		assertTrue(plugin.session.snapshot().fact("blood-moon-rises").value());
		assertTrue(plugin.session.snapshot().fact("dragon-slayer-ii").value());
		assertTrue(plugin.session.snapshot().fact("while-guthix-sleeps").value());
		assertTrue(plugin.session.snapshot().fact("beneath-cursed-sands").source().contains("Quest.getState"));
		when(client.getIntStack()).thenReturn(new int[]{1});
		VarbitChanged changed = new VarbitChanged(); changed.setVarbitId(VarbitID.BCS);
		plugin.onVarbitChanged(changed); tick();
		assertFalse(plugin.session.snapshot().fact("beneath-cursed-sands").value());
		when(client.getIntStack()).thenReturn(new int[]{2});
		VarbitChanged dawn = new VarbitChanged(); dawn.setVarpId(VarPlayerID.VMQ4_PRIMARY);
		plugin.onVarbitChanged(dawn); tick(); assertTrue(plugin.session.snapshot().fact("final-dawn").value());
		when(client.getIntStack()).thenReturn(new int[]{1});
		VarbitChanged blood = new VarbitChanged(); blood.setVarpId(VarPlayerID.MYQ6_PRIMARY);
		plugin.onVarbitChanged(blood); tick(); assertFalse(plugin.session.snapshot().fact("blood-moon-rises").value());
		when(client.getIntStack()).thenReturn(new int[]{2});
		VarbitChanged dragon = new VarbitChanged(); dragon.setVarpId(VarPlayerID.DRAGONSLAYER2_MAIN);
		plugin.onVarbitChanged(dragon); tick(); assertTrue(plugin.session.snapshot().fact("dragon-slayer-ii").value());
		when(client.getIntStack()).thenReturn(new int[]{1});
		VarbitChanged frozen = new VarbitChanged(); frozen.setVarpId(VarPlayerID.NEX_PERM);
		plugin.onVarbitChanged(frozen); tick(); assertFalse(plugin.session.snapshot().fact("frozen-door").value());
		VarbitChanged wgs = new VarbitChanged(); wgs.setVarpId(VarPlayerID.WGS_PRIMARY);
		plugin.onVarbitChanged(wgs); tick(); assertFalse(plugin.session.snapshot().fact("while-guthix-sleeps").value());
		when(client.getVarbitValue(VarbitID.PRAYER_DEADEYE_UNLOCKED)).thenReturn(1);
		plugin.onVarbitChanged(new VarbitChanged()); tick();
		assertTrue(plugin.session.snapshot().fact("deadeye").value());
		when(client.getAccountHash()).thenReturn(2L);
		when(config.getRSProfileKey()).thenReturn("synthetic-character-b");
		plugin.onRuneScapeProfileChanged(new RuneScapeProfileChanged("synthetic-character-a", "synthetic-character-b"));
		assertNull(plugin.session.snapshot());
		when(client.getIntStack()).thenReturn(null); when(client.getVarbitValue(VarbitID.PRAYER_DEADEYE_UNLOCKED)).thenReturn(0);
		tick(); tick();
		assertFalse(plugin.session.snapshot().bank.known());
		assertFalse(plugin.session.snapshot().fact("song-of-elves").known());
		assertFalse(plugin.session.snapshot().fact("frozen-door").known());
		assertFalse(plugin.session.snapshot().fact("final-dawn").known());
		assertFalse(plugin.session.snapshot().fact("blood-moon-rises").known());
		assertFalse(plugin.session.snapshot().fact("dragon-slayer-ii").known());
		assertFalse(plugin.session.snapshot().fact("while-guthix-sleeps").known());
		assertFalse(plugin.session.snapshot().fact("deadeye").value());
	}

	@Test public void unavailableQuestScriptRemainsUnknownAndReaderRequiresClientThread()
	{
		when(client.getIntStack()).thenReturn(null);
		AccountSnapshot snapshot = plugin.reader.read(com.ironpath.model.Observation.unknown());
		assertFalse(snapshot.fact("regicide").known());
		when(client.isClientThread()).thenReturn(false);
		assertThrows(IllegalStateException.class, () -> plugin.reader.read(com.ironpath.model.Observation.unknown()));
	}

	@Test public void queuedRecentBossResultCannotSurviveProfileLoadingDisableOrAccountSwitch()
	{
		for(String boundary:java.util.List.of("profile","loading","account"))
		{
			observeBank();
			plugin.recentBoss.enable(true,plugin.session.generation());
			var capture=plugin.recentBoss.capture;
			var state=com.ironpath.advisor.ZilyanaWornStateComparisonTest.state(25867,0,99);
			var context=new com.ironpath.encounter.EncounterContext(null,null,"zilyana","ordinary","normal",1,"solo",java.util.Map.of());
			capture.start(99,plugin.session.generation(),context,"synthetic",10,state);
			capture.localDamage(11,20);
			for(int i=11;i<20;i++) { capture.sample(i,state); }
			capture.death(20); capture.loot(20); capture.sample(25,state);
			plugin.onGameTick(new GameTick());
			assertFalse("Comparison publication should be queued",delayed.isEmpty());
			if(boundary.equals("profile")) { plugin.onProfileChanged(new ProfileChanged()); }
			else if(boundary.equals("loading")) { plugin.onGameStateChanged(new GameStateChanged()); }
			else { when(client.getAccountHash()).thenReturn(2L); plugin.onGameTick(new GameTick()); }
			while(!delayed.isEmpty()) { delayed.remove().run(); }
			assertNull("Stale publication at "+boundary,plugin.recentImpact);
			assertTrue(plugin.recentBoss.capture.recent().isEmpty());
		}
	}

	@Test public void nexSummarySurvivesArenaBankArenaAndAccountSwitchDuringPendingBankingClearsIt()
	{
		observeBank();
		when(client.getWorld()).thenReturn(301);
		plugin.nexSession.select("10:synthetic-character-a:1",301);
		plugin.nexSession.add(com.ironpath.nex.NexCaptureTest.replay(0,20,60));
		// Establish a reviewed arena source using the actual public adapter.
		var player=mock(net.runelite.api.Player.class); var world=mock(net.runelite.api.WorldView.class);
		var boss=mock(net.runelite.api.NPC.class);
		when(client.getLocalPlayer()).thenReturn(player); when(player.getWorldView()).thenReturn(world);
		when(boss.getWorldView()).thenReturn(world); when(boss.getId()).thenReturn(net.runelite.api.gameval.NpcID.NEX);
		when(client.getVarbitValue(VarbitID.NEX_BARRIER)).thenReturn(2);
		plugin.nex.spawn(boss); plugin.nex.barrier();
		GameStateChanged loading=new GameStateChanged(); loading.setGameState(GameState.LOADING);
		plugin.onGameStateChanged(loading); assertNull(plugin.session.snapshot()); assertTrue(plugin.nexSession.suspended());
		assertTrue(plugin.nexSession.recent().isEmpty()); assertFalse(plugin.nex.capture.active());
		when(client.getWidget(InterfaceID.Bankmain.ITEMS)).thenReturn(bankWidget);
		tick(); tick(); assertFalse(plugin.nexSession.suspended()); assertEquals(1,plugin.nexSession.recent().size());
		assertFalse(plugin.session.snapshot().bank.known()); // Revalidated summary is separate from the cleared account/bank dump.
		plugin.onGameStateChanged(loading); assertTrue(plugin.nexSession.suspended());
		when(client.getWidget(InterfaceID.Bankmain.ITEMS)).thenReturn(null);
		net.runelite.api.IndexedObjectSet actors=mock(net.runelite.api.IndexedObjectSet.class);
		when(client.getTopLevelWorldView()).thenReturn(world); when(world.npcs()).thenReturn(actors);
		when(actors.stream()).thenAnswer(i->java.util.stream.Stream.of(boss));
		tick(); tick(); assertFalse(plugin.nexSession.suspended()); assertEquals(1,plugin.nexSession.recent().size());
		plugin.nex.spawn(boss); plugin.nex.barrier(); plugin.onGameStateChanged(loading);
		when(config.getRSProfileKey()).thenReturn("synthetic-character-b"); when(client.getAccountHash()).thenReturn(2L);
		when(client.getWidget(InterfaceID.Bankmain.ITEMS)).thenReturn(bankWidget);
		when(actors.stream()).thenAnswer(i->java.util.stream.Stream.empty());
		tick(); tick(); assertTrue(plugin.nexSession.recent().isEmpty());
		assertFalse(plugin.nex.capture.active());
	}
	@Test public void nexHistoryClearsOnEveryHardBoundaryAndDisable()
	{
		for(String boundary:java.util.List.of("profile","rs-profile","logout","unsupported","reset"))
		{
			tick(); tick(); observeBank(); plugin.nexSession.select("synthetic",301);
			plugin.nexSession.add(com.ironpath.nex.NexCaptureTest.replay(0,20,60));
			if(boundary.equals("profile")) { plugin.onProfileChanged(new ProfileChanged()); }
			else if(boundary.equals("rs-profile")) { plugin.onRuneScapeProfileChanged(new RuneScapeProfileChanged("a","b")); }
			else if(boundary.equals("unsupported"))
			{
				when(client.getWorldType()).thenReturn(EnumSet.of(WorldType.MEMBERS,WorldType.PVP)); tick();
				when(client.getWorldType()).thenReturn(EnumSet.of(WorldType.MEMBERS));
			}
			else if(boundary.equals("reset")) { plugin.nexSession.clear(); plugin.nex.clearActive(); }
			else { GameStateChanged logout=new GameStateChanged(); logout.setGameState(GameState.LOGIN_SCREEN); plugin.onGameStateChanged(logout); }
			assertTrue(boundary,plugin.nexSession.recent().isEmpty()); assertFalse(plugin.nex.capture.active());
		}
		tick(); tick(); observeBank(); plugin.nexSession.add(com.ironpath.nex.NexCaptureTest.replay(0,20,60));
		plugin.shutDown(); assertTrue(plugin.nexSession.recent().isEmpty());
	}

	@Test public void supportedWorldChangeWithoutStateEventStillClearsNexTraceAndHistory()
	{
		observeBank(); plugin.nexSession.add(com.ironpath.nex.NexCaptureTest.replay(0,20,60));
		plugin.nex.capture.start(0,true,com.ironpath.preferences.NexMethod.PILOT,"synthetic-roster");
		assertTrue(plugin.nex.capture.active()); when(client.getWorld()).thenReturn(302); tick();
		assertFalse(plugin.nex.capture.active()); assertTrue(plugin.nexSession.recent().isEmpty());
	}

}
