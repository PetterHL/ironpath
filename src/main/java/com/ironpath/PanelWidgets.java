package com.ironpath;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import javax.swing.*;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/** Shared native wrapping and keyboard behavior for the encounter view and retained calculation presenters. */
final class PanelWidgets
{
	private static final float LEFT_ALIGNMENT=0;
	static final Color MUTED=new Color(177,180,184), BENEFIT=new Color(134,207,151), LINE=new Color(61,63,66);
	/** A full-width native surface. Outer spacing belongs to the surface, not its text. */
	static JPanel card(JPanel parent,String name)
	{
		JPanel card=column(parent);
		card.setBackground(new Color(32,34,36)); card.setOpaque(true);
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,8,0,parent.getBackground()),
			BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),BorderFactory.createEmptyBorder(7,8,7,8))));
		card.setName(name); parent.add(card); return card;
	}
	static JPanel column(JPanel parent)
	{
		JPanel column=new JPanel() {
			@Override public Dimension getPreferredSize()
			{
				// Wrapping changes with the allocated sidebar width. BoxLayout otherwise
				// caches a height measured before this padded surface had its width.
				((BoxLayout)getLayout()).invalidateLayout(this); return super.getPreferredSize();
			}
		}; column.setLayout(new BoxLayout(column,BoxLayout.Y_AXIS));
		column.setFont(parent.getFont()); column.setOpaque(false); column.setAlignmentX(LEFT_ALIGNMENT); return column;
	}
	static JTextArea label(JPanel panel,String message,boolean bold,float extra,Color color)
	{
		text(panel,message); JTextArea label=(JTextArea)panel.getComponent(panel.getComponentCount()-1);
		label.setFont(panel.getFont().deriveFont(bold?Font.BOLD:Font.PLAIN,panel.getFont().getSize2D()+extra));
		label.setForeground(color); return label;
	}
	/** Centre the content group; wrapped lines retain their natural reading alignment. */
	static JTextArea centeredLabel(JPanel panel,String message,boolean bold,float extra,Color color)
	{
		JPanel group=new JPanel(new CenteredTextLayout()) {
			@Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE,getPreferredSize().height); }
		}; group.setOpaque(false); group.setFont(panel.getFont()); group.setAlignmentX(LEFT_ALIGNMENT);
		JTextArea text=label(group,message,bold,extra,color); text.putClientProperty("compact-label",Boolean.TRUE);
		text.setUI(new javax.swing.plaf.basic.BasicTextAreaUI() {
			@Override public javax.swing.text.View create(javax.swing.text.Element element)
			{
				return new javax.swing.text.WrappedPlainView(element,true) {
					private int offset(int start,int end) throws javax.swing.text.BadLocationException
					{
						String line=getDocument().getText(start,end-start).stripTrailing();
						int width=text.getWidth()-text.getInsets().left-text.getInsets().right-1;
						return Math.max(0,(width-text.getFontMetrics(text.getFont()).stringWidth(line))/2);
					}
					@Override protected int drawUnselectedText(java.awt.Graphics g,int x,int y,int start,int end) throws javax.swing.text.BadLocationException
					{ return super.drawUnselectedText(g,x+offset(start,end),y,start,end); }
					@Override protected float drawUnselectedText(java.awt.Graphics2D g,float x,float y,int start,int end) throws javax.swing.text.BadLocationException
					{ return super.drawUnselectedText(g,x+offset(start,end),y,start,end); }
				};
			}
		});
		panel.add(group); return text;
	}
	static void checkBox(JCheckBox box,java.awt.Font font)
	{
		box.setFont(font); box.setOpaque(false); box.setAlignmentX(LEFT_ALIGNMENT);
		String label=box.getText(); box.getAccessibleContext().setAccessibleName(label); box.setToolTipText(label);
		if(box.getFontMetrics(font).stringWidth(label)>172)
		{ box.setText("<html><div style='width:125px'>"+label.replace("&","&amp;")+"</div></html>"); }
		box.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH-12,Math.max(28,box.getPreferredSize().height)));
	}
	static JButton button(JPanel panel, String label, Runnable action)
	{
		JButton button = new JButton(label);
		button.setFont(panel.getFont()); button.setMargin(new java.awt.Insets(2,3,2,3));
		button.setForeground(ColorScheme.TEXT_COLOR); button.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
		button.setContentAreaFilled(false); button.setOpaque(true);
		if (label.length() > 24 || button.getFontMetrics(button.getFont()).stringWidth(label)>185)
		{ button.setText("<html><div style='width:135px;text-align:center'>" + label.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</div></html>"); }
		button.setToolTipText(label);
		button.getAccessibleContext().setAccessibleName(label);
		int height = Math.max(28, button.getPreferredSize().height);
		button.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH - 12, height));
		button.setMinimumSize(new Dimension(0, height));
		button.setAlignmentX(LEFT_ALIGNMENT);
		button.addActionListener(event -> action.run());
		button.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("ENTER"), "activate");
		button.getActionMap().put("activate", new javax.swing.AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent event) { button.doClick(); } });
		if(panel instanceof ActionLinks) { panel.add(button); }
		else
		{
			JPanel group=new ActionLinks(); group.setFont(panel.getFont()); group.add(button); panel.add(group);
		}
		return button;
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
				if(getText().isEmpty()) { return new Dimension(0,0); }
				int width = getParent() == null || getParent().getWidth() <= 0 ? 201 : Math.max(1, getParent().getWidth() - getParent().getInsets().left - getParent().getInsets().right);
				// BoxLayout caps the allocated width even when the native viewport is wider.
				// Measure wrapping at that same width so detail paragraphs cannot lose lines.
				width = Math.min(width, super.getMaximumSize().width);
				if ("card-title".equals(getName())) { width=Math.max(1,width-46); }
				if(Boolean.TRUE.equals(getClientProperty("compact-label")) || "card-title".equals(getName()))
				{ width=Math.min(width,java.util.Arrays.stream(getText().split("\n",-1)).mapToInt(line->getFontMetrics(getFont()).stringWidth(line)+2).max().orElse(width)); }
				java.awt.Insets insets = getInsets();
				javax.swing.text.View view = getUI().getRootView(this);
				// Swing reserves one pixel for the document caret when wrapping.
				view.setSize(Math.max(1, width - insets.left - insets.right - 1), Integer.MAX_VALUE);
				return new Dimension(width, (int) Math.ceil(view.getPreferredSpan(javax.swing.text.View.Y_AXIS))
					+ insets.top + insets.bottom);
			}
			@Override public Dimension getMaximumSize() { return new Dimension(super.getMaximumSize().width,getPreferredSize().height); }
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
