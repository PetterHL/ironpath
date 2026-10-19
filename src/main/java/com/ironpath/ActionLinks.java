package com.ironpath;

import java.awt.*;
import javax.swing.JPanel;

/** Centres each row of full native action labels, wrapping at the sidebar width. */
final class ActionLinks extends JPanel implements LayoutManager
{
	private final int gap;
	ActionLinks() { this(4); }
	ActionLinks(int gap) { this.gap=gap; setOpaque(false); setLayout(this); setAlignmentX(LEFT_ALIGNMENT); }
	private Dimension arrange(boolean apply)
	{
		int width=getWidth()>0 ? getWidth() : 199, x=0, y=0, row=0;
		java.util.List<Component> pending=new java.util.ArrayList<>();
		for (Component action : getComponents())
		{
			Dimension size=action.getPreferredSize(); int w=Math.min(width,size.width);
			if (x>0 && x+w>width) { if(apply) { place(pending,width,x-gap,y); } pending.clear(); y+=row+4; x=0; row=0; }
			pending.add(action);
			x+=w+gap; row=Math.max(row,size.height);
		}
		if(apply) { place(pending,width,Math.max(0,x-gap),y); }
		return new Dimension(width,y+row);
	}
	private void place(java.util.List<Component> actions,int width,int used,int y)
	{
		int x=Math.max(0,(width-used)/2);
		for(Component action:actions) { Dimension size=action.getPreferredSize(); int w=Math.min(width,size.width); action.setBounds(x,y,w,size.height); x+=w+gap; }
	}
	@Override public Dimension preferredLayoutSize(Container parent) { return arrange(false); }
	@Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE,getPreferredSize().height); }
	@Override public void layoutContainer(Container parent) { arrange(true); }
	@Override public Dimension minimumLayoutSize(Container parent) { return new Dimension(0,0); }
	@Override public void addLayoutComponent(String name,Component component) {}
	@Override public void removeLayoutComponent(Component component) {}
}
