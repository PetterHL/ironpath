# Main integration and branch cleanup

13 October 2026 · user-authorised repository integration. No new plugin features or runtime changes.

## Preservation and integration

Before mutation, fetched current refs and verified that the current heads of #1–#4 (`f224f41`, `e52728d`, `b3a93d9`, `fad507f`) and old main `c717927` were ancestors of the tested #10 head `81291b03a1cc70131d31c094adf92be1c2269f6b`. There were no commits unique to those older branches; this includes later fixes, not just their original submissions.

Merged PRs #5–#10 in order using normal merge commits. Retargeted each remaining dependent PR to main after its predecessor was integrated. Each merge checked the exact expected head SHA. No squash, rebase, force update of main or history deletion. GitHub reported clean mergeability and no submitted reviews on these six PRs before integration.

Main after #10: `fb25fc6fbd3f7a1c9298b10bebbd13511435b30c`. Its complete tracked tree is identical to the tested #10 head. GitHub automatically marked #1 merged; #2–#4 were explicitly closed as superseded with preservation/validation explanations. The ten old PR branches were removed in one atomic push after verifying that each exact head remained an ancestor of main; expected-head leases protected against concurrent edits. PR #9/#10 artifact links were changed to immutable commit URLs so removed branch content remains accessible.

## Fresh validation

- Windows, Eclipse Adoptium JDK 11.0.32.101, Gradle 8.10, resolved RuneLite client 1.13.1 (confirmed by dependency report).
- `.\gradlew.bat test build --no-daemon`: **BUILD SUCCESSFUL**, 124 tests, zero failures/errors/skips. All six build tasks executed; this was not only an up-to-date report.
- Test XML totals independently checked. Existing source/test/catalogue code remained unchanged during integration.
- Non-fatal existing unchecked-operation and Gradle deprecation warnings remain.
- The external calculator oracle was not rerun. Existing original fixtures ran through the normal suite; earlier external validation retains its historical status.
- No authenticated client was launched. Native sprites, real-client scaling, bank/account transitions and player-usefulness checks remain pending as previously documented.

## Workflow after cleanup

Main is the development baseline, not a claim of Plugin Hub release or comprehensive feature completion. R1.5a remains the next assignment in TASKS. Begin each deliverable on a new branch from updated main, open a focused PR to main, validate, merge when authorised and remove the merged branch. Use a short PR stack only for a genuine dependency; do not leave the project's working baseline indefinitely outside main.

README checkout instructions and active AGENTS/TASKS/DESIGN status now use main. Historical snapshots and test reports remain records of their original dates. This follow-up changes documentation only.
