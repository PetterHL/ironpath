# Nex transition — 15 October 2026

Baseline: [e1888ff](https://github.com/PetterHL/ironpath/tree/e1888ffc2ef571ed6159efaeead4016f42e32646).
Transition build: **0.14.1**. Knowledge `ad1-r3b-1`, catalogue 14 and preference 2
are unchanged. This is a cleanup and implementation handoff, not Nex delivery.

## Changes

- Replaced active product/design/UX/task/development instructions with the accepted
  encounter-and-method direction and one N1–N4 backlog.
- Archived ten old documents, preserving original prose/status/evidence and pinning
  their relative links to the baseline. [Manifest](../archive/pre-nex-20261015/manifest.json)
  records original/snapshot hashes. Historical report links follow the archived specs.
- Removed `CompactRowLayout`, which had no production or test references.
- Extracted `CombatStateReader` from the old boss observer. The same immutable
  slots, real/current levels, prayers and style/spec values are read on the client
  thread; no quest scanning or new capture fields are added.
- Renamed `RecentBossObserver` to `ZilyanaObserver` and `RecentBossComparison` to
  `ZilyanaWornStateComparison`, including their callers/tests. Added an explicit
  ordinary-normal-Zilyana context guard to prevent accidental reuse as a Nex model.

## Preserved and deferred

Combat formulas, catalogue facts, source registers, original oracle resources,
account/privacy/preference protections and the working advisor UI remain. The
Zilyana experiment stays opt-in and retains its current loading reset, timer and
one-worn-state limitations. Existing pending actual-client checks are not passed
by this change. N1 adds the real Nex adapter/session flow; N2 replaces primary
ranking and removes its superseded consumers.

## Verification

Verified on Windows with Temurin **11.0.32.1+1**, Gradle **8.10** and resolved
RuneLite **1.13.1**. Dependencies were already cached; no fresh dependency resolution
is claimed. The build retains `latest.release`, so later online builds may differ.

```powershell
.\gradlew.bat test build --offline --no-daemon
.\gradlew.bat dependencyInsight --dependency net.runelite:client --configuration testRuntimeClasspath --offline --no-daemon
git diff --check
```

- Full test/build: **354 tests in 50 suites, zero failures, errors or skips**;
  all six build tasks executed successfully in 5m 40s. Compiler/Gradle deprecation
  warnings remain; no new API or dependency was introduced.
- Four new regressions cover client-thread enforcement, unread versus observed
  empty equipment, immutable evidence across equipment/prayer/level/spec changes,
  and rejection of other encounters, phases and modes by the Zilyana comparison.
  Existing observer, comparison, UI, lifecycle, preference and arithmetic tests pass.
- Archive checks: all ten original hashes match the baseline; all ten snapshot
  hashes match the manifest. Each archived body equals its original after the
  documented link transformation. Original pending checks remain pending.
- Documentation: all **312 relative links across 60 Markdown files**, local heading
  anchors and Markdown fences pass. The nine replaced active documents shrink
  from 3,400 to 541 lines, with full originals retained. Whitespace review passes.
- Source review: no runtime catalogue data, oracle fixtures, formula implementation,
  preference format or account-identity protection changed. Renamed tests retain
  their previous assertions; the shared reader preserves the extracted sampling.

No live client/kills, Nex capture, contribution, banking-safe session retention,
time-estimate validation or Plugin Hub acceptance is claimed.
