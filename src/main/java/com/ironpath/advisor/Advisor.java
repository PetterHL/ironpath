package com.ironpath.advisor;

import com.ironpath.advisor.KnowledgeCatalog.Decision;
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
			default: return null;
		}
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
