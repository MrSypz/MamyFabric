package com.sypztep.mamy.common.payload;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.payload.AddAirhikeParticlesPayloadS2C;
import com.sypztep.mamy.common.component.living.ability.PhantomWalkerComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record AirHikePayloadC2S() implements CustomPayload {
    public static final Id<AirHikePayloadC2S> ID =  new Id<>(Mamy.id("airhike"));
    public static final PacketCodec<PacketByteBuf, AirHikePayloadC2S> CODEC = PacketCodec.unit(new AirHikePayloadC2S());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send() {
        ClientPlayNetworking.send(new AirHikePayloadC2S());
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<AirHikePayloadC2S> {
        @Override
        public void receive(AirHikePayloadC2S payload, ServerPlayNetworking.Context context) { // handle on server side
            PhantomWalkerComponent airComponent = ModEntityComponents.PHANTOMWALKER.get(context.player());
            if (airComponent.canUseAirJump()) {
                airComponent.performAirJump();
                PlayerLookup.tracking(context.player()).forEach(foundPlayer -> AddAirhikeParticlesPayloadS2C.send(foundPlayer, context.player().getId()));
            }
        }
    }
}