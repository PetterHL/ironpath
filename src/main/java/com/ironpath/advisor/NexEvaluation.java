package com.ironpath.advisor;

import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import com.ironpath.nex.NexRecord;
import java.util.concurrent.atomic.AtomicLong;

/** Frozen, in-memory evaluation inputs. A context correction never reads a newer account or preferences. */
public final class NexEvaluation
{
	private static final AtomicLong SERIAL=new AtomicLong();
	public final long revision=SERIAL.incrementAndGet();
	public final String modelRevision;
	private final AccountSnapshot account;
	private final Preferences preferences;
	private final KnowledgeCatalog catalog;
	public NexEvaluation(AccountSnapshot account,Preferences preferences,KnowledgeCatalog catalog)
	{
		this.account=java.util.Objects.requireNonNull(account); this.preferences=java.util.Objects.requireNonNull(preferences);
		this.catalog=java.util.Objects.requireNonNull(catalog);
		modelRevision=NexOwnedEvaluator.VERSION+"/"+MeleeModel.VERSION+"/"+RangedModel.VERSION+"/"+catalog.version+"/"+com.ironpath.nex.NexContext.VERSION+"/"+NexFangAdvice.VERSION;
	}
	public NexOwnedResult evaluate(NexRecord record,java.util.function.BooleanSupplier current)
	{
		NexOwnedResult arithmetic=new NexOwnedEvaluator(catalog).evaluate(record.summary,account,preferences,current);
		return new NexOwnedResult(record.summary,arithmetic.choices,preferences.nex,arithmetic.scenarioUnlocks,record.context,record.revision,revision,modelRevision,arithmetic.advice);
	}
}
