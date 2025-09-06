package com.sypztep.mamy.common.network.server;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.living.PlayerStanceComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ToggleStancePayloadC2S() implements CustomPayload {
    public static final Id<ToggleStancePayloadC2S> ID = new Id<>(Mamy.id("toggle_stance"));
    public static final PacketCodec<PacketByteBuf, ToggleStancePayloadC2S> CODEC =
            PacketCodec.unit(new ToggleStancePayloadC2S());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send() {
        ClientPlayNetworking.send(new ToggleStancePayloadC2S());
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<ToggleStancePayloadC2S> {
        @Override
        public void receive(ToggleStancePayloadC2S payload, ServerPlayNetworking.Context context) {
            PlayerStanceComponent stanceComponent = ModEntityComponents.PLAYERSTANCE.get(context.player());
            stanceComponent.toggleStance();
        }
    }
}