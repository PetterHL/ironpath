# Post-kill N3A/N3B/N3C integration

18 October 2026. Development build 0.22.0. Runtime join
`nex-declared-plan-1`; package bridge `nex-package-3`. Based on main `96d91e8`;
CTX1 remains a separate PR. Existing mechanics and source/licence reviews are
retained in [resources](nex-spec-resources.md), [damage/ammo](nex-spec-damage.md)
and [packages](nex-packages.md). No new game formula or external dataset is added.

## What the captured kill supplies

The guarded owned-evaluation worker freezes resource readings, every retained
captured equipment/level/prayer/style state, the selected N2 owned alternative,
planning locks and equip-fact precedence. Later account/preferences reads cannot
change an already-inspected scenario. Net energy changes, equipped spec weapons,
animations and worn time do not become identified attacks, casts or procs.

The optional **Spec and package plans** entry opens all captured states. Each state
has **Conditional N3 package plan**. No plan is required before or during play.
The captured package, selected supported owned package (or unchanged captured
baseline when no N2 pair exists), and owned-plus-hypothetical edits all use one
immutable conditions object. Every upgrade recomputes complete slots, requirements,
hands, ammo, prayers/style, locks, quiver branches and package-specific damage.
This is a declared conditional plan, not an automatic recommendation.

## Explicit declaration

The player supplies a 1–3000 tick exclusive horizon, 1–128 legal attack offsets,
starting energy, first natural-restore offset/order, fixed HP/hard-cap assumptions
and optional cast/distinct eligible-death offsets (one death per offset in this
editor; the underlying API retains more general explicit equal-tick events). Empty required fields remain
unknown; no attack offsets is invalid, rather than a zero-output plan. HP unknowns
withhold dependent damage while independently supported resources remain visible.
Initial attack/spell readiness, fixed Lightbearer, usable sampled stock, legal
access and rune/spell opportunities require explicit confirmation. Attack offsets
must already account for spell/switch delays; the bridge does not model those costs.
The target/phase/window comes from the selected captured state. Unsupported
phases/windows retain damage gaps. This does not resolve Nex Zaros transforms.

Ordinary-only, first-special and funded-special policies create indexed actions
at the same shared opportunities. Exact VW/ZCB packages can attempt their own
special; an explicit ordinary fallback follows a rejected special on the same
tick. Accepted attacks displace other attacks through the existing shared delay.
Cast, eligible death, primary attack, fallback is the explicit equal-tick event
order; natural restoration uses the chosen before/after policy. Cast upgrade state
comes from frozen planning, never from a sampled one-charge reading.

Current finite-ammo scenarios declare no active retrieval/no saved ammo. A charged
quiver spends a splinter for an admitted shot while available, then recomputes the
uncharged branch. The resource-branch probability is retained; a specific branch
is not expected average consumption. Exact sampled inventory, worn stack and
quiver counters are reused as explicit usable-stock assumptions. Unread inventory,
unknown quantities, bank-only preparation or unknown joint packing block dependent
resource results. Later bank counts are never substituted. Only selected ammo
pools appear in the result report.

Hypothetical edits can change multiple slots (for example weapon + compatible
bolts + empty offhand), with explicit exact item forms and added usable quantities.
Original stock is preserved; hypothetical additions never enter the owned baseline.
All plans reserve the same inventory space after captured supplies, retaining
worn reserve packages so displaced gear cannot disappear. An observed reserve
package is exempt from proposal locks; actions using a proposed package still
obey them. An unchanged exact worn item retains observed equip usability at the
same real levels; this does not turn bank presence into an equip unlock or persist
a new account fact. New item forms still require their own requirements/unlocks.

## N3A natural-restoration sensitivity

The resource review also offers **Natural-restoration examples**. A complete,
continuous capture with an unambiguous measured-start energy reading and
Lightbearer in every recorded fight state can seed three hypothetical periodic
VW/ZCB schedules over its measured duration. Fixed ring and periodic attack access
remain assumptions; no actual launch attribution is asserted. All 0–25 first
restore offsets and both orders are evaluated by the existing `SpecTimeline`.
Only compact min/max uses/final-energy/cap-loss summaries are retained.

These examples deliberately exclude Death Charge/other sources by assumption.
They do not estimate the unknown effect of observed casts/procs as zero. The full
plan editor can instead declare casts/deaths and exercise the existing finite
Death Charge model. Sensitivity ranges have no statistical confidence meaning.

## Publication, export and limits

The production plugin's executor performs calculations; Swing only edits inputs
and displays immutable results. Request tickets, frozen calculation/choice/pair
identity, current view/account/session guards and the active-attempt gate prevent
stale publication. Input edits clear prior results immediately. Reset, logout,
world/profile changes, suspended travel and detached views cannot publish or copy
old data. A new kill does not rewrite an already-inspected plan. No plan edits or
performance data are persisted.

**Copy plan report** exports fixed allowed fields: declared numeric conditions,
exact relevant package slots, scoped totals/deltas/gaps and model/source versions.
It omits names, account/profile IDs, roster/cohort strings, raw traces and unrelated
containers; it is bounded to 24,000 characters with a truncation notice. A selectable
report and explicit feedback handle clipboard failure. Main Diagnostics schema 2
also includes frozen N3A resource evidence/examples. Synthetic/live provenance is
explicit. No kill wall-clock timestamp is invented.

Expected launch damage and self damage are conditional on supplied per-launch HP.
They are not an HP trajectory, actual spec count, globally best weapon, personal
contribution, survival or team-time forecast. Automatic opportunity attribution,
full target transforms, acquisition/cost presentation, food/potion/delay models
and N4 remain separate work in TASKS. Live RuneLite/player validation remains open.
