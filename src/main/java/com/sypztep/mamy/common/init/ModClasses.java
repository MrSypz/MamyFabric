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

    // ===== TIER 0 (Starting Class) - EXTREME CHALLENGE =====
    PlayerClass NOVICE = register(PlayerClass
            .create("novice", 0, 0, "Novice", Formatting.GRAY, ResourceType.MANA,
                    "A beginning adventurer with no specialized skills - fragile but determined")
            .attributes(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, 8.0
            ))
            .growthFactors(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(1.5),  // Grows faster
                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(3.0),         // Better scaling
                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.2)
            ))
            .resource(200)  // Base resource for scaling
            .maxLevel(10)
            .build());

    // ===== TIER 1 (First Job Classes) - BALANCED CHALLENGE =====

    PlayerClass SWORDMAN = register(PlayerClass
            .create("swordman", 1, 1, "Swordman", Formatting.RED, ResourceType.RAGE,
                    "A warrior who trades magic for martial prowess")
            .attributes(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, 44.0
            ))
            .growthFactors(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(3.5),  // Best health growth
                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(1.8),         // Moderate rage growth
                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.12)
            ))
            .resource(260)  // 1.3x base - lowest but adequate for melee
            .jobBonuses((short)7, (short)2, (short)4, (short)0, (short)3, (short)2)
            .build()
            .addRequirement(NOVICE, 10));

    PlayerClass MAGE = register(PlayerClass
            .create("mage", 1, 2, "Mage", Formatting.BLUE, ResourceType.MANA,
                    "A glass cannon wielding devastating arcane power")
            .attributes(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, 32.0
            ))
            .growthFactors(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(1.2),  // Slow health growth
                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(8.0),         // Massive mana growth
                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.6)
            ))
            .resource(1340)  // 6.7x base - MAXIMUM resource pool
            .jobBonuses((short)0, (short)4, (short)0, (short)8, (short)3, (short)3)
            .build()
            .addRequirement(NOVICE, 10));

    PlayerClass ARCHER = register(PlayerClass
            .create("archer", 1, 3, "Archer", Formatting.GREEN, ResourceType.MANA,
                    "A ranged specialist with mobility and precision")
            .attributes(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, 36.0
            ))
            .growthFactors(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(2.2),  // Moderate growth
                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(3.5),         // Good mana for skills
                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.3)
            ))
            .resource(520)  // 2.6x base - medium resource needs
            .jobBonuses((short)3, (short)3, (short)1, (short)2, (short)7, (short)2)
            .build()
            .addRequirement(NOVICE, 10));

    PlayerClass ACOLYTE = register(PlayerClass
            .create("acolyte", 1, 4, "Acolyte", Formatting.GOLD, ResourceType.MANA,
                    "A divine hybrid balancing healing magic and survivability")
            .attributes(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, 38.0
            ))
            .growthFactors(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(2.0),  // Balanced growth
                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(5.5),         // High mana for healing
                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.45)
            ))
            .resource(1000)  // 5.0x base - high mana for healing/support
            .jobBonuses((short)3, (short)2, (short)3, (short)3, (short)3, (short)4)
            .build()
            .addRequirement(NOVICE, 10));

    PlayerClass THIEF = register(PlayerClass
            .create("thief", 1, 5, "Thief", Formatting.DARK_GRAY, ResourceType.RAGE,
                    "An agile assassin who relies on speed over brute force")
            .attributes(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, 34.0
            ))
            .growthFactors(Map.of(
                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(1.5),  // Slowest growth
                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(2.5),         // Moderate rage for skills
                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.35)   // Fast regen for combos
            ))
            .resource(340)  // 1.7x base - enough for ability chains
            .jobBonuses((short)4, (short)4, (short)2, (short)1, (short)4, (short)3)
            .build()
            .addRequirement(NOVICE, 10));

    // ===== TIER 2 (Second Job Classes) =====
//    PlayerClass KNIGHT = register(PlayerClass
//            .create("knight", 2, 1, "Knight", Formatting.GOLD, ResourceType.RAGE,
//                    "A noble warrior sworn to protect others")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 120.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(5.0),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(2.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.12)
//            ))
//            .resource(200)
//            .jobBonuses((short)10, (short)3, (short)8, (short)0, (short)4, (short)3)
//            .maxLevel(50)
//            .build()
//            .addRequirement(SWORDMAN, 40));
//
//    PlayerClass CRUSADER = register(PlayerClass
//            .create("crusader", 2, 2, "Crusader", Formatting.YELLOW, ResourceType.MANA,
//                    "A holy warrior who blends sword and divine magic")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 100.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(4.0),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(5.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.35)
//            ))
//            .resource(300)
//            .jobBonuses((short)6, (short)3, (short)6, (short)4, (short)4, (short)5)
//            .maxLevel(50)
//            .build()
//            .addRequirement(SWORDMAN, 40));
//
//    PlayerClass WIZARD = register(PlayerClass
//            .create("wizard", 2, 3, "Wizard", Formatting.DARK_BLUE, ResourceType.MANA,
//                    "A master of elemental magic and arcane knowledge")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 60.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(1.5),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(10.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.8)
//            ))
//            .resource(500)
//            .jobBonuses((short)0, (short)5, (short)0, (short)12, (short)4, (short)4)
//            .maxLevel(50)
//            .build()
//            .addRequirement(MAGE, 40));
//
//    PlayerClass SAGE = register(PlayerClass
//            .create("sage", 2, 4, "Sage", Formatting.LIGHT_PURPLE, ResourceType.MANA,
//                    "A scholar who masters both magic and knowledge")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 70.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(2.0),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(8.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.6)
//            ))
//            .resource(450)
//            .jobBonuses((short)0, (short)4, (short)2, (short)10, (short)5, (short)6)
//            .maxLevel(50)
//            .build()
//            .addRequirement(MAGE, 40));
//
//    PlayerClass HUNTER = register(PlayerClass
//            .create("hunter", 2, 5, "Hunter", Formatting.DARK_GREEN, ResourceType.RAGE,
//                    "A master tracker skilled in hunting beasts")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 90.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(3.0),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(5.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.4)
//            ))
//            .resource(350)
//            .jobBonuses((short)4, (short)5, (short)2, (short)2, (short)10, (short)3)
//            .maxLevel(50)
//            .build()
//            .addRequirement(ARCHER, 40));
//
//    PlayerClass BARD = register(PlayerClass
//            .create("bard", 2, 6, "Bard", Formatting.AQUA, ResourceType.MANA,
//                    "A performer who supports allies with songs")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 75.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(2.5),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(6.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.5)
//            ))
//            .resource(400)
//            .jobBonuses((short)2, (short)6, (short)1, (short)4, (short)8, (short)5)
//            .maxLevel(50)
//            .build()
//            .addRequirement(ARCHER, 40));
//
//    PlayerClass PRIEST = register(PlayerClass
//            .create("priest", 2, 7, "Priest", Formatting.WHITE, ResourceType.MANA,
//                    "A divine healer dedicated to supporting others")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 80.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(2.5),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(7.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.9)
//            ))
//            .resource(450)
//            .jobBonuses((short)3, (short)3, (short)4, (short)6, (short)4, (short)8)
//            .maxLevel(50)
//            .build()
//            .addRequirement(ACOLYTE, 40));
//
//    PlayerClass MONK = register(PlayerClass
//            .create("monk", 2, 8, "Monk", Formatting.DARK_AQUA, ResourceType.RAGE,
//                    "A martial artist who fights with fists and spirit")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 95.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(3.5),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(3.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.5)
//            ))
//            .resource(250)
//            .jobBonuses((short)8, (short)6, (short)5, (short)2, (short)3, (short)4)
//            .maxLevel(50)
//            .build()
//            .addRequirement(ACOLYTE, 40));
//
//    PlayerClass ASSASSIN = register(PlayerClass
//            .create("assassin", 2, 9, "Assassin", Formatting.BLACK, ResourceType.RAGE,
//                    "A shadow warrior who strikes from darkness")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 70.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(2.0),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(3.5),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.5)
//            ))
//            .resource(250)
//            .jobBonuses((short)6, (short)8, (short)3, (short)1, (short)6, (short)4)
//            .maxLevel(50)
//            .build()
//            .addRequirement(THIEF, 40));
//
//    PlayerClass ROGUE = register(PlayerClass
//            .create("rogue", 2, 10, "Rogue", Formatting.GRAY, ResourceType.RAGE,
//                    "A versatile fighter skilled in tricks and traps")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 65.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(1.8),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(4.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.4)
//            ))
//            .resource(280)
//            .jobBonuses((short)5, (short)6, (short)2, (short)3, (short)5, (short)7)
//            .maxLevel(50)
//            .build()
//            .addRequirement(THIEF, 40));
//
//    // ===== TIER 3 (Transcendent Jobs) =====
//    PlayerClass PALADIN = register(PlayerClass
//            .create("paladin", 3, 1, "Paladin", Formatting.AQUA, ResourceType.MANA,
//                    "A transcendent holy warrior with divine powers")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 200.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(8.0),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(4.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.3)
//            ))
//            .resource(350)
//            .jobBonuses((short)15, (short)4, (short)12, (short)6, (short)5, (short)6)
//            .maxLevel(50)
//            .transcendent()
//            .build()
//            .addTranscendentRequirement(KNIGHT, 50));
//
//    PlayerClass LORD_KNIGHT = register(PlayerClass
//            .create("lord_knight", 3, 2, "Lord Knight", Formatting.WHITE, ResourceType.RAGE,
//                    "A transcendent knight with legendary prowess")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 180.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(7.0),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(3.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.2)
//            ))
//            .resource(300)
//            .jobBonuses((short)18, (short)4, (short)10, (short)0, (short)6, (short)4)
//            .maxLevel(50)
//            .transcendent()
//            .build()
//            .addTranscendentRequirement(KNIGHT, 50));
//
//    PlayerClass HIGH_WIZARD = register(PlayerClass
//            .create("high_wizard", 3, 3, "High Wizard", Formatting.LIGHT_PURPLE, ResourceType.MANA,
//                    "A transcendent master of arcane magic")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 80.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(2.0),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(15.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(1.2)
//            ))
//            .resource(700)
//            .jobBonuses((short)0, (short)6, (short)0, (short)18, (short)5, (short)5)
//            .maxLevel(50)
//            .transcendent()
//            .build()
//            .addTranscendentRequirement(WIZARD, 50));
//
//    PlayerClass PROFESSOR = register(PlayerClass
//            .create("professor", 3, 4, "Professor", Formatting.DARK_PURPLE, ResourceType.MANA,
//                    "A transcendent scholar with ultimate knowledge")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 100.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(3.0),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(12.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(1.0)
//            ))
//            .resource(600)
//            .jobBonuses((short)0, (short)5, (short)3, (short)15, (short)6, (short)9)
//            .maxLevel(50)
//            .transcendent()
//            .build()
//            .addTranscendentRequirement(SAGE, 50));
//
//    PlayerClass SNIPER = register(PlayerClass
//            .create("sniper", 3, 5, "Sniper", Formatting.DARK_GREEN, ResourceType.RAGE,
//                    "A transcendent marksman with perfect accuracy")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 140.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(4.5),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(8.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.6)
//            ))
//            .resource(500)
//            .jobBonuses((short)5, (short)8, (short)3, (short)3, (short)15, (short)4)
//            .maxLevel(50)
//            .transcendent()
//            .build()
//            .addTranscendentRequirement(HUNTER, 50));
//
//    PlayerClass MAESTRO = register(PlayerClass
//            .create("maestro", 3, 6, "Maestro", Formatting.GOLD, ResourceType.MANA,
//                    "A transcendent performer with divine melodies")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 120.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(3.5),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(10.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.8)
//            ))
//            .resource(550)
//            .jobBonuses((short)3, (short)9, (short)2, (short)6, (short)12, (short)8)
//            .maxLevel(50)
//            .transcendent()
//            .build()
//            .addTranscendentRequirement(BARD, 50));
//
//    PlayerClass HIGH_PRIEST = register(PlayerClass
//            .create("high_priest", 3, 7, "High Priest", Formatting.YELLOW, ResourceType.MANA,
//                    "A transcendent master of divine magic and healing")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 120.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(3.5),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(12.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(1.5)
//            ))
//            .resource(600)
//            .jobBonuses((short)4, (short)4, (short)6, (short)9, (short)5, (short)12)
//            .maxLevel(50)
//            .transcendent()
//            .build()
//            .addTranscendentRequirement(PRIEST, 50));
//
//    PlayerClass CHAMPION = register(PlayerClass
//            .create("champion", 3, 8, "Champion", Formatting.RED, ResourceType.RAGE,
//                    "A transcendent martial artist with ultimate combat skills")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 160.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(5.5),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(6.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.7)
//            ))
//            .resource(400)
//            .jobBonuses((short)12, (short)9, (short)8, (short)3, (short)4, (short)6)
//            .maxLevel(50)
//            .transcendent()
//            .build()
//            .addTranscendentRequirement(MONK, 50));
//
//    PlayerClass ASSASSIN_CROSS = register(PlayerClass
//            .create("assassin_cross", 3, 9, "Assassin Cross", Formatting.DARK_RED, ResourceType.RAGE,
//                    "A transcendent shadow assassin with deadly skills")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 100.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(3.0),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(6.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.8)
//            ))
//            .resource(400)
//            .jobBonuses((short)9, (short)12, (short)4, (short)2, (short)9, (short)6)
//            .maxLevel(50)
//            .transcendent()
//            .build()
//            .addTranscendentRequirement(ASSASSIN, 50));
//
//    PlayerClass SHADOW_CHASER = register(PlayerClass
//            .create("shadow_chaser", 3, 10, "Shadow Chaser", Formatting.DARK_GRAY, ResourceType.RAGE,
//                    "A transcendent trickster master of stealth and deception")
//            .attributes(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, 100.0
//            ))
//            .growthFactors(Map.of(
//                    EntityAttributes.GENERIC_MAX_HEALTH, GrowthFactor.flat(3.0),
//                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(6.0),
//                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(0.8)
//            ))
//            .resource(400)
//            .jobBonuses((short)8, (short)9, (short)3, (short)4, (short)8, (short)10)
//            .maxLevel(50)
//            .transcendent()
//            .build()
//            .addTranscendentRequirement(ROGUE, 50));

    private static PlayerClass register(PlayerClass playerClass) {
        CLASSES.put(playerClass.getId(), playerClass);
        return playerClass;
    }
}
