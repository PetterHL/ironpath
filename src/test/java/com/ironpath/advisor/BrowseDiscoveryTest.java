package com.ironpath.advisor;

import com.google.gson.Gson;
import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import org.junit.Test;
import static com.ironpath.advisor.R15Fixtures.*;
import static org.junit.Assert.*;

public class BrowseDiscoveryTest
{
	@Test public void unreadPossessionNeverCreatesOwnedUseAndSkippedRoutesDoNotEraseIt() throws Exception
	{
		KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson()); Advisor advisor=new Advisor(catalog);
		AccountSnapshot account=R2AdviceTest.postBowfa();
		assertFalse(advisor.ownedUpgrades(account,Preferences.EMPTY).stream().anyMatch(u->u.upgrade.id.equals("tormented-bracelet")));
		// Use the actual bundled key rather than inventing a separate ownership meaning.
		String key="owns-"+catalog.upgrades.stream().filter(u->u.id.equals("tormented-bracelet")).findFirst().orElseThrow().ownedWhen.key;
		Preferences manual=Preferences.EMPTY.confirm(key,true,P4aFixtures.TIME).skip("demonic-gorillas",true);
		assertTrue(advisor.ownedUpgrades(account,manual).stream().anyMatch(u->u.upgrade.id.equals("tormented-bracelet")));
		assertTrue(advisor.ownedUpgrades(items(account,19544),Preferences.EMPTY).stream().anyMatch(u->u.upgrade.id.equals("tormented-bracelet")));
		assertFalse(advisor.ownedUpgrades(account,manual).stream().anyMatch(u->u.upgrade.id.equals("rosewood-blowpipe")));
	}
	@Test public void specialistDiscoveryDoesNotRequireANumericMechanicAndHybridHasBothStyles() throws Exception
	{
		KnowledgeCatalog catalog=KnowledgeCatalog.load(new Gson());
		KnowledgeCatalog.Upgrade venator=catalog.upgrades.stream().filter(u->u.id.equals("venator-bow")).findFirst().orElseThrow();
		assertEquals("NONE",venator.comparison);
		assertTrue(new BrowseMetadata(catalog,venator).matches("Ranged","weapon","muspah",""));
		assertFalse(new BrowseMetadata(catalog,venator).matches("Magic","weapon","",""));
		BrowseMetadata treads=new BrowseMetadata(catalog,catalog.upgrades.stream().filter(u->u.id.equals("avernic-treads")).findFirst().orElseThrow());
		assertTrue(treads.matches("Ranged","feet","","")); assertTrue(treads.matches("Magic","feet","",""));
	}
}
