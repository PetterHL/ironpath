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
For Jagex Account login use [RuneLite's instructions](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).
Never commit credentials, account captures or local configuration. If using the
credential-file development flow, remove the temporary credential-writing launcher
argument afterwards and remove the local credential file when testing is finished.

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
