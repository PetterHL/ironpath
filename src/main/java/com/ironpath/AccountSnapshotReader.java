package com.ironpath;

import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;

final class AccountSnapshotReader
{
	private final Client client;

	@Inject
	AccountSnapshotReader(Client client) { this.client = client; }

	AccountSnapshot read(Observation<Set<Integer>> bank)
	{
		if (!client.isClientThread()) { throw new IllegalStateException("Client thread required"); }
		Instant now = Instant.now();
		Map<String, Integer> levels = new HashMap<>();
		for (Skill skill : Skill.values())
		{
			int level = client.getRealSkillLevel(skill);
			if (level > 0) { levels.put(skill.name(), level); }
		}
		return new AccountSnapshot(Observation.observed(levels, now, "real skill levels"),
			container(client.getItemContainer(InventoryID.INV), now, "inventory"),
			container(client.getItemContainer(InventoryID.WORN), now, "equipment"), bank,
			unlock(VarbitID.PRAYER_RIGOUR_UNLOCKED, now, "Rigour unlock varbit"),
			unlock(VarbitID.PRAYER_AUGURY_UNLOCKED, now, "Augury unlock varbit"));
	}

	private Observation<Boolean> unlock(int id, Instant time, String source)
	{
		int value = client.getVarbitValue(id);
		return value == 0 || value == 1 ? Observation.observed(value == 1, time, source) : Observation.unknown();
	}

	static Observation<Set<Integer>> container(ItemContainer container, Instant time, String source)
	{
		if (container == null) { return Observation.unknown(); }
		Set<Integer> ids = new HashSet<>();
		for (Item item : container.getItems())
		{
			if (item.getId() > 0 && item.getQuantity() > 0) { ids.add(item.getId()); }
		}
		return Observation.observed(Set.copyOf(ids), time, source);
	}
}
