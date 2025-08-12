package com.sypztep.mamy.client.screen.hud;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.event.hud.LevelHudRenderer;
import com.sypztep.mamy.client.util.BlendMode;
import com.sypztep.mamy.client.util.DrawContextUtils;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.PlayerClassManager;
import com.sypztep.mamy.common.system.classes.ResourceType;
import com.sypztep.mamy.common.util.NumberUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class ResourceBarHud {
    // UI Constants - matching LevelHudRenderer style
    private static final int HUD_X = 5;
    private static final int HUD_Y_OFFSET = 45;
    private static final int BAR_WIDTH = 140;
    private static final int BAR_HEIGHT = 8;
    private static final int PADDING = 6;

    // Texture identifiers
    private static final Identifier BACKGROUND_TEXTURE = Mamy.id("textures/gui/hud/resource/card.png");
    private static final Identifier SKY_NIGHT_TEXTURE = Mamy.id("textures/sky_night.png");

    // Color scheme
    private static final int BACKGROUND_DARK = 0xFF000000;
    private static final int BACKGROUND_LIGHT = 0xE01B1B2A;
    private static final int BORDER_COLOR = 0xFF2D3748;
    private static final int BAR_BORDER_COLOR = 0xFF4A5568;
    private static final int TEXT_COLOR = 0xFFF7FAFC;
    private static final int PERCENTAGE_COLOR = 0xFFAAAAAA;

    // Golden gradient line
    private static final int GOLDEN_CENTER = 0xFFFFD700;
    private static final int GOLDEN_EDGE = 0x00FFD700;
    private static final int LINE_HEIGHT = 1;

    // Resource-specific background colors
    private static final int MANA_BAR_BG_COLOR = 0xFF1a1a3d;
    private static final int RAGE_BAR_BG_COLOR = 0xFF3d1a1a;

    // Animation constants
    private static final float RESOURCE_GLOW_DURATION = 2.0f;
    private static final float SLIDE_DURATION = 0.6f;
    private static final float AUTO_HIDE_DELAY = 8.0f;
    private static final float PORTAL_ANIMATION_SPEED = 0.001f;

    // Particle constants
    private static final float PARTICLE_LIFETIME = 3.0f; // How long particles live
    private static final float GRAVITY = 25.0f; // Gravity strength
    private static final float INITIAL_VELOCITY_Y = -30.0f; // Initial upward velocity
    private static final float INITIAL_VELOCITY_X_VARIATION = 20.0f; // Random horizontal spread

    // Portal effect settings
    private static float portalTime = 0.0f;

    // Animation state
    private float animatedResourceProgress = 0.0f;
    private float resourceGainGlowTimer = 0.0f;
    private float slideOffset = 1.0f;
    private float hideTimer = 0.0f;
    private boolean shouldBeVisible = false;

    // Previous values for change detection
    private float lastResourceAmount = -1;
    private ResourceType lastResourceType = null;

    // Particle system
    private final List<ResourceParticle> particles = new ArrayList<>();

    /**
     * Floating number particle class
     */
    private static class ResourceParticle {
        float x, y;           // Position
        float velocityX, velocityY; // Velocity
        float life;           // Remaining lifetime
        float maxLife;        // Maximum lifetime
        String text;          // Text to display
        int color;            // Text color
        boolean isGain;       // Whether this is a gain or loss
        float scale;          // Text scale

        ResourceParticle(float x, float y, float amount, int color, boolean isGain) {
            this.x = x;
            this.y = y;
            this.maxLife = PARTICLE_LIFETIME;
            this.life = maxLife;
            this.isGain = isGain;
            this.color = color;
            this.scale = 1.0f;

            // Format the text
            String prefix = isGain ? "+" : "-";
            this.text = prefix + NumberUtil.formatNumber((long) Math.abs(amount));

            // Random initial velocity
            Random rand = new Random();
            this.velocityX = (rand.nextFloat() - 0.5f) * INITIAL_VELOCITY_X_VARIATION;
            this.velocityY = INITIAL_VELOCITY_Y + rand.nextFloat() * 10.0f; // Slight variation
        }

        void update(float deltaTime) {
            // Apply physics
            x += velocityX * deltaTime;
            y += velocityY * deltaTime;
            velocityY += GRAVITY * deltaTime; // Apply gravity

            // Update lifetime
            life -= deltaTime;

            // Calculate scale and alpha based on lifetime
            float lifeRatio = life / maxLife;
            scale = MathHelper.lerp(1.0f - lifeRatio, 1.0f, 0.3f); // Shrink over time
        }

        boolean isAlive() {
            return life > 0;
        }

        int getAlpha() {
            float lifeRatio = life / maxLife;
            if (lifeRatio > 0.7f) {
                // Fade in phase
                return (int)(255 * ((1.0f - lifeRatio) / 0.3f));
            } else {
                // Fade out phase
                return (int)(255 * (lifeRatio / 0.7f));
            }
        }

        int getColorWithAlpha() {
            int alpha = getAlpha();
            return (alpha << 24) | (color & 0x00FFFFFF);
        }
    }

    /**
     * Render the resource bar HUD with particles
     */
    public void render(DrawContext drawContext, MinecraftClient client, float deltaTime) {
        if (client.player == null) return;

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.getNullable(client.player);
        if (classComponent == null) return;

        PlayerClassManager manager = classComponent.getClassManager();

        // Update portal animation
        portalTime += PORTAL_ANIMATION_SPEED;
        if (portalTime > Math.PI * 20) portalTime = 0.0f;

        // Get resource data
        float currentResource = manager.getCurrentResource();
        float maxResource = manager.getMaxResource();
        ResourceType resourceType = manager.getResourceType();

        // Check for resource changes and spawn particles
        if (lastResourceAmount != currentResource || lastResourceType != resourceType) {
            if (lastResourceAmount >= 0) {
                float change = currentResource - lastResourceAmount;

                if (Math.abs(change) > 0.1f) { // Only show significant changes
                    spawnResourceParticle(change, resourceType);
                }

                if (change > 0) {
                    resourceGainGlowTimer = RESOURCE_GLOW_DURATION;
                }

                shouldBeVisible = true;
                hideTimer = AUTO_HIDE_DELAY;
            } else if (currentResource < maxResource) {
                shouldBeVisible = true;
                hideTimer = AUTO_HIDE_DELAY;
            }

            lastResourceAmount = currentResource;
            lastResourceType = resourceType;
        }

        // Update particles
        updateParticles(deltaTime);

        // Don't render if fully hidden
        if (slideOffset >= 1.0f && !shouldBeVisible) return;

        // Update animations
        updateAnimations(deltaTime, manager);

        // Calculate position
        int screenHeight = client.getWindow().getScaledHeight();
        int hudY = screenHeight - HUD_Y_OFFSET;
        int currentHudX = calculateHudX();

        // Render the enhanced resource bar
        renderEnhancedResourceBar(drawContext, client, manager, currentHudX, hudY, resourceType);

        // Render particles
        renderParticles(drawContext, client);
    }

    /**
     * Spawn a resource usage particle
     */
    private void spawnResourceParticle(float amount, ResourceType resourceType) {
        // Calculate spawn position (center of the resource bar)
        int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
        int barCenterX = calculateHudX() + PADDING + BAR_WIDTH / 2;
        int barCenterY = screenHeight - HUD_Y_OFFSET + PADDING +
                MinecraftClient.getInstance().textRenderer.fontHeight + LINE_HEIGHT + 4 + BAR_HEIGHT / 2;

        // Determine color based on gain/loss and resource type
        boolean isGain = amount > 0;
        int particleColor;

        if (isGain) {
            particleColor = resourceType.getColor(); // Use resource color for gains
        } else {
            particleColor = 0xFFFF4444; // Red for losses
        }

        // Create the particle
        ResourceParticle particle = new ResourceParticle(barCenterX, barCenterY, amount, particleColor, isGain);
        particles.add(particle);

        // Limit particle count to prevent performance issues
        if (particles.size() > 10) {
            particles.removeFirst(); // Remove oldest particle
        }
    }

    /**
     * Update all particles
     */
    private void updateParticles(float deltaTime) {
        Iterator<ResourceParticle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            ResourceParticle particle = iterator.next();
            particle.update(deltaTime);

            if (!particle.isAlive()) {
                iterator.remove();
            }
        }
    }

    /**
     * Render all particles
     */
    private void renderParticles(DrawContext drawContext, MinecraftClient client) {
        TextRenderer textRenderer = client.textRenderer;

        for (ResourceParticle particle : particles) {
            // Calculate scaled text dimensions
            int textWidth = (int)(textRenderer.getWidth(particle.text) * particle.scale);
            int textHeight = (int)(textRenderer.fontHeight * particle.scale);

            // Center the text on the particle position
            int textX = (int)(particle.x - (float) textWidth / 2);
            int textY = (int)(particle.y - (float) textHeight / 2);

            // Draw text with glow effect for better visibility
            int glowColor = 0x40000000; // Semi-transparent black glow

            // Draw glow (offset in multiple directions)
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    if (offsetX == 0 && offsetY == 0) continue;
                    drawScaledText(drawContext, textRenderer, particle.text,
                            textX + offsetX, textY + offsetY, glowColor, particle.scale);
                }
            }

            // Draw main text
            drawScaledText(drawContext, textRenderer, particle.text,
                    textX, textY, particle.getColorWithAlpha(), particle.scale);
        }
    }

    /**
     * Draw scaled text (simple implementation)
     */
    private void drawScaledText(DrawContext drawContext, TextRenderer textRenderer, String text,
                                int x, int y, int color, float scale) {
        if (scale == 1.0f) {
            // No scaling needed
            drawContext.drawText(textRenderer, text, x, y, color, false);
        } else {
            // Apply scaling using matrix transformations
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(x, y, 0);
            drawContext.getMatrices().scale(scale, scale, 1.0f);
            drawContext.drawText(textRenderer, text, 0, 0, color, false);
            drawContext.getMatrices().pop();
        }
    }

    /**
     * Update animations with enhanced easing
     */
    private void updateAnimations(float deltaTime, PlayerClassManager manager) {
        float targetProgress = manager.getResourcePercentage();

        // Smooth progress animation
        float lerpSpeed = 0.06f;
        animatedResourceProgress = MathHelper.lerp(lerpSpeed, animatedResourceProgress, targetProgress);

        // Resource gain glow timer
        if (resourceGainGlowTimer > 0) {
            resourceGainGlowTimer -= deltaTime;
            if (resourceGainGlowTimer < 0) resourceGainGlowTimer = 0;
        }

        // Auto-hide logic
        if (hideTimer > 0) {
            hideTimer -= deltaTime;
            if (hideTimer <= 0) shouldBeVisible = false;
        }

        // Enhanced slide animation with easing
        float targetSlideOffset = shouldBeVisible ? 0.0f : 1.0f;
        float slideSpeed = 1.0f / SLIDE_DURATION;

        if (slideOffset != targetSlideOffset) {
            float direction = targetSlideOffset > slideOffset ? 1.0f : -1.0f;
            slideOffset += direction * slideSpeed * deltaTime;

            if (direction > 0 && slideOffset > targetSlideOffset) slideOffset = targetSlideOffset;
            else if (direction < 0 && slideOffset < targetSlideOffset) slideOffset = targetSlideOffset;
        }
    }

    /**
     * Render enhanced resource bar matching LevelHudRenderer aesthetic
     */
    private void renderEnhancedResourceBar(DrawContext drawContext, MinecraftClient client, PlayerClassManager manager,
                                           int hudX, int hudY, ResourceType resourceType) {
        TextRenderer textRenderer = client.textRenderer;

        // Get resource values
        float currentResource = manager.getCurrentResource();
        float maxResource = manager.getMaxResource();

        // Calculate enhanced dimensions
        int hudWidth = BAR_WIDTH + (PADDING * 2);
        int hudHeight = textRenderer.fontHeight + BAR_HEIGHT + LINE_HEIGHT + (PADDING * 3) + 4;

        // Dynamic colors from ResourceType
        int barColor = resourceType.getColor();
        int glowColor = resourceType.getColorglow();
        int barBgColor = (resourceType == ResourceType.MANA) ? MANA_BAR_BG_COLOR : RAGE_BAR_BG_COLOR;

        // === ENHANCED BACKGROUND WITH GRADIENT ===
        DrawContextUtils.renderVerticalGradientWithBlendedImage(
                drawContext,
                hudX,
                hudY,
                hudWidth,
                hudHeight,
                BACKGROUND_LIGHT,
                BACKGROUND_DARK,
                BACKGROUND_TEXTURE,
                0.2f,
                BlendMode.MULTIPLY_SCREEN_HYBRID,
                DrawContextUtils.ImageScaleMode.FILL,
                256, 128
        );
        drawContext.drawBorder(hudX, hudY, hudWidth, hudHeight, BORDER_COLOR);

        int contentY = hudY + PADDING;

        // Resource type label
        String resourceLabel = resourceType.getDisplayName();
        int labelX = hudX + PADDING;
        drawContext.drawTextWithShadow(textRenderer, resourceLabel, labelX, contentY, barColor);

        // Percentage (right aligned)
        String percentText = String.format("%.0f%%", animatedResourceProgress * 100);
        int percentX = hudX + hudWidth - textRenderer.getWidth(percentText) - PADDING;
        drawContext.drawTextWithShadow(textRenderer, percentText, percentX, contentY, PERCENTAGE_COLOR);

        contentY += textRenderer.fontHeight + 2;

        // === GOLDEN GRADIENT LINE ===
        int lineX = hudX + PADDING;
        int lineWidth = hudWidth - (PADDING * 2);
        DrawContextUtils.renderHorizontalLineWithCenterGradient(drawContext, lineX, contentY, lineWidth, LINE_HEIGHT, 0, GOLDEN_CENTER, GOLDEN_EDGE);
        contentY += LINE_HEIGHT + 2;

        // === ENHANCED PROGRESS BAR WITH PORTAL EFFECT ===
        int barX = hudX + PADDING;
        int barY = contentY;

        drawContext.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, barBgColor);

        int progressWidth = (int) (BAR_WIDTH * animatedResourceProgress);

        if (progressWidth > 0) {
            if (resourceGainGlowTimer > 0) {
                int glowAlpha = LevelHudRenderer.calculateGlowStrength(resourceGainGlowTimer, RESOURCE_GLOW_DURATION);

                // Multiple glow layers for depth
                int outerGlowColor = (glowAlpha / 6) << 24 | (glowColor & 0x00FFFFFF);
                drawContext.fill(barX - 2, barY - 2, barX + progressWidth + 2, barY + BAR_HEIGHT + 2, outerGlowColor);

                int innerGlowColor = (glowAlpha / 3) << 24 | (glowColor & 0x00FFFFFF);
                drawContext.fill(barX - 1, barY - 1, barX + progressWidth + 1, barY + BAR_HEIGHT + 1, innerGlowColor);
            }

            // SECOND: Base gradient fill
            int darkerColor = DrawContextUtils.darkenColor(barColor, 0.7f);
            DrawContextUtils.renderVerticalGradient(drawContext, barX, barY, progressWidth, BAR_HEIGHT, barColor, darkerColor);

            // THIRD: Magical portal effect overlay
            float portalAlpha = 0.2f;
            if (resourceGainGlowTimer > 0) {
                portalAlpha = 0.2f + (resourceGainGlowTimer / RESOURCE_GLOW_DURATION) * 0.1f;
            }

            DrawContextUtils.renderMagicalPortalEffect(drawContext, barX, barY, progressWidth, BAR_HEIGHT,
                    SKY_NIGHT_TEXTURE, portalTime, portalAlpha);
        }

        // Enhanced bar border
        drawContext.drawBorder(barX, barY, BAR_WIDTH, BAR_HEIGHT, BAR_BORDER_COLOR);

        // Inner highlight for depth
        int highlightColor = 0x30FFFFFF;
        drawContext.fill(barX + 1, barY + 1, barX + BAR_WIDTH - 1, barY + 2, highlightColor);

        // === RESOURCE TEXT (BOTTOM) ===
        String resourceText = NumberUtil.formatNumber((long) currentResource) + "/" + NumberUtil.formatNumber((long) maxResource);
        int textX = barX + BAR_WIDTH - textRenderer.getWidth(resourceText);
        int textY = barY + 1;
        drawContext.drawTextWithShadow(textRenderer, resourceText, textX, textY, TEXT_COLOR);
    }

    /**
     * Calculate HUD X position with enhanced easing
     */
    private int calculateHudX() {
        int hudWidth = BAR_WIDTH + (PADDING * 2);
        int hiddenX = -hudWidth - 10;
        float easedOffset = DrawContextUtils.enhancedEaseInOut(slideOffset);
        return MathHelper.lerp(easedOffset, HUD_X, hiddenX);
    }

    /**
     * Force show the resource bar with glow effect
     */
    public void forceShow() {
        shouldBeVisible = true;
        hideTimer = AUTO_HIDE_DELAY;
        resourceGainGlowTimer = RESOURCE_GLOW_DURATION;
    }

    /**
     * Force hide the resource bar
     */
    public void forceHide() {
        shouldBeVisible = false;
        hideTimer = 0;
    }

    /**
     * Trigger glow effect (for skill usage, combat, etc.)
     */
    public void triggerGlow() {
        resourceGainGlowTimer = RESOURCE_GLOW_DURATION;
        forceShow();
    }

    /**
     * Manually spawn a resource particle (for external triggers like skill usage)
     */
    public void spawnParticle(float amount, ResourceType resourceType) {
        spawnResourceParticle(amount, resourceType);
        forceShow(); // Show the bar when manually spawning particles
    }
}