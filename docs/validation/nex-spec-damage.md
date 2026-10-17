# N3b mechanics validation

16 October 2026. Build 0.18.0, based on combined main `dc8c5f4` (0.17.1).
Models `nex-spec-damage-1`, `nex-spec-plan-1`; retained energy rules
`nex-spec-resources-1`. [Source review and exact contract](../research/nex-spec-damage.md).

## Delivered

- Single-launch Voidwaker, PvM ruby/diamond and ZCB effects with explicit complete
  package stats, heavy defence, current HP, hard caps and ruby eligibility/self-cost.
- Finite exact ammo and charged/blessed quiver branches joined to shared spec
  energy, Lightbearer, Death Charge and ordinary attack delays. Empty ammo does
  not spend energy; rejected attacks do not consume projectile/splinter outcomes.
- Separately supplied charged/uncharged packages, last-splinter behavior, exact
  remaining/spent stock, conditional branch probabilities and immutable results.
- Named damage gaps preserve unrelated supported launches and energy/ammo results.
  Runtime capture/advice/lifecycle behavior remains unchanged; native resource
  details continue explaining why captured readings cannot establish spec advice.

## Independent verification

The targeted damage/plan/energy suites pass. New tests use hand-enumerated numeric
cases rather than expected values calculated by the implementation:

- Voidwaker max 51 gives 52 equally likely values, 25–76, mean 50.5; hard-cap and
  remaining-HP clipping, odd rounding, ordinary successful-zero and Fang intervals.
- ZCB attack roll 100 doubles to 200 versus defence 199: accuracy 1/2. Ruby's
  total proc probability is 0.53 (including misses' natural chance), yielding
  58.3 expected target damage at high HP and 2.65 self-cost at player HP 59.
  Diary, zero accuracy, 499/500 target HP, low target/player HP and immunity cases.
- Diamond's distinct proc chance, 115%/126% rounded ranges, successful zeros,
  accuracy bypass, normalization, HP/hard caps, invalid inputs and immutable output.
- One projectile over two opportunities has exactly three terminal assembler
  branches: probabilities 0.64, 0.16 and 0.20; expected accepted shots 1.8 and
  projectile consumption 0.36. No fractional stock permits an impossible shot.
- One splinter over two shots has three terminal paths; weighted mean damage
  65/3 in the synthetic 10/11-damage packages and expected splinters spent 5/9.
  Charged/blessed offence agrees when charge remains; blessed spends no splinters.
- Mixed melee/ranged/spec resource competition, empty-bolt rejection with an
  equal-tick fallback, unused random outcomes, insufficient energy/delay, improved
  Death Charge plus natural restore, distinct event indices and missing state.
- Under one synthetic ten-tick condition, baseline damage 40; a Voidwaker plan
  gains 40 and a ZCB plan gains 38.3 after their different ordinary displacements.
  These are hand examples, not a recommendation for Petter's actual setup.
  Identical and lower whole plans retain zero and negative deltas.

The final normal JDK 11 / Gradle 8.10 / RuneLite 1.13.2 `test build --no-daemon`
passes: **229 tests across 31 suites, zero failures/errors/skips** (34 seconds).
The two new suites contribute 20 tests; all nine existing energy scenarios and
the retained combat, unknown-state, package, native-view and lifecycle regressions
pass. A long rare branch also preserves a finite log probability after the usual
double probability underflows. Changed Markdown links and `git diff --check` pass.
The existing Gradle deprecation notice remains. No graphical client or new native
visual acceptance is claimed from this build.

## Remaining acceptance

No real client was launched and no new live-kill/player test was performed.
N1/N2 mappings, resource attribution and player acceptance remain open. The model
has no automatic capture-to-plan connection, spec-weapon ranking or upgrade cards.
Input package legality, opportunity eligibility, target/player HP and recovery
operation must be established by the eventual caller. Resource branch weights
are conditional RNG probabilities, not confidence in an upgrade or a forecast of
a complete kill. No phase/soft-cap, survival, contribution, team-time, acquisition
pace or Plugin Hub acceptance is claimed. See TASKS for remaining N3/N4 work.

## DEV1 integration review — 17 October 2026

Merged the reviewed DEV1 workflow from main after PR #40. The validation-index
conflict retains both DEV1 evidence and this delivery's version/scope. No numerical
mechanics or calculation expectations were changed by this integration.

Reviewed discrete Voidwaker/ruby/diamond outcomes, probability normalization, caps and self-cost, exact finite ammo/splinter accounting, resource admission before spending, equal-tick events and scoped damage gaps. The twenty independent hand/resource regressions remain in the full suite.

Normal compatibility command:

```sh
source /workspace/ironpath-environment/activate.sh
./gradlew test build offlineReview --no-daemon
```

Temurin 11.0.32.1+1 / Gradle 8.10 / RuneLite 1.13.2: **246 tests across
33 suites**, zero failures/errors/skips. All seven DEV1 scenarios passed
**231 checks** and regenerated seven reports plus **44 PNGs**. The retained DEV1
scenarios exercise capture, ordinary comparisons, lifecycle/publication and UI;
this delivery's new numerical kernels are verified by their separate unit/
integration hand cases, not automatically attributed to a captured kill.

No new blocking issue was identified within this conditional API scope. Existing
source-retrieval limitations and live/client/player acceptance remain open. No
automatic recommendations, additional phase simulation or N4 time claim is made.
