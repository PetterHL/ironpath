# Nex damage callback follow-up validation

23 October 2026. Build **0.29.0** follows PR #51 at exactly
`feb439bdc93278e05b08844a0ab941f5593adf87`; underlying PR #50 is
`f02028dd0af74e358e69e8b268bdcae64c669761`. Main checked at `7f68d0e`.
[Research, source revisions and decisions](../research/nex-damage-observation.md).
The original offline delivery is recorded below. The supplied 0.29.0 live report
now confirms repeated hitsplat-object use and a complete per-target damage/healing
accounting check for one kill; see the dated live check. Its findings do not
quantify all losses in the earlier 0.28.3 kill or establish general coverage.

## Verification scope

Focused regressions cover equal-damage/multiple same-tick deliveries, explicitly
replayed deliveries, synthetic mutable reuse with frozen amounts/forms, own/other/
heal/unclassified/zero/invalid payloads, non-NPC/outside/view/untracked guards and
post-death exclusion. Additional cases check object/observation overflow without
damage loss, counter saturation without capture capacity, smoke/next-attempt
clearing, immutable snapshots, nullable projectile actors, exclusions and no
ranged match from UNKNOWN. JSON/UI source agreement and explicit unavailable
whole-kill contribution are checked.

PR #51's earlier three fixes remain unchanged. Existing regressions exercise
1,200 HUD-enabled ticks with diagnostic overflow, replacing an already populated
plan's saved impact/supply confirmations, and Soul Split/Deflect Melee separation.
All three passed again in this correction round; their PR #51 review threads
were marked resolved on 23 October after verification.

The long production clipboard test still parses a single 1,500-tick capture,
preserves 370 useful callbacks and omits only 1,495 identical polls, independent
of page 51. Maximum changing-state/callback bounds, allowlist redaction,
pending/hidden-state, lifecycle and clipboard regressions remain active. Native
DEV1 checks the Unverified headline at fonts 12/20 and includes it in the 225 × 600
Encounter viewport; full-height Diagnostics and reports retain observed sums.

Required commands, run sequentially using the prepared JDK 11 environment:

```sh
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --no-daemon
./gradlew test build --no-daemon
```

Both final commands passed sequentially on 23 October using **JDK 11.0.32.1 /
Gradle 8.10**. The pinned run resolved **RuneLite 1.13.2**; the normal
`latest.release` run resolved **1.13.3**, independently verified with
`dependencyInsight`. Both executed **410 tests / 47 suites, zero failures,
errors or skips**. DEV1 passed **14 scenarios / 1,137 checks**, generating
**244 PNGs**, including **64 native 225 × 600 viewports**.

The final Encounter viewports/full-height views and expanded Diagnostics were
visually inspected at fonts **12 and 20**. The Unverified headline and observed
damage scope are readable; ordinary equipment results and controls remain.
[Selected native previews and provenance](images/nex-damage-52/README.md) retain
the unmodified viewport PNGs. Full-height output stays in the CI artifact.
All **26 generated copied-report JSON blocks** parsed with schema, coverage,
source and retained/exported/omitted-count checks. Existing production Copy,
redaction, page independence and clipboard fallback regressions passed.
Changed-document relative links and `git diff --check` pass.

Early runs caught stale version/ratio assertions and a new preview assertion
using the wrong Swing component type; those were corrected before the final
commands. The final callback-limit regression also checks that the last omitted
projectile count survives capture clearing. Prior 403-test / 14-scenario results
remain scoped to 0.28.3.

Delivery is tracked by [issue #52](https://github.com/PetterHL/ironpath/issues/52)
and its focused follow-up PR. That PR records the exact delivered commit,
current-head CI run and artifact links. No raw live report or bytecode dump is
included in Git.

## Live one-copy check — 23 October 2026

One user-supplied report identifies **LIVE_PUBLIC_CLIENT / READY**, build
**0.29.0**, adapter `nex-public-9`, RuneLite **1.13.2**, export `nex-evidence-2`.
The reviewed code delivery is `14cd28b23d00af6fba4abced1cd074ae8b25d7b4`;
the report itself embeds a build version, not a commit SHA. No kill timestamp is
recorded; 23 October is the review date. The original report remains outside Git
(SHA-256 `ce2920c5bec6d646cd3455f40bd45ae4f71d92b2cbbc21515a2398305b957776`).
Only bounded aggregate findings and relative tick offsets are documented here.

**Capture/export:** COMPLETE; **567 / 567** sampled/expected ticks, **340.2 s**
from observed smoke to dying, all five phases. The JSON parses with strictly
increasing retained callback order: **1,559 retained observations, 1,406 exported
callbacks, 153 unchanged polls omitted, zero export omissions**. Neither capture
nor correlation is truncated/limited; no callback counter is at its saturation
limit. The 860 main and 10 pre-start state rows remain within the 1,024-row bound.
This verifies one-copy ingestion for this report, not repeated clipboard or
bank/reload behavior.

**Hitsplat correction:** **513** callbacks on the five tracked targets include
**487 repeated-object callbacks**. All **171 own impacts / 2,090 damage** are
retained, including **52 zero-damage impacts**. There are **313 other-classified
hits / 4,184 damage** and **29 healing callbacks / 874 healing**. No tracked target
hits are rejected by identity, missing/view/tracking guards, unclassified type or
capture boundaries; no own impact is omitted. The outside/non-NPC rejection
counts remain separate and do not enter these totals.

| Target | Own damage | Other damage | Healing | Damage minus healing |
|---|---:|---:|---:|---:|
| Nex | 1,404 | 2,865 | 869 | 3,400 |
| Fumus | 177 | 323 | 0 | 500 |
| Umbra | 194 | 306 | 0 | 500 |
| Cruor | 150 | 355 | 5 | 500 |
| Glacies | 165 | 335 | 0 | 500 |
| Total | 2,090 | 4,184 | 874 | 5,400 |

Thus **6,274 damage − 874 healing = 5,400**, matching the reviewed initial HP
of Nex and four minions, independently for each target. This is strong
single-kill evidence for the corrected callback handling and damage accounting.
It confirms live object reuse and that the old fight-long identity gate would
reject repeated objects. It does not reconstruct the earlier kill, prove every
callback's causal owner, exclude offsetting errors, establish universal coverage
or validate server contribution/loot semantics. The recorded **33.3121%** ratio
remains diagnostic; the Unverified headline and null whole-kill contribution stay.

**Remaining observation evidence:** all **364 retained projectiles** have
`source=UNKNOWN`; there is no identified LOCAL flight. Existing ranged links
therefore remain unavailable/candidates, never inferred from animation or ID.
Zero HUD bindings were accepted: 270 script callbacks split into 136 no-fresh-
variable and 134 hidden-widget rejections. The disabled counter is zero; this
report does not show that a HUD setting was disabled. Quantized actor-bar
readings remain available.

**Resource follow-up from the existing capture:** energy reaches 1,000 at relative
tick 129, falls to 500 at tick 137 and rises to 600 at tick 162: the next +10%
reading is **25 ticks after spending from full**. In contrast, spending at tick
224 follows a regeneration reading at 212 and precedes the next at 237, retaining
the 25-tick interval while below full. These are observed sequences relevant to
the model's full-cap clock; they do not establish a general replacement rule.
At Cruor's tick-290 death, +150 energy and Death Charge active 2→1 occur in the
same cycle; Umbra's tick-178 death has no matching restoration/active transition.
Six Voidwaker 11275 animation candidates coincide with energy drops, retaining
immutable callback gear and settled-state distinctions. These observations do
not automatically confirm spec execution, eligible deaths, casts, procs or expiry.
No SpecTimeline, Death Charge kernel or automatic readiness gate is changed here.

Checks performed for this documentation update: reparse the supplied JSON;
verify chronology, retention counts, every target's arithmetic, reused-object
counts, all own impact sums/zero hits, saturation flags, source uncertainty and
resource offsets; validate changed-document links and `git diff --check`.
The 410-test / 14-scenario results above remain scoped to the code delivery.
No new gameplay, automated test result or implementation change is claimed by
this documentation-only update.

## Remaining work and deferred live checks

The user agreed to continue analysis/documentation from this report. **A second
kill is not required to accept this single-kill hitsplat correction or continue
offline work.** Bank return, a subsequent capture and arena reload remain a
separate lifecycle acceptance item, deferred until gameplay is convenient.

- Investigate projectile source availability/timing from public API/source evidence;
  retain UNKNOWN and the existing filter/correlation scope meanwhile.
- Keep integer Nex HUD validity open; use actor-bar bounds without guessing exact HP.
- Research full-energy regeneration and Death Charge eligibility/active transitions
  separately, using the existing report before requesting another targeted capture.
- Broader lifecycle acceptance, whole-kill/server contribution, automatic
  recommendations, consumption and N4/time estimates remain open.

No merge or Plugin Hub publication is authorized.
