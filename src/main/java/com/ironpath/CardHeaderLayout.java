package com.ironpath;

import java.awt.*;

/** Only the name sits beside the fixed sprite well; reasons and metrics use the full body. */
final class CardHeaderLayout implements LayoutManager
{
	private int width(Container parent) { return parent.getWidth()>0 ? parent.getWidth() : 199; }
	@Override public Dimension preferredLayoutSize(Container parent)
	{
		int width=width(parent); Component title=parent.getComponent(1); title.setSize(Math.max(1,width-46),Short.MAX_VALUE);
		return new Dimension(width,Math.max(40,title.getPreferredSize().height));
	}
	@Override public void layoutContainer(Container parent)
	{
		int width=width(parent); parent.getComponent(0).setBounds(0,0,40,40);
		parent.getComponent(1).setBounds(46,0,Math.max(1,width-46),parent.getHeight());
	}
	@Override public Dimension minimumLayoutSize(Container parent) { return new Dimension(0,40); }
	@Override public void addLayoutComponent(String name,Component component) {}
	@Override public void removeLayoutComponent(Component component) {}
}
