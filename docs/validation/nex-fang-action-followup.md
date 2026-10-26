# Fang input reconciliation and action-message follow-up

25 October 2026. Development build **0.32.1**, continuing the open N3-ADVICE
[PR #57](https://github.com/PetterHL/ironpath/pull/57). Adapter `nex-public-11`,
signals `nex-actions-2`, clock `nex-fang-clock-2`, advice `nex-fang-advice-2`;
the frozen JSON shape remains `nex-evidence-5`. Full-plan automatic gates remain
unchanged. This is a correction to the provisional preview, not mapping promotion.

## Supplied client evidence

One supplied LIVE_PUBLIC_CLIENT report from build 0.32.0 / RuneLite 1.13.2 is
COMPLETE, exportComplete=true, with no action/observation truncation, correlation
work limit or export omissions. It contains 48 Fang animation callbacks, 13
provisional clock inferences and one admitted ordinary-window sample. The frozen
preview contains 98 independent package pairs. Their paired bounds were checked
independently by enumerating every integer HP breakpoint; all references resolve
and both weapon IDs remain Fang. This accepts the bounded export and conditional
calculation path for this report, not the completeness of attack attribution.

The user separately supplied a native client screenshot showing the provisional
Fang card with one attack and confirmed use of brews, manta rays and Death Charge.
The screenshot's displayed kill duration differs from the report; its numerical
gain is therefore not cross-validated against that report. No screenshot, raw
report, account identity, container dump or exact gameplay trace is committed.

Of 18 SETTLED_INPUTS_CHANGED rows, the exported fields show 14 Prayer-point-only
changes and one player-HP-only change. The remaining rows include offensive
Attack/Strength, target-form or offhand changes. The original comparator compared
the whole visible-level map, so incidental resource changes could reject an
otherwise stable ordinary Fang sample. The export omits some runtime prayer
flags, so its visible field differences do not prove that every other original
runtime input was identical.

The report also retains six FOOD_UNKNOWN success signals and no typed DC success
message, alongside DC effect/active/cooldown evidence. Manta ray was outside the
old exact food allowlist. The user confirms casting DC, but the old export does
not retain the type/text of rejected messages; it cannot prove which channel
carried a missing success message. No energy delta or effect is promoted to a
cast, proc or expiry.

## Corrections and boundaries

- Reconcile known positive player HP and remaining Prayer independently of
  ordinary Fang offence. Keep zero/unread transitions guarded. Attack/Strength,
  base levels, active prayer flags, full slots, style, special selection,
  target/form/phase/window and every existing motion/order/readiness guard still
  apply. The original callback state remains frozen; no later input replaces it.
- Group admitted attacks by shared offensive inputs and equip availability,
  rather than the full CombatState resource snapshot. Identical actual slots,
  base equip levels, visible Attack/Strength, active prayer flags, style,
  inventory readability, positive supported carried gear and switch space must
  agree. Bank/preferences remain frozen per evaluation. HP/Prayer polls, energy,
  dose counts and quiver counters cannot fragment a fixed-Fang sample. Newly
  available gear cannot leak back into earlier attacks. Paired gains still use
  the same action sample and all-positive-HP domain, with no trajectory inference.
- Add the exact **You eat the manta ray.** mapping as FOOD_MANTA_RAY, using the
  reviewed ordinary-food +3-tick delay. It shares the shark/karambwan ordering,
  ambiguity and expired-clock guards. Generic/unsupported food stays unknown;
  brews are not eat signals. No manta quantity, healing, consumption ledger or
  finite supply-plan support is added.
- Admit exact normal/upgraded DC success text from the public **ENGINE** game
  message channel as ENGINE_MESSAGE. On that channel, food, phase text and
  unrelated messages remain excluded. Player, console and dialogue text cannot
  create the signal. Existing GAMEMESSAGE/SPAM behavior remains. ENGINE support
  is a provisional source correction, not a proven diagnosis of the missing
  live messages or a validated cast/proc/expiry mapping.
- Freeze clock/action model versions in the existing evaluation revision. Keep
  the eight full finite-plan gaps, unknown HP/readiness/stock, unchecked declared
  scenarios and N4 boundaries intact.

## Pinned source review

No external implementation, dataset or UI was copied.

- [RuneLite 1.13.2 ChatMessageType](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-api/src/main/java/net/runelite/api/ChatMessageType.java)
  explicitly defines ENGINE as a message sent by the game engine. Reviewed file
  SHA-256: `04381e15f28cd5fbb04684db90fdfa2233ca0fa8e60fe127089ae9a9c8c2ef69`.
- [Attack Timer Metronome, pin 78c5d72](https://github.com/ngraves95/attacktimer/blob/78c5d72aa11490debc3cc71b8a87dcefde7353cb/src/main/java/com/attacktimer/AttackTimerMetronomePlugin.java)
  corroborates the ordinary-food three-tick class and separately named fast/slow
  exceptions. Only manta ray is added to IronPath's exact allowlist; no generic
  three-tick assumption is introduced. Reviewed SHA-256:
  `0b8272fab142198617a24079994cfc117bbc5a92f0ebf8ca706aa6cd66b484e5`.
- [Death Charge Reminder, pin 99d99c6](https://github.com/InfernoStats/death-charge-reminder/blob/99d99c6a5d1c0fb247f007d259393dda2253b2cd/src/main/java/com/infernostats/DeathChargeReminderPlugin.java)
  recognizes the exact normal/upgraded success text without limiting its message
  type. IronPath keeps a smaller explicit game-source allowlist. Reviewed SHA-256:
  `b560077fb881617e09409bb33ed280bb52cbf106f89e906bc162beb0becd0c22`.

## Verification

Independent synthetic regressions cover positive HP/Prayer changes across both
callback reconciliation and frame restart, real offensive/active-prayer changes,
zero/unread transitions, unchanged callback provenance, grouping across resource
polls and isolation when equip availability changes. Manta tests cover +3 delay,
same-tick ambiguity, expired cooldown, typed export and unsupported foods. Public
observer tests admit exact ENGINE DC success while rejecting player/console/
dialogue, ENGINE food and ENGINE phase messages. The existing DEV1 N3 replay
now sends its typed upgraded DC message through ENGINE and the real worker/report.

A local diagnostic projection reconstructed only the exported scalar fields.
Before the correction it reproduced all 981 exported reason/confidence labels.
The revised projection changes 15 resource-only rejections to provisional
inferences (13 to 28 in total); ordinary-window candidates rise from one to two.
This is **not a full kill replay**: inventory quantities and some prayer flags
are absent. It cannot reconstruct complete owned-package availability or prove
those inferred actions were executed. Old FOOD_UNKNOWN rows remain unknown and
missing DC messages are not invented.

Required sequential final commands:

```sh
./gradlew -PruneLiteVersion=1.13.2 test build offlineReview --no-daemon
./gradlew test build --no-daemon
```

Both final commands **passed** with Temurin JDK 11.0.32.1+1 / Gradle 8.10.
Pinned RuneLite **1.13.2** completed in **3m 1s**; normal `latest.release` resolved
to **1.13.4** and completed in **2m 6s**. Both ran **447 tests / 49 suites**, with
no failures, errors or skipped tests. All **14 DEV1 scenarios / 1,171 checks /
248 PNGs** pass. Parsed both production copied reports to check clock/action/
advice versions and the typed ENGINE source. Changed whitespace and relative
Markdown file targets pass.

The delivery PR owns the exact revision and current CI/artifact links. No new
gameplay check is claimed for 0.32.1; controlled attack/eat/cast and Nex-window
acceptance remain open.
