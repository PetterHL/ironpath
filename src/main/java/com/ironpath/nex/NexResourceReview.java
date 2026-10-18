package com.ironpath.nex;

import java.util.*;

/** Sampled public resource changes. No energy delta is attributed to an attack, cast or proc. */
public final class NexResourceReview
{
	public static final class Point
	{
		public final int tick;
		public final Integer energy, activeCharges, cooldown, ring;
		private Point(NexSummary.Sample sample)
		{
			tick=sample.tick; energy=bounded(sample.state.specialEnergy,SpecTimeline.CAP);
			activeCharges=bounded(sample.state.resources.get("death-charge-active"),2);
			cooldown=bounded(sample.state.resources.get("death-charge-cooldown"),1);
			Integer id=sample.state.slots.get("ring"); ring=id==null || id<0?null:id;
		}
		private boolean sameResources(Point p)
		{ return Objects.equals(energy,p.energy) && Objects.equals(activeCharges,p.activeCharges)
			&& Objects.equals(cooldown,p.cooldown) && Objects.equals(ring,p.ring); }
	}
	public final long summaryKey;
	public final List<Point> points;
	public final Integer firstEnergy, lastEnergy;
	public final int sampledIncreases, sampledDecreases;
	public final boolean improvedActiveObserved, fixedLightbearerObserved;
	public final Set<String> gaps;
	public final NexResourceScenarios scenarios;
	private NexResourceReview(NexSummary summary)
	{
		summaryKey=summary.key;
		List<Point> changes=new ArrayList<>(); Set<String> missing=new LinkedHashSet<>();
		Point previous=null, first=null, last=null;
		int increases=0,decreases=0; boolean improved=false,lightbearer=true;
		for(NexSummary.Sample sample:summary.timeline)
		{
			if(sample.tick<summary.startTick || sample.tick>summary.endTick) { continue; }
			Point p=new Point(sample);
			if(previous!=null && p.tick<previous.tick) { throw new IllegalArgumentException("Resource samples out of order"); }
			if(first==null) { first=p; }
			last=p;
			if(p.energy==null) { missing.add("Some energy readings are unavailable; changes across them are not counted."); }
			if(p.activeCharges==null || p.cooldown==null) { missing.add("Some Death Charge readings are unavailable."); }
			if(p.ring==null) { missing.add("Ring state is unread in part of the capture."); }
			lightbearer &= Objects.equals(p.ring,25975);
			improved |= Objects.equals(p.activeCharges,2);
			if(previous!=null && previous.energy!=null && p.energy!=null)
			{
				int delta=p.energy-previous.energy;
				increases+=Math.max(0,delta); decreases+=Math.max(0,-delta);
			}
			if(previous==null || !p.sameResources(previous)) { changes.add(p); }
			previous=p;
		}
		if(first==null) { missing.add("No fight resource samples are available."); }
		else if(first.tick!=summary.startTick) { missing.add("The first resource sample is later than the measured start."); }
		if(summary.signals==null || !summary.signals.continuous)
		{ missing.add("Continuous capture is unestablished; sample changes do not cover every resource event."); }
		firstEnergy=first==null?null:first.energy; lastEnergy=last==null?null:last.energy;
		points=List.copyOf(changes); gaps=Collections.unmodifiableSet(missing);
		sampledIncreases=increases; sampledDecreases=decreases;
		improvedActiveObserved=improved; fixedLightbearerObserved=first!=null && lightbearer;
		scenarios=NexResourceScenarios.from(summary,this);
	}
	public static NexResourceReview from(NexSummary summary) { return new NexResourceReview(Objects.requireNonNull(summary)); }
	private static Integer bounded(Integer value,int max) { return value==null || value<0 || value>max?null:value; }
}
