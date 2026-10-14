package com.ironpath.advisor;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Discovery facets describe reviewed coverage, never suitability or model support. */
public final class BrowseMetadata
{
	public final Set<String> styles, slots, activities, roles;
	public BrowseMetadata(KnowledgeCatalog catalog, KnowledgeCatalog.Upgrade upgrade)
	{
		Set<String> styles=new LinkedHashSet<>(), slots=new LinkedHashSet<>();
		if (upgrade.browseStyle!=null) { styles.add(upgrade.browseStyle); }
		if (upgrade.browseSlot!=null) { slots.add(upgrade.browseSlot); }
		catalog.ranged.items.stream().filter(i->i.id==upgrade.itemId).forEach(i->{ styles.add("Ranged"); slots.add(i.slot); });
		catalog.comparison.items.stream().filter(i->i.id==upgrade.itemId).forEach(i->{ styles.add("Magic"); slots.add(i.slot); });
		this.styles=Set.copyOf(styles); this.slots=Set.copyOf(slots);
		activities=catalog.decisions.stream().filter(d->upgrade.routes.contains(d.id)).map(d->d.activity).collect(Collectors.toUnmodifiableSet());
		Set<String> roles=new LinkedHashSet<>(); roles.add(upgrade.role);
		catalog.progression.relations.stream().filter(r->r.subject.equals(upgrade.id)&&"reviewed".equals(r.status)).forEach(r->roles.add(r.role));
		this.roles=Set.copyOf(roles);
	}
	public boolean matches(String style, String slot, String activity, String role)
	{ return (style.isEmpty()||styles.contains(style)) && (slot.isEmpty()||slots.contains(slot))
		&& (activity.isEmpty()||activities.contains(activity)) && (role.isEmpty()||roles.contains(role)); }
}
