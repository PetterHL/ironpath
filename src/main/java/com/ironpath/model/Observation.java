package com.ironpath.model;

import java.time.Instant;
import java.util.Objects;

/** Unknown has no value, timestamp or invented provenance. */
public final class Observation<T>
{
	private final T value;
	private final Instant time;
	private final String source;

	private Observation(T value, Instant time, String source)
	{
		this.value = value;
		this.time = time;
		this.source = source;
	}

	public static <T> Observation<T> unknown()
	{
		return new Observation<>(null, null, null);
	}

	public static <T> Observation<T> observed(T value, Instant time, String source)
	{
		return new Observation<>(Objects.requireNonNull(value), Objects.requireNonNull(time),
			Objects.requireNonNull(source));
	}

	public boolean known() { return value != null; }
	public T value() { return value; }
	public Instant time() { return time; }
	public String source() { return source; }
	public String evidence() { return known() ? source + " at " + time : "unknown"; }
}
