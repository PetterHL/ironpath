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
		named(panel,"n3-policy",JComboBox.class).setSelectedIndex(1); confirmConditions(panel);
	}
	static void confirmConditions(JPanel panel)
	{
		for(String name:List.of("n3-legal","n3-condition-1","n3-condition-2","n3-condition-3","n3-condition-4","n3-condition-5","n3-condition-6","n3-condition-7","n3-condition-8"))
		{ JCheckBox choice=named(panel,name,JCheckBox.class); if(!choice.isSelected()) { choice.doClick(); } }
	}
	@Test public void invalidFieldsExplainTheirOwnBoundsWithoutQueuingOrKeepingResults() throws Exception
	{
		var source=NexPlanComparisonTest.fixture(catalog,27690,0,Map.of(),Map.of("inventory-slots-used",0),false);
		List<Runnable> tasks=new ArrayList<>();
		SwingUtilities.invokeAndWait(()->{
			var panel=new NexScenarioPanel(source,0,0,catalog,text->{},"SYNTHETIC_OFFLINE",new Font("DejaVu Sans",Font.PLAIN,12));
			panel.attach(()->true,tasks::add); PanelPreview.click(panel,"Plan specs & gear");
			String[][] cases={
				{"n3-horizon","4000","Window length: enter a whole number from 1 to 3000"},
				{"n3-horizon","0","Window length"},{"n3-horizon","1.5","Window length"},
				{"n3-energy","100.1","Starting spec energy: use 0–100% in steps of 0.1%"},
				{"n3-energy","-1","Starting spec energy"},{"n3-energy","50.01","Starting spec energy"},{"n3-energy","text","Starting spec energy"},
				{"n3-target-hp","0","Target HP: enter a whole number from 1 to 100000"},
				{"n3-player-hp","256","Player HP: enter a whole number from 1 to 255"},
				{"n3-restore","26","First restore tick: enter a whole number from 0 to 25"},
				{"n3-cap","-1","Damage cap"},{"n3-opportunities","0,0","distinct increasing whole ticks, 0–4"},
				{"n3-opportunities","4,0","Attack ticks"},{"n3-opportunities","0,5","before window end 5"},
				{"n3-opportunities","-1","Attack ticks"},{"n3-opportunities","","Attack ticks"},
				{"n3-opportunities","0,","Attack ticks"},{"n3-opportunities","9999999999999","Attack ticks"},
				{"n3-casts","0,0","Death Charge cast ticks"},{"n3-deaths","5","Eligible death ticks"}
			};
			for(String[] input:cases)
			{
				fill(panel); named(panel,"n3-casts",JTextField.class).setText(""); named(panel,"n3-deaths",JTextField.class).setText("");
				named(panel,input[0],JTextField.class).setText(input[1]); PanelPreview.click(panel,"Compare this plan");
				assertTrue(Arrays.toString(input)+"\n"+PanelPreview.text(panel),PanelPreview.text(panel).contains(input[2]));
				assertTrue(tasks.isEmpty()); assertNull(panel.result()); assertNull(named(panel,"n3-report",JTextArea.class));
			}
			fill(panel); named(panel,"n3-deaths",JTextField.class).setText("");
			named(panel,"n3-energy",JTextField.class).setText("99.9"); PanelPreview.click(panel,"Compare this plan");
			assertEquals(1,tasks.size()); assertFalse(PanelPreview.text(panel).contains("Eligible death ticks:"));
		});
	}
	@Test public void eachScenarioChoiceStartsUnknownAndIndependentlyGatesFiniteResults() throws Exception
	{
		List<Runnable> tasks=new ArrayList<>(); var ref=new AtomicReference<NexScenarioPanel>();
		var source=NexPlanComparisonTest.fixture(catalog,27690,0,Map.of(),Map.of("inventory-slots-used",0),false);
		SwingUtilities.invokeAndWait(()->{
			var panel=new NexScenarioPanel(source,0,0,catalog,text->{},"SYNTHETIC_OFFLINE",new Font("DejaVu Sans",Font.PLAIN,20)); ref.set(panel);
			panel.attach(()->true,tasks::add); PanelPreview.click(panel,"Plan specs & gear");
			assertEquals(10,PanelPreview.children(panel).stream().filter(c->c instanceof JCheckBox).count());
			assertTrue(PanelPreview.children(panel).stream().filter(c->c instanceof JCheckBox).noneMatch(c->((JCheckBox)c).isSelected()));
			fill(panel); PanelPreview.click(panel,"Compare this plan");
		});
		tasks.remove(0).run(); SwingUtilities.invokeAndWait(()->assertNotNull(ref.get().result().comparison.actualToOwned.before.plan));
		for(String name:List.of("n3-legal","n3-condition-1","n3-condition-2","n3-condition-3","n3-condition-4","n3-condition-5","n3-condition-6","n3-condition-7","n3-condition-8"))
		{
			SwingUtilities.invokeAndWait(()->{ named(ref.get(),name,JCheckBox.class).doClick(); PanelPreview.click(ref.get(),"Compare this plan"); });
			tasks.remove(0).run();
			SwingUtilities.invokeAndWait(()->{
				assertNull(name,ref.get().result().comparison.actualToOwned.before.plan);
				assertNull(name,ref.get().result().comparison.actualToOwned.expectedDamage);
				named(ref.get(),name,JCheckBox.class).doClick();
			});
		}
	}

	@Test public void pendingInputEditsAndDetachedViewsCannotPublishOrCopyOldResults() throws Exception
	{
		List<Runnable> tasks=new ArrayList<>(); AtomicReference<String> copied=new AtomicReference<>();
		var source=NexPlanComparisonTest.fixture(catalog,27690,0,Map.of(),Map.of("inventory-slots-used",0),false);
		var ref=new AtomicReference<NexScenarioPanel>();
		SwingUtilities.invokeAndWait(()->{
			var panel=new NexScenarioPanel(source,0,0,catalog,copied::set,"SYNTHETIC_OFFLINE",new Font("DejaVu Sans",Font.PLAIN,12)); ref.set(panel);
			panel.attach(()->true,tasks::add); PanelPreview.click(panel,"Plan specs & gear"); fill(panel);
			PanelPreview.click(panel,"Compare this plan"); assertNull(panel.result());
			named(panel,"n3-target-hp",JTextField.class).setText("400");
		});
		tasks.remove(0).run(); SwingUtilities.invokeAndWait(()->assertNull(ref.get().result()));
		SwingUtilities.invokeAndWait(()->PanelPreview.click(ref.get(),"Compare this plan")); tasks.remove(0).run();
		SwingUtilities.invokeAndWait(()->{
			var panel=ref.get(); assertNotNull(panel.result()); PanelPreview.click(panel,"Copy plan report");
			assertTrue(copied.get().contains("target HP=400")); assertFalse(copied.get().contains("PRIVATE_SENTINEL"));
			named(panel,"n3-target-hp",JTextField.class).setText("300"); PanelPreview.click(panel,"Compare this plan"); panel.detach();
		});
		tasks.remove(0).run(); SwingUtilities.invokeAndWait(()->assertNull(ref.get().result()));
	}
	@Test public void clipboardFailureLeavesSelectableReportAndInvalidPlanNeverQueuesWork() throws Exception
	{
		List<Runnable> tasks=new ArrayList<>(); var ref=new AtomicReference<NexScenarioPanel>();
		var source=NexPlanComparisonTest.fixture(catalog,26374,21944,Map.of(),Map.of("inventory-slots-used",0,"worn-ammo-quantity",2),false);
		SwingUtilities.invokeAndWait(()->{
			var panel=new NexScenarioPanel(source,0,0,catalog,text->{ throw new IllegalStateException("busy"); },"SYNTHETIC_OFFLINE",new Font("DejaVu Sans",Font.PLAIN,20)); ref.set(panel);
			panel.attach(()->true,tasks::add); PanelPreview.click(panel,"Plan specs & gear");
			PanelPreview.click(panel,"Compare this plan"); assertTrue(tasks.isEmpty()); fill(panel); PanelPreview.click(panel,"Compare this plan");
		});
		tasks.remove(0).run(); SwingUtilities.invokeAndWait(()->{
			var panel=ref.get(); assertNotNull(panel.result()); PanelPreview.click(panel,"Copy plan report");
			JTextArea report=named(panel,"n3-report",JTextArea.class); assertNotNull(report); assertFalse(report.isEditable());
			assertTrue(report.getText().contains("SYNTHETIC_OFFLINE")); assertTrue(report.getText().contains("Ammo branch:"));
		});
	}
	@Test public void supplyAndPriceEditsCannotPublishAPendingOlderCalculation() throws Exception
	{
		var source=NexPlanComparisonTest.fixture(catalog,27690,0,Map.of(6685,1L),Map.of("inventory-slots-used",1),false);
		List<Runnable> tasks=new ArrayList<>(); var ref=new AtomicReference<NexScenarioPanel>();
		SwingUtilities.invokeAndWait(()->{
			var panel=new NexScenarioPanel(source,0,0,catalog,text->{},"SYNTHETIC_OFFLINE",new Font("DejaVu Sans",Font.PLAIN,12)); ref.set(panel);
			panel.attach(()->true,tasks::add); PanelPreview.click(panel,"Plan specs & gear"); fill(panel); PanelPreview.click(panel,"Food & potions");
			named(panel,"n3-supply-BREW",JTextField.class).setText("0");
			assertFalse(named(panel,"n3-supplies-confirmed",JCheckBox.class).isSelected());
			PanelPreview.click(panel,"Compare this plan");
		});
		tasks.remove(0).run(); SwingUtilities.invokeAndWait(()->{
			assertNull(ref.get().result().comparison.actualToOwned.before.plan);
			named(ref.get(),"n3-supplies-confirmed",JCheckBox.class).doClick(); PanelPreview.click(ref.get(),"Compare this plan");
			named(ref.get(),"n3-supply-BREW",JTextField.class).setText("1");
		});
		tasks.remove(0).run(); SwingUtilities.invokeAndWait(()->{
			assertNull(ref.get().result()); PanelPreview.click(ref.get(),"Compare this plan");
			PanelPreview.click(ref.get(),"Unit costs");
			JComboBox<?> units=named(ref.get(),"n3-cost-item",JComboBox.class);
			for(int n=0;n<units.getItemCount();n++) if(units.getItemAt(n).toString().endsWith("(6691)")) { units.setSelectedIndex(n); break; }
			named(ref.get(),"n3-cost-value",JTextField.class).setText("0"); PanelPreview.click(ref.get(),"Set unit cost");
		});
		tasks.remove(0).run(); SwingUtilities.invokeAndWait(()->{ assertNull(ref.get().result()); PanelPreview.click(ref.get(),"Compare this plan"); });
		tasks.remove(0).run(); SwingUtilities.invokeAndWait(()->{
			var result=ref.get().result(); assertNotNull(result.comparison.actualToOwned.before.plan);
			assertEquals(Long.valueOf(0),result.costs.get(result.comparison.actualToOwned.before).gp);
			PanelPreview.click(ref.get(),"Clear unit costs"); assertNull(ref.get().result());
		});
	}

}
