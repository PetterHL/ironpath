package com.ironpath;

import com.ironpath.model.AccountSnapshot;
import java.util.Objects;

/** One active session only. Tokens protect client callbacks and queued Swing updates. */
final class ObservationSession
{
	private long generation;
	private long revision;
	private String identity;
	private AccountSnapshot snapshot;

	synchronized long generation() { return generation; }
	synchronized long revision() { return revision; }
	synchronized AccountSnapshot snapshot() { return snapshot; }

	synchronized void clear()
	{
		generation++;
		revision++;
		identity = null;
		snapshot = null;
	}

	synchronized void select(String currentIdentity)
	{
		if (!Objects.equals(identity, currentIdentity))
		{
			clear();
			identity = currentIdentity;
		}
	}

	synchronized boolean accepts(long token, String currentIdentity)
	{
		return identity != null && token == generation && identity.equals(currentIdentity);
	}

	synchronized boolean publish(long token, String currentIdentity, AccountSnapshot next)
	{
		if (!accepts(token, currentIdentity)) { return false; }
		snapshot = next;
		revision++;
		return true;
	}

	synchronized boolean currentView(long token, long viewRevision)
	{
		return generation == token && revision == viewRevision;
	}
}
