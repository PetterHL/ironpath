package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.advisor.PoweredMagic;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static com.ironpath.advisor.P4aFixtures.*;
import static org.junit.Assert.*;

public class ReferenceLifecycleTest
{
	private final ObservationSession session=new ObservationSession();
	private KnowledgeCatalog catalog;
	private AccountSnapshot account;
	@Before public void setup() throws Exception
	{
		catalog=KnowledgeCatalog.load(new Gson()); account=account(Set.of(11907,6585),90,90);
		session.select("synthetic-a");session.loadPreferences(session.generation(),"synthetic-a",Preferences.EMPTY,true);
		session.publish(session.generation(),"synthetic-a",account);session.reference(catalog);
	}
	private boolean edit(String key,String value){return session.editReference(session.generation(),session.revision(),"synthetic-a",account,key,value,catalog);}
	@Test public void confirmationInvalidatesOnLevelsItemsAssumptionsAndManualInputs()
	{
		assertTrue(edit("confirm","true"));assertTrue(session.reference(catalog).confirmed);
		// A new snapshot with identical values/timestamps should not destroy a confirmed reference.
		AccountSnapshot same=account(Set.of(11907,6585),90,90);session.publish(session.generation(),"synthetic-a",same);account=same;
		assertTrue(session.reference(catalog).confirmed);
		AccountSnapshot next=levels(account,"MAGIC",93);session.publish(session.generation(),"synthetic-a",next);account=next;
		assertFalse(session.reference(catalog).confirmed);
		assertEquals(26,new PoweredMagic(catalog).compare(account,Preferences.EMPTY,session.reference(catalog),"OCCULT").before.maxHit);
		assertTrue(edit("confirm","true"));assertTrue(edit("boost","MAGIC_POTION"));assertFalse(session.reference(catalog).confirmed);
		assertTrue(edit("confirm","true"));session.editPreferences(session.generation(),"synthetic-a",Preferences.EMPTY.confirm("augury",false,TIME));
		assertFalse(session.reference(catalog).confirmed);
		assertTrue(edit("confirm","true"));AccountSnapshot changed=account(Set.of(11907,6585,13235),93,90);
		session.publish(session.generation(),"synthetic-a",changed);account=changed;assertFalse(session.reference(catalog).confirmed);
	}
	@Test public void aStaleCorrectionOrConfirmationCannotCrossSnapshotRevisionOrIdentity()
	{
		long generation=session.generation(),revision=session.revision();AccountSnapshot old=account;
		AccountSnapshot changed=levels(account,"MAGIC",93);session.publish(generation,"synthetic-a",changed);session.reference(catalog);
		assertFalse(session.editReference(generation,revision,"synthetic-a",old,"confirm","true",catalog));
		session.select("synthetic-b");session.loadPreferences(session.generation(),"synthetic-b",Preferences.EMPTY,true);
		session.publish(session.generation(),"synthetic-b",account(Set.of(11907),90,90));
		assertFalse(session.editReference(generation,session.revision(),"synthetic-a",old,"boost","MAGIC_POTION",catalog));
		assertEquals(PoweredMagic.Boost.NONE,session.reference(catalog).boost);assertFalse(session.reference(catalog).confirmed);
	}
	@Test public void profileLogoutAndClearDiscardMemoryReferenceAndNothingIsAddedToPreferences()
	{
		assertTrue(edit("neck","0"));assertTrue(edit("boost","MAGIC_POTION"));assertTrue(edit("confirm","true"));
		assertTrue(session.preferences().confirmations.isEmpty());assertTrue(session.preferences().skipped.isEmpty());
		session.clear();assertNull(session.reference(catalog));
		session.select("synthetic-a-other-profile");session.loadPreferences(session.generation(),session.identity(),Preferences.EMPTY,true);
		session.publish(session.generation(),session.identity(),account);
		assertFalse(session.reference(catalog).confirmed);assertEquals(PoweredMagic.Boost.NONE,session.reference(catalog).boost);
		assertEquals(Integer.valueOf(6585),session.reference(catalog).slots.get("neck"));
	}
}
