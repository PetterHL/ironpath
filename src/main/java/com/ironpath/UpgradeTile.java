package com.ironpath;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import net.runelite.client.ui.ColorScheme;

/** One native button owns the complete card, its keyboard action and visible focus. */
final class UpgradeTile extends JPanel
{
	final JPanel body = new JPanel();
	final JButton open;
	UpgradeTile(String id, String title, java.awt.Font font, Runnable activate)
	{
		super(new BorderLayout());
		setName("upgrade-card-" + id); setFont(font); setAlignmentX(LEFT_ALIGNMENT);
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		body.setFont(font); body.setOpaque(false); body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		open = new JButton()
		{
			@Override public Dimension getPreferredSize()
			{ body.setSize(Math.max(1,UpgradeTile.this.getWidth()-14),Short.MAX_VALUE); Dimension size=body.getPreferredSize(); return new Dimension(size.width+14,size.height+14); }
		};
		open.setLayout(new BorderLayout()); open.add(body);
		open.setBorder(BorderFactory.createEmptyBorder(6,6,6,6)); open.setContentAreaFilled(false);
		open.setFont(font); open.setName("open-upgrade-"+id); open.setToolTipText(title);
		open.getAccessibleContext().setAccessibleName("Details for "+title);
		open.addActionListener(e->activate.run());
		open.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),"open-card");
		open.getInputMap().put(KeyStroke.getKeyStroke("SPACE"),"open-card");
		open.getActionMap().put("open-card",new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { open.doClick(); } });
		open.addFocusListener(new FocusAdapter() {
			@Override public void focusGained(FocusEvent e) { focus(true); }
			@Override public void focusLost(FocusEvent e) { focus(false); }
		});
		focus(false); add(open);
	}
	private void focus(boolean focused)
	{ setBorder(BorderFactory.createLineBorder(focused?new Color(244,213,141):ColorScheme.MEDIUM_GRAY_COLOR)); repaint(); }
	void finish()
	{
		MouseAdapter listener=new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) {
			if(SwingUtilities.isLeftMouseButton(e)) { open.requestFocusInWindow(); open.doClick(); }
		} };
		addMouseListener(listener); clickable(body,listener);
	}
	private void clickable(java.awt.Container parent,MouseAdapter listener)
	{
		parent.setFocusable(false); parent.addMouseListener(listener);
		for(java.awt.Component child:parent.getComponents()) {
			child.setFocusable(false);
			if(child instanceof java.awt.Container) { clickable((java.awt.Container)child,listener); }
			else { child.addMouseListener(listener); }
		}
	}
}
