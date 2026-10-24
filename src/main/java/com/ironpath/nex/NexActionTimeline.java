package com.ironpath.nex;

import java.util.*;
import static com.ironpath.nex.NexActions.Kind.*;

/** Worker-only, provisional Fang clock. Availability never creates an attack or missed opportunity. */
public final class NexActionTimeline
{
	public static final String VERSION="nex-fang-clock-1";
	public enum Confidence { OBSERVED_SIGNAL, PROVISIONAL_INFERENCE, EXCLUDED, AMBIGUOUS }
	public enum Reason { MESSAGE, NO_ATTACK_SIGNAL, CONTINUING_ANIMATION, ANIMATION_CALLBACK, FRAME_RESTART,
		UNSUPPORTED_WEAPON_STYLE, SPECIAL_SELECTED, NO_TARGET, MOVING, COOLDOWN, REPEATED_SIGNAL,
		NO_SETTLED_STATE, SETTLED_INPUTS_CHANGED, SAME_TICK_FOOD, MISSING_TICKS, OUT_OF_ORDER }
	public enum Readiness { UNKNOWN, COOLDOWN, AVAILABLE }
	/** Scoped sample exclusions are independent of the clock and the full-plan resource checklist. */
	public enum SampleGap { MAPPING_UNVALIDATED, UNKNOWN_PHASE, UNSUPPORTED_TARGET_FORM, UNSUPPORTED_WINDOW }
	public static final class Entry
	{
		public final NexActions.Signal signal;
		public final Confidence confidence;
		public final Reason reason;
		public final Integer readyBefore,readyAfter;
		public final Readiness readiness;
		public final Long settledOrder;
		public final SampleGap sampleGap;
		Entry(NexActions.Signal s,Confidence confidence,Reason reason,Integer before,Integer after,Long settledOrder)
		{
			this.signal=s; sampleGap=sampleGap(s.sample); this.confidence=confidence; this.reason=reason; readyBefore=before; readyAfter=after;
			readiness=before==null?Readiness.UNKNOWN:s.tick<before?Readiness.COOLDOWN:Readiness.AVAILABLE; this.settledOrder=settledOrder;
		}
		public boolean attack() { return confidence==Confidence.PROVISIONAL_INFERENCE; }
	}
	public static final class ImpactLink
	{
		public final NexObservations.Impact impact;
		public final List<Long> candidateOrders;
		public final boolean ambiguous;
		ImpactLink(NexObservations.Impact impact,List<Long> candidates,boolean ambiguous)
		{ this.impact=impact; candidateOrders=List.copyOf(candidates); this.ambiguous=ambiguous; }
	}
	public final List<Entry> entries;
	public final List<ImpactLink> impacts;
	public final boolean truncated;
	public NexActionTimeline(NexSummary summary)
	{
		var signals=summary.actions.signals;
		List<Entry> rows=new ArrayList<>();
		Integer ready=null; int lastAttack=Integer.MIN_VALUE/2, lastTick=-1, lastFrame=-1, lastAnimation=-1;
		long lastOrder=-1; NexActions.Signal prior=null, lastSettled=null;
		Set<Integer> foodTicks=new HashSet<>();
		for(var s:signals) { if(food(s.kind)) { foodTicks.add(s.tick); } }
		for(int i=0;i<signals.size();i++)
		{
			var s=signals.get(i); Integer before=ready; Long settledOrder=null;
			Reason reason=Reason.NO_ATTACK_SIGNAL; Confidence confidence=Confidence.OBSERVED_SIGNAL;
			boolean ordered=s.tick>=lastTick && s.order>lastOrder;
			boolean gap=lastTick>=0 && s.tick>lastTick+1;
			if(!ordered || gap || prior!=null && !sameWeaponStyle(prior,s)) { ready=null; }
			before=ready;
			if(!ordered) { reason=Reason.OUT_OF_ORDER; confidence=Confidence.EXCLUDED; }
			else if(food(s.kind))
			{
				reason=Reason.MESSAGE;
				if(s.kind==FOOD_UNKNOWN) { ready=null; }
				else if(ready!=null && s.tick<ready) { ready+=s.kind==FOOD_SHARK?3:2; }
			}
			else if(s.kind==DC_NORMAL || s.kind==DC_UPGRADED) { reason=Reason.MESSAGE; }
			else
			{
				boolean callback=s.kind==ANIMATION && s.animation==9471;
				boolean restart=s.kind==SETTLED && s.animation==9471 && lastAnimation==9471 && s.frame>=0 && lastFrame>=0 && s.frame<lastFrame
					&& s.tick!=lastAttack && lastSettled!=null && s.tick==lastSettled.tick+1 && sameInputs(lastSettled,s);
				if(callback || restart)
				{
					reason=callback?Reason.ANIMATION_CALLBACK:Reason.FRAME_RESTART;
					NexActions.Signal settled=null;
					if(s.kind==SETTLED) { settled=s; }
					else for(int j=i+1;j<signals.size();j++)
					{
						var next=signals.get(j); if(next.tick>s.tick+1) { break; }
						if(next.kind==SETTLED && next.tick>=s.tick) { settled=next; break; }
					}
					if(settled!=null) { settledOrder=settled.order; }
					if(gap) { reason=Reason.MISSING_TICKS; }
					else if(!fang(s) || s.sample.state.style==null || !Set.of(0,1,3).contains(s.sample.state.style)) { reason=Reason.UNSUPPORTED_WEAPON_STYLE; }
					else if(s.sample.state.specialSelected) { reason=Reason.SPECIAL_SELECTED; }
					else if(s.sample.target==null) { reason=Reason.NO_TARGET; }
					else if(s.moving) { reason=Reason.MOVING; }
					else if(s.tick==lastAttack) { reason=Reason.REPEATED_SIGNAL; }
					else if(foodTicks.contains(s.tick)) { reason=Reason.SAME_TICK_FOOD; }
					else if(ready!=null && s.tick<ready) { reason=Reason.COOLDOWN; }
					else if(settled==null) { reason=Reason.NO_SETTLED_STATE; }
					else if(!sameInputs(s,settled)) { reason=Reason.SETTLED_INPUTS_CHANGED; }
					if(reason==Reason.ANIMATION_CALLBACK || reason==Reason.FRAME_RESTART)
					{ confidence=Confidence.PROVISIONAL_INFERENCE; ready=s.tick+5; lastAttack=s.tick; }
					else
					{
						confidence=Set.of(Reason.NO_SETTLED_STATE,Reason.SETTLED_INPUTS_CHANGED,Reason.SAME_TICK_FOOD).contains(reason)?Confidence.AMBIGUOUS:Confidence.EXCLUDED;
						if(reason!=Reason.REPEATED_SIGNAL && reason!=Reason.COOLDOWN) { ready=null; }
					}
				}
				else if(s.animation==9471 && s.kind==SETTLED) { reason=Reason.CONTINUING_ANIMATION; }
			}
			rows.add(new Entry(s,confidence,reason,before,ready,settledOrder));
			if(s.kind==SETTLED) { lastAnimation=s.animation; lastFrame=s.frame; lastSettled=s; }
			lastTick=s.tick; lastOrder=s.order; prior=s;
		}
		entries=List.copyOf(rows); truncated=summary.actions.truncated || summary.observations.truncated;
		List<ImpactLink> links=new ArrayList<>();
		for(var hit:summary.observations.impacts)
		{
			List<Long> candidates=new ArrayList<>(); boolean competing=false;
			for(var row:entries)
			{
				var s=row.signal;
				if(s.tick<=hit.tick && hit.tick-s.tick<=2 && s.order<hit.order && Objects.equals(s.sample.target,hit.target)
					&& s.animation==9471 && (s.kind==ANIMATION || row.reason==Reason.FRAME_RESTART))
				{ if(row.attack()) { candidates.add(s.order); } else if(row.reason!=Reason.REPEATED_SIGNAL) { competing=true; } }
			}
			for(var launch:summary.observations.launches)
			{
				if(launch.sample.tick<=hit.tick && hit.tick-launch.sample.tick<=2 && launch.order<hit.order && Objects.equals(launch.sample.target,hit.target)
					&& (launch.weapon!=NexObservations.Weapon.FANG || launch.attack!=NexObservations.Attack.ORDINARY_CANDIDATE)) { competing=true; }
			}
			links.add(new ImpactLink(hit,candidates,competing || candidates.size()!=1 || truncated));
		}
		// Even a unique temporal candidate is not proof of hit ownership. Multiple hits per launch stay ambiguous.
		Map<Long,Integer> counts=new HashMap<>(); links.forEach(l->l.candidateOrders.forEach(k->counts.merge(k,1,Integer::sum)));
		List<ImpactLink> finalLinks=new ArrayList<>();
		for(var link:links) { finalLinks.add(new ImpactLink(link.impact,link.candidateOrders,link.ambiguous || link.candidateOrders.stream().anyMatch(k->counts.get(k)>1))); }
		impacts=List.copyOf(finalLinks);
	}
	private static SampleGap sampleGap(NexSummary.Sample sample)
	{
		if(!Set.of("SMOKE","SHADOW","BLOOD","ICE","ZAROS").contains(sample.phase)) { return SampleGap.UNKNOWN_PHASE; }
		if(sample.targetState.form!=NexTargetState.Form.NEX) { return SampleGap.UNSUPPORTED_TARGET_FORM; }
		if(sample.phase.equals("ZAROS") || !Set.of("UNKNOWN","NONE","FEAR_THE_SHADOW").contains(sample.window)) { return SampleGap.UNSUPPORTED_WINDOW; }
		return SampleGap.MAPPING_UNVALIDATED;
	}

	private static boolean food(NexActions.Kind k) { return k==FOOD_SHARK || k==FOOD_KARAMBWAN || k==FOOD_UNKNOWN; }
	private static boolean fang(NexActions.Signal s) { return Set.of(26219,27246).contains(s.sample.state.slots.getOrDefault("weapon",-1)); }
	private static boolean sameWeaponStyle(NexActions.Signal a,NexActions.Signal b)
	{ return Objects.equals(a.sample.state.slots.get("weapon"),b.sample.state.slots.get("weapon")) && Objects.equals(a.sample.state.style,b.sample.state.style); }
	private static boolean sameInputs(NexActions.Signal a,NexActions.Signal b)
	{
		var x=a.sample.state; var y=b.sample.state;
		return Objects.equals(a.sample.target,b.sample.target) && a.sample.phase.equals(b.sample.phase) && a.sample.window.equals(b.sample.window)
			&& a.sample.targetState.form==b.sample.targetState.form && x.slots.equals(y.slots) && Objects.equals(x.style,y.style)
			&& x.visibleLevels.equals(y.visibleLevels) && x.realLevels.equals(y.realLevels) && x.prayers.equals(y.prayers) && x.specialSelected==y.specialSelected;
	}
}
