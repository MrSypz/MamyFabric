package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.render.entity.*;
import com.sypztep.mamy.common.entity.skill.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModEntityTypes {
    public static EntityType<HealingLightEntity> HEALING_LIGHT;
    public static EntityType<ArrowRainEntity> ARROW_RAIN;
    public static EntityType<DoubleStrafeEntity> DOUBLE_STRAFE;
    public static EntityType<MagicArrowEntity> MAGIC_ARROW;
    public static EntityType<FireballEntity> FIREBALL;
    public static EntityType<FireboltEntity> FIREBOLT;
    public static EntityType<MeteorFloorEntity> METEOR_FLOOR;
    public static EntityType<MeteorEntity> METEOR;

    public static void init() {
        HEALING_LIGHT = registerEntity("healing_light", createSkillProjectile(HealingLightEntity::new));
        ARROW_RAIN = registerEntity("arrow_rain", createSkillProjectile(ArrowRainEntity::new));
        DOUBLE_STRAFE = registerEntity("double_strafe", createSkillProjectile(DoubleStrafeEntity::new));
        MAGIC_ARROW = registerEntity("magic_arrow", createSkillProjectile(MagicArrowEntity::new));
        FIREBALL = registerEntity("fireball", createSkillProjectile(FireballEntity::new,1f,1f));
        FIREBOLT = registerEntity("firebolt", createSkillProjectile(FireboltEntity::new));
        METEOR = registerEntity("meteor", createSkillProjectile(MeteorEntity::new,8,8));
        METEOR_FLOOR = registerEntity("meteor_floor", createSkillProjectile(MeteorFloorEntity::new,8,0.1f));
    }

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void registerRender() {
            EntityRendererRegistry.register(ModEntityTypes.HEALING_LIGHT, HealingLightEntityRenderer::new);
            EntityRendererRegistry.register(ModEntityTypes.ARROW_RAIN, ArrowRainEntityRenderer::new);
            EntityRendererRegistry.register(ModEntityTypes.DOUBLE_STRAFE, DoubleStrafeEntityRenderer::new);
            EntityRendererRegistry.register(ModEntityTypes.MAGIC_ARROW, MagicArrowEntityRenderer::new);
            EntityRendererRegistry.register(ModEntityTypes.FIREBALL, FireballEntityRenderer::new);
            EntityRendererRegistry.register(ModEntityTypes.FIREBOLT, FireboltEntityRenderer::new);
            EntityRendererRegistry.register(ModEntityTypes.METEOR, MeteorEntityRenderer::new);
            EntityRendererRegistry.register(ModEntityTypes.METEOR_FLOOR, MeteorFloorEntityRenderer::new);
        }
    }

    private static <T extends Entity> EntityType<T> registerEntity(String name, EntityType.Builder<T> builder) {
        return Registry.register(Registries.ENTITY_TYPE, Mamy.id(name), builder.build());
    }

    private static <T extends Entity> EntityType.Builder<T> createSkillProjectile(EntityType.EntityFactory<T> factory, float width, float height) {
        return EntityType.Builder.create(factory, SpawnGroup.MISC)
                .dimensions(width, height)
                .maxTrackingRange(128)
                .trackingTickInterval(4)
                .disableSaving();
    }

    private static <T extends Entity> EntityType.Builder<T> createSkillProjectile(EntityType.EntityFactory<T> factory) {
        return createSkillProjectile(factory,0.1f,0.1f);
    }
}