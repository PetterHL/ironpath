# R3a client equipment correction

13 October 2026. Plugin **0.8.1**, knowledge **r3a-2**, bundled schema **10**, preference schema **2**. This follows the R3a implementation on PR #18 and retains its dependency on documentation PR #17 at `75897d5`. No PR is merged.

**Historical correction:** the report below records 0.8.1 as tested. Petter's next client check confirmed the repeated crystal/blowpipe cards were gone, but exposed unsupported Lightbearer and rejected Rosewood's acquisition role. The [0.8.2 follow-up](R3a-lightbearer.md) withdraws Rosewood from all recommendation views, fixes Lightbearer and connects specialist roles to the supported ranged weapons. The captures and 228-test count below retain their original context.

Petter supplied real-client Home, Zaryte vambraces Value and Shadow Path screenshots and reported black crystal armour, Toxic/Blazing blowpipe and Avernic treads (max). The screenshots show item sprites and exposed incorrect repeat-acquisition checks, unsupported ranged feet and confusing Rosewood discovery. They establish those specific failures, not the player's complete bank, levels, viewport/scaling settings or a successful client acceptance matrix. The reported boot ID was initially misread as 31047; the max treads ID is 31097. No ID remapping was added.

## Changes and evidence

| Finding | Correction | Boundary |
|---|---|---|
| Coloured crystal was unknown | Default plus Hefin, Ithell, Iorwerth (black), Trahaearn, Cadarn, Crwys, Amlodd and the permanent-world Deadman cosmetic enter the existing ownership families. Active forms retain ordinary ranged bonuses and crystal weights. | Inactive forms establish ownership and zero offensive contribution, never an active set multiplier. Charge quantity remains unread. This does not add magic armour comparisons or permit unsupported worlds. |
| Blazing was unknown | Loaded 28688 uses Toxic's reviewed ordinary stats/mechanic family; empty 28687 establishes ownership only. Existing Toxic fact keys remain stable. | Empty/manual ownership cannot establish a supplied reference. Darts, scales and current load remain separately labelled assumptions/checks. Other wood/drygore blowpipe families are not inferred as Toxic equivalents. |
| Max treads blocked ranged numbers | Add ordinary ranged stats for all eight existing treads forms: +15 accuracy and +2 strength, or +3 with the Pegasian attachment. Retain existing equip requirements. | No melee model or new individual-attachment acquisition outcomes. Existing magic stats and all formula modules are unchanged. |
| Rosewood looked like a damage upgrade over owned Toxic | Reviewed active Toxic/Blazing with the wield requirement covers the stronger ordinary fast-attack role. Rosewood explicitly says it is weaker and offers an optional scale-free alternative. | It remains in All/Specialist, with its recipe and original numerical comparison. Rapid Burst, resource economics and special-energy alternatives remain unquantified. Empty/manual Toxic alone cannot establish the observed active alternative. |
| Deferred options filled the homepage | Exclude DEFER from the homepage preview candidates; preserve rank and all eligible choices in Browse. Explain an all-deferred result instead of adding filler. | No item quota, font reduction or suppression from full discovery. Existing preview regression now checks non-deferred membership and full Browse retention separately. |

Full pinned Wiki pages were fetched for Blazing (`15191641`), the existing crystal helm/body/legs reviews (`15369569`, `15369568`, `15369571`), crystal equipment (`15355197`) and all eight existing treads reviews. Ordinary bonuses, cosmetic equivalence and active/empty distinctions were reviewed. Numeric IDs were checked against the existing official RuneLite `ItemID.java` revision `42a6f17a6a2e8e478aa763890ecd0181a59dad38`; bank placeholders, noted duplicates and restricted-world equipment were excluded. Source reverse dependencies remain validated. Only identifiers, independently recorded facts and original explanations are bundled; no article prose, calculator dataset/code or web sprites were copied. Retrieval files stay outside the repository.

## Regression scenario

`ClientEquipmentRegressionTest.reportedGearFixture()` is an original synthetic case using the reported gear classes: equipped black helm 27729/body 27721/legs 27725, loaded Blazing 28688, max treads 31097; observed bank Bowfa 25867 and dragon darts 11230. Ranged is 99, Magic/Slayer 99, other original fixture levels 90, all unlisted worn slots explicitly empty. Other storage/ownership remains unknown; no goal or ownership confirmations are required. These chosen inputs are not Petter's captured account.

The case produces supported ranged comparisons for vambraces, suppresses repeat crystal/Toxic/treads acquisition, and retains Rosewood in Specialist/All with the scale-free explanation rather than the homepage. Independent hand checks retain full-crystal max hit 32 versus inactive 28 at 99 Ranged; loaded Toxic/Blazing with dragon darts has max hit 20, 2 Rapid ticks and attack roll `107 × (30 + 64)` before armour. Treads add their exact reviewed bonuses. Unknown equipment, unobserved darts, empty forms and manual ownership retain their gaps.

JDK 11 / Gradle 8.10 / RuneLite 1.13.1 `test build --no-daemon` passes **228 tests, zero failures/errors/skips**, including the unchanged **1,944** original ranged oracle cases. Nine added regressions cover the colour/form matrices, independent ordinary hand checks, empty/manual/unknown evidence, exact treads strength differences, the container-reader-to-advisor path, Rosewood retention and native card discovery. All previous 219 tests remain; only the preview membership expectation intentionally changes to exclude deferred choices while preserving full Browse membership. `git diff --check` passes. The final capture adjustment is separately checked by rerunning `R3PanelTest`.

For this synthetic fixture's explicitly empty hands, vambraces produce max hit **20 → 21** and theoretical DPS **8.24 → 8.66 (+0.42)** in the neutral reference. The supplied player screenshot instead used Barrows gloves; these numbers are not claimed as Petter's gain. Value preserves the actual reference assumptions and separately labelled encounter examples.

Native Swing captures from the synthetic case use fallback item glyphs; they do not establish corrected runtime sprites or player usefulness:

- [Home](../images/r3a/client-gear-home.png)
- [Specialist scrolled to optional Rosewood](../images/r3a/client-gear-specialist.png)
- [Vambraces Value](../images/r3a/client-gear-value.png)

## PC retest

Close the development client, `git pull --ff-only` on `r3a-personalized-cards`, then run `gradlew.bat test build` and `gradlew.bat run` with JDK 11. On the supported Ironman/world, reopen the bank and allow two ticks. Confirm that owned coloured crystal and Toxic/Blazing are absent from acquisition cards, that Value recognises max treads and shows a valid comparison when the other inputs are supported, and that Rosewood is an explicit optional specialist. A remaining gap must name its actual input; internal dart loading is still an assumption/check. Post-fix live verification remains **pending**.
