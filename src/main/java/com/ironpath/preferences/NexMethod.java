package com.ironpath.preferences;

import com.ironpath.advisor.PoweredMagic;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Explicit planning choices only. Locks never edit the immutable observed timeline. */
public final class NexMethod
{
	public static final String ID = "nex-trio-mp2-one-kill";
	public static final Set<String> ROLES = Set.of("melee", "ranged", "spec");
	public static final NexMethod PILOT = new NexMethod(true, Map.of());
	public final boolean upgradedDeathCharge;
	public final Map<String, Integer> locks;

	public NexMethod(boolean upgradedDeathCharge, Map<String, Integer> locks)
	{
		if (locks == null || locks.size() > 33) { throw new IllegalArgumentException("Invalid method locks"); }
		locks.forEach((key, item) -> {
			String[] parts = key.split("/", -1);
			if (parts.length != 2 || !ROLES.contains(parts[0]) || !PoweredMagic.SLOTS.contains(parts[1])
				|| item == null || item < 0 || item > 1000000) { throw new IllegalArgumentException("Invalid method lock"); }
			if (parts[1].equals("ring") && item!=25975) { throw new IllegalArgumentException("Pilot fixes Lightbearer throughout"); }
		});
		this.upgradedDeathCharge = upgradedDeathCharge;
		this.locks = Map.copyOf(locks);
	}

	public NexMethod lock(String key, Integer item)
	{
		Map<String, Integer> next = new HashMap<>(locks);
		if (item == null) { next.remove(key); } else { next.put(key, item); }
		return new NexMethod(upgradedDeathCharge, next);
	}
	public String cohort() { return ID + ":" + upgradedDeathCharge + ":" + new java.util.TreeMap<>(locks); }
}
