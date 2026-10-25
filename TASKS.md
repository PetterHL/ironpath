# IronPath active work

Updated **25 October 2026**. This is the **only active backlog and roadmap**.
Start new work from current main; do not continue retired stacked branches.
The user authorized merging the complete 0.23.3 delivery into main and cleaning
up covered PRs/branches on 19 October. This includes CTX1 #43, native UI1 #46,
PR #48's current presentation/follow-up fixes and PR #49's sidebar simplification.
Integration does not establish live-client or player acceptance.

## Current baseline and order

Main baseline: **0.31.0**, `2018144`, including merged PR #55, PR #56 and PR #54 (`9a2b592`),
PR #50 (`289f371`), PR #51 (`43d20a1`) and PR #53 (`1fe77cb`) above the complete 0.23.3 lineage.
Merge ancestry preserves all deliveries. Covered PRs/branches are retired only
after their heads are verified as ancestors of main; no unique work is discarded.

| Order | Work | State / boundary |
|---|---|---|
| Complete | **Current UI and sidebar simplification** | 0.23.3 consolidated main baseline; Gear & method / Gear & unlocks removed |
| Complete | **CTX1 #43 / UI1 #46 / N3 #45 / PR #48** | Full ancestry retained in main; live acceptance remains open |
| Alongside, when gameplay is available | **N1/N2/CTX1/N3 live acceptance** | Open; no new Nex kill required for offline UI review |
| Integrated | **N3 decision support (0.27.0), PR #50** | Declared per-attack conditions, Magic-type Voidwaker under Deflect Melee, supplies, minion/Zaros windows, finite spell runes, scoped costs/routes and frozen public target evidence |
| Integrated | **Nex observation (0.28.3), PR #51** | Bounded HP/launch/resource candidates, settled state, start quantities and recorded-tick drafts; live mapping remains open |
| Integrated: issue [#52](https://github.com/PetterHL/ironpath/issues/52), PR [#53](https://github.com/PetterHL/ironpath/pull/53) | **Nex damage coverage / projectile source evidence (0.29.0)** | One live kill confirms object reuse (487/513 callbacks), retains all 171 own impacts and matches per-target damage minus healing to 5,400 HP. [Evidence and limits](docs/validation/nex-damage-observation.md#live-one-copy-check--23-october-2026). Projectile/HUD attribution, resource semantics and broader lifecycle acceptance remain open |
| Integrated: [PR #54](https://github.com/PetterHL/ironpath/pull/54) | **N3 evidence validation (0.30.0)** | Full-cap clock correction, diagnostic repeat-source counters and the 24 October Nex delivery decision. No automatic recommendation yet |
| Integrated: [PR #56](https://github.com/PetterHL/ironpath/pull/56) | **Karambwan potion cooldown (0.30.1)** | Reject potions one/two ticks after karambwan; retain the three-tick boundary and same-tick combos. [Verification](docs/validation/karambwan-potion-cooldown.md) |
| Integrated, 0.31.0 / PR #55 | **N3-ACTION: action evidence and attack clock** | Bounded Fang state, food/DC messages and frozen timeline; controlled client mapping check remains pending |
| PR #57, 0.32.1 follow-up | **N3-ADVICE: provisional automatic Fang preview** | One supplied 0.32.0 live report and separate screenshot confirm export/calculation/UI paths; scoped resource/grouping and message corrections follow. Mapping/window/base-stat assumptions and product acceptance remain open |
| After the first useful comparison | **Broader N3 automatic recommendations** | Ranged attribution and finite spec/DC/supply plans remain gated; solve dependencies as needed for each added output |
| After N1–N3 validation | **N4 contribution/team-time estimates** | Not implemented/validated |
| Later | More methods/bosses, optional recorder and screenshot export | Separate roadmap work |

Deliver future changes through focused PRs; no automatic merge or Plugin Hub publication.

## Nex delivery decision — 24 October 2026

**Keep Nex Trio MP2 as the pilot.** Do not pivot to Vardorvis or rebuild the
engine/UI. The next product milestone is one automatic post-kill equipment
comparison for supported ordinary **Osmumten's fang** attacks, holding weapon,
style and attack tempo fixed. This is a deliberate first slice, not a claim that
Fang was observed in every recorded window or that it is the best weapon.

[Reviewed plugin patterns and limits](docs/research/nex-action-evidence.md)
inform the two implementation steps below. Reuse the current observer, N2 package
search, damage kernels, frozen reports and panel. No other plugin is required at runtime.

Decisions:

- Implement a bounded attack-state/cooldown model using local signals and settled
  state. Keep directly observed evidence separate from inferred launch/readiness.
  An attack click or available cooldown is not proof of an executed attack or a
  wasted legal opportunity. Start with supported ordinary Fang melee; unsupported
  weapons/styles/windows remain excluded with reasons.
- Add successful food and Death Charge game-message mappings. Food messages can
  inform attack delay; DC messages can establish a cast candidate alongside
  existing active/cooldown/effect evidence. Silence, an energy delta or an active
  transition alone cannot establish consumption, a cast, proc or expiry.
- Use valid target-HP bounds where the existing damage model can support them.
  Exact HP is unnecessary when every compatible HP gives the same answer.
  Otherwise expose a justified range or withhold that dependent result; never
  substitute the bar midpoint or stale post-impact HP for exact pre-impact HP.
- Make support specific to the output. Ordinary damage comparisons need compatible
  attack/target/gear inputs, not a solved Death Charge or joint-packing timeline.
  The current eight-item automatic-gap list is a static full-plan checklist,
  not measured progress. **0.30.0 still has all eight gaps and no automatic
  recommendation.** The later scoped result must have its own validated support
  contract; do not bypass the full finite-plan guards.
- Defer ranged ownership, full spec/DC/resource optimization and N4 seconds/team
  contribution until the first slice works. UNKNOWN first-flight sources stay
  UNKNOWN. Vardorvis is a source reference only; no second-boss adapter now.

### N3-ACTION — 0.31.0 implementation for review

Implemented from main `9a2b592` after the authorized PR #54 merge.
[Contract, verification and controlled client procedure](docs/validation/nex-action-evidence.md).
Code review and live mapping promotion remain separate.

- [x] Extend the existing Nex observer with allowlisted food and normal/upgraded
  DC success messages, including the relevant game-message types. Normalize tags,
  retain typed events with tick/source/confidence, and never store raw chat/names.
  Preserve Nex phase parsing and distinguish unrelated/player/plugin messages.
- [x] Implement the bounded Fang attack-state/cooldown model using local animation,
  target identity, weapon/style and reconciled tick state. Cover continuing
  animations, blocking, rejected clicks, weapon swaps and food delay. Retain misses
  and zero hits; ambiguous launches/impacts remain ambiguous.
- [x] Preserve unknown initial readiness and bounded pre-start context; smoke is
  not a cooldown reset. Keep lifecycle, identity, buffer, overflow and worker
  guards. Do not deduplicate legitimate equal same-tick events by text alone.
- [x] Export a frozen, inspectable action timeline through the existing report:
  observed signals, inferred attacks, confidence/rejection reasons, cooldown and
  food/DC events. It must be usable by N3-ADVICE, not just aggregate counters.
- [x] Test accepted/rejected/repeating attacks, misses, block/movement, swaps,
  eat/attack ordering, normal/upgraded DC, unrelated chat, unknown pre-start state,
  ambiguous impacts and reset/overflow. Run the prescribed pinned and normal
  builds and existing offline review; update schema/model versions as needed.
- [x] Provide one short controlled client procedure comparing a known sequence
  of Fang attacks, eating and DC casts with the exported timeline. A fresh full
  Nex kill is not required for those basic mappings; Nex-specific phase/target
  support still needs encounter evidence before N3-ADVICE promotes it.
  Record exact agreements/disagreements and leave untested mappings provisional.
- [ ] Execute the controlled client procedure and record mapping agreements or
  disagreements. All 0.31.0 mappings remain provisional; no promotion is claimed.

**Done for code review:** the event model, consumer-ready timeline and meaningful
replays/builds are delivered in one PR with the short client procedure. **Done
for promotion:** the supported mappings agree with controlled client evidence.
If a mapping fails, narrow or correct that mapping; do not restart a generic
diagnostics project or silently label it confirmed.

### N3-ADVICE — First useful automatic result

**0.32.0 implementation for review**, from integrated main `2018144`. The user
explicitly requested proceeding with N3-ADVICE on 25 October despite the pending
controlled check. This authorizes the provisional implementation below, without
promoting any mapping or declaring product acceptance. [Contract and verification](docs/validation/nex-fang-advice.md).

The supplied client report motivates the **0.32.1** [reconciliation/message
follow-up](docs/validation/nex-fang-action-followup.md) on the same open PR #57.
It accepts one bounded conditional export/calculation path and visible client
card, while controlled mapping/window acceptance remains unchecked.

Follow N3-ACTION with a post-kill comparison in the existing Compare setups panel:

- [x] Automatically construct actual versus relevant owned/hypothetical equipment
  packages for supported ordinary Fang attacks, holding weapon/style/tempo and
  compatible observed conditions fixed. Reuse N2 search and existing arithmetic;
  do not build a second calculator or require per-kill declaration forms.
- [x] Compare expected damage for the **same provisional attack sample**. Include
  zero-damage/missed attacks; selection must not depend on damage landing.
  Show the number of included attacks and exclusions/ambiguity in Details or the
  report. Do not extrapolate the subset to whole-kill damage or changed rotations.
- [x] Gate target/phase/overhead and relevant package inputs for the provisional
  base-stat ordinary-damage preview; unknown windows and unsupported forms stay out. For uncertain
  HP, evaluate the paired comparison over the same feasible states, including
  mechanic breakpoints; endpoints alone need a proof of sufficiency. Preserve
  negative gains and support gaps. Unresolved resource inputs block only results
  that actually depend on them.
- [x] Deliver an end-to-end frozen replay from captured action evidence to one
  correctly scoped equipment result, plus unknown/unsupported/ambiguous and
  zero-hit cases. Show concise results in the existing panel; provenance belongs
  in Diagnostics and whole-capture schema-5 JSON.
- [ ] Execute the controlled mappings check and validate supported Nex windows
  against a short captured segment. Base target-stat assumptions and unknown
  impact-time HP remain explicit; no validated automatic recommendation is claimed.

**Product acceptance:** a supported Nex segment automatically produces a useful
Fang equipment comparison with coverage and defensible uncertainty. An unsupported
segment explains the missing input. No seconds saved, whole-kill contribution,
best-spec claim, live coaching or hypothetical attack opportunities are implied.
Only then broaden the supported attack set and resume full N3/N4 work.

## N3 evidence validation (0.30.0)

From current main `1fe77cb`, after merged PRs #50/#51/#53.
[Source review, exact unknowns and smallest observations](docs/research/n3-evidence-validation.md)
· [Verification and readiness](docs/validation/n3-evidence-validation.md).

- [x] Review the active roadmap, three merged PRs, current implementation and
  committed 0.29.0 live findings; pin RuneLite 1.13.2 public contracts/source.
  No new raw-report parse or Nex kill is claimed.
- [x] Correct the declared fixed-ring natural clock: stop at cap, restart a full
  interval when leaving full, preserve phase below full. Keep initial phase/order
  and same-tick cap/proc policy explicit; no automatic readiness inference.
- [x] Split retained-object repeat projectile source getter counts into
  LOCAL/OTHER/UNKNOWN. Preserve first-flight source/target and all correlation,
  bound, lifecycle and privacy gates; a later LOCAL getter cannot enable a match.
- [x] Record exact remaining unknowns, insufficient signals and minimum controlled
  observations for launches/ownership, impact HP, readiness/energy, DC transitions,
  usable stock/packing and consumption. A fresh complete Nex kill is not required.
- [x] Complete both prescribed commands in CI: pinned RuneLite 1.13.2 and normal
  1.13.3, JDK 11.0.32+1 / Gradle 8.10; all 14 DEV1 scenarios pass. Exact code
  commit and CI/artifact evidence are in the validation record. Workspace startup
  prevented local execution and manual PNG inspection; no live acceptance is claimed.
- [ ] Full-plan automatic recommendations remain **NOT READY**: retain all eight
  automatic gaps and unchecked declarations in 0.30.0. The 24 October decision
  above schedules N3-ACTION then a separately supported N3-ADVICE result; it does
  not claim those mappings already exist. N4, contribution/time estimates,
  broader lifecycle acceptance and unrelated UI work remain separate.

## Nex callback coverage follow-up (0.29.0)

Integration base: PR #51 `feb439bdc93278e05b08844a0ab941f5593adf87`, dependent
on PR #50 `f02028dd0af74e358e69e8b268bdcae64c669761`; main `7f68d0e`.
This follows the explicit [23 October handoff](https://github.com/PetterHL/ironpath/pull/51#issuecomment-6359152035),
which authorizes this dependent correction branch despite the default main-first rule.

- [x] Compare pinned public callback contracts and the six requested plugin implementations;
  [record adopted/existing/deferred patterns and notices](docs/research/nex-damage-observation.md).
- [x] Replace unsupported hitsplat-object identity rejection with direct scalar callback
  capture; fixed enum counters expose entry/filter/classification sums and reuse.
- [x] Preserve projectile scope and first-callback timing; distinguish LOCAL/OTHER/UNKNOWN,
  report exclusions, and withhold ranged matches without local-source evidence.
- [x] Withhold whole-kill contribution at headline/report boundaries; retain observed
  damage sums/ratio and independent timing/gear/resource results in Diagnostics.
- [x] Version one-copy export to `nex-evidence-2`; preserve all useful retained events,
  UI pagination, allowlists, bounded memory, workers, confirmation and lifecycle guards.
- [x] Complete required sequential builds, source/version evidence and native font 12/20
  review: 410 tests / 47 suites on RuneLite 1.13.2 and 1.13.3; 14 DEV1 scenarios /
  1,137 checks. Focused issue #52 links the delivery PR with exact commit/CI/artifact evidence.
- [x] Analyze the supplied 0.29.0 live report: 487/513 repeated-object callbacks,
  all 171 own impacts retained, and damage minus healing matches each target's HP.
  This confirms the correction for one kill; the earlier kill is not reconstructed.
- [x] Investigate the 364 UNKNOWN projectile sources and rejected HUD bindings offline;
  [0.30.0 findings](docs/research/n3-evidence-validation.md) retain ownership/HP unknowns
  and specify the missing observations. No ownership or exact HP is inferred.
- [x] Research full-cap regeneration and DC eligibility/active transitions using the
  existing report and pinned public sources. The 0.30.0 fixed-ring correction is
  corroborated; cast/eligibility/proc/expiry classification remains unavailable.
- [ ] Bank return / next kill / arena reload: separate deferred lifecycle acceptance.
  A second kill is not a prerequisite for accepting this single-kill correction or
  continuing offline analysis/documentation; the user confirmed this on 23 October.

[Verification and short next-live procedure](docs/validation/nex-damage-observation.md).
No merge or Plugin Hub publication is authorized.

## Sidebar simplification (0.23.3)

- [x] Retain the complete PR #48 UI (`836e1e9`), including CTX1 #43, UI1 #46,
  current main's N3 plans and the profile/report/scroll corrections.
- [x] Remove Gear & method and Gear & unlocks entirely, including manual gear
  locks/method controls. Keep Team & method and the remaining layout unchanged.
- [x] Preserve automatic observations, stored preferences/manual facts, upgraded
  Death Charge assumptions, calculations and lifecycle guards. Add no controls.
- [x] Verify with the full unit/build/offline workflow and native 225px/font
  12/20 previews. [Evidence](docs/validation/sidebar-simplification.md).
  Live RuneLite acceptance remains separate.

## PR #48 — approved review follow-up (0.23.2)

The 19 October review adds approved navigation and arrival wording to issue #47.
This supersedes earlier top Encounter / Diagnostics tabs.

- [x] Distinct unsupported-account/world, loading/settings and bank reasons;
  Loading encounter… / Waiting for Nex / Tracking kill… / Kill tracking: Off
  follow actual readiness, suspension and capture state.
- [x] Transient field-specific plan errors include window, energy, HP/restore/cap
  and distinct increasing tick bounds; unknown inputs retain dependent gaps.
- [x] Populated Gear & unlocks uses readable names/states, with provenance only in
  Diagnostics. Stored keys, exact sets and observed-fact precedence stay intact.
- [x] One sidebar: Last kill always visible, Compare setups initially open,
  Team & method / Gear & method / Gear & unlocks / Diagnostics initially closed.
  Independent keyboard-accessible full-row headers; Diagnostics last.
- [x] Preserve drafts, selected frozen comparison/planner, report subtree and
  scroll through ordinary updates/verified reloads. Hard boundaries still clear.
- [x] Add focused regressions, inspect native 225 × 600/font 12/20 evidence
  and prepare the current PR for review. Performed local checks, current CI
  outcome and artifact links are recorded in the PR.
- [x] Subsequent review: keep profile-executor clearing free of live client reads,
  retarget Diagnostics on explicit kill/attempt inspection and restore root scroll
  after a laid-out suspension. Regressions retain background-report freezing,
  immediate invalidation and hard-scope clearing.

[Implementation, checks and screenshots](docs/validation/pr48-review-followup.md).
Live client/player acceptance remains open; merge/publication is separate.

## UI1 #47 — presentation refinement (0.23.0, for review)

- [x] Keep Encounter, Compare setups and genuine item-count package labels; apply
  issue #47's OSRS field/action labels and accessibility/tooltips. Damage
  contribution retains the same observed hitsplat calculation.
- [x] Normal Encounter, cards, gear/unlocks, team editor, kill/comparison details
  and optional planner contain results, short state/origin labels and controls.
  Move help, scope, assumption, testing/future-work paragraphs, provenance, long
  gaps and raw traces to the common Diagnostics area or explicit reports.
- [x] Distinguish equipped/inventory/bank/hypothetical gear without changing search,
  package membership, exact variants, alternative ordering or calculations.
  Render readable account choices while preserving internal keys and every member
  of ownership sets.
- [x] Replace the N3 confirmation wall with explicit unchecked scenario choices;
  missing choices and unknown numeric inputs still gate dependent results.
- [x] Centre headings, values, coherent sprite/name groups and actions at 225px.
  Keep forms and technical text left aligned; preserve native fonts and scrolling.
- [x] Complete focused tests, both prescribed full checks, visual review at 12/20,
  and validation evidence with exact delivery commit/CI artifact links.
- [x] Prepare one reviewable PR with representative embedded before/after PNGs. Live gameplay
  acceptance stays open; no merge or publication.

[Implementation, integration base, native evidence and performed checks](docs/validation/ui1-47-wording-layout.md).
These visibility/centring decisions supersede older UI1 requests for explanatory
scope sentences beside every card or in ordinary Details. Historical checklists
below retain their original delivery scope.

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

- [x] Use **Encounter / Diagnostics** as primary views. Keep comparison/kill
  details and Reset discoverable through secondary actions. The 19 October sidebar
  simplification removes Gear & method (Setup) and Gear & unlocks (Account),
  preserving automatic observations, saved preferences and calculations.
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

## CTX1 — Editable Nex kill context (included in main)

Implementation: **0.21.0**. [Contracts, automated evidence and remaining client
checks](docs/validation/ctx1-kill-context.md). The 19 October consolidation includes
the complete reviewed feature lineage. Live-client/player checks remain open.

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
PR #43's P2 review correction additionally covers copying/navigating the older
report after fresh combat account reads, with clipboard success/fallback and
uninterrupted active capture. See the CTX1 validation record for corrected-head counts.

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

## N3A/N3B/N3C — Post-kill runtime plans (included in main)

Build 0.22.0 (report-selection follow-up 0.22.1) joins the existing kernels without claiming automatic attack attribution.
User authorization on 18 October expands N3A to the combined N3A/B/C delivery.
[Contract](docs/research/nex-runtime-plans.md) · [verification](docs/validation/nex-runtime-plans.md).

- [x] Freeze captured packages/owned alternatives, equip facts and planning inputs.
- [x] Provide optional post-kill shared declarations for opportunities, HP/caps,
  restore/start state, legal cast/death events and exact hypothetical package edits.
- [x] Connect complete packages to finite N3A energy/DC/delay and N3B damage/ammo/
  quiver branches through N3C; separate captured-to-owned and marginal upgrade gains.
- [x] Preserve unknowns, exact forms, hands/ammo, observed reserves, locks, usable
  carried stock and shared supply/space checks; bank-only packing stays unavailable.
- [x] Add natural-only fixed-Lightbearer sensitivity examples, with every assumption
  disclosed; no observed spec/proc count or global-best/time recommendation.
- [x] Guard worker requests and frozen inspection, add allowlisted selectable
  scenario export, and extend DEV1 with public-event-to-plan and stale Reset checks.
- [x] Resolve the default Diagnostics selection when a matching calculation arrives
  after opening the page; preserve explicit filters/selection and export observed
  slots/target class even when the selected output is unavailable.
- [x] Integrate the runtime-plan delivery and the complete CTX1/UI lineage into main.
- [ ] Validate native client/player use together with the existing open live checks.

## Nex observation follow-up (0.28.2)

Main/PR #50 were checked on 20 October: main `7f68d0e`, PR #50 open at
`f02028d` (0.27.0), matching the research baseline. The focused follow-up uses
PR #50 as its unmerged dependency. [Contract and sources](docs/research/nex-observation.md)
· [Offline evidence and exact client procedure](docs/validation/nex-observation.md).

- [x] Implement timed/spawn-scoped fresh HUD candidates, conditional actor-bar
  HP bounds from the existing catalogue and unknowns; reject stale target/form/
  scene values. Do not call the Nex HUD channel live-validated or pre-impact HP.
- [x] Freeze pilot animation equipment/levels/target, distinct projectile evidence,
  own misses and ambiguous hits; reconcile later state without changing callbacks.
- [x] Freeze smoke-callback stock/food units/potion doses, client/server resources,
  raw Death Charge upgrade/active/cooldown/effects and tracked deaths. Do not infer
  consumption, casts/procs, resource cause or readiness from these signals.
- [x] Join matching launch-state candidates to independently supported post-kill
  comparisons; copy classified recorded ticks into the existing explicit N3 draft.
  Preserve unchecked assumptions, worker/input/report guards and dynamic-mechanic gaps.
- [x] Add focused synthetic arithmetic/order/lifecycle/privacy regressions and
  extend the existing DEV1 production-worker/panel scenario. Offline evidence is
  recorded separately from client acceptance in the validation record and PR.
- [x] Address PR #51 review: keep HP diagnostics outside the worn-state bound,
  invalidate saved impact/supply confirmations on tick import, preserve the
  selected Zaros overhead and expose bounded selectable event windows in
  Diagnostics/reports. Add long-capture, populated-plan and matching regressions.
- [x] Make client evidence one-copy: whole-capture per-target/source HP summaries,
  bounded animation groups, RuneLite version and fixed saturating HUD callback/
  first-failed-guard counts. Preserve the selected event window as optional detail;
  distinguish actor bars from unconfirmed Nex-specific HUD availability.
- [ ] Execute the ten controlled client checks in the validation record: HP
  forms/targets/HUD visibility, ordinary/spec/missed/delayed attacks, concurrent
  resources, DC proc versus expiry, full energy, stock/consumption and lifecycle.
- [ ] Promote only corroborated mappings and dependent results after client
  checks. Initial readiness, legal/missed opportunities, alternate weapon schedules,
  confirmed supply uses, dynamic Nex stats/healing/reflection and N4 remain open.

## N3 — Decision support and remaining automatic recommendations

Development build **0.27.0**, for review, starts from consolidated main `7f68d0e`.
[Contract and source review](docs/research/nex-decision-support.md) ·
[Initial verification](docs/validation/nex-decision-support.md) ·
[Zaros/spell verification](docs/validation/nex-zaros-spells.md) ·
[Per-attack verification](docs/validation/nex-impact-conditions.md) ·
[Deflection attack-type verification](docs/validation/nex-deflect-attack-type.md).
Reuses the N3A/B/C runtime caller, package factory and catalogue; no second engine.

- [x] Establish a bounded capture-to-scenario contract. Freeze public package,
  levels and raw readings; explicitly declare legal opportunities, HP, readiness,
  restoration/order and eligible casts/deaths. Preserve fixed allowlisted automatic
  evidence requirements in reports; no worn-time/impact/net-delta attribution.
- [x] Keep unknown captured windows unknown. Add matching declared vulnerable
  Fumus/Umbra/Cruor/Glacies windows using their own stats, size and non-Magic flat
  armour. Apply the transform before supplied HP/hard caps; preserve misses and
  Magic-type Voidwaker handling. Other target/phase gaps preserve finite resources.
- [x] Present higher/same/lower damage for the selected complete conditional pair,
  with separate captured-to-owned and owned-to-upgrade results; no global ranking.
- [x] Reuse reviewed exact-item acquisition routes without asserting completed
  components, personal odds, pace or a full-item ETA. Keep permanent unlocks
  separate from recurring ammunition/charges/consumption.
- [x] Price admitted ammo, conditional splinter consumption and declared supply
  units plus admitted Death Charge runes using explicit per-unit GP. Unknown prices
  withhold the scoped total; other gear upkeep, acquisition and whole-trip cost stay excluded.
- [x] Add declared shark/karambwan combos and brew/restore/super-combat/ranging
  doses with finite captured stock, eating/drinking bounds, shared displaced-attack
  delays and per-launch level recomputation. Require a separate unchecked supply
  declaration. Keep per-launch HP independent; no healing/survival optimizer.
- [x] Extend the existing DEV1 scenario and targeted regressions for unknown
  windows, minion transforms, dose exhaustion, drains/reboosts, scoped costs,
  unconfirmed supply choices, stale edits, selectable reports and Reset.
- [x] Freeze public NPC forms and health ratios with the selected package; preserve
  overhead changes through deduplication and export only fixed enums/readings.
  A rounded health bar does not establish exact HP or target combat levels.
- [x] Support explicit fixed Zaros overhead/Defence/Magic conditions, recomputing
  complete packages with their own rolls. Missing Magic blocks dependent Twisted
  bow damage; unknown overhead/stats and unreviewed melee deflection keep gaps.
- [x] Check normal Death/Blood/Soul rune stock from frozen loose stacks and carried
  pouch readings; validate Magic at admitted casts after supplies and charge only
  casts accepted by the existing cooldown. Preserve independent no-cast plans.
- [x] Allow independently declared per-attack HP/caps/ruby eligibility and Zaros
  impact overhead/Defence/Magic. Replace fixed inputs explicitly; absent rows and
  blank values stay unknown. Reuse the complete-package bridge and shared finite
  resource timeline; preserve same-tick fallback, frozen reports and request guards.
- [x] Separate Voidwaker's Magic-type special from ordinary melee in explicitly
  declared Zaros Deflect Melee impacts. Preserve ordinary-melee/reflection gaps,
  including admitted unfunded fallbacks, and withhold deltas with an unsupported
  baseline. Rejected fallback gaps do not suppress funded supported specials.
- [ ] Establish independently corroborated legal launches, per-launch HP, initial
  readiness, restore phase/order, spell/death events and usable stock for automatic
  recommendations. The 0.28.2 candidate capture supplies inspection evidence,
  not accepted mappings, legal opportunities or inferred readiness.
- [ ] Corroborate dynamic Zaros transfers/healing, impact-time overheads, ordinary
  melee deflection/reflection and other special windows before enabling dependent automatic
  personal comparisons. Fixed declarations are not a full encounter model. No
  OSRS “Zaros soft cap” is established; do not import RS3 mechanics.
- [ ] Validate native client/player use, real supply/charge/quantity readings and
  gameplay mappings together with N1/N2/CTX1 live checks. No automatic spec advice,
  cast/consumption inference, healing trajectory, survival or N4 time claim.


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
