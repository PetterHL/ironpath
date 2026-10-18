# CTX1 — Editable Nex kill context

18 October 2026. Development build **0.21.0**, based on UI1's merged main
`96d91e8912d56548b72c4587e03ee683e43892e5`. Implemented for review; feature merge
and live/player acceptance remain separate. Initial implementation commit:
[`789f69e70991e63a77bcdf334f75fd6fe3c53591`](https://github.com/PetterHL/ironpath/commit/789f69e70991e63a77bcdf334f75fd6fe3c53591).
The delivery also clears the panel's frozen diagnostic-record reference on Reset
and refreshes default-source details after Apply. The delivery PR records its current
head and links that head's CI run/artifact.

## Result and boundaries

The optional **Future kill context** action works before a kill. **Edit kill
context** is available beside the latest completed or already-inspected kill,
including comparison/details and Diagnostics. No arbitrary history selector or
mandatory pre-fight form is added. The native editor separates:

- Strategy: MP2 / RP2 / Other / Unknown.
- Group: organized / mass / unknown. Mass never overwrites strategy.
- Participation: exact positive whole count including the player / variable /
  unknown. Variable and unknown have no constant count.
- Per-field confirmations, **Apply context**, and a separate **Use for future**
  option. Context sources are expandable; invalid counts do not apply an edit.

NexCapture freezes future defaults at attempt start, including an attempt that
later re-anchors at smoke. Inheritance is never kill confirmation. A manually
entered but unconfirmed value has distinct provenance. Preferences schema **4**
saves only future values alongside existing planning choices; schemas 1–3 migrate
the former pilot as inherited assumptions, preserving locks/facts and inventing
no confirmation. No annotations, calculated results or performance history enter
preference storage.

NexSummary's observations, damage, timing and timeline remain unchanged. NexRecord
holds the effective annotation revision and NexEvaluation freezes ownership,
planning and model inputs once at the first post-kill evaluation. Completed
comparisons keep these inputs; later bank/account/Setup changes apply to future
comparisons. Context-only edits cannot pull in newer ownership or planning facts.
This deliberately replaces the previous account-refresh-driven recomputation of
the latest completed kill. Setup explains the frozen-input behavior.

Historical edits and future defaults use a dedicated action, never
NexObserver.method(). Edits can publish while another attempt continues. Worker
publication checks account/profile/session generation, world, kill record,
annotation revision, input revision and model revision. A newer unrelated attempt
does not invalidate an older frozen calculation. Older correction workers,
reset/logout/identity changes and evicted records cannot publish or reappear.
Annotations, frozen inputs and calculations share the **50-record**, memory-only
session lifetime and banking visibility rules. Live observations still clear on
travel; completed records stay hidden until same-account/profile/world revalidation.

## Comparability and supported outputs

Typed NexComparability replaces opaque string-only matching. A matched average
requires a complete kill, confirmed organized MP2/RP2 context with exact count,
compatible timer/adapter, compatible observed worn packages, levels/prayers/style
and recorded projectile state, and a stable known observed arena roster. Planning
locks are excluded. Unknown/unconfirmed/variable, mass, unspecified Other,
incomplete and incompatible records remain outside matched means. Editing an
older kill reconciles the latest cohort without modifying any summary.

These are descriptive matches. Nearby players do not establish party membership;
equal count and visible roster do not establish equal teammates' damage or a
counterfactual team model.

| Output | CTX1 contract |
|---|---|
| Observed duration and scoped hitsplat share | Preserved across all context edits; existing capture exclusions remain |
| Descriptive matched averages | Reconciled using confirmed context and typed compatible observation evidence |
| Conditional ordinary state arithmetic | Existing independently supported N2 kernels and exact frozen inputs; support/gaps remain scoped per state |
| Pilot/method recommendations | Context changes cannot enable automatic recommendations; other contexts are labeled conditional ordinary scenarios |
| Contribution and team time | Unavailable; no N4 estimate or DPS-ratio shortcut |

## Frozen inspection and report consistency

The editor keeps an unfinished draft across unrelated refreshes. Account updates
during combat cannot disable an otherwise-current historical annotation: its action
uses session/kill/context guards while preference/account-edit actions retain their
stricter view guards. A corrected calculation is withheld until the matching
revision publishes. New kills do not replace the older record being inspected.
Eviction closes its editor and rejects stale Apply/publication actions.

Production diagnostic report schema **2** projects attempt-start defaults,
effective per-field provenance, context/input/model revisions and per-output
support. Reports stay bound to the selected kill/calculation. Pending, hidden or
mismatched revisions cannot export old combat metrics. Existing allowlisting,
24,000-character bound, omission disclosure, explicit Copy action and selectable
clipboard fallback remain. Names, account/profile IDs, roster/cohort fingerprints,
raw traces and container dumps remain excluded.

## Initial CTX1 verification (cd3917c)

Environment: JDK **11.0.32.1**, Gradle **8.10**, RuneLite client/API **1.13.2**,
adapter `nex-public-4`, evaluator `nex-owned-1`, context `nex-context-1`, ordinary
models `melee-2` / `ranged-2`, knowledge `n3-package-1`, catalogue schema 14,
preferences schema 4 and report schema 2. No mechanics, catalogue dataset or licence
content changed.

Performed from the delivery worktree:

```sh
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --no-daemon
./gradlew test build --no-daemon
./gradlew dependencyInsight --dependency net.runelite:client --configuration compileClasspath --no-daemon
git diff --check
```

Pinned validation passed: **309 tests, 0 failures**; all **12 DEV1
scenarios, 703 checks, 0 failures**; **150 PNGs**, nine baseline diagnostic reports
at both fonts and three corrected reports at both fonts. Normal build passed,
reusing identical test/build outputs. Dependency inspection resolved
`latest.release` to **1.13.2**; this establishes no newer RuneLite compatibility.

The original nine scenarios retain capture, independent arithmetic, unknown/gap,
bank/reload, identity/reset and frozen-selection coverage. Their former implicit
pilot means now remain unavailable until context is confirmed. Existing mean/share
unit cases explicitly confirm their synthetic declarations; the new context replay
independently checks `(12 + 18) / 2 = 15 s`, mean share `37.5%` and aggregate share
`40%` after correction.

| Added DEV1 scenario | Checks / PNGs | Evidence |
|---|---|---|
| `nex-context` | 90 / 18 | Unknown defaults and refreshed source details, manual-unconfirmed values, confirmed corrections, independent means, invalid zero, RP2 mass/variable, field/source/Apply viewports and report agreement |
| `nex-context-active` | 56 / 8 | New public bank information and changed Setup lock; older kill edited during a new public attempt; preserved input revision, arithmetic/locks, active completion and subsequent inherited defaults |
| `nex-context-pending` | 46 / 8 | Repeated pending corrections, delayed initial/correction workers, current report, Reset and fresh publication, 50 further public completions evicting the inspected annotation, stale Apply/callback rejection and logout |

Focused unit cases also cover schemas 1–3 migration and schema-4 round trip,
positive/unknown/variable validation, smoke re-anchoring, per-field provenance,
typed gear/style/projectile/roster/context incompatibility, planning-lock exclusion,
frozen statistics, model/input mismatch, suspension, eviction and reset/identity
lifetime. The obsolete expectation that a new account snapshot or unrelated attempt
must discard a frozen completed calculation has been replaced by input-revision
checks; existing hard account/scene invalidation tests remain.

## Preview inspection and remaining client checks

Inspected actual native Swing components at **225px**, DejaVu Sans **12/20**, using
RuneLiteLAF. Representative evidence includes Encounter's 600px viewport, the
context editor at both fonts, fields/Apply viewports, per-field sources, the invalid
count error, variable-mass state, corrected Diagnostics, older-kill inspection after
a newer result and the post-eviction view. At 20px the optional editor scrolls;
the participation/count/Apply controls fit the tested lower screenful. Checkboxes,
icons, fields, combo geometry, buttons and wrapped text have automated bounds checks.
Real ItemManager sprites are replaced by explicitly labeled offline placeholders.

Real-client event mapping, actual sidebar focus/keyboard/scroll/DPI behavior,
clipboard interaction, real sprites, bank/reload retesting, timer calibration and
player usefulness remain open. Existing N1/N2 and N3/N4 limitations remain; synthetic
passes establish neither live acceptance nor Plugin Hub approval. Generated reports,
PNGs and private captures are not committed. The delivery PR links the current CI
run and `dev1-offline-review` artifact containing these files.

## PR #43 P2 review correction — frozen report actions during combat

The [review finding](https://github.com/PetterHL/ironpath/pull/43#discussion_r4307903410)
identified a guard mismatch at `cd3917c`: an unrelated inventory/equipment event
during another active attempt could publish a new account snapshot without a panel
render. Navigation into the older kill's Diagnostics remained valid, but Copy report,
selectable-text toggling and Back still required the obsolete live account view.

Read-only frozen report, detail and comparison actions now use the same panel-view
and session/result-revision guard as context navigation. They still consume the
selected immutable kill/calculation, including existing context/input/model checks.
Account/Setup mutations, Reset and capture-setting actions retain their stricter
live-view guards. Identity/profile/world, suspension, logout, reset, eviction and
stale-publication protections remain covered by the existing full suite. No client
reads or calculations move onto the EDT.

The existing `nex-context-active` replay now navigates from the older kill's editor
to Diagnostics **after** an actual combat inventory event has published fresh account
data, before Apply. It verifies clipboard invocation, visible selectable fallback,
toggle/Back navigation, exact frozen calculation/input/context revisions and the
unchanged active capture key. After Apply, another actual equipment switch and the
normal replay GameTick publish a new snapshot; the replay copies the corrected
older report with a successful injected clipboard while the next attempt continues.
The second kill must still complete. The fixture records clipboard writes only in
memory; production clipboard behavior is unchanged.

The added replay failed **22 checks** before the production guard correction,
including clipboard/selectable actions and Back at both fonts. Prescribed pinned
`test build offlineReview --no-daemon` now passes: **309 tests, 12 scenarios,
761 checks, zero failures; 156 PNGs**. The active-context scenario supplies **114
checks and 14 PNGs**. Normal `test build --no-daemon` passes using the same compiled
outputs; dependency inspection still resolves `latest.release` to **1.13.2**.
`git diff --check` and edited-document link checks pass.

Inspected the new `active-frozen-report-{12,20}.png`,
`active-corrected-report-{12,20}.png` and successful-copy 600px viewports, including
feedback, visible selectable text and Copy report at native 225px width. Matching
TXT files record both frozen revisions. Earlier results above remain evidence for
the pre-review head; the corrected head and its current CI run/artifact are linked
from PR #43. Real-client clipboard/focus/scroll and gameplay acceptance remain open.
