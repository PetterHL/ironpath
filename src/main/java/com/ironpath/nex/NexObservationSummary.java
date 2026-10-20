package com.ironpath.nex;

import java.util.*;

/** Fixed-size, allowlisted worker projection of the whole retained capture, independent of event pages. */
public final class NexObservationSummary
{
	public static final int MAX_CHARS=7000,MAX_ANIMATIONS=12;
	private enum Target { NEX,FUMUS,UMBRA,CRUOR,GLACIES,UNKNOWN }
	private static final class HealthGroup
	{
		int count;
		NexObservations.Health first,last;
		final Set<NexTargetState.Form> forms=EnumSet.noneOf(NexTargetState.Form.class);
		void add(NexObservations.Health reading)
		{ if(first==null) { first=reading; } last=reading; count++; forms.add(reading.targetState.form); }
	}
	private NexObservationSummary() { }
	public static List<String> from(NexSummary summary)
	{
		var o=summary.observations; List<String> out=new ArrayList<>();
		out.add("Whole-capture observation summary; independent of selected event window; capture truncated="+o.truncated);
		Map<NexObservations.HudCheck,Integer> checks=new EnumMap<>(NexObservations.HudCheck.class);
		for(var check:NexObservations.HudCheck.values()) { checks.put(check,o.hudChecks.getOrDefault(check,0)); }
		out.add("HUD checks="+checks+"; per-counter limit="+NexObservations.MAX_HUD_COUNT+"; at limit="+checks.containsValue(NexObservations.MAX_HUD_COUNT));
		out.add("HUD counters cover active-capture callbacks and the first failed binding guard. Zero accepted callbacks do not establish Nex HUD availability; accepted callbacks may have omitted HP rows after capture truncation.");
		Map<Target,Map<NexHealth.Source,HealthGroup>> groups=new EnumMap<>(Target.class);
		for(var reading:o.health)
		{
			Target target=target(reading.target); var source=reading.targetState.health.source;
			groups.computeIfAbsent(target,k->new EnumMap<>(NexHealth.Source.class)).computeIfAbsent(source,k->new HealthGroup()).add(reading);
		}
		for(var target:groups.entrySet()) for(var source:target.getValue().entrySet())
		{
			var group=source.getValue();
			out.add("Health target="+target.getKey()+"; source="+source.getKey()+"; retained readings="+group.count+"; forms="+group.forms
				+"; first="+health(group.first,summary.startTick)+"; last="+health(group.last,summary.startTick));
		}
		Map<String,Integer> animations=new TreeMap<>();
		for(var launch:o.launches)
		{ animations.merge("weapon="+launch.weapon+"; animation="+launch.sample.animation+"; kind="+launch.attack,1,Integer::sum); }
		var ordered=new ArrayList<>(animations.entrySet()); ordered.sort(Map.Entry.<String,Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()));
		for(int i=0;i<Math.min(MAX_ANIMATIONS,ordered.size());i++)
		{ var animation=ordered.get(i); out.add("Animation candidates: "+animation.getKey()+"; callbacks="+animation.getValue()); }
		out.add("Animation groups omitted="+Math.max(0,ordered.size()-MAX_ANIMATIONS)+"; callback counts are not attack/spec counts. HP values remain source-labelled readings, not a simulated trajectory or validated impact HP.");
		List<String> bounded=new ArrayList<>(); int chars=0,omitted=0;
		for(String line:out) { if(chars+line.length()+1>MAX_CHARS-80) { omitted++; } else { bounded.add(line); chars+=line.length()+1; } }
		bounded.add("Observation summary fields omitted="+omitted);
		return List.copyOf(bounded);
	}
	private static String health(NexObservations.Health reading,int start)
	{
		var hp=reading.targetState.health;
		return "tick "+(reading.tick-start)+", bounds "+hp.lower+".."+hp.upper+"/"+hp.maximum+", "+hp.validity;
	}
	private static Target target(String value)
	{
		if(value!=null) for(var target:Target.values()) { if(value.split("@",2)[0].equals(target.name())) { return target; } }
		return Target.UNKNOWN;
	}
}
