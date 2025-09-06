package com.sypztep.mamy.common.network.server;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.skill.SkillManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UseSkillPayloadC2S(Identifier skillId) implements CustomPayload {
    public static final Id<UseSkillPayloadC2S> ID = new Id<>(Mamy.id("use_skill"));
    public static final PacketCodec<PacketByteBuf, UseSkillPayloadC2S> CODEC =
            PacketCodec.tuple(Identifier.PACKET_CODEC, UseSkillPayloadC2S::skillId, UseSkillPayloadC2S::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(Identifier skillId) {
        ClientPlayNetworking.send(new UseSkillPayloadC2S(skillId));
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<UseSkillPayloadC2S> {
        @Override
        public void receive(UseSkillPayloadC2S payload, ServerPlayNetworking.Context context) {
            SkillManager.useSkill(context.player(), payload.skillId());
        }
    }
}