package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.living.*;
import com.sypztep.mamy.common.component.living.ability.HeadShotEntityComponent;
import com.sypztep.mamy.common.component.living.ability.PhantomWalkerComponent;
import com.sypztep.mamy.common.component.living.party.PlayerPartyComponent;
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
    public static final ComponentKey<HeadShotEntityComponent> HEADSHOT = ComponentRegistry.getOrCreate(Mamy.id("headshot"), HeadShotEntityComponent.class);
    public static final ComponentKey<PhantomWalkerComponent> PHANTOMWALKER = ComponentRegistry.getOrCreate(Mamy.id("phantomwalker"), PhantomWalkerComponent.class);

    public static final ComponentKey<PlayerPartyComponent> PLAYERPARTY = ComponentRegistry.getOrCreate(Mamy.id("playerparty"), PlayerPartyComponent.class);
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(LivingEntity.class, LIVINGLEVEL).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(LivingLevelComponent::new);
        registry.beginRegistration(PlayerEntity.class, PLAYERCLASS).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(PlayerClassComponent::new);
        registry.registerFor(MobEntity.class, DAMAGETRACKER, entity -> new DamageTrackerComponent());
        registry.registerForPlayers(PLAYERSTANCE, PlayerStanceComponent::new, RespawnCopyStrategy.ALWAYS_COPY);

        registry.beginRegistration(PlayerEntity.class, PLAYERPARTY).respawnStrategy(RespawnCopyStrategy.LOSSLESS_ONLY).end(PlayerPartyComponent::new);
        registry.registerForPlayers(PLAYERWEIGHT, PlayerWeightComponent::new, RespawnCopyStrategy.ALWAYS_COPY);

        registry.registerFor(LivingEntity.class, HEADSHOT, HeadShotEntityComponent::new);
        registry.registerFor(PlayerEntity.class, PHANTOMWALKER, PhantomWalkerComponent::new);
    }
}
