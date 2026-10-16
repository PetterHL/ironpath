package com.ironpath.nex;

import com.ironpath.encounter.EncounterSummary.Outcome;
import java.util.*;

/** Separate lifetime from live account observations. Suspended evidence is inaccessible until banking revalidation. */
public final class NexSession
{
	public static final int MAX_RECENT=50;
	private final Deque<NexSummary> recent=new ArrayDeque<>();
	private String identity;
	private int world, suspendedAt;
	private boolean suspended, atBank, returning;
	private long revision;
	public synchronized long revision() { return revision; }
	public synchronized boolean suspended() { return suspended; }
	public synchronized boolean select(String identity, int world)
	{
		if (!Objects.equals(this.identity,identity) || this.world!=world)
		{ clear(); this.identity=identity; this.world=world; return true; }
		return false;
	}
	public synchronized void add(NexSummary summary)
	{
		if (identity==null || suspended) { return; }
		recent.addLast(summary); while(recent.size()>MAX_RECENT) { recent.removeFirst(); } ++revision;
	}
	public synchronized boolean suspend(String identity, int world, int tick, boolean fromArena)
	{
		if ((!fromArena && !atBank && !suspended) || this.identity==null || !this.identity.equals(identity) || this.world!=world)
		{ clear(); return false; }
		if (!suspended) { suspendedAt=tick; returning=atBank; atBank=false; }
		suspended=true; ++revision; return true;
	}
	public synchronized boolean revalidate(String identity, int world, int tick, boolean bankVisible, boolean arena)
	{
		if (!Objects.equals(this.identity,identity) || this.world!=world || tick<suspendedAt)
		{ clear(); return false; }
		if (!suspended)
		{
			boolean nextBank=bankVisible || atBank && !arena;
			if(atBank!=nextBank) { atBank=nextBank; ++revision; }
			return true;
		}
		// Completed history is bounded by count and account lifetime, not how quickly a bank is opened.
		// A nearby Nex can remain loaded during outbound travel; keep waiting until a bank is verified.
		if (!bankVisible && !(arena && returning)) { return false; }
		suspended=false; atBank=bankVisible; returning=false; ++revision; return true;
	}
	public synchronized void clear() { identity=null; world=0; suspendedAt=0; suspended=atBank=returning=false; recent.clear(); ++revision; }
	public synchronized List<NexSummary> recent() { return suspended ? List.of() : List.copyOf(recent); }
	public synchronized Stats stats()
	{
		List<NexSummary> all=recent();
		NexSummary last=all.stream().filter(s->s.outcome==Outcome.COMPLETE).reduce((a,b)->b).orElse(null);
		List<NexSummary> cohort=new ArrayList<>();
		if (last!=null) { all.stream().filter(s->s.outcome==Outcome.COMPLETE && s.cohort.equals(last.cohort)).forEach(cohort::add); }
		return new Stats(all,last,cohort);
	}
	public static final class Stats
	{
		public final NexSummary last, lastAttempt;
		public final int attempts, complete, failed, censored, n, shareN;
		public final double meanSeconds, minSeconds, maxSeconds, meanShare, minShare, maxShare, aggregateShare;
		public final List<NexSummary> cohort;
		Stats(List<NexSummary> all, NexSummary last, List<NexSummary> cohort)
		{
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
