package com.ironpath;

import com.ironpath.dev.PanelPreview;
import com.ironpath.dev.ReviewReport;
import com.ironpath.nex.NexPlanComparison;
import java.nio.file.Path;
import java.util.*;
import javax.swing.*;
import static org.mockito.Mockito.when;

/** DEV1 extension: public-event capture -> frozen N2 -> native declared N3A/B/C plan -> guarded worker. */
final class OfflineN3Scenario
{
	static void run(Path output,ReviewReport report) throws Exception
	{
		report.section("Scope","Synthetic public-event kill, frozen captured melee/owned package, explicit hypothetical Voidwaker. Attack offsets, HP, restore phase, legal access and initial spell state are declared test assumptions, not observations. Uses the same production plugin/evaluator/Swing path as the existing DEV1 scenarios.");
		for(int font:List.of(12,20))
		{
			try(var r=new OfflinePluginReplay(font))
			{
				r.carried.put(385,1L); r.carried.put(3144,1L); r.carried.put(6685,1L); r.carried.put(3024,1L);
				r.fight(30,20,60,true); r.settle(); if(font==12) { OfflineReview.versions(report,r.catalog); }
				final NexScenarioPanel[] panel={null};
				edt(()->{
					PanelPreview.click(r.panel,"Compare with owned gear"); PanelPreview.combo(r.panel,"nex-owned-support-filter").setSelectedItem("All states");
					PanelPreview.click(r.panel,"Spec energy & Death Charge"); PanelPreview.click(r.panel,"Natural-restoration examples");
				});
				report.check("N3A three bounded natural-restoration examples "+font,3,r.plugin.ownedResult.resources.scenarios.examples.size());
				capture(r,output,"natural-examples",font,report);
				edt(()->{
					PanelPreview.click(r.panel,"Spec energy & Death Charge");
					PanelPreview.combo(r.panel,"nex-owned-role-filter").setSelectedItem("melee");
					panel[0]=(NexScenarioPanel)PanelPreview.children(r.panel).stream().filter(c->c instanceof NexScenarioPanel).findFirst().orElseThrow();
					PanelPreview.click(r.panel,"Plan specs & gear"); fill(r.panel);
					JComboBox<?> slot=PanelPreview.combo(r.panel,"n3-upgrade-slot"); slot.setSelectedItem("weapon");
					JComboBox<?> items=PanelPreview.combo(r.panel,"n3-upgrade-item");
					for(int i=0;i<items.getItemCount();i++) if(items.getItemAt(i).toString().endsWith("(27690)")) { items.setSelectedIndex(i); break; }
					PanelPreview.click(r.panel,"Add item");
				});
				capture(r,output,"declared-inputs",font,report);
				edt(()->{ try { PanelPreview.viewport(r.panel,output.resolve("evaluate-viewport-"+font+".png"),225,600,"n3-evaluate",List.of("n3-evaluate"),report); } catch(Exception ex) { throw new IllegalStateException(ex); } });
				edt(()->PanelPreview.click(r.panel,"Compare this plan"));
				report.check("N3 uses plugin worker at "+font,true,r.workers.size()>0); r.settle();
				final NexPlanComparison.Result[] finished={null};
				edt(()->{
					finished[0]=panel[0].result(); report.check("N3 result published "+font,true,finished[0]!=null);
					if(finished[0]==null) { throw new IllegalStateException("No N3 result"); }
					var c=finished[0].comparison;
					report.check("Captured finite plan available "+font,true,c.actualToOwned.before.plan!=null);
					report.check("Owned finite plan available "+font,true,c.actualToOwned.after.plan!=null);
					report.check("Hypothetical VW finite plan available "+font,true,c.ownedToUpgrade.after.plan!=null);
					if(c.ownedToUpgrade.after.plan!=null)
					{ report.check("One explicitly planned VW special "+font,1,c.ownedToUpgrade.after.plan.resources.voidwakerSpecs); }
					report.check("Separate marginal damage available "+font,true,c.actualToOwned.expectedDamage!=null && c.ownedToUpgrade.expectedDamage!=null);
					PanelPreview.click(r.panel,"Copy plan report");
					report.check("Clipboard fallback is visible "+font,true,PanelPreview.text(r.panel).contains("Clipboard unavailable"));
					String text=NexScenarioPanelTest.named(r.panel,"n3-report",JTextArea.class).getText();
					report.check("Plan report preserves synthetic provenance "+font,true,text.startsWith("Evidence: SYNTHETIC_OFFLINE"));
					report.check("Plan report exports captured and modeled context "+font,true,text.contains("Captured context: target=NEX; phase=SHADOW; window=FEAR_THE_SHADOW") && text.contains("Modeled context: target=NEX; phase=SHADOW; window=FEAR_THE_SHADOW"));
					report.check("Plan report exports frozen levels/prayers/style "+font,true,text.contains("real levels=") && text.contains("visible levels=") && text.contains("active prayer flags=[PIETY]; style button=1") && text.contains("family/prayer/style=MELEE / PIETY /"));
					try { java.nio.file.Files.writeString(output.resolve("plan-report-"+font+".txt"),text); }
					catch(java.io.IOException ex) { throw new IllegalStateException(ex); }
				});
				capture(r,output,"conditional-results",font,report);
				edt(()->{ try { PanelPreview.viewport(r.panel,output.resolve("report-viewport-"+font+".png"),225,600,"n3-copy-report",List.of("n3-copy-report","n3-report-scroll"),report); } catch(Exception ex) { throw new IllegalStateException(ex); } });
				edt(()->{
					PanelPreview.click(r.panel,"Food & potions");
					for(var entry:Map.of("SHARK","1","KARAMBWAN","1","BREW","0","RESTORE","3").entrySet())
					{ NexScenarioPanelTest.named(r.panel,"n3-supply-"+entry.getKey(),JTextField.class).setText(entry.getValue()); }
					PanelPreview.click(r.panel,"Compare this plan");
				}); r.settle();
				edt(()->{
					report.check("Undeclared supply readiness blocks plan "+font,null,panel[0].result().comparison.actualToOwned.before.plan);
					NexScenarioPanelTest.named(r.panel,"n3-supplies-confirmed",JCheckBox.class).doClick();
				});
				capture(r,output,"supply-editor",font,report);
				edt(()->{ try { PanelPreview.viewport(r.panel,output.resolve("supply-viewport-"+font+".png"),225,600,"n3-supply-label-SHARK",List.of("n3-supply-SHARK","n3-supply-KARAMBWAN","n3-supply-BREW","n3-supply-RESTORE","n3-supply-SUPER_COMBAT","n3-supply-RANGING","n3-supplies-confirmed"),report); } catch(Exception ex) { throw new IllegalStateException(ex); } });
				edt(()->{
					PanelPreview.click(r.panel,"Unit costs"); JComboBox<?> units=PanelPreview.combo(r.panel,"n3-cost-item");
					for(int id:List.of(385,3144,6691,3030))
					{
						for(int i=0;i<units.getItemCount();i++) if(units.getItemAt(i).toString().endsWith("("+id+")")) { units.setSelectedIndex(i); break; }
						NexScenarioPanelTest.named(r.panel,"n3-cost-value",JTextField.class).setText("100"); PanelPreview.click(r.panel,"Set unit cost");
					}
				});
				capture(r,output,"unit-costs",font,report);
				edt(()->{ try { PanelPreview.viewport(r.panel,output.resolve("cost-viewport-"+font+".png"),225,600,"n3-cost-item",List.of("n3-cost-item","n3-cost-value","n3-cost-summary"),report); } catch(Exception ex) { throw new IllegalStateException(ex); } });
				edt(()->{ PanelPreview.click(r.panel,"Unit costs"); PanelPreview.click(r.panel,"Food & potions"); PanelPreview.click(r.panel,"Compare this plan"); }); r.settle();
				edt(()->{
					var result=panel[0].result(); var evaluation=result.comparison.actualToOwned.before;
					report.check("Frozen carried supplies admit finite plan "+font,true,evaluation.plan!=null);
					if(evaluation.plan==null) { throw new IllegalStateException(evaluation.resourceGaps.toString()); }
					var cost=com.ironpath.nex.NexPlanSupport.cost(evaluation,result.declaration.unitCosts);
					report.check("Four declared supply units cost 400 gp "+font,Long.valueOf(400),cost.gp);
					report.check("Food shifts outstanding cooldown "+font,true,evaluation.plan.resources.ordinaryAttacks<finished[0].comparison.actualToOwned.before.plan.resources.ordinaryAttacks);
					PanelPreview.click(r.panel,"Copy plan report");
					String text=NexScenarioPanelTest.named(r.panel,"n3-report",JTextArea.class).getText();
					report.check("Supply and cost report retains explicit assumptions "+font,true,text.contains("Declared supply actions=[0:BREW, 1:SHARK, 1:KARAMBWAN, 3:RESTORE]") && text.contains("gp=400"));
				});
				capture(r,output,"supply-results",font,report);
				edt(()->{
					NexScenarioPanelTest.named(r.panel,"n3-target-hp",JTextField.class).setText(""); PanelPreview.click(r.panel,"Compare this plan");
				}); r.settle();
				edt(()->{
					var c=panel[0].result().comparison;
					report.check("Missing HP preserves resource plan "+font,true,c.ownedToUpgrade.after.plan!=null);
					report.check("Missing HP withholds damage delta "+font,null,c.ownedToUpgrade.expectedDamage);
				});
				capture(r,output,"unknown-hp",font,report);
				var firstSource=finished[0].source;
				r.fight(35,40,40,true); r.settle();
				edt(()->{
					var same=(NexScenarioPanel)PanelPreview.children(r.panel).stream().filter(c->c instanceof NexScenarioPanel).findFirst().orElseThrow();
					report.check("New kill preserves original plan inputs "+font,true,same==panel[0] && same.source==firstSource);
					NexScenarioPanelTest.named(r.panel,"n3-target-hp",JTextField.class).setText("500"); PanelPreview.click(r.panel,"Compare this plan");
				});
				if(r.workers.size()==0) { throw new IllegalStateException("Expected pending N3 worker"); }
				Runnable stale=r.workers.take(); edt(()->PanelPreview.click(r.panel,"Back to encounter"));
				edt(()->PanelPreview.click(r.panel,"Clear session")); r.callbacks.drain(); OfflinePluginReplay.edt();
				r.runWorker(stale); OfflinePluginReplay.edt();
				edt(()->report.check("Reset cannot resurrect scenario report "+font,false,PanelPreview.text(r.panel).contains("Conditional plan results")));
				capture(r,output,"reset-pending",font,report);
			}
			zaros(output,report,font);
		}
	}
	private static void zaros(Path output,ReviewReport report,int font) throws Exception
	{
		try(var r=new OfflinePluginReplay(font))
		{
			r.carried.put(560,3L); r.carried.put(565,3L); r.carried.put(566,3L);
			r.fight(30,20,60,true,offset->{ if(offset==8) {
				when(r.boss.getId()).thenReturn(net.runelite.api.gameval.NpcID.NEX_SOULSPLIT);
				when(r.boss.getHealthRatio()).thenReturn(9); when(r.boss.getHealthScale()).thenReturn(30);
			} }); r.settle();
			final NexScenarioPanel[] planner={null};
			edt(()->{
				PanelPreview.click(r.panel,"Compare with owned gear"); PanelPreview.combo(r.panel,"nex-owned-support-filter").setSelectedItem("All states");
				int selected=-1;
				for(int i=0;i<r.plugin.ownedResult.choices.size();i++)
				{ var s=r.plugin.ownedResult.choices.get(i).actual; if(s.phase.equals("ZAROS") && s.targetState.overhead()==com.ironpath.nex.NexTargetState.Overhead.SOUL_SPLIT) { selected=i; break; } }
				if(selected<0) { throw new IllegalStateException("No frozen Soul Split state"); }
				PanelPreview.combo(r.panel,"nex-owned-state").setSelectedIndex(selected);
				planner[0]=(NexScenarioPanel)PanelPreview.children(r.panel).stream().filter(c->c instanceof NexScenarioPanel).findFirst().orElseThrow();
				PanelPreview.click(r.panel,"Plan specs & gear"); fill(r.panel);
				report.check("Zaros form seeds overhead without confirmation "+font,"Soul Split",PanelPreview.combo(r.panel,"n3-zaros-overhead").getSelectedItem());
				report.check("Zaros stable impact choice starts unchecked "+font,false,NexScenarioPanelTest.named(r.panel,"n3-zaros-fixed",JCheckBox.class).isSelected());
				NexScenarioPanelTest.named(r.panel,"n3-casts",JTextField.class).setText("0");
				PanelPreview.click(r.panel,"Compare this plan");
			}); r.settle();
			edt(()->{
				report.check("Unknown Zaros stats withhold damage "+font,null,planner[0].result().comparison.actualToOwned.before.plan.expectedDamage);
				report.check("Known rune stock preserves resources "+font,Map.of(560,1,565,1,566,1),planner[0].result().comparison.actualToOwned.before.spentRunes);
				NexScenarioPanelTest.named(r.panel,"n3-zaros-defence",JTextField.class).setText("250");
				NexScenarioPanelTest.named(r.panel,"n3-zaros-fixed",JCheckBox.class).doClick();
			});
			capture(r,output,"zaros-inputs",font,report);
			edt(()->{ try { PanelPreview.viewport(r.panel,output.resolve("zaros-viewport-"+font+".png"),225,600,"n3-zaros-overhead",List.of("n3-zaros-overhead","n3-zaros-defence","n3-zaros-magic","n3-zaros-fixed"),report); } catch(Exception ex) { throw new IllegalStateException(ex); } });
			edt(()->PanelPreview.click(r.panel,"Compare this plan")); r.settle();
			edt(()->{
				report.check("Declared Zaros target recomputes damage "+font,true,planner[0].result().comparison.actualToOwned.before.plan.expectedDamage!=null);
				PanelPreview.click(r.panel,"Copy plan report"); String text=NexScenarioPanelTest.named(r.panel,"n3-report",JTextArea.class).getText();
				report.check("Zaros report preserves health ratio and distinct declarations "+font,true,text.contains("health ratio/scale=9/30") && text.contains("target Defence=250; target Magic=null") && text.contains("admitted Death Charge rune units={560=1, 565=1, 566=1}"));
				try { java.nio.file.Files.writeString(output.resolve("zaros-report-"+font+".txt"),text); } catch(Exception ex) { throw new IllegalStateException(ex); }
			});
			capture(r,output,"zaros-results",font,report);
		}
	}
	private static void fill(JPanel panel)
	{
		for(var entry:Map.of("n3-horizon","10","n3-energy","100","n3-restore","25","n3-target-hp","500","n3-player-hp","99","n3-cap","200","n3-opportunities","0,4,5,8,9").entrySet())
		{ NexScenarioPanelTest.named(panel,entry.getKey(),JTextField.class).setText(entry.getValue()); }
		PanelPreview.combo(panel,"n3-order").setSelectedIndex(1); PanelPreview.combo(panel,"n3-kandarin").setSelectedIndex(1);
		PanelPreview.combo(panel,"n3-ruby").setSelectedIndex(2); PanelPreview.combo(panel,"n3-policy").setSelectedIndex(1);
		NexScenarioPanelTest.confirmConditions(panel);
	}
	private static void edt(Runnable action) throws Exception { SwingUtilities.invokeAndWait(action); }
	private static void capture(OfflinePluginReplay r,Path output,String name,int font,ReviewReport report) throws Exception
	{ edt(()->{ try { PanelPreview.capture(r.panel,output.resolve(name+"-"+font+".png"),225,report); } catch(Exception ex) { throw new IllegalStateException(ex); } }); }
}
