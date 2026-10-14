package com.ironpath.advisor;

import java.time.LocalDate;
import java.util.List;

/** Reviewed mechanics only; recipient and trial scope are part of the model identity. */
public final class AcquisitionModel
{
	public enum Support { REVIEWED, UNSUPPORTED, INVALID }
	public enum Mechanic { FIXED_BERNOULLI, INDEPENDENT_ROLLS, REPEATED_SUCCESSES, UNSUPPORTED }
	public final String id, activity, mode, rewardFamily, route, node, recipient, trialUnit, version, reason;
	public final Mechanic mechanic;
	public final Support support;
	public final long numerator, denominator;
	public final int rolls, requiredSuccesses;
	public final LocalDate reviewed;
	public final List<String> assumptions, exclusions, sources;

	AcquisitionModel(KnowledgeCatalog.AcquisitionSpec s)
	{
		id=s.id; activity=s.activity; mode=s.mode; rewardFamily=s.rewardFamily; route=s.route; node=s.node;
		recipient=s.recipient; trialUnit=s.trialUnit; version=s.version; reason=s.reason;
		mechanic=s.mechanic; support=s.support; numerator=s.numerator; denominator=s.denominator;
		rolls=s.rolls; requiredSuccesses=s.requiredSuccesses;
		reviewed=LocalDate.parse(s.reviewed); assumptions=List.copyOf(s.assumptions);
		exclusions=List.copyOf(s.exclusions); sources=List.copyOf(s.sources);
	}
}
