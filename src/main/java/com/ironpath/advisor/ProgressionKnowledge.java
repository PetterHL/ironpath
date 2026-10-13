package com.ironpath.advisor;

import java.util.List;

/** Small bundled relationship vocabulary. Utility links never enter the hard route graph. */
public final class ProgressionKnowledge
{
	public enum Dimension { ORDINARY_OFFENCE, BURST, MULTI_TARGET, CONVENIENCE, INVENTORY, RESOURCE }
	public enum Significance { CORE, SUBSTANTIAL, CONTEXTUAL }
	public enum Constraint { METHOD_CAPABILITY, OPTIONAL_IMPROVEMENT }
	public static final class Context
	{
		public String id, activity, mode, method, reviewed;
		public KnowledgeCatalog.Condition accessWhen, methodWhen;
		public List<String> sources, resourceConflicts;
		public List<Preparation> preparation;
	}
	public static final class Preparation
	{
		public String role, label;
		public Constraint constraint;
		public KnowledgeCatalog.Condition when;
		public List<String> sources;
	}
	public static final class Relation
	{
		public String id, subject, context, role, reason, limitation, reviewed, status;
		public Dimension dimension;
		public Significance significance;
		public KnowledgeCatalog.Condition when, noBenefitWhen;
		public List<Alternative> alternatives;
		public List<String> sources;
	}
	public static final class Alternative
	{
		public String label;
		public KnowledgeCatalog.Condition when;
		public boolean coversBenefit;
	}
	public final List<Context> contexts;
	public final List<Relation> relations;
	ProgressionKnowledge(List<Context> contexts, List<Relation> relations)
	{ this.contexts=List.copyOf(contexts); this.relations=List.copyOf(relations); }
}
