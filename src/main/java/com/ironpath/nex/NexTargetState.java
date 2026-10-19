package com.ironpath.nex;

import java.util.Objects;

/** Immutable public NPC readings. A health bar is a ratio, never exact target HP or combat stats. */
public final class NexTargetState
{
	public static final String VERSION="nex-target-state-1";
	public enum Form { UNKNOWN, NEX, SPAWNING, SOUL_SPLIT, DEFLECT_MELEE, DYING, MINION }
	public enum Overhead { UNKNOWN, NONE, SOUL_SPLIT, DEFLECT_MELEE }
	public static final NexTargetState UNKNOWN=new NexTargetState(Form.UNKNOWN,null,null);
	public final Form form;
	public final Integer healthRatio,healthScale;
	public NexTargetState(Form form,Integer ratio,Integer scale)
	{
		this.form=Objects.requireNonNull(form);
		boolean readable=ratio!=null && scale!=null && scale>0 && scale<=10000 && ratio>=0 && ratio<=scale;
		healthRatio=readable?ratio:null; healthScale=readable?scale:null;
	}
	/** NPC forms are observed; whether their overhead applies at a hypothetical impact remains declared. */
	public Overhead overhead()
	{
		switch(form)
		{
			case NEX: return Overhead.NONE;
			case SOUL_SPLIT: return Overhead.SOUL_SPLIT;
			case DEFLECT_MELEE: return Overhead.DEFLECT_MELEE;
			default: return Overhead.UNKNOWN;
		}
	}
	public String reading()
	{ return "form="+form+"; overhead="+overhead()+"; health ratio/scale="+healthRatio+"/"+healthScale+"; exact HP and target levels unknown"; }
	@Override public boolean equals(Object other)
	{
		if(!(other instanceof NexTargetState)) { return false; }
		NexTargetState s=(NexTargetState)other;
		return form==s.form && Objects.equals(healthRatio,s.healthRatio) && Objects.equals(healthScale,s.healthScale);
	}
	@Override public int hashCode() { return Objects.hash(form,healthRatio,healthScale); }
}
