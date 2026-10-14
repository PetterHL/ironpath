# AD1c — Visible priority and advisor navigation

14 October 2026 · implementation candidate · plugin **0.13.0**. Knowledge
**ad1a-1**, catalogue schema **13**, preference schema **2**, progression-2 and
combat/acquisition model versions are unchanged.

This focused increment starts at **AD1a #28, `75595a5`**, which contains UX2 #26
(`54d9c239`) and EP1 #25 (`fafb745`). Both predecessor chats had completed their
implementation handoffs before this work began. AD1a supplies the concrete
DPS/card/navigation dependencies. **AD1b #29 (`98c82d2`) is a separate EP1-based
candidate and is not included here.** No existing PR is merged, closed or rewritten.

## Result

The default Upgrades view explicitly shows **Recommended order**. Recommended and
Browse are variants of the same overview. Each acquisition card combines its
supported gain or reviewed utility, the actual baseline and a concise reason
combining value with remaining effort. A material comparison-input correction
can retain one short status; repeated ownership warnings no longer occupy that
slot. The unchanged progression-2 queue supplies the order, including the existing
family deduplication and viewport budget. No largest-DPS, shortest-grind or DPS/hour
sort is introduced.

`PriorityExplanation` exposes typed value, effort and constraint plus product
copy. Normal **Why this position** no longer prints engine phrases such as
“unknown value is not zero”, table cells or diagnostic comparison rationale.
Core roles, specialist utility, marginal/covered choices, preparation and unknown
work remain distinct. Optional choices are explicitly optional in their detail
reason. Numerical gain and comparison gaps still come from AD1a's supported models.

`AccountChecks` groups each unresolved ownership fact once, retaining distinct
affected outcome IDs. Account shows affected counts and opens the corresponding
reversible correction. Correction controls remain subject to live observation
precedence and identity/view guards. Confirming one fact resolves that fact across
the affected advice while unrelated checks remain. These are planning facts;
manual ownership does not establish charges, quantities or usable equipment.

Top navigation is **Upgrades / Account**. Inside an item it is **← Upgrades** and
**Impact / Acquisition**. Account returns to the originating detail or overview;
Back retains search, filters, caret, focus and stable-ID reading position.
Navigation uses wrapping full labels at enlarged fonts. Diagnostics remains
separate under Account; lifecycle changes clear the correction target, navigation
and session reference/image state.

Acquisition leads with source, actual remaining components/assembly and a concrete
next action. Access, equipment and preparation are available in one secondary
entry; complete recipe, route checks and estimate evidence remain secondary.
Owned components continue to cut only their completed upstream work. A specific
known next requirement takes precedence over generic method-check prose.

## Conditional priority audit

[Complete evaluated audit](AD1c-priority-audit.md) exercises **all 29 active outcomes**
across **13 account fixtures × two explicit ownership states**: synthetic planning
absence versus unconfirmed ownership. Includes midgame, post-Bowfa, developed,
single-megarare, nearly complete, owned components and unread observations. Every
row retains value, priority, effort, method suitability, consequential constraint
and visible explanation. Queue positions are deterministic policy choices, not
precise efficiency measurements; equally conditional choices are not certified
ready or ordered by invented hours.

| Synthetic case | Finding |
|---|---|
| Post-Bowfa, explicit planning absence | Tormented, occult, Toxic and Scorching are the first four. Seven of 23 outcomes remain conditional for other requirements/methods. |
| Same account, unconfirmed ownership | 19 of 23 outcomes are conditional; 42 distinct ownership facts are grouped in Account. Anguish leads the conditional group through its core role; Medium, Long, Very long and unavailable effort remain distinct. |
| Owned components | Completed drops reduce remaining work without removing assembly/resources or shared allocations. Unconfirmed ownership still contains Short and Very long choices; neither becomes a fabricated total ETA. |
| Single-megarare | Appropriate remaining magic/other-role upgrades remain visible; owned tbow does not cover every specialist role. |
| Nearly complete | Shadow can naturally be the only remaining opportunity; its Very long effort remains visible. Unknown ownership changes it to conditional rather than inventing a nearer filler. |
| Midgame | Preparation and unavailable metrics remain consequential; higher levels/usable gear are not inferred. |
| Unread observations | No acquisition cards are manufactured. Open-bank/Account/Browse affordances remain available. |
| Reported ranged gear classes with synthetic missing inputs | All 12 emitted outcomes are conditional in both ownership states because access/method inputs are incomplete. Supported Long work and unavailable-method cases remain distinct. |

**Conclusion:** the conditional class is broad but does not erase supported value
or effort distinctions: the ranker retains its confidence, value/effort and
compatible-context ties. AD1c reveals these distinctions and the unresolved checks
instead of replacing the class with guessed readiness or a new numerical score.
The actual reported account still requires AD1c-C1 review. The client-variant
fixture contains previously reported gear classes plus explicitly synthetic
missing inputs, **not a full captured account or Petter's calculated result**.

## Verification and component evidence

Run with Temurin **11.0.32.1+1**, Gradle **8.10**, resolved RuneLite **1.13.1**:

```sh
source /workspace/ironpath-environment/activate.sh
./gradlew test build --offline --console=plain
git diff --check
```

**The complete `test build` passes: 317 tests, zero failures, errors or skips.**
`git diff --check` passes. The focused regression run also passes. Existing combat arithmetic, estimate scope, preferences,
identity, delayed IO/image callbacks, search/filter/navigation and ranker tests
are retained. Old label/ownership-badge expectations now exercise Impact,
Acquisition and the shared Account correction flow. New tests verify conditional
value/effort distinctions, shared-fact resolution, actual Advisor coverage,
navigation restoration and stale correction rejection.

New **225px native Swing** captures and bounds reports use synthetic inputs and
fallback sprite wells. No generated design image or real-client pass is implied.

| Implemented view | Capture |
|---|---|
| Recommended order | [Post-Bowfa](../images/ad1c/post-bowfa-home.png), [nearly complete](../images/ad1c/nearly-complete-home.png), [unread](../images/ad1c/unread-bank-home.png) |
| Impact and complete packages | [Zaryte](../images/ad1c/zaryte-value.png), [buckler/Blazing setup](../images/ad1c/ad1a-buckler-12.png) |
| Acquisition | [Zaryte remaining work](../images/ad1c/zaryte-path.png), [scoped manual next shard](../images/ad1c/manual-next-shard.png) |
| Account corrections | [Shared check and focused correction](../images/ad1c/shared-correction.png) |
| Input versus model gap | [Correctable input](../images/ad1c/shadow-input-check.png), [unsupported model](../images/ad1c/shadow-model-gap.png) |
| Enlarged 20px fonts | [Overview](../images/ad1c/large-font-home.png), [Impact](../images/ad1c/large-font-value.png), [Acquisition](../images/ad1c/large-font-path.png), [Account](../images/ad1c/large-font-account.png), [complete package](../images/ad1c/ad1a-buckler-20.png) |

The harness verifies wrapping and sidebar bounds without shrinking fonts. These
are first-600px captures; paired `*-bounds.txt` reports cover the full components.
Historical UX2/AD1a images and original test reports remain unchanged.

## Pending acceptance

TASKS remains the only backlog. **AD1c-C1** owns full reported-account review;
**AD1c-C2** owns desired-reference comparison and real RuneLite sprites/scaling,
keyboard/lifecycle and player usefulness; **AD1c-C3** owns the joint Acquisition
flow after authorised integration of AD1b. The [reference handoff](../images/ad1c/references.md)
records that the desired images were unavailable. No image match is claimed.

Inherited hours are still optional **manual, next-component scenarios** on this
AD1a-based branch. AD1b's automatic models, broader time coverage and remaining
work are neither duplicated nor claimed delivered by AD1c. Likewise AD1a's full
player reproduction, Confliction and other mapped mechanic gaps stay open.
Automated tests and component captures cannot pass joint AD1 player acceptance
or establish Plugin Hub release readiness.
