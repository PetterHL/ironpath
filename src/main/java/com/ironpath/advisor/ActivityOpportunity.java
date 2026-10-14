package com.ironpath.advisor;

import java.util.List;
import com.ironpath.model.Truth;

/** Shared useful families, not additive gains or a promise of simultaneous rewards. */
public final class ActivityOpportunity
{
	public final String id, activity, mode, title, category, reason;
	public final KnowledgeCatalog.Benefit bestValue;
	public final boolean available;
	public final List<String> usefulFamilies, usefulRewards, uncertainRewards, exclusions, allocations, sources;
	public final List<Integer> itemIds;
	public final Truth access, method;
	public final List<String> preparation;
	public final ProgressionPriority.Priority priority;
	public ActivityOpportunity(String activity, String mode, String title, boolean available, KnowledgeCatalog.Benefit bestValue,
		List<String> families, List<String> rewards, List<String> uncertain, List<String> exclusions, List<String> allocations, List<String> sources, List<Integer> itemIds, boolean longTail, Truth access, Truth method, List<String> preparation, ProgressionPriority.Priority priority)
	{
		this.access=access; this.method=method; this.preparation=List.copyOf(preparation);
		this.activity=activity; this.mode=mode; id=activity+"/"+mode; this.title=title; this.available=available && !families.isEmpty(); this.bestValue=bestValue;
		usefulFamilies=List.copyOf(families); usefulRewards=List.copyOf(rewards); uncertainRewards=List.copyOf(uncertain);
		this.exclusions=List.copyOf(exclusions); this.allocations=List.copyOf(allocations); this.sources=List.copyOf(sources);
		this.itemIds=List.copyOf(itemIds);
		this.priority=this.available?priority:ProgressionPriority.Priority.CONDITIONAL;
		category=this.available && this.priority.ordinal()<=ProgressionPriority.Priority.WORTHWHILE.ordinal() ? "Worth doing"
			: this.available && this.priority.ordinal()<=ProgressionPriority.Priority.LONG_HORIZON.ordinal() ? "Long-term activity" : "Check first / Plan ahead";
		reason=usefulFamilies.size() >= 2 ? "Distinct useful missing reward families share this activity; their number alone does not establish priority. Any useful reward is not all rewards; benefits and drop probabilities are not added."
			: longTail ? "Only one confirmed useful reward family remains. A shared-reward advantage is not established; do not manufacture an immediate grind."
			: "One useful reward family is confirmed. Follow its individual pursuit assessment; no multi-reward advantage or additive gain is claimed.";
	}
}
