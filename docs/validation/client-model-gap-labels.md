# Client model-gap explanation

13 October 2026. Branch `client-model-gap-labels` starts at integrated main `9035a06`. Plugin patch **0.6.2**; knowledge **reference-1**, catalogue schema **8**, combat **powered-magic-2** and preferences remain. This focused explanation fix is independent of the draft R2 ranged models.

## Supplied client finding

Two supplied RuneLite images show Shadow Value using Enemy with 0 Defence, DPS first, supported max hit, four→five ticks and removed off-hand; Ancestral hat keeps max hit first and four→four ticks. The expanded reference confirms **Brimstone ring** with **tormented bracelet**, so the existing unsupported accuracy proc is the reason hit chance/DPS are withheld. Confliction is not the cause in that supplied reference. The prior generic “history mechanic” label was misleading for Brimstone. No raw reference/account dump, observation timestamps or identifiers are saved. Scaling, full lower-page visibility, keyboard/Back, other account transitions and usefulness remain pending; these screenshots do not establish a DPS model pass.

## Fix and validation

Comparison results carry immutable per-metric gap explanations. Derive them only from items present in each complete setup, label Before/After/Before and after, and keep the one-handed restriction on Confliction. The visible Value metrics explain the exact item/mechanic without requiring assumptions disclosure. The consequential blocker uses that same focused explanation. Brimstone's unsupported proc and Confliction's unsupported stacks remain guarded; no numerical formula/support/ownership/default-reference change is introduced.

`test build --no-daemon` passes **165 tests, zero failures/errors/skips** on Temurin 11.0.32.1 / RuneLite 1.13.1. Retain 180 neutral, 234 P4a and 540 R1 numeric cases unchanged. New behavioural checks cover Brimstone alone, Confliction alone, both, candidate-only Confliction, and a two-handed replacement removing only the candidate's history gap. UI checks preserve weapon/armour metric order, show the precise gap before opening assumptions and restore supported numbers after an explicit empty-ring reference correction. Existing native widths/enlarged fonts and lifecycle checks remain. `git diff --check` passes. No new live retest of the corrected copy is claimed.

Fresh synthetic headless Swing captures at 225 logical pixels:

- [Brimstone weapon gap](../images/client-gap/brimstone-gap.png)
- [Brimstone armour gap](../images/client-gap/brimstone-armour-gap.png)
- [Explicit empty-ring control](../images/client-gap/explicit-empty-ring-control.png)

These use fallback sprite wells, not in-game capture. Original screenshots/reports and pending checks remain historical.

## Continue the client test

On current main, an explicit reference correction to an observed supported alternative ring, or **Deliberately empty ring**, can exercise the numeric display without claiming measured Brimstone performance. Keep the control label and inspect both before/after setups. Restore Brimstone afterwards and verify the gap returns. On the explanation branch, the visible gap should name Brimstone before and after and should not blame Confliction when it is absent. Test weapon/armour detail, full wrapping and Back. A reviewed Brimstone-proc model with ring-alternative/package fixtures is the separate quantitative follow-up in TASKS.
