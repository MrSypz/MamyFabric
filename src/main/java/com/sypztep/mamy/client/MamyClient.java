package com.sypztep.mamy.client;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.client.animation.SkillAnimationRegistry;
import com.sypztep.mamy.client.event.SkillCooldownCleanUpEvent;
import com.sypztep.mamy.client.event.hud.*;
import com.sypztep.mamy.client.event.passive.dexterity.BowItemEvent;
import com.sypztep.mamy.client.event.tooltip.ItemWeightTooltip;
import com.sypztep.mamy.client.render.entity.BloodLustEntityRenderer;
import com.sypztep.mamy.client.screen.hud.CastingHudRenderer;
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

        ModParticles.Client.init();

        EntityRendererRegistry.register(ModEntityTypes.BLOOD_LUST, BloodLustEntityRenderer::new);

        ModPayloads.Client.registerClientPayloads();
        ModKeyBindings.register();
        LevelHudRenderer.register();
        ToastHudRenderer.register();
        ResourceBarHudRenderer.register();

        SkillHudOverlayRenderer.register();
        HotbarIconOverlayRenderer.register();
        CastingHudRenderer.init();

        BowItemEvent.register();
        SkillCooldownCleanUpEvent.register();

        IconOverlayManager.initialize(); // Only call once
        VersionHudRenderer.register(); // <- This adds your bottom-left alpha tag
        ItemWeightTooltip.register();
        SkillAnimationRegistry.registerAnimations();

//        ClientTickEvents.END_CLIENT_TICK.register(client -> {
//            if (!KeyEntryScreen.keyValidated && client.currentScreen instanceof TitleScreen) {
//                KeyEntryScreen.keyValidated = true;
//
//                String identity = KeyEntryScreen.getPlayerIdentity(client);
//                String apiUrl = String.format("http://tyranus.online/smp/check_token.php?uuid=%s", identity);
//
//                HttpClient httpClient = HttpClient.newHttpClient();
//                HttpRequest request = HttpRequest.newBuilder()
//                        .uri(URI.create(apiUrl))
//                        .GET()
//                        .build();
//
//                CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
//
//                responseFuture.thenAccept(response -> {
//                    try {
//                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
//                        boolean validated = json.get("success").getAsBoolean();
//                        if (validated) {
//                            KeyEntryScreen.keyValidated = true;
//                        } else {
//                            client.execute(() -> {
//                                client.setScreen(new KeyEntryScreen());
//                                KeyEntryScreen.keyValidated = false; // Reset flag so user can submit key
//                            });
//                        }
//                    } catch (Exception e) {
//                        client.execute(() -> {
//                            client.setScreen(new KeyEntryScreen());
//                            KeyEntryScreen.keyValidated = false;
//                        });
//                    }
//                }).exceptionally(ex -> {
//                    client.execute(() -> {
//                        client.setScreen(new KeyEntryScreen());
//                        KeyEntryScreen.keyValidated = false;
//                    });
//                    return null;
//                });
//            }
//        });
    }
}