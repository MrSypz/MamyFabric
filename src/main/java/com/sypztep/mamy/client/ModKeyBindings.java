package com.sypztep.mamy.client;

import com.sypztep.mamy.client.screen.PassiveAbilityScreen;
import com.sypztep.mamy.client.screen.PlayerInfoScreen;
import com.sypztep.mamy.common.payload.ToggleStancePayloadC2S;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {

    public static KeyBinding OPEN_STAT_SCREEN;
    public static KeyBinding OPEN_PASSIEV_SCREEN;
    public static KeyBinding SWITCH_STANCE;

    public static void register() {
        OPEN_STAT_SCREEN = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.open_stat_screen", // Translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K, // K key
                "category.mamy.keys" // Category translation key
        ));
        OPEN_PASSIEV_SCREEN = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.open_passive_screen", // Translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P, // K key
                "category.mamy.keys" // Category translation key
        ));
        SWITCH_STANCE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.swotch_stance", // Translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R, // K key
                "category.mamy.combat" // Category translation key
        ));

        ClientTickEvents.END_CLIENT_TICK.register(ModKeyBindings::handleKeyInputs);
    }

    private static void handleKeyInputs(MinecraftClient client) {
        if (OPEN_STAT_SCREEN.wasPressed()) {
            if (client.player != null && client.currentScreen == null) {
                client.setScreen(new PlayerInfoScreen(client));
            }
        }
        if (OPEN_PASSIEV_SCREEN.wasPressed()) {
            if (client.player != null && client.currentScreen == null) {
                client.setScreen(new PassiveAbilityScreen(client));
            }
        }
        if (SWITCH_STANCE.wasPressed()) {
            ToggleStancePayloadC2S.send();
        }
    }
}