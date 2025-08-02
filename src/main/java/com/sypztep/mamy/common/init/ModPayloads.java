package com.sypztep.mamy.common.init;

import com.sypztep.mamy.client.payload.AddEmitterParticlePayloadS2C;
import com.sypztep.mamy.client.payload.AddTextParticlesPayloadS2C;
import com.sypztep.mamy.client.payload.SendToastPayloadS2C;
import com.sypztep.mamy.common.payload.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModPayloads {
    public ModPayloads() {
    }

    public static void init() {
        PayloadTypeRegistry.playS2C().register(AddTextParticlesPayloadS2C.ID, AddTextParticlesPayloadS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(AddEmitterParticlePayloadS2C.ID, AddEmitterParticlePayloadS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(SendToastPayloadS2C.ID, SendToastPayloadS2C.CODEC);

        PayloadTypeRegistry.playC2S().register(IncreaseStatsPayloadC2S.ID, IncreaseStatsPayloadC2S.CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleStancePayloadC2S.ID, ToggleStancePayloadC2S.CODEC);
        PayloadTypeRegistry.playC2S().register(UseSkillPayloadC2S.ID, UseSkillPayloadC2S.CODEC);
        PayloadTypeRegistry.playC2S().register(BindSkillPayloadC2S.ID, BindSkillPayloadC2S.CODEC);
        PayloadTypeRegistry.playC2S().register(LearnSkillPayloadC2S.ID, LearnSkillPayloadC2S.CODEC);
        PayloadTypeRegistry.playC2S().register(UpgradeSkillPayloadC2S.ID, UpgradeSkillPayloadC2S.CODEC);

        registerPayloads();
    }

    private static void registerPayloads() {
        ServerPlayNetworking.registerGlobalReceiver(IncreaseStatsPayloadC2S.ID, new IncreaseStatsPayloadC2S.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(ToggleStancePayloadC2S.ID, new ToggleStancePayloadC2S.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(UseSkillPayloadC2S.ID, new UseSkillPayloadC2S.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(BindSkillPayloadC2S.ID, new BindSkillPayloadC2S.Receiver());

        ServerPlayNetworking.registerGlobalReceiver(LearnSkillPayloadC2S.ID, new LearnSkillPayloadC2S.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(UpgradeSkillPayloadC2S.ID, new UpgradeSkillPayloadC2S.Receiver());
    }
    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void registerClientPayloads() {
            ClientPlayNetworking.registerGlobalReceiver(AddTextParticlesPayloadS2C.ID, new AddTextParticlesPayloadS2C.Receiver());
            ClientPlayNetworking.registerGlobalReceiver(AddEmitterParticlePayloadS2C.ID, new AddEmitterParticlePayloadS2C.Receiver());
            ClientPlayNetworking.registerGlobalReceiver(SendToastPayloadS2C.ID, new SendToastPayloadS2C.Receiver());
        }
    }
}
