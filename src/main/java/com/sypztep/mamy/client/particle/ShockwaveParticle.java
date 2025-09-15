package com.sypztep.mamy.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public abstract class ShockwaveParticle extends SpriteBillboardParticle {
    protected final SpriteProvider spriteProvider;
    protected static final Quaternionf QUATERNION = new Quaternionf(0F, -0.7F, 0.7F, 0F);
    protected final float startAlpha;
    protected final float fadeStartRatio;

    protected ShockwaveParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.spriteProvider = spriteProvider;
        this.maxAge = 10; // Short lifespan for dust effect
        this.scale = 0.75F; // Base scale, overridden by getScale
        this.gravityStrength = 0.0F; // No gravity for ground dust
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.startAlpha = 1F;
        this.fadeStartRatio = 0.15F;
        this.alpha = startAlpha;
        this.setSpriteForAge(spriteProvider);
    }

    // Abstract method for subclasses to define size scaling
    protected abstract float getScale(float ticks);

    @Override
    public void tick() {
        super.tick();
        this.setSpriteForAge(this.spriteProvider);
        this.scale = getScale((float) this.age / this.maxAge); // Update scale dynamically
        updateAlpha();
    }

    protected void updateAlpha() {
        float scaledAge = this.age * 0.05f;
        float ageRatio = scaledAge / (float) this.maxAge;

        if (ageRatio <= fadeStartRatio) {
            this.alpha = startAlpha;
        } else {
            float fadeProgress = (ageRatio - fadeStartRatio) / (1.0F - fadeStartRatio);
            this.alpha = startAlpha * (1.0F - (fadeProgress * fadeProgress));
        }
        this.alpha = Math.max(0.0F, this.alpha);
    }


    @Override
    public void buildGeometry(VertexConsumer buffer, Camera camera, float ticks) {
        Vec3d vec3 = camera.getPos();
        float x = (float) (MathHelper.lerp(ticks, this.prevPosX, this.x) - vec3.getX());
        float y = (float) (MathHelper.lerp(ticks, this.prevPosY, this.y) - vec3.getY());
        float z = (float) (MathHelper.lerp(ticks, this.prevPosZ, this.z) - vec3.getZ());

        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };

        float scale = this.getScale(ticks);
        for (int i = 0; i < 4; ++i) {
            Vector3f vertex = vertices[i];
            vertex.rotate(QUATERNION);
            vertex.mul(scale);
            vertex.add(x, y, z);
        }

        float minU = this.getMinU();
        float maxU = this.getMaxU();
        float minV = this.getMinV();
        float maxV = this.getMaxV();
        int light = this.getBrightness(ticks);

        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).texture(maxU, maxV).color(this.red, this.green, this.blue, this.alpha).light(light);
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).texture(maxU, minV).color(this.red, this.green, this.blue, this.alpha).light(light);
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).texture(minU, minV).color(this.red, this.green, this.blue, this.alpha).light(light);
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).texture(minU, maxV).color(this.red, this.green, this.blue, this.alpha).light(light);

        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).texture(minU, maxV).color(this.red, this.green, this.blue, this.alpha).light(light);
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).texture(minU, minV).color(this.red, this.green, this.blue, this.alpha).light(light);
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).texture(maxU, minV).color(this.red, this.green, this.blue, this.alpha).light(light);
        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).texture(maxU, maxV).color(this.red, this.green, this.blue, this.alpha).light(light);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public record Factory(SpriteProvider sprites) implements ParticleFactory<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            throw new UnsupportedOperationException("Use subclass factories instead");
        }
    }
}