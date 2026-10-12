# Initial tasks

Updated 12 October 2026. Only this file tracks initial work. All tasks below are **not started**. Research and the documentation foundation are complete; no plugin code exists. Petter has approved the public repository `PetterHL/ironpath`; the repository and documentation foundation are established on GitHub. Independent development is agreed; competitor benchmarking is optional.

## T1 — Small account and value experiment (next)

**Outcome:** one narrow RuneLite development build that demonstrates reliable account observations and understandable recommendations.

- In the approved repository, add the current official example-plugin scaffold. Keep original notices; use the supported Java target and one Gradle project.
- Capture real skill levels, inventory/equipment, observed bank and one relevant quest/unlock into an immutable snapshot. Display only necessary facts and freshness in a basic sidebar. Treat missing data as unknown.
- Use three to six independently reviewed sample decisions in a pure Java evaluator. Do not copy a competitor's catalogue. Synthetic rule fixtures are acceptable for logic tests but must be labelled and must not appear as real advice.
- Exercise five scenarios: bank unopened; midgame player who declines CG; post-Bowfa raider; advanced account with a high-end item but remaining gaps; and a consumed unlock/variant plus character-switch case. Separate lifecycle regression tests from product judgement.
- Make the basic flow obvious: open the sidebar, observe the bank when needed, read a few suggestions and inspect why/alternatives. Record the account evidence and reasoning behind each suggestion. Do not require using Iron Compass or measuring superiority over another plugin.
- Verify local preference-storage support before adding persistent account facts. Bank observations remain memory-only.

**Done:** `gradlew test` and `gradlew build` pass (or exact blockers are reported); unknown bank and character isolation tests pass; no network/upload path exists in this plugin; live checks are explicitly passed or pending. Produce at least three concrete examples of understandable, account-specific advice across the five scenarios, including an established raider. Do not claim live usability testing when no game session is available.

## T2 — Review the bounded catalogue

Depends on T1's working foundation. Select at most twelve decisions within the CoX scope; at least four must help established accounts. Check current alternatives and hard requirements, write original explanations and effort assumptions, and attach source/revision/review/licence metadata. Do not allow one guide's opinion to become a hard requirement. Validate IDs, references, `all`/`any` conditions, cycles and missing evidence. Finish five representative account fixtures with expected explanations and alternatives, not just exact ordering snapshots.

## T3 — Complete the local recommendation slice

Implement deterministic ranking, up to three diverse choices, optional single goal, nearest prerequisite, skip/restore and reversible manual confirmations. Keep one sidebar. Verify owned gear removes redundant grinds, source links carry no account data, skipped requirements do not become completed, unknowns do not fabricate advice, and preferences survive restart without leaking between characters.

## T4 — Small user and maintenance trial

Test with Petter and, if available and explicitly invited, one or two experienced Ironmen. No unsolicited outreach. Check whether they can find a recommendation without assistance, understand its explanation, inspect an alternative and skip a grind. Acceptance target: at least four of five scenarios yield an actionable or correctly abstaining result, zero critical ownership/access errors, and at least three recommendations the player considers useful for the supplied account. These are proposed acceptance criteria, not measured results. Simulate a relevant source change and time the review/update process. Do not automate discovery unless it saves observed repeated work.

## T5 — Prepare distribution

After T4 passes: final licence/attribution check, concise install/privacy/coverage documentation, metadata/icon, supported-client build and a short manual smoke check. Choose a distinct Plugin Hub manifest identifier (`ironpath` is already used by Iron Compass). Follow current Plugin Hub rules and submit the pinned public source commit only when publication is authorised. Record plugin version, knowledge version and last review date. First release is the CoX slice; no implicit promise of comprehensive endgame coverage.

## First Codex assignment

Use this when implementation is requested. The repository name and public visibility have already been approved:

> Read AGENTS.md, README.md, docs/DESIGN.md and T1 in TASKS.md. Implement T1 only. Begin from the current official RuneLite example-plugin template and preserve its notices; verify today's supported Java target and public APIs. Build a minimal local account snapshot and a pure evaluator with three to six sample decisions, then expose a simple, understandable sidebar. Use unknown state for unobserved bank/unlocks, clear snapshots on all account transitions, and keep bank data in memory. Verify RuneLite's supported local-file API before persisting preferences; do not place account facts in synced configuration. Separate synthetic test logic from reviewed advice. This is an independent project: Iron Compass is an optional reference, not a required benchmark or reason to redirect the work. Do not add a backend, runtime AI, external account lookups, scraper, remote knowledge updater, full catalogue or polished UI. Run meaningful logic/lifecycle tests and a build. Record in-game checks as pending unless actually performed. Report what works, how to run it locally and what remains to validate; update T1 only. Do not submit to Plugin Hub.
