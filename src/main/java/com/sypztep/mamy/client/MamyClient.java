package com.sypztep.mamy.client;

import com.sypztep.mamy.client.event.*;
import com.sypztep.mamy.client.render.entity.BloodLustEntityRenderer;
import com.sypztep.mamy.common.init.ModEntityTypes;
import com.sypztep.mamy.common.init.ModParticles;
import com.sypztep.mamy.common.init.ModPayloads;
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
        ScreenEventHandler.register();
        ResourceBarHudRenderer.register();
        SkillHudOverlayRenderer.register();
    }
}
