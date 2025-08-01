package com.sypztep.mamy.common.event.player;

import com.sypztep.mamy.common.system.skill.SkillManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public final class PlayerDisconnectCleanupEvent implements ServerPlayConnectionEvents.Disconnect {
    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register(new PlayerDisconnectCleanupEvent());
    }

    @Override
    public void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        String playerId = handler.getPlayer().getUuidAsString();

        SkillManager.cleanupPlayer(playerId);
    }
}