package com.sypztep.mamy.common.event.living;

import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.data.MobExpEntry;
import com.sypztep.mamy.common.init.ModEntityComponents;
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

        // STEP 1: Apply base stats from datapack
        applyBasicMobStats(livingEntity, mobEntry);

        // STEP 2: Generate enhanced variant with name keywords
        String baseName = livingEntity.getType().getName().getString();
        ProgressiveDifficultySystem.MonsterVariant variant =
                ProgressiveDifficultySystem.generateMonsterVariant(livingEntity.getRandom(), baseName);

        // STEP 3: Apply rarity multipliers + name-based bonuses
        ProgressiveDifficultySystem.applyMonsterVariant(livingEntity, variant);

        // STEP 4: Debug logging for complex monsters
//        if (variant.rarity != ProgressiveDifficultySystem.MonsterRarity.COMMON) {
//            System.out.printf("[RareSpawn] %s %s spawned! (Rarity: ×%.1f, Name: '%s')\n",
//                    variant.rarity.name(), baseName,
//                    variant.totalStatMultiplier, variant.enhancedName);
//        }
//
//        // Log name bonuses if present
//        if (com.sypztep.mamy.common.system.difficulty.NameBasedBonusSystem.hasNameBonuses(variant.enhancedName)) {
//            System.out.printf("[NameBonuses] %s has enhanced name: '%s'\n",
//                    baseName, variant.enhancedName);
//        }
    }

    /**
     * Apply static stats with dynamic scaling
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
                    if (stat != null) {
                        stat.setPoints((short) baseValue);
                    }
                }
            }
            levelComponent.refreshAllStatEffectsInternal();
        }

        entity.setHealth(entity.getMaxHealth());
    }

    public static int getRarityLevel(LivingEntity entity) {
        if (!entity.hasCustomName()) return 0;

        String name = entity.getCustomName().getString();

        if (name.contains("Void") || name.contains("Avatar") || name.contains("Cosmic")) {
            return 5; // Mythic
        } else if (name.contains("Primordial") || name.contains("Dimensional") || name.contains("Bane")) {
            return 4; // Legendary
        } else if (name.contains("Nightmare") || name.contains("Abyssal") || name.contains("Eternal")) {
            return 3; // Epic
        } else if (name.contains("Bloodthirsty") || name.contains("Corrupted") || name.contains("Fierce")) {
            return 2; // Rare
        } else if (name.contains("Strong") || name.contains("Angry") || name.contains("Wild")) {
            return 1; // Uncommon
        }

        return 0; // Common
    }
}