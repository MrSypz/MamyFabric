package com.sypztep.mamy.client.event.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.util.DrawContextUtils;
import com.sypztep.mamy.common.component.living.PlayerShieldScoreComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ShieldScoreHudRenderer implements HudRenderCallback {
    private static final Identifier SHIELD_EMPTY = Mamy.id("textures/gui/sprites/hud/shield_empty.png");
    private static final Identifier SHIELD_FULL = Mamy.id("textures/gui/sprites/hud/shield.png");
    private static final int TEXTURE_SIZE = 64;
    private static final int FADE_OUT_DELAY = 60; // 3 seconds at 20 TPS

    private static float currentAlpha = 0.0f;
    private static int fadeOutTimer = 0;
    private static boolean wasFullyCharged = false;

    public static void register() {
        HudRenderCallback.EVENT.register(new ShieldScoreHudRenderer());
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player == null || client.options.hudHidden) return;

        // Shield points only
        PlayerShieldScoreComponent shieldComponent = ModEntityComponents.PLAYERSHIELDSCORE.get(player);
        double percentage = shieldComponent.getShieldScorePercentage();

        // --- Fade logic ---
        if (percentage >= 100.0) {
            if (!wasFullyCharged) {
                wasFullyCharged = true;
                fadeOutTimer = FADE_OUT_DELAY;
            }

            if (fadeOutTimer > 0) {
                fadeOutTimer--;
                currentAlpha = 1.0f;
            } else {
                currentAlpha = MathHelper.lerp(0.05f, currentAlpha, 0.0f);
            }
        } else {
            if (wasFullyCharged) {
                wasFullyCharged = false;
            }
            currentAlpha = MathHelper.lerp(0.1f, currentAlpha, 1.0f);
            fadeOutTimer = 0;
        }

        if (currentAlpha <= 0.01f) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int centerX = screenWidth / 2 + 64;
        int centerY = screenHeight / 2 + 64;

        // --- Enable blending for texture transparency ---
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // --- Draw shield textures (scaled & faded) ---
        DrawContextUtils.withScaleAt(context, 0.5f, centerX, centerY, () -> {
            // Apply alpha to all textures
            context.setShaderColor(1f, 1f, 1f, currentAlpha);

            // Empty shield
            context.drawTexture(SHIELD_EMPTY, -TEXTURE_SIZE / 2, -TEXTURE_SIZE / 2,
                    0, 0, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);

            // Filled shield
            if (percentage > 0) {
                double fillRatio = percentage / 100.0;
                int fillHeight = (int) (fillRatio * TEXTURE_SIZE);
                int fillStartY = TEXTURE_SIZE - fillHeight;
                int textureStartY = TEXTURE_SIZE - fillHeight;

                context.drawTexture(SHIELD_FULL,
                        -TEXTURE_SIZE / 2, -TEXTURE_SIZE / 2 + fillStartY,
                        0, textureStartY,
                        TEXTURE_SIZE, fillHeight,
                        TEXTURE_SIZE, TEXTURE_SIZE);
            }

            // Reset shader color to avoid affecting other HUD elements
//            context.setShaderColor(1f, 1f, 1f, 1f);
        });


        // --- Draw percentage text (unscaled & faded) ---
        TextRenderer textRenderer = client.textRenderer;
        String percentageText = String.valueOf((int) percentage);

        int textWidth = textRenderer.getWidth(percentageText);
        int textX = centerX - textWidth / 2;
        int textY = centerY - textRenderer.fontHeight / 2;
//        context.setShaderColor(1f, 1f, 1f, currentAlpha);
        context.drawText(textRenderer, percentageText, textX, textY, 0xFFFFFF, true);
        // --- Disable blending after texture rendering ---
        context.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.disableBlend();
    }
}