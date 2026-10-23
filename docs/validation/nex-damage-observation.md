# Nex damage callback follow-up validation

23 October 2026. Build **0.29.0** follows PR #51 at exactly
`feb439bdc93278e05b08844a0ab941f5593adf87`; underlying PR #50 is
`f02028dd0af74e358e69e8b268bdcae64c669761`. Main checked at `7f68d0e`.
[Research, source revisions and decisions](../research/nex-damage-observation.md).
The 0.28.3 live report's coverage cause remains unresolved; no new gameplay
or dynamic client object-reuse observation is claimed. Development inspection
of the resolved 1.13.2 binary confirms a mutable slot-reuse path; the research
record identifies its checksum and exact evidence boundary.

## Verification scope

Focused regressions cover equal-damage/multiple same-tick deliveries, explicitly
replayed deliveries, synthetic mutable reuse with frozen amounts/forms, own/other/
heal/unclassified/zero/invalid payloads, non-NPC/outside/view/untracked guards and
post-death exclusion. Additional cases check object/observation overflow without
damage loss, counter saturation without capture capacity, smoke/next-attempt
clearing, immutable snapshots, nullable projectile actors, exclusions and no
ranged match from UNKNOWN. JSON/UI source agreement and explicit unavailable
whole-kill contribution are checked.

PR #51's earlier three fixes remain unchanged. Existing regressions exercise
1,200 HUD-enabled ticks with diagnostic overflow, replacing an already populated
plan's saved impact/supply confirmations, and Soul Split/Deflect Melee separation.
All three passed again in this correction round; their PR #51 review threads
were marked resolved on 23 October after verification.

The long production clipboard test still parses a single 1,500-tick capture,
preserves 370 useful callbacks and omits only 1,495 identical polls, independent
of page 51. Maximum changing-state/callback bounds, allowlist redaction,
pending/hidden-state, lifecycle and clipboard regressions remain active. Native
DEV1 checks the Unverified headline at fonts 12/20 and includes it in the 225 × 600
Encounter viewport; full-height Diagnostics and reports retain observed sums.

Required commands, run sequentially using the prepared JDK 11 environment:

```sh
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --no-daemon
./gradlew test build --no-daemon
```

Both final commands passed sequentially on 23 October using **JDK 11.0.32.1 /
Gradle 8.10**. The pinned run resolved **RuneLite 1.13.2**; the normal
`latest.release` run resolved **1.13.3**, independently verified with
`dependencyInsight`. Both executed **410 tests / 47 suites, zero failures,
errors or skips**. DEV1 passed **14 scenarios / 1,137 checks**, generating
**244 PNGs**, including **64 native 225 × 600 viewports**.

The final Encounter viewports/full-height views and expanded Diagnostics were
visually inspected at fonts **12 and 20**. The Unverified headline and observed
damage scope are readable; ordinary equipment results and controls remain.
[Selected native previews and provenance](images/nex-damage-52/README.md) retain
the unmodified viewport PNGs. Full-height output stays in the CI artifact.
All **26 generated copied-report JSON blocks** parsed with schema, coverage,
source and retained/exported/omitted-count checks. Existing production Copy,
redaction, page independence and clipboard fallback regressions passed.
Changed-document relative links and `git diff --check` pass.

Early runs caught stale version/ratio assertions and a new preview assertion
using the wrong Swing component type; those were corrected before the final
commands. The final callback-limit regression also checks that the last omitted
projectile count survives capture clearing. Prior 403-test / 14-scenario results
remain scoped to 0.28.3.

Delivery is tracked by [issue #52](https://github.com/PetterHL/ironpath/issues/52)
and its focused follow-up PR. That PR records the exact delivered commit,
current-head CI run and artifact links. No raw live report or bytecode dump is
included in Git.

## Next live check

1. Load the identified build; enable observation before the smoke start. Run one
   ordinary Trio MP2 kill through completion. Copy report once from Diagnostics;
   event-page navigation is unnecessary. Keep the original report outside Git.
2. Check build/adapter/schema, `captureTruncated`, counter `atLimit`, callback/
   export counts and omissions. Inspect NEX and each minion independently:
   entry sums → first rejection boundary → own/other/heal/unclassified → retained/
   omitted impacts. Counters include post-death callbacks and overlapping reuse
   diagnostics; compare measured classes with recorded damage, not entry totals.
3. Inspect repeated-object counts without assuming they explain the former live
   loss. Check callback starvation versus target/view/lifecycle exclusions versus
   unknown classification and observation overflow. No single result establishes
   repeated-session coverage or server contribution.
4. Inspect flight `source` and callback exclusion counts. UNKNOWN remains unknown;
   no ownership from ID/proximity/animation. Compare delayed ordinary/special/zero
   events and gear switches only as unvalidated candidate links.
5. Bank, return for the next kill, and exercise an arena reload. Confirm frozen
   report selection and 50-record/reset/identity boundaries. Separately compare
   original energy/DC callbacks to settled readings, including spending after full
   energy; DC proc/expiry and full-energy mechanics require their own evidence.

Whole-kill contribution remains unavailable. No merge, Plugin Hub publication,
automatic recommendation, consumption inference or N4/time claim.
