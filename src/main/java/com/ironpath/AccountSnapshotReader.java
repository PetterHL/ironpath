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
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
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
		Map<String, Observation<Boolean>> facts = new HashMap<>();
		facts.put("song-of-elves", quest(Quest.SONG_OF_THE_ELVES, now));
		facts.put("regicide", quest(Quest.REGICIDE, now));
		facts.put("beneath-cursed-sands", quest(Quest.BENEATH_CURSED_SANDS, now));
		facts.put("perilous-moons", quest(Quest.PERILOUS_MOONS, now));
		facts.put("final-dawn", quest(Quest.THE_FINAL_DAWN, now));
		facts.put("frozen-door", quest(Quest.THE_FROZEN_DOOR, now));
		facts.put("deadeye", unlock(VarbitID.PRAYER_DEADEYE_UNLOCKED, now, "Deadeye unlock varbit"));
		facts.put("mystic-vigour", unlock(VarbitID.PRAYER_MYSTIC_VIGOUR_UNLOCKED, now, "Mystic Vigour unlock varbit"));
		return new AccountSnapshot(Observation.observed(levels, now, "real skill levels"),
			container(client.getItemContainer(InventoryID.INV), now, "inventory"),
			container(client.getItemContainer(InventoryID.WORN), now, "equipment"), bank,
			unlock(VarbitID.PRAYER_RIGOUR_UNLOCKED, now, "Rigour unlock varbit"),
			unlock(VarbitID.PRAYER_AUGURY_UNLOCKED, now, "Augury unlock varbit"), facts);
	}

	private Observation<Boolean> quest(Quest quest, Instant time)
	{
		try
		{
			QuestState state = quest.getState(client);
			return state == null ? Observation.unknown() : Observation.observed(state == QuestState.FINISHED, time,
				quest.getName() + " completion via Quest.getState");
		}
		catch (RuntimeException unavailable) { return Observation.unknown(); } // Script/state not ready: never false.
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
