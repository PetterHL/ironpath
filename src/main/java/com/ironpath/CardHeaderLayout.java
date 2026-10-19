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
		int width=width(parent); Component title=parent.getComponent(1);
		int textWidth=Math.min(Math.max(1,width-46),title.getPreferredSize().width), x=Math.max(0,(width-46-textWidth)/2);
		parent.getComponent(0).setBounds(x,Math.max(0,(parent.getHeight()-40)/2),40,40);
		title.setBounds(x+46,0,textWidth,parent.getHeight());
	}
	@Override public Dimension minimumLayoutSize(Container parent) { return new Dimension(0,40); }
	@Override public void addLayoutComponent(String name,Component component) {}
	@Override public void removeLayoutComponent(Component component) {}
}
