# Nex damage callback and source review

23 October 2026. Build **0.29.0**, adapter `nex-public-9`, observations
`nex-observations-4`, export `nex-evidence-2`. Integration base is PR #51
`feb439bdc93278e05b08844a0ab941f5593adf87`, whose PR #50 dependency is
`f02028dd0af74e358e69e8b268bdcae64c669761`. Main remains `7f68d0e`.
This follows the [23 October handoff](https://github.com/PetterHL/ironpath/pull/51#issuecomment-6359152035).
Neither dependency is merged here.

## Callback contract and correction

All RuneLite references below are pinned to **1.13.2**,
`84233cdb5ebc324d1d079de9da0ce61a772558fc`, retrieved 23 October.

- [HitsplatApplied](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-api/src/main/java/net/runelite/api/events/HitsplatApplied.java)
  documents an event when a hitsplat is **processed**, regardless of rendering,
  including when four hitsplats are already visible. Visible slots are not a safe
  collection limit.
- [Hitsplat](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-api/src/main/java/net/runelite/api/Hitsplat.java)
  is an interface with amount/type/disappearance getters and type-based
  mine/other classification. It provides no stable identity, immutability,
  allocation-per-application or duplicate-delivery guarantee.
- [Hooks.post](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-client/src/main/java/net/runelite/client/callback/Hooks.java)
  forwards to the event bus. IronPath has one subscribed forwarding handler.
  The public source does not establish the injected client's object allocation
  lifetime. The resolved binary supplies the narrower evidence below. The later
  23 October live check confirms repeated object use; duplicate dispatch has
  not been demonstrated.

The actual Gradle-resolved `net.runelite:injected-client:1.13.2` artifact was
also inspected using JDK 11 `jar`/`javap -p -c` during development. SHA-256:
`2e45b9ee289d31d8c1f24a668143f9ca68682ba5ecbad5137e45803cd7fba942`.
The payload implementation (`ca`, implements public Hitsplat) has a mutating
update method. Actor implementation `ci.dv` obtains an existing payload from its
list when a slot exists (allocating only when necessary), then calls that update.
The update hook publishes that same object via a temporary static reference;
`ci.hb` places it in a newly allocated HitsplatApplied and posts it. The reference
is cleared at the beginning of the next application. Non-rendered paths create
a temporary payload when there is no updated slot. Thus **distinct processed
hits can carry the same mutable payload object on this supported binary**.
This is implementation evidence, not an allocation guarantee for future builds
or proof about which paths the supplied live kill took. No binary/source dump,
obfuscated class dependency, reflection or private API is added to IronPath.
The public callback remains the sole runtime interface.

The justified correction is to use the **application callback**, rather than
the payload object's fight-long identity, as the adapter's event boundary. Freeze
amount/type/classification immediately and assign a fresh scalar sequence key.
Equal damage and multiple same-tick callbacks remain separate. The pure capture's
explicit event-key replay guard remains. The adapter cannot safely identify a
duplicate delivery from amount/tick, object identity or disappearance cycle;
replaying the same callback twice is explicitly counted twice. No unsupported
heuristic replaces the removed identity filter.

A bounded identity set now measures repeated objects only; it never rejects a
callback or censors capture. Synthetic mutable reuse reproduces the former
filter's failure mode, now supported
by the pinned binary's reuse path, **not a replay of the supplied live kill or a
measured explanation of all its missing damage**.
The later 23 October live report confirms 487 repeated-object callbacks among
513 tracked-target callbacks, preserves all 171 own impacts and passes the
per-target damage-minus-healing check. This supports the correction for that
kill; it does not recover or quantify all losses in the earlier 0.28.3 report.
[Detailed single-kill evidence and limits](../validation/nex-damage-observation.md#live-one-copy-check--23-october-2026).

## Source comparison and disposition

| Source | Finding and disposition |
|---|---|
| [RuneLite DPS Counter](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-client/src/main/java/net/runelite/client/plugins/dpscounter/DpsCounterPlugin.java) | Adopt direct callback-to-scalar classification; retain IronPath's explicit tracked Nex/minion/view/lifecycle filters and healing/unclassified exclusions. Its broad boss/interacting filters and Party counters do not establish IronPath coverage. |
| [Damage Counter](https://github.com/0anth/damage-counter/blob/d69450bf84a738c696fa71591cd3a9700562f236/src/main/java/com/damagecounter/DamageCounterPlugin.java), `d69450bf84a738c696fa71591cd3a9700562f236` | Same immediate event pattern, no fight-long payload identity guard. Do not adopt its broad total accumulation, boss-reset or Party/name handling. Keep explicit heal/unclassified treatment. |
| [Combat Logger](https://github.com/SuperNerdEric/combat-logger/blob/c2805b8faef25ec90dae9042d71c8bf08a8c387d/src/main/java/com/combatlogger/CombatLoggerPlugin.java), `c2805b8faef25ec90dae9042d71c8bf08a8c387d` | Immediate scalar capture and explicit unknown source support the existing immutable path. Party evidence stays separate. No names, full containers, disk log, queue, Party integration or source inference imported. |
| [Special Counter](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-client/src/main/java/net/runelite/client/plugins/specialcounter/SpecialCounterPlugin.java) | Energy callback may precede actor update; captures original tick before deferred actor read. Already adapted in IronPath: original payload/time/server reading and later tick state are separate. Preserve both; no parallel deferred classifier, confirmed spec or weapon attribution from a delta. |
| [Regeneration Meter](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-client/src/main/java/net/runelite/client/plugins/regenmeter/RegenMeterPlugin.java) | Compares fixed Lightbearer interval and ring-switch clock adjustment; resets the regeneration clock at 1000 energy. The later live capture records full energy at tick 129, spending at 137 and the next +10% at 162. General full-cap behavior remains a separate mechanics follow-up; do not silently change SpecTimeline or infer readiness. |
| [Timers and Buffs](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-client/src/main/java/net/runelite/client/plugins/timersandbuffs/TimersAndBuffsPlugin.java) | DC active 2 creates a timer, 1 creates only after 0, and 0 removes it. Already retain raw active/client/server transitions and normal/upgraded effects. Timer display is not proof of proc, expiry, cast or causal energy restoration; broader DC semantics remain separate. |

The processing integration, counters and tests are independently authored. The
adapted direct-event pattern's full RuneLite/Damage Counter BSD notices are
[retained](../../LICENSES/Nex-observation-references.txt). No GPL code/data,
optimizer beam/slot pruning, formula changes or external runtime integration.

## Bounded counters and projectile uncertainty

`NexCallbackDiagnostics` holds only enum-keyed scalar tallies: NEX, each of
FUMUS/UMBRA/CRUOR/GLACIES, OUTSIDE and UNKNOWN. Entry, first rejected guard,
measured own/other, healing/unclassified, repeated-object observations and
impact retention/overflow have counts and positive payload sums. Counts saturate
at 65,535, sums at 2,147,483,647; `atLimit` explicitly marks either bound. Negative
payloads have an invalid count, never a negative damage sum. Counters overlap
where stated (entry, repeated object and classification); they must not be added
as independent damage. `HIT_IDENTITY_REJECTED` is explicitly zero under this policy.

Counter lifetime is the active attempt, including rejected/post-death callbacks.
Smoke reanchor, next attempt and existing reset/identity/scene boundaries clear
them with observation evidence. Frozen completed summaries retain immutable
copies for the existing 50-record lifetime. They spend no event/sample/observation
slots. The diagnostic identity set stops retaining new objects at 2,048 and counts
that limit; collection continues. Payload sums are **not** fight damage. Measured
own/other counters exclude later-than-death, invalid, duplicate explicit keys,
out-of-order and main capture-limit hits. Impact omission counters separately
identify observation-buffer loss without affecting accepted damage totals.

[Projectile](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-api/src/main/java/net/runelite/api/Projectile.java)
marks both actors nullable. [ProjectileMoved](https://github.com/runelite/runelite/blob/84233cdb5ebc324d1d079de9da0ce61a772558fc/runelite-api/src/main/java/net/runelite/api/events/ProjectileMoved.java)
documents repeated movement callbacks (ground-target events may occur only once),
but neither API promises that a later callback resolves missing actors. The same
resolved 1.13.2 binary's projectile implementation (`ai`) resolves both actor
getters from current actor tables using signed indices; index zero returns null.
Consequently an absent table entry can in principle resolve later, whereas an
unknown index need not. This establishes dynamic lookup, not an observed Nex
callback order, a fresh late source binding, or a safe ownership upgrade. Therefore
preserve first-eligible-callback deduplication and the existing scope: exclude
known other sources and known outside/untracked targets; keep nullable actors.
Do not promote a retained UNKNOWN from later getters. Exclusion/repeat/identity
limit/retention counters explicitly count **callbacks**, not unique shots.

Flights now carry typed **LOCAL / OTHER / UNKNOWN** evidence. Runtime OTHER
sources remain excluded and counted; a null source always exports UNKNOWN.
JSON and Diagnostics use the same type, with no misleading `localSource=false`.
Only explicit LOCAL evidence can participate in existing ranged candidate matches.
ID, proximity, likely weapon or nearby animation never supplies ownership.

## Damage presentation and export

The headline **Damage contribution** reads **Unverified**. Timing, gear and
independently supported resource/state arithmetic remain available. Diagnostics
retains observed local/total damage and the scoped recorded hitsplat ratio;
no HP-loss denominator or coverage threshold is invented. Existing `share()`
arithmetic is unchanged and explicitly documented as a recorded-payload ratio.

Export `nex-evidence-2` replaces the flight boolean with `source`, adds complete
fixed-vocabulary `callbackChecks` (including zeros and saturation), counter scope,
observed damage sums/ratio, `damageCoverage: UNVERIFIED` and null
`wholeKillContributionPercent`. All retained ordered useful evidence remains
page-independent; unchanged-poll compaction, allowlists, bounds, worker guards,
privacy and clipboard fallback remain. The old `nex-evidence-1` contract is not
silently relabeled. UI pagination remains 12 rows.

[Validation, single-kill live findings and deferred checks](../validation/nex-damage-observation.md).
