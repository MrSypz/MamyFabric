package com.sypztep.mamy.client.event.hud;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.util.DrawContextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public final class HealthBarRenderer {
    private static final Identifier HEALTHBAR_TEXTURE = Mamy.id("textures/gui/hud/health/healthbar.png");
    private static final Identifier WATER_CAUSTIC = Mamy.id("textures/vfx/water_caustic.png");

    private static final float XP_GLOW_DURATION = 3.0f;

    private static float animatedHealthProgress = 0.0f;
    private static float healthGlowTimer = 0.0f;
    private static float healthShakeTimer = 0.0f;
    private static float portalTime = 0.0f;
    private static int lastHealth = 0;
    private static final float HEALTH_SHAKE_DURATION = 0.5f;
    private static final float HEALTH_SHAKE_INTENSITY = 2.0f;

    private static float textFadeAlpha = 0.3f;
    private static float targetTextAlpha = 0.3f;
    private static final float TEXT_FADE_SPEED = 0.02f;

    public static void render(DrawContext context, PlayerEntity player, int x, int y, TextRenderer textRenderer) {
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();

        updateAnimations(currentHealth, maxHealth);

        int colorStart= 0xFFFF0000, colorEnd = 0xFF220000, glowColor = 0x00FF0000;

        // Calculate shake offset
        float shakeOffsetX = 0;
        float shakeOffsetY = 0;
        if (healthShakeTimer > 0) {
            shakeOffsetX = (float)((Math.random() - 0.5) * 2 * HEALTH_SHAKE_INTENSITY * (healthShakeTimer / HEALTH_SHAKE_DURATION));
            shakeOffsetY = (float)((Math.random() - 0.5) * 2 * HEALTH_SHAKE_INTENSITY * (healthShakeTimer / HEALTH_SHAKE_DURATION));
        }

        renderShaderProgressBar(context, x + (int)shakeOffsetX, y + (int)shakeOffsetY, 78, 8,
                animatedHealthProgress, currentHealth >= maxHealth, colorStart, colorEnd, glowColor,
                player, textRenderer);
    }

    private static void updateAnimations(float currentHealth, float maxHealth) {
        float targetProgress = maxHealth > 0 ? currentHealth / maxHealth : 0.0f;
        float lerpSpeed = 0.05f;
        animatedHealthProgress = MathHelper.lerp(lerpSpeed, animatedHealthProgress, targetProgress);

        if ((int) currentHealth != lastHealth) {
            // Trigger shake when health decreases (taking damage)
            if ((int) currentHealth < lastHealth) {
                healthShakeTimer = HEALTH_SHAKE_DURATION;
            }

            healthGlowTimer = XP_GLOW_DURATION;
            lastHealth = (int) currentHealth;
            targetTextAlpha = 1.0f;
        }

        if (healthGlowTimer > 0) healthGlowTimer -= 0.016f;
        if (healthShakeTimer > 0) healthShakeTimer -= 0.016f;
        updateTextFadeAnimation();
        portalTime += 0.001f;
    }

    private static void updateTextFadeAnimation() {
        textFadeAlpha = MathHelper.lerp(TEXT_FADE_SPEED, textFadeAlpha, targetTextAlpha);

        if (targetTextAlpha == 1.0f && Math.abs(textFadeAlpha - 1.0f) < 0.01f) {
            if (healthGlowTimer < XP_GLOW_DURATION * 0.7f) {
                targetTextAlpha = 0.3f;
            }
        }

        textFadeAlpha = MathHelper.clamp(textFadeAlpha, 0.0f, 1.0f);
    }

    private static void renderShaderProgressBar(DrawContext drawContext, int x, int y, int width, int height, float progress, boolean isMaxLevel, int colorStart, int colorEnd, int glowColor, PlayerEntity player, TextRenderer textRenderer) {
        drawContext.drawTexture(HEALTHBAR_TEXTURE, x, y, 0, 0, width, height, width, height);
        int finalwidth = width - 4;
        int progressWidth = isMaxLevel ? finalwidth : (int) (finalwidth * progress);

        if (progressWidth > 0) {
            if (healthGlowTimer > 0) {
                int glowAlpha = calculateGlowStrength(healthGlowTimer, XP_GLOW_DURATION);
                int outerGlowColor = (glowAlpha / 6) << 24 | glowColor;
                int progressX = x + 2;
                int progressY = y + 2;
                int progressH = height - 4;

                drawContext.fill(progressX - 3, progressY - 3, progressX + progressWidth + 3, progressY + progressH + 3, outerGlowColor);
                outerGlowColor = (glowAlpha / 4) << 24 | glowColor;
                drawContext.fill(progressX - 2, progressY - 2, progressX + progressWidth + 2, progressY + progressH + 2, outerGlowColor);
                int innerGlowColor = (glowAlpha / 2) << 24 | glowColor;
                drawContext.fill(progressX - 1, progressY - 1, progressX + progressWidth + 1, progressY + progressH + 1, innerGlowColor);
            }

            DrawContextUtils.renderVerticalGradient(drawContext, x + 2, y + 2, progressWidth, height - 4, colorStart, colorEnd);
            DrawContextUtils.renderAnimatedFluidBar(drawContext, x + 2, y + 2, progressWidth, height - 4, WATER_CAUSTIC, portalTime, 0.4f);
        }

        int highlightColor = 0x40FFFFFF;
        drawContext.fill(x + 1, y + 1, x + width - 1, y + 2, highlightColor);

        String healthText = formatHealthValue(player.getHealth() + player.getAbsorptionAmount()) + "/" +
                formatHealthValue(player.getMaxHealth());

        int textAlpha = (int)(textFadeAlpha * 255);
        int textColor = (textAlpha << 24) | 0xFFFFFF;

        DrawContextUtils.withScaleAt(drawContext, 0.6f, x + 4, y + 3, () -> {
            drawContext.drawTextWithShadow(textRenderer, healthText, 0, 0, textColor);
        });
    }


    private static String formatHealthValue(float value) {
        if (value == (int) value) return String.format("%.0f", value);
        else return String.format("%.1f", value);
    }

    private static int calculateGlowStrength(float timer, float duration) {
        float normalizedTime = timer / duration;
        return (int) (255 * normalizedTime);
    }

    public static class Armor {
        private static float animatedArmorProgress = 0.0f;
        private static float armorGlowTimer = 0.0f;
        private static float armorShakeTimer = 0.0f;
        private static int lastArmor = 0;
        private static final float ARMOR_GLOW_DURATION = 2.0f;
        private static final float ARMOR_SHAKE_DURATION = 0.5f;
        private static final float ARMOR_SHAKE_INTENSITY = 2.0f;
        private static final Identifier ARMOR_TEXTURE =  Identifier.ofVanilla("hud/armor_full");

        // Text fade animation (same as health)
        private static float textFadeAlpha = 0.3f;
        private static float targetTextAlpha = 0.3f;
        private static final float TEXT_FADE_SPEED = 0.02f;

        public static void render(DrawContext context, PlayerEntity player, int x, int y, TextRenderer textRenderer) {
            int armor = player.getArmor();
            if (armor <= 0) return;

            updateArmorAnimations(armor);

            // White armor colors
            int colorStart = 0xFFFFFFFF;
            int colorEnd = 0xFFCCCCCC;
            int glowColor = 0x00FFFFFF;

            // Calculate shake offset
            float shakeOffsetX = 0;
            float shakeOffsetY = 0;
            if (armorShakeTimer > 0) {
                shakeOffsetX = (float)((Math.random() - 0.5) * 2 * ARMOR_SHAKE_INTENSITY * (armorShakeTimer / ARMOR_SHAKE_DURATION));
                shakeOffsetY = (float)((Math.random() - 0.5) * 2 * ARMOR_SHAKE_INTENSITY * (armorShakeTimer / ARMOR_SHAKE_DURATION));
            }

            renderArmorBar(context, x + (int)shakeOffsetX, y + (int)shakeOffsetY, 78, 8,
                    animatedArmorProgress, armor >= 20, colorStart, colorEnd, glowColor,
                    player, textRenderer);
            context.drawGuiTexture(ARMOR_TEXTURE, x - 6, y - 1 , 9, 9);
        }

        private static void updateArmorAnimations(int currentArmor) {
            float targetProgress = currentArmor / 20.0f;
            float lerpSpeed = 0.05f;
            animatedArmorProgress = MathHelper.lerp(lerpSpeed, animatedArmorProgress, targetProgress);

            // Trigger effects when armor changes (taking damage)
            if (currentArmor != lastArmor) {
                if (currentArmor < lastArmor) { // Armor decreased - taking damage
                    armorShakeTimer = ARMOR_SHAKE_DURATION;
                }
                armorGlowTimer = ARMOR_GLOW_DURATION;
                lastArmor = currentArmor;

                // Trigger text fade to full opacity
                targetTextAlpha = 1.0f;
            }

            if (armorGlowTimer > 0) armorGlowTimer -= 0.016f;
            if (armorShakeTimer > 0) armorShakeTimer -= 0.016f;

            // Update text fade animation
            updateTextFadeAnimation();
        }

        private static void updateTextFadeAnimation() {
            textFadeAlpha = MathHelper.lerp(TEXT_FADE_SPEED, textFadeAlpha, targetTextAlpha);

            if (targetTextAlpha == 1.0f && Math.abs(textFadeAlpha - 1.0f) < 0.01f) {
                if (armorGlowTimer < ARMOR_GLOW_DURATION * 0.7f) {
                    targetTextAlpha = 0.3f;
                }
            }

            textFadeAlpha = MathHelper.clamp(textFadeAlpha, 0.0f, 1.0f);
        }

        private static void renderArmorBar(DrawContext drawContext, int x, int y, int width, int height,
                                           float progress, boolean isMaxLevel, int colorStart, int colorEnd,
                                           int glowColor, PlayerEntity player, TextRenderer textRenderer) {
            drawContext.drawTexture(HEALTHBAR_TEXTURE, x, y, 0, 0, width, height, width, height);
            int finalwidth = width - 4;
            int progressWidth = isMaxLevel ? finalwidth : (int) (finalwidth * progress);

            if (progressWidth > 0) {
                // Simple glow effect for armor
                if (armorGlowTimer > 0) {
                    int glowAlpha = (int)((armorGlowTimer / ARMOR_GLOW_DURATION) * 255);
                    int outerGlowColor = (glowAlpha / 4) << 24 | glowColor;
                    int progressX = x + 2;
                    int progressY = y + 2;
                    int progressH = height - 4;

                    drawContext.fill(progressX - 2, progressY - 2, progressX + progressWidth + 2, progressY + progressH + 2, outerGlowColor);
                    drawContext.fill(progressX - 1, progressY - 1, progressX + progressWidth + 1, progressY + progressH + 1, outerGlowColor);
                }

                DrawContextUtils.renderVerticalGradient(drawContext, x + 2, y + 2, progressWidth, height - 4, colorStart, colorEnd);
            }

            int highlightColor = 0x40FFFFFF;
            drawContext.fill(x + 1, y + 1, x + width - 1, y + 2, highlightColor);

            // Armor text with fade animation
            String armorText = String.valueOf(player.getArmor());
            int textAlpha = (int)(textFadeAlpha * 255);
            int textColor = (textAlpha << 24) | 0xFFFFFF;

            DrawContextUtils.withScaleAt(drawContext, 0.6f, x + 4, y + 3, () -> {
                drawContext.drawTextWithShadow(textRenderer, armorText, 0, 0, textColor);
            });
        }
    }
}