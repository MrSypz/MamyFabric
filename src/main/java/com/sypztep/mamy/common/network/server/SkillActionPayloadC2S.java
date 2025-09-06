package com.sypztep.mamy.common.network.server;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SkillActionPayloadC2S(Identifier skillId, SkillAction action) implements CustomPayload {
    public static final Id<SkillActionPayloadC2S> ID = new Id<>(Mamy.id("skill_action"));
    public static final PacketCodec<PacketByteBuf, SkillActionPayloadC2S> CODEC =
            PacketCodec.tuple(
                    Identifier.PACKET_CODEC, SkillActionPayloadC2S::skillId,
                    SkillAction.PACKET_CODEC, SkillActionPayloadC2S::action,
                    SkillActionPayloadC2S::new
            );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }

    public static void sendLearn(Identifier skillId) {
        ClientPlayNetworking.send(new SkillActionPayloadC2S(skillId, SkillAction.LEARN));
    }

    public static void sendUpgrade(Identifier skillId) {
        ClientPlayNetworking.send(new SkillActionPayloadC2S(skillId, SkillAction.UPGRADE));
    }

    public static void sendUnlearn(Identifier skillId) {
        ClientPlayNetworking.send(new SkillActionPayloadC2S(skillId, SkillAction.UNLEARN));
    }

    public enum SkillAction {
        LEARN,
        UPGRADE,
        UNLEARN;

        public static final PacketCodec<ByteBuf, SkillAction> PACKET_CODEC =
                PacketCodecs.indexed(index -> SkillAction.values()[index], SkillAction::ordinal);

    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<SkillActionPayloadC2S> {
        @Override
        public void receive(SkillActionPayloadC2S payload, ServerPlayNetworking.Context context) {
            PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(context.player());

            switch (payload.action()) {
                case LEARN -> classComponent.learnSkill(payload.skillId());
                case UPGRADE -> classComponent.upgradeSkill(payload.skillId());
                case UNLEARN -> classComponent.unlearnSkill(payload.skillId());
            }
        }
    }
}