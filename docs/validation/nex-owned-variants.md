# Exact locked melee forms and comparison navigation

16 October 2026. Development build **0.16.6**, knowledge **n2-owned-2**.
Adapter `nex-public-4`, ordinary model versions and catalogue/preferences schemas
are unchanged. Earlier reports retain their original results and pending checks.

## Support correction

The melee catalogue included the normal Infernal cape and Dragon defender but
not their exact locked forms. A compatible ordinary melee state could therefore
fail before comparison. The new regression failed on the old catalogue.

| Exact form | Item ID | Stab / slash / crush | Strength |
|---|---:|---|---:|
| Dragon defender (l) | 24143 | 25 / 24 / 23 | 6 |
| Infernal cape (l) | 24224 | 4 / 4 / 4 | 8 |

Identifiers/names were checked against the pinned official RuneLite ItemID source;
limited bonus facts were cross-checked against the pinned equipment reference
and existing normal-item facts. [Sources and reuse boundary](../research/nex-owned.md).
No dataset, implementation, image or guide prose is imported. The new source URL
allowance is restricted to one repository, a 40-character revision and the exact
equipment JSON path; arbitrary repositories/files and revision mismatches fail.

Locked IDs remain exact in actual/proposed setups. Normal ownership does not
create locked stock, and broken/mangled forms are not normalized into usable
ones. Ordinary arithmetic, target assumptions, locks and charge/packing limits
are unchanged. Supported forms do not establish whole-encounter offence or a
global optimum among unsupported alternatives.

## Scenario presentation

Resource/inventory/level changes can create many distinct captured scenarios.
Unsupported specs, targets and special windows remain in the result. The main
view now distinguishes supported carried scenarios, bank preparation and
unavailable states; none is a fraction of fight time or damage.

The comparison view defaults to supported rows when possible. Role/support
filters and grouped gap counts make the other rows inspectable without deleting
them. Empty filter results are explicit. Filtering preserves actual references,
carried/bank scope, selected pairs and frozen inspected results. Identity changes
invalidate old controls and clear the view.

Complete-result display and a retained bank round trip have in-session evidence.
Short scene-only or replaced-spawn captures remain excluded from complete means.
During paused travel, Setup now shows the revalidation message rather than
claiming there is no complete kill. This corrects an independently reproduced
display path; it does not establish the chronology of separate client screenshots
or change retention/reset rules.
The main view follows the latest complete baseline; reopening any earlier kill's
comparison is not yet exposed. Further Setup/travel, comparable means, exact
charges/stock, packing and player-usefulness checks remain open. Raw screenshots,
inventory dumps, performance values and client traces are not committed.

## Verification

The normal `test build` passed on JDK 11 and RuneLite 1.13.2: **195 tests in 27
suites**, zero failures, errors or skips (31 seconds). Regressions cover exact-ID locked comparison,
damaged-form rejection, ownership distinctions, grouped gaps retaining states,
source restrictions, role/support filtering, empty selections, bank labeling,
paused/resumed Setup, stale identity controls and narrow/enlarged-font layouts.
Existing combat, capture, worker, preference, source and session regressions pass.
The exact locked-form comparison regression failed before the catalogue change.
Changed Markdown local links resolve, and `git diff --check` passes. Synthetic
captures were inspected at normal panel width; they are not live-client acceptance.
