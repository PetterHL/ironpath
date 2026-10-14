package com.ironpath.advisor;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MeleeModelTest
{
	private KnowledgeCatalog catalog;
	private Map<Integer,MeleeItem> items;
	@Before public void load() throws Exception
	{ catalog=KnowledgeCatalog.load(new Gson()); items=catalog.melee.items.stream().collect(Collectors.toMap(i->i.id,i->i)); }
	private TargetProfile target(String id) { return catalog.comparison.targets.stream().filter(t->t.id.equals(id)).findFirst().orElseThrow(); }
	static Map<String,Integer> slots(int weapon)
	{ Map<String,Integer> slots=new LinkedHashMap<>(); PoweredMagic.SLOTS.forEach(s->slots.put(s,0)); slots.put("weapon",weapon); return slots; }
	@Test public void originalCompletePackagesMatchPinnedReference() throws Exception
	{
		try (BufferedReader reader=new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/r3b-oracle.csv"))))
		{
			reader.readLine(); String line; int count=0;
			while ((line=reader.readLine())!=null)
			{
				String[] r=line.split(","); Map<String,Integer> slots=slots(Integer.parseInt(r[3]));
				String[] names={"head","body","legs","hands","cape","neck","feet","ring","shield"};
				for(int i=0;i<names.length;i++){slots.put(names[i],Integer.parseInt(r[i+4]));}
				CombatSetup s=CombatSetup.melee(slots,r[16],Map.of()).edit("prayer",r[13]).edit("boost",r[14]).edit("style",r[15]);
				PoweredMagic.Metrics m=MeleeModel.calculate(Integer.parseInt(r[1]),Integer.parseInt(r[2]),s,target(s.target),items);
				assertEquals(r[0]+" max",Integer.parseInt(r[17]),m.maxHit);
				assertEquals(r[0]+" attack",Long.parseLong(r[18]),m.attackRoll);
				assertEquals(r[0]+" defence",Long.parseLong(r[19]),m.defenceRoll);
				assertEquals(r[0]+" chance",Double.parseDouble(r[20]),m.hitChance,1e-12);
				assertEquals(r[0]+" DPS",Double.parseDouble(r[21]),m.dps,1e-10); count++;
			}
			assertEquals(3168,count);
		}
	}
	@Test public void handDerivedWhipRoundingBoostAndSuccessfulZero()
	{
		CombatSetup s=CombatSetup.melee(slots(4151),"neutral-zero-defence",Map.of());
		PoweredMagic.Metrics m=MeleeModel.calculate(99,99,s,target(s.target),items);
		// Controlled: 99+1+8=108; floor((108*146+320)/640)=25; ordinary NPC defence=(0+9)*64.
		assertEquals(25,m.maxHit);assertEquals(108L*146,m.attackRoll);assertEquals(576,m.defenceRoll);assertEquals(4,m.attackTicks);
		assertEquals(m.hitChance*(12.5+1.0/26)/2.4,m.dps,1e-12);
		m=MeleeModel.calculate(99,99,s.edit("prayer","PIETY").edit("boost","SUPER_ATTACK_STRENGTH"),target(s.target),items);
		// Full super boost 118; prayer floors independently: floor(118*1.23)+9=154 strength.
		assertEquals(118,m.visibleMagic);assertEquals(154,m.effectiveMagic);assertEquals(35,m.maxHit);assertEquals(150L*146,m.attackRoll);
	}
	@Test public void typeSpecificDefenceAndSlowWeaponCannotKeepDefender()
	{
		CombatSetup s=CombatSetup.melee(slots(7158),"dharok",Map.of()).edit("style","SLASH_AGGRESSIVE");
		PoweredMagic.Metrics slash=MeleeModel.calculate(90,90,s,target(s.target),items);
		PoweredMagic.Metrics crush=MeleeModel.calculate(90,90,s.edit("style","CRUSH_AGGRESSIVE"),target(s.target),items);
		assertEquals(109L*314,slash.defenceRoll);assertEquals(109L*308,crush.defenceRoll);assertEquals(7,slash.attackTicks);
		assertTrue(slash.dps>crush.dps);
		try { MeleeModel.calculate(90,90,s.edit("shield","12954"),target(s.target),items);fail("Invalid hands"); }
		catch(IllegalArgumentException expected) { }
		try { MeleeModel.calculate(90,90,s.edit("style","STAB_ACCURATE"),target(s.target),items);fail("Invented style"); }
		catch(IllegalArgumentException expected) { }
	}
}
