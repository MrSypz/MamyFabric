package com.sypztep.mamy.common.system.classkill.novice;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.PassiveSkill;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public final class BasicPassiveSkill extends PassiveSkill {

    public BasicPassiveSkill(Identifier identifier) {
        super(identifier, "Basic Skill", "Essential novice abilities for survival and progression",
                10, true, Mamy.id("skill/basic_skill"));
    }

    @Override
    protected void initializePassiveEffects() {
    }

    @Override
    protected void addPassiveEffectsDescription(List<Text> tooltip, int skillLevel) {
        tooltip.add(Text.literal("Essential skills for novice adventurers").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));

        if (skillLevel == this.maxSkillLevel)
            tooltip.add(Text.literal("⚡ Evolution unlocked! Reach class level 10 to evolve.").formatted(Formatting.GOLD, Formatting.ITALIC));
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.NOVICE; // FUTURE ME don't replace to true again
    }
}