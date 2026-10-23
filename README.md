# IronPath

**23 October 2026: development build 0.30.0 — N3 evidence validation.**
The declared fixed-ring model now pauses regeneration at full special energy.
Repeat projectile getter counters expose later availability without changing
first-flight UNKNOWN attribution; evidence export is `nex-evidence-3`.
**Automatic recommendations remain unavailable.**
[Readiness, source evidence and smallest remaining observations](docs/validation/n3-evidence-validation.md).

**Understand your setup. Find upgrades that matter where you play.**

IronPath is a free, local RuneLite plugin for account-aware PvM improvement.
The selected direction is an encounter-and-method workflow: understand the setup
you actually use, find better uses of owned equipment, then compare worthwhile
new upgrades and their effect on your play.

**23 October 2026: development build 0.29.0 — Nex callback coverage follow-up.**
One supplied live kill now confirms repeated hitsplat-object use, retains all 171
own impacts and matches damage minus healing to the HP of all five targets.
[Single-kill findings and remaining gates](docs/validation/nex-damage-observation.md#live-one-copy-check--23-october-2026).
Bank/next-kill acceptance is deferred; projectile sources and resource semantics remain open.

Damage contribution now reads **Unverified** while whole-kill coverage is open.
Diagnostics preserves the scoped recorded damage sums/ratio. Direct hitsplat
callback processing no longer rejects events by payload-object identity; bounded
counts/sums expose entry, rejection, classification and repeated-object evidence.
Projectile sources are explicit LOCAL / OTHER / UNKNOWN. One-copy evidence export
is now `nex-evidence-2`, with all retained useful events and full fixed counters.
The earlier live report's missing-damage cause remains unresolved; synthetic
object reuse is not a replay of the live kill; the pinned client binary confirms
a mutable reuse path. [Source comparison](docs/research/nex-damage-observation.md)
· [Verification and next live check](docs/validation/nex-damage-observation.md).

**20 October 2026: client-test follow-up in development build 0.28.2.**
Copy report now includes all retained target/HP-source summaries, candidate
animation groups, the running RuneLite version and fixed HUD callback/rejection
counts. One copy supplies the test overview; selecting event windows is optional.
Nex-specific integer HUD availability remains unconfirmed. Ordinary overhead
health bars remain bounded actor-bar readings.

**20 October 2026: review corrections in development build 0.28.1.**
HP-only diagnostics no longer fill the main state buffer. Recorded-tick imports
clear saved impact/supply confirmations, and launch matching preserves the selected
Zaros overhead. Diagnostics and copied reports share a selectable 12-event window
with relative time/order, target forms and candidate links. Client acceptance
remains open in the [validation record](docs/validation/nex-observation.md).

**20 October 2026: development build 0.28.0 — bounded Nex observation for review.**
The PR #50 follow-up records fresh boss-HUD HP candidates or bounded health-bar
intervals, pilot attack/launch-package candidates, distinct projectiles, own zero
hits, settled state and Death Charge/resource callbacks. Smoke-callback quantities
stay separate from consumption. Diagnostics and the existing user-triggered report
show the frozen evidence; recorded attack ticks can seed an explicit N3 draft
without confirming its assumptions. Conditional state comparisons retain their
independent phase/target support. HUD/weapon/DC mappings and full-energy timing
still need [these exact client checks](docs/validation/nex-observation.md).
[Contract, sources and remaining gates](docs/research/nex-observation.md).

**20 October 2026: development build 0.27.0 — N3 decision support for review.**
**Plan specs & gear** now supports explicit food/potion schedules with finite
carried stock, shared attack delays and brew/restore/reboost effects. Vulnerable
Fumus/Umbra/Cruor/Glacies windows use separate target stats and flat armour.
Zaros plans require declared overheads and target levels; captured NPC forms
and health-bar readings stay separate. Optional **Use per-attack conditions**
supplies independent HP, caps, ruby eligibility and Zaros conditions at each
attack tick. Missing rows stay unknown; earlier/fixed values are never copied
into them. [Impact conditions and verification](docs/validation/nex-impact-conditions.md).
Death Charge checks finite loose/pouch
Death/Blood/Soul runes and Magic after declared supplies; rejected cooldown casts
consume no runes. **Unit costs** prices ammo, splinters, supplies and admitted runes;
**Acquisition routes** reuses reviewed catalogue paths without inventing remaining
account work or acquisition hours. Declared Deflect Melee impacts support
Voidwaker's Magic-type special; ordinary melee and an admitted ordinary fallback
retain their damage gaps. Unknown windows/stats still withhold dependent damage.
[Attack-type verification](docs/validation/nex-deflect-attack-type.md) · [Contract](docs/research/nex-decision-support.md)
· [Verification](docs/validation/nex-decision-support.md).
Automatic attack/spec/consumption attribution and live acceptance remain open.

**19 October 2026: consolidated main build 0.23.3 — current Nex UI and sidebar simplification.**
The native Encounter panel uses concise OSRS labels, centred results and genuine
item/package sprites. Normal screens keep results and controls; Diagnostics holds
technical explanations, calculation scope, provenance and reports. **Damage
contribution** originally named the observed hitsplat ratio; build 0.29.0 withholds
that headline value while coverage remains unverified.
Last kill stays above independent collapsible sections: Compare setups (open),
Team & method and Diagnostics (initially closed). Gear & method and Gear & unlocks
are removed, including their manual controls; saved preferences still apply.
Verified arrival shows **Waiting for Nex**; loading, bank verification, unsupported
accounts/worlds and unavailable settings have distinct statuses. Invalid plan
inputs explain their field bounds. Saved corrections remain inspectable in Diagnostics.
The subsequent review fixes profile-thread clearing, explicit kill/attempt report
selection and scroll restoration after a displayed loading/banking pause.
[Review corrections, native screenshots and validation](docs/validation/pr48-review-followup.md) ·
[Original before/after evidence](docs/validation/ui1-47-wording-layout.md).

The main baseline includes N3 runtime plans, CTX1 #43, UI1 #46, PR #48's current
UI and the sidebar simplification in PR #49. Optional **Team & method** corrections and future defaults retain frozen
inputs, per-field confirmation and active-encounter guards. **Plan specs & gear**
keeps exact complete-package calculations and explicit scenario choices; unknown
inputs withhold dependent outputs. Reports remain selectable if copying fails.
[Context contract](docs/validation/ctx1-kill-context.md) ·
[N3 contract](docs/research/nex-runtime-plans.md).

[TASKS](TASKS.md) owns remaining mechanics and live gameplay acceptance.
[Consolidation and verification](docs/validation/sidebar-simplification.md).
Plugin Hub publication remains separate.

Bank-travel correction: completed history no longer expires while waiting to
open the bank. It stays hidden until same-account/profile/world revalidation;
logout, identity changes and Reset still clear it. The panel shows the build
version and a non-private reset explanation when history is cleared. Verified
return travel remains recognized while preferences are still loading.
Scene reloads inside a verified arena now resume capture without requiring a
bank visit; Reset remains available while a revalidated account is paused.
Client retesting remains open.
[Finding and verification](docs/validation/nex-bank-retention.md).

N3c connects complete actual, supported owned and hypothetical upgrade packages
through the existing ordinary/spec damage and finite energy/ammo kernels.
Conditional comparisons recompute whole packages, retain negative and zero deltas,
and expose resource/damage gaps and provenance. These are explicit conditional scenarios;
captured kills do not automatically become launch plans or personal recommendations.
[N3c contract and verification](docs/validation/nex-packages.md).
N3b supplies the underlying independent Voidwaker/ruby/diamond damage and exact
ammo/splinter branches. [N3b evidence](docs/validation/nex-spec-damage.md).

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
one earlier retained bank visit have in-session evidence. A later bank test lost
history; the 0.19.1 correction awaits client retesting. Broader session and player
checks remain open. [Capture contract](docs/validation/nex-smoke-anchor.md).
The development launcher retains RuneLite's required assertions with a temporary
GPU-package exception, correcting the 0.16.3 startup failure; see
[development](docs/DEVELOPMENT.md#gpu-assertion-during-development).

N2 compares complete owned packages against each distinct actually captured
state. It keeps melee/ranged/spec evidence separate, preserves planning locks,
distinguishes carried alternatives from bank preparation and exposes gains,
losses and zero. Nex ordinary offence uses exact sampled levels/prayers and a
labeled base-stat scenario; it does not estimate kill time or personal contribution.
[N2 evidence and remaining checks](docs/validation/nex-owned.md).

N1 session capture retains last complete timing and diagnostic damage evidence, comparable means/ranges,
excluded attempts and immutable timelines. Its real-client mappings and player
acceptance are still pending. [N1 validation](docs/validation/nex-session.md).

The pilot is **Nex / Trio MP2 / one kill per trip**, with Lightbearer throughout
and assumed upgraded Death Charge. Equipment, switches, active spell state and
readable unlocks are observed automatically. The sidebar omits **Gear & method**
(Setup), including gear locks and manual method controls, and **Gear & unlocks**
(Account). Existing saved preferences, planning locks and manual facts still apply;
they never overwrite the actually-worn history. No manual setup is required.
Complete summaries stay in memory across recognized same-account bank trips and
clear on logout/profile change.
Observed hitsplat share is scoped to Nex and the four named minions; it is not
server contribution or loot eligibility. Timing uses the observed smoke
announcement and death/dying signal, with no game-timer calibration claim.

The previous general homepage and global progression ranking are removed. The
reported Tbow/amethyst-quiver and Fang/defender equipment has an anonymous regression
fixture; levels, prayers, stock and phase inputs in that fixture are synthetic.
Ancient blessing remains a physical worn item while quiver arrows are a separate
projectile input. Conditional ordinary charged/blessed quiver offence is supported;
N3b adds conditional finite ammo/splinter and special-attack arithmetic. Automatic
spec recommendations, healing and contribution/time remain N3–N4.
Scorching and other unsupported alternatives leave explicit coverage gaps
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

For calculation/UI review without gameplay, see
[the DEV1 offline command](docs/DEVELOPMENT.md#offline-review-dev1). It runs ten
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
