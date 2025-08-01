package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.BloodLustEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModEntityTypes {
    public static EntityType<BloodLustEntity> BLOOD_LUST;

    public static void init() {
        BLOOD_LUST = registerEntity("bloodlust", createNoHitbox(BloodLustEntity::new));
    }

    private static <T extends Entity> EntityType<T> registerEntity(String name, EntityType.Builder<T> builder) {
        return Registry.register(Registries.ENTITY_TYPE, Mamy.id(name), builder.build());
    }

    private static <T extends Entity> EntityType.Builder<T> createDefaultEntityType(EntityType.EntityFactory<T> factory) {
        return EntityType.Builder.create(factory, SpawnGroup.MISC)
                .dimensions(0.5f, 0.5f)
                .maxTrackingRange(126)
                .trackingTickInterval(20);
    }

    private static <T extends Entity> EntityType.Builder<T> createNoHitbox(EntityType.EntityFactory<T> factory) {
        return EntityType.Builder.create(factory, SpawnGroup.MISC)
                .dimensions(0.1f, 0.1f)
                .maxTrackingRange(512)
                .trackingTickInterval(4);
    }
}
