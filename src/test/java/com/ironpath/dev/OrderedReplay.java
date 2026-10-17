package com.ironpath.dev;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Consumer;

/** Development-only ordered callbacks. No encounter-specific phase or summary types. */
public final class OrderedReplay
{
	private final List<Step> steps = new ArrayList<>();
	private int lastTick = -1;

	public void at(int tick, String label, Runnable callback)
	{
		if (tick < lastTick) { throw new IllegalArgumentException("Replay ticks must be ordered"); }
		lastTick = tick;
		steps.add(new Step(tick, label, callback));
	}

	/** Equal-tick callbacks execute in insertion order, with state set by each callback. */
	public void run(IntConsumer clock, Consumer<String> trace)
	{
		for (Step step : steps)
		{
			clock.accept(step.tick);
			trace.accept("tick " + step.tick + " · " + step.label);
			step.callback.run();
		}
	}

	private static final class Step
	{
		final int tick;
		final String label;
		final Runnable callback;
		Step(int tick, String label, Runnable callback)
		{ this.tick = tick; this.label = label; this.callback = callback; }
	}
}
