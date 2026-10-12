# Product and implementation decisions

Approved development direction · updated 12 October 2026 · implementation and live validation remain separate

## Product test

**Question:** Can an established Ironman quickly understand and choose a useful next PvM action from a small set of account-aware recommendations?

Decision updated on 12 October 2026: proceed independently. Petter reports that Iron Compass is difficult to use for his needs. Retain its technical overlap as research, but do not require a comparison against it or an upstream-contribution decision before implementation. Our acceptance criteria concern our own advice, clarity and ease of use.

The system can explain authored expertise without AI. It cannot derive good strategy merely by collecting item stats, drop tables and videos. Someone must decide when an upgrade is valuable, for which encounter and player context, and what alternatives are acceptable. This editorial work is the main cost and constraint.

The initial CoX slice established account observations, local preferences and explainable advice. Petter approved moving directly to broader PvM progression on 12 October 2026. The next delivery must compare real acquisition/activity choices across CG, Zulrah, ToA, Slayer, Moons/Royal Titans and CoX. Retain CoX as one activity and regression case. No separate CoX-only release or completed old T4 trial is required before expansion. Pending tests are not assumed passed. Do not implement a loadout/DPS optimiser.

## MVP boundary

| Implement | Defer or exclude |
|---|---|
| Standard Ironman, permanent main-game worlds | UIM logistics, HCIM safety routing, GIM shared ownership and seasonal modes |
| Skills, worn/inventory items, bank observed this session, selected quests/unlocks | Complete Collection Log, historical boss KC, all Combat Achievements |
| First broad slice: 12–16 reviewed decisions total; wider first catalogue: approximately 35–45 | Every boss/item, exhaustive early-game questing/skilling, universal optimal routing |
| Up to three diverse actionable suggestions, each with why, trade-off, effort and source | A universal optimal route, numerical account quality or invented DPS gain |
| No-goal mode; one optional supported activity, item or unlock goal; bounded raid/advanced preparation goals as coverage grows | Multiple goal queues, free-text goals, combat coaching or complete raid/challenge planners |
| Skip/restore an activity; labelled manual possession/unlock confirmations | Arbitrary user-authored executable rules or broad plugin integrations |
| One sidebar with detail expansion and ordinary source links | Overlays, live combat coaching, pathfinding, elaborate visual graphs |
| Bundled, versioned knowledge; local evaluation | Runtime scraping, remote catalogues, automatic strategy ingestion |

If an account/mode or item combination is outside coverage, say so. Do not present the catalogue as comprehensive advice for endgame OSRS.

### First catalogue budget

TASKS.md P3 defines the next 12–16-decision broad slice; P4 defines the approximately 35–45-decision catalogue. Count meaningful decisions, not equipment tiers. Include acquisition choices, prerequisites, alternatives after declining a grind, and opportunities to start content with sufficient owned equipment. Existing scroll/assembly/switch reminders alone do not establish broader progression.

The wider candidate families cover shared PvM foundations, midgame equipment, ranged, magic/prayers, Slayer/specialist weapons, jewellery/armour, raids and a few advanced goals. These are research candidates, not a validated gear order. Verify current facts and strategy assumptions before activation; never pad the catalogue to meet a number. Recognising owned advanced gear to suppress redundant grinds does not require supporting its full acquisition route.

The first broad slice must include useful non-CoX choices after Bowfa. Readiness means documented access and labelled preparation assumptions, never a promise of successful kills. Team/solo assumptions must be visible. Retained CoX advice concerns ordinary team raids, not solo or Challenge Mode certification.

## Recommendation behaviour

1. Resolve each condition to `TRUE`, `FALSE` or `UNKNOWN`. For `ALL`, any false dominates; otherwise unknown propagates. For `ANY`, any true dominates; otherwise unknown propagates. An unread container never implies an absent item.
2. Separate hard requirements, suggested preparation, completion and relevance. Do not encode a useful preparatory item as a game access requirement.
3. Exclude completed, skipped, unsupported and unreviewed candidates from actionable advice. Unknown ownership never establishes a missing-item grind. Ask for targeted observation/confirmation; any conditional route preview must remain clearly separate from confirmed actionable recommendations. Avoid an account-wide questionnaire.
4. Rank eligible actions with a small, explicit comparator: selected-goal relevance, contextual improvement over recognised owned alternatives, breadth of usefulness, then authored effort and stable ID tie-break. Important longer routes must not be displaced automatically by trivial quick actions. Review priority bands against fixtures; do not invent a precision-weighted efficiency formula.
5. Show at most three materially different activity choices. Prefer a useful alternative over three rewards from the same grind. Blocked goals show the nearest confirmed missing prerequisite separately, not as an immediately available activity. Activity-level skip/restore excludes that acquisition route, not its completed outcome or the utility of already-owned equipment.
6. Explain the actual matching account evidence, what this helps, what the player gives up, why a competing option was not prioritised, and uncertainty. If nothing useful is supported, return no recommendation.

Each decision needs a stable ID, activity ID, target/outcome, hard access conditions, suggested preparation, completion condition, contextual applicability/benefit, alternatives, qualitative effort with assumptions, source IDs, review status and last review date. Goals and activity routes are distinct: multiple reviewed activities may support one outcome, and one upgrade may support several goals. Keep reusable bounded action types in a small Java model plus bundled JSON, not a generic scripting language. Move catalogue-specific goal choices and benefit ordering out of hardcoded CoX switches.

Use explicit `all`/`any` prerequisites only where needed, including next-step explanations for alternative routes. Optional suggestions must not become dependency edges. Validate hard-dependency cycles; a raid can yield equipment useful in that same raid without requiring its own reward for access. Skipping a genuinely required prerequisite makes that route blocked; it does not silently mark it complete.

For goals, show confirmed/missing/unknown prerequisites and the next viable action. A checklist fraction measures prerequisites only, never the probability of receiving a random drop. Broad effort labels are editorial estimates. Numerical hours are deferred; they would require activity rates, learning/setup time, supplies and RNG assumptions. Prior dry KC does not imply improved odds on ordinary independent drops.

## Small architecture

One Java RuneLite plugin, one Gradle project, four small responsibilities:

| Component | Responsibility |
|---|---|
| `AccountSnapshotReader` | Capture selected API facts on the client thread into immutable values; no UI or ranking |
| `KnowledgeCatalog` | Load bundled JSON once; validate IDs, references, supported predicates and evidence |
| `Advisor` | Pure Java evaluation of snapshot + catalogue + preferences; no RuneLite or Swing dependency |
| Sidebar/controller | Event handling, up to three suggestions, goal/skip controls and source links |

Read client state on the client thread. Coalesce relevant `StatChanged`, `ItemContainerChanged`, `VarbitChanged` and lifecycle changes; never rescan the entire catalogue every tick. Swing changes go through its event thread. Start with synchronous evaluation of a tiny immutable snapshot; add background evaluation only if measurement requires it. All delayed callbacks must carry/check an account-generation token.

Reuse RuneLite dependency injection, event bus, `PluginPanel`/toolbar components, injected Gson, item definitions and supported browser utility. Use item variations cautiously: cosmetic variants may be equivalent, but charged/unusable, imbued/non-imbued, consumed and component forms may not be. Keep exact IDs and explicit reviewed equivalence sets. Exclude zero-quantity placeholders; do not infer possession of crafted gear from its unassembled components.

### Bounded generalisation

Implement model changes with actual P3 decisions, not ahead of them as a separate framework. Extend selected quest/unlock observations using supported client APIs and the skills required by reviewed routes. Preserve evidence and tri-state semantics. Do not require Collection Log, boss KC or new plugin integrations.

Replace fixed CoX kinds, unique-kind checks, fixed gear-group assumptions and the 12-decision cap where required. Keep catalogue limits and validation for IDs, references, hard cycles, source consistency and supported predicates. Model context-specific alternatives rather than one universal gear hierarchy. Preserve exact reviewed usable/charged variants.

Preserve stable goal/decision/activity/fact IDs where possible. Migrate or explicitly retire local preference identifiers when changing schemas, retaining character/profile isolation. Existing item-ID sets do not establish quantities, supplies or remaining charges; resource planning is deferred.

### P3 implementation contract

Plugin 0.3.0 / knowledge `p3-1` / catalogue schema 4 implements the 16-decision
slice. The original reader, lifecycle/controller, Swing sidebar, local `Filepath`
store and serial preference IO remain in place. Bounded predicates now cover
selected facts, global planning possession, observed usable packages, levels and
ALL/ANY composition; they cannot call APIs or execute catalogue strings.

Goals, primary/shared route benefits, activities, outcomes, contextual benefit
bands and wield/package conditions are bundled data. Ranking compares primary
selected-goal routes, shared relevance, contextual benefit, supported-goal breadth,
qualitative effort and stable ID. Activity/family diversity limits output to three.
A long major route can precede a small owned-kit review. Preparation gates define
where this slice makes a recommendation; labels distinguish them from access.

Acquisition completion uses `POSSESSION`, whose false value requires an explicit
target or exact-variant absence confirmation. Container absence stays globally
unknown. Positive observations/manual ownership beat a conflicting target absence,
while `USABLE` needs actual observed active/charged forms and wield/package levels.
Historical owned-parts reminders retain their narrower storage-comparison semantics
and warn about other storage. ANY diagnostics keep alternative routes grouped.

Preference schema 2 reads schema 1 without changing the identity key or widening
old decision skips. All four old goal IDs and exact item/unlock keys remain valid;
retired identifiers are explicitly discarded. New activity skips and labelled
ownership-target confirmations use the same local path and generation safeguards.
No live observation, quest result or raw identity enters the preference document.

### State and privacy

- Live inventory/equipment can establish positive possession. Bank results mean “observed in your bank at time T.” Absence means “not observed in supported storage,” not “never acquired.” Other storage requires manual confirmation or remains unknown.
- Keep bank observations in memory for the current session. Clear on logout, login/account transition, connection loss, world hop, profile change and plugin disable; require a new observation. Conservative clearing is acceptable for v0.
- Manual confirmations include provenance and can be cleared. Positive live evidence can supersede a conflicting manual absence. “Owned manually” is a planning assumption, not evidence of immediately usable equipment or sufficient charges.
- Persist selected goal, skips and manual confirmations only in a small plugin-owned local JSON file keyed by RuneLite's current character/profile identity; no username key, credentials, telemetry or auto-export. Reuse the implemented supported `Filepath` integration and its first-save/restart regression coverage. Ordinary UI settings may use ConfigManager, but account facts must not: RuneLite can sync profile configuration.
- All calculations stay local. Opening a source link is ordinary user-initiated browser traffic with no account state in the URL. RuneLite itself still has its normal game/update/network behaviour; the plugin cannot promise that the whole client is offline.

## Keeping knowledge current

**Default:** knowledge changes travel in normal reviewed Plugin Hub updates with the Java plugin. No server, separate updater, signing infrastructure or runtime rule downloads.

1. Maintain a small source register in the catalogue: source type, exact URL, author, revision/commit/date where available, accessed date, reuse basis and affected decision IDs. A node's review date is separate from the latest observed page revision.
2. Initially use official Jagex release news/RSS plus a shortlist of Wiki item/strategy pages and guide authors. Review relevant changes before each release and aim for a weekly short triage. Monthly, inspect official new-content announcements and new guide publications to discover sources absent from the shortlist. Community issues may nominate sources. No recurring automation has been created in this setup.
3. If triage becomes repetitive, add one maintainer-run script that reads feed metadata and permitted MediaWiki revision metadata. It reports changed/new candidates; it never rewrites priorities, downloads executable code, or publishes. Verify Wiki API access and usage policy first. Respect identification, caching, backoff and limits. A timestamp change is not a strategy change.
4. Human review decides whether a game fact changed and whether it changes a recommendation. Separate released content from polls/future updates. For disputed strategy retain alternatives and explicit conditions. Do not claim consensus from copied guides or a creator's popularity.
5. Update only affected decisions, run catalogue validation and affected profile fixtures, bump `knowledgeVersion`, record a concise change note and submit the new commit to Plugin Hub. `schemaVersion` changes only when the reader contract changes. Stable decision IDs preserve preferences; removed IDs are retired/migrated explicitly.
6. Known-invalid decisions are withdrawn or corrected promptly. Display the bundled review date and coverage. Passing 90 days without review shows a catalogue-age notice, not a claim that every fact has become wrong. Offline clients cannot know a newly reported issue until an update arrives. A broken new catalogue must fail closed with a useful error, not silently fall back to fabricated advice; revert to a known-good release when needed.

Budget estimate to test: 20–40 minutes of triage in a quiet week, with additional hours after relevant releases. This is not a commitment or observed maintenance metric. Measure it. If the workload is too high, reduce coverage or contribute the catalogue to an existing maintainer; autonomous scraping does not remove the editorial obligation.
