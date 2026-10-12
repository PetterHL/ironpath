# Product and implementation decisions

Recommended design · 12 October 2026 · not a shipped specification

## Product test

**Question:** Can an established Ironman quickly understand and choose a useful next PvM action from a small set of account-aware recommendations?

Decision updated on 12 October 2026: proceed independently. Petter reports that Iron Compass is difficult to use for his needs. Retain its technical overlap as research, but do not require a comparison against it or an upstream-contribution decision before implementation. Our acceptance criteria concern our own advice, clarity and ease of use.

The system can explain authored expertise without AI. It cannot derive good strategy merely by collecting item stats, drop tables and videos. Someone must decide when an upgrade is valuable, for which encounter and player context, and what alternatives are acceptable. This editorial work is the main cost and constraint.

Choose a narrow CoX-focused slice because it includes several combat styles, meaningful gear alternatives and post-entry upgrades. This is a hypothesis to validate, not evidence that CoX is the largest market gap. Do not implement a loadout/DPS optimiser already addressed by specialist tools.

## MVP boundary

| Implement | Defer or exclude |
|---|---|
| Standard Ironman, permanent main-game worlds | UIM logistics, HCIM safety routing, GIM shared ownership and seasonal modes |
| Skills, worn/inventory items, bank observed this session, selected quests/unlocks | Complete Collection Log, historical boss KC, all Combat Achievements |
| At most 12 curated CoX-related decisions | Whole-game progression, every boss or equipment item |
| Up to three diverse actionable suggestions, each with why, trade-off, effort and source | A universal optimal route, numerical account quality or invented DPS gain |
| No-goal mode; one optional CoX learning/improvement goal or supported item/unlock goal | Multiple goal queues, free-text goals, Inferno or all-raid planners |
| Skip/restore an activity; labelled manual possession/unlock confirmations | Arbitrary user-authored executable rules or broad plugin integrations |
| One sidebar with detail expansion and ordinary source links | Overlays, live combat coaching, pathfinding, elaborate visual graphs |
| Bundled, versioned knowledge; local evaluation | Runtime scraping, remote catalogues, automatic strategy ingestion |

If an account/mode or item combination is outside coverage, say so. Do not present the catalogue as comprehensive advice for endgame OSRS.

### Initial content budget

Author at most twelve **decisions**, not twelve isolated equipment tiers. Candidate families are: starting CoX with an adequate owned kit; Bowfa/crystal investment versus using an available ranged alternative; completing a useful crystal package; a blowpipe sidegrade; improving a powered magic weapon; fixing a meaningful magic-damage slot gap; a melee/dragonbane upgrade; defence reduction alternatives; a relevant jewellery upgrade; Rigour; Augury; and practising CoX with existing gear rather than another marginal upgrade.

This is a research shortlist, **not a validated gear order or release dataset**. Merge overlapping candidates and replace weak ones during T2. Include current alternatives and owned high-end equipment in applicability checks; do not freeze the meta at an older guide's publication date. New bosses do not automatically expand scope, but their rewards may change an existing comparison.

At least four decisions must remain useful to post-Bowfa or established raiding accounts. Readiness refers to documented access and an explicitly stated preparation baseline, never a promise of successful kills. Team assumptions must be visible; first-pass CoX preparation is for ordinary team raids, not solo or Challenge Mode certification.

## Recommendation behaviour

1. Resolve each condition to `TRUE`, `FALSE` or `UNKNOWN`. For `ALL`, any false dominates; otherwise unknown propagates. For `ANY`, any true dominates; otherwise unknown propagates. An unread container never implies an absent item.
2. Separate hard requirements, suggested preparation, completion and relevance. Do not encode a useful preparatory item as a game access requirement.
3. Exclude completed, skipped, unsupported and unreviewed candidates. An unknown completion condition does not justify telling the player to obtain that item; ask for a bank refresh or confirmation separately.
4. Rank eligible actions with a small, explicit comparator: selected-goal relevance, contextual improvement over recognised owned alternatives, then an authored effort band and stable ID tie-break. Review priority bands against fixtures; do not invent a precision-weighted efficiency formula.
5. Show at most three materially different choices. Prefer a useful alternative over three rewards from the same grind. Blocked goals show the nearest confirmed missing prerequisite separately, not as an immediately available activity.
6. Explain the actual matching account evidence, what this helps, what the player gives up, why a competing option was not prioritised, and uncertainty. If nothing useful is supported, return no recommendation.

Each decision needs a stable ID, target/outcome, access conditions, completion condition, contextual applicability/benefit, alternatives, qualitative effort with assumptions, source IDs, review status and last review date. Use a small Java model plus bundled JSON, not a generic scripting language.

Use explicit `all`/`any` prerequisites only where needed. Optional suggestions must not become dependency edges. Validate hard-dependency cycles; a raid can yield equipment useful in that same raid without requiring its own reward for access. Skipping a genuinely required prerequisite makes that route blocked; it does not silently mark it complete.

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

### State and privacy

- Live inventory/equipment can establish positive possession. Bank results mean “observed in your bank at time T.” Absence means “not observed in supported storage,” not “never acquired.” Other storage requires manual confirmation or remains unknown.
- Keep bank observations in memory for the current session. Clear on logout, login/account transition, connection loss, world hop, profile change and plugin disable; require a new observation. Conservative clearing is acceptable for v0.
- Eventual manual confirmations include provenance and can be cleared. Positive live evidence can supersede a conflicting manual absence. “Owned manually” is a planning assumption, not evidence of immediately usable equipment or sufficient charges.
- Persist selected goal, skips and manual confirmations only in a small plugin-owned local JSON file keyed by RuneLite's current character/profile identity; no username key, credentials, telemetry or auto-export. Validate the current supported `Filepath`/plugin-directory integration in T1. Ordinary UI settings may use ConfigManager, but account facts must not: RuneLite can sync profile configuration.
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
