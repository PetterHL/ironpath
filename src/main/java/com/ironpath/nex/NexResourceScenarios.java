package com.ironpath.nex;

import com.ironpath.encounter.EncounterSummary.Outcome;
import java.util.*;

/** Bounded post-kill examples, never a reconstruction of captured attacks or a weapon recommendation. */
public final class NexResourceScenarios
{
	public static final String VERSION="nex-resource-examples-1";
	public static final String ASSUMPTIONS="Examples assume fixed Lightbearer, attacks ready at offset 0 and an opportunity every listed delay. "
		+"Only natural restoration is included; Death Charge casts/procs and other energy sources are excluded by assumption. "
		+"Ownership, ammunition, target access, ordinary attacks and spell costs are unverified. These are separate hypothetical schedules, not measured uses or executable kill plans.";
	public static final String LIMITS="Ranges vary the first restore offset (0–25 ticks) and before/after attack order. "
		+"They are sensitivity cases, not confidence intervals. No damage, best-weapon, contribution or team-time result follows.";
	public enum Gap
	{
		INCOMPLETE("A complete kill is required."),
		DURATION("The measured duration must be 1–3000 ticks."),
		START_ENERGY("An unambiguous energy reading at the measured start is required."),
		CAPTURE("Continuous fight capture is unestablished."),
		RING("Lightbearer must appear in every recorded fight state; ring-switch timing is unsupported.");
		public final String explanation;
		Gap(String explanation) { this.explanation=explanation; }
	}
	public enum Schedule
	{
		VOIDWAKER("Voidwaker",4,500), ZCB_RAPID("Zaryte crossbow · rapid",5,750), ZCB_STANDARD("Zaryte crossbow · standard",6,750);
		public final String label;
		public final int delay, cost;
		Schedule(String label,int delay,int cost) { this.label=label; this.delay=delay; this.cost=cost; }
		private SpecTimeline.Event event(int tick)
		{ return this==VOIDWAKER?SpecTimeline.Event.voidwaker(tick):SpecTimeline.Event.zaryteCrossbow(tick,this==ZCB_RAPID); }
	}
	public static final class Example
	{
		public final Schedule schedule;
		public final int minUses, maxUses, minFinalEnergy, maxFinalEnergy, minWasted, maxWasted, cases;
		private Example(Schedule schedule,List<SpecTimeline.Result> results)
		{
			this.schedule=schedule; cases=results.size();
			IntSummaryStatistics uses=results.stream().mapToInt(r->r.voidwakerSpecs+r.zaryteSpecs).summaryStatistics();
			IntSummaryStatistics energy=results.stream().mapToInt(r->r.finalEnergy).summaryStatistics();
			IntSummaryStatistics waste=results.stream().mapToInt(r->r.wasted).summaryStatistics();
			minUses=uses.getMin(); maxUses=uses.getMax(); minFinalEnergy=energy.getMin(); maxFinalEnergy=energy.getMax();
			minWasted=waste.getMin(); maxWasted=waste.getMax();
		}
	}
	public final int duration;
	public final Integer initialEnergy;
	public final Set<Gap> gaps;
	public final List<Example> examples;
	private NexResourceScenarios(NexSummary summary,NexResourceReview review)
	{
		long ticks=(long)summary.endTick-summary.startTick;
		duration=ticks<1 || ticks>SpecTimeline.MAX_TICKS?0:(int)ticks;
		EnumSet<Gap> missing=EnumSet.noneOf(Gap.class);
		if(summary.outcome!=Outcome.COMPLETE) { missing.add(Gap.INCOMPLETE); }
		if(duration==0) { missing.add(Gap.DURATION); }
		Integer energy=null; boolean start=false, ambiguous=false;
		for(NexResourceReview.Point point:review.points)
		{
			if(point.tick!=summary.startTick) { continue; }
			if(!start) { energy=point.energy; start=true; }
			if(point.energy==null || !Objects.equals(energy,point.energy)) { ambiguous=true; }
		}
		initialEnergy=start && !ambiguous?energy:null;
		if(initialEnergy==null) { missing.add(Gap.START_ENERGY); }
		if(summary.signals==null || !summary.signals.continuous) { missing.add(Gap.CAPTURE); }
		if(!review.fixedLightbearerObserved) { missing.add(Gap.RING); }
		gaps=Collections.unmodifiableSet(missing);
		List<Example> computed=new ArrayList<>();
		if(missing.isEmpty())
		{
			SpecTimeline model=new SpecTimeline();
			for(Schedule schedule:Schedule.values())
			{
				List<SpecTimeline.Event> events=new ArrayList<>();
				for(int tick=0;tick<duration;tick+=schedule.delay) { events.add(schedule.event(tick)); }
				computed.add(new Example(schedule,model.unknownRestorationPhase(initialEnergy,duration,SpecTimeline.Ring.LIGHTBEARER,events)));
			}
		}
		examples=List.copyOf(computed);
	}
	static NexResourceScenarios from(NexSummary summary,NexResourceReview review)
	{ return new NexResourceScenarios(summary,review); }
}
