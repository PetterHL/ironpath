# Ironpath

**Your path to endgame PvM**

PvM progression roadmap · updated 12 October 2026

**Status: development implementation in open PRs; broader PvM expansion is the next assignment.** At this update main contains the documentation foundation. T1–T3 implementation and T4 trial preparation are on stacked branches; see TASKS.md for exact baselines and pending checks. No Plugin Hub approval/submission or external service exists.

The intended product is a free, open-source RuneLite sidebar that helps an Ironman choose a useful next PvM activity or equipment objective using observed account state. Evaluation runs locally, without AI, API keys, registration or an owner-operated backend.

## Decision

Proceed directly to **broader Ironman PvM progression across midgame and endgame entry**. CoX remains one supported activity. The next delivery combines the existing foundation with a small model generalisation and 12–16 reviewed decisions across multiple activities; the first wider catalogue targets approximately 35–45 decisions. General mode works without a selected goal; one optional goal refines advice. Coverage remains explicit, not every boss or item. The old CoX-only release and user-trial gate no longer block expansion; unperformed tests remain pending.

Personalised rules are technically feasible. The project will proceed independently, with usefulness and usability validated on representative accounts. Iron Compass remains a reference with overlapping features, not a reason to stop development or a required quality benchmark. Petter's experience is that its current workflow does not meet his usability needs; feature coverage alone does not establish a good user experience.

The product should make useful, encounter-specific trade-offs easy to understand: what improves the account's current options, what is merely convenient, when another grind is unnecessary, and which alternative remains sensible after the player skips a grind. A player should quickly find a few clear recommendations without navigating a large checklist.

## Read and work

| File | Purpose |
|---|---|
| [docs/RESEARCH.md](docs/RESEARCH.md) | Verified findings, source links, limitations and reuse assessment |
| [docs/DESIGN.md](docs/DESIGN.md) | MVP, account-state semantics, small architecture and update policy |
| [TASKS.md](TASKS.md) | Active P1–P5 roadmap, implementation baselines and next Codex assignment |
| [AGENTS.md](AGENTS.md) | Persistent instructions for AI-assisted development |

Use `TASKS.md` as the only task list initially. One developer, one repository, small commits. No Trello board, project board, separate knowledge service or additional planning documents. Once other contributors need issue discussions, move active tasks into GitHub Issues rather than maintaining two backlogs.

## Repository

Approved repository name and visibility: **`PetterHL/ironpath`**, public, confirmed by Petter on 12 October 2026. Created on GitHub on 12 October 2026. Use BSD-2-Clause for original work. The included licence does not relicense external guide content, images or dependencies.

Naming note: Iron Compass already uses `ironpath` as its Plugin Hub manifest identifier. This is an independent project; keep the chosen product/repository name, avoid any implied affiliation, and choose a distinct Hub manifest identifier before submission.

Reuse the existing T1–T3 RuneLite implementation and preserve its licence notices. Use its build/login instructions and verify supported APIs when extending it. Do not scaffold the plugin again or request approval again for the already approved repository name and public visibility.

## Start here

Run P1–P3 together as described at the end of `TASKS.md`. Bring the existing implementation and current main plan onto one working branch, then deliver real choices between PvM activities. No additional CoX-only polish or standalone architecture phase is required. Preserve useful build instructions and historical test results when combining branches.

The design introduces no per-user hosted inference, storage or API expenses. Development and content review still cost time; free distribution does not make knowledge maintenance automatic.
