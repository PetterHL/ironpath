# DEV1 first review checkpoint

17 October 2026. Implementation starts from main `dc8c5f4`, with the approved DEV1
task definition from planning PR #39 (`0847fe8`). No unmerged N3b/N3c/banking
implementation is included. Plugin runtime remains 0.17.1; this delivery is
development-only. **First checkpoint is ready for review, not accepted.**

## Working command and outputs

```sh
./gradlew -PruneLiteVersion=1.13.2 offlineReview --args='nex-complete' --offline --no-daemon
```

After dependency preparation this regenerates
`build/review/nex-complete/report.md` plus twelve actual native Swing PNGs:
main, comparison, expanded calculation basis, unsupported spec gap, expanded
resource readings, and kill details, each at **225px** with **12/20px DejaVu Sans**.
The report contains links to its companion PNGs, source/input provenance, exact
callback ordering and expected/actual checks. It uses RuneLiteLAF and explicitly
labels empty sprite wells. See [setup and usage](../DEVELOPMENT.md#offline-review-dev1).

The PR's **Offline review** workflow uploads the same files as
`dev1-nex-complete`, retained for 30 days. Generated reports/screenshots are build
artifacts, not committed fixtures. The PR links the run/artifact for review.

## Exercised behavior and calculations

The ordered synthetic public-API fixture drives `NexObserver` and
`CombatStateReader`, then production capture/session/evaluation and the real panel.
No completed summary is injected. The initial smoke callback precedes the next
game tick. Barrier event payloads arrive before the getter catches up. All five
phases, an MP2 ranged/melee transition, a Voidwaker spec-candidate switch,
duplicate hitsplat callbacks, the dying NPC transformation and corroborating arena
completion are exercised. A worker evaluates immutable inputs; painting and
navigation run on the EDT. Account/unlock/preference facts are explicit synthetic
fixture inputs, not a fake live account read or persisted player data.

Results from the review command: **65 checks, 0 failures**.

| Check | Expected and actual |
|---|---|
| Encounter | One COMPLETE attempt; no excluded attempts |
| Timer | Smoke tick 0 → dying tick 30; 18.0 s; corroborating arena end tick 31 |
| Sample continuity | 30 / 30 eligible ticks |
| Scoped observed damage | 20 mine + 60 other = 80; share 25%; duplicate callbacks excluded |
| Actually worn history | Ranged empty offhand, melee defender, distinct spec-candidate evidence |
| Ordinary ranged hand case | Max 72, attack roll 53303, defence roll 68326, 5-tick rapid delay |
| Ordinary hit chance | 53303 / (2 × 68327) = 0.3900581029461267 |
| Actual ordinary DPS | (36 + 1/73) × hit chance / 3 = 4.682478322581585 |
| Owned arrow package | Only ammo changes to carried dragon arrows; max 74; attack roll unchanged |
| Owned ordinary DPS | (37 + 1/75) × hit chance / 3 = 4.8124501945708795 |
| Whole-package delta | +0.1299718719892944 theoretical DPS; not encounter throughput |
| Identity / negative alternative | Identical melee package gives zero; blood-fury alternative retains offensive loss |
| Unsupported metrics | Spec candidate and Zaros retain scoped gaps; no team-time estimate |

Expectations reuse the independent hand case in `NexOwnedEvaluatorTest`, with the
arrow case evaluated by hand: visible 112 with Rigour gives effective strength
145; amethyst/dragon totals 91/96 give integer base max 35/36. The declared Nex
magic 230 gives the reviewed Tbow damage multiplier 206%, yielding 72/74. The
runner does not call production arithmetic to manufacture these expected values.
Whole-package proposals and zero/negative checks use the production evaluator.

## Verification and fault detection

Prepared Cloud runtime: Temurin **11.0.32.1+1**, Gradle **8.10**, RuneLite API/client
**1.13.2**, adapter **nex-public-4**, evaluator **nex-owned-1**, knowledge
**n2-owned-2**, ranged model **ranged-2**, melee model **melee-2**; catalogue schema
14 and preference schema 3. Per-comparison source/version references are in the
generated report. Initial offline attempts correctly failed because dependency
metadata/runtime JARs were missing; online preparation resolved them. These
attempts are not counted as passes.

```sh
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --offline --no-daemon
./gradlew test build --no-daemon
git diff --check
```

The pinned test/build/review passed, with **212 tests, 0 failures/errors/skips**
(including three new workflow tests), and 65 scenario checks. The normal
`latest.release` build passed as well; it resolved the same 1.13.2 dependency and
reused the already passing compilation/tests. This is not evidence for a newer
RuneLite version. Existing arithmetic, incompatible-package, unknown-state,
capture, lifecycle and panel regressions remain in the suite.

A temporary isolated production change replaced `NexSummary.seconds()`'s tick
factor **0.6 with 0.7**. The same offline command exited 1 and retained a failure
report: expected **18.0**, actual **21.0**; main-view checks failed at both font
sizes too (three failures). The source was restored in a `finally` block; no
intentional fault or changed expectation is committed. The restored build and
review passed again. This demonstrates that a small calculation change can be
reviewed without gameplay. Failure evidence remains locally under
`build/review/mutation-proof/report.md`.

Fresh output is also checked: the integration test seeds an obsolete PNG, runs
the complete scenario and verifies that only twelve current PNGs remain. The
standalone JavaExec always runs, so missing images cannot be hidden by Gradle's
up-to-date checks. The complete scenario has been regenerated repeatedly while
correcting preview setup, without resetting independent expected results.

All twelve images were visually inspected in both font sizes. Standard Metal LAF
initially gave the history evidence panel a light background; using RuneLite's
actual look and feel fixes the preview without changing product UI code. Text
wrap checks pass; long native combo selections elide, while complete selected
claims appear in wrapping text below. Full-component PNGs can be tall and need
scrolling when reviewed. They are not pixel-golden tests: production maps may
display equivalent data in different iteration order across JVMs. Semantics and
independent numbers are asserted. Cloud fontconfig emitted cache-directory
warnings; DejaVu Sans was available and rendering completed.

## Review boundary

Assess whether the report and PNGs make a calculation/UI iteration reviewable
without gameplay. The rest of DEV1's scenarios remains unimplemented pending
this checkpoint: unknown/incomplete cases, banking/next kill, controlled pending
identity reset, and a fresh result during historical inspection. Existing tests
cover parts of those behaviors; this first scenario does not claim end-to-end
coverage of the plugin's publication guards.

No live event mapping, real item sprite, native DPI/focus/scroll/GPU behavior,
personal performance or player usefulness has been validated. N1–N4 acceptance
stays open. No new spec damage, survival, contribution or team-time claim,
recording/export feature, or Plugin Hub publication is introduced.

## P2 layout-check follow-up — 17 October 2026

The review of PR #40 reproduced a false PASS for a text area at x=210 with width
120 in a 225px image, and for visible text allocated zero width. The previous
check compared widths without positions and skipped zero-width components.

The development-only preview now checks the complete text-area bounds against
both the image and every ancestor inside the captured root. Non-empty visible
areas with no allocated space fail. Text measurement accounts for insets, a
minimum glyph width, unwrapped line width and the actual allocated wrapping width;
it does not trust a preferred size derived from a wider parent. Failure evidence
still includes the current PNG plus the affected text and a clipping reason.
Explicitly hidden descendants and empty areas remain ignored. No scenario,
production plugin code, calculation expectation or runtime version changed.

Eight capture/report regressions cover image edges (including x=210/width 120),
parent/grandparent clips, zero width/height, positive compressed widths/insets,
underallocated wrapping behind an overridden preferred size and an unwrapped
line. Positive controls cover exact edges, valid wrapping at 12/20px, hidden
sections and empty text. Against the previous helper, six test methods failed,
including both reported cases; all eight pass with the fix.

Local focused verification used Temurin 11.0.32.1+1 and JUnit 4.13.2 to compile/run
only PanelPreview, ReviewReport and PanelPreviewTest. The repository's full Gradle
command was also attempted with Gradle 8.10 and RuneLite pinned to 1.13.2, but this
workspace's network policy returned HTTP 403 for repo.runelite.net before compile.
That blocked local full build is not a pass. The existing PR workflow provides
the full execution evidence instead:

- Implementation: `e8c4f5a1e72b1dca7a0fac7af309ce96dadf27a3`.
- [CI run 39022466379](https://github.com/PetterHL/ironpath/actions/runs/39022466379)
  passed `./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --no-daemon`
  on Temurin 11.0.32+1. Tests, build and the standalone review all executed.
- [Regenerated report and twelve PNGs](https://github.com/PetterHL/ironpath/actions/runs/39022466379/artifacts/12096869224)
  are available as `dev1-nex-complete`; CI reported PASS. These are current outputs,
  not the baseline artifact linked earlier in the PR.
- Revised-image inspection is still pending: the workspace also blocks the
  connector's artifact-download host. Access to repo.runelite.net has been
  requested so the same full run can regenerate inspectable images locally.
  The original visual inspection above applies to the previous checkpoint only.

`git diff --check` passes. This follow-up does not accept N1–N4 live behavior,
expand DEV1's scenario set, merge the PR or publish the plugin.
