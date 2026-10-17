# DEV1 offline review workflow

17 October 2026. Implementation starts from main `dc8c5f4`, with the approved DEV1
task definition from planning PR #39 (`0847fe8`). No unmerged N3b/N3c/banking
implementation is included. Plugin runtime remains 0.17.1; this delivery is
development-only. **The first checkpoint was reviewed on 17 October; Petter authorized completing
the same DEV1 task.** See [current suite evidence](#dev1-scenario-completion); the
sections below retain the original checkpoint and follow-up evidence.

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

## Original checkpoint review boundary

This boundary describes the first delivery. The scenario expansion is now included
in [DEV1 completion](#dev1-scenario-completion) after the authorized review.

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
- At this follow-up, revised-image inspection was blocked by the workspace
  artifact-download host and RuneLite dependency access. This was resolved during
  the subsequent review: the latest-head CI artifact from run 39022628712 matched
  SHA-256 `2f5e3e4ce80d26062735e22e87829b5ed8d692a4cad50038b09a1b4da0271804`;
  all twelve revised images were inspected and a local pinned full build/review
  passed 220 tests and 65 scenario checks. No new clipping issue was found.

`git diff --check` passes. This follow-up does not accept N1–N4 live behavior,
expand DEV1's scenario set, merge the PR or publish the plugin.

## DEV1 scenario completion

17 October 2026. After the first checkpoint review, Petter authorized continuing
DEV1. PR #40 now includes the remaining scenarios in the same workflow. Runtime
plugin version and production code remain 0.17.1; no N3b/N3c or draft banking/reload
implementation is imported. [Commands, outputs and adding a scenario](../DEVELOPMENT.md#offline-review-dev1).

The default `offlineReview` runs all seven scenarios and writes a linked
`build/review/index.md`. A single scenario or `list` can be selected with `--args`.
GitHub Actions now uploads the whole review directory as `dev1-offline-review`,
including available failure evidence and retaining it for 30 days.

| Scenario | Checks across 12/20px | PNGs | Verified behavior |
|---|---:|---:|---|
| nex-complete | 65 | 12 | Original complete public-event replay, independent calculations, scoped gaps |
| nex-unknown | 32 | 6 | Unread bank/inventory/ammunition/resources remain explicit; dependent ranged claims unavailable; supported worn melee comparison still available |
| nex-incomplete | 20 | 4 | Exit without corroboration is CENSORED; no complete mean or owned result; excluded details remain inspectable |
| nex-bank-trip | 38 | 6 | History hidden during travel, revalidated at bank and arena return; two complete kills and fresh comparison |
| nex-identity-pending | 26 | 4 | Evaluation finishes before character change; held publication released after fresh result cannot overwrite it or schedule stale-account work |
| nex-reset-pending | 26 | 4 | Actual Reset action invalidates an unexecuted worker; late callback cannot replace a fresh result |
| nex-frozen-history | 24 | 8 | Selected negative alternative/resources and kill details remain frozen across new results; Back/reopen selects latest kill |
| Total | **231** | **44** | Seven reports, zero failed checks |

The first/second bank-trip kills last 20/30 ticks: **12/18 seconds**, with a
**15-second** comparable mean. Scoped damage is 20/80 and 60/120: mean per-kill
share is **37.5%**, while aggregate share is **40%**. These independent expectations
deliberately differ so conflating the two statistics fails a check. Completion is
detected from fresh public NPC/event sequences; no completed summary is injected.
The trip stays within current main's 100-tick banking grace and does not accept
draft PR #38's longer-travel or scene-reload behavior.

The original scenario still runs the direct adapter pipeline with supplied
account/preferences. The six new scenarios run actual plugin lifecycle/event
handlers, AccountSnapshotReader, in-memory PreferenceIO, the production evaluator
on a real worker, guarded client callbacks and the plugin-owned Swing panel.
RuneLite dependencies are mocked; event-bus dispatch itself is outside scope.
Each scenario/font has a fresh account/session/queue. Quest scripts and bank
containers stay unread, while synthetic account support/unlock facts are explicit.
The shared public-state fixture is extracted from the original scenario and
reuses NexOwnedFixtures. Scheduling controls, reports and previews remain test-only
and independent of Nex summary/phase types in the shared `dev` helpers.

### Verification and fault detection

Temurin **11.0.32.1+1**, Gradle **8.10**, RuneLite client/API **1.13.2**, DejaVu Sans
12/20 and RuneLiteLAF. Adapter/evaluator/knowledge/model versions remain those
recorded above. Both commands passed:

```sh
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --args='all' --offline --no-daemon
./gradlew test build offlineReview --args='all' --no-daemon
git diff --check
```

**226 tests across 31 suites**, zero failures/errors/skips; the standalone review
passed all **231 checks** and generated seven reports plus **44 native 225px PNGs**.
The normal `latest.release` run resolved the same RuneLite 1.13.2 and executed the
tests/review again after fixture cleanup. No newer-release compatibility is claimed.
All 44 images were visually inspected at both font sizes. Text/gap explanations,
cleared/paused views, different share statistics and correct frozen/latest kill
identities were checked. No new clipping issue was found. Tall detail views,
elided native combo selections, explicit placeholder sprites and fontconfig cache
warnings remain as described above.

A temporary isolated mutation removed the final `evaluateOwned` client-publication
guard in IronpathPlugin. `nex-identity-pending` then exited 1 and failed both fonts:
the old callback overwrote the fresh result and scheduled one evaluation with
stale account inputs. This checks the actual guard rather than a fake publication
implementation. Production source was restored in `finally`; final builds and all
scenarios passed afterward. Local failure evidence is under
`build/mutation-proof/publication-guard/`; it is not committed or presented as a
passing artifact. The original timer mutation and layout-fault regressions remain
separate recorded evidence. Expectations were not auto-updated from observed output.

DEV1's offline workflow is implemented and ready for review. N1–N4 real-client
event mappings, resources/stock, real sprites/DPI/focus/scroll/GPU behavior and
player usefulness remain open. No real encounter recording/export, persistent
combat traces, spec-damage recommendation, survival, contribution or team-time
claim is introduced. No merge or Plugin Hub publication was performed.
