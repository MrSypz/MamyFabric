package com.sypztep.mamy.common.data;

import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.EntityType;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public record MobExpEntry(
        int expReward,
        int classReward,
        int baseLevel,
        Map<StatTypes, Integer> stats
) {

    public static final Map<EntityType<?>, MobExpEntry> MOBEXP_MAP = new ConcurrentHashMap<>();

    // Convenience constructor with default stats
    public MobExpEntry(int expReward,int classReward, int baseLevel) {
        this(expReward,classReward, baseLevel, createDefaultStats());
    }

    private static Map<StatTypes, Integer> createDefaultStats() {
        return Map.of(
                StatTypes.STRENGTH, 0,
                StatTypes.AGILITY, 0,
                StatTypes.VITALITY, 0,
                StatTypes.INTELLIGENCE, 0,
                StatTypes.DEXTERITY, 0,
                StatTypes.LUCK, 0
        );
    }

    // Convenience constructor with level-based stats
    public static MobExpEntry withLevelStats(int expReward, int classReward, int baseLevel) {
        var stats = Arrays.stream(StatTypes.values())
                .collect(Collectors.toMap(
                        stat -> stat,
                        stat -> baseLevel,
                        (existing, replacement) -> replacement
                ));
        return new MobExpEntry(expReward, classReward, baseLevel, stats);
    }

    // OVERWORLD MOBS - Level passed as parameter
    public static MobExpEntry overworldMob(int expReward, int classReward, int baseLevel) {
        return new MobExpEntry(expReward, classReward, baseLevel, Map.of(
                StatTypes.STRENGTH, baseLevel,
                StatTypes.AGILITY, 32, // 75% evasion
                StatTypes.VITALITY, baseLevel,
                StatTypes.INTELLIGENCE, baseLevel,
                StatTypes.DEXTERITY, 64, // 16% accuracy boost
                StatTypes.LUCK, baseLevel
        ));
    }

    public static MobExpEntry overworldArcher(int expReward, int classReward, int baseLevel) {
        return new MobExpEntry(expReward, classReward, baseLevel, Map.of(
                StatTypes.STRENGTH, baseLevel,
                StatTypes.AGILITY, 40, // High evasion
                StatTypes.VITALITY, baseLevel,
                StatTypes.INTELLIGENCE, baseLevel,
                StatTypes.DEXTERITY, 80, // Very high accuracy
                StatTypes.LUCK, baseLevel + 15 // Archers get luck bonus
        ));
    }

    public static MobExpEntry overworldMage(int expReward, int classReward, int baseLevel) {
        return new MobExpEntry(expReward, classReward, baseLevel, Map.of(
                StatTypes.STRENGTH, Math.max(1, baseLevel - 2),
                StatTypes.AGILITY, 35, // Medium-high evasion
                StatTypes.VITALITY, baseLevel,
                StatTypes.INTELLIGENCE, baseLevel + 12,
                StatTypes.DEXTERITY, 50, // Medium accuracy
                StatTypes.LUCK, baseLevel + 12
        ));
    }

    public static MobExpEntry overworldWarrior(int expReward, int classReward, int baseLevel) {
        return new MobExpEntry(expReward, classReward, baseLevel, Map.of(
                StatTypes.STRENGTH, baseLevel + 5,
                StatTypes.AGILITY, 25, // Slightly lower evasion
                StatTypes.VITALITY, baseLevel + 8,
                StatTypes.INTELLIGENCE, Math.max(1, baseLevel - 3),
                StatTypes.DEXTERITY, 70, // High accuracy
                StatTypes.LUCK, baseLevel + 8
        ));
    }

    public static MobExpEntry overworldTank(int expReward, int classReward, int baseLevel) {
        return new MobExpEntry(expReward, classReward, baseLevel, Map.of(
                StatTypes.STRENGTH, baseLevel + 8,
                StatTypes.AGILITY, 15, // Low evasion
                StatTypes.VITALITY, baseLevel + 20,
                StatTypes.INTELLIGENCE, baseLevel,
                StatTypes.DEXTERITY, 90, // Very high accuracy to compensate
                StatTypes.LUCK, baseLevel + 3
        ));
    }

    // NETHER MOBS - Level passed as parameter
    public static MobExpEntry netherMob(int expReward, int classReward, int baseLevel) {
        return new MobExpEntry(expReward, classReward, baseLevel, Map.of(
                StatTypes.STRENGTH, baseLevel + 5,
                StatTypes.AGILITY, 82, // 87.5% evasion
                StatTypes.VITALITY, baseLevel + 5,
                StatTypes.INTELLIGENCE, baseLevel + 5,
                StatTypes.DEXTERITY, 120, // 30% accuracy boost
                StatTypes.LUCK, baseLevel + 5
        ));
    }

    public static MobExpEntry netherWarrior(int expReward, int classReward, int baseLevel) {
        return new MobExpEntry(expReward, classReward, baseLevel, Map.of(
                StatTypes.STRENGTH, baseLevel + 10,
                StatTypes.AGILITY, 75, // High evasion
                StatTypes.VITALITY, baseLevel + 12,
                StatTypes.INTELLIGENCE, baseLevel,
                StatTypes.DEXTERITY, 130, // Very high accuracy
                StatTypes.LUCK, baseLevel + 10
        ));
    }

    public static MobExpEntry netherMage(int expReward, int classReward, int baseLevel) {
        return new MobExpEntry(expReward, classReward, baseLevel, Map.of(
                StatTypes.STRENGTH, baseLevel,
                StatTypes.AGILITY, 90, // Very high evasion
                StatTypes.VITALITY, baseLevel + 3,
                StatTypes.INTELLIGENCE, baseLevel + 20,
                StatTypes.DEXTERITY, 100, // High accuracy
                StatTypes.LUCK, baseLevel + 18
        ));
    }

    public static MobExpEntry netherBoss(int expReward, int classReward, int baseLevel) {
        return new MobExpEntry(expReward, classReward, baseLevel, Map.of(
                StatTypes.STRENGTH, baseLevel + 25,
                StatTypes.AGILITY, 95, // Boss-level evasion
                StatTypes.VITALITY, baseLevel + 90,
                StatTypes.INTELLIGENCE, baseLevel + 25,
                StatTypes.DEXTERITY, 150, // Boss-level accuracy
                StatTypes.LUCK, baseLevel + 20
        ));
    }

    // END MOBS - Level passed as parameter
    public static MobExpEntry endMob(int expReward, int classReward, int baseLevel) {
        return new MobExpEntry(expReward, classReward, baseLevel, Map.of(
                StatTypes.STRENGTH, baseLevel + 10,
                StatTypes.AGILITY, 112, // 95% evasion
                StatTypes.VITALITY, baseLevel + 40,
                StatTypes.INTELLIGENCE, baseLevel + 10,
                StatTypes.DEXTERITY, 176, // 44% accuracy boost (requires 80+ DEX to hit)
                StatTypes.LUCK, baseLevel + 10
        ));
    }

    public static MobExpEntry endTank(int expReward, int classReward, int baseLevel) {
        return new MobExpEntry(expReward, classReward, baseLevel, Map.of(
                StatTypes.STRENGTH, baseLevel + 15,
                StatTypes.AGILITY, 100, // High but not max evasion
                StatTypes.VITALITY, baseLevel + 25,
                StatTypes.INTELLIGENCE, baseLevel + 5,
                StatTypes.DEXTERITY, 200, // Extreme accuracy
                StatTypes.LUCK, baseLevel + 8
        ));
    }

    public static MobExpEntry endBoss(int expReward, int classReward, int baseLevel) {
        return new MobExpEntry(expReward, classReward, baseLevel, Map.of(
                StatTypes.STRENGTH, baseLevel + 30,
                StatTypes.AGILITY, 120, // Maximum evasion
                StatTypes.VITALITY, baseLevel + 135,
                StatTypes.INTELLIGENCE, baseLevel + 30,
                StatTypes.DEXTERITY, 220, // Maximum accuracy
                StatTypes.LUCK, baseLevel + 25
        ));
    }
    // Easy access method for stats
    public int getStat(StatTypes statType) {
        return stats.getOrDefault(statType, 0);
    }

    // Existing static methods for MOBEXP_MAP
    public static MobExpEntry getEntry(EntityType<?> entityType) {
        return MOBEXP_MAP.get(entityType);
    }

    public static int getExpReward(EntityType<?> entityType) {
        MobExpEntry entry = MOBEXP_MAP.get(entityType);
        return entry != null ? entry.expReward() : 0;
    }
    public static int getClassReward(EntityType<?> entityType) {
        MobExpEntry entry = MOBEXP_MAP.get(entityType);
        return entry != null ? entry.classReward() : 0;
    }

    public static int getBaseLevel(EntityType<?> entityType) {
        MobExpEntry entry = MOBEXP_MAP.get(entityType);
        return entry != null ? entry.baseLevel() : 1;
    }

    public static Map<StatTypes, Integer> getStats(EntityType<?> entityType) {
        MobExpEntry entry = MOBEXP_MAP.get(entityType);
        return entry != null ? entry.stats() : createDefaultStats();
    }

    public static boolean hasEntry(EntityType<?> entityType) {
        return MOBEXP_MAP.containsKey(entityType);
    }

    public static void addEntry(EntityType<?> entityType, MobExpEntry entry) {
        MOBEXP_MAP.put(entityType, entry);
    }

    public static void removeEntry(EntityType<?> entityType) {
        MOBEXP_MAP.remove(entityType);
    }

    public static void clearAll() {
        MOBEXP_MAP.clear();
    }

    @Override
    public String toString() {
        return String.format("MobExpEntry{expReward=%d, baseLevel=%d, stats=%s}",
                expReward, baseLevel, stats);
    }
}