# IronPath product and architecture

Product refinement · 13 October 2026 · proposed R1.5 specification, on the implemented but unmerged R1 baseline. Supersedes the reset’s ranking and UI choices; preserves its comprehensive scope.

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
| Attractive UI versus sidebar space | Native Swing, concise pursuit/activity rows plus a responsive two-column item grid, progressive disclosure and client item sprites. A larger optional surface must pass current policy review; it is not a core dependency. |

No rewrite, hosted service, database, user-facing AI, subscriptions, gameplay automation or mandatory other plugin. Remain one Java/Gradle project. Standard Ironman main-game planning is the baseline. UIM storage optimisation, GIM shared-bank inference, HCIM safety certification and seasonal mechanics require separate explicit support; they must not leak into ordinary recommendations.

## R1 implementation — 13 October 2026

The baseline findings below are the reset's historical assessment. R1 now delivers full supported powered-magic loadouts, typed `CombatSetup`/`TargetProfile`/per-metric `ComparisonResult`, an independent family model and comparison service, and native Value/Path views. The P4a `PoweredMagic` name is retained as a compatibility facade; arithmetic is shared. Ten outcomes include the named endgame slice, while coverage remains open.

Current upgrade ranking uses capability/context bands, supported absolute marginal DPS, metric confidence, optional goal relevance, remaining effort and stable ID. The 5% relative-gain boundary distinguishes material versus small *ordinary sustained-magic* improvements in this slice; it is a transparent calibration choice, not a universal rule for specs, sustain or other combat families. Shadow uses its capability role when a positive supported gain exists; zero/negative gains are deferred. Encounter prose count has no influence. One Ancestral piece occupies the primary shortlist; every relevant piece remains inspectable. No RNG hours or fabricated efficiency score.

Equipment slots and quantities extend immutable snapshots without changing persisted preferences. Known/unknown quantity semantics deliberately avoid summing observations made at different times. Typed ALL/ANY acquisition dependencies short-circuit owned intermediate components. Sources now link items, models, routes and contexts and permit reviewed official Jagex evidence. Content quotas are gone; a 4M-character parser budget, condition fan-out/depth and per-path depth guard bound resources. ToA scaling and special/state models remain explicit gaps.

See [validation/R1.md](validation/R1.md) for formula boundaries, fixtures, reproducible oracle commands, native-width evidence and pending client checks. The full product ambition remains unchanged; the refinement below inserts R1.5 before/alongside R2. This implementation starts at PR #8's exact head; it does not alter the open stack.

## Verified implementation and PR stack

Rechecked GitHub open-PR metadata and local Git ancestry on **13 October 2026**. All nine PRs are open and unmerged. Main remains `c717927cdf4e3fd8128d28b96e05690965f5a908`.

| PR | Head / SHA | Target | Relationship |
|---|---|---|---|
| #1 | `t1-account-advisor` / `f224f41` | `main` | Earlier observation work |
| #2 | `t2-cox-catalogue` / `e52728d` | `t1-account-advisor` | Earlier catalogue work; draft |
| #3 | `t3-local-advisor` / `b3a93d9` | `t2-cox-catalogue` | Earlier local planning |
| #4 | `t4-user-maintenance-trial` / `fad507f` | `t3-local-advisor` | Historical trial preparation; draft |
| #5 | `p1-p3-broad-pvm` / `eb22fef` | `main` | Consolidates earlier work and records |
| #6 | `docs/upgrade-first-roadmap` / `2ef409a` | `p1-p3-broad-pvm` | Ancestor of #7 |
| #7 | `p4a-upgrade-advisor` / `b50b0a9` | `docs/upgrade-first-roadmap` | Ancestor of #8 |
| #8 | `docs/product-reset-20261012` / `7d31483` | `p4a-upgrade-advisor` | Product reset; ancestor of #9 |
| #9 | `r1-full-loadout-magic` / `309afb2` | `docs/product-reset-20261012` | Current implementation baseline |

This documentation-only branch starts at #9's exact head, `309afb27ba7a6e0aee43d6c078b1234beff34c55`, and targets `r1-full-loadout-magic`. It preserves the entire #5 → #6 → #7 → #8 → #9 ancestry. No application code, runtime catalogue, existing PR metadata or branches are changed. Integrate/retarget deliberately when authorised; never blindly merge overlapping earlier PRs. Recheck remote heads before starting implementation.

R1's 124 tests and 540 new plus 234 retained oracle cases are **historical implementation evidence**, not tests rerun by this documentation assignment. Actual-client checks remain pending. R1.5 effort estimates, ranking categories and redesigned cards are **proposed**, not delivered features.

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

## Recommendation design — R1.5 decision

### Independent assessment of R1

Retain R1's complete supported loadouts, per-metric evidence, typed routes and identity protections. Its actual `Advisor.upgrades()` comparator orders value band, absolute marginal DPS, metric support, optional interest, route availability, effort, then ID. A Shadow-specific capability band and a 5% ordinary-magic calibration affect the first comparison. Effort is too late to prevent a powerful rare item dominating. The top-three family adjustment improves variety, but does not distinguish an item worth owning from an activity worth spending time on. Absolute DPS deltas also cannot compare different targets or utility roles fairly.

The R1 overview capture has roughly 300-pixel cards, repeated reference prose and full-width Open details buttons. The detail repeats numerical summaries and makes source/ownership controls compete with value. `ItemManager` is already called: sprites need better composition and client verification, not a new asset pipeline. See [UX](UX.md). The user-mentioned screenshot was not present in the supplied attachment; inspected `images/r1/r1-overview.png` and `r1-value.png` are the documented synthetic baseline, not a claim to have seen a live screenshot.

**Decision:** separate ownership value, pursuit priority, and activity value. Deliver deterministic categories and comparisons, not a universal efficiency score or a DPS-per-acquisition-hour ratio. No mandatory goal selection; use reviewed common encounter contexts, clearly labelled as defaults rather than inferred preferences.

### Evidence and categories

Every candidate retains independent axes: contextual value (capability/material/specialist/marginal), remaining effort, RNG tail, access/equip state, evidence support, and optional preference. Effort is route-specific; rarity is not an item-wide permanent verdict. An owned horn can turn Zaryte into a useful assembly plan; the original Nex rarity must not rank that remaining route.

| Presentation | Deterministic admission rule | Meaning |
|---|---|---|
| Worth pursuing | Positive, reviewed account-specific role; viable unskipped route; no unresolved ownership/access fact that changes the advice; bounded or repeatable effort with credible evidence; no adverse dominance or unresolved substantial dependency | A defensible active pursuit, not a promise that the player can execute it |
| Worth doing | Feasible unskipped activity route without consequential access unknowns, plus at least two distinct, non-dominated, useful missing reward families with sufficiently known ownership, or one useful reward plus a separately evidenced progression unlock | A shared opportunity; identify the useful rewards and what remains uncertain |
| Long-term | Valuable outcome with reviewed long-tail acquisition or high preparation/coordination burden | Worth owning; not automatically the next grind |
| Check first / Plan ahead | Consequential ownership, prerequisites, pace or remaining-work gaps prevent an active-pursuit claim | Conditional value remains discoverable; name the next useful check/step |
| Specialist / Small gain / Defer | Narrow positive role, little demonstrated improvement, or dominated route | Preserve context and searchability; do not pad priorities |

Specialist value can qualify as Worth pursuing if its relevance and effort justify it; specialist is a role, not a permanently low-priority class. A preferred long grind remains Long-term with a preference marker. Skips remove that route from pursuit/activity selection but do not erase item value or owned utility. An inaccessible upgrade stays in Plan ahead, with the nearest genuine dependency. Unknown never maps to zero hours, low effort or confirmed absence.

Primary item-group precedence: owned equivalent → omit acquisition; demonstrated no worthwhile role/dominated → Defer; consequential unknown → Check first; confirmed blocking prerequisite → Plan ahead; remaining long-tail/high-burden route → Long-term; positive qualifying route → Worth pursuing. Specialist/value and tail labels remain independent badges, so Check first does not erase known rarity. If multiple routes exist, assess each and use the highest justified pursuit category (Worth pursuing before Long-term before conditional) among unskipped routes; keep alternatives and their exclusions in Path. Never choose a route on missing effort alone. Activity entries are separate assessments: blocked/uncertain access yields a conditional activity, not Worth doing. Reuse R1's supported ordinary-magic value calibration initially; new role-specific value bands require reviewed conditions, not a universal 5% rule.

### Deterministic selection procedure (version `pursuit-1`, proposed)

1. Reuse validated contexts and ownership/usability guards. Establish strongest supported owned alternatives for the same role/context; keep recognised unsupported competitors visible. Calculate supported metrics and retain separate qualitative utility. Do not use missing metrics as zero gain.
2. Evaluate each route's remaining DAG, including components, skill/quest work, supply cost and upkeep. Assign evidence-backed acquisition modes or UNKNOWN: READY (only assembly/claim remains), BOUNDED (known finite task), REPEATABLE (ordinary repeat RNG), LONG_TAIL. These describe the process, not an ordered time scale: a finite task can take longer than a random drop. Separately retain a reviewed remaining-burden band LOW/MODERATE/HIGH/UNKNOWN with route-specific evidence and matched conditions. LOW requires little remaining preparation and limited known work; MODERATE permits repeated sessions with supported access/resources; HIGH captures substantial known work or coordination. Do not assign these from rarity alone or guessed hours; a substantial unquantified prerequisite forces Check first/Plan ahead, even for a common drop. Keep unusually long RNG tails visible independently of mean effort.
3. Within the same role/context and compatible assumptions, mark A dominated only when B offers at least the supported value, no worse access/work/upkeep, and is strictly better on one supported dimension. Work dominance needs an explicit remaining-step comparison or conservative non-overlapping **expected-effort ranges across the supported pace scenarios**, with no worse reviewed tail/coordination burden; tier labels alone do not prove it. This is dominance of the stated planning evidence, not a guarantee B drops first. UNKNOWN or overlapping estimates imply a trade-off, not dominance. Different encounters, special roles and shared resources prevent inappropriate comparisons. This is a partial order, not a comparator that can form sort cycles.
4. Apply the admission rules above, before sorting on item strength. Long-tail classification cannot be overcome by a large DPS number. Form activity candidates from stable activity/mode IDs and deduplicated useful reward families. Exclude owned/dominated rewards and distinguish uncertain ones; a full Ancestral set is one dependent family, not three votes. Never manufacture a second reward to keep CoX promoted.
5. Sort **within each presentation group** by readiness of next step, reviewed remaining-burden band (LOW/MODERATE/HIGH; UNKNOWN separate), value band, explicit encounter preference, supported context coverage and evidence quality. Do not sort BOUNDED before REPEATABLE merely because one is deterministic. In Long-term use value/relevance first, with effort retained as a trade-off. Numerical deltas may break a remaining tie only within a bucket whose metric, target, baseline, role and model assumptions all match; otherwise skip that key. Stable ID is always the final tie-breaker. Activity order uses best supported reward value, then presence of an independent useful family/unlock, feasible remaining route, preference and stable ID; it does not sum DPS or award unlimited points for catalogue size. Return the matched ordering reasons as data. Build fixed comparison buckets rather than using pair-dependent sort rules that violate comparator transitivity.
6. Overview selects up to two distinct active pursuits, one shared activity and a long-term preview **when justified**. These are layout slots, never catalogue caps or a quota to fill. Deduplicate activity/route families so the same grind does not consume every slot; list all relevant outcomes in Browse. If only long-tail rewards remain, say so and show them without changing their effort class. Unknown/provisional candidates have their own visible entry, not silent exclusion.

Band boundaries and layout choices are product calibrations, not mathematical optimality. Version them and test changes against TASKS' profile matrix. An effort tier may be revised when actual remaining work changes; the UI must state why. Optional short-session/team preferences may filter or annotate compatible routes without certifying readiness. Do not start R1.5 with a preference questionnaire.

### Opportunity cost and activity rewards

Show a concrete alternative: “This remains a long-term upgrade; tormented bracelet has a nearer component route for your current magic setup,” only if those account facts and relative effort are supported. If credible time scenarios exist, compare outcomes over the **same declared budget** using each route's acquisition distribution. Say “chance of at least one useful reward,” not “you could complete three upgrades.” Do not add individual quantiles, DPS percentages across targets, or mutually exclusive allocations of an enhanced seed/zenyte/special energy. Resource allocation choices must remain alternatives.

Activity value declines as useful missing rewards disappear. Many useful CoX rewards can make Chambers worthwhile without making Twisted bow a targeted recommendation. With only tbow missing, that activity loses its multi-reward explanation and remains a long-tail choice. Shadow, Scythe and Nex routes follow the same rules, with their actual loot mechanics and constraints; none gets a hardcoded “best next” exemption. Common supplies may be mentioned qualitatively; do not convert GP value into Ironman progression benefit.

### Reusable acquisition contracts and architecture

Extend the existing immutable catalogue and advisor contracts; names below are an implementation target, not a schema already shipped. Keep parsing/version migration inside the current project and `TASKS.md` as the only backlog.

| Contract / component | Minimum fields and responsibility |
|---|---|
| `AcquisitionModel` | Stable model/activity/mode/reward-family IDs, eligible recipient, trial unit, mechanic kind (fixed Bernoulli, multiple rolls, deterministic; stateful unsupported initially), rational probabilities, assumptions, source IDs/revisions, review and model version, support state |
| `PaceScenario` | Successful eligible trials per active hour or complete cycle minutes, source kind (user scenario/reviewed benchmark/later observed), scope of overhead, mode/team/points configuration, date and input provenance; optional rather than fabricated |
| `EffortEstimate` | Route/node ID, remaining outcome, support level, mean trials, P50/P90 and optional P10, conditional hours if supported, prerequisite/assembly/supply exclusions, unknown reasons, RNG tail and pace sensitivity kept separate |
| `ActivityOpportunity` | Activity/mode key, distinct useful outcome families with account relevance/ownership confidence, mutually exclusive allocations, shared prerequisites and non-additive explanation; quantitative union only with supported joint mechanics |
| `PursuitAssessment` | Value evidence reference, effort reference, category, blockers, relevant alternative, matched reason codes, comparison/ranking versions and stable ties; immutable render input |
| `AcquisitionEstimator` | Pure analytic unit-to-time conversion and supported distributions; no client reads, RNG simulation, network or Swing |
| `RecommendationRanker` | Category admission, dominance graph, stable group sorting/diversity, explanation evidence; extract from Advisor only as needed |
| Later `ActivityObservation` | Character generation, start/end, completed/failed/abandoned/censored, elapsed active session and in-instance scope, mode/team/points, detection provenance and validation status |

Data path: client snapshot + preferences + bundled facts → existing comparison/path evaluation → acquisition evidence → pursuit/activity assessment → immutable overview/detail models → Swing. Quantitative evidence is optional independently of route and value evidence. Extend source dependencies to probabilities, reward pools, pace assumptions and observation adapters so a changed rate invalidates only affected estimates. A stale/invalid rate withdraws time/odds, not an otherwise valid value comparison.

Raw observations and inferred pace stay memory-only. R1.5 permits optional manually entered pacing as a labelled scenario, never measured performance. Session observation later improves that scenario without silently changing mode/team assumptions. Persisting derived history would be a separate explicit privacy/migration design in R5; this refinement does not authorise raw account/loot persistence, remote telemetry or reading another plugin's private stores.

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

## Architecture retained from the product reset (historical gap map)

R1 has already delivered the slot/quantity, comparison, route and source changes described in its implementation section. The table below preserves the reset rationale, not a fresh list of missing R1 capabilities; R1.5 additions are specified above.

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

Use R1, R1.5 and R2–R8 in [TASKS.md](../TASKS.md). Source validation, model work, UI and tests belong inside useful implementation PRs, not repeated planning gates. A concrete correctness/policy/licensing blocker applies to the affected claim or feature; it is not a blanket stop on independent catalogue work.

A representative midgame, post-Bowfa and advanced account must each be able to explain several useful choices and their next steps. Late-stage completion can correctly yield few or no meaningful upgrades; never pad the shortlist. Assess personal relevance, comparison trust, acquisition clarity and actual sidebar usability separately from test counts.
