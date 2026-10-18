package com.ironpath;

import com.ironpath.dev.OrderedReplay;
import com.ironpath.dev.ReviewReport;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class OfflineReviewTest
{
	@Rule public TemporaryFolder temporary = new TemporaryFolder();

	@Test public void completeReplayChecksProductionCalculationsAndRegeneratesActualPanelImages() throws Exception
	{
		var output = temporary.newFolder().toPath();
		Files.writeString(output.resolve("stale.png"), "obsolete prior output");
		var result = OfflineReview.run("nex-complete", output);
		assertTrue(Files.readString(output.resolve("report.md")), result.passed());
		assertFalse(Files.exists(output.resolve("stale.png")));
		try (var files = Files.list(output)) { assertEquals(20, files.filter(p -> p.toString().endsWith(".png")).count()); }
		assertEquals(225, javax.imageio.ImageIO.read(output.resolve("main-20.png").toFile()).getWidth());
	}

	@Test public void independentExpectedResultsRejectWrongValuesAndKeepFailedEvidence() throws Exception
	{
		var report = new ReviewReport();
		assertFalse(report.near("Wrong duration", 18, 19, 1e-10));
		assertFalse(report.near("Unknown is not zero", 0, Double.NaN, 0));
		assertFalse(report.passed());
		var output = temporary.newFolder().toPath(); report.write(output);
		String text = Files.readString(output.resolve("report.md"));
		assertTrue(text.contains("FAIL · 2 checks · 2 failures"));
		assertTrue(text.contains("19.0"));
	}

	@Test public void sameTickStateAndCallbacksStayInDeclaredOrder() throws Exception
	{
		List<Integer> actual = new ArrayList<>(); int[] clock = {-1}, state = {0};
		var replay = new OrderedReplay();
		replay.at(7, "event payload arrives before polled state", () -> actual.add(state[0]));
		replay.at(7, "getter catches up", () -> state[0] = 3);
		replay.at(7, "tick poll", () -> actual.add(state[0]));
		replay.run(t -> clock[0] = t, line -> { });
		assertEquals(List.of(0, 3), actual); assertEquals(7, clock[0]);
		assertThrows(IllegalArgumentException.class, () -> replay.at(6, "backwards", () -> { }));
	}

	@Test public void unknownInputsBlockOnlyDependentClaims() throws Exception { lifecycle("nex-unknown", 6); }
	@Test public void incompleteAttemptsRemainExcluded() throws Exception { lifecycle("nex-incomplete", 4); }
	@Test public void bankTripRetainsHistoryAndAcceptsNextKill() throws Exception { lifecycle("nex-bank-trip", 6); }
	@Test public void pendingIdentityPublicationCannotOverwriteFreshWork() throws Exception { lifecycle("nex-identity-pending", 6); }
	@Test public void resetCancelsPendingWorkWithoutPreventingFreshPublication() throws Exception { lifecycle("nex-reset-pending", 6); }
	@Test public void newResultsPreserveInspectedHistory() throws Exception { lifecycle("nex-frozen-history", 8); }
	@Test public void longBankTripAndNearbyNexPreserveHistoryWithoutWeakeningIdentityGuards() throws Exception { lifecycle("nex-bank-long-trip", 8); }
	@Test public void arenaReloadResumesCaptureWithoutABankVisit() throws Exception { lifecycle("nex-arena-reload", 4); }
	@Test public void contextCorrectionsReconcileConfirmedCohorts() throws Exception { context("nex-context"); }
	@Test public void historicalContextEditsPreserveAnotherActiveAttemptAndFrozenInputs() throws Exception { context("nex-context-active"); }
	@Test public void repeatedContextEditsRejectStalePublicationAfterResetAndLogout() throws Exception { context("nex-context-pending"); }
	private void context(String id) throws Exception
	{
		var output=temporary.newFolder().toPath(); var report=OfflineReview.run(id,output);
		assertTrue(Files.readString(output.resolve("report.md")),report.passed());
		assertEquals(225,javax.imageio.ImageIO.read(output.resolve("corrected-diagnostics-20.png").toFile()).getWidth());
	}

	private void lifecycle(String id, int images) throws Exception
	{
		var output = temporary.newFolder().toPath();
		Files.writeString(output.resolve("stale.png"), "old preview");
		var report = OfflineReview.run(id, output);
		assertTrue(Files.readString(output.resolve("report.md")), report.passed());
		assertFalse(Files.exists(output.resolve("stale.png")));
		try (var files = Files.list(output)) { assertEquals(images+6, files.filter(p -> p.toString().endsWith(".png")).count()); }
	}
}
