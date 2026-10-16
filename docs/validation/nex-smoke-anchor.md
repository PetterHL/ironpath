# Nex smoke anchor and dying/end correlation

15 October 2026. Development build **0.16.5**, adapter **nex-public-4**.
Knowledge `n2-owned-1`, catalogue 14 and preferences 3 are unchanged. This replaces
the start/end policies in the earlier validation reports, whose original results
and pending checks remain historical evidence.

## Corrected capture contract

The [reviewed public reference](https://github.com/Worley03/nex-droprate-calculator/blob/48217ba0e2915e9553c922d73034c5f2866fc4a7/src/main/java/com/nexdropratecalculator/NexDroprateCalculatorPlugin.java)
uses the smoke announcement for start and barrier 3→2 for completion. Active arena
value 3 is insufficient to locate combat start, and a dying NPC is not the same
instant as arena completion. Requiring an arena-entry transition near smoke and
a symmetric five-tick dying/end match was an unsupported restriction.

The new measured interval is **observed Nex smoke announcement → tracked Nex
death/dying**. It does not include waiting before smoke or the subsequent death
animation. It is not the game's official kill timer and does not import a fixed
offset. A missing arena-entry transition is diagnostic only.

An initially unknown capture can anchor on actual smoke only before any later
phase/subphase, positive observed damage or unclassified hit. Earlier states are
retained separately with their original timestamps/phases. They remain inspectable
but do not enter fight coverage, damage, cohorts or N2 equipment scenarios. The
actual smoke callback supplies its current state; no prior state or phase is
backfilled. Anchoring does not add another attempt or restart buffer/time/event
limits. Duplicate smoke cannot move the start or erase subsequent evidence.

Dying must be corroborated by arena completion within the existing 20-tick bounded
wait. Reverse ordering retains a shorter five-tick allowance. These are explicit
adapter policies, not universal guarantees of server timing. Both end signals,
all phases and continuous pre-death sampling remain necessary. Missing/late
signals, death/teleport, spawn/view replacement and hard identity boundaries still
censor or clear as appropriate. Old excluded summaries are never promoted.

## Independent replay and safeguards

A synthetic public-API sequence starts with arena value 3, receives smoke later,
observes all phases and a dying transformation, then receives arena completion
after the former five-tick limit. It failed on the old implementation and now
produces one complete smoke-to-dying interval after the correction.
This is an independently authored fixture, not a recording of private gameplay.

Coverage also checks both callback orders, source event values, same-tick states,
wrong-actor/duplicate/late smoke, prior damage/later phases blocking a reset,
unobserved start, missing phases/samples/end, directional end limits and timeout,
preserved immutable pre-smoke states, shared memory/time bounds, stale/reset
history, local death without repeated attempts, N2 exclusion of pre-smoke states,
and narrow/enlarged-font history navigation. Existing arithmetic, compatibility,
worker, preference, banking and account/world regressions remain required.

## Verification and remaining client checks

JDK 11 / RuneLite 1.13.2: `XDG_CACHE_HOME=/tmp/ironpath-cache ./gradlew test build --no-daemon`
**passed: 188 tests across 27 suites, zero failures/errors/skips** (32 s).
JAR assembly, `git diff --check` and changed-document local links pass. Existing
Gradle deprecation notices remain. Focused adapter/capture/UI/lifecycle/owned
checks passed before the normal build. No complete updated-client capture,
calibrated game timer or player acceptance is established by synthetic replay.
No raw account dump, screenshot or performance trace is added to this report or
source tree.

Update/restart the current N2 branch and enter before a fresh smoke announcement.
After normal completion, inspect measured smoke start, death/dying, arena end and
coverage. Verify the completed result and owned comparisons survive a bank visit.
If an attempt remains incomplete, its bounded signal details identify the missing
evidence; do not loosen another requirement without inspecting that evidence.
