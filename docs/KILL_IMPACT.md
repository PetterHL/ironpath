# Observation and kill-impact contract

Current direction: Nex Trio MP2 pilot, 15 October 2026. Delivery lives only in
[TASKS N1–N4](../TASKS.md). The [previous research](archive/pre-nex-20261015/KILL_IMPACT.md)
retains its source work; the former optional-boss product placement is superseded.

## Observation

Record encounter/spawn identity, method, phase/subphase, target, completion status,
actually worn states, current levels/prayers, relevant ammo and spec-energy changes.
Distinguish attack launch evidence from delayed impacts. A hitsplat has no universal
weapon/attack identity; current equipment at impact is not enough attribution.
Food/potion use needs corroboration; inventory loss alone may be a drop or banking.

Nex, minions and other targets must have explicit inclusion rules. MP2 contains
melee and ranged windows within shadow phase; time spent wearing a style is not
its share of useful attacks. Keep observed/proposed states separate.

Nex Droprate Calculator demonstrates public-event feasibility, not an authoritative
server contribution API. Observed local damage divided by observed total damage
must name its target/damage coverage. Thralls, damage-over-time, healing, missed
events, overkill and eligibility/MVP rules require review. Do not equate this ratio
with loot credit, drop probability or a guaranteed server value.

## Session

Keep bounded summaries only in memory. Show a mean over comparable complete kills,
sample count and spread; distinguish the mean of per-kill percentages from aggregate
damage share. Failed, abandoned and censored attempts are counted separately, never
silently inserted as fast completions or zero damage. Keep timer origins consistent.
For `nex-public-4`, the observed interval is the smoke announcement to tracked
Nex death/dying. Arena entry is diagnostic; arena completion corroborates the
end after a bounded dying delay. Pre-smoke states are retained separately and
excluded from fight metrics/comparisons. No game-timer calibration is claimed.

For one-kill trips, N1 must preserve completed summaries across recognized ordinary
banking transitions. End/censor active traces, keep clearing live account/reference
data, suspend access and require same-account/profile/world revalidation. Old-generation
callbacks cannot affect a new trace. Logout, disconnect/ambiguous identity, account
or profile change, unsupported worlds, disable and Reset clear the session.
Changed method/setup/team evidence separates comparable cohorts; unknown teammate
composition must remain explicit rather than assumed identical across kills.

## Outputs

| Output | Required meaning |
|---|---|
| Observed duration/share | What the validated capture actually recorded, with coverage and timer scope |
| Modeled offence | Supported attacks for exact compatible setup, target, phase, boosts and mechanics |
| Estimated contribution | Counterfactual personal share under explicit player/team/target assumptions |
| Estimated team time | Expected change for comparable kills; not personal DPS multiplied into total time |

Compute actually-used versus owned improvements separately from owned versus new
upgrade. Re-evaluate deliberate multi-item packages together. Each independent
candidate uses the same baseline; its gain cannot be summed with other cards.
Missing models withhold dependent metrics only; a best-owned claim needs adequate
coverage of relevant alternatives, while narrower pairwise results can survive.

Specs require starting energy, regeneration phase, Lightbearer wear, Death Charge
casts/procs and eligible deaths, caps/waste, attack opportunities and displaced
ordinary attacks. Upgraded Death Charge availability is not proof it was cast or
that both procs occurred. ZCB bolt outcomes need HP/cap/accuracy/phase handling.
Food, brews/restores and alternative supply plans need delay/boost/resource support.

## Time and validation

N3a distinguishes sampled resource changes from attributed events: net energy
deltas do not identify spending, regeneration, specs or Death Charge procs. Its
finite scenario kernel requires explicit initial conditions and opportunities;
shared energy/cap/delay consistency does not establish spec damage, an executable
encounter or team-time value. [Resource contract](research/nex-spec-resources.md).

Do not scale the last kill by a DPS ratio, use PB as the average or turn combat
duration into acquisition throughput. Handle finite HP, rounding, overkill, attack
timing, phase changes, mandatory windows and teammate damage where material.
Unknown phase/team effects do not become zero. Avoid decimal-second precision
when the capture/model cannot justify it.

Freeze the proposed model before held-out testing. Check identical setups, negative
gains, compatible switches, event ordering and lifecycle boundaries. Use matched
blocks alternating actually owned setups to test upgrade deltas, controlling known
method/team differences. Report counts, exclusions, bias and uncertainty type.
More observations reduce sampling noise, not model bias or missing mechanics.
A good baseline prediction alone does not validate a small upgrade effect.

Record real-client event mappings, capture coverage and player usefulness separately
from synthetic tests. N1 development build implements bounded public-event capture and session summaries;
[its report](validation/nex-session.md) separates synthetic verification from pending
real-client/player validation. N2 now supplies conditional per-state ordinary offence against the documented
Nex base-stat scenario. It does not weight by worn time, attribute attacks or
produce contribution/time. Those dependent encounter outputs remain N3–N4.
[N2 evidence and exact limits](validation/nex-owned.md).
