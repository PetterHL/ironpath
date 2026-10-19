package com.ironpath.nex;

import com.ironpath.encounter.EncounterSummary.Outcome;
import java.util.*;

/** Separate lifetime from live account observations. Suspended evidence requires same-account bank/arena revalidation. */
public final class NexSession
{
	public static final int MAX_RECENT=50;
	private final Deque<NexSummary> recent=new ArrayDeque<>();
	private final Map<Long,NexRecord> records=new LinkedHashMap<>();
	private String identity;
	private String resetReason;
	private int world, suspendedAt;
	private boolean suspended, atBank, returning, arenaReload;
	private long revision;
	public synchronized long revision() { return revision; }
	public synchronized boolean suspended() { return suspended; }
	public synchronized boolean accepts(String identity,int world)
	{ return this.identity!=null && this.identity.equals(identity) && this.world==world && !suspended; }
	public synchronized boolean select(String identity, int world)
	{
		if (!Objects.equals(this.identity,identity) || this.world!=world)
		{
			if(this.identity!=null) { clear(this.world!=world?"World changed.":"Account or profile changed."); }
			else { clearData(); }
			this.identity=identity; this.world=world; return true;
		}
		return false;
	}
	public synchronized void add(NexSummary summary)
	{
		if (identity==null || suspended || records.containsKey(summary.key)) { return; }
		resetReason=null; recent.addLast(summary); records.put(summary.key,new NexRecord(summary));
		while(recent.size()>MAX_RECENT) { NexSummary old=recent.removeFirst(); if(records.get(old.key).summary==old) { records.remove(old.key); } } ++revision;
	}
	public synchronized NexRecord record(long key) { return suspended?null:records.get(key); }
	public synchronized NexRecord freezeInputs(long key,com.ironpath.advisor.NexEvaluation inputs)
	{
		NexRecord record=record(key); if(record==null || record.inputs!=null) { return record; }
		record=record.inputs(inputs); records.put(key,record); return record;
	}
	public synchronized boolean publish(NexRecord expected,com.ironpath.advisor.NexOwnedResult result)
	{
		NexRecord record=record(expected.summary.key);
		if(record!=expected || result.summary!=record.summary || result.contextRevision!=record.revision || record.inputs==null
			|| result.inputRevision!=record.inputs.revision || !result.modelRevision.equals(record.inputs.modelRevision)
			|| !result.context.equals(record.context)) { return false; }
		records.put(record.summary.key,record.calculation(result)); ++revision; return true;
	}
	public synchronized boolean annotate(long key,long expectedRevision,NexContext context)
	{
		NexRecord old=record(key);
		if(identity==null || old==null || old.revision!=expectedRevision || old.summary.outcome!=Outcome.COMPLETE) { return false; }
		records.put(key,old.annotate(context)); ++revision; return true;
	}
	public synchronized boolean suspend(String identity, int world, int tick, boolean fromArena)
	{ return suspend(identity,world,tick,fromArena,false); }
	/** arenaScene is current arena evidence at LOADING, not a remembered departure source. */
	public synchronized boolean suspend(String identity, int world, int tick, boolean fromArena, boolean arenaScene)
	{
		if(this.identity==null || !this.identity.equals(identity)) { clear("Account identity changed or could not be verified."); return false; }
		if(this.world!=world) { clear("World changed."); return false; }
		if(!fromArena && !atBank && !suspended) { clear("Travel did not start from a verified Nex arena or bank."); return false; }
		if (!suspended) { suspendedAt=tick; returning=atBank; arenaReload=fromArena && arenaScene && !returning; atBank=false; }
		suspended=true; ++revision; return true;
	}
	public synchronized boolean revalidate(String identity, int world, int tick, boolean bankVisible, boolean arena)
	{ return revalidate(identity,world,tick,bankVisible,arena,false); }
	/** arenaExit requires an observed outside-arena barrier; missing NPCs alone are inconclusive. */
	public synchronized boolean revalidate(String identity, int world, int tick, boolean bankVisible, boolean arena, boolean arenaExit)
	{
		if(!Objects.equals(this.identity,identity)) { clear("Account identity changed or could not be verified."); return false; }
		if(this.world!=world) { clear("World changed."); return false; }
		if(tick<suspendedAt) { clear("Game tick counter restarted during travel."); return false; }
		if (!suspended)
		{
			boolean nextBank=bankVisible || atBank && !arena;
			if(atBank!=nextBank) { atBank=nextBank; ++revision; }
			return true;
		}
		// Completed history is bounded by count and account lifetime, not how quickly a bank is opened.
		// LOADING can rebuild the current arena. Resume if it is still verified after loading.
		// Once outside it, later nearby Nex evidence cannot turn an outbound leg into a reload.
		if (arenaExit && arenaReload) { arenaReload=false; ++revision; }
		if (!bankVisible && !(arena && (returning || arenaReload))) { return false; }
		suspended=false; atBank=bankVisible; returning=arenaReload=false; ++revision; return true;
	}
	/** Only constant, non-private reason text supplied by the lifecycle; kept in memory, never logged or saved. */
	public synchronized void clear(String reason)
	{
		if(reason==null) { resetReason=null; }
		else if(!recent.isEmpty()) { resetReason=reason; }
		clearData();
	}
	public synchronized void clear() { clear(null); }
	private void clearData() { identity=null; world=0; suspendedAt=0; suspended=atBank=returning=arenaReload=false; recent.clear(); records.clear(); ++revision; }
	public synchronized List<NexSummary> recent() { return suspended ? List.of() : List.copyOf(recent); }
	public synchronized Stats stats()
	{
		List<NexSummary> all=recent();
		NexSummary last=all.stream().filter(s->s.outcome==Outcome.COMPLETE).reduce((a,b)->b).orElse(null);
		List<NexSummary> cohort=new ArrayList<>();
		if (last!=null) { all.stream().filter(s->record(last.key).matches(record(s.key))).forEach(cohort::add); }
		return new Stats(all,last,cohort,resetReason,suspended?Map.of():records);
	}
	public static final class Stats
	{
		public final NexSummary last, lastAttempt;
		public final String resetReason;
		public final int attempts, complete, failed, censored, n, shareN;
		public final double meanSeconds, minSeconds, maxSeconds, meanShare, minShare, maxShare, aggregateShare;
		public final List<NexSummary> cohort;
		public final Map<Long,NexRecord> records;
		public NexRecord record(NexSummary summary) { return summary==null?null:records.get(summary.key); }
		Stats(List<NexSummary> all, NexSummary last, List<NexSummary> cohort,String resetReason,Map<Long,NexRecord> records)
		{
			this.records=Map.copyOf(records);
			this.resetReason=resetReason;
			this.last=last; lastAttempt=all.isEmpty()?null:all.get(all.size()-1); this.cohort=List.copyOf(cohort); attempts=all.size(); n=cohort.size();
			complete=(int)all.stream().filter(s->s.outcome==Outcome.COMPLETE).count();
			failed=(int)all.stream().filter(s->s.outcome==Outcome.FAILED).count(); censored=attempts-complete-failed;
			meanSeconds=cohort.stream().mapToDouble(NexSummary::seconds).average().orElse(Double.NaN);
			minSeconds=cohort.stream().mapToDouble(NexSummary::seconds).min().orElse(Double.NaN);
			maxSeconds=cohort.stream().mapToDouble(NexSummary::seconds).max().orElse(Double.NaN);
			shareN=(int)cohort.stream().filter(s->s.share()!=null).count();
			meanShare=cohort.stream().filter(s->s.share()!=null).mapToDouble(NexSummary::share).average().orElse(Double.NaN);
			minShare=cohort.stream().filter(s->s.share()!=null).mapToDouble(NexSummary::share).min().orElse(Double.NaN);
			maxShare=cohort.stream().filter(s->s.share()!=null).mapToDouble(NexSummary::share).max().orElse(Double.NaN);
			long total=cohort.stream().mapToLong(NexSummary::totalDamage).sum();
			aggregateShare=total==0?Double.NaN:100.0*cohort.stream().mapToLong(NexSummary::localDamage).sum()/total;
		}
	}
}
