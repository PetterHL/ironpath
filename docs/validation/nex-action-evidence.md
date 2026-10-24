# N3-ACTION validation

24 October 2026. Development build **0.31.0**, from main `9a2b592` after the
user-authorized merge of PR #54. This delivers code review, **not mapping
promotion**. N3-ADVICE remains separate. No automatic comparison, full-plan
recommendation, live coaching, proc/expiry, consumption quantity, exact HP or N4
claim is enabled.

## Frozen contract

- Adapter `nex-public-10`, observations `nex-observations-6`, action signals
  `nex-actions-1`, provisional clock `nex-fang-clock-1`, JSON `nex-evidence-4`.
- `NexObserver` captures local animation/interaction callbacks, settled game-tick
  animation/frame and movement-pose readings, and allowlisted GAMEMESSAGE/SPAM
  success messages. Each row freezes tick/cycle/order, source, target spawn,
  target form/HP provenance and the callback's equipment/style/levels/prayers.
  No menu click is treated as execution. Player/NPC/console chat cannot create
  food or cast signals. Existing Nex phase parsing is preserved.
- The message allowlist recognizes shark and karambwan, generic/other `You eat ….`
  messages as unknown food delay, and the exact normal/upgraded DC success text
  in the pinned source review. Tags are stripped before classification; no raw
  text, name or arbitrary food label is retained/exported. Equal deliveries at
  the same tick remain separate; neither message equality nor event-object
  identity is a reliable delivery deduplication key.
- The separate 4,096-row action bound cannot censor main capture. Arena pre-start
  context retains at most ten ticks / 128 rows. Capacity loss is explicit.
  Smoke reanchoring prunes older context without resetting the retained clock;
  missing/unknown earlier readiness remains unknown. Identity/world/reset/scene
  clearing uses existing observer/session guards. No new data is persisted.
- `NexObservationReview` derives the timeline on the existing worker. Ordinary
  Fang IDs 26219/27246, stab buttons 0/1/3 and animation 9471 can yield a
  **provisional** inference after matching settled target/form/phase/window and
  package/level/prayer/style inputs within one tick. A five-tick clock comes from
  the existing catalogue. A settled frame decrease in consecutive matching Fang
  states is a separately labeled provisional restart, not a validated attack API.
- A continuing animation without a restart never creates periodic attacks.
  Blocking/idle/interaction signals alone never create attacks. Moving-pose,
  special-selected, unsupported style/weapon, missing target/state, changed
  inputs, early/duplicate callbacks and missing ticks have explicit reasons.
  Weapon/style swaps invalidate modeled readiness. A separate sample-gap enum
  excludes unknown phase, unsupported forms and special/Zaros windows from the
  future ordinary sample without changing the independently inferred clock. Even
  the ordinary-window case retains MAPPING_UNVALIDATED. Cooldown availability does
  not establish an attack, legality or a wasted opportunity.
- Known food-message delays add three ticks for shark and two for karambwan to
  a known outstanding cooldown; eating at or after readiness does not start a
  new cooldown. Unknown food invalidates the clock. Unknown initial readiness stays
  unknown until a provisional attack anchors the clock. Attack plus food at the
  same tick is ambiguous in either callback order, because callback order is not
  server action order. Full-HP eating can be silent; no message cannot prove no
  consumption. Unmapped food/drink mechanics remain unsupported.
- Every raw signal has its interpretation, reason, readiness and before/after
  ready-tick in `ACTION_SIGNAL` rows. `settledOrder` links the reconciliation.
  `actionTimeline.impacts` links **candidate** action orders to every retained
  own impact, including zero. Multiple/unmatched/competing links remain ambiguous;
  even a unique temporal candidate is not ownership proof. Amount never selects
  the attack sample. The first-flight projectile contract is unchanged.
- Export shares existing report-local targets, deduplicated numeric states and
  relative times. The timeline is independent of the selected UI page. The existing
  8,000,000-character report ceiling remains: a size failure emits explicit
  exportComplete=false instead of a partial JSON timeline. Consumers must reject it.
  Existing frozen calculation/input/context, historical view, stale publication,
  hidden/reset report and session retention guards remain. A consumer must check
  mapping support, truncation and per-result target/mechanic/HP validity; an
  inferred action or a cast-success signal cannot bypass full-plan gates.

## Automated verification

The focused observer and action-clock scripts cover accepted/repeating/rejected
signals, block/interaction/movement, unsupported styles, settled swaps, food
ordering and additive delays, unknown food/readiness, pre-smoke continuity,
normal/upgraded DC, unrelated chat, misses, multiple/unmatched impacts,
out-of-order evidence, reset and both bounds. Expectations use independent tick
scripts (e.g. attack at 0 → ready 5; shark + karambwan + karambwan → ready 12),
not observed random damage or implementation-generated snapshots.

DEV1's existing `nex-n3-plan` public-plugin replay now adds food/DC messages,
checks exactly one provisional Fang inference despite continuing animation,
exports the timeline through the real worker/report at fonts 12 and 20, checks
page-independent copying and preserves the first timeline across the next kill.
The `nex-incomplete` replay additionally exercises a deliberately aborted short
segment through the guarded worker and real Copy report action, asserting a
provisional Fang row and typed food/DC evidence with no numerical choices or
complete-kill statistics. Incomplete segments with action evidence now receive
worker diagnostics through the existing record/input/publication guards; the
N2 evaluator retains its COMPLETE-only comparison gate.
All fourteen scenarios and existing lifecycle/arithmetic/unknown-state regressions
remain required. Generated reports/PNGs belong to build artifacts, not Git.

Final code revision: [`54105a9`](https://github.com/PetterHL/ironpath/commit/54105a968c6c07d3d0317f0eaca4b87acdc2ed72).
The following verification-record commit changes documentation only.
Local Temurin JDK **11.0.32.1+1**, Gradle **8.10**:

```sh
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --no-daemon
./gradlew test build --no-daemon
```

The final pinned command **passed** in 4m 7s on RuneLite **1.13.2**:
**428 tests / 48 suites**, no failures/errors, and all **14 DEV1 scenarios /
1,153 checks / 244 PNGs** passed. The normal build uses `latest.release`;
[PR #55](https://github.com/PetterHL/ironpath/pull/55) records its final local
result, actual resolved version, current CI run and report/PNG artifact.
The commands run sequentially in each environment. Initial missing-cache/setup
failures are not counted as executed tests; dependencies were resolved through
the configured network/proxy before verification.

Inspected `nex-n3-plan` observation viewports at native 225 × 600 and fonts 12/20,
plus both full-height views. Parsed both production copied JSON blocks: 34 action
signals, one provisional inference at offset 4, shark at 6 and upgraded DC at 7;
all state/target/settled-order references resolve. Copies are page-independent and
the earlier timeline survives the next kill. Parsed both `nex-incomplete` copied
reports as well: 15 action rows, one provisional launch, shark and upgraded DC,
CENSORED outcome and no numerical choices. Inspected its enlarged-font Diagnostics
viewport. Changed patch whitespace and relative Markdown targets pass. Synthetic
verification is not controlled-client evidence.

## Short controlled client check — pending

Use 0.31.0, record the actual RuneLite version, and keep Lightbearer and a known
Fang stab style fixed. Do this only when ordinary gameplay permits. A short Nex
arena segment followed by leaving is sufficient; a completed kill is unnecessary.
The existing observer is arena-scoped; it does not record unrelated NPC practice.

1. Open the arena before smoke so a few pre-start settled ticks are captured.
   Open **Last attempt details** for that segment, then use
   **Diagnostics → Copy report now** (an older complete kill may otherwise stay
   selected). An incomplete attempt
   remains inspectable; wait for its normal post-attempt worker result before
   copying. Check `nex-evidence-4`, `actionTimeline.mappingStatus`, truncation and
   the actual relative event times. Do not annotate any mapping as confirmed yet.
2. Note a small known sequence: two isolated ordinary Fang attacks at least five
   ticks apart, one known zero/miss if it occurs naturally, a short continued
   autoattack sequence, and a period with a block/movement or rejected/out-of-range
   attack intent. Compare each observed attack to ANIMATION/SETTLED evidence and
   provisional inferences. No inferred attack should arise from idle cooldown,
   held animation or interaction alone. Record disagreements, not just totals.
3. While below full HP, eat one shark and one karambwan on distinct known ticks.
   Check the SPAM/GAMEMESSAGE source, kind, exact separate deliveries and the
   provisional +3/+2 changes to an outstanding cooldown; eating after it expires
   must not postpone the next attack. If naturally testing combo/same-tick eating,
   expect ambiguity for a same-tick attack. Check an unsupported food is unknown,
   and keep full-HP silence explicitly inconclusive. Do not infer stock used.
4. Cast the available Death Charge variant once when legal. Match normal or
   upgraded message kind to the known cast and inspect nearby captured active,
   cooldown/effect signals. A normal variant needs a separate eligible account/
   configuration; if unavailable, leave it untested. Do not infer eligibility,
   proc, restored energy or expiry from this check.
5. Copy once after the attempt/exit. Confirm pre-start rows have negative offsets
   and smoke did not reset the clock. Compare callback and settled weapon/style
   around a deliberate switch; changed inputs must be excluded/ambiguous. Keep
   misses and unmatched/overlapping hits in the exported evidence. Clear session
   and verify it cannot export the old timeline.

Record the known action/tick, exported signal/order, interpretation, agreement or
mismatch and version for **each** mapping. All mappings are currently **untested
and provisional**. Correct or narrow failed mappings. N3-ADVICE additionally needs
relevant Nex target/phase/window evidence and its own per-result uncertainty
contract before promoting any ordinary-damage equipment result. No fresh full
kill, raw account dump or persisted chat is required.
