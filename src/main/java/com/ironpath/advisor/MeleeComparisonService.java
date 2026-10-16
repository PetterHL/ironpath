package com.ironpath.advisor;

import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Truth;
import com.ironpath.preferences.Preferences;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import static com.ironpath.advisor.ComparisonResult.Metric.*;

/** Pure ordinary melee proposals. Searches complete compatible packages, including accuracy/strength trade-offs. */
public final class MeleeComparisonService
{
	private final KnowledgeCatalog catalog;
	private final Map<Integer,MeleeItem> items;
	public MeleeComparisonService(KnowledgeCatalog catalog)
	{ this.catalog=catalog; items=catalog.melee.items.stream().collect(Collectors.toUnmodifiableMap(i->i.id,i->i)); }
	private TargetProfile target(String id)
	{ return catalog.comparison.targets.stream().filter(t->t.id.equals(id)).findFirst().orElse(null); }
	public int targetDefence(String id) { TargetProfile t=target(id); return t==null || t.defenceLevel==null ? -1 : t.defenceLevel; }
	private String equipError(AccountSnapshot a, MeleeItem item)
	{
		if(item.minimumCombat!=null)
		{
			for(String skill:List.of("ATTACK","STRENGTH","DEFENCE","HITPOINTS","PRAYER","RANGED","MAGIC"))
			{ if(a.level(skill)==null) { return item.label+" requires confirmed combat level "+item.minimumCombat+"; a contributing skill is unread."; } }
			int base=a.level("DEFENCE")+a.level("HITPOINTS")+a.level("PRAYER")/2;
			int attack=Math.max(a.level("ATTACK")+a.level("STRENGTH"),Math.max(a.level("RANGED")+a.level("RANGED")/2,a.level("MAGIC")+a.level("MAGIC")/2));
			if((base*10+attack*13)/40<item.minimumCombat) { return item.label+" requires combat level "+item.minimumCombat+"."; }
		}
		for (Map.Entry<String,Integer> r : item.requirements.entrySet())
		{ if (a.atLeast(r.getKey(),r.getValue())!=Truth.TRUE) { return item.label+" requires confirmed "+r.getValue()+" "+r.getKey()+"."; } }
		return null;
	}
	private List<MeleeItem> owned(String slot, AccountSnapshot a)
	{ return items.values().stream().filter(i->i.slot.equals(slot) && a.observedItem(i.id)==Truth.TRUE && equipError(a,i)==null)
		.sorted(Comparator.comparingInt(i->i.id)).collect(Collectors.toList()); }
	private List<MeleeItem> eligibleOwned(String slot, AccountSnapshot a, Preferences p)
	{ return owned(slot,a).stream().filter(i->i.equipFact==null || AccountFacts.planning(a,p,i.equipFact)==Truth.TRUE).collect(Collectors.toList()); }
	public Map<String,Map<String,String>> referenceChoices(AccountSnapshot a)
	{
		Map<String,Map<String,String>> result=new LinkedHashMap<>();
		for (String slot : PoweredMagic.SLOTS)
		{
			Map<String,String> options=new LinkedHashMap<>();
			if (!slot.equals("weapon")) { options.put("0","Deliberately empty "+slot); }
			owned(slot,a).forEach(i->options.put(Integer.toString(i.id),i.label)); result.put(slot,options);
		}
		Map<String,String> targets=new LinkedHashMap<>();
		catalog.comparison.targets.stream().filter(t->t.meleeStab!=null || !t.scalingSupported).forEach(t->targets.put(t.id,t.name));
		result.put("target",targets);
		owned("weapon",a).forEach(i->{ Map<String,String> styles=new LinkedHashMap<>(); i.styles.forEach(style->styles.put(style.name(),style.label())); result.put("styles-"+i.id,styles); });
		return result;
	}
	public CombatSetup propose(AccountSnapshot a) { return propose(a,Preferences.EMPTY,"neutral-zero-defence"); }
	public CombatSetup propose(AccountSnapshot a, Preferences p) { return propose(a,p,"neutral-zero-defence"); }
	public CombatSetup propose(AccountSnapshot a, String targetId) { return propose(a,Preferences.EMPTY,targetId); }
	public CombatSetup propose(AccountSnapshot a, Preferences p, String targetId)
	{
		if (target(targetId)==null) { throw new IllegalArgumentException("Unknown melee target"); }
		Map<String,Integer> slots=new LinkedHashMap<>(); Map<String,String> origins=new LinkedHashMap<>();
		for (String slot : PoweredMagic.SLOTS)
		{
			Integer equipped=a.equipmentSlots.known() ? a.equipmentSlots.value().get(slot) : null;
			int id=equipped!=null ? equipped : a.equipment.known() ? 0 : -1;
			List<MeleeItem> options=eligibleOwned(slot,a,p); if (options.isEmpty()) { options=owned(slot,a); } if (!options.isEmpty()) { id=options.get(0).id; }
			slots.put(slot,id); origins.put(slot,id>0 ? "Observed reference item; unsupported contributions remain gaps."
				: id==0 ? "Empty reference assumption; no supported owned contribution observed. Other storage may be unread." : "Equipment unread; contribution unknown.");
		}
		if (!a.equipmentSlots.known() && a.equipment.known() && a.equipment.value().stream().anyMatch(id->!items.containsKey(id)))
		{ slots.put("head",-1); origins.put("head","Unsupported equipment without slot evidence."); }
		CombatSetup seed=CombatSetup.melee(slots,targetId,origins);
		MeleeItem selected=items.get(slots.get("weapon")); if (selected!=null && !selected.styles.isEmpty()) { seed=seed.meleeCopy(slots,origins,selected.styles.get(0)); }
		CombatSetup strongest=strongest(a,p,seed,null);
		return strongest==null ? seed : strongest;
	}
	public CombatSetup reconsider(AccountSnapshot a,Preferences p,CombatSetup seed)
	{
		if(seed==null || seed.minimalTemplate || seed.provenance.containsKey("melee-style") || seed.provenance.values().stream().anyMatch(origin->origin.startsWith("Explicit reference correction"))) { return seed; }
		CombatSetup best=strongest(a,p,seed,null);
		return best==null || best.slots.equals(seed.slots) && best.meleeStyle==seed.meleeStyle ? seed : best;
	}
	/** Retain every non-dominated (attack,strength) total. A strength-only greedy choice misses real accuracy breakpoints. */
	private List<Package> frontier(List<Package> candidates)
	{
		Map<String,List<Package>> groups=new LinkedHashMap<>();
		for (Package p:candidates) { groups.computeIfAbsent(MeleeEffects.signature(p.slots,items),key->new ArrayList<>()).add(p); }
		return groups.values().stream().flatMap(group->ordinaryFrontier(group).stream()).collect(Collectors.toList());
	}
	private List<Package> ordinaryFrontier(List<Package> candidates)
	{
		candidates.sort(Comparator.comparingInt((Package p)->p.attack).reversed().thenComparing(Comparator.comparingInt((Package p)->p.strength).reversed()));
		List<Package> result=new ArrayList<>(); int strength=Integer.MIN_VALUE;
		for (Package p : candidates) { if (p.strength>strength) { result.add(p); strength=p.strength; } }
		return result;
	}
	private static final class Package
	{
		final Map<String,Integer> slots; final int attack,strength;
		Package(Map<String,Integer> slots,int attack,int strength) { this.slots=slots; this.attack=attack; this.strength=strength; }
	}
	private List<MeleeModel.Style> candidateStyles(MeleeItem weapon, CombatSetup seed, MeleeItem candidate)
	{
		if (candidate==null) { return weapon.styles; }
		if (weapon.id==seed.slots.get("weapon") && weapon.styles.contains(seed.meleeStyle)) { return List.of(seed.meleeStyle); }
		String stance=seed.meleeStyle.name().substring(seed.meleeStyle.name().indexOf('_'));
		List<MeleeModel.Style> matching=weapon.styles.stream().filter(style->style.name().endsWith(stance)).collect(Collectors.toList());
		return matching.isEmpty() ? weapon.styles : matching;
	}
	private CombatSetup strongest(AccountSnapshot a, Preferences p, CombatSetup seed, MeleeItem candidate)
	{
		TargetProfile t=target(seed.target);
		if (t==null || !(t.scalingSupported || t.meleeScenario) || t.meleeStab==null || a.level("ATTACK")==null || a.level("STRENGTH")==null) { return null; }
		CombatSetup best=null; double dps=-1;
		List<MeleeItem> weapons=candidate!=null && candidate.slot.equals("weapon") ? List.of(candidate) : eligibleOwned("weapon",a,p);
		for (MeleeItem weapon : weapons) for (MeleeModel.Style style : candidateStyles(weapon,seed,candidate))
		{
			if (candidate!=null && candidate.slot.equals("shield") && weapon.twoHanded) { continue; }
			List<Package> options=List.of(new Package(new LinkedHashMap<>(),0,0));
			for (String slot : PoweredMagic.SLOTS)
			{
				List<Integer> ids;
				if (slot.equals("weapon")) { ids=List.of(weapon.id); }
				else if (slot.equals("shield") && weapon.twoHanded) { ids=List.of(0); }
				else if (candidate!=null && candidate.slot.equals(slot)) { ids=List.of(candidate.id); }
				else if (candidate!=null && (!slot.equals("shield") || !items.get(seed.slots.get("weapon")).twoHanded)) { ids=List.of(seed.slots.get(slot)); }
				else
				{
					ids=eligibleOwned(slot,a,p).stream().map(i->i.id).collect(Collectors.toList());
					if (ids.isEmpty())
					{
						Integer original=slot.equals("shield") && !seed.minimalTemplate && a.equipmentSlots.known()
							? a.equipmentSlots.value().get("shield") : null;
						ids=List.of(original!=null && original>0 ? original : seed.slots.get(slot));
					}
				}
				List<Package> next=new ArrayList<>();
				for (Package pack : options) for (int id : ids)
				{
					Map<String,Integer> slots=new LinkedHashMap<>(pack.slots); slots.put(slot,id); MeleeItem item=items.get(id);
					next.add(new Package(slots,pack.attack+(item==null ? 0 : item.attack(style.type)),pack.strength+(item==null ? 0 : item.strength)));
				}
				options=frontier(next);
			}
			for (Package pack : options)
			{
				Map<String,String> origins=new LinkedHashMap<>(seed.provenance);
				pack.slots.forEach((slot,id)->{ if (id>0 && (candidate==null || !java.util.Objects.equals(seed.slots.get(slot),id))) { origins.put(slot,candidate!=null && id==candidate.id ? "Hypothetical supplied candidate; not observed ownership or charge quantity."
					: "Strongest supported observed ordinary melee package; "+a.itemEvidence(id)); } });
				if (weapon.twoHanded) { origins.put("shield","Excluded for two-handed weapon."); }
				else if (candidate!=null && items.get(seed.slots.get("weapon")).twoHanded && pack.slots.get("shield")==0)
				{ origins.put("shield","Explicit empty one-handed off-hand alternative; no supported owned shield observed."); }
				CombatSetup option=seed.meleeCopy(pack.slots,origins,style);
				if (validate(a,p,option,candidate==null ? 0 : candidate.id)!=null) { continue; }
				double value;
				try { value=MeleeModel.calculate(a.level("ATTACK"),a.level("STRENGTH"),option,t,items).dps; }
				catch (IllegalArgumentException unsupported) { continue; }
				if (value>dps) { dps=value; best=option; }
			}
		}
		return best;
	}
	public String validate(AccountSnapshot a, Preferences p, CombatSetup s) { return validate(a,p,s,0); }
	private String validate(AccountSnapshot a, Preferences p, CombatSetup s, int candidateId)
	{
		if (s==null || s.family!=CombatSetup.Family.MELEE || s.meleePrayer==null || s.meleeBoost==null || s.meleeStyle==null
			|| s.souls==null || !s.slots.keySet().equals(new java.util.HashSet<>(PoweredMagic.SLOTS)) || target(s.target)==null)
		{ return "Unknown melee reference inputs."; }
		if (!s.minimalTemplate && !a.equipment.known()) { return "Equipment unread; explicitly correct the reference instead of treating unknown gear as empty."; }
		for (String skill : List.of("ATTACK","STRENGTH"))
		{ if (a.level(skill)==null || a.level(skill)<1 || a.level(skill)>99) { return "Known real "+skill+" 1–99 is required."; } }
		for (String slot : PoweredMagic.SLOTS)
		{
			Integer id=s.slots.get(slot); if (id==null || id<0) { return "Unknown melee reference slot: "+slot+"."; }
			if (id==0) { if (slot.equals("weapon")) { return "No observed supported melee weapon; manual ownership cannot establish usability."; } continue; }
			MeleeItem item=items.get(id);
			if (item==null || !slot.equals(item.slot)) { return "Unreviewed melee contribution in "+slot+": item "+id+". Correct the reference or explicitly exclude that slot."; }
			if (id!=candidateId && a.observedItem(id)!=Truth.TRUE) { return item.label+" is not observed; manual ownership cannot create a usable baseline."; }
			String error=equipError(a,item); if (error!=null) { return error; }
			if (item.equipFact!=null && AccountFacts.planning(a,p,item.equipFact)!=Truth.TRUE)
			{ return item.label+" requires confirmed "+item.equipFact+" completion; ownership alone is not an equip unlock."; }
		}
		MeleeItem weapon=items.get(s.slots.get("weapon"));
		if (!weapon.styles.contains(s.meleeStyle)) { return "Unsupported attack type/stance for "+weapon.label+"; choose one of its actual melee styles."; }
		if (weapon.twoHanded && s.slots.get("shield")!=0) { return "Two-handed melee weapon cannot use an off-hand."; }
		TargetProfile t=target(s.target);
		if (s.onTask && !t.slayer) { return "No reviewed eligible Slayer assignment for this target; choose off-task."; }
		if (s.defenceOverride < -1 || s.defenceOverride>=0 && (t.defenceLevel==null || s.defenceOverride>t.defenceLevel)) { return "Assumed Defence must be between zero and the target reference level; no debuff outcome is inferred."; }
		if (weapon.family==MeleeModel.Weapon.SCYTHE && t.size==null) { return "Scythe multi-hit offence requires reviewed target size."; }
		if (s.meleePrayer==MeleeModel.Prayer.INCREDIBLE_ULTIMATE && a.atLeast("PRAYER",34)!=Truth.TRUE) { return "34 Prayer is unconfirmed for the paired 15% melee prayers."; }
		if (s.meleePrayer==MeleeModel.Prayer.CHIVALRY || s.meleePrayer==MeleeModel.Prayer.PIETY)
		{
			boolean piety=s.meleePrayer==MeleeModel.Prayer.PIETY;
			if (a.atLeast("PRAYER",piety ? 70 : 60)!=Truth.TRUE || a.atLeast("DEFENCE",piety ? 70 : 65)!=Truth.TRUE
				|| AccountFacts.planning(a,p,"knight-waves")!=Truth.TRUE)
			{ return "King's Ransom/Knight Waves unlock and Prayer/Defence requirements are unconfirmed for "+s.meleePrayer+"."; }
		}
		return null;
	}
	public ComparisonResult compare(AccountSnapshot a, Preferences p, CombatSetup before, KnowledgeCatalog.Upgrade upgrade)
	{ return compare(a,p,before,upgrade.itemId); }
	public ComparisonResult compare(AccountSnapshot a, Preferences p, CombatSetup before, int candidateId)
	{
		MeleeItem candidate=items.get(candidateId); boolean weapon=candidate!=null && candidate.slot.equals("weapon");
		String error=validate(a,p,before); if (error!=null) { return unavailable(before,error,weapon); }
		TargetProfile t=target(before.target);
		if (candidate==null) { return unavailable(before,"Candidate melee family is unreviewed.",weapon); }
		if (!(t.scalingSupported || t.meleeScenario) || t.meleeStab==null) { return unavailable(before,"Target melee defence/phase/scaling unsupported; no raid scaling is invented.",weapon); }
		// A target/task/stack/debuff change can change the strongest owned package. Keep focused slot/style corrections.
		before=reconsider(a,p,before);
		if (candidate.equipFact!=null && AccountFacts.planning(a,p,candidate.equipFact)!=Truth.TRUE)
		{ return unavailable(before,candidate.label+" requires confirmed "+candidate.equipFact+" completion.",weapon); }
		error=equipError(a,candidate); if (error!=null) { return unavailable(before,error+" Retain the plan-ahead route.",weapon); }
		CombatSetup after=strongest(a,p,before,candidate);
		if (after==null) { return unavailable(before,"No complete supported compatible candidate melee package.",weapon); }
		PoweredMagic.Metrics b=MeleeModel.calculate(a.level("ATTACK"),a.level("STRENGTH"),before,t,items);
		PoweredMagic.Metrics n=MeleeModel.calculate(a.level("ATTACK"),a.level("STRENGTH"),after,t,items);
		boolean dpsFirst=weapon || !before.slots.get("weapon").equals(after.slots.get("weapon")) || before.meleeStyle!=after.meleeStyle;
		EnumMap<ComparisonResult.Metric,PoweredMagic.Status> support=support(before.confirmed ? PoweredMagic.Status.SUPPORTED : PoweredMagic.Status.CONDITIONAL);
		Map<ComparisonResult.Metric,String> gaps=new EnumMap<>(ComparisonResult.Metric.class);
		String competitors=catalog.melee.unsupportedWeapons.entrySet().stream().filter(e->a.observedItem(e.getKey())==Truth.TRUE)
			.sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).collect(Collectors.joining(", "));
		if (!competitors.isEmpty())
		{
			String gap="Observed competing melee family not modelled: "+competitors+". This supported reference cannot establish a strongest-owned DPS upgrade.";
			support.put(THEORETICAL_DPS,PoweredMagic.Status.UNSUPPORTED); gaps.put(THEORETICAL_DPS,gap);
		}
		List<String> assumptions=new ArrayList<>();
		assumptions.add("Complete ordinary melee packages. Automatic proposals search the strongest supported owned alternative; explicit corrections select a focused reference. Candidate packages preserve unrelated reference corrections and use the same stance where the new weapon offers it; otherwise a compatible style is proposed. No superiority claim against unreviewed families. Candidate supplies/charges are hypothetical; current charges are not measured.");
		assumptions.add("Baseline slots: "+description(before)); assumptions.add("Candidate slots: "+description(after));
		assumptions.add("Real Attack "+a.level("ATTACK")+", Strength "+a.level("STRENGTH")+"; baseline "+before.assumptionLabel()+"; candidate "+after.assumptionLabel()+". Prayers/boosts are proposed assumptions, not live state. Super attack/strength use full 5 + floor(15% base); potion decay excluded.");
		assumptions.add(t.name+"; reference Defence "+t.defenceLevel+"; stab/slash/crush bonuses "+t.meleeStab+"/"+t.meleeSlash+"/"+t.meleeCrush+"; size "+t.size+". Defence rolls "+b.defenceRoll+" → "+n.defenceRoll+". Any corrected Defence is an explicit scenario, not an observed debuff or assumed teammate weapon. No automatic raid scaling.");
		assumptions.add("Attack bonus "+b.attackBonus+" → "+n.attackBonus+"; melee strength "+(int)b.damagePercent+" → "+(int)n.damagePercent+". Ordinary successful zero becomes 1; misses stay zero. Attack interval "+b.attackTicks+" → "+n.attackTicks+" ticks.");
		assumptions.add("Single-target ordinary attacks, including reviewed Fang, size-based Scythe, Soulreaper, Salve/task, lance/demonbane and July Inquisitor rules. Salve overrides Slayer bonuses on undead. Task is a planning assumption, not observed assignment. Demonbane effectiveness is target-specific. Void/other sets, specials, poison, healing, kill time and trips remain gaps. Blood fury healing and Lightbearer special energy are not valued as ordinary DPS; lower offence never establishes lower sustain/utility.");
		assumptions.add("Fang rerolls accuracy only on stab: shared defence roll outside ToA, independent attack/defence attempts inside. Its damage range contracts on both stab and slash. Scythe max hit is the sum of independently rolled target-size hits, not a single guaranteed hit. Soul stacks are pre-attack; gained stacks apply to the next swing. Ramp is the average offence over the next five swings from zero, not fully stacked sustained DPS. July Soulreaper stacks cause no HP drain; retention/decay, special healing and encounter downtime are not estimated.");
		assumptions.add("Abyssal tentacle consumes its whip after 10,000 attacks including misses; the kraken tentacle remains. Combining is irreversible. Whip supply and competing enhanced-trident use remain resource choices.");
		TreeSet<String> sources=new TreeSet<>(catalog.melee.sources); sources.addAll(t.sources);
		for (CombatSetup s : List.of(before,after)) { s.slots.values().stream().filter(id->id>0).forEach(id->sources.addAll(items.get(id).sources)); }
		assumptions.add("Model "+MeleeModel.VERSION+"; knowledge "+catalog.version+"; sources "+sources.stream().map(id->id+"@"+catalog.sources.get(id).revision).collect(Collectors.joining(", ")));
		boolean ramp=before.souls==MeleeModel.Souls.RAMP &&
			(items.get(before.slots.get("weapon")).family==MeleeModel.Weapon.SOULREAPER || items.get(after.slots.get("weapon")).family==MeleeModel.Weapon.SOULREAPER);
		String reason=gaps.getOrDefault(THEORETICAL_DPS,ramp
			? "Next-five-swing average from zero soul stacks; not full-stack sustained DPS or measured performance." : "Reference estimate, not measured performance.");
		return new ComparisonResult(before,after,b,n,t,support,reason,MeleeModel.VERSION,catalog.version,assumptions,dpsFirst,gaps,
			ramp ? ComparisonResult.OffenceScope.NEXT_FIVE_SWINGS : ComparisonResult.OffenceScope.STEADY_REFERENCE);
	}
	private String description(CombatSetup s)
	{ return PoweredMagic.SLOTS.stream().map(slot->slot+"="+(s.slots.get(slot)==0 ? "deliberately empty" : items.get(s.slots.get(slot)).label)
		+" ("+s.provenance.getOrDefault(slot,"explicit correction")+")").collect(Collectors.joining("; ")); }
	private EnumMap<ComparisonResult.Metric,PoweredMagic.Status> support(PoweredMagic.Status status)
	{
		EnumMap<ComparisonResult.Metric,PoweredMagic.Status> result=new EnumMap<>(ComparisonResult.Metric.class);
		for (ComparisonResult.Metric m : ComparisonResult.Metric.values()) { result.put(m,m==EXPECTED_KILL_TIME || m==TRIP_PERFORMANCE ? PoweredMagic.Status.UNSUPPORTED : status); } return result;
	}
	private ComparisonResult unavailable(CombatSetup s, String reason, boolean weapon)
	{ return new ComparisonResult(s,null,null,null,s==null ? null : target(s.target),support(PoweredMagic.Status.UNSUPPORTED),reason,MeleeModel.VERSION,catalog.version,List.of(),weapon,Map.of(THEORETICAL_DPS,reason)); }
}
