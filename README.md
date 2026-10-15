# IronPath

**Understand your setup. Find upgrades that matter where you play.**

IronPath is a free, local RuneLite plugin for account-aware PvM improvement.
The selected direction is an encounter-and-method workflow: understand the setup
you actually use, find better uses of owned equipment, then compare worthwhile
new upgrades and their effect on your play.

**15 October 2026: transition development build 0.14.1. Nex is the selected next
pilot, not an implemented feature.** The existing general advisor and optional
Zilyana experiment still run while the new flow is built. No Nex contribution,
session average, special-attack comparison or seconds-saved claim ships here.

The pilot is **Nex / Trio MP2 / one kill per trip**, with Lightbearer throughout
and upgraded Death Charge. Equipment, switches and spell use should be observed
automatically, with focused corrections and saved method choices. Complete
combat summaries will be session-only and must survive ordinary banking.

## Project map

| Document | Purpose |
|---|---|
| [TASKS](TASKS.md) | The only active backlog and next delivery |
| [Design](docs/DESIGN.md) | Product decision, code boundaries and reuse/removal map |
| [UX](docs/UX.md) | Intended Nex flow and current UI boundary |
| [Kill impact](docs/KILL_IMPACT.md) | Observation, session and numerical contracts |
| [Coverage](docs/CATALOGUE.md) | Existing support and specific Nex gaps |
| [Research](docs/RESEARCH.md) | Evidence, source limitations and licensing |
| [Development](docs/DEVELOPMENT.md) | Build, run and verification instructions |
| [Transition report](docs/validation/nex-transition.md) | Actual changes and verification |
| [Historical archive](docs/archive/pre-nex-20261015/README.md) | Previous plans, original results and pending checks |

The previous product direction is superseded. Its mathematics, sources, account
protections and useful acquisition knowledge remain reusable. Historical roadmap
items are not automatic prerequisites for the Nex pilot.

## Build

Use JDK 11 and the included Gradle wrapper. In Windows PowerShell:

```powershell
.\gradlew.bat test build
.\gradlew.bat run
```

On macOS/Linux use `./gradlew` instead. See [development](docs/DEVELOPMENT.md) for
updating an existing checkout and Jagex Account login. No backend, runtime AI,
API key or mandatory third-party plugin is required.

Repository: [PetterHL/ironpath](https://github.com/PetterHL/ironpath).
Original code uses [BSD-2-Clause](LICENSE); preserve [third-party notices](THIRD_PARTY_NOTICES.md).
The plugin has not been published to Plugin Hub. Its Hub identifier must be
distinct from the existing Iron Compass identifier before submission.
