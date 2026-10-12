package com.ironpath.preferences;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

/** Serial IO on RuneLite's worker executor; ordering survives plugin disable/re-enable. */
public final class PreferenceIO
{
	private final PreferenceStorage storage;
	private final Executor executor;
	private CompletableFuture<?> tail = CompletableFuture.completedFuture(null);
	public PreferenceIO(PreferenceStorage storage, Executor executor) { this.storage = storage; this.executor = executor; }
	public synchronized CompletableFuture<Preferences> load(String key)
	{
		CompletableFuture<Preferences> result = tail.handle((v, e) -> null).thenApplyAsync(v ->
		{
			try { return storage.read(key); } catch (Exception ex) { throw new CompletionException(ex); }
		}, executor);
		tail = result;
		return result;
	}
	public synchronized CompletableFuture<Void> save(String key, Preferences preferences)
	{
		CompletableFuture<Void> result = tail.handle((v, e) -> null).thenRunAsync(() ->
		{
			try { storage.write(key, preferences); } catch (Exception ex) { throw new CompletionException(ex); }
		}, executor);
		tail = result;
		return result;
	}
}
