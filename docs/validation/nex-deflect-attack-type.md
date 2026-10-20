# N3 Deflect Melee attack types

20 October 2026. Development build **0.27.0**, for review in PR #50, extends
`4c3f549d7b1247f71da2eb9311458e9cad41308a` on consolidated main `7f68d0e`.
The PR records the delivered commit and current CI artifact.
[Contract/source review](../research/nex-decision-support.md#deflect-melee-and-attack-type)
and [previous impact-condition evidence](nex-impact-conditions.md).

## Result and evidence

The Zaros deflection gate now considers the actual attack after validating its
exact weapon. Voidwaker's Disrupt deals guaranteed Magic damage while deriving
its distribution from that package's melee max. It no longer inherits the
ordinary-melee gap merely from the equipment family. Existing melee/ranged
formulae, finite energy/ammo/supplies, HP/caps and explicit impact declarations
remain unchanged. No new controls or automatically confirmed inputs are added.

This narrow interaction is independently derived from Jagex's released Disrupt
damage type and the reviewed melee-only scope of Nex's curse. The pinned reference
separates stat family and damage type as well. It is not a newly measured server
exception or a rule for every special attack. See the source table in the contract
for retrieval limits. No external implementation, article text or dataset is copied.

Ordinary melee remains unsupported under Deflect Melee, including the same
Voidwaker's ordinary fallback. Rejected fallbacks add no damage gap. If energy
cannot fund a special and the ordinary fallback is admitted, the existing ledger
preserves finite resources and supported earlier hits but withholds total damage.
An unsupported captured/owned baseline still withholds a marginal gain even when
the hypothetical special is supported. Reflected damage is not treated as zero
for an unsupported ordinary hit.

Bridge version **nex-package-7** and runtime **nex-declared-plan-6** identify the
changed calculation/report rule. Standalone report schema 5 keeps the same bounded
allowlisted fields and adds the attack-type explanation. NPC forms/health ratios
remain captured evidence, distinct from every independently declared impact.

## Verification

Focused bridge/runtime tests cover independently calculated complete-package max
35 → uniform 17..52 → mean 34.5, and owned max 38 → uniform 19..57 → mean 38.
The pairwise gain is 3.5. A separately supplied 10 HP clips every outcome to 10.
Two funded specials spend 100% energy and contribute 69 expected damage; their
rejected same-tick ordinary fallbacks do not add gaps. Starting at 50% admits one
special and then an unsupported ordinary fallback, preserving spending while
withholding total damage. Fixed/per-attack declarations, unknown overhead and
unconfirmed inputs retain their distinct gates.

The existing `nex-n3-plan` DEV1 scenario exercises public capture → frozen package
→ native form → real worker → guarded results/export at 225px and fonts 12/20.
Funded and unfunded Deflect Melee cases include separate HP caps, an unsupported
Fang baseline, withheld deltas, report rule/versions and new-kill report freezing.
Existing edit/Reset/request guards continue in the same replay.

Pinned full validation passed with JDK **11.0.32.1**, Gradle **8.10** and
RuneLite **1.13.2**:

```sh
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --no-daemon
./gradlew test build --no-daemon
```

**380 tests / 44 suites; 14 DEV1 scenarios / 1,109 checks; zero failures,
errors or skips.** The standalone run produced **240 PNGs**, including **62
native 225 × 600 viewports**. Full-height funded/fallback result screens at both
fonts and the selectable-export viewports were inspected. The supported
hypothetical damage and withheld ordinary baseline/deltas remain distinct.
Existing Fontconfig/deprecation messages were non-fatal.
The separate normal compatibility command also passed, retaining the same
RuneLite dependency/classpath and reusing the successful test/build outputs.
This is not compatibility evidence for a newer RuneLite release.

The first focused test exposed an exact-equality assertion on the probability
sum `10.000000000000004`; the independent expectation remains 10, with the same
`1e-12` tolerance used for other distribution means. The DEV1 cap check asserts
both outcome bounds are exactly 10. No game formula was changed to match a test.
Changed-document links and `git diff --check` pass. Full synthetic reports and
PNGs belong to the current CI artifact; no routine output is added to Git.

## Remaining acceptance

Live RuneLite/server/player agreement, real stock/health/form readings,
DPI/focus/scroll/clipboard and usefulness remain open. No legal attack attribution,
exact NPC HP, initial-readiness inference, overhead timing, ordinary-melee
reflection, Soul Split healing, Turmoil transfer, healing/survival trajectory,
automatic recommendations or N4 contribution/team-time output is enabled.
No merge or Plugin Hub publication.
