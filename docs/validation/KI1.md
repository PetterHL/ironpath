# KI1 recent-boss implementation candidate

14 October 2026. Branch `ki1-recent-boss`, based on integrated main `2dd799a`; plugin 0.9.0, knowledge r3a-3, catalogue schema 10, preference schema 2 and ordinary ranged model `ranged-1`. No equipment or target facts are added to the runtime catalogue. UX and melee implementation proceeds independently; integration is recorded separately when performed.

## Delivered development scope

The first pilot is normal **Commander Zilyana**, using the reviewed R2 target and ranged packages already on main. This avoids making KI1 depend on incomplete Vorkath Salve/bolt or melee work. It is not a broad boss/raid coverage claim. The shared context carries optional activity/run, room/encounter, phase, mode, scaling, party and role evidence. Unknown party stays unknown: nearby-player count does not prove party membership. Solo, multi-phase and raid-room replay fixtures do not activate other runtime adapters.

Enable **Recent boss (experimental)** in Account and preferences. It is off by default and the choice is memory-only. After a subsequently observed spawn, local/boss interaction starts an engagement trace. A credited completion requires observed local damage, the tracked boss's death, and a matching server loot-composition signal within the bounded finalization window. Only after finalization does the quiet homepage entry appear. No focus/tab change, overlay, chat message, gameplay action or live numerical recommendation is introduced.

The displayed time is **observed engagement to observed death**, excluding pre-interaction work. It is not a game-reported kill timer, average, PB, full-cycle rate or estimate of seconds saved. There is deliberately no generic chat/PB parser. A target already damaged on engagement, missed state samples, overflow, missing credit, transformation, failure and loading/account transitions cannot become a clean completed trace.

The actual slots, real/current levels, active prayers, public combat-style index and special-energy state are sampled independently of account/quest evaluation. Same-tick changes preserve ordering/ambiguity, with zero elapsed ticks instead of invented extra time. Locally attributed damage is descriptive; it is not assigned to the weapon currently equipped when a delayed splat arrives. Inventory changes remain consumption/drop ambiguity, not asserted food events or a numerical food penalty.

After completion, a worker reuses reviewed advisor eligibility, route/importance/status ordering and compatible ranged packages. The chosen baseline is the longest captured nonzero-duration **worn state**, labelled with its captured-tick scope; it is never replaced by an optimized owned proposal. Other states are retained, not averaged into full-fight DPS. Accessory/ammo substitutions retain its weapon. Exact visible Ranged, prayer and style apply to both packages; the existing formula's Boolean potion assumption is not used for that state. A separate strongest supported owned-package comparison and recurring-ammunition scenarios do not imply a new grind or observed loading/stock.

Value shows worn/candidate packages, ordinary max hit, hit chance, DPS and interval where supported, plus sources and assumptions. Each candidate has the same independent baseline. Path reuses the existing captured planning route. New kills update the homepage entry quietly while an open recent context keeps its historical baseline. Back uses existing anchors; lifecycle transitions discard the surface and stale controls.

## Numerical and observation boundaries

- Numbers are theoretical ordinary attacks against the **reviewed unreduced Zilyana target**, not measured encounter DPS or team contribution. Other players/shared reductions remain unknown.
- Unknown items/slots are not empty. Unknown internal darts and active quiver/ammo state withhold affected comparisons; bank darts cannot establish a loaded baseline. Task-dependent helmet and lower-tier prayer support remain explicit gaps.
- Observed special-energy use withholds the contextual comparison because special/reduction effects are not established. A selected spec toggle alone is not treated as a successful hit. Enchanted-bolt proc gaps retain the existing per-metric support boundary. Defensive/supply/sustain value is not inferred from outgoing DPS.
- Candidate packages, stronger owned alternatives and hypothetical recurring ammunition are labelled scenarios. Their charge/loading assumptions do not claim account supplies. Zero/negative gains remain visible; neither gains nor upgrade deltas are added.
- Capture, finite-target time expectation, validated personal seconds, full activity/cycle throughput and acquisition hours remain separate. KI2/KI3 are not implemented.

## Public API evidence and correlation limits

Resolved Java dependency: RuneLite **1.13.1**. [Reviewed API metadata](../research/ki1-api-sources.json) pins source revision `5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03` and hashes the inspected bytes. New Java is independently authored; no external source, calculator test corpus or dataset is imported.

| Mapping | Evidence and boundary |
|---|---|
| Normal boss | Public gameval NPC `GODWARS_SARADOMIN_AVATAR = 2205`, matching the existing runtime `zilyana` target |
| Spawn/index reuse | `NpcSpawned`, `NpcChanged`, `NpcDespawned`, actor identity and world-view ID stay client-side; a generation plus monotonically increasing spawn key identifies the immutable trace |
| Current state | Worn container, public real/boosted skills, active prayers; `COM_MODE = 43`, `SA_ENERGY = 300`, `SA_ATTACK = 301`; standard supported ranged styles use 0 accurate / 1 rapid / 2 longrange; other inputs withhold numbers |
| Local damage/death | `HitsplatApplied.isMine()` and `ActorDeath`; damage does not identify launch weapon or exact current NPC HP |
| Player credit | `net.runelite.client.events.ServerNpcLoot` is an ordinary core event, not a dependency on the Loot Tracker plugin. It exposes NPC composition/items, **no actor/spawn identity or timestamp**. Only the unique active pilot trace's death-window can correlate it; actual ordering/late-duplicate behavior still needs client verification |
| Finalization | Five ticks after observed death, permitting a same/previous-tick credit signal; duplicates cannot reopen a finalized summary. Old or wrong-boss credit cannot independently complete another trace. Missing evidence is censored |

`ServerNpcLoot` correlation is experimental, not proven exactly-once server attribution. Exact controlled mapping/start/death/loot ordering and timer-origin checks are release gates below. No scene scan backfills an already-active boss when enabling; wait for a subsequent observed spawn. Unsupported transforms and late/missing evidence are not silently accepted.

## Resource and lifecycle safeguards

Active/finalizing evidence is change-coalesced, capped at 512 state segments, 20,000 events and 3,000 ticks; exceeding a limit censors the trace. Retain at most 50 frozen recent summaries. Comparisons run after completion on the existing executor, outside the client thread and EDT. No full advisor/quest evaluation is added to every capture tick. Source/model/setup dependencies travel with the result.

All state is session memory. Loading/region changes, logout/disconnect, unsupported worlds, identity/profile changes, clearing recent kills and disable reject old results and remove traces/summaries. Generation, request ticket and encounter identity guard queued worker/client/EDT publication. Existing bank/reference/preference boundaries and storage are unchanged; no telemetry, performance file, bank dump, export or history migration is added.

## Verification and pending acceptance

The implementation has synthetic event/state, mocked API and Swing tests. They cover corroborated/de-duplicated completion, wrong actor/boss credit, late/missing signals, mid-kill enable, missing samples, damage/gear-switch ambiguity, already-damaged starts, death, transform/index reuse, overflow/timeouts, bounded recent history and identity resets. Worker publication is explicitly queued across profile/loading/account changes and must be rejected. Cross-family fixtures retain unknown raid scaling/party and exact encounter/phase scope.

Comparison cases distinguish worn crossbow versus owned tbow, preserve a drained visible level, produce a genuine supported neck/ammunition upgrade, show an identical owned no-gain control, and retain unknown darts/quiver/item/spec refusals. The ordinary 1,944-case ranged reference corpus is unchanged. UI captures use synthetic accounts and fallback glyphs at 225×600 and enlarged font; they verify text wrapping, Value/Path/Back, frozen historical baseline and stale-control isolation, not real sprites/player usefulness.

JDK 11.0.32.1 / Gradle 8.10 / RuneLite 1.13.1 `test build --no-daemon` passes **260 tests, 0 failures, 0 errors, 0 skips**, including all 1,944 unchanged ranged oracle comparisons. The 20 added tests cover the specific behaviors above. Source metadata JSON and local document links parse/resolve; `git diff --check` passes.

Pure replay resource check: `Pure replay: 100 encounters / 9900 unchanged-state samples in 7.651646 ms; retained 50 summaries.` This measures the pure state machine on this worker, not actual client-thread latency or eligible-kill capture rate.

Synthetic component captures: [recent context](../images/ki1/recent-context.png), [Value](../images/ki1/recent-value.png), [Path](../images/ki1/recent-path.png), [unknown ammo](../images/ki1/unknown-ammo.png) and [enlarged font](../images/ki1/large-font.png). The deterministic test renderer invalidates nested Swing layout caches after disclosure changes; it asserts visible text has its measured wrapped height.

Actual-client gates remain **pending**, since no authenticated game session was available:

1. Controlled start/death/loot ordering, duplicate/late event attribution, player death/teleport/loading, mid-kill enable, transform/index reuse and timing at matched origins; investigate differences beyond one tick.
2. Count all eligible normal kills and all exclusions; measure complete-capture fraction (proposed ≥95%) and numerical coverage of ordinary player setups. Runtime recent-summary counts are not this independent eligible-kill denominator. No fractions are fabricated from synthetic fixtures.
3. Real-sprite native width, OS/client 100/125/150/200% scaling, focus/Back/scroll and account switching; confirm comprehension of observed engagement versus theoretical offence and actual versus owned setup.
4. Assess the exact opt-in passive surface against current Hub guidance before public distribution; this PR does not publish to Plugin Hub or claim policy approval. User acceptance is needed before default-enabling the feature.

Most-boss/raid coverage, calibrated personal seconds, broad common ammo/special/method support and guarded multi-trip cohorts remain the existing KI1–KI3/R3b/R4 backlog. The pilot does not close those requirements.
