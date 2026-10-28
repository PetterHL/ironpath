# Nex report selection and live equipment check

28 October 2026. Development build **0.33.2**, on PR #59 after `f17380b`.
Adapter `nex-public-12`, calculation models and capture eligibility are retained.

## Supplied 0.33.1 evidence

One report is PAUSED and exports no session evidence. A subsequent report on
RuneLite 1.13.4 is READY and contains a **325.8-second complete kill**: 543/543
ticks, continuous sampling, smoke at offset 0, death/dying at 543 and arena end
at 551. No capture/export truncation or omitted callbacks is reported. This
accepts one available completed result after revalidation, not universal bank
or spawning-lifecycle acceptance. Reports and screenshots remain outside Git.

The equipment export includes **three provisional Fang samples**, all in one
Shadow / Fear the shadow group, and **105 comparisons**. Every comparison uses
the same three action references and observed setup with Masori chaps (f). An
independent audit of exported parameters reproduces all 210 before/after damage
means as attack count times accuracy times the uniform Fang damage mean. All
105 exported gains and percentage ratios agree with those means; action references
resolve and the weapon remains fixed. Equipment parameters and live attack/window
mappings are not independently promoted by this consistency check.

The screenshot's Bandos tassets +1.9%, Avernic defender +2.9%, Torva platelegs
+1.9%, Torva full helm +1.9% and Masori body (f) -1.9% match the exported signed
changes and rounding. The first four changes raise max hit from 46 to 47; the
Masori body alternative retains max hit 46 while reducing expected damage.
This remains a comparison on the three included samples, not a whole-kill gain.
There are 44 candidates and 41 exclusions: 23 ambiguous attacks, 12 windows,
two unknown phases and four unsupported targets. Contribution/time/supplies
remain independently unavailable.

An Incomplete attempt notice remains in the supplied screenshot. Both the
copied report and expanded Calculation details are still selected on the complete
kill, so the excluded record's actual cause has not been supplied. The 0.33.1
spawning correction cannot yet be accepted as resolving that live notice.

## Correction

Diagnostics now shows the latest attempt's outcome and allowlisted exclusion
reason separately from its selected frozen report. When these refer to different
records, **Use last attempt** explicitly selects the latest record for copying
and Calculation details. This removes the need to navigate through another section
to obtain a short exclusion reason. The Report record label identifies the
selected record independently of the latest-attempt status. Hidden sessions expose neither the reason
nor an actionable record selector.

An end-to-end replay also reproduced a selection overwrite: rebuilding an older
open base-stat comparison unconditionally selected that comparison's report
again. Initial control reconstruction now preserves an independently selected
report. Deliberate state/pair/filter changes still select their own comparison.
Selected calculations and whole-capture exports remain frozen across unrelated
refreshes; the latest-status card does not alter their contents.

State-buffer and event/time-limit reasons are now in the exact report allowlist,
instead of becoming unclassified capture exclusions. Arbitrary reason text stays
excluded. This improves diagnosis without changing capture bounds or outcomes.

## Verification and limits

Three targeted regressions fail before the change: latest-attempt status/selection,
hidden/stale selector behavior and fixed limit-reason export. The extended real
plugin replay caught the older-comparison overwrite and requires explicit selection
to export the censored record, preserving the earlier complete report until then.
It renders full-height and 225 x 600 latest-status/selected-record views at fonts
12 and 20 in `nex-frozen-history`, through the existing native Swing harness.

Full commands, counts and current CI/artifact links are recorded on
[PR #59](https://github.com/PetterHL/ironpath/pull/59). The latest-attempt cause in
the supplied live session remains open until that record's reason is available;
this change does not infer it or reclassify an old exclusion as complete.
