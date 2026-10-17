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

The first checkpoint supplies one scenario, `nex-complete`. It runs without OSRS
credentials, a display server or gameplay. This is a synthetic integration check,
not real-client validation. [Checkpoint results](validation/dev1-offline-review.md).

Prepare JDK 11, the Gradle 8.10 wrapper and **DejaVu Sans** (`fonts-dejavu-core`
on Debian/Ubuntu). Set `JAVA_HOME` to that JDK and use a writable Gradle user home.
Keep the session's configured HTTPS proxy and CA trust. In Cloud, Java/Gradle need
the corresponding proxy host/port and supplied CA truststore as well as the shell
proxy; do not disable TLS verification. The prepared IronPath Cloud environment
currently provides `/workspace/ironpath-environment/activate.sh` for these settings;
source it when present. It is environment setup, not a repository dependency.

From the repository root:

```sh
# First run downloads Maven/RuneLite dependencies if not already cached.
./gradlew -PruneLiteVersion=1.13.2 offlineReview --args='nex-complete' --no-daemon

# Once prepared, the same review works without network access.
./gradlew -PruneLiteVersion=1.13.2 offlineReview --args='nex-complete' --offline --no-daemon

# Affected integration plus retained adapter, calculation, UI and lifecycle checks.
./gradlew -PruneLiteVersion=1.13.2 test build --offline --no-daemon

# Separate compatibility check against the normal latest.release dependency.
./gradlew test build --no-daemon
```

Use `gradlew.bat` in PowerShell. The explicit Gradle property pins both RuneLite
client and jshell for the review; ordinary builds still default to `latest.release`.
Keep the pin in the command when sharing results. The report records the actual
client/API JARs, JDK, Gradle, adapter, evaluator, catalogue and comparison model
versions. Do not replace the review pin merely because a newer dependency resolves;
check compatibility separately and review a deliberate pin update.

Outputs are always regenerated under `build/review/nex-complete/`:

- `report.md`: outcome, timer/damage scope, ordered callbacks, comparison inputs,
  sources/results, named gaps, independent expected/actual checks and limits.
- `main`, `comparison`, `basis`, `gap`, `resources`, `kill-details`, each with
  `-12.png` and `-20.png`: actual native Swing at 225px and two font sizes.

The runner exits nonzero on failed checks and still writes a failure report. It
removes previous PNGs before replay, so failed execution cannot leave old images
masquerading as current results. `offlineReview` deliberately has no Gradle output
cache; deleting a PNG does not require `clean` or `--rerun-tasks`. Unknown scenario
IDs fail before replay. Tests also generate independent temporary previews; these
do not substitute for running the review command.

Open the report and PNGs together. Compare the fixed checks first, then inspect
the narrow layout and explanation of gaps. The real RuneLite look and feel is
loaded; DejaVu Sans 12/20 gives consistent Cloud text metrics. Empty item sprite
wells are explicit placeholders; no cache/ItemManager sprites are loaded. The
images show full scrollable component contents, not a screenshot of an OS window.
Long native combo-box selections may elide; the selected setup and claims remain
in wrapping text below them. Do not treat these as real DPI/focus/scroll checks.

The text-layout check rejects visible non-empty text areas outside the PNG or
any ancestor's bounds inside the captured root, including zero-sized areas.
It also checks usable space after insets, glyph width and wrapping at the actual
allocated width, independently of an overridden preferred size. Hidden/collapsed
sections and empty text areas are ignored. A failed check still writes its image
and reports the affected text and clipping reason.

The **Offline review** GitHub Actions job runs the pinned test/build/review for PRs
and uploads `dev1-nex-complete` (report plus twelve PNGs, 30-day retention), including
available failure evidence. Link the workflow run/artifact from the PR. Routine
generated outputs stay outside Git history. Regenerate after source changes; do
not relabel stale output as evidence for a new revision.

### Scenario boundaries and subsequent changes

`OfflineReview` selects a development-only scenario from its registry. The Nex
fixture supplies mocked public client state at every callback consumed by the
adapter, including barrier event payloads ahead of polled state. It runs the
production `NexObserver`/`CombatStateReader`, `NexCapture`, `NexSession`, and
`NexOwnedEvaluator`, then renders the actual `NexPanel` on the EDT. Completion is
detected from public-event signals, never by inserting a completed summary.
The evaluator runs on a separate worker. Account/unlock facts and preferences
are explicit synthetic fixture inputs rather than a mock login/account read.
The first checkpoint does not exercise the plugin's queued-publication guards.

Shared `dev/OrderedReplay`, `ReviewReport` and `PanelPreview` contain no Nex phases
or summary types. A future encounter supplies its own fixture/adapter and registers
a scenario callback. Retained `ZilyanaObserver`/`EncounterCapture` are a second
architectural reference, not newly enabled boss support. Keep client values and
event ordering in the encounter fixture, not the shared runner. Everything is in
test sources; no fixture loader or replay control ships in the plugin JAR.

For a calculation change, add/revise a reviewed hand case or independent oracle
with its justification, run the same command and inspect the expected/actual table.
For a UI change, use the existing semantic button/combo names and regenerate both
font sizes. Do not auto-bless current output or copy a new value into expectations
without an independent reason. `OfflineReviewTest` runs the entire first scenario
and checks fresh PNG generation; existing lifecycle/arithmetic tests stay in place.

Wait for the first checkpoint review before adding the rest of DEV1's scenarios
(unknown inputs, incomplete attempt, bank/next kill, controlled pending identity
reset and fresh results during historical inspection). Synthetic verification
does not close any N1–N4 live mappings, resource capture, client presentation or
player-usefulness checks. No real encounter recorder/export is implemented.
