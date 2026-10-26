package com.ironpath;

import java.util.EnumSet;
import javax.swing.*;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.VarbitID;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Public arrival/reload events, including the outbound and hard-account barriers. */
public class NexArrivalTest
{
	private static void status(OfflinePluginReplay r,String expected) throws Exception
	{ SwingUtilities.invokeAndWait(()->assertEquals(expected,NexScenarioPanelTest.named(r.panel,"nex-tracking-status",JTextArea.class).getText())); }
	@Test public void portalAndArenaReloadShowReadyOnlyAfterRequiredEvidenceAndCanCaptureNextKill() throws Exception
	{
		try(var r=new OfflinePluginReplay(12))
		{
			status(r,"Waiting for Nex");
			r.loading(); r.edt(); status(r,"Loading encounter…");
			r.bossPresent=false; r.arrive(false,false); status(r,"Loading encounter…"); assertTrue(r.plugin.nexSession.suspended());
			r.bossPresent=true; r.tick(); r.settle(); status(r,"Waiting for Nex"); assertFalse(r.plugin.nexSession.suspended());
			r.fight(20,20,60,true,offset->{ if(offset==4) { try { r.edt(); status(r,"Tracking kill…"); } catch(Exception ex) { throw new RuntimeException(ex); } } }); r.settle();
			status(r,"Waiting for Nex");
			r.barrier(1); r.loading(); r.arrive(false); status(r,"Open bank to verify this account");
			assertTrue(r.plugin.nexSession.suspended()); assertFalse(r.plugin.nex.capture.active());
			r.arrive(true); status(r,"Go to Nex to track kills");
			r.loading(); r.arrive(false,false); status(r,"Loading encounter…");
			r.bossPresent=true; r.tick(); r.settle(); status(r,"Waiting for Nex"); assertFalse(r.plugin.nexSession.suspended());
			r.fight(20,20,60,true); r.settle(); assertEquals(2,r.plugin.nexSession.stats().complete);
			SwingUtilities.invokeAndWait(()->com.ironpath.dev.PanelPreview.click(r.panel,"Turn off kill tracking")); r.settle(); status(r,"Kill tracking: Off");
		}
	}
	@Test public void unsupportedAccountWorldAndMissingIdentityHaveDistinctNormalStatuses() throws Exception
	{
		try(var r=new OfflinePluginReplay(12))
		{
			doReturn(0).when(r.client).getVarbitValue(VarbitID.IRONMAN); r.tick(); r.settle(); status(r,"Standard Ironman required");
			doReturn(1).when(r.client).getVarbitValue(VarbitID.IRONMAN); when(r.client.getWorldType()).thenReturn(EnumSet.of(WorldType.MEMBERS,WorldType.PVP));
			r.tick(); r.settle(); status(r,"Use a permanent members world");
			when(r.client.getWorldType()).thenReturn(EnumSet.of(WorldType.MEMBERS)); when(r.client.getAccountHash()).thenReturn(0L);
			r.tick(); r.settle(); status(r,"Waiting for account"); assertEquals(0,r.plugin.nexSession.stats().attempts);
		}
	}
	@Test public void settingsLoadingAndReadFailureStayDistinctFromAccountLoading() throws Exception
	{
		try(var r=new OfflinePluginReplay(12))
		{
			r.plugin.session.clear(); r.tick(); r.edt(); status(r,"Loading settings…");
			r.settle(); r.tick(); r.settle(); status(r,"Waiting for Nex");
			var session=r.plugin.session;
			assertTrue(session.loadPreferences(session.generation(),session.identity(),com.ironpath.preferences.Preferences.EMPTY,false));
			r.tick(); r.settle(); status(r,"Settings unavailable · editing disabled");
		}
	}
	@Test public void delayedPaintingAndDirtyAccountReadsCannotLeaveTheRecordingStatusBehind() throws Exception
	{
		for(int font:java.util.List.of(12,20))
		try(var r=new OfflinePluginReplay(font))
		{
			var blocked=new java.util.concurrent.CountDownLatch(1);
			var release=new java.util.concurrent.CountDownLatch(1);
			SwingUtilities.invokeLater(()->{
				blocked.countDown();
				try { if(!release.await(10,java.util.concurrent.TimeUnit.SECONDS)) { throw new AssertionError("Client replay did not release EDT"); } }
				catch(InterruptedException ex) { Thread.currentThread().interrupt(); throw new AssertionError(ex); }
			});
			try
			{
				assertTrue(blocked.await(10,java.util.concurrent.TimeUnit.SECONDS));
				r.barrier(3); r.overhead("Fill my soul with smoke!");
				for(int i=0;i<2;i++)
				{
					r.plugin.onItemContainerChanged(new ItemContainerChanged(net.runelite.api.gameval.InventoryID.WORN,r.client.getItemContainer(net.runelite.api.gameval.InventoryID.WORN)));
					r.tick();
				}
			}
			finally { release.countDown(); }
			r.edt(); status(r,"Tracking kill…");
			final JTextArea[] progress={null};
			SwingUtilities.invokeAndWait(()->{
				progress[0]=NexScenarioPanelTest.named(r.panel,"nex-tracking-progress",JTextArea.class);
				assertEquals("1.2 s · Smoke",progress[0].getText());
			});
			r.tick(); r.edt();
			SwingUtilities.invokeAndWait(()->{
				assertSame("A heartbeat must preserve the existing view",progress[0],NexScenarioPanelTest.named(r.panel,"nex-tracking-progress",JTextArea.class));
				assertEquals("1.8 s · Smoke",progress[0].getText());
			});
			r.overhead("Darken my shadow!"); r.tick(); r.edt();
			SwingUtilities.invokeAndWait(()->assertEquals("2.4 s · Shadow",progress[0].getText()));
			r.loading(); r.edt(); status(r,"Loading encounter…");
			SwingUtilities.invokeAndWait(()->assertFalse(com.ironpath.dev.PanelPreview.children(r.panel).stream().anyMatch(c->"nex-tracking-progress".equals(c.getName()))));
		}
	}
}
