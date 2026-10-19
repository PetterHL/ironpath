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
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.EnumID;
import com.ironpath.nex.NexSpellResources;

/** Reads immutable combat evidence independently of any boss adapter or proposed loadout. */
final class CombatStateReader
{
	private final Client client;

	CombatStateReader(Client client) { this.client = client; }

	CombatState read()
	{
		if (!client.isClientThread()) { throw new IllegalStateException("Client thread required"); }
		Map<String, Integer> slots = new HashMap<>();
		Map<Integer,String> names = new HashMap<>();
		var worn = AccountSnapshotReader.slots(client.getItemContainer(InventoryID.WORN), Instant.now());
		for (String slot : PoweredMagic.SLOTS)
		{
			slots.put(slot, worn.known() ? worn.value().getOrDefault(slot, -1) : -1);
		}
		for (int id : slots.values())
		{
			if(id<=0) { continue; }
			var definition=client.getItemDefinition(id);
			if(definition!=null && definition.getName()!=null) { names.put(id,definition.getName()); }
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
		var inventory = AccountSnapshotReader.quantities(client.getItemContainer(InventoryID.INV), Instant.now(), "inventory");
		Map<String,Integer> resources = new HashMap<>();
		var carried=client.getItemContainer(InventoryID.INV);
		if(carried!=null)
		{
			int occupied=0;
			for(var item:carried.getItems()) { if(item!=null && item.getId()>0 && item.getQuantity()>0) { occupied++; } }
			resources.put("inventory-slots-used",occupied);
		}
		resources.put("death-charge-active", client.getVarbitValue(VarbitID.ARCEUUS_DEATH_CHARGE_ACTIVE));
		resources.put("death-charge-cooldown", client.getVarbitValue(VarbitID.ARCEUUS_DEATH_CHARGE_COOLDOWN));
		var equipment = client.getItemContainer(InventoryID.WORN);
		if (equipment != null)
		{
			var ammo = equipment.getItem(EquipmentInventorySlot.AMMO.getSlotIdx());
			resources.put("worn-ammo-quantity", ammo == null ? 0 : ammo.getQuantity());
		}
		// These are public raw counters, not a general charged-item API or proof of the selected projectile.
		resources.put("quiver-ammo-id-raw", client.getVarpValue(VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO));
		resources.put("quiver-ammo-quantity-raw", client.getVarpValue(VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO_AMOUNT));
		resources.put("quiver-charged-raw", client.getVarbitValue(VarbitID.WORN_ITEMS_DIZANAS_QUIVER_CHARGED));
		resources.put("quiver-splinters-raw", client.getVarbitValue(VarbitID.CHARGES_DIZANAS_QUIVER_QUANTITY));
		if(inventory.known() && NexSpellResources.POUCHES.stream().anyMatch(id->inventory.value().getOrDefault(id,0L)>0))
		{
			int[] types={VarbitID.RUNE_POUCH_TYPE_1,VarbitID.RUNE_POUCH_TYPE_2,VarbitID.RUNE_POUCH_TYPE_3,VarbitID.RUNE_POUCH_TYPE_4,VarbitID.RUNE_POUCH_TYPE_5,VarbitID.RUNE_POUCH_TYPE_6};
			int[] amounts={VarbitID.RUNE_POUCH_QUANTITY_1,VarbitID.RUNE_POUCH_QUANTITY_2,VarbitID.RUNE_POUCH_QUANTITY_3,VarbitID.RUNE_POUCH_QUANTITY_4,VarbitID.RUNE_POUCH_QUANTITY_5,VarbitID.RUNE_POUCH_QUANTITY_6};
			var mapping=client.getEnum(EnumID.RUNEPOUCH_RUNE);
			for(int i=0;i<types.length;i++)
			{
				int type=client.getVarbitValue(types[i]),amount=client.getVarbitValue(amounts[i]);
				int id=type==0?0:type<0 || mapping==null?-1:mapping.getIntValue(type);
				if(type>0 && id<=0) { id=-1; }
				resources.put("rune-pouch-item-"+(i+1),id); resources.put("rune-pouch-quantity-"+(i+1),amount);
			}
		}
		return new CombatState(slots, real, visible, prayers, client.getVarpValue(VarPlayerID.COM_MODE),
			client.getVarpValue(VarPlayerID.SA_ENERGY), client.getVarpValue(VarPlayerID.SA_ATTACK) == 1,
			inventory.known() ? inventory.value() : null, resources, names);
	}
}
