# PR #48 review follow-up — 0.23.2

19 October 2026. Implements the [approved review](https://github.com/PetterHL/ironpath/pull/48#pullrequestreview-5570175742)
of `05527592b61ae7f1ca8e6b0a4217a14d82e9309b`, including its three findings,
arrival/status requirements and replacement of top tabs with independent sections.
The earlier [issue #47 evidence](ui1-47-wording-layout.md) retains its original
integration bases, arithmetic checks and before/after screenshots.

## Subsequent review corrections (0.23.2)

Addresses all three findings in the [review of `abdd3fc`](https://github.com/PetterHL/ironpath/pull/48#pullrequestreview-5570819614):

- Profile invalidation still clears generations, history, calculations and the
  panel immediately. Off-thread rendering uses Waiting for account without
  reading live account/world/varbit state; normal client-thread readiness resumes
  after the queued bank/login/dirty reset and fresh complete ticks.
- Kill details and Last attempt details select the same frozen diagnostic target
  and clear the previous export immediately. A merely arriving result still
  preserves the inspected report; incomplete attempts export no old calculation.
- The panel retains its pre-suspension scroll offset independently of the shorter
  paused viewport, including repeated pause renders. Verified return restores it
  within the available height; hard scope changes discard it and start at the top.

The three new/strengthened regressions failed against the reviewed implementation.
The scroll regression lays out the shorter paused panel and drains the EDT before
returning; its target stays clear of the report area that intentionally disappears.
The production section design, arithmetic and calculation scope remain unchanged.

Performed on the correction source with JDK 11.0.32.1 / Gradle 8.10:

- Pinned full `test build offlineReview --no-daemon`: **348 tests / 42 suites**,
  no failures/errors/skips; **14 scenarios / 1,009 checks / 222 PNGs**, no failures.
- Normal `test build` passed with the same up-to-date outputs; dependencyInsight
  confirms `latest.release` resolves to **RuneLite 1.13.2**.
- Checked all **56** regenerated viewports are 225 × 600. Inspected fresh
  `nex-complete` Encounter/Diagnostics viewports and `nex-visual` full-height main
  captures at both fonts 12/20; the approved layout is retained, and fresh
  Diagnostics shows build 0.23.2. The screenshot table below retains 0.23.1 evidence.
- Changed-document relative links and `git diff --check` pass.

The exact delivery revision and fresh CI/artifact links are recorded in PR #48.

## Result and boundaries

Last kill duration and Damage contribution stay above Compare setups (initially
expanded), Team & method, Gear & method, Gear & unlocks and Diagnostics (initially
collapsed, Diagnostics last). Native full-width arrow headers support Space/Enter
and report accessible expanded/collapsed state. There are no duplicate navigation
tabs or planner shortcuts detached from their selected comparison.

Section headers/bodies remain attached during refresh. Context editors and the
selectable diagnostic report keep their actual Swing subtrees; draft values,
selection/caret, nested report scroll and root scroll survive unrelated renders.
Gear drafts and N3 inputs/selected packages survive refresh and verified travel.
A session-local numeric scope changes on clear/identity/world/reset, allowing the
UI to distinguish verified suspension from a hard boundary without account IDs.
Suspension hides evidence, detaches planner actions and clears exports. Eviction,
annotation revisions and hard boundaries still invalidate old evidence/actions.

Status comes from actual client/session readiness. Verified arena and capture
readiness show **Waiting for Nex**; scene rebuilding/return evidence shows
**Loading encounter…**; active capture shows **Tracking kill…**; disabled capture
shows **Kill tracking: Off**. Unsupported account/world, settings loading/read
failure and outbound bank verification keep distinct short instructions.
Detailed transition reasons remain in Diagnostics.

Public-event replay covers portal/bank return, initial readiness, missing arena
NPCs and same-arena loading. These cases resume after the required evidence;
no additional stuck-capture defect was reproduced. The existing resumption,
identity, outbound-travel and reset rules are retained. Nearby Nex still cannot
resume an outbound leg before bank verification. This does not establish which
build/state produced the user's live paused message.

Plan validation reports the invalid field and its bounds on submission: whole
window 1–3000; energy 0–100% in 0.1% steps; restore 0–25; target/cap 1–100000;
player HP 1–255; 1–128 distinct increasing attack ticks before the exclusive end.
Cast/death lists use the same ordered bounded offsets. Optional blanks remain
unknown. Invalid input clears results/report and queues no calculation. The
numeric kernels and exact-package/unknown gating are unchanged.

Populated saved corrections show readable gear/unlock names and Owned / Not
owned or Unlocked / Locked states. Timestamps/manual provenance are in Diagnostics.
Internal keys, exact variant membership, stored values and observed-fact precedence
are retained. The replay saves Fang ownership and a false Rigour correction via
real UI actions; observed Rigour still wins in the account-fact calculation.

## Verification

Final check results and the current CI/artifact links are recorded in PR #48.
The prescribed checks are:

```sh
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --no-daemon
./gradlew test build --no-daemon
```

Environment: prepared JDK 11.0.32.1, Gradle 8.10, DejaVu Sans, RuneLite 1.13.2.
The normal build's actual resolved dependency is recorded with the final results;
no newer-client compatibility claim is made.

Focused regressions: distinct arrival/account/world/settings statuses;
window/energy/HP/tick errors; readable populated corrections and observed precedence;
independent keyboard/accessibility toggles; draft/planner/selection/report/caret/
scroll retention; verified travel; hard-scope clearing; pending default selection;
negative alternatives; clipboard fallback; stale copy/image/preference actions.
The existing 14 DEV1 scenarios retain public-event banking/reload, long outbound
travel, account/reset/stale workers, historical corrections during another active
attempt, 50-record eviction and declared N3 arithmetic/report checks.

## Native screenshots

Retained 0.23.1 production Swing captures, 225px width, body fonts 12/20, genuine pinned
item sprites through the test ItemManager boundary. Viewports are exactly
225 × 600; full-height files are also in the CI artifact. These are synthetic
component captures, not live gameplay or OS-window screenshots.

Created using intellectual property belonging to Jagex Limited under the terms
of Jagex's Fan Content Policy. This content is not endorsed by or affiliated
with Jagex. See [separate artwork terms](../../THIRD_PARTY_NOTICES.md#preview-game-artwork).

| State | Font 12 | Font 20 |
|---|---|---|
| Result and open comparisons | [PNG](images/pr48-review/result-12.png) | [PNG](images/pr48-review/result-20.png) |
| Collapsed secondary sections | [PNG](images/pr48-review/collapsed-12.png) | [PNG](images/pr48-review/collapsed-20.png) |
| Expanded Team & method | [PNG](images/pr48-review/team-12.png) | [PNG](images/pr48-review/team-20.png) |
| Expanded Gear & method | [PNG](images/pr48-review/gear-12.png) | [PNG](images/pr48-review/gear-20.png) |
| Populated Gear & unlocks | [PNG](images/pr48-review/unlocks-12.png) | [PNG](images/pr48-review/unlocks-20.png) |
| Expanded Diagnostics | [PNG](images/pr48-review/diagnostics-12.png) | [PNG](images/pr48-review/diagnostics-20.png) |
| Verified Waiting for Nex | [PNG](images/pr48-review/ready-12.png) | [PNG](images/pr48-review/ready-20.png) |
| Transient invalid-window feedback | [PNG](images/pr48-review/invalid-12.png) | [PNG](images/pr48-review/invalid-20.png) |

Sources: `nex-visual`'s encounter, secondary-collapsed, team, gear, populated-unlocks,
diagnostics and invalid-window viewports; `nex-bank-trip`'s waiting-for-nex viewport.
The existing suite checks text/control geometry; whole-image inspection is recorded
with final validation. No screenshots are relabeled from the prior delivery.

Live event/cache behavior, portal/banking/reload retesting in the actual client,
OS focus/keyboard/scroll/DPI/clipboard and player acceptance remain open. No new
Nex kill is required for this offline review. Merge and Plugin Hub publication
remain separate from completing the review follow-up.
