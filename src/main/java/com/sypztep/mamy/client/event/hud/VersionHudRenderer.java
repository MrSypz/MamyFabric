package com.sypztep.mamy.client.event.hud;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public class VersionHudRenderer implements HudRenderCallback {
    private final String displayText;

    public VersionHudRenderer() {
        // Grab version from fabric.mod.json (linked to gradle.properties)
        String version = FabricLoader.getInstance()
                .getModContainer("mamy") // your mod id here
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        displayText = "mamy | Alpha v" + version +" nothing stable..";
    }

    @Override
    public void onHudRender(DrawContext drawContext,  RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden) return;

        Window window = client.getWindow();
        int x = 4; // Left padding
        int y = window.getScaledHeight() - 10; // Bottom padding

        drawContext.drawText(client.textRenderer, displayText, x, y, 0xFFFFFF, true);
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new VersionHudRenderer());
    }
}
