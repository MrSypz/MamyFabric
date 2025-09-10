package com.sypztep.mamy.common.network.server;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.network.client.PlayerAnimationSyncPayloadS2C;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PlayerAnimationSyncPayloadC2S(
        Identifier animationId,
        boolean isPlaying
) implements CustomPayload {

    public static final Id<PlayerAnimationSyncPayloadC2S> ID = new Id<>(Mamy.id("player_animation_sync_c2s"));

    public static final PacketCodec<PacketByteBuf, PlayerAnimationSyncPayloadC2S> CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC, PlayerAnimationSyncPayloadC2S::animationId,
            PacketCodecs.BOOL, PlayerAnimationSyncPayloadC2S::isPlaying,
            PlayerAnimationSyncPayloadC2S::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void sendToServer(Identifier animationId, boolean isPlaying) {
        ClientPlayNetworking.send(new PlayerAnimationSyncPayloadC2S(animationId, isPlaying));
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<PlayerAnimationSyncPayloadC2S> {
        @Override
        public void receive(PlayerAnimationSyncPayloadC2S payload, ServerPlayNetworking.Context context) {
            PlayerAnimationSyncPayloadS2C.sendToClients(context.player(), payload.animationId(), payload.isPlaying());
        }
    }
}