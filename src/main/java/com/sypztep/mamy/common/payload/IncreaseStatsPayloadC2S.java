package com.sypztep.mamy.common.payload;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record IncreaseStatsPayloadC2S(StatTypes statType) implements CustomPayload {
    public static final Id<IncreaseStatsPayloadC2S> ID = new Id<>(Mamy.id("increase_stats"));
    public static final PacketCodec<PacketByteBuf, IncreaseStatsPayloadC2S> CODEC = PacketCodec.of(
            IncreaseStatsPayloadC2S::encode,
            IncreaseStatsPayloadC2S::decode
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void encode(PacketByteBuf buf) {
        buf.writeEnumConstant(statType);
    }

    public static IncreaseStatsPayloadC2S decode(PacketByteBuf buf) {
        StatTypes statType = buf.readEnumConstant(StatTypes.class);
        return new IncreaseStatsPayloadC2S(statType);
    }

    public static void send(StatTypes statType) {
        ClientPlayNetworking.send(new IncreaseStatsPayloadC2S(statType));
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<IncreaseStatsPayloadC2S> {
        @Override
        public void receive(IncreaseStatsPayloadC2S payload, ServerPlayNetworking.Context context) {
            try {
                LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(context.player());

                boolean success = component.tryIncreaseStat(payload.statType, (short) 1);

                if (success) {
                    Mamy.LOGGER.debug("Player {} increased {} by 1 point",
                            context.player().getName().getString(), payload.statType.getName());
                } else {
                    Mamy.LOGGER.debug("Player {} cannot increase {} (insufficient stat points)",
                            context.player().getName().getString(), payload.statType.getName());
                }

            } catch (Exception e) {
                Mamy.LOGGER.warn("Failed to increase stat {} for player {}: {}",
                        payload.statType.getName(), context.player().getName().getString(), e.getMessage());
            }
        }
    }
}