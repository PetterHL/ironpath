# Development instructions

- Product name: Ironpath. Tagline: Your path to endgame PvM. Repository: PetterHL/ironpath.

- Keep code, UI text, documentation and commit messages in English. Discussion with Petter may be Norwegian.
- Read README.md, docs/DESIGN.md and the active task in TASKS.md. Consult docs/RESEARCH.md for evidence. Do not expand the scope because an API or guide exists.
- Current approved direction (12 October 2026): broad Ironman PvM progression. Follow active P1–P5 in TASKS.md; the next assignment is P1–P3 together. Reuse existing T1–T3 implementation. Historical CoX-only scope, 12-decision ceiling and T4-before-expansion instructions are superseded. Pending real-client tests remain pending and do not block expansion unless a concrete defect does.
- Build this as an independent project. Iron Compass is an optional reference with overlapping features, not a required comparison gate or grounds for redirecting the work upstream. Validate our own usefulness and usability.
- Use the official RuneLite Java plugin template and current supported public APIs. Keep one Gradle project and minimal dependencies. Do not build a web app, backend, database, AI integration or general-purpose rule engine.
- Treat game state as partial observations. Unknown is not false or zero. Preserve observation time and provenance. Bank ownership, collection history and permanent unlocks are different facts.
- Keep raw account observations in memory. Persist only necessary preferences/manual confirmations in a plugin-owned local file using RuneLite's supported file API. Do not put bank data or account facts in potentially synced configuration.
- Clear account state on logout, profile/account changes and unsupported worlds. Verify character identity before applying asynchronous results. Never log bank dumps or account identifiers.
- Separate hard game requirements from recommendations. No mandatory Bowfa route, inferred player skill, invented DPS percentages, exact grind times or RNG completion percentages.
- Ship only reviewed knowledge with source references, review dates and explicit alternatives. New content and source changes are candidates, not automatically accepted facts.
- Keep evaluation pure and deterministic. Test meaningful recommendation and account-isolation failures, not getters or implementation structure.
- Follow current Plugin Hub policy. No gameplay automation, combat instructions, reflection, subprocesses, arbitrary downloaded code or runtime guide scraping.
- Preserve third-party notices. Do not copy guides, datasets or code based solely on public availability or attribution.
- Report what changed, what was tested and what still needs in-game validation. Update only the active task's status. Do not claim Hub approval or live testing without evidence.
