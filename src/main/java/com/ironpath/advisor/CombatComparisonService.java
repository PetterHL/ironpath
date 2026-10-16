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
import java.util.stream.Collectors;
import static com.ironpath.advisor.ComparisonResult.Metric.*;

/** Pure proposal, validation and comparison. Rendering consumes its immutable result. */
public final class CombatComparisonService
{
	private final KnowledgeCatalog catalog;
	private final Map<Integer, KnowledgeCatalog.CombatItem> items;
	public CombatComparisonService(KnowledgeCatalog catalog)
	{
		this.catalog = catalog;
		items = catalog.comparison.items.stream().collect(Collectors.toUnmodifiableMap(i -> i.id, i -> i));
	}
	public PoweredMagic.Setup propose(AccountSnapshot a)
	{ return propose(a, "neutral-zero-defence"); }
	public PoweredMagic.Setup propose(AccountSnapshot a, String targetId)
	{
		if (target(targetId) == null) { throw new IllegalArgumentException("Unknown reference target"); }
		Map<String, Integer> slots = new LinkedHashMap<>();
		Map<String, String> provenance = new LinkedHashMap<>();
		for (String slot : PoweredMagic.SLOTS)
		{
			Integer equipped = a.equipmentSlots.known() ? a.equipmentSlots.value().get(slot) : null;
			KnowledgeCatalog.CombatItem owned = items.values().stream().filter(i -> i.slot.equals(slot)
				&& a.observedItem(i.id) == Truth.TRUE)
				.sorted(Comparator.comparing((KnowledgeCatalog.CombatItem i) -> equipError(a, Preferences.EMPTY, i.id) != null)
					.thenComparing(Comparator.comparingInt((KnowledgeCatalog.CombatItem i) -> damage(i)).reversed())
					.thenComparing(Comparator.comparingInt((KnowledgeCatalog.CombatItem i) -> i.magicAttack).reversed()).thenComparingInt(i -> i.id))
				.findFirst().orElse(null);
			// Unreviewed equipped gear stays a gap when no observed supported replacement exists.
			if (owned != null)
			{ slots.put(slot, owned.id); provenance.put(slot, "Observed owned reference choice; " + a.itemEvidence(owned.id)); }
			else if (equipped != null && equipped > 0 && !items.containsKey(equipped))
			{ slots.put(slot, equipped); provenance.put(slot, "Unsupported equipped item; " + a.equipmentSlots.evidence()); }
			else if (equipped != null || !a.equipmentSlots.known() && a.equipment.known())
			{
				slots.put(slot, 0); provenance.put(slot, "Empty reference assumption: no supported owned item observed; other storage remains unknown.");
			}
			else { slots.put(slot, -1); provenance.put(slot, "Equipment slot unread; unknown bonus."); }
		}
		// Old immutable set consumers remain usable, but cannot identify an unreviewed item's slot.
		if (!a.equipmentSlots.known() && a.equipment.known() && a.equipment.value().stream().anyMatch(id -> !items.containsKey(id)))
		{ slots.put("head", -1); provenance.put("head", "Unsupported equipped gear without slot evidence."); }
		PoweredMagic.Setup proposal = new PoweredMagic.Setup(slots, PoweredMagic.Prayer.NONE, PoweredMagic.Boost.NONE,
			PoweredMagic.Style.ACCURATE, false, false,
			targetId, provenance);
		// Compare observed charged staff packages, including removal of an off-hand for Shadow.
		double best = -1;
		for (KnowledgeCatalog.CombatItem weapon : items.values().stream().filter(i -> i.weapon != null
			&& a.observedItem(i.id) == Truth.TRUE && equipError(a, Preferences.EMPTY, i.id) == null)
			.sorted(Comparator.comparingInt(i -> i.id)).collect(Collectors.toList()))
		{
			List<Integer> shields = new ArrayList<>(); shields.add(0);
			if (!weapon.twoHanded) { items.values().stream().filter(i -> i.slot.equals("shield") && a.observedItem(i.id) == Truth.TRUE
				&& equipError(a, Preferences.EMPTY, i.id) == null).sorted(Comparator.comparingInt(i -> i.id)).forEach(i -> shields.add(i.id)); }
			// A known but unsupported shield cannot be excluded until the user corrects it;
			// a two-handed replacement explicitly removes it by compatibility.
			List<Integer> shieldOptions = !weapon.twoHanded && (slots.get("shield") < 0 || slots.get("shield") > 0 && !items.containsKey(slots.get("shield")))
				? List.of(slots.get("shield")) : shields;
			// The proc makes flat ring accuracy an insufficient ordering rule.
			List<Integer> rings = items.values().stream().filter(i -> i.slot.equals("ring")
				&& a.observedItem(i.id) == Truth.TRUE && equipError(a, Preferences.EMPTY, i.id) == null)
				.sorted(Comparator.comparingInt(i -> i.id)).map(i -> i.id).collect(Collectors.toList());
			if (slots.get("ring") < 0 || slots.get("ring") > 0 && !items.containsKey(slots.get("ring")))
			{ rings = List.of(slots.get("ring")); }
			else { rings.add(0); }
			for (int shield : shieldOptions) for (int ring : rings)
			{
				Map<String, Integer> candidate = new LinkedHashMap<>(slots); candidate.put("weapon", weapon.id); candidate.put("shield", shield); candidate.put("ring", ring);
				Map<String, String> origins = new LinkedHashMap<>(provenance);
				origins.put("weapon", "Strongest supported observed staff package for this target; " + a.itemEvidence(weapon.id));
				origins.put("shield", weapon.twoHanded ? "Removed for two-handed reference" : shield == 0 ? "Explicit empty off-hand reference alternative" : "Contextually compared observed off-hand; " + a.itemEvidence(shield));
				origins.put("ring", ring == 0 ? "Explicit empty ring alternative" : "Contextually compared observed ring including supported effects; " + a.itemEvidence(ring));
				PoweredMagic.Setup option = new PoweredMagic.Setup(candidate, proposal.prayer, proposal.boost, proposal.style, false, false, proposal.target, origins);
				if (candidate.values().stream().anyMatch(id -> id < 0 || id > 0 && !items.containsKey(id))) { continue; }
				Integer level = a.level("MAGIC"); if (level == null) { continue; }
				double value = PoweredStaffModel.calculate(level, option, target(proposal.target), items).dps;
				if (value > best) { best = value; proposal = option; }
			}
		}
		return proposal;
	}
	private static int damage(KnowledgeCatalog.CombatItem i) { return i.damagePermille == null ? i.damagePercent * 10 : i.damagePermille; }
	public Map<String, String> choices(String slot, AccountSnapshot a)
	{
		Map<String, String> result = new LinkedHashMap<>();
		if (!slot.equals("weapon")) { result.put("0", "Deliberately empty " + slot); }
		items.values().stream().filter(i -> i.slot.equals(slot) && a.observedItem(i.id) == Truth.TRUE)
			.sorted(Comparator.comparingInt(i -> i.id)).forEach(i -> result.put(Integer.toString(i.id), i.label));
		return java.util.Collections.unmodifiableMap(result);
	}
	public Map<String, String> targetChoices()
	{
		Map<String, String> result = new LinkedHashMap<>();
		catalog.comparison.targets.forEach(t -> result.put(t.id, t.name)); return result;
	}
	private TargetProfile target(String id)
	{ return catalog.comparison.targets.stream().filter(t -> t.id.equals(id)).findFirst().orElse(null); }
	public String validate(AccountSnapshot a, Preferences p, CombatSetup s)
	{
		if (s == null || s.family != CombatSetup.Family.POWERED_MAGIC || !s.slots.keySet().equals(new java.util.HashSet<>(PoweredMagic.SLOTS))
			|| s.prayer == null || s.boost == null || s.style == null || target(s.target) == null)
		{ return "Reference slots or assumptions are unknown."; }
		if (!s.minimalTemplate && !a.equipment.known()) { return "Equipment is unread. Observe it or explicitly choose a reference template; unknown bonuses are not zero."; }
		if (!a.equipmentSlots.known() && a.equipment.known())
		{
			for (String slot : PoweredMagic.SLOTS)
			{ if (a.equipment.value().stream().map(items::get).filter(java.util.Objects::nonNull).filter(i -> i.slot.equals(slot)).count() > 1)
				{ return "Conflicting observed pieces in " + slot + "; correct equipment slot evidence."; } }
		}
		if (a.level("MAGIC") == null || a.level("MAGIC") < 75 || a.level("MAGIC") > 99) { return "Known real Magic 75–99 is required."; }
		for (String slot : PoweredMagic.SLOTS)
		{
			Integer id = s.slots.get(slot);
			if (id == null || id < 0) { return "Unknown reference slot: " + slot + ". Observe or explicitly correct this input."; }
			if (id == 0) { if (slot.equals("weapon")) { return "No observed charged powered staff; manual ownership cannot establish usability."; } continue; }
			KnowledgeCatalog.CombatItem item = items.get(id);
			if (item == null || !item.slot.equals(slot)) { return "Unsupported or incompatible equipped/reference item " + id + " in " + slot + ". Correct this slot; its bonus is not zero."; }
			if (a.observedItem(id) != Truth.TRUE) { return "Selected " + item.label + " is not observed; manual ownership cannot create a usable baseline."; }
			String equip = equipError(a, p, id); if (equip != null) { return equip; }
		}
		KnowledgeCatalog.CombatItem weapon = items.get(s.slots.get("weapon"));
		if (weapon.twoHanded && s.slots.get("shield") != 0) { return "Two-handed weapon cannot coexist with an off-hand. Explicitly remove the shield."; }
		if (s.prayer == PoweredMagic.Prayer.MYSTIC_MIGHT && a.atLeast("PRAYER", 45) != Truth.TRUE) { return "45 Prayer is not confirmed for Mystic Might."; }
		if (s.prayer == PoweredMagic.Prayer.AUGURY)
		{
			Preferences.Confirmation fallback = p.confirmations.get("augury");
			boolean unlocked = a.augury.known() ? a.augury.value() : fallback != null && fallback.value;
			if (!unlocked || a.atLeast("PRAYER", 77) != Truth.TRUE || a.atLeast("DEFENCE", 70) != Truth.TRUE)
			{ return "Augury unlock, 77 Prayer and 70 Defence are not confirmed."; }
		}
		return null;
	}
	private String equipError(AccountSnapshot a, Preferences p, int id)
	{
		KnowledgeCatalog.Gear gear = catalog.gear.values().stream().flatMap(List::stream).filter(g -> g.id == id).findFirst().orElse(null);
		if (gear == null || a.atLeast(gear.skill, gear.level) != Truth.TRUE) { return "Equip level is not confirmed for item " + id + "."; }
		KnowledgeCatalog.CombatItem item = items.get(id);
		if (item != null && item.requirements != null)
		{
			for (Map.Entry<String, Integer> requirement : item.requirements.entrySet())
			{ if (a.atLeast(requirement.getKey(), requirement.getValue()) != Truth.TRUE) { return gear.label + " requires " + requirement.getValue() + " " + requirement.getKey() + "."; } }
		}
		if (id == 28313 && AccountFacts.planning(a,p,"duke-defeated") != Truth.TRUE
			&& AccountFacts.planning(a,p,"desert-treasure-ii") != Truth.TRUE)
		{ return "Magus requires a Duke kill; confirm that fact or completed Desert Treasure II."; }
		return null;
	}
	public ComparisonResult compare(AccountSnapshot a, Preferences p, CombatSetup setup, KnowledgeCatalog.Upgrade upgrade)
	{ return upgrade.comparison.equals("NONE") ? unavailable(setup, "This specialist role has no supported combat model.") : compare(a, p, setup, upgrade.itemId); }
	public ComparisonResult compare(AccountSnapshot a, Preferences p, CombatSetup setup, int candidateId)
	{
		KnowledgeCatalog.CombatItem candidate = items.get(candidateId);
		boolean weaponComparison = candidate != null && candidate.weapon != null;
		String error = validate(a, p, setup);
		if (error != null) { return unavailable(setup, error, weaponComparison); }
		TargetProfile target = target(setup.target);
		if (!target.scalingSupported) { return unavailable(setup, "ToA phase, raid level, path and party scaling are not supported. Select a reviewed non-ToA target; no default invocation is invented.", weaponComparison); }
		if (candidate == null) { return unavailable(setup, "Candidate mechanics are not supported."); }
		if (candidate.weapon != null && candidate.weapon.equals("SWAMP") && !items.get(setup.slots.get("weapon")).weapon.equals("SEAS"))
		{ return unavailable(setup, "Swamp requires a charged seas baseline; keep the stronger owned powered staff.", true); }
		error = equipError(a, p, candidateId); if (error != null) { return unavailable(setup, error + " Retain the plan-ahead route.", weaponComparison); }
		Map<String, Integer> afterSlots = new LinkedHashMap<>(setup.slots); afterSlots.put(candidate.slot, candidateId);
		Map<String, String> afterProvenance = new LinkedHashMap<>(setup.provenance);
		afterProvenance.put(candidate.slot, "Proposed candidate; charged and supplied assumption.");
		if (candidate.twoHanded) { afterSlots.put("shield", 0); afterProvenance.put("shield", "Removed: two-handed weapon replacement."); }
		CombatSetup afterSetup = new CombatSetup(afterSlots, setup.prayer, setup.boost, setup.style, false, setup.minimalTemplate, setup.target, afterProvenance);
		PoweredMagic.Metrics before = PoweredStaffModel.calculate(a.level("MAGIC"), setup, target, items);
		PoweredMagic.Metrics after = PoweredStaffModel.calculate(a.level("MAGIC"), afterSetup, target, items);
		EnumMap<ComparisonResult.Metric, PoweredMagic.Status> support = support(setup.confirmed ? PoweredMagic.Status.SUPPORTED : PoweredMagic.Status.CONDITIONAL);
		EnumMap<ComparisonResult.Metric, String> gaps = new EnumMap<>(ComparisonResult.Metric.class);
		String reason = "Reference estimate only; not observed combat performance.";
		java.util.Set<String> beforeGaps = accuracyGaps(setup), afterGaps = accuracyGaps(afterSetup);
		java.util.Set<String> allGaps = new java.util.TreeSet<>(beforeGaps); allGaps.addAll(afterGaps);
		if (!allGaps.isEmpty())
		{
			support.put(HIT_CHANCE, PoweredMagic.Status.UNSUPPORTED); support.put(THEORETICAL_DPS, PoweredMagic.Status.UNSUPPORTED);
			String detail = allGaps.stream().map(gap -> (beforeGaps.contains(gap) && afterGaps.contains(gap)
				? "Before and after: " : beforeGaps.contains(gap) ? "Before: " : "After: ") + gap)
				.collect(Collectors.joining(" "));
			gaps.put(HIT_CHANCE, detail); gaps.put(THEORETICAL_DPS, detail);
			reason += " " + detail + " Max hit remains supported.";
		}
		List<String> assumptions = new ArrayList<>();
		assumptions.add(setup.confirmed ? "Confirmed reference estimate; candidate remains proposed." : "Proposed complete reference; correct any consequential assumption.");
		assumptions.add("Baseline slots: " + description(setup)); assumptions.add("Candidate slots: " + description(afterSetup));
		if (setup.slots.get("ammo") == 20235)
		{ assumptions.add("Ancient blessing remains equipped: zero magic attack/damage. Its +1 Prayer and God Wars protection do not change this fixed-prayer uninterrupted DPS scenario; prayer duration and trip utility are not modelled."); }
		assumptions.add("Real Magic " + a.level("MAGIC") + " (" + a.levels.evidence() + "); visible " + before.visibleMagic
			+ "; " + setup.prayer + "; " + setup.boost + "; " + setup.style + ". Prayer/boost are selected assumptions, not observed active.");
		assumptions.add(target.name + (target.analytical ? " · Authored analytical reference; Defence " + target.defenceLevel : " · NPC " + target.npcId)
			+ "; Magic " + target.magicLevel + "; magic-defence bonus " + target.magicDefence
			+ "; defence roll " + target.defenceRoll() + ". No reduction, task, salve or elemental weakness; powered attacks receive no wind weakness.");
		if (target.analytical) { assumptions.add("Ordinary attack rolls and damage distribution; accuracy is not guaranteed and damage is not forced to maximum. No HP, size, melee defensive stats, raid scaling or creature-family effects are assigned. This calculation uses the reviewed powered-magic inputs; ranged defensive inputs have their own independently validated family model. Specialist usefulness requires an explicit encounter context."); }
		assumptions.add("Attack bonus " + before.attackBonus + " → " + after.attackBonus + "; damage bonus " + before.damagePercent + "% → " + after.damagePercent
			+ "%. Shadow multiplies equipment, including its weapon attack; caps gear damage before adding prayer. Off-hands are removed for Shadow.");
		assumptions.add("Uninterrupted attacks: tridents/Sang 4 ticks, Eye 3, Shadow 5. Successful zero becomes 1; misses remain 0. Supplies/charges assumed; remaining charges not measured.");
		assumptions.add("Attack interval " + before.attackTicks + " → " + after.attackTicks + " ticks ("
			+ (before.attackTicks * 0.6) + " → " + (after.attackTicks * 0.6) + " seconds); DPS uses expected damage and hit chance.");
		assumptions.add("Sang's current successful-hit proc: 20% adds 8 damage before the final zero floor; the reported max includes that proc. Healing is not converted into trips or extra kills.");
		assumptions.add("Sang healing, Eye specials, Confliction history, ancient/elemental spells, ToA scaling, encounter time and trips remain separate model gaps.");
		if (List.of(setup,afterSetup).stream().anyMatch(s -> s.slots.values().stream().filter(id -> id > 0).anyMatch(id -> items.get(id).brimstoneEffect)))
		{ assumptions.add("Brimstone: each ordinary magic attack has a 25% chance to use floor(0.9 × target defence roll). Hit chance is the weighted mixture of ordinary and proc accuracy, independently for before and after. It does not drain Magic or permanently reduce the target. Confliction one-handed history remains unsupported."); }
		assumptions.add("Model " + catalog.comparison.modelVersion + "; knowledge " + catalog.version + "; sources " + sourceDescription(setup, afterSetup, target));
		return new ComparisonResult(setup, afterSetup, before, after, target, support, reason, catalog.comparison.modelVersion, catalog.version, assumptions,
			candidate.weapon != null || before.attackTicks != after.attackTicks, gaps);
	}
	private java.util.Set<String> accuracyGaps(CombatSetup setup)
	{
		java.util.Set<String> result = new java.util.TreeSet<>();
		for (int id : setup.slots.values())
		{
			if (id == 0) { continue; }
			KnowledgeCatalog.CombatItem item = items.get(id);
			if (item.unsupportedAccuracy) { result.add(item.label + ": accuracy proc is not modelled."); }
			if (item.historyEffect && !items.get(setup.slots.get("weapon")).twoHanded)
			{ result.add(item.label + ": one-handed miss-history reroll is not modelled."); }
		}
		return result;
	}
	private String description(CombatSetup setup)
	{
		return PoweredMagic.SLOTS.stream().map(slot -> slot + "=" + (setup.slots.get(slot) == 0 ? "deliberately empty"
			: items.get(setup.slots.get(slot)).label + " [" + setup.slots.get(slot) + "]") + " ("
			+ setup.provenance.getOrDefault(slot, "explicit reference correction") + ")").collect(Collectors.joining("; "));
	}
	private String sourceDescription(CombatSetup before, CombatSetup after, TargetProfile target)
	{
		java.util.Set<String> sources = new java.util.TreeSet<>(target.sources);
		sources.addAll(catalog.comparison.sources);
		for (CombatSetup setup : List.of(before, after)) { setup.slots.values().stream().filter(id -> id > 0).forEach(id -> sources.addAll(items.get(id).sources)); }
		return sources.stream().map(id -> id + "@" + catalog.sources.get(id).revision).collect(Collectors.joining(", "));
	}
	private static EnumMap<ComparisonResult.Metric, PoweredMagic.Status> support(PoweredMagic.Status status)
	{
		EnumMap<ComparisonResult.Metric, PoweredMagic.Status> result = new EnumMap<>(ComparisonResult.Metric.class);
		for (ComparisonResult.Metric metric : ComparisonResult.Metric.values()) { result.put(metric, metric == EXPECTED_KILL_TIME || metric == TRIP_PERFORMANCE ? PoweredMagic.Status.UNSUPPORTED : status); }
		return result;
	}
	private ComparisonResult unavailable(CombatSetup setup, String reason)
	{ return unavailable(setup, reason, false); }
	private ComparisonResult unavailable(CombatSetup setup, String reason, boolean dpsFirst)
	{ return new ComparisonResult(setup, null, null, null, setup == null ? null : target(setup.target), support(PoweredMagic.Status.UNSUPPORTED), reason, catalog.comparison.modelVersion, catalog.version, List.of(), dpsFirst, Map.of()); }
}
