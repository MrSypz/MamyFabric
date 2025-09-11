package com.sypztep.mamy.client;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.client.event.SkillCooldownCleanUpEvent;
import com.sypztep.mamy.client.event.animation.NetworkAnimationManager;
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

    public static boolean hasMagicArrows = false;
    public static float currentDistortionTime = 0.0f;
    public static float currentDistortionStrength = 0.0f;

    public static void updateDistortionShader(float time, float strength) {
        currentDistortionTime = time;
        currentDistortionStrength = strength;
        hasMagicArrows = true; // Mark that we have active arrows
    }
    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        ModEntityTypes.Client.registerRender();

        SkillAnimationManager.initialize();
        NetworkAnimationManager.initialize();

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

        ClientTickEvents.END_CLIENT_TICK.register(CameraShakeManager.Event::register);
//        FabricVeilRenderLevelStageEvent.EVENT.register((stage, levelRenderer, bufferSource, matrixStack, frustumMatrix, projectionMatrix, renderTick, deltaTracker, camera, frustum) -> {
//            if (stage == VeilRenderLevelStageEvent.Stage.AFTER_ENTITIES) {
//            }
//        });
    }
}