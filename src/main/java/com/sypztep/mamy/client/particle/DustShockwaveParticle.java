package com.sypztep.mamy.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class DustShockwaveParticle extends ShockwaveParticle {
    public DustShockwaveParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, spriteProvider, velocityX, velocityY, velocityZ);
        this.red = 1F; // Brownish color (approx 0x8B4513)
        this.green =1;
        this.blue = 1;
        this.maxAge = 12; // Slightly longer for dust to linger
        this.scale = 5;
        this.velocityY = 0.01; // No vertical motion to stay on ground
    }

    @Override
    protected float getScale(float ticks) {
        float ageRatio = (float) this.age / this.maxAge;
        return scale + ageRatio * 5.0F; // Start at 0.75, grow to 2.75 for spreading effect
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider sprites;

        public Factory(SpriteProvider sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new DustShockwaveParticle(world, x, y, z, sprites, xSpeed, ySpeed, zSpeed);
        }
    }
}