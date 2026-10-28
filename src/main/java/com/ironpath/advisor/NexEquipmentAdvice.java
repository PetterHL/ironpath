package com.ironpath.advisor;

import com.google.gson.*;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.nex.*;
import com.ironpath.preferences.Preferences;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Practical base-Nex scenarios from frozen loadouts. Never promotes inferred actions to observations. */
public final class NexEquipmentAdvice
{
	public static final String VERSION="nex-equipment-estimates-1";
	public static final String BASIS="Base Nex stats; ordinary vulnerable windows, unchanged teammate throughput and behaviour; no fixed waits. "
		+"MP2 damage mix: 70% melee / 20% ranged / 10% specials; RP2: 15% / 75% / 10%. "
		+"Recorded local/total Nex + minion damage supplies share; otherwise equal share of selected team (default trio). "
		+"Finite ammo/charge quantities are not required for supplied theoretical offence; observed empty quivers lose their charged bonus. Ruby bolts use representative 2000 target HP and 99 player HP, no Kandarin bonus. "
		+"Specials compare equal 50%-energy launches, without forecasting casts, regeneration or supply use. "
		+"These are reusable equipment scenarios, not reconstructed attacks in siphon, immunity or deflection windows.";
	public final List<NexFangAdvice.Row> rows;
	public final List<String> gaps;
	private NexEquipmentAdvice(List<NexFangAdvice.Row> rows,List<String> gaps)
	{ this.rows=List.copyOf(rows); this.gaps=List.copyOf(gaps); }
	static NexEquipmentAdvice empty() { return new NexEquipmentAdvice(List.of(),List.of()); }
	public static double weight(NexContext context,String role)
	{
		boolean ranged=context.strategy==NexContext.Strategy.RP2;
		return role.equals("spec")?0.10:role.equals("ranged")?(ranged?0.75:0.20):(ranged?0.15:0.70);
	}
	static EncounterImpact.Estimate impact(NexSummary summary,NexContext context,String role,Double gain)
	{
		double share=summary.share()!=null?summary.share()/100:1.0/(context.participants==null?3:context.participants);
		Double duration=summary.outcome==com.ironpath.encounter.EncounterSummary.Outcome.COMPLETE?summary.seconds():null;
		return EncounterImpact.estimate(share,EncounterImpact.weighted(weight(context,role),gain,true,true),duration,0.0);
	}
	static NexEquipmentAdvice evaluate(NexSummary summary,AccountSnapshot account,Preferences prefs,KnowledgeCatalog catalog,BooleanSupplier current)
	{ return evaluate(summary,account,prefs,catalog,current,summary.context); }
	static NexEquipmentAdvice evaluate(NexSummary summary,AccountSnapshot account,Preferences prefs,KnowledgeCatalog catalog,BooleanSupplier current,NexContext context)
	{
		List<NexFangAdvice.Row> rows=new ArrayList<>(); List<String> gaps=new ArrayList<>();
		Map<Integer,MeleeItem> melee=new HashMap<>(); catalog.melee.items.forEach(i->melee.put(i.id,i));
		Map<Integer,RangedItem> ranged=new HashMap<>(); catalog.ranged.items.forEach(i->ranged.put(i.id,i));
		Set<Integer> equipmentIds=new HashSet<>(melee.keySet()); equipmentIds.addAll(ranged.keySet());
		Set<List<Object>> seen=new HashSet<>(); int groups=0;
		var evaluator=new NexOwnedEvaluator(catalog,true,false);
		var specialEvaluator=new NexOwnedEvaluator(catalog,true,true);
		for(var captured:summary.timeline)
		{
			if(!current.getAsBoolean()) { return empty(); }
			int weapon=captured.state.slots.getOrDefault("weapon",-1);
			boolean isMelee=melee.containsKey(weapon) && melee.get(weapon).family!=null;
			boolean isRanged=ranged.containsKey(weapon) && ranged.get(weapon).weapon!=null;
			if(!isMelee && !isRanged) { continue; }
			String ordinary=isMelee?"melee":"ranged";
			// Target/window is deliberately a model input, never a rewritten captured observation.
			var sample=new NexSummary.Sample(captured.tick,"BASE_NEX","ORDINARY","NEX@reference",ordinary,captured.animation,captured.state);
			var state=captured.state;
			List<Object> key=Arrays.asList(ordinary,state.slots,state.realLevels,state.visibleLevels.get("ATTACK"),
				state.visibleLevels.get("STRENGTH"),state.visibleLevels.get("RANGED"),state.prayers,state.style,
				evaluator.fangGroupInputs(sample),equipmentPool(state,equipmentIds),state.resources.get("quiver-ammo-id-raw"),
				state.resources.getOrDefault("worn-ammo-quantity",-1)>0,state.resources.getOrDefault("quiver-ammo-quantity-raw",-1)>0,
				state.resources.getOrDefault("quiver-splinters-raw",-1)>0);
			if(!seen.add(key)) { continue; }
			if(++groups>64) { gaps.add("Equipment scenario work limit reached"); break; }
			add(rows,gaps,sample,ordinary,summary,account,prefs,catalog,evaluator,current,context);
			if(weapon==27690 || weapon==26374)
			{ add(rows,gaps,sample,"spec",summary,account,prefs,catalog,specialEvaluator,current,context); }
		}
		return current.getAsBoolean()?new NexEquipmentAdvice(rows,gaps):empty();
	}
	private static Set<Integer> equipmentPool(com.ironpath.encounter.CombatState state,Set<Integer> equipmentIds)
	{
		Set<Integer> pool=new TreeSet<>();
		if(state.inventory!=null) { state.inventory.forEach((id,n)->{ if(n>0 && equipmentIds.contains(id)) { pool.add(id); } }); }
		return pool;
	}
	private static void add(List<NexFangAdvice.Row> rows,List<String> gaps,NexSummary.Sample sample,String role,NexSummary summary,
		AccountSnapshot account,Preferences prefs,KnowledgeCatalog catalog,NexOwnedEvaluator evaluator,BooleanSupplier current,NexContext context)
	{
		try
		{
			Map<Map<String,Integer>,NexFangAdvice.Row> unique=new LinkedHashMap<>();
			for(var choice:evaluator.equipmentChoices(sample,account,prefs,current))
			{
				if(!choice.supported()) { if(choice.gapReason()!=null && gaps.size()<64) { gaps.add(role+": "+choice.gapReason()); } continue; }
				var kind=choice.bankPreparation?NexFangAdvice.Kind.BANK_PREPARATION:NexFangAdvice.Kind.CARRIED;
				put(unique,kind,sample,choice.comparison,choice.exhaustive,summary,role,catalog,account,prefs,context);
				for(var pair:choice.alternatives) { put(unique,kind,sample,pair,false,summary,role,catalog,account,prefs,context); }
			}
			for(var pair:evaluator.equipmentUpgrades(sample,account,prefs))
			{ put(unique,NexFangAdvice.Kind.UPGRADE,sample,pair,false,summary,role,catalog,account,prefs,context); }
			var ordered=new ArrayList<>(unique.values());
			ordered.sort(Comparator.comparingInt((NexFangAdvice.Row r)->r.gain.lower>1e-9?0:1)
				.thenComparingDouble(r->-IronmanOrder.priority(r.damageGain==null?0:r.damageGain,r.work.tier))
				.thenComparing(r->new TreeMap<>(r.pair.afterSetup.slots).toString()));
			rows.addAll(ordered);
		}
		catch(IllegalArgumentException invalid) { if(gaps.size()<64) { gaps.add(role+": "+invalid.getMessage()); } }
	}
	private static void put(Map<Map<String,Integer>,NexFangAdvice.Row> rows,NexFangAdvice.Kind kind,NexSummary.Sample sample,ComparisonResult pair,
		boolean exhaustive,NexSummary summary,String role,KnowledgeCatalog catalog,AccountSnapshot account,Preferences prefs,NexContext context)
	{
		if(pair.before==null || pair.after==null) { return; }
		rows.putIfAbsent(pair.afterSetup.slots,new NexFangAdvice.Row(kind,sample,pair,List.of(),exhaustive,
			IronmanOrder.work(pair,catalog,account,prefs),summary,role,context));
	}
	public JsonObject json()
	{
		JsonObject out=new JsonObject(); out.addProperty("model",VERSION); out.addProperty("basis",BASIS);
		JsonArray values=new JsonArray();
		for(var row:rows)
		{
			JsonObject r=new JsonObject(); r.addProperty("role",row.role); r.addProperty("kind",row.kind.name()); r.addProperty("tick",row.sample.tick);
			r.add("beforeSlots",new Gson().toJsonTree(row.pair.beforeSetup.slots)); r.add("afterSlots",new Gson().toJsonTree(row.pair.afterSetup.slots));
			r.addProperty("beforeRate",row.beforeDamage); r.addProperty("afterRate",row.afterDamage); r.addProperty("roleGain",row.damageGain);
			r.addProperty("affectedShare",row.affectedShare); r.addProperty("estimatedContribution",row.impact.contribution);
			r.addProperty("secondsSaved",row.impact.secondsSaved); r.addProperty("newDuration",row.impact.newDuration); values.add(r);
		}
		out.add("comparisons",values); JsonArray missing=new JsonArray(); gaps.forEach(missing::add); out.add("gaps",missing); return out;
	}
}
