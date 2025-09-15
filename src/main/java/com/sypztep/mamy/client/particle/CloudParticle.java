package com.sypztep.mamy.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CloudParticle extends ShockwaveParticle {

    CloudParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, spriteProvider,velocityX, velocityY, velocityZ);
        this.maxAge = 16;
        this.scale = 2.0F + world.getRandom().nextFloat() * 1.5F;
        this.velocityY = velocityY * 0.05;
        this.alpha = 0.0F; // Start invisible, fade in
        this.red = 1;
        this.green = 1;
        this.blue = 1;
    }

    @Override
    protected float getScale(float ticks) {
        float ageRatio = ((float) this.age / this.maxAge);
        return scale * (0.8f + ageRatio * 0.2F); // Start at 0.75, grow to 2.75 for spreading effect    }
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new CloudParticle(world, x, y, z, this.spriteProvider, velocityX, velocityY, velocityZ);
        }
    }
}