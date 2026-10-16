# Encounter-first UX

Selected direction, 15 October 2026. This is the target for the Nex pilot, not a
claim that the transition build already contains these screens.

## One remembered context

The main view starts with `Nex · Trio MP2 · One kill per trip`. The method is a
saved preference; detect equipment/spell use and ask only focused corrections.
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
