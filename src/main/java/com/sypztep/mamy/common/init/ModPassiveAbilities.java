package com.sypztep.mamy.common.init;

import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.passive.abilities.agility.*;
import com.sypztep.mamy.common.system.passive.abilities.dexterity.*;
import com.sypztep.mamy.common.system.passive.abilities.vitality.*;
import com.sypztep.mamy.common.system.passive.abilities.strength.*;
import com.sypztep.mamy.common.system.passive.abilities.intelligence.*;
import com.sypztep.mamy.common.system.passive.abilities.luck.*;
import com.sypztep.mamy.common.system.passive.abilities.mixes.*;
import com.sypztep.mamy.common.system.stat.StatTypes;

import java.util.*;

public interface ModPassiveAbilities {
    Map<String, PassiveAbility> ABILITIES = new LinkedHashMap<>();

    // === AGILITY ABILITIES ===
    PassiveAbility SWIFT_FEET = register(new SwiftFeetAbility("swift_feet",Map.of(StatTypes.AGILITY, 5)));
    PassiveAbility DODGE_MASTER = register(new DodgeMasterAbility("dodge_master",Map.of(StatTypes.AGILITY, 15)));
    PassiveAbility QUICK_REFLEX = register(new QuickReflexPassive("quick_reflex",Map.of(StatTypes.AGILITY, 20)));
    PassiveAbility WIND_WALKER = register(new WindWalkerAbility("wind_walker",Map.of(StatTypes.AGILITY, 30)));
    PassiveAbility SHADOW_STEP = register(new ShadowStepPassiveAbility("shadow_step",Map.of(StatTypes.AGILITY, 50)));

    // === DEXTERITY ABILITIES ===
    PassiveAbility HEADHUNTER = register(new HeadhunterAbility());
    PassiveAbility PRECISION_STRIKES = register(new PrecisionStrikesAbility());
    PassiveAbility STEADY_AIM = register(new SteadyAimAbility());

    // === VITALITY ABILITIES ===
    PassiveAbility IRON_SKIN = register(new IronSkinAbility());
    PassiveAbility REGENERATION = register(new RegenerationAbility());
    PassiveAbility LAST_STAND = register(new LastStandAbility());

    // === STRENGTH ABILITIES ===
    PassiveAbility HEAVY_HITTER = register(new HeavyHitterAbility());
    PassiveAbility BERSERKER = register(new BerserkerAbility());
    PassiveAbility WEAPON_MASTER = register(new WeaponMasterAbility());

    // === INTELLIGENCE ABILITIES ===
    PassiveAbility ARCANE_POWER = register(new ArcanePowerAbility());
    PassiveAbility MANA_SHIELD = register(new ManaShieldAbility());
    PassiveAbility SPELL_ECHO = register(new SpellEchoAbility());

    // === LUCK ABILITIES ===
    PassiveAbility FORTUNE_FAVOR = register(new FortuneFavorAbility());
    PassiveAbility CRITICAL_EXPERT = register(new CriticalExpertAbility());
    PassiveAbility LUCKY_STRIKES = register(new LuckyStrikesAbility());

    // === MULTI-STAT ABILITIES ===
    PassiveAbility ELEMENTAL_IMMUNITY = register(new ElementalImmunityAbility());
    PassiveAbility COMBAT_VETERAN = register(new CombatVeteranAbility());
    PassiveAbility LEGENDARY_WARRIOR = register(new LegendaryWarriorAbility());

    private static PassiveAbility register(PassiveAbility ability) {
        ABILITIES.put(ability.getId(), ability);
        return ability;
    }

    static PassiveAbility getAbility(String id) {
        return ABILITIES.get(id);
    }

    static Collection<PassiveAbility> getAllAbilities() {
        return ABILITIES.values();
    }

    static List<String> getAllAbilityIds() {
        return new ArrayList<>(ABILITIES.keySet());
    }

    static PassiveAbility getAbilityByName(String name) {
        return ABILITIES.values().stream()
                .filter(ability -> ability.getDisplayName().getString().equalsIgnoreCase(name)
                        || ability.getId().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get abilities by stat requirement
     */
    static List<PassiveAbility> getAbilitiesForStat(StatTypes statType) {
        return ABILITIES.values().stream()
                .filter(ability -> ability.getRequirements().containsKey(statType))
                .toList();
    }

    /**
     * Get abilities ordered by unlock level (lowest requirements first)
     */
    static List<PassiveAbility> getAbilitiesOrderedByLevel() {
        return ABILITIES.values().stream()
                .sorted((a, b) -> {
                    int aMax = a.getRequirements().values().stream().mapToInt(Integer::intValue).max().orElse(0);
                    int bMax = b.getRequirements().values().stream().mapToInt(Integer::intValue).max().orElse(0);
                    return Integer.compare(aMax, bMax);
                })
                .toList();
    }
}