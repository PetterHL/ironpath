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

public class RangedModelTest
{
	private KnowledgeCatalog catalog;
	private Map<Integer,RangedItem> items;
	@Before public void load() throws Exception
	{ catalog=KnowledgeCatalog.load(new Gson()); items=catalog.ranged.items.stream().collect(Collectors.toMap(i->i.id,i->i)); }
	private TargetProfile target(String id) { return catalog.comparison.targets.stream().filter(t->t.id.equals(id)).findFirst().orElseThrow(); }
	static Map<String,Integer> slots(int weapon)
	{ Map<String,Integer> slots=new LinkedHashMap<>(); PoweredMagic.SLOTS.forEach(s->slots.put(s,0)); slots.put("weapon",weapon); return slots; }
	@Test public void originalCompletePackagesAgreeWithPinnedIndependentReference() throws Exception
	{
		try (BufferedReader reader=new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/r2-oracle.csv"))))
		{
			reader.readLine(); String line; int count=0;
			while ((line=reader.readLine())!=null)
			{
				String[] r=line.split(","); Map<String,Integer> slots=slots(Integer.parseInt(r[2]));
				String[] names={"head","body","legs","hands","cape","neck","ring","shield","ammo"};
				for(int i=0;i<names.length;i++){slots.put(names[i],Integer.parseInt(r[i+3]));}
				CombatSetup s=new CombatSetup(slots,PoweredMagic.Prayer.NONE,PoweredMagic.Boost.NONE,PoweredMagic.Style.ACCURATE,false,false,r[17],Map.of(),CombatSetup.Family.RANGED,
					RangedModel.Prayer.valueOf(r[13]),RangedModel.Style.valueOf(r[15]),Boolean.parseBoolean(r[14]),Boolean.parseBoolean(r[16]),Integer.parseInt(r[12]));
				PoweredMagic.Metrics m=RangedModel.calculate(Integer.parseInt(r[1]),s,target(r[17]),items);
				assertEquals(r[0]+" max",Integer.parseInt(r[18]),m.maxHit);
				assertEquals(r[0]+" attack",Long.parseLong(r[19]),m.attackRoll);
				assertEquals(r[0]+" defence",Long.parseLong(r[20]),m.defenceRoll);
				assertEquals(r[0]+" chance",Double.parseDouble(r[21]),m.hitChance,1e-12);
				assertEquals(r[0]+" DPS",Double.parseDouble(r[22]),m.dps,1e-10); count++;
			}
			assertEquals(1944,count);
		}
	}
	@Test public void handDerivedCrystalRoundingSpeedAndSuccessfulZeroStaySeparate()
	{
		Map<String,Integer> slots=slots(25865);slots.put("head",23971);slots.put("body",23975);slots.put("legs",23979);
		CombatSetup s=CombatSetup.ranged(slots,"neutral-zero-defence",Map.of());
		PoweredMagic.Metrics m=RangedModel.calculate(99,s,target(s.target),items);
		// effective 107, strength 106: floor((107*170+320)/640)=28; full crystal floor(28*46/40)=32.
		assertEquals(32,m.maxHit);assertEquals(4,m.attackTicks);
		assertEquals(107L*(128+9+31+18+64)*26/20,m.attackRoll);assertEquals(576,m.defenceRoll);
		assertEquals(m.hitChance*(16+1.0/33)/2.4,m.dps,1e-12);
		PoweredMagic.Metrics accurate=RangedModel.calculate(99,s.edit("style","ACCURATE"),target(s.target),items);
		assertEquals(5,accurate.attackTicks);assertTrue(m.dps>accurate.dps);
	}
	@Test public void twistedCapsMagicAccuracyAndLowMagicMatchIntegerHandChecks()
	{
		assertEquals(40,RangedModel.twistedPercent(0,true));assertEquals(54,RangedModel.twistedPercent(0,false));
		assertEquals(140,RangedModel.twistedPercent(250,true));assertEquals(215,RangedModel.twistedPercent(250,false));
		assertEquals(248,RangedModel.twistedPercent(350,false));
		Map<String,Integer> slots=slots(20997);slots.put("ammo",11212);CombatSetup setup=CombatSetup.ranged(slots,"zilyana",Map.of());
		TargetProfile t=new TargetProfile();t.scalingSupported=true;t.defenceLevel=0;t.rangedLight=0;t.rangedStandard=0;t.rangedHeavy=0;t.magicLevel=250;t.magicAttack=0;
		int cap=RangedModel.calculate(99,setup,t,items).maxHit;t.magicLevel=3000;assertEquals(cap,RangedModel.calculate(99,setup,t,items).maxHit);
		t.magicLevel=1;t.magicAttack=250;assertEquals(cap,RangedModel.calculate(99,setup,t,items).maxHit);
		t.magicAttack=0;assertTrue(RangedModel.calculate(99,setup,t,items).maxHit<cap);
		t.xerician=true;t.magicLevel=350;assertTrue(RangedModel.calculate(99,setup,t,items).maxHit>cap);
	}
	@Test public void seekingFloorPreservesMissesMaxHitAndAddsOnlyBowAccuracy()
	{
		assertEquals(3.2,RangedModel.successfulMean(4,3),1e-12); // [3,3,3,3,4], with misses excluded.
		Map<String,Integer> slots=slots(20997);slots.put("ammo",11212);CombatSetup s=CombatSetup.ranged(slots,"graardor",Map.of());
		PoweredMagic.Metrics ordinary=RangedModel.calculate(99,s,target(s.target),items);
		slots.put("ammo",33595);PoweredMagic.Metrics seeking=RangedModel.calculate(99,s.rangedCopy(slots,Map.of(),s.target,0),target(s.target),items);
		assertEquals(ordinary.maxHit,seeking.maxHit);assertEquals(ordinary.attackBonus+20,seeking.attackBonus);
		assertTrue(seeking.hitChance<1);assertTrue(seeking.dps>ordinary.dps);
	}
	@Test public void quiverAndDartBonusesRespectProjectileCompatibility()
	{
		Map<String,Integer> bow=slots(25865);bow.put("cape",28955);CombatSetup s=CombatSetup.ranged(bow,"neutral-zero-defence",Map.of());
		PoweredMagic.Metrics a=RangedModel.calculate(99,s,target(s.target),items);assertEquals(146,a.attackBonus);assertEquals(109,a.damagePercent,0);
		Map<String,Integer> arrow=slots(20997);arrow.put("cape",28955);arrow.put("ammo",11212);
		a=RangedModel.calculate(99,CombatSetup.ranged(arrow,s.target,Map.of()),target(s.target),items);assertEquals(98,a.attackBonus);assertEquals(84,a.damagePercent,0);
		Map<String,Integer> pipe=slots(12926);pipe.put("cape",28955);pipe.put("ammo",33595);
		a=RangedModel.calculate(99,CombatSetup.ranged(pipe,s.target,Map.of()).rangedCopy(pipe,Map.of(),s.target,11230),target(s.target),items);
		assertEquals(48,a.attackBonus);assertEquals(58,a.damagePercent,0); // Arrow/charged quiver/dart accuracy excluded.
		assertFalse(RangedModel.compatible(items.get(31583),items.get(21330)));assertTrue(RangedModel.compatible(items.get(31583),items.get(811)));
		assertFalse(RangedModel.compatible(items.get(9185),items.get(21905)));assertTrue(RangedModel.compatible(items.get(11785),items.get(21905)));
		assertFalse(RangedModel.compatible(items.get(12788),items.get(11212)));assertTrue(RangedModel.compatible(items.get(12788),items.get(33589)));
	}
	@Test public void partialVoidAndCrystalSlayerOrderDoNotManufactureSetDamage()
	{
		Map<String,Integer> slots=slots(25865);slots.put("head",11865);slots.put("body",23975);slots.put("legs",23979);
		TargetProfile t=target("graardor");t.slayer=true;
		CombatSetup s=CombatSetup.ranged(slots,t.id,Map.of()).edit("style","ACCURATE").edit("task","true");
		assertEquals(36,RangedModel.calculate(99,s,t,items).maxHit);
		assertEquals(32,RangedModel.calculate(99,s.edit("task","false"),t,items).maxHit);
		slots=slots(12788);slots.put("ammo",892);slots.put("head",11664);slots.put("body",13072);slots.put("legs",13073);slots.put("hands",8842);
		CombatSetup full=CombatSetup.ranged(slots,"neutral-zero-defence",Map.of());int max=RangedModel.calculate(99,full,target(full.target),items).maxHit;
		slots.put("hands",0);assertTrue(RangedModel.calculate(99,full.rangedCopy(slots,Map.of(),full.target,0),target(full.target),items).maxHit<max);
	}
}
