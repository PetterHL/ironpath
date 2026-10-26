# Fang zero-damage display — 0.32.3

26 October 2026. Follow-up to the user's screenshot of **Bandos chestplate,
+0.00 expected damage, 3 Fang attacks** on open PR #57, following `b3a147d`.
The previous supplied JSON described two attacks with a negative-to-zero Bandos
range. That earlier numerical result does not explain this new scalar-zero
screenshot. There is no matching three-attack report, so no exact live input
or cause is claimed from the image alone. Raw captures remain outside Git.

## Finding and correction

The existing complete-package model reproduces a genuine zero for a synthetic
Torva platebody → Bandos chestplate change across three compatible ordinary
Fang attacks at visible Attack/Strength 118. Both damage intervals are 8–47,
with identical accuracy: the lower Strength bonus does not cross a damaging
breakpoint for this setup. At visible level 117 the max hit instead falls from
47 to 46, and the paired gain is negative. At level 114 the minimum Fang roll
falls even though both maxima remain 46, so max-hit equality alone is not proof
of equal expected damage. These are controlled examples, not a replay of the
user's unidentified three-attack kill.

Presentation changes:

- Exact zero now reads **No expected damage change**. It means equal modelled
  ordinary attack damage on that sample, not equal defensive/Prayer/survival value.
- A small nonzero scalar uses **<0.01 expected gain/loss**, preserving direction.
- Interval endpoints retain outward rounding. Floating residue below `1e-9`
  damage at zero is cleaned for display, preventing a spurious +0.01 or -0.00.
  A zero endpoint reads 0.00 without a positive sign.
- Single-item alternatives name **Replaces {item}** from the exact comparison
  baseline. Every supported card shows **Max hit {before} → {after}** alongside
  expected damage and the included attack count. Full min/max/accuracy inputs
  remain available in Diagnostics.

This is a display correction. Combat kernels, exact exported bounds, sample
references, frozen comparison inputs, known-HP policy and all phase/target/action
gates are unchanged. No defence/survival, whole-kill, DPS or max-hit interpretation
is substituted for the expected-damage change. Model/schema versions remain
unchanged; only the build version advances to 0.32.3.

## Verification

Regression coverage uses the shared evaluator on three attacks at those nearby
boost levels, retaining exact zero, real negative bounds and the minimum-roll
breakpoint. A separate one-damage distribution verifies that a sub-cent gain
and loss remain distinguishable from exact zero.

DEV1 uses public plugin/observer/evaluator events for a synthetic Torva-to-Bandos
zero comparison, checks the labels and baseline, and captures full-height plus
225 × 600 views at fonts 12 and 20. The genuine Bandos sprite 11832 is pinned in
the existing Jagex provenance manifest; no artwork is approximated or taken from
the user's screenshot.

The local pinned test/build passes **36 tests in 4 suites** covering Fang advice,
frozen Diagnostics, ItemManager sprites and the native N3 scenario, using JDK
11.0.32.1+1 / Gradle 8.10 / RuneLite 1.13.2. Documentation links and whitespace
checks pass. The prescribed full pinned test/build/offline review
and normal RuneLite compatibility build run in CI. Performed results and the
review artifact are recorded in [PR #57](https://github.com/PetterHL/ironpath/pull/57).
Live mapping/window acceptance and the missing typed DC source remain open.
