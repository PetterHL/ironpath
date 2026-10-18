# N3 runtime plan integration validation

18 October 2026. Development build **0.22.0**, based on main `96d91e8`.
Original implementation/tested code commit: `225f7dd08abe4862614b14460e001e22b3b813ae`.
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

## PR #45 review corrections

Tested correction commit: `87b6acd749798ba0d94028e641d9277329eed093`.
Runtime join `nex-declared-plan-2`, package bridge `nex-package-4`, standalone
plan report schema 2. The three P2 findings are corrected:

- Packing starts from immutable captured free space and occupied stacks. Each
  scenario releases only its own modeled equipment/projectile stack slots, keeping
  other carried gear/supplies reserved. The full 28-slot Dragon 2h / Infernal cape /
  26-shark reproduction leaves the owned plan unavailable before and after a
  hypothetical cape edit. A 27-slot counterpart remains available with identical
  captured-to-owned damage/resource results across the same edit.
- Hypothetical projectile selection follows the resulting weapon and physical
  ammo compatibility. ZCB / blessed quiver / ten ruby dragon bolts can change to
  Voidwaker with only the weapon edit; the physical ammo slot stays unchanged,
  the plan performs its declared melee special and spends no bolts.
- The standalone allowlist exports frozen real/visible levels, prayer flags,
  style button and derived family/prayer/style plus sanitized captured/model
  target/phase/window and provenance. Assertions check Piety, Strength 118 and
  Smoke/base context, with public-event DEV1 checks for Shadow/Fear the shadow.
  Unexpected skill/prayer/context strings and unrelated inventory remain excluded.

All three new unit regressions failed against the previous code. After correction,
the focused runtime/package suites pass **41 tests**. Full pinned
`./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --no-daemon` passes
**317 tests / 38 suites, zero failures/errors/skips; 10 scenarios / 569 checks,
zero failures; 130 PNGs**. All nine baseline scenarios remain green.
`./gradlew test build --no-daemon` also passes with tasks up to date; dependency
inspection resolves normal `latest.release` client/jshell to **1.13.2**. JDK and
Gradle remain 11.0.32.1 / 8.10. Missing RuneLite dependencies were resolved online
through the configured proxy before verification; subsequent runs reused that cache.

Inspected both native 225 × 600 report viewports at 12pt/20pt and the generated
standalone TXT contents. The existing selectable-report/clipboard fallback remains
usable. DEV1 now publishes `nex-n3-plan/plan-report-{12,20}.txt` alongside its PNGs
and six new report checks. Markdown links and `git diff --check` pass. Current-head
CI/run/artifact evidence is linked from PR #45. No generated output is committed;
the remaining client/gameplay acceptance below is unchanged.

## Remaining acceptance

The initial implementation validation did not launch a real client. A later guided
client exercise is recorded below. Existing N1/N2/UI1 client checks,
real ItemManager sprites, native DPI/focus/scroll/clipboard behavior and player
usefulness remain open. CTX1 is still in its own PR; this branch does not merge it.
No Plugin Hub or merge authorization is implied. The optional plan does not
establish automatic capture-to-recommendation evidence, Zaros transformations,
real cast/proc attribution, target HP trajectories or a full encounter simulation.

## 0.22.1 guided-client follow-up

A guided live exercise on 0.22.0 displayed complete kill results and resource
readings. A paused-travel report correctly hid account/kill data; a later READY
report selected an unavailable initial target row rather than the displayed
improvement. This does not establish the Fang card's exact before/after inputs.
The player subsequently confirmed that the same completed result remained visible
after opening the bank, establishing one live bank return in this exercise. The
resource panel's first/last energy readings and the natural-restoration examples'
admitted-use ranges were also confirmed to match the exported report. This is
display agreement, not evidence of actual spec counts. No raw report, performance
record, account identifiers or private container contents are committed.

A synthetic regression reproduces Diagnostics opened before the post-kill worker
finishes: index zero remains selected after a positive later-state card arrives.
0.22.1 resolves that default once a matching frozen calculation arrives, choosing
the same first visible improvement (or a supported row when no positive card
exists). Explicit inspected selections, including an empty filtered selection,
remain unchanged. The default is not reselected on later refresh/new kills.

Unavailable-row exports also retain allowlisted exact observed slots and a fixed
vocabulary target class. Raw target/spawn keys, unknown slot names, item-name maps
and unrelated inventory contents remain excluded. Hidden states still export no
observed slots.

Combined validation, including the PR review corrections above, passed with the
pinned RuneLite 1.13.2 command: **319 JUnit tests across 38 suites**, with no
failures/errors/skips, plus **10 DEV1 scenarios, 569 checks and 130 PNGs**.
The normal `./gradlew test build --no-daemon` also passed with tasks up to date.
The pending-selection regression covers a paused session revalidated at a bank,
including reports opened while paused, while pending and after the matching
calculation arrives. Inspected the regenerated Diagnostics and N3 report viewports
at 12pt/20pt. Published CI evidence is linked in the PR.

The exact Fang comparison and N3 declared-plan player checks remain open. One
same-kill bank return is now reported as passing; broader identity/world/reload
and client checks remain open. This does not label the whole client test accepted.
