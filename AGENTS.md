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

## Next delivery

- Start from current main and TASKS.md **UI1: Encounter and Diagnostics**.
  Follow with CTX1 only after the UI1 review. Do not repeat delivered DEV1/N3b/N3c
  work or resume their retired stacked branches. TASKS.md is the roadmap as well
  as the backlog; the UI discussion brief is preserved historical input.
- UI1 uses supported per-state comparisons, identifies changed items/packages,
  and adds an allowlisted, user-triggered report. It does not add editable context,
  automatic spec recommendations or team-time estimates. Preserve 0.19.3 banking,
  arena-reload, reset reasons and all nine baseline replay scenarios.
- The 18 October main consolidation integrates reviewed development work with
  live acceptance still open. It is not Plugin Hub approval or standing permission
  to merge subsequent feature PRs. See docs/validation/main-consolidation-20261018.md.

## Delivery

- Current: development build 0.19.3 fixes arena-only scene reloads being mistaken
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
  No automatic capture caller, best-spec ranking or N4 time claim is supplied.
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
