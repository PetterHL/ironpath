package com.ironpath;

import com.ironpath.encounter.EncounterSummary;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.events.ServerNpcLoot;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** Mocked public API ordering, not evidence from actual client kills. */
public class RecentBossObserverTest
{
	Client client; Player player; NPC boss; WorldView view; RecentBossObserver observer;
	AtomicInteger tick=new AtomicInteger(10);
	@Before public void setup()
	{
		client=mock(Client.class); player=mock(Player.class); boss=mock(NPC.class); view=mock(WorldView.class);
		when(client.isClientThread()).thenReturn(true);
		when(client.getTickCount()).thenAnswer(i->tick.get()); when(client.getLocalPlayer()).thenReturn(player);
		when(client.getRealSkillLevel(any(Skill.class))).thenReturn(90); when(client.getBoostedSkillLevel(any(Skill.class))).thenReturn(103);
		when(client.getVarpValue(net.runelite.api.gameval.VarPlayerID.SA_ENERGY)).thenReturn(1000);
		when(client.getVarpValue(net.runelite.api.gameval.VarPlayerID.COM_MODE)).thenReturn(1);
		when(boss.getId()).thenReturn(2205); when(boss.getIndex()).thenReturn(4); when(boss.getWorldView()).thenReturn(view);
		when(view.getId()).thenReturn(-1); when(player.getWorldView()).thenReturn(view); when(player.getInteracting()).thenReturn(boss);
		IndexedObjectSet players=mock(IndexedObjectSet.class); when(view.players()).thenReturn(players);
		when(players.stream()).thenAnswer(i->java.util.stream.Stream.of(player));
		ItemContainer worn=mock(ItemContainer.class); when(client.getItemContainer(InventoryID.WORN)).thenReturn(worn);
		when(worn.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx())).thenReturn(new Item(25867,1));
		observer=new RecentBossObserver(client); observer.enable(true,7);
	}
	void damage()
	{
		Hitsplat hit=mock(Hitsplat.class); when(hit.isMine()).thenReturn(true); when(hit.getAmount()).thenReturn(20);
		HitsplatApplied event=new HitsplatApplied(); event.setActor(boss); event.setHitsplat(hit); observer.hit(event);
	}
	void loot()
	{ NPCComposition composition=mock(NPCComposition.class); when(composition.getId()).thenReturn(2205); observer.loot(new ServerNpcLoot(composition,List.of())); }
	@Test public void optInAndObservedSpawnRequiredThenDeathAndLootProduceCompleteSummary()
	{
		observer.tick(); damage(); loot(); assertFalse(observer.capture.active());
		observer.spawn(boss); observer.tick(); tick.set(11); damage(); for(int i=11;i<20;i++) { tick.set(i); observer.tick(); } tick.set(20); observer.death(boss); observer.despawn(boss);
		tick.set(21); loot(); tick.set(25); observer.tick();
		EncounterSummary s=observer.capture.recent().get(0); assertEquals(EncounterSummary.Outcome.COMPLETE,s.outcome);
		assertEquals(Integer.valueOf(25867),s.segments.get(0).state.slots.get("weapon"));
		assertEquals(Integer.valueOf(103),s.segments.get(0).state.visibleLevels.get("RANGED"));
		loot(); assertEquals(1,observer.capture.recent().size());
	}
	@Test public void wrongActorAndForeignLootCannotConfirmCompletion()
	{
		observer.spawn(boss); observer.tick(); tick.set(11); damage(); observer.death(mock(NPC.class));
		tick.set(20); observer.death(boss);
		NPCComposition other=mock(NPCComposition.class); when(other.getId()).thenReturn(2215);
		observer.loot(new ServerNpcLoot(other,List.of())); tick.set(25); observer.tick();
		assertEquals(EncounterSummary.Outcome.CENSORED,observer.capture.recent().get(0).outcome);
	}
	@Test public void transformIndexReuseDeathAndLoadingInvalidateWithoutFabricatedCompletion()
	{
		observer.spawn(boss); observer.tick(); when(boss.getId()).thenReturn(999); observer.changed(boss);
		assertEquals(EncounterSummary.Outcome.EXCLUDED,observer.capture.recent().get(0).outcome);
		when(boss.getId()).thenReturn(2205); observer.spawn(boss); observer.tick();
		NPC replacement=mock(NPC.class); when(replacement.getId()).thenReturn(2205); when(replacement.getIndex()).thenReturn(4); when(replacement.getWorldView()).thenReturn(view);
		observer.spawn(replacement); assertEquals(EncounterSummary.Outcome.CENSORED,observer.capture.recent().get(1).outcome);
		when(player.getInteracting()).thenReturn(replacement); observer.tick(); observer.death(player);
		assertEquals(EncounterSummary.Outcome.FAILED,observer.capture.recent().get(2).outcome);
		observer.reset(8); loot(); tick.set(30); observer.tick(); assertTrue(observer.capture.recent().isEmpty());
	}
	@Test public void midFightEnableAlreadyDamagedAndOtherPlayerEvidenceRemainScoped()
	{
		when(boss.getHealthScale()).thenReturn(30); when(boss.getHealthRatio()).thenReturn(10);
		observer.spawn(boss); observer.tick(); tick.set(11); damage(); tick.set(20); observer.death(boss); loot(); tick.set(25); observer.tick();
		assertEquals(EncounterSummary.Outcome.CENSORED,observer.capture.recent().get(0).outcome);
		observer.enable(false,8); observer.spawn(boss); observer.tick(); assertFalse(observer.capture.active());
	}
}
