package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.ComparisonResult;
import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.advisor.NexOwnedEvaluator;
import com.ironpath.advisor.NexOwnedFixtures;
import com.ironpath.advisor.NexOwnedResult;
import com.ironpath.dev.OrderedReplay;
import com.ironpath.dev.PanelPreview;
import com.ironpath.dev.ReviewReport;
import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.nex.NexCapture;
import com.ironpath.nex.NexSession;
import com.ironpath.nex.NexSummary;
import com.ironpath.preferences.NexMethod;
import com.ironpath.preferences.Preferences;
import java.awt.Font;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import net.runelite.api.*;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.gameval.VarPlayerID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** The first DEV1 scenario. All runtime boundary values are explicitly synthetic. */
final class OfflineNexScenario
{
	private int tick, barrier = 2, energy = 1000, charge = 2;
	private boolean special;
	private Map<String, Integer> worn = NexOwnedFixtures.ranged();
	private final Map<Integer, Long> carried = new TreeMap<>();
	private final Client client = mock(Client.class);
	private final Player local = mock(Player.class);
	private final WorldView world = mock(WorldView.class);
	private final NPC boss = mock(NPC.class), minion = mock(NPC.class);
	private final NexSession session = new NexSession();
	private final KnowledgeCatalog catalog;
	private final NexObserver observer;
	private final Preferences prefs = Preferences.EMPTY.nex(NexMethod.PILOT.lock("melee/shield", 12954));
	private final List<String> trace = new ArrayList<>();

	OfflineNexScenario() throws Exception
	{
		catalog = KnowledgeCatalog.load(new Gson());
		Thread clientThread = Thread.currentThread();
		when(client.isClientThread()).thenAnswer(i -> Thread.currentThread() == clientThread);
		when(client.getTickCount()).thenAnswer(i -> tick);
		when(client.getVarbitValue(anyInt())).thenAnswer(i -> {
			int id = i.getArgument(0);
			if (id == VarbitID.NEX_BARRIER) { return barrier; }
			if (id == VarbitID.ARCEUUS_DEATH_CHARGE_ACTIVE) { return charge; }
			if (id == VarbitID.ARCEUUS_DEATH_CHARGE_COOLDOWN) { return 0; }
			if (id == VarbitID.WORN_ITEMS_DIZANAS_QUIVER_CHARGED) { return 1; }
			if (id == VarbitID.CHARGES_DIZANAS_QUIVER_QUANTITY) { return 200; }
			throw new IllegalArgumentException("Unspecified synthetic varbit " + id);
		});
		when(client.getVarpValue(anyInt())).thenAnswer(i -> {
			int id = i.getArgument(0);
			if (id == VarPlayerID.COM_MODE) { return 1; }
			if (id == VarPlayerID.SA_ENERGY) { return energy; }
			if (id == VarPlayerID.SA_ATTACK) { return special ? 1 : 0; }
			if (id == VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO) { return 21326; }
			if (id == VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO_AMOUNT) { return 500; }
			throw new IllegalArgumentException("Unspecified synthetic varp " + id);
		});
		when(client.getRealSkillLevel(any(Skill.class))).thenReturn(99);
		when(client.getBoostedSkillLevel(any(Skill.class))).thenAnswer(i -> {
			Skill skill = i.getArgument(0);
			return skill == Skill.ATTACK || skill == Skill.STRENGTH ? 118 : skill == Skill.RANGED ? 112 : 99;
		});
		when(client.isPrayerActive(any(Prayer.class))).thenAnswer(i -> {
			Prayer prayer = i.getArgument(0);
			return worn.get("weapon") == 20997 ? prayer == Prayer.RIGOUR : prayer == Prayer.PIETY;
		});
		when(client.getLocalPlayer()).thenReturn(local);
		when(client.getTopLevelWorldView()).thenReturn(world);
		when(world.getId()).thenReturn(-1);
		when(local.getWorldView()).thenReturn(world);
		when(local.getName()).thenReturn("synthetic member one");
		when(local.getInteracting()).thenReturn(boss);
		when(local.isDead()).thenReturn(false);
		when(boss.getWorldView()).thenReturn(world); when(boss.getIndex()).thenReturn(1);
		when(boss.getId()).thenReturn(NpcID.NEX);
		when(minion.getWorldView()).thenReturn(world); when(minion.getIndex()).thenReturn(2);
		when(minion.getId()).thenReturn(NpcID.NEX_SHADOWMAGE);
		IndexedObjectSet<NPC> npcs = mock(IndexedObjectSet.class);
		when(world.npcs()).thenAnswer(i -> npcs); when(npcs.stream()).thenAnswer(i -> List.of(boss, minion).stream());
		List<Player> roster = new ArrayList<>(); roster.add(local);
		for (String name : List.of("synthetic member two", "synthetic member three"))
		{
			Player member = mock(Player.class); when(member.getName()).thenReturn(name);
			when(member.getWorldView()).thenReturn(world); roster.add(member);
		}
		IndexedObjectSet<Player> players = mock(IndexedObjectSet.class);
		when(world.players()).thenAnswer(i -> players); when(players.iterator()).thenAnswer(i -> roster.iterator());
		ItemContainer equipment = mock(ItemContainer.class), inventory = mock(ItemContainer.class);
		when(client.getItemContainer(InventoryID.WORN)).thenReturn(equipment);
		when(client.getItemContainer(InventoryID.INV)).thenReturn(inventory);
		when(equipment.getItem(anyInt())).thenAnswer(i -> equipment(i.getArgument(0)));
		when(inventory.getItems()).thenAnswer(i -> inventory());
		Map<Integer, String> names = catalog.itemNames();
		when(client.getItemDefinition(anyInt())).thenAnswer(i -> {
			int id = i.getArgument(0); ItemComposition item = mock(ItemComposition.class);
			when(item.getName()).thenReturn(names.getOrDefault(id, id == 27690 ? "Voidwaker" : "Synthetic item " + id));
			return item;
		});
		NexOwnedFixtures.ranged().values().stream().filter(id -> id > 0).forEach(id -> carried.put(id, 1L));
		NexOwnedFixtures.melee().values().stream().filter(id -> id > 0).forEach(id -> carried.put(id, 1L));
		carried.put(27690, 1L); carried.put(11212, 200L); carried.put(24780, 1L);
		session.select("offline-synthetic-profile", 301);
		observer = new NexObserver(client, catalog, session);
		observer.method(prefs.nex);
	}

	private Item equipment(int index)
	{
		for (EquipmentInventorySlot slot : EquipmentInventorySlot.values())
		{
			if (slot.getSlotIdx() != index) { continue; }
			String key = slot == EquipmentInventorySlot.AMULET ? "neck" : slot == EquipmentInventorySlot.GLOVES ? "hands"
				: slot == EquipmentInventorySlot.BOOTS ? "feet" : slot.name().toLowerCase(java.util.Locale.ROOT);
			int id = worn.getOrDefault(key, 0); return id > 0 ? new Item(id, 1) : null;
		}
		return null;
	}
	private Item[] inventory()
	{
		return carried.entrySet().stream().filter(e -> !worn.containsValue(e.getKey()))
			.map(e -> new Item(e.getKey(), Math.toIntExact(e.getValue()))).toArray(Item[]::new);
	}
	private void hit(NPC actor, boolean mine, int amount)
	{
		Hitsplat hit = mock(Hitsplat.class);
		when(hit.isMine()).thenReturn(mine); when(hit.isOthers()).thenReturn(!mine);
		when(hit.getHitsplatType()).thenReturn(mine ? HitsplatID.DAMAGE_ME : HitsplatID.DAMAGE_OTHER);
		when(hit.getAmount()).thenReturn(amount);
		HitsplatApplied event = new HitsplatApplied(); event.setActor(actor); event.setHitsplat(hit);
		observer.hit(event); observer.hit(event); // Same object duplicate must not double-count.
	}

	void run(Path output, ReviewReport report) throws Exception
	{
		report.section("Scenario and provenance", "`nex-complete` · Nex / Trio MP2 / one kill per trip.\n\n"
			+ "Reported ranged/melee equipment comes from NexOwnedFixtures; no private account dump. "
			+ "All levels (real 99; visible attack/strength 118, ranged 112), prayers (Rigour/Piety), style button 1, "
			+ "three-person visible roster, event order, HP-independent hits, stock and timing are SYNTHETIC. "
			+ "Voidwaker is a synthetic spec-candidate switch, not an attributed attack. Lightbearer throughout. "
			+ "Synthetic stock: 500 quiver amethyst arrows, 200 splinters, 200 carried dragon arrows, one of each switch item. "
			+ "Account/unlock facts reuse NexOwnedFixtures.account(null); bank knowledge remains unknown. "
			+ "Explicit preferences: upgraded Death Charge assumed; melee/shield locked to Dragon defender 12954. "
			+ "Detected active charges 2 → 1 and energy 1000 → 500 are readings, not inferred casts/procs/specs.");
		OrderedReplay replay = new OrderedReplay();
		replay.at(0, "Initial arena poll (barrier 2, ranged equipment)", observer::tick);
		replay.at(0, "Smoke overhead anchors capture before next GameTick", () -> observer.overhead(boss, "Fill my soul with smoke!"));
		for (int t = 1; t <= 36; t++)
		{
			if (t == 1)
			{
				replay.at(t, "Barrier event value 3; polled value still 2", () -> observer.barrier(3));
				replay.at(t, "Polled barrier catches up before GameTick", () -> barrier = 3);
			}
			if (t == 5) { replay.at(t, "Shadow game message", () -> observer.chat(ChatMessageType.GAMEMESSAGE, "Nex: Darken my shadow!")); }
			if (t == 8) { replay.at(t, "Ranged MP2 window", () -> observer.overhead(boss, "Embrace darkness!")); }
			if (t == 9) { replay.at(t, "20 mine on Nex, 60 other on Umbra; duplicate callbacks", () -> { hit(boss, true, 20); hit(minion, false, 60); }); }
			if (t == 10)
			{
				replay.at(t, "Equip melee with defender and Piety before window callback", () -> worn = NexOwnedFixtures.melee());
				replay.at(t, "Melee MP2 window", () -> observer.overhead(boss, "Fear the shadow!"));
			}
			if (t == 12)
			{
				replay.at(t, "Voidwaker selected special; resource callback", () -> {
					worn = new LinkedHashMap<>(NexOwnedFixtures.melee()); worn.put("weapon", 27690); special = true; observer.sample(-1);
				});
			}
			if (t == 13)
			{
				replay.at(t, "Return to melee, sampled energy/charge decrease", () -> {
					worn = NexOwnedFixtures.melee(); special = false; energy = 500; charge = 1; observer.sample(-1);
				});
			}
			if (t == 15) { replay.at(t, "Blood phase", () -> observer.overhead(boss, "Flood my lungs with blood!")); }
			if (t == 20) { replay.at(t, "Ice phase", () -> observer.overhead(boss, "Infuse me with the power of ice!")); }
			if (t == 21) { replay.at(t, "Two-handed ranged switch with empty offhand", () -> { worn = NexOwnedFixtures.ranged(); observer.sample(-1); }); }
			if (t == 25) { replay.at(t, "Zaros phase", () -> observer.overhead(boss, "NOW, THE POWER OF ZAROS!")); }
			if (t == 30) { replay.at(t, "Nex dying public NPC transformation", () -> { when(boss.getId()).thenReturn(NpcID.NEX_DYING); observer.changed(boss); }); }
			if (t == 31)
			{
				replay.at(t, "Arena-end event 2 while polled barrier remains 3", () -> observer.barrier(2));
				replay.at(t, "Polled barrier catches up before GameTick", () -> barrier = 2);
			}
			replay.at(t, "GameTick", observer::tick);
		}
		replay.run(value -> tick = value, trace::add);
		report.section("Ordered callback script", "```text\n" + String.join("\n", trace) + "\n```");
		report.check("One captured attempt", 1, session.recent().size());
		if (session.recent().isEmpty()) { throw new IllegalStateException("No summary produced by public events"); }
		NexSummary summary = session.recent().get(0);
		report.check("Encounter outcome", Outcome.COMPLETE, summary.outcome);
		report.check("Smoke start tick", 0, summary.startTick); report.check("Death tick", 30, summary.endTick);
		report.check("Arena-end event payload", 31, summary.signals.barrierEndTick);
		report.check("All five phases", Set.of("SMOKE", "SHADOW", "BLOOD", "ICE", "ZAROS"), summary.signals.phases);
		report.near("Duration: (30 - 0) × 0.6", 18, summary.seconds(), 1e-10);
		report.check("Sampled eligible ticks", 30, summary.sampledTicks);
		report.check("Expected eligible ticks", 30, summary.expectedTicks);
		report.check("Local damage, duplicates excluded", 20L, summary.localDamage());
		report.check("Total Nex + named minion damage", 80L, summary.totalDamage());
		report.near("Share: 100 × 20 / (20 + 60)", 25, summary.share() == null ? Double.NaN : summary.share(), 1e-10);
		report.check("Complete statistics sample count", 1, session.stats().n);
		report.check("Excluded attempts", 0, session.stats().failed + session.stats().censored);
		report.check("Captured role switches", true, summary.timeline.stream().map(s -> s.role).collect(java.util.stream.Collectors.toSet()).containsAll(Set.of("ranged", "melee", "spec candidate")));
		report.check("Spec candidate remains evidence, not confirmed attack", true, summary.timeline.stream().anyMatch(s -> s.role.equals("spec candidate") && s.state.slots.get("weapon") == 27690));
		report.check("Melee defender survives ranged empty offhand", true, summary.timeline.stream().anyMatch(s -> s.role.equals("melee") && s.state.slots.get("shield") == 12954)
			&& summary.timeline.stream().anyMatch(s -> s.role.equals("ranged") && s.state.slots.get("shield") == 0));
		report.section("Capture result", summary.outcome + " · " + summary.reason + "\n\n" + summary.timerScope + "\n\n"
			+ NexSummary.DAMAGE_SCOPE + "\n\nGaps: " + new java.util.TreeSet<>(summary.gaps) + ".\n\n"
			+ "Eligibility is synthetic observed-attempt coverage, not coverage of live kills. Damage buckets: "
			+ summary.damage.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue().local + "/" + e.getValue().total).sorted().collect(java.util.stream.Collectors.joining("; ")));
		var account = NexOwnedFixtures.account(null);
		var worker = Executors.newSingleThreadExecutor(r -> new Thread(r, "offline-evaluator"));
		NexOwnedResult result;
		try { result = worker.submit(() -> {
			NexOwnedResult evaluated = new NexOwnedEvaluator(catalog).evaluate(summary, account, prefs);
			checkComparisons(evaluated, report);
			return evaluated;
		}).get(30, TimeUnit.SECONDS); }
		finally { worker.shutdownNow(); }
		report.section("Versions", "Plugin " + System.getProperty("ironpath.version", "test runtime") + "; JDK " + System.getProperty("java.version")
			+ "; RuneLite API JAR " + Path.of(Client.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getFileName()
			+ "; RuneLite client JAR " + Path.of(net.runelite.client.ui.PluginPanel.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getFileName()
			+ "; adapter " + NexCapture.VERSION + "; evaluator " + NexOwnedEvaluator.VERSION + "; knowledge " + catalog.version
			+ "; Gradle " + System.getProperty("ironpath.gradleVersion", "test runtime")
			+ "; catalogue schema 14; preferences schema 3. Model versions are recorded with comparison inputs. Font: DejaVu Sans 12/20 px, RuneLiteLAF, headless Java2D.");
		SwingUtilities.invokeAndWait(() -> {
			try { previews(output, result, report); } catch (Exception ex) { throw new RuntimeException(ex); }
		});
	}

	private void checkComparisons(NexOwnedResult result, ReviewReport report)
	{
		var ranged = result.choices.stream().filter(c -> c.actual.tick == 8 && c.actual.role.equals("ranged")).findFirst().orElseThrow();
		report.check("Ranged MP2 ordinary comparison supported", true, ranged.supported());
		if (ranged.supported())
		{
			var metrics = ranged.comparison.before;
			// Independent hand case retained in NexOwnedEvaluatorTest, not computed by the model under review.
			report.check("Ranged attack bonus: 200 + 10 quiver", 210, metrics.attackBonus);
			report.near("Ranged strength: 35 + 55 arrows + 1 quiver", 91, metrics.damagePercent, 0);
			report.check("Tbow max hit, independent integer hand case", 72, metrics.maxHit);
			report.check("Tbow attack roll, independent hand case", 53303L, metrics.attackRoll);
			report.check("Nex defence roll: 269 × 254", 68326L, metrics.defenceRoll);
			double chance = 53303.0 / (2 * 68327);
			report.near("Hit chance: 53303 / (2 × 68327)", chance, metrics.hitChance, 1e-12);
			report.near("DPS: (36 + 1/73) × hit chance / 3", (36 + 1.0 / 73) * chance / 3, metrics.dps, 1e-12);
			report.check("Rapid attack delay", 5, metrics.attackTicks);
			report.check("Selected quiver projectile", 21326, ranged.comparison.beforeSetup.loadedAmmo);
			report.check("Physical ammo slot remains blessing", 20235, ranged.comparison.beforeSetup.slots.get("ammo"));
			report.check("Carried owned proposal can improve ordinary offence", true, ranged.comparison.dpsGain() > 0);
			Map<String, Integer> expectedSlots = new TreeMap<>(NexOwnedFixtures.ranged()); expectedSlots.put("ammo", 11212);
			report.check("Whole proposed package changes only the carried arrow", expectedSlots, ranged.proposed.slots);
			// Visible strength 112 with Rigour gives 145; base max 35 -> 36, Nex Tbow factor 206%.
			report.check("Dragon-arrow max hit: floor(36 × 206 / 100)", 74, ranged.comparison.after.maxHit);
			report.check("Arrow change leaves attack roll unchanged", 53303L, ranged.comparison.after.attackRoll);
			report.near("Owned DPS: (37 + 1/75) × hit chance / 3", (37 + 1.0 / 75) * chance / 3, ranged.comparison.after.dps, 1e-12);
			report.near("Whole-package delta: hand-computed expected damage difference / 3",
				((37 + 1.0 / 75) - (36 + 1.0 / 73)) * chance / 3, ranged.comparison.dpsGain(), 1e-12);
		}
		var firstMelee = result.choices.stream().filter(c -> c.actual.role.equals("melee") && c.supported()).findFirst().orElseThrow();
		var identity = new NexOwnedEvaluator(catalog).compare(firstMelee.actual,
			new NexOwnedEvaluator(catalog).setup(firstMelee.actual, firstMelee.actual.state.slots), NexOwnedFixtures.account(null), prefs);
		report.near("Identical complete melee package gives zero", 0, identity.dpsGain(), 1e-12);
		report.check("Supported blood-fury alternative retains negative offence", true, firstMelee.alternatives.stream()
			.anyMatch(p -> p.afterSetup.slots.get("neck") == 24780 && p.dpsGain() < 0));
		report.check("Spec candidate has scoped gap", true, result.choices.stream().anyMatch(c -> c.actual.role.equals("spec candidate") && !c.supported()));
		report.check("Zaros has scoped gap", true, result.choices.stream().anyMatch(c -> c.actual.phase.equals("ZAROS") && !c.supported()));
		report.check("No bank scenarios fabricated from unknown bank", false, result.choices.stream().anyMatch(c -> c.bankPreparation));
		StringBuilder rows = new StringBuilder("| Tick/role/phase/window | Result |\n|---|---|\n");
		for (var choice : result.choices)
		{
			rows.append("| ").append(choice.actual.tick).append(" / ").append(choice.actual.role).append(" / ").append(choice.actual.phase)
				.append(" / ").append(choice.actual.window).append(" | ").append(choice.headline()).append(" |\n");
			if (!choice.supported()) { continue; }
			var pair = choice.comparison;
			report.check("Tick " + choice.actual.tick + " does not estimate team time", false, pair.available(ComparisonResult.Metric.EXPECTED_KILL_TIME));
			if (choice.actual.tick != 8 && choice.actual.tick != 10) { continue; }
			report.section("Comparison inputs/results · tick " + choice.actual.tick,
				"Actual slots: `" + new TreeMap<>(choice.actual.state.slots) + "`\n\nVisible levels: `" + new TreeMap<>(choice.actual.state.visibleLevels)
				+ "`; prayers: " + choice.actual.state.prayers + "; style: " + choice.actual.state.style + "; target: " + choice.actual.target
				+ ".\n\nCarried quantities: `" + new TreeMap<>(choice.actual.state.inventory) + "`.\n\nProposed slots: `" + new TreeMap<>(choice.proposed.slots)
				+ "`\n\n" + pair.summary() + "\n\nModel " + pair.modelVersion + "; knowledge " + pair.knowledgeVersion
				+ ".\n\nAssumptions/sources: " + String.join("; ", pair.assumptions) + "\n\nCoverage: " + choice.coverage + ".");
		}
		report.section("Comparison overview", rows + "\nGrouped gaps: " + result.gaps + ".\n\n"
			+ "Whole-package ordinary comparisons are conditional, independent alternatives; counts are not fight coverage. "
			+ "No spec damage, attack attribution, survival, personal contribution or team-time output is introduced.");
	}

	private void previews(Path output, NexOwnedResult result, ReviewReport report) throws Exception
	{
		for (int font : List.of(12, 20))
		{
			PanelPreview.fonts(font);
			NexPanel panel = new NexPanel(null); panel.setFont(new Font("DejaVu Sans", Font.PLAIN, font));
			NexPanel.Actions actions = new NexPanel.Actions() {
				public boolean current() { return true; } public boolean canEdit() { return false; }
				public void method(NexMethod method) { throw new UnsupportedOperationException("Read-only preview"); }
				public void confirm(String key, Boolean value) { throw new UnsupportedOperationException("Read-only preview"); }
				public void reset() { throw new UnsupportedOperationException("Read-only preview"); }
				public void enable(boolean value) { throw new UnsupportedOperationException("Read-only preview"); }
			};
			panel.render("Synthetic offline scenario", NexOwnedFixtures.account(null), prefs, catalog, session.stats(), true, actions, "Synthetic; no preference IO", result);
			report.check("Main view " + font + "px shows measured kill", true, PanelPreview.text(panel).contains("Last complete kill · 18.0 s"));
			capture(panel, output, "main", font, report);
			PanelPreview.click(panel, "Owned setup comparisons");
			PanelPreview.combo(panel, "nex-owned-role-filter").setSelectedItem("ranged");
			report.check("Comparison view " + font + "px preserves projectile", true, PanelPreview.text(panel).contains("Selected quiver projectile: Amethyst arrow"));
			capture(panel, output, "comparison", font, report);
			PanelPreview.click(panel, "Full setups and calculation basis"); capture(panel, output, "basis", font, report);
			PanelPreview.combo(panel, "nex-owned-support-filter").setSelectedItem("Unavailable comparisons");
			PanelPreview.combo(panel, "nex-owned-role-filter").setSelectedItem("spec candidate");
			report.check("Gap view " + font + "px preserves unsupported spec", true, PanelPreview.text(panel).contains("ordinary attacks cannot establish spec value"));
			capture(panel, output, "gap", font, report);
			PanelPreview.click(panel, "Special-attack resources"); PanelPreview.click(panel, "Resource readings and rules");
			capture(panel, output, "resources", font, report);
			PanelPreview.click(panel, "Back to encounter"); PanelPreview.click(panel, "Kill and calculation details");
			capture(panel, output, "kill-details", font, report);
		}
	}
	private static void capture(NexPanel panel, Path output, String state, int font, ReviewReport report) throws Exception
	{ PanelPreview.capture(panel, output.resolve(state + "-" + font + ".png"), 225, report); }
}
