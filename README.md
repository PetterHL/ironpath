# IronPath

IronPath is a local RuneLite plugin for comparing equipment in the encounters
you play. The current pilot is **Nex / Trio MP2 / one kill per trip**, with
Lightbearer throughout and assumed upgraded Death Charge. Gear, switches,
levels and active prayers are observed automatically; no setup form is required.

**Development build 0.32.2:** the main comparison is the provisional **Fang
equipment** preview. The older theoretical DPS cards have been removed from the
homepage. **Base-stat comparisons** opens the retained per-state gear comparisons
and optional declared spec/resource planner. Session counts are in Diagnostics.
During capture, **Tracking kill…** shows advancing recorded time and phase.
An unobserved smoke start stays explicitly unconfirmed.
[Cleanup and verification](docs/validation/nex-sidebar-cleanup.md).

After a completed kill, Compare setups shows equipment alternatives for the
same supported ordinary Fang action sample, at the same weapon, style and tempo.
It keeps carried gear, conditional bank preparation and hypothetical upgrades
separate. Expected-damage bounds retain zero and negative alternatives. Food
signals include manta ray; exact Death Charge messages are captured from the
supported game-message channels.

The preview remains **provisional**: action mappings, supported Nex windows and
base target stats are not fully validated. There is no whole-kill extrapolation,
automatic spec recommendation or team-time estimate. Damage contribution remains
**Unverified**. The optional planner uses explicitly declared inputs and does not
turn observations into confirmed attack, supply or Death Charge plans.
[Preview contract](docs/validation/nex-fang-advice.md) ·
[Latest live findings and corrections](docs/validation/nex-fang-action-followup.md).

Last kill stays above the collapsible **Compare setups**, **Team & method** and
**Diagnostics** sections. Optional context corrections preserve frozen gear and
calculation inputs. Diagnostics contains the build version, exclusions, captured
evidence and **Copy report now**, with selectable text if the clipboard fails.
Reports bind to the selected kill; open that kill's details before copying when
checking a particular result.

Completed history stays in memory across recognized bank trips and remains hidden
until the same account/profile/world is verified. Logout, identity/world changes,
disable and Clear session discard it. Only explicit preferences are saved; there
is no telemetry, backend or automatic upload. Existing saved planning locks and
manual facts retain their precedence without replacing observed history.

[TASKS](TASKS.md) owns the active roadmap and remaining live acceptance.
[Validation records](docs/validation/README.md) retain earlier deliveries,
source reviews and their limits. Plugin Hub publication remains separate.

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

For calculation/UI review without gameplay, see
[the DEV1 offline command](docs/DEVELOPMENT.md#offline-review-dev1). It runs fourteen
synthetic scenarios through the production Nex adapter/session/evaluator and native
Swing panel, including controlled plugin publication, banking and frozen-history
checks, and generates reports plus native-width PNGs at normal/enlarged fonts.

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

## Preview artwork

Created using intellectual property belonging to Jagex Limited under the terms of Jagex's Fan Content Policy. This content is not endorsed by or affiliated with Jagex.

[Genuine preview sprites: provenance and separate terms](THIRD_PARTY_NOTICES.md).
