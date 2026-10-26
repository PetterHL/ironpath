package com.ironpath.advisor;

import com.ironpath.encounter.CombatState;
import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.model.*;
import com.ironpath.nex.NexSummary;
import com.ironpath.preferences.NexMethod;
import com.ironpath.preferences.Preferences;
import java.util.*;
import java.util.stream.Collectors;

/** Ordinary post-kill pairwise offence and complete carried-package search; no global progression policy. */
public final class NexOwnedEvaluator
{
	public static final String VERSION="nex-owned-2";
	KnowledgeCatalog catalog() { return catalog; }
	// Computational guard, not a gear quota. A truncated search is explicitly not an optimum.
	private static final int MAX_SEARCH_NODES=100000;
	private final KnowledgeCatalog catalog;
	private final NexPackageFactory packages;
	private final Map<Integer,MeleeItem> melee;
	private final Map<Integer,RangedItem> ranged;
	private final TargetProfile target;
	public NexOwnedEvaluator(KnowledgeCatalog catalog)
	{
		this.catalog=catalog; packages=new NexPackageFactory(catalog);
		melee=catalog.melee.items.stream().collect(Collectors.toUnmodifiableMap(i->i.id,i->i));
		ranged=catalog.ranged.items.stream().collect(Collectors.toUnmodifiableMap(i->i.id,i->i));
		target=catalog.comparison.targets.stream().filter(t->t.id.equals("nex-base")).findFirst().orElseThrow();
	}

	public NexOwnedResult evaluate(NexSummary summary, AccountSnapshot account, Preferences prefs)
	{ return evaluate(summary,account,prefs,()->true); }
	public NexOwnedResult evaluate(NexSummary summary, AccountSnapshot account, Preferences prefs,java.util.function.BooleanSupplier current)
	{
		if(summary==null) { return new NexOwnedResult(null,List.of(),prefs.nex); }
		if(summary.outcome!=Outcome.COMPLETE)
		{ return new NexOwnedResult(summary,List.of(),prefs.nex,Map.of(),summary.context,0,0,VERSION,NexFangAdvice.evaluate(summary,account,prefs,this,current)); }
		List<NexOwnedResult.Choice> choices=new ArrayList<>(); Set<List<Object>> seen=new HashSet<>();
		for(NexSummary.Sample s:summary.timeline)
		{
			if(!current.getAsBoolean()) { return new NexOwnedResult(summary,List.of(),prefs.nex); }
			// Preserve all distinct input states/windows. Never choose the longest worn setup.
			List<Object> key=Arrays.asList(s.role,s.phase,s.window,targetName(s.target),s.state,s.phase.equals("ZAROS")?s.targetState.overhead():null);
			if(!seen.add(key)) { continue; }
			String gap=contextGap(s);
			CombatSetup before=null;
			if(gap==null)
			{
				try { before=setup(s,s.state.slots); gap=validate(s,before,account,prefs,false); }
				catch(IllegalArgumentException ex) { gap=ex.getMessage(); }
			}
			if(gap!=null) { choices.add(new NexOwnedResult.Choice(s,null,null,gap,true,List.of())); continue; }
			Set<Integer> carried=new TreeSet<>(); s.state.slots.values().stream().filter(i->i>0).forEach(carried::add);
			if(s.state.inventory!=null) { s.state.inventory.forEach((id,n)->{if(n>0) { carried.add(id); }}); }
			List<String> coverage=new ArrayList<>();
			if(s.state.inventory==null) { coverage.add("Inventory unread at this state; only worn items can be searched."); }
			for(int id:carried)
			{
				if((s.role.equals("melee") && catalog.melee.unsupportedWeapons.containsKey(id))
					|| (s.role.equals("ranged") && catalog.ranged.unsupportedWeapons.containsKey(id)))
				{ coverage.add("Carried alternative not modelled: "+catalog.itemNames().getOrDefault(id,"Item "+id)); }
			}
			if(!account.bank.known()) { coverage.add("Bank ownership unread; this does not establish global best owned."); }
			else { coverage.add("Search covers carried gear at this state. Bank-only alternatives require a next-trip inventory plan."); }
			choices.add(search(s,account,prefs,before,carried,coverage,false,current));
			if(account.bank.known() && account.bank.value().stream().anyMatch(id->!carried.contains(id) && slot(s.role,id)!=null))
			{
				Set<Integer> owned=new TreeSet<>(carried); owned.addAll(account.bank.value());
				List<String> nextCoverage=new ArrayList<>(coverage);
				nextCoverage.add("Bank preparation scenario only: recheck packing, stock, charges, switch space and supplies before the next trip. Does not establish an executable combined trip plan.");
				choices.add(search(s,account,prefs,before,owned,nextCoverage,true,current));
			}
		}
		Map<String,Truth> unlocks=new TreeMap<>();
		for(MeleeItem item:catalog.melee.items)
		{ if(item.equipFact!=null) { unlocks.put(item.equipFact,AccountFacts.planning(account,prefs,item.equipFact)); } }
		return new NexOwnedResult(summary,choices,prefs.nex,unlocks,summary.context,0,0,VERSION,
			NexFangAdvice.evaluate(summary,account,prefs,this,current));
	}
	/** Frozen offence and equip-availability inputs for a compatible fixed-Fang sample. */
	List<Object> fangGroupInputs(NexSummary.Sample sample)
	{
		var state=sample.state; Set<Integer> carriedAlternatives=new TreeSet<>();
		if(state.inventory!=null)
		{ state.inventory.forEach((id,n)->{ if(n>0 && melee.containsKey(id)) { carriedAlternatives.add(id); } }); }
		// Match the inputs consumed by ordinary melee and the shared package validator.
		// Incidental HP/Prayer, energy, dose counts and quiver polls cannot split this fixed-Fang sample.
		return Arrays.asList(state.slots,state.realLevels,state.visibleLevels.get("ATTACK"),state.visibleLevels.get("STRENGTH"),
			state.prayers,state.style,state.inventory!=null,Set.copyOf(carriedAlternatives),state.resources.getOrDefault("inventory-slots-used",-1));
	}
	/** Same complete-package search, restricted to the captured Fang and style. */
	List<NexOwnedResult.Choice> fangChoices(NexSummary.Sample s,AccountSnapshot account,Preferences prefs,
		java.util.function.BooleanSupplier current)
	{
		CombatSetup before=setup(s,s.state.slots);
		String gap=contextGap(s); if(gap==null) { gap=validate(s,before,account,prefs,false); }
		Integer weaponLock=prefs.nex.locks.get("melee/weapon");
		if(weaponLock!=null && !weaponLock.equals(s.state.slots.get("weapon"))) { gap="Fixed Fang conflicts with weapon lock"; }
		if(gap!=null) { return List.of(new NexOwnedResult.Choice(s,null,null,gap,true,List.of())); }
		Set<Integer> pool=new TreeSet<>(); s.state.slots.values().stream().filter(id->id>0).forEach(pool::add);
		if(s.state.inventory!=null) { s.state.inventory.forEach((id,n)->{ if(n>0) { pool.add(id); } }); }
		List<NexOwnedResult.Choice> choices=new ArrayList<>();
		choices.add(search(s,account,prefs,before,pool,new ArrayList<>(),false,current,true));
		if(account.bank.known())
		{
			pool.addAll(account.bank.value());
			choices.add(search(s,account,prefs,before,pool,new ArrayList<>(),true,current,true));
		}
		return List.copyOf(choices);
	}
	/** One exact hypothetical item on the observed baseline. Equip/lock/input checks still apply. */
	List<ComparisonResult> fangUpgrades(NexSummary.Sample sample,CombatSetup observed,AccountSnapshot account,Preferences prefs)
	{
		var state=sample.state;
		var baseline=new CombatState(observed.slots,state.realLevels,state.visibleLevels,state.prayers,state.style,
			state.specialEnergy,state.specialSelected,state.inventory,state.resources);
		var s=new NexSummary.Sample(sample.tick,sample.phase,sample.window,sample.target,"melee",sample.animation,baseline,sample.targetState);
		List<ComparisonResult> upgrades=new ArrayList<>();
		for(MeleeItem item:catalog.melee.items)
		{
			if(item.slot.equals("weapon") || item.slot.equals("ring") || Objects.equals(observed.slots.get(item.slot),item.id)) { continue; }
			Map<String,Integer> slots=new HashMap<>(observed.slots); slots.put(item.slot,item.id);
			try
			{
				var pair=compare(s,setup(s,slots),account,prefs,true,Set.of(item.id));
				if(pair.before!=null && pair.after!=null) { upgrades.add(pair); }
			} catch(IllegalArgumentException invalid) { /* Unsupported forms retain no fabricated number. */ }
		}
		return List.copyOf(upgrades);
	}
	private NexOwnedResult.Choice search(NexSummary.Sample s,AccountSnapshot account,Preferences prefs,CombatSetup before,
		Set<Integer> pool,List<String> coverage,boolean nextTrip,java.util.function.BooleanSupplier current)
	{ return search(s,account,prefs,before,pool,coverage,nextTrip,current,false); }
	private NexOwnedResult.Choice search(NexSummary.Sample s,AccountSnapshot account,Preferences prefs,CombatSetup before,
		Set<Integer> pool,List<String> coverage,boolean nextTrip,java.util.function.BooleanSupplier current,boolean fixedWeapon)
	{
			Search search=new Search(s,account,prefs,before,nextTrip,current);
			List<List<Integer>> options=new ArrayList<>();
			for(String slot:PoweredMagic.SLOTS)
			{
				TreeSet<Integer> ids=new TreeSet<>(); ids.add(before.slots.get(slot));
				for(int id:pool) { if(slot.equals(slot(s.role,id))) { ids.add(id); } }
				if(slot.equals("shield")) { ids.add(0); } // Only accepted when forced by a two-handed weapon or observed empty.
				Integer lock=slot.equals("ring")?Integer.valueOf(25975):prefs.nex.locks.get(s.role+"/"+slot);
				if(lock!=null) { ids.clear(); ids.add(lock); }
				if(fixedWeapon && slot.equals("weapon")) { ids.clear(); ids.add(before.slots.get(slot)); }
				options.add(new ArrayList<>(ids));
			}
			search.walk(0,new LinkedHashMap<>(),options,pool);
			if(!search.exhaustive) { coverage.add("Search work limit reached; result is the best found, not an exhaustive optimum."); }
			String gap=search.best==null?"No complete supported package satisfies the planning locks and observed stock.":null;
			ComparisonResult compared=search.best==null?null:compare(s,search.best,account,prefs,nextTrip);
			List<ComparisonResult> pairs=new ArrayList<>();
			for(int id:pool)
			{
				String slot=slot(s.role,id);
				if(slot==null || Objects.equals(before.slots.get(slot),id)) { continue; }
				Map<String,Integer> slots=new HashMap<>(before.slots); slots.put(slot,id);
				if(slot.equals("weapon") && (s.role.equals("melee")?melee.get(id).twoHanded:ranged.get(id).twoHanded)) { slots.put("shield",0); }
				try
				{
					ComparisonResult pair=compare(s,setup(s,slots),account,prefs,nextTrip);
					if(pair.before!=null) { pairs.add(pair); }
				} catch(IllegalArgumentException ex) { /* Kept as a coverage gap; never manufacture a result. */ }
			}
			Set<Integer> skipped=new TreeSet<>(pool);
			if(s.role.equals("melee")) { skipped.retainAll(catalog.melee.unsupportedWeapons.keySet()); }
			else { skipped.retainAll(catalog.ranged.unsupportedWeapons.keySet()); }
			if(!skipped.isEmpty()) { coverage.add("Unmodelled alternatives in this search: "+skipped+". Supported pairwise results remain valid; global best is unknown."); }
			return new NexOwnedResult.Choice(s,search.best,compared,gap,search.exhaustive,coverage,nextTrip,pairs);
	}

	/** Explicit pairwise scenarios remain available even if another owned family has no model. */
	public ComparisonResult compare(NexSummary.Sample sample, CombatSetup candidate, AccountSnapshot account, Preferences prefs)
	{ return compare(sample,candidate,account,prefs,false); }
	private ComparisonResult compare(NexSummary.Sample sample, CombatSetup candidate, AccountSnapshot account, Preferences prefs,boolean nextTrip)
	{ return compare(sample,candidate,account,prefs,nextTrip,Set.of()); }
	private ComparisonResult compare(NexSummary.Sample sample, CombatSetup candidate, AccountSnapshot account, Preferences prefs,boolean nextTrip,Set<Integer> hypothetical)
	{
		CombatSetup before=null; String gap=contextGap(sample);
		try
		{
			if(gap==null) { before=setup(sample,sample.state.slots); gap=validate(sample,before,account,prefs,false); }
			if(gap==null) { gap=validate(sample,candidate,account,prefs,true,nextTrip,hypothetical); }
		}
		catch(IllegalArgumentException ex) { gap=ex.getMessage(); }
		EnumMap<ComparisonResult.Metric,PoweredMagic.Status> support=new EnumMap<>(ComparisonResult.Metric.class);
		for(ComparisonResult.Metric m:ComparisonResult.Metric.values())
		{ support.put(m,gap!=null || m==ComparisonResult.Metric.EXPECTED_KILL_TIME || m==ComparisonResult.Metric.TRIP_PERFORMANCE
			?PoweredMagic.Status.UNSUPPORTED:PoweredMagic.Status.CONDITIONAL); }
		PoweredMagic.Metrics b=gap==null?metrics(sample,before):null, a=gap==null?metrics(sample,candidate):null;
		List<String> assumptions=new ArrayList<>();
		assumptions.add("Nex base-stat ordinary active-attack scenario: Defence 260, stab/slash/crush 40/140/60, ranged light/standard/heavy 190/190/150, Magic 230, magic attack 100, size 3. No inferred defence reduction, phase throughput, attack attribution or seconds saved.");
		assumptions.add("Observed state at tick "+sample.tick+" · "+sample.phase+" / "+sample.window+" / "+sample.target+". Same visible levels "+new TreeMap<>(sample.state.visibleLevels)+", active prayers "+new TreeSet<>(sample.state.prayers)+" and weapon style button "+sample.state.style+". Current state is not proof of a launched attack.");
		assumptions.add("Each alternative is independent. No summing role gains, changing supplies, assigning spec energy, healing or treating worn time as attack share. Charges and sufficient ammunition for an entire kill remain unverified; this is offence if supplied.");
		if(nextTrip) { assumptions.add("Owned bank preparation scenario, not available during the captured kill. Bank observation may be stale; complete trip packing and supply capacity are unverified."); }
		if(before!=null && before.loadedAmmo>0) { assumptions.add("Selected projectile from captured public quiver counters: item "+before.loadedAmmo+". Worn blessing remains in the actual slots. Counter mappings still need live-client validation."); }
		TreeSet<String> sourceIds=new TreeSet<>(target.sources);
		if(before!=null && candidate!=null)
		{
			for(CombatSetup setup:List.of(before,candidate))
			{
				for(int id:setup.slots.values())
				{
					if(sample.role.equals("melee") && melee.containsKey(id)) { sourceIds.addAll(melee.get(id).sources); }
					if(sample.role.equals("ranged") && ranged.containsKey(id)) { sourceIds.addAll(ranged.get(id).sources); }
				}
				if(sample.role.equals("ranged") && ranged.containsKey(setup.loadedAmmo)) { sourceIds.addAll(ranged.get(setup.loadedAmmo).sources); }
			}
		}
		sourceIds.addAll(sample.role.equals("melee")?catalog.melee.sources:catalog.ranged.sources);
		assumptions.add("Models "+VERSION+" / "+(sample.role.equals("melee")?MeleeModel.VERSION:RangedModel.VERSION)+"; knowledge "+catalog.version+"; reviewed sources "+sourceIds.stream().map(id->id+"@"+catalog.sources.get(id).revision).collect(Collectors.joining(", ")));
		return new ComparisonResult(before,candidate,b,a,target,support,gap==null?"Conditional ordinary offence; not measured Nex performance":gap,
			VERSION,catalog.version,assumptions,true,gap==null?Map.of():Map.of(ComparisonResult.Metric.THEORETICAL_DPS,gap));
	}

	public CombatSetup setup(NexSummary.Sample s, Map<String,Integer> slots)
	{
		Map<String,String> origins=new TreeMap<>(); slots.forEach((slot,id)->origins.put(slot,
			Objects.equals(s.state.slots.get(slot),id)?"Actually worn at tick "+s.tick:"Proposed owned alternative; observed history unchanged"));
		if(s.role.equals("melee"))
		{ return packages.build(true,slots,origins,s.state.style,s.state.prayers,0); }
		if(!s.role.equals("ranged")) { throw new IllegalArgumentException("Special attack effects and finite energy belong to N3"); }
		int loadedAmmo=0;
		RangedItem weapon=ranged.get(slots.get("weapon")), ammo=ranged.get(slots.get("ammo"));
		if(weapon!=null && weapon.weapon!=null && RangedModel.ammoKind(weapon)!=RangedItem.Ammo.NONE
			&& RangedModel.ammoKind(weapon)!=RangedItem.Ammo.DART && !RangedModel.compatible(weapon,ammo))
		{
			RangedItem cape=ranged.get(slots.get("cape"));
			int id=s.state.resources.getOrDefault("quiver-ammo-id-raw",-1);
			if(cape==null || !cape.label.toLowerCase(Locale.ROOT).contains("quiver") || !RangedModel.compatible(weapon,ranged.get(id))
				|| s.state.resources.getOrDefault("quiver-ammo-quantity-raw",-1)<1) { throw new IllegalArgumentException("Compatible quiver ammunition and quantity unread"); }
			loadedAmmo=id;
		}
		return packages.build(false,slots,origins,s.state.style,s.state.prayers,loadedAmmo);
	}
	private String contextGap(NexSummary.Sample s)
	{
		if(!s.role.equals("melee") && !s.role.equals("ranged")) { return "Special or unclassified role: ordinary attacks cannot establish spec value (N3)."; }
		if(!targetName(s.target).equals("NEX")) { return "Target unknown or minion: target-specific caps/armour require a separate model."; }
		if(!Set.of("SMOKE","SHADOW","BLOOD","ICE").contains(s.phase)) { return "Phase unknown or Zaros: phase-specific prayers/defence are not modelled."; }
		if(s.window!=null && !Set.of("UNKNOWN","NONE","FEAR_THE_SHADOW","EMBRACE_DARKNESS").contains(s.window))
		{ return "Special/minion window: eligible ordinary attack opportunities are not established."; }
		if(s.role.equals("melee") && "EMBRACE_DARKNESS".equals(s.window)) { return "MP2 ranged window; melee availability is not established."; }
		return null;
	}
	private static String targetName(String value) { return value==null?"unknown":value.split("@",2)[0]; }
	private String slot(String role,int id) { return role.equals("melee")?(melee.containsKey(id)?melee.get(id).slot:null):(ranged.containsKey(id)?ranged.get(id).slot:null); }
	private String validate(NexSummary.Sample s,CombatSetup setup,AccountSnapshot account,Preferences prefs,boolean proposed)
	{ return validate(s,setup,account,prefs,proposed,false); }
	private String validate(NexSummary.Sample s,CombatSetup setup,AccountSnapshot account,Preferences prefs,boolean proposed,boolean nextTrip)
	{ return validate(s,setup,account,prefs,proposed,nextTrip,Set.of()); }
	private String validate(NexSummary.Sample s,CombatSetup setup,AccountSnapshot account,Preferences prefs,boolean proposed,boolean nextTrip,Set<Integer> hypothetical)
	{
		if(setup==null || !setup.target.equals(target.id) || setup.family!=(s.role.equals("melee")?CombatSetup.Family.MELEE:CombatSetup.Family.RANGED)) { return "Incompatible role or target"; }
		if(!setup.slots.keySet().equals(new HashSet<>(PoweredMagic.SLOTS))) { return "Complete observed slots required"; }
		if(proposed)
		{
			CombatSetup expected=setup(s,setup.slots);
			if(setup.meleePrayer!=expected.meleePrayer || setup.meleeStyle!=expected.meleeStyle
				|| setup.rangedPrayer!=expected.rangedPrayer || setup.rangedStyle!=expected.rangedStyle
				|| setup.loadedAmmo!=expected.loadedAmmo || setup.onTask || setup.defenceOverride!=-1
				|| setup.souls!=MeleeModel.Souls.START || setup.rangingPotion || setup.meleeBoost!=MeleeModel.Boost.NONE)
			{ return "Candidate must retain the captured prayer, levels and reviewed style mapping"; }
			if(!nextTrip)
			{
				int extra=0;
				Set<Integer> original=new HashSet<>(s.state.slots.values()), proposedItems=new HashSet<>(setup.slots.values());
				for(int id:original) if(id>0 && !proposedItems.contains(id))
				{
					// Only reviewed projectiles can join an existing inventory stack.
					RangedItem item=ranged.get(id);
					if(item==null || item.ammo==RangedItem.Ammo.NONE || s.state.inventory==null || s.state.inventory.getOrDefault(id,0L)<1) { extra++; }
				}
				for(int id:proposedItems) if(id>0 && !original.contains(id)) { extra--; }
				if(extra>0 && s.state.resources.getOrDefault("inventory-slots-used",-1)<0) { return "Additional switch space is unknown"; }
				if(extra>0 && s.state.resources.get("inventory-slots-used")+extra>28) { return "Complete switch needs additional inventory space; supplies are not discarded"; }
			}
		}
		for(String skill:s.role.equals("melee")?List.of("ATTACK","STRENGTH"):List.of("RANGED"))
		{ if(!s.state.visibleLevels.containsKey(skill) || s.state.visibleLevels.get(skill)<1 || s.state.visibleLevels.get(skill)>200) { return "Visible "+skill+" unread"; } }
		for(String slot:PoweredMagic.SLOTS)
		{
			Integer id=setup.slots.get(slot); if(id==null || id<0) { return "Unknown "+slot+" contribution"; }
			if(proposed)
			{
				Integer lock=slot.equals("ring")?Integer.valueOf(25975):prefs.nex.locks.get(s.role+"/"+slot);
				if(lock!=null && !lock.equals(id)) { return "Planning lock conflicts in "+slot; }
				if(id>0 && !s.state.slots.containsValue(id) && (s.state.inventory==null || s.state.inventory.getOrDefault(id,0L)<1)
					&& !hypothetical.contains(id) && !(nextTrip && account.bank.known() && account.bank.value().contains(id))) { return "Item "+id+" not carried at this state; bank/manual ownership is not inventory availability"; }
			}
			if(id==0) { continue; }
			Map<String,Integer> requirements;
			if(s.role.equals("melee"))
			{
				MeleeItem item=melee.get(id); if(item==null || !slot.equals(item.slot)) { return "Unreviewed melee contribution: "+slot+" item "+id; }
				if(item.family==MeleeModel.Weapon.SOULREAPER) { return "Soulreaper stack state not captured"; }
				if(proposed && !s.state.slots.containsValue(id) && item.minimumCombat!=null)
				{
					for(String skill:List.of("ATTACK","STRENGTH","DEFENCE","HITPOINTS","PRAYER","RANGED","MAGIC"))
					{ if(!s.state.realLevels.containsKey(skill)) { return "Contributing combat level unread"; } }
					var l=s.state.realLevels;
					int base=l.get("DEFENCE")+l.get("HITPOINTS")+l.get("PRAYER")/2;
					int offence=Math.max(l.get("ATTACK")+l.get("STRENGTH"),Math.max(l.get("RANGED")*3/2,l.get("MAGIC")*3/2));
					if((base*10+offence*13)/40<item.minimumCombat) { return "Combat level requirement not met"; }
				}
				requirements=item.requirements;
				if(proposed && !s.state.slots.containsValue(id) && item.equipFact!=null && AccountFacts.planning(account,prefs,item.equipFact)!=Truth.TRUE) { return "Equip unlock unconfirmed: "+item.equipFact; }
			}
			else
			{
				RangedItem item=ranged.get(id); if(item==null || !slot.equals(item.slot)) { return "Unreviewed ranged contribution: "+slot+" item "+id; }
				if(item.boltEffect) { return "Enchanted bolt HP/proc effects belong to N3"; }
				if(item.chargedQuiver && item.id!=28955 && item.id!=28957
					&& s.state.resources.getOrDefault("quiver-splinters-raw",-1)<1) { return "Non-blessed quiver splinter stock unread or empty"; }
				requirements=item.requirements;
			}
			if(proposed && !s.state.slots.containsValue(id)) for(var req:requirements.entrySet())
			{ if(s.state.realLevels.getOrDefault(req.getKey(),-1)<req.getValue()) { return "Equip level unconfirmed: "+req.getKey(); } }
		}
		if(setup.slots.get("weapon")<1) { return "No supported weapon"; }
		boolean two=s.role.equals("melee")?melee.get(setup.slots.get("weapon")).twoHanded:ranged.get(setup.slots.get("weapon")).twoHanded;
		if(two && setup.slots.get("shield")!=0) { return "Two-handed weapon conflicts with offhand"; }
		if(s.role.equals("ranged"))
		{
			RangedItem weapon=ranged.get(setup.slots.get("weapon")); if(weapon==null || weapon.weapon==null) { return "No reviewed ranged weapon"; }
			if(RangedModel.ammoKind(weapon)==RangedItem.Ammo.DART) { return "Internal dart loading unknown"; }
			if(RangedModel.ammoKind(weapon)!=RangedItem.Ammo.NONE)
			{
				RangedItem ammo=ranged.get(setup.loadedAmmo>0?setup.loadedAmmo:setup.slots.get("ammo"));
				if(!RangedModel.compatible(weapon,ammo)) { return "Incompatible or unknown ammunition"; }
				if(ammo.boltEffect) { return "Enchanted bolt HP/proc effects require the N3 package plan, including selected quiver ammunition"; }
				if(Objects.equals(s.state.slots.get("ammo"),ammo.id) && s.state.resources.getOrDefault("worn-ammo-quantity",-1)<1) { return "Worn ammunition quantity unread or empty"; }
				if(setup.loadedAmmo>0 && (setup.loadedAmmo!=s.state.resources.getOrDefault("quiver-ammo-id-raw",-1)
					|| s.state.resources.getOrDefault("quiver-ammo-quantity-raw",-1)<1)) { return "Selected quiver projectile is not observed"; }
			}
		}
		try { metrics(s,setup); } catch(IllegalArgumentException | NullPointerException ex) { return "Unsupported complete package: "+ex.getMessage(); }
		return null;
	}
	private PoweredMagic.Metrics metrics(NexSummary.Sample s, CombatSetup setup)
	{ return s.role.equals("melee")?MeleeModel.calculateVisible(s.state.visibleLevels.get("ATTACK"),s.state.visibleLevels.get("STRENGTH"),setup,target,melee)
		:RangedModel.calculateVisible(s.state.visibleLevels.get("RANGED"),setup,target,ranged); }
	private final class Search
	{
		final NexSummary.Sample sample; final AccountSnapshot account; final Preferences prefs; final CombatSetup before;
		final boolean nextTrip; final java.util.function.BooleanSupplier current;
		CombatSetup best; double bestDps=-1; int nodes; boolean exhaustive=true;
		Search(NexSummary.Sample s,AccountSnapshot a,Preferences p,CombatSetup before,boolean nextTrip,java.util.function.BooleanSupplier current)
		{
			sample=s; account=a; prefs=p; this.before=before; this.nextTrip=nextTrip; this.current=current;
			// A search limit must never manufacture a weaker unlocked baseline.
			if(validate(s,before,a,p,true,nextTrip)==null) { best=before; bestDps=metrics(s,before).dps; }
		}
		void walk(int index,Map<String,Integer> slots,List<List<Integer>> options,Set<Integer> carried)
		{
			if(++nodes>MAX_SEARCH_NODES || !current.getAsBoolean()) { exhaustive=false; return; }
			if(index==PoweredMagic.SLOTS.size())
			{
				try
				{
					CombatSetup candidate=setup(sample,slots);
					if(validate(sample,candidate,account,prefs,true,nextTrip)!=null) { return; }
					double dps=metrics(sample,candidate).dps;
					if(dps>bestDps+1e-12 || Math.abs(dps-bestDps)<1e-12 && changes(candidate)<changes(best)) { best=candidate; bestDps=dps; }
				} catch(IllegalArgumentException ex) { /* Explicit coverage is scoped below; never replace the baseline. */ }
				return;
			}
			String slot=PoweredMagic.SLOTS.get(index);
			for(int id:options.get(index))
			{
				if(!exhaustive) { break; }
				if(id>0 && !carried.contains(id)) { continue; }
				// Never discard an observed offhand unless the proposed weapon requires it.
				if(slot.equals("shield") && id==0 && before.slots.get("shield")!=0)
				{
					Integer weapon=slots.get("weapon");
					boolean two=sample.role.equals("melee")?melee.containsKey(weapon) && melee.get(weapon).twoHanded:ranged.containsKey(weapon) && ranged.get(weapon).twoHanded;
					if(!two) { continue; }
				}
				slots.put(slot,id); walk(index+1,slots,options,carried);
			}
			slots.remove(slot);
		}
		int changes(CombatSetup s) { return s==null?Integer.MAX_VALUE:(int)PoweredMagic.SLOTS.stream().filter(slot->!Objects.equals(s.slots.get(slot),before.slots.get(slot))).count(); }
	}
}
