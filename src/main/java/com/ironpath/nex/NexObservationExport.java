package com.ironpath.nex;

import com.google.gson.*;
import com.ironpath.advisor.PoweredMagic;
import com.ironpath.encounter.CombatState;
import java.util.*;

/** Explicit evidence projection, never reflective serialization of account/capture objects. */
public final class NexObservationExport
{
	private final NexSummary summary;
	private final Map<String,String> targets=new LinkedHashMap<>();
	private final Map<JsonObject,Integer> states=new LinkedHashMap<>();
	private final List<JsonObject> events=new ArrayList<>();
	private long baseOrder;
	private int baseCycle;
	private NexObservationExport(NexSummary summary) { this.summary=summary; }
	public static String from(NexSummary summary,NexObservationReview review)
	{ return from(summary,review,com.ironpath.advisor.NexFangAdvice.unavailable(summary)); }
	public static String from(NexSummary summary,NexObservationReview review,com.ironpath.advisor.NexFangAdvice advice)
	{ return new NexObservationExport(summary).build(review,advice).toString(); }
	private JsonObject build(NexObservationReview review,com.ironpath.advisor.NexFangAdvice advice)
	{
		var o=summary.observations;
		baseOrder=Long.MAX_VALUE;
		for(var l:o.launches) { origin(l.order,l.cycle); }
		for(var s:o.signals) { origin(s.order,s.cycle); }
		for(var f:o.flights) { origin(f.order,f.cycle); }
		for(var h:o.impacts) { origin(h.order,h.cycle); }
		for(var h:o.health) { origin(h.order,h.cycle); }
		for(var a:summary.actions.signals) { origin(a.order,a.cycle); }
		for(int i=0;i<o.launches.size();i++)
		{
			var l=o.launches.get(i); var e=event("LAUNCH",l.sample.tick,l.cycle,l.order,l.sample.target);
			e.addProperty("id","L"+(i+1)); e.addProperty("weapon",l.weapon.name()); e.addProperty("attack",l.attack.name());
			e.addProperty("reconciliation",review.reconciled.get(l).name()); sample(e,l.sample); events.add(e);
		}
		for(var s:o.signals)
		{
			var e=event(s.kind.name(),s.tick,s.cycle,s.order,s.target);
			e.addProperty("value",s.value); e.addProperty("serverValue",s.serverValue);
			if(s.state!=null) { e.addProperty("state",state(s.state)); } events.add(e);
		}
		for(int i=0;i<o.flights.size();i++)
		{
			var f=o.flights.get(i); var e=event("PROJECTILE",f.tick,f.cycle,f.order,f.target);
			e.addProperty("id","F"+(i+1)); e.addProperty("projectile",f.id); e.addProperty("source",f.source.name());
			e.addProperty("startCycleOffset",(long)f.startCycle-f.cycle); e.addProperty("endCycleOffset",(long)f.endCycle-f.cycle);
			e.add("targetState",targetState(f.targetState)); events.add(e);
		}
		for(int i=0;i<o.impacts.size();i++)
		{
			var h=o.impacts.get(i); var e=event("IMPACT",h.tick,h.cycle,h.order,h.target);
			e.addProperty("id","I"+(i+1)); e.addProperty("amount",h.amount);
			JsonArray candidates=new JsonArray(); var match=review.matches.get(i); int count=0;
			for(var l:match.candidates) { if(count++<NexObservationWindow.MAX_LINKS) { candidates.add("L"+(o.launches.indexOf(l)+1)); } }
			e.add("launchCandidates",candidates); e.addProperty("candidateLinksOmitted",Math.max(0,count-NexObservationWindow.MAX_LINKS)); e.add("targetState",targetState(h.targetState)); events.add(e);
		}
		for(var h:o.health)
		{ var e=event("HEALTH",h.tick,h.cycle,h.order,h.target); e.add("targetState",targetState(h.targetState)); events.add(e); }
		int actionIndex=0;
		for(var row:review.actions.entries)
		{
			var a=row.signal; var e=event("ACTION_SIGNAL",a.tick,a.cycle,a.order,a.sample.target);
			e.addProperty("actionIndex",actionIndex++);
			e.addProperty("kind",a.kind.name()); e.addProperty("source",a.source.name());
			e.addProperty("sampleGap",row.sampleGap.name()); e.addProperty("confidence",row.confidence.name()); e.addProperty("reason",row.reason.name());
			e.addProperty("frame",a.frame); e.addProperty("moving",a.moving); e.addProperty("preStart",a.tick<summary.startTick);
			e.addProperty("readinessBefore",row.readiness.name());
			e.addProperty("readyBeforeTick",row.readyBefore==null?null:row.readyBefore-summary.startTick);
			e.addProperty("readyAfterTick",row.readyAfter==null?null:row.readyAfter-summary.startTick);
			// Orders are rebased below with the ordinary evidence; references are rebased after sorting.
			e.addProperty("settledOrder",row.settledOrder); sample(e,a.sample); events.add(e);
		}
		events.sort(Comparator.comparingLong(e->e.get("order").getAsLong()));
		if(!events.isEmpty()) { baseOrder=events.get(0).get("order").getAsLong(); baseCycle=events.get(0).get("cycle").getAsInt(); }
		JsonArray retained=new JsonArray(); JsonObject previousPoll=null; int compressed=0;
		for(var e:events)
		{
			long order=e.get("order").getAsLong(); int cycle=e.get("cycle").getAsInt();
			e.addProperty("order",order-baseOrder); e.addProperty("cycle",(long)cycle-baseCycle);
			if(e.has("settledOrder") && !e.get("settledOrder").isJsonNull()) { e.addProperty("settledOrder",e.get("settledOrder").getAsLong()-baseOrder); }
			if("TICK_RESOURCES".equals(e.get("type").getAsString()))
			{
				JsonObject payload=e.deepCopy(); payload.remove("tick"); payload.remove("cycle"); payload.remove("order");
				if(payload.equals(previousPoll)) { compressed++; continue; }
				previousPoll=payload;
			}
			retained.add(e);
		}
		JsonObject root=new JsonObject(); root.addProperty("schema","nex-evidence-5"); root.addProperty("kill","K1");
		root.add("fangAdvice",advice.json());
		root.addProperty("timing","tick relative to measured start; cycle/order relative to first retained callback; samples have tick only");
		root.addProperty("scope","All retained allowlisted observations; correlations remain unvalidated candidates. Zero impacts are observed zero damage, not inferred attacks. No legal-opportunity or proc inference; separately labeled provisional action-clock inferences follow.");
		root.addProperty("measuredEndTick",summary.endTick-summary.startTick); root.addProperty("captureTruncated",o.truncated);
		root.addProperty("correlationLimited",review.correlationLimited); root.addProperty("retainedCallbacks",events.size());
		root.addProperty("unchangedPollsOmitted",compressed); root.addProperty("exportedCallbacks",retained.size()); root.addProperty("exportOmissions",0); root.addProperty("exportComplete",true);
		root.add("events",retained);
		JsonObject action=new JsonObject(); action.addProperty("model",NexActionTimeline.VERSION); action.addProperty("evidence",NexActions.VERSION);
		action.addProperty("mappingStatus","PROVISIONAL_REQUIRES_CONTROLLED_CLIENT_CHECK");
		action.addProperty("initialReadiness","UNKNOWN"); action.addProperty("truncated",review.actions.truncated);
		action.addProperty("scope","Fang stab animation/cycle candidates only; 5-tick inferred clock, shark/manta ray +3 and karambwan +2 delays. No message is not no consumption. Smoke never resets readiness. Availability is not an executed attack or missed opportunity. Unique temporal hit candidates are not ownership proof. No proc, expiry, finite stock or advice inferred.");
		JsonArray actionImpacts=new JsonArray();
		for(int i=0;i<review.actions.impacts.size();i++)
		{
			var link=review.actions.impacts.get(i); JsonObject e=new JsonObject(); e.addProperty("impact","I"+(i+1));
			JsonArray candidates=new JsonArray(); link.candidateOrders.forEach(k->candidates.add(k-baseOrder)); e.add("actionOrders",candidates);
			e.addProperty("status",link.ambiguous?"AMBIGUOUS_OR_UNMATCHED":"UNIQUE_TEMPORAL_CANDIDATE"); actionImpacts.add(e);
		}
		action.add("impacts",actionImpacts); root.add("actionTimeline",action);
		root.addProperty("damageCoverage","UNVERIFIED");
		root.addProperty("wholeKillContributionPercent",(Number)null);
		root.addProperty("observedLocalDamage",summary.localDamage()); root.addProperty("observedTotalDamage",summary.totalDamage());
		root.addProperty("observedHitsplatRatioPercent",summary.share());
		root.addProperty("callbackCounterScope","Active attempt only; cleared on smoke reanchor; includes rejected and post-death callbacks. Positive payload sums, not fight damage. Object reuse is counted, never rejected. Projectile exclusions and repeat source readings count callbacks, not distinct shots. Repeat source readings never revise first-flight source or enable matching.");
		root.addProperty("callbackCountLimit",NexCallbackDiagnostics.MAX_COUNT); root.addProperty("callbackAmountLimit",NexCallbackDiagnostics.MAX_AMOUNT);
		JsonObject counters=new JsonObject();
		for(var target:NexCallbackDiagnostics.Target.values())
		{
			JsonObject stages=new JsonObject();
			for(var stage:NexCallbackDiagnostics.Stage.values())
			{
				var tally=o.callbacks.get(target,stage); JsonObject entry=new JsonObject();
				entry.addProperty("count",tally.count); entry.addProperty("amount",tally.amount); entry.addProperty("atLimit",tally.atLimit()); stages.add(stage.name(),entry);
			}
			counters.add(target.name(),stages);
		}
		root.add("callbackChecks",counters);
		root.add("samples",samples(summary.timeline)); root.add("beforeStart",samples(summary.beforeStart));
		JsonArray stateTable=new JsonArray(); states.keySet().forEach(stateTable::add); root.add("states",stateTable);
		JsonObject targetTable=new JsonObject(); targets.forEach((raw,ref)->targetTable.addProperty(ref,targetClass(raw))); root.add("targets",targetTable);
		JsonObject checks=new JsonObject(); for(var check:NexObservations.HudCheck.values()) { checks.addProperty(check.name(),o.hudChecks.getOrDefault(check,0)); } root.add("hudChecks",checks);
		return root;
	}
	private void origin(long order,int cycle) { if(order<baseOrder) { baseOrder=order; baseCycle=cycle; } }
	private JsonArray samples(List<NexSummary.Sample> samples)
	{
		JsonArray out=new JsonArray();
		for(var s:samples) { JsonObject e=new JsonObject(); e.addProperty("tick",s.tick-summary.startTick); e.addProperty("target",target(s.target)); sample(e,s); out.add(e); }
		return out;
	}
	private void sample(JsonObject e,NexSummary.Sample s)
	{
		e.addProperty("animation",s.animation); e.addProperty("phase",allowed(s.phase,"SMOKE","SHADOW","BLOOD","ICE","ZAROS"));
		e.addProperty("window",allowed(s.window,"NONE","FEAR_THE_SHADOW","EMBRACE_DARKNESS","BLOOD_SIPHON","BLOOD_SACRIFICE","ICE_CONTAIN","ICE_PRISON","MINION_FUMUS","MINION_UMBRA","MINION_CRUOR","MINION_GLACIES"));
		e.addProperty("role",allowed(s.role,"melee","ranged","spec candidate","unclassified")); e.addProperty("state",state(s.state)); e.add("targetState",targetState(s.targetState));
	}
	private JsonObject event(String type,int tick,int cycle,long order,String target)
	{
		JsonObject e=new JsonObject(); e.addProperty("type",type); e.addProperty("tick",tick-summary.startTick);
		e.addProperty("cycle",cycle); e.addProperty("order",order); e.addProperty("target",target(target)); return e;
	}
	private String target(String raw) { return raw==null?"UNKNOWN":targets.computeIfAbsent(raw,k->"T"+(targets.size()+1)); }
	private static String targetClass(String raw) { return allowed(raw.split("@",2)[0],"NEX","FUMUS","UMBRA","CRUOR","GLACIES"); }
	private static String allowed(String value,String... values) { return Arrays.asList(values).contains(value)?value:"UNKNOWN"; }
	private JsonObject targetState(NexTargetState t)
	{
		JsonObject out=new JsonObject(); out.addProperty("form",t.form.name()); number(out,"ratio",t.healthRatio); number(out,"scale",t.healthScale);
		var h=t.health; JsonObject hp=new JsonObject(); hp.addProperty("source",h.source.name()); hp.addProperty("validity",h.validity.name());
		number(hp,"lower",h.lower); number(hp,"upper",h.upper); number(hp,"maximum",h.maximum);
		if(h.source!=NexHealth.Source.UNKNOWN) { hp.addProperty("tick",h.tick-summary.startTick); hp.addProperty("cycle",(long)h.cycle-baseCycle); hp.addProperty("target",target(h.target)); hp.addProperty("definition",h.definition); }
		out.add("health",hp); return out;
	}
	private int state(CombatState s)
	{
		JsonObject out=new JsonObject(); out.add("slots",numbers(s.slots,PoweredMagic.SLOTS));
		List<String> levels=List.of("ATTACK","STRENGTH","DEFENCE","HITPOINTS","PRAYER","RANGED","MAGIC","AGILITY");
		out.add("baseLevels",numbers(s.realLevels,levels)); out.add("visibleLevels",numbers(s.visibleLevels,levels));
		JsonArray prayers=new JsonArray();
		for(String p:new TreeSet<>(s.prayers))
		{ if(!allowed(p,"PIETY","RIGOUR","DEADEYE","EAGLE_EYE","CHIVALRY","INCREDIBLE_REFLEXES","ULTIMATE_STRENGTH","STEEL_SKIN","THICK_SKIN","ROCK_SKIN","AUGURY","MYSTIC_MIGHT","SHARP_EYE","HAWK_EYE","BURST_OF_STRENGTH","SUPERHUMAN_STRENGTH","CLARITY_OF_THOUGHT","IMPROVED_REFLEXES").equals("UNKNOWN")) { prayers.add(p); } }
		out.add("prayers",prayers); number(out,"style",s.style); number(out,"energy",s.specialEnergy); out.addProperty("specialSelected",s.specialSelected);
		out.add("resources",numbers(s.resources,List.of("spec-energy-server","death-charge-active","death-charge-cooldown","death-charge-upgrade-raw","death-charge-active-server","death-charge-cooldown-server","death-charge-upgrade-server","quiver-ammo-id-raw","quiver-ammo-quantity-raw","quiver-splinters-raw","quiver-charged-raw","worn-ammo-quantity","inventory-slots-used")));
		return states.computeIfAbsent(out,k->states.size());
	}
	private static JsonObject numbers(Map<String,Integer> source,Collection<String> keys)
	{ JsonObject out=new JsonObject(); for(String key:keys) { number(out,key,source.get(key)); } return out; }
	private static void number(JsonObject out,String key,Number value) { out.add(key,value==null?JsonNull.INSTANCE:new JsonPrimitive(value)); }
}
