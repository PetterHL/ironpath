# IronPath product and architecture

Product reset · 12 October 2026 · authoritative direction, superseding earlier slice budgets.

## A. Stable product vision

**IronPath — Your path to endgame PvM** is a free, open-source, local RuneLite plugin for established Ironman accounts. Opening it should answer: **What upgrades are worthwhile for my account, why, and how do I obtain them?** No goal, setup wizard, account registration or paid API is required.

The destination is comprehensive, maintained coverage of meaningful main-game endgame/BiS equipment, specialist sidegrades, prayers and permanent PvM rewards, including the bridges needed to reach them. A small implementation increment must never redefine that destination. There is no catalogue ceiling. A three-card shortlist is a presentation choice, not a coverage limit. Highly developed accounts are first-class users.

Success requires several relevant choices, understandable personal value, supported numerical comparisons and navigable acquisition paths. Passing unit tests, increasing item counts or showing technically correct but irrelevant Dharok examples does not establish success. Optional interests refine advice; they never supply its primary value.

### Independent assessment

**Proceed, retaining the foundation and changing its scale assumptions.** A deterministic local advisor is feasible. The main cost is research and maintaining correct interactions, not infrastructure. A universal best-next-grind answer is not identifiable from gear alone: enjoyment, team access, execution and preferred encounters matter. Offer transparent alternatives and a balanced shortlist instead of pretending to know a universal optimum.

| Trade-off / risk | Decision and practical mitigation |
|---|---|
| Comprehensive coverage versus unsupported advice | Inventory all relevant families now; activate only validated contexts. Measure gaps explicitly. Adjust delivery pace and obtain contributions before quietly shrinking product scope. |
| Automatic advice versus partial account observations | Use observed equipment/levels/unlocks immediately; after bank opening improve coverage. Provisional advice names its one consequential unknown. Zero observations means an honest useful empty state, not fabricated personalisation. |
| Accurate numbers versus realistic gear | Prioritise full reference loadouts and reusable combat families. A missing engine feature becomes a tracked model gap, not permanent exemption from quantitative work. |
| BiS versus worthwhile acquisition | BiS is contextual. Small accessory breakpoints may be poor grinds; scarce resources, alternatives and upkeep matter. Long-term megarares remain visible. |
| Sustained damage versus encounter success | Calculate supported attacks locally. Do not translate DPS into kills/trip. Analytical time/resource estimates need their own validated assumptions and policy assessment. |
| Maintenance versus release freshness | Version bundled facts/models; dependency-based change review and source discovery. Known-invalid claims are disabled, unaffected coverage remains. Offline users cannot receive instant corrections. |
| Attractive UI versus sidebar space | Native Swing, compact single-column cards, progressive disclosure and real item sprites. A larger optional surface must pass current policy review; it is not a core dependency. |

No rewrite, hosted service, database, user-facing AI, subscriptions, gameplay automation or mandatory other plugin. Remain one Java/Gradle project. Standard Ironman main-game planning is the baseline. UIM storage optimisation, GIM shared-bank inference, HCIM safety certification and seasonal mechanics require separate explicit support; they must not leak into ordinary recommendations.

## Verified implementation and PR stack

Inspected source and GitHub metadata on 12 October 2026:

| PR | Head / SHA | Target | State |
|---|---|---|---|
| #1 | `t1-account-advisor` / `f224f41` | `main` | Open; currently conflicting |
| #2 | `t2-cox-catalogue` / `e52728d` | `t1-account-advisor` | Open draft; currently conflicting |
| #3 | `t3-local-advisor` / `b3a93d9` | `t2-cox-catalogue` | Open |
| #4 | `t4-user-maintenance-trial` / `fad507f` | `t3-local-advisor` | Open draft |
| #5 | `p1-p3-broad-pvm` / `eb22fef` | `main` | Open; incorporates earlier work and records |
| #6 | `docs/upgrade-first-roadmap` / `2ef409a` | `p1-p3-broad-pvm` | Open |
| #7 | `p4a-upgrade-advisor` / `b50b0a9` | `docs/upgrade-first-roadmap` | Open |

`main` is `c717927`. No PR above is merged. This documentation branch starts at #7's exact head and targets `p4a-upgrade-advisor`. It does not resolve old integration conflicts. When integration is authorised, preserve #5's consolidation, apply #6 → #7 → reset in order, and explicitly reconcile/retarget the remaining stack. Do not merge earlier overlapping PRs blindly. Recheck remote heads before changing any integration plan.

P4a delivers four upgrade outcomes, nineteen decisions, 81 recognised item forms, 84 sources and thirteen combat-item records (plugin 0.4.0, knowledge `p4a-1`, schema 5). Historical evidence: 106 passing tests and 234 synthetic oracle cases. This session does not rerun or extend those results. P4a in-game observation, appearance and usefulness checks remain pending.

## B. Required capabilities and coverage

[CATALOGUE.md](CATALOGUE.md) is the coverage inventory; [UX.md](UX.md) specifies the interface. [RESEARCH.md](RESEARCH.md) records source limits. [TASKS.md](../TASKS.md) is the only backlog.

1. **Observe:** identity-isolated levels, equipment slots, quantities, bank observation provenance, selected quests, prayers and permanent unlocks; explicit unknowns for unavailable state.
2. **Understand alternatives:** exact item forms grouped into ownership families, but charge state, stats and usability remain distinct. Recognise strong alternatives before activating acquisition advice.
3. **Recommend:** contextual marginal value, optional interests, effort, confidence and diminishing returns; several useful choices with no goal.
4. **Compare:** consistent before/after setups, max hit, accuracy and theoretical DPS across melee/ranged/magic; special mechanics and target conditions added through reusable modules.
5. **Guide acquisition:** hard requirements, preparation, components, alternate routes, completed steps and nearest useful remaining step. Relevant inaccessible upgrades stay visible.
6. **Present:** native item cards, meaningful headlines, before/after details, compact route views, search/filter, readable uncertainty and secondary preferences.
7. **Maintain:** open-ended coverage inventory, source/model versions, change impact, withdrawn/stale states, independent reference checks and player usefulness evidence.

Coverage is by **item family × encounter/context × baseline × model support**, not raw item count. Cosmetic/charged variants are recognition coverage, not extra upgrades. Publish separate coverage measures for researched candidates, validated recommendation contexts, acquisition paths and supported comparisons. A supported max hit does not imply supported DPS.

## Account semantics and acquisition

Retain TRUE/FALSE/UNKNOWN and provenance. `ALL`: false dominates, otherwise unknown propagates. `ANY`: true dominates, otherwise unknown propagates. An unread bank, an empty bank, a filtered view or absence from observed containers does not establish global absence. Collection history is not ownership. Positive live possession overrides manual absence; readable live unlocks override manual fallbacks. A manual ownership fact cannot establish charges, quantity, equip state or a usable loadout.

Add typed slot and quantity observations without losing the existing item-set compatibility views. Observation time and scope must travel with values. Stored-bank quantities may be stale after leaving the bank; they are planning evidence, not a real-time supply guarantee. Charges absent from supported APIs remain unknown. Read on the client thread; evaluate immutable snapshots; update Swing on the EDT. Raw observations and derived reference setups remain memory-only. Persist only explicit preferences/manual confirmations through existing local Filepath storage, with migration and character/profile guards.

A route is a small typed dependency graph of activities, access requirements, component acquisition and assembly. Use bounded ALL/ANY and DAG validation, not an executable rule language. Equip conditions do not automatically block obtaining an item. Recommended gear is never a hard gate, and obtaining a boss reward cannot require owning that reward. A skipped activity excludes that route, not already-owned utility or all alternate routes.

Example **Zaryte crossbow** (existing pinned evidence retained): The Frozen Door/Nex access and team preparation are distinct from 80 Ranged to wield. Track Armadyl crossbow (Commander Zilyana route), nihil horn (Nex), 250 nihil shards and assembly. Show already-observed horn as complete planning evidence. Do not infer 250 shards from an item-ID set. Choose the next unsatisfied hard dependency on a viable route; if the limiting fact is unknown, show a targeted check. Compare sustained ranged weapons separately from the ruby/spec role and the competing use of special energy. Neither owning a twisted bow nor merely lacking Zaryte decides its value.

Show component progress such as an observed quantity against its requirement only when supported. Do not display a percentage of an RNG drop completed. Qualitative effort covers remaining skill/quest work, repeat RNG, team coordination, supplies and upkeep. Later probability/effort scenarios may use published rates with mode/scale and assumptions, never guaranteed hours or dry-streak entitlement. Shared activities list distinct potential rewards without summing gains or assuming simultaneous drops.

## Recommendation design

Keep deterministic, explainable rules. Current `Advisor.upgrades()` sorts authored benefit band → goal → application count → route availability → ID. It does **not** consume `PoweredMagic` deltas, does not compare competing complete loadouts, and authored application text can inflate breadth. `upgrades()` requires `when == TRUE`, which can hide a distant valuable upgrade if readiness is encoded in applicability. Those are concrete limits, not reasons to replace the entire evaluator.

Recommended flow:

1. Select released, validated upgrade contexts. Remove owned equivalents and invalid mechanics. Keep an explicit deferred explanation for dominated/marginal cases in Browse.
2. Establish baseline alternatives by role and encounter, including specialist specs, off-hands, ammunition and set packages. A two-handed candidate removes the old off-hand. Never manufacture a weaker baseline to make an upgrade look good.
3. Compute supported scenario deltas; use reviewed qualitative evidence for genuine utility not captured by damage (healing, range, supply savings). Missing models do not imply zero value, and supported numbers do not automatically outrank stronger qualitative upgrades.
4. Assign transparent value bands: capability-changing, material improvement, specialist improvement, marginal/no demonstrated gain. Conditional rules and measured breakpoints justify bands; record the matched explanation. Do not impose one DPS threshold across all roles.
5. Within comparable bands, refine by explicitly selected encounter interest, deduplicated encounter IDs, remaining work/upkeep and confidence. Avoid claiming interests or team access from gear. Without preferences use a balanced set of reviewed common PvM contexts and label them.
6. Form a diverse shortlist: worthwhile nearer work plus relevant long-term opportunities when available. Avoid three minor upgrades for the same slot/activity crowding out a megarare. Browse exposes every supported relevant candidate, including other rewards from shared content. Stable IDs break genuine ties.

Do not aggregate unrelated DPS percentages into an opaque total or divide an invented benefit by guessed grind hours. Initially compare Pareto trade-offs (better value but more work); add calibration only if profile tests reveal systematic errors. A deterministic score component may be useful internally if units and conditions are documented, but no universal efficiency score is promised.

Poor-advice regression cases: Shadow must not trigger a default swamp grind; Bowfa + crystal must not imply Masori improves Bowfa; tbow must not hide Zaryte's supported special role; a target already owned in another equivalent form must suppress its grind; full treads must not trigger duplicate Cerberus upgrades; zero max-hit delta may still carry accuracy value but must not claim a damage breakpoint; declined CG must leave viable alternatives; scarce enhanced seeds must not be spent twice; prayer level must not substitute for an unlock. Include recently released superior alternatives, not only historic BiS.

## Quantitative contract and implementation strategy

**Decision: extend an independently authored local Java combat core by validated mechanic families.** Reuse the P4a model as a regression baseline. The Wiki calculator remains a pinned development-time differential reference, not a runtime dependency or source of player-data export. No JS engine, webview, subprocess or cross-plugin internal dependency.

A comparison's input contract includes exact baseline/candidate slot items, charges/ammo assumptions, real and effective levels, attack type/style/speed, prayers, boosts, spell/element, task/salve conditions, NPC ID/version, size, defensive stats and selected phase/scaling/HP/reduction assumptions. Every field is observed, explicitly assumed or unknown. Report model/knowledge revision, exclusions and supported/conditional/unsupported **per metric**.

Propose a complete compatible reference setup from equipped/observed owned items plus conservative reviewed templates. Keep current-versus-proposed labels accurate. Unknown gear cannot silently become empty or zero. Offer a focused correction, not eleven compulsory dropdowns. Start with direct slot substitution and a few reviewed packages, not a universal optimiser. Display the strongest supported owned competing setup for the selected context; preserve candidate uncertainty. Confirmation improves input agreement, not mathematical validity.

| Layer | What is implemented incrementally | Required boundaries |
|---|---|---|
| Q0 (existing) | Ordinary seas/swamp max hit, accuracy and DPS against Dharok | P4a's very small slot vocabulary; not full magic coverage |
| Q1 | Typed item stats, loadouts, target profiles, shared rounding/roll/distribution primitives; standard melee/ranged/magic attacks | Equipment incompatibility, ammo, style, zeroes, immunity, prayer/boost order, attack interval and negative bonuses |
| Q2 | Set and target multipliers, elemental spells, powered staves, dragon/demon/undead/task effects | Void/crystal/obsidian/Barrows; salve/task stacking; target ranged defence categories; current release rules |
| Q3 | Tbow scaling, Shadow multipliers/caps, Scythe multi-hit/size, Fang accuracy/min-hit/ToA differences, Soulreaper stacks, Eye/Confliction state effects | Per-family checked modules; readiness is a planning condition, not a damage modifier; no generic percentage shortcut |
| Q4 | Expected special attack damage, bolts/HP caps, defence reduction and bounded special-energy budgets | Zaryte hit/proc/HP conditions, Lightbearer/Death Charge/surge assumptions, Soulflame/team effects and competing specs; no permanent average DPS uplift without a defined cycle |
| Q5 | Analytical expected target kill time and narrowly defined resource scenarios | Hit distributions, overkill, first-hit timing, phase thresholds, regeneration, HP and supply assumptions; policy review before any encounter-simulation-like feature |

These layers are capability dependencies, not a requirement to finish all Q2 before any Q3 module. Prioritise models with large catalogue reuse, and implement endgame examples early.

Numerical rules: whole max hits; separate accuracy percentage points from relative gain; rounded DPS with full values in tests; zero baseline yields an absolute change, not division by zero. `HP / DPS` may be labelled a simple uninterrupted damage-time proxy if shown, but never expected kill time. A proper finite-HP expectation must account for the attack distribution and overkill. Trip estimates need incoming damage, healing, prayer, supplies, resupply and execution assumptions; default to qualitative sustain until independently validated. A claim such as “two extra kills” must remain a scenario range, never an account promise.

The current Hub restrictions also reject content simulations. That is a **policy uncertainty for Q5**, not a proven exemption for a planner or blanket prohibition on conventional DPS arithmetic. Do not build playable encounter simulators, combat rehearsal, live prayer/movement guidance or automatic gameplay. If analytical trip/encounter modelling approaches that boundary, seek policy clarification before shipping that feature; keep delivering Q1–Q4.

Validation: hand-derived integer/rounding/zero/negative/breakpoint cases, original synthetic fixtures against a pinned reference, and disagreement investigation. Include tests with realistic complete loadouts and at least two target profiles per general mechanic, plus unusual target effects. Freeze input/source versions. Oracle agreement alone does not establish current game correctness; release notes may invalidate both models. Never silently fall back to a stale oracle.

## Architecture changes against actual code

| Existing code | Retain / change and why |
|---|---|
| `ObservationSession`, `PreferenceIO`, `LocalPreferences`, lifecycle guards | Retain identity/generation/revision protections, serial IO, migration and local-only semantics; extend regression coverage for new input types. |
| `AccountSnapshotReader` / `AccountSnapshot` | Extend sets to slot/quantity observations and selected sourced unlocks; preserve compatibility views. Avoid broad speculative account scraping. |
| `KnowledgeCatalog` | Retain bundled typed JSON, source validation, stable IDs and cycle checks. Remove slice quotas (12 upgrades, 64 decisions, 256 sources, 32 activities etc.) as content ceilings. Keep justified byte/depth/resource guards with documented error handling and scalability tests. |
| Catalogue comparison schema | Replace `NONE/OCCULT/SWAMP` switch, single `ComparisonSpec`, hardcoded Dharok validation, and Doom-specific `minDelve` shared rewards with typed model/context IDs and generic route eligibility. Keep mechanic code explicit, data non-executable. |
| Source model | Allow reviewed official Jagex and approved research references with safe fixed URLs; current Wiki/ItemID-only URL allowlist excludes needed evidence. Generalise source links to facts/items/models/routes/contexts, not only decisions. |
| `Advisor` | Keep truth/ownership evaluation; extract small `UpgradeEvaluator`, `RouteEvaluator` and `RecommendationRanker` only as complexity requires. Feed structured comparison/value evidence into ranking; separate relevance from readiness. |
| `PoweredMagic` / `MagicReference` | Extract shared `CombatSetup`, `TargetProfile`, `ComparisonResult` and `CombatComparisonService`; retain existing tests. Separate input proposal/validation from formula modules and rendering. No engine per item. |
| `IronpathPanel` | Split overview/detail/path reusable Swing components. Render immutable view models; do not instantiate calculators or build domain evidence in rendering. Preserve navigation/scroll state and targeted confirmation guards. |
| `catalog.json` | Split authoring by combat/content family only when needed for readable diffs; assemble/load deterministically in the same jar. Do not add a database, runtime scripting or distributed knowledge service. |
| Legacy goals and broad advice | Retain useful routes and stable preferences; keep goals secondary. Gradually surface eligible legacy outcomes as upgrade cards. Retire duplicated text/CoX-centric primary navigation, not accurate source history. |

## Keeping knowledge current

Knowledge travels with normal reviewed plugin releases. No runtime scraping or rule downloads.

1. Keep source URL, author, source kind, revision/commit/publication date, access/review dates, release status, reuse basis and affected fact/model/route/context IDs. A page revision date is not a mechanics-review date.
2. Weekly maintainer triage of official news/RSS and model changes; before each release inspect affected mechanics; monthly discover sources/items absent from the watchlist. This is a recommended development routine, not a scheduled automation created by this session.
3. Add one optional maintainer-run metadata/change-report tool, after API permission/access checks. Cache, identify requests and back off. Compare source revisions, upstream model commits and official updates; suggest affected IDs and new candidates. Never auto-promote evidence, rewrite recommendations or publish.
4. Check official release status and subsequent fixes; corroborate disputed strategy with encounter-specific expert evidence. Original explanations, no guide harvesting. Track proposals separately from released content. Existing creators/Discord membership do not imply permission to redistribute.
5. Update affected entries and reference fixtures, bump knowledge/model versions, validate all links/variants and run affected account scenarios. Schema bumps require migration/retirement decisions. Release through reviewed GitHub/Hub commits when authorised.
6. Known-invalid claims are withdrawn or corrected; stale entries show a review warning and cannot claim current numerical authority. A 90-day review age can trigger triage, not automatic declaration that every fact is wrong. A malformed bundle fails closed with a useful message. Keep rollback to a known-good release.

Measure actual maintenance time. The earlier 20–40-minute quiet-week estimate was never observed capacity and cannot justify a complete endgame catalogue. First reduce duplicated facts and improve dependency checks; then slow rollout or seek contributions. Any intentional reduction of the long-term coverage commitment must be an explicit product decision, not an AI agent's silent simplification.

## C. Delivery and success

Use R1–R8 in [TASKS.md](../TASKS.md). Source validation, model work, UI and tests belong inside useful implementation PRs, not repeated planning gates. A concrete correctness/policy/licensing blocker applies to the affected claim or feature; it is not a blanket stop on independent catalogue work.

A representative midgame, post-Bowfa and advanced account must each be able to explain several useful choices and their next steps. Late-stage completion can correctly yield few or no meaningful upgrades; never pad the shortlist. Assess personal relevance, comparison trust, acquisition clarity and actual sidebar usability separately from test counts.
