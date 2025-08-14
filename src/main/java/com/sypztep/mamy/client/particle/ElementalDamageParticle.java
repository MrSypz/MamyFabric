package com.sypztep.mamy.client.particle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;

public final class ElementalDamageParticle extends Particle {
    // Animation phases - similar to TextParticle but longer
    private static final int FLICK_DURATION = 15;    // White flash to color phase
    private static final int STABLE_DURATION = 25;   // Stable display phase
    private static final int FADE_DURATION = 15;     // Fade out phase

    private static final float VELOCITY_DAMPEN = 0.9f;
    private static final float FADE_AMOUNT = 0.1f;

    private String text;
    private float scale;
    private float maxSize;
    private final float targetRed, targetGreen, targetBlue;
    private float targetAlpha = 1.0f;

    public ElementalDamageParticle(ClientWorld world, double x, double y, double z, Vec3d initialVelocity, Color color) {
        super(world, x, y, z);

        this.maxAge = FLICK_DURATION + STABLE_DURATION + FADE_DURATION; // Total ~55 ticks
        this.scale = 0.0f;
        this.maxSize = -0.055f;

        // Set color targets
        this.targetRed = color.getRed() / 255.0f;
        this.targetGreen = color.getGreen() / 255.0f;
        this.targetBlue = color.getBlue() / 255.0f;
        super.setColor(1.0f, 1.0f, 1.0f); // Start white for flash effect

        // Set initial velocity (similar to TextParticle)
        this.velocityX = initialVelocity.x;
        this.velocityY = initialVelocity.y;
        this.velocityZ = initialVelocity.z;

        // Use similar gravity to TextParticle
        this.gravityStrength = -0.125f;
    }

    public void setMaxSize(float maxSize) {
        this.maxSize = maxSize;
    }

    public void setText(@NotNull String text) {
        this.text = text;
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.age <= FLICK_DURATION) {
            // Phase 1: Flash from white to target color (similar to TextParticle)
            tickFlickPhase();
        } else if (this.age <= FLICK_DURATION + STABLE_DURATION) {
            // Phase 2: Stable display
            tickStablePhase();
        } else {
            // Phase 3: Fade out
            tickFadePhase();
        }

        // Apply velocity dampening (similar to TextParticle)
        this.velocityY *= VELOCITY_DAMPEN;

        // Update position
        this.x += this.velocityX;
        this.y += this.velocityY;
        this.z += this.velocityZ;

        this.age++;
        if (this.age >= this.maxAge) {
            this.markDead();
        }
    }

    private void tickFlickPhase() {
        float progress = this.age / (float) FLICK_DURATION;
        float easeProgress = ease(progress, 0.0f, 1.0f, 1.0f); // Use TextParticle's easing

        // Flash from white to target color
        setColor(
                MathHelper.lerp(easeProgress, 1.0f, targetRed),
                MathHelper.lerp(easeProgress, 1.0f, targetGreen),
                MathHelper.lerp(easeProgress, 1.0f, targetBlue)
        );

        // Scale up with easing
        this.scale = MathHelper.lerp(easeProgress, 0.0f, this.maxSize);
        this.alpha = MathHelper.lerp(progress, 1.0f, targetAlpha);
    }

    private void tickStablePhase() {
        // Maintain stable appearance
        setColor(targetRed, targetGreen, targetBlue);
        this.scale = this.maxSize;
        this.alpha = targetAlpha;
    }

    private void tickFadePhase() {
        int phaseAge = this.age - FLICK_DURATION - STABLE_DURATION;
        float progress = phaseAge / (float) FADE_DURATION;

        // Fade out similar to TextParticle
        setColor(
                targetRed * (1f - progress * FADE_AMOUNT),
                targetGreen * (1f - progress * FADE_AMOUNT),
                targetBlue * (1f - progress * FADE_AMOUNT)
        );
        this.scale = MathHelper.lerp(progress, this.maxSize, 0.0f);
        this.alpha = MathHelper.lerp(progress, targetAlpha, 0.0f);
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        if (text == null || text.isEmpty()) return;

        Vec3d cameraPos = camera.getPos();
        float particleX = (float) (prevPosX + (x - prevPosX) * tickDelta - cameraPos.x);
        float particleY = (float) (prevPosY + (y - prevPosY) * tickDelta - cameraPos.y);
        float particleZ = (float) (prevPosZ + (z - prevPosZ) * tickDelta - cameraPos.z);

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        var vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
        float textX = textRenderer.getWidth(text) / -2.0F;

        Matrix4f matrix = new Matrix4f()
                .translation(particleX, particleY, particleZ)
                .rotate(camera.getRotation())
                .rotate((float) Math.PI, 0.0F, 1.0F, 0.0F)
                .scale(scale, scale, scale);

        Vector3f offset = new Vector3f(0.0f, 0.0f, 0.03f);
        int textColor = new Color(
                clamp(red, 0.0f, 1.0f),
                clamp(green, 0.0f, 1.0f),
                clamp(blue, 0.0f, 1.0f),
                clamp(alpha, 0.001f, 1.0f)
        ).getRGB();

        int borderColor = new Color(0f, 0f, 0f, clamp(alpha * 0.8f, 0.001f, 1.0f)).getRGB();

        // Draw border (outline)
        for (int[] pos : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            matrix.translate(offset.set(0.0f, 0.0f, 0.001f));
            textRenderer.draw(text, textX + pos[0], pos[1], borderColor, false, matrix,
                    vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        }

        // Draw main text
        matrix.translate(offset.set(0.0f, 0.0f, 0.001f));
        textRenderer.draw(text, textX, 0, textColor, false, matrix,
                vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);

        vertexConsumers.draw();
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    // TextParticle's easing function
    private float ease(float t, float b, float c, float d) {
        float a = -1;
        float p;
        if (t == 0) return b;
        if ((t /= d) == 1) return b + c;
        p = d * .3f;
        float s;
        if (a < Math.abs(c)) {
            a = c;
            s = p / 4;
        } else s = p / (float) (2 * Math.PI) * (float) Math.asin(c / a);
        return a * (float) Math.pow(2, -10 * t) * (float) Math.sin((t * d - s) * (2 * Math.PI) / p) + c + b;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    // Factory method using outward speed pattern like your code
    public static ElementalDamageParticle createWithOutwardSpeed(ClientWorld world, double x, double y, double z,
                                                                 Vec3d position, Random random, Color color) {
        Vec3d velocity = getOutwardSpeed(position, random);
        return new ElementalDamageParticle(world, x, y, z, velocity, color);
    }

    // Your outward speed method adapted for Vec3d
    public static Vec3d getOutwardSpeed(Vec3d position, Random random) {
        // Normalize the vector to get the direction
        Vec3d direction = position.normalize();
        // Apply random rotation variation
        float randomLen = 0.02f + random.nextFloat() * 0.04f;
        float angleVariation = random.nextFloat() * 0.3f; // variation up to ±22.5 degrees
        float sin = MathHelper.sin(angleVariation);
        float cos = MathHelper.cos(angleVariation);
        double newX = direction.x * cos - direction.z * sin;
        double newZ = direction.x * sin + direction.z * cos; // Fixed: should be newZ, not newY
        return new Vec3d(newX * randomLen, 0, newZ * randomLen);
    }

    // Helper method to create multiple particles with outward spread (like your spawnNumber)
    public static void spawnOutwardBurst(ClientWorld world, double x, double y, double z,
                                         String text, Color color, int count, Random random) {
        for (int i = 0; i < count; i++) {
            // Create random position around the spawn point for outward direction
            Vec3d randomPos = new Vec3d(
                    random.nextGaussian() * 0.5,
                    0,
                    random.nextGaussian() * 0.5
            );

            Vec3d velocity = getOutwardSpeed(randomPos, random);
            ElementalDamageParticle particle = new ElementalDamageParticle(world, x, y + 1, z, velocity, color);
            particle.setText(text);

            MinecraftClient.getInstance().particleManager.addParticle(particle);
        }
    }
}