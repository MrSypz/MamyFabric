package com.sypztep.mamy.common.system.skill.swordman.passive;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.PassiveSkill;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import com.sypztep.mamy.common.util.AttributeModification;

import java.util.List;

public class HPRecoveryPassiveSkill extends PassiveSkill {

    public HPRecoveryPassiveSkill(Identifier id) {
        super(
                id,
                "HP Recovery",
                "Enhanced health regeneration based on your maximum health and training level.",
                ModClasses.SWORDMAN,
                1, // base cost to learn
                1, // upgrade cost per level
                10, // max level
                false, // not a default skill
                Mamy.id("skill/hp_recovery") // icon
        );
    }

    @Override
    protected void initializePassiveEffects() {
        // Add custom health regeneration based on the formula: (5^level) + (0.2 * level * MaxHP)
        addAttributeModification(
                AttributeModification.addValue(
                        ModEntityAttributes.HEALTH_REGEN,
                        Mamy.id("hp_recovery_base"),
                        skillLevel -> {
                            return Math.pow(5, skillLevel);
                        }
                )
        );
    }

    @Override
    protected void addPassiveEffectsDescription(List<Text> tooltip, int skillLevel) {
        tooltip.add(Text.literal("Enhanced health regeneration through training and conditioning.").formatted(Formatting.GRAY));

        double baseRegen = Math.pow(5, skillLevel);
        double percentageRegen = 0.2 * skillLevel;

        tooltip.add(Text.literal("• Base Health Regen: ").formatted(Formatting.GRAY)
                .append(Text.literal("+" + String.format("%.0f", baseRegen) + "/tick").formatted(Formatting.GREEN)));
        tooltip.add(Text.literal("• Percentage Health Regen: ").formatted(Formatting.GRAY)
                .append(Text.literal("+" + String.format("%.1f", percentageRegen * 100) + "% of Max HP").formatted(Formatting.GREEN)));

        if (skillLevel < getMaxSkillLevel()) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Next Level:").formatted(Formatting.GOLD));
            double nextBaseRegen = Math.pow(5, skillLevel + 1);
            double nextPercentageRegen = 0.2 * (skillLevel + 1);
            tooltip.add(Text.literal("• Base Health Regen: ").formatted(Formatting.GRAY)
                    .append(Text.literal("+" + String.format("%.0f", nextBaseRegen) + "/tick").formatted(Formatting.DARK_GREEN)));
            tooltip.add(Text.literal("• Percentage Health Regen: ").formatted(Formatting.GRAY)
                    .append(Text.literal("+" + String.format("%.1f", nextPercentageRegen * 100) + "% of Max HP").formatted(Formatting.DARK_GREEN)));
        }
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.SWORDMAN;
    }
}
