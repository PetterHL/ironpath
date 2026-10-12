# Ironpath

**Your path to endgame PvM**

P1–P3 development build · 12 October 2026 · plugin 0.3.0 · knowledge p3-1

**Delivered baseline: P1–P3 implement the first broad PvM slice, reusing T1–T3 and T4's historical records.** Sixteen reviewed decisions span CG, Zulrah, ToA, Slayer, Moons, Royal Titans and ordinary team CoX. On 12 October 2026, PR #5 was open and unmerged at `eb22fef`; main remained `c717927`. TASKS.md preserves automated results, the limited user-reported PC flow and pending detailed checks. The upgrade-first experience described below is planned P4a work, not a shipped feature. No Plugin Hub submission or external service exists.

The intended product is a free, open-source RuneLite sidebar that automatically identifies worthwhile Ironman PvM upgrades using observed account state: **which upgrades are worth pursuing, what difference will they make, and how do I obtain them?** Equipment, relevant prayers, skill levels and permanent unlocks are eligible. Evaluation runs locally, without AI, API keys, registration or an owner-operated backend.

## Decision

Proceed with an **upgrade-first vertical slice** on the existing broad PvM foundation. P4a covers approximately 3–4 representative upgrades end to end, including a tested account-aware numerical comparison, supported alternatives and acquisition prerequisites. The default experience works without a goal; optional goals remain secondary. Compact single-column cards lead to value/comparison/route details, with clearly labelled provisional advice when ownership is unknown. Detailed observations and administration move behind secondary controls.

After testing and review, expand toward approximately 8–12 thoroughly researched upgrades, then according to usefulness. These are scope budgets, not quotas or permanent limits; retain useful existing content. Compare actual marginal value rather than recommending every missing item. Theoretical DPS requires an explicit reference setup/target and must not be presented as faster full encounters or more kills per trip. See docs/DESIGN.md for the bounded calculation model and limitations.

Personalised rules are technically feasible. The project will proceed independently, with usefulness and usability validated on representative accounts. Iron Compass remains a reference with overlapping features, not a reason to stop development or a required quality benchmark. Petter's experience is that its current workflow does not meet his usability needs; feature coverage alone does not establish a good user experience.

The product should make useful, encounter-specific trade-offs easy to understand: what improves the account's current options, what is merely convenient, when another grind is unnecessary, and which alternative remains sensible after the player skips a grind. A player should quickly find a few clear recommendations without navigating a large checklist.

## Read and work

| File | Purpose |
|---|---|
| [docs/RESEARCH.md](docs/RESEARCH.md) | Verified findings, source links, limitations and reuse assessment |
| [docs/DESIGN.md](docs/DESIGN.md) | MVP, account-state semantics, small architecture and update policy |
| [TASKS.md](TASKS.md) | Active P4a/P4b/P5 roadmap, implementation baselines, historical evidence and next Codex assignment |
| [AGENTS.md](AGENTS.md) | Persistent instructions for AI-assisted development |

Use `TASKS.md` as the only task list initially. One developer, one repository, small commits. No Trello board, project board, separate knowledge service or additional planning documents. Once other contributors need issue discussions, move active tasks into GitHub Issues rather than maintaining two backlogs.

## Repository

Approved repository name and visibility: **`PetterHL/ironpath`**, public, confirmed by Petter on 12 October 2026. Created on GitHub on 12 October 2026. Use BSD-2-Clause for original work. The included licence does not relicense external guide content, images or dependencies.

Naming note: Iron Compass already uses `ironpath` as its Plugin Hub manifest identifier. This is an independent project; keep the chosen product/repository name, avoid any implied affiliation, and choose a distinct Hub manifest identifier before submission.

Reuse the existing T1–P3 RuneLite implementation and preserve its licence notices. Use its build/login instructions and verify supported APIs when extending it. Do not scaffold the plugin again or request approval again for the already approved repository name and public visibility.

## Start here

The implementation baseline combines PRs #1–#3 (including `b3a93d9`), PR #4's
historical records and main's broad plan in PR #5. All five PRs were still open
and unmerged when this documentation update was prepared. Recheck before work.
The next implementation assignment is P4a in TASKS.md. P4b and publication follow
separately; this update changes documentation only.

The existing official RuneLite template, Gradle 8.10, Java release 11 and notices
are retained; see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). RuneLite resolves
to 1.13.1 in the verified build. Runtime evaluation needs no hosted service.

## Test locally on your PC

Install a **JDK 11** (for example Temurin 11), Git, and set `JAVA_HOME` to that JDK.
Both `java -version` and `javac -version` should work. Gradle is included in the
repository; internet access is needed for its first dependency download.

Clone the repository and check out the combined implementation branch:

```sh
git clone https://github.com/PetterHL/ironpath.git
cd ironpath
git checkout p1-p3-broad-pvm
```

For an existing clone, close the development client, enter its `ironpath` directory,
and update the branch first:

```sh
git fetch origin
git switch p1-p3-broad-pvm
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

On a **standard Ironman in an ordinary permanent members world**, open Ironpath
and allow two server ticks for initial observations/preferences. Open your bank,
then read a suggestion and expand **Why and alternatives**. Pinned source links
are under **Reviewed sources**. Advice is planning, not a complete loadout or a
promise of successful kills.

## Use the delivered P1–P3 build

The following describes the current implementation, including its targeted-absence
confirmation requirement. P4a will introduce upgrade-first/provisional cards and
reference comparisons; those features are not present in the commands above.

1. **No goal** gives up to three materially different activities. Select one
   optional goal to refine priority: ordinary team CoX, Rigour/Augury use levels,
   owned-parts swamp assembly, ranged routes, powered-magic progression or
   low-level ToA / a fang. There are no free-text or multiple queued goals.
2. Goals distinguish access, suggested preparation, use levels and target
   outcomes. Confirmed missing prerequisites appear separately from available
   cards. **ANY** routes remain alternatives; optional supplies or higher prayers
   do not become game access requirements.
3. Acquisition advice needs a **targeted planning confirmation** when ownership
   cannot be established. An empty or unopened bank does not mean an upgrade is
   missing globally. Under **Skips and manual confirmations**, select only the
   target indicated by the goal (for example “Bowfa … or twisted bow owned”),
   then **Not owned / locked** if correct. Positive observed or manually owned
   supported alternatives override that absence. Confirmations have provenance
   and can be cleared; there is no account-wide questionnaire.
4. **Skip this activity** excludes its acquisition route. Skipping **CG** never
   hides an owned Bowfa/crystal kit, and Moons can remain a reviewed alternative
   when its facts support it. The controls also retain individual action skips
   for owned reminders. **Restore activity/action** reverses the corresponding
   choice; a skip never completes an outcome.
5. **Owned / unlocked** can suppress a redundant grind or provide an unknown
   quest/unlock fallback. It cannot create a charged, equipped or usable weapon.
   Readable client quest/unlock facts take precedence; observed possession beats
   manual absence. **Clear selected fact** or **Reset all planning choices** is
   reversible.
6. Wait for **Preferences saved locally**, then restart. The same character/profile
   should restore the goal, skips and confirmations, while the bank remains
   unknown until opened. Check another character and RuneLite profile separately.
   These are pending PC checks, not automated game-test results.

Controls wait for identity and preference loading. A corrupt/unsupported file
disables editing to protect it; a failed save labels current changes memory-only.
T3 preference schema 1 is read with stable goal/action/item keys intact. The next
explicit save writes schema 2, which also accepts activity and ownership-target
keys. Old action skips are not widened; retired identifiers are explicitly dropped.

## Privacy and coverage

Real levels, inventory/equipment, session bank, Rigour/Augury and Deadeye/Mystic
Vigour unlocks, plus completion of Song of the Elves, Regicide, Beneath Cursed Sands
and Perilous Moons are read on the client thread into immutable snapshots.
Unavailable quest scripts, containers or skills stay unknown. Quests use supported
`Quest.getState`; selected progress events coalesce into a read, while idle ticks
retain the existing snapshot. No KC, Collection Log, resource quantities or player
skill are inferred.

The catalogue recognises selected exact active/charged, empty and cosmetic forms
for distinct purposes. Bowfa usable-package checks still cover default-colour active
crystal armour. Atlatl acquisition is supported, but its full strength/ammunition
setup is outside the CoX baseline. Only the base keris partisan is recognised for
ToA preparation. No complete loadouts, solo/Expert/Challenge Mode certification,
all bosses, or advanced-item acquisition catalogue is promised. Advanced owned
magic alternatives may suppress a redundant route without proving a spell setup.

CG planning begins with the regular Gauntlet unlock; a prior regular completion is
unobserved. Zulrah's partial Regicide/dialogue access is conservatively represented
by completed Regicide plus a labelled dialogue check. Slayer advice seeks a task;
it never claims a current cave-kraken task. Moons' Herblore baseline and Titans'
duo/spell assumptions are preparation, not game entry requirements. Source review
is static editorial review, not expert/player endorsement or in-game validation.

Raw observations and character/profile identity stay in memory. Only explicit goal,
action/activity skips and manual confirmation values/times are saved through
RuneLite's supported `Filepath` API:

```text
.runelite/plugin-data/ironpath-advisor/<SHA-256 identity key>.json
```

On Windows this is under `%USERPROFILE%\.runelite`. The existing key separates
configuration profile, RuneScape profile and character without a username or raw
account ID. Files are local and unencrypted; keep them private. Reset writes empty
choices for the current identity. No bank, live levels/quests/unlocks or observed
timestamps are serialized, synced through ConfigManager, logged or uploaded.
Only fixed source links open browser traffic, carrying no account data.

Logout, connection loss, hop, region loading, profile/character changes, unsupported
modes/worlds and plugin disable clear observations. Reopen the bank afterwards.
Preference IO stays serial and off the client thread; delayed results and UI actions
check identity/generation. The first-save directory fix and lifecycle guards from
`b3a93d9` remain in place.

Automated fixtures use synthetic observations, isolated temporary storage and
mocked client/UI events. They do not establish live accuracy or player usefulness.
Limited earlier T1 user-performed passes and the simulated T4 maintenance rehearsal
are preserved verbatim in TASKS.md. Unperformed T1–T4 and P1–P3 real-client checks
remain pending; no client was launched for this work and no Hub approval is claimed.
