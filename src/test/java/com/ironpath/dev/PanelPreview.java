package com.ironpath.dev;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
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
import javax.swing.plaf.FontUIResource;

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
		for (Component c : children(panel))
		{
			if (c instanceof JTextArea && visible(c) && c.getWidth() > 0)
			{
				if (c.getWidth() > width || c.getHeight() < c.getPreferredSize().height)
				{ clipped.add(((JTextArea) c).getText()); }
			}
		}
		report.check(file.getFileName() + " wrapped text clipping", List.of(), clipped);
		BufferedImage image = new BufferedImage(width, panel.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		try { panel.printAll(graphics); } finally { graphics.dispose(); }
		Files.createDirectories(file.getParent());
		if (!ImageIO.write(image, "png", file.toFile())) { throw new IllegalStateException("PNG writer unavailable"); }
		report.section(file.getFileName().toString(), "![Synthetic native Swing preview](" + file.getFileName() + ")\n\n"
			+ width + " × " + image.getHeight() + " px. Empty item sprite wells: no ItemManager/cache sprites loaded.");
	}

	private static void layout(Container parent)
	{
		parent.doLayout();
		for (Component child : parent.getComponents()) { if (child instanceof Container) { layout((Container) child); } }
	}
	private static void requireEdt()
	{ if (!SwingUtilities.isEventDispatchThread()) { throw new IllegalStateException("Preview requires Swing EDT"); } }
}
