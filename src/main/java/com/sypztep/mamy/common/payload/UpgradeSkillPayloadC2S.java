package com.sypztep.mamy.common.payload;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityComponents;
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

public record UpgradeSkillPayloadC2S(Identifier skillId) implements CustomPayload {
    public static final Id<UpgradeSkillPayloadC2S> ID = new Id<>(Mamy.id("upgrade_skill"));
    public static final PacketCodec<PacketByteBuf, UpgradeSkillPayloadC2S> CODEC =
            PacketCodec.tuple(Identifier.PACKET_CODEC, UpgradeSkillPayloadC2S::skillId, UpgradeSkillPayloadC2S::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(Identifier skillId) {
        ClientPlayNetworking.send(new UpgradeSkillPayloadC2S(skillId));
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<UpgradeSkillPayloadC2S> {
        @Override
        public void receive(UpgradeSkillPayloadC2S payload, ServerPlayNetworking.Context context) {
            var classComponent = ModEntityComponents.PLAYERCLASS.get(context.player());
            var classManager = classComponent.getClassManager();

            // Get skill
            Skill skill = SkillRegistry.getSkill(payload.skillId());
            if (skill == null) {
                context.player().sendMessage(Text.literal("Unknown skill!")
                        .formatted(Formatting.RED), false);
                return;
            }

            // Check if skill is learned
            if (!classComponent.hasLearnedSkill(payload.skillId())) {
                context.player().sendMessage(Text.literal("You haven't learned this skill yet!")
                        .formatted(Formatting.RED), false);
                return;
            }

            // Get current skill level
            int currentLevel = classComponent.getSkillLevel(payload.skillId());

            // Check if already at max level
            if (currentLevel >= skill.getMaxSkillLevel()) {
                context.player().sendMessage(Text.literal("This skill is already at maximum level!")
                        .formatted(Formatting.RED), false);
                return;
            }

            // Check if player has enough class points
            int cost = skill.getUpgradeClassPointCost();
            int availablePoints = classManager.getClassStatPoints();
            if (availablePoints < cost) {
                context.player().sendMessage(Text.literal(String.format("Not enough class points! Need %d, have %d", cost, availablePoints))
                        .formatted(Formatting.RED), false);
                return;
            }

            // Upgrade the skill (this will spend the points)
            boolean success = classComponent.upgradeSkill(payload.skillId());

            if (success) {
                int newLevel = classComponent.getSkillLevel(payload.skillId());
                context.player().sendMessage(Text.literal("Upgraded " + skill.getName() + " to level " + newLevel + "!")
                        .formatted(Formatting.GOLD), false);
            } else {
                context.player().sendMessage(Text.literal("Failed to upgrade skill!")
                        .formatted(Formatting.RED), false);
            }
        }
    }
}
