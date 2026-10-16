# Nex N3a: finite special-attack resources

Reviewed 16 October 2026. Model `nex-spec-resources-1`, development build 0.17.0.
Based on N2 build 0.16.5 / capture adapter `nex-public-4`. This adds resource
evidence and an independently authored resource scenario kernel. It does not
complete N3's damage, upgrade selection, ammunition or supply models.
Combined build 0.17.1 incorporates N2 0.16.6 / knowledge `n2-owned-2` without
changing these energy/delay rules or capture adapter. The initial implementation
and verification below retain their stated historical baseline.

## Reviewed inputs and sources

| Source | Bounded fact used | Retrieval and limitation |
|---|---|---|
| Existing [Lightbearer S18, revision 15299656](https://oldschool.runescape.wiki/w/Lightbearer?oldid=15299656), cross-checked with indexed [game-tick reference](https://oldschool.runescape.wiki/w/Ticks) | Natural 10% restoration every 50 ticks; fixed Lightbearer every 25 ticks | Existing full-page source register retained; indexed tick reference checked on 16 October. Fresh full Wiki/mirror requests were blocked. Ring-switch timer rules are deliberately excluded. |
| [Pinned equipment reference](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/Equipment.ts), existing [Zaryte S16](https://oldschool.runescape.wiki/w/Zaryte_crossbow?oldid=15293439) and indexed [Voidwaker reference](https://oldschool.runescape.wiki/w/Voidwaker) | Voidwaker costs 50% and has four-tick delay; ZCB costs 75%, six ticks normally or five on rapid | Pinned equipment file fetched and checked for costs; existing catalogue's `r2-zaryte-crossbow` retains six-tick base speed and ordinary rapid modifier. Voidwaker's four-tick reference was checked through indexed material. No spec damage or guaranteed bolt-damage claim. |
| [Pinned RuneLite timers](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-client/src/main/java/net/runelite/client/plugins/timersandbuffs/GameTimer.java), [handler](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-client/src/main/java/net/runelite/client/plugins/timersandbuffs/TimersAndBuffsPlugin.java) and [tests](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-client/src/test/java/net/runelite/client/plugins/timersandbuffs/TimersAndBuffsPluginTest.java) | Active/cooldown timers are 60 seconds (100 ticks); active value two supports improved use, one may be a remaining charge | All three files fetched at the pinned revision. Timer support does not calibrate exact server expiry order or establish an observed cast/proc. |
| Indexed [Arceuus spellbook](https://oldschool.runescape.wiki/w/Necromancy_spell), Jagex's [14 May 2025 Yama announcement on Steam](https://store.steampowered.com/news/posts/?enddate=1747216272&feed=steam_community_announcements) | Death Charge grants 15% per eligible proc; upgrade permits two eligible souls per cast; cooldown starts at cast | Spell fact read from indexed Wiki material; announcement opened and reviewed. Fresh full Wiki fetch unavailable. Eligibility remains an explicit scenario input, never inferred from any Nex/minion death. |

Fetched development-reference SHA-256 values:

- Equipment.ts: `75391a934540c846a6d360302e825fceb88ddabc8de217ccae224476331a3388`.
- GameTimer.java: `250f4827f3e0c1918464a7ceb78a793f1653f39511bd403f64c1d93aa267eaf5`.
- TimersAndBuffsPlugin.java: `fbd7c3d1989f69bef23f1309671ae8285e91788e400a71b1e63aa52483993523`.
- TimersAndBuffsPluginTest.java: `56aa301b10c5740ec7bef29e2c7568f04fceba8f3518d419731b7dd3b9edcbb5`.
- Jagex Steam announcement page: `11fbab2e6b8c231bb75b461ce00bb11718f3f702f86c5345882fab9eeb0ecf17`.

These are original factual summaries and independent Java arithmetic. No external
code, prose, images or dataset is copied or bundled. Existing notices and source
registers are unchanged. Source availability and synthetic tests do not establish
current live-game agreement.

## Observed review

The existing post-kill worker builds an immutable `NexResourceReview` with the
frozen N2 result. **Owned setup comparisons → Special-attack resources** shows
first/last recorded energy and sums of positive/negative sampled net changes.
An optional reading selector preserves equal-tick input order, active/cooldown
readings and ring changes without constructing a component for every sample.
Only fight samples within the measured interval are included;
pre-smoke evidence never supplies starting energy or an improved-use claim.

Unread/out-of-range values remain unknown and break delta chains. A one/zero
charge reading never establishes absence of the upgrade. Decreases may include
expiry; energy changes can combine spending and restoration, with no identified
weapon, launch, cast or proc. At-cap samples do not quantify wasted restoration.
Continuous state sampling does not establish capture of every resource event.

The report uses the existing worker, identity/revision checks and frozen detail
lifetime. It writes nothing to disk, changes no capture boundaries and supplies
no in-combat recommendations. Current adapter/client acceptance remains open.

## Finite scenario contract

`SpecTimeline` accepts a known initial energy in tenths of percent, an exclusive
end tick, a fixed ring for the entire scenario, a first natural-restore tick and
ordered explicit events. All energy sources share the 1000-unit cap. Excess
restoration is lost at that event; it cannot be saved for later. Natural phase
continues through spending/full energy. The input starts with **no active Death
Charge and its cast available, with attacks initially ready**; it must not be used to replay a capture with
unknown prior cast/cooldown state.

Events are explicit casts, distinct eligible deaths and legal attack opportunities.
Casts supply one/two charges, expire at cast+100 and permit recast at cast+100.
The expiry boundary is a declared policy awaiting live calibration. Procs consume
charges even at full energy. Duplicate death keys cannot produce another proc,
including across recasts. Eligibility, runes, unlocks, target interaction and cast
opportunity costs must be established by the caller; the kernel does not invent
or validate them. No heal/potion/Surge source is silently inserted.

All attacks share one next-attack tick. Accepted specs spend their full energy
and displace ordinary attack opportunities during their delay. Rejected specs
spend nothing and add no delay; they do not automatically become ordinary attacks.
An explicit ordinary event may follow a rejection. ZCB rapid/other styles are
distinct; no bolt, accuracy, damage, target HP/cap or personal/team metric is
computed. These results establish **resource and delay consistency only**, not
that a setup, supplied attack window or whole kill is executable.

Equal-tick explicit events retain the caller's order. Natural restoration can be
placed before or after those events. `unknownRestorationPhase` enumerates every
first-restore offset from zero through the fixed-ring interval, including a restore
due at the scenario start, and both policies as sensitivity cases, without assigning
probabilities or claiming all possible interleavings. No interval is advertised as
statistical uncertainty. Ring switches, prior active spell state, cast/attack
attribution and observed phase are not backfilled.

Inputs are bounded to 3000 ticks and 2048 explicit events; results and plans are
immutable. The scenario API has hand-calculated tests but is **not automatically
fed from captured deltas or used to rank upgrades**. The runtime UI exposes
observed evidence and reference costs only. N3b must join reviewed spec damage,
HP-dependent bolts, ammo/charges, supplied opportunities and ordinary attacks
before recommending Voidwaker versus ZCB.
