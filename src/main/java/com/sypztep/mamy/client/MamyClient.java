package com.sypztep.mamy.client;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.client.event.SkillCooldownCleanUpEvent;
import com.sypztep.mamy.client.event.animation.CrowdControlAnimationManager;
import com.sypztep.mamy.client.event.animation.SkillAnimationManager;
import com.sypztep.mamy.client.event.hud.*;
import com.sypztep.mamy.client.event.tooltip.ItemWeightTooltip;
import com.sypztep.mamy.client.render.entity.ArrowRainEntityRenderer;
import com.sypztep.mamy.client.render.entity.ArrowStrafeEntityRenderer;
import com.sypztep.mamy.client.render.entity.HealingLightEntityRenderer;
import com.sypztep.mamy.client.screen.overlay.IconOverlayManager;
import com.sypztep.mamy.common.init.ModEntityTypes;
import com.sypztep.mamy.common.init.ModParticles;
import com.sypztep.mamy.common.init.ModPayloads;
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

        SkillAnimationManager.initialize();
        CrowdControlAnimationManager.initialize();

        ModParticles.Client.init();

        EntityRendererRegistry.register(ModEntityTypes.HEALING_LIGHT, HealingLightEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.ARROW_RAIN, ArrowRainEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.ARROW_STRAFE, ArrowStrafeEntityRenderer::new);

        ModPayloads.Client.registerClientPayloads();
        ModKeyBindings.register();
        LevelHudRenderer.register();
        ToastHudRenderer.register();
        ResourceBarHudRenderer.register();

        SkillHudOverlayRenderer.register();
        HotbarIconOverlayRenderer.register();
        CastingHudRenderer.init();

        SkillCooldownCleanUpEvent.register();

        IconOverlayManager.initialize(); // Only call once
        VersionHudRenderer.register(); // <- This adds your bottom-left alpha tag
        ItemWeightTooltip.register();
    }
}