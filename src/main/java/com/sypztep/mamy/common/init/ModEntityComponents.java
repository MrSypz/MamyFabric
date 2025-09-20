package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.living.*;
import com.sypztep.mamy.common.component.living.AirHikeComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public final class ModEntityComponents implements EntityComponentInitializer {
    public static final ComponentKey<LivingLevelComponent> LIVINGLEVEL = ComponentRegistry.getOrCreate(Mamy.id("livinglevel"), LivingLevelComponent.class);
    public static final ComponentKey<DamageTrackerComponent> DAMAGETRACKER = ComponentRegistry.getOrCreate(Mamy.id("dmgtracker"), DamageTrackerComponent.class);
    public static final ComponentKey<PlayerClassComponent> PLAYERCLASS = ComponentRegistry.getOrCreate(Mamy.id("playerclass"), PlayerClassComponent.class);
    public static final ComponentKey<PlayerStanceComponent> PLAYERSTANCE = ComponentRegistry.getOrCreate(Mamy.id("playerstance"), PlayerStanceComponent.class);
    public static final ComponentKey<PlayerWeightComponent> PLAYERWEIGHT = ComponentRegistry.getOrCreate(Mamy.id("playerweight"), PlayerWeightComponent.class);
    public static final ComponentKey<PlayerShieldScoreComponent> PLAYERSHIELDSCORE = ComponentRegistry.getOrCreate(Mamy.id("playershieldscore"), PlayerShieldScoreComponent.class);
    public static final ComponentKey<StealComponent> LIVINGSTEAL = ComponentRegistry.getOrCreate(Mamy.id("livingsteal"), StealComponent.class);
    public static final ComponentKey<LivingHidingComponent> HIDING = ComponentRegistry.getOrCreate(Mamy.id("livinghiding"), LivingHidingComponent.class);
    public static final ComponentKey<AirHikeComponent> AIRHIKE = ComponentRegistry.getOrCreate(Mamy.id("airhike"), AirHikeComponent.class);
    public static final ComponentKey<EvasionTimerComponent> EVASIONTIMER = ComponentRegistry.getOrCreate(Mamy.id("evasiontimer"), EvasionTimerComponent.class);

//    public static final ComponentKey<DungeonDataComponent> DUNGEON_DATA = ComponentRegistry.getOrCreate(Mamy.id("dungeon_data"), DungeonDataComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(LivingEntity.class, LIVINGLEVEL).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(LivingLevelComponent::new);
        registry.beginRegistration(PlayerEntity.class, PLAYERCLASS).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(PlayerClassComponent::new);
        registry.registerFor(MobEntity.class, DAMAGETRACKER, entity -> new DamageTrackerComponent());
        registry.registerForPlayers(PLAYERSTANCE, PlayerStanceComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(PLAYERWEIGHT, PlayerWeightComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(PLAYERSHIELDSCORE, PlayerShieldScoreComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerFor(LivingEntity.class, LIVINGSTEAL, StealComponent::new);
        registry.registerFor(LivingEntity.class, HIDING, LivingHidingComponent::new);
        registry.registerFor(PlayerEntity.class, AIRHIKE, AirHikeComponent::new);
        registry.registerFor(LivingEntity.class, EVASIONTIMER, EvasionTimerComponent::new);

//        registry.registerForPlayers(DUNGEON_DATA, DungeonDataComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}