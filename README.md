# Ironpath

**Your path to endgame PvM**

Research and project foundation · 12 October 2026

**Status: proposal and implementation brief, not an implemented or approved RuneLite plugin.** The public repository is established. No Plugin Hub submission or external service has been created.

The intended product is a free, open-source RuneLite sidebar that helps an Ironman choose a useful next PvM activity or equipment objective using observed account state. Evaluation runs locally, without AI, API keys, registration or an owner-operated backend.

## Decision

Proceed with a small proof of value, not a general OSRS progression platform. Start with **CoX-focused progression for midgame and established raiding accounts**, covering at most 12 curated decisions. General mode works without a selected goal, within this explicitly labelled catalogue. Optional goals refine those recommendations.

Personalised rules are technically feasible. The project will proceed independently, with usefulness and usability validated on representative accounts. Iron Compass remains a reference with overlapping features, not a reason to stop development or a required quality benchmark. Petter's experience is that its current workflow does not meet his usability needs; feature coverage alone does not establish a good user experience.

The product should make useful, encounter-specific trade-offs easy to understand: what improves the account's current options, what is merely convenient, when another grind is unnecessary, and which alternative remains sensible after the player skips a grind. A player should quickly find a few clear recommendations without navigating a large checklist.

## Read and work

| File | Purpose |
|---|---|
| [docs/RESEARCH.md](docs/RESEARCH.md) | Verified findings, source links, limitations and reuse assessment |
| [docs/DESIGN.md](docs/DESIGN.md) | MVP, account-state semantics, small architecture and update policy |
| [TASKS.md](TASKS.md) | Five ordered tasks, acceptance gates and the first Codex assignment |
| [AGENTS.md](AGENTS.md) | Persistent instructions for AI-assisted development |

Use `TASKS.md` as the only task list initially. One developer, one repository, small commits. No Trello board, project board, separate knowledge service or additional planning documents. Once other contributors need issue discussions, move active tasks into GitHub Issues rather than maintaining two backlogs.

## Repository

Approved repository name and visibility: **`PetterHL/ironpath`**, public, confirmed by Petter on 12 October 2026. Created on GitHub on 12 October 2026. Use BSD-2-Clause for original work. The included licence does not relicense external guide content, images or dependencies.

Naming note: Iron Compass already uses `ironpath` as its Plugin Hub manifest identifier. This is an independent project; keep the chosen product/repository name, avoid any implied affiliation, and choose a distinct Hub manifest identifier before submission.

Add the current official RuneLite example-plugin scaffold during T1, preserving its licence notices. Recheck the supported toolchain then; the inspected template targets Java 11. Do not request approval again for the already approved repository name and public visibility.

## Start here

Run the assignment at the end of `TASKS.md`. The first build is a narrow account-data and recommendation experiment. It must produce an inspectable result before extending the catalogue or polishing the UI.

The design introduces no per-user hosted inference, storage or API expenses. Development and content review still cost time; free distribution does not make knowledge maintenance automatic.
