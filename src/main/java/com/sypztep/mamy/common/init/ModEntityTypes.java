package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.BloodLustEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModEntityTypes {
    public static EntityType<BloodLustEntity> BLOOD_LUST;

    public static void init() {
        BLOOD_LUST = registerEntity("bloodlust", createEntityTypeSlash(BloodLustEntity::new));
    }

    private static <T extends Entity> EntityType<T> registerEntity(String name, EntityType<T> entityType) {
        return Registry.register(Registries.ENTITY_TYPE, Mamy.id(name), entityType);
    }
    private static <T extends Entity> EntityType<T> createEntityType(EntityType.EntityFactory<T> factory) {
        return FabricEntityTypeBuilder.create(SpawnGroup.MISC, factory).dimensions(EntityDimensions.changing(0.5f, 0.5f)).trackRangeBlocks(126).trackedUpdateRate(20).build();
    }
    private static <T extends Entity> EntityType<T> createNoHitbock(EntityType.EntityFactory<T> factory) {
        return FabricEntityTypeBuilder.create(SpawnGroup.MISC, factory).dimensions(EntityDimensions.changing(0.1f, 0.1f)).trackRangeBlocks(512).trackedUpdateRate(4).build();
    }
    private static <T extends Entity> EntityType<T> createEntityTypeSlash(EntityType.EntityFactory<T> factory) {
        return FabricEntityTypeBuilder.create(SpawnGroup.MISC, factory).dimensions(EntityDimensions.changing(5.0F, 0.2F)).build();
    }
}
