package com.ironpath.advisor;

import java.util.List;

/** Existing possession covers acquisition, but does not establish usable or charged equipment. */
public final class OwnedUpgrade
{
	public final KnowledgeCatalog.Upgrade upgrade;
	public final String evidence;
	public final List<String> applications, roles;
	public final List<String> sources;
	OwnedUpgrade(KnowledgeCatalog.Upgrade upgrade, String evidence, List<String> applications, List<String> roles, List<String> sources)
	{
		this.upgrade=upgrade; this.evidence=evidence;
		this.applications=List.copyOf(applications); this.roles=List.copyOf(roles);
		this.sources=List.copyOf(sources);
	}
}
