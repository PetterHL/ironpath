# IronPath design

Decision: 15 October 2026. **Encounter and method become the primary product
context.** The user approved this transition and the Nex pilot. This replaces the
old account-wide automatic-grind homepage and optional recent-boss direction.
[TASKS](../TASKS.md) owns delivery; [history](archive/pre-nex-20261015/README.md)
preserves previous decisions and evidence.

## Product contract

For a selected boss/raid and method, help the player use owned equipment, food,
potions and spells better; identify relevant new upgrades; explain their effect
on that play. Observe automatically, remember explicit choices, and permit focused
corrections/locks. Post-kill evidence improves relevance without requiring a long
setup form every session. Advice appears after completion.

The pilot is Nex Trio MP2, one kill per trip, faster team kills and higher personal
contribution. Lightbearer stays equipped; upgraded Death Charge is confirmed by
the user. Longer trips are not this profile's objective. Further methods and boss/
raid contexts remain possible without turning the pilot into a universal simulator.

The 19 October sidebar simplification removes the Gear & method (Setup) and
Gear & unlocks (Account) editors and navigation. Automatic gear/unlock observations,
stored preferences/manual facts, planning locks, upgraded Death Charge assumptions
and calculations remain intact. There are no replacement controls; existing kill
details and comparisons retain observed evidence and frozen planning inputs.

## Three distinct setups

| Setup | Meaning |
|---|---|
| Actually used | Immutable observed timeline, including switches and unknowns |
| Best relevant owned | Compatible method package proposed from observed ownership and explicit assumptions |
| Owned plus upgrade | That owned baseline re-evaluated with a new item or deliberate package |

Show actual-to-owned improvement separately from the marginal new grind. Compare
whole melee/ranged/spec packages, hands, sets, ammo and shared inventory/energy.
Empty offhand during a two-handed switch says nothing about another switch's
defender. Item presence does not establish charges, supplies or permanent unlocks.
Locked choices constrain proposals, not the recorded history.

A partial search can establish the best supported option, not the best among all
owned gear. Missing support blocks dependent claims. Explicit supported pairwise
comparisons remain useful even when a broader optimum is unknown. Never discard
an item silently to manufacture a weaker baseline.

## Current presentation (19 October 2026)

PR #48’s approved follow-up uses one native Encounter sidebar: Last kill stays
above independent collapsible Compare setups (open), Team & method, Gear & method,
Gear & unlocks and Diagnostics (initially closed, Diagnostics last). Persistent
headers/editor/report subtrees retain expansion, draft, selection and scroll. A
non-private in-memory session scope distinguishes verified travel from hard
identity/world/reset boundaries; suspended evidence stays hidden and exports clear.
Ready/status labels come from actual client/session readiness, including verified
Waiting for Nex. Invalid inputs report their field bounds; correction provenance
stays in Diagnostics. [Follow-up evidence](validation/pr48-review-followup.md).

Issue #47 supersedes earlier per-card help/scope prose. Show the result, compact
origin/role labels and actions in normal screens. Put calculation definitions,
assumptions, pilot restrictions, provenance, long gaps and raw traces in common
Diagnostics/explicit reports. Damage contribution is the existing observed ratio,
not a new contribution estimate. Centre headings, values, sprite/name groups and
actions inside the 225px column; keep forms and technical text left aligned.
[Narrow native evidence](validation/ui1-47-wording-layout.md) and [UX](UX.md).

The integration preserves N3's frozen unlock facts through CTX1's NexEvaluation
wrapper, as well as context/input/model revisions and pending Diagnostics defaults.
A presentation change cannot drop either lineage or enable unsupported numbers.

## Code boundaries and transition disposition

### UI1 / CTX1 implementation (18 October 2026)

UI1 (0.20.0) consumes immutable results through NexPresentation and the allowlisted
NexReport. CTX1 (0.21.0) adds NexContext, NexRecord and NexEvaluation. Swing actions
never calculate or read account state. DEV1 reuses production formatting; its
runner and assertions remain test-only.

NexContext keeps strategy, group mode and exact/variable/unknown participation
separate, with per-field unknown/inherited/manual-unconfirmed/annotated provenance.
Preferences schema 4 stores only future values; schemas 1–3 migrate the fixed pilot
as unconfirmed inheritance. NexCapture snapshots defaults at attempt start, including
before a later smoke re-anchor. NexSummary and all observed facts remain immutable.

NexRecord holds the effective annotation revision and frozen NexEvaluation inputs
for each retained kill. Inputs freeze at its first post-kill evaluation, including
ownership, planning preferences and model versions. Later bank/account/Setup changes
apply to future comparisons. A correction evaluates those preserved inputs on the
worker, recalculates only context support/descriptive matching, and publishes under
identity/world/session, kill, annotation, input and model guards. Pending corrected
outputs cannot display/export a previous revision. Historical context edits and
future defaults use a dedicated action; neither calls NexObserver.method() nor
interrupts another active attempt. A report and its selected calculation share revisions.

NexComparability replaces string-only grouping with explicit adapter/timer,
observed worn package, level/prayer/style/projectile and stable roster evidence,
plus confirmed organized strategy and exact count. Unknown, variable, mass,
unspecified Other, incomplete or incompatible evidence stays out of matched means.
Planning locks are excluded. These are descriptive matches: equal count and visible
roster do not prove party membership or equal teammate damage. No causal team model
is introduced. Unsupported context retains observations and independently justified
ordinary state arithmetic; it cannot enable Trio MP2 recommendations or team-time.

Annotations, inputs and calculations share the session's 50-record in-memory bound,
banking visibility rules and identity/world/logout/reset/disable lifetime. They
are evicted together. There is no persisted performance history or arbitrary
history selector. [Validation and limitations](validation/ctx1-kill-context.md).

| Existing area | Disposition and concrete next use |
|---|---|
| AccountSnapshotReader, AccountSnapshot, Observation | Retain scoped ownership, slots/quantities and public unlock evidence |
| ObservationSession, preferences, plugin generation guards | Retain identity/privacy protections; NexSession now supplies a separately tested summary lifetime |
| CombatStateReader, immutable CombatState | Shared lightweight client-state reading extracted in T0; Nex extends evidence when mappings are validated |
| EncounterCapture / Context / Summary | Bounded immutable concepts reused by NexCapture/Summary; adapter and banking need live validation |
| ZilyanaObserver / EncounterCapture | Retained adapter/capture regression reference; no runtime collector or navigation in the Nex plugin |
| ZilyanaWornStateComparison | Removed in N2 with its global-policy consumer; no longest-worn Nex baseline |
| CombatSetup, ranged/melee/magic models and comparison services | Reuse validated arithmetic, source dependencies and compatible-package handling; close actual Nex blockers |
| KnowledgeCatalog, route/acquisition models, source registers | Retain reviewed facts, provenance and useful paths; no second catalogue or new global-grind mandate |
| Advisor, progression ranking and pursuit presentation | Removed in N2 with obsolete global-policy tests. AccountFacts retains the pure fact-precedence contract; acquisition scope/data/kernels remain |
| IronpathPanel, RecentBossPanel, old tiles/deck/Browse | Removed in N2; NexPanel is the sole product view |
| CompactRowLayout | Removed in T0: no production or test references |

Keep observation, encounter summaries, comparisons and presentation separate.
N3a's `NexResourceReview` summarizes immutable captured resources on the post-kill
worker. `SpecTimeline` independently checks explicitly supplied resource/delay
scenarios; it does not attribute captured deltas or provide damage/upgrade advice.
The native resource details consume frozen evidence, with the existing identity
and historical-view guards. See [N3a contract](research/nex-spec-resources.md).
Boss identifiers and event correlations belong in adapters, not reusable formulas.
N3b adds `SpecAttackModel` single-launch distributions and `SpecAttackPlan` finite
ammo/splinter branches joined to `SpecTimeline`. Exact stocks gate spending;
resource outcomes and legal opportunities are explicit scenario inputs. Complete
charged/uncharged package damage is supplied separately. No capture/worker advice
path invokes these kernels yet. See [N3b contract](research/nex-spec-damage.md).
N3c's `NexPackageComparison` is the complete-package caller of those kernels.
It shares `NexPackageFactory` with N2, derives bounds/rolls/delays from exact
catalogue-backed packages, and keeps actual-to-owned and owned-to-upgrade deltas
separate. One immutable conditions declaration, explicit usable joint stock and
source/assumption facts are required. Damage gaps preserve supported resources;
missing resource inputs preserve independent launch/package details. The optional NexPlanComparison runtime caller now freezes captured/N2 packages
and joins them under a separately declared post-kill plan. There is still no
automatic attack attribution or runtime weapon recommendation. See
[the runtime contract](research/nex-runtime-plans.md). See
[complete-package contract](research/nex-packages.md).
Do not prebuild a generic rules engine. Extract shared code when it has a concrete
consumer, preserving its semantics and tests.

### N3-ACTION implementation (24 October 2026)

`NexActions` adds a separate bounded immutable stream of local animation,
interaction, settled tick and typed message signals to the existing summary.
The adapter retains ten ticks / 128 rows of pre-start arena context; smoke does
not reset readiness. `NexActionTimeline` runs within `NexObservationReview` on the
worker and exposes provisional Fang attacks, exclusions/ambiguity, cooldown and
impact candidates. No availability-only attack, exact HP, proc/expiry or advice
is inferred. The schema-4 allowlisted report shares target/state tables and guards.
[Contract and pending mapping validation](validation/nex-action-evidence.md).

## Invariants

- Public client reads occur on the client thread; workers/Swing receive immutable
  data and check identity, generation and encounter keys before publishing.
- Account observations and raw combat traces stay in memory. Persist explicit
  method choices/manual facts only, using existing guarded local preference IO.
- N1 must retain completed summaries across recognized ordinary banking, while
  ending active traces and revalidating the same character/profile. Ambiguous
  identity and logout clear data. N1 suspends summaries during recognized bank
  travel without an opening deadline; live account state still resets on loading.
  A nearby Nex does not end outbound suspension before bank verification.
  See [0.19.1 bank retention](validation/nex-bank-retention.md).
- Damage, contribution, phase offence, expected team time and acquisition pace
  are separate. [Kill impact](KILL_IMPACT.md) defines their evidence requirements.
- Fewer eats, survival, longer trips and supplied resources cannot be inferred
  from offensive DPS alone. Unknown benefit is neither zero nor grounds for ranking
  an unsupported grind as a quantified priority.
- Use one local Java plugin and native Swing. Preserve licences; no backend,
  runtime AI, telemetry, automation or private-plugin scraping.

## Scope of T0

This transition changes project authority, archives the old plans, removes dead
layout code and separates shared state reading from the retained Zilyana example.
Existing formulas, catalogue/schema, persisted preferences and active advisor UI
remain compatible. Removing that active UI before N1 would leave no working flow.
Its replacement is explicit work, not an indefinite second product.

## N1 implementation boundary

Build 0.15.0 uses `NexPanel` as the only toolbar root. General ranking/homepage
navigation is retired. Native wrapping/buttons were extracted to `PanelWidgets`;
old presenters and model/ranking regressions remain for N2's contextual replacement
and removal, without an alternate runtime homepage. The opt-in Zilyana classes
remain regression/reference machinery and are not offered as Nex support.

`NexObserver` supplies explicit public-event identities to the pure bounded
`NexCapture`; `NexSession` owns the separately guarded completed-summary lifetime.
See the [event review](research/nex-events.md) and [validation](validation/nex-session.md).
Observed setups include every captured switch and unknown; no longest-worn baseline
or recommendation replaces them. Saved method/locks and manual facts use preference
schema 3. Raw state and performance remain memory-only. N2 owns package proposals;
N3 owns the finite energy/spec model; N4 owns validated time estimates.

## N2 implementation boundary

Build 0.16.0 evaluates on a worker and publishes only when account identity,
generation, account revision, preference inputs and Nex revision still match.
A superseded job stops searching; completed results/selected history are immutable.
No calculations run on Swing or the client thread. The optional Zilyana worker
and collector are no longer invoked.

`NexOwnedEvaluator` enumerates complete compatible supported packages for each
captured role/state. It never substitutes a reference for the observed timeline,
uses current visible levels (including decay/drains), keeps Lightbearer fixed and
checks available items, equip requirements, hands, ammo, set effects and additional
switch space. It preserves the original valid unlocked package before searching.
A computational work limit marks a partial search and never claims an optimum.
Carried and bank-preparation searches are distinct; bank candidates do not prove
joint packing, sufficient stock or immediate availability. Every pair is a separate
alternative, not an additive plan across states or roles.

The selected projectile can come from captured quiver counters while Ancient
blessing remains in the worn ammo slot. `ranged-2` adds this explicit input; existing
arithmetic with ordinary worn ammo/internal darts is unchanged. Exact public
counter mappings still need live checks. Spec/unknown roles, minions, Zaros and
special windows retain scoped gaps. Ordinary offence uses the documented Nex
base-stat scenario, not inferred defence debuffs or encounter attack opportunities.
See [the support/evidence contract](research/nex-owned.md).
