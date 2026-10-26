# Observation and kill-impact contract

Issue #58 (26 October 2026), build **0.33.0**, supersedes conflicting older
HP-interval, ownership-label and blanket time-estimate guidance below. Current
ordinary comparisons use the full accuracy-weighted damage distribution, one
observed baseline per sample, a unified equipment list and role-specific curated
candidates plus manual additions. Independent resource, mapping and lifecycle
guards remain. [Implementation, sources and validation](validation/encounter-equipment-advice.md).


Current direction: Nex Trio MP2 pilot, 15 October 2026. Delivery lives only in
[TASKS](../TASKS.md). The [previous research](archive/pre-nex-20261015/KILL_IMPACT.md)
retains its source work; the former optional-boss product placement is superseded.

## Observation

Build 0.28.0 adds [bounded candidate observation](research/nex-observation.md):
separate HUD/bar HP sources, launch packages and tick reconciliation, distinct
projectiles/zero hits, smoke-callback stock and raw Death Charge/server resources.
The [client procedure](validation/nex-observation.md) remains open. Candidate
matching never upgrades readiness, consumption or a hypothetical impact to observed.

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
Build 0.29.0 withholds the whole-kill Damage contribution headline as Unverified;
recorded local/total sums and their ratio remain diagnostic evidence with
unverified coverage. Callback counters locate losses but cannot establish coverage
by themselves. [Source/support review](research/nex-damage-observation.md).

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
callbacks cannot affect a new trace. Waiting to open the bank or a long recognized
return leg does not expire bounded completed history. Outbound arena-like evidence
remains suspended until bank verification. A scene reload with arena evidence at
its origin may resume in the same arena without banking; an observed exit revokes
that permission, while temporarily missing NPCs do not establish an exit. Logout,
disconnect/ambiguous identity, account or profile change, unsupported worlds,
disable and Reset clear the session.
Changed method/setup/team evidence separates comparable cohorts; unknown teammate
composition must remain explicit rather than assumed identical across kills.

## Outputs

UI1 reports only supported state/package metrics; a largest relative DPS change
across unrelated states is not the largest whole-kill improvement. Cards identify
the actual changed item/package and retain scope. N3b/N3c conditional APIs do not
become automatically attributable personal recommendations through a UI change.

### Explicit diagnostic export (UI1, 0.20.0)

Raw account observations/traces remain memory-only. UI1 is narrowly scoped to a
user-triggered, bounded clipboard report of allowlisted diagnostic/calculation
facts, not persistent session logging, telemetry, raw trace export or a recording.
No automatic upload or file saving. Version the report schema and record relevant
plugin/build, adapter/model/catalogue versions, outcome/timer/damage scope, manual
assumptions, coverage/omissions and the displayed comparison's exact relevant
inputs/results. Bind export to its immutable kill/result/input revision.

Use report-local references; omit names, account/profile IDs, roster fingerprints,
cohort strings, unrestricted signal logs and inventory/bank dumps. Existing roster
hashes are not anonymous export IDs. Preserve unknown versus observed/assumed/
synthetic provenance. Export time is not the kill timestamp. Clear/hidden session
state cannot be recovered through export. Formatting can be shared with DEV1,
but a diagnostic report cannot reproduce a lossless ordered event replay.

The version-2 production report explicitly projects numeric setup/state inputs,
per-metric support and outputs, relevant candidate quantities and source/model
revisions. Adapter exclusions and comparison gaps are mapped to fixed allowlisted
reasons/categories; arbitrary reason strings and embedded target identities never
leave the application. Narrative fields retain a 24,000-character budget, with whole fields omitted and
a visible omission count. Build 0.28.3 adds a separate whole-capture allowlisted
JSON evidence block; fixed capture bounds and deduplicated state keep the entire
report within 8,000,000 characters. Only unchanged resource polls are omitted,
with explicit counts and upstream capture/link truncation flags. Only the selected comparison
is exported; unrelated scenarios and ownership/unlock provenance are explicitly
omitted. This is sufficient for scoped diagnosis, not a complete calculation or
event replay. DEV1 alone writes synthetic reports to its build artifacts.

### Corrected context (CTX1, 0.21.0)

Immutable evidence never changes when a user annotates strategy/team size.
Snapshot future defaults at start; apply later edits as versioned per-kill
metadata. Re-evaluate only dependent supported metrics from preserved inputs;
do not silently substitute newly observed ownership/preferences. Keep pending
results separate from the previous revision. Reconcile comparable aggregates
without treating team count as proof of participant identity or stable damage.
Unsupported methods preserve observations and explicitly justified arithmetic,
not pilot-specific recommendations. Per-kill metadata has the session's bounded
memory/identity/reset/eviction lifetime. NexEvaluation freezes account/planning
inputs once per completed kill; later bank/Setup changes apply to future
comparisons. The schema-2 report includes attempt-start defaults, effective
per-field provenance and context/input/model revisions; pending or mismatched
outputs cannot be exported. [Automated acceptance and remaining checks](validation/ctx1-kill-context.md).

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

N3b adds [conditional attack/stock arithmetic](research/nex-spec-damage.md).
Expected damage is a sum of supplied single-launch expectations under explicit
HP/window/package and saving/charge conditions. Never feed expected damage back
as actual remaining HP. Ruby self-damage is conditional cost, not a survival or
healing model. Resource branch probabilities describe specified independent
retrieval/charge rolls only, not encounter uncertainty or upgrade confidence.
Runtime build 0.26.0 permits independently declared per-opportunity HP/caps/ruby
and Zaros conditions through those same shared steps. A missing row remains
unknown; fixed/previous rows cannot supply it. Reports use standalone schema 5
to distinguish the selected mode and every declared opportunity. This is an
input contract, not automatic attribution or a simulated HP/stat/overhead path.
Build 0.27.0 distinguishes Magic-type Voidwaker specials from ordinary melee
under a declared Deflect Melee impact. The same weapon's admitted ordinary fallback
retains a damage gap; rejected fallbacks contribute none. Independent resource
accounting survives, and an unsupported baseline still withholds pairwise damage.
No reflected-damage amount or survival result is inferred.
Compare complete alternative plans under the same declared conditions; do not
add a spec's damage to every ordinary baseline attack it displaces.

N3c now derives those inputs from complete actual/owned/hypothetical packages
under one explicit conditions declaration. Its supported/negative/zero deltas
remain conditional launch-plan expectations. No worn-duration/energy-delta attack
inference or automatic client recommendation is introduced. Unknown unused pools
are not empty stock; selected shared stock and packing must be explicit.
[N3c numerical contract](research/nex-packages.md).

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
