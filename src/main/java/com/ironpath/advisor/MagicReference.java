package com.ironpath.advisor;

import com.ironpath.model.AccountSnapshot;
import com.ironpath.model.Observation;
import com.ironpath.preferences.Preferences;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Memory-only proposal/corrections. Owned manual gear cannot enter the observed baseline.
 * Its owner clears this state with the same character/profile generation as observations. */
public final class MagicReference
{
	private Map<String, Object> inputs;
	private PoweredMagic.Setup setup;
	private boolean corrected;

	public PoweredMagic.Setup update(AccountSnapshot account, Preferences preferences, KnowledgeCatalog catalog)
	{
		Map<String, Object> next = new HashMap<>();
		next.put("levels", value(account.levels)); next.put("inventory", value(account.inventory));
		next.put("equipment", value(account.equipment)); next.put("bank", value(account.bank));
		next.put("slots", value(account.equipmentSlots)); next.put("inventory-quantities", value(account.inventoryQuantities));
		next.put("equipment-quantities", value(account.equipmentQuantities)); next.put("bank-quantities", value(account.bankQuantities));
		next.put("facts", account.facts.entrySet().stream().collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> value(e.getValue()))));
		next.put("augury", value(account.augury));
		Map<String, Boolean> manual = new HashMap<>();
		preferences.confirmations.forEach((key, confirmation) -> manual.put(key, confirmation.value));
		next.put("manual", manual);
		if (!Objects.equals(inputs, next))
		{
			setup = setup == null || !corrected ? new PoweredMagic(catalog).propose(account) : setup.unconfirmed();
			inputs = next;
		}
		return setup;
	}
	private Object value(Observation<?> observation) { return observation.known() ? observation.value() : "unknown"; }
	public boolean edit(String key, String value, AccountSnapshot account, Preferences preferences, KnowledgeCatalog catalog)
	{
		if (setup == null) { return false; }
		try
		{
			PoweredMagic.Setup next = setup.edit(key, value);
			if (key.equals("confirm") && new PoweredMagic(catalog).validate(account, preferences, next) != null) { return false; }
			if (!PoweredMagic.SLOTS.contains(key) && !java.util.Set.of("prayer", "boost", "style", "target", "template", "confirm").contains(key)) { return false; }
			if (key.equals("target") && !new PoweredMagic(catalog).targetChoices().containsKey(value)) { return false; }
			setup = next; corrected = true; return true;
		}
		catch (IllegalArgumentException invalidChoice) { return false; }
	}
	public void clear() { inputs = null; setup = null; corrected = false; }
}
