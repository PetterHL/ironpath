# Endgame progression intelligence — research and decisions

13 October 2026. Documentation-only review of **R2 PR #16, `a501e6fa3c022b78d3b34d8d5414059c704ae96f`**, based on main `cb7c2cd874e8225581e85566f652236ee7e6feb3`. PR #14 (`f821c625`) is a separate earlier ranged implementation; it is not assumed integrated or interchangeable. No application code, runtime data or PR merges are part of this assignment. Historical R2 test results were inspected, not rerun. Live-client and player-usefulness checks remain pending.

**Concurrent R2 follow-up:** before completing this PR, R2 advanced to `32cb24a8db7ae807166d1f2d9844224c3be71fd6` (plugin 0.7.1 / knowledge r2-2). Its diff and review evidence were inspected and incorporated without modifying its application changes. It fixes owned ranged-package selection, projectile-aware quiver choice and remaining anguish/rupture requirements. The architecture findings below remain applicable; the newer 195-test result is preserved as externally recorded implementation evidence, not rerun here.

This is an evidence report, **not another backlog**. [DESIGN](DESIGN.md) owns the selected contracts, [CATALOGUE](CATALOGUE.md) owns coverage, and [TASKS](../TASKS.md) alone owns delivery and acceptance.

## Decision

Proceed with a local, reviewed relationship catalogue and a few explicit mechanic modules. Preserve the current snapshot, comparison, acquisition and UI foundation. The missing capability is mostly the representation and evaluation of **roles and trade-offs**, not a graph database or a new recommendation service.

Separate an item's general role, its marginal importance for this account, and the remaining work to obtain it. A major upgrade can be important and long-term simultaneously. A convenience improvement can be worth pursuing without repaying its acquisition time. Conversely, a reward appearing in a best-in-slot preparation table does not make it a prerequisite.

Comprehensive coverage means accounting for meaningful equipment, activities, alternatives and their contextual relationships, including developed accounts. It does not mean calculating every possible team, loadout, phase and player behaviour. Reviewed qualitative relationships are valid outputs; useful quantitative gaps still have owners in TASKS. There is no item or relationship quota.

## What the R2 code actually supports

| Inspected implementation | Keep | Concrete gap and consequence |
|---|---|---|
| `ObservationSession`, snapshots, guarded preferences | Three-valued knowledge, scoped quantities, lifecycle isolation, local-only storage | New team/method preferences must not become observed skills, unlocks or team equipment. No new telemetry is needed. |
| `CombatSetup`, `TargetProfile`, `ComparisonResult`, magic/ranged services | Complete compatible proposals, independently versioned families, per-metric support | A target profile describes combat inputs, not an activity role, learning method, comfort preference or reward eligibility. Keep these separate. |
| `KnowledgeCatalog.Upgrade`, `Application`, `ContextBenefit` | Stable IDs, predicates and sources; room for data-driven conditions | One `role`, one ordered `Benefit`, first matching context benefit and a list of application prose cannot represent independent DPS, sustain, convenience and indirect value together. Context IDs must currently be numeric targets or the `reviewed-specialist` sentinel. |
| `Advisor.upgrades()` | Account-aware baseline and separate ranged encounter comparisons | Available ordinary DPS can overwrite the qualitative band; the 5% rule also reaches ranged and has a Shadow-ID exception. A supported minor/negative ordinary result must not erase an independently supported utility role. |
| `RecommendationRanker.rank()` | Determinism, fixed comparison buckets, partial-evidence dominance, reasons | `MINOR` always becomes DEFER; burden precedes value for active pursuits. This can bury broad core/QoL improvements or promote trivial cheap work. One scalar gain cannot establish dominance across utility roles. |
| `Advisor.pursuitCandidate()` | Remaining route, owned-intermediate bypass, explicit unknowns | Coverage is `upgrade.contexts.size()` (including analytical context); coordination is inferred from HIGH burden; upkeep is always zero; work ranges are null. These are not measured breadth, coordination, upkeep or general effort dominance. Missing numeric support can block a separately known qualitative benefit. Suggested preparation and equip checks enter the same blocker aggregation as access. |
| `ActivityOpportunity`, `activityRewards` | Deduplicated families, shared acquisition, owned/unknown exclusions | Two families automatically create Worth doing; this alone does not establish good activity value. Availability is ANDed across reward assessments, so one reward-specific unknown can contaminate the activity. There is no reusable item → activity role → useful reward explanation. |
| Typed path nodes and ALL/ANY dependencies | Actual component/access DAGs and cycle checks | `SharedReward.minDelve` still validates only 2–4 and renders Doom-specific wording. Optional preparations need role alternatives without joining the hard acquisition DAG. |
| Source validation and native Value/Path/Browse | Bundled evidence, no network at runtime, restrained sidebar | Add claim-level dependencies and original strategic evidence with approved URLs. The current allowlist accepts Wiki, specific RuneLite ItemID source and Jagex news, not arbitrary community URLs. Do not weaken it to unrestricted links. |

The later R2 correction improves owned ranged package selection and intermediate-item paths; keep those fixes when implementing the broader role-alternative contract. It does not change the inspected ranking, activity aggregation or relationship schema bottlenecks.

These are design constraints observed in code, not claims that every affected account currently receives bad advice. The R2 report records 186 tests and 1,944 ranged oracle cases; that evidence concerns its delivered scope, not the proposed intelligence layer.

Code entry points at the inspected R2 revision: [Advisor](https://github.com/PetterHL/ironpath/blob/a501e6fa3c022b78d3b34d8d5414059c704ae96f/src/main/java/com/ironpath/advisor/Advisor.java), [KnowledgeCatalog](https://github.com/PetterHL/ironpath/blob/a501e6fa3c022b78d3b34d8d5414059c704ae96f/src/main/java/com/ironpath/advisor/KnowledgeCatalog.java), [RecommendationRanker](https://github.com/PetterHL/ironpath/blob/a501e6fa3c022b78d3b34d8d5414059c704ae96f/src/main/java/com/ironpath/advisor/RecommendationRanker.java) and [ActivityOpportunity](https://github.com/PetterHL/ironpath/blob/a501e6fa3c022b78d3b34d8d5414059c704ae96f/src/main/java/com/ironpath/advisor/ActivityOpportunity.java).

## Evidence method and limits

Current Wiki and official pages were retrieved through ordinary HTTPS, their relevant mechanics/strategy/change sections inspected, and Wiki revision IDs plus response hashes recorded in [the source manifest](research/progression-sources.json). Community retrieval varies: C1/C5 were inspected through the web reader, C2/C3 through ordinary HTTPS, and C4 only through indexed excerpts after full-page retrieval failed. C4 supports only the existence of differing preferences, never a mechanic or benchmark. The web reader denied several Wiki pages; successful ordinary retrieval did not require authentication or bypass access controls. No Wiki text, images, calculator code or bulk dataset is redistributed. The report contains original summaries and design conclusions.

**F** below is a checked mechanic or acquisition fact. **S** is a source's strategic recommendation, conditional on its method. **P** is an experienced player's preference or reported experience. **D** is our product inference. F is not a claim that we independently measured the game. Numeric activation still needs exact-form data, complete input contracts and fixtures. A Wiki strategy table and an item page are related sources, not two independent experiments.

All 30 relationships below received a claim/condition/alternative/defer review. They are validated **research relationships**, not 30 newly shipped runtime recommendations. Their unresolved numeric, geometric and player-preference boundaries are deliberately explicit.

### Current-source corrections

- **CoX:** Jagex's 12 August release changes conditional unique weights to totals 60 regular / 56 CM, with tbow weight 2. Old 69-total assumptions are stale. Neither weight is the player's per-raid probability. [J1]
- **Venator:** the 22 July article explicitly retracts its target-finding change; the 19 August release subsequently implements it. The current Wiki still warns that detailed bounce geometry may be outdated. The chronology is resolved; exact revised geometry is not. Validate specific arrangements before numerical promotion. [J2] [J3] [S07]
- **Recent mechanics:** July changes Inquisitor, Ralos and burning-claw energy; September changes Ghommal's penny to a consumed unlock, including blood fury. Do not use older guides/calculators uncritically. [J2] [J4]
- **Future content:** Fractured Archive is announced for 20 October, after this research cut. Retain its existing watchlist; no released recommendation or performance claim yet. [J5]

## Thirty representative relationships

### Raids, core progression and preparation

| ID and relationship | Checked evidence | Account-specific decision, alternatives and boundary |
|---|---|---|
| R01 — CoX → tbow and other rewards | **F:** CoX awards prayers, armour, weapons and specialists through a shared, points/mode-dependent loot system. Tbow's damage role depends on target magic rather than being universally best. [J1] [S01] [S02] | **D:** recommend a justified CoX activity, with distinct useful reward families; show tbow as an important long-tail outcome. Already owned prayers/armour remove their reward reasons. If only tbow remains, preserve that choice without fabricating a multi-reward advantage. No generic raid-hour estimate or compulsory tbow target. |
| R02 — Lance → Olm melee → CoX rewards | **F:** lance has a dragonbane effect and is made from a hydra claw plus Zamorakian hasta. **S:** current CoX guidance includes Scythe, lance and other melee alternatives; scale/reduction change the comparison. [S03] [S04] | **D:** distinguish an owned lance or nearly finished recipe from a fresh Slayer/Hydra detour. Evaluate owned Fang/tentacle/Scythe for the intended method. Lance can be a worthwhile broad dragon specialist without being required before first team CoX. No automatic inference that the detour saves time; solo attack-cycle suitability needs its own method evidence. |
| R03 — DWH and BGS → team defence reduction | **F:** DWH reduces current Defence proportionally; BGS drains by damage and rolls against slash. Tekton has special first-hit/miss behaviour. **S:** percentage reduction followed by BGS can be complementary. [S03] [S05] [S06] | **D:** role is shared reduction coverage, not “everyone must own both.” Existing BGS, teammate allocation, room/scale and residual defence affect a DWH grind's importance. Unknown teammates remain unknown. A group's joining rule is a method/team constraint, never a game-wide access condition. |
| R04 — Owned elder maul → reduced need for DWH | **F:** elder maul's special reduces current Defence by 35% on a successful hit and has its own accuracy modifier. It shares the broad reduction role with DWH. [S08] | **D:** an owned maul can cover the main role and suppress a redundant shaman grind where the reviewed context agrees. Do not prescribe obtaining a rare CoX reward before starting CoX. Preserve exceptions such as attack style, requirements, timing or particular tasks; compare the full package, not weapon names. |
| R05 — Rigour → ranged capability → further PvM | **F:** unlock comes from consuming the CoX dexterous scroll; use also requires 74 Prayer/70 Defence. [S09] | **D:** a broadly useful core unlock for a ranged-dependent account; compare its currently available prayer, not an artificially prayerless baseline. Owned unread scroll creates an unlock/level action, not another raid grind. Rigour helps CoX without becoming circular hard access to its own source. Prayer upkeep and actual unlock state remain separate. |
| R06 — Anguish → broad ranged neck upgrade | **F:** +15 ranged attack/+5 ranged strength; 75 HP to equip. Rupture is a further upgrade, so anguish is not current absolute BiS. [S10] | **D:** broad core value can justify high personal importance over a weaker neck without a universal +1 max hit promise. Owned rupture suppresses the duplicate acquisition. Existing zenyte/intermediates shorten the route; competing jewellery allocations and crafting/onyx/enchanting work remain explicit. A small neutral DPS delta alone must not define all account value. |

### Multi-target damage, convenience and challenge rewards

| ID and relationship | Checked evidence | Account-specific decision, alternatives and boundary |
|---|---|---|
| R07 — Venator → lower-attention multi-combat Slayer | **F:** charged attacks can bounce twice, with independent accuracy and reduced secondary maximums; August implements the targeting change withdrawn in July. **S/P:** packed eligible targets and automatic engagement support low-input Slayer. [S07] [J2] [J3] [C1] | **D:** useful for an account that values this method, even if active barraging is faster. Compare barrage/chins and owned melee with rune/arrow/essence supply and attention as separate trade-offs. Five shards and charging remain acquisition/upkeep. Single combat or unsuitable geometry removes the bounce benefit. No universal AFK duration, task list, XP rate or “free” upkeep. |
| R08 — Venator → Colosseum Fremennik seer handling | **F/S:** the strategy guide gives a charged Venator/dragon-arrow/Rigour switch and lists tbow, blowpipe and other options. Its favourable one-action result depends on levels, maximums and a valid return bounce. [S11] [S07] | **D:** real convenience and wave-opening value, not mandatory quiver access. Compare the player's existing switches and inventory burden. A first-quiver learner and repeat-run player may value it differently. Check current geometry and breakpoints before showing guaranteed one-shot numbers; do not assert Venator kills every Fremennik or replaces the main wave weapon. |
| R09 — SGS → Colosseum sustain | **F:** SGS restores health/prayer through its special. **S:** the correct Fremennik target can support a strong restore under the encounter's special hit rules. [S12] [S11] | **D:** worthwhile sustain/comfort alternative to spending energy on damage; existing blowpipe/eldritch/supply strategy can lower its priority. Do not require a new GWD grind for every learner or advertise extra completions per trip. The restore calculation and the player's survival/learning outcome are different claims. |
| R10 — Quiver → ranged stats and ammo-switch convenience | **F:** quiver supplies another arrow/bolt slot; its charged extra bonus applies to arrows/bolts, not blowpipe darts or crystal weapons. Blessing and Ava recovery are separate states. [S13] | **D:** Bowfa/blowpipe users can retain base-stat and future/slot utility without receiving a false charged bonus. Tbow plus crossbow switching offers a separate convenience reason. Completion is a challenge with unknown learning effort; neither Venator nor a megarare is a hard access gate. Do not confuse first quiver, charging, blessing and recovery unlock. |
| R11 — Muspah → Venator plus heart/sceptre progression | **F:** Muspah supplies shards, essence and an ancient icon; saturation requires an existing imbued heart plus 150,000 essence. [S14] [S15] | **D:** shared activity value depends on which rewards and components are useful now. Four owned Venator shards change remaining work; saturation is not a promised benefit for an account without a heart. Essence allocated to saturation cannot simultaneously fund bow charges. Owned Bowfa/other reviewed methods can support Muspah; do not require tbow to pursue it. [S34] |

### Specials, demonbane and Doom

| ID and relationship | Checked evidence | Account-specific decision, alternatives and boundary |
|---|---|---|
| R12 — Zaryte → high-HP ruby-bolt burst | **F:** Evoke costs 75% energy, doubles accuracy and guarantees the enchanted effect only on a successful hit. Ruby enhancement depends on current target HP, with a 110 cap. [S16] [S17] | **D:** an owned tbow does not erase this special role. Ordinary crossbows/other specs remain alternatives; low HP, immunity, phase caps and energy competition can reduce value. “Weaker ordinary DPS” is not universal either. Horn/ACB/shards determine remaining work; no flat 110 guaranteed against every target or permanent DPS bonus. |
| R13 — Lightbearer → special availability | **F:** special-energy regeneration is doubled, with equipment-switch timer semantics. [S18] | **D:** complements Zaryte, healing specs and encounter-specific special use, but competes with damage rings. Starting energy, encounter/cycle duration and other restores decide whether an extra usable spec occurs. Short encounters may gain none. No sum of independent Zaryte/Lightbearer/Death Charge percentages; model one declared energy budget or retain qualitative synergy. |
| R14 — Scorching → K'ril control and demon damage | **F:** demonbane damage/accuracy and a demon-only binding special; binding is not conditional on landing damage. [S19] | **D:** damage and easier distance control are independent benefits. Owned Bowfa kiting or Emberlight melee can be adequate alternatives; a synapse detour is optional. Method, special availability and minion handling matter. No universal kills/trip promise or inference that every demon is equally affected or bindable. |
| R15 — Scorching versus tbow → Doom main ranged role | **S:** current Doom guidance uses tbow or Scorching; faster attacks have acid/movement trade-offs. Elite Void is a serious setup alternative, not merely incomplete Masori. [S20] [S19] [S02] | **D:** a capable Scorching setup must not trigger “obtain tbow/Zaryte/Scythe first.” Compare available gear for a chosen delve/method; better theoretical DPS is only one dimension. Bowfa ownership alone does not establish equivalent Doom preparation: demonbane mechanics and switches need checking. Exact encounter times and acid-per-kill are unmodelled. |
| R16 — Doom → separate melee/demonbane roles | **F:** shield interruption has demonbane restrictions. **S:** Darklight, demonbane spells and crystal halberd offer accessible roles; Zaryte/Scythe are optional improvements, not prerequisites. [S20] [S35] | **D:** distinguish hard quest access, the selected method's combat capabilities and optional comfort upgrades. A cheap switch can meet a role while a premium item improves it. Never demand owning Eye/treads before earning Doom rewards. Availability of a reviewed setup does not certify reaching a delve or executing its mechanics. |
| R17 — Doom → treads → optional Cerberus additions | **F:** base treads become eligible from completed delve 4; each Cerberus boot is added independently for 4,000 tears, with separate assembly/equip levels. [S21] [S22] | **D:** base, partial and max treads are distinct outcomes. No mandatory three-boot grind before Doom or before using base treads. Hybrid inventory savings matter only if the compared method actually replaces boot switches. Exact style breakpoints and owned boots alter priority. Cloth/Eye have different eligibility and value; do not couple all rewards to treads readiness. |
| R18 — Emberlight → demon damage plus charge convenience | **F:** Emberlight improves Arclight's stats, avoids degradation and retains demonbane mechanics with target-specific effectiveness. [S23] | **D:** sustainable demon melee can be worthwhile even when another owned weapon competes on raw DPS. Compare Scythe/Fang/Arclight for the actual demon and sustain budget. Current Arclight charge/infusion requirements remain real acquisition work. Do not treat “demon” as a uniform damage multiplier or “nondegrading” as evidence of faster kills. |
| R19 — One synapse → competing demonbane branches | **F:** a synapse creates Emberlight, Scorching or Purging; reversion can recover it, but other inputs are not necessarily recovered. [S24] [S19] [S23] | **D:** display mutually exclusive uses of the same resource, not three immediately obtainable upgrades. Team/content preferences and owned alternatives decide the initial branch. Never assume reversibility is costless, especially Arclight charge/infusion preparation. The route can explain switching; the plugin does not perform it. |

### Melee, prayers and utility on developed accounts

| ID and relationship | Checked evidence | Account-specific decision, alternatives and boundary |
|---|---|---|
| R20 — Fang → high-defence stab / ToA | **F:** the stab accuracy effect differs inside/outside ToA; its constrained damage distribution is another mechanic. [S25] | **D:** valuable before and after megarares in applicable roles, but no universal Fang > lance/rapier ordering. Target defence, stab/slash selection, off-hand and phase determine marginal benefit. Raids are its acquisition activity, not a requirement to own Fang before beginning that activity. Exact invocation and scaling remain required numeric inputs. |
| R21 — Scythe → size-sensitive melee / ToB and Sol | **F:** additional hits depend on target size; charges/resources matter. **S:** Colosseum's Sol supports slash alternatives including tentacle and Soulreaper. [S26] [S11] | **D:** compare 1×1, 2×2 and 3×3 targets and real slash/crush defences. One BiS label cannot rank every encounter. Preserve two-handed incompatibility, blood supply and sustain alternatives. A reward useful inside its own source activity is a natural feedback relationship, not hard access. |
| R22 — Soulreaper → sustained melee versus interrupted fights | **F:** stacks accumulate after attacks and strengthen later swings; current decay is 50 ticks per stack without continued attacks. [S27] | **D:** a five-stack result cannot describe fresh starts or frequent downtime. Compare realistic ramp/retention scenarios with owned Scythe/other weapons. Four-component acquisition remains distinct from stack modelling. Never infer perfect retention from ownership or apply its strength effect to weapon swaps. |
| R23 — BGS/DWH/maul → coordinated ToB reduction | **F/S:** percentage reducers and BGS can serve complementary reduction stages, notably Maiden/Xarpus. [S06] [S05] [S08] | **D:** per-player relevance depends on assigned role, teammates, floors and reset points. Owned BGS can remain useful after obtaining maul. Do not sum full independent reduction benefits or treat “someone on the team has it” as observed. Numerical gains need shared starting/residual defence and a declared attack order. |
| R24 — Yellow keris → ToA sustain option | **F:** its special heals/restores inside ToA, consuming both prayer and special energy; it does not work elsewhere. [S28] | **D:** keep this utility even when Fang wins ordinary attacks. Invocation/method, sufficient resources, supplies and competing burst decide importance. Owning the jewel/base allows assembly; it does not establish ability to sustain a particular raid level. No automatic high-invocation certification. |
| R25 — Blood fury → melee sustain versus offensive neck | **F:** melee hits can heal; charges are consumed on successful hits, including multiple hits per Scythe swing. [S29] | **D:** lower DPS can still offer valuable comfort or supply relief. Compare torture/rancour and healer alternatives with shard availability. Do not transform expected healing into guaranteed extra kills or claim charges are consumed once per Scythe attack. A developed account can rationally prefer either side of this trade-off. |
| R26 — Divine rune pouch → inventory flexibility | **F:** four rune types instead of three; thread/pouch and 75 Crafting assembly. [S30] | **D:** show a saved slot only for a declared rune set/loadout that actually needs it. No benefit if three types already suffice. Additional supplies or fewer switches are possible uses, not automatic DPS. Owned thread removes the fresh-drop step; a QoL outcome can be immediately useful without being numerically stronger. |
| R27 — Consumed penny → charge efficiency | **F:** current penny is a consumable permanent effect conditional on retaining Master CA tier; September extended it to blood fury. [S31] [J4] | **D:** acquired/consumed/active are different states. For a charged-gear user near the tier it may be meaningful; do not send an account through extensive CAs solely for modest supply saving. Neither a missing bank item nor future tier loss proves the unlock was never obtained. No fixed GP-to-Ironman-priority conversion. |
| R28 — Salve/task gear → undead package alternatives | **F:** Salve(ei) and Slayer-helmet bonuses do not stack; eligible effects depend on the target and attack family. [S32] | **D:** compare whole packages, not additive accessory gains. Task status and undead applicability are inputs. An owned stronger general neck does not erase this specialist. Bouncing/area attacks require separately reviewed propagation; don't generalise a single-target formula to all secondary hits. |
| R29 — Augury → magic damage/accuracy and defence | **F:** current Augury adds magic damage as well as accuracy/defence; unlock and use requirements are separate. [S33] | **D:** compare the available prayer and full loadout; do not repeat the outdated “accuracy only” description. Powered, standard and special family interactions need explicit models. Unlocked Augury suppresses another-scroll advice. Its usefulness helps explain CoX independently of a tbow goal. |
| R30 — Thralls → useful damage with adverse Doom interaction | **F:** thralls have spellbook/access/resource requirements. **S:** Doom guidance warns their hits generate extra acid and discourages them there. [S36] [S20] | **D:** reusable utility needs adverse interactions, not only positive synergies. “Adds damage” must not become “always recommended.” Retain spellbook conflicts and the method's trade-off. The advisor provides planning explanations, never live combat instructions. |

## How far the model must reach

The existing inventory remains the broad starting map. Extend coverage along these axes; these are coverage dimensions, not mandatory future subsystems:

| Activity family | Relationships to account for |
|---|---|
| CoX / CM, ToA, ToB / hard mode | Shared and role-dependent rewards, room/phase/scaling, team debuffs, prayer/spell unlocks, self-improving raid gear, supplies and inventory. |
| GWD / Nex, DT2, Doom, Yama, Nightmare / Phosani, Corp | Specialists, defence floors, burst windows, resource/sustain choices, task/delve/mode requirements and component rewards. |
| Inferno / Colosseum | Learning method versus speed method, range/control/sustain, sufficient alternatives, permanent rewards, no ownership-to-skill inference. |
| Slayer / Muspah / Zulrah / Vorkath / Araxxor / Hueycoatl / Royal Titans / Moons | Broad bridges, task/element/bane/flat-armour rules, multi-target geometry, recurring supply and low-input preferences. |
| Wilderness, vampyres and new released content | Location/risk conditions, immunity/protection, specialist gear and release-specific mechanics; no inferred risk tolerance. |
| Utility and permanent progression | Pouches, prayers, spellbooks, imbues, relevant diaries/CAs, pools/teleports, charge economy and mutually exclusive materials. Keep a concrete PvM use; avoid an unrelated completion checklist. |

All meaningful reviewed relationships can be stored and explained using the same small contracts. Coverage can grow without promising exhaustive optimisation of all possible loadouts or numerical simulation of every encounter. Unknown current/future interactions are visible gaps; “not modelled” never silently becomes “irrelevant.”

## Critical interpretation of the proposed principles

1. **Multiple values: accept, without turning them into additive points.** Core equipment is a role/breadth property; AFK is an attention preference; indirect value is an explanation chain. These are not ten interchangeable quantities.
2. **Contextual relationships: accept.** Distinguish game access, equip requirements, method requirements and optional better preparation. Item → activity → reward can form useful feedback loops; only hard acquisition dependencies must be acyclic.
3. **Dynamic tiers: accept with qualification.** General role, personal importance, route status and uncertainty must remain separate. Tiers are product judgement explained by evidence, not a discoverable universal truth about OSRS.
4. **No pure efficiency objective: accept.** A player may enjoy raids, an upgrade itself, lower attention or smoother learning. Do not pretend to infer enjoyment. Without preferences show justified variety; preferences refine it without creating unsupported benefit.
5. **Preparation traps: reject both extremes.** “Possible without it” does not establish a sensible learner setup; “listed in BiS” does not justify a detour. Offer an evidence-backed sufficient route now plus optional improvements and their independent value.
6. **Comprehensive ambition: accept as maintained coverage.** The workload is continuing content review. Comprehensive is not a one-time checkbox, a requirement for exact hours everywhere, or a reason for a large software platform.
7. **Optional goals: retain.** Automatic direct value plus account-relevant activity rewards works without a goal. Preferences can change prominence, never mechanical truth, access, RNG or evidence confidence.

## Community evidence and disagreement

The current Wiki strategy pages supply the main source-reviewed method alternatives. The attributed WDR CM guide [C2] explicitly targets learners with prior room knowledge in trios; it calls tbow close to a baseline while admitting alternatives. This is evidence of **method and audience dependence**, not a universal CoX gate. Its older set-effect and cooperative-spell advice is not adopted as current Ironman mechanics. WDR's own public pages [C3] defer detailed current gear advice to Discord; those channels were not accessed and no current Discord rule is claimed.

MasaConor's firsthand AFK Slayer write-up [C1] reports extensive Venator use and prioritises low input over fastest experience. Other players explicitly prefer barrage or question niche value [C4]. These establish a legitimate preference axis, not population-wide optimality or current benchmark rates. Older Colosseum advice [C5] also supplies downgrades, but is unattributed/reproduced and not independent mechanics evidence; its universal-seeming ordering is not adopted. Reported experience cannot certify a specific user's learning success.

### Remaining evidence questions

- Current Venator arrangement rules and the exact supported seer breakpoint after the August change need targeted calculation/client verification. Preserve qualitative use now; mark geometry-dependent numbers unsupported.
- “Adequate for learning” needs player/method review. There is no source-derived universal minimum gear, death count or success probability for Colosseum, solo CoX or deep Doom.
- Team roles, player attention preferences and actual repeat-cycle pace are not generally observable from bank data. Use explicit optional inputs or conditional alternatives.
- No new full raid/Nex/Doom/DT2 acquisition distribution or encounter/trip model was validated here. Current eligibility/mechanism facts do not establish complete personal ETAs.
- Broad catalogue rows outside these 30 relationships retain their recorded research status. Neither this report nor existing oracle agreement closes all mechanic/variant/release gaps.

## Maintenance and implementation handoff

Promote one reviewed relation with a positive case, a strong-owned-alternative case and a false/unknown-condition case. Record factual and strategic claims separately, including review date, method/audience, revision/hash, release status and affected IDs. Extend existing source dependencies to relations and preparation alternatives. Retire invalid numeric claims without throwing away still-valid route or qualitative evidence.

Keep authoring as bundled JSON plus Java mechanics. Split the large authoring file by family only when useful for diffs; deterministic loading and stable IDs suffice. No database, runtime scraping, AI-generated live rules, general rule language or numerical model per individual item. The [DESIGN specification](DESIGN.md#progression-intelligence-contract-version-progression-1) and R3–R6 in TASKS turn this evidence into implementable work with counterfactual account checks.

Documentation verification: nine documentation/metadata files only; all 30 relationship IDs, 46 source references and 16 planned PI profiles checked; 43 response hashes matched the retrieved local source pages; relative file links resolve; `git diff --check` passes. TASKS's historical snapshot section is byte-for-byte unchanged against both inspected R2 heads. Application code, runtime catalogue and build/test files are unchanged. No application or live-client tests were run for this documentation assignment.

## Sources

Accessed and relevant claims reviewed on **13 October 2026**. Wiki sources below are revision-pinned; official pages use publication dates and response hashes in the manifest. Revision timestamps are not automatically review dates. Community pages are qualified above. Raw source pages stay outside the repository.

- **S01** — [Chambers of Xeric](https://oldschool.runescape.wiki/w/Chambers_of_Xeric?oldid=15366126) · revision 15366126.
- **S02** — [Twisted bow](https://oldschool.runescape.wiki/w/Twisted_bow?oldid=15363157) · revision 15363157.
- **S03** — [Chambers of Xeric / Strategies](https://oldschool.runescape.wiki/w/Chambers_of_Xeric/Strategies?oldid=15357585) · revision 15357585.
- **S04** — [Dragon hunter lance](https://oldschool.runescape.wiki/w/Dragon_hunter_lance?oldid=15264894) · revision 15264894.
- **S05** — [Dragon warhammer](https://oldschool.runescape.wiki/w/Dragon_warhammer?oldid=15362680) · revision 15362680.
- **S06** — [Bandos godsword](https://oldschool.runescape.wiki/w/Bandos_godsword?oldid=15364757) · revision 15364757.
- **S07** — [Venator bow](https://oldschool.runescape.wiki/w/Venator_bow?oldid=15345156) · revision 15345156.
- **S08** — [Elder maul](https://oldschool.runescape.wiki/w/Elder_maul?oldid=15240979) · revision 15240979.
- **S09** — [Rigour](https://oldschool.runescape.wiki/w/Rigour?oldid=14954629) · revision 14954629.
- **S10** — [Necklace of anguish](https://oldschool.runescape.wiki/w/Necklace_of_anguish?oldid=15301384) · revision 15301384.
- **S11** — [Fortis Colosseum / Strategies](https://oldschool.runescape.wiki/w/Fortis_Colosseum/Strategies?oldid=15370386) · revision 15370386.
- **S12** — [Saradomin godsword](https://oldschool.runescape.wiki/w/Saradomin_godsword?oldid=15231951) · revision 15231951.
- **S13** — [Dizana's quiver](https://oldschool.runescape.wiki/w/Dizana%27s_quiver?oldid=15357677) · revision 15357677.
- **S14** — [Phantom Muspah](https://oldschool.runescape.wiki/w/Phantom_Muspah?oldid=15367478) · revision 15367478.
- **S15** — [Saturated heart](https://oldschool.runescape.wiki/w/Saturated_heart?oldid=15371902) · revision 15371902.
- **S16** — [Zaryte crossbow](https://oldschool.runescape.wiki/w/Zaryte_crossbow?oldid=15293439) · revision 15293439.
- **S17** — [Ruby bolts (e)](https://oldschool.runescape.wiki/w/Ruby_bolts_(e)?oldid=15358865) · revision 15358865.
- **S18** — [Lightbearer](https://oldschool.runescape.wiki/w/Lightbearer?oldid=15299656) · revision 15299656.
- **S19** — [Scorching bow](https://oldschool.runescape.wiki/w/Scorching_bow?oldid=15317455) · revision 15317455.
- **S20** — [Doom of Mokhaiotl / Strategies](https://oldschool.runescape.wiki/w/Doom_of_Mokhaiotl/Strategies?oldid=15359842) · revision 15359842.
- **S21** — [Doom of Mokhaiotl](https://oldschool.runescape.wiki/w/Doom_of_Mokhaiotl?oldid=15360840) · revision 15360840.
- **S22** — [Avernic treads](https://oldschool.runescape.wiki/w/Avernic_treads?oldid=15314043) · revision 15314043.
- **S23** — [Emberlight](https://oldschool.runescape.wiki/w/Emberlight?oldid=15358485) · revision 15358485.
- **S24** — [Tormented synapse](https://oldschool.runescape.wiki/w/Tormented_synapse?oldid=15192516) · revision 15192516.
- **S25** — [Osmumten's fang](https://oldschool.runescape.wiki/w/Osmumten%27s_fang?oldid=15236139) · revision 15236139.
- **S26** — [Scythe of vitur](https://oldschool.runescape.wiki/w/Scythe_of_vitur?oldid=15365993) · revision 15365993.
- **S27** — [Soulreaper axe](https://oldschool.runescape.wiki/w/Soulreaper_axe?oldid=15360513) · revision 15360513.
- **S28** — [Keris partisan of the sun](https://oldschool.runescape.wiki/w/Keris_partisan_of_the_sun?oldid=15314679) · revision 15314679.
- **S29** — [Amulet of blood fury](https://oldschool.runescape.wiki/w/Amulet_of_blood_fury?oldid=15355928) · revision 15355928.
- **S30** — [Divine rune pouch](https://oldschool.runescape.wiki/w/Divine_rune_pouch?oldid=15359154) · revision 15359154.
- **S31** — [Ghommal's lucky penny](https://oldschool.runescape.wiki/w/Ghommal%27s_lucky_penny?oldid=15352575) · revision 15352575.
- **S32** — [Salve amulet(ei)](https://oldschool.runescape.wiki/w/Salve_amulet(ei)?oldid=15365051) · revision 15365051.
- **S33** — [Augury](https://oldschool.runescape.wiki/w/Augury?oldid=15362822) · revision 15362822.
- **S34** — [Phantom Muspah / Strategies](https://oldschool.runescape.wiki/w/Phantom_Muspah/Strategies?oldid=15371265) · revision 15371265.
- **S35** — [Arclight](https://oldschool.runescape.wiki/w/Arclight?oldid=15365331) · revision 15365331.
- **S36** — [Thrall](https://oldschool.runescape.wiki/w/Thrall?oldid=15357994) · revision 15357994.
- **J1** — [Summer Sweep-Up — Agility & Chambers of Xeric Changes](https://secure.runescape.com/m=news/summer-sweep-up---agility--chambers-of-xeric-changes?oldschool=1) · 2026-08-12.
- **J2** — [Summer Sweep-Up: Gear & PvM Changes](https://secure.runescape.com/m=news/summer-sweep-up-gear--pvm-changes?oldschool=1) · 2026-07-22.
- **J3** — [Summer Sweep-Up — Hunter & Skilling](https://secure.runescape.com/m=news/summer-sweep-up---hunter--skilling?oldschool=1) · 2026-08-19.
- **J4** — [Summer Sweep-Up: Miscellaneous](https://secure.runescape.com/m=news/summer-sweep-up-miscellaneous?oldschool=1) · 2026-09-02.
- **J5** — [The Fractured Archive — Everything You Need To Know](https://secure.runescape.com/m=news/the-fractured-archive---everything-you-need-to-know?oldschool=1) · 2026-10-06.
- **C2** — [Attributed We Do Raids CoX/CM guide](https://oldschool.runescape.wiki/w/User%3AOstentatio/snuffbox?oldid=15161714) · revision 15161714.
- **C1** — [MasaConor: A detailed guide to AFK Venator Bow training](https://www.reddit.com/r/ironscape/comments/19eksxl/a_detailed_guide_to_afk_venator_bow_training/) · 2024-01-24.
- **C3** — [We Do Raids public gear-guide directory](https://wedoraids.com/resources/gear-guides/).
- **C4** — [Venator bow Slayer: differing attention and acquisition preferences](https://www.reddit.com/r/ironscape/comments/1fdxj7w) · 2024-09-11.
- **C5** — [First quiver advice (unattributed reproduction; not a mechanics authority)](https://www.runescapeguides.com/fortis-colosseum-first-dizanas-quiver-advice-osrs/) · 2025-02-04.

[S01]: https://oldschool.runescape.wiki/w/Chambers_of_Xeric?oldid=15366126
[S02]: https://oldschool.runescape.wiki/w/Twisted_bow?oldid=15363157
[S03]: https://oldschool.runescape.wiki/w/Chambers_of_Xeric/Strategies?oldid=15357585
[S04]: https://oldschool.runescape.wiki/w/Dragon_hunter_lance?oldid=15264894
[S05]: https://oldschool.runescape.wiki/w/Dragon_warhammer?oldid=15362680
[S06]: https://oldschool.runescape.wiki/w/Bandos_godsword?oldid=15364757
[S07]: https://oldschool.runescape.wiki/w/Venator_bow?oldid=15345156
[S08]: https://oldschool.runescape.wiki/w/Elder_maul?oldid=15240979
[S09]: https://oldschool.runescape.wiki/w/Rigour?oldid=14954629
[S10]: https://oldschool.runescape.wiki/w/Necklace_of_anguish?oldid=15301384
[S11]: https://oldschool.runescape.wiki/w/Fortis_Colosseum/Strategies?oldid=15370386
[S12]: https://oldschool.runescape.wiki/w/Saradomin_godsword?oldid=15231951
[S13]: https://oldschool.runescape.wiki/w/Dizana%27s_quiver?oldid=15357677
[S14]: https://oldschool.runescape.wiki/w/Phantom_Muspah?oldid=15367478
[S15]: https://oldschool.runescape.wiki/w/Saturated_heart?oldid=15371902
[S16]: https://oldschool.runescape.wiki/w/Zaryte_crossbow?oldid=15293439
[S17]: https://oldschool.runescape.wiki/w/Ruby_bolts_(e)?oldid=15358865
[S18]: https://oldschool.runescape.wiki/w/Lightbearer?oldid=15299656
[S19]: https://oldschool.runescape.wiki/w/Scorching_bow?oldid=15317455
[S20]: https://oldschool.runescape.wiki/w/Doom_of_Mokhaiotl/Strategies?oldid=15359842
[S21]: https://oldschool.runescape.wiki/w/Doom_of_Mokhaiotl?oldid=15360840
[S22]: https://oldschool.runescape.wiki/w/Avernic_treads?oldid=15314043
[S23]: https://oldschool.runescape.wiki/w/Emberlight?oldid=15358485
[S24]: https://oldschool.runescape.wiki/w/Tormented_synapse?oldid=15192516
[S25]: https://oldschool.runescape.wiki/w/Osmumten%27s_fang?oldid=15236139
[S26]: https://oldschool.runescape.wiki/w/Scythe_of_vitur?oldid=15365993
[S27]: https://oldschool.runescape.wiki/w/Soulreaper_axe?oldid=15360513
[S28]: https://oldschool.runescape.wiki/w/Keris_partisan_of_the_sun?oldid=15314679
[S29]: https://oldschool.runescape.wiki/w/Amulet_of_blood_fury?oldid=15355928
[S30]: https://oldschool.runescape.wiki/w/Divine_rune_pouch?oldid=15359154
[S31]: https://oldschool.runescape.wiki/w/Ghommal%27s_lucky_penny?oldid=15352575
[S32]: https://oldschool.runescape.wiki/w/Salve_amulet(ei)?oldid=15365051
[S33]: https://oldschool.runescape.wiki/w/Augury?oldid=15362822
[S34]: https://oldschool.runescape.wiki/w/Phantom_Muspah/Strategies?oldid=15371265
[S35]: https://oldschool.runescape.wiki/w/Arclight?oldid=15365331
[S36]: https://oldschool.runescape.wiki/w/Thrall?oldid=15357994
[J1]: https://secure.runescape.com/m=news/summer-sweep-up---agility--chambers-of-xeric-changes?oldschool=1
[J2]: https://secure.runescape.com/m=news/summer-sweep-up-gear--pvm-changes?oldschool=1
[J3]: https://secure.runescape.com/m=news/summer-sweep-up---hunter--skilling?oldschool=1
[J4]: https://secure.runescape.com/m=news/summer-sweep-up-miscellaneous?oldschool=1
[J5]: https://secure.runescape.com/m=news/the-fractured-archive---everything-you-need-to-know?oldschool=1
[C2]: https://oldschool.runescape.wiki/w/User%3AOstentatio/snuffbox?oldid=15161714
[C1]: https://www.reddit.com/r/ironscape/comments/19eksxl/a_detailed_guide_to_afk_venator_bow_training/
[C3]: https://wedoraids.com/resources/gear-guides/
[C4]: https://www.reddit.com/r/ironscape/comments/1fdxj7w
[C5]: https://www.runescapeguides.com/fortis-colosseum-first-dizanas-quiver-advice-osrs/
