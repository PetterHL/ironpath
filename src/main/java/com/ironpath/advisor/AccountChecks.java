package com.ironpath.advisor;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** One correction per unresolved ownership fact, with the affected outcomes kept distinct. */
public final class AccountChecks
{
	public static final class Check
	{
		public final String key;
		public final List<String> outcomes;
		Check(String key, java.util.Set<String> outcomes) { this.key=key; this.outcomes=List.copyOf(outcomes); }
	}
	public final List<Check> ownership;
	public AccountChecks(List<Advisor.UpgradeView> upgrades)
	{
		Map<String,java.util.Set<String>> affected=new TreeMap<>();
		for(Advisor.UpgradeView view:upgrades) {
			for(String key:view.questions) { affected.computeIfAbsent(key,k->new TreeSet<>()).add(view.upgrade.id); }
		}
		ownership=affected.entrySet().stream().map(e->new Check(e.getKey(),e.getValue()))
			.sorted(java.util.Comparator.comparingInt((Check c)->c.outcomes.size()).reversed().thenComparing(c->c.key))
			.collect(Collectors.toUnmodifiableList());
	}
}
