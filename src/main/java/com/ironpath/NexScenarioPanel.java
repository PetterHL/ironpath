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
		policy=new JComboBox<>(new String[]{"Ordinary","First special","Funded specials"}),
		slot=new JComboBox<>(PoweredMagic.SLOTS.toArray(new String[0]));
	private final JComboBox<Item> item=new JComboBox<>();
	private final JCheckBox legal=new JCheckBox("Confirm plan");
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
		PanelWidgets.button(this,"Conditional N3 package plan",()->{ if(valid.getAsBoolean()) { body.setVisible(!body.isVisible()); refresh(); } }); add(body);
		PanelWidgets.text(body,"Compare captured equipment, the selected owned package and a hypothetical upgrade under the same declared plan. Nothing here identifies attacks from worn time or energy changes.");
		PanelWidgets.text(body,"This optional plan stays with this inspected calculation. It does not change saved Setup, kill observations or future kills.");
		var sample=source.choices.get(choice).actual;
		if(sample.state.specialEnergy!=null && sample.state.specialEnergy>=0 && sample.state.specialEnergy<=1000)
		{ energy.setText(BigDecimal.valueOf(sample.state.specialEnergy,1).toPlainString()); }
		Integer hp=sample.state.visibleLevels.get("HITPOINTS"); if(hp!=null && hp>0) { playerHp.setText(hp.toString()); }
		PanelWidgets.text(body,"Energy/player HP start from this state's readings when available. Reusing them for the hypothetical window is an assumption. Blank inputs stay unknown.");
		field("Horizon in ticks (end excluded)",horizon,"n3-horizon"); field("Starting special energy (%)",energy,"n3-energy");
		field("First natural restore offset (0–25)",restore,"n3-restore"); field("Natural restore order",order,"n3-order");
		field("Target HP at each opportunity",targetHp,"n3-target-hp"); field("Player HP at each opportunity",playerHp,"n3-player-hp");
		field("Hard damage cap per hit",cap,"n3-cap"); field("Hard Kandarin effect",kandarin,"n3-kandarin"); field("Ruby effect allowed",ruby,"n3-ruby");
		field("Attack policy",policy,"n3-policy");
		PanelWidgets.text(body,"First special uses a supported VW/ZCB special at the first supplied opportunity; funded specials attempt one at every opportunity. An ordinary fallback follows each rejected special on the same tick. Other weapons use ordinary attacks.");
		field("Legal attack offsets, comma separated",opportunities,"n3-opportunities");
		field("Death Charge cast offsets (blank: none)",casts,"n3-casts"); field("Distinct eligible death offsets (blank: none)",deaths,"n3-deaths");
		PanelWidgets.text(body,"Offsets are relative to this hypothetical window, not the observed kill. Equal-tick order is cast → eligible death → special → ordinary fallback; natural restoration uses your chosen order. Active retrieval is excluded: every accepted projectile is spent; charged-quiver shots spend a splinter while stock lasts.");
		PanelWidgets.text(body,"Optional hypothetical package edits. Add all needed changes, including compatible ammo and an empty offhand for a two-handed weapon. The complete package is validated again; locks and carried supplies still apply.");
		field("Change slot",slot,"n3-upgrade-slot"); field("Exact item",item,"n3-upgrade-item"); field("New usable quantity",quantity,"n3-upgrade-quantity");
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
		PanelWidgets.text(body,"Confirm legal target access at the supplied offsets, attack-ready/no active Death Charge/cast-ready at start, and fixed Lightbearer. Any listed casts have runes and legal spell opportunities; listed deaths are distinct and eligible. Sampled stock is assumed usable. Attack offsets already account for spell/switch delays. Constant HP and the listed phase/window are scenario assumptions. Unknowns still block dependent results.");
		legal.setName("n3-legal"); control(legal); body.add(legal);
		calculate=PanelWidgets.button(body,"Evaluate plan",this::calculate); calculate.setName("n3-evaluate");
		PanelWidgets.text(body,"Bank-only stock/packing, target transforms, soft caps, healing and survival are not established by this editor. Unsupported outputs retain explicit gaps.");
		body.add(output);
		javax.swing.event.DocumentListener changed=new javax.swing.event.DocumentListener() {
			public void insertUpdate(javax.swing.event.DocumentEvent e) { invalidateResult(); }
			public void removeUpdate(javax.swing.event.DocumentEvent e) { invalidateResult(); }
			public void changedUpdate(javax.swing.event.DocumentEvent e) { invalidateResult(); }
		};
		for(JTextField field:List.of(horizon,energy,restore,targetHp,playerHp,cap,opportunities,casts,deaths)) { field.getDocument().addDocumentListener(changed); }
		for(JComboBox<?> combo:List.of(order,kandarin,ruby,policy)) { combo.addActionListener(e->invalidateResult()); }
		legal.addActionListener(e->invalidateResult());
	}
	void attach(BooleanSupplier valid,Consumer<Runnable> execute) { this.valid=valid; this.execute=execute; calculate.setEnabled(valid.getAsBoolean()); }
	void detach() { valid=()->false; }
	NexPlanComparison.Result result() { return result; }
	private void field(String label,JComponent field,String name)
	{ PanelWidgets.text(body,label); field.setName(name); control(field); body.add(field); }
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
	private void message(String text) { output.removeAll(); PanelWidgets.text(output,text); refresh(); }
	private void calculate()
	{
		if(!valid.getAsBoolean()) { return; }
		NexPlanComparison.Declaration declaration;
		try
		{
			Integer initial=energy.getText().isBlank()?null:new BigDecimal(energy.getText().trim()).movePointRight(1).intValueExact();
			declaration=new NexPlanComparison.Declaration(Integer.parseInt(horizon.getText().trim()),initial,number(restore),
				order.getSelectedIndex()==0?null:order.getSelectedIndex()==1?SpecTimeline.RestoreOrder.BEFORE_EVENTS:SpecTimeline.RestoreOrder.AFTER_EVENTS,
				number(targetHp),number(playerHp),number(cap),tri(kandarin),tri(ruby),legal.isSelected(),NexPlanComparison.Policy.values()[policy.getSelectedIndex()],
				offsets(opportunities),offsets(casts),offsets(deaths),edits,quantities);
		}
		catch(IllegalArgumentException | ArithmeticException ex) { invalidateResult(); message("Check plan inputs. Use whole tick offsets in increasing order; energy supports tenths of a percent. Supply 1–128 attack offsets and a 1–3000 tick horizon."); return; }
		long ticket=++request; result=null; report=null; message("Evaluating this declared plan…"); calculate.setEnabled(false);
		try { execute.accept(()->{
			NexPlanComparison.Result evaluated=null; String formatted=null;
			try { evaluated=new NexPlanComparison(catalog).evaluate(source,choice,pair,declaration); formatted="Evidence: "+provenance+"\n"+evaluated.report(); }
			catch(IllegalArgumentException ex) { /* Bounded invalid/incompatible inputs are presented without raw exception data. */ }
			var finished=evaluated; var text=formatted;
			SwingUtilities.invokeLater(()->{
				if(ticket!=request || !valid.getAsBoolean()) { if(ticket==request) { calculate.setEnabled(true); } return; }
				calculate.setEnabled(true); result=finished; report=text; output.removeAll();
				if(finished==null) { message("This complete package or plan is incompatible. Recheck exact items, stock, locks and required inputs."); return; }
				show(finished); refresh();
			});
		}); }
		catch(java.util.concurrent.RejectedExecutionException ex) { calculate.setEnabled(true); message("The calculation worker is unavailable. Try again after the session is ready."); }
	}
	private void show(NexPlanComparison.Result result)
	{
		PanelWidgets.text(output,"Conditional plan results · frozen calculation "+source.revision);
		show("Captured equipment under declared attacks",result.comparison.actualToOwned.before);
		show("Selected owned package",result.comparison.actualToOwned.after);
		show("Owned plus hypothetical edits",result.comparison.ownedToUpgrade.after);
		PanelWidgets.text(output,"Captured-to-owned expected launch damage change: "+gain(result.comparison.actualToOwned.expectedDamage));
		PanelWidgets.text(output,"Owned-to-upgrade expected launch damage change: "+gain(result.comparison.ownedToUpgrade.expectedDamage));
		PanelWidgets.text(output,"These are conditional launch expectations at supplied HP, not a kill forecast, automatic best-spec recommendation or time saving.");
		PanelWidgets.text(output,"The report below is selectable if copying is unavailable.");
		JTextArea feedback=(JTextArea)output.getComponent(output.getComponentCount()-1);
		JButton copy=PanelWidgets.button(output,"Copy plan report",()->{
			if(!valid.getAsBoolean() || report==null) { return; }
			try { clipboard.copy(report); feedback.setText("Scenario report copied."); }
			catch(RuntimeException ex) { feedback.setText("Clipboard unavailable. Select and copy the report below."); }
			refresh();
		});
		copy.setName("n3-copy-report");
		JTextArea text=new JTextArea(report); text.setName("n3-report"); text.setFont(getFont()); text.setEditable(false); text.setLineWrap(true); text.setWrapStyleWord(true);
		JScrollPane scroll=new JScrollPane(text); scroll.setName("n3-report-scroll"); scroll.setAlignmentX(0); scroll.setPreferredSize(new Dimension(200,160)); scroll.setMaximumSize(new Dimension(net.runelite.client.ui.PluginPanel.PANEL_WIDTH-12,160)); output.add(scroll);
	}
	private void show(String label,NexPackageComparison.Evaluation e)
	{
		PanelWidgets.text(output,label);
		for(String gap:e.resourceGaps) { PanelWidgets.text(output,gap); }
		if(e.plan==null) { PanelWidgets.text(output,"Finite resource result unavailable. Dependent total damage is withheld."); return; }
		var p=e.plan; var r=p.resources;
		PanelWidgets.text(output,"Expected launch damage: "+metric(p.expectedDamage)+" · self damage: "+metric(p.expectedSelfDamage));
		PanelWidgets.text(output,"Accepted ordinary / Voidwaker / ZCB: "+r.ordinaryAttacks+" / "+r.voidwakerSpecs+" / "+r.zaryteSpecs
			+" · Death Charge procs: "+r.deathChargeProcs);
		PanelWidgets.text(output,"Energy spent / restored / lost at cap / final (%): "+r.spent/10.0+" / "+r.restored/10.0+" / "+r.wasted/10.0+" / "+r.finalEnergy/10.0);
		PanelWidgets.text(output,"Selected ammo spent: "+new TreeMap<>(p.spentAmmo)+" · splinters spent: "+p.spentSplinters+". No-retrieval resource branch.");
		for(String gap:p.gaps) { PanelWidgets.text(output,gap); }
	}
	private static String metric(Double value) { return value==null?"unavailable":String.format(Locale.ROOT,"%.2f",value); }
	private static String gain(Double value) { return value==null?"unavailable":String.format(Locale.ROOT,"%+.2f",value); }
	private static Integer number(JTextField field) { return field.getText().isBlank()?null:Integer.valueOf(field.getText().trim()); }
	private static Boolean tri(JComboBox<?> combo) { return combo.getSelectedIndex()==0?null:combo.getSelectedIndex()==2; }
	private static List<Integer> offsets(JTextField field)
	{
		if(field.getText().isBlank()) { return List.of(); }
		String[] parts=field.getText().trim().split(",",-1); if(parts.length>128) { throw new IllegalArgumentException(); }
		List<Integer> result=new ArrayList<>(); for(String part:parts) { result.add(Integer.valueOf(part.trim())); } return result;
	}
}
