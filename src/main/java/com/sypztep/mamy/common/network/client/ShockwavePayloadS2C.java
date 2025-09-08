package com.sypztep.mamy.common.network.client;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.event.ShockwaveHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

public record ShockwavePayloadS2C(
        double x,
        double y,
        double z,
        double time,
        double radius,
        double amplitude
) implements CustomPayload {

    public static final CustomPayload.Id<ShockwavePayloadS2C> ID = new CustomPayload.Id<>(Mamy.id("shockwave"));

    public static final PacketCodec<RegistryByteBuf, ShockwavePayloadS2C> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, ShockwavePayloadS2C::x,
            PacketCodecs.DOUBLE, ShockwavePayloadS2C::y,
            PacketCodecs.DOUBLE, ShockwavePayloadS2C::z,
            PacketCodecs.DOUBLE, ShockwavePayloadS2C::time,
            PacketCodecs.DOUBLE, ShockwavePayloadS2C::radius,
            PacketCodecs.DOUBLE, ShockwavePayloadS2C::amplitude,
            ShockwavePayloadS2C::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(ServerPlayerEntity player, double x, double y, double z, double time, double radius, double amplitude) {
        ServerPlayNetworking.send(player, new ShockwavePayloadS2C(x, y, z, time, radius, amplitude));
    }

    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<ShockwavePayloadS2C> {
        @Override
        public void receive(ShockwavePayloadS2C payload, ClientPlayNetworking.Context context) {
            ShockwaveHandler.handleShockwave(payload.x(), payload.y(), payload.z(), payload.time(), payload.radius(), payload.amplitude());
        }
    }
}