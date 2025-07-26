package com.sypztep.mamy.common.event.living;

import com.sypztep.mamy.common.component.living.DamageTrackerComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class InitDamageTrackerEvent implements ServerEntityEvents.Load {
    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register(new InitDamageTrackerEvent());
    }
    @Override
    public void onLoad(Entity entity, ServerWorld serverWorld) {
        if (!(entity instanceof LivingEntity livingEntity) || livingEntity instanceof PlayerEntity) return;

        DamageTrackerComponent tracker = ModEntityComponents.DAMAGETRACKER.getNullable(livingEntity);
        if (tracker != null) tracker.setMaxHealth(livingEntity.getMaxHealth());
    }
}
