package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.Advisor;
import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.preferences.Preferences;
import com.ironpath.preferences.PreferenceStorage;
import com.ironpath.preferences.PreferenceIO;
import com.ironpath.preferences.LocalPreferences;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.UnaryOperator;
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
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@PluginDescriptor(name = "Ironpath", internalName = "ironpath-advisor", description = "Local PvM progression advisor for standard Ironmen", tags = {"ironman", "cox", "progression"})
public class IronpathPlugin extends Plugin
{
	@Inject Client client;
	@Inject ClientThread clientThread;
	@Inject ConfigManager configManager;
	@Inject ClientToolbar toolbar;
	@Inject ItemManager itemManager;
	@Inject AccountSnapshotReader reader;
	@Inject Gson gson;
	@Inject ScheduledExecutorService executor;
	PreferenceStorage preferenceStorage;
	private PreferenceIO preferenceIO;

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
	ZilyanaObserver recentBoss;
	volatile com.ironpath.encounter.KillImpactResult recentImpact;
	private volatile long recentTicket;
	private long lastRecentKey, lastRecentObservationKey;

	@Override
	protected void startUp()
	{
		running = true;
		recentBoss=new ZilyanaObserver(client);
		recentImpact=null; lastRecentKey=lastRecentObservationKey=0; ++recentTicket;
		long startup = ++lifecycle;
		session.clear();
		clearRecentBoss();
		dirty = true;
		bankDirty = false;
		bankIdentity = null;
		loginTicks = 0;
		String status;
		try
		{
			catalog = KnowledgeCatalog.load(gson);
			advisor = new Advisor(catalog);
			if (preferenceIO == null)
			{
				if (preferenceStorage == null) { preferenceStorage = new LocalPreferences(this::getPluginDirectory, gson, catalog); }
				preferenceIO = new PreferenceIO(preferenceStorage, executor);
			}
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
			panel = new IronpathPanel(itemManager);
			panel.render(initialStatus, null, List.of(), catalog, null, Preferences.EMPTY, null, "", List.of());
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
		clearRecentBoss();
		SwingUtilities.invokeLater(() ->
		{
			// UI ownership stays on the EDT, including cleanup of an in-flight startup.
			if (navigation != null) { toolbar.removeNavigation(navigation); }
			if (panel != null) { panel.render("Plugin disabled; observations cleared.", null, List.of(), null, null, Preferences.EMPTY, null, "", List.of()); }
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
		if(recentReady()) { recentBoss.sample(); }
		AccountSnapshot previous = session.snapshot();
		if (previous == null || !Integer.valueOf(client.getRealSkillLevel(event.getSkill())).equals(previous.level(event.getSkill().name())))
		{
			dirty = true;
		}
	}
	@Subscribe public void onVarbitChanged(VarbitChanged event)
	{
		if(recentReady()) { recentBoss.sample(); }
		AccountSnapshot previous = session.snapshot();
		// Only selected quest progress invalidates quest facts. Script evaluation stays in the coalesced read.
		boolean questChanged = Set.of(VarbitID.SOTE, VarbitID.BCS, VarbitID.PMOON_QUEST, VarbitID.MM2_PROGRESS, VarbitID.VIKINGEXILE, VarbitID.WGS).contains(event.getVarbitId())
			|| Set.of(VarPlayerID.SOTE_PRIMARY, VarPlayerID.BCS_PRIMARY, VarPlayerID.PMOON_MAIN,
				VarPlayerID.REGICIDE_QUEST, VarPlayerID.NEX_PERM, VarPlayerID.VMQ4_PRIMARY,
				VarPlayerID.MM2_QUEST, VarPlayerID.DT2_PRIMARY, VarPlayerID.DRAGONSLAYER2_MAIN, VarPlayerID.MYQ6_PRIMARY, VarPlayerID.FRIS_R1, VarPlayerID.FRIS_R2, VarPlayerID.WGS_PRIMARY).contains(event.getVarpId());
		if (previous == null || !supported() || questChanged || !previous.rigour.known() || !previous.augury.known()
			|| (previous.rigour.value() ? 1 : 0) != client.getVarbitValue(VarbitID.PRAYER_RIGOUR_UNLOCKED)
			|| (previous.augury.value() ? 1 : 0) != client.getVarbitValue(VarbitID.PRAYER_AUGURY_UNLOCKED)
			|| !previous.fact("deadeye").known() || !previous.fact("mystic-vigour").known()
			|| (previous.fact("deadeye").value() ? 1 : 0) != client.getVarbitValue(VarbitID.PRAYER_DEADEYE_UNLOCKED)
			|| (previous.fact("mystic-vigour").value() ? 1 : 0) != client.getVarbitValue(VarbitID.PRAYER_MYSTIC_VIGOUR_UNLOCKED))
		{ dirty = true; }
	}
	@Subscribe public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.BANKMAIN) { dirty = true; bankDirty = true; bankIdentity = identity(); }
	}
	@Subscribe public void onItemContainerChanged(ItemContainerChanged event)
	{
		int id = event.getContainerId();
		if(recentReady()) {
			if(id==InventoryID.WORN) { recentBoss.sample(); }
			if(id==InventoryID.INV) { recentBoss.inventoryChanged(); }
		}
		if (id == InventoryID.INV || id == InventoryID.WORN || id == InventoryID.BANK) { dirty = true; }
		if (id == InventoryID.BANK) { bankDirty = true; bankIdentity = identity(); }
	}

	private void invalidate(String reason)
	{
		session.clear(); // Immediate token invalidation even if a profile event arrives off-thread.
		clearRecentBoss();
		render(reason, null);
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
			clearRecentBoss();
			bankDirty = bankDirty && identity.equals(bankIdentity);
			loadPreferences(session.generation(), identity);
		}
		recentBoss.tick();
		publishRecentBoss();
		if (!dirty) { return; }
		long token = session.generation();
		AccountSnapshot previous = session.snapshot();
		Observation<Set<Integer>> bank = previous == null ? Observation.unknown() : previous.bank;
		Observation<java.util.Map<Integer, Long>> bankQuantities = previous == null ? Observation.unknown() : previous.bankQuantities;
		Widget bankWidget = client.getWidget(InterfaceID.Bankmain.ITEMS);
		if (bankDirty && bankWidget != null && !bankWidget.isHidden())
		{
			bank = AccountSnapshotReader.container(client.getItemContainer(InventoryID.BANK), Instant.now(), "bank observed this session");
			bankQuantities = AccountSnapshotReader.quantities(client.getItemContainer(InventoryID.BANK), bank.time(), "bank quantities observed this session");
			bankDirty = false;
		}
		AccountSnapshot snapshot = reader.read(bank, bankQuantities);
		if (!session.publish(token, identity(), snapshot)) { return; }
		dirty = false;
		render("Observed locally. Open a suggestion to inspect its evidence.", snapshot);
	}

	private void clearUnavailable(String message)
	{
		if (session.identity() != null) { session.clear(); clearRecentBoss(); }
		bankDirty = false;
		bankIdentity = null;
		dirty = true;
		render(message, null);
	}

	private boolean recentReady()
	{ return recentBoss!=null && running && session.snapshot()!=null && current(session.generation(),session.identity()); }
	private void clearRecentBoss()
	{
		++recentTicket; recentImpact=null; lastRecentKey=lastRecentObservationKey=0;
		if(recentBoss!=null) { recentBoss.reset(session.generation()); }
	}
	@Subscribe public void onNpcSpawned(net.runelite.api.events.NpcSpawned event)
	{ if(recentReady()) { recentBoss.spawn(event.getNpc()); } }
	@Subscribe public void onNpcDespawned(net.runelite.api.events.NpcDespawned event)
	{ if(recentReady()) { recentBoss.despawn(event.getNpc()); } }
	@Subscribe public void onNpcChanged(net.runelite.api.events.NpcChanged event)
	{ if(recentReady()) { recentBoss.changed(event.getNpc()); } }
	@Subscribe public void onActorDeath(net.runelite.api.events.ActorDeath event)
	{ if(recentReady()) { recentBoss.death(event.getActor()); } }
	@Subscribe public void onHitsplatApplied(net.runelite.api.events.HitsplatApplied event)
	{ if(recentReady()) { recentBoss.hit(event); } }
	@Subscribe public void onServerNpcLoot(net.runelite.client.events.ServerNpcLoot event)
	{ if(recentReady()) { recentBoss.loot(event); } }
	private void publishRecentBoss()
	{
		if(!session.preferencesLoaded() || session.snapshot()==null) { return; }
		var summaries=recentBoss.capture.recent();
		if(!summaries.isEmpty() && summaries.get(summaries.size()-1).key!=lastRecentObservationKey)
		{
			lastRecentObservationKey=summaries.get(summaries.size()-1).key;
			if(summaries.get(summaries.size()-1).outcome!=com.ironpath.encounter.EncounterSummary.Outcome.COMPLETE)
			{ render("Recent encounter evidence updated locally.",session.snapshot()); }
		}
		var summary=summaries.stream().filter(s->s.outcome==com.ironpath.encounter.EncounterSummary.Outcome.COMPLETE)
			.reduce((first,last)->last).orElse(null);
		if(summary==null || summary.key==lastRecentKey) { return; }
		lastRecentKey=summary.key;
		long ticket=++recentTicket, token=session.generation();
		String expectedIdentity=session.identity();
		AccountSnapshot captured=session.snapshot(); Preferences choices=session.preferences();
		KnowledgeCatalog knowledge=catalog;
		executor.execute(()->{
			var result=new com.ironpath.advisor.ZilyanaWornStateComparison(knowledge).evaluate(summary,captured,choices);
			clientThread.invokeLater(()->{
				if(ticket!=recentTicket || !current(token,expectedIdentity)) { return; }
				recentImpact=result;
				render("Recent boss comparison updated locally.",session.snapshot());
			});
		});
	}

	private String identity()
	{
		String profile = configManager.getRSProfileKey();
		long character = client.getAccountHash();
		net.runelite.client.config.ConfigProfile configProfile = configManager.getProfile();
		return profile == null || configProfile == null || character == 0 || character == -1 ? null
			: configProfile.getId() + ":" + profile + ":" + character;
	}

	private boolean supported()
	{
		return client.getVarbitValue(VarbitID.IRONMAN) == 1
			&& client.getWorldType().contains(WorldType.MEMBERS)
			&& EnumSet.of(WorldType.MEMBERS, WorldType.SKILL_TOTAL).containsAll(client.getWorldType());
	}

	private boolean current(long token, String expectedIdentity)
	{
		return running && client.getGameState() == GameState.LOGGED_IN && supported()
			&& session.accepts(token, expectedIdentity) && expectedIdentity.equals(identity());
	}

	private void loadPreferences(long token, String expectedIdentity)
	{
		preferenceIO.load(LocalPreferences.key(expectedIdentity)).whenComplete((preferences, error) -> clientThread.invokeLater(() ->
		{
			if (!current(token, expectedIdentity)) { return; }
			if (session.loadPreferences(token, expectedIdentity, error == null ? preferences : Preferences.EMPTY, error == null))
			{ render("Observed locally. Preferences apply only to this character and profile.", session.snapshot()); }
		}));
	}

	private void changePreferences(long token, String expectedIdentity, UnaryOperator<Preferences> change)
	{
		clientThread.invokeLater(() ->
		{
			if (!current(token, expectedIdentity) || !session.writable()) { return; }
			Preferences next = change.apply(session.preferences());
			if (!session.editPreferences(token, expectedIdentity, next)) { return; }
			render("Your planning choices were updated.", session.snapshot());
			preferenceIO.save(LocalPreferences.key(expectedIdentity), next).whenComplete((ignored, error) -> clientThread.invokeLater(() ->
			{
				if (!current(token, expectedIdentity)) { return; }
				session.saved(token, expectedIdentity, next, error == null);
				render("Observed locally. Open a suggestion to inspect its evidence.", session.snapshot());
			}));
		});
	}

	private void render(String status, AccountSnapshot snapshot)
	{
		long token = session.generation();
		long revision = session.revision();
		String expectedIdentity = session.identity();
		Preferences preferences = session.preferences();
		boolean loaded = session.preferencesLoaded();
		String storageStatus = session.storageStatus();
		List<Advisor.Recommendation> recommendations = snapshot == null || !loaded ? List.of() : advisor.evaluate(snapshot, preferences);
		com.ironpath.advisor.PoweredMagic.Setup reference = catalog == null ? null : session.reference(catalog);
		List<Advisor.UpgradeView> upgrades = snapshot == null || !loaded ? List.of() : advisor.upgrades(snapshot, preferences, reference, session.rangedReference(catalog), session.meleeReference(catalog), session.pace());
		com.ironpath.advisor.PursuitOverview overview = new com.ironpath.advisor.PursuitOverview(upgrades,
			snapshot == null || !loaded ? List.of() : advisor.activityOpportunities(snapshot, preferences, upgrades),
			snapshot == null || !loaded ? List.of() : advisor.quickActions(snapshot, preferences),
			snapshot == null || !loaded ? List.of() : advisor.availableActivities(snapshot, preferences),
			snapshot == null || !loaded ? List.of() : advisor.ownedUpgrades(snapshot, preferences));
		Advisor.GoalView goal = snapshot == null || !loaded ? null : advisor.goal(snapshot, preferences);
		List<String> reasons = snapshot == null || !loaded || !recommendations.isEmpty() ? List.of()
			: advisor.explainNoAdvice(snapshot, preferences);
		IronpathPanel.Actions actions = snapshot == null || !loaded ? null : new IronpathPanel.Actions()
		{
			public void goal(String id)
			{
				if (id == null || Advisor.goalChoices(catalog).containsKey(id)) { changePreferences(token, expectedIdentity, p -> p.goal(id)); }
			}
			public void skip(String id, boolean skipped)
			{
				if (catalog.activities.containsKey(id) || catalog.decisions.stream().anyMatch(d -> d.id.equals(id))) { changePreferences(token, expectedIdentity, p -> p.skip(id, skipped)); }
			}
			public void confirm(String key, Boolean value)
			{
				if (catalog.factChoices().containsKey(key)) { changePreferences(token, expectedIdentity, p -> p.confirm(key, value, Instant.now())); }
			}
			public void clear() { changePreferences(token, expectedIdentity, p -> Preferences.EMPTY); }
			public void recentBoss(boolean enabled)
			{
				clientThread.invokeLater(()->{
					if(!IronpathPlugin.this.current(token,expectedIdentity)) { return; }
					IronpathPlugin.this.clearRecentBoss(); recentBoss.enable(enabled,session.generation());
					render(enabled ? "Experimental recent boss enabled for this session." : "Recent boss disabled and cleared.",session.snapshot());
				});
			}
			public void clearRecentBoss()
			{
				clientThread.invokeLater(()->{
					if(!IronpathPlugin.this.current(token,expectedIdentity)) { return; }
					IronpathPlugin.this.clearRecentBoss();
					render("Recent kills cleared from memory.",session.snapshot());
				});
			}
			public boolean current() { return running && session.currentView(token, revision) && session.snapshot() == snapshot; }
			public boolean canEdit() { return session.writable(); }
			public void reference(String key, String value)
			{
				clientThread.invokeLater(() -> {
					if (!running || !session.accepts(token, identity()) || !expectedIdentity.equals(identity())
						|| !session.editReference(token, revision, expectedIdentity, snapshot, key, value, catalog)) { return; }
					render("Reference updated in memory for this character/profile.", snapshot);
				});
			}
			public void pace(String value) { pace("demonic-gorillas","ordinary",value); }
			public void pace(String activity, String mode, String value) { pace(activity,mode,null,value); }
			public void pace(String activity, String mode, String methodKey, String value)
			{
				clientThread.invokeLater(() -> {
					if (!IronpathPlugin.this.current(token, expectedIdentity) || !session.currentView(token, revision) || session.snapshot() != snapshot) { return; }
					com.ironpath.advisor.PaceScenario next = null;
					try
					{
						if (!value.isBlank())
						{
							String[] bounds=value.trim().split("-", -1);
							if (bounds.length > 2) { throw new IllegalArgumentException(); }
							double minimum=Double.parseDouble(bounds[0]), maximum=bounds.length == 1 ? minimum : Double.parseDouble(bounds[1]);
							next=new com.ironpath.advisor.PaceScenario(activity, mode, minimum, maximum,
								"Eligible completed trials per active hour, including travel/banking/failures in the scenario", Instant.now(),methodKey);
						}
					}
					catch (IllegalArgumentException invalid)
					{ render("Pace needs a positive completed-trials/hour value or range, for example 40 or 30-50; current scenario retained.", snapshot); return; }
					if (session.editPace(token, revision, expectedIdentity, snapshot, next))
					{ render("Pace scenario updated in memory; it is not measured performance.", snapshot); }
				});
			}
		};
		var recentResult=recentImpact;
		boolean recentIsEnabled=recentBoss!=null && recentBoss.enabled();
		var recentEvidence=recentBoss==null ? List.<com.ironpath.encounter.EncounterSummary>of() : recentBoss.capture.recent();
		SwingUtilities.invokeLater(() ->
		{
			if (!running || panel == null || !session.currentView(token, revision) || session.snapshot() != snapshot) { return; }
			panel.recentBoss(recentResult,recentIsEnabled,recentEvidence);
			panel.render(status, snapshot, recommendations, catalog, goal, preferences, actions, storageStatus, reasons, overview, reference);
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
