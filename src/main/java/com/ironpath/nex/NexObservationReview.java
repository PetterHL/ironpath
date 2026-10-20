package com.ironpath.nex;

import java.util.*;

/** Worker-side correlation of immutable evidence. All matches remain candidates pending client checks. */
public final class NexObservationReview
{
	public enum Reconciliation { NO_SETTLED_READING, SAME_INPUTS, INPUTS_CHANGED, TARGET_CHANGED }
	public static final class Match
	{
		public final NexObservations.Impact impact;
		public final List<NexObservations.Launch> candidates;
		private Match(NexObservations.Impact impact,List<NexObservations.Launch> candidates)
		{ this.impact=impact; this.candidates=List.copyOf(candidates); }
	}
	public final List<Match> matches;
	public final Map<NexObservations.Launch,Reconciliation> reconciled;
	public final NexSummary.Sample startResources;
	public final Integer startEnergy;
	/** Food units and potion doses at the smoke callback, never inferred consumption. Null means unread. */
	public final Map<NexSupplies.Kind,Integer> startSupplies;
	public final boolean correlationLimited;
	public final int hudReadings,barReadings;
	public final NexObservationWindow window;
	public NexObservationReview(NexSummary summary)
	{
		Map<NexObservations.Launch,Reconciliation> settled=new LinkedHashMap<>();
		for(var launch:summary.observations.launches)
		{
			Reconciliation status=Reconciliation.NO_SETTLED_READING;
			for(var signal:summary.observations.signals)
			{
				if(signal.kind!=NexObservations.Kind.TICK_RESOURCES || signal.order<=launch.order || signal.tick<launch.sample.tick || signal.tick>launch.sample.tick+1 || signal.state==null) { continue; }
				status=!Objects.equals(launch.sample.target,signal.target)?Reconciliation.TARGET_CHANGED
					:sameInputs(launch.sample.state,signal.state)?Reconciliation.SAME_INPUTS:Reconciliation.INPUTS_CHANGED;
				break;
			}
			settled.put(launch,status);
		}
		reconciled=Map.copyOf(settled);
		List<Match> found=new ArrayList<>();
		int work=0; boolean limited=false;
		for(var hit:summary.observations.impacts)
		{
			List<NexObservations.Launch> candidates=new ArrayList<>();
			for(var launch:summary.observations.launches)
			{
				if(++work>1000000) { limited=true; break; }
				if(launch.sample.target==null || !launch.sample.target.equals(hit.target) || launch.order>=hit.order || launch.cycle>hit.cycle) { continue; }
				boolean ranged=launch.weapon==NexObservations.Weapon.TWISTED_BOW || launch.weapon==NexObservations.Weapon.ZARYTE_CROSSBOW;
				// Melee has only a bounded diagnostic window; it is not a calibrated weapon delay.
				boolean possible=!ranged && hit.tick>=launch.sample.tick && hit.tick-launch.sample.tick<=2;
				if(ranged) for(var flight:summary.observations.flights)
				{
					if(++work>1000000) { limited=true; break; }
					if(flight.localSource && Objects.equals(flight.target,hit.target) && Math.abs((long)flight.startCycle-launch.cycle)<=30
						&& Math.abs((long)flight.endCycle-hit.cycle)<=30) { possible=true; }
				}
				if(possible) { candidates.add(launch); }
			}
			found.add(new Match(hit,candidates));
		}
		matches=List.copyOf(found);
		correlationLimited=limited;
		NexSummary.Sample start=null;
		for(var sample:summary.timeline)
		{
			if(sample.tick!=summary.startTick || summary.signals==null || !Objects.equals(summary.signals.smokeTick,summary.startTick)) { continue; }
			if(start==null) { start=sample; }
		}
		startResources=start;
		Integer energy=start==null?null:start.state.specialEnergy;
		startEnergy=energy==null || energy<0 || energy>1000 || !energy.equals(start.state.resources.get("spec-energy-server"))?null:energy;
		Map<NexSupplies.Kind,Integer> supplies=null;
		if(start!=null && start.state.inventory!=null)
		{
			try { supplies=NexSupplies.quantities(start.state.inventory); }
			catch(IllegalArgumentException invalid) { /* Unread/invalid quantities never become zero stock. */ }
		}
		startSupplies=supplies;
		hudReadings=(int)summary.observations.health.stream().filter(s->s.targetState.health.source==NexHealth.Source.BOSS_HUD).count();
		barReadings=(int)summary.observations.health.stream().filter(s->s.targetState.health.source==NexHealth.Source.ACTOR_BAR).count();
		window=new NexObservationWindow(summary,this);
	}
	/** Same target/phase/package/current levels, not worn duration or impact-time equipment. */
	public static boolean sameState(NexSummary.Sample a,NexSummary.Sample b)
	{ return Objects.equals(a.target,b.target) && a.phase.equals(b.phase) && a.window.equals(b.window) && a.state.equals(b.state)
		&& (!"ZAROS".equals(a.phase) || a.targetState.overhead()==b.targetState.overhead()); }
	private static boolean sameInputs(com.ironpath.encounter.CombatState a,com.ironpath.encounter.CombatState b)
	{ return a.slots.equals(b.slots) && a.realLevels.equals(b.realLevels) && a.visibleLevels.equals(b.visibleLevels) && a.prayers.equals(b.prayers) && Objects.equals(a.style,b.style); }
	public static List<NexObservations.Launch> launches(NexSummary summary,NexSummary.Sample sample)
	{
		List<NexObservations.Launch> found=new ArrayList<>();
		for(var launch:summary.observations.launches) { if(sameState(launch.sample,sample)) { found.add(launch); } }
		return List.copyOf(found);
	}
	/** A draft for explicit conditional comparisons at recorded callbacks, never a faster weapon's opportunity schedule. */
	public static List<NexObservations.Launch> planLaunches(NexSummary summary,NexSummary.Sample sample)
	{
		if(summary.observations.truncated) { return List.of(); }
		List<NexObservations.Launch> found=new ArrayList<>(); Set<Integer> ticks=new HashSet<>();
		for(var launch:launches(summary,sample))
		{
			if(launch.attack==NexObservations.Attack.UNCLASSIFIED) { continue; }
			if(!ticks.add(launch.sample.tick) || found.size()>=NexPlanComparison.MAX_OPPORTUNITIES) { return List.of(); }
			found.add(launch);
		}
		return List.copyOf(found);
	}
}
