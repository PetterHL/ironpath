package com.ironpath.advisor;

import java.util.*;
import java.util.stream.Collectors;

/** Shared N2/N3 construction from explicit slots, public style buttons and active prayers. */
public final class NexPackageFactory
{
	private final Map<Integer,MeleeItem> melee;
	public NexPackageFactory(KnowledgeCatalog catalog)
	{ melee=catalog.melee.items.stream().collect(Collectors.toUnmodifiableMap(i->i.id,i->i)); }

	public CombatSetup build(boolean isMelee,Map<String,Integer> slots,Map<String,String> origins,
		Integer style,Set<String> prayers,int loadedAmmo)
	{
		if(style==null || style<0 || style>3) { throw new IllegalArgumentException("Attack style button unread or unsupported"); }
		if(isMelee)
		{
			MeleeItem weapon=melee.get(slots.get("weapon"));
			if(weapon==null || weapon.styles.size()!=4) { throw new IllegalArgumentException("Weapon style-button mapping not reviewed for this melee weapon"); }
			MeleeModel.Prayer prayer=prayers.contains("PIETY")?MeleeModel.Prayer.PIETY
				:prayers.contains("CHIVALRY")?MeleeModel.Prayer.CHIVALRY
				:prayers.containsAll(Set.of("INCREDIBLE_REFLEXES","ULTIMATE_STRENGTH"))?MeleeModel.Prayer.INCREDIBLE_ULTIMATE:MeleeModel.Prayer.NONE;
			Set<String> lower=Set.of("CLARITY_OF_THOUGHT","IMPROVED_REFLEXES","BURST_OF_STRENGTH","SUPERHUMAN_STRENGTH","INCREDIBLE_REFLEXES","ULTIMATE_STRENGTH");
			if(prayer==MeleeModel.Prayer.NONE && prayers.stream().anyMatch(lower::contains))
			{ throw new IllegalArgumentException("Observed melee prayer combination not modelled"); }
			return CombatSetup.melee(slots,"nex-base",origins).meleeCopy(slots,origins,weapon.styles.get(style)).edit("prayer",prayer.name());
		}
		if(style>2) { throw new IllegalArgumentException("Ranged style-button mapping unreviewed"); }
		if(prayers.contains("SHARP_EYE") || prayers.contains("HAWK_EYE")) { throw new IllegalArgumentException("Observed lower-tier ranged prayer not modelled"); }
		RangedModel.Prayer prayer=prayers.contains("RIGOUR")?RangedModel.Prayer.RIGOUR
			:prayers.contains("DEADEYE")?RangedModel.Prayer.DEADEYE:prayers.contains("EAGLE_EYE")?RangedModel.Prayer.EAGLE_EYE:RangedModel.Prayer.NONE;
		return CombatSetup.ranged(slots,"nex-base",origins).rangedCopy(slots,origins,"nex-base",loadedAmmo)
			.edit("prayer",prayer.name()).edit("style",RangedModel.Style.values()[style].name());
	}
}
