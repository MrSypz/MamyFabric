package com.sypztep.mamy.client;

import com.sypztep.mamy.client.screen.ClassEvolutionScreen;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.component.living.PlayerStanceComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.network.server.ToggleStancePayloadC2S;
import com.sypztep.mamy.common.system.skill.SkillCastingManager;
import com.sypztep.mamy.common.system.skill.SkillUsabilityChecker;
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
    private static final boolean[] keyVisualStates = new boolean[8]; // Visual states for all 8 slots
    private static final long[] keyVisualPressTimes = new long[8];
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
        if (client.isPaused()) return;

        SkillCastingManager.getInstance().tick();

        if (SWITCH_STANCE.wasPressed()) ToggleStancePayloadC2S.send();
        if (CLASS_SELECTOR.wasPressed()) client.setScreen(new ClassEvolutionScreen(client));


        PlayerStanceComponent stanceComponent = ModEntityComponents.PLAYERSTANCE.get(client.player);
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);

        if (!stanceComponent.isInCombatStance()) return;

        long currentTime = System.currentTimeMillis();
        updateVisualStates(currentTime);

        boolean shiftPressed = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT);

        handleSkillKeyPress(SKILL_SLOT_1, shiftPressed ? 4 : 0, classComponent);
        handleSkillKeyPress(SKILL_SLOT_2, shiftPressed ? 5 : 1, classComponent);
        handleSkillKeyPress(SKILL_SLOT_3, shiftPressed ? 6 : 2, classComponent);
        handleSkillKeyPress(SKILL_SLOT_4, shiftPressed ? 7 : 3, classComponent);
    }

    private static void handleSkillKeyPress(KeyBinding keyBinding, int slotIndex, PlayerClassComponent classComponent) {
        if (keyBinding.wasPressed()) {
            setKeyVisualPressed(slotIndex);
            attemptSkillUse(classComponent, slotIndex);
        }
    }

    private static void attemptSkillUse(PlayerClassComponent classComponent, int slot) {
        Identifier skillId = classComponent.getBoundSkill(slot);
        if (skillId == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int skillLevel = classComponent.getSkillLevel(skillId);

        // Quick validation before attempting to cast
        SkillUsabilityChecker.UsabilityCheck check =
                SkillUsabilityChecker.checkClientUsability(client.player, skillId, skillLevel);

        if (check.isUsable()) {
            // Skill is usable, attempt casting
            SkillCastingManager.getInstance().startCasting(skillId, skillLevel);
        } else {
            // Only provide feedback for certain failure types to avoid spam
            if (check.result == SkillUsabilityChecker.SkillUsabilityResult.INSUFFICIENT_RESOURCE ||
                    check.result == SkillUsabilityChecker.SkillUsabilityResult.NOT_LEARNED) {
                SkillUsabilityChecker.sendUsabilityFeedback(client.player, check);
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