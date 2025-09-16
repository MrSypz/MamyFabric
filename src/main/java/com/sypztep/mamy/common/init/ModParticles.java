package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.particle.*;
import com.sypztep.mamy.client.particle.complex.SparkParticleEffect;
import com.sypztep.mamy.client.particle.complex.ThunderSparkParticle;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModParticles {
    public static SimpleParticleType ARROW_IMPACT;
    public static SimpleParticleType METEOR_IMPACT;
    public static ParticleType<SparkParticleEffect> SPARK;
    public static SimpleParticleType CLOUD;
    public static SimpleParticleType AIRHIKE;

    public static class Client{
        public static void init(){
            ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
            registry.register(ARROW_IMPACT, ArrowImpactParticle.Factory::new);
            registry.register(METEOR_IMPACT, BlastShockwaveParticle.Factory::new);
            registry.register(SPARK, new ThunderSparkParticle.Factory());
            registry.register(CLOUD, CloudParticle.Factory::new);
            registry.register(AIRHIKE, AirHikeParticle.Factory::new);
        }
    }

    public static void init() {
        ARROW_IMPACT = register("arrow_impact");
        METEOR_IMPACT = register("meteor_impact");
        SPARK = Registry.register(Registries.PARTICLE_TYPE, Mamy.id("spark"),
                FabricParticleTypes.complex(SparkParticleEffect.CODEC, SparkParticleEffect.PACKET_CODEC));
        CLOUD = register("cloud");
        AIRHIKE = register("airhike");
    }

    private static SimpleParticleType register(String name) {
        return Registry.register(Registries.PARTICLE_TYPE, Mamy.id(name), FabricParticleTypes.simple());
    }
}
