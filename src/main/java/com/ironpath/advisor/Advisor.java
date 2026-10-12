package com.ironpath.advisor;

import com.ironpath.advisor.KnowledgeCatalog.Condition;
import com.ironpath.advisor.KnowledgeCatalog.Decision;
import com.ironpath.advisor.KnowledgeCatalog.Gear;
import com.ironpath.advisor.KnowledgeCatalog.Goal;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.model.Truth;
import com.ironpath.preferences.Preferences;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Pure deterministic evaluation. No RuneLite, Swing, clock, IO or inferred global ownership. */
public final class Advisor
{
	private final KnowledgeCatalog catalog;
	public Advisor(KnowledgeCatalog catalog) { this.catalog = catalog; }
	public List<Recommendation> evaluate(AccountSnapshot account) { return evaluate(account, Preferences.EMPTY); }

	public List<Recommendation> evaluate(AccountSnapshot account, Preferences preferences)
	{
		List<Recommendation> candidates = candidates(account, preferences);
		Set<String> activities = new HashSet<>(), families = new HashSet<>();
		List<Recommendation> result = new ArrayList<>();
		for (Recommendation recommendation : candidates)
		{
			Decision d = recommendation.decision;
			if (activities.contains(d.activity) || families.contains(d.family)) { continue; }
			activities.add(d.activity); families.add(d.family);
			List<String> evidence = new ArrayList<>(recommendation.evidence);
			Recommendation competing = candidates.stream().filter(r -> !r.decision.id.equals(d.id)
				&& r.decision.outcome.equals(d.outcome)).findFirst().orElse(null);
			if (competing != null)
			{
				evidence.add("Competing route: " + competing.decision.title + ". Order follows contextual benefit, optional goal relevance, then remaining effort and stable ID; both remain reviewed choices.");
			}
			result.add(new Recommendation(d, evidence));
			if (result.size() == 3) { break; }
		}
		return List.copyOf(result);
	}

	private List<Recommendation> candidates(AccountSnapshot account, Preferences preferences)
	{
		List<Recommendation> candidates = new ArrayList<>();
		for (Decision d : catalog.decisions)
		{
			if (!"reviewed".equals(d.status) || skipped(d, preferences) || completed(d, account, preferences) != Truth.FALSE
				|| truth(d.when, account, preferences) != Truth.TRUE || truth(d.accessWhen, account, preferences) != Truth.TRUE
				|| truth(d.prepWhen, account, preferences) != Truth.TRUE
				|| d.requires.stream().anyMatch(id -> completed(decision(id), account, preferences) != Truth.TRUE
					|| !"reviewed".equals(decision(id).status))) { continue; }
			List<String> evidence = new ArrayList<>();
			collectEvidence(d.when, account, preferences, evidence);
			collectEvidence(d.accessWhen, account, preferences, evidence);
			collectEvidence(d.prepWhen, account, preferences, evidence);
			if (d.completeWhen != null && d.completeWhen.test == KnowledgeCatalog.Test.LEVEL)
			{
				evidence.add(displaySkill(d.completeWhen.key) + " " + account.level(d.completeWhen.key) + " < "
					+ d.completeWhen.minimum + ": " + account.levels.evidence());
			}
			if (d.completeWhen != null && d.kind == KnowledgeCatalog.Kind.ACQUIRE)
			{ evidence.add("Target gap confirmed for planning: " + conditionEvidence(d.completeWhen, account, preferences)); }
			evidence.addAll(d.notes);
			evidence.add("Selection: " + (preferences.goal == null ? "no selected goal" : goalRank(d, preferences.goal) < 2
				? "directly supports selected goal" : "another supported activity") + "; " + benefitReason(d, account, preferences)
				+ "; effort band " + d.effortBand
				+ ". Ties use stable decision ID. No efficiency score.");
			candidates.add(new Recommendation(d, evidence.stream().distinct().collect(Collectors.toList())));
		}
		candidates.sort(Comparator.comparing((Recommendation r) -> benefit(r.decision, account, preferences))
			.thenComparingInt(r -> goalRank(r.decision, preferences.goal))
			.thenComparing(r -> r.decision.effortBand).thenComparing(r -> r.decision.id));
		return candidates;
	}

	/** Substantial outcomes, independent of route availability and selected goals. */
	public List<UpgradeView> upgrades(AccountSnapshot account, Preferences preferences)
	{
		List<UpgradeView> result = new ArrayList<>();
		for (KnowledgeCatalog.Upgrade upgrade : catalog.upgrades)
		{
			if (!"reviewed".equals(upgrade.status) || truth(upgrade.when, account, preferences) != Truth.TRUE
				|| truth(upgrade.ownedWhen, account, preferences) == Truth.TRUE) { continue; }
			List<RouteView> routes = new ArrayList<>();
			for (String id : upgrade.routes)
			{
				Decision route = decision(id);
				if (skipped(route, preferences) || !"reviewed".equals(route.status)
					|| completed(route, account, preferences) == Truth.TRUE) { continue; }
				List<Requirement> requirements = new ArrayList<>();
				addRequirements(route.accessWhen, account, preferences, requirements, false, "ACCESS");
				addRequirements(route.when, account, preferences, requirements, false, "INPUTS / CONTEXT");
				addRequirements(route.prepWhen, account, preferences, requirements, false, "SUGGESTED PREPARATION");
				for (String dependency : route.requires)
				{ addRequirements(decision(dependency).completeWhen, account, preferences, requirements,
					skipped(decision(dependency), preferences), "PREREQUISITE"); }
				Requirement next = requirements.stream().filter(r -> r.truth != Truth.TRUE || r.skipped).findFirst().orElse(null);
				Truth access = truth(route.accessWhen, account, preferences);
				String status = access == Truth.FALSE ? "Needs access prerequisite" : access == Truth.UNKNOWN ? "Access check needed"
					: next == null ? "Supported planning route; recheck supplies and current access"
					: next.role.equals("SUGGESTED PREPARATION") ? "Preparation to review" : "Inputs to check";
				routes.add(new RouteView(route, status, next == null ? route.tradeoff
					: next.role + ": " + next.label + " — " + (next.truth == Truth.FALSE ? "confirmed gap" : "unconfirmed planning input"), requirements, next == null));
			}
			if (routes.isEmpty()) { continue; }
			routes.sort(Comparator.comparing((RouteView r) -> !r.available).thenComparing(r -> r.decision.effortBand).thenComparing(r -> r.decision.id));
			List<String> evidence = new ArrayList<>(); collectEvidence(upgrade.when, account, preferences, evidence);
			List<String> questions = new ArrayList<>();
			if (truth(upgrade.ownedWhen, account, preferences) == Truth.UNKNOWN) { questions.addAll(ownershipQuestions(upgrade.ownedWhen, account, preferences)); }
			for (String set : upgrade.unknownSets)
			{ if (ownership(set, account, preferences) == Truth.UNKNOWN) { questions.add("owns-" + set); } }
			List<String> applications = upgrade.applications.stream().filter(a -> truth(a.when, account, preferences) == Truth.TRUE)
				.map(a -> a.text).collect(Collectors.toList());
			KnowledgeCatalog.ContextBenefit context = upgrade.contextBenefits.stream()
				.filter(b -> truth(b.when, account, preferences) == Truth.TRUE).findFirst().orElse(null);
			KnowledgeCatalog.Benefit band = context == null ? upgrade.benefitBand : context.band;
			List<Requirement> equip = new ArrayList<>(); addRequirements(upgrade.equipWhen, account, preferences, equip, false, "EQUIP");
			List<Requirement> parts = new ArrayList<>();
			for (KnowledgeCatalog.Part part : upgrade.parts)
			{ parts.add(new Requirement(part.label, planningTruth(part.when, account, preferences), conditionEvidence(part.when, account, preferences), false,
				part.optional ? "OPTIONAL ADDITION" : "COMPONENT / ASSEMBLY")); }
			Requirement outstandingPart = parts.stream().filter(r -> r.role.equals("COMPONENT / ASSEMBLY") && r.truth != Truth.TRUE).findFirst().orElse(null);
			if (outstandingPart != null)
			{
				for (int i = 0; i < routes.size(); i++)
				{
					RouteView route = routes.get(i);
					if (route.available) { routes.set(i, new RouteView(route.decision, route.status,
						"Component to " + (outstandingPart.truth == Truth.FALSE ? "obtain" : "confirm") + ": " + outstandingPart.label,
						route.requirements, true)); }
				}
			}
			List<String> shared = new ArrayList<>();
			for (KnowledgeCatalog.SharedReward reward : upgrade.shared)
			{
				if (truth(reward.when, account, preferences) != Truth.TRUE || truth(reward.ownedWhen, account, preferences) == Truth.TRUE) { continue; }
				shared.add(reward.title + " — eligible from completed delve " + reward.minDelve + ". "
					+ (truth(reward.ownedWhen, account, preferences) == Truth.UNKNOWN ? "Ownership/alternatives unconfirmed. " : "Planning gap confirmed. ") + reward.detail);
			}
			String label = questions.isEmpty() ? "Planning gap confirmed" : "Ownership unconfirmed — if you do not already own the outcome/alternative";
			if (routes.stream().noneMatch(r -> r.available)) { label += " · Plan ahead"; }
			if (equip.stream().anyMatch(r -> r.truth != Truth.TRUE)) { label += " · Needs equip levels"; }
			if (band == KnowledgeCatalog.Benefit.MINOR) { label += " · Contextual / consider deferring"; }
			result.add(new UpgradeView(upgrade, label, context == null ? upgrade.valueReason : context.reason,
				band, evidence, applications, questions.stream().distinct().collect(Collectors.toList()), routes, equip, parts, shared));
		}
		result.sort(Comparator.comparing((UpgradeView u) -> u.band)
			.thenComparingInt(u -> preferences.goal != null && u.upgrade.goals.contains(preferences.goal) ? 0 : 1)
			.thenComparing(Comparator.comparingInt((UpgradeView u) -> u.applications.size()).reversed())
			.thenComparing(u -> u.routes.stream().noneMatch(r -> r.available)).thenComparing(u -> u.upgrade.id));
		// Retain a supported available alternative beside longer blocked plans in the first three slots.
		if (result.size() > 3 && result.subList(0, 3).stream().noneMatch(u -> u.routes.stream().anyMatch(r -> r.available)))
		{
			UpgradeView available = result.subList(3, result.size()).stream().filter(u -> u.routes.stream().anyMatch(r -> r.available)).findFirst().orElse(null);
			if (available != null) { result.remove(available); result.add(2, available); }
		}
		return List.copyOf(result);
	}

	private List<String> ownershipQuestions(Condition c, AccountSnapshot a, Preferences p)
	{
		if (c.test == KnowledgeCatalog.Test.ALL || c.test == KnowledgeCatalog.Test.ANY)
		{ return c.conditions.stream().flatMap(child -> ownershipQuestions(child, a, p).stream()).collect(Collectors.toList()); }
		return c.test == KnowledgeCatalog.Test.POSSESSION && ownership(c.key, a, p) == Truth.UNKNOWN ? List.of("owns-" + c.key) : List.of();
	}

	/** Administrative steps do not occupy the primary outcome slots. */
	public List<Recommendation> quickActions(AccountSnapshot a, Preferences p)
	{
		return candidates(a, p).stream().filter(r -> r.decision.kind == KnowledgeCatalog.Kind.READ
			|| r.decision.kind == KnowledgeCatalog.Kind.ASSEMBLE || r.decision.kind == KnowledgeCatalog.Kind.REVIEW).collect(Collectors.toList());
	}

	public List<Recommendation> availableActivities(AccountSnapshot a, Preferences p)
	{ return List.copyOf(candidates(a, p)); }

	public static final class RouteView
	{
		public final Decision decision;
		public final String status, next;
		public final List<Requirement> requirements;
		public final boolean available;
		RouteView(Decision decision, String status, String next, List<Requirement> requirements, boolean available)
		{ this.decision = decision; this.status = status; this.next = next; this.requirements = List.copyOf(requirements); this.available = available; }
	}
	public static final class UpgradeView
	{
		public final KnowledgeCatalog.Upgrade upgrade;
		public final String label, value;
		public final KnowledgeCatalog.Benefit band;
		public final List<String> evidence, applications, questions, shared;
		public final List<RouteView> routes;
		public final List<Requirement> equip, parts;
		UpgradeView(KnowledgeCatalog.Upgrade upgrade, String label, String value, KnowledgeCatalog.Benefit band,
			List<String> evidence, List<String> applications, List<String> questions, List<RouteView> routes,
			List<Requirement> equip, List<Requirement> parts, List<String> shared)
		{
			this.upgrade = upgrade; this.label = label; this.value = value; this.band = band;
			this.evidence = List.copyOf(evidence); this.applications = List.copyOf(applications); this.questions = List.copyOf(questions);
			this.routes = List.copyOf(routes); this.equip = List.copyOf(equip); this.parts = List.copyOf(parts); this.shared = List.copyOf(shared);
		}
	}

	private boolean skipped(Decision d, Preferences p) { return p.skipped.contains(d.id) || p.skipped.contains(d.activity); }
	private Decision decision(String id) { return catalog.decisions.stream().filter(d -> d.id.equals(id)).findFirst().orElseThrow(); }
	public static Map<String, String> goalChoices(KnowledgeCatalog catalog)
	{
		Map<String, String> choices = new LinkedHashMap<>();
		catalog.goals.values().forEach(g -> { if ("reviewed".equals(catalog.decisions.stream()
			.filter(d -> d.id.equals(g.decisionId)).findFirst().orElseThrow().status)) { choices.put(g.id, g.title); } });
		return java.util.Collections.unmodifiableMap(choices);
	}
	private int goalRank(Decision d, String id)
	{
		Goal goal = catalog.goals.get(id);
		if (goal == null) { return 0; }
		if (goal.routes.contains(d.id) || goal.requires.contains(d.id)) { return 0; }
		return d.goals.contains(id) ? 1 : 2;
	}
	private KnowledgeCatalog.Benefit benefit(Decision d, AccountSnapshot a, Preferences p)
	{
		return d.contextBenefits.stream().filter(b -> truth(b.when, a, p) == Truth.TRUE).map(b -> b.band).findFirst().orElse(d.benefitBand);
	}
	private String benefitReason(Decision d, AccountSnapshot a, Preferences p)
	{
		return d.contextBenefits.stream().filter(b -> truth(b.when, a, p) == Truth.TRUE).map(b -> b.reason).findFirst().orElse(d.benefitReason);
	}

	private Observation<Boolean> fact(AccountSnapshot a, Preferences p, String key)
	{
		Observation<Boolean> live = a.fact(key);
		if (live.known()) { return live; }
		Preferences.Confirmation manual = p.confirmations.get(key);
		return manual == null ? live : Observation.observed(manual.value, manual.time, "manual planning confirmation: " + key);
	}
	private Truth possession(AccountSnapshot a, Preferences p, int id)
	{
		if (a.observedItem(id) == Truth.TRUE) { return Truth.TRUE; }
		Preferences.Confirmation manual = p.confirmations.get("item-" + id);
		return manual == null ? Truth.UNKNOWN : manual.value ? Truth.TRUE : Truth.FALSE;
	}
	private Truth ownership(String set, AccountSnapshot a, Preferences p)
	{
		Truth exact = Truth.any(catalog.itemSets.get(set).stream().map(id -> possession(a, p, id)).toArray(Truth[]::new));
		if (exact == Truth.TRUE) { return Truth.TRUE; }
		// Historical and new targets can name the same exact variant set. Their
		// confirmations have the same meaning; a positive still wins a conflict.
		List<Preferences.Confirmation> groups = p.confirmations.entrySet().stream()
			.filter(e -> sameOwnershipSet(e.getKey(), set)).map(Map.Entry::getValue).collect(Collectors.toList());
		if (groups.stream().anyMatch(c -> c.value)) { return Truth.TRUE; }
		return groups.isEmpty() ? exact : Truth.FALSE;
	}
	private boolean sameOwnershipSet(String key, String set)
	{
		return key.startsWith("owns-") && catalog.itemSets.containsKey(key.substring(5))
			&& new HashSet<>(catalog.itemSets.get(set)).equals(new HashSet<>(catalog.itemSets.get(key.substring(5))));
	}
	private Truth completionTruth(Condition c, AccountSnapshot a, Preferences p)
	{
		if (c.test == KnowledgeCatalog.Test.ALL) { return Truth.all(c.conditions.stream().map(x -> completionTruth(x, a, p)).toArray(Truth[]::new)); }
		if (c.test == KnowledgeCatalog.Test.ANY) { return Truth.any(c.conditions.stream().map(x -> completionTruth(x, a, p)).toArray(Truth[]::new)); }
		if (c.test == KnowledgeCatalog.Test.ITEMS)
		{
			if (ownership(c.key, a, p) == Truth.TRUE) { return Truth.TRUE; }
			// Historical owned-parts/switch reminders use bounded storage absence, never an acquisition grind.
			return Truth.any(catalog.itemSets.get(c.key).stream().map(id -> possession(a, p, id) == Truth.TRUE
				? Truth.TRUE : a.observedItem(id)).toArray(Truth[]::new));
		}
		return truth(c, a, p);
	}
	private Truth completed(Decision d, AccountSnapshot a, Preferences p)
	{ return d.repeatable ? Truth.FALSE : completionTruth(d.completeWhen, a, p); }

	Truth truth(Condition c, AccountSnapshot a) { return truth(c, a, Preferences.EMPTY); }
	private Truth truth(Condition c, AccountSnapshot a, Preferences p)
	{
		switch (c.test)
		{
			case ALL: return Truth.all(c.conditions.stream().map(x -> truth(x, a, p)).toArray(Truth[]::new));
			case ANY: return Truth.any(c.conditions.stream().map(x -> truth(x, a, p)).toArray(Truth[]::new));
			case LEVEL: return a.atLeast(c.key, c.minimum);
			case ITEMS: return expected(Truth.any(catalog.itemSets.get(c.key).stream().map(a::observedItem).toArray(Truth[]::new)), c.value == null || c.value);
			case USABLE: return Truth.any(items(c.key).stream().map(g -> usable(g, a, p)).toArray(Truth[]::new));
			case POSSESSION: return expected(ownership(c.key, a, p), c.value == null || c.value);
			case EQUIPPED: return !a.equipment.known() ? Truth.UNKNOWN : catalog.itemSets.get(c.key).stream()
				.anyMatch(a.equipment.value()::contains) ? Truth.TRUE : Truth.FALSE;
			case FACT: return expected(Truth.of(fact(a, p, c.key)), c.value);
			case RIGOUR: return expected(Truth.of(fact(a, p, "rigour")), c.value);
			case AUGURY: return expected(Truth.of(fact(a, p, "augury")), c.value);
			case CONSTANT: return c.value ? Truth.TRUE : Truth.FALSE;
			default: return Truth.UNKNOWN;
		}
	}
	private Truth usable(Gear g, AccountSnapshot a, Preferences p)
	{
		return Truth.all(a.observedItem(g.id), a.atLeast(g.skill, g.level),
			g.useWhen == null ? Truth.TRUE : truth(g.useWhen, a, p));
	}
	private List<Gear> items(String set)
	{
		return catalog.itemSets.get(set).stream().map(id -> catalog.gear.values().stream().flatMap(List::stream)
			.filter(g -> g.id == id).findFirst().orElseThrow()).collect(Collectors.toList());
	}
	private static Truth expected(Truth truth, boolean value)
	{ return value || truth == Truth.UNKNOWN ? truth : truth == Truth.TRUE ? Truth.FALSE : Truth.TRUE; }

	private void collectEvidence(Condition c, AccountSnapshot a, Preferences p, List<String> evidence)
	{
		if (c.test == KnowledgeCatalog.Test.ALL || c.test == KnowledgeCatalog.Test.ANY)
		{
			for (Condition child : c.conditions) { if (truth(child, a, p) == Truth.TRUE) { collectEvidence(child, a, p, evidence); } }
		}
		else if (c.test == KnowledgeCatalog.Test.USABLE)
		{
			Gear item = items(c.key).stream().filter(g -> usable(g, a, p) == Truth.TRUE).findFirst().orElse(null);
			if (item != null)
			{
				evidence.add(item.label + " observed in " + a.itemEvidence(item.id));
				if (item.useWhen != null) { collectEvidence(item.useWhen, a, p, evidence); }
			}
		}
		else if (c.test == KnowledgeCatalog.Test.ITEMS || c.test == KnowledgeCatalog.Test.EQUIPPED)
		{
			items(c.key).stream().filter(g -> c.test == KnowledgeCatalog.Test.EQUIPPED
				? a.equipment.known() && a.equipment.value().contains(g.id) : a.observedItem(g.id) == Truth.TRUE)
				.forEach(g -> evidence.add(g.label + (c.test == KnowledgeCatalog.Test.EQUIPPED ? " currently equipped: " + a.equipment.evidence()
					: " observed in " + a.itemEvidence(g.id))));
		}
		else if (c.test != KnowledgeCatalog.Test.CONSTANT) { evidence.add(conditionEvidence(c, a, p)); }
	}
	private String conditionEvidence(Condition c, AccountSnapshot a, Preferences p)
	{
		if (c.test == KnowledgeCatalog.Test.ALL || c.test == KnowledgeCatalog.Test.ANY)
		{
			return c.test + " [" + c.conditions.stream().map(x -> label(x) + " = " + truth(x, a, p) + ": "
				+ conditionEvidence(x, a, p)).collect(Collectors.joining("; ")) + "]";
		}
		if (c.test == KnowledgeCatalog.Test.LEVEL)
		{ return displaySkill(c.key) + " " + (a.level(c.key) == null ? "unknown" : a.level(c.key)) + " >= " + c.minimum + ": " + a.levels.evidence(); }
		if (c.test == KnowledgeCatalog.Test.FACT || c.test == KnowledgeCatalog.Test.RIGOUR || c.test == KnowledgeCatalog.Test.AUGURY)
		{
			String key = c.test == KnowledgeCatalog.Test.FACT ? c.key : c.test.name().toLowerCase(java.util.Locale.ROOT);
			return label(c) + ": " + fact(a, p, key).evidence();
		}
		if (c.test == KnowledgeCatalog.Test.CONSTANT) { return "Authored planning target; no inferred completion."; }
		List<String> evidence = new ArrayList<>();
		for (Gear item : items(c.key))
		{
			if (a.observedItem(item.id) == Truth.TRUE) { evidence.add(item.label + " observed in " + a.itemEvidence(item.id)); }
			else if (p.confirmations.containsKey("item-" + item.id))
			{ evidence.add(item.label + ": " + p.confirmations.get("item-" + item.id).evidence()); }
		}
		p.confirmations.entrySet().stream().filter(e -> sameOwnershipSet(e.getKey(), c.key)).sorted(Map.Entry.comparingByKey())
			.forEach(e -> evidence.add(catalog.factChoices().get(e.getKey()) + ": " + e.getValue().evidence()));
		if (c.test == KnowledgeCatalog.Test.USABLE)
		{
			for (Gear g : items(c.key))
			{
				if (a.observedItem(g.id) != Truth.TRUE) { continue; }
				if (a.atLeast(g.skill, g.level) != Truth.TRUE)
				{ evidence.add(g.label + " observed, but " + g.skill + " " + (a.level(g.skill) == null ? "unknown" : a.level(g.skill)) + " does not confirm its " + g.level + " wield level"); }
				if (g.useWhen != null && truth(g.useWhen, a, p) != Truth.TRUE)
				{ evidence.add("Observed " + g.label + " package needs " + conditionEvidence(g.useWhen, a, p)); }
			}
		}
		if (evidence.isEmpty()) { evidence.add(a.bank.known() ? "Other storage remains unknown." : "Unread containers remain unknown."); }
		if (c.test == KnowledgeCatalog.Test.POSSESSION && ownership(c.key, a, p) == Truth.UNKNOWN)
		{ evidence.add("Confirm only this target under manual confirmations: " + catalog.factChoices().get("owns-" + c.key) + ". Bank absence does not prove global absence."); }
		if (c.test == KnowledgeCatalog.Test.USABLE) { evidence.add("Manual ownership cannot confirm a usable weapon. Only reviewed active/charged forms and wield/package levels are covered; charges, ammunition and supplies still need checking."); }
		return String.join("; ", evidence);
	}
	private String label(Condition c)
	{
		switch (c.test)
		{
			case ALL: case ANY: return c.test + " of " + c.conditions.stream().map(this::label).collect(Collectors.joining(" / "));
			case LEVEL: return c.key + " >= " + c.minimum;
			case FACT: return catalog.factChoices().get(c.key) + (c.value ? "" : " not yet confirmed");
			case RIGOUR: return "Rigour permanently unlocked";
			case AUGURY: return "Augury permanently unlocked";
			case USABLE: return "Recognised observed " + c.key + " option with wield/package levels";
			case ITEMS: case POSSESSION: return "Possession: " + items(c.key).stream().map(g -> g.label).collect(Collectors.joining(" / "));
			case EQUIPPED: return "Equipped: " + c.key;
			default: return "Reviewed planning opportunity";
		}
	}
	private static String displaySkill(String key)
	{ return key.charAt(0) + key.substring(1).toLowerCase(java.util.Locale.ROOT); }

	/** Goal requirements and alternative routes are separate from actionable cards. */
	public GoalView goal(AccountSnapshot a, Preferences p)
	{
		Goal goal = catalog.goals.get(p.goal);
		if (goal == null || !goalChoices(catalog).containsKey(goal.id)) { return null; }
		Decision primary = decision(goal.decisionId);
		boolean complete = completionTruth(goal.completeWhen, a, p) == Truth.TRUE;
		List<Requirement> requirements = new ArrayList<>();
		for (String id : goal.requires)
		{
			Decision dependency = decision(id);
			addRequirements(dependency.completeWhen, a, p, requirements, skipped(dependency, p));
		}
		addRequirements(goal.when, a, p, requirements, false, goal.whenRole);
		if (complete && goal.completeWhen.test != KnowledgeCatalog.Test.CONSTANT)
		{
			requirements.clear();
			requirements.add(new Requirement("Owned outcome / supported alternative; another route is unnecessary", Truth.TRUE,
				conditionEvidence(goal.completeWhen, a, p), false));
		}
		String nearest = requirements.stream().filter(r -> r.truth == Truth.FALSE).map(r -> r.label).findFirst().orElse(null);
		Recommendation next = candidates(a, p).stream().filter(r -> goalRank(r.decision, goal.id) == 0).findFirst().orElse(null);
		boolean blockedSkip = p.skipped.contains(goal.id) || requirements.stream().anyMatch(r -> r.skipped && r.truth != Truth.TRUE)
			|| goal.routes.stream().allMatch(id -> skipped(decision(id), p));
		if (next == null && !complete && !blockedSkip)
		{
			List<Condition> alternatives = new ArrayList<>();
			for (String id : goal.routes)
			{
				Decision route = decision(id);
				if (skipped(route, p) || completed(route, a, p) == Truth.TRUE) { continue; }
				Condition path = new Condition(); path.test = KnowledgeCatalog.Test.ALL;
				path.conditions = List.of(route.accessWhen, route.prepWhen, route.when);
				alternatives.add(path);
				if (route.completeWhen != null && completed(route, a, p) == Truth.UNKNOWN)
				{
					requirements.add(new Requirement("Target to confirm for " + catalog.activities.get(route.activity), Truth.UNKNOWN,
						conditionEvidence(route.completeWhen, a, p), false));
				}
			}
			if (!alternatives.isEmpty())
			{
				Condition any = new Condition(); any.test = KnowledgeCatalog.Test.ANY; any.conditions = alternatives;
				Truth availablePath = planningTruth(any, a, p);
				String paths = goal.routes.stream().map(this::decision).filter(r -> !skipped(r, p) && completed(r, a, p) != Truth.TRUE)
					.map(r -> catalog.activities.get(r.activity) + " — Access: " + conditionEvidence(r.accessWhen, a, p)
						+ "; Suggested preparation: " + conditionEvidence(r.prepWhen, a, p)
						+ "; Context: " + conditionEvidence(r.when, a, p)).collect(Collectors.joining(" | "));
				requirements.add(new Requirement("ANY reviewed route's access and preparation", availablePath, paths, false, "ALTERNATIVES"));
				if (nearest == null && availablePath == Truth.FALSE)
				{
					for (String id : goal.routes)
					{
						Decision route = decision(id);
						if (skipped(route, p) || completed(route, a, p) == Truth.TRUE) { continue; }
						List<Requirement> steps = new ArrayList<>();
						addRequirements(route.accessWhen, a, p, steps, false, "ACCESS");
						addRequirements(route.prepWhen, a, p, steps, false, "PREPARATION");
						addRequirements(route.when, a, p, steps, false, "CONTEXT");
						Requirement missing = steps.stream().filter(r -> r.truth == Truth.FALSE).findFirst().orElse(null);
						if (missing != null)
						{
							nearest = catalog.activities.get(route.activity) + " route — " + missing.role + ": " + missing.label;
							break;
						}
					}
				}
			}
		}
		String status = complete ? "Satisfied by confirmed outcome or supported alternative"
			: blockedSkip ? "Blocked by a skipped action; skipping does not complete it"
			: nearest != null ? "Blocked: confirmed access or planning conditions are missing"
			: next != null ? "Available preparation action"
			: "Needs observation or confirmation; prerequisites remain unknown";
		String nextAction = complete ? "No further action for this target. Clear the goal or choose another."
			: next != null && !blockedSkip ? next.decision.title + ". This is the next supported action, not certification of raid access."
			: blockedSkip ? "Restore the skipped action or select a supported alternative activity."
			: "No acquisition route is confirmed available. Observe supported variants or confirm only the indicated planning target; check the source requirements.";

		return new GoalView(goal.title, status, requirements, nearest, nextAction, primary);
	}
	private Truth planningTruth(Condition c, AccountSnapshot a, Preferences p)
	{
		if (c.test == KnowledgeCatalog.Test.ALL) { return Truth.all(c.conditions.stream().map(x -> planningTruth(x, a, p)).toArray(Truth[]::new)); }
		if (c.test == KnowledgeCatalog.Test.ANY) { return Truth.any(c.conditions.stream().map(x -> planningTruth(x, a, p)).toArray(Truth[]::new)); }
		if (c.test == KnowledgeCatalog.Test.ITEMS) { return expected(ownership(c.key, a, p), c.value == null || c.value); }
		if (c.test == KnowledgeCatalog.Test.USABLE && truth(c, a, p) != Truth.TRUE) { return Truth.UNKNOWN; }
		return truth(c, a, p);
	}

	private void addRequirements(Condition c, AccountSnapshot a, Preferences p, List<Requirement> out, boolean skipped)
	{ addRequirements(c, a, p, out, skipped, "PREREQUISITE"); }
	private void addRequirements(Condition c, AccountSnapshot a, Preferences p, List<Requirement> out, boolean skipped, String role)
	{
		if (c.test == KnowledgeCatalog.Test.ALL)
		{ for (Condition child : c.conditions) { addRequirements(child, a, p, out, skipped, role); } return; }
		if (c.test == KnowledgeCatalog.Test.CONSTANT) { return; }
		Truth value = c.test == KnowledgeCatalog.Test.ITEMS ? ownership(c.key, a, p) : truth(c, a, p);
		if (c.test == KnowledgeCatalog.Test.USABLE && value != Truth.TRUE)
		{
			// Missing storage is unknown; a positively observed weapon's level can still be a confirmed blocker.
			value = Truth.UNKNOWN;
			Gear observed = items(c.key).stream().filter(g -> a.observedItem(g.id) == Truth.TRUE)
				.sorted(Comparator.comparingInt(g -> g.level)).findFirst().orElse(null);
			if (observed != null)
			{
				Condition level = new Condition(); level.test = KnowledgeCatalog.Test.LEVEL; level.key = observed.skill; level.minimum = observed.level;
				if (truth(level, a, p) != Truth.TRUE) { addRequirements(level, a, p, out, false, role); }
				if (observed.useWhen != null) { addRequirements(observed.useWhen, a, p, out, false, role); }
			}
		}
		out.add(new Requirement(label(c), value, conditionEvidence(c, a, p), skipped && value != Truth.TRUE, role));
	}

	public List<String> explainNoAdvice(AccountSnapshot a) { return explainNoAdvice(a, Preferences.EMPTY); }
	public List<String> explainNoAdvice(AccountSnapshot a, Preferences p)
	{
		if (!evaluate(a, p).isEmpty()) { return List.of(); }
		List<String> reasons = new ArrayList<>();
		if (!p.skipped.isEmpty()) { reasons.add("Skipped activities/actions are excluded; a skip never completes a target. Restore them in planning controls."); }
		List<Decision> ordered = new ArrayList<>(catalog.decisions);
		ordered.sort(Comparator.comparingInt(d -> goalRank(d, p.goal)));
		for (Decision d : ordered)
		{
			if (!"reviewed".equals(d.status) || skipped(d, p)) { continue; }
			Truth completed = completed(d, a, p);
			if (completed == Truth.TRUE) { continue; }
			Condition blocker = truth(d.accessWhen, a, p) != Truth.TRUE ? d.accessWhen
				: truth(d.prepWhen, a, p) != Truth.TRUE ? d.prepWhen
				: truth(d.when, a, p) != Truth.TRUE ? d.when : completed == Truth.UNKNOWN ? d.completeWhen : null;
			if (blocker != null) { reasons.add(d.title + ": " + conditionEvidence(blocker, a, p)); }
			if (reasons.size() >= 3) { break; }
		}
		if (reasons.isEmpty()) { reasons.add("Reviewed targets are complete or outside this slice. Unknown facts do not establish a missing upgrade."); }
		return List.copyOf(reasons);
	}

	public static final class Requirement
	{
		public final String label, evidence, role;
		public final Truth truth;
		public final boolean skipped;
		Requirement(String label, Truth truth, String evidence, boolean skipped)
		{ this(label, truth, evidence, skipped, "CONTEXT"); }
		Requirement(String label, Truth truth, String evidence, boolean skipped, String role)
		{ this.label = label; this.truth = truth; this.evidence = evidence; this.skipped = skipped; this.role = role; }
	}
	public static final class GoalView
	{
		public final String title, status, nearestMissing, nextAction;
		public final List<Requirement> requirements;
		public final Decision decision;
		GoalView(String title, String status, List<Requirement> requirements, String nearestMissing, String nextAction, Decision decision)
		{ this.title = title; this.status = status; this.requirements = List.copyOf(requirements); this.nearestMissing = nearestMissing; this.nextAction = nextAction; this.decision = decision; }
	}
	public static final class Recommendation
	{
		public final Decision decision;
		public final List<String> evidence;
		Recommendation(Decision decision, List<String> evidence) { this.decision = decision; this.evidence = List.copyOf(evidence); }
	}
}
