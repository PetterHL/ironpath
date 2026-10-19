package com.ironpath;

import com.ironpath.encounter.CombatState;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.EnumID;
import net.runelite.api.EnumComposition;
import com.ironpath.nex.NexSpellResources;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Public-state sampling regressions; no live encounter behavior is implied. */
public class CombatStateReaderTest
{
	private Client client;
	private CombatStateReader reader;

	@Before public void setup()
	{
		client = mock(Client.class);
		when(client.isClientThread()).thenReturn(true);
		reader = new CombatStateReader(client);
	}

	@Test public void refusesSamplingOutsideClientThread()
	{
		when(client.isClientThread()).thenReturn(false);
		assertThrows(IllegalStateException.class, () -> reader.read());
		verify(client).isClientThread();
		verifyNoMoreInteractions(client);
	}

	@Test public void unreadEquipmentAndLevelsStayDistinctFromObservedEmptySlots()
	{
		CombatState unread = reader.read();
		assertFalse(unread.slots.isEmpty());
		assertTrue(unread.slots.values().stream().allMatch(id -> id == -1));
		assertTrue(unread.realLevels.isEmpty());
		assertTrue(unread.visibleLevels.isEmpty());

		when(client.getItemContainer(InventoryID.WORN)).thenReturn(mock(ItemContainer.class));
		CombatState empty = reader.read();
		assertEquals(unread.slots.keySet(), empty.slots.keySet());
		assertTrue(empty.slots.values().stream().allMatch(id -> id == 0));
		assertTrue(unread.slots.values().stream().allMatch(id -> id == -1));
	}

	@Test public void switchesPreserveEarlierEquipmentLevelsPrayersAndRawSpecState()
	{
		ItemContainer worn = mock(ItemContainer.class);
		when(client.getItemContainer(InventoryID.WORN)).thenReturn(worn);
		int weaponSlot = EquipmentInventorySlot.WEAPON.getSlotIdx();
		when(worn.getItem(weaponSlot)).thenReturn(new Item(9185, 1));
		when(client.getRealSkillLevel(Skill.RANGED)).thenReturn(90);
		when(client.getBoostedSkillLevel(Skill.RANGED)).thenReturn(73);
		when(client.isPrayerActive(Prayer.EAGLE_EYE)).thenReturn(true);
		when(client.getVarpValue(VarPlayerID.COM_MODE)).thenReturn(1);
		when(client.getVarpValue(VarPlayerID.SA_ENERGY)).thenReturn(750);
		when(client.getVarpValue(VarPlayerID.SA_ATTACK)).thenReturn(1);
		var definition=mock(net.runelite.api.ItemComposition.class);
		when(definition.getName()).thenReturn("Rune crossbow"); when(client.getItemDefinition(9185)).thenReturn(definition);
		CombatState before = reader.read();

		when(worn.getItem(weaponSlot)).thenReturn(new Item(20997, 1));
		when(client.getBoostedSkillLevel(Skill.RANGED)).thenReturn(103);
		when(client.isPrayerActive(Prayer.EAGLE_EYE)).thenReturn(false);
		when(client.getVarpValue(VarPlayerID.COM_MODE)).thenReturn(0);
		when(client.getVarpValue(VarPlayerID.SA_ENERGY)).thenReturn(250);
		when(client.getVarpValue(VarPlayerID.SA_ATTACK)).thenReturn(0);
		CombatState after = reader.read();

		assertEquals(Integer.valueOf(9185), before.slots.get("weapon"));
		assertEquals("Rune crossbow",before.itemNames.get(9185)); assertFalse(after.itemNames.containsKey(9185));
		assertEquals(Integer.valueOf(20997), after.slots.get("weapon"));
		assertEquals(Integer.valueOf(90), before.realLevels.get("RANGED"));
		assertEquals(before.realLevels, after.realLevels);
		assertEquals(Integer.valueOf(73), before.visibleLevels.get("RANGED"));
		assertEquals(Integer.valueOf(103), after.visibleLevels.get("RANGED"));
		assertTrue(before.prayers.contains("EAGLE_EYE"));
		assertTrue(after.prayers.isEmpty());
		assertEquals(Integer.valueOf(1), before.style);
		assertEquals(Integer.valueOf(0), after.style);
		assertEquals(Integer.valueOf(750), before.specialEnergy);
		assertEquals(Integer.valueOf(250), after.specialEnergy);
		assertTrue(before.specialSelected);
		assertFalse(after.specialSelected);
		assertThrows(UnsupportedOperationException.class, () -> before.slots.put("weapon", 0));
		assertThrows(UnsupportedOperationException.class, () -> before.prayers.clear());
	}
	@Test public void runePouchReadingsRequireACarriedPouchAndFreezeSeparateFromLooseRunes()
	{
		ItemContainer inventory=mock(ItemContainer.class); when(client.getItemContainer(InventoryID.INV)).thenReturn(inventory);
		when(inventory.getItems()).thenReturn(new Item[]{new Item(12791,1),new Item(560,2)});
		EnumComposition mapping=mock(EnumComposition.class); when(client.getEnum(EnumID.RUNEPOUCH_RUNE)).thenReturn(mapping);
		when(mapping.getIntValue(7)).thenReturn(560); when(mapping.getIntValue(8)).thenReturn(565); when(mapping.getIntValue(13)).thenReturn(566);
		when(client.getVarbitValue(VarbitID.RUNE_POUCH_TYPE_1)).thenReturn(7); when(client.getVarbitValue(VarbitID.RUNE_POUCH_QUANTITY_1)).thenReturn(3);
		when(client.getVarbitValue(VarbitID.RUNE_POUCH_TYPE_2)).thenReturn(8); when(client.getVarbitValue(VarbitID.RUNE_POUCH_QUANTITY_2)).thenReturn(4);
		when(client.getVarbitValue(VarbitID.RUNE_POUCH_TYPE_3)).thenReturn(13); when(client.getVarbitValue(VarbitID.RUNE_POUCH_QUANTITY_3)).thenReturn(5);
		var before=reader.read(); assertEquals(java.util.Map.of(560,5,565,4,566,5),NexSpellResources.stock(before));
		assertEquals(Long.valueOf(2),before.inventory.get(560));
		when(client.getVarbitValue(VarbitID.RUNE_POUCH_QUANTITY_1)).thenReturn(1);
		assertEquals(Integer.valueOf(3),NexSpellResources.stock(reader.read()).get(560)); assertEquals(Integer.valueOf(5),NexSpellResources.stock(before).get(560));
		when(client.getEnum(EnumID.RUNEPOUCH_RUNE)).thenReturn(null);
		assertThrows(IllegalArgumentException.class,()->NexSpellResources.stock(reader.read()));
		when(inventory.getItems()).thenReturn(new Item[]{new Item(560,2)});
		assertEquals(java.util.Map.of(560,2,565,0,566,0),NexSpellResources.stock(reader.read()));
		assertEquals(Integer.valueOf(5),NexSpellResources.stock(before).get(560));
	}
}
