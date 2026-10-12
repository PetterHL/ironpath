# Ironpath

**Your path to endgame PvM**

T3 development build · 12 October 2026 · plugin 0.2.0 · knowledge t3-1

**Status: T3 implements the local recommendation slice. Automated tests/build pass; T3 real-client checks remain pending.** Earlier user-performed T1 checks and T2 static results are recorded in `TASKS.md`. Petter reports that T2 works on his PC; this does not establish every accuracy/usefulness check. T4/T5 have not started. No Plugin Hub submission or external service has been created.

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

The first build is a narrow account-data and recommendation experiment. T1–T3 results and remaining checks are recorded in `TASKS.md`. The nine-decision catalogue stays bounded to CoX preparation. The wider design also describes later tasks.

The design introduces no per-user hosted inference, storage or API expenses. Development and content review still cost time; free distribution does not make knowledge maintenance automatic.

## Test locally on your PC

Install a **JDK 11** (for example Temurin 11), Git, and set `JAVA_HOME` to that JDK.
Both `java -version` and `javac -version` should work. Gradle is included in the
repository; internet access is needed for its first dependency download.

Clone the repository and check out the T3 branch (built on the unmerged T2 PR):

```sh
git clone https://github.com/PetterHL/ironpath.git
cd ironpath
git checkout t3-local-advisor
```

For an existing clone, close the development client, enter its `ironpath` directory,
and update the branch first:

```sh
git fetch origin
git switch t3-local-advisor
git pull --ff-only
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
repository. Close the client to stop it. If you used RuneLite's
`--insecure-write-credentials` setup, remove that argument from the launcher's
Client arguments after generating the local credentials, and delete the local
`.runelite/credentials.properties` when development login testing is finished.
Do not upload or commit that file.

On a **standard Ironman in an ordinary permanent members world**, enable Ironpath
if necessary and open its toolbar button. Allow two server ticks for initial
observations. Open your bank, then read a suggestion and expand **Why and
alternatives**. A source button opens the pinned source revision in your browser.
The catalogue covers reading an already observed Rigour/Augury scroll, training
Prayer for an already unlocked prayer, and planning ordinary team CoX with
recognised weapons, assembling an owned magic fang upgrade, and reviewing owned
lance, defence-reduction and occult options. Team planning does not certify raid
access or a complete usable loadout. No CG grind is required by this catalogue.

## Try T3 in the sidebar

1. Open your bank, then return to Ironpath. Check **Knowledge t3-1** and wait for
   the local preference load. **No goal** gives up to three different choices.
2. Select an optional goal and press **Set goal**: ordinary team CoX,
   Rigour, Augury, or a review of an owned-parts swamp trident upgrade.
   Read the goal status and **Goal prerequisites and sources**. A blocked goal
   shows its nearest confirmed missing prerequisite separately; unknown ownership
   is not marked missing. If no reviewed acquisition route exists, the sidebar
   says so rather than inventing a grind.
3. Expand **Why and alternatives** on a card. Ranking uses selected-goal relevance,
   contextual improvement over recognised owned alternatives, then the authored
   effort band and stable decision ID. There is at most one choice per family;
   specific defence-reduction coordination replaces broad team planning.
4. Use **Skip this action**, or select any activity under **Skips and manual
   confirmations**. **Restore action** reverses a skip. Skipping a required unlock
   blocks that route; it never marks the unlock complete.
5. In that same section select an exact supported item or prayer unlock, then
   **Owned / unlocked** or **Not owned / locked**. Confirmations show their manual
   provenance/time and can be cleared individually or reset with all planning
   choices. Manual ownership can suppress redundant assembly and inform goal
   planning; it cannot establish charged, equipped or immediately usable gear.
   Readable prayer unlock varbits take precedence over manual fallback facts.
   Positive observed possession overrides a manual absence.
6. Wait for **Preferences saved locally**, restart the client and verify the goal,
   skips and confirmations return for the same character/profile. The bank should
   be unknown until opened again. Check another character and RuneLite profile:
   their choices should be separate. These are pending PC checks, not Cloud results.

The goal controls are disabled until identity/local preferences are ready. A corrupt
or unsupported local file disables editing to protect it; a failed save says that
current changes are memory-only. No-goal mode and **Clear goal** remain optional;
there are no free-text goals, multiple queues or universal acquisition routes.

## Privacy and coverage

Real levels, inventory, equipment, session bank observations and two permanent
prayer unlock varbits are read on the client thread into immutable snapshots.
An unopened bank, unavailable container or missing skill is unknown. Permanent
unlocks are separate from scroll possession; uncharged gear and components do
not count as recognised charged weapons. Exact supported variants are bundled
in `catalog.json`; other combinations can produce no advice. Great Kourend access,
Barbarian Training, supplies, player skill and full raid loadouts remain unverified.

Raw observations and character/profile identity stay in memory. Only the selected
goal, skipped decision IDs and explicit manual confirmation values/times are written
to a plugin-owned local JSON file through supported RuneLite `Filepath` APIs:

```text
.runelite/plugin-data/ironpath-advisor/<SHA-256 identity key>.json
```

On Windows this is under `%USERPROFILE%\.runelite`. The key separates the current
RuneLite configuration profile, RuneScape profile and character; it contains no
username or raw account ID. The file is local, not encrypted; keep it private.
No bank contents, live unlocks, levels or observed timestamps are serialized.
**Reset all planning choices** removes the current character/profile's saved
choices (the file then holds an empty preference document).

Ironpath does not write these facts to synced ConfigManager configuration, log
account identifiers or bank contents, perform account lookups, download rules or
upload data. Evaluation uses bundled content and injected Gson; browser buttons
use only fixed pinned source URLs. RuneLite retains its normal network/configuration
behaviour. The catalogue still contains the nine T2 decisions and 39 source records;
T3 changes ranking metadata and the local workflow, not source evidence or coverage.

Snapshots and active planning state clear on logout, connection loss, world hop,
profile/character changes, unsupported modes/worlds and plugin disable. Region
loading also clears conservatively, so you may need to reopen your bank after moving.
Persisted explicit choices reload only for the matching character/profile. Worker
IO is serial and off the client thread; loads, save notifications, queued UI actions
and Swing views reject stale character/generation results. Idle ticks do not evaluate
the catalogue again.

Automated fixtures establish deterministic logic, temporary-file persistence and
mocked lifecycle/UI behavior. **All T3 in-game checks are pending**, including PC
restart/file behavior, goals/skips/manual controls, two characters/profiles and
readability. Earlier T1 passes are limited to those recorded in `TASKS.md`; bank
item accuracy/freshness, variants, unlock timing, hop/logout/region transitions,
unsupported worlds, disable/re-enable, source buttons and usefulness still need
specific real-client evidence. No Hub approval is claimed.
