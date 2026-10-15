# IronPath active work

Updated 15 October 2026. **This is the only active backlog.** The encounter-and-
method direction replaces the former AD1/R/KI delivery order. Historical work,
results and unperformed checks are preserved in the [archive](docs/archive/pre-nex-20261015/README.md).

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

Implementation is available for review in development build 0.15.0. The Nex adapter,
native session view, method/locks, immutable state/resource capture and banking
round trips have synthetic replay/lifecycle coverage. [Evidence and pending live
checks](docs/validation/nex-session.md). **N1 is not accepted yet:** initial client screenshots show censored attempts,
not a validated complete capture. The [0.16.1 follow-up](docs/validation/nex-live-start.md)
fixes reproduced start ordering/bank recognition and clarifies active status.
The second test still misses both boundaries. [0.16.2 diagnostics](docs/validation/nex-live-signals.md)
separate observed smoke, matched start and raw barrier transitions; the live cause
and required start/end rules remain open.
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

## N2 — Better use of owned equipment (implementation for review)

Development build 0.16.0 supplies immutable per-state carried-package comparisons,
conditional bank preparation, explicit pairs, locks and coverage gaps in the Nex
view. The old homepage/global progression policy and obsolete consumers/tests are
removed together; shared formula/oracle, source/schema, observation and preference
regressions remain. [Implementation and verification](docs/validation/nex-owned.md).

Petter supplied the ranged and melee equipment on 15 October; an anonymous fixture
uses those items with explicitly synthetic levels/prayers/quantities. **Acceptance
remains open:** no real captured account with complete levels/inventory/charges,
no validated combined bank packing plan and no player/client checks. Spec-role
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
