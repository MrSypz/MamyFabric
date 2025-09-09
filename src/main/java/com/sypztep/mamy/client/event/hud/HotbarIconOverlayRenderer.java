package com.sypztep.mamy.client.event.hud;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.screen.PlayerInfoScreen;
import com.sypztep.mamy.client.screen.SkillBindingScreen;
import com.sypztep.mamy.client.screen.SkillLearningScreen;
import com.sypztep.mamy.client.screen.overlay.IconOverlayManager;
import net.minecraft.util.Identifier;

public final class HotbarIconOverlayRenderer {

    public static void register() {
        IconOverlayManager.clearAll();

        IconOverlayManager.addScreenIcon(
                Identifier.ofVanilla("icon/accessibility"),
                "Character Stats",
                "View your character's attributes, stats, and progression",
                PlayerInfoScreen::new
        );

        IconOverlayManager.addScreenIcon(
                Mamy.id("overlay/skills"),
                "Skills",
                "Learn and upgrade your skills",
                SkillLearningScreen::new
        );

        IconOverlayManager.addScreenIcon(
                Mamy.id("overlay/binding"),
                "Skill Bindings",
                "Configure skill hotkeys",
                SkillBindingScreen::new
        );
    }
}