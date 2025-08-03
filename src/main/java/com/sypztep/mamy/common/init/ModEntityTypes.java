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
        BLOOD_LUST = registerEntity("bloodlust", createSkillProjectile(BloodLustEntity::new));
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

    /**
     * For skill projectiles that should NOT be saved to world files
     * This prevents NBT serialization crashes with empty ItemStacks
     */
    private static <T extends Entity> EntityType.Builder<T> createSkillProjectile(EntityType.EntityFactory<T> factory) {
        return EntityType.Builder.create(factory, SpawnGroup.MISC)
                .dimensions(0.1f, 0.1f)
                .maxTrackingRange(512)
                .trackingTickInterval(4)
                .disableSaving()      // ✅ This prevents NBT save crashes!
                .disableSummon();     // ✅ Prevents /summon command usage
    }

    /**
     * For persistent skill entities that SHOULD be saved (rare cases)
     */
    private static <T extends Entity> EntityType.Builder<T> createPersistentSkillEntity(EntityType.EntityFactory<T> factory) {
        return EntityType.Builder.create(factory, SpawnGroup.MISC)
                .dimensions(0.25f, 0.25f)
                .maxTrackingRange(128)
                .trackingTickInterval(10);
        // Note: No .disableSaving() - these will be saved normally
    }

    /**
     * For hook/utility entities (temporary, no saving needed)
     */
    private static <T extends Entity> EntityType.Builder<T> createUtilityEntity(EntityType.EntityFactory<T> factory) {
        return EntityType.Builder.create(factory, SpawnGroup.MISC)
                .dimensions(0.25f, 0.25f)
                .maxTrackingRange(8)
                .trackingTickInterval(5)
                .disableSaving()
                .disableSummon();
    }
}