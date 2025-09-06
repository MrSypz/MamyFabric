package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.render.entity.ArrowRainEntityRenderer;
import com.sypztep.mamy.client.render.entity.ArrowStrafeEntityRenderer;
import com.sypztep.mamy.client.render.entity.HealingLightEntityRenderer;
import com.sypztep.mamy.common.entity.skill.ArrowRainEntity;
import com.sypztep.mamy.common.entity.skill.ArrowStrafeEntity;
import com.sypztep.mamy.common.entity.skill.HealingLightEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModEntityTypes {
    public static EntityType<HealingLightEntity> HEALING_LIGHT;
    public static EntityType<ArrowRainEntity> ARROW_RAIN;
    public static EntityType<ArrowStrafeEntity> ARROW_STRAFE;

    public static void init() {
        HEALING_LIGHT = registerEntity("healing_light", createSkillProjectile(HealingLightEntity::new));
        ARROW_RAIN = registerEntity("arrow_rain", createSkillProjectile(ArrowRainEntity::new));
        ARROW_STRAFE = registerEntity("arrow_strafe", createSkillProjectile(ArrowStrafeEntity::new));
    }
    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void registerRender() {
            EntityRendererRegistry.register(ModEntityTypes.HEALING_LIGHT, HealingLightEntityRenderer::new);
            EntityRendererRegistry.register(ModEntityTypes.ARROW_RAIN, ArrowRainEntityRenderer::new);
            EntityRendererRegistry.register(ModEntityTypes.ARROW_STRAFE, ArrowStrafeEntityRenderer::new);
        }
    }

    private static <T extends Entity> EntityType<T> registerEntity(String name, EntityType.Builder<T> builder) {
        return Registry.register(Registries.ENTITY_TYPE, Mamy.id(name), builder.build());
    }

    private static <T extends Entity> EntityType.Builder<T> createSkillProjectile(EntityType.EntityFactory<T> factory) {
        return EntityType.Builder.create(factory, SpawnGroup.MISC)
                .dimensions(0.1f, 0.1f)
                .maxTrackingRange(128)
                .trackingTickInterval(4)
                .disableSaving()
                .disableSummon();
    }
}