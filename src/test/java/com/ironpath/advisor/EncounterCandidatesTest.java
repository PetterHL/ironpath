package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.preferences.Preferences;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class EncounterCandidatesTest
{
	@Test public void encounterRolesManualAdditionsAndExactVariants()
	{
		var c=EncounterCandidates.nex();
		var melee=c.suggestions("nex","nex-trio-mp2-one-kill",EncounterCandidates.Role.MELEE,Set.of());
		assertTrue(melee.containsAll(Set.of(27238,27241,22322,26219)));
		assertFalse(melee.contains(24143)); // Exact locked forms remain observed/manual alternatives.
		assertTrue(c.suggestions("nex","nex-trio-mp2-one-kill",EncounterCandidates.Role.MELEE,Set.of(24143)).contains(24143));
		assertFalse(melee.contains(24420)); // Not the entire offensive item catalogue.
		assertFalse(melee.contains(29049)); // Broken Moon equipment is not the usable suggested form.
		assertTrue(melee.contains(29004));
		assertTrue(c.suggestions("nex","nex-trio-mp2-one-kill",EncounterCandidates.Role.MELEE,Set.of(24420)).contains(24420));
		assertFalse(c.suggestions("nex","unsupported-method",EncounterCandidates.Role.MELEE,Set.of()).contains(22322));
		assertFalse(c.suggestions("other-boss","nex-trio-mp2-one-kill",EncounterCandidates.Role.MELEE,Set.of()).contains(22322));
		assertFalse(c.suggestions("nex","nex-trio-mp2-one-kill",EncounterCandidates.Role.RANGED,Set.of()).contains(22322));
		assertTrue(c.suggestions("nex","nex-trio-mp2-one-kill",EncounterCandidates.Role.SPECIAL,Set.of()).contains(27690));
		assertEquals("indexed-search-2026-10-26",c.revision);
		assertTrue(c.items.stream().allMatch(i->i.conditions!=null && !i.conditions.isBlank()));
	}
	@Test public void rangedCandidatesRetainRealAmmunitionCompatibility() throws Exception
	{
		var catalog=KnowledgeCatalog.load(new Gson());
		var crossbow=catalog.ranged.items.stream().filter(i->i.id==26374).findFirst().orElseThrow();
		var bolts=catalog.ranged.items.stream().filter(i->i.id==21944).findFirst().orElseThrow();
		var arrows=catalog.ranged.items.stream().filter(i->i.id==33595).findFirst().orElseThrow();
		assertTrue(RangedModel.compatible(crossbow,bolts)); assertFalse(RangedModel.compatible(crossbow,arrows));
		assertEquals("BOLTS",EncounterCandidates.nex().items.stream().filter(i->i.id==26374).findFirst().orElseThrow().ammoKind);
	}
	@Test public void effortUsesAvailableComponentsAndKeepsUnknownWorkUnknown() throws Exception
	{
		var c=KnowledgeCatalog.load(new Gson()); var evaluator=new NexOwnedEvaluator(c);
		var s=NexOwnedFixtures.sample("melee",NexOwnedFixtures.state(NexOwnedFixtures.melee(),Map.of()));
		var before=evaluator.setup(s,s.state.slots); var slots=new HashMap<>(s.state.slots); slots.put("shield",22322);
		var after=evaluator.setup(s,slots);
		var pair=new ComparisonResult(before,after,null,null,null,Map.of(),"test","test","test",List.of(),false,Map.of());
		assertEquals(0,IronmanOrder.work(pair,c,NexOwnedFixtures.account(Set.of(22322)),Preferences.EMPTY).tier);
		assertEquals(1,IronmanOrder.work(pair,c,NexOwnedFixtures.account(Set.of(22477,12954)),Preferences.EMPTY).tier);
		assertTrue(IronmanOrder.work(pair,c,NexOwnedFixtures.account(null),Preferences.EMPTY).tier>=2);
		// A previously unsuccessful trial count is not even an input to this policy.
		for(double a:List.of(0.001,0.01,0.10)) for(int effort=0;effort<6;effort++)
		{
			assertTrue(IronmanOrder.priority(a*2,effort)>IronmanOrder.priority(a,effort+1));
			assertTrue(IronmanOrder.priority(a,0)>IronmanOrder.priority(a,effort+1));
		}
	}
}
