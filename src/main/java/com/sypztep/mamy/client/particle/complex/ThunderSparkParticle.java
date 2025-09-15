package com.sypztep.mamy.client.particle.complex;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.render.VertexContext;
import com.sypztep.mamy.common.init.ModSoundEvents;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class ThunderSparkParticle extends Particle {
    private final Vec3d destination;
    private final List<Vec3d> arcs = new ArrayList<>();
    private final boolean isExplosion;
    private boolean soundPlayed = false; // Flag to prevent sound spam

    private float intensity;
    private float pulsePhase;

    protected ThunderSparkParticle(ClientWorld world, double x, double y, double z, Vec3d destination, boolean isExplosion) {
        super(world, x, y, z);
        this.destination = destination;
        this.isExplosion = isExplosion;
        this.maxAge = isExplosion ? 8 : 12;
        this.intensity = isExplosion ? 1.5f : 1.0f;
        this.pulsePhase = world.getRandom().nextFloat() * 2.0f * (float)Math.PI;

        randomizeArcs();

        // Thunder colors - blue-white electric
        if (isExplosion) {
            setColor(0.8F, 0.9F, 1.0F);
        } else {
            setColor(0.4F, 0.8F, 1.0F);
        }

        // Play ZAP sound when particle spawns
        playZapSound();
    }

    private void playZapSound() {
        if (!soundPlayed) {
            float volume = isExplosion ? 2f :1f;
            float pitch = 0.9f + world.getRandom().nextFloat() * 0.3f;

            world.playSound(x, y, z, ModSoundEvents.ENTITY_ZAP, SoundCategory.AMBIENT, volume, pitch, false);
            soundPlayed = true;
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    @Override
    protected int getBrightness(float tint) {
        return 0xF000F0; // Full brightness for lightning
    }

    @Override
    public void tick() {
        super.tick();

        pulsePhase += 0.8f;

        if (age == 2 && isExplosion && world.getRandom().nextFloat() < 0.3f) {
            float volume = 0.3f;
            float pitch = 1.2f + world.getRandom().nextFloat() * 0.4f;
            world.playSound(x, y, z, ModSoundEvents.ENTITY_ZAP, SoundCategory.AMBIENT, volume, pitch, false);
        }

        if (world.getRandom().nextInt(isExplosion ? 2 : 3) == 0) {
            randomizeArcs();
        }

        float ageRatio = (float) age / maxAge;
        if (isExplosion) {
            intensity = (1.0f - ageRatio * ageRatio) * 2.0f;
        } else {
            float basePulse = 0.7f + 0.3f * (float)Math.sin(pulsePhase);
            intensity = basePulse * (1.0f - ageRatio * 0.7f);
        }

        alpha = Math.min(1.0f, intensity);
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        VertexConsumerProvider.Immediate provider = VertexConsumerProvider.immediate(
                new net.minecraft.client.util.BufferAllocator(1024)
        );

        // Get the lightning render layer
        VertexConsumer lightningConsumer = provider.getBuffer(getLightningRenderLayer());

        // Create matrix stack for transformations
        MatrixStack matrices = new MatrixStack();

        // Apply camera offset
        Vec3d cameraPos = camera.getPos();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        VertexContext context = new VertexContext(matrices, provider);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float thickness = isExplosion ? 0.06f * intensity : 0.04f * intensity;

        // Draw lightning arcs using VertexContext
        for (int i = 1; i < arcs.size(); i++) {
            Vec3d start = arcs.get(i - 1);
            Vec3d end = arcs.get(i);

            drawLightningSegment(context, lightningConsumer, matrix,
                    (float)start.x, (float)start.y, (float)start.z,
                    (float)end.x, (float)end.y, (float)end.z,
                    thickness);
        }

        // Draw the provider to render everything
        provider.draw();
    }

    private void drawLightningSegment(VertexContext context, VertexConsumer consumer, Matrix4f matrix,
                                      float x1, float y1, float z1, float x2, float y2, float z2,
                                      float width) {
        float halfWidth = width * 0.5f;

        // Calculate direction and perpendicular vectors
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (length < 0.001f) return;

        // Normalize direction
        dx /= length;
        dy /= length;
        dz /= length;

        // Create perpendicular vector
        float px, pz;
        if (Math.abs(dy) < 0.9f) {
            // Use cross product with up vector
            px = -dz * halfWidth;
            pz = dx * halfWidth;
        } else {
            // Use cross product with right vector when mostly vertical
            px = halfWidth;
            pz = 0;
        }

        // Calculate color with pulse effect
        float colorVariation = 0.9f + 0.1f * (float)Math.sin(pulsePhase + x1 + y1 + z1);
        float segmentRed = red * colorVariation * intensity;
        float segmentGreen = green * colorVariation * intensity;
        float segmentBlue = blue * colorVariation * intensity;
        float segmentAlpha = alpha * intensity;

        // Draw the lightning segment as a textured quad
        context.fillGradientWithTexture(consumer, matrix,
                x1 - px, y1 - halfWidth, z1 - pz, 0.0f, 0.0f, segmentRed, segmentGreen, segmentBlue, segmentAlpha,
                x1 + px, y1 + halfWidth, z1 + pz, 0.125f, 0.0f, segmentRed, segmentGreen, segmentBlue, segmentAlpha,
                x2 + px, y2 + halfWidth, z2 + pz, 0.125f, 1.0f, segmentRed, segmentGreen, segmentBlue, segmentAlpha * 0.6f,
                x2 - px, y2 - halfWidth, z2 - pz, 0.0f, 1.0f, segmentRed, segmentGreen, segmentBlue, segmentAlpha * 0.6f);

        // Draw second face for thickness (perpendicular)
        float py = halfWidth;
        context.fillGradientWithTexture(consumer, matrix,
                x1, y1 - py, z1 - halfWidth, 0.0f, 0.0f, segmentRed, segmentGreen, segmentBlue, segmentAlpha * 0.8f,
                x1, y1 + py, z1 + halfWidth, 0.125f, 0.0f, segmentRed, segmentGreen, segmentBlue, segmentAlpha * 0.8f,
                x2, y2 + py, z2 + halfWidth, 0.125f, 1.0f, segmentRed, segmentGreen, segmentBlue, segmentAlpha * 0.5f,
                x2, y2 - py, z2 - halfWidth, 0.0f, 1.0f, segmentRed, segmentGreen, segmentBlue, segmentAlpha * 0.5f);
    }

    private void randomizeArcs() {
        arcs.clear();

        double length = Math.sqrt(destination.squaredDistanceTo(x, y, z));
        if (length < 0.1) {
            arcs.add(new Vec3d(x, y, z));
            arcs.add(destination);
            return;
        }

        int segmentCount = Math.min(12, Math.max(3, (int)(length / 0.4)));
        Vec3d direction = destination.subtract(x, y, z).normalize();

        // Start point
        arcs.add(new Vec3d(x, y, z));

        // Intermediate points with electric jitter
        for (int i = 1; i < segmentCount; i++) {
            double progress = (double)i / segmentCount;
            Vec3d basePoint = direction.multiply(length * progress).add(x, y, z);

            double jitterAmount = isExplosion ? 0.8 : 0.5;
            double falloffFactor = Math.sin(progress * Math.PI);

            Vec3d jitteredPoint = basePoint.addRandom(world.getRandom(),
                    (float) (jitterAmount * falloffFactor * intensity));

            arcs.add(jitteredPoint);
        }

        // End point
        arcs.add(destination);
    }

    // Lightning render layer using cleck.png like ThunderSphere
    private static RenderLayer getLightningRenderLayer() {
        return RenderLayer.of(
                "thunder_spark",
                VertexFormats.POSITION_COLOR_TEXTURE_LIGHT,
                VertexFormat.DrawMode.QUADS,
                1536,
                false,
                true,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.POSITION_COLOR_TEXTURE_LIGHTMAP_PROGRAM)
                        .texture(new RenderLayer.Texture(Mamy.id("textures/vfx/white.png"), false, false))
                        .transparency(RenderLayer.ADDITIVE_TRANSPARENCY)
                        .depthTest(RenderLayer.LEQUAL_DEPTH_TEST)
                        .cull(RenderLayer.DISABLE_CULLING)
                        .writeMaskState(RenderLayer.COLOR_MASK)
                        .lightmap(RenderLayer.ENABLE_LIGHTMAP)
                        .target(RenderLayer.MAIN_TARGET)
                        .build(false)
        );
    }

    public static class Factory implements ParticleFactory<SparkParticleEffect> {
        @Override
        public @Nullable Particle createParticle(SparkParticleEffect parameters, ClientWorld world,
                                                 double x, double y, double z,
                                                 double velocityX, double velocityY, double velocityZ) {
            boolean isExplosion = velocityX > 0.5;
            return new ThunderSparkParticle(world, x, y, z, parameters.destination(), isExplosion);
        }
    }
}