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
