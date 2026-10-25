package com.ironpath.advisor;

import com.google.gson.*;
import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.nex.*;
import com.ironpath.preferences.Preferences;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Worker-only fixed-tempo equipment preview. Provisional signals never become validated attacks. */
public final class NexFangAdvice
{
	public static final String VERSION="nex-fang-advice-1";
	public static final String STATUS="PROVISIONAL_MAPPING_AND_BASE_STATS";
	// Computational bound, with explicit exclusions rather than a claim of complete coverage.
	private static final int MAX_GROUPS=64;
	public enum Kind { CARRIED, BANK_PREPARATION, UPGRADE }
	public enum Gap { INCOMPLETE_CAPTURE, TRUNCATED, OUTSIDE_MEASURED_INTERVAL, AMBIGUOUS_ATTACK,
		UNSUPPORTED_TARGET, UNKNOWN_PHASE, UNSUPPORTED_WINDOW, PACKAGE_INPUTS, WORK_LIMIT, CANCELLED }
	public static final class Range
	{
		public final double lower,upper;
		Range(double lower,double upper) { this.lower=lower; this.upper=upper; }
		Range times(int n) { return new Range(lower*n,upper*n); }
	}
	public static final class Row
	{
		public final Kind kind;
		public final NexSummary.Sample sample;
		public final ComparisonResult pair;
		/** Indices in the frozen action timeline, including misses and actions without a matched impact. */
		public final List<Integer> actionIndices;
		public final Range gain;
		public final boolean exhaustive;
		Row(Kind kind,NexSummary.Sample sample,ComparisonResult pair,List<Integer> indices,boolean exhaustive)
		{
			this.kind=kind; this.sample=sample; this.pair=pair; actionIndices=List.copyOf(indices);
			gain=pairedBounds(pair.before,pair.after).times(indices.size()); this.exhaustive=exhaustive;
		}
		public String headline()
		{
			return Math.abs(gain.upper-gain.lower)<1e-9?String.format(Locale.ROOT,"%+.2f expected damage",gain.lower)
				:String.format(Locale.ROOT,"%+.2f to %+.2f expected damage",Math.floor(gain.lower*100)/100,Math.ceil(gain.upper*100)/100);
		}
	}
	public final List<Row> rows;
	/** One reason for each excluded candidate; not a percentage of kill damage/time. */
	public final Map<Integer,Gap> excluded;
	public final List<Integer> included;
	public final int candidates,ambiguousImpacts,zeroImpacts;
	public final Set<Gap> blockers;
	private NexFangAdvice(List<Row> rows,Map<Integer,Gap> excluded,List<Integer> included,int candidates,
		int ambiguousImpacts,int zeroImpacts,Set<Gap> blockers)
	{
		this.rows=List.copyOf(rows); this.excluded=Collections.unmodifiableMap(new TreeMap<>(excluded));
		this.included=List.copyOf(included); this.candidates=candidates;
		this.ambiguousImpacts=ambiguousImpacts; this.zeroImpacts=zeroImpacts; this.blockers=Set.copyOf(blockers);
	}
	public static NexFangAdvice unavailable(NexSummary summary)
	{ return new NexFangAdvice(List.of(),Map.of(),List.of(),0,0,0,Set.of(Gap.PACKAGE_INPUTS)); }
	static NexFangAdvice evaluate(NexSummary summary,AccountSnapshot account,Preferences prefs,NexOwnedEvaluator evaluator,BooleanSupplier current)
	{
		var timeline=new NexActionTimeline(summary);
		Set<Gap> blockers=EnumSet.noneOf(Gap.class);
		if(summary.outcome!=Outcome.COMPLETE) { blockers.add(Gap.INCOMPLETE_CAPTURE); }
		if(timeline.truncated) { blockers.add(Gap.TRUNCATED); }
		Map<Integer,Gap> excluded=new TreeMap<>(); List<Integer> included=new ArrayList<>();
		Map<List<Object>,List<Integer>> groups=new LinkedHashMap<>(); int candidates=0;
		for(int i=0;i<timeline.entries.size();i++)
		{
			var row=timeline.entries.get(i); var s=row.signal.sample;
			// A held animation, menu intent or ready clock is never an attack sample.
			if(!row.attack() && row.signal.kind!=NexActions.Kind.ANIMATION && row.reason!=NexActionTimeline.Reason.SAME_TICK_FOOD) { continue; }
			if(row.signal.animation!=9471) { continue; }
			candidates++;
			Gap gap=!blockers.isEmpty()?blockers.contains(Gap.TRUNCATED)?Gap.TRUNCATED:Gap.INCOMPLETE_CAPTURE
				:row.signal.tick<summary.startTick || row.signal.tick>=summary.endTick?Gap.OUTSIDE_MEASURED_INTERVAL
				:!row.attack()?Gap.AMBIGUOUS_ATTACK:contextGap(s);
			if(gap!=null) { excluded.put(i,gap); continue; }
			List<Object> key=List.of(s.phase,s.window,s.target,s.state);
			if(!groups.containsKey(key) && groups.size()>=MAX_GROUPS) { excluded.put(i,Gap.WORK_LIMIT); continue; }
			groups.computeIfAbsent(key,k->new ArrayList<>()).add(i);
		}
		List<Row> rows=new ArrayList<>();
		for(var indices:groups.values())
		{
			if(!current.getAsBoolean()) { return new NexFangAdvice(List.of(),Map.of(),List.of(),candidates,0,0,Set.of(Gap.CANCELLED)); }
			var captured=timeline.entries.get(indices.get(0)).signal.sample;
			// Action evidence deliberately has no role inference. Derive melee only after the Fang gates.
			var sample=new NexSummary.Sample(captured.tick,captured.phase,captured.window,captured.target,"melee",
				captured.animation,captured.state,captured.targetState);
			try
			{
				var choices=evaluator.fangChoices(sample,account,prefs,current); List<Row> groupRows=new ArrayList<>();
				NexOwnedResult.Choice owned=null;
				for(var choice:choices)
				{
					if(!choice.supported() || !fixed(choice.comparison)) { continue; }
					if(owned==null || choice.comparison.after.dps>owned.comparison.after.dps) { owned=choice; }
					Kind kind=choice.bankPreparation?Kind.BANK_PREPARATION:Kind.CARRIED;
					groupRows.add(new Row(kind,sample,choice.comparison,indices,choice.exhaustive));
					for(var pair:choice.alternatives)
					{
						if(fixed(pair) && !pair.afterSetup.slots.equals(choice.proposed.slots))
						{ groupRows.add(new Row(kind,sample,pair,indices,false)); }
					}
				}
				if(owned==null) { indices.forEach(i->excluded.put(i,Gap.PACKAGE_INPUTS)); continue; }
				for(var pair:evaluator.fangUpgrades(sample,owned.proposed,account,prefs))
				{ if(fixed(pair)) { groupRows.add(new Row(Kind.UPGRADE,sample,pair,indices,false)); } }
				included.addAll(indices); rows.addAll(groupRows);
			}
			catch(IllegalArgumentException invalid) { indices.forEach(i->excluded.put(i,Gap.PACKAGE_INPUTS)); }
		}
		if(!current.getAsBoolean()) { return new NexFangAdvice(List.of(),Map.of(),List.of(),candidates,0,0,Set.of(Gap.CANCELLED)); }
		return new NexFangAdvice(rows,excluded,included,candidates,
			(int)timeline.impacts.stream().filter(i->i.ambiguous).count(),
			(int)summary.observations.impacts.stream().filter(i->i.amount==0).count(),blockers);
	}
	private static boolean fixed(ComparisonResult pair)
	{
		return pair.before!=null && pair.after!=null && pair.before.attackTicks==5 && pair.after.attackTicks==5
			&& Objects.equals(pair.beforeSetup.slots.get("weapon"),pair.afterSetup.slots.get("weapon"))
			&& pair.beforeSetup.meleeStyle==pair.afterSetup.meleeStyle;
	}
	private static Gap contextGap(NexSummary.Sample s)
	{
		if(s.target==null || !s.target.startsWith("NEX@") || s.targetState.form!=NexTargetState.Form.NEX
			|| s.targetState.overhead()!=NexTargetState.Overhead.NONE) { return Gap.UNSUPPORTED_TARGET; }
		if(!Set.of("SMOKE","SHADOW","BLOOD","ICE").contains(s.phase)) { return Gap.UNKNOWN_PHASE; }
		// Unknown windows cannot prove ordinary conditions. Siphon, minions and Zaros stay out.
		if(!Set.of("NONE","FEAR_THE_SHADOW").contains(s.window)) { return Gap.UNSUPPORTED_WINDOW; }
		return null;
	}
	/** All positive pre-impact HP, paired at every integer breakpoint. Beyond both maxima the
	 * expectations are constant. No bar midpoint, callback HP or expected-damage HP trajectory. */
	public static Range pairedBounds(PoweredMagic.Metrics before,PoweredMagic.Metrics after)
	{
		double[] b=expectations(before),a=expectations(after);
		double lo=Double.POSITIVE_INFINITY,hi=Double.NEGATIVE_INFINITY;
		for(int hp=1;hp<Math.max(b.length,a.length);hp++)
		{
			double delta=a[Math.min(hp,a.length-1)]-b[Math.min(hp,b.length-1)];
			lo=Math.min(lo,delta); hi=Math.max(hi,delta);
		}
		return new Range(lo,hi);
	}
	private static double[] expectations(PoweredMagic.Metrics m)
	{
		var damage=SpecAttackModel.ordinary(m.minHit,m.maxHit,m.hitChance,false,new SpecAttackModel.Limit(100000,100000),VERSION);
		double[] probabilities=new double[Math.max(1,damage.maximum)+1];
		for(var outcome:damage.outcomes) { probabilities[outcome.damage]+=outcome.probability; }
		double tail=0; double[] tails=new double[probabilities.length],expected=new double[probabilities.length];
		for(int d=probabilities.length-1;d>=1;d--) { tail+=probabilities[d]; tails[d]=tail; }
		for(int hp=1;hp<expected.length;hp++) { expected[hp]=expected[hp-1]+tails[hp]; }
		return expected;
	}
	public JsonObject json()
	{
		JsonObject out=new JsonObject(); out.addProperty("model",VERSION); out.addProperty("status",STATUS);
		out.addProperty("kernels",NexActionTimeline.VERSION+" / "+MeleeModel.VERSION+" / "+SpecAttackModel.VERSION);
		out.addProperty("sampleReferences","actionIndices reference explicit ACTION_SIGNAL.actionIndex values, independent of callback sorting");
		out.addProperty("validated",false); out.addProperty("candidates",candidates); out.addProperty("included",included.size());
		out.addProperty("ambiguousImpacts",ambiguousImpacts); out.addProperty("zeroImpacts",zeroImpacts);
		out.addProperty("scope","Independent equipment alternatives on identical provisional Fang samples, fixed weapon/style/5-tick tempo; base Nex stats, ordinary untransformed damage. HP unknown: all positive integer pre-impact HP, paired at every damage breakpoint; above both maxima constant. Sum bounds are conservative outer bounds, not a feasible HP trajectory. Mapping/window/base-stat assumptions unvalidated. No observed-damage subtraction, whole-kill extrapolation, seconds, spec, DC or resource plan.");
		JsonArray blocked=new JsonArray(); blockers.stream().sorted().forEach(g->blocked.add(g.name())); out.add("blockers",blocked);
		JsonArray omissions=new JsonArray(); excluded.forEach((index,gap)->{ JsonObject e=new JsonObject(); e.addProperty("actionIndex",index); e.addProperty("reason",gap.name()); omissions.add(e); }); out.add("exclusions",omissions);
		JsonArray results=new JsonArray();
		for(Row row:rows)
		{
			JsonObject r=new JsonObject(); r.addProperty("kind",row.kind.name());
			r.addProperty("phase",row.sample.phase); r.addProperty("window",row.sample.window);
			JsonArray indices=new JsonArray(); row.actionIndices.forEach(indices::add); r.add("actionIndices",indices);
			r.addProperty("gainLower",row.gain.lower); r.addProperty("gainUpper",row.gain.upper); r.addProperty("exhaustivePackageSearch",row.exhaustive);
			r.add("beforeSlots",slots(row.pair.beforeSetup)); r.add("afterSlots",slots(row.pair.afterSetup));
			r.addProperty("beforeMin",row.pair.before.minHit); r.addProperty("beforeMax",row.pair.before.maxHit); r.addProperty("beforeAccuracy",row.pair.before.hitChance);
			r.addProperty("afterMin",row.pair.after.minHit); r.addProperty("afterMax",row.pair.after.maxHit); r.addProperty("afterAccuracy",row.pair.after.hitChance);
			r.addProperty("defenceRoll",row.pair.before.defenceRoll); r.addProperty("knowledge",row.pair.knowledgeVersion);
			results.add(r);
		}
		out.add("comparisons",results); return out;
	}
	private static JsonObject slots(CombatSetup setup)
	{ JsonObject out=new JsonObject(); for(String slot:PoweredMagic.SLOTS) { out.addProperty(slot,setup.slots.get(slot)); } return out; }
}
