# Troubleshooting

## One copy from the client

When a problem appears, open **Diagnostics → Copy report now** once while the
session is visible. Send that text. Any currently selected kill is fine; do not
switch records or open Calculation details just to obtain an exclusion reason.
If the clipboard fails, the same text remains selectable below the button.

The overview covers all retained capture intervals (up to 50), including exclusions,
their original start source/form and signal offsets. These are intervals created
by the plugin; several intervals do not prove several player attempts. The selected
kill/calculation is frozen separately. An explicit new copy refreshes the overview;
background updates preserve already selectable text and its selection/scroll.

A copied report is self-contained: logging out afterwards does not invalidate it.
Logout, identity/world changes, disable and Clear session still discard the original
in-memory evidence. A report copied after clearing cannot recover it. During a
pause, runtime bank/identity-readable/barrier/arena readings explain the current
state, but retained session evidence remains hidden until revalidation. There is
no background file logging, telemetry or automatic upload.

## Development procedure

1. Read the supplied report and earlier evidence first. Record the build version,
   source revision, CLEAN/DIRTY/UNKNOWN provenance, adapter/client versions, status
   and overview completeness. A DIRTY build is not an exact committed source tree.
2. Separate the observed symptom, the actual interval outcome/exclusion and current
   runtime state. Compare local interval references and relative offsets. A report
   on a selected complete kill does not explain another interval's exclusion;
   use the overview. PAUSED is a visibility state, not a failed-kill verdict.
3. State what is observed, what is a hypothesis and what is missing. Do not infer
   the player's actions from interval counts. Do not label a possible cause fixed
   merely because a synthetic test produces similar counts or wording.
4. Reproduce the specific signal/order/guard in the existing offline adapter/plugin
   harness. Establish an independent failing expectation before changing behavior.
   If the exact cause is unknown, improve bounded diagnostics and test that export
   first. Do not relax eligibility or identity guards to make the symptom disappear.
5. Validate the scoped change with affected regressions, both required builds and
   native 225px views at fonts 12/20. Inspect generated reports for completeness,
   frozen selection, bounds and privacy. Report implementation acceptance separately
   from unperformed live acceptance.
6. Request client work only if existing reports, code and controlled offline checks
   cannot answer a material question. Specify the exact missing fact and smallest
   observation in one instruction. Do not default to another complete kill, repeated
   screenshots or a sequence of record/menu changes. Never require the user to stay
   logged in after copying a sufficient report.

Keep raw user reports/captures outside Git. PR descriptions and public validation
records can describe implementation and synthetic tests; publishing private live
diagnostic material requires explicit authorization. No automatic upload follows
from Copy report now.
