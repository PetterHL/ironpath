# R3b: melee families, conditions and remaining paths

14 October 2026. Review branch `r3b-melee-families`, based on updated main
`e3f02bc95ed61b1fe181e322291f8f0a63cc1fca` (#24). Plugin **0.10.0**, knowledge
**r3b-2**, catalogue schema **12**, melee **melee-2**. Preference schema 2,
powered-magic-2, ranged-1, progression-1 and the integrated UX/KI1 remain intact.
EP1/progression-2 and UX2 are separate pending assignments.

The ordinary-melee increment is preserved. An account can now compare owned
Fang, Scythe, Soulreaper, lance, Emberlight/Arclight and ordinary stab/slash/crush
bridges, inspect current endgame armour/accessories, and follow remaining
component/assembly work. This report supplements the original
[ordinary-melee evidence](R3b-ordinary-melee.md); its original fixtures/captures
and pending client checks remain historical evidence.

## Calculation support

| Family/condition | Supported ordinary reference | Boundaries |
|---|---|---|
| Fang | Stab rerolls with shared defence outside ToA and independent complete checks inside; contracted min/max damage on both stab and slash | No special; ToA examples use explicit fixed rolls, not inferred invocation/path/party scaling |
| Scythe | Charged/uncharged facts; one/two/three independently rolled hits for size 1/2/3-or-larger; separately rounded maxima and successful-zero expectation | Aggregate max is the sum of hits, not one guaranteed hit; accuracy is per hitsplat; adjacent-target geometry and overkill excluded |
| Soulreaper | Pre-attack 0–5 stacks; separately floored prayer and additive stack contributions; next-five-swing average from zero | Stack acquisition follows each attack including misses. Ramp has a typed `NEXT_FIVE_SWINGS` scope and visible mean-DPS label. No automatic timing/decay, special, healing or encounter-duration model |
| Task / Salve | Selected exact black-mask/Slayer and normal/enchanted/imbued Salve forms; Salve overrides task on undead | Eligible task is an explicit assumption; no live task inference, charged-mask defence proc, Void or arbitrary cosmetic equivalence |
| Dragon / demon | Lance's ordinary dragon effect; Arclight/Emberlight effectiveness, including Duke's reduced effectiveness | Creature/phase facts are reviewed per target; no vampyre immunity, general demon/dragon simulation or specialist special attack |
| Inquisitor | Current head 0.5%, body 1%, legs 1% crush accuracy/damage; current mace facts | No old extra full-set or mace-only multiplier; no crush effect on slash/stab |
| Owned packages | Effect-aware stat frontier, real weapon styles, one-/two-handed compatibility and separately checked unlocks | Unknown contributions stay unknown. Unsupported observed multi-hit/set/target families withhold DPS superiority while valid reference metrics remain |
| Defence scenarios | Explicit lower target Defence shared by both packages; changing target resets the override | No presumed landed spec, team ownership, defence floor or automatic raid scaling |

July's implemented changes are used: Soulreaper stacks no longer drain HP, and
Inquisitor's former conditional mace bonus moved into current offensive stats.
The [official update](https://secure.runescape.com/m=news/summer-sweep-up-gear--pvm-changes?oldschool=1)
was retrieved and reviewed alongside current exact-form Wiki revisions.

Targets retain neutral ordinary, Dharok and Graardor and add post-quest ordinary
Vorkath and awake normal Duke. The authored neutral target now explicitly has
size 1. Olm left-claw and Akkha/Ba-Ba examples are labelled **fixed-input
scenarios**. They do not certify a team, raid level, method, room/phase timing,
or full-fight performance. The generic unsupported raid profiles remain gaps.

Target/task/prayer/boost/soul/Defence changes reconsider the strongest supported
owned baseline. Explicit slot/style corrections and minimal exclusions remain
focused references. An unknown original off-hand cannot become an empty
one-handed alternative after a two-handed proposal. Source observations,
preferences, confirmations, character/profile changes and stale actions retain
the existing invalidation/identity guards. References stay in memory only.

## Value and acquisition

Twenty-five new outcomes are a reviewed delivery count, not a catalogue limit:
Fang, Scythe, Soulreaper, lance, Emberlight, Inquisitor's mace; three Torva,
three Oathplate and three Inquisitor pieces; Ultor/Bellator; torture/rancour;
Avernic defender, ferocious gloves, primordial boots, faceguard; the primordial
addition to existing treads; and blood fury's independent sustain role.
The original tentacle outcome/path is retained.

Full supported references include reviewed rapier/Blade, hasta/spear,
bludgeon/dagger, zombie axe/dragon scimitar, all eight treads forms, selected
Slayer/Salve forms, and charged/uncharged/cosmetic endgame forms. Recognition,
comparison and acquisition remain separate: baseline items do not automatically
gain a new acquisition recommendation. Finished reviewed cosmetics suppress
duplicate acquisition without proving equip unlocks or remaining charges.

Owned intermediate nodes cut only their upstream work. Reviewed source-stage
metadata lets the selected recipe distinguish remaining reward acquisition from
bounded resources/assembly. Partial batch inputs use the largest observed output
count; unread batch stock remains unknown. Examples include:

- Claw/hasta and leather bypass fresh Hydra access/task work; lance training and
  Lithkren/DS2 assembly remain independent.
- Torva uses the owned damaged piece plus the correct one/two Bandosian
  components and 90 Smithing. Dismantling useful Bandos is a resource choice.
- Each Oathplate piece has a direct-drop **OR** production path: nine plates,
  450 shards/2,520 crushed shale when making all plates, and 83 Smithing. Eight
  observed plates reduce production inputs to 50 shards/280 shale; other storage
  may further shorten that conservative planning amount.
- Soulreaper uses four distinct boss parts and 2,000 blood runes at unboosted
  75 Magic. Already owned parts do not require another boss drop.
- Torture uses the retained staged zenyte route with its own amulet mould and
  98 Crafting. Strung/unstrung amulets bypass appropriate earlier work; the
  93-Magic enchantment and rune requirements remain separate. Rancour requires
  the fang/torture and 86 Crafting, without an invented acid-etching step.
- Only the missing primordial treads addition is offered when treads are owned.
  It uses primordial boots and 4,000 tears at 80 Magic/60 Runecraft; other boot
  additions are not prerequisites. Equip and stock remain separate checks.
- Blood fury keeps supported lower ordinary offence beside independent healing
  utility. Owned fury/shard bypass their own source work. The reviewed Darkmeyer
  method distinguishes compatible Sentinel weapons from 82-Thieving
  pickpocketing; an ordinary melee kit does not establish Vyre readiness.

Unknown quantities remain unknown; manual ownership is not a supply/charge
measurement. Shared blood, synapse/infusion, zenyte, chromium, Bandos/Oathplate
materials and boot/tear allocations are explicit qualitative conflicts. No new
probabilities, pace, acquisition hours, healing amounts, personal seconds or trip
counts are invented. Independent sustain utility can lead the card while Value
retains the negative ordinary comparison.

## Sources, rights and validation

[Source retrieval metadata](../research/r3b-family-sources.json) pins actual direct
API revision/content retrieval, dates and SHA-256 hashes, plus the retrieved
official update. The bundled register links item/model/route/context/role/effort
dependencies in both directions. Numerical facts and original summaries are
included; Wiki prose/tables/Lua/images and upstream calculator implementation or
equipment data remain outside the repository. Original Java remains BSD work.

The separate development-only comparator stays pinned to
`89c3e25b344aea90d0189746e4b5f73dde0f0383`. The original
[family harness](r3b-family-oracle.test.ts) generated **15,936** complete-package
cases, covering real styles, four armour packages, prayer/boosts, Salve/task,
dragon/Duke effects, three lower/unreduced Olm scenarios, ToA/off-ToA and all
six fixed soul states. Java matches max hit, attack/defence rolls, hit chance and
DPS; the original **3,168** melee and **1,944** ranged cases remain unchanged.
The pinned comparator also cross-checks all **145** supported exact-form melee
offensive facts/weapon intervals/hands. It is not a runtime dependency.

Hand assertions independently enumerate small uniform Fang rolls, check Scythe
rounding/expectation by size, prayer/stack floors and ramp, current Inquisitor
pieces, Salve/task exclusion and dragon/demon/Defence conditions. Account cases
exercise context-sensitive owned selection, preserved corrections, incompatible
and unknown off-hands, equip locks, owned component bypasses, skips, nearly
complete/cosmetic ownership, independent blood-fury utility and existing treads.
Malformed family/effect/slot/target/evidence records fail closed.

Native tests cover Value/Path, actual selected-weapon styles, task/soul/Defence
controls, new typed ramp copy and independent reference/lifecycle guards.
The [developed-account matrix](r3b-family-accounts.md) records synthetic
context-dependent choices. UI captures use 225 logical pixels and deliberate
sprite fallbacks, not an authenticated client. Verification results and inspected
captures are recorded below.

Temurin **11.0.32.1+1**, Gradle **8.10**, resolved RuneLite **1.13.1**:
`./gradlew test build --no-daemon --offline` passes **309 tests, zero failures,
errors or skips** (287-test integrated baseline plus 22 new tests). The normal
online build was exercised during setup; offline verification uses the same
resolved dependency cache, not stubs. `git diff --check` passes.

Development checks corrected source/recipe evidence links and an obsolete test
that stopped adding duplicates at 64 entries. Account/UI regressions caught an
unknown off-hand becoming empty during owned-package reconsideration, an
off-hand candidate disappearing from a two-handed-only package, out-of-sync
stored controls, and repeat-grind labels on owned assembly inputs. The final
tests preserve these refusals/corrections and check black-mask combat access and
partial Oathplate materials; no comparison formula was weakened to satisfy a
wrong fixture.

Inspected 225px Swing captures show wrapped names/qualifiers, independent
references, typed ramp copy and separate resource/assembly work. They retain
the existing detail-heavy UI; UX2 owns its broader simplification.

| Synthetic native state | Capture |
|---|---|
| Soulreaper starting reference | [Viewport](../images/r3b-families/soul-start-viewport.png) |
| Explicit next-five-swing offence, max and per-hit accuracy | [Complete Value view](../images/r3b-families/soul-ramp.png) |
| Missing distinct boss components and assembly | [Path viewport](../images/r3b-families/soul-path-viewport.png) |
| Existing treads/boots, bounded resources and assembly | [Path viewport](../images/r3b-families/treads-primordial-path-viewport.png) |

## Pending actual-client acceptance and remaining coverage

Real ItemManager images/asynchronous loading, keyboard/focus, 100/125/150/200%
OS/client scaling, actual bank/count/quest/profile refresh and player usefulness
remain pending. New training/boss-kill/source-method confirmations are explicit
planning checks, not newly validated live adapters. Prior integration/KI1/R1/R2/
R3a live checks retain their status.

Void/Barrows/obsidian and further set conditions, additional exact variants,
dual-hit/Hallowfell geometry, reductions/floors and automatic raid scaling,
specials, poison/venom/healing, supply duration and whole encounter/trip metrics
remain explicit coverage gaps. The inventory is uncapped and R4–R8/KI2–KI3 stay
open. This is reviewable R3b family/condition support, not comprehensive endgame
coverage, completed real-client acceptance or Plugin Hub publication.
