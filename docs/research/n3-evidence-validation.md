# N3 evidence validation — 23 October 2026

Build **0.30.0**, from main `1fe77cbcc8a939711438cc436f94cce3f31b9871`.
PRs [#50](https://github.com/PetterHL/ironpath/pull/50),
[#51](https://github.com/PetterHL/ironpath/pull/51) and
[#53](https://github.com/PetterHL/ironpath/pull/53) are merged; this is a new,
focused step from their integrated main, not a continuation of a stacked branch.
[TASKS](../../TASKS.md) remains the active roadmap.

**Automatic N3 recommendations remain unavailable.** This review closes one
conditional energy-model discrepancy and adds a diagnostic to distinguish first
and repeat projectile getter availability. It establishes no new legal launch,
exact impact HP, cast, eligible death, proc or consumed resource.

## Evidence reviewed and its limits

The existing [0.29.0 validation record](../validation/nex-damage-observation.md#live-one-copy-check--23-october-2026)
is the live evidence input. Its recorded report digest is
`ce2920c5bec6d646cd3455f40bd45ae4f71d92b2cbbc21515a2398305b957776`.
This step reviews the committed findings; it does **not** claim another
raw-report parse, new gameplay or access to the original private capture.

That record supports single-kill hitsplat application accounting, including
misses: 171 own impacts, 52 zero hits, and per-target damage minus healing
equal to the reviewed initial HP. It records 364 UNKNOWN retained projectile
sources, no accepted integer HUD binding, and these relative resource readings:

- Full energy at tick 129; 1,000 → 500 at 137; 600 at 162.
- A below-full regeneration reading at 212, spending at 224 and next regeneration
  at 237.
- Cruor's death at 290 coincides with +150 energy and active 2 → 1. Umbra's
  death at 178 has no matching restoration/active transition.
- Six Voidwaker animation 11275 candidates coincide with energy drops.

The arithmetic and sequences retain their original single-kill scope. They do
not label six executed specials, prove every hit's causal owner, reconstruct
per-impact HP, establish Umbra's eligibility or identify the cause of Cruor's
energy increase. End-of-kill HP accounting is not an ordered HP replay.

## Pinned public source review

All RuneLite links use **1.13.2**, commit
`84233cdb5ebc324d1d079de9da0ce61a772558fc`, retrieved 23 October.
These are independently summarized contracts and factual behavior; no external
implementation or dataset is copied. Existing notices remain applicable.

| Source | Supported conclusion / boundary |
|---|---|
| [Projectile](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-api/src/main/java/net/runelite/api/Projectile.java) and [ProjectileMoved](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-api/src/main/java/net/runelite/api/events/ProjectileMoved.java) | Both actor getters are nullable; movement callbacks repeat and some ground-target flights report once. Neither guarantees later source resolution. The prior [resolved-binary inspection](nex-damage-observation.md#bounded-counters-and-projectile-uncertainty) establishes signed actor-table lookup, not a source index for these live flights. Origin point, projectile ID, equipment, interaction and co-location cannot establish ownership. |
| [AnimationChanged](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-api/src/main/java/net/runelite/api/events/AnimationChanged.java) and [Actor](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-api/src/main/java/net/runelite/api/Actor.java) | Animation changes and interaction are public state, not a server acknowledgement of a legal attack or an attack-ready clock. Interactions also include following/trading. The inspected [Client API](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-api/src/main/java/net/runelite/api/Client.java) supplies no authoritative general next-attack-ready getter. |
| [Actor health contract](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-api/src/main/java/net/runelite/api/Actor.java) and [OpponentInfoOverlay](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-client/src/main/java/net/runelite/client/plugins/opponentinfo/OpponentInfoOverlay.java) | Actor health is quantized and may be missing. Opponent Info inverts that bar and displays an estimate; its display midpoint does not independently establish exact HP. IronPath already preserves the interval using independently reviewed maxima (Nex 3,400; named minions 500). Singleton/full/zero intervals remain conditional bar evidence at that reading's time, not validated pre-impact HP. |
| [OpponentInfoPlugin](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-client/src/main/java/net/runelite/client/plugins/opponentinfo/OpponentInfoPlugin.java) and [HUD variables](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-api/src/main/java/net/runelite/api/gameval/VarbitID.java) | Generic integer HP/base-HP HUD channels exist. Their existence does not establish a Nex/minion channel, spawn identity, freshness or order relative to a hit. Existing fresh-script, visible-widget, unique-target and server-agreement guards remain. |
| [VarbitChanged](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-api/src/main/java/net/runelite/api/events/VarbitChanged.java) and [SpecialCounter](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-client/src/main/java/net/runelite/client/plugins/specialcounter/SpecialCounterPlugin.java) | Client prediction may produce the only change callback even if the server agrees later. Preserve original payload and later server/tick state separately; a net energy decrease neither identifies the weapon nor isolates gross spending/restoration. |
| [Regeneration Meter](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-client/src/main/java/net/runelite/client/plugins/regenmeter/RegenMeterPlugin.java) | Fixed standard/Lightbearer intervals are 50/25 ticks. At energy 1,000 its clock resets rather than advancing. This corroborates the live full-cap contrast and supports the bounded correction below; a client meter is not a server event/order API. Ring-switch behavior remains outside this model. |
| [Timers and Buffs](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-client/src/main/java/net/runelite/client/plugins/timersandbuffs/TimersAndBuffsPlugin.java) and [tests](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-client/src/test/java/net/runelite/client/plugins/timersandbuffs/TimersAndBuffsPluginTest.java) | Active 2 starts an improved timer; 0 → 1 starts a normal timer; 2 → 1 keeps the existing timer; 0 removes it. Cooldown has its own variable. These are timer mappings, not a general cast/eligible-death/proc/expiry classifier. |
| [ItemContainerChanged](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-api/src/main/java/net/runelite/api/events/ItemContainerChanged.java) | Container changes cover many causes, including adding/removing items. A quantity loss or menu action alone does not establish consumption, launch, rune spending or usability. |

Useful source blob IDs: RegenMeterPlugin `88ab115c6ea8914eefdf3e9c8973f1cdfd6bf066`;
TimersAndBuffsPlugin `fcf0d85a2293e8b1c5623d58649fca4984e20afd`;
VarbitChanged `2b84f92e499d04b9a27a6243e50bc7b33a039bdd`;
OpponentInfoOverlay `7c0f5d8b033950724876ef7a69f49a4ac2585b42`.
These are Git blob identifiers, not capture hashes.

## Supported implementation changes

### Conditional natural-regeneration clock

`SpecTimeline` advances to **nex-spec-resources-3**. Its earlier absolute
periodic schedule continued through full energy and invented natural-restoration
waste while already full. The fixed-ring declared model now stops its natural
clock at the cap. An admitted special leaving full starts a new complete 25/50
tick interval. Spending while below full and a proc that remains below full
preserve the current phase. A natural restore or explicitly declared proc that
reaches full stops the clock. Partial excess from an actual modeled gain remains
waste; eligible DC charges still consume at full.

This supports the report's 137 → 162 contrast without labeling the spending or
restoration callbacks as executed attacks or natural events. Standard-ring
50-tick behavior is source-backed, not newly live-validated. The model still
requires a separately supplied initial phase below full and before/after order.
Transient cap/proc/spend ordering within one tick remains an explicit scenario
policy; the report has not validated it. No ring-switch, concurrent-source or
automatic readiness classifier is introduced. Natural-only examples use this same
kernel; their full-start phase ranges can correctly collapse.

### Repeat projectile getter diagnostics

Three fixed counters split retained-object repeat callbacks into
`PROJECTILE_REPEAT_LOCAL_SOURCE`, `PROJECTILE_REPEAT_OTHER_SOURCE` and
`PROJECTILE_REPEAT_UNKNOWN_SOURCE`. They record only what the getter returns
at that callback, under its current target-class scope. A repeat can have a
different target scope. Counts are callbacks, not distinct projectiles, and
do not identify an individual retained flight or prove its original owner.
Objects excluded before retention remain outside this repeat cohort.

The first flight's source/target/time and candidate matching remain unchanged.
In particular a later LOCAL reading never upgrades an UNKNOWN flight or enables
a ranged match. No actor, coordinate or extra event is exported. The existing
fixed counter bounds, saturation flags, smoke/reset/lifecycle clearing and
immutable summaries apply. Observation vocabulary is **nex-observations-5**;
export **nex-evidence-3** makes the added counter vocabulary explicit and retains
the whole-capture, page-independent allowlist.

## Remaining gates and smallest additional observations

Each observation below is a validation unit, not a request for another complete
Nex kill. General weapon/resource checks can use a controlled ordinary target.
Nex-specific target legality/HUD timing still needs Nex-specific evidence when
gameplay is convenient. Negative controls matter: an attempted action and a
successful one must be distinguishable.

| N3 requirement | Exactly unknown / why current evidence is insufficient | Smallest additional live observation |
|---|---|---|
| Legal/local launches and special identity | Which candidates execute ordinary/special attacks, which target is actually attacked, where the callback gear applies, and which legal opportunities were missed. Own zero/positive impacts establish processed payloads, not a launch; animation/energy/selected-spec/clicks and delayed correlations can overlap. Target vulnerability, range/line of sight, movement and cooldown are not established by interaction. An alternative weapon's schedule cannot be copied from observed attacks. | One isolated known ordinary attack, one special and one miss for each admitted weapon/animation, with independently noted action/outcome, callback and settled gear, target/form and impact timing. Include a rejected/too-early attempt or movement/target-unavailable control. A short Nex window must separately corroborate Nex-specific access; this does not enumerate every missed opportunity. |
| Projectile ownership | Whether source is absent throughout each flight or only on first callback; whether later actor lookup belongs to the original source. The 364 first readings cannot answer this, and coordinates/IDs are ambiguous with other actors. | For one independently identified local shot and one other-player shot, observe first and later getter categories in a controlled non-overlapping sequence. New repeat counters can establish whether later getters ever resolve; an individual time/scoped flight observation is still needed before changing first-source policy. If all remain null, ownership stays unavailable through this API. |
| Usable target HP | Exact HP for the relevant launch/impact and package comparison, including intervening damage/healing and callback order. Bar intervals are useful bounded observations but N3's current scalar inputs cannot treat them as exact. Total damage minus healing gives no ordered pre-impact history. No HUD candidate was accepted: 136 no-fresh-variable and 134 hidden-widget first failures do not prove HUD absence, disabled settings or a bad mapping. | During a short known-target segment with the game's HUD visibly available, pair fresh HUD script/variable/server readings with the displayed integer, tracked target/form and one isolated hit; then switch target/form to check stale-value rejection. If Nex exposes no integer channel, no amount of another identical full kill supplies exact HP: retain bounds/unknowns until an independent exact channel and timing are established. |
| Initial and per-opportunity readiness | Residual attack/eat/drink/spell cooldown at smoke or a rebased draft start; whether each opportunity is legal and the starting DC lifetime/cast cooldown. The first smoke snapshot is a reading at that callback, not the preceding action history. A ready weapon animation or sampled energy does not supply those clocks. | A short sequence beginning after an independently timed idle interval, then one attack, one food/drink and one cast with next accepted/rejected attempts at the relevant boundary; retain pre-start state and callback order. This calibrates the specific mapping only. Automatic capture still needs adequate prehistory or an authoritative readiness signal for each attempt. |
| Special energy phase/order | Energy quantity can be directly read when client/server agree, but initial below-full restore phase, same-tick source ordering and gross simultaneous spends/gains remain unknown. The full-cap contrast supports the new fixed-ring rule only. | No new kill for the fixed-Lightbearer separated-tick correction. For unresolved order, one isolated below-full regeneration cycle plus a deliberately coincident spend or DC gain, with original payload and later server readings. Test a standard-ring full interval separately if live acceptance of that branch is needed; ring switching remains excluded. |
| DC cast / active / expiry | Which visual/variable transitions mean an accepted cast or remaining lifetime; whether 1 → 0 is final proc, expiry or another loss. An effect or timer update is not an execution acknowledgement, and existing report findings do not supply every preceding cast. Initial 1/2 does not date a pre-start cast. | One independently confirmed successful cast, its cooldown/active/effect transition and no-kill expiry after 100 ticks; a failed/cooldown cast control. Keep original callbacks and server agreement through expiry. This can be done outside Nex. |
| DC eligibility / proc | Which Nex/minion deaths qualify for this player; whether a charge decrement and +150 have the same cause. Cruor's 2 → 1 / +150 is compatible with a proc but not causal proof; Umbra's absent transition cannot distinguish no active spell, expiry, missing eligibility or ordering. Observed own damage alone does not establish the spell's exact eligibility rule. | With a confirmed active upgraded cast and energy below cap, one independently qualifying death away from a natural-restore boundary; observe 2 → 1, +150 and effects, then a second distinct qualifying death (1 → 0). A no-eligibility or expiry control is needed to distinguish causes. General checks validate the spell mapping; a short Nex/minion death segment is still needed for target-specific eligibility. |
| Usable stock and packing | Exact usable ammo/quiver/splinters/charges, normal loose/pouch Death/Blood/Soul runes and jointly carried supply/space at an admitted action. Initial known food units/potion doses and visible levels are already frozen; ownership, counter presence or a bank sighting do not prove usable packed stock or the selected ammo source. | One stable carried-state check of each relevant source, then one isolated admitted shot/cast with before/after stock and selected source confirmed. Verify a quiver transition and charged/exhausted boundary separately. Test unread/absent sources as unknown; no bank dump is needed. |
| Consumption and level/delay effects | Actual shark/karambwan use, brew/restore/super-combat/ranging doses, ammo saving/retrieval, splinter and rune use, and their timing relative to attacks. Container loss, level changes or net energy cannot distinguish eating from dropping/rearranging, other spells, drains, pickups or concurrent effects. The existing no-saving/no-retrieval branch is a declaration, not measured consumption. | One isolated successful use of each modeled family with before/after exact units/doses, visible levels and next-attack timing; one drop/rearrangement or failed-use control. For ammo/runes, pair a confirmed launch/cast with before/after source readings and exclude retrieval/other spells. A short combo-food sequence is needed for the shared-delay branch. |

All eight fixed `NexPlanSupport.automaticGaps()` entries remain. Even with a
confirmed energy clock, other launch/HP/readiness/DC/stock/consumption inputs are
missing. Dynamic Zaros/impact overhead and ordinary-melee reflection gates remain
as previously documented; this task neither expands those mechanics nor enters
N4, contribution/time estimates or UI redesign.

[Validation and review status](../validation/n3-evidence-validation.md).
