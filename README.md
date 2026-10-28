# IronPath

IronPath is a local RuneLite plugin for comparing equipment in the encounters
you play. The current pilot is **Nex / Trio MP2 / one kill per trip**, with
Lightbearer throughout and assumed upgraded Death Charge. Gear, switches,
levels and active prayers are observed automatically; no setup form is required.

**Development build 0.33.3:** **Diagnostics → Copy report now** includes a current
overview of every retained capture interval, its start source and exclusion reason,
bank/pause readings and exact build provenance. The selected calculation stays
frozen in its own section. No record switching or Calculation details is required.
[Troubleshooting procedure](docs/TROUBLESHOOTING.md) ·
[Implementation and synthetic validation](docs/validation/nex-support-report.md).

Spawning Nex no longer starts a partial attempt from
an arena reading alone. [Spawning/respawn correction](docs/validation/nex-spawning-attempts.md).
Compare setups uses **Melee**, **Ranged** and
**Special attacks**, with a compact equipment list and expandable comparisons.
Ordinary damage includes accuracy and the complete damage distribution without
clipping against remaining target HP. Positive, zero and negative alternatives
share the observed baseline. **Add comparison item** extends the Nex strategy-table
suggestions. Ironman ordering combines supported damage benefit with remaining
acquisition work; no GE prices or acquisition-hour score is shown.

The current automatic numerical slice remains ordinary fixed-weapon/style/tempo
melee samples. Ranged and special attack attribution remain unavailable. Open
**How this was compared** for sample scope, provisional mapping status, acquisition
checks and missing inputs. Whole-encounter damage weighting and verified contribution
are unavailable in current captures, so dependent contribution/time values read
**Not estimated**. The constant-throughput estimate model is implemented for supported
inputs; supplies also remain **Not estimated** without a consumption model.
Damage contribution above the list remains **Unverified**.
[Scope, sources and validation](docs/validation/encounter-equipment-advice.md).

**Base-stat comparisons** retains the per-state comparisons and optional declared
spec/resource planner. During capture, **Tracking kill…** advances recorded time
and phase; an unobserved smoke start stays unconfirmed. All finite-plan resource
guards remain intact. [Earlier matched live check](docs/validation/nex-fang-live-0322.md)
validates status/time for one kill, not current advice coverage or DC mappings.

Last kill stays above the collapsible **Compare setups**, **Team & method** and
**Diagnostics** sections. Optional context corrections preserve frozen gear and
calculation inputs. Diagnostics contains the build version, exclusions, captured
evidence and **Copy report now**, with selectable text if the clipboard fails.
One copy includes the bounded session overview regardless of the selected kill.
Copy while evidence is visible; the copied text is self-contained after logout.
A paused report includes runtime readings while session evidence stays hidden.

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
