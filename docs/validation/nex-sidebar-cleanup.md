# Nex sidebar cleanup — 0.32.2

26 October 2026. The user requested cleanup before the next client test.
This follows PR #57 head `8db2d3d` on `n3/fang-advice`; it changes presentation
without promoting the provisional action mappings or numerical support.

## Result

Compare setups now shows one automatic **Fang equipment** preview. The former
per-state theoretical DPS highlight cards, More/Fewer comparisons and Inspect
wrapper have been removed. **Base-stat comparisons** explicitly opens the
existing per-state results, with the complete supported/unavailable state filters,
zero/negative alternatives, full gear setups and optional declared planner.
The numerical headline retains the words **theoretical DPS** there.

The Fang card's **Diagnostics** action opens the frozen calculation basis.
**Kill details**, Team & method, tracking and Clear session remain available.
Session counts now live in Diagnostics, without a separate homepage toggle.
The obsolete card renderer, unused presentation helpers and toggles are removed.

The PR review's remaining frozen-selection defect is corrected: opening a
Fang alternative now stores its index with the diagnostic calculation rather
than reading the mutable latest-kill selector. A new kill can update the main
card while the older diagnostic result and selected row stay visible. Explicit
selection changes, calculation replacement, eviction and scope resets clear the
dependent selection; suspended evidence stays hidden under the existing guards.

## Active recording status

The user also reported Loading encounter remaining visible during a kill and
requested clear evidence that recording is working. A coalesced account read
could advance the account revision after queuing ACTIVE, invalidating that view.
The status cache then treated the unpainted request as displayed and suppressed
the next active render. Full status publication now follows the read, and the
cache acknowledges only a successfully painted view. Delayed EDT work can retry
against the latest account snapshot instead of leaving an old loading label.

During active capture, Tracking kill is accompanied by recorded seconds and the
observed phase. A scalar immutable capture snapshot feeds a lightweight text
update each game tick; it does not rebuild the sidebar, read client state from
Swing, infer successful attacks or run comparisons during combat. Missing smoke
start stays Start unconfirmed; time freezes at observed death while completion
is reconciled. Generation/session/capture guards discard obsolete progress.
Tracking remains visible if settings are read-only while capture still works;
inactive settings failures retain their distinct status.

The arrival regression deliberately blocks painting across two dirty account
reads, checks Tracking kill and advancing time/phase, verifies the same component
is retained for heartbeats, then reloads and checks that active progress clears.
DEV1 additionally captures active 225 × 600 screens at fonts 12 and 20.

README now describes the current build and links to the validation history;
it no longer interleaves old release announcements and superseded availability
claims with the current workflow. Current design/UX guidance is reconciled with
the already removed gear/unlock sections. Historical validation records remain.

No combat kernels, package search, evidence-model versions, report schema, saved facts,
scenario confirmations or evidence lifetimes change. Missing or ambiguous Fang
evidence still withholds numbers. Selected reports remain frozen to their kill.

## Verification

The focused selection regression passes at fonts 12 and 20, including a second
complete kill with a new populated Fang selector and reset. The adapted genuine
Fire cape sprite regression passes, including delayed ItemManager completion
and rejection after identity clearing. No new image assets are required.

The delayed-painting regression independently reproduces the previous plugin's
stale Waiting for Nex label when Tracking kill is expected, then passes with
the corrected publication and heartbeat at fonts 12 and 20. This establishes
the race without claiming it is the only possible source of the live symptom.

Full review uses the prescribed commands:

```sh
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --no-daemon
./gradlew test build --no-daemon
```

[PR #57](https://github.com/PetterHL/ironpath/pull/57) records the performed full
build results and links the current revision's CI reports and native screenshots.
Navigation regressions retain all alternatives and selection across refresh,
Diagnostics and Back; lifecycle, frozen report and planner checks remain intact.
DEV1 includes native 225px views at fonts 12 and 20, plus actual 600px viewports
for the main Fang card, explicit base-stat comparisons and active capture.

The subsequent [matched client check](nex-fang-live-0322.md) confirms active
Tracking kill/time/phase and the cleaned main card for one run. Broader
client/player acceptance, including manta-ray and DC message checks, remains
pending. No merge or Plugin Hub publication is authorized by this cleanup.
