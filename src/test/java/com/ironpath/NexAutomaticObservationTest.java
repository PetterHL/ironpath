package com.ironpath;

import com.ironpath.nex.*;
import java.util.*;
import net.runelite.api.*;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Mocked public callbacks and mutable getters exercise binding/order, not actual server mappings. */
public class NexAutomaticObservationTest
{
	private NexObserverTest f;
	@Before public void setup() throws Exception { f=new NexObserverTest(); f.setup(); f.start(); }
	private void hud(int definition,int hp,int maximum)
	{
		Widget widget=mock(Widget.class); when(f.client.getWidget(InterfaceID.HpbarHud.HP_BAR_TEXT)).thenReturn(widget);
		when(f.client.getVarpValue(VarPlayerID.HPBAR_HUD_NPC)).thenReturn(definition);
		when(f.client.getServerVarpValue(VarPlayerID.HPBAR_HUD_NPC)).thenReturn(definition);
		when(f.client.getVarbitValue(VarbitID.HPBAR_HUD_HP)).thenReturn(hp); when(f.client.getServerVarbitValue(VarbitID.HPBAR_HUD_HP)).thenReturn(hp);
		when(f.client.getVarbitValue(VarbitID.HPBAR_HUD_BASEHP)).thenReturn(maximum); when(f.client.getServerVarbitValue(VarbitID.HPBAR_HUD_BASEHP)).thenReturn(maximum);
		f.observer.resourceEvent(-1,VarPlayerID.HPBAR_HUD_INFO,1);
		f.observer.hud();
	}
	private NexSummary finish() { f.observer.transition(); return f.session.recent().get(0); }
	@Test public void longHudTrafficAndDiagnosticOverflowCannotCensorTheMainCapture()
	{
		when(f.boss.getHealthRatio()).thenReturn(15); when(f.boss.getHealthScale()).thenReturn(30);
		for(int t=2;t<=1200;t++)
		{
			f.tick.set(t); f.observer.tick(); hud(NpcID.NEX,1700,3400);
			assertTrue("HUD-enabled capture must remain active at tick "+t,f.observer.capture.active());
		}
		var summary=finish();
		assertEquals("Only initial/form/player state belongs in the main buffer",1,summary.timeline.size());
		assertTrue(summary.signals.continuous); assertEquals(1201,summary.sampledTicks);
		assertTrue(summary.observations.truncated);
		assertTrue(summary.observations.health.stream().anyMatch(h->h.targetState.health.source==NexHealth.Source.BOSS_HUD));
		assertNotEquals("State buffer limit reached",summary.reason);
	}
	@Test public void hudExpiresOnTickTargetLossFormChangePredictionAndReload()
	{
		when(f.boss.getHealthRatio()).thenReturn(15); when(f.boss.getHealthScale()).thenReturn(30);
		f.tick.set(2); hud(NpcID.NEX,1700,3400);
		f.tick.set(3); f.observer.sample(-1);
		f.tick.set(4); hud(NpcID.NEX,1600,3400); when(f.player.getInteracting()).thenReturn(f.minion); f.observer.sample(-1);
		when(f.player.getInteracting()).thenReturn(f.boss); f.observer.sample(-1);
		f.tick.set(5); hud(NpcID.NEX,1500,3400); when(f.boss.getId()).thenReturn(NpcID.NEX_SOULSPLIT); f.observer.changed(f.boss); f.observer.sample(-1);
		f.tick.set(6); hud(NpcID.NEX_SOULSPLIT,1400,3400); when(f.client.getServerVarbitValue(VarbitID.HPBAR_HUD_HP)).thenReturn(1390); f.observer.sample(-1);
		var s=finish();
		var first=s.observations.health.stream().filter(x->x.tick==2).reduce((a,b)->b).orElseThrow().targetState.health;
		assertEquals(NexHealth.Source.BOSS_HUD,first.source); assertEquals(Integer.valueOf(1700),first.lower);
		assertEquals(NexHealth.Validity.AWAITING_CLIENT_VALIDATION,first.validity); assertNotNull(first.target);
		assertTrue(s.observations.health.stream().filter(x->x.tick==3).allMatch(x->x.targetState.health.source==NexHealth.Source.ACTOR_BAR));
		assertEquals(NexHealth.Source.ACTOR_BAR,s.observations.health.get(s.observations.health.size()-1).targetState.health.source);
		assertTrue(s.observations.health.stream().filter(x->x.target!=null && x.target.startsWith("UMBRA")).noneMatch(x->x.targetState.health.source==NexHealth.Source.BOSS_HUD));
		f.observer.clearActive(); assertEquals(Integer.valueOf(1700),first.lower);
	}
	@Test public void hudRejectsDuplicateDefinitionHiddenWidgetWrongNpcAndServerDisagreement()
	{
		f.tick.set(2); hud(NpcID.NEX_SHADOWMAGE,400,500);
		when(f.client.getVarbitValue(VarbitID.HPBAR_HUD_BOSS_DISABLED)).thenReturn(1); hud(NpcID.NEX,3000,3400);
		when(f.client.getVarbitValue(VarbitID.HPBAR_HUD_BOSS_DISABLED)).thenReturn(0);
		Widget hidden=mock(Widget.class); when(hidden.isHidden()).thenReturn(true); when(f.client.getWidget(InterfaceID.HpbarHud.HP_BAR_TEXT)).thenReturn(hidden); f.observer.hud();
		var duplicate=f.npc(NpcID.NEX_SHADOWMAGE,3); f.observer.spawn(duplicate); when(f.player.getInteracting()).thenReturn(f.minion);
		hud(NpcID.NEX_SHADOWMAGE,300,500);
		assertTrue(finish().observations.health.stream().noneMatch(s->s.targetState.health.source==NexHealth.Source.BOSS_HUD));
	}
	@Test public void launchPackageSurvivesDelayedMissAfterSwitchAndProjectileCallbacksDeduplicate()
	{
		var equipment=f.client.getItemContainer(InventoryID.WORN);
		when(equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx())).thenReturn(new Item(20997,1));
		f.tick.set(2); when(f.client.getGameCycle()).thenReturn(60); f.observer.sample(426);
		Projectile p=mock(Projectile.class); when(p.getSourceActor()).thenReturn(f.player); when(p.getTargetActor()).thenReturn(f.boss);
		when(p.getId()).thenReturn(1120); when(p.getStartCycle()).thenReturn(60); when(p.getEndCycle()).thenReturn(120);
		f.observer.projectile(p); f.observer.projectile(p);
		when(equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx())).thenReturn(new Item(26219,1)); f.observer.sample(-1);
		f.tick.set(4); when(f.client.getGameCycle()).thenReturn(120); f.hit(f.boss,true,false,HitsplatID.BLOCK_ME,0);
		var summary=finish(); assertEquals(1,summary.observations.flights.size()); assertEquals(1,summary.observations.impacts.size());
		var review=new NexObservationReview(summary); assertEquals(1,review.matches.get(0).candidates.size());
		assertEquals(Integer.valueOf(20997),review.matches.get(0).candidates.get(0).sample.state.slots.get("weapon"));
		assertEquals(0,review.matches.get(0).impact.amount);
	}
	@Test public void rerenderingOldHudValuesAfterInteractionBoundaryDoesNotMakeThemFresh()
	{
		f.tick.set(2); hud(NpcID.NEX,1700,3400);
		f.observer.interactingChanged(f.player); f.observer.hud(); f.observer.sample(-1);
		var summary=finish();
		assertNotEquals(NexHealth.Source.BOSS_HUD,summary.observations.health.get(summary.observations.health.size()-1).targetState.health.source);
		assertTrue(summary.observations.health.stream().anyMatch(s->s.targetState.health.source==NexHealth.Source.BOSS_HUD));
	}
	@Test public void energyCallbackBeforeActorUpdateAndDcEndDoNotBecomeSpecsOrProcs()
	{
		f.tick.set(2); when(f.client.getGameCycle()).thenReturn(60);
		when(f.client.getServerVarpValue(VarPlayerID.SA_ENERGY)).thenReturn(1000);
		f.observer.resourceEvent(-1,VarPlayerID.SA_ENERGY,500);
		f.observer.resourceEvent(VarbitID.ARCEUUS_DEATH_CHARGE_ACTIVE,-1,1);
		f.observer.resourceEvent(-1,VarPlayerID.SA_ENERGY,650);
		ActorSpotAnim effect=mock(ActorSpotAnim.class); when(effect.getId()).thenReturn(3289);
		IterableHashTable<ActorSpotAnim> table=mock(IterableHashTable.class); when(table.iterator()).thenAnswer(i->List.of(effect).iterator()); when(f.player.getSpotAnims()).thenReturn(table);
		f.observer.spot(f.player); f.observer.spot(f.player);
		var o=finish().observations;
		assertTrue(o.launches.isEmpty()); assertEquals(2,o.signals.stream().filter(s->s.kind==NexObservations.Kind.ENERGY).count());
		assertEquals(1,o.signals.stream().filter(s->s.kind==NexObservations.Kind.DC_END_EFFECT).count());
		var energy=o.signals.stream().filter(s->s.kind==NexObservations.Kind.ENERGY).findFirst().orElseThrow();
		assertEquals(500,energy.value); assertEquals(1000,energy.serverValue); assertEquals(60,energy.cycle);
	}
	@Test public void crossbowAnimationAndEnergyLossCannotDistinguishSpecialFromOrdinary()
	{
		var equipment=f.client.getItemContainer(InventoryID.WORN);
		when(equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx())).thenReturn(new Item(26374,1));
		f.observer.resourceEvent(-1,VarPlayerID.SA_ENERGY,250); f.observer.sample(9168);
		assertEquals(NexObservations.Attack.ORDINARY_OR_SPECIAL_CANDIDATE,finish().observations.launches.get(0).attack);
	}
}
