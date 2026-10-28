# Practical Nex equipment estimates — 0.34.0

28 October 2026. Continues PR #59 `ef74891` with #57 `ed58691` in its
ancestry. The user's practical-estimates decision supersedes earlier promotion
and whole-encounter validation gates; it does not change the recorded evidence.

The retained kill provides loadouts, visible stats, prayers and ownership inputs.
Planning changes replace the calculation revision while keeping that account
basis. Context edits update method weights and team-size fallback. New worker
results still require matching record/input/context revisions and account scope.
New kills supply new bases; explicitly opened older comparisons/reports stay frozen.
Useful partial captures offer equipment comparisons without whole-kill seconds.

The main list uses the existing legal-package search and combat kernels against
base Nex in ordinary vulnerable windows, independently of action/projectile
attribution. Melee/ranged rates account for speed changes; supported Voidwaker
and Zaryte ruby/diamond specials compare equal 50%-energy launches. Unknown item
forms and equip requirements are still rejected, and upgrades are not owned stock.
Existing finite resource-planner and hitsplat target/object-reuse guards remain.

Contribution is recorded own / total damage on Nex and named minions, excluding
healing/unclassified hits. Estimates use that share (otherwise 1/team size,
default trio), observed complete duration and unchanged teammate throughput.
Role damage mix is MP2 70/20/10 or RP2 15/75/10 for melee/ranged/specials. For role
gain r and weight w, g=w*r; new contribution is s*(1+g)/(1+s*g), duration is
T/(1+s*g). Alternatives remain independent, including negative/zero changes.

The model assumes no fixed waits, reference target stats and supplied ammo/charges.
An observed empty non-blessed quiver loses its charged bonus. Ruby bolts use
representative target/player HP 2000/99, no Kandarin bonus; this is not a phase/HP
simulation. No Death Charge schedule, supply savings or attack ownership is inferred.
Assumptions and numerical pairs are in existing Diagnostics / equipmentEstimates
report JSON. Normal screens omit empty supply and unavailable estimate fields.

Validation: JDK 11.0.32.1+1 / Gradle 8.10, normal `./gradlew test build --no-daemon`
passed all 481 tests across 54 suites. Regressions cover missing action
mappings, partial loadouts, role weighting, missing resource details, settings and
context recalculation, stale publication, identity/reset, frozen report selection,
contribution display and melee/ranged navigation. Existing offline scenarios run
inside the normal suite; no additional screenshot bundle was produced.
Existing live reports were reused; no fresh Nex kill or publication is required.
