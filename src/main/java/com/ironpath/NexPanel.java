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
		default boolean contextCurrent() { return current(); }
		boolean canEdit();
		void method(NexMethod method);
		void confirm(String key, Boolean value);
		void reset();
		void enable(boolean enabled);
		default void executeScenario(Runnable task) { java.util.concurrent.ForkJoinPool.commonPool().execute(task); }
		default void context(long key,long revision,com.ironpath.nex.NexContext context,boolean remember) { }
	}
	@FunctionalInterface interface ClipboardWriter { void copy(String text); }
	private final ItemManager items;
	private final ClipboardWriter clipboard;
	private final NexReport.Provenance provenance;
	private NexSummary diagnosticSummary;
	private NexOwnedResult diagnosticOwned, inspectedCalculation;
	private com.ironpath.nex.NexRecord diagnosticRecord;
	private NexSummary contextTarget;
	private NexContextEditor contextEditor;
	private String contextReturnPage="encounter";
	private int diagnosticChoice, diagnosticPair;
	private boolean diagnosticDefaultPending;
	private JTextArea reportArea;
	private JScrollPane reportScroll;
	private String reportFeedback="";
	private NexScenarioPanel scenarioPanel;
	private java.util.function.Consumer<Runnable> scenarioExecutor;
	private boolean scenarioAllowed;
	private boolean moreComparisons, sessionTools, calculationDetailsExpanded;
	private NexSummary calculationDetailsSummary;

	private String page="encounter";
	private NexSummary inspected;
	private NexOwnedResult inspectedOwned;
	private int ownedIndex;
	private int ownedPairIndex;
	private String ownedRoleFilter="All roles", ownedSupportFilter="Supported comparisons";
	private String factKey="rigour", roleKey="melee", slotKey="weapon";
	private Integer lockItem;
	private Map<Integer,String> displayNames=Map.of();
	private long view;
	private java.awt.Point returnPosition;
	NexPanel(ItemManager items)
	{ this(items,text->java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(text),null),NexReport.Provenance.LIVE_PUBLIC_CLIENT); }
	NexPanel(ItemManager items,ClipboardWriter clipboard,NexReport.Provenance provenance)
	{ this.items=items; this.clipboard=clipboard; this.provenance=provenance; }

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
		if(scenarioPanel!=null) { scenarioPanel.detach(); }
		scenarioExecutor=actions==null?null:actions::executeScenario; scenarioAllowed=!active;
		boolean paused=status.startsWith("Nex session paused");
		boolean available=account!=null && actions!=null;
		if(!available || paused || stats.attempts==0 || calculationDetailsSummary!=null && !retained(stats,calculationDetailsSummary))
		{ calculationDetailsSummary=null; calculationDetailsExpanded=false; }
		if(!page.equals("diagnostics") || !available || paused || stats.attempts==0) { clearReport(); }
		if(account==null || (!paused && stats.attempts==0))
		{
			inspected=null; inspectedOwned=null; inspectedCalculation=null; diagnosticSummary=null; diagnosticOwned=null; diagnosticRecord=null;
			ownedIndex=0; returnPosition=null; diagnosticDefaultPending=true; scenarioPanel=null;
			if(account==null) { contextTarget=null; contextEditor=null; diagnosticRecord=null; }
		}
		if(available && !paused)
		{
			if(inspected!=null && !retained(stats,inspected)) { inspected=null; inspectedCalculation=null; }
			if(inspectedOwned!=null && !retained(stats,inspectedOwned.summary)) { inspectedOwned=null; }
			if(diagnosticSummary!=null && !retained(stats,diagnosticSummary)) { diagnosticSummary=null; diagnosticOwned=null; diagnosticRecord=null; clearReport(); }
			if(contextTarget!=null && !retained(stats,contextTarget)) { contextTarget=null; contextEditor=null; page="encounter"; }
			if(inspectedOwned!=null)
			{ var record=stats.record(inspectedOwned.summary); if(record.revision!=inspectedOwned.contextRevision && record.calculation!=null) { inspectedOwned=record.calculation; } }
			if(inspected!=null && (inspectedCalculation==null || stats.record(inspected).revision!=inspectedCalculation.contextRevision))
			{ inspectedCalculation=stats.record(inspected).calculation; }
			if(diagnosticSummary!=null)
			{
				var record=stats.record(diagnosticSummary);
				if(diagnosticRecord==null || diagnosticRecord.revision!=record.revision || diagnosticOwned==null && record.calculation!=null)
				{ diagnosticRecord=record; diagnosticOwned=record.calculation; clearReport(); }
			}
		}
		if(account==null)
		{
			if(!page.equals("diagnostics")) { page="encounter"; }
			factKey="rigour"; roleKey="melee"; slotKey="weapon"; lockItem=null;
			ownedRoleFilter="All roles"; ownedSupportFilter="Supported comparisons";
		}
		Map<Integer,String> names=new HashMap<>(catalog==null?Map.of():catalog.itemNames());
		if(available && !paused && stats.last!=null) { stats.last.timeline.forEach(sample->names.putAll(sample.state.itemNames)); }
		if(available && !paused && inspected!=null) { inspected.timeline.forEach(sample->names.putAll(sample.state.itemNames)); }
		displayNames=Map.copyOf(names);
		// Keep the selectable report attached during a valid refresh. Removing its
		// subtree would discard native focus even if caret/scroll were restored later.
		for(var component:getComponents()) { if(component!=reportScroll && component!=contextEditor) { remove(component); } }
		if(contextEditor!=null && (!page.equals("context") || !available || paused)) { remove(contextEditor); }
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS)); setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(BorderFactory.createEmptyBorder(3,2,4,2));
		PanelWidgets.label(this,"IronPath · Nex",true,2,ColorScheme.TEXT_COLOR);
		Runnable redraw=()->render(status,account,prefs,catalog,stats,enabled,actions,storage,owned,active);
		java.util.function.BooleanSupplier valid=()->view==currentView && (actions==null || actions.current());
		// Frozen kill views depend on session/result revisions, not unrelated live account reads.
		java.util.function.BooleanSupplier contextValid=()->view==currentView && (actions==null || actions.contextCurrent());
		JPanel nav=new ActionLinks(false,4); nav.setFont(getFont().deriveFont(Math.min(18f,getFont().getSize2D())));
		for(String destination:List.of("encounter","diagnostics"))
		{
			JButton tab=button(nav,destination.equals("encounter")?"Encounter":"Diagnostics",()->{
				if(!contextValid.getAsBoolean()) { return; }
				if(destination.equals("diagnostics"))
				{
					if(!page.equals("diagnostics"))
					{
						diagnosticSummary=page.equals("context") && contextTarget!=null?contextTarget:inspectedOwned!=null?inspectedOwned.summary:inspected!=null?inspected:stats.last!=null?stats.last:stats.lastAttempt;
						diagnosticRecord=stats.record(diagnosticSummary);
						diagnosticOwned=diagnosticRecord!=null && diagnosticRecord.calculation!=null?diagnosticRecord.calculation:inspectedOwned!=null?inspectedOwned:inspected!=null?inspectedCalculation:owned;
						if(diagnosticOwned!=null && diagnosticOwned.summary!=diagnosticSummary) { diagnosticOwned=null; }
						diagnosticChoice=inspectedOwned!=null?ownedIndex:0; diagnosticPair=inspectedOwned!=null?ownedPairIndex:0;
						diagnosticDefaultPending=inspectedOwned==null;
						if(diagnosticOwned!=null && diagnosticOwned.summary!=diagnosticSummary) { diagnosticOwned=null; }
					}
				}
				else { inspected=null; inspectedOwned=null; inspectedCalculation=null; diagnosticSummary=null; diagnosticOwned=null; diagnosticRecord=null; contextTarget=null; contextEditor=null; }
				page=destination; redraw.run();
			});
			tab.setName("nex-nav-"+destination); tab.getAccessibleContext().setAccessibleDescription(destination+" view");
			boolean selected=page.equals(destination) || destination.equals("encounter") && !page.equals("diagnostics");
			tab.putClientProperty("selected",selected);
			tab.setSelected(selected); tab.getAccessibleContext().setAccessibleDescription((selected?"Selected ":"")+destination+" view");
			tab.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,3,0,selected?new java.awt.Color(191,198,208):PanelWidgets.LINE),BorderFactory.createEmptyBorder(3,3,3,3)));
			tab.setFont(nav.getFont()); tab.setMargin(new java.awt.Insets(0,0,0,0));
		}
		add(nav);
		if(page.equals("diagnostics"))
		{
			if(diagnosticSummary==null && available && !paused) { diagnosticSummary=stats.last!=null?stats.last:stats.lastAttempt; }
			if(diagnosticSummary!=null && available && !paused) { diagnosticRecord=stats.record(diagnosticSummary); }
			if(diagnosticSummary!=null && diagnosticOwned==null && owned!=null && owned.summary==diagnosticSummary && available && !paused) { diagnosticOwned=owned; }
			if(diagnosticOwned!=null && !coherent(stats.record(diagnosticSummary),diagnosticOwned)) { diagnosticOwned=null; }
			if(available && !paused) { selectDiagnosticDefault(); }
			NexReport.State state=paused?NexReport.State.PAUSED:account==null?NexReport.State.NO_ACCOUNT:!available?NexReport.State.NOT_READY
				:stats.attempts==0 && stats.resetReason!=null?NexReport.State.RESET:diagnosticSummary==null?NexReport.State.NO_KILL:diagnosticOwned==null && diagnosticSummary.outcome==com.ironpath.encounter.EncounterSummary.Outcome.COMPLETE?NexReport.State.PENDING:NexReport.State.READY;
			diagnostics(status,stats,catalog,state,contextValid);
			if(available && !paused && diagnosticRecord!=null) { context(diagnosticRecord,redraw,contextValid); }
			if(!paused && available && (inspectedOwned!=null || inspected!=null))
			{ button(this,inspectedOwned!=null?"Back to comparison":"Back to kill details",()->{ if(contextValid.getAsBoolean()) { page="encounter"; redraw.run(); } }); }
		}
		else if(!available) { text(status); }
		else if(paused) { text(status); }
		else if(page.equals("context"))
		{
			button(this,"Back to selected view",()->{ if(contextValid.getAsBoolean()) { page=contextReturnPage; redraw.run(); } });
			var record=stats.record(contextTarget);
			if(contextTarget!=null && record==null) { text("This kill is no longer retained."); }
			else
			{
				heading(record==null?"Future kill context":"Kill context");
				if(record!=null)
				{ text(String.format(Locale.ROOT,"Selected kill · %.1f s",contextTarget.seconds()));
					text(record.calculation==null?"Updating calculation…":"Context applied"); }
				long key=record==null?-1:record.summary.key, annotationRevision=record==null?-1:record.revision;
				if(contextEditor==null || contextEditor.key!=key || contextEditor.revision!=annotationRevision
					|| record==null && !contextEditor.initial.equals(prefs.nexContext))
				{
					if(contextEditor!=null) { remove(contextEditor); }
					contextEditor=new NexContextEditor(key,annotationRevision,record==null?prefs.nexContext:record.context,getFont());
				}
				contextEditor.bind((context,remember)->{ if(contextValid.getAsBoolean()) { actions.context(key,annotationRevision,context,remember); } },actions.canEdit());
				if(contextEditor.getParent()!=this) { add(contextEditor); } else { setComponentZOrder(contextEditor,getComponentCount()-1); }
				text(storage);
			}
		}
		else if(page.equals("account")) { back(redraw,contextValid); account(account,prefs,catalog,actions,valid,storage); }
		else if(page.equals("setup")) { back(redraw,contextValid); setup(prefs,stats.last,catalog,actions,valid,storage); }
		else if(inspectedOwned!=null)
		{
			back(redraw,contextValid); var record=stats.record(inspectedOwned.summary); context(record,redraw,contextValid);
			if(record!=null && record.revision!=inspectedOwned.contextRevision) { text("Calculation pending for corrected context. Previous outputs withheld."); }
			else { ownedDetails(inspectedOwned,catalog,contextValid); }
		}
		else if(inspected!=null) { back(redraw,contextValid); context(stats.record(inspected),redraw,contextValid); details(inspected,catalog,contextValid); }
		else
		{
			if(!enabled) { text("Capture disabled. Session cleared."); }
			else if(active) { text("Observing the current Nex attempt. Results appear after completion."); }
			else if(stats.last==null) { text("Ready to observe the next full Nex kill. Equipment and spell use are detected automatically."); }
			if(!active && stats.attempts==0 && stats.resetReason!=null) { text("Previous session cleared: "+stats.resetReason); }
			if(stats.last!=null)
			{
				JPanel kill=PanelWidgets.card(this,"nex-kill-card");
				PanelWidgets.label(kill,"Last kill",true,0,PanelWidgets.MUTED).getAccessibleContext().setAccessibleName("Last complete kill");
				boolean enlarged=getFont().getSize()>=18;
				JPanel metrics=enlarged?PanelWidgets.column(kill):new JPanel(new java.awt.GridLayout(1,2,8,0));
				metrics.setOpaque(false); metrics.setFont(getFont()); metrics.setAlignmentX(LEFT_ALIGNMENT); kill.add(metrics);
				killMetric(metrics,String.format(Locale.ROOT,"%.1f s",stats.last.seconds()),enlarged?"Time":"Duration","nex-duration",enlarged);
				killMetric(metrics,stats.last.share()==null?"Unknown":String.format(Locale.ROOT,"%.1f%%",stats.last.share()),"Share","nex-share",enlarged);
				PanelWidgets.label(kill,"Observed Nex + minion hitsplats",false,0,PanelWidgets.MUTED);
				if(stats.n>1) { PanelWidgets.text(kill,aggregateLabel(stats)); }
				context(stats.record(stats.last),redraw,contextValid);
			}
			if(stats.lastAttempt!=null && stats.lastAttempt!=stats.last)
			{
				text("Latest attempt incomplete.");
				button(this,"Last attempt details",()->{ if(contextValid.getAsBoolean()) { inspected=stats.lastAttempt; inspectedCalculation=null; rememberPosition(); redraw.run(); } });
			}
			if(stats.last!=null)
			{
				heading("Compare setups");
				if(!coherent(stats.record(stats.last),owned)) { text("Comparing the captured setups…"); }
				else
				{
					List<NexPresentation.Card> cards=NexPresentation.cards(owned);
					if(cards.isEmpty()) { text(owned.choices.stream().anyMatch(c->c.supported())?"No improvement found in supported setups.":"No supported setup comparison yet."); }
					JPanel further=ownedBody(); further.setVisible(moreComparisons);
					for(int cardIndex=0;cardIndex<cards.size();cardIndex++)
					{
						var card=cards.get(cardIndex);
						JPanel surface=PanelWidgets.card(cardIndex<2?this:further,"nex-equipment-card-"+card.choiceIndex);
						equipmentCard(surface,card,catalog,contextValid,()->{ openOwned(owned,card.choiceIndex,card.pairIndex); redraw.run(); });
					}
					if(cards.size()>2)
					{
						button(this,moreComparisons?"Fewer comparisons":"More comparisons ("+(cards.size()-2)+")",()->{ if(contextValid.getAsBoolean()) { moreComparisons=!moreComparisons; redraw.run(); } }).setName("nex-more-comparisons");
						add(further);
					}
				}
				JPanel inspection=PanelWidgets.card(this,"nex-inspection-actions");
				PanelWidgets.label(inspection,"Inspect",true,0,PanelWidgets.MUTED);
				JPanel links=new ActionLinks(); links.setFont(getFont()); inspection.add(links);
				if(coherent(stats.record(stats.last),owned))
				{
					JButton comparisons=button(links,"Comparisons",()->{ if(contextValid.getAsBoolean()) { openOwned(owned,0,0); redraw.run(); } });
					comparisons.setName("nex-owned"); comparisons.setToolTipText("Owned setup comparisons");
					button(links,"Spec and package plans",()->{
						if(contextValid.getAsBoolean()) { openOwned(owned,0,0); ownedSupportFilter="All states"; redraw.run(); }
					}).setName("nex-plans");
				}
				JButton details=button(links,"Kill details",()->{
					if(contextValid.getAsBoolean()) { rememberPosition(); inspected=stats.last; inspectedCalculation=owned!=null && owned.summary==stats.last?owned:null; redraw.run(); }
				}); details.setName("nex-details"); details.setToolTipText("Kill and calculation details");
			}
			JPanel management=PanelWidgets.card(this,"nex-management-actions");
			PanelWidgets.label(management,"Setup & session",true,0,PanelWidgets.MUTED);
			JPanel settings=new ActionLinks(); settings.setFont(getFont()); management.add(settings);
			for(String destination:List.of("setup","account"))
			{ button(settings,destination.equals("setup")?"Setup":"Account",()->{ if(valid.getAsBoolean()) { rememberPosition(); page=destination; redraw.run(); } }); }
			JButton defaults=button(settings,"Future context",()->{ if(contextValid.getAsBoolean()) { contextTarget=null; contextEditor=null; contextReturnPage="encounter"; page="context"; redraw.run(); } });
			defaults.setToolTipText("Future kill context");
			JPanel controls=new ActionLinks(); controls.setFont(getFont()); management.add(controls);
			JButton capture=button(controls,enabled?"Capture on":"Capture off",()->{ if(valid.getAsBoolean()) { actions.enable(!enabled); } });
			capture.setToolTipText(enabled?"Disable capture":"Enable capture");
			JButton reset=button(controls,"Reset",()->{ if(valid.getAsBoolean()) { actions.reset(); } }); reset.setName("nex-reset"); reset.setToolTipText("Reset session");
			JButton counts=button(controls,"Counts",()->{ if(contextValid.getAsBoolean()) { sessionTools=!sessionTools; redraw.run(); } }); counts.setToolTipText("Session tools");
			if(sessionTools) { PanelWidgets.text(management,stats.complete+" complete · "+stats.failed+" failed · "+stats.censored+" excluded"); }

		}
		if(available && paused)
		{ button(this,"Reset session",()->{ if(valid.getAsBoolean()) { actions.reset(); } }).setName("nex-reset"); }
		if(available && !paused && (page.equals("setup") || page.equals("account")))
		{
			JPanel secondary=new ActionLinks(); secondary.setFont(getFont());
			for(String destination:List.of("setup","account"))
			{ button(secondary,destination.equals("setup")?"Setup":"Account",()->{ if(valid.getAsBoolean()) { page=destination; redraw.run(); } }); }
			add(secondary);
		}
		if(available && paused)
		{
			for(String destination:List.of("setup","account"))
			{ button(this,destination.equals("setup")?"Setup":"Account",()->{ if(valid.getAsBoolean()) { page=destination; redraw.run(); } }); }
		}
		revalidate(); repaint();
	}
	private void heading(String value)
	{ text(value); getComponent(getComponentCount()-1).setFont(getFont().deriveFont(java.awt.Font.BOLD)); }
	private static boolean retained(NexSession.Stats stats,NexSummary summary)
	{ var record=stats.record(summary); return record!=null && record.summary==summary; }
	private static boolean coherent(com.ironpath.nex.NexRecord record,NexOwnedResult result)
	{
		return record!=null && result!=null && record.summary==result.summary && record.revision==result.contextRevision
			&& record.context.equals(result.context) && (record.inputs==null || record.inputs.revision==result.inputRevision && record.inputs.modelRevision.equals(result.modelRevision));
	}
	private static String aggregateLabel(NexSession.Stats stats)
	{
		if(stats.n==0) { return "No matched average"; }
		return stats.n==1?"1 matching kill":String.format(Locale.ROOT,"Comparable mean %.1f s · %d kills",stats.meanSeconds,stats.n);
	}
	private void killMetric(JPanel parent,String value,String caption,String name,boolean enlarged)
	{
		JPanel metric=enlarged?new JPanel(new java.awt.BorderLayout(8,0)):PanelWidgets.column(parent);
		metric.setFont(getFont()); metric.setOpaque(false); metric.setAlignmentX(LEFT_ALIGNMENT);
		JPanel valueColumn=PanelWidgets.column(metric);
		JTextArea number=PanelWidgets.label(valueColumn,value,true,enlarged?4:7,ColorScheme.TEXT_COLOR); number.setName(name);
		if(enlarged)
		{
			number.setMaximumSize(new Dimension(number.getFontMetrics(number.getFont()).stringWidth(value)+2,Integer.MAX_VALUE));
			metric.add(valueColumn,java.awt.BorderLayout.WEST);
			JPanel label=PanelWidgets.column(metric); PanelWidgets.label(label,caption,false,0,PanelWidgets.MUTED); metric.add(label,java.awt.BorderLayout.CENTER);
		}
		else { metric.add(valueColumn); PanelWidgets.label(metric,caption,false,0,PanelWidgets.MUTED); }
		parent.add(metric);
	}
	private void equipmentCard(JPanel surface,NexPresentation.Card card,KnowledgeCatalog catalog,java.util.function.BooleanSupplier valid,Runnable inspect)
	{
		var choice=card.choice; var changed=NexPresentation.changedItems(card.pair);
		String title=changed.size()==1?name(changed.values().iterator().next(),catalog):changed.size()+"-item package";
		if(changed.size()==1) { item(surface,changed.values().iterator().next(),title,valid); }
		else
		{
			JPanel sprites=new ActionLinks(); sprites.setFont(getFont());
			changed.values().stream().filter(id->id>0).distinct().limit(3).forEach(id->sprites.add(sprite(id,valid)));
			surface.add(sprites); PanelWidgets.label(surface,title,true,0,ColorScheme.TEXT_COLOR);
		}
		PanelWidgets.label(surface,NexOwnedResult.headline(card.pair).replace("theoretical DPS","DPS"),true,5,PanelWidgets.BENEFIT).setName("nex-card-metric-"+card.choiceIndex);
		PanelWidgets.label(surface,"Modelled · "+NexPresentation.scope(choice).toLowerCase(Locale.ROOT),false,0,PanelWidgets.MUTED).setName("nex-card-scope-"+card.choiceIndex);
		PanelWidgets.label(surface,NexPresentation.reason(card.pair),false,0,PanelWidgets.MUTED);
		PanelWidgets.label(surface,choice.bankPreparation?"Bank prep · check packing & stock":"Carried · supplies assumed",false,0,PanelWidgets.MUTED);
		if(!choice.exhaustive) { PanelWidgets.label(surface,"Partial search",false,0,PanelWidgets.MUTED); }
		if(choice.coverage.stream().anyMatch(gap->gap.startsWith("Carried alternative not modelled:") || gap.startsWith("Unmodelled alternatives in this search:")))
		{ PanelWidgets.label(surface,"Some alternatives unmodelled",false,0,PanelWidgets.MUTED); }
		if(card.pair.beforeSetup.slots.containsValue(24780) || card.pair.afterSetup.slots.containsValue(24780))
		{ PanelWidgets.label(surface,"Healing and survival unmodelled",false,0,PanelWidgets.MUTED); }
		JPanel footer=new ActionLinks(true); footer.setFont(getFont()); surface.add(footer);
		JButton details=button(footer,"Details →",()->{ if(valid.getAsBoolean()) { inspect.run(); } });
		details.setToolTipText("Compare package"); details.setName("nex-card-"+card.choiceIndex); details.getAccessibleContext().setAccessibleName("Compare "+title);
	}
	private void context(com.ironpath.nex.NexRecord record,Runnable redraw,java.util.function.BooleanSupplier valid)
	{
		if(record==null) { return; }
		JPanel surface=new JPanel(new java.awt.BorderLayout(5,0)); surface.setFont(getFont()); surface.setOpaque(false); surface.setAlignmentX(LEFT_ALIGNMENT);
		surface.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,PanelWidgets.LINE),BorderFactory.createEmptyBorder(2,2,6,2)));
		surface.setName("nex-context-summary"); add(surface);
		JPanel summary=PanelWidgets.column(surface); surface.add(summary,java.awt.BorderLayout.CENTER);
		PanelWidgets.text(summary,NexPresentation.contextValue(record.context));
		PanelWidgets.label(summary,record.context.confirmed()?"Confirmed":"Unconfirmed",false,0,PanelWidgets.MUTED);
		if(record.summary.outcome!=com.ironpath.encounter.EncounterSummary.Outcome.COMPLETE) { return; }
		JPanel action=PanelWidgets.column(surface); surface.add(action,java.awt.BorderLayout.EAST);
		JButton edit=button(action,"Edit",()->{
			if(!valid.getAsBoolean()) { return; }
			contextTarget=record.summary; contextEditor=null; contextReturnPage=page; page="context"; redraw.run();
		}); edit.setName("nex-edit-context"); edit.setToolTipText("Edit kill context");
		if(!record.context.pilot() && (page.equals("diagnostics") || inspected!=null || inspectedOwned!=null))
		{ text("Pilot recommendations unavailable for this context. Independent ordinary offence remains conditional."); }
	}
	private void rememberPosition()
	{
		var viewport=(JViewport)SwingUtilities.getAncestorOfClass(JViewport.class,this);
		returnPosition=viewport==null?null:viewport.getViewPosition();
	}
	private void openOwned(NexOwnedResult owned,int index,int pair)
	{
		rememberPosition(); inspectedOwned=owned; ownedIndex=index; ownedPairIndex=pair;
		ownedRoleFilter="All roles"; ownedSupportFilter=owned.choices.stream().anyMatch(c->c.supported())?"Supported comparisons":"All states";
	}
	private void back(Runnable redraw,java.util.function.BooleanSupplier valid)
	{
		button(this,"Back to encounter",()->{
			if(!valid.getAsBoolean()) { return; } page="encounter"; inspected=null; inspectedOwned=null; inspectedCalculation=null; redraw.run();
			var viewport=(JViewport)SwingUtilities.getAncestorOfClass(JViewport.class,this);
			if(viewport!=null && returnPosition!=null) { viewport.setViewPosition(returnPosition); }
		});
	}
	/** Resolve a default once the frozen calculation arrives, without replacing an explicit inspected selection. */
	private void selectDiagnosticDefault()
	{
		if(!diagnosticDefaultPending || diagnosticOwned==null || diagnosticOwned.summary!=diagnosticSummary) { return; }
		var cards=NexPresentation.cards(diagnosticOwned);
		if(!cards.isEmpty()) { var card=cards.get(0); diagnosticChoice=card.choiceIndex; diagnosticPair=card.pairIndex; }
		else
		{
			diagnosticChoice=diagnosticOwned.choices.isEmpty()?-1:0; diagnosticPair=0;
			for(int i=0;i<diagnosticOwned.choices.size();i++)
			{ if(diagnosticOwned.choices.get(i).supported()) { diagnosticChoice=i; break; } }
		}
		diagnosticDefaultPending=false;
	}
	private void diagnostics(String status,NexSession.Stats stats,KnowledgeCatalog catalog,NexReport.State state,java.util.function.BooleanSupplier valid)
	{
		heading("Diagnostics");
		PanelWidgets.label(this,"Build "+BuildInfo.VERSION,false,0,PanelWidgets.MUTED);
		boolean hidden=state==NexReport.State.PAUSED || state==NexReport.State.NO_ACCOUNT || state==NexReport.State.NOT_READY || state==NexReport.State.RESET;
		text(status);
		if(stats.resetReason!=null) { text("Previous session cleared: "+stats.resetReason); }
		NexSummary summary=hidden?null:diagnosticSummary; NexOwnedResult result=hidden?null:diagnosticOwned;
		if(summary!=null) { text("Frozen kill #"+summary.key+" · "+summary.outcome); }
		else { text("No visible kill evidence"); }
		String report=NexReport.snapshot(summary,result,diagnosticChoice,diagnosticPair,catalog,provenance,state,hidden?null:diagnosticRecord).format();
		if(reportArea!=null && !report.equals(reportArea.getText())) { clearReport(); }
		PanelWidgets.text(this,reportFeedback); final JTextArea feedback=(JTextArea)getComponent(getComponentCount()-1);
		button(this,"Copy report now",()->{
			if(!valid.getAsBoolean()) { return; }
			reportArea.setText(report); reportArea.setCaretPosition(0); reportArea.getParent().getParent().setVisible(true);
			try { clipboard.copy(report); reportFeedback="Report copied. Selectable text is below."; }
			catch(java.awt.HeadlessException | IllegalStateException | SecurityException ex)
			{ reportFeedback="Clipboard unavailable or busy. Select the report below and copy it manually."; }
			feedback.setText(reportFeedback);
			revalidate(); repaint();
		}).setName("nex-copy-report");
		JPanel inputs=PanelWidgets.card(this,"nex-diagnostic-inputs");
		PanelWidgets.label(inputs,"Inputs",true,0,ColorScheme.TEXT_COLOR);
		PanelWidgets.text(inputs,"Nex · "+(provenance==NexReport.Provenance.SYNTHETIC_OFFLINE?"synthetic fixture":"public client observations"));
		PanelWidgets.text(inputs,summary==null?"No visible kill evidence":"Captured gear, levels & prayers");
		PanelWidgets.label(inputs,"Base target stats and planning locks are assumptions.",false,0,PanelWidgets.MUTED);
		JPanel calculations=PanelWidgets.card(this,"nex-diagnostic-calculations");
		PanelWidgets.label(calculations,"Calculation status",true,0,ColorScheme.TEXT_COLOR);
		if(summary==null) { PanelWidgets.text(calculations,"Waiting for a completed kill."); }
		else
		{
			PanelWidgets.text(calculations,summary.outcome==com.ironpath.encounter.EncounterSummary.Outcome.COMPLETE?"Observed duration recorded":"Incomplete attempt · excluded");
			if(result==null) { PanelWidgets.text(calculations,"Calculation pending or unavailable for this kill."); }
			else
			{
				long supported=result.choices.stream().filter(c->c.supported()).count();
				PanelWidgets.text(calculations,supported==0?"No supported setup comparison":supported+" supported setup scenarios");
			}
		}
		JPanel missing=PanelWidgets.card(this,"nex-diagnostic-gaps");
		PanelWidgets.label(missing,"Missing / limited",true,0,ColorScheme.TEXT_COLOR);
		if(result!=null)
		{
			result.gaps.entrySet().stream().sorted(Map.Entry.comparingByKey()).limit(8).forEach(e->PanelWidgets.text(missing,e.getValue()+" · "+NexReport.gapLabel(e.getKey())));
			if(result.gaps.size()>8) { PanelWidgets.text(missing,"More gaps remain in comparison details."); }
		}
		PanelWidgets.text(missing,"Team-time estimates unavailable.");
		if(summary!=calculationDetailsSummary) { calculationDetailsSummary=summary; calculationDetailsExpanded=false; }
		JPanel basis=ownedBody(); basis.setName("nex-calculation-details"); basis.setVisible(calculationDetailsExpanded);
		button(this,"Calculation details",()->{ if(valid.getAsBoolean()) { calculationDetailsExpanded=!calculationDetailsExpanded; basis.setVisible(calculationDetailsExpanded); revalidate(); repaint(); } });
		PanelWidgets.text(basis,"IronPath · build "+BuildInfo.VERSION);
		PanelWidgets.text(basis,"Adapter "+com.ironpath.nex.NexCapture.VERSION+" · evaluator "+com.ironpath.advisor.NexOwnedEvaluator.VERSION);
		PanelWidgets.text(basis,"Knowledge "+(catalog==null?"unavailable":catalog.version));
		PanelWidgets.text(basis,"Pilot planning scope: Trio MP2 · one kill per trip.");
		PanelWidgets.text(basis,"Timing: smoke → death/dying. Observed hitsplat share: Nex + named minions, not server loot credit.");
		if(summary!=null)
		{
			PanelWidgets.text(basis,"State coverage "+Math.min(summary.sampledTicks,summary.expectedTicks)+" / "+summary.expectedTicks+" ticks · "+summary.unclassifiedHits+" unclassified hits");
			if(summary==stats.last)
			{
				PanelWidgets.text(basis,aggregateLabel(stats));
				if(stats.n==0) { PanelWidgets.text(basis,stats.record(summary).exclusion()); }
				if(stats.n>1) { PanelWidgets.text(basis,String.format(Locale.ROOT,"Range %.1f–%.1f s",stats.minSeconds,stats.maxSeconds)); }
				if(stats.shareN>1) { PanelWidgets.text(basis,String.format(Locale.ROOT,"Mean per-kill share %.1f%% · %d kills; aggregate %.1f%%",stats.meanShare,stats.shareN,stats.aggregateShare)); }
				PanelWidgets.text(basis,"Matched averages require confirmed strategy/group/count, compatible timer/adapter, worn packages, sampled levels/prayers and a stable observed arena roster. Planning locks are excluded. Equal count is not equal teammate damage; visible people do not prove party membership.");
			}
			if(result!=null)
			{
				long carried=result.choices.stream().filter(c->c.supported() && !c.bankPreparation).count();
				long bank=result.choices.stream().filter(c->c.supported() && c.bankPreparation).count();
				PanelWidgets.text(basis,"Supported scenarios: carried "+carried+", bank preparation "+bank+". Unavailable: "+(result.choices.size()-carried-bank)+". Counts are not fight coverage.");
				if(diagnosticChoice>=0 && diagnosticChoice<result.choices.size())
				{
					var c=result.choices.get(diagnosticChoice);
					PanelWidgets.text(basis,"Selected "+c.actual.role+" · "+c.actual.phase+" · tick "+c.actual.tick+" · pair "+diagnosticPair);
				}
				PanelWidgets.text(basis,"Independent state comparisons; no whole-kill ranking. Captured gear, levels and prayer readings are separate from the assumed base-stat target and frozen planning locks. No launch attribution or team-time estimate.");
			}
		}
		add(basis);
		button(this,"Show selectable report",()->{ if(valid.getAsBoolean()) { reportArea.getParent().getParent().setVisible(!reportArea.getParent().getParent().isVisible()); revalidate(); repaint(); } });
		if(reportArea==null)
		{
			reportArea=new JTextArea(report) {
				@Override public void scrollRectToVisible(java.awt.Rectangle bounds) { if(isFocusOwner()) { super.scrollRectToVisible(bounds); } }
			}; reportArea.setName("nex-report-text"); reportArea.setEditable(false); reportArea.setLineWrap(true); reportArea.setWrapStyleWord(true); reportArea.setFont(getFont());
			reportArea.getAccessibleContext().setAccessibleName("Sanitized diagnostic report"); reportArea.setCaretPosition(0);
			reportScroll=new JScrollPane(reportArea); reportScroll.setAlignmentX(LEFT_ALIGNMENT);
			reportScroll.setPreferredSize(new Dimension(PANEL_WIDTH-12,180)); reportScroll.setMaximumSize(new Dimension(PANEL_WIDTH-12,180)); reportScroll.setVisible(false); add(reportScroll);
		}
		else { setComponentZOrder(reportScroll,getComponentCount()-1); }
		text("Names, account IDs, roster hashes, raw traces and inventory/bank dumps are omitted. Nothing is uploaded. This report is not a replay.");
	}
	private void clearReport()
	{
		if(reportArea!=null) { reportArea.setText(""); }
		if(reportScroll!=null) { remove(reportScroll); }
		reportArea=null; reportScroll=null; reportFeedback="";
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
		text("Filter by role and support. Hidden rows remain in the captured history and are not recalculated.");
		List<String> roles=new ArrayList<>(); roles.add("All roles");
		result.choices.stream().map(c->c.actual.role).distinct().sorted().forEach(roles::add);
		JComboBox<String> role=new JComboBox<>(roles.toArray(new String[0])); role.setName("nex-owned-role-filter");
		role.setSelectedItem(ownedRoleFilter); addControl(role);
		JComboBox<String> support=new JComboBox<>(new String[]{"Supported comparisons","All states","Unavailable comparisons"});
		support.setName("nex-owned-support-filter"); support.setSelectedItem(ownedSupportFilter); addControl(support);
		if(!result.gaps.isEmpty())
		{
			JPanel gaps=ownedBody(); gaps.setVisible(false);
			button(this,"Why comparisons are unavailable",()->{ if(valid.getAsBoolean()) { gaps.setVisible(!gaps.isVisible()); revalidate(); repaint(); } });
			result.gaps.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
				.forEach(e->PanelWidgets.text(gaps,e.getValue()+(e.getValue()==1?" scenario · ":" scenarios · ")+e.getKey())); add(gaps);
		}
		JComboBox<String> states=new JComboBox<>(); states.setName("nex-owned-state");
		List<Integer> indices=new ArrayList<>(); boolean[] filling={false}; addControl(states);
		JPanel detail=ownedBody(); add(detail);
		Runnable show=()->{
			if(!valid.getAsBoolean() || filling[0] || states.getSelectedIndex()<0) { return; }
			int selected=indices.get(states.getSelectedIndex());
			if(ownedIndex!=selected) { ownedPairIndex=0; }
			ownedIndex=selected; if(scenarioPanel!=null) { scenarioPanel.detach(); } detail.removeAll();
			var c=result.choices.get(ownedIndex);
			PanelWidgets.text(detail,c.bankPreparation?"Bank preparation scenario · not carried in this kill":"Carried alternatives at the captured state");
			if(!c.supported()) { PanelWidgets.text(detail,c.gapReason()); scenario(detail,result,catalog,valid); }
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
					scenario(value,result,catalog,valid);
					value.revalidate(); revalidate();
				}; options.addActionListener(e->compare.run()); compare.run();
			}
			c.coverage.forEach(g->PanelWidgets.text(detail,g)); detail.revalidate(); revalidate(); repaint();
		};
		Runnable filter=()->{
			if(!valid.getAsBoolean()) { return; }
			ownedRoleFilter=(String)role.getSelectedItem(); ownedSupportFilter=(String)support.getSelectedItem();
			filling[0]=true; indices.clear(); states.removeAllItems();
			for(int i=0;i<result.choices.size();i++)
			{
				var c=result.choices.get(i);
				if(!ownedRoleFilter.equals("All roles") && !ownedRoleFilter.equals(c.actual.role)) { continue; }
				if(ownedSupportFilter.equals("Supported comparisons") && !c.supported()
					|| ownedSupportFilter.equals("Unavailable comparisons") && c.supported()) { continue; }
				indices.add(i); states.addItem(c.actual.role+" · tick "+c.actual.tick+" · "+c.actual.phase+(c.bankPreparation?" · bank preparation":" · carried"));
			}
			if(!indices.isEmpty()) { states.setSelectedIndex(Math.max(0,indices.indexOf(ownedIndex))); }
			filling[0]=false; states.setEnabled(!indices.isEmpty());
			if(indices.isEmpty())
			{ ownedIndex=-1; ownedPairIndex=0; if(scenarioPanel!=null) { scenarioPanel.detach(); } detail.removeAll(); PanelWidgets.text(detail,"No scenarios match these filters. Choose All states to inspect this role's coverage gaps."); }
			else { show.run(); }
			detail.revalidate(); revalidate(); repaint();
		};
		states.addActionListener(e->show.run()); role.addActionListener(e->filter.run()); support.addActionListener(e->filter.run()); filter.run();
	}
	private void scenario(JPanel parent,NexOwnedResult result,KnowledgeCatalog catalog,java.util.function.BooleanSupplier valid)
	{
		if(result.planning==null || ownedIndex<0 || scenarioExecutor==null) { return; }
		if(scenarioPanel==null || scenarioPanel.source!=result || scenarioPanel.choice!=ownedIndex || scenarioPanel.pair!=ownedPairIndex)
		{
			if(scenarioPanel!=null) { scenarioPanel.detach(); }
			scenarioPanel=new NexScenarioPanel(result,ownedIndex,ownedPairIndex,catalog,clipboard,provenance.toString(),getFont());
		}
		boolean allowed=scenarioAllowed;
		scenarioPanel.attach(()->allowed && valid.getAsBoolean(),scenarioExecutor); parent.add(scenarioPanel);
	}
	private JPanel ownedBody()
	{
		return PanelWidgets.column(this);
	}
	private String changes(ComparisonResult pair,KnowledgeCatalog catalog)
	{
		List<String> changes=new ArrayList<>();
		for(String slot:com.ironpath.advisor.PoweredMagic.SLOTS)
		{ if(!Objects.equals(pair.beforeSetup.slots.get(slot),pair.afterSetup.slots.get(slot))) { changes.add(slot+": "+name(pair.beforeSetup.slots.get(slot),catalog)+" → "+name(pair.afterSetup.slots.get(slot),catalog)); } }
		if(pair.beforeSetup.loadedAmmo!=pair.afterSetup.loadedAmmo)
		{ changes.add("selected projectile: "+name(pair.beforeSetup.loadedAmmo>0?pair.beforeSetup.loadedAmmo:pair.beforeSetup.slots.getOrDefault("ammo",-1),catalog)
			+" → "+name(pair.afterSetup.loadedAmmo>0?pair.afterSetup.loadedAmmo:pair.afterSetup.slots.getOrDefault("ammo",-1),catalog)); }
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
		text("Planning locks apply to future comparisons. Completed comparisons retain their frozen planning inputs. Empty is an explicit choice.");
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
	{ item(this,id,name(id,catalog),valid); }
	private void item(JPanel parent,int id,String title,java.util.function.BooleanSupplier valid)
	{
		JLabel image=sprite(id,valid);
		JPanel row=new JPanel(new CardHeaderLayout()); row.setFont(getFont()); row.setOpaque(false);
		row.setAlignmentX(LEFT_ALIGNMENT); row.add(image); PanelWidgets.label(row,title,true,0,ColorScheme.TEXT_COLOR); row.getComponent(1).setName("card-title"); parent.add(row);
	}
	private JLabel sprite(int id,java.util.function.BooleanSupplier valid)
	{
		JLabel image=new JLabel(items==null?"◇":"") {
			@Override public void setIcon(Icon icon) { if(valid.getAsBoolean()) { super.setIcon(icon); } }
		}; image.setPreferredSize(new Dimension(40,36)); image.setName("nex-item-sprite-"+id);
		image.getAccessibleContext().setAccessibleName(displayNames.getOrDefault(id,"Item "+id));
		if(items==null) { image.setToolTipText("Offline sprite placeholder"); image.getAccessibleContext().setAccessibleName("Offline sprite placeholder"); }
		if (id>0 && items!=null) { items.getImage(id).addTo(image); }
		return image;
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
