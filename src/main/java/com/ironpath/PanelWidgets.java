package com.ironpath;

import java.awt.Dimension;
import javax.swing.*;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/** Shared native wrapping and keyboard behavior for the encounter view and retained calculation presenters. */
final class PanelWidgets
{
	private static final float LEFT_ALIGNMENT=0;
	static JButton button(JPanel panel, String label, Runnable action)
	{
		JButton button = new JButton(label);
		button.setFont(panel.getFont()); button.setMargin(new java.awt.Insets(2,3,2,3));
		button.setForeground(ColorScheme.TEXT_COLOR); button.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
		button.setContentAreaFilled(false); button.setOpaque(true);
		if (label.length() > 24 || button.getFontMetrics(button.getFont()).stringWidth(label)>185)
		{ button.setText("<html><div style='width:150px'>" + label.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</div></html>"); }
		button.setToolTipText(label);
		int height = Math.max(28, button.getPreferredSize().height);
		button.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH - 12, height));
		button.setMinimumSize(new Dimension(0, height));
		button.setAlignmentX(LEFT_ALIGNMENT);
		button.addActionListener(event -> action.run());
		button.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("ENTER"), "activate");
		button.getActionMap().put("activate", new javax.swing.AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent event) { button.doClick(); } });
		panel.add(button); return button;
	}
	static void text(JPanel panel, String message)
	{
		JTextArea text = new JTextArea(message)
		{
			@Override public void scrollRectToVisible(java.awt.Rectangle bounds)
			{
				// A newly rendered label's deferred caret repaint must not scroll the result list.
				if (isFocusOwner()) { super.scrollRectToVisible(bounds); }
			}
			@Override public Dimension getPreferredSize()
			{
				int width = getParent() == null || getParent().getWidth() <= 0 ? 201 : Math.max(1, getParent().getWidth() - getParent().getInsets().left - getParent().getInsets().right);
				// BoxLayout caps the allocated width even when the native viewport is wider.
				// Measure wrapping at that same width so detail paragraphs cannot lose lines.
				width = Math.min(width, getMaximumSize().width);
				if ("card-title".equals(getName())) { width=Math.max(1,width-46); }
				java.awt.Insets insets = getInsets();
				javax.swing.text.View view = getUI().getRootView(this);
				// Swing reserves one pixel for the document caret when wrapping.
				view.setSize(Math.max(1, width - insets.left - insets.right - 1), Integer.MAX_VALUE);
				return new Dimension(width, (int) Math.ceil(view.getPreferredSpan(javax.swing.text.View.Y_AXIS))
					+ insets.top + insets.bottom);
			}
		}; text.setFocusable(false); text.setLineWrap(true); text.setWrapStyleWord(true);
		text.setMinimumSize(new Dimension(0,0));
		text.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		text.setFont(panel.getFont());
		text.setEditable(false); text.setOpaque(false);
		text.setForeground(ColorScheme.TEXT_COLOR);
		text.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0)); text.setAlignmentX(LEFT_ALIGNMENT);
		panel.add(text);
	}
}
