package com.sypztep.mamy.client.event.hud;

import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.SkillCastingManager;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

@Environment(EnvType.CLIENT)
public class CastingHudRenderer {

    public static void init() {
        HudRenderCallback.EVENT.register(CastingHudRenderer::renderCastingBar);
    }

    private static void renderCastingBar(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        SkillCastingManager castingManager = SkillCastingManager.getInstance();

        if (!castingManager.isCasting() || client.player == null) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Position below hotbar
        int barWidth = 182;
        int barHeight = 8;
        int x = (screenWidth - barWidth) / 2;
        int y = screenHeight - 32 - barHeight - 25;

        // Background (dark border)
        context.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xFF000000);

        // Inner background (gray)
        context.fill(x, y, x + barWidth, y + barHeight, 0xFF333333);

        // Progress bar
        float progress = castingManager.getCastProgress();
        int progressWidth = (int)(barWidth * progress);

        // Animated casting color (blue to green)
        int color1 = 0xFF4A90E2; // Blue
        int color2 = 0xFF67B26F; // Green
        int currentColor = ColorHelper.Argb.lerp(progress, color1, color2);

        if (progressWidth > 0) {
            context.fill(x, y, x + progressWidth, y + barHeight, currentColor);
        }

        // Get skill name
        Identifier skillId = castingManager.getCurrentCastingSkill();
        String skillName = "Unknown";
        if (skillId != null) {
            Skill skill = SkillRegistry.getSkill(skillId);
            if (skill != null) {
                skillName = skill.getName();
            }
        }

        // Skill name and remaining time
        float remainingSeconds = castingManager.getRemainingTicks() / 20.0f;
        Text castingText = Text.literal("Casting: ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal(skillName).formatted(Formatting.WHITE))
                .append(Text.literal(String.format(" (%.1fs)", remainingSeconds))
                        .formatted(Formatting.GRAY));

        int textWidth = client.textRenderer.getWidth(castingText);
        context.drawTextWithShadow(client.textRenderer, castingText,
                (screenWidth - textWidth) / 2, y - 14, 0xFFFFFFFF);

        // Cancel instruction
        Text cancelText = Text.literal("Press skill key again to cancel")
                .formatted(Formatting.DARK_GRAY);
        int cancelWidth = client.textRenderer.getWidth(cancelText);
        context.drawTextWithShadow(client.textRenderer, cancelText,
                (screenWidth - cancelWidth) / 2, y + barHeight + 4, 0xFFAAAAAA);
    }
}