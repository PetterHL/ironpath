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
				evidence.add("Competing route: " + competing.decision.title + ". Order follows the common progression-2 value/effort table and stable ties; both remain reviewed choices.");
			}
			result.add(new Recommendation(d, evidence,recommendation.remainingEffort,recommendation.priority));
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
			RemainingEffort effort=RemainingEffort.evaluate(catalog.effortProfiles.get(d.pursuit.profile),d.pursuit.band,d.pursuit.reason,
				c->planningTruth(c,account,preferences),d.kind==KnowledgeCatalog.Kind.READ || d.kind==KnowledgeCatalog.Kind.ASSEMBLE);
			ValueEvidence.Importance importance=benefit(d,account,preferences)==KnowledgeCatalog.Benefit.MAJOR?ValueEvidence.Importance.KEY
				:benefit(d,account,preferences)==KnowledgeCatalog.Benefit.MODERATE?ValueEvidence.Importance.STRONG
				:benefit(d,account,preferences)==KnowledgeCatalog.Benefit.MINOR?ValueEvidence.Importance.CONTEXTUAL:ValueEvidence.Importance.UNCERTAIN;
			evidence.add("progression-2: " + importance.label + "; " + effort.band.label + " remaining effort; " + effort.suitability.label + ". " + effort.reason);
			candidates.add(new Recommendation(d, evidence.stream().distinct().collect(Collectors.toList()),effort,
				ProgressionPriority.of(importance,effort,false,false,false)));
		}
		candidates.sort(Comparator.comparing((Recommendation r) -> r.priority)
			.thenComparingInt(r -> goalRank(r.decision, preferences.goal))
			.thenComparing(r -> benefit(r.decision,account,preferences))
			.thenComparing(r -> r.remainingEffort.band)
			.thenComparingInt(r -> r.decision.completeWhen!=null && r.decision.completeWhen.test==KnowledgeCatalog.Test.LEVEL?r.decision.completeWhen.minimum:100)
			.thenComparing(r -> r.decision.id));
		return candidates;
	}

	/** Inspect possession-covered outcomes without inventing acquisition priority or usability. */
	public List<OwnedUpgrade> ownedUpgrades(AccountSnapshot account, Preferences preferences)
	{
		List<OwnedUpgrade> result=new ArrayList<>();
		for (KnowledgeCatalog.Upgrade upgrade : catalog.upgrades)
		{
			if (!"reviewed".equals(upgrade.status) || truth(upgrade.ownedWhen,account,preferences)!=Truth.TRUE) { continue; }
			List<String> applications=upgrade.applications.stream().filter(a->truth(a.when,account,preferences)==Truth.TRUE)
				.map(a->"Reviewed use: "+a.label).collect(Collectors.toList());
			List<String> roles=new ArrayList<>();
			Set<String> sources=new java.util.LinkedHashSet<>(upgrade.sources);
			for (ProgressionKnowledge.Relation role : catalog.progression.relations)
			{
				if (!role.subject.equals(upgrade.id)||!"reviewed".equals(role.status)) { continue; }
				Truth applicable=truth(role.when,account,preferences);
				if (applicable==Truth.FALSE||truth(role.noBenefitWhen,account,preferences)==Truth.TRUE) { continue; }
				roles.add((applicable==Truth.UNKNOWN ? "Conditional role: " : "Reviewed role: ")+role.reason+" "+role.limitation);
				for (ProgressionKnowledge.Alternative alternative:role.alternatives)
				{ if (truth(alternative.when,account,preferences)==Truth.TRUE) { roles.add("Adequate owned alternative: "+alternative.label); } }
				sources.addAll(role.sources);
			}
			result.add(new OwnedUpgrade(upgrade,conditionEvidence(upgrade.ownedWhen,account,preferences),applications,roles,List.copyOf(sources)));
		}
		result.sort(Comparator.comparing(u->u.upgrade.id));
		return List.copyOf(result);
	}

	/** Substantial outcomes, independent of route availability and selected goals. */
	public List<UpgradeView> upgrades(AccountSnapshot account, Preferences preferences)
	{ return upgrades(account, preferences, new CombatComparisonService(catalog).propose(account)); }

	public List<UpgradeView> upgrades(AccountSnapshot account, Preferences preferences, CombatSetup reference)
	{ return upgrades(account, preferences, reference, null); }

	public List<UpgradeView> upgrades(AccountSnapshot account, Preferences preferences, CombatSetup reference, PaceScenario pace)
	{ return upgrades(account, preferences, reference, new RangedComparisonService(catalog).propose(account), pace); }

	public List<UpgradeView> upgrades(AccountSnapshot account, Preferences preferences, CombatSetup reference, CombatSetup rangedReference, PaceScenario pace)
	{ return upgrades(account,preferences,reference,rangedReference,new MeleeComparisonService(catalog).propose(account,preferences),pace); }

	public List<UpgradeView> upgrades(AccountSnapshot account, Preferences preferences, CombatSetup reference, CombatSetup rangedReference, CombatSetup meleeReference, PaceScenario pace)
	{
		CombatComparisonService comparisonService = new CombatComparisonService(catalog);
		RangedComparisonService rangedService = new RangedComparisonService(catalog);
		MeleeComparisonService meleeService = new MeleeComparisonService(catalog);
		Map<String,CombatSetup> rangedContexts = new LinkedHashMap<>(), magicContexts = new LinkedHashMap<>(), meleeContexts = new LinkedHashMap<>();
		Map<String, Map<String, String>> referenceChoices = new LinkedHashMap<>();
		PoweredMagic.SLOTS.forEach(slot -> referenceChoices.put(slot, comparisonService.choices(slot, account)));
		referenceChoices.put("target", comparisonService.targetChoices());
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
			{
				Observation<Long> quantity = part.quantity == null ? Observation.unknown() : account.observedQuantity(part.itemId);
				Truth value = part.quantity == null ? planningTruth(part.when, account, preferences)
					: quantity.known() && quantity.value() >= part.quantity ? Truth.TRUE : Truth.UNKNOWN;
				String evidenceText = part.quantity == null ? conditionEvidence(part.when, account, preferences)
					: quantity.known() ? quantity.value() + " / " + part.quantity + " in observed containers; " + quantity.evidence()
					: "Quantity unknown; item presence or manual ownership does not establish the required count.";
				String labelText = part.label + (part.quantity == null ? "" : quantity.known()
					? " · " + quantity.value() + " / " + part.quantity + " observed" : " · Quantity unread");
				parts.add(new Requirement(labelText, value, evidenceText, false, part.optional ? "OPTIONAL ADDITION" : "COMPONENT / ASSEMBLY"));
			}
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
				shared.add(reward.title + " — " + reward.eligibilityLabel + ": " + truth(reward.eligibilityWhen, account, preferences) + ". "
					+ (truth(reward.ownedWhen, account, preferences) == Truth.UNKNOWN ? "Ownership/alternatives unconfirmed. " : "Planning gap confirmed. ") + reward.detail);
			}
			String label = questions.isEmpty() ? "Planning gap confirmed" : "Ownership unconfirmed — if you do not already own the outcome/alternative";
			if (routes.stream().noneMatch(r -> r.available)) { label += " · Plan ahead"; }
			if (equip.stream().anyMatch(r -> r.truth != Truth.TRUE)) { label += " · Needs equip levels"; }
			if (band == KnowledgeCatalog.Benefit.MINOR) { label += " · Contextual / consider deferring"; }
			boolean ranged = upgrade.comparison.equals("RANGED"), melee = upgrade.comparison.equals("MELEE");
			ComparisonResult comparison = melee ? meleeService.compare(account,preferences,meleeReference,upgrade) : ranged ? rangedService.compare(account, preferences, rangedReference, upgrade)
				: comparisonService.compare(account, preferences, reference, upgrade);
			List<ComparisonResult> contexts = new ArrayList<>();
			if (ranged)
			{
				for (String targetId : upgrade.contexts)
				{
					if (targetId.equals(rangedReference.target)) { continue; }
					CombatSetup contextual = rangedContexts.computeIfAbsent(targetId,id -> rangedService.propose(account,id));
					ComparisonResult resultForTarget = rangedService.compare(account,preferences,contextual,upgrade);
					contexts.add(resultForTarget);
				}
			}
			if (melee)
			{
				for (String targetId : upgrade.contexts)
				{
					if (targetId.equals(meleeReference.target)) { continue; }
					CombatSetup contextual=meleeContexts.computeIfAbsent(targetId,id->meleeService.propose(account,preferences,id));
					contexts.add(meleeService.compare(account,preferences,contextual,upgrade));
				}
			}
			if (!ranged && !melee && !upgrade.comparison.equals("NONE"))
			{
				for (String targetId : upgrade.contexts)
				{
					if (targetId.equals(reference.target)) { continue; }
					CombatSetup contextual=magicContexts.computeIfAbsent(targetId,id->comparisonService.propose(account,id));
					contexts.add(comparisonService.compare(account,preferences,contextual,upgrade));
				}
			}
			boolean contextualGain = contexts.stream().anyMatch(c -> c.available(ComparisonResult.Metric.THEORETICAL_DPS) && c.dpsGain() > 1e-10);
			String ranking = "Reviewed contextual " + band + " value; combat metric gap: " + comparison.reason;
			if (comparison.available(ComparisonResult.Metric.THEORETICAL_DPS))
			{
				double change = comparison.dpsGain(), relative = comparison.before.dps > 0 ? change / comparison.before.dps : 0;
				if (change <= 1e-10 && !contextualGain) { band = KnowledgeCatalog.Benefit.MINOR; label += " · No demonstrated sustained gain; defer"; }
				else if (change <= 1e-10) { label += " · Specialist gain in a reviewed encounter; neutral result is separate"; }
				else { band = relative >= 0.25 ? KnowledgeCatalog.Benefit.MAJOR : relative >= 0.05 ? KnowledgeCatalog.Benefit.MODERATE : KnowledgeCatalog.Benefit.MINOR; }
				ranking = String.format(java.util.Locale.ROOT, "Supported marginal DPS %+.3f on %s; %s value. %s", change, comparison.target.name, band,
					comparison.before.maxHit == comparison.after.maxHit ? "No max-hit breakpoint; accuracy is separate." : "Whole-loadout max-hit breakpoint.");
			}
			if (contextualGain)
			{ evidence.add("Supported specialist value in " + contexts.stream().filter(c -> c.available(ComparisonResult.Metric.THEORETICAL_DPS) && c.dpsGain() > 1e-10)
				.map(c -> c.target.name).collect(Collectors.joining(", ")) + ". Inspect the separate encounter results and complete inputs; gains are not added or compared as a universal score."); }
			evidence.add("Ranking: " + ranking + " Ordinary offence evidence only. Independent roles establish personal importance; pursuit status and effort remain separate. Context count carries no priority.");
			List<Requirement> path = new ArrayList<>();
			for (KnowledgeCatalog.PathNode node : upgrade.path)
			{ path.add(new Requirement(node.label + (node.quantity != null && account.observedQuantity(node.itemId).known()
				? " · " + account.observedQuantity(node.itemId).value() + " / " + node.quantity + " observed" : ""), pathTruth(node, account, preferences), node.detail + " "
				+ (node.quantity == null ? conditionEvidence(node.completeWhen, account, preferences) : quantityEvidence(node, account))
				+ dependencyDescription(node.dependencies), false, node.role)); }
			if (!upgrade.path.isEmpty())
			{
				Map<String, KnowledgeCatalog.PathNode> nodes = upgrade.path.stream().collect(Collectors.toMap(n -> n.id, n -> n));
				KnowledgeCatalog.PathNode nearest = nearestPath(upgrade.path.get(upgrade.path.size() - 1), nodes, account, preferences);
				if (nearest != null)
				{
					for (int i = 0; i < routes.size(); i++) { RouteView route = routes.get(i);
						routes.set(i, new RouteView(route.decision, route.status,
							(pathTruth(nearest, account, preferences) == Truth.UNKNOWN ? "Check needed: " : "Remaining: ")
							+ nearest.label + ". " + nearest.detail, route.requirements, route.available)); }
				}
			}
			if (!upgrade.path.isEmpty())
			{
				Map<String, KnowledgeCatalog.PathNode> nodes = upgrade.path.stream().collect(Collectors.toMap(n -> n.id, n -> n));
				for (KnowledgeCatalog.PathNode remaining : remainingPath(upgrade.path.get(upgrade.path.size()-1), nodes, account, preferences))
				{
					if (!remaining.id.equals(upgrade.path.get(upgrade.path.size()-1).id) && pathTruth(remaining, account, preferences) == Truth.UNKNOWN)
					{ questions.addAll(ownershipQuestions(remaining.completeWhen, account, preferences)); }
				}
			}
			ValueEvidence personalValue=ProgressionEvaluator.evaluate(catalog,upgrade,comparison,contexts,c -> truth(c,account,preferences));
			Set<String> valueSources=new java.util.TreeSet<>(upgrade.sources);
			for (ValueEvidence.Role role : personalValue.roles)
			{
				valueSources.addAll(role.sources);
				catalog.progression.contexts.stream().filter(c->c.id.equals(role.context)).forEach(c->valueSources.addAll(c.sources));
			}
			result.add(new UpgradeView(upgrade, label, context == null ? upgrade.valueReason : context.reason,
				band, evidence, applications, questions.stream().distinct().collect(Collectors.toList()), routes, equip, parts, shared, comparison, path,
				melee ? meleeService.referenceChoices(account) : ranged ? rangedService.referenceChoices(account) : referenceChoices, contexts, personalValue, new ArrayList<>(valueSources)));
		}
		List<RecommendationRanker.Candidate> candidates = new ArrayList<>();
		for (UpgradeView view : result)
		{ for (RouteView route : view.routes) { candidates.add(pursuitCandidate(view, route, account, preferences, pace)); } }
		Map<String, UpgradeView> byId = result.stream().collect(Collectors.toMap(u -> u.upgrade.id, u -> u));
		List<UpgradeView> ranked = new ArrayList<>(); Set<String> selected = new HashSet<>();
		List<PursuitAssessment> routeAssessments=new RecommendationRanker().rank(candidates);
		for (PursuitAssessment assessment : routeAssessments)
		{
			if (selected.add(assessment.id))
			{
				UpgradeView value=byId.get(assessment.id);
				ranked.add(new UpgradeView(value,assessment,routeAssessments.stream().filter(a->a.id.equals(assessment.id)).collect(Collectors.toList()),
					routeChoices(value,assessment,account,preferences)));
			}
		}
		List<ActivityOpportunity> activities=activityOpportunities(account,preferences,ranked);
		List<UpgradeView> explained=new ArrayList<>();
		for (UpgradeView upgrade : ranked)
		{
			List<String> trails=new ArrayList<>(); Set<String> visited=new HashSet<>();
			for (ValueEvidence.Role role : upgrade.valueEvidence.roles)
			{
				if (role.applicable != Truth.TRUE || role.covered || !visited.add(role.context)) { continue; }
				ProgressionKnowledge.Context context=catalog.progression.contexts.stream().filter(c->c.id.equals(role.context)).findFirst().orElseThrow();
				activities.stream().filter(a->a.activity.equals(context.activity)&&a.mode.equals(context.mode)).findFirst().ifPresent(activity -> {
					List<String> other=activity.usefulRewards.stream().filter(r->!r.equals(upgrade.upgrade.title)).distinct().collect(Collectors.toList());
					if (!other.isEmpty()) { trails.add("Direct role: " + role.reason + " Useful at " + activity.title + ", which still offers " + String.join(", ",other) + ". Separate rewards; no priority amplification."); }
				});
			}
			explained.add(new UpgradeView(upgrade,trails));
		}
		return List.copyOf(explained);
	}
	private List<UpgradePresentation.RouteChoice> routeChoices(UpgradeView value, PursuitAssessment assessment, AccountSnapshot account, Preferences preferences)
	{
		List<UpgradePresentation.RouteChoice> choices=new ArrayList<>();
		choices.add(new UpgradePresentation.RouteChoice(assessment.remainingEffort.method,null,true,RemainingEffort.Band.SHORT,
			assessment.remainingEffort.band,"Direct remaining route. " + assessment.remainingEffort.reason));
		ActivityEffortProfile profile=catalog.effortProfiles.get(assessment.remainingEffort.profile);
		for (ActivityEffortProfile.Detour detour : profile.detours)
		{
			KnowledgeCatalog.Upgrade preparation=catalog.upgrades.stream().filter(u->u.id.equals(detour.upgrade)).findFirst().orElseThrow();
			boolean owned=truth(preparation.ownedWhen,account,preferences)==Truth.TRUE;
			Decision route=decision(preparation.routes.get(0));
			if (!owned && skipped(route,preferences)) { continue; }
			RemainingEffort.Band band=owned || acquisitionStagesOwned(preparation,account,preferences)?RemainingEffort.Band.SHORT:route.pursuit.band;
			RemainingEffort prospective=RemainingEffort.evaluate(profile,decision(assessment.route).pursuit.band,
				"Conditional method after this preparation; other inputs are still checked.",
				c->truthWithPreparation(c,preparation,account,preferences),assessment.remainingEffort.acquisitionBypassed);
			choices.add(new UpgradePresentation.RouteChoice("Preparation alternative",preparation.id,owned,band,prospective.band,
				detour.reason + (owned?" Already owned: no repeat preparation grind.":" Allocate its inputs once; shared requirements are not charged twice. Check the preparation's own Path.")));
		}
		return List.copyOf(choices);
	}
	private Truth truthWithPreparation(Condition condition, KnowledgeCatalog.Upgrade preparation, AccountSnapshot account, Preferences preferences)
	{
		if (condition.test==KnowledgeCatalog.Test.ALL) { return Truth.all(condition.conditions.stream().map(c->truthWithPreparation(c,preparation,account,preferences)).toArray(Truth[]::new)); }
		if (condition.test==KnowledgeCatalog.Test.ANY) { return Truth.any(condition.conditions.stream().map(c->truthWithPreparation(c,preparation,account,preferences)).toArray(Truth[]::new)); }
		if (condition.test==KnowledgeCatalog.Test.USABLE && catalog.itemSets.get(condition.key).contains(preparation.itemId))
		{ return truth(preparation.equipWhen,account,preferences); }
		return planningTruth(condition,account,preferences);
	}

	private RecommendationRanker.Candidate pursuitCandidate(UpgradeView view, RouteView route,
		AccountSnapshot account, Preferences preferences, PaceScenario pace)
	{
		KnowledgeCatalog.EffortSpec spec = route.decision.pursuit;
		KnowledgeCatalog.EffortVariant variant = spec.variants.stream().filter(v -> truth(v.when, account, preferences) == Truth.TRUE).findFirst().orElse(null);
		PursuitAssessment.Mode mode = variant == null ? spec.mode : variant.mode;
		PursuitAssessment.Burden burden = variant == null ? spec.burden : variant.burden;
		PursuitAssessment.Tail tail = variant == null ? spec.tail : variant.tail;
		String rationale = (variant == null ? spec.reason : variant.reason) + " " + spec.missingMechanics;
		boolean declaredBypass=variant!=null && variant.bypassesAcquisition;
		boolean remainingAcquisition=!view.upgrade.path.isEmpty() && remainingPath(view.upgrade.path.get(view.upgrade.path.size()-1),
			view.upgrade.path.stream().collect(Collectors.toMap(n->n.id,n->n)),account,preferences).stream().anyMatch(n->n.acquisition);
		boolean bypassed=acquisitionStagesOwned(view.upgrade,account,preferences) || declaredBypass && !remainingAcquisition || mode==PursuitAssessment.Mode.READY;
		if (bypassed)
		{
			if (mode!=PursuitAssessment.Mode.READY) { mode = PursuitAssessment.Mode.BOUNDED; }
			burden = PursuitAssessment.Burden.LOW; tail = PursuitAssessment.Tail.ORDINARY;
			rationale = "Confirmed owned acquisition stages bypass the repeat grind; only remaining resources, skills and assembly are assessed. " + spec.missingMechanics;
		}
		List<Requirement> remaining = new ArrayList<>(); Set<String> steps = new HashSet<>();
		if (!view.upgrade.path.isEmpty())
		{
			Map<String, KnowledgeCatalog.PathNode> nodes = view.upgrade.path.stream().collect(Collectors.toMap(n -> n.id, n -> n));
			List<KnowledgeCatalog.PathNode> effective = remainingPath(view.upgrade.path.get(view.upgrade.path.size()-1), nodes, account, preferences);
			for (KnowledgeCatalog.PathNode node : effective)
			{
				steps.add(view.upgrade.id + "/" + node.id);
				boolean work = node.role.equals("ACTIVITY") || node.role.equals("COMPONENT") || node.role.equals("ASSEMBLY");
				// Output ownership is separate from the inputs needed to produce it. Missing outputs are real work;
				// unknown intermediate ownership may shorten that work and must remain a check.
				if (node.completeWhen.test == KnowledgeCatalog.Test.POSSESSION && work
					&& pathTruth(node, account, preferences) == Truth.FALSE) { continue; }
				if (node.id.equals(view.upgrade.path.get(view.upgrade.path.size()-1).id)) { continue; }
				addPathRequirement(node, account, preferences, remaining);
			}
			// Suggested preparation is not necessarily a hard dependency edge in the recipe.
			// It still matters while acquiring the reward, but owned components can remove that activity.
			if (!acquisitionStagesOwned(view.upgrade,account,preferences) && (variant == null || effective.stream().anyMatch(n -> n.role.equals("ACTIVITY"))))
			{
				addRequirements(route.decision.prepWhen, account, preferences, remaining, false, "SUGGESTED PREPARATION");
				addRequirements(route.decision.accessWhen, account, preferences, remaining, false, "ACCESS");
			}
			addRequirements(route.decision.when, account, preferences, remaining, false, "INPUTS / CONTEXT");
			for (String dependency : route.decision.requires)
			{ addRequirements(decision(dependency).completeWhen, account, preferences, remaining, skipped(decision(dependency), preferences), "PREREQUISITE"); }
			// A path may bypass activity access with an owned intermediate, but never equip conditions.
			remaining.addAll(view.equip);
		}
		else
		{
			remaining.addAll(route.requirements); remaining.addAll(view.equip);
			Set<String> outputs = route.decision.kind == KnowledgeCatalog.Kind.ACQUIRE ? ownershipKeys(route.decision.completeWhen) : Set.of();
			for (int i=0; i<view.upgrade.parts.size(); i++)
			{
				KnowledgeCatalog.Part part=view.upgrade.parts.get(i);
				if (part.optional) { continue; }
				if (part.when.test == KnowledgeCatalog.Test.POSSESSION && outputs.contains(part.when.key)
					&& planningTruth(part.when, account, preferences) == Truth.FALSE) { continue; }
				remaining.add(view.parts.get(i));
			}
			steps.add(route.decision.id);
		}
		if (!acquisitionStagesOwned(view.upgrade,account,preferences))
		{ addRequirements(view.upgrade.eligibilityWhen,account,preferences,remaining,false,"REWARD ELIGIBILITY"); }
		boolean unknown = !view.questions.isEmpty() || remaining.stream().anyMatch(r -> r.truth == Truth.UNKNOWN || r.skipped);
		boolean blocked = remaining.stream().anyMatch(r -> r.truth == Truth.FALSE && !r.role.equals("SUGGESTED PREPARATION") && !r.role.equals("PREPARATION"));
		unknown |= remaining.stream().anyMatch(r -> r.truth != Truth.TRUE && (r.role.equals("SUGGESTED PREPARATION") || r.role.equals("PREPARATION")));
		List<String> blockers = new ArrayList<>();
		if (!view.questions.isEmpty()) { blockers.add("Confirm ownership: " + catalog.factChoices().get(view.questions.get(0))); }
		remaining.stream().filter(r -> r.truth != Truth.TRUE || r.skipped).forEach(r -> blockers.add(r.role + ": " + r.label));
		RemainingEffort effort=RemainingEffort.evaluate(catalog.effortProfiles.get(spec.profile),variant==null?spec.band:variant.band,
			variant==null?spec.reason:variant.reason,c->planningTruth(c,account,preferences),bypassed);
		if (declaredBypass && !bypassed && variant.band==RemainingEffort.Band.UNKNOWN)
		{ effort=new RemainingEffort(RemainingEffort.Band.UNKNOWN,RemainingEffort.Suitability.UNKNOWN,spec.profile,effort.version,
			"Remaining secondary component route",variant.reason,"Completed component removed; secondary acquisition and assembly remain",
			spec.missingMechanics,effort.sources,false); }
		if (effort.suitability==RemainingEffort.Suitability.UNKNOWN || effort.suitability==RemainingEffort.Suitability.PREPARE)
		{
			unknown |= effort.suitability==RemainingEffort.Suitability.UNKNOWN;
			blocked |= effort.suitability==RemainingEffort.Suitability.PREPARE;
			blockers.add("METHOD: " + effort.reason);
		}
		rationale=effort.reason + " " + effort.gap;
		ComparisonResult comparison = view.comparison;
		boolean numeric = comparison.available(ComparisonResult.Metric.THEORETICAL_DPS);
		Double gain = numeric ? comparison.dpsGain() : null;
		boolean contextualGain = view.contextComparisons.stream().anyMatch(c -> c.available(ComparisonResult.Metric.THEORETICAL_DPS) && c.dpsGain() > 1e-10);
		boolean positive = view.valueEvidence.independentUtility || view.valueEvidence.importance.ordinal() < ValueEvidence.Importance.MARGINAL.ordinal() || numeric && gain > 1e-10 || contextualGain;
		if (!numeric && !view.valueEvidence.independentUtility && view.valueEvidence.importance == ValueEvidence.Importance.UNCERTAIN)
		{ unknown = true; blockers.add("Comparison input/model gap: " + comparison.gaps.getOrDefault(ComparisonResult.Metric.THEORETICAL_DPS, comparison.reason)); }
		String bucket = view.upgrade.role;
		if (numeric)
		{
			CombatSetup setup = comparison.beforeSetup;
			bucket += "/" + comparison.target.id + "/" + new java.util.TreeMap<>(setup.slots)
				+ "/" + new java.util.TreeMap<>(account.levels.value()) + "/" + setup.assumptionLabel() + "/" + setup.loadedAmmo + "/" + setup.onTask + "/" + comparison.modelVersion;
		}
		else { bucket += "/qualitative/" + view.upgrade.id; }
		if (contextualGain && numeric && gain <= 1e-10) { bucket += "/specialist/" + view.upgrade.id; gain = null; }
		EffortEstimate estimate = null;
		for (AcquisitionModel model : catalog.acquisitionModels.values().stream().sorted(Comparator.comparing(m -> m.id)).collect(Collectors.toList()))
		{
			if (!model.route.equals(route.decision.id)) { continue; }
			KnowledgeCatalog.PathNode component=view.upgrade.path.stream().filter(n->n.id.equals(model.node)).findFirst().orElse(null);
			KnowledgeCatalog.Condition componentWhen=component==null ? view.upgrade.parts.stream()
				.filter(part->model.node.equals(part.when.key)).map(part->part.when).findFirst().orElseThrow() : component.completeWhen;
			boolean stageRemaining=component==null ? planningTruth(componentWhen,account,preferences)!=Truth.TRUE
				:steps.contains(view.upgrade.id + "/" + model.node);
			boolean conditional=planningTruth(componentWhen,account,preferences)==Truth.UNKNOWN;
			int successes=model.requiredSuccesses;
			if(component!=null && component.quantity!=null)
			{
				com.ironpath.model.Observation<Long> count=account.observedQuantity(component.itemId);
				// Counts are lower bounds across storage, never an exact global remainder.
				if(count.known()) { successes=Math.max(0,model.requiredSuccesses-(int)Math.min(count.value(),model.requiredSuccesses)); }
				conditional=successes>0;
			}
			PaceScenario selected=null;
			if(stageRemaining && truth(route.decision.accessWhen,account,preferences)==Truth.TRUE)
			{
				selected=catalog.automaticPaces.values().stream().filter(p->p.support==AcquisitionModel.Support.REVIEWED
					&& p.activity.equals(model.activity) && p.mode.equals(model.mode)
					&& planningTruth(p.when,account,preferences)==Truth.TRUE)
					.sorted(Comparator.comparing(p->p.id)).map(AutomaticPaceProfile::scenario).findFirst().orElse(null);
				String methodKey=selected==null?effort.profile + "/" + effort.version + "/" + effort.method:selected.methodKey;
				if(pace!=null && pace.activity.equals(model.activity) && pace.mode.equals(model.mode)
					&& (pace.methodKey==null || pace.methodKey.equals(methodKey))
					&& (effort.suitability==RemainingEffort.Suitability.SUITABLE || effort.suitability==RemainingEffort.Suitability.WORKABLE))
				{ selected=pace; }
			}
			estimate=new AcquisitionEstimator().estimate(model,stageRemaining,
				"Owned component/intermediate removes this drop stage; remaining route work is separate.",selected,successes,conditional,
				selected==null?effort.profile + "/" + effort.version + "/" + effort.method:selected.methodKey);
			// Probability support is independent of reviewed qualitative effort. A withdrawn numerical
			// claim removes its odds/hours, rather than erasing the separately reviewed A0 route.
		}
		return new RecommendationRanker.Candidate(new RecommendationRanker.Candidate(view.upgrade.id, route.decision.id, route.decision.activity, spec.modeId,
			view.upgrade.rewardFamily, bucket, view.band, mode, burden, tail, rationale,
			unknown, blocked, preferences.goal != null && view.upgrade.goals.contains(preferences.goal),
			!unknown && !blocked, positive, 0, view.valueEvidence.independentUtility || numeric ? 2 : 1,
			-1, -1, gain, null, null, blockers,
			variant == null ? spec.allocations : variant.allocations, steps, estimate), view.valueEvidence,effort);
	}

	private Set<String> ownershipKeys(Condition condition)
	{
		if (condition.test == KnowledgeCatalog.Test.ALL || condition.test == KnowledgeCatalog.Test.ANY)
		{ return condition.conditions.stream().flatMap(c -> ownershipKeys(c).stream()).collect(Collectors.toSet()); }
		return condition.test == KnowledgeCatalog.Test.POSSESSION ? Set.of(condition.key) : Set.of();
	}
	private boolean acquisitionStagesOwned(KnowledgeCatalog.Upgrade upgrade, AccountSnapshot a, Preferences p)
	{
		List<KnowledgeCatalog.PathNode> acquisition = upgrade.path.stream().filter(n -> n.acquisition).collect(Collectors.toList());
		return !acquisition.isEmpty() && acquisition.stream().allMatch(n -> pathTruth(n,a,p) == Truth.TRUE);
	}

	private void addPathRequirement(KnowledgeCatalog.PathNode node, AccountSnapshot account, Preferences preferences, List<Requirement> remaining)
	{
		if (node.quantity != null)
		{ remaining.add(new Requirement(node.label, pathTruth(node, account, preferences), quantityEvidence(node, account), false, node.role)); }
		else { addRequirements(node.completeWhen, account, preferences, remaining, false, node.role); }
	}

	/** Only reachable remaining DAG work; owning an intermediate cuts its entire upstream branch. */
	private List<KnowledgeCatalog.PathNode> remainingPath(KnowledgeCatalog.PathNode node,
		Map<String, KnowledgeCatalog.PathNode> nodes, AccountSnapshot account, Preferences preferences)
	{
		if (pathTruth(node, account, preferences) == Truth.TRUE) { return List.of(); }
		List<KnowledgeCatalog.PathNode> result = new ArrayList<>(remainingDependency(node.dependencies, nodes, account, preferences));
		result.add(node); return result.stream().distinct().collect(Collectors.toList());
	}
	private List<KnowledgeCatalog.PathNode> remainingDependency(KnowledgeCatalog.Dependencies dependency,
		Map<String, KnowledgeCatalog.PathNode> nodes, AccountSnapshot account, Preferences preferences)
	{
		if (dependency == null) { return List.of(); }
		if (dependency.mode == KnowledgeCatalog.DependencyMode.NODE)
		{ return remainingPath(nodes.get(dependency.node), nodes, account, preferences); }
		if (dependency.mode == KnowledgeCatalog.DependencyMode.ALL)
		{ return dependency.children.stream().flatMap(d -> remainingDependency(d, nodes, account, preferences).stream()).distinct().collect(Collectors.toList()); }
		List<List<KnowledgeCatalog.PathNode>> branches = new ArrayList<>();
		for (KnowledgeCatalog.Dependencies child : dependency.children)
		{
			List<KnowledgeCatalog.PathNode> branch = remainingDependency(child, nodes, account, preferences);
			if (branch.isEmpty()) { return List.of(); }
			if (child.mode == KnowledgeCatalog.DependencyMode.NODE && nodes.get(child.node).alternativeOnly
				&& pathTruth(nodes.get(child.node), account, preferences) == Truth.FALSE) { continue; }
			branches.add(branch);
		}
		// An unconfirmed already-owned alternative is a check, not a cheap assumed acquisition.
		return branches.stream().filter(b -> b.size() == 1 && b.get(0).alternativeOnly).findFirst()
			.orElseGet(() -> branches.isEmpty() ? List.of() : branches.get(0));
	}

	public List<ActivityOpportunity> activityOpportunities(AccountSnapshot account, Preferences preferences, List<UpgradeView> upgrades)
	{
		Set<String> keys = new java.util.TreeSet<>();
		upgrades.forEach(u -> u.routes.forEach(r -> keys.add(r.decision.activity + "/" + r.decision.pursuit.modeId)));
		catalog.activityRewards.forEach(r -> keys.add(r.activity + "/" + r.mode));
		List<ActivityOpportunity> result = new ArrayList<>();
		for (String key : keys)
		{
			String activity = key.substring(0, key.indexOf('/')), mode = key.substring(key.indexOf('/')+1);
			if (preferences.skipped.contains(activity)) { continue; }
			Set<String> families = new java.util.TreeSet<>(), sources = new java.util.TreeSet<>(), allocations = new java.util.TreeSet<>();
			Set<Integer> itemIds = new java.util.TreeSet<>();
			List<String> rewards = new ArrayList<>(), uncertain = new ArrayList<>(), exclusions = new ArrayList<>();
			boolean available = false, longTail = true; KnowledgeCatalog.Benefit best = KnowledgeCatalog.Benefit.PLANNING;
			ProgressionPriority.Priority activityPriority=ProgressionPriority.Priority.CONDITIONAL;
			for (UpgradeView view : upgrades)
			{
				RouteView route = view.routes.stream().filter(r -> r.decision.activity.equals(activity) && r.decision.pursuit.modeId.equals(mode)).findFirst().orElse(null);
				if (route == null) { continue; }
				KnowledgeCatalog.EffortVariant variant=route.decision.pursuit.variants.stream().filter(v -> truth(v.when, account, preferences) == Truth.TRUE).findFirst().orElse(null);
				if (route.decision.kind == KnowledgeCatalog.Kind.ASSEMBLE || variant != null && variant.bypassesAcquisition
					|| acquisitionStagesOwned(view.upgrade,account,preferences))
				{ exclusions.add(view.upgrade.title + ": owned components removed this acquisition stage; not counted as a missing activity reward."); continue; }
				if (view.valueEvidence.importance == ValueEvidence.Importance.MARGINAL || view.pursuit.category == PursuitAssessment.Category.DEFER)
				{ exclusions.add(view.upgrade.title + ": small/no demonstrated gain or dominated."); continue; }
				if (!view.questions.isEmpty()) { uncertain.add(view.upgrade.title + ": ownership unconfirmed; not counted as missing."); continue; }
				Truth eligibility=truth(view.upgrade.eligibilityWhen,account,preferences);
				if (eligibility != Truth.TRUE) { uncertain.add(view.upgrade.title + ": reward eligibility " + eligibility + "; independent of activity access."); continue; }
				families.add(view.upgrade.rewardFamily); rewards.add(view.upgrade.title); sources.addAll(view.upgrade.sources); itemIds.add(view.upgrade.itemId);
				PursuitAssessment routeAssessment=view.routeAssessments.stream().filter(a->a.route.equals(route.decision.id)).findFirst().orElseThrow();
				if (routeAssessment.priority.ordinal()<activityPriority.ordinal()) { activityPriority=routeAssessment.priority; }
				longTail &= view.pursuit.tail == PursuitAssessment.Tail.LONG;
				allocations.addAll(view.pursuit.allocations); available |= truth(route.decision.accessWhen,account,preferences)==Truth.TRUE && truth(route.decision.prepWhen,account,preferences)==Truth.TRUE;
				KnowledgeCatalog.Benefit value=view.valueEvidence.importance == ValueEvidence.Importance.KEY ? KnowledgeCatalog.Benefit.MAJOR : view.valueEvidence.importance == ValueEvidence.Importance.STRONG ? KnowledgeCatalog.Benefit.MODERATE : KnowledgeCatalog.Benefit.MINOR;
				if (value.ordinal() < best.ordinal()) { best = value; }
			}
			for (KnowledgeCatalog.ActivityReward reward : catalog.activityRewards)
			{
				if (!reward.activity.equals(activity) || !reward.mode.equals(mode) || truth(reward.when, account, preferences) != Truth.TRUE) { continue; }
				Truth owned = truth(reward.ownedWhen, account, preferences);
				if (owned == Truth.TRUE) { exclusions.add(reward.title + ": owned scroll/unlocked benefit; not a missing reward."); continue; }
				if (owned == Truth.UNKNOWN) { uncertain.add(reward.title + ": unlock/scroll ownership unknown; not counted as missing."); continue; }
				if (reward.requiredQuantity!=null)
				{
					Observation<Long> quantity=account.observedQuantity(reward.itemId);
					if (quantity.known() && quantity.value()>=reward.requiredQuantity)
					{ exclusions.add(reward.title + ": observed resources bypass collection; confirm allocation before assembly."); continue; }
					if (truth(reward.resourceWhen,account,preferences)!=Truth.FALSE)
					{ uncertain.add(reward.title + ": check remaining quantity/allocation; presence or a lower bound does not prove the full requirement."); continue; }
				}
				Truth eligibility=truth(reward.eligibilityWhen,account,preferences);
				if (eligibility != Truth.TRUE) { uncertain.add(reward.title + ": reward eligibility " + eligibility + "; not counted as eligible."); continue; }
				RemainingEffort rewardEffort=RemainingEffort.evaluate(catalog.effortProfiles.get(reward.profile),reward.band,
					"Remaining shared reward work; independent of other drops.",c->planningTruth(c,account,preferences),false);
				ProgressionPriority.Priority rewardPriority=ProgressionPriority.of(reward.benefit==KnowledgeCatalog.Benefit.MAJOR?ValueEvidence.Importance.KEY
					:reward.benefit==KnowledgeCatalog.Benefit.MODERATE?ValueEvidence.Importance.STRONG:ValueEvidence.Importance.CONTEXTUAL,rewardEffort,false,false,false);
				if (rewardPriority.ordinal()<activityPriority.ordinal()) { activityPriority=rewardPriority; }
				families.add(reward.family); rewards.add(reward.title); sources.addAll(reward.sources); sources.addAll(rewardEffort.sources); itemIds.add(reward.itemId);
				available |= truth(reward.accessWhen, account, preferences) == Truth.TRUE && truth(reward.prepWhen, account, preferences) == Truth.TRUE;
				longTail &= reward.tail != PursuitAssessment.Tail.ORDINARY;
				if (reward.benefit.ordinal() < best.ordinal()) { best = reward.benefit; }
			}
			if (families.isEmpty() && uncertain.isEmpty()) { continue; }
			if (activity.equals("chambers-of-xeric"))
			{ exclusions.add("Ancestral pieces are one dependent armour family, not three independent benefits."); }
			ProgressionKnowledge.Context context=catalog.progression.contexts.stream().filter(c->c.activity.equals(activity)&&c.mode.equals(mode)).findFirst().orElse(null);
			Truth access=context == null ? Truth.UNKNOWN : truth(context.accessWhen,account,preferences);
			Truth method=context == null ? Truth.UNKNOWN : truth(context.methodWhen,account,preferences);
			List<String> preparation=new ArrayList<>();
			if (context != null)
			{
				available=access==Truth.TRUE && method==Truth.TRUE;
				exclusions.addAll(context.resourceConflicts); sources.addAll(context.sources);
				for (ProgressionKnowledge.Preparation option : context.preparation)
				{
					Truth supported=truth(option.when,account,preferences);
					preparation.add(option.constraint + ": " + option.label + " — " + supported);
					if (option.constraint==ProgressionKnowledge.Constraint.METHOD_CAPABILITY && supported!=Truth.TRUE) { available=false; }
				}
			}

			result.add(new ActivityOpportunity(activity, mode, catalog.activities.get(activity), available, best,
				new ArrayList<>(families), rewards, uncertain, exclusions, new ArrayList<>(allocations), new ArrayList<>(sources), new ArrayList<>(itemIds), longTail, access, method, preparation,activityPriority));
		}
		result.sort(Comparator.comparing((ActivityOpportunity a) -> a.priority).thenComparing(a->a.id));
		return List.copyOf(result);
	}

	private String dependencyDescription(KnowledgeCatalog.Dependencies dependency)
	{ return dependency == null ? "" : dependency.mode == KnowledgeCatalog.DependencyMode.NODE ? " " + dependency.node
		: " · " + dependency.mode + " prerequisite branch: [" + dependency.children.stream().map(this::dependencyDescription).collect(Collectors.joining(", ")) + "]"; }
	private KnowledgeCatalog.PathNode nearestPath(KnowledgeCatalog.PathNode node, Map<String, KnowledgeCatalog.PathNode> nodes,
		AccountSnapshot a, Preferences p)
	{
		if (pathTruth(node, a, p) == Truth.TRUE) { return null; }
		KnowledgeCatalog.PathNode next = nearestDependency(node.dependencies, nodes, a, p);
		return next == null ? node : next;
	}
	private KnowledgeCatalog.PathNode nearestDependency(KnowledgeCatalog.Dependencies dependency,
		Map<String, KnowledgeCatalog.PathNode> nodes, AccountSnapshot a, Preferences p)
	{
		if (dependency == null) { return null; }
		if (dependency.mode == KnowledgeCatalog.DependencyMode.NODE) { return nearestPath(nodes.get(dependency.node), nodes, a, p); }
		if (dependency.mode == KnowledgeCatalog.DependencyMode.ALL)
		{ return dependency.children.stream().map(d -> nearestDependency(d, nodes, a, p)).filter(java.util.Objects::nonNull).findFirst().orElse(null); }
		if (dependency.children.stream().anyMatch(d -> nearestDependency(d, nodes, a, p) == null)) { return null; }
		// Prefer an already-owned intermediate check over redoing its upstream acquisition.
		KnowledgeCatalog.Dependencies branch = dependency.children.stream().filter(d -> d.mode == KnowledgeCatalog.DependencyMode.NODE
			&& nodes.get(d.node).dependencies == null && pathTruth(nodes.get(d.node), a, p) == Truth.UNKNOWN).findFirst().orElse(dependency.children.get(0));
		return nearestDependency(branch, nodes, a, p);
	}
	private Truth pathTruth(KnowledgeCatalog.PathNode node, AccountSnapshot a, Preferences p)
	{
		if (node.quantity == null) { return planningTruth(node.completeWhen, a, p); }
		Observation<Long> count = a.observedQuantity(node.itemId);
		if (count.known() && count.value() >= node.quantity) { return Truth.TRUE; }
		// Explicit global absence may establish fresh acquisition work; a container zero cannot.
		return node.completeWhen.test==KnowledgeCatalog.Test.POSSESSION && planningTruth(node.completeWhen,a,p)==Truth.FALSE ? Truth.FALSE : Truth.UNKNOWN;
	}
	private String quantityEvidence(KnowledgeCatalog.PathNode node, AccountSnapshot a)
	{
		Observation<Long> count = a.observedQuantity(node.itemId);
		return count.known() ? count.value() + " / " + node.quantity + " (largest observed container count); " + count.evidence()
			: "Quantity unknown; ownership does not establish the required amount.";
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
		public final List<Requirement> path;
		public final ComparisonResult comparison;
		public final List<ComparisonResult> contextComparisons;
		public final Map<String, Map<String, String>> referenceChoices;
		public final PursuitAssessment pursuit;
		public final ValueEvidence valueEvidence;
		public final List<String> indirectReasons, evidenceSources;
		public final UpgradeCard card;
		public final UpgradePresentation presentation;
		public final List<PursuitAssessment> routeAssessments;
		UpgradeView(KnowledgeCatalog.Upgrade upgrade, String label, String value, KnowledgeCatalog.Benefit band,
			List<String> evidence, List<String> applications, List<String> questions, List<RouteView> routes,
			List<Requirement> equip, List<Requirement> parts, List<String> shared, ComparisonResult comparison, List<Requirement> path, Map<String, Map<String, String>> referenceChoices, List<ComparisonResult> contexts, ValueEvidence valueEvidence, List<String> evidenceSources)
		{
			this.upgrade = upgrade; this.label = label; this.value = value; this.band = band;
			this.evidence = List.copyOf(evidence); this.applications = List.copyOf(applications); this.questions = List.copyOf(questions);
			this.routes = List.copyOf(routes); this.equip = List.copyOf(equip); this.parts = List.copyOf(parts); this.shared = List.copyOf(shared);
			this.comparison = comparison; this.path = List.copyOf(path); this.referenceChoices = Map.copyOf(referenceChoices); this.pursuit = null;
			contextComparisons = List.copyOf(contexts); this.valueEvidence=valueEvidence; this.evidenceSources=List.copyOf(evidenceSources); indirectReasons=List.of(); card=null; presentation=null; routeAssessments=List.of();
		}
		private UpgradeView(UpgradeView value, PursuitAssessment assessment, List<PursuitAssessment> assessments, List<UpgradePresentation.RouteChoice> choices)
		{
			upgrade=value.upgrade; label=value.label; this.value=value.value; band=value.band;
			evidence=value.evidence; applications=value.applications; questions=value.questions;
			routes=value.routes; equip=value.equip; parts=value.parts; shared=value.shared; comparison=value.comparison;
			path=value.path; referenceChoices=value.referenceChoices; pursuit=assessment;
			contextComparisons=value.contextComparisons; valueEvidence=value.valueEvidence; indirectReasons=value.indirectReasons;
			java.util.Set<String> sources=new java.util.TreeSet<>(value.evidenceSources); sources.addAll(assessment.remainingEffort.sources); evidenceSources=List.copyOf(sources);
			routeAssessments=List.copyOf(assessments); presentation=new UpgradePresentation(this,choices); card=new UpgradeCard(this);
		}

		private UpgradeView(UpgradeView value, List<String> trails)
		{
			upgrade=value.upgrade; label=value.label; this.value=value.value; band=value.band; evidence=value.evidence;
			applications=value.applications; questions=value.questions; routes=value.routes; equip=value.equip; parts=value.parts;
			shared=value.shared; comparison=value.comparison; path=value.path; referenceChoices=value.referenceChoices;
			pursuit=value.pursuit; contextComparisons=value.contextComparisons; valueEvidence=value.valueEvidence; indirectReasons=List.copyOf(trails); evidenceSources=value.evidenceSources; card=value.card; presentation=value.presentation; routeAssessments=value.routeAssessments;
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
	public Truth truthForPlanningFact(AccountSnapshot a, Preferences p, String key) { return Truth.of(fact(a, p, key)); }
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
		public final RemainingEffort remainingEffort;
		public final ProgressionPriority.Priority priority;
		Recommendation(Decision decision,List<String> evidence,RemainingEffort effort,ProgressionPriority.Priority priority)
		{ this.decision=decision; this.evidence=List.copyOf(evidence); remainingEffort=effort; this.priority=priority; }
	}
}
