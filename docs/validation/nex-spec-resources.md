# N3a resource foundation validation

16 October 2026. Development build 0.17.0, based on N2 `9ca9adb` (0.16.5).
Model `nex-spec-resources-1`. Separate branch `n3/nex-spec-resources`; no merge or
Plugin Hub publication. [Mechanics and limitations](../research/nex-spec-resources.md).

## Delivered behavior

- Post-kill owned details expose frozen energy/Death Charge/ring evidence, with
  optional ordered readings. Unknown inputs stay unknown; sampled deltas are not
  identified specs/procs or gross restored/spent energy.
- Independent finite scenarios share energy, caps and attack delays across
  Voidwaker, ZCB and ordinary attacks. Cast cooldown/expiry, distinct eligible
  deaths, normal/improved charges, wasted restoration and explicit same-tick
  policies are represented. Unknown fixed-ring restoration phase can be explored
  without assigning probabilities.
- Scenario inputs are assumptions, not inferred from captures. No spec damage,
  bolt outcomes, ammunition sufficiency, survival, upgrade ranking, contribution
  or team-time claim is added. The current capture adapter is unchanged.

## Verification

Targeted resource tests pass: 12 tests across `SpecTimelineTest` and
`NexResourceReviewTest`. Hand calculations cover 50/75% costs, 25/50-tick restore,
loss at the cap, no banking of wasted energy, two-charge exhaustion, expired and
duplicate deaths, cast-based cooldown, energy competition, displaced ordinary
attacks, ZCB style delays, same-tick ordering, unknown restoration phase, invalid
and bounded inputs, unknown readings and pre-smoke exclusion.

The normal JDK 11 / Gradle 8.10 / RuneLite 1.13.2 `test build --no-daemon` passes
**201 tests across 29 suites, zero failures/errors/skips**, including all retained
arithmetic, unknown-state, package and lifecycle regressions. The final rerun also
checks the optional resource-reading selector.

Native Swing tests capture 225px width at 12pt and 20pt, collapsed/expanded resource
details, selection, a new kill arriving during frozen inspection, and account
reset. Rendered images were inspected; text wraps within the native width. These
are synthetic empty-sprite captures in `build/validation/n3`, not real-client
acceptance. Changed-document links and `git diff --check` pass. The capture adapter
and historical archive are unchanged. Gradle retains its existing deprecation notice.

## Remaining acceptance

No real client was launched and no live kill/player acceptance was performed.
N1/N2 live checks remain open. Retest on the eventual combined branch after any
capture fixes. Check native 225px/large-font rendering with real sprites and
confirm the resource wording is useful to Petter after a complete capture.

Before resource scenarios become recommendations, establish cast/launch/proc
evidence, initial spell/cooldown and restoration phase, ring-switch behavior,
supplied attack windows, ammo/charges, target HP/bolt/spec damage and ordinary
attacks displaced. Calibrate the exact expiry/order boundaries. Implement the
remaining N3/N4 acceptance in TASKS without promoting this foundation to acceptance.

## 0.17.1 combined integration

16 October 2026. N3a is integrated with N2 0.16.6 (`f30453a`), preserving
knowledge `n2-owned-2`, the role/support filters, grouped gaps, exact locked melee
forms and paused-travel status. Resource review and grouped gaps are both built
into the same immutable post-kill result on the guarded worker. Capture adapter
`nex-public-4` and resource rules `nex-spec-resources-1` are unchanged.

Four conflicting files were reconciled explicitly: development version, current
instructions, README and the immutable result constructor. A previously unordered
synthetic N2 fixture was corrected to use chronological, in-interval sample ticks;
the resource review's ordering guard is preserved. An integration regression joins
locked melee comparisons, ordinary filters, resource-reading selection, temporary
travel suspension, stale controls and a new kill arriving during inspection.

The combined JDK 11 / Gradle 8.10 / RuneLite 1.13.2 normal `test build` passed:
**209 tests across 29 suites, zero failures/errors/skips** (33 seconds). This
includes both branches' retained regressions and the new integration case. Native
resource/filter captures were inspected at 225px, including enlarged resource
text. Changed Markdown links and whitespace checks pass. The pinned equipment
cost and RuneLite timer/handler references were fetched again; their SHA-256
values match those recorded above. Existing acceptance limits remain open; the
merge does not establish live resource attribution or spec damage.
