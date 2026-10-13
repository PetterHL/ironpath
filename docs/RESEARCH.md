# IronPath research and evidence

## Neutral analytical reference — 13 October 2026

The requested **Enemy with 0 Defence** is an authored comparison assumption, not an observed NPC or combat-dummy stat claim. The exact powered-magic inputs are Defence 0, Magic 0, magic-defence bonus 0, standard non-ToA rules and no reduction/task/salve/weakness. Ordinary NPC magic defence uses `(Magic + 9) × (magic defence bonus + 64)`, so the roll is **576**, not zero. Existing pinned `magic-formula` and `magic-damage` sources supply the ordinary mechanics and are linked to the new context in the bundled register; their original review dates are retained. No new source retrieval or endorsement is claimed for the authored input choices. HP, size, melee/ranged defences, scaling and creature-family properties are unassigned in runtime.

The independently authored [neutral harness](validation/neutral-oracle.test.ts) runs against the separate GPL development reference at `89c3e25b344aea90d0189746e4b5f73dde0f0383`. It uses the generic custom-monster base with zero Defence/Magic/magic-defence bonus and no attributes/weakness; its unused HP/size fields are incidental harness defaults, not runtime inputs or kill-time evidence. **180 original full-loadout cases** cover three Magic levels, five staves, three armour packages, two styles and no-boost/Augury-heart scenarios. Outputs agree with the original Java implementation within the established tolerances. Ordinary successful-zero damage and Sang's current proc retain the R1 distributions. No upstream code, equipment data or imagery is redistributed. See [validation](validation/neutral-reference.md) for hand checks and limits.

## R1.5a client finding: Ancient blessing — 13 October 2026

The supplied client screenshots exposed unsupported equipped ammo ID 20235. Reviewed [Ancient blessing revision 15252873](https://oldschool.runescape.wiki/w/Ancient_blessing?oldid=15252873): item ID 20235, ammunition slot, all attack/defence bonuses zero, magic damage 0%, Prayer +1 and Zarosian God Wars protection. These item facts support retaining this exact item in the powered-magic reference. Prayer duration and faction protection are outside the fixed-prayer uninterrupted DPS model. Other blessings and unsupported ammunition are not inferred from this review.

Accessed/reviewed 13 October 2026; pinned response SHA-256 `7df999408e572fc5b3061fdc4ad040d1729de3d092fe8e909b05da0318bdd05d`. Original stat entries and explanations only; no Wiki prose, code, dataset or assets redistributed. Catalogue source `r15-ancient-blessing` links both the gear and combat-stat records for 20235. Knowledge is now `r1.5a-2`; schema 7 and combat model `powered-magic-2` are unchanged.

## Acquisition effort and native UI investigation — 13 October 2026

**Status:** research and proposed implementation contract. No acquisition estimate, personal performance tracker or redesigned UI was implemented here. R1 evidence below remains unchanged. The source review establishes mechanics and technical feasibility, not reliable account-specific kill rates or Hub approval.

### Evidence register and conclusions

Sources accessed/reviewed 13 October 2026. RuneLite source was inspected at master and pinned to `5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03`; revalidate public APIs against the implementation's resolved client version (R1 reports 1.13.1). These are development references, not new runtime dependencies. No upstream code, sprites, guide text or bulk dataset is imported.

| ID / source | Verified finding and permissible inference | Limit / maintenance need |
|---|---|---|
| E1 [Demonic gorilla, revision 15362958](https://oldschool.runescape.wiki/w/Demonic_gorilla?oldid=15362958) | Full page retrieved via normal HTTPS; drop table lists one zenyte shard at 1/300. Candidate for the first fixed-probability acquisition model | Estimate the **next shard**, not a completed bracelet or four jewellery pieces; account for MM2/access, crafting/onyx/assembly and competing shard allocation separately. No kills/hour verified |
| E2 [Zulrah, revision 15366227](https://oldschool.runescape.wiki/w/Zulrah?oldid=15366227) | Full page retrieved; magic fang is listed as two 1/1,024 rolls per kill | Multiple-roll extension must distinguish expected quantity from chance of at least one; do not encode exact 1/512 per kill. No player pacing verified |
| E3 [Jagex CoX changes, 12 August 2026](https://secure.runescape.com/m=news/summer-sweep-up---agility--chambers-of-xeric-changes?oldschool=1) | Implemented table has total regular weighting 60, CM 56; tbow weight 2 in each and each Ancestral piece weight 4 | These are conditional unique weights, not per-raid personal chances. Old 69-total tables are stale. Points, caps, recipient allocation, mode and later fixes require a complete model before raid-hour claims |
| E4 [Jagex ToA drop mechanics, 28 October 2022](https://secure.runescape.com/m=news/tombs-of-amascut-drop-mechanics--osmumtens-fang?oldschool=1) | Historical primary explanation separates raid points/level, team recipient share and unique weights; Shadow is 1/24 conditional on the relevant unique roll. Low-level restrictions and stateful tertiary rewards differ | Historical model evidence, **not a current complete ToA audit**. Recheck all later updates, caps and eligibility before promotion. Never show 1/24 raids as Shadow's chance |
| E5 [RuneLite PluginPanel](https://github.com/runelite/runelite/blob/5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03/runelite-client/src/main/java/net/runelite/client/ui/PluginPanel.java) | Content panel preferred width 225; border inset 6 each side. Wrapper prefers 225+17 for scrollbar; horizontal scrolling is disabled | Do not subtract the scrollbar twice. Measure actual viewport/insets and font metrics; OS/client scaling must be checked in client |
| E6 [RuneLite ItemManager](https://github.com/runelite/runelite/blob/5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03/runelite-client/src/main/java/net/runelite/client/game/ItemManager.java) | `getImage` provides cached asynchronous game sprites; documented `addTo` refreshes labels/buttons when ready | Existing IronPath already uses this API. Preserve native sprite size/aspect and async identity guards; client data needed for genuine previews |
| E7 [RuneLite RaidsPlugin](https://github.com/runelite/runelite/blob/5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03/runelite-client/src/main/java/net/runelite/client/plugins/raids/RaidsPlugin.java) | CoX plugin observes start/floor/finish messages and reads total/personal points | Proof selected passive observation is feasible; not a universal raid-timing API or complete effort measure. In-raid timing omits scouting, banking, failures and team formation |
| E8 [RuneLite ChatCommandsPlugin](https://github.com/runelite/runelite/blob/5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03/runelite-client/src/main/java/net/runelite/client/plugins/chatcommands/ChatCommandsPlugin.java) | Recognises KC and several completion/PB message formats | Some captures specifically extract PB, not the latest duration. Reimplement only narrowly validated local event adapters; do not use PB as average pace or invoke chat upload services |
| E9 [RuneLite LootTrackerPlugin](https://github.com/runelite/runelite/blob/5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03/runelite-client/src/main/java/net/runelite/client/plugins/loottracker/LootTrackerPlugin.java) | Handles `ServerNpcLoot` and guarded reward-chest reads, including ToA metadata; multiple event paths exist | Loot event is not a run start/end or proof of all attempts. Avoid duplicate chest/loot/chat counts and private-plugin dependencies. Its remote/synced storage behaviour is not IronPath's privacy design |
| E10 [Hub restrictions](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features), [Hub review](https://github.com/runelite/runelite/wiki/Plugin-Hub-Review), [Jagex client guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1) | Review covers game rules/security. Restrictions include window/focus manipulation and content simulation. Existing plugins are not automatic approval for new ones | Passive retrospective summaries and analytic drop probabilities appear consistent with a local advisor; this is engineering judgment, not approval. Recheck before delivery; seek targeted clarification if a planned adapter/encounter model approaches a restriction. No live combat instructions, automation or playable simulation |

E1 local retrieval SHA-256: `94c97da9b26ad8c0e902a8886e3e310ca45ebb6cd055ba54e1f672fe77e618df`; E2: `87830c2f671d9133129694d55188230edfa6cc306e2d7d140f78236299cb58b3`. These identify the UTF-8 saved responses; linked oldid is the durable review reference. Web-reader access failed for these pages, while ordinary HTTPS retrieval succeeded; no access-control bypass was used. Raw pages are not redistributed. Other Wiki snippets are discovery only. No current specialist-community pacing benchmark or player session was verified in this assignment.

### What gear can and cannot tell us

Combat levels and full loadouts support checked attack comparisons. Gear may establish a supported setup and identify known hard requirements; it does not reveal execution, death rate, movement, phase skips, switches, puzzle speed, scouting, group availability, supplies or resupply habits. Even exact uninterrupted DPS does not establish average boss or raid cycle duration. Do not scale a guide's kills/hour by the ratio of two DPS values or infer a raid invocation from gear.

Separate three clocks: (1) encounter timer, (2) repeat cycle including banking/travel/respawn/failures, (3) broader acquisition effort including prerequisites and supplying the activity. Hours always name their scope. Never turn active hours into calendar days without an explicitly selected play schedule. Learning/challenge rewards such as Inferno and quiver stay qualitative until a credible individual basis exists; no equipment-to-success probability.

Delivery tiers (dependencies and acceptance live only in TASKS):

| Support | Useful output | Withheld |
|---|---|---|
| A0 reviewed route only | Remaining prerequisites, effort/tail/coordination/upkeep labels with rationale; alternatives and shared rewards | Numerical hours/odds without reviewed mechanics |
| A1 reviewed probability | Mean trials, median and P90 for a supported next reward under stated eligible trials | Time without pacing; full-item total if components/preparation remain unknown |
| A2 explicit pace scenario | Conditional hour summaries using player-entered or separately reviewed cycle pace; visible sensitivity | “Your expected time” from an unobserved benchmark; exact totals across unknown prerequisites |
| A3 validated local observation (later) | Session-derived cycle pace for matched mode/team/setup, sample size and overhead coverage; labelled recent estimate | Historic account inference, PB-as-average, automatic transfer to harder modes/new gear |
| A4 complex route models (later, selective) | Validated stateful/multi-component distributions or bounded analytic scenarios when useful | Universal simulator, every-item exact ETA, automatic route optimiser |

A0 remains useful indefinitely for individual unsupported mechanics; it must not become an excuse to abandon quantitative coverage. A1/A2 are independent of full encounter simulation. Do not block ranged/melee models on A3/A4.

### Acquisition mathematics and uncertainty contract

For independent eligible trials with a fixed success probability `0 < p < 1`, the number `N` of **future trials including the successful trial** is geometric:

```
P(N <= n) = 1 - (1-p)^n             n = 0, 1, ...
E[N] = 1/p
n_q = ceil(log(1-q) / log(1-p))     0 < q < 1
E[hours | r] = 1/(p*r)
hours_q | r = n_q/r                r = eligible trials per active hour
```

Use numerically stable `log1p`/`expm1` equivalents and boundary checks. `p=1` means one trial; `p=0` has no finite completion time; missing/invalid p or nonpositive pace is unavailable, never zero. Within-trial completion timing is intentionally not modelled. For a fixed-rate drop, past dry KC does not reduce remaining expectation or increase the next-trial chance. Never show an RNG percentage-completed bar.

**Worked E1 scenario, next zenyte shard:** p=1/300 gives mean 300 kills, P50=208, P90=690, P10=32. At an **illustrative, unmeasured** 40 eligible kills/hour: mean 7.5 h, median 5.2 h, P90 about 17.3 h. This is not a recommended/default player pace. A sensitivity input of 30–50 kills/hour gives mean 6–10 h; it is a pacing scenario range, not a confidence interval. The UI must retain “next shard only; access/onyx/crafting/supplies excluded.” Ten percent still need more than P90; the mean is not a deadline or a 50% completion point. Default to kills until a pace is supplied; no compulsory input flow.

**E2 multiple rolls:** if the two specified rolls are independent and each has probability 1/1024, chance of at least one magic fang per kill is `1-(1023/1024)^2`, whereas expected fang count is `2/1024`. These quantities differ. Verify independence/eligibility and source mechanics when implementing; don't silently round a per-roll table into an exact per-kill probability. Multiple successes and other useful rewards require the actual joint loot structure.

With varying known probabilities, `P(no success after n)=product(1-p_i)` only for conditionally independent trials; do not plug average team points into a nonlinear model and call it exact. Raid personal chance must model both the unique event and allocation to this player. Loot weights conditional on a unique are not personal drop rates. Hidden/stateful mechanics (e.g. vestige rolls), duplicate protection, guaranteed counters, ordered components and changing eligibility require an explicit state model. Unobserved hidden state cannot be inferred from a dry streak. Keep those at A0 until validated.

For sequential components, linearity can combine **means** when the actual process and all component expectations are supported; medians/P90s cannot be added. Components from the same activity can overlap and must not be treated as independent sequential grinds. Review reuse of prerequisites, allocation of shared resources and owned intermediates; an unknown component blocks a full-route estimate while permitting a clearly labelled partial estimate. Deterministic training/quest estimates are separate scenario inputs, not RNG drops. No Monte Carlo or generic graph simulator is needed for R1.5.

For an activity's useful-reward set, sum mutually exclusive outcomes only within the same roll. For independent parallel rolls use the correct complement/product; otherwise use the joint model or withhold the union. “Any useful reward” is not “all useful rewards,” and expected unique count is not expected distinct upgrades. Re-evaluate the useful set after ownership changes; duplicate scrolls or a consumed unlock must not keep inflating activity value. Supply income is not GE-priced upgrade utility.

Two different uncertainties must remain distinct: **RNG distribution conditional on mechanics/pace** and **uncertainty about mechanics, pace and account inputs**. Use mean, median and “90% by … under this scenario; 10% later,” with no false upper guarantee. Optional P10–P90 is a central predictive range conditional on the assumptions, not an empirical confidence interval. Round hours to sensible precision and expose trial units and exclusions near the numbers. A wide long tail may argue for diversification, but does not prove one player's optimal choice.

### Passive observation feasibility and validation limits

A supported activity adapter can observe public client events, correlate start/finish/loot/KC, and keep session summaries locally. No universal trustworthy duration/loot event exists for all content. A missing event, hopping, mid-run plugin enable, disconnect or logout creates a **censored/invalid observation**, not a successful fast completion. NPC despawn is not sufficient proof of player credit or loot eligibility; Boss Timers is primarily a respawn timer, not an acquisition-efficiency oracle.

Use a small explicit state machine per supported activity: idle → observed start → complete/failed/abandoned, with generation and deduplication keys. Preserve mode/team/points and timer scope; reset on identity/profile/unsupported-world transitions. Compute cycle throughput from total eligible completions divided by recorded active session time, including attributable failure and banking costs. Do not average reciprocals of individual successful kill durations. Show successful in-raid duration separately when full-cycle time is unavailable. User-explicit pauses can exclude breaks; idle heuristics must be labelled rather than secretly making pace look faster.

Initial A3 calibration proposal: at least 10 eligible completed attempts across two fully observed sessions for an automatically selected session-pace estimate; below that show observations as provisional and keep the scenario/manual fallback. This is a product minimum to test, not statistical assurance. Retain failures/censored counts, report median/range and sample count, and use a bounded recent window (e.g. last 30 matching completions) with explicit reset after material mode/team changes. Never infer drop rates from this small sample. Start with one validated activity and event-replay fixtures before widening adapters. Compare estimates to a manually timed actual-client session; exact thresholds/window remain unvalidated.

Keep raw events/pace in memory with no telemetry or chat uploads. Historical persistence would require an explicit later storage/retention/migration decision, deletion control and account isolation. Prefer direct supported public events over scraping other plugins' config/private state; optional integration needs its own API/version review. The source code proves technical possibility, not reliability on every live run or policy acceptance of every use.

### Promotion and maintenance

Each acquisition fact needs outcome/route/activity/mode, precise trial unit and recipient, prerequisites, roll count/table weights, duplicate/state rules, source URL/revision/date, original evidence summary, model version, reviewed date, known exclusions and invalidation dependencies. Each pace needs setup/team/mode, successful-versus-attempt rate, clock/overhead scope, provenance and sample/benchmark limitations. Quantitative promotion requires independent hand cases and a pinned independent reference where suitable; discrepancies withdraw affected claims.

Recheck changed drop rates, reward pools, points formulas/caps, mode restrictions, component recipes, access rules, supply requirements, item IDs and event formats before each affected release. A source update triggers review, not automatic publication. Invalid odds disable dependent time/priority claims while preserving independently supported value/path data. No runtime scraping, background web service or account export. All remaining research and implementation work is assigned in TASKS, not a second backlog here.

## R1 current mechanics review — 13 October 2026

Individual current pages were retrieved over permitted HTTPS and reviewed with their variant fields, equip requirements, bonuses, recipes and revision footers. The new `r1-*` records in the [bundled register](../src/main/resources/com/ironpath/catalog.json) retain exact revisions, review/access dates and item/model/route/context dependencies. Explanations and numeric code are original. No bulk upstream equipment data, calculator code, guide prose, game artwork or player data is redistributed.

Review covers Ahrim's usable degradation forms (broken forms excluded), Virtus powered bonuses (ancient bonus remains a gap), ordinary/twisted Ancestral, tormented ornament, fractional seers variants, Magus and its assembly intermediates, ordinary/fortified wards, other supported off-hands, god/imbued/locked/max capes, all avernic-treads combinations, Shadow/Eye/Sang charged versus uncharged forms, resource counts and ordinary Graardor. Unknown equipped items remain visible model gaps rather than empty slots. Confliction and Brimstone are recognised competitors even where their state/proc model is unsupported.

**Investigated disagreement:** the initial independently authored Sang calculation omitted the current successful-hit damage proc. Current Sang `15336446` and holy Sang `15276380`, plus Jagex's [implemented 22 July gear update](https://secure.runescape.com/m=news/summer-sweep-up-gear--pvm-changes?oldschool=1), confirm a 1/5 proc adding 8 damage and the two-blood-rune charge cost. The source register records the official page's retrieved SHA-256 as well as its publication date. The revised model handles the damage distribution, including the final successful-zero floor; it makes no healing-to-trip claim. All 540 complete-loadout rows now agree with the pinned external reference, and independent hand cases agree.

The external GPL calculator runs only in a separate temporary checkout at `89c3e25b344aea90d0189746e4b5f73dde0f0383`. [validation/r1-oracle.test.ts](validation/r1-oracle.test.ts) is an original harness; its fixtures are synthetic, not copied upstream tests. Formula inspection serves validation, not imported implementation. R1's Java test corpus records complete original inputs and generated outputs. The unchanged P4a corpus continues to pass through the shared arithmetic. [validation/R1.md](validation/R1.md) explains reproducibility, boundaries and actual checks.

ToA's multiplier/cap is hand-checked independently, but no encounter figures ship until phase and raid/path/party scaling are supported. Max hit, accuracy, DPS, expected kill time and trip performance remain distinct metrics. No live game or player-usefulness evidence was collected.

## R2 first ranged-family review — 13 October 2026

Source/model work starts independently of the pending R1.5a integration. Review current active crystal bow (23983), Bowfa, crystal armour, twisted bow, ordinary ranged formulas/styles and Eagle Eye/Deadeye/Rigour. Exact revision links, retrieved response hashes, original arithmetic/fixtures and limitations are in [R2 model validation](validation/R2-ranged-models.md) and its [source manifest](validation/r2-source-review.json). General Masori facts were reviewed as a future equipment-audit source; no exact full Masori stat catalogue or acquisition outcome is promoted here. `Ranged_accuracy` is only a redirect to general equipment stats and is not accepted as formula evidence.

Use independently authored Java `ranged-family-1` modules and the existing ordinary attack primitive; run GPL calculator `89c3e25b344aea90d0189746e4b5f73dde0f0383` only in a separate temporary development checkout. An original 1,152-case corpus agrees after correcting the harness's historical crystal-bow ID to the current active 23983. No upstream implementation, bulk data or artwork is imported. Reviewed stats/formulas are factual inputs; explanations and code are original. These dormant model checks are not a runtime promotion, source-register schema expansion or proof of personal advice/readiness. Complete setup/ownership/routes/UI integration and the remaining R2 families stay in TASKS.

## Product-reset investigation — 12 October 2026

This section records the historical product-reset investigation; the newer acquisition investigation above supersedes its ranking/UI scope. [DESIGN](DESIGN.md), [CATALOGUE](CATALOGUE.md), [UX](UX.md) and the active [backlog](../TASKS.md) control future work. Historical research below remains evidence, not a scope limit. No live account, new game client, current creator video or gated Discord strategy guide was inspected in this session. No application tests were rerun.

### Independent conclusion

The ambition is technically feasible within one free local RuneLite plugin. The present implementation is not yet a representative endgame advisor. The decisive shortcomings are tiny full-setup/model coverage, authored ranking disconnected from marginal calculations, and catalogue/schema limits that turn a vertical slice into a product ceiling. Retain the privacy/observation foundation and refactor these specific constraints. A wholesale rewrite, hosted inference or universal optimiser would increase maintenance without solving knowledge quality.

Comprehensive coverage costs continuing editorial and mechanic-validation work. Rules cannot infer enjoyment, execution, team access or supply tolerance from a bank. The appropriate product offers contextual, explainable choices with confidence and trade-offs. It should not claim a mathematically optimal universal next grind. Quantity and uncertainty handling, realistic competing loadouts and honest model boundaries matter more than a sophisticated scoring formula.

### Repository evidence

Inspected real source at PR #7 head `b50b0a93e00a9388224af0f666708921f47dda6d`, remote branch relationships and PR metadata; DESIGN records all seven open PRs and exact short heads. Main does not contain the combined implementation. This reset is stacked on #7, avoiding a documentation PR full of unmerged implementation changes.

| Finding from actual source | Product consequence |
|---|---|
| Account reader stores container item-ID sets, not quantities or slots | Cannot honestly count shards or reconstruct observed full equipment from these snapshots; extend typed observations |
| `Advisor.upgrades()` orders authored band, optional goal, application count, availability, stable ID | Useful deterministic base, but numerical deltas do not affect priority; textual breadth is a weak proxy |
| Relevance requires TRUE before a card is created | Keep readiness separate or valuable distant upgrades can disappear |
| Knowledge loader caps upgrades at 12 (plus decision/source/activity/set caps) | Remove arbitrary content ceilings; keep justified resource protections |
| Comparison schema names OCCULT/SWAMP, one fixed target/model; shared reward eligibility names delve | Generalise typed model/context/route contracts, not a universal executable DSL |
| `PoweredMagic` supports a tiny gear vocabulary and rejects unsupported slots | Existing exact numbers are honest but not representative of real endgame kits |
| Swing panel mixes rendering and calculation construction | Introduce immutable view models and small reusable components; preserve existing navigation/icon work |
| Identity, preference migration and delayed-result guards already exist | Retain and extend; a rewrite risks losing valuable correctness |

The P4a count/test baseline is historical, not newly verified execution. Runtime source data remains unchanged by this PR. In particular, newly discovered balance changes are research findings; existing active knowledge has not silently been refreshed.

### Combat-calculation options and rights

| Option | Evidence / benefits | Decision and limitations |
|---|---|---|
| [Wiki DPS calculator](https://github.com/weirdgloop/osrs-dps-calc/tree/89c3e25b344aea90d0189746e4b5f73dde0f0383) | Inspected repository, licence, `PlayerVsNPCCalc.ts`, item/type data and fixture structure. Mature TypeScript attack distributions, special mechanics and target-time infrastructure; 5,436 equipment records in this checkout help discover omissions | Valuable pinned differential reference. Not a Java library or safe drop-in RuneLite dependency. Data/seasonal entries need filtering and separate rights checks; code breadth does not prove current mechanics |
| [Better DPS Calculator](https://github.com/mcdalcin/better-dps-calculator/tree/3fd61bb3d48bdb3c0cacc57032be4a7530242ee7) | Java 11 implementation demonstrates local feasibility. Inspected licence and [SYNC_STATUS](https://github.com/mcdalcin/better-dps-calculator/blob/3fd61bb3d48bdb3c0cacc57032be4a7530242ee7/SYNC_STATUS.md): reference `b6bc098…`, last verified 12 July 2026; documents incomplete parity and unsupported cases | Candidate engineering/reference source, not an approved dependency. Its sync report is upstream-reported evidence, not a test we ran; three-month-old parity is not October correctness. Inspect individual family correctness before any reuse |
| Independent Java family core | Fits existing pure P4a architecture; explicit bounded mechanics and reusable primitives can support many upgrades | Selected approach. Implement integer rounding, distributions, model variants and interactions independently, with hand-derived and reference checks. More authoring effort; avoid rebuilding a complete simulator |
| Runtime website, JavaScript engine, browser widget or remote API | Could expose a large external calculator | Reject as default architecture: dependencies, privacy, policy and integration complexity undermine one local maintainable plugin |

The inspected Wiki and Better DPS repositories carry **GPL-3.0** licences. GPL software is usable in free open source, but copying/deriving or combining it can impose distribution/source/licence obligations on the combined work; it cannot simply be relabelled as BSD-2-Clause. This is a reuse decision to make explicitly, not a claim GPL is incompatible with RuneLite. Independently authored code plus development-only comparison is the selected path. If maintaining a family independently proves disproportionate, assess a clearly licensed extraction or deliberate project-licence change rather than covert copying. No such import or relicense occurs here.

Pin every oracle run to commit + complete inputs. Record discrepancies and current official fixes; matching two stale implementations is not validation. Original synthetic input/output fixtures must have provenance; inspect rights before redistributing upstream test/data corpora. This session used reference item names for discovery, not a copied runtime dataset. Wiki article text, wiki datasets and game sprites have separate rights from calculator code. The historical Wiki policy discussion below is still qualified: its full current copyright page was inaccessible. Verify actual applicable terms before importing bulk facts or adapted text; attribution is not a substitute.

**Practical calculation boundary:** complete baseline/candidate setups + target profiles feed shared attack distributions. Derive max hit, hit probability, expected damage/attack and theoretical sustained DPS only for supported mechanics. Shadow, tbow, Fang, Scythe and Soulreaper are family modules, not arbitrary authored bonuses. Expected specs/debuffs need a stated energy/HP/team scenario. Finite-target expected time needs overkill and timing; trip gains require incoming damage, resources, execution and policy checks. See DESIGN Q0–Q5 and the proposed R1 slice. Useful numbers remain required even when a difficult metric is deferred.

### Current ecosystem and release research

[CATALOGUE's evidence register](CATALOGUE.md#evidence-register) links eight official update/announcement pages read for this reset, with publication dates and scope. It captures important omissions from older BiS lists: rupture/kisten/Seeker families, Hallowfell, newer ranged equipment, fangs and elemental amulets, changed Inquisitor/spec behaviour and permanent penny handling. These are researched candidates with explicit gaps, not all validated implementation facts.

Distinguish actual releases from proposed changes. The latest follow-up leaves elemental amulets unchanged despite a discussed adjustment. The Fractured Archive is announced for **20 October 2026**, after this research cut, so its named rewards belong to a future watchlist. Older CoX loot weighting must not be reused as current effort evidence after the August change. A source saying “best” or marketing a multi-target weapon is not an account-specific DPS model.

Direct Wiki pages were blocked in this environment. Indexed snippets and the calculator's equipment inventory can identify candidates but do not constitute a current per-item audit. Catalogue D rows explicitly expose that limitation; they require full source/mechanic/path validation before promotion. The four V outcomes describe P4a implementation state, not a fresh expert endorsement of every retained claim. Review released changes against those facts during implementation.

### Sources, experienced players and maintenance

| Source | What was checked / appropriate use | Update and reuse limits |
|---|---|---|
| [Jagex news archive](https://secure.runescape.com/m=news/archive?oldschool=1) and linked individual updates | Release state, implemented balance changes, hotfixes and future watchlist | Inspect following fixes, not just launch; proposals may change. Link and write short original factual notes; no article/image replication |
| Wiki item, mechanics and strategy pages; calculators | Strong mechanic/acquisition references, revisions and candidate discovery | Community edited; current full-page access incomplete here. Strategy is contextual; separate content/data/code/image rights |
| [We Do Raids gear resources](https://wedoraids.com/resources/gear-guides/) | Public resource page directs to maintained Discord gear setups | Gated guides were not inspected. Mains' budget progression differs from Ironman acquisition; record actual encounter/version, not server reputation as proof. No copying community tables |
| [Molgoatkirby community spotlight](https://secure.runescape.com/m=news/community-spotlight-molgoatkirby?oldschool=1) | Official 14 August interview identifies a relevant endgame theorycraft/speedrunning perspective | Discovery of an experienced source, not independent verification of a gear claim. Inspect primary videos/notes and assumptions before adoption; speedrun utility need not equal typical account priority |
| [GearScape](https://gearscape.net/) | Public optimiser/tool as a complementary way to discover competing setups | Do not make it mandatory or upload accounts. Tool output needs mechanic/source/version checks; no assumption its code or data is licensed for reuse |
| BRUHsailer and other efficient-Ironman guides (historical links below) | Long route dependencies and alternative progression hypotheses | Starting-account efficiency is not an endgame universal order; adaptation licence does not establish original guide rights |
| Player feedback and specialist encounter communities | Identify missed alternatives, actual friction and contradictions | Record scenario and reproducible reason; popularity/consensus alone does not establish mechanics; do not harvest private messages |

No creator endorsement or community consensus is claimed. Use sources according to claim type: official implemented change for release state; checked mechanics/formula for numbers; specialist experience for scenario relevance; player feedback for usefulness. Conflicting sources become a visible issue with the affected claim unavailable or conditional.

**Maintenance policy:** each fact/context stores stable ID, source URL and revision where available, access/review dates, evidence type, affected items/models/routes, original summary, licence/reuse decision, confidence and known exceptions. Model versions identify rules, not just software builds. Review new game updates when they affect covered content; perform a regular lightweight inventory/staleness sweep (monthly starting cadence, adjusted to actual workload). Review age is a signal, not automatic proof a fact is wrong. Known invalidity disables the affected claim. Offline bundled knowledge cannot promise immediate hotfix freshness.

Development-time scripts may diff permitted official announcements/API snapshots, RuneLite ItemID changes and pinned calculator revisions; CI can validate missing metadata, unresolved candidate rows, route cycles and regression fixtures. Fetch only permitted sources with caching/rate limits; a robots/access block is not permission to bypass it. Human review produces an ordinary bundled-data/code PR. No runtime scraping, hosted production service, user telemetry or per-account AI is required. Budget maintenance by prioritising impactful changed families and seeking contributions, not by hiding gaps behind a permanent catalogue cap.

### Native UI and policy evidence

Inspected [PluginPanel source](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/ui/PluginPanel.java): a Swing panel with `PANEL_WIDTH=225`, scrollbar allowance 17 and border offset 6. Existing IronPath already uses `ItemManager`. [UX](UX.md) therefore specifies actual narrow Swing screens with client sprites and separate value/path views, rather than a browser mockup pretending to be implementable.

Checked [Plugin Hub repository](https://github.com/runelite/plugin-hub) and [Rejected or Rolled Back Features](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features) (page showed update 6 October 2026). Relevant restrictions include code/runtime APIs, client window/focus manipulation and content simulations; existing plugins may be grandfathered. Conventional local calculations are a defensible engineering direction, not a blanket Hub approval. Wider windows need current verification and Q5 must be assessed before any encounter-simulation-like feature ships. This uncertainty does not justify dropping Q1–Q4 quantitative work.

## Historical foundation and implementation research

The complete previous RESEARCH.md follows verbatim from `b50b0a9`. Earlier v0 scope, maintenance budgets and planned next steps are superseded; source findings and their stated limitations remain historical evidence. Current decisions are above and in DESIGN.

<details>
<summary>Expand earlier ecosystem, T2, P3 and P4a research and calculation evidence</summary>

# Feasibility and source audit

Researched 12 October 2026. This is a bounded source/code review, not an in-game evaluation, legal clearance, exhaustive competitor study or validated progression dataset.

## Assessment

**Technical feasibility: good. Our recommendation quality and usability: to be validated. Maintenance feasibility: conditional on narrow coverage.**

**Decision update, 12 October 2026:** Petter reports that Iron Compass's current workflow is unusable for his needs. This is direct user feedback, not a general usability study. The project will proceed independently. Iron Compass remains a source of technical reference and feature overlap; its existence is not a reason to stop, and outperforming it is not an implementation gate. Repository name `PetterHL/ironpath` and public visibility are approved.

A local rule-based plugin can match account observations to curated advice and explain its result. An AI model is unnecessary for this bounded job. Existing account-aware plugins reinforce that conclusion. The difficult work is contextual strategy: an item's nominal strength is not its marginal value to a particular account, and the player's time, preferences and ability cannot all be inferred from game state.

Do not start by harvesting a large corpus. Start with a dozen defensible decisions and five contrasting accounts, checking whether players can understand and act on the advice. A wider source collection is useful only when it resolves an actual recommendation gap. Bank value, completionist goals and max efficiency are not interchangeable with PvM capability.

## 1. RuneLite and distribution

The official [Plugin Hub repository](https://github.com/runelite/plugin-hub) describes public source repositories, the example template, manifest submission with a pinned commit, and commit updates for later releases. Its inspected instructions recommend Java 11; the [example build](https://github.com/runelite/example-plugin/blob/master/build.gradle) sets Java release 11. Recheck at implementation time rather than imposing a modern Java/Kotlin stack.

[Hub review documentation](https://github.com/runelite/runelite/wiki/Plugin-Hub-Review) says initial submissions and updates are reviewed for security and rule compliance. This is not verification of usefulness, factual accuracy, performance or compatibility. Approval is not guaranteed by this research.

The current [restrictions](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features) require Java and restrict reflection, native code, subprocesses and downloaded executable code. They direct plugins to injected Gson, the permitted browser utility and RuneLite's `Filepath` API. Follow the current rules rather than copying grandfathered behaviour. A planning sidebar is a reasonable candidate; it must not become a combat helper. [Jagex's guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1) prohibit features such as automatic boss attack/prayer/position guidance.

The [Hub information page](https://github.com/runelite/runelite/wiki/Information-about-the-Plugin-Hub) also requires disclosure for third-party server communications. The recommended MVP avoids such plugin traffic entirely. Public Git hosting and Hub distribution remove the need for owner-operated runtime infrastructure; they do not promise permanent free third-party services or zero developer effort. Build automation is optional, not a prerequisite, and paid runners/services should not be enabled by default.

## 2. Account-data feasibility

| Data | Verified mechanism | Reliability boundary and decision |
|---|---|---|
| Skills/levels | `Client.getRealSkillLevel`, skill experience and skill-change events [A] | Good logged-in input; use real levels, not temporary boosted values. Include now. |
| Inventory/equipment | `Client.getItemContainer` and container events [A] | Current observed containers, not every possible storage. Resolve exact variants and usability. Include now. |
| Bank | Bank item container; existing account-state implementation [B] | Observe after opening the bank. Cache only within the active character session; filtered bank UI is not the data model. “Not observed” is not global nonownership. Include now. |
| Quests | `Quest.getState(Client)` [C] | Current implementation invokes a quest-status script. Read on the client thread after login initialisation; query only necessary quests. Include selected quests. |
| Other unlocks | `getVarbitValue`/`getVarpValue` [A] | Requires maintained mappings and verified meaning. Consumed prayer scrolls need unlock state, not possession checks. Add only unlocks used by rules; use labelled manual fallback. |
| Collection Log | WikiSync's current source handles log scripts and a user-triggered sync control [D] | Technically feasible; not a universal complete-account getter. Current Hub policy explicitly allows a narrowly specified search-toggle action for obtaining log data. Coverage/timing need a spike. Historical acquisition is not current possession. Defer. |
| Boss KC | Core Chat Commands records observed counts; HiscoreClient offers external lookup [E] | Local history is incomplete; public lookup introduces network, freshness and unranked/missing cases. No verified single complete local getter. Neither KC nor absence proves skill. Defer. |
| Combat Achievements | Generated `CA_TASK_COMPLETED_*` varplayers and a maintained tracker [F] | Completion is locally readable with task mappings; mappings and login timing still need validation. Do not claim all achievements are unavailable or necessarily require widget scraping. Defer because v0 rules do not need them. |
| Character identity/preferences | `ConfigManager.getRSProfileKey` and profile lifecycle [G] | Supports isolation, but its configuration can synchronise remotely. Persist sensitive manual facts separately; keep bank observations in memory. Verify local-file API in T1. |

**A:** [Client.java](https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/Client.java), read directly from source.

**B:** [Iron Compass AccountStateService](https://github.com/gabworknestio-beep/iron-compass/blob/4f55942723b2f1fd609213ea1b2b528e7641a670/src/main/java/com/ironcompass/state/AccountStateService.java), inspected as a reference, not copied.

**C:** [Quest.java](https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/Quest.java).

**D:** [WikiSync source](https://github.com/weirdgloop/WikiSync/tree/1ff4ea0653395b552844d658d3668815d82e0a79/src/main/java/com/andmcadams/wikisync), particularly `WikiSyncPlugin` and `SyncButtonManager`; [permitted collection-log exception](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features#sensitive-apis). The old Collection Log README's requirement to visit every page is not sufficient evidence for today's best acquisition method.

**E:** [ChatCommandsPlugin.java](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/plugins/chatcommands/ChatCommandsPlugin.java).

**F:** [VarPlayerID.java](https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/gameval/VarPlayerID.java); [Combat Achievements Tracker](https://github.com/ehubbartt/combat-achievements-tracker). Tracker documentation says progress loads on login; no live execution was performed here.

**G:** [ConfigManager.java](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/config/ConfigManager.java); [Filepath.java](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/util/Filepath.java). The sync implication is from source, not an assumption that “no network code in our plugin” makes every configuration write local-only.

## 3. Existing solutions and reuse

| Project | What the inspected material establishes | Implication |
|---|---|---|
| [Iron Compass](https://github.com/gabworknestio-beep/iron-compass) | Account-aware goals, observed-bank handling, alternatives and a 40-objective gear catalogue across progression stages. Gear evaluator and account-state source were inspected; output quality was not tested. | Overlapping reference, not an assumed good product or a gate to our development. Petter reports poor usability for his needs. BSD-2-Clause code permits reuse subject to notices; catalogue rights require separate scrutiny. |
| [Ironman Progression](https://github.com/Wizard-Fish/Ironman-Progression) | Repository describes live quest/skill progression with phased checklists, gear suggestions and optional observed-bank hints. | Further evidence this is an existing category, not an empty niche. No need for another broad checklist. |
| [CoX Gear Planner](https://github.com/davidcottrellcoding/cox-gear-planner) | Repository describes owned-equipment raid planning and room-specific DPS calculations. | Complementary specialist tool; do not duplicate its optimiser. README/source availability does not itself establish runtime quality or Hub availability. |
| [Log Adviser](https://github.com/SFranciscoSouza/LogAdviser) | Ranks remaining collection-log activities by expected time to a new slot. | Useful comparison for transparent ranking, but collection speed is a different objective from PvM capability. |
| [WikiSync](https://github.com/weirdgloop/WikiSync) | Exposes implementation patterns for reading account/log state; its normal operation uploads public player data to a Wiki service. | Study locally readable patterns, do not require its service or silently export account data. |
| [OSRS Wiki DPS calculator](https://github.com/weirdgloop/osrs-dps-calc) | Existing loadout/monster calculation project; GPL-3.0 repository. | Use as a maintainer validation aid when checking a disputed comparison, not as a new Java dependency or an assumed permissively licensed code source. |

Distribution checks used Plugin Hub tree commit `03534bbdcd2bb2611eabca11c644fde1c992acb6`. Iron Compass is listed under manifest name **`ironpath`**, not `iron-compass`; it pins `96dc43b51c6d2cf96ed179330dd7b5d85edc5af3`. That pinned README and gear JSON were read; the 40-objective catalogue is present there too. The default branch was separately observed at `4f55942723b2f1fd609213ea1b2b528e7641a670`; do not assume its every feature is distributed. [Pinned manifest](https://github.com/runelite/plugin-hub/blob/03534bbdcd2bb2611eabca11c644fde1c992acb6/plugins/ironpath).

The same tree lists Log Adviser and Combat Achievements Tracker. The older `evansloan/collection-log` manifest is **disabled as unmaintained**, so it is not a sensible mandatory dependency despite appearing in searches. [Manifest](https://github.com/runelite/plugin-hub/blob/03534bbdcd2bb2611eabca11c644fde1c992acb6/plugins/collection-log).

**Build decision:** proceed with a small independent plugin and validate useful advice through a simple user experience. Do not fork Iron Compass's entire broad system merely to save scaffolding work. Reuse RuneLite's own public utilities first and avoid reading another plugin's private configuration as an API. Existing plugins may inform technical choices but do not determine whether this project should exist. Nobody has been contacted.

## 4. Knowledge sources and rights

| Source | Appropriate use | Quality and reuse boundary |
|---|---|---|
| [Official Jagex news](https://secure.runescape.com/m=news/archive?oldschool=1) | Releases, balance changes and authoritative mechanical changes | Separate Game Updates from Future Updates/polls. Published does not mean implemented. Link and write original notes; do not redistribute articles/artwork by default. |
| [OSRS Wiki Ironman guide](https://oldschool.runescape.wiki/w/Ironman_guide), item and boss strategy pages | Requirements, acquisition mechanics, rates, variants and encounter context | Wiki data is not a universal progression policy. Strategy/user pages may be opinionated or stale. Direct page/API access was blocked in this environment, so no current full-page item audit was completed. |
| [BRUHsailer web adaptation](https://github.com/umkyzn/BRUHsailer) | A reference route and a way to locate the original authors' material | Efficient-account assumptions may not suit an established account. A web adapter's software licence is not automatically a licence to the underlying guide. Verify provenance and permission before copying. |
| [ironman.guide gear page](https://ironman.guide/gear) | Discover candidate equipment and linked Wiki pages | The publisher says it mirrors Wiki progression and was checked 17 September 2026. It is therefore not independent corroboration of the same Wiki claim. No tables are imported. |
| [Creator video candidate: ToA guide](https://www.youtube.com/watch?v=N2DCZ1nt4p4) | Candidate practical strategy, assumptions and alternative setups | Search returned a 1 April 2026 solo-300 ToA guide. The video itself could not be fetched/viewed; no tactical claim is adopted from it. Evaluate encounter/version/skill context, not subscriber count. Use links and original summaries; no transcript harvesting. |
| Player reports and discussions | Identify missing alternatives, stale advice and disagreements | Leads for validation, not automatic facts. Require a reproducible explanation and independent mechanic checks. |

The Wiki's indexed [licensing template documentation](https://oldschool.runescape.wiki/w/Template:CC-BY-NC-SA/doc) identifies **CC BY-NC-SA 3.0** for Wiki content. The [licence deed](https://creativecommons.org/licenses/by-nc-sa/3.0/) requires attribution, noncommercial use and share-alike terms for adaptations. The full current [Wiki copyright policy](https://oldschool.runescape.wiki/w/RuneScape:Copyrights) was not accessible here; file-specific licences and current terms must be checked before reuse.

Choose BSD-2-Clause for original project code/documentation. Do not relabel copied/adapted Wiki text or guide datasets as BSD. If licensed source material is later redistributed, identify it separately, retain required credit/licence/history links, record modifications and verify compatibility of the intended use. Attribution alone is not permission, and “free plugin” alone does not resolve every reuse condition. Original rules should be independently authored from checked evidence; that is not blanket clearance for systematic extraction. This foundation contains no copied progression dataset or third-party images.

## 5. Update options

| Approach | Assessment |
|---|---|
| Bundled JSON + ordinary Hub updates | Recommended now: reproducible, offline, source-reviewed and reversible. Delivery waits for an accepted Hub update. |
| Maintainer metadata watcher | Sensible later: detect feed/revision changes and produce a review list. No player data or runtime service. Validate API access first. |
| Remote JSON fetched by every player | Defer: adds availability, compatibility, privacy disclosure and review questions. “Only data” does not automatically make arbitrary rules acceptable. No need for this at twelve decisions. |
| Automatic guide ingestion and ranking | Reject for v0: syntax extraction does not establish source independence, legality, context or strategic correctness. |

[MediaWiki Revisions](https://www.mediawiki.org/wiki/API:Revisions) documents revision metadata; [RecentChanges](https://www.mediawiki.org/wiki/API:RecentChanges) supports discovery. These establish a possible mechanism, not verified OSRS Wiki endpoint access or permission. Track the known pages and periodically look for genuinely new sources/content, rather than only polling a fixed list forever.

The live Jagex archive on the research date explicitly separated an upcoming [Fractured Archive overview](https://secure.runescape.com/m=news/the-fractured-archive---everything-you-need-to-know?oldschool=1) from released game updates. This illustrates why a source watcher needs review before changing live recommendations; the overview is not adopted as shipped progression content here.

## 6. Remaining validation gates

- **Value and usability:** test five mid/late-game situations. Players should be able to find a suggestion, understand why it fits their account, inspect an alternative and decide what to do. Include established raiders; no competitor comparison is required.
- **Account accuracy:** test unopened/opened/closed bank, placeholders, variants, logout, hop and two character/profile changes in a real development client. No authenticated game session was available here.
- **Privacy:** verify the supported local file directory API and inspect that neither facts nor manual confirmations enter synced config/logs.
- **Knowledge:** review the initial comparisons against current Wiki/official mechanics and encounter-specific expert advice. Full guide contents and permissions are not verified by search snippets.
- **Effort:** measure how long one small catalogue update takes. Reduce coverage if the proposed weekly review budget is unrealistic.

These uncertainties do not prevent implementation. They do prevent claiming a complete or validated advisor today.

## T2 bounded review — 12 October 2026

T2 fetched current full Wiki pages successfully and reviewed the relevant requirements,
mechanics, alternatives and variants. This supersedes the foundation's access limitation
for these selected sources only. The bundled [source register](../src/main/resources/com/ironpath/catalog.json)
contains 38 exact Wiki revision links and the official RuneLite
[item definitions](https://github.com/runelite/runelite/blob/42a6f17a6a2e8e478aa763890ecd0181a59dad38/runelite-api/src/main/java/net/runelite/api/gameval/ItemID.java).
Access/review dates are 12 October 2026; source revision IDs and decision review dates
are recorded separately. This is an editorial/mechanics review, not an expert consensus
claim, live account audit or player-usefulness test.

The current CoX equipment discussion includes Eye of Ayak and dragon hunter wand
magic alternatives, scythe/Soulreaper melee options, and elder maul/BGS/DWH team
coverage. These informed conservative suppression and original comparisons rather
than an imported equipment ranking. Empty/enhanced/ornamented items are reviewed
as explicit IDs. An empty staff can prevent redundant assembly without establishing
a charged powered weapon; a dragon hunter wand does not establish an available
spellbook or spell. Scythe/Soulreaper components do not count as assembled weapons.
The lance's required Barbarian Training sections are named but remain unobserved.
Default active crystal pieces are supported; coloured armour and other unlisted
forms remain outside this bounded baseline. No boss-access quests or team loadouts
were inferred from weapon possession.

One attempted URL, `Holy_Sanguinesti_staff_(uncharged)`, returned HTTP 404.
It is not an evidence source. The accessible full
[holy staff page](https://oldschool.runescape.wiki/w/Holy_Sanguinesti_staff?oldid=15276380)
shows both charge states and their IDs; the official definitions independently
identify 25731/25733. No factual claim was adopted from the failed URL.

The Wiki's copyright page is a soft redirect. Its linked
[Weird Gloop licensing policy, revision 1299](https://meta.weirdgloop.org/w/Licensing?oldid=1299)
was fetched and read, together with the page footers. They identify CC BY-NC-SA 3.0
for the relevant current Wiki text, with separate media/additional terms.
RuneLite's pinned root licence was also checked as BSD-2-Clause. Source records
describe evidence/reuse scope; original Ironpath text and rules do not redistribute
guide prose, tables, images, source code or a third-party progression dataset.
This limited fact-and-link approach is not blanket permission for future copying.

All nine decisions and the five representative fixtures are documented under T2 in
`TASKS.md`. The fixtures are synthetic; T1's passed static review does not establish
RuneLite accuracy. Real-client checks and player judgement remain pending.


## P3 bounded source review — 12 October 2026

For this delivery, full current Wiki pages were fetched successfully through the
configured network path. The source register now pins the actual inspected
revisions, access/review date, author, affected decisions, licence and reuse basis.
The nine existing decisions retain their reviewed T1/T2 evidence. New explanations
and preparation bands are original editorial choices; this is a static fact and
conditional strategy review, not expert/player endorsement or an in-game trial.
No prose, equipment tables, images or progression dataset is imported.

| New route | Reviewed evidence and important boundary |
|---|---|
| CG / Bowfa / crystal | The Gauntlet `15367929`, strategies `15359999`, Song of the Elves `15363333`, Bowfa `15366535`, crystal equipment `15355197`. SOTE access is distinct from the unobserved first regular completion needed before CG. NPC singing avoids a false mandatory Crafting/Smithing level. The route targets another enhanced seed only while its weapon gap is confirmed; it does not certify a complete crystal kit. |
| Slayer to 87 / first trident | Cave kraken `15342837`, Kraken `15368743`, existing seas-trident evidence. Separate skill milestone from seeking a task/drop. Small cave krakens are a reviewed first-trident alternative to the boss; neither task nor completion probability is inferred. |
| Zulrah / magic fang | Zulrah `15366227`, strategies `15356843`, Regicide `15369687`, retained fang/swamp evidence. The encounter page describes Port Tyras progress and sacrifice dialogue; the strategy page simplifies this to completed Regicide. Use completion as a conservative supported gate and explicitly leave dialogue/partial progress unobserved. Assembly's 59 Crafting is not Zulrah access. |
| Low-level ToA / fang | Tombs `15356620`, strategies `15356953`, Beneath Cursed Sands `15363320`, base keris `15254688`, retained fang evidence. Raid access is BCS; fang/Bowfa/higher prayers are not entry requirements. The base keris and observed three-style baseline support planning only, not a full kit or a guaranteed useful raid level. |
| Moons / atlatl | Moons `15357442`, strategies `15357348`, atlatl `15356407`. Perilous Moons unlocks chest claims. Herblore helps local supply preparation; atlatl damage uses strength context and ammunition. It remains a conditional alternative after CG is declined, rather than an equivalent weapon at every boss. |
| Royal Titans / prayers | Titans `15357424`, strategies `15367754`, Deadeye `15222979`, Mystic Vigour `15335909`, and the two scroll revisions pinned in the register. No quest/skill entry gate; duo, spell and kit assumptions are visible. Deadeye/Mystic Vigour require 62/63 Prayer to read/use; their higher Rigour/Augury unlock alternatives satisfy the bounded target. Observed relevant unconsumed scrolls exclude further acquisition. |

The three new weapon forms and two prayer-scroll IDs were checked against the
Wiki item pages and RuneLite 1.13.1's supported generated item constants: base
keris `25979`, atlatl `29000`, inactive Bowfa `25862`, scrolls `30626`/`30627`.
The inactive Bowfa suppresses another enhanced-seed route without passing a usable
weapon check. Existing reviewed charged/empty/cosmetic variants retain their
separate meanings. The atlatl's full loadout and enhanced keris variants are outside
this slice's observed preparation matcher.

Selected quest completion uses `Quest.getState(Client)` on the client thread after
initialisation. The compiled public API was inspected: it runs the supported quest
status script; missing/unavailable results remain unknown. Relevant progress events
use generated SOTE/BCS/PMOON/Regicide identifiers only to invalidate/coalesce a read,
not to invent quest-completion thresholds. Deadeye/Mystic Vigour use the permanent
unlock constants `PRAYER_DEADEYE_UNLOCKED` and
`PRAYER_MYSTIC_VIGOUR_UNLOCKED`, distinct from active prayer state.

Bundled coverage is 16 decisions / 61 exact IDs / 62 source records / seven goals.
Six acquisition/unlock choices cover six non-CoX activities, alongside
one Slayer milestone and the retained owned-parts/prayer/CoX decisions. Missing
storage alone never activates an acquisition route. Source-page availability and
static fixtures do not establish live observation accuracy, real-client usability,
expert consensus, rights to redistribute source material, or Hub approval.

## P4a evidence and bounded numerical validation — 12 October 2026

This delivery extends the existing catalogue and evaluator; it does not import a
calculator engine or equipment/monster dataset. Four reviewed outcomes reference
nineteen decisions (the original sixteen remain), eighty-one exact item forms and
eighty-four sources. Thirteen individually checked stat records support only the
ordinary trident reference. Source authors, exact revisions, access/review dates,
reuse basis and affected decisions remain in the bundled source register.

New mechanics review accessed the actual pages over HTTPS and recorded their
revision footers. The important additional route records are smoke devil
`15357607` (93 Slayer, active task and face protection), Nex `15371100` and The
Frozen Door `15166745` (permanent prison unlock versus current entry/team checks),
Armadyl crossbow `15364436`, nihil horn `15190673` and shards `15190623` (distinct
component work; shards can come from Ancient Prison monsters), The Final Dawn
`15369722`, Doom `15360840` and avernic treads `15314043` (quest, individual delve
eligibility, base versus eight exact treads forms and optional additions).

Cloth `15248146`, tormented bracelet `15301600` and confliction gauntlets
`15330811` bound the conditional shared opportunity: 83 Crafting, 70 Smithing,
cloth, a tormented bracelet and 10,000 demon tears at a bank; 90 Hitpoints to wear.
Already-owned cloth/gauntlets suppress that opportunity. The Eye opportunity
checks recognised advanced staff ownership independently. Neither one assumes
quantity, simultaneous rewards, access readiness or any ability to clear a delve.
Existing Zaryte/occult/seas/swamp/advanced-alternative evidence was reused and
current mechanics were rechecked. These are original limited explanations, not
copied source prose or tables; source review is not player endorsement.

`Quest.THE_FROZEN_DOOR` and `Quest.THE_FINAL_DAWN` compile against RuneLite 1.13.1.
The reader uses the supported `Quest.getState` script with unknown fallback.
Selected `NEX_PERM`/`VMQ4_PRIMARY` progress events request a coalesced read; there is
no all-quests/tick scan. Tests exercise those script/progress facts and clearing;
real-client accuracy remains pending. Smoke-task/face-protection, Nex visit/team,
ruby-special context and material quantities are explicitly timestamped manual
planning assumptions. They must be rechecked before use, not treated as live
observations or inferred from ownership.

The independently authored model follows the checked mechanics at magic formula
`15321382`, magic damage `15371256`, ordinary Dharok `15316733`, seas `15364129`,
swamp `15364128`, occult `15314514`, Mystic Might `15362148` and Augury `15362822`.
Supported flat neck/boot stats have individual pinned evidence in the register.
No wind weakness is applied to non-elemental powered-staff attacks.

For visible Magic L, base seas/swamp hits are `floor(L/3)-5` and
`floor(L/3)-2`. Floor the base multiplied by total supported gear/prayer damage.
Accuracy uses `floor(L × prayer multiplier) + style bonus + 8`, with +3 for
Accurate or +1 for Longrange; invisible style bonuses do not change base damage.
The attack roll is that effective level times `(64 + magic attack bonus)`.
Dharok's defence roll is `(1+9) × (64-11) = 530`. Inclusive rolls give
`1-(D+2)/(2(A+1))` when A>D, otherwise `A/(2(D+1))`.

On a successful attack, the uniformly rolled zero is raised to one; misses stay
zero. The conditional mean is `M/2 + 1/(M+1)`, then multiply by hit chance and
divide by 2.4 seconds. For example, at 90 Magic with seas, no boost/prayer and
all non-weapon slots explicitly empty, A=`101×79=7979`; accuracy is `29/30`.
Successful outcomes sum to 326 across 26 rolls, so DPS is
`(29/30)×(326/26)/2.4 = 5.0502136752`. Swamp gives max 28, A=8989 and
`(1-266/8990)×(407/29)/2.4 = 5.6746768440`. Adding only an occult instead gives
max 26, A=9191 and DPS 5.2749035662. These are reference attacks, not encounter
speed, trip length, resources or actual player DPS.

Other original hand fixtures check Magic 75/78/89/90/92/93/99 breakpoints, low/equal/
high attack-roll branches, zero misses versus successful zeros, negative boot
bonuses, both styles, Mystic Might (+15% accuracy/+2% damage), Augury (+25%/+4%),
and a full +4 magic-potion assumption. Zero delta from an already selected occult
is retained rather than inflated into a percentage gain. Unknown/incompatible
slots, empty/manual weapons and unavailable prayer/equip requirements withhold
numbers. Whole max hits, one-decimal hit chance and two-decimal DPS are displayed;
accuracy differences use percentage points, and no relative-percent division is
needed.

The external GPL-3.0 **Wiki calculator** was actually run at
`89c3e25b344aea90d0189746e4b5f73dde0f0383` in a separate temporary checkout, with its
own existing data and dependencies. The original synthetic harness is
[validation/p4a-oracle.test.ts](validation/p4a-oracle.test.ts); no upstream test
corpus was copied. Its 234 rows span the seven Magic levels, seas/swamp (swamp
starts at 78), empty/fury/occult necks, both styles and none/Mystic-Might-plus-potion/
Augury scenarios. All other slots are explicitly empty, NPC is 1673 and no special
mechanics are selected. Installation used the session proxy/CA trust and a
writable temporary Yarn directory. No external code/data was added to the plugin
or its runtime dependencies.

Reproduce in a separate reference checkout: check out that exact commit, run its
Yarn 4.9.2 `install --immutable`, copy the original harness to
`src/tests/calc/IronpathP4a.test.ts`, and run
`yarn jest src/tests/calc/IronpathP4a.test.ts --runInBand --coverage=false`.
The local run passed; the generated outputs are retained in
`src/test/resources/com/ironpath/p4a-oracle.csv`. Java tests match integer max hits
and rolls exactly, hit chance within `1e-12` and DPS within `1e-10`. All 234 match;
no discrepancy remains. The separate hand derivation agrees as well. Agreement
with this pinned implementation is bounded validation, not proof of game
correctness or a licence to expand into unsupported combat mechanics.

</details>
