# Initial tasks

Updated 12 October 2026. Only this file tracks initial work. **T1 is implemented for review; automated checks pass and in-game checks are pending. T2–T5 are not started.** Petter has approved the public repository `PetterHL/ironpath`; the repository and documentation foundation are established on GitHub. Independent development is agreed; competitor benchmarking is optional.

## T1 — Small account and value experiment (ready for code review; in-game checks pending)

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

**Automated validation:** `gradlew test` and `gradlew build` passed together on JDK `11.0.32.1`; **20 tests, zero failures**. Product fixtures and mocked RuneLite lifecycle tests are separate. Tests cover unopened banks/unknown completion, decline-CG alternatives, post-Bowfa and high-end accounts, consumed unlocks/spare scrolls, low-level scroll reading, inactive gear/components, individual wield levels, immutable evidence, three-valued conditions, unreviewed/invalid catalogue rejection, placeholders, cached-bank freshness, all game-state transitions, character/profile changes, changed identity without an event, unsupported modes/worlds, disable, stale result/view rejection and irrelevant-varbit coalescing. These are synthetic observations, not captured accounts or in-game tests.

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

**Pending real-client checks:** login initialisation and unlock varbits; unopened/opened/closed/filtered bank and observation times; placeholders and supported charged/cosmetic variants; consumed unlocks; logout, hop, reconnect and region-loading clears; two characters and RuneLite profile changes; unsupported modes/worlds; disable/re-enable; source links and sidebar usability. No player usefulness judgement, game accuracy pass or Hub approval is claimed. See README for simple Windows/macOS/Linux build and launch commands and Jagex Account login guidance. T2 remains unstarted; no Hub submission.

## T2 — Review the bounded catalogue

Depends on T1's working foundation. Select at most twelve decisions within the CoX scope; at least four must help established accounts. Check current alternatives and hard requirements, write original explanations and effort assumptions, and attach source/revision/review/licence metadata. Do not allow one guide's opinion to become a hard requirement. Validate IDs, references, `all`/`any` conditions, cycles and missing evidence. Finish five representative account fixtures with expected explanations and alternatives, not just exact ordering snapshots.

## T3 — Complete the local recommendation slice

Implement deterministic ranking, up to three diverse choices, optional single goal, nearest prerequisite, skip/restore and reversible manual confirmations. Keep one sidebar. Verify owned gear removes redundant grinds, source links carry no account data, skipped requirements do not become completed, unknowns do not fabricate advice, and preferences survive restart without leaking between characters.

## T4 — Small user and maintenance trial

Test with Petter and, if available and explicitly invited, one or two experienced Ironmen. No unsolicited outreach. Check whether they can find a recommendation without assistance, understand its explanation, inspect an alternative and skip a grind. Acceptance target: at least four of five scenarios yield an actionable or correctly abstaining result, zero critical ownership/access errors, and at least three recommendations the player considers useful for the supplied account. These are proposed acceptance criteria, not measured results. Simulate a relevant source change and time the review/update process. Do not automate discovery unless it saves observed repeated work.

## T5 — Prepare distribution

After T4 passes: final licence/attribution check, concise install/privacy/coverage documentation, metadata/icon, supported-client build and a short manual smoke check. Choose a distinct Plugin Hub manifest identifier (`ironpath` is already used by Iron Compass). Follow current Plugin Hub rules and submit the pinned public source commit only when publication is authorised. Record plugin version, knowledge version and last review date. First release is the CoX slice; no implicit promise of comprehensive endgame coverage.

## First Codex assignment

Use this when implementation is requested. The repository name and public visibility have already been approved:

> Read AGENTS.md, README.md, docs/DESIGN.md and T1 in TASKS.md. Implement T1 only. Begin from the current official RuneLite example-plugin template and preserve its notices; verify today's supported Java target and public APIs. Build a minimal local account snapshot and a pure evaluator with three to six sample decisions, then expose a simple, understandable sidebar. Use unknown state for unobserved bank/unlocks, clear snapshots on all account transitions, and keep bank data in memory. Verify RuneLite's supported local-file API before persisting preferences; do not place account facts in synced configuration. Separate synthetic test logic from reviewed advice. This is an independent project: Iron Compass is an optional reference, not a required benchmark or reason to redirect the work. Do not add a backend, runtime AI, external account lookups, scraper, remote knowledge updater, full catalogue or polished UI. Run meaningful logic/lifecycle tests and a build. Record in-game checks as pending unless actually performed. Report what works, how to run it locally and what remains to validate; update T1 only. Do not submit to Plugin Hub.
