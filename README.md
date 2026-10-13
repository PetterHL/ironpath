# Ironpath

**Your path to endgame PvM**

P4a development build · 12 October 2026 · plugin 0.4.0 · knowledge p4a-1

**Delivered: P4a's four-upgrade slice on the P1–P3 foundation.** Occult necklace,
trident of the swamp, Zaryte crossbow and base avernic treads now have no-goal
cards, provisional ownership, alternatives and acquisition details. A bounded,
local powered-magic comparison uses an explicit reference setup against ordinary
Barrows Dharok. The existing sixteen broad PvM decisions remain accessible.
TASKS.md records the current automated results and pending PC checks; its earlier
75-test result and limited user-reported flow remain historical evidence.
No Plugin Hub submission or external service exists.

The intended product is a free, open-source RuneLite sidebar that automatically identifies worthwhile Ironman PvM upgrades using observed account state: **which upgrades are worth pursuing, what difference will they make, and how do I obtain them?** Equipment, relevant prayers, skill levels and permanent unlocks are eligible. Evaluation runs locally, without AI, API keys, registration or an owner-operated backend.

## Decision

The **upgrade-first vertical slice** is implemented on the existing broad PvM foundation. Four outcomes have value, alternatives and route details; occult and swamp share a tested numerical model. The default experience works without a goal. Compact single-column cards open details with Back navigation, while goals, observations and administration are under Account and preferences. Unknown ownership remains provisional; access, preparation and equip requirements remain separate.

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
P4a is delivered on `p4a-upgrade-advisor`, stacked on PR #6
(`docs/upgrade-first-roadmap` at `2ef409a`), which contains PR #5
(`eb22fef`). Main was rechecked at `c717927`; both PRs remained open and unmerged.
This branch changes implementation, catalogue and validation documentation. P4b
and publication remain separate assignments after review.

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
git checkout p4a-upgrade-advisor
```

For an existing clone, close the development client, enter its `ironpath` directory,
and update the branch first:

```sh
git fetch origin
git switch p4a-upgrade-advisor
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
then open a card with **Open details**. Pinned source links are under
**Reviewed sources**. Advice is planning, not a complete loadout or a promise of
successful kills. Leave account cases you cannot exercise pending.

## Use the P4a build

1. Open with **no goal**. Up to three primary upgrade cards show a supported
   effect, contexts, remaining work and a confidence/access label. A fourth
   supported outcome is under **More supported upgrades**. A supported observed
   or manually owned baseline can justify a provisional card; an empty bank
   alone cannot. Unsupported contexts receive a coverage explanation.
2. Choose **Open details** for baseline evidence, alternatives, equip levels,
   acquisition requirements and sources. **Back to upgrades** returns to the
   cards. A relevant inaccessible outcome stays visible as **Plan ahead**;
   ownership or access checks never become an available grind by assumption.
3. Confirm only the named ownership fact if appropriate. **Already owned**
   suppresses redundant acquisition; **Confirm not owned** and **Clear
   confirmation** are reversible. Positive observed/manual possession wins over
   a conflicting absence. Manual ownership cannot create charged/usable gear.
4. Occult/swamp details offer **Reference setup and assumptions**. The proposal
   uses observed real levels and supported exact tridents, neck and boot pieces;
   every other slot is deliberately empty. It defaults to no offensive prayer,
   no boost and Accurate. Numbers before confirmation are a **proposed reference
   scenario**, not your actual loadout. Correct a supported weapon/neck/feet,
   prayer, boost or style, then **Confirm reference**. Unsupported equipped
   pieces or unread equipment withhold numbers until you explicitly choose
   **Use minimal template** to exclude them. No missing piece silently gives
   zero bonuses; empty/uncharged/manual weapons cannot enter the baseline.
5. The calculation covers uninterrupted ordinary **Dharok, NPC 1673** attacks.
   It reports max hit, hit chance (changes in percentage points) and theoretical
   DPS. It excludes wind weakness for tridents, venom/poison, set effects,
   advanced staffs, raid scaling, movement and resources. Wind spells remain a
   Barrows alternative. These numbers never promise kill time, trip length or
   deeper delves. Zaryte and treads have qualitative/stat comparisons, not a
   bolt, full-loadout or Doom combat model.
6. Reference corrections/confirmations stay in memory. Relevant level, item or
   manual-input changes invalidate confirmation; identity/lifecycle changes
   clear the setup. Same-value observation refreshes retain confirmation.
   **Quick actions** separates owned scroll/switch/assembly reminders.
   **Existing broad PvM advice**, including **More available activities**, retains
   CG, Moons, Titans, Slayer, ToA and ordinary team CoX advice and sources.
7. **Account and preferences** contains observations, diagnostics, optional goals,
   manual planning assumptions and activity/action skip/restore. A skip excludes
   acquisition work without completing an outcome or hiding owned reward
   utility. Current task, Nex entry/team and supply/quantity assumptions need
   rechecking before play; their manual timestamps are not live observations.
   Wait for **Preferences saved locally**, then restart or change character/
   profile to check isolated choices and cleared observations/reference setups.
   These PC checks remain pending.

The retained activity evaluator still needs targeted absence confirmations for
missing-item grinds. Its ranking now puts contextual benefit before optional goal
relevance and remaining effort; it does not count goal aliases. A goal retains its
own route/next-step view even when a higher-value activity leads the secondary
list. Owned-parts swamp assembly remains a quick action and an inspectable route
of the grouped swamp outcome. Treads' Cerberus additions are optional, with
separate skill/component/4,000-tear checks. Cloth and Eye opportunities retain
separate ownership/value and delve eligibility; their gains are not combined.

Controls wait for identity and preference loading. A corrupt/unsupported file
disables editing to protect it; a failed save labels current changes memory-only.
T3 preference schema 1 is read with stable goal/action/item keys intact. The next
explicit save writes schema 2, which also accepts activity and ownership-target
keys. Old action skips are not widened; retired identifiers are explicitly dropped.

## Privacy and coverage

Real levels, inventory/equipment, session bank, Rigour/Augury and Deadeye/Mystic
Vigour unlocks, plus completion of Song of the Elves, Regicide, Beneath Cursed Sands,
Perilous Moons, The Frozen Door and The Final Dawn are read on the client thread
into immutable snapshots.
Unavailable quest scripts, containers or skills stay unknown. Quests use supported
`Quest.getState`; selected progress events coalesce into a read, while idle ticks
retain the existing snapshot. No KC, Collection Log, resource quantities or player
skill are inferred.

The catalogue recognises selected exact active/charged, empty and cosmetic forms
for distinct purposes. Bowfa usable-package checks still cover default-colour active
crystal armour. Atlatl acquisition is supported, but its full strength/ammunition
setup is outside the CoX baseline. Only the base keris partisan is recognised for
ToA preparation. The magic reference is a restricted template, not a complete observed loadout.
No solo/Expert/Challenge Mode certification, all-boss coverage or general advanced
acquisition catalogue is promised. Advanced owned
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
choices for the current identity. No bank, live levels/quests/unlocks, observed timestamps or reference setups are
serialized, synced through ConfigManager, logged or uploaded.
Only fixed source links open browser traffic, carrying no account data.

Logout, connection loss, hop, region loading, profile/character changes, unsupported
modes/worlds and plugin disable clear observations. Reopen the bank afterwards.
Preference IO stays serial and off the client thread; delayed results and UI actions
check identity/generation. The first-save directory fix and lifecycle guards from
`b3a93d9` remain in place.

Automated fixtures use synthetic observations, isolated temporary storage and
mocked client/UI events. They do not establish live accuracy or player usefulness.
Limited earlier T1 user-performed passes and the simulated T4 maintenance rehearsal
are preserved verbatim in TASKS.md. Unperformed T1–T4, P1–P3 and P4a real-client checks remain pending. Headless Swing
previews and mocked controls do not establish game-client usefulness. No client
was launched for this work and no Hub approval is claimed.
