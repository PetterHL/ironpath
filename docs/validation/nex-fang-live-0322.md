# Matched Nex client check — 0.32.2

26 October 2026. One supplied LIVE_PUBLIC_CLIENT report and native screenshot
from **0.32.2 / RuneLite 1.13.2**, implemented in `ce9731a`. The user separately
confirms that Tracking kill, advancing time and phase worked during this kill.
This records scoped live evidence; no runtime/model change or new mapping
acceptance is implied. Raw reports and screenshots remain outside Git.

## Accepted observations

- The report and screenshot both show a complete **294.6-second** kill. The
  report retains 491/491 sampled ticks, a continuous capture and complete export,
  with no truncation, correlation limit or export omissions. It exports 2,201
  useful callbacks from 2,333 retained callbacks; 132 unchanged polls are omitted.
- The cleaned main view is visible. The user's confirmation accepts the active
  status/time/phase behavior for this run; the finished screenshot correctly
  shows Waiting for Nex. Broader reload/bank/identity acceptance is unchanged.
- The two included Fang samples share gear, offensive levels, prayer flags and
  available comparison inputs despite different positive HP/Prayer readings and
  spec energy. They form one group with **98 independent alternatives**, rather
  than duplicating the comparison rows. This confirms the corrected grouping
  on these two live inputs, not universal attack attribution.
- Every pair uses those same two action references and fixes the Fang weapon.
  Independent enumeration of paired expected damage at every positive integer
  HP breakpoint reproduces all 98 exported ranges within `5e-14`. Action and
  state references resolve. There are 3 carried and 95 hypothetical alternatives;
  22 have strictly negative bounds, 16 are zero, 59 overlap/touch zero and one
  has strictly positive bounds.
- That positive alternative changes exact Dragon defender **24143** to Avernic
  defender **22322** on the supported owned baseline. Its total gain is
  **0.01590793250575384 to 0.4374681439082124 expected damage** for the two
  included attacks. The screenshot's outward-rounded **+0.01 to +0.44** matches.
  This is neither a whole-kill estimate nor an observed damage increase.

## Coverage that remains missing

The report has 30 animation-9471 callbacks with captured Fang equipment and
**20 provisional attack inferences**. Only two inferred attacks enter the
supported Shadow/Fear the shadow comparison. Of the other inferred attacks,
**14** have unsupported windows (unknown Smoke, Blood siphon/sacrifice or Ice
prison) and **4** have unsupported Zaros target forms. These are current support
gates, not 18 verified bad or missed attacks by the player.

The other ten Fang callbacks remain ambiguous: seven movement flags, two
changed offensive-stat reconciliations and one missing target. The two changed
reconciliations include real visible Attack/Strength changes, rather than the
previous incidental HP/Prayer-only rejection. One additional animation-9471
callback was captured with Zaryte crossbow equipment and is rejected for weapon/
style support. Together these explain the preview's 31 candidates and 29
exclusions without treating every 9471 callback as an executed Fang attack.

Food messages are **3 FOOD_SHARK and 1 FOOD_UNKNOWN**. No typed manta-ray event
is present, so this report does not validate that mapping. There are **zero typed
DC success messages**, despite three retained DC effect observations and active/
cooldown readings. The ENGINE extension has not established the missing live
message source; wording, channel and message availability remain unresolved.
No cast/proc/expiry count or consumption ledger is inferred from these readings.

All 161 retained own impacts include 49 zeros; 155 action links remain ambiguous
or unmatched, and the six unique temporal candidates remain unvalidated. Recorded
damage minus healing again matches Nex 3,400 plus four 500-HP minions, totaling
5,400. The recorded hitsplat ratio remains diagnostic and Damage contribution
stays Unverified.

## Next work and verification boundary

Use this existing report to review ordinary-window coverage and special-window
end boundaries before widening the Fang sample. UNKNOWN must not be relabelled
NONE merely to admit more attacks. Diagnose the missing typed DC success source
separately; the ordinary equipment result does not depend on a solved DC plan.
A new complete kill is not required to begin that review.

The independent report audit and relative documentation links were checked.
No runtime files change and no gameplay mapping is promoted. Existing automated
results remain those of implementation `ce9731a`: 449 tests, 14 DEV1 scenarios /
1,181 checks / 252 PNGs, and pinned/normal CI success linked from PR #57. Full
finite-plan automatic gates, base-stat assumptions and N4 boundaries remain.
