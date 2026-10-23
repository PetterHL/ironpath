package com.ironpath.advisor;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Bounded, bundled PvM decisions with a small set of observation predicates. */
public final class KnowledgeCatalog
{
	/** Reviewed display names across observed gear and comparison families. */
	public Map<Integer,String> itemNames()
	{
		Map<Integer,String> result=new java.util.TreeMap<>();
		gear.values().stream().flatMap(List::stream).forEach(g->result.put(g.id,g.label));
		comparison.items.forEach(i->result.put(i.id,i.label));
		ranged.items.forEach(i->result.put(i.id,i.label));
		melee.items.forEach(i->result.put(i.id,i.label));
		upgrades.forEach(u->result.putIfAbsent(u.itemId,u.title));
		return Map.copyOf(result);
	}

	public enum Kind { READ, TRAIN, ASSEMBLE, REVIEW, PLAN, ACQUIRE }
	public enum Effort { SMALL, TRAINING, GRIND }
	public enum Benefit { MAJOR, MODERATE, MINOR, PLANNING }
	public enum Test { ALL, ANY, LEVEL, ITEMS, EQUIPPED, RIGOUR, AUGURY, FACT, POSSESSION, USABLE, CONSTANT }
	private static final Set<String> SKILLS = Set.of("ATTACK", "STRENGTH", "DEFENCE", "RANGED",
		"MAGIC", "PRAYER", "HITPOINTS", "AGILITY", "CRAFTING", "SLAYER", "HERBLORE", "RUNECRAFT", "CONSTRUCTION", "FISHING", "COOKING", "FIREMAKING", "WOODCUTTING", "MINING", "SMITHING", "FLETCHING", "THIEVING", "HUNTER", "FARMING");
	// This is the finite reader contract, not arbitrary client varbit/quest lookup from JSON.
	private static final Set<String> FACTS = Set.of("rigour", "augury", "song-of-elves", "regicide",
		"beneath-cursed-sands", "perilous-moons", "deadeye", "mystic-vigour", "final-dawn", "frozen-door",
		"smoke-task", "smoke-protection", "nex-entry", "nex-team", "zaryte-context", "zaryte-shards", "doom-preparation", "treads-tears", "desert-treasure-ii", "monkey-madness-ii", "duke-defeated", "peer-ring-icons", "cox-preparation", "toa-preparation", "monkey-form",
		"multi-combat-method", "secrets-of-north", "muspah-preparation", "doom-delve-two", "doom-delve-three", "doom-delve-four",
		"blood-moon-rises", "dragon-slayer-ii", "leviathan-defeated", "colosseum-preparation", "zilyana-entry", "knight-waves", "kraken-task", "fremennik-isles", "fremennik-exiles",
		"barbarian-training", "barbarian-hasta", "monkey-madness-i", "vardorvis-defeated", "whisperer-defeated", "hydra-task", "while-guthix-sleeps", "arclight-infusion", "duradel-notes", "yama-entry", "arachnid-task", "cerberus-task", "sins-of-father", "vyre-method");
	public final Map<String, Goal> goals;
	public final Map<String, String> activities;
	private final Map<String, String> ownershipLabels;
	public final String version;
	public final LocalDate reviewed;
	public final List<Decision> decisions;
	public final List<Upgrade> upgrades;
	public final ComparisonSpec comparison;
	public final RangedSpec ranged;
	public final MeleeSpec melee;
	public final Map<String, AcquisitionModel> acquisitionModels;
	public final List<ActivityReward> activityRewards;
	public final ProgressionKnowledge progression;
	public final Map<String, ActivityEffortProfile> effortProfiles;
	public final Map<String, AutomaticPaceProfile> automaticPaces;
	public final Map<String, Source> sources;
	final Map<String, List<Gear>> gear;
	final Map<String, List<Integer>> itemSets;

	private KnowledgeCatalog(Document document)
	{
		if (document.schemaVersion != 14 || document.decisions == null
			|| document.decisions.isEmpty())
		{
			throw new IllegalArgumentException("Unsupported or incomplete PvM catalogue");
		}
		version = require(document.knowledgeVersion);
		reviewed = LocalDate.parse(require(document.reviewed));
		if (document.sources == null || document.gear == null || document.itemSets == null)
		{ throw new IllegalArgumentException("Missing evidence"); }
		Set<String> sourceIds = new HashSet<>();
		for (Source source : document.sources)
		{
			if (source == null || !require(source.id).matches("[a-z][a-z0-9-]*") || !sourceIds.add(source.id))
			{ throw new IllegalArgumentException("Invalid or duplicate source ID"); }
			boolean wiki = require(source.url).matches("https://oldschool\\.runescape\\.wiki/w/[A-Za-z0-9_/%()-]+\\?oldid=[0-9]+");
			boolean official = source.url.matches("https://secure\\.runescape\\.com/m=news/[a-z0-9-]+\\?oldschool=1");
			boolean paceEvidence=source.url.matches("https://osrsmethods\\.com/methods/(completing-the-corrupted-gauntlet|killing-zulrah|killing-demonic-gorillas|killing-tormented-demons|killing-phantom-muspah-twisted-bow)");
			boolean api = source.url.matches("https://github\\.com/runelite/runelite/blob/[a-f0-9]{40}/runelite-api/src/main/java/net/runelite/api/gameval/ItemID\\.java");
			boolean equipmentFacts=source.url.matches("https://github\\.com/weirdgloop/osrs-dps-calc/blob/[a-f0-9]{40}/cdn/json/equipment\\.json");
			boolean minionFacts=source.id.equals("n3-minion-reference") && source.url.matches("https://github\\.com/weirdgloop/osrs-dps-calc/blob/[a-f0-9]{40}/cdn/json/monsters\\.json")
				&& source.items.isEmpty() && source.models.isEmpty() && source.decisions.isEmpty() && source.routes.isEmpty();
			if(source.id.equals("n3-minion-reference") && !minionFacts) { throw new IllegalArgumentException("Exact minion source required"); }
			if (!wiki && !api && !official && !paceEvidence && !equipmentFacts && !minionFacts)
			{
				throw new IllegalArgumentException("Unapproved source URL");
			}
			require(source.author); require(source.type); require(source.reuseBasis); require(source.licence);
			LocalDate accessed = LocalDate.parse(require(source.accessed));
			LocalDate review = LocalDate.parse(require(source.reviewed));
			if (accessed.isAfter(review) || review.isAfter(reviewed))
			{ throw new IllegalArgumentException("Inconsistent source dates"); }
			if (paceEvidence ? !require(source.revision).matches("sha256:[a-f0-9]{64}") : official ? require(source.revision).isBlank() : !(wiki ? source.url.endsWith("?oldid=" + require(source.revision)) : source.url.contains("/blob/" + require(source.revision) + "/")))
			{ throw new IllegalArgumentException("Source revision mismatch"); }
		}
		sources = Map.copyOf(document.sources.stream().collect(Collectors.toMap(s -> s.id, s -> s)));
		Set<String> ids = new HashSet<>();

		for (Decision decision : document.decisions)
		{
			if (decision == null || !require(decision.id).matches("[a-z][a-z0-9-]*") || !ids.add(decision.id)) { throw new IllegalArgumentException("Invalid or duplicate decision ID"); }
			if (decision.kind == null || decision.effortBand == null || decision.benefitBand == null)
			{ throw new IllegalArgumentException("Unsupported decision"); }
			require(decision.title); require(decision.why); require(decision.alternative);
			require(decision.tradeoff); require(decision.effort); require(decision.access);
			require(decision.completion); require(decision.applicability); require(decision.family);
			require(decision.activity); require(decision.outcome); require(decision.benefitReason); require(decision.preparation);
			if (decision.goals == null || decision.notes == null
				|| decision.contextBenefits == null
				|| decision.accessWhen == null || decision.prepWhen == null)
			{ throw new IllegalArgumentException("Missing activity, preparation or benefit contract"); }
			if (!Set.of("reviewed", "candidate").contains(require(decision.status))) { throw new IllegalArgumentException("Unknown review status"); }
			if (LocalDate.parse(require(decision.reviewed)).isAfter(reviewed))
			{ throw new IllegalArgumentException("Inconsistent decision review date"); }
			if (decision.sources == null || decision.sources.isEmpty() || !sources.keySet().containsAll(decision.sources))
			{
				throw new IllegalArgumentException("Missing decision source");
			}
			decision.sources = List.copyOf(decision.sources);
			if (decision.requires == null || new HashSet<>(decision.requires).size() != decision.requires.size())
			{ throw new IllegalArgumentException("Invalid hard dependencies"); }
			decision.requires = List.copyOf(decision.requires);
			if (decision.when == null || (decision.repeatable == (decision.completeWhen != null)))
			{ throw new IllegalArgumentException("Missing applicability or completion condition"); }
		}
		for (Source source : sources.values())
		{
			if (source.decisions == null || source.decisions.isEmpty() && source.items.isEmpty() && source.models.isEmpty() && source.routes.isEmpty() && source.contexts.isEmpty() && source.activityContexts.isEmpty() && source.relations.isEmpty() && source.profiles.isEmpty() && source.paces.isEmpty() || !ids.containsAll(source.decisions)
				|| new HashSet<>(source.decisions).size() != source.decisions.size())
			{
				throw new IllegalArgumentException("Invalid affected decisions");
			}
			for (String id : source.decisions)
			{
				if (document.decisions.stream().noneMatch(d -> d.id.equals(id) && d.sources.contains(source.id)))
				{
					throw new IllegalArgumentException("Inconsistent source reference");
				}
			}
			source.decisions = List.copyOf(source.decisions);
		}
		// Source-to-decision links must agree in both directions.
		for (Decision decision : document.decisions)
		{
			for (String source : decision.sources)
			{
				if (!sources.get(source).decisions.contains(decision.id))
				{ throw new IllegalArgumentException("Inconsistent decision source"); }
			}
			if (!ids.containsAll(decision.requires)) { throw new IllegalArgumentException("Unknown hard dependency"); }
		}
		Map<String, Decision> byId = document.decisions.stream().collect(Collectors.toMap(d -> d.id, d -> d));
		Set<String> visited = new HashSet<>();
		for (String id : ids) { visit(id, byId, new HashSet<>(), visited); }
		if (document.gear.isEmpty())
		{ throw new IllegalArgumentException("Missing gear"); }
		for (String group : document.gear.keySet()) { if (!require(group).matches("[A-Za-z][A-Za-z0-9]*")) { throw new IllegalArgumentException("Invalid gear group"); } }
		Set<Integer> itemIds = new HashSet<>();
		for (List<Gear> group : document.gear.values())
		{
			if (group == null || group.isEmpty()) { throw new IllegalArgumentException("Empty gear group"); }
			for (Gear item : group)
			{
				if (item == null || item.id <= 0 || !itemIds.add(item.id) || item.level < 1 || item.level > 99
					|| !SKILLS.contains(item.skill))
				{
					throw new IllegalArgumentException("Invalid gear evidence");
				}
				require(item.label);
				if (item.sources == null || item.sources.isEmpty() || !sources.keySet().containsAll(item.sources))
				{ throw new IllegalArgumentException("Missing item evidence"); }
				item.sources = List.copyOf(item.sources);
			}
		}
		gear = Map.copyOf(document.gear.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> List.copyOf(e.getValue()))));
		for (Map.Entry<String, List<Integer>> entry : document.itemSets.entrySet())
		{
			if (!entry.getKey().matches("[A-Za-z][A-Za-z0-9]*")) { throw new IllegalArgumentException("Invalid set ID"); }
			List<Integer> set = entry.getValue();
			if (set == null || set.isEmpty() || new HashSet<>(set).size() != set.size() || !itemIds.containsAll(set))
			{ throw new IllegalArgumentException("Invalid item set reference"); }
		}
		itemSets = Map.copyOf(document.itemSets.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> List.copyOf(e.getValue()))));
		if (document.activities == null || document.goals == null || document.goals.isEmpty()
			|| document.ownershipLabels == null)
		{ throw new IllegalArgumentException("Missing planning choices"); }
		document.activities.forEach((id, label) -> { stableId(id); require(label); });

		activities = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(document.activities));
		if (!itemSets.keySet().containsAll(document.ownershipLabels.keySet()))
		{ throw new IllegalArgumentException("Unknown ownership set"); }
		document.ownershipLabels.values().forEach(KnowledgeCatalog::require);
		ownershipLabels = Map.copyOf(document.ownershipLabels);
		Set<String> goalIds = new HashSet<>();
		for (Goal goal : document.goals)
		{
			if (goal == null) { throw new IllegalArgumentException("Missing goal"); }
			stableId(goal.id); require(goal.title);
			if (!Set.of("ACCESS", "PREPARATION", "USE", "INPUTS", "CONTEXT").contains(require(goal.whenRole)))
			{ throw new IllegalArgumentException("Unsupported goal requirement role"); }
			if (!goalIds.add(goal.id) || !ids.contains(goal.decisionId) || goal.routes == null || goal.routes.isEmpty()
				|| !ids.containsAll(goal.routes) || new HashSet<>(goal.routes).size() != goal.routes.size()
				|| goal.requires == null || !ids.containsAll(goal.requires)
				|| new HashSet<>(goal.requires).size() != goal.requires.size())
			{ throw new IllegalArgumentException("Invalid goal routes"); }
			Decision evidence = byId.get(goal.decisionId);
			validate(goal.when, evidence, 0); validate(goal.completeWhen, evidence, 0);
			goal.routes = List.copyOf(goal.routes); goal.requires = List.copyOf(goal.requires);
		}
		goals = java.util.Collections.unmodifiableMap(document.goals.stream().collect(Collectors.toMap(g -> g.id, g -> g,
			(a, b) -> a, java.util.LinkedHashMap::new)));
		for (Decision decision : document.decisions)
		{
			if (!activities.containsKey(decision.activity) || !goalIds.containsAll(decision.goals))
			{ throw new IllegalArgumentException("Unknown activity or benefit goal"); }
			decision.goals = List.copyOf(decision.goals); decision.notes = List.copyOf(decision.notes);
			validate(decision.when, decision, 0); validate(decision.accessWhen, decision, 0); validate(decision.prepWhen, decision, 0);
			if (decision.completeWhen != null) { validate(decision.completeWhen, decision, 0); }
			for (ContextBenefit benefit : decision.contextBenefits)
			{
				if (benefit == null || benefit.band == null) { throw new IllegalArgumentException("Unsupported benefit"); }
				require(benefit.reason); validate(benefit.when, decision, 0);
			}
			decision.contextBenefits = List.copyOf(decision.contextBenefits);
		}
		for (List<Gear> group : gear.values())
		{
			for (Gear item : group) { if (item.useWhen != null)
				{
					validateGearCondition(item.useWhen, 0, new HashSet<>());
					Decision evidence = new Decision(); evidence.sources = List.copyOf(sources.keySet());
					validate(item.useWhen, evidence, 0);
				} }
		}
		decisions = List.copyOf(document.decisions);
		if (document.upgrades == null || document.upgrades.isEmpty())
		{ throw new IllegalArgumentException("Missing upgrade outcomes"); }
		Set<String> upgradeIds = new HashSet<>();
		for (Upgrade upgrade : document.upgrades)
		{
			if (upgrade == null) { throw new IllegalArgumentException("Missing upgrade outcome"); }
			stableId(upgrade.id);
			if (!upgradeIds.add(upgrade.id) || upgrade.benefitBand == null || !itemIds.contains(upgrade.itemId)
				|| !Set.of("NONE", "POWERED_MAGIC", "RANGED", "MELEE").contains(require(upgrade.comparison))
				|| upgrade.routes == null || upgrade.routes.isEmpty()
				|| !ids.containsAll(upgrade.routes) || new HashSet<>(upgrade.routes).size() != upgrade.routes.size()
				|| upgrade.goals == null || new HashSet<>(upgrade.goals).size() != upgrade.goals.size()
				|| !goalIds.containsAll(upgrade.goals) || upgrade.unknownSets == null
				|| !ownershipLabels.keySet().containsAll(upgrade.unknownSets))
			{ throw new IllegalArgumentException("Invalid upgrade references"); }
			require(upgrade.title); require(upgrade.valueReason); require(upgrade.alternatives);
			if (upgrade.browseStyle!=null && !Set.of("Melee","Ranged","Magic").contains(upgrade.browseStyle)
				|| upgrade.browseSlot!=null && !PoweredMagic.SLOTS.contains(upgrade.browseSlot))
			{ throw new IllegalArgumentException("Invalid discovery metadata"); }
			require(upgrade.headline); require(upgrade.effortShort);
			if (!Set.of("reviewed", "candidate").contains(require(upgrade.status))
				|| LocalDate.parse(require(upgrade.reviewed)).isAfter(reviewed)) { throw new IllegalArgumentException("Unreviewed upgrade"); }
			require(upgrade.remainingWork); require(upgrade.limitation);
			evidence(upgrade.sources);
			Decision contract = new Decision(); contract.id = upgrade.id; contract.sources = upgrade.sources;
			validate(upgrade.when, contract, 0); validate(upgrade.ownedWhen, contract, 0);
			validate(upgrade.equipWhen, contract, 0); validate(upgrade.eligibilityWhen, contract, 0);
			for (String route : upgrade.routes)
			{ if (!byId.get(route).sources.containsAll(upgrade.sources)) { throw new IllegalArgumentException("Route lacks upgrade evidence"); } }
			if (upgrade.applications == null || upgrade.applications.isEmpty()
				|| upgrade.parts == null || upgrade.shared == null
				|| upgrade.contextBenefits == null)
			{ throw new IllegalArgumentException("Invalid contextual value contract"); }
			Set<String> applications = new HashSet<>();
			for (Application application : upgrade.applications)
			{
				if (application == null) { throw new IllegalArgumentException("Missing application"); }
				stableId(application.id); require(application.text); require(application.label);
				if (!applications.add(application.id)) { throw new IllegalArgumentException("Duplicate encounter application"); }
				validate(application.when, contract, 0);
			}
			for (Part part : upgrade.parts)
			{ if (part == null) { throw new IllegalArgumentException("Missing component"); } require(part.label); validate(part.when, contract, 0);
				if (part.quantity != null && (part.quantity < 1 || part.itemId == null || !itemIds.contains(part.itemId)))
				{ throw new IllegalArgumentException("Invalid component quantity"); } }
			Map<String, PathNode> path = upgrade.path.stream().collect(Collectors.toMap(n -> require(n.id), n -> n));
			for (PathNode node : upgrade.path)
			{
				stableId(node.id); require(node.label); require(node.detail);
				if (!Set.of("ACCESS", "PREPARATION", "COMPONENT", "ASSEMBLY", "ACTIVITY").contains(node.role))
				{ throw new IllegalArgumentException("Invalid path role"); }
				validate(node.completeWhen, contract, 0); validateDependencies(node.dependencies, path.keySet(), 0);
				if (node.quantity != null && (node.quantity < 1 || node.itemId == null || !itemIds.contains(node.itemId)))
				{ throw new IllegalArgumentException("Invalid path quantity"); }
				if ((node.batchItemId==null)!=(node.batchSize==null) || node.batchItemId!=null &&
					(!itemIds.contains(node.batchItemId) || node.quantity==null || node.batchSize<1 || node.quantity%node.batchSize!=0 || node.batchItemId.equals(node.itemId)))
				{ throw new IllegalArgumentException("Invalid remaining-batch quantity"); }
				if (node.batchItemId!=null) { gear.values().stream().flatMap(List::stream).filter(g->g.id==node.batchItemId).forEach(g->requireItemEvidence(g,contract)); }
				if (node.quantity != null) { gear.values().stream().flatMap(List::stream).filter(g -> g.id == node.itemId).forEach(g -> requireItemEvidence(g,contract)); }
			}
			for (String node : path.keySet()) { visitPath(node, path, new HashSet<>(), new HashSet<>()); }
			for (SharedReward reward : upgrade.shared)
			{
				if (reward == null) { throw new IllegalArgumentException("Missing shared reward"); }
				require(reward.title); require(reward.detail); require(reward.eligibilityLabel); evidence(reward.sources);
				if (!upgrade.sources.containsAll(reward.sources) || reward.eligibilityWhen == null || reward.retiredMinDelve != null)
				{ throw new IllegalArgumentException("Invalid shared reward evidence"); }
				validate(reward.when, contract, 0); validate(reward.ownedWhen, contract, 0);
			validate(reward.eligibilityWhen, contract, 0);
			}
			for (ContextBenefit benefit : upgrade.contextBenefits)
			{
				if (benefit == null || benefit.band == null) { throw new IllegalArgumentException("Unknown value band"); }
				require(benefit.reason); validate(benefit.when, contract, 0);
			}
		}
		upgrades = List.copyOf(document.upgrades);
		comparison = document.comparison;
		melee = document.melee;
		if (melee==null || !MeleeModel.VERSION.equals(melee.modelVersion) || melee.items==null || melee.items.isEmpty() || melee.unsupportedWeapons==null)
		{ throw new IllegalArgumentException("Missing melee contract"); }
		evidence(melee.sources);
		Set<Integer> meleeIds=new HashSet<>();
		for (MeleeItem item : melee.items)
		{
			if (item==null || !itemIds.contains(item.id) || !meleeIds.add(item.id) || !PoweredMagic.SLOTS.contains(require(item.slot))
				|| item.stab==null || item.slash==null || item.crush==null || item.strength==null || item.ticks==null
				|| item.stab < -100 || item.stab > 300 || item.slash < -100 || item.slash > 300 || item.crush < -100 || item.crush > 300
				|| item.strength < -100 || item.strength > 300 || item.equipFact!=null && !FACTS.contains(item.equipFact) || item.requirements==null || item.styles==null || item.styles.stream().anyMatch(java.util.Objects::isNull)
				|| item.family==null || item.effect==null || new HashSet<>(item.styles).size()!=item.styles.size()
				|| item.minimumCombat!=null && (item.minimumCombat<3 || item.minimumCombat>126)
				|| item.family!=MeleeModel.Weapon.ORDINARY && !item.slot.equals("weapon")
				|| item.effect==MeleeEffects.Gear.SLAYER && !item.slot.equals("head")
				|| (item.effect==MeleeEffects.Gear.SALVE || item.effect==MeleeEffects.Gear.SALVE_ENCHANTED) && !item.slot.equals("neck")
				|| item.effect==MeleeEffects.Gear.INQUISITOR_HEAD && !item.slot.equals("head")
				|| item.effect==MeleeEffects.Gear.INQUISITOR_BODY && !item.slot.equals("body")
				|| item.effect==MeleeEffects.Gear.INQUISITOR_LEGS && !item.slot.equals("legs")
				|| item.slot.equals("weapon") && (item.styles.isEmpty() || item.ticks<2 || item.ticks>10)
				|| !item.slot.equals("weapon") && (!item.styles.isEmpty() || item.ticks!=0 || item.twoHanded))
			{ throw new IllegalArgumentException("Invalid melee item/style/compatibility contract"); }
			require(item.label); evidence(item.sources);
			item.requirements.forEach((skill,level)->{ if (!SKILLS.contains(skill) || level==null || level<1 || level>99) { throw new IllegalArgumentException("Invalid melee equip requirement"); } });
		}
		melee.unsupportedWeapons.forEach((id,label)->{ if (!itemIds.contains(id) || meleeIds.contains(id)) { throw new IllegalArgumentException("Invalid unsupported melee family"); } require(label); });
		ranged = document.ranged;
		if (ranged == null || !RangedModel.VERSION.equals(ranged.modelVersion) || ranged.items == null || ranged.items.isEmpty()
			|| ranged.candidateAmmo == null || ranged.unsupportedWeapons==null) { throw new IllegalArgumentException("Missing ranged contract"); }
		evidence(ranged.sources);
		Set<Integer> rangedIds = new HashSet<>();
		for (RangedItem item : ranged.items)
		{
			if (item == null || !itemIds.contains(item.id) || !rangedIds.add(item.id) || !PoweredMagic.SLOTS.contains(require(item.slot))
				|| item.attack < -100 || item.attack > 300 || item.strength < -100 || item.strength > 300 || item.ammo == null || item.setPart == null
				|| item.crystalWeight < 0 || item.crystalWeight > 3 || item.ammoTier < 0 || item.ammoTier > 10
				|| item.weapon != null && (!item.slot.equals("weapon") || item.ticks < 2 || item.ticks > 10 || item.defenceType == null || item.maxAmmoTier < 0 || item.maxAmmoTier > 10)
				|| item.weapon == null && item.slot.equals("weapon")
				|| item.seeking && (item.ammo != RangedItem.Ammo.ARROW || item.slot.equals("weapon"))
				|| item.chargedQuiver && !item.slot.equals("cape") || item.boltEffect && item.ammo != RangedItem.Ammo.BOLT
				|| item.ammo != RangedItem.Ammo.NONE && !item.slot.equals("ammo") || item.requirements == null)
			{ throw new IllegalArgumentException("Invalid ranged item/compatibility contract"); }
			require(item.label); evidence(item.sources);
			if (item.weapon != null && item.twoHanded != !(item.weapon == RangedItem.Weapon.CROSSBOW || item.weapon == RangedItem.Weapon.DRAGON_HUNTER))
			{ throw new IllegalArgumentException("Invalid ranged hands"); }
			if (item.weapon != null && item.defenceType != (item.weapon == RangedItem.Weapon.TOXIC || item.weapon == RangedItem.Weapon.ROSEWOOD
				? RangedItem.DefenceType.LIGHT : item.weapon == RangedItem.Weapon.CROSSBOW || item.weapon == RangedItem.Weapon.DRAGON_HUNTER ? RangedItem.DefenceType.HEAVY : RangedItem.DefenceType.STANDARD))
			{ throw new IllegalArgumentException("Invalid ranged defence type"); }
			item.requirements.forEach((skill,level) -> { if (!SKILLS.contains(skill) || level < 1 || level > 99) { throw new IllegalArgumentException("Invalid ranged equip requirement"); } });
		}
		ranged.unsupportedWeapons.forEach((id,label)->{if(!itemIds.contains(id)||rangedIds.contains(id)){throw new IllegalArgumentException("Invalid unsupported ranged family");}require(label);});
		for (Upgrade upgrade : upgrades)
		{ if ("reviewed".equals(upgrade.status) && "Ranged".equals(upgrade.browseStyle) && "weapon".equals(upgrade.browseSlot)
			&& !rangedIds.contains(upgrade.itemId) && !ranged.unsupportedWeapons.containsKey(upgrade.itemId)
			&& ownedVariants(upgrade.ownedWhen).stream().noneMatch(id->rangedIds.contains(id)||ranged.unsupportedWeapons.containsKey(id)))
			{ throw new IllegalArgumentException("Ranged promotion lacks numeric support or an explicit owned-family gap: " + upgrade.id); } }
		for (Map.Entry<String,Integer> entry : ranged.candidateAmmo.entrySet())
		{
			RangedItem.Weapon family = RangedItem.Weapon.valueOf(entry.getKey());
			RangedItem ammo = ranged.items.stream().filter(i -> i.id == entry.getValue()).findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown candidate ammunition"));
			if (ranged.items.stream().filter(i -> i.weapon == family).anyMatch(w -> !RangedModel.compatible(w,ammo)))
			{ throw new IllegalArgumentException("Invalid supplied candidate ammunition"); }
		}
		if (comparison == null || !"powered-magic-3".equals(comparison.modelVersion)
			|| comparison.targets == null || comparison.targets.isEmpty()
			|| comparison.items == null || comparison.items.isEmpty())
		{ throw new IllegalArgumentException("Unsupported comparison contract"); }
		Set<String> targetIds = new HashSet<>();
		for (TargetProfile target : comparison.targets)
		{
			stableId(target.id); require(target.name); evidence(target.sources);
			if (!targetIds.add(target.id) || !Set.of("STANDARD", "TOA").contains(target.variant)
				|| target.scalingSupported && !target.analytical && target.npcId < 1 || target.magicLevel < 0 || target.magicLevel > 10000
				|| target.analytical && (target.npcId != 0 || !target.variant.equals("STANDARD") || !target.scalingSupported
					|| target.defenceLevel == null || target.defenceLevel != 0 || target.magicLevel != 0 || target.magicDefence != 0)
				|| target.flatArmour!=null && target.flatArmour!=0 && target.flatArmour!=-2
				|| target.magicDefence < -64 || target.magicDefence > 10000
				|| target.defenceLevel != null && (target.defenceLevel < 0 || target.defenceLevel > 10000)
				|| target.magicAttack != null && (target.magicAttack < 0 || target.magicAttack > 10000)
				|| target.rangedLight != null && (target.rangedLight < -64 || target.rangedLight > 10000)
				|| target.rangedStandard != null && (target.rangedStandard < -64 || target.rangedStandard > 10000)
				|| target.rangedHeavy != null && (target.rangedHeavy < -64 || target.rangedHeavy > 10000)
				|| target.rangedStandard != null && (target.rangedLight == null || target.rangedHeavy == null || target.defenceLevel == null || target.magicAttack == null)
				|| target.analytical && (target.xerician || target.dragon || target.slayer || target.rangedLight == null || target.rangedStandard == null || target.rangedHeavy == null
					|| target.rangedLight != 0 || target.rangedStandard != 0 || target.rangedHeavy != 0 || target.magicAttack == null || target.magicAttack != 0)
				|| target.meleeStab != null && (target.meleeStab < -64 || target.meleeStab > 10000 || target.meleeSlash==null || target.meleeCrush==null || target.defenceLevel==null)
				|| target.meleeSlash != null && (target.meleeStab==null || target.meleeSlash < -64 || target.meleeSlash > 10000)
				|| target.meleeCrush != null && (target.meleeStab==null || target.meleeCrush < -64 || target.meleeCrush > 10000)
				|| target.analytical && (target.meleeStab==null || target.meleeStab!=0 || target.meleeSlash!=0 || target.meleeCrush!=0)
				|| target.size!=null && (target.size<1 || target.size>64)
				|| target.demonbanePercent!=null && (target.demonbanePercent<0 || target.demonbanePercent>1000)
				|| target.meleeScenario && (target.meleeStab==null || target.size==null || target.scalingSupported)
				|| target.variant.equals("TOA") && target.scalingSupported)
			{ throw new IllegalArgumentException("Unsupported target profile/scaling"); }
		}

		if (!targetIds.contains("neutral-zero-defence")) { throw new IllegalArgumentException("Missing default analytical reference"); }
		evidence(comparison.sources);
		Set<Integer> statIds = new HashSet<>();
		for (CombatItem item : comparison.items)
		{
			if (item == null) { throw new IllegalArgumentException("Missing stat item"); }
			if (!itemIds.contains(item.id) || !statIds.add(item.id) || !PoweredMagic.SLOTS.contains(require(item.slot))
				|| item.magicAttack < -100 || item.magicAttack > 300 || item.damagePercent < 0 || item.damagePercent > 100
				|| item.damagePermille != null && (item.damagePermille < 0 || item.damagePermille > 1000)
				|| (item.slot.equals("weapon") ? !Set.of("SEAS", "SWAMP", "SANG", "EYE", "SHADOW").contains(item.weapon == null ? "" : item.weapon) : item.weapon != null))
			{ throw new IllegalArgumentException("Invalid supported slot metadata"); }
			require(item.label); evidence(item.sources);
			if (item.brimstoneEffect && (!item.slot.equals("ring") || item.unsupportedAccuracy
				|| !item.sources.contains("r1-brimstone-ring")))
			{ throw new IllegalArgumentException("Brimstone effect requires reviewed ring mechanics"); }
			if (item.twoHanded != "SHADOW".equals(item.weapon)) { throw new IllegalArgumentException("Invalid hand compatibility"); }
			if (item.requirements != null) { item.requirements.forEach((skill, level) -> {
				if (!SKILLS.contains(skill) || level < 1 || level > 99) { throw new IllegalArgumentException("Invalid equip requirement"); }
			}); }
		}
		if (document.acquisitions == null || document.activityRewards == null) { throw new IllegalArgumentException("Missing pursuit contracts"); }
		java.util.Map<String, AcquisitionModel> models = new java.util.LinkedHashMap<>();
		Set<String> acquisitionRoutes=new HashSet<>();
		for (AcquisitionSpec model : document.acquisitions)
		{
			if (model == null) { throw new IllegalArgumentException("Missing acquisition model"); }
			stableId(model.id); stableId(model.version); stableId(model.mode); stableId(model.rewardFamily);
			if (models.containsKey(model.id) || !acquisitionRoutes.add(model.route) || !activities.containsKey(model.activity) || !ids.contains(model.route)
				|| model.support == null || model.mechanic == null || model.rolls<1 || model.rolls>10
				|| model.requiredSuccesses<1 || model.requiredSuccesses>10
				|| model.mechanic!=AcquisitionModel.Mechanic.INDEPENDENT_ROLLS && model.rolls!=1
				|| model.mechanic!=AcquisitionModel.Mechanic.REPEATED_SUCCESSES && model.requiredSuccesses!=1
				|| model.denominator <= 0 || model.numerator < 0
				|| model.numerator > model.denominator || model.denominator > 1_000_000_000L
				|| model.assumptions == null || model.assumptions.isEmpty() || model.exclusions == null || model.exclusions.isEmpty())
			{ throw new IllegalArgumentException("Invalid acquisition mechanic/scope"); }
			require(model.recipient); require(model.trialUnit); require(model.reason);
			if (LocalDate.parse(require(model.reviewed)).isAfter(reviewed)) { throw new IllegalArgumentException("Invalid acquisition review date"); }
			evidence(model.sources);
			if (!byId.get(model.route).activity.equals(model.activity) || !byId.get(model.route).sources.containsAll(model.sources)
				|| upgrades.stream().noneMatch(u -> u.routes.contains(model.route) && (u.path.stream().anyMatch(n -> n.id.equals(model.node)) || u.path.isEmpty() && u.parts.stream().anyMatch(part->part.when.test==Test.POSSESSION && model.node.equals(part.when.key)))))
			{ throw new IllegalArgumentException("Acquisition is not linked to its path stage"); }
			if(model.mechanic==AcquisitionModel.Mechanic.REPEATED_SUCCESSES && upgrades.stream()
				.filter(u->u.routes.contains(model.route)).flatMap(u->u.path.stream())
				.noneMatch(n->n.id.equals(model.node) && n.quantity!=null && n.quantity==model.requiredSuccesses))
			{ throw new IllegalArgumentException("Repeated reward model does not match its required path count"); }
			for (String source : model.sources) { if (!sources.get(source).acquisitions.contains(model.id)) { throw new IllegalArgumentException("Missing acquisition source dependency"); } }
			models.put(model.id, new AcquisitionModel(model));
		}
		acquisitionModels = Map.copyOf(models);
		if (document.effortProfiles==null || document.effortProfiles.isEmpty()) { throw new IllegalArgumentException("Missing activity effort profiles"); }
		Map<String,ActivityEffortProfile> profiles=new java.util.LinkedHashMap<>();
		for (ActivityEffortProfile profile : document.effortProfiles)
		{
			stableId(profile.id); stableId(profile.version); stableId(profile.mode); require(profile.scope); require(profile.gap);
			if (profiles.putIfAbsent(profile.id,profile)!=null || !activities.containsKey(profile.activity)
				|| LocalDate.parse(require(profile.reviewed)).isAfter(reviewed) || profile.methods==null || profile.methods.isEmpty() || profile.detours==null)
			{ throw new IllegalArgumentException("Invalid activity effort profile"); }
			evidence(profile.sources); Decision contract=new Decision(); contract.sources=profile.sources;
			Set<String> methodIds=new HashSet<>();
			for (ActivityEffortProfile.Method method : profile.methods)
			{
				stableId(method.id); require(method.label); require(method.limitation);
				if (!methodIds.add(method.id) || method.minimumBand==null || method.minimumBand==RemainingEffort.Band.UNKNOWN
					|| method.suitability!=RemainingEffort.Suitability.SUITABLE && method.suitability!=RemainingEffort.Suitability.WORKABLE)
				{ throw new IllegalArgumentException("Invalid effort method"); }
				validate(method.when,contract,0);
			}
			for (ActivityEffortProfile.Detour detour : profile.detours)
			{
				require(detour.reason);
				if (upgrades.stream().noneMatch(u->u.id.equals(detour.upgrade)&&u.status.equals("reviewed")))
				{ throw new IllegalArgumentException("Unreviewed preparation detour"); }
			}
			for (String source : profile.sources) { if (!sources.get(source).profiles.contains(profile.id)) { throw new IllegalArgumentException("Missing profile source dependency"); } }
			profile.methods=List.copyOf(profile.methods); profile.sources=List.copyOf(profile.sources); profile.detours=List.copyOf(profile.detours);
		}
		effortProfiles=Map.copyOf(profiles);
		if(document.automaticPaces==null) { throw new IllegalArgumentException("Missing automatic pace inventory"); }
		Map<String,AutomaticPaceProfile> paces=new java.util.LinkedHashMap<>();
		for(AutomaticPaceProfile pace:document.automaticPaces)
		{
			stableId(pace.id); stableId(pace.version); stableId(pace.mode); require(pace.scope); require(pace.methodLabel);
			if(paces.putIfAbsent(pace.id,pace)!=null || !activities.containsKey(pace.activity) || pace.support==null
				|| !Double.isFinite(pace.minimumPerHour) || !Double.isFinite(pace.maximumPerHour)
				|| pace.minimumPerHour<=0 || pace.maximumPerHour<pace.minimumPerHour
				|| LocalDate.parse(require(pace.reviewed)).isAfter(reviewed)
				|| pace.assumptions==null || pace.assumptions.isEmpty() || pace.exclusions==null || pace.exclusions.isEmpty())
			{ throw new IllegalArgumentException("Invalid automatic repeat-hour scenario"); }
			evidence(pace.sources); Decision contract=new Decision(); contract.sources=pace.sources;
			validate(pace.when,contract,0);
			for(String source:pace.sources)
			{ if(!sources.get(source).paces.contains(pace.id)) { throw new IllegalArgumentException("Missing pace source dependency"); } }
			pace.sources=List.copyOf(pace.sources); pace.assumptions=List.copyOf(pace.assumptions); pace.exclusions=List.copyOf(pace.exclusions);
		}
		automaticPaces=Map.copyOf(paces);
		for (Decision decision : decisions)
		{
			if (!"reviewed".equals(decision.status)) { continue; }
			EffortSpec effort=decision.pursuit;
			ActivityEffortProfile profile=effort==null?null:profiles.get(effort.profile);
			if (profile==null || effort.band==null || !profile.activity.equals(decision.activity) || !profile.mode.equals(effort.modeId))
			{ throw new IllegalArgumentException("Active decision lacks reviewed value/effort contract: " + decision.id); }
			validateEffort(effort.mode,effort.burden,effort.tail,effort.modeId,effort.reason,effort.missingMechanics,effort.allocations);
			evidence(effort.sources);
			if (effort.variants==null || !decision.sources.containsAll(effort.sources)) { throw new IllegalArgumentException("Incomplete decision effort contract"); }
			for (String source : effort.sources) { if (!sources.get(source).efforts.contains(decision.id)) { throw new IllegalArgumentException("Missing decision effort dependency"); } }
		}
		for (Upgrade upgrade : upgrades)
		{
			stableId(upgrade.rewardFamily); stableId(upgrade.role);
			if (upgrade.contexts == null || upgrade.contexts.isEmpty() || new HashSet<>(upgrade.contexts).size() != upgrade.contexts.size())
			{ throw new IllegalArgumentException("Missing reviewed comparison contexts"); }
			upgrade.contexts.forEach(KnowledgeCatalog::stableId);
			if (upgrade.contexts.stream().anyMatch(context -> !targetIds.contains(context)
				&& !(upgrade.comparison.equals("NONE") && document.activityContexts.stream().anyMatch(c -> c.id.equals(context)))))
			{ throw new IllegalArgumentException("Unknown reviewed comparison context"); }
			for (String route : upgrade.routes)
			{
				EffortSpec effort=byId.get(route).pursuit;
				if (effort == null) { throw new IllegalArgumentException("Missing remaining-effort metadata"); }
				ActivityEffortProfile profile=profiles.get(effort.profile);
				if (profile==null || effort.band==null || !profile.activity.equals(byId.get(route).activity) || !profile.mode.equals(effort.modeId))
				{ throw new IllegalArgumentException("Route lacks reviewed effort profile"); }
				validateEffort(effort.mode, effort.burden, effort.tail, effort.modeId, effort.reason, effort.missingMechanics, effort.allocations);
				evidence(effort.sources);
				if (!byId.get(route).sources.containsAll(effort.sources) || effort.variants == null) { throw new IllegalArgumentException("Missing effort evidence"); }
				for (String source : effort.sources) { if (!sources.get(source).efforts.contains(route)) { throw new IllegalArgumentException("Missing effort source dependency"); } }
				Decision contract=new Decision(); contract.sources=byId.get(route).sources;
				for (EffortVariant variant : effort.variants)
				{
					if (variant == null) { throw new IllegalArgumentException("Missing remaining-work variant"); }
					validate(variant.when, contract, 0);
					validateEffort(variant.mode, variant.burden, variant.tail, effort.modeId, variant.reason, effort.missingMechanics, variant.allocations);
					if (variant.band==null) { throw new IllegalArgumentException("Variant lacks remaining effort band"); }
				}
			}
		}
		Set<String> rewardIds=new HashSet<>();
		for (ActivityReward reward : document.activityRewards)
		{
			if (reward == null) { throw new IllegalArgumentException("Missing shared-activity reward"); }
			stableId(reward.id); stableId(reward.family); stableId(reward.mode); require(reward.title);
			ActivityEffortProfile rewardProfile=profiles.get(reward.profile);
			if (rewardProfile==null || reward.band==null || !rewardProfile.activity.equals(reward.activity) || !rewardProfile.mode.equals(reward.mode))
			{ throw new IllegalArgumentException("Shared reward lacks effort contract"); }
			if (!rewardIds.add(reward.id) || !activities.containsKey(reward.activity) || reward.benefit == null || reward.tail == null || !itemIds.contains(reward.itemId))
			{ throw new IllegalArgumentException("Invalid activity reward"); }
			evidence(reward.sources); Decision contract=new Decision(); contract.sources=reward.sources;
			validate(reward.when, contract, 0); validate(reward.ownedWhen, contract, 0);
			validate(reward.eligibilityWhen, contract, 0);
			validate(reward.accessWhen, contract, 0); validate(reward.prepWhen, contract, 0);
			if (reward.requiredQuantity != null)
			{ if (reward.requiredQuantity<1) { throw new IllegalArgumentException("Invalid reward resource quantity"); } validate(reward.resourceWhen,contract,0); }
			else if (reward.resourceWhen != null) { throw new IllegalArgumentException("Missing resource quantity"); }
			gear.values().stream().flatMap(List::stream).filter(g -> g.id == reward.itemId).forEach(g -> requireItemEvidence(g, contract));
			for (String source : reward.sources) { if (!sources.get(source).rewards.contains(reward.id)) { throw new IllegalArgumentException("Missing reward source dependency"); } }
		}
		activityRewards=List.copyOf(document.activityRewards);
		for (Source source : sources.values())
		{
			if (!itemIds.containsAll(source.items) || !ids.containsAll(source.routes)
				|| !targetIds.containsAll(source.contexts) || source.models.stream().anyMatch(m -> !m.equals(comparison.modelVersion) && !m.equals(ranged.modelVersion) && !m.equals(melee.modelVersion))
				|| source.paces.stream().anyMatch(id->!paces.containsKey(id)||!paces.get(id).sources.contains(source.id))
				|| source.acquisitions.stream().anyMatch(id -> !models.containsKey(id) || !models.get(id).sources.contains(source.id))
				|| source.efforts.stream().anyMatch(id -> !ids.contains(id) || byId.get(id).pursuit == null || !byId.get(id).pursuit.sources.contains(source.id))
				|| source.rewards.stream().anyMatch(id -> activityRewards.stream().noneMatch(r -> r.id.equals(id) && r.sources.contains(source.id))))
			{ throw new IllegalArgumentException("Unknown source dependency"); }
			if (!source.models.isEmpty() && !comparison.sources.contains(source.id) && !ranged.sources.contains(source.id) && !melee.sources.contains(source.id)
				|| source.routes.stream().anyMatch(id -> !byId.get(id).sources.contains(source.id)))
			{ throw new IllegalArgumentException("Inconsistent source model/route dependency"); }
			for (int id : source.items) { if (gear.values().stream().flatMap(List::stream).noneMatch(g -> g.id == id && g.sources.contains(source.id))
				&& comparison.items.stream().noneMatch(i -> i.id == id && i.sources.contains(source.id))
				&& ranged.items.stream().noneMatch(i -> i.id == id && i.sources.contains(source.id))
				&& melee.items.stream().noneMatch(i -> i.id == id && i.sources.contains(source.id)))
				{ throw new IllegalArgumentException("Inconsistent source item dependency"); } }
			for (String id : source.contexts) { if (comparison.targets.stream().noneMatch(t -> t.id.equals(id) && t.sources.contains(source.id)))
				{ throw new IllegalArgumentException("Inconsistent source context dependency"); } }
		}

		if (document.activityContexts == null || document.roleSupport == null) { throw new IllegalArgumentException("Missing progression knowledge"); }
		Set<String> contextKeys=new HashSet<>(), relationIds=new HashSet<>();
		for (ProgressionKnowledge.Context context : document.activityContexts)
		{
			stableId(context.id); stableId(context.mode); require(context.method);
			if (!contextKeys.add(context.id) || !activities.containsKey(context.activity) || context.preparation == null || context.resourceConflicts == null
				|| LocalDate.parse(require(context.reviewed)).isAfter(reviewed)) { throw new IllegalArgumentException("Invalid activity context"); }
			evidence(context.sources); Decision contract=new Decision(); contract.sources=context.sources;
			validate(context.accessWhen,contract,0); validate(context.methodWhen,contract,0);
			for (ProgressionKnowledge.Preparation option : context.preparation)
			{
				stableId(option.role); require(option.label); evidence(option.sources);
				if (option.constraint == null || !context.sources.containsAll(option.sources)) { throw new IllegalArgumentException("Invalid preparation evidence"); }
				validate(option.when,contract,0);
			}
			for (String source : context.sources) { if (!sources.get(source).activityContexts.contains(context.id)) { throw new IllegalArgumentException("Missing method source dependency"); } }
			context.resourceConflicts.forEach(KnowledgeCatalog::require); context.resourceConflicts=List.copyOf(context.resourceConflicts);
			context.preparation=List.copyOf(context.preparation); context.sources=List.copyOf(context.sources);
		}
		for (ProgressionKnowledge.Relation relation : document.roleSupport)
		{
			stableId(relation.id); stableId(relation.role); require(relation.reason); require(relation.limitation);
			if (!relationIds.add(relation.id) || upgrades.stream().noneMatch(u->u.id.equals(relation.subject)) || !contextKeys.contains(relation.context)
				|| relation.dimension == null || relation.significance == null || relation.alternatives == null
				|| !Set.of("reviewed","candidate","withdrawn").contains(require(relation.status))
				|| LocalDate.parse(require(relation.reviewed)).isAfter(reviewed)) { throw new IllegalArgumentException("Invalid role relationship"); }
			evidence(relation.sources); Decision contract=new Decision(); contract.sources=relation.sources;
			validate(relation.when,contract,0); validate(relation.noBenefitWhen,contract,0);
			for (ProgressionKnowledge.Alternative alternative : relation.alternatives) { require(alternative.label); validate(alternative.when,contract,0); }
			relation.alternatives=List.copyOf(relation.alternatives); relation.sources=List.copyOf(relation.sources);
			for (String source : relation.sources) { if (!sources.get(source).relations.contains(relation.id)) { throw new IllegalArgumentException("Missing claim source dependency"); } }
		}
		for (Source source : sources.values())
		{ if (!profiles.keySet().containsAll(source.profiles) || source.profiles.stream().anyMatch(id->!profiles.get(id).sources.contains(source.id))
			|| !contextKeys.containsAll(source.activityContexts) || !relationIds.containsAll(source.relations)
			|| source.relations.stream().anyMatch(id->document.roleSupport.stream().noneMatch(r->r.id.equals(id)&&r.sources.contains(source.id)))
			|| source.activityContexts.stream().anyMatch(id->document.activityContexts.stream().noneMatch(c->c.id.equals(id)&&c.sources.contains(source.id)))) { throw new IllegalArgumentException("Unknown claim dependency"); } }
		progression=new ProgressionKnowledge(document.activityContexts,document.roleSupport);

	}

	private static void validateEffort(AcquisitionScope.Mode mode, AcquisitionScope.Burden burden, AcquisitionScope.Tail tail,
		String modeId, String reason, String missingMechanics, List<String> allocations)
	{
		if (mode == null || burden == null || tail == null || allocations == null) { throw new IllegalArgumentException("Invalid effort support"); }
		stableId(modeId); require(reason); require(missingMechanics); allocations.forEach(KnowledgeCatalog::stableId);
	}

	private void evidence(List<String> ids)
	{
		if (ids == null || ids.isEmpty() || new HashSet<>(ids).size() != ids.size() || !sources.keySet().containsAll(ids))
		{ throw new IllegalArgumentException("Invalid evidence references"); }
	}

	private static void validateDependencies(Dependencies dependencies, Set<String> ids, int depth)
	{
		if (dependencies == null) { return; }
		if (depth > 8 || dependencies.mode == null) { throw new IllegalArgumentException("Invalid path dependencies"); }
		if (dependencies.mode == DependencyMode.NODE)
		{ if (!ids.contains(dependencies.node) || dependencies.children != null) { throw new IllegalArgumentException("Unknown path node"); } }
		else
		{
			if (dependencies.node != null || dependencies.children == null || dependencies.children.isEmpty() || dependencies.children.size() > 12)
			{ throw new IllegalArgumentException("Invalid path ALL/ANY"); }
			dependencies.children.forEach(child -> validateDependencies(child, ids, depth + 1));
		}
	}
	private static List<String> dependencyIds(Dependencies dependency)
	{ return dependency == null ? List.of() : dependency.mode == DependencyMode.NODE ? List.of(dependency.node)
		: dependency.children.stream().flatMap(child -> dependencyIds(child).stream()).collect(Collectors.toList()); }
	private static void visitPath(String id, Map<String, PathNode> nodes, Set<String> path, Set<String> visited)
	{
		if (visited.contains(id)) { return; }
		if (path.size() >= 128) { throw new IllegalArgumentException("Acquisition path depth exceeds resource guard"); }
		if (!path.add(id)) { throw new IllegalArgumentException("Acquisition path cycle"); }
		for (String next : dependencyIds(nodes.get(id).dependencies)) { visitPath(next, nodes, path, visited); }
		path.remove(id); visited.add(id);
	}

	/** Exact reviewed facts offered as reversible planning confirmations. */
	public Map<String, String> factChoices()
	{
		Map<String, String> choices = new java.util.LinkedHashMap<>();
		choices.put("rigour", "Rigour unlock"); choices.put("augury", "Augury unlock");
		choices.put("desert-treasure-ii", "Desert Treasure II completed");
		choices.put("blood-moon-rises", "The Blood Moon Rises completed");
		choices.put("dragon-slayer-ii", "Dragon Slayer II completed");
		choices.put("leviathan-defeated", "The Leviathan defeated at least once");
		choices.put("colosseum-preparation", "Colosseum supplies/method reviewed (not completion or readiness certification)");
		choices.put("fremennik-isles", "The Fremennik Isles completed (helm equip)");
		choices.put("fremennik-exiles", "The Fremennik Exiles completed (faceguard equip)");
		choices.put("knight-waves", "King's Ransom and Knight Waves completed (Chivalry/Piety unlock)");
		choices.put("kraken-task", "Current cave-kraken task checked");
		choices.put("while-guthix-sleeps", "While Guthix Sleeps completed");
		choices.put("duradel-notes", "Duradel's notes read (synapse assembly)");
		choices.put("zilyana-entry", "Saradomin GWD 70 Agility, rope and visit entry checked");
		choices.put("monkey-madness-ii", "Monkey Madness II completed");
		choices.put("duke-defeated", "Duke Sucellus defeated at least once");
		choices.put("peer-ring-icons", "Peer the Seer ring-icon instruction learned");
		choices.put("monkey-form", "Monkey form/greegree available for zenyte assembly");
		choices.put("cox-preparation", "CoX team/loadout and supplies reviewed (not access)");
		choices.put("toa-preparation", "ToA mode/loadout and supplies reviewed (not access)");
		choices.put("song-of-elves", "Song of the Elves completed"); choices.put("regicide", "Regicide completed");
		choices.put("beneath-cursed-sands", "Beneath Cursed Sands completed"); choices.put("perilous-moons", "Perilous Moons completed");
		choices.put("deadeye", "Deadeye unlock"); choices.put("mystic-vigour", "Mystic Vigour unlock");
		choices.put("final-dawn", "The Final Dawn completed"); choices.put("frozen-door", "The Frozen Door completed");
		choices.put("smoke-task", "Active smoke-devil Slayer task (manual current check)");
		choices.put("smoke-protection", "Facemask or Slayer helmet available (manual check)");
		choices.put("nex-entry", "Nex chamber killcount/key entry checked for this visit");
		choices.put("nex-team", "Nex team and preparation reviewed (not game access)");
		choices.put("zaryte-context", "Intended high-HP target allows ruby-bolt special utility");
		choices.put("zaryte-shards", "250 nihil shards confirmed (manual quantity check)");
		choices.put("doom-preparation", "Doom supplies/preparation reviewed (not delve capability)");
		choices.put("treads-tears", "4,000 demon tears for one optional treads addition (manual quantity check)");
		choices.put("multi-combat-method", "Intended Venator use has eligible packed multicombat targets (manual scenario)");
		choices.put("secrets-of-north", "Secrets of the North completed");
		choices.put("muspah-preparation", "Muspah method and supplies reviewed (not success certification)");
		choices.put("doom-delve-two", "Planning scenario: complete Doom delve 2 (not observed ability)");
		choices.put("doom-delve-three", "Planning scenario: complete Doom delve 3 (not observed ability)");
		choices.put("doom-delve-four", "Planning scenario: complete Doom delve 4 (not observed ability)");
		choices.put("barbarian-training", "Barbarian Firemaking (not pyres), Fishing and Smithing completed for lance equip");
		choices.put("barbarian-hasta", "Barbarian Fishing and Smithing completed for hasta equip");
		choices.put("monkey-madness-i", "Monkey Madness I completed for dragon scimitar equip");
		choices.put("vardorvis-defeated", "Vardorvis defeated at least once for Ultor equip");
		choices.put("whisperer-defeated", "The Whisperer defeated at least once for Bellator equip");
		choices.put("hydra-task", "Current eligible Hydra assignment (recheck when task changes)");
		choices.put("while-guthix-sleeps", "While Guthix Sleeps completed");
		choices.put("arclight-infusion", "Selected Arclight charges plus infusion cover 10,000 for Emberlight assembly (recheck)");
		choices.put("duradel-notes", "Duradel's notes read after While Guthix Sleeps");
		choices.put("yama-entry", "Yama source access checked; no method readiness inferred");
		choices.put("arachnid-task", "Current eligible araxyte assignment for Araxxor (recheck)");
		choices.put("cerberus-task", "Current eligible hellhound assignment for Cerberus (recheck)");
		choices.put("sins-of-father", "Sins of the Father completed for the reviewed Darkmeyer blood-shard route");
		choices.put("vyre-method", "Compatible Vyrewatch Sentinel weapon/method and supplies checked (not general melee readiness)");
		Map<Integer,String> names=itemNames();
		ownershipLabels.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
			// Some retained ownership sets used their internal key as a display label.
			// List every exact member; a confirmation must keep the set's full meaning.
			String label=e.getValue().equals(e.getKey())?itemSets.get(e.getKey()).stream()
				.map(id->names.getOrDefault(id,"Item "+id)+" ("+id+")").collect(Collectors.joining(" / ")):e.getValue();
			choices.put("owns-"+e.getKey(),label);
		});
		gear.values().stream().flatMap(List::stream).sorted(java.util.Comparator.comparing(g -> g.label))
			.forEach(g -> choices.put("item-" + g.id, g.label));
		return java.util.Collections.unmodifiableMap(choices);
	}

	private static void visit(String id, Map<String, Decision> decisions, Set<String> path, Set<String> visited)
	{
		if (visited.contains(id)) { return; }
		if (!path.add(id)) { throw new IllegalArgumentException("Hard dependency cycle"); }
		for (String dependency : decisions.get(id).requires)
		{
			if (decisions.get(dependency).repeatable) { throw new IllegalArgumentException("Repeatable hard dependency"); }
			visit(dependency, decisions, path, visited);
		}
		path.remove(id); visited.add(id);
	}

	private void validate(Condition condition, Decision decision, int depth)
	{
		if (condition == null || condition.test == null || depth > 8)
		{ throw new IllegalArgumentException("Unsupported condition"); }
		switch (condition.test)
		{
			case ALL: case ANY:
				if (condition.conditions == null || condition.conditions.isEmpty() || condition.conditions.size() > 12
					|| condition.key != null || condition.minimum != null || condition.value != null)
				{ throw new IllegalArgumentException("Invalid all/any condition"); }
				for (Condition child : condition.conditions) { validate(child, decision, depth + 1); }
				condition.conditions = List.copyOf(condition.conditions);
				return;
			case LEVEL:
				if (condition.key == null || !SKILLS.contains(condition.key) || condition.minimum == null
					|| condition.minimum < 1 || condition.minimum > 99 || condition.value != null)
				{ throw new IllegalArgumentException("Invalid level condition"); }
				break;
			case ITEMS: case EQUIPPED: case USABLE: case POSSESSION:
				if (condition.key == null || !itemSets.containsKey(condition.key) || condition.minimum != null || (condition.test != Test.POSSESSION && condition.test != Test.ITEMS && condition.value != null))
				{ throw new IllegalArgumentException("Invalid item condition"); }
				if (condition.test == Test.POSSESSION && !ownershipLabels.containsKey(condition.key))
				{ throw new IllegalArgumentException("Ownership target has no confirmation label"); }
				for (int id : itemSets.get(condition.key))
				{
					Gear item = gear.values().stream().flatMap(List::stream).filter(g -> g.id == id).findFirst().orElseThrow();
					requireItemEvidence(item, decision);
					if (condition.test == Test.USABLE && item.useWhen != null) { validate(item.useWhen, decision, depth + 1); }
				}
				break;
			case FACT:
				if (condition.key == null || !FACTS.contains(condition.key) || condition.value == null || condition.minimum != null)
				{ throw new IllegalArgumentException("Invalid fact condition"); }
				break;
			case CONSTANT:
				if (condition.value == null || condition.key != null || condition.minimum != null)
				{ throw new IllegalArgumentException("Invalid constant"); }
				break;
			case RIGOUR: case AUGURY:
				if (condition.value == null || condition.key != null || condition.minimum != null)
				{ throw new IllegalArgumentException("Invalid unlock condition"); }
				break;
			default: throw new IllegalArgumentException("Unsupported condition");
		}
		if (condition.conditions != null) { throw new IllegalArgumentException("Leaf condition has children"); }
	}

	private static void stableId(String id)
	{ if (!require(id).matches("[a-z][a-z0-9-]*")) { throw new IllegalArgumentException("Invalid stable ID"); } }

	// Only observation/level package conditions; reject recursive usable-item packages.
	private void validateGearCondition(Condition condition, int depth, Set<String> path)
	{
		if (condition == null || condition.test == null || depth > 8) { throw new IllegalArgumentException("Invalid gear package"); }
		if (condition.test == Test.ALL || condition.test == Test.ANY)
		{
			if (condition.conditions == null || condition.conditions.isEmpty()) { throw new IllegalArgumentException("Empty gear package"); }
			for (Condition child : condition.conditions) { validateGearCondition(child, depth + 1, path); }
		}
		else if (condition.test == Test.USABLE)
		{
			if (!itemSets.containsKey(condition.key) || !path.add(condition.key)) { throw new IllegalArgumentException("Gear package cycle"); }
			for (int id : itemSets.get(condition.key))
			{
				Gear item = gear.values().stream().flatMap(List::stream).filter(g -> g.id == id).findFirst().orElseThrow();
				if (item.useWhen != null) { validateGearCondition(item.useWhen, depth + 1, path); }
			}
			path.remove(condition.key);
		}
		else if (condition.test != Test.LEVEL && condition.test != Test.ITEMS && condition.test != Test.FACT)
		{ throw new IllegalArgumentException("Unsupported gear package predicate"); }
	}

	private Set<Integer> ownedVariants(Condition condition)
	{
		if (condition.test==Test.ALL || condition.test==Test.ANY) { return condition.conditions.stream().flatMap(c->ownedVariants(c).stream()).collect(Collectors.toSet()); }
		return condition.test==Test.POSSESSION && Boolean.TRUE.equals(condition.value)?Set.copyOf(itemSets.get(condition.key)):Set.of();
	}
	private static void requireItemEvidence(Gear item, Decision decision)
	{
		if (!decision.sources.containsAll(item.sources)) { throw new IllegalArgumentException("Decision lacks item evidence: " + decision.id + " / " + item.label); }
	}

	private static String require(String text)
	{
		if (text == null || text.isBlank()) { throw new IllegalArgumentException("Missing catalogue field"); }
		return text;
	}

	public static KnowledgeCatalog load(Gson gson) throws IOException
	{
		try (InputStream stream = KnowledgeCatalog.class.getResourceAsStream("/com/ironpath/catalog.json"))
		{
			if (stream == null) { throw new IOException("Bundled catalogue missing"); }
			try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) { return read(gson, reader); }
		}
	}

	public static KnowledgeCatalog read(Gson gson, Reader reader)
	{
		// 4 MiB bounds memory/startup cost while leaving catalogue coverage uncapped.
		StringBuilder input = new StringBuilder();
		try { char[] buffer = new char[8192]; int count;
			while ((count = reader.read(buffer)) != -1) { if (input.length() + count > 4 * 1024 * 1024) { throw new IllegalArgumentException("Catalogue exceeds 4 MiB resource guard"); } input.append(buffer, 0, count); }
		} catch (IOException failure) { throw new IllegalArgumentException("Cannot read catalogue", failure); }
		Document document = gson.fromJson(input.toString(), Document.class);
		if (document == null) { throw new IllegalArgumentException("Empty catalogue"); }
		return new KnowledgeCatalog(document);
	}

	private static final class Document
	{
		int schemaVersion;
		String knowledgeVersion, reviewed;
		List<Decision> decisions;
		List<Source> sources;
		Map<String, List<Gear>> gear;
		Map<String, List<Integer>> itemSets;
		Map<String, String> activities, ownershipLabels;
		List<Goal> goals;
		List<Upgrade> upgrades;
		ComparisonSpec comparison;
		RangedSpec ranged;
		MeleeSpec melee;
		List<AcquisitionSpec> acquisitions;
		List<ActivityReward> activityRewards;
		List<ProgressionKnowledge.Context> activityContexts;
		List<ProgressionKnowledge.Relation> roleSupport;
		List<ActivityEffortProfile> effortProfiles;
		List<AutomaticPaceProfile> automaticPaces;
	}

	public static final class Upgrade
	{
		public boolean reviewedRemainingStages;
		public String id, title, valueReason, alternatives, remainingWork, limitation, comparison, headline, effortShort, status, reviewed;
		public String browseStyle, browseSlot;
		public int itemId;
		public Benefit benefitBand;
		public Condition when, ownedWhen, equipWhen, eligibilityWhen;
		public List<String> routes, goals, sources, unknownSets;
		public List<Application> applications;
		public List<Part> parts;
		public List<SharedReward> shared;
		public List<ContextBenefit> contextBenefits = List.of();
		public List<PathNode> path = List.of();
		public String rewardFamily, role;
		public List<String> contexts;
	}
	public enum DependencyMode { NODE, ALL, ANY }
	public static final class Dependencies { public DependencyMode mode; public String node; public List<Dependencies> children; }
	public static final class PathNode
	{
		public String id, label, detail, role;
		public Integer itemId, quantity, batchItemId, batchSize;
		public Condition completeWhen;
		public Dependencies dependencies;
		public boolean alternativeOnly, acquisition;
	}
	public static final class Application { public String id, text, label; public Condition when; }
	public static final class Part { public String label; public Condition when; public boolean optional; public Integer itemId, quantity; }
	public static final class SharedReward
	{
		public String title, detail;
		public String eligibilityLabel;
		// Reject the retired schema-9 field rather than silently retaining a second source of truth.
		@com.google.gson.annotations.SerializedName("minDelve") private Integer retiredMinDelve;
		public Condition eligibilityWhen;
		public Condition when, ownedWhen;
		public List<String> sources;
	}
	public static final class ComparisonSpec
	{
		public String modelVersion, targetName;
		public int targetId, targetMagic, targetMagicDefence, attackTicks;
		public List<String> sources;
		public List<CombatItem> items;
		public List<TargetProfile> targets;
	}
	public static final class MeleeSpec
	{
		public String modelVersion;
		public List<String> sources;
		public List<MeleeItem> items;
		public Map<Integer,String> unsupportedWeapons;
	}
	public static final class RangedSpec
	{
		public String modelVersion;
		public List<String> sources;
		public List<RangedItem> items;
		public Map<String,Integer> candidateAmmo;
		public Map<Integer,String> unsupportedWeapons;
	}
	public static final class CombatItem
	{
		public int id, magicAttack, damagePercent;
		public Integer damagePermille;
		public boolean twoHanded, historyEffect, unsupportedAccuracy, brimstoneEffect;
		public Map<String, Integer> requirements;
		public String label, slot, weapon;
		public List<String> sources;
	}

	public static final class Decision
	{
		public String id, title, why, alternative, tradeoff, effort, access, completion, applicability, family, status, reviewed;
		public String activity, outcome, preparation, benefitReason;
		public Kind kind;
		public Benefit benefitBand;
		public List<String> goals, notes;
		public List<ContextBenefit> contextBenefits;
		public Effort effortBand;
		public int itemId;
		public List<String> sources;
		public List<String> requires;
		public Condition when, completeWhen, accessWhen, prepWhen;
		public boolean repeatable;
		public EffortSpec pursuit;
	}

	public static final class EffortSpec
	{
		public AcquisitionScope.Mode mode;
		public AcquisitionScope.Burden burden;
		public AcquisitionScope.Tail tail;
		public String modeId, reason, missingMechanics, profile;
		public RemainingEffort.Band band;
		public List<String> sources, allocations;
		public List<EffortVariant> variants;
	}
	public static final class EffortVariant
	{
		public Condition when;
		public AcquisitionScope.Mode mode;
		public AcquisitionScope.Burden burden;
		public AcquisitionScope.Tail tail;
		public String reason;
		public List<String> allocations;
		public boolean bypassesAcquisition;
		public RemainingEffort.Band band;
	}
	public static final class AcquisitionSpec
	{
		public String id, activity, mode, rewardFamily, route, node, recipient, trialUnit, version, reason, reviewed;
		public AcquisitionModel.Support support;
		public AcquisitionModel.Mechanic mechanic;
		public long numerator, denominator;
		public int rolls=1, requiredSuccesses=1;
		public List<String> assumptions, exclusions, sources;
	}
	public static final class ActivityReward
	{
		public String id, activity, mode, family, title;
		public String profile;
		public RemainingEffort.Band band;
		public int itemId;
		public Benefit benefit;
		public AcquisitionScope.Tail tail;
		public Integer requiredQuantity;
		public Condition resourceWhen;
		public Condition when, ownedWhen, accessWhen, prepWhen;
		public Condition eligibilityWhen;
		public List<String> sources;
	}

	public static final class Goal
	{
		public String id, title, decisionId, whenRole;
		public List<String> routes, requires;
		public Condition when, completeWhen;
	}

	public static final class ContextBenefit
	{
		public Condition when;
		public Benefit band;
		public String reason;
	}

	public static final class Condition
	{
		public Test test;
		public String key;
		public Integer minimum;
		public Boolean value;
		public List<Condition> conditions;
	}

	public static final class Source
	{
		public String id, type, url, author, revision, accessed, reviewed, licence, reuseBasis;
		public List<String> decisions;
		public List<Integer> items = List.of();
		public List<String> models = List.of(), routes = List.of(), contexts = List.of();
		public List<String> paces = List.of(), acquisitions = List.of(), efforts = List.of(), rewards = List.of(), relations = List.of(), activityContexts = List.of(), profiles = List.of();
	}

	static final class Gear
	{
		int id, level;
		String label, skill;
		Condition useWhen;
		List<String> sources;
	}
}
