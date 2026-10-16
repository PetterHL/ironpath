package com.ironpath.advisor;

import com.ironpath.encounter.CombatState;
import com.ironpath.encounter.EncounterSummary.Outcome;
import com.ironpath.model.*;
import com.ironpath.nex.NexSummary;
import com.ironpath.preferences.NexMethod;
import java.util.*;
import static com.ironpath.advisor.P4aFixtures.fact;

/** User-reported gear only, 15 October. Levels/prayers/quantities/windows are synthetic test inputs.
 * No bank dump, account identifier, real kill or claim of measured damage is included. */
public final class NexOwnedFixtures
{
	private NexOwnedFixtures() { }
	public static Map<String,Integer> ranged()
	{
		Map<String,Integer> s=new LinkedHashMap<>(); PoweredMagic.SLOTS.forEach(slot->s.put(slot,0));
		s.putAll(Map.of("head",27235,"neck",19547,"cape",28949,"ammo",20235,"body",23975,
			"legs",27241,"feet",31097,"hands",7462,"ring",25975,"weapon",20997)); return s;
	}
	public static Map<String,Integer> melee()
	{
		Map<String,Integer> s=new LinkedHashMap<>(ranged());
		s.putAll(Map.of("head",24271,"neck",29801,"body",11832,"legs",11834,"weapon",26219,
			"shield",12954,"hands",22981)); return s;
	}
	public static CombatState state(Map<String,Integer> slots,Map<Integer,Long> inventory)
	{
		Map<String,Integer> real=new TreeMap<>();
		for(String skill:List.of("ATTACK","STRENGTH","DEFENCE","HITPOINTS","PRAYER","RANGED","MAGIC","AGILITY")) { real.put(skill,99); }
		Map<String,Integer> visible=new TreeMap<>(real); visible.put("ATTACK",118); visible.put("STRENGTH",118); visible.put("RANGED",112);
		return new CombatState(slots,real,visible,Set.of(slots.get("weapon")==26219?"PIETY":"RIGOUR"),1,1000,false,inventory,
			Map.of("quiver-ammo-id-raw",21326,"quiver-ammo-quantity-raw",500,"quiver-splinters-raw",200,"worn-ammo-quantity",1,"inventory-slots-used",20));
	}
	public static NexSummary.Sample sample(String role,CombatState state)
	{ return new NexSummary.Sample(role.equals("ranged")?10:20,"SHADOW",role.equals("ranged")?"EMBRACE_DARKNESS":"FEAR_THE_SHADOW","NEX@1:1:1",role,-1,state); }
	public static NexSummary summary(List<NexSummary.Sample> samples)
	{ return new NexSummary(1,0,100,100,100,0,Outcome.COMPLETE,"synthetic complete",NexMethod.ID,NexMethod.ID,samples,Map.of(),Set.of()); }
	public static AccountSnapshot account(Set<Integer> bank)
	{
		var s=state(melee(),Map.of());
		return new AccountSnapshot(fact(s.realLevels),fact(Set.of()),fact(new HashSet<>(s.slots.values())),
			bank==null?Observation.unknown():fact(bank),fact(true),fact(true));
	}
}
