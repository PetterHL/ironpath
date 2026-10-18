package com.ironpath.nex;

import java.util.Objects;

/** Annotation revisions are separate from immutable observations and share their bounded session lifetime. */
public final class NexRecord
{
	public final NexSummary summary;
	public final NexContext context;
	public final long revision;
	public final com.ironpath.advisor.NexEvaluation inputs;
	public final com.ironpath.advisor.NexOwnedResult calculation;
	public NexRecord(NexSummary summary) { this(summary,summary.context,0,null,null); }
	private NexRecord(NexSummary summary,NexContext context,long revision,com.ironpath.advisor.NexEvaluation inputs,com.ironpath.advisor.NexOwnedResult calculation)
	{ this.summary=Objects.requireNonNull(summary); this.context=Objects.requireNonNull(context); this.revision=revision; this.inputs=inputs; this.calculation=calculation; }
	public NexRecord annotate(NexContext context) { return new NexRecord(summary,context,revision+1,inputs,null); }
	NexRecord inputs(com.ironpath.advisor.NexEvaluation inputs) { return new NexRecord(summary,context,revision,inputs,null); }
	NexRecord calculation(com.ironpath.advisor.NexOwnedResult result) { return new NexRecord(summary,context,revision,inputs,result); }
	public String exclusion()
	{
		if(summary.outcome!=com.ironpath.encounter.EncounterSummary.Outcome.COMPLETE) { return "Incomplete attempt"; }
		return summary.comparability==null?"Comparability evidence unavailable":summary.comparability.exclusion(context);
	}
	public boolean matches(NexRecord other)
	{ return other!=null && exclusion()==null && other.exclusion()==null
		&& summary.timerScope.equals(other.summary.timerScope)
		&& summary.comparability.matches(other.summary.comparability,context,other.context); }
}
