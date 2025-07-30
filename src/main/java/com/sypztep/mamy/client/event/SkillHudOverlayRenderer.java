package com.sypztep.mamy.client.event;

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

        renderStanceIndicator(context, stanceComponent.isInCombatStance());
    }

    private static void renderStanceIndicator(DrawContext context, boolean inCombatStance) {
        int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int x = screenWidth - 100;
        int y = 10;

        Text stanceText = inCombatStance ?
                Text.literal("COMBAT").formatted(Formatting.RED, Formatting.BOLD) :
                Text.literal("NORMAL").formatted(Formatting.GRAY);

        context.fill(x - 5, y - 2, x + 80 + stanceText.getString().length(), y + 12, 0x80000000);

        context.drawText(MinecraftClient.getInstance().textRenderer,
                Text.literal("Stance: ").append(stanceText), x, y, 0xFFFFFF, true);
    }
}
