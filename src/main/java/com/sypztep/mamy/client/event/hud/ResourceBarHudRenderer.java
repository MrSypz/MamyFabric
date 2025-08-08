package com.sypztep.mamy.client.event.hud;

import com.sypztep.mamy.client.screen.hud.ResourceBarHud;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public final class ResourceBarHudRenderer implements HudRenderCallback {
    public static final ResourceBarHud resourceBarHud = new ResourceBarHud();

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.currentScreen != null) {
            return;
        }

        float deltaTime = tickCounter.getTickDelta(false) / 20.0f; // Convert to seconds

        resourceBarHud.render(drawContext, client, deltaTime);
    }

    /**
     * Register this HUD renderer
     */
    public static void register() {
        HudRenderCallback.EVENT.register(new ResourceBarHudRenderer());
    }
}