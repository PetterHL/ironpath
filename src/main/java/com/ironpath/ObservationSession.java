package com.ironpath;

import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.util.Objects;

/** One active session only. Tokens protect client callbacks and queued Swing updates. */
final class ObservationSession
{
	private long generation;
	private long revision;
	private String identity;
	private AccountSnapshot snapshot;
	private Preferences preferences = Preferences.EMPTY;
	private boolean loaded, writable;
	private String storageStatus = "Preferences unavailable until login.";

	synchronized String identity() { return identity; }
	synchronized Preferences preferences() { return preferences; }
	synchronized boolean preferencesLoaded() { return loaded; }
	synchronized boolean writable() { return writable; }
	synchronized String storageStatus() { return storageStatus; }
	synchronized boolean loadPreferences(long token, String currentIdentity, Preferences next, boolean canWrite)
	{
		if (!accepts(token, currentIdentity)) { return false; }
		preferences = next; loaded = true; writable = canWrite;
		storageStatus = canWrite ? "Preferences loaded locally." : "Local preferences could not be read. Editing disabled to protect the file.";
		revision++; return true;
	}
	synchronized boolean editPreferences(long token, String currentIdentity, Preferences next)
	{
		if (!accepts(token, currentIdentity) || !loaded || !writable) { return false; }
		preferences = next; storageStatus = "Saving local preferences…"; revision++; return true;
	}
	synchronized void saved(long token, String currentIdentity, Preferences saved, boolean success)
	{
		if (!accepts(token, currentIdentity) || preferences != saved) { return; }
		storageStatus = success ? "Preferences saved locally." : "Local save failed. Current changes are memory-only; check directory permissions.";
		revision++;
	}
	synchronized long generation() { return generation; }
	synchronized long revision() { return revision; }
	synchronized AccountSnapshot snapshot() { return snapshot; }

	synchronized void clear()
	{
		generation++;
		revision++;
		identity = null;
		snapshot = null;
		preferences = Preferences.EMPTY; loaded = false; writable = false;
		storageStatus = "Loading local character preferences…";
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
