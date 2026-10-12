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

/** A fixed five-decision experiment, not an executable rule language. */
public final class KnowledgeCatalog
{
	public enum Kind { PLAN_TEAM, UNLOCK_RIGOUR, UNLOCK_AUGURY, TRAIN_RIGOUR, TRAIN_AUGURY }
	public final String version;
	public final LocalDate reviewed;
	public final List<Decision> decisions;
	public final Map<String, Source> sources;
	final Map<String, List<Gear>> gear;

	private KnowledgeCatalog(Document document)
	{
		if (document.schemaVersion != 1 || document.decisions == null
			|| document.decisions.size() < 3 || document.decisions.size() > 6)
		{
			throw new IllegalArgumentException("Unsupported or incomplete T1 catalogue");
		}
		version = require(document.knowledgeVersion);
		reviewed = LocalDate.parse(require(document.reviewed));
		if (document.sources == null || document.gear == null) { throw new IllegalArgumentException("Missing evidence"); }
		Set<String> sourceIds = new HashSet<>();
		for (Source source : document.sources)
		{
			if (!sourceIds.add(require(source.id))) { throw new IllegalArgumentException("Duplicate source"); }
			if (!require(source.url).startsWith("https://oldschool.runescape.wiki/w/")
				|| !source.url.matches("https://oldschool\\.runescape\\.wiki/w/[A-Za-z0-9_/%]+\\?oldid=[0-9]+"))
			{
				throw new IllegalArgumentException("Unapproved source URL");
			}
			require(source.author); require(source.type); require(source.reuseBasis);
			LocalDate.parse(require(source.accessed)); require(source.revision);
		}
		sources = Map.copyOf(document.sources.stream().collect(Collectors.toMap(s -> s.id, s -> s)));
		Set<String> ids = new HashSet<>();
		Set<Kind> kinds = new HashSet<>();
		for (Decision decision : document.decisions)
		{
			if (!ids.add(require(decision.id)) || !kinds.add(decision.kind)) { throw new IllegalArgumentException("Duplicate decision"); }
			if (decision.kind == null || decision.priority < 0) { throw new IllegalArgumentException("Unsupported decision"); }
			require(decision.title); require(decision.why); require(decision.alternative);
			require(decision.tradeoff); require(decision.effort); require(decision.access);
			require(decision.completion); require(decision.applicability); require(decision.family);
			if ((decision.kind == Kind.UNLOCK_RIGOUR && decision.itemId != 21034)
				|| (decision.kind == Kind.UNLOCK_AUGURY && decision.itemId != 21079))
			{
				throw new IllegalArgumentException("Unreviewed scroll mapping");
			}
			if (!Set.of("reviewed", "candidate").contains(decision.status)) { throw new IllegalArgumentException("Unknown review status"); }
			LocalDate.parse(require(decision.reviewed));
			if (decision.sources == null || decision.sources.isEmpty() || !sources.keySet().containsAll(decision.sources))
			{
				throw new IllegalArgumentException("Missing decision source");
			}
			decision.sources = List.copyOf(decision.sources);
		}
		for (Source source : sources.values())
		{
			if (source.decisions == null || source.decisions.isEmpty() || !ids.containsAll(source.decisions))
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
		if (!document.gear.keySet().equals(Set.of("melee", "ranged", "magic", "crystalBody", "crystalLegs", "crystalHelm")))
		{
			throw new IllegalArgumentException("Unsupported gear groups");
		}
		Set<Integer> itemIds = new HashSet<>();
		for (List<Gear> group : document.gear.values())
		{
			if (group.isEmpty()) { throw new IllegalArgumentException("Empty gear group"); }
			for (Gear item : group)
			{
				if (item.id <= 0 || !itemIds.add(item.id) || item.level < 1 || item.level > 99
					|| !Set.of("ATTACK", "MAGIC", "RANGED", "DEFENCE").contains(item.skill))
				{
					throw new IllegalArgumentException("Invalid gear evidence");
				}
				require(item.label);
			}
		}
		gear = Map.copyOf(document.gear.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> List.copyOf(e.getValue()))));
		decisions = List.copyOf(document.decisions);
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
	}

	public static final class Decision
	{
		public String id, title, why, alternative, tradeoff, effort, access, completion, applicability, family, status, reviewed;
		public Kind kind;
		public int priority;
		public int itemId;
		public List<String> sources;
	}

	public static final class Source
	{
		public String id, type, url, author, revision, accessed, reuseBasis;
		public List<String> decisions;
	}

	static final class Gear
	{
		int id, level;
		String label, skill;
		boolean crystal;
	}
}
