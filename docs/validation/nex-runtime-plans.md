# N3 runtime plan integration validation

18 October 2026. Development build **0.22.0**, based on main `96d91e8`.
Implementation/tested code commit: `225f7dd08abe4862614b14460e001e22b3b813ae`.
[Contract and limitations](../research/nex-runtime-plans.md).

## Delivered

- One optional post-kill runtime path joins existing N3A finite energy/Death Charge,
  N3B damage/ammo and N3C complete-package comparison kernels on the production worker.
- Frozen captured → owned and owned → hypothetical gains remain separate. Complete
  exact packages, shared opportunities, delays, caps, stock, displaced equipment,
  planning locks and declared hypothetical quantities are checked together.
- Unknown HP preserves independently supported resources; unknown starts/stock/
  opportunities never become fabricated inputs. Unsupported phases remain scoped gaps.
- N3A natural-only examples enumerate fixed-Lightbearer phase/order sensitivity,
  separately from observed energy readings and explicitly declared DC events.
- Native optional editor, guarded requests, allowlisted selectable export, provenance
  and clipboard fallback use the existing UI/lifecycle boundaries. No saved plans,
  performance persistence, automatic weapon recommendation or team-time prediction.

## Performed verification

Pinned JDK 11.0.32.1 / Gradle 8.10 / RuneLite 1.13.2 validation passes:
`./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --no-daemon`.
**314 tests across 38 suites, zero failures/errors/skips; 10 DEV1 scenarios,
563 checks, zero failures, 130 PNGs.** All nine prior scenarios remain green.
The normal compatibility command `./gradlew test build --no-daemon` also passes
against the same resolved RuneLite 1.13.2 classpath (tasks up to date). Dependencies
were available in the prepared cache; this is not evidence of a fresh download. Existing
RuneLite/Gradle deprecation and headless font-cache diagnostics remain non-fatal.

Inspected native 225px images include the N3A examples, declared plan, full results,
unknown-HP output and 600px viewports containing Evaluate/Copy/report controls at
12pt/20pt. Early clipping and layout-cache findings were corrected before the
passing run. Changed-document links and `git diff --check` pass. No routine generated
output or private captures are committed. Current-head CI and report/PNG artifact links are recorded in
[PR #45](https://github.com/PetterHL/ironpath/pull/45).
Independent hand expectations cover full-package VW damage 34.5 → 38 → 40,
separate +3.5 / +2 marginal gains, ZCB ruby proc/accuracy, a one-bolt exhaustion,
rejected spending, explicit improved DC charges and ordinary fallbacks. Unknown
start/HP/inventory, exact forms, locks and immutable inputs retain targeted tests.
Request/edit/detach tests reject stale publication and preserve selectable reports
on clipboard failure.

DEV1 adds `nex-n3-plan` to the original nine-scenario harness. It runs public-event
capture through the real plugin/N2 worker/native editor/N3 worker, checks finite
plans and separate deltas, missing HP, frozen older inputs after a new kill, and
Reset while a plan worker is pending, at 225px/12pt/20pt. All scenario inputs,
legal attack windows, resource use and HP are explicitly synthetic assumptions.

## Remaining acceptance

No real client/gameplay test is performed. Existing N1/N2/UI1 client checks,
real ItemManager sprites, native DPI/focus/scroll/clipboard behavior and player
usefulness remain open. CTX1 is still in its own PR; this branch does not merge it.
No Plugin Hub or merge authorization is implied. The optional plan does not
establish automatic capture-to-recommendation evidence, Zaros transformations,
real cast/proc attribution, target HP trajectories or a full encounter simulation.
