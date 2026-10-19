# N3 decision support

19 October 2026. Development build **0.24.0**, for review, from consolidated main
`7f68d0e`. Runtime `nex-declared-plan-3`, package bridge `nex-package-5`, launch
model `nex-spec-damage-3`, resources `nex-spec-resources-2`, supplies
`nex-supplies-1`, presentation projection `nex-plan-support-1`, knowledge
`n3-decision-1`. Catalogue schema 14 and preference schema 4 are unchanged.

## Capture-to-scenario contract

This is a post-kill conditional comparison, never a reconstructed actual attack
plan or an automatic spec recommendation. The existing worker freezes selected
captured/N2 packages and planning inputs. Those packages enter the same complete
actual → owned → hypothetical calculations, with exact forms, hands/projectiles,
locks, shared opportunities/delays, stock, packing and displaced ordinary attacks.
Negative and zero gains remain visible; no cross-state/global ranking is added.

| Input | Captured evidence | Required scenario declaration |
|---|---|---|
| Equipment, levels, prayers/style | Frozen public-state reading | Reusing that state throughout the chosen window |
| Target/phase/window | Recorded target class and adapter phase/window | Legal vulnerable access at every listed opportunity; `UNKNOWN` stays unknown |
| Attack offsets | No identified launches | Explicit legal offsets; equipment at an impact or worn duration is insufficient |
| HP | Player reading may seed the editor; exact per-launch target HP is absent | Positive per-launch target/player HP and hard cap as applicable; no expected-damage feedback |
| Spec resources | Raw sampled readings/net changes | Initial attack/spell readiness, no active DC, fixed Lightbearer, restoration phase/order |
| Death Charge | Raw active counter and upgrade planning fact | Legal casts, distinct eligible deaths, runes/spell opportunities; no inferred cast/proc count |
| Gear/ammo/splinters | Exact frozen inventory/worn/raw-counter readings | Usability, exact selected stock and joint packing; bank presence alone fails |
| Supplies | Frozen item quantities and visible levels | Explicit consumption ticks, eating/drinking readiness, usable stock and only the declared level effects |
| Costs | No market query | GP per selected projectile/splinter/food or individual potion dose |

Reports retain a fixed allowlist of automatic-evidence requirements. Declaring a
hypothetical input does not upgrade its origin to observed. Missing/unsupported
damage preserves independent finite resources; unknown readiness or stock prevents
the finite plan. Capture does not establish these automatic gates, so dependent
automatic personal recommendations remain unavailable. No new attribution,
coaching, HP trajectory, survival, contribution or N4 team-time output is enabled.

## Target mechanics

Four small target records were independently added to the existing catalogue.
Each has Defence 200, Magic 200, size 1, Magic defence 150 and Magic attack bonus 0.
Fumus stab/slash/crush defence is 25/100/100; Umbra 100/100/100;
Cruor 100/25/100; Glacies 100/100/25. Light/standard/heavy ranged defence is
50/50/50 except Umbra's 25/25/25. All four have non-Magic flat armour -2.
No whole monster dataset is shipped.

The plan requires the corresponding Smoke/Shadow/Blood/Ice phase and an explicitly
declared vulnerable base window. The exact `MINION_<target>` adapter window maps
to that declaration; unknown/mismatched windows withhold damage. A declared legal
window is not proof the minion was actually attackable at a captured tick.
Whole-package metrics use that minion's own defence, Magic and size. Accurate
non-Magic launch outcomes receive +2 before supplied HP/hard caps; misses remain
zero. Voidwaker's Magic-type special bypasses that adjustment, while its ordinary
melee attack receives it. This transform belongs to the launch distribution;
ordinary model metrics alone do not include flat armour.

Zaros overheads, dynamic target-stat transfers and other special windows remain
scoped damage gaps. Earlier generic references to a “Zaros soft cap” did not
establish an OSRS rule: indexed OSRS evidence describes overhead/stat effects,
while similarly named RS3 content has different mechanics. No RS3 cap is imported.
Personal comparisons depending on those effects remain disabled.

## Finite declared supplies

The optional, initially closed Food & potions editor supports only these exact
normal-world item families:

| Unit | Exact IDs (1 through 4 doses for potions) | Reviewed effect used |
|---|---|---|
| Shark | 385 | +3 ticks to an outstanding weapon cooldown |
| Cooked karambwan | 3144 | +2 ticks; separate combo admission, with the resulting normal-food cooldown |
| Saradomin brew dose | 6691, 6689, 6687, 6685 | Attack/Strength/Ranged/Magic each lose `floor(current/10)+2`; Defence gains `floor(base*20/100)+2` up to the normal brew ceiling |
| Super restore dose | 3030, 3028, 3026, 3024 | Drained combat stats gain `floor(base/4)+8`, capped at base; existing boosts stay unchanged |
| Super combat dose | 12701, 12699, 12697, 12695 | Attack/Strength/Defence gain `floor(base*15/100)+5`, capped at their potion ceilings |
| Ranging dose | 173, 171, 169, 2444 | Ranged gains `floor(base/10)+4`, capped at its potion ceiling |

The separate supply checkbox starts unchecked, including when its subsection is
closed. Nonempty use lists cannot produce a finite plan until explicitly confirmed.
Stock is pooled in dose units only within the exact reviewed family. Unknown
inventory or insufficient doses blocks the plan, without inventing supplies or
releasing their reserved packing slots. No pickups, decanting, drops, potion-mix,
league, prayer-item or internal-charge behaviour is assumed.

Normal food, potion, karambwan is the declared same-tick order, followed by the
existing cast/death/attack ordering. Normal food, combo food and potion admission
have bounded separate timers; drinks require at least three ticks between doses.
Food adds its delay to an outstanding weapon cooldown. Idle eating creates no
invented lost attack. Same-tick shark/karambwan adds both penalties to an outstanding
cooldown. Rejected attacks still spend no ammo/energy. Shared events affect each
complete scenario's own cooldown, so different weapon delays displace attacks
independently under the same declaration.

Each opportunity recomputes offensive metrics from the declared brew/restore/
reboost level sequence. Unknown unused skills stay unknown without suppressing
an independently supported combat family; a missing affected base level withholds
that restored/boosted skill rather than preserving a stale known value. Other drains/boosts and natural level decay are excluded
by explicit assumption. Player HP stays separately supplied for every launch;
food/brew healing, incoming damage, prayer restoration/drain and survival are
not simulated or optimized. No consumption is attributed from inventory loss.

## Cost and acquisition presentation

Unit costs are optional bounded whole GP amounts, scoped to this declared plan.
Counts include only admitted ammo, the specified splinter branch and the shared
finite declared supply uses. Potion cost keys denote **one dose**, not one full
bottle. Missing prices keep the scoped GP total unavailable; an explicitly entered
zero is distinct. Computation happens on the worker and is frozen with the result.
This excludes other gear upkeep, Death Charge runes, acquisition and whole-trip
cost. No prices are fetched, persisted or presented as current market quotations.
A resource branch's cost is not expected average random consumption.

Acquisition routes match exact changed hypothetical item IDs in the retained
catalogue. No generic variant normalization creates a route. Reviewed route work
and stages remain unassessed facts: no today's bank read, completed-component
assertion, item ETA, recipient odds, pace or grind ranking is inferred. Unsupported
exact items show Route unavailable. Existing permanent-unlock requirements never
become recurring ammunition, splinter or dose costs.

## Source and rights review

| Reference | Review and retrieval |
|---|---|
| [Pinned monster facts](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/cdn/json/monsters.json) | Fetched 19 October; SHA-256 `ec38284c4651cf76a568b5e95cd11cc942e2077d901d20041f770e56460dbca2`. Independently recorded four bounded profiles only. |
| [Pinned launch reference](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/PlayerVsNPCCalc.ts) | Fetched; retained SHA-256 `6c0b467e61fe85ed5cd752991d4a072b695431809cf0d4616d02946e95929801`. Cross-check: flat armour applies to accurate non-Magic damage. No code copied. |
| [Fumus](https://oldschool.runescape.wiki/w/Fumus), [Umbra](https://oldschool.runescape.wiki/w/Umbra), [indexed Fumus](https://osrsindex.com/wiki/fumus?site=osrs_wiki), [indexed Umbra](https://osrsindex.com/wiki/umbra?site=osrs_wiki) | Indexed material independently corroborates vulnerability and -2 armour. Full Wiki/mirror fetches returned 403; indexed content is not a fresh server test. |
| [OSRS Nex strategy evidence](https://osrsindex.com/wiki/nex-strategies?site=osrs_wiki) | Indexed overhead/stat descriptions inspected. Full fetch failed; this does not establish exact Zaros target stats or enable that phase. |
| [Food/Fast foods](https://oldschool.runescape.wiki/w/Food/Fast_foods), [indexed food mechanics](https://osrsindex.com/wiki/food-fast-foods?site=osrs_wiki), [Potions](https://osrsindex.com/wiki/potions?site=osrs_wiki) | Indexed waiting-weapon, food/combo and separate three-tick drinking facts; full retrieval blocked. Existing K12 review remains historical. |
| [Indexed official food-delay post copy](https://oldschool.runescape.wiki/w/Update%3AThe_Ruinous_Powers_-_Updated_Greenlight_Poll_Blog) | Original action/eat-delay explanation corroborates additive 3+2 combo delay; proposed prayer effects are not implemented. |
| [Indexed brew](https://osrsindex.com/wiki/saradomin-brew?site=osrs_wiki), [indexed restore](https://osrsindex.com/wiki/super-restore?site=osrs_wiki) | Current-level brew drain and base-level restore ceiling cross-checked. No generic 3:1 ratio is assumed. |
| [RuneLite item-stat facts](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-client/src/main/java/net/runelite/client/plugins/itemstats/ItemStatChanges.java) | Fetched; SHA-256 `4874489bffc9a0a24d419b6da082196f6d9af0c6f1d5854ddcdaffdd6c63a33d`. Bounded dose/boost cross-check, not a client integration. |
| [Brew reference](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-client/src/main/java/net/runelite/client/plugins/itemstats/potions/SaradominBrew.java), [restore reference](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-client/src/main/java/net/runelite/client/plugins/itemstats/potions/SuperRestore.java) | Fetched; hashes `210233e847163a53fbe0f266c2e43c83acfed5c9ceeb50990be4096621f48820` and `7b12fda591452c91163e0bae0260782535fb9ddaf748b35ce4777620f1c4888f`. Current-stat drain and restoration ceiling cross-checks. |
| [Official exact item identifiers](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-api/src/main/java/net/runelite/api/gameval/ItemID.java) | Fetched; SHA-256 `6f00480cae7939de1f5187dda406e7a2029f4280bf2b395b789619847587221d`. All supply/dose and splinter identifiers checked. |

Algorithms, tests, bounded factual records and prose are independently authored.
No calculator implementation, bulk dataset, source prose or images are imported.
Reference availability does not authorize GPL code/dataset redistribution in BSD
work. Existing licence and Jagex/artwork notices remain intact. Retrieval and
synthetic tests do not establish current server agreement or live usefulness.

## Lifecycle and reports

Supply/cost edits invalidate pending requests/results immediately. New kills and
later account reads cannot change an inspected plan. Existing session, visibility,
view/request, Reset/logout/world/profile and eviction guards apply unchanged.
No supply plan, price or performance history is persisted.

Standalone plan report schema 3 projects fixed supply enums/ticks, declared unit
costs, scoped counts/cost gaps, static catalogue route facts/source IDs and existing
frozen package/calculation inputs. It remains bounded to 24,000 characters, with a
truncation notice and selectable clipboard fallback. No raw containers, account
names/IDs, roster/cohort fingerprints, raw traces or client item names are added.
Observed Diagnostics remains additive schema 2 and lists the automatic evidence
gates. [Verification and remaining acceptance](../validation/nex-decision-support.md).
