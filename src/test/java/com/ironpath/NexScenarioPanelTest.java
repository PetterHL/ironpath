package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.*;
import com.ironpath.dev.PanelPreview;
import java.awt.Font;
import java.util.*;
import java.util.concurrent.atomic.*;
import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NexScenarioPanelTest
{
	private final KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson());
	public NexScenarioPanelTest() throws Exception { }
	static <T extends java.awt.Component> T named(java.awt.Container root,String name,Class<T> type)
	{
		for(java.awt.Component c:root.getComponents())
		{
			if(name.equals(c.getName()) && type.isInstance(c)) { return type.cast(c); }
			if(c instanceof java.awt.Container) { T found=named((java.awt.Container)c,name,type); if(found!=null) { return found; } }
		}
		return null;
	}
	static void fill(JPanel panel)
	{
		for(var entry:Map.of("n3-horizon","5","n3-energy","100","n3-restore","25","n3-target-hp","500","n3-player-hp","99","n3-cap","200","n3-opportunities","0,4").entrySet())
		{ named(panel,entry.getKey(),JTextField.class).setText(entry.getValue()); }
		named(panel,"n3-order",JComboBox.class).setSelectedIndex(1);
		named(panel,"n3-kandarin",JComboBox.class).setSelectedIndex(1); named(panel,"n3-ruby",JComboBox.class).setSelectedIndex(2);
		named(panel,"n3-policy",JComboBox.class).setSelectedIndex(1); named(panel,"n3-legal",JCheckBox.class).doClick();
	}
	@Test public void pendingInputEditsAndDetachedViewsCannotPublishOrCopyOldResults() throws Exception
	{
		List<Runnable> tasks=new ArrayList<>(); AtomicReference<String> copied=new AtomicReference<>();
		var source=NexPlanComparisonTest.fixture(catalog,27690,0,Map.of(),Map.of("inventory-slots-used",0),false);
		var ref=new AtomicReference<NexScenarioPanel>();
		SwingUtilities.invokeAndWait(()->{
			var panel=new NexScenarioPanel(source,0,0,catalog,copied::set,"SYNTHETIC_OFFLINE",new Font("DejaVu Sans",Font.PLAIN,12)); ref.set(panel);
			panel.attach(()->true,tasks::add); PanelPreview.click(panel,"Conditional N3 package plan"); fill(panel);
			PanelPreview.click(panel,"Evaluate plan"); assertNull(panel.result());
			named(panel,"n3-target-hp",JTextField.class).setText("400");
		});
		tasks.remove(0).run(); SwingUtilities.invokeAndWait(()->assertNull(ref.get().result()));
		SwingUtilities.invokeAndWait(()->PanelPreview.click(ref.get(),"Evaluate plan")); tasks.remove(0).run();
		SwingUtilities.invokeAndWait(()->{
			var panel=ref.get(); assertNotNull(panel.result()); PanelPreview.click(panel,"Copy plan report");
			assertTrue(copied.get().contains("target HP=400")); assertFalse(copied.get().contains("PRIVATE_SENTINEL"));
			named(panel,"n3-target-hp",JTextField.class).setText("300"); PanelPreview.click(panel,"Evaluate plan"); panel.detach();
		});
		tasks.remove(0).run(); SwingUtilities.invokeAndWait(()->assertNull(ref.get().result()));
	}
	@Test public void clipboardFailureLeavesSelectableReportAndInvalidPlanNeverQueuesWork() throws Exception
	{
		List<Runnable> tasks=new ArrayList<>(); var ref=new AtomicReference<NexScenarioPanel>();
		var source=NexPlanComparisonTest.fixture(catalog,26374,21944,Map.of(),Map.of("inventory-slots-used",0,"worn-ammo-quantity",2),false);
		SwingUtilities.invokeAndWait(()->{
			var panel=new NexScenarioPanel(source,0,0,catalog,text->{ throw new IllegalStateException("busy"); },"SYNTHETIC_OFFLINE",new Font("DejaVu Sans",Font.PLAIN,20)); ref.set(panel);
			panel.attach(()->true,tasks::add); PanelPreview.click(panel,"Conditional N3 package plan");
			PanelPreview.click(panel,"Evaluate plan"); assertTrue(tasks.isEmpty()); fill(panel); PanelPreview.click(panel,"Evaluate plan");
		});
		tasks.remove(0).run(); SwingUtilities.invokeAndWait(()->{
			var panel=ref.get(); assertNotNull(panel.result()); PanelPreview.click(panel,"Copy plan report");
			JTextArea report=named(panel,"n3-report",JTextArea.class); assertNotNull(report); assertFalse(report.isEditable());
			assertTrue(report.getText().contains("SYNTHETIC_OFFLINE")); assertTrue(report.getText().contains("Ammo branch:"));
		});
	}
}
