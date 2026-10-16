# N3c: complete-package conditional comparisons

16 October 2026. Build 0.19.0; bridge `nex-package-2`, knowledge `n3-package-1`,
catalogue schema 14. Reuses `melee-2`, `ranged-2`, `nex-spec-resources-1` and
`nex-spec-plan-1`. `nex-spec-damage-2` permits inapplicable player HP for diamond
bolts; ruby still requires known player HP. Numerical damage rules are unchanged.
[TASKS](../../TASKS.md) is the active backlog; [verification](../validation/nex-packages.md)
records actual automated results and the remaining client boundary.

## Construction and scope

`NexPackageFactory` is shared with N2. It builds a complete `CombatSetup` from
exact slots, the explicit style button, active prayers and selected quiver
projectile. N2 keeps its existing observation, ownership/search and context
checks. N3c does not infer these inputs from worn duration, impacts or energy
deltas. It adds no runtime capture caller, new UI, persistence or setup wizard.

`NexPackageComparison` builds immutable scenario inputs, derives the ordinary
attack rolls, damage bounds and delays, then invokes the existing N3b damage
and finite-resource kernels. Voidwaker uses its own melee package and ordinary
max; a Fang max is never reused for it. Fang's independently computed lower
bound is now exposed alongside its existing upper bound and correlated accuracy.
Multi-hit Scythe, Soulreaper stack distributions, seeking-arrow floors and
weapon-internal charge/dart ledgers are outside this bridge's damage support.
They have named gaps rather than a generic uniform-hit substitution.

Complete ranged sets are evaluated by the existing ranged model. Exact hands,
projectile tiers, physical ammo versus selected quiver ammo and fixed Lightbearer
are checked. Charged/blessed and depleted quiver offence is recomputed using
separate complete cape packages. These calculation branches neither change the
recorded physical cape nor imply ownership of its uncharged or blessed form.
Compatible worn ammunition takes priority over different ammunition in the
quiver, as documented in the [quiver mechanics](https://oldschool.runescape.wiki/w/Quivere).
An explicit quiver selection conflicting with that priority is rejected with a
package gap; it is never silently substituted into the recorded package.

## Explicit input contract

`ScenarioFact` distinguishes **observed**, **assumed** and **unknown**. Known
facts require source, scope and time. Unknown facts retain their reason/scope and
cannot be read as values. Synthetic fixtures use the assumed origin. Collection
inputs are copied by domain constructors; `CombatState` already freezes its maps
and sets. Results retain the original conditions, scenario facts, package
provenance, source revisions and all participating model versions.

| Input | Contract and dependency |
|---|---|
| Encounter | One immutable `Conditions` declaration shared by compared evaluations: scope, exclusive horizon, ordered steps and fixed target/player HP at each opportunity. Identity equality prevents accidental comparison across different declarations. |
| Target/phase/window | Explicit fact. `NEX` Smoke/Shadow/Blood/Ice with a declared base-stat window is the same bounded ordinary arithmetic scenario as N2. It is not proof of live phase throughput. Zaros, minions and special windows withhold damage; MP2 Embrace darkness rejects melee launches. |
| Launch opportunity | Explicit legal availability per attempted step. Missing/false availability prevents a finite plan; an omitted action is an explicitly idle opportunity. Wearing a setup does not create a launch. |
| Target HP / hard cap | Known positive values required for dependent damage. The supplied hard cap is an explicit reviewed/assumed scenario condition, never an inferred Nex soft cap. Expected damage is never fed back into the next HP input. |
| Player HP / bolt eligibility | Required for ruby damage and self-cost, including low-HP suppression. Diamond and ordinary melee/arrows do not depend on player HP or ruby eligibility. Hard Kandarin is explicit for bolt procs; unknown is not no diary. |
| Initial resources | Known energy, fixed-Lightbearer restoration phase/order, and explicit attack-ready/no-active-charge/cast-ready start. Other initial spell/readiness states are outside N3a's start contract, not silently reset. |
| Death Charge | Explicit casts matching the selected method upgrade state; distinct eligible death keys and eligibility facts. False eligibility produces no proc event. Unknown eligibility/cast state blocks resource accounting. No proc is inferred from an NPC death or sampled delta. |
| Package | Complete exact slots and role, real equip requirements/unlocks, visible levels, active prayers, style button and projectile selection. Proposed locks constrain suggestions; actually-used history remains immutable. At each shared attempted attack step, comparisons retain all levels and, within a combat family, active prayers/style. Matching uses the common step, not scenario-local package names; time-varying states remain attached to their own opportunities. |
| Usable stock | Exact equipment and selected projectile quantities shared across all roles. Presence in a bank or a worn/counter ID is not an automatic quantity/charge assertion. Proposed stock must stay the same; explicitly hypothetical exact items may be added only to the upgrade scenario. |
| Joint packing | Gear/projectile inventory space reserved after supplies and unselected items, checked for every role's stowed union. Bank preparation also requires an explicit validated joint-packing fact and usable quantities; its availability remains conditional next-trip preparation. |
| Recovery / quiver | Active recovery fact, including applied upgrade and body/obstruction restrictions; its device/cape must be compatible. Saved/lost projectile and spent/retained splinter draws are explicit conditional branch inputs. Charged quivers require exact splinter stock; blessed quivers consume none. |

Only selected projectile pools enter the finite launch ledger. An unused unknown
ammo pool does not block a melee plan or become an empty account inventory.
Similarly, zero splinter **consumption** without a charged quiver does not establish
physical stock. `Evaluation.remainingSplinters` stays null when the supplied pool
was unknown. The low-level ledger's unused zero allocation is not an observation.

Named damage gaps preserve independently supported package/launch details and
energy/ammo accounting. Missing energy, availability or usable stock prevents the
finite plan while retaining independently derived launch inputs. Rejected attacks
still spend nothing. Event-index-to-step mapping preserves identity through idle
opportunities, ineligible deaths and equal-tick casts/attacks.

## Comparison meaning

The three-scenario API keeps actually used -> supported owned alternative separate
from owned -> owned plus hypothetical upgrade. An actually-used plan has carried
availability; the marginal upgrade retains the owned baseline's availability
scope. No search or global-best claim is made. A supported explicit pair remains
available when another alternative is unsupported.

Damage deltas subtract complete conditional plans, including displaced ordinary
attacks, finite energy, shared exact ammo, charge exhaustion and all applied
resource events. Identical plans give zero; supported worse packages retain
negative deltas. Package changes, energy/spec/ordinary counts, projectile and
splinter consumption deltas stay separate. Unsupported totals are null with named
gaps, not zero. Resource-branch probabilities remain attached to each plan; neither
a single branch nor a difference of its expectations is an unconditional forecast.

This API does not model a full Nex HP trajectory, extra phase transforms, minion
armour, siphon/healing, survival, acquisition paths, cost presentation, automatic
best-spec ranking, personal contribution or team seconds. No capture or game
agreement is accepted from synthetic tests. See the [N2](nex-owned.md),
[N3a](nex-spec-resources.md) and [N3b](nex-spec-damage.md) boundaries.

## Added bounded equipment evidence

The existing revision-pinned
[equipment reference](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/cdn/json/equipment.json)
was fetched on 16 October. SHA-256:
`96022a4a1764cc9d7d3d0a73f9434563e5f727e7c7f85eb1a79ab791dd4d678d`.
Only five bounded factual entries were independently recorded in the existing
catalogue; the source register entry is `n3-package-equipment`.

| Exact item | Independently recorded offence facts |
|---|---|
| Voidwaker 27690 | Stab/slash/crush 70/80/-2, strength 80, 4 ticks, one hand, 75 Attack. Slash-sword buttons: slash accurate/aggressive, stab controlled, slash defensive. |
| Ruby bolts (e) 9242 | Ranged strength 103, adamant-tier bolt compatibility, ruby effect. |
| Diamond bolts (e) 9243 | Ranged strength 105, adamant-tier bolt compatibility, diamond effect. |
| Ruby dragon bolts (e) 21944 | Ranged strength 122, dragon-tier bolt compatibility, ruby effect. |
| Diamond dragon bolts (e) 21946 | Ranged strength 122, dragon-tier bolt compatibility, diamond effect. |

The exact five identifiers were also checked against the existing pinned
[official RuneLite ItemID source](https://github.com/runelite/runelite/blob/42a6f17a6a2e8e478aa763890ecd0181a59dad38/runelite-api/src/main/java/net/runelite/api/gameval/ItemID.java),
SHA-256 `6f00480cae7939de1f5187dda406e7a2029f4280bf2b395b789619847587221d`.
Existing ranged projectile compatibility facts and slash-sword style evidence
are reused. Indexed Wiki [Attack requirements](https://oldschool.runescape.wiki/w/Atrack)
and [weapon styles](https://oldschool.runescape.wiki/w/Weapon_types) were inspected;
the full Voidwaker page was blocked. The older proposed rework requirements are
not treated as current equip requirements. These are bounded factual cross-checks,
not a fresh full game-mechanics verification. N3b's bolt/spec rules and their
retrieval limitations remain documented separately.

All Java, tests and prose are original. No reference implementation, generated
dataset, article prose, images or sprites were copied or bundled. Existing
BSD licence and third-party notices remain. Public reference availability does
not grant permission to redistribute GPL code or Wiki datasets.
