package com.ironpath.nex;

import java.util.Objects;

/** A timed, spawn-scoped HP reading. HUD availability is separate from Nex client acceptance. */
public final class NexHealth
{
	public enum Source { UNKNOWN, ACTOR_BAR, BOSS_HUD }
	public enum Validity { UNKNOWN, BOUNDED, AWAITING_CLIENT_VALIDATION }
	public static final NexHealth UNKNOWN=new NexHealth(Source.UNKNOWN,Validity.UNKNOWN,null,null,null,-1,-1,null,-1,-1);
	public final Source source;
	public final Validity validity;
	public final Integer lower,upper,maximum;
	public final int tick,cycle,definition,worldView;
	/** Memory only. Reports project a target class and report-local reference instead. */
	public final String target;
	private NexHealth(Source source,Validity validity,Integer lower,Integer upper,Integer maximum,int tick,int cycle,String target,int definition,int worldView)
	{
		this.source=source; this.validity=validity; this.lower=lower; this.upper=upper; this.maximum=maximum;
		this.tick=tick; this.cycle=cycle; this.target=target; this.definition=definition; this.worldView=worldView;
	}
	/** Maximum must be independently justified for this exact target; never estimated from bar width. */
	public static NexHealth bar(int ratio,int scale,int maximum,int tick,int cycle,String target,int definition,int worldView)
	{
		if(target==null || maximum<1 || maximum>100000 || scale<1 || scale>10000 || ratio<0 || ratio>scale) { return UNKNOWN; }
		int lo,hi;
		if(ratio==0) { lo=hi=0; }
		else if(scale==1) { lo=1; hi=maximum; }
		else
		{
			lo=ratio==1?1:(int)(((long)maximum*(ratio-1)+scale-2)/(scale-1));
			hi=Math.min(maximum,(int)(((long)maximum*ratio+scale-2)/(scale-1))-1);
			// The full bar includes the exact maximum.
			if(ratio==scale) { lo=hi=maximum; }
		}
		if(lo>hi || lo<0) { return UNKNOWN; }
		return new NexHealth(Source.ACTOR_BAR,Validity.BOUNDED,lo,hi,maximum,tick,cycle,target,definition,worldView);
	}
	/** Adapter has checked a fresh HUD callback, unique tracked actor, definition and server agreement.
	 * This is deliberately still a candidate until Nex-specific live timing/identity checks pass. */
	public static NexHealth hud(int hp,int maximum,int tick,int cycle,String target,int definition,int worldView)
	{
		if(target==null || maximum<1 || maximum>100000 || hp<0 || hp>maximum) { return UNKNOWN; }
		return new NexHealth(Source.BOSS_HUD,Validity.AWAITING_CLIENT_VALIDATION,hp,hp,maximum,tick,cycle,target,definition,worldView);
	}
	public String reading()
	{ return "HP source="+source+"; validity="+validity+"; bounds="+lower+".."+upper+"; maximum="+maximum; }
	/** Time remains on the retained reading; unchanged values need not consume another worn-state slot. */
	@Override public boolean equals(Object other)
	{
		if(!(other instanceof NexHealth)) { return false; }
		NexHealth h=(NexHealth)other;
		return source==h.source && validity==h.validity && Objects.equals(lower,h.lower) && Objects.equals(upper,h.upper)
			&& Objects.equals(maximum,h.maximum) && Objects.equals(target,h.target) && definition==h.definition && worldView==h.worldView;
	}
	@Override public int hashCode() { return Objects.hash(source,validity,lower,upper,maximum,target,definition,worldView); }
}
