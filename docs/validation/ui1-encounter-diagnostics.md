# UI1 — Encounter, Diagnostics and sanitized report

18 October 2026. Development build **0.20.0**, implemented for review from main
`53abe3b`. This delivers UI1 only. CTX1, live acceptance and N3/N4 recommendation/
contribution work remain open; no feature merge or Plugin Hub publication.

## Result and implementation

The primary views are Encounter and Diagnostics. Setup/Account stay secondary,
including navigation between those pages; Back, keyboard actions, Reset and
frozen comparison/kill inspection remain available. Encounter shows duration,
observed Nex + named-minion hitsplat share and compact comparable mean/count.
A single matching kill no longer repeats mean/min/max; Diagnostics retains
spread, aggregate versus mean share, eligibility/gaps and matching limitations.
The fixed Trio MP2 pilot is labeled model/planning scope, with per-kill context
unconfirmed. Partial attempts remain separate.

NexPresentation projects existing supported results without recalculating combat.
It visits carried then bank scenarios in capture order, chooses the first positive
supported complete package per state (proposed package, then explicit alternatives),
and deduplicates equal before/after equipment and projectiles within each role.
Carried wins over an identical bank change. It does not rank unrelated state
percentages or combine gains. Zero/negative/unknown states remain in comparisons;
the no-positive case appears once. Partial search, ordinary offence if supplied,
bank preparation and healing limitations stay beside their affected claims.
Sprites use ItemManager in the live client; offline wells have placeholder labels.

Diagnostics can open before account/kill readiness, during pending evaluation,
after Reset, and during paused travel. Hidden history is not projected/exported.
An inspected scenario/pair keeps the same kill, calculation revision and frozen
planning locks across a newer result or preferences. Returning to the comparison
preserves selection; Encounter explicitly returns to the latest result.

NexReport is an immutable allowlisted model/formatter shared with DEV1. It exports
schema/build/model/catalogue/source versions; explicit live/synthetic provenance;
report-local K1/C1 references; outcome/exclusion/timer/damage scope and totals;
selected scenario, exact before/after slot/projectile IDs, sampled levels/prayers/
style/resources, relevant candidate quantities, conditional target assumptions,
per-metric support, exact outputs, search/availability scope and omissions. A
24,000-character limit omits whole fields and discloses the count. Unknown inputs
remain unknown. No kill wall-clock timestamp is invented.

It excludes names, profile/account identifiers, roster/cohort fingerprints,
arbitrary interpolated diagnostic strings, full inventories/bank, traces,
unrelated scenarios and ownership/unlock provenance. This is a scoped diagnostic
report, not a lossless replay or a full recomputation input. Only an explicit Copy
report action touches the clipboard. Busy, restricted or headless clipboard leaves
focusable selectable text and a clear outcome. Nothing is uploaded or automatically
saved by the plugin. Removed report widgets are cleared; stale actions cannot copy.

## Before / after evidence

Before was regenerated locally from current main `53abe3b` in an isolated detached
worktree with the same JDK/RuneLite/font settings and `offlineReview --args=nex-complete`.
The existing consolidated baseline's [successful CI run](https://github.com/PetterHL/ironpath/actions/runs/39118107674)
and [native preview artifact](https://github.com/PetterHL/ironpath/actions/runs/39118107674/artifacts/12133301201)
retain the pre-UI1 screens (subject to GitHub artifact retention).

| Same synthetic encounter | Before 0.19.3 | After 0.20.0 |
|---|---|---|
| Duration / observed share | 18.0 s / 25.0% | 18.0 s / 25.0%, unchanged |
| +2.8% ordinary ranged comparison | Twisted bow named, despite ammunition change | Dragon arrow, scoped to ranged / SMOKE / tick 0 |
| Matching kill | Repeated mean/range/share aggregates at n=1 | “1 matching kill”; aggregates in Diagnostics |
| Zero melee alternative | Filler homepage row | Preserved in comparisons |
| Main PNG at 12px | 225 × 827 | 225 × 540 |
| Main PNG at 20px | 225 × 1932 | 225 × 1192 |
| Diagnostics/report | Unavailable | Safe states, frozen allowlisted report and clipboard fallback |

Heights are evidence of reduced content, not a measure of usefulness. Actual
225 × 600 JViewport previews show navigation, headline hierarchy and Copy report
at normal/enlarged fonts. Full comparison/basis/resources PNGs preserve long names
and limitations. Native combo labels may elide; the selected claims also wrap below.

## Verification

Prepared Cloud environment: JDK **11.0.32.1**, Gradle **8.10**, RuneLite **1.13.2**,
RuneLiteLAF, DejaVu Sans **12 / 20px**. Model versions: `nex-public-4`,
`nex-owned-1`, retained melee/ranged models and knowledge `n3-package-1`.

```bash
source /workspace/ironpath-environment/activate.sh
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --no-daemon
./gradlew test build dependencyInsight --dependency net.runelite:client --configuration compileClasspath --no-daemon
```

Pinned full validation: **295 tests, 0 failures; nine scenarios, 511 checks, 0
failures; 116 native PNGs and 18 production-format synthetic TXT reports**.
Normal `test build offlineReview` also passed; `latest.release` resolved
to the same **1.13.2**, not an additional RuneLite compatibility version.
Fontconfig reports unwritable cache directories; DejaVu rendering and checks pass.
Existing Java deprecation and Gradle 9 compatibility notices remain.

The nine baseline scenarios remain intact, including delayed bank opening/return,
arena reload, unknown inputs, partial capture, identity/reset with held workers,
and frozen negative alternatives. UI1 adds Diagnostics/clipboard/report checks
and screenshots to that same harness. Pending workers have explicit Diagnostics
previews; the frozen-history scenario compares the exact exported negative pair
before/after a newer kill publishes. Unit tests independently cover changed arrow
identity, stable deduplication, zero results, redaction canaries, wrong-kill/hidden
state rejection, frozen preferences/targets, output bounds and clipboard failure.
The existing arithmetic, incompatible-package, privacy and lifecycle tests pass.

Layout verification now checks button/label bounds and plain/HTML action text in
addition to text wrapping. Negative geometry tests reject collapsed labels and
truncated actions. Primary controls are checked inside real 600px viewports.
Nested report contents deliberately scroll; RuneLiteLAF's zero-size scrollbar
arrows are excluded from primary-control checks. Images were inspected, including
before/after, both Encounter fonts, Diagnostics/fallback/pending, unknown/zero,
identity-cleared, negative frozen comparison and expanded calculation basis. An
empty comparison filter exports no selected pair rather than the previously hidden
selection.

Outputs: `build/review/index.md` and each scenario directory. For `nex-complete`,
review `main-*.png`, `encounter-viewport-*.png`, `diagnostics-*.png`,
`diagnostics-viewport-*.png`, `selectable-report-*.png` and
`diagnostic-report-*.txt`. The PR description links its exact current CI run and
`dev1-offline-review` artifact after publication. Routine generated outputs and
private data are not committed.

## Separate client/player checks

Real sprites/cache, OS clipboard, focus/keyboard navigation, Back/scroll in the
actual sidebar, DPI/GPU rendering, event-bus mapping, timer calibration and player
usefulness have not been verified here. Existing bank/reload and broader N1/N2
live checks remain open. In the client, confirm build 0.20.0; inspect Encounter
and a selected comparison; open Diagnostics and copy a report; verify selected
inputs match the screen and Back restores the comparison. Continue ordinary
bank/reload testing separately. Review UI1 before starting CTX1.
