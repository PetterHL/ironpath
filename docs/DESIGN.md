# Product and implementation decisions

Approved direction · updated 12 October 2026 · P4a implemented; review and real-client validation pending

## Product

Ironpath is an **automatic, account-aware Ironman PvM upgrade advisor**. Its central question is:

**Which upgrades are worth pursuing for my account, what difference will they make, and how do I obtain them?**

Open the sidebar to see useful upgrades inferred from supported account observations, without selecting a goal. An upgrade may be equipment, a prayer, a meaningful skill level or a permanent unlock. Missing an item is not evidence that obtaining it is worthwhile: compare its contextual value with the account's supported alternatives and the remaining work.

Goals are optional refinements. First versus efficient Inferno, first Colosseum completion, solo versus efficient solo versus CM CoX, and efficient ToB are possible future contexts, not a promised goal list for P4a. Existing supported goals remain available in secondary controls; their count does not measure usefulness. Do not infer intent, player skill, raid readiness or activity participation from ownership.

Continue independently, reusing the existing plugin. Iron Compass remains a reference, not a comparison gate. The hard part is maintaining defensible contextual advice, not collecting the most items. No owner-operated backend, paid AI, registration or runtime guide scraping is needed.

## Verified baseline and scope

On 12 October 2026, `main` was `c717927cdf4e3fd8128d28b96e05690965f5a908`; [PR #5](https://github.com/PetterHL/ironpath/pull/5) remained open and unmerged at `eb22fef97aa899bac5611b3001462dfdbebe0b80` (`p1-p3-broad-pvm`). PRs #1–#4 also remained open; #5 incorporates their implementation/records. This documentation update is based on #5. Recheck heads before implementation or integration; do not infer merge status from a PR's synthetic merge commit.

The delivered baseline is plugin 0.3.0, knowledge `p3-1`, schema 4: 16 reviewed decisions, 61 exact item IDs, 62 sources and seven goals. Its 75-test result and limited user-reported PC flow are historical evidence in TASKS.md, not validation of the design below. Detailed observation, usefulness, variant, persistence and isolation checks remain pending.

**P4a is implemented on `p4a-upgrade-advisor`, based on PR #6 at `2ef409a` (including PR #5).** It delivers the four representative outcomes and the bounded reference model described here. TASKS.md records actual test/build/oracle results and pending PC checks. No P4b implementation or publication is included. The scope was **approximately 3–4 representative upgrade experiences end to end**. Preserve useful existing content. After testing and review, P4b expands toward approximately 8–12 thoroughly researched upgrades, then follows demonstrated usefulness. These numbers budget new upgrade experiences; they neither delete the existing 16 decisions nor impose a permanent catalogue limit. The previous 35–45-decision expansion-first plan is superseded.

Keep standard Ironman/permanent main-game coverage. Defer UIM logistics, HCIM safety routing, GIM shared ownership, seasonal modes, exhaustive progression, general loadout optimisation, KC/Collection Log dependencies and complete encounter simulation.

## Sidebar experience

Use a **single column of compact cards**, initially three primary choices with a small secondary list when useful. A two-column grid wastes the narrow sidebar on wrapped titles and compressed comparisons. Retain RuneLite's normal Swing panel and visual conventions.

Each card shows an item icon (or a simple prayer/skill/unlock symbol), upgrade name, one strongest supported effect/comparison, a few relevant PvM contexts, qualitative remaining effort, and a short confidence/access label. Use supported client item imagery; do not download a new image dataset. Examples of labels: `Reference setup`, `Ownership unconfirmed`, `Needs prerequisite`. Explain labels in plain language; colour alone is insufficient.

Click through to a detail view with a clear Back control:

1. Why this matters for this account, including the observed or confirmed baseline.
2. Current versus upgraded performance for a named reference setup/target, or an explicit reason numbers are unavailable.
3. Relevant encounters, secondary benefits, owned alternatives and situations where the upgrade is marginal or worse.
4. Acquisition routes, already satisfied steps, remaining prerequisites and the nearest meaningful next step.
5. Sources, review dates, calculation assumptions and small controls to correct the reference setup or a specific unknown.

Put account observations, full diagnostics, manual confirmations, skip/restore and optional goals under secondary `Account and preferences` controls. A short bank freshness/status line may remain above cards; a long form must not. An empty or unopened bank should still allow relevant **provisional** cards when other evidence supports them. If nothing is supportable, show the specific observation/coverage gap, not invented advice.

Keep `Quick actions` separate: reading an owned scroll, assembling already owned parts or reviewing a switch must not consume the main upgrade slots. A significant prayer unlock/training outcome can remain a primary recommendation while its final administrative step is a quick action. Existing broad activity/preparation advice remains accessible under secondary advice; do not silently discard it or pretend it has a calculated upgrade benefit.

## Recommendation semantics

Preserve the existing three-valued facts: `ALL` false dominates, otherwise unknown propagates; `ANY` true dominates, otherwise unknown propagates. An unread container, or absence from all observed containers, never proves global nonownership. Positive observed possession overrides a manual absence; readable live quest/unlock facts override manual fallback. Manual ownership does not prove charges, equipped state or usability.

Evaluate **value, certainty and route access separately**:

| Result | Required treatment |
|---|---|
| Supported improvement with a confirmed gap | Explain marginal value against supported owned alternatives; show remaining route work. |
| Relevant improvement, target/alternative ownership unknown | Label provisional; say “if you do not already own …”; request only the fact that could change this recommendation. Never write “you are missing …”. |
| Relevant upgrade, access missing/unknown | Keep it eligible for a `Plan ahead` card/detail; name the nearest confirmed missing prerequisite or the access check needed. Do not label the grind available. This works without a selected goal. |
| Owned equivalent or no meaningful contextual improvement | Suppress the acquisition or explain why to defer it; do not suppress distinct special-attack utility by a universal weapon tier. |
| Unsupported baseline/mechanic | Retain a bounded qualitative explanation if justified; withhold the unsupported number. Otherwise explain coverage. |

Use a small deterministic comparator: contextual marginal-value band first; optional goal/preference relevance within that band; distinct evidenced encounter applications; then remaining-work/access band and stable ID. Certainty/access labels remain visible rather than pretending all results are equally actionable. Primary selection should retain an available useful alternative beside a longer blocked route when both are supported. Validate ordering with account examples; neither ease nor goal choice should elevate trivial actions or irrelevant upgrades.

Replace the P3 comparator's `decision.goals.size()` term and its “useful for N goals” claim. Benefits across encounters must describe distinct applications and their conditions, not count goal aliases or add DPS percentages from different targets. No weighted universal efficiency score. Source evidence should explain the value band and why an owned alternative changes it.

Group duplicate outcomes across routes into one upgrade card. A shared activity can expose additional **separate** reward outcomes in its details; collapse repeated acquisition work without dropping reward-specific value. Do not add their benefits, assume simultaneous drops, or assign a faster completion time for “farming several upgrades together”. Respect activity skips while preserving the utility of already-owned rewards and other available routes. Do not let one-activity diversity filtering hide all shared-reward information.

Effort means remaining work: skill/quest gaps, components, solo/team coordination, repeated RNG acquisition, and charging/upkeep. Show owned parts as completed planning steps without inferring quantities or resources from item-ID sets. Use qualitative bands with assumptions; no exact hours, percentage-to-drop or improvement in ordinary independent drop odds from prior dry KC.

## Trustworthy comparisons

### Reference setup and result contract

The current reader stores sets of item IDs and real levels, not complete loadouts, slot assignments, quantities, active prayers or boosts. The catalogue's `Gear` records provide wield/package checks, not combat stat vectors. Numerical comparisons therefore require an explicit small addition, not an assumption that the existing snapshot already contains all calculator inputs.

Propose one bounded reference setup from observed equipment and a reviewed template using supported owned alternatives. Use exact-ID slot/stat metadata only for supported pieces; resolve conflicting pieces with a deterministic contextual rule and a small choice when needed. Do not search every possible loadout. Display all selected slots (including deliberately empty slots), weapon, style, prayer and boost assumptions together. One confirmation and focused corrections should usually suffice. An unknown slot, unsupported bonus or incompatible setup must not silently contribute zero.

Before confirmation, any number is explicitly a **proposed reference scenario using observed levels**, not the player's actual setup. After confirmation it is still a reference estimate, not observed combat performance. Unknown inputs that cannot be bounded suppress numbers, not the whole recommendation. Default to no temporary boost and no offensive prayer with visible labels; allow a small reviewed selection, and never assume an unlocked prayer is active. Do not add a full calculator form.

Keep reference proposals/corrections in memory for P4a. Relevant equipment/level/assumption changes invalidate confirmation and recalculate; logout/profile/character changes clear it with the existing generation guards. Persisting new setup profiles is outside this slice.

Every numeric result carries baseline and candidate exact items/forms, slot stats, real and assumed effective skill levels, prayer/boost/style, target ID and phase/scaling/defence assumptions, supported and excluded mechanics, formula/model version, knowledge/source revision, and whether inputs were observed, manually confirmed or proposed. Return explicit `supported`, `conditional` or `unsupported` status with a reason. Render whole max hits, suitably rounded hit chance/DPS and distinguish percentage points from relative percent; never divide by a zero baseline or imply more accuracy than the model warrants.

### Smallest calculation decision

P4a should independently implement **one bounded powered-magic model**, shared by the occult and trident cases: reviewed ordinary seas/swamp forms, supported flat gear bonuses, a small prayer/boost selection, exact integer rounding, attack interval, standard accuracy and successful-hit distribution. No general combat engine is needed. The initial target is ordinary Barrows **Dharok, NPC 1673**, during active attacks, without raid scaling, defence reduction, venom/poison, thralls, specials, set effects or movement. Do not apply his wind weakness to non-elemental trident attacks. Wind spells remain an important qualitative competing option; improving one trident over another is not proof the trident is the best Barrows strategy.

A hand-checkable fixture: at 90 Magic, no boosts/prayer/damage bonuses, seas and swamp base max hits are **25 and 28**. The seas formula is `floor(Magic / 3) - 5`, swamp `floor(Magic / 3) - 2` within the supported wield-level range. Adding only an occult's 5% magic damage makes a 25 base max hit **26**, after flooring. These are scoped numerical expectations, not universal damage/DPS percentages or live-account results. Review and test accuracy, style adjustments and successful zero-hit handling independently before publishing DPS; `hit chance × max hit / 2` is not an adequate unexamined shortcut.

P4a must deliver at least one account-aware tested numerical comparison and the bounded trident target-DPS case. Use a range of account levels and corrected reference setups, not a hardcoded showcase number. If the selected target has a concrete unmodelled mechanic, substitute another reviewed ordinary target within the same small model and document why. A model that cannot validate its advertised result is a real delivery blocker; adding a full external engine is not the default remedy.

| Claim | Boundary |
|---|---|
| Max hit / hit chance | Valid only for the specified attack, inputs and target where relevant. A stat increase can yield no max-hit breakpoint. |
| Theoretical DPS | Expected damage per attack divided by the supported attack interval at uninterrupted attacks, with stated excluded effects. Not observed player DPS. |
| Expected target kill time | Deferred: needs HP, hit distributions, overkill, attack timing and phase/HP mechanics. HP divided by DPS is only a crude proxy and must not be labelled expected kill time. |
| Full encounter time / kills per trip / deeper delves | Deferred research: requires movement, phases, healing/resources, mechanics and player execution or suitable empirical evidence. Never extrapolate from the slice's DPS. |

### Existing calculator assessment

Reviewed 12 October 2026; source inspection, the original assessment below predates execution. P4a has since run original synthetic comparisons locally; see docs/RESEARCH.md and TASKS.md for the version, inputs, tolerances and exclusions:

| Candidate | Evidence and decision |
|---|---|
| [OSRS Wiki DPS calculator at `89c3e25`](https://github.com/weirdgloop/osrs-dps-calc/tree/89c3e25b344aea90d0189746e4b5f73dde0f0383) | TypeScript/Next application with separate calculation/distribution logic and tests. [Package](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/package.json), [calculation source](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/PlayerVsNPCCalc.ts), [basic tests](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/tests/calc/BasicRolls.test.ts) and [GPL-3.0 licence](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/LICENSE) inspected. Not a drop-in Java library; embedding/porting creates runtime or maintenance work. Use pinned, locally run synthetic comparisons as a validation reference, alongside hand-derived expectations. |
| [Better DPS Calculator at `3fd61bb`](https://github.com/mcdalcin/better-dps-calculator/tree/3fd61bb3d48bdb3c0cacc57032be4a7530242ee7) | Java 11 RuneLite plugin with a [GPL-3.0 licence](https://github.com/mcdalcin/better-dps-calculator/blob/3fd61bb3d48bdb3c0cacc57032be4a7530242ee7/LICENSE), not an established public library contract. Its [sync report](https://github.com/mcdalcin/better-dps-calculator/blob/3fd61bb3d48bdb3c0cacc57032be4a7530242ee7/SYNC_STATUS.md) records 150 fixtures against older upstream `b6bc098d`, but incomplete parity, special attacks and live spell/variant input coverage. Java compatibility alone does not solve correctness or upkeep. Do not couple to another plugin's internals. |

Code/data reuse is not approved merely by public availability. BSD original code can participate in GPL-compatible distribution, but copying/porting GPL calculator code cannot simply be shipped as BSD-only work; it would require an explicit distribution/licensing decision and notices. Wiki-derived datasets have separate reuse terms. For this narrow scope, original formulas from checked mechanics plus independent synthetic validation avoid importing an engine/dataset. Preserve provenance of our own fixtures; do not copy another test corpus by default. Inspect upstream differences when updating the pinned oracle; parity with one version is not proof of game correctness.

## Representative P4a upgrades and checked mechanics

These are conditional examples, **not a top-four ranking for every account**. Final exact variants, acquisition predicates and source records must be validated in the implementation; the P4a delivery activates these four outcomes with source-reviewed variants/routes and the documented bounded comparisons. TASKS.md distinguishes synthetic validation from pending live checks.

| Outcome | Personal-value case and acquisition boundary |
|---|---|
| **Occult necklace** | Reuse the current occult/fury recognition to compare a supported magic neck slot; calculate max-hit breakpoints with the same narrow magic model. Its [checked 5% damage bonus and 70 Magic requirement](https://oldschool.runescape.wiki/w/Occult_necklace?oldid=15314514) are not a guaranteed 5% DPS gain. Extend from an owned-switch reminder to the smoke-devil acquisition/Slayer prerequisite route; verify the 93 Slayer/task requirements in P4a. Retain an already-owned switch as a quick action. |
| **Trident of the swamp** | Reuse magic-fang/Zulrah and owned-parts assembly routes; compare against an observed seas trident at the named Dharok reference target. [Swamp mechanics](https://oldschool.runescape.wiki/w/Trident_of_the_swamp?oldid=15364128) require 78 Magic to wield and 59 Crafting for assembly; scale upkeep and alternatives matter. [Seas mechanics](https://oldschool.runescape.wiki/w/Trident_of_the_seas?oldid=15364129) and [Dharok's ordinary form/weakness](https://oldschool.runescape.wiki/w/Dharok_the_Wretched?oldid=15316733) delimit the comparison. Recognised advanced staffs must prevent an irrelevant baseline recommendation; their full damage models are out of scope. |
| **Zaryte crossbow** | Specialist/special-attack explanation, not a full bolt simulator. [Checked mechanics](https://oldschool.runescape.wiki/w/Zaryte_crossbow?oldid=15293439): 80 Ranged; assembly from Armadyl crossbow, nihil horn and 250 nihil shards; Evoke costs 75% energy, doubles accuracy and triggers the enchanted effect **if the attack hits**. Ruby effects depend on target current HP, caps and target-specific exceptions; 110 is not guaranteed on every shot. Compare the role with owned ranged weapons and competing special attacks; Bowfa/twisted bow ownership alone neither proves nor removes specialist value. Nex/team/access work remains explicit and needs current acquisition-source verification in P4a. |
| **Base avernic treads / a relevant supported upgrade** | Test a shared activity and a potentially marginal upgrade, not an automatic best-in-slot grind. Compare the exact owned boots and relevant styles. [Treads](https://oldschool.runescape.wiki/w/Avernic_treads?oldid=15314043) require 80 Defence/Strength/Ranged/Magic; Cerberus boots can be added separately, requiring 80 Magic/60 Runecraft and 4,000 demon tears each. Base ownership does not imply max treads; item sets do not prove enough tears. Show the closest Final Dawn/access, equip-level or component step rather than promising readiness. |

The [Doom reward mechanics reviewed at revision `15360840`](https://oldschool.runescape.wiki/w/Doom_of_Mokhaiotl?oldid=15360840) place cloth, Eye of Ayak and treads eligibility at delves 2, 3 and 4 respectively. Cloth upgrades a tormented bracelet; the Eye is a powered staff; treads are separate boots. Deeper attempts risk unclaimed rewards. Show cloth/Eye as separately conditional secondary opportunities on the treads' route, using existing advanced-weapon ownership recognition where appropriate. Do not add two more full acquisition/combat models just to populate that section, or assume missing components, simultaneous rewards, any delve capability or a guaranteed useful drop. If no distinct additional reward has supported value, omit that shared-benefit claim.

This covers Slayer, Zulrah, Barrows comparisons, Nex and Doom, while retaining CG, Moons, Titans, ToA and ordinary team CoX advice. Prayers/levels/unlocks remain first-class outcomes; retained prayer training/unlock cases exercise that behaviour without expanding P4a's main equipment sample. This is a bounded source review with original summaries; no guide prose, stat table, images or third-party code is imported.

## Proportionate architecture

Keep one Java/Gradle plugin and the existing boundaries:

| Existing component | Reuse and minimum extension |
|---|---|
| `AccountSnapshotReader` / `AccountSnapshot` | Keep real levels, exact inventory/equipment/session-bank sets, selected quest/prayer facts, timestamps and provenance. Add only selected access facts needed by the chosen routes through supported APIs, with labelled manual fallback if unavailable. No all-quests reader or new storage scanner. |
| `KnowledgeCatalog` | Retain source/variant/predicate validation. Give outcomes stable metadata and route references; existing `Decision.activity`, `outcome`, access/preparation/completion predicates and sources become the starting route representation. Add bounded contextual comparisons and reviewed stat/target facts only for this slice. One bundled catalogue remains sufficient. |
| `Advisor` | Keep pure evaluation. Separate assessed upgrade value/certainty from route availability, group by outcome, expose useful blocked/provisional results and remove goal-count ranking. Reuse existing truth/ownership/usability and route diagnostics. |
| Small comparison helper | Pure Java values in/out, versioned supported inputs and explicit unsupported reasons. A few records/classes beside Advisor suffice; no calculation framework, service container or expression language. |
| `IronpathPanel` / controller | Reorder existing UI into compact cards/details/secondary controls. Keep RuneLite integration, event coalescing, Swing thread, and stale-view/generation protections, including reference-setup edits. |
| Preferences / `ObservationSession` | Retain local Filepath path, hashed identity key, serial IO, schema-1-to-2 compatibility and first-save/restart guards. Preserve stable goals/skips/manual keys; map route identifiers explicitly if needed. No new persistent loadout subsystem. |

Model the three concepts distinctly in small values: **outcome** (what improves), **route** (how and under what prerequisites), **contextual benefit/comparison** (value over an alternative in a named situation). Use stable references and extend the current model in place. Do not build a general progression graph, database, optimiser or generic plugin API. Source validation must still reject malformed references, unsupported predicates and hard-dependency cycles; grouping cannot weaken it.

Bank/live observations stay in memory and clear on existing lifecycle transitions. Source links use the supported browser utility, carry no account data and open only on user action. RuneLite has its own normal network behaviour; local evaluation is not a promise that the entire client is offline. Original English content and BSD-2-Clause notices remain; third-party evidence keeps its own terms.

## Review and remaining decisions

The slice is feasible without a rewrite because ownership/routes/UI already exist; the missing stat/reference-setup/comparison layer is deliberately small. Both numerical cases share it. Zaryte and Doom require conditional explanations and route work, not full simulators. Do not broaden into raid scaling or bolt/encounter modelling during this delivery.

No outstanding product-architecture decision blocks implementation. Bounded source checks for new access/variant facts, independent numeric validation and truthful unsupported-input handling are delivery work in P4a, not separate approval gates. A concrete unresolvable source/formula mismatch blocks only the affected claim, but the mandatory numerical case must still pass. New discoveries justify a documented small substitution, not silently dropping that acceptance criterion. Existing real-client validation remains pending; it must not be represented as passed by this documentation review.

## Keeping knowledge current

**Default:** knowledge changes travel in normal reviewed Plugin Hub updates with the Java plugin. No server, separate updater, signing infrastructure or runtime rule downloads.

1. Maintain a small source register in the catalogue: source type, exact URL, author, revision/commit/date where available, accessed date, reuse basis and affected decision IDs. A node's review date is separate from the latest observed page revision.
2. Initially use official Jagex release news/RSS plus a shortlist of Wiki item/strategy pages and guide authors. Review relevant changes before each release and aim for a weekly short triage. Monthly, inspect official new-content announcements and new guide publications to discover sources absent from the shortlist. Community issues may nominate sources. No recurring automation has been created in this setup.
3. If triage becomes repetitive, add one maintainer-run script that reads feed metadata and permitted MediaWiki revision metadata. It reports changed/new candidates; it never rewrites priorities, downloads executable code, or publishes. Verify Wiki API access and usage policy first. Respect identification, caching, backoff and limits. A timestamp change is not a strategy change.
4. Human review decides whether a game fact changed and whether it changes a recommendation. Separate released content from polls/future updates. For disputed strategy retain alternatives and explicit conditions. Do not claim consensus from copied guides or a creator's popularity.
5. Update only affected decisions, run catalogue validation and affected profile fixtures, bump `knowledgeVersion`, record a concise change note and submit the new commit to Plugin Hub. `schemaVersion` changes only when the reader contract changes. Stable decision IDs preserve preferences; removed IDs are retired/migrated explicitly.
6. Known-invalid decisions are withdrawn or corrected promptly. Display the bundled review date and coverage. Passing 90 days without review shows a catalogue-age notice, not a claim that every fact has become wrong. Offline clients cannot know a newly reported issue until an update arrives. A broken new catalogue must fail closed with a useful error, not silently fall back to fabricated advice; revert to a known-good release when needed.

Budget estimate to test: 20–40 minutes of triage in a quiet week, with additional hours after relevant releases. This is not a commitment or observed maintenance metric. Measure it. If the workload is too high, reduce coverage or contribute the catalogue to an existing maintainer; autonomous scraping does not remove the editorial obligation.
