package com.sypztep.mamy.common.system.classkill.acolyte.passive;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.PassiveSkill;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public class DivineProtectionPassiveSkill extends PassiveSkill {

    public DivineProtectionPassiveSkill(Identifier id) {
        super(
                id,
                "Divine Protection",
                "Divine blessing reduces damage from undead and demon creatures.",
                10,
                Mamy.id("skill/divine_protection")
        );
    }

    @Override
    protected void initializePassiveEffects() {
    }

    @Override
    public void applyPassiveEffects(PlayerEntity player, int skillLevel) {
        super.applyPassiveEffects(player, skillLevel);
    }

    public static float calculateDamageReduction(PlayerEntity player, int skillLevel) {
        if (skillLevel <= 0) return 0;

        int baseLevel = ModEntityComponents.LIVINGLEVEL.get(player).getLevel();
        return (1f * skillLevel) + (0.04f * (baseLevel + 1));
    }

    @Override
    protected void addPassiveEffectsDescription(List<Text> tooltip, int skillLevel) {
        tooltip.add(Text.literal("Divine blessing protects against unholy creatures.").formatted(Formatting.GRAY));

        if (skillLevel > 0) {
            int baseLevel = 1; // Default for tooltip
            float reduction = (1f * skillLevel) + (0.04f * (baseLevel + 1));
            tooltip.add(Text.literal("• Damage Reduction vs Undead/Demons: ").formatted(Formatting.GRAY)
                    .append(Text.literal("+" + String.format("%.1f", reduction)).formatted(Formatting.YELLOW)));
            tooltip.add(Text.literal("  (Scales with Base Level)").formatted(Formatting.DARK_GRAY));
        }

        if (skillLevel < getMaxSkillLevel()) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Next Level:").formatted(Formatting.GOLD));
            int baseLevel = 1;
            float nextReduction = (1f * (skillLevel + 1)) + (0.04f * (baseLevel + 1));
            tooltip.add(Text.literal("• Damage Reduction: ").formatted(Formatting.GRAY)
                    .append(Text.literal("+" + String.format("%.1f", nextReduction)).formatted(Formatting.DARK_GREEN)));
        }
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.ACOLYTE;
    }
}