package com.sypztep.mamy.client.event;

import com.sypztep.mamy.common.system.skill.ClientSkillCooldowns;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

public final class SkillCooldownCleanUpEvent implements ClientPlayConnectionEvents.Disconnect {
    public static void register() {
        ClientPlayConnectionEvents.DISCONNECT.register(new SkillCooldownCleanUpEvent());
    }
    @Override
    public void onPlayDisconnect(ClientPlayNetworkHandler clientPlayNetworkHandler, MinecraftClient minecraftClient) {
        ClientSkillCooldowns.clear();
    }
}
