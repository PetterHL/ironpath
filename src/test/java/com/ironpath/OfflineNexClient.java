package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.advisor.NexOwnedFixtures;
import java.util.*;
import net.runelite.api.*;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.gameval.VarPlayerID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Shared synthetic public-API boundary for adapter and full-plugin replay. No private account state. */
class OfflineNexClient
{
	int tick, barrier = 2, energy = 1000, charge = 2, cooldown, ammoId = 21326, ammoQuantity = 500, splinters = 200, quiverCharged = 1;
	boolean bossPresent = true, inventoryReadable = true;
	boolean special;
	Map<String, Integer> worn = NexOwnedFixtures.ranged();
	final Map<Integer, Long> carried = new TreeMap<>();
	final Client client = mock(Client.class);
	final Player local = mock(Player.class);
	final WorldView world = mock(WorldView.class);
	NPC boss = mock(NPC.class);
	final NPC minion = mock(NPC.class);
	final KnowledgeCatalog catalog;

	OfflineNexClient() throws Exception
	{
		catalog = KnowledgeCatalog.load(new Gson());
		Thread clientThread = Thread.currentThread();
		when(client.isClientThread()).thenAnswer(i -> Thread.currentThread() == clientThread);
		when(client.getTickCount()).thenAnswer(i -> tick);
		when(client.getVarbitValue(anyInt())).thenAnswer(i -> {
			int id = i.getArgument(0);
			if (id == VarbitID.NEX_BARRIER) { return barrier; }
			if (id == VarbitID.ARCEUUS_DEATH_CHARGE_ACTIVE) { return charge; }
			if (id == VarbitID.ARCEUUS_DEATH_CHARGE_COOLDOWN) { return cooldown; }
			if (id == VarbitID.WORN_ITEMS_DIZANAS_QUIVER_CHARGED) { return quiverCharged; }
			if (id == VarbitID.CHARGES_DIZANAS_QUIVER_QUANTITY) { return splinters; }
			throw new IllegalArgumentException("Unspecified synthetic varbit " + id);
		});
		when(client.getVarpValue(anyInt())).thenAnswer(i -> {
			int id = i.getArgument(0);
			if (id == VarPlayerID.COM_MODE) { return 1; }
			if (id == VarPlayerID.SA_ENERGY) { return energy; }
			if (id == VarPlayerID.SA_ATTACK) { return special ? 1 : 0; }
			if (id == VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO) { return ammoId; }
			if (id == VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO_AMOUNT) { return ammoQuantity; }
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
		when(local.getInteracting()).thenAnswer(i -> boss);
		when(local.isDead()).thenReturn(false);
		when(boss.getWorldView()).thenReturn(world); when(boss.getIndex()).thenReturn(1);
		when(boss.getId()).thenReturn(NpcID.NEX);
		when(minion.getWorldView()).thenReturn(world); when(minion.getIndex()).thenReturn(2);
		when(minion.getId()).thenReturn(NpcID.NEX_SHADOWMAGE);
		IndexedObjectSet<NPC> npcs = mock(IndexedObjectSet.class);
		when(world.npcs()).thenAnswer(i -> npcs); when(npcs.stream()).thenAnswer(i -> bossPresent ? List.of(boss, minion).stream() : java.util.stream.Stream.empty());
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
		when(client.getItemContainer(InventoryID.INV)).thenAnswer(i -> inventoryReadable ? inventory : null);
		when(equipment.getItems()).thenAnswer(i -> java.util.Arrays.stream(EquipmentInventorySlot.values())
			.map(slot -> equipment(slot.getSlotIdx())).filter(java.util.Objects::nonNull).toArray(Item[]::new));
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
	}

	Item equipment(int index)
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
	Item[] inventory()
	{
		return carried.entrySet().stream().filter(e -> !worn.containsValue(e.getKey()))
			.map(e -> new Item(e.getKey(), Math.toIntExact(e.getValue()))).toArray(Item[]::new);
	}
}
