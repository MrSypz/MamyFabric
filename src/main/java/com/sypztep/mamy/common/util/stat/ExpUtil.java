package com.sypztep.mamy.common.util.stat;

import com.sypztep.mamy.client.payload.SendToastPayloadS2C;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
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

        int playerLevel =  ModEntityComponents.LIVINGLEVEL.get(player).getLevel();
        int targetLevel = ModEntityComponents.LIVINGLEVEL.get(target).getLevel();

        float levelMultiplier = calculateLevelPenalty(playerLevel, targetLevel);
        int finalExp = Math.round(expFromDamage * levelMultiplier);

        return Math.max(0, finalExp);
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

    public static void awardExperience(PlayerEntity player, long amount, String source) {
        awardExperience(player, amount, source, true);
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
            case 1 -> 1.00f;
            case 0 -> 1.00f;
            case -1 -> 1.00f;
            case -2 -> 1.00f;
            case -3 -> 1.00f;
            case -4 -> 1.00f;
            case -5 -> 1.00f;
            case -6 -> 0.95f;
            case -7 -> 0.95f;
            case -8 -> 0.95f;
            case -9 -> 0.95f;
            case -10 -> 0.95f;
            case -11 -> 0.90f;
            case -12 -> 0.90f;
            case -13 -> 0.90f;
            case -14 -> 0.90f;
            case -15 -> 0.90f;
            case -16 -> 0.85f;
            case -17 -> 0.85f;
            case -18 -> 0.85f;
            case -19 -> 0.85f;
            case -20 -> 0.85f;
            case -21 -> 0.60f;
            case -22 -> 0.60f;
            case -23 -> 0.60f;
            case -24 -> 0.60f;
            case -25 -> 0.60f;
            case -26 -> 0.35f;
            case -27 -> 0.35f;
            case -28 -> 0.35f;
            case -29 -> 0.35f;
            case -30 -> 0.35f;
            default -> levelDiff > 16 ? 0.4f : 0.1f;
        };
    }
}
