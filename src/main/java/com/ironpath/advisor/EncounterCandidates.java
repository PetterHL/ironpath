package com.ironpath.advisor;

import com.google.gson.Gson;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/** Encounter-specific suggestions; neither ownership nor calculation support is implied. */
public final class EncounterCandidates
{
	public enum Role
	{
		MELEE("Melee"), RANGED("Ranged"), MAGIC("Magic"), SPECIAL("Special attacks");
		public final String label; Role(String label) { this.label=label; }
		@Override public String toString() { return label; }
	}
	public static final class Item
	{
		public final int id;
		public final String name,slot,method,conditions,ammoKind;
		public final List<Role> roles;
		public final List<Integer> requiredCompanions;
		public final boolean defensive;
		private Item(Spec s)
		{
			id=s.id; name=s.name; slot=s.slot; method=s.method; conditions=s.conditions; ammoKind=s.ammoKind;
			roles=List.copyOf(s.roles); requiredCompanions=List.copyOf(s.requiredCompanions);
			defensive=s.defensive;
		}
	}
	private static final class Spec
	{ int id; String name,slot,method,conditions,ammoKind; boolean defensive; List<Role> roles; List<Integer> requiredCompanions; }
	private static final class Document
	{ String encounter,source,revision,reviewed; List<String> methods; List<Spec> items; }
	public final String encounter,source,revision,reviewed;
	public final Set<String> methods;
	public final List<Item> items;
	private EncounterCandidates(Document d)
	{
		encounter=d.encounter; source=d.source; revision=d.revision; reviewed=d.reviewed;
		methods=Set.copyOf(d.methods);
		Set<Integer> seen=new HashSet<>();
		for(Spec item:d.items)
		{
			if(item.id<=0 || !seen.add(item.id) || item.roles.isEmpty() || !PoweredMagic.SLOTS.contains(item.slot))
			{ throw new IllegalArgumentException("Invalid encounter candidate"); }
		}
		items=d.items.stream().map(Item::new).collect(Collectors.toUnmodifiableList());
	}
	private static final class Nex { static final EncounterCandidates VALUE=readNex(); }
	public static EncounterCandidates nex() { return Nex.VALUE; }
	private static EncounterCandidates readNex()
	{
		try(InputStream in=EncounterCandidates.class.getResourceAsStream("/com/ironpath/nex-candidates.json"))
		{
			if(in==null) { throw new IllegalStateException("Missing Nex equipment baseline"); }
			return new EncounterCandidates(new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),Document.class));
		}
		catch(IOException e) { throw new IllegalStateException(e); }
	}
	public Set<Integer> suggestions(String encounter,String method,Role role,Set<Integer> manual)
	{
		if(!this.encounter.equals(encounter) || !methods.contains(method)) { return Set.copyOf(manual); }
		Set<Integer> ids=items.stream().filter(i->this.encounter.equals(encounter) && i.roles.contains(role)
			&& (i.method.equals("any") || i.method.equals(method))).map(i->i.id).collect(Collectors.toCollection(TreeSet::new));
		ids.addAll(manual); return Set.copyOf(ids);
	}
}
