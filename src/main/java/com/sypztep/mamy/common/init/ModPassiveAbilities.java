package com.sypztep.mamy.common.init;

import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.passive.abilities.agility.*;
import com.sypztep.mamy.common.system.passive.abilities.dexterity.*;
import com.sypztep.mamy.common.system.passive.abilities.vitality.*;
import com.sypztep.mamy.common.system.passive.abilities.strength.*;
import com.sypztep.mamy.common.system.passive.abilities.intelligence.*;
import com.sypztep.mamy.common.system.passive.abilities.luck.*;
import com.sypztep.mamy.common.system.stat.StatTypes;

import java.util.*;

public interface ModPassiveAbilities {
    Map<String, PassiveAbility> ABILITIES = new LinkedHashMap<>();

    // === AGILITY ABILITIES ===
    PassiveAbility SWIFT_FEET = register(new SwiftFeetAbility("swift_feet",Map.of(StatTypes.AGILITY, 5)));
    PassiveAbility QUICK_REFLEX = register(new QuickReflexPassive("quick_reflex",Map.of(StatTypes.AGILITY, 15)));
    PassiveAbility WIND_WALKER = register(new WindWalkerAbility("wind_walker",Map.of(StatTypes.AGILITY, 30)));
    PassiveAbility SHADOW_STEP = register(new ShadowStepAbility("shadow_step",Map.of(StatTypes.AGILITY, 50)));

    // === DEXTERITY ABILITIES ===
    PassiveAbility PRECISION_STRIKES = register(new PrecisionStrikesAbility("precision_strikes", Map.of(StatTypes.DEXTERITY, 10)));
    PassiveAbility HEADHUNTER = register(new HeadhunterAbility("headhunter",Map.of(StatTypes.AGILITY, 20)));
    PassiveAbility STEADY_AIM = register(new SteadyAimAbility("steady_aim", Map.of(StatTypes.DEXTERITY, 35)));

    // === VITALITY ABILITIES ===
    PassiveAbility IRON_SKIN = register(new IronSkinAbility("iron_skin", Map.of(StatTypes.VITALITY, 8)));
    PassiveAbility REGENERATION = register(new RegenerationAbility("regeneration", Map.of(StatTypes.VITALITY, 20)));
    PassiveAbility LAST_STAND = register(new LastStandAbility("last_stand", Map.of(StatTypes.VITALITY, 40)));

    // === STRENGTH ABILITIES ===
    PassiveAbility HEAVY_HITTER = register(new HeavyHitterAbility("heavy_hitter", Map.of(StatTypes.STRENGTH, 12)));
    PassiveAbility BERSERKER = register(new BerserkerAbility("berserker", Map.of(StatTypes.STRENGTH, 30)));
    PassiveAbility WEAPON_MASTER = register(new WeaponMasterAbility("weapon_master", Map.of(StatTypes.STRENGTH, 45)));

    // === INTELLIGENCE ABILITIES ===
    PassiveAbility ARCANE_POWER = register(new ArcanePowerAbility("arcane_power", Map.of(StatTypes.INTELLIGENCE, 15)));
    PassiveAbility MANA_SHIELD = register(new ManaShieldAbility("mana_shield", Map.of(StatTypes.INTELLIGENCE, 25)));
    PassiveAbility SPELL_ECHO = register(new SpellEchoAbility("spell_echo", Map.of(StatTypes.INTELLIGENCE, 40)));

    // === LUCK ABILITIES ===
    PassiveAbility FORTUNE_FAVOR = register(new FortuneFavorAbility("fortune_favor", Map.of(StatTypes.LUCK, 10)));
    PassiveAbility CRITICAL_EXPERT = register(new CriticalExpertAbility("critical_expert", Map.of(StatTypes.LUCK, 25)));
    PassiveAbility LUCKY_STRIKES = register(new LuckyStrikesAbility("lucky_strikes", Map.of(StatTypes.LUCK, 40)));

    // === MULTI-STAT ABILITIES ===
//    PassiveAbility ELEMENTAL_IMMUNITY = register(new ElementalImmunityAbility("elemental_immunity", Map.of(StatTypes.VITALITY, 25,
//            StatTypes.INTELLIGENCE, 15)));
//    PassiveAbility COMBAT_VETERAN = register(new CombatVeteranAbility("combat_veteran", Map.of(StatTypes.STRENGTH, 25,
//            StatTypes.AGILITY, 20,
//            StatTypes.VITALITY, 20)));
//    PassiveAbility LEGENDARY_WARRIOR = register(new LegendaryWarriorAbility("legendary_warrior", Map.of(StatTypes.STRENGTH, 50,
//            StatTypes.AGILITY, 40,
//            StatTypes.VITALITY, 45,
//            StatTypes.DEXTERITY, 35,
//            StatTypes.INTELLIGENCE, 30,
//            StatTypes.LUCK, 40)));

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

    static List<PassiveAbility> getAbilitiesGroupedByStatThenLevel() {
        return ABILITIES.values().stream()
                .sorted(Comparator
                        // Sort/group by the first stat in requirements map (or null-safe)
                        .comparing((PassiveAbility a) -> a.getRequirements().keySet().stream()
                                .findFirst()
                                .map(Enum::ordinal) // StatTypes enum order
                                .orElse(Integer.MAX_VALUE))
                        // Then by required level (lowest first)
                        .thenComparing(a -> a.getRequirements().values().stream()
                                .mapToInt(Integer::intValue)
                                .max().orElse(0))
                )
                .toList();
    }

}