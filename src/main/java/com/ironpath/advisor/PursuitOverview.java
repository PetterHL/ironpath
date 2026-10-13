package com.ironpath.advisor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Presentation slots are not quotas. Browse always retains all relevant reviewed outcomes. */
public final class PursuitOverview
{
	public final List<Advisor.UpgradeView> all, pursuits, longTerm, conditional;
	public final List<ActivityOpportunity> activities;
	public final List<Advisor.Recommendation> quickActions, broadAdvice;
	public PursuitOverview(List<Advisor.UpgradeView> all, List<ActivityOpportunity> activities,
		List<Advisor.Recommendation> quickActions, List<Advisor.Recommendation> broadAdvice)
	{
		this.all=List.copyOf(all); this.activities=List.copyOf(activities);
		this.quickActions=List.copyOf(quickActions); this.broadAdvice=List.copyOf(broadAdvice);
		List<Advisor.UpgradeView> active=new ArrayList<>(), longer=new ArrayList<>(), checks=new ArrayList<>();
		Set<String> families=new HashSet<>(), routes=new HashSet<>();
		for (Advisor.UpgradeView view : all)
		{
			if (view.pursuit.category == PursuitAssessment.Category.PURSUE && active.size() < 2
				&& !families.contains(view.pursuit.family) && !routes.contains(view.pursuit.activity))
			{ active.add(view); families.add(view.pursuit.family); routes.add(view.pursuit.activity); }
			if (view.pursuit.category == PursuitAssessment.Category.LONG_TERM) { longer.add(view); }
			if (view.pursuit.category == PursuitAssessment.Category.CHECK || view.pursuit.category == PursuitAssessment.Category.PLAN) { checks.add(view); }
		}
		pursuits=List.copyOf(active); longTerm=List.copyOf(longer); conditional=List.copyOf(checks);
	}
}
