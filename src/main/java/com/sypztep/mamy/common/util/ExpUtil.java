package com.sypztep.mamy.common.util;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.common.network.client.SendToastPayloadS2C;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.data.MobExpEntry;
import com.sypztep.mamy.common.event.living.MobSpawnStatsEvent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.difficulty.ProgressiveDifficultySystem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ExpUtil {
    public static void applyDeathPenalty(ServerPlayerEntity player, DamageSource damageSource) {
        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.get(player);

        if (levelComponent.getLevelSystem().isMaxLevel()) return;

        long expToNextLevel = levelComponent.getExperienceToNextLevel();
        long penaltyAmount = Math.round(expToNextLevel * ModConfig.deathPenaltyPercentage);

        if (penaltyAmount <= 0) return;

        long currentExp = levelComponent.getExperience();
        long newExp = Math.max(0, currentExp - penaltyAmount);

        levelComponent.getLevelSystem().setExperience(newExp);

        String killerName = getKillerName(damageSource);
        SendToastPayloadS2C.sendDeathPenalty(player, penaltyAmount, killerName);
    }
    private static String getKillerName(DamageSource damageSource) {
        if (damageSource.getAttacker() instanceof LivingEntity attacker) {
            if (attacker.hasCustomName()) return attacker.getCustomName().getString();
            return attacker.getType().getName().getString();
        }

        if (damageSource.getSource() instanceof LivingEntity source) {
            if (source.hasCustomName()) return source.getCustomName().getString();
            return source.getType().getName().getString();
        }

        return damageSource.getName();
    }

    public static int calculateExpReward(PlayerEntity player, LivingEntity target, float damagePercentage) {
        EntityType<?> entityType = target.getType();
        int baseExp = MobExpEntry.getExpReward(entityType);

        if (baseExp <= 0) return 0;

        float expFromDamage = baseExp * damagePercentage;

        int playerLevel = ModEntityComponents.LIVINGLEVEL.get(player).getLevel();
        int targetLevel = ModEntityComponents.LIVINGLEVEL.get(target).getLevel();

        float levelMultiplier = calculateLevelPenalty(playerLevel, targetLevel);

        float rarityMultiplier = ProgressiveDifficultySystem.getVariantExpMultiplier(target);

        int finalExp = Math.round(expFromDamage * levelMultiplier * rarityMultiplier);

        return Math.max(0, finalExp);
    }

    public static int calculateClassReward(PlayerEntity player, LivingEntity target, float damagePercentage) {
        EntityType<?> entityType = target.getType();
        int baseClassReward = MobExpEntry.getClassReward(entityType);

        if (baseClassReward <= 0) return 0;

        float classExpFromDamage = baseClassReward * damagePercentage;

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.getNullable(player);
        if (classComponent == null) return 0;

        int playerClassLevel = classComponent.getClassManager().getClassLevel();
        int targetLevel = ModEntityComponents.LIVINGLEVEL.get(target).getLevel();

        float levelMultiplier = calculateLevelPenalty(playerClassLevel, targetLevel);

        float rarityMultiplier = ProgressiveDifficultySystem.getVariantExpMultiplier(target);

        int finalClassExp = Math.round(classExpFromDamage * levelMultiplier * rarityMultiplier);

        return Math.max(0, finalClassExp);
    }

    public static void awardExperience(PlayerEntity player, long amount, String source, boolean showMessage) {
        if (amount <= 0) return;

        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.getNullable(player);
        if (levelComponent != null) {
            int oldLevel = levelComponent.getLevel();
            levelComponent.addExperience((int) amount);
            int newLevel = levelComponent.getLevel();

            if (showMessage) {
                if (newLevel > oldLevel)
                    SendToastPayloadS2C.sendLevelUp((ServerPlayerEntity) player, newLevel - oldLevel);
                SendToastPayloadS2C.sendExperience((ServerPlayerEntity) player, amount, source);
            }
        }
    }

    public static void awardClassExperience(PlayerEntity player, long amount, String source, boolean showMessage) {
        if (amount <= 0) return;

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.getNullable(player);
        if (classComponent != null) {
            int oldClassLevel = classComponent.getClassManager().getClassLevel();

            classComponent.addClassExperience(amount);

            int newClassLevel = classComponent.getClassManager().getClassLevel();

            if (showMessage) {
                if (newClassLevel > oldClassLevel && player instanceof ServerPlayerEntity serverPlayer) {
                    SendToastPayloadS2C.sendLevelUp(serverPlayer, newClassLevel - oldClassLevel);
                }

                if (player instanceof ServerPlayerEntity serverPlayer) {
                    String classSource = "Class: " + source;
                    SendToastPayloadS2C.sendExperience(serverPlayer, amount, classSource);
                }
            }
        }
    }

    public static void awardCombinedExperience(PlayerEntity player, LivingEntity target, float damagePercentage, String source) {
        int mainExp = calculateExpReward(player, target, damagePercentage);
        int classExp = calculateClassReward(player, target, damagePercentage);

        // Check for rare kill bonus message
        float rarityMultiplier = ProgressiveDifficultySystem.getVariantExpMultiplier(target);
        boolean isRareKill = rarityMultiplier > 1.0f;

        // Add rarity info to source if it's a rare kill
        String finalSource = source;
        if (isRareKill) {
            int rarityLevel = MobSpawnStatsEvent.getRarityLevel(target);
            String[] rarityNames = {"Common", "Uncommon", "Rare", "Epic", "Legendary", "Mythic"};
            finalSource = source + " (" + rarityNames[Math.min(rarityLevel, 5)] + " ×" + String.format("%.1f", rarityMultiplier) + ")";
        }

        // Track level changes for combined toast
        int oldMainLevel = 0;
        int oldClassLevel = 0;
        boolean shouldReceiveMain = shouldReceiveExperience(player, target);
        boolean shouldReceiveClass = shouldReceiveClassExperience(player, target);

        // Get old levels
        if (shouldReceiveMain && mainExp > 0) {
            LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.getNullable(player);
            if (levelComponent != null) {
                oldMainLevel = levelComponent.getLevel();
            }
        }

        if (shouldReceiveClass && classExp > 0) {
            PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.getNullable(player);
            if (classComponent != null) {
                oldClassLevel = classComponent.getClassManager().getClassLevel();
            }
        }

        if (shouldReceiveMain && mainExp > 0) {
            awardExperience(player, mainExp, finalSource, false);
        }

        if (shouldReceiveClass && classExp > 0) {
            awardClassExperience(player, classExp, finalSource, false);
        }

        int newMainLevel = oldMainLevel;
        int newClassLevel = oldClassLevel;

        if (shouldReceiveMain && mainExp > 0) {
            LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.getNullable(player);
            if (levelComponent != null) {
                newMainLevel = levelComponent.getLevel();
            }
        }

        if (shouldReceiveClass && classExp > 0) {
            PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.getNullable(player);
            if (classComponent != null) newClassLevel = classComponent.getClassManager().getClassLevel();
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            long actualMainExp = shouldReceiveMain ? mainExp : 0;
            long actualClassExp = shouldReceiveClass ? classExp : 0;

            if (actualMainExp > 0 || actualClassExp > 0) {
                SendToastPayloadS2C.sendCombinedExperience(serverPlayer, actualMainExp, actualClassExp, finalSource);
            }

            // Send combined level up toast if any levels were gained
            int mainLevelsGained = newMainLevel - oldMainLevel;
            int classLevelsGained = newClassLevel - oldClassLevel;

            if (mainLevelsGained > 0 || classLevelsGained > 0) SendToastPayloadS2C.sendCombinedLevelUp(serverPlayer, mainLevelsGained, classLevelsGained);
        }
    }

    public static float calculateLevelPenalty(int playerLevel, int targetLevel) {
        int levelDiff = targetLevel - playerLevel;

        return switch (levelDiff) {
            case 15 -> 1.15f;
            case 14 -> 1.20f;
            case 13 -> 1.25f;
            case 12 -> 1.30f;
            case 11 -> 1.35f;
            case 10 -> 1.40f;
            case 9 -> 1.35f;
            case 8 -> 1.30f;
            case 7 -> 1.25f;
            case 6 -> 1.20f;
            case 5 -> 1.15f;
            case 4 -> 1.10f;
            case 3 -> 1.05f;
            case 2, 1, 0, -1, -2, -3, -4, -5 -> 1.00f;
            case -6, -7, -8, -9, -10 -> 0.95f;
            case -11, -12, -13, -14, -15 -> 0.90f;
            case -16, -17, -18, -19, -20 -> 0.85f;
            case -21, -22, -23, -24, -25 -> 0.60f;
            case -26, -27, -28, -29, -30, -31 -> 0.35f;
            default -> {
                if (levelDiff > 16) yield 0.40f;
                if (levelDiff < -31) yield 0.10f;
                yield 1.00f;
            }
        };
    }

    public static boolean shouldReceiveExperience(PlayerEntity player, LivingEntity target) {
        if (target instanceof PlayerEntity) return false;

        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.getNullable(player);
        return levelComponent == null || !levelComponent.getLevelSystem().isMaxLevel();
    }

    public static boolean shouldReceiveClassExperience(PlayerEntity player, LivingEntity target) {
        if (target instanceof PlayerEntity) return false;

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.getNullable(player);
        return classComponent == null || !classComponent.getClassManager().getClassLevelSystem().isMaxLevel();
    }
}