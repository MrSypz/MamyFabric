package com.sypztep.mamy.client.event.hud;

import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.SkillCastingManager;
import com.sypztep.mamy.common.init.ModClassesSkill;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public final class CastingHudRenderer implements HudRenderCallback {
    private static final float LERP_TIME = 0.05f;

    private float smoothProgress = 0f;
    private float smoothRemainingTime = 0f;
    private float barAlpha = 0f;
    private float textAlpha = 0f;
    private float pulseAnimation = 0f;
    private boolean wasCasting = false;

    public static void init() {
        HudRenderCallback.EVENT.register(new CastingHudRenderer());
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        SkillCastingManager castingManager = SkillCastingManager.getInstance();

        float deltaTime = renderTickCounter.getTickDelta(true);
        boolean isCasting = castingManager.isCasting() && client.player != null;

        pulseAnimation += deltaTime * 0.1f;
        if (pulseAnimation > Math.PI * 2) {
            pulseAnimation -= (float) (Math.PI * 2);
        }

        float targetAlpha = isCasting ? 1f : 0f;
        float alphaLerpSpeed = isCasting ? deltaTime * LERP_TIME * 3f : deltaTime * LERP_TIME;
        barAlpha = MathHelper.lerp(alphaLerpSpeed, barAlpha, targetAlpha);
        textAlpha = MathHelper.lerp(alphaLerpSpeed, textAlpha, targetAlpha);

        if (barAlpha <= 0.01f && !isCasting) {
            wasCasting = false;
            return;
        }

        if (!isCasting && !wasCasting) return;

        if (isCasting) {
            float currentProgress = castingManager.getCastProgress();
            float currentRemainingTime = castingManager.getRemainingTicks() * LERP_TIME;

            smoothProgress = MathHelper.lerp(deltaTime * 0.9f, smoothProgress, currentProgress);
            smoothRemainingTime = MathHelper.lerp(deltaTime * 0.9f, smoothRemainingTime, currentRemainingTime);

            wasCasting = true;
        } else {
            smoothProgress = MathHelper.lerp(deltaTime * LERP_TIME, smoothProgress, 0f);
            smoothRemainingTime = MathHelper.lerp(deltaTime * LERP_TIME, smoothRemainingTime, 0f);
        }

        renderCastingHud(context, castingManager, isCasting);
    }

    private void renderCastingHud(DrawContext context, SkillCastingManager castingManager, boolean isCasting) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Position below hotbar
        int barWidth = 182;
        int barHeight = 5;
        int x = (screenWidth - barWidth) / 2;
        int y = screenHeight - 32 - barHeight - 25;

        int bgAlpha = (int)(barAlpha * 255) << 24;
        int progressAlpha = (int)(barAlpha * 255) << 24;
        int textAlphaInt = (int)(textAlpha * 255);

        context.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, bgAlpha);

        context.fill(x, y, x + barWidth, y + barHeight, bgAlpha | 0x333333);

        int progressWidth = (int)(barWidth * smoothProgress);

        if (progressWidth > 0 && barAlpha > 0.01f) {
            int progressColor = progressAlpha | 0xFFFFFF; // White

            context.fill(x, y, x + progressWidth, y + barHeight, progressColor);

            int highlightAlpha = (int)(barAlpha * 64) << 24;
            context.fill(x, y, x + progressWidth, y + 1, highlightAlpha | 0xFFFFFF);
        }

        if (textAlpha > 0.01f && isCasting) {
            Identifier skillId = castingManager.getCurrentCastingSkill();
            String skillName = "Unknown";
            if (skillId != null) {
                Skill skill = ModClassesSkill.getSkill(skillId);
                if (skill != null) {
                    skillName = skill.getName();
                }
            }

            Text castingText = Text.literal("Casting: ")
                    .formatted(Formatting.YELLOW)
                    .append(Text.literal(skillName).formatted(Formatting.WHITE))
                    .append(Text.literal(String.format(" (%.1fs)", smoothRemainingTime))
                            .formatted(Formatting.GRAY));

            int textWidth = client.textRenderer.getWidth(castingText);
            int textColor = (textAlphaInt << 24) | 0xFFFFFF;

            context.drawTextWithShadow(client.textRenderer, castingText,
                    (screenWidth - textWidth) / 2, y - 14, textColor);

            Text cancelText = Text.literal("Press skill key again to cancel")
                    .formatted(Formatting.DARK_GRAY);
            int cancelWidth = client.textRenderer.getWidth(cancelText);
            int cancelColor = ((int)(textAlpha * 170) << 24) | 0xAAAAAA;

            context.drawTextWithShadow(client.textRenderer, cancelText,
                    (screenWidth - cancelWidth) / 2, y + barHeight + 4, cancelColor);
        }
    }
}