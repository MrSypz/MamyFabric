package com.sypztep.mamy.client.particle.complex;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ThunderSparkParticle extends Particle {
    private final Vec3d destination;
    private final List<Vec3d> arcs = new ArrayList<>();
    private final boolean isExplosion;

    private float intensity;
    private float pulsePhase;

    protected ThunderSparkParticle(ClientWorld world, double x, double y, double z, Vec3d destination, boolean isExplosion) {
        super(world, x, y, z);
        this.destination = destination;
        this.isExplosion = isExplosion;
        this.maxAge = isExplosion ? 8 : 12; // Explosion sparks are shorter
        this.intensity = isExplosion ? 1.5f : 1.0f;
        this.pulsePhase = world.getRandom().nextFloat() * 2.0f * (float)Math.PI;

        randomizeArcs();

        // Thunder colors - blue-white electric
        if (isExplosion) {
            setColor(0.8F, 0.9F, 1.0F); // Brighter for explosion
        } else {
            setColor(0.4F, 0.6F, 1.0F); // Electric blue
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getBrightness(float tint) {
        return 0xF000F0;
    }

    @Override
    public void tick() {
        super.tick();

        // Update pulse phase for electric effect
        pulsePhase += 0.8f;

        // Randomize arcs more frequently for electric effect
        if (world.getRandom().nextInt(isExplosion ? 2 : 3) == 0) {
            randomizeArcs();
        }

        // Update intensity based on age
        float ageRatio = (float) age / maxAge;
        if (isExplosion) {
            // Explosion: bright start, quick fade
            intensity = (1.0f - ageRatio * ageRatio) * 2.0f;
        } else {
            // Normal: pulse with sine wave
            float basePulse = 0.7f + 0.3f * (float)Math.sin(pulsePhase);
            intensity = basePulse * (1.0f - ageRatio * 0.7f);
        }

        // Update alpha based on intensity
        alpha = Math.min(1.0f, intensity);
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Vec3d cameraPos = camera.getPos();
        Matrix4f positionMatrix = new Matrix4f();

        // Scale thickness based on intensity and whether it's an explosion
        float thickness = isExplosion ? 0.04f * intensity : 0.025f * intensity;

        for (int i = 1; i < arcs.size(); i++) {
            Vec3d start = arcs.get(i - 1), end = arcs.get(i);
            float startX = (float) (start.getX() - cameraPos.getX());
            float startY = (float) (start.getY() - cameraPos.getY());
            float startZ = (float) (start.getZ() - cameraPos.getZ());
            float endX = (float) (end.getX() - cameraPos.getX());
            float endY = (float) (end.getY() - cameraPos.getY());
            float endZ = (float) (end.getZ() - cameraPos.getZ());

            Vector3f direction = new Vector3f(endX - startX, endY - startY, endZ - startZ);
            if (direction.length() < 0.001f) continue; // Skip degenerate segments

            direction.normalize();

            // Create perpendicular vectors for the spark "tube"
            Vector3f perpendicular1 = new Vector3f();
            Vector3f perpendicular2 = new Vector3f();

            // Find a vector perpendicular to direction
            if (Math.abs(direction.y) < 0.9f) {
                perpendicular1.set(0, 1, 0);
            } else {
                perpendicular1.set(1, 0, 0);
            }

            // Cross product to get first perpendicular
            direction.cross(perpendicular1, perpendicular1);
            perpendicular1.normalize();
            perpendicular1.mul(thickness);

            // Cross product to get second perpendicular
            direction.cross(perpendicular1, perpendicular2);
            perpendicular2.normalize();
            perpendicular2.mul(thickness);

            // Draw the spark as a rectangular tube with proper UV coordinates
            drawSparkSegment(vertexConsumer, positionMatrix,
                    startX, startY, startZ,
                    endX, endY, endZ,
                    perpendicular1, perpendicular2);
        }
    }

    private void drawSparkSegment(VertexConsumer consumer, Matrix4f matrix,
                                  float startX, float startY, float startZ,
                                  float endX, float endY, float endZ,
                                  Vector3f perp1, Vector3f perp2) {

        // Calculate segment-specific color variation
        float colorVariation = 0.9f + 0.1f * (float)Math.sin(pulsePhase + startX + startY + startZ);
        float segmentRed = red * colorVariation * intensity;
        float segmentGreen = green * colorVariation * intensity;
        float segmentBlue = blue * colorVariation * intensity;
        float segmentAlpha = alpha * intensity;

        // UV coordinates สำหรับ texture
        float minU = 0.0f;
        float maxU = 1.0f;
        float minV = 0.0f;
        float maxV = 1.0f;

        // Get light level (full bright for lightning effect)
        int light = 15728880; // Maximum light level (0xF000F0)

        // Draw 4 faces of the rectangular tube
        // Face 1: +perp1
        drawQuadWithUV(consumer, matrix,
                startX + perp1.x(), startY + perp1.y(), startZ + perp1.z(),
                endX + perp1.x(), endY + perp1.y(), endZ + perp1.z(),
                endX + perp2.x(), endY + perp2.y(), endZ + perp2.z(),
                startX + perp2.x(), startY + perp2.y(), startZ + perp2.z(),
                segmentRed, segmentGreen, segmentBlue, segmentAlpha,
                minU, maxU, minV, maxV, light);

        // Face 2: +perp2
        drawQuadWithUV(consumer, matrix,
                startX + perp2.x(), startY + perp2.y(), startZ + perp2.z(),
                endX + perp2.x(), endY + perp2.y(), endZ + perp2.z(),
                endX - perp1.x(), endY - perp1.y(), endZ - perp1.z(),
                startX - perp1.x(), startY - perp1.y(), startZ - perp1.z(),
                segmentRed, segmentGreen, segmentBlue, segmentAlpha,
                minU, maxU, minV, maxV, light);

        // Face 3: -perp1
        drawQuadWithUV(consumer, matrix,
                startX - perp1.x(), startY - perp1.y(), startZ - perp1.z(),
                endX - perp1.x(), endY - perp1.y(), endZ - perp1.z(),
                endX - perp2.x(), endY - perp2.y(), endZ - perp2.z(),
                startX - perp2.x(), startY - perp2.y(), startZ - perp2.z(),
                segmentRed, segmentGreen, segmentBlue, segmentAlpha,
                minU, maxU, minV, maxV, light);

        // Face 4: -perp2
        drawQuadWithUV(consumer, matrix,
                startX - perp2.x(), startY - perp2.y(), startZ - perp2.z(),
                endX - perp2.x(), endY - perp2.y(), endZ - perp2.z(),
                endX + perp1.x(), endY + perp1.y(), endZ + perp1.z(),
                startX + perp1.x(), startY + perp1.y(), startZ + perp1.z(),
                segmentRed, segmentGreen, segmentBlue, segmentAlpha,
                minU, maxU, minV, maxV, light);
    }

    private void drawQuadWithUV(VertexConsumer consumer, Matrix4f matrix,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                float x3, float y3, float z3,
                                float x4, float y4, float z4,
                                float r, float g, float b, float a,
                                float minU, float maxU, float minV, float maxV, int light) {
        // Front face with proper UV coordinates
        consumer.vertex(matrix, x1, y1, z1).texture(minU, minV).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x2, y2, z2).texture(maxU, minV).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x3, y3, z3).texture(maxU, maxV).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x4, y4, z4).texture(minU, maxV).color(r, g, b, a).light(light);

        // Back face (reverse winding) with proper UV coordinates
        consumer.vertex(matrix, x1, y1, z1).texture(minU, minV).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x4, y4, z4).texture(minU, maxV).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x3, y3, z3).texture(maxU, maxV).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x2, y2, z2).texture(maxU, minV).color(r, g, b, a).light(light);
    }

    private void randomizeArcs() {
        arcs.clear();

        double length = Math.sqrt(destination.squaredDistanceTo(x, y, z));
        if (length < 0.1) {
            arcs.add(new Vec3d(x, y, z));
            arcs.add(destination);
            return;
        }

        // More segments for longer distances, but limit for performance
        int segmentCount = Math.min(12, Math.max(3, (int)(length / 0.4)));

        Vec3d direction = destination.subtract(x, y, z).normalize();

        // Start point
        arcs.add(new Vec3d(x, y, z));

        // Intermediate points with electric jitter
        for (int i = 1; i < segmentCount; i++) {
            double progress = (double)i / segmentCount;
            Vec3d basePoint = direction.multiply(length * progress).add(x, y, z);

            // Add electric jitter - more chaotic for explosions
            double jitterAmount = isExplosion ? 0.8 : 0.5;
            double falloffFactor = Math.sin(progress * Math.PI); // More jitter in the middle

            Vec3d jitteredPoint = basePoint.addRandom(world.getRandom(),
                    (float) (jitterAmount * falloffFactor * intensity));

            arcs.add(jitteredPoint);
        }

        // End point
        arcs.add(destination);
    }

    public static class Factory implements ParticleFactory<SparkParticleEffect> {
        @Override
        public @Nullable Particle createParticle(SparkParticleEffect parameters, ClientWorld world,
                                                 double x, double y, double z,
                                                 double velocityX, double velocityY, double velocityZ) {
            // Use velocityX as a flag for explosion sparks (1.0 = explosion, 0.0 = normal)
            boolean isExplosion = velocityX > 0.5;
            return new ThunderSparkParticle(world, x, y, z, parameters.destination(), isExplosion);
        }
    }
}