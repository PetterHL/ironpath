# Feasibility and source audit

Researched 12 October 2026. This is a bounded source/code review, not an in-game evaluation, legal clearance, exhaustive competitor study or validated progression dataset.

## Assessment

**Technical feasibility: good. Our recommendation quality and usability: to be validated. Maintenance feasibility: conditional on narrow coverage.**

**Decision update, 12 October 2026:** Petter reports that Iron Compass's current workflow is unusable for his needs. This is direct user feedback, not a general usability study. The project will proceed independently. Iron Compass remains a source of technical reference and feature overlap; its existence is not a reason to stop, and outperforming it is not an implementation gate. Repository name `PetterHL/ironpath` and public visibility are approved.

A local rule-based plugin can match account observations to curated advice and explain its result. An AI model is unnecessary for this bounded job. Existing account-aware plugins reinforce that conclusion. The difficult work is contextual strategy: an item's nominal strength is not its marginal value to a particular account, and the player's time, preferences and ability cannot all be inferred from game state.

Do not start by harvesting a large corpus. Start with a dozen defensible decisions and five contrasting accounts, checking whether players can understand and act on the advice. A wider source collection is useful only when it resolves an actual recommendation gap. Bank value, completionist goals and max efficiency are not interchangeable with PvM capability.

## 1. RuneLite and distribution

The official [Plugin Hub repository](https://github.com/runelite/plugin-hub) describes public source repositories, the example template, manifest submission with a pinned commit, and commit updates for later releases. Its inspected instructions recommend Java 11; the [example build](https://github.com/runelite/example-plugin/blob/master/build.gradle) sets Java release 11. Recheck at implementation time rather than imposing a modern Java/Kotlin stack.

[Hub review documentation](https://github.com/runelite/runelite/wiki/Plugin-Hub-Review) says initial submissions and updates are reviewed for security and rule compliance. This is not verification of usefulness, factual accuracy, performance or compatibility. Approval is not guaranteed by this research.

The current [restrictions](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features) require Java and restrict reflection, native code, subprocesses and downloaded executable code. They direct plugins to injected Gson, the permitted browser utility and RuneLite's `Filepath` API. Follow the current rules rather than copying grandfathered behaviour. A planning sidebar is a reasonable candidate; it must not become a combat helper. [Jagex's guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1) prohibit features such as automatic boss attack/prayer/position guidance.

The [Hub information page](https://github.com/runelite/runelite/wiki/Information-about-the-Plugin-Hub) also requires disclosure for third-party server communications. The recommended MVP avoids such plugin traffic entirely. Public Git hosting and Hub distribution remove the need for owner-operated runtime infrastructure; they do not promise permanent free third-party services or zero developer effort. Build automation is optional, not a prerequisite, and paid runners/services should not be enabled by default.

## 2. Account-data feasibility

| Data | Verified mechanism | Reliability boundary and decision |
|---|---|---|
| Skills/levels | `Client.getRealSkillLevel`, skill experience and skill-change events [A] | Good logged-in input; use real levels, not temporary boosted values. Include now. |
| Inventory/equipment | `Client.getItemContainer` and container events [A] | Current observed containers, not every possible storage. Resolve exact variants and usability. Include now. |
| Bank | Bank item container; existing account-state implementation [B] | Observe after opening the bank. Cache only within the active character session; filtered bank UI is not the data model. “Not observed” is not global nonownership. Include now. |
| Quests | `Quest.getState(Client)` [C] | Current implementation invokes a quest-status script. Read on the client thread after login initialisation; query only necessary quests. Include selected quests. |
| Other unlocks | `getVarbitValue`/`getVarpValue` [A] | Requires maintained mappings and verified meaning. Consumed prayer scrolls need unlock state, not possession checks. Add only unlocks used by rules; use labelled manual fallback. |
| Collection Log | WikiSync's current source handles log scripts and a user-triggered sync control [D] | Technically feasible; not a universal complete-account getter. Current Hub policy explicitly allows a narrowly specified search-toggle action for obtaining log data. Coverage/timing need a spike. Historical acquisition is not current possession. Defer. |
| Boss KC | Core Chat Commands records observed counts; HiscoreClient offers external lookup [E] | Local history is incomplete; public lookup introduces network, freshness and unranked/missing cases. No verified single complete local getter. Neither KC nor absence proves skill. Defer. |
| Combat Achievements | Generated `CA_TASK_COMPLETED_*` varplayers and a maintained tracker [F] | Completion is locally readable with task mappings; mappings and login timing still need validation. Do not claim all achievements are unavailable or necessarily require widget scraping. Defer because v0 rules do not need them. |
| Character identity/preferences | `ConfigManager.getRSProfileKey` and profile lifecycle [G] | Supports isolation, but its configuration can synchronise remotely. Persist sensitive manual facts separately; keep bank observations in memory. Verify local-file API in T1. |

**A:** [Client.java](https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/Client.java), read directly from source.

**B:** [Iron Compass AccountStateService](https://github.com/gabworknestio-beep/iron-compass/blob/4f55942723b2f1fd609213ea1b2b528e7641a670/src/main/java/com/ironcompass/state/AccountStateService.java), inspected as a reference, not copied.

**C:** [Quest.java](https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/Quest.java).

**D:** [WikiSync source](https://github.com/weirdgloop/WikiSync/tree/1ff4ea0653395b552844d658d3668815d82e0a79/src/main/java/com/andmcadams/wikisync), particularly `WikiSyncPlugin` and `SyncButtonManager`; [permitted collection-log exception](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features#sensitive-apis). The old Collection Log README's requirement to visit every page is not sufficient evidence for today's best acquisition method.

**E:** [ChatCommandsPlugin.java](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/plugins/chatcommands/ChatCommandsPlugin.java).

**F:** [VarPlayerID.java](https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/gameval/VarPlayerID.java); [Combat Achievements Tracker](https://github.com/ehubbartt/combat-achievements-tracker). Tracker documentation says progress loads on login; no live execution was performed here.

**G:** [ConfigManager.java](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/config/ConfigManager.java); [Filepath.java](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/util/Filepath.java). The sync implication is from source, not an assumption that “no network code in our plugin” makes every configuration write local-only.

## 3. Existing solutions and reuse

| Project | What the inspected material establishes | Implication |
|---|---|---|
| [Iron Compass](https://github.com/gabworknestio-beep/iron-compass) | Account-aware goals, observed-bank handling, alternatives and a 40-objective gear catalogue across progression stages. Gear evaluator and account-state source were inspected; output quality was not tested. | Overlapping reference, not an assumed good product or a gate to our development. Petter reports poor usability for his needs. BSD-2-Clause code permits reuse subject to notices; catalogue rights require separate scrutiny. |
| [Ironman Progression](https://github.com/Wizard-Fish/Ironman-Progression) | Repository describes live quest/skill progression with phased checklists, gear suggestions and optional observed-bank hints. | Further evidence this is an existing category, not an empty niche. No need for another broad checklist. |
| [CoX Gear Planner](https://github.com/davidcottrellcoding/cox-gear-planner) | Repository describes owned-equipment raid planning and room-specific DPS calculations. | Complementary specialist tool; do not duplicate its optimiser. README/source availability does not itself establish runtime quality or Hub availability. |
| [Log Adviser](https://github.com/SFranciscoSouza/LogAdviser) | Ranks remaining collection-log activities by expected time to a new slot. | Useful comparison for transparent ranking, but collection speed is a different objective from PvM capability. |
| [WikiSync](https://github.com/weirdgloop/WikiSync) | Exposes implementation patterns for reading account/log state; its normal operation uploads public player data to a Wiki service. | Study locally readable patterns, do not require its service or silently export account data. |
| [OSRS Wiki DPS calculator](https://github.com/weirdgloop/osrs-dps-calc) | Existing loadout/monster calculation project; GPL-3.0 repository. | Use as a maintainer validation aid when checking a disputed comparison, not as a new Java dependency or an assumed permissively licensed code source. |

Distribution checks used Plugin Hub tree commit `03534bbdcd2bb2611eabca11c644fde1c992acb6`. Iron Compass is listed under manifest name **`ironpath`**, not `iron-compass`; it pins `96dc43b51c6d2cf96ed179330dd7b5d85edc5af3`. That pinned README and gear JSON were read; the 40-objective catalogue is present there too. The default branch was separately observed at `4f55942723b2f1fd609213ea1b2b528e7641a670`; do not assume its every feature is distributed. [Pinned manifest](https://github.com/runelite/plugin-hub/blob/03534bbdcd2bb2611eabca11c644fde1c992acb6/plugins/ironpath).

The same tree lists Log Adviser and Combat Achievements Tracker. The older `evansloan/collection-log` manifest is **disabled as unmaintained**, so it is not a sensible mandatory dependency despite appearing in searches. [Manifest](https://github.com/runelite/plugin-hub/blob/03534bbdcd2bb2611eabca11c644fde1c992acb6/plugins/collection-log).

**Build decision:** proceed with a small independent plugin and validate useful advice through a simple user experience. Do not fork Iron Compass's entire broad system merely to save scaffolding work. Reuse RuneLite's own public utilities first and avoid reading another plugin's private configuration as an API. Existing plugins may inform technical choices but do not determine whether this project should exist. Nobody has been contacted.

## 4. Knowledge sources and rights

| Source | Appropriate use | Quality and reuse boundary |
|---|---|---|
| [Official Jagex news](https://secure.runescape.com/m=news/archive?oldschool=1) | Releases, balance changes and authoritative mechanical changes | Separate Game Updates from Future Updates/polls. Published does not mean implemented. Link and write original notes; do not redistribute articles/artwork by default. |
| [OSRS Wiki Ironman guide](https://oldschool.runescape.wiki/w/Ironman_guide), item and boss strategy pages | Requirements, acquisition mechanics, rates, variants and encounter context | Wiki data is not a universal progression policy. Strategy/user pages may be opinionated or stale. Direct page/API access was blocked in this environment, so no current full-page item audit was completed. |
| [BRUHsailer web adaptation](https://github.com/umkyzn/BRUHsailer) | A reference route and a way to locate the original authors' material | Efficient-account assumptions may not suit an established account. A web adapter's software licence is not automatically a licence to the underlying guide. Verify provenance and permission before copying. |
| [ironman.guide gear page](https://ironman.guide/gear) | Discover candidate equipment and linked Wiki pages | The publisher says it mirrors Wiki progression and was checked 17 September 2026. It is therefore not independent corroboration of the same Wiki claim. No tables are imported. |
| [Creator video candidate: ToA guide](https://www.youtube.com/watch?v=N2DCZ1nt4p4) | Candidate practical strategy, assumptions and alternative setups | Search returned a 1 April 2026 solo-300 ToA guide. The video itself could not be fetched/viewed; no tactical claim is adopted from it. Evaluate encounter/version/skill context, not subscriber count. Use links and original summaries; no transcript harvesting. |
| Player reports and discussions | Identify missing alternatives, stale advice and disagreements | Leads for validation, not automatic facts. Require a reproducible explanation and independent mechanic checks. |

The Wiki's indexed [licensing template documentation](https://oldschool.runescape.wiki/w/Template:CC-BY-NC-SA/doc) identifies **CC BY-NC-SA 3.0** for Wiki content. The [licence deed](https://creativecommons.org/licenses/by-nc-sa/3.0/) requires attribution, noncommercial use and share-alike terms for adaptations. The full current [Wiki copyright policy](https://oldschool.runescape.wiki/w/RuneScape:Copyrights) was not accessible here; file-specific licences and current terms must be checked before reuse.

Choose BSD-2-Clause for original project code/documentation. Do not relabel copied/adapted Wiki text or guide datasets as BSD. If licensed source material is later redistributed, identify it separately, retain required credit/licence/history links, record modifications and verify compatibility of the intended use. Attribution alone is not permission, and “free plugin” alone does not resolve every reuse condition. Original rules should be independently authored from checked evidence; that is not blanket clearance for systematic extraction. This foundation contains no copied progression dataset or third-party images.

## 5. Update options

| Approach | Assessment |
|---|---|
| Bundled JSON + ordinary Hub updates | Recommended now: reproducible, offline, source-reviewed and reversible. Delivery waits for an accepted Hub update. |
| Maintainer metadata watcher | Sensible later: detect feed/revision changes and produce a review list. No player data or runtime service. Validate API access first. |
| Remote JSON fetched by every player | Defer: adds availability, compatibility, privacy disclosure and review questions. “Only data” does not automatically make arbitrary rules acceptable. No need for this at twelve decisions. |
| Automatic guide ingestion and ranking | Reject for v0: syntax extraction does not establish source independence, legality, context or strategic correctness. |

[MediaWiki Revisions](https://www.mediawiki.org/wiki/API:Revisions) documents revision metadata; [RecentChanges](https://www.mediawiki.org/wiki/API:RecentChanges) supports discovery. These establish a possible mechanism, not verified OSRS Wiki endpoint access or permission. Track the known pages and periodically look for genuinely new sources/content, rather than only polling a fixed list forever.

The live Jagex archive on the research date explicitly separated an upcoming [Fractured Archive overview](https://secure.runescape.com/m=news/the-fractured-archive---everything-you-need-to-know?oldschool=1) from released game updates. This illustrates why a source watcher needs review before changing live recommendations; the overview is not adopted as shipped progression content here.

## 6. Remaining validation gates

- **Value and usability:** test five mid/late-game situations. Players should be able to find a suggestion, understand why it fits their account, inspect an alternative and decide what to do. Include established raiders; no competitor comparison is required.
- **Account accuracy:** test unopened/opened/closed bank, placeholders, variants, logout, hop and two character/profile changes in a real development client. No authenticated game session was available here.
- **Privacy:** verify the supported local file directory API and inspect that neither facts nor manual confirmations enter synced config/logs.
- **Knowledge:** review the initial comparisons against current Wiki/official mechanics and encounter-specific expert advice. Full guide contents and permissions are not verified by search snippets.
- **Effort:** measure how long one small catalogue update takes. Reduce coverage if the proposed weekly review budget is unrealistic.

These uncertainties do not prevent implementation. They do prevent claiming a complete or validated advisor today.

## T2 bounded review — 12 October 2026

T2 fetched current full Wiki pages successfully and reviewed the relevant requirements,
mechanics, alternatives and variants. This supersedes the foundation's access limitation
for these selected sources only. The bundled [source register](../src/main/resources/com/ironpath/catalog.json)
contains 38 exact Wiki revision links and the official RuneLite
[item definitions](https://github.com/runelite/runelite/blob/42a6f17a6a2e8e478aa763890ecd0181a59dad38/runelite-api/src/main/java/net/runelite/api/gameval/ItemID.java).
Access/review dates are 12 October 2026; source revision IDs and decision review dates
are recorded separately. This is an editorial/mechanics review, not an expert consensus
claim, live account audit or player-usefulness test.

The current CoX equipment discussion includes Eye of Ayak and dragon hunter wand
magic alternatives, scythe/Soulreaper melee options, and elder maul/BGS/DWH team
coverage. These informed conservative suppression and original comparisons rather
than an imported equipment ranking. Empty/enhanced/ornamented items are reviewed
as explicit IDs. An empty staff can prevent redundant assembly without establishing
a charged powered weapon; a dragon hunter wand does not establish an available
spellbook or spell. Scythe/Soulreaper components do not count as assembled weapons.
The lance's required Barbarian Training sections are named but remain unobserved.
Default active crystal pieces are supported; coloured armour and other unlisted
forms remain outside this bounded baseline. No boss-access quests or team loadouts
were inferred from weapon possession.

One attempted URL, `Holy_Sanguinesti_staff_(uncharged)`, returned HTTP 404.
It is not an evidence source. The accessible full
[holy staff page](https://oldschool.runescape.wiki/w/Holy_Sanguinesti_staff?oldid=15276380)
shows both charge states and their IDs; the official definitions independently
identify 25731/25733. No factual claim was adopted from the failed URL.

The Wiki's copyright page is a soft redirect. Its linked
[Weird Gloop licensing policy, revision 1299](https://meta.weirdgloop.org/w/Licensing?oldid=1299)
was fetched and read, together with the page footers. They identify CC BY-NC-SA 3.0
for the relevant current Wiki text, with separate media/additional terms.
RuneLite's pinned root licence was also checked as BSD-2-Clause. Source records
describe evidence/reuse scope; original Ironpath text and rules do not redistribute
guide prose, tables, images, source code or a third-party progression dataset.
This limited fact-and-link approach is not blanket permission for future copying.

All nine decisions and the five representative fixtures are documented under T2 in
`TASKS.md`. The fixtures are synthetic; T1's passed static review does not establish
RuneLite accuracy. Real-client checks and player judgement remain pending.
