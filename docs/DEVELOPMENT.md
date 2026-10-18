# Development and verification

Use JDK 11, Git and the included Gradle wrapper (8.10). The retained build resolves
RuneLite through `latest.release`; the last integrated verified dependency was
1.13.1. Record the actual resolved version when verifying a change. The first build
needs dependency downloads. Runtime evaluation is local.

## Checkout and run

For an existing clean checkout, update main with `git fetch origin`, `git switch
main` and `git pull --ff-only`, then create a short-lived branch for the change.
Preserve any local changes and unrelated branches. Close the development client
before updating or rebuilding it.

The 18 October consolidation makes main the common base for N3b/N3c, banking/
arena-reload fixes and all nine DEV1 scenarios. Do not start from the retired
stacked PR branches. TASKS.md owns the order: UI1, then CTX1, with live acceptance
tracked separately. After merging future authorized work, delete only heads whose
commits are preserved in main (or an explicit archive); never discard unique work.

Windows PowerShell:

```powershell
.\gradlew.bat test build --no-daemon
.\gradlew.bat run
```

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

The offline workflow supplies nine synthetic scenarios without OSRS credentials,
a display server or gameplay: the seven DEV1 baseline cases plus two bank/reload
regressions from PR #38. The first calculation/UI checkpoint was reviewed on
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

Outputs are regenerated in `build/review/<scenario>/`: `report.md` plus the named
PNG states with `-12.png` / `-20.png`, all at the native **225px** width. Running
`all` also writes `build/review/index.md`, linking every report and PASS/FAIL result.
The complete suite generates nine reports and 56 PNGs. Each scenario and font
starts with fresh client/account/session state. Reports contain input provenance,
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
Empty item sprite wells are explicitly labeled placeholders; no ItemManager/cache
sprites are loaded. Images paint full component contents and may be tall. Native
combo selections may elide, with full selected claims in wrapping text below.
These are not screenshots of an OS window or real DPI/focus/scroll/GPU tests.

The layout checker rejects visible nonempty text areas outside the PNG or any
ancestor inside the capture root, zero-sized/compressed areas, and wrapping that
exceeds the actual allocated content width/height. Hidden/empty areas are ignored.
Failures still write the PNG and the affected text/reason. Visual inspection
remains necessary; layout checks do not establish usefulness or interaction quality.

The **Offline review** GitHub Actions job runs the pinned test/build and all nine
scenarios. It uploads `dev1-offline-review`, containing the index, reports and PNGs
with 30-day retention, including available failure evidence. Link the exact run
and artifact from the PR. Routine generated output stays outside Git history.
Regenerate after source changes; never relabel stale evidence for a new revision.

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
