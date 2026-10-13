package com.ironpath.advisor;

import java.util.List;
import java.util.Set;
import org.junit.Test;
import static com.ironpath.advisor.PursuitAssessment.*;
import static org.junit.Assert.*;

/** Unsupported ranged/melee names here are synthetic ranker inputs, never runtime catalogue promotions. */
public class RecommendationRankerTest
{
	private RecommendationRanker.Candidate candidate(String id,Mode mode,Burden burden,Tail tail,String bucket,double gain,Double min,Double max,boolean unknown,Set<String> steps,List<String> resources)
	{
		return new RecommendationRanker.Candidate(id,id+"-route",id+"-activity","ordinary",id,bucket,KnowledgeCatalog.Benefit.MODERATE,
			mode,burden,tail,"Synthetic explicit remaining-work evidence",unknown,false,false,true,true,1,2,0,0,gain,min,max,List.of(),resources,steps,null);
	}
	private RecommendationRanker.Candidate c(String id,Mode mode,Burden burden,double gain)
	{ return candidate(id,mode,burden,mode==Mode.LONG_TAIL?Tail.LONG:Tail.ORDINARY,"same-target-baseline-role",gain,null,null,false,Set.of(id),List.of()); }
	@Test public void effortAdmissionPrecedesLargeStrengthAndUnknownIsNeverCheap()
	{
		List<PursuitAssessment> ranked=new RecommendationRanker().rank(List.of(c("tbow",Mode.LONG_TAIL,Burden.HIGH,100),c("nearer",Mode.REPEATABLE,Burden.LOW,1),c("unknown",Mode.UNKNOWN,Burden.UNKNOWN,1000)));
		assertEquals("nearer",ranked.get(0).id);assertEquals(Category.LONG_TERM,ranked.get(1).category);assertEquals(Category.CHECK,ranked.get(2).category);
		assertTrue(ranked.get(0).reasonCodes.contains("READINESS_THEN_BURDEN"));
	}
	@Test public void repeatableAndBoundedAreNotOrderedAsTimeTiers()
	{
		List<PursuitAssessment> ranked=new RecommendationRanker().rank(List.of(c("finite",Mode.BOUNDED,Burden.HIGH,2),c("repeat",Mode.REPEATABLE,Burden.LOW,1)));
		assertEquals("repeat",ranked.get(0).id);assertEquals(Category.LONG_TERM,ranked.get(1).category);
	}
	@Test public void dominanceNeedsMatchedContextAndSupportedNonOverlappingExpectedWork()
	{
		var a=candidate("a",Mode.REPEATABLE,Burden.MODERATE,Tail.ORDINARY,"same",2,5.,10.,false,Set.of("a"),List.of());
		var b=candidate("b",Mode.REPEATABLE,Burden.MODERATE,Tail.ORDINARY,"same",1,11.,20.,false,Set.of("b"),List.of());
		assertTrue(RecommendationRanker.dominates(a,b));
		assertEquals(Category.DEFER,new RecommendationRanker().rank(List.of(a,b)).stream().filter(x->x.id.equals("b")).findFirst().orElseThrow().category);
		var overlapping=candidate("overlap",Mode.REPEATABLE,Burden.LOW,Tail.ORDINARY,"same",1,8.,20.,false,Set.of("b"),List.of());
		var otherTarget=candidate("target",Mode.REPEATABLE,Burden.LOW,Tail.ORDINARY,"other-target",1,11.,20.,false,Set.of("b"),List.of());
		var allocation=candidate("choice",Mode.REPEATABLE,Burden.LOW,Tail.ORDINARY,"same",1,11.,20.,false,Set.of("b"),List.of("zenyte"));
		assertFalse(RecommendationRanker.dominates(a,overlapping));assertFalse(RecommendationRanker.dominates(a,otherTarget));assertFalse(RecommendationRanker.dominates(a,allocation));
	}
	@Test public void explicitRemainingStepsCanProveDominanceButUnknownCannot()
	{
		var ready=candidate("ready",Mode.READY,Burden.LOW,Tail.ORDINARY,"same",2,null,null,false,Set.of("assemble"),List.of());
		var longer=candidate("longer",Mode.REPEATABLE,Burden.MODERATE,Tail.ORDINARY,"same",2,null,null,false,Set.of("drop","assemble"),List.of());
		assertTrue(RecommendationRanker.dominates(ready,longer));
		var unknown=candidate("unknown",Mode.READY,Burden.LOW,Tail.ORDINARY,"same",2,null,null,true,Set.of("assemble"),List.of());
		assertFalse(RecommendationRanker.dominates(unknown,longer));
	}
	@Test public void permutationAndDifferentTargetBucketsHaveStableTransitiveOrder()
	{
		var a=c("a",Mode.REPEATABLE,Burden.MODERATE,1);var b=candidate("b",Mode.REPEATABLE,Burden.MODERATE,Tail.ORDINARY,"different",999,null,null,false,Set.of("b"),List.of());var c=c("c",Mode.REPEATABLE,Burden.MODERATE,2);
		List<String> first=new RecommendationRanker().rank(List.of(a,b,c)).stream().map(x->x.id).collect(java.util.stream.Collectors.toList());
		for(List<RecommendationRanker.Candidate> order:List.of(List.of(c,b,a),List.of(b,a,c),List.of(c,a,b)))
		{ assertEquals(first,new RecommendationRanker().rank(order).stream().map(x->x.id).collect(java.util.stream.Collectors.toList())); }
		assertTrue(first.indexOf("c")<first.indexOf("a"));
	}
}
