# Nex public-event adapter review

Reviewed 15 October 2026. Adapter `nex-public-3` (0.16.2 follow-up; N1 used `nex-public-1`), RuneLite API compiled against
1.13.2. The original review is static API/source evidence. Initial client screenshots now
show a censored attempt; complete live capture, timing agreement and player
usefulness remain unverified. [Follow-up findings](../validation/nex-live-start.md).
[Second-test signal investigation](../validation/nex-live-signals.md).

## Evidence and mappings

| Evidence | Adopted mapping | Limit |
|---|---|---|
| [Generated NPC IDs](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-api/src/main/java/net/runelite/api/gameval/NpcID.java) | Nex 11278–11282, including spawning, soul-split, deflect and dying; four named mages 11283–11286 | Index alone is not identity. Match actor object, world-view and a local spawn serial. Pets, prison monsters, reavers and other NPCs are excluded. Transformations preserve the same actor key; replacement censors the old attempt. |
| [Pinned Nex droprate plugin](https://github.com/Worley03/nex-droprate-calculator/blob/48217ba0e2915e9553c922d73034c5f2866fc4a7/src/main/java/com/nexdropratecalculator/NexDroprateCalculatorPlugin.java) | `NEX_BARRIER == 3` denotes active arena; 3→2 is an end signal, 0/1 is leaving. The smoke line identifies the start; callback delivery order is not assumed. | Its timer and all-NPC contribution implementation are not imported. IronPath requires an observed 2→3 boundary, a matched smoke line, all phase boundaries, continuous worn samples and corroborating tracked Nex death/dying for a complete capture. |
| [Nex text reference](https://github.com/luuj/nex/blob/c2a5645ba8c19b751d320f7d88449fa4b6450a8c/NexText.java), [phase reference](https://github.com/luuj/nex/blob/c2a5645ba8c19b751d320f7d88449fa4b6450a8c/NexPhase.java) | Smoke, shadow, blood, ice, Zaros and minion call lines; blood siphon/sacrifice and ice special lines | Historical public reference for message facts, not current gameplay/policy approval. No source code, strategy, prayer/tile overlays or dataset is copied. |
| [Prior MP2 strategy review](../RESEARCH.md#pilot-evidence-reviewed-on-15-october) | Shadow has ranged windows during Embrace darkness and melee opportunities around Fear the shadow | Record those actual lines as subphases. Never label the whole phase melee or infer an attack from time wearing a style. Current attempts to fetch the mirror returned HTTP 403; this delivery does not claim a fresh strategy-page review. |
| Public `OverheadTextChanged`, `ChatMessage`, `NpcChanged`, `ActorDeath` | Actor-bound overhead lines; narrowly accepted `Nex\|`/`Nex:` messages in NPC_SAY/GAMEMESSAGE with the tracked Nex present; death/dying + barrier end within five ticks | No line or despawn alone completes a kill. Duplicate phase lines do not erase an already observed subphase. Missing phases, end signals or tick samples censor the attempt. Unknown/mid-kill initial phase remains unknown. |
| [Hitsplat API](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-api/src/main/java/net/runelite/api/Hitsplat.java) | `isMine()` and `isOthers()` classify observed hits on the explicit target set | Healing and unclassified types are excluded and noted. Duplicate callbacks for the same hitsplat object are ignored; equal-valued distinct hits remain distinct. Public events cannot establish all true damage, loot eligibility, MVP or official contribution. |
| [Death Charge handling](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-client/src/main/java/net/runelite/client/plugins/timersandbuffs/TimersAndBuffsPlugin.java) | Raw active/cooldown values are sampled. Two active charges establish improved active use; one/zero does not establish upgrade absence | The saved upgraded toggle is an explicit planning assumption. State and energy changes alone do not establish a particular cast/proc, spec weapon or regeneration cause. |
| [Public resource constants](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-api/src/main/java/net/runelite/api/gameval/VarPlayerID.java), [varbits](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-api/src/main/java/net/runelite/api/gameval/VarbitID.java) | Worn item IDs/public definition names and ammo quantity; raw quiver temporary ammo, charge flag and splinter count; special energy/selection; levels, prayers and inventory quantities | Quiver temporary counters are deliberately raw: selection/freshness/activation require real-client verification. Other internal item charges remain unknown. Zero raw counters are not a claim of absent usable stock. Inventory losses do not prove food/potion consumption. |

The observed timer starts at the captured **arena barrier 2→3** and ends at the
first tracked Nex death/dying signal. It is not calibrated to the game's Nex timer;
there is no fixed four-tick backfill or comparison between unlike timer origins.
Start state is captured synchronously in the boundary callback, before the following
GameTick advances the clock. Missing same-tick boundary/state evidence censors the
attempt. Since 0.16.1 a later smoke callback can corroborate that already sampled
boundary within ten ticks, as can an earlier matching smoke line. Prior samples
remain UNKNOWN until the actual smoke callback; their phase/state is not backfilled.
A mid-kill arrival without an observed boundary cannot be promoted by smoke.
End signals must correlate within five ticks. These bounded matching windows are
adapter policy, not claims of guaranteed server timing.

The second live test still has no captured start/end barrier despite all phases,
continuous samples and a death/dying signal. Re-reading the pinned plugin confirms
active value 3 and end transition 3→2, but **does not establish 2→3 as a necessary
start on every bank trip**. It uses the smoke line for start instead. IronPath's
2→3 requirement remains an unvalidated conservative restriction, not a verified
mapping for the one-kill method. A source review and synthetic sequence do not
establish which transitions this client actually received.

Since 0.16.2, `VarbitChanged.getValue()` supplies the event value directly; tick
polling remains a separate fallback. The public
[event contract](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-api/src/main/java/net/runelite/api/events/VarbitChanged.java)
labels it the new value. Missing smoke matching no longer conceals a received
smoke line: details show both timestamps. Up to 32 client-thread signal entries
retain the first 16 and latest 16 barrier transitions, smoke, Nex forms,
ActorDeath/despawn and scene transitions, without account identifiers or files.
When one end signal is missing, capture waits up to 20 ticks for diagnostic
evidence; both present signals still require the original five-tick correlation.
This diagnostic wait is not a newly verified server bound. Travel still flushes
or censors immediately. Eligibility and timer origin are unchanged.

Damage is aggregated by **phase/subphase at the observed impact** and spawn target.
A hit is never assigned to the weapon worn at impact. Animation samples retain
current state and target as possible launch evidence, without pretending every
animation is an attack. Same-tick and delayed-hit uncertainty remains explicit.
Known model weapons supply worn-role labels; selected special states are candidates,
not proof of a launched special. Unsupported weapons are retained as unclassified.

## Sessions and comparability

Keep at most 50 attempts, 1,024 state/animation entries per attempt, 20,000 event
callbacks and 3,000 ticks per active attempt. Completed evidence is frozen; hit
objects, actors and active buffers are discarded at scene transitions. Summaries,
including bounded immutable actually-worn timelines, stay only in memory.

On leaving an observed arena, first finalize a corroborated end or censor the
active attempt. Clear live account/reference state and invalidate queued actions.
Hide summaries during transit. Resume only for the same profile/character and world
at a visible bank. After that recognized bank visit, return travel may resume at
an observed Nex scene. Each pending leg has a 100-tick limit; repeated loading
events do not extend it. Ambiguous transit, expiry, logout/disconnect, profile or
account change, unsupported worlds, disable and Reset clear summaries.

Statistics use complete captures in the latest complete kill's cohort: the saved
method/locks, exact set of observed worn-role packages/levels/prayers and observed arena roster.
Roster is sampled once per game tick. Hashes are session-only; visible people are not verified party membership.
An unread or changing roster uses an unmatched cohort. Gear/team changes separate
cohorts; state sampling order does not turn a Tbow's empty offhand into the melee
setup's offhand. Transient worn combinations can conservatively split cohorts.

The duration range and share range are observed min–max, not confidence intervals.
Mean per-kill share weights each eligible kill equally; aggregate share divides
summed local damage by summed classified total damage. Their sample counts can
differ when no classified damage was captured. Failures/censored captures stay
outside complete averages. Captured/eligible counts concern observed attempts;
no denominator for unobserved kills or all server damage is claimed.

Only method, explicit item locks and manual facts enter local preference schema 3.
Schemas 1/2 migrate their existing choices. Names, roster hashes, containers,
performance, timelines and summaries are never serialized or logged. No runtime
network access, scraping or other-plugin integration is added.


The first visible same-account bank visit is recorded even without suspended
history. A visible bank has priority over a nearby loaded Nex NPC; arena return
requires the reviewed active/idle arena barrier state as well as a matching NPC.
An active capture message replaces the ambiguous waiting text at the next tick.
Frozen details expose start, matched smoke, death and arena-end ticks, phase
coverage and continuity. Specific missing reasons replace the former generic
completion-failure message. Hard identity/world/reset/expiry boundaries remain.
