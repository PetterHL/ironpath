package com.ironpath;

import com.ironpath.model.Truth;
import com.ironpath.nex.*;
import java.util.*;
import net.runelite.api.GameState;

/** One user-triggered export: current bounded session overview plus a frozen selected record.
 * Only typed public scalars and fixed reason categories enter the overview. Never persist it. */
final class NexSupportReport
{
	static final int MAX_OVERVIEW_CHARS=64000, MAX_CHARS=MAX_OVERVIEW_CHARS+NexReport.MAX_CHARS;
	static final String SELECTED="\n--- FROZEN SELECTED RECORD ---\n";
	static final class Runtime
	{
		static final Runtime UNKNOWN=new Runtime(-1,null,null,Truth.UNKNOWN,null,Truth.UNKNOWN,Truth.UNKNOWN,false,false);
		final long scope;
		final GameState gameState;
		final Integer tick,barrier;
		final Truth bankVisible,arena,identityReadable;
		final boolean active,awaitingArena;
		Runtime(long scope,GameState gameState,Integer tick,Truth bankVisible,Integer barrier,Truth arena,Truth identityReadable,boolean active,boolean awaitingArena)
		{
			this.scope=scope; this.gameState=gameState; this.tick=tick; this.bankVisible=Objects.requireNonNull(bankVisible);
			this.barrier=barrier!=null && barrier>=0 && barrier<=3?barrier:null;
			this.arena=Objects.requireNonNull(arena); this.identityReadable=Objects.requireNonNull(identityReadable);
			this.active=active; this.awaitingArena=awaitingArena;
		}
	}
	static final class Frame
	{
		final NexSession.Stats stats;
		final Runtime runtime;
		Frame(NexSession.Stats stats,Runtime runtime) { this.stats=Objects.requireNonNull(stats); this.runtime=Objects.requireNonNull(runtime); }
	}
	static boolean hidden(NexReport.State state)
	{ return state==NexReport.State.PAUSED || state==NexReport.State.NO_ACCOUNT || state==NexReport.State.NOT_READY || state==NexReport.State.RESET; }
	static String selected(String report)
	{ int at=report.indexOf(SELECTED); return at<0?report:report.substring(at+SELECTED.length()); }
	static String snapshot(Frame frame,long expectedScope,NexSummary selected,long selectedRevision,String frozen,
		NexReport.Provenance provenance,NexReport.State state,NexPanel.TrackingStatus tracking)
	{
		var stats=frame.stats;
		boolean changed=stats.scope!=expectedScope, hidden=changed || hidden(state) || stats.suspended;
		var runtime=!changed && frame.runtime.scope==stats.scope?frame.runtime:Runtime.UNKNOWN;
		StringBuilder out=new StringBuilder("IronPath troubleshooting report · schema 1\n");
		out.append("Evidence: ").append(provenance).append("; status: ").append(changed?"SESSION_CHANGED":stats.suspended?NexReport.State.PAUSED:state).append('\n');
		out.append("Build: ").append(BuildInfo.VERSION).append("; revision: ").append(BuildInfo.REVISION)
			.append("; working tree: ").append(BuildInfo.WORKTREE).append("; adapter: ").append(NexCapture.VERSION).append('\n');
		String clientVersion=net.runelite.client.RuneLiteProperties.getVersion();
		out.append("RuneLite client version: ").append(clientVersion!=null && clientVersion.matches("\\d+\\.\\d+\\.\\d+(?:-SNAPSHOT)?")?clientVersion:"UNKNOWN").append('\n');
		out.append("Overview is current at copy; runtime readings are the latest client-thread sample; the selected-record section is frozen. Attempt rows are capture intervals, not confirmed player attempts.\n");
		out.append("Runtime readings: client=").append(runtime.gameState==null?"UNKNOWN":runtime.gameState)
			.append("; bank visible=").append(runtime.bankVisible).append("; arena=").append(runtime.arena)
			.append("; barrier=").append(runtime.barrier==null?"UNKNOWN":runtime.barrier).append("; identity readable=").append(runtime.identityReadable)
			.append("; panel tracking=").append(changed?"UNKNOWN":tracking).append("; session suspended=").append(changed?"UNKNOWN":stats.suspended)
			.append("; awaiting arena=").append(runtime==Runtime.UNKNOWN?"UNKNOWN":runtime.awaitingArena).append("; capture active=").append(runtime==Runtime.UNKNOWN?"UNKNOWN":runtime.active).append('\n');
		out.append("Last clear category: ").append(changed?"SESSION_CHANGED":clearCode(stats.resetReason)).append('\n');
		out.append("Capture bounds: states=").append(NexCapture.MAX_SAMPLES).append("; callbacks=").append(NexCapture.MAX_EVENTS)
			.append("; elapsed ticks=").append(NexCapture.MAX_TICKS).append("; retained records=").append(NexSession.MAX_RECENT).append('\n');
		String detail;
		if(hidden)
		{
			out.append("Session overview: HIDDEN; revalidation or a current account scope is required. No record counts, reasons or selected evidence exported.\n");
			detail="Session evidence unavailable or hidden; no kill/calculation exported.\n";
		}
		else
		{
			List<NexRecord> records=new ArrayList<>(stats.records.values()); records.sort(Comparator.comparingLong(r->r.summary.key));
			Map<Long,String> refs=new HashMap<>(); for(int i=0;i<Math.min(records.size(),NexSession.MAX_RECENT);i++) { refs.put(records.get(i).summary.key,"A"+(i+1)); }
			out.append("Retained session: ").append(stats.complete).append(" complete; ").append(stats.failed).append(" failed; ").append(stats.censored).append(" excluded. Older evicted records are unavailable.\n");
			out.append("Selected record: ").append(selected==null?"NONE":refs.getOrDefault(selected.key,"UNAVAILABLE"))
				.append("; latest recorded interval: ").append(stats.lastAttempt==null?"NONE":refs.getOrDefault(stats.lastAttempt.key,"UNAVAILABLE")).append('\n');
			Integer base=records.isEmpty()?null:records.get(0).summary.startTick;
			out.append("Offsets below are ticks relative to the first retained measured start; unknown values stay UNKNOWN.\n");
			out.append("Runtime sample offset: ").append(offset(runtime.tick,base)).append('\n');
			int count=0;
			for(var record:records)
			{
				if(count>=NexSession.MAX_RECENT) { break; } count++;
				var s=record.summary; var signals=s.signals; var origin=signals==null?NexSummary.Origin.UNKNOWN:signals.origin;
				out.append("Attempt ").append(refs.get(s.key)).append(": outcome=").append(s.outcome).append("; exclusion=").append(NexReport.exclusion(s))
					.append("; origin=").append(origin.source).append("; origin form=").append(origin.form).append("; origin barrier=").append(origin.barrier==null?"UNKNOWN":origin.barrier)
					.append("; origin offset=").append(offset(origin.tick,base)).append("; measured start=").append(offset(s.startTick,base)).append("; end=").append(offset(s.endTick,base))
					.append("; sampled/expected=").append(s.sampledTicks).append('/').append(s.expectedTicks).append('\n');
				out.append("  Signals: smoke=").append(offset(signals==null?null:signals.smokeTick,base)).append("; observed smoke=").append(offset(signals==null?null:signals.observedSmokeTick,base))
					.append("; death=").append(offset(signals==null?null:signals.deathTick,base)).append("; arena end=").append(offset(signals==null?null:signals.barrierEndTick,base))
					.append("; continuous=").append(signals==null?"UNKNOWN":signals.continuous).append("; phases=");
				List<String> phases=new ArrayList<>(); if(signals!=null) { for(String phase:List.of("SMOKE","SHADOW","BLOOD","ICE","ZAROS")) { if(signals.phases.contains(phase)) { phases.add(phase); } } }
				out.append(phases).append("; states=").append(s.timeline.size()+s.beforeStart.size()).append("; observations truncated=").append(s.observations.truncated)
					.append("; actions truncated=").append(s.actions.truncated).append('\n');
			}
			out.append("Overview records: ").append(count).append('/').append(records.size()).append("; complete=").append(count==records.size()).append('\n');
			var retained=selected==null?null:stats.record(selected);
			boolean selectedCurrent=selected==null || retained!=null && retained.summary==selected && retained.revision==selectedRevision;
			detail=selectedCurrent?frozen:"Selected record unavailable or revised; no stale calculation exported.\n";
		}
		out.append("No names, account/world IDs, raw chat/trace, gear or container data in the overview. No automatic save or upload.\n");
		if(out.length()>MAX_OVERVIEW_CHARS) { out=new StringBuilder("IronPath troubleshooting report · schema 1\nOverview unavailable: SIZE_LIMIT; complete=false\n"); }
		if(detail.length()>NexReport.MAX_CHARS) { detail="Selected record unavailable: SIZE_LIMIT.\n"; }
		return out.append(SELECTED).append(detail).toString();
	}
	private static String offset(Integer tick,Integer base) { return tick==null || base==null?"UNKNOWN":Long.toString((long)tick-base); }
	private static String clearCode(String reason)
	{
		if(reason==null) { return "NONE"; }
		switch(reason)
		{
			case "Account transition: observations cleared. Reopen your bank after login.": return "ACCOUNT_TRANSITION";
			case "Profile changed; observations cleared.": case "Character profile changed; observations cleared.": return "PROFILE_CHANGED";
			case "World changed.": return "WORLD_CHANGED";
			case "Account or profile changed.": case "Account identity changed or could not be verified.": return "IDENTITY_UNVERIFIED_OR_CHANGED";
			case "Reset session selected.": return "USER_RESET";
			case "Capture setting changed.": return "CAPTURE_SETTING_CHANGED";
			case "Plugin disabled.": return "PLUGIN_DISABLED";
			case "Game tick counter restarted during travel.": return "CLOCK_ROLLBACK";
			case "Travel did not start from a verified Nex arena or bank.": return "UNVERIFIED_TRAVEL_SOURCE";
			default: return "UNCLASSIFIED_CLEAR";
		}
	}
}
