# Main integration and repository cleanup — 14 October 2026

Petter authorised cleanup of all PRs/branches followed by a combined test. The inventory contained six open PRs and ten remote branches including main. Earlier #1–#13/#15 records retain their existing merged/closed states.

## Integration scope

| PR | Original head | Result |
|---|---|---|
| #14 — initial R2 family models | `f821c625` | Superseded by the package engine in #16; closed, with the complete separate history preserved as tag `r2-ranged-models` |
| #16 — R2 ranged packages | `32cb24a8` | Merge to main with original history |
| #17 — progression design | `75897d56` | Retarget to main and merge after #16 |
| #18 — relationship advice/native cards | `d9e897b7` | Retarget to main and merge after #17 |
| #20 — Browse/search/navigation and review fixes | `a8ee55a4` | Retarget to main and merge after #18 |
| #19 — Kill Impact design | `a05b4be6` | Preserve research scope, combine with #20, reconcile current instructions and merge to main |

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

The combined application/resources/build/test tree is identical to UX head `a8ee55a4`; the reconciliation adds only research and documentation. The earlier 240-test UX result remains historical evidence until a fresh integrated-main test completes. Combined build, final PR/branch inventory and archive verification are pending at this integration-preparation commit.

Actual RuneLite sprites/focus, OS/client scaling, authenticated account/profile transitions and player usefulness remain pending in their original acceptance records. No client launch, personal-time implementation, Plugin Hub submission or publication is performed by this cleanup.
