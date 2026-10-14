# R3b: ordinary melee and the abyssal-tentacle path

14 October 2026. Review branch `r3b-standard-melee`, based on main
`2dd799a5687276fd4ad7cff868c38794232fa7b5`. Plugin **0.9.0**, knowledge **r3b-1**,
catalogue schema **11**, melee **melee-1**; powered-magic-2, ranged-1,
progression-1 and preference schema 2 are unchanged. This is the first R3b
increment; the full family/condition acceptance remains open in TASKS.

An account with a supported whip setup can inspect a complete ordinary tentacle
comparison and its remaining acquisition/assembly work. The synthetic 90 Attack /
90 Strength example below changes neutral ordinary DPS **6.17 → 6.38**, max hit
**30 → 31**, and hit chance **98.5% → 98.6%**. It retains the owned defender and
uses no offensive prayer/boost, Accurate slash and a deliberately empty ammo slot.
The comparison is conditional reference offence, not measured performance,
poison-inclusive weapon damage, a universal next grind or a kill-time estimate.

## Delivered support and boundaries

| Capability | Implemented in this increment | Remaining gap |
|---|---|---|
| Ordinary melee | Separately floored Attack/Strength, actual attack type/stance, attack/defence rolls, successful-zero correction, max hit, hit chance, attack interval and theoretical DPS | No special, poison, incoming damage, healing, kill-time or trip model |
| Supported weapons | Whip and lava/ice cosmetics; tentacle and ornament; dragon sword; Sarachnis cudgel; dragon 2h sword; rune scimitar | Other bridges and endgame families need reviewed modules/records |
| Complete packages | Eleven explicit slots; strongest supported observed owned one-/two-handed packages; accuracy/strength Pareto search and actual style enumeration | No unreviewed set or creature modifiers; no claim against unmodelled competitors |
| Armour/reference facts | Neitiznot helm/faceguard, torso/Bandos, fury/blood fury/torture/rancour, dragon/Avernic defenders, Barrows/ferocious gloves, dragon/primordial boots, berserker(i)/warrior(i)/brimstone/Lightbearer, fire/infernal/mythical capes | These are supported stat references, not separate new acquisition recommendations; Torva/Oathplate/Inquisitor, Ultor/Bellator, treads and other variants remain pending |
| Targets | Authored Enemy with 0 Defence; ordinary Dharok; ordinary Graardor, with distinct stab/slash/crush defensive bonuses | No raid scaling, reductions, target/task/Salve or demon/dragon multipliers |
| Assumptions | No prayer/boost by default; paired 15% prayers, Chivalry/Piety with checked unlock/levels; full super attack + strength; focused target/style/slot correction | Piety/Chivalry unlock remains a dated explicit planning confirmation; no live prayer, potion, task, charge or duration inference |
| Unknowns | Unknown/unreviewed slot contributions block dependent metrics; manual ownership cannot supply a baseline; observed Fang/Scythe/Soulreaper/lance competitors withhold DPS superiority | Recognition is explicitly scoped to reviewed known forms; remaining inventory coverage is open |
| Sustained utility | Blood fury has its ordinary offensive facts; text preserves sustain, charges and Lightbearer energy as separate dimensions | No numerical healing/energy/trip gain and no new sustain recommendation policy |
| Tentacle path | Owned whip/component bypasses, drop access/task check, irreversible assembly and whip upkeep; both reviewed finished forms suppress acquisition | No odds, pace/hours, or new multi-resource optimiser |

The neutral melee roll is `(0 + 9) × (0 + 64) = 576`, not zero. Ordinary Dharok
uses Defence **100**, stab/slash/crush **252/250/244**; Graardor uses Defence
**250**, **90/90/90**. HP, size, phase transitions and reductions are unused.

Package proposals examine all supported observed weapons/styles and non-dominated
accuracy/strength totals. This avoids picking a two-handed weapon from max hit
alone or picking every accessory by strength alone. Candidate packages preserve
unrelated corrected reference slots; a one-handed candidate can restore an
observed off-hand when replacing a two-handed baseline. They preserve stance where
the candidate permits it, otherwise disclose a compatible proposed style. A removed two-handed off-hand cannot silently become zero for a later one-handed candidate when its original contribution is unreviewed. Unconfirmed quest-bound alternatives are excluded before dominance pruning so they cannot erase a usable weaker piece. All
slot/style/ownership/equip facts are revalidated. Explicit exclusion is distinct
from an unknown contribution.

Melee controls are independent of magic and ranged. All three references remain
session-only and share existing identity/generation/revision guards. Changed
levels, containers, slot evidence, quantities, facts or manual confirmations
invalidate melee confirmation. Character/profile/logout clear it; stale actions
cannot alter another account. No account/reference persistence or telemetry was
added. New manual planning keys use the existing explicit preference format.

Neitiznot helm/faceguard equip quests and mythical-cape Dragon Slayer II completion
are checked independently of ownership. The two Fremennik quests use public
`Quest.getState`, with their public progress IDs in the existing coalesced refresh;
false/unavailable quest evidence cannot be replaced by a positive ownership fact.
Actual-client observation of those refreshes remains pending.

## Route and account behaviour

A whip and kraken tentacle already owned remove their own drop stages. In particular,
assembly does not require 87 Slayer. A missing whip retains the 85-Slayer abyssal-
demon work and a boost/access check; a missing tentacle retains **87 Slayer,
50 Magic and a cave-kraken assignment**, using an appropriate magic method.
The task is an explicit planning check, never inferred from levels or weapons;
recheck dated manual confirmations when the assignment changes.

Assembly is irreversible. After **10,000 attacks including misses**, the whip is
consumed and the kraken tentacle remains. The component also competes with the
ten-tentacle enhanced-trident option. Owned inputs shorten remaining work without
turning later upkeep or competing uses into free resources. Skipped acquisition
removes the route; finished tentacle ownership removes acquisition. Every active
outcome owned yields an honest empty acquisition list.

Known unmodelled melee competitors retain valid selected-reference max hit and
hit chance while withholding DPS superiority and ranking on that gain. Unreviewed
equipped Salve/Slayer/set gear is a contribution gap rather than a zero bonus. This
is conservative partial coverage; condition/family work must make those common
setups useful in later R3b increments.

## Sources, independent validation and rights

The bundled `r3b-*` register records actual direct Wiki API revision/content
retrieval on 14 October, with review/access dates and reverse item/model/route/
context dependencies. Equipment stats and exact forms, actual weapon styles,
ordinary arithmetic, prayer/boost conditions and both acquisition stages were
reviewed. Style pairs use factual metadata from
[CombatStyles revision 15299810](https://oldschool.runescape.wiki/w/Module%3ACombatStyles?oldid=15299810).
No source prose, Lua, source tables or images are redistributed.

The independently authored Java implementation agrees on max hit, attack/defence
rolls, hit chance and DPS across **3,168 original complete-package cases** against
[calculator commit 89c3e25b344aea90d0189746e4b5f73dde0f0383](https://github.com/weirdgloop/osrs-dps-calc/tree/89c3e25b344aea90d0189746e4b5f73dde0f0383).
The separate pinned checkout also cross-checked all **35** reviewed stat forms and
both ordinary NPC defensive profiles. GPL calculator code and its equipment data
remain outside the plugin repository. Only the original
[harness](r3b-oracle.test.ts) and synthetic
[CSV](../../src/test/resources/com/ironpath/r3b-oracle.csv) are included. The matrix
covers Attack/Strength **75/85, 90/90, 99/99**, all 22 actual styles across six base
weapons, two complete armour packages, four prayer scenarios, boost/no boost and
three targets. Example invocation in that checkout:

```sh
IRONPATH_CATALOG=/path/to/ironpath/src/main/resources/com/ironpath/catalog.json \
  node .yarn/releases/yarn-4.9.2.cjs jest src/tests/r3b-ironpath.test.ts --runInBand --coverage=false
```

Initial development checks caught tentative wrong Dharok defensive inputs in the
harness and wrong Emir's Arena ring IDs in the draft records. The final inputs
use the reviewed Wiki facts above and **26770/26769** for those rings; all checks
were rerun after correction. No formula was changed to match the wrong inputs.

Temurin **11.0.32.1+1**, Gradle **8.10**, resolved RuneLite **1.13.1**:
`./gradlew test build --no-daemon` passes **257 tests, zero failures/errors/skips**
(240-test integrated baseline plus 17 new tests). Existing magic and **1,944 ranged**
oracle cases remain unchanged. Hand-derived tests check integer boosts/rounding,
successful zero, defensive types and incompatible hands. Account tests cover
DPS-versus-max-hit ordering, accuracy/strength alternatives, preserved corrections,
unknown/manual gear, unsupported families, equip unlocks, owned components, skips,
all-owned results and separate blood-fury utility. Malformed facts/styles/targets/
evidence fail closed. Native tests exercise controls, direct Value/Path, labels,
confirmation invalidation and stale identity/revision rejection. Two historical
R1/R1.5 advice fixtures explicitly retain their original family scope.

## Native component evidence and pending client checks

All captures use original synthetic account facts, **225 logical pixels**, native
Swing and deliberate sprite fallbacks. They are not an authenticated RuneLite or
ItemManager image test. Inspected captures show readable wrapped names, DPS-first
weapon value, an independent off-hand, collapsed correction controls, route work
and a per-metric unmodelled-family gap. Visual inspection corrected the inherited
combined “DPS and hit chance gap” label when only DPS is withheld.

| State | Capture |
|---|---|
| Ordinary account, one supported melee pursuit | [Overview](../images/r3b/overview.png) |
| Complete whip → tentacle reference | [Value](../images/r3b/value.png) |
| Remaining acquisition and upkeep | [Path](../images/r3b/path.png) |
| Explicit recipe and dependency checks | [Expanded recipe](../images/r3b/path-expanded.png) |
| Fang observed, DPS superiority withheld | [Per-metric family gap](../images/r3b/family-gap.png) |

**Pending actual-client acceptance:** real sprites/async image loading, keyboard
focus and controls, ordinary/100/125/150/200% scaling, observed bank/quantity and
quest-progress refresh, character/profile transitions, and player comprehension
of which comparison and remaining acquisition work is shown. Prior R1/R2/R3a live
checks retain their original pending status. No full R3b, comprehensive catalogue,
Kill Impact, personal-seconds, trip or Plugin Hub publication claim is made.
