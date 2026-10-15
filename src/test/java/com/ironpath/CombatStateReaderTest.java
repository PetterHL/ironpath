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
}
