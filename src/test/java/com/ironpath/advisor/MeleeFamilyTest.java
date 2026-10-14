package com.ironpath.advisor;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MeleeFamilyTest
{
	private KnowledgeCatalog catalog; private Map<Integer,MeleeItem> items;
	@Before public void load() throws Exception
	{ catalog=KnowledgeCatalog.load(new Gson()); items=catalog.melee.items.stream().collect(Collectors.toMap(i->i.id,i->i)); }
	private TargetProfile target(String id) { return catalog.comparison.targets.stream().filter(t->t.id.equals(id)).findFirst().orElseThrow(); }
	private CombatSetup setup(int weapon,String target)
	{ return CombatSetup.melee(MeleeModelTest.slots(weapon),target,Map.of()).edit("style",items.get(weapon).styles.get(0).name()); }
	private PoweredMagic.Metrics metrics(CombatSetup s)
	{ return MeleeModel.calculate(90,90,s,target(s.target),items); }
	@Test public void familyPackagesMatchOriginalPinnedOracle() throws Exception
	{
		try (BufferedReader reader=new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/r3b-family-oracle.csv"))))
		{
			reader.readLine(); String line; int count=0;
			while((line=reader.readLine())!=null)
			{
				String[] r=line.split(","); Map<String,Integer> slots=MeleeModelTest.slots(Integer.parseInt(r[3]));
				String[] names={"head","body","legs","hands","cape","neck","feet","ring","shield"};
				for(int i=0;i<names.length;i++) { slots.put(names[i],Integer.parseInt(r[i+4])); }
				MeleeModel.Souls[] states={MeleeModel.Souls.START,MeleeModel.Souls.ONE,MeleeModel.Souls.TWO,MeleeModel.Souls.THREE,MeleeModel.Souls.FOUR,MeleeModel.Souls.RETAINED};
				CombatSetup s=CombatSetup.melee(slots,r[16],Map.of()).edit("prayer",r[13]).edit("boost",r[14]).edit("style",r[15]).edit("task",r[17]).edit("souls",states[Integer.parseInt(r[18])].name()).edit("defence",r[19]);
				PoweredMagic.Metrics m=MeleeModel.calculate(Integer.parseInt(r[1]),Integer.parseInt(r[2]),s,target(s.target),items);
				assertEquals(line+" max",Integer.parseInt(r[20]),m.maxHit);
				assertEquals(line+" attack",Long.parseLong(r[21]),m.attackRoll);
				assertEquals(line+" defence",Long.parseLong(r[22]),m.defenceRoll);
				assertEquals(line+" chance",Double.parseDouble(r[23]),m.hitChance,1e-12);
				assertEquals(line+" DPS",Double.parseDouble(r[24]),m.dps,1e-10); count++;
			}
			assertTrue(count>10000);
		}
	}
	@Test public void fangSharedDefenceAccuracyAgreesWithIndependentUniformEnumeration()
	{
		for(int attack=0;attack<12;attack++) for(int defence=0;defence<12;defence++)
		{
			int hits=0;
			for(int a=0;a<=attack;a++)for(int b=0;b<=attack;b++)for(int d=0;d<=defence;d++) { if(a>d || b>d) { hits++; } }
			assertEquals(hits/((attack+1.0)*(attack+1)*(defence+1)),FangModel.hitChance(attack,defence,false),1e-12);
			double ordinary=PoweredMagic.hitChance(attack,defence);
			assertEquals(1-(1-ordinary)*(1-ordinary),FangModel.hitChance(attack,defence,true),1e-12);
		}
		CombatSetup slash=setup(26219,"dharok").edit("style","SLASH_AGGRESSIVE");
		PoweredMagic.Metrics m=metrics(slash); assertEquals(PoweredMagic.hitChance(m.attackRoll,m.defenceRoll),m.hitChance,1e-12);
		assertEquals(23,m.maxHit); // Effective strength 101, ordinary max 26; shrink floor(26*3/20)=3.
	}
	@Test public void scytheHitsUseSeparateRoundedMaximaAndSuccessfulZeroExpectations()
	{
		CombatSetup s=setup(22325,"neutral-zero-defence");TargetProfile t=target(s.target);
		// Accurate 90 Strength + 8; floor((98*(75+64)+320)/640)=21.
		assertEquals(21,metrics(s).maxHit);
		t.size=2;PoweredMagic.Metrics two=metrics(s);assertEquals(21+10,two.maxHit);
		t.size=3;PoweredMagic.Metrics three=metrics(s);assertEquals(21+10+5,three.maxHit);
		assertEquals(three.hitChance*(MeleeModel.ordinaryMean(21)+MeleeModel.ordinaryMean(10)+MeleeModel.ordinaryMean(5))/3,three.dps,1e-12);
		assertTrue(three.dps>two.dps);
		t.size=null;
		try { metrics(s);fail("Unknown size"); } catch(IllegalArgumentException expected) { }
	}
	@Test public void soulStateIsPreAttackAdditiveAndRampIsNotFullStacks()
	{
		CombatSetup s=setup(28338,"neutral-zero-defence").edit("prayer","PIETY").edit("boost","SUPER_ATTACK_STRENGTH");
		// Boosted 108: prayer floor 132 and five-stack floor 32 are independently added before stance/+8.
		assertEquals(172,metrics(s.edit("souls","RETAINED")).effectiveMagic);
		double sum=0; int max=0;
		for(String state:List.of("START","ONE","TWO","THREE","FOUR"))
		{ PoweredMagic.Metrics m=metrics(s.edit("souls",state));sum+=m.dps;max=Math.max(max,m.maxHit); }
		assertEquals(sum/5,metrics(s.edit("souls","RAMP")).dps,1e-12); assertEquals(max,metrics(s.edit("souls","RAMP")).maxHit);
		assertTrue(metrics(s.edit("souls","RAMP")).dps<metrics(s.edit("souls","RETAINED")).dps);
		CombatSetup other=setup(4151,"neutral-zero-defence");assertEquals(metrics(other).dps,metrics(other.edit("souls","RETAINED")).dps,0);
		assertFalse(s.edit("souls","RETAINED").edit("confirm","true").unconfirmed().confirmed);
		assertEquals(MeleeModel.Souls.RETAINED,s.edit("souls","RETAINED").unconfirmed().souls);
	}
	@Test public void salveOverridesTaskAndLanceStacksAfterTheChosenCondition()
	{
		CombatSetup s=setup(22978,"vorkath-melee").edit("head","11865").edit("neck","12018");
		assertEquals(metrics(s).dps,metrics(s.edit("task","true")).dps,0);
		assertTrue(metrics(s).dps>metrics(s.edit("neck","0" )).dps);
		PoweredMagic.Metrics b=metrics(s.edit("neck","0")),task=metrics(s.edit("neck","0").edit("task","true"));
		assertTrue(task.attackRoll>b.attackRoll);
		assertEquals(metrics(s.edit("head","0")).dps,metrics(s).dps,0);
	}
	@Test public void currentInquisitorPiecesHaveNoOldFullSetOrMaceException()
	{
		CombatSetup s=setup(24417,"dharok");PoweredMagic.Metrics b=metrics(s);
		assertEquals((90+3+8L)*(102+10+64)*201/200,metrics(s.edit("head","24419")).attackRoll);
		CombatSetup full=s.edit("head","24419").edit("body","24420").edit("legs","24421");
		long ordinary=(90+3+8L)*(102+10+16+12+64);
		assertEquals(ordinary*205/200,metrics(full).attackRoll);
		assertEquals(102,items.get(24417).crush.intValue());assertEquals(96,items.get(24417).strength.intValue());
	}
	@Test public void dragonAndDemonEffectsRespectTargetAndFixedDefenceScenario()
	{
		CombatSetup lance=setup(22978,"olm-melee-scenario");PoweredMagic.Metrics base=metrics(lance),reduced=metrics(lance.edit("defence","80"));
		assertTrue(reduced.hitChance>base.hitChance);assertEquals(base.maxHit,reduced.maxHit);assertEquals(base.attackRoll,reduced.attackRoll);
		assertEquals(89L*(50+64),reduced.defenceRoll);
		assertEquals(-1,lance.edit("defence","80").edit("target","dharok").defenceOverride);
		CombatSetup ember=setup(29589,"duke-melee");TargetProfile t=target(ember.target);PoweredMagic.Metrics duke=metrics(ember);
		t.demonbanePercent=100; assertTrue(metrics(ember).maxHit>duke.maxHit);assertTrue(metrics(ember).attackRoll>duke.attackRoll);
		t.demonbanePercent=null;assertTrue(metrics(ember).dps<duke.dps);
	}
}
