package com.ironpath.advisor;

import java.util.List;
import java.util.Map;

/** Reviewed stat facts and explicit family mechanics; no executable catalogue rules. */
public final class RangedItem
{
	public enum Weapon { BOWFA, CRYSTAL, TWISTED, SHORTBOW, CROSSBOW, DRAGON_HUNTER, TOXIC, ROSEWOOD }
	public enum Ammo { NONE, ARROW, BOLT, DART }
	public enum DefenceType { LIGHT, STANDARD, HEAVY }
	public enum SetPart { NONE, VOID_HEAD, VOID_BODY, VOID_LEGS, VOID_HANDS, ELITE_BODY, ELITE_LEGS }
	public int id, attack, strength, ticks, maxAmmoTier, ammoTier, crystalWeight;
	public String label, slot;
	public Weapon weapon;
	public Ammo ammo = Ammo.NONE;
	public DefenceType defenceType;
	public SetPart setPart = SetPart.NONE;
	public boolean twoHanded, seeking, slayerHelmet, chargedQuiver, boltEffect;
	public Map<String, Integer> requirements;
	public List<String> sources;
}
