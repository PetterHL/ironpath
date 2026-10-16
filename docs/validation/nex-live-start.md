# First Nex client test: start, bank and status follow-up

15 October 2026. Development build **0.16.1**, adapter **nex-public-2**, based on
N2 `bab06ed`. Knowledge `n2-owned-1`, catalogue 14 and preferences 3 are unchanged.

## Observed evidence and uncertainty

Petter supplied native-client screenshots of a censored attempt (#2). The panel
reports 536/536 sampled ticks, UNKNOWN phase/target at the selected first sample,
later Smoke/Shadow/Blood/Ice/Zaros scopes and the explicit gap **Start boundary
missing; enabled/arrived mid-kill**. Thus continuity over the captured interval
does not establish that the capture began at the fight start. No complete live
kill, full eligible fraction, game-timer agreement or N2 value is validated.

Petter initially recalled a paused/bank message for the second attempt, then said
the message may have appeared during the kill or been misread. These screenshots
do not establish the exact live callback ordering, pause duration or activation
time. The fixes below address independently reproduced defects; a new client
attempt must verify whether they resolve this session's failure.

The selected worn melee sample shows an Infernal cape and base Prayer 91; the
previous anonymous fixture's retained melee quiver was an assumption, not actual
account capture. A carried quiver and positive raw arrow counter appear while the
quiver is unworn. Its raw worn-charge flag must not be interpreted as absent stock.
Other displayed resource/ownership values are not promoted to verified charge,
consumption or whole-method evidence. No account identifiers, raw inventory dump,
private trace or damage history is committed.

## Reproduced defects and changes

1. The new `observedBoundaryBeforeSmokeDoesNotBecomeAMidKillEnable` regression
   failed before the fix: a real observed 2→3 boundary followed by smoke on the
   next tick was permanently labeled a mid-kill start. Start capture now samples
   the actual boundary immediately, with UNKNOWN phase until smoke arrives. A
   smoke callback within the existing ten-tick matching bound can corroborate
   the already recorded start from either side. No timestamp, phase or worn
   state is backfilled. Smoke cannot invent a missing boundary; missing/late
   smoke, missing end signals, gaps and genuine mid-kill arrival remain censored.
2. The first visible bank visit previously did not set the bank-return marker
   unless it resumed suspended history. Every verified same-account bank visit
   now marks the return source. A visible bank is sufficient even when a Nex NPC
   remains loaded nearby; arena return additionally requires the reviewed arena
   barrier state. Unbanked return, unknown identity, changed world/profile,
   expiry and other hard resets retain their prior rejection behavior.
3. The main view previously had no explicit active-capture state and suppressed
   refreshes during capture. It now says **Observing the current Nex attempt**
   when capture starts. This is a quiet status update, not combat coaching.
4. Frozen details now include observed start, matched smoke, death/dying and arena
   end ticks, phase coverage and continuity. Exclusions name the specific missing
   signal instead of only saying that start/phase/sample/end evidence is missing.

## Verification

JDK 11 / RuneLite 1.13.2: `XDG_CACHE_HOME=/tmp/ironpath-cache ./gradlew test build --no-daemon`
**passed: 173 tests across 27 suites, zero failures/errors/skips** (34 s).
Normal JAR assembly passed. Existing Gradle deprecation notices remain; no build
errors. `git diff --check` and active-document local links pass. The new ordering
regression was first run against the old implementation and failed as expected.

New regressions exercise smoke-before-boundary, boundary-before-smoke in the same
and following tick, actual boundary samples retaining UNKNOWN history, a genuine
mid-kill arrival despite later smoke/all phases, late smoke, missing end signals,
immutable eligibility evidence, first-bank return, a nearby loaded Nex, resumed
capture/status and concrete details. The old contradictory bank fixture now tests
unbanked arena return with **no visible bank**, preserving that rejection.

Existing multi-switch, phase/minion, death/teleport, banking, account/profile/world,
stale worker/action, preference and independent arithmetic regressions remain.
Historical validation reports retain their original build/test results. Synthetic
replays are independently authored; the user screenshots are not represented as
an event recording or a passing live replay.

## Next client attempt

Close/update/restart the development client on `n2/nex-owned-advice`. Open the
bank once, keep capture enabled, enter before a fresh Nex spawn and observe one
normal full kill. During the kill the panel should display the active-capture
message. Afterward inspect the exact four signal ticks and outcome; then verify
the result survives the normal bank visit. A failed capture remains excluded.
Client retest and player acceptance are **pending**.
