package com.sypztep.mamy.client.event.hud;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.screen.PassiveAbilityScreen;
import com.sypztep.mamy.client.screen.PlayerInfoScreen;
import com.sypztep.mamy.client.screen.SkillBindingScreen;
import com.sypztep.mamy.client.screen.SkillLearningScreen;
import com.sypztep.mamy.client.screen.overlay.IconOverlayManager;
import net.minecraft.util.Identifier;

public class HotbarIconOverlayRenderer {
    private static final String HOTBAR_GROUP_ID = "hotbar_icons";

    public static void register() {
        IconOverlayManager.clearAll();

        IconOverlayManager.registerIconGroup(HOTBAR_GROUP_ID, IconOverlayManager.IconPosition.HOTBAR_RIGHT, true);

        IconOverlayManager.addScreenIcon(
                HOTBAR_GROUP_ID,
                Identifier.ofVanilla("icon/accessibility"),
                "Character Stats",
                "View your character's attributes, stats, and progression",
                PlayerInfoScreen::new
        );

        IconOverlayManager.addScreenIcon(
                HOTBAR_GROUP_ID,
                Mamy.id("textures/gui/icons/skills_icon.png"),
                "Skills",
                "Learn and upgrade your skills",
                SkillLearningScreen::new
        );

        IconOverlayManager.addScreenIcon(
                HOTBAR_GROUP_ID,
                Mamy.id("textures/gui/icons/keybind_icon.png"),
                "Skill Bindings",
                "Configure skill hotkeys",
                SkillBindingScreen::new
        );

        IconOverlayManager.addScreenIcon(
                HOTBAR_GROUP_ID,
                Mamy.id("textures/gui/icons/passive_icon.png"),
                "Passive Table",
                "Check your passive ability",
                PassiveAbilityScreen::new
        );
    }
}