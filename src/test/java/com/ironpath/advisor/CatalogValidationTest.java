package com.ironpath.advisor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.junit.Test;
import static org.junit.Assert.*;

/** Fail closed on mistakes that could change the meaning or provenance of bundled advice. */
public class CatalogValidationTest
{
	private JsonObject document()
	{
		return new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/com/ironpath/catalog.json"),
			StandardCharsets.UTF_8)).getAsJsonObject();
	}

	private void rejects(Consumer<JsonObject> mutation)
	{
		JsonObject document = document(); mutation.accept(document);
		assertThrows(IllegalArgumentException.class, () -> KnowledgeCatalog.read(new Gson(), new StringReader(document.toString())));
	}

	private JsonObject first(JsonObject document) { return document.getAsJsonArray("decisions").get(0).getAsJsonObject(); }

	@Test public void rejectsDuplicateIdsReferencesAndOverBudgetCatalogues()
	{
		rejects(d -> d.getAsJsonArray("decisions").get(1).getAsJsonObject().addProperty("id", first(d).get("id").getAsString()));
		rejects(d -> d.getAsJsonArray("sources").add(d.getAsJsonArray("sources").get(0).deepCopy()));
		rejects(d -> {
			while (d.getAsJsonArray("decisions").size() <= 64) { d.getAsJsonArray("decisions").add(first(d).deepCopy()); }
		});
		rejects(d -> first(d).getAsJsonArray("sources").add("missing-source"));
		rejects(d -> first(d).getAsJsonArray("requires").add("missing-decision"));
		rejects(d -> d.getAsJsonObject("itemSets").getAsJsonArray("magicFang").add(999999));
	}

	@Test public void rejectsMissingEvidenceAndRevisionMismatch()
	{
		rejects(d -> first(d).remove("sources"));
		rejects(d -> d.getAsJsonArray("sources").get(0).getAsJsonObject().remove("reuseBasis"));
		rejects(d -> d.getAsJsonArray("sources").get(0).getAsJsonObject().remove("licence"));
		rejects(d -> d.getAsJsonArray("sources").get(0).getAsJsonObject().remove("reviewed"));
		rejects(d -> d.getAsJsonArray("sources").get(0).getAsJsonObject().addProperty("revision", "1"));
		rejects(d -> d.getAsJsonArray("sources").get(0).getAsJsonObject().addProperty("reviewed", "2026-10-11"));
		rejects(d -> d.getAsJsonArray("sources").get(0).getAsJsonObject().getAsJsonArray("decisions").remove(0));
		rejects(d -> d.getAsJsonObject("gear").getAsJsonArray("melee").get(0).getAsJsonObject().remove("sources"));
		// Consistent cross-links alone are insufficient if a fixed matcher selects unevidenced gear.
		rejects(d -> {
			for (var decision : d.getAsJsonArray("decisions"))
			{
				if (decision.getAsJsonObject().get("id").getAsString().equals("plan-team-cox"))
				{ decision.getAsJsonObject().getAsJsonArray("sources").remove(new com.google.gson.JsonPrimitive("whip")); }
			}
			for (var source : d.getAsJsonArray("sources"))
			{
				if (source.getAsJsonObject().get("id").getAsString().equals("whip"))
				{ source.getAsJsonObject().getAsJsonArray("decisions").remove(new com.google.gson.JsonPrimitive("plan-team-cox")); }
			}
		});
	}

	@Test public void rejectsMalformedAllAnyLeavesAndUnknownPredicates()
	{
		rejects(d -> first(d).getAsJsonObject("when").add("conditions", new JsonArray()));
		rejects(d -> { first(d).getAsJsonObject("when").addProperty("test", "ANY"); first(d).getAsJsonObject("when").remove("conditions"); });
		rejects(d -> first(d).getAsJsonObject("when").addProperty("key", "PRAYER"));
		rejects(d -> first(d).getAsJsonObject("when").addProperty("test", "SCRIPT"));
		rejects(d -> first(d).getAsJsonObject("completeWhen").remove("value"));
		rejects(d -> first(d).getAsJsonObject("completeWhen").add("conditions", new JsonArray()));
		rejects(d -> d.getAsJsonArray("decisions").get(1).getAsJsonObject().getAsJsonObject("completeWhen").addProperty("minimum", 100));
		rejects(d -> first(d).getAsJsonObject("when").getAsJsonArray("conditions").get(1).getAsJsonObject().addProperty("key", "missing-set"));
	}

	@Test public void rejectsUnreviewedEffortBandsAndOldSchema()
	{
		rejects(d -> first(d).remove("effortBand"));
		rejects(d -> first(d).addProperty("effortBand", "FASTEST"));
		rejects(d -> d.addProperty("schemaVersion", 2));
	}

	@Test public void rejectsHardDependencyCyclesAndRepeatablePrerequisites()
	{
		// Prayer training really requires the permanent unlock; reversing that edge is a cycle.
		rejects(d -> first(d).getAsJsonArray("requires").add("train-rigour"));
		rejects(d -> first(d).getAsJsonArray("requires").add("unlock-rigour"));
		rejects(d -> first(d).getAsJsonArray("requires").add("plan-team-cox"));
		rejects(d -> first(d).remove("completeWhen"));
		rejects(d -> first(d).addProperty("repeatable", true));
	}
	@Test public void rejectsUnknownGoalsActivitiesOwnershipTargetsAndGearPackageCycles()
	{
		rejects(d -> first(d).addProperty("activity", "unreviewed-activity"));
		rejects(d -> first(d).getAsJsonArray("goals").add("unreviewed-goal"));
		rejects(d -> d.getAsJsonArray("goals").get(0).getAsJsonObject().getAsJsonArray("routes").add("missing-route"));
		rejects(d -> d.getAsJsonArray("goals").get(0).getAsJsonObject().addProperty("whenRole", "GAMEPLAY_SCRIPT"));
		rejects(d -> d.getAsJsonObject("ownershipLabels").remove("bowfaOrBetter"));
		rejects(d -> {
			JsonObject cyclic = new JsonObject(); cyclic.addProperty("test", "USABLE"); cyclic.addProperty("key", "ranged");
			d.getAsJsonObject("gear").getAsJsonArray("ranged").get(0).getAsJsonObject().add("useWhen", cyclic);
		});
		rejects(d -> {
			JsonObject leaf = new JsonObject(); leaf.addProperty("test", "LEVEL"); leaf.addProperty("key", "SLAYER"); leaf.addProperty("minimum", 100);
			d.getAsJsonObject("gear").getAsJsonArray("routes").get(0).getAsJsonObject().add("useWhen", leaf);
		});
	}

	@Test public void comparableRoutesAndGoalsUseDataWithoutUniqueKindsOrBossSwitches()
	{
		JsonObject doc = document();
		for (var goal : doc.getAsJsonArray("goals"))
		{
			if (goal.getAsJsonObject().get("id").getAsString().equals("toa-entry"))
			{ goal.getAsJsonObject().addProperty("id", "practice-amascut"); }
		}
		for (var decision : doc.getAsJsonArray("decisions"))
		{
			JsonArray goals = decision.getAsJsonObject().getAsJsonArray("goals");
			if (goals.remove(new com.google.gson.JsonPrimitive("toa-entry"))) { goals.add("practice-amascut"); }
		}
		for (var upgrade : doc.getAsJsonArray("upgrades"))
		{
			JsonArray goals = upgrade.getAsJsonObject().getAsJsonArray("goals");
			if (goals.remove(new com.google.gson.JsonPrimitive("toa-entry"))) { goals.add("practice-amascut"); }
		}
		KnowledgeCatalog catalog = KnowledgeCatalog.read(new Gson(), new StringReader(doc.toString()));
		assertTrue(catalog.decisions.stream().filter(d -> d.kind == KnowledgeCatalog.Kind.ACQUIRE).count() >= 4);
		assertTrue(Advisor.goalChoices(catalog).containsKey("practice-amascut"));
		assertFalse(Advisor.goalChoices(catalog).containsKey("toa-entry"));
	}

	@Test public void rejectsBrokenOutcomeComparisonAndSharedRewardContracts()
	{
		rejects(d -> d.remove("upgrades"));
		rejects(d -> d.getAsJsonArray("upgrades").get(0).getAsJsonObject().remove("status"));
		rejects(d -> d.getAsJsonArray("upgrades").get(0).getAsJsonObject().getAsJsonArray("routes").add("missing-route"));
		rejects(d -> d.getAsJsonArray("upgrades").get(0).getAsJsonObject().getAsJsonArray("sources").add("missing-source"));
		rejects(d -> d.getAsJsonArray("upgrades").get(0).getAsJsonObject().getAsJsonArray("applications").add(d.getAsJsonArray("upgrades").get(0).getAsJsonObject().getAsJsonArray("applications").get(0).deepCopy()));
		rejects(d -> d.getAsJsonArray("upgrades").get(3).getAsJsonObject().getAsJsonArray("shared").get(0).getAsJsonObject().addProperty("minDelve",1));
		rejects(d -> d.getAsJsonObject("comparison").getAsJsonArray("targets").get(0).getAsJsonObject().addProperty("magicDefence",-65));
		rejects(d -> d.getAsJsonObject("comparison").getAsJsonArray("targets").get(3).getAsJsonObject().addProperty("scalingSupported",true));
		rejects(d -> d.getAsJsonObject("comparison").getAsJsonArray("items").get(0).getAsJsonObject().addProperty("slot","head"));
		rejects(d -> d.getAsJsonObject("comparison").getAsJsonArray("items").get(0).getAsJsonObject().remove("sources"));
	}
	@Test public void analyticalTargetCannotClaimAnNpcOrSilentlyChangeItsDefenceAssumptions()
	{
		rejects(d -> d.getAsJsonObject("comparison").getAsJsonArray("targets").get(0).getAsJsonObject().addProperty("npcId",2215));
		rejects(d -> d.getAsJsonObject("comparison").getAsJsonArray("targets").get(0).getAsJsonObject().remove("defenceLevel"));
		rejects(d -> d.getAsJsonObject("comparison").getAsJsonArray("targets").get(0).getAsJsonObject().addProperty("magicLevel",80));
		rejects(d -> d.getAsJsonObject("comparison").getAsJsonArray("targets").get(0).getAsJsonObject().addProperty("magicDefence",298));
	}

}
