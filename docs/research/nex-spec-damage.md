# N3b: conditional spec damage and finite ammunition

Reviewed 16 October 2026. Build 0.18.0, based on combined main `dc8c5f4` (0.17.1).
Independent models `nex-spec-damage-1` and `nex-spec-plan-1` reuse the N3a
`nex-spec-resources-1` energy/delay rules. [TASKS](../../TASKS.md) owns remaining N3
work. This delivery implements the mechanics API; it does not accept live Nex
capture, reconstruct attacks from energy deltas or recommend a spec upgrade.

## Evidence and rights

| Source | Reviewed facts and limitations |
|---|---|
| [Voidwaker Wiki](https://oldschool.runescape.wiki/w/Voidwaker), indexed [special-attack reference](https://osrsindex.com/wiki/special-attacks?site=osrs_wiki), pinned [development reference](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/PlayerVsNPCCalc.ts) | Guaranteed Magic-type special based on its own melee package max. Inclusive interval from half the ordinary max, rounded down, to the ordinary max plus that half. Costs/delays retain N3a evidence. Wiki full-page retrieval was blocked; indexed material and the pinned reference were cross-checked. No general immunity or encounter transform is established. |
| [ZCB Wiki](https://oldschool.runescape.wiki/w/Zaryte_crossbow), [indexed page](https://osrsindex.com/wiki/zaryte-crossbow?site=osrs_wiki), pinned [bolt reference](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/dists/bolts.ts) | Evoke doubles the attack roll, guarantees eligible ruby/diamond effects on a successful roll, and retains a natural proc opportunity after a miss. ZCB's passive changes ruby to 22%/110 and diamond max to 126%, rounded down. The indexed page was opened; the two pinned development files were fetched. These are PvM mechanics, not an always-110-damage claim. |
| [Ruby Wiki](https://oldschool.runescape.wiki/w/Ruby_bolts_(e)), [indexed page](https://osrsindex.com/wiki/ruby-bolts-e?site=osrs_wiki), same pinned references | Natural PvM rate 6%, or 6.6% with hard Kandarin. It replaces normal damage with 20% current target HP, rounded down/capped at 100; ZCB uses 22%/110. Self-cost is floor(current player HP/10). The pinned reference checks at least 10 player HP; lower HP suppresses the effect. Eligibility is additionally explicit. Full mirror requests failed; indexed passages and the pinned arithmetic were reviewed. |
| [Diamond Wiki](https://oldschool.runescape.wiki/w/Diamond_bolts_(e)), [indexed page](https://osrsindex.com/wiki/diamond-bolts-e?site=osrs_wiki), same pinned bolt reference | Natural PvM rate 10%, or 11% with hard Kandarin; proc bypasses accuracy and uses floor(115% normal max), or floor(126%) for ZCB. Successful-zero handling reuses the ordinary family contract. |
| [Quiver Wiki](https://oldschool.runescape.wiki/w/Dizana%27s_quiver), [indexed charging reference](https://oldschool.runescape.wiki/w/Quivere), retained N2 equipment facts | Eligible arrows/bolts get +10 accuracy/+1 strength while charged; a separate 1/3 roll consumes one splinter. Blessed has the same eligible offence without recurring splinter use. The indexed charging passage was reviewed; fresh full-page retrieval failed. Bowfa, blowpipe darts, weapon charges and venator exceptions are outside this ledger. |
| [Ava assembler Wiki](https://oldschool.runescape.wiki/w/Ava%27s_assembler), [indexed recovery table](https://oldschool.runescape.wiki/w/Ramged_training) | Active assembler recovery 80%, accumulator 72%. The caller must establish actual recovery operation, including body/obstruction restrictions and the quiver's applied upgrade. Quiver ownership alone does not enable recovery. No ground pickups are assumed during the plan. |

Development-reference SHA-256 values at the pinned revision:

- `PlayerVsNPCCalc.ts`: `6c0b467e61fe85ed5cd752991d4a072b695431809cf0d4616d02946e95929801`.
- `dists/bolts.ts`: `258225ffe2e8e9d82adc1924b40df737e75489ef30b7d071dbe51b80b7d47e53`.

Only bounded game facts were used. Java algorithms, tests and prose are original;
no GPL implementation, reference dataset, guide text or images are imported or
bundled. Existing notices and catalogue source registers remain intact. Indexed
facts and a static development reference do not establish current server agreement.

## Single-launch damage

`SpecAttackModel` returns immutable discrete outcomes, including proc status,
target damage, ruby self-damage, probabilities and the supplied package/target
basis. Its attack/projectile types must match the resource launch.

- Ordinary inputs are the complete package's reviewed damage bounds and hit
  chance. A successful zero becomes one when max is positive; misses remain zero.
  A zero max retains the existing family models' zero result. Fang callers must
  supply its own interval and correlated-roll accuracy, not a generic sword roll.
- Voidwaker uses **its own package's** ordinary melee max, not the worn main
  weapon's max, Magic level or Magic damage bonus. A max of 51 gives 25–76 and
  mean 50.5 before target limits.
- Crossbow max/roll come from the whole compatible ranged package, the exact
  selected projectile and target heavy-ranged defence. Double the ZCB spec's
  attack roll before the accuracy calculation; do not double the probability.
- With natural proc probability `p` and doubled-roll accuracy `a`, eligible Evoke
  has effect probability `a + (1-a)*p`. Its other outcome is a miss. Ordinary
  ruby/diamond attacks mix natural effects with ordinary hits/misses instead.
  When ruby is ineligible, Evoke retains doubled accuracy for the ordinary hit.
- Ruby uses current HP separately at every supplied launch. The caller establishes
  target susceptibility. HP costs are separate from offensive damage. Diamond's
  proc has its own rounded uniform damage range, not ruby's fixed HP hit.
- Each outcome is clipped to supplied remaining target HP and an explicitly
  reviewed **hard** cap. This is not Nex Zaros soft-cap reduction, minion armour,
  siphon healing, reflection, invulnerability or a phase-transition model. A caller
  lacking those effects must supply a named damage gap for that launch.

Known numeric input bounds, provenance, HP and supported crossbow modes are
required. Missing state is never silently treated as zero, no diary, no cap or
ordinary full-health Nex. These booleans/rolls are explicit scenario assumptions,
not defaults derived from unread client state.

## Finite launch plans

`SpecAttackPlan` joins one indexed launch to every attack event in a N3a plan.
Casts, eligible deaths, initial spell/attack readiness, fixed ring, restore phase,
equal-tick order and exclusive horizon retain the [N3a contract](nex-spec-resources.md).
An index identifies equal-tick attempts/fallbacks without matching labels or times.

Exact usable ammo quantities are keyed by physical item ID and shared across all
roles. Missing stock is an invalid plan, not empty stock. Zero stock rejects that
attack as `NO_AMMUNITION` before spending energy or setting attack delay. The caller
may supply an explicit fallback event. Delay/energy rejections consume no ammo,
splinter or random branch and contribute no damage. A saved final projectile can
be reused; losing it prevents a later shot. This is finite integer accounting,
not an average fraction of a projectile spent from an assumed infinite stack.

Each projectile launch supplies active retrieval and a saved/lost outcome; each
charged quiver launch supplies a spent/retained splinter outcome. These describe
conditional resource branches, not observed consumption or predictions of future
random rolls. Only actual accepted draws contribute to the branch probability.
Branches for unattempted shots, empty charge or blessed quivers must not be counted
again when enumerating alternatives. The ledger assumes independent retrieval and
quiver-consumption rolls, separate from bolt outcomes, and no mid-plan pickups,
recharging or weapon-internal charges.
The natural log of the branch probability is retained when a long rare branch's
ordinary double-precision probability underflows; underflow is not impossibility.

Charge availability is checked before the shot. Spending the last splinter still
permits that shot's bonus; the next shot uses the complete uncharged package.
Charged and uncharged damage inputs are separately computed packages, avoiding a
fixed quiver DPS uplift. Blessed always selects the charged offence branch and
uses no splinters. The caller still validates exact ammo compatibility, hands,
stock location, applied retrieval upgrade and every proposed package.

Results preserve energy/restoration/cap waste, accepted attack counts, exact
remaining/spent projectile stocks, splinters, per-launch damage and scoped gaps.
An accepted unsupported damage launch withholds the damage total while preserving
supported launch details and resource accounting. No damage model is demanded of
a rejected attack. Immutable plans/results retain N3a's 3000-tick/2048-event bounds.

## Numerical and product boundary

The damage total is a sum of **conditional single-launch expectations** at supplied
HP/windows, under the chosen resource branch. It is not a stochastic HP trajectory,
counterfactual last kill, survival result, personal contribution or team-time model.
Never subtract expected damage/self-cost to manufacture the next actual target or
player HP. Comparing complete plans requires the same declared encounter conditions
and horizon, including all ordinary attacks displaced by specs and explicit stock
outcomes. Gains from independent upgrades are not additive.

These kernels have no automatic runtime caller. Post-kill resource readings stay
frozen evidence with no inferred cast/spec count; no setup wizard or live coaching
is added. Connecting observed attack opportunities, reviewed complete-package
construction, phase transforms, acquisition/recurring-cost presentation and supply
effects remains N3 work. N1/N2 client acceptance and N4 contribution/time validation
remain open. [Actual verification](../validation/nex-spec-damage.md).
