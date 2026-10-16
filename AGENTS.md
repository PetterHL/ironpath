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

## Delivery

- Current: N3a resource foundation in development build 0.17.0, based on
  N2 build 0.16.5. N1/N2 capture testing continues independently.
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
