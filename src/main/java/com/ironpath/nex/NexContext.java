package com.ironpath.nex;

import java.util.Objects;

/** Typed, memory-only kill context. Saved values become unconfirmed inheritance at attempt start. */
public final class NexContext
{
	public static final String VERSION="nex-context-1";
	public enum Strategy { MP2, RP2, OTHER, UNKNOWN }
	public enum Group { ORGANIZED, MASS, UNKNOWN }
	public enum Participation { EXACT, VARIABLE, UNKNOWN }
	public enum Source { UNKNOWN, INHERITED, UNCONFIRMED, ANNOTATED }
	public final Strategy strategy;
	public final Group group;
	public final Participation participation;
	/** Includes the player. Null for variable or unknown participation. */
	public final Integer participants;
	public final Source strategySource, groupSource, countSource;
	public static final NexContext UNKNOWN=new NexContext(Strategy.UNKNOWN,Group.UNKNOWN,Participation.UNKNOWN,null,
		Source.UNKNOWN,Source.UNKNOWN,Source.UNKNOWN);
	/** Migration of the former pilot: a planning assumption, never confirmed kill evidence. */
	public static final NexContext PILOT=new NexContext(Strategy.MP2,Group.ORGANIZED,Participation.EXACT,3,
		Source.INHERITED,Source.INHERITED,Source.INHERITED);

	public NexContext(Strategy strategy,Group group,Participation participation,Integer participants,
		Source strategySource,Source groupSource,Source countSource)
	{
		this.strategy=Objects.requireNonNull(strategy); this.group=Objects.requireNonNull(group);
		this.participation=Objects.requireNonNull(participation);
		if(participation==Participation.EXACT ? participants==null || participants<=0 : participants!=null)
		{ throw new IllegalArgumentException("Enter a positive exact count including you, or choose Unknown / Variable."); }
		this.participants=participants;
		this.strategySource=source(strategy==Strategy.UNKNOWN,strategySource);
		this.groupSource=source(group==Group.UNKNOWN,groupSource);
		this.countSource=source(participation==Participation.UNKNOWN,countSource);
	}
	private static Source source(boolean unknown,Source source)
	{
		Objects.requireNonNull(source);
		if(!unknown && source==Source.UNKNOWN) { throw new IllegalArgumentException("Known values need provenance"); }
		return unknown?Source.UNKNOWN:source;
	}
	public NexContext inherited()
	{ return new NexContext(strategy,group,participation,participants,Source.INHERITED,Source.INHERITED,Source.INHERITED); }
	public boolean confirmed()
	{ return strategy!=Strategy.UNKNOWN && group!=Group.UNKNOWN && participation==Participation.EXACT
		&& strategySource==Source.ANNOTATED && groupSource==Source.ANNOTATED && countSource==Source.ANNOTATED; }
	public boolean pilot()
	{ return confirmed() && strategy==Strategy.MP2 && group==Group.ORGANIZED && participants==3; }
	public String label()
	{ return strategy+" / "+group+" / "+(participation==Participation.EXACT?participants+" participants":participation+" participants"); }
	public String provenance()
	{ return "Strategy: "+strategySource+"; group: "+groupSource+"; participation: "+countSource; }
	@Override public boolean equals(Object o)
	{
		if(!(o instanceof NexContext)) { return false; } NexContext c=(NexContext)o;
		return strategy==c.strategy && group==c.group && participation==c.participation && Objects.equals(participants,c.participants)
			&& strategySource==c.strategySource && groupSource==c.groupSource && countSource==c.countSource;
	}
	@Override public int hashCode() { return Objects.hash(strategy,group,participation,participants,strategySource,groupSource,countSource); }
	@Override public String toString() { return label()+"; "+provenance(); }
}
