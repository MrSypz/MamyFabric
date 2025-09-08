package com.sypztep.mamy.common.network.client;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.screen.CameraShakeManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

public record CameraShakePayloadS2C(
        double x,
        double y,
        double z,
        double time,        // Shake duration
        double radius,      // Shake effect radius
        double amplitude    // Shake intensity
) implements CustomPayload {

    public static final CustomPayload.Id<CameraShakePayloadS2C> ID = new CustomPayload.Id<>(Mamy.id("camera_shake"));

    public static final PacketCodec<RegistryByteBuf, CameraShakePayloadS2C> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, CameraShakePayloadS2C::x,
            PacketCodecs.DOUBLE, CameraShakePayloadS2C::y,
            PacketCodecs.DOUBLE, CameraShakePayloadS2C::z,
            PacketCodecs.DOUBLE, CameraShakePayloadS2C::time,
            PacketCodecs.DOUBLE, CameraShakePayloadS2C::radius,
            PacketCodecs.DOUBLE, CameraShakePayloadS2C::amplitude,
            CameraShakePayloadS2C::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(ServerPlayerEntity player, double x, double y, double z, double time, double radius, double amplitude) {
        ServerPlayNetworking.send(player, new CameraShakePayloadS2C(x, y, z, time, radius, amplitude));
    }
    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<CameraShakePayloadS2C> {
        @Override
        public void receive(CameraShakePayloadS2C payload, ClientPlayNetworking.Context context) {
            CameraShakeManager.getInstance().startShake(payload.time(), payload.radius(), payload.amplitude(), payload.x(), payload.y(), payload.z());
        }
    }
}