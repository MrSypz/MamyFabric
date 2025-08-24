package com.sypztep.mamy.common.system.skill.novice;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.PassiveSkill;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public final class BasicPassiveSkill extends PassiveSkill {

    public BasicPassiveSkill(Identifier identifier) {
        super(identifier, "Basic Skill", "Essential novice abilities for survival and progression",
                ModClasses.NOVICE, 0, 1, 10, true, Mamy.id("skill/basic_skill"));
    }

    @Override
    protected void initializePassiveEffects() {
    }

    @Override
    protected void addPassiveEffectsDescription(List<Text> tooltip, int skillLevel) {
        tooltip.add(Text.literal("Essential skills for novice adventurers").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));

        if (skillLevel >= 6)
            tooltip.add(Text.literal("✓ Amadeus Storage Access").formatted(Formatting.GREEN));
        if (skillLevel >= 10)
            tooltip.add(Text.literal("✓ First Job Class Evolution").formatted(Formatting.GREEN));

        // Show next unlock if not max level
        if (skillLevel < 10) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Next unlock at level " + getNextUnlockLevel(skillLevel) + ":").formatted(Formatting.YELLOW));
            switch (getNextUnlockLevel(skillLevel)) {
                case 6 -> tooltip.add(Text.literal("- Amadeus Storage Access").formatted(Formatting.GRAY));
                case 10 -> tooltip.add(Text.literal("- First Job Class Evolution").formatted(Formatting.GOLD, Formatting.BOLD));
            }
        }

        // Special note about evolution requirement
        if (skillLevel >= 10) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("⚡ Evolution unlocked! Reach class level 10 to evolve.").formatted(Formatting.GOLD, Formatting.ITALIC));
        }
    }

    private int getNextUnlockLevel(int currentLevel) {
        if (currentLevel < 5) return 5;
        if (currentLevel < 6) return 6;
        if (currentLevel < 7) return 7;
        return 10; // Max level
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return true; // All Classes
    }

    public static boolean canUseAmadeusStorage(PlayerEntity player) {
        return getBasicSkillLevel(player) >= 6;
    }

    public static boolean canEvolveToFirstJob(PlayerEntity player) {
        return getBasicSkillLevel(player) >= 10;
    }

    private static int getBasicSkillLevel(PlayerEntity player) {
        var classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        var skillManager = classComponent.getClassManager().getSkillManager();
        return skillManager.getSkillLevel(Mamy.id("basic_skill"));
    }
}