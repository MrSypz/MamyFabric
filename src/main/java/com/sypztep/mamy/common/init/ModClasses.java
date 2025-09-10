package com.sypztep.mamy.common.init;

import com.sypztep.mamy.common.system.classes.GrowthFactor;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.classes.ResourceType;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

public interface ModClasses {
    Map<String, PlayerClass> CLASSES = new HashMap<>();

    PlayerClass NOVICE = register(PlayerClass
            .create("novice", 0, 0, "Novice", Formatting.GRAY, ResourceType.MANA,
                    "A beginning adventurer with no specialized skills - fragile but determined")
            .jobHealthModify(0.0) // Novice: 0
            .growthFactors(Map.of(
                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(1.0),
                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(1)
            ))
            .maxLevel(10)
            .build());

    PlayerClass SWORDMAN = register(PlayerClass
            .create("swordman", 1, 1, "Swordman", Formatting.RED, ResourceType.RAGE,
                    "A warrior who trades magic for martial prowess")
            .jobHealthModify(0.7) // Swordman: .7
            .growthFactors(Map.of(
                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(2.0),
                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(1)
            ))
            .jobBonuses((short)7, (short)2, (short)4, (short)0, (short)3, (short)2)
            .build()
            .addRequirement(NOVICE, 10));

    PlayerClass MAGICIAN = register(PlayerClass
            .create("magician", 1, 2, "Magician", Formatting.BLUE, ResourceType.MANA,
                    "A glass cannon wielding devastating arcane power")
            .jobHealthModify(0.3)
            .growthFactors(Map.of(
                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(6.0),
                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(1)
            ))
            .jobBonuses((short)0, (short)4, (short)0, (short)8, (short)3, (short)3)
            .build()
            .addRequirement(NOVICE, 10));

    PlayerClass ARCHER = register(PlayerClass
            .create("archer", 1, 3, "Archer", Formatting.GREEN, ResourceType.MANA,
                    "A ranged specialist with mobility and precision")
            .jobHealthModify(0.5)
            .growthFactors(Map.of(
                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(2.0),
                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(1)
            ))
            .jobBonuses((short)3, (short)3, (short)1, (short)2, (short)7, (short)2)
            .build()
            .addRequirement(NOVICE, 10));

    PlayerClass ACOLYTE = register(PlayerClass
            .create("acolyte", 1, 4, "Acolyte", Formatting.GOLD, ResourceType.MANA,
                    "A divine hybrid balancing healing magic and survivability")
            .jobHealthModify(0.4)
            .growthFactors(Map.of(
                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(5.0),
                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(1)
            ))
            .jobBonuses((short)3, (short)2, (short)3, (short)3, (short)3, (short)4)
            .build()
            .addRequirement(NOVICE, 10));

    PlayerClass THIEF = register(PlayerClass
            .create("thief", 1, 5, "Thief", Formatting.DARK_GRAY, ResourceType.RAGE,
                    "An agile assassin who relies on speed over brute force")
            .jobHealthModify(0.5) // Thief: .5
            .growthFactors(Map.of(
                    ModEntityAttributes.RESOURCE, GrowthFactor.flat(2.0),
                    ModEntityAttributes.RESOURCE_REGEN, GrowthFactor.flat(1)
            ))
            .jobBonuses((short)4, (short)4, (short)2, (short)1, (short)4, (short)3)
            .build()
            .addRequirement(NOVICE, 10));

    private static PlayerClass register(PlayerClass playerClass) {
        CLASSES.put(playerClass.getId(), playerClass);
        return playerClass;
    }
}