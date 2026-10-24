# Karambwan potion cooldown

24 October 2026. Development build **0.30.1**, based on integrated main
`9a2b59299e45304aa702d94b5717b9ad30e1df42`. Supply model `nex-supplies-2`.
This addresses the [PR #50 P2 review finding](https://github.com/PetterHL/ironpath/pull/50#discussion_r4367600752).

## Correction and source

The declared supply validator advanced normal-food and karambwan readiness after
a karambwan but left potion readiness unchanged. It therefore accepted potions
one or two ticks after eating one. An impossible restore/reboost could affect
offensive levels or Death Charge's Magic requirement.

Karambwan now advances all three readiness timers to its tick plus three. The
declared same-tick order remains shark, potion, karambwan. A potion before the
karambwan at that tick is valid; the next potion must wait until the boundary.
The validation feedback names both potion and karambwan cooldown sources.

The [reviewed mechanics reference](https://github.com/data-dependent/osrs-guides/blob/07d8da46ba83cea00666bd00ee2cf4587aff7475/skilling.md#review-of-specialized-mechanics),
retrieved 24 October 2026, explicitly describes the three-tick block on normal
food, potion sipping and karambwan eating. This is a bounded rule correction;
no reference implementation, prose or images are copied.

## Regression evidence

- All four supported potion families reject ticks 1 and 2 after karambwan at
  tick 0, and accept tick 3.
- Same-tick shark → brew → karambwan retains that order and accepts restore at
  tick 3. Brew at 0 followed by karambwan at 1 moves the next-potion boundary
  from 3 to 4.
- The native scenario editor rejects early restore before queuing work and
  admits a finite comparison at the boundary.
- All three new tests fail on the uncorrected implementation. The existing
  `nex-n3-plan` fixture moves restore from tick 3 to tick 4 after karambwan at 1;
  its frozen report checks the corrected schedule, supply version and unit cost.

## Verification

Both commands passed sequentially with JDK **11.0.32.1**, Gradle **8.10** and
RuneLite **1.13.2**:

```sh
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --no-daemon
./gradlew test build --no-daemon
```

**416 tests / 47 suites; zero failures, errors or skips.** DEV1 passed all
**14 scenarios / 1,137 checks**, producing **244 PNGs**. All **64** viewport
images are **225 × 600**. Corrected supply viewports and full-height editor/results
were inspected at body fonts 12 and 20. The frozen supply report retains the
three-tick boundary, `nex-supplies-2` and the independent 400 GP supply cost.
Changed-document relative links and `git diff --check` pass. Generated reports
and screenshots remain build/CI artifacts, not committed source files.

Unknown-state gates, finite stock, attack delays, level effects and all eight
automatic recommendation gaps remain in place. These offline checks do not
establish live consumption attribution or client/player acceptance.
