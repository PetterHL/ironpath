# Ironpath

**Your path to endgame PvM**

PvM progression roadmap · updated 12 October 2026

**Status: development implementation in open PRs; broader PvM expansion is the next assignment.** At this update main contains the documentation foundation. T1–T3 implementation and T4 trial preparation are on stacked branches; see TASKS.md for exact baselines and pending checks. No Plugin Hub approval/submission or external service exists.

The intended product is a free, open-source RuneLite sidebar that helps an Ironman choose a useful next PvM activity or equipment objective using observed account state. Evaluation runs locally, without AI, API keys, registration or an owner-operated backend.

## Decision

Proceed directly to **broader Ironman PvM progression across midgame and endgame entry**. CoX remains one supported activity. The next delivery combines the existing foundation with a small model generalisation and 12–16 reviewed decisions across multiple activities; the first wider catalogue targets approximately 35–45 decisions. General mode works without a selected goal; one optional goal refines advice. Coverage remains explicit, not every boss or item. The old CoX-only release and user-trial gate no longer block expansion; unperformed tests remain pending.

Personalised rules are technically feasible. The project will proceed independently, with usefulness and usability validated on representative accounts. Iron Compass remains a reference with overlapping features, not a reason to stop development or a required quality benchmark. Petter's experience is that its current workflow does not meet his usability needs; feature coverage alone does not establish a good user experience.

The product should make useful, encounter-specific trade-offs easy to understand: what improves the account's current options, what is merely convenient, when another grind is unnecessary, and which alternative remains sensible after the player skips a grind. A player should quickly find a few clear recommendations without navigating a large checklist.

## Read and work

| File | Purpose |
|---|---|
| [docs/RESEARCH.md](docs/RESEARCH.md) | Verified findings, source links, limitations and reuse assessment |
| [docs/DESIGN.md](docs/DESIGN.md) | MVP, account-state semantics, small architecture and update policy |
| [TASKS.md](TASKS.md) | Active P1–P5 roadmap, implementation baselines and next Codex assignment |
| [AGENTS.md](AGENTS.md) | Persistent instructions for AI-assisted development |

Use `TASKS.md` as the only task list initially. One developer, one repository, small commits. No Trello board, project board, separate knowledge service or additional planning documents. Once other contributors need issue discussions, move active tasks into GitHub Issues rather than maintaining two backlogs.

## Repository

Approved repository name and visibility: **`PetterHL/ironpath`**, public, confirmed by Petter on 12 October 2026. Created on GitHub on 12 October 2026. Use BSD-2-Clause for original work. The included licence does not relicense external guide content, images or dependencies.

Naming note: Iron Compass already uses `ironpath` as its Plugin Hub manifest identifier. This is an independent project; keep the chosen product/repository name, avoid any implied affiliation, and choose a distinct Hub manifest identifier before submission.

Reuse the existing T1–T3 RuneLite implementation and preserve its licence notices. Use its build/login instructions and verify supported APIs when extending it. Do not scaffold the plugin again or request approval again for the already approved repository name and public visibility.

## Start here

Run P1–P3 together as described at the end of `TASKS.md`. Bring the existing implementation and current main plan onto one working branch, then deliver real choices between PvM activities. No additional CoX-only polish or standalone architecture phase is required. Preserve useful build instructions and historical test results when combining branches.

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
