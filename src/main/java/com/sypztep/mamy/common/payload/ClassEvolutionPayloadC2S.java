package com.sypztep.mamy.common.payload;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.ClassRegistry;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.classes.PlayerClassManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public record ClassEvolutionPayloadC2S(String targetClassId, boolean isTranscendent) implements CustomPayload {
    public static final Id<ClassEvolutionPayloadC2S> ID = new Id<>(Mamy.id("class_evolution"));
    public static final PacketCodec<PacketByteBuf, ClassEvolutionPayloadC2S> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ClassEvolutionPayloadC2S::targetClassId,
            PacketCodecs.BOOL, ClassEvolutionPayloadC2S::isTranscendent,
            ClassEvolutionPayloadC2S::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(String targetClassId, boolean isTranscendent) {
        ClientPlayNetworking.send(new ClassEvolutionPayloadC2S(targetClassId, isTranscendent));
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<ClassEvolutionPayloadC2S> {
        @Override
        public void receive(ClassEvolutionPayloadC2S payload, ServerPlayNetworking.Context context) {
            ServerPlayerEntity player = context.player();
            PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
            PlayerClassManager classManager = classComponent.getClassManager();

            // Validate the target class exists
            PlayerClass targetClass = ClassRegistry.getClass(payload.targetClassId());
            if (targetClass == null) {
                player.sendMessage(Text.literal("Invalid class: " + payload.targetClassId())
                        .formatted(Formatting.RED), false);
                return;
            }

            // Check if player is ready for evolution/transcendence
            boolean canProceed = payload.isTranscendent() ?
                    classManager.isReadyForTranscendence() :
                    classManager.isReadyForEvolution();

            if (!canProceed) {
                player.sendMessage(Text.literal("You are not ready for evolution yet! Requires level 45.")
                        .formatted(Formatting.RED), false);
                return;
            }

            // Validate that the target class is actually available
            boolean isValidTarget = false;
            if (payload.isTranscendent()) {
                isValidTarget = classManager.getAvailableTranscendence().contains(targetClass);
            } else {
                isValidTarget = classManager.getAvailableEvolutions().contains(targetClass);
            }

            if (!isValidTarget) {
                player.sendMessage(Text.literal("Cannot evolve to " + targetClass.getDisplayName() +
                        " from your current class!").formatted(Formatting.RED), false);
                return;
            }

            // Perform the evolution
            boolean success = classComponent.evolveToClass(payload.targetClassId());

            if (success) {
                // Send success message (the evolution method already sends a message,
                // but we can add additional feedback here if needed)
                if (payload.isTranscendent()) {
                    player.sendMessage(Text.literal("✨ TRANSCENDENCE COMPLETE ✨")
                            .formatted(Formatting.GOLD, Formatting.BOLD), false);
                    player.sendMessage(Text.literal("You have been reborn with greater potential!")
                            .formatted(Formatting.YELLOW), false);
                } else {
                    player.sendMessage(Text.literal("Evolution successful! You have grown stronger!")
                            .formatted(Formatting.GREEN), false);
                }
            } else {
                player.sendMessage(Text.literal("Evolution failed! Please try again.")
                        .formatted(Formatting.RED), false);
            }
        }
    }
}