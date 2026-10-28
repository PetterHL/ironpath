package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.model.*;
import com.ironpath.preferences.Preferences;
import java.util.*;
import org.junit.Test;
import static com.ironpath.advisor.P4aFixtures.fact;
import static org.junit.Assert.*;

public class IronmanOrderTest
{
	private final KnowledgeCatalog catalog;
	public IronmanOrderTest() throws Exception { catalog=KnowledgeCatalog.load(new Gson()); }

	@Test public void nexMethodUsesObservedUsableCrossbow() throws Exception
	{
		assertEquals(5,work(26382,"head",account(Set.of(11785),Map.of(),true)).tier);
		assertEquals(7,work(26382,"head",account(Set.of(11785),Map.of("RANGED",1),true)).tier);
		assertEquals(6,work(26382,"head",account(null,Map.of(),true)).tier);
		assertEquals(7,work(26382,"head",account(Set.of(),Map.of(),true)).tier);
		assertEquals(6,work(26382,"head",account(Set.of(11785),Map.of(),null)).tier);
	}

	@Test public void usableChecksAdditionalAndRecursiveGearConditions()
	{
		var usable=new KnowledgeCatalog.Condition(); usable.test=KnowledgeCatalog.Test.USABLE; usable.key="maul";
		assertEquals(Truth.TRUE,IronmanOrder.condition(usable,catalog,account(Set.of(21003),Map.of(),true),Preferences.EMPTY));
		assertEquals(Truth.FALSE,IronmanOrder.condition(usable,catalog,account(Set.of(21003),Map.of("STRENGTH",1),true),Preferences.EMPTY));
		assertEquals(Truth.UNKNOWN,IronmanOrder.condition(usable,catalog,account(Set.of(21003),Map.of("STRENGTH",-1),true),Preferences.EMPTY));
		usable.key="ranged";
		assertEquals(Truth.FALSE,IronmanOrder.condition(usable,catalog,account(Set.of(25865),Map.of(),true),Preferences.EMPTY));
		Set<Integer> crystal=Set.of(25865,23975,23979,23971);
		assertEquals(Truth.TRUE,IronmanOrder.condition(usable,catalog,account(crystal,Map.of(),true),Preferences.EMPTY));
		assertEquals(Truth.FALSE,IronmanOrder.condition(usable,catalog,account(crystal,Map.of("AGILITY",1),true),Preferences.EMPTY));
		usable.value=false;
		assertEquals(Truth.FALSE,IronmanOrder.condition(usable,catalog,account(crystal,Map.of(),true),Preferences.EMPTY));
		assertEquals(Truth.UNKNOWN,IronmanOrder.condition(usable,catalog,account(null,Map.of(),true),Preferences.EMPTY));
	}

	@Test public void torvaAssemblyRequiresKnownSmithing() throws Exception
	{
		Set<Integer> components=Set.of(26376,26394);
		assertEquals(1,work(26382,"head",account(components,Map.of(),null)).tier);
		assertEquals(7,work(26382,"head",account(components,Map.of("SMITHING",1),null)).tier);
		assertEquals(6,work(26382,"head",account(components,Map.of("SMITHING",-1),null)).tier);
	}

	@Test public void downstreamZenyteStagesBypassEarlierAcquisitionAndSkills() throws Exception
	{
		assertEquals(1,work(19553,"neck",account(Set.of(19529),Map.of(),true)).tier);
		for(int id:List.of(19501,19541)) // Unstrung alternative or completed strung amulet.
		{
			assertEquals(1,work(19553,"neck",account(Set.of(id),Map.of("CRAFTING",1),null)).tier);
			assertEquals(7,work(19553,"neck",account(Set.of(id),Map.of("CRAFTING",1,"MAGIC",1),null)).tier);
			assertEquals(6,work(19553,"neck",account(Set.of(id),Map.of("CRAFTING",1,"MAGIC",-1),null)).tier);
		}
		assertEquals(7,work(19553,"neck",account(Set.of(19529),Map.of("CRAFTING",1),null)).tier);
		assertTrue(work(19553,"neck",account(Set.of(),Map.of(),null)).tier>1);
	}

	@Test public void npcAssemblyAlternativeBypassesSelfSingingSkills() throws Exception
	{
		assertEquals(1,work(25865,"weapon",account(Set.of(25859),Map.of("CRAFTING",1,"SMITHING",1),null)).tier);
	}

	@Test public void acquisitionQuantitiesMustBeConfirmed() throws Exception
	{
		var base=account(Set.of(26378,26394),Map.of(),null);
		assertTrue(work(26384,"body",base).tier>1); // Presence does not confirm two components.
		var counted=new AccountSnapshot(base.levels,base.inventory,base.equipment,base.bank,base.rigour,base.augury,
			base.facts,base.equipmentSlots,Observation.unknown(),Observation.unknown(),fact(Map.of(26394,2L)));
		assertEquals(1,work(26384,"body",counted).tier);
		var insufficient=new AccountSnapshot(base.levels,base.inventory,base.equipment,base.bank,base.rigour,base.augury,
			base.facts,base.equipmentSlots,Observation.unknown(),Observation.unknown(),fact(Map.of(26394,1L)));
		assertTrue(work(26384,"body",insufficient).tier>1);
	}

	private IronmanOrder.Work work(int id,String slot,AccountSnapshot account)
	{
		var before=slot.equals("weapon")?CombatSetup.ranged(NexOwnedFixtures.ranged(),"test",Map.of())
			:CombatSetup.melee(NexOwnedFixtures.melee(),"test",Map.of());
		var pair=new ComparisonResult(before,before.edit(slot,Integer.toString(id)),null,null,null,Map.of(),"test","test","test",List.of(),false,Map.of());
		return IronmanOrder.work(pair,catalog,account,Preferences.EMPTY);
	}

	private AccountSnapshot account(Set<Integer> items,Map<String,Integer> overrides,Boolean ready)
	{
		Map<String,Integer> levels=new HashMap<>();
		for(String skill:List.of("ATTACK","STRENGTH","DEFENCE","RANGED","HITPOINTS","PRAYER","MAGIC",
			"AGILITY","CRAFTING","SMITHING","SLAYER","FLETCHING")) { levels.put(skill,99); }
		levels.putAll(overrides); levels.values().removeIf(v->v<0);
		Map<String,Observation<Boolean>> facts=new HashMap<>();
		if(ready!=null) for(String key:List.of("nex-team","frozen-door","nex-entry","monkey-form","monkey-madness-ii"))
		{ facts.put(key,fact(ready)); }
		return new AccountSnapshot(fact(levels),fact(Set.of()),fact(Set.of()),items==null?Observation.unknown():fact(items),
			fact(true),fact(true),facts);
	}
}
