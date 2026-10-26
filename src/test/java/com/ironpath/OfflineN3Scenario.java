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
				r.fight(30,20,60,true,offset->{
					if(offset==5) { try {
						r.edt(); capture(r,output,"active-tracking",font,report);
						edt(()->{ try {
							report.check("Active kill status is visible "+font,"Tracking kill…",NexScenarioPanelTest.named(r.panel,"nex-tracking-status",JTextArea.class).getText());
							PanelPreview.viewport(r.panel,output.resolve("active-tracking-viewport-"+font+".png"),225,600,null,List.of("nex-tracking-status","nex-tracking-progress"),report);
						} catch(Exception ex) { throw new IllegalStateException(ex); } });
					} catch(Exception ex) { throw new IllegalStateException(ex); } }
					if(offset==6) { r.plugin.onChatMessage(new net.runelite.api.events.ChatMessage(null,net.runelite.api.ChatMessageType.SPAM,"","You eat the shark.","",0)); }
					if(offset==7) { r.plugin.onChatMessage(new net.runelite.api.events.ChatMessage(null,net.runelite.api.ChatMessageType.ENGINE,"","Upon the death of your next two foes, some of your special attack energy will be restored.","",0)); }
					if(offset==4)
					{
						when(r.local.getAnimation()).thenReturn(9471);
						var event=new net.runelite.api.events.AnimationChanged(); event.setActor(r.local); r.plugin.onAnimationChanged(event);
					}
				}); r.settle(); if(font==12) { OfflineReview.versions(report,r.catalog); }
				report.check("Pilot animation callback retained "+font,1,r.plugin.ownedResult.summary.observations.launches.size());
				report.check("Server-agreed smoke callback energy "+font,Integer.valueOf(1000),r.plugin.ownedResult.observations.startEnergy);
				var observed=r.plugin.ownedResult.summary.observations.launches.get(0);
				report.check("Settled callback preserves same launch inputs "+font,com.ironpath.nex.NexObservationReview.Reconciliation.SAME_INPUTS,r.plugin.ownedResult.observations.reconciled.get(observed));
				report.check("One provisional Fang attack, no timer-only repeats "+font,1L,r.plugin.ownedResult.observations.actions.entries.stream().filter(com.ironpath.nex.NexActionTimeline.Entry::attack).count());
				report.check("Food success survives public handler to worker "+font,1L,r.plugin.ownedResult.observations.actions.entries.stream().filter(e->e.signal.kind==com.ironpath.nex.NexActions.Kind.FOOD_SHARK).count());
				var advice=r.plugin.ownedResult.advice;
				report.check("Automatic Fang sample from captured actions "+font,1,advice.included.size());
				report.check("Automatic Fang complete-package pairs "+font,true,!advice.rows.isEmpty());
				report.check("Automatic Fang remains provisional "+font,false,advice.json().get("validated").getAsBoolean());
				String frozenAdvice=advice.json().toString();
				capture(r,output,"fang-advice",font,report);
				edt(()->{ try {
					report.check("Main Fang preview has no competing theoretical DPS card "+font,false,PanelPreview.text(r.panel).contains("DPS"));
					PanelPreview.viewport(r.panel,output.resolve("fang-advice-viewport-"+font+".png"),225,600,"nex-fang-gain",List.of("nex-fang-gain","nex-fang-max-hit","nex-fang-sample","nex-fang-details"),report);
				} catch(Exception ex) { throw new IllegalStateException(ex); } });
				final NexScenarioPanel[] panel={null};
				edt(()->{
					PanelPreview.click(r.panel,"Base-stat comparisons"); PanelPreview.combo(r.panel,"nex-owned-support-filter").setSelectedItem("All states");
					PanelPreview.click(r.panel,"Spec energy & Death Charge"); PanelPreview.click(r.panel,"Natural-restoration examples");
				});
				report.check("N3A three bounded natural-restoration examples "+font,3,r.plugin.ownedResult.resources.scenarios.examples.size());
				capture(r,output,"natural-examples",font,report);
				edt(()->{
					PanelPreview.click(r.panel,"Spec energy & Death Charge");
					PanelPreview.combo(r.panel,"nex-owned-role-filter").setSelectedItem("melee");
					panel[0]=(NexScenarioPanel)PanelPreview.children(r.panel).stream().filter(c->c instanceof NexScenarioPanel).findFirst().orElseThrow();
					PanelPreview.click(r.panel,"Plan specs & gear"); PanelPreview.click(r.panel,"Use recorded attack ticks");
					report.check("Recorded offsets seed only a draft "+font,"0",NexScenarioPanelTest.named(r.panel,"n3-opportunities",JTextField.class).getText());
					report.check("Recorded draft never confirms readiness "+font,false,NexScenarioPanelTest.named(r.panel,"n3-legal",JCheckBox.class).isSelected());
					fill(r.panel);
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
					for(var entry:Map.of("SHARK","1","KARAMBWAN","1","BREW","0","RESTORE","4").entrySet())
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
					report.check("Supply and cost report retains explicit assumptions "+font,true,text.contains("Declared supply actions=[0:BREW, 1:SHARK, 1:KARAMBWAN, 4:RESTORE]") && text.contains("gp=400") && text.contains("nex-supplies-2"));
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
				edt(()->{
					PanelPreview.openSection(r.panel,"Diagnostics"); PanelPreview.click(r.panel,"Calculation details");
					var pages=PanelPreview.combo(r.panel,"nex-observation-page");
					report.check("Observation events have selectable windows "+font,true,pages.getItemCount()>1);
					PanelPreview.click(r.panel,"Copy report now");
					String copied=NexScenarioPanelTest.named(r.panel,"nex-report-text",JTextArea.class).getText();
					report.check("Default copy includes whole-capture health and HUD checks "+font,true,copied.contains("Health target=NEX;") && copied.contains("HUD checks=") && copied.contains("RuneLite client version:"));
					report.check("Whole-copy exports provisional action timeline "+font,true,copied.contains(com.ironpath.nex.NexActionTimeline.VERSION) && copied.contains("ACTION_SIGNAL") && copied.contains("DC_UPGRADED") && copied.contains("ENGINE_MESSAGE") && copied.contains("FOOD_SHARK") && copied.contains("PROVISIONAL_REQUIRES_CONTROLLED_CLIENT_CHECK"));
					pages.setSelectedIndex(1); PanelPreview.click(r.panel,"Copy report now");
					report.check("Copied evidence is independent of UI event page "+font,true,copied.equals(NexScenarioPanelTest.named(r.panel,"nex-report-text",JTextArea.class).getText()));
					try { java.nio.file.Files.writeString(output.resolve("observation-report-"+font+".txt"),copied); } catch(Exception ex) { throw new IllegalStateException(ex); }
				});
				capture(r,output,"observation-events",font,report);
				edt(()->{ try { PanelPreview.viewport(r.panel,output.resolve("observation-events-viewport-"+font+".png"),225,600,"nex-observation-page",List.of("nex-observation-page"),report); } catch(Exception ex) { throw new IllegalStateException(ex); } });
				edt(()->PanelPreview.closeSection(r.panel,"Diagnostics"));
				var firstSource=finished[0].source;
				String frozenActions=r.plugin.ownedResult.observations.export;
				r.fight(35,40,40,true); r.settle();
				report.check("Next kill cannot mutate frozen launch evidence "+font,1,firstSource.summary.observations.launches.size());
				report.check("Frozen action messages survive next kill "+font,true,new com.ironpath.nex.NexObservationReview(firstSource.summary,firstSource.advice).export.equals(frozenActions));
				report.check("Next kill preserves frozen Fang preview "+font,true,frozenAdvice.equals(firstSource.advice.json().toString()));
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
			zeroDamage(output,report,font); zaros(output,report,font);
		}
	}
	private static void zeroDamage(Path output,ReviewReport report,int font) throws Exception
	{
		try(var r=new OfflinePluginReplay(font))
		{
			r.carried.remove(11832); // This is the explicitly hypothetical Bandos alternative.
			r.fight(30,20,60,true,offset->{ if(offset==4) {
				r.worn=new LinkedHashMap<>(com.ironpath.advisor.NexOwnedFixtures.melee());
				r.worn.put("body",26384); r.worn.put("cape",24224); r.worn.put("shield",24143);
				r.plugin.onItemContainerChanged(new net.runelite.api.events.ItemContainerChanged(net.runelite.api.gameval.InventoryID.WORN,r.client.getItemContainer(net.runelite.api.gameval.InventoryID.WORN)));
				when(r.local.getAnimation()).thenReturn(9471);
				var event=new net.runelite.api.events.AnimationChanged(); event.setActor(r.local); r.plugin.onAnimationChanged(event);
			} }); r.settle();
			edt(()->{
				var rows=r.plugin.ownedResult.advice.rows; int selected=-1;
				for(int i=0;i<rows.size();i++)
				{ var p=rows.get(i).pair; if(p.beforeSetup.slots.get("body")==26384 && p.afterSetup.slots.get("body")==11832) { selected=i; break; } }
				if(selected<0) { throw new IllegalStateException("No Torva-to-Bandos comparison"); }
				var row=rows.get(selected);
				report.near("Bandos has zero paired offence change at this breakpoint "+font,0,row.gain.lower,0);
				report.near("Bandos upper bound is also zero "+font,0,row.gain.upper,0);
				PanelPreview.combo(r.panel,"nex-fang-alternative").setSelectedIndex(selected);
				report.check("Exact zero has an explicit label "+font,"No expected damage change",NexScenarioPanelTest.named(r.panel,"nex-fang-gain",JTextArea.class).getText());
				report.check("The replaced item is named "+font,"Replaces Torva platebody",NexScenarioPanelTest.named(r.panel,"nex-fang-replaces",JTextArea.class).getText());
				report.check("Max-hit equality is visible "+font,"Max hit 47 → 47",NexScenarioPanelTest.named(r.panel,"nex-fang-max-hit",JTextArea.class).getText());
			});
			capture(r,output,"fang-zero-damage",font,report);
			edt(()->{ try {
				PanelPreview.viewport(r.panel,output.resolve("fang-zero-damage-viewport-"+font+".png"),225,600,"nex-fang-replaces",List.of("nex-fang-replaces","nex-fang-gain","nex-fang-max-hit","nex-fang-sample","nex-fang-details"),report);
			} catch(Exception ex) { throw new IllegalStateException(ex); } });
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
				PanelPreview.click(r.panel,"Base-stat comparisons"); PanelPreview.combo(r.panel,"nex-owned-support-filter").setSelectedItem("All states");
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
			edt(()->{
				NexScenarioPanelTest.named(r.panel,"n3-opportunities",JTextField.class).setText("0,5");
				report.check("Per-attack option starts unchecked "+font,false,NexScenarioPanelTest.named(r.panel,"n3-per-attack",JCheckBox.class).isSelected());
				NexScenarioPanelTest.named(r.panel,"n3-per-attack",JCheckBox.class).doClick(); NexScenarioPanelTest.saveImpact(r.panel,0,500,250,"Soul Split");
				PanelPreview.click(r.panel,"Compare this plan");
			}); r.settle();
			edt(()->{
				var evaluation=planner[0].result().comparison.actualToOwned.before;
				report.check("Missing impact row retains resources "+font,true,evaluation.plan!=null);
				report.check("Missing impact row cannot borrow fixed HP or overhead "+font,null,evaluation.plan.expectedDamage);
				NexScenarioPanelTest.saveImpact(r.panel,5,10,300,"None");
			});
			capture(r,output,"impact-inputs",font,report);
			edt(()->{
				try {
					PanelPreview.viewport(r.panel,output.resolve("impact-hp-viewport-"+font+".png"),225,600,"n3-per-attack",List.of("n3-per-attack","n3-impact-tick","n3-impact-hp","n3-impact-player-hp","n3-impact-cap"),report);
					PanelPreview.viewport(r.panel,output.resolve("impact-zaros-viewport-"+font+".png"),225,600,"n3-impact-overhead",List.of("n3-impact-overhead","n3-impact-defence","n3-impact-magic","n3-impact-confirmed"),report);
				} catch(Exception ex) { throw new IllegalStateException(ex); }
				PanelPreview.click(r.panel,"Compare this plan");
			}); r.settle();
			final String[] impactReport={null};
			edt(()->{
				var result=planner[0].result(); var evaluation=result.comparison.actualToOwned.before;
				report.check("Per-attack stats and HP admit damage "+font,true,evaluation.plan.expectedDamage!=null);
				report.check("Second impact keeps separately supplied HP "+font,Integer.valueOf(10),evaluation.conditions.steps.get(3).targetHp.value()); // cast at 0 precedes attack/fallback.
				report.check("All packages share impact conditions "+font,true,evaluation.conditions==result.comparison.actualToOwned.after.conditions && evaluation.conditions==result.comparison.ownedToUpgrade.after.conditions);
				PanelPreview.click(r.panel,"Copy plan report"); impactReport[0]=NexScenarioPanelTest.named(r.panel,"n3-report",JTextArea.class).getText();
				report.check("Per-attack report exports distinct declared values "+font,true,impactReport[0].contains("schema 5") && impactReport[0].contains("Declared attack tick=5; target HP=10; player HP=99; hard cap=200; ruby allowed=true; impact confirmed=true; overhead=NONE; target Defence=300; target Magic=null"));
				try { java.nio.file.Files.writeString(output.resolve("impact-report-"+font+".txt"),impactReport[0]); } catch(Exception ex) { throw new IllegalStateException(ex); }
			});
			capture(r,output,"impact-results",font,report);
			edt(()->{
				NexScenarioPanelTest.saveImpact(r.panel,0,500,250,"Deflect Melee");
				NexScenarioPanelTest.saveImpact(r.panel,5,10,300,"Deflect Melee");
				PanelPreview.combo(r.panel,"n3-policy").setSelectedIndex(2);
				PanelPreview.combo(r.panel,"n3-upgrade-slot").setSelectedItem("weapon");
				JComboBox<?> items=PanelPreview.combo(r.panel,"n3-upgrade-item");
				for(int i=0;i<items.getItemCount();i++) if(items.getItemAt(i).toString().endsWith("(27690)")) { items.setSelectedIndex(i); break; }
				PanelPreview.click(r.panel,"Add item"); PanelPreview.click(r.panel,"Compare this plan");
			}); r.settle();
			edt(()->{
				var result=planner[0].result(); var ordinary=result.comparison.actualToOwned.before.plan; var special=result.comparison.ownedToUpgrade.after.plan;
				report.check("Deflect ordinary Fang keeps dependent damage gap "+font,null,ordinary.expectedDamage);
				report.check("Deflect declared VW admits two Magic specials "+font,2,special.resources.voidwakerSpecs);
				report.check("Deflect declared VW has supported damage "+font,true,special.expectedDamage!=null && special.gaps.isEmpty());
				report.check("Deflect declared VW retains independent HP cap minimum "+font,10,special.hits.get(1).damage.minimum);
				report.check("Deflect declared VW retains independent HP cap maximum "+font,10,special.hits.get(1).damage.maximum);
				report.check("Deflect unknown ordinary baseline withholds marginal gain "+font,null,result.comparison.ownedToUpgrade.expectedDamage);
				PanelPreview.click(r.panel,"Copy plan report"); String text=NexScenarioPanelTest.named(r.panel,"n3-report",JTextArea.class).getText();
				report.check("Deflect report retains attack-type rule and explicit overhead "+font,true,text.contains("Voidwaker's Magic-type special is supported") && text.contains("overhead=DEFLECT_MELEE") && text.contains("nex-package-7"));
				try { java.nio.file.Files.writeString(output.resolve("deflect-funded-report-"+font+".txt"),text); } catch(Exception ex) { throw new IllegalStateException(ex); }
			});
			capture(r,output,"deflect-funded-results",font,report);
			edt(()->{
				try { PanelPreview.viewport(r.panel,output.resolve("deflect-funded-viewport-"+font+".png"),225,600,"n3-copy-report",List.of("n3-copy-report","n3-report-scroll"),report); }
				catch(Exception ex) { throw new IllegalStateException(ex); }
				NexScenarioPanelTest.named(r.panel,"n3-energy",JTextField.class).setText("50"); PanelPreview.click(r.panel,"Compare this plan");
			}); r.settle();
			edt(()->{
				var plan=planner[0].result().comparison.ownedToUpgrade.after.plan;
				report.check("Deflect low energy admits one special "+font,1,plan.resources.voidwakerSpecs);
				report.check("Deflect low energy admits ordinary fallback "+font,1,plan.resources.ordinaryAttacks);
				report.check("Deflect ordinary fallback keeps total damage unavailable "+font,null,plan.expectedDamage);
				report.check("Deflect supported first special remains inspectable "+font,true,plan.hits.get(0).damage!=null);
				report.check("Deflect fallback adds exactly one damage gap "+font,1,plan.gaps.size());
				PanelPreview.click(r.panel,"Copy plan report"); impactReport[0]=NexScenarioPanelTest.named(r.panel,"n3-report",JTextArea.class).getText();
				try { java.nio.file.Files.writeString(output.resolve("deflect-fallback-report-"+font+".txt"),impactReport[0]); } catch(Exception ex) { throw new IllegalStateException(ex); }
			});
			capture(r,output,"deflect-fallback-results",font,report);
			r.fight(35,40,40,true); r.settle();
			edt(()->{
				report.check("New kill preserves frozen per-attack report "+font,impactReport[0],NexScenarioPanelTest.named(r.panel,"n3-report",JTextArea.class).getText());
				PanelPreview.click(r.panel,"Compare this plan");
				NexScenarioPanelTest.named(r.panel,"n3-impact-hp",JTextField.class).setText("20");
			}); r.settle();
			edt(()->{
				report.check("Draft edits reject pending per-attack publication "+font,null,planner[0].result());
				report.check("Draft edits clear old per-attack export "+font,null,NexScenarioPanelTest.named(r.panel,"n3-report",JTextArea.class));
				PanelPreview.click(r.panel,"Load attack conditions"); PanelPreview.click(r.panel,"Compare this plan");
			});
			Runnable stale=r.workers.take(); edt(()->PanelPreview.click(r.panel,"Back to encounter"));
			edt(()->PanelPreview.click(r.panel,"Clear session")); r.callbacks.drain(); OfflinePluginReplay.edt(); r.runWorker(stale); OfflinePluginReplay.edt();
			edt(()->report.check("Reset rejects pending per-attack publication "+font,false,PanelPreview.text(r.panel).contains("Conditional plan results")));
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
