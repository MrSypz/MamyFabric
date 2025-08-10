package com.sypztep.mamy.client;

import com.sypztep.mamy.client.event.hud.*;
import com.sypztep.mamy.client.event.passive.dexterity.BowItemEvent;
import com.sypztep.mamy.client.render.entity.BloodLustEntityRenderer;
import com.sypztep.mamy.client.screen.overlay.IconOverlayManager;
import com.sypztep.mamy.common.init.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class MamyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModParticles.Client.init();

        EntityRendererRegistry.register(ModEntityTypes.BLOOD_LUST, BloodLustEntityRenderer::new);

        ModPayloads.Client.registerClientPayloads();
        ModKeyBindings.register();
        LevelHudRenderer.register();
        ToastHudRenderer.register();
        ResourceBarHudRenderer.register();
        SkillHudOverlayRenderer.register();
        SkillBoundingRenderer.register();
        HotbarIconOverlayRenderer.register();
        BowItemEvent.register();

        IconOverlayManager.initialize(); // Only call once
        VersionHudRenderer.register(); // <- This adds your bottom-left alpha tag
    }
}
