# Encounter-first UX

Encounter direction selected 15 October; UI review incorporated 18 October 2026.
UI1 is visually refined for review in 0.21.1, on CTX1 0.21.0. TASKS.md owns
delivery. Later versioned sections retain the implemented behavior/history.

## PR #46 visual review

The current review keeps one IronPath / Nex heading, a compact selected-tab row,
last-kill results, a context strip with Edit alongside it, then useful equipment.
Body fonts stay 12/20, with enlarged navigation at 18 to fit readable full labels.
Result values use columns at the normal font and value/label rows when enlarged.
Genuine item sprites, changed-item/package identity and supported DPS dominate
cards; modelled role/phase, reasons, availability/packing and search limits remain
secondary readable text. Multi-item packages show several changed-item sprites.
Details has one consistent position at the bottom right.

Inspect groups comparisons and kill details. Setup & session groups Setup,
Account, future context, capture, Reset and counts. All actions keep their guards,
frozen selection, Back and remembered scrolling. Calculation details in Diagnostics
retains expansion across unrelated account reads for the same selected kill.

[Before/after screenshots, sprite provenance and open client checks](validation/ui1-46-visual-review.md).

## Native refinement (0.21.1, issue #44)

Use the existing Encounter / Diagnostics reference as a visual guide, adapted to
one 225px native sidebar. It does not replace the product contract or roadmap.
Show a clear selected tab, quiet dark surfaces, separated sections and native
ItemManager sprites. The current review uses the readable compact navigation above.

Encounter order: Nex; a last-complete-kill card with prominent observed duration
and damage share labeled as Nex/minion hitsplats; one compact context summary
with Edit; **Compare setups**. An unconfirmed/unknown context remains explicit.
Show a matched mean only when more than one compatible kill makes it useful.
Timer boundaries, exclusions and damage interpretation remain inspectable.

Each equipment card names the actual changed item or complete package, gives
supported modelled DPS, role/phase, one short reason, material carried/bank and
partial-search qualifiers, and Details. Begin with two deterministic cards at
most; **More comparisons** exposes the rest without sorting independent state
percentages. All zero/negative/unavailable alternatives remain accessible through
**Owned setup comparisons**. This display limit is not a product/catalogue ceiling.

Setup, Account, future context, kill/comparison details and Reset remain explicit
secondary controls; capture settings and counts sit in Session tools. No raw tick
IDs, repeated pilot disclaimers or empty matched statistics crowd the main view.
Diagnostics groups Inputs, Calculation status and Missing / limited; expandable
Calculation details retain versions, exact scope, support counts and matching rules.
The actual build remains directly visible. Copy report and selectable fallback
keep the same immutable selection, account guards and privacy contract.

The optional context editor retains independent strategy/group/participation,
per-field confirmation/provenance, explicit Apply and separate future defaults.
Historical edits/report navigation cannot interrupt another active attempt.
Actual-to-owned and owned-to-new-upgrade value, relevant acquisition/recurring
costs and validated N4 contribution/team-time remain governed by TASKS and DESIGN.
This refinement adds no unsupported metric or new combat formula.

[Native before/after screenshots, visual comparison and open client checks](validation/ui1-native-refinement.md).

## UI1 implementation (0.20.0, for review)

One native 225px panel with **Encounter / Diagnostics** primary navigation.
Setup and Account remain explicit secondary actions, together with comparisons,
kill/attempt details and Reset. Keep frozen inspection and Back/scroll behavior.

Original 0.20.0 order (refined above): boss and pilot model scope; last complete duration and scoped
observed hitsplat share; compact comparable mean/count; **Improve your setup**
cards; secondary actions. Keep failed/censored attempts separate. At n=1, show
one matching kill rather than repeating its mean/range. Details retain spread,
mean-per-kill versus aggregate share, capture scope and exclusions.

Cards are single-column, with a changed-item sprite/name (or multi-item package),
one supported primary metric, phase/role scope, one short reason and details.
Do not name an unchanged weapon when the gain comes from arrows or armour.
Group/deduplicate deterministically; do not rank unrelated state percentages as
whole-kill impact. Keep conditional/bank/partial-search qualifiers beside affected
claims. Preserve zero/negative comparisons without filler cards. Use ItemManager
live; label offline sprite placeholders. Stack metrics when enlarged fonts require it.

Diagnostics is directly available, including safe empty/reset/paused/pending
states, without exposing hidden history or bypassing account guards. It contains
status/build, observed-versus-assumed inputs, coverage and detailed results, plus
one-click Copy report. The selected immutable calculation and exported report
must agree. Use the allowlisted export contract in KILL_IMPACT.md; clipboard
failure leaves selectable text. No automatic sending or account connection.

In-client Save screenshot, editable context and new numerical claims are outside
UI1. Use DEV1 native PNGs and manual client screenshots for this first cycle.
Check actual viewport hierarchy and controls as well as full-height text wrapping.

Card selection visits carried scenarios before conditional bank preparation, then
uses original capture order. It selects the first positive supported whole-package
comparison in each state (proposed package first, then explicit alternatives), and
deduplicates identical before/after equipment/projectiles within each role. A
carried change wins over the identical bank scenario. There is no fixed quota and
no cross-state percentage ranking; zero/negative/unavailable rows remain in details.
A quiver-to-worn-ammo change identifies the new projectile rather than the unchanged
weapon or a fictitious second empty-projectile upgrade.

Diagnostics freezes the selected scenario and pair. Returning with **Back to
comparison** preserves the selected pair; **Encounter** explicitly opens the latest
result. With no initial kill, Diagnostics can show the next published result. An
already selected kill never silently changes when a newer kill arrives. Suspended
history remains hidden, and identity/reset clears report text and invalidates old
actions. The export uses a report-local K1/C1 plus a memory-only calculation
revision and frozen planning locks, never current preferences.

An expanded selectable report retains its text selection, caret, scroll and
clipboard feedback across refreshes while the same accessible report is selected.
Its text/scroll subtree stays attached to preserve focus. A changed or hidden
report clears the previous text and starts collapsed.

See [UI1 validation](validation/ui1-encounter-diagnostics.md) for viewport evidence,
clipboard fallback and the separate client checks.

## Editable kill context (CTX1, 0.21.0)

**Future kill context** is optional before play. **Edit kill context** sits beside
the latest completed or already-inspected kill, including its Diagnostics and
comparison/details views. The editor retains an unfinished draft across unrelated
refreshes. Strategy (MP2/RP2/Other/Unknown), organized/mass/unknown group and
exact/variable/unknown participation are separate controls. Exact counts must be
positive whole numbers and include the player. Mass never changes strategy.

**Apply context** changes only that kill. Confirm strategy, group and count
separately when known; unconfirmed manual entries and inherited defaults stay
unconfirmed. **Use for future** separately saves future defaults. It never confirms
future kills or alters an already-active attempt. **Context sources** exposes
per-field provenance. There is no mandatory pre-fight form or new history browser.

The selected record shows its observed duration/damage and context revision.
During recomputation, previous calculation outputs are withheld. Diagnostics and
Copy report use the same corrected revision and frozen calculation inputs;
newer bank observations/preferences cannot enter an edit. Completed comparisons
retain their frozen planning inputs; Setup changes apply to future comparisons.
A historical annotation can complete while another attempt continues.

Inventory/equipment refreshes during that attempt also leave the older report's
Copy, selectable text and frozen detail/comparison navigation usable. These read
actions follow the selected immutable result and session revisions; Account/Setup
mutations retain their stricter live-view checks.

Unconfirmed/unknown/variable or incompatible context has no matched average.
Confirmed organized MP2/RP2 records need compatible observed setup/timer/roster
evidence; count alone is insufficient and does not establish equal team damage.
Mass and unspecified Other remain unmatched. Conditional ordinary state comparisons
can remain inspectable outside the pilot; they are not method recommendations.
No automatic spec recommendation, contribution model or team-time estimate is added.

[CTX1 validation](validation/ctx1-kill-context.md) includes 225px editor/report PNGs
at 12pt/20pt, real JViewport screenfuls and the separate unperformed client checks.

## Pilot planning choices

The main view identifies `Nex · Trio MP2 pilot model`, independently of annotated
kill context. Detect equipment/spell use and permit focused corrections.
Lightbearer throughout and upgraded Death Charge are the confirmed pilot choices.
A compact Setup action exposes melee, ranged and spec packages plus locked choices.
Detected, manually assumed and unknown state must remain distinguishable.

## Main view

1. Last complete kill: measured duration and observed damage share, each with a
   defined scope. Session mean and comparable sample count are nearby; spread and
   excluded/incomplete counts are inspectable.
2. Improve with owned equipment: actual setup versus a proposed compatible owned
   package. No acquisition implication.
3. Relevant new upgrades: marginal value against the owned baseline, recurring
   supply costs where relevant and a concise acquisition action.

Keep the first view short. Show useful results without a fixed filler quota or a
catalogue ceiling. A card has item identity, one concrete benefit and the strongest
supported metric. A material missing input stays next to its affected claim;
technical diagnostics and repeated explanations belong in details.

Opening an option shows the actual/owned/candidate setup, changed slots, supported
phase/spec results and assumptions. Acquisition is available there without making
the player navigate multiple parallel recommendation systems. Back preserves focus
and scroll; fresh kills must not replace the historical baseline being inspected.

## Interaction and presentation rules

- Update quietly after completion. No modal, forced sidebar/focus switch, chat
  spam, in-combat recommendation overlay or next-action coaching.
- Use native Swing/PluginPanel and ItemManager sprites. Validate ordinary narrow
  width, long labels, real images and enlarged fonts; never force a wider window.
- Unknown and incomplete data have concise states. Show honest zero/no-useful-
  upgrade results. Unsupported benefits do not masquerade as numeric priorities.
- Display whole-package changes as such. Never add independent card gains.
- Keep resource upgrades such as arrows separate from permanent equipment.
- Provide Reset session. A bank trip keeps completed same-account summaries;
  logout/account change clears them. Persistent method choice is not persistent
  performance history.

## N1 capture foundation

N1 development build 0.15.0 opens `Nex · Trio MP2 · One kill per trip`. Encounter,
Setup and Account are the native navigation. The homepage shows last complete
observed timing/share, same-cohort means, sample counts, min–max spread and excluded
attempt counts. Partial attempts have separate inspectable evidence. Kill details
freeze the selected summary across new results and expose timing/damage scope,
phase/target, state/resource samples and uncertainty. No in-combat recommendations
or forced navigation are added.

Setup lists observed worn roles and keeps manual planning locks separate from
history. Item selection uses reviewed/observed names; no per-kill setup form is
required. The upgrade toggle is labeled an assumption, while raw detected Death
Charge state is in details. Account corrections remain accessible. Method/locks
and correction writes retain profile/generation/revision guards and serial local IO.

N1 retired the old general homepage navigation. N2 adds the owned flow below;
new-upgrade cards, counterfactual contribution and team seconds remain N3–N4. [Synthetic checks and pending native-client/player acceptance](validation/nex-session.md)
are recorded separately; historical screenshots are not acceptance of this view.

## N2 owned equipment

Build 0.16.0 adds **Improve with owned equipment** after the last complete result.
Each displayed role names its captured tick and conditional theoretical result;
it does not summarize whole-kill improvement. **Owned setup comparisons** opens a
frozen result with state/phase and carried/bank-preparation selection. Unsupported
states remain inspectable with their actual evidence and a concise affected gap.

A complete-package proposal is separate from explicit pairwise alternatives.
Negative and unchanged results remain visible; blood-fury offence cannot establish
healing or overall value. The physical ammo slot and selected quiver projectile
are shown separately. Full slots and numerical/source assumptions expand under
**Full setups and calculation basis**. Back restores the encounter position;
new kills do not replace the inspected summary or selected pair. Identity resets
clear it. Native sprites remain ItemManager images; synthetic captures use empty
sprite wells. Ordinary/enlarged-font narrow captures are verification, not client
or player acceptance.

## 0.16.1 live-test follow-up

The main view explicitly says **Observing the current Nex attempt** during capture,
then returns to the last completed/partial result. Travel status is refreshed when
capture starts; a waiting message must not imply capture is inactive. This adds no
live combat advice. Kill details now list observed start boundary, matched smoke,
Nex death/dying and arena-end ticks, observed phases and sample continuity. Missing
signals have specific reasons; an excluded attempt is never silently promoted.
See [first-client findings and retest](validation/nex-live-start.md).

## 0.16.2 signal investigation

The second client attempt still lacks arena boundaries. Kill details identify the
adapter, show observed smoke separately from matched smoke, and list raw barrier
transitions with callback/poll origin. A **Capture signal trace** button reveals
the bounded nonpersistent sequence without requiring the equipment/inventory
section to be shared. An unmatched end waits at most 20 ticks for diagnostic
evidence; travel still flushes immediately. Acceptance criteria are unchanged.
See [second-client findings](validation/nex-live-signals.md).

## N3a post-kill resources

Build 0.17.0 adds **Special-attack resources** inside the frozen **Owned setup
comparisons** view. It shows first/last recorded energy, sampled net increases/
decreases and observed Lightbearer/Death Charge evidence. **Resource readings and
rules** expands equal-tick-ordered readings and reference costs. Unknown readings
remain explicit. No spec/attack/proc count is inferred from energy changes, and
at-cap readings are not labeled wasted energy. New results do not replace the
inspected kill; identity reset clears it. No live coaching or capture change.
The conditional resource scenario API has no player setup wizard or automatic
spec recommendation. [Scope and validation](validation/nex-spec-resources.md).
Combined build 0.17.1 retains the resource review alongside the 0.16.6 role/support
filters and paused-travel presentation; changing an ordinary scenario filter does
not change which kill the resource review describes.

Build 0.18.0 adds independent N3b damage/ammo scenario kernels without a new runtime
calculation view. Resource details explain that sampled readings cannot establish
launches, HP or initial conditions. No automatic spec-weapon ranking or upgrade
card is shown until those inputs and package/phase handling are supported.

## 0.16.5 observed smoke start

The measured interval is labeled **Observed smoke announcement → Nex death/dying**.
Details show the measured smoke start and the optional arena start transition as
distinct fields. A visible arena-entry transition is not an eligibility condition.
Before-smoke states remain in the history selector with an explicit prefix and
exclusion explanation; the default selection is the first fight state. They are
excluded from combat statistics and owned-equipment scenarios. The start is never
backdated, and a later smoke line cannot erase observed combat/later phases.
End matching distinguishes dying from subsequent arena completion. Missing
evidence remains excluded. [Contract and verification](validation/nex-smoke-anchor.md).

## 0.16.6 comparison coverage and selection

The main view counts supported carried scenarios, bank preparation and unavailable
states separately. These counts do not represent fight/damage coverage. A missing
ordinary role has an explicit unavailable message. **Owned setup comparisons**
starts with supported scenarios when any exist, with role and support filters.
**All states** and **Unavailable comparisons** retain the gaps; **Why comparisons
are unavailable** expands grouped reason counts. An empty filter has a clear
message and disabled state selector. Filtering does not recompute or alter history.
Bank preparation remains labeled conditional, and identity changes clear the view.

While a bank/travel session is paused, every navigation page shows the revalidation
message. Setup must not describe temporarily hidden history as an empty session.
The ordinary Setup content returns after revalidation without changing stored
summaries or preferences.

The main result/comparison follows the latest complete kill. An already inspected
comparison stays frozen while the view remains valid; there is currently no
selector for opening any earlier kill's comparison from the main view. A new
partial attempt does not enter complete averages or replace the last complete
baseline. Setup/history across further travel still needs live checks.

## 0.19.0 complete-package scenarios

N3c's explicit conditional scenario API connects complete packages to finite
spec plans without changing runtime navigation or adding a setup wizard. Captured
readings still do not establish legal launches, HP or initial conditions. No
personal spec upgrade, contribution or seconds-saved card is shown.
[Contract and verification](validation/nex-packages.md).

## 0.19.1 bank waiting and retention

Completed history remains paused and hidden until the same-account bank trip is
verified, without a deadline for opening the bank or returning. A nearby loaded
Nex does not falsely end the outbound wait. Opening the bank restores the last
complete result, session statistics and details; logout/account/profile/world
changes and Reset still clear them. The reported empty bank-open session failed
validation and requires retesting on the corrected build.
[Finding, regression cases and next client check](validation/nex-bank-retention.md).

## 0.19.2 test visibility

The header shows the actual bundled build version on the development classpath
and in the assembled plugin. When completed history is cleared, the empty
Encounter page retains a short constant explanation (for example world change,
unverified travel or Reset). Fresh account reads do not overwrite that cause;
a new captured attempt replaces it. Explanations contain no account identifiers
or old performance data and are neither saved nor logged. Normal travel pauses
remain separate from reset messages.

A verified bank-return scene now supplies the next departure source while settings
IO is pending. An additional scene transition must not require a completed
preference read to preserve history. [Verification](validation/nex-bank-retention.md).

## 0.19.3 arena scene reloads

A scene rebuild while still in the verified Nex arena can resume observation
without a bank visit. Completed summaries remain available after revalidation;
active traces still end at the scene boundary. An observed departure continues
to require bank verification, with no waiting deadline. After account/settings
revalidation, the paused view offers Reset session with the existing view and
identity guards. Client testing is deferred;
[automated regression evidence](validation/nex-bank-retention.md) is recorded separately.
