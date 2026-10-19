package com.ironpath;

import com.ironpath.advisor.*;
import com.ironpath.nex.*;
import java.awt.Dimension;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.*;
import javax.swing.*;

/** Optional post-kill plan editor. All arithmetic and report formatting run on a worker. */
final class NexScenarioPanel extends JPanel
{
	final NexOwnedResult source;
	final int choice,pair;
	private final KnowledgeCatalog catalog;
	private final NexPanel.ClipboardWriter clipboard;
	private final String provenance;
	private BooleanSupplier valid=()->false;
	private Consumer<Runnable> execute;
	private long request;
	private final JPanel body=new JPanel(), output=new JPanel();
	private final JTextField horizon=new JTextField(""), energy=new JTextField(""), restore=new JTextField(""),
		targetHp=new JTextField(""), playerHp=new JTextField(""), cap=new JTextField(""),
		opportunities=new JTextField(""), casts=new JTextField(""), deaths=new JTextField(""), quantity=new JTextField("1");
	private final JComboBox<String> order=new JComboBox<>(new String[]{"Unknown","Before attacks","After attacks"}),
		kandarin=new JComboBox<>(new String[]{"Unknown","No","Yes"}), ruby=new JComboBox<>(new String[]{"Unknown","No","Yes"}),
		policy=new JComboBox<>(new String[]{"Normal attacks","First special","Spec whenever energy allows"}),
		slot=new JComboBox<>(PoweredMagic.SLOTS.toArray(new String[0]));
	private final JComboBox<Item> item=new JComboBox<>();
	private final Map<String,JCheckBox> conditions=new LinkedHashMap<>();
	private final Map<String,Integer> edits=new TreeMap<>();
	private final Map<Integer,Integer> quantities=new TreeMap<>();
	private final JTextArea changes=new JTextArea();
	private final JButton calculate;
	private NexPlanComparison.Result result;
	private String report;
	private static final class Item
	{
		final int id; final String name;
		Item(int id,String name) { this.id=id; this.name=name; }
		public String toString() { return name+(id==0?"":" ("+id+")"); }
	}
	NexScenarioPanel(NexOwnedResult source,int choice,int pair,KnowledgeCatalog catalog,NexPanel.ClipboardWriter clipboard,String provenance,java.awt.Font font)
	{
		this.source=source; this.choice=choice; this.pair=pair; this.catalog=catalog; this.clipboard=clipboard; this.provenance=provenance;
		setFont(font); setOpaque(false); setLayout(new BoxLayout(this,BoxLayout.Y_AXIS)); setAlignmentX(0);
		for(JPanel panel:List.of(body,output)) { panel.setFont(font); panel.setOpaque(false); panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS)); panel.setAlignmentX(0); }
		body.setVisible(false);
		PanelWidgets.button(this,"Plan specs & gear",()->{ if(valid.getAsBoolean()) { body.setVisible(!body.isVisible()); refresh(); } }); add(body);
		var sample=source.choices.get(choice).actual;
		if(sample.state.specialEnergy!=null && sample.state.specialEnergy>=0 && sample.state.specialEnergy<=1000)
		{ energy.setText(BigDecimal.valueOf(sample.state.specialEnergy,1).toPlainString()); }
		Integer hp=sample.state.visibleLevels.get("HITPOINTS"); if(hp!=null && hp>0) { playerHp.setText(hp.toString()); }
		field("Window length (ticks)",horizon,"n3-horizon"); field("Starting spec energy (%)",energy,"n3-energy");
		field("First restore tick (0–25)",restore,"n3-restore"); field("Restore order",order,"n3-order");
		field("Target HP per attack",targetHp,"n3-target-hp"); field("Player HP per attack",playerHp,"n3-player-hp");
		field("Damage cap per hit",cap,"n3-cap"); field("Kandarin hard diary",kandarin,"n3-kandarin"); field("Ruby bolts allowed",ruby,"n3-ruby");
		field("When to spec",policy,"n3-policy");
		field("Attack ticks",opportunities,"n3-opportunities");
		field("Death Charge cast ticks",casts,"n3-casts"); field("Eligible death ticks",deaths,"n3-deaths");
		PanelWidgets.centeredLabel(body,"Hypothetical gear",true,0,net.runelite.client.ui.ColorScheme.TEXT_COLOR);
		field("Change slot",slot,"n3-upgrade-slot"); field("Exact item",item,"n3-upgrade-item"); field("Usable quantity",quantity,"n3-upgrade-quantity");
		slot.addActionListener(e->{ if(valid.getAsBoolean()) { items(); } });
		item.addActionListener(e->{ Object selected=item.getSelectedItem(); item.setToolTipText(selected==null?null:selected.toString()); }); items();
		PanelWidgets.button(body,"Add item",()->{
			if(!valid.getAsBoolean()) { return; }
			try
			{
				Item selected=(Item)item.getSelectedItem(); if(selected==null) { return; }
				int n=Integer.parseInt(quantity.getText().trim()); if(n<1 || n>20000) { throw new IllegalArgumentException("Use a quantity from 1 to 20000."); }
				edits.put((String)slot.getSelectedItem(),selected.id); if(selected.id>0) { quantities.put(selected.id,n); }
				quantities.keySet().retainAll(new HashSet<>(edits.values())); changes.setText(describeChanges()); invalidateResult();
			}
			catch(IllegalArgumentException ex) { message("Check the hypothetical quantity (1–20000)."); }
		});
		changes.setAlignmentX(0); changes.setMinimumSize(new Dimension(0,0)); changes.setEditable(false); changes.setLineWrap(true); changes.setWrapStyleWord(true); changes.setOpaque(false); changes.setFont(font); body.add(changes);
		PanelWidgets.button(body,"Clear edits",()->{ if(valid.getAsBoolean()) { edits.clear(); quantities.clear(); changes.setText(""); invalidateResult(); } });
		PanelWidgets.centeredLabel(body,"Scenario choices",true,0,net.runelite.client.ui.ColorScheme.TEXT_COLOR);
		String[] labels={"Legal attack windows","Attack & cast ready","No active Death Charge","Lightbearer throughout","Usable gear & stock","Runes & legal casts","Distinct eligible deaths","Fixed HP & phase","Spell/switch delays included"};
		for(int i=0;i<labels.length;i++)
		{
			JCheckBox check=new JCheckBox(labels[i]); String name=i==0?"n3-legal":"n3-condition-"+i;
			check.setName(name); PanelWidgets.checkBox(check,font); conditions.put(name,check); body.add(check);
		}
		calculate=PanelWidgets.button(body,"Compare this plan",this::calculate); calculate.setName("n3-evaluate");
		body.add(output);
		javax.swing.event.DocumentListener changed=new javax.swing.event.DocumentListener() {
			public void insertUpdate(javax.swing.event.DocumentEvent e) { invalidateResult(); }
			public void removeUpdate(javax.swing.event.DocumentEvent e) { invalidateResult(); }
			public void changedUpdate(javax.swing.event.DocumentEvent e) { invalidateResult(); }
		};
		for(JTextField field:List.of(horizon,energy,restore,targetHp,playerHp,cap,opportunities,casts,deaths)) { field.getDocument().addDocumentListener(changed); }
		for(JComboBox<?> combo:List.of(order,kandarin,ruby,policy)) { combo.addActionListener(e->invalidateResult()); }
		conditions.values().forEach(check->check.addActionListener(e->invalidateResult()));
	}
	void attach(BooleanSupplier valid,Consumer<Runnable> execute) { this.valid=valid; this.execute=execute; calculate.setEnabled(valid.getAsBoolean()); }
	void detach() { valid=()->false; }
	NexPlanComparison.Result result() { return result; }
	private void field(String label,JComponent field,String name)
	{
		PanelWidgets.text(body,label); field.setName(name); field.getAccessibleContext().setAccessibleName(label); field.setToolTipText(label); control(field); body.add(field);
		if(field instanceof JComboBox)
		{
			JComboBox<?> combo=(JComboBox<?>)field; PanelWidgets.text(body,""); JTextArea selected=(JTextArea)body.getComponent(body.getComponentCount()-1);
			Runnable selection=()->{
				String value=Objects.toString(combo.getSelectedItem(),""); combo.setToolTipText(value);
				selected.setText(combo.getFontMetrics(combo.getFont()).stringWidth(value)>175?value:"");
				combo.getAccessibleContext().setAccessibleDescription(value); refresh();
			}; combo.addActionListener(e->selection.run()); selection.run();
		}
	}
	private void control(JComponent c)
	{ c.setFont(getFont()); c.setAlignmentX(0); c.setMaximumSize(new Dimension(net.runelite.client.ui.PluginPanel.PANEL_WIDTH-12,Math.max(28,c.getPreferredSize().height))); }
	private void items()
	{
		item.removeAllItems(); item.addItem(new Item(0,"Empty slot")); String selected=(String)slot.getSelectedItem();
		Map<Integer,String> names=new TreeMap<>();
		catalog.melee.items.stream().filter(i->selected.equals(i.slot)).forEach(i->names.put(i.id,i.label));
		catalog.ranged.items.stream().filter(i->selected.equals(i.slot)).forEach(i->names.put(i.id,i.label));
		names.entrySet().stream().sorted(Map.Entry.<Integer,String>comparingByValue().thenComparing(Map.Entry.comparingByKey())).forEach(e->item.addItem(new Item(e.getKey(),e.getValue())));
	}
	private String describeChanges()
	{ StringBuilder out=new StringBuilder(); edits.forEach((slot,id)->out.append(slot).append(": ").append(catalog.itemNames().getOrDefault(id,id==0?"Empty":"Item "+id)).append("\n")); return out.toString(); }
	private void refresh()
	{
		output.invalidate(); body.invalidate();
		for(java.awt.Container parent=this;parent!=null;parent=parent.getParent()) { parent.invalidate(); }
		revalidate(); repaint();
	}
	private void invalidateResult() { ++request; result=null; report=null; output.removeAll(); calculate.setEnabled(valid.getAsBoolean()); refresh(); }
	private void message(String text) { output.removeAll(); PanelWidgets.text(output,text); output.getComponent(0).setName("n3-feedback"); refresh(); }
	private void calculate()
	{
		if(!valid.getAsBoolean()) { return; }
		NexPlanComparison.Declaration declaration;
		try
		{
			int end=number(horizon,"Window length",1,SpecTimeline.MAX_TICKS,true);
			Integer initial=energy();
			declaration=new NexPlanComparison.Declaration(end,initial,number(restore,"First restore tick",0,25,false),
				order.getSelectedIndex()==0?null:order.getSelectedIndex()==1?SpecTimeline.RestoreOrder.BEFORE_EVENTS:SpecTimeline.RestoreOrder.AFTER_EVENTS,
				number(targetHp,"Target HP",1,100000,false),number(playerHp,"Player HP",1,255,false),number(cap,"Damage cap",1,100000,false),tri(kandarin),tri(ruby),conditions.values().stream().allMatch(JCheckBox::isSelected),NexPlanComparison.Policy.values()[policy.getSelectedIndex()],
				offsets(opportunities,"Attack ticks",end,true),offsets(casts,"Death Charge cast ticks",end,false),offsets(deaths,"Eligible death ticks",end,false),edits,quantities);
		}
		catch(InputError ex) { invalidateResult(); message(ex.getMessage()); return; }
		String selectedConditions=conditions.values().stream().map(check->check.getToolTipText()+"="+check.isSelected()).collect(java.util.stream.Collectors.joining("; "));
		long ticket=++request; result=null; report=null; message("Recalculating…"); calculate.setEnabled(false);
		try { execute.accept(()->{
			NexPlanComparison.Result evaluated=null; String formatted=null;
			try
			{
				evaluated=new NexPlanComparison(catalog).evaluate(source,choice,pair,declaration);
				formatted="Evidence: "+provenance+"\nScenario choices: "+selectedConditions+"\n"+evaluated.report();
				if(formatted.length()>24000) { formatted=formatted.substring(0,23920)+"\n[Fields omitted by size limit]"; }
			}
			catch(IllegalArgumentException ex) { /* Bounded invalid/incompatible inputs are presented without raw exception data. */ }
			var finished=evaluated; var text=formatted;
			SwingUtilities.invokeLater(()->{
				if(ticket!=request || !valid.getAsBoolean()) { if(ticket==request) { calculate.setEnabled(true); } return; }
				calculate.setEnabled(true); result=finished; report=text; output.removeAll();
				if(finished==null) { message("Incompatible gear or plan"); return; }
				show(finished); refresh();
			});
		}); }
		catch(java.util.concurrent.RejectedExecutionException ex) { calculate.setEnabled(true); message("Calculation unavailable · try again"); }
	}
	private void show(NexPlanComparison.Result result)
	{
		PanelWidgets.centeredLabel(output,"Conditional plan results",true,0,net.runelite.client.ui.ColorScheme.TEXT_COLOR);
		show("Captured gear",result.comparison.actualToOwned.before);
		show("Owned gear",result.comparison.actualToOwned.after);
		show("Hypothetical gear",result.comparison.ownedToUpgrade.after);
		PanelWidgets.text(output,"Captured → owned damage: "+gain(result.comparison.actualToOwned.expectedDamage));
		PanelWidgets.text(output,"Owned → hypothetical damage: "+gain(result.comparison.ownedToUpgrade.expectedDamage));
		PanelWidgets.text(output,"");
		JTextArea feedback=(JTextArea)output.getComponent(output.getComponentCount()-1);
		JButton copy=PanelWidgets.button(output,"Copy plan report",()->{
			if(!valid.getAsBoolean() || report==null) { return; }
			JScrollPane reportScroll=NexScenarioPanel.reportScroll(output); reportScroll.setVisible(true);
			try { clipboard.copy(report); feedback.setText("Scenario report copied."); }
			catch(RuntimeException ex) { feedback.setText("Clipboard unavailable. Select and copy the report below."); }
			refresh();
		});
		copy.setName("n3-copy-report");
		JTextArea text=new JTextArea(report); text.setName("n3-report"); text.setFont(getFont()); text.setEditable(false); text.setLineWrap(true); text.setWrapStyleWord(true);
		JScrollPane scroll=new JScrollPane(text); scroll.setName("n3-report-scroll"); scroll.setVisible(false); scroll.setAlignmentX(0); scroll.setPreferredSize(new Dimension(200,160)); scroll.setMaximumSize(new Dimension(net.runelite.client.ui.PluginPanel.PANEL_WIDTH-12,160)); output.add(scroll);
	}
	private void show(String label,NexPackageComparison.Evaluation e)
	{
		PanelWidgets.text(output,label);
		if(e.plan==null) { PanelWidgets.text(output,"Resources unavailable"); return; }
		var p=e.plan; var r=p.resources;
		PanelWidgets.text(output,"Expected launch damage: "+metric(p.expectedDamage)+" · self damage: "+metric(p.expectedSelfDamage));
		PanelWidgets.text(output,"Normal / Voidwaker / ZCB attacks: "+r.ordinaryAttacks+" / "+r.voidwakerSpecs+" / "+r.zaryteSpecs
			+" · Death Charge: "+r.deathChargeProcs);
		PanelWidgets.text(output,"Energy spent / restored / lost at cap / final (%): "+r.spent/10.0+" / "+r.restored/10.0+" / "+r.wasted/10.0+" / "+r.finalEnergy/10.0);
		PanelWidgets.text(output,"Ammo spent: "+p.spentAmmo.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e2->catalog.itemNames().getOrDefault(e2.getKey(),"Item "+e2.getKey())+": "+e2.getValue()).collect(java.util.stream.Collectors.joining(", "))+" · splinters: "+p.spentSplinters);
		if(!p.gaps.isEmpty() || !e.resourceGaps.isEmpty()) { PanelWidgets.text(output,"Some results unavailable"); }
	}
	private static JScrollPane reportScroll(JPanel output)
	{ return (JScrollPane)java.util.Arrays.stream(output.getComponents()).filter(c->c instanceof JScrollPane).findFirst().orElseThrow(); }

	private static String metric(Double value) { return value==null?"unavailable":String.format(Locale.ROOT,"%.2f",value); }
	private static String gain(Double value) { return value==null?"unavailable":String.format(Locale.ROOT,"%+.2f",value); }
	private static final class InputError extends IllegalArgumentException
	{ InputError(String message) { super(message); } }
	private static Integer number(JTextField field,String label,int min,int max,boolean required)
	{
		if(field.getText().isBlank() && !required) { return null; }
		try { int value=Integer.parseInt(field.getText().trim()); if(value>=min && value<=max) { return value; } }
		catch(NumberFormatException ex) { /* Use the field's public constraint below. */ }
		throw new InputError(label+": enter a whole number from "+min+" to "+max+".");
	}
	private Integer energy()
	{
		if(energy.getText().isBlank()) { return null; }
		try
		{
			int value=new BigDecimal(energy.getText().trim()).movePointRight(1).intValueExact();
			if(value>=0 && value<=1000) { return value; }
		}
		catch(IllegalArgumentException | ArithmeticException ex) { /* Use a bounded, actionable message. */ }
		throw new InputError("Starting spec energy: use 0–100% in steps of 0.1%.");
	}
	private static Boolean tri(JComboBox<?> combo) { return combo.getSelectedIndex()==0?null:combo.getSelectedIndex()==2; }
	private static List<Integer> offsets(JTextField field,String label,int end,boolean required)
	{
		if(field.getText().isBlank() && !required) { return List.of(); }
		String error=label+": enter "+(required?"1–128":"up to 128")+" distinct increasing whole ticks, 0–"+(end-1)+", separated by commas (before window end "+end+").";
		String[] parts=field.getText().trim().split(",",-1); if(parts.length>NexPlanComparison.MAX_OPPORTUNITIES) { throw new InputError(error); }
		List<Integer> result=new ArrayList<>(); int previous=-1;
		for(String part:parts)
		{
			int tick; try { tick=Integer.parseInt(part.trim()); } catch(NumberFormatException ex) { throw new InputError(error); }
			if(tick<0 || tick>=end || tick<=previous) { throw new InputError(error); } result.add(tick); previous=tick;
		}
		return result;
	}
}
