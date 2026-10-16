# Nex owned equipment: ordinary offence contract

16 October 2026. Development build 0.16.6, knowledge `n2-owned-2`, catalogue
schema 14, preferences schema 3. Models `nex-owned-1`, `ranged-2`, `melee-2`.
This is N2's bounded ordinary comparison, not a Nex encounter simulator.

## Evidence and independent implementation

The existing catalogue pins [Nex revision 15371100](https://oldschool.runescape.wiki/w/Nex?oldid=15371100)
through `r2-nex` and `r3b2-nex`. The direct raw Wiki request returned 403 in this
session. The bounded numerical target facts were cross-checked against the
already pinned [development reference monster data](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/cdn/json/monsters.json):
Defence 260; stab/slash/crush defence 40/140/60; light/standard/heavy ranged defence
190/190/150; Magic 230; magic attack 100; size 3. This is a fixed base-stat scenario,
not an assertion of currently observed NPC stats, debuffs or every phase.

Only these bounded factual values were independently entered into the existing
catalogue. No external implementation, guide prose, images or dataset is copied
or bundled. The original licence/source records and notices remain. Reviewed
formula, gear, prayer and equipment facts reuse the existing claim dependencies;
the result includes item/target/model source IDs and revisions. Current game
agreement and a fresh complete guide review are not claimed from this check.

The [pinned equipment reference](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/Equipment.ts)
and existing quiver sources agree on the ordinary eligible-projectile +10 ranged
attack/+1 strength for charged and blessed forms. Our original Java input now
distinguishes the physical worn ammo slot from a selected quiver projectile.
Blessing/quiver zero melee offence uses the existing exact-form factual evidence.
This does not value blessing prayer, charge duration, upkeep or supply competition.

Exact locked forms were rechecked against official item identifiers at
[the pinned ItemID source](https://github.com/runelite/runelite/blob/42a6f17a6a2e8e478aa763890ecd0181a59dad38/runelite-api/src/main/java/net/runelite/api/gameval/ItemID.java)
and the bounded factual bonuses in the
[pinned equipment reference](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/cdn/json/equipment.json).
Dragon defender (l), 24143, retains stab/slash/crush 25/24/23 and strength 6;
Infernal cape (l), 24224, retains 4/4/4 and strength 8. They have separate exact
entries and do not imply ownership of another form. Broken/mangled forms remain
unreviewed. Only these limited game facts were independently recorded; no
reference code, generated dataset, prose or images are copied. The source URL
validator permits only this repository's revision-pinned equipment JSON path.

## Input and support boundary

- Every distinct captured input state/role/phase/window remains separate. The
  comparison never chooses a longest-worn baseline or weights by worn duration.
  The full immutable N1 timeline remains the observation record.
- Role/support filters are presentation only. Supported carried comparisons,
  bank-preparation scenarios and unavailable states are counted separately.
  Grouped reasons do not discard distinct states; their counts are not coverage
  of combat time, attacks, damage or the account's relevant alternatives.
- Current visible Attack/Strength/Ranged levels, including drains/decay, are
  passed directly into independent family arithmetic. A boost is not inferred
  from those levels. Known supported active prayers are retained. Lower/unreviewed
  combinations are gaps, not silently no-prayer inputs.
- Four-entry reviewed melee style tables map the captured style button in order.
  Other melee button maps are explicitly unavailable. Ranged accurate/rapid/
  longrange map 0/1/2. Candidate weapons use the same captured button mapping;
  no unobserved stance/prayer optimization is advertised.
- Only Nex Smoke/Shadow/Blood/Ice base-stat ordinary offence is offered. Minions,
  Zaros and explicitly special/minion windows are unavailable. Unknown ordinary
  windows permit a conditional base-stat scenario, never proof an attack was
  possible. MP2 Embrace darkness does not admit melee. Blood siphon, caps,
  minion armour, target healing, reflected attacks and phase throughput are not
  estimated. No attack is attributed from worn state or impact timing.
- Spec-candidate and unknown roles retain evidence and their own N3 gap. There
  is no fixed Lightbearer uplift, Death Charge proc assumption or shared energy
  allocation. Soulreaper state and enchanted-bolt HP/procs remain gaps here.
- Quiver raw ID/quantity must identify compatible positive-stock ammunition when
  the worn ammo slot does not. A non-blessed charged quiver additionally needs
  positive captured splinter stock. Blessed forms do not invent splinter upkeep.
  These public counter mappings still require N1's actual-client validation.
  Ordinary numbers are conditional on those mappings and supplied resources;
  they do not prove enough ammunition/charges for a complete kill.

## Search, ownership and shared resources

Carried searches use the selected state's worn items and positive inventory
quantities. They preserve Lightbearer and role/slot locks, real equip requirements,
known unlocks, compatible hands/ammo and complete set packages. Unknown slots do
not become empty. The old melee defender is not erased by a separate Tbow switch.
An extra inventory slot needed to unequip both weapon and defender is checked
against captured occupied slots; food/potions are never silently discarded.

Bank searches are separately labeled next-trip preparation scenarios. Observed
bank presence permits a conditional offence pair, not immediate inventory
availability, current usable charges or a validated combined packing plan.
Unknown bank/stock and unsupported alternatives remain visible limitations.
Explicit manual ownership/locks cannot create observed carried gear.

The search enumerates supported complete combinations, including sets and
accuracy/strength trade-offs. A 100,000-node work guard marks partial results;
it is not a catalogue/item quota. The valid unlocked actual package is seeded
before searching, so an interrupted search cannot manufacture a weaker baseline.
Superseded jobs stop and guarded publication rejects old identities/revisions.
Ties prefer fewer changed slots, then stable enumeration order.

The result is the best supported package **within that search and scenario**.
It is never global best owned. Unsupported competing families such as Scorching
remain named coverage gaps without suppressing an unrelated supported pair.
Independent pairs keep positive, negative and true-zero offence. Their gains
cannot be summed across states, roles or shared equipment/energy. Blood fury's
healing/sustain remains unestimated even when its offensive delta is negative.
