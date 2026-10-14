package com.ironpath.advisor;

import com.ironpath.model.*;
import java.util.*;
import static com.ironpath.advisor.P4aFixtures.*;
import static com.ironpath.advisor.R15Fixtures.*;

/** Original synthetic accounts, including deliberate false/unknown facts; no personal performance. */
public final class EP1Fixtures
{
	public static AccountSnapshot ready()
	{
		AccountSnapshot a=items(developed(false),11840,1755,11212,6731,6573,2357,11065,1592);
		for(String key:List.of("regicide","zulrah-dialogue","final-dawn","doom-preparation","doom-delve-four","song-of-elves",
			"while-guthix-sleeps","duradel-notes","muspah-preparation","secrets-of-north","frozen-door","nex-entry","nex-team",
			"dragon-slayer-ii","blood-moon-rises","colosseum-preparation","leviathan-defeated","duke-defeated","kraken-task"))
		{ a=withFact(a,key,fact(true)); }
		return a;
	}
	public static AccountSnapshot level(AccountSnapshot a,String skill,int value)
	{
		Map<String,Integer> levels=new HashMap<>(a.levels.value());levels.put(skill,value);
		return new AccountSnapshot(fact(levels),a.inventory,a.equipment,a.bank,a.rigour,a.augury,a.facts,
			a.equipmentSlots,a.inventoryQuantities,a.equipmentQuantities,a.bankQuantities);
	}
	public static AccountSnapshot unread(AccountSnapshot a)
	{ return new AccountSnapshot(a.levels,Observation.unknown(),Observation.unknown(),Observation.unknown(),a.rigour,a.augury,a.facts); }
	public static Map<String,AccountSnapshot> profiles(KnowledgeCatalog catalog)
	{
		Map<String,AccountSnapshot> result=new LinkedHashMap<>();AccountSnapshot ready=ready();
		result.put("post-bowfa",ready);
		result.put("doom-workable",items(level(ready,"RANGED",80),29591,6746));
		result.put("doom-ready",items(ready,29591,6746));
		result.put("owned-components",withQuantity(items(ready,29580,70,19535,19529,12922,26372,11785),26231,250));
		result.put("early",level(level(level(ready,"RANGED",60),"MAGIC",60),"DEFENCE",50));
		result.put("single-megarare",items(ready,20997,6746));
		result.put("nearly-complete",items(items(ready,29591),catalog.upgrades.stream().filter(u->!u.id.equals("tumekens-shadow")).mapToInt(u->u.itemId).toArray()));
		result.put("unread-bank",unread(ready));return result;
	}
}
