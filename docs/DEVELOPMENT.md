# Development and verification

Use JDK 11, Git and the included Gradle wrapper (8.10). The retained build resolves
RuneLite through `latest.release`; CTX1 normal and pinned validation both resolved
1.13.2. Record the actual resolved version when verifying a change. The first build
needs dependency downloads. Runtime evaluation is local.

## Checkout and run

For an existing clean checkout, update main with `git fetch origin`, `git switch
main` and `git pull --ff-only`, then create a short-lived branch for the change.
Preserve any local changes and unrelated branches. Close the development client
before updating or rebuilding it.

The 19 October consolidation makes main **0.23.3** the common base for N3 plans,
CTX1, the current UI, sidebar simplification and all 14 DEV1 scenarios. Do not
start from retired feature branches. TASKS.md owns the order, with live acceptance
tracked separately. After merging future authorized work, delete only heads whose
commits are preserved in main (or an explicit archive); never discard unique work.

Windows PowerShell:

```powershell
.\gradlew.bat test build --no-daemon
.\gradlew.bat run
```

For the pinned development client in PowerShell, quote the entire property:

```powershell
.\gradlew.bat "-PruneLiteVersion=1.13.2" run
```

An unquoted dotted version can be split by PowerShell into a spurious task such
as `.13.2`.

macOS/Linux use `./gradlew` instead. An offline build is valid when dependencies
are already cached; document that condition rather than claiming a fresh download.

`run` opens the RuneLite development client with IronPath and developer mode.
Java assertions are enabled with `-ea`, as required by RuneLite's development
plugin loader. The temporary GPU workaround excludes only
`net.runelite.client.plugins.gpu...` with `-da`; unit tests retain Gradle's
separate assertion configuration.
For Jagex Account login use [RuneLite's instructions](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).
Never commit credentials, account captures or local configuration. If using the
credential-file development flow, remove the temporary credential-writing launcher
argument afterwards and remove the local credential file when testing is finished.

## GPU assertion during development

On 15 October a supplied client log ended in `AssertionError` at RuneLite GPU
`ModelUploader.uploadSortedModel:194`, called by `GpuPlugin.drawTemp`. The
[1.13.2 source](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/java/net/runelite/client/plugins/gpu/ModelUploader.java#L194)
asserts that a projected face distance lies inside the model diameter. No IronPath
frame appears in that stack. Bank layout DEBUG messages preceding it are not
evidence that bank tags caused the failure.

Build 0.16.3 changed global `-ea` to `-ea:com.ironpath...`. That was incorrect:
[ExternalPluginManager.loadBuiltin](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/java/net/runelite/client/externalplugins/ExternalPluginManager.java#L460)
checks assertions in its own class and rejects startup when they are disabled.
The 0.16.3 configuration/build check passed but did not exercise that API. The
next client launch exposed this regression.

Build 0.16.4 restores `-ea` and adds only
`-da:net.runelite.client.plugins.gpu...`. This retains the required RuneLite and
IronPath checks and excludes the GPU package as a temporary workaround. It does
not repair GPU/model code or establish the sole cause of the frozen client. No
GPU setting or other plugin's saved configuration is changed.

Verification: JDK 11 / RuneLite 1.13.2. A temporary probe executed the actual
public `loadBuiltin(IronpathPlugin.class)` in a separate headless JVM with the
real `run` classpath and configured JVM arguments. It **passed**, including
checks that IronPath/RuneLite assertions are on and GPU assertions are off.
The same probe with the old 0.16.3 arguments reproduced the exact loader rejection.
Normal `test build` passed (6 s), with the unchanged 178-test suite up to date.
This establishes loader compatibility, not graphical-client or Nex acceptance.

If the client cannot close normally, end the affected RuneLite development
process, update the current branch and restart with `run`. If it still freezes,
temporarily disable the GPU plugin through RuneLite settings and collect the new
error or a Java thread dump while frozen. Do not attribute another freeze to the
old assertion without fresh evidence. Raw user logs remain outside version control.

## Verification by responsibility

- Retained combat arithmetic: original melee/ranged/magic cases and oracle corpora.
- Refactors: affected source/read/observer/comparison tests plus normal test/build.
- Privacy: unknown state, profile/character changes, stale workers/images/actions,
  local preference IO and migrations.
- UI changes: native panel width/enlarged fonts, real ItemManager sprites, focus,
  Back/scroll and actual client behavior. Synthetic captures alone are insufficient.
- Nex deliveries: use the concrete N1–N4 acceptance in [TASKS](../TASKS.md).
- Documentation: relative links, archive integrity, current instructions/status and
  `git diff --check`. Historical reports keep their original result scope.

Standard Ironman on an ordinary supported permanent members world is the retained
account support boundary. Broader modes require explicit observation/model support.
No client or Plugin Hub acceptance is implied by a successful build.

## Offline review (DEV1)

The offline workflow supplies fourteen synthetic scenarios without OSRS credentials,
a display server or gameplay: nine baseline/bank/reload cases, three CTX1 cases,
N3 declared plans and the multi-item UI fixture. The first checkpoint was reviewed on
17 October 2026; Petter authorized the remaining scenarios in the same task.
[Validation and limits](validation/dev1-offline-review.md).

Prepare JDK 11, the Gradle 8.10 wrapper and **DejaVu Sans** (`fonts-dejavu-core`
on Debian/Ubuntu). Set `JAVA_HOME` to that JDK and use a writable Gradle user home.
Keep the configured HTTPS proxy and CA trust. Java/Gradle need the corresponding
proxy host/port and supplied CA truststore as well as the shell proxy; do not
disable TLS verification. The prepared IronPath Cloud environment provides
`/workspace/ironpath-environment/activate.sh`; source it when present. It refreshes
proxy/trust settings for the current machine and is environment setup, not a
repository dependency. RuneLite dependencies come from `repo.runelite.net` and
Maven Central; the wrapper needs its configured Gradle distribution host. A new
checkout/cache needs online preparation before the offline commands can work.
Do not count dependency-resolution failures as executed tests.

From the repository root:

```sh
# First run resolves dependencies and reviews every scenario (the default).
./gradlew -PruneLiteVersion=1.13.2 offlineReview --no-daemon

# Discover scenario IDs, or regenerate just one report and its previews.
./gradlew -PruneLiteVersion=1.13.2 offlineReview --args='list' --offline --no-daemon
./gradlew -PruneLiteVersion=1.13.2 offlineReview --args='nex-bank-trip' --offline --no-daemon

# Full pinned validation, including standalone generation of all reports/previews.
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --args='all' --offline --no-daemon

# Separate compatibility check with the normal latest.release dependency.
./gradlew test build --no-daemon
```

Use `gradlew.bat` in PowerShell. The property pins both RuneLite client and jshell;
normal builds still default to `latest.release`. Keep the pin when sharing results.
Reports record actual client/API JARs, JDK, Gradle, adapter, evaluator, catalogue and
model/source versions. A successful normal build resolving the same version is
not evidence for compatibility with a newer RuneLite release.

| Scenario | Behavior | Previews at each font size |
|---|---|---|
| `nex-complete` | Public-event complete encounter; independent ordinary DPS/max-hit cases; zero/loss and spec/Zaros gaps | Main, comparison, calculation basis, gap, resources, kill details |
| `nex-unknown` | Unread bank/inventory, ammunition and energy/Death Charge; dependent ranged claims blocked while supported melee remains | Main, unknown ammo, unknown resources |
| `nex-incomplete` | Arena exit without corroborated completion; attempt remains excluded from complete statistics | Excluded main and attempt details |
| `nex-bank-trip` | Complete kill → suspended travel → recognized bank → recognized return → next complete kill | Paused travel, bank, next-kill main |
| `nex-identity-pending` | Finished evaluation held before publication; character changes; old callback released after fresh work publishes | Cleared view, fresh result after rejection |
| `nex-reset-pending` | Worker held before evaluation; actual Reset action; old callback released after fresh work publishes | Cleared view, fresh result after rejection |
| `nex-frozen-history` | New results while a selected negative owned alternative/resources or kill details are inspected; Back opens the latest result | Initial selection, frozen comparison, frozen details, latest comparison |
| `nex-bank-long-trip` | Exit callback before a lagging getter, repeated loading, nearby Nex, 150-tick outbound wait, late bank and 150-tick return; identity change still clears | Late outbound, late bank, next kill, identity cleared |
| `nex-arena-reload` | Arena scene rebuilt without banking, missing NPCs and repeated loading, then fresh public arena evidence and next smoke/kill | Rebuilding scene, next kill |
| `nex-context` | Unknown/inherited/manual-unconfirmed context, corrections, independently checked matched means, invalid count and variable RP2 mass | Editor, fields/Apply viewports, source details, corrected Diagnostics |
| `nex-context-active` | Older inspected kill edited while another public attempt continues; newer bank/lock inputs stay excluded; remembered defaults apply only to subsequent attempts | Active historical edit, frozen corrected report, later inherited context |
| `nex-context-pending` | Repeated edits with held workers, corrected report consistency, Reset, 50 further public kills evicting the inspected annotation, then logout | Pending edit/report, corrected Diagnostics, eviction and logout |

Outputs are regenerated in `build/review/<scenario>/`: `report.md` plus the named
PNG states with `-12.png` / `-20.png`, all at the native **225px** width. Running
`all` also writes `build/review/index.md`, linking every report and PASS/FAIL result.
The UI1 suite originally generated nine scenario reports, 116 PNGs and a sanitized
`diagnostic-report-12.txt` / `diagnostic-report-20.txt` for each scenario. Each scenario and font
starts with fresh client/account/session state. CTX1 adds three reports and editor/
context/report previews to the same registry; current counts are in its validation
record. Reports contain input provenance,
ordered callbacks, encounter outcomes, timer/damage scopes, gaps and expected versus
actual checks. Published lifecycle comparisons include actual/proposed inputs,
results and source assumptions; the complete scenario retains the detailed
independent hand calculations.

A failed check makes the command exit nonzero but leaves a current report and
available images. `all` continues through other scenarios after a recorded failure.
Each selected scenario removes its previous PNGs before replay. `offlineReview`
has no Gradle output cache, so a deleted PNG cannot be hidden by an up-to-date
result. Unknown IDs fail before replay. Unit tests use temporary directories;
they do not substitute for running the standalone review command.

Open the report and PNGs together. Check the independent expected/actual table,
then inspect both fonts for wrapping, meaningful result/gap text and the correct
selected kill. RuneLiteLAF and DejaVu Sans 12/20 give consistent Cloud metrics.
DEV1 now injects pinned genuine item sprites through the test ItemManager boundary;
see the source/terms and current preview workflow below. Older delivery captures
retain their explicitly labeled placeholder wells. Live cache integration stays open. Images paint full component contents and may be tall. Native
combo selections may elide, with full selected claims in wrapping text below.
UI1 also paints actual 225 × 600 JViewport screenfuls, checking primary
navigation and Copy report visibility at both fonts. These are not screenshots
of an OS window or real DPI/focus/scroll/GPU tests.

The layout checker rejects visible nonempty text areas outside the PNG or any
ancestor inside the capture root, zero-sized/compressed areas, and wrapping that
exceeds the actual allocated content width/height. UI1 additionally checks
button/label geometry and plain/HTML button text. CTX1 extends control checks to
checkboxes (including their icons), editable fields and combo geometry. Scrollable report contents are
intentionally clipped by their nested viewport; RuneLiteLAF scrollbar arrows are
intentionally zero-sized. Hidden/empty areas are ignored.
Failures still write the PNG and the affected text/reason. Visual inspection
remains necessary; layout checks do not establish usefulness or interaction quality.

The **Offline review** GitHub Actions job runs the pinned test/build and all fourteen
scenarios. It uploads `dev1-offline-review`, containing the index, reports and PNGs
with 30-day retention, including available failure evidence. Link the exact run
and artifact from the PR. Routine generated output stays outside Git history.
Regenerate after source changes; never relabel stale evidence for a new revision.

### Reviewing UI1 diagnostics

In `nex-complete`, compare `main-12.png` / `main-20.png` and
`encounter-viewport-*.png` with `diagnostics-*.png`,
`diagnostics-viewport-*.png` and `selectable-report-*.png`. The item card should
name **Dragon arrow**, with +2.8% theoretical DPS scoped to its captured ranged
state, rather than Twisted bow. Open comparison/basis/gap/resource/kill-detail
PNGs for the exact qualifications. The two pending-publication scenarios also
render held-worker Diagnostics. Frozen history checks the exported negative
alternative while a newer result publishes.

The TXT reports use the **production** allowlisted formatter with explicit
`SYNTHETIC_OFFLINE` provenance. The plugin's offline panel factory only replaces
sprite/clipboard boundaries; the scenario runner/assertions stay in test sources.
No live report is saved automatically. On a real client, Diagnostics → Copy report
now copies the selected frozen report; a busy, headless or restricted clipboard
leaves focusable, selectable text for manual Ctrl+A/Ctrl+C. No upload occurs.

[UI1 validation](validation/ui1-encounter-diagnostics.md) records exact commands,
counts, comparison evidence and unperformed client checks.

### Reviewing CTX1 context edits

Open `nex-context`'s `corrected-context-12.png` / `-20.png`,
`context-fields-viewport-*`, `context-apply-viewport-*`, `invalid-count-*`,
`variable-mass-*`, `context-source-details-*` and `corrected-diagnostics-*`.
The editor is optional and scrolls at enlarged fonts. Inspect its native controls,
positive-count error, per-field confirmations and separate **Use for future** option.
`corrected-report-*.txt` uses production schema 2 and must agree with the selected
annotation/input revisions. Unknown and unconfirmed context has no matched mean.

`nex-context-active` changes public bank knowledge and a real Setup lock before
correcting the older kill during the next attempt. Its old calculation keeps its
input revision and arithmetic; saved context defaults are inherited only by a later
attempt. It also publishes fresh account data during that attempt before opening
Diagnostics: `active-frozen-report-*` verifies the unavailable-clipboard/selectable
path before Apply; `active-corrected-report-*` verifies successful copying after Apply
and another equipment refresh. Both retain the older kill and active capture. The
successful-copy `*-viewport-*` files check Copy report in a 600px screenful.
`nex-context-pending` holds old workers/publications, edits repeatedly,
then tests Reset, 50 public completion replays/eviction and logout. Context writes
are collected by an explicit in-memory preference boundary; no file is saved.

[CTX1 validation](validation/ctx1-kill-context.md) records current results and
unperformed live controls, real sprites, focus/scroll/DPI/clipboard and player checks.

### Scenario boundaries and adding a scenario or encounter

`OfflineReview.scenarios()` registers callbacks accepting only an output directory
and `ReviewReport`. `dev/OrderedReplay`, `ReplayQueue`, `ReviewReport` and
`PanelPreview` contain no Nex phases or summary types. A future encounter supplies
its own public-state fixture, adapter and callback; it need not adopt NexSummary.
The retained `ZilyanaObserver`/`EncounterCapture` provide a second architectural
reference, not newly enabled boss support. Keep phase names, expected completion
signals and encounter fixtures outside the shared infrastructure.

`OfflineNexClient` shares the anonymous reported gear and explicit synthetic public
state between the original direct-adapter scenario and the lifecycle scenarios.
It supplies callback payloads before getter updates, fresh NPC identity per kill,
and polled state at every tick. `nex-complete` supplies synthetic account/preferences
directly and exercises the production adapter/capture/session, evaluator and panel.
It does not exercise account reading or plugin publication. `OfflinePluginReplay`
runs the actual plugin lifecycle and public event handlers, AccountSnapshotReader,
in-memory PreferenceIO, worker evaluator, guarded client publication and plugin-owned
panel. RuneLite Client/ConfigManager/toolbar/thread/executor are mocked at the
boundary; event-bus dispatch itself is not run. No summary is injected in any scenario.
All painting/navigation happens on the EDT, reads on the synthetic client thread,
and selected worker tasks on a real separate worker, with bounded waits and explicit
callback release. Preferences use memory only; attempts to write fail.

To add a scenario, reuse an existing encounter fixture, register a fresh callback,
and add an end-to-end `OfflineReviewTest` case. Specify unknown versus observed
inputs and the ordered callback/state script. Derive expectations from reviewed
hand cases, existing independent oracles or lifecycle invariants. Add semantic
checks and meaningful preview states at both font sizes. Test a fault to show the
relevant guard/check can fail; do not auto-bless current output. Keep fixture
loading and execution in test sources so they never enter the plugin JAR.

The original bank scenario exercises a short recognized trip. The long-trip
scenario adds waits beyond the removed 100-tick deadline on both legs, rejects
nearby-Nex evidence before bank verification, and checks hard identity clearing.
The arena-reload scenario verifies same-arena resumption without a bank and a
new complete public-event kill. These exercise the combined N3b/N3c/banking
main baseline; original DEV1 results retain their own delivery scope. Real-client event mappings, stock/charges,
real sprites, focus/scroll/GPU behavior and player usefulness still require the
N1–N4 checks. DEV1 adds no real encounter recorder/export or relaxed trace policy.

## N3 declared-plan review

Build 0.28.2 makes **Diagnostics → Copy report now** sufficient for the next
capture diagnosis: its bounded overview covers all retained target/HP sources,
weapon/animation groups and HUD callback/rejection counters, and records the
running RuneLite version. Event windows remain optional detail. Do not presume
that Nex emits RuneLite's generic integer-HUD channel or equate it with an actor's
normal overhead health bar.

Build 0.28.1 additionally captures `observation-events-12.png` / `-20.png` and
native `observation-events-viewport-*` screenfuls. Inspect the event-page selector,
relative timings/links and matching copied report; full client acceptance remains
separate. Long HUD traffic and populated-plan imports have regression coverage.

Build 0.28.0 extends `nex-n3-plan` with a synthetic animation callback, settled
state/start-resource assertions and an unchecked recorded-tick draft. Inspect
`declared-inputs-12.png` / `-20.png` at the recorded-tick control and full-height
Diagnostics at both fonts. All 14 scenarios and both full commands remain required.
[Observation verification and exact pending client checks](validation/nex-observation.md).

Build 0.27.0 extends the same `nex-n3-plan` scenario with explicitly declared
Deflect Melee impacts. Inspect `deflect-funded-results-*`,
`deflect-funded-viewport-*` and `deflect-fallback-results-*` at 12/20, plus their
matching `deflect-*-report-*` exports. Funded Magic-type Voidwaker specials remain
supported; an ordinary Fang baseline and an admitted unfunded Voidwaker fallback
retain damage gaps. No controls or automatic evidence gates change.
[Source derivation, checks and limits](validation/nex-deflect-attack-type.md).

`nex-n3-plan` extends DEV1 with public-event capture, frozen N2 inputs, the native
conditional editor and the production N3A/B/C worker. It checks separate package
marginals, finite stock, missing HP, historical inspection and Reset while work
is pending. Run the normal full commands in TASKS; an individual replay is
`./gradlew -PruneLiteVersion=1.13.2 offlineReview --args='nex-n3-plan' --no-daemon`.
The editor's legal windows/HP/initial state are explicit synthetic assumptions,
not automatically attributed game data. See [validation](validation/nex-runtime-plans.md).

### Reviewing issue #44's native refinement

The corrected CTX1 baseline is `2fe53429526a2e43a6489e48af3cfba596925b03`.
[Selected before/after screenshots and reference comparison](validation/ui1-native-refinement.md)
record the same synthetic fixture at 225 × 600 and fonts 12/20. The complete
scenario additionally renders `pending-main-*`, `no-improvement-main-*` and
`comparison-viewport-*`; the context scenario adds `context-editor-viewport-*`.
Inspect full-height images as well as the viewport. First-screen checks include
observed duration, context Edit and the first comparison's benefit and phase.
That earlier iteration used offline diamond placeholders. The current review below
uses genuine pinned game art; live cache integration remains a client check.

### Genuine preview sprites and the PR #46 visual review

Current [before/after evidence](validation/ui1-46-visual-review.md) uses local,
unaltered game-art fixtures through the actual ItemManager/addTo presentation
path. See [ownership and separate terms](../THIRD_PARTY_NOTICES.md#preview-game-artwork)
and `src/test/resources/com/ironpath/sprites/manifest.json`. Missing IDs fail;
add a genuine PNG, its exact source and checksum when a fixture needs another
item. Keep these assets in test resources. The production plugin JAR contains
none of them and makes no new runtime requests.

`nex-visual` extends the same DEV1 harness with genuine multi-item packages,
expanded further comparisons and expanded Diagnostics after a real synthetic
inventory event. Inspect `main-*`, `expanded-*`, `expanded-viewport-*`,
`diagnostics-refreshed-*` and `diagnostics-details-viewport-*` at both fonts.
The original 12 scenarios remain, including CTX1's historical active-encounter
report/clipboard/navigation cases. Baseline production `997f923` plus the same
test sprite boundary/fixture produces the before views; its focused Diagnostics
expansion test must fail. Real client cache/OS behavior remains open.

## Issue #47 native review

Run both TASKS commands after UI changes. `nex-visual` includes same-fixture
Encounter, multi-item packages, team settings and Diagnostics at 225px
and fonts 12/20, with actual 600px viewports plus full-height PNGs. `nex-n3-plan`
retains explicit finite inputs and each required scenario choice. The first-screen
viewport explicitly resets position when reparenting a shorter-than-600px panel.
Empty status rows occupy no space; errors are checked when present.
[Before/after provenance, selected images and verification](validation/ui1-47-wording-layout.md).

## PR #48 review follow-up

Build 0.23.1 replaces top tabs with independent sections. Last kill remains
above Compare setups; secondary forms and Diagnostics start collapsed. DEV1
uses explicit section open/close actions and scrolls viewports to the section
being reviewed. This supersedes earlier tab/first-screen Diagnostics directions.
`nex-visual` adds collapsed-secondary, populated-correction and invalid-window
viewports. `nex-bank-trip` adds verified Waiting for Nex viewports, and
`nex-arena-reload` checks Loading encounter… before fresh public arena evidence.
Run both TASKS commands and inspect [the follow-up evidence](validation/pr48-review-followup.md).

Build 0.23.2 corrects the subsequent review's client-thread, explicit diagnostic
selection and suspended-scroll findings. Focused regressions dispatch a logged-in
profile change from a worker, select another kill/incomplete attempt after copying
a report, and lay out/drain the EDT during suspension before verified return.
The full pinned gate and normal build remain required.

Build 0.23.3 removes Gear & method and Gear & unlocks from the complete current UI.
The corresponding editor screenshots are retired; saved-preference/precedence
checks use the guarded backend. All 14 scenarios remain active. Main now includes
the complete CTX1/UI1/PR #48 lineage and this simplification.
[Scope and verification](validation/sidebar-simplification.md).

Build 0.26.0 extends `nex-n3-plan` with independent per-attack HP/caps/ruby and
Zaros conditions. Inspect `impact-inputs-*` and `impact-results-*` full-height
PNGs, `impact-hp-viewport-*` / `impact-zaros-viewport-*` native 225 × 600 views
and matching `impact-report-*.txt` schema-5 exports. Public-event replay includes
missing-row unknowns, shared complete-package conditions, new-kill freezing,
edited pending work and Reset. [Verification and open limits](validation/nex-impact-conditions.md).
