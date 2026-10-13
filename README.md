# Ironpath

**Your path to endgame PvM**

R1 development build · 13 October 2026 · plugin 0.5.0 · knowledge r1-1

**Delivered: realistic powered-magic references and first endgame value.** Shadow,
Ancestral hat/top/bottom, tormented bracelet and Magus ring join occult, swamp,
Zaryte and treads. Complete supported slot setups produce max hit, hit chance and
theoretical DPS on ordinary Dharok or General Graardor. The model includes charged
tridents, Sang's current damage proc, Eye and Shadow; it respects fractional ring
bonuses, prayer/boost assumptions and off-hand removal. Supported marginal changes
influence deterministic advice. Native cards lead to **Value | Path** screens.
The earlier observation, privacy, preferences and broad PvM foundation remains.

This is partial coverage of the comprehensive product. ToA scaling, one-handed
Confliction history, Brimstone accuracy procs, spell specialists and encounter/trip
metrics remain visible gaps. See [R1 validation](docs/validation/R1.md) and
[synthetic account demonstrations](docs/validation/r1-scenarios.md). No live-client
or player-usefulness pass is claimed. No Plugin Hub submission or runtime service.

The intended product is a free, open-source RuneLite sidebar that automatically identifies worthwhile Ironman PvM upgrades using observed account state: **which upgrades are worth pursuing, what difference will they make, and how do I obtain them?** Equipment, relevant prayers, skill levels and permanent unlocks are eligible. Evaluation runs locally, without AI, API keys, registration or an owner-operated backend.

## Product direction

IronPath is intended to become a comprehensive endgame PvM upgrade advisor, including BiS, meaningful specialist sidegrades, permanent rewards and the progression bridges needed to reach them. **R1 is an implementation increment, not the product boundary.** There is no catalogue ceiling or requirement to select a goal.

R1 connects full supported powered-magic references to marginal-value ranking and delivers initial endgame outcomes. The next implementation is **R1.5a: pursuit priority and visual foundation**; R2–R8 then extend combat families, coverage, maintenance and real-client evidence. Ranged model work can overlap this increment. Useful numerical comparison is a core capability: max hit, accuracy and theoretical DPS where supported, with explicit context and honest exclusions. Kill-time and trip claims need additional models; they cannot be inferred from a DPS percentage.

Recommendations must explain what changes relative to owned alternatives, where it matters, when to defer, and how to obtain the upgrade. Strong alternatives and partial observations change the advice. An unread bank never proves an item is missing. Long-term upgrades remain visible alongside nearer opportunities.

The architecture stays one local Java RuneLite plugin with bundled reviewed knowledge and native Swing UI. The main investment is accurate knowledge, reusable combat mechanics and useful presentation. We are not adding a backend, subscriptions or per-user AI. See the independent feasibility assessment and trade-offs in the design.

**Proposed refinement, not yet implemented:** distinguish upgrades worth owning from active pursuits and valuable shared activities. R1.5a adds transparent effort/long-tail categories, a first conditional next-zenyte-shard estimate, compact pursuit rows and a responsive item grid. Hours require an explicit pace and scope; gear/DPS does not predict raid times. Local performance observation and complex raid/component models follow later. See [revised wireframes](docs/UX.md) and [next assignment](TASKS.md#next-implementation). Comprehensive coverage, optional goals and all existing R1 work remain intact.

## Read and work

| File | Purpose |
|---|---|
| [docs/DESIGN.md](docs/DESIGN.md) | A: stable product vision; B: required capabilities and architecture; C: delivery principles |
| [docs/CATALOGUE.md](docs/CATALOGUE.md) | Comprehensive initial coverage inventory, promotion criteria and visible research/model gaps |
| [docs/UX.md](docs/UX.md) | Native RuneLite design and concrete sidebar wireframes |
| [docs/RESEARCH.md](docs/RESEARCH.md) | Current evidence, source limits, calculator reuse/licensing and historical research |
| [TASKS.md](TASKS.md) | Active R1, R1.5 and R2–R8 assignments, quality criteria, next brief and preserved implementation/test history |
| [AGENTS.md](AGENTS.md) | Persistent development instructions that protect the product vision |

TASKS.md is the only backlog. The catalogue is an evidence/coverage inventory, not a second task tracker. Implementation increments can be small; the intended product must not silently shrink to match them.

## Repository

Approved repository name and visibility: **`PetterHL/ironpath`**, public, confirmed by Petter on 12 October 2026. Created on GitHub on 12 October 2026. Use BSD-2-Clause for original work. The included licence does not relicense external guide content, images or dependencies.

Naming note: Iron Compass already uses `ironpath` as its Plugin Hub manifest identifier. This is an independent project; keep the chosen product/repository name, avoid any implied affiliation, and choose a distinct Hub manifest identifier before submission.

Reuse the existing T1–P3 RuneLite implementation and preserve its licence notices. Use its build/login instructions and verify supported APIs when extending it. Do not scaffold the plugin again or request approval again for the already approved repository name and public visibility.

## Start here

The latest inspected implementation is PR #7, `p4a-upgrade-advisor`, at `b50b0a9`, stacked on PR #6 (`docs/upgrade-first-roadmap`, `2ef409a`) and PR #5 (`p1-p3-broad-pvm`, `eb22fef`). PR #5 consolidates earlier T1–T4 work. PRs #1–#7 remain open and unmerged; main is `c717927`. The complete relationship is recorded in the design. Recheck remote state before future work.

This product-reset branch contains **documentation and wireframes only**, targeting #7's branch to keep review focused. It changes neither plugin behaviour nor the recorded test results. The next implementation assignment is **R1: realistic full-loadout magic and first endgame value**, described in TASKS.md. Old P4b counts and sequential planning/review gates are superseded. No existing PR is merged, closed or overwritten.

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
git checkout r1-full-loadout-magic
```

For an existing clone, close the development client, enter its `ironpath` directory,
and update the branch first:

```sh
git fetch origin
git switch r1-full-loadout-magic
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

## Use the R1 build

1. Open with **no goal** and observe the bank. The three primary cards balance
   supported value with varied outcomes; **More supported upgrades** keeps every
   relevant reviewed outcome inspectable. Ownership uncertainty is conditional
   advice, never a claim that unread storage is empty.
2. Open a card. **Value** shows before/after gear, max hit, hit chance and
   theoretical DPS where individually supported. **Path** shows actual access,
   preparation, components, ALL/ANY assembly branches and equip conditions.
   **Back to upgrades** restores the overview scroll position.
3. The complete reference prefers supported observed owned pieces and charged
   staff packages. A reviewed mage body at 85+ Magic selects ordinary Graardor as
   an explicit high-defence reference; smaller templates start at Dharok.
   These are reference profiles, not inferred encounter interests or readiness.
   No prayer/boost and Accurate are default assumptions. Shadow removes the
   off-hand. Unknown/unreviewed equipped slots with no supported alternative
   block numbers until a focused correction or explicit template exclusion.
4. **Reference setup and assumptions** discloses every slot, provenance, real and
   effective levels, source/model versions and exclusions. Correct a slot,
   prayer, boost, style or target, then **Confirm reference**. Empty slots are
   deliberate reference exclusions; uncharged/manual weapons cannot become usable.
   Confirmation does not establish measured performance. Selecting ToA shows its
   missing phase/scaling inputs instead of inventing an invocation.
5. Numbers cover uninterrupted ordinary attacks. Sang's 20% successful-hit
   +8-damage proc is included; healing is separate. Confliction's one-handed
   history and Brimstone accuracy procs withhold accuracy/DPS while retaining
   supported max hit. Ancient/elemental spells, defence reduction, movement,
   specials, kill time and trips are outside this comparison.
6. Path quantities are timestamped, container-scoped planning evidence. The
   largest readable container count is a conservative lower bound; a stale bank
   stack is never added to its later inventory copy. Unread/insufficient counts
   remain unknown. Owned intermediate components bypass their upstream branch;
   boosts, preparation and current supplies still need checking.
7. **Already owned**, **Confirm not owned** and **Clear confirmation** change only
   the named planning fact. Positive possession wins conflicts; readable unlocks
   override manual fallbacks. **Account and preferences** retains optional goals,
   skips and local saves. A skip excludes acquisition without completing it.
   Levels/items/slots/quantities/manual changes invalidate reference confirmation;
   identity/lifecycle changes clear it. Wait for **Preferences saved locally**
   before restarting. All real-client checks remain pending.

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
Perilous Moons, The Frozen Door, The Final Dawn, Monkey Madness II and Desert
Treasure II are read on the client thread
into immutable snapshots.
Unavailable quest scripts, containers or skills stay unknown. Quests use supported
`Quest.getState`; selected progress events coalesce into a read, while idle ticks
retain the existing snapshot. Equipment slots and supported positive container quantities retain provenance.
No KC, Collection Log, unobserved quantities or player execution ability is inferred.

The catalogue recognises selected exact active/charged, empty and cosmetic forms
for distinct purposes. Bowfa usable-package checks still cover default-colour active
crystal armour. Atlatl acquisition is supported, but its full strength/ammunition
setup is outside the CoX baseline. Only the base keris partisan is recognised for
ToA preparation. The magic reference is a complete compatible proposal from reviewed observed gear,
with explicitly labelled empty exclusions, prayer/boost assumptions and model gaps.
It is not asserted to be the current worn loadout.
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
