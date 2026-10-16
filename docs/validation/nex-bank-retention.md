# Nex completed-history loss during bank travel

16 October 2026. Development build 0.19.1, based on N3c `99356a1` / PR #37.
Capture arithmetic and adapter version `nex-public-4` are unchanged. This report
records a failed client check and a tested code correction; the corrected client
round trip still needs validation.

## Client evidence

Petter supplied screenshots from the current test client:

- Kill #1 was COMPLETE with the correlated Nex death/dying and arena-end reason.
  Displayed smoke start 237, death/dying 811 and arena end 818. The internal
  duration was `(811 - 237) * 0.6 = 344.4` seconds. All five phases, continuous
  samples, 574/574 ticks and zero unclassified hits were displayed.
- The main result showed one complete/eligible attempt, session n=1 and observed
  Nex/minion hitsplat share 44.2%. This does not calibrate the game timer or
  establish official server contribution, team composition or all live mappings.
- After leaving the encounter, the result became unavailable before bank opening.
  A subsequent screenshot showed Ready to observe and 0/0 captured attempts.
  Petter explicitly confirmed that the bank was open for that screenshot.
- Petter noted that opening the bank may have been delayed. Exact elapsed ticks,
  exit callbacks and live reset cause were not recorded. The former one-minute
  expiry is a plausible cause, not a measured diagnosis of that specific trip.

This bank-retention check failed. Earlier successful screenshots and prior
reports do not override it. No raw account/bank dump, character identifier,
performance persistence or user screenshot was added to the repository.

## Reproductions and correction

Three new regression cases failed against the previous code before the fix:

1. `NexSession` cleared completed history after 100 ticks (roughly 60 seconds)
   while waiting for the bank, or during a longer return leg. Completed summaries
   now remain bounded to 50 attempts in memory until a hard account/session
   boundary. They stay hidden during pending travel and cannot be appended to
   while suspended. Opening the verified bank restores the original immutable
   result, without reconstructing it from the screenshots.
2. Arena-like barrier/Nex evidence before the outbound bank opened also cleared
   history. A nearby loaded Nex is insufficient to identify a return trip. The
   outbound leg now remains paused until bank verification; only a recognized
   bank-origin return can resume from arena evidence.
3. An exit-barrier callback before LOADING made the departure look unrecognized.
   `NexObserver` retains its verified arena source through that callback until
   the scene transition. `transition`/`clearActive` consume/reset it, so a nearby
   NPC outside the arena does not create an unrestricted travel source.

The combined plugin regression uses LOADING/LOGGED_IN, an exit barrier, delayed
bank opening with a nearby Nex, a late return, and actual native panel rendering.
After bank revalidation the same completed summary and n=1 are visible again.
An additional adapter test checks the departure-source lifetime and reset.

No timeout is needed to bound already bounded immutable summaries. Live account
snapshots, actor references, active capture buffers and pending worker/action
tokens retain their existing clearing behavior. Unknown/changed identity,
profile/character/world change, logout/disconnect, unsupported worlds, disable
and Reset still clear the session. Clock rollback before the travel origin is
rejected. Old time-expiry expectations were replaced with these hard boundaries.

## Automated verification

The affected NexSession, observer, plugin lifecycle and native panel suites pass.
The three initial reproductions failed before the production correction; the
adapter source-lifetime case adds coverage of its consumption/reset.

Final normal cached-dependency command:

```sh
source /workspace/ironpath-environment/activate.sh
XDG_CACHE_HOME=/tmp/ironpath-cache ./gradlew test build --no-daemon --offline
```

- Temurin JDK 11.0.32.1+1, Gradle 8.10, RuneLite 1.13.2.
- **261 tests across 32 suites; zero failures, errors or skips.**
- Four new regression cases; existing account/profile/world/logout/reset,
  unsupported-world, bounded-history, stale worker/action, combat and N3 cases pass.
- Normal `build/libs/ironpath-0.19.1.jar` assembly succeeds.
- `git diff --check` and changed-document relative file links pass.
- The integration assertion checks the native panel's Last complete kill after
  bank revalidation. No graphical client was launched for this correction.

## Client retest

Use branch `fix/nex-banking-session`, build 0.19.1. Restarting the development
client starts a new in-memory session; the already cleared kill cannot be restored.

1. Capture one complete Nex kill and note the displayed duration and n=1.
2. Leave the arena, deliberately wait more than a minute before opening the bank,
   then open it. The original duration, counts and encounter details must return.
3. Close the bank and return on the same account/profile/world. The same result
   must remain, then a second complete kill must add to complete counts (cohort
   means combine only when the observed method/setup/team evidence matches).
4. Check a hard boundary separately: logout/Reset clears the session, and a later
   bank opening must not revive it.

No corrected live-client result is claimed yet. Broader N1/N2 capture, resource,
multi-trip and player checks remain open in [TASKS](../../TASKS.md).
