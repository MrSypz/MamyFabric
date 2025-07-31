package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.particle.BloodBubbleParticle;
import com.sypztep.mamy.client.particle.BloodBubbleSplatterParticle;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModParticles {
    public static SimpleParticleType BLOOD_BUBBLE = add("blood_bubble");
    public static SimpleParticleType BLOOD_BUBBLE_SPLATTER = add("blood_bubble_splatter");

    public static void init(){
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
        registry.register(BLOOD_BUBBLE, BloodBubbleParticle.Factory::new);
        registry.register(BLOOD_BUBBLE_SPLATTER, BloodBubbleSplatterParticle.Factory::new);
    }

    private static SimpleParticleType add(String name) {
        return Registry.register(Registries.PARTICLE_TYPE, Mamy.id(name), FabricParticleTypes.simple());
    }
}
