package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.NexOwnedFixtures;
import com.ironpath.advisor.NexOwnedResult;
import com.ironpath.dev.OrderedReplay;
import com.ironpath.dev.PanelPreview;
import com.ironpath.dev.ReplayQueue;
import com.ironpath.nex.NexSummary;
import com.ironpath.preferences.NexMethod;
import com.ironpath.preferences.PreferenceStorage;
import com.ironpath.preferences.Preferences;
import java.awt.Font;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ConfigProfile;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Real plugin lifecycle and handlers, with controlled client/worker queues and in-memory preferences. */
final class OfflinePluginReplay extends OfflineNexClient implements AutoCloseable
{
	final IronpathPlugin plugin = new IronpathPlugin();
	final ReplayQueue callbacks = new ReplayQueue(), workers = new ReplayQueue();
	final List<String> trace = new ArrayList<>();
	final List<NexSummary> encounters = new ArrayList<>();
	final java.util.Map<Long, NexOwnedResult> results = new java.util.LinkedHashMap<>();
	final ConfigManager config = mock(ConfigManager.class);
	private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> new Thread(r, "offline-plugin-worker"));
	private final Widget bank = mock(Widget.class);
	private boolean bankVisible;
	NexPanel panel;

	OfflinePluginReplay(int font) throws Exception
	{
		try
		{
			when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
			when(client.getWorld()).thenReturn(301);
			when(client.getWorldType()).thenReturn(EnumSet.of(WorldType.MEMBERS));
			when(client.getAccountHash()).thenReturn(1L);
			// These account facts are explicitly synthetic; unavailable quest scripts remain unknown.
			doReturn(1).when(client).getVarbitValue(VarbitID.IRONMAN);
			for (int id : List.of(VarbitID.PRAYER_RIGOUR_UNLOCKED, VarbitID.PRAYER_AUGURY_UNLOCKED))
			{ doReturn(1).when(client).getVarbitValue(id); }
			for (int id : List.of(VarbitID.PRAYER_DEADEYE_UNLOCKED, VarbitID.PRAYER_MYSTIC_VIGOUR_UNLOCKED))
			{ doReturn(0).when(client).getVarbitValue(id); }
			when(client.getWidget(InterfaceID.Bankmain.ITEMS)).thenAnswer(i -> bankVisible ? bank : null);
			when(client.getItemContainer(InventoryID.BANK)).thenReturn(null);
			when(config.getRSProfileKey()).thenReturn("offline-synthetic-a");
			ConfigProfile profile = mock(ConfigProfile.class); when(profile.getId()).thenReturn(10L);
			when(config.getProfile()).thenReturn(profile);
			plugin.client = client; plugin.configManager = config; plugin.gson = new Gson();
			plugin.reader = new AccountSnapshotReader(client);
			plugin.clientThread = mock(ClientThread.class); plugin.toolbar = mock(ClientToolbar.class);
			plugin.executor = mock(ScheduledExecutorService.class);
			doAnswer(call -> { callbacks.add(call.getArgument(0)); return null; })
				.when(plugin.clientThread).invokeLater(any(Runnable.class));
			doAnswer(call -> { workers.add(call.getArgument(0)); return null; })
				.when(plugin.executor).execute(any(Runnable.class));
			doAnswer(call -> { panel = (NexPanel) ((NavigationButton) call.getArgument(0)).getPanel();
				panel.setFont(new Font("DejaVu Sans", Font.PLAIN, font)); return null; })
				.when(plugin.toolbar).addNavigation(any(NavigationButton.class));
			plugin.preferenceStorage = new PreferenceStorage() {
				public Preferences read(String key) { return Preferences.EMPTY.nex(NexMethod.PILOT.lock("melee/shield", 12954)); }
				public void write(String key, Preferences value) { throw new AssertionError("Replay must not persist preferences"); }
			};
			SwingUtilities.invokeAndWait(() -> PanelPreview.fonts(font));
			plugin.startUp(); edt();
			ready();
		}
		catch (Exception | AssertionError failure)
		{
			try { close(); } catch (Exception cleanup) { failure.addSuppressed(cleanup); }
			throw failure;
		}
	}

	static void edt() throws Exception { SwingUtilities.invokeAndWait(() -> { }); }
	void tick()
	{
		callbacks.drain(); ++tick; plugin.onGameTick(new GameTick());
	}
	void ready() throws Exception
	{
		for (int i = 0; i < 3; i++) { tick(); settle(); }
		if (plugin.session.snapshot() == null || !plugin.session.preferencesLoaded())
		{ throw new IllegalStateException("Plugin observations/preferences did not become ready"); }
	}
	void runWorker(Runnable task) throws Exception
	{ worker.submit(task).get(30, TimeUnit.SECONDS); }
	void settle() throws Exception
	{
		for (int i = 0; workers.size() > 0 || callbacks.size() > 0; i++)
		{
			if (i == 100) { throw new IllegalStateException("Plugin work failed to settle"); }
			while (workers.size() > 0) { runWorker(workers.take()); }
			callbacks.drain();
		}
		if (plugin.ownedResult != null) { results.put(plugin.ownedResult.summary.key, plugin.ownedResult); }
		edt();
	}
	void overhead(String text)
	{ plugin.onOverheadTextChanged(new OverheadTextChanged(boss, text)); }
	void barrier(int next)
	{
		VarbitChanged event = new VarbitChanged(); event.setVarbitId(VarbitID.NEX_BARRIER); event.setValue(next);
		plugin.onVarbitChanged(event); barrier = next; // Event payload precedes getter update.
	}
	private void hit(NPC target, boolean mine, int amount)
	{
		Hitsplat hit = mock(Hitsplat.class);
		when(hit.isMine()).thenReturn(mine); when(hit.isOthers()).thenReturn(!mine);
		when(hit.getHitsplatType()).thenReturn(mine ? HitsplatID.DAMAGE_ME : HitsplatID.DAMAGE_OTHER);
		when(hit.getAmount()).thenReturn(amount);
		HitsplatApplied event = new HitsplatApplied(); event.setActor(target); event.setHitsplat(hit);
		plugin.onHitsplatApplied(event); plugin.onHitsplatApplied(event);
	}

	/** Fresh callbacks produce every summary; no pre-completed summary is injected. */
	NexSummary fight(int duration, int mine, int other, boolean complete) throws Exception
	{
		int start = tick + 1;
		boss = mock(NPC.class);
		when(boss.getWorldView()).thenReturn(world); when(boss.getIndex()).thenReturn(1);
		when(boss.getId()).thenReturn(NpcID.NEX); worn = NexOwnedFixtures.ranged(); special = false;
		OrderedReplay replay = new OrderedReplay();
		replay.at(start, "New Nex actor; arena poll then smoke", () -> {
			plugin.onNpcSpawned(new NpcSpawned(boss)); plugin.onGameTick(new GameTick()); overhead("Fill my soul with smoke!");
		});
		for (int t = start + 1; t <= start + duration + 6; t++)
		{
			int offset = t - start;
			if (offset == 1) { replay.at(t, "Barrier payload 3 before poll", () -> barrier(3)); }
			if (offset == 2) { replay.at(t, "Shadow ranged window", () -> { overhead("Darken my shadow!"); overhead("Embrace darkness!"); }); }
			if (offset == 3) { replay.at(t, "Scoped hits; duplicate callbacks", () -> { hit(boss, true, mine); hit(minion, false, other); }); }
			if (offset == 4) { replay.at(t, "Melee switch with defender", () -> {
				worn = NexOwnedFixtures.melee(); overhead("Fear the shadow!");
				plugin.onItemContainerChanged(new ItemContainerChanged(InventoryID.WORN, client.getItemContainer(InventoryID.WORN)));
			}); }
			if (offset == 6) { replay.at(t, "Blood", () -> overhead("Flood my lungs with blood!")); }
			if (offset == 7) { replay.at(t, "Ice", () -> overhead("Infuse me with the power of ice!")); }
			if (offset == 8) { replay.at(t, "Zaros", () -> overhead("NOW, THE POWER OF ZAROS!")); }
			if (offset == duration)
			{
				if (complete) { replay.at(t, "Dying NPC transformation", () -> {
					NPCComposition previous = mock(NPCComposition.class); when(previous.getId()).thenReturn(NpcID.NEX);
					when(boss.getId()).thenReturn(NpcID.NEX_DYING); plugin.onNpcChanged(new NpcChanged(boss, previous));
				}); }
				else { replay.at(t, "Exit without death/end corroboration", () -> barrier(1)); }
			}
			if (offset == duration + 1 && complete) { replay.at(t, "Arena completion payload", () -> barrier(2)); }
			replay.at(t, "GameTick (client callbacks held)", () -> plugin.onGameTick(new GameTick()));
		}
		replay.run(value -> tick = value, trace::add);
		edt();
		var attempt = plugin.nexSession.stats().lastAttempt;
		if (attempt == null) { throw new IllegalStateException("Public replay produced no attempt"); }
		encounters.add(attempt);
		return attempt;
	}

	void loading()
	{
		++tick; when(client.getGameState()).thenReturn(GameState.LOADING);
		GameStateChanged changed = new GameStateChanged(); changed.setGameState(GameState.LOADING);
		trace.add("tick " + tick + " · LOADING; suspend history and invalidate live work");
		plugin.onGameStateChanged(changed);
	}
	void arrive(boolean atBank) throws Exception
	{
		bankVisible = atBank; bossPresent = !atBank; barrier = atBank ? 0 : 2;
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		GameStateChanged changed = new GameStateChanged(); changed.setGameState(GameState.LOGGED_IN);
		plugin.onGameStateChanged(changed); ready();
		trace.add("tick " + tick + " · Revalidated " + (atBank ? "bank" : "arena return"));
	}
	void changeIdentity()
	{
		when(config.getRSProfileKey()).thenReturn("offline-synthetic-b"); when(client.getAccountHash()).thenReturn(2L);
		trace.add("tick " + tick + " · Character/profile changed before queued publication");
		plugin.onRuneScapeProfileChanged(new RuneScapeProfileChanged("offline-synthetic-a", "offline-synthetic-b"));
	}
	@Override public void close() throws Exception
	{
		try { plugin.shutDown(); callbacks.drain(); edt(); }
		finally { worker.shutdownNow(); }
	}
}
