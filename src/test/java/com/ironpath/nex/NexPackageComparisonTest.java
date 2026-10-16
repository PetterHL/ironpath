package com.ironpath.nex;

import com.google.gson.Gson;
import com.ironpath.advisor.*;
import com.ironpath.encounter.CombatState;
import com.ironpath.model.Truth;
import com.ironpath.preferences.NexMethod;
import java.time.Instant;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.nex.NexPackageComparison.*;
import static com.ironpath.nex.SpecTimeline.*;
import static com.ironpath.nex.SpecAttackPlan.*;
import static org.junit.Assert.*;

/** Anonymous synthetic scenarios: all levels, timings, HP, stock and resource outcomes are assumptions. */
public class NexPackageComparisonTest
{
	private NexPackageComparison bridge;
	private KnowledgeCatalog catalog;
	private static final Instant TIME=Instant.parse("2026-10-16T10:00:00Z");
	private static <T> ScenarioFact<T> f(T value)
	{ return ScenarioFact.assumed(value,"Anonymous synthetic fixture; not a captured kill","Nex Trio MP2 one-trip scenario",TIME); }
	private static <T> ScenarioFact<T> unknown(String input)
	{ return ScenarioFact.unknown(input+" unread",input); }
	@Before public void load() throws Exception
	{ catalog=KnowledgeCatalog.load(new Gson()); bridge=new NexPackageComparison(catalog); }
	private static Map<String,Integer> slots(int weapon,int cape,int ammo)
	{
		Map<String,Integer> s=new LinkedHashMap<>(); PoweredMagic.SLOTS.forEach(slot->s.put(slot,0));
		s.put("ring",25975); s.put("weapon",weapon); s.put("cape",cape); s.put("ammo",ammo); return s;
	}
	private static CombatState state(Map<String,Integer> slots)
	{
		Map<String,Integer> real=new TreeMap<>();
		for(String skill:List.of("ATTACK","STRENGTH","DEFENCE","RANGED","MAGIC","PRAYER","HITPOINTS","AGILITY")) { real.put(skill,99); }
		Map<String,Integer> visible=new TreeMap<>(real); visible.put("ATTACK",118); visible.put("STRENGTH",118); visible.put("RANGED",112);
		boolean melee=Set.of(27690,26219).contains(slots.get("weapon"));
		return new CombatState(slots,real,visible,Set.of(melee?"PIETY":"RIGOUR"),1,1000,false,Map.of(),Map.of());
	}
	private static Loadout loadout(String role,Map<String,Integer> slots,int projectile)
	{ return new Loadout(role,f(state(slots)),f(projectile),f(Retrieval.NONE)); }
	private static Step attack(int tick)
	{ return Step.attack(tick,f(new Context("NEX","SMOKE",Window.BASE)),f(true),f(500),f(99),f(200),f(true)); }
	private static Conditions conditions(int end,List<Step> steps)
	{ return new Conditions("Synthetic supplied Nex opportunities; fixed HP at each step",f(end),f(1000),f(25),f(RestoreOrder.BEFORE_EVENTS),
		f(Start.ATTACK_READY_NO_CHARGE_CAST_READY),f(false),steps); }
	private static Action action(String key,Attack attack)
	{ return new Action(key,attack,f(false),f(true)); }
	private static Stock stock(Collection<Loadout> packages,Map<Integer,Integer> ammunition,int splinters,int space)
	{
		Map<Integer,Integer> gear=new TreeMap<>();
		for(Loadout p:packages) for(int id:p.state.value().slots.values()) if(id>0 && !ammunition.containsKey(id)) { gear.put(id,1); }
		return new Stock(Availability.CARRIED,f(gear),f(ammunition),f(splinters),f(space),
			f(Map.of("fremennik-exiles",Truth.TRUE,"knight-waves",Truth.TRUE)),f(false),Set.of());
	}
	private static Scenario scenario(Kind kind,Map<String,Loadout> loadouts,Map<Integer,Action> actions,Stock stock)
	{ return new Scenario("Synthetic "+kind,kind,loadouts,actions,stock); }
	private Evaluation evaluate(Conditions c,Scenario s)
	{ return bridge.evaluate(c,s,NexMethod.PILOT); }
	private static void runnable(Evaluation e)
	{ assertNotNull(e.resourceGaps.toString(),e.plan); }

	@Test public void threeCompletePackagesDeriveTheirOwnVoidwakerMaxAndSeparateMarginalGains()
	{
		var actual=loadout("spec",slots(27690,0,0),0);
		var ownedSlots=new HashMap<>(actual.state.value().slots); ownedSlots.put("neck",29801);
		var owned=loadout("spec",ownedSlots,0); var upgradeSlots=new HashMap<>(ownedSlots); upgradeSlots.put("cape",21295);
		var upgrade=loadout("spec",upgradeSlots,0);
		Stock base=stock(List.of(actual,owned),Map.of(),0,10);
		Stock extra=stock(List.of(actual,owned,upgrade),Map.of(),0,10);
		extra=new Stock(extra.availability,extra.equipment,extra.ammunition,extra.splinters,extra.inventorySpace,extra.unlocks,extra.bankPacking,Set.of(21295));
		var c=conditions(1,List.of(attack(0))); var actions=Map.of(0,action("spec",Attack.VOIDWAKER));
		var result=bridge.compare(c,scenario(Kind.ACTUAL,Map.of("spec",actual),actions,base),
			scenario(Kind.OWNED,Map.of("spec",owned),actions,base),scenario(Kind.UPGRADE,Map.of("spec",upgrade),actions,extra),NexMethod.PILOT);
		var a=result.actualToOwned.before; var o=result.actualToOwned.after; var u=result.ownedToUpgrade.after;
		runnable(a); runnable(o); runnable(u);
		// Hand arithmetic: floor(118*1.23)+3+8=156; strengths 80,92,100 -> ordinary max 35,38,40.
		assertEquals(35,a.launches.get(0).chargedMetrics.maxHit); assertEquals(38,o.launches.get(0).chargedMetrics.maxHit);
		assertEquals(40,u.launches.get(0).chargedMetrics.maxHit);
		assertEquals(34.5,a.plan.expectedDamage,1e-12); assertEquals(38,o.plan.expectedDamage,1e-12); assertEquals(40,u.plan.expectedDamage,1e-12);
		assertEquals(3.5,result.actualToOwned.expectedDamage,1e-12); assertEquals(2,result.ownedToUpgrade.expectedDamage,1e-12);
		assertEquals("0 -> 29801",result.actualToOwned.packageChanges.get("spec").get("neck"));
		assertEquals(Integer.valueOf(0),actual.state.value().slots.get("neck"));
		assertFalse(base.equipment.value().containsKey(21295)); assertFalse(base.hypothetical.contains(21295));
		assertEquals(NexPackageComparison.VERSION,u.versions.get("bridge")); assertTrue(u.versions.containsKey("n3-package-equipment"));
		assertTrue(u.evidence.toString().contains("ASSUMED")); assertTrue(u.evidence.toString().contains("not observed outcomes"));
	}
	@Test public void identicalPlansGiveZeroAndSupportedWorseCompletePackagesKeepNegativeDeltas()
	{
		var goodSlots=slots(27690,0,0); goodSlots.put("neck",29801);
		var good=loadout("spec",goodSlots,0); var worse=loadout("spec",slots(27690,0,0),0);
		var stock=stock(List.of(good,worse),Map.of(),0,10); var c=conditions(1,List.of(attack(0))); var actions=Map.of(0,action("s",Attack.VOIDWAKER));
		var a=evaluate(c,scenario(Kind.ACTUAL,Map.of("s",good),actions,stock));
		var same=evaluate(c,scenario(Kind.OWNED,Map.of("s",good),actions,stock));
		assertEquals(0,bridge.compare(a,same).expectedDamage,0); assertEquals(Integer.valueOf(0),bridge.compare(a,same).spentEnergy);
		var bad=evaluate(c,scenario(Kind.OWNED,Map.of("s",worse),actions,stock));
		assertEquals(-3.5,bridge.compare(a,bad).expectedDamage,1e-12);
		assertTrue(bridge.compare(a,same).packageChanges.isEmpty());
	}
	@Test public void zcbRubyAndDiamondDeriveRollsAndDamageFromExactSelectedBoltPackages()
	{
		for(int ammo:new int[]{21944,21946})
		{
			var l=loadout("spec",slots(26374,28949,20235),ammo); var stock=stock(List.of(l),Map.of(ammo,2),1,10);
			var c=conditions(1,List.of(attack(0))); var result=evaluate(c,scenario(Kind.ACTUAL,Map.of("s",l),Map.of(0,action("s",Attack.ZARYTE_CROSSBOW)),stock));
			runnable(result); var d=result.launches.get(0); var m=d.chargedMetrics;
			// Full package: 110+18+10 ranged attack; 122+3+1 strength. Effective 142/145.
			assertEquals(138,m.attackBonus); assertEquals(126,m.damagePercent,0); assertEquals(43,m.maxHit);
			assertEquals(28684,m.attackRoll); assertEquals(57566,m.defenceRoll); assertEquals(27264,d.unchargedMetrics.attackRoll);
			double accuracy=57368.0/(2*57567),proc=accuracy+(1-accuracy)*(ammo==21944?.06:.10);
			assertEquals(proc,d.chargedDamage.procChance,1e-12);
			assertEquals(proc*(ammo==21944?110:27+1.0/55),result.plan.expectedDamage,1e-12);
			assertEquals(ammo==21944?proc*9:0,result.plan.expectedSelfDamage,1e-12);
			assertEquals(5,d.inputs.delay); assertEquals(Integer.valueOf(20235),d.inputs.charged.slots.get("ammo"));
			assertEquals(ammo,d.inputs.charged.loadedAmmo); assertEquals(750,result.plan.resources.spent);
		}
	}
	@Test public void specsDisplaceFangAttacksUsingTheirOwnDelaysAndOwnMeleeMax()
	{
		var fang=loadout("melee",slots(26219,0,0),0); var vw=loadout("spec",slots(27690,0,0),0);
		var zcb=loadout("spec",slots(26374,28949,20235),21944);
		var stock=stock(List.of(fang,vw,zcb),Map.of(21944,10),10,20);
		List<Step> steps=new ArrayList<>(); Map<Integer,Action> vactions=new TreeMap<>(),zactions=new TreeMap<>();
		for(int tick=0;tick<10;tick++)
		{ steps.add(attack(tick)); vactions.put(tick,action(tick==0?"spec":"melee",tick==0?Attack.VOIDWAKER:Attack.ORDINARY));
			zactions.put(tick,action(tick==0?"spec":"melee",tick==0?Attack.ZARYTE_CROSSBOW:Attack.ORDINARY)); }
		var c=conditions(10,steps);
		var v=evaluate(c,scenario(Kind.ACTUAL,Map.of("melee",fang,"spec",vw),vactions,stock));
		var z=evaluate(c,scenario(Kind.OWNED,Map.of("melee",fang,"spec",zcb),zactions,stock));
		runnable(v); runnable(z);
		assertEquals(2,v.plan.resources.ordinaryAttacks); assertEquals(1,z.plan.resources.ordinaryAttacks);
		assertEquals(35,v.launches.get(0).chargedMetrics.maxHit); assertEquals(35,v.launches.get(4).chargedMetrics.maxHit);
		// Fang base max=41, inclusive 6..35, outside-ToA correlated accuracy against stab defence 27976.
		var fm=v.launches.get(4).chargedMetrics; assertEquals(6,fm.minHit); assertEquals(25181,fm.attackRoll);
		double fangChance=25181.0*(4*25181.0+5)/(6*25182.0*27977.0),fangDamage=20.5*fangChance;
		assertEquals(fangDamage,v.launches.get(4).chargedDamage.expectedDamage,1e-12);
		assertEquals(34.5+2*fangDamage,v.plan.expectedDamage,1e-12);
		assertEquals(z.launches.get(0).chargedDamage.expectedDamage+fangDamage,z.plan.expectedDamage,1e-12);
		assertEquals(z.plan.expectedDamage-v.plan.expectedDamage,bridge.compare(v,z).expectedDamage,1e-12);
		assertEquals(Integer.valueOf(-1),bridge.compare(v,z).ordinaryAttacks); assertEquals(Integer.valueOf(250),bridge.compare(v,z).spentEnergy);
	}
	@Test public void lastSplinterBoostsItsShotThenUsesIndependentlyRecomputedUnchargedPackage()
	{
		var charged=loadout("ranged",slots(26374,28949,20235),21946); var blessed=loadout("ranged",slots(26374,28955,20235),21946);
		var stock=stock(List.of(charged,blessed),Map.of(21946,2),1,10); var c=conditions(6,List.of(attack(0),attack(5)));
		var actions=Map.of(0,action("r",Attack.ORDINARY),1,action("r",Attack.ORDINARY));
		var a=evaluate(c,scenario(Kind.ACTUAL,Map.of("r",charged),actions,stock));
		var b=evaluate(c,scenario(Kind.OWNED,Map.of("r",blessed),actions,stock)); runnable(a); runnable(b);
		assertEquals(a.plan.hits.get(0).damage.expectedDamage,b.plan.hits.get(0).damage.expectedDamage,0);
		assertTrue(a.plan.hits.get(0).charged); assertFalse(a.plan.hits.get(1).charged); assertTrue(b.plan.hits.get(1).charged);
		assertEquals(1,a.plan.spentSplinters); assertEquals(0,b.plan.spentSplinters); assertEquals(1.0/3,a.plan.resourceBranchProbability,1e-12);
		assertEquals(1,b.plan.resourceBranchProbability,0); assertEquals(a.launches.get(1).unchargedDamage.expectedDamage,a.plan.hits.get(1).damage.expectedDamage,0);
		assertEquals(Integer.valueOf(28949),charged.state.value().slots.get("cape"));
		assertEquals(Integer.valueOf(28947),a.packages.get("r").uncharged.slots.get("cape"));
		assertTrue(bridge.compare(a,b).expectedDamage>0); assertEquals(Integer.valueOf(-1),bridge.compare(a,b).spentSplinters);
	}
	@Test public void ordinaryAndSpecRolesShareTheLastExactProjectileAndRejectedShotsSpendNothing()
	{
		var ordinary=loadout("ranged",slots(26374,28949,20235),21944); var spec=loadout("spec",slots(26374,28949,20235),21944);
		var stock=stock(List.of(ordinary,spec),Map.of(21944,1),1,10); var c=conditions(7,List.of(attack(0),attack(1),attack(5),attack(6)));
		var actions=Map.of(0,action("r",Attack.ORDINARY),1,action("s",Attack.ZARYTE_CROSSBOW),2,action("s",Attack.ZARYTE_CROSSBOW),3,action("s",Attack.ZARYTE_CROSSBOW));
		var result=evaluate(c,scenario(Kind.ACTUAL,Map.of("r",ordinary,"s",spec),actions,stock)); runnable(result);
		assertEquals(1,result.plan.resources.ordinaryAttacks); assertEquals(0,result.plan.resources.zaryteSpecs); assertEquals(0,result.plan.resources.spent);
		assertEquals(Integer.valueOf(1),result.plan.spentAmmo.get(21944)); assertEquals(1,result.plan.spentSplinters);
		assertEquals(1,result.plan.hits.size()); assertEquals(1.0/3,result.plan.resourceBranchProbability,1e-12);
		assertEquals(Status.ATTACK_DELAY,result.plan.resources.entries.get(1).status);
		assertEquals(Status.NO_AMMUNITION,result.plan.resources.entries.get(2).status);
		assertEquals(Status.NO_AMMUNITION,result.plan.resources.entries.get(3).status);
	}
	@Test public void explicitAmmoSavingIsAConditionalBranchAndDoesNotDuplicateStockAcrossRoles()
	{
		var base=loadout("ranged",slots(26374,28955,20235),21944);
		var l=new Loadout(base.role,base.state,base.projectile,f(Retrieval.ASSEMBLER));
		var stock=stock(List.of(l),Map.of(21944,1),0,10); var c=conditions(6,List.of(attack(0),attack(5)));
		var actions=Map.of(0,new Action("r",Attack.ORDINARY,f(true),unknown("inapplicable blessed charge roll")),1,action("r",Attack.ORDINARY));
		var r=evaluate(c,scenario(Kind.ACTUAL,Map.of("r",l),actions,stock)); runnable(r);
		assertEquals(2,r.plan.hits.size()); assertEquals(Integer.valueOf(1),r.plan.spentAmmo.get(21944)); assertEquals(.8*.2,r.plan.resourceBranchProbability,1e-12);
		assertEquals(0,r.plan.spentSplinters);
	}
	@Test public void missingHpBlocksDamageWhileFiniteResourcesAndOtherLaunchesSurvive()
	{
		var l=loadout("spec",slots(27690,0,0),0); var stock=stock(List.of(l),Map.of(),0,10);
		Step missing=Step.attack(0,f(new Context("NEX","SMOKE",Window.BASE)),f(true),unknown("target HP"),f(99),f(200),f(true));
		var c=conditions(5,List.of(missing,attack(4))); var actions=Map.of(0,action("s",Attack.VOIDWAKER),1,action("s",Attack.VOIDWAKER));
		var e=evaluate(c,scenario(Kind.ACTUAL,Map.of("s",l),actions,stock)); runnable(e);
		assertNull(e.plan.expectedDamage); assertEquals(1000,e.plan.resources.spent); assertEquals(2,e.plan.hits.size());
		assertTrue(e.plan.gaps.toString().contains("target HP")); assertNotNull(e.launches.get(1).chargedDamage);
		assertEquals(34.5,e.plan.hits.get(1).damage.expectedDamage,0);
	}
	@Test public void unknownStartOpportunitiesAndStockNeverBecomeZeroButIndependentPackageInputsRemain()
	{
		var l=loadout("spec",slots(27690,0,0),0); var stock=stock(List.of(l),Map.of(),0,10); var s=scenario(Kind.ACTUAL,Map.of("s",l),Map.of(0,action("s",Attack.VOIDWAKER)),stock);
		var base=conditions(1,List.of(attack(0)));
		var c=new Conditions(base.scope,base.endTick,unknown("starting energy"),base.firstRestoreTick,base.order,base.start,base.hardKandarin,base.steps);
		var e=evaluate(c,s); assertNull(e.plan); assertTrue(e.resourceGaps.toString().contains("starting energy")); assertEquals(34.5,e.launches.get(0).chargedDamage.expectedDamage,0);
		Step unready=Step.attack(0,f(new Context("NEX","SMOKE",Window.BASE)),unknown("eligible opportunity"),f(500),f(99),f(200),f(true));
		e=evaluate(conditions(1,List.of(unready)),s); assertNull(e.plan); assertTrue(e.resourceGaps.toString().contains("eligible opportunity"));
		var unread=new Stock(stock.availability,unknown("usable equipment"),stock.ammunition,stock.splinters,stock.inventorySpace,stock.unlocks,stock.bankPacking,Set.of());
		e=evaluate(base,scenario(Kind.ACTUAL,s.loadouts,s.actions,unread)); assertNull(e.plan); assertTrue(e.resourceGaps.toString().contains("usable equipment"));
		assertNotNull(e.launches.get(0).chargedDamage);
	}
	@Test public void unsupportedZarosMinionAndSpecialWindowsNeverUseOrdinaryNexDamage()
	{
		var l=loadout("spec",slots(27690,0,0),0); var stock=stock(List.of(l),Map.of(),0,10);
		for(Context ctx:List.of(new Context("NEX","ZAROS",Window.BASE),new Context("FUMUS","SMOKE",Window.BASE),new Context("NEX","BLOOD",Window.SPECIAL)))
		{
			Step step=Step.attack(0,f(ctx),f(true),f(500),f(99),f(200),f(true));
			var e=evaluate(conditions(1,List.of(step)),scenario(Kind.ACTUAL,Map.of("s",l),Map.of(0,action("s",Attack.VOIDWAKER)),stock));
			runnable(e); assertNull(e.plan.expectedDamage); assertNull(e.launches.get(0).chargedMetrics); assertEquals(500,e.plan.resources.spent);
			assertFalse(e.plan.gaps.isEmpty());
		}
	}
	@Test public void fullReportedSwitchPackagesKeepDefenderAndSharedGearWithoutRewritingHistory()
	{
		var melee=loadout("melee",NexOwnedFixtures.melee(),0); var ranged=loadout("ranged",NexOwnedFixtures.ranged(),21326);
		var vwslots=new HashMap<>(NexOwnedFixtures.melee()); vwslots.put("weapon",27690); var vw=loadout("spec",vwslots,0);
		var stock=stock(List.of(melee,ranged,vw),Map.of(21326,5),2,20); var c=conditions(15,List.of(attack(0),attack(5),attack(10)));
		var s=scenario(Kind.ACTUAL,Map.of("m",melee,"r",ranged,"s",vw),Map.of(0,action("r",Attack.ORDINARY),1,action("m",Attack.ORDINARY),2,action("s",Attack.VOIDWAKER)),stock);
		var e=evaluate(c,s); runnable(e); assertNotNull(e.plan.expectedDamage);
		assertEquals(Integer.valueOf(0),e.packages.get("r").charged.slots.get("shield"));
		assertEquals(Integer.valueOf(12954),e.packages.get("m").charged.slots.get("shield"));
		assertEquals(melee.state.value().slots,e.packages.get("m").charged.slots);
		assertEquals(72,e.launches.get(0).chargedMetrics.maxHit); assertEquals(53303,e.launches.get(0).chargedMetrics.attackRoll);
	}
	@Test public void exactFormsLocksRequirementsHandsAndSharedInventoryAreEnforced()
	{
		var good=loadout("spec",slots(27690,0,0),0); var pool=stock(List.of(good),Map.of(),0,10); var c=conditions(1,List.of(attack(0))); var actions=Map.of(0,action("s",Attack.VOIDWAKER));
		var own=scenario(Kind.OWNED,Map.of("s",good),actions,pool);
		var e=bridge.evaluate(c,own,NexMethod.PILOT.lock("spec/neck",29801)); assertNull(e.plan); assertTrue(e.resourceGaps.toString().contains("lock"));
		Map<String,Integer> unknownForm=new HashMap<>(good.state.value().slots); unknownForm.put("shield",20463);
		var bad=loadout("spec",unknownForm,0); e=evaluate(c,scenario(Kind.OWNED,Map.of("s",bad),actions,pool));
		assertNull(e.plan); assertTrue(e.resourceGaps.toString().contains("exact melee item"));
		var twoSlots=slots(20997,0,21326); twoSlots.put("shield",21000); var two=loadout("ranged",twoSlots,0);
		e=evaluate(c,scenario(Kind.OWNED,Map.of("r",two),Map.of(0,action("r",Attack.ORDINARY)),stock(List.of(two),Map.of(21326,2),0,10)));
		assertNull(e.plan); assertTrue(e.resourceGaps.toString().contains("offhand"));
		var low=good.state.value(); var real=new HashMap<>(low.realLevels); real.put("ATTACK",74);
		var lowState=new CombatState(low.slots,real,low.visibleLevels,low.prayers,low.style,1000,false,Map.of(),Map.of());
		var under=new Loadout("spec",f(lowState),f(0),f(Retrieval.NONE)); e=evaluate(c,scenario(Kind.OWNED,Map.of("s",under),actions,pool));
		assertNull(e.plan); assertTrue(e.resourceGaps.toString().contains("Equip requirement"));
		var bow=loadout("ranged",slots(20997,0,21326),0); var cramped=stock(List.of(good,bow),Map.of(21326,1),0,0);
		e=evaluate(c,scenario(Kind.OWNED,Map.of("s",good,"r",bow),actions,cramped));
		assertNull(e.plan); assertTrue(e.resourceGaps.toString().contains("inventory space"));
		assertEquals(Integer.valueOf(0),good.state.value().slots.get("shield"));
	}
	@Test public void bankPresenceNeedsUsableQuantitiesAndJointPackingAndCannotBecomeCarriedStock()
	{
		var l=loadout("spec",slots(27690,0,0),0); var stock=stock(List.of(l),Map.of(),0,10); var c=conditions(1,List.of(attack(0)));
		var bank=new Stock(Availability.BANK_PREPARATION,stock.equipment,stock.ammunition,stock.splinters,stock.inventorySpace,stock.unlocks,unknown("joint bank packing"),Set.of());
		var e=evaluate(c,scenario(Kind.OWNED,Map.of("s",l),Map.of(0,action("s",Attack.VOIDWAKER)),bank));
		assertNull(e.plan); assertTrue(e.resourceGaps.toString().contains("joint bank packing"));
		var validated=new Stock(bank.availability,bank.equipment,bank.ammunition,bank.splinters,bank.inventorySpace,bank.unlocks,f(true),Set.of());
		e=evaluate(c,scenario(Kind.OWNED,Map.of("s",l),Map.of(0,action("s",Attack.VOIDWAKER)),validated)); runnable(e);
		assertEquals(Availability.BANK_PREPARATION,e.scenario.stock.availability);
		var missing=new Stock(Availability.CARRIED,f(Map.of(25975,1)),stock.ammunition,stock.splinters,stock.inventorySpace,stock.unlocks,f(false),Set.of());
		e=evaluate(c,scenario(Kind.OWNED,Map.of("s",l),Map.of(0,action("s",Attack.VOIDWAKER)),missing)); assertNull(e.plan);
		assertTrue(e.resourceGaps.toString().contains("usable CARRIED stock"));
	}
	@Test public void unrelatedUnsupportedAlternativeCannotSuppressASupportedExplicitPair()
	{
		var good=loadout("spec",slots(27690,0,0),0); var bad=loadout("ranged",slots(29591,0,21326),0);
		var stock=stock(List.of(good,bad),Map.of(21326,2),0,10); var c=conditions(1,List.of(attack(0))); var actions=Map.of(0,action("s",Attack.VOIDWAKER));
		var a=evaluate(c,scenario(Kind.ACTUAL,Map.of("s",good),actions,stock)); var b=evaluate(c,scenario(Kind.OWNED,Map.of("s",good),actions,stock));
		var unsupported=evaluate(c,scenario(Kind.OWNED,Map.of("r",bad),Map.of(0,action("r",Attack.ORDINARY)),stock));
		assertNull(unsupported.plan); assertEquals(0,bridge.compare(a,b).expectedDamage,0); assertNull(bridge.compare(a,unsupported).expectedDamage);
	}
	@Test public void comparisonRejectsChangedHorizonLevelsOrManufacturedOwnedStock()
	{
		var l=loadout("spec",slots(27690,0,0),0); var stock=stock(List.of(l),Map.of(),0,10); var c=conditions(1,List.of(attack(0)));
		var actions=Map.of(0,action("s",Attack.VOIDWAKER)); var s=scenario(Kind.ACTUAL,Map.of("s",l),actions,stock); var a=evaluate(c,s);
		assertThrows(IllegalArgumentException.class,()->bridge.compare(a,evaluate(conditions(2,c.steps),s)));
		var extra=new HashMap<>(stock.equipment.value()); extra.put(21295,1);
		var manufactured=new Stock(stock.availability,f(extra),stock.ammunition,stock.splinters,stock.inventorySpace,stock.unlocks,stock.bankPacking,Set.of());
		assertThrows(IllegalArgumentException.class,()->bridge.compare(a,evaluate(c,scenario(Kind.OWNED,Map.of("s",l),actions,manufactured))));
		assertThrows(IllegalArgumentException.class,()->scenario(Kind.OWNED,Map.of("s",l),actions,new Stock(stock.availability,stock.equipment,stock.ammunition,stock.splinters,stock.inventorySpace,stock.unlocks,stock.bankPacking,Set.of(27690))));
		var previous=l.state.value(); var visible=new HashMap<>(previous.visibleLevels); visible.put("STRENGTH",120);
		var changed=new Loadout("spec",f(new CombatState(previous.slots,previous.realLevels,visible,previous.prayers,previous.style,1000,false)),f(0),f(Retrieval.NONE));
		assertThrows(IllegalArgumentException.class,()->bridge.compare(a,evaluate(c,scenario(Kind.OWNED,Map.of("s",changed),actions,stock))));
	}
	@Test public void renamedLoadoutsCannotChangeLevelsPrayerOrStyleAtTheSameOpportunity()
	{
		var base=loadout("spec",slots(27690,0,0),0); var state=base.state.value();
		var visible=new HashMap<>(state.visibleLevels); visible.put("STRENGTH",99);
		var real=new HashMap<>(state.realLevels); real.put("STRENGTH",98);
		var changedStates=List.of(
			new CombatState(state.slots,state.realLevels,visible,state.prayers,state.style,1000,false),
			new CombatState(state.slots,real,state.visibleLevels,state.prayers,state.style,1000,false),
			new CombatState(state.slots,state.realLevels,state.visibleLevels,Set.of(),state.style,1000,false),
			new CombatState(state.slots,state.realLevels,state.visibleLevels,state.prayers,0,1000,false));
		var stock=stock(List.of(base),Map.of(),0,10); var c=conditions(1,List.of(attack(0)));
		var before=evaluate(c,scenario(Kind.ACTUAL,Map.of("observed-spec",base),Map.of(0,action("observed-spec",Attack.VOIDWAKER)),stock));
		runnable(before);
		for(var changed:changedStates)
		{
			var alternative=new Loadout("spec",f(changed),f(0),f(Retrieval.NONE));
			var after=evaluate(c,scenario(Kind.OWNED,Map.of("candidate-spec",alternative),Map.of(0,action("candidate-spec",Attack.VOIDWAKER)),stock));
			runnable(after); assertThrows(IllegalArgumentException.class,()->bridge.compare(before,after));
			assertThrows(IllegalArgumentException.class,()->bridge.compare(after,before));
		}
		var renamed=evaluate(c,scenario(Kind.OWNED,Map.of("candidate-spec",base),Map.of(0,action("candidate-spec",Attack.VOIDWAKER)),stock));
		runnable(renamed); assertEquals(0,bridge.compare(before,renamed).expectedDamage,0);
	}
	@Test public void comparisonMatchesTimeVaryingStatesByOpportunityRatherThanLoadoutName()
	{
		var boosted=loadout("spec",slots(27690,0,0),0); var state=boosted.state.value();
		var visible=new HashMap<>(state.visibleLevels); visible.put("STRENGTH",99);
		var unboosted=new Loadout("spec",f(new CombatState(state.slots,state.realLevels,visible,state.prayers,state.style,1000,false)),f(0),f(Retrieval.NONE));
		var stock=stock(List.of(boosted,unboosted),Map.of(),0,10); var c=conditions(5,List.of(attack(0),attack(4)));
		var actions=Map.of(0,action("first",Attack.VOIDWAKER),1,action("second",Attack.VOIDWAKER));
		var before=evaluate(c,scenario(Kind.ACTUAL,Map.of("first",boosted,"second",unboosted),actions,stock)); runnable(before);
		var renamedActions=Map.of(0,action("second",Attack.VOIDWAKER),1,action("first",Attack.VOIDWAKER));
		var renamed=evaluate(c,scenario(Kind.OWNED,Map.of("first",unboosted,"second",boosted),renamedActions,stock)); runnable(renamed);
		assertEquals(0,bridge.compare(before,renamed).expectedDamage,0);
		var swapped=evaluate(c,scenario(Kind.OWNED,before.scenario.loadouts,renamedActions,stock)); runnable(swapped);
		assertThrows(IllegalArgumentException.class,()->bridge.compare(before,swapped));
	}
	@Test public void quiverSelectionCannotOverrideCompatibleWornBoltsOrArrows()
	{
		for(int[] ids:new int[][]{{26374,21944,21946},{20997,21326,11212}})
		{
			var loadout=loadout("ranged",slots(ids[0],28955,ids[1]),ids[2]);
			var stock=stock(List.of(loadout),Map.of(ids[1],1,ids[2],1),0,10);
			var result=evaluate(conditions(1,List.of(attack(0))),scenario(Kind.ACTUAL,Map.of("r",loadout),Map.of(0,action("r",Attack.ORDINARY)),stock));
			assertNull(result.plan); assertTrue(result.resourceGaps.toString().contains("Compatible worn ammunition takes priority"));
		}
	}
	@Test public void quiverSelectionStillAllowsIncompatibleWornAmmoAndTheSameExactProjectile()
	{
		for(int worn:new int[]{21326,21946})
		{
			var loadout=loadout("spec",slots(26374,28955,worn),21946);
			Map<Integer,Integer> ammunition=new HashMap<>(); ammunition.put(worn,1); ammunition.put(21946,1);
			var stock=stock(List.of(loadout),ammunition,0,10);
			var result=evaluate(conditions(1,List.of(attack(0))),scenario(Kind.ACTUAL,Map.of("s",loadout),Map.of(0,action("s",Attack.ZARYTE_CROSSBOW)),stock));
			runnable(result); assertEquals(21946,result.packages.get("s").ammo);
			assertEquals(Map.of(21946,1),result.plan.spentAmmo); assertNotNull(result.plan.expectedDamage);
			assertEquals(Integer.valueOf(worn),result.scenario.loadouts.get("s").state.value().slots.get("ammo"));
		}
	}
	@Test public void resourceEventsRemainExplicitAndOrderedAndDoNotInferDeathChargeFromGear()
	{
		var l=loadout("spec",slots(27690,0,0),0); var stock=stock(List.of(l),Map.of(),0,10);
		var steps=List.of(Step.cast(0,f(true)),attack(0),Step.death(4,f("synthetic-death-1"),f(true)),Step.death(5,f("synthetic-death-2"),f(true)),attack(5),attack(10));
		var base=conditions(11,steps); var c=new Conditions(base.scope,base.endTick,f(700),base.firstRestoreTick,base.order,base.start,base.hardKandarin,steps);
		var actions=Map.of(1,action("s",Attack.VOIDWAKER),4,action("s",Attack.VOIDWAKER),5,action("s",Attack.VOIDWAKER));
		var e=evaluate(c,scenario(Kind.ACTUAL,Map.of("s",l),actions,stock)); runnable(e);
		assertEquals(2,e.plan.resources.deathChargeProcs); assertEquals(1000,e.plan.resources.spent); assertEquals(0,e.plan.resources.finalEnergy);
		assertEquals(2,e.plan.resources.voidwakerSpecs); assertEquals(69,e.plan.expectedDamage,0);
		assertEquals(Status.INSUFFICIENT_ENERGY,e.plan.resources.entries.get(e.plan.resources.entries.size()-1).status);
	}
	@Test public void evidenceAndInputsAreImmutableAndUnknownFactsCannotBeReadAsDefaults()
	{
		Map<Integer,Integer> map=new HashMap<>(); map.put(27690,1);
		var pool=new Stock(Availability.CARRIED,f(map),f(Map.of()),f(0),f(10),f(Map.of()),f(false),Set.of());
		map.clear(); assertEquals(Integer.valueOf(1),pool.equipment.value().get(27690));
		assertThrows(UnsupportedOperationException.class,()->pool.equipment.value().clear()); assertThrows(IllegalStateException.class,()->unknown("HP").value());
		var l=loadout("spec",slots(27690,0,0),0); var c=conditions(1,List.of(attack(0)));
		var e=evaluate(c,scenario(Kind.ACTUAL,Map.of("s",l),Map.of(0,action("s",Attack.VOIDWAKER)),stock(List.of(l),Map.of(),0,10)));
		assertThrows(UnsupportedOperationException.class,()->e.packages.clear()); assertThrows(UnsupportedOperationException.class,()->e.versions.clear());
		assertThrows(UnsupportedOperationException.class,()->e.plan.hits.clear()); assertThrows(UnsupportedOperationException.class,()->e.scenario.actions.clear());
	}
	@Test public void completeVoidSetIsRecomputedAndAPartialSetDoesNotReceiveItsMultiplier()
	{
		var fullSlots=slots(26374,28955,21946); fullSlots.putAll(Map.of("head",11664,"body",8839,"legs",8840,"hands",8842));
		var partialSlots=new HashMap<>(fullSlots); partialSlots.put("hands",0);
		var full=loadout("ranged",fullSlots,0); var partial=loadout("ranged",partialSlots,0);
		var stock=stock(List.of(full,partial),Map.of(21946,2),0,10); var c=conditions(1,List.of(attack(0))); var actions=Map.of(0,action("r",Attack.ORDINARY));
		var a=evaluate(c,scenario(Kind.ACTUAL,Map.of("r",full),actions,stock)); var b=evaluate(c,scenario(Kind.OWNED,Map.of("r",partial),actions,stock)); runnable(a); runnable(b);
		// No offence on the four Void pieces. The full set alone floors 142*1.1 and 145*1.1.
		assertEquals(31512,a.launches.get(0).chargedMetrics.attackRoll); assertEquals(28684,b.launches.get(0).chargedMetrics.attackRoll);
		assertEquals(47,a.launches.get(0).chargedMetrics.maxHit); assertEquals(43,b.launches.get(0).chargedMetrics.maxHit);
		assertTrue(bridge.compare(a,b).expectedDamage<0);
	}
	@Test public void hpDiaryStockAndRecoveryGapsAreScopedToTheirDependentMetrics()
	{
		var diamond=loadout("spec",slots(26374,28955,21946),0); var ruby=loadout("spec",slots(26374,28955,21944),0);
		var pool=stock(List.of(diamond,ruby),Map.of(21944,1,21946,1),0,10);
		Step step=Step.attack(0,f(new Context("NEX","SMOKE",Window.BASE)),f(true),f(500),unknown("player HP"),f(200),unknown("ruby eligibility"));
		var c=conditions(1,List.of(step)); var actions=Map.of(0,action("s",Attack.ZARYTE_CROSSBOW));
		var d=evaluate(c,scenario(Kind.ACTUAL,Map.of("s",diamond),actions,pool)); runnable(d); assertNotNull(d.plan.expectedDamage);
		var r=evaluate(c,scenario(Kind.OWNED,Map.of("s",ruby),actions,pool)); runnable(r); assertNull(r.plan.expectedDamage); assertTrue(r.plan.gaps.toString().contains("player HP"));
		var noDiary=new Conditions(c.scope,c.endTick,c.initialEnergy,c.firstRestoreTick,c.order,c.start,unknown("Kandarin diary"),c.steps);
		var diary=evaluate(noDiary,scenario(Kind.ACTUAL,Map.of("s",diamond),actions,pool)); runnable(diary); assertNull(diary.plan.expectedDamage);
		assertTrue(diary.plan.gaps.toString().contains("Kandarin diary"));
		var unread=new Stock(pool.availability,pool.equipment,unknown("exact ammo"),pool.splinters,pool.inventorySpace,pool.unlocks,pool.bankPacking,Set.of());
		var stockGap=evaluate(c,scenario(Kind.ACTUAL,Map.of("s",diamond),actions,unread)); assertNull(stockGap.plan); assertNotNull(stockGap.launches.get(0).chargedDamage);
		var noRecovery=new Loadout(diamond.role,diamond.state,diamond.projectile,unknown("active recovery"));
		var recovery=evaluate(c,scenario(Kind.ACTUAL,Map.of("s",noRecovery),actions,pool)); assertNull(recovery.plan); assertTrue(recovery.resourceGaps.toString().contains("active recovery"));
	}
	@Test public void unknownInitialSpellRestoreChargeAndQuiverActivationDoNotBecomeDefaults()
	{
		var l=loadout("spec",slots(26374,28949,20235),21944); var stock=stock(List.of(l),Map.of(21944,1),1,10); var base=conditions(1,List.of(attack(0)));
		var s=scenario(Kind.ACTUAL,Map.of("s",l),Map.of(0,action("s",Attack.ZARYTE_CROSSBOW)),stock);
		var c=new Conditions(base.scope,base.endTick,base.initialEnergy,base.firstRestoreTick,base.order,unknown("initial spell and attack state"),base.hardKandarin,base.steps);
		assertTrue(evaluate(c,s).resourceGaps.toString().contains("initial spell")); assertNull(evaluate(c,s).plan);
		c=new Conditions(base.scope,base.endTick,base.initialEnergy,unknown("restore phase"),base.order,base.start,base.hardKandarin,base.steps);
		assertNull(evaluate(c,s).plan); assertTrue(evaluate(c,s).resourceGaps.toString().contains("restore phase"));
		var unread=new Stock(stock.availability,stock.equipment,stock.ammunition,unknown("splinter stock"),stock.inventorySpace,stock.unlocks,stock.bankPacking,Set.of());
		var e=evaluate(base,scenario(Kind.ACTUAL,s.loadouts,s.actions,unread)); assertNull(e.plan); assertNotNull(e.launches.get(0).chargedDamage);
		var noQuiver=loadout("spec",slots(26374,22109,20235),21944);
		e=evaluate(base,scenario(Kind.OWNED,Map.of("s",noQuiver),s.actions,stock)); assertNull(e.plan); assertTrue(e.resourceGaps.toString().contains("exact quiver"));
	}
	@Test public void idleWindowsAndIneligibleDeathsRetainResourceEventToStepIdentity()
	{
		var l=loadout("spec",slots(27690,0,0),0); var stock=stock(List.of(l),Map.of(),0,10);
		var c=conditions(5,List.of(attack(0),Step.death(1,unknown("inapplicable death key"),f(false)),attack(4)));
		var e=evaluate(c,scenario(Kind.ACTUAL,Map.of("s",l),Map.of(2,action("s",Attack.VOIDWAKER)),stock)); runnable(e);
		assertEquals(Map.of(0,2),e.eventSteps); assertEquals(1,e.plan.hits.size()); assertEquals(0,e.plan.hits.get(0).eventIndex);
		assertEquals(2,e.eventSteps.get(e.plan.hits.get(0).eventIndex).intValue()); assertEquals(0,e.plan.resources.deathChargeProcs);
	}
	@Test public void rangedOnlyMp2WindowsRejectMeleeAndUnsupportedAlternativeDoesNotAlterThePair()
	{
		var l=loadout("spec",slots(27690,0,0),0); var stock=stock(List.of(l),Map.of(),0,10);
		var step=Step.attack(0,f(new Context("NEX","SHADOW",Window.EMBRACE_DARKNESS)),f(true),f(500),f(99),f(200),f(true));
		var e=evaluate(conditions(1,List.of(step)),scenario(Kind.ACTUAL,Map.of("s",l),Map.of(0,action("s",Attack.VOIDWAKER)),stock));
		assertNull(e.plan); assertTrue(e.resourceGaps.toString().contains("excludes melee")); assertNull(e.launches.get(0).chargedDamage);
	}
	@Test public void unusedUnknownAmmoAndSplintersDoNotBlockMeleeOrBecomeKnownEmptyStock()
	{
		var l=loadout("spec",slots(27690,0,0),0); var stock=stock(List.of(l),Map.of(),0,10);
		var unknownPools=new Stock(stock.availability,stock.equipment,unknown("unrelated ammo pool"),unknown("unused splinter pool"),stock.inventorySpace,stock.unlocks,stock.bankPacking,Set.of());
		var c=conditions(1,List.of(attack(0))); var actions=Map.of(0,action("s",Attack.VOIDWAKER));
		var e=evaluate(c,scenario(Kind.ACTUAL,Map.of("s",l),actions,unknownPools)); runnable(e);
		assertEquals(34.5,e.plan.expectedDamage,0); assertNull(e.remainingSplinters);
		assertFalse(e.scenario.stock.ammunition.known()); assertTrue(e.plan.spentAmmo.isEmpty()); assertEquals(0,e.plan.spentSplinters);
		var known=new Stock(stock.availability,stock.equipment,stock.ammunition,f(20),stock.inventorySpace,stock.unlocks,stock.bankPacking,Set.of());
		e=evaluate(c,scenario(Kind.ACTUAL,Map.of("s",l),actions,known)); runnable(e); assertEquals(Integer.valueOf(20),e.remainingSplinters);
	}
}
