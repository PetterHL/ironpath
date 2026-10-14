# Browse search and navigation — first UX increment

14 October 2026. Plugin **0.8.3**; knowledge **r3a-3**, catalogue schema **10** and preference schema **2** remain. Branch `ux-card-navigation` depends on PR #18 at `d9e897b7114591287a8667616969b7bd9d09a0b8` and targets `r3a-personalized-cards`. Main remains `cb7c2cd`; no dependency is merged by this change.

## Behaviour

Browse previously offered categories only, with no explicit reset. Switching from a scrolled homepage could open Browse partway down, and Overview did not retain an independent position. Back used an absolute pixel position, which could return to another card after an earlier recommendation disappeared.

Browse now searches supported acquisition outcomes by item name. Every whitespace-separated word must match; case and word order do not matter. For example, `CROSSBOW zaryte` finds Zaryte crossbow. Search combines with the current category without changing domain rank. Clear filters restores All and the full evaluated result. No-match copy names the filtering condition and offers a way back to all supported upgrades; an empty supported catalogue retains the existing unknown-state explanation.

Typing replaces only count/card/empty-state components and retains the same live text editor, caret and keyboard focus target. It reuses the evaluated result without recomputing account advice or altering preferences. Replaced result cards and their asynchronous images have an additional result-generation guard. Search stays in memory, survives Value/Path/Back and observation refreshes, and clears with lifecycle state. Homepage Long-term/Specialist links clear an earlier search so their one-action discovery shows the complete category.

Overview and Browse store independent ID anchors. Returning to either restores the card offset and named focus target; a new search/category starts at the top. Detail Back also uses a card ID, retaining its offset when earlier recommendations disappear. All deferred navigation still checks the current view/account action; lifecycle clearing drops all saved anchors and search. Ordinary numeric comparisons, acquisition records and ranking are unchanged.

This is a partial R6 discovery increment alongside R3a navigation work. Search does not inspect research candidates, owned-use explanations, routes or sources. Style/slot/activity filters, broader coverage and owned-use discovery remain pending in TASKS.

## Component evidence

The matching before/after account is `R2AdviceTest.postBowfa()` with `R15Fixtures.absent(catalog)`, no goal, as used by the existing R3a component harness. It has 20 supported upgrade outcomes; `zaryte` yields two, while `no such upgrade` yields zero. These are synthetic fixtures, not Petter's account. Screenshots use a 225×600 logical-pixel panel (213px inner width), normal 12px or explicitly enlarged 20px font and fallback item glyphs. They establish component layout only.

| State | Capture |
|---|---|
| Baseline Browse, freshly built from `d9e897b` before editing | [Before](../images/ux-navigation/browse-before.png) |
| Browse with search, same account and dimensions | [After](../images/ux-navigation/ux-browse.png) |
| Two matching Zaryte outcomes with intact Value/Path actions | [Search](../images/ux-navigation/ux-search.png) |
| No matching outcomes and Clear filters | [Empty search](../images/ux-navigation/ux-no-results.png) |
| Same empty-search state at 20px font | [Large font](../images/ux-navigation/ux-large-font.png) |

`R3PanelTest` adds six regressions for combined search/category/reset and deterministic order; retained live editor/caret and stale result actions; Path/Back and observation refresh; lifecycle clearing and old controls; independent Overview/Browse anchors and filter reset to top; native/enlarged-font empty states; and Back after an earlier upgrade disappears. Related checks share fixtures within these tests. Existing panel, preference/observation lifecycle, recommendation and numeric regressions remain.

Baseline: JDK 11 / Gradle 8.10 / RuneLite 1.13.1 `test build --no-daemon` passed **232 tests**. Final build on Debian 13.6 / Temurin 11.0.32.1+1 / Gradle 8.10 / RuneLite 1.13.1 passes **238 tests, zero failures/errors/skips**, retaining all **1,944** original ranged oracle cases. `git diff --check` passes. The headless environment emits font-cache and existing Gradle deprecation notices; neither prevents the checks. No new dependency, combat mechanic or source retrieval is introduced.

## PC acceptance — pending

Close the development client, fetch and switch to `ux-card-navigation`, then run `gradlew.bat test build` and `gradlew.bat run` (or `./gradlew` on macOS/Linux). Open the bank and allow initial observations to settle.

1. Browse, type an item name and multiple words, edit the middle of the query, then switch categories. Check the count, caret, visible focus and absence of typing interruptions. Clear filters restores all supported choices. Search does not imply that an unobserved item is missing.
2. Scroll both Overview and Browse, switch between them, then open Value and Path by mouse and Enter/Space. Back should restore filters, the same card and the initiating action's focus. Change an ownership fact while in detail and confirm that removal of an earlier card does not move Back to a different upgrade.
3. Check genuine ItemManager images, asynchronous loading and 100/125/150/200% OS/client scaling at actual native width. Long names, search controls and empty-state copy must remain readable without horizontal scrolling or smaller fonts.
4. Switch account/profile or log out with a search active. The new account must begin on Overview with no earlier query, anchors, detail, stale action or image. Find an upgrade, explain its value and reach its next step.

Authenticated-client keyboard/focus behaviour, real sprites/DPI, profile switching and player usefulness remain **pending**. No polished-client acceptance, broader R6 completion or publication is claimed.
