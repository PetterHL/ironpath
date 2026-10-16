package com.ironpath;

import com.ironpath.nex.NexResourceReview;
import com.ironpath.nex.SpecTimeline;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import javax.swing.*;

/** Frozen post-kill resource evidence, without attributing sampled deltas to attacks. */
final class NexResourcesPanel
{
	static void populate(JPanel panel,NexResourceReview review,BooleanSupplier valid,Runnable relayout)
	{
		PanelWidgets.text(panel,"Special-attack resources · kill #"+review.summaryKey);
		PanelWidgets.text(panel,"First recorded energy "+energy(review.firstEnergy)+" · last recorded "+energy(review.lastEnergy));
		PanelWidgets.text(panel,"Sampled energy changes: +"+energy(review.sampledIncreases)+" / −"+energy(review.sampledDecreases)
			+". These are net changes between readings, not total restoration or spending.");
		PanelWidgets.text(panel,review.fixedLightbearerObserved?"Lightbearer appears in every recorded fight state. The restoration phase is unobserved."
			:"Continuous Lightbearer wear is unestablished. Ring switches and unread states need separate timing support.");
		PanelWidgets.text(panel,review.improvedActiveObserved?"Two active Death Charge charges were observed, supporting improved active use."
			:"Improved Death Charge use is unestablished by these readings. One or zero charges cannot establish upgrade absence.");
		PanelWidgets.text(panel,"Energy changes can combine spending and restoration. They do not identify spec weapons, attack counts, casts or procs. Charge decreases may include expiry; a full energy reading does not measure wasted restoration.");
		for(String gap:review.gaps) { PanelWidgets.text(panel,gap); }
		JPanel trace=new JPanel(); trace.setFont(panel.getFont()); trace.setOpaque(false);
		trace.setLayout(new BoxLayout(trace,BoxLayout.Y_AXIS)); trace.setAlignmentX(0); trace.setVisible(false);
		PanelWidgets.button(panel,"Resource readings and rules",()->{
			if(valid.getAsBoolean()) { trace.setVisible(!trace.isVisible()); relayout.run(); }
		});
		if(!review.points.isEmpty())
		{
			JComboBox<String> readings=new JComboBox<>(); readings.setFont(panel.getFont()); readings.setName("nex-resource-reading");
			for(int i=0;i<review.points.size();i++) { readings.addItem("Reading "+(i+1)+" · tick "+review.points.get(i).tick); }
			readings.setAlignmentX(0); readings.setMaximumSize(new java.awt.Dimension(net.runelite.client.ui.PluginPanel.PANEL_WIDTH-12,Math.max(28,readings.getPreferredSize().height)));
			trace.add(readings); PanelWidgets.text(trace,reading(review.points.get(0)));
			JTextArea detail=(JTextArea)trace.getComponent(trace.getComponentCount()-1);
			readings.addActionListener(event->{
				if(valid.getAsBoolean()) { detail.setText(reading(review.points.get(readings.getSelectedIndex()))); relayout.run(); }
			});
		}
		PanelWidgets.text(trace,"Reference energy costs: Voidwaker "+energy(SpecTimeline.Attack.VOIDWAKER.cost)
			+"; Zaryte crossbow "+energy(SpecTimeline.Attack.ZARYTE_CROSSBOW.cost)+". These are not measured uses in this kill.");
		PanelWidgets.text(trace,"Fixed-ring natural restoration is 10% per "+SpecTimeline.Ring.STANDARD.interval+" ticks, or "
			+SpecTimeline.Ring.LIGHTBEARER.interval+" with Lightbearer. Death Charge can restore 15% per eligible proc, bounded by its active charges and the 100% energy cap. No fixed damage or time uplift follows.");
		PanelWidgets.text(trace,"Rules "+SpecTimeline.VERSION+". These readings do not establish starting conditions, restoration phase/order, eligible deaths, launched attacks or target HP. Automatic spec-upgrade comparisons, supply costs and team time remain unavailable.");
		panel.add(trace);
	}
	private static String reading(NexResourceReview.Point point)
	{
		return "Tick "+point.tick+" · energy "+energy(point.energy)+"\nDeath Charge active "+value(point.activeCharges)
			+" · cooldown "+value(point.cooldown)+"\nRing "+(point.ring==null?"unread":point.ring==25975?"Lightbearer":point.ring==0?"empty":"item "+point.ring);
	}
	private static String value(Integer value) { return value==null?"unread":value.toString(); }
	private static String energy(Integer value)
	{ return value==null?"unread":String.format(Locale.ROOT,"%.1f%%",value/10.0); }
}
