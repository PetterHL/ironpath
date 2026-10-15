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
