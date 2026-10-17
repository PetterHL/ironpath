# IronPath active work

Updated 17 October 2026. **This is the only active backlog.** The encounter-and-
method direction replaces the former AD1/R/KI delivery order. Historical work,
results and unperformed checks are preserved in the [archive](docs/archive/pre-nex-20261015/README.md).

## DEV1 — Offline scenarios and native UI previews (implemented for review)

Approved for implementation planning on 17 October 2026. Goal: Codex Cloud can
exercise IronPath and review calculations and the actual panel without requiring
Petter to log in and complete a Nex kill after every change. This supports N1–N4;
it does not change their product scope or close their outstanding live acceptance.

Treat this as one cohesive task with one review checkpoint, not a series of tiny
infrastructure deliveries. Start from current main, read AGENTS.md and the active
documents, and implement on a focused branch. Open a PR; do not merge automatically.

### First working delivery and review checkpoint

- [x] Reuse the current tests/fixtures to run one complete synthetic Nex encounter
  through the actual public-event adapter/capture, session, production evaluator
  and native Swing panel. Supply representative ranged/melee/spec-candidate
  switches, complete compatible inputs for at least one supported ordinary
  comparison, and explicit synthetic account/preferences state. Do not bypass
  completion detection by injecting a pre-completed summary for this scenario.
- [x] Provide a simple documented command to select/run the scenario and generate
  a concise calculation/check report plus PNG previews. Include encounter outcome,
  timer scope, damage totals/share, exclusion/gap reasons, comparison inputs/results
  and expected-versus-actual checks. Capture the main view and relevant comparison/
  detail states at native width (currently 225px), normal and enlarged fonts.
- [x] Run affected regressions and the normal build; inspect the generated images
  and fix problems introduced by this work. Present the report, screenshots, exact
  commands, versions and verification limits in the PR. Deliver a usable workflow,
  not only interfaces, scaffolding or a plan.

First delivery: `offlineReview --args='nex-complete'`, with an explicit development
RuneLite pin, checks and twelve 225px native previews. See the
[workflow instructions](docs/DEVELOPMENT.md#offline-review-dev1) and
[checkpoint evidence](docs/validation/dev1-offline-review.md). The checkpoint is
reviewed on 17 October 2026: all twelve revised CI images were inspected and a
local pinned run passed 220 tests and 65 checks. Petter then authorized continuing
the same DEV1 task. The full suite below extends that reviewed checkpoint.

**Review checkpoint:** Petter/reviewer assesses whether the outputs are useful and
whether a small calculation or UI change can be evaluated without gameplay.
Demonstrate this with an isolated temporary change or existing meaningful
regression; do not leave intentional faults or unrelated product changes committed.
Do not automatically replace expected results with current implementation output.
This review gate was met before the scenario expansion below. Codex should perform
its own test/fix iterations before presenting the first delivery; manual gameplay
is not required for this checkpoint.

### Complete the same workflow after review

Implemented as six additional scenarios in the same PR: unknown inputs, incomplete
attempt, recognized bank trip, identity/pending publication, Reset/pending worker
and frozen history. The default `offlineReview` command runs all seven scenarios.
[Current verification](docs/validation/dev1-offline-review.md#dev1-scenario-completion).

- [x] Add a compact scenario set for unknown bank/ammo/resources, an incomplete
  attempt, completed kill → recognized bank trip → next kill, identity reset while
  evaluation is pending, and a new result arriving during inspection of an earlier
  kill. Reuse existing lifecycle/regression coverage rather than duplicating it.
  Exercise controlled pending work so stale publication guards are actually tested.
- [x] Finish documentation in [Development](docs/DEVELOPMENT.md): Cloud setup,
  scenario commands, output locations, adding a scenario/encounter, input provenance,
  comparison/review procedure and remaining live checks. Record actual validation
  evidence and update this task's checkboxes; do not report blocked runs as passes.

### Implementation boundaries and reuse

Reuse NexCaptureTest, NexObserverTest, ObservationLifecycleTest, NexOwnedFixtures,
NexOwnedEvaluator, NexPanelTest and relevant resource/arithmetic tests after
checking current main. Summary-only fixtures remain useful for fast calculation/
UI checks, but label which layers they bypass. Use production models/components;
do not duplicate calculations or recreate the UI in a web app.

Keep scenario loading/execution, tick and same-tick event ordering, reports and
screenshot infrastructure reusable across encounters. Keep Nex signals, phase
names, completion rules and fixtures in the Nex-specific part. Future encounters
must not have to adopt NexSummary or Nex phase concepts. Inspect retained Zilyana
code as a second architectural reference; adding live boss support is not part
of DEV1. Extract only boundaries needed now, without a generic combat rules engine
or broad production rewrite.

Mock RuneLite dependencies at the boundary and provide the client state consumed
at each relevant callback, including event payloads that differ from polled state.
Keep deterministic control of queued work, reset state between scenarios and
render Swing on the EDT. Preserve production thread, identity, unknown-state and
publication guarantees. Keep fixture loading and runner controls development-only.

Label synthetic evidence and user-reported equipment separately from real observed
levels, quantities, charges and performance. Use independent existing oracles,
reviewed hand calculations and behavioral invariants for expectations. Unsupported
metrics must remain unsupported; DEV1 adds no spec-damage, survival, contribution
or team-time claims. Placeholder sprites must be identified explicitly.

Document JDK 11, Gradle/dependency preparation and consistent fonts for Cloud.
Record the actual RuneLite/model/catalogue versions and make regression runs
reproducible despite the current latest.release dependency. Prefer a narrow
development version control/locking approach, with newer-version compatibility
checked separately. Ensure preview outputs are correctly declared or explicitly
regenerated so Gradle up-to-date checks cannot silently omit required artifacts.
Reports/previews must be accessible for review, without committing routine generated
output or private data. If setup/network restrictions block execution, state the
exact blocker and what was not verified.

**Acceptance:** A prepared checkout can generate reviewable calculations and actual
panel images without OSRS credentials or gameplay. Relevant incorrect outputs fail
checks; incomplete kills stay excluded, valid bank trips retain history, stale
results cannot republish, and inspected results remain frozen as intended.
The same commands can support subsequent Codex changes. Actual RuneLite event
mappings, real sprites/DPI/focus/GPU behavior and player usefulness remain separate
live checks; existing N1–N4 acceptance is not waived.

**Later follow-up, outside DEV1:** Consider bounded, opt-in encounter recording
after the runner is useful. Current summaries are not lossless event recordings.
A future recorder needs ordered events and accompanying state, versioned anonymous
identities and an explicit development-export exception to the current memory-only
trace policy. Do not implement recording/export or relax that policy in DEV1.

## T0 — Transition (this change)

- [x] Record the accepted direction and replace conflicting active instructions.
- [x] Archive previous planning/evidence documents with a recoverable baseline.
- [x] Map retained foundations, transitional code and retirement points.
- [x] Remove the unused row layout; isolate boss-independent combat-state reading.
- [x] Name and scope the retained Zilyana observer/comparison explicitly.
- [x] Complete build, regression and documentation checks; record actual results
  in [the transition report](docs/validation/nex-transition.md).

This change does not implement Nex capture, new ranking/UI, banking-safe session
retention, Death Charge recognition or impact estimates. Those are N1–N4 below.

## N1 — Understand the real Nex session (in progress)

Build 0.19.3 corrects scene reloads inside the Nex arena without requiring a bank
visit. Bank history remains bounded without a deadline; hard account/profile/
world guards remain. [Regression evidence](docs/validation/nex-bank-retention.md).
Client testing is deferred; no new live acceptance is claimed.

Implementation is available for review in development build 0.15.0. The Nex adapter,
native session view, method/locks, immutable state/resource capture and banking
round trips have synthetic replay/lifecycle coverage. [Evidence and pending live
checks](docs/validation/nex-session.md). **N1 is not accepted yet:** initial client screenshots show censored attempts,
historical failures rather than acceptance. Complete-result display and one
bank-retention check have since been confirmed in-session; matched multi-trip
means, broader lifecycle and usefulness checks remain open. The
[0.16.5 correction](docs/validation/nex-smoke-anchor.md) replaces arena-entry
anchoring with an observed smoke start and distinguishes dying from later arena
completion. It preserves pre-smoke states and conservative late-arrival rejection.
Bank recognition, explicit active status and bounded diagnostics remain. Earlier
reports retain their original evidence and checks.
**16 October client follow-up:** one displayed complete kill had 574/574 ticks,
344.4 seconds and zero unclassified hits, then the bank-open view showed 0/0
attempts. **Bank retention failed this round.** Build 0.19.1 removes the bank-opening
expiry and premature nearby-Nex reset, and retains the arena departure source
across an exit-barrier callback. Build 0.19.2 also covers another scene change
while preferences load after a verified return, and shows build/reset diagnostics.
[Finding and regression evidence](docs/validation/nex-bank-retention.md).
Retest the corrected build before accepting retention; prior screenshots/reports
keep their original scopes.

Finish the real-client and usefulness checks before
claiming the adapter's live mappings, capture fractions or game-timer agreement.

Deliver one useful native flow for **Nex / Trio MP2 / one kill per trip**.
The confirmed pilot uses Lightbearer without ring switches and upgraded Death
Charge. Voidwaker is the reported spec weapon; spec timing, remaining equipment,
ammo/charges and supplies must be observed rather than invented.

- Save method and explicit locks/corrections; automatic observations are the
  default. Distinguish detected spell state from an assumed upgrade toggle.
- Implement a reviewed Nex event adapter with phase/subphase/target identity,
  start/end correlation, local/team damage scope and completion status. MP2
  includes ranged windows inside P2; it is not melee for all of P2.
- Capture actual melee/ranged/spec setups, current levels/prayers, ammo and
  spec-energy/Death Charge changes. Preserve delayed-hit/same-tick ambiguity.
  Inventory changes alone are not proof of eating or potion consumption.
- Show last kill and session mean, sample count and spread, with clearly defined
  timing/contribution scopes. Do not confuse average per-kill share with aggregate
  damage share. Failed/censored kills remain separate from complete averages.
- Retain bounded completed summaries across recognized banking transitions after
  same-account revalidation. Clear active traces and stale callbacks; clear the
  session on logout, identity/profile change, unsupported world, disable or Reset.
- Introduce the encounter-first view and retire the replaced general homepage
  path. Keep useful account corrections and calculation details accessible.

Acceptance: real and replayed multi-switch kills; Tbow with empty offhand does not
erase melee defender; stable phase/minion identity; missing/late/duplicate events;
death/teleport/mid-kill enable; several one-kill bank trips; account switch during
pending work; matching timer origins; no false completed kills or leaked sessions.
Record captured/eligible fractions and real-client/player findings separately.

Offline review now also includes `nex-bank-long-trip` and `nex-arena-reload`:
long delayed banking/return, observed exit versus nearby Nex, missing/rebuilt arena
NPCs, next complete kills and retained hard identity clearing. These complement
the seven DEV1 baseline scenarios; live retesting remains open.

## N2 — Better use of owned equipment (implementation for review)

Development build 0.16.0 supplies immutable per-state carried-package comparisons,
conditional bank preparation, explicit pairs, locks and coverage gaps in the Nex
view. The old homepage/global progression policy and obsolete consumers/tests are
removed together; shared formula/oracle, source/schema, observation and preference
regressions remain. [Implementation and verification](docs/validation/nex-owned.md).

Petter supplied the ranged and melee equipment on 15 October; an anonymous fixture
uses those items with explicitly synthetic levels/prayers/quantities. **Acceptance
remains open:** live capture now produces N2 results, while exact stock/charges,
combined bank packing and player usefulness still need validation. Build 0.16.6
adds locked melee variants plus role/support filters and grouped gaps; see
[the correction report](docs/validation/nex-owned-variants.md). Spec-role
history is retained; its finite energy/effects remain N3. Supported ordinary
pairwise offence is not global best owned or encounter throughput.

Evaluate complete method-compatible owned packages across melee/ranged/spec roles.
Compare them to the immutable actually-used timeline, respecting locks, available
inventory and shared resources. Preserve useful reviewed model/source machinery.
Replace the global progression ranking for this flow with explainable contextual
choices; remove obsolete policy/UI consumers and their superseded tests together.

Acceptance: full actual account fixture with no private dump committed; compatible
hands/ammo/sets; gains and losses; unchanged package gives zero; unknown stock or
model coverage does not become absence or a false global-best claim. An unrelated
unsupported item must not suppress a supported explicit pairwise comparison.

## N3 — Relevant upgrades and the pilot spec system

**N3a resource foundation is integrated with N2 0.16.6 in build 0.17.1.**
Post-kill details expose frozen
resource readings; an independent bounded scenario API checks shared energy,
regen phase/order, cap waste, Death Charge casts/eligible procs and spec versus
ordinary attack delays. It does not infer attacks/casts/procs or supply a spec
damage/upgrade recommendation. [Contract](docs/research/nex-spec-resources.md) and
[verification](docs/validation/nex-spec-resources.md).

**N3b mechanics implementation is complete for review in build 0.18.0.**

- [x] Independently implement Voidwaker damage and PvM ruby/diamond bolt outcomes,
  including ZCB accuracy, natural procs after misses, HP/caps and ruby self-damage.
- [x] Join accepted launches to finite exact ammo/splinter stock, charged/blessed
  quiver branches, shared energy and displaced ordinary attacks. Keep unsupported
  damage scoped and test against hand calculations and complete resource branches.
- [x] Record reviewed sources, numerical boundaries and build/regression evidence
  in the [N3b report](docs/validation/nex-spec-damage.md).

**N3c is implemented for review in build 0.19.0**: reviewed complete-package
construction now feeds N3b through bounded conditional comparisons. **N3 remains
in progress.** See [the N3c report](docs/validation/nex-packages.md). Automatic
capture-to-plan attribution, additional phase mechanics, acquisition/recurring-cost presentation
and supply effects remain separate follow-up work. N1/N2 acceptance above is
unchanged; combined client checks remain open after the integration.

Prioritize actual Nex coverage: ammo/quiver, relevant melee packages, Voidwaker
versus Zaryte crossbow, Lightbearer and upgraded Death Charge. Validate finite
energy, regeneration timing, cast/proc opportunities, HP-dependent bolts and the
ordinary attacks displaced by specs. Use verified sources and independent cases.
Show recurring ammo/splinter use separately from permanent equipment/unlocks;
retain a concise acquisition path and owned-alternative comparison.

Acceptance includes charged-versus-blessed quiver equivalence where applicable,
correct spec costs, no fixed Lightbearer uplift, resource competition, blocked
metrics scoped to dependencies, and whole-package rather than summed-item gains.
Capture food/potion effects; only compare altered supply plans with a supported
consumption/delay/boost model. Unsupported healing/survival claims remain explicit.

### N3c — Connect complete packages to N3b (automated acceptance complete)

Deliver a tested bridge from complete equipment packages to the existing N3b
damage/resource kernels for Nex Trio MP2, one kill per trip. Reuse N2 package
construction, the reviewed catalogue and ordinary combat models; do not build a
second catalogue or simulator. N3b 0.18.0 is the implementation dependency.
Implementation, supported scenarios, source limits and actual automated results are recorded in the
[N3c report](docs/validation/nex-packages.md) and
[input contract](docs/research/nex-packages.md).

Development and automated acceptance can proceed without Petter performing a
client test now. Completing N3c does not accept N1/N2 live mappings or establish
personal upgrade recommendations.

Scope:

- Construct complete melee/ranged/spec inputs for the actually-used setup, a
  relevant supported owned alternative, and that owned baseline with a hypothetical
  upgrade. Keep observed history immutable and separate actual-to-owned from
  owned-to-upgrade gains. A supported alternative is not a global-best claim.
- Respect exact item forms, requirements, locks, fixed Lightbearer, hands, sets,
  selected ammunition, quiver activation and shared inventory/resources across
  roles. Keep carried availability distinct from conditional bank preparation;
  bank presence alone cannot establish usable stock or a valid joint packing plan.
- Derive damage bounds, attack rolls and delays from each complete package.
  Voidwaker uses its own melee package; ZCB uses its compatible ranged/bolt
  package. Recompute charged and uncharged quiver packages independently.
- Define an explicit input contract: observed, explicitly assumed (including
  synthetic fixtures), or unknown, with source/scope and model versions. Cover
  target/phase, legal launch windows, target/player HP, starting energy and spell
  state, restore timing/order, eligible Death Charge events, exact ammo/splinter
  stock and active recovery. Unknown is never zero, absent or a default.
- Produce a bounded conditional comparison with package changes, supported
  damage/resource deltas, assumptions and named gaps. Use a small representative
  set of complete scenarios first. Do not infer launches or eligible windows
  from worn duration, impact equipment or sampled energy deltas.

Acceptance:

- [x] End-to-end automated cases exercise package facts -> derived ordinary/spec
  inputs -> finite plans -> comparison results. Include Voidwaker, ZCB ruby/diamond
  and charged/blessed quiver cases; verify against independent hand calculations,
  not only direct kernel calls with hand-entered damage values.
- [x] Identical packages/plans give zero; supported worse alternatives retain
  negative deltas. Comparisons use the same declared encounter conditions and
  horizon. Whole-package gains are recomputed, never summed from item cards.
- [x] Compatible hands/ammo/sets, exact variants, locks and shared inventory are
  checked. A two-handed ranged switch does not erase the melee defender. Proposed
  items do not overwrite observations or manufacture owned/carried stock.
- [x] Specs displace ordinary attacks with the correct package delays and share
  finite energy/ammo/splinters. Rejected attacks spend nothing; last-ammo and
  last-splinter cases retain N3b behavior. Charged/blessed offence agrees while
  charge remains, with their different recurring resource use preserved.
- [x] Missing HP, eligible opportunities, initial/resource state or reviewed
  target/phase mechanics blocks only dependent results with a specific reason.
  Unsupported Zaros soft caps, minion effects or other special windows cannot
  silently use ordinary Nex arithmetic. An unrelated unsupported alternative
  does not suppress a supported explicit pair.
- [x] Results retain package/input provenance and distinguish conditional
  expectations from observed outcomes. No expected damage is fed back as actual
  HP; no single resource branch is presented as an unconditional kill forecast.
- [x] Run affected automated tests and the normal test/build; preserve arithmetic,
  unknown-state, compatibility and lifecycle regressions. Record actual results,
  supported scenarios and remaining client checks. Keep fixtures anonymous and
  label synthetic levels, HP, timings and quantities explicitly.

Outside N3c: automatic capture-to-plan attribution, full Nex phase/HP simulation,
automatic best-spec ranking, personal contribution or seconds saved (N4),
food/potion/healing/survival models, acquisition paths and recurring-cost
presentation. Scope acquisition/presentation as a separate later task, reusing
the retained route data and N3b consumption accounting. No new setup wizard or
live combat coaching. Client validation of capture, switches and resource readings
remains required before conditional results become personal recommendations.

## N4 — Validate personal contribution and team-time estimates

Implement only the bounded Nex method model supported by N1–N3 evidence. Preserve
phase/target damage, mandatory windows, teammates and finite attack opportunities.
Keep observed share, modeled personal damage and expected team time separate.
Expose assumptions and the type of uncertainty; never promise a reconstruction
of the exact last kill with unseen gear. See [numerical contract](docs/KILL_IMPACT.md).

Acceptance: freeze the model before held-out checks; alternate genuinely owned
setups in matched session blocks; report sample sizes, exclusions, errors and
upgrade-delta uncertainty. Ordinary prediction accuracy alone does not establish
a small upgrade saving. Do not headline positive seconds when unresolved effects
or uncertainty can reverse the result. Validate usefulness with Petter in-game.

## After the Nex pilot

Extend to other methods and bosses/raid encounters through shared models and
small adapters, selected by usefulness and reusable mechanics. Broader acquisition
estimation and old coverage gaps remain recorded evidence, not parallel delivery
mandates. Reassess the scoped feature and current policy before public distribution;
Plugin Hub submission is a separate authorized action.
