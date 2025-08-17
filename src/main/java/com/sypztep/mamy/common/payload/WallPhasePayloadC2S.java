package com.sypztep.mamy.common.payload;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record WallPhasePayloadC2S() implements CustomPayload {
    public static final Id<WallPhasePayloadC2S> ID = new Id<>(Mamy.id("wall_phase"));
    public static final PacketCodec<PacketByteBuf, WallPhasePayloadC2S> CODEC = PacketCodec.unit(new WallPhasePayloadC2S());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
    public static void send() {
        ClientPlayNetworking.send(new WallPhasePayloadC2S());
    }
    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<WallPhasePayloadC2S> {
        @Override
        public void receive(WallPhasePayloadC2S payload, ServerPlayNetworking.Context context) {
            var phasingComponent = ModEntityComponents.WALLPHASING.get(context.player());
            phasingComponent.tryPhase();
        }
    }
}