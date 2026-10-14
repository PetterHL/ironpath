#!/usr/bin/env python3
"""Reproduce acquisition coverage from the bundled active route/method contracts; no network."""
import json
import argparse
from pathlib import Path
parser=argparse.ArgumentParser()
parser.add_argument('--output', default='docs/validation/integrated-acquisition-coverage.md')
args=parser.parse_args()
root=Path(__file__).resolve().parents[1]
c=json.loads((root/'src/main/resources/com/ironpath/catalog.json').read_text())
decisions={d['id']:d for d in c['decisions']};profiles={p['id']:p for p in c['effortProfiles']}
models={m['route']:m for m in c['acquisitions']};paces={p['activity']:p for p in c['automaticPaces']}
lines=['# AD1b active acquisition coverage','',
f"Generated from catalogue schema {c['schemaVersion']} / knowledge {c['knowledgeVersion']} by `python tools/ad1b_coverage.py`. This is evidence, not a second backlog; follow-up ownership and sequence are in TASKS AD1b-B1–B4. Original AD1b-coverage.md retains the pre-integration evidence.", '',
'Rows cover every active outcome × route × reviewed method. A1 = conditional component odds; A2 = component hours under an eligible pace scenario; observed A3 and complete-route hours are unavailable throughout. Automatic availability depends on the full predicates below, access, remaining components and source support. A benchmark is not a personal throughput prediction. No route means or quantiles are added across rows.', '',
'| Outcome | Route | Activity / qualitative method | Probability/state model | Automatic hours | Manual hours | Observed / full route | Specific exclusions or gap / owner |',
'|---|---|---|---|---|---|---|---|']
active=[u for u in c['upgrades'] if u['status']=='reviewed']
for u in active:
 for route in u['routes']:
  d=decisions[route];prof=profiles[d['pursuit']['profile']];m=models.get(route);p=paces.get(d['activity'])
  for method in prof['methods']:
   probability='A0: unavailable' if not m else f"A1: `{m['id']}`; {m['numerator']}/{m['denominator']} per roll × {m['rolls']}; up to {m['requiredSuccesses']} reward(s)"
   auto='Unavailable' if not m or not p or (d['activity']=='zulrah' and method['id']=='mage-only') else f"A2 if `{p['id']}` predicates hold"
   manual='Unavailable' if not m else 'A2 if access and a compatible method are established; same activity/mode'
   if m:
    gap='Component only; storage counts are lower bounds; allocation, quest/training/supplies/assembly excluded. '
    if d['activity']=='zulrah':gap+='First magic-fang method with a Seas trident has odds/manual only; automatic scenario needs stronger compatible mage/range switches. '
    if d['activity']=='muspah':gap+='Automatic pace is tbow-only; Bowfa/switch pace awaits compatible benchmark. '
    gap+='AD1b-B1 (remaining method coverage), B4 (excluded route work)'
   else:gap=prof['gap']+' '+prof['gapOwner']
   lines.append('| '+' | '.join([f"`{u['id']}`",f"`{route}`",f"{d['activity']} / `{method['id']}`",probability,auto,manual,'Unavailable',gap])+' |')
lines+=['','## Automatic scenario predicates and evidence','',
'Each scenario is a successful-repeat-hour assumption using a documented hourly guide benchmark. Compatible gear/method selection uses existing reviewed strategy records. No numerical gain or level multiplier is applied. Failures, initial learning, supply acquisition and material extra downtime are excluded, rather than silently assigned a fabricated probability. Qualitative method adequacy may remain supported when a more specific hourly benchmark is unavailable.','',
'| Scenario | Rate / hour | Essential applicability | Evidence source ID |', '|---|---|---|---|']
for p in c['automaticPaces']:
 sid=next(s for s in p['sources'] if s.startswith('ad1b-'))
 lines.append(f"| `{p['id']}` | {p['minimumPerHour']:g} | `{json.dumps(p['when'],separators=(',',':'))}` | `{sid}` + linked strategy/item records |")
lines+=['','## Retained secondary decisions and shared rewards','',
'Every active secondary decision retains its explicit EP1 qualitative contract. Hours below are not newly inferred from a training level, owned scroll or one-hop reward relationship.','',
'| Decision / reward | Activity / method | Numeric hours | Specific remaining gap |','|---|---|---|---|']
for d in c['decisions']:
 if d['status']=='reviewed':
  p=profiles[d['pursuit']['profile']]
  for method in p['methods']:lines.append(f"| `{d['id']}` | {d['activity']} / `{method['id']}` | No secondary-action total | {p['gap']} {p['gapOwner']} |")
for reward in c['activityRewards']:
 p=profiles[reward['profile']]
 lines.append(f"| shared `{reward['id']}` | {reward['activity']} | No shared-reward total | {p['gap']} {p['gapOwner']} Shared benefits and times are not added. |")
positive={m['route'] for m in c['acquisitions']}
positive_outcomes={u['id'] for u in active if any(r in positive for r in u['routes'])}
routes={r for u in active for r in u['routes']};families={decisions[r]['activity'] for r in routes}
lines+=['','## Delivery counts and limits','',
 f"Active inventory: **{len(active)} outcomes, {len(routes)} routes, {len(families)} acquisition families**. Conditional component probability models: **{len(positive_outcomes)}/{len(active)} outcomes and {len(positive)}/{len(routes)} routes**. Five source-dependent automatic scenarios serve five families. Nine outcomes can show automatic component hours on applicable, still-acquiring accounts; the magic-fang route retains odds/manual hours because the promoted default requires already stronger magic gear. Counts are release evidence, never catalogue limits.", '',
 'Every unsupported row retains a specific blocker and TASKS owner. All full-route totals and observed pace remain unavailable. A0 fallback and this matrix do **not** complete AD1b; B1–B4 and real-client/account acceptance remain open.']
(root/args.output).write_text('\n'.join(lines)+'\n')
print(f'{len(active)} outcomes / {len(routes)} routes / {len(families)} families; {len(positive_outcomes)} modelled outcomes')
