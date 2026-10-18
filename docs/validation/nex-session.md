# N1 Nex session implementation

15 October 2026. Development build 0.15.0, based on merged transition `3fbaae6`.
**Implementation for review; N1 real-client/player acceptance remains open.**

Later finding: the [0.19.1 bank-retention correction](nex-bank-retention.md)
records a complete-result screenshot followed by empty history with the bank
open. It supersedes the 100-tick travel-expiry policy described in this original
report; its corrected client round trip remains unverified.

## Delivered behavior

- The toolbar opens Nex / Trio MP2 / one kill per trip with Encounter, Setup and
  Account navigation. The global progression homepage is no longer reachable.
  Old calculation/presentation/ranking code and its arithmetic regressions remain
  for N2's contextual replacement/removal, without an alternate runtime product.
- Public Nex/mage identities, phase/subphase lines, arena start/end and death/dying
  signals feed a bounded pure capture machine. A full kill needs correlated start,
  complete phase/tick evidence and correlated end signals. Mid-kill arrival/enable,
  death, teleport, missing boundaries and limit overflows remain separate attempts.
- Immutable actual worn samples preserve melee, ranged and selected-spec candidate
  states, target/animation evidence, levels, prayers, inventory quantities, ammo,
  special energy and raw Death Charge/quiver counters. Public item names are read
  on the client thread. Unsupported gear is retained. A Tbow empty offhand does
  not erase an earlier/later melee defender. No hit is assigned to its impact-time
  weapon, no inventory loss proves consumption and no resource change proves its
  cause. Internal item charges without a public mapping remain unknown.
- Method and optional planning locks/corrections are explicit local preferences;
  observed history is immutable. The pilot keeps Lightbearer; conflicting ring
  locks are rejected, while a differing observed ring stays visible as a gap.
  Upgraded Death Charge is an assumption until active spell evidence distinguishes
  it; one remaining charge/zero does not establish upgrade absence.
- Last complete timing/share, same-cohort means, sample counts, min–max ranges,
  aggregate damage share and excluded counts are shown separately. Latest partial
  attempt details remain inspectable. Selected details freeze across new results.
  The mean uses matching method/locks, observed worn packages/levels/prayers and
  arena roster; roster uncertainty or changes prevent matching other attempts.
- Completed evidence survives tested arena→bank→arena round trips. Live account
  snapshots/references and active buffers still clear on loading; summaries remain
  hidden until same-account/profile/world banking revalidation. Each pending leg
  expires after 100 ticks. Logout/disconnect, profile/account/world change,
  unsupported worlds, disable and Reset clear the in-memory session. Stale queued
  actions/load/save/image callbacks cannot publish into another character's view.
- Preference schema 3 stores only method/upgrade assumption, explicit locks and
  existing manual facts/choices. Schemas 1/2 retain their stable prior keys. No
  observed containers, roster/name, trace or performance history is serialized.
  No runtime networking, logging, automation or new dependency was introduced.

[Reviewed signals, sources, scope and limitations](../research/nex-events.md).
N2 owns compatible owned-package proposals; N3 owns finite spec/energy models;
N4 owns validated contribution/time estimates. This build makes none of those claims.

## Verification

JDK 11.0.32.1, Gradle wrapper 8.10; resolved RuneLite **1.13.2**. The environment's
activation script supplies the inherited proxy and CA trust. Dependency downloads
succeeded; later runs reuse the Gradle cache.

Final verification: `XDG_CACHE_HOME=/tmp/ironpath-cache ./gradlew test build --no-daemon`
**passed: 371 tests, 54 suites, zero failures/errors/skips** (3m 38s). Normal JAR
assembly succeeded. `git diff --check` and active-document relative-link validation
passed. Historical archive files are unchanged. The final rerun includes the
synchronous start-callback and eventless world-change regressions. Gradle reported
its existing deprecation notice; there were no build errors.

The synthetic replays are original Java event sequences in `NexCaptureTest` and
`NexObserverTest`; no private account/kill dump is committed. They cover multi-switch
state, phase/minion scope, morph versus replaced spawn identity, equal distinct and
duplicate hit callbacks, excluded healing/unknown hits, missing/late/out-of-order
signals, mid-kill enable, death/teleport, hard bounds and clear/reset behavior.
Session/plugin tests exercise several one-kill bank trips, both travel directions,
account switch during pending banking, world change without a state event, unknown
identity, failed IO, schema migration and stale queued methods/Reset. Retained
arithmetic, unknown-state, incompatible-package and old worker regressions remain.
Old homepage UI-action assertions in `PreferenceLifecycleTest` were migrated to
native method/lock/account actions; independent reference/pace lifetime tests remain.

The canonical complete pure replay captures **1 / 1 eligible attempts**, 30 / 30
pre-death ticks, 27 local / 87 classified observed damage, and 18.0 seconds from
its explicit tick 0→30 origin. The public-adapter replay starts at tick 1 and ends
at 30, hence **17.4 seconds**. These are intentionally distinct origins, not a
comparison with the game's timer. Deliberately incomplete/death fixtures do not
enter complete averages. The two-share fixture independently verifies mean of
per-kill percentages versus summed-damage share. These fractions describe fixtures,
not expected live capture completeness.

`NexPanelTest` produces normal-width 225px synthetic native captures at 12pt and
20pt under `build/validation/n1/` for Encounter, Setup and details. Wrapping/height,
frozen details, assumption labels and identity clearing are checked. Their null
ItemManager fallback is not real sprite/client validation. Runtime uses genuine
ItemManager images with stale-view guards. No authenticated RuneLite session was
available here, so no live-client screenshot or player endorsement is claimed.

## Pending real-client and player findings

**Real kills observed: 0. Real eligible/captured fraction: unavailable (0/0).**
No claims of game-timer agreement, official contribution accuracy, actual supplied
gear/stock, live switch accuracy or Petter usefulness are made.

1. In the development client, enable before a fresh Nex spawn; run actual Trio MP2
   with Lightbearer and upgraded Death Charge. Check actual melee/ranged/Voidwaker
   switches, P2 ranged windows, NPC transformations and four minions against the
   frozen timeline. Report total encountered/captured/eligible attempts, complete
   tick coverage and each exclusion reason; do not report only successful cases.
2. Compare the recorded barrier/death ticks with the timer's actual origin/end
   events. Review smoke/chat/overhead ordering and death/barrier correlation before
   adjusting bounds. Replay anonymized event sequences from actual findings using
   synthetic IDs; do not commit names, account/bank dumps or private raw traces.
3. Observe Death Charge 2→1→0 and special energy/ammo/quiver changes around real
   casts, procs and switches. Verify temporary quiver counters' freshness/meaning.
   Unknown internal charges and ambiguous resource causes must remain unknown.
4. Complete several one-kill bank trips in both directions. Include teleport/death
   during active capture, logout, profile/account switch while banking or preference
   work is pending, unsupported world, Reset and disable. Check that prior complete
   summaries survive only valid same-account trips and no stale data resurfaces.
5. Check native 225px layout, enlarged fonts, actual ItemManager sprites, keyboard
   actions, Back/scroll and quiet post-kill updates. Have Petter assess whether
   scopes, sample counts, exclusions and the observed setup are useful and clear.

Keep actual findings, sample counts and omissions separate from synthetic results.
Reassess this exact encounter feature against current Plugin Hub policy before any
future distribution. This work does not publish, merge or seek Hub submission.
