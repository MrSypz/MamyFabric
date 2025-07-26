package com.sypztep.mamy.common.api.entity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public final class DominatusPlayerEntityEvents {
    public static final Event<DamageDealt> DAMAGE_DEALT = EventFactory.createArrayBacked(DamageDealt.class, (listeners) -> (entity, source, finalDamage) -> {
        for (DamageDealt listener : listeners) {
            listener.onDamageDealt(entity, source, finalDamage);
        }
    });

    @FunctionalInterface
    public interface DamageDealt {
        void onDamageDealt(LivingEntity entity, DamageSource source, float finalDamage);
    }
}