package com.ironpath.nex;

import java.util.*;

/** Bounded scalar action evidence. No chat text, menu intent, actors or item names. */
public final class NexActions
{
	public static final String VERSION="nex-actions-2";
	public static final int MAX=4096, PRE_TICKS=10, PRE_MAX=128;
	public enum Kind { ANIMATION, SETTLED, INTERACTION, FOOD_SHARK, FOOD_MANTA_RAY, FOOD_KARAMBWAN, FOOD_UNKNOWN, DC_NORMAL, DC_UPGRADED }
	public enum Source { LOCAL_ANIMATION, GAME_TICK, LOCAL_INTERACTION, GAME_MESSAGE, SPAM_MESSAGE, ENGINE_MESSAGE }
	public static final class Signal
	{
		public final Kind kind;
		public final Source source;
		public final int tick,cycle,animation,frame;
		public final long order;
		public final boolean moving;
		public final NexSummary.Sample sample;
		public Signal(Kind kind,Source source,int tick,int cycle,long order,int animation,int frame,boolean moving,NexSummary.Sample sample)
		{
			this.kind=Objects.requireNonNull(kind); this.source=Objects.requireNonNull(source);
			this.tick=tick; this.cycle=cycle; this.order=order; this.animation=animation; this.frame=frame;
			this.moving=moving; this.sample=Objects.requireNonNull(sample);
		}
	}
	public static final NexActions EMPTY=new NexActions(List.of(),false);
	public final List<Signal> signals;
	public final boolean truncated;
	public NexActions(List<Signal> signals,boolean truncated)
	{
		if(signals.size()>MAX) { throw new IllegalArgumentException("Bounded action evidence required"); }
		this.signals=List.copyOf(signals); this.truncated=truncated;
	}
	/** Exact, independently authored pilot allowlist after markup normalization by the adapter. */
	public static Kind message(String text)
	{
		switch(text)
		{
			case "You eat the shark.": return Kind.FOOD_SHARK;
			case "You eat the manta ray.": return Kind.FOOD_MANTA_RAY;
			case "You eat the karambwan.": return Kind.FOOD_KARAMBWAN;
			case "You eat the food.": return Kind.FOOD_UNKNOWN;
			case "Upon the death of your next foe, some of your special attack energy will be restored.": return Kind.DC_NORMAL;
			case "Upon the death of your next two foes, some of your special attack energy will be restored.": return Kind.DC_UPGRADED;
			default: return text.startsWith("You eat ") && text.endsWith(".")?Kind.FOOD_UNKNOWN:null;
		}
	}
}
