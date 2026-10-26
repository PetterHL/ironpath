package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.*;
import com.ironpath.dev.PanelPreview;
import com.ironpath.nex.*;
import com.ironpath.preferences.*;
import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.AsyncBufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class NexSpritesTest
{
	@Test public void pinnedGenuineSpriteBytesMatchTheirProvenance() throws Exception
	{
		try(var reader=new java.io.InputStreamReader(getClass().getResourceAsStream("/com/ironpath/sprites/manifest.json"),java.nio.charset.StandardCharsets.UTF_8))
		{
			var manifest=new com.google.gson.JsonParser().parse(reader).getAsJsonObject();
			for(var entry:manifest.getAsJsonArray("assets"))
			{
				var asset=entry.getAsJsonObject(); int id=asset.get("id").getAsInt();
				try(var stream=getClass().getResourceAsStream("/com/ironpath/sprites/"+id+".png"))
				{
					StringBuilder hash=new StringBuilder();
					for(byte value:MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())) { hash.append(String.format(Locale.ROOT,"%02x",value&255)); }
					assertEquals(asset.get("sha256").getAsString(),hash.toString());
				}
				BufferedImage sprite=OfflineSprites.read(id);
				assertEquals(asset.get("width").getAsInt(),sprite.getWidth()); assertEquals(asset.get("height").getAsInt(),sprite.getHeight());
				assertTrue("Genuine icon contains visible pixels",visiblePixels(sprite)>0);
			}
		}
	}
	@Test public void productionItemManagerPathPaintsTheGenuineSpriteAndRejectsStaleImageChanges() throws Exception
	{
		ItemManager items=mock(ItemManager.class); ClientThread clientThread=mock(ClientThread.class);
		doAnswer(call->{ ((Runnable)call.getArgument(0)).run(); return null; }).when(clientThread).invokeLater(any(Runnable.class));
		var delayed=new AsyncBufferedImage(clientThread,36,32,BufferedImage.TYPE_INT_ARGB);
		when(items.getImage(anyInt())).thenAnswer(call->new AsyncBufferedImage(clientThread,36,32,BufferedImage.TYPE_INT_ARGB));
		when(items.getImage(6570)).thenReturn(delayed);
		var catalog=KnowledgeCatalog.load(new Gson());
		var sample=new NexSummary.Sample(5,"SHADOW","FEAR_THE_SHADOW","NEX@test","melee",9471,
			NexOwnedFixtures.state(NexOwnedFixtures.melee(),Map.of()),new NexTargetState(NexTargetState.Form.NEX,1,30));
		var signals=List.of(
			new NexActions.Signal(NexActions.Kind.ANIMATION,NexActions.Source.LOCAL_ANIMATION,5,150,1,9471,0,false,sample),
			new NexActions.Signal(NexActions.Kind.SETTLED,NexActions.Source.GAME_TICK,5,150,2,9471,0,false,sample));
		var summary=new NexSummary(1,0,100,100,100,0,com.ironpath.encounter.EncounterSummary.Outcome.COMPLETE,"synthetic",NexMethod.ID,NexMethod.ID,
			List.of(sample),Map.of(),Set.of(),null,List.of(),NexContext.UNKNOWN,null,NexObservations.EMPTY,new NexActions(signals,false));
		var account=NexOwnedFixtures.account(null);
		var result=new NexOwnedEvaluator(catalog).evaluate(summary,account,Preferences.EMPTY);
		var session=new NexSession(); session.select("test",301); session.add(summary);
		AtomicReference<JLabel> selected=new AtomicReference<>(); AtomicReference<NexPanel> rendered=new AtomicReference<>();
		AtomicReference<NexPanel.Actions> currentActions=new AtomicReference<>();
		SwingUtilities.invokeAndWait(()->{
			NexPanel.Actions actions=new NexPanel.Actions() {
				public boolean current() { return true; } public boolean canEdit() { return false; }
				public void method(NexMethod m) {} public void confirm(String k,Boolean v) {} public void reset() {} public void enable(boolean e) {}
			};
			currentActions.set(actions);
			var panel=new NexPanel(items); rendered.set(panel); panel.render("Observed",account,Preferences.EMPTY,catalog,session.stats(),true,actions,"",result);
			for(int i=0;i<result.advice.rows.size();i++)
			{ if(NexPresentation.changedItems(result.advice.rows.get(i).pair).equals(Map.of("cape",6570))) { PanelPreview.combo(panel,"nex-fang-alternative").setSelectedIndex(i); break; } }
			var label=(JLabel)PanelPreview.children(panel).stream().filter(c->"nex-item-sprite-6570".equals(c.getName())).findFirst().orElseThrow();
			selected.set(label); assertSame(delayed,((ImageIcon)label.getIcon()).getImage()); verify(items,atLeastOnce()).getImage(6570);
		});
		var graphics=delayed.createGraphics(); try { graphics.drawImage(OfflineSprites.read(6570),0,0,null); } finally { graphics.dispose(); }
		delayed.loaded();
		var late=new AsyncBufferedImage(clientThread,36,32,BufferedImage.TYPE_INT_ARGB);
		AtomicReference<JLabel> stale=new AtomicReference<>();
		SwingUtilities.invokeAndWait(()->{
			JLabel label=selected.get(); BufferedImage painted=new BufferedImage(40,36,BufferedImage.TYPE_INT_ARGB);
			var g=painted.createGraphics(); try { label.setSize(40,36); label.paint(g); } finally { g.dispose(); }
			assertTrue("Production getImage/addTo paints recognizable nonempty art",visiblePixels(painted)>0);
			var panel=rendered.get(); when(items.getImage(6570)).thenReturn(late);
			panel.render("Account refreshed",account,Preferences.EMPTY,catalog,session.stats(),true,currentActions.get(),"",result);
			JLabel pending=(JLabel)PanelPreview.children(panel).stream().filter(c->"nex-item-sprite-6570".equals(c.getName())).findFirst().orElseThrow(); stale.set(pending);
			Icon old=pending.getIcon();
			panel.render("Identity changed",null,Preferences.EMPTY,catalog,session.stats(),true,null,"");
			pending.setIcon(new ImageIcon(OfflineSprites.read(20997)));
			assertSame("Old view rejects a stale setIcon action",old,pending.getIcon());
			assertFalse("Cleared account has no item images",PanelPreview.children(panel).stream().anyMatch(c->c instanceof JLabel && ((JLabel)c).getIcon()!=null));
		});
		// Complete a genuinely pending production addTo callback after identity clearing.
		var lateGraphics=late.createGraphics(); try { lateGraphics.drawImage(OfflineSprites.read(6570),0,0,null); } finally { lateGraphics.dispose(); }
		late.loaded(); SwingUtilities.invokeAndWait(()->assertFalse(PanelPreview.children(rendered.get()).contains(stale.get())));
	}
	private static long visiblePixels(BufferedImage image)
	{
		long pixels=0;
		for(int y=0;y<image.getHeight();y++) { for(int x=0;x<image.getWidth();x++) { if((image.getRGB(x,y)>>>24)!=0) { pixels++; } } }
		return pixels;
	}
}
