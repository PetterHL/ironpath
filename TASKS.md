# IronPath tasks

Updated 13 October 2026, endgame intelligence and native homepage refinement. [Evidence and 30 relationships](docs/PROGRESSION_RESEARCH.md); `progression-1` supersedes earlier ranking/admission policy, not delivered history. [UX](docs/UX.md) selects the R3a card-first homepage and supersedes the earlier row-first/100px-tile layout. **This active R1 → R1.5 → R2–R8 backlog supersedes P4b/P5 and every older catalogue-size budget or sequential planning gate.** Historical briefs/results are preserved verbatim below and are not active instructions.

**14 October addition — Personalized Kill Impact:** [research/design](docs/KILL_IMPACT.md) selects passive recent-boss context inside IronPath, followed by validated same-method time estimates. KI1–KI3 below refine R1.5b/R4/Q5; they do not replace R3b, comprehensive coverage or the current UI work. Documentation only, no new delivered capability. The original research inspection used R3a #18 `d9e897b` before integration; the current baseline below includes the UX follow-up.

## A. Product vision

An established Ironman opens IronPath without selecting a goal and discovers worthwhile PvM upgrades, understands their account-specific value, sees meaningful supported numbers, and follows a clear acquisition path. Comprehensive maintained endgame/BiS and specialist coverage is the destination; implementation size is not a product limit. See [DESIGN](docs/DESIGN.md).

## B. Capabilities and coverage

Required: partial account observation; strong-alternative recognition; contextual ranking; full reference loadouts and local combat families; multi-step acquisition; native value-first UI; maintained sources and model coverage. [CATALOGUE](docs/CATALOGUE.md) inventories all relevant families, including current release candidates and future watchlist entries. D/R/V/Q are evidence/support states, not competing backlogs. [UX](docs/UX.md) gives concrete screens.

**Current implemented baseline:** main integrates R2 #16, progression design #17, R3a #18, Kill Impact design #19 and UX #20 on 14 October 2026. Plugin 0.8.3 / knowledge r3a-3 / schema 10 / preference schema 2. Kill Impact remains unimplemented design; R3b and later capability work remain open. The older R2-model #14 is superseded and preserved by its same-name tag. [Integration/cleanup record](docs/validation/main-integration-20261014.md). Earlier implementation/test records retain their original context; actual-client acceptance remains pending.

## C. Incremental implementation

Each assignment produces useful advice, reviewed knowledge, UI evidence and meaningful validation in the same PR. R1.5a, R2, R3a and the UX increment are integrated, with actual-client checks pending. Source review and ordinary PR review are part of implementation, not serial approval gates between every small slice. A contributor may split an assignment into reviewable PRs without reducing its acceptance criteria or redefining the destination.

| Milestone | Dependencies | Demonstrable result |
|---|---|---|
| R1 — Realistic magic and first endgame value | P4a + this reset | Full-loadout magic; Shadow/Ancestral early; numerical value influences advice; functional value/path views |
| R1.5a — Pursuit priority and visual foundation | R1; integrated, live checks pending | Explainable categories, shared activities, native compact cards and first conditional next-shard estimate |
| R1.5b — Effort expansion and first observation adapter | R1.5a; alongside R2, not its release gate | Multiple-roll estimates and one validated session-pace adapter |
| R2 — Ranged systems and major alternatives (integrated; live checks pending) | R1 shared contracts + R1.5a presentation/ranking + reference/metric follow-up | Bowfa, tbow and other ranged packages compared correctly; meaningful post-Bowfa advice |
| R3 — Relationship intelligence and melee | R2 contracts; small R3a vertical increment, then family work | Independent value/importance/effort, adequate role alternatives and useful melee comparisons |
| R4 — Specials, synergies and specialists | R3 relation contracts + relevant combat family | Burst/debuff/energy, multi-target/control and adverse interactions with explicit boundaries |
| KI1 → KI2 → KI3 — Personalized Kill Impact within R1.5b/R4/Q5 | R3a cards/comparisons; each later metric depends on its actual model and evidence | Actual recent-kill context → validated personal time sensitivity → selective spec/supply/method expansion; not a separate product or gate to R3b |
| R5 — Routes, preparation and permanent rewards | Extend alongside R3/R4; R1.5b remains independent | Complete remaining paths, optional detours, selective effort models and stateful utility |
| R6 — Comprehensive relationships and discovery | Incremental R3–R5 support; continuous coverage review | No unexplained role/alternative/activity gaps; developed accounts receive useful or honest no-gain advice |
| R7 — Sustainable maintenance | Source schema in R1; begin before catalogue scale grows | Development-time change/impact checks and repeatable model validation; no runtime service |
| R8 — Real-client usefulness and release readiness | Continuous UI checks from R1; broad R6 coverage for comprehensive claim | Evidence from representative accounts and native UI, accurate public coverage statement |

These are capability milestones, not upgrade-count targets. R6 remains open while material inventory gaps exist; new releases extend the inventory. R7 and real-client feedback run alongside feature delivery. Q1–Q5 layers in DESIGN describe calculation dependencies, not a requirement to finish all standard formulas before implementing a high-value megarare.

### R1 — Realistic full-loadout magic and first endgame value (implemented for review; live checks pending)

Implement a working endgame improvement on P4a, rather than another small set of disconnected cards.

- Extend immutable observations with equipment slots and supported container quantities/provenance; retain item-set consumers and all identity/IO protections. Add no persistent bank cache. A quantity remains unknown when unread.
- Extract typed `CombatSetup`, `TargetProfile`, per-metric `ComparisonResult` and a comparison service. Remove OCCULT/SWAMP and Dharok-only schema constraints as required. Remove arbitrary content-count ceilings while retaining justified resource/depth safeguards. Keep pure deterministic evaluation and original formulas.
- Support realistic magic reference gear: Ahrim/Virtus/Ancestral baselines as needed; occult, tormented bracelet, Magus ring, current supported boots/off-hands, prayers and boosts. Exact supported items/forms and mechanics must be reviewed. Do not quietly omit a damage-bearing slot or treat unsupported items as empty. Demonstrate two-handed weapon replacement removing an off-hand.
- Deliver **Tumeken's shadow and Ancestral pieces** as real upgrade outcomes with complete routes, owned-alternative checks and defer explanations, alongside the existing occult/swamp outcomes. Add tormented bracelet/Magus as useful reusable accessory comparisons. This named slice establishes endgame value; it is not a ceiling. Research current superior/specialist alternatives from CATALOGUE before ranking; recognise them even where their new model remains unsupported.
- Implement Shadow's current multipliers/caps and ToA distinction with independently checked rounding/interactions; support at least two reviewed non-identical target profiles and an explicit ToA scenario only if its scaling inputs are supported. No invented default invocation or raid level. If ToA scaling remains unsupported, label it and still ship checked non-ToA comparisons; keep the ToA gap active.
- Propose a complete compatible reference from equipped/observed gear and labelled assumptions. Provide focused correction, preserve input provenance and invalidate stale confirmations. A useful reference is available without a goal or compulsory multi-step configuration.
- Feed supported marginal changes and relevant alternatives into ranking. Separate relevance from access. Retain plan-ahead Shadow for appropriate accounts; Shadow ownership must stop routine swamp-grind advice. A small/no breakpoint accessory should not outrank a capability-changing option merely because it has more authored applications.
- Deliver the UX overview and Value/Path detail structure with native icons, before/after summary, supported metrics, assumptions disclosure, compact prerequisites and Back preserving list position. Move calculation construction out of rendering. Show Zaryte's existing route with truthful component quantity when observed, otherwise unknown; this exercises the shared observation improvement.
- Generalise source links to items/models/routes/contexts and allow reviewed official update evidence. Promote each shipped outcome only after its current facts, variants, routes and original explanations pass the catalogue contract. Preserve older useful content and source notices.

**Acceptance:** realistic post-Bowfa magic account sees justified nearer options and Shadow/Ancestral plan-ahead value; Shadow owner receives relevant armour/accessory advice rather than swamp; two complete loadouts produce reproducible supported comparisons; zero breakpoints and unknown gear are honest. Show synthetic before/after scenarios with inputs, sources, model versions and ranking explanation. Preserve P4a valid numeric/ownership/lifecycle cases, investigate reference disagreements, run the build and meaningful regressions. Render overview, numeric detail, unsupported detail and route at native width. Record pending real-client checks without pretending screenshots establish usefulness. Do not implement universal optimisation, all combat families or encounter simulation in R1.

### R1 implementation evidence — 13 October 2026

Based exactly on product-reset PR #8 (`7d31483133483f1a18d7bc34e3fd290064943907`), retaining its entire P4a/#7 → #6 → #5 ancestry and documents. New branch `r1-full-loadout-magic`; existing PRs are not merged, closed or changed.

Plugin **0.5.0**, knowledge **r1-1**, schema **6**: ten outcomes, twenty-five decisions, 151 recognised forms, 157 linked evidence records and 82 combat-item records. Counts describe this delivery, not catalogue limits. Shadow, individual Ancestral pieces, tormented bracelet and Magus have acquisition paths; prior occult/swamp/Zaryte/treads advice remains. Reference proposals compare supported owned staff/off-hand packages, all eleven slots, prayers/boosts and two distinct ordinary targets. Calculations run before rendering and feed deterministic value bands/order. Zero breakpoints and unknown inputs are explicit; Shadow ownership suppresses swamp acquisition.

Slot and container-count observations retain scope/time, stay in memory and clear with existing identity guards. Quantity checks use the largest observed container count rather than adding potentially duplicated stacks from different observation times. Magus/tormented recipes have typed ALL/ANY dependency graphs, actual skills/resources, owned-intermediate bypasses and cycle checks. Zaryte displays observed shards or unknown quantity. Source dependencies cover items, models, routes and contexts and accept reviewed official updates. Arbitrary content quotas are removed; parser/condition/path resource guards remain.

**Validation:** 124 automated tests pass on Temurin 11.0.32.1 / RuneLite 1.13.1; 540 new original full-loadout cases agree with pinned reference `89c3e25…`, alongside all 234 original P4a cases. The Sang proc disagreement was investigated against current Wiki pages and the implemented July Jagex update, then fixed with its correct successful-zero distribution. Six no-goal profiles and native-width Swing overview, Value, unsupported detail, ordinary path, branching recipe and Zaryte quantity views are recorded in [R1 validation](docs/validation/R1.md) and [synthetic demonstrations](docs/validation/r1-scenarios.md). `git diff --check` passes.

**Open boundaries:** no ToA phase/raid/path/party scaling, one-handed Confliction history, Brimstone accuracy proc, elemental/ancient spells, encounter times or trip model. ToA refuses numbers rather than supplying a default raid level; Confliction/Brimstone preserve supported max hit with accuracy/DPS gaps. Sang healing and Lightbearer utility remain qualitative. Native item sprites, actual client 100–200% scaling, bank refresh, player usefulness and all historical live checks remain **pending**. Screenshots use synthetic inputs and headless Swing; they do not establish game-client correctness or usefulness. R1.5 and R2–R8 remain open; no comprehensive product or Hub release is claimed.

### R1.5a — Pursuit priority and visual foundation (integrated; actual-client checks pending)

**Smallest useful increment:** reclassify R1's existing supported outcomes into defensible pursuits/activities/long-term/conditional choices and present them compactly, with an A1/A2 estimate for the next zenyte shard on the tormented route. Retain full-loadout comparisons and all existing routes. No new combat family, universal time simulator, mandatory goal, telemetry or account history store. This is one useful implementation milestone; internal commits may separate pure assessment and UI without creating another planning phase.

Deliverables:

1. Introduce the minimal immutable acquisition, pace, effort and pursuit contracts in DESIGN; extend bundled source/model dependencies and schema validation with a documented version bump. Reuse R1 route DAGs and comparisons. Add missing metadata to existing outcomes; an unsupported model yields a reason, not omitted value or invented hours.
2. Implement `pursuit-1` category admission, dominance restrictions, deterministic group order and activity-family deduplication. Remove the special role of absolute DPS as a universal priority. Effort is considered before strength within immediate-pursuit ordering. Unknown facts/effort cannot produce unconditional pursuit. Keep every relevant long-tail/specialist outcome discoverable; no content quotas or filled-card mandate.
3. Evaluate the **remaining** route. Owned zenyte/intermediates can expose assembly/skill work instead of a fresh shard grind. An owned horn cannot trigger another horn grind. Resource allocations and alternative routes remain exclusive choices. Group Ancestral CoX work as one activity with explicit useful families; recognised legacy prayer outcomes can supply distinct benefits only after their ownership/unlock relevance is evaluated. Do not invent new ranged/melee numeric models to populate the group.
4. Ship E1's reviewed fixed-probability **next shard** distribution in kills (mean/P50/P90) and optional user-entered eligible kills/hour. Default hours remain unavailable until pacing is supplied; no preset invented rate. Display conditional hours with scope/exclusions and resettable in-memory input. Use already-sourced path prerequisites/alternatives, refreshing facts where needed. Keep DT2, Nex, raids and challenges qualitative where acquisition support is incomplete. This is a credible first estimate, not a completed-bracelet ETA.
5. Implement UX's hybrid overview, two-column Browse with automatic single-column fallback, full-name accessibility, native ItemManager sprites, compact card action and separate activity detail. Remove repeated Open details bars and calculation paragraphs from overview. Retain Value/Path navigation, one consequential-unknown prompt, source/input disclosures and Back/scroll/identity guards. All relevant R1 outcomes remain reachable without truncation; basic All/Pursue/Long-term/Check categories ship now, full search/style/slot filters remain R6.
6. Record native-width component captures, explicit synthetic account comparisons and an actual-client visual check when a client is available. Existing pending observation/lifecycle checks remain visible. Do not mark the actual-client acceptance row passed from headless screenshots.

Acceptance criteria (all are meaningful behaviour, not getter/schema-shape tests):

- Same immutable inputs and versions produce the same categories/order/reason codes. Labels and Why this priority agree. Rearranging source records or adding encounter prose cannot change priority. Unknown effort never behaves as zero; known-invalid rates withhold odds/hours and dependent urgency.
- The six required profiles in the refinement matrix below produce the stated category/omission behaviour. Unsupported future combat items can exercise pure ranker fixtures as **synthetic evidence**, but cannot be promoted into runtime recommendations to make a test pass. End-to-end UI fixtures use validated R1/legacy content.
- E1 p=1/300 returns mean 300, P50 208, P90 690 future kills; user scenario 40/h gives 7.5 h mean, 5.2 h median, about 17.3 h P90. Test p=0/1, invalid/unknown pace, rounding and a dry-streak metamorphic case. These are not promises or real player measurements. Owned shard removes that drop stage; unknown crafting/onyx work prevents a total-item ETA. Adding component medians/P90s is forbidden.
- Unknown ownership, blocked access, skipped routes, zero gain, supported qualitative specialist value, duplicate reward families and competing zenyte/seed allocations behave defensibly. An unknown unlock cannot count as a confirmed missing shared reward. Owning a shared reward reduces its activity justification; no change manufactures a harder grind.
- At 225 logical pixels there is no horizontal scroll, clipped label or font reduction to force two columns. Card activation works by mouse and keyboard, Back restores selection/scroll, async sprites and delayed callbacks cannot revive stale identity views. Long item names and enlarged fonts trigger the specified fallback.
- Run the normal build and affected meaningful regressions, retaining R1's established numeric/ownership/isolation cases. Include before/after overview, Browse, Value, Path, unknown and empty screenshots with provenance. No new oracle corpus is needed solely for an unchanged combat formula.
- Actual RuneLite check: 100/125/150/200% client/OS scaling matrix, native sprites and async load, long-name wrapping, keyboard focus, detail/back, profile switch. Have a player identify a worthwhile pursuit, explain why Shadow can remain long-term, and find the next route step without diagnostics. Record findings and fixes. If unavailable, deliver implementation for review with this row **pending**, not a claimed polished-client pass; do not block unrelated R2 research/model work.

**Exit:** a reviewer can see better priorities and a usable compact overview on the existing magic slice, explain the estimate's scope, and implement ranged outcomes against stable contracts. Do not expand R1.5a into learning curves, complete raid reward distributions, complex route totals or full personal-performance tracking.

### R1.5a implementation evidence — 13 October 2026

Short-lived branch `r1.5a-pursuit-visual-foundation` starts at integrated main
`f495706`. Plugin **0.6.0**, knowledge **r1.5a-1**, schema **7**; ranking
**pursuit-1**, acquisition **zenyte-fixed-1**. R1's powered-magic-2 formulas and
all existing outcome/route IDs remain intact. Preference schema 2 is unchanged;
existing stable confirmations/skips/goals continue to load. Two explicit scroll
ownership keys extend the existing contract, with no bank/reference/pace persistence.

Implemented immutable acquisition/pace/estimate/pursuit/activity/presentation
contracts; remaining DAG evaluation with owned-intermediate bypass and exclusive
resource choices; category admission, conservative dominance, stable context
buckets, effort-before-strength ordering and deduplicated activity families.
CoX prayer benefits require confirmed missing unlock and scroll ownership; unknown
unlocks never become votes. A single ordinary reward is not labelled long-term
merely because it has no shared reward. Owned horn excludes fresh horn work;
quantities/assembly remain separate. Known-invalid E1 support withdraws its
odds/hours and dependent pursuit urgency while preserving value/path evidence.

Native compact pursuit rows, representative activity sprites, adaptive Browse,
All/Pursue/Long-term/Check, Value/Path and one consequential prompt are delivered.
Pacing accepts an optional positive number/range, resets in memory, and retains
existing identity/revision guards. No new combat family, raid rate model, observation
adapter, runtime service or publication is included.

Automated totals and the exact build, six synthetic account comparisons,
source/schema contract, before/after native component captures and limitations are
in [R1.5a validation](docs/validation/R1.5a.md). The full normal suite retains
R1 numeric/ownership/lifecycle checks; no new external oracle corpus was needed.
Headless captures use native Swing with fallback sprite wells; they do not prove
real RuneLite image loading, DPI/scaling or player usefulness. **The actual-client
100/125/150/200% and player-usefulness acceptance row remains pending**, with the
focused PC checklist in that report. All earlier pending real-client checks and
historical TASKS/RESEARCH records are preserved.

**First PC screenshots:** native sprites and full names in the two-column Check
Browse view are observed. Shadow detail clipping and Ancient blessing (20235)
blocking magic comparison were found and corrected on this branch. The follow-up
suite passes 155 tests; exact-item evidence and regression details are in the
validation report. Corrected detail/comparison client retest and the broader
scaling/usefulness acceptance checks remain pending.

### Reference baseline and metric presentation — integrated; client checks pending

**User direction, 13 October 2026:** replace the automatically selected Graardor/Dharok reference with a neutral enemy with 0 Defence, based on the POH combat-dummy comparison idea. The product label is **Enemy with 0 Defence**; do not require or advertise a POH/dummy location. This is an analytical reference, not a measurement of player performance. Deliver this focused follow-up after R1.5a, alongside R1.5b if useful, and before R2's user-facing release. R2 research/models and the optional observation adapter do not depend on it.

- Review and pin the exact target inputs: Defence level, magic-defence level/bonuses, other defensive bonuses and any size or family-specific inputs. Zero Defence alone does not establish zero magic defence, guaranteed accuracy or dummy-specific guaranteed-max-hit behaviour. Use ordinary attack distributions and supported hit-chance rules; do not calculate DPS as max hit divided by attack interval. Label assumptions and keep source/model versions. Do not invent target HP, raid scaling or special target effects.
- Make this neutral profile the automatic comparison/proposal default for supported families and use the same declared baseline for before/after weapon packages. Remove automatic Graardor/Dharok selection and boss names from default overview/Browse/value summaries. Retain reviewed boss profiles as explicit optional contexts for specialist checks; a neutral comparison cannot establish universal boss usefulness. Context-sensitive families such as tbow/Scythe/Fang require their own reviewed inputs and contextual examples.
- **Weapons:** lead overview rows, Browse cards and Value detail with supported theoretical DPS before → after and absolute delta; attack interval/speed and hit chance are visible in detail/assumptions, with max hit secondary. Account for compatible off-hand replacement and strongest supported owned alternatives. If DPS is unsupported, show the model gap; supported max hit may remain explicitly secondary and must not masquerade as a DPS gain.
- **Armour, jewellery and other equipment that leaves attack interval unchanged:** max hit may remain the primary compact metric, with accuracy and DPS available in Value. For any equipment/set effect that changes attack interval, use the DPS-first rule. Preserve zero-breakpoint and accuracy-only value, qualitative utility and uncertainty. This display preference does not override effort/category ranking or permit cross-context DPS sorting.
- Validate ordinary 0-Defence target rolls and expected damage against hand checks and a pinned reference, including successful zero hits, different attack speeds, equal-max-hit weapons with different DPS, complete staff/off-hand packages, unchanged-speed accessories and unsupported history mechanics. Preserve R1 boss/oracle cases as explicit-context regressions. Capture overview/Browse/Value at native width and retest the default label/metric in the client; do not rewrite historical screenshots as new behaviour.

**Acceptance:** no goal/setup wizard is needed to obtain the neutral reference; the default card no longer says Graardor reference. A slower weapon's larger max hit cannot imply higher DPS. Weapon details lead with the supported DPS result or an honest gap, while ordinary armour/jewellery retain max-hit summaries. Exact target assumptions remain inspectable and specialist usefulness stays contextual. Runtime implementation is delivered on `reference-neutral-dps`, based on PR #12 at `dbbd515`. Plugin 0.6.1, knowledge reference-1, catalogue schema 8; combat powered-magic-2, pursuit-1 and preference schema 2 remain. The neutral target is an authored assumption with Defence/Magic/magic-defence bonus all zero and ordinary magic defence roll 576; it is not an NPC stat claim. The analytical target does not count as another useful encounter in ranking breadth. New metric rows allow taller Browse value lines without reducing fonts. [Validation, independent reference checks, synthetic profiles and fresh component captures](docs/validation/neutral-reference.md). Actual-client acceptance remains pending.

### R1.5b — Effort expansion and local pacing (alongside R2; not implemented)

Extend the same estimator to reviewed multiple independent rolls using Zulrah/magic fang E2; distinguish at-least-one probability from expected item count and show shared reward relevance without additive gains. **14 October sequencing refinement:** use KI1's selected encounter adapter as the first shared passive observation work instead of implementing an independent CoX collector first. Vorkath is a possible pilot, not a user-mandated target; select by usefulness, observability and mechanic reuse. It initially supplies scoped combat durations, not farming throughput. CoX completion/points/timing remains subsequent adapter coverage, with raid probability modelling independently in R5. Reuse the same completion/failure/censoring and identity contracts. If full-cycle detection cannot be validated, retain manual cycle pacing; never convert recent combat duration directly into acquisition hours.

**Acceptance:** event replay covers start/finish, duplicate messages/chest events, mid-run enable, missing events, failure/abandonment, disconnect, hop and character/profile changes. Confirm client-version compatibility. Keep completed, failed and censored counts separate; no PB-to-average substitution. Apply the proposed minimum sample/scope rules in RESEARCH, label them as calibration, and compare against a manually timed client session before automatically selecting observed pace. No persisted raw events, telemetry, other-plugin private storage or gameplay guidance. Record policy assessment for the exact adapter; do not claim approval from the existence of a core plugin. Source failures withhold the affected estimate, not R2 delivery.

### R2 — Ranged packages and post-Bowfa choices (implemented increment; review/live checks pending)

Use R1.5a categories and card/detail contracts, plus the neutral-reference and metric presentation follow-up above; R2 mechanic research and pure models may start alongside R1.5a, but its user-facing release must use the revised priority model. R1.5b observation is optional. Implement shared ranged rolls, ammo/defence-type rules and reusable crystal/void/slayer/target effects, then Bowfa/crystal, twisted bow, blowpipe/rosewood alternatives, conventional crossbows and relevant ranged armour/jewellery. Include current Masori, quiver, anguish/rupture and ammunition candidates; verify Seeker restrictions/floor effects and revised gear rules. Promote supported bridging items from existing activity-only advice into outcomes when useful. Recognise two-handed/ammo/slot compatibility and upkeep. Review high-HP versus low-magic/low-defence contexts rather than treating tbow as universal.

**Acceptance:** Bowfa+crystal does not receive Masori as a Bowfa damage upgrade; owned tbow does not erase useful Bowfa/blowpipe/spec niches; current superior jewellery/bolts/arrows affect baselines correctly; set replacement and ammunition assumptions are visible. Differential and hand tests cover scaling caps, breakpoints and invalid ammo. At least post-Bowfa and tbow-owned fixtures show changed priorities for documented reasons. Ship complete paths and native details for every new outcome. Twisted bow remains discoverable without automatically outranking supported nearer improvements; Bowfa seed/shard and shared CoX effort use reviewed qualitative fallbacks until acquisition models are validated.

### R2 implementation evidence — 13 October 2026

Focused branch `r2-ranged-packages` starts at updated main `cb7c2cd`, preserving R1.5a/reference/gap fixes. Plugin **0.7.0**, knowledge **r2-1**, schema **9**, ranged **ranged-1**. Seventeen additional outcomes, 69 independently reviewed ranged stat records and complete compatible proposals extend the retained magic/priority/route foundation; these are delivery counts, never coverage ceilings. Bowfa/crystal, tbow, toxic/rosewood, ordinary crossbow/MSB(i) baselines, Masori, Void/task, quiver, anguish/rupture, Venator and ranged accessories/ammo have explicit support and gaps.

Neutral ranged inputs are authored separately; ordinary Graardor and Zilyana exercise defence type and high-Magic trade-offs. Tbow can remain Long-term on positive specialist evidence while losing neutral DPS; different contexts cannot form a universal numeric tie. Masori loses crystal synergy for Bowfa and is deferred when that substitution loses value. Strong owned armour/jewellery/ammo, two-handed removal, internal darts, rune-only Rosewood and bow-only seeking floor/accuracy all affect comparisons. Magic/ranged corrections are independent, session-only, invalidated by consequential observations and cleared with existing identity guards.

Remaining paths have self/NPC singing alternatives, true component quantities and owned-stage bypasses. Inactive outcomes suppress duplicate acquisition without establishing usability. Toxic's current 78-Fletching/chisel recipe, necklace mould/92 Crafting, rupture etching/access, Venator icons/resources and assembler head/guarantee scope were checked. No new raid/challenge probability, hours, kill-time or trip estimate is invented. Source dependencies and schema validation cover the new contracts; preference schema/IDs and privacy remain.

**Verification:** the normal Temurin 11.0.32.1 / RuneLite 1.13.1 `test build --no-daemon` passes. The final run contains **186 tests, zero failures/errors/skips**, with **1,944 original complete ranged cases** checked against pinned development-only reference `89c3e25…` plus independent caps/rounding/set/ammo/distribution hand cases. Earlier magic/ownership/lifecycle regressions remain. Combined no-goal profiles and six fresh 225-pixel Swing captures, including an unknown-ammo gap, are recorded in [R2 validation](docs/validation/R2.md) and [synthetic profiles](docs/validation/r2-scenarios.md). Source/code/data rights remain separate; no GPL code/dataset was imported. `git diff --check` passes.

**Review corrections, 13 October 2026:** plugin **0.7.1** / knowledge **r2-2** in the same R2 PR. Armour/accessory comparison reconsiders supported observed owned weapon packages, retains the candidate and combat assumptions, and validates alternative ammo/off-hands. Charged/blessed quiver selection includes projectile-specific bonuses. Owned uncut/cut zenyte removes fresh gorilla work and votes for anguish while retaining downstream skill/resource checks. Rupture explicitly requires 84 Crafting for etching and final combination, including owned etched fangs. Nine new regressions plus the retained suite pass: **195 tests, zero failures/errors/skips**, including all **1,944** ranged oracle cases. See [review evidence](docs/validation/R2.md#review-corrections--13-october-2026); original delivery evidence above is preserved.

**Open boundaries:** real sprites/async loading, client/OS scaling, in-game quest/bank/profile checks and player usefulness remain **pending**. Raids/scaling, enchanted bolts/specials, bounce and additional variants/armour/off-hands remain visible in CATALOGUE and R3–R8. No comprehensive coverage, real-client pass, publication or Hub approval is claimed.

### R3 — Relationship-aware advice and melee packages

**R3a implemented for review below; R3b remains unimplemented.** Implement DESIGN `progression-1` against the inspected R2 contracts. Preserve `pursuit-1` history and current numerical regressions; migrate behaviour explicitly rather than replacing the plugin. [Research R01–R30](docs/PROGRESSION_RESEARCH.md) supplies reviewed examples, not a hardcoded list of mandatory grinds.

**R3a — Small vertical relationship increment, including useful advice:**

- Add typed activity contexts/roles, independent value evidence, scoped owned alternatives and preparation options using the existing catalogue/predicate machinery. Distinguish hard access, equip, method capability and optional preparation. A utility context need not have a numeric target. Extend generic reward eligibility and remove the Doom-only `minDelve` schema dependency while retaining existing outcome/route IDs.
- Separate personal importance from effort/status. Migrate the existing core ranged-neck, Zaryte specialist and treads preparation explanations; include Venator's researched convenience/preparation role as qualitative supported advice once its route/variants pass normal validation. Numbers must not be invented to complete this increment. Keep exact Venator geometry/breakpoints visibly unsupported until checked.
- Correct observed R2 ranking bottlenecks: ordinary DPS does not overwrite independent utility; raw context count is not breadth; analytical reference has no breadth weight; coordination/upkeep need real evidence instead of HIGH-burden/zero placeholders; no item-name priority exemption. Use importance before burden within actionable choices, retaining comparable numerical buckets and honest long-term status.
- Give activities independent access/method assessments and per-reward eligibility. Two rewards alone do not establish priority, and a reward-specific unknown must not block every known useful reward. Show one-hop indirect explanations with direct-value anchors and cycle guards; do not build recursive scores.
- Deliver the [UX card-first homepage](docs/UX.md): sprite/name header, full-width account-value reason, supported contextual primary metric, concise status and direct Value/Path actions. Use one column at normal native width; two only when measured width/font permits the complete content. Reuse the card in Browse, introduce Specialist discovery alongside existing categories, and retain the selected neutral reference in Value. Personal importance, adequate owned alternatives and defer reasons come from evaluated evidence, not Swing text rules. This increment must visibly improve advice and its first impression, not ship unused schema alone.

**R3a acceptance:** profiles PI01–PI05, PI08–PI12 and PI14–PI16 below demonstrate appropriate conditional/qualitative outputs; all existing active R2 outcomes remain reachable. No goal is required. Supported DPS and lifecycle regressions remain intact; a negative ordinary metric cannot erase a validated spec/QoL benefit. Explanation reason codes identify the actual triggering facts and alternatives. Route cycle checks reject hard cycles while ordinary utility feedback terminates safely. Repeat IDs/context aliases do not improve rank. Where complete profile expectations require a later model, retain explicit pending support rather than claiming a full pass.

**R3a native UI acceptance (component implementation verified; actual-client acceptance pending):**

- [ ] First impression: rich no-goal fixture at 225×600 logical pixels shows real item identities, account-specific reasons and more than one relevant option before scrolling. Target three complete compact cards, or two plus a visible next card when qualifiers wrap; document measured heights. Preview length follows complete-card height/viewport budget, not a hard upgrade count. Sparse/large-font states never invent filler or hide content to pass density. Browse reaches every eligible outcome without a catalogue ceiling.
- [ ] Card contract: genuine ItemManager sprites in reserved wells; full wrapping names; reason across full body width; one supported primary numeric delta with unambiguous context where available; precise status/unknown label; explicit Value and Path actions. No generic “Huge upgrade,” fabricated metrics or full-width Open details bars. Screenshot missing/loading sprites separately from real sprite success.
- [ ] Deterministic discovery: highest justified actionable option remains first when present; distinct important long-term/specialist value appears before repeated role/family previews, with no filler quota or item-name exception. All-megarare, long-term-only and nearly complete accounts yield justified options or an honest no-material-gain state. Independent rewards sharing an activity are not all suppressed; correlated pieces do not inflate priority. Reordered/duplicate evidence does not change rank. Long-term and Specialist links near the header give one-action filtered discovery even when preview candidates lie below the fold.
- [ ] Account counterfactuals: own an adequate alternative, complete an unlock/intermediate, correct the reference or skip a route; verify the card reason/status/order changes consistently and alternate routes/owned utility survive. Unknown ownership is conditional, not missing. No goal/configuration gate. Pure presentation reflow or sprite arrival cannot alter ranking.
- [ ] Numbers remain first-class: ordinary weapon/weapon-switch card shows supported theoretical DPS delta; unchanged-speed accessory may show max-hit delta. Trace each displayed number to its exact comparison/target. Specialist utility survives a negative neutral result; Value retains that result and explicit encounter trade-offs. Unsupported DPS never becomes zero or disappears behind an unexplained label. Preserve existing numeric regressions without a new oracle corpus for unchanged formulas.
- [ ] Native layout/accessibility: one column at ordinary 213px inner width; two only at at least 170px per cell plus actual content/font fit. Exercise both sides of the width threshold in the component harness and the host's real available width. Long names, enlarged fonts, zero/negative metrics and checks wrap without clipping, ellipsis-only meaning, horizontal scroll or font reduction. No detached/wider-window dependency. Inspect contrast, focus visibility, textual status, accessible item/action names and row-major keyboard order.
- [ ] Navigation and everyday use: Value/body opens Value; Path opens directly at the remaining step/check; child clicks do not trigger both actions. Mouse and Enter/Space work. Back restores home/Browse filters, card, focus and scroll. Updates preserve an ID anchor; profile switch clears prior account content, focus anchors and delayed actions. Late sprites/callbacks cannot populate another account's card. Controls/optional goals remain secondary; consequential uncertainty stays visible on advice.
- [ ] Evidence: run normal build and meaningful affected ranker/panel/lifecycle regressions. Capture matching before/after fixture states at 225×600 and enlarged font, plus Value, Path, Specialist, long-term-only, partial/empty and delayed-image states. Label synthetic data and fallbacks. Record actual RuneLite/OS versions and viewport at 100/125/150/200% scaling with real ItemManager images, async loading, focus/Back and profile switching. Have a player find a worthwhile upgrade, explain the benefit/trade-off and reach its next step without diagnostics. If the client/player is unavailable, record those rows **pending**; do not claim a polished-client pass from component captures.

**Codex implementation handoff:** recheck #17/#16/main heads and integration state before choosing the R3a code branch; this documentation review does not authorise merging them. Implement against the accepted `progression-1`/R2 contracts, keeping existing observation and quantitative services. Extend `PursuitOverview` (or a small immutable successor) for explained preview ordering; adapt `IronpathPanel.upgradeCard`, overview/Browse navigation and `BrowseGrid` sizing. Remove the all-body beside-sprite layout and repeated text padding where they cause excess height; preserve useful navigation/identity guards. Present explicit card actions with separate guarded handlers. Add the small Specialist filter from reviewed role data now; leave broader R6 filters in R6. Do not rescope this into a new product reset, combat-model rewrite, frontend framework or broad research assignment. Deliver screenshots, changed behaviour, meaningful test results and honest pending client checks in the implementation PR. No application work is delivered by this documentation update.

### R3a implementation evidence — 13 October 2026

Separate implementation branch `r3a-personalized-cards` from PR #17 `75897d56d99e90a79b05931bbecddbc3c410212f`, retaining corrected R2 `32cb24a`. The implementation PR targets `docs/endgame-progression-intelligence`; #17 stays documentation-only. No PR is merged.

Plugin **0.8.0**, knowledge **r3a-1**, bundled schema **10**, ranker **progression-1**. Direct personal importance, scoped reviewed roles/alternatives, method versus optional preparation, independent reward eligibility and guarded one-hop explanations are evaluated outside Swing. Core-neck, ruby-special, hybrid-treads and Venator relationships change real advice. Venator's charged/uncharged ownership and five-shard path are explicit; observed quantities and owned stages shorten only their own work. General convenience stays optional without an inferred activity preference. An explicit single-combat scenario removes only the relevant bounce benefit. Source claims, versions, exclusions and allocation conflicts remain inspectable.

Native homepage/Browse reuse full-width reason cards with ItemManager integration, wrapping names, contextual deltas/gaps, status and separate Value/Path actions. Importance-based discovery preserves the first justified actionable choice, then important long-term/specialist representatives before repeats. A measured two-viewport budget selects complete cards; all eligible outcomes remain in Browse. Normal 213px inner width uses one column; two require at least 170px per cell plus content/font fit. Back retains filters/focus/scroll anchors; live updates anchor to stable IDs; profile clear and stale actions/images are guarded.

The normal Temurin 11.0.32.1 / Gradle 8.10 / RuneLite 1.13.1 `test build --no-daemon` passes **219 tests, zero failures/errors/skips**, retaining all 195 baseline cases and the unchanged **1,944** complete ranged oracle comparisons. Ordinary combat formula/fixture files are unchanged. Existing presentation/policy tests explicitly migrate the superseded row/grid/burden expectations; historical R1 fixture scopes exclude the new R3a outcome. Original historical reports remain intact. Native 225×600 captures show two complete cards plus the next card; measured normal heights and enlarged-font/Value/Path/partial/all-owned/loading states are recorded in [R3a validation](docs/validation/R3a.md). Synthetic fixtures cover the supported portions of PI01–PI05, PI08–PI12 and PI14–PI16; complete melee/debuff, geometry, treads additions, penny/pouch and preference expectations remain explicitly pending.

**Still pending:** authenticated-client asynchronous loading, OS/client 100/125/150/200% scaling, actual focus/navigation/profile checks and player usefulness. The original component captures use fallback glyphs. Petter subsequently supplied real-client screenshots with visible item sprites, exposing crystal/Blazing recognition gaps and missing ranged stats for max treads. Plugin **0.8.1** / knowledge **r3a-2** adds the reviewed forms, separates active/empty evidence and makes Rosewood optional when a stronger blowpipe is observed; deferred choices remain in Browse/Specialist without filling the homepage. [Client findings, scoped source review and regressions](docs/validation/R3a-client-variants.md) preserve the original 219-test evidence above. A post-fix client retest is pending. Exact Venator geometry, specials, raid odds/hours, full encounter/trip performance and broader model/relationship coverage remain unsupported. No publication, comprehensive-coverage claim or Hub approval.

**Client follow-up, 14 October 2026:** plugin **0.8.2** / knowledge **r3a-3** supports Lightbearer’s reviewed zero ordinary ranged bonuses and connects R3 specialist predicates to the supported R2 weapon set, including Toxic/Blazing. Petter confirmed the repeated crystal/blowpipe cards were gone and requested Rosewood removal. Its acquisition outcome/decision are now inactive in every advice view; owned-item stats, stable keys and original numeric regressions remain. Possible Doom special use remains research. An empty Specialist filter explains its scope. [Validation and PC retest](docs/validation/R3a-lightbearer.md) supersede the optional-Rosewood recommendation in the previous correction; post-fix live verification remains pending.

**R3b — Melee family increments on the same contracts:** implement ordinary melee styles and complete one-/two-handed packages, target/task/salve/demon/dragon conditions, then Fang, Scythe and Soulreaper family modules. Ship useful melee comparisons as each family passes review; do not wait for all specialists or exact acquisition hours. Include lance/owned alternatives and sustain trade-offs, Torva, current Inquisitor/mace, Oathplate, Ultor/Bellator, rancour/torture, defender/gloves/boots and meaningful crush/stab/slash bridges. Retain the uncapped inventory.

**R3b acceptance:** Fang stab versus slash and inside/outside ToA; Scythe 1×1/2×2/3×3; Soulreaper start/ramp/retained/lost stacks; defence-reduced versus unreduced Olm and crush/slash alternatives; salve/task exclusivity; incompatible off-hand; and blood-fury sustain independent of lower DPS. No universal Scythe or Fang ordering. Current July Inquisitor changes invalidate old set assumptions. PI01, PI06, PI09, PI10, PI13 and PI14 gain numeric assertions only for supported complete contexts.

### R4 — Specials, synergies and encounter specialists

Build on R3's role evidence and the relevant combat family; work can proceed on independent families without waiting for every R3b item. Model special burst, ruby/diamond procs, defence reduction and explicit energy budgets. Prioritise Zaryte, DWH/BGS/elder maul, Lightbearer/Death Charge, claws/burning claws, voidwaker, tonalztics and current relevant specs such as Crimson kisten/Soulflame. Preserve July changes, target HP/caps/immunities, phase/team resets, role allocation and competing energy use.

Add reviewed Venator arrangements and Colosseum switch breakpoints after the August geometry review; demonbane damage/control, Doom roles and negative thrall interaction; wilderness/dragon/undead/elemental/raid specialists using shared mechanics. Qualitative support can precede numbers and is never permanently exempt from relevant numerical work. Do not import a guide's maxed setup as a minimum.

**Acceptance:** PI02–PI04, PI07, PI09, PI10 and PI13. Tbow owner still sees a justified Zaryte role; successful ruby proc and unconditional hit are not confused; lower target HP or an immunity changes its value. Lightbearer only gains extra specs in a supported cycle. A team already covering reduction lowers redundant acquisition importance without pretending the local player owns their weapons. Venator's single-target result does not measure bounce/convenience; invalid geometry withholds only dependent claims. Scorching/adequate switches can support a Doom route without tbow/Zaryte/Scythe. Thralls' extra acid is an adverse interaction, not automatic net benefit. No numerical kills/trip or phase-skip promise from ordinary DPS.

Investigate Q5 only for a concrete useful scenario after reusable attack distributions exist. Resolve relevant current Hub policy questions before shipping encounter-simulation-like features. This uncertainty does not block ordinary Q1–Q4 or qualitative role advice.

### Personalized Kill Impact — KI1–KI3

**Selected, unimplemented.** [KILL_IMPACT](docs/KILL_IMPACT.md) defines observation/model/UX contracts and the source register. Keep TASKS as the sole backlog. This adds a bounded context to the existing advisor, not a new plugin, dashboard, universal simulator or research reset.

**Coverage clarification, 14 October:** the target is most relevant PvM bosses, explicitly including raid bosses/rooms in CoX/CM, ToA and ToB/hard mode. Whole-raid impact is a separate scope to evaluate and deliver where supportable. Vorkath was an example, not a required first boss or a catalogue boundary. Use the existing uncapped encounter inventory and track observation, contextual offence, personal time and whole-activity support independently. A pilot cannot establish broad coverage.

**Priority and dependency decision:** complete the in-flight R3a review/client corrections and compact UI work. KI1 can then be a focused visible increment alongside R3b, reusing R1.5b observation contracts. Prioritise reusable Salve/dragon/undead interactions and common bolt effects within R3b/R4 because they unlock ordinary Vorkath setups as well as wider advice. KI2 needs distributions, supported actual inputs and a validated encounter adapter; it does not wait for every R4 specialist. KI3 follows evidence of usefulness. Neither observation nor personal seconds blocks ordinary quantitative/qualitative coverage. Recheck current branch heads before implementation; no merge is authorised here.

**KI1 — Actual kill context and tangible upgrade comparisons**

- Implement bounded, session-only capture for an evidence-selected first boss/method, using validated public event mappings and immutable encounter summaries. Normal post-quest solo Vorkath is a candidate, not a mandated target. Keep optional raid/run, room/encounter, phase/target, mode/scaling and party/role fields in shared contracts; no Vorkath-only schema. Record actual equipment segments, current levels/prayers/style/ammo evidence and independently scoped reported/observed duration. Keep completed, failed/abandoned, censored and excluded observations distinct. Preserve current conservative loading/profile resets; no silent cross-session history.
- Reuse candidate eligibility, compatible packages, strongest-owned alternatives, importance/status/effort and Value/Path. A recent-kill baseline must never be the automatically optimised reference proposal. Label owned alternatives and recurring ammunition separately from new acquisition.
- Deliver a quiet Recent boss entry and compact contextual cards after completion. Show supported ordinary-phase DPS/accuracy/max-hit comparisons and precise remaining gaps; no personal-seconds headline yet. Qualitative advice survives unknown ammo/spec mechanics, but unknowns cannot become zero bonuses. No forced tab/focus changes, modal, chat spam or live coaching.
- Supply reviewed target/method facts and complete relevant interactions for the initial supported packages; if selecting Vorkath, include Salve/dragon/undead/slot interactions. Identify exact excluded setups and record how often ordinary player setups hit those gaps; an adapter that only shows unsupported messages is not a successful vertical increment.

**KI1 acceptance:** shared-contract fixtures cover ordinary solo, multi-phase and raid-room contexts without boss-specific shared schema, including unknown party/scaling and precise output scope; these fixtures do not claim live raid support. Event replay and actual-client evidence for start/end, duplicate/late messages, death/teleport, mid-kill enable, missing state, transformations/index reuse, delayed hits, switch/food ambiguity, overflow and identity changes; no false completion or account leakage. Matched timer origins agree within one tick or have an investigated reason. Demonstrate worn-versus-owned-baseline differences, a genuine supported upgrade, owned alternative, recurring ammo and honest no-gain/unsupported states at native width. Report eligible-capture and numeric-coverage fractions and player comprehension, not just test counts. Keep full-cycle pace and performance persistence unavailable. If actual-client access is absent, leave those gates explicitly pending.

**KI2 — Validated personal seconds for comparable kills**

- Add independently authored reusable damage distributions/finite-HP expectations, with first-impact/overkill/miss/timing semantics. Keep the existing max-hit/DPS regression corpus unchanged unless a concrete formula correction is justified. Use the correct pinned reference path; its scalar TTK is not an oracle for every HP-dependent effect.
- Start with same-cadence damage/accuracy substitutions and stable observed methods. Pool cadence/gap evidence from comparable complete kills; do not fit a universal player-efficiency factor or scale the last kill by a DPS ratio. Match real level/prayer/ammo/charge state and preserve conservative cohort resets.
- Make multi-trip calibration usable: add a tested character-scoped in-memory completed-summary store across recognised ordinary bank/region transitions. Suspend until same identity/profile/world is revalidated; clear on ambiguity, logout/disconnect, profile or unsupported-world changes. Active trace and existing bank/reference state still invalidate immediately, and old-generation callbacks remain rejected. Do not require ten kills in a single trip or silently add cross-login persistence. Cohort matching uses validated model-relevant regimes, not exact tick-for-tick potion histories.
- Add bounded analytical phase treatment for the selected encounters and paired baseline/candidate sensitivity, including possible phase-count changes; Vorkath supplies a worked example. Ordinary DPS or combat-only time must not be presented as full kill time. Withhold seconds when material phase/proc/spec/supply effects are unsupported; display the supported offence result. Common ruby/diamond and spec coverage comes from shared R4 modules, never local fixed percentages.
- Show an estimated average delta with a correctly named sensitivity/uncertainty range, sample count and same-method condition. Proposed calibration: no personal headline before 10 matching kills, recent window up to 30, plus coverage/spread checks. These thresholds are not proof. Document observed duration separately from estimated savings. No additive upgrade deltas, seconds/hour ranking or time-to-acquire inference.

**KI2 acceptance:** demonstrate a second materially different boss/method using shared comparison/impact contracts and a small reviewed adapter, with source/model/client validation for its advertised metrics; the first boss is not the delivered architecture ceiling. Mathematical and differential fixtures; held-out baseline prediction **and** matched actual-owned before/after setup comparisons, following KILL_IMPACT's predeclared error/uncertainty gates. Tiny gains below validation resolution stay provisional. Verify phase boundaries, zero/negative results, unsupported-state refusal and source-version invalidation. Publish bias/error, all exclusions, sample counts and the fraction of normal pilot kills eligible for numbers. If most ordinary kills in an advertised context are excluded, identify/fund the specific next model or retain the honest KI1 scope; do not claim personal time estimates delivered. Resolve concrete policy-boundary questions for the exact analytical feature before public distribution; no blanket delay to ordinary advisor work.

**KI3 — Selective specials, supply and method expansion**

Use the shared R4 models for spec energy/regen/debuff/reset/HP-dependent bolts and compatible weapon/method changes. Add hard food/combo food/brew/restore effects only where corroborated observations and a validated counterfactual healing/stat policy support them. Avoid double-counted delay in mechanic windows; keep brew suitability encounter-specific. Treat charged/blessed quiver, internal darts, ammo saving and recurring supply costs independently of permanent unlocks. Defensive/sustain value stays qualitative until incoming-damage/resource modelling supports numbers. Full-cycle acquisition pacing remains separate R1.5b/R5 work.

**KI3 acceptance:** repeated-kill cohorts do not transfer blindly across speed/range/method/spec changes; spec opportunity depends on a stated cycle; combo healing is not labelled delay-free; restored levels are distinguished from restored boosts; unknown charges/HP/reductions withhold affected metrics. Add bosses/methods one validated adapter at a time toward broad coverage. Deliver explicit raid-room/boss and team-content increments using reviewed mode/scaling/role and shared-reduction evidence; do not wait for every solo boss or total-raid timing before useful raid-boss advice. Whole-raid totals require non-overlapping scope, compatible route/baseline, dependencies and overhead; never scale total team/raid time by local DPS or sum independent quantiles. Persistent-history/trip claims require their own explicit evidence and privacy scope. Keep no-telemetry/local evaluation and the primary upgrade advisor intact.

### R5 — Complete routes, permanent rewards and selective effort models

Paths and honest A0 effort ship with each outcome, not at the end of R5. Extend the existing estimator selectively: current CoX/ToA/ToB personal allocation, Nex contribution/team rules, stateful drops and multi-component processes only with complete mechanics/input review. Preserve R1.5b's bounded multiple-roll/pace work; it is not a gate to relationships or melee. No universal simulator, fixed hours from gear, or payback-time requirement for an upgrade.

Represent preparation **alternatives**, useful independently valuable detours, and “start this reviewed method now” alongside optional improvements. Expand acquisition/equip/unlock/active/supply states for prayers, quiver/infernal/MA2 rewards, diaries/CAs/imbues, penny, pouches and relevant travel/resupply. Add optional attention, method/team and activity preferences only when they change useful decisions; follow existing local persistence/migration rules. No questionnaire, silent performance-history storage, remote telemetry or teammates inferred from account gear.

**Acceptance:** PI01–PI05, PI08, PI11–PI16. Owned horn/zenyte/shards/boots remove only completed work; one synapse or essence stock cannot fund mutually exclusive outcomes. Treads additions are optional and independent; reward delve eligibility differs from quest access and confidence in reaching the delve. Prayer level is not consumed-scroll state. Penny missing from the bank after consumption does not create a new acquisition recommendation; tier loss affects activation, not unlock history. Divine pouch utility needs a real four-rune scenario. Long-tail status does not erase high importance or an explicit interest. Unknown learning/pace blocks the relevant estimate, not all known value.

For supported quantitative routes, distinguish next component from whole item, conditional unique weights from per-player chance, mean from quantiles, and shared/overlapping work from independent sums. A rate change withdraws affected estimates only. Current CoX weights are 60/56 totals, not the historical 69. Any persisted performance-summary proposal remains explicit work here, with its own privacy/migration design.

### UX navigation follow-up — 14 October 2026

Plugin **0.8.3**, knowledge **r3a-3**, schema **10**, preference schema **2**. Focused implementation on `ux-card-navigation`, based on PR #18 `d9e897b`; target `r3a-personalized-cards`. No dependencies merged. Browse gains item-name search combined with existing categories, Clear filters, accurate counts and a recoverable no-match state. Search updates only the result components, preserving its live text editor. Independent Overview/Browse anchors and stable-ID Back handle intervening account updates; lifecycle clearing drops search and all anchors. Replaced result cards and delayed imagery cannot activate the old result set.

This delivers the first search portion of R6 alongside R3a navigation acceptance. Initial delivery `b18351c` passed 238 tests. Review corrections preserve search caret/selection across full observation refreshes and reject deferred scroll restoration from an earlier result generation; unfocused labels cannot trigger caret-driven scrolling. JDK 11 / Gradle 8.10 / RuneLite 1.13.1 `test build --no-daemon` passes **240 tests, zero failures/errors/skips** (232-test baseline, six original and two review panel regressions; all 1,944 original ranged oracle cases retained). Broader filters, owned-use search, real-client focus/scaling and player usefulness remain pending. [Validation, matched before/after components and PC checks](docs/validation/ux-navigation.md). Existing R3a/client reports remain historical evidence.

### UX completion and discovery controls — 14 October 2026

Plugin **0.8.4**, knowledge **r3a-4**, schema **10**, preference schema **2** on
`ux-completion`, based on integrated main `2dd799a`; review/integration pending.
Compact Browse/account-evidence entry, matching Long-term/Specialist counts and a
quiet list-update notice finish the current homepage details. Browse adds combined
style/slot/activity/role/importance/status facets and owned/covered use inspection;
owned possession does not imply usability, numerical benefit or a new grind.
Value/Path reuse the item header and visibly identify the selected tab. Filters,
expanded state, caret, focus targets and ID anchors survive Back/updates; lifecycle
clearing and stale actions/images remain guarded.

`test build --offline --no-daemon` passes **248 tests, zero failures/errors/skips**,
retaining all **1,944** ranged oracle cases. Synthetic native/enlarged-font captures
and the focused remaining PC checklist are in [validation](docs/validation/ux-completion.md).
This delivers R6's current discovery controls and owned-use text entry point.
Actual-client sprites/scaling/focus and player usefulness remain pending; comprehensive
relationship/trail coverage, owned quantitative comparisons, R3b/R4/R5 and KI1 remain
separate work. Historical 0.8.3 evidence above is preserved.

### R6 — Comprehensive relationship coverage and discovery

Audit the uncapped CATALOGUE by **item/outcome × role × activity/method × owned alternative**, alongside numeric and acquisition support. Include every meaningful current endgame family, specialist, sidegrade and permanent PvM utility; extend when content releases. An inventory row alone is not coverage. Account for unreviewed/invalid/unsupported relationships with a reason and evidence need. No arbitrary catalogue limit and no claim of comprehensive delivery while material gaps remain unexplained.

The UX completion increment above implements Browse search, style/slot/activity/role/importance/status filters and owned-use text discovery. Continue reviewed relationship/use coverage, owned quantitative comparisons and actual-client accessible scaling. Show how a useful preparation supports an activity and what that activity can still offer; enable deeper inspectable trails with visited-ID guards as useful, without recursive rank amplification. Keep brief cards, Value/Path and UX's measured one-/two-column layout. Preferences change prominence, not truth, and all relevant conditional/long-term/deferred options remain reachable.

**Acceptance:** all PI profiles plus the existing account/identity matrices. Mature accounts receive defensible specialist/QoL/resource choices or an honest small/no-gain result. One selected goal is never necessary. Search reveals both missing-upgrade and owned-utility explanations, with current evidence and numeric gaps distinguishable. Publish separate matrices for reviewed role relationships, adequate alternatives, paths, per-metric models and live validation. Inspect both ranked results and important omissions. Demonstrate the addition of a newly released role through data plus an existing module when possible; add explicit Java mechanics only for a genuinely new mechanic. The announced Archive remains a watchlist until release/review.


### R7 — Maintenance without a production service

Add optional development scripts/CI that check source/model dependencies, stale review flags, item-ID/variant changes, broken references, inventory gaps, route cycles and known-invalid facts. Fetch only permitted endpoints or use saved permitted inputs; rate-limit/cache. Detect Jagex implemented changes separately from proposals. An optional maintainer-assisted research summary is not trusted runtime knowledge and must not upload accounts.

**Acceptance:** replay a real recent change (for example July 2026 Inquisitor or September penny behaviour) and identify affected facts/models/routes/fixtures. A changed source creates a review finding, not an automatic recommendation. Known-invalid claims can be disabled without disabling unrelated knowledge. Document rights/revision/provenance for every imported asset if any. Benchmark realistic expanded catalogue startup/evaluation on the client; replace arbitrary quotas with justified safeguards. No hosted runtime dependency or scheduled-service requirement.

### R8 — Real-client usefulness and release readiness

Use the matrix below throughout delivery, then perform a focused end-to-end check across mature coverage. Preserve historical pending checks; do not rewrite them as successes. Verify native width/scaling, item icons, scrolling/Back, account switching, preference IO, bank refresh and calculation corrections. Observe a player finding useful upgrades, explaining their comparison and identifying a next acquisition step without opening diagnostics.

**Acceptance:** recommendation disagreements are documented and important false positives/false negatives fixed; player can explain assumptions; no misleading ownership/route/numeric claims; performance remains usable with broad coverage. State actual supported modes, contexts, models and known gaps in README. Prepare Plugin Hub naming/policy/build/notice materials only when requested; submission/publication is separately authorised. A release can honestly be partial; a comprehensive product claim requires the coverage criteria above, not passing a fixed number of tests.

## Recommendation-quality matrix

Synthetic profiles have explicit positive/negative/unknown facts, complete reference setups and no selected goal. Keep expected examples contextual; do not encode one universal order as a golden test.

| Profile | Useful behaviour to demonstrate | Unacceptable behaviour |
|---|---|---|
| Established midgame; no Bowfa; trident/whip-tier | Several viable gear/prayer bridges plus a relevant long-term option; declined CG gives sensible alternatives | Universal CG mandate; only quests; false absence from unread bank |
| Post-Bowfa + crystal; partial magic/melee | Material magic/melee/megarare options; specialist ranged trade-offs | Masori as a Bowfa damage upgrade; endless tiny ranged accessories |
| Tbow owned, no Shadow | Magic improvements and supported spec/specialist choices | Hiding all ranged specialist value; recommending tbow again |
| Shadow owned, no tbow | Relevant ranged/melee options and actual magic gear breakpoints | Default swamp grind; fake gain from incompatible off-hand |
| All three megarares; accessories/prayers/specialists missing | Smaller but defensible context-specific choices, upkeep/defer trade-offs | Only bridge items; every missing sidegrade called essential |
| Nearly complete; strong specialist coverage | Honest small/no-gain outcomes and searchable deferred/coverage details | Three invented priorities just to fill the layout |
| Partial or stale observations; conflicting manual fact | Targeted unknown labels and safe conditional advice | Unknown treated as missing; previous character data retained |
| Owned parts, low access, skipped route | Completed component evidence, actual remaining step, alternative route | Already-owned grind, reward/access cycle, skip erases owned utility |

For profiles with meaningful covered gaps, aim for at least three defensible, varied choices including long-term value where relevant; this is a usefulness check, not mandatory padding. Score each inspected recommendation on relevance, baseline correctness, value evidence, path correctness and comprehensibility (pass/conditional/fail with reason). Review both top results and important omissions. Change one fact at a time (gain Shadow, consume prayer, open bank, skip activity, change target) and verify the explanation changes coherently. Pair automated scenario assertions with native screenshots and focused player feedback; neither substitutes for the other.

## Refinement account and misleading-ranking matrix

These are planned acceptance fixtures, **not newly executed tests**. Pin complete account facts, baseline/context, ownership confidence, rate/pace assumptions and expected reason codes. No selected goal by default. Validate counterfactuals as well as the top cards.

| Profile | Expected decision | Counterfactual / failure to catch |
|---|---|---|
| Post-Bowfa, no tbow; useful magic/melee gaps | Nearer supported improvements can be Worth pursuing; CoX may be Worth doing if distinct useful rewards remain; tbow Long-term when in supported coverage | Large tbow gain cannot override long-tail class; owning Bowfa cannot imply Masori boosts it |
| Tbow owner, no Shadow | Compare nearer magic support against existing staff; retain Shadow Long-term and ToA activity only for independently useful rewards | Add Shadow: remove its grind, reassess accessory breakpoints; do not invent ToA pace from tbow |
| Shadow owner missing other upgrades | Material accessory/robe breakpoints and other supported roles remain useful; CoX activity may be justified; swamp suppressed | Owning a robe piece reduces remaining work; no automatic “finish Ancestral” priority from Shadow alone |
| Many missing useful CoX rewards | One activity explanation lists distinct valued missing families, while tbow remains a separate long-term item | Own/consume scrolls and own armour: activity justification shrinks; unknown unlock or duplicate armour variants cannot inflate it |
| Only an exceptionally rare raid reward missing | Honest few/no immediate pursuits; rare item and supported specialist alternatives remain discoverable | Do not rename the remaining long-tail grind “efficient” because the list is empty; dry KC does not lower fixed-drop remaining expectation |
| Established account with several obtainable mid-effort upgrades | Surface varied supported improvements ahead of a large uncertain grind where remaining work supports it | Add a consequential unknown prerequisite: move only affected advice to Check first/Plan ahead; do not assume a short total route |

Also exercise: equal supported gains with disjoint versus overlapping effort ranges; different-target DPS values; qualitative specialist versus numerical damage; missing/invalid pace; old versus current CoX weighting; multi-roll expected count versus at-least-one probability; same-activity double counting; resource allocation conflicts; all-owned and all-unknown accounts; stale asynchronous identity. Tests must assert justified relationships and explanation changes, not one universal item ordering.

## Progression-intelligence acceptance matrix

**Planned fixtures, not tests executed by this documentation assignment.** Each fixture must specify complete reference slots/levels and TRUE/FALSE/UNKNOWN ownership, unlock, route and supply facts. No goal/preference by default unless stated. Pin context/method, sources, models and ranking versions; assert decision relationships and evidence, not a brittle universal ordered item list. The named account facts below are fixture inputs, never inferred from lack of bank observations. R3–R6 indicate when each supported portion becomes implementable.

| ID / account profile | Expected result | Counterfactual / failure condition |
|---|---|---|
| PI01 — First team CoX: 90s combat, Bowfa/crystal, swamp, tentacle/Fang, BGS; no lance/DWH; 90 Slayer; team explicitly covers percentage reduction | Show a supported team route now; lance retains its dragon-specialist value and substantial fresh route. Personal reduction gap is lower with known team coverage. | Remove the team fact: conditional role check, not “you own DWH.” Switch to solo: require reviewed solo-method assessment, not inherited team readiness. No universal 95-Slayer detour. |
| PI02 — Mature CoX account: usable lance/maul, prayers and relevant CoX armour owned; only tbow materially missing | Tbow can be highly important and long-term. No shared multi-reward claim and no shaman grind for a covered reduction role. | Mark Ancestral absent with positive marginal use: activity gains a genuine additional family. Duplicating robe variants does not add votes. |
| PI03 — First quiver: 99 ranged/magic, Bowfa/crystal, tentacle, swamp, blowpipe, Rigour; no Venator/SGS/megarare; supported existing method | Show quiver as a challenge path and Venator/SGS as optional improvements with alternatives. No certainty of success or fixed learning ETA. | Add Venator: show its role and remove its acquisition. Lower relevant levels/change prayer: withhold unsupported one-shot claim. Do not hard-gate on quiver's own reward. |
| PI04 — AFK-oriented Slayer: 92 Slayer, Bowfa/crystal, barrage access, many eligible tasks, supply facts known; Venator absent | Venator retains substantial attention/convenience value. With explicit lower-attention preference it can rise without a DPS/payback claim. | Remove preference: contextual convenience choice remains visible, not assumed personal desire. Choose active-XP preference or single combat: change prominence/applicability without declaring the item universally bad. |
| PI05 — Developed Doom candidate: Scorching, elite Void, appropriate demonbane swaps, crystal halberd/blowpipe, Rigour/Lightbearer, Final Dawn complete; no tbow/Zaryte/Scythe | Recognise reviewed preparation and a treads/other-eligible-reward route. Expensive alternatives are optional, not access requirements. | Remove demonbane capability: selected method needs a check/alternative. Change assumed delve 3→4: treads eligibility changes without changing quest access or claiming actual ability. |
| PI06 — Core ranged gap: strong ranged kit, fury, zenyte and other assembly inputs/skills confirmed; anguish absent | Show anguish's role and remaining assembly, not a fresh gorilla grind. Importance need not be tiny because one neutral breakpoint is small. | Add rupture: suppress duplicate anguish acquisition. Allocate the zenyte to a preferred competing jewellery outcome: expose choice, not both as free upgrades. |
| PI07 — Tbow owner, high-HP eligible encounter, Zaryte absent | Retain Zaryte burst role independently of sustained comparison; actual Nex/ACB/component effort remains visible. | Lower HP/add immunity/change spec budget: narrow or remove that role. No unconditional 110, no arbitrary tbow→Zaryte main-weapon replacement. |
| PI08 — Own nihil horn/ACB but unknown shard quantity; or own four Venator shards | Remove completed drop stages, show remaining exact resources/work and uncertainty. | Confirm sufficient shards/materials: move to assembly. Do not turn all effort LOW while a large skill/resource gap remains. Previous dry KC does not imply progress. |
| PI09 — All three megarares plus core armour/prayers; Venator, selected sustain or utility absent | Show genuinely uncovered roles and a sufficient-owned-alternative explanation. No repeat Bowfa/swamp bridge acquisition or “every missing specialist is essential.” | Add utility alternatives or reject relevant activity: reduce affected priority only. All-megarare ownership alone cannot erase multi-target/convenience/special roles. |
| PI10 — Nearly complete, all useful modelled roles covered | Few/no active pursuits is correct; small optional improvements and gaps are inspectable. | Delete one trivial item: do not invent three top priorities. Add a specific useful activity preference: promote only evidence-supported contextual value. |
| PI11 — Muspah with four shards, imbued heart owned, constrained essence | Explain remaining bow and saturation routes with competing essence allocation; shared rewards aren't guaranteed simultaneous. | Remove heart ownership: saturation becomes a conditional downstream possibility, not current completed-heart value or automatic second reward. |
| PI12 — Partial bank, unknown charges/teammates/unlock, conflicting manual absence | Known role evidence remains; uncertain ownership/usability/status is clear with one focused check. | Switch character/profile during evaluation: no old context/preferences/results. A missing DPS model with valid QoL evidence withholds DPS only. |
| PI13 — Full melee kit tested against small/large, high/low-defence targets; team debuffs explicit | Fang/Scythe/Soulreaper rankings can change by context; debuff/sustain trade-offs stay independent. | Switch Fang stab→slash, ToA→outside, size 3→1, or reset Soulreaper stacks: only supported affected metrics change. No blanket megarare dominance. |
| PI14 — Existing treads and some Cerberus boots; otherwise developed account | Only missing useful style additions remain. Inventory savings reflect actual replaced switches. | Own base treads at insufficient equip levels: own-but-not-usable, not another Doom grind. One missing boot never requires all three. Unknown treads eligibility must not block eligible cloth/Eye activity evidence. |
| PI15 — Consumed penny, master tier lost; thread/pouch owned; prayer scroll unread | Distinct unlock/active/assembly actions; restored tier restores the penny effect, and a four-rune use can justify pouch QoL. | Empty bank must not imply penny unconsumed. Three-rune reference removes saved-slot claim. Prayer levels alone never prove a consumed scroll. |
| PI16 — Large optional prep detour versus adequate current setup, including a useful QoL upgrade | Default is start supported method now plus optional detour. Independent broad/comfort value may still justify pursuing the upgrade. | Remove the existing adequate role: priority may rise. Rename/duplicate contexts or add an indirect utility cycle: rank must not rise. Unknown effort is not cheap; long effort is not low importance. |

Cross-cutting checks: hard dependency cycles rejected; utility cycles terminate without score amplification; qualitative claims survive unrelated missing metrics; optional prep never becomes ACCESS; new aliases/prose do not increase breadth; one unknown reward cannot veto valid activity opportunities; partial quantities and allocation conflicts remain explicit; source withdrawal removes dependent claims only. Check native Value/Path explanations for PI03, PI05, PI09 and PI12 at sidebar width, not only model results. Actual user preference/usefulness and client evidence remain separate, pending validation.


### Client comparison-gap explanation — 13 October 2026

Two supplied RuneLite screenshots confirm the integrated Enemy with 0 Defence label, DPS-first Shadow detail with 4→5 ticks and off-hand removal, and max-hit-first Ancestral detail with unchanged four-tick interval. The supplied reference confirms Brimstone ring with tormented bracelet: the ring's unmodelled accuracy proc blocks hit chance/DPS; Confliction is not involved in that reference. These are limited visual/client findings, not a numeric-model, DPI/navigation or player-usefulness acceptance pass. Full reference dumps, observation timestamps and account identifiers are not recorded here.

Branch `client-model-gap-labels` clarifies the existing per-metric gap: name only present items, distinguish before/after/both, and explain it beside the Value metrics. Brimstone is not described as an attack-history effect. Keep supported max hit, DPS-first/max-hit-first presentation, numeric support boundaries and known-equipment references. An explicit empty-ring control can test the supported numeric view; it does not establish actual Brimstone DPS. Plugin patch 0.6.2, knowledge reference-1, schema 8 and powered-magic-2 remain. [Validation](docs/validation/client-model-gap-labels.md).

**Quantitative follow-up alongside R2:** review and implement Brimstone's accuracy proc with independent hand/reference fixtures and contextual strongest-owned-ring comparisons. This concrete model gap blocks supported DPS on the tested reference; do not silently drop the ring or treat its effect as zero. Confliction's one-handed history remains a separate task in the existing magic/remaining-model coverage. The explanation fix implements neither mechanic.

## Next implementation

**Continue R3b melee families and relevant R4 models from updated main, alongside focused actual-client UX checks and KI1 evidence-selected capture work.** R3a relationship advice/cards and the first R6 item-name search increment are already integrated; do not rebuild them or restore the superseded R2-model implementation. Read DESIGN `progression-1`, PROGRESSION_RESEARCH R01–R30, KILL_IMPACT and the PI account matrix for the relevant scope. Keep ordinary comparison, acquisition and recent-kill metrics distinct.

R1.5b pacing, complex R5 distributions, full R6 filters and remaining numeric gaps continue as documented. Brimstone and Confliction remain active quantitative gaps. Kill Impact research is not implementation or a gate to melee/current UI work. New increments must improve useful no-goal advice, preserve numerical/lifecycle regressions and report actual client checks separately. TASKS.md remains the only backlog.

## Historical task snapshots and results

The following is the **complete pre-reset TASKS.md, preserved verbatim** from `b50b0a9`. Its “next task”, catalogue budgets, delivery order and review gates are superseded above. Its implementation reports, exact test evidence and unperformed live checks remain valid historical records. No new passing result is implied by this reset.

<details>
<summary>Expand historical T1–T4 and P1–P4a briefs/results (not active instructions)</summary>

# Ironpath tasks

Updated 12 October 2026. This is the single active backlog.

## Current decision and status

Petter approved an **automatic, account-aware Ironman PvM upgrade advisor** on 12 October 2026. The primary question is which upgrades are worthwhile for this account, what they change and how to obtain them. Goals are optional; equipment, prayers, skill levels and permanent unlocks are eligible. The next task is **P4a, an upgrade-first vertical slice of approximately 3–4 representative upgrades**, not another catalogue-expansion-first delivery. Useful broad PvM work remains in place. The old CoX trial is not a prerequisite; unperformed tests remain pending.

Verified for this update: `main` is `c717927cdf4e3fd8128d28b96e05690965f5a908`; [PR #5](https://github.com/PetterHL/ironpath/pull/5), `p1-p3-broad-pvm`, is **open and unmerged** at `eb22fef97aa899bac5611b3001462dfdbebe0b80`. It incorporates PRs #1–#4, which also remain open. This documentation change is stacked on that implementation. Recheck current heads before starting; do not rebuild T1–P3, merge/close prior PRs or assume main contains their code.

| Historical work | Verified repository state | Remaining qualification |
|---|---|---|
| T1 account observations / sidebar | [PR #1](https://github.com/PetterHL/ironpath/pull/1), `t1-account-advisor` | Limited user-performed checks recorded; other live checks pending |
| T2 reviewed CoX catalogue | [PR #2](https://github.com/PetterHL/ironpath/pull/2), `t2-cox-catalogue` | Nine decisions; coverage is deliberately narrow |
| T3 goals, ranking, local preferences | [PR #3](https://github.com/PetterHL/ironpath/pull/3), `t3-local-advisor` at `b3a93d9aa60f7661e49b970fc3b9272e7ad2df12` | Build / 58 tests reported passing; T3 real-client checks pending |
| T4 trial preparation | [PR #4](https://github.com/PetterHL/ironpath/pull/4), `t4-user-maintenance-trial` at `fad507f0b87f2e4f879c8810daad663f7dd3043f` | Documentation only; simulated maintenance rehearsal complete; user trial pending |
| T5 CoX distribution | Not started | Superseded by P5 below |
| P1–P3 broad PvM foundation | PR #5 at `eb22fef`; 16 decisions, seven goals | Historical 75 tests passed; limited PC flow user-reported working, detailed validation pending |

Historical implementation/test reports are in [TASKS.md at the T4 baseline](https://github.com/PetterHL/ironpath/blob/fad507f0b87f2e4f879c8810daad663f7dd3043f/TASKS.md). Preserve those results when combining branches. This plan replaces the old CoX-only scope and T1-only assignment; historical reports remain evidence, not active instructions.

## Delivery order

**P4a → one implementation review → P4b → P5.** P4a includes source verification, minimal model changes, the bounded numerical comparison, sidebar experience and validation in one assignment. Expand toward approximately 8–12 researched upgrades only after testing and review; counts are budgets, not quotas or permanent limits. No plugin implementation is part of this documentation update.

The P1–P3 briefs and results immediately below describe the completed earlier delivery. Their goal-count comparator, available-only cards and earlier catalogue budget are historical behaviour, superseded for future implementation by docs/DESIGN.md and P4. Preserve their reported results and pending checks accurately.

## P1 — Bring the existing foundation and current plan together (implemented)

- Inspect current branch heads and open PRs before work. Start from the existing T3 implementation including `b3a93d9`; the recorded T4 branch already contains it.
- Bring that implementation, T4 historical results and the current main planning documents together on a working branch. Resolve documentation conflicts in favour of this active P1–P5 plan while preserving accurate historical reports and useful build/login instructions.
- Do not reset existing branches, discard user changes or automatically merge/close the stacked PRs. Open the coherent implementation PR against main and explain which earlier work it includes.
- Run the existing build and meaningful baseline tests once. Fix actual blockers as encountered; do not restart a full CoX review or repeat completed maintenance simulations.

**Done:** one working baseline includes existing implementation and current scope; earlier test evidence remains available; pending real-client checks remain pending.

## P2 — Generalise only what the broad slice needs (implemented)

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

## P3 — First useful broad PvM slice (static delivery complete; PC checks pending)

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

### P1–P3 results — 12 October 2026

Branch `p1-p3-broad-pvm` is a combined delivery against current main, based on
`fad507f0b87f2e4f879c8810daad663f7dd3043f`. It contains T1/T2/T3 through
`b3a93d9aa60f7661e49b970fc3b9272e7ad2df12`, including the first-save directory fix
and CoX goal-status regression. Current main `c717927` was merged into this new
working branch, with its broad scope taking precedence. PRs #1–#3's implementation
and PR #4's historical documentation are incorporated; those PRs were not merged,
closed or reset. T1–T4 text below remains verbatim, under an explicit historical
heading. Main's AGENTS/design/plan and useful PC build/Jagex login instructions
are retained. P4/P5/distribution were not started.

**P1:** the combined baseline's `test build --no-daemon` passed **58 tests, zero
failures** on Temurin JDK 11.0.32.1 / RuneLite 1.13.1. The cloud clone required the
supported shell network permission to reach the proxy; the temporary JDK,
configured proxy and system CA setup reused the documented T1 approach. No new
foundation or repeat maintenance simulation was created.

**P2:** all real decisions share the pure bounded evaluation flow. Reusable action
types replace CoX kinds/unique-kind constraints; seven goals, activities, outcomes,
contextual benefit bands, supported-goal breadth, qualitative effort and usable
item packages live in bundled JSON. Hard access, suggested preparation, context
and completion have separate predicates/labels. ALL/ANY and hard-cycle validation
remain bounded; selected-goal routes rank before shared/other relevance, then
contextual benefit, breadth, effort and stable ID. At most three different
activities/families are shown. Activity skips exclude routes while retaining owned
reward utility; individual old action skips remain available.

The existing reader/controller/lifecycle/store were extended in place. Selected
quest completion uses supported `Quest.getState` on the client thread; Deadeye and
Mystic Vigour use permanent unlock varbits. Only selected progress events request a
coalesced read. Unknown facts, time/provenance, positive live precedence, observed
usable/charged distinctions, memory-only observations and character/profile guards
remain. Acquisition absence needs a targeted explicit confirmation, including when
an opened bank is empty. Schema 2 reads T3 schema 1, preserves all four old goal IDs,
action skips and exact fact keys, keeps the same isolated identity/path, and drops
retired IDs explicitly. No old skip is widened automatically.

**P3:** plugin **0.3.0**, knowledge **p3-1**, catalogue schema **4**: **16 reviewed
decisions / 61 exact item IDs / 62 sources / seven goals**. Retain the nine reviewed
scroll/training/owned-kit/ordinary-team decisions; add a Slayer-87 milestone and
six real acquisition/unlock choices covering CG, Zulrah, Slayer/cave krakens, ToA,
Moons and Royal Titans. Post-Bowfa facts can support Zulrah, ToA and intermediate
prayer choices. The magic-fang upgrade supports both raid goals; CG can be skipped
in favour of Moons or an accurate unknown/blocked explanation. Owned advanced
magic options suppress redundant trident routes without manufacturing charges.

Full current pages were accessed for the bounded new access/acquisition/variant
and alternative review; actual inspected revisions, original explanations,
editorial preparation assumptions, dates and reuse/licence metadata are bundled.
The review details and exact limitations are in docs/RESEARCH.md. There is no
runtime scraping, new runtime dependency, backend, database, AI, KC/Collection Log,
DPS optimiser, universal gear hierarchy or complete loadout planner.

**Concrete fixes during generalisation:** regression tests caught a coordination
preparation condition that had lost the retained CoX baseline, and alternative-route
diagnostics that could turn bounded container absence into a missing global
assembly input. Both were corrected. Observed relevant unconsumed Royal Titans
scrolls also prevent another acquisition grind. When all remaining alternatives
are blocked, the goal names a real route-specific access/preparation condition;
an unknown alternative prevents claiming a universally missing requirement.

**Final automated validation:** `test build --offline --no-daemon` on the same JDK
and CA setup: **75 tests, zero failures/errors/skips**; `git diff --check` passes.
All 58 existing cases remain, with assertions adapted only for the broader budget
and generic explanation wording, plus 17 new cases covering broad goals/ranking,
CG skip/restore, post-Bowfa and advanced accounts, shared upgrades, explicit target
absence, open/unknown banks, manual/live precedence and non-usable ownership,
Slayer milestone/task separation, ANY prayer alternatives, access versus preparation,
owned scrolls, invalid references/package cycles, catalogue-defined goals, schema
migration/restart and selected quest/unlock character isolation. The original
first-save, serial IO, delayed callback, actual Swing listener and profile/character
regressions still pass. These are synthetic fixtures and mocked API/UI events,
not a live account trial. Non-fatal Fontconfig cache and Gradle deprecation warnings
remain. No outstanding test/build blocker.

### Three concrete synthetic examples

These rows describe tested synthetic observations and explicit preferences. They
are not captured account facts or player usefulness judgements. Skills meet the
stated authored preparation baselines; the bank is positively observed, and only
the relevant missing ownership targets are explicitly confirmed.

| Synthetic context and goal | Suggested activity | Reviewed alternative and reason |
|---|---|---|
| Whip, rune crossbow, charged seas trident, base keris; 90 combat skills, required quests confirmed; Bowfa/twisted bow and atlatl targets confirmed absent. Goal: ranged options; **CG skipped**. | **Moons for an eclipse atlatl**; no CG card returns. | Keep the crossbow for appropriate low-level/team planning. Moons is a contextual alternative with melee/local-supply preparation, not a universal Bowfa equivalent. Restore CG and its reviewed route regains priority. |
| Bowfa with default active crystal pieces, whip, seas trident and base keris; Rigour unlocked, Augury/Mystic Vigour locked; swamp/competing magic and fang gaps confirmed. **No goal**. | **Royal Titans, Zulrah and ToA** are three distinct choices. | The seas trident remains usable while deciding whether scale upkeep justifies Zulrah; current prayers remain available while deciding whether a duo unlock route is worthwhile. Bowfa suppresses another ranged acquisition without erasing the magic/prayer gaps. |
| Bowfa/crystal, whip, observed charged Shadow and base keris; required access confirmed, higher prayers unlocked, fang absent. Goal: **ToA / fang**. | **Low-level ToA practice toward a fang**. | Ordinary team CoX planning with the observed kit remains available. Shadow ownership suppresses base/swamp trident grinds, while the fang remains a distinct supported melee objective; no success, supplies or solo/Expert readiness is inferred. |

### Focused PC checklist — limited flow reported working; detailed checks pending

Use README's JDK 11 build/launch commands on `p1-p3-broad-pvm`. No game client was
launched in Cloud; `DISPLAY` is unset and no authenticated game session is available.
Earlier T1 passes and the limited P3 user report below have their stated evidence
boundaries. Do not grind/acquire items just to exercise a boundary; label unexercised
checks pending.

1. **Observations:** before opening the bank, verify unknown storage. Open it and
   compare displayed levels, selected quest completion and prayer unlocks with the
   game, including an unavailable/uncovered variant. Confirm active/empty forms,
   placeholders and provenance do not invent ownership/usability.
2. **Goals and alternatives:** on supported facts, try no goal, ranged, magic and
   ToA goals. Confirm up to three different activities, meaningful priority changes,
   targeted ownership prompts and separate access/preparation/ANY explanations.
   Open a pinned reference under Reviewed sources.
3. **CG skip/restore:** skip CG at activity level. Check Moons appears when supported,
   or an accurate blocked/unknown reason; no CG card returns. An already-owned
   Bowfa/crystal kit must still support appropriate advice. Restore the activity.
4. **Manual facts and persistence:** with the client closed, preserve a backup of
   only the plugin directory before a fresh-directory check. Test a reversible
   target absence, manual advanced ownership and clearing. Wait for Preferences
   saved locally, restart and check restored choices plus unknown bank. If a T3
   file is available, verify schema-1 choices survive and the next save is schema 2.
5. **Isolation:** check another character and RuneLite profile, then return. Confirm
   only each identity's choices restore, observations clear on transitions/hop/logout,
   and stale UI/load/save callbacks cannot change the new identity. Keep local files
   private; report anonymous labels and minimal facts only.

Accuracy, source-button behavior, real-client save/restart, character/profile
isolation and player usefulness remain **pending**. Zero live critical errors is
not assumed. P4's approximately 35–45 decisions and P5/publication remain outside
this delivery.

### P3 user-reported PC flow — 12 October 2026

After the short guided PC test and clarification of the Restore activity/action
controls, Petter reported: “evrything works.” Record the guided flow as **reported
working by the user**, rather than a Codex-observed pass for each assertion.
The supplied sequence covered branch update/build/launch, Knowledge p3-1 and the
bank-unknown/open-bank flow, no-goal suggestions, switching ranged/magic/ToA goals,
and CG activity skip/restore. No screenshots, account facts, exact card results,
build output or per-step measurements accompanied this report; those details
are not inferred or stored.

This provides a limited positive workflow report. Detailed level/quest/prayer and
item-variant accuracy, alternative correctness and player usefulness judgements,
source buttons, manual confirmation boundaries, first-save/schema migration,
restart and character/profile isolation remain **pending**. It does not accept
all five checklist scenarios, establish zero critical errors, or change any
historical T1–T4 result. P4/distribution remain outside this assignment.

## P4 — Upgrade-first delivery (replaces catalogue-expansion-first scope)

P4a is the next implementation assignment. Complete it in one coherent PR, with small commits as useful and one normal review at the end. Source checks, model changes, calculations, UI and tests are work within that delivery, not separate approval gates. P4b follows only after the slice is tested and reviewed. Do not reopen settled backend, AI, repository or independent-development decisions.

### P4a — First complete upgrade experience (implemented; review and PC checks pending)

**Outcome:** observe account → identify relevant upgrade → explain personal value → compare alternatives → show acquisition path. The experience must work with no selected goal and include clearly labelled provisional advice where ownership is unknown.

Use docs/DESIGN.md's representative cases: **occult necklace, trident of the swamp, Zaryte crossbow and avernic treads**. Four is a scope budget, not a quota. A documented substitution or a three-case slice is acceptable only if it preserves the numerical comparison, specialist limitations and prerequisite/shared-route cases across multiple PvM activities. Do not shrink to CoX-only advice, discard existing broad content or pad the slice with administrative reminders. Cloth/Eye secondary opportunities do not require two additional full upgrade implementations.

| Work / dependency | Implementation and completion evidence |
|---|---|
| **1. Reconcile baseline and evidence** | Recheck main and relevant open PRs; use PR #5's implementation plus these updated documents, following the current stacked-PR workflow if unmerged. Do not merge/close/reset earlier PRs. Run existing baseline tests once. Verify selected exact variants, new route access/prerequisites, supported alternatives and target/formula sources. Reuse existing evidence where applicable; pin new revisions and record access/review/reuse metadata. |
| **2. Outcome/value and route evaluation**, using 1 | Extend existing catalogue/Advisor in place to distinguish outcome, route and contextual comparison. Replace goal-count ranking; retain stable identifiers, pure truth evaluation, skip semantics and source/cycle validation. Add provisional and plan-ahead results with targeted confirmations and route-specific nearest steps in no-goal mode. Group shared routes and separate quick actions from major upgrade candidates. |
| **3. Reference setup and numerical model**, using 1 | Build docs/DESIGN.md's bounded powered-magic reference setup/comparison helper. Reuse observed levels/items; add only reviewed slot/stat/target facts. Propose a setup with lightweight confirmation/correction, explicit empty/unknown slots, prayer/boost assumptions and invalidation. Keep it in memory. Show before/after max hit and the supported trident theoretical-DPS/hit-chance comparison against ordinary Dharok, with exact assumptions and unsupported fallbacks. No external runtime calculator, dependency import, general optimiser or persisted loadout system. |
| **4. Four upgrade experiences**, using 2–3 | Occult: contextual magic bonus/breakpoint and Slayer acquisition prerequisite. Swamp: owned seas alternative, target comparison, Zulrah/assembly/upkeep work. Zaryte: special-attack role, hit/HP/energy/bolt limitations and verified Nex/component route. Treads: exact owned boot alternative, equip/access/component gaps and conditional shared Doom rewards. Preserve relevant advanced ownership, prayer/skill/unlock and CG/Moons/Titans/ToA/CoX content. Numbers are required only where the bounded model supports them. |
| **5. Sidebar and validation**, integrated with 2–4 | Compact single-column cards open useful detail views; sources/assumptions and contextual corrections are accessible. Observations, diagnostics, goals and manual administration move to secondary controls. Keep major upgrades visible above separate quick actions. Reuse lifecycle/identity, Swing and serial IO guards. Complete the acceptance checks below, update P4a results and README's delivered behaviour, and open a reviewable PR. |

**Required acceptance checks:**

1. **Account-specific value:** no-goal fixtures for midgame, post-Bowfa and advanced accounts select useful supported outcomes. Owned equivalents suppress redundant grinds; owning Bowfa/twisted bow alone does not suppress Zaryte's distinct role or make it worthwhile. A deliberately marginal/no-gain case is deferred or accurately explained. Goal aliases/count changes cannot increase benefit; a real optional goal may refine relevant priorities.
2. **Partial knowledge:** unopened and observed-empty banks never establish global absence. Supported evidence can produce provisional cards with specific ownership/alternative questions; irrelevant or unsupported contexts do not produce a generic shopping list. Confirm/clear and positive live/manual precedence update cards correctly. Unsupported inputs suppress a numerical result while preserving justified qualitative advice.
3. **Routes and shared rewards:** a relevant inaccessible upgrade shows its next prerequisite without selecting a goal; distinguish hard access, preparation and equip requirements. Preserve ALL/ANY alternatives and activity skip/restore, including owned reward utility after skipping. Doom outcomes have individual ownership/value and delve eligibility; do not assume simultaneous drops, enough materials or player capability. One shared route explanation replaces duplicated work without adding unrelated benefits.
4. **Trustworthy numbers:** independently derive and test supported integer-rounding/max-hit cases, including 90-Magic seas 25 → swamp 28 and seas 25 → 26 with only occult's 5% bonus. Vary Magic across breakpoints, supported gear/prayers/boosts and styles; test zero-delta, unknown gear, incompatible slots, empty/charged variants and unavailable assumptions. Validate accuracy branches, successful zero-hit handling and attack interval against hand checks and a pinned, locally run calculator using original synthetic inputs. Record reference version, inputs, outputs and tolerances; investigate discrepancies. No production-code-derived expectations, copied corpus or claim that oracle agreement proves game correctness. If the reference cannot be run, document the limitation and supply independent hand-derived accuracy/DPS fixtures; the mandatory numeric case cannot simply be waived.
5. **Numerical honesty and alternatives:** at least one account-aware end-to-end number changes when observed levels/reference setup change. Trident DPS is labelled uninterrupted attacks against the exact target, excludes poison/venom and does not inherit wind weakness. Mention a relevant wind-spell alternative at Barrows without claiming full spell optimisation. Zaryte's conditional proc is never called a guaranteed hit; no unsupported encounter-time, kills/trip or deeper-delve estimate appears.
6. **UI and regression safety:** cards are legible at RuneLite's normal narrow width and with long labels. No goal or account-wide questionnaire is required; details show assumptions, alternatives and acquisition work. Owned-scroll/assembly actions are separate. Test meaningful actual control behaviour for corrections/back navigation and delayed stale actions. Reference confirmations invalidate after relevant changes and clear on character/profile transitions. Retain relevant catalogue, first-save, serial IO, schema migration, restart and account-isolation tests; replace only assertions intentionally superseded by the new product semantics.

**Validation and review deliverable:** run the meaningful test suite and build, catalogue validation and `git diff --check`; report actual commands/results and resolved RuneLite/JDK versions. Provide three concise synthetic walkthroughs (including a provisional/blocked and an advanced-specialist case), the supported calculation matrix and known exclusions. Every selected outcome path must be inspectable across fixtures; a single account need not receive every card. Preserve historical 75-test evidence as history, not a claimed new pass. Record real-client checks below as passed only when actually exercised.

**Focused PC checklist — pending:**

- Open with no goal, before/after bank observation; inspect a useful card and its ownership/reference assumptions without navigating a setup form.
- Compare supported before/after figures to the displayed setup, correct one choice, change a relevant level/equipment input, and confirm recalculation/invalidation. Inspect an unsupported setup's honest fallback.
- Inspect a specialist and a blocked/shared-route example if the account supports them; confirm alternatives and remaining work are useful. Leave unavailable account cases pending rather than grind items for a test.
- Check narrow-width readability, detail/back controls, quick actions, secondary goals/diagnostics, sources and activity skip/restore.
- Recheck first-save/restart and two-character/profile separation using the existing privacy-safe procedures; stale setup/UI actions must not cross identities. Distinguish observed passes, user reports and synthetic evidence.

**Stop after P4a delivery and review request.** Do not start P4b, full encounter modelling or Plugin Hub publication in the same assignment.

### P4a implementation results — 12 October 2026

**Baseline and stack:** rechecked main `c717927cdf4e3fd8128d28b96e05690965f5a908`,
PR #5 open/unmerged at `eb22fef97aa899bac5611b3001462dfdbebe0b80`, and PR #6
open/unmerged at `2ef409a9f66f3bb93fa85942f1a1c850e6a0b074`. Created
`p4a-upgrade-advisor` directly from #6, verified #5 is its ancestor, and preserved
the earlier implementation, four updated guidance documents, P1–P3 reports and
historical T1–T4 records. The implementation PR targets
`docs/upgrade-first-roadmap` to keep this delivery reviewable in the existing
stack. Existing PRs/branches were neither merged, closed nor reset.

**Delivered:** plugin **0.4.0**, knowledge **p4a-1**, catalogue schema **5**:
**four reviewed upgrade outcomes / nineteen decisions / eighty-one exact item
forms / eighty-four sources / thirteen bounded stat records**. The original
sixteen decisions and stable preference identifiers remain. Occult adds the
93-Slayer/task/protection route; swamp groups Zulrah and owned-parts assembly;
Zaryte distinguishes Nex, Armadyl-crossbow and shard work; base treads shows
quest/equip requirements and separately optional Cerberus additions. Already
owned components reduce the remaining component checks. Cloth/gauntlets and Eye
opportunities have their own ownership/value and delve-2/3 eligibility; treads
requires completed delve 4. No quantity, simultaneous reward or delve capability
is inferred. Relevant exact variants, access and alternatives were checked with
actual revision footers and the existing pinned API definitions; detailed
mechanics/oracle provenance is in docs/RESEARCH.md.

Value, ownership certainty and acquisition access are separate. No-goal cards can
be provisional or plan ahead; specific ownership questions remain reversible,
and live/manual positive evidence beats absence. Owned equivalents suppress the
acquisition. Bowfa/twisted bow alone neither establishes nor suppresses Zaryte
specialist value. A magic-only eternal case is explicitly marginal; a melee-only
primordial case receives no treads grind. Comparator order is contextual value,
optional relevant goal, distinct evidenced encounter applications, then remaining
access/work and stable ID. Goal counts/aliases contribute no score.

The existing Swing panel now leads with three compact single-column cards and an
optional fourth. Details expose baseline evidence, alternatives, numerical
assumptions, requirements/parts/shared rewards and sources, with Back navigation.
Targeted ownership questions keep stable positions after edits. Equivalent exact
variant sets share confirmation meaning, so a historical advanced-ownership key
suppresses the grouped outcome and redundant assembly without a duplicate
ownership question. No charge/usability is created by that planning fact. Quick actions are
separate; all retained available broad advice, goals, observations and
administration remain accessible through secondary controls. Native ItemManager
images are used in the client; headless previews use a symbol fallback. Explicit
RuneLite colours and wrapped long controls remain legible at the native width.
These previews are not an authenticated client or player usefulness test.

The powered-magic helper returns **supported / conditional / unsupported** with
reasons and the complete input/source/model contract. Proposals use observed
levels/exact supported items, explicit empty slots and visible style/prayer/boost
assumptions. Unsupported or unread equipment cannot silently add zero; a user can
explicitly select the minimal reference template. Lightweight corrections and
confirmation stay in memory, invalidate after input changes and clear with the
existing account/profile generation. Stale reference and navigation actions are
rejected. No loadout persistence, runtime calculator, dependency, backend or
account-data export was introduced. Preferences remain schema 2 with existing
schema-1 migration, serial Filepath IO and first-save/restart isolation guards.

**Actual validation:** the first offline baseline attempt could not resolve
uncached `latest.release` metadata. `source /workspace/ironpath-environment/activate.sh`
followed by `./gradlew test build --no-daemon` resolved the metadata and passed
**75 tests, zero failures/errors/skips** on Temurin **11.0.32.1+1**, RuneLite
**1.13.1**, Gradle **8.10**. This is a fresh baseline pass, separate from the
historical P3 result. Final `./gradlew test build --offline --no-daemon` passed
**106 tests, zero failures/errors/skips**; catalogue validation and
`git diff --check` pass. Fontconfig cache and Gradle deprecation warnings remain
non-fatal. The final tests cover the four paths, partial knowledge, owned/manual
precedence, marginal/specialist contexts, aliases, shared outcomes/components,
integer/accuracy/distribution cases, unsupported inputs and actual Swing/client
control events. Existing regression cases are retained; priority assertions were
changed only for the documented value-before-goal rule, retaining direct-goal
next-step and available-alternative checks.

The pinned Wiki calculator was actually run separately on **234 original
synthetic scenarios**, not a copied upstream corpus. All integer results match;
accuracy tolerance is `1e-12`, DPS tolerance `1e-10`, with no discrepancy.
Independent hand-derived expectations also pass, including both accuracy branches,
successful zeros, attack interval and gear/prayer/style rounding. The original
repeatable harness and output fixture retain provenance. Oracle agreement is not
claimed as game correctness. Headless native-width primary/detail PNGs are
produced under `build/reports/p4a`; details/back, targeted confirm/clear,
correction/confirmation, input-change invalidation and deliberately delayed
cross-character actions are exercised with actual controls and mocked client
queues. Real browser, client appearance and account facts remain pending.

**Supported calculation matrix and exclusions:**

| Input | Supported boundary |
|---|---|
| Weapon | Observed charged seas 11905/11907/22288; swamp 12899/22292. Empty/manual forms cannot become a baseline. Candidate swamp is explicitly proposed charged/supplied. |
| Slots | Reviewed fury/occult plain/ornamented necks and dragon/primordial/eternal/pegasian boots; every other reference slot explicitly empty. Incompatible/unknown mechanics return unavailable. |
| Skills/assumptions | Real Magic 75–99 (swamp equip 78+); no boost or full +4 magic-potion assumption; Accurate/Longrange; no offensive prayer, Mystic Might or Augury only when use/unlock requirements are confirmed. Never infer an active prayer. |
| Target/result | Ordinary Barrows Dharok NPC 1673, Magic 1 / magic defence -11; 4 ticks / 2.4 seconds of uninterrupted attacks. Max hit, hit chance and theoretical DPS only. |
| Excluded | Wind weakness on tridents, venom/poison, advanced staff/set mechanics, thralls/specials, raids/scaling/reduction, movement/resources, expected kill/encounter time, kills/trip and deeper-delve estimates. Wind spells remain a qualitative Barrows alternative. |

At **90 Magic, Accurate, no boost/prayer and all non-weapon slots deliberately
empty**: seas max **25**, hit chance **96.6667%**, DPS **5.0502136752**; swamp max
**28**, chance **97.0412%**, DPS **5.6746768440**; seas plus only occult max **26**,
chance **97.1062%**, DPS **5.2749035662**. UI rounds hit chance to one decimal and
DPS to two. These are proposed reference scenarios, not actual account performance
or universal DPS percentages. Observed levels and corrected reference choices
change the end-to-end numbers; an already selected occult has zero delta.

**Three concrete synthetic walkthroughs — no player data/usefulness claim:**

| Observed facts / optional preferences | Outcome and next step | Alternatives / boundary |
|---|---|---|
| Seas trident 11907 and fury 6585; Magic 75, Slayer 80; no goal; global target ownership unknown. | Provisional swamp and occult cards. Occult names Slayer 93 then task/protection checks; swamp separates 78-Magic equip work. The supported occult reference gives max 20 → 21; swamp numbers are withheld below its wield level. Positive inventory with an unread or empty bank also supports provisional cards in a separate fixture. | Keep seas/fury. No missing global ownership is invented; a targeted confirmation can change certainty or suppress the outcome. |
| Bowfa/crystal, seas, fury and dragon boots; Magic 90, Slayer 90; no goal. | Swamp, occult and treads remain relevant. Swamp has a tested Dharok comparison; occult names its Slayer gap and treads The Final Dawn. Correcting reference feet/neck changes the numbers. | Keep seas and compare scale upkeep; wind spells remain relevant at Barrows. Zulrah skip excludes that route; a separately tested owned-fang/chisel case retains assembly/quick-action utility, with no further fang grind. |
| Twisted bow 20997, Armadyl crossbow 11785 and warhammer 13576; Magic/Ranged 99; no goal. | Conditional Zaryte specialist card, with The Frozen Door/access/team and component checks. An explicit ruby-target planning confirmation refines its value band; tbow alone produces no Zaryte card. | Tbow keeps its sustained role, ACB its bolt role and defence reduction competes for special energy. Evoke requires a hit and 75% energy; 110 ruby damage is not guaranteed. No unsupported DPS is shown. |

**PC status:** no authenticated client/in-game testing was performed. Every P4a
PC item immediately above remains **pending**, including native item icons,
bank/quest/variant accuracy, source browsing, readability/usefulness in the actual
client, correction/invalidation, first-save/restart and two-character/profile
checks. Historical limited user reports stay limited; zero live critical errors
is not assumed. P4b, full encounter modelling and Plugin Hub publication were
not started.

### P4b — Expand useful coverage after P4a review (not started)

Expand toward approximately **8–12 thoroughly researched upgrades**, reusing the validated UI, outcome/route model and comparison boundaries. Preserve useful existing decisions; this is not a total-record quota or permanent limit. Prioritise gaps found in use: relevant prayers/permanent unlocks, skill breakpoints and equipment across multiple PvM activities. Add numerical contexts only when their assumptions/mechanics can be verified with proportionate tests; qualitative specialist advice remains valid.

Retain independent content/source review, owned-alternative checks, provisional/access handling and acquisition trade-offs per upgrade. Add optional advanced goals only when supported outcomes justify them; do not implement the whole example goal list. Further expansion follows observed usefulness and sustainable maintenance rather than a preset 35–45-decision target.

## P5 — Usefulness trial and distribution readiness (not started)

Reuse T4's safety checks and recorded maintenance rehearsal; do not repeat a simulation just to complete a checklist. Development need not wait for unavailable user testing, but release claims need evidence.

- Check real-client observation accuracy, numerical assumptions, save/restart and character/profile isolation, plus secondary goals and activity skip/restore.
- Try five contrasting scenarios, including no-goal/provisional, declined-CG, post-Bowfa and established-raider contexts. Aim for four of five actionable or correctly qualified/abstaining results and three distinct recommendations judged useful. A provisional result counts only if its uncertainty and next confirmation are understood. Record unexercised checks as pending.
- Zero critical ownership/access/isolation errors and no known incorrect numerical claims before release. Fix concrete defects; do not convert pending availability into fabricated passes.
- Review source freshness and real maintenance effort; reduce coverage if upkeep is disproportionate.
- Prepare licence/notices, privacy/coverage/install documentation, metadata/icon and supported build. Retain the approved product name and choose a distinct Plugin Hub manifest identifier.
- Plugin Hub submission requires explicit publication authorisation; this roadmap grants none.

## Next Codex assignment

Read AGENTS.md, README.md, docs/DESIGN.md and TASKS.md from the latest upgrade-first documentation branch/commit. Verify current main and PR heads, then combine those documents with the existing PR #5 implementation without merging or closing earlier PRs. Implement **P4a only**: the 3–4-upgrade vertical slice, no-goal/provisional recommendations, bounded account-aware comparisons and acquisition details. Reuse the existing architecture and broad content; meet P4a's numerical, UI and regression criteria. Deliver one coherent PR with actual validation results, synthetic walkthroughs and pending PC checks. Stop before P4b and publication.

## Documentation-direction review — 12 October 2026

This update changes only README.md, AGENTS.md, docs/DESIGN.md and TASKS.md. No application code, catalogue data, tests or dependencies are changed. Repository state and PR #5 were checked rather than presumed merged; #1–#4 remain open. The P1–P3 results/PC records and historical T1–T4 records below are preserved, including pending checks and the limited user-reported flow.

Consistency review: both numeric examples share one small powered-magic model; Zaryte and Doom remain bounded contextual/route cases. Reference loadout inputs are explicitly additional work because current snapshots are item-ID sets. Value/certainty/access are separate, and goal-count ranking is deliberately replaced. P4a is one delivery/review, followed by evidence-led expansion, with no new framework or infrastructure. Numeric validation and new access-source checks are implementation work; no product-architecture decision remains blocking. Application tests are not rerun for this documentation-only update; the historical 75-test result remains unchanged.

## Historical T1–T4 records (preserved; superseded task instructions)

The following text records the earlier task snapshots verbatim. Its CoX-only scope and delivery gates are historical; the P1–P5 plan above is active.

# Initial tasks

Updated 12 October 2026. Only this file tracks initial work. **T1 passed static review with limited user-performed PC checks recorded below; T2's catalogue and static verification are complete; T3's local recommendation slice is implemented, with its live checks pending. T4 is prepared with user testing pending; T5 has not started.** Petter has approved the public repository `PetterHL/ironpath`; the repository and documentation foundation are established on GitHub. Independent development is agreed; competitor benchmarking is optional.

## T1 — Small account and value experiment (ready for code review; in-game validation in progress)

**Outcome:** one narrow RuneLite development build that demonstrates reliable account observations and understandable recommendations.

- In the approved repository, add the current official example-plugin scaffold. Keep original notices; use the supported Java target and one Gradle project.
- Capture real skill levels, inventory/equipment, observed bank and one relevant quest/unlock into an immutable snapshot. Display only necessary facts and freshness in a basic sidebar. Treat missing data as unknown.
- Use three to six independently reviewed sample decisions in a pure Java evaluator. Do not copy a competitor's catalogue. Synthetic rule fixtures are acceptable for logic tests but must be labelled and must not appear as real advice.
- Exercise five scenarios: bank unopened; midgame player who declines CG; post-Bowfa raider; advanced account with a high-end item but remaining gaps; and a consumed unlock/variant plus character-switch case. Separate lifecycle regression tests from product judgement.
- Make the basic flow obvious: open the sidebar, observe the bank when needed, read a few suggestions and inspect why/alternatives. Record the account evidence and reasoning behind each suggestion. Do not require using Iron Compass or measuring superiority over another plugin.
- Verify local preference-storage support before adding persistent account facts. Bank observations remain memory-only.

**Done:** `gradlew test` and `gradlew build` pass (or exact blockers are reported); unknown bank and character isolation tests pass; no network/upload path exists in this plugin; live checks are explicitly passed or pending. Produce at least three concrete examples of understandable, account-specific advice across the five scenarios, including an established raider. Do not claim live usability testing when no game session is available.

### T1 results — 12 October 2026

Code review: [PR #1 — Implement T1 local account observations and CoX sidebar](https://github.com/PetterHL/ironpath/pull/1), branch `t1-account-advisor`.

- Implemented the official example-plugin scaffold at `46e14f2c448fab6cbc599df1015b9631dcf1fc52` with original notices, Gradle 8.10, Java release 11 and one Gradle project. Current official RuneLite APIs were checked at `42a6f17a6a2e8e478aa763890ecd0181a59dad38`; the build resolved `latest.release` to `1.13.1`. There are no extra runtime dependencies; Mockito is test-only.
- Added immutable real-level, inventory/equipment, session-bank and Rigour/Augury unlock observations with time/provenance. Only a visible bank following a bank event is sampled; a stale cached container alone is insufficient. Zero-quantity placeholders are excluded. Missing data remains unknown; positive possession and permanent unlocks are separate.
- Added five source-reviewed samples in bundled `catalog.json`: read an observed dexterous scroll, read an observed arcane scroll, train Prayer for unlocked Rigour, train Prayer for unlocked Augury, and plan ordinary team CoX with recognised observed weapons. The evaluator is pure Java with deterministic authored ordering and at most three diverse suggestions; duplicate Prayer-training actions are suppressed. These are limited original preparation explanations, not a reviewed full progression catalogue or an expert/player endorsement. No T2 content expansion was performed.
- Full current Wiki pages were fetched for the limited mechanic/variant review. Source records include pinned revision URLs, author/type, access and review dates, reuse basis and affected sample IDs. No competitor catalogue, Wiki prose/tables, images or guide dataset was copied. The sidebar exposes matching evidence, why, access distinctions, alternatives, trade-offs, qualitative effort, coverage, freshness and source links.
- Raw observations and identity remain memory-only. No preference/manual-fact persistence was added. Supported local storage was verified in RuneLite's `Plugin.getPluginDirectory()` → `.runelite/plugin-data/[internalName]` and `Filepath.joinSegment`/reader/writer APIs; future character preferences must use that local path and character/profile identity rather than synced configuration. `internalName` is set. There are no HTTP clients, account lookups, uploads, runtime scraping, downloaded rules, reflection, subprocesses or gameplay input in production code. Browser source links are fixed, user-initiated and carry no account state.
- All state clears on lifecycle/account/profile transitions, unsupported modes/worlds and disable; region loading also clears conservatively. Client callbacks and queued Swing views check generation/revision, current character/profile identity and current snapshot before applying results. Event changes are coalesced at the next server tick; idle ticks do not reevaluate the catalogue.

**Automated validation:** `gradlew test` and `gradlew build` passed together on JDK `11.0.32.1`; **23 tests, zero failures**. Product fixtures and mocked RuneLite lifecycle tests are separate. Tests cover unopened banks/unknown completion, decline-CG alternatives, post-Bowfa and high-end accounts, consumed unlocks/spare scrolls, low-level scroll reading, inactive gear/components, individual wield levels, immutable evidence, three-valued conditions, unreviewed/invalid catalogue rejection, placeholders, cached-bank freshness, all game-state transitions, character/profile changes, changed identity without an event, unsupported modes/worlds, disable, stale result/view rejection and irrelevant-varbit coalescing. Three additional synthetic checks cover empty-result explanations: an uncharged staff with otherwise supported weapons and completed prayer samples, unknown bank/skill evidence, and crystal-package/individual wield-level coverage. Observing the supported charged form enables the existing CoX sample. These are synthetic observations, not captured accounts or in-game tests.

**User-performed Windows validation — 12 October 2026:** Petter reported `BUILD SUCCESSFUL` for the local `gradlew.bat test build` command, then logged into the development client with a Jagex Account. A supplied sidebar screenshot confirms that Ironpath opens and displays `Bank unknown` before the user has opened the bank. These checks were performed by the user on his PC; Codex did not interact with the game. No screenshot, account identifier, credential, bank content or raw account observation is stored in this repository. Displayed skill/unlock accuracy still needs comparison with the game UI.

| Manual check | Status | Evidence / limitation |
|---|---|---|
| Windows local build/test command | Passed, user reported | `BUILD SUCCESSFUL`; local detailed test output was not supplied. |
| Jagex Account development login and sidebar opening | Passed, user performed | User reported login; supplied image shows the active Ironpath sidebar. |
| Bank remains unknown before first opening | Passed, user performed | User confirmed bank had not been opened; sidebar displays `Bank unknown`. |
| First bank opening updates observation status | Passed, user performed | A subsequent sidebar image displays `Bank last observed this session`. This establishes the status transition, not full item-capture or close/filter accuracy. |
| Positive team CoX suggestion is displayed | Passed, user performed | A later sidebar image shows `Plan ordinary team CoX with your observed weapons` after the earlier empty result. Actual item-capture, provenance accuracy and usefulness still need user verification. |
| Expanded recommendation evidence renders | Passed, user performed | Supplied detail images show the expected three weapon labels, distinct storage provenance, observation times, preparation baseline, access distinction, alternative and trade-off text. Actual storage locations still need user confirmation; this is a rendering check, not an independent account-accuracy pass. |
| Empty-result explanation | Failed in initial UI; correction awaiting user retest | After the bank status updated, the generic empty state did not identify the unmet sample condition. Added a `Why no suggestion` expansion using the same pure evaluator checks; no catalogue or recommendation eligibility was expanded. |
| Displayed skill/unlock accuracy and remaining real-client behavior | Pending | Rendering alone does not establish that the values match the game. |

The following representative profiles remain synthetic; their full product/usefulness checks are separate from the initial manual checks above.

| Synthetic scenario | Concrete evidence and supported result | Product judgement / in-game status |
|---|---|---|
| Bank unopened | No observed weapons or scrolls; unlocks unknown. Abstain and explain bank observation rather than invent missing gear. | Pending |
| Midgame player declines CG | Recognised whip + rune crossbow + charged trident, 80+ style baseline and 70 Prayer. Suggest planning ordinary team CoX with these observed options; CG is not required. Access, supplies, armour and team agreement remain unverified. | Pending |
| Post-Bowfa raider | Corrupted Bowfa + active crystal armour + whip/trident, both prayer unlocks confirmed, 74 Prayer. Suggest training to 77 for already unlocked Augury, alongside team planning; no extra arcane scroll or basic ranged grind. | Pending |
| Advanced account with remaining gap | Observed fang + twisted bow + charged Shadow, 90 style levels, unlocked Rigour/Augury and 72 Prayer. Prioritise 74 Prayer for Rigour and team planning; suppress duplicate training advice and recognise the owned high-end weapons. | Pending |
| Consumed unlock/variant and character switch | Unlocked prayers remain completed when scrolls are absent or spare scrolls remain. Uncharged/inactive/component forms do not satisfy the weapon baseline. Switching characters clears bank/unlocks and rejects the old generation before new observations. | Pending |

**Cloud constraints:** the initial shell clone could not reach `proxy:8080`; enabling the shell's supported network permission resolved it. Without that permission, even Gradle's offline task listing failed local daemon IPC with `java.net.SocketException: Operation not permitted`; commands need the same permission for local sockets. The preinstalled Java 21 runtime failed compilation with `does not provide the required capabilities: [JAVA_COMPILER]`. A temporary Temurin JDK 11 resolved that. Its initial Maven download failed with `PKIX path building failed`; using the configured system CA store `/etc/ssl/certs/java/cacerts` resolved it while keeping TLS verification enabled. No outstanding test/build blocker remains. Swing tests emitted non-fatal `Fontconfig error: No writable cache directories`. `DISPLAY` is unset and no authenticated game session was available; the development client was not launched and no in-game checks were performed.

Reproduction command used in Cloud (temporary paths are environment-specific; PC commands are in README):

```sh
JAVA_HOME=/tmp/ironpath-jdk11 GRADLE_USER_HOME=/workspace/.gradle ./gradlew \
  -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts \
  -Dhttps.proxyHost=proxy -Dhttps.proxyPort=8080 \
  -Dhttp.proxyHost=proxy -Dhttp.proxyPort=8080 test build --no-daemon
```

**Pending real-client checks:** login initialisation and displayed skill/unlock accuracy; bank item-capture, close/filter freshness and observation times; placeholders and supported charged/cosmetic variants; the new empty-result explanation and the positive suggestion's expanded matching evidence; consumed unlocks; logout, hop, reconnect and region-loading clears; two characters and RuneLite profile changes; unsupported modes/worlds; disable/re-enable; source links and recommendation usefulness. Only the manual passes explicitly recorded above are established. No general game accuracy pass, player usefulness judgement or Hub approval is claimed. See README for simple Windows/macOS/Linux build and launch commands and Jagex Account login guidance. T2 remains unstarted; no Hub submission.

## T2 — Review the bounded catalogue (complete for static review; real-client checks pending)

Depends on T1's working foundation. Select at most twelve decisions within the CoX scope; at least four must help established accounts. Check current alternatives and hard requirements, write original explanations and effort assumptions, and attach source/revision/review/licence metadata. Do not allow one guide's opinion to become a hard requirement. Validate IDs, references, `all`/`any` conditions, cycles and missing evidence. Finish five representative account fixtures with expected explanations and alternatives, not just exact ordering snapshots.

### T2 results — 12 October 2026

Branch `t2-cox-catalogue` builds on PR #1's unmerged head `ae9af8758016a7048302d136cffba0ca20e821ff`. T1 passed static review according to Petter; **T1 has not been live-tested**. Its lifecycle implementation and pending manual checks are retained.

- Authored **nine decisions**, keeping the five stable T1 IDs and adding four choices useful to established/post-Bowfa accounts: assemble an observed magic fang/seas-trident upgrade; compare an owned lance for Olm; agree team defence-reduction coverage; review an owned occult switch when a fury is currently equipped. These are bounded preparation choices, not a comprehensive acquisition route, universal gear order or player endorsement. Team coverage replaces the overlapping broad team-planning prompt.
- Reviewed full current pages for the selected mechanics, alternatives and variants. Bundled **39 source records** (38 Wiki revisions and one pinned official RuneLite item-definition file) carry author/type, exact revision URL, accessed/reviewed dates, licence/reuse basis and affected IDs. Each of **56 exact item IDs** has evidence references. Source/reuse findings are recorded in `docs/RESEARCH.md`; no competitor catalogue, guide prose, equipment tables or images were imported.
- Checked alternatives including the Eye of Ayak, dragon hunter wand, Sanguinesti staff/Shadow, scythe/Soulreaper axe and elder maul/BGS/DWH. Owned competing magic forms, including empty staffs, suppress redundant trident assembly. The wand is not counted as a ready powered spell setup. Owned scythe/Soulreaper axe suppresses the narrow lance comparison. Cosmetic and enhanced equivalences are explicit; inactive gear and components do not establish usable weapons. Unlisted variants remain outside coverage.
- Added only bounded observation conditions (`all`/`any`, levels, named exact-item sets, current equipment and the existing two unlocks) and completion/dependency gates. Unknown completion withholds an action; an unread container never proves an item absent. Confirmed Prayer unlocks remain different from spare scrolls. The only hard decision dependencies are Prayer training's permanent unlocks. Planning baselines, crystal kit preferences and team-role discussion remain recommendations; raid access, lance miniquest sections, charges, supplies and spell setups are explicitly unverified where not captured.
- Catalogue loading fails closed for invalid/duplicate IDs, unsupported predicates/schema, malformed conditions, bad item/source/decision references, missing evidence/review/reuse fields, inconsistent source links/revisions/dates, invalid completion definitions and hard-dependency cycles. The existing authored comparator and three-choice/family limit remain; no goal ranking, nearest-prerequisite UI, skip controls, manual confirmations, persistence or other T3 features were added. Reader/lifecycle code and dependencies are unchanged; sidebar changes only correct coverage and explanation text.

**Automated validation:** `gradlew test build --no-daemon` passed on Temurin JDK `11.0.32.1`, with **33 tests, zero failures**, retaining all 20 T1 tests. Five new representative product fixtures assert matching evidence, expected reasoning and alternatives; four decision-boundary and four catalogue-validation tests cover the new semantics and failure modes. `git diff --check` passes. RuneLite resolves to `1.13.1`. The same supported shell network permission, system CA store and proxy arguments documented under T1 were used. Non-fatal Fontconfig cache and Gradle deprecation warnings remain.

| Synthetic T2 fixture | Expected reasoning and alternatives |
|---|---|
| Bank unopened; then positive fang/chisel/trident in inventory | Abstain while competing-weapon completion is unknown. Positive parts do not prove another weapon is absent from the unopened bank. |
| Midgame, 85 combat skills/70 Prayer; whip, rune crossbow, charged seas trident; no prayer unlocks | Plan ordinary team CoX using the observed crossbow, with CG optional. Kourend access and complete raid preparation remain unverified; missing scrolls do not become acquisition advice. |
| Post-Bowfa, 90 combat skills/80 Prayer/80 Crafting; active crystal kit, seas trident, magic fang and chisel | Suggest assembling the owned magic upgrade and team planning. Explain uncharging, scale upkeep and keeping the seas trident as an alternative; no new ranged grind or contrived low-Prayer gap. |
| Established Bowfa raider, 93 combat skills/80 Prayer; swamp trident, DWH and occult; fury currently equipped | Review the owned occult switch and agree defence-reduction coverage. Simpler hybrid switching and teammate coverage are explicit alternatives; current equipment does not prove the usual raid kit is deficient. |
| Established account, 95 combat skills/90 Prayer; Bowfa kit, fang/lance, charged Ayak, BGS; ornamented occult equipped | Compare lance for Olm and agree coverage using the observed BGS/Ayak kit. Keep room-specific alternatives and unknown Barbarian Training visible. Suppress redundant trident assembly and occult advice. |

**Limitations and remaining manual checks:** these fixtures are synthetic, not captured accounts, player judgement or RuneLite testing. No development client was launched; no authenticated game session was available. All T1 real-client checks remain pending. Additionally check fang assembly/uncharge and enhanced forms; default active versus inactive crystal pieces and supported Bowfa colours; empty versus charged Ayak/Sanguinesti/Shadow; ornamented neck/reduction gear; current equipment versus actual raid switches; and whether the four established-account prompts are useful or repetitive. Gear/skill baselines do not certify solo/Challenge Mode readiness or raid access. Unsupported items/variants need external review rather than inferred equivalence. No Plugin Hub submission, T3 implementation or T4 usefulness pass is claimed.

## T3 — Complete the local recommendation slice (ready for review; in-game checks pending)

Implement deterministic ranking, up to three diverse choices, optional single goal, nearest prerequisite, skip/restore and reversible manual confirmations. Keep one sidebar. Verify owned gear removes redundant grinds, source links carry no account data, skipped requirements do not become completed, unknowns do not fabricate advice, and preferences survive restart without leaking between characters.

### T3 results — 12 October 2026

Code review: [PR #3 — Implement T3 local goals, ranking and isolated preferences](https://github.com/PetterHL/ironpath/pull/3), stacked on PR #2.

Branch `t3-local-advisor` builds on T2's `e52728d5313552fd8b0bdaa5eefdcbaa4be00915`.
It also integrates the existing T1 follow-up diagnostics and user-performed check
records from `f224f410f23f8d4f33310a4520aded9e84518b23`. T1/T2 results above describe
those earlier task snapshots; their historical statements about later tasks do not
set the current T3 status. Petter authorised proceeding after reporting that T2
appears to work. That general report is not a pass for specific T2 or T3 scenarios.

- Implemented a pure comparator of selected-goal relevance, contextual benefit,
  authored effort band and stable ID; no efficiency score or time/DPS estimates.
  At most three choices and one per family remain. Owned competing gear suppresses
  redundant assembly/comparisons; specific team coordination takes precedence over
  broad team planning. No-goal mode works; only one optional goal is selected.
- Added four bounded goals: ordinary team CoX learning/improvement, meeting
  Rigour/Augury unlock/use levels, and reviewing an owned-parts swamp trident
  upgrade. The sidebar separates confirmed, missing and unknown prerequisites,
  nearest confirmed missing prerequisite, and the next supported action. A locked
  prayer is still a blocked goal even if Prayer is already high enough. No covered
  acquisition route means abstaining and asking for observation/confirmation,
  not fabricated grind advice. Preparation remains distinct from game access.
- Added skip/restore for all reviewed actions, including prerequisites not currently
  offered as cards. A skipped unlock blocks training until restored or actually
  confirmed complete; skipping never counts as completion. Unrelated viable CoX
  actions can still be shown while one goal is blocked.
- Added reversible exact-item and prayer-unlock confirmations with explicit manual
  provenance/time, individual clear buttons and reset controls in the same sidebar.
  Manual ownership can suppress redundant work and inform planning, but cannot
  create charged/equipped/usable weapon evidence or observed assembly supplies.
  Readable unlock varbits are authoritative; positive observed possession overrides
  conflicting manual absence. Raw snapshots are never modified by confirmations.
- Persisted only goal, skipped IDs and explicit confirmation values/times in local
  schema-1 JSON using supported `Plugin.getPluginDirectory()` / `Filepath` APIs.
  A SHA-256 key isolates configuration profile + RuneScape profile + character;
  no username/raw identity or automatic account observation is serialized. Writes
  use a temporary file and atomic replace with a supported non-atomic fallback.
  Serial worker IO preserves accepted save/load order across disable/re-enable;
  client-thread callbacks and UI actions check current identity/generation before
  applying results. Corrupt/unsupported files disable editing; save failures are
  visible, never falsely reported as saved. No ConfigManager writes or uploads.
- Plugin version is **0.2.0**. Bundled schema **3**, knowledge **t3-1**, replaces
  legacy priority metadata with the small reviewed effort bands. The same nine
  reviewed decisions, 56 exact item IDs, 39 source records and source review dates
  remain; T3 introduces no new source claim, rule-download path or catalogue expansion.
  Current official RuneLite 1.13.1, Java release 11 and the existing template/notices
  remain supported. T4/T5 and Plugin Hub submission were not started.

**Automated validation:** `gradlew test build --no-daemon` on Temurin JDK
`11.0.32.1`: **58 tests, zero failures**. This includes the original T1/T2 fixtures,
T1 abstention explanations, goal ranking/diversity, skipped prerequisite versus
actual completion, unknown ownership, manually owned alternatives, unusable manual
gear, unlock precedence/provenance, fixed source URLs, real temporary-file restart
round trips, character/profile separation, corrupt/oversized/retired preferences,
serialized IO recovery, actual Swing control listeners with mocked client events,
delayed load/action/save callbacks, disable/re-enable, and visible write failure.
These tests use synthetic facts and isolated temporary storage, not live accounts.
`git diff --check` passes.

**Cloud constraints:** the documented network permission, proxy and system CA
arguments from T1 were used; RuneLite remains 1.13.1. No outstanding test/build
blocker. Non-fatal Fontconfig cache and Gradle deprecation warnings remain.
`DISPLAY` is unset and no authenticated game session is available in Cloud, so
Codex did not launch or interact with RuneLite in-game.

| T3 real-client check | Status |
|---|---|
| No-goal / single-goal selection and clear; readable ranking/evidence | Pending |
| Goal prerequisites and nearest missing requirement match game/manual facts | Pending |
| Skip required unlock, blocked route, restore without false completion | Pending |
| Manual positive/negative, provenance, observed-positive precedence, clear/reset | Pending |
| PC file write and restart restore; bank remains unknown until reopened | Pending |
| Two characters and RuneLite profiles; logout/hop/unsupported-world isolation | Pending |
| Disable/re-enable, source buttons and catalogue usefulness | Pending |

PC build/launch and a short T3 check sequence are in `README.md`. Wait for
**Preferences saved locally** before restarting. No T3 in-game pass or T4 user-trial
result is claimed. Earlier limited T1 user-performed passes remain recorded above.


### T3 review fixes — 12 October 2026

Fixed the two findings from reviewed commit `3889580` on `t3-local-advisor`,
updating the existing PR #3:

- `LocalPreferences.write` now calls supported `Filepath.createDirectories()` on
  the destination parent before creating its temporary file. A regression starts
  with a nonexistent plugin subdirectory, saves for the first time and reloads
  the goal/manual confirmation through a fresh store instance.
- CoX goal status now recognises an available same-family preparation alternative
  when defence-reduction coordination replaces broad team planning. Confirmed
  missing, unknown and skipped prerequisites retain their existing status/guards;
  the alternative does not certify raid access or complete the goal.

Both bug regressions failed against `3889580` before the fixes. The added control
fixture for missing/unknown/skipped prerequisites passed before and after the
fixes. Full `gradlew test build --no-daemon` passes on the same supported JDK 11
and RuneLite setup: **58 tests, zero failures**; `git diff --check` passes. No
outstanding build blocker. These are synthetic logic/temporary-file tests;
**no in-game testing was performed**. Catalogue, privacy/isolation rules and
schemas are unchanged. T4/T5 remain unstarted.

**Short PC checklist — all pending:** update `t3-local-advisor` and use README's
build/launch commands, then:

1. With the client closed, temporarily rename/back up only
   `.runelite/plugin-data/ironpath-advisor` if it exists to exercise a fresh
   directory. Launch, save a goal/manual confirmation, wait for **Preferences
   saved locally**, restart and verify the choices return. Bank observations
   should remain unknown until reopened. Preserve the backup.
2. Select ordinary team CoX with a positively observed, supported three-style kit
   and defence-reduction weapon. If the coordination card replaces broad planning,
   the goal should say **Available preparation action** and name that next action.
   Skip the selected goal action and restore it; its blocked status should reverse.
3. Switch character and RuneLite profile: each should show its own choices. Return
   to the original character/profile and verify its saved choices return without
   carrying over bank observations.

## T4 — Small user and maintenance trial (prepared; user testing pending)

Test with Petter and, if available and explicitly invited, one or two experienced Ironmen. No unsolicited outreach. Check whether they can find a recommendation without assistance, understand its explanation, inspect an alternative and skip a grind. Acceptance target: at least four of five scenarios yield an actionable or correctly abstaining result, zero critical ownership/access errors, and at least three recommendations the player considers useful for the supplied account. These are proposed acceptance criteria, not measured results. Simulate a relevant source change and time the review/update process. Do not automate discovery unless it saves observed repeated work.

### T4 PC trial — instructions and pending results

Baseline: `t3-local-advisor` at **`b3a93d9aa60f7661e49b970fc3b9272e7ad2df12`**,
including the first-save directory and same-family CoX goal-status fixes. Petter
reports T3 passed static review; specific T3 real-client checks remain pending.
The trial branch is `t4-user-maintenance-trial`. It changes only this task file;
plugin **0.2.0**, knowledge **t3-1**, schema **3** and the shipped catalogue remain
unchanged. Earlier T1–T3 sections describe their historical task snapshots.

Allow about **20–30 minutes**, plus restart/login time. Use JDK 11 and a standard
Ironman on an ordinary permanent members world. Use anonymous account/profile
labels (A/B, P/Q) in the results; record only the few facts needed to judge advice,
not names, IDs, credentials, screenshots or bank dumps. Include an established
raiding account. If a boundary cannot be exercised on the available accounts,
record it as **unexercised / pending**, not passed; do not grind or acquire items
just for this trial. Do not invite or contact anyone through Codex.

Windows PowerShell, inside an existing `ironpath` clone with the client closed:

```powershell
git fetch origin
git switch t4-user-maintenance-trial
git pull --ff-only
git merge-base --is-ancestor b3a93d9 HEAD
if ($LASTEXITCODE -ne 0) { throw "Required T3 fixes are missing" }
java -version
javac -version
.\gradlew.bat test build
if ($LASTEXITCODE -ne 0) { throw "Fix the build before the PC trial" }
# Perform the first manual step below before launching.
.\gradlew.bat run
```

**First manual step, before `run`:** with RuneLite closed, rename
`%USERPROFILE%\.runelite\plugin-data\ironpath-advisor` to a unique backup name
if it exists. Preserve it and leave other RuneLite files alone. If it does not
exist, leave it absent. This exercises `b3a93d9`'s first save into a missing
directory. After the trial, close the client before moving the trial directory
aside and restoring your backup. README contains Jagex Account development-login
guidance; never include local credentials in the results.

Run these five scenarios in order. Expected results describe the existing build,
not new features or promises of full raid readiness.

1. **Find useful advice.** Log in, wait two server ticks and for local preferences
   to load. Before opening the bank, check **Bank unknown**. Open the bank, then
   use **No goal**. Without help locating a card, choose a suggestion, explain why
   it fits, inspect **Why and alternatives**, and decide a next action. Record
   seconds to find it and any assistance. Repeat for up to three distinct useful
   recommendations across supplied accounts, including an established raider.
   **Expected:** at most three different families; matching levels/items and
   provenance agree with the game; owned alternatives avoid redundant work. Unknown
   storage cannot prove missing gear. Empty results explain limits; CG stays optional
   and preparation never certifies access. A source button opens a pinned reference
   without account data in the URL.
2. **Goals and alternatives.** Set ordinary team CoX, expand **Goal prerequisites
   and sources**, then try Rigour/Augury or the owned-parts upgrade and **Clear goal**.
   With a supported observed three-style kit and defence-reduction weapon, inspect
   the coordination card replacing broad planning. **Expected:** one optional goal;
   that CoX alternative says **Available preparation action** and names coordination.
   A genuinely missing prerequisite is separate from unknown ownership; no uncovered
   acquisition grind is invented. Confirmed outcomes suppress redundant work;
   clearing the goal returns to no-goal advice.
3. **Skip and restore.** Skip a card and restore it. For a still-locked prayer goal,
   also skip its unlock action under **Skips and manual confirmations**, then restore.
   **Expected:** the card disappears and returns only if still eligible; a skipped
   required unlock blocks the route without marking it unlocked or offering dependent
   training. Restoring removes the skip block, leaving actual missing/unknown facts
   intact. If both prayers are already unlocked, the locked-route subcheck stays pending.
4. **Manual confirmations.** Temporarily set a supported item's positive and negative
   confirmation, inspect its manual provenance/time, then clear it. Try manual absence
   for a positively observed item and the opposite of a readable prayer unlock.
   **Expected:** confirmations are reversible planning assumptions; positive observed
   possession and readable unlock varbits take precedence. Manual ownership cannot
   create charged, equipped or usable gear. Clear/reset restores live or unknown
   state. After checking **Reset all planning choices**, set one goal, one skip and
   one harmless temporary confirmation for scenario 5.
5. **First save, restart and isolation.** Wait for **Preferences saved locally**;
   confirm a JSON file now exists under the fresh plugin directory. Restart and
   return to A/P. Before reopening the bank, check restored choices and unknown bank.
   Switch to B/P, then A/Q; set different choices and wait for each save. Return to
   each combination, then A/P. **Expected:** only its own goal/skip/manual fact returns;
   bank/live observations clear at restart and character/profile transitions. Editing
   waits for identity/preferences; save errors are visible. The local JSON contains
   choices and manual values/times, no bank, levels, live unlocks or raw identity.
   Keep it private and report only field names. Missing B or Q means isolation pending.

Only mark a scenario passed after its applicable checks are exercised and the
player judges the result actionable or correctly abstaining. An abstention needs
a correct reason, not just an empty list. Pending subchecks do not count toward
the four-of-five gate. False ownership, a false access/readiness claim, or any
cross-character/profile leakage is a critical error and blocks acceptance.

| Scenario | Status | Account/profile label; actual result; time/assistance; mismatch or unexercised check |
|---|---|---|
| 1. Recommendation usefulness | **Pending user testing** | Awaiting Petter's results |
| 2. Goals / alternatives | **Pending user testing** | Awaiting Petter's results |
| 3. Skip / restore | **Pending user testing** | Awaiting Petter's results |
| 4. Manual confirmations | **Pending user testing** | Awaiting Petter's results |
| 5. First save / restart / character and profile isolation | **Pending user testing** | Awaiting Petter's results |

Record three **distinct** recommendations considered useful, with at least one
from an established raiding account. Showing the same card again is not another
usefulness vote; synthetic fixtures are not player evidence.

| Recommendation title / ID | Anonymous account context | Useful? Why; understood alternative; chosen next action |
|---|---|---|
| Pending 1 | Awaiting user | Awaiting user |
| Pending 2 | Awaiting user | Awaiting user |
| Pending 3 | Awaiting user | Awaiting user |

**Result fields:** test date/build commit/JDK: pending; scenarios actionable or
correctly abstaining: pending / 5; critical ownership/access/isolation errors:
pending (do not assume zero); distinct useful recommendations: pending; overall
T4 acceptance: **pending user results**. Report failures with the control/action,
expected and actual result and minimal relevant facts. This five-scenario trial
does not silently pass other outstanding T1–T3 lifecycle/variant checks.

### T4 maintenance rehearsal — 12 October 2026

**Explicit simulation, not a real source/game update:** candidate `SIM-T4-001`
assumes wearing normal and ornamented occult necklaces changes from 70 to 80
Magic, with IDs/stats unchanged. No new source was discovered or endorsed. The
rehearsal used an independent temporary clone of `b3a93d9` at
`/tmp/ironpath-t4-rehearsal-be53n8lu`, with a local simulation ticket and temporary
tests. It was never pushed or used in a game client.

**Impact review:** source register entries `occult` (revision `15314514`) and
`occult-or` (`15187781`) both point to **`review-occult-switch`**, the only affected
decision. Change its level predicate, access/applicability text and the two neck
records (IDs `12002`, `19720`) together. Check the charged-weapon matcher, manual
ownership semantics, equipped completion and fury/ornamented alternatives.
Other eight decisions, goals, dependency edges, ranking and item IDs are unchanged;
below-level accounts should lose only the occult prompt. No Slayer/acquisition
requirement or new grind follows from this hypothetical wield change.

The temporary candidate changed those fields, marked its explanation and both
source reuse notes **SIMULATION ONLY**, and bumped only its knowledge version to
`t4-rehearsal-only`. Schema 3, stable IDs and the 12 October review date remained.
Pinned real URLs/revisions were retained explicitly as **baseline evidence only**;
they do not support the invented requirement. A real update would require verifying
released mechanics and recording actual new revisions before accepting/publishing it.

**Validation and rollback:** Temurin JDK `11.0.32.1`, RuneLite `1.13.1`, existing
Gradle wrapper and the supported Cloud network/proxy/system-CA setup from T1:

- Unmodified temporary baseline: `gradlew test build --no-daemon` passed,
  **58 tests, zero failures**.
- Two temporary simulation tests failed against the baseline as expected: advice
  was still offered at 79 Magic and metadata still required 70.
- Candidate validation covered normal/ornamented forms below/at 80, unknown Magic
  and equipment, equipped completion, manual ownership failing to create usable
  gear, consistent requirement text/metadata and independent Prayer advice. The
  first candidate run exposed one **fixture mistake**: 79 Magic does not meet the
  unchanged 80-Magic team baseline. Corrected that control to an already unlocked
  Rigour/73-Prayer gap; no production code change was needed. Full candidate
  `test build --no-daemon` then passed **60 tests, zero failures**, including all
  original catalogue rejection tests and five representative account fixtures.
- Reviewed the candidate diff: only one decision, two neck records, two simulation
  source notes and the temporary knowledge version changed; `git diff --check`
  passed. Restored the catalogue from `b3a93d9`, removed the temporary fixture,
  and ran `CatalogValidationTest` plus `CoxAccountFixturesTest`: **10 tests,
  zero failures**. The temporary clone has no tracked diff after rollback.

**Measured effort:** agent elapsed time, measured with a monotonic clock for
review/update windows and build-log timestamps for the candidate runs. These are
observed rehearsal timings, not an estimate of human labour:

| Rehearsal phase | Measured elapsed time |
|---|---|
| Impact/source-link/requirement review | 31.8 seconds |
| Temporary fixture preparation and cold baseline build wait (overlapping work) | 145.2 seconds; Gradle itself reported 1m 34s |
| Expected-failure check and candidate catalogue update | 66.8 seconds |
| Inspect/correct the invalid control fixture | 72.1 seconds |
| Two candidate validation runs | 19.1 seconds total (9.3 failed, 9.8 passed) |
| Rollback and targeted validation command | 7.0 seconds |

Selected rehearsal windows total **342.0 seconds (5m 42s)**: 170.7 seconds for
impact review/update/fixture correction, 26.1 seconds for candidate validation
and rollback, plus 145.2 seconds for fixture preparation/cold baseline wait.
JDK download/setup, PC-checklist writing, report-writing gaps and GitHub handoff
are excluded. Raw timing/build logs and the candidate patch remain under `/tmp`
for this session; the durable evidence is this report. No live source retrieval,
expert/player review or release submission was timed. One simulated change cannot
validate the proposed 20–40 minute weekly triage budget or justify a watcher;
measure real repeated reviews before adding automation.

**Shipped result:** the working catalogue and rolled-back copy both match the
baseline byte for byte, SHA-256
`470268a6ed539afaa6932df6cd813498b00dec63dcda157cd90be8176560a84d`.
Only `TASKS.md` differs from `b3a93d9` in the trial branch. The nine decisions,
56 item IDs, 39 sources, plugin version and knowledge/schema versions are unchanged.
No development client was launched. **Maintenance rehearsal complete**;
**user trial and T4 acceptance remain pending**. No source watcher, new feature,
outreach, T5 work or Plugin Hub submission is part of this preparation.


</details>
