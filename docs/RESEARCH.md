# Evidence and research index

Updated 15 October 2026. This file separates the accepted product decision from
verified implementation and remaining research. [TASKS](../TASKS.md) owns delivery.

## Decision and inspection

The user approved Nex Trio MP2, one kill per trip, Lightbearer throughout, upgraded
Death Charge and automatic setup with focused overrides. Faster kills and higher
personal contribution are the objectives. Other gear, supplies and spec timing
are not supplied account evidence. No screenshots or private account dumps were
used in this transition.

Inspected baseline [e1888ff](https://github.com/PetterHL/ironpath/tree/e1888ffc2ef571ed6159efaeead4016f42e32646),
plugin 0.14.0. The old optional Zilyana comparison picks the longest nonzero worn
segment; its capture resets on loading. These are concrete gaps for multi-switch
Nex and banking between every kill. T0 separates/relabels that experiment but does
not fix these gaps by claiming Nex support.

## Pilot evidence reviewed on 15 October

| Source | Finding and boundary |
|---|---|
| [Nex strategy page, Wiki mirror](https://osrsindex.com/wiki/nex-strategies?site=osrs_wiki) | MP2 uses melee in appropriate shadow windows, ranged during Embrace darkness, returning when Fear the shadow permits. The direct Wiki was unavailable; this is mirrored strategy evidence, not new measured event mappings. |
| [Plugin Hub manifest](https://github.com/runelite/plugin-hub/blob/master/plugins/nex-droprate-calculator) and [pinned Nex plugin](https://github.com/Worley03/nex-droprate-calculator/blob/48217ba0e2915e9553c922d73034c5f2866fc4a7/src/main/java/com/nexdropratecalculator/NexDroprateCalculatorPlugin.java) | Public NEX_BARRIER/chat/tick/hitsplat signals support feasibility. The plugin sums NPC hits during the active fight and excludes healing from its total; its value is not proof of official contribution semantics. No integration or copying is required. |
| [RuneLite Death Charge tests](https://github.com/runelite/runelite/blob/e98399f931c60bfa8e42f924da850f508ec8e00d/runelite-client/src/test/java/net/runelite/client/plugins/timersandbuffs/TimersAndBuffsPluginTest.java) | Normal cast uses active value 1; improved cast uses 2. Observing two charges can establish improved active use; one remaining charge/zero cannot alone establish absence of the upgrade. Installed-client behavior still needs verification. |
| [Wiki calculator Equipment.ts](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/Equipment.ts) | Reference spec costs are Voidwaker 50 and ZCB 75. Charged ordinary and blessed quiver share the eligible-projectile +10 accuracy/+1 strength bonus. This is a development reference, not a complete Nex model or imported implementation. |
| [RuneLite feature policy](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features) | Reviewed 15 October; restrictions include content simulation/high-end boss plugins. Existing plugins do not establish approval. Scope review is required before distribution, not a blocker to this cleanup. |

The current public/API examples establish feasibility only. N1–N4 must pin reviewed
mechanics/mappings and test actual phase boundaries, completion correlation, target
inclusion, delayed attacks, charge/ammo visibility, resource timelines and uncertainty.
No live kills, accuracy of contribution, or predictive time savings are established here.

## Retained evidence

- [Previous research](archive/pre-nex-20261015/RESEARCH.md): source revisions,
  independent arithmetic, acquisition limitations and historical decisions.
- [Kill-impact research](archive/pre-nex-20261015/KILL_IMPACT.md): public-event and
  numerical investigation; former product placement and pilot ordering are superseded.
- [Progression relationships](archive/pre-nex-20261015/PROGRESSION_RESEARCH.md):
  30 reviewed relationships, useful as source material, not the current ranking policy.
- [Validation index](validation/README.md), [original integration](validation/ad1-r3b-integration.md)
  and [research registers](research/): original provenance and unperformed checks.

Use independent Java implementations. Review code, datasets, written guides and
images separately; GPL reference availability does not permit copying into BSD
work. Source/model changes invalidate dependent claims only. Keep licence notices,
review dates and limitations; never fabricate a research pass from existing tests.

## N1 static adapter review

The [Nex public-event review](research/nex-events.md) pins the mappings, exact
sources, independent implementation boundary, resource unknowns and session policy
used by development build 0.15.0. RuneLite 1.13.2 is the compiled dependency.
[Validation](validation/nex-session.md) separates synthetic replays/lifecycle/UI
checks from unperformed real-client and player checks. No account dump, live kill,
official contribution or modeled seconds-saved result is claimed.

## N2 owned-package review

The [ordinary offence and owned-package contract](research/nex-owned.md) records
the exact Nex base-stat facts, retained pinned formula/item evidence, independently
authored quiver projectile handling, style mapping boundaries and computational
search scope. No calculator code or dataset is imported. The direct Wiki request
was unavailable; bounded target facts were cross-checked against the already
pinned development reference. This is not a fresh full live-game verification.

[N2 validation](validation/nex-owned.md) distinguishes reported equipment from
synthetic levels, prayers, ammo/splinter quantities and phase windows. No private
account dump, live kill or new distribution authorization is claimed.

## N3a resource review

[Finite special-attack resources](research/nex-spec-resources.md) records the
versioned energy/delay rules, source retrieval limits, pinned reference hashes,
scenario assumptions and observed-review boundary. The direct Wiki/mirror pages
were unavailable; indexed facts, the existing source register, pinned RuneLite
files and Jagex's Steam announcement are distinguished explicitly.
[Validation](validation/nex-spec-resources.md) records synthetic results and
remaining live checks. N3's damage/ammo/supply models and upgrade decisions remain open.

## N3b damage and ammunition review

The [N3b spec/bolt and ammunition review](research/nex-spec-damage.md) records
current indexed Wiki facts, pinned development-reference hashes, independently
authored conditional models and exact boundaries. [N3b verification](validation/nex-spec-damage.md)
separates hand-calculated mechanics tests from unperformed live/client acceptance.

## N3c complete-package review

The [complete-package input and evidence contract](research/nex-packages.md)
records reuse of N2 construction and N3b kernels, exact added equipment facts,
pinned source hashes, indexed-evidence limits, independent hand cases and scoped
unknowns. [N3c verification](validation/nex-packages.md) records automated
acceptance. Live capture/player acceptance and automatic personal recommendations
remain open; no reference implementation or dataset is imported.

## Runtime N3A/N3B/N3C join

[The post-kill contract](research/nex-runtime-plans.md) records captured versus
declared inputs, finite stock, source reuse and the observed-reserve boundary.
[Validation](validation/nex-runtime-plans.md) retains synthetic and client limits.

## CTX1 context contract

CTX1 (0.21.0) changes metadata, lifecycle and descriptive comparability. It adds no
combat formula or new target/method mechanics. MP2/RP2/Other/Unknown and group/count
are user declarations; nearby public players do not establish a party. Inherited
defaults are not kill confirmations. Conditional ordinary state arithmetic retains
its existing N2 evidence/limitations outside the pilot; this does not generalize
method recommendations or N4. [Synthetic verification](validation/ctx1-kill-context.md)
distinguishes automated data/worker/Swing checks from unperformed live acceptance.

## N3 decision support (19 October 2026)

[Capture-to-scenario, minion and supply review](research/nex-decision-support.md)
records pinned target/reference hashes, indexed factual cross-checks, separately
reviewed item/dose IDs, finite delay/level rules and explicit unknown gates.
Costs use declared unit prices; routes reuse existing catalogue evidence. No
bulk reference dataset, calculator implementation or article prose is imported.
[Initial verification](validation/nex-decision-support.md) and
[Zaros/spell follow-up](validation/nex-zaros-spells.md) separate synthetic checks
from unperformed client/attribution/dynamic-overhead and player acceptance. Public
NPC health APIs explicitly transmit a ratio rather than real HP. Declared target
stats are never inferred from that ratio or from player stat drains. The follow-up
pins official NPC/Actor/item and pouch API references plus the limited spell-cost
cross-check; no external implementation is copied.

The 20 October [attack-type review](validation/nex-deflect-attack-type.md) derives
a narrow declared Deflect Melee interaction from Jagex's released Magic-type
Voidwaker special and the reviewed melee-only curse scope. It distinguishes the
guaranteed special from its ordinary fallback; it does not establish ordinary
reflection, overhead timing or live-server agreement. The contract records the
official launch article, rechecked pinned reference hash and indexed-source limits.
