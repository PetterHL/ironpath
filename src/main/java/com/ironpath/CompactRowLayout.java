package com.ironpath;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.BoxLayout;

/** Measure wrapping text at its actual width before BoxLayout caches the enclosing row height. */
final class CompactRowLayout extends BorderLayout
{
	CompactRowLayout() { super(4,0); }
	@Override public Dimension preferredLayoutSize(Container parent)
	{
		Component center=getLayoutComponent(CENTER), west=getLayoutComponent(WEST), east=getLayoutComponent(EAST);
		if (!(center instanceof Container)) { return super.preferredLayoutSize(parent); }
		Insets insets=parent.getInsets();
		int width=parent.getWidth() > 0 ? parent.getWidth() : 213;
		int sideWidth=(west == null ? 0 : west.getPreferredSize().width+getHgap())
			+ (east == null ? 0 : east.getPreferredSize().width+getHgap());
		Container body=(Container)center;
		body.setSize(Math.max(1,width-insets.left-insets.right-sideWidth),Short.MAX_VALUE);
		if (body.getLayout() instanceof BoxLayout) { ((BoxLayout)body.getLayout()).invalidateLayout(body); }
		int height=body.getPreferredSize().height;
		if (west != null) { height=Math.max(height,west.getPreferredSize().height); }
		if (east != null) { height=Math.max(height,east.getPreferredSize().height); }
		return new Dimension(width,height+insets.top+insets.bottom);
	}
}
