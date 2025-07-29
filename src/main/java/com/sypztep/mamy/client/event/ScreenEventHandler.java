package com.sypztep.mamy.client.event;

import com.sypztep.mamy.client.screen.PassiveAbilityScreen;
import com.sypztep.mamy.client.screen.PlayerInfoScreen;
import com.sypztep.mamy.client.screen.camera.SpiralCameraController;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public final class ScreenEventHandler {
    private static Screen lastScreen = null;
    private static final SpiralCameraController cameraController = SpiralCameraController.getInstance();

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ScreenEventHandler::onClientTick);
    }
    public ScreenEventHandler() {}
    private static void onClientTick(MinecraftClient client) {
        Screen currentScreen = client.currentScreen;

        if (currentScreen != lastScreen) {
            if (lastScreen != null && currentScreen == null) {
                onScreenClosed(lastScreen);
            }

            if (currentScreen != null && lastScreen == null) {
                onScreenOpened(currentScreen);
            }

            if (currentScreen != null && lastScreen != null && currentScreen != lastScreen) {
                onScreenClosed(lastScreen);
                onScreenOpened(currentScreen);
            }

            lastScreen = currentScreen;
        }
    }

    private static void onScreenOpened(Screen screen) {
        if (shouldAnimateForScreen(screen)) {
            cameraController.startSpiralAnimation();
        }
    }

    private static void onScreenClosed(Screen screen) {
        if (shouldAnimateForScreen(screen)) {
            cameraController.stopSpiralAnimation();
        }
    }

    private static boolean shouldAnimateForScreen(Screen screen) {
        return screen instanceof PlayerInfoScreen ||
                screen instanceof PassiveAbilityScreen;
    }
}
