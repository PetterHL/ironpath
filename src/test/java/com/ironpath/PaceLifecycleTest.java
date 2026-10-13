package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.advisor.PaceScenario;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import org.junit.Test;
import static com.ironpath.advisor.P4aFixtures.*;
import static com.ironpath.advisor.R15Fixtures.*;
import static org.junit.Assert.*;

public class PaceLifecycleTest
{
	@Test public void explicitPaceStaysInMemoryAndStaleActionsCannotCrossRevisionOrIdentity() throws Exception
	{
		ObservationSession session=new ObservationSession();session.select("A/profile");long token=session.generation();
		AccountSnapshot a=developed(false);session.publish(token,"A/profile",a);session.loadPreferences(token,"A/profile",Preferences.EMPTY,true);
		long revision=session.revision();PaceScenario pace=new PaceScenario("demonic-gorillas","ordinary",40,40,"cycle scenario",TIME);
		assertTrue(session.editPace(token,revision,"A/profile",a,pace));assertEquals(40,session.pace().minimumPerHour,0);
		assertFalse(session.editPace(token,revision,"A/profile",a,null));
		assertSame(Preferences.EMPTY,session.preferences());assertTrue(session.preferences().confirmations.isEmpty());
		KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson());session.reference(catalog);assertFalse(session.reference(catalog).confirmed);
		session.select("B/profile");assertNull(session.pace());assertFalse(session.editPace(token,session.revision(),"A/profile",a,pace));
		session.publish(session.generation(),"B/profile",a);session.loadPreferences(session.generation(),"B/profile",Preferences.EMPTY,true);
		assertTrue(session.editPace(session.generation(),session.revision(),"B/profile",a,pace));session.clear();assertNull(session.pace());
	}
	@Test public void observationRefreshRejectsAQueuedPaceWithoutTreatingItAsASavedPreference()
	{
		ObservationSession session=new ObservationSession();session.select("A");long token=session.generation();AccountSnapshot a=developed(false);
		session.publish(token,"A",a);session.loadPreferences(token,"A",Preferences.EMPTY,true);long revision=session.revision();
		session.publish(token,"A",items(a,19529));
		assertFalse(session.editPace(token,revision,"A",a,new PaceScenario("demonic-gorillas","ordinary",40,40,"cycle scenario",TIME)));
		assertNull(session.pace());assertNull(session.preferences().goal);
	}
}
