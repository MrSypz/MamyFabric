package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.render.entity.ArrowRainEntityRenderer;
import com.sypztep.mamy.client.render.entity.DoubleStrafeEntityRenderer;
import com.sypztep.mamy.client.render.entity.HealingLightEntityRenderer;
import com.sypztep.mamy.client.render.entity.MagicArrowEntityRenderer;
import com.sypztep.mamy.common.entity.skill.ArrowRainEntity;
import com.sypztep.mamy.common.entity.skill.DoubleStrafeEntity;
import com.sypztep.mamy.common.entity.skill.HealingLightEntity;
import com.sypztep.mamy.common.entity.skill.MagicArrowEntity;
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

    public static void init() {
        HEALING_LIGHT = registerEntity("healing_light", createSkillProjectile(HealingLightEntity::new));
        ARROW_RAIN = registerEntity("arrow_rain", createSkillProjectile(ArrowRainEntity::new));
        DOUBLE_STRAFE = registerEntity("double_strafe", createSkillProjectile(DoubleStrafeEntity::new));
        MAGIC_ARROW = registerEntity("magic_arrow", createSkillProjectile(MagicArrowEntity::new));
    }
    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void registerRender() {
            EntityRendererRegistry.register(ModEntityTypes.HEALING_LIGHT, HealingLightEntityRenderer::new);
            EntityRendererRegistry.register(ModEntityTypes.ARROW_RAIN, ArrowRainEntityRenderer::new);
            EntityRendererRegistry.register(ModEntityTypes.DOUBLE_STRAFE, DoubleStrafeEntityRenderer::new);
            EntityRendererRegistry.register(ModEntityTypes.MAGIC_ARROW, MagicArrowEntityRenderer::new);
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