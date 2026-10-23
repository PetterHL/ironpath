package com.ironpath;

import com.ironpath.advisor.*;
import com.ironpath.nex.*;
import java.util.*;

/** Immutable allowlisted export. No account object, names, cohort, method string,
 * item-name map, raw trace or container serialization can enter the report model. */
final class NexReport
{
	// Narrative retains its old budget. Evidence is separately bounded by 2,048 callbacks
	// and 1,024 samples, numeric fields/fixed vocabularies, and deduplicated states.
	static final int MAX_NARRATIVE_CHARS=24000, MAX_CHARS=8_000_000;
	enum Provenance { LIVE_PUBLIC_CLIENT, SYNTHETIC_OFFLINE }
	enum State { READY, PENDING, NO_ACCOUNT, NO_KILL, PAUSED, RESET, NOT_READY }
	private final List<String> lines;
	private final String evidence;
	private NexReport(List<String> lines,String evidence) { this.lines=List.copyOf(lines); this.evidence=evidence; }

	static NexReport snapshot(NexSummary summary,NexOwnedResult result,int choiceIndex,int pairIndex,
		KnowledgeCatalog catalog,Provenance provenance,State state)
	{ return snapshot(summary,result,choiceIndex,pairIndex,catalog,provenance,state,null); }
	static NexReport snapshot(NexSummary summary,NexOwnedResult result,int choiceIndex,int pairIndex,
		KnowledgeCatalog catalog,Provenance provenance,State state,NexRecord record)
	{ return snapshot(summary,result,choiceIndex,pairIndex,catalog,provenance,state,record,0); }
	static NexReport snapshot(NexSummary summary,NexOwnedResult result,int choiceIndex,int pairIndex,
		KnowledgeCatalog catalog,Provenance provenance,State state,NexRecord record,int ignoredObservationPage)
	{
		List<String> out=new ArrayList<>(); String evidence=null;
		out.add("IronPath diagnostic report · schema 2");
		out.add("Evidence: "+provenance+"; status: "+state);
		out.add("Build: "+BuildInfo.VERSION+"; adapter: "+NexCapture.VERSION+"; evaluator: "+NexOwnedEvaluator.VERSION);
		String clientVersion=net.runelite.client.RuneLiteProperties.getVersion();
		out.add("RuneLite client version: "+(clientVersion!=null && clientVersion.matches("\\d+\\.\\d+\\.\\d+(?:-SNAPSHOT)?")?clientVersion:"UNKNOWN"));
		out.add("Models: "+MeleeModel.VERSION+" / "+RangedModel.VERSION+"; knowledge: "+(catalog==null?"unavailable":catalog.version));
		out.add("Pilot model/planning scope: Nex / Trio MP2 / one kill per trip; inheritance is not per-kill confirmation.");
		out.add("No kill timestamp recorded. Tick offsets below are relative to measured start. This is a diagnostic snapshot, not a replay recording.");
		// Hidden/cleared state must never be recoverable even if a caller accidentally supplies it.
		if(state==State.NO_ACCOUNT || state==State.PAUSED || state==State.RESET || state==State.NOT_READY)
		{ out.add("Session evidence unavailable or hidden; no kill/calculation exported."); return new NexReport(out,evidence); }
		if(summary==null) { out.add("No captured kill available."); return new NexReport(out,evidence); }
		out.add("Report-local kill: K1; outcome: "+summary.outcome+"; exclusion: "+exclusion(summary));
		NexContext context=record!=null && record.summary==summary?record.context:result!=null && result.summary==summary?result.context:summary.context;
		long contextRevision=record!=null && record.summary==summary?record.revision:result!=null && result.summary==summary?result.contextRevision:0;
		out.add("Attempt-start context (defaults): "+summary.context.label()+"; "+summary.context.provenance());
		out.add("Effective context revision: "+contextRevision+"; "+context.label()+"; "+context.provenance());
		out.add("Output support: observed duration/share unchanged; conditional ordinary arithmetic uses captured state and frozen planning inputs. Pilot context confirmed="+context.pilot()+". Automatic method recommendations and team-time estimates unavailable.");
		out.add("Comparability: "+(record==null?"not evaluated":record.exclusion()==null?"eligible for descriptive matching; count alone is insufficient":record.exclusion())+". Planning locks are not observed performance; roster identities omitted.");
		out.add("Timer: "+summary.timerScope+"; duration seconds: "+summary.seconds());
		out.add("Damage scope: "+NexSummary.DAMAGE_SCOPE);
		out.add("Observed local/total hitsplats: "+summary.localDamage()+" / "+summary.totalDamage()+"; share percent: "+summary.share());
		out.add("Sampled/expected ticks: "+summary.sampledTicks+" / "+summary.expectedTicks+"; unclassified hits: "+summary.unclassifiedHits);
		if(summary.signals!=null)
		{
			var s=summary.signals;
			out.add("Signal offsets: smoke="+offset(s.smokeTick,summary)+"; dying="+offset(s.deathTick,summary)
				+"; arena start="+offset(s.boundaryTick,summary)+"; arena end="+offset(s.barrierEndTick,summary)+"; continuous="+s.continuous);
		}
		out.add("Capture gaps: "+summary.gaps.size()+"; launch attribution, event-order ambiguity, internal charges and consumption are not established.");
		if(state==State.PENDING || result==null || result.summary!=summary || result.contextRevision!=contextRevision || !result.context.equals(context)
			|| record!=null && (record.inputs==null?result.inputRevision!=0:result.inputRevision!=record.inputs.revision || !result.modelRevision.equals(record.inputs.modelRevision)))
		{ out.add("Calculation unavailable/pending for K1. No other kill's result exported."); return new NexReport(out,evidence); }
		out.add("Report-local calculation: C1; immutable calculation revision: "+result.revision);
		out.addAll(result.observations.overview);
		observation(out,summary,result);
		evidence=result.observations.export;
		out.add("Automatic N3 recommendation gaps: "+NexPlanSupport.automaticGaps());
		var resources=result.resources; var scenarios=resources.scenarios;
		out.add("Resource evidence (tenths percent): first="+resources.firstEnergy+"; last="+resources.lastEnergy
			+"; sampled net increases="+resources.sampledIncreases+"; sampled net decreases="+resources.sampledDecreases
			+"; all recorded rings Lightbearer="+resources.fixedLightbearerObserved+"; two active charges observed="+resources.improvedActiveObserved);
		out.add("Conditional resource examples: "+NexResourceScenarios.VERSION+" / "+SpecTimeline.VERSION
			+"; start energy="+scenarios.initialEnergy+"; duration ticks="+scenarios.duration+"; blocked="+scenarios.gaps);
		out.add(NexResourceScenarios.ASSUMPTIONS); out.add(NexResourceScenarios.LIMITS);
		for(var example:scenarios.examples)
		{
			out.add("Separate example "+example.schedule+"; cost="+example.schedule.cost+"; delay="+example.schedule.delay
				+"; admitted uses="+example.minUses+".."+example.maxUses+"; final energy="+example.minFinalEnergy+".."+example.maxFinalEnergy
				+"; lost at cap="+example.minWasted+".."+example.maxWasted+"; cases="+example.cases);
		}

		out.add("Calculation context/input revisions: "+result.contextRevision+" / "+result.inputRevision+"; models: "+result.modelRevision);
		out.add("Scenario counts (not fight coverage): supported="+result.choices.stream().filter(c->c.supported()).count()
			+"; unavailable="+result.choices.stream().filter(c->!c.supported()).count());
		Map<String,Integer> gaps=new TreeMap<>();
		result.gaps.forEach((gap,n)->gaps.merge(gapCode(gap),n,Integer::sum));
		out.add("Comparison exclusions: "+gaps);
		if(result.planning!=null)
		{
			out.add("Frozen planning assumption: upgraded Death Charge="+result.planning.upgradedDeathCharge+"; this does not establish cast/proc use.");
			out.add("Frozen planning locks (item IDs): "+new TreeMap<>(result.planning.locks)+"; Lightbearer fixed by pilot.");
		}
		else { out.add("Frozen planning assumptions unavailable in this fixture."); }
		if(choiceIndex<0 || choiceIndex>=result.choices.size())
		{ out.add("No selected comparison."); return new NexReport(out,evidence); }
		var choice=result.choices.get(choiceIndex); var sample=choice.actual; var captured=sample.state;
		out.add("Matching recorded launch-state candidates="+NexObservationReview.launches(summary,sample).size()
			+"; supported result="+(choice.supported()?"CONDITIONAL_STATE_OFFENCE":"DEPENDENT_DAMAGE_UNAVAILABLE")+"; no missed-opportunity or alternative schedule inference.");
		out.add("Frozen public target reading: "+sample.targetState.reading()+"; "+NexTargetState.VERSION);
		out.add("Selected scenario: "+(choiceIndex+1)+"; pair: "+pairIndex+"; tick offset: "+(sample.tick-summary.startTick)
			+"; role: "+vocabulary(sample.role,"melee","ranged","spec candidate","unclassified")
			+"; phase: "+vocabulary(sample.phase,"SMOKE","SHADOW","BLOOD","ICE","ZAROS")
			+"; window: "+vocabulary(sample.window,"UNKNOWN","NONE","FEAR_THE_SHADOW","EMBRACE_DARKNESS",
				"BLOOD_SIPHON","BLOOD_SACRIFICE","ICE_CONTAIN","ICE_PRISON",
				"MINION_FUMUS","MINION_UMBRA","MINION_CRUOR","MINION_GLACIES"));
		out.add("Availability: "+(choice.bankPreparation?"conditional bank preparation; stock/packing unverified":"carried at captured state")
			+"; search: "+(choice.exhaustive?"complete within supported scope":"partial; optimum unestablished"));
		out.add("Observed base levels: "+levels(captured.realLevels)+"; visible levels: "+levels(captured.visibleLevels));
		Map<String,Integer> observedSlots=new LinkedHashMap<>();
		for(String slot:PoweredMagic.SLOTS) { observedSlots.put(slot,captured.slots.getOrDefault(slot,-1)); }
		out.add("Observed slots (exact item IDs, 0 empty, -1 unknown): "+observedSlots);
		String observedTarget=sample.target==null?null:sample.target.split("@",2)[0];
		out.add("Observed target class: "+vocabulary(observedTarget,"NEX","FUMUS","UMBRA","CRUOR","GLACIES"));
		out.add("Observed offensive prayer flags (allowlisted): "+captured.prayers.stream().filter(p->Set.of("PIETY","RIGOUR","DEADEYE","EAGLE_EYE","CHIVALRY","INCREDIBLE_REFLEXES","ULTIMATE_STRENGTH","STEEL_SKIN","THICK_SKIN","ROCK_SKIN","AUGURY","MYSTIC_MIGHT","SHARP_EYE","HAWK_EYE","BURST_OF_STRENGTH","SUPERHUMAN_STRENGTH","CLARITY_OF_THOUGHT","IMPROVED_REFLEXES").contains(p)).sorted().collect(java.util.stream.Collectors.toList())
			+"; style button: "+captured.style+"; energy tenths percent: "+captured.specialEnergy+"; selected spec: "+captured.specialSelected);
		for(String key:List.of("quiver-ammo-id-raw","quiver-ammo-quantity-raw","quiver-splinters-raw","quiver-charged-raw","worn-ammo-quantity","inventory-slots-used","death-charge-active"))
		{ out.add("Observed counter "+key+": "+captured.resources.getOrDefault(key,-1)+" (-1 unread; raw counters do not prove usable stock)"); }
		if(!choice.supported()) { out.add("Selected comparison unavailable: "+gapCode(choice.gapReason())); return new NexReport(out,evidence); }
		List<ComparisonResult> pairs=new ArrayList<>(); pairs.add(choice.comparison); pairs.addAll(choice.alternatives);
		if(pairIndex<0 || pairIndex>=pairs.size()) { out.add("Selected pair unavailable; no substitute exported."); return new NexReport(out,evidence); }
		var pair=pairs.get(pairIndex);
		out.add("Comparison versions: "+pair.modelVersion+" / "+pair.knowledgeVersion+"; offence scope: "+pair.offenceScope);
		setup(out,"Actual",pair.beforeSetup); setup(out,"Candidate",pair.afterSetup);
		for(int id:new TreeSet<>(pair.afterSetup.slots.values()))
		{
			if(id>0 && !pair.beforeSetup.slots.containsValue(id))
			{ out.add("Relevant carried candidate quantity · item "+id+": "+(captured.inventory==null?"unread":captured.inventory.getOrDefault(id,0L))); }
		}
		var target=pair.target;
		out.add("Assumed Nex base target: defence="+target.defenceLevel+"; stab/slash/crush="+target.meleeStab+"/"+target.meleeSlash+"/"+target.meleeCrush
			+"; ranged light/standard/heavy="+target.rangedLight+"/"+target.rangedStandard+"/"+target.rangedHeavy+"; magic="+target.magicLevel+"; magic attack="+target.magicAttack+"; size="+target.size);
		metrics(out,"Before",pair.before); metrics(out,"After",pair.after);
		out.add("Displayed metric: "+NexOwnedResult.headline(pair)+"; exact delta DPS: "+pair.dpsGain());
		out.add("Per-metric support: "+new TreeMap<>(pair.support));
		if(catalog!=null)
		{
			Set<String> sources=new TreeSet<>(target.sources);
			for(var setup:List.of(pair.beforeSetup,pair.afterSetup))
			{
				Set<Integer> ids=new HashSet<>(setup.slots.values()); ids.add(setup.loadedAmmo);
				catalog.melee.items.stream().filter(i->ids.contains(i.id)).forEach(i->sources.addAll(i.sources));
				catalog.ranged.items.stream().filter(i->ids.contains(i.id)).forEach(i->sources.addAll(i.sources));
			}
			sources.addAll(sample.role.equals("melee")?catalog.melee.sources:catalog.ranged.sources);
			for(String id:sources) { if(catalog.sources.containsKey(id)) { out.add("Reviewed source: "+id+"@"+catalog.sources.get(id).revision); } }
		}
		out.add("Limitations: conditional ordinary active-attack offence if supplied; internal charges and ammunition for an entire kill unverified. No inferred defence reduction, launch attribution, phase throughput, global best, joint packing, healing, survival, contribution or seconds saved. Independent alternatives cannot be added.");
		if(pair.beforeSetup.slots.containsValue(24780) || pair.afterSetup.slots.containsValue(24780))
		{ out.add("Blood fury healing/survival unmodelled; offence alone cannot establish overall value."); }
		out.add("Coverage omissions: "+choice.coverage.size()+" scoped coverage notices; unmodelled alternatives may change the global optimum.");
		return new NexReport(out,evidence);
	}
	private static String offset(Integer tick,NexSummary summary) { return tick==null?"unobserved":Integer.toString(tick-summary.startTick); }
	private static void observation(List<String> out,NexSummary summary,NexOwnedResult result)
	{
		var o=summary.observations; var r=result.observations;
		out.add("Automatic observation="+NexObservations.VERSION+"; launches="+o.launches.size()+"; distinct projectiles="+o.flights.size()+"; own impacts="+o.impacts.size()+"; truncated="+o.truncated);
		out.add("HP states: HUD candidates="+r.hudReadings+"; bounded actor bars="+r.barReadings+". HUD data is not validated Nex impact HP; no HP trajectory inferred.");
		out.add("Smoke-callback start stock="+(r.startResources==null?"UNKNOWN":"CAPTURED_NOT_CONSUMPTION")+"; server-agreed starting energy="+r.startEnergy+"; attack/spell readiness and inherited charge lifetime UNKNOWN.");
		out.add("Starting food units / potion doses="+r.startSupplies+" (null unread); consumption UNKNOWN. Correlation incomplete="+r.correlationLimited+"; unmatched counts may include work-limit omissions.");
		out.add("Resource callbacks after the measured end are diagnostic end-grace evidence only; they do not extend duration/damage. Measured end tick offset="+(summary.endTick-summary.startTick));
		out.add("Impact correlation: one candidate="+r.matches.stream().filter(m->m.candidates.size()==1).count()+"; multiple candidates="+r.matches.stream().filter(m->m.candidates.size()>1).count()
			+"; unmatched="+r.matches.stream().filter(m->m.candidates.isEmpty()).count()+"; zero hits="+o.impacts.stream().filter(h->h.amount==0).count()+". All candidates remain unvalidated, including singleton matches and other owned damage sources.");
		out.add("DC end effects/charge loss do not distinguish proc from expiry. Full-energy regeneration remains a client test item.");
	}
	private static String exclusion(NexSummary summary)
	{
		if(summary.outcome==com.ironpath.encounter.EncounterSummary.Outcome.COMPLETE) { return "none"; }
		// Exact known adapter reasons or fixed incomplete-signal tokens; never echo arbitrary text.
		if(Set.of("Method or planning locks changed during capture","Left arena or scene loading during active capture",
			"Nex spawn identity replaced","Target identity limit","Unreviewed Nex transformation","Arena left before correlated completion",
			"Local player died/unavailable","Nex despawned without correlated completion","World-view identity changed","Hit identity limit","Player death").contains(summary.reason))
		{ return summary.reason; }
		List<String> missing=new ArrayList<>();
		for(String token:List.of("smoke start not observed before combat","tick or worn-state coverage interrupted","Nex death/dying not observed",
			"arena end boundary not observed","end signals outside matching window","final pre-death state sample missing"))
		{ if(summary.reason!=null && summary.reason.startsWith("Incomplete capture:") && summary.reason.contains(token)) { missing.add(token); } }
		return missing.isEmpty()?"unclassified capture exclusion":String.join("; ",missing);
	}
	private static String vocabulary(String value,String... allowed) { return Arrays.asList(allowed).contains(value)?value:"UNKNOWN"; }
	private static Map<String,Integer> levels(Map<String,Integer> levels)
	{
		Map<String,Integer> out=new TreeMap<>();
		for(String key:List.of("ATTACK","STRENGTH","DEFENCE","HITPOINTS","PRAYER","RANGED","MAGIC","AGILITY"))
		{ if(levels.containsKey(key)) { out.put(key,levels.get(key)); } }
		return out;
	}
	private static void setup(List<String> out,String label,CombatSetup setup)
	{
		Map<String,Integer> slots=new LinkedHashMap<>(); for(String key:PoweredMagic.SLOTS) { slots.put(key,setup.slots.get(key)); }
		out.add(label+" slots (exact item IDs, 0 empty, -1 unknown): "+slots+"; selected quiver projectile: "+setup.loadedAmmo+" (0 uses worn ammo)");
		out.add(label+" family/prayer/style: "+setup.family+" / "+(setup.family==CombatSetup.Family.MELEE?setup.meleePrayer:setup.rangedPrayer)
			+" / "+(setup.family==CombatSetup.Family.MELEE?setup.meleeStyle:setup.rangedStyle)+"; boost: "+setup.meleeBoost+"; ranging potion="+setup.rangingPotion
			+"; task="+setup.onTask+"; souls="+setup.souls+"; defence override="+setup.defenceOverride);
	}
	private static void metrics(List<String> out,String label,PoweredMagic.Metrics m)
	{
		if(m==null) { out.add(label+" metrics unavailable"); return; }
		out.add(label+" metrics: min/max="+m.minHit+"/"+m.maxHit+"; chance="+m.hitChance+"; DPS="+m.dps+"; attack ticks="+m.attackTicks
			+"; attack/defence roll="+m.attackRoll+"/"+m.defenceRoll+"; attack bonus="+m.attackBonus+"; strength/damage field="+m.damagePercent);
	}
	/** Only fixed reason categories leave the application, never interpolated raw diagnostic text. */
	static String gapCode(String value)
	{
		if(value==null) { return "UNAVAILABLE"; }
		if(value.startsWith("Special or unclassified")) { return "SPEC_ATTRIBUTION_UNSUPPORTED"; }
		if(value.startsWith("Target unknown")) { return "TARGET_UNSUPPORTED"; }
		if(value.startsWith("Phase unknown")) { return "PHASE_UNSUPPORTED"; }
		if(value.startsWith("Compatible quiver")) { return "QUIVER_AMMO_UNREAD"; }
		if(value.startsWith("Unknown ") || value.startsWith("Visible ") || value.contains("unread")) { return "INPUT_UNREAD"; }
		if(value.startsWith("Unreviewed")) { return "ITEM_MODEL_UNSUPPORTED"; }
		if(value.contains("lock")) { return "PLANNING_LOCK_CONFLICT"; }
		if(value.contains("inventory space") || value.contains("switch space")) { return "SWITCH_SPACE_UNVERIFIED"; }
		return "OTHER_MODEL_OR_ELIGIBILITY_GAP";
	}
	static String gapLabel(String value)
	{
		switch(gapCode(value))
		{
			case "SPEC_ATTRIBUTION_UNSUPPORTED": return "Spec attacks unverified";
			case "TARGET_UNSUPPORTED": return "Target model unavailable";
			case "PHASE_UNSUPPORTED": return "Unknown phase / Zaros";
			case "QUIVER_AMMO_UNREAD": return "Quiver ammo unread";
			case "INPUT_UNREAD": return "Required input unread";
			case "ITEM_MODEL_UNSUPPORTED": return "Item model unavailable";
			case "PLANNING_LOCK_CONFLICT": return "Planning lock conflict";
			case "SWITCH_SPACE_UNVERIFIED": return "Switch space unverified";
			default: return "Other model / eligibility gap";
		}
	}
	String format()
	{
		String footer="\nOmissions: names, account/profile IDs, roster/cohort fingerprints, raw traces, full inventory/bank, unrelated scenarios and ownership/unlock provenance are excluded. Unknown stock/charges remain unknown.\n";
		StringBuilder out=new StringBuilder(); int omitted=0;
		for(String line:lines)
		{
			if(out.length()+line.length()+footer.length()+100>MAX_NARRATIVE_CHARS) { omitted++; }
			else { out.append(line).append('\n'); }
		}
		out.append("Bounded output: ").append(omitted==0?"no selected fields truncated":omitted+" fields omitted by size limit");
		out.append(footer);
		if(evidence!=null)
		{
			// Valid capture bounds fit as a whole. Never silently emit a partial JSON document.
			if(out.length()+evidence.length()+16<=MAX_CHARS) { out.append("```json\n").append(evidence).append("\n```\n"); }
			else { out.append("Evidence unavailable: export exceeds size limit.\n```json\n{\"schema\":\"nex-evidence-1\",\"exportComplete\":false,\"reason\":\"SIZE_LIMIT\"}\n```\n"); }
		}
		return out.toString();
	}
}
