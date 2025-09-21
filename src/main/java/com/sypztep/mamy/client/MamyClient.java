package com.sypztep.mamy.client;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.client.event.SkillCooldownCleanUpEvent;
import com.sypztep.mamy.client.event.animation.NetworkAnimationManager;
import com.sypztep.mamy.client.event.animation.SkillAnimationManager;
import com.sypztep.mamy.client.event.hud.*;
import com.sypztep.mamy.client.event.tooltip.ItemWeightTooltip;
import com.sypztep.mamy.client.render.DebugBoxRenderer;
import com.sypztep.mamy.client.render.item.DynamicItemRenderer;
import com.sypztep.mamy.client.render.item.LoadingCustomModelImpl;
import com.sypztep.mamy.client.screen.CameraShakeManager;
import com.sypztep.mamy.client.screen.overlay.IconOverlayManager;
import com.sypztep.mamy.common.init.ModEntityTypes;
import com.sypztep.mamy.common.init.ModParticles;
import com.sypztep.mamy.common.init.ModPayloads;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class MamyClient implements ClientModInitializer {
    public static ModConfig config = new ModConfig();

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

        ModelLoadingPlugin.register(new LoadingCustomModelImpl());
        DynamicItemRenderer.initItemResource();

        IconOverlayManager.initialize();
        VersionHudRenderer.register();
        ItemWeightTooltip.register();

        ClientTickEvents.END_CLIENT_TICK.register(CameraShakeManager.Event::register);
        WorldRenderEvents.AFTER_ENTITIES.register((context) -> {
            MatrixStack matrices = context.matrixStack();
            VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
            DebugBoxRenderer.render(matrices, consumers, context.camera());
        });
    }
}