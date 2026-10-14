package com.ironpath.advisor;

import com.ironpath.model.Truth;
import java.util.List;

/** Personal importance and independent utility, before acquisition effort is considered. */
public final class ValueEvidence
{
	public enum Importance
	{
		KEY("Key improvement"), STRONG("Strong option"), CONTEXTUAL("Contextual option"), MARGINAL("Marginal or covered"), UNCERTAIN("Uncertain");
		public final String label;
		Importance(String label) { this.label=label; }
	}
	public static final class Role
	{
		public final String context, role, reason, limitation;
		public final ProgressionKnowledge.Dimension dimension;
		public final Truth applicable;
		public final List<String> alternatives, sources;
		public final boolean covered;
		Role(ProgressionKnowledge.Relation relation, Truth applicable, List<String> alternatives, boolean covered)
		{
			context=relation.context; role=relation.role; reason=relation.reason; limitation=relation.limitation;
			dimension=relation.dimension; this.applicable=applicable; this.alternatives=List.copyOf(alternatives);
			this.covered=covered; sources=List.copyOf(relation.sources);
		}
	}
	public final Importance importance;
	public final String reason, baseline;
	public final boolean specialist, independentUtility;
	public final List<String> reasonCodes;
	public final List<Role> roles;
	ValueEvidence(Importance importance, String reason, String baseline, boolean specialist, boolean utility, List<String> codes, List<Role> roles)
	{
		this.importance=importance; this.reason=reason; this.baseline=baseline; this.specialist=specialist;
		independentUtility=utility; reasonCodes=List.copyOf(codes); this.roles=List.copyOf(roles);
	}
}
