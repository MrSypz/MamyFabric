package com.sypztep.mamy.common.event.player;

import com.sypztep.mamy.common.api.entity.DominatusPlayerEntityEvents;
import com.sypztep.mamy.common.component.living.DamageTrackerComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

public final class MarkDamageTrackerEvent implements DominatusPlayerEntityEvents.DamageDealt {
    public static void register() {
        DominatusPlayerEntityEvents.DAMAGE_DEALT.register(new MarkDamageTrackerEvent());
    }
    @Override
    public void onDamageDealt(LivingEntity entity, DamageSource source, float finalDamage) {
        if (entity.getWorld().isClient()) return; // Server Side Only

        if (entity instanceof PlayerEntity || !(source.getAttacker() instanceof PlayerEntity player)) return;

        DamageTrackerComponent tracker = ModEntityComponents.DAMAGETRACKER.getNullable(entity);

        if (tracker == null) return;

        if (finalDamage > 0) {
            float actualDamage = Math.min(finalDamage, entity.getHealth());
            tracker.addDamage(player, actualDamage);
        }
    }
}
