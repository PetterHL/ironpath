# R2 first ranged-family models

13 October 2026. `r2-ranged-models` starts from integrated main `f495706` while R1.5a main integration is pending. This is a reviewable start on R2's pure family models. It does not complete R2's user-facing assignment. Plugin 0.5.0 / knowledge r1-1 / catalogue schema 6 remain the base branch's runtime versions. R1.5a and the neutral-default/DPS-first follow-up remain on PR #12's branch, which includes merged PR #13. Their integration and actual-client checks are tracked separately.

## Implemented arithmetic

Original Java `ranged-family-1` calculates ordinary supplied attacks for the modern active crystal bow, Bowfa and twisted bow. Inputs explicitly declare real/visible Ranged, style, prayer, complete additional gear bonuses, compatible ammunition, usable weapon and active crystal pieces. Null bonus/level/target inputs stay unknown. These scenarios are assumptions supplied to a pure function, not observed ownership or equip/readiness certification.

- Intrinsic attack/strength/base interval: crystal bow 100/78/5, Bowfa 128/106/5, tbow 70/20/6. Rapid removes one tick; Accurate adds three to both effective levels; Longrange gives neither damage/accuracy boost.
- Gear bonuses exclude weapon/arrow contributions. Tbow requires a supplied ordinary-arrow strength; crystal bows require declared no arrows with zero arrow strength. Seeker effects and incompatible or unknown ammo are explicit gaps. Complete slot/equip/charge/ammo eligibility must be enforced by the future observation/reference adapter.
- Prayer rounding occurs separately for accuracy and strength before style/+8: Eagle Eye 15/15%, Deadeye 18/18%, Rigour 20/23%. The enum records an assumed prayer; it cannot establish an unlock or active prayer.
- Base max hit uses integer stages of `(effectiveStrength × (64 + totalStrength) + 320) / 640`. Active helm/body/legs contribute weights 1/3/2: accuracy multiplier `(20 + sum)/20`, damage `(40 + sum)/40`. Apply the bonuses to crystal bow/Bowfa only. Inactive pieces supply no special modifier; explicit ordinary gear totals can still include their flat stats. A synthetic flat-strength armour replacement loses to full crystal for Bowfa in the hand case; this is not a universal equipment claim or Masori promotion.
- Standard ranged defence uses explicit Defence and the **standard ranged defence** bonus. All three families are standard projectiles. Magic-defence bonus, offensive Magic bonus and standard ranged defence are separate quantities.
- Tbow uses the larger target Magic level/offensive Magic bonus, capped at 250 outside CoX or 350 in CoX. Its integer curve stages are tested independently from ordinary hit chance. CoX changes only that cap here; final scaled target stats must already be explicitly supplied. No raid invocation/party-size/HP/phase is generated. No encounter-specific exceptions are assumed.
- `AttackMath` extracts only IronPath's existing ordinary inclusive-roll and successful-zero mean arithmetic. PoweredMagic delegates to the same arithmetic. DPS combines hit chance, successful-hit mean and actual interval; max hit is not divided directly by attack speed.

The current result withholds the whole narrowly defined scenario for unmodelled effects, unknown/incompatible inputs, unconfigured scaling or a nonordinary damage distribution. Runtime per-metric fallback and candidate comparisons remain the existing shared comparison contract and the next R2 integration task. Void/slayer/salve, charged-quiver effects, Seeker floors, flat-armour/phase exceptions, raid scaling, specs, crossbows/bolts, blowpipes and other families remain model gaps. Zero-max-damage distributions are explicitly unsupported in this first increment.

## Source review

The following pages were retrieved, pinned and reviewed on **13 October 2026**. [Original retrieval manifest](r2-source-review.json) records exact URLs/revisions/response hashes. Raw HTML stays in `/tmp/ironpath-r2-source-review`; no page text, images or upstream datasets are bundled. Wiki factual stats/formulas are used as evidence; explanations, Java and synthetic fixtures are original. No calculator implementation is copied into BSD work.

| Reviewed page | Pinned revision |
|---|---|
| [Damage_per_second/Ranged](https://oldschool.runescape.wiki/w/Damage_per_second/Ranged?oldid=14906132) | 14906132 |
| [Crystal_bow](https://oldschool.runescape.wiki/w/Crystal_bow?oldid=15320290) | 15320290 |
| [Eagle_Eye](https://oldschool.runescape.wiki/w/Eagle_Eye?oldid=15370489) | 15370489 |
| [Deadeye](https://oldschool.runescape.wiki/w/Deadeye?oldid=15222979) | 15222979 |
| [Maximum_ranged_hit](https://oldschool.runescape.wiki/w/Maximum_ranged_hit?oldid=15199053) | 15199053 |
| [Ranged](https://oldschool.runescape.wiki/w/Ranged?oldid=15337465) | 15337465 |
| [Rigour](https://oldschool.runescape.wiki/w/Rigour?oldid=14954629) | 14954629 |
| [Masori_armour](https://oldschool.runescape.wiki/w/Masori_armour?oldid=15236176) | 15236176 |
| [Twisted_bow](https://oldschool.runescape.wiki/w/Twisted_bow?oldid=15363157) | 15363157 |
| [Crystal_armour](https://oldschool.runescape.wiki/w/Crystal_armour?oldid=15355197) | 15355197 |
| [Bow_of_faerdhinen](https://oldschool.runescape.wiki/w/Bow_of_faerdhinen?oldid=15366535) | 15366535 |

`Ranged_accuracy` redirected to equipment stats and was excluded from formula evidence. Masori's general set facts were reviewed to inform the later full item/route audit; exact per-piece data is not activated here. Existing current equipped forms, charge states and observation APIs still require audit before this model can supply personal recommendations.

## Validation and investigated disagreement

The original [harness](r2-family-oracle.test.ts) runs in the separate GPL development checkout at `89c3e25b344aea90d0189746e4b5f73dde0f0383`. Its synthetic additional gear bonuses are explicitly +50 attack/+10 strength, supplied ordinary-arrow strength 60 for tbow, and declared active crystal combinations. External data remains in the temporary reference checkout; fixture rows contain original inputs and generated arithmetic outputs only.

**1,152 cases:** Ranged 85/99 × three families × Accurate/Rapid/Longrange × None/Eagle Eye/Deadeye/Rigour × four explicit final target scenarios × no crystal/helm/body+legs/full crystal. Targets are neutral 0/0 defence, 150 Defence/75 standard bonus with highest offensive Magic 200, 250/200 defence with Magic 500 outside CoX, and the same explicit final stats with the CoX cap. Monster scaling is disabled in the reference to avoid changing the authored final inputs. No NPC, encounter time or player data is represented.

The initial crystal-bow harness used historical ID **4212**; that historical equipment record does not exercise current crystal armour synergy. The Wiki's current active form is **23983**. Corrected the original harness and regenerated outputs without changing Java arithmetic. This is a fixture-identity correction, not evidence that current crystal synergy is absent. All cases now agree with [r2-family-oracle.csv](../../src/test/resources/com/ironpath/r2-family-oracle.csv): whole max hits and integer rolls exactly; accuracy within `1e-12`, DPS within `1e-10`.

Nine meaningful tests additionally cover independent hand derivations, integer breakpoints/caps, larger max hit losing to a faster style, flat-strength armour versus crystal in an explicit package, tbow's context-dependent advantage, incompatible ammo, inactive/unknown/usability inputs and unsupported target/effects. Hand example: 99 Ranged, Rigour, Rapid Bowfa, +50 attack/+10 strength and full active crystal yields effective accuracy 126 and strength 129, attack roll `126 × 242 × 26/20 = 39639` after truncation, max hit `floor(36 × 46/40) = 41`, four ticks, and neutral defence 576. Hit chance is `1 − 578/79280`; DPS is that chance times `(41/2 + 1/42)/2.4`. Tbow on neutral has 54% damage and 40% accuracy curves in the hand case; outside/inside caps give different high-Magic damage.

`source /workspace/ironpath-environment/activate.sh && ./gradlew test build --no-daemon` passes **133 tests, zero failures/errors/skips** on this R1-based branch, using Temurin 11.0.32.1 / RuneLite 1.13.1. This includes the retained 234 P4a and 540 R1 powered-magic rows through the extracted shared arithmetic. The already delivered neutral/R1.5a branch has its separate 163-test report; these counts describe distinct branch states and are not added into a claimed integration pass. `git diff --check` passes. Non-fatal Fontconfig/Gradle warnings remain. No new UI or game-client result is claimed.

## Continue R2

This model is dormant until full immutable ranged setup/target/reference contracts, observed usable variants, ammo/slot/charge compatibility, strongest owned alternatives, per-metric comparisons, complete reviewed routes and the existing DPS-first native views are integrated. Use the neutral reference and explicit specialist targets after the R1.5a/PR #13 integration; keep effort/category ranking and identity guards. Then extend the remaining R2 families and accessory/prayer coverage. Follow TASKS's comprehensive acceptance criteria, synthetic post-Bowfa/tbow-owned recommendation cases and actual-client checks. R2 remains **in progress**, with inventory promotion and client/usefulness checks pending.
