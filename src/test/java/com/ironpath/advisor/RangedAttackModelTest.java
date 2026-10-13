package com.ironpath.advisor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.Test;
import static com.ironpath.advisor.RangedAttackModel.*;
import static org.junit.Assert.*;

public class RangedAttackModelTest
{
	private final RangedAttackModel model=new RangedAttackModel();
	private Setup scenario(int level, Family family, Style style, Prayer prayer, int crystal)
	{
		return new Setup(family,style,prayer,family==Family.TWISTED_BOW?Ammo.ORDINARY_ARROW:Ammo.NONE,
			level,level,50,10,family==Family.TWISTED_BOW?60:0,true,crystal==1||crystal==3,crystal>=2,crystal>=2,Set.of());
	}
	private Target target(int magic, int magicAttack, boolean chambers)
	{ return new Target(250,200,magic,magicAttack,chambers,true,true); }
	@Test public void rigourCrystalRoundingAndRapidDpsHaveIndependentHandChecks()
	{
		Result r=model.calculate(scenario(99,Family.BOWFA,Style.RAPID,Prayer.RIGOUR,3),Target.neutral());
		assertTrue(r.reason,r.available());
		assertEquals(126,(int)r.effectiveAccuracy); assertEquals(129,(int)r.effectiveStrength);
		assertEquals(39639,(long)r.attackRoll); assertEquals(576,(long)r.defenceRoll);
		assertEquals(41,(int)r.maxHit); assertEquals(4,(int)r.attackTicks);
		assertEquals(1-578.0/79280,r.hitChance,1e-12);
		assertEquals((1-578.0/79280)*(41.0/2+1.0/42)/2.4,r.dps,1e-12);
	}
	@Test public void rapidAndAccurateChangeDamageAndSpeedWhileLongrangeKeepsOrdinaryDamage()
	{
		Result rapid=model.calculate(scenario(99,Family.BOWFA,Style.RAPID,Prayer.NONE,3),Target.neutral());
		Result accurate=model.calculate(scenario(99,Family.BOWFA,Style.ACCURATE,Prayer.NONE,3),Target.neutral());
		Result longrange=model.calculate(scenario(99,Family.BOWFA,Style.LONGRANGE,Prayer.NONE,3),Target.neutral());
		assertTrue(accurate.maxHit>rapid.maxHit); assertTrue(rapid.dps>accurate.dps);
		assertEquals(rapid.maxHit,longrange.maxHit); assertEquals(rapid.attackRoll,longrange.attackRoll);
		assertEquals(5,(int)longrange.attackTicks); assertEquals(4,(int)rapid.attackTicks);
	}
	@Test public void crystalPiecesAreAdditiveActiveModifiersOnlyForCrystalWeapons()
	{
		Result naked=model.calculate(scenario(99,Family.BOWFA,Style.RAPID,Prayer.NONE,0),Target.neutral());
		Result helm=model.calculate(scenario(99,Family.BOWFA,Style.RAPID,Prayer.NONE,1),Target.neutral());
		Result bodyLegs=model.calculate(scenario(99,Family.BOWFA,Style.RAPID,Prayer.NONE,2),Target.neutral());
		assertEquals(naked.attackRoll*21/20,(long)helm.attackRoll);
		assertEquals(naked.maxHit*41/40,(int)helm.maxHit);
		assertEquals(naked.attackRoll*25/20,(long)bodyLegs.attackRoll);
		assertEquals(naked.maxHit*45/40,(int)bodyLegs.maxHit);
		Result tbow=model.calculate(scenario(99,Family.TWISTED_BOW,Style.RAPID,Prayer.NONE,0),Target.neutral());
		Result withCrystal=model.calculate(scenario(99,Family.TWISTED_BOW,Style.RAPID,Prayer.NONE,3),Target.neutral());
		assertEquals(tbow.maxHit,withCrystal.maxHit); assertEquals(tbow.attackRoll,withCrystal.attackRoll);
		assertEquals(tbow.dps,withCrystal.dps);
	}
	@Test public void replacingCrystalWithFlatStrengthDoesNotAutomaticallyImproveBowfa()
	{
		Setup crystal=new Setup(Family.BOWFA,Style.RAPID,Prayer.RIGOUR,Ammo.NONE,99,99,100,0,0,true,true,true,true,Set.of());
		Setup flatStrength=new Setup(Family.BOWFA,Style.RAPID,Prayer.RIGOUR,Ammo.NONE,99,99,130,8,0,true,false,false,false,Set.of());
		Result before=model.calculate(crystal,Target.neutral()),after=model.calculate(flatStrength,Target.neutral());
		assertTrue(before.maxHit>after.maxHit); assertTrue(before.dps>after.dps);
		// This is an explicit synthetic replacement, not a universal Masori-versus-crystal claim.
	}
	@Test public void tbowUsesHighestOffensiveMagicAndItsContextCapWithIntegerStages()
	{
		Setup s=scenario(99,Family.TWISTED_BOW,Style.RAPID,Prayer.NONE,0);
		Result neutral=model.calculate(s,Target.neutral());
		assertEquals(14,(int)neutral.maxHit); assertEquals(7875,(long)neutral.attackRoll);
		Result level=model.calculate(s,target(250,0,false)),bonus=model.calculate(s,target(0,250,false));
		assertEquals(level.maxHit,bonus.maxHit);assertEquals(level.attackRoll,bonus.attackRoll);
		assertEquals(55,(int)level.maxHit); assertEquals(27563,(long)level.attackRoll);
		Result outside=model.calculate(s,target(500,0,false)),inside=model.calculate(s,target(500,0,true));
		assertEquals(level.maxHit,outside.maxHit); assertEquals(level.attackRoll,outside.attackRoll);
		assertEquals(64,(int)inside.maxHit);assertTrue(inside.dps>outside.dps);
		Result normalBoundary=model.calculate(s,target(249,0,false));
		assertTrue(normalBoundary.maxHit<=level.maxHit);
		assertEquals(inside.maxHit,model.calculate(s,target(350,0,true)).maxHit);
		assertTrue(model.calculate(s,target(349,0,true)).maxHit<=inside.maxHit);
		assertEquals(model.calculate(s,target(244,0,false)).attackRoll,level.attackRoll);
	}
	@Test public void tbowDoesNotDominateCrystalWeaponsAcrossTargetContexts()
	{
		Setup bowfa=scenario(99,Family.BOWFA,Style.RAPID,Prayer.RIGOUR,3);
		Setup tbow=scenario(99,Family.TWISTED_BOW,Style.RAPID,Prayer.RIGOUR,0);
		assertTrue(model.calculate(bowfa,Target.neutral()).dps>model.calculate(tbow,Target.neutral()).dps);
		Target high=target(250,0,false);
		assertTrue(model.calculate(tbow,high).dps>model.calculate(bowfa,high).dps);
	}
	@Test public void unknownOrIncompatibleAmmoAndHistoryEffectsProduceGaps()
	{
		Setup bowfa=new Setup(Family.BOWFA,Style.RAPID,Prayer.NONE,Ammo.ORDINARY_ARROW,99,99,50,10,60,true,false,false,false,Set.of());
		assertFalse(model.calculate(bowfa,Target.neutral()).available());
		for(Ammo ammo:new Ammo[]{Ammo.NONE,Ammo.SEEKER_ARROW})
		{
			Setup tbow=new Setup(Family.TWISTED_BOW,Style.RAPID,Prayer.NONE,ammo,99,99,50,10,null,true,false,false,false,Set.of());
			Result gap=model.calculate(tbow,Target.neutral());assertFalse(gap.available());assertNull(gap.dps);
		}
		Setup history=new Setup(Family.BOWFA,Style.RAPID,Prayer.NONE,Ammo.NONE,99,99,50,10,0,true,false,false,false,Set.of(Unmodelled.SLAYER));
		assertFalse(model.calculate(history,Target.neutral()).available());
		Setup unusable=new Setup(Family.BOWFA,Style.RAPID,Prayer.NONE,Ammo.NONE,99,99,50,10,0,false,false,false,false,Set.of());
		assertFalse(model.calculate(unusable,Target.neutral()).available());
	}
	@Test public void unknownInputsStayUnknownAndUnconfiguredRaidStatsCannotProduceNumbers()
	{
		Setup unknown=new Setup(Family.BOWFA,Style.RAPID,Prayer.NONE,Ammo.NONE,99,99,null,10,0,true,false,false,false,Set.of());
		assertNull(model.calculate(unknown,Target.neutral()).dps);
		Setup tbow=scenario(99,Family.TWISTED_BOW,Style.RAPID,Prayer.NONE,0);
		assertFalse(model.calculate(tbow,new Target(0,0,null,0,false,true,true)).available());
		assertFalse(model.calculate(tbow,new Target(0,0,0,0,true,false,true)).available());
		assertFalse(model.calculate(tbow,new Target(0,0,0,0,false,true,false)).available());
		Setup bowfa=scenario(99,Family.BOWFA,Style.RAPID,Prayer.NONE,0);
		assertTrue(model.calculate(bowfa,new Target(0,0,null,null,false,true,true)).available());
	}
	@Test public void firstRangedFamiliesMatchPinnedOriginalReferenceCases() throws Exception
	{
		int count=0;
		Target[] targets={Target.neutral(),new Target(150,75,100,200,false,true,true),target(500,0,false),target(500,0,true)};
		try(BufferedReader reader=new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/r2-family-oracle.csv"),StandardCharsets.UTF_8)))
		{
			String line;
			while((line=reader.readLine())!=null)
			{
				if(line.startsWith("#")||line.startsWith("ranged"))continue;
				String[] row=line.split(",");
				Result r=model.calculate(scenario(Integer.parseInt(row[0]),Family.valueOf(row[1]),Style.valueOf(row[2].toUpperCase(java.util.Locale.ROOT)),
					Prayer.valueOf(row[3]),Integer.parseInt(row[5])),targets[Integer.parseInt(row[4])]);
				assertTrue(line+": "+r.reason,r.available());
				assertEquals(line,Integer.parseInt(row[6]),(int)r.maxHit);
				assertEquals(line,Long.parseLong(row[7]),(long)r.attackRoll);
				assertEquals(line,Long.parseLong(row[8]),(long)r.defenceRoll);
				assertEquals(line,Double.parseDouble(row[9]),r.hitChance,1e-12);
				assertEquals(line,Double.parseDouble(row[10]),r.dps,1e-10);count++;
			}
		}
		assertEquals(1152,count);
	}
}
