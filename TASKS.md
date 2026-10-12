# Ironpath tasks

Updated 12 October 2026. This is the single active backlog.

## Current decision and status

Petter approved moving directly from the CoX experiment to broader Ironman PvM progression on 12 October 2026. CoX remains a supported activity and regression case. A separate CoX-only release and completion of the old T4 user trial are **not prerequisites for expansion**. Do not interpret this decision as proof that unperformed tests passed.

At this planning update, main contains the documentation foundation; implementation is in the open stacked PRs below. Do not rebuild T1–T3 from scratch.

| Historical work | Verified repository state | Remaining qualification |
|---|---|---|
| T1 account observations / sidebar | [PR #1](https://github.com/PetterHL/ironpath/pull/1), `t1-account-advisor` | Limited user-performed checks recorded; other live checks pending |
| T2 reviewed CoX catalogue | [PR #2](https://github.com/PetterHL/ironpath/pull/2), `t2-cox-catalogue` | Nine decisions; coverage is deliberately narrow |
| T3 goals, ranking, local preferences | [PR #3](https://github.com/PetterHL/ironpath/pull/3), `t3-local-advisor` at `b3a93d9aa60f7661e49b970fc3b9272e7ad2df12` | Build / 58 tests reported passing; T3 real-client checks pending |
| T4 trial preparation | [PR #4](https://github.com/PetterHL/ironpath/pull/4), `t4-user-maintenance-trial` at `fad507f0b87f2e4f879c8810daad663f7dd3043f` | Documentation only; simulated maintenance rehearsal complete; user trial pending |
| T5 CoX distribution | Not started | Superseded by P5 below |

Historical implementation/test reports are in [TASKS.md at the T4 baseline](https://github.com/PetterHL/ironpath/blob/fad507f0b87f2e4f879c8810daad663f7dd3043f/TASKS.md). Preserve those results when combining branches. This plan replaces the old CoX-only scope and T1-only assignment; historical reports remain evidence, not active instructions.

## Delivery order

P1, P2 and P3 form the **next implementation assignment**. Deliver them together as a coherent PR, using small commits as useful. Do not introduce a new approval/review gate between these three tasks. P4 and P5 follow separately. No plugin implementation is part of this planning update.

## P1 — Bring the existing foundation and current plan together (next)

- Inspect current branch heads and open PRs before work. Start from the existing T3 implementation including `b3a93d9`; the recorded T4 branch already contains it.
- Bring that implementation, T4 historical results and the current main planning documents together on a working branch. Resolve documentation conflicts in favour of this active P1–P5 plan while preserving accurate historical reports and useful build/login instructions.
- Do not reset existing branches, discard user changes or automatically merge/close the stacked PRs. Open the coherent implementation PR against main and explain which earlier work it includes.
- Run the existing build and meaningful baseline tests once. Fix actual blockers as encountered; do not restart a full CoX review or repeat completed maintenance simulations.

**Done:** one working baseline includes existing implementation and current scope; earlier test evidence remains available; pending real-client checks remain pending.

## P2 — Generalise only what the broad slice needs

Implement this alongside P3's real decisions, not as a standalone framework project.

- Distinguish a goal, an activity and its outcome. Allow multiple reviewed routes toward an outcome.
- Replace one-off CoX decision kinds / hardcoded goals with a small set of reusable action types and catalogue-defined goals, contextual benefits and effort bands. Remove the 12-decision and unique-kind constraints; retain bounded validated input.
- Separate hard access, recommended preparation, completion and relevance. Support explicit ALL/ANY alternatives in both eligibility and next-step explanations. Avoid reward/access dependency cycles.
- Rank by selected-goal relevance, contextual improvement over supported owned alternatives, breadth of usefulness, then qualitative effort and stable tie-breaking. No numerical optimality or invented efficiency claims. Keep important longer goals visible beside quick actions.
- Add stable activity IDs for activity-level skip/restore, while retaining a meaningful decision-level distinction if needed. Skipping CG must exclude its acquisition route without marking it complete or hiding useful already-owned crystal equipment.
- Add only the quest/unlock facts needed by P3 through supported RuneLite APIs, read on the client thread. Support required skills such as Slayer/Herblore in the bounded predicates.
- Preserve TRUE/FALSE/UNKNOWN, observed versus manual provenance, charged/usable versus owned distinctions, memory-only live facts and character/profile isolation.
- Unknown global ownership must not become an unconditional missing-item grind. Request a targeted confirmation or show clearly separate conditional planning information. Do not require an account-wide questionnaire.
- Preserve stable IDs where possible; explicitly migrate or retire goals, skips and manual confirmation keys when schema changes require it.
- Keep one Java/Gradle plugin, a pure evaluator, bundled JSON and the existing sidebar. No backend, AI, database, scraping, Collection Log/KC dependency, generic rule language, DPS optimiser or framework rewrite.

**Done:** P3 routes share the same small evaluation flow; existing supported advice remains correct or is deliberately migrated; adding a comparable reviewed decision does not require another boss-specific Java switch.

## P3 — First useful broad PvM slice

Target **12–16 meaningful reviewed decisions total**, including useful migrated existing decisions. The count is a budget, not a reason to pad the catalogue.

- Cover CG/Bowfa/crystal, Zulrah, ToA, Slayer progression, Moons of Peril / Royal Titans, and existing ordinary team CoX. These must appear as real alternative activities, not only mentions in explanation text.
- Include actual acquisition and prerequisite choices. Merely reading owned scrolls, swapping owned gear or assembling already-owned parts does not satisfy this task.
- At least four reviewed acquisition/unlock routes must span at least three non-CoX activities. At least two relevant route choices must remain useful after Bowfa.
- Include a no-goal mode and a small catalogue-defined set of goals. Demonstrate one shared upgrade helping more than one goal and a credible alternative after CG is declined.
- Review current access, item variants, alternatives and benefit assumptions before shipping each decision. Write original explanations with source/revision/access/review/reuse metadata. Unverified candidates remain inactive; never fabricate evidence to reach the count.
- Recognise relevant owned advanced alternatives to suppress redundant grinds without promising complete acquisition coverage for those items.
- Keep recommendations focused: up to three materially different activities, evidence, why now, trade-off, alternative, broad effort and sources. Explain coverage/unknowns briefly; no full loadout planner or combat coaching.

**Meaningful acceptance checks:**

1. An unopened bank does not create an invented missing-item grind.
2. The same account changes relevant priorities when switching between supported goals.
3. Declining CG yields a reviewed alternative when one is supported, or an accurate blocked/unknown explanation; CG does not reappear through another card.
4. Post-Bowfa and established raiding accounts receive relevant non-CoX route choices when their facts support them.
5. Observed/manual advanced ownership suppresses redundant acquisition advice without claiming equipment is charged or immediately usable.
6. Missing access requirements are separate from optional preparation; ANY routes and completed prerequisites behave correctly.
7. Preference migration/restart and account/profile isolation regressions remain covered.
8. Existing relevant tests and build pass. Report exact limitations and any real-client checks not performed.

**Done:** present three concrete synthetic account examples with observed facts, selected goal, suggested activity, alternative and reasoning, including a declined-CG and a post-Bowfa case. Label synthetic evidence accurately. Record a short focused PC checklist; user testing is not a prerequisite for delivering this PR. Do not start P4 or Plugin Hub work in this assignment.

## P4 — Expand the first catalogue to approximately 35–45 decisions

After P3 is implemented, expand breadth using the same model. Prioritise missing useful choices over filling quotas; do not require another abstract architecture study.

| Area | Candidate decisions | Approximate budget |
|---|---|---:|
| Shared PvM foundations | Relevant quest/skill unlocks, Piety, fire cape, key glove/defender gaps when they constrain a supported goal | 5 |
| Midgame equipment | Zombie axe, targeted Barrows, Moons, proceed with an adequate owned kit | 5 |
| Ranged | CG/Bowfa/crystal packages, supported alternatives, Zulrah/blowpipe, Vorkath/Ava progression | 5 |
| Magic and prayers | Powered staffs, trident/swamp, meaningful magic slots, Royal Titans prayers, Rigour/Augury | 6 |
| Slayer and specialist weapons | Relevant Slayer milestones, demonbane, dragonbane and defence reduction | 5 |
| Jewellery and armour | Zenytes/Crafting, selected GWD upgrades, defer marginal gains | 5 |
| Raids | ToA, ordinary team CoX, ToB entry/preparation and selected later improvements | 6 |
| Advanced objectives | A few goal-specific DT2, Inferno and Colosseum preparation choices | 3 |

These are research candidates, not a validated progression order or a promise of every route. Reuse existing decisions; do not count every item tier as a separate user choice. Yama, Doom and Nex acquisition routes are later candidates unless evidence justifies replacing a lower-value candidate within the budget. Relevant owned rewards may be recognised earlier.

**Done:** source-reviewed breadth serves midgame, post-Bowfa and established raiding accounts; unsupported goals and item combinations are explicit; affected account fixtures and catalogue validation pass. No full quest/skill planner, UIM/HCIM/GIM routing or every-boss coverage.

## P5 — Broad usefulness trial and distribution readiness

Replace the old CoX-only release gate with a short broad trial. Reuse existing T4 safety checks and maintenance evidence; do not repeat a simulation merely to complete a checklist.

- Check real-client observation accuracy, save/restart and character/profile isolation, plus goal change and activity skip/restore.
- Try five contrasting scenarios, including no-goal, declined-CG, post-Bowfa and established raider contexts. Aim for four of five actionable or correctly abstaining results and three distinct recommendations judged useful. Record unexercised checks as pending.
- Zero critical ownership/access/isolation errors before release. A concrete discovered defect is a reason to fix it; pending user availability is not a reason to stop P1–P4 development.
- Review current sources and maintenance effort proportional to the wider catalogue.
- Prepare licence/notices, privacy/coverage/install documentation, metadata/icon and supported build. Choose a distinct Plugin Hub manifest identifier.
- Plugin Hub submission requires explicit publication authorisation. This roadmap change does not grant it.

## Next Codex assignment

Read AGENTS.md, README.md, docs/DESIGN.md and this active P1–P5 plan from current main. Implement **P1–P3 together**, reusing T3 including `b3a93d9` and the T4 historical records. Do not rebuild the foundation or wait for the old CoX trial. Deliver a coherent PR against main with the first broad 12–16-decision slice, meaningful tests, accurate task results and a short manual checklist. Stop before P4/distribution. Report actual blockers; do not claim unperformed game tests passed.
