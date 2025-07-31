package com.sypztep.mamy.client;

import com.sypztep.mamy.common.component.living.PlayerStanceComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.payload.UseSkillPayloadC2S;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class SkillKeyBindings {

    public static KeyBinding BLOODLUST_SKILL; // Shift + E
    public static KeyBinding FIREBALL_SKILL; // Shift + Q  
    public static KeyBinding HEALING_LIGHT_SKILL; // Shift + R
    public static KeyBinding ARROW_SHOWER_SKILL; // Shift + F
    public static KeyBinding SHIELD_BASH_SKILL; // Shift + C
    public static KeyBinding SHADOW_STEP_SKILL; // Shift + X

    // Ultimate skills - more complex combinations
    public static KeyBinding BERSERKER_RAGE_SKILL; // Ctrl + Shift + Z
    public static KeyBinding METEOR_STRIKE_SKILL; // Ctrl + Shift + X
    public static KeyBinding DIVINE_JUDGMENT_SKILL; // Ctrl + Shift + C

    public static void register() {
        // Basic Skills (Shift + Key)
        BLOODLUST_SKILL = registerSkillKey("bloodlust_skill", GLFW.GLFW_KEY_E, "category.mamy.skills");
        FIREBALL_SKILL = registerSkillKey("fireball_skill", GLFW.GLFW_KEY_Q, "category.mamy.skills");
        HEALING_LIGHT_SKILL = registerSkillKey("healing_light_skill", GLFW.GLFW_KEY_R, "category.mamy.skills");
        ARROW_SHOWER_SKILL = registerSkillKey("arrow_shower_skill", GLFW.GLFW_KEY_F, "category.mamy.skills");
        SHIELD_BASH_SKILL = registerSkillKey("shield_bash_skill", GLFW.GLFW_KEY_C, "category.mamy.skills");
        SHADOW_STEP_SKILL = registerSkillKey("shadow_step_skill", GLFW.GLFW_KEY_X, "category.mamy.skills");

        // Ultimate Skills (Ctrl + Shift + Key)
        BERSERKER_RAGE_SKILL = registerUltimateSkillKey("berserker_rage_skill", GLFW.GLFW_KEY_Z, "category.mamy.ultimate_skills");
        METEOR_STRIKE_SKILL = registerUltimateSkillKey("meteor_strike_skill", GLFW.GLFW_KEY_X, "category.mamy.ultimate_skills");
        DIVINE_JUDGMENT_SKILL = registerUltimateSkillKey("divine_judgment_skill", GLFW.GLFW_KEY_C, "category.mamy.ultimate_skills");

        ClientTickEvents.END_CLIENT_TICK.register(SkillKeyBindings::handleSkillInputs);
    }

    private static KeyBinding registerSkillKey(String translationKey, int keyCode, String category) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy." + translationKey,
                InputUtil.Type.KEYSYM,
                keyCode,
                category
        ));
    }

    private static KeyBinding registerUltimateSkillKey(String translationKey, int keyCode, String category) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mamy." + translationKey,
                InputUtil.Type.KEYSYM,
                keyCode,
                category
        ));
    }

    private static void handleSkillInputs(MinecraftClient client) {
        if (client.player == null || client.currentScreen != null) return;

        PlayerStanceComponent stanceComponent = ModEntityComponents.PLAYERSTANCE.get(client.player);

        // Check if in combat stance for skills
        if (!stanceComponent.isInCombatStance()) {
            return; // Skills only work in combat stance
        }

        // Basic Skills (Shift + Key)
        if (isShiftPressed() && !isCtrlPressed()) {
            if (BLOODLUST_SKILL.wasPressed()) {
                useSkill("bloodlust", client);
            } else if (FIREBALL_SKILL.wasPressed()) {
                useSkill("fireball", client);
            } else if (HEALING_LIGHT_SKILL.wasPressed()) {
                useSkill("healing_light", client);
            } else if (ARROW_SHOWER_SKILL.wasPressed()) {
                useSkill("arrow_shower", client);
            } else if (SHIELD_BASH_SKILL.wasPressed()) {
                useSkill("shield_bash", client);
            } else if (SHADOW_STEP_SKILL.wasPressed()) {
                useSkill("shadow_step", client);
            }
        }

        // Ultimate Skills (Ctrl + Shift + Key)
        if (isShiftPressed() && isCtrlPressed()) {
            if (BERSERKER_RAGE_SKILL.wasPressed()) {
                useSkill("berserker_rage", client);
            } else if (METEOR_STRIKE_SKILL.wasPressed()) {
                useSkill("meteor_strike", client);
            } else if (DIVINE_JUDGMENT_SKILL.wasPressed()) {
                useSkill("divine_judgment", client);
            }
        }
    }

    private static boolean isShiftPressed() {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ||
                InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private static boolean isCtrlPressed() {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) ||
                InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    private static void useSkill(String skillId, MinecraftClient client) {
        // Send packet to server
        UseSkillPayloadC2S.send(skillId);

        // Optional: Show local feedback
        client.player.sendMessage(
                Text.literal("Using skill: " + skillId)
                        .formatted(Formatting.YELLOW),
                true // Overlay
        );
    }
}
