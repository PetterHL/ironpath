package com.ironpath.advisor;

import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.preferences.Preferences;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static com.ironpath.advisor.P4aFixtures.*;

/** Explicit original synthetic evidence, never a player questionnaire or an observed account. */
public final class R15Fixtures
{
	public static Preferences absent(KnowledgeCatalog catalog)
	{
		Preferences p=Preferences.EMPTY;
		for (String key : catalog.factChoices().keySet()) { if (key.startsWith("owns-")) { p=p.confirm(key,false,TIME); } }
		return p;
	}
	public static AccountSnapshot developed(boolean shadow)
	{
		Set<Integer> items=new HashSet<>(Set.of(12899,4708,4712,4714,21795,6585,7462,6731,25867,23971,23975,23979,4151));
		if (shadow) { items.add(27275); }
		AccountSnapshot original=account(items,99,99);
		Map<String,Integer> levels=new HashMap<>(original.levels.value()); levels.replaceAll((key,value)->99); levels.put("FLETCHING",99);
		Map<String,Observation<Boolean>> facts=new HashMap<>(original.facts);
		for (String key : Set.of("cox-preparation","toa-preparation","monkey-madness-ii","monkey-form","beneath-cursed-sands","smoke-task","smoke-protection","zaryte-context","desert-treasure-ii","peer-ring-icons")) { facts.put(key,fact(true)); }
		return new AccountSnapshot(fact(levels),original.inventory,original.equipment,original.bank,fact(false),fact(false),facts,
			Observation.unknown(),Observation.unknown(),Observation.unknown(),fact(Map.of(6573,1L,2357,1L,11065,1L,565,500L,566,20L,564,1L,1592,1L)));
	}
	public static AccountSnapshot items(AccountSnapshot a, int... more)
	{
		Set<Integer> owned=new HashSet<>(a.bank.value()); for (int id : more) { owned.add(id); }
		return new AccountSnapshot(a.levels,a.inventory,a.equipment,fact(owned),a.rigour,a.augury,a.facts,
			a.equipmentSlots,a.inventoryQuantities,a.equipmentQuantities,a.bankQuantities);
	}
	public static AccountSnapshot withFact(AccountSnapshot a, String key, Observation<Boolean> value)
	{
		Map<String,Observation<Boolean>> facts=new HashMap<>(a.facts); facts.put(key,value);
		return new AccountSnapshot(a.levels,a.inventory,a.equipment,a.bank,a.rigour,a.augury,facts,
			a.equipmentSlots,a.inventoryQuantities,a.equipmentQuantities,a.bankQuantities);
	}
	public static AccountSnapshot withQuantity(AccountSnapshot a, int item, long count)
	{
		Map<Integer,Long> counts=new HashMap<>(a.bankQuantities.value()); counts.put(item,count);
		return new AccountSnapshot(a.levels,a.inventory,a.equipment,a.bank,a.rigour,a.augury,a.facts,
			a.equipmentSlots,a.inventoryQuantities,a.equipmentQuantities,fact(counts));
	}
}
