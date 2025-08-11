package com.sypztep.mamy.client.payload;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.skill.ClientSkillCooldowns;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record SkillCooldownPayloadS2C(Identifier skillId, float cooldownSeconds) implements CustomPayload {
    public static final CustomPayload.Id<SkillCooldownPayloadS2C> ID = new Id<>(Mamy.id("skill_cooldown"));

    public static final PacketCodec<PacketByteBuf, SkillCooldownPayloadS2C> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeIdentifier(value.skillId);
                buf.writeFloat(value.cooldownSeconds);
            },
            buf -> new SkillCooldownPayloadS2C(buf.readIdentifier(), buf.readFloat())
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(ServerPlayerEntity player, Identifier skillId, float cooldownSeconds) {
        ServerPlayNetworking.send(player, new SkillCooldownPayloadS2C(skillId, cooldownSeconds));
    }

    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<SkillCooldownPayloadS2C> {
        @Override
        public void receive(SkillCooldownPayloadS2C payload, ClientPlayNetworking.Context context) {
            ClientSkillCooldowns.setCooldown(payload.skillId(), payload.cooldownSeconds());
        }
    }
}