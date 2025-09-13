package com.sypztep.mamy.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class ArrowImpactParticle extends ShockwaveParticle {
    public ArrowImpactParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, spriteProvider, velocityX, velocityY, velocityZ);
        this.red = 1.0F; // White color for arrow impact
        this.green = 1.0F;
        this.blue = 1.0F;
        this.maxAge = 10; // Same as original
        this.gravityStrength = 0.008F; // Slight downward pull for impact realism
    }

    @Override
    protected float getScale(float ticks) {
        float ageRatio = (float) this.age / this.maxAge;
        return 0.75F + ageRatio * 0.25F; // Start at 0.75, grow slightly to 1.0 for subtle expansion
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider sprites;

        public Factory(SpriteProvider sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ArrowImpactParticle(world, x, y, z, sprites, xSpeed, ySpeed, zSpeed);
        }
    }
}