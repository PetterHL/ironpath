# N3-ADVICE provisional Fang equipment preview

25 October 2026. Development build **0.32.0**, based on integrated main
`2018144` / PR #55. The user explicitly authorized implementing N3-ADVICE before
the pending controlled mapping check. This delivers a **provisional preview**;
it does not validate Fang execution, encounter windows or observed target stats.
The controlled [N3-ACTION procedure](nex-action-evidence.md#short-controlled-client-check--pending)
and captured Nex-window acceptance remain pending.

## Contract

- `NexFangAdvice` runs on the existing guarded post-kill worker, using the frozen
  account/preferences/catalogue and `NexActionTimeline`. No new live reads,
  persistence, plugin dependency, declaration form or live coaching is added.
- Each paired result uses identical action indices and the captured Fang ID,
  stab style, prayers, visible levels and five-tick tempo. The raw action sample's
  role remains unclassified; only the gated comparison derives a melee role.
  Distinct input/target/phase/window groups remain separate. Their gains must not
  be combined or extrapolated to a whole kill.
- Provisional attack inferences are considered independently of damage landing.
  Zero/missing/ambiguous impacts never select a better-looking sample. Rejected,
  unreconciled and ambiguous attack signals have explicit exclusions. Held
  animations, cooldown availability and interaction alone never create attacks.
- Require normal Nex form/no overhead, known Smoke/Shadow/Blood/Ice phase and
  explicit NONE/FEAR_THE_SHADOW window. Unknown windows, minions, Zaros,
  siphon/special windows and pre-start/end-grace actions stay out. Window labels
  and the base-stat ordinary-damage interpretation are still provisional.
- Reuse `NexOwnedEvaluator`'s complete-package search, `NexPackageFactory`,
  `MeleeModel` and `SpecAttackModel`. The weapon cannot change. Preserve
  Lightbearer, locks, equip levels/unlocks, exact variants, hands and carried
  switch-space checks. Bank alternatives are next-trip preparation. Hypothetical
  results replace one catalogue item on the selected owned baseline; they do
  not establish ownership, acquisition priority or a packed trip.
- The owned baseline maximizes supported **uncapped ordinary offence** within
  the bounded search, not HP-dependent damage or global account value. All
  supported pairwise alternatives, including zero/negative changes, remain
  selectable. Unknown/unmodelled alternatives and defensive/healing/set effects
  outside the existing offence model cannot establish an optimum.
- Incomplete/truncated captures withhold numerical previews. The existing N2
  COMPLETE-only gate and all full finite-plan guards remain. Sixty-four distinct
  comparison groups bound additional worker work; remaining candidates receive
  WORK_LIMIT exclusions. The existing per-search work limit remains explicit.
  Cancellation returns no preview and existing publication guards reject stale work.

## HP uncertainty

No captured HP reading narrows pre-impact HP in this delivery. In particular,
the preview never turns a stale bar, midpoint or post-impact HUD value into exact
earlier HP. Instead each ordinary-damage pair is evaluated over **all positive
integer pre-impact HP** under the explicitly assumed base-stat, untransformed
ordinary window. Target Defence 260 and the reviewed base bonuses come from the
existing `nex-base` catalogue target; no observed defence reduction is inferred.
Unsupported phase transforms are excluded, not approximated by an arbitrary cap.

The existing discrete damage kernel supplies each setup's probability mass.
For integer HP `h`, `E[min(D,h)] = sum(k=1..h) P(D>=k)`. Evaluate the **paired
difference at every integer** through the greater damage maximum. Beyond both
maxima both expectations are constant, so no other extrema exist. Interior
breakpoints matter: endpoint-only subtraction and subtraction of independent
before/after ranges are insufficient. No HP probability distribution is assumed.

The displayed interval is an expected-damage difference across feasible HP
states, not a confidence interval or realised random-damage range. Repeated
identical-input attacks multiply this per-attack interval. This is a conservative
outer bound; it does not claim every HP combination forms a feasible kill
trajectory. Expected damage never updates a subsequent HP state.

## Presentation and frozen evidence

Compare setups shows **Fang equipment / Provisional · base stats**, the changed
item/package, damage interval, sample count, fixed tempo and gear origin. A
selector retains zero/negative alternatives. Fang details opens the existing
Diagnostics with coverage, exclusions, selected pair and assumptions. No new
top-level section or wizard is introduced. Selection and report visibility use
the existing scope, retained-record and view guards.

`nex-evidence-5` preserves all schema-4 observations/action-clock fields and adds
`fangAdvice`: model `nex-fang-advice-1`, unvalidated status, blockers, per-action
exclusions, same-sample indices, both complete slot maps, damage bounds/accuracy,
base defence roll and knowledge version. ACTION_SIGNAL rows have explicit
zero-based `actionIndex` references. This is independent of the UI selection and
event page. No names, account IDs, target fingerprints or raw chat are added.
The existing whole-export size guard emits exportComplete=false rather than
partial evidence. Frozen results survive later kills/account reads, and reset,
identity/world changes, eviction and stale-worker guards still hide/reject them.

## Verification

Focused tests cover all-positive-HP paired bounds against independent exhaustive
enumeration (including an interior extremum), zero/missing/ambiguous hits,
negative alternatives, fixed weapon/style/tempo, carried/bank/hypothetical gear,
unknown/special/unsupported context, missing equipment, locks, unknown resource
inputs, truncation, incomplete attempts, cancellation and immutable outputs.
Existing DEV1 `nex-n3-plan` exercises public callbacks through the real
worker/panel/report at fonts 12 and 20, including frozen preview retention.

Required final commands, run sequentially with Temurin JDK 11.0.32.1+1 / Gradle 8.10:

```sh
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --no-daemon
./gradlew test build --no-daemon
```

Both final commands **passed**. Pinned RuneLite **1.13.2** completed in **3m 0s**;
normal `latest.release` resolved to **1.13.4** and completed in **2m 25s**.
Both ran **439 tests / 49 suites**, with no failures, errors or skipped tests.
The pinned offline command passed all **14 DEV1 scenarios / 1,171 checks /
248 PNGs**. Gradle dependencyInsight independently confirmed the normal version.

Inspected the final Fang full-height images at fonts 12/20 and native 225 × 600
viewports. Text, selectors and details controls fit, including enlarged fonts.
Parsed both production copied reports: schema 5, one included provisional Fang
action, **94 complete-package pairs**, including **14 negative alternatives**,
and resolving explicit action indices. Existing page-independent copy, next-kill
freezing, reset/identity/lifecycle and stale-publication checks pass. Whitespace
and relative Markdown file targets pass. Verified implementation commit:
`5d7be55`. Build reports/PNGs remain generated artifacts outside Git.
CI has not run for this delivery: automatic approval review rejected the GitHub
push because explicit destination authorization was absent. The local branch is
ready; push and PR creation await explicit user authorization.

The genuine Fire cape preview sprite (6570) was retrieved from RuneLite's item
icon service on 25 October; its hash/provenance is in the retained sprite manifest
and existing Jagex notice applies. No external code or dataset was copied.

Controlled client mappings, target/window/stat agreement and player usefulness
remain **unperformed**. All eight automatic full-plan gaps remain; finite
spec/DC/supply recommendations, whole-kill contribution, seconds saved, N4,
merge and Plugin Hub publication are outside this delivery.
