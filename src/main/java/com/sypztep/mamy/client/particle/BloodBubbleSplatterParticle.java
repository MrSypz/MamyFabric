package com.sypztep.mamy.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class BloodBubbleSplatterParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    protected BloodBubbleSplatterParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.setSpriteForAge(spriteProvider);
        this.scale *= 0.5F + this.random.nextFloat() * 0.5F;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
    }
    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_LIT;
    }

    @Override
    public void tick() {
        this.setSpriteForAge(this.spriteProvider);
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        if (this.age++ >= this.maxAge){
            this.markDead();
        } else {
            this.velocityY -= 0.05;
            this.move(this.velocityX,this.velocityY,this.velocityZ);
        }
    }
    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider){
            this.spriteProvider = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            return new BloodBubbleSplatterParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
        }
    }
}
