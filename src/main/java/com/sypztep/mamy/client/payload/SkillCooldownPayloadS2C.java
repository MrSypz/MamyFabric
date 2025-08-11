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

public record SkillCooldownPayloadS2C(Identifier skillId, long cooldownEndTime) implements CustomPayload {
    public static final CustomPayload.Id<SkillCooldownPayloadS2C> ID =
            new Id<>(Mamy.id("skill_cooldown"));

    public static final PacketCodec<PacketByteBuf, SkillCooldownPayloadS2C> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeIdentifier(value.skillId);
                buf.writeLong(value.cooldownEndTime);
            },
            buf -> new SkillCooldownPayloadS2C(buf.readIdentifier(), buf.readLong())
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(ServerPlayerEntity player, Identifier skillId, long cooldownEndTime) {
        ServerPlayNetworking.send(player, new SkillCooldownPayloadS2C(skillId, cooldownEndTime));
    }

    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<SkillCooldownPayloadS2C> {
        @Override
        public void receive(SkillCooldownPayloadS2C payload, ClientPlayNetworking.Context context) {
            ClientSkillCooldowns.setCooldown(payload.skillId(), payload.cooldownEndTime());
        }
    }
}
