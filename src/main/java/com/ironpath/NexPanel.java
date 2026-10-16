package com.ironpath;

import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.advisor.NexOwnedResult;
import com.ironpath.advisor.ComparisonResult;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.nex.NexSession;
import com.ironpath.nex.NexSummary;
import com.ironpath.preferences.NexMethod;
import com.ironpath.preferences.Preferences;
import java.awt.Dimension;
import java.util.*;
import javax.swing.*;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/** Encounter-first native view. Frozen details do not change when another kill arrives. */
final class NexPanel extends PluginPanel
{
	interface Actions
	{
		boolean current();
		boolean canEdit();
		void method(NexMethod method);
		void confirm(String key, Boolean value);
		void reset();
		void enable(boolean enabled);
	}
	private final ItemManager items;
	private String page="encounter";
	private NexSummary inspected;
	private NexOwnedResult inspectedOwned;
	private int ownedIndex;
	private int ownedPairIndex;
	private String factKey="rigour", roleKey="melee", slotKey="weapon";
	private Integer lockItem;
	private Map<Integer,String> displayNames=Map.of();
	private long view;
	private java.awt.Point returnPosition;
	NexPanel(ItemManager items) { this.items=items; }

	void render(String status, AccountSnapshot account, Preferences prefs, KnowledgeCatalog catalog,
		NexSession.Stats stats, boolean enabled, Actions actions, String storage)
	{ render(status,account,prefs,catalog,stats,enabled,actions,storage,null); }
	void render(String status, AccountSnapshot account, Preferences prefs, KnowledgeCatalog catalog,
		NexSession.Stats stats, boolean enabled, Actions actions, String storage, NexOwnedResult owned)
	{ render(status,account,prefs,catalog,stats,enabled,actions,storage,owned,false); }
	void render(String status, AccountSnapshot account, Preferences prefs, KnowledgeCatalog catalog,
		NexSession.Stats stats, boolean enabled, Actions actions, String storage, NexOwnedResult owned,boolean active)
	{
		if (!SwingUtilities.isEventDispatchThread()) { throw new IllegalStateException("Swing thread required"); }
		long currentView=++view;
		Map<Integer,String> names=new HashMap<>(catalog==null?Map.of():catalog.itemNames());
		if(account!=null && stats.last!=null) { stats.last.timeline.forEach(s->names.putAll(s.state.itemNames)); stats.last.beforeStart.forEach(s->names.putAll(s.state.itemNames)); }
		if(account!=null && inspected!=null) { inspected.timeline.forEach(s->names.putAll(s.state.itemNames)); inspected.beforeStart.forEach(s->names.putAll(s.state.itemNames)); }
		displayNames=Map.copyOf(names);
		if (account==null) { page="encounter"; inspected=null; inspectedOwned=null; ownedIndex=0; returnPosition=null; factKey="rigour"; roleKey="melee"; slotKey="weapon"; lockItem=null; }
		removeAll(); setLayout(new BoxLayout(this,BoxLayout.Y_AXIS)); setBackground(ColorScheme.DARK_GRAY_COLOR);
		text("IronPath"); text("Nex · Trio MP2 · One kill per trip");
		if (account==null || actions==null) { text(status); revalidate(); repaint(); return; }
		Runnable redraw=()->render(status,account,prefs,catalog,stats,enabled,actions,storage,owned,active);
		java.util.function.BooleanSupplier valid=()->view==currentView && actions.current();
		JPanel nav=new ActionLinks(); nav.setFont(getFont());
		for (String destination:List.of("encounter","setup","account"))
		{
			button(nav,destination.equals("encounter")?"Encounter":destination.equals("setup")?"Setup":"Account",()->{
				if (!valid.getAsBoolean()) { return; } page=destination; inspected=null; inspectedOwned=null; redraw.run();
			});
		}
		add(nav);
		if (page.equals("account")) { account(account,prefs,catalog,actions,valid,storage); }
		else if (page.equals("setup")) { setup(prefs,stats.last,catalog,actions,valid,storage); }
		else if(inspectedOwned!=null)
		{
			button(this,"Back to encounter",()->{
				if(!valid.getAsBoolean()) { return; } inspectedOwned=null; redraw.run();
				var viewport=(JViewport)SwingUtilities.getAncestorOfClass(JViewport.class,this);
				if(viewport!=null && returnPosition!=null) { viewport.setViewPosition(returnPosition); }
			});
			ownedDetails(inspectedOwned,catalog,valid);
		}
		else if (inspected!=null)
		{
			button(this,"Back to encounter",()->{
				if (!valid.getAsBoolean()) { return; } inspected=null; redraw.run();
				var viewport=(JViewport)SwingUtilities.getAncestorOfClass(JViewport.class,this);
				if (viewport!=null && returnPosition!=null) { viewport.setViewPosition(returnPosition); }
			});
			details(inspected,catalog,valid);
		}
		else
		{
			if (status.startsWith("Nex session paused")) { text(status); }
			else if (!enabled) { text("Capture disabled. Session cleared."); }
			else if (active) { text("Observing the current Nex attempt. Results appear after completion."); }
			else if (stats.last==null) { text("Ready to observe the next full Nex kill. Equipment and spell use are detected automatically."); }
			if(stats.last!=null)
			{
				text(String.format(Locale.ROOT,"Last complete kill · %.1f s",stats.last.seconds()));
				text("Observed smoke announcement → Nex death/dying.");
				text(stats.last.share()==null?"Damage share unavailable":String.format(Locale.ROOT,"Observed Nex + minion hitsplat share · %.1f%%",stats.last.share()));
				text(String.format(Locale.ROOT,"Session mean · %.1f s · n = %d\nRange %.1f–%.1f s",stats.meanSeconds,stats.n,stats.minSeconds,stats.maxSeconds));
				if (stats.shareN>0)
				{ text(String.format(Locale.ROOT,"Mean per-kill share %.1f%% · n = %d\nRange %.1f–%.1f%%\nAggregate damage share %.1f%%",stats.meanShare,stats.shareN,stats.minShare,stats.maxShare,stats.aggregateShare)); }
				text("Matched method, worn packages, sampled levels/prayers and arena roster. Visible people do not establish party membership.");
				button(this,"Kill and calculation details",()->{
					if (!valid.getAsBoolean()) { return; }
					var viewport=(JViewport)SwingUtilities.getAncestorOfClass(JViewport.class,this);
					returnPosition=viewport==null?null:viewport.getViewPosition(); inspected=stats.last; redraw.run();
				}).setName("nex-details");
			}
			if(stats.lastAttempt!=null && stats.lastAttempt!=stats.last)
			{
				text("Last attempt · "+stats.lastAttempt.outcome+" · "+stats.lastAttempt.reason);
				button(this,"Last attempt details",()->{if(valid.getAsBoolean()) { inspected=stats.lastAttempt; redraw.run(); }});
			}
			if(stats.last!=null)
			{
				text("Improve with owned equipment");
				if(owned==null) { text("Comparing the captured setups…"); }
				else
				{
					long supported=owned.choices.stream().filter(c->c.comparison!=null && c.comparison.before!=null).count();
					text(supported+" / "+owned.choices.size()+" state and inventory scenarios supported. Each uses its own observed setup.");
					Set<String> roles=new HashSet<>();
					for(var c:owned.choices)
					{
						if(c.bankPreparation || c.comparison==null || !roles.add(c.actual.role)) { continue; }
						text(c.actual.role+" · tick "+c.actual.tick+" · "+c.headline()); item(c.proposed.slots.get("weapon"),catalog,valid);
					}
					button(this,"Owned setup comparisons",()->{
						if(!valid.getAsBoolean()) { return; }
						var viewport=(JViewport)SwingUtilities.getAncestorOfClass(JViewport.class,this);
						returnPosition=viewport==null?null:viewport.getViewPosition(); inspectedOwned=owned; ownedIndex=0; ownedPairIndex=0; redraw.run();
					}).setName("nex-owned");
				}
			}
			text(stats.complete+" / "+stats.attempts+" captured attempts eligible for complete statistics · "+stats.failed+" failed · "+stats.censored+" censored/excluded. Last "+NexSession.MAX_RECENT+" attempts maximum.");
			text("Completed summaries stay in memory across a recognized bank trip. Logout, account/profile change and Reset clear them.");
			button(this,enabled?"Disable capture":"Enable capture",()->{if(valid.getAsBoolean()) { actions.enable(!enabled); }});
			button(this,"Reset session",()->{if(valid.getAsBoolean()) { actions.reset(); }}).setName("nex-reset");
			text("Spec effects, new upgrades and encounter-time estimates remain under development.");
		}
		revalidate(); repaint();
	}
	private void ownedDetails(NexOwnedResult result,KnowledgeCatalog catalog,java.util.function.BooleanSupplier valid)
	{
		text("Owned equipment · kill #"+result.summary.key);
		text("Ordinary offence scenarios. Independent alternatives cannot be added together. Bank preparation requires packing and stock checks before the next trip.");
		JPanel resources=ownedBody(); resources.setVisible(false);
		button(this,"Special-attack resources",()->{
			if(valid.getAsBoolean()) { resources.setVisible(!resources.isVisible()); revalidate(); repaint(); }
		});
		NexResourcesPanel.populate(resources,result.resources,valid,()->{ revalidate(); repaint(); }); add(resources);
		if(result.choices.isEmpty()) { text("No complete captured setup to compare."); return; }
		JComboBox<String> states=new JComboBox<>(); states.setName("nex-owned-state");
		for(var c:result.choices) { states.addItem(c.actual.role+" · tick "+c.actual.tick+" · "+c.actual.phase+(c.bankPreparation?" · bank preparation":" · carried")); }
		states.setSelectedIndex(Math.min(ownedIndex,result.choices.size()-1)); addControl(states);
		JPanel detail=ownedBody(); add(detail);
		Runnable show=()->{
			if(!valid.getAsBoolean()) { return; }
			if(ownedIndex!=states.getSelectedIndex()) { ownedPairIndex=0; }
			ownedIndex=states.getSelectedIndex(); detail.removeAll();
			var c=result.choices.get(ownedIndex);
			PanelWidgets.text(detail,c.bankPreparation?"Bank preparation scenario · not carried in this kill":"Carried alternatives at the captured state");
			if(c.comparison==null) { PanelWidgets.text(detail,c.gap); }
			else
			{
				List<ComparisonResult> pairs=new ArrayList<>(); pairs.add(c.comparison); pairs.addAll(c.alternatives);
				JComboBox<String> options=new JComboBox<>(); options.setFont(getFont()); options.setName("nex-owned-pair");
				options.addItem("Proposed complete package");
				for(var pair:c.alternatives) { options.addItem(changes(pair,catalog)); }
				options.setAlignmentX(LEFT_ALIGNMENT); options.setSelectedIndex(Math.min(ownedPairIndex,pairs.size()-1));
				options.setMaximumSize(new Dimension(PANEL_WIDTH-12,Math.max(28,options.getPreferredSize().height))); detail.add(options);
				JPanel value=ownedBody(); detail.add(value);
				Runnable compare=()->{
					if(!valid.getAsBoolean()) { return; } ownedPairIndex=options.getSelectedIndex(); value.removeAll(); var pair=pairs.get(ownedPairIndex);
					PanelWidgets.text(value,NexOwnedResult.headline(pair));
					PanelWidgets.text(value,ownedPairIndex==0?(c.exhaustive?"Best supported package within this search":"Best package found in a partial search")+". Global best owned remains unestablished.":"Explicit owned alternative · compare this complete package independently.");
					PanelWidgets.text(value,pair.headline()); PanelWidgets.text(value,changes(pair,catalog));
					PanelWidgets.text(value,"Actual ammo slot: "+name(pair.beforeSetup.slots.get("ammo"),catalog));
					if(pair.beforeSetup.loadedAmmo>0 || pair.afterSetup.loadedAmmo>0)
					{ PanelWidgets.text(value,"Selected quiver projectile: "+(pair.beforeSetup.loadedAmmo>0?name(pair.beforeSetup.loadedAmmo,catalog):"worn "+name(pair.beforeSetup.slots.get("ammo"),catalog))
						+" → "+(pair.afterSetup.loadedAmmo>0?name(pair.afterSetup.loadedAmmo,catalog):"worn "+name(pair.afterSetup.slots.get("ammo"),catalog))+". Worn ammo slot listed separately."); }
					if(pair.beforeSetup.slots.containsValue(24780) || pair.afterSetup.slots.containsValue(24780)) { PanelWidgets.text(value,"Blood fury healing and survival are not estimated; offensive loss does not establish worse overall value."); }
					JPanel basis=ownedBody(); basis.setVisible(false);
					button(value,"Full setups and calculation basis",()->{
						if(valid.getAsBoolean()) { basis.setVisible(!basis.isVisible()); value.invalidate(); detail.invalidate(); invalidate(); revalidate(); repaint(); }
					});
					for(String slot:com.ironpath.advisor.PoweredMagic.SLOTS) { PanelWidgets.text(basis,slot+": "+name(pair.beforeSetup.slots.get(slot),catalog)+" → "+name(pair.afterSetup.slots.get(slot),catalog)); }
					for(String assumption:pair.assumptions) { PanelWidgets.text(basis,assumption); }
					value.add(basis);
					value.revalidate(); revalidate();
				}; options.addActionListener(e->compare.run()); compare.run();
			}
			c.coverage.forEach(g->PanelWidgets.text(detail,g)); detail.revalidate(); revalidate(); repaint();
		}; states.addActionListener(e->show.run()); show.run();
	}
	private JPanel ownedBody()
	{
		JPanel panel=new JPanel(); panel.setFont(getFont()); panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		panel.setAlignmentX(LEFT_ALIGNMENT); panel.setOpaque(false); return panel;
	}
	private String changes(ComparisonResult pair,KnowledgeCatalog catalog)
	{
		List<String> changes=new ArrayList<>();
		for(String slot:com.ironpath.advisor.PoweredMagic.SLOTS)
		{ if(!Objects.equals(pair.beforeSetup.slots.get(slot),pair.afterSetup.slots.get(slot))) { changes.add(slot+": "+name(pair.beforeSetup.slots.get(slot),catalog)+" → "+name(pair.afterSetup.slots.get(slot),catalog)); } }
		return changes.isEmpty()?"Same package · zero change":String.join("; ",changes);
	}
	private void setup(Preferences prefs, NexSummary last, KnowledgeCatalog catalog, Actions actions,
		java.util.function.BooleanSupplier valid, String storage)
	{
		text("Automatic observations are the baseline. Lightbearer throughout is the selected pilot choice; equipment still comes from the client.");
		text("Upgraded Death Charge: "+(prefs.nex.upgradedDeathCharge?"assumed for planning (pilot choice)":"not assumed")+". This does not establish a cast or proc.");
		JCheckBox upgraded=new JCheckBox("Assume upgrade",prefs.nex.upgradedDeathCharge); upgraded.setFont(getFont());
		upgraded.setToolTipText("Assume upgraded Death Charge"); upgraded.getAccessibleContext().setAccessibleName("Assume upgraded Death Charge"); add(upgraded);
		button(this,"Save method",()->{if(valid.getAsBoolean() && actions.canEdit()) { actions.method(new NexMethod(upgraded.isSelected(),prefs.nex.locks)); }});
		text("Planning locks / corrections apply to proposed packages. Observed history is preserved. Empty is an explicit choice.");
		JComboBox<String> role=new JComboBox<>(new String[]{"melee","ranged","spec"}); role.setSelectedItem(roleKey); addControl(role);
		role.addActionListener(e->roleKey=(String)role.getSelectedItem());
		JComboBox<String> slot=new JComboBox<>(com.ironpath.advisor.PoweredMagic.SLOTS.toArray(new String[0])); slot.setSelectedItem(slotKey); addControl(slot);
		slot.setName("nex-lock-slot");
		JComboBox<ItemChoice> item=new JComboBox<>(); item.setName("nex-lock-item"); addControl(item);
		Runnable choices=()->{
			Map<Integer,String> names=new HashMap<>(); names.put(0,"Empty");
			catalog.melee.items.stream().filter(i->slotKey.equals(i.slot)).forEach(i->names.put(i.id,i.label));
			catalog.ranged.items.stream().filter(i->slotKey.equals(i.slot)).forEach(i->names.put(i.id,i.label));
			catalog.comparison.items.stream().filter(i->slotKey.equals(i.slot)).forEach(i->names.put(i.id,i.label));
			if(last!=null) { last.timeline.forEach(s->{ int id=s.state.slots.getOrDefault(slotKey,-1); if(id>=0) { names.put(id,name(id,catalog)); } }); }
			item.removeAllItems(); names.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(e->item.addItem(new ItemChoice(e.getKey(),e.getValue())));
			for(int i=0;i<item.getItemCount();i++) { if(Objects.equals(item.getItemAt(i).id,lockItem)) { item.setSelectedIndex(i); } }
		}; choices.run();
		slot.addActionListener(e->{slotKey=(String)slot.getSelectedItem(); lockItem=null; choices.run();});
		JLabel error=new JLabel(); error.setFont(getFont()); add(error);
		button(this,"Save lock / correction",()->{
			if (!valid.getAsBoolean() || !actions.canEdit()) { return; }
			try { lockItem=((ItemChoice)item.getSelectedItem()).id; actions.method(prefs.nex.lock(role.getSelectedItem()+"/"+slot.getSelectedItem(),lockItem)); }
			catch (IllegalArgumentException ex) { error.setText("The pilot keeps Lightbearer equipped."); }
		});
		button(this,"Clear selected lock",()->{if(valid.getAsBoolean() && actions.canEdit()) { actions.method(prefs.nex.lock(role.getSelectedItem()+"/"+slot.getSelectedItem(),null)); }});
		prefs.nex.locks.forEach((key,id)->text(key+": "+name(id,catalog)+" · manual planning lock"));
		if (last==null) { text("No complete kill yet. Melee, ranged and spec candidates will appear from the observed timeline."); }
		else
		{
			Set<String> shown=new HashSet<>();
			for (NexSummary.Sample sample:last.timeline)
			{
				String key=sample.role+new TreeMap<>(sample.state.slots);
				if (!shown.add(key)) { continue; }
				text("Observed "+sample.role+" · tick "+sample.tick);
				item(sample.state.slots.getOrDefault("weapon",-1),catalog,valid);
				text("Offhand: "+name(sample.state.slots.getOrDefault("shield",-1),catalog));
			}
			text("Worn roles describe evidence, not confirmed attacks. A selected special is a candidate; energy drops and animations remain in details.");
		}
		text(storage);
	}
	private void details(NexSummary summary, KnowledgeCatalog catalog, java.util.function.BooleanSupplier valid)
	{
		text("Kill #"+summary.key+" · "+summary.outcome+" · "+summary.reason);
		text("Capture adapter "+com.ironpath.nex.NexCapture.VERSION);
		text(summary.timerScope); text(NexSummary.DAMAGE_SCOPE);
		if(summary.signals!=null)
		{
			var s=summary.signals;
			text("Measured start (smoke): "+tick(s.smokeTick)+"\nArena start transition (diagnostic): "+tick(s.boundaryTick)
				+"\nSmoke line observed: "+tick(s.observedSmokeTick)
				+"\nNex death/dying: "+tick(s.deathTick)+"\nArena end boundary: "+tick(s.barrierEndTick));
			String barriers=s.trace.stream().filter(line->line.contains("barrier ")).collect(java.util.stream.Collectors.joining("; "));
			text("Barrier readings: "+(barriers.isEmpty()?"not recorded":barriers));
			JPanel trace=ownedBody(); trace.setVisible(false);
			button(this,"Capture signal trace",()->{ if(valid.getAsBoolean()) { trace.setVisible(!trace.isVisible()); revalidate(); repaint(); } });
			s.trace.forEach(line->PanelWidgets.text(trace,line)); add(trace);
			text("Observed phases: "+String.join(", ",List.of("SMOKE","SHADOW","BLOOD","ICE","ZAROS").stream().filter(s.phases::contains).toArray(String[]::new))
				+"\nContinuous state samples: "+s.continuous);
		}
		text("State coverage "+Math.min(summary.sampledTicks,summary.expectedTicks)+" / "+summary.expectedTicks+" ticks. Unclassified hits: "+summary.unclassifiedHits+". Eligible fraction counts observed attempts, not kills before capture was enabled.");
		summary.damage.forEach((scope,damage)->text(scope+": "+damage.local+" local / "+damage.total+" observed damage"));
		if(!summary.beforeStart.isEmpty())
		{ text(summary.beforeStart.size()+" pre-smoke state changes retained separately; excluded from fight statistics and equipment comparisons."); }
		List<NexSummary.Sample> history=new ArrayList<>(summary.beforeStart); history.addAll(summary.timeline);
		if (!history.isEmpty())
		{
			JComboBox<String> samples=new JComboBox<>();
			samples.setName("nex-history-state");
			for(int i=0;i<history.size();i++)
			{ var s=history.get(i); samples.addItem((i<summary.beforeStart.size()?"Before smoke · ":"")+"Tick "+s.tick+" · "+s.phase+" · "+s.role); }
			if(!summary.timeline.isEmpty()) { samples.setSelectedIndex(summary.beforeStart.size()); }
			addControl(samples); JPanel evidence=new JPanel(); evidence.setLayout(new BoxLayout(evidence,BoxLayout.Y_AXIS)); evidence.setFont(getFont()); add(evidence);
			Runnable display=()->{
				if (!valid.getAsBoolean()) { return; }
				evidence.removeAll(); NexSummary.Sample s=history.get(samples.getSelectedIndex());
				PanelWidgets.text(evidence,s.phase+" / "+s.window+"\nTarget "+s.target+" · animation "+s.animation+" (launch unverified)");
				new TreeMap<>(s.state.slots).forEach((slot,id)->PanelWidgets.text(evidence,slot+": "+name(id,catalog)));
				PanelWidgets.text(evidence,"Base levels "+new TreeMap<>(s.state.realLevels)+"\nCurrent levels "+new TreeMap<>(s.state.visibleLevels)+"\nPrayers "+s.state.prayers);
				PanelWidgets.text(evidence,"Special energy (tenths of %) "+s.state.specialEnergy+" · selected "+s.state.specialSelected+"\nResources "+new TreeMap<>(s.state.resources));
				Integer charge=s.state.resources.get("death-charge-active");
				PanelWidgets.text(evidence,charge!=null && charge==2?"Detected two active Death Charge charges (improved active use)":charge!=null && charge==1?"Detected one active charge; upgrade unknown from this value":"No active charge observed; upgrade availability unknown");
				PanelWidgets.text(evidence,"Inventory "+(s.state.inventory==null?"unread":new TreeMap<>(s.state.inventory))+"\nChanges alone do not prove consumption. Quiver raw counters need client validation; other internal charges remain unknown.");
				evidence.revalidate(); revalidate();
			}; samples.addActionListener(e->display.run()); display.run();
		}
		summary.gaps.forEach(this::text);
	}
	private static String tick(Integer value) { return value==null?"not observed":"tick "+value; }
	private void account(AccountSnapshot account, Preferences prefs, KnowledgeCatalog catalog, Actions actions,
		java.util.function.BooleanSupplier valid, String storage)
	{
		text("Account corrections"); text("Observed possession and readable unlocks take precedence. Manual facts do not establish charges or current usability.");
		Map<String,String> facts=catalog.factChoices();
		JComboBox<String> choices=new JComboBox<>(facts.keySet().stream().sorted().toArray(String[]::new)); addControl(choices);
		choices.setSelectedItem(factKey);
		PanelWidgets.text(this,facts.get((String)choices.getSelectedItem()));
		JTextArea description=(JTextArea)getComponent(getComponentCount()-1);
		choices.addActionListener(e->{factKey=(String)choices.getSelectedItem(); description.setText(facts.get(factKey));});
		button(this,"Owned / unlocked",()->{if(valid.getAsBoolean() && actions.canEdit()) { actions.confirm((String)choices.getSelectedItem(),true); }});
		button(this,"Not owned / locked",()->{if(valid.getAsBoolean() && actions.canEdit()) { actions.confirm((String)choices.getSelectedItem(),false); }});
		button(this,"Clear selected fact",()->{if(valid.getAsBoolean() && actions.canEdit()) { actions.confirm((String)choices.getSelectedItem(),null); }});
		prefs.confirmations.forEach((key,value)->text(facts.getOrDefault(key,key)+": "+value.evidence()));
		text("Equipment slots: "+(account.equipmentSlots.known()?account.equipmentSlots.value():"unknown"));
		text("Bank ownership: "+(account.bank.known()?"observed this session":"unknown; open bank to observe")); text(storage);
	}
	private void item(int id, KnowledgeCatalog catalog, java.util.function.BooleanSupplier valid)
	{
		JLabel image=new JLabel("◇") {
			@Override public void setIcon(Icon icon) { if(valid.getAsBoolean()) { super.setIcon(icon); } }
		}; image.setPreferredSize(new Dimension(32,32));
		JPanel row=new JPanel(new CardHeaderLayout()); row.setFont(getFont()); row.setOpaque(false);
		row.add(image); PanelWidgets.text(row,name(id,catalog)); row.getComponent(1).setName("card-title"); add(row);
		if (id>0 && items!=null) { items.getImage(id).addTo(image); }
	}
	private String name(int id, KnowledgeCatalog catalog) { return id<0?"unknown":id==0?"empty":displayNames.getOrDefault(id,"Item "+id); }
	private void addControl(JComponent control)
	{
		control.setFont(getFont()); control.setMaximumSize(new Dimension(PANEL_WIDTH-12,Math.max(28,control.getPreferredSize().height))); control.setAlignmentX(LEFT_ALIGNMENT); add(control);
	}
	private void text(String value) { PanelWidgets.text(this,value); }
	private static JButton button(JPanel parent,String value,Runnable action) { return PanelWidgets.button(parent,value,action); }
	static final class ItemChoice
	{
		final int id; final String label;
		ItemChoice(int id,String label) { this.id=id; this.label=label; }
		@Override public String toString() { return label; }
	}
}
