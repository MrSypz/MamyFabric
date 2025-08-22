package com.sypztep.mamy.common.event.living;

import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.data.MobExpEntry;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.difficulty.NameBasedBonusSystem;
import com.sypztep.mamy.common.system.difficulty.ProgressiveDifficultySystem;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;

public final class MobSpawnStatsEvent implements ServerEntityEvents.Load {

    private static final MobSpawnStatsEvent INSTANCE = new MobSpawnStatsEvent();

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register(INSTANCE);
    }

    @Override
    public void onLoad(Entity entity, ServerWorld world) {
        if (!(entity instanceof LivingEntity livingEntity) || entity instanceof PlayerEntity) return;

        MobExpEntry mobEntry = MobExpEntry.getEntry(livingEntity.getType());
        if (mobEntry == null) return;

        // Skip if peaceful mode
        if (world.getDifficulty() == Difficulty.PEACEFUL) {
            applyBasicMobStats(livingEntity, mobEntry);
            return;
        }

        // === ENHANCED PROGRESSIVE MONSTER GENERATION ===

        // STEP 1: Apply base stats from datapack
        applyBasicMobStats(livingEntity, mobEntry);

        // STEP 2: Generate ENHANCED variant with ALL new systems:
        // - Local difficulty integration (0-6)
        // - Dynamic level scaling (no cap!)
        // - Biome stat multipliers
        // - Armor scaling by tier
        String baseName = livingEntity.getType().getName().getString();
        ProgressiveDifficultySystem.MonsterVariant variant =
                ProgressiveDifficultySystem.generateEnhancedMonsterVariant(
                        livingEntity.getRandom(),
                        baseName,
                        mobEntry.baseLevel(),
                        world,
                        livingEntity.getBlockPos()
                );

        // STEP 3: Apply ALL enhancements
        ProgressiveDifficultySystem.applyMonsterVariant(livingEntity, variant);
    }

    /**
     * STEP 1: Apply base mob stats from datapack (foundation)
     * FIXED: Now uses correct stat API calls
     */
    private void applyBasicMobStats(LivingEntity entity, MobExpEntry mobEntry) {
        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.getNullable(entity);
        if (levelComponent == null) return;

        levelComponent.getLevelSystem().setLevel((short) mobEntry.baseLevel());

        if (!LivingEntityUtil.isPlayer(entity)) {
            for (StatTypes statType : StatTypes.values()) {
                int baseValue = mobEntry.getStat(statType);
                if (baseValue > 0) {
                    var stat = levelComponent.getStatByType(statType);
                    if (stat != null) stat.setPoints((short) baseValue);
                }
            }
            levelComponent.refreshAllStatEffectsInternal();
        }

        entity.setHealth(entity.getMaxHealth());
    }

    /**
     * UNIFIED rarity detection - no more redundant code!
     * Uses NameBasedBonusSystem for consistency
     */
    public static int getRarityLevel(LivingEntity entity) {
        return NameBasedBonusSystem.getRarityLevel(entity);
    }

    /**
     * Get rarity enum for other systems
     */
    public static ProgressiveDifficultySystem.MonsterRarity getRarity(LivingEntity entity) {
        return NameBasedBonusSystem.getRarityFromName(entity);
    }

    /**
     * Check if monster has enhanced stats (for loot systems, etc.)
     * For future version 0.6.0
     */
    public static boolean hasEnhancedStats(LivingEntity entity) {
        return getRarityLevel(entity) > 0 || NameBasedBonusSystem.hasNameBonuses(entity.getCustomName().getString());
    }

    /**
     * Get total stat multiplier for a monster (for damage calculations)
     */
    @Deprecated
    public static float getStatMultiplier(LivingEntity entity) {
        ProgressiveDifficultySystem.MonsterRarity rarity = getRarity(entity);
        float baseMultiplier = rarity.statMultiplier;

        // Factor in biome multiplier if available
        float biomeMultiplier = ProgressiveDifficultySystem.getBiomeStatMultiplier(
                entity.getWorld(), entity.getBlockPos());

        return baseMultiplier * biomeMultiplier;
    }
}