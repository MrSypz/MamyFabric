package com.sypztep.mamy.common.system.classkill.archer.passive;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.PassiveSkill;
import com.sypztep.mamy.common.util.AttributeModification;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class VulturesEyePassiveSkill extends PassiveSkill {

    public VulturesEyePassiveSkill(Identifier id, List<SkillRequirement> skillRequirements) {
        super(
                id,
                "Vulture's Eye",
                "Increases range and Accuracy when using bows or crossbow.",
                ModClasses.ARCHER,
                1,
                1,
                10,
                false,
                Mamy.id("skill/passive/vultures_eye"),
                skillRequirements
        );
    }

    @Override
    protected void initializePassiveEffects() {
        addAttributeModification(
                AttributeModification.addValue(
                        ModEntityAttributes.ACCURACY,
                        Mamy.id("vultures_eye_accuracy"),
                        skillLevel -> skillLevel
                )
        );

        addAttributeModification(
                AttributeModification.addValue(
                        ModEntityAttributes.ARROW_SPEED,
                        Mamy.id("vultures_eye_arrow_speed"),
                        skillLevel -> skillLevel * 0.25
                )
        );
    }

    @Override
    protected void addPassiveEffectsDescription(List<Text> tooltip, int skillLevel) {
        tooltip.add(Text.literal("Accuracy: +" + skillLevel).formatted(Formatting.GREEN));
        tooltip.add(Text.literal("Arrow Speed: +" + String.format("%.1f", skillLevel * 0.25)).formatted(Formatting.GREEN));
        tooltip.add(Text.literal("Increases arrow range by ~" + skillLevel + " block(s)").formatted(Formatting.YELLOW));
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.ARCHER;
    }
}