# UX2 — Native visual advisor

14 October 2026 · implementation for review · plugin **0.11.0** · unchanged knowledge **ep1-1**, catalogue **12**, preference schema **2**, ranking **progression-2**, activity profiles **activity-effort-1** and combat formula versions.

Based on EP1 #25, `ep1-account-efficient-progression` at **fafb745**; stacked PR base is that branch, not main. The EP1 contracts and queue were inspected and exercised locally. External EP1/UX2 review and combined PC acceptance remain open. Neither dependency nor this change is merged by this work. [EP1 evidence and full outcome/route/method matrix](EP1.md) remain authoritative for the domain calibration.

## Result

The default deck and Browse use one native card action for the complete sprite/name/result surface. Enter and Space activate it; focus outlines the card. Each acquisition card shows its supported delta or reviewed specialist property, actual comparison baseline and one selected status. Positive/negative results have text and a colour equivalent; true zero is neutral. Whole-card activation replaces the repeated card Value/Path buttons. The two destinations remain immediately available inside details.

Value presents compact changed items/packages with genuine ItemManager requests, supported before/after metrics and one account-specific value/effort reason. Unchanged secondary stats are omitted from the normal flow and remain inspectable in calculation basis. A weapon's unavailable DPS remains the primary unavailable result; supported max hit is secondary. A specialist property never creates a numerical benefit. If a specialist card uses an explicit encounter comparison, both its target and the actual owned weapon from that comparison are shown; the neutral selected result stays in Value/basis.

The shared analytical premise is explained under **Evidence and calculation basis**. No 0 Defence/reference badge appears on cards or in normal details. Non-neutral contexts remain labelled. DPS still describes ordinary uninterrupted offence, not boss throughput, kill time, trips or acquisition pace.

Path leads with source/activity, the account-selected remaining-effort band and a concrete next step. Evaluated remaining DAG nodes supply access, components/assembly and preparation; owned intermediates remove completed upstream branches from the normal instructions. Equip remains separate. Full ALL/ANY recipes, completed requirements, route alternatives, source/probability assumptions and optional pace correction are behind **Estimate basis and route evidence**. Optional preparation retains its own work and no unsupported combined saving.

Account shows relevant levels, recorded gear/unlocks and reversible ownership/unlock corrections. Observed possession, worn slots, usable gear and manual planning facts remain distinct. Optional goals, skipped routes and experimental Recent boss are secondary. **Diagnostics** is a separate screen for observation provenance, timestamps, raw fact details, knowledge version and local storage state, with Back to account.

## Preserved boundaries and checks

- EP1's queue, correlated-family preview subsequence and catalogue coverage are unchanged. Reflow selects a measured prefix; it does not rerank or insert long-term/specialist filler. Long-term/Specialist counts and complete Browse remain.
- Search/facets/category, caret/selection, expanded filters, owned-use discovery, independent list anchors and Back remain guarded. Account/Diagnostics and Value/Path navigation restore the appropriate focus/scroll destination. Lifecycle clearing drops all transient views and anchors.
- Stale card/filter actions and delayed ItemManager callbacks cannot activate/update a new result/account. Before/after sprites also have current-view/action guards. Reference, ownership and account correction callbacks reject detached old controls.
- Per-metric availability retains supported zero/loss, missing input, unsupported calculation and reviewed qualitative utility. Missing input exposes a specific correction; unsupported mechanics retain evidence rather than a futile correction prompt. Missing values never become zero.
- Next-step and remaining-work records are produced by Advisor. Slot names/changes, primary/secondary metric values, availability, selected status and account-specific reason are immutable presentation data. Swing does not parse diagnostic reports or compute ranking.
- Automatic profiles remain qualitative A0. Existing selective A1 odds and matching optional manual A2 scenarios remain next-reward only. Clear removes manual hours because EP1 has no promoted automatic full-cycle numerical benchmark. No new odds, performance inference, gear-derived pace, full-item ETA, save format or telemetry is added.

## Automated validation

Temurin **11.0.32.1+1**, Gradle **8.10**, resolved RuneLite **1.13.1**. Command:

```sh
source /workspace/ironpath-environment/activate.sh
./gradlew test build --offline --no-daemon
```

The final run passes **306 tests, zero failures/errors/skips**. Initial dependency resolution used the configured session proxy; the final validation uses cached dependencies. `git diff --check` passes.

Eight new regressions exercise the actual advisor and Swing flow, including all eight EP1 account cases, zero/loss/unsupported metrics, faithful contextual baselines, intermediate bypasses, native/large-font bounds, keyboard activation, manual next-shard scope/Clear, account/diagnostics separation and detached corrections. Existing panel/lifecycle tests were migrated only where UX2 supersedes the old card buttons, report placement, status prose or exact font equality. Their search/caret/scroll, account isolation, reference-family, owned-use and sprite regressions remain. Existing magic/ranged/melee arithmetic and EP1 coverage/ranking/source tests are retained.

## Component evidence and visual reference

All images below are **implemented native Swing component captures at 225 × 600 logical pixels**, with approximately **213px inner width**, original synthetic accounts from EP1Fixtures, no goal, native ItemManager integration replaced by its explicit loading/fallback glyph in the headless harness. They contain evaluated values, not mockup numbers. They are not logged-in RuneLite screenshots or proof of genuine client sprites/player usefulness.

Produced by `UX2PanelTest` in `build/validation/ux2/images`; adjacent `*-bounds.txt` records identify text and measured component bounds. Images are copied unmodified into `ux2-images`. Default harness font is 12px; large-font captures use 20px, with results deliberately larger. The ordinary post-Bowfa view fits three complete cards and the start of the fourth in its first 600px. Large fonts show fewer complete cards without shrinking, clipping or losing catalogue eligibility.

| Evaluated case | Capture |
|---|---|
| Post-Bowfa next-step order | [Overview](ux2-images/post-bowfa-home.png) |
| Early/foundational gaps | [Overview](ux2-images/early-home.png) |
| Single megarare | [Overview](ux2-images/single-megarare-home.png) |
| Nearly complete, long grind naturally first | [Overview](ux2-images/nearly-complete-home.png) |
| Workable versus suitable Doom package | [Workable](ux2-images/doom-workable-home.png), [Suitable](ux2-images/doom-ready-home.png) |
| Owned components remove fresh stages | [Overview](ux2-images/owned-components-home.png) |
| Unread containers | [Overview](ux2-images/unread-bank-home.png) |
| Supported ordinary comparison | [Blowpipe Value](ux2-images/supported-blowpipe-value.png) |
| Unquantified special role | [Zaryte Value](ux2-images/zaryte-value.png), [Path](ux2-images/zaryte-path.png) |
| Correctable input versus unsupported calculation | [Shadow input](ux2-images/shadow-input-check.png), [Shadow model gap](ux2-images/shadow-model-gap.png) |
| Concrete source → next step → remaining work | [Masori Path](ux2-images/masori-path.png) |
| Next-reward manual estimate scope | [Next zenyte shard](ux2-images/manual-next-shard.png) |
| Account and focused correction | [Account](ux2-images/account.png), [Corrections](ux2-images/account-corrections.png) |
| Separate technical evidence | [Diagnostics](ux2-images/diagnostics.png) |
| Enlarged font | [Overview](ux2-images/large-font-home.png), [Value](ux2-images/large-font-value.png), [Path](ux2-images/large-font-path.png), [Account](ux2-images/large-font-account.png), [Diagnostics](ux2-images/large-font-diagnostics.png) |

The written **“Oppsummering av UX”** incorporated into [UX.md](../UX.md) is the available desired reference. No new illustrative overview/detail/account images were supplied in this task or claimed as inspected. Older R3a/UX-completion images are preserved as historical baseline, not used as the new target. The new captures match the written hierarchy: gold identity, dark card surfaces, a prominent supported result, concise baseline, one status, whole-card details, compact changed metrics, a single visible priority reason, source/remaining/next acquisition and separate diagnostics. Larger fonts intentionally show less content per viewport.

## Pending PC/player acceptance

Use this stacked build only after reviewing EP1 and UX2 together; do not claim these checks from harness images.

1. At ordinary sidebar width, verify real asynchronously loaded item sprites and their stable aspect/wells; inspect genuine before/after sprites. Test native RuneLite/OS 100/125/150/200% scaling and record actual viewport/font/client version.
2. Find a useful next upgrade on representative post-Bowfa, early, single-megarare and nearly complete accounts without a goal or mandatory pace. Explain its baseline, value/effort reason, acquisition source and next step. Check important omissions and disagreements with EP1's queue.
3. Use mouse, Tab, Enter and Space on the whole card; verify visible focus. Switch Value/Path, open basis/corrections, Account/Diagnostics, and return. Confirm viewport, search text/caret, filters and stable card position after bank refresh/reordering.
4. Verify unread bank, unknown equipped slot, unsupported Shadow calculation and a real zero breakpoint remain different. Confirm charges/ammunition/manual ownership do not create a supplied comparison.
5. Confirm owned intermediates suppress fresh acquisition while remaining quantities/skills/assembly stay visible. Inspect an adequate owned method beside the separate Doom preparation alternative; no repeated grind or unverified total-time saving.
6. Optionally set a matching next-component pace scenario, inspect scope/quantiles, then Clear. Account/profile/loading/logout/unsupported-world transitions must clear transient comparisons, recent evidence and pace; detached controls/images must remain inert.
7. Verify local preference save/load, editing-disabled/save-failure reporting in Diagnostics and experimental KI1's retained lifecycle/Recent boss flow. No KI2 personal-time or broad boss-support pass is implied.

No actual-client or player-usefulness pass, Plugin Hub approval/submission, merge or comprehensive-model completion is claimed.
