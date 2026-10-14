package com.ironpath;

import com.ironpath.model.AccountSnapshot;
import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.advisor.MagicReference;
import com.ironpath.advisor.PoweredMagic;
import com.ironpath.preferences.Preferences;
import java.util.Objects;

/** One active session only. Tokens protect client callbacks and queued Swing updates. */
final class ObservationSession
{
	private long generation;
	private long revision;
	private String identity;
	private AccountSnapshot snapshot;
	private final MagicReference reference = new MagicReference();
	private final com.ironpath.advisor.RangedReference rangedReference = new com.ironpath.advisor.RangedReference();
	private com.ironpath.advisor.PaceScenario pace;
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
	synchronized com.ironpath.advisor.PaceScenario pace() { return pace; }
	synchronized boolean editPace(long token, long viewRevision, String currentIdentity, AccountSnapshot expected,
		com.ironpath.advisor.PaceScenario next)
	{
		if (!accepts(token, currentIdentity) || revision != viewRevision || snapshot != expected || !loaded || snapshot == null) { return false; }
		pace=next; revision++; return true;
	}
	synchronized PoweredMagic.Setup reference(KnowledgeCatalog catalog)
	{ return snapshot == null || !loaded ? null : reference.update(snapshot, preferences, catalog); }
	synchronized com.ironpath.advisor.CombatSetup rangedReference(KnowledgeCatalog catalog)
	{ return snapshot == null || !loaded ? null : rangedReference.update(snapshot, preferences, catalog); }
	synchronized boolean editReference(long token, long viewRevision, String currentIdentity, AccountSnapshot expected,
		String key, String value, KnowledgeCatalog catalog)
	{
		if (!accepts(token, currentIdentity) || revision != viewRevision || snapshot != expected || snapshot == null || !loaded
			|| !(key.startsWith("ranged/") ? rangedReference.edit(key.substring(7), value, snapshot, preferences, catalog)
				: reference.edit(key, value, snapshot, preferences, catalog))) { return false; }
		revision++; return true;
	}

	synchronized void clear()
	{
		generation++;
		revision++;
		identity = null;
		snapshot = null;
		reference.clear();
		rangedReference.clear();
		pace = null;
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
