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
		TeamPlan plan = assessTeam(account);
		return plan.blockers.isEmpty() ? plan.evidence : null;
	}

	/** Explains abstention using observed facts, without asking the player to obtain gear. */
	public List<String> explainNoAdvice(AccountSnapshot account)
	{
		if (!evaluate(account).isEmpty()) { return List.of(); }
		List<String> reasons = new ArrayList<>();
		prayerReason(reasons, account, account.rigour, "Rigour", 74, KnowledgeCatalog.Kind.UNLOCK_RIGOUR);
		prayerReason(reasons, account, account.augury, "Augury", 77, KnowledgeCatalog.Kind.UNLOCK_AUGURY);
		if (catalog.decisions.stream().anyMatch(d -> d.kind == KnowledgeCatalog.Kind.PLAN_TEAM && "reviewed".equals(d.status)))
		{
			reasons.addAll(assessTeam(account).blockers);
		}
		if (catalog.decisions.stream().anyMatch(d -> !"reviewed".equals(d.status)))
		{
			reasons.add("Samples awaiting review are excluded from suggestions.");
		}
		return List.copyOf(reasons);
	}

	private void prayerReason(List<String> reasons, AccountSnapshot account, Observation<Boolean> unlock,
		String prayer, int target, KnowledgeCatalog.Kind scrollKind)
	{
		if (!unlock.known())
		{
			reasons.add(prayer + ": unlock state unknown; no scroll or training suggestion can be confirmed.");
		}
		else if (!unlock.value())
		{
			Decision decision = catalog.decisions.stream().filter(d -> d.kind == scrollKind).findFirst().orElse(null);
			String storage = decision == null || account.observedItem(decision.itemId) == Truth.UNKNOWN
				? "unread containers remain unknown" : "other storage remains unknown";
			reasons.add(prayer + ": not unlocked, but no eligible reviewed action with a positively observed scroll; " + storage + ".");
		}
		else if (account.atLeast("PRAYER", target) == Truth.TRUE)
		{
			reasons.add(prayer + ": already unlocked and its Prayer level met. Its scroll and Prayer-training samples are complete.");
		}
		else
		{
			reasons.add(prayer + ": unlocked. Training sample needs confirmed Prayer below " + target
				+ " and Defence at least 70; observed Prayer " + value(account.level("PRAYER"))
				+ ", Defence " + value(account.level("DEFENCE")) + ".");
		}
	}

	private TeamPlan assessTeam(AccountSnapshot account)
	{
		List<String> blockers = new ArrayList<>();
		List<String> evidence = new ArrayList<>();
		for (String skill : List.of("ATTACK", "STRENGTH", "RANGED", "MAGIC", "HITPOINTS"))
		{
			baselineReason(blockers, account, skill, 80);
		}
		baselineReason(blockers, account, "DEFENCE", 70);
		baselineReason(blockers, account, "PRAYER", 70);
		for (String style : List.of("melee", "ranged", "magic"))
		{
			Gear item = catalog.gear.get(style).stream().filter(g -> usable(account, g)).findFirst().orElse(null);
			if (item == null)
			{
				weaponReason(blockers, account, style);
				continue;
			}
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
		return new TeamPlan(List.copyOf(evidence), List.copyOf(blockers));
	}

	private static void baselineReason(List<String> blockers, AccountSnapshot account, String skill, int minimum)
	{
		if (account.atLeast(skill, minimum) != Truth.TRUE)
		{
			blockers.add("Team CoX planning: " + skill + " " + value(account.level(skill))
				+ "; this sample needs an observed real level of at least " + minimum + ". This is a preparation baseline, not a raid access requirement.");
		}
	}

	private void weaponReason(List<String> blockers, AccountSnapshot account, String style)
	{
		List<Gear> observed = catalog.gear.get(style).stream()
			.filter(g -> account.observedItem(g.id) == Truth.TRUE).collect(java.util.stream.Collectors.toList());
		if (observed.isEmpty())
		{
			boolean unknown = catalog.gear.get(style).stream().anyMatch(g -> account.observedItem(g.id) == Truth.UNKNOWN);
			blockers.add("Team CoX planning: no covered " + style + " weapon confirmed in observed inventory/equipment/bank. "
				+ (unknown ? "Unread containers remain unknown. " : "Other storage remains unknown. ")
				+ (style.equals("magic") ? "Only selected charged powered-staff variants are recognised." : "Unrecognised variants are outside this sample."));
			return;
		}
		Set<String> details = new java.util.LinkedHashSet<>();
		for (Gear item : observed)
		{
			if (account.atLeast(item.skill, item.level) != Truth.TRUE)
			{
				details.add(item.label + " observed, but " + item.skill + " " + value(account.level(item.skill))
					+ " does not confirm its " + item.level + " wield level");
			}
			if (item.crystal)
			{
				if (account.atLeast("AGILITY", 70) != Truth.TRUE)
				{
					details.add("Bowfa observed, but 70 Agility is not confirmed (observed " + value(account.level("AGILITY")) + ")");
				}
				for (String part : List.of("crystalBody", "crystalLegs", "crystalHelm"))
				{
					if (catalog.gear.get(part).stream().noneMatch(g -> usable(account, g)))
					{
						details.add("Bowfa observed, but recognised active " + part.replace("crystal", "crystal ").toLowerCase(java.util.Locale.ROOT)
						+ " is not confirmed; this sample currently covers default-colour armour only");
					}
				}
			}
		}
		blockers.add("Team CoX planning (" + style + "): " + String.join("; ", details) + ".");
	}

	private static final class TeamPlan
	{
		final List<String> evidence;
		final List<String> blockers;
		TeamPlan(List<String> evidence, List<String> blockers)
		{
			this.evidence = evidence;
			this.blockers = blockers;
		}
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
