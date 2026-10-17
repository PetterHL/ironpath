package com.ironpath.dev;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Explicit callback scheduling for offline scenarios. No encounter or client dependencies. */
public final class ReplayQueue
{
	private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
	public void add(Runnable task) { tasks.add(task); }
	public int size() { return tasks.size(); }
	public Runnable take()
	{
		Runnable task = tasks.poll();
		if (task == null) { throw new IllegalStateException("Expected queued replay work"); }
		return task;
	}
	public void drain()
	{
		for (int i = 0; !tasks.isEmpty(); i++)
		{
			if (i == 1000) { throw new IllegalStateException("Replay queue failed to settle"); }
			take().run();
		}
	}
}
