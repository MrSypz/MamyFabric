package com.sypztep.mamy.client.payload;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.util.ElementalDamageDisplay;
import com.sypztep.mamy.common.system.damage.ElementType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

public record ElementalDamagePayloadS2C(
        int targetEntityId,
        Map<String, Float> elementalDamage,
        boolean showBreakdown
) implements CustomPayload {

    public static final Id<ElementalDamagePayloadS2C> ID = new Id<>(Mamy.id("elemental_damage"));

    public static final PacketCodec<PacketByteBuf, ElementalDamagePayloadS2C> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, ElementalDamagePayloadS2C::targetEntityId,
            PacketCodecs.map(HashMap::new, PacketCodecs.STRING, PacketCodecs.FLOAT), ElementalDamagePayloadS2C::elementalDamage,
            PacketCodecs.BOOL, ElementalDamagePayloadS2C::showBreakdown,
            ElementalDamagePayloadS2C::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(ServerPlayerEntity player, int targetEntityId, Map<ElementType, Float> elementalDamage, boolean showBreakdown) {
        Map<String, Float> stringDamage = new HashMap<>();
        elementalDamage.forEach((element, damage) -> stringDamage.put(element.name(), damage));
        ServerPlayNetworking.send(player, new ElementalDamagePayloadS2C(targetEntityId, stringDamage, showBreakdown));
    }

    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<ElementalDamagePayloadS2C> {
        @Override
        public void receive(ElementalDamagePayloadS2C payload, ClientPlayNetworking.Context context) {
            Entity target = context.client().world.getEntityById(payload.targetEntityId());
            if (target == null) return;
            // Convert string back to ElementType using Java 21 pattern matching
            Map<ElementType, Float> elementalDamage = new HashMap<>();
            payload.elementalDamage().forEach((elementName, damage) -> {
                try {
                    ElementType element = ElementType.valueOf(elementName.toUpperCase());
                    if (damage > 0) elementalDamage.put(element, damage);
                } catch (IllegalArgumentException e) {
                    // Ignore unknown element types
                }
            });
            if (elementalDamage.isEmpty()) return;

            if (payload.showBreakdown()) {
                ElementalDamageDisplay.showElementalDamageSmart(target, elementalDamage);
            } else {
                elementalDamage.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .ifPresent(entry -> ElementalDamageDisplay.showElementalDamage(target, entry.getKey(), entry.getValue()));
            }
        }
    }
}