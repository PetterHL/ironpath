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

- Current: N2 implementation in development build 0.16.3, based on N1.
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
- Live-test follow-up 0.16.1 (`nex-public-2`) correlates smoke on either side
  of an actually sampled start boundary. Never invent a boundary or backfill
  phases/state for a late arrival. Record first bank visits, distinguish nearby
  NPC presence from arena state, and display active capture status. See
  docs/validation/nex-live-start.md; real-client retesting remains open.
- Follow-up 0.16.2 (`nex-public-3`) consumes barrier event payloads and keeps
  bounded in-memory signal diagnostics. The second test still lacks both
  boundaries; do not claim it is fixed. The required 2→3 start transition and
  five-tick end correlation are unvalidated policy, not established game timing.
  See docs/validation/nex-live-signals.md before changing those rules.
- Development launcher 0.16.3 scopes Java assertions to `com.ironpath...`.
  A client log showed RuneLite GPU `ModelUploader:194` failing an assertion
  enabled by the former global `-ea`. This launch change does not establish
  that every GPU freeze is fixed; see docs/DEVELOPMENT.md.
- N2 searches complete supported carried packages per captured state. Bank-only
  scenarios are conditional next-trip preparation, never current inventory
  availability or a validated joint packing plan. No global-best claim. Keep
  explicit supported pairs when another alternative has no model. See
  docs/research/nex-owned.md for the exact offence/support contract.
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
