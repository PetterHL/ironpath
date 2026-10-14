package com.ironpath;

import com.ironpath.advisor.*;
import com.ironpath.encounter.*;
import java.awt.Dimension;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import javax.swing.*;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;

/** Small post-kill surface, kept apart from homepage layout for independent UX integration. */
final class RecentBossPanel extends JPanel
{
	RecentBossPanel(KillImpactResult result, KillImpactResult.Option selected, ItemManager items,
		KnowledgeCatalog catalog, java.awt.Font font, BooleanSupplier current, BiConsumer<KillImpactResult.Option,Boolean> open)
	{
		setFont(font); setLayout(new BoxLayout(this,BoxLayout.Y_AXIS)); setOpaque(false); setAlignmentX(LEFT_ALIGNMENT);
		setName("recent-boss-context");
		EncounterSummary summary=result.summary;
		line(this,"Recent boss · Commander Zilyana");
		line(this,summary.durationLabel()+" · partial timer");
		line(this,"Ordinary offence · unreduced target");
		if(result.selected!=null)
		{
			int captured=summary.segments.stream().mapToInt(EncounterSummary.Segment::ticks).sum();
			line(this,"Worn state · "+result.selected.ticks()+" / "+captured+" captured ticks");
		}
		if(selected!=null) { details(result,selected,catalog,current); return; }
		if(!result.gaps.isEmpty() && result.options.stream().allMatch(o->o.comparison==null))
		{ line(this,result.gaps.get(result.gaps.size()-1)); }
		if(result.options.isEmpty()) { line(this,"No supported upgrade comparison established for this captured setup."); }
		for(KillImpactResult.Option option:result.options)
		{
			JPanel card=new JPanel(); card.setFont(font); card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS)); card.setAlignmentX(LEFT_ALIGNMENT);
			card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),BorderFactory.createEmptyBorder(6,6,6,6)));
			card.setName("recent-option-"+option.itemId);
			JPanel header=new JPanel(new CardHeaderLayout()); header.setFont(font); header.setOpaque(false); header.setAlignmentX(LEFT_ALIGNMENT);
			JLabel icon=new JLabel("◇") { @Override public void setIcon(javax.swing.Icon image) { if(current.getAsBoolean()) { super.setIcon(image); setText(image==null ? "◇" : ""); } } }; icon.setPreferredSize(new Dimension(40,40));
			icon.getAccessibleContext().setAccessibleName(option.title+" item image");
			if(items!=null) { items.getImage(option.itemId).addTo(icon); }
			header.add(icon); line(header,option.title); header.getComponent(1).setName("card-title"); card.add(header);
			line(card,option.reason);
			line(card,metric(option.comparison));
			line(card,option.kind+(option.upgrade==null ? "" : " · "+option.upgrade.pursuit.category.label));
			JPanel actions=new ActionLinks(); actions.setFont(font); actions.setOpaque(false); actions.setAlignmentX(LEFT_ALIGNMENT);
			JButton value=IronpathPanel.button(actions,"Value",()->{ if(current.getAsBoolean()) { open.accept(option,false); } });
			value.setName("recent-value-"+option.itemId);
			if(option.upgrade!=null)
			{
				JButton path=IronpathPanel.button(actions,"Path",()->{ if(current.getAsBoolean()) { open.accept(option,true); } });
				path.setName("recent-path-"+option.itemId);
			}
			card.add(actions); add(card);
		}
	}
	static String metric(ComparisonResult comparison)
	{
		if(comparison==null || comparison.before==null) { return "Ordinary offence unavailable · inspect model gap"; }
		if(!comparison.available(ComparisonResult.Metric.THEORETICAL_DPS)) { return "DPS unavailable · "+comparison.reason; }
		double gain=comparison.dpsGain();
		return String.format(Locale.ROOT,"Ordinary DPS %.2f → %.2f (%+.2f)%s",comparison.before.dps,comparison.after.dps,gain,
			gain<=0 ? " · no offensive gain" : "");
	}
	private void details(KillImpactResult result,KillImpactResult.Option option,KnowledgeCatalog catalog,BooleanSupplier current)
	{
		line(this,option.title+" · "+option.kind); line(this,option.reason); line(this,metric(option.comparison));
		ComparisonResult c=option.comparison;
		if(c==null || c.before==null) { if(c!=null) { line(this,c.reason); } return; }
		line(this,"Max hit "+c.before.maxHit+" → "+c.after.maxHit);
		if(c.available(ComparisonResult.Metric.HIT_CHANCE))
		{ line(this,String.format(Locale.ROOT,"Hit chance %.1f%% → %.1f%%",100*c.before.hitChance,100*c.after.hitChance)); }
		line(this,"Attack interval "+c.before.attackTicks+" → "+c.after.attackTicks+" ticks");
		line(this,"Actually worn: "+packageLabel(c.beforeSetup,catalog));
		line(this,"Candidate package: "+packageLabel(c.afterSetup,catalog));
		line(this,"Each candidate is independent against the same worn state; gains cannot be added.");
		line(this,"Engagement is measured from observed interaction to death, not a game timer or full kill duration. Numbers cover only the selected state; other states are not averaged. No time savings are estimated.");
		JPanel evidence=new JPanel(); evidence.setLayout(new BoxLayout(evidence,BoxLayout.Y_AXIS)); evidence.setOpaque(false);
		evidence.setVisible(false); evidence.setFont(getFont()); IronpathPanel.button(this,"Inputs and sources",()->{ if(current.getAsBoolean()) { evidence.setVisible(!evidence.isVisible()); revalidate(); } });
		line(evidence,"Adapter "+result.summary.adapterVersion+" · model "+c.modelVersion+" · knowledge "+c.knowledgeVersion);
        long numeric=result.options.stream().filter(o->o.comparison!=null && o.comparison.before!=null
            && o.comparison.available(ComparisonResult.Metric.THEORETICAL_DPS)).count();
        line(evidence,"Supported ordinary-DPS scenarios: "+numeric+" / "+result.options.size()+" shown options. Not an eligible-kill capture rate.");
        result.gaps.forEach(g->line(evidence,g));
		c.assumptions.forEach(a->line(evidence,a));
		java.util.Set<String> sources=new java.util.TreeSet<>(catalog.ranged.sources); sources.addAll(c.target.sources);
		for(CombatSetup setup:List.of(c.beforeSetup,c.afterSetup))
		{
			catalog.ranged.items.stream().filter(i->setup.slots.containsValue(i.id) || setup.loadedAmmo==i.id)
				.forEach(i->sources.addAll(i.sources));
		}
		for(String id:sources)
		{
			KnowledgeCatalog.Source source=catalog.sources.get(id);
			JButton link=IronpathPanel.button(evidence,"Source: "+id,()->{
				if(current.getAsBoolean()) { net.runelite.client.util.LinkBrowser.browse(source.url); }
			}); link.setToolTipText(source.revision);
		}
		add(evidence);
	}
	private String packageLabel(CombatSetup setup,KnowledgeCatalog catalog)
	{
		return setup.slots.entrySet().stream().map(e->e.getKey()+"="+(e.getValue()==0 ? "observed empty" :
			catalog.ranged.items.stream().filter(i->i.id==e.getValue()).map(i->i.label).findFirst().orElse("unreviewed item "+e.getValue())))
			.collect(java.util.stream.Collectors.joining("; "));
	}
	private static void line(JPanel parent,String value)
	{
		IronpathPanel.text(parent,value);
		parent.getComponent(parent.getComponentCount()-1).setFocusable(false);
	}
}
