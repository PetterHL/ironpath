package com.ironpath.dev;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.View;

/** Paints actual Swing components on the EDT; no recreation of the product UI. */
public final class PanelPreview
{
	private PanelPreview() { }
	public static void fonts(int size)
	{
		requireEdt();
		Font font = new Font("DejaVu Sans", Font.PLAIN, size);
		if (!font.getFamily().equals("DejaVu Sans")) { throw new IllegalStateException("Install fonts-dejavu-core for reproducible previews"); }
		for (Object key : List.copyOf(UIManager.getDefaults().keySet()))
		{ if (UIManager.get(key) instanceof Font) { UIManager.put(key, new FontUIResource(font)); } }
	}

	public static List<Component> children(Container parent)
	{
		List<Component> all = new ArrayList<>();
		for (Component child : parent.getComponents())
		{
			all.add(child);
			if (child instanceof Container) { all.addAll(children((Container) child)); }
		}
		return all;
	}

	public static boolean visible(Component c)
	{
		for (Component p = c; p != null; p = p.getParent()) { if (!p.isVisible()) { return false; } }
		return true;
	}

	public static String text(Container panel)
	{
		requireEdt();
		StringBuilder text = new StringBuilder();
		for (Component c : children(panel))
		{ if (c instanceof JTextArea && visible(c)) { text.append(((JTextArea) c).getText()).append('\n'); } }
		return text.toString();
	}

	public static void click(Container panel, String tooltip)
	{
		requireEdt();
		JButton button = (JButton) children(panel).stream().filter(c -> c instanceof JButton && visible(c)
			&& tooltip.equals(((JButton) c).getToolTipText())).findFirst().orElseThrow(() -> new IllegalStateException("Missing visible action: " + tooltip));
		button.doClick(0);
	}

	public static JComboBox<?> combo(Container panel, String name)
	{
		requireEdt();
		return (JComboBox<?>) children(panel).stream().filter(c -> c instanceof JComboBox && visible(c)
			&& name.equals(c.getName())).findFirst().orElseThrow();
	}

	public static void capture(JComponent panel, Path file, int width, ReviewReport report) throws Exception
	{
		requireEdt();
		panel.setSize(width, 10000);
		for (int pass = 0; pass < 4; pass++)
		{
			panel.invalidate(); layout(panel);
			panel.setSize(width, panel.getPreferredSize().height);
		}
		layout(panel);
		List<String> clipped = new ArrayList<>();
		Rectangle imageBounds = new Rectangle(0, 0, width, panel.getHeight());
		for (Component c : children(panel))
		{
			if (c instanceof JTextArea && visible(c) && !((JTextArea) c).getText().isEmpty()
				&& SwingUtilities.getAncestorOfClass(JScrollPane.class,c)==null)
			{
				JTextArea text = (JTextArea) c;
				String reason = clippingReason(text, panel, imageBounds);
				if (reason != null) { clipped.add(reason + ": " + text.getText()); }
			}
		}
		report.check(file.getFileName() + " wrapped text clipping", List.of(), clipped);
		List<String> controls=new ArrayList<>();
		for(Component c:children(panel))
		{
			// RuneLiteLAF deliberately gives native scrollbar arrow buttons zero size.
			if(!visible(c) || !(c instanceof javax.swing.AbstractButton || c instanceof JLabel || c instanceof javax.swing.JTextField || c instanceof JComboBox)
				|| SwingUtilities.getAncestorOfClass(javax.swing.JScrollBar.class,c)!=null) { continue; }
			Rectangle bounds=SwingUtilities.convertRectangle(c,new Rectangle(0,0,c.getWidth(),c.getHeight()),panel);
			if(c.getWidth()<=0 || c.getHeight()<=0 || !imageBounds.contains(bounds)) { controls.add("Control outside image: "+c.getName()); continue; }
			if(c instanceof javax.swing.AbstractButton)
			{
				javax.swing.AbstractButton button=(javax.swing.AbstractButton)c; Insets insets=button.getInsets();
				javax.swing.Icon icon=button.getIcon();
				if(icon==null && c instanceof javax.swing.JCheckBox) { icon=UIManager.getIcon("CheckBox.icon"); }
				int iconWidth=icon==null?0:icon.getIconWidth()+button.getIconTextGap();
				View html=(View)button.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
				if(html==null && button.getFontMetrics(button.getFont()).stringWidth(button.getText())>button.getWidth()-insets.left-insets.right-iconWidth)
				{ controls.add("Truncated button: "+button.getToolTipText()); }
				if(html!=null && (html.getPreferredSpan(View.X_AXIS)>button.getWidth()-insets.left-insets.right
					|| html.getPreferredSpan(View.Y_AXIS)>button.getHeight()-insets.top-insets.bottom))
				{ controls.add("Truncated wrapped button: "+button.getToolTipText()); }
			}
		}
		report.check(file.getFileName()+" button/label geometry",List.of(),controls);
		BufferedImage image = new BufferedImage(width, panel.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		try { panel.printAll(graphics); } finally { graphics.dispose(); }
		Files.createDirectories(file.getParent());
		if (!ImageIO.write(image, "png", file.toFile())) { throw new IllegalStateException("PNG writer unavailable"); }
		report.section(file.getFileName().toString(), "![Synthetic native Swing preview](" + file.getFileName() + ")\n\n"
			+ width + " × " + image.getHeight() + " px. Sprites use the pinned genuine game-art fixtures through the ItemManager boundary where configured; see the sprite provenance notice.");
	}

	/** Paint an actual scroll viewport. Required controls must fit completely in the
	 * chosen screenful; full-height PNGs alone cannot establish first-screen access. */
	public static void viewport(JComponent panel,Path file,int width,int height,String focusName,List<String> required,ReviewReport report) throws Exception
	{
		requireEdt();
		Container previous=panel.getParent();
		JViewport viewport=new JViewport(); viewport.setView(panel); viewport.setSize(width,height);
		// Reparenting invalidates nested width-dependent BoxLayouts. Settle them as
		// for a full-height capture before scrolling, so sprite rows are not compressed
		// by a height cached before the view acquired its final width.
		for(int pass=0;pass<4;pass++)
		{
			panel.invalidate(); layout(panel);
			panel.setSize(width,Math.max(height,panel.getPreferredSize().height));
		}
		layout(panel);
		viewport.setViewPosition(new java.awt.Point(0,0));
		if(focusName!=null)
		{
			Component target=children(panel).stream().filter(c->focusName.equals(c.getName())).findFirst().orElseThrow();
			var at=SwingUtilities.convertPoint(target,0,0,panel);
			viewport.setViewPosition(new java.awt.Point(0,Math.max(0,Math.min(at.y-20,panel.getHeight()-height))));
		}
		Rectangle visible=new Rectangle(viewport.getViewPosition(),viewport.getExtentSize());
		for(String name:required)
		{
			Component target=children(panel).stream().filter(c->name.equals(c.getName())).findFirst().orElseThrow();
			Rectangle bounds=SwingUtilities.convertRectangle(target,new Rectangle(0,0,target.getWidth(),target.getHeight()),panel);
			report.check(file.getFileName()+" visible control "+name,true,visible.contains(bounds));
		}
		BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB); Graphics2D graphics=image.createGraphics();
		try { viewport.printAll(graphics); } finally { graphics.dispose(); viewport.remove(panel); if(previous!=null) { previous.add(panel); } }
		ImageIO.write(image,"png",file.toFile());
		report.section(file.getFileName().toString(),"![Synthetic 600px scroll viewport]("+file.getFileName()+")\n\nActual JViewport; focus/OS scrolling still requires a client check.");
	}

	private static String clippingReason(JTextArea text, JComponent panel, Rectangle imageBounds)
	{
		if (text.getWidth() <= 0 || text.getHeight() <= 0) { return "Collapsed text area"; }
		Rectangle bounds = SwingUtilities.convertRectangle(text,
			new Rectangle(0, 0, text.getWidth(), text.getHeight()), panel);
		if (!imageBounds.contains(bounds)) { return "Text area extends outside the image"; }
		// Every ancestor clips its children, even when the child's own bounds fit the image.
		// Stop at the captured root: this PNG contains its full contents, not an outer viewport.
		for (Container parent = text.getParent(); parent != panel; parent = parent.getParent())
		{
			Rectangle parentBounds = SwingUtilities.convertRectangle(parent,
				new Rectangle(0, 0, parent.getWidth(), parent.getHeight()), panel);
			if (!parentBounds.contains(bounds)) { return "Text area extends outside an ancestor"; }
		}
		Insets insets = text.getInsets();
		// Swing reserves one pixel for the caret when wrapping, as in PanelWidgets.
		int contentWidth = text.getWidth() - insets.left - insets.right - 1;
		int contentHeight = text.getHeight() - insets.top - insets.bottom;
		if (contentWidth <= 0 || contentHeight <= 0) { return "No usable text space"; }
		FontMetrics metrics = text.getFontMetrics(text.getFont());
		if (text.getText().codePoints().filter(c -> !Character.isISOControl(c))
			.anyMatch(c -> metrics.charWidth(c) > contentWidth)) { return "Text width is smaller than a glyph"; }
		View view = text.getUI().getRootView(text);
		try
		{
			// Measure the allocated width, not an overridden preferred size based on a wider parent.
			view.setSize(contentWidth, Integer.MAX_VALUE);
			if (!text.getLineWrap() && view.getPreferredSpan(View.X_AXIS) > contentWidth)
			{ return "Unwrapped text exceeds the available width"; }
			if (Math.ceil(view.getPreferredSpan(View.Y_AXIS)) > contentHeight)
			{ return "Wrapped text exceeds the available height"; }
			return null;
		}
		finally { view.setSize(contentWidth, contentHeight); }
	}

	private static void layout(Container parent)
	{
		parent.doLayout();
		for (Component child : parent.getComponents()) { if (child instanceof Container) { layout((Container) child); } }
	}
	private static void requireEdt()
	{ if (!SwingUtilities.isEventDispatchThread()) { throw new IllegalStateException("Preview requires Swing EDT"); } }
}
