package com.sypztep.mamy.client.util;

import com.sypztep.mamy.common.init.ModCustomParticles;
import net.minecraft.entity.Entity;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TextParticleRegistry {
    private static final Map<Integer, TextParticleProvider> REGISTRY = new LinkedHashMap<>();
    private static int nextId = 0;

    public static int register(TextParticleProvider provider) {
        int id = nextId++;
        REGISTRY.put(id, provider);
        return id;
    }

    public static void handleParticle(Entity entity, int id) {
        TextParticleProvider provider = REGISTRY.get(id);
        if (provider != null) {
            provider.spawnParticle(entity);
        }
    }
    public static int getParticleId(TextParticleProvider provider) {
        return ModCustomParticles.PARTICLES.get(provider);
    }
}
