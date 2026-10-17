# Nex completed-history loss during bank travel

16 October 2026. Development build 0.19.1, based on N3c `99356a1` / PR #37.
Capture arithmetic and adapter version `nex-public-4` are unchanged. This report
records a failed client check and a tested code correction; the corrected client
round trip still needs validation.

## Client evidence

Petter supplied screenshots from the current test client:

- Kill #1 was COMPLETE with the correlated Nex death/dying and arena-end reason.
  Displayed smoke start 237, death/dying 811 and arena end 818. The internal
  duration was `(811 - 237) * 0.6 = 344.4` seconds. All five phases, continuous
  samples, 574/574 ticks and zero unclassified hits were displayed.
- The main result showed one complete/eligible attempt, session n=1 and observed
  Nex/minion hitsplat share 44.2%. This does not calibrate the game timer or
  establish official server contribution, team composition or all live mappings.
- After leaving the encounter, the result became unavailable before bank opening.
  A subsequent screenshot showed Ready to observe and 0/0 captured attempts.
  Petter explicitly confirmed that the bank was open for that screenshot.
- Petter noted that opening the bank may have been delayed. Exact elapsed ticks,
  exit callbacks and live reset cause were not recorded. The former one-minute
  expiry is a plausible cause, not a measured diagnosis of that specific trip.

This bank-retention check failed. Earlier successful screenshots and prior
reports do not override it. No raw account/bank dump, character identifier,
performance persistence or user screenshot was added to the repository.

## Reproductions and correction

Three new regression cases failed against the previous code before the fix:

1. `NexSession` cleared completed history after 100 ticks (roughly 60 seconds)
   while waiting for the bank, or during a longer return leg. Completed summaries
   now remain bounded to 50 attempts in memory until a hard account/session
   boundary. They stay hidden during pending travel and cannot be appended to
   while suspended. Opening the verified bank restores the original immutable
   result, without reconstructing it from the screenshots.
2. Arena-like barrier/Nex evidence before the outbound bank opened also cleared
   history. A nearby loaded Nex is insufficient to identify a return trip. The
   outbound leg now remains paused until bank verification; only a recognized
   bank-origin return can resume from arena evidence.
3. An exit-barrier callback before LOADING made the departure look unrecognized.
   `NexObserver` retains its verified arena source through that callback until
   the scene transition. `transition`/`clearActive` consume/reset it, so a nearby
   NPC outside the arena does not create an unrestricted travel source.

The combined plugin regression uses LOADING/LOGGED_IN, an exit barrier, delayed
bank opening with a nearby Nex, a late return, and actual native panel rendering.
After bank revalidation the same completed summary and n=1 are visible again.
An additional adapter test checks the departure-source lifetime and reset.

No timeout is needed to bound already bounded immutable summaries. Live account
snapshots, actor references, active capture buffers and pending worker/action
tokens retain their existing clearing behavior. Unknown/changed identity,
profile/character/world change, logout/disconnect, unsupported worlds, disable
and Reset still clear the session. Clock rollback before the travel origin is
rejected. Old time-expiry expectations were replaced with these hard boundaries.

## Automated verification

The affected NexSession, observer, plugin lifecycle and native panel suites pass.
The three initial reproductions failed before the production correction; the
adapter source-lifetime case adds coverage of its consumption/reset.

Final normal cached-dependency command:

```sh
source /workspace/ironpath-environment/activate.sh
XDG_CACHE_HOME=/tmp/ironpath-cache ./gradlew test build --no-daemon --offline
```

- Temurin JDK 11.0.32.1+1, Gradle 8.10, RuneLite 1.13.2.
- **261 tests across 32 suites; zero failures, errors or skips.**
- Four new regression cases; existing account/profile/world/logout/reset,
  unsupported-world, bounded-history, stale worker/action, combat and N3 cases pass.
- Normal `build/libs/ironpath-0.19.1.jar` assembly succeeds.
- `git diff --check` and changed-document relative file links pass.
- The integration assertion checks the native panel's Last complete kill after
  bank revalidation. No graphical client was launched for this correction.

## 0.19.2 preparation without another client run

A new plugin regression reproduced another loss against 0.19.1: after a bank
return was verified from public arena state, a second LOADING could arrive before
the preference read completed. The observer had not yet ticked, so that verified
scene was missing as a departure source. The same public arena evidence now
establishes that source independently of settings IO. The test also completes an
old queued preference read after the new scene boundary and checks that it cannot
publish into the new live generation.

Build metadata is expanded from Gradle's version into a bundled properties file,
so the native header identifies the actual development/JAR build. A constant reset
explanation survives fresh account reads after history loss and disappears after
a new attempt. It contains no account identifiers or performance data and is not
persisted/logged. A pure lifecycle test and a native panel regression verify the
notice lifetime, frozen prior results and no old-history disclosure. Native
225px captures at 12pt/20pt were inspected: `build/validation/n1/bank-reset-12.png`
and `bank-reset-20.png`; these are synthetic, not client acceptance.

The same normal cached-dependency `test build` command passes for 0.19.2:

- **264 tests across 32 suites; zero failures, errors or skips.**
- Three additional regressions cover the delayed preference read, reset-reason
  lifetime and native empty-session display. The delayed-read reproduction failed
  against 0.19.1 before the production correction.
- `build/libs/ironpath-0.19.2.jar` contains `BuildInfo.class` and expanded
  `com/ironpath/build.properties` with `version=0.19.2`.
- Native 225px captures at both font sizes, `git diff --check` and changed-document
  relative file links pass. No graphical client was launched.

## 0.19.3 review regression: LOADING inside the arena

The review finding on PR #38 is confirmed. In `c1ed704`, LOADING from an
unbanked arena sets `returning=false` and `suspended=true`. The subsequent
same-account arena read cannot resume that outbound-only state, so `nexReady()`
rejects the next smoke callback indefinitely. GPU expanded-map-loading changes
are one trigger; no actual bank trip is required. The paused panel also returned
before rendering Reset.

The permanent `sceneReloadInsideArenaAllowsTheNextSmokeStartWithoutABank`
integration test exercises repeated LOADING/LOGGED_IN, an initially cleared NPC
list, fresh account/settings reads and the next real smoke callback, without
opening a bank. The same test fails on `c1ed704` and passes on base `99356a1`.

The fix records arena-reload eligibility separately from bank-return eligibility.
At the first LOADING it requires the current arena barrier and the adapter's
current arena or matching public scene evidence. A previously observed exit
overrides a lagging getter. Repeated LOADING preserves the initial classification
and cannot promote an outbound leg. A settled outside-arena barrier revokes
reload eligibility; temporarily absent NPCs alone do not establish an exit.
Matching arena evidence can then resume an arena rebuild without opening a bank.
Completed immutable summaries survive; active traces and old live generations
still end at the scene boundary. A reload does not establish a bank visit.

Five added tests cover the integration reproduction, completed-history retention
and repeated loading, exit/reload classification, identity/world/clock/reset
boundaries, and the paused Reset action with rejection of an old view's button.
The existing delayed-bank, nearby-Nex, preference-IO and lifecycle cases remain;
the outbound integration additionally verifies that an observed exit wins over
a lagging getter at LOADING. Reset becomes available once the live account and
settings have been revalidated, while paused history remains hidden.

The bank-history deadline remains removed. Account/profile/world, unknown
identity, clock rollback, logout, unsupported-world, disable, worker and action
guards remain in place. No graphical client was launched; client testing is
deferred at Petter's request.

Automated verification for 0.19.3 (JDK 11.0.32.1+1, Gradle 8.10, RuneLite 1.13.2):

- Normal `test build --no-daemon --offline` with the command/environment above
  succeeds in 54 seconds. Initial network resolution refreshed the restored
  dependency metadata before the offline runs.
- **269 tests across 32 suites; zero failures, errors or skips** (264 existing
  tests plus five new regressions).
- The identical integration reproduction fails on `c1ed704` and passes on
  `99356a1`; it also passes in the complete corrected suite.
- `build/libs/ironpath-0.19.3.jar` contains bundled `version=0.19.3` metadata.
- `git diff --check` and changed-document relative file links pass.

## DEV1 integration review — 17 October 2026

Merged the reviewed DEV1 workflow through the existing N3b/N3c stack after
PR #40. Reviewed the bank/reload distinction, exit evidence overriding a lagging
getter, repeated LOADING, delayed preferences, hidden suspended history and hard
identity/reset boundaries. This integration changes test replay and documentation;
the reviewed 0.19.3 production correction remains unchanged.

Two additional scenarios drive the real plugin's public event callbacks and
capture/comparison pipeline, using the same synthetic client and native panel:

- `nex-bank-long-trip`: wait 150 logged-in ticks outbound with nearby Nex after
  an observed exit, verify paused/hidden history and Reset, restore the exact
  first summary at the bank, wait another 150 ticks returning, and complete the
  next kill. The two matched kills retain n=2, mean 15 seconds and aggregate
  damage share 40%. A subsequent character change still clears the history.
- `nex-arena-reload`: repeated LOADING with initially missing NPCs keeps history
  suspended; restored arena evidence resumes it without a bank. The next real
  smoke callback starts a fresh complete kill and publishes its own comparison.

The long-trip replay was also run with the former 100-tick deadline temporarily
restored: it exited unsuccessfully with 16 failed checks across both font sizes,
including lost history, wrong counts and wrong combined means. The mutation was
removed before the final build. This demonstrates that the new replay detects
the old regression rather than merely rendering a passing fixture.

Final normal compatibility command:

```sh
source /workspace/ironpath-environment/activate.sh
./gradlew test build offlineReview --no-daemon
```

Temurin 11.0.32.1+1 / Gradle 8.10 / RuneLite 1.13.2: **288 tests across
34 suites**, zero failures/errors/skips. All nine scenarios passed **293 checks**
and regenerated nine reports plus **56 PNGs**. The two added scenarios contribute
62 checks and 12 PNGs. Inspected all 12 new native 225px panel images at 12/20px:
paused Reset, restored first result, next-kill counts/means and identity clearing
remain readable without new clipping. The JAR contains bundled `version=0.19.3`
and excludes the offline replay/development harness.

These are synthetic regression and UI results. The N3b/N3c numerical API remains
covered by its independent unit/integration cases; it is not automatically
inferred from these captures. No corrected live-client acceptance is claimed.
This PR remains draft pending the deferred client retest below.

## Client retest (deferred)

Use branch `fix/nex-banking-session`, build 0.19.3. Restarting the development
client starts a new in-memory session; the already cleared kill cannot be restored.

1. Check the header says build 0.19.3. Capture one complete Nex kill and note
   the displayed duration and n=1.
2. Leave the arena, deliberately wait more than a minute before opening the bank,
   then open it. The original duration, counts and encounter details must return.
3. Close the bank and return on the same account/profile/world. The same result
   must remain, then a second complete kill must add to complete counts (cohort
   means combine only when the observed method/setup/team evidence matches).
4. Check a hard boundary separately: logout/Reset clears the session, and a later
   bank opening must not revive it.
5. When client testing is available, change GPU expanded map loading while still
   in the arena. After scene/account revalidation, the next smoke must start
   capture without visiting a bank. Reset must also be available in a paused
   view after account/settings revalidation.

If counts reset, one screenshot of Encounter now includes the build and reset
reason. The first short retest can stop after the original result returns at the
opened bank; further kills and hard-boundary checks can be done later.

No corrected live-client result is claimed yet. Broader N1/N2 capture, resource,
multi-trip and player checks remain open in [TASKS](../../TASKS.md).
