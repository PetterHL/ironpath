package com.ironpath.advisor;

import com.ironpath.advisor.PursuitAssessment.Burden;
import com.ironpath.advisor.PursuitAssessment.Category;
import com.ironpath.advisor.PursuitAssessment.Mode;
import com.ironpath.advisor.PursuitAssessment.Tail;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Partial evidence dominance followed by fixed, transitive comparison buckets. */
public final class RecommendationRanker
{
	public static final String VERSION="progression-2";
	public static final class Candidate
	{
		public final String id, route, activity, activityMode, family, bucket, rationale;
		public final KnowledgeCatalog.Benefit value;
		public final Mode mode;
		public final Burden burden;
		public final Tail tail;
		public final boolean unknown, blocked, preferred, nextReady, positive;
		public final int coverage, confidence, coordination, upkeep;
		public final Double gain, minimumExpectedWork, maximumExpectedWork;
		public final List<String> blockers, allocations;
		public final Set<String> remainingSteps;
		public final EffortEstimate estimate;
		public final ValueEvidence evidence;
		public final RemainingEffort remainingEffort;
		public ValueEvidence.Importance importance() { return evidence == null ? value == KnowledgeCatalog.Benefit.MAJOR ? ValueEvidence.Importance.KEY : value == KnowledgeCatalog.Benefit.MODERATE ? ValueEvidence.Importance.STRONG : value == KnowledgeCatalog.Benefit.MINOR ? ValueEvidence.Importance.MARGINAL : ValueEvidence.Importance.UNCERTAIN : evidence.importance; }
		public Candidate(String id, String route, String activity, String activityMode, String family, String bucket,
			KnowledgeCatalog.Benefit value, Mode mode, Burden burden, Tail tail, String rationale,
			boolean unknown, boolean blocked, boolean preferred, boolean nextReady, boolean positive,
			int coverage, int confidence, int coordination, int upkeep, Double gain, Double minimumExpectedWork,
			Double maximumExpectedWork, List<String> blockers, List<String> allocations, Set<String> remainingSteps, EffortEstimate estimate)
		{
			this.id=id; this.route=route; this.activity=activity; this.activityMode=activityMode; this.family=family; this.bucket=bucket;
			this.value=value; this.mode=mode; this.burden=burden; this.tail=tail; this.rationale=rationale;
			this.unknown=unknown; this.blocked=blocked; this.preferred=preferred; this.nextReady=nextReady; this.positive=positive;
			this.coverage=coverage; this.confidence=confidence; this.coordination=coordination; this.upkeep=upkeep;
			this.gain=gain; this.minimumExpectedWork=minimumExpectedWork; this.maximumExpectedWork=maximumExpectedWork;
			this.blockers=List.copyOf(blockers); this.allocations=List.copyOf(allocations);
			this.remainingSteps=Set.copyOf(remainingSteps); this.estimate=estimate; evidence=null;
			remainingEffort=RemainingEffort.synthetic(burden,unknown,blocked);
		}
		Candidate(Candidate c, ValueEvidence evidence)
		{ this(c,evidence,c.remainingEffort); }
		Candidate(Candidate c, ValueEvidence evidence, RemainingEffort remainingEffort)
		{
			id=c.id; route=c.route; activity=c.activity; activityMode=c.activityMode; family=c.family; bucket=c.bucket; rationale=c.rationale;
			value=c.value; mode=c.mode; burden=c.burden; tail=c.tail; unknown=c.unknown; blocked=c.blocked; preferred=c.preferred;
			nextReady=c.nextReady; positive=c.positive; coverage=c.coverage; confidence=c.confidence; coordination=c.coordination; upkeep=c.upkeep;
			gain=c.gain; minimumExpectedWork=c.minimumExpectedWork; maximumExpectedWork=c.maximumExpectedWork; blockers=c.blockers;
			allocations=c.allocations; remainingSteps=c.remainingSteps; estimate=c.estimate; this.evidence=evidence;
			this.remainingEffort=remainingEffort;
		}
	}

	public List<PursuitAssessment> rank(List<Candidate> candidates)
	{
		List<PursuitAssessment> assessments=new ArrayList<>();
		for (Candidate c : candidates)
		{
			Candidate dominant=candidates.stream().filter(other -> dominates(other,c)).sorted(Comparator.comparing(other -> other.id)).findFirst().orElse(null);
			Category category=c.importance() == ValueEvidence.Importance.UNCERTAIN ? Category.CHECK : dominant != null || !c.positive && c.evidence == null || c.importance() == ValueEvidence.Importance.MARGINAL ? Category.DEFER
				: c.unknown ? Category.CHECK : c.blocked ? Category.PLAN
				: c.remainingEffort.suitability==RemainingEffort.Suitability.PREPARE ? Category.PLAN
				: c.remainingEffort.suitability==RemainingEffort.Suitability.UNKNOWN || c.remainingEffort.band==RemainingEffort.Band.UNKNOWN ? Category.CHECK
				: c.remainingEffort.band.ordinal()>=RemainingEffort.Band.LONG.ordinal() || c.tail==Tail.LONG ? Category.LONG_TERM : Category.PURSUE;
			String code=dominant != null ? "DOMINATED" : category == Category.DEFER ? "SMALL_OR_NO_GAIN"
				: c.unknown ? "CONSEQUENTIAL_UNKNOWN" : c.blocked ? "PREREQUISITE_REMAINING"
				: category == Category.CHECK ? "EFFORT_UNAVAILABLE" : category == Category.LONG_TERM ? "LONG_TAIL_OR_BURDEN" : "SUPPORTED_PURSUIT";
			List<String> codes=new ArrayList<>(c.evidence == null ? List.of() : c.evidence.reasonCodes);
			codes.add(code); codes.add("VALUE_EFFORT_TABLE");
			codes.add("EFFORT_"+c.remainingEffort.band); codes.add("METHOD_"+c.remainingEffort.suitability);
			if (c.preferred) { codes.add("OPTIONAL_INTEREST"); }
			String reason=dominant != null ? "A compatible alternative has at least the supported value and demonstrably no more remaining work: " + dominant.id
				: category == Category.DEFER ? "Small/no demonstrated gain in this reference; inspect specialist utility before pursuing."
				: category == Category.CHECK ? "Check the consequential input or effort gap before treating this as an active pursuit."
				: category == Category.PLAN ? "Complete or check the named prerequisite; value does not establish access."
				: category == Category.LONG_TERM ? "Worth owning, with substantial remaining work or a long RNG tail; strength does not make it the next grind."
				: "Account-specific benefit and remaining effort jointly justify this next step.";
			assessments.add(new PursuitAssessment(c,category,codes,List.of(reason,c.rationale,
				"Value: " + c.importance().label + "; remaining effort: " + c.remainingEffort.band.label + "; " + c.remainingEffort.suitability.label
					+ ". Matched progression-2 table cell: " + ProgressionPriority.of(c.importance(),c.remainingEffort,c.unknown,c.blocked,dominant!=null).label + "."),
				dominant == null ? null : dominant.id));
		}
		java.util.Map<String,Candidate> byRoute=candidates.stream().collect(Collectors.toMap(c -> c.id+"/"+c.route,c -> c));
		// Bucket identity itself carries no priority; a renamed/duplicate context cannot promote a choice.
		java.util.Map<String,String> representatives=new java.util.HashMap<>();
		for (Candidate candidate : candidates) { representatives.merge(candidate.bucket,candidate.id,(a,b)->a.compareTo(b)<0?a:b); }
		assessments.sort(Comparator.comparing((PursuitAssessment a) -> a.priority)
			.thenComparing(a -> !byRoute.get(a.id+"/"+a.route).preferred)
			.thenComparing(Comparator.comparingInt((PursuitAssessment a) -> byRoute.get(a.id+"/"+a.route).confidence).reversed())
			.thenComparing(a -> byRoute.get(a.id+"/"+a.route).importance())
			.thenComparing(a -> a.remainingEffort.band)
			.thenComparing(a -> !byRoute.get(a.id+"/"+a.route).nextReady)
			.thenComparing(a -> representatives.get(byRoute.get(a.id+"/"+a.route).bucket))
			.thenComparing(Comparator.comparingDouble((PursuitAssessment a) -> { Double gain=byRoute.get(a.id+"/"+a.route).gain; return gain == null ? 0 : gain; }).reversed())
			.thenComparing(a -> a.id).thenComparing(a -> a.route));
		List<PursuitAssessment> explained=new ArrayList<>();
		for (PursuitAssessment assessment : assessments)
		{
			String alternative=assessment.alternative;
			if (alternative == null && assessment.category != Category.PURSUE)
			{
				alternative=assessments.stream().filter(a -> a.category == Category.PURSUE
					&& !a.family.equals(assessment.family) && !a.activity.equals(assessment.activity))
					.map(a -> a.id).findFirst().orElse(null);
			}
			explained.add(new PursuitAssessment(byRoute.get(assessment.id+"/"+assessment.route),assessment.category,
				assessment.reasonCodes,assessment.reasons,alternative));
		}
		return List.copyOf(explained);
	}

	public static boolean dominates(Candidate better, Candidate worse)
	{
		if (better.evidence != null && better.evidence.independentUtility || worse.evidence != null && worse.evidence.independentUtility
			|| better.coordination < 0 || worse.coordination < 0 || better.upkeep < 0 || worse.upkeep < 0
			|| better.id.equals(worse.id) || !better.bucket.equals(worse.bucket) || better.gain == null || worse.gain == null
			|| !better.positive || better.unknown || worse.unknown || better.blocked || worse.blocked
			|| better.mode == Mode.UNKNOWN || worse.mode == Mode.UNKNOWN || better.burden == Burden.UNKNOWN || worse.burden == Burden.UNKNOWN
			|| better.tail == Tail.UNKNOWN || worse.tail == Tail.UNKNOWN || better.gain < worse.gain
			|| better.tail.ordinal() > worse.tail.ordinal() || better.coordination > worse.coordination || better.upkeep > worse.upkeep
			|| !better.allocations.isEmpty() || !worse.allocations.isEmpty()) { return false; }
		boolean range=better.maximumExpectedWork != null && worse.minimumExpectedWork != null
			&& better.maximumExpectedWork < worse.minimumExpectedWork;
		boolean steps=!better.remainingSteps.isEmpty() && worse.remainingSteps.containsAll(better.remainingSteps);
		return (range || steps) && (range || !better.remainingSteps.equals(worse.remainingSteps) || better.gain > worse.gain
			|| better.upkeep < worse.upkeep || better.coordination < worse.coordination);
	}
}
