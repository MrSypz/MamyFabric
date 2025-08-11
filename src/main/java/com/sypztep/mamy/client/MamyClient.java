package com.sypztep.mamy.client;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.client.event.hud.*;
import com.sypztep.mamy.client.event.passive.dexterity.BowItemEvent;
import com.sypztep.mamy.client.render.entity.BloodLustEntityRenderer;
import com.sypztep.mamy.client.screen.overlay.IconOverlayManager;
import com.sypztep.mamy.common.init.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class MamyClient implements ClientModInitializer {
    public static ModConfig config = new ModConfig();

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

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
