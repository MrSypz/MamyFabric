package com.sypztep.mamy.common.event.player;

import com.sypztep.mamy.client.event.animation.NetworkAnimationManager;
import com.sypztep.mamy.common.system.skill.SkillManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public final class PlayerDisconnectCleanupEvent implements ServerPlayConnectionEvents.Disconnect {
    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register(new PlayerDisconnectCleanupEvent());
    }

    @Override
    public void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        PlayerEntity player = handler.player;

        SkillManager.cleanupPlayer(player.getUuidAsString());
        NetworkAnimationManager.cleanupPlayer(player.getId());
    }
}