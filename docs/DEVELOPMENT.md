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
