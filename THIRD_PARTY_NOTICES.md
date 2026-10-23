# Third-party notices

The development scaffold is based on the official
[RuneLite example plugin](https://github.com/runelite/example-plugin/tree/46e14f2c448fab6cbc599df1015b9631dcf1fc52),
checked on 12 October 2026. The Gradle wrapper and launch/build structure are
retained. The template has no root licence file at that commit; its wrapper
scripts retain their Apache-2.0 headers, and `src/test/resources/logback-test.xml`
retains Adam's complete BSD-2-Clause notice. The Gradle wrapper JAR is distributed
under [Apache-2.0](LICENSES/Apache-2.0.txt).

RuneLite and development dependencies keep their own licences. The original
Ironpath code, explanations and documentation use the repository's BSD-2-Clause
licence; this does not relicense dependencies or evidence sources.

The bundled catalogue source register links pinned OSRS Wiki revisions and the
official RuneLite item definitions, recording authors, access/review dates,
licences and reuse basis. Wiki content is CC BY-NC-SA 3.0; see the checked
[Weird Gloop licensing policy](https://meta.weirdgloop.org/w/Licensing?oldid=1299).
RuneLite's source is BSD-2-Clause. Only a
small selection of checked mechanic facts informs original explanations; no
Wiki prose, equipment tables, images, transcripts or third-party progression
catalogue is redistributed. No competitor catalogue is redistributed. Nex observation source-pattern references are described below.

P4a's external OSRS Wiki DPS calculator reference was run separately at
`89c3e25b344aea90d0189746e4b5f73dde0f0383` under its GPL-3.0 licence. Its source,
algorithm, dependencies and equipment/monster datasets are not included here.
The local model is independently authored from individually checked mechanics;
the reference harness and synthetic inputs are original to this task. The small
result fixture contains outputs for those original inputs, with provenance and
tolerances in docs/RESEARCH.md. This does not relicense the external calculator.

## Preview game artwork

Created using intellectual property belonging to Jagex Limited under the terms of Jagex's Fan Content Policy. This content is not endorsed by or affiliated with Jagex.

The genuine OSRS item icons in `src/test/resources/com/ironpath/sprites/*.png`
and their appearance in development screenshots remain Jagex Limited's property.
They are **not licensed under this repository's BSD software licence**.

Source: RuneLite's item-image service, `https://static.runelite.net/cache/item/icon/{itemId}.png`.
The adjacent `manifest.json` records each exact item ID, source URL, retrieval date,
dimensions and SHA-256. The original PNG bytes are retained without recolouring,
redrawing, generative approximations or extraction of game code. These are a
small set of test-only fixtures for this free RuneLite plugin's native UI review.
Neither the production plugin JAR nor its resource loader contains these files;
production uses RuneLite's public ItemManager/cache API. No runtime web request
is added. The development-only shadow launcher includes test resources.

Reuse basis reviewed 18 October 2026: Jagex's [Fan Content Policy](https://legal.jagex.com/docs/policies/fan-content-policy),
version **1.4, 29 September 2026**, particularly 2.1/2.2 (game art versus code),
3 (limited/revocable licence), 4.1.6 (modifications/plugins), 4.2 (sharing),
6.1.3/6.1.6 (permitted plugin exception and no endorsement) and 8 (attribution).
This notice does not grant broader asset redistribution or imply Jagex/Plugin Hub
approval of this plugin's features. Screenshots are non-commercial plugin review
evidence, not a standalone artwork package. Follow the policy for any further use.

IronPath source code remains under [BSD 2-Clause](LICENSE). No third-party
calculator code, datasets, article prose or generated item art is included.

## Nex observation source patterns

Build 0.29.0 follows RuneLite DPS Counter and Damage Counter's direct
HitsplatApplied-to-scalar classification pattern, adapted to IronPath's existing
spawn/view, immutable capture, privacy and lifecycle boundaries. The integration,
bounded counters and tests are independently written; no external handler is
copied verbatim. Their complete BSD-2-Clause notices are retained in
[LICENSES/Nex-observation-references.txt](LICENSES/Nex-observation-references.txt).
Combat Logger was inspected as a comparison for immediate scalar/unknown-source
capture; no code, queues, Party integration or export was imported. Resource timing
references were compared against existing IronPath behavior without copying code.
Exact sources and dispositions are in the
[research record](docs/research/nex-damage-observation.md).
