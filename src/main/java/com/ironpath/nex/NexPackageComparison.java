package com.ironpath.nex;

import com.ironpath.advisor.*;
import com.ironpath.encounter.CombatState;
import com.ironpath.model.Truth;
import com.ironpath.preferences.NexMethod;
import java.util.*;
import java.util.stream.Collectors;
import static com.ironpath.nex.SpecTimeline.*;
import static com.ironpath.nex.SpecAttackPlan.*;

/** Complete-package bridge, not a capture-to-attack inference or encounter/HP simulator. */
public final class NexPackageComparison
{
	public static final String VERSION="nex-package-4";
	public enum Kind { ACTUAL, OWNED, UPGRADE }
	public enum Availability { CARRIED, BANK_PREPARATION }
	public enum Start { ATTACK_READY_NO_CHARGE_CAST_READY, OTHER }
	public enum StepKind { ATTACK, CAST, DEATH }
	/** BASE means the reviewed, explicitly conditional Nex base-stat ordinary window. */
	public enum Window { BASE, FEAR_THE_SHADOW, EMBRACE_DARKNESS, SPECIAL }
	public static final class Context
	{
		public final String target,phase;
		public final Window window;
		public Context(String target,String phase,Window window)
		{ this.target=Objects.requireNonNull(target); this.phase=Objects.requireNonNull(phase); this.window=Objects.requireNonNull(window); }
	}
	public static final class Step
	{
		public final int tick;
		public final StepKind kind;
		public final ScenarioFact<Context> context;
		public final ScenarioFact<Boolean> legal,rubyAllowed,upgraded,eligible;
		public final ScenarioFact<Integer> targetHp,playerHp,hardCap;
		public final ScenarioFact<String> deathKey;
		private Step(int tick,StepKind kind,ScenarioFact<Context> context,ScenarioFact<Boolean> legal,
			ScenarioFact<Integer> targetHp,ScenarioFact<Integer> playerHp,ScenarioFact<Integer> hardCap,
			ScenarioFact<Boolean> rubyAllowed,ScenarioFact<Boolean> upgraded,ScenarioFact<Boolean> eligible,ScenarioFact<String> deathKey)
		{
			if(tick<0 || tick>=MAX_TICKS) { throw new IllegalArgumentException("Step tick outside scenario bounds"); }
			this.tick=tick; this.kind=kind; this.context=context; this.legal=legal;
			this.targetHp=targetHp; this.playerHp=playerHp; this.hardCap=hardCap; this.rubyAllowed=rubyAllowed;
			this.upgraded=upgraded; this.eligible=eligible; this.deathKey=deathKey;
		}
		public static Step attack(int tick,ScenarioFact<Context> context,ScenarioFact<Boolean> legal,
			ScenarioFact<Integer> targetHp,ScenarioFact<Integer> playerHp,ScenarioFact<Integer> hardCap,ScenarioFact<Boolean> rubyAllowed)
		{ return new Step(tick,StepKind.ATTACK,Objects.requireNonNull(context),Objects.requireNonNull(legal),
			Objects.requireNonNull(targetHp),Objects.requireNonNull(playerHp),Objects.requireNonNull(hardCap),Objects.requireNonNull(rubyAllowed),null,null,null); }
		public static Step cast(int tick,ScenarioFact<Boolean> upgraded)
		{ return new Step(tick,StepKind.CAST,null,null,null,null,null,null,Objects.requireNonNull(upgraded),null,null); }
		public static Step death(int tick,ScenarioFact<String> key,ScenarioFact<Boolean> eligible)
		{ return new Step(tick,StepKind.DEATH,null,null,null,null,null,null,null,Objects.requireNonNull(eligible),Objects.requireNonNull(key)); }
	}
	/** One shared immutable declaration is required for every compared plan, including equal-tick order and HP. */
	public static final class Conditions
	{
		public final String scope;
		public final ScenarioFact<Integer> endTick,initialEnergy,firstRestoreTick;
		public final ScenarioFact<RestoreOrder> order;
		public final ScenarioFact<Start> start;
		public final ScenarioFact<Boolean> hardKandarin;
		public final List<Step> steps;
		public Conditions(String scope,ScenarioFact<Integer> endTick,ScenarioFact<Integer> initialEnergy,
			ScenarioFact<Integer> firstRestoreTick,ScenarioFact<RestoreOrder> order,ScenarioFact<Start> start,
			ScenarioFact<Boolean> hardKandarin,List<Step> steps)
		{
			if(scope==null || scope.isBlank() || steps.size()>MAX_EVENTS) { throw new IllegalArgumentException("Bounded declared scenario required"); }
			int last=-1;
			for(Step step:steps)
			{ if(step.tick<last) { throw new IllegalArgumentException("Steps must retain declared tick/order"); } last=step.tick; }
			this.scope=scope; this.endTick=Objects.requireNonNull(endTick); this.initialEnergy=Objects.requireNonNull(initialEnergy);
			this.firstRestoreTick=Objects.requireNonNull(firstRestoreTick); this.order=Objects.requireNonNull(order);
			this.start=Objects.requireNonNull(start); this.hardKandarin=Objects.requireNonNull(hardKandarin); this.steps=List.copyOf(steps);
		}
	}
	/** Exact state facts; role labels do not infer an attack. Projectile 0 means use the physical ammo slot. */
	public static final class Loadout
	{
		public final String role;
		public final ScenarioFact<CombatState> state;
		public final ScenarioFact<Integer> projectile;
		public final ScenarioFact<Retrieval> retrieval;
		public Loadout(String role,ScenarioFact<CombatState> state,ScenarioFact<Integer> projectile,ScenarioFact<Retrieval> retrieval)
		{
			if(!NexMethod.ROLES.contains(role)) { throw new IllegalArgumentException("Explicit method role required"); }
			this.role=role; this.state=Objects.requireNonNull(state); this.projectile=Objects.requireNonNull(projectile);
			this.retrieval=Objects.requireNonNull(retrieval);
		}
	}
	/** Known usable quantities and gear space after reservations. Optional captured stacks are reclaimed per scenario. Bank presence alone is insufficient. */
	public static final class Stock
	{
		public final Availability availability;
		public final ScenarioFact<Map<Integer,Integer>> equipment,ammunition;
		public final ScenarioFact<Integer> splinters,inventorySpace;
		public final ScenarioFact<Map<String,Truth>> unlocks;
		public final ScenarioFact<Boolean> bankPacking;
		public final Set<Integer> hypothetical;
		/** Occupied captured inventory stacks. Only stacks used by this scenario can release a reserved slot. */
		public final Set<Integer> inventoryItems;
		public Stock(Availability availability,ScenarioFact<Map<Integer,Integer>> equipment,ScenarioFact<Map<Integer,Integer>> ammunition,
			ScenarioFact<Integer> splinters,ScenarioFact<Integer> inventorySpace,ScenarioFact<Map<String,Truth>> unlocks,
			ScenarioFact<Boolean> bankPacking,Set<Integer> hypothetical)
		{ this(availability,equipment,ammunition,splinters,inventorySpace,unlocks,bankPacking,hypothetical,Set.of()); }
		/** inventorySpace is unoccupied space when inventoryItems supplies the original occupied stacks. */
		public Stock(Availability availability,ScenarioFact<Map<Integer,Integer>> equipment,ScenarioFact<Map<Integer,Integer>> ammunition,
			ScenarioFact<Integer> splinters,ScenarioFact<Integer> inventorySpace,ScenarioFact<Map<String,Truth>> unlocks,
			ScenarioFact<Boolean> bankPacking,Set<Integer> hypothetical,Set<Integer> inventoryItems)
		{
			this.availability=Objects.requireNonNull(availability); this.equipment=ScenarioFact.freezeMap(Objects.requireNonNull(equipment));
			this.ammunition=ScenarioFact.freezeMap(Objects.requireNonNull(ammunition)); this.splinters=Objects.requireNonNull(splinters);
			this.inventorySpace=Objects.requireNonNull(inventorySpace); this.unlocks=ScenarioFact.freezeMap(Objects.requireNonNull(unlocks));
			this.bankPacking=Objects.requireNonNull(bankPacking); this.hypothetical=Set.copyOf(hypothetical); this.inventoryItems=Set.copyOf(inventoryItems);
			if(hypothetical.stream().anyMatch(id->id<1)) { throw new IllegalArgumentException("Exact hypothetical item forms required"); }
			if(inventoryItems.size()>28 || inventoryItems.stream().anyMatch(id->id<1)) { throw new IllegalArgumentException("Bounded exact inventory stacks required"); }
		}
	}
	public static final class Action
	{
		public final String loadout;
		public final Attack attack;
		public final ScenarioFact<Boolean> savedAmmo,spentSplinter;
		public Action(String loadout,Attack attack,ScenarioFact<Boolean> savedAmmo,ScenarioFact<Boolean> spentSplinter)
		{ this.loadout=Objects.requireNonNull(loadout); this.attack=Objects.requireNonNull(attack);
			this.savedAmmo=Objects.requireNonNull(savedAmmo); this.spentSplinter=Objects.requireNonNull(spentSplinter); }
	}
	public static final class Scenario
	{
		public final String label;
		public final Kind kind;
		public final Map<String,Loadout> loadouts;
		/** Keys are shared Conditions.steps indices. Omitting an attack step is an explicit idle opportunity. */
		public final Map<Integer,Action> actions;
		public final Stock stock;
		public Scenario(String label,Kind kind,Map<String,Loadout> loadouts,Map<Integer,Action> actions,Stock stock)
		{
			if(label==null || label.isBlank() || loadouts.isEmpty() || loadouts.size()>MAX_EVENTS)
			{ throw new IllegalArgumentException("Named bounded complete-package scenario required"); }
			if(kind!=Kind.UPGRADE && !stock.hypothetical.isEmpty()) { throw new IllegalArgumentException("Only an upgrade scenario may introduce hypothetical stock"); }
			if(kind==Kind.ACTUAL && stock.availability!=Availability.CARRIED) { throw new IllegalArgumentException("Actually-used packages require carried availability"); }
			this.label=label; this.kind=Objects.requireNonNull(kind); this.loadouts=Map.copyOf(loadouts);
			this.actions=Map.copyOf(actions); this.stock=Objects.requireNonNull(stock);
		}
	}
	public static final class PackageInputs
	{
		public final CombatSetup uncharged,charged;
		public final boolean melee;
		public final int delay,ammo;
		public final Quiver quiver;
		private PackageInputs(CombatSetup uncharged,CombatSetup charged,boolean melee,int delay,int ammo,Quiver quiver)
		{ this.uncharged=uncharged; this.charged=charged; this.melee=melee; this.delay=delay; this.ammo=ammo; this.quiver=quiver; }
	}
	public static final class Derived
	{
		public final int stepIndex;
		public final PackageInputs inputs;
		public final PoweredMagic.Metrics unchargedMetrics,chargedMetrics;
		public final SpecAttackModel.Damage unchargedDamage,chargedDamage;
		public final String gap;
		private Derived(int stepIndex,PackageInputs inputs,PoweredMagic.Metrics unchargedMetrics,PoweredMagic.Metrics chargedMetrics,
			SpecAttackModel.Damage unchargedDamage,SpecAttackModel.Damage chargedDamage,String gap)
		{
			this.stepIndex=stepIndex; this.inputs=inputs; this.unchargedMetrics=unchargedMetrics; this.chargedMetrics=chargedMetrics;
			this.unchargedDamage=unchargedDamage; this.chargedDamage=chargedDamage; this.gap=gap;
		}
	}
	public static final class Evaluation
	{
		public final Conditions conditions;
		public final String method;
		public final Scenario scenario;
		public final Map<String,PackageInputs> packages;
		public final Map<Integer,Derived> launches;
		/** Resource event index -> shared step index, preserving idle and ineligible-death omissions. */
		public final Map<Integer,Integer> eventSteps;
		public final SpecAttackPlan.Result plan;
		/** Physical remaining stock is unknown when the supplied pool was unknown, even if no splinters were used. */
		public final Integer remainingSplinters;
		public final List<String> resourceGaps,evidence;
		/** All reviewed dependencies, including item forms, quiver branches and model versions. */
		public final Map<String,String> versions;
		private Evaluation(Conditions conditions,String method,Scenario scenario,Map<String,PackageInputs> packages,Map<Integer,Derived> launches,Map<Integer,Integer> eventSteps,
			SpecAttackPlan.Result plan,List<String> resourceGaps,List<String> evidence,Map<String,String> versions)
		{
			this.conditions=conditions; this.method=method; this.scenario=scenario; this.packages=Map.copyOf(packages); this.launches=Map.copyOf(launches);
			this.eventSteps=Map.copyOf(eventSteps);
			this.plan=plan; this.resourceGaps=List.copyOf(resourceGaps); this.evidence=List.copyOf(evidence); this.versions=Map.copyOf(versions);
			remainingSplinters=plan!=null && scenario.stock.splinters.known()?scenario.stock.splinters.value()-plan.spentSplinters:null;
		}
	}
	public static final class Delta
	{
		public final Evaluation before,after;
		public final Double expectedDamage,expectedSelfDamage;
		public final Integer spentEnergy,spentSplinters,ordinaryAttacks,voidwakerSpecs,zaryteSpecs;
		public final Map<Integer,Integer> spentAmmo;
		public final Map<String,Map<String,String>> packageChanges;
		private Delta(Evaluation before,Evaluation after)
		{
			this.before=before; this.after=after;
			SpecAttackPlan.Result b=before.plan,a=after.plan;
			expectedDamage=b==null || a==null || b.expectedDamage==null || a.expectedDamage==null?null:a.expectedDamage-b.expectedDamage;
			expectedSelfDamage=b==null || a==null || b.expectedSelfDamage==null || a.expectedSelfDamage==null?null:a.expectedSelfDamage-b.expectedSelfDamage;
			spentEnergy=b==null || a==null?null:a.resources.spent-b.resources.spent;
			spentSplinters=b==null || a==null?null:a.spentSplinters-b.spentSplinters;
			ordinaryAttacks=b==null || a==null?null:a.resources.ordinaryAttacks-b.resources.ordinaryAttacks;
			voidwakerSpecs=b==null || a==null?null:a.resources.voidwakerSpecs-b.resources.voidwakerSpecs;
			zaryteSpecs=b==null || a==null?null:a.resources.zaryteSpecs-b.resources.zaryteSpecs;
			Map<Integer,Integer> ammo=new TreeMap<>();
			if(b!=null && a!=null)
			{
				Set<Integer> ids=new TreeSet<>(b.spentAmmo.keySet()); ids.addAll(a.spentAmmo.keySet());
				for(int id:ids) { ammo.put(id,a.spentAmmo.getOrDefault(id,0)-b.spentAmmo.getOrDefault(id,0)); }
			}
			spentAmmo=Map.copyOf(ammo);
			Map<String,Map<String,String>> changes=new TreeMap<>(); Set<String> keys=new TreeSet<>(before.scenario.loadouts.keySet()); keys.addAll(after.scenario.loadouts.keySet());
			for(String key:keys)
			{
				Loadout bl=before.scenario.loadouts.get(key),al=after.scenario.loadouts.get(key); Map<String,String> slots=new TreeMap<>();
				if(bl==null || al==null || !bl.state.known() || !al.state.known()) { slots.put("package","Added, removed or unknown complete package"); }
				else
				{
					for(String slot:PoweredMagic.SLOTS)
					{ Integer bi=bl.state.value().slots.get(slot),ai=al.state.value().slots.get(slot);
						if(!Objects.equals(bi,ai)) { slots.put(slot,bi+" -> "+ai); } }
					if(bl.projectile.known() && al.projectile.known() && !bl.projectile.value().equals(al.projectile.value()))
					{ slots.put("selected-projectile",bl.projectile.value()+" -> "+al.projectile.value()); }
				}
				if(!slots.isEmpty()) { changes.put(key,Map.copyOf(slots)); }
			}
			packageChanges=Map.copyOf(changes);
		}
	}
	public static final class Comparison
	{
		public final Delta actualToOwned,ownedToUpgrade;
		private Comparison(Delta actualToOwned,Delta ownedToUpgrade)
		{ this.actualToOwned=actualToOwned; this.ownedToUpgrade=ownedToUpgrade; }
	}
	private final KnowledgeCatalog catalog;
	private final NexPackageFactory factory;
	private final Map<Integer,MeleeItem> melee;
	private final Map<Integer,RangedItem> ranged;
	private final TargetProfile target;
	public NexPackageComparison(KnowledgeCatalog catalog)
	{
		this.catalog=catalog; factory=new NexPackageFactory(catalog);
		melee=catalog.melee.items.stream().collect(Collectors.toUnmodifiableMap(i->i.id,i->i));
		ranged=catalog.ranged.items.stream().collect(Collectors.toUnmodifiableMap(i->i.id,i->i));
		target=catalog.comparison.targets.stream().filter(t->t.id.equals("nex-base")).findFirst().orElseThrow();
	}
	private static <T> T need(ScenarioFact<T> fact)
	{ if(!fact.known()) { throw new IllegalArgumentException("Unknown "+fact.scope+": "+fact.source); } return fact.value(); }
	private static int number(ScenarioFact<Integer> fact,int lo,int hi)
	{ int n=need(fact); if(n<lo || n>hi) { throw new IllegalArgumentException("Out-of-range "+fact.scope); } return n; }
	private static void evidence(List<String> evidence,ScenarioFact<?>... facts)
	{ for(ScenarioFact<?> fact:facts) { if(fact!=null) { evidence.add(fact.evidence()); } } }
	private static void quantities(Map<Integer,Integer> quantities)
	{ quantities.forEach((id,n)->{if(id<1 || n<0) { throw new IllegalArgumentException("Exact nonnegative usable stock required"); }}); }
	private boolean projectile(int id)
	{ return ranged.containsKey(id) && ranged.get(id).ammo!=RangedItem.Ammo.NONE; }
	private static Quiver quiver(int cape)
	{
		switch(cape)
		{
			case 28947: case 28951: return Quiver.UNCHARGED;
			case 28949: case 28953: return Quiver.CHARGED;
			case 28955: case 28957: return Quiver.BLESSED;
			default: return Quiver.NONE;
		}
	}
	private static int unchargedCape(int cape)
	{ return cape==28953 || cape==28957?28951:28947; }
	/** Retained worn reserve packages constrain joint packing, not proposed action choices. */
	private static boolean observedReserve(Loadout loadout,Scenario scenario)
	{ return loadout.state.origin==ScenarioFact.Origin.OBSERVED && scenario.actions.values().stream()
		.noneMatch(action->scenario.loadouts.get(action.loadout)==loadout); }
	private void requirements(Loadout loadout,Scenario scenario,Map<String,Integer> requirements,String equipFact,Integer minimumCombat,int itemId)
	{
		// Actually worn items establish equip usability, but never charges or carried ownership.
		if(scenario.loadouts.values().stream().anyMatch(observed->observed.state.origin==ScenarioFact.Origin.OBSERVED
			&& observed.state.value().slots.containsValue(itemId)
			&& observed.state.value().realLevels.equals(loadout.state.value().realLevels))) { return; }
		Map<String,Integer> levels=need(loadout.state).realLevels;
		for(var req:requirements.entrySet())
		{ if(levels.getOrDefault(req.getKey(),-1)<req.getValue()) { throw new IllegalArgumentException("Equip requirement unconfirmed: "+req.getKey()); } }
		if(equipFact!=null && need(scenario.stock.unlocks).getOrDefault(equipFact,Truth.UNKNOWN)!=Truth.TRUE)
		{ throw new IllegalArgumentException("Equip unlock unconfirmed: "+equipFact); }
		if(minimumCombat!=null)
		{
			for(String skill:List.of("ATTACK","STRENGTH","DEFENCE","HITPOINTS","PRAYER","RANGED","MAGIC"))
			{ if(!levels.containsKey(skill)) { throw new IllegalArgumentException("Combat level requirement unread: "+skill); } }
			int base=levels.get("DEFENCE")+levels.get("HITPOINTS")+levels.get("PRAYER")/2;
			int offence=Math.max(levels.get("ATTACK")+levels.get("STRENGTH"),Math.max(levels.get("RANGED")*3/2,levels.get("MAGIC")*3/2));
			if((base*10+offence*13)/40<minimumCombat) { throw new IllegalArgumentException("Combat level requirement not met"); }
		}
	}
	private PackageInputs build(Loadout loadout,Scenario scenario,NexMethod method)
	{
		CombatState state=need(loadout.state); Map<String,Integer> slots=state.slots;
		if(state.realLevels.values().stream().anyMatch(level->level<1 || level>99))
		{ throw new IllegalArgumentException("Real equip levels outside 1–99"); }
		if(!slots.keySet().equals(new HashSet<>(PoweredMagic.SLOTS)) || slots.values().stream().anyMatch(id->id<0))
		{ throw new IllegalArgumentException("Complete exact slots required; unknown is not empty"); }
		if(slots.get("ring")!=25975) { throw new IllegalArgumentException("Pilot requires fixed Lightbearer in every role"); }
		boolean isMelee=melee.containsKey(slots.get("weapon"));
		if(loadout.role.equals("melee") && !isMelee || loadout.role.equals("ranged") && isMelee)
		{ throw new IllegalArgumentException("Weapon does not match the declared role"); }
		for(String slot:PoweredMagic.SLOTS)
		{
			int id=slots.get(slot);
			Integer lock=method.locks.get(loadout.role+"/"+slot);
			if(scenario.kind!=Kind.ACTUAL && !observedReserve(loadout,scenario) && lock!=null && id!=lock) { throw new IllegalArgumentException("Planning lock conflicts: "+loadout.role+"/"+slot); }
			if(id==0) { continue; }
			if(isMelee)
			{
				MeleeItem item=melee.get(id);
				if(item==null || !slot.equals(item.slot)) { throw new IllegalArgumentException("Unreviewed exact melee item: "+slot+" "+id); }
				requirements(loadout,scenario,item.requirements,item.equipFact,item.minimumCombat,id);
			}
			else
			{
				RangedItem item=ranged.get(id);
				if(item==null || !slot.equals(item.slot)) { throw new IllegalArgumentException("Unreviewed exact ranged item: "+slot+" "+id); }
				requirements(loadout,scenario,item.requirements,null,null,id);
			}
		}
		Map<String,String> origins=new TreeMap<>(); slots.forEach((slot,id)->origins.put(slot,loadout.state.evidence()));
		int selected=number(loadout.projectile,0,1000000);
		if(isMelee && selected!=0) { throw new IllegalArgumentException("Melee package cannot select a projectile"); }
		CombatSetup setup=factory.build(isMelee,slots,origins,state.style,state.prayers,selected);
		int ammo=0,delay;
		Quiver q=Quiver.NONE;
		if(isMelee)
		{
			MeleeItem weapon=melee.get(slots.get("weapon"));
			if(weapon.twoHanded && slots.get("shield")!=0) { throw new IllegalArgumentException("Two-handed weapon conflicts with offhand"); }
			delay=weapon.ticks;
		}
		else
		{
			RangedItem weapon=ranged.get(slots.get("weapon"));
			if(weapon==null || weapon.weapon==null) { throw new IllegalArgumentException("Unreviewed ranged weapon"); }
			if(weapon.twoHanded && slots.get("shield")!=0) { throw new IllegalArgumentException("Two-handed weapon conflicts with offhand"); }
			if(!Set.of(RangedItem.Ammo.ARROW,RangedItem.Ammo.BOLT).contains(RangedModel.ammoKind(weapon)))
			{ throw new IllegalArgumentException("Weapon-internal charges/darts are outside this finite projectile model"); }
			q=quiver(slots.get("cape"));
			if(selected>0 && q==Quiver.NONE) { throw new IllegalArgumentException("Selected quiver projectile requires an exact quiver form"); }
			if(selected>0 && selected!=slots.get("ammo") && RangedModel.compatible(weapon,ranged.get(slots.get("ammo"))))
			{ throw new IllegalArgumentException("Compatible worn ammunition takes priority over selected quiver ammunition"); }
			ammo=selected>0?selected:slots.get("ammo");
			if(!RangedModel.compatible(weapon,ranged.get(ammo))) { throw new IllegalArgumentException("Incompatible exact selected ammunition"); }
			requirements(loadout,scenario,ranged.get(ammo).requirements,null,null,ammo);
			delay=weapon.ticks-(setup.rangedStyle==RangedModel.Style.RAPID?1:0);
		}
		CombatSetup uncharged=setup;
		if(q==Quiver.CHARGED || q==Quiver.BLESSED) { uncharged=setup.edit("cape",Integer.toString(unchargedCape(slots.get("cape")))); }
		return new PackageInputs(uncharged,setup,isMelee,delay,ammo,q);
	}
	private String contextGap(Step step,PackageInputs inputs)
	{
		Context c=need(step.context);
		if(!c.target.equals("NEX")) { return "Target/minion mechanics unreviewed: "+c.target; }
		if(!Set.of("SMOKE","SHADOW","BLOOD","ICE").contains(c.phase)) { return "Phase mechanics unreviewed (including Zaros soft caps): "+c.phase; }
		if(c.window==Window.SPECIAL) { return "Special window effects are not modelled"; }
		if(c.window==Window.EMBRACE_DARKNESS && inputs.melee) { return "MP2 Embrace darkness does not establish a legal melee launch"; }
		return null;
	}
	private PoweredMagic.Metrics metrics(CombatSetup setup,Loadout loadout,boolean isMelee)
	{
		Map<String,Integer> levels=need(loadout.state).visibleLevels;
		for(String key:isMelee?List.of("ATTACK","STRENGTH"):List.of("RANGED"))
		{ if(!levels.containsKey(key)) { throw new IllegalArgumentException("Visible "+key+" unread"); } }
		return isMelee?MeleeModel.calculateVisible(levels.get("ATTACK"),levels.get("STRENGTH"),setup,target,melee)
			:RangedModel.calculateVisible(levels.get("RANGED"),setup,target,ranged);
	}
	private SpecAttackModel.Damage damage(Conditions conditions,Step step,Action action,PackageInputs inputs,CombatSetup setup,
		PoweredMagic.Metrics metrics,String basis)
	{
		SpecAttackModel.Limit limit=new SpecAttackModel.Limit(number(step.targetHp,1,100000),number(step.hardCap,1,100000));
		if(action.attack==Attack.VOIDWAKER) { return SpecAttackModel.voidwaker(metrics.maxHit,limit,basis); }
		if(inputs.melee)
		{
			MeleeModel.Weapon family=melee.get(setup.slots.get("weapon")).family;
			if(family==MeleeModel.Weapon.SCYTHE || family==MeleeModel.Weapon.SOULREAPER)
			{ throw new IllegalArgumentException("Multi-hit/stack damage distribution is outside this launch bridge"); }
			return SpecAttackModel.ordinary(metrics.minHit,metrics.maxHit,metrics.hitChance,false,limit,basis);
		}
		RangedItem ammo=ranged.get(inputs.ammo);
		if(ammo.seeking) { throw new IllegalArgumentException("Seeking-arrow damage floor is not modelled by this launch bridge"); }
		if(ammo.boltEffect)
		{
			SpecAttackModel.Bolt bolt;
			switch(ammo.id)
			{
				case 9242: case 21944: bolt=SpecAttackModel.Bolt.RUBY; break;
				case 9243: case 21946: bolt=SpecAttackModel.Bolt.DIAMOND; break;
				default: throw new IllegalArgumentException("Enchanted bolt effect not reviewed: "+ammo.id);
			}
			return SpecAttackModel.crossbow(metrics.maxHit,metrics.attackRoll,metrics.defenceRoll,bolt,setup.slots.get("weapon")==26374,
				action.attack==Attack.ZARYTE_CROSSBOW,need(conditions.hardKandarin),bolt==SpecAttackModel.Bolt.RUBY?number(step.playerHp,1,255):null,
				bolt!=SpecAttackModel.Bolt.RUBY || need(step.rubyAllowed),limit,basis);
		}
		if(action.attack==Attack.ZARYTE_CROSSBOW) { throw new IllegalArgumentException("ZCB special needs reviewed ruby/diamond ammunition"); }
		return SpecAttackModel.ordinary(0,metrics.maxHit,metrics.hitChance,true,limit,basis);
	}
	/** Only selected resource pools participate. An omitted pool is not a zero-quantity account observation. */
	private Map<Integer,Integer> ammunition(Stock stock,Map<String,PackageInputs> packages)
	{
		Map<Integer,Integer> result=new TreeMap<>();
		for(PackageInputs input:packages.values()) if(input.ammo>0)
		{
			Integer quantity=need(stock.ammunition).get(input.ammo);
			if(quantity==null) { throw new IllegalArgumentException("Exact ammunition stock unknown: "+input.ammo); }
			result.put(input.ammo,quantity);
		}
		quantities(result); return result;
	}
	private void packing(Scenario scenario,Map<String,PackageInputs> packages)
	{
		Stock stock=scenario.stock;
		Map<Integer,Integer> gear=need(stock.equipment),ammo=ammunition(stock,packages); quantities(gear);
		int space=number(stock.inventorySpace,0,28);
		if(stock.availability==Availability.BANK_PREPARATION && !need(stock.bankPacking))
		{ throw new IllegalArgumentException("Bank presence is not a validated joint packing plan"); }
		for(int id:stock.hypothetical)
		{ if(gear.getOrDefault(id,0)<1 && (!stock.ammunition.known() || stock.ammunition.value().getOrDefault(id,0)<1))
			{ throw new IllegalArgumentException("Hypothetical item lacks explicit usable scenario quantity: "+id); } }
		Set<Integer> union=new TreeSet<>();
		for(PackageInputs input:packages.values())
		{
			for(int id:input.charged.slots.values())
			{
				if(id==0 || projectile(id)) { continue; }
				if(gear.getOrDefault(id,0)<1) { throw new IllegalArgumentException("Exact equipment not in usable "+stock.availability+" stock: "+id); }
				union.add(id);
			}
			if(input.ammo>0 && !ammo.containsKey(input.ammo)) { throw new IllegalArgumentException("Exact ammunition stock unknown: "+input.ammo); }
		}
		Set<Integer> modeled=new HashSet<>(union); modeled.addAll(ammo.keySet());
		// Unselected carried gear/supplies remain reserved. Hypothetical loadouts cannot free another scenario's slots.
		for(int id:stock.inventoryItems) { if(modeled.contains(id)) { space++; } }
		if(space>28) { throw new IllegalArgumentException("Modeled inventory stacks exceed captured occupied slots"); }
		for(PackageInputs input:packages.values())
		{
			Set<Integer> stowed=new HashSet<>(union); stowed.removeAll(input.charged.slots.values());
			for(var entry:ammo.entrySet())
			{
				// These are separate exact stacks, shared once, rather than copied for each role.
				if(entry.getValue()>0 && entry.getKey().intValue()!=input.charged.loadedAmmo && entry.getKey().intValue()!=input.charged.slots.get("ammo"))
				{ stowed.add(entry.getKey()); }
			}
			if(stowed.size()>space) { throw new IllegalArgumentException("Shared switches/ammo exceed reserved inventory space; supplies are not discarded"); }
		}
	}
	public Evaluation evaluate(Conditions conditions,Scenario scenario,NexMethod method)
	{
		Map<String,PackageInputs> packages=new TreeMap<>(); Map<Integer,Derived> derived=new TreeMap<>();
		List<String> gaps=new ArrayList<>(),evidence=new ArrayList<>(); Map<String,String> versions=new TreeMap<>();
		evidence(evidence,conditions.endTick,conditions.initialEnergy,conditions.firstRestoreTick,conditions.order,conditions.start,conditions.hardKandarin,
			scenario.stock.equipment,scenario.stock.ammunition,scenario.stock.splinters,scenario.stock.inventorySpace,scenario.stock.unlocks,scenario.stock.bankPacking);
		versions.put("bridge",VERSION); versions.put("knowledge",catalog.version); versions.put("melee",MeleeModel.VERSION); versions.put("ranged",RangedModel.VERSION);
		versions.put("damage",SpecAttackModel.VERSION); versions.put("plan",SpecAttackPlan.VERSION); versions.put("resources",SpecTimeline.VERSION);
		for(String id:target.sources) { versions.put(id,catalog.sources.get(id).revision); }
		for(String id:catalog.melee.sources) { versions.put(id,catalog.sources.get(id).revision); }
		for(String id:catalog.ranged.sources) { versions.put(id,catalog.sources.get(id).revision); }
		for(var entry:scenario.loadouts.entrySet())
		{
			Loadout l=entry.getValue(); evidence.add(entry.getKey()+": "+l.state.evidence()); evidence.add(l.projectile.evidence()); evidence.add(l.retrieval.evidence());
			try { packages.put(entry.getKey(),build(l,scenario,method)); }
			catch(IllegalArgumentException ex) { gaps.add("Package "+entry.getKey()+": "+ex.getMessage()); }
		}
		for(PackageInputs p:packages.values()) for(CombatSetup setup:List.of(p.uncharged,p.charged))
		{
			for(int id:setup.slots.values())
			{
				List<String> sources=p.melee?(melee.containsKey(id)?melee.get(id).sources:List.of()):(ranged.containsKey(id)?ranged.get(id).sources:List.of());
				for(String source:sources) { versions.put(source,catalog.sources.get(source).revision); }
			}
			if(ranged.containsKey(setup.loadedAmmo)) for(String source:ranged.get(setup.loadedAmmo).sources) { versions.put(source,catalog.sources.get(source).revision); }
		}
		try { if(packages.size()==scenario.loadouts.size()) { packing(scenario,packages); } }
		catch(IllegalArgumentException ex) { gaps.add(ex.getMessage()); }
		List<Event> events=new ArrayList<>(); Map<Integer,Launch> launches=new TreeMap<>(); Map<Integer,Integer> eventSteps=new TreeMap<>();
		for(var action:scenario.actions.entrySet())
		{
			if(action.getKey()<0 || action.getKey()>=conditions.steps.size() || conditions.steps.get(action.getKey()).kind!=StepKind.ATTACK
				|| !scenario.loadouts.containsKey(action.getValue().loadout)) { throw new IllegalArgumentException("Action must reference a declared attack opportunity and package"); }
		}
		for(int i=0;i<conditions.steps.size();i++)
		{
			Step step=conditions.steps.get(i);
			evidence(evidence,step.context,step.legal,step.targetHp,step.playerHp,step.hardCap,step.rubyAllowed,step.upgraded,step.eligible,step.deathKey);
			try
			{
				if(step.kind==StepKind.CAST)
				{
					boolean upgraded=need(step.upgraded);
					if(upgraded!=method.upgradedDeathCharge) { throw new IllegalArgumentException("Cast differs from declared method upgrade state"); }
					eventSteps.put(events.size(),i); events.add(Event.cast(step.tick,upgraded)); continue;
				}
				if(step.kind==StepKind.DEATH)
				{ if(need(step.eligible)) { Event event=Event.eligibleDeath(step.tick,need(step.deathKey)); eventSteps.put(events.size(),i); events.add(event); } continue; }
				Action action=scenario.actions.get(i); if(action==null) { continue; }
				evidence(evidence,action.savedAmmo,action.spentSplinter);
				PackageInputs input=packages.get(action.loadout); if(input==null) { continue; }
				Loadout loadout=scenario.loadouts.get(action.loadout);
				int weapon=input.charged.slots.get("weapon");
				if(action.attack==Attack.VOIDWAKER && weapon!=27690 || action.attack==Attack.ZARYTE_CROSSBOW && weapon!=26374)
				{ throw new IllegalArgumentException("Special launch does not match its own exact weapon package"); }
				PoweredMagic.Metrics uncharged=null,charged=null; SpecAttackModel.Damage ud=null,cd=null; String gap=null;
				try
				{
					gap=contextGap(step,input);
					if(gap==null)
					{
						uncharged=metrics(input.uncharged,loadout,input.melee); charged=metrics(input.charged,loadout,input.melee);
						String basis=conditions.scope+" · step "+i+" · "+scenario.label+" · "+loadout.state.evidence()+" · "+VERSION;
						ud=damage(conditions,step,action,input,input.uncharged,uncharged,basis+" · uncharged package");
						cd=damage(conditions,step,action,input,input.charged,charged,basis+" · charged package");
					}
				}
				catch(IllegalArgumentException ex) { gap=ex.getMessage(); }
				derived.put(i,new Derived(i,input,uncharged,charged,ud,cd,gap));
				if(!need(step.legal)) { throw new IllegalArgumentException("Launch opportunity explicitly unavailable"); }
				if(step.context.known() && step.context.value().window==Window.EMBRACE_DARKNESS && input.melee)
				{ throw new IllegalArgumentException("MP2 Embrace darkness excludes melee launches"); }
				Event event=action.attack==Attack.VOIDWAKER?Event.voidwaker(step.tick)
					:action.attack==Attack.ZARYTE_CROSSBOW?Event.zaryteCrossbow(step.tick,input.charged.rangedStyle==RangedModel.Style.RAPID)
					:Event.ordinary(step.tick,input.delay);
				Retrieval retrieval=input.ammo>0?need(loadout.retrieval):Retrieval.NONE;
				int cape=input.charged.slots.get("cape");
				if(input.quiver==Quiver.NONE && (retrieval==Retrieval.ASSEMBLER && cape!=22109 || retrieval==Retrieval.ACCUMULATOR && cape!=10499))
				{ throw new IllegalArgumentException("Active retrieval is incompatible with the exact cape/quiver package"); }
				boolean saved=input.ammo>0 && need(action.savedAmmo);
				boolean splinter=input.quiver==Quiver.CHARGED && need(action.spentSplinter);
				launches.put(events.size(),new Launch(action.attack,input.ammo,retrieval,input.quiver,saved,splinter,ud,cd,gap));
				eventSteps.put(events.size(),i); events.add(event);
			}
			catch(IllegalArgumentException ex) { gaps.add("Step "+i+": "+ex.getMessage()); }
		}
		SpecAttackPlan.Result result=null;
		try
		{
			int end=number(conditions.endTick,1,MAX_TICKS),energy=number(conditions.initialEnergy,0,CAP),restore=number(conditions.firstRestoreTick,0,Ring.LIGHTBEARER.interval);
			if(need(conditions.start)!=Start.ATTACK_READY_NO_CHARGE_CAST_READY) { throw new IllegalArgumentException("Initial attack/spell state outside supported start contract"); }
			RestoreOrder order=need(conditions.order);
			boolean chargedQuiver=packages.values().stream().anyMatch(p->p.quiver==Quiver.CHARGED);
			int splinters=chargedQuiver || scenario.stock.splinters.known()?number(scenario.stock.splinters,0,20000):0;
			if(gaps.isEmpty())
			{
				SpecTimeline.Plan energyPlan=new SpecTimeline.Plan(energy,end,Ring.LIGHTBEARER,restore,order,events);
				result=new SpecAttackPlan().run(new SpecAttackPlan.Plan(conditions.scope,energyPlan,launches,ammunition(scenario.stock,packages),splinters));
			}
		}
		catch(IllegalArgumentException ex) { gaps.add(ex.getMessage()); }
		evidence.add("Conditional launch expectations for one declared resource branch; not observed outcomes, a kill forecast, or global best owned.");
		evidence.add("Only selected projectile pools enter the launch ledger. No charged quiver means zero splinter consumption, not known physical stock; use remainingSplinters for the supplied pool.");
		evidence.add("Shared method: "+method.cohort()+"; availability "+scenario.stock.availability+"; hypothetical exact items "+scenario.stock.hypothetical);
		return new Evaluation(conditions,method.cohort(),scenario,packages,derived,eventSteps,result,gaps,evidence,versions);
	}
	public Delta compare(Evaluation before,Evaluation after)
	{
		if(before.conditions!=after.conditions || !before.method.equals(after.method))
		{ throw new IllegalArgumentException("Comparisons require the same immutable encounter conditions, method and horizon"); }
		// A loadout name is local to its scenario. Shared attack steps establish the comparison pair.
		for(var entry:before.scenario.actions.entrySet())
		{
			Action next=after.scenario.actions.get(entry.getKey());
			if(next==null) { continue; }
			String beforeKey=entry.getValue().loadout,afterKey=next.loadout;
			Loadout b=before.scenario.loadouts.get(beforeKey),a=after.scenario.loadouts.get(afterKey);
			if(b.state.known() && a.state.known()
				&& (!b.state.value().visibleLevels.equals(a.state.value().visibleLevels) || !b.state.value().realLevels.equals(a.state.value().realLevels)))
			{ throw new IllegalArgumentException("Compared package levels must retain the same declared encounter conditions"); }
			PackageInputs bp=before.packages.get(beforeKey),ap=after.packages.get(afterKey);
			if(b.state.known() && a.state.known() && bp!=null && ap!=null && bp.melee==ap.melee
				&& (!b.state.value().prayers.equals(a.state.value().prayers) || !Objects.equals(b.state.value().style,a.state.value().style)))
			{ throw new IllegalArgumentException("Equipment alternatives must retain declared active prayers and style button within a combat family"); }
		}
		Stock bs=before.scenario.stock,as=after.scenario.stock;
		if(before.scenario.kind==Kind.OWNED && after.scenario.kind==Kind.UPGRADE && bs.availability!=as.availability)
		{ throw new IllegalArgumentException("Owned-to-upgrade gains must retain the owned baseline availability scope"); }
		if(bs.availability==as.availability)
		{
			// An upgrade may add explicitly hypothetical exact stock; ordinary proposals cannot create it.
			if(bs.equipment.known() && as.equipment.known()) { sameStock(bs.equipment.value(),as.equipment.value(),as.hypothetical); }
			if(bs.ammunition.known() && as.ammunition.known()) { sameStock(bs.ammunition.value(),as.ammunition.value(),as.hypothetical); }
			if(bs.splinters.known() && as.splinters.known() && !bs.splinters.value().equals(as.splinters.value())
				|| bs.inventorySpace.known() && as.inventorySpace.known() && !bs.inventorySpace.value().equals(as.inventorySpace.value())
				|| !bs.inventoryItems.equals(as.inventoryItems))
			{ throw new IllegalArgumentException("Compared plans must retain declared starting splinters and reserved inventory space"); }
		}
		return new Delta(before,after);
	}
	private static void sameStock(Map<Integer,Integer> before,Map<Integer,Integer> after,Set<Integer> hypothetical)
	{
		Set<Integer> ids=new HashSet<>(before.keySet()); ids.addAll(after.keySet());
		for(int id:ids) if(!hypothetical.contains(id) && !Objects.equals(before.get(id),after.get(id)))
		{ throw new IllegalArgumentException("Proposed packages cannot manufacture or remove usable stock: "+id); }
	}
	public Comparison compare(Conditions conditions,Scenario actual,Scenario owned,Scenario upgrade,NexMethod method)
	{
		if(actual.kind!=Kind.ACTUAL || owned.kind!=Kind.OWNED || upgrade.kind!=Kind.UPGRADE)
		{ throw new IllegalArgumentException("Keep actually used, owned baseline and owned plus upgrade distinct"); }
		Evaluation a=evaluate(conditions,actual,method),o=evaluate(conditions,owned,method),u=evaluate(conditions,upgrade,method);
		return new Comparison(compare(a,o),compare(o,u));
	}
}
