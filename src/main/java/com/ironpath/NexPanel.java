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
	private NexOwnedResult diagnosticFangResult;
	private int diagnosticFangSelection;
	private com.ironpath.nex.NexRecord diagnosticRecord;
	private NexSummary contextTarget;
	private NexContextEditor contextEditor;
	private int diagnosticChoice, diagnosticPair, observationPage;
	private NexSummary observationSummary;
	private boolean diagnosticDefaultPending;
	private JTextArea reportArea;
	private JScrollPane reportScroll;
	private String reportFeedback="";
	private NexScenarioPanel scenarioPanel;
	private java.util.function.Consumer<Runnable> scenarioExecutor;
	private boolean scenarioAllowed;
	private boolean calculationDetailsExpanded;
	private NexSummary adviceSource;
	private NexOwnedResult adviceInspection;
	private int adviceSelection=-1;
	private int adviceGroup;
	private String adviceRole="Melee";
	private boolean adviceAll;
	private final Set<Integer> manualAdvice=new HashSet<>();
	private static final com.ironpath.advisor.EncounterCandidates CANDIDATES=com.ironpath.advisor.EncounterCandidates.nex();
	private NexSummary calculationDetailsSummary;

	private NexSummary inspected;
	private NexOwnedResult inspectedOwned;
	private int ownedIndex;
	private int ownedPairIndex;
	private String ownedRoleFilter="All roles", ownedSupportFilter="Supported comparisons";
	private Map<Integer,String> displayNames=Map.of();
	private long view;
	private java.awt.Point returnPosition, nextPosition, suspendedPosition;
	private boolean availableView;
	private Runnable updateDiagnostics;
	NexPanel(ItemManager items)
	{ this(items,text->java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(text),null),NexReport.Provenance.LIVE_PUBLIC_CLIENT); }
	NexPanel(ItemManager items,ClipboardWriter clipboard,NexReport.Provenance provenance)
	{ this.items=items; this.clipboard=clipboard; this.provenance=provenance; }

	private JPanel content=this, summaryBody, controlsBody;
	private CollapsibleSection compare,team,diagnostic;
	private Long visibleScope;
	private boolean editingDefaults;
	private JTextArea trackingProgress;
	private long trackingCapture=-1;

	enum TrackingStatus
	{
		LOGIN("Log in to track kills"), ACCOUNT("Waiting for account"), UNSUPPORTED_ACCOUNT("Standard Ironman required"),
		UNSUPPORTED_WORLD("Use a permanent members world"), SETTINGS_LOADING("Loading settings…"), SETTINGS_UNAVAILABLE("Settings unavailable · editing disabled"),
		LOADING("Loading encounter…"), BANK("Open bank to verify this account"), READY("Waiting for Nex"),
		OUTSIDE("Go to Nex to track kills"), ACTIVE("Tracking kill…"), OFF("Kill tracking: Off");
		final String label; TrackingStatus(String label) { this.label=label; }
	}
	void render(String status,AccountSnapshot account,Preferences prefs,KnowledgeCatalog catalog,NexSession.Stats stats,boolean enabled,Actions actions,String storage)
	{ render(status,account,prefs,catalog,stats,enabled,actions,storage,null); }
	void render(String status,AccountSnapshot account,Preferences prefs,KnowledgeCatalog catalog,NexSession.Stats stats,boolean enabled,Actions actions,String storage,NexOwnedResult owned)
	{ render(status,account,prefs,catalog,stats,enabled,actions,storage,owned,false); }
	void render(String status,AccountSnapshot account,Preferences prefs,KnowledgeCatalog catalog,NexSession.Stats stats,boolean enabled,Actions actions,String storage,NexOwnedResult owned,boolean active)
	{
		TrackingStatus tracking=status.startsWith("Nex session paused")?TrackingStatus.BANK:account==null?
			(status.startsWith("Outside coverage")?TrackingStatus.UNSUPPORTED_ACCOUNT:TrackingStatus.ACCOUNT):actions==null?TrackingStatus.SETTINGS_LOADING:
			!enabled?TrackingStatus.OFF:active?TrackingStatus.ACTIVE:TrackingStatus.READY;
		render(status,account,prefs,catalog,stats,enabled,actions,storage,owned,active,tracking);
	}
	void render(String status,AccountSnapshot account,Preferences prefs,KnowledgeCatalog catalog,NexSession.Stats stats,boolean enabled,Actions actions,String storage,NexOwnedResult owned,boolean active,TrackingStatus tracking)
	{ render(status,account,prefs,catalog,stats,enabled,actions,storage,owned,active,tracking,null); }
	void render(String status,AccountSnapshot account,Preferences prefs,KnowledgeCatalog catalog,NexSession.Stats stats,boolean enabled,Actions actions,String storage,NexOwnedResult owned,boolean active,TrackingStatus tracking,com.ironpath.nex.NexCapture.Progress progress)
	{
		if(!SwingUtilities.isEventDispatchThread()) { throw new IllegalStateException("Swing thread required"); }
		var viewport=(JViewport)SwingUtilities.getAncestorOfClass(JViewport.class,this);
		java.awt.Point position=nextPosition!=null?nextPosition:viewport==null?null:viewport.getViewPosition(); nextPosition=null;
		long currentView=++view;
		updateDiagnostics=null;
		if(scenarioPanel!=null) { scenarioPanel.detach(); }
		scenarioExecutor=actions==null?null:actions::executeScenario; scenarioAllowed=!active;
		boolean paused=stats.suspended || tracking==TrackingStatus.BANK || status.startsWith("Nex session paused");
		boolean available=account!=null && actions!=null && catalog!=null;
		boolean sameScope=visibleScope!=null && visibleScope==stats.scope;
		// Suspension hides evidence and disables every old action; a verified return may restore drafts.
		// Identity/world/reset boundaries change scope and discard them, even inside collapsed sections.
		if(!sameScope || account==null && !stats.suspended)
		{
			inspected=null; inspectedOwned=null; inspectedCalculation=null; diagnosticSummary=null; diagnosticOwned=null; diagnosticRecord=null;
			contextTarget=null; contextEditor=null; scenarioPanel=null; ownedIndex=0; ownedPairIndex=0;
			returnPosition=null; diagnosticDefaultPending=true; calculationDetailsSummary=null; calculationDetailsExpanded=false;
			observationSummary=null; observationPage=0;
			adviceSource=null; adviceInspection=null; adviceSelection=-1; adviceGroup=0; adviceRole="Melee"; adviceAll=false; manualAdvice.clear();
			editingDefaults=false;
			ownedRoleFilter="All roles"; ownedSupportFilter="Supported comparisons"; clearReport();
			suspendedPosition=null; position=new java.awt.Point();
		}
		// A laid-out pause clamps the viewport. Preserve the last usable offset only once.
		else if(paused && availableView && suspendedPosition==null) { suspendedPosition=position; }
		else if(available && !paused && suspendedPosition!=null)
		{ position=suspendedPosition; }
		availableView=available && !paused;
		visibleScope=stats.scope;
		if(available && !paused)
		{
			if(adviceInspection!=null && (!retained(stats,adviceInspection.summary)
				|| !coherent(stats.record(adviceInspection.summary),adviceInspection))) { adviceInspection=null; }
			if(inspected!=null && !retained(stats,inspected)) { inspected=null; inspectedCalculation=null; }
			if(inspectedOwned!=null && !retained(stats,inspectedOwned.summary)) { inspectedOwned=null; scenarioPanel=null; }
			if(contextTarget!=null && !retained(stats,contextTarget)) { contextTarget=null; contextEditor=null; if(team!=null) { team.expand(false); } }
			if(diagnosticSummary!=null && !retained(stats,diagnosticSummary)) { diagnosticSummary=null; diagnosticOwned=null; diagnosticRecord=null; clearReport(); }
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
		if(!available || paused) { clearReport(); }
		Map<Integer,String> names=new HashMap<>(catalog==null?Map.of():catalog.itemNames());
		if(available && !paused && stats.last!=null) { stats.last.timeline.forEach(sample->names.putAll(sample.state.itemNames)); }
		if(available && !paused && inspected!=null) { inspected.timeline.forEach(sample->names.putAll(sample.state.itemNames)); }
		displayNames=Map.copyOf(names);
		Runnable redraw=()->render(status,account,prefs,catalog,stats,enabled,actions,storage,owned,active,tracking);
		java.util.function.BooleanSupplier valid=()->view==currentView && available && !paused && actions.current();
		java.util.function.BooleanSupplier contextValid=()->view==currentView && available && !paused && actions.contextCurrent();
		if(summaryBody==null)
		{
			setLayout(new BoxLayout(this,BoxLayout.Y_AXIS)); setBackground(ColorScheme.DARK_GRAY_COLOR);
			setBorder(BorderFactory.createEmptyBorder(3,2,4,2));
			summaryBody=PanelWidgets.column(this); add(summaryBody);
			compare=new CollapsibleSection(this,"Compare setups","nex-section-compare",true);
			team=new CollapsibleSection(this,"Team & method","nex-section-team",false);
			diagnostic=new CollapsibleSection(this,"Diagnostics","nex-section-diagnostics",false);
			add(compare); add(team);
			controlsBody=PanelWidgets.column(this); add(controlsBody); add(diagnostic);
		}
		for(var section:List.of(compare,team,diagnostic))
		{
			section.updateFont(getFont());
			for(var listener:section.header.getActionListeners()) { section.header.removeActionListener(listener); }
			section.header.addActionListener(e->{
				if(view!=currentView) { return; }
				if(section==diagnostic && !section.expanded() && diagnosticSummary==null && contextValid.getAsBoolean())
				{
					var selected=inspectedOwned!=null?inspectedOwned.summary:inspected!=null?inspected:team.expanded() && contextTarget!=null?contextTarget:stats.last!=null?stats.last:stats.lastAttempt;
					var record=stats.record(selected);
					selectDiagnostics(selected,record==null?null:record.calculation,inspectedOwned!=null?ownedIndex:0,inspectedOwned!=null?ownedPairIndex:0,inspectedOwned==null);
				}
				section.expand(!section.expanded()); redraw.run();
			});
		}
		summaryBody.setFont(getFont()); summaryBody.removeAll(); content=summaryBody;
		PanelWidgets.centeredLabel(content,"Encounter · Nex",true,2,ColorScheme.TEXT_COLOR);
		text(tracking.label); content.getComponent(content.getComponentCount()-1).setName("nex-tracking-status");
		trackingProgress=null; trackingCapture=-1;
		if(tracking==TrackingStatus.ACTIVE && active && progress!=null)
		{
			trackingCapture=progress.key;
			trackingProgress=PanelWidgets.centeredLabel(content,progressLabel(progress),false,0,PanelWidgets.BENEFIT);
			trackingProgress.setName("nex-tracking-progress");
		}
		if(available && !paused)
		{
			if(!active && stats.attempts==0 && stats.resetReason!=null) { text("Session cleared"); }
			if(stats.last!=null)
			{
				JPanel kill=PanelWidgets.card(content,"nex-kill-card");
				PanelWidgets.centeredLabel(kill,"Last kill",true,0,PanelWidgets.MUTED).getAccessibleContext().setAccessibleName("Last complete kill");
				boolean enlarged=getFont().getSize()>=18;
				JPanel metrics=PanelWidgets.column(kill); kill.add(metrics);
				killMetric(metrics,String.format(Locale.ROOT,"%.1f s",stats.last.seconds()),enlarged?"Time":"Duration","nex-duration",enlarged);
				killMetric(metrics,"Unverified","Damage contribution","nex-share",enlarged);
				if(stats.n>1) { PanelWidgets.text(kill,aggregateLabel(stats)); }
				if(inspectedOwned==null && inspected==null) { context(stats.record(stats.last),redraw,contextValid); }
			}
		}
		compare.body.removeAll(); content=compare.body;
		if(!available || paused) { text(tracking.label); }
		else if(inspectedOwned!=null)
		{
			back(redraw,contextValid);
			var record=stats.record(inspectedOwned.summary); context(record,redraw,contextValid);
			if(record!=null && record.revision!=inspectedOwned.contextRevision) { text("Recalculating…"); }
			else { ownedDetails(inspectedOwned,catalog,contextValid); }
		}
		else if(inspected!=null) { back(redraw,contextValid); context(stats.record(inspected),redraw,contextValid); details(inspected,catalog,contextValid); }
		else
		{
			if(stats.lastAttempt!=null && stats.lastAttempt!=stats.last)
			{ text("Incomplete attempt"); button(content,"Last attempt details",()->{ if(contextValid.getAsBoolean()) { openDetails(stats.lastAttempt,null); redraw.run(); } }); }
			if(stats.last!=null)
			{
				if(adviceInspection!=null)
				{
					if(adviceInspection.summary!=stats.last)
					{
						text("Comparing previous kill");
						button(content,"Latest kill",()->{ if(contextValid.getAsBoolean()) { adviceInspection=null; adviceSelection=-1; redraw.run(); } });
					}
					fangAdvice(adviceInspection,catalog,contextValid,redraw);
				}
				else if(!coherent(stats.record(stats.last),owned)) { text("Recalculating…"); }
				else
				{
					fangAdvice(owned,catalog,contextValid,redraw);
				}
				JPanel links=new ActionLinks(); links.setFont(getFont()); content.add(links);
				if(coherent(stats.record(stats.last),owned))
				{
					JButton comparisons=button(links,"Base-stat comparisons",()->{ if(contextValid.getAsBoolean()) { openOwned(owned,0,0); redraw.run(); } });
					comparisons.setName("nex-owned");
				}
				JButton details=button(links,"Kill details",()->{
					if(contextValid.getAsBoolean()) { openDetails(stats.last,owned!=null && owned.summary==stats.last?owned:null); redraw.run(); }
				}); details.setName("nex-details"); details.setToolTipText("Kill and calculation details");
			}
			else { text("No complete kill yet"); }
		}
		content=team.body; clearBody(content,contextEditor);
		if(!available || paused) { if(contextEditor!=null) { content.remove(contextEditor); } text(tracking.label); }
		else
		{
			if(contextTarget==null && !editingDefaults && team.expanded())
			{
				contextTarget=inspectedOwned!=null?inspectedOwned.summary:inspected!=null?inspected:stats.last;
				if(contextTarget==null) { editingDefaults=true; }
			}
			var record=stats.record(contextTarget);
			long key=record==null?-1:record.summary.key, revision=record==null?-1:record.revision;
			if(contextEditor==null || contextEditor.key!=key || contextEditor.revision!=revision
				|| record==null && !contextEditor.initial.equals(prefs.nexContext))
			{ if(contextEditor!=null) { content.remove(contextEditor); } contextEditor=new NexContextEditor(key,revision,record==null?prefs.nexContext:record.context,getFont()); }
			text(record==null?"Default team & method":String.format(Locale.ROOT,"Selected kill · %.1f s",record.summary.seconds()));
			contextEditor.bind((context,remember)->{ if(contextValid.getAsBoolean()) { actions.context(key,revision,context,remember); } },actions.canEdit());
			if(contextEditor.getParent()!=content) { content.add(contextEditor); } else { content.setComponentZOrder(contextEditor,content.getComponentCount()-1); }
			button(content,record==null && stats.last!=null?"Edit last kill":"Default team & method",()->{
				if(contextValid.getAsBoolean()) { editingDefaults=record!=null || stats.last==null; contextTarget=editingDefaults?null:stats.last; contextEditor=null; redraw.run(); }
			});
			storageStatus(storage);
		}
		content=controlsBody; content.setFont(getFont()); content.removeAll();
		if(available)
		{
			JPanel controls=new ActionLinks(); controls.setFont(getFont()); content.add(controls);
			button(controls,enabled?"Kill tracking: On":"Kill tracking: Off",()->{ if(valid.getAsBoolean()) { actions.enable(!enabled); } }).setToolTipText(enabled?"Turn off kill tracking":"Turn on kill tracking");
			button(controls,"Clear session",()->{ if(view==currentView && actions.current()) { actions.reset(); } }).setName("nex-reset");
		}
		content=diagnostic.body; clearBody(content,reportScroll);
		if(diagnostic.expanded() && available && !paused)
		{
			if(diagnosticSummary==null) { diagnosticSummary=stats.last!=null?stats.last:stats.lastAttempt; diagnosticDefaultPending=true; }
			diagnosticRecord=stats.record(diagnosticSummary);
			if(diagnosticRecord!=null && diagnosticRecord.calculation!=null) { diagnosticOwned=diagnosticRecord.calculation; }
			else if(diagnosticOwned==null && owned!=null && owned.summary==diagnosticSummary) { diagnosticOwned=owned; }
			if(diagnosticOwned!=null && !coherent(diagnosticRecord,diagnosticOwned)) { diagnosticOwned=null; }
			selectDiagnosticDefault();
		}
		if(diagnosticFangResult!=diagnosticOwned) { diagnosticFangResult=null; }
		NexReport.State state=paused?NexReport.State.PAUSED:account==null?NexReport.State.NO_ACCOUNT:!available?NexReport.State.NOT_READY:
			stats.attempts==0 && stats.resetReason!=null?NexReport.State.RESET:diagnosticSummary==null?NexReport.State.NO_KILL:
			diagnosticOwned==null && diagnosticSummary.outcome==com.ironpath.encounter.EncounterSummary.Outcome.COMPLETE?NexReport.State.PENDING:NexReport.State.READY;
		diagnostics(status,stats,prefs,catalog,state,()->view==currentView && (actions==null || actions.contextCurrent()));
		content=this; updateDiagnostics=()->{ if(view==currentView) { redraw.run(); } }; revalidate(); repaint();
		java.awt.Point restorePosition=position;
		if(viewport!=null && restorePosition!=null) { SwingUtilities.invokeLater(()->{
			if(view!=currentView || viewport.getView()!=this) { return; }
			viewport.setViewPosition(new java.awt.Point(restorePosition.x,Math.min(restorePosition.y,Math.max(0,getHeight()-viewport.getExtentSize().height))));
			if(availableView) { suspendedPosition=null; }
		}); }
	}
	private static void clearBody(JPanel body,JComponent retained)
	{ for(var c:body.getComponents()) { if(c!=retained) { body.remove(c); } } }
	void updateTrackingProgress(com.ironpath.nex.NexCapture.Progress progress)
	{
		if(!SwingUtilities.isEventDispatchThread()) { throw new IllegalStateException("Swing thread required"); }
		if(trackingProgress==null || progress==null || trackingCapture!=progress.key) { return; }
		String label=progressLabel(progress);
		if(!label.equals(trackingProgress.getText())) { trackingProgress.setText(label); revalidate(); repaint(); }
	}
	private static String progressLabel(com.ironpath.nex.NexCapture.Progress progress)
	{
		String scope=!progress.smokeObserved?"Start unconfirmed":progress.phase.equals("UNKNOWN")?"Phase unknown":
			progress.phase.substring(0,1)+progress.phase.substring(1).toLowerCase(Locale.ROOT);
		return String.format(Locale.ROOT,"%.1f s · %s",progress.elapsedTicks*0.6,scope);
	}
	private void selectDiagnostics(NexSummary summary,NexOwnedResult result,int choice,int pair,boolean pending)
	{
		boolean changed=diagnosticSummary!=summary || diagnosticOwned!=result || diagnosticChoice!=choice || diagnosticPair!=pair;
		if(changed) { clearReport(); diagnosticFangResult=null; }
		if(diagnosticSummary!=summary) { diagnosticRecord=null; }
		diagnosticSummary=summary; diagnosticOwned=result; diagnosticChoice=choice; diagnosticPair=pair; diagnosticDefaultPending=pending;
		if(changed && updateDiagnostics!=null) { SwingUtilities.invokeLater(updateDiagnostics); }
	}
	private void heading(String value)
	{ PanelWidgets.centeredLabel(content,value,true,0,ColorScheme.TEXT_COLOR); }
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
		JPanel metric=PanelWidgets.column(parent);
		PanelWidgets.centeredLabel(metric,value,true,enlarged?4:7,ColorScheme.TEXT_COLOR).setName(name);
		if(enlarged && caption.equals("Damage contribution"))
		{ PanelWidgets.centeredLabel(metric,"Damage",false,0,PanelWidgets.MUTED); PanelWidgets.centeredLabel(metric,"contribution",false,0,PanelWidgets.MUTED); }
		else { PanelWidgets.centeredLabel(metric,caption,false,0,PanelWidgets.MUTED); }
		metric.getAccessibleContext().setAccessibleName(caption+": "+value);
		parent.add(metric);
	}
	private void fangAdvice(NexOwnedResult result,KnowledgeCatalog catalog,java.util.function.BooleanSupplier valid,Runnable redraw)
	{
		var advice=result.advice;
		if(adviceSource!=result.summary)
		{ adviceSource=result.summary; adviceSelection=-1; adviceGroup=0; manualAdvice.clear(); }
		PanelWidgets.centeredLabel(content,"Combat role",false,0,PanelWidgets.MUTED);
		JComboBox<String> role=new JComboBox<>(new String[]{"Melee","Ranged","Special attacks"});
		role.setName("nex-combat-role"); role.setSelectedItem(adviceRole); role.setFont(getFont()); addControl(role);
		role.addActionListener(e->{ if(valid.getAsBoolean()) { adviceRole=(String)role.getSelectedItem(); redraw.run(); } });
		PanelWidgets.centeredLabel(content,"Recommended upgrades",true,0,ColorScheme.TEXT_COLOR);
		if(!adviceRole.equals("Melee"))
		{
			text("No supported attack sample");
			return;
		}
		if(advice.rows.isEmpty())
		{
			text("No supported melee sample");
			text(advice.blockers.contains(com.ironpath.advisor.NexFangAdvice.Gap.TRUNCATED)?"Action evidence truncated":
				advice.excluded.isEmpty()?"No ordinary attacks captured":fangGap(advice.excluded.values().iterator().next()));
			return;
		}
		List<com.ironpath.nex.NexSummary.Sample> groups=new ArrayList<>();
		for(var row:advice.rows) { if(!groups.contains(row.sample)) { groups.add(row.sample); } }
		adviceGroup=Math.min(adviceGroup,groups.size()-1);
		if(groups.size()>1)
		{
			PanelWidgets.centeredLabel(content,"Comparison setup",false,0,PanelWidgets.MUTED);
			JComboBox<String> group=new JComboBox<>(); group.setName("nex-advice-context"); group.setFont(getFont());
			for(var sample:groups) { group.addItem(sample.phase+" · tick "+sample.tick); }
			group.setSelectedIndex(adviceGroup); addControl(group);
			group.addActionListener(e->{ if(valid.getAsBoolean()) { adviceGroup=group.getSelectedIndex(); adviceSelection=-1; redraw.run(); } });
		}
		var sample=groups.get(adviceGroup);
		Set<Integer> suggested=CANDIDATES.suggestions("nex","nex-trio-mp2-one-kill",com.ironpath.advisor.EncounterCandidates.Role.MELEE,manualAdvice);
		JCheckBox all=new JCheckBox("Include sidegrades & downgrades",adviceAll); all.setFont(getFont()); all.setName("nex-advice-all");
		// Wrapping native checkbox text is handled by a narrow-font-safe separate label.
		all.setText("All alternatives"); all.setToolTipText("Include defensive alternatives, sidegrades and downgrades"); addControl(all);
		all.addActionListener(e->{ if(valid.getAsBoolean()) { adviceAll=all.isSelected(); redraw.run(); } });
		for(int i=0;i<advice.rows.size();i++)
		{
			var row=advice.rows.get(i); if(row.sample!=sample) { continue; }
			var changed=NexPresentation.changedItems(row.pair); if(changed.isEmpty() && i!=adviceSelection) { continue; }
			boolean observed=row.kind!=com.ironpath.advisor.NexFangAdvice.Kind.UPGRADE;
			if(!observed && !changed.values().stream().allMatch(suggested::contains)) { continue; }
			boolean defensive=CANDIDATES.items.stream().anyMatch(item->item.defensive && changed.containsValue(item.id));
			if(!adviceAll && !defensive && row.gain.lower<=1e-9 && i!=adviceSelection) { continue; }
			int index=i; JPanel card=PanelWidgets.card(content,"nex-advice-row-"+i);
			String title=changed.size()==1?name(changed.values().iterator().next(),catalog):changed.isEmpty()?"Current setup":changed.size()+"-item package";
			if(changed.size()==1) { item(card,changed.values().iterator().next(),title,valid); }
			else { PanelWidgets.centeredLabel(card,title,true,0,ColorScheme.TEXT_COLOR); }
			PanelWidgets.centeredLabel(card,row.headline(),true,0,ColorScheme.TEXT_COLOR);
			JButton open=button(card,adviceSelection==i?"Close comparison ▴":"Open comparison ▾",()->{
				if(valid.getAsBoolean())
				{ adviceSelection=adviceSelection==index?-1:index; adviceInspection=adviceSelection<0?null:result; redraw.run(); }
			}); open.setName("nex-advice-open-"+i);
			if(adviceSelection!=i) { continue; }
			if(changed.size()==1)
			{
				var change=changed.entrySet().iterator().next();
				PanelWidgets.centeredLabel(card,"Replaces "+name(row.pair.beforeSetup.slots.get(change.getKey()),catalog),false,0,PanelWidgets.MUTED).setName("nex-fang-replaces");
			}
			else
			{
				for(var change:changed.entrySet()) { PanelWidgets.text(card,name(row.pair.beforeSetup.slots.get(change.getKey()),catalog)+" → "+name(change.getValue(),catalog)); }
			}
			PanelWidgets.centeredLabel(card,row.headline(),true,2,ColorScheme.TEXT_COLOR).setName("nex-fang-gain");
			PanelWidgets.centeredLabel(card,"Max hit "+row.pair.before.maxHit+" → "+row.pair.after.maxHit,false,0,PanelWidgets.MUTED).setName("nex-fang-max-hit");
			PanelWidgets.centeredLabel(card,"Estimated contribution",false,0,PanelWidgets.MUTED);
			PanelWidgets.centeredLabel(card,"Not estimated",true,0,ColorScheme.TEXT_COLOR);
			PanelWidgets.centeredLabel(card,"Estimated team time saved",false,0,PanelWidgets.MUTED);
			PanelWidgets.centeredLabel(card,"Not estimated",true,0,ColorScheme.TEXT_COLOR);
			PanelWidgets.centeredLabel(card,"Supplies: Not estimated",false,0,PanelWidgets.MUTED);
			JButton details=button(card,"How this was compared",()->{
				if(!valid.getAsBoolean()) { return; }
				selectDiagnostics(result.summary,result,0,0,false); diagnostic.expand(true);
				diagnosticFangResult=result; diagnosticFangSelection=index;
				calculationDetailsSummary=result.summary; calculationDetailsExpanded=true; redraw.run();
			}); details.setName("nex-fang-details");
		}
		PanelWidgets.centeredLabel(content,"Add comparison item",false,0,PanelWidgets.MUTED);
		JComboBox<String> manual=new JComboBox<>(); manual.setName("nex-fang-alternative"); manual.setFont(getFont());
		for(int i=0;i<advice.rows.size();i++)
		{
			var row=advice.rows.get(i); var changed=NexPresentation.changedItems(row.pair);
			manual.addItem((i+1)+" · "+(changed.size()==1?name(changed.values().iterator().next(),catalog):changed.isEmpty()?"Current setup":changed.size()+"-item package")+" · "+row.sample.phase+" "+row.sample.tick);
		}
		addControl(manual);
		button(content,"Add item",()->{
			if(!valid.getAsBoolean()) { return; }
			adviceInspection=result; adviceSelection=manual.getSelectedIndex(); var row=advice.rows.get(adviceSelection);
			manualAdvice.addAll(NexPresentation.changedItems(row.pair).values()); adviceGroup=groups.indexOf(row.sample); redraw.run();
		}).setName("nex-advice-add");
	}

	private static String roleLabel(String role)
	{
		switch(role) { case "melee": return "Melee"; case "ranged": return "Ranged";
			case "magic": return "Magic"; case "spec candidate": case "spec": return "Special attacks"; default: return "Unknown"; }
	}
	private static String fangGap(com.ironpath.advisor.NexFangAdvice.Gap gap)
	{
		switch(gap)
		{
			case UNKNOWN_PHASE: return "Nex phase unknown";
			case UNSUPPORTED_TARGET: return "Nex target form unsupported";
			case UNSUPPORTED_WINDOW: return "Ordinary Nex window unknown";
			case PACKAGE_INPUTS: return "Gear or equip requirements unavailable";
			case AMBIGUOUS_ATTACK: return "Melee attack evidence ambiguous";
			case WORK_LIMIT: return "Comparison work limit reached";
			default: return "No melee sample within the complete kill";
		}
	}
	private void context(com.ironpath.nex.NexRecord record,Runnable redraw,java.util.function.BooleanSupplier valid)
	{
		if(record==null) { return; }
		JPanel surface=new JPanel(new java.awt.BorderLayout(5,0)) {
			@Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE,getPreferredSize().height); }
		}; surface.setFont(getFont()); surface.setOpaque(false); surface.setAlignmentX(LEFT_ALIGNMENT);
		surface.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,PanelWidgets.LINE),BorderFactory.createEmptyBorder(2,2,6,2)));
		surface.setName("nex-context-summary"); content.add(surface);
		JPanel summary=PanelWidgets.column(surface); surface.add(summary,java.awt.BorderLayout.CENTER);
		PanelWidgets.centeredLabel(summary,NexPresentation.contextValue(record.context),false,0,ColorScheme.TEXT_COLOR);
		PanelWidgets.centeredLabel(summary,record.context.confirmed()?"Confirmed":"Unconfirmed",false,0,PanelWidgets.MUTED);
		if(record.summary.outcome!=com.ironpath.encounter.EncounterSummary.Outcome.COMPLETE) { return; }
		JPanel action=PanelWidgets.column(surface); surface.add(action,java.awt.BorderLayout.EAST);
		JButton edit=button(action,"Edit",()->{
			if(!valid.getAsBoolean()) { return; }
			contextTarget=record.summary; contextEditor=null; editingDefaults=false; team.expand(true);
			if(diagnosticSummary!=record.summary) { selectDiagnostics(record.summary,record.calculation,0,0,true); }
			redraw.run();
		}); edit.setName("nex-edit-context"); edit.setToolTipText("Edit team & method");
		action.setPreferredSize(new Dimension(edit.getPreferredSize().width,edit.getPreferredSize().height));
		if(!record.context.pilot() && content==diagnostic.body)
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
		selectDiagnostics(owned.summary,owned,index,pair,false);
	}
	private void openDetails(NexSummary summary,NexOwnedResult result)
	{
		rememberPosition(); inspected=summary; inspectedCalculation=result;
		selectDiagnostics(summary,result,0,0,true);
	}
	private void back(Runnable redraw,java.util.function.BooleanSupplier valid)
	{
		button(content,"Back to encounter",()->{
			if(!valid.getAsBoolean()) { return; } inspected=null; inspectedOwned=null; inspectedCalculation=null; nextPosition=returnPosition; redraw.run();
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
	private void diagnostics(String status,NexSession.Stats stats,Preferences prefs,KnowledgeCatalog catalog,NexReport.State state,java.util.function.BooleanSupplier valid)
	{

		PanelWidgets.label(content,"Build "+BuildInfo.VERSION,false,0,PanelWidgets.MUTED);
		boolean hidden=state==NexReport.State.PAUSED || state==NexReport.State.NO_ACCOUNT || state==NexReport.State.NOT_READY || state==NexReport.State.RESET;
		text(status);
		if(!hidden) { text(stats.complete+" complete · "+stats.failed+" failed · "+stats.censored+" excluded"); }
		prefs.confirmations.forEach((key,value)->text((catalog==null?key:catalog.factChoices().getOrDefault(key,key))+": "+value.evidence()));
		if(stats.resetReason!=null) { text("Previous session cleared: "+stats.resetReason); }
		NexSummary summary=hidden?null:diagnosticSummary; NexOwnedResult result=hidden?null:diagnosticOwned;
		if(summary!=null) { text("Frozen kill #"+summary.key+" · "+summary.outcome); }
		else { text("No visible kill evidence"); }
		if(!hidden && summary!=observationSummary) { observationSummary=summary; observationPage=0; }
		String report=NexReport.snapshot(summary,result,diagnosticChoice,diagnosticPair,catalog,provenance,state,hidden?null:diagnosticRecord,observationPage).format();
		if(reportArea!=null && !report.equals(reportArea.getText())) { clearReport(); }
		PanelWidgets.text(content,reportFeedback); final JTextArea feedback=(JTextArea)content.getComponent(content.getComponentCount()-1);
		button(content,"Copy report now",()->{
			if(!valid.getAsBoolean() || reportArea==null) { return; }
			reportArea.setText(report); reportArea.setCaretPosition(0); reportArea.getParent().getParent().setVisible(true);
			try { clipboard.copy(report); reportFeedback="Report copied. Selectable text is below."; }
			catch(java.awt.HeadlessException | IllegalStateException | SecurityException ex)
			{ reportFeedback="Clipboard unavailable or busy. Select the report below and copy it manually."; }
			feedback.setText(reportFeedback);
			revalidate(); repaint();
		}).setName("nex-copy-report");
		JPanel inputs=PanelWidgets.card(content,"nex-diagnostic-inputs");
		PanelWidgets.label(inputs,"Inputs",true,0,ColorScheme.TEXT_COLOR);
		PanelWidgets.text(inputs,"Nex · "+(provenance==NexReport.Provenance.SYNTHETIC_OFFLINE?"synthetic fixture":"public client observations"));
		PanelWidgets.text(inputs,summary==null?"No visible kill evidence":"Captured gear, levels & prayers");
		PanelWidgets.label(inputs,"Base target stats and planning locks are assumptions.",false,0,PanelWidgets.MUTED);
		JPanel calculations=PanelWidgets.card(content,"nex-diagnostic-calculations");
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
		JPanel missing=PanelWidgets.card(content,"nex-diagnostic-gaps");
		PanelWidgets.label(missing,"Missing / limited",true,0,ColorScheme.TEXT_COLOR);
		if(result!=null)
		{
			result.gaps.entrySet().stream().sorted(Map.Entry.comparingByKey()).limit(8).forEach(e->PanelWidgets.text(missing,e.getValue()+" · "+NexReport.gapLabel(e.getKey())));
			if(result.gaps.size()>8) { PanelWidgets.text(missing,"More gaps remain in comparison details."); }
		}
		PanelWidgets.text(missing,"Team-time estimates unavailable.");
		if(summary!=calculationDetailsSummary) { calculationDetailsSummary=summary; calculationDetailsExpanded=false; }
		JPanel basis=ownedBody(); basis.setName("nex-calculation-details"); basis.setVisible(calculationDetailsExpanded);
		button(content,"Calculation details",()->{ if(valid.getAsBoolean()) { calculationDetailsExpanded=!calculationDetailsExpanded; basis.setVisible(calculationDetailsExpanded); revalidate(); repaint(); } });
		PanelWidgets.text(basis,"IronPath · build "+BuildInfo.VERSION);
		PanelWidgets.text(basis,"Adapter "+com.ironpath.nex.NexCapture.VERSION+" · evaluator "+com.ironpath.advisor.NexOwnedEvaluator.VERSION);
		PanelWidgets.text(basis,"Knowledge "+(catalog==null?"unavailable":catalog.version));
		if(result!=null)
		{
			PanelWidgets.text(basis,"Fang preview: "+result.advice.included.size()+" included attacks; "+result.advice.excluded.size()+" excluded candidates; "+result.advice.ambiguousImpacts+" ambiguous impacts; "+result.advice.zeroImpacts+" recorded zero impacts. Impact amounts never select the attack sample.");
			PanelWidgets.text(basis,"Attack mappings/windows and base target stats remain unvalidated assumptions. Same weapon/style/5-tick tempo. Damage includes accuracy and the full ordinary distribution without remaining-HP clipping. Independent alternatives; bank/hypothetical gear needs next-trip packing. See fangAdvice in Copy report for every pair and excluded action.");
			if(diagnosticFangResult==result && diagnosticFangSelection<result.advice.rows.size())
			{
				var selected=result.advice.rows.get(diagnosticFangSelection);
				PanelWidgets.text(basis,"Selected Fang alternative "+(diagnosticFangSelection+1)+" · "+selected.kind+" · "+selected.sample.phase+" / "+selected.sample.window
					+" · "+selected.actionIndices.size()+" attacks: "+selected.headline()+". Min/max/accuracy before="+selected.pair.before.minHit+"/"+selected.pair.before.maxHit+"/"+selected.pair.before.hitChance
					+"; after="+selected.pair.after.minHit+"/"+selected.pair.after.maxHit+"/"+selected.pair.after.hitChance+". Baseline is the actually observed equipment for every alternative in this sample. Whole-encounter damage weighting and verified contribution are unavailable: sample damage is not whole-kill gain. Supplies require incoming damage and consumption support. Constant-throughput estimates use s*(1+g)/(1+s*g), T/(1+s*g), with unchanged teammates, damage pool and behaviour. Role gain must first be weighted into overall gain. Fixed waits can reduce time savings. Acquisition: "+selected.work.reason+". Ordering uses diminishing damage benefit minus a fixed remaining-work penalty; no objective-best claim.");
			}
		}
		PanelWidgets.text(basis,"Pilot planning scope: Trio MP2 · one kill per trip.");
		PanelWidgets.text(basis,"Default team & method: "+prefs.nexContext.label()+"\n"+prefs.nexContext.provenance());
		if(diagnosticRecord!=null && !hidden) { PanelWidgets.text(basis,diagnosticRecord.context.provenance()); }
		NexHelp.populate(basis);
		PanelWidgets.text(basis,"Timing: smoke → death/dying. Damage contribution unavailable: whole-kill coverage unverified.");
		if(summary!=null)
		{
			PanelWidgets.text(basis,"Recorded damage: "+summary.localDamage()+" local / "+summary.totalDamage()+" total; observed hitsplat ratio "+(summary.share()==null?"unavailable":String.format(Locale.ROOT,"%.2f%%",summary.share()))+". Nex + named minions only; healing/unclassified excluded. Whole-kill coverage unverified.");
			PanelWidgets.text(basis,"State coverage "+Math.min(summary.sampledTicks,summary.expectedTicks)+" / "+summary.expectedTicks+" ticks · "+summary.unclassifiedHits+" unclassified hits");
			if(summary==stats.last)
			{
				PanelWidgets.text(basis,aggregateLabel(stats));
				if(stats.n==0) { PanelWidgets.text(basis,stats.record(summary).exclusion()); }
				if(stats.n>1) { PanelWidgets.text(basis,String.format(Locale.ROOT,"Range %.1f–%.1f s",stats.minSeconds,stats.maxSeconds)); }
				if(stats.shareN>1) { PanelWidgets.text(basis,String.format(Locale.ROOT,"Mean recorded hitsplat ratio %.1f%% · %d kills; aggregate %.1f%%",stats.meanShare,stats.shareN,stats.aggregateShare)); }
				PanelWidgets.text(basis,"Matched averages require confirmed strategy/group/count, compatible timer/adapter, worn packages, sampled levels/prayers and a stable observed arena roster. Planning locks are excluded. Equal count is not equal teammate damage; visible people do not prove party membership.");
			}
			if(result!=null)
			{
				PanelWidgets.text(basis,"Automatic observation · "+com.ironpath.nex.NexObservations.VERSION+"\n"
					+summary.observations.launches.size()+" pilot animation candidates · "+summary.observations.flights.size()+" distinct projectiles · "+summary.observations.impacts.size()+" own hits (including zero)");
				PanelWidgets.text(basis,"HP: "+result.observations.hudReadings+" fresh HUD candidate states · "+result.observations.barReadings+" bounded bar states. Nex HUD identity/timing and launch mappings await client checks. Matches remain candidates; impacts never supply launch equipment.");
				PanelWidgets.text(basis,"Smoke-callback resources: "+(result.observations.startResources==null?"unknown":"captured")+" · server-agreed energy "+result.observations.startEnergy+" (tenths of percent). Starting stock is separate from consumption; readiness, restoration phase/order and inherited charge lifetime remain unknown.");
				PanelWidgets.text(basis,"Starting food units / potion doses: "+result.observations.startSupplies+" (null unknown). Consumption remains unconfirmed.");
				if(result.observations.correlationLimited) { PanelWidgets.text(basis,"Correlation work limit reached; candidate matches are incomplete."); }
				if(summary.observations.truncated) { PanelWidgets.text(basis,"Observation buffer full; later observations omitted."); }
				PanelWidgets.label(basis,"Observation events",true,0,ColorScheme.TEXT_COLOR);
				PanelWidgets.text(basis,"Copy report includes all retained observation evidence as JSON. Event windows are optional details.");
				var window=result.observations.window;
				JComboBox<String> eventPage=new JComboBox<>(); eventPage.setName("nex-observation-page");
				for(int i=0;i<window.pages();i++) { eventPage.addItem("Events "+(i+1)+" / "+window.pages()); }
				eventPage.setSelectedIndex(Math.min(observationPage,window.pages()-1));
				eventPage.setFont(getFont()); eventPage.setAlignmentX(LEFT_ALIGNMENT); eventPage.setMaximumSize(new Dimension(PANEL_WIDTH-12,eventPage.getPreferredSize().height));
				eventPage.getAccessibleContext().setAccessibleName("Observation event window"); basis.add(eventPage);
				PanelWidgets.text(basis,String.join("\n\n",window.page(observationPage)));
				JTextArea events=(JTextArea)basis.getComponent(basis.getComponentCount()-1); events.setName("nex-observation-events");
				events.setEditable(false); events.setFocusable(true); events.getAccessibleContext().setAccessibleName("Sanitized observation events");
				eventPage.addActionListener(e->{
					if(!valid.getAsBoolean()) { return; }
					observationPage=eventPage.getSelectedIndex();
					if(updateDiagnostics!=null) { updateDiagnostics.run(); }
				});
				long carried=result.choices.stream().filter(c->c.supported() && !c.bankPreparation).count();
				long bank=result.choices.stream().filter(c->c.supported() && c.bankPreparation).count();
				PanelWidgets.text(basis,"Supported scenarios: carried "+carried+", bank preparation "+bank+". Unavailable: "+(result.choices.size()-carried-bank)+". Counts are not fight coverage.");
				if(diagnosticChoice>=0 && diagnosticChoice<result.choices.size())
				{
					var c=result.choices.get(diagnosticChoice);
					PanelWidgets.text(basis,"Selected "+c.actual.role+" · "+c.actual.phase+" · tick "+c.actual.tick+" · pair "+diagnosticPair);
					PanelWidgets.text(basis,"Matching pilot launch-state candidates: "+com.ironpath.nex.NexObservationReview.launches(summary,c.actual).size()
						+". This result supports "+(c.supported()?"conditional state offence":"no dependent damage calculation")+"; it does not establish every legal or missed attack opportunity.");
				}
				PanelWidgets.text(basis,"Independent state comparisons; no whole-kill ranking. Captured gear, levels and prayer readings are separate from the assumed base-stat target and frozen planning locks. No launch attribution or team-time estimate.");
				result.gaps.forEach((gap,count)->PanelWidgets.text(basis,count+" scenarios · "+gap));
				if(diagnosticChoice>=0 && diagnosticChoice<result.choices.size())
				{
					var choice=result.choices.get(diagnosticChoice);
					choice.coverage.forEach(gap->PanelWidgets.text(basis,gap));
					List<ComparisonResult> pairs=new ArrayList<>(); if(choice.comparison!=null) { pairs.add(choice.comparison); } pairs.addAll(choice.alternatives);
					if(diagnosticPair>=0 && diagnosticPair<pairs.size()) { pairs.get(diagnosticPair).assumptions.forEach(assumption->PanelWidgets.text(basis,assumption)); }
				}
				NexResourcesPanel.populate(basis,result.resources,valid,()->{ revalidate(); repaint(); },true);
			}
			details(basis,summary,catalog,valid,true);
		}
		content.add(basis);
		button(content,"Show selectable report",()->{ if(valid.getAsBoolean() && reportArea!=null) { reportArea.getParent().getParent().setVisible(!reportArea.getParent().getParent().isVisible()); revalidate(); repaint(); } });
		if(reportArea==null)
		{
			reportArea=new JTextArea(report) {
				@Override public void scrollRectToVisible(java.awt.Rectangle bounds) { if(isFocusOwner()) { super.scrollRectToVisible(bounds); } }
			}; reportArea.setName("nex-report-text"); reportArea.setEditable(false); reportArea.setLineWrap(true); reportArea.setWrapStyleWord(true); reportArea.setFont(getFont());
			reportArea.getAccessibleContext().setAccessibleName("Sanitized diagnostic report"); reportArea.setCaretPosition(0);
			reportScroll=new JScrollPane(reportArea); reportScroll.setAlignmentX(LEFT_ALIGNMENT);
			reportScroll.setPreferredSize(new Dimension(PANEL_WIDTH-12,180)); reportScroll.setMaximumSize(new Dimension(PANEL_WIDTH-12,180)); reportScroll.setVisible(false); content.add(reportScroll);
		}
		else { content.setComponentZOrder(reportScroll,content.getComponentCount()-1); }
		text("Names, account IDs, roster hashes, raw traces and inventory/bank dumps are omitted. Nothing is uploaded. This report is not a replay.");
	}
	private void clearReport()
	{
		if(reportArea!=null) { reportArea.setText(""); }
		if(reportScroll!=null && reportScroll.getParent()!=null) { reportScroll.getParent().remove(reportScroll); }
		reportArea=null; reportScroll=null; reportFeedback="";
	}

	private void ownedDetails(NexOwnedResult result,KnowledgeCatalog catalog,java.util.function.BooleanSupplier valid)
	{
		heading("Base-stat comparisons");
		text("Owned gear · kill #"+result.summary.key);
		JPanel resources=ownedBody(); resources.setVisible(false);
		button(content,"Spec energy & Death Charge",()->{
			if(valid.getAsBoolean()) { resources.setVisible(!resources.isVisible()); revalidate(); repaint(); }
		});
		NexResourcesPanel.populate(resources,result.resources,valid,()->{ revalidate(); repaint(); }); content.add(resources);
		if(result.choices.isEmpty()) { text("No complete captured setup to compare."); return; }
		List<String> roles=new ArrayList<>(); roles.add("All roles");
		result.choices.stream().map(c->roleLabel(c.actual.role)).distinct().sorted().forEach(roles::add);
		JComboBox<String> role=new JComboBox<>(roles.toArray(new String[0])); role.setName("nex-owned-role-filter");
		role.setSelectedItem(ownedRoleFilter); addControl(role);
		JComboBox<String> support=new JComboBox<>(new String[]{"Supported comparisons","All states","Unavailable comparisons"});
		support.setName("nex-owned-support-filter"); support.setSelectedItem(ownedSupportFilter); addControl(support);
		JComboBox<String> states=new JComboBox<>(); states.setName("nex-owned-state");
		List<Integer> indices=new ArrayList<>(); boolean[] filling={false}; addControl(states);
		JPanel detail=ownedBody(); content.add(detail);
		Runnable show=()->{
			if(!valid.getAsBoolean() || filling[0] || states.getSelectedIndex()<0) { return; }
			int selected=indices.get(states.getSelectedIndex());
			if(ownedIndex!=selected) { ownedPairIndex=0; }
			ownedIndex=selected; selectDiagnostics(result.summary,result,ownedIndex,ownedPairIndex,false); if(scenarioPanel!=null) { scenarioPanel.detach(); } detail.removeAll();
			var c=result.choices.get(ownedIndex);
			PanelWidgets.centeredLabel(detail,c.bankPreparation?"Bank gear":"Captured gear",false,0,PanelWidgets.MUTED);
			if(!c.supported()) { PanelWidgets.text(detail,"No comparison available"); scenario(detail,result,catalog,valid); }
			else
			{
				List<ComparisonResult> pairs=new ArrayList<>(); pairs.add(c.comparison); pairs.addAll(c.alternatives);
				JComboBox<String> options=new JComboBox<>(); options.setFont(getFont()); options.setName("nex-owned-pair");
				options.addItem("Proposed gear setup");
				for(var pair:c.alternatives) { options.addItem(changes(pair,catalog)); }
				options.setAlignmentX(LEFT_ALIGNMENT); options.setSelectedIndex(Math.min(ownedPairIndex,pairs.size()-1));
				options.setMaximumSize(new Dimension(PANEL_WIDTH-12,Math.max(28,options.getPreferredSize().height))); detail.add(options);
				JPanel value=ownedBody(); detail.add(value);
				Runnable compare=()->{
					if(!valid.getAsBoolean()) { return; } ownedPairIndex=options.getSelectedIndex(); selectDiagnostics(result.summary,result,ownedIndex,ownedPairIndex,false); value.removeAll(); var pair=pairs.get(ownedPairIndex);
					options.setToolTipText(options.getSelectedItem().toString());
					options.getAccessibleContext().setAccessibleName("Gear comparison");
					PanelWidgets.centeredLabel(value,ownedPairIndex==0?"Proposed gear setup":"Owned alternative",true,0,ColorScheme.TEXT_COLOR);
					PanelWidgets.centeredLabel(value,NexOwnedResult.headline(pair),true,0,ColorScheme.TEXT_COLOR).setName("nex-base-stat-gain");
					PanelWidgets.text(value,changes(pair,catalog));
					PanelWidgets.centeredLabel(value,NexPresentation.origin(c,pair),false,0,PanelWidgets.MUTED);
					PanelWidgets.text(value,"Actual ammo slot: "+name(pair.beforeSetup.slots.get("ammo"),catalog));
					if(pair.beforeSetup.loadedAmmo>0 || pair.afterSetup.loadedAmmo>0)
					{ PanelWidgets.text(value,"Selected quiver ammo: "+(pair.beforeSetup.loadedAmmo>0?name(pair.beforeSetup.loadedAmmo,catalog):"worn "+name(pair.beforeSetup.slots.get("ammo"),catalog))
						+" → "+(pair.afterSetup.loadedAmmo>0?name(pair.afterSetup.loadedAmmo,catalog):"worn "+name(pair.afterSetup.slots.get("ammo"),catalog))+""); }
					JPanel basis=ownedBody(); basis.setVisible(false);
					button(value,"Full gear setups",()->{
						if(valid.getAsBoolean()) { basis.setVisible(!basis.isVisible()); value.invalidate(); detail.invalidate(); invalidate(); revalidate(); repaint(); }
					});
					for(String slot:com.ironpath.advisor.PoweredMagic.SLOTS) { PanelWidgets.text(basis,slot+": "+name(pair.beforeSetup.slots.get(slot),catalog)+" → "+name(pair.afterSetup.slots.get(slot),catalog)); }
					value.add(basis);
					scenario(value,result,catalog,valid);
					value.revalidate(); revalidate();
				}; options.addActionListener(e->compare.run()); compare.run();
			}
			detail.revalidate(); revalidate(); repaint();
		};
		Runnable filter=()->{
			if(!valid.getAsBoolean()) { return; }
			ownedRoleFilter=(String)role.getSelectedItem(); ownedSupportFilter=(String)support.getSelectedItem();
			filling[0]=true; indices.clear(); states.removeAllItems();
			for(int i=0;i<result.choices.size();i++)
			{
				var c=result.choices.get(i);
				if(!ownedRoleFilter.equals("All roles") && !ownedRoleFilter.equals(roleLabel(c.actual.role))) { continue; }
				if(ownedSupportFilter.equals("Supported comparisons") && !c.supported()
					|| ownedSupportFilter.equals("Unavailable comparisons") && c.supported()) { continue; }
				indices.add(i); states.addItem((i+1)+": "+roleLabel(c.actual.role)+" · tick "+c.actual.tick+" · "+c.actual.phase
					+(c.actual.phase.equals("ZAROS")?" · "+c.actual.targetState.overhead():"")+(c.bankPreparation?" · bank preparation":" · carried"));
			}
			if(!indices.isEmpty()) { states.setSelectedIndex(Math.max(0,indices.indexOf(ownedIndex))); }
			filling[0]=false; states.setEnabled(!indices.isEmpty());
			if(indices.isEmpty())
			{ ownedIndex=-1; ownedPairIndex=0; selectDiagnostics(result.summary,result,-1,0,false); if(scenarioPanel!=null) { scenarioPanel.detach(); } detail.removeAll(); PanelWidgets.text(detail,"No matching comparisons"); }
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
		return PanelWidgets.column(content);
	}
	private String changes(ComparisonResult pair,KnowledgeCatalog catalog)
	{
		List<String> changes=new ArrayList<>();
		for(String slot:com.ironpath.advisor.PoweredMagic.SLOTS)
		{ if(!Objects.equals(pair.beforeSetup.slots.get(slot),pair.afterSetup.slots.get(slot))) { changes.add(slot+": "+name(pair.beforeSetup.slots.get(slot),catalog)+" → "+name(pair.afterSetup.slots.get(slot),catalog)); } }
		if(pair.beforeSetup.loadedAmmo!=pair.afterSetup.loadedAmmo)
		{ changes.add("selected projectile: "+name(pair.beforeSetup.loadedAmmo>0?pair.beforeSetup.loadedAmmo:pair.beforeSetup.slots.getOrDefault("ammo",-1),catalog)
			+" → "+name(pair.afterSetup.loadedAmmo>0?pair.afterSetup.loadedAmmo:pair.afterSetup.slots.getOrDefault("ammo",-1),catalog)); }
		return changes.isEmpty()?"Same gear · no change":String.join("; ",changes);
	}
	private void details(NexSummary summary, KnowledgeCatalog catalog, java.util.function.BooleanSupplier valid)
	{ details(content,summary,catalog,valid,false); }
	private void details(JPanel parent,NexSummary summary,KnowledgeCatalog catalog,java.util.function.BooleanSupplier valid,boolean diagnostic)
	{
		PanelWidgets.centeredLabel(parent,summary.outcome==com.ironpath.encounter.EncounterSummary.Outcome.COMPLETE?String.format(Locale.ROOT,"Kill #%d · %.1f s",summary.key,summary.seconds()):"Incomplete attempt",true,0,ColorScheme.TEXT_COLOR);
		if(diagnostic) {
		PanelWidgets.text(parent,"Kill #"+summary.key+" · "+summary.outcome+" · "+summary.reason);
		PanelWidgets.text(parent,"Capture adapter "+com.ironpath.nex.NexCapture.VERSION);
		PanelWidgets.text(parent,summary.timerScope); PanelWidgets.text(parent,NexSummary.DAMAGE_SCOPE);
		if(summary.signals!=null)
		{
			var s=summary.signals;
			PanelWidgets.text(parent,"Measured start (smoke): "+tick(s.smokeTick)+"\nArena start transition (diagnostic): "+tick(s.boundaryTick)
				+"\nSmoke line observed: "+tick(s.observedSmokeTick)
				+"\nNex death/dying: "+tick(s.deathTick)+"\nArena end boundary: "+tick(s.barrierEndTick));
			String barriers=s.trace.stream().filter(line->line.contains("barrier ")).collect(java.util.stream.Collectors.joining("; "));
			PanelWidgets.text(parent,"Barrier readings: "+(barriers.isEmpty()?"not recorded":barriers));
			JPanel trace=ownedBody(); trace.setVisible(false);
			button(parent,"Capture signal trace",()->{ if(valid.getAsBoolean()) { trace.setVisible(!trace.isVisible()); revalidate(); repaint(); } });
			s.trace.forEach(line->PanelWidgets.text(trace,line)); parent.add(trace);
			PanelWidgets.text(parent,"Observed phases: "+String.join(", ",List.of("SMOKE","SHADOW","BLOOD","ICE","ZAROS").stream().filter(s.phases::contains).toArray(String[]::new))
				+"\nContinuous state samples: "+s.continuous);
		}
		PanelWidgets.text(parent,"State coverage "+Math.min(summary.sampledTicks,summary.expectedTicks)+" / "+summary.expectedTicks+" ticks. Unclassified hits: "+summary.unclassifiedHits+". Eligible fraction counts observed attempts, not kills before capture was enabled.");
		summary.damage.forEach((scope,damage)->PanelWidgets.text(parent,scope+": "+damage.local+" local / "+damage.total+" observed damage"));
		if(!summary.beforeStart.isEmpty())
		{ PanelWidgets.text(parent,summary.beforeStart.size()+" pre-smoke state changes retained separately; excluded from fight statistics and equipment comparisons."); }
		}
		List<NexSummary.Sample> history=new ArrayList<>(summary.beforeStart); history.addAll(summary.timeline);
		if (!history.isEmpty())
		{
			JComboBox<String> samples=new JComboBox<>();
			samples.setName("nex-history-state");
			for(int i=0;i<history.size();i++)
			{ var s=history.get(i); samples.addItem((i<summary.beforeStart.size()?"Before smoke · ":"")+"Tick "+s.tick+" · "+s.phase+" · "+s.role); }
			if(!summary.timeline.isEmpty()) { samples.setSelectedIndex(summary.beforeStart.size()); }
			samples.setFont(getFont()); samples.setAlignmentX(LEFT_ALIGNMENT); samples.setMaximumSize(new Dimension(PANEL_WIDTH-12,Math.max(28,samples.getPreferredSize().height))); parent.add(samples); JPanel evidence=new JPanel(); evidence.setLayout(new BoxLayout(evidence,BoxLayout.Y_AXIS)); evidence.setFont(getFont()); parent.add(evidence);
			Runnable display=()->{
				if (!valid.getAsBoolean()) { return; }
				evidence.removeAll(); NexSummary.Sample s=history.get(samples.getSelectedIndex());
				if(diagnostic) { PanelWidgets.text(evidence,s.phase+" / "+s.window+"\nTarget "+s.target+" · animation "+s.animation+" (launch unverified)");
				PanelWidgets.text(evidence,s.targetState.reading());
				}
				new TreeMap<>(s.state.slots).forEach((slot,id)->PanelWidgets.text(evidence,slot+": "+name(id,catalog)));
				if(diagnostic) { PanelWidgets.text(evidence,"Base levels "+new TreeMap<>(s.state.realLevels)+"\nCurrent levels "+new TreeMap<>(s.state.visibleLevels)+"\nPrayers "+s.state.prayers);
				PanelWidgets.text(evidence,"Special energy (tenths of %) "+s.state.specialEnergy+" · selected "+s.state.specialSelected+"\nResources "+new TreeMap<>(s.state.resources));
				Integer charge=s.state.resources.get("death-charge-active");
				PanelWidgets.text(evidence,charge!=null && charge==2?"Detected two active Death Charge charges (improved active use)":charge!=null && charge==1?"Detected one active charge; upgrade unknown from this value":"No active charge observed; upgrade availability unknown");
				PanelWidgets.text(evidence,"Inventory "+(s.state.inventory==null?"unread":new TreeMap<>(s.state.inventory))+"\nChanges alone do not prove consumption. Quiver raw counters need client validation; other internal charges remain unknown.");
				}
				evidence.revalidate(); revalidate();
			}; samples.addActionListener(e->display.run()); display.run();
		}
		if(diagnostic) { summary.gaps.forEach(g->PanelWidgets.text(parent,g)); }
	}
	private static String tick(Integer value) { return value==null?"not observed":"tick "+value; }
	private void storageStatus(String storage)
	{
		if(storage==null) { return; }
		if(storage.contains("failed")) { text("Settings not saved"); }
		else if(storage.contains("could not be read")) { text("Settings unavailable · editing disabled"); }
		else if(storage.startsWith("Saving")) { text("Saving…"); }
		else if(storage.startsWith("Loading")) { text("Loading settings…"); }
	}

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
		control.setFont(getFont()); control.setMaximumSize(new Dimension(PANEL_WIDTH-12,Math.max(28,control.getPreferredSize().height))); control.setAlignmentX(LEFT_ALIGNMENT); content.add(control);
	}
	private void text(String value) { PanelWidgets.text(content,value); }
	private static JButton button(JPanel parent,String value,Runnable action) { return PanelWidgets.button(parent,value,action); }
}
