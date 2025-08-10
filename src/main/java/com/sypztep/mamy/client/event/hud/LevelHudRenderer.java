package com.sypztep.mamy.client.event.hud;

import com.sypztep.mamy.client.util.DrawContextUtils;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import com.sypztep.mamy.common.util.NumberUtil;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;

public final class LevelHudRenderer implements HudRenderCallback {
    // Position settings
    private static final int HUD_X = 10;
    private static final int HUD_Y = 10;
    private static final int BAR_WIDTH = 140;
    private static final int BAR_HEIGHT = 8;
    private static final int CLASS_BAR_OFFSET = 12; // Space between main and class bars

    // Animation settings
    private static final float XP_GLOW_DURATION = 2.0f;
    private static final float SLIDE_DURATION = 0.5f;
    private static final float AUTO_HIDE_DELAY = 8.0f;

    // Colors
    private static final int BACKGROUND_COLOR = 0xA0000000;
    private static final int BORDER_COLOR = 0xFF444444;
    private static final int XP_BAR_COLOR = 0xFF00CC00;
    private static final int XP_BAR_BG_COLOR = 0xFF222222;
    private static final int CLASS_BAR_COLOR = 0xFFFF7F50; // Blue for class
    private static final int CLASS_BAR_BG_COLOR = 0xFF222222;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int LEVEL_COLOR = 0xFFFFD700;
    private static final int XP_GAIN_GLOW_COLOR = 0xFFFFFFFF;
    private static final int MAX_LEVEL_COLOR = 0xFFFF6600;

    // Animation state
    private static float animatedXpProgress = 0.0f;
    private static float animatedClassProgress = 0.0f;
    private static long lastXp = 0;
    private static long lastClassXp = 0;
    private static int lastLevel = 1;
    private static int lastClassLevel = 1;
    private static float xpGainGlowTimer = 0.0f;
    private static float classGainGlowTimer = 0.0f;

    // Slide animation state
    private static float slideOffset = 0.0f;
    private static float hideTimer = 0.0f;
    private static boolean shouldBeVisible = false;

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.getDebugHud().shouldShowDebugHud()) return;

        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.get(client.player);
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);

        if (!LivingEntityUtil.isPlayer(client.player)) return;

        // Get current values
        int level = levelComponent.getLevel();
        long currentXp = levelComponent.getExperience();
        long xpToNext = levelComponent.getExperienceToNextLevel();
        boolean isMaxLevel = levelComponent.getLevelSystem().isMaxLevel();

        int classLevel = classComponent.getClassManager().getClassLevel();
        long currentClassXp = classComponent.getClassManager().getClassExperience();
        long classXpToNext = classComponent.getClassManager().getClassLevelSystem().getExperienceToNextLevel();
        boolean isMaxClassLevel = classComponent.getClassManager().getClassLevelSystem().isMaxLevel();

        // Detect XP/Level changes
        if ((currentXp > lastXp || level > lastLevel)) {
            xpGainGlowTimer = XP_GLOW_DURATION;
            shouldBeVisible = true;
            hideTimer = AUTO_HIDE_DELAY;
        }

        // Detect Class XP/Level changes ⭐ เพิ่มใหม่
        if ((currentClassXp > lastClassXp || classLevel > lastClassLevel)) {
            classGainGlowTimer = XP_GLOW_DURATION;
            shouldBeVisible = true;
            hideTimer = AUTO_HIDE_DELAY;
        }

        // Update animations
        float deltaTime = tickCounter.getTickDelta(false) / 20.0f;
        updateAnimations(currentXp, xpToNext, isMaxLevel, currentClassXp, classXpToNext, isMaxClassLevel, deltaTime);

        // Calculate slide position
        int currentHudX = calculateHudX();

        // Only render if HUD is at least partially visible
        if (slideOffset < 1.0f) {
            renderLevelHud(drawContext, client, levelComponent, classComponent, currentHudX);
        }

        // Update last values
        lastXp = currentXp;
        lastClassXp = currentClassXp;
        lastLevel = level;
        lastClassLevel = classLevel;
    }

    // Replace the updateAnimations method in LevelHudRenderer with this fixed version:

    private void updateAnimations(long currentXp, long xpToNext, boolean isMaxLevel,
                                  long currentClassXp, long classXpToNext, boolean isMaxClassLevel, float deltaTime) {
        // Main XP progress animation
        float targetProgress = isMaxLevel ? 1.0f : (float) ((double) currentXp / (double) xpToNext);
        float lerpSpeed = 0.08f;
        animatedXpProgress = MathHelper.lerp(lerpSpeed, animatedXpProgress, targetProgress);

        // Class XP progress animation
        float targetClassProgress = isMaxClassLevel ? 1.0f : (float) ((double) currentClassXp / (double) classXpToNext);
        animatedClassProgress = MathHelper.lerp(lerpSpeed, animatedClassProgress, targetClassProgress);

        // XP gain glow timer countdown
        if (xpGainGlowTimer > 0) {
            xpGainGlowTimer -= deltaTime;
            if (xpGainGlowTimer < 0) xpGainGlowTimer = 0;
        }

        // Class gain glow timer countdown
        if (classGainGlowTimer > 0) {
            classGainGlowTimer -= deltaTime;
            if (classGainGlowTimer < 0) classGainGlowTimer = 0;
        }

        // Handle auto-hide functionality - THIS IS THE FIX
        if (shouldBeVisible && hideTimer > 0) {
            hideTimer -= deltaTime;
            if (hideTimer <= 0) {
                shouldBeVisible = false; // Properly set to false when timer expires
                hideTimer = 0; // Reset timer
            }
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

    private void renderLevelHud(DrawContext drawContext, MinecraftClient client, LivingLevelComponent levelData,
                                PlayerClassComponent classData, int hudX) {
        TextRenderer textRenderer = client.textRenderer;

        // Get values
        Optional<String> playerName = Optional.ofNullable(client.player).map(clientPlayerEntity -> clientPlayerEntity.getName().getString());
        int level = levelData.getLevel();
        long currentXp = levelData.getExperience();
        long xpToNext = levelData.getExperienceToNextLevel();
        int availableStatPoints = levelData.getAvailableStatPoints();
        boolean isMaxLevel = levelData.getLevelSystem().isMaxLevel();

        // Calculate text elements
        String levelText = isMaxLevel ? "MAX" : "Lv." + level;
        int playerNameWidth = textRenderer.getWidth(playerName.orElse("NaN"));
        int levelTextWidth = textRenderer.getWidth(levelText);

        // Calculate dynamic HUD dimensions
        int minHudWidth = BAR_WIDTH + 6;
        int requiredWidth = playerNameWidth + levelTextWidth + 20;
        int hudWidth = Math.max(minHudWidth, requiredWidth);
        int hudHeight = 60 + CLASS_BAR_OFFSET; // Height for both bars + text

        int currentY = HUD_Y;

        // Background panel with border
        drawContext.fill(hudX - 3, currentY - 3, hudX + hudWidth, currentY + hudHeight, BACKGROUND_COLOR);
        drawContext.drawBorder(hudX - 3, currentY - 3, hudWidth + 3, hudHeight + 3, BORDER_COLOR);

        // Player name on the left
        drawContext.drawTextWithShadow(textRenderer, playerName.orElse("NaN"), hudX, currentY, TEXT_COLOR);

        // Level on the right
        int levelColor = isMaxLevel ? MAX_LEVEL_COLOR : LEVEL_COLOR;
        int levelX = hudX + hudWidth - levelTextWidth - 6;
        drawContext.drawTextWithShadow(textRenderer, levelText, levelX, currentY, levelColor);

        currentY += textRenderer.fontHeight + 3;

        // === MAIN XP BAR ===
        int mainBarY = currentY;
        drawContext.fill(hudX, mainBarY, hudX + BAR_WIDTH, mainBarY + BAR_HEIGHT, XP_BAR_BG_COLOR);

        // Main XP Bar progress with glow effect
        int progressWidth = isMaxLevel ? BAR_WIDTH : (int) (BAR_WIDTH * animatedXpProgress);

        if (progressWidth > 0) {
            // XP gain glow effect
            if (xpGainGlowTimer > 0) {
                renderGlowEffect(drawContext, hudX, mainBarY, progressWidth, xpGainGlowTimer);
            }

            // Main XP bar
            drawContext.fill(hudX, mainBarY, hudX + progressWidth, mainBarY + BAR_HEIGHT, XP_BAR_COLOR);
        }

        drawContext.drawBorder(hudX, mainBarY, BAR_WIDTH, BAR_HEIGHT, BORDER_COLOR);

        // === CLASS XP BAR ===
        int classBarY = mainBarY + CLASS_BAR_OFFSET;
        drawContext.fill(hudX, classBarY, hudX + BAR_WIDTH, classBarY + BAR_HEIGHT, CLASS_BAR_BG_COLOR);

        // Class XP Bar progress with glow effect ⭐ ใช้ animated progress
        boolean isMaxClassLevel = classData.getClassManager().getClassLevelSystem().isMaxLevel();
        int classProgressWidth = isMaxClassLevel ? BAR_WIDTH : (int) (BAR_WIDTH * animatedClassProgress);

        if (classProgressWidth > 0) {
            // Class gain glow effect ⭐ เพิ่มใหม่
            if (classGainGlowTimer > 0) {
                renderGlowEffect(drawContext, hudX, classBarY, classProgressWidth, classGainGlowTimer);
            }

            // Class XP bar
            drawContext.fill(hudX, classBarY, hudX + classProgressWidth, classBarY + BAR_HEIGHT, CLASS_BAR_COLOR);
        }

        drawContext.drawBorder(hudX, classBarY, BAR_WIDTH, BAR_HEIGHT,BORDER_COLOR);

        currentY = classBarY + BAR_HEIGHT + 4;

        // XP Text and Stats
        String xpText = isMaxLevel ? "MAX LEVEL" :
                NumberUtil.formatNumber(currentXp) + "/" + NumberUtil.formatNumber(xpToNext);
        drawContext.drawTextWithShadow(textRenderer, xpText, hudX, currentY, TEXT_COLOR);

        // Stats Point on the right
        String statText = "Stats: " + availableStatPoints;
        int statX = hudX + BAR_WIDTH - textRenderer.getWidth(statText);
        drawContext.drawTextWithShadow(textRenderer, statText, statX, currentY, LEVEL_COLOR);

        // Class info on the right (next line)
        Text classInfo = classData.getClassManager().getClassInfo();
        int classInfoX = hudX + BAR_WIDTH - textRenderer.getWidth(classInfo);
        drawContext.drawTextWithShadow(textRenderer, classInfo, classInfoX, currentY + 12, 0xFF3366FF);

        // XP Percentage (below on left)
        if (!isMaxLevel) {
            currentY += textRenderer.fontHeight + 1;
            String percentText = String.format("%.1f%%", animatedXpProgress * 100);
            drawContext.drawTextWithShadow(textRenderer, percentText, hudX, currentY, 0xFFAAAAAA);
        }
    }

    /**
     * Render glow effect for progress bars
     */
    private void renderGlowEffect(DrawContext drawContext, int x, int y, int width, float glowTimer) {
        int glowAlpha = glowStrength(glowTimer, XP_GLOW_DURATION);
        int finalGlowColor = (glowAlpha << 24) | (LevelHudRenderer.XP_GAIN_GLOW_COLOR & 0x00FFFFFF);

        // Glow layers
        drawContext.fill(x - 2, y - 2, x + width + 2, y + BAR_HEIGHT + 2, finalGlowColor);
        drawContext.fill(x - 1, y - 1, x + width + 1, y + BAR_HEIGHT + 1, finalGlowColor);
    }

    public static int glowStrength(float glowTimer, float xpGlowDuration) {
        float glowStrength = glowTimer / xpGlowDuration;
        float time = (xpGlowDuration - glowTimer) * 3.0f;
        float pulse = (float) (0.5f + 0.5f * Math.sin(time * Math.PI));
        float finalGlow = glowStrength * (0.7f + 0.3f * pulse);

        return (int) (finalGlow * 130);
    }

    private int calculateHudX() {
        int hudWidth = BAR_WIDTH + 16;
        int hiddenX = -hudWidth;
        float easedOffset = cubicEaseInOut(slideOffset);
        return MathHelper.lerp(easedOffset, HUD_X, hiddenX);
    }

    private float cubicEaseInOut(float t) {
        if (t < 0.5f) {
            return 4.0f * t * t * t;
        } else {
            float p = 2.0f * t - 2.0f;
            return 1.0f + p * p * p / 2.0f;
        }
    }
    public static void register() {
        HudRenderCallback.EVENT.register(new LevelHudRenderer());
    }
}