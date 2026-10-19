package com.ironpath;

import java.awt.Dimension;
import javax.accessibility.*;
import javax.swing.*;

/** Independent native section; its header and body survive ordinary result refreshes. */
final class CollapsibleSection extends JPanel
{
	final JPanel body;
	final JButton header;
	private boolean expanded;
	private final String title;
	CollapsibleSection(JPanel parent,String title,String name,boolean expanded)
	{
		this.title=title; this.expanded=expanded;
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS)); setOpaque(false); setAlignmentX(LEFT_ALIGNMENT);
		body=PanelWidgets.column(parent); body.setName(name+"-body"); body.setVisible(expanded);
		header=new JButton() {
			@Override public AccessibleContext getAccessibleContext()
			{
				if(accessibleContext==null) { accessibleContext=new AccessibleJButton() {
					@Override public AccessibleStateSet getAccessibleStateSet()
					{ var states=super.getAccessibleStateSet(); states.add(CollapsibleSection.this.expanded?AccessibleState.EXPANDED:AccessibleState.COLLAPSED); return states; }
				}; } return accessibleContext;
			}
			@Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE,getPreferredSize().height); }
		};
		header.setName(name); header.setToolTipText(title); header.getAccessibleContext().setAccessibleName(title);
		header.setHorizontalAlignment(SwingConstants.LEFT); header.setAlignmentX(LEFT_ALIGNMENT);
		header.setMargin(new java.awt.Insets(5,5,5,5)); header.setBackground(net.runelite.client.ui.ColorScheme.MEDIUM_GRAY_COLOR);
		header.setForeground(net.runelite.client.ui.ColorScheme.TEXT_COLOR);
		header.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),"activate");
		header.getActionMap().put("activate",new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { header.doClick(); } });
		add(header); add(body); updateFont(parent.getFont());
	}
	boolean expanded() { return expanded; }
	@Override public Dimension getPreferredSize()
	{ ((BoxLayout)getLayout()).invalidateLayout(this); return super.getPreferredSize(); }
	@Override public Dimension getMaximumSize()
	{ return new Dimension(Integer.MAX_VALUE,getPreferredSize().height); }
	void expand(boolean value)
	{
		boolean old=expanded; expanded=value; body.setVisible(value); updateLabel();
		header.getAccessibleContext().firePropertyChange(AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
			old?AccessibleState.EXPANDED:AccessibleState.COLLAPSED,value?AccessibleState.EXPANDED:AccessibleState.COLLAPSED);
		revalidate(); repaint();
	}
	void updateFont(java.awt.Font font) { setFont(font); body.setFont(font); header.setFont(font); updateLabel(); }
	private void updateLabel()
	{
		String arrow=expanded?"▾":"▸";
		String escaped=title.replace("&","&amp;");
		header.setText("<html><table cellpadding='0' cellspacing='0'><tr><td width='14'>"+arrow+"</td><td width='175'>"+escaped+"</td></tr></table></html>");
		header.setSelected(expanded); header.getAccessibleContext().setAccessibleDescription(expanded?"Expanded":"Collapsed");
		header.setMinimumSize(new Dimension(0,Math.max(28,header.getPreferredSize().height)));
	}
}
