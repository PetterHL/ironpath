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
	private final Map<NexSupplies.Kind,JTextField> supplyFields=new EnumMap<>(NexSupplies.Kind.class);
	private final Map<Integer,Long> unitCosts=new TreeMap<>();
	private final JCheckBox supplyConfirmed=new JCheckBox("Only declared supply effects");
	private final JTextField targetDefence=new JTextField(),targetMagic=new JTextField();
	private final JComboBox<String> overhead=new JComboBox<>(new String[]{"Unknown","None","Soul Split","Deflect Melee"});
	private final JCheckBox zarosFixed=new JCheckBox("Fixed overhead & stats");
	private final JCheckBox perAttack=new JCheckBox("Use per-attack conditions"),impactConfirmed=new JCheckBox("Overhead & stats at impact");
	private final JTextField impactTick=new JTextField(),impactHp=new JTextField(),impactPlayerHp=new JTextField(),impactCap=new JTextField(),
		impactDefence=new JTextField(),impactMagic=new JTextField();
	private final JComboBox<String> impactRuby=new JComboBox<>(new String[]{"Unknown","No","Yes"}),
		impactOverhead=new JComboBox<>(new String[]{"Unknown","None","Soul Split","Deflect Melee"});
	private final Map<Integer,NexPlanComparison.Impact> impacts=new TreeMap<>();
	private boolean impactDirty;
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
		field("Kandarin hard diary",kandarin,"n3-kandarin");
		JPanel fixedBody=PanelWidgets.column(body); body.add(fixedBody);
		field(fixedBody,"Target HP per attack",targetHp,"n3-target-hp"); field(fixedBody,"Player HP per attack",playerHp,"n3-player-hp");
		field(fixedBody,"Damage cap per hit",cap,"n3-cap"); field(fixedBody,"Ruby bolts allowed",ruby,"n3-ruby");
		if("ZAROS".equals(sample.phase))
		{
			PanelWidgets.centeredLabel(fixedBody,"Zaros target",true,0,net.runelite.client.ui.ColorScheme.TEXT_COLOR);
			overhead.setSelectedIndex(sample.targetState.overhead().ordinal());
			field(fixedBody,"Overhead at impact",overhead,"n3-zaros-overhead");
			field(fixedBody,"Target Defence level",targetDefence,"n3-zaros-defence"); field(fixedBody,"Target Magic level",targetMagic,"n3-zaros-magic");
			zarosFixed.setName("n3-zaros-fixed"); PanelWidgets.checkBox(zarosFixed,font); fixedBody.add(zarosFixed);
			zarosFixed.setToolTipText("Declare these target stats and overhead at every hypothetical impact, with no intervening transfers. Exact HP remains a separate input; health-bar readings are not exact HP.");
		}
		field("When to spec",policy,"n3-policy");
		field("Attack ticks",opportunities,"n3-opportunities");
		perAttack.setName("n3-per-attack"); PanelWidgets.checkBox(perAttack,font); body.add(perAttack);
		perAttack.setToolTipText("Replace fixed HP, caps, ruby eligibility and Zaros inputs with saved values for each attack tick. Missing ticks and blank fields stay unknown; fixed values are not reused.");
		JPanel impactBody=PanelWidgets.column(body); impactBody.setVisible(false); body.add(impactBody);
		field(impactBody,"Attack tick",impactTick,"n3-impact-tick");
		field(impactBody,"Target HP",impactHp,"n3-impact-hp"); field(impactBody,"Player HP",impactPlayerHp,"n3-impact-player-hp");
		field(impactBody,"Damage cap",impactCap,"n3-impact-cap"); field(impactBody,"Ruby bolts allowed",impactRuby,"n3-impact-ruby");
		if("ZAROS".equals(sample.phase))
		{
			field(impactBody,"Overhead at impact",impactOverhead,"n3-impact-overhead");
			field(impactBody,"Target Defence level",impactDefence,"n3-impact-defence"); field(impactBody,"Target Magic level",impactMagic,"n3-impact-magic");
			impactConfirmed.setName("n3-impact-confirmed"); PanelWidgets.checkBox(impactConfirmed,font); impactBody.add(impactConfirmed);
			impactConfirmed.setToolTipText("Confirm these separately supplied conditions at this hypothetical attack's impact. No overhead cadence, stat transfer or exact HP is inferred.");
		}
		PanelWidgets.text(impactBody,""); JTextArea impactSummary=(JTextArea)impactBody.getComponent(impactBody.getComponentCount()-1); impactBody.remove(impactSummary); impactSummary.setName("n3-impact-summary");
		PanelWidgets.button(impactBody,"Save attack conditions",()->{
			if(!valid.getAsBoolean()) { return; }
			try
			{
				int tick=impactTick();
				impacts.put(tick,new NexPlanComparison.Impact(number(impactHp,"Target HP",1,100000,false),number(impactPlayerHp,"Player HP",1,255,false),number(impactCap,"Damage cap",1,100000,false),tri(impactRuby),
					new NexPlanComparison.Zaros(NexTargetState.Overhead.values()[impactOverhead.getSelectedIndex()],number(impactDefence,"Target Defence",0,1000,false),number(impactMagic,"Target Magic",0,1000,false),impactConfirmed.isSelected())));
				impactDirty=false; impactSummary.setText(impactSummary()); invalidateResult();
			}
			catch(InputError ex) { message(ex.getMessage()); }
		});
		PanelWidgets.button(impactBody,"Load attack conditions",()->{
			if(!valid.getAsBoolean()) { return; }
			try
			{
				var impact=impacts.getOrDefault(impactTick(),NexPlanComparison.Impact.UNKNOWN);
				impactHp.setText(value(impact.targetHp)); impactPlayerHp.setText(value(impact.playerHp)); impactCap.setText(value(impact.hardCap));
				impactRuby.setSelectedIndex(impact.rubyAllowed==null?0:impact.rubyAllowed?2:1); impactOverhead.setSelectedIndex(impact.zaros.overhead.ordinal());
				impactDefence.setText(value(impact.zaros.defenceLevel)); impactMagic.setText(value(impact.zaros.magicLevel)); impactConfirmed.setSelected(impact.zaros.fixedWindow);
				impactDirty=false; invalidateResult();
			}
			catch(InputError ex) { message(ex.getMessage()); }
		});
		PanelWidgets.button(impactBody,"Clear attack conditions",()->{ if(valid.getAsBoolean()) { impacts.clear(); impactDirty=false; impactSummary.setText(""); invalidateResult(); } }); impactBody.add(impactSummary);
		perAttack.addActionListener(e->{ fixedBody.setVisible(!perAttack.isSelected()); impactBody.setVisible(perAttack.isSelected()); invalidateResult(); });
		field("Death Charge cast ticks",casts,"n3-casts"); field("Eligible death ticks",deaths,"n3-deaths");
		JPanel supplyBody=PanelWidgets.column(body); supplyBody.setVisible(false);
		PanelWidgets.button(body,"Food & potions",()->{ if(!valid.getAsBoolean()) { return; } supplyBody.setVisible(!supplyBody.isVisible()); refresh(); }); body.add(supplyBody);
		for(NexSupplies.Kind kind:NexSupplies.Kind.values())
		{
			JTextField ticks=new JTextField(); supplyFields.put(kind,ticks);
			PanelWidgets.text(supplyBody,kind.label+" ticks"); supplyBody.getComponent(supplyBody.getComponentCount()-1).setName("n3-supply-label-"+kind.name()); ticks.setName("n3-supply-"+kind.name());
			ticks.getAccessibleContext().setAccessibleName(kind.label+" ticks"); ticks.setToolTipText("Declared uses; comma-separated tick offsets"); control(ticks); supplyBody.add(ticks);
		}
		supplyConfirmed.setName("n3-supplies-confirmed"); PanelWidgets.checkBox(supplyConfirmed,font); supplyBody.add(supplyConfirmed);
		supplyConfirmed.setToolTipText("Declare usable frozen supply stock, eating/drinking readiness and no other level changes, decay or effects. HP remains separately supplied per attack.");
		JPanel costBody=PanelWidgets.column(body); costBody.setVisible(false);
		PanelWidgets.button(body,"Unit costs",()->{ if(!valid.getAsBoolean()) { return; } costBody.setVisible(!costBody.isVisible()); refresh(); }); body.add(costBody);
		JComboBox<Item> costItem=new JComboBox<>(); costItem.setName("n3-cost-item");
		Map<Integer,String> costNames=new TreeMap<>();
		catalog.ranged.items.stream().filter(i->i.ammo!=RangedItem.Ammo.NONE).forEach(i->costNames.put(i.id,i.label));
		costNames.put(NexPlanSupport.SPLINTER,"Sunfire splinter");
		costNames.putAll(NexSpellResources.RUNES);
		for(NexSupplies.Kind kind:NexSupplies.Kind.values()) { costNames.put(kind.costKey(),kind.label); }
		costNames.forEach((id,name)->costItem.addItem(new Item(id,name)));
		field(costBody,"Cost unit",costItem,"n3-cost-item");
		JTextField costValue=new JTextField(); costValue.setName("n3-cost-value"); control(costValue);
		PanelWidgets.text(costBody,"GP per unit"); costBody.add(costValue); costValue.getAccessibleContext().setAccessibleName("GP per unit");
		PanelWidgets.text(costBody,""); JTextArea costSummary=(JTextArea)costBody.getComponent(costBody.getComponentCount()-1); costSummary.setName("n3-cost-summary"); costBody.remove(costSummary);
		costItem.addActionListener(e->{ Item selected=(Item)costItem.getSelectedItem(); costItem.setToolTipText(selected==null?null:selected.toString()); });
		PanelWidgets.button(costBody,"Set unit cost",()->{
			if(!valid.getAsBoolean()) { return; }
			try
			{
				long gp=Long.parseLong(costValue.getText().trim()); if(gp<0 || gp>1000000000L) { throw new NumberFormatException(); }
				Item selected=(Item)costItem.getSelectedItem();
				if(unitCosts.size()>=32 && !unitCosts.containsKey(selected.id)) { message("Use at most 32 unit costs."); return; }
				unitCosts.put(selected.id,gp);
				costSummary.setText(unitCosts.entrySet().stream().map(e->costNames.get(e.getKey())+": "+e.getValue()+" gp").collect(java.util.stream.Collectors.joining("\n"))); invalidateResult();
			}
			catch(NumberFormatException ex) { message("Unit cost: enter whole GP from 0 to 1000000000."); }
		});
		PanelWidgets.button(costBody,"Clear unit costs",()->{ if(valid.getAsBoolean()) { unitCosts.clear(); costSummary.setText(""); invalidateResult(); } }); costBody.add(costSummary);
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
		String[] labels={"Legal attack windows","Attack & cast ready","No active Death Charge","Lightbearer throughout","Usable gear & stock","Runes & legal casts","Distinct eligible deaths","Declared HP & phase","Spell/switch delays included"};
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
		for(JTextField field:List.of(horizon,energy,restore,targetHp,playerHp,cap,opportunities,casts,deaths,targetDefence,targetMagic)) { field.getDocument().addDocumentListener(changed); }
		for(JTextField field:supplyFields.values()) { field.getDocument().addDocumentListener(changed); }
		javax.swing.event.DocumentListener impactChanged=new javax.swing.event.DocumentListener() {
			public void insertUpdate(javax.swing.event.DocumentEvent e) { changed(); }
			public void removeUpdate(javax.swing.event.DocumentEvent e) { changed(); }
			public void changedUpdate(javax.swing.event.DocumentEvent e) { changed(); }
			private void changed() { impactDirty=true; invalidateResult(); }
		};
		for(JTextField field:List.of(impactHp,impactPlayerHp,impactCap,impactDefence,impactMagic)) { field.getDocument().addDocumentListener(impactChanged); }
		impactTick.getDocument().addDocumentListener(changed);
		impactRuby.addActionListener(e->{ impactDirty=true; invalidateResult(); }); impactOverhead.addActionListener(e->{ impactDirty=true; invalidateResult(); });
		impactConfirmed.addActionListener(e->{ impactDirty=true; invalidateResult(); });
		supplyConfirmed.addActionListener(e->invalidateResult());
		zarosFixed.addActionListener(e->invalidateResult()); overhead.addActionListener(e->invalidateResult());
		for(JComboBox<?> combo:List.of(order,kandarin,ruby,policy)) { combo.addActionListener(e->invalidateResult()); }
		conditions.values().forEach(check->check.addActionListener(e->invalidateResult()));
	}
	void attach(BooleanSupplier valid,Consumer<Runnable> execute) { this.valid=valid; this.execute=execute; calculate.setEnabled(valid.getAsBoolean()); }
	void detach() { valid=()->false; }
	NexPlanComparison.Result result() { return result; }
	private void field(String label,JComponent field,String name) { field(body,label,field,name); }
	private void field(JPanel target,String label,JComponent field,String name)
	{
		PanelWidgets.text(target,label); field.setName(name); field.getAccessibleContext().setAccessibleName(label); field.setToolTipText(label); control(field); target.add(field);
		if(field instanceof JComboBox)
		{
			JComboBox<?> combo=(JComboBox<?>)field; PanelWidgets.text(target,""); JTextArea selected=(JTextArea)target.getComponent(target.getComponentCount()-1);
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
	private static String value(Object value) { return value==null?"":value.toString(); }
	private int impactTick()
	{
		int end=number(horizon,"Window length",1,SpecTimeline.MAX_TICKS,true),tick=number(impactTick,"Attack tick",0,end-1,true);
		if(!offsets(opportunities,"Attack ticks",end,true).contains(tick)) { throw new InputError("Attack tick: choose one of the declared attack ticks."); }
		return tick;
	}
	private String impactSummary()
	{
		List<String> rows=new ArrayList<>();
		impacts.forEach((tick,i)->rows.add("Tick "+tick+": target HP "+known(i.targetHp)+", player HP "+known(i.playerHp)+", cap "+known(i.hardCap)+", ruby "+(i.rubyAllowed==null?"unknown":i.rubyAllowed?"yes":"no")
			+("ZAROS".equals(source.choices.get(choice).actual.phase)?", "+impactOverhead.getItemAt(i.zaros.overhead.ordinal())+", Defence "+known(i.zaros.defenceLevel)+", Magic "+known(i.zaros.magicLevel)+", "+(i.zaros.fixedWindow?"confirmed":"unconfirmed"):"")));
		return String.join("\n",rows);
	}
	private static String known(Object value) { return value==null?"unknown":value.toString(); }
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
			List<Integer> attackTicks=offsets(opportunities,"Attack ticks",end,true);
			if(perAttack.isSelected())
			{
				if(impactDirty) { throw new InputError("Save or load this attack's conditions before comparing."); }
				if(!attackTicks.containsAll(impacts.keySet())) { throw new InputError("Saved conditions include removed attack ticks. Restore those ticks or clear attack conditions."); }
			}
			Map<NexSupplies.Kind,List<Integer>> supplyOffsets=new EnumMap<>(NexSupplies.Kind.class);
			for(var entry:supplyFields.entrySet()) { supplyOffsets.put(entry.getKey(),offsets(entry.getValue(),entry.getKey().label+" ticks",end,false)); }
			List<NexSupplies.Use> supplies;
			try { supplies=NexSupplies.ordered(supplyOffsets,end); }
			catch(IllegalArgumentException ex) { throw new InputError(ex.getMessage()); }
			declaration=new NexPlanComparison.Declaration(end,initial,number(restore,"First restore tick",0,25,false),
				order.getSelectedIndex()==0?null:order.getSelectedIndex()==1?SpecTimeline.RestoreOrder.BEFORE_EVENTS:SpecTimeline.RestoreOrder.AFTER_EVENTS,
				perAttack.isSelected()?null:number(targetHp,"Target HP",1,100000,false),perAttack.isSelected()?null:number(playerHp,"Player HP",1,255,false),perAttack.isSelected()?null:number(cap,"Damage cap",1,100000,false),tri(kandarin),perAttack.isSelected()?null:tri(ruby),conditions.values().stream().allMatch(JCheckBox::isSelected) && (supplies.isEmpty() || supplyConfirmed.isSelected()),NexPlanComparison.Policy.values()[policy.getSelectedIndex()],
				attackTicks,offsets(casts,"Death Charge cast ticks",end,false),offsets(deaths,"Eligible death ticks",end,false),edits,quantities,supplies,unitCosts,
				perAttack.isSelected()?NexPlanComparison.Zaros.UNKNOWN:new NexPlanComparison.Zaros(NexTargetState.Overhead.values()[overhead.getSelectedIndex()],number(targetDefence,"Target Defence",0,1000,false),number(targetMagic,"Target Magic",0,1000,false),zarosFixed.isSelected()),
				perAttack.isSelected()?NexPlanComparison.ConditionMode.PER_OPPORTUNITY:NexPlanComparison.ConditionMode.FIXED,perAttack.isSelected()?impacts:Map.of());
		}
		catch(InputError ex) { invalidateResult(); message(ex.getMessage()); return; }
		String selectedConditions="Supply effects confirmed="+supplyConfirmed.isSelected()+"; "+conditions.values().stream().map(check->check.getToolTipText()+"="+check.isSelected()).collect(java.util.stream.Collectors.joining("; "));
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
		PanelWidgets.text(output,NexPlanSupport.decision(result.comparison.actualToOwned));
		PanelWidgets.text(output,"Owned → hypothetical damage: "+gain(result.comparison.ownedToUpgrade.expectedDamage));
		PanelWidgets.text(output,NexPlanSupport.decision(result.comparison.ownedToUpgrade));
		if(!result.routes.isEmpty())
		{
			JPanel routes=PanelWidgets.column(output); routes.setVisible(false);
			PanelWidgets.button(output,"Acquisition routes",()->{ if(!valid.getAsBoolean()) { return; } routes.setVisible(!routes.isVisible()); refresh(); }); output.add(routes);
			for(var route:result.routes)
			{
				PanelWidgets.centeredLabel(routes,route.label,true,0,net.runelite.client.ui.ColorScheme.TEXT_COLOR);
				PanelWidgets.text(routes,route.work==null?"Route unavailable":route.work);
			}
		}
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
		var cost=result.costs.get(e);
		PanelWidgets.text(output,"Ammo, supplies & runes: "+(cost==null?"unavailable":cost.gp==null?"unit costs missing":cost.gp+" gp"));
		var consumed=result.consumedSupplies;
		if(!consumed.isEmpty()) { PanelWidgets.text(output,"Supplies: "+consumed.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry->entry.getKey().label+": "+entry.getValue()).collect(java.util.stream.Collectors.joining(", "))); }
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
