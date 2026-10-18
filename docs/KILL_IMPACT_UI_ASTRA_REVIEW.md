# Kill Impact UI, diagnostics and Nex kill-context — Astra review brief

**Status:** Research/design handoff for Astra, not an approved implementation specification.  
**Date:** 18 October 2026  
**Repository:** PetterHL/ironpath  
**Scope:** UX, native RuneLite feasibility, offline/live test handoff, and editable Nex context.  
**Requested next step:** Independent Astra review and recommendation, **not code implementation, merge, or roadmap replacement**.

## 1. Why this document exists

This brief consolidates the 18 October product/UI discussion so that Astra can assess a cohesive, realistic next step. The user likes the KillImpact visual direction and wants to improve the current RuneLite plugin UX now, while continuing Nex observation, calculation and test work. The user explicitly does **not** want an aspirational image treated as an exact implementation spec.

The conversation converged on three linked needs:

1. A compact, attractive **Encounter** view focused on the previous kill and the best *supported* improvements.
2. A separate **Diagnostics** view to bridge RuneLite and an external Codex/ChatGPT review workflow through one-click copyable reports and screenshots.
3. A small, editable **Kill context** for Nex method/strategy and team size, ideally assignable before the fight **or corrected after a completed kill**, followed by safe recomputation of context-dependent derived results.

Astra should challenge trade-offs, identify the smallest viable implementation boundary, and decide what belongs in the upcoming UI work versus later Nex-model work.

## 2. Current product direction and technical baseline

This is **IronPath**, a free, local Java/Gradle RuneLite plugin for account-aware PvM analysis. The **encounter-and-method pivot of 15 October 2026** is authoritative: understand a real encounter and actual setup, improve use of already owned gear, then compare meaningful new upgrades. The old general global progression-ranking homepage is superseded. The Nex pilot is **Trio MP2, one kill per trip**; subsequent methods/bosses are future extensions, not existing validated support. Preserve AGENTS.md, TASKS.md (only active backlog), docs/DESIGN.md, docs/UX.md, docs/KILL_IMPACT.md, docs/DEVELOPMENT.md, and their evidence contracts.

At the time of this review brief:
- README/AGENTS describe development build **0.17.1**, N1/N2/N3a integrated. N1 live acceptance, N2 player/client acceptance, and N3/N4 modeled upgrade impacts are **not complete**.
- **NexPanel.java** is a real native Swing PluginPanel, currently navigating Encounter / Setup / Account. The current top-level UI is comparatively text-heavy.
- **PanelWidgets.java** supplies reusable native text/buttons; **CardHeaderLayout.java** allocates a 40px sprite well; **ItemManager** already supplies item images in the live client.
- **NexMethod.java** currently has a fixed pilot ID (nex-trio-mp2-one-kill), upgraded-Death-Charge assumption, and optional gear locks. It does **not** yet model general Nex methods and team-size variants.
- **NexSummary** stores a frozen, in-memory observed encounter with a cohort/method, timing, signals, damage scope, states and gaps. Recorded evidence must not be rewritten when post-kill planning context changes.
- **DEV1 already exists**: ./gradlew -PruneLiteVersion=1.13.2 offlineReview --no-daemon runs seven synthetic scenarios through production-relevant adapter/session/evaluator/native Swing paths. It currently generates 44 PNG previews at actual 225px width, normal/enlarged fonts, plus reports and a scenario index under build/review/. **.github/workflows/offline-review.yml** uploads those outputs as CI artifacts. Reuse it; do not build a second independent harness.

This brief **does not override** the existing numerical/measurement contract. In particular, N2 supported ordinary-offence comparisons are not per-kill seconds saved; finite spec, ammo, eating/potions, team contribution and time deltas remain subject to N3/N4 validation.

## 3. Design concepts explored, user preference, and reference-image caution

Several illustrative images were generated during discussion:

- **IronPath / endgame-upgrades**: wide, ornate, multi-card aspirational dashboard; too wide and too dense for a real sidebar.
- **Pathforge**: ranked progression, high-value permanent unlocks, effort and longer-term preparation; alternative brand/product concept, not a selected roadmap.
- **KillImpact**: boss-specific post-kill summary, item sprites, bold relevant impact, and resource/accuracy signals. **This was the user's clear favorite**.
- **BossPath**: goal-filtered Inferno/CoX/ToB/Colosseum progression; not the current Nex-pilot direction.
- A follow-up visual showed RuneLite with Encounter / Diagnostics tabs, compact cards and a copy-report affordance. User questioned whether it was genuinely feasible.
- The user then requested **only the RuneLite sidebar/tab, not the whole game client**; the last image illustrates two *alternative screen states* side-by-side (Encounter and Diagnostics) for design review, **not** a dual-width production UI or two simultaneously visible panes.

The user ultimately found a **simpler, narrow-panel text/card mock** shown earlier in the conversation more faithful to what we should build. The image is inspiration for appearance and information hierarchy, **not a pixel-perfect reference**, and it is not proof of actual data coverage.

**Image provenance:** The concept images were generated in the ChatGPT conversation, **not committed in this branch**. When reviewing visually, attach the preferred narrow-panel screenshot(s) to Astra separately. The semantic layout below must remain sufficient without those assets.

**Realism constraints:**
- RuneLite PluginPanel has a standard nominal width **225px** with scrollbar/layout reducing usable content width. Do not expand or fake a 400–500px panel. Actual CSS-like browser components are unavailable; use supported native Swing, RuneLite ColorScheme, ItemManager sprites, custom-painted panels only where justified, and ordinary Java layouts.
- Tabs are feasible as small buttons / segmented native navigation, but the illustrated polish, four-across KPIs, ample whitespace and large typographic flourishes are **not** literal requirements at 225px.
- Compact item rows, 1–2-column small metric blocks, subdued borders, small sprites, readable colored deltas, accordions, detail navigation, and dark RuneLite visual language are all realistic targets.
- Normal and enlarged fonts, long item names, scrollbar position, focus, keyboard access and visible controls require explicit checks.
- Screenshots with mock sprites in a headless run do not prove in-client sprite appearance. Prefer actual RuneLite ItemManager icons when live, clearly identify placeholder wells in offline tests.

## 4. Preferred information architecture

One RuneLite plugin sidebar, with two **logical primary views**:

### A. Encounter (the user-facing default)

Suggested layout at native width:

    IronPath                             [gear]
    Nex · method / team context [edit]
    [Encounter] [Diagnostics]

    Last kill
    2:41             24%
    elapsed          observed share
    Session mean · n, when comparable

    Biggest supported gains
    [item sprite] Item / owned setup
                  +supported metric
                  one-line why [>]
    [item sprite] Another improvement ...
    [More alternatives]

    [Setup / account] [More details]

The information order matters more than visual decoration:
1. Current boss/method and concise context; show unknown/unconfirmed where necessary.
2. Last complete kill observed duration, damage share (with appropriate bounded label), session mean and comparable sample count; incomplete attempts separate. Only show food/potions if attribution is supported; inventory deltas alone are not eating proof.
3. **Most useful supported improvements**, each with item/whole-package identity, a single strong defensible metric, a short reason, and a detail action. Full evidence, unsupported gaps, and source explanations live in detail/Diagnostics. Do not fill a fixed number of cards.
4. Access to existing Setup, Account corrections, inspected immutable history and Reset without introducing a confusing abundance of top-level tabs.

The earlier conversational simplified version (Last kill + three short cards under Biggest gains) is a better approximation than the generated wide images.

### B. Diagnostics (testing and review)

A clearly separate, lightweight native view for development/live-validation sharing, not an unreadable wall of information in Encounter. Desired blocks:

- Observed kill/attempt ID or bounded session reference; status (complete / incomplete / paused / pending / excluded), adapter/scenario version, current active method and source (observed, manual, synthetic).
- Selected boss, method/strategy, team-size evidence or manual input, and an explicit distinction between captured inputs and assumptions.
- What is supported: observed timing/share, per-state ordinary offence, existing owned comparisons, resource readings; unsupported team-seconds/spec attribution/food or incomplete dependencies must remain clearly **unavailable** when appropriate.
- Coverage/gap reasons, key timing signals, comparison inputs and results, exclusions and warnings, without repeating every technical log line in the normal user view.
- A **Copy report** button: one-click clipboard export of a short, structured, readable, versioned diagnostic report, with all numbers and relevant caveats needed for review in ChatGPT/Codex. No manual selecting/scanning of sidebar text.
- A **Save screenshot** button or another intuitive way to save a readable PNG of the *actual native panel* or selected screen for sharing. Clarify whether Save captures only the panel, full scrollable content or viewport; avoid implying a full game-client screenshot. The existing offline native Swing screenshot runner must keep working.

The user asked specifically for **a separate tab** and **one-click copy log/report**, because there is no direct RuneLite-to-ChatGPT connection.

Astra should evaluate whether Diagnostics should be development-only, opt-in or available in public builds via settings/overflow, without compromising the normal UI. Avoid requiring a backend, API key or ChatGPT integration.

## 5. Important distinction: mockups are NOT modeled results

Images contained visually compelling but **invented** examples such as Zaryte crossbow “-12s per kill”, ruby/dragon bolts “-7s per kill”, Anglerfish “-5s per kill”, “+0.4 kills per trip”, 24%/38% damage share, hypothetical consumption, and “Available” statuses for kill-time prediction. They are **illustrative placeholders only**, not verified IronPath outputs or promises.

Do not use those numbers as test expectations or production result fields. Show:
- **Observed** only for captured duration/damage share with explicit scope and quality.
- **Supported modeled** offence/accuracy/max-hit only where calculations and prerequisites exist, clearly scoped to state/role/target/phase.
- **Unavailable** or **insufficient evidence** for per-kill time, contribution, useful spec effects, kill-trip extensions, and supply savings until the relevant model and validation justify them.
- Never add independent card gains; compare whole packages to the same baseline.
- Do not let mock UI create a false requirement that N4 must be complete before the visual modernization. A polished UI should remain useful while time-saved is unavailable, rather than showing repeated “hours not estimated” filler.
- No live fight coaching, popups, automatic focus changes or screen overlays. Updates occur quietly after combat.

Nex specifics in the source docs are authoritative; do not infer active spec casts from net energy changes, genuine food use from inventory loss, or party membership from visible players.

## 6. Nex “Kill context”: editable before AND after the kill

This was the final new product idea raised in the chat and is the main extra research question for Astra.

### Problem

Different Nex methods, especially **MP2**, **RP2** and **mass** play, may change the valid offense model, phase assumptions and teammate effect. **Team size** (trio, 5-man, mass, etc.) also materially affects attempts to estimate team contribution or group kill time. The RuneLite client cannot safely infer exact strategy or participant membership just from nearby player count.

### Proposed UX

Keep a **small context summary immediately under “Nex”** (not in upgrade cards):

    Nex
    [Kill context: MP2 · 3 players  ✎]
    Last kill ...
    Biggest gains ...

Selecting the pencil expands/opens a compact **Edit kill context** UI:
- Strategy/method: MP2, RP2, Other/Custom, Unknown (exact taxonomy to be reviewed).
- Team size: small numeric count with validation, or user-friendly Duo/Trio/4/5/6+ presets plus exact count; Unknown when unconfirmed.
- “Mass” may be a distinct **activity mode/team-size band** rather than the same conceptual field as MP2/RP2. **Astra should explicitly resolve this**; avoid a schema where selecting “Mass” silently overwrites valid phase-2 style data.
- Source label (manually confirmed before, post-kill correction, uncertain default), and an obvious Apply/Save action.
- Optionally remember a **default for future kills**, separate from a **per-kill override**.

### Proposed behavior

1. **Before kill:** Remember the user's usual method/team setting if deliberately set. Do not force a form before entering combat.
2. **During kill:** Observe actual equipment, states, attacks/signals and damage as now; do not fabricate team membership.
3. **After kill:** Attach or correct strategy/team size on the selected completed kill, even if the defaults were wrong or absent. Optionally provide an unobtrusive “confirm context” hint when materially required.
4. **On Apply:** Recompute only **supported context-dependent derived outputs** from the frozen observed baseline; guard background results against stale writes, identity changes and later kills. Correcting assumptions does **not** modify the captured timeline, timestamps, observed damage, or original evidence.
5. **Session summaries:** Group only comparable confirmed cohorts. A context correction must not retroactively contaminate averages; recompute or exclude that kill appropriately.
6. **No model for selected method/team?** Clearly indicate observations remain valid but dependent team-time/recommendation results are unavailable. Never reuse MP2 trio estimates under RP2 or mass merely because the UI permits changing the label.
7. **Lifecycle/privacy:** Keep account observations and combat traces in memory per existing identity/logout/reset contracts. Distinguish saved future preferences from scoped per-kill metadata. Do not persist session history or raw traces without an explicit reviewed policy change.

### Architectural concern

The existing fixed NexMethod pilot ID, Preferences.nex settings, NexSummary.cohort/method and session-mean grouping may need a modest extension. Astra should recommend a sound model for:
- Global default vs immutable recorded evidence vs post-kill per-summary user annotation;
- Structural distinction between strategy/phase-2 style, team count and mass-mode;
- Support/validation matrix for methods and team sizes;
- Change propagation, stale asynchronous evaluation, cohort reconciliation, and history freezing.

Do not quietly rebuild N1–N4 mechanics as a consequence of adding two form inputs. Treat wider RP2/mass support as **future modeling work** if not yet supported.

## 7. Test and review loop: RuneLite ↔ Codex/ChatGPT

The user repeatedly emphasized that a gameplay/manual Nex kill is expensive. The desired iterative loop is:

1. Codex Cloud implements UI changes against current main on a focused feature branch.
2. DEV1 synthetic encounter/scenario fixtures run production components **without RuneLite login or gameplay**.
3. Actual Swing panel rendered at **225px** and normal/enlarged font sizes; generate **Encounter and Diagnostics** previews, collapsed and expanded states, missing/unknown context, incomplete kill and multi-kill/frozen-inspection examples.
4. Build/review report records input provenance, supported/unavailable calculations, key numeric checks, method/team context, lifecycle gates, and exact tool/dependency versions.
5. Codex reads/visually inspects screenshots, fixes clipping/overlap/overlong text or broken hierarchy, runs tests/build, attaches PR artifacts and reports for human review.
6. Real RuneLite testing remains necessary for ItemManager sprites, live combat event mapping, keyboard/focus, clipboard/file chooser, real DPI and actual player usefulness.
7. During real play, the user can click **Copy report** in Diagnostics, then paste the sanitized report in ChatGPT/Codex, and optionally share a screenshot. This is **manual, user-controlled sharing**: not a live connector or background upload.

Existing DEV1 seven scenarios / 44 images / CI upload should be **extended**, not duplicated. Include UI checks for:
- Empty account, no kill yet, actively observing, incomplete attempt, valid complete kill.
- Unsupported metric and zero/negative gain; unknown stock, ammo or consumption.
- Method/team context absent, selected pre-kill, corrected after kill; unsupported RP2/mass; same-kill recomputation and cohort changes.
- New kill arriving while an older record is inspected (frozen inspection preserved).
- Bank-trip retention, logout/account switch/reset and pending stale-work rejection.
- Clipboard content matching the selected immutable kill, correct “observed vs assumed vs synthetic” labels, no account names/IDs/raw inventory dumps or sensitive identifiers in the default export.
- Screenshot success/failure behavior in ordinary client; headless screenshot generation separately.
- Normal/enlarged-font 225px rendering; wrap, clip, scroll, tab switching and Back behavior.

**Clipboard/report requirements:** One click copies a coherent UTF-8 text report, not a giant uncontrolled debug dump. Consider a schema/version, model/adapter/catalog version, timestamp/session identity **without private player identity**, exact supported numbers, omitted metrics/gap reasons, scenario origin and manual context source. Keep reports bounded and privacy-safe. Catch OS clipboard unavailability (including headless tests) gracefully. No automatic sending, no backend or mandatory third-party plugins.

**Screenshot requirements:** Confirm screenshot captures *real rendered Swing*, not an HTML approximation. The user should be able to share the image in ChatGPT. Prefer safe user-selected save location and predictable behavior; no silent saving of private data. Determine whether the “Save screenshot” control belongs in developer diagnostics and whether a manual native screenshot is needed at all when an offline report can be generated on demand.

## 8. Open questions Astra should evaluate

1. **UI:** What is the most legible, feasible information hierarchy in ~225px? Should Encounter and Diagnostics be two top-level tabs, or should Diagnostics be behind a dev/overflow entry? How do Setup/Account/history remain discoverable?
2. **Appearance:** Which elements of the images are realistically achievable using native Swing/RuneLite conventions without fragile custom painting? How many tiles/card metrics fit before readability suffers?
3. **Data honesty:** What should a “Biggest gains” card show today with **N2** modeled offence but **N4** team-time unvalidated? How should we label “time saved per kill” when unsupported?
4. **Diagnostics:** Is one-click **Copy report** plus offline CI screenshots enough for the next cycle? Should in-client PNG save be a same-task feature or optional follow-up? What should a diagnostic export contain/exclude?
5. **Manual context:** How do MP2, RP2, mass and team-size relate as independent dimensions? Which should be explicit, optional or inferred safely? How should post-kill editing work without rewriting measured truth?
6. **Cohorts/replay:** How do we avoid corrupting frozen inspected results or session averages when a kill's context is amended after the fact?
7. **Scope:** What is the most cohesive **single implementable UI task** now, and what modeling additions should be deferred to N3/N4 or later method extensions?
8. **Quality gates:** Which preview states, unit/lifecycle checks, real-client checks and copy/export tests are sufficient for review; what remains unvalidated even when all synthetic tests pass?

## 9. Requested Astra output and guardrails

Please read this document **and** the current repo, especially:
- AGENTS.md, TASKS.md, README.md, docs/DESIGN.md, docs/UX.md
- docs/KILL_IMPACT.md, docs/DEVELOPMENT.md and relevant validation reports
- NexPanel.java, PanelWidgets.java, CardHeaderLayout.java, NexMethod.java, Preferences.java, NexSummary.java
- DEV1 offline screenshot/report tests and GitHub Actions workflow.

**Astra assignment:** Critically assess the proposal. Return (a) an improved realistic UX/navigation design, (b) how to structure the optional editable Nex context, (c) a privacy-safe Diagnostics/test-handoff recommendation, (d) a tightly scoped next development task suitable for Codex Cloud, (e) risks/unknowns and checks, and (f) any required corrections to this brief. Prefer clear decisions with rationale to a long speculative roadmap.

**Do not implement code, create a PR, merge, publish, or modify the active task backlog during this initial Astra review.** User will evaluate recommendations and explicitly approve implementation. Existing N1–N4 measurement rules remain authoritative. Screenshot concept examples are **not** implementation requirements or numeric evidence.

## 10. Naming / later product scope (discussion context only)

IronPath remains the preferred umbrella project/plugin name **for now**. “Kill Impact” works well as the encounter analysis view or feature label. Earlier generated concept labels **Pathforge**, **BossPath** and **KillImpact** are alternative naming/visual explorations, not approved renames. Broad goal-based progression and other bosses are longer-term ambitions, but this review must not revive the superseded global homepage or an unrelated full redesign.

---

**Bottom line:** Make the current RuneLite sidebar substantially more useful and pleasant **now**, make live/Cloud testing and sharing effortless, and permit honest pre/post-kill Nex context without confusing preferences, frozen observations and unsupported modeled claims.