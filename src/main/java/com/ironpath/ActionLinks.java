package com.ironpath;

import java.awt.*;
import javax.swing.JPanel;

/** Wraps full action labels instead of letting a narrow GridLayout ellipsize them. */
final class ActionLinks extends JPanel implements LayoutManager
{
	private final boolean right;
	private final int gap;
	ActionLinks() { this(false,4); }
	ActionLinks(boolean right) { this(right,4); }
	ActionLinks(boolean right,int gap) { this.right=right; this.gap=gap; setOpaque(false); setLayout(this); setAlignmentX(LEFT_ALIGNMENT); }
	private Dimension arrange(boolean apply)
	{
		int width=getWidth()>0 ? getWidth() : 213, x=0, y=0, row=0;
		for (Component action : getComponents())
		{
			Dimension size=action.getPreferredSize(); int w=Math.min(width,size.width);
			if (x>0 && x+w>width) { y+=row+4; x=0; row=0; }
			if (apply) { action.setBounds(right && getComponentCount()==1?width-w:x,y,w,size.height); }
			x+=w+gap; row=Math.max(row,size.height);
		}
		return new Dimension(width,y+row);
	}
	@Override public Dimension preferredLayoutSize(Container parent) { return arrange(false); }
	@Override public void layoutContainer(Container parent) { arrange(true); }
	@Override public Dimension minimumLayoutSize(Container parent) { return new Dimension(0,0); }
	@Override public void addLayoutComponent(String name,Component component) {}
	@Override public void removeLayoutComponent(Component component) {}
}
