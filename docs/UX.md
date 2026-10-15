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

## Current build

T0 retains the working Upgrades/Account and Impact/Acquisition UI and opt-in Zilyana
experiment. N1 replaces the homepage as it delivers the new session view; N2
replaces the old primary ranking. Old captures are [historical evidence](archive/pre-nex-20261015/UX.md),
not desired visual references or proof of Nex usability. [TASKS](../TASKS.md) owns
the native-client and player acceptance work.
