package com.sypztep.mamy.client.event.hud;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.client.screen.hud.ResourceBarHud;
import com.sypztep.mamy.client.util.BlendMode;
import com.sypztep.mamy.client.util.DrawContextUtils;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.gearscore.PlayerGearscore;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import com.sypztep.mamy.common.util.NumberUtil;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public final class LevelHudRenderer implements HudRenderCallback {
    // Layout settings - base dimensions (not scaled)
    // UPDATED: Using consistent X position with ResourceBarHud for pixel perfect alignment
    private static final int BASE_HUD_X = 5;
    private static final int BASE_HUD_Y = 2;
    private static final int BASE_HUD_WIDTH = 260;
    private static final int BASE_PLAYER_HEAD_SIZE = 32;
    private static final int BASE_MAIN_BAR_WIDTH = 245;
    private static final int BASE_BAR_HEIGHT = 6;
    private static final int BASE_PADDING = 8;
    private static final int BASE_LINE_HEIGHT = 1;
    private static final int BASE_SPACING = 4;
    private static final int BASE_HEAD_TEXT_SPACING = 6; // Space between head and player name
    private static final int BASE_ICON_SIZE = 12; // Size for texture icons
    private static final int BASE_ICON_TEXT_SPACING = 4; // Space between icon and text

    // Resource bar offset animation constants
    private static final float RESOURCE_BAR_OFFSET_DURATION = 0.4f; // Duration for Y offset animation
    private static final int BASE_VERTICAL_SPACING = -4; // Base spacing between components (before scaling)

    // GUI Texture identifiers (modern 1.21.1 approach)
    private static final Identifier BALANCE_ICON = Mamy.id("hud/level/balance");
    private static final Identifier XP_ICON = Mamy.id("hud/level/xp");
    private static final Identifier CLASS_ICON = Mamy.id("hud/level/class");
    private static final Identifier STATS_ICON = Mamy.id("hud/level/stats");

    private static final Identifier BACKGROUND_TEXTURE = Mamy.id("textures/gui/hud/level/card.png");
    private static final Identifier SKY_NIGHT_TEXTURE = Mamy.id("textures/vfx/water_caustic.png");

    // Animation settings
    private static final float XP_GLOW_DURATION = 3.0f;
    private static final float SLIDE_DURATION = 0.6f;
    private static final float AUTO_HIDE_DELAY = ModConfig.autohideDuration;
    private static final float PORTAL_ANIMATION_SPEED = 0.001f;

    // Shake animation settings
    private static final float SHAKE_DURATION = 0.8f;
    private static final float SHAKE_INTENSITY = 4.0f;

    // Portal effect settings
    private static float portalTime = 0.0f;

    // Static color scheme
    private static final int BACKGROUND_DARK = 0xFF000000;
    private static final int BACKGROUND_LIGHT = 0xE01B1B2A;
    private static final int BORDER_COLOR = 0xFF2D3748;

    // Gradient line colors (golden)
    private static final int GOLDEN_CENTER = 0xFFFFD700;
    private static final int GOLDEN_EDGE = 0x00FFD700;

    // Bar colors
    private static final int BASE_XP_BAR_START = 0xFF00E676;
    private static final int BASE_XP_BAR_END = 0xFF00C853;
    private static final int BAR_BG_COLOR = 0xFF1A1A2E;
    private static final int BAR_BORDER_COLOR = 0xFF4A5568;

    // Text colors
    private static final int HEADER_TEXT_COLOR = 0xFFF7FAFC;
    private static final int CONTENT_TEXT_COLOR = 0xFFE2E8F0;
    private static final int LEVEL_COLOR = 0xFFFFD700;
    private static final int STATS_COLOR = 0xFFFF9800;
    private static final int PERCENTAGE_COLOR = 0xFFAAAAAA;

    // Animation state
    private static float animatedXpProgress = 0.0f;
    private static float animatedClassProgress = 0.0f;
    private static long lastXp = 0;
    private static long lastClassXp = 0;
    private static int lastLevel = 1;
    private static int lastClassLevel = 1;

    // Separate glow timers for base and class XP
    private static float baseXpGlowTimer = 0.0f;
    private static float classXpGlowTimer = 0.0f;

    // Shake animation state
    private static float shakeTimer = 0.0f;
    private static boolean isLevelUpShaking = false;
    private static final Random shakeRandom = new Random();

    // Slide animation state
    private static float slideOffset = 0.0f;
    private static float hideTimer = 0.0f;
    private static boolean shouldBeVisible = false;

    // Resource bar offset animation state
    private static float resourceBarOffsetProgress = 0.0f;
    private static boolean lastResourceBarVisible = false;

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.getDebugHud().shouldShowDebugHud()) return;

        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.get(client.player);
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);

        if (!LivingEntityUtil.isPlayer(client.player)) return;

        // Update portal animation
        portalTime += PORTAL_ANIMATION_SPEED;
        if (portalTime > Math.PI * 20) portalTime = 0.0f; // Reset after full cycle

        // Get current values
        int level = levelComponent.getLevel();
        long currentXp = levelComponent.getExperience();
        long xpToNext = levelComponent.getExperienceToNextLevel();
        boolean isMaxLevel = levelComponent.getLevelSystem().isMaxLevel();

        int classLevel = classComponent.getClassManager().getClassLevel();
        long currentClassXp = classComponent.getClassManager().getClassExperience();
        long classXpToNext = classComponent.getClassManager().getClassLevelSystem().getExperienceToNextLevel();
        boolean isMaxClassLevel = classComponent.getClassManager().getClassLevelSystem().isMaxLevel();

        // Check if Alt key is being held down
        boolean forceShow = InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_ALT);

        // Detect changes and trigger appropriate effects
        boolean baseXpGained = currentXp > lastXp;
        boolean baseLevelUp = level > lastLevel;
        boolean classXpGained = currentClassXp > lastClassXp;
        boolean classLevelUp = classLevel > lastClassLevel;

        // Trigger base XP glow only when base XP is gained
        if (baseXpGained || baseLevelUp) {
            baseXpGlowTimer = XP_GLOW_DURATION;
            shouldBeVisible = true;
            hideTimer = AUTO_HIDE_DELAY;
        }

        // Trigger class XP glow only when class XP is gained
        if (classXpGained || classLevelUp) {
            classXpGlowTimer = XP_GLOW_DURATION;
            shouldBeVisible = true;
            hideTimer = AUTO_HIDE_DELAY;
        }

        // Trigger shake animation on level ups
        if (baseLevelUp || classLevelUp) {
            shakeTimer = SHAKE_DURATION;
            isLevelUpShaking = true;
        }

        // Handle Alt key override visibility
        if (forceShow) {
            shouldBeVisible = true;
            hideTimer = 0; // Stop auto-hide timer while Alt is held
        } else if (hideTimer <= 0) {
            shouldBeVisible = false;
        }

        // Update animations
        float deltaTime = tickCounter.getTickDelta(false) / 20.0f;
        updateAnimations(currentXp, xpToNext, isMaxLevel, currentClassXp, classXpToNext, isMaxClassLevel, deltaTime);

        // Calculate slide position
        int currentHudX = calculateHudX();

        // Calculate Y offset based on resource bar visibility with proper scaling consideration
        int currentHudY = calculateHudY(deltaTime);

        // Calculate shake offset
        int shakeOffsetX = 0;
        int shakeOffsetY = 0;
        if (isLevelUpShaking && shakeTimer > 0) {
            float shakeStrength = (shakeTimer / SHAKE_DURATION) * SHAKE_INTENSITY;
            shakeOffsetX = (int) ((shakeRandom.nextFloat() - 0.5f) * 2.0f * shakeStrength);
            shakeOffsetY = (int) ((shakeRandom.nextFloat() - 0.5f) * 2.0f * shakeStrength);
        }

        // Only render if HUD is at least partially visible
        if (slideOffset < 1.0f) {
            renderLevelHud(drawContext, client, levelComponent, classComponent,
                    currentHudX + shakeOffsetX, currentHudY + shakeOffsetY);
        }

        // Update last values
        lastXp = currentXp;
        lastClassXp = currentClassXp;
        lastLevel = level;
        lastClassLevel = classLevel;
    }

    private void updateAnimations(long currentXp, long xpToNext, boolean isMaxLevel, long currentClassXp, long classXpToNext, boolean isMaxClassLevel, float deltaTime) {
        // Smooth progress animations
        float targetProgress = isMaxLevel ? 1.0f : (float) ((double) currentXp / (double) xpToNext);
        float lerpSpeed = 0.05f;
        animatedXpProgress = MathHelper.lerp(lerpSpeed, animatedXpProgress, targetProgress);

        float targetClassProgress = isMaxClassLevel ? 1.0f : (float) ((double) currentClassXp / (double) classXpToNext);
        animatedClassProgress = MathHelper.lerp(lerpSpeed, animatedClassProgress, targetClassProgress);

        // Base XP glow timer
        if (baseXpGlowTimer > 0) {
            baseXpGlowTimer -= deltaTime;
            if (baseXpGlowTimer < 0) baseXpGlowTimer = 0;
        }

        // Class XP glow timer
        if (classXpGlowTimer > 0) {
            classXpGlowTimer -= deltaTime;
            if (classXpGlowTimer < 0) classXpGlowTimer = 0;
        }

        // Shake animation timer
        if (shakeTimer > 0) {
            shakeTimer -= deltaTime;
            if (shakeTimer <= 0) {
                shakeTimer = 0;
                isLevelUpShaking = false;
            }
        }

        // Auto-hide logic
        if (shouldBeVisible && hideTimer > 0) {
            hideTimer -= deltaTime;
            if (hideTimer <= 0) {
                shouldBeVisible = false;
                hideTimer = 0;
            }
        }

        // Slide animation
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
     * Calculate HUD Y position with resource bar offset animation
     * UPDATED: Proper scaling consideration for pixel perfect alignment
     */
    private int calculateHudY(float deltaTime) {
        // Check if resource bar visibility has changed
        boolean currentResourceBarVisible = ResourceBarHud.isVisible();

        if (currentResourceBarVisible != lastResourceBarVisible) {
            lastResourceBarVisible = currentResourceBarVisible;
        }

        // Animate the offset progress
        float targetOffsetProgress = currentResourceBarVisible ? 1.0f : 0.0f;
        float offsetSpeed = 1.0f / RESOURCE_BAR_OFFSET_DURATION;

        if (resourceBarOffsetProgress != targetOffsetProgress) {
            float direction = targetOffsetProgress > resourceBarOffsetProgress ? 1.0f : -1.0f;
            resourceBarOffsetProgress += direction * offsetSpeed * deltaTime;

            if (direction > 0 && resourceBarOffsetProgress > targetOffsetProgress) {
                resourceBarOffsetProgress = targetOffsetProgress;
            } else if (direction < 0 && resourceBarOffsetProgress < targetOffsetProgress) {
                resourceBarOffsetProgress = targetOffsetProgress;
            }
        }

        // Calculate the offset with proper scaling consideration
        float easedProgress = DrawContextUtils.enhancedEaseInOut(resourceBarOffsetProgress);

        // FIXED: Calculate proper offset considering ResourceBarHud's scaled height plus consistent spacing
        int resourceBarOffset = 0;
        if (currentResourceBarVisible) {
            // Get the actual scaled height of the resource bar
            int scaledResourceBarHeight = ResourceBarHud.getResourceBarHeight();
            // Add consistent vertical spacing (scaled to maintain pixel perfect alignment)
            int scaledSpacing = (int)(BASE_VERTICAL_SPACING * ModConfig.resourcebarscale);
            resourceBarOffset = (int)(easedProgress * (scaledResourceBarHeight + scaledSpacing));
        }

        return BASE_HUD_Y + resourceBarOffset;
    }

    private void renderLevelHud(DrawContext drawContext, MinecraftClient client, LivingLevelComponent levelData, PlayerClassComponent classData, int hudX, int hudY) {
        float scale = ModConfig.lvbarscale;

        DrawContextUtils.withScaleAt(drawContext, scale, hudX, hudY, () -> {
            renderHudContent(drawContext, client, levelData, classData, hudY);
        });
    }

    private void renderHudContent(DrawContext drawContext, MinecraftClient client, LivingLevelComponent levelData, PlayerClassComponent classData, int baseY) {
        TextRenderer textRenderer = client.textRenderer;
        int currentY = baseY;

        // Calculate total height
        int totalHeight = calculateTotalHeight(textRenderer);

        // === MAIN BACKGROUND ===
        DrawContextUtils.renderVerticalGradientWithBlendedImage(
                drawContext,
                BASE_HUD_X,
                currentY,
                BASE_HUD_WIDTH,
                totalHeight,
                BACKGROUND_LIGHT,
                BACKGROUND_DARK,
                BACKGROUND_TEXTURE,
                0.15f,
                BlendMode.MULTIPLY_SCREEN_HYBRID,
                DrawContextUtils.ImageScaleMode.FILL,
                512, 256
        );
        drawContext.drawBorder(BASE_HUD_X, currentY, BASE_HUD_WIDTH, totalHeight, BORDER_COLOR);

        currentY += BASE_PADDING;

        // === HEADER LINE: [HEAD] Player Name + Level ===
        currentY = renderHeaderLine(drawContext, textRenderer, client, levelData, currentY);

        // === GOLDEN GRADIENT LINE 1 ===
        currentY = renderGoldenLine(drawContext, currentY);

        // === BASE XP LINE ===
        currentY = renderBaseXpLine(drawContext, textRenderer, levelData, currentY);

        // === BASE XP BAR WITH SHADER PORTAL EFFECT ===
        currentY = renderShaderProgressBar(drawContext, BASE_HUD_X + BASE_PADDING, currentY, BASE_MAIN_BAR_WIDTH, BASE_BAR_HEIGHT,
                animatedXpProgress, levelData.getLevelSystem().isMaxLevel(),
                BASE_XP_BAR_START, BASE_XP_BAR_END, baseXpGlowTimer, true); // Use baseXpGlowTimer

        // === CLASS XP LINE ===
        currentY = renderClassXpLine(drawContext, textRenderer, classData, currentY);

        // === CLASS XP BAR WITH SHADER PORTAL EFFECT ===
        int classColorValue = (0xFF << 24) | classData.getClassManager().getCurrentClass().getColor().getColorValue();
        currentY = renderShaderProgressBar(drawContext, BASE_HUD_X + BASE_PADDING, currentY, BASE_MAIN_BAR_WIDTH, BASE_BAR_HEIGHT,
                animatedClassProgress, classData.getClassManager().getClassLevelSystem().isMaxLevel(),
                classColorValue, DrawContextUtils.darkenColor(classColorValue, 0.8f), classXpGlowTimer, false); // Use classXpGlowTimer

        // === GOLDEN GRADIENT LINE 2 ===
        currentY = renderGoldenLine(drawContext, currentY);

        // === INFO LINE: [Level-SubLevel] Class + Stats ===
        renderInfoLine(drawContext, textRenderer, levelData, classData, currentY);
    }
    private String formatGearscore(int gearscore) {
        if (gearscore >= 1000) {
            return String.format("%,d", gearscore); // Adds commas: 1,234
        }
        return String.valueOf(gearscore);
    }
    private static final int GEARSCORE_COLOR_LOW = 0xFF808080;      // Gray for low gearscore
    private static final int GEARSCORE_COLOR_NORMAL = 0xFFFFFFFF;   // White for normal
    private static final int GEARSCORE_COLOR_HIGH = 0xFF00FF00;     // Green for high
    private static final int GEARSCORE_COLOR_EPIC = 0xFF9932CC;     // Purple for epic
    private static final int GEARSCORE_COLOR_LEGENDARY = 0xFFFFD700; // Gold for legendary

    /**
     * Get color based on gearscore tier
     */
    private int getGearscoreColor(int gearscore) {
        if (gearscore < 1000) return GEARSCORE_COLOR_LOW;        // 0-999 (early game)
        if (gearscore < 3000) return GEARSCORE_COLOR_NORMAL;     // 1000-2999 (mid game)
        if (gearscore < 6000) return GEARSCORE_COLOR_HIGH;       // 3000-5999 (late game)
        if (gearscore < 9000) return GEARSCORE_COLOR_EPIC;       // 6000-8999 (endgame)
        return GEARSCORE_COLOR_LEGENDARY;                        // 9000+ (maxed out)
    }
    private int renderHeaderLine(DrawContext drawContext, TextRenderer textRenderer, MinecraftClient client, LivingLevelComponent levelData, int y) {
        // Render player head
        if (client.player != null) {
            PlayerSkinDrawer.draw(drawContext, client.player.getSkinTextures(), BASE_HUD_X + BASE_PADDING, y, BASE_PLAYER_HEAD_SIZE);
            drawContext.drawBorder(BASE_HUD_X + BASE_PADDING, y, BASE_PLAYER_HEAD_SIZE, BASE_PLAYER_HEAD_SIZE, BORDER_COLOR);
        }

        // Player name (left aligned after head with spacing)
        Optional<String> playerName = Optional.ofNullable(client.player).map(clientPlayerEntity -> clientPlayerEntity.getName().getString());
        int nameX = BASE_HUD_X + BASE_PADDING + BASE_PLAYER_HEAD_SIZE + BASE_HEAD_TEXT_SPACING;
        int nameY = y + (BASE_PLAYER_HEAD_SIZE - textRenderer.fontHeight) / 2;
        drawContext.drawTextWithShadow(textRenderer, playerName.orElse("Unknown"), nameX, nameY, HEADER_TEXT_COLOR);

        // Calculate gearscore for display
        int gearscore = client.player != null ? PlayerGearscore.calculateGearscore(client.player) : 0;

        // Level and Gearscore (right aligned) - Stack them vertically
        int level = levelData.getLevel();
        boolean isMaxLevel = levelData.getLevelSystem().isMaxLevel();
        String levelText = isMaxLevel ? "MAX" : "Base Lv." + level;
        int levelColor = isMaxLevel ? 0xFFE91E63 : LEVEL_COLOR;

        // Enhanced level color during shake
        if (isLevelUpShaking && shakeTimer > 0) {
            float intensity = shakeTimer / SHAKE_DURATION;
            levelColor = ColorHelper.Argb.getArgb(255,
                    Math.min(255, (int)(255 * (1.0f + intensity))),
                    Math.min(255, (int)(215 * (1.0f + intensity * 0.5f))),
                    0);
        }

        // Gearscore text and formatting
        String gearscoreText = "GS: " + formatGearscore(gearscore);
        int gearscoreColor = getGearscoreColor(gearscore);

        // Calculate right-aligned positions
        int levelTextWidth = textRenderer.getWidth(levelText);
        int gearscoreTextWidth = textRenderer.getWidth(gearscoreText);
        int maxTextWidth = Math.max(levelTextWidth, gearscoreTextWidth);

        int rightAlignX = BASE_HUD_X + BASE_HUD_WIDTH - maxTextWidth - BASE_PADDING;

        // Draw level text (top line)
        int levelX = rightAlignX + (maxTextWidth - levelTextWidth); // Right align within the space
        drawContext.drawTextWithShadow(textRenderer, levelText, levelX, nameY, levelColor);

        // Draw gearscore text (bottom line)
        int gearscoreX = rightAlignX + (maxTextWidth - gearscoreTextWidth); // Right align within the space
        int gearscoreY = nameY + textRenderer.fontHeight + 2; // 2px spacing between lines
        drawContext.drawTextWithShadow(textRenderer, gearscoreText, gearscoreX, gearscoreY, gearscoreColor);

        return y + BASE_PLAYER_HEAD_SIZE + BASE_SPACING;
    }

    private int renderGoldenLine(DrawContext drawContext, int y) {
        int lineX = BASE_HUD_X + BASE_PADDING;
        int lineWidth = BASE_HUD_WIDTH - (BASE_PADDING * 2);
        DrawContextUtils.renderHorizontalLineWithCenterGradient(drawContext, lineX, y, lineWidth, BASE_LINE_HEIGHT, 0, GOLDEN_CENTER, GOLDEN_EDGE);
        return y + BASE_LINE_HEIGHT + BASE_SPACING;
    }

    private int renderBaseXpLine(DrawContext drawContext, TextRenderer textRenderer, LivingLevelComponent levelData, int y) {
        long currentXp = levelData.getExperience();
        long xpToNext = levelData.getExperienceToNextLevel();
        boolean isMaxLevel = levelData.getLevelSystem().isMaxLevel();

        int contentX = BASE_HUD_X + BASE_PADDING;

        // Render XP icon using modern GUI texture approach
        renderGuiIcon(drawContext, XP_ICON, contentX, y + (textRenderer.fontHeight - BASE_ICON_SIZE) / 2);
        int textX = contentX + BASE_ICON_SIZE + BASE_ICON_TEXT_SPACING;

        // Base: text
        String baseText = "Base:";
        drawContext.drawTextWithShadow(textRenderer, baseText, textX, y, CONTENT_TEXT_COLOR);

        // XP numbers
        String baseXpText = isMaxLevel ? "MAX" : NumberUtil.formatNumber(currentXp) + "/" + NumberUtil.formatNumber(xpToNext);
        int baseXpX = textX + textRenderer.getWidth(baseText) + 10;
        drawContext.drawTextWithShadow(textRenderer, baseXpText, baseXpX, y, CONTENT_TEXT_COLOR);

        // Percentage (right aligned)
        String basePercentText = isMaxLevel ? "100%" : String.format("%.1f%%", animatedXpProgress * 100);
        int basePercentX = BASE_HUD_X + BASE_HUD_WIDTH - textRenderer.getWidth(basePercentText) - BASE_PADDING;
        drawContext.drawTextWithShadow(textRenderer, basePercentText, basePercentX, y, PERCENTAGE_COLOR);

        return y + textRenderer.fontHeight + 2;
    }

    private int renderClassXpLine(DrawContext drawContext, TextRenderer textRenderer, PlayerClassComponent classData, int y) {
        long currentClassXp = classData.getClassManager().getClassExperience();
        long classXpToNext = classData.getClassManager().getClassLevelSystem().getExperienceToNextLevel();
        boolean isMaxClassLevel = classData.getClassManager().getClassLevelSystem().isMaxLevel();
        int classColorValue = (0xFF << 24) | classData.getClassManager().getCurrentClass().getColor().getColorValue();

        int contentX = BASE_HUD_X + BASE_PADDING;

        // Render class icon using modern GUI texture approach
        renderGuiIcon(drawContext, CLASS_ICON, contentX, y + (textRenderer.fontHeight - BASE_ICON_SIZE) / 2);
        int textX = contentX + BASE_ICON_SIZE + BASE_ICON_TEXT_SPACING;

        // Job: text
        String jobText = "Job:";
        drawContext.drawTextWithShadow(textRenderer, jobText, textX, y, classColorValue);

        // Class XP numbers
        String classXpText = isMaxClassLevel ? "MAX" : NumberUtil.formatNumber(currentClassXp) + "/" + NumberUtil.formatNumber(classXpToNext);
        int classXpX = textX + textRenderer.getWidth(jobText) + 10;
        drawContext.drawTextWithShadow(textRenderer, classXpText, classXpX, y, CONTENT_TEXT_COLOR);

        // Percentage (right aligned)
        String classPercentText = isMaxClassLevel ? "100%" : String.format("%.1f%%", animatedClassProgress * 100);
        int classPercentX = BASE_HUD_X + BASE_HUD_WIDTH - textRenderer.getWidth(classPercentText) - BASE_PADDING;
        drawContext.drawTextWithShadow(textRenderer, classPercentText, classPercentX, y, PERCENTAGE_COLOR);

        return y + textRenderer.fontHeight + 2;
    }

    private void renderInfoLine(DrawContext drawContext, TextRenderer textRenderer, LivingLevelComponent levelData, PlayerClassComponent classData, int y) {
        int contentX = BASE_HUD_X + BASE_PADDING;
        int classColorValue = classData.getClassManager().getCurrentClass().getColor().getColorValue();

        Text classInfo = classData.getClassManager().getClassInfo();

        // Stats and currencies
        int availableStatPoints = levelData.getAvailableStatPoints();
        String statsText = "PS: " + availableStatPoints;
        String cpText = "CP: " + classData.getClassManager().getClassLevelSystem().getStatPoints();

        // Calculate positions
        int classInfoWidth = textRenderer.getWidth(classInfo);
        int statsWidth = textRenderer.getWidth(statsText);
        int cpWidth = textRenderer.getWidth(cpText);

        int contentWidth = BASE_HUD_WIDTH - (BASE_PADDING * 2);
        int totalWidth = classInfoWidth + statsWidth + cpWidth;
        int spacing = Math.max(8, (contentWidth - totalWidth) / 2); // Divide by 3 for 3 gaps

        // Render texts with icons
        drawContext.drawTextWithShadow(textRenderer, classInfo, contentX, y, classColorValue);

        // Stats icon and text
        int statsX = contentX + classInfoWidth + spacing;
        renderGuiIcon(drawContext, STATS_ICON, statsX - BASE_ICON_SIZE - BASE_ICON_TEXT_SPACING / 2, y + (textRenderer.fontHeight - BASE_ICON_SIZE) / 2);
        drawContext.drawTextWithShadow(textRenderer, statsText, statsX, y, STATS_COLOR);

        // CP icon and text
        int cpX = statsX + statsWidth + spacing;
        renderGuiIcon(drawContext, CLASS_ICON, cpX - BASE_ICON_SIZE - BASE_ICON_TEXT_SPACING / 2, y + (textRenderer.fontHeight - BASE_ICON_SIZE) / 2); // Changed to CLASS_ICON for CP
        drawContext.drawTextWithShadow(textRenderer, cpText, cpX, y, CONTENT_TEXT_COLOR);
    }

    private void renderGuiIcon(DrawContext drawContext, Identifier iconTexture, int x, int y) {
        drawContext.drawGuiTexture(iconTexture, x, y, BASE_ICON_SIZE, BASE_ICON_SIZE);
    }

    private int renderShaderProgressBar(DrawContext drawContext, int x, int y, int width, int height, float progress, boolean isMaxLevel, int colorStart, int colorEnd, float glowTimer, boolean isBaseXp) {
        drawContext.fill(x, y, x + width, y + height, BAR_BG_COLOR);

        int progressWidth = isMaxLevel ? width : (int) (width * progress);

        if (progressWidth > 0) {
            // FIRST: Render glow effect BEFORE portal effect (only if this specific bar has glow)
            if (glowTimer > 0) {
                int glowAlpha = calculateGlowStrength(glowTimer, XP_GLOW_DURATION);

                int outerGlowColor = (glowAlpha / 6) << 24 | 0x00FFFFFF;

                // Outer glow
                drawContext.fill(x - 3, y - 3, x + progressWidth + 3, y + height + 3, outerGlowColor);
                outerGlowColor = (glowAlpha / 4) << 24 | 0x00FFFFFF;
                drawContext.fill(x - 2, y - 2, x + progressWidth + 2, y + height + 2, outerGlowColor);

                // Inner glow
                int innerGlowColor = (glowAlpha / 2) << 24 | 0x00FFFFFF;
                drawContext.fill(x - 1, y - 1, x + progressWidth + 1, y + height + 1, innerGlowColor);
            }

            // SECOND: Base gradient layer
            DrawContextUtils.renderVerticalGradient(drawContext, x, y, progressWidth, height, colorStart, colorEnd);

            // THIRD: Portal effect overlay
            float portalAlpha = 0.4f;
            if (glowTimer > 0) {
                portalAlpha = 0.4f + (glowTimer / XP_GLOW_DURATION) * 0.3f; // Increase intensity during glow
            }

            DrawContextUtils.renderMagicalPortalEffect(drawContext, x, y, progressWidth, height, SKY_NIGHT_TEXTURE, portalTime, portalAlpha);
        }

        // Enhanced border with depth
        drawContext.drawBorder(x, y, width, height, BAR_BORDER_COLOR);

        // Inner highlight for depth
        int highlightColor = 0x40FFFFFF;
        drawContext.fill(x + 1, y + 1, x + width - 1, y + 2, highlightColor);

        return y + height + BASE_SPACING;
    }

    public static int calculateGlowStrength(float glowTimer, float duration) {
        float glowStrength = glowTimer / duration;
        float time = (duration - glowTimer) * 4.0f;
        float pulse = (float) (0.6f + 0.4f * Math.sin(time * Math.PI));
        float finalGlow = glowStrength * (0.8f + 0.2f * pulse);

        return (int) (finalGlow * 160);
    }

    private int calculateTotalHeight(TextRenderer textRenderer) {
        return BASE_PADDING * 2 + // Top and bottom padding
                BASE_PLAYER_HEAD_SIZE + BASE_SPACING + // Header section
                BASE_LINE_HEIGHT + BASE_SPACING + // Golden line 1
                textRenderer.fontHeight + 2 + BASE_BAR_HEIGHT + BASE_SPACING + // Base XP section
                textRenderer.fontHeight + 2 + BASE_BAR_HEIGHT + BASE_SPACING + // Class XP section
                BASE_LINE_HEIGHT + BASE_SPACING + // Golden line 2
                textRenderer.fontHeight; // Info section
    }

    private int calculateHudX() {
        int hudWidth = BASE_HUD_WIDTH + 24;
        int hiddenX = -hudWidth;
        float easedOffset = DrawContextUtils.enhancedEaseInOut(slideOffset);
        return MathHelper.lerp(easedOffset, BASE_HUD_X, hiddenX);
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new LevelHudRenderer());
    }
}