# Encounter equipment advice — issue #58

26 October 2026. Development build **0.33.0**, based on main `2018144` and
unmerged PR #57 `ed58691816ad262d720cc2ebca3526b72755c153`. Issue #58 explicitly
requests implementation of the revised design. This delivery does not merge
#57, merge itself, publish to Plugin Hub or promote live mapping support.

## Behavior and calculation

The main role control uses Melee, Ranged and Special attacks for Nex. The current
numerical automatic slice remains provisional ordinary melee with the captured
Fang, style and five-tick tempo fixed. Ranged/spec without supported attack
attribution show No supported attack sample. Per-state comparisons and declared
plans remain behind Base-stat comparisons.

`NexFangAdvice` now uses the complete ordinary damage distribution, including
accuracy and successful-zero conversion from the existing kernel. It does not
clip each roll to remaining HP. Its percentage is the paired sample damage
change divided by sample baseline damage; zero baselines withhold the percentage.
No misses/accuracy/max-hit mechanic is removed. Existing actual mechanic caps and
HP-dependent ruby/spec formulas retain their independent N3 semantics.
The model revision is `nex-fang-advice-3`. Existing gainLower/gainUpper export
fields are equal scalar values for compatibility, with explicit metric/model
fields. Frozen schema-5 exports remain bounded, allowlisted and user-triggered.

Every alternative in a sample now uses that sample's actually worn complete
setup. Hypothetical items no longer silently use the best-owned setup as their
baseline. Complete owned packages are still searched together, with exact variants,
hands, fixed ring, stock and planning locks checked. Duplicate paths to identical
packages are removed within that same sample. Different captured conditions have
an explicit Comparison setup selector; percentages are never summed across roles.

Compact item/package rows show one signed Damage value and a disclosure control.
The default list keeps gains and the requested fortified Masori defensive options.
All alternatives also exposes other zero changes and downgrades;
Add comparison item reveals another already computed supported exact alternative
on the frozen input. Open comparisons identify replacements, max hit, independent
estimate availability and How this was compared. Ownership and acquisition scope
remain internal or in Diagnostics, without normal-screen badges. Partial samples,
unknowns, unsupported phases, incomplete/truncated capture and stale publications
retain their existing guards. An open comparison keeps its frozen kill/result across
a new kill, labels the older comparison and offers Latest kill. Identity/reset,
eviction and revised-input guards still discard inaccessible evidence.

## Candidate evidence and bounded facts

The generic encounter/role/method candidate schema is a suggestion baseline, not
an ownership whitelist. Nex begins with the Ranged and Hybrid tables on
[Nex/Strategies](https://oldschool.runescape.wiki/w/Nex/Strategies), inspected via
indexed search on 26 October. Direct retrieval is blocked by robots.txt; the
recorded revision is honestly `indexed-search-2026-10-26`, not a fabricated Wiki
oldid. Table facts are independently encoded as exact IDs, roles, slots, method,
ammo kind, companion requirements and conditions. Manual additions extend that
set. Actual observed equipment and complete carried-package searches are never
filtered away by the recommendation table. Inclusion does not imply numerical
support; actual combinations and ammo are checked by the existing package kernels.

Eight bounded melee item records extend the same catalogue: fortified Masori body
and chaps, usable new Eclipse Moon helm/body/legs, Mixed hide top/legs and Aranea
boots. Attack/strength facts were independently recorded from the existing
[pinned equipment reference](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/cdn/json/equipment.json).
No implementation, prose, images or bulk dataset is imported. Eclipse is 50
Defence/75 Ranged; Mixed hide legs require 60 Ranged without a Defence requirement;
Aranea boots have no equip requirement. These were cross-checked in indexed
[Moon equipment](https://oldschool.runescape.wiki/w/Moon_equipment),
[Mixed hide armour](https://oldschool.runescape.wiki/w/Mixed_hide_armour) and
[Aranea boots](https://oldschool.runescape.wiki/w/Aranea_boots) on 26 October.
Only the exact recorded forms receive support; broken/degraded forms are not
silently normalized. Eclipse burn requires its weapon and is not added to Fang.
Masori offence carries no invented healing/defence/supply benefit.

Preview sprites are genuine unmodified Jagex artwork from the existing RuneLite
static icon source, with SHA-256, dimensions and retrieval dates in the test
manifest. They stay outside the production plugin JAR. Existing Jagex notices
and the separate artwork terms apply.

## Ironman ordering

`ironman-order-1` reuses reviewed catalogue routes, acquisition path predicates,
method profiles, unlocks, skills and observed components. An already observed
item has no acquisition work. Confirmed acquisition components can shorten work
to assembly; required quantities and skill gates remain explicit. Missing data
receives an unknown tier, never free acquisition. Failed independent drop rolls
are not an input. The worst remaining tier in a combined package is conservative;
shared allocation/assembly details remain in the source route text.

Positive damage changes precede sidegrades/downgrades. Within that grouping, the
internal ordering is `log2(1 + 100 × max(0, gain)) - 0.5 × remainingWorkTier`.
Available=0, confirmed assembly=1, reviewed short/medium/long/very-long=2–5,
unknown=6 and unmet preparation=7. This monotone policy preserves greater-benefit,
no-more-effort dominance while making a consistent tradeoff for incomparable
options. It is a reviewable initial preference, not an objectively best ranking.
The score, GE prices, hours and gain-per-grind-hour controls are absent from the
UI. Current gain is sample-scoped; no whole-encounter weighting is invented to
rank a partial sample. Defence/supply support cannot silently become zero value.

## Contribution, time and supply honesty

`encounter-impact-1` accepts supported overall personal gain. Unchanged attack
mix can use `g = affected baseline damage share × role gain`; speed-changing
comparisons require rates over equivalent exposure instead of equal attack counts.
A partial provisional Fang sample does not establish that share. Worn ticks,
positive hits and sampled damage ratios are not substitutes for coverage.

For verified same-pool baseline contribution `s` and duration `T`:

```
m = 1 + s*g
newContribution = s*(1+g)/m
newDuration = T/m
secondsSaved = T-newDuration
```

A known fixed portion `F` uses `F + (T-F)/m`. Zero/negative gains, solo share and
invalid/no-finite-throughput cases are handled. Missing duration only withholds
time, while missing contribution or whole-encounter weighting withholds dependent
estimates. Constant teammates/availability, unchanged damage pool/behavior and
phase/wait limitations belong in details. This is a theoretical damage-share
estimate, not loot credit, server attribution or a complete executable plan.

Current captures lack supported whole-encounter weighting and verified contribution.
Consequently the current UI correctly shows Not estimated for contribution and
time. Incoming-damage and consumption models are also unavailable, so Supplies
reads Not estimated. No brew/dose saving is derived from defence stats alone.
The finite spec/DC/ammo/supply planner retains all eight automatic gaps.

## Validation

Targeted model, diagnostics, frozen-history, sprites and native N3 review were
run with JDK 11.0.32.1+1 / Gradle 8.10 / pinned RuneLite 1.13.2.
[PR #59](https://github.com/PetterHL/ironpath/pull/59) records the tested revisions,
actual final counts, compatibility version and current CI/artifact links.

Required full commands:

```sh
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --args=all --no-daemon
./gradlew test build --no-daemon
```

The revised zero fixture keeps the observed defender consistent with its explicit
synthetic planning lock. The original mismatch would require a combined change;
it must not turn a different best-owned baseline into the baseline for a Bandos
row. Unit coverage still exercises exact locked defender and cape forms.

The native N3 scenario generates compact, expanded, positive and zero result
views at 225px width with fonts 12 and 20, plus real 225 × 600 JViewport captures.
These are synthetic Swing renders, not live client screenshots. Current source
changes do not establish new client mappings, whole-kill contribution, supplies
or practical recommendation usefulness. The earlier supplied live reports retain
their original scope; no new kill was requested for this offline implementation.
