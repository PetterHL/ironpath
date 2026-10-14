# IronPath

**Your path to endgame PvM**

Integrated delivery · 14 October 2026 · plugin **0.14.0** · knowledge **ad1-r3b-1** · catalogue schema **14** · preference schema **2**

Test **main** after updating your clone. This build integrates plan #27, EP1 #25 → UX2 #26 → AD1a #28, automatic acquisition #29, AD1c #30 and published R3b #31. It preserves the earlier observation, preferences, ranged/magic, ordinary-melee and opt-in KI1 deliveries. [Combined validation, integration decisions and PC checklist](docs/validation/ad1-r3b-integration.md). The original per-delivery reports retain their original versions, account fixtures, source provenance and pending client checks.

IronPath is a free, open-source RuneLite sidebar that identifies worthwhile Ironman PvM progress from observed account state, explains its benefit and remaining acquisition work, and runs locally without a backend, AI, API keys or registration. Comprehensive maintained endgame/BiS and specialist coverage remains the destination; current coverage is partial and uncapped. No goal or manual pace is required.

**Recommended order and navigation:** progression-2 combines account-specific value, compatible owned alternatives, method suitability and remaining effort across all 54 active outcomes and 55 routes. The homepage preserves that order. Whole cards open details; use **Upgrades / Account**, **Recommended / Browse**, and **Impact / Acquisition** inside an item. Account groups unresolved ownership checks and focused reversible corrections; Diagnostics remains separate. Browse/search/filters, Back, focus/scroll and stale account/image guards are retained.

**DPS-first comparisons:** ordinary offensive weapons, armour and accessories lead with supported relative theoretical DPS. Impact retains absolute before/after/delta, max hit, hit chance, every changed slot, loading, assumptions and named per-metric gaps. A zero baseline shows absolute change; a real loss remains visible. Multi-slot changes are labelled **Setup change**. Brimstone accuracy is supported; Confliction history and other unresolved mechanics retain explicit gaps. The shared 0 Defence analytical premise appears in calculation basis; it is not boss throughput. [AD1a evidence](docs/validation/AD1a.md).

**Automatic component estimates in Acquisition:** five reviewed repeat-hour scenarios cover eligible CG, Zulrah, demonic-gorilla, tormented-demon and tbow-Muspah methods. Nine of 54 outcomes can show automatic component hours on applicable accounts; ten have component probability models. Mean, median/P90 and manual pace sensitivity remain distinct. Owned components and conservative observed quantities remove only supported completed work; full-item totals, observed speed, learning and supply acquisition remain unavailable. Matching manual pace is optional and session-only; **Clear pace** restores the compatible automatic default or unavailable hours. Source/method evidence and controls are under **Estimate basis and route evidence**. [Current complete route/method coverage](docs/validation/integrated-acquisition-coverage.md); [original AD1b provenance](docs/validation/AD1b.md).

**R3b melee:** Fang, size-aware Scythe, explicit Soulreaper stacks/ramp, task/Salve, dragon/demon and current Inquisitor mechanics compare complete compatible owned packages. Endgame armour/accessories, component-aware paths, missing primordial treads additions and independent blood-fury sustain are included. Blood fury can retain utility despite a negative ordinary DPS result. Soulreaper ramp is explicitly a **five-swing mean**, not full-stack sustained DPS or encounter timing. ToA/Olm examples are fixed-input scenarios; automatic raid scaling, specials, healing amounts, kill-time and trips remain gaps. [R3b sources and validation](docs/validation/R3b-families.md).

**Retained client corrections and KI1:** crystal cosmetics, Blazing/Toxic, all ranged treads forms and Lightbearer remain recognised. Rosewood acquisition advice remains withdrawn while supported owned-item numbers survive. The optional **Recent boss (experimental)** normal-Zilyana pilot captures session-only worn-state/offence context; personal seconds, broader bosses/raids and actual-client acceptance remain pending. [Earlier combined UX/melee/KI1 evidence](docs/validation/ux-melee-ki1-integration.md).

Real RuneLite sprites, scaling, actual reported magic/ranged setups and player usefulness remain pending. Automated and synthetic Swing evidence does not close joint AD1 acceptance. TASKS AD1b-B1–B4, AD1c-C1–C3 and R8 own the remaining checks; no new feature area starts with this integration.

## Read and work

| File | Purpose |
|---|---|
| [docs/DESIGN.md](docs/DESIGN.md) | A: stable product vision; B: required capabilities and architecture; C: delivery principles |
| [docs/CATALOGUE.md](docs/CATALOGUE.md) | Comprehensive initial coverage inventory, promotion criteria and visible research/model gaps |
| [docs/UX.md](docs/UX.md) | Native RuneLite design and concrete sidebar wireframes |
| [docs/RESEARCH.md](docs/RESEARCH.md) | Current evidence, source limits, calculator reuse/licensing and historical research |
| [TASKS.md](TASKS.md) | Single active backlog, R3–R6 intelligence work, account acceptance and preserved implementation/test history |
| [docs/PROGRESSION_RESEARCH.md](docs/PROGRESSION_RESEARCH.md) | R2 architecture assessment, 30 relationships, decisions, qualified sources and evidence gaps |
| [AGENTS.md](AGENTS.md) | Persistent development instructions that protect the product vision |

TASKS.md is the only backlog. The catalogue is an evidence/coverage inventory, not a second task tracker. Implementation increments can be small; the intended product must not silently shrink to match them.

## Repository

Approved repository name and visibility: **`PetterHL/ironpath`**, public, confirmed by Petter on 12 October 2026. Created on GitHub on 12 October 2026. Use BSD-2-Clause for original work. The included licence does not relicense external guide content, images or dependencies.

Naming note: Iron Compass already uses `ironpath` as its Plugin Hub manifest identifier. This is an independent project; keep the chosen product/repository name, avoid any implied affiliation, and choose a distinct Hub manifest identifier before submission.

Reuse the existing T1–P3 RuneLite implementation and preserve its licence notices. Use its build/login instructions and verify supported APIs when extending it. Do not scaffold the plugin again or request approval again for the already approved repository name and public visibility.

## Start here

Main is the shared development baseline, including EP1, UX2, AD1a, AD1b, AD1c and R3b families. [Integration/cleanup record](docs/validation/main-integration-20261014.md) and [R3a validation](docs/validation/R3a.md) separate combined automated checks from pending client acceptance.

To test the integrated build, close the development client first:

```sh
git fetch origin
git switch main
```

Historical planning branches and validation reports are records, not fresh prerequisites.

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
then activate a card with its title/chevron, mouse or keyboard. **Browse** retains all relevant reviewed outcomes. Open **Impact / Acquisition** inside an item and use **← Upgrades** to return. Pinned source links are under
**Reviewed sources**. Advice is planning, not a complete loadout or a promise of
successful kills. Leave account cases you cannot exercise pending.

## Test the integrated flow

1. Open IronPath without a goal or manual pace; open your bank and check the recommended choices and their actual baselines.
2. Inspect **Impact** for Magus versus owned Brimstone, ranged setup changes, and supported melee accessories. Check positive/zero/loss results, changed hands/ammo and named model gaps.
3. Open **Acquisition** for an eligible automatic method. Check component scope, remaining parts, mean and estimate basis; enter matching manual pace and **Clear pace** to restore the default. Other methods must retain honest unavailable hours.
4. Exercise Fang/Scythe/Soulreaper contexts, task/Salve and soul ramp; inspect partial melee components and blood-fury utility without expecting personal time or trip claims.
5. Use Account corrections, Browse filters/search and Back at normal/enlarged fonts. Switch profile/account and confirm that old references, manual pace, recent boss context and delayed actions clear.

Test on a standard Ironman in an ordinary permanent members world. [Full PC checklist and remaining limitations](docs/validation/ad1-r3b-integration.md). Do not commit private account/bank dumps or login credentials. No Plugin Hub publication is claimed.
