package com.sypztep.mamy.client.screen.hud;

import com.sypztep.mamy.client.event.hud.LevelHudRenderer;
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
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class ResourceBarHud {
    // UI Constants
    private static final int HUD_X = 5; // Left side of screen
    private static final int HUD_Y_OFFSET = 35 ; // Offset from bottom
    private static final int BAR_WIDTH = 110;
    private static final int BAR_HEIGHT = 12;
    // Colors
    private static final int BACKGROUND_COLOR = 0x80000000; // Semi-transparent black
    private static final int BORDER_COLOR = 0xFF333333;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    // Mana colors
    private static final int MANA_BAR_BG_COLOR = 0xFF1a1a3d;
    // Rage colors
    private static final int RAGE_BAR_BG_COLOR = 0xFF3d1a1a;
    // Animation constants
    private static final float RESOURCE_GLOW_DURATION = 1.0f; // 1 second glow
    private static final float SLIDE_DURATION = 0.5f; // 0.5 second slide
    private static final float AUTO_HIDE_DELAY = 15.0f; // Hide after 10 seconds

    // Animation state
    private float animatedResourceProgress = 0.0f;
    private float resourceGainGlowTimer = 0.0f;
    private float slideOffset = 1.0f; // 0 = fully visible, 1 = fully hidden
    private float hideTimer = 0.0f;
    private boolean shouldBeVisible = false;

    // Previous values for change detection
    private float lastResourceAmount = -1;
    private ResourceType lastResourceType = null;

    /**
     * Render the resource bar HUD
     */
    public void render(DrawContext drawContext, MinecraftClient client, float deltaTime) {
        if (client.player == null) return;

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.getNullable(client.player);
        if (classComponent == null) return;

        PlayerClassManager manager = classComponent.getClassManager();

        // Get resource data
        float currentResource = manager.getCurrentResource();
        float maxResource = manager.getMaxResource();
        ResourceType resourceType = manager.getResourceType();

        // Check for resource changes to trigger visibility and glow
        if (lastResourceAmount != currentResource || lastResourceType != resourceType) {
            if (lastResourceAmount >= 0 && currentResource > lastResourceAmount) {
                // Resource gained - trigger glow and show
                resourceGainGlowTimer = RESOURCE_GLOW_DURATION;
                shouldBeVisible = true;
                hideTimer = AUTO_HIDE_DELAY;
            } else if (currentResource < maxResource) {
                shouldBeVisible = true;
                hideTimer = AUTO_HIDE_DELAY;
            }

            lastResourceAmount = currentResource;
            lastResourceType = resourceType;
        }

        // Don't render if fully hidden
        if (slideOffset >= 1.0f && !shouldBeVisible) return;

        // Update animations
        updateAnimations(deltaTime,manager);

        // Calculate HUD position with slide animation
        int screenHeight = client.getWindow().getScaledHeight();
        int hudY = screenHeight - HUD_Y_OFFSET;
        int slideDistance = (int) (slideOffset * (BAR_HEIGHT + 30)); // Slide down off screen
        int actualHudY = hudY + slideDistance;

        // Render the resource bar
        renderResourceBar(drawContext, client, manager, HUD_X, actualHudY, resourceType);
    }

    /**
     * Update all animations
     */
    private void updateAnimations(float deltaTime, PlayerClassManager manager) {
        float targetProgress = manager.getResourcePercentage();

        // Smooth progress animation
        float lerpSpeed = 0.08f;
        animatedResourceProgress = MathHelper.lerp(lerpSpeed, animatedResourceProgress, targetProgress);

        // Resource gain glow timer countdown
        if (resourceGainGlowTimer > 0) {
            resourceGainGlowTimer -= deltaTime;
            if (resourceGainGlowTimer < 0) resourceGainGlowTimer = 0;
        }

        // Handle auto-hide functionality
        if (hideTimer > 0) {
            hideTimer -= deltaTime;
            if (hideTimer <= 0) shouldBeVisible = false;
        }

        // Calculate target slide offset
        float targetSlideOffset = shouldBeVisible ? 0.0f : 1.0f;

        // Smooth slide animation
        float slideSpeed = 1.0f / SLIDE_DURATION;
        if (slideOffset != targetSlideOffset) {
            float direction = targetSlideOffset > slideOffset ? 1.0f : -1.0f;
            slideOffset += direction * slideSpeed * deltaTime;

            // Clamp to target
            if (direction > 0 && slideOffset > targetSlideOffset) slideOffset = targetSlideOffset;
            else if (direction < 0 && slideOffset < targetSlideOffset) slideOffset = targetSlideOffset;
        }
    }

    private void renderResourceBar(DrawContext drawContext, MinecraftClient client, PlayerClassManager manager,
                                   int hudX, int hudY, ResourceType resourceType) {
        TextRenderer textRenderer = client.textRenderer;

        // Get resource values
        float currentResource = manager.getCurrentResource();
        float maxResource = manager.getMaxResource();

        // Calculate dimensions
        int hudWidth = BAR_WIDTH + 6;
        int hudHeight = BAR_HEIGHT + textRenderer.fontHeight + 8;

        // Dynamic colors from ResourceType
        int barColor = resourceType.getColor();
        int glowColor = resourceType.getColorglow();

        // For background, keep your original color but based on resource type (optional)
        int barBgColor = (resourceType == ResourceType.MANA) ? MANA_BAR_BG_COLOR : RAGE_BAR_BG_COLOR;

        // Background panel with border
        drawContext.fill(hudX - 3, hudY - 3, hudX + hudWidth, hudY + hudHeight, BACKGROUND_COLOR);
        drawContext.drawBorder(hudX - 3, hudY - 3, hudWidth + 3, hudHeight + 3, BACKGROUND_COLOR);

        // Resource type label color dynamically from enum color
        String resourceLabel = resourceType.getDisplayName();
        drawContext.drawTextWithShadow(textRenderer, resourceLabel, hudX, hudY, barColor);

        int barY = hudY + textRenderer.fontHeight + 2;

        // Resource bar background
        drawContext.fill(hudX, barY, hudX + BAR_WIDTH, barY + BAR_HEIGHT, barBgColor);

        // Resource bar progress with glow effect
        int progressWidth = (int) (BAR_WIDTH * animatedResourceProgress);

        if (progressWidth > 0) {
            if (resourceGainGlowTimer > 0) {
                int glowAlpha = LevelHudRenderer.calculateGlowStrength(resourceGainGlowTimer, RESOURCE_GLOW_DURATION);
                int resourceGlowColor = (glowAlpha << 24) | (glowColor & 0x00FFFFFF);
                drawContext.fill(hudX - 2, barY - 2, hudX + progressWidth + 2, barY + BAR_HEIGHT + 2, resourceGlowColor);
                drawContext.fill(hudX - 1, barY - 1, hudX + progressWidth + 1, barY + BAR_HEIGHT + 1, resourceGlowColor);
            }
            drawContext.fill(hudX, barY, hudX + progressWidth, barY + BAR_HEIGHT, barColor);
        }

        // Resource bar border
        drawContext.drawBorder(hudX, barY, BAR_WIDTH, BAR_HEIGHT, BORDER_COLOR);

        // Resource text (current/max)
        String resourceText = NumberUtil.formatNumber((long) currentResource) + "/" + NumberUtil.formatNumber((long) maxResource);
        int textX = hudX + BAR_WIDTH - textRenderer.getWidth(resourceText);
        int textY = barY + 2;
        drawContext.drawTextWithShadow(textRenderer, resourceText, textX, textY, TEXT_COLOR);

        // Resource percentage (left side of bar)
        String percentText = String.format("%.0f%%", animatedResourceProgress * 100);
        drawContext.drawTextWithShadow(textRenderer, percentText, hudX + 2, textY, 0xFFAAAAAA);
    }

    /**
     * Force show the resource bar (for when skills are used, etc.)
     */
    public void forceShow() {
        shouldBeVisible = true;
        hideTimer = AUTO_HIDE_DELAY;
    }

    /**
     * Force hide the resource bar
     */
    public void forceHide() {
        shouldBeVisible = false;
        hideTimer = 0;
    }
}