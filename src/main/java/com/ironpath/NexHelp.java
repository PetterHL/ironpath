package com.ironpath;

import javax.swing.JPanel;

/** Common technical explanation, opened explicitly in Diagnostics. No account data. */
final class NexHelp
{
	static void populate(JPanel panel)
	{
		for(String explanation:new String[]{
			"Damage contribution uses observed Nex + named-minion hitsplats, not server loot credit. Timing runs from observed smoke to Nex death/dying with corroborating completion.",
			"Independent state comparisons are alternatives, not additive gains or a whole-kill ranking. Complete packages are calculated together. A partial supported-domain search does not establish the global best owned setup.",
			"Equipped gear means the changed items are already worn in that captured state; inventory gear is carried, and bank gear is conditional next-trip preparation. Bank presence does not establish usable quantities or joint packing. Supplies are assumptions. Healing and survival are unmodelled.",
			"The pilot keeps Lightbearer equipped.",
			"Pilot planning scope: Trio MP2, one kill per trip, with upgraded Death Charge assumed unless changed. Worn roles are evidence, not confirmed attacks. Sampled energy or charge changes cannot identify spec weapons, casts or procs.",
			"Gear locks and manual facts apply to future comparisons. Completed results retain frozen inputs. Observed ownership/unlocks take precedence; a manual fact does not establish charges or usability. Empty is an explicit slot choice.",
			"Team & method has separate P2, team-type and team-size declarations. Future defaults stay unconfirmed for each kill. Per-field confirmation affects only the selected kill unless Use for future is selected. Count includes you; variable/unknown counts are never exact. Per-field sources are retained in Calculation details.",
			"Plan specs & gear is an optional hypothetical plan for the inspected calculation. It changes neither captured evidence nor saved gear/method. Energy and player HP may start from captured readings; reusing them for a window is an assumption. Blank numeric inputs remain unknown. Blank cast/death tick lists mean no such events.",
			"Ticks are offsets from the hypothetical window; its end is excluded. Enter distinct increasing comma-separated offsets. First special tries the supported Voidwaker/ZCB spec at the first offset. Spec whenever energy allows tries at every offset. Rejected specs fall back to a normal attack on that tick; other weapons use normal attacks.",
			"Scenario choices must be explicitly selected: legal target access at attack offsets; attack-ready and cast-ready at start; no active Death Charge; fixed Lightbearer; sampled/hypothetical gear and stock usable; runes and legal cast opportunities for listed casts; distinct eligible listed deaths; constant supplied HP and captured phase/window; attack offsets including spell/switch delays. An unselected choice leaves the shared readiness declaration unknown and dependent outputs unavailable.",
			"Equal-tick plan order is cast, eligible death, spec, normal fallback; natural restoration follows the chosen order. Every accepted projectile is spent in the no-retrieval branch; charged-quiver shots spend splinters while stock lasts. Hypothetical edits require exact compatible equipment, quantities, locks and packing, including offhand/ammo changes.",
			"Conditional launch damage is evaluated at supplied HP, without a simulated HP trajectory, best-spec recommendation, survival estimate or team-time claim. Unknown phase windows, Zaros overhead/stat changes and other target transforms keep dependent damage unavailable. Matching vulnerable minion windows use their own stats and non-Magic flat armour. Detailed plan gaps, model versions and inputs are retained in the explicitly copied/selectable plan report.",
			"Food & potions declares a shared finite supply schedule using frozen carried stock: shark, karambwan, brew, restore, super combat and ranging doses. No item loss is attributed to consumption. Eating/drinking timers and only these level effects are assumed; natural decay, other drains/boosts and healing/survival are not inferred. An outstanding attack cooldown gains the food delay; idle eating does not manufacture lost attacks. Same-tick order is normal food, potion, karambwan, casts/deaths, then attacks. HP remains separately supplied per launch.",
			"Unit costs are explicit GP per projectile, splinter, food or single potion dose. Missing prices keep scoped cost unavailable, never zero. Costs refer only to the specified ammunition/charge branch and declared supplies, excluding other gear upkeep, spell runes, acquisition and full-trip cost. Acquisition routes reuse catalogue evidence without inferring completed components, access, pace, drop odds or a full-item ETA.",
			"Synthetic previews and unit tests do not establish live event mappings, cache sprites, DPI, focus, keyboard, clipboard, scroll behavior or player usefulness. Live gameplay acceptance remains open."
		}) { PanelWidgets.text(panel,explanation); }
	}
}
