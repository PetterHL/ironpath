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

## Code boundaries and transition disposition

### UI1 implementation / planned CTX1 boundaries (18 October review)

UI1 (0.20.0) consumes existing immutable results through NexPresentation and
the immutable allowlisted NexReport model. NexOwnedResult retains the evaluation
revision and immutable planning inputs that produced it.
Do not add calculations or account reads to Swing/export actions. The production
allowlisted report formatter may be reused by DEV1; its runner/assertions remain
test-only. Reuse current arithmetic, native widgets, images and lifecycle guards.

CTX1 separates future defaults, frozen observations, planning locks and per-kill
annotations. Do not use NexObserver.method() for historical edits: its current
capture-censoring behavior is inappropriate. Preserve the evaluation inputs that
produced the inspected result; annotate/recompute under explicit revisions instead
of silently reading a newer bank/account/preferences snapshot. Retain session,
identity, generation and stale-publication checks alongside kill/context versions.

Current cohorts are opaque strings combining method/planning locks, observed
gear/levels/prayers and roster fingerprints. CTX1 must define typed comparability
rules and reconcile means on edits without modifying NexSummary evidence. Planning
locks are not observed performance; matching team count alone is not matching
teammates or damage. Keep unknown/incompatible evidence explicit. Support is
per output; changing a label cannot generalize the Trio MP2 model.

CTX1 remains planned; UI1 adds no annotations, cohort redesign or combat formulas.

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
