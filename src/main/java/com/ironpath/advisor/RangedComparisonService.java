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

/** Pure ranged proposal and comparison, using complete compatible owned packages per target. */
public final class RangedComparisonService
{
	private final KnowledgeCatalog catalog;
	private final Map<Integer,RangedItem> items;
	public RangedComparisonService(KnowledgeCatalog catalog)
	{ this.catalog = catalog; items = catalog.ranged.items.stream().collect(Collectors.toUnmodifiableMap(i -> i.id,i -> i)); }
	private TargetProfile target(String id)
	{ return catalog.comparison.targets.stream().filter(t -> t.id.equals(id)).findFirst().orElse(null); }
	public Map<String,String> choices(String slot, AccountSnapshot account)
	{
		Map<String,String> result = new LinkedHashMap<>();
		if (!slot.equals("weapon")) { result.put("0","Deliberately empty " + slot); }
		items.values().stream().filter(i -> i.slot.equals(slot) && account.observedItem(i.id) == Truth.TRUE)
			.sorted(Comparator.comparingInt(i -> i.id)).forEach(i -> result.put(Integer.toString(i.id),i.label));
		return result;
	}
	public Map<String,Map<String,String>> referenceChoices(AccountSnapshot account)
	{
		Map<String,Map<String,String>> result = new LinkedHashMap<>();
		PoweredMagic.SLOTS.forEach(slot -> result.put(slot,choices(slot,account)));
		Map<String,String> targets = new LinkedHashMap<>();
		catalog.comparison.targets.stream().filter(t -> t.rangedStandard != null || !t.scalingSupported).forEach(t -> targets.put(t.id,t.name));
		result.put("target",targets);
		Map<String,String> darts = new LinkedHashMap<>();
		items.values().stream().filter(i -> i.ammo == RangedItem.Ammo.DART && account.observedItem(i.id) == Truth.TRUE)
			.sorted(Comparator.comparingInt(i -> i.id)).forEach(i -> darts.put(Integer.toString(i.id),i.label));
		result.put("loaded-ammo",darts); return result;
	}
	public CombatSetup propose(AccountSnapshot account) { return propose(account,"neutral-zero-defence"); }
	public CombatSetup propose(AccountSnapshot account, String targetId)
	{
		if (target(targetId) == null) { throw new IllegalArgumentException("Unknown ranged target"); }
		Map<String,Integer> slots = new LinkedHashMap<>(); Map<String,String> origins = new LinkedHashMap<>();
		for (String slot : PoweredMagic.SLOTS)
		{
			RangedItem best = ownedChoices(slot,account).stream().findFirst().orElse(null);
			Integer equipped = account.equipmentSlots.known() ? account.equipmentSlots.value().get(slot) : null;
			if (best != null) { slots.put(slot,best.id); origins.put(slot,"Observed owned reference choice; " + account.itemEvidence(best.id)); }
			else if (equipped != null && equipped > 0) { slots.put(slot,equipped); origins.put(slot,"Unsupported equipped item; correct its contribution."); }
			else if (equipped != null || account.equipment.known()) { slots.put(slot,0); origins.put(slot,"Empty reference assumption; no reviewed owned alternative observed, other storage unknown."); }
			else { slots.put(slot,-1); origins.put(slot,"Equipment unread; unknown contribution."); }
		}
		if (!account.equipmentSlots.known() && account.equipment.known() && account.equipment.value().stream().anyMatch(id -> !items.containsKey(id)))
		{ slots.put("head",-1); origins.put("head","Unsupported equipment without slot evidence."); }
		CombatSetup base = CombatSetup.ranged(slots,targetId,origins), proposal = base; double strongest = -1;
		for (RangedItem weapon : ownedChoices("weapon",account))
		{
			CombatSetup option = packageFor(account,base,weapon,false);
			if (validate(account,Preferences.EMPTY,option) != null || !target(targetId).scalingSupported) { continue; }
			PoweredMagic.Metrics metrics = RangedModel.calculate(account.level("RANGED"),option,target(targetId),items);
			if (metrics.dps > strongest) { strongest = metrics.dps; proposal = option; }
			CombatSetup voidOption = voidPackage(account,option);
			if (voidOption != null && validate(account,Preferences.EMPTY,voidOption) == null)
			{
				double value = RangedModel.calculate(account.level("RANGED"),voidOption,target(targetId),items).dps;
				if (value > strongest) { strongest = value; proposal = voidOption; }
			}
		}
		// Preserve a valid weapon proposal even when ammunition or another slot prevents numbers.
		if (strongest < 0 && !ownedChoices("weapon",account).isEmpty())
		{ proposal = packageFor(account,proposal,ownedChoices("weapon",account).get(0),false); }
		return proposal;
	}
	private List<RangedItem> ownedChoices(String slot, AccountSnapshot a)
	{
		return items.values().stream().filter(i -> i.slot.equals(slot) && (i.weapon != null || !slot.equals("weapon"))
			&& a.observedItem(i.id) == Truth.TRUE && equipError(a,i) == null)
			.sorted(Comparator.comparingInt((RangedItem i) -> i.strength).reversed()
				.thenComparing(Comparator.comparingInt((RangedItem i) -> i.attack).reversed()).thenComparingInt(i -> i.id)).collect(Collectors.toList());
	}
	private RangedItem bestAmmo(AccountSnapshot account, RangedItem weapon)
	{ return ownedChoices("ammo",account).stream().filter(i -> RangedModel.compatible(weapon,i) && !i.boltEffect)
		.sorted(Comparator.comparingInt((RangedItem i) -> i.strength).reversed().thenComparing((RangedItem i) -> !i.seeking).thenComparingInt(i -> i.id)).findFirst().orElse(null); }
	private CombatSetup packageFor(AccountSnapshot a, CombatSetup base, RangedItem weapon, boolean candidate)
	{
		Map<String,Integer> slots = new LinkedHashMap<>(base.slots); Map<String,String> origins = new LinkedHashMap<>(base.provenance);
		slots.put("weapon",weapon.id); origins.put("weapon",candidate ? "Proposed supplied candidate; charge/loading assumption, not observed usability." : "Strongest supported observed package for this target; " + a.itemEvidence(weapon.id));
		if (weapon.twoHanded) { slots.put("shield",0); origins.put("shield","Removed for two-handed package."); }
		else
		{
			RangedItem shield = ownedChoices("shield",a).stream().findFirst().orElse(null);
			if (shield != null) { slots.put("shield",shield.id); origins.put("shield","Observed one-handed off-hand alternative; " + a.itemEvidence(shield.id)); }
			else if (base.slots.get("shield") == 0 || items.containsKey(base.slots.get("shield"))) { slots.put("shield",0); origins.put("shield","Explicit empty off-hand alternative."); }
		}
		for (String slot : List.of("head","body","legs","hands","cape"))
		{
			List<RangedItem> options = ownedChoices(slot,a);
			boolean crystal = weapon.weapon == RangedItem.Weapon.BOWFA || weapon.weapon == RangedItem.Weapon.CRYSTAL;
			RangedItem selected = options.stream().filter(i -> i.setPart == RangedItem.SetPart.NONE).sorted(
				Comparator.comparingInt((RangedItem i) -> crystal ? i.crystalWeight : 0).reversed()
					.thenComparing(Comparator.comparingInt((RangedItem i) -> i.strength + (quiverBonus(i,weapon) ? 1 : 0)).reversed())
					.thenComparing(Comparator.comparingInt((RangedItem i) -> i.attack + (quiverBonus(i,weapon) ? 10 : 0)).reversed()).thenComparingInt(i -> i.id)).findFirst().orElse(null);
			if (selected != null) { slots.put(slot,selected.id); origins.put(slot,"Observed compatible package choice; " + a.itemEvidence(selected.id)); }
		}
		RangedItem ammo = bestAmmo(a,weapon); RangedItem.Ammo kind = RangedModel.ammoKind(weapon); int darts = 0;
		if (kind != RangedItem.Ammo.NONE)
		{
			if (ammo == null && candidate) { ammo = items.get(catalog.ranged.candidateAmmo.get(weapon.weapon.name())); }
			String provenance = ammo == null ? "Compatible ammunition unobserved; choose a supplied reference."
				: a.observedItem(ammo.id) == Truth.TRUE ? "Observed ammunition; proposed loading/supply assumption, quantity and current load unverified. " + a.itemEvidence(ammo.id)
				: "Candidate-only supplied ammunition assumption: " + ammo.label + "; not observed account ownership.";
			if (kind == RangedItem.Ammo.DART) { darts = ammo == null ? -1 : ammo.id; origins.put("loaded-ammo",provenance); }
			else { slots.put("ammo",ammo == null ? -1 : ammo.id); origins.put("ammo",provenance); }
		}
		if (kind == RangedItem.Ammo.NONE || kind == RangedItem.Ammo.DART)
		{
			// The ammo slot contributes no offensive stats for self-supplied weapons. A reviewed blessing may remain.
			RangedItem slotAmmo = items.get(slots.get("ammo"));
			if (slotAmmo != null && slotAmmo.ammo != RangedItem.Ammo.NONE || slots.get("ammo") < 0)
			{ slots.put("ammo",0); origins.put("ammo","Deliberately excluded: this weapon generates projectiles or uses internal darts."); }
		}
		return base.rangedCopy(slots,origins,base.target,darts);
	}
	private boolean quiverBonus(RangedItem item, RangedItem weapon)
	{ return item.chargedQuiver && (RangedModel.ammoKind(weapon) == RangedItem.Ammo.ARROW || RangedModel.ammoKind(weapon) == RangedItem.Ammo.BOLT); }
	private CombatSetup voidPackage(AccountSnapshot a, CombatSetup base)
	{
		Map<String,Integer> slots = new LinkedHashMap<>(base.slots); Map<String,String> origins = new LinkedHashMap<>(base.provenance);
		for (String slot : List.of("head","body","legs","hands"))
		{
			RangedItem piece = ownedChoices(slot,a).stream().filter(i -> i.setPart != RangedItem.SetPart.NONE)
				.sorted(Comparator.comparing((RangedItem i) -> !(i.setPart == RangedItem.SetPart.ELITE_BODY || i.setPart == RangedItem.SetPart.ELITE_LEGS)).thenComparingInt(i -> i.id)).findFirst().orElse(null);
			if (piece == null) { return null; } slots.put(slot,piece.id); origins.put(slot,"Observed complete Void package; " + a.itemEvidence(piece.id));
		}
		return base.rangedCopy(slots,origins,base.target,base.loadedAmmo);
	}
	public String validate(AccountSnapshot a, Preferences p, CombatSetup setup)
	{ return validate(a,p,setup,0); }
	/** A hypothetical candidate may be supplied; every alternative weapon and its ammunition must be observed. */
	private String validate(AccountSnapshot a, Preferences p, CombatSetup setup, int candidateId)
	{
		if (setup == null || setup.family != CombatSetup.Family.RANGED || setup.rangedPrayer == null || setup.rangedStyle == null
			|| !setup.slots.keySet().equals(new java.util.HashSet<>(PoweredMagic.SLOTS)) || target(setup.target) == null)
		{ return "Unknown ranged reference inputs."; }
		if (!setup.minimalTemplate && !a.equipment.known()) { return "Equipment is unread; explicitly correct the reference instead of treating unknown gear as empty."; }
		Integer level = a.level("RANGED"); if (level == null || level < 1 || level > 99) { return "Known real Ranged 1–99 is required."; }
		for (String slot : PoweredMagic.SLOTS)
		{
			Integer id = setup.slots.get(slot); if (id == null || id < 0) { return "Unknown ranged reference slot: " + slot + "."; }
			if (id == 0) { if (slot.equals("weapon")) { return "No observed usable ranged weapon; manual ownership cannot establish loading/charges."; } continue; }
			RangedItem item = items.get(id);
			if (item == null || !item.slot.equals(slot)) { return "Unreviewed or incompatible ranged item " + id + " in " + slot + "; its bonus is unknown."; }
			if (id != candidateId && a.observedItem(id) != Truth.TRUE && !candidateAmmo(candidateId,id))
			{ return item.label + " is not observed; manual ownership cannot create a usable baseline."; }
			String error = equipError(a,item); if (error != null) { return error; }
			if (id == 28310 && !unlocked(a,p,"leviathan-defeated") && !unlocked(a,p,"desert-treasure-ii"))
			{ return "Venator ring requires a confirmed Leviathan kill or completed Desert Treasure II."; }
		}
		RangedItem weapon = items.get(setup.slots.get("weapon"));
		if (weapon == null || weapon.weapon == null) { return "Selected weapon has no supported ranged family."; }
		if (weapon.twoHanded && setup.slots.get("shield") != 0) { return "Two-handed ranged weapon cannot use an off-hand."; }
		RangedItem.Ammo kind = RangedModel.ammoKind(weapon);
		RangedItem ammo = items.get(kind == RangedItem.Ammo.DART ? setup.loadedAmmo : setup.slots.get("ammo"));
		if (kind != RangedItem.Ammo.NONE && (!RangedModel.compatible(weapon,ammo)
			|| a.observedItem(ammo.id) != Truth.TRUE && ammo.id != candidateId && !candidateAmmo(candidateId,ammo.id)))
		{ return "Compatible " + kind + " ammunition is unobserved or invalid for " + weapon.label + "; loaded darts are a separate reference assumption."; }
		if (setup.rangedPrayer == RangedModel.Prayer.EAGLE_EYE && a.atLeast("PRAYER",44) != Truth.TRUE) { return "44 Prayer is unconfirmed for Eagle Eye."; }
		if (setup.rangedPrayer == RangedModel.Prayer.DEADEYE && (a.atLeast("PRAYER",62) != Truth.TRUE || !unlocked(a,p,"deadeye"))) { return "Deadeye unlock and 62 Prayer are unconfirmed."; }
		if (setup.rangedPrayer == RangedModel.Prayer.RIGOUR && (!unlocked(a,p,"rigour") || a.atLeast("PRAYER",74) != Truth.TRUE || a.atLeast("DEFENCE",70) != Truth.TRUE))
		{ return "Rigour unlock, 74 Prayer and 70 Defence are unconfirmed."; }
		return null;
	}
	private boolean candidateAmmo(int candidateId, int ammoId)
	{
		RangedItem candidate = items.get(candidateId);
		return candidate != null && candidate.weapon != null
			&& Integer.valueOf(ammoId).equals(catalog.ranged.candidateAmmo.get(candidate.weapon.name()));
	}
	private boolean unlocked(AccountSnapshot a, Preferences p, String key)
	{ return new Advisor(catalog).truthForPlanningFact(a,p,key) == Truth.TRUE; }
	private String equipError(AccountSnapshot a, RangedItem item)
	{
		for (Map.Entry<String,Integer> requirement : item.requirements.entrySet())
		{ if (a.atLeast(requirement.getKey(),requirement.getValue()) != Truth.TRUE) { return item.label + " requires confirmed " + requirement.getValue() + " " + requirement.getKey() + "."; } }
		return null;
	}
	public ComparisonResult compare(AccountSnapshot a, Preferences p, CombatSetup setup, KnowledgeCatalog.Upgrade upgrade)
	{ return compare(a,p,setup,upgrade.itemId); }
	public ComparisonResult compare(AccountSnapshot a, Preferences p, CombatSetup setup, int candidateId)
	{
		RangedItem candidate = items.get(candidateId); boolean dpsFirst = candidate != null && candidate.weapon != null;
		String error = validate(a,p,setup);
		if (error != null) { return unavailable(setup,error,dpsFirst); }
		TargetProfile target = target(setup.target);
		if (candidate == null) { return unavailable(setup,"Candidate ranged mechanics are unreviewed.",dpsFirst); }
		if (!target.scalingSupported || target.rangedStandard == null || target.magicAttack == null)
		{ return unavailable(setup,"Target phase/scaling or ranged defensive inputs are unsupported; no raid level is invented.",dpsFirst); }
		error = equipError(a,candidate); if (error != null) { return unavailable(setup,error + " Retain the plan-ahead route.",dpsFirst); }
		if (candidateId == 28310 && !unlocked(a,p,"leviathan-defeated") && !unlocked(a,p,"desert-treasure-ii"))
		{ return unavailable(setup,"Venator ring requires a confirmed Leviathan kill; retain its plan-ahead path.",dpsFirst); }
		List<CombatSetup> alternatives = new ArrayList<>();
		if (candidate.weapon != null)
		{
			addPackages(alternatives,a,packageFor(a,setup,candidate,true),candidate);
		}
		else
		{
			// Retain the focused reference as an option, then reconsider every supported owned weapon.
			alternatives.add(withCandidate(setup,candidate));
			for (RangedItem weapon : ownedChoices("weapon",a))
			{ addPackages(alternatives,a,packageFor(a,setup,weapon,false),candidate); }
		}
		CombatSetup after = null; double strongest = -1;
		for (CombatSetup option : alternatives)
		{
			if (validate(a,p,option,candidateId) != null) { continue; }
			double value = RangedModel.calculate(a.level("RANGED"),option,target,items).dps;
			if (value > strongest) { strongest = value; after = option; }
		}
		if (after == null) { return unavailable(setup,"No supported compatible candidate package with observed alternative weapons and ammunition.",dpsFirst); }
		RangedItem afterWeapon = items.get(after.slots.get("weapon"));
		RangedItem afterAmmo = items.get(RangedModel.ammoKind(afterWeapon) == RangedItem.Ammo.DART ? after.loadedAmmo : after.slots.get("ammo"));
		PoweredMagic.Metrics beforeMetrics = RangedModel.calculate(a.level("RANGED"),setup,target,items);
		PoweredMagic.Metrics afterMetrics = RangedModel.calculate(a.level("RANGED"),after,target,items);
		dpsFirst |= !setup.slots.get("weapon").equals(after.slots.get("weapon")) || beforeMetrics.attackTicks != afterMetrics.attackTicks;
		EnumMap<ComparisonResult.Metric,PoweredMagic.Status> support = support(setup.confirmed ? PoweredMagic.Status.SUPPORTED : PoweredMagic.Status.CONDITIONAL);
		Map<ComparisonResult.Metric,String> gaps = new EnumMap<>(ComparisonResult.Metric.class);
		RangedItem beforeWeapon = items.get(setup.slots.get("weapon"));
		RangedItem beforeAmmo = items.get(RangedModel.ammoKind(beforeWeapon) == RangedItem.Ammo.DART ? setup.loadedAmmo : setup.slots.get("ammo"));
		if (beforeAmmo != null && beforeAmmo.boltEffect || afterAmmo != null && afterAmmo.boltEffect)
		{
			String gap = "Enchanted bolt proc/HP/diary mechanics are not included; ordinary max hit is secondary, full attack DPS is unavailable.";
			support.put(HIT_CHANCE,PoweredMagic.Status.UNSUPPORTED); support.put(THEORETICAL_DPS,PoweredMagic.Status.UNSUPPORTED);
			gaps.put(HIT_CHANCE,gap); gaps.put(THEORETICAL_DPS,gap);
		}
		List<String> assumptions = new ArrayList<>();
		assumptions.add("Complete proposed ranged reference; supplies/charges and internal dart loading are assumptions, never a quantity or current-load observation.");
		assumptions.add("Baseline slots: " + description(setup)); assumptions.add("Candidate slots: " + description(after));
		assumptions.add("Real Ranged " + a.level("RANGED") + "; visible " + beforeMetrics.visibleMagic + "; " + setup.assumptionLabel() + ". Selected prayers/boosts are not live state; potion decay excluded.");
		assumptions.add(target.name + "; Defence " + target.defenceLevel + "; light/standard/heavy bonuses " + target.rangedLight + "/" + target.rangedStandard + "/" + target.rangedHeavy
			+ "; Magic " + target.magicLevel + "; magic attack " + target.magicAttack + "; ranged defence rolls " + beforeMetrics.defenceRoll + " → " + afterMetrics.defenceRoll + ". No reduction or invented HP.");
		assumptions.add("Attack bonus " + beforeMetrics.attackBonus + " → " + afterMetrics.attackBonus + "; ranged strength " + beforeMetrics.damagePercent + " → " + afterMetrics.damagePercent + ". Crystal effects require active pieces and crystal-family bows. Full Void is a package; task assumption " + setup.onTask + " applies only to reviewed eligible targets with an imbued helmet.");
		assumptions.add("Twisted bow uses max(target Magic, magic attack), capped at 250 outside CoX / 350 within reviewed CoX contexts. Neutral Magic 0 is deliberately poor for this specialist. A neutral loss does not erase reviewed encounter value.");
		assumptions.add("Compatible arrows/bolts use their strength; blowpipe darts contribute strength only and are loaded internally. Rosewood permits rune darts after the July 2026 update, not amethyst/dragon. Seeking arrows have +20 bow accuracy and floor successful damage at 3; misses remain 0, max hit is unchanged.");
		assumptions.add("Charged quiver adds +10 accuracy/+1 strength only for arrows/bolts. Bowfa/crystal and blowpipes receive its base cape stats only. Charge quantity, ammunition saving and trip effects are not modelled.");
		assumptions.add("Uninterrupted ordinary PvM attacks; attack interval " + beforeMetrics.attackTicks + " → " + afterMetrics.attackTicks + " ticks. Successful zero becomes 1 before a seeking-arrow floor. Specials, enchanted bolts, poison/venom, bouncing, defence reduction, encounter time and trips remain separate gaps.");
		TreeSet<String> sources = new TreeSet<>(catalog.ranged.sources); sources.addAll(target.sources);
		for (CombatSetup s : List.of(setup,after)) { s.slots.values().stream().filter(id -> id > 0).forEach(id -> sources.addAll(items.get(id).sources)); if (s.loadedAmmo > 0) { sources.addAll(items.get(s.loadedAmmo).sources); } }
		assumptions.add("Model " + RangedModel.VERSION + "; knowledge " + catalog.version + "; sources " + sources.stream().map(id -> id + "@" + catalog.sources.get(id).revision).collect(Collectors.joining(", ")));
		return new ComparisonResult(setup,after,beforeMetrics,afterMetrics,target,support,gaps.isEmpty() ? "Reference estimate, not measured performance." : gaps.get(THEORETICAL_DPS),RangedModel.VERSION,catalog.version,assumptions,dpsFirst,gaps);
	}
	private CombatSetup withCandidate(CombatSetup base, RangedItem candidate)
	{
		Map<String,Integer> slots = new LinkedHashMap<>(base.slots); slots.put(candidate.slot,candidate.id);
		Map<String,String> origins = new LinkedHashMap<>(base.provenance); origins.put(candidate.slot,"Proposed candidate, supplied/equipped assumption.");
		return base.rangedCopy(slots,origins,base.target,base.loadedAmmo);
	}
	private void addPackages(List<CombatSetup> alternatives, AccountSnapshot a, CombatSetup base, RangedItem candidate)
	{
		alternatives.add(withCandidate(base,candidate));
		CombatSetup voidAlternative = voidPackage(a,base);
		// Insert the candidate last so a set alternative cannot silently discard the proposed upgrade.
		if (voidAlternative != null) { alternatives.add(withCandidate(voidAlternative,candidate)); }
	}
	private String description(CombatSetup s)
	{ return PoweredMagic.SLOTS.stream().map(slot -> slot + "=" + (s.slots.get(slot) == 0 ? "deliberately empty" : items.get(s.slots.get(slot)).label)
		+ " (" + s.provenance.getOrDefault(slot,"explicit correction") + ")").collect(Collectors.joining("; "))
		+ (s.loadedAmmo > 0 ? "; loaded darts=" + items.get(s.loadedAmmo).label + " (" + s.provenance.getOrDefault("loaded-ammo","explicit loading assumption") + ")" : ""); }
	private EnumMap<ComparisonResult.Metric,PoweredMagic.Status> support(PoweredMagic.Status status)
	{ EnumMap<ComparisonResult.Metric,PoweredMagic.Status> result = new EnumMap<>(ComparisonResult.Metric.class);
		for (ComparisonResult.Metric metric : ComparisonResult.Metric.values()) { result.put(metric,metric == EXPECTED_KILL_TIME || metric == TRIP_PERFORMANCE ? PoweredMagic.Status.UNSUPPORTED : status); } return result; }
	private ComparisonResult unavailable(CombatSetup s, String reason, boolean weapon)
	{ return new ComparisonResult(s,null,null,null,s == null ? null : target(s.target),support(PoweredMagic.Status.UNSUPPORTED),reason,RangedModel.VERSION,catalog.version,List.of(),weapon,Map.of(THEORETICAL_DPS,reason)); }
}
