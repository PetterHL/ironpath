# Neutral reference and DPS-first weapons

13 October 2026. Branch `reference-neutral-dps`, based on R1.5a PR #12 at `dbbd51592259ebaadedb0372a549de28244293dd`. This focused follow-up depends on its native Browse/value views and Ancient blessing correction; integration remains pending. Plugin **0.6.1**, knowledge **reference-1**, catalogue schema **8**. Combat **powered-magic-2**, ranking **pursuit-1**, acquisition **zenyte-fixed-1** and preference schema **2** remain.

## Delivered behaviour and boundaries

Automatic proposals use **Enemy with 0 Defence**, without a goal or setup wizard. This authored analytical target explicitly has Defence 0, Magic 0 and magic-defence bonus 0, ordinary standard powered-magic rolls, and no special target effects. Defence roll is `(0+9) × (0+64) = 576`. Accuracy is below 100%; successful damage is distributed normally. No NPC ID, HP, size, other defensive stats or raid scaling is asserted. Boss profiles remain explicit alternatives; ToA remains an unavailable scaling context. The analytical reference does not count as another useful encounter in ranking breadth.

Weapon overview/Browse/Value show supported theoretical DPS before → after with absolute delta first. Details expose max hit, hit chance and actual before/after attack interval. Accessories/armour with unchanged interval retain max hit first. The result contract also selects DPS first for interval-changing effects. Full weapon replacement removes incompatible off-hands. Unsupported history accuracy/DPS or unavailable inputs produce a primary DPS model gap; supported max hit remains secondary. No formula, effort/category sorting or persistence change is introduced.

A larger max hit can lose to a faster weapon on this target. At 99 Magic in a weak damage kit, swamp can beat Shadow's ordinary DPS; Shadow stays discoverable with contextual limits and may be deferred by the existing ranker. Stronger damage gear or an explicit high-defence target changes the comparison. Shadow ownership still suppresses another swamp acquisition route even when the neutral reference uses the faster staff. A neutral gain cannot establish universal boss usefulness.

## Verification

`source /workspace/ironpath-environment/activate.sh && ./gradlew test build --no-daemon` passes **163 tests, zero failures/errors/skips**, using Temurin 11.0.32.1 and RuneLite 1.13.1. `git diff --check` passes. Non-fatal Fontconfig and Gradle deprecation warnings remain. Existing 234 P4a and 540 R1 oracle rows retain their explicit boss contexts and numeric assertions. R1/R1.5a synthetic high-defence regressions now declare their original target rather than depending on automatic target selection; package selection occurs for that target. Historical reports/captures are unchanged.

The new [original harness](neutral-oracle.test.ts) was run in a separate development checkout of the GPL calculator at `89c3e25b344aea90d0189746e4b5f73dde0f0383`. **180 original cases** in [neutral-oracle.csv](../../src/test/resources/com/ironpath/neutral-oracle.csv) agree: whole max hit and integer attack/defence rolls exactly, accuracy within `1e-12`, DPS within `1e-10`. Matrix: 85/90/99 Magic × five staves × Ahrim/Virtus/Ancestral packages × Accurate/Longrange × no boost or Augury/heart. Ordinary custom-target inputs are fixed to zero Defence/Magic/magic-defence bonus; unused reference-tool HP/size defaults have no effect on these attacks and are not runtime claims. External dependencies stayed in `/tmp`; no upstream implementation or datasets enter this plugin.

Independent hand cases check the 90-Magic seas roll `101 × 79 = 7979`, defence 576, chance `1 − 578/15960`, successful-hit mean `326/26` and four-tick DPS `chance × (326/26)/2.4`. Equal-max-hit seas and Eye cases have different three/four-tick DPS. A larger-max-hit but slower Shadow case loses DPS. Regressions also cover complete staff/off-hand replacement, strongest owned package selection, unchanged-speed accessories, unsupported one-handed Confliction history, missing reference slots, invalid analytical inputs, explicit boss selection and unconfigured ToA. [Six fresh neutral account profiles](neutral-scenarios.md) record deterministic no-goal advice without claiming player usefulness.

## Native component evidence

Fresh headless Swing captures at 225 logical pixels use the real panel and fallback sprite wells. Full names and before/after DPS fit in two columns using taller value rows; long names/enlarged fonts still trigger one column without shrinking text. Existing width/font/navigation/identity tests remain. These images do not establish native sprites, DPI scaling or an in-game pass.

| State | Fresh capture |
|---|---|
| Neutral overview | [Overview](../images/neutral/runtime/overview.png) |
| Two-column Browse with weapon DPS and accessory max hit | [Browse](../images/neutral/runtime/browse.png) |
| DPS-first weapon Value and 4→5 tick interval | [Weapon](../images/neutral/runtime/weapon-value.png) |
| Max-hit-first unchanged-speed armour | [Armour](../images/neutral/runtime/armour-value.png) |
| Primary DPS history gap, secondary supported max hit | [History gap](../images/neutral/runtime/history-gap.png) |

## PC retest — pending

Close the development client, fetch and check out `reference-neutral-dps`, then use README's JDK 11 build/run commands. Observe equipment/bank and leave No goal. Check the neutral target label on overview, Browse and Value; weapon DPS precedes max hit while armour/jewellery retain max hit first. Compare the displayed ticks/hit chance, and verify Shadow removes the off-hand. Change to a reviewed boss context and inspect its different numbers; ToA must remain a gap. Unknown slots/history must not become zero DPS gains. Check full names, keyboard/Back/scroll and real sprites at the retained 100/125/150/200% scaling matrix. Record usefulness and any disagreement separately from arithmetic agreement. All these client checks and older pending lifecycle checks remain **pending**.
