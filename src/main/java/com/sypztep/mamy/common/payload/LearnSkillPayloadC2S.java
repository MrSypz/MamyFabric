package com.sypztep.mamy.common.payload;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.PlayerClassManager;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public record LearnSkillPayloadC2S(Identifier skillId) implements CustomPayload {
    public static final Id<LearnSkillPayloadC2S> ID = new Id<>(Mamy.id("learn_skill"));
    public static final PacketCodec<PacketByteBuf, LearnSkillPayloadC2S> CODEC =
            PacketCodec.tuple(Identifier.PACKET_CODEC, LearnSkillPayloadC2S::skillId, LearnSkillPayloadC2S::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(Identifier skillId) {
        ClientPlayNetworking.send(new LearnSkillPayloadC2S(skillId));
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<LearnSkillPayloadC2S> {
        @Override
        public void receive(LearnSkillPayloadC2S payload, ServerPlayNetworking.Context context) {
            PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(context.player());
            PlayerClassManager classManager = classComponent.getClassManager();

            // Get skill
            Skill skill = SkillRegistry.getSkill(payload.skillId());
            if (skill == null) {
                context.player().sendMessage(Text.literal("Unknown skill!")
                        .formatted(Formatting.RED), false);
                return;
            }

            // Check if already learned
            if (classComponent.hasLearnedSkill(payload.skillId())) {
                context.player().sendMessage(Text.literal("You already know this skill!")
                        .formatted(Formatting.RED), false);
                return;
            }

            // Check if skill is available for current class
            if (!skill.isAvailableForClass(classManager.getCurrentClass())) {
                context.player().sendMessage(Text.literal("Your class cannot learn this skill!")
                        .formatted(Formatting.RED), false);
                return;
            }

            // Check if player has enough class points
            int cost = skill.getBaseClassPointCost();
            int availablePoints = classManager.getClassStatPoints();
            if (availablePoints < cost) {
                context.player().sendMessage(Text.literal(String.format("Not enough class points! Need %d, have %d", cost, availablePoints))
                        .formatted(Formatting.RED), false);
                return;
            }

            // Learn the skill (this will spend the points)
            boolean success = classComponent.learnSkill(payload.skillId());

            if (success) {
                context.player().sendMessage(Text.literal("Learned " + skill.getName() + "!")
                        .formatted(Formatting.GREEN), false);
            } else {
                context.player().sendMessage(Text.literal("Failed to learn skill!")
                        .formatted(Formatting.RED), false);
            }
        }
    }
}