package com.sypztep.mamy.common.init;

import com.sypztep.mamy.common.system.classes.GrowthFactor;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.classes.ResourceType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

public interface ModClasses {
    Map<String, PlayerClass> CLASSES = new HashMap<>();

    // ===== TIER 0 (Starting Class) =====
    PlayerClass NOVICE = register(PlayerClass
            .create("novice", 0, 0, "Novice", Formatting.GRAY, ResourceType.MANA,
                    "A beginning adventurer with no specialized skills")
            .attributes(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, 10.0
                    ))
            .maxLevel(10)
            .build());

    // ===== TIER 1 (First Job Classes) =====
    PlayerClass SWORDMAN = register(PlayerClass
            .create("swordman", 1, 1, "Swordman", Formatting.RED, ResourceType.RAGE,
                    "A warrior who has chosen the path of the sword")
            .attributes(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, 40.0,
                    EntityAttributes.GENERIC_ARMOR, 4.0,
                    ModEntityAttributes.MELEE_ATTACK_DAMAGE, 0.15
            ))
            .growthFactors(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.percent(0.12),
                    ModEntityAttributes.RESOURCE, GrowthFactor.percent(0.04),
                    ModEntityAttributes.MELEE_ATTACK_DAMAGE, GrowthFactor.flat(0.8),
                    EntityAttributes.GENERIC_ARMOR, GrowthFactor.flat(0.1)
            ))
            .resource(150)
            .jobBonuses((short)7, (short)2, (short)4, (short)0, (short)3, (short)2)
            .build()
            .addRequirement(NOVICE, 10));

    PlayerClass MAGE = register(PlayerClass
            .create("mage", 1, 2, "Mage", Formatting.BLUE, ResourceType.MANA,
                    "A spellcaster who manipulates arcane energies")
            .attributes(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, 25.0,
                    ModEntityAttributes.MAGIC_ATTACK_DAMAGE, 0.25,
                    ModEntityAttributes.RESOURCE_REGEN, 35.0
            ))
            .growthFactors(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.percent(0.05),
                    ModEntityAttributes.RESOURCE, GrowthFactor.percent(0.10),
                    ModEntityAttributes.MAGIC_ATTACK_DAMAGE, GrowthFactor.percent(0.03),
                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(2.0)
            ))
            .resource(300)
            .jobBonuses((short)0, (short)4, (short)0, (short)8, (short)3, (short)3)
            .build()
            .addRequirement(NOVICE, 10));

    PlayerClass ARCHER = register(PlayerClass
            .create("archer", 1, 3, "Archer", Formatting.GREEN, ResourceType.MANA,
                    "A ranged combatant skilled with bow and arrow")
            .attributes(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, 30.0,
                    EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.02,
                    ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE, 0.15,
                    ModEntityAttributes.CRIT_CHANCE, 0.10
            ))
            .growthFactors(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.percent(0.07),
                    ModEntityAttributes.RESOURCE, GrowthFactor.percent(0.06),
                    ModEntityAttributes.CRIT_CHANCE, GrowthFactor.flat(0.005),
                    ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE, GrowthFactor.percent(0.02),
                    EntityAttributes.GENERIC_MOVEMENT_SPEED, GrowthFactor.flat(0.001)
            ))
            .resource(200)
            .jobBonuses((short)3, (short)3, (short)1, (short)2, (short)7, (short)2)
            .build()
            .addRequirement(NOVICE, 10));

    PlayerClass ACOLYTE = register(PlayerClass
            .create("acolyte", 1, 4, "Acolyte", Formatting.GOLD, ResourceType.MANA,
                    "A holy servant dedicated to helping others")
            .attributes(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, 35.0,
                    ModEntityAttributes.MAGIC_ATTACK_DAMAGE, 0.15,
                    ModEntityAttributes.RESOURCE_REGEN, 25.0,
                    ModEntityAttributes.HEAL_EFFECTIVE, 0.20
            ))
            .growthFactors(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.percent(0.08),
                    ModEntityAttributes.RESOURCE, GrowthFactor.percent(0.08),
                    ModEntityAttributes.MAGIC_ATTACK_DAMAGE, GrowthFactor.percent(0.02),
                    ModEntityAttributes.HEAL_EFFECTIVE, GrowthFactor.flat(0.01)
            ))
            .resource(250)
            .jobBonuses((short)3, (short)2, (short)3, (short)3, (short)3, (short)4)
            .build()
            .addRequirement(NOVICE, 10));

    PlayerClass THIEF = register(PlayerClass
            .create("thief", 1, 5, "Thief", Formatting.DARK_GRAY, ResourceType.RAGE,
                    "A nimble rogue who strikes from the shadows")
            .attributes(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, 28.0,
                    EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.03,
                    ModEntityAttributes.MELEE_ATTACK_DAMAGE, 0.12,
                    ModEntityAttributes.CRIT_CHANCE, 0.15,
                    ModEntityAttributes.BACK_ATTACK, 0.30
            ))
            .growthFactors(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.percent(0.06),
                    ModEntityAttributes.RESOURCE, GrowthFactor.percent(0.05),
                    ModEntityAttributes.CRIT_CHANCE, GrowthFactor.flat(0.007),
                    EntityAttributes.GENERIC_MOVEMENT_SPEED, GrowthFactor.flat(0.0015),
                    ModEntityAttributes.BACK_ATTACK, GrowthFactor.flat(0.01)
            ))
            .resource(180)
            .jobBonuses((short)4, (short)4, (short)2, (short)1, (short)4, (short)3)
            .build()
            .addRequirement(NOVICE, 10));

//    // ===== TIER 2 (Second Job - Swordman Path) =====
//    public static final PlayerClass KNIGHT = register(new PlayerClass(
//            "knight", 2, 1, "Knight", Formatting.GOLD,
//            Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 60.0, // Higher health for tank
//                    EntityAttributes.GENERIC_ARMOR, 4.0,
//                    ModEntityAttributes.MELEE_ATTACK_DAMAGE, 0.20
//            ),
//            ResourceType.RAGE, 450f,
//            "A noble warrior sworn to protect others",
//            50, false
//    ).addRequirement(SWORDMAN, 40));
//
//    public static final PlayerClass CRUSADER = register(new PlayerClass(
//            "crusader", 2, 2, "Crusader", Formatting.YELLOW,
//            Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 50.0, // Balanced health
//                    ModEntityAttributes.MELEE_ATTACK_DAMAGE, 0.15,
//                    ModEntityAttributes.MAGIC_ATTACK_DAMAGE, 0.15 // Hybrid magic/melee
//            ),
//            ResourceType.MANA, 400f,
//            "A holy warrior who blends sword and divine magic",
//            50, false
//    ).addRequirement(SWORDMAN, 40));
//
//    // ===== TIER 2 (Second Job - Mage Path) =====
//    public static final PlayerClass WIZARD = register(new PlayerClass(
//            "wizard", 2, 3, "Wizard", Formatting.DARK_BLUE,
//            Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 35.0, // Still low health
//                    ModEntityAttributes.MAGIC_ATTACK_DAMAGE, 0.40
//            ),
//            ResourceType.MANA, 600f,
//            "A master of elemental magic and arcane knowledge",
//            50, false
//    ).addRequirement(MAGE, 40));
//
//    public static final PlayerClass WARLOCK = register(new PlayerClass(
//            "warlock", 2, 4, "Warlock", Formatting.DARK_PURPLE,
//            Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 45.0, // Slightly higher health
//                    ModEntityAttributes.MAGIC_ATTACK_DAMAGE, 0.30
//            ),
//            ResourceType.MANA, 500f,
//            "A dark sorcerer who draws power from forbidden magic",
//            50, false
//    ).addRequirement(MAGE, 40));
//
//    // ===== TIER 2 (Second Job - Archer Path) =====
//    public static final PlayerClass HUNTER = register(new PlayerClass(
//            "hunter", 2, 5, "Hunter", Formatting.DARK_GREEN,
//            Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 40.0, // Balanced health
//                    EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.04,
//                    ModEntityAttributes.CRIT_CHANCE, 0.20,
//                    ModEntityAttributes.CRIT_DAMAGE, 0.25
//            ),
//            ResourceType.RAGE, 350f,
//            "A master tracker skilled in hunting beasts",
//            50, false
//    ).addRequirement(ARCHER, 40));
//
//    public static final PlayerClass ASSASSIN = register(new PlayerClass(
//            "assassin", 2, 6, "Assassin", Formatting.BLACK,
//            Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 35.0, // Low health, high damage
//                    EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.05,
//                    ModEntityAttributes.CRIT_CHANCE, 0.25
//            ),
//            ResourceType.RAGE, 300f,
//            "A shadow warrior who strikes from darkness",
//            50, false
//    ).addRequirement(ARCHER, 40));
//
//    // ===== TIER 3 (Transcendent Jobs) =====
//    public static final PlayerClass LORD_KNIGHT = register(new PlayerClass(
//            "lord_knight", 3, 1, "Lord Knight", Formatting.WHITE,
//            Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 80.0, // Very high health
//                    EntityAttributes.GENERIC_ARMOR, 6.0,
//                    ModEntityAttributes.MELEE_ATTACK_DAMAGE, 0.35
//            ),
//            ResourceType.RAGE, 600f,
//            "A transcendent knight with legendary prowess",
//            50, true // Transcendent class
//    ).addTranscendentRequirement(KNIGHT, 50));
//
//    public static final PlayerClass PALADIN = register(new PlayerClass(
//            "paladin", 3, 2, "Paladin", Formatting.AQUA,
//            Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 70.0, // High health
//                    EntityAttributes.GENERIC_ARMOR, 5.0,
//                    ModEntityAttributes.MELEE_ATTACK_DAMAGE, 0.25,
//                    ModEntityAttributes.MAGIC_ATTACK_DAMAGE, 0.25 // Balanced hybrid
//            ),
//            ResourceType.MANA, 550f,
//            "A transcendent holy warrior with divine powers",
//            50, true
//    ).addTranscendentRequirement(CRUSADER, 50));
//
//    public static final PlayerClass HIGH_WIZARD = register(new PlayerClass(
//            "high_wizard", 3, 3, "High Wizard", Formatting.LIGHT_PURPLE,
//            Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 45.0, // Still relatively low
//                    ModEntityAttributes.MAGIC_ATTACK_DAMAGE, 0.60 // Very high magic power
//            ),
//            ResourceType.MANA, 800f,
//            "A transcendent master of arcane magic",
//            50, true
//    ).addTranscendentRequirement(WIZARD, 50));
//
//    public static final PlayerClass HIGH_PRIEST = register(new PlayerClass(
//            "high_priest", 3, 4, "High Priest", Formatting.YELLOW,
//            Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 55.0,
//                    ModEntityAttributes.MAGIC_ATTACK_DAMAGE, 0.40
//            ),
//            ResourceType.MANA, 700f,
//            "A transcendent master of divine magic and healing",
//            50, true
//    ).addTranscendentRequirement(WARLOCK, 50));
//
//    public static final PlayerClass SNIPER = register(new PlayerClass(
//            "sniper", 3, 5, "Sniper", Formatting.DARK_GREEN,
//            Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 50.0,
//                    EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.06,
//                    ModEntityAttributes.CRIT_CHANCE, 0.35,
//                    ModEntityAttributes.CRIT_DAMAGE, 0.50
//            ),
//            ResourceType.RAGE, 500f,
//            "A transcendent marksman with perfect accuracy",
//            50, true
//    ).addTranscendentRequirement(HUNTER, 50));
//
//    public static final PlayerClass ASSASSIN_CROSS = register(new PlayerClass(
//            "assassin_cross", 3, 6, "Assassin Cross", Formatting.DARK_RED,
//            Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 45.0,
//                    EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.08,
//                    ModEntityAttributes.CRIT_CHANCE, 0.40
//            ),
//            ResourceType.RAGE, 450f,
//            "A transcendent shadow assassin with deadly skills",
//            50, true
//    ).addTranscendentRequirement(ASSASSIN, 50));

    private static PlayerClass register(PlayerClass playerClass) {
        CLASSES.put(playerClass.getId(), playerClass);
        return playerClass;
    }
}
