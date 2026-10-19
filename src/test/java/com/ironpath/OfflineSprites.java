package com.ironpath;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.AsyncBufferedImage;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Genuine, pinned game-art fixtures. Only the ItemManager boundary is replaced;
 * production still uses getImage/addTo and its guarded JLabel. No network IO. */
final class OfflineSprites
{
	static ItemManager manager()
	{
		ItemManager items=mock(ItemManager.class);
		ClientThread callbacks=mock(ClientThread.class);
		doAnswer(call->{ ((Runnable)call.getArgument(0)).run(); return null; }).when(callbacks).invokeLater(any(Runnable.class));
		Map<Integer,AsyncBufferedImage> loaded=new HashMap<>();
		when(items.getImage(anyInt())).thenAnswer(call->loaded.computeIfAbsent(call.getArgument(0),id->{
			BufferedImage sprite=read(id);
			AsyncBufferedImage image=new AsyncBufferedImage(callbacks,sprite.getWidth(),sprite.getHeight(),BufferedImage.TYPE_INT_ARGB);
			var graphics=image.createGraphics(); try { graphics.drawImage(sprite,0,0,null); } finally { graphics.dispose(); }
			image.loaded(); return image;
		}));
		return items;
	}
	static BufferedImage read(int id)
	{
		try(var stream=OfflineSprites.class.getResourceAsStream("/com/ironpath/sprites/"+id+".png"))
		{
			if(stream==null) { throw new IllegalStateException("Missing pinned preview sprite for item "+id+"; add its genuine PNG and provenance, never approximate it"); }
			BufferedImage image=ImageIO.read(stream);
			if(image==null) { throw new IllegalStateException("Invalid preview sprite "+id); }
			return image;
		}
		catch(java.io.IOException ex) { throw new IllegalStateException("Cannot read pinned preview sprite "+id,ex); }
	}
}
