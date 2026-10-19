# IronPath development instructions

## Current authority

- The user approved the encounter-and-method pivot and transition on 15 October
  2026. This supersedes the old account-wide/no-goal-first product direction,
  progression ranking requirements, parallel AD1/R roadmap and optional-boss-only
  UX. Work in the same repository; retain useful foundations and history.
- Read README.md, TASKS.md, docs/DESIGN.md and the relevant UX, KILL_IMPACT,
  CATALOGUE and RESEARCH sections. TASKS.md is the only active backlog.
- docs/archive is historical reference, never active instructions. Older
  validation reports retain their original scope/results and pending client checks.
- Code, UI, documents and commits are English; discussion with Petter may be Norwegian.

## Current delivery

- N3 PR #50 development build **0.25.0** adds frozen public target forms/health
  ratios, separately declared fixed Zaros overhead/target levels, and finite normal
  Death/Blood/Soul rune stock/costs for declared Death Charge. Read
  docs/research/nex-decision-support.md and docs/validation/nex-zaros-spells.md.
  Health ratios are not exact HP, observed forms do not confirm future impact
  overheads, and player drains do not reveal target stats. Keep unsupported
  deflection, unknown inputs and automatic attribution gated. No merge or publication.

- N3 follow-up **0.24.0** is prepared on current main for review. Read
  docs/research/nex-decision-support.md and docs/validation/nex-decision-support.md.
  It adds declared finite supplies/level effects, vulnerable minion launch inputs,
  scoped manual unit costs and catalogue acquisition routes. Unknown windows stay
  unknown. Keep automatic launch/spec/consumption attribution and Zaros
  overhead/stat-dependent comparisons unavailable. Existing no-coaching/N4 and
  separate merge/publication authorization boundaries still apply.
- Consolidated main baseline **0.23.3** includes the complete PR #48 **0.23.2** UI
  and sidebar simplification. Keep Gear & method and Gear & unlocks absent,
  including manual gear/method controls.
  Retain Team & method, automatic observations, saved preferences and calculations.
- Issue #47 supplies the presentation baseline, development build **0.23.2**.
  PR #48’s approved review replaces top tabs with independent collapsible sections:
  Last kill always visible; Compare setups open; Team & method and Diagnostics
  initially closed, Diagnostics last after the sidebar simplification. Read
  docs/validation/pr48-review-followup.md. Preserve drafts/selection/scroll across
  refresh and verified reloads; identity/reset/eviction still clear evidence.
  Use Waiting for Nex only after arena/capture readiness; keep distinct loading,
  bank, account/world and settings statuses. Invalid plan submissions need brief
  field-specific bounds; saved correction timestamps belong in Diagnostics.
  Read docs/validation/ui1-47-wording-layout.md and docs/UX.md. TASKS.md remains
  the sole backlog. Normal screens contain results, short labels and controls;
  technical explanations, scope/assumption paragraphs, pilot/testing/future-work
  text, provenance and raw traces belong in explicitly opened Diagnostics/reports.
  This supersedes older instructions to attach explanatory prose to every card.
- Use OSRS wording from issue #47. Keep Encounter, Compare setups and item-count
  package labels. Damage contribution is only a label for the unchanged observed
  hitsplat ratio. Keep exact item names/IDs and distinguish equipped, inventory,
  bank and hypothetical gear; never narrow the carried-package search.
- Centre headings, values, sprite/name groups and action groups within the native
  225px column. Keep forms, tables and long diagnostic text left aligned. Validate
  native 225 × 600 images and full-height screens at body fonts 12 and 20.
- N3 scenario choices are explicit unchecked controls. Every required declaration
  must be supplied before its shared readiness flag is known. Do not pre-confirm
  hidden inputs. Preserve unknown gating, exact stock, complete-package arithmetic,
  shared delays/conditions, proposal locks, frozen input and request/view guards.
- The user authorized the 19 October main consolidation of PR #49, including
  main/N3 #45, CTX1 #43 `2fe5342`, UI1 #46 `d5b14db` and PR #48 `836e1e9`.
  Preserve their full ancestry and the sidebar simplification. Start new work from
  main; retire only branch heads already included there. Future merges and Plugin
  Hub publication need separate authorization. Live acceptance remains open.
- Keep genuine preview sprites/test boundaries and Jagex provenance/notices.
  Keep immutable capture, CTX1 annotations/future defaults, the 50-record lifetime,
  frozen planning/unlock inputs, matched-cohort rules, report selection, clipboard
  fallback, Back/scroll and expanded Diagnostics across unrelated refreshes.
- Reuse NexPlanComparison/NexPackageComparison and existing arithmetic. No new
  attribution, automatic spec recommendation, HP trajectory, coaching or N4 claim.
  Preserve banking/reload/reset/identity guards and all baseline DEV1 scenarios.

## Delivery

- CTX1 **0.21.0** adds editable kill context and preferences schema 4; schemas
  1–3 migrate without kill confirmation. Annotations, frozen evaluation inputs and
  calculations share the bounded 50-record in-memory lifetime. Live snapshots and
  active traces still clear at scene transitions; retained records stay hidden until
  same-account/profile/world revalidation. No performance history is persisted.
- UI1 **0.20.0** adds compact Encounter / Diagnostics and an allowlisted
  user-triggered report; retained calculations/selection are immutable across new
  results. Clipboard failure leaves selectable text. See the UI1 validation record.
- Retained 0.19.3 behavior fixes arena-only scene reloads being mistaken
  for outbound bank travel. Current arena evidence at LOADING permits arena
  revalidation; an observed exit revokes that permission. Missing NPCs alone do
  not establish an exit. Reset remains available while paused after live account
  revalidation. Build 0.19.2 extends the 0.19.1 completed-history fix during bank
  travel on top of 0.19.0 N3c. Completed summaries have no bank-opening deadline;
  outbound nearby-Nex evidence keeps them paused until a visible bank verifies
  the same account/profile/world. A verified arena source survives an exit-barrier
  callback only until the next scene transition. A bank return verified from
  public arena evidence establishes the next departure source even while
  preferences load. The panel shows bundled build metadata and a constant reset
  explanation; reasons contain no identifiers and are never persisted/logged.
  Live snapshots, active traces
  and stale callbacks still clear; hard identity/logout/world/reset guards remain.
  See docs/validation/nex-bank-retention.md; client retesting remains open.
- Development build 0.19.0 adds N3c complete-package conditional
  comparisons to N3b 0.18.0. Reuse NexPackageFactory and NexPackageComparison;
  do not infer launch opportunities, HP or initial resources from captured states.
  See docs/research/nex-packages.md and docs/research/nex-spec-damage.md.
  Combined client testing remains open.
  Live-client/player acceptance remains open in docs/validation/nex-session.md
  and docs/validation/nex-owned.md. Nex Trio MP2, one kill per trip. Lightbearer throughout,
  upgraded Death Charge, observed melee/ranged/spec switches. Do not guess the
  rest of the account or require a screenshot/manual setup form before observation.
- Keep one Java/Gradle RuneLite plugin. Reuse independently authored combat
  models, account observation, preferences, provenance and existing useful sources.
  No wholesale restart, universal simulator, backend or second catalogue.
- Remove verified dead code. Replace active old UI/ranking in the delivery that
  supplies its new purpose; do not maintain two permanent products. The transition
  retained the former UI through T0. N1 replaced its runtime homepage; N2 removes
  the old presenters, global ranking policy and their obsolete tests. Retain
  independent arithmetic, acquisition data/kernels and lifecycle regressions.
- Live adapter 0.16.5 (`nex-public-4`) starts measurement at the observed Nex
  smoke announcement, not arena entry. The 2→3 transition is diagnostic only.
  Unknown pre-smoke states remain separately inspectable and share the same
  buffer/time/event limits. Later phases, positive damage or unclassified hits
  before smoke prevent re-anchoring; duplicate smoke never moves an established
  start. No phase/state is backfilled and no old summary is promoted.
- Corroborate death/dying with arena completion: end may follow dying by up to
  20 ticks, with a shorter five-tick reverse-order allowance. These are bounded
  adapter policies, not calibrated server guarantees. Keep source event values,
  bounded diagnostics, first-bank return and hard identity/reset guards. See
  docs/validation/nex-smoke-anchor.md; live acceptance remains open.
- Development launcher 0.16.4 keeps global `-ea`, required by RuneLite's
  `ExternalPluginManager.loadBuiltin`, with only `net.runelite.client.plugins.gpu...`
  excluded as a temporary GPU assertion workaround. The 0.16.3 package-only
  setting broke startup. Verify the actual loadBuiltin guard with the run JVM
  arguments, not just Gradle configuration. See docs/DEVELOPMENT.md.
- N2 searches complete supported carried packages per captured state. Bank-only
  scenarios are conditional next-trip preparation, never current inventory
  availability or a validated joint packing plan. No global-best claim. Keep
  explicit supported pairs when another alternative has no model. See
  docs/research/nex-owned.md for the exact offence/support contract.
- N3a adds frozen post-kill resource readings and a bounded finite energy/delay
  scenario API. It does not infer specs/casts/procs from deltas or rank spec
  upgrades. Fixed-ring scenarios require explicit initial spell state, restore
  phase/order, eligible deaths and attack opportunities. Preserve the distinction
  between sampled net changes and modeled spending/restoration. See
  docs/research/nex-spec-resources.md; N3 damage/ammo/supply acceptance stays open.
- N3b uses independently supplied complete-package stats, legal launch windows,
  current target/player HP and hard caps. Exact ammo stock and explicit saving/
  charge branches join the existing finite energy timeline; rejected attacks do
  not spend ammo or energy. Never turn conditional launch expectations into a
  simulated HP trajectory, survival estimate, captured spec count or best-weapon
  recommendation. Soft caps, phase transforms, automatic opportunity attribution
  and package proposals, acquisition presentation and supply effects remain separate work.
- N3c compares explicit complete actual/owned/upgrade scenarios under one shared
  immutable conditions declaration. Exact usable joint stock/packing is separate
  from bank presence; assumptions are not observations. Preserve scoped damage/
  resource gaps, conditional branch labels, source/model versions and negative gains.
  NexPlanComparison now supplies an optional declared post-kill caller; automatic
  capture attribution, best-spec ranking and N4 time claims remain unavailable.
- Knowledge `n3-package-1` adds bounded Voidwaker and ruby/diamond bolt facts
  to the same catalogue. Existing `n2-owned-2` added exact locked Dragon defender (24143) and
  Infernal cape (24224) melee forms. Preserve exact captured IDs and ownership;
  do not generalize normal/locked/broken/mangled variants. Comparison filters
  retain all immutable scenarios and grouped gaps; counts are not fight coverage.
  See docs/validation/nex-owned-variants.md. Complete-result display and one bank
  retention round trip have in-session evidence; broader acceptance stays open.
- Use short-lived branches from current main and focused PRs. Do not merge or
  publish to Plugin Hub without authorization. Do not alter unrelated work.
- Test the affected behavior and run the normal build. Preserve arithmetic,
  unknown-state, incompatible-package and lifecycle regressions. Migrate tests
  enforcing superseded product choices when their behavior is replaced.

## Account, observation and privacy

- Read public RuneLite state on the client thread into immutable snapshots.
  Evaluate outside that thread/Swing EDT; publish guarded immutable results.
- Unknown is not absent, zero, empty or unlocked. Keep source, time and scope;
  ownership, exact variants, charges, quantity, equipped state and usability differ.
- Preserve profile/character identity, generations, stale callback guards, serial
  preference IO and migrations. Keep raw account data and combat traces in memory.
- Persist only explicit method/preferences/manual facts in plugin-owned local
  Filepath storage. No bank dumps, performance history, names or telemetry.
- N1 must separately implement/test completed-session retention across ordinary
  banking. Do not weaken current resets in this transition. Active traces must be
  ended/censored; logout, profile change, unsupported worlds and disable clear data.

## Comparison and presentation

- Separate actually used setup, best relevant owned package, and owned package
  with a hypothetical upgrade. Respect locked choices, hands, ammo, sets, switches
  and shared spec/inventory resources. Never substitute proposed gear for observations.
- Numerical claims require complete compatible inputs and target/phase/method,
  relevant mechanics, provenance and model versions. Missing support blocks only
  dependent metrics; a best-supported option is not proof of the global best.
- Separate observed duration/damage share, theoretical offence, estimated personal
  contribution and team time. No last-kill/DPS-ratio shortcut, additive savings,
  PB-as-average, invented intervals, skill scores or combat-to-grind-hour conversion.
- Model specs/Lightbearer/Death Charge as a finite energy and opportunity timeline.
  Preserve defensive, supply, recurring-ammo and permanent-unlock distinctions.
- Native Swing/PluginPanel and ItemManager images; readable normal panel width.
  Quiet post-kill updates, concise results and optional details. No live combat
  coaching, forced focus/navigation, automation or per-kill setup wizard.

## Evidence and distribution

- Version reviewed mechanics and dependencies. Preserve per-metric gaps and
  withdraw known-invalid claims without suppressing unrelated supported results.
- External calculators are development references. Do not copy GPL code/data
  into BSD work; review code, dataset, prose and image rights separately.
- Use supported public Java APIs. No reflection, subprocess integration, runtime
  scraping/downloaded code, private-plugin data scraping or required other plugins.
- Reassess the exact new encounter feature against current Plugin Hub policy
  before distribution. Existing plugins are not approval for new behavior.
- Unit tests and synthetic images do not establish real-client or player usefulness.
