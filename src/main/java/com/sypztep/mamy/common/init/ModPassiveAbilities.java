package com.sypztep.mamy.common.init;

import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.passive.abilities.*;
import com.sypztep.mamy.common.system.stat.StatTypes;

import java.util.*;

public final class ModPassiveAbilities {
    private static final Map<String, PassiveAbility> ABILITIES = new LinkedHashMap<>();

    // === AGILITY ABILITIES ===
    public static final PassiveAbility SWIFT_FEET = register(new SwiftFeetAbility());
    public static final PassiveAbility DODGE_MASTER = register(new DodgeMasterAbility());
    public static final PassiveAbility WIND_WALKER = register(new WindWalkerAbility());

    // === DEXTERITY ABILITIES ===
    public static final PassiveAbility HEADHUNTER = register(new HeadhunterAbility());
    public static final PassiveAbility PRECISION_STRIKES = register(new PrecisionStrikesAbility());
    public static final PassiveAbility STEADY_AIM = register(new SteadyAimAbility());

    // === VITALITY ABILITIES ===
    public static final PassiveAbility IRON_SKIN = register(new IronSkinAbility());
    public static final PassiveAbility REGENERATION = register(new RegenerationAbility());
    public static final PassiveAbility LAST_STAND = register(new LastStandAbility());

    // === STRENGTH ABILITIES ===
    public static final PassiveAbility HEAVY_HITTER = register(new HeavyHitterAbility());
    public static final PassiveAbility BERSERKER = register(new BerserkerAbility());
    public static final PassiveAbility WEAPON_MASTER = register(new WeaponMasterAbility());

    // === INTELLIGENCE ABILITIES ===
    public static final PassiveAbility ARCANE_POWER = register(new ArcanePowerAbility());
    public static final PassiveAbility MANA_SHIELD = register(new ManaShieldAbility());
    public static final PassiveAbility SPELL_ECHO = register(new SpellEchoAbility());

    // === LUCK ABILITIES ===
    public static final PassiveAbility FORTUNE_FAVOR = register(new FortuneFavorAbility());
    public static final PassiveAbility CRITICAL_EXPERT = register(new CriticalExpertAbility());
    public static final PassiveAbility LUCKY_STRIKES = register(new LuckyStrikesAbility());

    // === MULTI-STAT ABILITIES ===
    public static final PassiveAbility ELEMENTAL_IMMUNITY = register(new ElementalImmunityAbility());
    public static final PassiveAbility COMBAT_VETERAN = register(new CombatVeteranAbility());
    public static final PassiveAbility LEGENDARY_WARRIOR = register(new LegendaryWarriorAbility());

    private static PassiveAbility register(PassiveAbility ability) {
        ABILITIES.put(ability.getId(), ability);
        return ability;
    }

    public static PassiveAbility getAbility(String id) {
        return ABILITIES.get(id);
    }

    public static Collection<PassiveAbility> getAllAbilities() {
        return ABILITIES.values();
    }

    public static List<String> getAllAbilityIds() {
        return new ArrayList<>(ABILITIES.keySet());
    }

    public static PassiveAbility getAbilityByName(String name) {
        return ABILITIES.values().stream()
                .filter(ability -> ability.getDisplayName().getString().equalsIgnoreCase(name)
                        || ability.getId().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get abilities by stat requirement
     */
    public static List<PassiveAbility> getAbilitiesForStat(StatTypes statType) {
        return ABILITIES.values().stream()
                .filter(ability -> ability.getRequirements().containsKey(statType))
                .toList();
    }

    /**
     * Get abilities ordered by unlock level (lowest requirements first)
     */
    public static List<PassiveAbility> getAbilitiesOrderedByLevel() {
        return ABILITIES.values().stream()
                .sorted((a, b) -> {
                    int aMax = a.getRequirements().values().stream().mapToInt(Integer::intValue).max().orElse(0);
                    int bMax = b.getRequirements().values().stream().mapToInt(Integer::intValue).max().orElse(0);
                    return Integer.compare(aMax, bMax);
                })
                .toList();
    }
}