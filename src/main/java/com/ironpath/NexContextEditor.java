package com.ironpath;

import com.ironpath.nex.NexContext;
import java.awt.Dimension;
import java.util.Objects;
import java.util.function.BiConsumer;
import javax.swing.*;
import static com.ironpath.nex.NexContext.*;

/** Optional native editor. Keeps an unfinished draft attached across unrelated refreshes. */
final class NexContextEditor extends JPanel
{
	final long key, revision;
	final NexContext initial;
	private final JComboBox<Strategy> strategy=new JComboBox<>(Strategy.values());
	private final JComboBox<Group> group=new JComboBox<>(Group.values());
	private final JComboBox<Participation> participation=new JComboBox<>(Participation.values());
	private final JTextField count=new JTextField();
	private final JCheckBox strategyConfirmed=new JCheckBox("Confirm strategy"), groupConfirmed=new JCheckBox("Confirm group"), countConfirmed=new JCheckBox("Confirm count"), remember=new JCheckBox("Use for future");
	private BiConsumer<NexContext,Boolean> apply;
	private final JTextArea error;
	private final JButton applyButton;
	NexContextEditor(long key,long revision,NexContext initial,java.awt.Font font)
	{
		this.key=key; this.revision=revision; this.initial=initial;
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS)); setOpaque(false); setAlignmentX(LEFT_ALIGNMENT); setFont(font);
		for(JComboBox<?> field:java.util.List.of(strategy,group,participation))
		{
			field.setRenderer(new DefaultListCellRenderer() {
				@Override public java.awt.Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus)
				{
					String label=value==null?"":value.toString();
					if(!label.equals("MP2") && !label.equals("RP2") && !label.isEmpty()) { label=label.charAt(0)+label.substring(1).toLowerCase(java.util.Locale.ROOT); }
					return super.getListCellRendererComponent(list,label,index,selected,focus);
				}
			});
		}
		strategy.setName("nex-context-strategy"); group.setName("nex-context-group"); participation.setName("nex-context-participation"); count.setName("nex-context-count");
		strategy.setSelectedItem(initial.strategy); group.setSelectedItem(initial.group); participation.setSelectedItem(initial.participation);
		count.setText(initial.participants==null?"":initial.participants.toString());
		strategyConfirmed.setName("nex-context-confirm-strategy"); groupConfirmed.setName("nex-context-confirm-group"); countConfirmed.setName("nex-context-confirm-count"); remember.setName("nex-context-remember");
		strategyConfirmed.setSelected(initial.strategySource==Source.ANNOTATED); groupConfirmed.setSelected(initial.groupSource==Source.ANNOTATED); countConfirmed.setSelected(initial.countSource==Source.ANNOTATED);
		label("P2 method"); control(strategy); if(key>=0) { control(strategyConfirmed); }
		label("Team type"); control(group); if(key>=0) { control(groupConfirmed); }
		label("Team size"); control(participation); label("Players, including you"); control(count); if(key>=0) { control(countConfirmed); }
		Runnable eligibility=()->{ count.setEnabled(participation.getSelectedItem()==Participation.EXACT); };
		participation.addActionListener(e->eligibility.run()); eligibility.run();
		if(key>=0) { control(remember); }
		PanelWidgets.text(this,""); error=(JTextArea)getComponent(getComponentCount()-1); error.setName("nex-context-error");
		applyButton=PanelWidgets.button(this,key<0?"Save defaults":"Apply to this kill",()->{}); applyButton.setName("nex-context-apply");
		applyButton.addActionListener(e->{
			try
			{
				error.setText("");
				Participation p=(Participation)participation.getSelectedItem(); Integer n=null;
				if(p==Participation.EXACT)
				{
					String value=count.getText().trim();
					if(!value.matches("[0-9]{1,10}")) { throw new IllegalArgumentException("Enter a positive whole count including you."); }
					try { n=Integer.valueOf(value); } catch(NumberFormatException ex) { throw new IllegalArgumentException("Count is too large."); }
				}
				Strategy s=(Strategy)strategy.getSelectedItem(); Group g=(Group)group.getSelectedItem();
				NexContext next=new NexContext(s,g,p,n,source(strategyConfirmed,s==initial.strategy,initial.strategySource),
					source(groupConfirmed,g==initial.group,initial.groupSource),source(countConfirmed,p==initial.participation && Objects.equals(n,initial.participants),initial.countSource));
				if(apply!=null) { apply.accept(next,key<0 || remember.isSelected()); }
			}
			catch(IllegalArgumentException ex) { error.setText(ex.getMessage()); revalidate(); repaint(); }
		});
	}
	void bind(BiConsumer<NexContext,Boolean> apply,boolean canRemember)
	{
		this.apply=apply; remember.setEnabled(canRemember); if(!canRemember) { remember.setSelected(false); }
		applyButton.setEnabled(key>=0 || canRemember);
	}
	private Source source(JCheckBox confirmed,boolean unchanged,Source previous)
	{ return key<0?Source.INHERITED:confirmed.isSelected()?Source.ANNOTATED:unchanged && previous==Source.INHERITED?Source.INHERITED:Source.UNCONFIRMED; }
	private void label(String text)
	{
		JTextArea label=PanelWidgets.label(this,text,true,0,net.runelite.client.ui.ColorScheme.TEXT_COLOR);
		label.setBorder(BorderFactory.createEmptyBorder(8,0,3,0));
	}
	private void control(JComponent component)
	{
		component.setFont(getFont()); component.setAlignmentX(LEFT_ALIGNMENT);
		if(component instanceof JCheckBox) { PanelWidgets.checkBox((JCheckBox)component,getFont()); }
		component.setMaximumSize(new Dimension(net.runelite.client.ui.PluginPanel.PANEL_WIDTH-12,Math.max(28,component.getPreferredSize().height)));
		if(!(component instanceof JCheckBox))
		{
			String name=component==strategy?"P2 method":component==group?"Team type":component==participation?"Team size":"Players, including you";
			component.getAccessibleContext().setAccessibleName(name);
		}
		add(component);
	}
}
