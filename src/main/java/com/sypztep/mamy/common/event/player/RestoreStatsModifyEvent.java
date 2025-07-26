package com.sypztep.mamy.common.event.player;

import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public final class RestoreStatsModifyEvent implements ServerPlayerEvents.AfterRespawn {
    public static void register() {
        ServerPlayerEvents.AFTER_RESPAWN.register(new RestoreStatsModifyEvent());
    }
    @Override
    public void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.get(newPlayer);

        levelComponent.handleRespawn();

        newPlayer.setHealth(newPlayer.getMaxHealth());
    }
}
