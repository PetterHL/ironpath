package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.Advisor;
import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.WorldType;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@PluginDescriptor(name = "Ironpath", internalName = "ironpath-advisor", description = "Local CoX preparation experiment for standard Ironmen", tags = {"ironman", "cox", "progression"})
public class IronpathPlugin extends Plugin
{
	@Inject Client client;
	@Inject ClientThread clientThread;
	@Inject ConfigManager configManager;
	@Inject ClientToolbar toolbar;
	@Inject AccountSnapshotReader reader;
	@Inject Gson gson;

	final ObservationSession session = new ObservationSession();
	private KnowledgeCatalog catalog;
	private Advisor advisor;
	private IronpathPanel panel;
	private NavigationButton navigation;
	private volatile boolean running;
	private volatile long lifecycle;
	private boolean dirty;
	private boolean bankDirty;
	private String bankIdentity;
	private int loginTicks;

	@Override
	protected void startUp()
	{
		running = true;
		long startup = ++lifecycle;
		session.clear();
		dirty = true;
		bankDirty = false;
		bankIdentity = null;
		loginTicks = 0;
		String status;
		try
		{
			catalog = KnowledgeCatalog.load(gson);
			advisor = new Advisor(catalog);
			status = "Waiting for login and initial account observations.";
		}
		catch (Exception ex)
		{
			catalog = null;
			advisor = null;
			status = "Bundled catalogue unavailable or invalid. Advice is disabled.";
		}
		String initialStatus = status;
		SwingUtilities.invokeLater(() ->
		{
			if (!running || lifecycle != startup) { return; }
			panel = new IronpathPanel();
			panel.render(initialStatus, null, List.of(), catalog);
			if (!running || lifecycle != startup) { panel = null; return; }
			navigation = NavigationButton.builder().tooltip("Ironpath").icon(icon()).priority(6).panel(panel).build();
			toolbar.addNavigation(navigation);
		});
	}

	@Override
	protected void shutDown()
	{
		running = false;
		++lifecycle;
		session.clear();
		SwingUtilities.invokeLater(() ->
		{
			// UI ownership stays on the EDT, including cleanup of an in-flight startup.
			if (navigation != null) { toolbar.removeNavigation(navigation); }
			if (panel != null) { panel.render("Plugin disabled; observations cleared.", null, List.of(), null); }
			navigation = null;
			panel = null;
		});
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		// LOADING also clears on region changes: deliberately conservative for T1.
		invalidate("Account transition: observations cleared. Reopen your bank after login.");
	}

	@Subscribe public void onProfileChanged(ProfileChanged event) { invalidate("Profile changed; observations cleared."); }
	@Subscribe public void onRuneScapeProfileChanged(RuneScapeProfileChanged event) { invalidate("Character profile changed; observations cleared."); }
	@Subscribe public void onStatChanged(StatChanged event)
	{
		AccountSnapshot previous = session.snapshot();
		if (previous == null || !Integer.valueOf(client.getRealSkillLevel(event.getSkill())).equals(previous.level(event.getSkill().name())))
		{
			dirty = true;
		}
	}
	@Subscribe public void onVarbitChanged(VarbitChanged event)
	{
		AccountSnapshot previous = session.snapshot();
		if (previous == null || !supported() || !previous.rigour.known() || !previous.augury.known()
			|| (previous.rigour.value() ? 1 : 0) != client.getVarbitValue(VarbitID.PRAYER_RIGOUR_UNLOCKED)
			|| (previous.augury.value() ? 1 : 0) != client.getVarbitValue(VarbitID.PRAYER_AUGURY_UNLOCKED))
		{
			dirty = true;
		}
	}
	@Subscribe public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.BANKMAIN) { dirty = true; bankDirty = true; bankIdentity = identity(); }
	}
	@Subscribe public void onItemContainerChanged(ItemContainerChanged event)
	{
		int id = event.getContainerId();
		if (id == InventoryID.INV || id == InventoryID.WORN || id == InventoryID.BANK) { dirty = true; }
		if (id == InventoryID.BANK) { bankDirty = true; bankIdentity = identity(); }
	}

	private void invalidate(String reason)
	{
		session.clear(); // Immediate token invalidation even if a profile event arrives off-thread.
		render(reason, null, List.of());
		long token = session.generation();
		clientThread.invokeLater(() ->
		{
			if (!running || session.generation() != token) { return; }
			dirty = true;
			bankDirty = false;
			bankIdentity = null;
			loginTicks = 0;
		});
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!running || advisor == null || client.getGameState() != GameState.LOGGED_IN) { return; }
		// Wait for complete server ticks after login rather than interpreting default varbits as false.
		if (++loginTicks < 2) { return; }
		String identity = identity();
		if (identity == null) { clearUnavailable("Character identity unknown; waiting for RuneLite profile."); return; }
		if (!supported()) { clearUnavailable("Outside coverage: standard Ironman on permanent members worlds only."); return; }
		long before = session.generation();
		session.select(identity);
		if (before != session.generation())
		{
			dirty = true;
			bankDirty = bankDirty && identity.equals(bankIdentity);
		}
		if (!dirty) { return; }
		long token = session.generation();
		AccountSnapshot previous = session.snapshot();
		Observation<Set<Integer>> bank = previous == null ? Observation.unknown() : previous.bank;
		Widget bankWidget = client.getWidget(InterfaceID.Bankmain.ITEMS);
		if (bankDirty && bankWidget != null && !bankWidget.isHidden())
		{
			bank = AccountSnapshotReader.container(client.getItemContainer(InventoryID.BANK), Instant.now(), "bank observed this session");
			bankDirty = false;
		}
		AccountSnapshot snapshot = reader.read(bank);
		if (!session.publish(token, identity(), snapshot)) { return; }
		dirty = false;
		render("Observed locally. Open a suggestion to inspect its evidence.", snapshot, advisor.evaluate(snapshot));
	}

	private void clearUnavailable(String message)
	{
		if (session.snapshot() != null) { session.clear(); }
		bankDirty = false;
		bankIdentity = null;
		dirty = true;
		render(message, null, List.of());
	}

	private String identity()
	{
		String profile = configManager.getRSProfileKey();
		long character = client.getAccountHash();
		return profile == null || character == 0 || character == -1 ? null : profile + ":" + character;
	}

	private boolean supported()
	{
		return client.getVarbitValue(VarbitID.IRONMAN) == 1
			&& client.getWorldType().contains(WorldType.MEMBERS)
			&& EnumSet.of(WorldType.MEMBERS, WorldType.SKILL_TOTAL).containsAll(client.getWorldType());
	}

	private void render(String status, AccountSnapshot snapshot, List<Advisor.Recommendation> recommendations)
	{
		long token = session.generation();
		long revision = session.revision();
		SwingUtilities.invokeLater(() ->
		{
			if (!running || panel == null || !session.currentView(token, revision) || session.snapshot() != snapshot) { return; }
			panel.render(status, snapshot, recommendations, catalog);
		});
	}

	private static BufferedImage icon()
	{
		BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = icon.createGraphics();
		graphics.setColor(new Color(192, 160, 90));
		graphics.drawOval(1, 1, 13, 13);
		graphics.drawLine(4, 12, 11, 4);
		graphics.drawLine(7, 4, 11, 4);
		graphics.drawLine(11, 4, 11, 8);
		graphics.dispose();
		return icon;
	}
}
