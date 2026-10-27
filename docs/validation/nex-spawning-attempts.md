# Nex spawning and extra incomplete attempts

28 October 2026. Development build **0.33.1**, adapter **nex-public-12**, on
PR #59 after `1240112`. No merge or Plugin Hub publication.

## Supplied live evidence

The user confirms exactly one ordinary, fully completed Nex kill since starting
the test. The 0.33.0 / RuneLite 1.13.4 screenshot shows **1 complete / 0 failed /
2 excluded**, with an Incomplete attempt notice above the retained comparison.
The supplied report is frozen on the complete kill: **382.2 seconds**, smoke at
offset 0, death/dying at 637 and arena completion at 645; 637/637 sampled ticks,
continuous capture and no observation/export truncation. This is evidence of a
completed capture, not a failed player attempt. Raw captures stay outside Git.

The report does not contain the two excluded records' reasons, so their exact
callback sequences remain unverified. A synthetic adapter replay reproduces the
same one-complete/two-excluded count from one actual encounter: an active arena
reading while Nex is spawning, a scene transition before combat, the complete
fight, then a spawning respawn and departure. Both extra records arise because
the old partial-start gate admitted `NEX_SPAWNING` from the arena value alone.

## Correction and boundaries

`NexObserver.startIfReady` waits while the tracked NPC is `NEX_SPAWNING`, unless
the actual smoke announcement has arrived. This applies to initial active-arena
readings and fresh arena transitions/respawns. The actor remains tracked and
bounded pre-start action context remains available. Smoke starts and samples on
its callback even if the NPC getter is still spawning. An already active capture
is not discarded by this guard. Normal-form mid-kill arrivals still retain unknown
starts and incomplete diagnostics; completion still requires all existing signals.

This correction does not reclassify retained exclusions or widen numerical
support. The supplied complete kill has 40 Fang candidates but zero included
samples: 20 window exclusions, 16 ambiguous attack exclusions and four target
exclusions. Known special windows and unknown ordinary windows stay excluded.
Ordinary-window coverage and current recommendation usefulness remain open.

## Verification

Two focused regressions fail before the correction. The one-encounter replay
returns three records instead of one; the spawning-form test starts a capture
without smoke. The corrected tests require one complete record, zero excluded
records and no incomplete latest-attempt banner trigger. They also check immediate
smoke start before NPC transformation and preservation of pre-start food context.
Existing adapter/capture tests retain late-arrival, boundary-order, death,
missing-signal, reset, identity, limits and frozen-history coverage.

Exact full-build counts and current-head CI/artifact links are recorded on
[PR #59](https://github.com/PetterHL/ironpath/pull/59). The replay is synthetic;
the corrected spawning lifecycle has not yet been confirmed in a live client.
