# UX, ordinary melee and KI1 integration

14 October 2026. Combined branch `integration-ux-melee-ki1`, preserving the complete histories from integrated main `2dd799a` and these finished increments:

| Increment | Source head | Review / evidence |
|---|---|---|
| UX completion and Browse discovery | `ebcd5a28d0196aa82a64d685b0c766ac4ebf9b6a` | [PR #21](https://github.com/PetterHL/ironpath/pull/21), [UX validation](ux-completion.md) |
| Ordinary melee and tentacle path, first R3b increment | `b2ae8c5620777f947bb46e8fbfb94d7e522eff3b` | Already-published `r3b-standard-melee` branch; [R3b validation](R3b-ordinary-melee.md) |
| Experimental recent Zilyana context | `b222f3c9d6c28f5e74b31e67208874610988b54c` | [PR #22](https://github.com/PetterHL/ironpath/pull/22), [KI1 validation](KI1.md) |

The user authorised bringing in the finished UX changes after KI1 implementation and merging the work together at the end. No unrelated repository work is merged, reset, deleted or republished. The R3b source is the already-published feature branch.

The combined release is **plugin 0.9.0 / knowledge r3b-1 / catalogue schema 11 / preference schema 2**. Ordinary `powered-magic-2`, `ranged-1`, `melee-1` and `progression-1` remain. Original reports retain their original versions, test totals, screenshots and pending live checks; the combined result is recorded here.

## Integration decisions

- Keep UX's compact header, account evidence page, counted discovery, owned-use views, facets, marked Value/Path tabs and ID/caret/scroll guards.
- Place KI1 enable/disable/clear inside the shared account page and expandable section. Account evidence opens from the recent context and returns to it. Recent Value/Path retain their captured baseline; normal Browse navigation does not silently leave the recent view active.
- Retain the independent magic/ranged/melee references, source-linked melee route and shared lifecycle guards. Recent context remains opt-in, session-only, post-completion ranged offence; a captured melee weapon is an explicit current KI1 model gap.
- Preserve UX Venator discovery fields when moving to melee schema 11. Include newly delivered melee items in style/slot discovery so the tentacle outcome is reachable with the Melee + weapon facets.
- Resolve only overlapping header/version/catalogue/account-view edits. No combat formula or original oracle input is changed during integration.

## Combined checks

Final Temurin 11.0.32.1 / Gradle 8.10 / RuneLite 1.13.1 `test build --no-daemon` passes **287 tests, zero failures/errors/skips**. This combines the 240-test baseline, eight UX tests, seventeen R3b tests, twenty KI1 tests and two integration regressions. Local document links, source JSON and catalogue JSON resolve/parse; `git diff --check` passes. Original **1,944 ranged** and **3,168 melee** complete-input oracle comparisons remain in that run. Headless Swing captures exercise each feature's native 225px surface and enlarged fonts; they do not establish actual-client acceptance.

All original pending client/source-event, numeric-coverage, sprites/scaling/focus/navigation, account observation and player-usefulness checks remain pending. No calibrated kill-time, full encounter/trip, full R3b family, most-boss coverage or Plugin Hub publication claim is made. KI1 stays off by default. See the three reports above for exact boundaries and the existing TASKS backlog for next increments.

Fresh combined-tree component captures: [KI1 context](../images/integration-ux-melee-ki1/recent-context.png) and [melee Value](../images/integration-ux-melee-ki1/melee-value.png). These use fallback glyphs and remain synthetic evidence.
