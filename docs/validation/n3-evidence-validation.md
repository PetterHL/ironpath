# N3 evidence-validation readiness

23 October 2026. Development build **0.30.0**, main base
`1fe77cbcc8a939711438cc436f94cce3f31b9871` including merged PRs #50/#51/#53.
[Evidence, pinned sources, decisions and minimum live observations](../research/n3-evidence-validation.md).

## Result and boundaries

**N3 automatic recommendations: NOT READY.** All eight automatic evidence
gates remain. The conservative candidate, unknown-HP, legal/readiness, explicit
scenario confirmation, usable-stock and consumption gates are unchanged.
Whole-kill contribution remains Unverified. No new Nex kill is required for
this offline step; deferred bank/next-kill/reload acceptance remains separate.

- Supported conditional correction: stop natural regeneration at full energy,
  restart a complete fixed-ring interval when spending from full, preserve
  phase below full. Model `nex-spec-resources-3`; exact same-tick ordering,
  initial below-full phase and ring switches remain unvalidated.
- Supported instrumentation: repeat projectile getter LOCAL/OTHER/UNKNOWN
  counters; first-flight UNKNOWN never changes or enables a ranged link.
  Observation `nex-observations-5`, export `nex-evidence-3`.
- Existing usable evidence: processed own impacts including zero; timed scoped
  actor-bar intervals; agreed numeric energy and frozen visible levels/initial
  quantities. Each remains evidence at its own callback, not an inferred attack,
  exact impact HP, readiness, proc or consumption event.

This review uses the committed findings from the
[existing 0.29.0 report](nex-damage-observation.md#live-one-copy-check--23-october-2026).
It does not claim a new raw-report parse or gameplay observation.

## Verification

New independent resource cases check the recorded full-cap offsets 129/137/162
(rebased 0/8/33), all 26 initial Lightbearer phases and both order policies,
preserved below-full phase, standard-ring interval, natural/DC cap entry and
declared same-tick order. Existing cap-waste expectations are corrected: no
natural gain is generated while already full, while excess from actual gains
still counts. Full-start natural-only examples now have collapsed use ranges.

Projectile regressions cover UNKNOWN → LOCAL/OTHER getter changes, repeated
UNKNOWN, changed target scope, frozen first-flight evidence, no ranged match,
JSON counter agreement and lifecycle clearing. Existing maximum bounds,
saturation, privacy, page-independent Copy, unknown/confirmation, frozen-worker
and reset regressions remain required.

Both prescribed commands passed sequentially in repository CI on code delivery
[`288fa1d8ad76f23fbda67708599d08b8bccf05d6`](https://github.com/PetterHL/ironpath/commit/288fa1d8ad76f23fbda67708599d08b8bccf05d6):

```sh
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --no-daemon
./gradlew test build --no-daemon
```

The runner used **Temurin JDK 11.0.32+1 / Gradle 8.10**. The pinned command
used **RuneLite 1.13.2**; the normal build resolved **1.13.3**, verified by the
subsequent `dependencyInsight` step. Logs show success for every one of the
**14 DEV1 scenarios**, as well as both test/build commands. No new unit-test
total or manual artifact-content count is asserted from the job summary.

**CI: PASS** — [run 40020736579](https://github.com/PetterHL/ironpath/actions/runs/40020736579).
[Current code-delivery reports and native PNG artifact](https://github.com/PetterHL/ironpath/actions/runs/40020736579/artifacts/12529649256),
`dev1-offline-review`, SHA-256
`bd740cc17da000bc2517ae11a112a79322cf0be562ba400837f591b5bf6fd486`.
[Review PR #54](https://github.com/PetterHL/ironpath/pull/54) records the current
head status; this result remains scoped to the exact code delivery above.

The changed patch was reviewed and changed-document relative file targets and
added/edited source whitespace checked. The managed workspace stayed pending:
there was no local JVM run, raw live-report reparse, ZIP-content audit or manual
PNG inspection in this step. CI generated the native previews and ran their
existing automated assertions. No product layout was changed; real client,
player, clipboard and broader lifecycle acceptance remain open. This documentation
follow-up does not relabel earlier 0.29.0 results as current validation.
