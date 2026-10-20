package com.ironpath.nex;

import com.ironpath.advisor.*;
import com.ironpath.encounter.CombatState;
import com.ironpath.preferences.NexMethod;
import java.time.Instant;
import java.util.*;
import static com.ironpath.nex.NexPackageComparison.*;
import static com.ironpath.nex.SpecTimeline.*;
import static com.ironpath.nex.SpecAttackPlan.*;

/** Runtime N3A/B/C join. Captured packages are evaluated under a separately declared hypothetical plan. */
public final class NexPlanComparison
{
	public static final String VERSION="nex-declared-plan-5";
	public static final int MAX_OPPORTUNITIES=128;
	public enum Policy { ORDINARY, FIRST_SPECIAL, FUNDED_SPECIAL_WITH_ORDINARY_FALLBACK }
	public enum ConditionMode { FIXED, PER_OPPORTUNITY }
	/** Explicit scenario conditions, never derived from NPC form, health bar or player stat drains. */
	public static final class Zaros
	{
		public static final Zaros UNKNOWN=new Zaros(NexTargetState.Overhead.UNKNOWN,null,null,false);
		public final NexTargetState.Overhead overhead;
		public final Integer defenceLevel,magicLevel;
		public final boolean fixedWindow;
		public Zaros(NexTargetState.Overhead overhead,Integer defenceLevel,Integer magicLevel,boolean fixedWindow)
		{
			Declaration.check(defenceLevel,0,1000,"Target Defence"); Declaration.check(magicLevel,0,1000,"Target Magic");
			this.overhead=Objects.requireNonNull(overhead); this.defenceLevel=defenceLevel; this.magicLevel=magicLevel; this.fixedWindow=fixedWindow;
		}
	}
	/** Independently supplied impact inputs. No value is carried forward or inferred from earlier damage. */
	public static final class Impact
	{
		public static final Impact UNKNOWN=new Impact(null,null,null,null,Zaros.UNKNOWN);
		public final Integer targetHp,playerHp,hardCap;
		public final Boolean rubyAllowed;
		public final Zaros zaros;
		public Impact(Integer targetHp,Integer playerHp,Integer hardCap,Boolean rubyAllowed,Zaros zaros)
		{
			Declaration.check(targetHp,1,100000,"Target HP"); Declaration.check(playerHp,1,255,"Player HP"); Declaration.check(hardCap,1,100000,"Hard cap");
			this.targetHp=targetHp; this.playerHp=playerHp; this.hardCap=hardCap; this.rubyAllowed=rubyAllowed; this.zaros=Objects.requireNonNull(zaros);
		}
	}
	public static final class Declaration
	{
		public final int endTick;
		public final Integer initialEnergy, firstRestore, targetHp, playerHp, hardCap;
		public final RestoreOrder order;
		public final Boolean kandarin, rubyAllowed;
		public final boolean legalAndReady;
		public final Policy policy;
		public final List<Integer> opportunities, casts, deaths;
		public final Map<String,Integer> upgrades;
		public final Map<Integer,Integer> upgradeQuantities;
		public final Instant declaredAt;
		public final List<NexSupplies.Use> supplies;
		public final Map<Integer,Long> unitCosts;
		public final Zaros zaros;
		public final ConditionMode conditionMode;
		public final Map<Integer,Impact> impacts;
		public Declaration(int endTick,Integer initialEnergy,Integer firstRestore,RestoreOrder order,
			Integer targetHp,Integer playerHp,Integer hardCap,Boolean kandarin,Boolean rubyAllowed,boolean legalAndReady,
			Policy policy,List<Integer> opportunities,List<Integer> casts,List<Integer> deaths,
			Map<String,Integer> upgrades,Map<Integer,Integer> upgradeQuantities)
		{
			this(endTick,initialEnergy,firstRestore,order,targetHp,playerHp,hardCap,kandarin,rubyAllowed,legalAndReady,policy,
				opportunities,casts,deaths,upgrades,upgradeQuantities,List.of(),Map.of());
		}
		public Declaration(int endTick,Integer initialEnergy,Integer firstRestore,RestoreOrder order,
			Integer targetHp,Integer playerHp,Integer hardCap,Boolean kandarin,Boolean rubyAllowed,boolean legalAndReady,
			Policy policy,List<Integer> opportunities,List<Integer> casts,List<Integer> deaths,
			Map<String,Integer> upgrades,Map<Integer,Integer> upgradeQuantities,List<NexSupplies.Use> supplies,Map<Integer,Long> unitCosts)
		{ this(endTick,initialEnergy,firstRestore,order,targetHp,playerHp,hardCap,kandarin,rubyAllowed,legalAndReady,policy,opportunities,casts,deaths,upgrades,upgradeQuantities,supplies,unitCosts,Zaros.UNKNOWN); }
		public Declaration(int endTick,Integer initialEnergy,Integer firstRestore,RestoreOrder order,
			Integer targetHp,Integer playerHp,Integer hardCap,Boolean kandarin,Boolean rubyAllowed,boolean legalAndReady,
			Policy policy,List<Integer> opportunities,List<Integer> casts,List<Integer> deaths,
			Map<String,Integer> upgrades,Map<Integer,Integer> upgradeQuantities,List<NexSupplies.Use> supplies,Map<Integer,Long> unitCosts,Zaros zaros)
		{ this(endTick,initialEnergy,firstRestore,order,targetHp,playerHp,hardCap,kandarin,rubyAllowed,legalAndReady,policy,
			opportunities,casts,deaths,upgrades,upgradeQuantities,supplies,unitCosts,zaros,ConditionMode.FIXED,Map.of()); }
		public Declaration(int endTick,Integer initialEnergy,Integer firstRestore,RestoreOrder order,
			Integer targetHp,Integer playerHp,Integer hardCap,Boolean kandarin,Boolean rubyAllowed,boolean legalAndReady,
			Policy policy,List<Integer> opportunities,List<Integer> casts,List<Integer> deaths,
			Map<String,Integer> upgrades,Map<Integer,Integer> upgradeQuantities,List<NexSupplies.Use> supplies,Map<Integer,Long> unitCosts,Zaros zaros,
			ConditionMode conditionMode,Map<Integer,Impact> impacts)
		{
			this.zaros=Objects.requireNonNull(zaros);
			this.conditionMode=Objects.requireNonNull(conditionMode); this.impacts=Map.copyOf(impacts);
			if(impacts.size()>MAX_OPPORTUNITIES || !opportunities.containsAll(impacts.keySet()) || conditionMode==ConditionMode.FIXED && !impacts.isEmpty())
			{ throw new IllegalArgumentException("Per-attack conditions must reference declared attack ticks in per-opportunity mode."); }
			NexSupplies.validate(supplies,endTick);
			if(unitCosts.size()>32 || unitCosts.entrySet().stream().anyMatch(e->e.getKey()<1 || e.getKey()>1000000 || e.getValue()<0 || e.getValue()>1000000000L))
			{ throw new IllegalArgumentException("Use up to 32 exact nonnegative unit costs"); }
			this.supplies=List.copyOf(supplies); this.unitCosts=Map.copyOf(unitCosts);
			if(endTick<1 || endTick>MAX_TICKS || opportunities.isEmpty() || opportunities.size()>MAX_OPPORTUNITIES
				|| casts.size()>MAX_OPPORTUNITIES || deaths.size()>MAX_OPPORTUNITIES)
			{ throw new IllegalArgumentException("Declare 1–128 attack opportunities within a 1–3000 tick horizon."); }
			for(List<Integer> ticks:List.of(opportunities,casts,deaths))
			{
				int previous=-1;
				for(int tick:ticks)
				{ if(tick<0 || tick>=endTick || tick<=previous) { throw new IllegalArgumentException("Offsets must be distinct, increasing and before the exclusive end."); } previous=tick; }
			}
			check(initialEnergy,0,CAP,"Starting energy"); check(firstRestore,0,25,"First restore");
			check(targetHp,1,100000,"Target HP"); check(playerHp,1,255,"Player HP"); check(hardCap,1,100000,"Hard cap");
			if(!new HashSet<>(PoweredMagic.SLOTS).containsAll(upgrades.keySet()) || upgradeQuantities.size()>11
				|| upgradeQuantities.entrySet().stream().anyMatch(e->e.getKey()<1 || e.getKey()>1000000 || e.getValue()<1 || e.getValue()>20000)
				|| upgrades.values().stream().anyMatch(id->id<0 || id>1000000))
			{ throw new IllegalArgumentException("Exact bounded hypothetical equipment/stock required."); }
			this.endTick=endTick; this.initialEnergy=initialEnergy; this.firstRestore=firstRestore; this.order=order;
			this.targetHp=targetHp; this.playerHp=playerHp; this.hardCap=hardCap; this.kandarin=kandarin; this.rubyAllowed=rubyAllowed;
			this.legalAndReady=legalAndReady; this.policy=Objects.requireNonNull(policy);
			this.opportunities=List.copyOf(opportunities); this.casts=List.copyOf(casts); this.deaths=List.copyOf(deaths);
			this.upgrades=Map.copyOf(upgrades); this.upgradeQuantities=Map.copyOf(upgradeQuantities); declaredAt=Instant.now();
		}
		private static void check(Integer n,int lo,int hi,String label)
		{ if(n!=null && (n<lo || n>hi)) { throw new IllegalArgumentException(label+" outside supported bounds."); } }
		private <T> ScenarioFact<T> f(T value,String scope)
		{ return value==null?ScenarioFact.unknown("Not supplied in the declared plan",scope):ScenarioFact.assumed(value,"Explicit post-kill scenario declaration",scope,declaredAt); }
		public Impact impact(int tick)
		{ return conditionMode==ConditionMode.FIXED?new Impact(targetHp,playerHp,hardCap,rubyAllowed,zaros):impacts.getOrDefault(tick,Impact.UNKNOWN); }
	}
	public static final class Result
	{
		public final NexOwnedResult source;
		public final int choiceIndex,pairIndex;
		public final Declaration declaration;
		public final Comparison comparison;
		public final List<NexPlanSupport.Route> routes;
		public final Map<Evaluation,NexPlanSupport.Cost> costs;
		public final Map<NexSupplies.Kind,Integer> consumedSupplies;
		private Result(NexOwnedResult source,int choiceIndex,int pairIndex,Declaration declaration,Comparison comparison,List<NexPlanSupport.Route> routes)
		{ this.source=source; this.choiceIndex=choiceIndex; this.pairIndex=pairIndex; this.declaration=declaration; this.comparison=comparison; this.routes=List.copyOf(routes);
			Map<Evaluation,NexPlanSupport.Cost> costs=new HashMap<>();
			for(Evaluation e:List.of(comparison.actualToOwned.before,comparison.actualToOwned.after,comparison.ownedToUpgrade.after))
			{ var cost=NexPlanSupport.cost(e,declaration.unitCosts); if(cost!=null) { costs.put(e,cost); } }
			this.costs=Map.copyOf(costs); consumedSupplies=NexSupplies.consumed(declaration.supplies);
		}
		/** Allowlisted standalone report: no account, containers, roster, raw timeline or client-supplied item names. */
		public String report()
		{
			List<String> lines=new ArrayList<>(); Declaration d=declaration;
			lines.add("IronPath conditional N3 scenario · schema 5 · "+VERSION);
			lines.add("Report-local kill K1; frozen calculation revision "+source.revision+"; captured state "+choiceIndex+"; pair "+pairIndex);
			lines.add("Captured equipment and visible levels are inputs to hypothetical attacks, not attributed attack evidence.");
			var sample=source.choices.get(choiceIndex).actual; Context context=context(sample,d,d.impact(d.opportunities.get(0)));
			lines.add("Frozen public target reading: "+sample.targetState.reading()+"; "+NexTargetState.VERSION);
			lines.add("Captured context: target="+context.target+"; phase="+context.phase+"; window="+vocabulary(sample.window,"UNKNOWN","NONE","FEAR_THE_SHADOW","EMBRACE_DARKNESS",
				"BLOOD_SIPHON","BLOOD_SACRIFICE","ICE_CONTAIN","ICE_PRISON","MINION_FUMUS","MINION_UMBRA","MINION_CRUOR","MINION_GLACIES"));
			lines.add("Modeled context: target="+context.target+"; phase="+context.phase+"; window="+(d.conditionMode==ConditionMode.FIXED?context.window:"per-opportunity")+"; captured target/phase with separately declared impact conditions.");
			lines.add("Frozen public-state evidence; owned/hypothetical packages retain these levels, prayer flags and style button. Equip/attack access and resource/HP conditions remain separate assumptions.");
			lines.add("Declared horizon="+d.endTick+"; energy="+d.initialEnergy+"; first restore="+d.firstRestore+"; restore order="+d.order);
			lines.add("Condition mode="+d.conditionMode+"; Kandarin="+d.kandarin);
			if(d.conditionMode==ConditionMode.FIXED)
			{
				lines.add("Declared target HP="+d.targetHp+"; player HP="+d.playerHp+"; hard cap="+d.hardCap+"; ruby allowed="+d.rubyAllowed);
				if(context.phase.equals("ZAROS")) { lines.add("Declared Zaros fixed window="+d.zaros.fixedWindow+"; overhead="+d.zaros.overhead+"; target Defence="+d.zaros.defenceLevel+"; target Magic="+d.zaros.magicLevel); }
			}
			else for(int tick:d.opportunities)
			{
				Impact impact=d.impact(tick); Zaros z=impact.zaros;
				lines.add("Declared attack tick="+tick+"; target HP="+impact.targetHp+"; player HP="+impact.playerHp+"; hard cap="+impact.hardCap+"; ruby allowed="+impact.rubyAllowed
					+(context.phase.equals("ZAROS")?"; impact confirmed="+z.fixedWindow+"; overhead="+z.overhead+"; target Defence="+z.defenceLevel+"; target Magic="+z.magicLevel:""));
			}
			if(context.phase.equals("ZAROS")) { lines.add("Zaros levels/overhead at each hypothetical impact are assumptions. Soul Split healing, Turmoil transfers, reflected damage and overhead cycling are not simulated. Missing target Magic blocks only dependent Twisted bow damage."); }
			lines.add("Per-opportunity conditions replace fixed inputs. Missing rows/fields stay unknown; no carry-forward or expected-damage HP feedback. Special and same-tick fallback share the same inputs across every package.");
			lines.add("Declared legal opportunities / attack-ready / no initial active charge / cast-ready="+d.legalAndReady+"; policy="+d.policy);
			lines.add("Attack offsets="+d.opportunities+"; cast offsets="+d.casts+"; distinct eligible death offsets="+d.deaths);
			lines.add("Casts/deaths, rune availability and spell opportunities are assumptions. Blank lists explicitly exclude those events from this scenario.");
			lines.add("Ammo branch: no retrieval / no saved ammunition; charged-quiver launches spend a splinter while available. This branch does not estimate average consumption.");
			lines.add("Hypothetical package edits="+new TreeMap<>(d.upgrades)+"; declared new usable quantities="+new TreeMap<>(d.upgradeQuantities));
			Map<String,String> versions=new TreeMap<>();
			for(Evaluation e:List.of(comparison.actualToOwned.before,comparison.actualToOwned.after,comparison.ownedToUpgrade.after)) { versions.putAll(e.versions); }
			lines.add("Reviewed model/source versions="+versions);
			lines.add("Decision support="+NexPlanSupport.VERSION+"; automatic capture-to-recommendation gaps="+NexPlanSupport.automaticGaps());
			lines.add("Declared supply actions="+d.supplies.stream().map(u->u.tick+":"+u.kind).collect(java.util.stream.Collectors.toList())+"; model="+NexSupplies.VERSION);
			lines.add("Supply uses precede casts/deaths/attacks. Only declared uses change levels; no decay, external boosts/drains or healing trajectory is inferred. Supplied per-attack HP stays independent.");
			lines.add("Declared unit costs (gp per projectile/splinter/food or potion dose)="+new TreeMap<>(d.unitCosts));
			evaluation(lines,"Captured package",comparison.actualToOwned.before);
			evaluation(lines,"Owned baseline",comparison.actualToOwned.after);
			evaluation(lines,"Owned plus hypothetical package",comparison.ownedToUpgrade.after);
			delta(lines,"Captured-to-owned",comparison.actualToOwned); delta(lines,"Owned-to-upgrade",comparison.ownedToUpgrade);
			for(var entry:Map.of("Captured",comparison.actualToOwned.before,"Owned",comparison.actualToOwned.after,"Hypothetical",comparison.ownedToUpgrade.after).entrySet())
			{
				var cost=costs.get(entry.getValue());
				lines.add(entry.getKey()+" ammo, declared supply and rune cost: "+(cost==null?"unavailable":
					"units="+new TreeMap<>(cost.units)+"; gp="+cost.gp+"; missing unit prices="+new TreeSet<>(cost.missingPrices)));
			}
			for(var route:routes)
			{ lines.add("Hypothetical acquisition · item "+route.item+": "+(route.work==null?"route unavailable":route.work)+"; unassessed stages="+route.stages+"; sources="+route.sources); }
			lines.add("Acquisition routes are catalogue facts, not assessed missing components, drop odds, full-item ETA or account readiness. Permanent unlocks and recurring units are separate.");
			lines.add("Costs cover this conditional resource branch, declared supplies and admitted Death Charge rune units only. Other gear upkeep, acquisition, healing/survival and whole-trip cost are excluded.");
			lines.add("Conditional launch expectations at independently supplied HP for each opportunity. No simulated HP trajectory, measured spec count, global-best recommendation, survival, contribution or team-time estimate.");
			lines.add("No kill timestamp recorded. No account identifiers, roster fingerprints or unrelated container contents exported. Nothing is uploaded.");
			String text=String.join("\n",lines);
			return text.length()<=24000?text:text.substring(0,23920)+"\n[Fields omitted by size limit]";
		}
		private static void evaluation(List<String> lines,String label,Evaluation e)
		{
			lines.add(label+": resource gaps="+e.resourceGaps);
			for(var entry:new TreeMap<>(e.scenario.loadouts).entrySet())
			{
				var loadout=entry.getValue(); var state=loadout.state.value(); String prefix=label+" "+entry.getKey();
				lines.add(prefix+" exact slots="+allowedSlots(state)+"; projectile="+loadout.projectile.value()+"; package origin="+loadout.state.origin+"; role="+loadout.role);
				lines.add(prefix+" real levels="+allowedLevels(state.realLevels)+"; visible levels="+allowedLevels(state.visibleLevels));
				lines.add(prefix+" active prayer flags="+state.prayers.stream().filter(p->Set.of("PIETY","RIGOUR","DEADEYE","EAGLE_EYE","CHIVALRY","INCREDIBLE_REFLEXES","ULTIMATE_STRENGTH",
					"STEEL_SKIN","THICK_SKIN","ROCK_SKIN","AUGURY","MYSTIC_MIGHT","SHARP_EYE","HAWK_EYE","BURST_OF_STRENGTH","SUPERHUMAN_STRENGTH","CLARITY_OF_THOUGHT","IMPROVED_REFLEXES").contains(p)).sorted().collect(java.util.stream.Collectors.toList())+"; style button="+state.style);
				var inputs=e.packages.get(entry.getKey());
				if(inputs!=null)
				{
					var setup=inputs.charged;
					lines.add(prefix+" family/prayer/style="+setup.family+" / "+(inputs.melee?setup.meleePrayer:setup.rangedPrayer)+" / "+(inputs.melee?setup.meleeStyle:setup.rangedStyle));
				}
			}
			if(e.plan==null) { return; }
			lines.add(label+" admitted Death Charge rune units="+new TreeMap<>(e.spentRunes)+"; normal Death/Blood/Soul branch; no substitution or other rune spending; "+NexSpellResources.VERSION);
			var p=e.plan; var r=p.resources;
			lines.add(label+": expected launch damage="+p.expectedDamage+"; expected self damage="+p.expectedSelfDamage+"; damage gaps="+p.gaps);
			lines.add(label+": energy spent="+r.spent+"; restored="+r.restored+"; lost at cap="+r.wasted+"; final="+r.finalEnergy+"; ordinary="+r.ordinaryAttacks+"; VW="+r.voidwakerSpecs+"; ZCB="+r.zaryteSpecs+"; DC procs="+r.deathChargeProcs);
			lines.add(label+": selected ammo spent="+new TreeMap<>(p.spentAmmo)+"; remaining="+new TreeMap<>(p.remainingAmmo)+"; splinters spent="+p.spentSplinters+"; physical splinters remaining="+e.remainingSplinters+"; resource branch probability="+p.resourceBranchProbability);
		}
		private static Map<String,Integer> allowedSlots(CombatState state)
		{
			Map<String,Integer> slots=new TreeMap<>();
			for(String slot:PoweredMagic.SLOTS) { slots.put(slot,state.slots.getOrDefault(slot,-1)); }
			return slots;
		}
		private static Map<String,Integer> allowedLevels(Map<String,Integer> levels)
		{
			Map<String,Integer> out=new TreeMap<>();
			for(String skill:List.of("ATTACK","STRENGTH","DEFENCE","RANGED","MAGIC","HITPOINTS","PRAYER","AGILITY"))
			{ if(levels.containsKey(skill)) { out.put(skill,levels.get(skill)); } }
			return out;
		}
		private static void delta(List<String> lines,String label,Delta delta)
		{ lines.add(label+": expected damage change="+(delta.expectedDamage==null?"unavailable":String.format(Locale.ROOT,"%.6f",delta.expectedDamage))+"; energy change="+delta.spentEnergy+"; ammo change="+new TreeMap<>(delta.spentAmmo)+"; splinter change="+delta.spentSplinters+"; package changes="+new TreeMap<>(delta.packageChanges)); }
	}
	private final KnowledgeCatalog catalog;
	public NexPlanComparison(KnowledgeCatalog catalog) { this.catalog=Objects.requireNonNull(catalog); }
	public Result evaluate(NexOwnedResult source,int choiceIndex,int pairIndex,Declaration d)
	{
		if(source.summary==null || source.summary.outcome!=com.ironpath.encounter.EncounterSummary.Outcome.COMPLETE
			|| choiceIndex<0 || choiceIndex>=source.choices.size() || source.planning==null)
		{ throw new IllegalArgumentException("A complete frozen calculation and captured state are required."); }
		var choice=source.choices.get(choiceIndex); var sample=choice.actual; CombatState captured=sample.state;
		ComparisonResult pair=null;
		if(choice.supported())
		{
			List<ComparisonResult> pairs=new ArrayList<>(); pairs.add(choice.comparison); pairs.addAll(choice.alternatives);
			if(pairIndex<0 || pairIndex>=pairs.size()) { throw new IllegalArgumentException("Selected owned pair is unavailable."); }
			pair=pairs.get(pairIndex);
		}
		else if(pairIndex!=0) { throw new IllegalArgumentException("Selected owned pair is unavailable."); }
		int projectile=projectile(captured);
		CombatState owned=pair==null?captured:state(captured,pair.afterSetup.slots);
		int ownedProjectile=pair==null?projectile:pair.afterSetup.loadedAmmo;
		Map<String,Integer> upgraded=new TreeMap<>(owned.slots); upgraded.putAll(d.upgrades);
		CombatState upgrade=state(captured,upgraded);
		int upgradeProjectile=projectile(upgrade);
		String role=sample.role.equals("melee") || sample.role.equals("ranged")?sample.role:"spec";
		Loadout actualLoad=new Loadout(role,ScenarioFact.observed(captured,"Frozen captured state; extraction time, not a kill timestamp","captured package",d.declaredAt),d.f(projectile,"projectile selection"),d.f(Retrieval.NONE,"no-retrieval branch"));
		Loadout ownedLoad=new Loadout(role,d.f(owned,"owned package under declared conditions"),d.f(ownedProjectile,"projectile selection"),d.f(Retrieval.NONE,"no-retrieval branch"));
		Loadout upgradeLoad=new Loadout(role,d.f(upgrade,"hypothetical complete package"),d.f(upgradeProjectile,"projectile selection"),d.f(Retrieval.NONE,"no-retrieval branch"));
		List<Step> steps=new ArrayList<>(); Map<Integer,Integer> attackOrdinals=new HashMap<>();
		for(int tick=0;tick<d.endTick;tick++)
		{
			for(NexSupplies.Use use:d.supplies) { if(use.tick==tick) { steps.add(Step.supply(use)); } }
			if(d.casts.contains(tick)) { steps.add(Step.cast(tick,d.f(source.planning.upgradedDeathCharge,"declared cast upgrade state"))); }
			if(d.deaths.contains(tick)) { steps.add(Step.death(tick,d.f("D"+tick,"distinct declared death"),d.f(d.legalAndReady?Boolean.TRUE:null,"death eligibility"))); }
			int ordinal=d.opportunities.indexOf(tick);
			if(ordinal>=0)
			{
				Impact impact=d.impact(tick); Context context=context(sample,d,impact);
				for(int fallback=0;fallback<2;fallback++)
				{
					attackOrdinals.put(steps.size(),ordinal*2+fallback);
					steps.add(Step.attack(tick,d.f(context,"declared target/phase at opportunity "+tick),d.f(d.legalAndReady?Boolean.TRUE:null,"legal opportunity"),
						d.f(impact.targetHp,"target HP at opportunity "+tick),d.f(impact.playerHp,"player HP at opportunity "+tick),d.f(impact.hardCap,"hard cap at opportunity "+tick),d.f(impact.rubyAllowed,"ruby effect allowed at opportunity "+tick)));
				}
			}
		}
		Conditions conditions=new Conditions("Declared post-kill package scenario",d.f(d.endTick,"exclusive horizon"),d.f(d.initialEnergy,"starting energy"),
			d.f(d.firstRestore,"restoration phase"),d.f(d.order,"same-tick natural restore order"),
			d.f(d.legalAndReady?Start.ATTACK_READY_NO_CHARGE_CAST_READY:null,"initial attack/spell state"),d.f(d.kandarin,"hard Kandarin"),steps);
		Stock base=stock(source,captured,Map.of(),Map.of(),d);
		Stock extra=stock(source,captured,d.upgrades,d.upgradeQuantities,d);
		Stock ownedStock=choice.bankPreparation?new Stock(Availability.BANK_PREPARATION,base.equipment,base.ammunition,base.splinters,base.inventorySpace,base.unlocks,
			ScenarioFact.unknown("Bank presence is not usable joint stock or a verified packing plan","bank packing"),Set.of(),base.inventoryItems):base;
		if(choice.bankPreparation) { extra=new Stock(Availability.BANK_PREPARATION,extra.equipment,extra.ammunition,extra.splinters,extra.inventorySpace,extra.unlocks,ownedStock.bankPacking,extra.hypothetical,extra.inventoryItems); }
		Scenario actual=new Scenario("Captured package under declared attacks",Kind.ACTUAL,Map.of("selected",actualLoad),actions(actualLoad,attackOrdinals,d),base);
		Scenario proposed=new Scenario("Selected supported owned package",Kind.OWNED,Map.of("captured",actualLoad,"selected",ownedLoad),actions(ownedLoad,attackOrdinals,d),ownedStock);
		Scenario hypothetical=new Scenario("Owned plus declared hypothetical edits",Kind.UPGRADE,Map.of("captured",actualLoad,"owned",ownedLoad,"selected",upgradeLoad),actions(upgradeLoad,attackOrdinals,d),extra);
		var comparison=new NexPackageComparison(catalog).compare(conditions,actual,proposed,hypothetical,source.planning);
		return new Result(source,choiceIndex,pairIndex,d,comparison,NexPlanSupport.routes(catalog,d.upgrades.entrySet().stream()
			.filter(e->!Objects.equals(owned.slots.get(e.getKey()),e.getValue()))
			.collect(java.util.stream.Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue))));
	}
	private static String vocabulary(String value,String... allowed)
	{ return Arrays.asList(allowed).contains(value)?value:"UNKNOWN"; }
	private static Context context(NexSummary.Sample sample,Declaration d,Impact impact)
	{
		String target=sample.target==null?"UNKNOWN":vocabulary(sample.target.split("@",2)[0],"NEX","FUMUS","UMBRA","CRUOR","GLACIES");
		String phase=vocabulary(sample.phase,"SMOKE","SHADOW","BLOOD","ICE","ZAROS");
		Window window="FEAR_THE_SHADOW".equals(sample.window)?Window.FEAR_THE_SHADOW:"EMBRACE_DARKNESS".equals(sample.window)?Window.EMBRACE_DARKNESS
			:"NONE".equals(sample.window)?Window.BASE:"UNKNOWN".equals(sample.window) || sample.window==null?Window.UNKNOWN:Window.SPECIAL;
		if(!target.equals("NEX") && ("MINION_"+target).equals(sample.window)) { window=Window.BASE; }
		if(target.equals("NEX") && phase.equals("ZAROS") && d!=null)
		{
			var z=impact.zaros;
			// Impact conditions must be declared even when an NPC form was observed.
			return new Context(target,phase,z.fixedWindow?Window.BASE:Window.UNKNOWN,
				d.f(z.overhead==NexTargetState.Overhead.UNKNOWN?null:z.overhead,"declared Zaros overhead at impact"),
				d.f(z.defenceLevel,"declared Zaros target Defence"),d.f(z.magicLevel,"declared Zaros target Magic"));
		}
		return new Context(target,phase,window);
	}
	private int projectile(CombatState state)
	{
		if(catalog.melee.items.stream().anyMatch(item->item.id==state.slots.getOrDefault("weapon",-1))) { return 0; }
		RangedItem weapon=catalog.ranged.items.stream().filter(i->i.id==state.slots.getOrDefault("weapon",-1)).findFirst().orElse(null);
		RangedItem ammo=catalog.ranged.items.stream().filter(i->i.id==state.slots.getOrDefault("ammo",-1)).findFirst().orElse(null);
		return weapon!=null && weapon.weapon!=null && !RangedModel.compatible(weapon,ammo)?state.resources.getOrDefault("quiver-ammo-id-raw",0):0;
	}
	private static CombatState state(CombatState before,Map<String,Integer> slots)
	{ return new CombatState(slots,before.realLevels,before.visibleLevels,before.prayers,before.style,before.specialEnergy,before.specialSelected,before.inventory,before.resources); }
	private static Map<Integer,Action> actions(Loadout l,Map<Integer,Integer> ordinals,Declaration d)
	{
		int weapon=l.state.value().slots.getOrDefault("weapon",-1);
		Attack special=weapon==27690?Attack.VOIDWAKER:weapon==26374?Attack.ZARYTE_CROSSBOW:Attack.ORDINARY;
		Map<Integer,Action> actions=new TreeMap<>();
		for(var e:ordinals.entrySet())
		{
			int ordinal=e.getValue()/2; boolean fallback=e.getValue()%2==1;
			boolean useSpec=special!=Attack.ORDINARY && (d.policy==Policy.FUNDED_SPECIAL_WITH_ORDINARY_FALLBACK || d.policy==Policy.FIRST_SPECIAL && ordinal==0);
			if(fallback && !useSpec) { continue; }
			actions.put(e.getKey(),new Action("selected",useSpec && !fallback?special:Attack.ORDINARY,d.f(false,"no saved ammunition branch"),d.f(true,"spend splinter branch when charged")));
		}
		return actions;
	}
	private Stock stock(NexOwnedResult source,CombatState captured,Map<String,Integer> edits,Map<Integer,Integer> quantities,Declaration d)
	{
		Map<Integer,Integer> gear=new TreeMap<>(),ammo=new TreeMap<>();
		if(captured.inventory!=null) captured.inventory.forEach((id,n)->{ if(id>0 && n>0 && n<=20000) { gear.put(id,n.intValue()); } });
		for(int id:captured.slots.values()) if(id>0) { gear.merge(id,1,Integer::sum); }
		Set<Integer> projectiles=new HashSet<>(); catalog.ranged.items.stream().filter(i->i.ammo!=RangedItem.Ammo.NONE).forEach(i->projectiles.add(i.id));
		for(int id:projectiles) if(captured.inventory!=null && captured.inventory.containsKey(id)) { long quantity=captured.inventory.get(id); if(quantity>=0 && quantity<=20000) { ammo.put(id,(int)quantity); }; }
		int worn=captured.slots.getOrDefault("ammo",0),wornQuantity=captured.resources.getOrDefault("worn-ammo-quantity",-1);
		if(projectiles.contains(worn) && wornQuantity>=0) { ammo.merge(worn,wornQuantity,Integer::sum); }
		int quiver=captured.resources.getOrDefault("quiver-ammo-id-raw",-1),quiverQuantity=captured.resources.getOrDefault("quiver-ammo-quantity-raw",-1);
		if(projectiles.contains(quiver) && quiverQuantity>=0) { ammo.merge(quiver,quiverQuantity,Integer::sum); }
		Set<Integer> hypothetical=new HashSet<>();
		for(int id:edits.values()) if(id>0)
		{
			Integer n=quantities.get(id);
			if(n==null) { throw new IllegalArgumentException("Declare the usable quantity for every hypothetical item."); }
			hypothetical.add(id); if(projectiles.contains(id)) { ammo.merge(id,n,Integer::sum); } else { gear.merge(id,n,Integer::sum); }
		}
		Set<Integer> inventoryItems=new HashSet<>();
		if(captured.inventory!=null) { captured.inventory.forEach((id,n)->{ if(id>0 && n>0) { inventoryItems.add(id); } }); }
		int used=captured.resources.getOrDefault("inventory-slots-used",-1);
		Integer space=used<0 || used>28?null:28-used;
		Integer splinters=captured.resources.get("quiver-splinters-raw"); if(splinters!=null && (splinters<0 || splinters>20000)) { splinters=null; }
		return new Stock(Availability.CARRIED,captured.inventory==null?ScenarioFact.unknown("Captured inventory unread","usable carried equipment"):d.f(gear,"frozen worn and carried stock"),
			d.f(ammo,"exact selected projectile counters; no bank stock"),d.f(splinters,"sampled quiver stock"),d.f(space,"unoccupied captured inventory slots; scenario equipment stacks reclaimed during packing"),
			d.f(source.scenarioUnlocks,"frozen account/manual equip facts; unknown values preserved"),d.f(false,"no bank packing established"),hypothetical,inventoryItems);
	}
}
