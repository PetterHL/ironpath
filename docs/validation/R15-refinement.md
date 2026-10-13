# R1.5 documentation refinement validation

13 October 2026. **Documentation-only; no R1.5 application implementation or live-client validation.** Acceptance work remains in [TASKS](../../TASKS.md).

## Repository and review baseline

Inspected all nine open PR descriptions, head/base refs and local ancestry. Read the product reset and R1 documentation, R1 comparator and Swing rendering code, and R1 synthetic overview/Value captures. PR #8 and #9 inline-comment endpoints returned no comments. The user-mentioned screenshot was not present in the attachment, which contained only the written brief.

Branch `docs/effort-visual-foundation` starts at PR #9 head `309afb27ba7a6e0aee43d6c078b1234beff34c55` and targets `r1-full-loadout-magic`. The reset #8, P4a #7 and earlier #6/#5 ancestry are retained. Existing PRs and main are unchanged.

## Checks performed

- Documentation relative file/image paths resolve; new SVG parses as XML.
- Original four-panel PNG wireframe visually inspected: native logical panel dimensions, wrapping, hierarchy, conditional estimate scope, exclusions and sprite placeholders. It is a design artifact, not a Swing/client capture.
- Independent arithmetic check: p=1/300 yields mean 300 and P10/P50/P90 of 32/208/690 future eligible kills. At illustrative 40 kills/hour, mean is 7.5 hours and median 5.2; P90 is 17.25 before display rounding. This is a scenario, not observed throughput.
- Multiple-roll check: `1-(1023/1024)^2 = 0.0019521713256835938`, distinct from expected quantity `2/1024 = 0.001953125`. No live Zulrah estimate was implemented.
- Historical TASKS and RESEARCH sections compared with the R1 parent and preserved verbatim. Earlier UX is retained in [UX-20261012](../UX-20261012.md); earlier images and R1 validation artifacts remain unchanged.
- Application source, tests, runtime catalogue, build configuration and dependencies have no diff against R1. `git diff --check` passes.

Current source evidence, exact Wiki revisions, RuneLite pin, access limits and proposed-model boundaries are in [RESEARCH](../RESEARCH.md). A complete current raid-loot model, personal kill/raid pace, actual item-sprite appearance, client scaling/accessibility and player usefulness remain unvalidated. R1's historical build/oracle results were not rerun or claimed as new results for this documentation change.
