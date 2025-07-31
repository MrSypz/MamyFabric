package com.sypztep.mamy.client;

import com.sypztep.mamy.common.component.living.PlayerStanceComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.payload.UseSkillPayloadC2S;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SkillKeyBindings {
    public static KeyBinding BLOODLUST_SKILL; // Shift + E
    public static KeyBinding FIREBALL_SKILL; // Shift + Q  
    public static KeyBinding HEALING_LIGHT_SKILL; // Shift + R
    public static KeyBinding ARROW_SHOWER_SKILL; // Shift + F


    public static void register() {
        // Basic Skills (Shift + Key)
        BLOODLUST_SKILL = registerSkillKey("bloodlust_skill", GLFW.GLFW_KEY_Z, "category.mamy.skills");
        FIREBALL_SKILL = registerSkillKey("fireball_skill", GLFW.GLFW_KEY_X, "category.mamy.skills");
        HEALING_LIGHT_SKILL = registerSkillKey("healing_light_skill", GLFW.GLFW_KEY_C, "category.mamy.skills");
        ARROW_SHOWER_SKILL = registerSkillKey("arrow_shower_skill", GLFW.GLFW_KEY_V, "category.mamy.skills");


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

    private static void handleSkillInputs(MinecraftClient client) {
        if (client.player == null || client.currentScreen != null) return;

        PlayerStanceComponent stanceComponent = ModEntityComponents.PLAYERSTANCE.get(client.player);

        if (!stanceComponent.isInCombatStance()) {
            return;
        }

        // Basic Skills (Shift + Key)
        if (BLOODLUST_SKILL.wasPressed()) {
            useSkill("bloodlust");
        } else if (FIREBALL_SKILL.wasPressed()) {
            useSkill("basic_attack");
        } else if (HEALING_LIGHT_SKILL.wasPressed()) {
            useSkill("healing_light");
        } else if (ARROW_SHOWER_SKILL.wasPressed()) {
            useSkill("arrow_shower");
        } else if (ARROW_SHOWER_SKILL.wasPressed()) {
            useSkill("shield_bash");
        }
    }


    private static void useSkill(String skillId) {
        UseSkillPayloadC2S.send(skillId);
    }
}
