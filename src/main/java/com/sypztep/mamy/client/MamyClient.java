package com.sypztep.mamy.client;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.client.event.ShockwaveHandler;
import com.sypztep.mamy.client.event.SkillCooldownCleanUpEvent;
import com.sypztep.mamy.client.event.animation.CrowdControlAnimationManager;
import com.sypztep.mamy.client.event.animation.SkillAnimationManager;
import com.sypztep.mamy.client.event.hud.*;
import com.sypztep.mamy.client.event.tooltip.ItemWeightTooltip;
import com.sypztep.mamy.client.screen.CameraShakeManager;
import com.sypztep.mamy.client.screen.overlay.IconOverlayManager;
import com.sypztep.mamy.common.init.ModEntityTypes;
import com.sypztep.mamy.common.init.ModParticles;
import com.sypztep.mamy.common.init.ModPayloads;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class MamyClient implements ClientModInitializer {
    public static ModConfig config = new ModConfig();

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        ModEntityTypes.Client.registerRender();

        SkillAnimationManager.initialize();
        CrowdControlAnimationManager.initialize();

        ModParticles.Client.init();

        ModPayloads.Client.registerClientPayloads();
        ModKeyBindings.register();
        LevelHudRenderer.register();
        ToastHudRenderer.register();
        ResourceBarHudRenderer.register();

        SkillHudOverlayRenderer.register();
        HotbarIconOverlayRenderer.register();
        ShieldScoreHudRenderer.register();
        CastingHudRenderer.init();

        SkillCooldownCleanUpEvent.register();

        IconOverlayManager.initialize(); // Only call once
        VersionHudRenderer.register(); // <- This adds your bottom-left alpha tag
        ItemWeightTooltip.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                CameraShakeManager.getInstance().tick(0.05f);
                ShockwaveHandler.tick();
            }
        });
    }
}