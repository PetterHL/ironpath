# IronPath

**Understand your setup. Find upgrades that matter where you play.**

IronPath is a free, local RuneLite plugin for account-aware PvM improvement.
The selected direction is an encounter-and-method workflow: understand the setup
you actually use, find better uses of owned equipment, then compare worthwhile
new upgrades and their effect on your play.

**16 October 2026: combined development build 0.17.1 (N1/N2/N3a).**
N3a adds **Owned setup comparisons → Special-attack resources**, with frozen
energy, Death Charge and ring readings after a kill. Its separate finite energy/
delay scenario API requires explicit inputs and does not infer spec usage or
recommend a spec upgrade. [Scope and verification](docs/validation/nex-spec-resources.md).

N2's exact locked melee cape/defender
forms now have reviewed support. Owned comparisons open with supported scenarios,
role/support filters and grouped explanations for unavailable states. Counts
distinguish carried and bank preparation scenarios; they are not fight coverage.
[Variant support and checks](docs/validation/nex-owned-variants.md).

Nex capture measures from the
observed smoke announcement to death/dying, with corroborating arena completion.
Arena entry is diagnostic; pre-smoke equipment remains separately inspectable.
The bounded end matching accounts for dying preceding arena completion. The
reproduced failure pattern passes synthetic replay. Complete-result display and
retention across a bank visit now have in-session evidence; broader session and
player checks remain open. [Capture contract](docs/validation/nex-smoke-anchor.md).
The development launcher retains RuneLite's required assertions with a temporary
GPU-package exception, correcting the 0.16.3 startup failure; see
[development](docs/DEVELOPMENT.md#gpu-assertion-during-development).

N2 compares complete owned packages against each distinct actually captured
state. It keeps melee/ranged/spec evidence separate, preserves planning locks,
distinguishes carried alternatives from bank preparation and exposes gains,
losses and zero. Nex ordinary offence uses exact sampled levels/prayers and a
labeled base-stat scenario; it does not estimate kill time or personal contribution.
[N2 evidence and remaining checks](docs/validation/nex-owned.md).

N1 session capture retains last complete timing/share, comparable means/ranges,
excluded attempts and immutable timelines. Its real-client mappings and player
acceptance are still pending. [N1 validation](docs/validation/nex-session.md).

The pilot is **Nex / Trio MP2 / one kill per trip**, with Lightbearer throughout
and assumed upgraded Death Charge. Equipment, switches and active spell state are
observed automatically. Setup exposes optional saved planning locks/corrections;
they never overwrite the actually-worn history. Complete summaries stay in memory
across recognized same-account bank trips and clear on logout/profile change.
Observed hitsplat share is scoped to Nex and the four named minions; it is not
server contribution or loot eligibility. Timing uses the observed smoke
announcement and death/dying signal, with no game-timer calibration claim.

The previous general homepage and global progression ranking are removed. The
reported Tbow/amethyst-quiver and Fang/defender equipment has an anonymous regression
fixture; levels, prayers, stock and phase inputs in that fixture are synthetic.
Ancient blessing remains a physical worn item while quiver arrows are a separate
projectile input. Conditional ordinary charged/blessed quiver offence is supported;
finite ammo/splinter use, special-attack damage, healing and contribution/time remain
N3–N4. Scorching and other unsupported alternatives leave explicit coverage gaps
without blocking a supported pair.

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
