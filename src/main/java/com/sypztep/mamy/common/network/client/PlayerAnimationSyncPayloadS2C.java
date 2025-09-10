package com.sypztep.mamy.common.network.client;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.event.animation.NetworkAnimationManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record PlayerAnimationSyncPayloadS2C(
        int playerId,
        Identifier animationId,
        boolean isPlaying
) implements CustomPayload {

    public static final Id<PlayerAnimationSyncPayloadS2C> ID = new Id<>(Mamy.id("player_animation_sync"));

    public static final PacketCodec<PacketByteBuf, PlayerAnimationSyncPayloadS2C> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, PlayerAnimationSyncPayloadS2C::playerId,
            Identifier.PACKET_CODEC, PlayerAnimationSyncPayloadS2C::animationId,
            PacketCodecs.BOOL, PlayerAnimationSyncPayloadS2C::isPlaying,
            PlayerAnimationSyncPayloadS2C::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void sendToClients(ServerPlayerEntity sender, Identifier animationId, boolean isPlaying) {
        PlayerAnimationSyncPayloadS2C payload = new PlayerAnimationSyncPayloadS2C(
                sender.getId(),
                animationId,
                isPlaying
        );

        sender.getServerWorld().getPlayers().forEach(player -> {
            if (player.getId() != sender.getId()) {
                ServerPlayNetworking.send(player, payload);
            }
        });
    }

    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<PlayerAnimationSyncPayloadS2C> {
        @Override
        public void receive(PlayerAnimationSyncPayloadS2C payload, ClientPlayNetworking.Context context) {
            PlayerEntity player = (PlayerEntity) context.player().getWorld().getEntityById(payload.playerId());

            if (player != null) {
                if (payload.isPlaying()) {
                    NetworkAnimationManager.startPlayerAnimation(player, payload.animationId());
                } else {
                    NetworkAnimationManager.stopPlayerAnimation(player);
                }
            }
        }
    }
}