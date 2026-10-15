package com.ironpath.advisor;

import com.ironpath.encounter.*;
import com.ironpath.model.*;
import com.ironpath.preferences.Preferences;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/** Retained ordinary Zilyana worn-state comparison, not a whole-encounter evaluator. */
public final class ZilyanaWornStateComparison
{
	private final KnowledgeCatalog catalog;
	private final RangedComparisonService service;
	private final Map<Integer,RangedItem> items;
	public ZilyanaWornStateComparison(KnowledgeCatalog catalog)
	{ this.catalog=catalog; service=new RangedComparisonService(catalog);
		items=catalog.ranged.items.stream().collect(Collectors.toUnmodifiableMap(i->i.id,i->i)); }
	public KillImpactResult evaluate(EncounterSummary summary, AccountSnapshot current, Preferences preferences)
	{
		EncounterSummary.Segment selected=summary.segments.stream().filter(s->s.ticks()>0).max(Comparator.comparingInt(EncounterSummary.Segment::ticks)).orElse(null);
		List<String> gaps=new ArrayList<>(summary.gaps);
		List<KillImpactResult.Option> options=new ArrayList<>();
		if (!"zilyana".equals(summary.context.encounter) || !"ordinary".equals(summary.context.phase)
			|| !"normal".equals(summary.context.mode))
		{
			gaps.add("This comparison supports only ordinary normal Zilyana; encounter-specific evaluation required");
			return result(summary,selected,options,gaps);
		}
		if (selected==null || summary.outcome!=EncounterSummary.Outcome.COMPLETE)
		{ gaps.add("Complete credited encounter and worn-state evidence required"); return result(summary,selected,options,gaps); }
		CombatState state=selected.state;
		AccountSnapshot account=historical(current,summary,state);
		String stateGap=stateGap(state);
		if (stateGap==null && summary.gaps.stream().anyMatch(g->g.startsWith("Special energy used")))
		{ stateGap="Special use observed; reduced target defence and special effects are unverified"; }
		CombatSetup baseline=setup(state,summary.context.encounter);
		String invalid=stateGap==null ? service.validate(account,preferences,baseline) : stateGap;
		int visible=state.visibleLevels.getOrDefault("RANGED",0);
		// Advisor handles unknown ownership, skipped routes, importance and package eligibility as usual.
		List<Advisor.UpgradeView> eligible=new Advisor(catalog).upgrades(account,preferences,
			new CombatComparisonService(catalog).propose(account),baseline,null);
		for (Advisor.UpgradeView upgrade : eligible)
		{
			if (!"RANGED".equals(upgrade.upgrade.comparison)) { continue; }
			ComparisonResult comparison=invalid==null
				? service.compareObserved(account,preferences,baseline,upgrade.upgrade.itemId,visible) : null;
			options.add(new KillImpactResult.Option(upgrade.upgrade.title,upgrade.upgrade.itemId,"Upgrade option",
				upgrade.card.reason.replace("Explicitly empty reference","observed empty"),comparison,upgrade));
		}
		if (invalid!=null) { gaps.add(invalid); return result(summary,selected,options,gaps); }
		CombatSetup owned=service.strongestObservedOwned(account,baseline,visible);
		if (owned!=null)
		{
			ComparisonResult comparison=ownedComparison(account,baseline,owned,visible);
			options.add(0,new KillImpactResult.Option(items.get(owned.slots.get("weapon")).label+" (owned)",owned.slots.get("weapon"),"Try owned setup",
				"Compare your observed owned package before a new grind.",comparison,null));
		}
		RangedItem weapon=items.get(baseline.slots.get("weapon"));
		if (weapon!=null && RangedModel.ammoKind(weapon)!=RangedItem.Ammo.NONE && RangedModel.ammoKind(weapon)!=RangedItem.Ammo.DART)
		{
			items.values().stream().filter(i->i.ammo!=RangedItem.Ammo.NONE && !i.boltEffect && RangedModel.compatible(weapon,i))
				.sorted(Comparator.comparingInt(i->i.id)).forEach(ammo->{
					ComparisonResult comparison=service.compareObserved(account,preferences,baseline,ammo.id,visible);
					if (comparison.before!=null && comparison.available(ComparisonResult.Metric.THEORETICAL_DPS) && comparison.dpsGain()>0)
					{ options.add(new KillImpactResult.Option(ammo.label,ammo.id,"Recurring ammunition",
						"Supplied ammunition scenario; current stock, saving and upkeep are separate from permanent upgrades.",comparison,null)); }
				});
		}
		return result(summary,selected,options,gaps);
	}
	private KillImpactResult result(EncounterSummary summary, EncounterSummary.Segment selected,
		List<KillImpactResult.Option> options,List<String> gaps)
	{ return new KillImpactResult(summary,selected,options,gaps,catalog.version); }
	private String stateGap(CombatState s)
	{
		Integer level=s.visibleLevels.get("RANGED");
		if(level==null || level<1 || level>200) { return "Visible Ranged level unavailable"; }
		if(s.style==null || s.style<0 || s.style>2) { return "Attack style unreviewed for the worn weapon"; }
		if(s.prayers.contains("SHARP_EYE") || s.prayers.contains("HAWK_EYE")) { return "Lower-tier ranged prayer not yet supported by this comparison"; }
		RangedItem weapon=items.get(s.slots.get("weapon"));
		if(weapon==null || weapon.weapon==null) { return "Worn weapon has no supported ranged model; melee and spell capture remain contextual only"; }
		if(RangedModel.ammoKind(weapon)==RangedItem.Ammo.DART) { return "Internal darts are unread; bank ammunition is not loaded-ammo evidence"; }
		RangedItem cape=items.get(s.slots.get("cape")), head=items.get(s.slots.get("head"));
		if(cape!=null && cape.chargedQuiver && RangedModel.ammoKind(weapon)!=RangedItem.Ammo.NONE)
		{ return "Active quiver ammunition/charge state not observed"; }
		if(head!=null && head.slayerHelmet) { return "Task assignment not observed for the worn slayer helmet"; }
		return null;
	}
	private CombatSetup setup(CombatState state,String target)
	{
		RangedModel.Prayer prayer=state.prayers.contains("RIGOUR") ? RangedModel.Prayer.RIGOUR
			: state.prayers.contains("DEADEYE") ? RangedModel.Prayer.DEADEYE
			: state.prayers.contains("EAGLE_EYE") ? RangedModel.Prayer.EAGLE_EYE : RangedModel.Prayer.NONE;
		int style=state.style==null ? -1 : state.style;
		RangedModel.Style rangedStyle=style==0 ? RangedModel.Style.ACCURATE : style==1 ? RangedModel.Style.RAPID : RangedModel.Style.LONGRANGE;
		Map<String,String> provenance=new LinkedHashMap<>();
		state.slots.keySet().forEach(slot->provenance.put(slot,"Actually worn in the selected captured state; no owned-gear substitution"));
		return new CombatSetup(state.slots,PoweredMagic.Prayer.NONE,PoweredMagic.Boost.NONE,PoweredMagic.Style.ACCURATE,
			false,false,target,provenance,CombatSetup.Family.RANGED,prayer,rangedStyle,false,false,0);
	}
	private ComparisonResult ownedComparison(AccountSnapshot account,CombatSetup before,CombatSetup after,int visible)
	{
		TargetProfile target=catalog.comparison.targets.stream().filter(t->t.id.equals(before.target)).findFirst().orElseThrow();
		PoweredMagic.Metrics b=RangedModel.calculateVisible(visible,before,target,items),a=RangedModel.calculateVisible(visible,after,target,items);
		EnumMap<ComparisonResult.Metric,PoweredMagic.Status> support=new EnumMap<>(ComparisonResult.Metric.class);
		boolean proc=List.of(before,after).stream().map(s->items.get(s.slots.get("ammo"))).anyMatch(i->i!=null && i.boltEffect);
		for(ComparisonResult.Metric metric:ComparisonResult.Metric.values())
		{ support.put(metric,metric==ComparisonResult.Metric.EXPECTED_KILL_TIME || metric==ComparisonResult.Metric.TRIP_PERFORMANCE
			|| proc && metric!=ComparisonResult.Metric.MAX_HIT ? PoweredMagic.Status.UNSUPPORTED : PoweredMagic.Status.CONDITIONAL); }
		return new ComparisonResult(before,after,b,a,target,support,"Unreduced ordinary target; proposed owned package supply/loading assumptions",
			RangedModel.VERSION,catalog.version,List.of("Observed visible Ranged " + visible + "; observed prayer/style retained. Owned alternative is a proposal, not the captured setup.",
				"Worn slots " + before.slots + "; owned package " + after.slots + "; sources " + target.sources + " and bundled ranged item dependencies."),true,Map.of());
	}
	private AccountSnapshot historical(AccountSnapshot current,EncounterSummary summary,CombatState state)
	{
		Instant now=Instant.now(); Set<Integer> worn=new HashSet<>();
		summary.segments.forEach(s->s.state.slots.values().stream().filter(id->id>0).forEach(worn::add));
		if(current.equipment.known()) { worn.addAll(current.equipment.value()); }
		Map<String,Integer> levels=new HashMap<>(current.levels.known() ? current.levels.value() : Map.of()); levels.putAll(state.realLevels);
		return new AccountSnapshot(Observation.observed(levels,now,"Real levels at captured state"),current.inventory,
			Observation.observed(worn,now,"Actually worn during this encounter"),current.bank,current.rigour,current.augury,current.facts,
			Observation.observed(state.slots,now,"Captured equipment slots"),current.inventoryQuantities,Observation.unknown(),current.bankQuantities);
	}
}
