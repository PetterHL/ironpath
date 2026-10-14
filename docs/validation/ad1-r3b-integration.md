# EP1 / UX2 / AD1 / R3b integrated delivery

14 October 2026. Test **main**, plugin **0.14.0**, knowledge **ad1-r3b-1**,
catalogue schema **14**, preference schema **2**. This integrates the existing
published deliveries; it starts no new feature area and makes no client/player
acceptance claim.

## Reviewed inputs and order

| Order | PR | Original reviewed head | Contribution |
|---|---|---|---|
| 1 | [Plan #27](https://github.com/PetterHL/ironpath/pull/27) | `b2eb064` | Updated AD1 requirements and remaining acceptance |
| 2 | [EP1 #25](https://github.com/PetterHL/ironpath/pull/25) | `fafb745` | Catalogue-wide progression-2, qualitative profiles and typed presentation |
| 3 | [UX2 #26](https://github.com/PetterHL/ironpath/pull/26) | `54d9c239` | Native visual advisor, full comparison/path/account flow |
| 4 | [AD1a #28](https://github.com/PetterHL/ironpath/pull/28) | `75595a5` | DPS-first offensive results, Brimstone and compatible setup labels |
| 5 | [AD1b #29](https://github.com/PetterHL/ironpath/pull/29) | `98c82d2` | Source-dependent automatic component hours, distributions and scoped manual pace |
| 6 | [AD1c #30](https://github.com/PetterHL/ironpath/pull/30) | `095d915` | Recommended order, shared checks and Upgrades / Account → Impact / Acquisition |
| 7 | [R3b #31](https://github.com/PetterHL/ironpath/pull/31) | `b499359` | Melee-2 families/conditions and component-aware remaining paths |

Baseline: main `e3f02bc`, preserving all earlier integrated deliveries, ordinary
melee and experimental KI1. Plan #27 precedes the code so conflicts follow its
DPS/automatic-time/navigation requirements. AD1b joins after the shared EP1/UX2/
AD1a contracts and before AD1c; the published R3b increment joins last. Merge
commits retain every original head and its evidence. No unrelated PR/branch is
closed, overwritten, reset or deleted. User authorization covers pushing
integration fixes and merging these PRs; Plugin Hub publication is outside scope.

Original [EP1](EP1.md), [UX2](UX2.md), [AD1a](AD1a.md), [AD1b](AD1b.md),
[AD1c](AD1c.md), [R3b families](R3b-families.md) and
[earlier UX/melee/KI1](ux-melee-ki1-integration.md) reports retain their original
versions, synthetic accounts, mechanics/source provenance, oracle cases and
pending client checks. Their standalone candidate status is historical; this
report and the current README/TASKS describe the joint version.

## Integration decisions and review

- Preserve progression-2 as the common ordering policy. All **54 active outcomes,
  55 routes and 28 acquisition families** have reviewed qualitative profile/source
  contracts, including R3b. New R3b routes reuse applicable existing methods;
  new activity profiles carry reviewed package predicates, qualitative remaining
  work and named acquisition gaps. Long-tail routes retain Very long work,
  substantial ordinary component routes retain Long work, and actual completed
  stages shorten only their own acquisition. Owned gear does not establish
  readiness, speed, skill or success. The full emitted-outcome and priority audits
  include an owned-treads fixture so the primordial addition is evaluated too.
- Merge the catalogue structurally by stable identities. Keep AD1a magic-3,
  ranged-1, R3b melee-2, all exact forms, sources, role relationships, routes,
  component counts, allocations, effort and pace records. Synapse has one gear
  identity across EP1/R3b; both source records survive, with matching route/profile
  dependencies. Catalogue validation still rejects unsupported/malformed
  conditions, sources, promotion contracts and cycles. Schema 14 records this
  combined contract; existing preference schema/storage remains 2.
- AD1b estimates now render in AD1c's cards and Acquisition. Automatic defaults
  are **Model-based estimate**, never manual or measured. Mean, median/P90 and
  optional manual sensitivity remain distinct. Controls capture activity, mode
  and method/version with the existing stale-view/account guards. The automatic
  value leaves the optional input blank; **Clear pace** restores the supported
  default or unavailable hours. Expanded basis sections and pace/Defence fields use the native column at normal/enlarged fonts, with measured bounds checks. Estimate basis retains roll count, recipient,
  method assumptions, exclusions and linked source evidence.
- R3b results use AD1a's relative DPS-first card/detail contract for ordinary
  offensive armour, jewellery and weapons. Details retain absolute values,
  max hit, accuracy, compatible hands/loading and changed-slot labels. Keep the
  domain weapon-comparison flag separate from display selection. Soulreaper
  ramp carries `NEXT_FIVE_SWINGS` into the typed headline and detail metric;
  **Five-swing mean DPS** is not steady full-stack or full-fight throughput.
  Blood-fury resource/sustain utility remains alongside its lower ordinary DPS.
  Off-hand removal/restoration remains explicit. Role explanations use the
  actual role rather than incorrectly calling every new melee role a ranged neck.
- Preserve KI1, privacy/preferences, reference independence, identity/revision
  guards, stale UI/image actions, search/filters/caret/Back/scroll, owned-use
  discovery and the broader catalogue. No combat arithmetic, probability, pace
  benchmark, personal-time model or gameplay automation is invented to resolve
  a merge. Existing oracle/independent arithmetic evidence remains authoritative
  within its reviewed boundaries.

## Combined verification

Build environment: Temurin **11.0.32.1+1**, Gradle **8.10**, RuneLite **1.13.1**.
Initial restored dependency caches needed normal network resolution; the final
verification uses the complete cached dependencies.

```sh
source /workspace/ironpath-environment/activate.sh
./gradlew test build --offline --console=plain
python3 tools/ad1b_coverage.py
git diff --check
```

The complete combined `test build` passes **350 tests, zero failures, errors or skips** (4m 20s). A subsequent `test build` reruns the affected native UI and integration cases after the final pace/Defence field alignment/font adjustment; this changes presentation only, leaving catalogue, arithmetic, ranking and probability inputs unchanged. The affected run passes **24 tests, zero failures, errors or skips**, and rebuilds the final 0.14.0 jar (1m 20s). `git diff --check`, current Markdown fences/links and byte-identical acquisition coverage regeneration pass.
Tests cover original magic/ranged/melee arithmetic/oracles, acquisition
probability and quantities, actual Advisor/typed presentation, source withdrawal,
automatic/manual/Clear/method matching, shared corrections, recipe bypasses,
unknowns, identity/lifecycle/migrations and native Swing navigation. The added
integration regressions specifically check DPS-first melee accessories,
blood-fury negative offence/independent utility and soul-ramp scope. Original
UI expectations are updated only where the selected navigation/automatic-hour
contract supersedes them.

[Current generated acquisition support matrix](integrated-acquisition-coverage.md)
is reproducible and supplements the unchanged original AD1b matrix. Ten of 54
outcomes have component odds; nine can show automatic component hours on eligible
accounts. Five reviewed scenarios serve CG, Zulrah, demonic gorillas, tormented
demons and tbow Muspah. Other methods/outcomes retain specific source/state/pace/
remaining-work gaps; this fraction is not broad AD1b completion. No full-route
ETA, observed pace, summed quantile, personal seconds or trip claim is promoted.

## Synthetic component evidence

Captures below render the integrated Swing components at **225 px**, with authored
synthetic account inputs and fallback sprites. They are not Petter's account,
real RuneLite screenshots or player acceptance. Expanded estimate basis and
normal/enlarged-font controls have measured horizontal/text-height checks.

- [Recommended overview](../images/ad1-r3b/recommended.png)
- [Automatic synapse component estimate](../images/ad1-r3b/synapse-automatic.png)
- [Automatic Zulrah component estimate](../images/ad1-r3b/fang-automatic.png)
- [Enlarged-font manual estimate / expanded basis](../images/ad1-r3b/large-font-acquisition.png)
- [DPS-first Soulreaper five-swing mean](../images/ad1-r3b/soul-ramp.png)

The complete [metric audit](integrated-metric-coverage.md) and [priority audit](integrated-priority-audit.md) generated by the combined suite include
all 54 active outcomes, with an owned-treads fixture for the primordial addition.
Original standalone audit files remain unchanged.

## RuneLite PC checklist

Close the development client, then update the existing clone:

```sh
git fetch origin
git switch main
git pull --ff-only
```

With JDK 11, run `./gradlew test build` then `./gradlew run`;
PowerShell uses `.\gradlew.bat test build` and `.\gradlew.bat run`.

- [ ] Open without a goal/manual pace; observe bank and inspect recommended
  order, meaningful baselines and conditional versus confirmed ownership.
- [ ] Impact: reproduce the actual Magus/Brimstone and ranged comparisons with
  explicit complete inputs; check relative/absolute DPS, zero/loss, setup changes,
  changed off-hand/ammo/loading and named model gaps.
- [ ] Acquisition: choose an eligible automatic method, inspect component scope,
  remaining work, mean and estimate basis. Apply matching manual pace/range and
  Clear; change method/account and confirm no transfer of stale pace or results.
- [ ] Melee: exercise supported Fang/Scythe contexts, task/Salve, Soulreaper
  stacks/ramp, compatible hand changes, partial components/Oathplate quantities,
  primordial treads addition and blood-fury utility with negative ordinary DPS.
- [ ] Account corrections, Browse search/facets, keyboard, Back/focus/scroll,
  genuine sprites and async updates at native width and enlarged fonts/scaling.
  Switch profile/character and verify prior references, manual pace, imagery and
  experimental recent-boss evidence clear.
- [ ] Contrast midgame, post-Bowfa, single-megarare, developed/nearly-complete,
  partial-observation and owned-component accounts; evaluate important omissions
  as well as the first choices. Do not save private bank/account dumps in evidence.

## Remaining work

**Joint AD1/R8 acceptance remains pending:** actual reported account inputs,
reference-image comparison, real RuneLite sprites/scaling/focus/profile checks,
full repeat-session benchmark applicability and player usefulness. Synthetic
component images/tests cannot establish these passes.

**AD1b-B1–B4 remain partial delivery:** additional compatible methods; reviewed
personal raid/team probabilities and complete-cycle assumptions; stateful/task
components; new activity/production/resource/assembly work and supported route
totals. Integrated R3b acquisition omissions are assigned to those same existing
work items, not an unexplained new backlog. See TASKS and the generated matrix.

**Existing model/coverage limits remain:** Confliction and other named unsupported
families/variants, dynamic raid scaling, specials, quantitative healing/supplies,
finite-target time/trip models and KI1 actual-client/KI2/KI3/broader-boss acceptance.
No new feature area or Hub publication is started by this integration.
