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