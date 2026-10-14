package com.ironpath.encounter;

import com.ironpath.advisor.Advisor;
import com.ironpath.advisor.ComparisonResult;
import java.util.List;

/** Post-completion offence only. There is intentionally no seconds-saved field. */
public final class KillImpactResult
{
	public static final class Option
	{
		public final String title, kind, reason;
		public final int itemId;
		public final ComparisonResult comparison;
		public final Advisor.UpgradeView upgrade;
		public Option(String title, int id, String kind, String reason, ComparisonResult comparison, Advisor.UpgradeView upgrade)
		{ this.title=title; itemId=id; this.kind=kind; this.reason=reason; this.comparison=comparison; this.upgrade=upgrade; }
	}
	public final EncounterSummary summary;
	public final EncounterSummary.Segment selected;
	public final List<Option> options;
	public final List<String> gaps;
	public final String knowledgeVersion;
	public KillImpactResult(EncounterSummary summary, EncounterSummary.Segment selected, List<Option> options,
		List<String> gaps, String knowledge)
	{ this.summary=summary; this.selected=selected; this.options=List.copyOf(options); this.gaps=List.copyOf(gaps); knowledgeVersion=knowledge; }
}
