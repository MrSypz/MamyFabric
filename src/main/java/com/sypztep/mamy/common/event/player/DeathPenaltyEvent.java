package com.sypztep.mamy.common.event.player;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.common.util.ExpUtil;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public final class DeathPenaltyEvent implements ServerLivingEntityEvents.AfterDeath {
    private static final DeathPenaltyEvent INSTANCE = new DeathPenaltyEvent();

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(INSTANCE);
    }
    @Override
    public void afterDeath(LivingEntity livingEntity, DamageSource damageSource) {
        if (!(livingEntity instanceof ServerPlayerEntity player) || livingEntity.getWorld().isClient()) return;

        if (!ModConfig.enableDeathPenalty) return;

        if (!LivingEntityUtil.isKilledByMonster(damageSource)) return;

        ExpUtil.applyDeathPenalty(player, damageSource);
    }
}
