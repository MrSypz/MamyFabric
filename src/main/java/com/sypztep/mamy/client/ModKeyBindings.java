package com.sypztep.mamy.client;

import com.sypztep.mamy.client.screen.PassiveAbilityScreen;
import com.sypztep.mamy.client.screen.PlayerInfoScreen;
import com.sypztep.mamy.client.screen.SkillBindingScreen;
import com.sypztep.mamy.client.screen.SkillLearningScreen;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.component.living.PlayerStanceComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.payload.ToggleStancePayloadC2S;
import com.sypztep.mamy.common.payload.UseSkillPayloadC2S;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {

    // UI Keys
    public static KeyBinding OPEN_STAT_SCREEN;
    public static KeyBinding OPEN_PASSIVE_SCREEN;
    public static KeyBinding SWITCH_STANCE;
    public static KeyBinding OPEN_SKILL_BINDING;
    public static KeyBinding OPEN_SKILL_LEARNING;

    // Skill Slots (8 total)
    public static KeyBinding SKILL_SLOT_1; // Z
    public static KeyBinding SKILL_SLOT_2; // X
    public static KeyBinding SKILL_SLOT_3; // C
    public static KeyBinding SKILL_SLOT_4; // V
    public static KeyBinding SKILL_SLOT_5; // Shift + Z
    public static KeyBinding SKILL_SLOT_6; // Shift + X
    public static KeyBinding SKILL_SLOT_7; // Shift + C
    public static KeyBinding SKILL_SLOT_8; // Shift + V

    public static void register() {
        // UI Keys
        OPEN_STAT_SCREEN = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.open_stat_screen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.mamy.keys"
        ));

        OPEN_PASSIVE_SCREEN = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.open_passive_screen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "category.mamy.keys"
        ));

        SWITCH_STANCE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.switch_stance",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.mamy.combat"
        ));

        OPEN_SKILL_BINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.open_skill_binding",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.mamy.keys"
        ));
        OPEN_SKILL_LEARNING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.open_skill_learning",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N, // K key
                "category.mamy.keys"
        ));



        // Skill Slots (8 total)
        SKILL_SLOT_1 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.skill_slot_1",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                "category.mamy.skills"
        ));

        SKILL_SLOT_2 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.skill_slot_2",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "category.mamy.skills"
        ));

        SKILL_SLOT_3 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.skill_slot_3",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "category.mamy.skills"
        ));

        SKILL_SLOT_4 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.skill_slot_4",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.mamy.skills"
        ));

        // Extra skill slots - unbound by default, let user set
        SKILL_SLOT_5 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.skill_slot_5",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(), // Unbound
                "category.mamy.skills"
        ));

        SKILL_SLOT_6 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.skill_slot_6",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(), // Unbound
                "category.mamy.skills"
        ));

        SKILL_SLOT_7 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.skill_slot_7",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(), // Unbound
                "category.mamy.skills"
        ));

        SKILL_SLOT_8 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.skill_slot_8",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(), // Unbound
                "category.mamy.skills"
        ));


        ClientTickEvents.END_CLIENT_TICK.register(ModKeyBindings::handleKeyInputs);
    }

    private static void handleKeyInputs(MinecraftClient client) {
        if (client.player == null) return;
        if (client.currentScreen != null) return;

        // UI Keys (these work regardless of stance)
        if (OPEN_STAT_SCREEN.wasPressed()) {
            client.setScreen(new PlayerInfoScreen(client));
        }

        if (OPEN_PASSIVE_SCREEN.wasPressed()) {
            client.setScreen(new PassiveAbilityScreen(client));
        }

        if (SWITCH_STANCE.wasPressed()) {
            ToggleStancePayloadC2S.send();
        }

        if (OPEN_SKILL_BINDING.wasPressed()) {
            client.setScreen(new SkillBindingScreen(client));
        }

        if (OPEN_SKILL_LEARNING.wasPressed()) {
            client.setScreen(new SkillLearningScreen(client));
        }

        // Get components
        PlayerStanceComponent stanceComponent = ModEntityComponents.PLAYERSTANCE.get(client.player);
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);

        boolean inCombatStance = stanceComponent.isInCombatStance();

        // Always check skill keys to consume the input, but only execute if in combat stance
        if (SKILL_SLOT_1.wasPressed() && inCombatStance) {
            useSkillSlot(classComponent, 0);
        }
        if (SKILL_SLOT_2.wasPressed() && inCombatStance) {
            useSkillSlot(classComponent, 1);
        }
        if (SKILL_SLOT_3.wasPressed() && inCombatStance) {
            useSkillSlot(classComponent, 2);
        }
        if (SKILL_SLOT_4.wasPressed() && inCombatStance) {
            useSkillSlot(classComponent, 3);
        }
        if (SKILL_SLOT_5.wasPressed() && inCombatStance) {
            useSkillSlot(classComponent, 4);
        }
        if (SKILL_SLOT_6.wasPressed() && inCombatStance) {
            useSkillSlot(classComponent, 5);
        }
        if (SKILL_SLOT_7.wasPressed() && inCombatStance) {
            useSkillSlot(classComponent, 6);
        }
        if (SKILL_SLOT_8.wasPressed() && inCombatStance) {
            useSkillSlot(classComponent, 7);
        }
    }

    private static void useSkillSlot(PlayerClassComponent classComponent, int slot) {
        Identifier skillId = classComponent.getBoundSkill(slot);
        if (skillId != null) {
            UseSkillPayloadC2S.send(skillId);
        }
    }
}