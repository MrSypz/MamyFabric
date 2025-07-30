package com.sypztep.mamy.common.init;

import com.sypztep.mamy.client.payload.AddEmitterParticlePayloadS2C;
import com.sypztep.mamy.client.payload.AddTextParticlesPayloadS2C;
import com.sypztep.mamy.client.payload.SendToastPayloadS2C;
import com.sypztep.mamy.common.payload.IncreaseStatsPayloadC2S;
import com.sypztep.mamy.common.payload.ToggleStancePayloadC2S;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModPayloads {
    public ModPayloads() {
    }

    public static void init() {
        PayloadTypeRegistry.playS2C().register(AddTextParticlesPayloadS2C.ID, AddTextParticlesPayloadS2C.CODEC); // Server to Client
        PayloadTypeRegistry.playS2C().register(AddEmitterParticlePayloadS2C.ID, AddEmitterParticlePayloadS2C.CODEC); // Server to Client
        PayloadTypeRegistry.playS2C().register(SendToastPayloadS2C.ID, SendToastPayloadS2C.CODEC); // Server to Client

        PayloadTypeRegistry.playC2S().register(IncreaseStatsPayloadC2S.ID, IncreaseStatsPayloadC2S.CODEC); // Client to Server
        PayloadTypeRegistry.playC2S().register(ToggleStancePayloadC2S.ID, ToggleStancePayloadC2S.CODEC);

        registerPayloads();
    }
    private static void registerPayloads() {
        ServerPlayNetworking.registerGlobalReceiver(IncreaseStatsPayloadC2S.ID, new IncreaseStatsPayloadC2S.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(ToggleStancePayloadC2S.ID, new ToggleStancePayloadC2S.Receiver());
    }
    public static void registerClientPayloads() {
        ClientPlayNetworking.registerGlobalReceiver(AddTextParticlesPayloadS2C.ID, new AddTextParticlesPayloadS2C.Receiver());
        ClientPlayNetworking.registerGlobalReceiver(AddEmitterParticlePayloadS2C.ID, new AddEmitterParticlePayloadS2C.Receiver());
        ClientPlayNetworking.registerGlobalReceiver(SendToastPayloadS2C.ID, new SendToastPayloadS2C.Receiver());
    }
}
