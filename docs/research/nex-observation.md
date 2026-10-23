# Nex observation slice

23 October follow-up: build **0.29.0** retains this observation delivery and adds
[callback/source corrections](../research/nex-damage-observation.md) and
[new validation](../validation/nex-damage-observation.md). Whole-kill damage
contribution is Unverified; export advances to `nex-evidence-2`. The results below
retain their original revision scope.


22 October 2026. Build **0.28.3**, adapter `nex-public-8`, target readings
`nex-target-state-2`, evidence `nex-observations-3`, knowledge `n3-observation-1`.
This is a separate follow-up to open PR #50 (`f02028d`, 0.27.0). Main was checked
at `7f68d0e`; neither branch has been merged as part of this delivery.

## Evidence and support

The client adapter now freezes pilot animation callbacks, first projectile
callbacks, own hits including zero, selected resource callbacks, tick reconciliation
and Death Charge effects. NexCapture owns the bounded immutable delivery to the
existing session and worker. No client actor, projectile or hitsplat object leaves
the adapter. Identity sets clear at the existing lifecycle boundaries.

| Observation | Automatically available | Remaining interpretation |
|---|---|---|
| HP | Source, bounds/value, maximum, tick/cycle, tracked spawn key, definition and world view | HUD Nex mapping and relation to a particular impact remain unvalidated |
| Launch candidate | Animation, callback equipment/levels/prayers/style, phase/window and target | Mapping and same-update gear order are not proven launches or legal opportunities |
| Later state | Next tick snapshot within one tick, with SAME_INPUTS / INPUTS_CHANGED / TARGET_CHANGED / NO_SETTLED_READING | Agreement does not establish server action order |
| Projectile | One record per object, source-local flag, nullable target, ID and start/end cycles | First nullable readings stay unresolved; no shot count from repeated movement callbacks |
| Own impact | Target, time, amount including zero, and impact form/HP reading | Other owned damage, overlapping launches and misses remain candidate matches |
| Start resources | First measured-smoke callback's immutable state; server-agreed energy; exact food units/potion doses | Callback boundary is not earlier attack readiness, stock usability or active spell lifetime |
| Death Charge | Client/server active, cooldown and upgrade flag; normal/upgraded cast/end effects; tracked target deaths | No inferred cast, eligible proc, expiry or causal energy restoration |
| Supplies | Exact initial quantities and directly sampled visible levels | No consumption classifier; item clicks/losses never create uses |

Ordinary post-kill complete-package comparisons continue to use directly observed
visible levels regardless of boost/drain cause. Each selected result now links to
its matching launch-state candidates and has its own support label. A Zaros gap
does not disable an independent supported Smoke/Shadow/Blood/Ice state comparison.
No aggregate attack-weighted damage, spec count or recommendation is manufactured.

The existing **Plan specs & gear** editor can copy matching, classified candidate
ticks to an explicit conditional draft, rebased to its first callback. It requires
the same frozen full state, target, phase, window and Zaros overhead. Duplicate
ticks, over 128 entries or truncated capture withhold this shortcut. The action clears scenario
confirmations, including supply effects and every saved impact confirmation.
Removed attack rows are discarded; matching draft values remain unconfirmed.
HP, caps, readiness, restore phase/order, casts, deaths, stock
usability and mechanics remain explicit inputs. The normal NexPlanComparison /
NexPackageComparison path evaluates it. These ticks do not establish missed
opportunities or the schedule of an alternative weapon.

## HP channel and validity

The generic Actor bar is quantized. RuneLite also has a separate integer boss HUD
channel; a generic Actor API comment cannot rule out exact boss data.

`HP_HUD_UPDATE` is the only HUD capture trigger. A HUD HP/base/NPC/INFO variable
callback must also have occurred in the same tick after the capture boundary;
rerendering old cached values alone cannot establish freshness. The adapter requires a visible
HUD text widget, enabled boss HUD, a current tracked interaction in the same world
view, exactly one tracked actor of that definition, and agreeing client/server
HP/base HP/NPC values. The definition ID alone never identifies a spawn. A HUD
reading expires on the next tick or an observed interaction loss/change, form
change, despawn, scene transition, new attempt, smoke reanchor or hard clear. Values
changing since the HUD callback also revoke their use. No old value is rebound to
a minion or replacement actor.

Accepted channel readings are **BOSS_HUD / AWAITING_CLIENT_VALIDATION**, with equal
integer lower/upper bounds. **This build emits no validated Nex HUD HP.** The
strict binding checks are implemented and tested synthetically; live mapping,
freshness and within-tick impact meaning still require the client checks below.
No candidate HUD value populates a numerical N3 impact input automatically.

The generic RuneLite HUD handler does not establish that OSRS Nex supplies that
channel. No Nex-specific source guarantee was established in the 20 October source
recheck; RS3/private-server bars and other plugins are not evidence for it. A normal
bar above the actor is the separate quantized Actor channel. Missing HUD candidates
do not by themselves identify a bug or a disabled setting.

Fixed HUD counters record variable updates, script callbacks, accepted bindings
and the first failed guard (freshness, disabled/missing/hidden widget, interaction,
tracking/definition, values, server agreement or ambiguity). Each counter saturates
at 65,535 with an explicit at-limit flag; no actor or raw HUD payload is retained
by these counts. They neither spend state/observation/event slots nor change the
binding rules. They clear with observation evidence at smoke reanchor, attempt and
existing lifecycle boundaries. An accepted callback may lack a retained HP row
after diagnostic truncation; counts do not promote validity or prove server timing.


Otherwise ACTOR_BAR / BOUNDED uses reviewed normal-world maxima from the existing
catalogue: Nex 3400, each named minion 500. For scale s > 1 and positive ratio r,
the lower bound is 1 for r=1, otherwise ceil(M(r−1)/(s−1)); the upper bound is
min(M, ceil(Mr/(s−1))−1). A full bar includes M, ratio zero means zero, and scale
one bounds positive health to 1..M. Invalid readings/maxima yield UNKNOWN.
These are bounds conditional on the reviewed maximum and bar semantics, not
exact impact HP. No midpoint is substituted. Unchanged readings can be deduplicated;
the retained reading keeps its original time, and every candidate launch/impact
gets a fresh immutable reading. HP-only changes use the separate observation
buffer, including source switches and loss of readability. They never consume
worn-state slots; target/form/phase/player-state changes still do.

## Mapping and event order

Bounded pilot mappings use exact Fang 26219/27246, Twisted bow 20997,
Voidwaker 27690 and Zaryte crossbow 26374 IDs. Animation candidates are Fang 9471,
bow 426, Voidwaker ordinary 386/390 and special 11240/11275, crossbow 7552/9168.
Other animations while holding these weapons remain UNCLASSIFIED. In particular,
the crossbow animation does not distinguish an ordinary attack from a special;
selected spec and energy changes are separate evidence, never proof of execution.
No Fang special damage model is added. All weapon combinations await client checks.

Diagnostic correlations require the same spawn key and earlier callback order.
Ranged candidates require a local-source projectile whose start/end are within
30 client cycles of launch/impact; melee uses a maximum two-tick diagnostic window.
These are bounded search policies, not calibrated server hit delays. Zero hits,
unmatched hits, multiple candidates and even single candidate matches retain their
uncertainty. Impact-time equipment is never read as launch equipment. Next-tick
reconciliation preserves the callback state even when later equipment differs.

VarbitChanged records its original payload, tick, cycle, callback order and server
reading before actor settlement. Tick snapshots retain later state independently.
Resource callbacks during the existing bounded completion grace are retained even
after the measured death tick, so a late Death Charge signal is inspectable. They
are diagnostic tail evidence, excluded from measured duration/damage and plans.
Net energy can combine spending/restoration. Death Charge flags/effects remain
raw evidence: zero does not establish upgrade absence and an end effect or charge
decrease does not identify proc versus expiry. The full-energy regeneration
contrast with RuneLite remains unresolved; **SpecTimeline is unchanged**.

## Bounds, privacy and lifetime

At most 2048 combined health readings/launches/signals/flights/impacts are retained
per attempt, in addition to existing capture limits. Overflow is explicit and disables the
recorded-tick shortcut without censoring the main capture. Rejected overflow
diagnostics do not spend the main event budget. Correlation has a 1,000,000-check
work bound and marks incomplete matches. Smoke reanchor discards pre-smoke observation events without
backfilling; existing pre-smoke state history stays separate.

Diagnostics consumes frozen worker results and retains its selectable 12-event
pages, bounded candidate links and explicit capture/link-search omissions. Copy
report is independent of that page. Its schema-2 narrative keeps the 24,000-character
budget and the 7,000-character target/HP/animation overview, while a separate fenced
JSON block (`schema: nex-evidence-1`) exports the whole retained evidence projection.
The report remains a diagnostic document, not a lossless replay or new recorder.

The JSON contains callback-ordered `events`, tick-only `samples` and `beforeStart`,
a zero-based deduplicated `states` table and report-local `targets`. Tick offsets
use measured start; event cycles/orders and HP cycles use the first retained
callback as origin. Target IDs are local references, never spawn/world identifiers.
Samples have no invented callback order. HP source, validity, bounds, maximum and
original reading time remain explicit, including source changes and unknowns.
Every launch has its animation, classification, phase/window, target/form, immutable
state reference and reconciliation. Projectiles retain their IDs, source-local flag
and start/end offsets; impacts retain zero damage and bounded unvalidated launch
candidate links. Link omissions and incomplete correlations stay explicit.

State fields allowlist exact equipped IDs, base/visible levels, offensive prayer
flags, style, selected spec, energy and existing resource/DC client/server counters.
No arbitrary map keys/strings, item names, full containers, roster keys, account IDs
or raw signal traces are serialized. Absent numeric fields are JSON null; adapter-provided unread sentinels such as
-1 remain unchanged. Unknown is never converted to zero. No new stock-usability, consumption or proc inference.

Only repeated `TICK_RESOURCES` polls with identical allowlisted payload/state/target
are omitted; value/server disagreement, changed state and reversions are retained.
Non-poll callbacks and HP readings are never dropped by this compaction. The JSON
reports retained/exported callback counts, unchanged polls omitted and upstream
capture truncation. A complete export means all *retained* useful evidence, not
recovery of evidence already lost to the unchanged observation limit. Candidate
lists keep the existing eight-link bound, separately from event coverage.

Evidence bypasses the narrative budget so selected comparison prose cannot crowd
out late events. Output stays bounded by the existing 2,048 observations and 1,024
combined main/pre-start state samples, fixed vocabularies/numeric fields and state
deduplication (8,000,000-character report ceiling). Clipboard fallback, frozen
selected-record/revision, bank/reload, hidden-session, reset and identity guards
are unchanged. The worker builds the immutable JSON; Copy never reads live state.

Raw state stays in the same 50-record memory lifetime; no files, uploads or
performance persistence are added.

## Sources and rights

Reviewed RuneLite release: **1.13.2**, commit
`84233cdb5ebc324d1d079de9da0ce61a772558fc`. Retrieved 20 October 2026.

| Source | Bounded use |
|---|---|
| [OpponentInfoPlugin](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-client/src/main/java/net/runelite/client/plugins/opponentinfo/OpponentInfoPlugin.java#L182-L218) | HUD script trigger and distinct HP integers; SHA-256 `c4a4d25f1195dff11006e1d9364a82d50c27b62923042d826adce1d4beebdb84` |
| [AnimationID](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-api/src/main/java/net/runelite/api/gameval/AnimationID.java), [SpotanimID](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-api/src/main/java/net/runelite/api/gameval/SpotanimID.java) | Candidate IDs only; hashes `46996e814d660f71c6561df0284fd0aff9f038f54f0ed14a41f2598f14ae3a47` / `09bd1f18487614584ac60061a3652eb16fe304ffee377f3a442ac61edf2e6702` |
| [Projectile](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-api/src/main/java/net/runelite/api/Projectile.java), [Client server values](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-api/src/main/java/net/runelite/api/Client.java#L757-L792) | Public nullable actors, cycles and last server values; projectile hash `73a7b148f90bd0bc9d342ec50f5e196ce66ba5189bf67d9d6491cc8b7490f236` |
| [Special Counter](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-client/src/main/java/net/runelite/client/plugins/specialcounter/SpecialCounterPlugin.java#L299-L379) | Callback-before-actor-update warning; hash `0ad261121762635fe8974cd33f706de35f3cff4bc1d527df31f864dbd03f6374` |
| [Timers and Buffs](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-client/src/main/java/net/runelite/client/plugins/timersandbuffs/TimersAndBuffsPlugin.java#L309-L329), [Regeneration Meter](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-client/src/main/java/net/runelite/client/plugins/regenmeter/RegenMeterPlugin.java#L148-L162) | Active 2/1/0 and unresolved full-cap clock contrast; hashes `fbd7c3d1989f69bef23f1309671ae8285e91788e400a71b1e63aa52483993523` / `716434a451a29cfd0d4431871aa873f814c3c224302f2c71b0e3b24aee0d0a39` |
| [Pinned monster reference](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/cdn/json/monsters.json) | Five normal-world maximum-HP facts independently checked; existing catalogue source `n3-observation-hp`, no bulk dataset copied |
| User-supplied research handoff, 20 October 2026 | Source-backed planning; explicitly no live Nex session. Public references are linked above. |

Algorithms, prose and tests are independently authored. No GPL implementation,
source prose, images or bulk dataset is imported. Existing notices remain intact.
Dynamic Nex stats, Soul Split healing, ordinary melee reflection, impact overhead
timing, counterfactual HP, contribution and team time retain their dependent gaps.

[Offline verification and exact client checks](../validation/nex-observation.md).
