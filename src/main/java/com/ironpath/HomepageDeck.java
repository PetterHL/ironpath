package com.ironpath;

import java.awt.*;
import javax.swing.*;

/** Complete-card budget over two measured viewports. Reflow changes count, never domain order. */
final class HomepageDeck extends BrowseGrid
{
	private int budget()
	{
		JViewport viewport=(JViewport)SwingUtilities.getAncestorOfClass(JViewport.class,this);
		int height=viewport==null || viewport.getExtentSize().height<=0 ? 600 : viewport.getExtentSize().height;
		int header=0;
		if (getParent()!=null)
		{ for (Component component : getParent().getComponents()) { if (component==this) { break; } header+=component.getPreferredSize().height; } }
		return Math.max(140,height*2-header-30);
	}
	@Override public Dimension preferredLayoutSize(Container parent)
	{
		int width=getWidth()>0 ? getWidth() : 213, cols=columns(width), cell=(width-(cols-1)*6)/cols, used=0, count=0;
		for (int row=0;row<getComponentCount();row+=cols)
		{
			int height=0;
			for (int i=row;i<Math.min(row+cols,getComponentCount());i++)
			{ Component card=getComponent(i); card.setSize(cell,Short.MAX_VALUE); height=Math.max(height,card.getPreferredSize().height); }
			if (count>0 && used+height>budget()) { break; }
			used+=height+6; count=Math.min(row+cols,getComponentCount());
		}
		for (int i=0;i<getComponentCount();i++) { getComponent(i).setVisible(i<count); }
		return new Dimension(width,Math.max(0,used-6));
	}
	@Override public void layoutContainer(Container parent)
	{
		preferredLayoutSize(parent); int y=0, cols=columns(getWidth()), cell=(getWidth()-(cols-1)*6)/cols;
		for (int row=0;row<getComponentCount() && getComponent(row).isVisible();row+=cols)
		{
			int height=0;
			for (int i=row;i<Math.min(row+cols,getComponentCount());i++) { height=Math.max(height,getComponent(i).getPreferredSize().height); }
			for (int i=row;i<Math.min(row+cols,getComponentCount());i++) { getComponent(i).setBounds((i-row)*(cell+6),y,cell,height); }
			y+=height+6;
		}
	}
}
