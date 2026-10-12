# Ironpath

**Your path to endgame PvM**

T1 development experiment · 12 October 2026

**Status: T1 development build implemented; automated tests and build pass. In-game accuracy and usability checks are pending.** No Plugin Hub submission or external service has been created. T2–T5 have not started.

The intended product is a free, open-source RuneLite sidebar that helps an Ironman choose a useful next PvM activity or equipment objective using observed account state. Evaluation runs locally, without AI, API keys, registration or an owner-operated backend.

## Decision

Proceed with a small proof of value, not a general OSRS progression platform. Start with **CoX-focused progression for midgame and established raiding accounts**, covering at most 12 curated decisions. General mode works without a selected goal, within this explicitly labelled catalogue. Optional goals refine those recommendations.

Personalised rules are technically feasible. The project will proceed independently, with usefulness and usability validated on representative accounts. Iron Compass remains a reference with overlapping features, not a reason to stop development or a required quality benchmark. Petter's experience is that its current workflow does not meet his usability needs; feature coverage alone does not establish a good user experience.

The product should make useful, encounter-specific trade-offs easy to understand: what improves the account's current options, what is merely convenient, when another grind is unnecessary, and which alternative remains sensible after the player skips a grind. A player should quickly find a few clear recommendations without navigating a large checklist.

## Read and work

| File | Purpose |
|---|---|
| [docs/RESEARCH.md](docs/RESEARCH.md) | Verified findings, source links, limitations and reuse assessment |
| [docs/DESIGN.md](docs/DESIGN.md) | MVP, account-state semantics, small architecture and update policy |
| [TASKS.md](TASKS.md) | Five ordered tasks, acceptance gates and the first Codex assignment |
| [AGENTS.md](AGENTS.md) | Persistent instructions for AI-assisted development |

Use `TASKS.md` as the only task list initially. One developer, one repository, small commits. No Trello board, project board, separate knowledge service or additional planning documents. Once other contributors need issue discussions, move active tasks into GitHub Issues rather than maintaining two backlogs.

## Repository

Approved repository name and visibility: **`PetterHL/ironpath`**, public, confirmed by Petter on 12 October 2026. Created on GitHub on 12 October 2026. Use BSD-2-Clause for original work. The included licence does not relicense external guide content, images or dependencies.

Naming note: Iron Compass already uses `ironpath` as its Plugin Hub manifest identifier. This is an independent project; keep the chosen product/repository name, avoid any implied affiliation, and choose a distinct Hub manifest identifier before submission.

The T1 scaffold follows the official RuneLite example plugin at `46e14f2c448fab6cbc599df1015b9631dcf1fc52`, checked on 12 October 2026: Gradle 8.10 and Java release 11. The current `latest.release` dependency resolved to RuneLite `1.13.1` for this build. Original notices are retained; see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

## Start here

The first build is a narrow account-data and recommendation experiment. Its results and remaining checks are recorded under T1 in `TASKS.md`. The wider design describes later tasks, not features already implemented.

The design introduces no per-user hosted inference, storage or API expenses. Development and content review still cost time; free distribution does not make knowledge maintenance automatic.

## Test locally on your PC

Install a **JDK 11** (for example Temurin 11), Git, and set `JAVA_HOME` to that JDK.
Both `java -version` and `javac -version` should work. Gradle is included in the
repository; internet access is needed for its first dependency download.

Clone the repository and check out the T1 pull-request branch:

```sh
git clone https://github.com/PetterHL/ironpath.git
cd ironpath
git checkout t1-account-advisor
```

Windows PowerShell:

```powershell
.\gradlew.bat test build
.\gradlew.bat run
```

macOS / Linux:

```sh
./gradlew test build
./gradlew run
```

The template's `run` task opens the RuneLite development client with Ironpath
loaded and developer mode enabled. For a Jagex Account, follow RuneLite's
[Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts)
instructions to log into a development client. Do not put credentials in this
repository. Close the client to stop it.

On a **standard Ironman in an ordinary permanent members world**, enable Ironpath
if necessary and open its toolbar button. Allow two server ticks for initial
observations. Open your bank, then read a suggestion and expand **Why and
alternatives**. A source button opens the pinned source revision in your browser.
The samples cover reading an already observed Rigour/Augury scroll, training
Prayer for an already unlocked prayer, and planning ordinary team CoX with
recognised weapons. Team planning does not certify raid access or a complete
usable loadout. No CG grind is required by these samples.

If no recommendation appears, expand **Why no suggestion** to inspect completed
prayer samples and unconfirmed skill/weapon conditions. An uncharged staff does
not satisfy the charged magic-weapon sample; unobserved storage remains unknown.

## T1 privacy and coverage

Real levels, inventory, equipment, session bank observations and two permanent
prayer unlock varbits are read on the client thread into immutable snapshots.
An unopened bank, unavailable container or missing skill is unknown. Permanent
unlocks are separate from scroll possession; uncharged gear and components do
not count as recognised charged weapons. Exact supported variants are bundled
in `catalog.json`; other combinations can produce no advice.

Observations and internal character/profile identity remain in memory. Ironpath
does not write preferences or account facts, use synced configuration for facts,
log account identifiers or bank contents, perform account lookups, download rules,
or upload data. Evaluation uses bundled content and injected Gson. User-initiated
source links contain no account state. RuneLite itself retains its normal network
and configuration behaviour.

Snapshots clear on logout, connection loss, world hop, profile/character changes,
unsupported modes/worlds and plugin disable. T1 also clears on region loading, so
you may need to reopen your bank after moving. The character identity and a
generation/revision guard reject stale callbacks and queued sidebar updates.
Supported local persistence was checked against `Plugin.getPluginDirectory()`
and `Filepath`; goals, skips, manual confirmations and their persistence are deferred.

Initial user-performed Windows checks passed on 12 October 2026: local build,
Jagex Account development login, sidebar opening, an unknown bank before its
first opening and the bank observation status transition after opening.
Evidence and limitations are recorded in [T1](TASKS.md).

Remaining real-client checks are **pending**: displayed skill/unlock accuracy;
bank item-capture and close/filter freshness; the empty-result explanation and
a positive recommendation; placeholders and charged/cosmetic variants;
consumed scroll unlock timing; logout/hop/reconnect; two characters and RuneLite
profiles; unsupported worlds; disable/re-enable cleanup; source buttons; and
recommendation usefulness. The test fixtures are synthetic and establish logic
behavior, not player judgement.
