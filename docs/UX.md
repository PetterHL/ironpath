# IronPath native UI specification

13 October 2026 · **Selected R3a design; implemented for review, actual-client acceptance pending.** This supersedes the R1.5 row-first homepage and 100px Browse-cell rule. It refines presentation only: DESIGN `progression-1`, established research, quantitative coverage and the uncapped catalogue remain authoritative. [TASKS R3a](../TASKS.md#r3--relationship-aware-advice-and-melee-packages) is the sole implementation backlog.

**R3b ordinary melee, 14 October:** the existing native cards and Value/Path now support a melee family with independent session-only corrections. Controls list the selected weapon’s actual attack types/stances and label proposed prayers/boosts. Value names incompatible off-hands and unreviewed-family gaps; Path separates component ownership, drop access/task and irreversible whip consumption. [Synthetic 225px captures and pending client acceptance](validation/R3b-ordinary-melee.md).

## Decision and evidence

**Use a card-first upgrade advisor: compact, sprite-led, full-width cards at the ordinary RuneLite width.** Use two columns only when the actual native viewport and font can retain the same useful content. Do not require a wider window or make widening part of onboarding. Browse uses the same readable cards.

Inspected PR #17 at `71c8c177`, including R2 application code, `IronpathPanel`, `CompactRowLayout`, `BrowseGrid`, `PursuitOverview`, panel tests, and existing captures. [R2 overview](images/r2/overview.png), [Browse](images/r2/browse.png), [tbow Value](images/r2/tbow-value.png), [tbow Path](images/r2/tbow-path.png), [enlarged-font Browse](images/r15/runtime/browse-large-font.png) and [model-gap Value](images/client-gap/brimstone-gap.png) are **headless component captures with fallback glyphs**, not proof of real sprite appearance or client scaling. Existing [R1.5 wireframes](images/r15/effort-wireframes.png) remain historical, not the R3a layout target.

Concrete findings:

- R2's overview takes 1,218 captured pixels for four item cards, an activity and controls. Status headings and activity copy push long-term value down. Its tbow card leads with a negative neutral DPS comparison although its account explanation concerns a specialist role.
- R2 Browse is 2,735 pixels tall for 20 outcomes. Two columns increase item density, but roughly 103px tiles stack names, target labels and status across many lines. Six allowed comparison lines in `BrowseGrid` prevent some clipping without creating concise advice.
- Full-width rows also waste width: `CompactRowLayout` keeps the 40px sprite and action beside **all** body text. `text()` adds 5px on every side of every text area in addition to card padding. Moving only the name beside the sprite gives explanations the full card width.
- ItemManager asynchronous images, guarded detail navigation and accessible action names already exist. Account/preferences and broad advice are already collapsed; retain that foundation. The problem is card hierarchy, repetition, preview selection and weak visible value.
- `PursuitOverview` caps immediate preview selection at two and deduplicates both family and route. That can hide independent benefits from a shared source. Presentation selection must evolve without limiting catalogue eligibility.

RuneLite's [inspected PluginPanel source](https://github.com/runelite/runelite/blob/5ac57c9d9dc3f47efc9d98d1cad25753e7c82f03/runelite-client/src/main/java/net/runelite/client/ui/PluginPanel.java) declares 225 logical pixels, 6px insets and a separate 17px scrollbar allowance: ordinary inner width is about 213px. With a 6px gutter, two cells are 103.5px **before** card padding/borders. Do not subtract the scrollbar twice. This is a pinned source inspection, not verification of the installed client; the build currently requests `latest.release`. Record resolved RuneLite version and measured viewport during implementation.

| Realistic option | Strength | Cost at native width | Decision |
|---|---|---|---|
| Existing status sections and compact rows | Familiar, exposes status | Repeated headings; narrow body beside icon; long-term value below activity text | Replace homepage structure |
| Two equal columns throughout | Several recognisable sprites at once | Roughly 90px usable text width before nested text insets; long names, reasons and contextual numbers create tall tiles | Reject as default |
| Large featured card plus paired mini-tiles | Strong focal point and variety | Implies one best grind; remaining tiles lose reasons/numbers or remain tall; hero displaces options | Reject |
| Equal compact visual cards, one column at native width; two only when measured to fit | Full names, useful reasons and metrics; several comparable choices; consistent navigation | Fewer sprites across a row; requires disciplined copy and spacing | **Select** |

These are design judgements from source/capture review, not a player usability result. [Layout comparison](images/r3a/home-layout-options.png) shows the trade-off at 225px. It is an original schematic with labelled sprite placeholders and synthetic copy, not a RuneLite screenshot or recommendation result. The +2 max hit and +0.10 Zilyana DPS examples come from the retained R2 captures; they are separate labelled scenarios, not universal item benefits or a new combined account calculation.

## Homepage hierarchy and browsing

The first screen answers “What could improve my account?” without a goal or configuration step.

1. **Small header:** IronPath with compact Browse action; one quiet observation line linked to account evidence. No version/freshness paragraphs or repeated privacy footer in the recommendation stream. Consequential stale/unknown state stays visible beside affected advice.
2. **For your account:** a short deck of equally weighted item cards selected by the policy below. No oversized hero, carousel, horizontal scroller or repeating status heading for each card. Status belongs on the card.
3. **Discovery links near the deck header:** Long-term and Specialist, with counts where meaningful. They open filtered Browse and stay visible even when those roles are below the fold. Wrap links when needed. Specialist is a role filter; Long-term is a status filter; neither means low importance.
4. **Continue discovery:** Browse all with the accurate count after the preview. Below it, show a compact shared-activity opportunity only when justified, and a real assembly/claim action when present. Both remain available through Browse/Path. Account/Preferences, optional goals, broad preparation advice and coverage diagnostics follow as collapsed secondary controls.

At 225×600 logical pixels with ordinary font, aim for **three complete cards**, or two plus a clearly visible next card when necessary qualifiers wrap. Roughly four to six previews over a short scroll are a useful density target for a rich account, not a catalogue quota or requirement to invent advice. Choose preview length from measured complete-card heights within approximately one additional viewport; place Browse all at that boundary. Fewer real recommendations means fewer cards. Large fonts/short windows show fewer intact cards. Do not crop, shrink text, drop caveats or make off-screen items ineligible to hit a count.

### Preview selection and account-specific ranking

Compute a deterministic, explained preview sequence from the full evaluated result, outside Swing. Reflow changes how many cards fit, never domain rank. Use DESIGN's importance, preferences, evidence and burden policy; no UI score, random rotation or item-name overrides.

- Lead with the strongest justified actionable option when one exists. Preserve relative order of actionable choices under `progression-1`.
- Before repeating a covered role/reward family in the initial preview, surface the highest-importance distinct long-term or specialist option with demonstrated account value. If both exist, introduce their distinct representatives before further repeats. Within equally justified alternatives use documented deterministic tie-breaks. This is explained discovery variety, not a claim that a contextual choice is a better immediate grind.
- If no actionable option exists, lead with the strongest justified plan-ahead, long-term or conditional choice and its truthful label. Do not start with a large “nothing to do” block while worthwhile upgrades exist.
- A candidate serving several roles appears once. CoX rewards are not all duplicates solely because they share a source; correlated Ancestral pieces can share a family preview with a visible link to every piece. Grouping never sums gains or hides materially different routes/roles. An activity does not replace the valuable item.
- Do not promote low-value filler just to diversify. Conditional, marginal/deferred and owned-use outcomes remain discoverable in Browse. Unread ownership is “Check ownership,” never confirmed missing; uncertain evidence cannot produce unconditional advice.
- Ownership, an adequate usable alternative, an unlock, skipped route or corrected reference updates reason and ordering. Retain viewport/focus anchored to stable outcome IDs on refresh; show a quiet update indication if order changes while browsing. Never reshuffle from sprite loading or repaint.

R3a Browse supplies All / Pursue / Long-term / Check and **Specialist** access and a visible count. The 0.8.3 UX follow-up adds item-name search and Clear filters; words match regardless of case/order and combine with the selected category, preserving domain order. Specialist still uses reviewed role metadata. Search covers supported acquisition outcomes, including conditional and small-gain choices, rather than research-only entries or owned-use explanations. Typing retains the live editor; new filters start at the top. Overview/Browse retain independent ID anchors, and Back follows the same card after account updates. [Component evidence](validation/ux-navigation.md); actual-client focus/scaling remain pending. Style/slot/activity filters and owned-use discovery stay R6. Research-only entries are coverage information, never personalised recommendations.

## Card anatomy and responsive layout

Each card is a recognisable item surface:

```text
[real sprite] Full wrapping item name
Short account-specific value reason
Supported primary delta · target
Pursuit / Long-term / precise check
Value                         Path
```

- **Identity:** genuine ItemManager sprite at native aspect/size in a 36–40px reserved well; full bold wrapping name beside it. Allow extra name lines or place the name below the sprite when font metrics require it. No ambiguous abbreviations or hover-only names.
- **Reason:** one short account-specific sentence/phrase, usually one or two lines across the **full card width**. Name the role or difference from an adequate owned alternative; distinguish it from a generic missing-item list.
- **Numerical benefit:** one supported primary delta with explicit target/reference label. Give it comparable visual weight to the reason, not tooltip/detail-only treatment for ordinary numeric upgrades. Weapons and weapon-changing packages lead numerically with theoretical DPS; unchanged-speed armour/jewellery may use max hit. Before/after, accuracy and interval stay in Value. A shared visible comparison label above the deck may supply the target for matching cards, but exceptions name their target on-card; context travels into filtered Browse.
- **Status:** one readable line with the meaningful pursuit/remaining-work distinction; wrap if needed. Combine “Key improvement · Long-term” when necessary. Do not render every evidence axis as a badge or repeat “high work” beside an adequate long-term label. A consequential unknown gets a precise short label such as “Check ammo,” with the next check in Path.
- **Actions:** explicit compact **Value** and **Path** text buttons in one row, keyboard reachable and item-named for accessibility. Card body/title/sprite opens Value; Path opens Path directly. Child actions never bubble into the card action. No full-width Open details bar or chevron column stealing body width. Standard Swing focus/Enter/Space semantics, with visible focus on the actual action.

A specialist leads with its evidenced role, not a misleading neutral-loss headline. If a directly relevant quantitative specialist result exists, show its labelled metric; otherwise show the honest relevant model gap. Preserve **Enemy with 0 Defence** as the selected analytical default in Value, including negative/zero results and trade-offs; do not silently substitute a more flattering target. Explicit encounter examples retain their own inputs. No invented percentages, saved trip/kill counts or hours without a supported scope. Reviewed utility remains visible independently of ordinary DPS gaps.

**Layout rule:** one column at ordinary 213px inner width. Permit two only if `(availableWidth - 6) / 2 >= 170` logical pixels **and** actual font metrics fit complete names, reasons, metric/context, status and actions at a readable height. Thus 346px inner width is an initial design floor, not a promise of fit or request to enlarge RuneLite. Reflow the entire deck/grid to one column when the content check fails. Two-column order is row-major and identical to keyboard reading order. If the native host never supplies this width, one column is the complete supported experience.

Use content-derived heights; no fixed-height clipping, hover-only qualifiers or font shrinking. Start with 6px gaps/card padding, one border and small text-line gaps instead of repeated 5px text-area borders. A typical standard-font card may be 120–145px; longer copy grows naturally. Validate density in actual Swing, not from this estimate. Use RuneLite ColorScheme dark surfaces, restrained gold names and subtle boundaries. Respect client font; ordinary text contrast at least 4.5:1 and discernible focus/boundaries; colour is supplementary. Stable sprite wells prevent loading jumps. Missing/loading imagery retains name and a quiet placeholder; no sprite downloads, generated substitutes or copied sprite pack.

## Value, Path and shared activities

**Value:** compact Back, item sprite/name, then Value | Path immediately available. Lead with why it matters for this account, adequate owned alternative and defer reason. Supported comparison follows near the top with selected context visible. Do not put generic category paragraphs or long “Consider first” text before navigation. Show before/after once; name removed off-hand/set changes; keep max hit, hit chance and theoretical DPS distinct. Unsupported metrics state the exact missing input/mechanic, not zero. Assumptions, corrections, provenance/model versions and sources remain expandable. One ownership-correction action exposes existing guarded controls; consequential uncertainty remains visible without opening them.

For nearly complete accounts, name specialist role and limits: a small gain can be a valid optional choice. Preferences do not prove raid readiness. Reviewed one-hop “helps at X, which still offers Y” explanations stay anchored in direct value; no new graph screen.

**Path:** open directly at the next genuine remaining action or consequential check. Group hard Access, Components/assembly, Equip and optional Preparation separately; ALL/ANY routes remain vertical and collapsible. Show a reviewed sufficient-owned method beside an optional preparation detour where supported. Owned intermediates remove only completed stages. Text/symbols distinguish complete, remaining and unknown; quantities retain observation time. No RNG percentage-completed gauge.

Effort panels retain precise scope, optional pace and exclusions. Example: next zenyte shard, mean 300 / median 208 / P90 690 eligible kills under the reviewed model; 10% take longer. Hours require explicit valid pace and remain conditional. This is not completed-bracelet time; quest, onyx, crafting and supplies stay separate. Unknown effort is not cheap. No component-quantile sums, gear-derived pace or guaranteed deadlines. Long-tail raids say Time not estimated until mechanics and pacing support it.

Retain Set pace/Clear with blank default and explicit eligible-kills/hour units; put entered pace beside conditional hours. Keep mechanic/pace provenance, review date and exclusions available. A supplied pace range is sensitivity, not a confidence interval. Dry KC does not imply progress toward a fixed-rate drop. Withdraw known-invalid estimates without discarding valid value/path advice.

**Activity:** concise title, useful reward sprites/names and factual reason. One strong useful reward can justify it; no hardcoded “Several useful rewards.” Detail separates eligible rewards, uncertain/ineligible rewards, independent access/method evidence and owned exclusions. Distinguish any useful reward from obtaining all; no additive unrelated gains or promised simultaneous drops. Skipping a route does not erase owned utility or alternatives.

## Recent boss and Kill Impact (selected, not implemented)

[Kill Impact](KILL_IMPACT.md) adds a quiet **Recent boss · [boss name]** entry after a supported completed kill; Vorkath is illustrative. Raid results identify the raid, room/boss and relevant phase. Local offensive gain, team kill-time impact and a whole-raid estimate have distinct labels; unsupported room totals never imply zero gain. It opens existing contextual upgrade cards; the automatic account homepage remains primary. No popup, combat overlay, forced tab/focus change, per-kill chat message or new dashboard. Refresh after finalization and preserve a card the user is inspecting. Unsupported encounters do not fill the homepage with diagnostics.

Reuse sprite/name, one account-value reason, one supported primary metric, status and Value/Path. KI1 can show ordinary-phase offence from the actual used setup. KI2 may show `Est. 3–5 s faster/kill · same method` with sample count only after validation; this is **synthetic illustrative copy**, not a computed item benefit. Keep the range type and important uncertainty clear. `Small gain; no stable time estimate` is a valid state. Keep an already-owned alternative and recurring ammunition distinct from acquisition. Do not headline a larger number from an incompatible or assumed setup.

Value separates observed last/recent duration, actual setup segments, candidate package, stronger owned alternative and estimated impact. The ordinary neutral reference remains inspectable and is not overwritten by a recent boss. Disclose assumptions, source/model versions and sample quality under existing detail controls; material gaps remain beside the metric. No performance score, tick-log wall or automatic grind-hour conversion. One recent kill does not reorder the entire account by seconds saved. Use the existing native-width density and navigation/identity checks; [TASKS KI1–KI3](../TASKS.md#personalized-kill-impact--ki1ki3) owns delivery.

## Implementation boundaries and validation

Reuse native Swing/PluginPanel, ItemManager, reference/comparison services and guarded navigation. Immutable presentation data, calculations outside rendering, EDT updates, one vertical scroller, no horizontal/nested card scrolling. Preserve generation/revision guards and identity isolation for clicks, delayed updates and asynchronous imagery. No browser UI, JavaFX, per-tick rebuilding, animation, backend or window/focus manipulation.

Implement homepage, card reuse, Specialist discovery and detail entry points with the R3a relationship increment. This is not a product/research reset or schema-only release. Developer handoff and authoritative checkboxes are in [TASKS R3a](../TASKS.md#r3--relationship-aware-advice-and-melee-packages); [review record](validation/R3a-ui-direction.md) separates this documentation review from implementation evidence.

Required states: rich no-goal account, post-Bowfa, all-megarare/nearly complete, only long-term choices, small specialist gains, all supported outcomes owned, unread/partial bank, unknown ownership, unsupported numbers, skipped activity, owned intermediate, long names, large font, delayed sprites and account/profile change. Record measured viewport and resolved RuneLite/OS versions at 100/125/150/200% scaling, mouse/keyboard/focus/Back behaviour, genuine sprite loads and a player finding an upgrade, explaining its value and reaching the next step. Headless captures establish component layout only. [R3a component implementation and captures](validation/R3a.md) now verify the supported layout/navigation states. **Actual-client and player checks remain pending.**
