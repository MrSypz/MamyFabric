package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.particle.*;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModParticles {
    public static SimpleParticleType BLOOD_BUBBLE;
    public static SimpleParticleType BLOOD_BUBBLE_SPLATTER;
    public static SimpleParticleType ARROW_IMPACT;
    public static SimpleParticleType METEOR_IMPACT;

    public static class Client{
        public static void init(){
            ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
            registry.register(BLOOD_BUBBLE, BloodBubbleParticle.Factory::new);
            registry.register(BLOOD_BUBBLE_SPLATTER, BloodBubbleSplatterParticle.Factory::new);
            registry.register(ARROW_IMPACT, ArrowImpactParticle.Factory::new);
            registry.register(METEOR_IMPACT, DustShockwaveParticle.Factory::new);
        }
    }

    public static void init() {
        BLOOD_BUBBLE = register("blood_bubble");
        BLOOD_BUBBLE_SPLATTER = register("blood_bubble_splatter");
        ARROW_IMPACT = register("arrow_impact");
        METEOR_IMPACT = register("meteor_impact");
    }

    private static SimpleParticleType register(String name) {
        return Registry.register(Registries.PARTICLE_TYPE, Mamy.id(name), FabricParticleTypes.simple());
    }
}
