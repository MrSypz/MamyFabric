package com.sypztep.mamy.common.event.living;

import com.sypztep.mamy.common.component.living.DamageTrackerComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.util.ExpUtil;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;
import java.util.UUID;

public class MobDeathExperienceEvent implements ServerLivingEntityEvents.AfterDeath {
    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(new MobDeathExperienceEvent());
    }
    @Override
    public void afterDeath(LivingEntity entity, DamageSource damageSource) {
        if (entity instanceof PlayerEntity || entity.getWorld().isClient()) return;

        DamageTrackerComponent tracker = ModEntityComponents.DAMAGETRACKER.getNullable(entity);
        if (tracker == null) return;

        Map<UUID, Float> damageMap = tracker.getAllDamage();
        if (damageMap.isEmpty()) return;

        ServerWorld world = (ServerWorld) entity.getWorld();
        String entityName = entity.getType().getName().getString();

        damageMap.entrySet().parallelStream().forEach(entry -> {
            UUID playerId = entry.getKey();

            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(playerId);
            if (player == null) return;

            float damagePercentage = tracker.getDamagePercentage(player);
            if (damagePercentage <= 0) return;

            float percentage = damagePercentage * 100f;
            String source = String.format("%.1f%% damage to %s", percentage, entityName);

            ExpUtil.awardCombinedExperience(player, entity, damagePercentage, source);
        });

        tracker.clearDamage();
    }
}