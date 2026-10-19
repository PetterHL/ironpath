package com.ironpath;

import com.ironpath.advisor.NexOwnedFixtures;
import com.ironpath.dev.PanelPreview;
import com.ironpath.dev.ReviewReport;
import com.ironpath.encounter.EncounterSummary.Outcome;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InventoryID;

/** More equipment choices via the existing public-event replay, never injected results. */
final class OfflineVisualScenario
{
	static void run(Path output,ReviewReport report) throws Exception
	{
		report.section("Visual fixture","Same explicit Nex fixture at each font, real public-event/plugin/evaluator pipeline. "
			+"Three captured ranged switches temporarily omit head, neck, or both. Owned carried Masori mask/Anguish and dragon arrows remain known. "
			+"A carried Scorching bow has no ordinary model; supported-domain search exhaustiveness does not establish a global optimum. These are synthetic states, not attributed attacks or global ranks.");
		for(int font:List.of(12,20))
		{
			try(var r=new OfflinePluginReplay(font))
			{
				r.carried.put(29591,1L);
				var summary=r.fight(30,20,60,true,offset->{
					if(offset>=1 && offset<=4)
					{
						r.worn=new LinkedHashMap<>(NexOwnedFixtures.ranged());
						if(offset==1 || offset>=3) { r.worn.put("head",0); }
						if(offset>=2) { r.worn.put("neck",0); }
						r.plugin.onItemContainerChanged(new ItemContainerChanged(InventoryID.WORN,r.client.getItemContainer(InventoryID.WORN)));
					}
				}); r.settle();
				report.check("Public completion "+font,Outcome.COMPLETE,summary.outcome);
				report.near("Independent time: 30 × 0.6 "+font,18,summary.seconds(),0);
				report.near("Independent share: 20 / 80 "+font,25,summary.share(),0);
				var cards=NexPresentation.cards(r.plugin.ownedResult);
				report.check("Further complete-package choices exist "+font,true,cards.size()>2);
				report.check("At least one multi-item package "+font,true,cards.stream().anyMatch(c->NexPresentation.changedItems(c.pair).size()>1));
				report.check("Unsupported owned weapon remains an explicit coverage gap "+font,true,cards.stream().anyMatch(c->c.choice.coverage.stream().anyMatch(gap->gap.contains("not modelled"))));
				SwingUtilities.invokeAndWait(()->{ try {
					PanelPreview.capture(r.panel,output.resolve("main-"+font+".png"),225,report);
					concise(r,"Encounter",font,report);
					for(var card:cards)
					{
						var metric=NexScenarioPanelTest.named(r.panel,"nex-card-metric-"+card.choiceIndex,JTextArea.class);
						if(!PanelPreview.visible(metric)) { continue; }
						var surface=NexScenarioPanelTest.named(r.panel,"nex-equipment-card-"+card.choiceIndex,JPanel.class);
						var bounds=SwingUtilities.convertRectangle(metric,new java.awt.Rectangle(0,0,metric.getWidth(),metric.getHeight()),surface);
						report.check("Centred card metric "+card.choiceIndex+" / "+font,true,Math.abs(bounds.getCenterX()-surface.getWidth()/2.0)<=2);
					}
					PanelPreview.viewport(r.panel,output.resolve("encounter-viewport-"+font+".png"),225,600,null,List.of("nex-nav-encounter","nex-nav-diagnostics","nex-duration","nex-edit-context"),report);
					PanelPreview.click(r.panel,"More comparisons ("+(cards.size()-2)+")");
					PanelPreview.capture(r.panel,output.resolve("expanded-"+font+".png"),225,report);
					int third=cards.get(2).choiceIndex;
					PanelPreview.viewport(r.panel,output.resolve("expanded-viewport-"+font+".png"),225,600,"nex-equipment-card-"+third,List.of("nex-card-metric-"+third,"nex-card-scope-"+third),report);
					PanelPreview.click(r.panel,"Edit team & method");
					preview(r,output,"team",font,report);
					PanelPreview.click(r.panel,"Back to selected view");
					PanelPreview.click(r.panel,"Gear & method"); preview(r,output,"gear",font,report);
					PanelPreview.click(r.panel,"Gear & unlocks"); preview(r,output,"unlocks",font,report);
					PanelPreview.click(r.panel,"Encounter");
					PanelPreview.click(r.panel,"Diagnostics"); preview(r,output,"diagnostics",font,report);
					PanelPreview.click(r.panel,"Calculation details");
					PanelPreview.capture(r.panel,output.resolve("diagnostics-expanded-"+font+".png"),225,report);
				} catch(Exception ex) { throw new IllegalStateException(ex); } });
				r.carried.put(995,1L);
				r.plugin.onItemContainerChanged(new ItemContainerChanged(InventoryID.INV,r.client.getItemContainer(InventoryID.INV)));
				r.tick(); r.settle();
				SwingUtilities.invokeAndWait(()->{ try {
					report.check("Real inventory refresh preserves expanded Diagnostics "+font,true,PanelPreview.text(r.panel).contains("State coverage"));
					PanelPreview.capture(r.panel,output.resolve("diagnostics-refreshed-"+font+".png"),225,report);
					PanelPreview.viewport(r.panel,output.resolve("diagnostics-details-viewport-"+font+".png"),225,600,"nex-calculation-details",List.of(),report);
				} catch(Exception ex) { throw new IllegalStateException(ex); } });
			}
		}
	}
	private static void preview(OfflinePluginReplay r,Path output,String name,int font,ReviewReport report) throws Exception
	{
		PanelPreview.capture(r.panel,output.resolve(name+"-"+font+".png"),225,report);
		if(!name.equals("diagnostics")) { concise(r,name,font,report); }
		PanelPreview.viewport(r.panel,output.resolve(name+"-viewport-"+font+".png"),225,600,null,List.of("nex-nav-encounter","nex-nav-diagnostics"),report);
	}
	private static void concise(OfflinePluginReplay r,String screen,int font,ReviewReport report)
	{
		String shown=PanelPreview.text(r.panel);
		for(String prose:List.of("supplies assumed","Healing and survival","Independent state comparisons","Pilot planning scope","Planning locks apply","preferences loaded locally","preferences saved locally","Filter by role","confirm what you know"))
		{ report.check(screen+" has no help prose: "+prose+" / "+font,false,shown.toLowerCase(java.util.Locale.ROOT).contains(prose.toLowerCase(java.util.Locale.ROOT))); }
	}
}
