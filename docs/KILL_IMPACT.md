# Personalized Kill Impact — research and design

**KI1 implementation update, 14 October:** [the experimental Zilyana pilot](validation/KI1.md) now implements bounded capture and supported post-completion worn-state offence comparisons on `ki1-recent-boss`. The design/research evidence below retains its original scope; no KI2 seconds or actual-client acceptance is implied.

Original research status, 14 October 2026 · **Selected direction; not implemented or validated in the game at research time.**

Inspected `main` at `cb7c2cd874e8225581e85566f652236ee7e6feb3`, R2 #16 at `32cb24a`, documentation #17 at `75897d5`, and R3a #18 at `d9e897b7114591287a8667616969b7bd9d09a0b8`. This documentation change builds on #18 because its comparison, ranking and card contracts are concrete dependencies. No dependency is merged. [TASKS](../TASKS.md#personalized-kill-impact--ki1ki3) is the sole backlog; this document explains the design and evidence, not a competing task list.

## Decision

**Build Kill Impact inside IronPath as an optional, passive post-kill context for the existing upgrade advisor.** It can make recommendations substantially more tangible. Do not create another plugin, a combat coach, a general encounter simulator, or a second recommendation catalogue.

The useful promise is: **“Here are upgrades relevant to the boss you actually fight, compared with what you used.”** A supported later claim is **“Estimated average time saved on comparable kills, assuming the same method.”** We cannot truthfully promise “this exact kill would have been 8 seconds faster.” The unseen alternative has different random hits, attack opportunities and potentially phases.

Deliver observed kill context plus supported offensive comparisons first. Give seconds a concrete subsequent delivery and validation gate; do not quietly substitute DPS forever. **User clarification, 14 October: Vorkath was an example, not the product scope or a mandated first boss.** Select the first observation context by useful account coverage, observability and reusable mechanics; normal post-quest solo Vorkath is one candidate. Keep the current UI improvements, R3b melee coverage and R4 reusable mechanics moving. Shared capture supplies R1.5b's first observation work; avoid a duplicate CoX-specific collector. CoX remains explicitly included in the broader encounter/raid coverage below.

## Intended coverage: most PvM bosses, including raid encounters

**The product target is useful Kill Impact across most relevant PvM bosses, with raid bosses/rooms as first-class coverage and whole-raid impact as an additional calculation scope.** A small first delivery does not narrow that target. The detailed Vorkath discussion below is a worked feasibility example; its mechanics must not become assumptions embedded in shared contracts.

| Encounter family | Required design scope | Delivery distinction |
|---|---|---|
| Ordinary solo bosses | Actual setup, target and complete kill, with method-specific timing | First pilot selected by evidence, not by the example in the original request |
| Multi-phase/style-changing bosses | Phase boundaries, gear switches, immunity/reduction and relevant targets | Extend shared phase/setup contracts; no single full-fight DPS shortcut |
| Team bosses | Player role/contribution, party/scaling and shared reductions | Local offensive gain can be supported before personal influence on team kill time |
| CoX/CM, ToA and ToB/hard mode rooms/bosses | Raid → room/encounter → phase/target identity; mode, relevant scaling, team/role and actual switches | Deliver useful individual encounter comparisons without waiting for total-raid timing |
| Complete raids | Supported non-overlapping encounter contributions plus transitions, puzzles, preparation, deaths and other overhead | Separate estimate with explicit coverage; do not multiply total raid time by local DPS or sum independent medians/quantiles |
| Waves/multiple targets | Wave/target scope, geometry and contribution where relevant | Distinguish a target/phase gain from completion-time or readiness claims |

The encounter contract must support optional parent activity/run ID, encounter/room ID, phase/target ID, mode/scaling inputs and party/role evidence from the outset. Unknown party composition or teammate damage must stay unknown; no remote party telemetry is required. Existing target profiles and combat families remain reusable. A new boss using existing mechanics should primarily add reviewed data and a small event adapter, not duplicate the estimator or recommendation system.

For team content, show the player's supported offensive comparison even if team kill-time impact cannot be estimated. Never attribute all team damage or duration to the local player's loadout. For raids, show “at this boss/room/phase” versus “across this raid” explicitly. Whole-raid impact requires a shared baseline/route, mutually exclusive time segments, dependencies and overhead treatment. Unsupported rooms do not become zero and must not suppress valid supported room advice; withhold the total when missing contributions are material.

Track every encounter family in the uncapped CATALOGUE by observation, contextual offence, personal time and whole-activity support separately. “Most bosses” is a coverage objective to demonstrate against that inventory, not a claim achieved by shipping one adapter or a fixed sample count. KI1 includes cross-family contract fixtures, KI2 proves reuse beyond the first boss, and KI3 explicitly delivers raid-room/team increments while maintaining the broader inventory. Prioritise by player activity and reusable mechanics; do not postpone all raid-boss advice until every solo boss or a universal raid timer is complete.

### Value and alternatives

| Approach | Assessment |
|---|---|
| Manual target/loadout DPS comparison | Useful foundation, but asks the player to translate numbers to their activity and may use gear they did not wear |
| Passive kill context + existing upgrade cards | Best first delivery: automatic relevance, actual baseline, acquisition path and owned alternatives in one place |
| Seconds from the last kill multiplied by a DPS ratio | Cheap but misleading: RNG, mandatory phases, supplies and movement are incorrectly attributed to equipment |
| Universal combat replay/digital twin | Too much observability, maintenance and policy risk for the product value; reject |
| Separate plugin | Duplicates ownership, variants, comparison models, sources, privacy and UI; reject unless a genuinely separate product emerges |

Likely benefits are exposing worthwhile ammo/accessory changes, making small gains understandable, preserving boss-specific specialists and showing when the player's owned alternatives already suffice. These are hypotheses, not user-test findings. Risks are false precision, a stream of repetitive cards, bias toward whichever boss was last killed, and encouraging a very long Ironman grind for a small local benefit. Kill Impact must retain general importance, remaining effort and reasons to defer; seconds per kill are not a universal priority score or grind-payback requirement.

## Existing foundation and concrete gaps

Paths below refer to `src/main/java/com/ironpath/` at the inspected R3a head.

| Existing component | Reuse | Required addition or boundary |
|---|---|---|
| `AccountSnapshotReader`, `model/AccountSnapshot`, `model/Observation` | Equipment slots, quantities, real levels, ownership/unlock provenance | Reader does not capture active prayers, current boosted-level history, attack launches, phases or a kill timeline |
| `ObservationSession`, `IronpathPlugin` | Client-thread reads, immutable snapshots, generation/revision guards, supported-world and character/profile isolation | Current `StatChanged` handler only dirties on real-level changes; prayer/boost changes are not a combat trace. `GameTick` returns when not dirty. Add a separate bounded capture path, not full advisor evaluation each tick |
| `advisor/CombatSetup`, `MagicReference`, `RangedReference` | Typed slots/assumptions, compatible packages, focused corrections | References are proposals and may select strongest observed owned gear. They are not evidence of what was worn during a kill. `rangingPotion` is a Boolean and cannot encode brew drain/decay over time |
| `CombatComparisonService`, `RangedComparisonService`, `PoweredStaffModel`, `RangedModel` | Independently authored rolls, rounding, full supported package comparisons, per-metric gaps | Existing outputs are max hit, accuracy, expected damage/DPS; no reusable full damage PMF or encounter solver. Preserve ordinary regressions when adding distributions |
| `TargetProfile`, `KnowledgeCatalog`, bundled sources | Reviewed target stats and source dependencies | No HP/phase/cycle/encounter model in `TargetProfile`. Vorkath is not a runtime comparison target. Dragon flag/DHCB logic alone does not establish Vorkath coverage: Salve is absent, enchanted bolts are explicitly unsupported, melee is pending |
| `ComparisonResult` | Explicit theoretical metric distinction, source/model/input provenance | `EXPECTED_KILL_TIME` and `TRIP_PERFORMANCE` are enum placeholders marked unsupported, not delivered functionality. Add a separate typed impact result rather than hiding observations/ranges in `PoweredMagic.Metrics` |
| `Advisor.UpgradeView`, `ValueEvidence`, `RecommendationRanker`, `PursuitOverview` | Account importance, independent role value, adequate alternatives, status and routes | Attach recent-context evidence; never replace `progression-1` with time-saved sorting |
| `UpgradeCard`, `IronpathPanel`, `BrowseGrid` | Native item sprites, compact cards, Value/Path, guarded navigation | Small Recent boss entry and contextual metric; no per-kill modal, forced tab change or growing combat dashboard |
| `PaceScenario`, `AcquisitionEstimator`, preference IO | Explicit pace scope, local manual facts, serial storage protections | Combat duration is not cycle throughput. No automatic kill-time → kills/hour → acquisition-hours chain. No performance persistence in this increment |

One notable lifecycle constraint: current `onGameStateChanged` clears the whole session, including region loading. KI1 initially retains that conservative boundary. Same-instance repeated kills can aggregate; leaving/reloading starts a new window. **KI2 must add a separately tested, character-scoped in-memory summary lifetime across ordinary banking/region transitions**: a ten-kill threshold confined to one Vorkath trip would often prevent useful calibration. End/censor the active trace immediately, continue clearing existing bank/reference/live state, and suspend summary access until the same character/profile and supported world are revalidated. Only a specifically recognised ordinary transition may retain completed summaries; ambiguous identity, disconnect/logout, profile change or unsupported world clears them. Old-generation callbacks never publish into a new trace. This is not cross-login persistence or permission to weaken existing account isolation.

## What RuneLite can observe

This is source/API feasibility, **not a live-client verification**. Primary RuneLite sources K01–K10 and API references K18–K21 are in the evidence register. An event's existence does not guarantee complete attribution or exact tick ordering in every encounter.

| Signal | Available evidence | Reliability and intended use |
|---|---|---|
| Equipment/inventory | `Client.getItemContainer`, `ItemContainerChanged` | Direct slot/quantity observations when loaded. Capture changes during the fight; finish gear alone may be a spec/healing switch. Internal darts, second quiver ammo and charge activation need separately verified public state mappings; bank ammo is not proof of loaded ammo |
| Real/current levels | `getRealSkillLevel`, `getBoostedSkillLevel`, `StatChanged` | Direct visible state; retain current levels at relevant boundaries. Does not identify the potion that caused a change without corroboration |
| Prayer/style/spec state | `isPrayerActive`, public varp/varbit reads/events | Active state can be sampled. Validate specific current IDs and timing. Unlock is not active prayer; spec toggle or energy loss alone does not prove a successful special hit |
| Time | `GameTick`, `getTickCount`; boss duration game messages | Ticks are approximately 0.6 s. Preserve game-reported duration and separately defined observed duration. Chat Commands parses duration/PB messages, but its PB capture group is not the current kill time |
| Target/phase | NPC identity/transform/spawn/despawn, interaction, animation and public graphics/projectile events | Encounter-specific correlation. Use instance/world-view plus spawn sequence: NPC index alone can be reused. Despawn or interaction alone is not completed player credit |
| Damage | `HitsplatApplied`, `Hitsplat.isMine()`/`isOthers()` | The API does support local-versus-other attribution for designated hitsplat types; core DPS Counter uses it. No universal attacker/weapon/attack-ID field. Dot damage, thralls, recoil, multi-hits and delayed impacts need explicit treatment; do not silently assign every splat to the current weapon |
| NPC HP/defence | Actor health ratio/scale; reviewed NPC data | Generic health bar is quantised, not exact current HP. Static NPC max HP is not live HP or reduced Defence. Reductions/procs need a model and attributable evidence |
| Attack cadence | Reviewed player animations, equipped weapon, target, correlated impacts | Inferred launch/opportunity ledger, not an authoritative server attack log. An animation change is not always one attack; impacts lag launches. Preserve ambiguity and distinguish primary-target attacks from spawn handling |
| Consumption | Item/dose transitions, stats, animation/menu events | Inventory removal alone could be dropping, banking or another action; clicks can fail. Correlate state changes. Same-tick combo food needs a grouped event, not independent time penalties |
| Completion | Encounter-specific death/transform plus game completion/KC/loot evidence | Core Loot Tracker consumes `ServerNpcLoot`; validate event availability/ordering for the resolved client. No private plugin storage or mandatory Loot Tracker dependency. A message without observed start may establish a reported duration, not a complete trace |
| Player performance | Cadence/gap summaries within known windows | Descriptive evidence only. Cannot reliably classify every gap as error, movement, intentional eating, latency or strategy; no skill score |

Core Boss Timers is a **respawn** timer, not a turnkey encounter-duration service (K07). Existing DPS Counter demonstrates damage observation, not a trustworthy counterfactual upgrade engine (K09). Their existence is neither an integration dependency nor approval for new functionality.

## Observation and computation design

Keep one Gradle plugin and a small `combat observation → encounter summary → comparison → impact` pipeline. These are proposed contract names, not delivered classes:

| Contract | Essential fields and responsibility |
|---|---|
| `EncounterAdapter` | Versioned boss/mode IDs, optional parent raid/run and room/phase identity, scaling/party/role evidence, start/end correlation, supported methods, event mappings and per-metric capabilities |
| `CombatObservation` | Generation, encounter/spawn key, tick/sequence, observed slot state, visible levels, prayer/style/ammo evidence, attack/consumption/phase changes; immutable values, never queued mutable Actor references |
| `EncounterSummary` | Timer scope, completion status, capture coverage, setup segments, inferred attacks/gaps, observed phase durations, source/model versions and specific unknowns |
| `KillImpactResult` | Upgrade ID, actual used baseline, candidate package, optional best-owned comparison, supported metrics, estimated delta/range type, method assumptions, sample counts and refusal reasons |
| `ComparableKillWindow` | Bounded same-account/session/context summaries; method/setup fingerprint, recent trend and uncertainty; never learn across unsupported contexts automatically |

State machine: idle → candidate start → capturing → finalizing → complete. Branches are failed/abandoned, censored (start/end/data missing), and excluded context. Reconcile pending impacts and authoritative messages with a short adapter-defined finalization window; deduplicate completion signals. A late event cannot reopen or attach to the next kill. Timeouts invalidate coverage rather than creating a fast kill.

Sample only required public state on the client thread; do not rerun quest scripts/catalogue evaluation every tick. Retain a bounded change/event buffer, coalesce within the tick while preserving relevant ordering, and freeze the summary after completion. In overflow, mark the trace incomplete. Run comparisons outside the client thread and Swing EDT, with bounded candidate packages and cache keys including setup, target, model and knowledge revisions. Discard stale generation/encounter results; an ordinary advisor revision must not rewrite a historical baseline. Publish immutable results on the EDT after completion only.

Raw trace exists only for the active/finalizing encounter; aggregate summaries stay in memory. Proposed initial resource budget: 20,000 compact events/encounter, 50 recent summaries with at most 30 matching kills used per estimate, bounded total cohorts with oldest eviction. These are engineering safeguards to measure, not catalogue limits or quality guarantees. Clear on disable/logout/profile/unsupported-world boundaries; provide Clear recent kills. KI2's guarded ordinary-region retention applies only to completed summaries, never the raw trace or bank/reference state. No names, chat transcript, bank dumps, outgoing events, telemetry, export or background persistence. Existing explicit preferences remain the only saved data. Any later opt-in summary history needs an explicit privacy/retention/migration change.

### Candidate and baseline rules

1. The actual used setup is fixed evidence, segmented when it changes. Never call `propose()` and label its stronger owned gear “your last kill.” A focused correction remains an assumption and does not alter raw observations.
2. Reuse eligible reviewed upgrades and compatible packages from the advisor. Changing a weapon may require ammo, off-hand or armour changes; name the package and attribute its gain to the package, not every component independently.
3. Show **already-owned alternatives** as “Try your …”, apart from acquisition suggestions. A stronger owned alternative reduces the marginal reason to grind a missing item even if that item improves the worn setup.
4. An unsupported used setup can still establish boss relevance and qualitative roles. Compare against an assumed reference only under an explicit “Reference comparison” label; never attach personal seconds to it.
5. Preserve sustained, burst, defensive, convenience and resource value separately. A missing seconds model must not remove a valid Zaryte/Lightbearer/sustain role. Unknown benefit is not zero.
6. Each upgrade is compared independently against the same baseline. Gains are not additive. Recompute a deliberately selected package as a whole.

## Numerical contract

Three distinct outputs are required:

- **Observed:** last reported/observed kill duration, recent matching sample count, median and spread. Never relabel a PB as the baseline average.
- **Modelled offence:** max hit, accuracy and DPS for an explicit supported phase/setup, with full mechanics and effective-level state. An ordinary-phase DPS gain is not full Vorkath DPS.
- **Estimated impact:** expected difference over comparable future kills under a stated same-method assumption. This is a modelled counterfactual, not an observed saved duration or guaranteed next-kill improvement.

### Finite-target foundation

For a stationary single attack stream with remaining HP `h`, damage probability mass `p(d)` and no healing/phase change, an independently derived expectation is:

`N(0) = 0`

`N(h) = [1 + Σ(d>0) p(d) N(max(0, h-d))] / [1-p(0)]`

`N` is expected attacks, including misses and overkill. If `p(0)=1`, kill time is unavailable/infinite, not zero. If the first impact occurs at offset `t_first` and later impacts are spaced by `a` ticks, `E[T] = t_first + (N(H)-1) × a`, in ticks. Timing origin and projectile travel must be specified. Damage-dependent delays, HP-dependent procs and multi-hit timing require richer distributions/state; a mean/max-hit pair is insufficient.

K14 confirms the Wiki calculator has distribution/finite-HP machinery, but its pinned scalar `getTtk()` uses `getHtk() × expected attack speed`, while `getTtkDistribution()` has separate HP-dependent logic and timing conventions. **Do not validate an HP-dependent bolt scenario against the scalar method merely because it is named TTK.** Agree on origin, semantics, exclusions and omitted probability mass first. Independently author Java; the GPL reference remains development-only (K16).

### Personalization without inventing causality

Rejected shortcut: `lastKillTime × (1 - DPS_before / DPS_after)`. It accelerates immune phases and player pauses and treats lucky damage as a repeatable gear effect. Likewise, do not use observed `damage/time` as intrinsic weapon DPS or fit one “player efficiency” multiplier across bosses and weapons.

For a validated stationary, same-method regime, a useful bounded approximation is:

`E[T_j | θ] = b(θ) + (N_j(H)-1) × (a_j + g(θ)) + D(θ)`

Here `b` is first-impact overhead, `a` weapon interval, `g` a pooled inferred extra delay per attack opportunity, and `D` separately observed mandatory/non-scaling overhead. Estimate cadence from launch opportunities, **not total kill duration or lucky hit values**. Gaps included in `g` cannot also be included in `D`. Taking paired baseline/candidate expectations under the same `θ` yields a conditional delta. This factorisation is a design approximation to validate, not a model already shown to work on Vorkath.

Use it initially only for supported same-cadence damage/accuracy substitutions with adequately observed state. A changing attack speed, range, spec schedule, defence/sustain effect or method cannot automatically inherit the same gap profile. Real boosted levels must be used per supported segment, not regenerated from base level plus an assumed potion. Unknown material segments withhold seconds; there is no minimum-known-percentage shortcut that averages them away.

For Vorkath, a separate analytical encounter adapter must account for ordinary attacks, the reduced-damage acid window and the spawn immunity window (K11), including first special/order and method-dependent attacks during acid. Assess plausible phase counts and timings under **both** loadouts. Holding observed downtime fixed while the candidate crosses a phase boundary is invalid. Initially report seconds only where bounded phase/timing sensitivity is stable and validated; otherwise show offensive gain and “Time estimate unavailable: phase timing changes.” Never silently report combat-only time as full kill time. If this approach withholds most normal Vorkath kills, do not call it a successful time-saving feature: either fund the specific missing model or keep the product at post-kill offence comparison until that work is done.

This deliberately avoids a universal replay engine. A narrower single-phase solo context can validate the finite-target/cadence machinery, but does not establish Vorkath encounter accuracy. Full analytical phase treatment remains a concrete KI2 acceptance responsibility, with the exact scope assessed before release.

### Repeated kills and uncertainty

More matched kills improve the estimate of typical duration, gap patterns and variability. They do not reveal the effect of unowned gear, correct a wrong combat formula, prove player skill, or remove systematic phase/attribution bias. No ML or per-user AI is needed.

Group by boss/version/mode, actual method/primary weapon and speed, relevant loadout/ammo/charge state, level/boost/prayer regime and spec plan. Fingerprint model-relevant state/regimes rather than every exact tick of a potion trajectory; distinguish ordinary decay from a material method change, and validate any pooling tolerances instead of merging incompatible runs to reach a threshold. Separate learning sessions and materially changed methods; do not pool different gear and call it a larger sample. Track completed, failed/abandoned, censored and excluded counts separately. Slow valid kills remain in the distribution; robust summaries are not permission to delete inconvenient observations. Completion-conditioned impact is not success rate, survival benefit or full farming throughput.

Proposed product calibration: one kill can trigger context and a supported theoretical comparison; fewer than 10 comparable complete kills show provisional observations without a headline personal-seconds estimate. From 10 onward, consider a recent window of up to 30 matching kills only if coverage, spread and validation gates also pass. These are initial UI thresholds to test, **not statistical assurance**. Distinct windows and held-out sessions are needed during validation even though runtime history is initially session-only.

Report sample variability, model/input sensitivity and prediction variability separately. A range across plausible assumptions is a **sensitivity range**, not a 95% confidence interval. A confidence interval for an average is not a next-kill prediction interval. Avoid decimal seconds on cards; a tiny modelled effect may be below observable resolution/noise. Show “Small gain; no stable time estimate” when warranted. Never clamp negative deltas into a positive recommendation. If a defensible sensitivity range spans no gain, do not headline “faster.”

## What to include, and when

| Factor | Initial decision | Later requirement |
|---|---|---|
| Accuracy, max hit, ordinary DPS | Include for supported full setups and exact context | Extend shared families, not bespoke per-item percentages |
| Actual levels/prayers/potions | Capture state and compare supported segments | Decay, divine maintenance, drain/restoration and different supply plans require stateful inputs |
| Ammo and quiver | First-class candidates/evidence, including internal darts and charged/blessed forms | Distinguish permanent quiver/blessing from recurring splinters/ammo. Correct active ammo source and charge effects; unknown activation blocks affected numbers |
| Ruby/diamond and other enchanted bolts | Explicit existing numerical gap; prioritise common Vorkath procs in R4/KI2 | HP/cap/immunity/diary/weapon interactions and switching policy; never average ruby at starting HP for an entire kill |
| Specials, reductions, Lightbearer | Capture attributable state; retain reviewed qualitative roles | Starting energy, regen, weapon/attack success, spec distribution, reduction cap/reset and competition. A short kill may gain no extra spec from Lightbearer; no fixed percent uplift |
| Hard food and combo food | Observe consumption when corroborated; no automatic seconds penalty | Most hard food has a 3-tick delay; some food differs (K12). Same-tick healing is not necessarily delay-free. Deduplicate overlap with movement/immune windows; model changed healing policy before comparing foods |
| Brews/restores | Capture visible-level trajectory; no generic “brews improve DPS” | Healing density, drains, restored versus reboosted stats, dose sequence and encounter suitability. No recommendation to brew at every boss; actual level history is more useful initially than a hypothetical consumption optimiser (K10/K13) |
| Defence, healing procs, supplies saved, kills/trip | Qualitative contextual value initially | Incoming damage, protection, overheal, survival/resupply and changed behaviour. Fewer eats cannot be inferred from outgoing DPS alone |
| Movement, mechanics, player execution | Bounded method/gap/phase summaries | No “mistakes” or universal efficiency score. Same player may use a different method with new gear |
| Team bosses, raids, multi-target fights | First-class coverage target; deliver supported room/target offence before personal/team seconds where necessary | Contributions, scaling, shared reductions, role/geometry and phase state; do not attribute team time to the local loadout |

Food/supply modelling is KI3, not a precondition for the first useful card. Conversely, a baseline with material unsupported brew/spec/proc effects cannot be falsely treated as a clean KI2 baseline. KI1 must expose these gaps concisely and retain qualitative advice.

## UX and recommendation integration

Keep the ordinary automatic homepage primary. Add a compact **Recent boss · [boss name]** entry (Vorkath is an illustrative label) after a supported kill, opening the same cards filtered/contextualised to that activity. Refresh quietly after finalization; no chat spam, modal, combat overlay, forced sidebar opening or live numerical advice. A user inspecting a card keeps focus/scroll and the chosen historical baseline. Unsupported encounters do not generate repetitive failure cards.

Use real item sprites, one account-value reason, **one** primary supported metric and Value/Path. The selected recent context can use impact as that metric when supported; the ordinary neutral comparison remains available in Value and is not silently replaced globally. Owned alternatives get “Try owned setup”; supplies get a recurring-use label. Importance/effort/status still govern acquisition advice. Recent activity is session relevance, not an implicit permanent goal or a numeric multiplier on global rank.

Illustrative copy only, with synthetic values and no claim about a real item/account:

| State | Compact presentation |
|---|---|
| First supported kill | `Vorkath · last 1:42` followed by an upgrade card: `+4% ordinary-phase DPS · observed setup` |
| Validated KI2 cohort | `Est. 3–5 s faster/kill · same method` with `12 comparable kills` nearby; Value names whether the range is sensitivity or prediction |
| Common missing model | `Ruby-bolt time effect not modelled` beside a supported qualitative upgrade reason; no fabricated seconds |
| Adequate owned option | `Try your owned setup` with its separately supported comparison and no new-grind implication |
| Negligible/uncertain effect | `Small gain; no stable time estimate` or an honest no-material-upgrade state |

Value separates observed duration, used loadout, candidate package, best-owned alternative, supported metrics and assumption details. Keep source/model versions, sample scope, failed/censored counts and observation quality under disclosure; consequential uncertainty stays beside the metric. No full tick log, performance score or acquisition-hours conversion. At 225px native width use the existing one-column cards and density acceptance; do not add another large explanatory header.

## Validation and release decisions

No application tests or actual kills were executed by this research. Existing R3a test counts remain historical evidence for its own features.

| Validation layer | Evidence required before promotion |
|---|---|
| Mathematical | Original hand cases for misses/zeroes, overkill, finite HP, first-impact timing, speed changes and immune windows; identical setup yields zero delta; damage monotonicity only under genuinely equivalent mechanics |
| Differential | Pinned reference, original complete input cases and explicit output semantics. Preserve magic/ranged regressions; do not copy GPL source/tests/data. Investigate disagreements instead of moving tolerances |
| Event replay | Synthetic/adapted fixtures for exact start/end, late/missing/duplicate messages, NPC transforms/index reuse, multi-hit/projectile delay, same-tick switch/food/brew, wrong target, death/teleport, mid-kill enable, overflow, disconnect/loading/hop and profile switch |
| Actual client | Recorded RuneLite version and public-event mappings; manually annotated timer/phase/launch checks. Proposed gate: all controlled completion/identity edge cases correct; at least 95% of eligible normal kills captured; investigate timer discrepancies beyond one tick under identical origin |
| Predictive validity | Freeze the model before held-out assessment. Compare baseline predictions with later same-method kills; separately alternate actually owned baseline/candidate setups in matched blocks to test upgrade deltas and learning/order confounds |
| Product usefulness | Player identifies an upgrade, its conditional benefit and next path without diagnostics. Review important omitted options, recurring ammo burden and stronger owned alternatives. Seconds must not persuade users an expensive marginal grind is mandatory |
| Runtime/UI | Capture overhead and maximum buffer/candidate workload measured; no noticeable client stalls. Native width/scaling, sprite arrival, Back/focus and stale-result isolation; no live coaching |

For KI2, use at least 30 complete observations per tested setup across multiple held-out blocks and more than one method/player where available; continue sampling when uncertainty is too broad for the claimed effect. This is an initial validation budget, not a power calculation. Predeclare a candidate practical acceptance target: held-out baseline median absolute error no greater than `max(3 seconds, 5% of typical duration)` in the advertised context, no unexplained directional bias across supported groups, and correctly typed uncertainty ranges. Report all counts, errors and exclusions. **Baseline accuracy alone does not validate upgrade savings**: paired before/after mean-delta uncertainty must also be consistent with the claim; a 3-second effect cannot be sold as measured merely because a 5-second baseline-error gate passed. Claims below validation resolution stay theoretical/provisional.

Roll out KI1 as an optional experimental recent-kill context, then KI2 only per validated boss/method/candidate family. Publish eligible-capture and numeric-coverage fractions, not just successful examples. A pilot where most ordinary Vorkath loadouts are unsupported needs a concrete model-priority correction. Record user acceptance before default-enabling the surface; no telemetry is required. Expand one encounter adapter at a time toward most relevant PvM bosses, including explicit raid-room/boss increments, based on reusable mechanics and player value. Prove reuse beyond the pilot; do not let its item, NPC or phase assumptions become shared schema constraints. Source/client changes invalidate affected capability versions and cohorts, preserving unaffected advisor features.

## Policy and maintenance scope

The selected experience is retrospective equipment advice, not live mechanics assistance. Current RuneLite guidance still lists content simulations and new high-end boss plugins among rejected/not-considered features (K17), so being post-kill does not itself establish an exemption. This is a scoped release assessment, **not a reason to block research, ordinary DPS comparisons or all development**. Recheck the exact KI1 capture/presentation and KI2 analytical scope against current guidance; obtain clarification only where a concrete boundary remains unresolved before public distribution. No external contact or Plugin Hub submission is made here.

No playable rehearsal, player-input automation, recommended next combat action, prayer/movement overlay, remote account analysis or other-plugin private state. Keep supported Java APIs, existing Filepath rules and source notices. New models carry dependencies on exact mechanics/target/adapter/client versions. On a known invalid source, withdraw only affected numbers and retain valid recommendations. Review calculator code, datasets, written guides and sprites separately: availability is not permission to copy. No external source code, text corpus or equipment database is imported by this documentation change.

## Evidence register

Retrieved/reviewed 14 October 2026. [Machine-readable metadata](research/kill-impact-sources.json) records response hashes, source revisions, scope and retrieval limitations. Full-page retrieval through ordinary HTTPS succeeded for the Wiki pages after the web reader failed; these pages document community-researched mechanics, not live tests or Jagex guarantees. Report deductions and proposed thresholds above are our design judgements.

| ID | Source and reviewed claim |
|---|---|
| K01 | [RuneLite Hitsplat](https://github.com/runelite/runelite/blob/5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03/runelite-api/src/main/java/net/runelite/api/Hitsplat.java): amount/type and local/other classification; not weapon attribution |
| K02 | [HitsplatApplied](https://github.com/runelite/runelite/blob/5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03/runelite-api/src/main/java/net/runelite/api/events/HitsplatApplied.java): actor/hitsplat event, including non-rendered splats |
| K03 | [GameTick](https://github.com/runelite/runelite/blob/5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03/runelite-api/src/main/java/net/runelite/api/events/GameTick.java): post-packet tick and approximate duration |
| K04 | [StatChanged](https://github.com/runelite/runelite/blob/5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03/runelite-api/src/main/java/net/runelite/api/events/StatChanged.java): real/boosted level event |
| K05 | [Client API source](https://github.com/runelite/runelite/blob/5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03/runelite-api/src/main/java/net/runelite/api/Client.java): current levels/prayers/varps/varbits/tick getters |
| K06 | [Chat Commands](https://github.com/runelite/runelite/blob/5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03/runelite-client/src/main/java/net/runelite/client/plugins/chatcommands/ChatCommandsPlugin.java): duration/PB message patterns; avoid PB-as-kill-time reuse |
| K07 | [Boss Timers](https://github.com/runelite/runelite/blob/5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03/runelite-client/src/main/java/net/runelite/client/plugins/bosstimer/BossTimersPlugin.java): respawn timing from deaths/transforms |
| K08 | [Loot Tracker](https://github.com/runelite/runelite/blob/5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03/runelite-client/src/main/java/net/runelite/client/plugins/loottracker/LootTrackerPlugin.java): current `ServerNpcLoot` consumer; per-boss timing/credit still needs validation |
| K09 | [DPS Counter](https://github.com/runelite/runelite/blob/5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03/runelite-client/src/main/java/net/runelite/client/plugins/dpscounter/DpsCounterPlugin.java): `isMine()` damage tracking; no counterfactual inference |
| K10 | [SaradominBrew stat effect](https://github.com/runelite/runelite/blob/5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03/runelite-client/src/main/java/net/runelite/client/plugins/itemstats/potions/SaradominBrew.java): healing/Defence and offensive-drain dimensions |
| K11 | [Vorkath, revision 15364808](https://oldschool.runescape.wiki/w/Vorkath?oldid=15364808): undead/dragon/Salve exclusion, alternating specials, acid reduction and spawn immunity; not a complete calibrated encounter model |
| K12 | [Food, revision 15371148](https://oldschool.runescape.wiki/w/Food?oldid=15371148): ordinary delay, fast-food exceptions, combo healing; exact per-food rules need review for KI3 |
| K13 | [Saradomin brew, revision 15322175](https://oldschool.runescape.wiki/w/Saradomin_brew?oldid=15322175): offensive-stat drain and restore/inventory trade-offs; no universal brew-DPS gain |
| K14 | [Wiki calculator PlayerVsNPCCalc](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/PlayerVsNPCCalc.ts): scalar versus distribution TTK semantics and HP-dependent paths |
| K15 | [Wiki calculator HitDist](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/HitDist.ts): explicit damage distributions; development reference, no reuse/import |
| K16 | [Calculator GPL licence](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/LICENSE) and [Hub instructions](https://github.com/runelite/plugin-hub/blob/61225fc804d3eae96b5648970eb5777ec1e88428/README.md): keep independent implementation and review distribution requirements |
| K17 | [Rejected or Rolled Back Features](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features), page showed 13 October 2026 edit: current simulation/boss-plugin and API restrictions; no feature-specific ruling obtained |
| K18 | [Actor API](https://static.runelite.net/runelite-api/apidocs/net/runelite/api/Actor.html): health ratio/scale, interaction, animation; not an exact server combat log |
| K19 | [AnimationChanged](https://static.runelite.net/runelite-api/apidocs/net/runelite/api/events/AnimationChanged.html): animation event; attack interpretation remains adapter logic |
| K20 | [ItemContainerChanged](https://static.runelite.net/runelite-api/apidocs/net/runelite/api/events/ItemContainerChanged.html): changed containers include ordinary inventory actions, not only consumption |
| K21 | [StatChanged API](https://static.runelite.net/runelite-api/apidocs/net/runelite/api/events/StatChanged.html): current public API documentation, separately checked against source |

Unresolved implementation evidence: exact Vorkath start/end/phase IDs and timer origins in the installed client, active quiver/internal-ammo state mappings, event ordering/attack attribution, acceptable numeric-coverage rate, calibrated personal ranges and user benefit. These are visible KI1/KI2 acceptance items, not claims established by the research.

## Documentation verification

This delivery changes nine documentation/evidence-metadata files only. Checked 113 relative file links across those documents, parsed the source register and verified all 17 recorded response hashes against the retrieved bytes. The remaining five dynamic web-reader entries deliberately have no invented hash/revision. Historical TASKS content from the existing client comparison-gap record onward is preserved byte-for-byte. Whitespace checks pass. Application code, runtime catalogue, build files and tests are unchanged; no application suite or in-game validation was rerun for this documentation-only change.
