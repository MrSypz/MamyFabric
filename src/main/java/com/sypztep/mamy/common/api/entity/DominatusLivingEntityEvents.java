package com.sypztep.mamy.common.api.entity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public final class DominatusLivingEntityEvents {
    public static final Event<PreArmorDamage> PRE_ARMOR_DAMAGE = EventFactory.createArrayBacked(PreArmorDamage.class, callbacks -> (entity, source, amount, isCrit) -> {
        for (PreArmorDamage callback : callbacks) {
            amount = callback.preModifyDamage(entity, source, amount, isCrit);
            if (amount <= 0.0f) return 0.0f;
        }
        return amount;
    });

    @FunctionalInterface
    public interface PreArmorDamage {
        float preModifyDamage(LivingEntity entity, DamageSource source, float amount, boolean isCrit);
    }
}