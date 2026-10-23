# Nex observation verification and client checks

23 October follow-up: build **0.29.0** retains this observation delivery and adds
[callback/source corrections](../research/nex-damage-observation.md) and
[new validation](../validation/nex-damage-observation.md). Whole-kill damage
contribution is Unverified; export advances to `nex-evidence-2`. The results below
retain their original revision scope.


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
provides optional detailed rows; build 0.28.3 copies the whole retained evidence
projection independently of that window.
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

## Whole-capture clipboard follow-up (0.28.3, 22 October 2026)

Copy report now includes the entire retained allowlisted evidence projection as
`nex-evidence-1` JSON, independent of the selected UI page. It preserves explicit
callback timing/order, local targets, forms/HP source/validity/time, immutable
launch/settled states, projectiles, zero/positive impacts, resource callbacks and
Death Charge evidence. Main/pre-start samples retain tick timing separately.
Only unchanged resource polls are compacted; state tables deduplicate payloads.
The old narrative budget cannot remove event records. Capture/privacy/lifecycle
bounds and the clipboard fallback remain; no new inference or recommendation.

The production-Copy regression uses 1,500 ticks / 1,865 retained callbacks, with
120 animation candidates, 120 projectiles, 120 impacts (60 zero), three HP readings,
a middle minion, a temporary energy change/reversion and an end-grace DC callback.
One copy parses all 370 useful callbacks; exactly 1,495 unchanged polls are omitted.
Selecting UI page 51 produces identical clipboard text. Private sentinel keys,
item names, target identities and unrelated inventory stock remain absent.
Dense 2,048-event coverage and existing hidden-session/privacy/clipboard tests
remain active. DEV1 checks page-independent Copy at both fonts.

Both required full commands passed with JDK 11.0.32.1 / Gradle 8.10:
**403 tests / 46 suites, zero failures/errors/skips** on pinned RuneLite 1.13.2
and normal latest.release RuneLite 1.13.3 (verified with `dependencyInsight`). DEV1: **14 scenarios / 1,133 checks; 244
PNGs**, including **64 native 225 × 600 viewports**. Full-height observation
Diagnostics and native viewports were inspected at fonts 12/20. Synthetic Copy
reports at both fonts parse as JSON; page selection leaves their evidence unchanged.
Changed-document links and `git diff --check` pass. The PR records the delivered
commit and current-head CI/artifact links.
Live RuneLite/OS clipboard acceptance remains open; synthetic tests do not validate
Nex HUD/weapon/DC mappings or infer attack opportunities, consumption or procs.

## Live one-copy check (22 October 2026)

One user-supplied live report from **IronPath 0.28.3 / RuneLite 1.13.2**, code
`b46df204f9a24d47a9097217b8647145c8282acc`, was parsed and inspected in this session.
The raw report remains outside Git. These are aggregate diagnostic findings from
one kill, not a public capture fixture or validation of combat attribution.
No independent action/video log was supplied; the kill has no recorded timestamp.

### Export result: passed for this report

- Status READY, outcome COMPLETE; measured interval **578 ticks / 346.8 seconds**.
  The report covers Smoke, Shadow, Blood, Ice and Zaros, and all four minions.
- The single pasted report contains a valid `nex-evidence-1` JSON block with
  **1,444 retained observations**, **1,284 exported events**, **160 unchanged
  polls omitted**, **866 main samples**, **5 pre-start samples** and **578
  deduplicated states**. Callback order and all referenced state indices validate.
- `captureTruncated=false`, `correlationLimited=false`, `exportOmissions=0` and
  `exportComplete=true`; narrative fields are not size-truncated. The overview
  omits two animation groups by its own bound; their events remain in the JSON.
- Evidence includes retained target/form changes, actor-bar/unknown HP, weapon switches,
  launch state, energy/DC callbacks and completion-grace resources through tick
  590. Measured-end energy is 200 tenths-percent; a separate tick-579 callback
  records 300. That tail does not extend the measured interval or replace its
  final energy reading.
- This demonstrates one-report ingestion without navigating event pages. It does
  not independently establish whether the native OS clipboard API or selectable
  fallback supplied the pasted text, or establish repeated-run reliability.

### Observation findings: follow-up required

| Channel | Direct evidence | Limit / status |
|---|---|---|
| HP | 162 ACTOR_BAR and 90 UNKNOWN health events; Nex ordinary/Soul Split/Deflect Melee forms and all four minions are represented | Actor-bar intervals remain conditional on their reviewed maximum/semantics; no exact impact-time HP claim |
| Integer HUD | 233 variable updates, 241 script callbacks; first failed guards: 136 NO_FRESH_VARIABLE and 105 WIDGET_HIDDEN; zero accepted | Callbacks exist. This does not prove that Nex supplies a usable integer HUD or identify a broken setting |
| Projectiles | 379 retained projectiles; every `localSource` is false | In this adapter, known non-local sources are filtered before capture; false therefore represents an unresolved/null source here, not an observed other player. No local projectile was identified; ranged correlation remains unsupported |
| Hitsplats / impacts | 10 own impacts, including one zero, sum to 167; reported local/total damage is 167/636 | Strong coverage inconsistency: Nex bar bounds alone move from 3286–3314 to 29–57, before accounting for minions. The 26.26% observed ratio is not accepted as whole-kill damage contribution. Root cause is not established |
| Animations | 44 bow ordinary, 41 Fang ordinary and 6 Voidwaker special candidates; 72 other callbacks remain UNCLASSIFIED | These are 163 animation callbacks, not 163 confirmed attacks. Six special-animation candidates coincide by tick with six -500 energy callbacks; causal attribution remains unvalidated |
| Gear | Frozen states include Twisted bow, Fang and Voidwaker; every exported state has Lightbearer | The observed state evidence is available; launch/server action-order and package-effect acceptance remain separate |

The six -500 energy changes are at ticks **4, 124, 134, 266, 504 and 508**.
At tick 504 the report preserves both 900→1000 and 1000→500 in callback order.
No special count is inferred from net energy changes or hypothetical schedules.

Death Charge evidence is also inspectable from the same copy:

| Cast-effect tick | Active client/server reading | Next recorded active zero | Subsequent/nearby tracked minion death |
|---|---|---|---|
| 109 | 2 / 2 | 209 | Umbra 209 |
| 223 | 2 / 2 | 323 | Cruor 348 |
| 370 | 2 / 2 | 470 | Glacies 471 |

There are three cast-effect records, no end-effect records, no observed active
2→1 transition and no +150 energy callback. These observations establish neither
an eligible proc nor the cause of charge loss. Do not relabel the zeros as expiry,
claim a failed proc, or turn this sequence into tactical advice.

**Acceptance boundary:** the complete one-copy export works on this supplied
live report. `COMPLETE` establishes correlated encounter completion, not complete
hitsplat/projectile attribution. Damage coverage and local projectile attribution
remain unresolved. HUD availability, animation mappings, DC semantics and controlled
full-energy/stock/lifecycle tests retain their existing acceptance requirements.
No numerical model, recommendation or runtime behavior was changed by this review.

### Proposed separate issue

Suggested title: **Validate Nex hitsplat coverage and projectile source evidence**.
The active follow-up is tracked in [TASKS](../../TASKS.md); no new issue is created
by this documentation update.

- Reproduce why observed local/total damage coverage is so low. Add bounded,
  allowlisted counters at the public-event and filtering/classification boundaries
  to distinguish missing callbacks, rejected targets/views, duplicate handling and
  hitsplat classification. Do not guess a root cause from this report alone.
- Preserve source UNKNOWN separately from LOCAL/OTHER in projectile diagnostics;
  validate the supported public API's source availability and callback timing.
  Do not assign ownership from projectile ID, nearest animation or likely gear.
- Use focused regressions plus controlled client examples with independent
  attack/hitsplat notes, including positive/zero hits, delayed projectiles and
  target/gear switches. A candidate correlation alone cannot establish coverage.
- Make any damage-share presentation/support correction explicit if whole-kill
  coverage remains unverified. Preserve independently useful timing, HP and
  resource evidence; do not replace missing measurements with modeled damage.
- Retain privacy/allowlists, memory and lifecycle bounds, unknowns, one-copy export,
  UI pagination and existing arithmetic. Run the required tests/DEV1 after fixes.
  HUD/DC semantic acceptance can be tracked separately; it is not silently passed.
