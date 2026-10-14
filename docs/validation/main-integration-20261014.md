# Main integration and repository cleanup — 14 October 2026

Petter authorised cleanup of all PRs/branches followed by a combined test. The inventory contained six open PRs and ten remote branches including main. Earlier #1–#13/#15 records retain their existing merged/closed states.

## Integration scope

| PR | Original head | Result |
|---|---|---|
| #14 — initial R2 family models | `f821c625` | Superseded by the package engine in #16; closed, with the complete separate history preserved as tag `r2-ranged-models` |
| #16 — R2 ranged packages | `32cb24a8` | Merged to main with original history |
| #17 — progression design | `75897d56` | Retargeted to main and merged after #16 |
| #18 — relationship advice/native cards | `d9e897b7` | Retargeted to main and merged after #17 |
| #20 — Browse/search/navigation and review fixes | `a8ee55a4` | Retargeted to main and merged after #18 |
| #19 — Kill Impact design | `a05b4be6` | Research scope preserved; combined with #20, current instructions reconciled and merged to main |

#19/#20 overlap only in documentation. The README conflict retains both the Kill Impact design and current 0.8.3 UX behaviour. Current README, AGENTS, DESIGN and TASKS now identify main as the development baseline, with R3a implemented and R3b/KI1 still future work. The historical TASKS section and existing validation reports remain unchanged. Kill Impact remains **unimplemented design**, and no live-client acceptance or release is inferred from integration.

The old #14 branch is **not an ancestor of #16**. Its independently implemented model, sources, tests and 1,152-case corpus must not be discarded or merged alongside the replacement engine. The archive tag preserves its exact commit and complete tree. The current engine retains its original 1,944-case corpus.

## Branch preservation

Every pre-cleanup non-main branch has an annotated same-name snapshot tag. This preserves historical GitHub file links after branch removal, as well as the complete commit histories. Delete a branch only after its latest head is either an ancestor of main or preserved by its verified archive tag; reject a deletion if the remote head has changed.

| Removed branch / preserved tag | Original commit |
|---|---|
| `client-model-gap-labels` | `fb7d5dce23825aaff4c20584599faa6119874c7b` |
| `docs/endgame-progression-intelligence` | `75897d56d99e90a79b05931bbecddbc3c410212f` |
| `docs/personalized-kill-impact` | `a05b4be6e6d78568fd602feb2721b235f5679ec4` |
| `r1.5a-pursuit-visual-foundation` | `ea53583191c3ca6621812924780d1f21f0ef4aa7` |
| `r2-ranged-models` | `f821c625650605ee34f933bb2650b8d8e3e8bc66` |
| `r2-ranged-packages` | `32cb24a8db7ae807166d1f2d9844224c3be71fd6` |
| `r3a-personalized-cards` | `d9e897b7114591287a8667616969b7bd9d09a0b8` |
| `reference-neutral-dps` | `3192b7c7830f71de997d7e0548d4cc83797b1062` |
| `ux-card-navigation` | `a8ee55a4eee4b2bd607b7c57a3fac7b74141e4d3` |

Use `git switch main` and `git pull --ff-only` for current development. Old names resolve to historical tags, not active development branches.

## Combined validation

Tested main: **`2890b61f59dab0d2f18bae7615aebcc7c8ee8d71`**, after all five integration merges and branch cleanup. A fresh `./gradlew clean test build --no-daemon` on Debian 13.6 / Temurin 11.0.32.1+1 / Gradle 8.10 / RuneLite 1.13.1 passes **240 tests, zero failures/errors/skips**. All seven Gradle tasks execute from the clean tree, including the unchanged 1,944 original ranged oracle cases and both UX review regressions. Existing unchecked-template, Fontconfig-cache and Gradle-deprecation notices remain non-fatal.

The combined application/resources/build/test tree is identical to UX head `a8ee55a4`; the reconciliation adds only research and documentation. This completion record changes documentation only after the fresh test. All 250 relative document/image links resolve, research/catalogue JSON parses, `git diff --check` passes and the historical TASKS section matches the pre-cleanup UX snapshot byte-for-byte.

Final inventory: **zero open PRs**, **main only** in remote and local branch lists, and **nine verified annotated snapshot tags**. PRs #16–#20 are merged; #14 is closed as superseded. All eight integrated/previously merged branch heads are ancestors of main; #14's independent head is preserved by its exact archive tag. Remote deletion used explicit expected heads and an atomic push; no uninspected/new branch was deleted. Temporary local branches were removed only after main contained their commits.

For the combined PC test, close the development client and run `git fetch origin`, `git switch main`, `git pull --ff-only`, then `gradlew.bat test build` and `gradlew.bat run` on Windows (or `./gradlew` on macOS/Linux). Open the bank after login and verify native cards, item recognition, Value/Path/Back, search/filter/reset, selection retention during equipment/bank updates and account/profile clearing. Follow [the UX acceptance steps](ux-navigation.md#pc-acceptance--pending) and [the Lightbearer/client follow-up](R3a-lightbearer.md#pc-retest) for concrete examples. Old branch tags reproduce their historical builds and should not be used for this combined test.

Actual RuneLite sprites/focus, OS/client scaling, authenticated account/profile transitions and player usefulness remain pending in their original acceptance records. No client launch, personal-time implementation, Plugin Hub submission or publication is performed by this cleanup.
