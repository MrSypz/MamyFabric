package com.sypztep.mamy.client.event;

import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.component.living.PlayerStanceComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SkillHudOverlayRenderer {
    public static void register() {
        HudRenderCallback.EVENT.register(SkillHudOverlayRenderer::render);
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        PlayerStanceComponent stanceComponent = ModEntityComponents.PLAYERSTANCE.get(client.player);

        renderStanceAndSkillInfo(context, stanceComponent);
    }

    private static void renderStanceAndSkillInfo(DrawContext context, PlayerStanceComponent stanceComponent) {
        int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

        // Stance indicator (top right)
        int stanceX = screenWidth - 120;
        int stanceY = 10;

        Text stanceText = stanceComponent.isInCombatStance() ?
                Text.literal("⚔ COMBAT").formatted(Formatting.RED, Formatting.BOLD) :
                Text.literal("✋ NORMAL").formatted(Formatting.GRAY);

        context.drawText(MinecraftClient.getInstance().textRenderer, stanceText, stanceX, stanceY, 0xFFFFFF, true);

        // Skill hints (when in combat stance)
        if (stanceComponent.isInCombatStance()) {
            int hintX = 10;
            int hintY = screenHeight - 80;

            context.drawText(MinecraftClient.getInstance().textRenderer,
                    Text.literal("Skills Available:").formatted(Formatting.YELLOW),
                    hintX, hintY, 0xFFFFFF, true);

            context.drawText(MinecraftClient.getInstance().textRenderer,
                    Text.literal("Z/X/C/V").formatted(Formatting.GRAY),
                    hintX, hintY + 10, 0xFFFFFF, true);

        }
    }
}
