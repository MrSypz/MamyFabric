package com.sypztep.mamy.client;

import com.sypztep.mamy.client.screen.ClassEvolutionScreen;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.component.living.PlayerStanceComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.network.server.ToggleStancePayloadC2S;
import com.sypztep.mamy.common.system.skill.SkillCastDelayManager;
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

    // Chord detection
    private static boolean[] keyDown = new boolean[4];
    private static int chordMask = 0;
    private static long chordTimeout = 0;
    private static final long CHORD_TIMEOUT_MS = 25;

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
        SkillCastDelayManager.getInstance().tick();

        if (SWITCH_STANCE.wasPressed()) ToggleStancePayloadC2S.send();
        if (CLASS_SELECTOR.wasPressed()) client.setScreen(new ClassEvolutionScreen(client));

        PlayerStanceComponent stanceComponent = ModEntityComponents.PLAYERSTANCE.get(client.player);
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);

        if (!stanceComponent.isInCombatStance()) return;

        long currentTime = System.currentTimeMillis();
        updateVisualStates(currentTime);

        boolean[] currentDown = new boolean[4];
        currentDown[0] = SKILL_SLOT_1.isPressed();
        currentDown[1] = SKILL_SLOT_2.isPressed();
        currentDown[2] = SKILL_SLOT_3.isPressed();
        currentDown[3] = SKILL_SLOT_4.isPressed();

        boolean anyPressEvent = false;
        int pressEventMask = 0;
        for (int i = 0; i < 4; i++) {
            if (currentDown[i] && !keyDown[i]) {
                anyPressEvent = true;
                pressEventMask |= 1 << i;
            }
        }

        int currentMask = 0;
        for (int i = 0; i < 4; i++) {
            if (currentDown[i]) currentMask |= 1 << i;
        }

        if (anyPressEvent) {
            chordMask |= pressEventMask;
            chordTimeout = currentTime + CHORD_TIMEOUT_MS;
        }

        if (currentMask == 0) {
            if (chordMask != 0 && chordTimeout > 0) {
                triggerSlot(classComponent, chordMask, currentTime);
            }
            chordMask = 0;
            chordTimeout = 0;
        }

        if (chordTimeout > 0 && currentTime >= chordTimeout && chordMask != 0) {
            triggerSlot(classComponent, chordMask, currentTime);
            chordMask = 0;
            chordTimeout = 0;
        }

        keyDown = currentDown.clone();
    }

    private static void triggerSlot(PlayerClassComponent classComponent, int mask, long currentTime) {
        int slot = getSlotFromMask(mask);
        if (slot != -1) {
            attemptSkillUse(classComponent, slot);
            setKeyVisualPressed(slot, currentTime);
        }
    }

    private static int getSlotFromMask(int mask) {
        return switch (mask) {
            case 1 -> 0;   // Z
            case 2 -> 1;   // X
            case 4 -> 2;   // C
            case 8 -> 3;   // V
            case 3 -> 4;   // Z + X
            case 6 -> 5;   // X + C
            case 12 -> 6;  // C + V
            case 9 -> 7;   // Z + V
            default -> -1;
        };
    }

    private static void attemptSkillUse(PlayerClassComponent classComponent, int slot) {
        Identifier skillId = classComponent.getBoundSkill(slot);
        if (skillId == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int skillLevel = classComponent.getSkillLevel(skillId);

        if (SkillUsabilityChecker.checkClientUsability(client.player, skillId, skillLevel).isUsable())
            SkillCastingManager.getInstance().startCasting(skillId, skillLevel);
    }

    private static void setKeyVisualPressed(int keyIndex, long currentTime) {
        if (keyIndex < 0 || keyIndex >= keyVisualStates.length) return;
        keyVisualStates[keyIndex] = true;
        keyVisualPressTimes[keyIndex] = currentTime;
    }

    private static void updateVisualStates(long currentTime) {
        for (int i = 0; i < keyVisualStates.length; i++)
            if (keyVisualStates[i] && currentTime - keyVisualPressTimes[i] > KEY_HIGHLIGHT_DURATION)
                keyVisualStates[i] = false;
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
    public static String getKeybindDisplayName(int slotIndex) {
        return switch (slotIndex) {
            case 0 -> SKILL_SLOT_1.getBoundKeyLocalizedText().getString(); // Z
            case 1 -> SKILL_SLOT_2.getBoundKeyLocalizedText().getString(); // X
            case 2 -> SKILL_SLOT_3.getBoundKeyLocalizedText().getString(); // C
            case 3 -> SKILL_SLOT_4.getBoundKeyLocalizedText().getString(); // V
            case 4 -> SKILL_SLOT_1.getBoundKeyLocalizedText().getString() + "+" +
                    SKILL_SLOT_2.getBoundKeyLocalizedText().getString(); // Z+X
            case 5 -> SKILL_SLOT_2.getBoundKeyLocalizedText().getString() + "+" +
                    SKILL_SLOT_3.getBoundKeyLocalizedText().getString(); // X+C
            case 6 -> SKILL_SLOT_3.getBoundKeyLocalizedText().getString() + "+" +
                    SKILL_SLOT_4.getBoundKeyLocalizedText().getString(); // C+V
            case 7 -> SKILL_SLOT_1.getBoundKeyLocalizedText().getString() + "+" +
                    SKILL_SLOT_4.getBoundKeyLocalizedText().getString(); // Z+V
            default -> "?";
        };
    }
}