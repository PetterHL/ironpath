package com.ironpath.advisor;

import com.ironpath.advisor.KnowledgeCatalog.Decision;
import com.ironpath.advisor.KnowledgeCatalog.Condition;
import com.ironpath.advisor.KnowledgeCatalog.Gear;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.model.Truth;
import com.ironpath.preferences.Preferences;
import java.util.Map;
import java.util.LinkedHashMap;
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

	public List<Recommendation> evaluate(AccountSnapshot account) { return evaluate(account, Preferences.EMPTY); }

	public List<Recommendation> evaluate(AccountSnapshot account, Preferences preferences)
	{
		AccountSnapshot effective = withUnlocks(account, preferences);
		List<Recommendation> candidates = new ArrayList<>();
		for (Decision decision : catalog.decisions)
		{
			if (!"reviewed".equals(decision.status) || preferences.skipped.contains(decision.id)) { continue; }
			if (truth(decision.when, effective) != Truth.TRUE || completed(decision, effective, preferences) != Truth.FALSE) { continue; }
			if (decision.requires.stream().anyMatch(id -> !dependencyMet(id, effective, preferences))) { continue; }
			List<String> evidence = match(decision, effective);
			if (evidence != null)
			{
				List<String> details = new ArrayList<>(evidence);
				details.add("Selection: " + goalReason(decision, preferences.goal) + "; " + benefitReason(decision)
					+ "; effort band " + decision.effortBand + ". Ties use stable decision ID. No efficiency score.");
				candidates.add(new Recommendation(decision, details));
			}
		}
		candidates.sort(Comparator.comparingInt((Recommendation r) -> goalRank(r.decision, preferences.goal))
			.thenComparingInt(r -> benefitRank(r.decision, effective))
			.thenComparing(r -> r.decision.effortBand).thenComparing(r -> r.decision.id));
		Set<String> families = new HashSet<>();
		List<Recommendation> result = new ArrayList<>();
		for (Recommendation recommendation : candidates)
		{
			if (families.add(recommendation.decision.family)) { result.add(recommendation); }
			if (result.size() == 3) { break; }
		}
		return List.copyOf(result);
	}

	private boolean dependencyMet(String id, AccountSnapshot account, Preferences preferences)
	{
		Decision dependency = decision(id);
		return "reviewed".equals(dependency.status) && completed(dependency, account, preferences) == Truth.TRUE;
	}

	private Decision decision(String id) { return catalog.decisions.stream().filter(d -> d.id.equals(id)).findFirst().orElseThrow(); }

	public static Map<String, String> goalChoices(KnowledgeCatalog catalog)
	{
		Map<String, String> goals = new LinkedHashMap<>();
		Map<String, String> supported = Map.of("plan-team-cox", "Learn / improve ordinary team CoX",
			"train-rigour", "Unlock and meet Rigour's use levels", "train-augury", "Unlock and meet Augury's use levels",
			"assemble-swamp-trident", "Review an owned-parts swamp trident upgrade");
		for (String id : List.of("plan-team-cox", "train-rigour", "train-augury", "assemble-swamp-trident"))
		{
			if (catalog.decisions.stream().anyMatch(d -> d.id.equals(id) && "reviewed".equals(d.status))) { goals.put(id, supported.get(id)); }
		}
		return java.util.Collections.unmodifiableMap(goals);
	}

	private int goalRank(Decision candidate, String goal)
	{
		if (goal == null) { return 0; }
		if (goal.equals("plan-team-cox")) { return candidate.family.equals("team-preparation") ? 0 : 1; }
		if (candidate.id.equals(goal)) { return 0; }
		return decision(goal).requires.contains(candidate.id) ? 0 : 1;
	}

	private String goalReason(Decision candidate, String goal)
	{
		return goal == null ? "no selected goal" : goalRank(candidate, goal) == 0 ? "directly supports selected goal" : "other CoX preparation";
	}

	private int benefitRank(Decision d, AccountSnapshot account)
	{
		switch (d.kind)
		{
			case UNLOCK_RIGOUR: case UNLOCK_AUGURY: case TRAIN_RIGOUR: return 0;
			case TRAIN_AUGURY: return account.atLeast("PRAYER", 74) == Truth.TRUE ? 0 : 1;
			case ASSEMBLE_SWAMP: case REVIEW_LANCE: case REVIEW_OCCULT: return 2;
			case COORDINATE_REDUCTION: return 3;
			default: return 4;
		}
	}

	private String benefitReason(Decision d)
	{
		switch (d.kind)
		{
			case UNLOCK_RIGOUR: case UNLOCK_AUGURY: return "confirmed locked prayer with owned scroll";
			case TRAIN_RIGOUR: case TRAIN_AUGURY: return "confirmed Prayer gap; Rigour's lower threshold first unless a goal changes priority";
			case ASSEMBLE_SWAMP: return "owned parts and no recognised competing magic option";
			case REVIEW_LANCE: case REVIEW_OCCULT: return "owned specialised alternative to the observed kit";
			case COORDINATE_REDUCTION: return "specific owned team option before broad team planning";
			default: return "recognised three-style kit supports team planning";
		}
	}

	private static Observation<Boolean> unlockFact(Observation<Boolean> live, Preferences preferences, String key)
	{
		// A readable permanent unlock is authoritative. Manual facts are a labelled fallback.
		if (live.known()) { return live; }
		Preferences.Confirmation manual = preferences.confirmations.get(key);
		return manual == null ? live : Observation.observed(manual.value, manual.time, "manual planning confirmation: " + key);
	}

	private static AccountSnapshot withUnlocks(AccountSnapshot account, Preferences preferences)
	{
		return new AccountSnapshot(account.levels, account.inventory, account.equipment, account.bank,
			unlockFact(account.rigour, preferences, "rigour"), unlockFact(account.augury, preferences, "augury"));
	}

	/** Possession for planning only. A manual positive never creates a charged/equipped weapon. */
	private Truth possession(AccountSnapshot account, Preferences preferences, int id)
	{
		Truth live = account.observedItem(id);
		if (live == Truth.TRUE) { return live; }
		Preferences.Confirmation manual = preferences.confirmations.get("item-" + id);
		return manual == null ? Truth.UNKNOWN : manual.value ? Truth.TRUE : Truth.FALSE;
	}

	private Truth completionTruth(Condition condition, AccountSnapshot account, Preferences preferences)
	{
		switch (condition.test)
		{
			case ALL: return Truth.all(condition.conditions.stream().map(c -> completionTruth(c, account, preferences)).toArray(Truth[]::new));
			case ANY: return Truth.any(condition.conditions.stream().map(c -> completionTruth(c, account, preferences)).toArray(Truth[]::new));
			case ITEMS:
				// Preserve T2's bounded-storage comparison, but suppress redundant work for manual ownership elsewhere.
				return Truth.any(catalog.itemSets.get(condition.key).stream().map(id ->
				{
					Truth live = account.observedItem(id);
					Preferences.Confirmation manual = preferences.confirmations.get("item-" + id);
					return live == Truth.TRUE || (manual != null && manual.value) ? Truth.TRUE : live;
				}).toArray(Truth[]::new));
			default: return truth(condition, account);
		}
	}

	/** Goal guidance is separate from available recommendation cards; unknown ownership never becomes missing. */
	public GoalView goal(AccountSnapshot raw, Preferences preferences)
	{
		if (preferences.goal == null || !goalChoices(catalog).containsKey(preferences.goal)) { return null; }
		AccountSnapshot account = withUnlocks(raw, preferences);
		Decision goal = decision(preferences.goal);
		List<Requirement> requirements = new ArrayList<>();
		if (goal.kind == KnowledgeCatalog.Kind.TRAIN_RIGOUR || goal.kind == KnowledgeCatalog.Kind.TRAIN_AUGURY)
		{
			boolean rigour = goal.kind == KnowledgeCatalog.Kind.TRAIN_RIGOUR;
			Observation<Boolean> fact = rigour ? account.rigour : account.augury;
			String dependency = rigour ? "unlock-rigour" : "unlock-augury";
			requirements.add(new Requirement((rigour ? "Rigour" : "Augury") + " permanently unlocked", Truth.of(fact), fact.evidence(),
				Truth.of(fact) != Truth.TRUE && preferences.skipped.contains(dependency)));
			requirements.add(levelRequirement(account, "DEFENCE", 70));
			requirements.add(levelRequirement(account, "PRAYER", rigour ? 74 : 77));
		}
		else
		{
			addRequirements(goal.when, account, preferences, requirements);
			if (goal.kind == KnowledgeCatalog.Kind.PLAN_TEAM)
			{
				for (String style : List.of("melee", "ranged", "magic"))
				{
					Gear weapon = catalog.gear.get(style).stream().filter(g -> usable(account, g)).findFirst().orElse(null);
					requirements.add(new Requirement("Recognised observed " + style + " option with wield/package levels",
						weapon != null ? Truth.TRUE : Truth.UNKNOWN,
						weapon != null ? evidence(account, weapon) : "Not confirmed; unrecognised variants, charges and other storage remain unknown.", false));
					if (weapon == null)
					{
						// Explain a confirmed level blocker for the lowest-level positively observed path.
						// This does not declare other weapon ownership absent or make Bowfa mandatory.
						Gear observed = catalog.gear.get(style).stream().filter(g -> account.observedItem(g.id) == Truth.TRUE)
							.sorted(Comparator.comparingInt(g -> g.level)).findFirst().orElse(null);
						if (observed != null)
						{
							if (account.atLeast(observed.skill, observed.level) != Truth.TRUE)
							{ requirements.add(levelRequirement(account, observed.skill, observed.level)); }
							if (observed.crystal && account.atLeast("AGILITY", 70) != Truth.TRUE)
							{ requirements.add(levelRequirement(account, "AGILITY", 70)); }
						}
					}
				}
			}
		}
		boolean completed = goal.kind == KnowledgeCatalog.Kind.ASSEMBLE_SWAMP
			? completed(goal, account, preferences) == Truth.TRUE
			: !goal.repeatable && requirements.stream().allMatch(r -> r.truth == Truth.TRUE);
		if (completed && goal.kind == KnowledgeCatalog.Kind.ASSEMBLE_SWAMP)
		{
			requirements.clear(); // An owned alternative makes assembly inputs unnecessary.
			List<String> evidence = new ArrayList<>();
			for (int id : catalog.itemSets.get("competingMagic"))
			{
				if (account.observedItem(id) == Truth.TRUE) { evidence.add(catalog.factChoices().get("item-" + id) + ": " + account.itemEvidence(id)); }
				else if (preferences.confirmations.containsKey("item-" + id) && preferences.confirmations.get("item-" + id).value)
				{ evidence.add(catalog.factChoices().get("item-" + id) + ": " + preferences.confirmations.get("item-" + id).evidence()); }
			}
			requirements.add(new Requirement("Owned outcome / supported alternative; another assembly is unnecessary", Truth.TRUE, String.join("; ", evidence), false));
		}
		boolean skipped = preferences.skipped.contains(goal.id);
		boolean skippedRequirement = requirements.stream().anyMatch(r -> r.skipped);
		String nearest = requirements.stream().filter(r -> r.truth == Truth.FALSE).map(r -> r.label).findFirst().orElse(null);
		List<Recommendation> available = evaluate(raw, preferences);
		Recommendation next = available.stream().filter(r -> r.decision.id.equals(goal.id) || goal.requires.contains(r.decision.id)
			|| (goal.kind == KnowledgeCatalog.Kind.PLAN_TEAM && r.decision.family.equals("team-preparation"))).findFirst().orElse(null);
		boolean goalActionAvailable = next != null && (next.decision.id.equals(goal.id)
			|| (goal.kind == KnowledgeCatalog.Kind.PLAN_TEAM && next.decision.family.equals(goal.family)
				&& requirements.stream().allMatch(r -> r.truth == Truth.TRUE)));
		String status = completed ? "Satisfied by confirmed outcome or supported alternative"
			: skipped || skippedRequirement ? "Blocked by a skipped action; skipping does not complete it"
			: goalActionAvailable ? "Available preparation action"
			: nearest != null ? "Blocked: a confirmed prerequisite is missing"
			: "Needs observation or confirmation; prerequisites remain unknown";
		String nextAction = completed ? "No further action for this target. Clear the goal or choose another."
			: skipped || skippedRequirement ? "Restore the skipped action to resume this route."
			: next != null ? next.decision.title + ". This is the next supported action, not certification of raid access."
			: "No acquisition route is covered for the unconfirmed prerequisites. Observe your bank / supported variants or confirm planning facts. Check the goal's source requirements.";
		return new GoalView(goalChoices(catalog).get(goal.id), status, requirements, nearest, nextAction, goal);
	}

	private Requirement levelRequirement(AccountSnapshot account, String skill, int minimum)
	{
		return new Requirement(skill + " >= " + minimum, account.atLeast(skill, minimum),
			"Real level " + value(account.level(skill)) + ": " + account.levels.evidence(), false);
	}

	private void addRequirements(Condition condition, AccountSnapshot account, Preferences preferences, List<Requirement> requirements)
	{
		if (condition.test == KnowledgeCatalog.Test.ALL)
		{
			for (Condition child : condition.conditions) { addRequirements(child, account, preferences, requirements); }
		}
		else if (condition.test == KnowledgeCatalog.Test.LEVEL) { requirements.add(levelRequirement(account, condition.key, condition.minimum)); }
		else if (condition.test == KnowledgeCatalog.Test.ITEMS)
		{
			List<Integer> ids = catalog.itemSets.get(condition.key);
			Truth owned = Truth.any(ids.stream().map(id -> possession(account, preferences, id)).toArray(Truth[]::new));
			String label = ids.stream().map(id -> catalog.factChoices().get("item-" + id)).collect(java.util.stream.Collectors.joining(" / "));
			List<String> evidence = new ArrayList<>();
			for (int id : ids)
			{
				if (account.observedItem(id) == Truth.TRUE) { evidence.add(account.itemEvidence(id)); }
				else if (preferences.confirmations.containsKey("item-" + id)) { evidence.add(preferences.confirmations.get("item-" + id).evidence()); }
			}
			requirements.add(new Requirement("Possession: " + label, owned,
				evidence.isEmpty() ? "Unobserved ownership remains unknown; container absence is not global absence." : String.join("; ", evidence), false));
		}
	}

	public static final class Requirement
	{
		public final String label, evidence;
		public final Truth truth;
		public final boolean skipped;
		Requirement(String label, Truth truth, String evidence, boolean skipped)
		{ this.label = label; this.truth = truth; this.evidence = evidence; this.skipped = skipped; }
	}

	public static final class GoalView
	{
		public final String title, status, nearestMissing, nextAction;
		public final List<Requirement> requirements;
		public final Decision decision;
		GoalView(String title, String status, List<Requirement> requirements, String nearestMissing, String nextAction, Decision decision)
		{
			this.title = title; this.status = status; this.requirements = List.copyOf(requirements);
			this.nearestMissing = nearestMissing; this.nextAction = nextAction; this.decision = decision;
		}
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

	private Truth completed(Decision decision, AccountSnapshot account, Preferences preferences)
	{
		return decision.repeatable ? Truth.FALSE : completionTruth(decision.completeWhen, account, preferences);
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
		return List.of(prayer + " unlock confirmed for planning: " + unlock.evidence(),
			"Prayer " + level + " < " + target + "; Defence " + account.level("DEFENCE") + " >= 70: " + account.levels.evidence());
	}

	private List<String> planTeam(AccountSnapshot account)
	{
		TeamPlan plan = assessTeam(account);
		return plan.blockers.isEmpty() ? plan.evidence : null;
	}

	/** Explains abstention using observed facts, without asking the player to obtain gear. */
	public List<String> explainNoAdvice(AccountSnapshot account) { return explainNoAdvice(account, Preferences.EMPTY); }

	public List<String> explainNoAdvice(AccountSnapshot raw, Preferences preferences)
	{
		if (!evaluate(raw, preferences).isEmpty()) { return List.of(); }
		AccountSnapshot account = withUnlocks(raw, preferences);
		List<String> reasons = new ArrayList<>();
		if (!preferences.skipped.isEmpty()) { reasons.add("Skipped actions are excluded; skipping never completes a prerequisite. Restore them in planning controls."); }
		if (!preferences.confirmations.isEmpty()) { reasons.add("Manual confirmations are planning assumptions. Clear them to restore live / unknown state. They cannot establish usable weapons."); }
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
