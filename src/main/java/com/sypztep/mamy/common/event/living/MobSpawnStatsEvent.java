package com.sypztep.mamy.common.event.living;

import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.data.MobExpEntry;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

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

        applyStaticMobStatsAndLevel(livingEntity, mobEntry);
    }

    /**
     * Apply static stats without dynamic scaling
     */
    private void applyStaticMobStatsAndLevel(LivingEntity entity, MobExpEntry mobEntry) {
        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.getNullable(entity);
        if (levelComponent == null) return;

        levelComponent.getLevelSystem().setLevel((short) mobEntry.baseLevel());

        if (!LivingEntityUtil.isPlayer(entity)) {
            applyStatsToMob(levelComponent, mobEntry);
            levelComponent.refreshAllStatEffectsInternal();
        }

        entity.setHealth(entity.getMaxHealth());
    }

    /**
     * Apply stats to mob using MAMY's stat system with new Map-based approach
     */
    private void applyStatsToMob(LivingLevelComponent levelComponent, MobExpEntry mobEntry) {
        for (var statType : StatTypes.values()) {
            int value = mobEntry.getStat(statType);
            if (value > 0) {
                levelComponent.getLivingStats().getStat(statType).setPoints((short) value);
            }
        }
    }
}