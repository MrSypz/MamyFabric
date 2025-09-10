package com.sypztep.mamy.common.event.player;

import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.StatModifierHelper;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class InitPlayerClassEvent implements ServerPlayerEvents.AfterRespawn, ServerPlayConnectionEvents.Join {
    private static final InitPlayerClassEvent INSTANCE = new InitPlayerClassEvent();

    public static void register() {
        ServerPlayerEvents.AFTER_RESPAWN.register(INSTANCE);
        ServerPlayConnectionEvents.JOIN.register(INSTANCE);
    }

    @Override
    public void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.getNullable(newPlayer);

        if (component != null) {
            Set<String> allModifierSources = new HashSet<>();
            for (StatTypes statType : StatTypes.values()) {
                Map<String, Short> modifiers = component.getStatByType(statType).getTemporaryModifiers();
                allModifierSources.addAll(modifiers.keySet());
            }
            for (String source : allModifierSources) StatModifierHelper.removeAllModifiersFromSource(newPlayer, source);
        }

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(newPlayer);
        classComponent.respawn();
        newPlayer.setHealth(newPlayer.getMaxHealth());
    }
    @Override
    public void onPlayReady(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
        PlayerEntity player = serverPlayNetworkHandler.getPlayer();

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        classComponent.initialize();
    }
}