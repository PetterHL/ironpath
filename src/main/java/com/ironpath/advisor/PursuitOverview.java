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
	public final List<Advisor.Recommendation> quickActions, broadAdvice;
	public PursuitOverview(List<Advisor.UpgradeView> all, List<ActivityOpportunity> activities,
		List<Advisor.Recommendation> quickActions, List<Advisor.Recommendation> broadAdvice)
	{
		this.all=List.copyOf(all); this.activities=List.copyOf(activities);
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
		Set<String> families=new HashSet<>(), roles=new HashSet<>(), ids=new HashSet<>();
		if (!active.isEmpty()) { select(active.get(0),"Highest justified actionable option",ordered,reasons,families,roles,ids); }
		List<Advisor.UpgradeView> discovery=new ArrayList<>(all);
		// Importance is preserved; the existing rank supplies stable order within a tier.
		discovery.sort(java.util.Comparator.comparing(u->u.valueEvidence.importance));
		if (ordered.isEmpty() && !discovery.isEmpty()) { select(discovery.get(0),"Strongest remaining importance with truthful pursuit status",ordered,reasons,families,roles,ids); }
		// Important long-term and specialist representatives precede additional ordinary-role repeats.
		List<Advisor.UpgradeView> importantLongTerm=discovery.stream().filter(u->(u.pursuit.category==PursuitAssessment.Category.LONG_TERM || u.pursuit.tail==PursuitAssessment.Tail.LONG)
			&& u.valueEvidence.importance.ordinal()<ValueEvidence.Importance.MARGINAL.ordinal() && !families.contains(u.pursuit.family))
			.collect(java.util.stream.Collectors.toList());
		importantLongTerm.stream().filter(u->!roles.contains(discoveryRole(u))).findFirst()
			.or(()->importantLongTerm.stream().findFirst())
			.ifPresent(u->select(u,"Important long-term value stays discoverable before role repeats",ordered,reasons,families,roles,ids));
		List<Advisor.UpgradeView> importantSpecialists=discovery.stream().filter(u->u.valueEvidence.specialist
			&& u.valueEvidence.importance.ordinal()<ValueEvidence.Importance.MARGINAL.ordinal() && !families.contains(u.pursuit.family))
			.collect(java.util.stream.Collectors.toList());
		importantSpecialists.stream().filter(u->!roles.contains(discoveryRole(u))).findFirst()
			.or(()->importantSpecialists.stream().findFirst())
			.ifPresent(u->select(u,"Reviewed specialist value before repeated ordinary roles",ordered,reasons,families,roles,ids));
		for (Advisor.UpgradeView view : discovery)
		{
			if (view.valueEvidence.importance==ValueEvidence.Importance.MARGINAL || view.valueEvidence.importance==ValueEvidence.Importance.UNCERTAIN) { continue; }
			if (!families.contains(view.pursuit.family) && !roles.contains(discoveryRole(view)))
			{ select(view,"Distinct reviewed role before repeated family previews",ordered,reasons,families,roles,ids); }
		}
		for (Advisor.UpgradeView view : all)
		{ if (!families.contains(view.pursuit.family)) { select(view,"Next ranked independent reward family",ordered,reasons,families,roles,ids); } }
		for (Advisor.UpgradeView view : all) { select(view,"Remaining ranked choice; all outcomes retained in Browse",ordered,reasons,families,roles,ids); }
		preview=List.copyOf(ordered); previewReasons=Map.copyOf(reasons);
	}
	private static String discoveryRole(Advisor.UpgradeView view)
	{
		String role=view.valueEvidence.roles.stream().filter(r->r.applicable==com.ironpath.model.Truth.TRUE && !r.covered).map(r->r.role).sorted().findFirst().orElse(view.upgrade.role);
		return role;
	}
	private static void select(Advisor.UpgradeView view,String reason,List<Advisor.UpgradeView> result,Map<String,String> reasons,
		Set<String> families,Set<String> roles,Set<String> ids)
	{
		if (!ids.add(view.upgrade.id)) { return; }
		result.add(view); reasons.put(view.upgrade.id,reason); families.add(view.pursuit.family); roles.add(discoveryRole(view));
	}
}
