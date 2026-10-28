# One-copy troubleshooting

28 October 2026. Development build **0.33.3**, adapter **nex-public-13**, on PR #59
after `9e3face`. This delivery improves diagnosis; it does not establish or fix the
cause of an unavailable live excluded interval.

## Export contract

The Copy report now action emits troubleshooting schema 1: a current immutable
bounded session overview followed by the existing frozen schema-2 selected report
and its schema-5 evidence block. Every visible retained interval has a report-local
reference, outcome, allowlisted exclusion, typed capture origin, relative signal
offsets, state coverage and truncation flags. The latest and selected references
are explicit. Counts describe retained capture intervals, not player actions;
evicted records are explicitly unavailable. No record selection is necessary.

Origin is captured when the adapter starts: smoke, an observed barrier transition
or polling an active arena, together with typed form/barrier and tick. A later smoke
anchor preserves that original origin. Eligibility and numerical support do not
change. Legacy/synthetic summaries without origin remain UNKNOWN.

The overview also carries client-thread-captured runtime bank/arena/barrier,
identity-readable, capture and travel-pause facts. Copying uses cached immutable
scalars, never live RuneLite reads on Swing EDT. Build resources carry version,
Git revision and CLEAN/DIRTY/UNKNOWN provenance obtained at build time; the plugin
never runs Git. Tick offsets are relative, with no account/world identifiers,
timestamps, raw messages/trace, equipment or containers in the overview.

The current overview and frozen selected calculation have independent lifetimes.
Background renders preserve a focused/selectable copy; explicit copying refreshes
the overview. Selected evidence must still match the retained summary, annotation
revision and account scope. Stale/evicted/revised selection is withheld. Paused or
unverified sessions expose runtime facts but no record counts, reasons or selected
evidence. Logout/reset clear memory; an already copied string remains self-contained.
No automatic file saving, persistence or upload is added. Overview and selected
sections have explicit size bounds and fail closed if exceeded.

## Synthetic verification

Regressions cover a complete selected record alongside two excluded intervals in
one copy, typed origins across smoke anchoring and spawning/boundary callbacks,
fresh overview versus frozen calculation, unchanged clipboard-failure selection
through background updates, all 50 retained records, eviction, changed annotations,
scope changes, paused privacy and clear-after-logout behavior. A production plugin
lifecycle test distinguishes a paused outbound trip with no visible bank from a
verified bank return in copied runtime readings. Arbitrary reason/phase/trace and
account sentinels cannot enter the overview.

The existing native frozen-history replay now checks that one production Copy
action includes a newer excluded interval while preserving an older selected
calculation. It retains the 225 x 600/full-height views at fonts 12 and 20.
Full performed command results and current CI artifact are recorded on
[PR #59](https://github.com/PetterHL/ironpath/pull/59).

Follow [the troubleshooting procedure](../TROUBLESHOOTING.md). Synthetic checks
validate the export and lifecycle contract; live exclusion cause and live acceptance
remain open. No further kill is required to review this implementation.
