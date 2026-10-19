# Encounter-first UX

Current presentation decisions: **19 October 2026, PR #48 approved review / build 0.23.1**.
[TASKS](../TASKS.md) is the sole backlog. This guidance supersedes earlier requests
for scope/assumption paragraphs beside cards or in ordinary details. Historical
validation records retain their original screenshots and acceptance boundaries.

## Normal screens

Keep one native 225px **Encounter** sidebar with independently collapsible sections.
The last kill duration and Damage contribution stay visible above Compare setups
(initially expanded), Team & method, Gear & method, Gear & unlocks and Diagnostics
(initially collapsed, Diagnostics last). Each full-width header has a small arrow,
is wholly clickable and supports Space/Enter with an accessible expansion state.
Collapsed headers have no explanatory subtitle. Retain expansion during use.
Do not duplicate section navigation with separate tab/buttons.

Context/default and gear drafts, the selected kill/state/package, planner inputs
and scroll position survive unrelated renders and verified scene reloads. Keep
Plan specs & gear within its selected comparison. Hide suspended evidence and
clear reports immediately; restore drafts only within the same session scope.
Identity/world/reset/eviction guards discard unavailable evidence and stale actions.

Encounter shows the last complete duration and **Damage contribution**, optional
matched mean/count, a compact confirmed/unconfirmed team/method strip, **Compare
setups** cards, inspection actions and gear/session controls. Incomplete attempts
stay separate; pending/no-comparison/no-positive states use one brief status.
No forced focus/navigation or live combat coaching.

Use **Loading encounter…** while arrival/reload evidence is pending, **Waiting for
Nex** only when the arena is verified and capture is ready, **Tracking kill…**
during capture and **Kill tracking: Off** when disabled. Outbound bank verification
keeps an actionable bank instruction. Login/account loading, unsupported account,
unsupported world and unavailable settings have distinct brief reasons. Detailed
transitions belong in Diagnostics.

Normal cards and details show item/package identity, the supported result and
controls. Keep short role/phase and origin labels where they identify the result:
**Equipped gear**, **Inventory gear**, **Bank gear**, **Hypothetical gear**.
Equipped means actually worn; inventory includes carried items. Search all the
same complete carried packages. Do not relabel inventory as equipped or infer
usable stock/packing from bank presence. Keep exact names, IDs and variants.

Keep **Encounter**, **Compare setups** and **{count}-item package**. Package gains
are calculated together; independent alternatives are not additive. Cards use the
changed item, including ammunition rather than an unchanged weapon, and genuine
sprites for multi-item packages. There is no filler quota or cross-state ranking.
Supported zero/negative alternatives remain available through comparison controls.

**Damage contribution** is only the display label for the unchanged observed
Nex/named-minion hitsplat ratio. Detailed scope and the difference from server loot
credit belong in Diagnostics. No new attribution/contribution mathematics.

## Language and settings

Use the following player-facing labels, including meaningful tooltips and
accessibility names. Internal enums, preference keys and model identifiers stay
unchanged.

| Surface | Label |
|---|---|
| Setup / account | Gear & method / Gear & unlocks |
| Kill context / future defaults | Team & method / Default team & method |
| Context fields | P2 method / Team type / Team size / Players, including you |
| Per-kill apply | Apply to this kill |
| Capture toggle | Kill tracking: On / Off; Turn on / off kill tracking |
| Reset / counts | Clear session / Session stats |
| Owned comparisons | Compare with owned gear |
| Package detail / proposed option | Compare gear / Proposed gear setup |
| Quiver / identical package | Selected quiver ammo / Same gear · no change |
| Lock removal | Remove selected gear lock |
| Resource review / planner | Spec energy & Death Charge / Plan specs & gear |
| Attack policy / options | When to spec / Normal attacks / Spec whenever energy allows |
| Plan evaluation | Compare this plan |

Render account-choice descriptions, never keys such as owns-r3bFang. If a retained
ownership label is missing, derive it from **every exact member** of the existing
set, preserving its confirmation key and membership. Long selected names must be
readable in the adjacent value or tooltip without changing the underlying choice.
Saved corrections show their readable name and Owned / Not owned or Unlocked /
Locked state. Timestamps and manual provenance belong in Diagnostics; observed
facts keep their precedence over saved corrections.

Team editing remains optional: independent strategy, team type and exact/variable/
unknown count, per-field confirmation and a separate Use for future choice.
Inherited defaults do not confirm a kill. Keep unfinished drafts and clear errors
when resolved. Routine loaded/saved/observed-locally confirmations stay hidden;
failed saves show **Settings not saved**.

The N3 planner contains concise fields and explicit unchecked scenario choices:
legal attack windows; initial attack/cast readiness; no active Death Charge;
Lightbearer throughout; usable gear/stock; runes/legal casts; distinct eligible
deaths; fixed HP/phase; spell/switch delays included. Detailed definitions live in
Diagnostics. Never tick hidden confirmations or fabricate unknown values. Copy
plan report explicitly reveals the selectable report, including clipboard fallback.
Invalid submissions show transient field-specific constraints: window 1–3000
whole ticks, energy 0–100% in 0.1% steps, bounded HP/restore/cap values and
1–128 distinct increasing attack ticks before the exclusive window end. Optional
blank inputs remain unknown. Cast/death lists have the same bounded tick format.

## Diagnostics

Diagnostics is the common, explicitly opened place for technical explanation:
observation/timer/damage scope, matching rules, complete-package/resource models,
assumptions, pilot restrictions, unsupported/future work, testing limitations,
versions/provenance, long missing-coverage lists and raw in-memory readings/traces.
The retained phrase **The pilot keeps Lightbearer equipped.** belongs here.

Keep useful actual gear Details actions. Do not replace removed prose with rows
of explanation buttons or info icons. Use the approved main sections rather than
adding explanation sections to individual cards. Ordinary Full gear setups
shows gear, while calculation assumptions and raw capture/resource inspection are
under existing Diagnostics → Calculation details. An unsupported number remains
unavailable after its explanation moves.

Diagnostics and report export bind to the same immutable selected kill/comparison,
context/input/model revisions and planning/unlock facts. Explicit selection,
including no selection, wins over a default resolved when pending work arrives.
Copy report is user-triggered and allowlisted. No names/identifiers, roster hashes,
raw inventory/bank dumps, automatic upload, history logging or trace persistence.
A busy/headless clipboard leaves selectable text. Preserve report selection/caret/
scroll/focus subtree and expanded Calculation details across unrelated refreshes;
clear them with record/identity/visibility guards. Paused history stays inaccessible.

## Layout and verification

Centre section headings, headline values, item/package content groups and action
button groups with balanced margins. A sprite and long name form one coherent
centred group with naturally wrapped text. Multi-item packages retain real sprites.
Keep form labels/fields, tables and long Diagnostics text left aligned. Keep
clear section arrows, restrained colours and a compact hierarchy without widening the
sidebar, shrinking body fonts or introducing large empty spaces.

Inspect actual same-fixture native Swing **225 × 600** viewports at body fonts
**12 and 20**, plus full-height/scrolled output: Encounter, multi-item packages,
team/gear/unlock settings, N3 planner and Diagnostics. Exercise pending, incomplete,
unknown/unconfirmed and no-positive states. Geometry checks supplement whole-image
visual inspection. Keep pinned genuine artwork/Jagex notices; these synthetic
renders are not live client/player acceptance.

[PR #48 follow-up](validation/pr48-review-followup.md) and
[issue #47 validation](validation/ui1-47-wording-layout.md) record exact bases,
commands, evidence and remaining live checks. Retain existing banking/reload/reset,
identity/stale callback, frozen history, Back/scroll, context and independent
arithmetic regressions. No new formula, boss, acquisition logic, automatic spec
recommendation or N4 estimate belongs in this presentation refinement.
