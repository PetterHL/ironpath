package com.ironpath;

import com.google.gson.Gson;
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

@PluginDescriptor(name = "Ironpath", internalName = "ironpath-advisor", description = "Local Nex session evidence for standard Ironmen", tags = {"ironman", "nex", "encounter"})
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
	private NexPanel panel;
	java.util.function.Function<ItemManager,NexPanel> panelFactory=NexPanel::new;
	final com.ironpath.nex.NexSession nexSession=new com.ironpath.nex.NexSession();
	NexObserver nex;
	private long lastNexRevision;
	private boolean lastCaptureActive;
	private volatile NexPanel.TrackingStatus lastTrackingStatus;
	private volatile NexSupportReport.Runtime supportRuntime=NexSupportReport.Runtime.UNKNOWN;
	private volatile com.ironpath.nex.NexCapture.Progress latestTrackingProgress;
	volatile com.ironpath.advisor.NexOwnedResult ownedResult;
	private volatile long ownedTicket;
	private final java.util.Map<Long,com.ironpath.nex.NexRecord> pendingOwned=new java.util.concurrent.ConcurrentHashMap<>();
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
		clearOwned();
		lastTrackingStatus=null; latestTrackingProgress=null;
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
			nexSession.clear(); nex=new NexObserver(client,catalog,nexSession); lastNexRevision=nexSession.revision(); lastCaptureActive=false;
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
			status = "Bundled catalogue unavailable or invalid. Advice is disabled.";
		}
		String initialStatus = status;
		SwingUtilities.invokeLater(() ->
		{
			if (!running || lifecycle != startup) { return; }
			panel = panelFactory.apply(itemManager);
			panel.render(initialStatus,null,Preferences.EMPTY,catalog,nexSession.stats(),true,null,"");
			if (!running || lifecycle != startup) { panel = null; return; }
			navigation = NavigationButton.builder().tooltip("Ironpath").icon(icon()).priority(6).panel(panel).build();
			toolbar.addNavigation(navigation);
		});
	}

	@Override
	protected void shutDown()
	{
		latestTrackingProgress=null;
		clearOwned();
		running = false;
		++lifecycle;
		nexSession.clear("Plugin disabled."); if(nex!=null) { nex.clearActive(); }
		session.clear();
		SwingUtilities.invokeLater(() ->
		{
			// UI ownership stays on the EDT, including cleanup of an in-flight startup.
			if (navigation != null) { toolbar.removeNavigation(navigation); }
			if (panel != null) { panel.render("Plugin disabled; observations cleared.",null,Preferences.EMPTY,catalog,nexSession.stats(),false,null,""); }
			navigation = null;
			panel = null;
		});
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state=event.getGameState();
		if (nex!=null && (state==GameState.LOADING || state==GameState.LOGGED_IN && nexSession.suspended()))
		{
			if (state==GameState.LOADING)
			{
				boolean fromArena=nex.bankTravelSource() || nexSession.suspended();
				int barrier=client.getVarbitValue(VarbitID.NEX_BARRIER);
				boolean arenaScene=(barrier==2 || barrier==3) && !nex.arenaExitObserved()
					&& (nex.inArena() || nexArenaScene(barrier));
				nex.transition();
				nexSession.suspend(identity(),client.getWorld(),client.getTickCount(),fromArena,arenaScene);
			}
			clearLive(nexSession.suspended()?"Nex session paused. Checking this account after travel.":"Scene changed. Revalidating account observations.");
			return;
		}
		invalidate("Account transition: observations cleared. Reopen your bank after login.");
	}

	@Subscribe public void onProfileChanged(ProfileChanged event) { invalidate("Profile changed; observations cleared."); }
	@Subscribe public void onRuneScapeProfileChanged(RuneScapeProfileChanged event) { invalidate("Character profile changed; observations cleared."); }
	@Subscribe public void onStatChanged(StatChanged event)
	{
		if(nexReady()) { nex.sample(-1); }
		AccountSnapshot previous = session.snapshot();
		if (previous == null || !Integer.valueOf(client.getRealSkillLevel(event.getSkill())).equals(previous.level(event.getSkill().name())))
		{
			dirty = true;
		}
	}
	@Subscribe public void onVarbitChanged(VarbitChanged event)
	{
		if(nexReady())
		{
			nex.resourceEvent(event.getVarbitId(),event.getVarpId(),event.getValue());
			if(event.getVarbitId()==VarbitID.NEX_BARRIER) { nex.barrier(event.getValue()); }
			if(NexObserver.resourceChanged(event.getVarbitId(),event.getVarpId())) { nex.sample(-1); }
		}
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
		if(nexReady() && (id==InventoryID.WORN || id==InventoryID.INV)) { nex.sample(-1); }
		if (id == InventoryID.INV || id == InventoryID.WORN || id == InventoryID.BANK) { dirty = true; }
		if (id == InventoryID.BANK) { bankDirty = true; bankIdentity = identity(); }
	}

	private void invalidate(String reason)
	{
		nexSession.clear(reason); if(nex!=null) { nex.clearActive(); }
		clearLive(reason);
	}

	private void clearLive(String reason)
	{
		lastTrackingStatus=null; latestTrackingProgress=null;
		clearOwned();
		session.clear(); // Immediate token invalidation even if a profile event arrives off-thread.
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
		if (!running || catalog == null || client.getGameState() != GameState.LOGGED_IN) { return; }
		// Wait for complete server ticks after login rather than interpreting default varbits as false.
		if (++loginTicks < 2) { return; }
		String identity = identity();
		if (identity == null) { clearUnavailable("Character identity unknown; waiting for RuneLite profile."); return; }
		if (!supported()) { clearUnavailable("Outside coverage: standard Ironman on permanent members worlds only."); return; }
		Widget banking=client.getWidget(InterfaceID.Bankmain.ITEMS);
		boolean bankVisible=banking!=null && !banking.isHidden();
		int barrier=client.getVarbitValue(VarbitID.NEX_BARRIER);
		boolean arena=nexArenaScene(barrier), arenaExit=barrier==0 || barrier==1;
		if (nexSession.suspended())
		{
			nexSession.revalidate(identity,client.getWorld(),client.getTickCount(),bankVisible,arena,arenaExit);
		}
		if (!nexSession.suspended() && nexSession.select(identity,client.getWorld())) { nex.clearActive(); }
		// A first bank visit must count too, rather than only a bank reached from suspended history.
		if(!nexSession.suspended()) { nexSession.revalidate(identity,client.getWorld(),client.getTickCount(),bankVisible,arena,arenaExit); }
		long before = session.generation();
		session.select(identity);
		if (before != session.generation())
		{
			dirty = true;
			if(nex!=null) { nex.clearActive(); }
			bankDirty = bankDirty && identity.equals(bankIdentity);
			loadPreferences(session.generation(), identity);
		}
		// Revalidation can precede a slow settings read. Keep its verified departure source in either case.
		if(arena && !nexSession.suspended()) { nex.confirmArenaSource(); }
		if (nexReady()) { nex.method(session.preferences().nex); nex.defaults(session.preferences().nexContext); nex.tick(); }
		supportRuntime=readSupportRuntime();
		boolean activeChanged=lastCaptureActive!=nex.capture.active();
		boolean statusChanged=lastTrackingStatus!=trackingStatus(session.snapshot());
		boolean refresh=lastNexRevision!=nexSession.revision() || activeChanged || statusChanged;
		if (refresh)
		{
			lastNexRevision=nexSession.revision();
			lastCaptureActive=nex.capture.active();
		}
		if (!dirty)
		{
			if(refresh && (!lastCaptureActive || activeChanged || statusChanged)) { render("Nex session updated locally.",session.snapshot()); }
			publishTrackingProgress(); return;
		}
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
		// Publish the full status after the coalesced account read. Publishing before it
		// invalidated the queued ACTIVE view, leaving the prior loading label on screen.
		if(nex==null || !nex.capture.active() || activeChanged || statusChanged) { render("Observed locally.", snapshot); }
		publishTrackingProgress();
	}
	private void publishTrackingProgress()
	{
		var progress=nex==null?null:nex.capture.progress(); latestTrackingProgress=progress;
		if(progress==null) { return; }
		long token=session.generation(), revision=nexSession.revision(); String expectedIdentity=session.identity();
		SwingUtilities.invokeLater(()->{
			if(!running || panel==null || latestTrackingProgress!=progress || !session.accepts(token,expectedIdentity)
				|| nexSession.revision()!=revision || nexSession.suspended()) { return; }
			panel.updateTrackingProgress(progress);
		});
	}

	private void clearUnavailable(String message)
	{
		clearOwned();
		nexSession.clear(message); if(nex!=null) { nex.clearActive(); }
		if (session.identity() != null) { session.clear(); }
		bankDirty = false;
		bankIdentity = null;
		dirty = true;
		render(message, null);
	}

	private boolean nexArenaScene(int barrier)
	{
		var world=client.getTopLevelWorldView();
		return (barrier==2 || barrier==3) && world!=null && world.npcs()!=null
			&& world.npcs().stream().anyMatch(n->NexObserver.bossId(n.getId()));
	}

	private boolean nexReady()
	{ return nex!=null && running && session.snapshot()!=null && session.preferencesLoaded() && !nexSession.suspended() && current(session.generation(),session.identity()); }


	@Subscribe public void onNpcSpawned(net.runelite.api.events.NpcSpawned event)
 { if(nexReady()) { nex.spawn(event.getNpc()); } }
 @Subscribe public void onNpcDespawned(net.runelite.api.events.NpcDespawned event)
 { if(nexReady()) { nex.despawn(event.getNpc()); } }
 @Subscribe public void onNpcChanged(net.runelite.api.events.NpcChanged event)
 { if(nexReady()) { nex.changed(event.getNpc()); } }
 @Subscribe public void onActorDeath(net.runelite.api.events.ActorDeath event)
 { if(nexReady()) { nex.death(event.getActor()); } }
 @Subscribe public void onHitsplatApplied(net.runelite.api.events.HitsplatApplied event)
 { if(nexReady()) { nex.hit(event); } }
 @Subscribe public void onOverheadTextChanged(net.runelite.api.events.OverheadTextChanged event)
 { if(nexReady()) { nex.overhead(event.getActor(),event.getOverheadText()); } }
 @Subscribe public void onAnimationChanged(net.runelite.api.events.AnimationChanged event)
 { if(nexReady() && event.getActor()==client.getLocalPlayer()) { nex.animation(); } }
 @Subscribe public void onProjectileMoved(net.runelite.api.events.ProjectileMoved event)
 { if(nexReady()) { nex.projectile(event.getProjectile()); } }
 @Subscribe public void onGraphicChanged(net.runelite.api.events.GraphicChanged event)
 { if(nexReady()) { nex.spot(event.getActor()); } }
 @Subscribe public void onInteractingChanged(net.runelite.api.events.InteractingChanged event)
 { if(nexReady()) { nex.interactingChanged(event.getSource()); } }
 @Subscribe public void onScriptPostFired(net.runelite.api.events.ScriptPostFired event)
 { if(nexReady() && event.getScriptId()==net.runelite.api.ScriptID.HP_HUD_UPDATE) { nex.hud(); } }
 @Subscribe public void onChatMessage(net.runelite.api.events.ChatMessage event)
 { if(nexReady()) { nex.chat(event.getType(),event.getMessage()); } }

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

	// Retained independently of sidebar editors; package access supports lifecycle regression tests.
	void changePreferences(long token, long revision, long nexRevision, String expectedIdentity, UnaryOperator<Preferences> change)
	{
		clientThread.invokeLater(() ->
		{
			if (!current(token, expectedIdentity) || !session.currentView(token,revision) || nexSession.revision()!=nexRevision || !session.writable()) { return; }
			Preferences next = change.apply(session.preferences());
			if (!session.editPreferences(token, expectedIdentity, next)) { return; }
			if(nex!=null) { nex.method(next.nex); }
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
		if(nexSession.suspended()) { status="Nex session paused. Open a bank to revalidate this account, or return from a recognized bank trip."; }
		if(snapshot!=null && session.preferencesLoaded() && !nexSession.suspended())
		{ evaluateOwned(snapshot,session.preferences()); }
		long token=session.generation(), revision=session.revision(), nexRevision=nexSession.revision();
		String expectedIdentity=session.identity(); Preferences preferences=session.preferences();
		String storage=session.storageStatus(); var stats=nexSession.stats();
		supportRuntime=readSupportRuntime(); var runtime=supportRuntime;
		boolean enabled=nex!=null && nex.enabled(), active=nex!=null && nex.capture.active();
		var progress=active && client.isClientThread()?nex.capture.progress():null;
		NexPanel.TrackingStatus tracking=trackingStatus(snapshot);
		var owned=ownedResult;
		NexPanel.Actions actions=snapshot==null || !session.preferencesLoaded()?null:new NexPanel.Actions()
		{
			public boolean current() { return running && session.currentView(token,revision) && session.snapshot()==snapshot && nexSession.revision()==nexRevision; }
			public boolean contextCurrent() { return running && session.accepts(token,expectedIdentity) && session.preferencesLoaded() && nexSession.revision()==nexRevision; }
			public NexSupportReport.Frame supportFrame(NexSupportReport.Frame fallback)
			{ return contextCurrent()?new NexSupportReport.Frame(nexSession.stats(),supportRuntime):null; }
			public boolean canEdit() { return session.writable(); }
			public void executeScenario(Runnable task) { if(current()) { executor.execute(task); } }
			public void method(com.ironpath.preferences.NexMethod method)
			{ if(current()) { changePreferences(token,revision,nexRevision,expectedIdentity,p->p.nex(method)); } }
			public void confirm(String key, Boolean value)
			{ if(current() && catalog.factChoices().containsKey(key)) { changePreferences(token,revision,nexRevision,expectedIdentity,p->p.confirm(key,value,Instant.now())); } }
			public void context(long key,long annotationRevision,com.ironpath.nex.NexContext context,boolean remember)
			{
				if(!contextCurrent()) { return; }
				clientThread.invokeLater(()->{
					if(!IronpathPlugin.this.current(token,expectedIdentity) || !nexSession.accepts(expectedIdentity,client.getWorld())) { return; }
					if((key<0 || remember) && session.preferences()!=preferences) { return; }
					// Kill/revision, rather than the latest-kill pointer, binds a historical action.
					if(key>=0 && !nexSession.annotate(key,annotationRevision,context)) { return; }
					if(remember && session.writable()) { saveContext(token,expectedIdentity,context); }
					render(key<0?"Future context defaults updated.":"Kill context updated; observations preserved.",session.snapshot());
				});
			}
			public void reset() { clear(nex.enabled(),"Reset session selected."); }
			public void enable(boolean value) { clear(value,"Capture setting changed."); }
			private void clear(boolean enable,String reason)
			{
				if (!current()) { return; }
				clientThread.invokeLater(()->{
					if(!IronpathPlugin.this.current(token,expectedIdentity) || !session.currentView(token,revision) || nexSession.revision()!=nexRevision) { return; }
					nex.clearActive(); nexSession.clear(reason); nexSession.select(expectedIdentity,client.getWorld()); nex.enable(enable);
					clearOwned();
					render("Nex session cleared from memory.",session.snapshot());
				});
			}
		};
		String message=status;
		SwingUtilities.invokeLater(()->{
			if(!running || panel==null || !session.currentView(token,revision) || session.snapshot()!=snapshot || nexSession.revision()!=nexRevision
				|| (nex!=null && nex.capture.active())!=active) { return; }
			panel.render(message,snapshot,preferences,catalog,stats,enabled,actions,storage,owned,active,tracking,progress,runtime);
			lastTrackingStatus=tracking;
		});
	}
	/** Client-thread readings only. EDT report copying consumes the immutable cached scalars. */
	private NexSupportReport.Runtime readSupportRuntime()
	{
		if(!client.isClientThread()) { return NexSupportReport.Runtime.UNKNOWN; }
		long scope=nexSession.scope(); var gameState=client.getGameState();
		if(gameState!=GameState.LOGGED_IN)
		{ return new NexSupportReport.Runtime(scope,gameState,null,com.ironpath.model.Truth.UNKNOWN,null,com.ironpath.model.Truth.UNKNOWN,com.ironpath.model.Truth.UNKNOWN,nex!=null && nex.capture.active(),nexSession.awaitingArena()); }
		var bank=client.getWidget(InterfaceID.Bankmain.ITEMS); int barrier=client.getVarbitValue(VarbitID.NEX_BARRIER);
		return new NexSupportReport.Runtime(scope,gameState,client.getTickCount(),bank!=null && !bank.isHidden()?com.ironpath.model.Truth.TRUE:com.ironpath.model.Truth.FALSE,
			barrier,nexArenaScene(barrier)?com.ironpath.model.Truth.TRUE:com.ironpath.model.Truth.FALSE,identity()!=null?com.ironpath.model.Truth.TRUE:com.ironpath.model.Truth.FALSE,
			nex!=null && nex.capture.active(),nexSession.awaitingArena());
	}
	private NexPanel.TrackingStatus trackingStatus(AccountSnapshot snapshot)
	{
		// ProfileChanged can arrive on the profile executor. Clearing must not read live client state.
		if(!client.isClientThread()) { return NexPanel.TrackingStatus.ACCOUNT; }
		if(client.getGameState()==GameState.LOADING) { return NexPanel.TrackingStatus.LOADING; }
		if(client.getGameState()!=GameState.LOGGED_IN) { return NexPanel.TrackingStatus.LOGIN; }
		if(identity()==null) { return NexPanel.TrackingStatus.ACCOUNT; }
		if(client.getVarbitValue(VarbitID.IRONMAN)!=1) { return NexPanel.TrackingStatus.UNSUPPORTED_ACCOUNT; }
		if(!supported()) { return NexPanel.TrackingStatus.UNSUPPORTED_WORLD; }
		if(nex!=null && !nex.enabled()) { return NexPanel.TrackingStatus.OFF; }
		if(nexSession.suspended()) { return nexSession.awaitingArena()?NexPanel.TrackingStatus.LOADING:NexPanel.TrackingStatus.BANK; }
		if(snapshot==null) { return NexPanel.TrackingStatus.ACCOUNT; }
		if(!session.preferencesLoaded()) { return NexPanel.TrackingStatus.SETTINGS_LOADING; }
		if(nex!=null && nex.capture.active()) { return NexPanel.TrackingStatus.ACTIVE; }
		if(!session.writable()) { return NexPanel.TrackingStatus.SETTINGS_UNAVAILABLE; }
		return nex!=null && nex.inArena()?NexPanel.TrackingStatus.READY:NexPanel.TrackingStatus.OUTSIDE;
	}
	private void clearOwned()
	{
		++ownedTicket; ownedResult=null; pendingOwned.clear();
	}
	private void saveContext(long token,String identity,com.ironpath.nex.NexContext context)
	{
		Preferences next=session.preferences().context(context);
		if(!session.editPreferences(token,identity,next)) { return; }
		if(nex!=null) { nex.defaults(next.nexContext); }
		preferenceIO.save(LocalPreferences.key(identity),next).whenComplete((ignored,error)->clientThread.invokeLater(()->{
			if(!current(token,identity)) { return; }
			session.saved(token,identity,next,error==null);
			render("Future context defaults updated.",session.snapshot());
		}));
	}
	private void evaluateOwned(AccountSnapshot account,Preferences preferences)
	{
		var stats=nexSession.stats();
		pendingOwned.keySet().removeIf(key->!stats.records.containsKey(key));
		for(var summary:nexSession.recent())
		{
			// Incomplete segments still need worker-built action diagnostics for the controlled check.
			// NexOwnedEvaluator retains its COMPLETE-only numerical-comparison gate.
			if(summary.outcome!=com.ironpath.encounter.EncounterSummary.Outcome.COMPLETE && summary.actions.signals.isEmpty()) { continue; }
			var initial=nexSession.record(summary.key);
			if(initial==null) { continue; }
			if(initial.inputs==null) { initial=nexSession.freezeInputs(summary.key,new com.ironpath.advisor.NexEvaluation(account,preferences,catalog)); }
			final var record=initial;
			if(record==null) { continue; }
			if(record.calculation!=null || pendingOwned.get(summary.key)==record) { continue; }
			pendingOwned.put(summary.key,record);
			long epoch=ownedTicket, token=session.generation(); String identity=session.identity();
			executor.execute(()->{
				var result=record.inputs.evaluate(record,()->running && epoch==ownedTicket && nexSession.record(summary.key)==record);
				clientThread.invokeLater(()->{
					if(epoch!=ownedTicket || !current(token,identity) || !nexSession.accepts(identity,client.getWorld())) { return; }
					if(pendingOwned.get(summary.key)==record) { pendingOwned.remove(summary.key); }
					if(!nexSession.publish(record,result)) { return; }
					render("Owned equipment comparison updated locally.",session.snapshot());
				});
			});
		}
		var last=stats.last==null?null:nexSession.record(stats.last.key);
		ownedResult=last==null?null:last.calculation;
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
