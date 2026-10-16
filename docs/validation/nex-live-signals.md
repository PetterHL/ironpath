# Second Nex client test: boundary signal investigation

15 October 2026. Development build **0.16.2**, adapter **nex-public-3**, based on
`21abe38`. Knowledge `n2-owned-1`, catalogue 14 and preferences 3 are unchanged.

## Client evidence

The next screenshots report a censored attempt with **start boundary not
observed; arena end boundary not observed**. All five phases and continuous
543/543 state ticks are present, with Nex death/dying at tick 739. The first
selected sample is UNKNOWN. The screenshot's **Matched smoke: not observed**
does not mean the smoke line itself was absent: the prior UI exposed only a
successfully matched timestamp. No recorded callback stream is available.

These screenshots do not demonstrate a fixed capture or a user activation error.
They do not establish the actual barrier values, the time of the smoke line,
the delay between dying and arena end, or whether scene/readiness gates preceded
the first sample. No raw inventory, account identifier or damage trace is saved
in the repository. N1/N2 client and player acceptance remain **open**.

## Source review and changes

Re-reading the [pinned public reference](https://github.com/Worley03/nex-droprate-calculator/blob/48217ba0e2915e9553c922d73034c5f2866fc4a7/src/main/java/com/nexdropratecalculator/NexDroprateCalculatorPlugin.java)
confirms active value 3 and end transition 3→2. It does **not** prove the 2→3
transition required by IronPath occurs on every one-kill bank trip. Its start
comes from the smoke line. IronPath's start requirement and five-tick end match
remain unvalidated restrictions; this change does not manufacture missing
boundaries, revise the timer origin, or declare a complete kill from continuity.

- Barrier callbacks now consume `VarbitChanged.getValue()` rather than rereading
  the client. Polling remains independent. A regression with a lagging getter
  failed on the old code and passes with event-payload handling. This proves
  the callback behavior, not that a lagging getter caused the live failure.
- Details distinguish an actually received smoke line from successfully matched
  smoke, identify adapter `nex-public-3`, and show raw barrier transitions with
  callback/poll origins. **Capture signal trace** expands Nex form changes,
  ActorDeath/despawn and scene signals. It is a bounded in-memory sequence of
  at most 32 entries, preserving the beginning and latest entries; no names,
  account IDs, container contents, persistence or network transport are added.
- With only one end signal, diagnostics may wait 20 ticks before freezing.
  Previously the summary froze after five ticks and could not distinguish a
  missing barrier from a later barrier. End signals more than five ticks apart
  still censor the attempt. Complete timing/coverage still stops at the original
  death tick; travel flushes immediately. The longer wait is a diagnostic bound,
  not evidence of a particular game animation duration.

## Verification

JDK 11 / RuneLite 1.13.2: `XDG_CACHE_HOME=/tmp/ironpath-cache ./gradlew test build --no-daemon`
**passed: 178 tests across 27 suites, zero failures/errors/skips** (27 s).
Normal JAR assembly passed. `git diff --check` and changed-document local links
pass. Existing Gradle deprecation notices remain. The callback-payload regression
was first run against the old implementation and failed as expected.

Focused coverage includes authoritative callback
payloads, absent versus unmatched smoke, raw transition provenance, trace
immutability/bounds/reset, late end evidence remaining censored, and a bounded
missing-end wait. Existing unknown-history, same-tick ordering, missing phases,
death/teleport and identity/bank guards remain required.

## Next live evidence

Close the development client, update `n2/nex-owned-advice`, and restart. During
the next ordinary fresh-start attempt, keep capture enabled. If practical, stay
in the arena for up to 12 seconds after death so diagnostics can see a delayed
end signal; leaving sooner still flushes safely. Share only the top of attempt
details, including **Smoke line observed**, **Barrier readings** and the expanded
**Capture signal trace**. Equipment/inventory screenshots are unnecessary.

Use that real sequence to decide whether the correct start anchor is smoke or
another observed signal and whether dying/end correlation needs a different
contract. Do not claim that this diagnostic delivery resolves the live failure.
