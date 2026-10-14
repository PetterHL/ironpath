# AD1b — Automatic component acquisition estimates

14 October 2026 · review candidate stacked on EP1 #25 at `fafb7459c413add5d4a89b693c660cda70bf556f` · plugin **0.11.0**, knowledge **ad1b-1**, catalogue **13**, preference schema **2**, ranking **progression-2**. Combat formula versions are unchanged. Main and the separate AD1 planning PR #27 are not merged by this work.

## Result and delivery boundary

Automatic **Model-based estimate** hours appear without manual pace or personal history when a reviewed, source-dependent repeat-hour scenario is applicable. Matching optional manual pace replaces that scenario in session memory; Clear restores the automatic scenario or unavailable hours. Both origins are typed. A different activity's pace cannot override the default. UI-entered manual pace also captures the currently applicable method/version key; a tbow rate cannot transfer to a Bowfa method after account changes. Legacy manually authored test scenarios remain explicit activity/mode assumptions. The existing character/profile/snapshot/revision guards and memory-only storage remain in force.

Ten of the 29 active outcomes have conditional component probability models, covering ten of 30 routes and five of 17 acquisition families. Nine outcomes can show automatic component hours on applicable accounts. The first magic-fang route has odds/manual support but no promoted automatic first-Seas-trident pace. The automatic Zulrah benchmark applies to stronger observed powered-magic plus adequate Bowfa/full crystal or tbow switches; it does not require the blowpipe being sought. Muspah's automatic benchmark is tbow-specific; Bowfa remains an adequate qualitative method with odds/manual support, without borrowing tbow throughput.

These are **component acquisition** estimates, not total-item completion times. Source, method, trial/recipient, model/review, exclusions and ownership/allocation conditions remain inspectable. Unknown storage is not absence, quantities are conservative lower bounds, and a single shared shard/seed/synapse cannot finance multiple outcomes. A quantity shortfall gives an explicitly conditional **up to N** component scenario; enough observed components remove only that acquisition stage. Mean, median and P90 are separate; no probability-completed bar, summed quantiles, DPS/hour ranking, personal speed or success inference is added.

The [complete active outcome × route × method matrix](AD1b-coverage.md), retained secondary decisions and shared rewards record separate probability, automatic, manual, observed and full-route support. Reproduce it with `python tools/ad1b_coverage.py`. **AD1b remains open** for TASKS B1–B4 and actual account/client acceptance. Qualitative fallback and a complete inventory do not claim broad numerical completion.

## Mechanics and independent arithmetic

- Fixed next-component geometric models retain zenyte 1/300 and synapse 1/500 with their stable IDs and original source history.
- Corrupted Gauntlet enhanced seed: one eligible completed chest, 1/400. Ordinary Gauntlet and failed chests are not eligible trials. Armour seeds: one seed with 1/50 per eligible Corrupted chest; wait for the stated remaining count, rather than multiplying one-seed medians.
- Zulrah magic/tanzanite fang: two independent 1/1,024 rolls. Chance of at least one selected fang per kill is **2,047/1,048,576**. Mean waiting time is **1,048,576/2,047 ≈ 512.25** eligible kills, rather than exactly 512. A two-fang kill counts once toward the next-fang event; expected fang quantity is a different metric. Other rewards are not added.
- Muspah: one Venator shard with 1/100 chance per eligible kill. Five shards are needed for the bow; observed lower-bound quantities reduce the maximum remaining component scenario. The separate secondary loot rolls do not create two Venator rolls.

For r identical components with one success per Bernoulli trial, E[N] = r/p and P(N ≤ n) = P(Binomial(n,p) ≥ r). The independent Java implementation computes the finite lower tail analytically and searches integer quantiles; no simulation or general graph optimiser. Small finite populations use direct terms to preserve exact dyadic boundaries; large/rare populations use log-sum arithmetic. Hand cases: p=1/2, r=2 has median 3, P90 7, and P(N≤6)=57/64. Exhaustive finite bit-sequence enumeration independently checks those probabilities; quantile predecessor checks cover r=2,3,5 and several rates. Quantiles are not summed across stages.

A reviewed probability can survive unavailable/withdrawn pace. Invalid probability withdraws dependent odds/hours while preserving independently valid value/path/qualitative effort. Catalogue promotion validates source dependency links, dates, predicates, positive finite rates, roll/count contracts and activity/mode identity. The bundled schema changes to 13 because these mandatory contracts are new; preference schema remains unchanged.

## Pace evidence and applicability

The source benchmark is a conditional successful-repeat-hour planning assumption. It includes the ordinary repeat overhead implicit in the hourly acquisition guide, rather than just a successful combat timer. It does **not** include supply gathering, initial learning, breaks or additional failed attempts/downtime. No empirical failure probability is invented. Compatible account gear/levels/access selects a documented method scenario; it does not prove the player can sustain that rate. The scenario assumes the guide's compatible armour, prayer/boost, ammunition/supply and execution conditions, all disclosed in its basis.

The current canonical Wiki reader returned robots restrictions and ordinary HTTPS returned 403. No bypass was used. The review retrieved full attributed guide pages through the public OSRS Methods mirror, which identifies OSRS Wiki authors and licensing. Factual rate/requirement inputs and original explanations are bundled; no guide prose, tables, screenshots or code are redistributed. Canonical mechanics and adequacy references remain the repository's previously reviewed pinned Wiki records, supplemented by current indexed mechanic evidence. Mirror delivery is a source limitation; content hashes identify the retrieved response, not an immutable remote URL or a promise that prices remain fixed. Prices/GP and equipment cost are unused.

| Scenario | Benchmark | Compatibility review | Retrieved guide and SHA-256 |
|---|---|---|---|
| `cg-repeat` | 6 completed Corrupted chests/h | SOTE, Attack 80, Strength/Ranged 85, Magic 80, Prayer 77; generated equipment, not bank loadout | [Attributed guide](https://osrsmethods.com/methods/completing-the-corrupted-gauntlet); `99d45b7c9ad66fead8547cfb18007ab7aacfff9422f2340403c0fe5fb6ceea33` |
| `zulrah-switch-repeat` | 20 eligible kills/h | Regicide, Defence 70, Ranged 80, Magic 78, Prayer 45; supported usable swamp-or-better mage switch and adequate Bowfa/full crystal or tbow range. Reviewed strategy alternatives supply the compatibility judgement; rate stays fixed, with no DPS scaling. First Seas-trident/mage-only pace is not promoted. | [Attributed guide](https://osrsmethods.com/methods/killing-zulrah); `86b1445bc31ebcc786d29c88c0f69ec1db515ff84db351687f8b2cfec97a5bb6` |
| `gorilla-switch-repeat` | 70 eligible kills/h | MM2; Attack/Defence/HP 80, Strength/Ranged 85, Magic/Prayer 70, usable reviewed melee/range switches; guide armour/boost/supply execution assumed | [Attributed guide](https://osrsmethods.com/methods/killing-demonic-gorillas); `3da983968a4b50060dd457e3f668346b00464167b8de1ac7d86f04001c4d3ab6` |
| `demon-switch-repeat` | 55 eligible kills/h | WGS; Attack/Defence/HP 80, Strength/Ranged 85, Magic 82, Prayer 74, usable reviewed melee/range switches; guide armour/boost/supply execution assumed | [Attributed guide](https://osrsmethods.com/methods/killing-tormented-demons); `f809093cc8bdd35ba67426b0f0d4f685fa6a9ac58d8e516032d9e155504acc7b` |
| `muspah-tbow-repeat` | 25 eligible kills/h | Secrets of North and method review, Ranged 90, Defence 80, Magic 85, Prayer 74 and usable tbow; guide ammunition/armour/thralls/shield-method supplies assumed. No unsupported 30/h mage-switch transfer. | [Attributed guide](https://osrsmethods.com/methods/killing-phantom-muspah-twisted-bow); `b0b8694caa37fd61e3703fa55dea3a7e97264e7240f5133c0148f93a9994ec1e` |

Source-dependent scenario support can be withdrawn independently of acquisition mechanics. Only reviewed profiles with TRUE essential predicates auto-select; false or unknown inputs cannot select a weaker fallback rate. Observed component ownership may bypass access to a finished drop stage, while remaining assembly/supply work stays separate.

## Synthetic account checks

These fixtures are authored test inputs, not Petter's account or measured RuneLite sessions.

| Account / change | Expected scoped result |
|---|---|
| Compatible developed switches, no pace | Next zenyte mean 300/70 ≈ 4.3 h; synapse 500/55 ≈ 9.1 h |
| Same account with matching 30–50 gorilla/h manual range | Mean sensitivity 6–10 h; labelled manual, not an RNG interval; Clear returns 300/70 |
| Supported Zulrah switches, blowpipe still missing | Next fang mean ≈ 25.6 h at 20/h; own tanzanite fang removes only the fang stage |
| CG-compatible account, Bowfa still missing | Enhanced seed mean 400/6 ≈ 66.7 h; shard/singing/learning work excluded |
| Crystal body missing, then two armour seeds observed | Up to three-seed mean 25 h becomes up to one-seed mean ≈ 8.3 h; three observed seeds removes drop hours, not assembly |
| Post-Bowfa Muspah, then tbow observed | Odds/manual only becomes an applicable automatic five-shard scenario with mean 20 h; three observed shards gives up to two shards / 8 h |
| Both crystal body and legs missing; one shared seed | Alternative allocations remain conditional and separate; their hours are not added |
| Owned zenyte intermediate or five observed Venator shards | Completed acquisition stage has unavailable remaining-drop hours, not a zero full-route ETA |
| Unknown essential gear/quest or incompatible numeric-method levels | Dependent hours unavailable; value/path/eligible odds remain where supported |
| Withdrawn gorilla benchmark | Zenyte odds/A0 remain; automatic hours withdraw; unaffected CG default remains |

## Validation and pending acceptance

Final `./gradlew test build` passes **307 tests, zero failures/errors/skips** on Temurin **11.0.32.1+1**, Gradle **8.10**, RuneLite **1.13.1**. The coverage generator reproduces the committed matrix byte-for-byte; `git diff --check` and Markdown fence checks pass. Existing font-cache/deprecation messages do not fail the build. Tests cover the actual Advisor → typed presentation → card/detail path, independent reward arithmetic, partial/owned components, matching/manual/Clear, incompatible/unknown inputs, source withdrawal and existing lifecycle/navigation behavior.

Pending: actual RuneLite sprites/native width/enlarged fonts, player usefulness and timing against a full eligible repeat session, Petter's reported setup and contrasting accounts. Headless Swing captures use fallback sprites and cannot establish client acceptance. [Automatic synapse path](ad1b-images/synapse-automatic.png), [automatic Zulrah component path](ad1b-images/fang-automatic.png) and [enlarged fonts](ad1b-images/large-font.png) exercise real Swing controls with synthetic account inputs. Model-based assumptions are not observed pace; A3/R1.5b passive capture remains separate. UX2/AD1c still owns the complete visual flow. B1–B4 remain scheduled in TASKS with family-specific blockers.
