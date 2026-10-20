# Nex observation verification and client checks

20 October 2026. Build **0.28.2** follows open PR #50 at `f02028d`; checked main
was `7f68d0e` (0.23.3). The focused PR targets `n3/decision-support` to keep the
dependency separate. No merge or Plugin Hub publication is authorized here.
[Evidence/support contract and sources](../research/nex-observation.md).

## Implemented

- Spawn-bound, timed HP source/validity: fresh server-agreed HUD candidates,
  justified actor-bar intervals and unknowns. No validated Nex HUD or automatic
  per-impact HP is claimed.
- Frozen pilot animation/package candidates, unique projectile observations,
  own zero/positive hits, later-state reconciliation and ambiguous matching.
- Smoke-callback resources, exact initial food units/potion doses, direct levels,
  original resource payloads plus later server state and raw Death Charge signals.
- Per-result links to existing conditional state comparisons and an optional
  recorded-tick draft through the existing N3 editor/worker. All scenario
  confirmations remain explicit. No new combat formula or SpecTimeline rule.
- Bounded in-memory Diagnostics/report fields with the existing privacy,
  freezing, bank/reload, reset, identity and eviction guards.

## Tested offline

Targeted tests cover every integer HP at six maxima and four scales (see
NexObservationTest), 483–517 at ratio
15/30 with maximum 1000, invalid inputs, same-tick HUD binding, subsequent tick
expiry, target/form switches, disabled/hidden HUD, duplicate definitions, server changes,
cached HUD rerendering without a fresh variable event,
delayed zero damage after a gear switch, repeated projectile callbacks, callback
payloads preceding getter updates, Death Charge end-effect deduplication and late
post-death resource evidence confined to the existing completion grace, smoke
reanchor, next attempt, buffer bounds, unchanged immutable launch gear, starting
stock versus inventory loss, report allowlisting and paused-history hiding.

DEV1 `nex-n3-plan` now dispatches a real synthetic plugin animation callback,
checks frozen launch evidence and settled inputs, server-agreed start energy,
recorded draft ticks with unchecked readiness, and frozen observations after a
new kill. Existing arithmetic, minion/Zaros, finite resource, lifecycle, privacy,
pending worker, reset and clipboard cases remain active in all 14 scenarios.

Run sequentially with JDK 11 and the prepared environment:

```sh
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --no-daemon
./gradlew test build --no-daemon
```

Initial 0.28.0 commands passed sequentially on 20 October with JDK 11.0.32.1, Gradle 8.10
and resolved RuneLite 1.13.2: **393 tests / 46 suites, zero failures/errors/skips;
14 DEV1 scenarios / 1,121 checks; 240 PNGs**. All 62 exported viewports are
225 × 600. The normal build resolved the same classpath and reused the successful
test/build outputs; it does not establish compatibility with a newer release.
Native recorded-tick forms, ordinary/expanded Diagnostics and full-height output
were inspected at fonts 12/20. Changed-document links and `git diff --check` pass.

The PR records the exact delivered commit and current CI/artifact links.
Dependency-cache misses during initial offline attempts are setup failures, not
executed tests. Synthetic success does not validate RuneLite event mapping or
player usefulness.

## PR #51 review corrections (0.28.1)

- Move HP-only changes into the shared 2,048-observation bound. Worn-state
  compaction still distinguishes targets, forms, phase/window and player state.
  Diagnostic overflow is explicit and cannot exhaust the main state/event bound.
- Recorded-tick import clears general, fixed Zaros, supply and all saved impact
  confirmations. Removed impact rows are dropped; remaining draft values cannot
  restore a confirmation when loaded. Pending old results are invalidated.
- Match launch counts/tick imports using the selected Zaros overhead, consistent
  with existing comparison-state deduplication; HP differences do not split it.
- Add worker-built, allowlisted 12-event pages shared by Diagnostics and Copy
  report. Relative time/order, local target references, form and bounded candidate
  links expose middle events, with explicit capture/window/link omissions.
- New regressions exercise 1,200 continuous HUD ticks through diagnostic overflow,
  import into a populated confirmed plan, Soul Split/Deflect separation, event
  pagination/correlation/privacy and selection/report coherence during refresh/bank.
  DEV1 captures event selection and the matching copied report at both fonts.

Both required builds pass with JDK 11.0.32.1 and Gradle 8.10:
**399 tests / 46 suites, zero failures/errors/skips; 14 DEV1 scenarios /
1,131 checks; 244 PNGs**, including **64 native 225 × 600 viewports**. The pinned
review uses RuneLite 1.13.2; the normal `latest.release` build resolves RuneLite
1.13.3, verified with `dependencyInsight`. Both versions pass the full test suite. The event selector, report selection and full-height output were inspected
at fonts 12/20; changed-document links and `git diff --check` pass. The PR records
the exact delivered commit and its current CI/report artifact links.
Live acceptance has not been performed in this correction round.

## One-copy client-test follow-up (0.28.2)

- Every copied report includes a bounded overview of all retained target/HP-source
  groups, first/last labelled readings, observed forms and candidate animation
  counts, regardless of the selected event window. It is capped at 7,000 characters
  and twelve animation groups with explicit omissions; report size remains 24,000.
- Fixed HUD variable/script/accepted/first-failed-guard counters distinguish no
  observed callbacks from rejected bindings. Counters saturate at 65,535, never
  spend the main event/state or observation bounds and clear at existing boundaries.
- Reports identify the running RuneLite version. Native Diagnostics keeps the
  current event window as optional detail and describes the one-copy summary.
- Source recheck confirms the generic RuneLite channel, not Nex availability.
  An overhead actor bar remains a separate quantized source; zero HUD candidates
  alone neither establish a fault nor require changing a setting. HUD acceptance
  remains open. No private captures or performance history are added to Git.
- Synthetic tests cover rejected/accepted HUD counters, saturation/reanchor/next
  attempt, middle-minion readings in the default report, privacy/size/hidden state
  and the production Copy report action at fonts 12/20.

Both required commands passed sequentially with JDK 11.0.32.1 / Gradle 8.10:
**401 tests / 46 suites, zero failures/errors/skips; 14 DEV1 scenarios /
1,133 checks; 244 PNGs, including 64 native 225 × 600 viewports**. Pinned review
uses RuneLite 1.13.2; the normal build resolves 1.13.3, verified separately with
`dependencyInsight`. Native event/report controls and full-height output were
inspected at fonts 12/20. Changed-document links and `git diff --check` pass.
The PR records the exact delivery commit and current-head CI/artifact links.

## Client procedure — still open

Use build 0.28.2 with RuneLite 1.13.2. For the next diagnosis, play one ordinary
pilot kill and use **Diagnostics → Copy report now** once after completion. Leave
the event-window selector unchanged: the overview covers all retained target/HP
sources and HUD callback/rejection counters. Note which HP displays were actually
visible. No numeric HUD display is presumed to exist for Nex.

For isolated follow-up actions, **Calculation details → Observation events**
provides optional detailed rows; copying also includes the selected window.
Inspect **Resource readings and rules** as needed. Reports export only on request.
Select **All states** in owned comparisons to inspect unsupported phases too. Preserve a report before Reset; no report is
saved or uploaded automatically. Tests must be repeated for reliability.

| Check | Exact action and expected evidence |
|---|---|
| HP/forms | Inspect ordinary, Soul Split and Deflect forms and note the actual visible HP display. Establish whether a separate integer HUD exists for each form before comparing its values. Accepted bindings must be BOSS_HUD / AWAITING_CLIENT_VALIDATION; otherwise ACTOR_BAR bounds or UNKNOWN remain valid evidence and HUD availability stays open. No other plugin is required. |
| HP/target changes | Switch Nex → each vulnerable minion → Nex, lose interaction briefly, and, if a native HUD is available, disable/enable it. A minion must never inherit Nex HP. Missing updates must fall back to ACTOR_BAR bounds or UNKNOWN. Inspect same-tick damage/healing separately; do not label the captured value pre-impact HP. |
| Ordinary attacks/misses | For each actually carried pilot weapon, count a few isolated attacks including zero hits. Compare candidate animation/weapon/target and one-versus-multiple/unmatched impact counts against independent observation. Unrecognized animations must remain UNCLASSIFIED. Test actual ZCB ordinary/spec animations separately; they are not differentiated automatically. |
| Delayed hit/gear switch | Launch a bow/crossbow attack, switch to melee before impact, and inspect both states. The candidate must retain the launch callback's ranged package. Reconciliation may show changed inputs; the later hit must never be relabeled as a Fang launch. Repeat with overlapping owned damage/target switches; ambiguous matches must stay candidates. |
| Concurrent resources | Observe one isolated special, then a special near natural restoration and near a Death Charge restoration. Original ENERGY callback values/order and server readings must remain separate. A 50%/75% net change must not create an inferred spec count. |
| Death Charge | Observe cast, two separate eligible-death situations and a cast that expires unused; also enter with the spell already active. Inspect active 2/1/0, cooldown, upgrade raw/client/server values, cast/end effect IDs and tracked deaths. Supply independent action/tick notes. End effects and charge loss must remain unlabeled as proc/expiry until this mapping is corroborated. |
| Full energy | With Lightbearer fixed, spend after waiting at 100%; count ticks to the next natural +10%. Repeat after reaching 100% naturally and through Death Charge. Exclude additional energy sources from each isolated case. Compare observed callback offsets; report which clock behavior occurred. The existing finite scenario model has not been changed. |
| Stock/consumption | Compare smoke-callback food units/potion doses, ammo/quiver/pouch quantities and actual visible levels. Try food/combo/potions, a rejected click, an item drop and inventory movement. Initial stock must remain frozen; none of those losses/clicks may create confirmed consumption. Directly observed drained/restored levels must still be available independently. |
| Bank/reload/next kill | Complete a kill, keep its report selected, delay opening the bank, return and complete another kill. Verify retention and frozen observations; inspect an arena-only reload. No old HUD/projectile/active-capture evidence may enter the next attempt. |
| Hard clear | Reset, logout, change world/profile and disable tracking/plugin as applicable. Evidence and reports must clear/hide under the existing rules; late events must not restore them. |

Record build, action, target/form, independent expected count/time and the bounded
report's values or explicit omissions. Report missing HUD coverage, unknown source
actors and insufficient diagnostic detail as unresolved, without supplying manual
confirmations to make a test pass. No particular mapping becomes accepted merely
because one clean example agrees.

## Result boundary

Available now: automatic state/resource capture, inspectable attack/HP candidates,
independent conditional ordinary comparisons using observed visible levels, and
less typing for explicit plans at the recorded callbacks. Still open in the
client: verified launches/HP/Death Charge semantics, full-energy timing and stock
freshness. Opportunity schedules, confirmed consumption, dynamic Zaros mechanics,
ordinary reflection, healing, personal contribution and seconds saved stay gated.
