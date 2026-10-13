package com.ironpath.advisor;

import com.ironpath.model.Truth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import static com.ironpath.advisor.ValueEvidence.Importance.*;

/** Versioned family policy, with direct value anchors; no recursive utility scoring. */
final class ProgressionEvaluator
{
	static ValueEvidence evaluate(KnowledgeCatalog catalog, KnowledgeCatalog.Upgrade upgrade, ComparisonResult comparison,
		List<ComparisonResult> contexts, Function<KnowledgeCatalog.Condition,Truth> truth)
	{
		ValueEvidence.Importance importance=UNCERTAIN;
		String baseline="Owned role alternatives unconfirmed", reason="Check the comparison inputs; unknown value is not zero.";
		List<String> codes=new ArrayList<>(); List<ValueEvidence.Role> roles=new ArrayList<>();
		boolean numeric=comparison.available(ComparisonResult.Metric.THEORETICAL_DPS), specialist=false, utility=false, excluded=false;
		if (comparison.beforeSetup != null)
		{
			String slot=upgrade.comparison.equals("RANGED") ? catalog.ranged.items.stream().filter(i->i.id==upgrade.itemId).map(i->i.slot).findFirst().orElse("weapon")
				: catalog.comparison.items.stream().filter(i->i.id==upgrade.itemId).map(i->i.slot).findFirst().orElse("weapon");
			Integer id=comparison.beforeSetup.slots.get(slot);
			baseline=catalog.gear.values().stream().flatMap(List::stream).filter(g->java.util.Objects.equals(id,g.id)).map(g->g.label).findFirst().orElse(id != null && id==0 ? "Explicitly empty reference " + slot : "Selected reference " + slot);
		}
		if (numeric)
		{
			double relative=comparison.before.dps > 0 ? comparison.dpsGain()/comparison.before.dps : 0;
			importance=comparison.dpsGain()<=1e-10 ? MARGINAL : relative>=0.25 ? KEY : relative>=0.05 ? STRONG : MARGINAL;
			reason=(importance==MARGINAL ? "Small or no ordinary gain" : "Supported ordinary improvement") + " over " + baseline + ".";
			codes.add(importance==KEY ? "MAJOR_SUPPORTED_IMPROVEMENT" : importance==STRONG ? "MATERIAL_CORE_GAIN" : "SMALL_OR_NO_ORDINARY_GAIN");
		}
		// Explicit encounter comparisons remain independent of the selected analytical reference.
		if (contexts.stream().anyMatch(c->c.available(ComparisonResult.Metric.THEORETICAL_DPS)&&c.dpsGain()>1e-10
			&& (c.primaryMetric==ComparisonResult.Metric.THEORETICAL_DPS || c.before.dps>0 && c.dpsGain()/c.before.dps>=0.05)))
		{
			if (importance.ordinal()>CONTEXTUAL.ordinal()) { importance=CONTEXTUAL; reason="Useful encounter trade-off over owned packages; neutral value is separate."; }
			specialist |= !numeric || comparison.dpsGain()<=1e-10; codes.add("SUPPORTED_ENCOUNTER_TRADEOFF");
		}
		for (ProgressionKnowledge.Relation relation : catalog.progression.relations.stream()
			.filter(r->r.subject.equals(upgrade.id)&&r.status.equals("reviewed")).sorted(Comparator.comparing(r->r.id)).collect(java.util.stream.Collectors.toList()))
		{
			if (relation.dimension!=ProgressionKnowledge.Dimension.ORDINARY_OFFENCE) { specialist=true; }
			Truth applicable=truth.apply(relation.when);
			if (applicable==Truth.FALSE || truth.apply(relation.noBenefitWhen)==Truth.TRUE) { excluded=true; continue; }
			List<String> alternatives=new ArrayList<>(); boolean covered=false;
			for (ProgressionKnowledge.Alternative alternative : relation.alternatives)
			{ if (truth.apply(alternative.when)==Truth.TRUE) { alternatives.add(alternative.label); covered |= alternative.coversBenefit; } }
			roles.add(new ValueEvidence.Role(relation,applicable,alternatives,covered));
			if (covered) { codes.add("OWNED_ROLE_COVERED"); continue; }
			if (applicable==Truth.UNKNOWN)
			{
				codes.add("METHOD_CONDITIONAL");
				// Known weaker reference gear can establish conditional core value without proving global absence.
				if (relation.dimension==ProgressionKnowledge.Dimension.ORDINARY_OFFENCE && comparison.before!=null)
				{
					ValueEvidence.Importance conditional=relation.significance==ProgressionKnowledge.Significance.CORE ? KEY : relation.significance==ProgressionKnowledge.Significance.SUBSTANTIAL ? STRONG : CONTEXTUAL;
					if (conditional.ordinal()<importance.ordinal()) { importance=conditional; reason="Core ranged neck over " + baseline + "; check unobserved alternatives."; }
					codes.add("OWNERSHIP_CONDITIONAL_ROLE");
				}
				continue;
			}
			if (relation.dimension==ProgressionKnowledge.Dimension.ORDINARY_OFFENCE && comparison.before==null) { codes.add("COMPARISON_BASELINE_UNKNOWN"); continue; }
			boolean independent=relation.dimension!=ProgressionKnowledge.Dimension.ORDINARY_OFFENCE;
			utility |= independent;
			ValueEvidence.Importance roleImportance=relation.significance==ProgressionKnowledge.Significance.CORE ? KEY
				: relation.significance==ProgressionKnowledge.Significance.SUBSTANTIAL ? STRONG : CONTEXTUAL;
			if (roleImportance.ordinal()<importance.ordinal())
			{ importance=roleImportance; reason=relation.dimension==ProgressionKnowledge.Dimension.ORDINARY_OFFENCE ? "Core ranged neck over " + baseline + "." : relation.reason; }
			codes.add(relation.significance==ProgressionKnowledge.Significance.CORE ? "CORE_ROLE_GAP"
				: relation.dimension==ProgressionKnowledge.Dimension.CONVENIENCE && relation.significance==ProgressionKnowledge.Significance.SUBSTANTIAL ? "SUBSTANTIAL_CONVENIENCE" : "REVIEWED_ROLE_VALUE");
			if (!alternatives.isEmpty()) { codes.add("ADEQUATE_OWNED_METHOD"); }
		}
		if (!numeric && roles.isEmpty() && excluded) { importance=MARGINAL; reason="No reviewed utility in the selected contexts; other uses remain optional."; codes.add("METHOD_NO_BENEFIT"); }
		RangedItem candidate=catalog.ranged.items.stream().filter(i->i.id==upgrade.itemId).findFirst().orElse(null);
		KnowledgeCatalog.Condition toxic=new KnowledgeCatalog.Condition(); toxic.test=KnowledgeCatalog.Test.USABLE; toxic.key="toxicBlowpipeActive";
		if (candidate!=null && candidate.weapon==RangedItem.Weapon.ROSEWOOD && !utility && truth.apply(toxic)==Truth.TRUE)
		{
			importance=MARGINAL; specialist=true;
			reason="Owned Toxic/Blazing blowpipe is stronger for ordinary attacks. Rosewood is an optional scale-free alternative; check supplies separately.";
			codes.add("OWNED_STRONGER_BLOWPIPE");
		}
		return new ValueEvidence(importance,reason,baseline,specialist,utility,codes.stream().distinct().collect(java.util.stream.Collectors.toList()),roles);
	}
}
