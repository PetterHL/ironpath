package com.ironpath;

import com.ironpath.advisor.PoweredMagic;
import com.ironpath.encounter.CombatState;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarPlayerID;

/** Reads immutable combat evidence independently of any boss adapter or proposed loadout. */
final class CombatStateReader
{
	private final Client client;

	CombatStateReader(Client client) { this.client = client; }

	CombatState read()
	{
		if (!client.isClientThread()) { throw new IllegalStateException("Client thread required"); }
		Map<String, Integer> slots = new HashMap<>();
		var worn = AccountSnapshotReader.slots(client.getItemContainer(InventoryID.WORN), Instant.now());
		for (String slot : PoweredMagic.SLOTS)
		{
			slots.put(slot, worn.known() ? worn.value().getOrDefault(slot, -1) : -1);
		}
		Map<String, Integer> real = new HashMap<>(), visible = new HashMap<>();
		for (Skill skill : List.of(Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.RANGED,
			Skill.MAGIC, Skill.PRAYER, Skill.HITPOINTS))
		{
			int level = client.getRealSkillLevel(skill), current = client.getBoostedSkillLevel(skill);
			if (level > 0)
			{
				real.put(skill.name(), level);
				visible.put(skill.name(), current);
			}
		}
		Set<String> prayers = new HashSet<>();
		for (Prayer prayer : Prayer.values())
		{
			if (client.isPrayerActive(prayer)) { prayers.add(prayer.name()); }
		}
		return new CombatState(slots, real, visible, prayers, client.getVarpValue(VarPlayerID.COM_MODE),
			client.getVarpValue(VarPlayerID.SA_ENERGY), client.getVarpValue(VarPlayerID.SA_ATTACK) == 1);
	}
}
