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
	public enum Kind { READ, TRAIN, ASSEMBLE, REVIEW, PLAN, ACQUIRE }
	public enum Effort { SMALL, TRAINING, GRIND }
	public enum Benefit { MAJOR, MODERATE, MINOR, PLANNING }
	public enum Test { ALL, ANY, LEVEL, ITEMS, EQUIPPED, RIGOUR, AUGURY, FACT, POSSESSION, USABLE, CONSTANT }
	private static final Set<String> SKILLS = Set.of("ATTACK", "STRENGTH", "DEFENCE", "RANGED",
		"MAGIC", "PRAYER", "HITPOINTS", "AGILITY", "CRAFTING", "SLAYER", "HERBLORE");
	// This is the finite reader contract, not arbitrary client varbit/quest lookup from JSON.
	private static final Set<String> FACTS = Set.of("rigour", "augury", "song-of-elves", "regicide",
		"beneath-cursed-sands", "perilous-moons", "deadeye", "mystic-vigour");
	public final Map<String, Goal> goals;
	public final Map<String, String> activities;
	private final Map<String, String> ownershipLabels;
	public final String version;
	public final LocalDate reviewed;
	public final List<Decision> decisions;
	public final Map<String, Source> sources;
	final Map<String, List<Gear>> gear;
	final Map<String, List<Integer>> itemSets;

	private KnowledgeCatalog(Document document)
	{
		if (document.schemaVersion != 4 || document.decisions == null
			|| document.decisions.isEmpty() || document.decisions.size() > 64)
		{
			throw new IllegalArgumentException("Unsupported or incomplete PvM catalogue");
		}
		version = require(document.knowledgeVersion);
		reviewed = LocalDate.parse(require(document.reviewed));
		if (document.sources == null || document.sources.size() > 256 || document.gear == null || document.itemSets == null)
		{ throw new IllegalArgumentException("Missing evidence"); }
		Set<String> sourceIds = new HashSet<>();
		for (Source source : document.sources)
		{
			if (source == null || !require(source.id).matches("[a-z][a-z0-9-]*") || !sourceIds.add(source.id))
			{ throw new IllegalArgumentException("Invalid or duplicate source ID"); }
			boolean wiki = require(source.url).matches("https://oldschool\\.runescape\\.wiki/w/[A-Za-z0-9_/%()]+\\?oldid=[0-9]+");
			boolean api = source.url.matches("https://github\\.com/runelite/runelite/blob/[a-f0-9]{40}/runelite-api/src/main/java/net/runelite/api/gameval/ItemID\\.java");
			if (!wiki && !api)
			{
				throw new IllegalArgumentException("Unapproved source URL");
			}
			require(source.author); require(source.type); require(source.reuseBasis); require(source.licence);
			LocalDate accessed = LocalDate.parse(require(source.accessed));
			LocalDate review = LocalDate.parse(require(source.reviewed));
			if (accessed.isAfter(review) || review.isAfter(reviewed))
			{ throw new IllegalArgumentException("Inconsistent source dates"); }
			if (!(wiki ? source.url.endsWith("?oldid=" + require(source.revision)) : source.url.contains("/blob/" + require(source.revision) + "/")))
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
			if (decision.goals == null || decision.goals.size() > 16 || decision.notes == null || decision.notes.size() > 12
				|| decision.contextBenefits == null || decision.contextBenefits.size() > 8
				|| decision.accessWhen == null || decision.prepWhen == null)
			{ throw new IllegalArgumentException("Missing activity, preparation or benefit contract"); }
			if (!Set.of("reviewed", "candidate").contains(decision.status)) { throw new IllegalArgumentException("Unknown review status"); }
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
			if (source.decisions == null || source.decisions.isEmpty() || !ids.containsAll(source.decisions)
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
		if (document.gear.isEmpty() || document.gear.size() > 32 || document.itemSets.size() > 96)
		{ throw new IllegalArgumentException("Unsupported gear budget"); }
		for (String group : document.gear.keySet()) { if (!require(group).matches("[A-Za-z][A-Za-z0-9]*")) { throw new IllegalArgumentException("Invalid gear group"); } }
		Set<Integer> itemIds = new HashSet<>();
		for (List<Gear> group : document.gear.values())
		{
			if (group == null || group.isEmpty() || group.size() > 128) { throw new IllegalArgumentException("Empty gear group"); }
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
			if (set == null || set.isEmpty() || set.size() > 128 || new HashSet<>(set).size() != set.size() || !itemIds.containsAll(set))
			{ throw new IllegalArgumentException("Invalid item set reference"); }
		}
		itemSets = Map.copyOf(document.itemSets.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> List.copyOf(e.getValue()))));
		if (document.activities == null || document.goals == null || document.goals.isEmpty() || document.goals.size() > 16
			|| document.ownershipLabels == null)
		{ throw new IllegalArgumentException("Missing planning choices"); }
		document.activities.forEach((id, label) -> { stableId(id); require(label); });
		if (document.activities.size() > 32) { throw new IllegalArgumentException("Too many activities"); }
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
				|| !ids.containsAll(goal.routes) || goal.routes.size() > 16 || new HashSet<>(goal.routes).size() != goal.routes.size()
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
	}

	/** Exact reviewed facts offered as reversible planning confirmations. */
	public Map<String, String> factChoices()
	{
		Map<String, String> choices = new java.util.LinkedHashMap<>();
		choices.put("rigour", "Rigour unlock"); choices.put("augury", "Augury unlock");
		choices.put("song-of-elves", "Song of the Elves completed"); choices.put("regicide", "Regicide completed");
		choices.put("beneath-cursed-sands", "Beneath Cursed Sands completed"); choices.put("perilous-moons", "Perilous Moons completed");
		choices.put("deadeye", "Deadeye unlock"); choices.put("mystic-vigour", "Mystic Vigour unlock");
		ownershipLabels.entrySet().stream().sorted(Map.Entry.comparingByKey())
			.forEach(e -> choices.put("owns-" + e.getKey(), e.getValue()));
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

	private static void requireItemEvidence(Gear item, Decision decision)
	{
		if (!decision.sources.containsAll(item.sources)) { throw new IllegalArgumentException("Decision lacks item evidence"); }
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
		Document document = gson.fromJson(reader, Document.class);
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
	}

	static final class Gear
	{
		int id, level;
		String label, skill;
		Condition useWhen;
		List<String> sources;
	}
}
