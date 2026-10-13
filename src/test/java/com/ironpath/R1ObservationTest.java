package com.ironpath;

import com.google.gson.Gson;
import com.ironpath.advisor.KnowledgeCatalog;
import com.ironpath.advisor.PoweredMagic;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.preferences.Preferences;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import org.junit.Test;
import static com.ironpath.advisor.P4aFixtures.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class R1ObservationTest
{
	@Test public void supportedSlotsAndStackCountsAreIndependentImmutableObservations()
	{
		ItemContainer container=mock(ItemContainer.class);
		when(container.getItems()).thenReturn(new Item[]{new Item(26231,120),new Item(26231,30),new Item(-1,0)});
		when(container.getItem(2)).thenReturn(new Item(12002,1));when(container.getItem(9)).thenReturn(new Item(19544,1));when(container.getItem(10)).thenReturn(new Item(13235,1));
		assertEquals(Long.valueOf(150),AccountSnapshotReader.quantities(container,TIME,"synthetic bank").value().get(26231));
		Map<String,Integer> slots=AccountSnapshotReader.slots(container,TIME).value();assertEquals(Integer.valueOf(12002),slots.get("neck"));assertEquals(Integer.valueOf(19544),slots.get("hands"));assertEquals(Integer.valueOf(13235),slots.get("feet"));assertEquals(Integer.valueOf(0),slots.get("shield"));
		assertEquals(new java.util.HashSet<>(PoweredMagic.SLOTS),slots.keySet());
		assertThrows(UnsupportedOperationException.class,()->slots.put("head",999));
		assertFalse(AccountSnapshotReader.quantities(null,TIME,"unread").known());assertFalse(AccountSnapshotReader.slots(null,TIME).known());
	}
	@Test public void quantitiesAndSlotsInvalidateConfirmationAndClearWithCharacterGeneration() throws Exception
	{
		KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson());ObservationSession session=new ObservationSession();AccountSnapshot base=account(Set.of(11907),90,90);
		Map<Integer,Long> counts=new HashMap<>(Map.of(26231,120L));
		AccountSnapshot a=new AccountSnapshot(base.levels,base.inventory,base.equipment,base.bank,base.rigour,base.augury,base.facts,
			Observation.unknown(),Observation.unknown(),Observation.unknown(),fact(counts));counts.put(26231,250L);assertEquals(Long.valueOf(120),a.bankQuantities.value().get(26231));
		session.select("synthetic-a");session.loadPreferences(session.generation(),"synthetic-a",Preferences.EMPTY,true);session.publish(session.generation(),"synthetic-a",a);session.reference(catalog);
		assertTrue(session.editReference(session.generation(),session.revision(),"synthetic-a",a,"confirm","true",catalog));
		AccountSnapshot b=new AccountSnapshot(base.levels,base.inventory,base.equipment,base.bank,base.rigour,base.augury,base.facts,
			Observation.unknown(),Observation.unknown(),Observation.unknown(),fact(counts));session.publish(session.generation(),"synthetic-a",b);assertFalse(session.reference(catalog).confirmed);
		session.select("synthetic-b");assertNull(session.snapshot());assertNull(session.reference(catalog));assertTrue(session.preferences().confirmations.isEmpty());
	}
}
