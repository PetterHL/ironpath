# N3c complete-package bridge verification

16 October 2026. Build 0.19.0, based on N3b `dad1c5d` (PR #36, 0.18.0).
This is N3c's automated acceptance record, not N1/N2 client acceptance or a personal
upgrade recommendation. [Input/model/source contract](../research/nex-packages.md).

## Delivered behavior

- Shared N2/N3 package construction with the existing catalogue and ordinary
  models. Added exact Voidwaker and four ruby/diamond bolt facts; no second
  catalogue or simulator. Catalogue schema stays 14; knowledge is `n3-package-1`.
- Actual, supported owned and hypothetical upgrade scenarios retain immutable
  history and distinct marginal deltas. Exact hands/ammo/sets, locks, fixed
  Lightbearer, equip requirements/unlocks and joint gear/projectile packing are
  checked. Bank preparation needs known usable quantities and explicit packing.
- Whole-package bounds/rolls/delays feed N3b's finite energy, projectile, splinter
  and ordinary-displacement kernels. Missing inputs have named dependency gaps;
  unsupported alternatives do not suppress another explicit pair.
- Provenance includes observed/assumed/unknown origin, source/scope/time, exact
  packages, common conditions, event-to-step identity and model/source versions.
  Branch probabilities label conditional resource outcomes, not kill confidence.
- No runtime capture caller, new UI, persistence, live coaching or setup wizard.

## Independent automated cases

`NexPackageComparisonTest` exercises the full chain from catalogue-backed item
packages to ordinary/spec inputs, finite plans and separate comparison deltas.
Fixtures are anonymous. Levels, prayers, HP, legal opportunities, caps, timings,
stock, equip unlocks and recovery outcomes are explicitly synthetic assumptions.
The previously reported equipment fixture retains its own documented provenance.

Representative hand checks:

| Complete scenario | Independent expected result |
|---|---|
| Minimal Voidwaker, visible Strength 118, Piety, aggressive | Effective strength 156; total strength 80 -> ordinary max 35 -> spec 17..52, mean 34.5. Rancour changes max to 38/mean 38; adding Infernal cape changes max to 40/mean 40. Marginal gains are 3.5 then 2, with zero and negative reversals checked separately. |
| Fang under the same declared levels | Strength 103 -> base max 41 -> damage 6..35, mean 20.5 before accuracy. Stab attack roll 25181 against 27976; correlated outside-ToA hit chance is checked independently. |
| Minimal rapid ZCB, charged quiver and dragon ruby/diamond bolts | Complete ranged attack 138, strength 126; effective attack/strength 142/145 -> roll 28684, max 43. Heavy defence 57566. Evoke doubles the roll to 57368, not the accuracy. Ruby mean is 110 times its proc chance; diamond proc max 54 gives successful mean 27+1/55. |
| Charged -> depleted versus blessed | First charged shot agrees with blessed offence. Last splinter boosts that shot; subsequent shot uses independently derived roll 27264. Charged branch consumes one splinter with probability 1/3; blessed consumes none. |
| Voidwaker/ZCB competing with Fang ordinary attacks | One common 10-tick opportunity list permits two displaced-plan Fang attacks after Voidwaker and one after ZCB. Energy spending differs by 250 units; the complete damage delta includes that ordinary attack. |
| Full versus partial regular Void set | Full set floors effective levels to 156/159 -> attack roll 31512 and max 47. Removing gloves loses the set multiplier -> roll 28684 and max 43; the complete-package delta is negative. |

Further cases cover last ammo shared between ordinary/spec roles; rejected
delay/energy/ammo attempts; explicit saved projectile branches; initial/cast/death
state and event order; unknown HP, diary, recovery, stock, restore phase and initial
spell state; inapplicable player HP for diamond; unknown unused resource pools;
Zaros/minion/special/MP2 ranged-only windows; exact variants/requirements/locks;
two-handed switches and retained melee defender; finite packing and bank scope;
immutable history/facts/results; changed-condition and manufactured-stock rejection.

An integration regression exposed N2 accepting newly reviewed enchanted bolts
selected from a quiver without their proc model. It failed before the added
selected-ammunition guard. N2 now keeps that case explicitly unavailable; N3c
derives its supported bolt distribution through N3b. Physical and selected ammo
remain distinct. The retained ordinary model is not relabeled full bolt DPS.

## Execution

The affected N3c, N3b and N2 suites passed. After the additional N2 integration
regression failed before its correction, the final normal command passed:

```sh
source /workspace/ironpath-environment/activate.sh
XDG_CACHE_HOME=/tmp/ironpath-cache ./gradlew test build --no-daemon
```

- Temurin JDK 11.0.32.1+1, Gradle 8.10, resolved RuneLite 1.13.2.
- **253 tests across 32 suites; zero failures, errors or skips.**
- 23 new complete-package scenario cases plus one N2 selected-bolt regression;
  existing arithmetic, model/source, unknown-state, compatibility, capture,
  preference and lifecycle tests pass.
- Normal plugin jar: `build/libs/ironpath-0.19.0.jar`.
- `git diff --check` and changed-document relative file links pass.
- No historical archive files were changed. No additional client/UI claim is
  inferred from this build.

## Remaining validation and scope

N1/N2 live event mappings, stock/charge counters, combined banking/capture checks
and player usefulness retain their original open status. No game client was run,
no live kill/player acceptance was performed and no private account dump was
committed. The normal UI continues to expose frozen observed resource readings
and N2 comparisons, without automatic N3c attack-plan attribution.

N3 remains open for automatic capture-to-plan inputs, additional phase mechanics,
acquisition/recurring-cost presentation and supply models. N4 personal contribution
and team time remain separate. PR #36 is the implementation dependency; N3c is
delivered as a focused stacked draft for review, without merging either change or
publishing to Plugin Hub.
