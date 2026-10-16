# Nex public-event adapter review

Reviewed 15 October 2026. Adapter `nex-public-4` (0.16.5 follow-up; N1 used `nex-public-1`), RuneLite API compiled against
1.13.2. The original review is static API/source evidence. Initial client screenshots now
show a censored attempt; complete live capture, timing agreement and player
usefulness remain unverified. [Follow-up findings](../validation/nex-live-start.md).
[Second-test signal investigation](../validation/nex-live-signals.md).
[Current smoke-anchor contract](../validation/nex-smoke-anchor.md).

## Evidence and mappings

| Evidence | Adopted mapping | Limit |
|---|---|---|
| [Generated NPC IDs](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-api/src/main/java/net/runelite/api/gameval/NpcID.java) | Nex 11278–11282, including spawning, soul-split, deflect and dying; four named mages 11283–11286 | Index alone is not identity. Match actor object, world-view and a local spawn serial. Pets, prison monsters, reavers and other NPCs are excluded. Transformations preserve the same actor key; replacement censors the old attempt. |
| [Pinned Nex droprate plugin](https://github.com/Worley03/nex-droprate-calculator/blob/48217ba0e2915e9553c922d73034c5f2866fc4a7/src/main/java/com/nexdropratecalculator/NexDroprateCalculatorPlugin.java) | `NEX_BARRIER == 3` denotes active arena; 3→2 is an end signal, 0/1 is leaving. The smoke line identifies the start; callback delivery order is not assumed. | Its timer and all-NPC contribution implementation are not imported. IronPath requires an observed smoke announcement, all phase boundaries, continuous worn samples and corroborating tracked Nex death/dying plus arena end. 2→3 is diagnostic, not a required start. |
| [Nex text reference](https://github.com/luuj/nex/blob/c2a5645ba8c19b751d320f7d88449fa4b6450a8c/NexText.java), [phase reference](https://github.com/luuj/nex/blob/c2a5645ba8c19b751d320f7d88449fa4b6450a8c/NexPhase.java) | Smoke, shadow, blood, ice, Zaros and minion call lines; blood siphon/sacrifice and ice special lines | Historical public reference for message facts, not current gameplay/policy approval. No source code, strategy, prayer/tile overlays or dataset is copied. |
| [Prior MP2 strategy review](../RESEARCH.md#pilot-evidence-reviewed-on-15-october) | Shadow has ranged windows during Embrace darkness and melee opportunities around Fear the shadow | Record those actual lines as subphases. Never label the whole phase melee or infer an attack from time wearing a style. Current attempts to fetch the mirror returned HTTP 403; this delivery does not claim a fresh strategy-page review. |
| Public `OverheadTextChanged`, `ChatMessage`, `NpcChanged`, `ActorDeath` | Actor-bound overhead lines; narrowly accepted `Nex\|`/`Nex:` messages in NPC_SAY/GAMEMESSAGE with the tracked Nex present; dying can precede the corroborating barrier end | No line or despawn alone completes a kill. Duplicate phase lines do not erase an already observed subphase. Missing phases, end signals or tick samples censor the attempt. Unknown/mid-kill initial phase remains unknown. |
| [Hitsplat API](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-api/src/main/java/net/runelite/api/Hitsplat.java) | `isMine()` and `isOthers()` classify observed hits on the explicit target set | Healing and unclassified types are excluded and noted. Duplicate callbacks for the same hitsplat object are ignored; equal-valued distinct hits remain distinct. Public events cannot establish all true damage, loot eligibility, MVP or official contribution. |
| [Death Charge handling](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-client/src/main/java/net/runelite/client/plugins/timersandbuffs/TimersAndBuffsPlugin.java) | Raw active/cooldown values are sampled. Two active charges establish improved active use; one/zero does not establish upgrade absence | The saved upgraded toggle is an explicit planning assumption. State and energy changes alone do not establish a particular cast/proc, spec weapon or regeneration cause. |
| [Public resource constants](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-api/src/main/java/net/runelite/api/gameval/VarPlayerID.java), [varbits](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-api/src/main/java/net/runelite/api/gameval/VarbitID.java) | Worn item IDs/public definition names and ammo quantity; raw quiver temporary ammo, charge flag and splinter count; special energy/selection; levels, prayers and inventory quantities | Quiver temporary counters are deliberately raw: selection/freshness/activation require real-client verification. Other internal item charges remain unknown. Zero raw counters are not a claim of absent usable stock. Inventory losses do not prove food/potion consumption. |

The measured timer starts at the **observed Nex smoke announcement** and ends
at the first tracked Nex death/dying signal. It is not calibrated to the game's
timer: the pinned reference's four-tick offset is not imported, and arena entry
does not define combat start. The 2→3 transition remains an optional diagnostic.
The earlier requirement that smoke match arena entry is superseded.

Capture may begin with an unknown interval while waiting for smoke. On an actual
smoke callback, an interval with no later phase/subphase, positive classified
damage or unclassified hits can be anchored at that callback. Earlier worn states
remain immutable in a separate pre-smoke history; they are not relabeled or used
in combat statistics, owned comparisons or the fight cohort. The smoke callback
captures its actual current state. Duplicate smoke cannot move an established
start. Prior combat/later-phase evidence blocks re-anchoring, and missing smoke
keeps a true late arrival incomplete. Both histories share the original sample,
callback and elapsed-time bounds; anchoring does not restart those resource limits.

Arena completion may follow dying by up to 20 ticks. A barrier signal before
death/dying has a shorter five-tick allowance for callback ordering. Both signals
and all phases/continuous pre-death sampling remain mandatory; distant or absent
signals censor. These are bounded adapter policies, not guarantees of server
timing. The measured duration excludes the subsequent dying-to-arena-end delay.
An old censored summary is never recalculated into a complete one.

Since 0.16.2, `VarbitChanged.getValue()` supplies the event value directly; tick
polling remains a separate fallback. The public
[event contract](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-api/src/main/java/net/runelite/api/events/VarbitChanged.java)
labels it the new value. Missing smoke matching no longer conceals a received
smoke line: details show both timestamps. Up to 32 client-thread signal entries
retain the first 16 and latest 16 barrier transitions, smoke, Nex forms,
ActorDeath/despawn and scene transitions, without account identifiers or files.
When one end signal is missing, capture waits up to 20 ticks; once both exist,
it settles callbacks for five ticks, or flushes immediately during travel.
Travel still requires both matching end signals to complete. Neither despawn nor
loot alone supplies the missing signal. Eligibility/timer semantics are versioned
as `nex-public-4`; cohort keys include that version.

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
an observed Nex scene. Build 0.19.1 removes the former 100-tick expiry: elapsed
travel/waiting time does not erase bounded completed evidence. A nearby Nex or
arena-like barrier reading on the outbound leg keeps history suspended until the
bank is opened, rather than clearing it. The adapter remembers a verified arena
source across an exit-barrier update until its next transition/clear, preventing
LOADING from misclassifying that departure. Unrecognized transit, logout/disconnect,
profile or account change, world change, unknown identity, disable and Reset still
clear summaries. Returning capture cannot resume until revalidation. See the
[client finding and regression report](../validation/nex-bank-retention.md).

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
completion-failure message. Hard identity/world/reset boundaries remain. The old
travel-expiry policy is superseded by the 0.19.1 correction above.
