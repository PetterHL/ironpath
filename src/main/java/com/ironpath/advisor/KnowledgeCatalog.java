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

/** Bounded, bundled CoX decisions with a small set of observation predicates. */
public final class KnowledgeCatalog
{
	public enum Kind { PLAN_TEAM, UNLOCK_RIGOUR, UNLOCK_AUGURY, TRAIN_RIGOUR, TRAIN_AUGURY,
		ASSEMBLE_SWAMP, REVIEW_LANCE, COORDINATE_REDUCTION, REVIEW_OCCULT }
	public enum Test { ALL, ANY, LEVEL, ITEMS, EQUIPPED, RIGOUR, AUGURY }
	private static final Set<String> SKILLS = Set.of("ATTACK", "STRENGTH", "DEFENCE", "RANGED",
		"MAGIC", "PRAYER", "HITPOINTS", "AGILITY", "CRAFTING");
	public final String version;
	public final LocalDate reviewed;
	public final List<Decision> decisions;
	public final Map<String, Source> sources;
	final Map<String, List<Gear>> gear;
	final Map<String, List<Integer>> itemSets;

	private KnowledgeCatalog(Document document)
	{
		if (document.schemaVersion != 2 || document.decisions == null
			|| document.decisions.isEmpty() || document.decisions.size() > 12)
		{
			throw new IllegalArgumentException("Unsupported or incomplete CoX catalogue");
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
		Set<Kind> kinds = new HashSet<>();
		for (Decision decision : document.decisions)
		{
			if (decision == null || !require(decision.id).matches("[a-z][a-z0-9-]*") || !ids.add(decision.id)
				|| !kinds.add(decision.kind)) { throw new IllegalArgumentException("Invalid or duplicate decision ID"); }
			if (decision.kind == null || decision.priority < 0 || decision.priority > 9)
			{ throw new IllegalArgumentException("Unsupported decision"); }
			require(decision.title); require(decision.why); require(decision.alternative);
			require(decision.tradeoff); require(decision.effort); require(decision.access);
			require(decision.completion); require(decision.applicability); require(decision.family);
			if ((decision.kind == Kind.UNLOCK_RIGOUR && decision.itemId != 21034)
				|| (decision.kind == Kind.UNLOCK_AUGURY && decision.itemId != 21079))
			{
				throw new IllegalArgumentException("Unreviewed scroll mapping");
			}
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
		if (!document.gear.keySet().equals(Set.of("melee", "ranged", "magic", "crystalBody", "crystalLegs", "crystalHelm",
			"components", "otherMagic", "otherMelee", "neck", "reduction")))
		{
			throw new IllegalArgumentException("Unsupported gear groups");
		}
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
			require(entry.getKey());
			List<Integer> set = entry.getValue();
			if (set == null || set.isEmpty() || new HashSet<>(set).size() != set.size() || !itemIds.containsAll(set))
			{ throw new IllegalArgumentException("Invalid item set reference"); }
		}
		itemSets = Map.copyOf(document.itemSets.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> List.copyOf(e.getValue()))));
		for (Decision decision : document.decisions)
		{
			validate(decision.when, decision, 0);
			if (decision.completeWhen != null) { validate(decision.completeWhen, decision, 0); }
			// These fixed matchers also select gear beyond the explicit condition sets.
			List<String> groups = decision.kind == Kind.PLAN_TEAM || decision.kind == Kind.COORDINATE_REDUCTION
				? List.of("melee", "ranged", "magic", "crystalBody", "crystalLegs", "crystalHelm")
				: decision.kind == Kind.REVIEW_OCCULT ? List.of("magic") : List.of();
			for (String group : groups)
			{
				for (Gear item : gear.get(group)) { requireItemEvidence(item, decision); }
			}
		}
		decisions = List.copyOf(document.decisions);
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
			case ITEMS: case EQUIPPED:
				if (condition.key == null || !itemSets.containsKey(condition.key) || condition.minimum != null || condition.value != null)
				{ throw new IllegalArgumentException("Invalid item condition"); }
				for (int id : itemSets.get(condition.key))
				{
					Gear item = gear.values().stream().flatMap(List::stream).filter(g -> g.id == id).findFirst().orElseThrow();
					requireItemEvidence(item, decision);
				}
				break;
			case RIGOUR: case AUGURY:
				if (condition.value == null || condition.key != null || condition.minimum != null)
				{ throw new IllegalArgumentException("Invalid unlock condition"); }
				break;
			default: throw new IllegalArgumentException("Unsupported condition");
		}
		if (condition.conditions != null) { throw new IllegalArgumentException("Leaf condition has children"); }
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
	}

	public static final class Decision
	{
		public String id, title, why, alternative, tradeoff, effort, access, completion, applicability, family, status, reviewed;
		public Kind kind;
		public int priority;
		public int itemId;
		public List<String> sources;
		public List<String> requires;
		public Condition when, completeWhen;
		public boolean repeatable;
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
		boolean crystal;
		List<String> sources;
	}
}
