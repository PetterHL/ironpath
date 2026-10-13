package com.ironpath;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/** Reflows from actual content width/font metrics; never reduces the client font. */
final class BrowseGrid extends JPanel implements LayoutManager
{
	private static final int GAP=6, MINIMUM_CELL=100;
	BrowseGrid() { setLayout(this); setOpaque(false); setAlignmentX(LEFT_ALIGNMENT); setMaximumSize(new Dimension(213,Integer.MAX_VALUE)); }
	int columns(int width)
	{
		int cell=(width-GAP)/2;
		if (cell < MINIMUM_CELL) { return 1; }
		for (Component tile : getComponents())
		{
			for (JTextArea text : labels((Container)tile))
			{
				FontMetrics metrics=text.getFontMetrics(text.getFont()); int lines=1, used=0;
				for (String word : text.getText().split("\\s+"))
				{
					int size=metrics.stringWidth(word);
					if (size > cell-12) { return 1; }
					if (used > 0 && used+metrics.stringWidth(" ")+size > cell-12) { lines++; used=0; }
					used += (used == 0 ? 0 : metrics.stringWidth(" "))+size;
				}
				if (lines > 3) { return 1; }
			}
		}
		return 2;
	}
	private static List<JTextArea> labels(Container parent)
	{
		List<JTextArea> result=new ArrayList<>();
		for (Component child : parent.getComponents())
		{ if (child instanceof JTextArea) { result.add((JTextArea)child); } else if (child instanceof Container) { result.addAll(labels((Container)child)); } }
		return result;
	}
	private int width() { return getWidth() > 0 ? getWidth() : getParent() != null && getParent().getWidth() > 0 ? getParent().getWidth()-12 : 213; }
	@Override public Dimension preferredLayoutSize(Container parent)
	{
		int width=width(), cols=columns(width), cell=(width-(cols-1)*GAP)/cols, height=0;
		for (int row=0; row<getComponentCount(); row+=cols)
		{
			int rowHeight=0;
			for (int col=row; col<Math.min(row+cols,getComponentCount()); col++)
			{
				Component tile=getComponent(col); tile.setSize(cell,Short.MAX_VALUE);
				rowHeight=Math.max(rowHeight,tile.getPreferredSize().height);
			}
			height += rowHeight+GAP;
		}
		return new Dimension(width,Math.max(0,height-GAP));
	}
	@Override public void layoutContainer(Container parent)
	{
		int width=width(), cols=columns(width), cell=(width-(cols-1)*GAP)/cols, y=0;
		for (int row=0; row<getComponentCount(); row+=cols)
		{
			int height=0;
			for (int i=row; i<Math.min(row+cols,getComponentCount()); i++) { getComponent(i).setSize(cell,Short.MAX_VALUE); height=Math.max(height,getComponent(i).getPreferredSize().height); }
			for (int i=row; i<Math.min(row+cols,getComponentCount()); i++) { getComponent(i).setBounds((i-row)*(cell+GAP),y,cell,height); }
			y += height+GAP;
		}
	}
	@Override public Dimension minimumLayoutSize(Container parent) { return new Dimension(0,0); }
	@Override public void addLayoutComponent(String name, Component component) {}
	@Override public void removeLayoutComponent(Component component) {}
}
