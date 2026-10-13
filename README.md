# Ironpath

**Your path to endgame PvM**

Neutral-reference development build · 13 October 2026 · plugin 0.6.2 · knowledge reference-1 · schema 8

**Main includes R1.5a and the neutral-reference/DPS-first follow-up.** Effort-aware pursuit categories, deduplicated shared activities, compact native rows and responsive Browse retain all R1 comparisons/routes. The next-zenyte-shard estimate is conditional: mean/median/P90 in future eligible kills, optional session-only pace, and no completed-bracelet ETA. See [R1.5a validation and pending client checks](docs/validation/R1.5a.md).

**Delivered: realistic powered-magic references and first endgame value.** Shadow,
Ancestral hat/top/bottom, tormented bracelet and Magus ring join occult, swamp,
Zaryte and treads. Complete supported slot setups produce max hit, hit chance and
theoretical DPS on **Enemy with 0 Defence** by default, with ordinary Dharok and General Graardor available as explicit contexts. The model includes charged
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

**Client comparison-gap follow-up:** Value now names the unsupported accuracy item and whether it affects before, after or both reference setups. Brimstone's proc and Confliction's one-handed stacks remain separate model gaps; supported max hit stays visible. See [validation](docs/validation/client-model-gap-labels.md). This explanation patch is on `client-model-gap-labels`; main remains the integrated 0.6.1 baseline until review/integration.

## Product direction

IronPath is intended to become a comprehensive endgame PvM upgrade advisor, including BiS, meaningful specialist sidegrades, permanent rewards and the progression bridges needed to reach them. **R1 is an implementation increment, not the product boundary.** There is no catalogue ceiling or requirement to select a goal.

R1 connects full supported powered-magic references to marginal-value ranking and delivers initial endgame outcomes. **R1.5a: pursuit priority and visual foundation** is integrated; R1.5b and R2–R8 extend effort models, combat families, coverage, maintenance and real-client evidence. Ranged model work can overlap this increment. Useful numerical comparison is a core capability: max hit, accuracy and theoretical DPS where supported, with explicit context and honest exclusions. Kill-time and trip claims need additional models; they cannot be inferred from a DPS percentage.

The comparison follow-up is implemented on this branch: **Enemy with 0 Defence** is the automatic powered-magic reference. Weapons show theoretical DPS before → after and its absolute delta first; unchanged-speed armour/jewellery keep max hit first. The authored target has Defence 0, Magic 0 and magic-defence bonus 0; ordinary rolls give defence roll 576 and do not guarantee accuracy or maximum damage. Details show hit chance, attack interval and per-metric gaps. See [validation and pending client checks](docs/validation/neutral-reference.md).

Recommendations must explain what changes relative to owned alternatives, where it matters, when to defer, and how to obtain the upgrade. Strong alternatives and partial observations change the advice. An unread bank never proves an item is missing. Long-term upgrades remain visible alongside nearer opportunities.

The architecture stays one local Java RuneLite plugin with bundled reviewed knowledge and native Swing UI. The main investment is accurate knowledge, reusable combat mechanics and useful presentation. We are not adding a backend, subscriptions or per-user AI. See the independent feasibility assessment and trade-offs in the design.

**Pursuit priority:** upgrades worth owning, active pursuits and shared activities are separate. Readiness and supported remaining burden precede strength within an immediate-pursuit group. Shadow/Ancestral retain long-tail value; unknown ownership/preparation stays conditional. Owned components remove their own drop stages. Shared CoX benefits count Ancestral as one family and count prayer rewards only with confirmed missing unlock/scroll facts. No drop probabilities or DPS gains are added across rewards. See [implementation evidence](docs/validation/R1.5a.md) and [ranking contract](docs/DESIGN.md#deterministic-selection-procedure-version-pursuit-1-proposed).

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

The current development baseline is `main` at `9035a06`: PR #12 integrated R1.5a and the merged neutral-reference/DPS-first PR #13 on 13 October 2026. Plugin 0.6.1, knowledge reference-1 and catalogue schema 8 are now in main. Prior R1 integration and all earlier history remain intact. Actual-client checks are still pending in the original validation reports.

Close the development client and use the main-branch update/build instructions below to test the integrated build. R2 starts in draft PR #14 on `r2-ranged-models`; its first ranged-family models are tested but do not yet generate new personal recommendations. Full ranged references, routes, item/charge/ammo checks and UI integration are next in TASKS. Historical reports retain their original branch/test context.

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
git checkout main
```

For an existing clone, close the development client, enter its `ironpath` directory,
and update the branch first:

```sh
git fetch origin
git switch main
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
then activate a card with its title/chevron, mouse or keyboard. **Browse** retains all relevant reviewed outcomes. Pinned source links are under
**Reviewed sources**. Advice is planning, not a complete loadout or a promise of
successful kills. Leave account cases you cannot exercise pending.

## Use the R1.5a build

1. Open with **no goal** and observe the bank. The overview offers up to two
   **Worth pursuing** rows, one justified **Worth doing** activity, a long-term
   preview and a consequential check. Empty groups stay empty. **Browse** has
   All / Pursue / Long-term / Check and retains specialist/deferred choices.
   Two columns fall back to one when width, names or fonts need more space.
2. Activate a card by mouse, Enter or Space. **Value** shows supported metrics
   once, with priority reasons and a relevant alternative. **Path** leads with
   actual remaining work, separate equip checks and expandable ALL/ANY recipe
   branches. **Back to upgrades** retains Browse category, selection and scroll.
   Shared activity detail names useful families, exclusions and uncertain rewards.
3. The complete reference prefers supported observed owned pieces and the strongest
   supported charged staff/off-hand package for **Enemy with 0 Defence**.
   Boss profiles remain explicit alternatives in the target selector. Neutral DPS
   can favour a faster staff even when Shadow has a larger max hit; specialist
   usefulness and encounter readiness require their own context.
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
7. **Already own this? / Correct ownership** expands focused **Already owned**, **Confirm not owned** and **Clear confirmation** change only
   the named planning fact. Positive possession wins conflicts; readable unlocks
   override manual fallbacks. **Account and preferences** retains optional goals,
   skips and local saves. A skip excludes acquisition without completing it.
   Levels/items/slots/quantities/manual changes invalidate reference confirmation;
   identity/lifecycle changes clear it. Wait for **Preferences saved locally**
   before restarting. All real-client checks remain pending.

The tormented Path estimates the **next zenyte shard only** when that stage
remains. Default: mean 300, median 208, 90% by 690 eligible demonic gorilla kills;
10% take longer. **Set pace (optional)** accepts a positive kills/hour number or
range such as `30-50`, with explicit cycle overhead. Hours are conditional on that
scenario, which remains in memory and clears on lifecycle/account/profile changes.
**Clear pace** resets it. An owned shard/gem/bracelet removes the drop estimate.
Quest/access, onyx, skills, assembly and supplies are excluded; no full-item time,
RNG progress or dry-streak adjustment is implied. Raids/Nex/DT2/challenges retain
reviewed qualitative routes and explicit time-model gaps.

The retained activity evaluator still needs targeted absence confirmations for
missing-item grinds. This secondary legacy list retains its qualitative activity ordering; the primary R1.5a item assessments use `pursuit-1` categories and effort before strength. Neither counts goal aliases. A goal retains its
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
