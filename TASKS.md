# Initial tasks

Updated 12 October 2026. Only this file tracks initial work. **T1 passed static review with limited user-performed PC checks recorded below; T2's catalogue and static verification are complete; T3's local recommendation slice is implemented for review, with its live checks pending. T4/T5 have not started.** Petter has approved the public repository `PetterHL/ironpath`; the repository and documentation foundation are established on GitHub. Independent development is agreed; competitor benchmarking is optional.

## T1 — Small account and value experiment (ready for code review; in-game validation in progress)

**Outcome:** one narrow RuneLite development build that demonstrates reliable account observations and understandable recommendations.

- In the approved repository, add the current official example-plugin scaffold. Keep original notices; use the supported Java target and one Gradle project.
- Capture real skill levels, inventory/equipment, observed bank and one relevant quest/unlock into an immutable snapshot. Display only necessary facts and freshness in a basic sidebar. Treat missing data as unknown.
- Use three to six independently reviewed sample decisions in a pure Java evaluator. Do not copy a competitor's catalogue. Synthetic rule fixtures are acceptable for logic tests but must be labelled and must not appear as real advice.
- Exercise five scenarios: bank unopened; midgame player who declines CG; post-Bowfa raider; advanced account with a high-end item but remaining gaps; and a consumed unlock/variant plus character-switch case. Separate lifecycle regression tests from product judgement.
- Make the basic flow obvious: open the sidebar, observe the bank when needed, read a few suggestions and inspect why/alternatives. Record the account evidence and reasoning behind each suggestion. Do not require using Iron Compass or measuring superiority over another plugin.
- Verify local preference-storage support before adding persistent account facts. Bank observations remain memory-only.

**Done:** `gradlew test` and `gradlew build` pass (or exact blockers are reported); unknown bank and character isolation tests pass; no network/upload path exists in this plugin; live checks are explicitly passed or pending. Produce at least three concrete examples of understandable, account-specific advice across the five scenarios, including an established raider. Do not claim live usability testing when no game session is available.

### T1 results — 12 October 2026

Code review: [PR #1 — Implement T1 local account observations and CoX sidebar](https://github.com/PetterHL/ironpath/pull/1), branch `t1-account-advisor`.

- Implemented the official example-plugin scaffold at `46e14f2c448fab6cbc599df1015b9631dcf1fc52` with original notices, Gradle 8.10, Java release 11 and one Gradle project. Current official RuneLite APIs were checked at `42a6f17a6a2e8e478aa763890ecd0181a59dad38`; the build resolved `latest.release` to `1.13.1`. There are no extra runtime dependencies; Mockito is test-only.
- Added immutable real-level, inventory/equipment, session-bank and Rigour/Augury unlock observations with time/provenance. Only a visible bank following a bank event is sampled; a stale cached container alone is insufficient. Zero-quantity placeholders are excluded. Missing data remains unknown; positive possession and permanent unlocks are separate.
- Added five source-reviewed samples in bundled `catalog.json`: read an observed dexterous scroll, read an observed arcane scroll, train Prayer for unlocked Rigour, train Prayer for unlocked Augury, and plan ordinary team CoX with recognised observed weapons. The evaluator is pure Java with deterministic authored ordering and at most three diverse suggestions; duplicate Prayer-training actions are suppressed. These are limited original preparation explanations, not a reviewed full progression catalogue or an expert/player endorsement. No T2 content expansion was performed.
- Full current Wiki pages were fetched for the limited mechanic/variant review. Source records include pinned revision URLs, author/type, access and review dates, reuse basis and affected sample IDs. No competitor catalogue, Wiki prose/tables, images or guide dataset was copied. The sidebar exposes matching evidence, why, access distinctions, alternatives, trade-offs, qualitative effort, coverage, freshness and source links.
- Raw observations and identity remain memory-only. No preference/manual-fact persistence was added. Supported local storage was verified in RuneLite's `Plugin.getPluginDirectory()` → `.runelite/plugin-data/[internalName]` and `Filepath.joinSegment`/reader/writer APIs; future character preferences must use that local path and character/profile identity rather than synced configuration. `internalName` is set. There are no HTTP clients, account lookups, uploads, runtime scraping, downloaded rules, reflection, subprocesses or gameplay input in production code. Browser source links are fixed, user-initiated and carry no account state.
- All state clears on lifecycle/account/profile transitions, unsupported modes/worlds and disable; region loading also clears conservatively. Client callbacks and queued Swing views check generation/revision, current character/profile identity and current snapshot before applying results. Event changes are coalesced at the next server tick; idle ticks do not reevaluate the catalogue.

**Automated validation:** `gradlew test` and `gradlew build` passed together on JDK `11.0.32.1`; **23 tests, zero failures**. Product fixtures and mocked RuneLite lifecycle tests are separate. Tests cover unopened banks/unknown completion, decline-CG alternatives, post-Bowfa and high-end accounts, consumed unlocks/spare scrolls, low-level scroll reading, inactive gear/components, individual wield levels, immutable evidence, three-valued conditions, unreviewed/invalid catalogue rejection, placeholders, cached-bank freshness, all game-state transitions, character/profile changes, changed identity without an event, unsupported modes/worlds, disable, stale result/view rejection and irrelevant-varbit coalescing. Three additional synthetic checks cover empty-result explanations: an uncharged staff with otherwise supported weapons and completed prayer samples, unknown bank/skill evidence, and crystal-package/individual wield-level coverage. Observing the supported charged form enables the existing CoX sample. These are synthetic observations, not captured accounts or in-game tests.

**User-performed Windows validation — 12 October 2026:** Petter reported `BUILD SUCCESSFUL` for the local `gradlew.bat test build` command, then logged into the development client with a Jagex Account. A supplied sidebar screenshot confirms that Ironpath opens and displays `Bank unknown` before the user has opened the bank. These checks were performed by the user on his PC; Codex did not interact with the game. No screenshot, account identifier, credential, bank content or raw account observation is stored in this repository. Displayed skill/unlock accuracy still needs comparison with the game UI.

| Manual check | Status | Evidence / limitation |
|---|---|---|
| Windows local build/test command | Passed, user reported | `BUILD SUCCESSFUL`; local detailed test output was not supplied. |
| Jagex Account development login and sidebar opening | Passed, user performed | User reported login; supplied image shows the active Ironpath sidebar. |
| Bank remains unknown before first opening | Passed, user performed | User confirmed bank had not been opened; sidebar displays `Bank unknown`. |
| First bank opening updates observation status | Passed, user performed | A subsequent sidebar image displays `Bank last observed this session`. This establishes the status transition, not full item-capture or close/filter accuracy. |
| Positive team CoX suggestion is displayed | Passed, user performed | A later sidebar image shows `Plan ordinary team CoX with your observed weapons` after the earlier empty result. Actual item-capture, provenance accuracy and usefulness still need user verification. |
| Expanded recommendation evidence renders | Passed, user performed | Supplied detail images show the expected three weapon labels, distinct storage provenance, observation times, preparation baseline, access distinction, alternative and trade-off text. Actual storage locations still need user confirmation; this is a rendering check, not an independent account-accuracy pass. |
| Empty-result explanation | Failed in initial UI; correction awaiting user retest | After the bank status updated, the generic empty state did not identify the unmet sample condition. Added a `Why no suggestion` expansion using the same pure evaluator checks; no catalogue or recommendation eligibility was expanded. |
| Displayed skill/unlock accuracy and remaining real-client behavior | Pending | Rendering alone does not establish that the values match the game. |

The following representative profiles remain synthetic; their full product/usefulness checks are separate from the initial manual checks above.

| Synthetic scenario | Concrete evidence and supported result | Product judgement / in-game status |
|---|---|---|
| Bank unopened | No observed weapons or scrolls; unlocks unknown. Abstain and explain bank observation rather than invent missing gear. | Pending |
| Midgame player declines CG | Recognised whip + rune crossbow + charged trident, 80+ style baseline and 70 Prayer. Suggest planning ordinary team CoX with these observed options; CG is not required. Access, supplies, armour and team agreement remain unverified. | Pending |
| Post-Bowfa raider | Corrupted Bowfa + active crystal armour + whip/trident, both prayer unlocks confirmed, 74 Prayer. Suggest training to 77 for already unlocked Augury, alongside team planning; no extra arcane scroll or basic ranged grind. | Pending |
| Advanced account with remaining gap | Observed fang + twisted bow + charged Shadow, 90 style levels, unlocked Rigour/Augury and 72 Prayer. Prioritise 74 Prayer for Rigour and team planning; suppress duplicate training advice and recognise the owned high-end weapons. | Pending |
| Consumed unlock/variant and character switch | Unlocked prayers remain completed when scrolls are absent or spare scrolls remain. Uncharged/inactive/component forms do not satisfy the weapon baseline. Switching characters clears bank/unlocks and rejects the old generation before new observations. | Pending |

**Cloud constraints:** the initial shell clone could not reach `proxy:8080`; enabling the shell's supported network permission resolved it. Without that permission, even Gradle's offline task listing failed local daemon IPC with `java.net.SocketException: Operation not permitted`; commands need the same permission for local sockets. The preinstalled Java 21 runtime failed compilation with `does not provide the required capabilities: [JAVA_COMPILER]`. A temporary Temurin JDK 11 resolved that. Its initial Maven download failed with `PKIX path building failed`; using the configured system CA store `/etc/ssl/certs/java/cacerts` resolved it while keeping TLS verification enabled. No outstanding test/build blocker remains. Swing tests emitted non-fatal `Fontconfig error: No writable cache directories`. `DISPLAY` is unset and no authenticated game session was available; the development client was not launched and no in-game checks were performed.

Reproduction command used in Cloud (temporary paths are environment-specific; PC commands are in README):

```sh
JAVA_HOME=/tmp/ironpath-jdk11 GRADLE_USER_HOME=/workspace/.gradle ./gradlew \
  -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts \
  -Dhttps.proxyHost=proxy -Dhttps.proxyPort=8080 \
  -Dhttp.proxyHost=proxy -Dhttp.proxyPort=8080 test build --no-daemon
```

**Pending real-client checks:** login initialisation and displayed skill/unlock accuracy; bank item-capture, close/filter freshness and observation times; placeholders and supported charged/cosmetic variants; the new empty-result explanation and the positive suggestion's expanded matching evidence; consumed unlocks; logout, hop, reconnect and region-loading clears; two characters and RuneLite profile changes; unsupported modes/worlds; disable/re-enable; source links and recommendation usefulness. Only the manual passes explicitly recorded above are established. No general game accuracy pass, player usefulness judgement or Hub approval is claimed. See README for simple Windows/macOS/Linux build and launch commands and Jagex Account login guidance. T2 remains unstarted; no Hub submission.

## T2 — Review the bounded catalogue (complete for static review; real-client checks pending)

Depends on T1's working foundation. Select at most twelve decisions within the CoX scope; at least four must help established accounts. Check current alternatives and hard requirements, write original explanations and effort assumptions, and attach source/revision/review/licence metadata. Do not allow one guide's opinion to become a hard requirement. Validate IDs, references, `all`/`any` conditions, cycles and missing evidence. Finish five representative account fixtures with expected explanations and alternatives, not just exact ordering snapshots.

### T2 results — 12 October 2026

Branch `t2-cox-catalogue` builds on PR #1's unmerged head `ae9af8758016a7048302d136cffba0ca20e821ff`. T1 passed static review according to Petter; **T1 has not been live-tested**. Its lifecycle implementation and pending manual checks are retained.

- Authored **nine decisions**, keeping the five stable T1 IDs and adding four choices useful to established/post-Bowfa accounts: assemble an observed magic fang/seas-trident upgrade; compare an owned lance for Olm; agree team defence-reduction coverage; review an owned occult switch when a fury is currently equipped. These are bounded preparation choices, not a comprehensive acquisition route, universal gear order or player endorsement. Team coverage replaces the overlapping broad team-planning prompt.
- Reviewed full current pages for the selected mechanics, alternatives and variants. Bundled **39 source records** (38 Wiki revisions and one pinned official RuneLite item-definition file) carry author/type, exact revision URL, accessed/reviewed dates, licence/reuse basis and affected IDs. Each of **56 exact item IDs** has evidence references. Source/reuse findings are recorded in `docs/RESEARCH.md`; no competitor catalogue, guide prose, equipment tables or images were imported.
- Checked alternatives including the Eye of Ayak, dragon hunter wand, Sanguinesti staff/Shadow, scythe/Soulreaper axe and elder maul/BGS/DWH. Owned competing magic forms, including empty staffs, suppress redundant trident assembly. The wand is not counted as a ready powered spell setup. Owned scythe/Soulreaper axe suppresses the narrow lance comparison. Cosmetic and enhanced equivalences are explicit; inactive gear and components do not establish usable weapons. Unlisted variants remain outside coverage.
- Added only bounded observation conditions (`all`/`any`, levels, named exact-item sets, current equipment and the existing two unlocks) and completion/dependency gates. Unknown completion withholds an action; an unread container never proves an item absent. Confirmed Prayer unlocks remain different from spare scrolls. The only hard decision dependencies are Prayer training's permanent unlocks. Planning baselines, crystal kit preferences and team-role discussion remain recommendations; raid access, lance miniquest sections, charges, supplies and spell setups are explicitly unverified where not captured.
- Catalogue loading fails closed for invalid/duplicate IDs, unsupported predicates/schema, malformed conditions, bad item/source/decision references, missing evidence/review/reuse fields, inconsistent source links/revisions/dates, invalid completion definitions and hard-dependency cycles. The existing authored comparator and three-choice/family limit remain; no goal ranking, nearest-prerequisite UI, skip controls, manual confirmations, persistence or other T3 features were added. Reader/lifecycle code and dependencies are unchanged; sidebar changes only correct coverage and explanation text.

**Automated validation:** `gradlew test build --no-daemon` passed on Temurin JDK `11.0.32.1`, with **33 tests, zero failures**, retaining all 20 T1 tests. Five new representative product fixtures assert matching evidence, expected reasoning and alternatives; four decision-boundary and four catalogue-validation tests cover the new semantics and failure modes. `git diff --check` passes. RuneLite resolves to `1.13.1`. The same supported shell network permission, system CA store and proxy arguments documented under T1 were used. Non-fatal Fontconfig cache and Gradle deprecation warnings remain.

| Synthetic T2 fixture | Expected reasoning and alternatives |
|---|---|
| Bank unopened; then positive fang/chisel/trident in inventory | Abstain while competing-weapon completion is unknown. Positive parts do not prove another weapon is absent from the unopened bank. |
| Midgame, 85 combat skills/70 Prayer; whip, rune crossbow, charged seas trident; no prayer unlocks | Plan ordinary team CoX using the observed crossbow, with CG optional. Kourend access and complete raid preparation remain unverified; missing scrolls do not become acquisition advice. |
| Post-Bowfa, 90 combat skills/80 Prayer/80 Crafting; active crystal kit, seas trident, magic fang and chisel | Suggest assembling the owned magic upgrade and team planning. Explain uncharging, scale upkeep and keeping the seas trident as an alternative; no new ranged grind or contrived low-Prayer gap. |
| Established Bowfa raider, 93 combat skills/80 Prayer; swamp trident, DWH and occult; fury currently equipped | Review the owned occult switch and agree defence-reduction coverage. Simpler hybrid switching and teammate coverage are explicit alternatives; current equipment does not prove the usual raid kit is deficient. |
| Established account, 95 combat skills/90 Prayer; Bowfa kit, fang/lance, charged Ayak, BGS; ornamented occult equipped | Compare lance for Olm and agree coverage using the observed BGS/Ayak kit. Keep room-specific alternatives and unknown Barbarian Training visible. Suppress redundant trident assembly and occult advice. |

**Limitations and remaining manual checks:** these fixtures are synthetic, not captured accounts, player judgement or RuneLite testing. No development client was launched; no authenticated game session was available. All T1 real-client checks remain pending. Additionally check fang assembly/uncharge and enhanced forms; default active versus inactive crystal pieces and supported Bowfa colours; empty versus charged Ayak/Sanguinesti/Shadow; ornamented neck/reduction gear; current equipment versus actual raid switches; and whether the four established-account prompts are useful or repetitive. Gear/skill baselines do not certify solo/Challenge Mode readiness or raid access. Unsupported items/variants need external review rather than inferred equivalence. No Plugin Hub submission, T3 implementation or T4 usefulness pass is claimed.

## T3 — Complete the local recommendation slice (ready for review; in-game checks pending)

Implement deterministic ranking, up to three diverse choices, optional single goal, nearest prerequisite, skip/restore and reversible manual confirmations. Keep one sidebar. Verify owned gear removes redundant grinds, source links carry no account data, skipped requirements do not become completed, unknowns do not fabricate advice, and preferences survive restart without leaking between characters.

### T3 results — 12 October 2026

Code review: [PR #3 — Implement T3 local goals, ranking and isolated preferences](https://github.com/PetterHL/ironpath/pull/3), stacked on PR #2.

Branch `t3-local-advisor` builds on T2's `e52728d5313552fd8b0bdaa5eefdcbaa4be00915`.
It also integrates the existing T1 follow-up diagnostics and user-performed check
records from `f224f410f23f8d4f33310a4520aded9e84518b23`. T1/T2 results above describe
those earlier task snapshots; their historical statements about later tasks do not
set the current T3 status. Petter authorised proceeding after reporting that T2
appears to work. That general report is not a pass for specific T2 or T3 scenarios.

- Implemented a pure comparator of selected-goal relevance, contextual benefit,
  authored effort band and stable ID; no efficiency score or time/DPS estimates.
  At most three choices and one per family remain. Owned competing gear suppresses
  redundant assembly/comparisons; specific team coordination takes precedence over
  broad team planning. No-goal mode works; only one optional goal is selected.
- Added four bounded goals: ordinary team CoX learning/improvement, meeting
  Rigour/Augury unlock/use levels, and reviewing an owned-parts swamp trident
  upgrade. The sidebar separates confirmed, missing and unknown prerequisites,
  nearest confirmed missing prerequisite, and the next supported action. A locked
  prayer is still a blocked goal even if Prayer is already high enough. No covered
  acquisition route means abstaining and asking for observation/confirmation,
  not fabricated grind advice. Preparation remains distinct from game access.
- Added skip/restore for all reviewed actions, including prerequisites not currently
  offered as cards. A skipped unlock blocks training until restored or actually
  confirmed complete; skipping never counts as completion. Unrelated viable CoX
  actions can still be shown while one goal is blocked.
- Added reversible exact-item and prayer-unlock confirmations with explicit manual
  provenance/time, individual clear buttons and reset controls in the same sidebar.
  Manual ownership can suppress redundant work and inform planning, but cannot
  create charged/equipped/usable weapon evidence or observed assembly supplies.
  Readable unlock varbits are authoritative; positive observed possession overrides
  conflicting manual absence. Raw snapshots are never modified by confirmations.
- Persisted only goal, skipped IDs and explicit confirmation values/times in local
  schema-1 JSON using supported `Plugin.getPluginDirectory()` / `Filepath` APIs.
  A SHA-256 key isolates configuration profile + RuneScape profile + character;
  no username/raw identity or automatic account observation is serialized. Writes
  use a temporary file and atomic replace with a supported non-atomic fallback.
  Serial worker IO preserves accepted save/load order across disable/re-enable;
  client-thread callbacks and UI actions check current identity/generation before
  applying results. Corrupt/unsupported files disable editing; save failures are
  visible, never falsely reported as saved. No ConfigManager writes or uploads.
- Plugin version is **0.2.0**. Bundled schema **3**, knowledge **t3-1**, replaces
  legacy priority metadata with the small reviewed effort bands. The same nine
  reviewed decisions, 56 exact item IDs, 39 source records and source review dates
  remain; T3 introduces no new source claim, rule-download path or catalogue expansion.
  Current official RuneLite 1.13.1, Java release 11 and the existing template/notices
  remain supported. T4/T5 and Plugin Hub submission were not started.

**Automated validation:** `gradlew test build --no-daemon` on Temurin JDK
`11.0.32.1`: **55 tests, zero failures**. This includes the original T1/T2 fixtures,
T1 abstention explanations, goal ranking/diversity, skipped prerequisite versus
actual completion, unknown ownership, manually owned alternatives, unusable manual
gear, unlock precedence/provenance, fixed source URLs, real temporary-file restart
round trips, character/profile separation, corrupt/oversized/retired preferences,
serialized IO recovery, actual Swing control listeners with mocked client events,
delayed load/action/save callbacks, disable/re-enable, and visible write failure.
These tests use synthetic facts and isolated temporary storage, not live accounts.
`git diff --check` passes.

**Cloud constraints:** the documented network permission, proxy and system CA
arguments from T1 were used; RuneLite remains 1.13.1. No outstanding test/build
blocker. Non-fatal Fontconfig cache and Gradle deprecation warnings remain.
`DISPLAY` is unset and no authenticated game session is available in Cloud, so
Codex did not launch or interact with RuneLite in-game.

| T3 real-client check | Status |
|---|---|
| No-goal / single-goal selection and clear; readable ranking/evidence | Pending |
| Goal prerequisites and nearest missing requirement match game/manual facts | Pending |
| Skip required unlock, blocked route, restore without false completion | Pending |
| Manual positive/negative, provenance, observed-positive precedence, clear/reset | Pending |
| PC file write and restart restore; bank remains unknown until reopened | Pending |
| Two characters and RuneLite profiles; logout/hop/unsupported-world isolation | Pending |
| Disable/re-enable, source buttons and catalogue usefulness | Pending |

PC build/launch and a short T3 check sequence are in `README.md`. Wait for
**Preferences saved locally** before restarting. No T3 in-game pass or T4 user-trial
result is claimed. Earlier limited T1 user-performed passes remain recorded above.

## T4 — Small user and maintenance trial

Test with Petter and, if available and explicitly invited, one or two experienced Ironmen. No unsolicited outreach. Check whether they can find a recommendation without assistance, understand its explanation, inspect an alternative and skip a grind. Acceptance target: at least four of five scenarios yield an actionable or correctly abstaining result, zero critical ownership/access errors, and at least three recommendations the player considers useful for the supplied account. These are proposed acceptance criteria, not measured results. Simulate a relevant source change and time the review/update process. Do not automate discovery unless it saves observed repeated work.

## T5 — Prepare distribution

After T4 passes: final licence/attribution check, concise install/privacy/coverage documentation, metadata/icon, supported-client build and a short manual smoke check. Choose a distinct Plugin Hub manifest identifier (`ironpath` is already used by Iron Compass). Follow current Plugin Hub rules and submit the pinned public source commit only when publication is authorised. Record plugin version, knowledge version and last review date. First release is the CoX slice; no implicit promise of comprehensive endgame coverage.

## First Codex assignment

Use this when implementation is requested. The repository name and public visibility have already been approved:

> Read AGENTS.md, README.md, docs/DESIGN.md and T1 in TASKS.md. Implement T1 only. Begin from the current official RuneLite example-plugin template and preserve its notices; verify today's supported Java target and public APIs. Build a minimal local account snapshot and a pure evaluator with three to six sample decisions, then expose a simple, understandable sidebar. Use unknown state for unobserved bank/unlocks, clear snapshots on all account transitions, and keep bank data in memory. Verify RuneLite's supported local-file API before persisting preferences; do not place account facts in synced configuration. Separate synthetic test logic from reviewed advice. This is an independent project: Iron Compass is an optional reference, not a required benchmark or reason to redirect the work. Do not add a backend, runtime AI, external account lookups, scraper, remote knowledge updater, full catalogue or polished UI. Run meaningful logic/lifecycle tests and a build. Record in-game checks as pending unless actually performed. Report what works, how to run it locally and what remains to validate; update T1 only. Do not submit to Plugin Hub.
