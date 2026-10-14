# R3a Lightbearer and Specialist client follow-up

14 October 2026. Plugin **0.8.2**, knowledge **r3a-3**, schema **10**, preference schema **2**. Same implementation PR #18 and unchanged documentation dependency #17 at `75897d5`; no PR is merged.

Petter confirmed that the repeated crystal/blowpipe acquisition cards disappeared. The new vambraces Value screenshot still withheld numbers for equipped ring 25975: **Lightbearer**, already recognised by the magic model but absent from ranged stat records. Petter also reported an empty Specialist filter and explicitly requested removal of Rosewood acquisition advice, suggesting Doom special use only as a possibility. That possibility is not promoted to a runtime claim.

## Corrections

- Add Lightbearer's reviewed **0 ranged accuracy / 0 ranged strength** to the existing ranged reference catalogue. Re-fetched full Wiki revision `15299656` confirms zero offensive bonuses. Special-energy regeneration remains separate; it is not a constant ordinary DPS multiplier. Unknown rings still withhold numbers, and unknown ammunition is still a separate gap.
- The legacy `ranged` role predicate included Bowfa/tbow/rune crossbow but excluded Toxic/Blazing and other supported R2 weapons. Venator discovery and R3 ranged role predicates now use the existing `rangedObserved` set with per-item level/usability checks. Empty forms and manual possession do not establish those capabilities. Source dependencies are expanded and checked against the exact forms; no catalogue cap or filler is added.
- Mark the Rosewood acquisition outcome and decision inactive using the existing candidate status. Both are excluded from Home, All, Specialist, activity/quick recommendations and legacy advice. Keep stable IDs, ownership/preferences, reviewed ordinary stats and all numeric oracle cases. Remove the previous optional-Rosewood importance rule. A justified Doom special recommendation requires a separate review/model; it remains research.
- Explain a genuinely empty Specialist filter in terms of reviewed scope, owned outcomes, excluded routes and unconfirmed method inputs. Other supported upgrades remain accessible through All.

## Regression evidence

`ClientEquipmentRegressionTest.reportedLightbearerFixture()` is synthetic: black crystal helm/body/legs, loaded Blazing, max treads, Barrows gloves and Lightbearer equipped; dragon darts observed; no Bowfa/tbow observed and no goal/manual ownership needed. Ranged/Magic/Slayer are 99, other fixture levels 90, unlisted slots explicitly empty. These inputs reproduce the reported gear classes, not Petter's full account or loaded ammunition.

This case has a complete ordinary ranged reference and a Venator specialist card despite the lack of a legacy ranged weapon. Rosewood appears nowhere in advice. Lightbearer and an explicitly empty ring have identical ordinary metrics under the same inputs. No special-energy, bounce or encounter model is invented. Tests also cover absent ammunition, unrelated unknown rings, owned Venator, route exclusions, weak ranged baselines and already-owned Rosewood comparisons.

The existing R2 ammunition-gap UI test now opens the retained tbow weapon outcome instead of the withdrawn Rosewood card; all original unavailable-metric and supply assertions remain. The prior optional-Rosewood tests explicitly migrate to withdrawal assertions. Original arithmetic, all existing ranged stat records and the 1,944-case oracle corpus remain unchanged.

JDK 11 / Gradle 8.10 / RuneLite 1.13.1 `test build --no-daemon` passes **232 tests, zero failures/errors/skips**, including all **1,944** original ranged oracle cases. Four added regressions cover Lightbearer equivalence, specialist discovery without legacy weapon ownership, Rosewood withdrawal across account baselines, and a genuinely empty Specialist filter. `git diff --check` passes.

For the explicitly supplied synthetic 99-Ranged reference with Barrows gloves, vambraces show max hit **20 → 21** and theoretical DPS **8.25 → 8.66 (+0.41)**. Lightbearer remains in the reference with zero ordinary contribution; these are fixture values, not an asserted gain for Petter's complete account. Post-fix RuneLite verification remains **pending**. Committed captures use synthetic inputs and fallback glyphs:

- [Home](../images/r3a/lightbearer-home.png)
- [Specialist](../images/r3a/lightbearer-specialist.png)
- [Vambraces Value](../images/r3a/lightbearer-value.png)

## PC retest

Close the development client, pull `r3a-personalized-cards`, run `gradlew.bat test build`, then `gradlew.bat run`. Reopen the bank and allow two ticks. Check vambraces Value with Lightbearer still equipped; if another input is missing, it must be named. Check Specialist with an observed active Toxic/Blazing; existing ownership/skips may still leave that category empty. Rosewood must be absent from every recommendation view.
