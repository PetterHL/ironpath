# Sidebar simplification — 0.23.3

19 October 2026. Based on the complete PR #48 UI at
`836e1e999bc36c3c718554d89ced98d9b6684a13`, including main/N3 #45, CTX1 #43,
native UI1 #46 and PR #48's profile readiness, diagnostic selection and suspended
scroll corrections. The existing test branch retains that full ancestry; main
and the dependency branches are unchanged.

Remove Gear & method (including Gear locks and manual method controls) and
Gear & unlocks from the sidebar entirely. Keep Last kill, Compare setups,
Team & method, session controls and Diagnostics in their existing layout.
Automatic observations, saved methods/locks/manual facts, upgraded Death Charge
assumptions, exact ownership/fact precedence and calculation logic are retained.
No replacement controls or preference migration are added.

Preference lifecycle and offline context/visual scenarios exercise the retained
guarded save path directly. UI regressions assert the removed controls are absent
in empty, active, paused, complete and diagnostic states. Existing context drafts,
frozen selection/planner/report/caret/scroll, verified travel and hard-reset
regressions remain active. All 14 PR #48 scenarios are retained; only screenshots
and checks for the removed editors are retired.

Verification: JDK 11.0.32.1 / Gradle 8.10 / RuneLite 1.13.2.

- Pinned `test build offlineReview --offline --no-daemon`: 349 tests / 42 suites,
  zero failures/errors/skips; 14 scenarios / 955 checks, zero failures; 210 PNGs.
- Normal `test build --no-daemon` also passes with the same cached classpath.
- Inspected fresh native 225px views at fonts 12 and 20; the two gear sections
  are absent and the remaining PR #48 presentation is preserved.
- Bundled build metadata identifies this delivery as 0.23.3. Changed-document
  relative links and `git diff --check` pass. Generated output stays out of Git.

Current CI/artifact evidence is recorded in PR #49. These are synthetic checks;
live RuneLite, gameplay, native OS focus/scroll/DPI/clipboard and player acceptance
remain separate.
