package com.ironpath.advisor;

import com.ironpath.advisor.KnowledgeCatalog.Decision;
import com.ironpath.advisor.KnowledgeCatalog.Condition;
import com.ironpath.advisor.KnowledgeCatalog.Gear;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.model.Truth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Pure deterministic evaluation. No RuneLite, Swing, clock, IO or inferred ownership. */
public final class Advisor
{
	private final KnowledgeCatalog catalog;
	public Advisor(KnowledgeCatalog catalog) { this.catalog = catalog; }

	public List<Recommendation> evaluate(AccountSnapshot account)
	{
		List<Recommendation> candidates = new ArrayList<>();
		for (Decision decision : catalog.decisions)
		{
			if (!"reviewed".equals(decision.status)) { continue; }
			if (truth(decision.when, account) != Truth.TRUE || completed(decision, account) != Truth.FALSE) { continue; }
			if (decision.requires.stream().anyMatch(id -> catalog.decisions.stream()
				.filter(d -> d.id.equals(id)).anyMatch(d -> !"reviewed".equals(d.status) || completed(d, account) != Truth.TRUE)))
			{ continue; }
			List<String> evidence = match(decision, account);
			if (evidence != null) { candidates.add(new Recommendation(decision, evidence)); }
		}
		candidates.sort(Comparator.comparingInt((Recommendation r) -> r.decision.priority).thenComparing(r -> r.decision.id));
		Set<String> families = new HashSet<>();
		List<Recommendation> result = new ArrayList<>();
		for (Recommendation recommendation : candidates)
		{
			if (families.add(recommendation.decision.family)) { result.add(recommendation); }
			if (result.size() == 3) { break; }
		}
		return List.copyOf(result);
	}

	private List<String> match(Decision decision, AccountSnapshot account)
	{
		switch (decision.kind)
		{
			case PLAN_TEAM: return planTeam(account);
			case UNLOCK_RIGOUR: return unlock(account, account.rigour, decision.itemId, "Rigour", "dexterous prayer scroll", 74);
			case UNLOCK_AUGURY: return unlock(account, account.augury, decision.itemId, "Augury", "arcane prayer scroll", 77);
			case TRAIN_RIGOUR: return train(account, account.rigour, "Rigour", 74);
			case TRAIN_AUGURY: return train(account, account.augury, "Augury", 77);
			case ASSEMBLE_SWAMP: return assembleSwamp(account);
			case REVIEW_LANCE: return reviewLance(account);
			case COORDINATE_REDUCTION: return coordinateReduction(account);
			case REVIEW_OCCULT: return reviewOccult(account);
			default: return null;
		}
	}

	private Truth completed(Decision decision, AccountSnapshot account)
	{
		return decision.repeatable ? Truth.FALSE : truth(decision.completeWhen, account);
	}

	Truth truth(Condition condition, AccountSnapshot account)
	{
		switch (condition.test)
		{
			case ALL: return Truth.all(condition.conditions.stream().map(c -> truth(c, account)).toArray(Truth[]::new));
			case ANY: return Truth.any(condition.conditions.stream().map(c -> truth(c, account)).toArray(Truth[]::new));
			case LEVEL: return account.atLeast(condition.key, condition.minimum);
			case ITEMS: return Truth.any(catalog.itemSets.get(condition.key).stream().map(account::observedItem).toArray(Truth[]::new));
			case EQUIPPED: return !account.equipment.known() ? Truth.UNKNOWN
				: catalog.itemSets.get(condition.key).stream().anyMatch(account.equipment.value()::contains) ? Truth.TRUE : Truth.FALSE;
			case RIGOUR: return expected(Truth.of(account.rigour), condition.value);
			case AUGURY: return expected(Truth.of(account.augury), condition.value);
			default: return Truth.UNKNOWN;
		}
	}

	private static Truth expected(Truth truth, boolean value)
	{
		return value || truth == Truth.UNKNOWN ? truth : truth == Truth.TRUE ? Truth.FALSE : Truth.TRUE;
	}

	private Gear observed(AccountSnapshot account, String set)
	{
		return catalog.gear.values().stream().flatMap(List::stream)
			.filter(g -> catalog.itemSets.get(set).contains(g.id) && account.observedItem(g.id) == Truth.TRUE)
			.sorted(Comparator.comparingInt(g -> catalog.itemSets.get(set).indexOf(g.id))).findFirst().orElseThrow();
	}

	private static String evidence(AccountSnapshot account, Gear item)
	{
		return item.label + " observed in " + account.itemEvidence(item.id);
	}

	private List<String> assembleSwamp(AccountSnapshot account)
	{
		return List.of(evidence(account, observed(account, "magicFang")), evidence(account, observed(account, "seasAssembly")),
			"Crafting " + account.level("CRAFTING") + " >= 59; Magic " + account.level("MAGIC") + " >= 78: " + account.levels.evidence(),
			"No recognised swamp trident or competing magic option observed in supported storage. Other storage remains unknown; check before assembly.");
	}

	private List<String> reviewLance(AccountSnapshot account)
	{
		return List.of(evidence(account, observed(account, "lance")), evidence(account, observed(account, "ordinaryMelee")),
			"Attack " + account.level("ATTACK") + " >= 78: " + account.levels.evidence(),
			"Lance is not currently equipped: " + account.equipment.evidence()
				+ ". This does not establish your usual raid setup. Required Barbarian Training sections remain unobserved.");
	}

	private List<String> coordinateReduction(AccountSnapshot account)
	{
		List<String> evidence = planTeam(account);
		if (evidence == null) { return null; }
		evidence = new ArrayList<>(evidence);
		for (int id : catalog.itemSets.get("reduction"))
		{
			Gear item = catalog.gear.get("reduction").stream().filter(g -> g.id == id).findFirst().orElseThrow();
			if (usable(account, item)) { evidence.add(evidence(account, item)); }
		}
		evidence.add("Team coverage is unknown; this is repeatable pre-raid coordination, not evidence of missing teammate gear.");
		return evidence;
	}

	private List<String> reviewOccult(AccountSnapshot account)
	{
		Gear staff = catalog.gear.get("magic").stream().filter(g -> usable(account, g)).findFirst().orElse(null);
		if (staff == null) { return null; }
		Gear neck = catalog.gear.get("neck").stream()
			.filter(g -> catalog.itemSets.get("fury").contains(g.id) && account.equipment.value().contains(g.id)).findFirst().orElseThrow();
		return List.of(evidence(account, observed(account, "occult")), evidence(account, staff),
			neck.label + " currently equipped: " + account.equipment.evidence()
				+ ". Your saved raid switches are not observed; review whether you already bring the occult.");
	}

	private List<String> unlock(AccountSnapshot account, Observation<Boolean> unlock, int scroll, String prayer, String label, int level)
	{
		if (Truth.of(unlock) != Truth.FALSE || account.observedItem(scroll) != Truth.TRUE) { return null; }
		return List.of(label + " observed in " + account.itemEvidence(scroll),
			prayer + " not unlocked: " + unlock.evidence(),
			"Reading has no skill requirement. Using " + prayer + " needs " + level + " Prayer and 70 Defence; observed levels: "
				+ value(account.level("PRAYER")) + " / " + value(account.level("DEFENCE")) + ".");
	}

	private List<String> train(AccountSnapshot account, Observation<Boolean> unlock, String prayer, int target)
	{
		Integer level = account.level("PRAYER");
		if (Truth.of(unlock) != Truth.TRUE || level == null || level >= target
			|| account.atLeast("DEFENCE", 70) != Truth.TRUE) { return null; }
		return List.of(prayer + " permanently unlocked: " + unlock.evidence(),
			"Prayer " + level + " < " + target + "; Defence " + account.level("DEFENCE") + " >= 70: " + account.levels.evidence());
	}

	private List<String> planTeam(AccountSnapshot account)
	{
		for (String skill : List.of("ATTACK", "STRENGTH", "RANGED", "MAGIC", "HITPOINTS"))
		{
			if (account.atLeast(skill, 80) != Truth.TRUE) { return null; }
		}
		if (Truth.all(account.atLeast("DEFENCE", 70), account.atLeast("PRAYER", 70)) != Truth.TRUE) { return null; }
		List<String> evidence = new ArrayList<>();
		for (String style : List.of("melee", "ranged", "magic"))
		{
			Gear item = catalog.gear.get(style).stream().filter(g -> usable(account, g)).findFirst().orElse(null);
			if (item == null) { return null; }
			evidence.add(item.label + " observed in " + account.itemEvidence(item.id));
			if (item.crystal)
			{
				for (String part : List.of("crystalBody", "crystalLegs", "crystalHelm"))
				{
					Gear armour = catalog.gear.get(part).stream().filter(g -> usable(account, g)).findFirst().orElseThrow();
					evidence.add(armour.label + " observed in " + account.itemEvidence(armour.id));
				}
			}
		}
		evidence.add("Authored planning baseline: 80+ Attack/Strength/Ranged/Magic/Hitpoints, 70+ Defence/Prayer: " + account.levels.evidence());
		return evidence;
	}

	private boolean usable(AccountSnapshot account, Gear gear)
	{
		if (account.observedItem(gear.id) != Truth.TRUE || account.atLeast(gear.skill, gear.level) != Truth.TRUE) { return false; }
		if (catalog.gear.get("reduction").contains(gear) && (gear.id == 21003 || gear.id == 27100)
			&& account.atLeast("STRENGTH", 75) != Truth.TRUE) { return false; }
		if (!gear.crystal) { return true; }
		return account.atLeast("AGILITY", 70) == Truth.TRUE && List.of("crystalBody", "crystalLegs", "crystalHelm").stream()
			.allMatch(group -> catalog.gear.get(group).stream().anyMatch(g -> usable(account, g)));
	}

	private static String value(Integer value) { return value == null ? "unknown" : value.toString(); }

	public static final class Recommendation
	{
		public final Decision decision;
		public final List<String> evidence;
		Recommendation(Decision decision, List<String> evidence)
		{
			this.decision = decision;
			this.evidence = List.copyOf(evidence);
		}
	}
}
