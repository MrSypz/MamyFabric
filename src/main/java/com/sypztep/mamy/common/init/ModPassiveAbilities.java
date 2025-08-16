package com.sypztep.mamy.common.init;

import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.passive.abilities.agility.*;
import com.sypztep.mamy.common.system.passive.abilities.dexterity.*;
import com.sypztep.mamy.common.system.passive.abilities.intelligence.*;
import com.sypztep.mamy.common.system.passive.abilities.luck.*;
import com.sypztep.mamy.common.system.passive.abilities.strength.*;
import com.sypztep.mamy.common.system.passive.abilities.vitality.*;
import com.sypztep.mamy.common.system.stat.StatTypes;

import java.util.*;

public interface ModPassiveAbilities {
    Map<String, PassiveAbility> ABILITIES = new LinkedHashMap<>();

    // === AGILITY ABILITIES (EVASION + MOBILITY) ===
    PassiveAbility SWIFT_FEET = register(new SwiftFeetAbility("swift_feet", Map.of(StatTypes.AGILITY, 10)));
    PassiveAbility QUICK_REFLEX = register(new QuickReflexPassive("quick_reflex", Map.of(StatTypes.AGILITY, 20)));
    PassiveAbility WIND_WALKER = register(new WindWalkerAbility("wind_walker", Map.of(StatTypes.AGILITY, 30)));
    PassiveAbility SHADOW_STEP = register(new ShadowStepAbility("shadow_step", Map.of(StatTypes.AGILITY, 50)));
    PassiveAbility PHANTOM_WALKER = register(new PhantomWalkerAbility("phantom_walker", Map.of(StatTypes.AGILITY, 75)));
    PassiveAbility VOID_DANCER = register(new ShadowDasherAbility("void_dancer", Map.of(StatTypes.AGILITY, 99)));

    // === DEXTERITY ABILITIES (ACCURACY + PRECISION) ===
    PassiveAbility PRECISION_STRIKES = register(new PrecisionStrikesAbility("precision_strikes", Map.of(StatTypes.DEXTERITY, 10)));
    PassiveAbility HEADHUNTER = register(new HeadhunterAbility("headhunter", Map.of(StatTypes.DEXTERITY, 20)));
    PassiveAbility STEADY_AIM = register(new SteadyAimAbility("steady_aim", Map.of(StatTypes.DEXTERITY, 30)));
    PassiveAbility MARKS_MAN = register(new MarksmanAbility("marks_man", Map.of(StatTypes.DEXTERITY, 50)));
    PassiveAbility LETHAL_PRECISION = register(new LethalPrecisionAbility("lethal_precision", Map.of(StatTypes.DEXTERITY, 75)));
    PassiveAbility RICOCHET_MASTER = register(new RicochetMasterAbility("ricochet_master", Map.of(StatTypes.DEXTERITY, 99)));

    // === VITALITY ABILITIES (TANK + UTILITY) ===
    PassiveAbility HARDY_CONSTITUTION = register(new HardyConstitutionAbility("hardy_constitution", Map.of(StatTypes.VITALITY, 10)));
    PassiveAbility RAPID_REGENERATION = register(new RapidRegenerationAbility("rapid_regeneration", Map.of(StatTypes.VITALITY, 20)));
    PassiveAbility IRON_SKIN = register(new IronSkinAbility("iron_skin", Map.of(StatTypes.VITALITY, 30)));
    PassiveAbility GUARDIAN_SPIRIT = register(new GuardianSpiritAbility("guardian_spirit", Map.of(StatTypes.VITALITY, 50)));
    PassiveAbility UNDYING_WILL = register(new UndyingWillAbility("undying_will", Map.of(StatTypes.VITALITY, 75)));
    PassiveAbility IMMORTAL_FORTRESS = register(new ImmortalFortressAbility("immortal_fortress", Map.of(StatTypes.VITALITY, 99)));

    // === STRENGTH ABILITIES (DAMAGE + UTILITY) ===
    PassiveAbility BRUTAL_STRIKES = register(new BrutalStrikesAbility("brutal_strikes", Map.of(StatTypes.STRENGTH, 10)));
    PassiveAbility DEVASTATING_BLOWS = register(new DevastatingBlowsAbility("devastating_blows", Map.of(StatTypes.STRENGTH, 20)));
    PassiveAbility BACK_BREAKER = register(new BackBreakerAbility("back_breaker", Map.of(StatTypes.STRENGTH, 30)));
    PassiveAbility BERSERKER_RAGE = register(new BerserkerRageAbility("berserker_rage", Map.of(StatTypes.STRENGTH, 50)));
    PassiveAbility WEAPON_MASTER = register(new WeaponMasterAbility("weapon_master", Map.of(StatTypes.STRENGTH, 75)));
    PassiveAbility TITAN_STRENGTH = register(new TitanStrengthAbility("titan_strength", Map.of(StatTypes.STRENGTH, 99)));

    // === INTELLIGENCE ABILITIES (MAGIC + UTILITY) ===
    PassiveAbility MANA_EFFICIENCY = register(new ManaEfficiencyAbility("mana_efficiency", Map.of(StatTypes.INTELLIGENCE, 10)));
    PassiveAbility ELEMENTAL_AFFINITY = register(new ElementalAffinityAbility("elemental_affinity", Map.of(StatTypes.INTELLIGENCE, 20)));
    PassiveAbility ARCANE_INTELLECT = register(new ArcaneIntellectAbility("arcane_intellect", Map.of(StatTypes.INTELLIGENCE, 30)));
    PassiveAbility SPELL_PENETRATION = register(new SpellPenetrationAbility("spell_penetration", Map.of(StatTypes.INTELLIGENCE, 50)));
    PassiveAbility ELEMENTAL_MASTERY = register(new ElementalMasteryAbility("elemental_mastery", Map.of(StatTypes.INTELLIGENCE, 75)));
    PassiveAbility ARCHMAGE_POWER = register(new ArchmagePowerAbility("archmage_power", Map.of(StatTypes.INTELLIGENCE, 99)));

    // === LUCK ABILITIES (BALANCE + FORTUNE) ===
    PassiveAbility LUCKY_STRIKES = register(new LuckyStrikesAbility("lucky_strikes", Map.of(StatTypes.LUCK, 10)));
    PassiveAbility FORTUNE_FINDER = register(new FortuneFinderAbility("fortune_finder", Map.of(StatTypes.LUCK, 20)));
    PassiveAbility CRITICAL_MASTERY = register(new CriticalMasteryAbility("critical_mastery", Map.of(StatTypes.LUCK, 30)));
    PassiveAbility DESTINED_STRIKES = register(new DestinedStrikesAbility("destined_strikes", Map.of(StatTypes.LUCK, 50)));
    PassiveAbility PROBABILITY_MASTER = register(new ProbabilityMasterAbility("probability_master", Map.of(StatTypes.LUCK, 75)));
    PassiveAbility FATE_WEAVER = register(new FateWeaverAbility("fate_weaver", Map.of(StatTypes.LUCK, 99)));

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