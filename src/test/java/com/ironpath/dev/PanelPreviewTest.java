package com.ironpath.dev;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

/** Geometry regressions exercise the same PASS/FAIL report and painting as offlineReview. */
public class PanelPreviewTest
{
	@Rule public TemporaryFolder temporary = new TemporaryFolder();

	@Test public void rejectsTextClippedByAnyImageEdge() throws Exception
	{
		for (Rectangle bounds : List.of(new Rectangle(210, 10, 120, 40), new Rectangle(-5, 10, 120, 40),
			new Rectangle(10, -5, 120, 40), new Rectangle(10, 190, 120, 40)))
		{
			assertFalse(bounds.toString(), review(panel -> {
				JTextArea text = text("Offset text"); text.setBounds(bounds); panel.add(text);
			}).passed());
		}
	}

	@Test public void rejectsTextClippedByAParentEvenInsideTheImage() throws Exception
	{
		assertFalse(review(panel -> {
			JPanel parent = new JPanel(null); parent.setBounds(20, 20, 60, 60); panel.add(parent);
			JTextArea text = text("Parent clip"); text.setBounds(40, 0, 60, 40); parent.add(text);
		}).passed());
		assertFalse(review(panel -> {
			JPanel grandparent = new JPanel(null); grandparent.setBounds(20, 20, 100, 30); panel.add(grandparent);
			JPanel parent = new JPanel(null); parent.setBounds(0, 20, 100, 80); grandparent.add(parent);
			JTextArea text = text("Ancestor clip"); text.setBounds(0, 0, 100, 40); parent.add(text);
		}).passed());
	}

	@Test public void rejectsZeroWidthAndZeroHeightWithText() throws Exception
	{
		for (Dimension size : List.of(new Dimension(0, 40), new Dimension(120, 0)))
		{
			assertFalse(size.toString(), review(panel -> {
				JTextArea text = text("Collapsed text"); text.setBounds(10, 10, size.width, size.height); panel.add(text);
			}).passed());
		}
	}

	@Test public void rejectsPositiveWidthTooSmallForAGlyphOrTheInsets() throws Exception
	{
		assertFalse(review(panel -> {
			JTextArea text = text("W"); text.setBounds(10, 10, 2, 100); panel.add(text);
		}).passed());
		assertFalse(review(panel -> {
			JTextArea text = text("Inset text"); text.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
			text.setBounds(10, 10, 20, 100); panel.add(text);
		}).passed());
	}

	@Test public void measuresWrappingAtAllocatedWidthRatherThanAnOverriddenPreferredSize() throws Exception
	{
		assertFalse(review(panel -> {
			JTextArea text = new JTextArea("A paragraph that needs several lines in the allocated narrow text area.")
			{
				@Override public Dimension getPreferredSize() { return new Dimension(200, 15); }
			};
			configure(text); text.setBounds(10, 10, 45, 30); panel.add(text);
		}).passed());
	}

	@Test public void rejectsUnwrappedTextThatExceedsItsOwnContentWidth() throws Exception
	{
		assertFalse(review(panel -> {
			JTextArea text = text("This unwrapped line cannot fit in forty pixels.");
			text.setLineWrap(false); text.setPreferredSize(new Dimension(40, 30));
			text.setBounds(10, 10, 40, 30); panel.add(text);
		}).passed());
	}

	@Test public void acceptsExactImageAndParentEdgesAndValidWrappingAtBothFontSizes() throws Exception
	{
		for (int size : List.of(12, 20))
		{
			assertTrue(review(panel -> {
				JPanel parent = new JPanel(null); parent.setBounds(105, 0, 120, 200); panel.add(parent);
				JTextArea text = text("A short paragraph that wraps at the actual width.");
				text.setFont(new Font("DejaVu Sans", Font.PLAIN, size));
				text.setBounds(0, 0, 120, 200); parent.add(text);
			}).passed());
		}
	}

	@Test public void ignoresEmptyAreasAndTextInsideAnExplicitlyHiddenParent() throws Exception
	{
		assertTrue(review(panel -> {
			JPanel hidden = new JPanel(null); hidden.setBounds(0, 0, 20, 20); hidden.setVisible(false); panel.add(hidden);
			JTextArea text = text("Hidden text"); text.setBounds(210, 0, 0, 0); hidden.add(text);
			JTextArea empty = text(""); empty.setBounds(210, 0, 0, 0); panel.add(empty);
		}).passed());
	}

	private ReviewReport review(Consumer<JPanel> arrange) throws Exception
	{
		ReviewReport report = new ReviewReport();
		var image = temporary.newFolder().toPath().resolve("preview.png");
		SwingUtilities.invokeAndWait(() -> {
			JPanel panel = new JPanel(null); panel.setPreferredSize(new Dimension(225, 200));
			arrange.accept(panel);
			try { PanelPreview.capture(panel, image, 225, report); }
			catch (Exception ex) { throw new RuntimeException(ex); }
		});
		assertTrue("Failed checks must still leave a current image", Files.exists(image));
		return report;
	}

	private static JTextArea text(String message)
	{ JTextArea text = new JTextArea(message); configure(text); return text; }
	private static void configure(JTextArea text)
	{
		text.setFont(new Font("DejaVu Sans", Font.PLAIN, 12)); text.setMargin(new Insets(0, 0, 0, 0));
		text.setLineWrap(true); text.setWrapStyleWord(true);
	}
}
