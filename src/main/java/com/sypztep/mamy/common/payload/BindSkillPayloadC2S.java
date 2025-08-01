package com.sypztep.mamy.common.payload;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public record BindSkillPayloadC2S(int slot, String skillId) implements CustomPayload {
    public static final Id<BindSkillPayloadC2S> ID = new Id<>(Mamy.id("bind_skill"));
    public static final PacketCodec<PacketByteBuf, BindSkillPayloadC2S> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER, BindSkillPayloadC2S::slot,
                    PacketCodecs.STRING, BindSkillPayloadC2S::skillId,
                    BindSkillPayloadC2S::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(int slot, Identifier skillId) {
        String skillIdStr = skillId != null ? skillId.toString() : "";
        ClientPlayNetworking.send(new BindSkillPayloadC2S(slot, skillIdStr));
    }

    public static void unbind(int slot) {
        ClientPlayNetworking.send(new BindSkillPayloadC2S(slot, ""));
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<BindSkillPayloadC2S> {
        @Override
        public void receive(BindSkillPayloadC2S payload, ServerPlayNetworking.Context context) {
            var classComponent = ModEntityComponents.PLAYERCLASS.get(context.player());

            // Validate slot
            if (payload.slot() < 0 || payload.slot() >= 8) {
                context.player().sendMessage(Text.literal("Invalid skill slot: " + payload.slot())
                        .formatted(Formatting.RED), false);
                return;
            }

            // Handle unbinding (empty skillId)
            if (payload.skillId().isEmpty()) {
                boolean success = classComponent.bindSkill(payload.slot(), null);

                if (success) {
                    context.player().sendMessage(Text.literal("Unbound skill from slot " + (payload.slot() + 1))
                            .formatted(Formatting.YELLOW), false);
                } else {
                    context.player().sendMessage(Text.literal("Failed to unbind skill!")
                            .formatted(Formatting.RED), false);
                }
                return;
            }

            // Parse skill ID for binding
            Identifier skillId;
            try {
                skillId = Identifier.of(payload.skillId());
            } catch (Exception e) {
                context.player().sendMessage(Text.literal("Invalid skill ID!")
                        .formatted(Formatting.RED), false);
                return;
            }

            // Check if player has learned this skill
            if (!classComponent.hasLearnedSkill(skillId)) {
                context.player().sendMessage(Text.literal("You haven't learned this skill yet!")
                        .formatted(Formatting.RED), false);
                return;
            }

            // Bind the skill
            boolean success = classComponent.bindSkill(payload.slot(), skillId);

            if (success) {
                context.player().sendMessage(Text.literal("Bound skill to slot " + (payload.slot() + 1))
                        .formatted(Formatting.GREEN), false);
            } else {
                context.player().sendMessage(Text.literal("Failed to bind skill!")
                        .formatted(Formatting.RED), false);
            }
        }
    }
}