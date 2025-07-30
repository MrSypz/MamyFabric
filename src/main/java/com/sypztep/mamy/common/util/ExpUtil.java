package com.sypztep.mamy.common.util;

import com.sypztep.mamy.client.payload.SendToastPayloadS2C;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.data.MobExpEntry;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class ExpUtil {

    public static int calculateExpReward(PlayerEntity player, LivingEntity target, float damagePercentage) {
        EntityType<?> entityType = target.getType();
        int baseExp = MobExpEntry.getExpReward(entityType);

        if (baseExp <= 0) return 0;

        float expFromDamage = baseExp * damagePercentage;

        int playerLevel = ModEntityComponents.LIVINGLEVEL.get(player).getLevel();
        int targetLevel = ModEntityComponents.LIVINGLEVEL.get(target).getLevel();

        float levelMultiplier = calculateLevelPenalty(playerLevel, targetLevel);
        int finalExp = Math.round(expFromDamage * levelMultiplier);

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
        int finalClassExp = Math.round(classExpFromDamage * levelMultiplier);

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

            // Use batch update for sync
            classComponent.addClassExperience(amount);

            int newClassLevel = classComponent.getClassManager().getClassLevel();

            if (showMessage) {
                // Send class level up notification
                if (newClassLevel > oldClassLevel && player instanceof ServerPlayerEntity serverPlayer) {
                    SendToastPayloadS2C.sendLevelUp(serverPlayer, newClassLevel - oldClassLevel);
                }

                // Send class experience notification
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
            awardExperience(player, mainExp, source, false); // No toast
        }

        if (shouldReceiveClass && classExp > 0) {
            awardClassExperience(player, classExp, source, false); // No toast
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
                SendToastPayloadS2C.sendCombinedExperience(serverPlayer, actualMainExp, actualClassExp, source);
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
            case 16 -> 0.40f;
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
            case 2 -> 1.00f;
            case 1 -> 0.95f;
            case 0 -> 0.90f;
            case -1 -> 0.85f;
            case -2 -> 0.80f;
            case -3 -> 0.75f;
            case -4 -> 0.70f;
            case -5 -> 0.65f;
            case -6 -> 0.60f;
            case -7 -> 0.55f;
            case -8 -> 0.50f;
            case -9 -> 0.45f;
            case -10 -> 0.40f;
            default -> levelDiff > 16 ? 0.30f : 0.35f; // Very high or very low level difference
        };
    }
    // player are not have exp by default but for safty
    public static boolean shouldReceiveExperience(PlayerEntity player, LivingEntity target) {
        if (target instanceof PlayerEntity) return false;

        // Don't give exp if player is at max level
        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.getNullable(player);
        return levelComponent == null || !levelComponent.getLevelSystem().isMaxLevel();
    }

    public static boolean shouldReceiveClassExperience(PlayerEntity player, LivingEntity target) {
        if (target instanceof PlayerEntity) return false;

        // Don't give class exp if player is at max class level
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.getNullable(player);
        return classComponent == null || !classComponent.getClassManager().getClassLevelSystem().isMaxLevel();
    }
}