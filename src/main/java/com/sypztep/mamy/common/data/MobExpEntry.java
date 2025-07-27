package com.sypztep.mamy.common.data;

import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.EntityType;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public record MobExpEntry(
        int expReward,
        int baseLevel,
        Map<StatTypes, Integer> stats
) {

    public static final Map<EntityType<?>, MobExpEntry> MOBEXP_MAP = new ConcurrentHashMap<>();

    // Convenience constructor with default stats
    public MobExpEntry(int expReward, int baseLevel) {
        this(expReward, baseLevel, createDefaultStats());
    }

    private static Map<StatTypes, Integer> createDefaultStats() {
        return Map.of(
                StatTypes.STRENGTH, 1,
                StatTypes.AGILITY, 1,
                StatTypes.VITALITY, 1,
                StatTypes.INTELLIGENCE, 1,
                StatTypes.DEXTERITY, 1,
                StatTypes.LUCK, 1
        );
    }

    // Convenience constructor with level-based stats
    public static MobExpEntry withLevelStats(int expReward, int baseLevel) {
        var stats = Arrays.stream(StatTypes.values())
                .collect(Collectors.toMap(
                        stat -> stat,
                        stat -> baseLevel,
                        (existing, replacement) -> replacement // In case of duplicates, use the new value
                ));
        return new MobExpEntry(expReward, baseLevel, stats);
    }

    // Convenience constructors for specialized mobs
    public static MobExpEntry warrior(int expReward, int baseLevel) {
        return new MobExpEntry(expReward, baseLevel, Map.of(
                StatTypes.STRENGTH, baseLevel + 2,
                StatTypes.AGILITY, baseLevel,
                StatTypes.VITALITY, baseLevel + 5,
                StatTypes.INTELLIGENCE, Math.max(1, baseLevel - 5),
                StatTypes.DEXTERITY, baseLevel,
                StatTypes.LUCK, baseLevel + 5
        ));
    }

    public static MobExpEntry archer(int expReward, int baseLevel) {
        return new MobExpEntry(expReward, baseLevel, Map.of(
                StatTypes.STRENGTH, baseLevel,
                StatTypes.AGILITY, baseLevel + 8,
                StatTypes.VITALITY, baseLevel,
                StatTypes.INTELLIGENCE, baseLevel,
                StatTypes.DEXTERITY, baseLevel + 5,
                StatTypes.LUCK, baseLevel + 8
        ));
    }

    public static MobExpEntry mage(int expReward, int baseLevel) {
        return new MobExpEntry(expReward, baseLevel, Map.of(
                StatTypes.STRENGTH, Math.max(1, baseLevel - 1),
                StatTypes.AGILITY, baseLevel,
                StatTypes.VITALITY, baseLevel,
                StatTypes.INTELLIGENCE, baseLevel + 8,
                StatTypes.DEXTERITY, baseLevel,
                StatTypes.LUCK, baseLevel + 10
        ));
    }

    public static MobExpEntry tank(int expReward, int baseLevel) {
        return new MobExpEntry(expReward, baseLevel, Map.of(
                StatTypes.STRENGTH, baseLevel + 1,
                StatTypes.AGILITY, Math.max(1, baseLevel - 5),
                StatTypes.VITALITY, baseLevel + 8,
                StatTypes.INTELLIGENCE, baseLevel,
                StatTypes.DEXTERITY, baseLevel,
                StatTypes.LUCK, baseLevel + 2
        ));
    }

    public static MobExpEntry boss(int expReward, int baseLevel) {
        return new MobExpEntry(expReward, baseLevel, Map.of(
                StatTypes.STRENGTH, baseLevel + 15,
                StatTypes.AGILITY, baseLevel + 13,
                StatTypes.VITALITY, baseLevel + 15,
                StatTypes.INTELLIGENCE, baseLevel + 13,
                StatTypes.DEXTERITY, baseLevel + 13,
                StatTypes.LUCK, baseLevel + 13
        ));
    }

    // Easy access method for stats
    public int getStat(StatTypes statType) {
        return stats.getOrDefault(statType, 1);
    }

    // Existing static methods for MOBEXP_MAP
    public static MobExpEntry getEntry(EntityType<?> entityType) {
        return MOBEXP_MAP.get(entityType);
    }

    public static int getExpReward(EntityType<?> entityType) {
        MobExpEntry entry = MOBEXP_MAP.get(entityType);
        return entry != null ? entry.expReward() : 0;
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