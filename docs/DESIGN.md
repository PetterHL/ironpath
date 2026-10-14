# IronPath product and architecture

**R3b family review implementation, 14 October:** `melee-2` extends the existing complete-loadout contracts with reviewed weapon/condition modules, an effect-aware owned-package frontier and component-aware paths. `ComparisonResult.OffenceScope` distinguishes steady reference offence from Soulreaper's next-five-swing mean; both remain separate from encounter/trip performance. Target/task/stack/Defence changes reconsider owned alternatives while explicit slot/style corrections remain focused. Fixed ToA/Olm scenarios do not bypass the scaling gap in other combat families. [Evidence and pending client/coverage gates](validation/R3b-families.md). This preserves the delivered `progression-1` engine; EP1/UX2 remain pending as selected below.

**14 October 2026 decision:** account-efficient progression (`progression-2`) is the selected next policy; EP1/UX2 in TASKS own implementation. Current main still runs `progression-1`. This documentation change does not deliver new ranking, acquisition hours or UI. The new policy supersedes importance-first ordering and forced long-term preview insertion while retaining the relationship/value foundation.

Endgame intelligence design · 13 October 2026. Inspected main `cb7c2cd` and unmerged R2 PR #16 at `a501e6f`, then its concurrent review correction `32cb24a` (schema 9, `pursuit-1`). **The bounded `progression-1` migration is implemented for review in R3a; broader relationship/model coverage remains open.** See [implementation evidence](validation/R3a.md). It refines ranking and relationships while retaining comprehensive scope, existing working code and validation history. EP1's reviewed automatic effort profiles are the next priority; R1.5b observation and complex route totals remain separately scoped work.

## A. Stable product vision

**IronPath — Your path to endgame PvM** is a free, open-source, local RuneLite plugin for established Ironman accounts. Opening it should answer: **Which upgrades offer the most worthwhile progress for the remaining effort on my account now, why, and how do I obtain them?** No goal, setup wizard, account registration or paid API is required.

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
| Attractive UI versus sidebar space | Native Swing, compact visual upgrade cards with client item sprites and progressive disclosure. One column at ordinary width; two only with measured readable content under UX. No larger-window dependency. |

No rewrite, hosted service, database, user-facing AI, subscriptions, gameplay automation or mandatory other plugin. Remain one Java/Gradle project. Standard Ironman main-game planning is the baseline. UIM storage optimisation, GIM shared-bank inference, HCIM safety certification and seasonal mechanics require separate explicit support; they must not leak into ordinary recommendations.

## R1 implementation — 13 October 2026

<details>
<summary>Preserved R1 implementation snapshot; current R2 findings and next design follow below</summary>

The baseline findings below are the reset's historical assessment. R1 now delivers full supported powered-magic loadouts, typed `CombatSetup`/`TargetProfile`/per-metric `ComparisonResult`, an independent family model and comparison service, and native Value/Path views. The P4a `PoweredMagic` name is retained as a compatibility facade; arithmetic is shared. Ten outcomes include the named endgame slice, while coverage remains open.

Current upgrade ranking uses capability/context bands, supported absolute marginal DPS, metric confidence, optional goal relevance, remaining effort and stable ID. The 5% relative-gain boundary distinguishes material versus small *ordinary sustained-magic* improvements in this slice; it is a transparent calibration choice, not a universal rule for specs, sustain or other combat families. Shadow uses its capability role when a positive supported gain exists; zero/negative gains are deferred. Encounter prose count has no influence. One Ancestral piece occupies the primary shortlist; every relevant piece remains inspectable. No RNG hours or fabricated efficiency score.

Equipment slots and quantities extend immutable snapshots without changing persisted preferences. Known/unknown quantity semantics deliberately avoid summing observations made at different times. Typed ALL/ANY acquisition dependencies short-circuit owned intermediate components. Sources now link items, models, routes and contexts and permit reviewed official Jagex evidence. Content quotas are gone; a 4M-character parser budget, condition fan-out/depth and per-path depth guard bound resources. ToA scaling and special/state models remain explicit gaps.

See [validation/R1.md](validation/R1.md) for formula boundaries, fixtures, reproducible oracle commands, native-width evidence and pending client checks. The full product ambition remains unchanged; the refinement below inserts R1.5 before/alongside R2. This implementation starts at PR #8's exact head; it does not alter the open stack.

</details>

## Current integration and historical PR stack

Current development baseline: main integrates R2 #16, progression design #17, R3a #18, Kill Impact design #19 and UX #20 on 14 October 2026. Plugin 0.8.3 implements progression-1 and native card/search/navigation; Kill Impact remains design only. Earlier model #14 is archived as the `r2-ranged-models` snapshot tag rather than mixing its superseded engine into R2. [Integration and cleanup](validation/main-integration-20261014.md). New deliveries start from updated main; actual-client acceptance remains pending.

<details>
<summary>Preserved progression research inspection before integration</summary>

Current research snapshot: main is `cb7c2cd`, with #12/#13/#15 integrated. R2 PR #16 advanced from `a501e6f` to `32cb24a` during this assignment; its package/necklace corrections and 195-test historical result are preserved. Earlier ranged PR #14 (`f821c625`) remains separate. This documentation change targets #16 and changes no implementation. Recheck remote state before implementation or integration. No merge is authorised by this research assignment.

</details>

<details>
<summary>Earlier integration and pre-integration snapshots (preserved history)</summary>

On 13 October 2026, the user authorised integration and cleanup. PRs #5–#10 were merged into main in order with merge commits, preserving the complete earlier ancestry. PR #1 was automatically marked merged; #2–#4 were closed as superseded after ancestry verification. The tested implementation is now on main; R1.5 remains proposed. Future delivery starts from updated main. See [integration validation](validation/main-integration-20261013.md).

The following snapshot records the **pre-integration** inspection and original refinement-PR base, not current branch instructions.

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

</details>

## B. Required capabilities and coverage

[CATALOGUE.md](CATALOGUE.md) is the coverage inventory; [UX.md](UX.md) specifies the interface. [RESEARCH.md](RESEARCH.md) records source limits. [TASKS.md](../TASKS.md) is the only backlog.

1. **Observe:** identity-isolated levels, equipment slots, quantities, bank observation provenance, selected quests, prayers and permanent unlocks; explicit unknowns for unavailable state.
2. **Understand alternatives:** exact item forms grouped into ownership families, but charge state, stats and usability remain distinct. Recognise strong alternatives before activating acquisition advice.
3. **Recommend:** independent value dimensions and account-specific importance, effort/status and optional interests; role alternatives, sufficient preparation and bounded indirect explanations; several useful choices with no goal.
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

<a id="progression-intelligence-contract-version-progression-1"></a>

## Progression intelligence contract (version `progression-2`, selected)

**Selected next design; implementation pending in EP1/UX2.** R3a implements the earlier `progression-1` baseline. This contract supersedes its importance-before-burden ordering and discovery insertion, as well as older `pursuit-1` ordering. Preserve historical implementation and regression evidence; update behavioural expectations only where this explicit policy change requires it. [Thirty checked relationships and the code assessment](PROGRESSION_RESEARCH.md) are the evidence; TASKS EP1/UX2 own the next migration; R3–R6 retain broader relationship and model coverage. This is an extension of the current engine, not another reset.

### Independent assessment

Keep the seven product principles, with three qualifications: importance is not an objective global ranking; adequate preparation is method-dependent rather than “technically possible”; comprehensive coverage does not require every relation to have an exact numerical model. Core, specialist and QoL roles remain eligible on highly developed accounts. An optional preference can favour lower attention, smoother learning or a personally interesting long grind; without one, show reasoned variety without assuming enjoyment.

The existing `Benefit` plus ordinary-DPS calibration cannot carry this information. R2 can downgrade a supported utility item for a minor ordinary result, counts numeric context IDs as breadth, conflates suggested preparation with blocking conditions, and aggregates activity availability across individual rewards. Fix these specific bottlenecks while preserving comparisons, acquisition distributions, observations and identity guards.

### Separate evidence from the next-step recommendation

| Output | Meaning and examples | Never infer |
|---|---|---|
| General role | Core ranged neck, high-HP burst, demon specialist, lower-attention multi-target method, sustain, inventory economy | A permanent S/A/B item rank or that the account wants the activity |
| Personal importance | **Key improvement / Strong option / Contextual option / Marginal or covered / Uncertain**, with matched reasons and a named baseline | Importance from missing ownership, an item price, a large neutral DPS number or relationship count |
| Pursuit status | Can pursue; Plan ahead; Check first; Long-term undertaking; Optional/defer; owned use/assembly separately | Low importance from long effort, readiness from ownership, or completion from a skip |
| Remaining effort | Account- and method-specific Short / Medium / Long / Very long bands; optional scoped scenario hours; remaining prerequisites, components and supplies | Measured performance from gear, cheapness from missing estimates, or an exact learning duration |
| Next-step priority | A deterministic judgement of marginal account value relative to remaining effort and method suitability; drives the default queue | Importance alone, a permanent item tier, raw DPS/hour or certainty of an optimal grind |

Importance is a versioned product judgement, not a measurement. **Key improvement** needs a reviewed consequential role gap or major improvement across relevant use cases. **Strong option** needs material core value or a substantial supported specialist/convenience benefit. **Contextual option** has a real narrower or preference-dependent benefit. **Marginal/covered** needs a demonstrably small role delta or an adequate/equivalent owned solution; covered is not globally useless. **Uncertain** names missing evidence. No tier requires positive sustained DPS when the established benefit is different.

Rarity cannot turn a Key improvement into Marginal. Being easy to obtain cannot turn a minor accessory into Key. A long-term item may remain personally important and can be pinned without removing its burden/tail label. Importance describes value; next-step priority decides where it belongs in the current queue. For a nearly complete account, a long grind may be the best remaining useful option. The current Long-term display is retained as an effort-oriented view, not a verdict that long-term progression is unimportant.

### Minimal data contracts

Extend the existing bundled JSON and immutable Java objects. The names below describe responsibilities; do not create a framework to preserve every name. Use existing safe ALL/ANY/level/possession/fact predicates and a finite registry of supported new inputs, not executable expressions.

| Contract | Minimum information |
|---|---|
| `ActivityContext` | Stable activity/mode/method ID; optional target/phase/delve; source-reviewed role IDs; hard access predicates; input provenance; optional linked numeric `TargetProfile`. A method can be learner/team/solo/repeat without claiming skill observation. |
| `RoleSupport` relation | Stable subject outcome/item/unlock ID, context and role; applicability and no-benefit predicates; benefit dimensions; qualitative significance/rationale; compatible owned alternatives; synergy/adverse-interaction references; resource/allocation conflicts; optional mechanic/model IDs; factual and strategic claim references; review/support state. |
| `PreparationOption` | Activity-context/role, reviewed acceptable packages (ANY/ALL), significance to the selected method, optional better alternatives, relevant constraints and sources. Separate `HARD_ACCESS`, `EQUIP`, `METHOD_CAPABILITY`, `RECOMMENDED_PREPARATION`, `OPTIONAL_IMPROVEMENT`. |
| `ActivityReward` (extend existing) | Activity/mode → outcome/family, **reward eligibility** independent of activity access, ownership/unlock predicate, allocation group, route stage and sources. Replace Doom-only `minDelve` with typed eligibility conditions; keep existing IDs. |
| `ValueEvidence` | Subject, role/context, current baseline, independent dimension results, applicability truth, missing inputs, comparison references and matched reasons. A dimension may be reviewed qualitative even when DPS is unsupported. |
| `PursuitAssessment` / `ActivityOpportunity` (extend) | Importance, method suitability, effort band and support, next-step priority, alternatives, sufficient-now option, direct/indirect explanation trail, preference provenance, evidence/knowledge/ranker versions. Views consume this data; no domain decisions in Swing. |
| Activity effort profile | Stable activity/mode/method ID; supported compatible package/role predicates; levels/unlocks; reviewed workload/pace assumptions, scope, source dependencies and exclusions. Effort-only or optional scenario-hour support; no global early/mid/endgame speed multiplier. |
| Presentation result (extend existing `UpgradeCard` and detail models) | Typed metric/value/unit/baseline, availability and corrective action, selected status, priority reason, effort band, optional range and range kind, estimate origin, scope and next step. No assembled diagnostic prose as normal card content. |

A small initial benefit vocabulary is enough: ordinary offence, burst/special, defence reduction, multi-target/control, sustain/defence, attention/convenience, inventory and resource economy, access. **Core/specialist are role/breadth attributes; indirect progression is a relationship trail**, not a multiplier to add to these dimensions. A relation can carry several benefits, and several relations can share a mechanic. Avoid both one Java rule per item and a universal weighted sum.

Alternative equivalence is scoped to role, method and conditions. Tbow does not cover Zaryte burst or Venator engagement; a blood fury is not dominated just because rancour increases DPS. Owned exact forms, quantities, charges, ammunition, skill/equip and conflicts still apply. Recognised but unsupported competitors prevent claims of numerical superiority; they need not erase an independently established utility fact.

The default **Enemy with 0 Defence** comparison remains available as explicitly requested. It is an analytical reference, has zero activity-breadth weight and cannot determine contextual importance by itself. Do not silently replace the selected reference or compare unrelated-target DPS deltas. A specialist card leads with the supported role; the neutral number remains in its comparison detail.

### Evaluation and simple relationship traversal

**Without a selected goal**, seed relevance from released, reviewed roles and the account's owned alternatives, unlocks, levels and reachable progression paths. Broad core improvements need no inferred favourite boss. An unmet specialist role supplies a contextual option, not proof that the player wants that activity. Activity opportunities derive from independently useful missing outcomes, not from how many preparation edges point at them. An unknown preference keeps trade-offs visible. A skill/access gap changes route/status; it does not automatically erase future value. This supplies useful automatic advice without inventing play history or recursively inferring desire from a reward loop.

1. Evaluate observed facts and explicit preferences against reviewed relations. Retain TRUE/FALSE/UNKNOWN, exact forms and provenance. Goals/preferences refine prominence, never applicability or factual access. An empty bank observation does not establish global absence.
2. Resolve the strongest supported **compatible role alternatives**, using existing package proposals and role-specific qualitative evidence. Mark each role satisfied, improvement useful, gap, or unknown. Do not require a full loadout search optimiser. Any unsupported competitor is named.
3. Derive direct personal value and importance before looking through downstream rewards. Use authored, source-reviewed significance predicates and tested family thresholds. The old 5% calibration remains only within a separately justified ordinary-comparison context; remove the Shadow-ID ranking exemption as that path migrates. Duplicate prose/targets/relation rows never increase importance.
4. Evaluate remaining acquisition paths and distinguish hard access from method/preparation/equip. A missing optional improvement never blocks the route. A missing capability can mark a **selected method** unsuitable while another reviewed method remains available. Unknown skill/learning is not a failed access predicate. Owned intermediate items bypass only the corresponding upstream work.
5. Build activity opportunities from **usable or meaningfully obtainable reward outcomes** and independent access/method evidence. An ineligible or uncertain reward stays separately listed; it must not veto known eligible rewards or make the whole activity unavailable. Two rewards support a shared-value explanation, not automatic promotion. One important reward can justify an activity without inventing a second. Three Ancestral pieces remain one correlated family for discovery, while their actual distinct loot outcomes stay separate in probability models.
6. Join `RoleSupport(item, activity context)` to that activity's account-relevant rewards for indirect explanations: “Useful at this activity, which still offers these upgrades.” Initially expose **one activity hop**, with direct value as the fixed anchor. Already adequate preparation reduces the upgrade's indirect importance. This is explainability, not multiplication of downstream item scores or recursive promotion.
7. Derive remaining effort and next-step priority using the policy below; rank eligible routes and choices, retaining conditional, long-term, owned-utility and deferred records in Browse/detail. Deduplicate the same reward family/acquisition activity in the small overview, without truncating coverage.

These are indexed joins over ordinary lists/maps, not a graph platform. Hard component/access edges form an acyclic dependency graph. **Utility edges may cycle**: Rigour improves CoX, which awards Rigour. Never import a utility edge into hard access or let it reinforce its own importance. Visited-ID trails prevent repeated explanations; no recursive fixed-point score. Additional hops can be inspected later using the same records and cycle guards; the first visible hop is a delivery/presentation choice, not a relationship-coverage ceiling.

Example: missing tbow plus useful CoX rewards gives CoX independent activity relevance. Lance has a reviewed Olm role, but owned Fang/other gear might already satisfy the chosen method. Show lance's actual marginal comfort/damage and its remaining route, alongside starting CoX now. If only tbow remains, the multi-reward rationale disappears; neither lance nor CoX receives escalating priority through the cycle.

### Account-specific effort and suitability

**Universal scope:** this evaluation applies to every supported outcome, route and activity/method, including newly promoted catalogue entries. Named examples illustrate behaviour; they never define eligibility for personalisation. Share activity profiles, role predicates and route rules across items. EP1 must audit the complete active catalogue and migrate its default queue together, with a route-level coverage/support matrix. Numeric hours may be selective; the value/effort policy is not. An unsupported route needs a specific evidence gap and conditional handling, not an unexplained universal fallback or the old comparator.

Assess each activity/mode/method against the strongest supported **compatible owned** package, levels, prayers, unlocks, usable forms and supplies. Suggested account stages are summaries only: the same account may be well equipped for one activity and poorly equipped for another. Separate suitable, workable with limitations, substantial preparation required, and consequential unknown. A gear profile is not a claim about execution skill or completion probability.

Reviewed profiles automatically supply a broad effort band, without a questionnaire or mandatory kills/hour input. Select an optional reviewed full-cycle pace scenario only when its applicability is established; label hours as an assumption-based estimate. RESEARCH defines A0/A2 support and the review record. Missing numeric support may still permit a reviewed qualitative band. Missing account evidence remains distinct from confirmed absence. Unread gear must not silently select the weakest profile.

Compute **remaining** work: remove confirmed completed stages, retain actual access/training/assembly/supply work, and account for the supported method's repeated acquisition burden. Distinguish next reward from the full item. Use Short / Medium / Long / Very long plus an explicit unsupported/unknown state; document and version the calibration rather than assigning fixed item tiers. The same outcome can change band when prerequisites, owned components or its suitable package change.

Evaluate direct acquisition and relevant preparation detours as separate routes. For example, starting a reviewed Doom method now versus obtaining a synapse first requires the preparation's own effort plus the remaining Doom work under that alternative. Do not charge both routes, double-count shared requirements, spend one component on competing outcomes, or assume a detour saves total time. Preserve independent value of the preparation item. If the total cannot be supported, show the qualitative trade-off or a clearly scoped partial estimate.

### Deterministic next-step priority and selection

The default queue answers **worthwhile progress for remaining effort now**. Personal importance and effort jointly determine priority; neither is merely a late tie-breaker. Use a small versioned decision table over account-value bands, remaining-effort bands and method suitability, with matched reason codes. Do not divide neutral DPS by hours or add unrelated utility dimensions into a universal efficiency score.

Initial calibration direction (product judgements to validate in EP1, not new game facts):

| Account-specific result | Expected queue treatment |
|---|---|
| Substantial relevant improvement, suitable method, short/medium remaining work | Strong next-step candidate |
| Substantial improvement, workable method with relevant limitations | Adjust effort/priority using the reviewed profile; name the limitation |
| Exceptional value, very long remaining work, nearer substantial options exist | Long-term choice behind the better current opportunities |
| Tiny or already-covered improvement, short grind | Optional; cheapness alone does not create priority |
| Large foundational gaps for the acquisition method | Prepare first / later progression, no full-route time; surface independently useful reachable steps |
| Only long grinds or small useful improvements remain | Rank honest remaining choices; long-term items can lead with their label |
| Material unknown affecting the decision | Conditional result and one useful correction; no invented low cost or blanket removal of unrelated known value |

Distinguish hard access, method suitability, recommendation timing and acquisition horizon. A missing optional upgrade is never a hard requirement. Resolve these conditions before applying the value/effort policy. For equally justified candidates, use optional explicit interest, claim-specific evidence and stable comparable tie-breaks. Make preference effects explainable; a preference does not rewrite effort or factual suitability. Preserve transitivity, deterministic stable IDs and compatible metric/context buckets.

Reviewed multi-target, sustain, convenience, specialist and resource benefits can justify priority independently of ordinary DPS. Shared activities may offer several account-useful rewards, but reward count or repeated relationships must not multiply score or reduce the target item's time. Cyclic utility trails remain explanatory only.

Only claim dominance when the alternative is no worse on every material **known** dimension for the same role/context and route, and better on one. Unknown/incomparable benefits, upkeep, effort or overlapping scenarios imply a trade-off. Rank uncertainty must not masquerade as demonstrated dominance.

The homepage consumes the domain order. Remove mandatory early insertion of long-term/specialist representatives; use persistent compact discovery links and Browse for those views. A genuinely high-priority specialist keeps its place naturally. Deduplicate the same outcome and correlated reward-family repetition without moving a lower-priority long-term choice above better next steps. Retain all supported outcomes in Browse. If only long-term choices remain, show them with their truthful horizon; do not manufacture nearer advice.

EP1 changes `RecommendationRanker` and `PursuitOverview` together and exports a structured presentation result. UX2 renders it under UX.md. Reflow, sprite loading and filtering do not rerank. Preserve stable focus/scroll and make changed account evidence, rather than paint events, trigger recomputation.

### Preparation-detour safeguard

Every preparatory recommendation must answer: **What role improves? What already-owned option works? Is the improvement independently useful? What work remains? Can this player begin a reviewed method now?**

- Hard access is established by game requirements; item superiority never creates a hard gate.
- A reviewed adequate package means “this method supports using this gear,” not “you can certainly complete it.” Technical possibility alone is not enough evidence for a learner recommendation.
- Large work for a marginal single-activity preparation defaults to optional/defer, with the existing route shown. Broad direct value, substantial comfort or an explicit preference may justify a separate pursuit; state that reason rather than claiming the preparation pays for itself.
- Examples: no fresh lance requirement before team CoX; no forced Venator before quiver; no tbow/Zaryte/Scythe detour for an already suitable Scorching Doom setup. Conversely, missing necessary demonbane capabilities for the selected Doom method needs a real alternative/check.
- No arbitrary hour ratio or rarity threshold is needed. Where marginal benefit or effort is unknown, label the judgement conditional. Never assert the detour is mathematically inefficient without comparable evidence.

### Source review, migration and maintenance

Retain stable outcome/route/preference IDs. Add relation/context IDs and source links additively; explicitly migrate or retire old fields. Adapt current `applications`, `contextBenefits`, `activityRewards` and preparation predicates incrementally. Replace `reviewed-specialist` and Doom `minDelve` with real typed contexts/eligibility; don't leave two independent sources for the same fact. Bump bundled schema/knowledge/ranker versions as required; changing catalogue JSON alone does not require changing persisted preferences. New persisted preferences need the existing migration/identity protections.

Sources attach to **claims** as well as records: factual mechanic, strategic sufficiency, preference report, numeric model. Record revision/hash, review date, author/audience/method and released/proposed/withdrawn state. Add reviewed community URLs through explicit safe source types; linking a guide does not authorise copying its text/data/images. Numeric facts should rely on current primary mechanics evidence; community claims must not overrule implemented changes.

For each addition: review source/alternatives → author the relation and path → add positive/negative/unknown account fixtures → add numerical boundary/oracle cases only where needed → inspect native explanation → release through ordinary review. A maintainer can do this in one focused PR. CI checks referential integrity, typed conditions, duplicate equivalent relations, source dependencies, route cycles, contradictory requirements and known-invalid claims. No custom editorial service is needed.

An update invalidates only dependent claims/models. Example: new Venator targeting rules withdraw affected geometry/breakpoint claims while retaining the five-shard route and general multi-target role. Penny changes require consumed/active-state semantics; a changed drop weight invalidates acquisition odds, not combat stats. Manual release-note review comes first; optional tooling belongs to R7. Periodic review is a recommended routine, not a new scheduled automation.


## Historical R1.5 decision and current implementation contracts

<details>
<summary>Preserved pursuit-1 rationale and specification; ordering/admission superseded by progression-1</summary>

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

</details>

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

Raw observations and inferred pace stay memory-only. EP1 permits automatically selected reviewed activity-profile scenarios and optional manually entered pacing, both labelled assumptions rather than measured performance. Session observation later improves that scenario without silently changing mode/team assumptions. Persisting derived history would be a separate explicit privacy/migration design in R5; this refinement does not authorise raw account/loot persistence, remote telemetry or reading another plugin's private stores.

### R2 implementation of shared comparison contracts

R2 adds an independently authored ranged-1 family to the retained combat proposal/result contracts, with typed ranged item/ammo/set metadata and per-defence-type target inputs in schema 9. Magic and ranged have separate session-only correction/confirmation owners, sharing the existing snapshot/identity guards. No prayer/boost and Rapid are the automatic ranged assumptions. Neutral Defence/Magic/magic attack/all ranged bonuses are explicitly zero; no HP, size or family effect is assigned. Existing magic arithmetic/version is unchanged.

Positive reviewed specialist results may preserve a ranged long-term choice despite a neutral loss. Each contextual result has its own complete default proposal and inspectable inputs; different targets never supply a universal numerical ordering key. Confirmed owned acquisition stages lower remaining work and remove fresh activity reward votes, with quantity stages requiring actual scoped counts. No manual possession can establish supplied baseline gear or sufficient resources. See [R2 implementation and boundaries](validation/R2.md); R3–R8 and comprehensive inventory closure remain active.

## Personalized Kill Impact contract (selected, not implemented)

[Research and design](KILL_IMPACT.md) selects passive post-kill context within the existing advisor. Reuse ownership, comparison families, progression-1 importance, adequate alternatives and Value/Path; do not create a second catalogue or rank by seconds saved. Actual worn setup segments are distinct from an optimised reference and the strongest owned alternative. Recent boss evidence refines session relevance without creating a permanent goal or displacing account-wide advice.

Keep three separate outputs: observed kill duration; modelled offence for an explicit setup/phase; and, only after validation, estimated average impact on comparable future kills under the same method. The current kill-time enum is unsupported, not an existing engine. No last-kill × DPS-ratio shortcut, PB-as-average, additive upgrade gains, skill score or automatic acquisition-hours conversion. Full distributions, finite HP/overkill/first-impact timing, material phase changes and current levels/prayers/ammo must be handled before dependent time claims.

Add a small versioned encounter adapter and immutable summary/impact contracts beside the account reader. Capture bounded change evidence on the client thread; evaluate after completion outside the client thread/EDT; guard stale results by identity, generation and encounter key. Current account snapshots do not capture boosted/prayer/cadence history. Preserve conservative loading/profile resets until any broader cohort lifetime is explicitly tested. Active traces and bounded recent summaries remain in memory; no remote telemetry or silent performance persistence.

Target broad support across most relevant PvM bosses, explicitly including CoX/CM, ToA and ToB/hard-mode encounters. Vorkath is a worked example and possible pilot, not a mandated first target. Shared contracts support optional raid/run, room, phase/target, mode/scaling and party/role context from the outset; local offence, team kill-time impact and whole-raid time remain distinct. KI1 delivers contextual supported offence; KI2 adds validated same-method time sensitivity with explicit phase/proc/unknown refusals; KI3 selectively extends specials/supplies/methods. Repeated kills improve cadence/variability evidence, not unseen-item causality or incorrect formulas. Samples, model sensitivity and next-kill randomness remain different uncertainties. See [TASKS KI1–KI3](../TASKS.md#personalized-kill-impact--ki1ki3) for delivery/acceptance and [UX](UX.md#recent-boss-and-kill-impact-selected-not-implemented) for the compact surface. This refines R1.5b/R4/Q5 without blocking R3b or comprehensive coverage.

## Quantitative contract and implementation strategy

**Decision: extend an independently authored local Java combat core by validated mechanic families.** Reuse the P4a model as a regression baseline. The Wiki calculator remains a pinned development-time differential reference, not a runtime dependency or source of player-data export. No JS engine, webview, subprocess or cross-plugin internal dependency.

A comparison's input contract includes exact baseline/candidate slot items, charges/ammo assumptions, real and effective levels, attack type/style/speed, prayers, boosts, spell/element, task/salve conditions, NPC ID/version, size, defensive stats and selected phase/scaling/HP/reduction assumptions. Every field is observed, explicitly assumed or unknown. Report model/knowledge revision, exclusions and supported/conditional/unsupported **per metric**.

Propose a complete compatible reference setup from equipped/observed owned items plus conservative reviewed templates. Keep current-versus-proposed labels accurate. Unknown gear cannot silently become empty or zero. Offer a focused correction, not eleven compulsory dropdowns. Start with direct slot substitution and a few reviewed packages, not a universal optimiser. Display the strongest supported owned competing setup for the selected context; preserve candidate uncertainty. Confirmation improves input agreement, not mathematical validity.

**Default reference and primary metric — user direction, 13 October 2026 (implemented for review; client checks pending):** use an analytical **Enemy with 0 Defence** as the automatic comparison baseline, replacing automatic Graardor/Dharok selection. The POH combat dummy motivates the neutral comparison; its location and any forced-max-hit behaviour are not part of the product claim. The authored powered-magic profile fixes Defence 0, Magic 0 and magic-defence bonus 0, yielding ordinary defence roll 576. No HP, size, other defence types or creature-family modifiers are assigned; later combat families need their own reviewed inputs. Zero Defence alone neither defines the magic defence roll nor guarantees a hit. Use ordinary expected damage/hit distributions and attack intervals. Keep existing reviewed bosses as explicit contextual alternatives and regression cases; specialist usefulness cannot be inferred from the neutral number.

Lead weapon comparisons with theoretical DPS, including speed and full compatible package changes, and retain max hit as secondary. Armour/jewellery with unchanged attack interval may lead with max hit while retaining accuracy/DPS in detail; any interval-changing effect uses the weapon rule. Unsupported DPS is a visible per-metric gap, never max hit presented as DPS or a zero gain. This is a presentation/default-context choice, not a universal ranking score. Delivery and acceptance are in TASKS's reference/metric follow-up before the visible R2 release.

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

Implement EP1 then UX2 in [TASKS.md](../TASKS.md), retaining R1.5 and R3–R8 for broader coverage and their actual dependencies. Source validation, model work, UI and tests belong inside useful implementation PRs, not repeated planning gates. A concrete correctness/policy/licensing blocker applies to the affected claim or feature; it is not a blanket stop on independent catalogue work.

A representative midgame, post-Bowfa and advanced account must each be able to explain several useful choices and their next steps. Late-stage completion can correctly yield few or no meaningful upgrades; never pad the shortlist. Assess personal relevance, comparison trust, acquisition clarity and actual sidebar usability separately from test counts.
