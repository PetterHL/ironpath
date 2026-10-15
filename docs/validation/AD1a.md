# AD1a — DPS-first comparisons and calculation coverage

14 October 2026 · implementation candidate · plugin **0.12.0**, knowledge **ad1a-1**, catalogue **13**, powered magic **powered-magic-3**. Ranged/melee, progression-2, activity-effort-1 and preference schema 2 are unchanged.

This branch stacks on **UX2 #26 at `54d9c239`**, which supplies the typed card/detail flow; UX2 depends on **EP1 #25 at `fafb745`**. The assignment comes from documentation **#27 at `b2eb064`**. None of these PRs is merged or accepted by this implementation. The AD1 section in TASKS retains the original requirements and the remaining AD1b/AD1c work.

## Result

Ordinary offensive upgrades show supported relative theoretical DPS first, including rings, gloves and armour. Details show absolute before/after/delta and secondary max hit/hit chance. A zero baseline uses absolute change without division. True zero, losses and unavailable metrics remain distinct; a nonzero change below display precision says “<0.1% DPS gain/loss” and “gain/loss <0.01” rather than a rounded zero. Reviewed specialist utility can still lead. Changing the displayed metric does not change the domain classification used by progression-2's encounter-value rule.

Brimstone now uses the independently calculated per-attack mixture: three quarters ordinary accuracy, one quarter accuracy against floor(9 × ordinary defence roll / 10). Apply it separately to both setups; replacing the ring removes the effect. It changes accuracy/DPS, not maximum damage, Magic level or persistent target state. Confliction's one-handed miss history still withholds dependent accuracy/DPS, including when combined with Brimstone. Two-handed Shadow retains Confliction's supported flat bonuses.

Magic reference selection now compares observed ring alternatives jointly with each supported staff/off-hand package. Flat ring accuracy alone cannot decide between Seers and Brimstone. Unknown/unreviewed contributions stay gaps and manual ownership does not establish usability.

Multi-slot comparisons say **Setup change** and name the actual weapon baseline. Details enumerate every changed slot, removed off-hand, ammunition-slot change and internally loaded darts. A buckler-supported crossbow package versus a blowpipe is not presented as a shield replacing a weapon. Negative selected-reference results remain visible even if a separately labelled encounter has positive specialist value.

## Reported account evidence and limits

The complete reported Magus/Brimstone and buckler/Blazing setups are **not present in the repository or #27 discussion**. They were requested during implementation and have not been supplied. No full player-account reproduction or resolution is claimed.

The earlier [client-variant](R3a-client-variants.md) and [Lightbearer](R3a-lightbearer.md) reports establish reported black crystal, loaded Blazing, max treads, Barrows gloves and Lightbearer, with explicitly incomplete levels/bank/ammunition evidence. `bucklerFixture()` reuses those **reported gear classes**, then explicitly supplies synthetic 99 Ranged, other fixture levels, observed dragon darts, Rune crossbow and runite bolts, no prayer/boost and the neutral reference. It yields **8.25 → 5.29 DPS (−2.96, −35.9%)**, while max hit rises **20 → 32**. This demonstrates why maximum hit alone misleads; these are not Petter's numbers.

`brimstoneFixture()` is a contrasting original synthetic account: 99 Magic, charged swamp trident, Brimstone, tormented, occult, Ahrim hood/top/skirt and Ancient blessing, other slots explicitly excluded by the legacy empty-equipment fixture, Duke kill confirmed, neutral target, no prayer/boost. The small Magus accuracy gain is supported even without a max-hit breakpoint. It is **not** a replacement for the missing player setup. The retained R1 fixtures separately exercise robes, prayers, boosts, shields and non-neutral targets.

## Complete active-outcome metric audit

All **29 active outcomes** are exercised through the actual Advisor and typed presentation across **12 synthetic profiles** (11 yield acquisition cards; the unread-bank profile deliberately yields no acquisition cards). The [evaluated matrix](AD1a-metric-coverage.md) publishes each emitted outcome, fixture, displayed result/context, actual baseline and exact refusal/gap. Owned/ineligible outcomes are naturally omitted per profile; every active outcome occurs in at least one evaluated profile. The withdrawn Rosewood outcome remains inactive.

- **25** offensive outcome contracts have ordinary DPS models: eight powered-magic, sixteen ranged and one melee. Supported ordinary results are now displayed DPS-first; none remains hidden by the old accessory max-hit rule.
- **Four** outcomes intentionally use qualitative utility without an ordinary comparison model: Zaryte crossbow special, Avernic treads acquisition, Venator bow and Scorching bow. Applicability still requires its own role/account evidence. Reference-item recognition is distinct from modelling their acquisition outcome.
- A supported primary number remains conditional on complete eligible inputs and the documented mechanic/context. The matrix includes unknown inputs, unsupported competitors and known equip/unlock refusals. Its “unsupported mechanic/context” category includes an ineligible selected context, not a claim that low level or an unconfirmed Duke/Leviathan kill is an unimplemented combat formula. Exact reasons are retained for that distinction.

| Outcome family | Offensive DPS model / display | Context boundaries and follow-up |
|---|---|---|
| Occult, swamp trident, Shadow, Ancestral hat/top/bottom, tormented, Magus | Supported complete ordinary powered setups; DPS-first, max hit secondary | Brimstone fixed here; Confliction one-handed history / spell families / ToA scaling remain below |
| Tbow, Bowfa, crystal helm/body/legs, Toxic, Masori mask/body/chaps, anguish, rupture, quiver, Venator ring, vambraces, buckler, assembler | Supported compatible ordinary ranged packages; DPS-first, including armour/rings/gloves | Special/bounce/demonbane families, proc bolts and target scaling remain below; loading/supply stays an assumption |
| Abyssal tentacle | Supported ordinary melee packages; DPS-first | Reviewed whip consumption remains separate; endgame competitors and conditional multipliers remain below |
| Zaryte crossbow, treads, Venator bow, Scorching bow | Intentional utility-led acquisition outcomes; ordinary outcome DPS not fabricated | Qualitative relevance and honest numeric boundaries survive; this is not hidden supported DPS |

## Outstanding blockers and ownership

This is a coverage report, not another backlog. TASKS AD1a and the owning R assignments retain delivery/acceptance. Prioritise further model work by the comparisons it unblocks, not by item count.

| Blocker | Affected contexts / outcomes | Required model or input work | Owner |
|---|---|---|---|
| One-handed Confliction miss-history reroll | All eight magic outcomes when either setup uses one-handed Confliction; Brimstone combination also withheld | Review miss/success transitions, same spell/target resets, first attack versus steady state and joint Brimstone rolls; independent transition/distribution fixtures. It is not a generic stacking bonus. | AD1a follow-up via R1/R4; highest remaining reusable magic blocker |
| Owned Scorching (29591) or active Venator (27612) competing ranged families | All sixteen ordinary ranged outcomes can lose the strongest-owned DPS claim; unaffected reference max hit/hit chance remain | Independently implement ordinary/demonbane and single-target/bounce families with method/target eligibility; do not suppress the competitor or certify a weaker reference | AD1a follow-up via R2/R4 |
| Enchanted bolt effects | Ranged comparisons whose before/after ammunition uses a proc bolt | Current HP, caps, proc/diary/target eligibility and expected distributions; ordinary-bolt packages remain supported | R2/R4, consumed by AD1a |
| Owned Fang (26219/27246), Scythe (22325/22486), Soulreaper (28338), lance (22978) | Tentacle and future melee comparisons against those observed alternatives | Family accuracy, multi-hit/size, stack/resource or dragon predicates; retain all current named refusals | R3b/R4, consumed by AD1a |
| Unreviewed worn forms/slots, including melee treads and broader endgame armour | Corresponding style's complete comparison where a contribution has no reviewed supported alternative | Review exact form stats/conditions and add family records; never treat an unknown contribution as an empty slot | R1/R2/R3b and ongoing AD1a metric audit |
| ToA / other unreviewed phase, raid or role scaling; ancient/elemental spells, task/Salve and creature modifiers beyond supported cases | Comparisons requiring those exact mechanics | Explicit validated target/family inputs; no default raid level or neutral-to-boss inference | R1/R2/R3b/R4 |
| Zaryte energy/bolt special, Venator bounce, Scorching binding/demonbane, treads multi-style package | Four intentionally utility-led outcomes | Relevant distributions, target geometry and competing complete packages; no constant universal DPS or encounter-time multiplier | R3b/R4; ordinary gaps remain part of AD1a coverage |
| Unobserved equipment, levels, compatible ammo or loading; unconfirmed Duke/Leviathan eligibility | Exact emitted input/refusal rows, including early and partial accounts | Observe/correct the actual inputs and distinguish equip/access checks; no mechanic change can manufacture them | AD1a reported-account verification and AD1c account corrections |

Finite-target kill time, full encounter/trip performance and acquisition hours are different metrics. No time/trip model is promoted here. Automatic acquisition time remains AD1b, final priority/navigation/correction copy AD1c, and actual-client/player acceptance R8.

## Mechanic evidence and validation

The existing pinned Wiki Brimstone source (`r1-brimstone-ring`, revision **15368603**, reviewed 13 October) remains linked to item and powered-magic-3. Fresh Wiki/Jagex retrieval was denied in this session; no fresh article inspection is claimed. The current calculator main resolved to pinned development reference [89c3e25](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/PlayerVsNPCCalc.ts). Its per-attack probability, integer defence truncation and powered-staff scope were checked; response SHA-256 is recorded in [RESEARCH](../archive/pre-nex-20261015/RESEARCH.md). Only mechanic facts were used; no GPL implementation, corpus or article material was copied.

`AD1aComparisonTest` independently counts winning integer attack/defence roll pairs and mixes the two defence distributions for **30 staff/defence cases**, including zero defence, both accuracy branches and non-divisible defence rolls. It covers all five powered staff families, successful-zero damage and Sang's retained distribution. Advisor regressions cover Magus/Brimstone, proc-aware ring selection, retained Confliction refusal, negative buckler packages, all active outcomes, zero baselines, true zero and tiny signed changes. Native Swing checks exercise the actual advisor/card/detail path at **225px** with **12px and 20px** fonts. Existing arithmetic corpora and lifecycle/ownership/navigation guards remain required.

Temurin **11.0.32.1**, Gradle **8.10**, resolved RuneLite **1.13.1**: the final full `test build` passes **313 tests, zero failures/errors/skips**. The six AD1a domain tests were subsequently rerun after adding the displayed-context column to the evidence export. `git diff --check` passes. Original numeric/lifecycle assertions are retained; old accessory max-hit-first, Brimstone-gap and raw `shield:` UI expectations are explicitly migrated to the new supported behaviour. Historical TASKS/UX/RESEARCH sections and prior captures remain unchanged. Reproduction:

```sh
source /workspace/ironpath-environment/activate.sh
./gradlew test build --offline --no-daemon
```

## Component evidence and pending acceptance

Captures are implemented Swing components at **225 × 600 logical pixels**, approximately **213px inner width**, with synthetic account inputs and explicit fallback sprites. Adjacent bounds files check wrapping and horizontal fit. They are not RuneLite screenshots or desired-reference images.

| Case | Normal / enlarged font |
|---|---|
| Magus versus Brimstone, small supported DPS gain | [12px](ad1a-images/ad1a-magus-12.png) / [20px](ad1a-images/ad1a-magus-20.png) |
| Buckler crossbow setup versus Blazing, retained neutral loss | [12px](ad1a-images/ad1a-buckler-12.png) / [20px](ad1a-images/ad1a-buckler-20.png) |

Pending: obtain the complete reported player references and reproduce both magic/ranged comparisons with their actual inputs/provenance; inspect genuine sprites and OS/client scaling; verify input refresh and profile transitions in RuneLite; review specialist versus neutral values with the player. Desired visual reference images have not been supplied and visual-reference acceptance remains open under AD1c. Automated fixtures and these captures do not establish player usefulness or close AD1 joint acceptance. No merge, Hub publication, telemetry or persistence expansion is performed.
