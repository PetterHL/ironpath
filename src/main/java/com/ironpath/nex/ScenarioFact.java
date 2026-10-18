package com.ironpath.nex;

import java.time.Instant;
import java.util.*;

/** Explicit scenario evidence. Assumptions never become observations; unknown never has a value. */
public final class ScenarioFact<T>
{
	public enum Origin { OBSERVED, ASSUMED, UNKNOWN }
	public final Origin origin;
	public final String source, scope;
	public final Instant time;
	private final T value;
	private ScenarioFact(Origin origin,T value,String source,String scope,Instant time)
	{
		if(source==null || source.isBlank() || scope==null || scope.isBlank())
		{ throw new IllegalArgumentException("Scenario source and scope required, including unknown inputs"); }
		this.origin=origin; this.value=value; this.source=source; this.scope=scope; this.time=time;
	}
	public static <T> ScenarioFact<T> observed(T value,String source,String scope,Instant time)
	{ return known(Origin.OBSERVED,value,source,scope,time); }
	public static <T> ScenarioFact<T> assumed(T value,String source,String scope,Instant time)
	{ return known(Origin.ASSUMED,value,source,scope,time); }
	private static <T> ScenarioFact<T> known(Origin origin,T value,String source,String scope,Instant time)
	{
		Objects.requireNonNull(value); Objects.requireNonNull(time);
		return new ScenarioFact<>(origin,value,source,scope,time);
	}
	/** Domain constructors freeze collection inputs while preserving their evidence and interface type. */
	static <K,V> ScenarioFact<Map<K,V>> freezeMap(ScenarioFact<Map<K,V>> fact)
	{
		return fact.known()?new ScenarioFact<>(fact.origin,Map.copyOf(fact.value),fact.source,fact.scope,fact.time):fact;
	}
	public static <T> ScenarioFact<T> unknown(String reason,String scope)
	{ return new ScenarioFact<>(Origin.UNKNOWN,null,reason,scope,null); }
	public boolean known() { return origin!=Origin.UNKNOWN; }
	public T value()
	{
		if(!known()) { throw new IllegalStateException("Unknown "+scope+": "+source); }
		return value;
	}
	public String evidence() { return origin+" · "+scope+" · "+source+(time==null?"":" · "+time); }
}
