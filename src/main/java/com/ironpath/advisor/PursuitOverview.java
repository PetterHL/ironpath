package com.ironpath.advisor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Deterministic discovery order; viewport height selects a prefix, never catalogue eligibility. */
public final class PursuitOverview
{
	public final List<Advisor.UpgradeView> all, pursuits, longTerm, conditional, preview;
	public final Map<String,String> previewReasons;
	public final List<ActivityOpportunity> activities;
	public final List<OwnedUpgrade> owned;
	public final AccountChecks checks;
	public final List<Advisor.Recommendation> quickActions, broadAdvice;
	public PursuitOverview(List<Advisor.UpgradeView> all, List<ActivityOpportunity> activities,
		List<Advisor.Recommendation> quickActions, List<Advisor.Recommendation> broadAdvice)
	{ this(all,activities,quickActions,broadAdvice,List.of()); }
	public PursuitOverview(List<Advisor.UpgradeView> all, List<ActivityOpportunity> activities,
		List<Advisor.Recommendation> quickActions, List<Advisor.Recommendation> broadAdvice, List<OwnedUpgrade> owned)
	{
		this.owned=List.copyOf(owned);
		this.all=List.copyOf(all); checks=new AccountChecks(all); this.activities=List.copyOf(activities);
		this.quickActions=List.copyOf(quickActions); this.broadAdvice=List.copyOf(broadAdvice);
		List<Advisor.UpgradeView> active=new ArrayList<>(), longer=new ArrayList<>(), checks=new ArrayList<>();
		for (Advisor.UpgradeView view : all)
		{
			if (view.pursuit.category==PursuitAssessment.Category.PURSUE) { active.add(view); }
			if (view.pursuit.category==PursuitAssessment.Category.LONG_TERM || view.pursuit.tail==PursuitAssessment.Tail.LONG) { longer.add(view); }
			if (view.pursuit.category==PursuitAssessment.Category.CHECK || view.pursuit.category==PursuitAssessment.Category.PLAN) { checks.add(view); }
		}
		pursuits=List.copyOf(active); longTerm=List.copyOf(longer); conditional=List.copyOf(checks);
		List<Advisor.UpgradeView> ordered=new ArrayList<>(); Map<String,String> reasons=new LinkedHashMap<>();
		Set<String> families=new HashSet<>();
		// A subsequence of the domain queue: deduplicate correlated families without moving any choice up.
		for (Advisor.UpgradeView view : all)
		{
			if (view.pursuit.category==PursuitAssessment.Category.DEFER || !families.add(view.pursuit.family)) { continue; }
			ordered.add(view); reasons.put(view.upgrade.id,"Domain priority retained: " + view.pursuit.priority.label);
		}
		preview=List.copyOf(ordered); previewReasons=Map.copyOf(reasons);
	}
}
