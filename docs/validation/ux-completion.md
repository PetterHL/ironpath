# Native UX completion and Browse discovery

14 October 2026. Branch `ux-completion`, based on integrated main `2dd799a`.
Plugin **0.8.4**, knowledge **r3a-4**, catalogue schema **10**, preference schema **2**.
No combat formula, account observation, preference persistence or acquisition probability changes.

## Delivered behaviour

The homepage puts Browse beside IronPath and links its quiet observation line to
Account evidence/preferences. Back restores the previous surface and reading
position, including an upgrade's Value/Path view. Implementation/test diagnostics
stay out of the recommendation stream. Long-term and Specialist show counts from
the same evaluated results as their destinations; either shortcut clears earlier
search/facets so it actually opens the complete category.

Browse keeps item-name search and adds style, equipment slot, acquisition activity,
reviewed role, personal importance and exact pursuit-status facets under **More
filters**. Facets combine with search/category without changing domain order.
Their values describe reviewed coverage, not readiness or confirmed personal
benefit. Expanded/collapsed state, selected filters, caret and ID anchors survive
detail/Back and observation refreshes. Clear filters restores acquisition All;
profile/logout clearing removes facets, owned inclusion and saved account state.
An unavailable selected facet remains selected rather than silently broadening
the result. Long labels retain a wrapping full selection when the combo cannot
show it at the current font.

A quiet list-update notice appears when observed evidence changes acquisition
membership/order while browsing, including updates during a detail visit. The
existing ID anchor keeps the reading position. Identical reevaluation, repaint,
sprite arrival and filter navigation do not manufacture a new notice. Returning
to Overview acknowledges it; lifecycle clearing discards it.

**Owned / covered** inspects reviewed outcomes whose acquisition is covered by
observed or explicitly confirmed possession, including an adequate alternative.
**Include owned in All** appends those entries without changing acquisition rank.
These records are evaluated outside Swing and remain available when the
acquisition route is skipped. Research-only/withdrawn outcomes stay excluded.
They expose current ownership evidence, applicable/conditional reviewed uses,
alternatives, limits and sources, with no acquisition Path or importance label.
Possession never implies charges, compatibility, usability or a quantified
personal benefit. Owned-use numerical comparisons remain a future capability;
the view explicitly labels that boundary instead of presenting a zero gain.

Value and Path now reuse the reserved sprite/name header and distinguish the
selected tab with a visible border/fill and accessible description. Image callbacks
retain view/account guards. New result cards retain the existing result-generation
guard, so replaced search results cannot open stale outcomes.

Discovery metadata derives styles/slots from existing comparison records and
roles/routes from reviewed catalogue records. Additive optional `browseStyle` and
`browseSlot` fields cover qualitative families without inventing combat stats;
Venator declares Ranged/weapon. `r3a-4` records this indexing metadata only. New
families can use these fields without item-name UI exceptions.

## Verification

Debian 13, Temurin **11.0.32.1+1**, Gradle **8.10**, resolved RuneLite **1.13.1**.
`test build --offline --no-daemon` passes **248 tests, zero failures/errors/skips**,
including all **1,944** unchanged ranged oracle cases. The eight new tests cover
combined facets/order/Back, destination counts/account entry, owned discovery and
truth boundaries, update notices, profile/stale controls, qualitative/hybrid
metadata and native/enlarged-font rendering. The existing numeric, ranking and
lifecycle suite remains. `git diff --check` passes.

The expanded-filter regression caught Swing listener ordering that lost the
expanded state after changing a facet. The toggle now updates its own guarded
state before a refresh. A final focused R3PanelTest run also regenerates the
additional detail/account captures. Font-cache and existing Gradle deprecation
notices do not prevent these checks.

All captures below use **synthetic** post-Bowfa account facts, no goal, a 225×600
logical-pixel panel with 213px inner width, and **fallback/loading glyphs**.
They establish Swing component layout, not real-client sprite/DPI/focus or player
acceptance. At normal 12px font the first card starts at y=90: tormented bracelet
176px, Zaryte crossbow 236px, twisted bow 216px. Two complete cards and the next
card are visible. The full measured deck retains five previews over a short scroll;
Browse reaches every eligible result. Fonts are not reduced to force density.

| Surface | Capture |
|---|---|
| Homepage/header/counts, 12px font | [Home](../images/ux-completion/ux-completion-home.png) |
| Browse, filters collapsed | [Browse](../images/ux-completion/ux-completion-browse.png) |
| Combined Ranged/neck filters, expanded | [Filters](../images/ux-completion/ux-completion-filters.png) |
| Same expanded controls at 20px font | [Large font](../images/ux-completion/ux-completion-large-font.png) |
| Owned/covered discovery | [Owned](../images/ux-completion/ux-completion-owned.png) |
| Owned evidence/use explanation | [Owned Value](../images/ux-completion/ux-completion-owned-value.png) |
| Value sprite/header and selected tab | [Value](../images/ux-completion/ux-completion-value.png) |
| Direct Path and selected tab | [Path](../images/ux-completion/ux-completion-path.png) |
| Account evidence from the header | [Account](../images/ux-completion/ux-completion-account.png) |

## Actual-client acceptance — pending

Fetch `ux-completion`, run `gradlew.bat test build` and `gradlew.bat run` (or
`./gradlew` on macOS/Linux), then observe the bank on a supported Ironman world.

1. Open account evidence from the header and Back from Home, Browse and Path.
   Verify timestamps/unknowns and restoration of scroll and keyboard focus.
2. Match Long-term/Specialist counts to their destinations. Combine search with
   style/slot/activity/role/importance/status, expand/collapse filters, open
   Value/Path and Back, then Clear filters. Inspect full selected labels with
   enlarged fonts, keyboard navigation and no horizontal scrolling.
3. Inspect an owned item and a possession-covered adequate alternative. Skip its
   acquisition route and verify its use explanation remains, without claiming
   charged equipment, exact benefit or a new grind. Correct an ownership fact and
   inspect the list-update notice/ID anchor.
4. Test genuine asynchronous ItemManager sprites at native width and actual
   100/125/150/200% OS/client scaling. Switch account/profile with filters, detail
   and delayed images active; no old state or action may populate the new account.
5. Have a player identify a worthwhile upgrade, explain its benefit/trade-off and
   reach the next step without diagnostics. Repeat representative partial,
   highly developed and nearly complete accounts.

No authenticated client or player was available here. These rows remain pending.
Broader R6 relationship coverage, owned quantitative comparisons, later models
and KI1 are still open; this delivery completes the current discovery controls,
not those product capabilities or Plugin Hub publication.
