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

| Existing area | Disposition and concrete next use |
|---|---|
| AccountSnapshotReader, AccountSnapshot, Observation | Retain scoped ownership, slots/quantities and public unlock evidence |
| ObservationSession, preferences, plugin generation guards | Retain identity/privacy protections; NexSession now supplies a separately tested summary lifetime |
| CombatStateReader, immutable CombatState | Shared lightweight client-state reading extracted in T0; Nex extends evidence when mappings are validated |
| EncounterCapture / Context / Summary | Bounded immutable concepts reused by NexCapture/Summary; adapter and banking need live validation |
| ZilyanaObserver | Retained opt-in experiment; its single-target lifecycle is explicitly not Nex support |
| ZilyanaWornStateComparison | Retained ordinary-Zilyana comparison with a context guard; longest worn segment is not a Nex baseline |
| CombatSetup, ranged/melee/magic models and comparison services | Reuse validated arithmetic, source dependencies and compatible-package handling; close actual Nex blockers |
| KnowledgeCatalog, route/acquisition models, source registers | Retain reviewed facts, provenance and useful paths; no second catalogue or new global-grind mandate |
| Advisor, progression ranking and pursuit presentation | Transitional consumers; replace their primary-flow policy in N2, then remove orphaned code |
| IronpathPanel, RecentBossPanel, current navigation | NexPanel replaces the toolbar root in N1; retained presenters support N2 replacement/removal |
| CompactRowLayout | Removed in T0: no production or test references |

Keep observation, encounter summaries, comparisons and presentation separate.
Boss identifiers and event correlations belong in adapters, not reusable formulas.
Do not prebuild a generic rules engine. Extract shared code when it has a concrete
consumer, preserving its semantics and tests.

## Invariants

- Public client reads occur on the client thread; workers/Swing receive immutable
  data and check identity, generation and encounter keys before publishing.
- Account observations and raw combat traces stay in memory. Persist explicit
  method choices/manual facts only, using existing guarded local preference IO.
- N1 must retain completed summaries across recognized ordinary banking, while
  ending active traces and revalidating the same character/profile. Ambiguous
  identity and logout clear data. N1 suspends summaries during recognized bank travel; live account state still resets on loading.
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
