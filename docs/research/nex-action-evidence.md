# Nex action evidence and delivery decision

Reviewed **24 October 2026** against IronPath PR #54 head
[`be6015a`](https://github.com/PetterHL/ironpath/commit/be6015aa19c6d6886dd4792ec6579a77de12a0c3).
This is a source review and product decision, not a fresh gameplay validation.
[TASKS.md](../../TASKS.md#nex-delivery-decision--24-october-2026) is the only active
backlog. It schedules N3-ACTION, then N3-ADVICE. Nex Trio MP2 remains the pilot.

## Concrete missing input

IronPath's [NexObserver.chat](https://github.com/PetterHL/ironpath/blob/be6015aa19c6d6886dd4792ec6579a77de12a0c3/src/main/java/com/ironpath/NexObserver.java)
currently accepts NPC_SAY/GAMEMESSAGE and forwards only Nex-prefixed phase text.
It does not map food-success or Death Charge cast-success messages; SPAM food
messages are filtered out. This is a concrete implementation gap, not proof that
the public API lacks useful action evidence. Existing animations, target states,
resource readings and effects remain useful corroboration.

## Pinned reference implementations

The Plugin Hub manifest revision reviewed was
[`3c20786`](https://github.com/runelite/plugin-hub/tree/3c20786491f6cf908643dc70cae972041f5508c4).
Pins below identify the code reviewed, not a promise that each mapping works
unchanged on IronPath's supported RuneLite versions.

| Reference | Useful implementation | Boundary / decision |
|---|---|---|
| [Attack Timer Metronome](https://github.com/ngraves95/attacktimer/blob/78c5d72aa11490debc3cc71b8a87dcefde7353cb/src/main/java/com/attacktimer/AttackTimerMetronomePlugin.java), `78c5d72`; [Attacking](https://github.com/ngraves95/attacktimer/blob/78c5d72aa11490debc3cc71b8a87dcefde7353cb/src/main/java/com/attacktimer/Attacking/Attacking.java) | Combines local animation, interaction, weapon speed/style and attack-state/cooldown; handles continuing animation, delayed state and food-message delay. | Adopt the bounded state-machine pattern for ordinary Fang first. The broad animation/movement heuristics are not a universal accepted-attack API. Reconcile callback/settled state explicitly; do not import UI-render timing into the combat model. |
| [EatTest](https://github.com/ngraves95/attacktimer/blob/78c5d72aa11490debc3cc71b8a87dcefde7353cb/src/test/java/com/attacktimer/EatTest.java), same pin | Exercises food messages as SPAM, including normal and fast foods. Plugin code distinguishes delay classes. | Add an explicit food/message allowlist and ordering tests. The source notes absent messages at full HP and generic food-name ambiguity: no message cannot prove no consumption. An inventory reduction alone is also insufficient. |
| [Death Charge Reminder](https://github.com/InfernoStats/death-charge-reminder/blob/99d99c6a5d1c0fb247f007d259393dda2253b2cd/src/main/java/com/infernostats/DeathChargeReminderPlugin.java), `99d99c6` | Recognizes distinct normal/upgraded successful-cast messages and starts a reminder. | Reuse message evidence alongside active/cooldown/effect observations. Normalize markup, restrict accepted message types and retain typed evidence, not raw chat. Do not import its wall-clock reminder as a server proc/expiry model. |
| [Combat Insight: LiveCombatSnapshot](https://github.com/BornDied/combat-insight/blob/a0f2e060973d1e00a835bcd2afa1e95cfa298bca/src/main/java/com/combatinsight/live/LiveCombatSnapshot.java), `a0f2e06`; [SpecialAttackTracker](https://github.com/BornDied/combat-insight/blob/a0f2e060973d1e00a835bcd2afa1e95cfa298bca/src/main/java/com/combatinsight/live/SpecialAttackTracker.java) | Estimates HP from health-bar bounds; keeps bounded pending spec/target/hitsplat observations. | Keep IronPath's HP intervals instead of presenting a midpoint as exact. Pending-target/time-window matching is a heuristic, not proof of ownership in crowded Nex. Retain misses and ambiguity; do not rebuild existing kernels from this reference. |
| [Vardorvis Heal Tracker](https://github.com/AnuVen/vardorvis-health-tracker/blob/f1d91b6ee3cf6e203d797986f716539d0adba289/src/main/java/com/vardorvishealth/VardorvisHealTrackerPlugin.java), `f1d91b6` | Reads heal hitsplats for its known NPC and resets on despawn. | Corroborates direct heal-event accounting already present in IronPath after #53. It does not solve exact HP replay or encounter lifecycle. No new healing engine or Vardorvis pivot is needed. |

## Shortcuts rejected

- [Zero XP Drop](https://github.com/YoungSensei-TSU/zero-xp-drop/blob/d2921276b88d91bcb76b2f9e7d73403afbbde75d/src/main/java/com/sensei/zeroxpdrop/ZeroXpDropPlugin.java),
  `d292127`, falls back to a projectile's source tile matching the local player's
  current/previous tile when actor source is unavailable. Stacked Nex players make
  that insufficient for ownership. Missing XP after a guessed attack also cannot
  independently prove a miss. Never promote an UNKNOWN first flight this way.
- An attack menu click is intent; a ready cooldown is modeled readiness. Neither
  establishes an executed attack or a missed legal opportunity. A cast-success
  message does not by itself classify subsequent DC proc, eligibility or expiry.
- A generic health bar gives an interval. A later bar/HUD reading cannot become
  exact earlier impact HP without a supported time/target relation.

## Chosen first slice

Reuse the existing N2 package construction and combat arithmetic. First connect
supported ordinary Fang attacks to a fixed-weapon/style/tempo equipment comparison.
This avoids making ranged ownership or a full spec/DC/resource plan prerequisites
for an independent ordinary-damage result. It does not remove their existing guards.

The result compares expected damage under two setups on the **same supported
attack sample**. Preserve zero hits/misses, attack-time gear and negative gains;
show supported count and excluded/ambiguous observations. Do not select only
positive hits, infer hypothetical attack opportunities, extrapolate to the full
kill, or compare observed random damage directly against expected alternative damage.

HP-independent cases can proceed when valid bounds prove the cap irrelevant to
both alternatives. Otherwise evaluate the paired difference over shared feasible
HP/mechanic states and justified breakpoints, or withhold it. Separate interval
endpoints or a midpoint are not automatically a valid bound on that difference.
Target/phase/overhead support and time validity still apply.

The eight strings in the current automatic-gap list describe the full plan's
requirements; they are not eight measured completion percentages. N3-ADVICE must
introduce a separately tested per-result support contract. **This documentation
change enables no automatic runtime result.** Full N3 and N4 remain unvalidated.

## Validation and reuse boundary

N3-ACTION must ship an inspectable bounded timeline and deterministic replays,
then a short controlled check of the supported attack/eat/cast mappings.
Basic mappings do not require a fresh full Nex kill. Promotion to the scoped
Nex result still requires relevant encounter-window evidence; synthetic replays
alone are insufficient. Unknown pre-start readiness remains unknown.

No external code or dataset is copied in this documentation update. Use independent
implementations and public APIs; no runtime dependency on another plugin. If later
adapting BSD-licensed code, review the pinned file/root licences and preserve all
applicable notices. Source availability is not permission to import an unrelated
GPL implementation or dataset. Existing plugins are evidence of implementation
patterns, not automatic distribution approval for IronPath.
