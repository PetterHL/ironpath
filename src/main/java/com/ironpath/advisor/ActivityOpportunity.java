package com.ironpath.advisor;

import java.util.List;

/** Shared useful families, not additive gains or a promise of simultaneous rewards. */
public final class ActivityOpportunity
{
	public final String id, activity, mode, title, category, reason;
	public final KnowledgeCatalog.Benefit bestValue;
	public final boolean available;
	public final List<String> usefulFamilies, usefulRewards, uncertainRewards, exclusions, allocations, sources;
	public final List<Integer> itemIds;
	public ActivityOpportunity(String activity, String mode, String title, boolean available, KnowledgeCatalog.Benefit bestValue,
		List<String> families, List<String> rewards, List<String> uncertain, List<String> exclusions, List<String> allocations, List<String> sources, List<Integer> itemIds, boolean longTail)
	{
		this.activity=activity; this.mode=mode; id=activity+"/"+mode; this.title=title; this.available=available && !families.isEmpty(); this.bestValue=bestValue;
		usefulFamilies=List.copyOf(families); usefulRewards=List.copyOf(rewards); uncertainRewards=List.copyOf(uncertain);
		this.exclusions=List.copyOf(exclusions); this.allocations=List.copyOf(allocations); this.sources=List.copyOf(sources);
		this.itemIds=List.copyOf(itemIds);
		category=this.available && usefulFamilies.size() >= 2 ? "Worth doing" : this.available ? longTail ? "Long-term activity" : "Single reward route" : "Check first / Plan ahead";
		reason=usefulFamilies.size() >= 2 ? "Distinct useful missing reward families share this activity. Any useful reward is not all rewards; benefits and drop probabilities are not added."
			: longTail ? "Only one confirmed useful reward family remains. A shared-reward advantage is not established; do not manufacture an immediate grind."
			: "One useful reward family is confirmed. Follow its individual pursuit assessment; no multi-reward advantage or additive gain is claimed.";
	}
}
