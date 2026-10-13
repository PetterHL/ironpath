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
catalogue is redistributed. No competitor code or catalogue was used.

P4a's external OSRS Wiki DPS calculator reference was run separately at
`89c3e25b344aea90d0189746e4b5f73dde0f0383` under its GPL-3.0 licence. Its source,
algorithm, dependencies and equipment/monster datasets are not included here.
The local model is independently authored from individually checked mechanics;
the reference harness and synthetic inputs are original to this task. The small
result fixture contains outputs for those original inputs, with provenance and
tolerances in docs/RESEARCH.md. This does not relicense the external calculator.
