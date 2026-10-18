package com.ironpath;

import com.ironpath.dev.PanelPreview;
import com.ironpath.dev.ReviewReport;
import com.ironpath.nex.*;
import java.nio.file.*;
import java.util.List;
import javax.swing.*;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InventoryID;
import static com.ironpath.nex.NexContext.*;
import static org.mockito.Mockito.*;

/** CTX1 extends DEV1's public-event/plugin/Swing pipeline; no parallel capture harness. */
final class OfflineContextScenarios
{
	static final List<String> IDS=List.of("nex-context","nex-context-active","nex-context-pending");
	private OfflineContextScenarios() { }
	static void run(String id,Path output,ReviewReport report) throws Exception
	{
		report.section("Pipeline and provenance","Existing DEV1 OfflinePluginReplay: synthetic public RuneLite events, real plugin/adapter/session, guarded worker and native Swing. "
			+"Context edits use the actual editor and Apply action. Preference writes are collected in memory only. No performance history is persisted. "
			+"All context confirmations, visible roster, bank stock, gear/levels/prayers and damage/timing are synthetic declarations, not live evidence.");
		for(int font:List.of(12,20))
		{
			try(var r=new OfflinePluginReplay(font,true))
			{
				if(font==12) { OfflineReview.versions(report,r.catalog); }
				if(id.equals("nex-context")) { correction(r,output,font,report); }
				else if(id.equals("nex-context-active")) { active(r,output,font,report); }
				else { pending(r,output,font,report); }
				report.section("Ordered callbacks · "+font,"```text\n"+String.join("\n",r.trace)+"\n```");
			}
		}
	}
	private static void correction(OfflinePluginReplay r,Path output,int font,ReviewReport report) throws Exception
	{
		// Optional defaults can be saved without a kill; they do not confirm the next kill.
		edt(()->{ PanelPreview.click(r.panel,"Future kill context"); fill(r,Strategy.UNKNOWN,Group.UNKNOWN,Participation.UNKNOWN,null,false,false); PanelPreview.click(r.panel,"Save defaults"); });
		r.settle(); edt(()->{
			PanelPreview.click(r.panel,"Context sources");
			report.check("Saved default source details refresh to the applied values",true,PanelPreview.text(r.panel).contains("Strategy: UNKNOWN; group: UNKNOWN; participation: UNKNOWN"));
			PanelPreview.click(r.panel,"Back to selected view");
		});
		var first=r.fight(20,20,60,true); r.settle();
		report.check("Unknown saved defaults stay unknown",NexContext.UNKNOWN,first.context);
		report.check("Unconfirmed complete kill is excluded from matched means",0,r.plugin.nexSession.stats().n);
		capture(r,output,"unknown-context",font,report);
		open(r); fillAndApply(r,Strategy.MP2,Group.ORGANIZED,Participation.EXACT,3,false,false); r.settle();
		report.check("Entered but unconfirmed context does not enter means",0,r.plugin.nexSession.stats().n);
		report.check("Per-field provenance distinguishes manual unconfirmed values",Source.UNCONFIRMED,r.plugin.nexSession.record(first.key).context.strategySource);
		capture(r,output,"unconfirmed-context",font,report);
		fillAndApply(r,Strategy.MP2,Group.ORGANIZED,Participation.EXACT,3,true,false); r.settle();
		var firstRecord=r.plugin.nexSession.record(first.key);
		report.check("Correction applies only to selected kill",true,firstRecord.context.pilot());
		report.check("Captured context remains unknown",NexContext.UNKNOWN,first.context);
		report.check("Without remember, defaults stay unknown",NexContext.UNKNOWN,r.plugin.session.preferences().nexContext);
		report.check("Single confirmed kill is eligible",1,r.plugin.nexSession.stats().n);
		capture(r,output,"corrected-context",font,report);
		edt(()->{
			PanelPreview.viewport(r.panel,output.resolve("context-fields-viewport-"+font+".png"),225,600,"nex-context-strategy",List.of("nex-context-strategy","nex-context-group","nex-context-participation","nex-context-count"),report);
			PanelPreview.viewport(r.panel,output.resolve("context-apply-viewport-"+font+".png"),225,600,"nex-context-participation",List.of("nex-context-participation","nex-context-count","nex-context-apply"),report);
			PanelPreview.click(r.panel,"Context sources");
		});
		capture(r,output,"context-source-details",font,report);
		edt(()->PanelPreview.click(r.panel,"Context sources"));
		edt(()->PanelPreview.click(r.panel,"Encounter"));
		var second=r.fight(30,60,60,true); r.settle();
		open(r); fillAndApply(r,Strategy.MP2,Group.ORGANIZED,Participation.EXACT,3,true,false); r.settle();
		var stats=r.plugin.nexSession.stats();
		report.check("Both corrected kills reconcile into the matched cohort",2,stats.n);
		report.near("Independent mean: (20 + 30) ticks × 0.6 / 2",15,stats.meanSeconds,1e-10);
		report.near("Independent mean share: (25 + 50) / 2",37.5,stats.meanShare,1e-10);
		report.near("Independent aggregate share: 100 × 80 / 200",40,stats.aggregateShare,1e-10);
		report.check("Timing and damage unchanged by corrections",true,first.seconds()==12 && second.seconds()==18 && first.localDamage()==20 && second.totalDamage()==120);
		// Invalid input must leave the revision unchanged.
		long revision=r.plugin.nexSession.record(second.key).revision;
		fillAndApply(r,Strategy.MP2,Group.ORGANIZED,Participation.EXACT,0,true,false); r.callbacks.drain();
		report.check("Zero count rejected without an annotation",revision,r.plugin.nexSession.record(second.key).revision);
		edt(()->report.check("Validation is visible beside editor",true,PanelPreview.text(r.panel).contains("positive exact count")));
		capture(r,output,"invalid-count",font,report);
		fillAndApply(r,Strategy.RP2,Group.MASS,Participation.VARIABLE,null,true,false); r.settle();
		var variable=r.plugin.nexSession.record(second.key);
		report.check("Mass does not overwrite RP2",Strategy.RP2,variable.context.strategy); report.check("Variable participation cannot be exact",null,variable.context.participants);
		report.check("Variable mass excluded from matched cohort",0,r.plugin.nexSession.stats().n);
		report.check("Independent ordinary arithmetic remains inspectable",true,variable.calculation.choices.stream().anyMatch(c->c.supported()));
		report.check("Unsupported context cannot enable pilot recommendations",false,variable.context.pilot());
		capture(r,output,"variable-mass",font,report);
		export(r,output,font,report,second.key,variable.revision);
	}
	private static void active(OfflinePluginReplay r,Path output,int font,ReviewReport report) throws Exception
	{
		var first=r.fight(20,20,60,true); r.settle(); var original=r.plugin.ownedResult;
		report.check("Pilot migration is inherited, not confirmed",Source.INHERITED,first.context.strategySource);
		long inputRevision=original.inputRevision;
		// New public bank information and a changed planning lock exist before the historical edit.
		r.loading(); ItemContainer bank=mock(ItemContainer.class); when(bank.getItems()).thenReturn(new Item[]{new Item(26382,1)});
		when(r.client.getItemContainer(InventoryID.BANK)).thenReturn(bank); r.arrive(true);
		r.plugin.onItemContainerChanged(new ItemContainerChanged(InventoryID.BANK,bank)); r.tick(); r.settle();
		report.check("Newer bank observation really exists",true,r.plugin.session.snapshot().bank.known());
		r.loading(); r.arrive(false);
		// Use real Setup action to change future planning. The old calculation must retain its lock.
		edt(()->PanelPreview.click(r.panel,"Setup"));
		edt(()->PanelPreview.combo(r.panel,"nex-lock-slot").setSelectedItem("shield"));
		edt(()->PanelPreview.click(r.panel,"Clear selected lock")); r.callbacks.drain(); r.settle();
		edt(()->PanelPreview.click(r.panel,"Encounter"));
		report.check("Future planning lock really changed",false,r.plugin.session.preferences().nex.locks.containsKey("melee/shield"));
		open(r); // Keep the selected old kill's editor open as the next public attempt starts.
		var second=r.fight(30,60,60,true,offset->{
			if(offset!=5) { return; }
			try
			{
				r.edt(); long activeKey=r.plugin.nex.capture.key();
				fillAndApply(r,Strategy.RP2,Group.MASS,Participation.EXACT,12,true,true); r.settle();
				report.check("Historical edit does not censor another active attempt",true,r.plugin.nex.capture.active() && activeKey==r.plugin.nex.capture.key());
				var corrected=r.plugin.nexSession.record(first.key).calculation;
				report.check("Corrected old calculation publishes during the new attempt",1L,corrected.contextRevision);
				report.check("Old calculation retains its exact input revision",inputRevision,corrected.inputRevision);
				report.check("New bank data cannot enter a context-only correction",false,corrected.choices.stream().anyMatch(c->c.bankPreparation));
				report.check("Frozen planning choices survive a newer preference",original.planning.locks,corrected.planning.locks);
				report.near("Corrected ordinary arithmetic still matches original",original.choices.get(0).comparison.after.dps,corrected.choices.get(0).comparison.after.dps,0);
				capture(r,output,"historical-edit-active",font,report);
			}
			catch(Exception failure) { throw new IllegalStateException(failure); }
		}); r.settle();
		report.check("Other attempt still completes",com.ironpath.encounter.EncounterSummary.Outcome.COMPLETE,second.outcome);
		report.check("Mid-combat defaults apply only to next attempt",NexContext.PILOT,second.context);
		report.check("Historical target remains selected while a newer kill publishes",first.key,r.plugin.nexSession.record(first.key).summary.key);
		capture(r,output,"historical-after-new-kill",font,report);
		export(r,output,font,report,first.key,1);
		edt(()->PanelPreview.click(r.panel,"Encounter"));
		var third=r.fight(20,20,60,true); r.settle();
		report.check("Remembered strategy snapshots into subsequent attempt",Strategy.RP2,third.context.strategy);
		report.check("Remembered group stays separate",Group.MASS,third.context.group);
		report.check("Remembered count stays inherited",Source.INHERITED,third.context.countSource);
		report.check("Remembered defaults cannot confirm a new kill",false,third.context.confirmed());
		report.check("Only explicit preferences written in memory",true,!r.preferenceWrites.isEmpty() && r.preferenceWrites.get(r.preferenceWrites.size()-1).nexContext.strategy==Strategy.RP2);
		capture(r,output,"future-inherited",font,report);
	}
	private static void pending(OfflinePluginReplay r,Path output,int font,ReviewReport report) throws Exception
	{
		var first=r.fight(20,20,60,true); // Hold the initial calculation.
		Runnable initial=r.workers.take(); open(r);
		fillAndApply(r,Strategy.RP2,Group.ORGANIZED,Participation.EXACT,2,true,false); r.callbacks.drain(); r.edt();
		Runnable oldCorrection=r.workers.take();
		fillAndApply(r,Strategy.OTHER,Group.MASS,Participation.VARIABLE,null,true,false); r.callbacks.drain(); r.edt();
		report.check("Repeated edits each produce a distinct revision",2L,r.plugin.nexSession.record(first.key).revision);
		capture(r,output,"pending-context",font,report);
		edt(()->PanelPreview.click(r.panel,"Diagnostics"));
		edt(()->{
			PanelPreview.click(r.panel,"Copy report now"); String text=reportText(r);
			report.check("Pending corrected report includes current context revision",true,text.contains("Effective context revision: 2"));
			report.check("Pending output never relabels the old calculation",false,text.contains("Before metrics:"));
		});
		r.settle(); var fresh=r.plugin.nexSession.record(first.key).calculation;
		report.check("Newest correction publishes",2L,fresh.contextRevision);
		r.runWorker(oldCorrection); r.runWorker(initial); r.callbacks.drain(); r.edt();
		report.check("Delayed results cannot replace current corrected calculation",true,fresh==r.plugin.nexSession.record(first.key).calculation);
		export(r,output,font,report,first.key,2);
		// Hold a corrected worker's publication, then exercise the actual Reset action.
		edt(()->PanelPreview.click(r.panel,"Encounter")); open(r);
		fillAndApply(r,Strategy.MP2,Group.ORGANIZED,Participation.EXACT,3,true,false); r.callbacks.drain();
		r.runWorker(r.workers.take()); Runnable stale=r.callbacks.take();
		edt(()->{ PanelPreview.click(r.panel,"Encounter"); PanelPreview.click(r.panel,"Reset session"); }); r.callbacks.drain(); r.edt();
		report.check("Reset clears annotated records and frozen inputs",0,r.plugin.nexSession.stats().records.size());
		var next=r.fight(20,20,60,true); r.settle(); var current=r.plugin.ownedResult;
		stale.run(); r.callbacks.drain(); r.edt();
		report.check("Old corrected callback cannot resurrect reset history",true,current==r.plugin.ownedResult && r.plugin.nexSession.record(first.key)==null);
		open(r); fillAndApply(r,Strategy.MP2,Group.ORGANIZED,Participation.EXACT,3,true,false); r.callbacks.drain();
		r.runWorker(r.workers.take()); Runnable evictionStale=r.callbacks.take();
		JButton[] staleApply={null}; edt(()->staleApply[0]=(JButton)named(r,"nex-context-apply"));
		for(int i=0;i<NexSession.MAX_RECENT;i++) { r.fight(12,20,60,true); }
		r.settle(); evictionStale.run(); r.callbacks.drain(); r.edt();
		edt(()->staleApply[0].doClick(0)); r.callbacks.drain();
		report.check("Public replays evict the annotated inspected record and its inputs",null,r.plugin.nexSession.record(next.key));
		report.check("Completed evidence and annotations share the 50-record bound",NexSession.MAX_RECENT,r.plugin.nexSession.stats().records.size());
		edt(()->report.check("Evicted editor cannot remain visible",false,PanelPreview.text(r.panel).contains("Kill #"+next.key+" context")));
		capture(r,output,"context-evicted",font,report);
		open(r); fillAndApply(r,Strategy.MP2,Group.ORGANIZED,Participation.EXACT,3,true,false); r.callbacks.drain();
		r.runWorker(r.workers.take()); Runnable logoutStale=r.callbacks.take();
		when(r.client.getGameState()).thenReturn(GameState.LOGIN_SCREEN);
		GameStateChanged logout=new GameStateChanged(); logout.setGameState(GameState.LOGIN_SCREEN); r.plugin.onGameStateChanged(logout);
		logoutStale.run(); r.callbacks.drain(); r.edt();
		report.check("Logout clears annotations, inputs and pending corrected results",true,r.plugin.nexSession.stats().records.isEmpty() && r.plugin.ownedResult==null);
		edt(()->{ PanelPreview.click(r.panel,"Diagnostics"); PanelPreview.click(r.panel,"Copy report now"); report.check("Logout report has no selected kill",false,reportText(r).contains("Report-local kill:")); });
		capture(r,output,"logout-cleared-context",font,report);
	}
	private static void open(OfflinePluginReplay r) throws Exception { edt(()->PanelPreview.click(r.panel,"Edit kill context")); }
	private static void fillAndApply(OfflinePluginReplay r,Strategy strategy,Group group,Participation participation,Integer count,boolean confirm,boolean remember) throws Exception
	{ edt(()->{ fill(r,strategy,group,participation,count,confirm,remember); PanelPreview.click(r.panel,"Apply context"); }); }
	private static void fill(OfflinePluginReplay r,Strategy strategy,Group group,Participation participation,Integer count,boolean confirm,boolean remember)
	{
		PanelPreview.combo(r.panel,"nex-context-strategy").setSelectedItem(strategy); PanelPreview.combo(r.panel,"nex-context-group").setSelectedItem(group);
		PanelPreview.combo(r.panel,"nex-context-participation").setSelectedItem(participation);
		((JTextField)named(r,"nex-context-count")).setText(count==null?"":count.toString());
		for(String field:List.of("strategy","group","count"))
		{ var box=PanelPreview.children(r.panel).stream().filter(c->("nex-context-confirm-"+field).equals(c.getName())).findFirst(); if(box.isPresent()) { ((JCheckBox)box.get()).setSelected(confirm); } }
		var box=PanelPreview.children(r.panel).stream().filter(c->"nex-context-remember".equals(c.getName())).findFirst(); if(box.isPresent()) { ((JCheckBox)box.get()).setSelected(remember); }
	}
	private static java.awt.Component named(OfflinePluginReplay r,String name)
	{ return PanelPreview.children(r.panel).stream().filter(c->name.equals(c.getName())).findFirst().orElseThrow(); }
	private static String reportText(OfflinePluginReplay r) { return ((JTextArea)named(r,"nex-report-text")).getText(); }
	private static void export(OfflinePluginReplay r,Path output,int font,ReviewReport report,long key,long revision) throws Exception
	{
		edt(()->{
			PanelPreview.click(r.panel,"Diagnostics"); PanelPreview.click(r.panel,"Copy report now"); String text=reportText(r);
			report.check("Displayed diagnostic kill stays selected",true,PanelPreview.text(r.panel).contains("Frozen kill #"+key));
			report.check("Report and selected context revision agree",true,text.contains("Effective context revision: "+revision+";") && text.contains("Calculation context/input revisions: "+revision+" /"));
			report.check("Corrected report includes no account or roster identity",false,text.contains("offline-synthetic") || text.contains("synthetic member") || text.contains("NEX@"));
			try { Files.writeString(output.resolve("corrected-report-"+font+".txt"),text); } catch(Exception ex) { throw new IllegalStateException(ex); }
		});
		capture(r,output,"corrected-diagnostics",font,report);
	}
	private static void capture(OfflinePluginReplay r,Path output,String state,int font,ReviewReport report) throws Exception
	{ edt(()->{ try { PanelPreview.capture(r.panel,output.resolve(state+"-"+font+".png"),225,report); } catch(Exception ex) { throw new IllegalStateException(ex); } }); }
	private static void edt(ThrowingRunnable action) throws Exception
	{ SwingUtilities.invokeAndWait(()->{ try { action.run(); } catch(Exception ex) { throw new IllegalStateException(ex); } }); }
	@FunctionalInterface private interface ThrowingRunnable { void run() throws Exception; }
}
