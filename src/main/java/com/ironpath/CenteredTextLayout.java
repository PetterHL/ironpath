package com.ironpath;

import java.awt.*;

/** A full-width row containing a centred, naturally wrapped text group. */
final class CenteredTextLayout implements LayoutManager
{
	@Override public Dimension preferredLayoutSize(Container parent)
	{
		Dimension text=parent.getComponent(0).getPreferredSize();
		return new Dimension(parent.getWidth()>0?parent.getWidth():text.width,text.height);
	}
	@Override public void layoutContainer(Container parent)
	{
		Component text=parent.getComponent(0); Dimension size=text.getPreferredSize();
		int width=Math.min(parent.getWidth(),size.width);
		text.setBounds((parent.getWidth()-width)/2,0,width,parent.getHeight());
	}
	@Override public Dimension minimumLayoutSize(Container parent) { return new Dimension(0,0); }
	@Override public void addLayoutComponent(String name,Component child) { }
	@Override public void removeLayoutComponent(Component child) { }
}
