package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.living.DamageTrackerComponent;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public final class ModEntityComponents implements EntityComponentInitializer {
    public static final ComponentKey<LivingLevelComponent> LIVINGLEVEL = ComponentRegistry.getOrCreate(Mamy.id("livinglevel"), LivingLevelComponent.class);
    public static final ComponentKey<DamageTrackerComponent> DAMAGETRACKER = ComponentRegistry.getOrCreate(Mamy.id("dmgtracker"), DamageTrackerComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(LivingEntity.class, LIVINGLEVEL).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(LivingLevelComponent::new);
        registry.registerFor(MobEntity.class, DAMAGETRACKER, entity -> new DamageTrackerComponent());
    }
}
