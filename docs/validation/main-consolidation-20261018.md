# Main consolidation — 18 October 2026

## Authorization and scope

Petter requested cleanup of current branches, a clean common main and an updated
Codex task/roadmap after the Kill Impact UI review. This authorizes integrating
existing development work and planning updates. It does not accept unperformed
live checks or authorize new UI/context implementation or public distribution.

Runtime source, catalogue, build configuration, workflow and test fixtures in
this integration are identical to reviewed banking-stack head
`762f46f21cd2a7f20ac9558724522d0e4d5a4c95` (development build 0.19.3).
Changes on top are documentation only; no new numerical or lifecycle logic.

## Preserved work

- PR #36: N3b conditional damage/ammo, head `024c44bf2471`.
- PR #37: N3c complete-package bridge, head `8c3b6a72baa9`.
- PR #38: bank retention and arena reload fixes, head `762f46f21cd2`.
  Its previous draft/client-test hold is superseded for development integration
  by the cleanup instruction. Client acceptance remains open in TASKS and its
  original validation report.
- PR #40: DEV1 already merged in main `43f4e76f5445`; the stack includes it.
- PR #39: original DEV1 planning commit `0847fe893c8f` is preserved as ancestry.
  Its plan was already delivered by #40; obsolete unchecked tasks are not restored.
- UI discussion brief `64cfdd4032a5` is preserved as ancestry and as its original
  file with an explicit review/consolidation status note.

All pre-existing non-main branch heads are reachable from the integration
commit. No unique commit is discarded. Old tags and historical validation reports
retain their scope. Replacing TASKS does not remove history: the previous full
backlog remains linked at the pinned pre-cleanup revision.

| Branch at inventory | Head | Disposition |
|---|---|---|
| ad1a-dps-first | `2fefa28dbb5d` | Preserved in integration ancestry |
| ad1b-automatic-acquisition | `3a90342ea420` | Preserved in integration ancestry |
| ad1c-priority-ux | `a11383ab0324` | Preserved in integration ancestry |
| dev1/offline-nex-review | `1dc7ee0a7538` | Preserved in integration ancestry |
| docs/account-efficient-progression-20261014 | `f95ffb32af86` | Preserved in integration ancestry |
| docs/ad1-useful-upgrade-advice | `b2eb0644ba13` | Preserved in integration ancestry |
| docs/offline-scenario-workflow | `0847fe893c8f` | Preserved in integration ancestry |
| ep1-account-efficient-progression | `460d78638479` | Preserved in integration ancestry |
| fix/nex-banking-session | `762f46f21cd2` | Preserved in integration ancestry |
| integration-ux-melee-ki1 | `0eaad5a8e7b2` | Preserved in integration ancestry |
| ki1-recent-boss | `b222f3c9d6c2` | Preserved in integration ancestry |
| n1/nex-session | `2e313370c51c` | Preserved in integration ancestry |
| n2/nex-owned-advice | `f30453ac1928` | Preserved in integration ancestry |
| n3/nex-spec-resources | `719cfcde7e4d` | Preserved in integration ancestry |
| n3b/nex-spec-damage | `024c44bf2471` | Preserved in integration ancestry |
| n3c/nex-package-bridge | `8c3b6a72baa9` | Preserved in integration ancestry |
| r3b-melee-families | `197ee084f4f8` | Preserved in integration ancestry |
| r3b-standard-melee | `b2ae8c562077` | Preserved in integration ancestry |
| research/kill-impact-ui-context-20261018 | `64cfdd4032a5` | Preserved in integration ancestry |
| transition/nex-pilot | `d2dc54c4af8e` | Preserved in integration ancestry |
| ux-completion | `ebcd5a28d019` | Preserved in integration ancestry |
| ux2-visual-advisor | `db7614a03fc3` | Preserved in integration ancestry |

Remote branch deletion is metadata cleanup after integration/ancestry verification.
Do not claim deleted branches without verifying the remote operation. Future work
starts from current main regardless of retained redundant branch names.

## Active documentation

TASKS.md is rewritten as the sole ordered backlog: UI1 next, CTX1 after its review,
N1/N2 live acceptance alongside, then remaining N3 integration and N4 validation.
Completed foundations are summarized and linked, not presented as new assignments.
AGENTS/README/UX/DESIGN/KILL_IMPACT/DEVELOPMENT and the validation index agree.

UI1 is native Encounter/Diagnostics with correctly identified per-state package
cards and a bounded allowlisted user-triggered report. No whole-kill ranking from
unrelated DPS deltas. CTX1 separates defaults/evidence/planning/annotations and
adds revisioned recalculation and conservative grouping without changing captures.
UI1 does not implement CTX1, a recorder, screenshot saving or new time claims.

## Verification evidence and limits

The exact code baseline's
[CI run 39067283091](https://github.com/PetterHL/ironpath/actions/runs/39067283091)
is completed/successful. Its pinned full test/build/offline-review workflow covers
nine scenarios. The existing validation record reports **288 tests**, **293
scenario checks** and **56 PNGs**; these are existing run results, not a newly
claimed local execution.

This workspace has JDK 17 and no prepared JDK 11/Gradle dependency cache. The
integration PR's pinned JDK 11 CI is the full-build gate. Local checks cover
unchanged runtime/test/build trees, branch ancestry, whitespace and changed-doc
relative links. Record the exact integration CI run and outcome in its PR.

Synthetic success does not establish live event mappings, stock/charges, timer
calibration, sprites/DPI/focus/clipboard, real bank/reload behavior or player
usefulness. TASKS retains those checks. No Plugin Hub publication.

