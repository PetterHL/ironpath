package com.ironpath.advisor;

import com.ironpath.model.AccountSnapshot;
import com.ironpath.preferences.Preferences;
import java.util.Map;

/** Session-only melee corrections. The owner clears this alongside character/profile observations. */
public final class MeleeReference
{
	private Map<String,Object> inputs;
	private CombatSetup setup;
	private boolean corrected;
	public CombatSetup update(AccountSnapshot account, Preferences preferences, KnowledgeCatalog catalog)
	{
		Map<String,Object> next = new java.util.HashMap<>();
		next.put("levels",value(account.levels)); next.put("inventory",value(account.inventory));
		next.put("bank",value(account.bank)); next.put("equipment",value(account.equipment)); next.put("slots",value(account.equipmentSlots));
		next.put("inventory-counts",value(account.inventoryQuantities)); next.put("bank-counts",value(account.bankQuantities)); next.put("equipment-counts",value(account.equipmentQuantities));
		next.put("facts",account.facts.entrySet().stream().collect(java.util.stream.Collectors.toMap(Map.Entry::getKey,e -> value(e.getValue()))));
		next.put("manual",preferences.confirmations.entrySet().stream().collect(java.util.stream.Collectors.toMap(Map.Entry::getKey,e -> e.getValue().value)));
		if (!java.util.Objects.equals(inputs,next)) { setup = setup == null || !corrected ? new MeleeComparisonService(catalog).propose(account,preferences) : setup.unconfirmed(); inputs = next; }
		return setup;
	}
	private Object value(com.ironpath.model.Observation<?> o) { return o.known() ? o.value() : "unknown"; }
	public boolean edit(String key, String value, AccountSnapshot a, Preferences p, KnowledgeCatalog catalog)
	{
		if (setup == null || !PoweredMagic.SLOTS.contains(key) && !java.util.Set.of("prayer","boost","style","target","template","confirm").contains(key)) { return false; }
		try
		{
			MeleeComparisonService service = new MeleeComparisonService(catalog);
			CombatSetup next = setup.edit(key,value);
			if (key.equals("target") && !service.referenceChoices(a).get("target").containsKey(value)) { return false; }
			if (key.equals("confirm") && service.validate(a,p,next) != null) { return false; }
			setup = next; corrected = true; return true;
		}
		catch (IllegalArgumentException invalid) { return false; }
	}
	public void clear() { inputs = null; setup = null; corrected = false; }
}
