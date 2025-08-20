package com.sypztep.mamy.client;

import com.sypztep.mamy.client.screen.ClassEvolutionScreen;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.component.living.PlayerStanceComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.payload.ToggleStancePayloadC2S;
import com.sypztep.mamy.common.system.skill.SkillCastingManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {

    // UI Keys
    public static KeyBinding SWITCH_STANCE;
    public static KeyBinding CLASS_SELECTOR;

    // Skill Slots
    public static KeyBinding SKILL_SLOT_1; // Z
    public static KeyBinding SKILL_SLOT_2; // X
    public static KeyBinding SKILL_SLOT_3; // C
    public static KeyBinding SKILL_SLOT_4; // V

    // Visual feedback for HUD
    private static boolean[] keyVisualStates = new boolean[8]; // Visual states for all 8 slots
    private static long[] keyVisualPressTimes = new long[8];
    private static final long KEY_HIGHLIGHT_DURATION = 150;

    public static void register() {
        SWITCH_STANCE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.switch_stance",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.mamy.combat"
        ));

        CLASS_SELECTOR = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy.class_selector",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.mamy.combat"
        ));

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

        ClientTickEvents.END_CLIENT_TICK.register(ModKeyBindings::handleKeyInputs);
    }

    private static void handleKeyInputs(MinecraftClient client) {
        if (client.player == null) return;
        if (client.currentScreen != null) return;

        SkillCastingManager.getInstance().tick();

        if (SWITCH_STANCE.wasPressed()) {
            ToggleStancePayloadC2S.send();
        }
        if (CLASS_SELECTOR.wasPressed()) {
            client.setScreen(new ClassEvolutionScreen(client));
        }

        PlayerStanceComponent stanceComponent = ModEntityComponents.PLAYERSTANCE.get(client.player);
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);
        boolean inCombatStance = stanceComponent.isInCombatStance();

        long currentTime = System.currentTimeMillis();

        // Update visual states
        updateVisualStates(currentTime);

        if (!inCombatStance) return;

        // Check for Shift key
        boolean shiftPressed = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT);

        if (shiftPressed) {
            // Shift combinations (slots 5-8)
            if (SKILL_SLOT_1.wasPressed()) { // Shift + Z = Slot 5
                useSkillSlot(classComponent, 4);
                setKeyVisualPressed(4);
                return;
            }
            if (SKILL_SLOT_2.wasPressed()) { // Shift + X = Slot 6
                useSkillSlot(classComponent, 5);
                setKeyVisualPressed(5);
                return;
            }
            if (SKILL_SLOT_3.wasPressed()) { // Shift + C = Slot 7
                useSkillSlot(classComponent, 6);
                setKeyVisualPressed(6);
                return;
            }
            if (SKILL_SLOT_4.wasPressed()) { // Shift + V = Slot 8
                useSkillSlot(classComponent, 7);
                setKeyVisualPressed(7);
            }
        } else {
            // Normal keys (slots 1-4)
            if (SKILL_SLOT_1.wasPressed()) { // Z = Slot 1
                useSkillSlot(classComponent, 0);
                setKeyVisualPressed(0);
            }
            if (SKILL_SLOT_2.wasPressed()) { // X = Slot 2
                useSkillSlot(classComponent, 1);
                setKeyVisualPressed(1);
            }
            if (SKILL_SLOT_3.wasPressed()) { // C = Slot 3
                useSkillSlot(classComponent, 2);
                setKeyVisualPressed(2);
            }
            if (SKILL_SLOT_4.wasPressed()) { // V = Slot 4
                useSkillSlot(classComponent, 3);
                setKeyVisualPressed(3);
            }
        }
    }

    private static void setKeyVisualPressed(int keyIndex) {
        keyVisualStates[keyIndex] = true;
        keyVisualPressTimes[keyIndex] = System.currentTimeMillis();
    }

    private static void updateVisualStates(long currentTime) {
        for (int i = 0; i < keyVisualStates.length; i++) {
            if (keyVisualStates[i] && currentTime - keyVisualPressTimes[i] > KEY_HIGHLIGHT_DURATION) {
                keyVisualStates[i] = false;
            }
        }
    }

    public static boolean isKeyVisuallyPressed(int keyIndex) {
        if (keyIndex < 0 || keyIndex >= keyVisualStates.length) return false;
        return keyVisualStates[keyIndex];
    }

    private static void useSkillSlot(PlayerClassComponent classComponent, int slot) {
        Identifier skillId = classComponent.getBoundSkill(slot);
        if (skillId != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                int skillLevel = classComponent.getClassManager().getSkillManager().getSkillLevel(skillId);
                SkillCastingManager.getInstance().startCasting(skillId, skillLevel);
            }
        }
    }

    public static KeyBinding getSkillKeybinding(int slotIndex) {
        return switch (slotIndex) {
            case 0 -> SKILL_SLOT_1;
            case 1 -> SKILL_SLOT_2;
            case 2 -> SKILL_SLOT_3;
            case 3 -> SKILL_SLOT_4;
            default -> null;
        };
    }
}