# N2 owned Nex equipment: implementation for review

15 October 2026. Branch `n2/nex-owned-advice`, stacked on N1 commit
`2e313370c51c49e45d5d290bbd209f1b31e2253c`. Plugin **0.16.0**, knowledge
**n2-owned-1**, catalogue schema **14**, preference schema **3**. Models
**nex-owned-1 / ranged-2 / melee-2 / powered-magic-3**.

## Result and scope

The native encounter view compares each captured state against complete supported
carried alternatives, with separate conditional bank-preparation scenarios.
Locks constrain proposals; actual history stays immutable. Explicit pairs expose
gains/losses/zero, and partial/unsupported searches cannot claim global best.
The selected quiver projectile remains separate from the physical worn blessing.
Captured occupied inventory slots protect extra hand-switch space.

All ordinary offence uses the [specified base-stat scenario and evidence](../research/nex-owned.md).
It is neither measured DPS nor expected kill duration. Spec/unknown states, minions,
Zaros, special windows, enchanted bolts and unobserved resources retain scoped
gaps. Contribution, healing, finite spec/ammo/splinter plans and team seconds
remain N3–N4. No acquisition-time or Plugin Hub publication claim is made.

Worker publication checks identity, generation, account revision/snapshot, Nex
revision and active/suspended capture. New kills keep the inspected summary and
pair frozen; lifecycle changes clear it. Preference schema 3 and its earlier
migrations are unchanged. Raw traces, account data and results remain memory-only.

## Account fixture provenance

Petter supplied the equipment in this chat. The anonymous `NexOwnedFixtures`
records these reported items:

| Role | Reported equipment |
|---|---|
| Ranged | Fortified Masori mask/chaps, anguish, non-blessed quiver, Ancient blessing, crystal body, Barrows gloves, max Avernic treads, Lightbearer, Tbow, amethyst arrows |
| Melee switches | Faceguard, rancour, Bandos chestplate/tassets, Fang, dragon defender, ferocious gloves; max treads/Lightbearer/blessing retained |

Keeping quiver for melee is an explicit fixture assumption from the switch list.
Real levels, prayers, precise crystal/charged variants, stock, inventory occupancy,
phase windows and ticks were **not** supplied. The fixture uses synthetic values
and must not be called a real captured account. Dragon arrows and blood fury are
separate candidate cases, not invented observed ownership. Prior N1 context reports
Voidwaker; no actual spec package or energy timeline has been manufactured here.
No character identifier, private account dump or performance history is committed.

Independent hand checks for the synthetic ranged scenario give attack bonus 210,
strength bonus 91, max hit 72, attack roll 53,303 and defence roll 68,326.
At visible Ranged 112 with synthetic Rigour/rapid this is about 4.68 ordinary
theoretical DPS. These inputs are demonstration evidence, not Petter's observed
stats or measured Nex output.

## Verification

Final verification: JDK 11 / RuneLite 1.13.2,
`XDG_CACHE_HOME=/tmp/ironpath-cache ./gradlew test build --no-daemon` **passed:
165 tests across 27 suites, zero failures/errors/skips** (31 s). Normal JAR
assembly succeeded. This includes the final selected-projectile source dependency,
partial-attempt worker revision and frozen selected-pair regressions. Existing
Gradle/deprecated API compiler notices remain; no build errors. Whitespace and
active-document relative links pass; historical archives are unchanged.

Meaningful new regressions cover reported complete switches/blessing/quiver,
unchanged packages, positive dragon-arrow and negative blood-fury pairs, charged/
blessed ordinary equivalence, locks without manufactured ownership, separate bank
availability, unknown ammo/charges/slots, hands/sets, full-inventory switching,
unsupported competitors, exact visible level decay and per-state spec/phase gaps.
Mocked delayed-worker tests reject old profile/account revisions and re-evaluate
fresh snapshots. Native 225px captures at 12pt and 20pt exercise main, owned detail,
expanded basis and negative pairs, frozen history and identity clearing.

Generated screenshots are under `build/validation/n2/`. They use synthetic state,
headless Swing and placeholder sprite wells; they are not live RuneLite acceptance.
Committed examples: [225px overview at 12pt](images/nex-owned/main-12.png) and
[negative blood-fury pair at 20pt](images/nex-owned/loss-20.png). These examples
inject synthetic carried dragon arrows/blood fury in addition to the reported
gear; they do not establish actual stock or account performance.

## Retirement and retained evidence

N2 removes the old `IronpathPanel`, recent-boss panel, tile/deck/Browse consumers,
global `Advisor`/ranker/progression presentation and optional Zilyana comparison
worker. Their global-priority/homepage-only tests retire with them. This deliberately
reduces the test count; it is not a like-for-like pass-count improvement.

Mixed tests retain their service-level arithmetic/compatibility checks. Catalog
source/schema/cycle validation, observed slots/quantities, full-loadout/oracle and
hand arithmetic, ranged/melee families, unknown/unusable inputs, acquisition
probability kernels, reference lifecycle, preference IO/migrations, Nex capture,
banking retention and identity isolation remain. `AccountFacts` preserves live-
over-manual precedence without retaining global ranking. Acquisition/relationship
records, DAG definitions, scopes, probability/effort kernels and source versions
remain available for the next encounter delivery. No second catalogue is created.

The old validation reports and `docs/archive` are unchanged. Their previous test
counts and unperformed checks remain historical evidence, not acceptance of N2.

## Remaining acceptance

- Capture real complete ranged/melee/spec states, levels/prayers, worn/quiver ammo,
  charges, inventory and switch occupancy. Verify the public quiver counters and
  exact non-blessed/cosmetic forms in the client.
- Complete N1's multi-switch, phase/minion/completion, timing-origin, capture-
  fraction and several bank-trip checks. N2 does not close those by testing models.
- Verify readable native sprites/DPI, correction locks, frozen details/Back and
  absence of combat coaching. Have Petter assess useful and unchanged results.
- Validate complete next-trip packing/stock before treating a bank scenario as a
  usable joint method plan. A separately supported offence pair is not that proof.
- Keep actual-account fixture acceptance open until real levels, resources and
  full inventory are known. No model-based output is represented as that evidence.

These checks remain open. The implementation is reviewable before organizing the
combined N1/N2 Nex session; no real-client/player pass is claimed.


## First client screenshots and 0.16.1 follow-up

Later screenshots provide a real censored attempt and a selected worn melee
sample; they do not validate a complete capture or N2 numeric output. The sampled
melee cape is Infernal cape, correcting the earlier fixture's quiver assumption.
The sample also shows base Prayer 91 and an unworn carried quiver, so its raw zero
worn-charge flag cannot establish depleted quiver stock. The full actual ranged
state and usable charges remain to inspect. No raw inventory/account dump is
committed. See the [separate findings, fix and retest report](nex-live-start.md).

The next client test still lacks start/end boundaries despite complete captured
tick coverage and all phases. N2 live acceptance is still open. Build 0.16.2 adds
event-payload handling and signal diagnostics; see the
[second-test report](nex-live-signals.md). This does not replace the original
0.16.0 validation results above or establish a complete live account fixture.
The original 165-test result above remains the result for N2 commit `bab06ed`.
