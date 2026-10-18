# IronPath active work

Updated **18 October 2026**. This is the **only active backlog and roadmap**.
Start new work from current main; do not continue retired stacked branches.
The user authorized consolidating existing work and updating this plan on
18 October. That consolidation introduced no UI/context feature or live-client
acceptance. UI1 and its two P2 review corrections are merged in the
0.20.0 delivery below. CTX1 is implemented for review in 0.21.0.

## Current baseline and order

Consolidated development baseline: **0.19.3** — N1 capture/session, N2 ordinary
owned comparisons, N3a resources, N3b conditional damage/ammunition, N3c
complete-package APIs, banking/arena-reload corrections, and DEV1.
N3b/N3c require explicit scenario inputs; they do not automatically turn a
captured kill into a spec recommendation or validated team-time prediction.

| Order | Work | State / boundary |
|---|---|---|
| Complete | **UI1 — Encounter and Diagnostics** | 0.20.0 merged; both P2 review findings fixed, client acceptance open |
| In review | **CTX1 — Editable Nex kill context** | 0.21.0 implemented for review; feature merge not authorized |
| Alongside, when gameplay is available | **N1/N2 live acceptance** | Open; does not block offline UI development |
| After context/evidence contracts | **N3 recommendation integration** | Kernels exist; attribution, remaining mechanics and product integration remain |
| After N1–N3 support and validation | **N4 contribution/team-time estimates** | Not implemented/validated |
| Later | More methods/bosses, optional recorder and screenshot export | Not prerequisites for UI1 |

Deliver one task at a time with a reviewable PR and evidence. Do not merge or
publish future work without user authorization. The cleanup approval does not
authorize future feature merges or Plugin Hub publication.

## UI1 — Encounter and Diagnostics (merged)

Implementation: **0.20.0**. Both PR #42 P2 findings have focused regressions and
passing full validation. [Commands, screenshots and limits](docs/validation/ui1-encounter-diagnostics.md).
Merged in main at 96d91e8; real-client/player acceptance remains open.

**Goal:** make the existing native panel compact and useful, and let Petter copy
a reliable report into ChatGPT/Codex after playing. Use existing supported
results; N4 is not a prerequisite. One cohesive UI/reporting delivery, not a
series of cosmetic or infrastructure-only PRs.

Read AGENTS.md, [UX](docs/UX.md), [Design](docs/DESIGN.md),
[Kill impact](docs/KILL_IMPACT.md), [Development](docs/DEVELOPMENT.md), and the
[original discussion brief](docs/KILL_IMPACT_UI_ASTRA_REVIEW.md). Decisions here
and in active design docs supersede the brief's open questions.

### Product and implementation scope

- [x] Use **Encounter / Diagnostics** as primary views. Keep Setup, Account,
  comparison/kill details and Reset discoverable through secondary actions.
  Preserve Back, scroll, keyboard access and frozen inspection.
- [x] Lead with the last complete duration and clearly scoped observed hitsplat
  share. Show comparable mean/count compactly; one matching kill needs no
  redundant mean/min/max. Keep incomplete attempts separate. Label the fixed
  pilot as model/planning scope, not detected or confirmed per-kill team context.
- [x] Use single-column item/package cards under **Improve your setup**. Identify
  the changed item (arrows, not the unchanged Tbow) or a multi-item package. Show
  one supported metric, phase/role scope, short reason and accessible details.
  Use live ItemManager sprites and explicitly labeled offline placeholders.
- [x] Define deterministic selection/deduplication of useful supported choices.
  Do not rank unrelated captured states/roles by their largest percentage as
  whole-kill value. Preserve whole packages, partial-search scope, material
  uncertainty and carried versus conditional bank-preparation availability.
  Show zero/no useful improvement once; retain negative alternatives in details.
  No filler quota or repeated unavailable time-saving cards.
- [x] Add bounded Diagnostics: status, bundled build/model versions, captured
  versus assumed inputs, coverage/gaps and calculations. Make safe status
  reachable with no account/kill, pending work, reset and paused travel. Never
  expose suspended/cleared history through Diagnostics. Preserve 0.19.3 reset
  reasons and paused Reset guards.
- [x] Add **Copy report**, bound to the selected immutable kill/comparison
  revision. Use a versioned allowlisted report model/formatter, not wholesale
  summary serialization. Include relevant exact before/after inputs, scoped
  outputs, versions and omissions. Omit names, account/profile identifiers,
  roster hashes/cohort strings, raw inventory/bank dumps and unrestricted traces.
  Use a report-local reference and live/synthetic provenance; export time is
  not an invented kill timestamp. Bound output and disclose truncation.
- [x] Preserve the displayed calculation's input snapshot during export; do not
  read today's account/preferences for an inspected result. Handle unavailable,
  busy or headless clipboard with selectable report text and a clear outcome.
  No automatic upload, backend or API key.
- [x] Extract only needed presentation/report boundaries. Calculations remain
  off client thread/EDT; retain generation, identity and stale image/action/
  publication guards. Share the production formatter with DEV1 where useful;
  runner, fixtures and assertion machinery remain test-only.

### Acceptance and handoff

- [x] Extend the existing **nine-scenario DEV1 suite**, not a second harness.
  Render actual Swing Encounter/Diagnostics and relevant expanded/empty/pending
  states at 225px with normal/enlarged fonts. Inspect images and representative
  viewports, including buttons/labels, long names and first-screen hierarchy;
  JTextArea clipping checks alone do not prove control or viewport usability.
- [x] Test changed-item identity, scoped supported/unknown/zero/negative states,
  coherent export, redaction and clipboard failure. New kills cannot change an
  inspected result/report. Identity/reset/stale work cannot expose old accounts
  or resurrect cleared data.
- [x] Preserve independent arithmetic and all lifecycle cases, including long
  bank trips and same-arena reloads. Run pinned full validation and normal build;
  record actual versions, outcomes, limitations and current CI artifact links.
- [x] Update active docs/status and open one PR with before/after evidence.
  Explicitly leave unperformed real sprites/DPI/focus/clipboard/scroll and player
  checks open. Iterate on UI1 review before beginning CTX1.

**Outside UI1:** context mutation/cohort redesign, new combat formulas, automatic
N3b/N3c advice, time/trip claims, full history browser, recording and in-client
Save screenshot. Add native screenshot saving later if manual screenshots and
DEV1 artifacts prove insufficient.

## CTX1 — Editable Nex kill context (implemented for review)

Implementation: **0.21.0**. [Contracts, automated evidence and remaining client
checks](docs/validation/ctx1-kill-context.md). UI1 is merged at 96d91e8; this feature
requires its own PR review and merge authorization.

**Goal:** optionally set usual context before play or correct a selected completed
kill afterwards, preserving evidence and any other active attempt.

- [x] Separate phase-2 strategy (MP2/RP2/Other/Unknown), group mode
  (organized/mass/unknown), and validated positive participant count including
  the player (or unknown). Mass never overwrites strategy. Variable/unconfirmed
  participation cannot become a falsely exact constant count.
- [x] Separate saved defaults, immutable captured facts, planning locks and
  bounded in-memory per-kill annotations. Snapshot defaults at attempt start;
  inherited values are not automatically confirmed. Migrate the fixed pilot
  without inventing confirmation. Keep per-field provenance and edit revisions.
- [x] Put a compact context editor beside the selected kill with explicit Apply
  and a separate remember-for-future option. No mandatory pre-fight form.
  Initially cover the latest completed and already-inspected record; an arbitrary
  history comparison selector does not already exist and needs explicit scope.
- [x] Use a dedicated annotation action, not NexObserver.method(), which currently
  censors capture on method changes. Future defaults changed during combat apply
  to the next attempt; historical edits cannot interrupt the current attempt.
- [x] Recompute only supported dependent outputs from preserved evaluation inputs.
  Do not silently add new bank observations/preferences to a context-only edit.
  Guard session generation, kill key, annotation revision and calculation-input/
  model revisions alongside existing checks. Pending/old results cannot be
  relabeled as newly corrected results.
- [x] Replace opaque string-only grouping where necessary with explicit
  comparability evidence. Planning locks are not observed performance. Preserve
  timer/adapter scope, observed setup and team uncertainty; equal team count is
  not equal teammate damage/composition. Reconcile aggregates after edits, keeping
  incomplete/incompatible/unconfirmed kills out of matched averages.
- [x] Define support per output. Other methods/team counts cannot reuse Trio MP2
  recommendations by changing a label. Retain observations and independently
  justified arithmetic; withhold dependent recommendations/team-time. Nearby
  players do not establish party membership.
- [x] Extend DEV1 for unknown/inherited context, post-kill correction, an older
  kill edited during a new attempt, repeated edits/pending publication, unsupported
  modes, preference migration, cohort reconciliation, reset/logout/eviction and
  frozen inspection/report consistency. Persist no performance history.

Automated acceptance: edits affect only their intended kill/default; observed timing,
damage and timeline stay unchanged; supported results/report revisions agree;
a historical annotation never censors another active attempt. Real-client editor,
focus/scroll/clipboard and player acceptance remain open. Completed comparison
inputs freeze once; later ownership/preferences apply to future comparisons.

## N1/N2 — Remaining live acceptance

Implementation is integrated; live/player acceptance remains open. Preserve
historical successes and the later lost-history failure in the
[N1](docs/validation/nex-session.md), [N2](docs/validation/nex-owned.md),
[variant](docs/validation/nex-owned-variants.md) and
[bank/reload](docs/validation/nex-bank-retention.md) reports.

- [ ] On current main (0.19.3 or successor), complete a kill, delay opening the
  bank, verify retained history, return and complete another kill.
- [ ] Check same-arena reload resumes capture without banking; logout,
  account/profile/world changes, disable and Reset clear history/pending work.
- [ ] Check live start/end/phase mappings, multi-switch history, timer origin,
  capture fractions and damage scope. Synthetic tests and hitsplat share do
  not establish game-timer calibration or server loot contribution.
- [ ] Check actual ammo/quiver/charge/quantity readings, carried/bank distinction,
  compatible packages, negative/zero results and useful owned comparisons,
  without committing private account dumps.
- [ ] Review actual sprites/scaling/focus/scroll and player usefulness.

Gameplay is an acceptance activity, not a requirement after each offline edit.
Integration does not convert unperformed checks into passes.

## N3 — Remaining recommendation integration and mechanics

N3a resources, N3b damage/ammo and N3c package APIs are implemented. Reuse
[N3b](docs/research/nex-spec-damage.md), [N3c](docs/research/nex-packages.md),
NexPackageFactory/NexPackageComparison and the existing catalogue.

- [ ] Establish a bounded evidence-backed capture-to-scenario contract for useful
  recommendations. Unknown opportunities, HP, initial resources, restore phase/
  order and casts/deaths remain unknown. Worn time, impact equipment and net
  energy deltas are not attack attribution.
- [ ] Close required pilot target/phase mechanics before enabling dependent
  personal comparisons. Separate actual-to-owned and owned-to-upgrade, recomputing
  whole packages with exact variants, locks, hands/ammo, shared energy, displaced
  attacks, joint usable stock and packing.
- [ ] Add supported acquisition and recurring-cost presentation using retained
  route data. Distinguish permanent unlocks from ammunition/quiver charges and
  consumption. Do not revive the global-grind homepage.
- [ ] Include food/combofood, brews/restores/potions and spells only with supported
  consumption/delay/boost/resource evidence; preserve healing/survival gaps.

Scope one concrete slice after CTX1/evidence review. Do not rebuild delivered
kernels or advertise conditional APIs as automatic live recommendations.

## N4 — Validate personal contribution and team-time estimates

Use only a bounded method model supported by N1–N3 evidence. Account for phase/
target damage, finite HP, rounding/overkill, mandatory windows, team damage and
attack/resource opportunities. No last-kill/DPS-ratio shortcut, additive savings
or assumed zero effect for unknown mechanics.

- [ ] Freeze the model before held-out evaluation. Alternate genuinely owned
  setups in matched blocks; report counts, exclusions, bias and uncertainty type.
- [ ] Validate upgrade deltas as well as baseline predictions. Withhold positive
  seconds headlines when unresolved effects/uncertainty can reverse the result.
- [ ] Separate observed share, modeled personal damage and expected team time;
  validate usefulness with Petter. One-kill trips do not imply a longer-trip goal.

## Delivered foundations and later options

- T0, N1/N2 implementation and N3a/N3b/N3c APIs are integrated; retain evidence and
  open boundaries in [validation records](docs/validation/README.md).
- DEV1 is implemented: reviewed baseline **nine scenarios, 293 checks, 56 PNGs**
  at 225px/12pt/20pt. Numerical APIs also have separate unit/integration tests.
  These are baseline counts, not fixed quotas for future work.
- Extend methods/bosses through shared arithmetic and small adapters when useful,
  without a universal simulator or revived global ranking.
- An opt-in recorder needs ordered events with accompanying state, anonymous
  identities and a separately reviewed memory-only trace-policy exception.
  UI1 diagnostic reports are not replay recordings.
- Screenshot saving and wider history navigation are optional scoped follow-ups.
  Public distribution/current Plugin Hub policy review remain separate work.

## Codex Cloud delivery loop

Read this file and AGENTS.md, branch from current main, implement the selected
task, run affected regressions and these commands, inspect artifacts and fix
findings before handing over a reviewable PR:

```sh
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --no-daemon
./gradlew test build --no-daemon
```

Use JDK 11 and the [prepared environment instructions](docs/DEVELOPMENT.md).
Preserve independent expectations. Record exact commit/versions, performed checks
and blockers; attach the current CI report/PNG artifact. No routine generated
output or private captures in Git. No automatic merge.

Previous task detail remains at
[the pre-cleanup revision](https://github.com/PetterHL/ironpath/blob/762f46f21cd2a7f20ac9558724522d0e4d5a4c95/TASKS.md).
The [consolidation record](docs/validation/main-consolidation-20261018.md) records
branch disposition and evidence. Historical plans are not parallel backlogs.
