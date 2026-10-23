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

The managed workspace remained pending during preparation. Repository CI is
used for executable checks; exact commands, result counts and artifact links
will be recorded after the current delivery completes. No previous build result
is relabeled as validation for this change.
