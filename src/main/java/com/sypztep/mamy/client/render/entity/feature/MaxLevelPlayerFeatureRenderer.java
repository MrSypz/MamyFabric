package com.sypztep.mamy.client.render.entity.feature;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.joml.Matrix4f;

public class MaxLevelPlayerFeatureRenderer {
    private static final Identifier GLOW_TEXTURE = Mamy.id("textures/misc/glow.png");

    private static final float GLOW_RED = 0.5f;     // Light blue
    private static final float GLOW_GREEN = 0.8f;   // Light blue
    private static final float GLOW_BLUE = 1.0f;    // Light blue

    private static final float BASE_GLOW_RADIUS = 3.5f;
    private static final float ANIMATION_DURATION = 60f; // 3 seconds
    private static final float PULSE_PERIOD = 80f; // 4 seconds
    private static final float PULSE_AMPLITUDE = 0.15f;
    private static final float BASE_ALPHA = 1f;
    private static final boolean ANIMATE_GLOW = true;
    // Full bright lighting (same as loot beams)
    private static final int FULL_BRIGHT = 15728880; // 0xF000F0

    public static void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, PlayerEntity player) {
        if (!shouldRenderGlow(player)) return;

        matrices.push();
        matrices.translate(0, 0.01f, 0);

        float radius = BASE_GLOW_RADIUS;
        float alpha = BASE_ALPHA;

        if (ANIMATE_GLOW) {
            float progress = getAnimatedProgress(player, ANIMATION_DURATION);
            if (progress >= 1.0f) {
                float pulseValue = calculatePulse(player.age, (int) ANIMATION_DURATION, (int) PULSE_PERIOD, PULSE_AMPLITUDE);
                alpha += pulseValue;
                radius += pulseValue * 0.5f; // Smaller radius change
            }
        }

        RenderLayer renderLayer = RenderLayer.getEntityTranslucentEmissive(GLOW_TEXTURE);
        VertexConsumer consumer = vertexConsumers.getBuffer(renderLayer);

        renderGlow(matrices, consumer, GLOW_RED, GLOW_GREEN, GLOW_BLUE, alpha, radius);

        matrices.pop();
        // Aura particle spawning
        World world = player.getWorld();
        if (world.isClient) {
            double centerX = player.getX();
            double centerY = player.getBodyY(0); // middle of player
            double centerZ = player.getZ();

            double radiusz = 0.6; // aura spread
            int particleCount = 2; // per tick

            for (int i = 0; i < particleCount; i++) {
                // Random position in a sphere around the player
                double offsetX = (player.getRandom().nextDouble() - 0.5) * 2 * radiusz;
                double offsetY = (player.getRandom().nextDouble() - 0.5) * 2 * radiusz;
                double offsetZ = (player.getRandom().nextDouble() - 0.5) * 2 * radiusz;

                // Random velocity (small drift)
                double velX = (player.getRandom().nextDouble() - 0.5) * 0.1;
                double velY = (player.getRandom().nextDouble() - 0.5) * 0.5;
                double velZ = (player.getRandom().nextDouble() - 0.5) * 0.1;

                world.addParticle(
                        ParticleTypes.REVERSE_PORTAL, // TODO: Replace Custom Sprite
                        centerX + offsetX,
                        centerY + offsetY,
                        centerZ + offsetZ,
                        velX, velY, velZ
                );
            }
        }

    }

    private static boolean shouldRenderGlow(PlayerEntity player) {
        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.get(player);
        return levelComponent.getLevelSystem().isMaxLevel();
    }

    private static float getAnimatedProgress(PlayerEntity player, float animationDuration) {
        float timeAlive = player.age;
        float rawProgress = Math.min(timeAlive / animationDuration, 1.0f);
        return calculateEaseProgress(rawProgress);
    }

    private static float calculateEaseProgress(float progress) {
        // Smooth ease-in-out animation curve
        if (progress < 0.5f) {
            return 2 * progress * progress;
        } else {
            return 1 - 2 * (1 - progress) * (1 - progress);
        }
    }

    private static float calculatePulse(int currentTime, int animationDuration, int pulsePeriod, float pulseAmplitude) {
        if (currentTime < animationDuration) return 0.0f;

        int timeIntoPulse = (currentTime - animationDuration) % pulsePeriod;
        float pulseProgress = (float) timeIntoPulse / pulsePeriod;
        return pulseAmplitude * (float) Math.sin(pulseProgress * Math.PI * 2);
    }

    /**
     * Renders glow with radial gradient from center (full color) to edges (translucent)
     */
    private static void renderGlow(MatrixStack matrices, VertexConsumer builder, float red, float green, float blue, float alpha, float radius) {
        MatrixStack.Entry matrixEntry = matrices.peek();
        Matrix4f pose = matrixEntry.getPositionMatrix();

        // Render multiple concentric layers for smooth gradient
        float[] alphaLevels = {alpha * 0.1f, alpha * 0.4f, alpha * 0.7f, alpha * 1.0f};
        float[] radiusLevels = {radius * 1.0f, radius * 0.75f, radius * 0.5f, radius * 0.25f};

        // Render from outer to inner (largest to smallest)
        for (int i = 0; i < alphaLevels.length; i++) {
            float currentRadius = radiusLevels[i];
            float currentAlpha = alphaLevels[i];

            renderSingleGlowQuad(pose, matrixEntry, builder, red, green, blue, currentAlpha, currentRadius, i * 0.001f);
        }
    }

    //this one correctly render api 1.21.1
    private static void renderSingleGlowQuad(Matrix4f pose, MatrixStack.Entry normal, VertexConsumer builder, float red, float green, float blue, float alpha, float radius, float yOffset) {
        // Bottom-left vertex (-radius, 0, -radius)
        builder.vertex(pose, -radius, yOffset, -radius).color(red, green, blue, alpha).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(FULL_BRIGHT).normal(normal, 0.0f, 1.0f, 0.0f);

        // Bottom-right vertex (-radius, 0, radius)
        builder.vertex(pose, -radius, yOffset, radius).color(red, green, blue, alpha).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(FULL_BRIGHT).normal(normal, 0.0f, 1.0f, 0.0f);

        // Top-right vertex (radius, 0, radius)
        builder.vertex(pose, radius, yOffset, radius).color(red, green, blue, alpha).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(FULL_BRIGHT).normal(normal, 0.0f, 1.0f, 0.0f);

        // Top-left vertex (radius, 0, -radius)
        builder.vertex(pose, radius, yOffset, -radius).color(red, green, blue, alpha).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(FULL_BRIGHT).normal(normal, 0.0f, 1.0f, 0.0f);
    }

    /**
     * Alternative: Multi-layered glow for more dramatic effect
     */
    public static void renderLayeredGlow(MatrixStack matrices, VertexConsumerProvider vertexConsumers, PlayerEntity player) {
        if (!shouldRenderGlow(player) || !player.isOnGround()) {
            return;
        }

        matrices.push();
        matrices.translate(0, 0.001f, 0);

        float baseRadius = BASE_GLOW_RADIUS;
        float baseAlpha = BASE_ALPHA;

        // Use original pulse system instead of loot beam pulse
        if (ANIMATE_GLOW) {
            float progress = getAnimatedProgress(player, ANIMATION_DURATION);
            if (progress >= 1.0f) {
                float pulseValue = calculatePulse(player.age, (int) ANIMATION_DURATION, (int) PULSE_PERIOD, PULSE_AMPLITUDE);
                baseAlpha += pulseValue;
                baseRadius += pulseValue * 0.5f;
            }
        }

        RenderLayer renderLayer = RenderLayer.getEntityTranslucentEmissive(GLOW_TEXTURE);
        VertexConsumer consumer = vertexConsumers.getBuffer(renderLayer);

        // Render multiple layers for depth (largest to smallest)
        renderGlow(matrices, consumer, GLOW_RED, GLOW_GREEN, GLOW_BLUE, baseAlpha * 0.3f, baseRadius * 1.5f);
        renderGlow(matrices, consumer, GLOW_RED, GLOW_GREEN, GLOW_BLUE, baseAlpha * 0.6f, baseRadius);
        renderGlow(matrices, consumer, GLOW_RED, GLOW_GREEN, GLOW_BLUE, baseAlpha * 0.9f, baseRadius * 0.5f);

        matrices.pop();
    }
}