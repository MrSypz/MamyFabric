package com.sypztep.mamy.common.event.player;

import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public final class InitPlayerClassEvent implements ServerPlayerEvents.AfterRespawn, ServerPlayConnectionEvents.Join {
    private static final InitPlayerClassEvent INSTANCE = new InitPlayerClassEvent();

    public static void register() {
        ServerPlayerEvents.AFTER_RESPAWN.register(INSTANCE);
        ServerPlayConnectionEvents.JOIN.register(INSTANCE);
    }

    @Override
    public void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(newPlayer);
        classComponent.initialize(); // Replace  classComponent.handleRespawn();

        newPlayer.setHealth(newPlayer.getMaxHealth());
    }

    @Override
    public void onPlayReady(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
        PlayerEntity player = serverPlayNetworkHandler.getPlayer();

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        classComponent.initialize();

        player.setHealth(player.getMaxHealth());
    }
}