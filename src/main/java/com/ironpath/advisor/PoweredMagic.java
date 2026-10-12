package com.ironpath.advisor;

import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.model.Truth;
import com.ironpath.preferences.Preferences;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Independently authored from the individually reviewed mechanics in the catalogue.
 * Only ordinary trident attacks against ordinary Dharok; never an encounter simulator. */
public final class PoweredMagic
{
	public enum Prayer
	{
		NONE(100, 0), MYSTIC_MIGHT(115, 2), AUGURY(125, 4);
		final int accuracyPercent, damagePercent;
		Prayer(int accuracyPercent, int damagePercent) { this.accuracyPercent = accuracyPercent; this.damagePercent = damagePercent; }
	}
	public enum Boost { NONE, MAGIC_POTION }
	public enum Style { ACCURATE, LONGRANGE }
	public enum Status { SUPPORTED, CONDITIONAL, UNSUPPORTED }
	public static final List<String> SLOTS = List.of("head", "cape", "neck", "ammo", "weapon", "body", "shield", "legs", "hands", "feet", "ring");
	private final KnowledgeCatalog catalog;
	public PoweredMagic(KnowledgeCatalog catalog) { this.catalog = catalog; }

	public static final class Setup
	{
		public final Map<String, Integer> slots;
		public final Prayer prayer;
		public final Boost boost;
		public final Style style;
		public final boolean confirmed, minimalTemplate;
		public Setup(Map<String, Integer> slots, Prayer prayer, Boost boost, Style style, boolean confirmed, boolean minimalTemplate)
		{ this.slots = Map.copyOf(slots); this.prayer = prayer; this.boost = boost; this.style = style; this.confirmed = confirmed; this.minimalTemplate = minimalTemplate; }
		public Setup edit(String key, String value)
		{
			Map<String, Integer> next = new LinkedHashMap<>(slots);
			if (SLOTS.contains(key)) { next.put(key, Integer.valueOf(value)); }
			return new Setup(next, key.equals("prayer") ? Prayer.valueOf(value) : prayer,
				key.equals("boost") ? Boost.valueOf(value) : boost, key.equals("style") ? Style.valueOf(value) : style,
				key.equals("confirm"), key.equals("template") || minimalTemplate);
		}
		Setup unconfirmed() { return new Setup(slots, prayer, boost, style, false, minimalTemplate); }
	}

	public Setup propose(AccountSnapshot account)
	{
		Map<String, Integer> slots = new LinkedHashMap<>(); SLOTS.forEach(slot -> slots.put(slot, 0));
		for (String slot : List.of("weapon", "neck", "feet"))
		{
			List<KnowledgeCatalog.CombatItem> owned = catalog.comparison.items.stream()
				.filter(i -> i.slot.equals(slot) && account.observedItem(i.id) == Truth.TRUE)
				.sorted(Comparator.comparingInt((KnowledgeCatalog.CombatItem i) -> i.damagePercent).reversed()
					.thenComparing(Comparator.comparingInt((KnowledgeCatalog.CombatItem i) -> i.magicAttack).reversed()).thenComparingInt(i -> i.id))
				.collect(Collectors.toList());
			KnowledgeCatalog.CombatItem equipped = owned.stream().filter(i -> account.equipment.known() && account.equipment.value().contains(i.id)).findFirst().orElse(null);
			if (equipped != null || !owned.isEmpty()) { slots.put(slot, equipped != null ? equipped.id : owned.get(0).id); }
		}
		return new Setup(slots, Prayer.NONE, Boost.NONE, Style.ACCURATE, false, false);
	}

	public Map<String, String> choices(String slot, AccountSnapshot a)
	{
		Map<String, String> choices = new LinkedHashMap<>();
		if (!slot.equals("weapon")) { choices.put("0", "Deliberately empty " + slot); }
		catalog.comparison.items.stream().filter(i -> i.slot.equals(slot) && a.observedItem(i.id) == Truth.TRUE)
			.sorted(Comparator.comparingInt(i -> i.id)).forEach(i -> choices.put(Integer.toString(i.id), i.label));
		return choices;
	}

	public Result compare(AccountSnapshot account, Preferences preferences, Setup setup, String comparison)
	{
		String error = validate(account, preferences, setup);
		if (!comparison.equals("OCCULT") && !comparison.equals("SWAMP")) { error = "This specialist/boot case has no supported combat model."; }
		if (error != null) { return Result.unavailable(error); }
		Map<String, Integer> candidate = new LinkedHashMap<>(setup.slots);
		if (comparison.equals("OCCULT")) { candidate.put("neck", 12002); }
		else
		{
			if (!"SEAS".equals(item(setup.slots.get("weapon")).weapon))
			{ return Result.unavailable("The swamp comparison requires an observed charged seas baseline; advanced/swamp weapons are not replaced by a weaker template."); }
			candidate.put("weapon", 12899);
		}
		int level = account.level("MAGIC");
		if (comparison.equals("SWAMP") && level < 78) { return Result.unavailable("78 real Magic is not confirmed for wielding swamp. Keep the qualitative plan; boosts do not establish equip requirements."); }
		Metrics before = calculate(level, setup.slots, setup.prayer, setup.boost, setup.style);
		Metrics after = calculate(level, candidate, setup.prayer, setup.boost, setup.style);
		List<String> contract = new ArrayList<>();
		contract.add(setup.confirmed ? "Confirmed reference estimate; candidate is proposed, not observed combat performance."
			: "Proposed reference scenario using observed levels; not your actual loadout. Confirm or correct it below.");
		contract.add("Baseline slots: " + slotDescription(setup.slots, account, false));
		contract.add("Candidate slots: " + slotDescription(candidate, account, true));
		contract.add("Real Magic " + level + "; assumed visible Magic " + before.visibleMagic + "; effective accuracy level " + before.effectiveMagic
			+ "; style " + setup.style + "; offensive prayer " + setup.prayer + "; temporary boost " + setup.boost + ". Prayer/boost are selected assumptions, never observed active.");
		contract.add("Level provenance: " + account.levels.evidence() + ". Prayer eligibility: "
			+ (setup.prayer == Prayer.AUGURY ? account.augury.known() ? account.augury.evidence()
				: preferences.confirmations.get("augury").evidence() : setup.prayer == Prayer.NONE ? "no offensive prayer assumed" : "observed Prayer level meets 45") + ".");
		contract.add("Baseline attack bonus " + before.attackBonus + ", damage bonus " + before.damagePercent + "%; candidate attack bonus "
			+ after.attackBonus + ", damage bonus " + after.damagePercent + "%. Floor visible-Magic base hit, total damage bonus and effective accuracy in the reviewed order.");
		contract.add(catalog.comparison.targetName + " · NPC " + catalog.comparison.targetId + "; active attacks; Magic 1 / magic defence -11; defence roll 530. No scaling or defence reduction.");
		contract.add("4 ticks / 2.4 seconds, uninterrupted. Successful zero damage becomes 1; misses remain 0. Non-elemental attacks receive no wind weakness.");
		contract.add("No venom/poison, thralls, specials, set effects, advanced-staff multipliers, movement or resource model. Wind spells remain a relevant Barrows alternative; these figures do not establish the best strategy.");
		contract.add("Model " + catalog.comparison.modelVersion + "; knowledge " + catalog.version + "; source revisions: "
			+ catalog.comparison.sources.stream().map(id -> id + "@" + catalog.sources.get(id).revision).collect(Collectors.joining(", ")) + ".");
		return new Result(setup.confirmed ? Status.SUPPORTED : Status.CONDITIONAL, "Reference estimate only; candidate assumed charged and supplied.", before, after, contract);
	}

	public String validate(AccountSnapshot a, Preferences p, Setup s)
	{
		if (s == null || !s.slots.keySet().equals(new java.util.HashSet<>(SLOTS)) || s.prayer == null || s.boost == null || s.style == null)
		{ return "Reference slots or assumptions are unknown."; }
		if (!s.minimalTemplate)
		{
			if (!a.equipment.known()) { return "Equipment is unread. Observe it or explicitly choose the minimal reference template; unknown bonuses are not zero."; }
			for (int id : a.equipment.value())
			{
				KnowledgeCatalog.CombatItem stat = item(id);
				if (stat == null) { return "Equipped item " + id + " has unsupported slot/bonus mechanics. Choose the minimal reference template to explicitly exclude it, or change equipment."; }
			}
			for (String slot : List.of("weapon", "neck", "feet"))
			{ if (a.equipment.value().stream().map(this::item).filter(Objects::nonNull).filter(i -> i.slot.equals(slot)).count() > 1)
				{ return "Conflicting observed pieces in " + slot + "; correct the reference or use the explicit minimal template."; } }
		}
		Integer magic = a.level("MAGIC");
		if (magic == null || magic < 75 || magic > 99) { return "Known real Magic 75–99 is required; unsupported levels never become zero."; }
		for (String slot : SLOTS)
		{
			Integer id = s.slots.get(slot);
			if (id == null || id < 0) { return "Unknown reference slot: " + slot; }
			if (id == 0) { if (slot.equals("weapon")) { return "No observed charged ordinary trident. Empty/uncharged or manually owned forms do not establish a numerical baseline."; } continue; }
			KnowledgeCatalog.CombatItem stat = item(id);
			if (stat == null || !stat.slot.equals(slot)) { return "Unsupported or incompatible item in " + slot + "."; }
			if (a.observedItem(id) != Truth.TRUE) { return "Selected " + stat.label + " is not observed; manual ownership cannot create a usable numerical baseline."; }
			if (slot.equals("weapon") && stat.weapon.equals("SWAMP") && magic < 78) { return "78 real Magic is required for the swamp baseline."; }
			// All supported non-weapon equip requirements are also checked, without assuming boosts.
			KnowledgeCatalog.Gear gear = catalog.gear.values().stream().flatMap(List::stream).filter(g -> g.id == id).findFirst().orElseThrow();
			if (a.atLeast(gear.skill, gear.level) != Truth.TRUE) { return stat.label + " equip level is not confirmed."; }
			if ((id == 13235 || id == 13237) && a.atLeast("DEFENCE", 75) != Truth.TRUE
				|| id == 13239 && a.atLeast("STRENGTH", 75) != Truth.TRUE)
			{ return stat.label + " additional equip level is not confirmed."; }
		}
		if (s.prayer == Prayer.MYSTIC_MIGHT && a.atLeast("PRAYER", 45) != Truth.TRUE) { return "45 Prayer is not confirmed for Mystic Might."; }
		if (s.prayer == Prayer.AUGURY)
		{
			Observation<Boolean> unlock = a.augury;
			Preferences.Confirmation fallback = p.confirmations.get("augury");
			boolean unlocked = unlock.known() ? unlock.value() : fallback != null && fallback.value;
			if (!unlocked || a.atLeast("PRAYER", 77) != Truth.TRUE || a.atLeast("DEFENCE", 70) != Truth.TRUE)
			{ return "Augury unlock, 77 Prayer and 70 Defence are not confirmed; no active prayer is inferred."; }
		}
		return null;
	}

	private KnowledgeCatalog.CombatItem item(int id)
	{ return catalog.comparison.items.stream().filter(i -> i.id == id).findFirst().orElse(null); }
	private String slotDescription(Map<String, Integer> slots, AccountSnapshot a, boolean candidate)
	{
		return SLOTS.stream().map(slot -> {
			int id = slots.get(slot); if (id == 0) { return slot + "=deliberately empty (0 bonuses)"; }
			KnowledgeCatalog.CombatItem stat = item(id);
			return slot + "=" + stat.label + " [" + id + "; attack " + stat.magicAttack + ", damage " + stat.damagePercent + "%; "
				+ (candidate ? "proposed reference item/form" : a.itemEvidence(id)) + "]";
		}).collect(Collectors.joining("; "));
	}

	private Metrics calculate(int magic, Map<String, Integer> slots, Prayer prayer, Boost boost, Style style)
	{
		int visible = magic + (boost == Boost.MAGIC_POTION ? 4 : 0);
		int effective = visible * prayer.accuracyPercent / 100 + (style == Style.ACCURATE ? 3 : 1) + 8;
		int attack = 0, damage = prayer.damagePercent;
		for (int id : slots.values()) { if (id != 0) { attack += item(id).magicAttack; damage += item(id).damagePercent; } }
		String weapon = item(slots.get("weapon")).weapon;
		int base = visible / 3 - (weapon.equals("SEAS") ? 5 : 2);
		int max = base * (100 + damage) / 100;
		long roll = (long) effective * (64 + attack);
		long defence = (long) (catalog.comparison.targetMagic + 9) * (catalog.comparison.targetMagicDefence + 64);
		double chance = hitChance(roll, defence);
		return new Metrics(max, chance, chance * successfulMean(max) / (catalog.comparison.attackTicks * 0.6), visible, effective, attack, damage, roll, defence);
	}

	/** Uniform inclusive rolls. These helpers also exercise the branch below Dharok's normal player roll. */
	public static double hitChance(long attack, long defence)
	{
		if (attack < 0 || defence < 0) { throw new IllegalArgumentException("Negative roll is unsupported"); }
		return attack > defence ? 1.0 - (defence + 2.0) / (2.0 * (attack + 1.0)) : attack / (2.0 * (defence + 1.0));
	}
	public static double successfulMean(int maxHit)
	{
		if (maxHit < 1) { throw new IllegalArgumentException("Unsupported max hit"); }
		return maxHit / 2.0 + 1.0 / (maxHit + 1.0);
	}
	public static final class Metrics
	{
		public final int maxHit, visibleMagic, effectiveMagic, attackBonus, damagePercent;
		public final long attackRoll, defenceRoll;
		public final double hitChance, dps;
		Metrics(int maxHit, double hitChance, double dps, int visible, int effective, int attack, int damage, long roll, long defence)
		{ this.maxHit = maxHit; this.hitChance = hitChance; this.dps = dps; visibleMagic = visible; effectiveMagic = effective; attackBonus = attack; damagePercent = damage; attackRoll = roll; defenceRoll = defence; }
	}
	public static final class Result
	{
		public final Status status;
		public final String reason;
		public final Metrics before, after;
		public final List<String> assumptions;
		Result(Status status, String reason, Metrics before, Metrics after, List<String> assumptions)
		{ this.status = status; this.reason = reason; this.before = before; this.after = after; this.assumptions = List.copyOf(assumptions); }
		static Result unavailable(String reason) { return new Result(Status.UNSUPPORTED, reason, null, null, List.of()); }
		public String summary()
		{
			if (status == Status.UNSUPPORTED) { return "Comparison unavailable: " + reason; }
			return String.format(java.util.Locale.ROOT, "%s reference · Max hit %d → %d; hit chance %.1f%% → %.1f%% (%+.1f percentage points); theoretical DPS %.2f → %.2f. Uninterrupted ordinary Dharok attacks only.",
				status == Status.CONDITIONAL ? "Proposed" : "Confirmed", before.maxHit, after.maxHit, before.hitChance * 100, after.hitChance * 100,
				(after.hitChance - before.hitChance) * 100, before.dps, after.dps);
		}
	}
}
