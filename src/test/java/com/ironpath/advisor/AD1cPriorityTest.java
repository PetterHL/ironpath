package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.ironpath.advisor.EP1Fixtures.*;
import static com.ironpath.advisor.R15Fixtures.*;
import static com.ironpath.advisor.P4aFixtures.*;

/** Audits the actual Advisor path; reported gear classes plus synthetic inputs are not a player pass. */
public class AD1cPriorityTest
{
	@Test public void accountChecksAreSharedAndConfirmationRemovesOnlyTheResolvedFact() throws Exception
	{
		KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson()); Advisor advisor=new Advisor(catalog);
		var account=ClientEquipmentRegressionTest.reportedLightbearerFixture();
		var before=advisor.upgrades(account,Preferences.EMPTY);
		AccountChecks.Check shared=new AccountChecks(before).ownership.stream().filter(c->c.outcomes.size()>1).findFirst().orElseThrow();
		assertEquals(new HashSet<>(shared.outcomes).size(),shared.outcomes.size());
		var after=advisor.upgrades(account,Preferences.EMPTY.confirm(shared.key,false,TIME));
		assertTrue(new AccountChecks(after).ownership.stream().noneMatch(c->c.key.equals(shared.key)));
		assertTrue(new AccountChecks(after).ownership.stream().anyMatch(c->!c.key.equals(shared.key)));
		assertEquals("No implied absence from a bank read",Preferences.EMPTY.confirmations.size(),0);
	}
	@Test public void conditionalOwnershipRetainsValueAndEffortDifferencesWithoutClaimingReadiness() throws Exception
	{
		KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson()); Advisor advisor=new Advisor(catalog);
		var all=advisor.upgrades(ready(),Preferences.EMPTY);
		var conditional=all.stream().filter(v->!v.questions.isEmpty()).collect(Collectors.toList());
		assertTrue(conditional.size()>1);
		assertTrue(conditional.stream().map(v->v.pursuit.remainingEffort.band).distinct().count()>1);
		assertTrue(conditional.stream().map(v->v.valueEvidence.importance).distinct().count()>1);
		for(var view:conditional) {
			if(view.pursuit.category!=PursuitAssessment.Category.DEFER) { assertEquals(ProgressionPriority.Priority.CONDITIONAL,view.pursuit.priority); }
			assertTrue(view.presentation.priorityReason.contains("if still needed"));
			assertFalse(view.card.status.contains("Check ownership"));
		}
		var pipe=all.stream().filter(v->v.upgrade.id.equals("toxic-blowpipe")).findFirst().orElseThrow();
		var shadow=all.stream().filter(v->v.upgrade.id.equals("tumekens-shadow")).findFirst().orElseThrow();
		assertEquals(ProgressionPriority.Priority.CONDITIONAL,pipe.pursuit.priority);
		assertEquals(ProgressionPriority.Priority.CONDITIONAL,shadow.pursuit.priority);
		assertNotEquals(pipe.presentation.priorityReason,shadow.presentation.priorityReason);
		// A class is not an exact efficiency score: the existing value/effort/confidence ties remain unchanged.
		var confirmed=advisor.upgrades(ready(),absent(catalog));
		assertTrue(confirmed.indexOf(confirmed.stream().filter(v->v.upgrade.id.equals("toxic-blowpipe")).findFirst().orElseThrow())
			<confirmed.indexOf(confirmed.stream().filter(v->v.upgrade.id.equals("tumekens-shadow")).findFirst().orElseThrow()));
	}
	@Test public void completePriorityAuditKeepsDomainOrderAndProductCopySeparateFromReports() throws Exception
	{
		KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson()); Advisor advisor=new Advisor(catalog);
		Map<String,AccountSnapshot> accounts=new LinkedHashMap<>(profiles(catalog));
		accounts.put("developed",developed(false));
		accounts.put("reported-gear-synthetic-inputs",ClientEquipmentRegressionTest.reportedLightbearerFixture());
		accounts.put("brimstone-synthetic",AD1aComparisonTest.brimstoneFixture());
		accounts.put("bowfa-without-armour",withFact(account(Set.of(25867),99,99),"song-of-elves",fact(true)));
		accounts.put("seas-baseline",account(Set.of(11907),99,99));
		accounts.put("melee-with-owned-treads",R15Fixtures.items(MeleeAdviceTest.ordinary(),31088,13239));
		Set<String> covered=new TreeSet<>();
		StringBuilder report=new StringBuilder("# AD1c evaluated priority audit\n\nSynthetic account evidence; reported gear classes have synthetic missing inputs. No full player account or client acceptance. Confirmed-absence fixtures use explicit `absent(catalog)` planning facts; unconfirmed fixtures use `Preferences.EMPTY`. Queue positions retain progression-2's class and tie policy, not a precise DPS/time score.\n");
		for(var profile:accounts.entrySet()) for(boolean unconfirmed:List.of(false,true)) {
			var all=advisor.upgrades(profile.getValue(),unconfirmed?Preferences.EMPTY:absent(catalog));
			var overview=new PursuitOverview(all,List.of(),List.of(),List.of());
			report.append("\n## ").append(profile.getKey()).append(unconfirmed?" / unconfirmed ownership":" / explicit planning absence")
				.append("\n\n").append(all.size()).append(" outcomes; ").append(overview.checks.ownership.size()).append(" distinct shared ownership checks.\n\n")
				.append("| Position | Outcome | Value | Priority | Effort | Method | Constraint | Visible reason |\n|---|---|---|---|---|---|---|---|\n");
			int position=0,previous=-1;
			for(var view:overview.preview) { int next=all.indexOf(view);assertTrue(next>previous);previous=next; }
			for(var view:all) {
				covered.add(view.upgrade.id); var presentation=view.presentation;
				assertEquals("progression-2",view.pursuit.version);
				assertFalse(presentation.priorityReason.contains("unknown value is not zero"));
				assertFalse(presentation.priorityReason.contains("table cell"));
				assertFalse(presentation.priorityReason.contains("neutral value is separate"));
				assertFalse(presentation.priorityReason.contains("Core ranged neck over"));
				assertFalse(presentation.nextStep.contains("unread gear is not absent"));
				if(presentation.effort.band==RemainingEffort.Band.UNKNOWN) { assertNull(presentation.maximumMeanHours); }
				report.append("| ").append(++position).append(" | ").append(view.upgrade.id).append(" | ").append(view.valueEvidence.importance)
					.append(" | ").append(view.pursuit.priority).append(" | ").append(presentation.effort.band).append(" | ").append(presentation.effort.suitability)
					.append(" | ").append(presentation.priorityExplanation.constraint).append(" | ").append(presentation.priorityReason).append(" |\n");
			}
		}
		assertEquals(catalog.upgrades.stream().filter(u->u.status.equals("reviewed")).map(u->u.id).collect(Collectors.toSet()),covered);
		Path root=Path.of("build/validation/ad1c"); Files.createDirectories(root); Files.writeString(root.resolve("priority-audit.md"),report);
	}
}
